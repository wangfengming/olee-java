package com.meituan.olee.ast;

import java.util.Objects;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

public class UnaryNode extends AstNode {
    public String operator;
    public AstNode right;

    public UnaryNode(String operator, AstNode right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        return context.grammar.unaryOps.get(this.operator)
            .apply(this.right.evaluate(context));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryNode unaryNode = (UnaryNode) o;
        return operator.equals(unaryNode.operator)
            && right.equals(unaryNode.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, right);
    }

    @Override
    public String toString() {
        return "UnaryNode{" +
            "operator='" + operator + '\'' +
            ", right=" + right +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.operator + this.right.toExprString(grammar);
    }
}
