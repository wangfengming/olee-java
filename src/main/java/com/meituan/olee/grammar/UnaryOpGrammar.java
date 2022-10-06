package com.meituan.olee.grammar;

import com.meituan.olee.exceptions.EvaluateException;

public abstract class UnaryOpGrammar {
    public int priority = 0;

    public UnaryOpGrammar() {
    }

    public UnaryOpGrammar(int priority) {
        this.priority = priority;
    }

    public Object apply(Object right) throws EvaluateException {
        throw new EvaluateException("Not implemented");
    }
}
