## glAttachShader

### 功能

　　将着色器对象附加到 program 对象。

### 方法

```
public static native void glAttachShader(int program,int shader);
```

##### 参数

** program： **指定着色器对象将附加到的 program 对象。

** shader： **指定要附加的着色器对象。

### 描述

　　为了创建一个可执行文件，必须要有一种方法来指定将被链接在一起的事物的列表。那么，program 对象就提供了这么一种机制。要在 program 对象中链接在一起的 shader 必须首先附加在该 program 对象上。glAttachShader 方法就是用于将指定的 shaders 附着到指定的 program 对象上。这表示 shader 将被包含在要被执行的 program 的链接操作中。

　　不管 shader 对象是否被附着到 program 对象上，在 shader 对象上执行的所有操作都是有效的。

　　在源代码加载到着色器对象之前或着色器对象被编译之前，将 shader 对象附着到 program 对象上都是被允许的。

　　多个同类型（例如都是 vertex shader 类型，或都是 fragment shader 类型）的 shader 对象不能被附着到同一个 program 对象上。但是，单个 shader 对象可以被附着到多个 program 对象上。

　　如果着色器对象在附加到程序对象时被删除，它将被标记为删除，并且在调用 **glDetachShader** 才能将其从它所连接的所有 program 对象中分离之前，不会进行删除。

### 错误

　　如果 program 或 shader 不是由 OpenGL 所生成的值，则出现 **GL_INVALID_VALUE** 错误。

　　如果 program 参数传的不是 program 对象，则出现 **GL_INVALID_OPERATION**。

　　如果 shader 参数传的不是 shader 对象，则出现 **GL_INVALID_OPERATION**。

　　如果 shader 已经被添加到 program 对象上，或者已经有一个同类型的 shader 对象添加到该 program 对象上的时，则出现 **GL_INVALID_OPERATION**。

### 原文地址

[glAttachShader](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glAttachShader.xml)