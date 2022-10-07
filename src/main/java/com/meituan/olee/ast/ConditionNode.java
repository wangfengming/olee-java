package com.meituan.olee.ast;

import java.util.Objects;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.util.OperatorUtils;

public class ConditionNode extends AstNode {
    public AstNode test;
    public AstNode consequent;
    public AstNode alternate;

    public ConditionNode(AstNode test, AstNode consequent, AstNode alternate) {
        this.test = test;
        this.consequent = consequent;
        this.alternate = alternate;
    }

    @Override
    public Object evaluate(EvaluateContext context) {
        Object test = this.test.evaluate(context);
        if (OperatorUtils.isFalsy(test)) {
            return this.alternate.evaluate(context);
        }
        if (this.consequent != null) {
            return this.consequent.evaluate(context);
        }
        return test;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionNode that = (ConditionNode) o;
        return test.equals(that.test)
            && Objects.equals(consequent, that.consequent)
            && alternate.equals(that.alternate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(test, consequent, alternate);
    }

    @Override
    public String toString() {
        return "ConditionNode{" +
            "test=" + test +
            ", consequent=" + consequent +
            ", alternate=" + alternate +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        return this.test.toExprString(grammar)
            + " ? "
            + (this.consequent != null ? this.consequent.toBoundingExprString(grammar) : "")
            + " : "
            + this.alternate.toBoundingExprString(grammar);
    }

    @Override
    public String toBoundingExprString(Grammar grammar) {
        return "(" + this.toExprString(grammar) + ")";
    }
}
