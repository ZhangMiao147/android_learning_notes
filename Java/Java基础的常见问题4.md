# Java 基础的常见问题 4

# 1. 为什么 Java 枚举文字不能具有泛型类型参数

[enum泛型 - 为什么Java枚举文字不能具有泛型类型参数?](https://code-examples.net/zh-CN/q/41793e)

由于类型擦除。

这两种方法都不可能，因为参数类型被擦除。

```java
public <T> T getValue(MyEnum<T> param);
public T convert(Object);
```

可以用以下代码解决

```java
public enum MyEnum {
    LITERAL1(String.class),
    LITERAL2(Integer.class),
    LITERAL3(Object.class);

    private Class<?> clazz;

    private MyEnum(Class<?> clazz) {
      this.clazz = clazz;
    }

    ...

}
```

# 2. arraylist,linkedlist,vector 效率

ArrayList 底层是数组结构，查询快，增删慢，线程不安全，效率高。

LinkedList 底层是链表数据结构，查询慢，增删快，线程不安全，效率高。

Vector 底层是数组结构，查询快，增删慢，线程安全，效率低。

# 3. 文件 IO 操作

# 4. AtomInteger

AtomInteger