## glGetAttribLocation

### 功能

　　返回属性变量的位置。

### 方法

```java
public static native int glGetAttribLocation(int program,String name);
```

##### 参数

program：指定要查询的程序对象。

name：指向以空结尾的字符串，该字符串包括要查询其位置的属性变量的名称。

### 描述

　　glGetAttribLocation 查询 name 指定的属性变量并 program 指定的程序对象的先前链接，并返回绑定到该属性变量的通用顶点属性的索引。如果 name 是矩阵属性变量，则返回矩阵的第一列的索引。如果 name 属性变量不是指定程序对象中的活动属性，或者名称以保留前缀 “ gl_ ” 开头，则返回 -1。

　　可以通过调用 **glBindAttribLocation** 随时指定属性变量名和通用属性索引之间的关联。在调用 **glLinkProgram** 之前，属性绑定不会生效。成功链接程序对象后，属性变量的索引值将保持固定，直到下一个链接命令出现。如果链接成功，则只能在链接之后查询属性值。glGetAttrilLocaion 返回上次为指定程序对象调用 **glLinkProgram** 时实际生效的绑定。 glGetAttribLocation 不返回自上次链接操作以来指定的属性绑定。

### 错误

　　如果 program 不是 OpenGL 生成的值，则产生 GL_INVALID_OPERATION 错误。

　　如果 program 不是程序对象，则产生 GL_INVALID_OPERATION 错误。

　　如果 program 没有成功链接，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glGetAttribLocation](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGetAttribLocation.xml)