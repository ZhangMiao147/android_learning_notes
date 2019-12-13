## glCreateShader

### 功能

　　创建着色器对象。

### 方法

```java
public static native int glCreateShader(int type);
```

##### 参数

　　shaderType：指定要创建的着色器的类型。只能是 GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER。

### 描述

　　glCreateShader 创建一个空的着色器对象，并返回一个可以引用的非零值（shader ID）。着色器对象用于维护定义着色器的源代码字符串。shaderType 指示要创建的着色器的类型。支持两种类型的着色器。GL_VERTEXT_SHADER 类型的着色器是一个用于在可编程顶点处理器上运行的着色器。GL_FRAGMENT_SHADER 类型的着色器是一个旨在在可编程片段处理器上运行的着色其。

　　创建时，着色器对象的 GL_SHADER_TYPE 参数设置为 GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER ，具体取决于 shaderType 的值。

### 注意

　　与纹理对象一样，只要上下文的服务端共享相同的地址空间，着色器对象的名称空间就可以在一组上下文中共享。如果名称空间跨上下文共享，则所有附加对象和与这些附加对象关联的数据也会共享。

　　当从不同的执行线程访问对象时，应用程序需要负责跨 API 调用提供同步。

### 错误

　　如果创建着色器对象时发生错误，则此函数返回 0。

　　如果 shaderType 不是一个可接受的值时，则产生 GL_INVALID_ENUM 错误。

### 原文地址

[glCreateShader](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCreateShader.xml)