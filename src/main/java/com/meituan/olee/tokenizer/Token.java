package com.meituan.olee.tokenizer;

import com.meituan.olee.grammar.TokenType;

import java.util.Objects;

public class Token {
    public TokenType type;
    public String value;
    public String raw;
    public Object literal;
    public boolean isArg;
    public int argIndex;

    public Token(TokenType type, String value, String raw) {
        this.type = type;
        this.value = value;
        this.raw = raw;
    }

    public Token literal(Object literal) {
        this.literal = literal;
        return this;
    }

    public Token argIndex(int argIndex) {
        this.isArg = true;
        this.argIndex = argIndex;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return isArg == token.isArg
            && argIndex == token.argIndex
            && type == token.type
            && value.equals(token.value)
            && raw.equals(token.raw)
            && Objects.equals(literal, token.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, raw, literal, isArg, argIndex);
    }

    @Override
    public String toString() {
        return "Token{" +
            "type=" + type +
            ", value='" + value + '\'' +
            ", raw='" + raw + '\'' +
            ", literal=" + literal +
            ", isArg=" + isArg +
            ", argIndex=" + argIndex +
            '}';
    }
}
