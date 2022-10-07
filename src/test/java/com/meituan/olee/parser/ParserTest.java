package com.meituan.olee.parser;

import com.meituan.olee.ast.*;
import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.Grammar;
import com.meituan.olee.tokenizer.Token;
import com.meituan.olee.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ParserTest {

    /**
     * 解析表达式，返回抽象语法树
     *
     * @param str 表达式
     * @return ast
     */
    AstNode parse(String str) {
        Grammar grammar = new Grammar();
        Tokenizer tokenizer = new Tokenizer(grammar);
        States states = new States();
        Parser parser = new Parser(grammar, states);
        List<Token> tokens = tokenizer.tokenize(str);
        parser.addTokens(tokens);
        return parser.complete();
    }

    /**
     * 解析表达还是，然后再将ast序列化为表达式。仅用于单测验证。
     *
     * @param str 表达还
     * @return 格式化后的表达式
     */
    String transform(String str) {
        Grammar grammar = new Grammar();
        Tokenizer tokenizer = new Tokenizer(grammar);
        States states = new States();
        Parser parser = new Parser(grammar, states);
        List<Token> tokens = tokenizer.tokenize(str);
        parser.addTokens(tokens);
        AstNode ast = parser.complete();
        return ast == null ? "" : ast.toExprString(grammar);
    }

    @Test
    void literal() {
        // 常量
        // boolean
        assertEquals(
            new LiteralNode(true),
            this.parse("true")
        );
        assertEquals("true", this.transform("true"));
        // null
        assertEquals(
            new LiteralNode(null),
            this.parse("null")
        );
        assertEquals("null", this.transform("null"));
        // string
        assertEquals(
            new LiteralNode("Hello \""),
            this.parse("\"Hello \"\"\"")
        );
        assertEquals("'Hello \"'", this.transform("\"Hello \"\"\""));
        // number。有小数点 double；无小数点 long
        assertEquals(
            new LiteralNode(10L),
            this.parse("10")
        );
        assertEquals("10", this.transform("10"));
        assertEquals(
            new LiteralNode(10.0D),
            this.parse("10.0")
        );
        assertEquals("10.0", this.transform("10.0"));
    }

    @Test
    void unary() {
        // 一元操作符
        assertEquals(
            new BinaryNode(
                "-",
                new BinaryNode(
                    "*",
                    new LiteralNode(1L),
                    new UnaryNode(
                        "!",
                        new UnaryNode(
                            "!",
                            new LiteralNode(true)
                        )
                    )
                ),
                new LiteralNode(2L)
            ),
            this.parse("1*!!true-2")
        );
        assertEquals("1 * !!true - 2", this.transform("1*!!true-2"));
    }

    @Test
    void binary() {
        // 二元操作符。考虑嵌套、优先级、结合性
        assertEquals(
            new BinaryNode(
                "+",
                new LiteralNode(1L),
                new LiteralNode(2L)
            ),
            this.parse("1+2")
        );
        assertEquals("1 + 2", this.transform("1+2"));
        assertEquals(
            new BinaryNode(
                "==",
                new BinaryNode(
                    "+",
                    new LiteralNode(2L),
                    new BinaryNode(
                        "*",
                        new LiteralNode(3L),
                        new LiteralNode(4L)
                    )
                ),
                new BinaryNode(
                    "-",
                    new BinaryNode(
                        "/",
                        new LiteralNode(5L),
                        new LiteralNode(6L)
                    ),
                    new LiteralNode(7L)
                )
            ),
            this.parse("2+3*4==5/6-7")
        );
        assertEquals("2 + 3 * 4 == 5 / 6 - 7", this.transform("2+3*4==5/6-7"));
        assertEquals(
            new BinaryNode(
                "/",
                new BinaryNode(
                    "*",
                    new LiteralNode(4L),
                    new BinaryNode(
                        "+",
                        new LiteralNode(2L),
                        new LiteralNode(3L)
                    )
                ),
                new LiteralNode(5L)
            ),
            this.parse("(4*(2+3))/5")
        );
        assertEquals("4 * (2 + 3) / 5", this.transform("(4*(2+3))/5"));
        // pow 的结合性是右结合，2 ^ 3 ^ 2 ~> 2 ^ (3 ^ 2)
        assertEquals(
            new BinaryNode(
                "^",
                new LiteralNode(2L),
                new BinaryNode(
                    "^",
                    new LiteralNode(3L),

                    new LiteralNode(2L)
                )
            ),
            this.parse("2^3^2")
        );
        assertEquals("2 ^ 3 ^ 2", this.transform("2^3^2"));
        assertEquals(
            new BinaryNode(
                "^",
                new BinaryNode(
                    "^",
                    new LiteralNode(2L),

                    new LiteralNode(3L)
                ),
                new LiteralNode(2L)
            ),
            this.parse("(2^3)^2")
        );
        assertEquals("(2 ^ 3) ^ 2", this.transform("(2^3)^2"));
    }

    @Test
    void ternary() {
        // 三元表达式。考虑嵌套
        assertEquals(
            new ConditionNode(
                new IdentifierNode("foo"),
                new ConditionNode(
                    new IdentifierNode("bar"),
                    new LiteralNode(1L),
                    new LiteralNode(2L)
                ),
                new LiteralNode(3L)
            ),
            this.parse("foo ? (bar ? 1 : 2) : 3")
        );
        assertEquals("foo ? (bar ? 1 : 2) : 3", this.transform("foo ? (bar ? 1 : 2) : 3"));
        assertEquals(
            new ConditionNode(
                new IdentifierNode("foo"),
                new ConditionNode(
                    new IdentifierNode("bar"),
                    new LiteralNode(1L),
                    new LiteralNode(2L)
                ),
                new LiteralNode(3L)
            ),
            this.parse("foo ? bar ? 1 : 2 : 3")
        );
        assertEquals("foo ? (bar ? 1 : 2) : 3", this.transform("foo ? bar ? 1 : 2 : 3"));
    }

    @Test
    void member() {
        // 属性访问。如 a.b.c a["b"].c a.bd.c a['b'+'d'].c
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("bar")
                    ),
                    new LiteralNode("baz")
                ),
                new LiteralNode(1L)
            ),
            this.parse("foo.bar.baz + 1")
        );
        assertEquals("foo.bar.baz + 1", this.transform("foo.bar.baz + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new BinaryNode(
                            "+",
                            new LiteralNode("bar"),
                            new LiteralNode("1")
                        )
                    ).computed(true),
                    new LiteralNode("baz")
                ),
                new LiteralNode(1L)
            ),
            this.parse("foo[\"bar\" + \"1\"].baz + 1")
        );
        assertEquals("foo['bar' + '1'].baz + 1", this.transform("foo[\"bar\" + \"1\"].baz + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new BinaryNode(
                            "+",
                            new LiteralNode("bar"),
                            new LiteralNode("1")
                        )
                    ).computed(true),
                    new LiteralNode("baz")
                ),
                new LiteralNode(1L)
            ),
            this.parse("(foo[\"bar\" + \"1\"]).baz + 1")
        );
        assertEquals("foo['bar' + '1'].baz + 1", this.transform("(foo[\"bar\" + \"1\"]).baz + 1"));
        assertEquals(
            new BinaryNode(
                "==",
                new MemberNode(
                    new IdentifierNode("a"),
                    new LiteralNode("b")
                ),
                new MemberNode(
                    new IdentifierNode("c"),
                    new LiteralNode("d")
                )
            ),
            this.parse("a.b==c.d")
        );
        assertEquals("a.b == c.d", this.transform("a.b==c.d"));
    }

    @Test
    void optional() {
        // 可选链属性访问。
        // 如 a?.b.c 当 a 为null时，返回 null，当 a 非 null 但是 b null 时，npe
        // 同时支持 a?.['b'] a?.()
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("bar")
                    ).optional(true),
                    new LiteralNode("baz")
                ).leftOptional(true),
                new LiteralNode(1L)
            ),
            this.parse("foo?.bar.baz + 1")
        );
        assertEquals("foo?.bar.baz + 1", this.transform("foo?.bar.baz + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new BinaryNode(
                            "+",
                            new LiteralNode("bar"),
                            new LiteralNode("1")
                        )
                    ).computed(true).optional(true),
                    new LiteralNode("baz")
                ).leftOptional(true),
                new LiteralNode(1L)
            ),
            this.parse("foo?.[\"bar\" + \"1\"].baz + 1")
        );
        assertEquals("foo?.['bar' + '1'].baz + 1", this.transform("foo?.[\"bar\" + \"1\"].baz + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new FunctionCallNode(
                        new IdentifierNode("foo"),
                        new LinkedList<>()
                    ).optional(true),
                    new LiteralNode("baz")
                ).leftOptional(true),
                new LiteralNode(1L)
            ),
            this.parse("foo?.().baz + 1")
        );
        assertEquals("foo?.().baz + 1", this.transform("foo?.().baz + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new MemberNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("bar")
                    ).optional(true),
                    new LiteralNode("baz")
                ).computed(true).leftOptional(true),
                new LiteralNode(1L)
            ),
            this.parse("foo?.bar[\"baz\"] + 1")
        );
        assertEquals("foo?.bar['baz'] + 1", this.transform("foo?.bar[\"baz\"] + 1"));
        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("bar")
                    ).optional(true),
                    new LinkedList<>()
                ).leftOptional(true),
                new LiteralNode(1L)
            ),
            this.parse("foo?.bar() + 1")
        );
        assertEquals("foo?.bar() + 1", this.transform("foo?.bar() + 1"));
    }

    @Test
    void objects() {
        // 对象，格式为 {a: 1, b: 2}, {'a': 1, 'b': 2}, {['a' + 1]: 1, b: 2}
        assertEquals(
            new ObjectNode(new LinkedList<ObjectNode.Entry>() {{
                add(new ObjectNode.Entry(
                    new LiteralNode("foo"),
                    new LiteralNode("bar")
                ));
                add(new ObjectNode.Entry(
                    new LiteralNode("tek"),
                    new BinaryNode(
                        "+",
                        new LiteralNode(1L),
                        new LiteralNode(2L)
                    )
                ));
            }}),
            this.parse("{foo: \"bar\", 'tek': 1+2}")
        );
        assertEquals("{'foo': 'bar', 'tek': 1 + 2}", this.transform("{foo: \"bar\", 'tek': 1+2}"));

        assertEquals(
            new ObjectNode(new LinkedList<ObjectNode.Entry>() {{
                add(new ObjectNode.Entry(
                    new LiteralNode("foo"),
                    new ObjectNode(new LinkedList<ObjectNode.Entry>() {{
                        add(new ObjectNode.Entry(
                            new LiteralNode("bar"),
                            new LiteralNode(1L)
                        ));
                    }})
                ));
            }}),
            this.parse("{foo: {bar: 1}}")
        );
        assertEquals("{'foo': {'bar': 1}}", this.transform("{foo: {bar: 1}}"));

        assertEquals(
            new ObjectNode(new LinkedList<>()),
            this.parse("{}")
        );
        assertEquals("{}", this.transform("{}"));

        assertEquals(
            new ObjectNode(new LinkedList<ObjectNode.Entry>() {{
                add(new ObjectNode.Entry(
                    new BinaryNode(
                        "+",
                        new LiteralNode("a"),
                        new LiteralNode(1L)
                    ),
                    new LiteralNode(1L)
                ));
            }}),
            this.parse("{[\"a\"+1]:1}")
        );
        assertEquals("{['a' + 1]: 1}", this.transform("{[\"a\"+1]:1}"));
    }

    @Test
    void arrays() {
        // 数组，格式为 [1,2]
        assertEquals(
            new ArrayNode(new LinkedList<AstNode>() {{
                add(new LiteralNode("foo"));
                add(new BinaryNode(
                    "+",
                    new LiteralNode(1L),
                    new LiteralNode(2L)
                ));
            }}),
            this.parse("[\"foo\", 1+2]")
        );
        assertEquals("['foo', 1 + 2]", this.transform("[\"foo\", 1+2]"));

        assertEquals(
            new ArrayNode(new LinkedList<AstNode>() {{
                add(new LiteralNode(1L));
                add(new ArrayNode(new LinkedList<AstNode>() {{
                    add(new LiteralNode(2L));
                    add(new LiteralNode(3L));
                }}));
            }}),
            this.parse("[1, [2, 3]]")
        );
        assertEquals("[1, [2, 3]]", this.transform("[1, [2, 3]]"));

        assertEquals(
            new ArrayNode(new LinkedList<>()),
            this.parse("[]")
        );
        assertEquals("[]", this.transform("[]"));
    }

    @Test
    void defineVariables() {
        // 定义临时变量。
        assertEquals(
            new DefNode(
                new LinkedList<DefNode.Def>() {{
                    add(new DefNode.Def("a", new LiteralNode(1L)));
                    add(new DefNode.Def("b", new LiteralNode(2L)));
                }},
                new BinaryNode(
                    "+",
                    new IdentifierNode("a"),
                    new IdentifierNode("b")
                )
            ),
            this.parse("def a = 1; def b = 2; a + b")
        );
        assertEquals("def a = 1; def b = 2; a + b", this.transform("def a = 1; def b = 2; a + b"));

        assertEquals(
            new DefNode(
                new LinkedList<DefNode.Def>() {{
                    add(new DefNode.Def("a", new LiteralNode(1L)));
                    add(new DefNode.Def(
                        "b",
                        new BinaryNode(
                            "+",
                            new IdentifierNode("a"),
                            new LiteralNode(1L)
                        )
                    ));
                }},
                new BinaryNode(
                    "+",
                    new IdentifierNode("a"),
                    new IdentifierNode("b")
                )
            ),
            this.parse("def a = 1; def b = a + 1; a + b")
        );
        assertEquals("def a = 1; def b = a + 1; a + b", this.transform("def a = 1; def b = a + 1; a + b"));

        assertEquals(
            new ConditionNode(
                new IdentifierNode("x"),
                new DefNode(
                    new LinkedList<DefNode.Def>() {{
                        add(new DefNode.Def("a", new LiteralNode(1L)));
                    }},
                    new BinaryNode(
                        "+",
                        new IdentifierNode("a"),
                        new LiteralNode(1L)
                    )
                ),
                new IdentifierNode("y")
            ),
            this.parse("x ? (def a = 1; a + 1) : y")
        );
        assertEquals("x ? (def a = 1; a + 1) : y", this.transform("x ? (def a = 1; a + 1) : y"));

        assertNull(this.parse("def a = 1"));
        assertEquals("", this.transform("def a = 1"));

        assertNull(this.parse("def a = 1;"));
        assertEquals("", this.transform("def a = 1;"));

        assertEquals(
            new DefNode(
                new LinkedList<DefNode.Def>() {{
                    add(new DefNode.Def("a", new LiteralNode(1L)));
                }},
                new BinaryNode(
                    "+",
                    new IdentifierNode("a"),
                    new LiteralNode(1L)
                )
            ),
            this.parse("def a = 1; a + 1;")
        );
        assertEquals("def a = 1; a + 1", this.transform("def a = 1; a + 1;"));
    }

    @Test
    void functionCall() {
        // 函数调用
        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new IdentifierNode("tr"),
                    new LinkedList<AstNode>() {{
                        add(new MemberNode(
                            new IdentifierNode("foo"),
                            new LiteralNode("baz")
                        ));
                    }}
                ),
                new LiteralNode(1L)
            ),
            this.parse("tr(foo.baz) + 1")
        );
        assertEquals("tr(foo.baz) + 1", this.transform("tr(foo.baz) + 1"));

        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new IdentifierNode("tr2"),
                    new LinkedList<AstNode>() {{
                        add(new FunctionCallNode(
                            new IdentifierNode("tr"),
                            new LinkedList<AstNode>() {{
                                add(new MemberNode(
                                    new IdentifierNode("foo"),
                                    new LiteralNode("baz")
                                ));
                            }}
                        ));
                        add(new LiteralNode(2L));
                        add(new LiteralNode(3L));
                    }}
                ),
                new LiteralNode(1L)
            ),
            this.parse("tr2(tr(foo.baz),2,3)+1")
        );
        assertEquals("tr2(tr(foo.baz), 2, 3) + 1", this.transform("tr2(tr(foo.baz),2,3)+1"));

        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("baz")
                    ),
                    new LinkedList<AstNode>() {{
                        add(new LiteralNode(1L));
                    }}
                ),
                new LiteralNode(1L)
            ),
            this.parse("foo.baz(1) + 1")
        );
        assertEquals("foo.baz(1) + 1", this.transform("foo.baz(1) + 1"));
    }

    @Test
    void transform() {
        // pipe 管道。func(a.b.c, arg2, arg3) 的函数调用可以写成 a.b.c|func(arg2, arg3) 的形式
        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new IdentifierNode("tr"),
                    new LinkedList<AstNode>() {{
                        add(new MemberNode(
                            new IdentifierNode("foo"),
                            new LiteralNode("baz")
                        ));
                    }}
                ).isTransform(true),
                new LiteralNode(1L)
            ),
            this.parse("foo.baz|tr+1")
        );
        assertEquals("foo.baz|tr() + 1", this.transform("foo.baz|tr+1"));

        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new IdentifierNode("tr2"),
                    new LinkedList<AstNode>() {{
                        add(new FunctionCallNode(
                            new IdentifierNode("tr"),
                            new LinkedList<AstNode>() {{
                                add(new MemberNode(
                                    new IdentifierNode("foo"),
                                    new LiteralNode("baz")
                                ));
                            }}
                        ).isTransform(true));
                        add(new LiteralNode(2L));
                        add(new LiteralNode(3L));
                    }}
                ).isTransform(true),
                new LiteralNode(1L)
            ),
            this.parse("foo.baz|tr|tr2(2,3)+1")
        );
        assertEquals("foo.baz|tr()|tr2(2, 3) + 1", this.transform("foo.baz|tr|tr2(2,3)+1"));

        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new MemberNode(
                        new IdentifierNode("foo"),
                        new LiteralNode("baz")
                    ),
                    new LinkedList<AstNode>() {{
                        add(new LiteralNode(1L));
                    }}
                ).isTransform(true),
                new LiteralNode(1L)
            ),
            this.parse("1|(foo.baz) + 1")
        );
        assertEquals("1|(foo.baz)() + 1", this.transform("1|(foo.baz) + 1"));
    }

    @Test
    void lambda() {
        // 定义函数。@ @1 @2 分别表示函数参数。如 @.x+@1.y 表示 (args) -> args[0].x + args[1].y
        assertEquals(
            new BinaryNode(
                "+",
                new FunctionCallNode(
                    new LambdaNode(new BinaryNode(
                        ">",
                        new MemberNode(
                            new IdentifierNode("@").argIndex(0),
                            new LiteralNode("x")
                        ),
                        new LiteralNode(1L)
                    )),
                    new LinkedList<AstNode>() {{
                        add(new IdentifierNode("arr"));
                    }}
                ).isTransform(true),
                new LiteralNode(1L)
            ),
            this.parse("arr|(@.x>1)+1")
        );
        assertEquals("arr|(@.x > 1)() + 1", this.transform("arr|(@.x>1)+1"));

        assertEquals(
            new FunctionCallNode(
                new IdentifierNode("filter"),
                new LinkedList<AstNode>() {{
                    add(new IdentifierNode("arr"));
                    add(new LambdaNode(new ConditionNode(
                        new BinaryNode(
                            ">",
                            new MemberNode(
                                new IdentifierNode("@1").argIndex(1),
                                new LiteralNode("x")
                            ),
                            new LiteralNode(1L)
                        ),
                        new LiteralNode(2L),
                        new LiteralNode(3L)
                    )));
                }}
            ).isTransform(true),
            this.parse("arr|filter((@1.x>1)?2:3)")
        );
        assertEquals("arr|filter(@1.x > 1 ? 2 : 3)", this.transform("arr|filter((@1.x>1)?2:3)"));

        assertEquals(
            new DefNode(
                new LinkedList<DefNode.Def>() {{
                    add(new DefNode.Def("f", new LambdaNode(new BinaryNode(
                        "+",
                        new IdentifierNode("@").argIndex(0),
                        new LiteralNode(1L)
                    ))));
                }},
                new FunctionCallNode(
                    new IdentifierNode("f"),
                    new ArrayList<AstNode>() {{
                        add(new LiteralNode(2L));
                    }}
                )
            ),
            this.parse("def f = @ + 1; f(2)")
        );
        assertEquals("def f = @ + 1; f(2)", this.transform("def f = @ + 1; f(2)"));
    }

    @Test
    void whitespaces() {
        assertEquals(
            new BinaryNode(
                "+",
                new LiteralNode(2L),
                new LiteralNode(3L)
            ),
            this.parse("\t2\r\n+\n\r3\n\n")
        );
        assertEquals("2 + 3", this.transform("\t2\r\n+\n\r3\n\n"));
    }

    @Test
    void throwErrors() {
        Exception exception1 = assertThrows(ParseException.class, () -> {
            this.parse("a.b ~+= c.d");
        });
        assertEquals("Invalid expression token: ~", exception1.getMessage());

        Exception exception2 = assertThrows(ParseException.class, () -> {
            this.parse("a.b =+= c.d");
        });
        assertEquals("Token = unexpected in expression: a.b =", exception2.getMessage());

        Exception exception3 = assertThrows(ParseException.class, () -> {
            this.parse("a.b == c.d +");
        });
        assertEquals("Unexpected end of expression: a.b == c.d +", exception3.getMessage());

        Grammar grammar = new Grammar();
        Tokenizer tokenizer = new Tokenizer(grammar);
        States states = new States();
        Parser parser = new Parser(grammar, states);
        List<Token> tokens = tokenizer.tokenize("a.b == c.d");
        parser.addTokens(tokens);
        parser.complete();
        Exception exception4 = assertThrows(ParseException.class, () -> {
            parser.addTokens(tokens);
        });
        assertEquals("Cannot add a new token to a completed Parser", exception4.getMessage());
    }
}
