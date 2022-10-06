package com.meituan.olee.evaluator;

@FunctionalInterface
public interface Expression<T> {
    public T evaluate(Object variables);
}
