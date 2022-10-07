package com.meituan.olee.ast;

import java.util.Objects;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

public class LiteralNode extends AstNode {
    public Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralNode that = (LiteralNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "LiteralNode{" +
            "value=" + value +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        if (this.value == null) return "null";
        if (this.value instanceof String) {
            return "'" + ((String) this.value).replaceAll("'", "''") + "'";
        }
        return this.value.toString();
    }
}
