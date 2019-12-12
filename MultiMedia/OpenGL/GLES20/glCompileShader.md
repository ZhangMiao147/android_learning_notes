## glCompileShader

### 功能

　　编译着色器对象。

### 方法

```
public static native void glCompileShader(int shader);
```

##### 参数

　　shader：指定要编译的着色器对象。

### 描述

　　对于支持着色器编译器的实现，flCompileShader 编译已经存储在 shader 指定的着色器对象中的源代码字符串。

　　编译状态将存储为着色器对象的状态的一部分。如果着色器编译时没有错误并且可以使用，则此值将设置为 GL_TRUE，否则将设置为 GL_FALSE。可以通过使用参数 shader 和 GL_COMPULE_STATUS 调用 **glGetShaderiv** 来查询状态值。

　　由于 OpenGL ES 着色语言规范指定的多种原因，着色器的编译可能会失败。无论编译是否成功，都可以通过调用 **glGetShaderInfoLog** 从着色器对象的信息日志中获取有关编译的信息。

### 注意

　　着色器编译器支持是可选的，因此必须在使用之前通过使用参数 GL_SHADER_COMPILER 调用 **glGet** 来查询。glShaderSource、glCompileShader、glGetShaderPrecisionFormat、glReleaseShaderCompile 等在不支持着色器编译器的实现上都将生成 GL_INVALID_OPERATION。这样的实现提供了 glShaderBinary 替代方法，用于提供预编译的着色器二进制文件。

### 错误

　　如果不支持着色器编译器，则产生 GL_INVALID_OPERATION 错误。

　　如果 shader 不是 OpenGL 生成的值，则产生 GL_INVALID_VALUE 错误。

　　如果 shader 不是着色器对象，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glCompileShader](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCompileShader.xml)
