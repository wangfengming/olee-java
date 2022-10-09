package com.meituan.olee.evaluator;

import com.meituan.olee.grammar.Grammar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluateContext {
    public PropertyAccessor propertyAccessor;
    public Grammar grammar;
    public Object variables;
    public Map<String, Object> locals;
    public Object[] args;
    public boolean leftNull;

    public EvaluateContext(PropertyAccessor propertyAccessor, Grammar grammar, Object variables) {
        this.propertyAccessor = propertyAccessor;
        this.grammar = grammar;
        this.variables = variables;
    }

    private EvaluateContext copy() {
        EvaluateContext newContext = new EvaluateContext(this.propertyAccessor, this.grammar, this.variables);
        newContext.locals = this.locals;
        newContext.args = args;
        newContext.leftNull = this.leftNull;
        return newContext;
    }

    public EvaluateContext withLocals() {
        EvaluateContext newContext = this.copy();
        newContext.locals = new HashMap<>();
        if (this.locals != null) {
            newContext.locals.putAll(this.locals);
        }
        return newContext;
    }

    public void addLocal(String name, Object variable) {
        this.locals.put(name, variable);
    }

    public EvaluateContext withArgs(Object[] args) {
        EvaluateContext newContext = this.copy();
        newContext.args = args;
        return newContext;
    }
}
