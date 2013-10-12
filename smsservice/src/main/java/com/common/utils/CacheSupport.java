/** Created by flym at 13-5-30 *//*

package com.appcenter.cp.component.cache;

import com.alisoft.xplatform.asf.cache.ICache;
import com.alisoft.xplatform.asf.cache.impl.DefaultCacheImpl;
import com.appcenter.cp.component.exception.AssertsException;
import com.appcenter.cp.component.json.JsonUtils;
import com.appcenter.cp.domain.BaseDomain;
import com.appcenter.cp.domain.Key;
import com.appcenter.cp.domain.util.DomainUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

*/
/**
 * 用于全局管理cache的存储和获取,并且直接与memcache 相应组件交互
 * <p/>
 * 此用于屏蔽具体memcached实现层逻辑,其它组件应依赖于此组件而发现不了memcached的存在
 * <p/>
 *
 * @author flym
 *//*

public class CacheSupport {
    private static final Log logger = LogFactory.getLog(CacheSupport.class);
    private static final String cacheClientName = "memCacheClient";

    //	private static IMemcachedCache cache = null;
    private static ICache cache = null;

    static {
        cache = new DefaultCacheImpl(10, 31);
        //暂时使用本地缓存
//		ICacheManager<IMemcachedCache> cacheManager = CacheUtil
//				.getCacheManager(IMemcachedCache.class, MemcachedCacheManager.class.getName());
        //使用默认配置文件
        //cacheManager.start();//todo 在合适的时候开启
        //cache = cacheManager.getCache(cacheClientName);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseDomain> String generateKey(Key key, Class<T> clazz) {
        Class<T> sourceClass = clazz;
        String domainNaming = DomainUtils.getDomainNaming(clazz);
        while(domainNaming == null && !clazz.equals(Object.class)) {
            clazz = (Class<T>) clazz.getSuperclass();
            domainNaming = DomainUtils.getDomainNaming(clazz);
        }

        if(domainNaming == null) {
            throw new AssertsException("类名:" + sourceClass + " 不满足cp domain规则");
        }

        return domainNaming + "-" + key.toString();
    }

    */
/** 放入一个数据 *//*

    public static <T extends BaseDomain> void put(T domain, Class<T> clazz) {
        cache.put(generateKey(domain.key(), clazz), JsonUtils.toInternalJson(domain));
    }

    */
/** 放入一个数据,并在指定秒之后清除 *//*

    public static <T extends BaseDomain> void put(T domain, Class<T> clazz, int TTL) {
        cache.put(generateKey(domain.key(), clazz), JsonUtils.toInternalJson(domain), TTL);
    }

    */
/** 放入一个特定的对象,可以为数组,集合及任意信息,但需要手动指定key *//*

    public static <T> void put(String key, T t) {
        cache.put(key, JsonUtils.toInternalJson(t));
    }

    */
/** 放入一个特定的对象,可以为数组,集合及任意信息,但需要手动指定key,同时在指定的时间之后被清除 *//*

    public static <T> void put(String key, T t, int TTL) {
        cache.put(key, JsonUtils.toInternalJson(t), TTL);
    }

    */
/** 异步地放入一个数据 *//*

    public static <T extends BaseDomain> void asyncPut(T domain, Class<T> clazz) {
//		cache.asynPut(generateKey(domain.key(), clazz), JsonUtils.toInternalJson(domain));
    }

    private static <T> T jsonToObject(String value) {
        if(value == null)
            return null;
        try {
            return JsonUtils.toObject(value);
        } catch(Exception e) {
            logger.error("反解析json失败:" + e.getMessage(), e);
            return null;
        }
    }

    */
/** 获取一个数据,在没有数据时会返回null *//*

    public static <T extends BaseDomain> T get(Key key, Class<T> clazz) {
        String value = (String) cache.get(generateKey(key, clazz));
        return jsonToObject(value);
    }

    */
/** 获取一个数据,在没有数据时会返回null *//*

    public static <T> T get(String key) {
        String value = (String) cache.get(key);
        return jsonToObject(value);
    }

    */
/** 移植一个对象 *//*

    public static <T extends BaseDomain> void remove(T domain, Class<T> clazz) {
        cache.remove(generateKey(domain.key(), clazz));
    }

    */
/** 根据key移除一个对象 *//*

    public static void remove(String key) {
        cache.remove(key);
    }

    */
/**
     * 设置标记值
     *
     * @return 设置是否成功, 如果相应的值已存在, 则返回false, 如不存在, 则返回true, 表示设置成功
     *//*

    public static boolean setFlag(String key) {
//		return cache.add(key, true);todo
        return false;
    }
}
*/
