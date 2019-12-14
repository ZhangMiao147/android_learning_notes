## glFramebufferRenderbuffer

### 功能

　　将 renderbuffer 对象附着到 framebuffer 对象。

### 方法

```java
public static native void glFramebufferRenderbuffer(int target,int attachment,int renderbuffertarget,int renderbuffer);
```

##### 参数

target：指定帧缓冲目标。符号常量必须是 GL_FRAMEBUFFER。

attachment：指定 renderbuffer 应附着到的附着点。必须是以下符号常量之一：GL_COLOR_ATTACHMENT0 、GL_DEPTH_ATTACHMENT 或 GL_STENCIL_ATTACHMENT。

renderbuffertarget：指定 renderbuffer 目标。符号常量必须为 GL_RENDERBUFFER。

renderbuffer：指定要附加的 renderbuffer 对象。

### 描述

　　glFramebufferRenderbuffer 将 renderbuffer 指定的 renderbuffer 附加为当前绑定的 framebuffer 对象的逻辑缓冲区之一。attachment 指定是否应将 renderbuffer 附加到 framebuffer 对象的颜色、深度或模板缓冲区。renderbuffer 不可以附加到默认的 framebuffer 对象（名称为 0 ）。

　　如果 renderbuffer 不为 0，则指定附加点的 GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE 的值设置为 GL_RENDERBUFFER，GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME 的值设置为 renderbuffer。GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL 和 GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE 分别设置为默认值 0 和 GL_TEXTURE_CUBE_MAP_POSITIVE_X。当前绑定的 framebuffer 对象的逻辑缓冲区的任何附着都将被破坏。

　　如果 renderbuffer 为 0，则分离当前绑定的 framebufffer 对象的逻辑缓冲区附着的图像（如果有的话）。GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE 的值设置为 GL_NONE。GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME 的值设置为 0。GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL 和 GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE 分别设置为默认值 0 和 GL_TEXTURE_CUBE_MAP_POSITIVE_X。

### 注意

　　如果在将图像附加到当前绑定的 framebuffer 时删除了 renderbuffer 对象，这就像是为了在当前绑定的 framebuffer 对象上附加此图像的附着点，调用 renderbuffer 为 0 的 glFramebufferRenderbuffer 方法。 换句话说，renderbuffer 图像将从当前绑定的 framebuffer 分离。请注意，renderbuffer 图像不会与任何未绑定的 framebuffer 分离。应用程序负责从任何非绑定 framebuffer 中分离图像。

### 错误

　　如果 target 不是 GL_FRAMEBUFFER，则产生 GL_INVALID_ENUM 错误。

　　如果 renderbuffertarget 不是 GL_RENDERBUFFER 且 renderbuffer 不是 0 ，则产生 GL_INVALID_ENUM 错误。

　　如果 attachment 是不可接收的附着点，则产生 GL_INVALID_ENUM 错误。

　　如果绑定了默认的帧缓冲对象名称 0 ，则产生 GL_INVALID_OPERATION 错误。

　　如果 renderbuffer 既不是 0 也不是现有 renderbuffer 对象的名称，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glFramebufferRenderbuffer](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glFramebufferRenderbuffer.xml)

　　允许使用顶点颜色数组。
