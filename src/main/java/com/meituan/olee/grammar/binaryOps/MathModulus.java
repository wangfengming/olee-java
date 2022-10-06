package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class MathModulus extends BinaryOpGrammar {
    public MathModulus() {
        this.priority = 60;
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // null % ? => 0
        if (left == null && right instanceof Number) {
            return 0;
        }
        // ? % null => divide by null
        if (right == null && left instanceof Number) {
            throw new ArithmeticException("% by null");
        }
        // number % number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.modulus((Number) left, (Number) right);
        }
        throw new EvaluateException("unsupported type for %");
    }
}
