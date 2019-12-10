# glBindTexture

#### 功能

　　将一个指定的纹理 ID 绑定到一个纹理目标上。

#### 方法

```
void glBindTexture(GLenum target, GLuint texture);
```

##### 参数

　　**target：**指定之前激活了的纹理要绑定到的一个目标。必须是 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP。

　　**texture：**指定纹理 ID。

#### 描述

　　glBindTexture 可以让你创建或使用一个纹理 ID。调用 glBindTexture，target 设置为 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP，texture 设置为已经激活了的要绑定到目标的纹理 ID。当一个纹理 ID 绑定到目标时，这个目标之前的绑定关系就会自动解除。

　　纹理 ID 都是无符整型的（unsigned intege），数值 0 被系统保留用于表示每个纹理目标的默认纹理。纹理 ID 和相应的纹理内容对当前的 GL 渲染环境的共享对象来说都是本地的。

　　可以调用 **glGenTextures** 来创建一系列的纹理 ID。

　　当一个纹理首次被绑定时，它采用指定的目标：一个纹理首次绑定到 GL_TEXTURE_2D 将称为二维纹理，首次绑定到 GL_TEXTURE_CUBE_MAP 的将称为立方体图纹理，紧接在第一次绑定后的二维纹理的状态等效于 GL 初始化时的默认 GL_TEXTURE_2D 的状态，对于立方体映射的纹理也是如此。

　　当一个纹理被绑定后，那 GL 对该纹理所绑定到的目标进行的操作也将影响绑定的纹理，而对目标的查询将是返回该纹理的状态值，也就是相当于目标变成了纹理 ID 的另一个索引 ID。纹理 ID = 0 会引用在初始化时绑定到它们的默认纹理。

　　由 glBindTexture 创建的绑定关系将一直保持激活状态，除非当前目标被另一个纹理 ID 绑定，或者是绑定的纹理 ID 通过调用 **glDeleteTextures** 删除了。

　　一旦被创建，一个纹理 ID 是可以重复绑定到它原来的目标上的。使用 glBindTexture 将一个已经存在的纹理 ID 绑定到一个目标上要比通过 **glTexImage2D** 重新加载纹理图像快得多。

#### 使用错误

　　如果 target 不是一个被允许的值（GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP）则会出现 GL_INVALID_ENUM 错误。

　　如果 texture 是之前创建的，并且绑定了一个目标，而这一次调用 glBindTexture 绑定的目标和之前的目标不一致，则会出现 GL_INVALID_OPERATION 错误。








