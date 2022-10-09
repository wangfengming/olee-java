package com.meituan.olee.grammar;

import com.meituan.olee.exceptions.EvaluateException;

import java.util.function.Supplier;

public abstract class BinaryOpGrammar {
    public int priority = 0;
    // 是否符合短路原则
    public boolean delay = false;
    // 是否右结合，如 a ^ b ^ c 表示 a ^ (b ^ c) 即 Math.pow(a, Math.pow(b, c))
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
