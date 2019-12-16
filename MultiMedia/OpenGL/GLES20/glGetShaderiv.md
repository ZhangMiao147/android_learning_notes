## glGetShaderic

### 功能

　　从着色器对象返回参数。

### 方法

```java
public static native void glGetShaderiv(int shader,int pname,int[] params,int offset);

public static native void glGetShaderiv(int shader,int pname,java.nio.IntBuffer params);
```

##### 参数

shader：指定要查询的着色器对象。

pname：指定对象的参数，可接受的符号名称为 GL_SHADER_TYPE、GL_DELETE_STATUS、GL_COMPILE_STATUS、GL_INFO_LOG_LENGTH、GL_SHADER_SOURCE_LENGTH。

params：返回请求的对象参数。

### 描述

　　glGetShaderiv 以 params 形式返回特定着色器对象的参数值。定义了以下参数：

**GL_SHADER_TYPE**：如果着色器是顶点着色器，则 params 返回 GL_VERTEX_SHADER ；如果着色器是片段着色器对象，则返回 GL_FRAGMENT_SHADER。

**GL_DELETE_STATUS**：如果 shader 当前被标记为删除，则 params 返回 GL_TRUE，否则返回 GL_FALSE。

**GL_COMPILE_STATUS**：对于支持着色器编译器的实现，如果着色器上的最后一次编译操作成功，则 programs 返回 GL_TRUE，否则返回 GL_FALSE。

**GL_INFO_LOG_LENGTH**：对于支持着色器编译器的实现，params 返回着色器信息日志的字符数，包括空终止字符（即存储信息日志所需的字符缓冲区的大小）。如果着色器没有信息日志，则返回值 0 。

**GL_SHADER_SOURCE_LENGTH**：对于支持着色器编译器的实现，params 返回构成着色器的着色器源的源字符串的连接长度，包括空终止字符。（即存储着色器源所需的字符缓冲区的大小）。如果不存在源代码，则返回 0。

### 注意

　　着色器编译器支持是可选的，因此必须在使用之前通过使用参数 GL_SHADER_COMPILER 调用 **glGet** 来查询。**glShaderSource**、**glCompileShader**、**glGetShaderPrecisionFormat**、**glReleaseShaderCompiler** 等在不支持着色器编译器的实现上都将产生 GL_INVALID_OPERATION 错误，用 glGetShaderiv 去查询 GL_COMPILE_STATUS、GL_INFO_LOG_LENGTH 和 GL_SHADER_SOURCE_LENGTH 也是一样的。这样的实现反而提供了 **glShaderBinary** 替代方案，用于提供预编译的着色器二进制文件。

　　如果产生错误，则不会更改 params 的内容。

### 错误

　　如果 pname 不是一个可接受的值，则产生 GL_INVALID_ENUM 错误。

　　如果 shader 不是 OpenGL 生成的值，则产生 GL_INVALID_VALUE 错误。

　　如果在不支持着色器编译器的情况下查询 pname 为 GL_COMPILE_STATUS、GL_INFO_LOG_LENGTH 或 GL_SHADER_SOURCE_LENGTH，则产生 GL_INVALID_OPERATION 错误。

　　如果 shader 没有关联着色器对象，则生成 GL_INVALID_OPERATION。

### 原文地址

[glGetShaderiv](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGetShaderiv.xml)
