package com.meituan.olee.ast;

import com.meituan.olee.Callback;
import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.grammar.Grammar;

import java.util.LinkedList;
import java.util.Objects;

public class FunctionNode extends AstNode {
    public AstNode expr;
    public LinkedList<String> argNames;

    public FunctionNode(AstNode expr) {
        this.expr = expr;
    }

    public FunctionNode(LinkedList<String> argNames) {
        this.argNames = argNames;
    }

    public FunctionNode(AstNode expr, LinkedList<String> argNames) {
        this.expr = expr;
        this.argNames = argNames;
    }

    @Override
    public Callback evaluate(EvaluateContext context) {
        return (args) -> {
            EvaluateContext newContext = context.withArgs(args);
            if (this.argNames != null) {
                newContext = newContext.withLocals();
                int idx = 0;
                for (String argName : this.argNames) {
                    newContext.locals.put(argName, args[idx]);
                    idx++;
                }
            }
            return this.expr.evaluate(newContext);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionNode that = (FunctionNode) o;
        return Objects.equals(expr, that.expr) && Objects.equals(argNames, that.argNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, argNames);
    }

    @Override
    public String toString() {
        return "FunctionNode{" +
            "expr=" + expr +
            ", argNames=" + argNames +
            '}';
    }

    @Override
    public String toExprString(Grammar grammar) {
        String result = this.expr.toExprString(grammar);
        if (this.argNames != null) {
            return "fn (" + String.join(", ", this.argNames) + ") => " + result;
        }
        return result;
    }
}
