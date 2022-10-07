package com.meituan.olee.grammar.unaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.UnaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class UnaryMinus extends UnaryOpGrammar {
    public UnaryMinus() {
        super(90);
    }

    @Override
    public Object apply(Object right) throws EvaluateException {
        if (right instanceof Number) {
            return NumberUtils.minus(0, (Number) right);
        }
        if (right instanceof CharSequence) {
            return NumberUtils.minus(0, NumberUtils.parse((CharSequence) right));
        }
        throw new EvaluateException("unsupported type for unary -");
    }
}
