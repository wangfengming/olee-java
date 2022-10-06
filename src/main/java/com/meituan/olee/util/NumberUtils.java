package com.meituan.olee.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public abstract class NumberUtils {
    public static boolean isDouble(Number value) {
        return value instanceof Double || value instanceof Float;
    }

    public static boolean isInt(Number value) {
        return value instanceof Integer || value instanceof Short || value instanceof Byte;
    }

    public static BigDecimal convertToBigDecimal(Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    public static BigInteger convertToBigInteger(Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }
        return BigInteger.valueOf(value.longValue());
    }

    public static Number parse(CharSequence text) {
        if (text == null || text.length() == 0) {
            return 0;
        }
        String trimmed = NumberUtils.trimAllWhitespace(text);
        if (trimmed.matches("^-?\\d+$")) {
            return Long.valueOf(trimmed);
        }
        return Double.valueOf(trimmed);
    }

    private static String trimAllWhitespace(CharSequence text) {
        int len = text.length();
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int compare(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            BigDecimal leftBigDecimal = NumberUtils.convertToBigDecimal(left);
            BigDecimal rightBigDecimal = NumberUtils.convertToBigDecimal(right);
            return leftBigDecimal.compareTo(rightBigDecimal);
        } else if (left instanceof Double || right instanceof Double) {
            return Double.compare(left.doubleValue(), right.doubleValue());
        } else if (left instanceof Float || right instanceof Float) {
            return Float.compare(left.floatValue(), right.floatValue());
        } else if (left instanceof BigInteger || right instanceof BigInteger) {
            BigInteger leftBigInteger = NumberUtils.convertToBigInteger(left);
            BigInteger rightBigInteger = NumberUtils.convertToBigInteger(right);
            return leftBigInteger.compareTo(rightBigInteger);
        } else if (left instanceof Long || right instanceof Long) {
            return Long.compare(left.longValue(), right.longValue());
        } else if (left instanceof Integer || right instanceof Integer) {
            return Integer.compare(left.intValue(), right.intValue());
        } else if (left instanceof Short || right instanceof Short) {
            return Short.compare(left.shortValue(), right.shortValue());
        } else if (left instanceof Byte || right instanceof Byte) {
            return Byte.compare(left.byteValue(), right.byteValue());
        } else {
            // Unknown Number subtypes -> best guess is double comparison
            return Double.compare(left.doubleValue(), right.doubleValue());
        }
    }

    public static Number plus(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return NumberUtils.convertToBigDecimal(left)
                .add(NumberUtils.convertToBigDecimal(right));
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return left.doubleValue() + right.doubleValue();
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left)
                .add(NumberUtils.convertToBigInteger(right));
        }
        if (left instanceof Long || right instanceof Long) {
            return left.longValue() + right.longValue();
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return left.intValue() + right.intValue();
        }
        // fallback double
        return left.doubleValue() + right.doubleValue();
    }

    public static Number minus(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return NumberUtils.convertToBigDecimal(left)
                .subtract(NumberUtils.convertToBigDecimal(right));
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return left.doubleValue() - right.doubleValue();
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left)
                .subtract(NumberUtils.convertToBigInteger(right));
        }
        if (left instanceof Long || right instanceof Long) {
            return left.longValue() - right.longValue();
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return left.intValue() - right.intValue();
        }
        // fallback double
        return left.doubleValue() - right.doubleValue();
    }

    public static Number multiply(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return NumberUtils.convertToBigDecimal(left)
                .multiply(NumberUtils.convertToBigDecimal(right));
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return left.doubleValue() * right.doubleValue();
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left)
                .multiply(NumberUtils.convertToBigInteger(right));
        }
        if (left instanceof Long || right instanceof Long) {
            return left.longValue() * right.longValue();
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return left.intValue() * right.intValue();
        }
        // fallback double
        return left.doubleValue() * right.doubleValue();
    }

    public static Number divide(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            BigDecimal leftBd = NumberUtils.convertToBigDecimal(left);
            BigDecimal rightBd = NumberUtils.convertToBigDecimal(right);
            int scale = Math.max(leftBd.scale(), rightBd.scale());
            return leftBd.divide(rightBd, scale, RoundingMode.HALF_EVEN);
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return left.doubleValue() / right.doubleValue();
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            BigDecimal leftBd = NumberUtils.convertToBigDecimal(left);
            BigDecimal rightBd = NumberUtils.convertToBigDecimal(right);
            return leftBd.divide(rightBd, 0, RoundingMode.HALF_EVEN);
        }
        // divide result always double
        return left.doubleValue() / right.doubleValue();
    }

    public static Number divideFloor(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            BigDecimal leftBd = NumberUtils.convertToBigDecimal(left);
            BigDecimal rightBd = NumberUtils.convertToBigDecimal(right);
            return leftBd.divide(rightBd, 0, RoundingMode.FLOOR).toBigInteger();
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return (long) (left.doubleValue() / right.doubleValue());
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left)
                .divide(NumberUtils.convertToBigInteger(right));
        }
        if (left instanceof Long || right instanceof Long) {
            return left.longValue() / right.longValue();
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return left.intValue() / right.intValue();
        }
        return (long) (left.doubleValue() / right.doubleValue());
    }

    public static Number modulus(Number left, Number right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return NumberUtils.convertToBigDecimal(left)
                .remainder(NumberUtils.convertToBigDecimal(right));
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return left.doubleValue() % right.doubleValue();
        }
        if (left instanceof BigInteger || right instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left)
                .remainder(NumberUtils.convertToBigInteger(right));
        }
        if (left instanceof Long || right instanceof Long) {
            return left.longValue() % right.longValue();
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return left.intValue() % right.intValue();
        }
        // fallback double
        return left.doubleValue() % right.doubleValue();
    }

    public static Number pow(Number left, Number right) {
        if (left instanceof BigDecimal) {
            return NumberUtils.convertToBigDecimal(left).pow(right.intValue());
        }
        if (NumberUtils.isDouble(left) || NumberUtils.isDouble(right)) {
            return Math.pow(left.doubleValue(), right.doubleValue());
        }
        if (left instanceof BigInteger) {
            return NumberUtils.convertToBigInteger(left).pow(right.intValue());
        }
        if (left instanceof Long || right instanceof Long) {
            return (long) Math.pow(left.longValue(), right.longValue());
        }
        if (NumberUtils.isInt(left) || NumberUtils.isInt(right)) {
            return (long) Math.pow(left.intValue(), right.intValue());
        }
        // fallback double
        return Math.pow(left.doubleValue(), right.doubleValue());
    }

}
