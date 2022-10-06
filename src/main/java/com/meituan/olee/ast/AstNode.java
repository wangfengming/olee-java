package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Grammar;

public abstract class AstNode {
    public AstNode parent;
    public boolean isMaybeLambda;
    public boolean optional = false;
    public boolean leftOptional = false;

    public abstract Object evaluate(EvaluateContext context) throws EvaluateException;

    public abstract String toExprString(Grammar grammar);

    public String toBoundingExprString(Grammar grammar) {
        return this.toExprString(grammar);
    }
}
