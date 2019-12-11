# glBindTexture

### 功能

　　将命名纹理绑定到纹理目标上。

### 方法

```
public static native void glBindTexture(int target,int texture);
```

##### 参数

　　**target：**指定纹理绑定的纹理单元目标。必须是 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP。

　　**texture：**指定纹理的名称。

### 描述

　　glBindTexture 可以创建或使用一个命名纹理。将 target 设置为 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP，texture 设置为新纹理，调用 glBindTexture 方法将 texture 绑定到当钱活动纹理单元的 target 上。当一个纹理 ID 绑定到目标时，这个目标之前的绑定关系就会自动解除。

　　纹理名称都是无符整型的（unsigned intege），保留数值 0 用于表示每个纹理目标的默认纹理。纹理名称和相应的纹理内容是当前的 GL 渲染上下文的共享对象空间的本地内容。

　　可以调用 **glGenTextures** 来创建一系列的纹理名称。

　　当一个纹理首次被绑定时，它采用指定的目标：一个纹理首次绑定到 GL_TEXTURE_2D 将称为二维纹理，首次绑定到 GL_TEXTURE_CUBE_MAP 的将称为立方体映射纹理，紧接在第一次绑定后的二维纹理的状态等效于 GL 初始化时的默认 GL_TEXTURE_2D 的状态，对于立方体映射的纹理也是如此。

　　当一个纹理被绑定后，那 GL 对该纹理所绑定到的目标进行的操作也将影响绑定的纹理，而对其绑定的目标的查询从该绑定纹理返回状态，也就是相当于纹理目标变成了绑定目标的纹理的别名。纹理名称 0 表示初始化时绑定到它们的默认纹理。

　　使用 glBindTexture 创建的纹理绑定将一直保持活动状态，除非当前目标被另一个纹理名称绑定，或者通过调用 **glDeleteTextures** 删除绑定纹理名称。

　　一旦被创建，一个纹理名称是可以根据需要重复绑定到它原来的目标上的。使用 glBindTexture 将一个已经存在的纹理名称绑定到一个目标上要比通过 **glTexImage2D** 重新加载纹理图像快得多。

### 错误

　　如果 target 不是被允许的值（GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP）之一，则会出现 GL_INVALID_ENUM 错误。

　　如果 texture 是之前创建的，并且绑定了一个目标，而这一次调用 glBindTexture 绑定的目标和之前的目标不一致，则会出现 GL_INVALID_OPERATION 错误。

### 原文地址

[glBindTexture](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBindTexture.xml)






