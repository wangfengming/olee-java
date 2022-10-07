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
        // ? + null => ?
        if (left == null && this.isSupported(right)) {
            return right;
        }
        // null + ? => ?
        if (right == null && this.isSupported(left)) {
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
        if (left instanceof Map && right instanceof Map) {
            Map<Object, Object> result = new HashMap<>((Map<?, ?>) left);
            result.putAll((Map<?, ?>) right);
            return result;
        }
        if (left instanceof List && right instanceof List) {
            List<Object> result = new ArrayList<>((List<?>) left);
            result.addAll((List<?>) right);
            return result;
        }
        throw new EvaluateException("unsupported type for +");
    }

    private boolean isSupported(Object target) {
        return target instanceof Number
            || target instanceof String
            || target instanceof List
            || target instanceof Map;
    }
}
