package com.meituan.olee;

import com.meituan.olee.ast.AstNode;
import com.meituan.olee.evaluator.DefaultPropertyAccessor;
import com.meituan.olee.evaluator.EvaluateContext;
import com.meituan.olee.evaluator.Expression;
import com.meituan.olee.evaluator.PropertyAccessor;
import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.UnaryOpGrammar;
import com.meituan.olee.parser.Parser;
import com.meituan.olee.parser.States;
import com.meituan.olee.tokenizer.Token;
import com.meituan.olee.tokenizer.Tokenizer;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public Expression compile(String str) throws ParseException {
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

    public Object evaluate(String str, Object variables) throws ParseException, ExecutionException {
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

    public void addTransform(String name, Callback fn) {
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
