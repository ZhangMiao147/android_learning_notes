## glCreateProgram

### 功能

　　创建 program 对象。

### 方法

```java
public static native int glCreateProgram();
```

### 描述

　　glCreateProgram 创建一个空 program 并返回一个可以被引用的非零值（program ID）。program 对象是可以附加着色器对象的对象。这提供了一种机制来指定创建 program 链接的着色器对象。它还提供了一种检查将用于创建 program 的着色器兼容性的方法（例如，检查顶点着色器和片元着色器之间的兼容性）。当不再需要作为 program 对象的一部分时，着色器对象就可以被分离了。

　　通过调用 **glCompileShader** 成功编译着色器对象，并且通过调用 **glAttachShader** 成功地将着色器对象附加到 program 对象，并且通过调用 **glLinkProgram** 成功的链接 program 对象之后，可以在 program 对象中创建一个或多个可执行文件。当调用 **glUseProgram** 时，这些可执行文件成为当前状态的一部分。可以通过调用 **glDeleteProgram** 删除 program 对象。当 program 对象不再是任何上下文的当前呈现状态的一部分时，将删除与 program 对象关联的内存。

### 注意

　　与纹理对象一样，只要上下文的服务器端共享相同的地址空间，程序对象的名称空间就可以在一组上下文中共享。如果名称空间跨上下文共享，则也会共享所有附加对象和这些附加对象关联的数据。

　　当从不同的执行线程访问对象时，应用程序负责跨 API 调用提供同步。

### 错误

　　如果创建 program 对象时发生错误，则此函数返回 0。

### 原文地址

[glCreateProgram](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCreateProgram.xml)