package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class MathMinus extends BinaryOpGrammar {
    public MathMinus() {
        super(50);
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // null - ? => -?
        if (left == null && right instanceof Number) {
            return this.apply(0, right);
        }
        // ? - null => ?
        if (right == null && left instanceof Number) {
            return left;
        }
        // number - number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.minus((Number) left, (Number) right);
        }
        throw new EvaluateException("unsupported type for -");
    }
}
