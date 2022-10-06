package com.meituan.olee.grammar.binaryOps;

import java.util.function.Supplier;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

public class LogicOr extends BinaryOpGrammar {
    public LogicOr() {
        this.priority = 20;
        this.delay = true;
    }

    @Override
    public Object applyDelay(Object left, Supplier<?> right) throws EvaluateException {
        return OperatorUtils.isFalsy(left) ? right.get() : left;
    }
}
