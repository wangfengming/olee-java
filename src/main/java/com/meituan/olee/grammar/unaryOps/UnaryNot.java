package com.meituan.olee.grammar.unaryOps;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.UnaryOpGrammar;
import com.meituan.olee.util.OperatorUtils;

public class UnaryNot extends UnaryOpGrammar {
    public UnaryNot() {
        this.priority = 90;
    }

    @Override
    public Object apply(Object right) throws EvaluateException {
        return OperatorUtils.isFalsy(right);
    }
}
