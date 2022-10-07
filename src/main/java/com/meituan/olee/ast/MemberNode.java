package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Grammar;

import java.util.Objects;

public class MemberNode extends AstNode {
    public AstNode left;
    public AstNode right;
    public boolean computed;

    public MemberNode(AstNode left, AstNode right) {
        this.left = left;
        this.right = right;
    }

    public MemberNode computed(boolean computed) {
        this.computed = computed;
        return this;
    }

    public MemberNode optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public MemberNode leftOptional(boolean leftOptional) {
        this.leftOptional = leftOptional;
        return this;
    }

    @Override
    public Object evaluate(EvaluateContext context) throws EvaluateException {
        Object left = this.left.evaluate(context);
        if (left == null) {
            if (this.optional) {
                context.leftNull = true;
                return null;
            }
            if (this.leftOptional && context.leftNull) {
                return null;
            }
            throw new EvaluateException("Cannot read properties of null (reading " + this.right.evaluate(context) + ")");
        }
        context.leftNull = false;
        Object right = this.right.evaluate(context);
        return context.propertyAccessor.get(left, right, this.computed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberNode that = (MemberNode) o;
        return computed == that.computed
            && left.equals(that.left)
            && right.equals(that.right)
            && optional == that.optional
            && leftOptional == that.leftOptional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, computed, optional, leftOptional);
    }

    @Override
    public String toString() {
        return "MemberNode{" +
            "left=" + left +
            ", right=" + right +
            ", computed=" + computed +
            ", optional=" + optional +
            ", leftOptional=" + leftOptional +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        if (this.computed) {
            String delimiter = this.optional ? "?.[" : "[";
            return this.left.toExprString(grammar)
                + delimiter
                + this.right.toExprString(grammar)
                + "]";
        }
        String delimiter = this.optional ? "?." : ".";
        return this.left.toExprString(grammar)
            + delimiter
            + (this.right instanceof LiteralNode
            ? ((LiteralNode) this.right).value
            : this.right.toExprString(grammar));
    }
}
