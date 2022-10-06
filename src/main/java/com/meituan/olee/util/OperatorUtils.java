package com.meituan.olee.util;

public class OperatorUtils {
    public enum CompareResult {
        eq,
        lt,
        gt,
        unknown,
    }

    /**
     * 比较对象大小，和 js 对齐，不做 deep equality 判断。
     * 基本类型和比较相等，引用类型比较指针。不相等的引用，> < 的结果都是 false。
     *
     * @param left  left Object to compare
     * @param right right Object to compare
     * @return 比较结果 CompareResult。
     */
    public static CompareResult compare(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return OperatorUtils.normalizeCompareResult(NumberUtils.compare((Number) left, (Number) right));
        }
        if (left instanceof CharSequence && right instanceof CharSequence) {
            return OperatorUtils.normalizeCompareResult(left.toString().compareTo(right.toString()));
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            return OperatorUtils.normalizeCompareResult(((Boolean) left).compareTo((Boolean) right));
        }
        if (left == right) {
            return CompareResult.eq;
        }
        if (left == null && right instanceof Number) {
            return OperatorUtils.normalizeCompareResult(NumberUtils.compare(0, (Number) right));
        }
        if (right == null && left instanceof Number) {
            return OperatorUtils.normalizeCompareResult(NumberUtils.compare((Number) left, 0));
        }
        if (left == null || right == null) {
            return CompareResult.unknown;
        }
        if (left.equals(right)) {
            return CompareResult.eq;
        }
        return CompareResult.unknown;
    }

    private static CompareResult normalizeCompareResult(int value) {
        if (value < 0) return CompareResult.lt;
        if (value > 0) return CompareResult.gt;
        return CompareResult.eq;
    }

    /**
     * 是否为假值。
     * <a href="https://developer.mozilla.org/en-US/docs/Glossary/Falsy">MDN Falsy</a>
     */
    public static boolean isFalsy(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Boolean && !(Boolean) obj) {
            return true;
        }
        if (obj instanceof Number && NumberUtils.compare((Number) obj, 0) == 0) {
            return true;
        }
        if (obj instanceof String && obj.equals("")) {
            return true;
        }
        return false;
    }
}
