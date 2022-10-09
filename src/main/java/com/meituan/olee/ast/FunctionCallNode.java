package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Callback;
import com.meituan.olee.grammar.Grammar;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionCallNode extends AstNode {
    public AstNode func;
    public List<AstNode> args;

    public boolean isTransform;

    public FunctionCallNode(AstNode func, List<AstNode> args) {
        this.func = func;
        this.args = args;
    }

    public FunctionCallNode isTransform(boolean isTransform) {
        this.isTransform = isTransform;
        return this;
    }

    public FunctionCallNode optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public FunctionCallNode leftOptional(boolean leftOptional) {
        this.leftOptional = leftOptional;
        return this;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        Object fn = null;
        if (this.func instanceof IdentifierNode) {
            // 优先读取 transforms
            fn = context.grammar.transforms.get(((IdentifierNode) this.func).value);
        }
        if (fn == null) {
            fn = this.func.evaluate(context);
        }
        if (fn == null) {
            // a?.b
            if (this.optional) {
                context.leftNull = true;
                return null;
            }
            // a?.b.c
            if (this.leftOptional && context.leftNull) {
                return null;
            }
            throw new EvaluateException("null is not a function");
        }
        context.leftNull = false;
        if (!(fn instanceof Callback)) {
            throw new EvaluateException(fn + " is not a function");
        }
        Object[] args = this.args.stream()
            .map((node) -> node.evaluate(context))
            .toArray();
        return ((Callback) fn).apply(args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCallNode that = (FunctionCallNode) o;
        return func.equals(that.func)
            && args.equals(that.args)
            && isTransform == that.isTransform
            && optional == that.optional
            && leftOptional == that.leftOptional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(func, args, isTransform, optional, leftOptional);
    }

    @Override
    public String toString() {
        return "FunctionCallNode{" +
            "func=" + func +
            ", args=" + args +
            ", isTransform=" + isTransform +
            ", optional=" + optional +
            ", leftOptional=" + leftOptional +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        if (this.isTransform) {
            return this.args.get(0).toExprString(grammar)
                + "|"
                + (
                this.func instanceof IdentifierNode
                    ? this.func.toExprString(grammar)
                    : "(" + this.func.toExprString(grammar) + ")")
                + "("
                + this.args.stream().skip(1)
                .map((arg) -> arg.toExprString(grammar))
                .collect(Collectors.joining(", "))
                + ")";
        }
        return this.func.toExprString(grammar)
            + (this.optional ? "?.(" : "(")
            + this.args.stream()
            .map((arg) -> arg.toExprString(grammar))
            .collect(Collectors.joining(", "))
            + ")";
    }
}
