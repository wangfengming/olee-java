package com.meituan.olee;

import java.util.List;
import java.util.function.Function;

import com.meituan.olee.ast.AstNode;
import com.meituan.olee.evaluator.Expression;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.UnaryOpGrammar;
import com.meituan.olee.tokenizer.Token;
import com.meituan.olee.tokenizer.Tokenizer;
import com.meituan.olee.parser.Parser;
import com.meituan.olee.parser.States;
import com.meituan.olee.evaluator.DefaultPropertyAccessor;
import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.evaluator.PropertyAccessor;

public class OneLineExpressionEvaluator {
    private final Grammar grammar;
    private final Tokenizer tokenizer;
    private final States states;
    private final PropertyAccessor propertyAccessor;

    public OneLineExpressionEvaluator() {
        this(new DefaultPropertyAccessor());
    }

    public OneLineExpressionEvaluator(PropertyAccessor propertyAccessor) {
        this.grammar = new Grammar();
        this.tokenizer = new Tokenizer(this.grammar);
        this.states = new States();
        this.propertyAccessor = propertyAccessor;
    }

    public Expression compile(String str) {
        List<Token> tokens = this.tokenizer.tokenize(str);
        Parser parser = new Parser(this.grammar, this.states);
        parser.addTokens(tokens);
        AstNode ast = parser.complete();

        return (variables) -> {
            if (ast == null) return null;
            EvaluateContext context = new EvaluateContext(this.propertyAccessor, this.grammar, variables);
            return ast.evaluate(context);
        };
    }

    public Object evaluate(String str, Object variables) {
        Expression expression = this.compile(str);
        return expression.evaluate(variables);
    }

    public void addBinaryOp(String operator, BinaryOpGrammar binaryOp) {
        this.grammar.binaryOps.put(operator, binaryOp);
        this.tokenizer.updateGrammar(this.grammar);
    }

    public void addUnaryOp(String operator, UnaryOpGrammar unaryOp) {
        this.grammar.unaryOps.put(operator, unaryOp);
        this.tokenizer.updateGrammar(this.grammar);
    }

    public void addTransform(String name, Function<List<?>, ?> fn) {
        this.grammar.transforms.put(name, fn);
    }

    public void removeBinaryOp(String operator) {
        if (this.grammar.binaryOps.remove(operator) != null) {
            this.tokenizer.updateGrammar(this.grammar);
        }
    }

    public void removeUnaryOp(String operator) {
        if (this.grammar.unaryOps.remove(operator) != null) {
            this.tokenizer.updateGrammar(this.grammar);
        }
    }

    public void removeTransform(String name) {
        this.grammar.transforms.remove(name);
    }
}
