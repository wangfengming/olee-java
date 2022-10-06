package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class MathPlus extends BinaryOpGrammar {
    public MathPlus() {
        this.priority = 50;
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // ? + null => ?
        if (left == null && (right instanceof Number || right instanceof String)) {
            return right;
        }
        // null + ? => ?
        if (right == null && (left instanceof Number || left instanceof String)) {
            return left;
        }
        // number + number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.plus((Number) left, (Number) right);
        }
        // string + string => string
        if (left instanceof String && right instanceof String) {
            return (String) left + right;
        }
        // string + number => string
        if (left instanceof String && right instanceof Number) {
            return (String) left + right;
        }
        // number + string => string
        if (left instanceof Number && right instanceof String) {
            return left.toString() + right;
        }
        throw new EvaluateException("unsupported type for +");
    }
}
