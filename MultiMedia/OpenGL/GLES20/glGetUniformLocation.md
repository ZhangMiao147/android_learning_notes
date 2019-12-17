## glGetUniformLocation

### 功能

　　返回统一变量的位置。

### 方法

```java
public static native int glGetUniformLocation(int program,String name);
```

##### 参数

program：指定要查询的程序对象。

name：指向以空结尾的字符串，该自渡川包含要查询其位置的统一变量的名称。

### 描述

　　glGetUniformLocation 返回一个整数，表示程序对象中特定统一变量的位置。name 必须是不包含空格的以空结尾的字符串。name 必须是程序中的活动统一变量名，它不能是结构，也不能是结构数组或向量或矩阵的子组件。如果 name 与程序中的活动统一变量不对应，或者 name 以保留前缀 “gl_” 开头，则此函数返回 -1。

　　可以通过为结构中的每个字段调用 glGetUniformLocation 来查询作为结构或结构数组的统一变量。数组元素运算符 “[]” 和结构字段运算符 “.” 可以在 name 中使用，以便选择数组中的元素或结构中的字段。使用这些运算符的结构不允许是其他结构、结构数组或向量或矩阵的子组件。除非 name 的最后一部分表示统一变量数组，否则可以使用数组的名称或使用 “[0]” 附加的名称来检索数组的第一个元素的位置。

　　在程序对象成功链接之前，分配给统一变量的实际位置是不知道的。发生链接后，命令 glGetUniformLocation 可用于获取统一变量的位置。然后可以将此位置值传递给 **glUniform** 以设置统一变量的值或传递给 **glGetUniform** 以查询统一变量的当前值。成功链接程序对象后，统一变量的索引值保持不变，直到发生下一个链接命令。如果链接成功，则只能在链接后查询统一变量位置和值。

### 错误

　　如果 program 不是 OpenGL 生成的值，则产生 GL_INVALID_VALUE 错误。

　　如果 program 不是程序对象，则产生 GL_INVALID_OPERATION 错误。

　　如果 program 没有成功链接，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glGetUniformLocation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGetUniformLocation.xml)