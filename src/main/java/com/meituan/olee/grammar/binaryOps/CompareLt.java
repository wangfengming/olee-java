package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

public class CompareLt extends BinaryOpGrammar {
    public CompareLt() {
        super(40);
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        return OperatorUtils.compare(left, right) == OperatorUtils.CompareResult.lt;
    }
}
