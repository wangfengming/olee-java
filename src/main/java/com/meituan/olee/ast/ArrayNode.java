package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Grammar;

import java.util.*;
import java.util.stream.Collectors;

public class ArrayNode extends AstNode {
    public LinkedList<AstNode> value;

    public ArrayNode(LinkedList<AstNode> value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        ArrayList<Object> result = new ArrayList<>();
        this.value.forEach((node) -> {
            Object value = node.evaluate(context);
            if (node instanceof SpreadNode) {
                if (value == null) return;
                if (value instanceof String) {
                    List<String> values = ((String) value)
                        .codePoints()
                        .mapToObj((cp) -> new String(Character.toChars(cp)))
                        .collect(Collectors.toList());
                    result.addAll(values);
                } else if (value instanceof Collection) {
                    result.addAll((Collection<?>) value);
                } else {
                    throw new EvaluateException("unsupported type for ...[]");
                }
            } else {
                result.add(value);
            }
        });
        return result;
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
