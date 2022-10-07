package com.meituan.olee.tokenizer;

import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.grammar.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
    Tokenizer tokenizer = new Tokenizer(new Grammar());

    @Test
    void symbol() {
        // 符号
        Token[] actual = this.tokenizer.tokenize(";[]{}:,()? . ?. ?.[ ?.(def=").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.semi, ";", ";"),
            new Token(TokenType.openBracket, "[", "["),
            new Token(TokenType.closeBracket, "]", "]"),
            new Token(TokenType.openCurly, "{", "{"),
            new Token(TokenType.closeCurly, "}", "}"),
            new Token(TokenType.colon, ":", ":"),
            new Token(TokenType.comma, ",", ","),
            new Token(TokenType.openParen, "(", "("),
            new Token(TokenType.closeParen, ")", ")"),
            new Token(TokenType.question, "?", "? "),
            new Token(TokenType.dot, ".", ". "),
            new Token(TokenType.optionalDot, "?.", "?. "),
            new Token(TokenType.optionalBracket, "?.[", "?.[ "),
            new Token(TokenType.optionalParen, "?.(", "?.("),
            new Token(TokenType.def, "def", "def"),
            new Token(TokenType.assign, "=", "="),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void binaryOp() {
        // 二元操作符
        Token[] actual = this.tokenizer.tokenize("1 + 1 - 1 * / // % ^ == != > >= < <= && || in").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "1", "1 ").literal(1L),
            new Token(TokenType.binaryOp, "+", "+ "),
            new Token(TokenType.literal, "1", "1 ").literal(1L),
            new Token(TokenType.binaryOp, "-", "- "),
            new Token(TokenType.literal, "1", "1 ").literal(1L),
            new Token(TokenType.binaryOp, "*", "* "),
            new Token(TokenType.binaryOp, "/", "/ "),
            new Token(TokenType.binaryOp, "//", "// "),
            new Token(TokenType.binaryOp, "%", "% "),
            new Token(TokenType.binaryOp, "^", "^ "),
            new Token(TokenType.binaryOp, "==", "== "),
            new Token(TokenType.binaryOp, "!=", "!= "),
            new Token(TokenType.binaryOp, ">", "> "),
            new Token(TokenType.binaryOp, ">=", ">= "),
            new Token(TokenType.binaryOp, "<", "< "),
            new Token(TokenType.binaryOp, "<=", "<= "),
            new Token(TokenType.binaryOp, "&&", "&& "),
            new Token(TokenType.binaryOp, "||", "|| "),
            new Token(TokenType.binaryOp, "in", "in"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void unaryOp() {
        // 一元操作符。（left不能是操作数，right一定是操作数）
        Token[] actual1 = this.tokenizer.tokenize("- ! +").toArray(new Token[]{});
        Token[] expected1 = new Token[]{
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.unaryOp, "!", "! "),
            new Token(TokenType.unaryOp, "+", "+"),
        };
        assertArrayEquals(expected1, actual1);

        Token[] actual2 = this.tokenizer.tokenize("-1?+2:!3").toArray(new Token[]{});
        Token[] expected2 = new Token[]{
            new Token(TokenType.unaryOp, "-", "-"),
            new Token(TokenType.literal, "1", "1").literal(1L),
            new Token(TokenType.question, "?", "?"),
            new Token(TokenType.unaryOp, "+", "+"),
            new Token(TokenType.literal, "2", "2").literal(2L),
            new Token(TokenType.colon, ":", ":"),
            new Token(TokenType.unaryOp, "!", "!"),
            new Token(TokenType.literal, "3", "3").literal(3L),
        };
        assertArrayEquals(expected2, actual2);
    }

    @Test
    void preferUnaryOp() {
        // 由于+-同时是一元操作符和二元操作符，当left不是操作数，right是操作数时，才视为一元操作符
        Token[] actual = this.tokenizer.tokenize("- [ - ( - * - ( + - ? - : - , - = - ; -").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.openBracket, "[", "[ "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.openParen, "(", "( "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.binaryOp, "*", "* "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.openParen, "(", "( "),
            new Token(TokenType.unaryOp, "+", "+ "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.question, "?", "? "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.colon, ":", ": "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.comma, ",", ", "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.assign, "=", "= "),
            new Token(TokenType.unaryOp, "-", "- "),
            new Token(TokenType.semi, ";", "; "),
            new Token(TokenType.unaryOp, "-", "-"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void string() {
        // 字符串。只对引号做转义，两个连续的引号转义为一个引号：'' -> ' "" -> ""
        Token[] actual = this.tokenizer.tokenize("\"foo \"\"bar\\\" 'i''m ok'").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "\"foo \"\"bar\\\"", "\"foo \"\"bar\\\" ").literal("foo \"bar\\"),
            new Token(TokenType.literal, "'i''m ok'", "'i''m ok'").literal("i'm ok"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void bool() {
        Token[] actual = this.tokenizer.tokenize("true false").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "true", "true ").literal(true),
            new Token(TokenType.literal, "false", "false").literal(false),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void number() {
        // 数字。有小数点是 double，无小数点是 long
        Token[] actual = this.tokenizer.tokenize("-7.6 20").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.unaryOp, "-", "-"),
            new Token(TokenType.literal, "7.6", "7.6 ").literal(7.6D),
            new Token(TokenType.literal, "20", "20").literal(20L),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void nil() {
        Token[] actual = this.tokenizer.tokenize("null").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "null", "null").literal(null),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void identifier() {
        // 标识符，变量名
        Token[] actual = this.tokenizer.tokenize("_foo9_bar").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.identifier, "_foo9_bar", "_foo9_bar"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void comment() {
        // 注释
        Token[] actual = this.tokenizer.tokenize("#end comment\n\"not a #comment\" # is a comment").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "\"not a #comment\"", "\"not a #comment\" ").literal("not a #comment"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void fullExpression() {
        // 完整句子
        Token[] actual = this.tokenizer.tokenize("6+x -  -17.55*y<= !foo.bar[\"baz\"\"foz\"]|filter(@3>@)").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.literal, "6", "6").literal(6L),
            new Token(TokenType.binaryOp, "+", "+"),
            new Token(TokenType.identifier, "x", "x "),
            new Token(TokenType.binaryOp, "-", "-  "),
            new Token(TokenType.unaryOp, "-", "-"),
            new Token(TokenType.literal, "17.55", "17.55").literal(17.55D),
            new Token(TokenType.binaryOp, "*", "*"),
            new Token(TokenType.identifier, "y", "y"),
            new Token(TokenType.binaryOp, "<=", "<= "),
            new Token(TokenType.unaryOp, "!", "!"),
            new Token(TokenType.identifier, "foo", "foo"),
            new Token(TokenType.dot, ".", "."),
            new Token(TokenType.identifier, "bar", "bar"),
            new Token(TokenType.openBracket, "[", "["),
            new Token(TokenType.literal, "\"baz\"\"foz\"", "\"baz\"\"foz\"").literal("baz\"foz"),
            new Token(TokenType.closeBracket, "]", "]"),
            new Token(TokenType.pipe, "|", "|"),
            new Token(TokenType.identifier, "filter", "filter"),
            new Token(TokenType.openParen, "(", "("),
            new Token(TokenType.identifier, "@3", "@3").argIndex(3),
            new Token(TokenType.binaryOp, ">", ">"),
            new Token(TokenType.identifier, "@", "@").argIndex(0),
            new Token(TokenType.closeParen, ")", ")"),
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    void invalidToken() {
        // 无效的token
        Exception exception1 = assertThrows(ParseException.class, () -> {
            this.tokenizer.tokenize("~");
        });
        assertEquals("Invalid expression token: ~", exception1.getMessage());

        Exception exception2 = assertThrows(ParseException.class, () -> {
            this.tokenizer.tokenize("+~-");
        });
        assertEquals("Invalid expression token: ~", exception2.getMessage());
    }

    @Test
    void ignoreEndSemi() {
        // 尾部分号可以省略
        Token[] actual = this.tokenizer.tokenize("def a = 1;").toArray(new Token[]{});
        Token[] expected = new Token[]{
            new Token(TokenType.def, "def", "def "),
            new Token(TokenType.identifier, "a", "a "),
            new Token(TokenType.assign, "=", "= "),
            new Token(TokenType.literal, "1", "1").literal(1L),
        };
        assertArrayEquals(expected, actual);
    }

}
