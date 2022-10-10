package com.meituan.olee.ast;

import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.util.OperatorUtils;

import java.util.Objects;

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
        return OperatorUtils.isFalsy(this.test.evaluate(context))
            ? this.alternate.evaluate(context)
            : this.consequent.evaluate(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionNode that = (ConditionNode) o;
        return test.equals(that.test)
            && consequent.equals(that.consequent)
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
            + this.consequent.toBoundingExprString(grammar)
            + " : "
            + this.alternate.toBoundingExprString(grammar);
    }

    @Override
    public String toBoundingExprString(Grammar grammar) {
        return "(" + this.toExprString(grammar) + ")";
    }
}
