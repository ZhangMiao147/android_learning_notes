# Java 基础的常见问题 4

# 1. 为什么 Java 枚举文字不能具有泛型类型参数

[enum泛型 - 为什么Java枚举文字不能具有泛型类型参数?](https://code-examples.net/zh-CN/q/41793e)

由于类型擦除。

这两种方法都不可能，因为参数类型被擦除。

```java
public <T> T getValue(MyEnum<T> param);
public T convert(Object);
```

## 1. 文件 IO 操作



## 2. AtomInteger

AtomInteger