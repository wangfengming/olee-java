package com.meituan.olee;

import com.meituan.olee.exceptions.EvaluateException;
import com.meituan.olee.grammar.Callback;
import com.meituan.olee.util.OperatorUtils;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EvaluateTest {

    OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator();

    @Test
    void grammars() throws ExecutionException {
        Map<?, ?> variables = Collections.singletonMap(
            "x", Collections.singletonMap("y", "1")
        );
        // +
        assertEquals(1L, (long) this.evaluator.evaluate("+1", null));
        assertEquals(3L, (long) this.evaluator.evaluate("1+2", null));
        assertEquals(2L, (long) this.evaluator.evaluate("null+2", null));
        assertEquals(1L, (long) this.evaluator.evaluate("+x.y", variables));
        // -
        assertEquals(-1L, (long) this.evaluator.evaluate("-1", null));
        assertEquals(1L, (long) this.evaluator.evaluate("2-1", null));
        assertEquals(-1L, (long) this.evaluator.evaluate("-x.y", variables));
        assertEquals(
            this.evaluator.evaluate("[1,2,3,4]", null),
            this.evaluator.evaluate("[1,2]+[3,4]", null)
        );
        assertEquals(
            this.evaluator.evaluate("{a:1,b:2,c:3,d:4}", null),
            this.evaluator.evaluate("{a:1,b:2}+{c:3,d:4}", null)
        );
        assertEquals(
            this.evaluator.evaluate("[1,2]", null),
            this.evaluator.evaluate("[1,2]+null", null)
        );
        assertEquals(
            this.evaluator.evaluate("{a:1,b:2}", null),
            this.evaluator.evaluate("{a:1,b:2}+null", null)
        );
        // %
        assertEquals(1L, (long) this.evaluator.evaluate("4%3", null));
        // ^
        assertEquals(64L, (long) this.evaluator.evaluate("4^3", null));
        // ==
        assertEquals(false, this.evaluator.evaluate("4==3", null));
        assertEquals(true, this.evaluator.evaluate("4==4", null));
        assertEquals(false, this.evaluator.evaluate("4=='4'", null));
        assertEquals(false, this.evaluator.evaluate("x==y", variables));
        assertEquals(true, this.evaluator.evaluate("z==y", variables));
        // !=
        assertEquals(true, this.evaluator.evaluate("4!=3", null));
        assertEquals(false, this.evaluator.evaluate("4!=4", null));
        assertEquals(true, this.evaluator.evaluate("4!='4'", null));
        assertEquals(true, this.evaluator.evaluate("x!=y", variables));
        assertEquals(false, this.evaluator.evaluate("z!=y", variables));
        // < > <= >=
        assertEquals(false, this.evaluator.evaluate("4<3.0", null));
        assertEquals(true, this.evaluator.evaluate("4>3.0", null));
        assertEquals(false, this.evaluator.evaluate("4<=3.0", null));
        assertEquals(true, this.evaluator.evaluate("4>=3.0", null));
        assertEquals(true, this.evaluator.evaluate("4<=4.0", null));
        assertEquals(true, this.evaluator.evaluate("4>=4.0", null));
        assertEquals(true, this.evaluator.evaluate("'abc'>'abb'", null));
        // in
        assertEquals(true, this.evaluator.evaluate("4 in [1,2,3,4]", null));
        assertEquals(false, this.evaluator.evaluate("5 in [1,2,3,4]", null));
        // !
        assertEquals(false, this.evaluator.evaluate("!4", null));
        assertEquals(true, this.evaluator.evaluate("!0", null));
        assertEquals(true, this.evaluator.evaluate("!''", null));
        assertEquals(true, this.evaluator.evaluate("!z", variables));
        // ||
        assertEquals(true, this.evaluator.evaluate("true || false", null));
    }

    @Test
    void literal() throws ExecutionException {
        assertEquals(1L, (long) this.evaluator.evaluate("1", null));
        assertEquals(1D, this.evaluator.evaluate("1.0", null));
        assertEquals(true, this.evaluator.evaluate("true", null));
        assertEquals(false, this.evaluator.evaluate("false", null));
        assertNull(this.evaluator.evaluate("null", null));
        assertEquals("i'm ok", this.evaluator.evaluate("'i''m ok'", null));
    }

    @Test
    void unary() throws ExecutionException {
        Map<?, ?> variables = Collections.singletonMap(
            "x", Collections.singletonMap("y", "1")
        );
        assertEquals(1L, (long) this.evaluator.evaluate("+x.y", variables));
        assertEquals(-1L, (long) this.evaluator.evaluate("-x.y", variables));
        assertEquals(true, this.evaluator.evaluate("!!x.y", variables));
    }


    @Test
    void binary() throws ExecutionException {
        assertEquals(20L, (long) this.evaluator.evaluate("(2 + 3) * 4", null));
        assertEquals(512L, (long) this.evaluator.evaluate("2 ^ 3 ^ 2", null));
        assertEquals(64L, (long) this.evaluator.evaluate("(2 ^ 3) ^ 2", null));
        assertEquals(23L, (long) this.evaluator.evaluate("3 + 4 * 5", null));
        assertEquals(35L, (long) this.evaluator.evaluate("(3 + 4) * 5", null));
        assertEquals(36L, (long) this.evaluator.evaluate("4 * 3 ^ 2", null));
        assertEquals(4D / 3D / 2D, (double) this.evaluator.evaluate("4 / 3 / 2", null));
        assertEquals(3D, (long) this.evaluator.evaluate("7 // 2", null));
        assertEquals("Hello8World'", this.evaluator.evaluate("'Hello'+(4+4)+'World'''", null));
        assertEquals(true, this.evaluator.evaluate("'foo' && 6 >= 6 && 0 + 1 && true", null));
        assertEquals(1L, (long) this.evaluator.evaluate("'foo' && 6 >= 6 && 0 + 1", null));
        assertEquals(true, this.evaluator.evaluate("'bar' in 'foobarbaz'", null));
        assertEquals(true, this.evaluator.evaluate("'bar' in ['foo','bar','baz']", null));
    }

    @Test
    void ternary() throws ExecutionException {
        assertEquals(1L, (long) this.evaluator.evaluate("'foo' ? 1 : 2", null));
        assertEquals(2L, (long) this.evaluator.evaluate("'' ? 1 : 2", null));
        assertEquals(2L, (long) this.evaluator.evaluate("'foo' ? 0 ? 1 : 2 : 3", null));
        assertEquals("foo", this.evaluator.evaluate("'foo' ?: 'bar'", null));
    }

    @Test
    void member() throws ExecutionException {
        Map<String, Object> variables1 = Collections.singletonMap(
            "foo",
            Collections.singletonMap(
                "bar",
                Collections.singletonMap("baz", "tek")
            )
        );
        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("foo", new HashMap<String, Object>() {{
            put("bar", new ArrayList<Object>() {{
                add(Collections.singletonMap("tek", "tok"));
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tek", "foz"));
            }});
        }});
        variables2.put("a", new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }});

        assertEquals("tek", this.evaluator.evaluate("foo.bar.baz", variables1));
        assertEquals("baz", this.evaluator.evaluate("foo.bar[1].tek", variables2));
        assertEquals(1, (int) this.evaluator.evaluate("a[0]", variables2));
        assertEquals(3, (int) this.evaluator.evaluate("a[-1]", variables2));
        assertEquals(3, (int) this.evaluator.evaluate("a[1+1]", variables2));
        assertEquals("tek", this.evaluator.evaluate("foo['ba' + 'r'].baz", variables1));
        assertEquals("c", this.evaluator.evaluate("'abc'[-1]", null));
    }

    @Test
    void optional() throws ExecutionException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("val", Collections.emptyMap());
        variables.put("fun", (Callback) (args) -> null);

        assertNull(this.evaluator.evaluate("foo?.bar.baz", variables));
        Exception exception1 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("val?.bar.baz", variables);
        });
        assertEquals("Cannot read properties of null (reading baz)", exception1.getMessage());

        assertNull(this.evaluator.evaluate("foo?.['bar'].baz", variables));
        Exception exception2 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("val?.['bar'].baz", variables);
        });
        assertEquals("Cannot read properties of null (reading baz)", exception2.getMessage());

        assertNull(this.evaluator.evaluate("foo?.().baz", variables));
        Exception exception3 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("fun?.().baz", variables);
        });
        assertEquals("Cannot read properties of null (reading baz)", exception3.getMessage());

        assertNull(this.evaluator.evaluate("foo?.bar()", variables));
        Exception exception4 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("val?.bar()", variables);
        });
        assertEquals("null is not a function", exception4.getMessage());
    }

    @Test
    void object() throws ExecutionException {
        assertEquals(
            new HashMap<String, Object>() {{
                put("foo", new HashMap<String, Object>() {{
                    put("bar", "tek");
                }});
            }},
            this.evaluator.evaluate("{foo: {bar: 'tek'}}", null)
        );

        assertEquals(
            new HashMap<String, Object>(),
            this.evaluator.evaluate("{}", null)
        );

        assertEquals(
            new HashMap<String, Object>() {{
                put("a1", 1L);
            }},
            this.evaluator.evaluate("{['a'+1]:1}", null)
        );

        assertEquals(
            "bar",
            this.evaluator.evaluate("{foo: 'bar'}.foo", null)
        );
        assertNull(
            this.evaluator.evaluate("{foo: 'bar'}.baz", null)
        );
    }

    @Test
    void array() throws ExecutionException {
        assertEquals(
            new ArrayList<Object>() {{
                add("foo");
                add(3L);
            }},
            this.evaluator.evaluate("['foo', 1+2]", null)
        );
        assertEquals(
            3L,
            (long) this.evaluator.evaluate("['foo', 1+2][-1]", null)
        );
        assertEquals(
            new ArrayList<Object>(),
            this.evaluator.evaluate("[]", null)
        );
    }

    @Test
    void def() throws ExecutionException {
        assertEquals(
            3L,
            (long) this.evaluator.evaluate("def a=1; def b=2;a+b", null)
        );
        assertEquals(
            6L,
            (long) this.evaluator.evaluate("def a=1; def b=a+1;def c=a+b;a+b+c", null)
        );
        assertEquals(
            4L,
            (long) this.evaluator.evaluate("def a=1; def a=a+1;def b=2;a+b", null)
        );
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("x", true);
            put("a", 1);
            put("b", 2);
        }};
        assertEquals(
            2L,
            (long) this.evaluator.evaluate("def a = 2; a", variables)
        );
        assertEquals(
            11L,
            (long) this.evaluator.evaluate("(x ? (def a = 10; a) : b) + a", variables)
        );
    }

    @Test
    void transform() throws ExecutionException {
        OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator();
        evaluator.addTransform(
            "half",
            (args) -> ((Number) args[0]).doubleValue() / 2
        );
        evaluator.addTransform(
            "filter",
            (args) -> ((List<?>) args[0]).stream()
                .filter((item) -> !OperatorUtils.isFalsy(((Callback) args[1]).apply(item)))
                .collect(Collectors.toList())
        );
        evaluator.addTransform(
            "map",
            (args) -> ((List<?>) args[0]).stream()
                .map((item) -> ((Callback) args[1]).apply(item))
                .collect(Collectors.toList())
        );
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("foo", 10);
            put("bar", new ArrayList<Object>() {{
                add(Collections.singletonMap("tek", "hello"));
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tok", "baz"));
            }});
            put("double", (Callback) ((args) -> ((Number) args[0]).longValue() * 2));
            put("fns", Collections.singletonMap(
                "half", (Callback) ((args) -> ((Number) args[0]).doubleValue() / 2)
            ));
        }};

        assertEquals(8D, evaluator.evaluate("foo|half+3", variables));
        assertEquals(23L, (long) evaluator.evaluate("foo|double+3", variables));
        assertEquals(8D, evaluator.evaluate("foo|(fns.half)+3", variables));
        assertEquals(8D, evaluator.evaluate("foo|(fns['half'])+3", variables));
        assertEquals(8D, evaluator.evaluate("foo|(fns['hal'+'f'])+3", variables));
        assertEquals(
            new ArrayList<Object>() {{
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tek", "baz"));
            }},
            evaluator.evaluate("bar|filter(@.tek == 'baz')", variables)
        );
        assertEquals(
            new ArrayList<Object>() {{
                add("1hello");
                add("1baz");
                add("1baz");
                add("1baz");
            }},
            evaluator.evaluate("bar|map('1'+(@.tek||@.tok))", variables)
        );
        assertEquals(
            new ArrayList<Object>() {{
                add("hello");
                add("baz");
                add("baz");
            }},
            evaluator.evaluate("bar|filter(@.tek)|map(@.tek)", variables)
        );

        Exception exception1 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("'hello'|world", variables);
        });
        assertEquals("null is not a function", exception1.getMessage());
    }

    @Test
    void functionCall() throws ExecutionException {
        OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator();
        evaluator.addTransform(
            "half",
            (args) -> ((Number) args[0]).doubleValue() / 2
        );
        evaluator.addTransform(
            "filter",
            (args) -> ((List<?>) args[0]).stream()
                .filter((item) -> !OperatorUtils.isFalsy(((Callback) args[1]).apply(item)))
                .collect(Collectors.toList())
        );
        evaluator.addTransform(
            "map",
            (args) -> ((List<?>) args[0]).stream()
                .map((item) -> ((Callback) args[1]).apply(item))
                .collect(Collectors.toList())
        );
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("foo", 10);
            put("bar", new ArrayList<Object>() {{
                add(Collections.singletonMap("tek", "hello"));
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tok", "baz"));
            }});
            put("double", (Callback) ((args) -> ((Number) args[0]).longValue() * 2));
            put("fns", Collections.singletonMap(
                "half", (Callback) ((args) -> ((Number) args[0]).doubleValue() / 2)
            ));
        }};

        assertEquals(8D, evaluator.evaluate("half(foo)+3", variables));
        assertEquals(23L, (long) evaluator.evaluate("double(foo)+3", variables));
        assertEquals(8D, evaluator.evaluate("fns.half(foo)+3", variables));
        assertEquals(8D, evaluator.evaluate("fns['half'](foo)+3", variables));
        assertEquals(8D, evaluator.evaluate("fns['hal'+'f'](foo)+3", variables));
        assertEquals(
            new ArrayList<Object>() {{
                add(Collections.singletonMap("tek", "baz"));
                add(Collections.singletonMap("tek", "baz"));
            }},
            evaluator.evaluate("filter(bar,@.tek == 'baz')", variables)
        );
        assertEquals(
            new ArrayList<Object>() {{
                add("1hello");
                add("1baz");
                add("1baz");
                add("1baz");
            }},
            evaluator.evaluate("map(bar,'1'+(@.tek||@.tok))", variables)
        );
        assertEquals(
            new ArrayList<Object>() {{
                add("hello");
                add("baz");
                add("baz");
            }},
            evaluator.evaluate("map(filter(bar,@.tek),@.tek)", variables)
        );

        Exception exception1 = assertThrows(EvaluateException.class, () -> {
            this.evaluator.evaluate("world('hello')", variables);
        });
        assertEquals("null is not a function", exception1.getMessage());
    }

    @Test
    void lambda() throws ExecutionException {
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("foo", 10);
        }};
        assertEquals(
            "large",
            this.evaluator.evaluate("foo|(@<10)?'small':'large'", variables)
        );
        assertEquals(
            new HashMap<String, Object>() {{
                put("x", 9D);
                put("y", 12D);
            }},
            this.evaluator.evaluate("(foo+3+5)|({x:@/2,y:@/2+3})", variables)
        );
        assertEquals(
            "small",
            this.evaluator.evaluate("def isLarge = @>@1;isLarge(1+2+3,4+5+6)?'great':'small'", variables)
        );
    }

    @Test
    void whitespace() throws ExecutionException {
        assertEquals(
            20L,
            (long) this.evaluator.evaluate("(\t2\n+\n3) *\n4\n\r\n", null)
        );
    }
}
