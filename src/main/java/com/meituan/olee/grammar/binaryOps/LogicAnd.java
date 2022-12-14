package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

import java.util.function.Supplier;

public class LogicAnd extends BinaryOpGrammar {

    public LogicAnd() {
        super(20, true);
    }

    @Override
    public Object applyDelay(Object left, Supplier<?> right) throws EvaluateException {
        return OperatorUtils.isFalsy(left) ? left : right.get();
    }
}
