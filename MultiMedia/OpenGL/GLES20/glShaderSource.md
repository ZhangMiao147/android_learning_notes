## glShaderSource

### 功能

　　替换着色器对象中的源代码。

### 方法

```java
// C function void glShaderSource ( GLuint shader, GLsizei count, const GLchar ** string, const GLint* length )
public static native void glShaderSource(int shader,String string);
```

##### 参数

shader：要被替换源代码的着色器对象的句柄（ID）。

（count：指定字符串和长度数组中的元素数。）

string：指定指向包含要加载到着色器的源代码的字符串（的指针数组）。

（length：指定字符串长度的数组。）

### 描述

　　对于支持着色器编译器的实现，glShaderSource 将着色器中的源代码设置为 string 指定的字符串数组中的源代码。先前存储在着色器对象中的任何源代码都将被完全替换。数组中的字符串数由 count 指定。如果 length 为 NULL，则认为每个字符串都以 null 结尾。如果 length 不是 NULL，则它指向包含字符串的每个对应元素的字符串长度的数组。length 数组中的每个元素可以包含相应字符串的长度（空字符串不计为字符串长度的一部分）或小于 0 的值以表示该字符串为空终止。此时不扫描或解析源代码字符串，它们知识复制到指定的着色器对象中。

### 注意

　　着色器编译器支持是可选的，因此必须在使用之前通过参数 GL_SHADER_COMPILER 调用 **glGet** 来查询。**glShaderSource**、**glCompileShader**、**glGetShaderPrecisionFormat **和 **glReleaseShaderCompiler** 等在不支持着色器编译器的实现上都将产生 GL_INVALID_OPERATION 错误。这样的实现反而提供了 **glShaderBinary** 替代方案，用于提供预编译的着色器二进制文件。

　　调用 glShaderSource 时，OpenGL 会复制着色器源代码字符串，因此应用程序可以在函数返回后立即释放源代码字符串的副本。

### 错误

　　如果不支持着色器编译器，则产生 **GL_INVALID_OPERATION**。

　　如果 shader 不是 OpenGL 生成的值，则产生 **GL_INVALID_VALUE**。

　　如果 shader 不是着色器对象，则产生 GL_INVALID_OPERATION 错误。

　　如果 count 比 0 小，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glShaderSource](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glShaderSource.xml)