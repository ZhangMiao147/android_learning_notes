## glFramebufferRenderbuffer

### 功能

　　将 renderbuffer 对象附加到 framebuffer 对象。

### 方法

```java
public static native void glFramebufferRenderbuffer(int target,int attachment,int renderbuffertarget,int renderbuffer);
```

##### 参数

target：指定帧缓冲目标。符号常量必须是 GL_FRAMEBUFFER。

attachment：指定 renderbuffer 应附加到的附着点。必须是以下符号常量之一：GL_COLOR_ATTACHMENT0 、GL_DEPTH_ATTACHMENT 或 GL_STENCIL_ATTACHMENT。

renderbuffertarget：指定 renderbuffer 目标。符号常量必须为 GL_RENDERBUFFER。

renderbuffer：指定要附加的 renderbuffer 对象。

### 描述

　　glFramebufferRenderbuffer 将 renderbuffer 指定的 renderbuffer 附加为

### 注意



### 错误



### 原文地址

[]()

　　允许使用顶点颜色数组。
