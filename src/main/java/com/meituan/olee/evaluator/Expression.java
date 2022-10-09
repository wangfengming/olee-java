package com.meituan.olee.evaluator;

import com.meituan.olee.exceptions.EvaluateException;

@FunctionalInterface
public interface Expression {
    public Object evaluate(Object variables) throws EvaluateException;
}
