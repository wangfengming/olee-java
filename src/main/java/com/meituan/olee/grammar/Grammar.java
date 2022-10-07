package com.meituan.olee.grammar;

import com.meituan.olee.grammar.binaryOps.*;
import com.meituan.olee.grammar.unaryOps.UnaryMinus;
import com.meituan.olee.grammar.unaryOps.UnaryNot;
import com.meituan.olee.grammar.unaryOps.UnaryPlus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Grammar {
    public final int PIPE_PRIORITY = 80;
    public final int MEMBER_PRIORITY = 100;
    public final int FUNCTION_CALL_PRIORITY = 100;

    public final Map<String, SymbolGrammar> symbols;
    public final Map<String, UnaryOpGrammar> unaryOps;
    public final Map<String, BinaryOpGrammar> binaryOps;
    public final Map<String, Function<List<?>, ?>> transforms = new HashMap<>();

    public Grammar() {
        this.symbols = this.initSymbols();
        this.unaryOps = this.initUnaryOps();
        this.binaryOps = this.initBinaryOps();
    }

    private Map<String, SymbolGrammar> initSymbols() {
        Map<String, SymbolGrammar> symbols = new HashMap<>();

        symbols.put("[", new SymbolGrammar(TokenType.openBracket));
        symbols.put("?.[", new SymbolGrammar(TokenType.optionalBracket));
        symbols.put("]", new SymbolGrammar(TokenType.closeBracket));
        symbols.put("|", new SymbolGrammar(TokenType.pipe));
        symbols.put("{", new SymbolGrammar(TokenType.openCurly));
        symbols.put("}", new SymbolGrammar(TokenType.closeCurly));
        symbols.put(".", new SymbolGrammar(TokenType.dot));
        symbols.put("?.", new SymbolGrammar(TokenType.optionalDot));
        symbols.put(":", new SymbolGrammar(TokenType.colon));
        symbols.put(",", new SymbolGrammar(TokenType.comma));
        symbols.put("(", new SymbolGrammar(TokenType.openParen));
        symbols.put("?.(", new SymbolGrammar(TokenType.optionalParen));
        symbols.put(")", new SymbolGrammar(TokenType.closeParen));
        symbols.put("?", new SymbolGrammar(TokenType.question));
        symbols.put("def", new SymbolGrammar(TokenType.def));
        symbols.put("=", new SymbolGrammar(TokenType.assign));
        symbols.put(";", new SymbolGrammar(TokenType.semi));

        return symbols;
    }

    private Map<String, UnaryOpGrammar> initUnaryOps() {
        Map<String, UnaryOpGrammar> unaryOps = new HashMap<>();

        unaryOps.put("!", new UnaryNot());
        unaryOps.put("+", new UnaryPlus());
        unaryOps.put("-", new UnaryMinus());

        return unaryOps;
    }

    private Map<String, BinaryOpGrammar> initBinaryOps() {
        Map<String, BinaryOpGrammar> binaryOps = new HashMap<>();

        binaryOps.put("+", new MathPlus());
        binaryOps.put("-", new MathMinus());
        binaryOps.put("*", new MathMultiply());
        binaryOps.put("/", new MathDivide());
        binaryOps.put("//", new MathDivideFloor());
        binaryOps.put("%", new MathModulus());
        binaryOps.put("^", new MathPow());

        binaryOps.put("==", new CompareEq());
        binaryOps.put("!=", new CompareNe());
        binaryOps.put(">", new CompareGt());
        binaryOps.put(">=", new CompareGe());
        binaryOps.put("<", new CompareLt());
        binaryOps.put("<=", new CompareLe());
        binaryOps.put("in", new CompareIn());

        binaryOps.put("&&", new LogicAnd());
        binaryOps.put("||", new LogicOr());

        return binaryOps;
    }
}
