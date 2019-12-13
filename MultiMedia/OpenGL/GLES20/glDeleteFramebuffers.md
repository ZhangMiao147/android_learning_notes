## glDeleteFramebuffers

### 功能

　　删除命名 framebuffer ( 帧缓冲区 ) 对象。 

### 方法

```java
public static native void glDeleteFramebuffers(int n,java.nio.IntBuffer framebuffers);
public static native void glDeleteFramebuffers(int n,int[] framebuffers,int offset);
```

##### 参数

　　n：指定要删除的帧缓冲区对象的数量。

　　framebuffers：指定要删除的帧缓冲区对象数据。

### 描述

　　glDeleteFramebuffers 删除由数组 framebuffers 的元素命名的 n 个 framebuffer 对象。删除帧缓冲区对象后，它没有附件，其名称可以被服用（例如调用 glGenFramebuffers 生成）。如果删除当前绑定的帧缓冲区对象，则绑定将恢复为 0 （窗口提供提供的帧缓冲区）。

　　glDeleteFramebuffers 默认忽略 0 和与现有帧缓冲对象不对应的名称。

### 错误

　　如果 n 为负，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glDeleteFramebuffers](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDeleteFramebuffers.xml)