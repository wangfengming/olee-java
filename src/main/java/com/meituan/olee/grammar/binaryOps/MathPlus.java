package com.meituan.olee.grammar.binaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MathPlus extends BinaryOpGrammar {
    public MathPlus() {
        super(50);
    }

    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        // null + number/string => number/string
        if (left == null && (right instanceof Number || right instanceof String)) {
            return right;
        }
        // number/string + null => number/string
        if (right == null && (left instanceof Number || left instanceof String)) {
            return left;
        }
        // number + number => number
        if (left instanceof Number && right instanceof Number) {
            return NumberUtils.plus((Number) left, (Number) right);
        }
        // string + number => string
        if (left instanceof String && right instanceof Number) {
            return (String) left + right;
        }
        // number + string => string
        if (left instanceof Number && right instanceof String) {
            return left.toString() + right;
        }
        // string + string => string
        if (left instanceof String && right instanceof String) {
            return (String) left + right;
        }
        throw new EvaluateException("unsupported type for +");
    }
}
