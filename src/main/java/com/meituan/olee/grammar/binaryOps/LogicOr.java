package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

import java.util.function.Supplier;

public class LogicOr extends BinaryOpGrammar {
    public LogicOr() {
        super(20, true);
    }

    @Override
    public Object applyDelay(Object left, Supplier<?> right) throws EvaluateException {
        return OperatorUtils.isFalsy(left) ? right.get() : left;
    }
}
