package com.meituan.olee.evaluator;

import com.meituan.olee.exceptions.EvaluateException;

import java.util.List;
import java.util.Map;

public class DefaultPropertyAccessor implements PropertyAccessor {
    @Override
    public Object get(Object target, Object key, boolean computed) throws EvaluateException {
        if (target instanceof Map) {
            return ((Map<?, ?>) target).get(key);
        }

        if (computed && (target instanceof List || target instanceof String)) {
            int index;
            if (key instanceof Number) {
                index = ((Number) key).intValue();
            } else if (key instanceof String) {
                index = Integer.parseInt((String) key);
            } else {
                throw new EvaluateException("Cannot read property " + key + " for" + target);
            }
            int size = target instanceof List
                ? ((List<?>) target).size()
                : ((String) target).length();
            if (index < 0 || index >= size) return null;
            if (target instanceof List) {
                return ((List<?>) target).get(index);
            }
            return ((String) target).substring(index, index + 1);
        }

        // 暂时只支持 List、Map 的属性读取。
        // 其他类型需要反射搞一波，我不会~ 好像可以用 commons-beanutils PropertyUtils.getProperty
        throw new EvaluateException("Not supported!");
    }
}
