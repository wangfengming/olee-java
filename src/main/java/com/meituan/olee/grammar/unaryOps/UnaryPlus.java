package com.meituan.olee.grammar.unaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.UnaryOpGrammar;
import com.meituan.olee.util.NumberUtils;

public class UnaryPlus extends UnaryOpGrammar {
    public UnaryPlus() {
        super(90);
    }

    @Override
    public Object apply(Object right) throws EvaluateException {
        if (right instanceof Number) {
            return right;
        }
        if (right instanceof CharSequence) {
            return NumberUtils.parse((CharSequence) right);
        }
        throw new EvaluateException("unsupported type for unary +");
    }
}
