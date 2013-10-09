package com.common;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: wula
 * Date: 13-10-9
 * Time: 下午9:58
 * To change this template use File | Settings | File Templates.
 */
public class RequestMappingHandlerMappingInterceptor extends HandlerInterceptorAdapter {
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        mergeContollerMappingToView(modelAndView, handler);
    }

    /**
     * 把view名称和controller @RequestMapping 值整合为一个新的路径
     * @param mv
     * @param handler
     */
    protected void mergeContollerMappingToView(ModelAndView mv, Object handler)
    {
        if(mv == null)
            return;

        String vn=mv.getViewName();

        if(vn == null)
            return;

        String cm=getControllerMapping(handler);

        if(cm != null)
        {
            if(!cm.endsWith("/"))
                cm+="/";

            if(cm.startsWith("/"))
                cm=cm.substring(1);

            vn=cm+vn;
            mv.setViewName(vn);
        }
    }

    /**
     * 获取controller @RequestMapping 值
     * @param handler
     * @return
     */
    protected String getControllerMapping(Object handler)
    {
        HandlerMethod handlerMethod = (HandlerMethod) handler ;
        Class<?> hc=handlerMethod.getBean().getClass();

        RequestMapping mapping = AnnotationUtils.findAnnotation(hc, RequestMapping.class);

        String re="";
        if(mapping != null)
        {
            String[] mvs=mapping.value();

            re=mvs[0];
        }


        return re;
    }
}
