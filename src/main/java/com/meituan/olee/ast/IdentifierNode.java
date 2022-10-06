package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Grammar;

import java.util.Objects;

public class IdentifierNode extends AstNode {
    public String value;
    public boolean isArg;
    public int argIndex;

    public IdentifierNode(String value) {
        this.value = value;
    }

    public IdentifierNode argIndex(int argIndex) {
        this.isArg = true;
        this.argIndex = argIndex;
        return this;
    }

    @Override
    public Object evaluate(EvaluateContext context) throws EvaluateException {
        if (context.args != null && this.isArg) {
            return context.args.get(this.argIndex);
        }
        if (context.locals != null && context.locals.containsKey(this.value)) {
            return context.locals.get(this.value);
        }
        if (context.variables == null) {
            throw new EvaluateException("No variables provided for evaluate");
        }
        return context.propertyAccessor.get(context.variables, this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierNode that = (IdentifierNode) o;
        return isArg == that.isArg
            && argIndex == that.argIndex
            && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isArg, argIndex);
    }

    @Override
    public String toString() {
        return "IdentifierNode{" +
            "value='" + value + '\'' +
            ", isArg=" + isArg +
            ", argIndex=" + argIndex +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        if (this.isArg) {
            return this.argIndex > 0 ? "@" + this.argIndex : "@";
        }
        return this.value;
    }
}
