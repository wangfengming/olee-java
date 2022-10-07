package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArrayNode extends AstNode {
    public LinkedList<AstNode> value;

    public ArrayNode(LinkedList<AstNode> value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        return this.value.stream().map((node) -> node.evaluate(context)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return value.equals(arrayNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ArrayNode{" +
            "value=" + value +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.value.stream()
            .map((item) -> item.toExprString(grammar))
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
