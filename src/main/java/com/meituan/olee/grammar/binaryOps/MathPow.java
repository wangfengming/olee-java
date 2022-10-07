package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class MathPow extends BinaryOpGrammar {
    public MathPow() {
        super(70);
        this.rtl = true;
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // null ^ ? => 0
        if (left == null && right instanceof Number) {
            return 0;
        }
        // ? ^ null => 1
        if (right == null && left instanceof Number) {
            return 1;
        }
        // number ^ number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.pow((Number) left, (Number) right);
        }
        throw new EvaluateException("unsupported type for ^");
    }
}
