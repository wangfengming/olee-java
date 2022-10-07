package com.meituan.olee.ast;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

public class LambdaNode extends AstNode {
    public AstNode expr;

    public LambdaNode(AstNode expr) {
        this.expr = expr;
    }

    @Override
    public Function<List<Object>, Object> evaluate(EvaluateContext context) {
        return (args) -> this.expr.evaluate(context.withArgs(args));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LambdaNode that = (LambdaNode) o;
        return expr.equals(that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }

    @Override
    public String toString() {
        return "LambdaNode{" +
            "expr=" + expr +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.expr.toExprString(grammar);
    }
}
