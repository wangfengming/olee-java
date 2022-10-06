package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefNode extends AstNode {
    public List<Def> defs;
    public AstNode statement;

    public DefNode(List<Def> defs, AstNode statement) {
        this.defs = defs;
        this.statement = statement;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        Map<String, Object> locals = new HashMap<>();
        this.defs.forEach((def) -> {
            Object value = def.value.evaluate(context.withLocals(locals));
            locals.put(def.name, value);
        });
        return this.statement.evaluate(context.withLocals(locals));
    }

    public static class Def {
        public String name;
        public AstNode value;

        public Def(String name, AstNode value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Def def = (Def) o;
            return name.equals(def.name) && value.equals(def.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public String toString() {
            return "Def{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefNode defNode = (DefNode) o;
        return defs.equals(defNode.defs)
            && statement.equals(defNode.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defs, statement);
    }

    @Override
    public String toString() {
        return "DefNode{" +
            "defs=" + defs +
            ", statement=" + statement +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.defs.stream()
            .map((def) -> "def " + def.name + " = " + def.value.toExprString(grammar))
            .collect(Collectors.joining("; ", "", "; " + this.statement.toExprString(grammar)));
    }

    public String toBoundingExprString(Grammar grammar) {
        return "(" + this.toExprString(grammar) + ")";
    }
}
