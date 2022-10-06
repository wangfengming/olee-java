package com.meituan.olee.tokenizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.TokenType;
import com.meituan.olee.util.NumberUtils;

public class Tokenizer {
    private Grammar grammar;
    private Pattern pattern;

    public Tokenizer(Grammar grammar) {
        this.grammar = grammar;
    }

    public void updateGrammar(Grammar grammar) {
        this.grammar = grammar;
        this.pattern = null;
    }

    public List<Token> tokenize(String str) {
        if (this.pattern == null) {
            this.pattern = this.createPattern();
        }
        List<String> elements = this.split(str);
        List<Token> tokens = this.getTokens(elements);
        return tokens;
    }

    private Pattern createPattern() {
        List<String> tokenNames = new ArrayList<>();
        tokenNames.addAll(this.grammar.symbols.keySet());
        tokenNames.addAll(this.grammar.binaryOps.keySet());
        tokenNames.addAll(this.grammar.unaryOps.keySet());
        // sort by length
        tokenNames.sort((a, b) -> b.length() - a.length());
        // escape
        tokenNames = tokenNames.stream()
            .map(this::escapeRegExp)
            .collect(Collectors.toList());

        List<String> regexElements = new ArrayList<>();
        // Strings
        regexElements.add("'(?:''|[^'])*'");
        regexElements.add("\"(?:\"\"|[^\"])*\"");
        // Whitespace
        regexElements.add("\\s+");
        // Booleans
        regexElements.add("\\btrue\\b");
        regexElements.add("\\bfalse\\b");
        // null
        regexElements.add("\\bnull\\b");
        // Comments
        regexElements.add("#.*\\n?");
        // tokens
        regexElements.addAll(tokenNames);
        // Identifiers
        regexElements.add("[a-zA-Z_$][a-zA-Z\\d_$]*|@\\d?");
        // Numerics (without negative symbol)
        regexElements.add("\\d*\\.\\d+|\\d+");
        String regexp = regexElements.stream().collect(Collectors.joining("|", "(", ")"));

        return Pattern.compile(regexp);
    }

    private String escapeRegExp(String str) {
        String result = str.replaceAll("([.*+?^${}()|\\[\\]\\\\])", "\\\\$1");
        if (str.matches("^[a-zA-Z_$][a-zA-Z\\d_$]*$|^@\\d?$")) {
            result = "\\b" + result + "\\b";
        }
        return result;
    }

    private List<String> split(String str) {
        List<String> elements = new LinkedList<>();
        Matcher matcher = this.pattern.matcher(str);
        int offset = 0;
        while (matcher.find()) {
            elements.add(matcher.group(1));
            if (offset < matcher.start()) {
                String invalidToken = str.substring(offset, matcher.start());
                throw new ParseException("Invalid expression token: " + invalidToken);
            }
            offset = matcher.end();
        }
        if (offset != str.length()) {
            String invalidToken = str.substring(offset);
            throw new ParseException("Invalid expression token: " + invalidToken);
        }
        return elements;
    }

    private List<Token> getTokens(List<String> elements) {
        LinkedList<Token> tokens = new LinkedList<>();
        for (String element : elements) {
            if (element.matches("^\\s*$")) {
                if (!tokens.isEmpty()) {
                    tokens.getLast().raw += element;
                }
            } else if (!element.startsWith("#")) {
                boolean isPreferUnaryOp = this.isPreferUnaryOp(tokens);
                Token token = this.createToken(element, isPreferUnaryOp);
                tokens.add(token);
            }
        }
        if (!tokens.isEmpty() && tokens.getLast().type == TokenType.semi) {
            tokens.removeLast();
        }
        return tokens;
    }

    private Token createToken(String element, boolean preferUnaryOp) {
        Token token = new Token(TokenType.literal, element, element);
        if (element.startsWith("\"") || element.startsWith("'")) {
            token.literal = this.unquote(element);
        } else if (element.matches("^(?:(\\d*\\.\\d+)|\\d+)$")) {
            token.literal = NumberUtils.parse(element);
        } else if (element.equals("true")) {
            token.literal = true;
        } else if (element.equals("false")) {
            token.literal = false;
        } else if (element.equals("null")) {
            token.literal = null;
        } else if (this.grammar.symbols.get(element) != null) {
            token.type = this.grammar.symbols.get(element).type;
        } else if (this.grammar.binaryOps.get(element) != null && this.grammar.unaryOps.get(element) != null) {
            token.type = preferUnaryOp ? TokenType.unaryOp : TokenType.binaryOp;
        } else if (this.grammar.binaryOps.get(element) != null) {
            token.type = TokenType.binaryOp;
        } else if (this.grammar.unaryOps.get(element) != null) {
            token.type = TokenType.unaryOp;
        } else if (element.matches("^[a-zA-Z_$][a-zA-Z\\d_$]*$|^@\\d?$")) {
            token.type = TokenType.identifier;
            Matcher matcher = Pattern.compile("^@(\\d?)$").matcher(element);
            if (matcher.matches()) {
                token.isArg = true;
                String index = matcher.group(1);
                token.argIndex = index.equals("") ? 0 : Integer.parseInt(index);
            }
        } else {
            throw new ParseException("Invalid expression token: " + element);
        }
        return token;
    }

    private boolean isPreferUnaryOp(List<Token> tokens) {
        int size = tokens.size();
        if (size == 0) return true;
        TokenType lastType = tokens.get(size - 1).type;
        return TokenType.openBracket == lastType
            || TokenType.openParen == lastType
            || TokenType.binaryOp == lastType
            || TokenType.unaryOp == lastType
            || TokenType.question == lastType
            || TokenType.colon == lastType
            || TokenType.comma == lastType
            || TokenType.assign == lastType
            || TokenType.semi == lastType;
    }

    private String unquote(String str) {
        return str.substring(1, str.length() - 1)
            .replaceAll("\"\"", "\"")
            .replaceAll("''", "'");
    }
}
