package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;

import java.util.function.Supplier;

public class LogicNullish extends BinaryOpGrammar {
    public LogicNullish() {
        super(20, true);
    }

    @Override
    public Object applyDelay(Object left, Supplier<?> right) throws EvaluateException {
        return left == null ? right.get() : left;
    }
}
