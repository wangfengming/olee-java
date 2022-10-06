package com.meituan.olee.grammar;

import java.util.function.Supplier;

import com.meituan.olee.exceptions.EvaluateException;

public abstract class BinaryOpGrammar {
    public int priority = 0;
    public boolean delay = false;
    public boolean rtl = false;

    public BinaryOpGrammar() {
    }

    public BinaryOpGrammar(int priority) {
        this.priority = priority;
    }

    public BinaryOpGrammar(int priority, boolean delay) {
        this.priority = priority;
        this.delay = delay;
    }

    public BinaryOpGrammar(int priority, boolean delay, boolean rtl) {
        this.priority = priority;
        this.delay = delay;
        this.rtl = rtl;
    }

    public Object apply(Object left, Object right) throws EvaluateException {
        throw new EvaluateException("Not implemented");
    }

    public Object applyDelay(Object left, Supplier<?> right) throws EvaluateException {
        throw new EvaluateException("Not implemented");
    }
}
