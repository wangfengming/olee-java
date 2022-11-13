package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Grammar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ObjectNode extends AstNode {
    public LinkedList<Entry> entries;

    public ObjectNode(LinkedList<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        Map<Object, Object> result = new HashMap<>();
        this.entries.forEach((entry) -> {
            if (entry.key != null) {
                result.put(entry.key.evaluate(context), entry.value.evaluate(context));
            } else {
                Object value = entry.value.evaluate(context);
                if (value == null) return;
                if (value instanceof Map) {
                    result.putAll((Map<?, ?>) value);
                } else {
                    throw new EvaluateException("unsupported type for ...{}");
                }
            }
        });
        return result;
    }

    public static class Entry {
        public AstNode key;
        public AstNode value;

        public Entry(AstNode value) {
            this.value = value;
        }

        public Entry(AstNode key, AstNode value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry that = (Entry) o;
            return Objects.equals(key, that.key) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "ObjectEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
        }

        public String toExprString(Grammar grammar) {
            if (this.key == null) {
                return this.value.toExprString(grammar);
            }
            String key = this.key instanceof LiteralNode
                ? this.key.toExprString(grammar)
                : "[" + this.key.toExprString(grammar) + "]";
            return key + ": " + this.value.toExprString(grammar);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectNode that = (ObjectNode) o;
        return entries.equals(that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public String toString() {
        return "ObjectNode{" +
            "entries=" + entries +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.entries.stream()
            .map((entry) -> entry.toExprString(grammar))
            .collect(Collectors.joining(", ", "{", "}"));
    }
}
