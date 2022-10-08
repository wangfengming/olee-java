package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.util.AstUtils;

import java.util.Objects;

public class BinaryNode extends AstNode {
    public String operator;
    public AstNode left;
    public AstNode right;

    public BinaryNode(String operator, AstNode left, AstNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        BinaryOpGrammar binaryOp = context.grammar.binaryOps.get(this.operator);
        Object left = this.left.evaluate(context);
        if (binaryOp.delay) {
            return binaryOp.applyDelay(left, () -> this.right.evaluate(context));
        }
        return binaryOp.apply(left, this.right.evaluate(context));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryNode that = (BinaryNode) o;
        return operator.equals(that.operator)
            && left.equals(that.left)
            && right.equals(that.right)
            && optional == that.optional
            && leftOptional == that.leftOptional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right, optional, leftOptional);
    }

    @Override
    public String toString() {
        return "BinaryNode{" +
            "operator='" + operator + '\'' +
            ", left=" + left +
            ", right=" + right +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        int priority = AstUtils.getPriority(this, grammar);
        int leftPriority = AstUtils.getPriority(this.left, grammar);
        int rightPriority = AstUtils.getPriority(this.right, grammar);
        boolean rtl = AstUtils.rtl(this.left, grammar);
        String left = (rtl ? leftPriority <= priority : leftPriority < priority)
            ? this.left.toBoundingExprString(grammar)
            : this.left.toExprString(grammar);
        String right = rightPriority < priority
            ? this.right.toBoundingExprString(grammar)
            : this.right.toExprString(grammar);
        return left + " " + this.operator + " " + right;
    }

    @Override
    public String toBoundingExprString(Grammar grammar) {
        return "(" + this.toExprString(grammar) + ")";
    }
}
