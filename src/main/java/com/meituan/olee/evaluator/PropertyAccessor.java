package com.meituan.olee.evaluator;

import com.meituan.olee.exceptions.EvaluateException;

@FunctionalInterface
public interface PropertyAccessor {
    public Object get(Object target, Object key, boolean computed) throws EvaluateException;
}
