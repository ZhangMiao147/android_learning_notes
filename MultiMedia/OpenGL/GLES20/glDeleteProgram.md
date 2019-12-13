## glDeleteProgram

### 功能

　　删除一个 program 对象。 

### 方法

```java
public static native void glDeleteProgram(int program);
```

##### 参数

　　program：指定要删除的 program 对象。

### 描述

　　glDeleteProgram 释放内存并使 program 指定的  program 对象的相关名称无效。这个命令有效地撤销了对 **glCreateProgram** 的调用的影响。

　　如果 program 对象正在被用作当前渲染状态的一部分，则它将被标记为删除，但在它不再是任何渲染上下文的当前状态的一部分之前不会被删除。如果要删除的程序对象附加了着色器对象，那么这些着色器对象将自动分离但不会被删除，除非它们先前调用 **glDeleteShader** 而被标记为删除。 program 的值为 0 将被忽略。

　　要确定对象是否已标记为删除，请使用参数 program 和 GL_DELETE_STATUS 调用 **glGetProgramiv**。

### 错误

　　如果 program 不是 OpenGL 生成的值，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glDeleteProgram](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDeleteProgram.xml)