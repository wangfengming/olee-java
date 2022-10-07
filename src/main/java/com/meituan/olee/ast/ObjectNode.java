package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
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
            result.put(entry.key.evaluate(context), entry.value.evaluate(context));
        });
        return result;
    }

    public static class Entry {
        public AstNode key;
        public AstNode value;

        public Entry(AstNode key, AstNode value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry that = (Entry) o;
            return key.equals(that.key) && value.equals(that.value);
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
        return this.entries.stream().map((entry) -> {
            String key = entry.key instanceof LiteralNode
                ? entry.key.toExprString(grammar)
                : "[" + entry.key.toExprString(grammar) + "]";
            return key + ": " + entry.value.toExprString(grammar);
        }).collect(Collectors.joining(", ", "{", "}"));
    }
}
