# One Line Expression Evaluator (olee)

简单的表达式解析

> olee-java is a fork of [Jexl](https://github.com/TomFrost/Jexl).

## 快速开始

```
import com.meituan.olee.OneLineExpressionEvaluator;

OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator();

expression.evaluate('1+x', new HashMap<String, Number>() {{
  put("x", 2);
}});
// => 3

Expression<?> exp = evaluator.compile('1+x');
exp.evalute(new HashMap<String, Number>() {{
  put("x", 2);
}});
// => 3
```

## 安装

TODO

## 常量表达式

支持 `string` `number` `boolean` `null` 常量。

- **不支持**，科学计数法数字。
- **不支持**，十六进制。

## 数组

使用 `[]` 可以定义数组。如 `[1,2,3,4]` 计算结果为 `[1,2,3,4]`。

## 对象

使用 `{key: value}` 可以定义对象。如 `{name:'Nikola',dob:'10-July-1856'}`。

## 属性访问

`.` `[]`。 支持形如 `a.b.c` `a['b'].c` `a.b[0][1]` 等等。

- `a[-1]` 表示 `a[a.length - 1]`。

默认的属性访问仅支持 `Map` `List` `String`。如

`{x:1}.x` => `1`, `[1,2,3][0]` => `1`, `[1,2,3][-1]` => `3`, `"abc"[-1]` => `"c"`。

### 自定义属性访问

若需要支持其他类型实例，需要进行设置，如：

```
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
```

也可以借助反射，或者 `commons-beanutils` 的 `PropertyUtils.getProperty`

## 可选链

`?.` `?.[]` `?.()` 可以在访问变量时，避免 `NullPointerException`。效果等同于 js 的语法。

## 操作符

### 一元操作符

| 操作符 | 符号  |
|-----|:---:|
| 取反  | `!` |
| `+` | `+` |
| `-` | `-` |

### 二元操作符

| 操作符     |      符号      |
|---------|:------------:|
| 加，拼接字符串 |     `+`      |
| 减       |     `-`      |
| 乘       |     `*`      |
| 除       |     `/`      |
| 整除      |     `//`     |
| 取模      |     `%`      |
| 指数      |     `^`      |
| 逻辑与     |     `&&`     |
| 逻辑或     | &#124;&#124; |

### 比较

| 操作符        |  符号  |
|------------|:----:|
| 相等         | `==` |
| 不等         | `!=` |
| 大于         | `>`  |
| 大于等于       | `>=` |
| 小于         | `<`  |
| 小于等于       | `<=` |
| 判断元组是否在数组中 | `in` |

### 三元表达式

如 `a ? b : c`。同时 `a ?: b` 相当于 js 的 `a || b`。

### 新增、删除二元操作符、一元操作符

- 新增二元操作符

```
evaluator.addBinaryOp("_=", new BinaryOpGrammar(20) {
    @Override
    public Object apply(Object left, Object right) throws EvaluateException {
        if (left == null) return right == null;
        return ((String) left).toLowerCase().equals(((String) right).toLowerCase());
    }
});

evaluator.evaluate("'FOO' _= 'foo'", null);
// => true
```

- 新增一元操作符

```
evaluator.addUnaryOp("~", new UnaryOpGrammar(1000) {
    @Override
    public Object apply(Object right) throws EvaluateException {
        return (long) Math.floor(((Number) right).doubleValue());
    }
});

evaluator.evaluate("~5.7+5", null);
// -> 10
```

- 删除二元操作符

```
evaluator.removeBinaryOp("+");
evaluator.evaluate("1+2", null); // => throws
```

### 操作符优先级

| 优先级 |                    符号                     | 操作符        |
|:---:|:-----------------------------------------:|------------|
| 10  |               &#124;&#124;                | 逻辑或        |
| 20  |                   `&&`                    | 逻辑与        |
| 30  |                `==`  `!=`                 | 相等         |
| 40  |          `<=` `<` `>=` `>` `in`           | 比较         |
| 50  |                  `+` `-`                  | 加、减、拼接     |
| 60  |             `*` `/` `//` `%`              | 乘、除、整除、取余数 |
| 70  |                    `^`                    | 指数         |
| 90  |                `!` `+` `-`                | 一元操作符      |
| 100 | `.` `?.` `[]` `.?[]` `.![]` `.^[]` `.$[]` | 成员访问、集合操作符 |

## 方法调用

支持方法调用，如：

```
Map<String, Object> variables = new HashMap<String, Object>() {{
    put("foo", 10);
    put("double", (Function<List<?>, Number>) ((args) -> ((Number) args.get(0)).longValue() * 2));
}};

evaluator.evaluate("double(foo)+3", variables);
// => 23
```

- 需要注意，方法需要定义在`变量`中。

## 方法调用语法糖：管道

`|` 可以简化方法调用。

`fun(arg1, arg2, arg3)` 可以写成 `arg1|fun(arg2, arg3)` 的形式。

`fun(arg1)` 可以写成 `arg1|fun()` 的形式，也可以省略括号 `arg1|fun`。

比如：`baz(bar(foo,1))` 可以简化为 `foo|bar(1)|baz`，更容易理解。

需要注意，如果是 `foo.bar(baz)`，转为管道形式应为 `baz|(foo.bar)`，不能是 `baz|foo.bar`。因为后者等同于 `(baz|foo).bar`
，不符合预期。

## Lambda

用于定义函数，`@`表示函数参数，`@n` 表示第 `n` 个参数。如

`@ + @1.x` 表示 `(args) => args[0] + args[1].x`。

示例：

```
OneLineExpressionEvaluator evaluator = new OneLineExpressionEvaluator();
evaluator.addTransform(
    "filter",
    (args) -> ((List<?>) args.get(0)).stream()
        .filter((item) -> ((Function<List<?>, Boolean>) args.get(1)).apply(Collections.singletonList(item)))
        .collect(Collectors.toList())
);
evaluator.addTransform(
    "map",
    (args) -> ((List<?>) args.get(0)).stream()
        .map((item) -> ((Function<List<?>, Object>) args.get(1)).apply(Collections.singletonList(item)))
        .collect(Collectors.toList())
);
Map<String, Object> variables = new HashMap<String, Object>() {{
    put("bar", new ArrayList<Object>() {{
        add(Collections.singletonMap("tek", "hello"));
        add(Collections.singletonMap("tek", "baz"));
        add(Collections.singletonMap("tek", "baz"));
        add(Collections.singletonMap("tok", "baz"));
    }});
}};
// => { bar: [{tek: "hello"}, {tek: "baz"}, {tek: "baz"}, {tok: 'baz'}]}

assertEquals(
    new ArrayList<Object>() {{
        add(Collections.singletonMap("tek", "baz"));
        add(Collections.singletonMap("tek", "baz"));
    }},
    evaluator.evaluate("bar|filter(@.tek == 'baz')", variables)
);
// => [{tek: "baz"}, {tek: "baz"}]
assertEquals(
    new ArrayList<Object>() {{
        add("1hello");
        add("1baz");
        add("1baz");
        add("1baz");
    }},
    evaluator.evaluate("bar|map('1'+(@.tek||@.tok))", variables)
);
// => ["1hello", "1baz", "1baz", "1baz"]

evaluator.evaluate("bar|filter(@.tek != null)|map(@.tek)", variables);
// => ["hello", "baz", "baz"]
```

可以看出这个示例中最后一个表达式 `"bar|filter(@.tek != null)|map(@.tek)"` 非常精简。

## 定义表达式内变量

使用形如 `def variableName = expression; returnExpression` 的形式使用表达式内变量。

如：`def a=1; def b=2;a+b` => `3`，`def a=1; def b=a+1; def b=2; a+2` => `4`

## 注意

需要注意，表达式不会修改传入的变量，也不存在赋值操作。
