package com.meituan.olee.evaluator;

@FunctionalInterface
public interface Expression {
    public Object evaluate(Object variables);
}
