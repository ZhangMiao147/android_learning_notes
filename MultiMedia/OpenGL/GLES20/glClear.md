## glClear

### 功能

　　清除预设值的缓冲区。

### 方法

```java
public static native void glClear(int mask);
```

##### 参数

mask：使用掩码的按位异或来表示要清除的缓冲区。三个掩码是 GL_COLOR_BUFFER_BIT、GL_DEPTH_BUFFER_BIT 和 GL_STENCIL_BUFFER_BIT。

### 描述

　　glClear 将窗口的位平面区域设置为先前由 **glClearColor**，**glClearDepthf** 和 **glClearStencil** 设置的值。

　　像素的归属测试、裁剪测试、抖动和缓冲区按位掩码会影响 glClear 的操作。裁剪箱限定了清除区域。glClear 忽略混合函数、模板、片元着色和深度缓冲。

　　glClear 采用单个参数，该参数是多个值的按位异或，指示要清除哪个缓冲区。

　　数值如下：

　　**GL_COLOR_BUFFER_BIT：**表示当前启用了颜色写入的缓冲区。

　　**GL_DEPTH_BUFFER_BIT：**深度缓冲区。

　　**GL_STENCIL_BUFFER_BIT：**指示模板缓冲区。

　　清除每个缓冲区的值取决于该缓冲区的清除值的设置。

### 注意

　　如果不存在缓冲区，则指向该缓冲区的 glClear 无效。

### 错误

　　如果掩码中设置了除三个定义之外的任何位，则出现 GL_INVALID_VALUE 错误。

### 原文地址

[glClear](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glClear.xml)




