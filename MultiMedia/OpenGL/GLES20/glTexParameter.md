## glTexParameter

### 功能

　　设置纹理参数。

### 方法

```java
public static native void glTexParameterf(int target,int pname,float param);

public static native void glTexParameteri(int target,int pname,int param);

public static native void glTexParameterfv(int target,int pname,float[] params,int offset);

    // C function void glTexParameterfv ( GLenum target, GLenum pname, const GLfloat *params )

public static native void glTexParameterfv(int target,int pname,java.nio.FloatBuffer params);

public static native void glTexParameteriv(int target,int pname,int[] params,int offset);

    // C function void glTexParameteriv ( GLenum target, GLenum pname, const GLint *params )

public static native void glTexParameteriv(int target,int pname,java.nio.IntBuffer params);
```

##### 参数

target：指定之前激活了的纹理要绑定到的一个目标。必须是 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP。

pname：指定一个单值而努力参数的符号名，pname 可以是下列值之一：GL_TEXTURE_MIN_FILTER、GL_TEXTURE_MAG_FILTER、GL_TEXTURE_WRAP+S、GL_TEXTURE_MRAP_T。

pname：指定 pname 的值。

params：指定 pname 存储的值的数组的指针。

### 描述

　　纹理贴图是一种将图像应用到对象表面的技术，就像图像时贴花或玻璃纸收缩包装一样。图像在纹理空间中创建，具有 （s，t）坐标系。纹理时二维或立方体映射的图像和一组参数，用于确定如何从图像中导出样本。

　　glTexParameter 将 params 中的一个或多个值分配给指定为 pname 的纹理参数。target 定义激活的纹理单元的目标纹理，可以是 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP。pname 中接受以下符号：

**GL_TEXTURE_MIN_FILTER**：只要纹理贴图比要贴的区域大，就会使用这个纹理缩小功能。有六个定义的缩小功能。其中两个值使用最近的一个或最近的四个纹理元素来计算纹理值。其中四个值用于 mipmap。

　　mipmap 是一组有序的数组，以逐渐降低的分辨率表示相同的图像。如果纹理具有 2 ^ n * 2 ^ m 的维度，则存在 max(n,m) + 1 个 mipmap。第一个 mipmap 就是原始纹理，尺寸为 2 ^ n * 2 ^ m。每个后续的 mipmap 具有维度 2 ^ (k-1) * 2 ^ (l-1)，其中 2 ^ k * 2 ^ 1 是前一个 mipmap 的维度，直到 k = 0 或 l = 0 。此时，后续的 mipmap 具有尺寸 1 * 2 ^ (l-1) 或  2 ^ ( k - 1) * 1 ，直到最终的 mipmap ，其尺寸为 1 * 1。

　　通过调用 **glTexImage2D** 、**glCompressedTexImage2D**、**glCopyTexImage2D** 设置 level 参数来定义 mipmap 的级别。 0 级时原始纹理，level max ( n , m) 是最终的 1 * 1 mipmap。

　　params 提供的缩小采样功能，可选参数如下：

　　**GL_NEAREST**：临近采样，返回与纹理像素的中心最接近（在曼哈顿距离内）的纹理元素的值。

　　**GL_LINEAR**：线性采样，返回最接近被纹理像素中的四个纹理元素的加权平均值。

　　**GL_NEAREST_MIPMAP_NEAREST**：选择最接近匹配纹理像素大小的 mipmap，并使用 GL_NEAREST 标准（最接近像素中心的纹理元素）来生成纹理值。







### 注意



### 错误



### 原文地址

[]()

　　允许使用顶点颜色数组。
