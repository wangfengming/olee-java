package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

import java.util.Objects;

public class SpreadNode extends AstNode {
    public AstNode value;

    public SpreadNode(AstNode value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        return this.value.evaluate(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpreadNode that = (SpreadNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SpreadNode{" +
            "value=" + value +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return "..." + this.value.toBoundingExprString(grammar);
    }
}
