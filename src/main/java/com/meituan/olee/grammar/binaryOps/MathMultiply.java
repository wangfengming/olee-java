package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class MathMultiply extends BinaryOpGrammar {
    public MathMultiply() {
        this.priority = 60;
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // null * ? => 0
        if (left == null && right instanceof Number) {
            return 0;
        }
        // ? * null => 0
        if (right == null && left instanceof Number) {
            return 0;
        }
        // number * number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.multiply((Number) left, (Number) right);
        }
        throw new EvaluateException("unsupported type for *");
    }
}
