package com.meituan.olee.evaluator;

import com.meituan.olee.exceptions.EvaluateException;

public interface PropertyAccessor {
    public Object get(Object target, Object key) throws EvaluateException;
}
