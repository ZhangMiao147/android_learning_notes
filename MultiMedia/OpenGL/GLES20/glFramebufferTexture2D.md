## glFramebufferTexture2D

### 功能

　　将纹理（texture）图像附加到帧缓冲区（framebuffer）对象。

### 方法

```java
public static native void glFramebufferTexture2D(int target,int attachment,int textarget,int texture,int level);
```

##### 参数

target：指定帧缓冲区目标。符号常量必须是 GL_FRAMMEBUFFER。

attachment：指定应附加纹理图像的附着点。必须是以下符号常量之一：GL_COLOR_ATTACHMENT、GL_DEPTH_ATTACHMENT 或 GL_STENCIL_ATTACHMENT。

textarget：指定纹理目标。必须是以下符号常量之一：GL_TEXTURE_2D、GL_TEXTURE_CUBE_MAP_POSITION_X、GL_TEXTURE_CUBE_MAP_NEGATIVE_X、GL_TEXTURE_CUBE_MAP_POSITIVE_Y、GL_TEXTURE_CUBE_MAP_NEGATIVE_Y、GL_TEXTURE_CUBE_MAP_POSITIVE_Z 或 GL_TEXTURE_CUBE_MAP_NEGATIVE_Z。

texture：指定要附加图像的纹理对象。

level：指定要附加的纹理图像的 mipmap 级别，该级别必须为 0。

### 描述

　　glFramebufferTexture2D 将 texture 和 level 指定的纹理图像附加为当前绑定的帧缓冲区对象的逻辑缓冲区之一。attachment 指定是否应将纹理图像（texture image）附加到帧缓冲区对象的颜色、深度或模板缓冲区。纹理图像不可以附加到默认帧缓冲区对象（名称为 0 ）。

　　如果 texture 不为 0，则指定附加点的 GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE 的值设置为 GL_TEXTURE，GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME 的值设置为 texture，GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL 的值设置为 level。如果纹理是立方体贴图纹理，则 GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE 的值设置为 textarget，否则将其设置为默认值 GL_TEXTURE_CUBE_MAP_POSITIVE_X。当前绑定的帧缓冲区对象的逻辑缓冲区的任何附着都将被破坏。

　　如果 texture 为 0 ，则分离当前绑定的帧缓冲区对象的逻辑缓冲区附着的图像（如果有的话）。GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE 的值设置为 GL_NONE。GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME 的值设置为 0。GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL 和 GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE 分别设置为默认值 0 和 GL_TEXTURE_CUBE_MAP_POSICTION_X。

### 注意

　　当纹理对象当前被绑定并可能被当前顶点或片段着色器采样时，需要采取特殊预防措施以避免将纹理图像附加到当前绑定的帧缓冲区。这样做可能导致在通过渲染操作写入像素（一个操作写入像素）和在使用当前绑定纹理中的纹理时同时读取那些相同像素（一个操作读取像素）之间创建“反馈循环”。在这种情况下，帧缓冲区将被视为已完成帧缓冲区，但在此状态下渲染的片段的值将是未定义的。纹理样本的值也可能是未定义的。

　　如果在将图像附加到当前绑定的帧缓冲区时删除纹理对象，这就像是为了在当前绑定的 framebuffer 对象上附加此图像的附着点，调用 renderbuffer 为 0 的 glFramebufferRenderbuffer 方法。换句话说，纹理图像与当前绑定的帧缓冲区分离。请注意，纹理图像不会与任何未绑定的帧缓冲区分离。应用程序负责从任何非绑定帧缓冲区中分离图像。

### 错误

　　如果 target 不是 GL_FRAMEBUFFER，则产生 GL_INVALID_ENUM 错误。

　　如果 textarget 不是可接受的纹理 target 且 texture 不为 0 ，则产生 GL_INVALID_ENUM。

　　如果 attachment 是不可接受的附着点，则产生 GL_INVALID_ENUM 错误。

　　如果 level 不是 0 且 texture 不是 0，则产生 GL_INVALID_VALUE 错误。

　　如果绑定了默认的帧缓冲对象（对象名称为 0 ），则产生 GL_INVALID_OPERATION 错误。

　　如果 texture 既不是 0 也不是现有纹理对象的名称。则产生 GL_INVALID_OPERATION。

　　如果 texture 时现有二维纹理对象的名称，但 textarget 不是 GL_TEXTURE_2D，或者 texture 是现有立方体贴图纹理对象的名称，但 textarget 是 GL_TEXTURE_2D，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glFramebufferTexture2D](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glFramebufferTexture2D.xml)