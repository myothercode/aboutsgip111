package com.common.utils;


import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 内部使用json工具
 *
 *
 */
public class JsonUtils {

    public static <T> String toBrowserJson(T obj, List<String> includePropertyList) {
        JSONSerializer jsonSerializer = new JSONSerializer();
        jsonSerializer.config(SerializerFeature.WriteNullStringAsEmpty, true);
        jsonSerializer.config(SerializerFeature.WriteDateUseDateFormat, true);
        jsonSerializer.config(SerializerFeature.WriteMapNullValue, true);
        jsonSerializer.config(SerializerFeature.WriteNullListAsEmpty, true);
        jsonSerializer.config(SerializerFeature.WriteNullBooleanAsFalse, true);
        jsonSerializer.config(SerializerFeature.DisableCircularReferenceDetect, true);
        if(includePropertyList != null)
            jsonSerializer.getPropertyPreFilters().add(new PropertyPreOnlyFilter(includePropertyList));
        jsonSerializer.write(obj);
        String text = jsonSerializer.getWriter().toString();
        return text;
    }

    /**
     * 将对象转换为json字符串
     *
     * @param includeProperties 只转换特定的属性
     */
    public static <T> String toInternalJson(T obj, String... includeProperties) {
        JSONSerializer jsonSerializer = new JSONSerializer();
        jsonSerializer.config(SerializerFeature.WriteNullBooleanAsFalse, true);
        jsonSerializer.config(SerializerFeature.WriteClassName, true);
        if(includeProperties.length != 0) {
            jsonSerializer.getPropertyPreFilters().add(new PropertyPreOnlyFilter(Arrays.asList(includeProperties)));
        }
        jsonSerializer.write(obj);
        return jsonSerializer.getWriter().toString();
    }

    /** 将json字符串转换为对象 */
    @SuppressWarnings("unchecked")
    public static <T> T toObject(String internalJson) {
        DefaultJSONParser jsonParser = new DefaultJSONParser(internalJson);
        return (T) jsonParser.parse();
    }

    /** 计算一个字符串中的字符 . 的个数 */
    private static int countDot(String s) {
        if(s == null)
            return 0;
        int i = 0;
        for(char c : s.toCharArray()) {
            if(c == '.')
                i++;
        }
        return i;
    }

    /** 用于实现过滤指定的字符串，当只有指定的字符串模式被满足时，相应的属性信息才会输出到界面上 */
    private static class PropertyPreOnlyFilter implements PropertyPreFilter {
        private static Map<List<String>, IncludeProperty> cacheMap = Maps.newHashMap();
        private static Pattern validPropertyPattern = Pattern.compile("^[_a-zA-Z0-9]+(?:\\.(?:[_a-zA-Z0-9]+|\\*))*$");
        private IncludeProperty includeProperty;

        private IncludeProperty makeIncludeProperty(List<String> onlyProperties) {
            IncludeProperty includeProperty = cacheMap.get(onlyProperties);
            if(includeProperty != null)
                return includeProperty;

            Set<IncludeProperty.StringProperty> stringPropertySet = Sets.newHashSet();
            Set<IncludeProperty.PatternProperty> patternPropertySet = Sets.newHashSet();

            for(String s : onlyProperties) {
                if(!validPropertyPattern.matcher(s).matches())
                    throw new RuntimeException("错误的路径字符串：" + s);
                int firstIndex = s.indexOf('*');
                if(firstIndex != -1) {
                    //处理*之前的.路径，将其加入stringProperty以便之前能够处理前面的数据
                    String firstString = s.substring(0, firstIndex - 1);//返回result.a.b
                    for(int i = firstString.indexOf('.');i != -1;) {
                        String temp = firstString.substring(0, i);
                        stringPropertySet.add(new IncludeProperty.StringProperty(temp));
                        i = firstString.indexOf('.', i + 1);
                    }
                    stringPropertySet.add(new IncludeProperty.StringProperty(firstString));
                    //从第1个*开始，依次根据.往后推
                    for(int i = s.indexOf('.', firstIndex);i != -1;) {
                        String temp = s.substring(0, i);
                        patternPropertySet.add(new IncludeProperty.PatternProperty(temp));
                        i = s.indexOf('.', i + 1);
                    }
                    patternPropertySet.add(new IncludeProperty.PatternProperty(s));
                } else {
                    stringPropertySet.add(new IncludeProperty.StringProperty(s));
                }
            }
            //排序之
            List<IncludeProperty.StringProperty> stringPropertyList = Lists.newArrayList(stringPropertySet);
            List<IncludeProperty.PatternProperty> patternPropertyList = Lists.newArrayList(patternPropertySet);
            Collections.sort(stringPropertyList);
            Collections.sort(patternPropertyList);

            includeProperty = new IncludeProperty(stringPropertyList, patternPropertyList);
            cacheMap.put(onlyProperties, includeProperty);
            return includeProperty;
        }


        private PropertyPreOnlyFilter(List<String> onlyProperties) {
            includeProperty = makeIncludeProperty(onlyProperties);
        }

        private boolean containInclude(String s) {
            int sPathLength = countDot(s);
            List<IncludeProperty.StringProperty> stringPropertyList = includeProperty.sortedPathList;
            List<IncludeProperty.PatternProperty> patternPropertyList = includeProperty.sortedPatternList;

            for(IncludeProperty.StringProperty sp : stringPropertyList) {
                if(sp.pathLength >= sPathLength && (sp.property.equals(s) || sp.property.startsWith(s + "."))) {
                    return true;
                }
            }

            for(IncludeProperty.PatternProperty pp : patternPropertyList) {
                if(pp.pathLength > sPathLength) {
                    break;
                } else if(pp.pathLength == sPathLength && pp.pattern.matcher(s).matches()) {
                    return true;
                }
            }
            return false;
        }

        public boolean apply(JSONSerializer serializer, Object source, String name) {
            SerialContext nowContext = new SerialContext(serializer.getContext(), source, name);
            String nowPath = getLinkedPath(nowContext);
            return containInclude(nowPath);
        }

        /** 输出结果 类似a.b.c.d等格式，忽略[] */
        private static String getLinkedPath(SerialContext serialContext) {
            //这里有点bad smell，即要考虑parent为null,又要考虑fieldName为null，且对collection判断只能从fieldName，而不能从object入手
            boolean isCollection = serialContext.getFieldName() instanceof Integer;
            boolean isFieldNameNull = serialContext.getFieldName() == null;
            if(serialContext.getParent() == null)
                return isCollection ? "" : isFieldNameNull ? "" : String.valueOf(serialContext.getFieldName());
            String parentLinkedPath = getLinkedPath(serialContext.getParent());
            if(isCollection || isFieldNameNull)
                return parentLinkedPath;
            return parentLinkedPath.length() == 0 ? String.valueOf(serialContext.getFieldName()) :
                    parentLinkedPath + "." + serialContext.getFieldName();
        }

    }

    private static class IncludeProperty {
        private final List<StringProperty> sortedPathList;
        private final List<PatternProperty> sortedPatternList;

        private IncludeProperty(List<StringProperty> sortedPathList, List<PatternProperty> sortedPatternList) {
            this.sortedPathList = sortedPathList;
            this.sortedPatternList = sortedPatternList;
        }

        private static class StringProperty implements Comparable<StringProperty> {
            String property;
            int pathLength;

            private StringProperty(String property) {
                this.property = property;
                pathLength = countDot(property);
            }

            @Override
            public int compareTo(StringProperty o) {
                return Ints.compare(this.pathLength, o.pathLength);
            }

            @Override
            public String toString() {
                return "StringProperty{" +
                        "property='" + property + '\'' +
                        ", pathLength=" + pathLength +
                        '}';
            }
        }

        private static class PatternProperty implements Comparable<PatternProperty> {
            private static Pattern convertStartPattern = Pattern.compile("\\*");
            private static Pattern convertDotPattern = Pattern.compile("\\.");
            private String sourcePatternStr;
            private Pattern pattern;
            private int pathLength;

            private PatternProperty(String sourcePatternStr) {
                this.sourcePatternStr = sourcePatternStr;
                pathLength = countDot(sourcePatternStr);
                String s = sourcePatternStr;
                s = convertStartPattern.matcher(s).replaceAll("[_a-zA-Z0-9]+");
                s = convertDotPattern.matcher(s).replaceAll("\\\\.");
                pattern = Pattern.compile("^" + s + "$");
            }

            @Override
            public int compareTo(PatternProperty o) {
                return Ints.compare(this.pathLength, o.pathLength);
            }

            @Override
            public String toString() {
                return "PatternProperty{" +
                        "sourcePatternStr='" + sourcePatternStr + '\'' +
                        ", pathLength=" + pathLength +
                        '}';
            }
        }
    }
}
