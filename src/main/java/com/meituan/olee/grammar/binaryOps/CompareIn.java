package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

import java.util.List;

public class CompareIn extends BinaryOpGrammar {
    public CompareIn() {
        super(40);
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        if (left instanceof CharSequence && right instanceof String) {
            return ((String) right).contains((CharSequence) left);
        }
        if (right instanceof List) {
            for (Object item : (List<?>) right) {
                if (OperatorUtils.compare(left, item) == OperatorUtils.CompareResult.eq) {
                    return true;
                }
            }
        }
        return false;
    }
}
