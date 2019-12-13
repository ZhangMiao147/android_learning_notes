## glDeleteShader

### 功能

　　删除 shader (着色器)对象。 

### 方法

```java
public static native void glDeleteShader(int shader);
```

##### 参数

　　shader：指定要删除的着色器对象。

### 描述

　　glDeleteShader 释放内存并使 shader 指定的着色器对象关联的名称失效。这个命令有效地撤销了对 **glCreateShader** 的调用的影响。

　　如果删除的着色器对象附加到程序对象，它将被标记为删除，但它不会被删除，在它不再附加到任何程序对象之前，对于任何渲染上下文，它将不会被删除（即在删除它之前，它必须从它附加的任何位置分离）。shader 为 0 将被忽略。

　　要确定对象是否已标记为删除，请使用参数 shader 和 GL_DELETE_STATUS 调用 **glGetShaderiv**。

###  错误

　　如果 shader 不是 OpenGl 生成的值，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glDeleteShader](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDeleteShader.xml)