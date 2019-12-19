## glTexImage2D

### 功能

　　指定二维纹理图像。

### 方法

```java
public static native void glTexImage2D(int target,int level,int internalformat,int width,int height,int border,int format,int type,java.nio.Buffer pixels(consta void* data));
```

##### 参数

target：指定活动纹理单元的目标纹理。必须是 GL_TEXTURE_2D、GL_TEXTURE_CUBE_MAP_POSITIVE_X、GL_TEXTURE_CUBE_MAP_NEGATIVE_X、GL_TEXTURE_CUBE_MAP_POSITIVE_Y、GL_TEXTURE_CUBE_MAP_NEGATIVE_Y、GL_TEXTURE_CUBE_MAP_POSITIVE_Z 或 GL_TEXTURE_CUBE_MAP_NEGATIVE_Z 。

level：指定细节级别，0 级表示基本图像级别， n 级则表示 Mipmap 缩小 n 级之后的图像（缩小 2^n）。

internalformat：指定纹理内部格式，必须是下列符号常量之一：GL_ALPHA、GL_LUMINANCE、GL_LUMINANCE_ALPHA、GL_RGB、GL_REBA。

width：指定纹理图像的宽，所有实现都支持宽至少为 64 像素（texels ）的 2D 纹理图像和宽至少为 16 像素的立方体贴图纹理图像。

height：指定纹理图像的高度。所有实现都支持高至少 64 像素的2D 纹理图像和高至少 16 像素的立方体贴图纹理图像。

border：指定边框的宽度。必须为 0 。

format：指定纹理数据的格式。必须匹配 internalformat。下面的符号值被接受：GL_ALPHA、GL_RGB、GL_RGBA、GL_LUMINANCE 和 GL_LUMINANCE_ALPHA。

type：指定纹理数据的数据类型。下面的符号值被接受：GL_UNSIGNED_BYTE、GL_UNSIGNED_SHORT_5_6_5、GL_UNSIGNED_SHORT_4_4_4_4 和 GL_UNSIGNED_SHORT_5_5_5_1。

pixels(data)：指定一个指向内存中图像数据的指针。

### 描述

　　纹理将指定纹理图像的一部分映射到活动纹理化的每个图形基元上。当前片段着色器或顶点着色器使用内置纹理查找函数时，纹理处于活动状态。

　　要定义纹理图像，请调用 **glTexImage2D**。参数描述纹理图像的属性，如高度、宽度，细节级别（详见 **glTexParameter**）以及格式。最后三个参数描述了图像在内存中的表示方式。

　　根据 type 的不同，数据以无法好字节或短字节序列的形式从 data 中读取。当 type 是 GL_UNSIGNED_BYTE，每个字节被解释为一个颜色分量。当 type 是一下当中的一个 GL_UNSIGNED_SHORT_5_6_5、GL_UNSIGNED_SHORT_4_4_4_4 或 GL_UNSIGNED_SHORT_5_5_5_1，每个无符号 short 值被解释为包含所有组件的单个像素(texel)，颜色元素（color components）按照格式(fromat)排列。颜色元素被视为一个、两个、三个或四个值的组，也是基于格式（ format）。组件组被称为像素（texels）。

　　width * height 个像素（texels）将从内存中读取，起始位置就是 data 的起始地址。默认情况下，这些像素（texels）是从相邻的内存位置获取的，当在读取所有 width 个纹理后，读取指针前进到下一个四字节（four-byte）边界。**glReadPixels**使用参数 GL_UNPACK_ALIGNMENT 指定四字节（four-byte）行对齐，并且可以将其设置为一个、两个、四个或八个字节。

　　第一个元素对应于纹理图像的左下角。后面元素从纹理图像的最低行开始从左到右对剩下的像素（texels）进行排列，然后依次到达纹理图像的最高行。最后一个元素对应于纹理图像的右上角。

　　format 决定 data 中每个元素的组成。它可以是以下符号值之一：

**GL_ALPHA**：每个元素都是单个 alhpa 元素（component）。GL 将其转换为浮点并通过将 rgb 三通道赋值为 0 组装成 RGBA 元素。然后将每个元素的值范围限制在 [0，1]。

**GL_RGB**：每个元素都是 RGB 三元组。GL 将其转换为浮点，并通过为 alpha 赋值为 1 将其组装成 RGBA 元素。然后将每个元素的值范围限制在 [0，1]。

**GL_RGBA**：每个元素包含所有四个组件。GL 将其转换为浮点，然后将每个元素的值限制在 [0，1]。

**GL_LUMINANCE**：每个元素是单个亮度值。GL 将其转换为浮点，然后为红色、绿色和蓝色把亮度值复制三次， alpha 设置为 1 ，将其组装成 RGBA 元素。然后将每个元素的值范围限制在 [0，1]。

**GL_LUMINANCE_ALPHA**：每个元素是 “亮度-α“ 对。GL 将其转换为浮点，然后为红色、绿色和蓝色把亮度值复制三次，将其组装成 RGBA 元素。然后将每个元素的值范围限制在 [0，1]。

　　颜色元素（components）根据 type 转换为浮点。当 type 是 GL_UNSIGNED_BYTE 时，每个元素（compnent）除以2 8-1。当 type 为 GL_UNSIGNED)SHORT_5_6_5、GL_UNSIGNED_SHORT_4_4_4_4 或 GL_UNSIGNED_SHORT_5_5_5_1 时，每个元素除以 2 N -1，其中 N 是位域中的位数。

### 注意

　　internalformat 必须匹配 format。纹理图像处理期间不支持格式之间的转换。type 可以用作提示指定所需的精度，但 GL 实现可以选择以其选择的任何内部分辨率存储纹理数组。

　　data 可能是一个空指针。在这种情况下，会分配纹理内存以适应宽度 width 和高度 height。然后可以下载子文本来初始化这个纹理内存。如果用户尝试将纹理图像的未初始化部分应用于基元，则图像未定义。

　　glTexImage2D 是用来指定由 **glActiveTexture** 指定的纹理单元是二维或立体贴图纹理。

### 错误

　　如果 target 不是 GL_TEXTURE_2D、GL_TEXTURE_CUBE_MAP_POSITIVE_X、GL_TEXTURE_CUBE_MAP_NEGATIVE_X、GL_TEXTURE_CUBE_MAP_POSITIVE_Y、GL_TEXTURE_CUBE_MAP_NEGATIVE_Y、GL_TEXTURE_CUBE_MAP_POSITIVE_Z 或 GL_TEXTURE_CUBE_MAP_NEGATIVE_Z ，则产生 GL_INVALID_ENUM 错误。

　　如果 format 或 type 不是可接受的值，则产生 GL_INVALID_ENUM 错误。

　　如果 target 是立方体贴图中的一个二维贴图，但是宽和高不相等（立方体贴图的每个维度都是相等的），则产生 GL_INVALID_VALUE 错误。

　　如果 level 比 0 小，则产生 GL_INVALID_VALUE 错误。

　　如果 level 大于 log 以 2 为低 max 的对数。（max 是 target 为 GL_TEXTURE_2D 时 GL_MAX_TEXTURE_SIZE 的返回值，或者当 target 不是 GL_TEXTURE_2D 时 GL_MAX_CUBE_TEXTURE_SIZE 的返回值），则产生 GL_INVALID_VALUE 错误。

　　如果 internalformat 是一个不可接收的值，则产生 GL_INVALID_VALUE 错误。

　　如果 height 或 width 的值 value 或者当 target 为 GL_TEXTURE_2D 时 value 大于 GL_MAX_TEXTURE_SIZE，或者当 target 不为 GL_TEXTURE_2D 时 value 大于 GL_MAX_CUBE_MAP_TEXTURE_SIZE ，则产生 GL_INVALID_VALUE 错误。

　　如果 border 的值不为 0 时，则产生 GL_INVALID_VALUE 错误。

　　如果 format 和 internalformat 不匹配时，则产生 GL_INVALID_OPERATION 错误。

　　如果 type 是 GL_UNSIGNED_SHORT_5_6_5，但是 format 是 GL_RGB ，则产生 GL_INVALID_OPERATION 错误。

　　如果 type 是 GL_UNSIGNED_SHORT_4_4_4_4 或者 GL_UNSIGNED_SHORT_5_5_5_1，但 format 不是 GL_RGBA，则产生 GL_INVALID_OPERATION 错误。 

### 原文地址

[glTexImage2D](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glTexImage2D.xml)