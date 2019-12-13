## glDeleteRenderbuffer

### 功能

　　删除命名的 renderbuffer 对象。 

### 方法

```java
public static native void glDeleteRenderbuffers(int n,int[] renderbuffers,int offset);

public static native void glDeleteRenderbuffers(int n,java.nio.IntBuffer renderbuffers);
```

##### 参数

　　n：指定要删除的 renderbuffer 对象的数量。

　　renderbuffers ：指定要删除的 renderbuffer 对象数组。

### 描述

　　glDeleteRenderbuffers 删除由数组 renderbuffers 的元素命名的 n 个 renderbuffer 对象。删除 renderbuffer 对象后，它没有内容，其名称可以被复用（例如 **glGenRenderbuffers**）。

　　如果删除当前绑定的 renderbuffer 对象，则绑定将还原为 0（没有任何 renderbuffer 对象）。此外，如果 renderbuffer 对象的图像附加到帧缓冲区对象，则在删除渲染缓冲区对象时必须特别小心。在这种情况下，如果删除的 renderbuffer 对象附加到当前绑定的帧缓冲区对象，则会自动分离它。但是，任何其他帧缓冲对象的附加都是应用程序的责任。

　　glDeleteRenderbuffer 默认忽略 0 和与现有 renderbuffer 对象不对应的名称。

### 错误

　　如果 n 是负数，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glDeleteRenderbuffers](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDeleteRenderbuffers.xml)