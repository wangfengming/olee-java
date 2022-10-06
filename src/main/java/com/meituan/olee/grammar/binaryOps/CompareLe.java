package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

public class CompareLe extends BinaryOpGrammar {
    public CompareLe() {
        this.priority = 40;
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        OperatorUtils.CompareResult cr = OperatorUtils.compare(left, right);
        return cr == OperatorUtils.CompareResult.lt || cr == OperatorUtils.CompareResult.eq;
    }
}
