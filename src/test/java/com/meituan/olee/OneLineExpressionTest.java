package com.meituan.olee;

import java.util.HashMap;
import java.util.Map;

import com.meituan.olee.evaluator.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.meituan.olee.evaluator.DefaultPropertyAccessor;
import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.exceptions.ParseException;
import com.meituan.olee.grammar.BinaryOpGrammar;
import com.meituan.olee.grammar.UnaryOpGrammar;

class OneLineExpressionTest {
    OneLineExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        this.evaluator = new OneLineExpressionEvaluator();
    }

    @Test
    void evaluate() {
        assertEquals(
            4L,
            (long) this.evaluator.evaluate("2+2", null)
        );
        assertNull(
            this.evaluator.evaluate("", null)
        );
        Exception exception = assertThrows(ParseException.class, () -> {
            this.evaluator.evaluate("2**2", null);
        });
        assertEquals("Token * unexpected in expression: 2**", exception.getMessage());
    }

    @Test
    void addBinaryOp() {
        // 新增二元操作符
        this.evaluator.addBinaryOp("_=", new BinaryOpGrammar(20) {
            @Override
            public Object apply(Object left, Object right) throws EvaluateException {
                if (left == null) return right == null;
                return ((String) left).toLowerCase().equals(((String) right).toLowerCase());
            }
        });
        this.evaluator.addBinaryOp("**", new BinaryOpGrammar(10) {
            @Override
            public Object apply(Object left, Object right) throws EvaluateException {
                return (((Number) left).longValue() + ((Number) right).longValue()) * 2;
            }
        });
        this.evaluator.addBinaryOp("***", new BinaryOpGrammar(100) {
            @Override
            public Object apply(Object left, Object right) throws EvaluateException {
                return (((Number) left).longValue() + ((Number) right).longValue()) * 2;
            }
        });
        assertEquals(true, this.evaluator.evaluate("'FOO' _= 'foo'", null));
        // **  优先级低 1 + 2 ** 3 + 4 ~> (1 + 2) ** (3 + 4)
        assertEquals(20L, (long) this.evaluator.evaluate("1 + 2 ** 3 + 4", null));
        // *** 优先级高 1 + 2 *** 3 + 4 ~> 1 + (2 *** 3) + 4
        assertEquals(15L, (long) this.evaluator.evaluate("1 + 2 *** 3 + 4", null));
    }

    @Test
    void addUnaryOp() {
        // 新增一元操作符
        this.evaluator.addUnaryOp("~", new UnaryOpGrammar(1000) {
            @Override
            public Object apply(Object right) throws EvaluateException {
                return (long) Math.floor(((Number) right).doubleValue());
            }
        });
        assertEquals(10L, (long) this.evaluator.evaluate("~5.7+5", null));
    }

    @Test
    void removeBinaryOp() {
        // 删除二元操作符
        this.evaluator.removeBinaryOp("+");
        this.evaluator.removeBinaryOp("*");
        Exception exception1 = assertThrows(ParseException.class, () -> {
            this.evaluator.evaluate("1+2", null);
        });
        Exception exception2 = assertThrows(ParseException.class, () -> {
            this.evaluator.evaluate("1*2", null);
        });
        assertEquals("Token + unexpected in expression: 1+", exception1.getMessage());
        assertEquals("Invalid expression token: *", exception2.getMessage());
    }

    @Test
    void removeUnaryOp() {
        // 删除一元操作符
        this.evaluator.removeUnaryOp("+");
        this.evaluator.removeUnaryOp("!");
        Exception exception1 = assertThrows(ParseException.class, () -> {
            this.evaluator.evaluate("+2", null);
        });
        Exception exception2 = assertThrows(ParseException.class, () -> {
            this.evaluator.evaluate("!2", null);
        });
        assertEquals("Token + unexpected in expression: +", exception1.getMessage());
        assertEquals("Invalid expression token: !", exception2.getMessage());
    }

    @Test
    void compile() {
        // 可以先解析，然后用于多次求值
        Expression<?> expression = this.evaluator.compile("{x:y,y:x}");
        assertEquals(
            new HashMap<String, Number>() {{
                put("x", 2);
                put("y", 1);
            }},
            expression.evaluate(new HashMap<String, Number>() {{
                put("x", 1);
                put("y", 2);
            }})
        );
        assertEquals(
            new HashMap<String, Number>() {{
                put("x", 3);
                put("y", 4);
            }},
            expression.evaluate(new HashMap<String, Number>() {{
                put("x", 4);
                put("y", 3);
            }})
        );
    }

    @Test
    void customAccessor() {
        // 可以自定义属性访问。
        // 形如 x.y 的形式，会被解析为 propertyAccessor(x, "y");
        // 形如 x[y] 的形式，会被解析为 propertyAccessor(x, y)，此时的 y 不一定是字符串。
        class User {
            private final String name;
            private final int age;

            public User(String name, int age) {
                this.name = name;
                this.age = age;
            }

            public String getName() {
                return name;
            }

            public int getAge() {
                return age;
            }
        }
        OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator(new DefaultPropertyAccessor() {
            @Override
            public Object get(Object target, Object key) throws EvaluateException {
                if (target instanceof User) {
                    if ("name".equals(key)) {
                        return ((User) target).getName();
                    }
                    if ("age".equals(key)) {
                        return ((User) target).getAge();
                    }
                }
                return super.get(target, key);
            }
        });
        User variables1 = new User("张三", 18);
        Map<String, Object> variables2 = new HashMap<String, Object>() {{
            put("user", variables1);
        }};

        assertEquals("张三", evaluator.evaluate("name", variables1));
        assertEquals("张三", evaluator.evaluate("user.name", variables2));
        assertEquals(18, (int) evaluator.evaluate("age", variables1));
        assertEquals(18, (int) evaluator.evaluate("user.age", variables2));
    }
}
