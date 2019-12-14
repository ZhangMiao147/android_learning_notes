## glEnable 与 glDisable

### 功能

　　启用或禁用服务器 GL 功能。

### 方法

```java
public static native void glEnable(int cap);
public static native void glDisable(int cap);
```

##### 参数

　　cap：指定表示 GL 功能的符号常量。

### 描述

　　glEnable 和 glDiable 启用和禁用各种功能。使用 **glIsEnable** 或 **glGet** 确定任何功能的当前设置。除 GL_DITHER(抖动)外，每个功能的初始值为 GL_FALSE。GL_DITHER 的初始值为 GL_TRUE。

　　glEnable 和 glDisable 都使用单个参数 cap，它可以采用以下值之一：

　　**GL_BLEND**

　　　　如果启用，则将计算的片段颜色值与颜色缓冲区中的值混合。请参阅 **glBlendFunc**。

　　**GL_CULL_FACE**

　　　　如果启用，则根据窗口的坐标来剔除多边形。请参阅 **glCullFace**。

　　**GL_DEPTH_TEST**

　　　　如果启用，进行深度比较并更新深度缓冲区。注意，即使存在深度缓冲区且深度掩码非零，如果禁用深度测试，也不会更新深度缓冲区。请参阅 **glDepthFunc** 和 **glDepthRangef** 。

　　**GL_DITHER**

　　　　如果启用，则在将颜色组件或索引写入颜色缓冲区之前对其进行抖动。

　　**GL_POLYGON_OFFSET_FILL**

　　　　如果启用，则会将偏移添加到由光栅化生成的多边形片段的深度值。请参阅 **glPolygonOffset**。（常用于处理 Z-fighting）。

　　**GL_SAMPLE_ALPHA_TO_CONVERAGE**

　　　　如果启用，则计算一个临时覆盖值，其中每个位由相应样本位置的 alpha 值确定。然后将临时覆盖值与片段覆盖至进行 AND 运算。

　　**GL_SAMPLE_COVERAGE**

　　　　如果启用，则片段的覆盖范围与临时覆盖值进行 AND 运算。如果 **GL_SAMPLE_COVERAGE_INVERT** 设置为 GL_TRUE，则反转 coverage 值。请参阅 **glSampleCoverage**。

　　**GL_SCISSOR_TEST**

　　　　如果启用，则丢弃裁剪矩形之外的片段。请参阅 **glScissior**。

　　**GL_STENCIL_TEST**

　　　　如果启用，将进行模板测试并更新模板缓冲区。请参阅 **glStencilFunc** 和 **glStencilOp**。

### 错误

　　如果 cap 不是之前列出的值之一，则产生 GL_INVALID_ENUM 错误。

### 原文地址

[glEnable](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glEnable.xml)

　　允许使用顶点颜色数组。
