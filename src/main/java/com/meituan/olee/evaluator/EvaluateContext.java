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
    public List<Object> args;
    public boolean leftNull;

    public EvaluateContext(PropertyAccessor propertyAccessor, Grammar grammar, Object variables) {
        this.propertyAccessor = propertyAccessor;
        this.grammar = grammar;
        this.variables = variables;
    }

    public EvaluateContext withLocals(Map<String, Object> locals) {
        EvaluateContext newContext = new EvaluateContext(this.propertyAccessor, this.grammar, this.variables);
        newContext.locals = new HashMap<>();
        if (this.locals != null) {
            newContext.locals.putAll(this.locals);
        }
        newContext.locals.putAll(locals);
        newContext.args = this.args;
        newContext.leftNull = this.leftNull;
        return newContext;
    }

    public EvaluateContext withArgs(List<Object> args) {
        EvaluateContext newContext = new EvaluateContext(this.propertyAccessor, this.grammar, this.variables);
        newContext.locals = this.locals;
        newContext.args = args;
        newContext.leftNull = this.leftNull;
        return newContext;
    }
}
