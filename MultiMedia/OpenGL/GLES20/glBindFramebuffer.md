# glBindFramebuffer

### 功能

　　绑定命名的帧缓冲区对象。

### 方法

```java
public static native void glBindFramebuffer(int target,int framebuffer);
```

##### 参数

　　**target：**指定帧缓冲区对象绑定的目标。符号常量必须是 GL_FRAMEBUFFER。

　　**framebuffer：**指定帧缓冲区对象的名称。

### 描述

　　glBindFramebuffer 允许创建或使用命名的帧缓冲对象。调用 glBindFramebuffer 并将目标设置为 **GL_FRAMEBUFFER**，将 framebuffer 设置为新的绑定了帧缓冲区对象名称的帧缓冲区对象。绑定帧缓冲区对象时，先前的绑定会自动中断。

　　帧缓冲区对象名称是无符号整数。保留值零表示由窗口系统提供的默认帧缓冲区。帧缓冲区对象的对象名称和相应的 framebuffer 对象内容对于当前 GL 渲染上下文的共享对象空间是本地的。

　　可以使用 **glGenFramebuffers** 生成一组新的 framebuffer 对象名称。

　　第一次绑定后的帧缓冲对象的状态时三个附着点（GL_COLOR_ATTACHMENTO，GL_DEPTH_ATTACHMENT 和 GL_STENCIL_ATTACHMENT），每个附加点都以 GL_NONE 作为对象类型。

　　当绑定了非零帧缓冲区对象名称，但目标 GL_FRAMEBUFFER 上的 GL 操作会影响绑定的帧缓冲区对象，查询目标 GL_FRAMEBUFFER 或帧缓冲区详细信息（如 GL_DEPTH_BITS）的状态将从绑定的帧缓冲区对象返回。当绑定帧缓冲对象名称为 0 时，则处于初始状态中，尝试修改或查询目标 GL_FRAMEBUFFER 上的状态会生成 GL_INVALID_OPERATION 错误。

　　当绑定了非零帧缓冲对象名称时，所有渲染到帧缓冲区（使用 **glDrawArrays** 和 **glDrawElements**）和从帧缓冲区读取（使用 **glReadPixels**、**glCopyTexImage2D** 或 **glCopyTexSubImage2D**）都使用附加到应用程序创建的帧缓冲区对象的图像，而不是默认窗口系统提供的帧缓冲区。

　　应用程序创建的帧缓冲区对象（即具有非零名称的对象）与默认的窗口系统提供的帧缓冲区有几个重要的不同。首先，它们具有用于颜色缓冲区、深度缓冲区和模板缓冲区的可修改的附着点，帧缓冲器可附着和分离图像。其次，附加图像的大小和格式完全由 GL 控制，不受窗口系统事件（例如像素格式选择、窗口大小调整和显示模式更改）的影响。第三，当呈现或读取应用程序创建的帧缓冲区对象时，像素所有权测试总是成功的（即它们拥有自己的所有像素）。第四，没有可见的颜色缓冲位平面，只有一个单独的”屏幕外“彩色图像附件，因此没有前后缓冲或交换的感觉。最后，不存在多样本缓冲区，因此依赖于实现的状态变量 GL_SAMPLES 和 GL_SAMPLE_BUFFERS 的值对于应用程序创建的帧缓冲区对象都是零。



### 注意

　　依赖于实现的像素深度和相关状态的查询是从当前绑定的帧缓冲对象导出的。这些包括 GL_RED_BITS、GL_GREEN_BITS、GL_BLUR_BITS、GL_ALPHA_BITS、GL_DEPTH_BITS、GL_STENCIL_BITS（模板）、GL_IMPLEMENTATION_COLOR_READ_TYPE、GL_IMPLEMENTATION_COLOR_READ_FORMAT、GL_SAMPLES 和 GL_SAMPLE_BUFFERS。

### 错误

　　如果目标不是 GL_FRAMEBUFFER ，则生成 **GL_INVALID_ENUM**。

### 原文地址

[glBindFramebuffer](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBindFramebuffer.xml)




