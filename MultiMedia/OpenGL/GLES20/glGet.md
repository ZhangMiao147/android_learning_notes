# glGet

### 功能

　　返回所选参数的一个或多个值。

### 方法

```java
public static native void glGetBooleanv(int pname,boolean[] params,int offset);

public static native void glGetBooleanv(int pname,java.nio.IntBuffer params);

public static native void glGetFloatv(int pname,float[] params,int offset);

public static native void glGetFloatv(int pname,java.nio.FloatBuffer params);

public static native void glGetIntegerv(int pname,int[] params,int offset);

public static native void glGetIntegerv(int pname,java.nio.IntBuffer params);
```

#### 参数

　　**pname** ：指定要返回的参数值。接受下面列表中的符号常量。

　　**params** ：返回指定参数的一个或多个值。

### 描述

　　这些命令返回 GL 中简单状态变量的值。pname 是一个符号常量，指示要返回的状态变量。params 是指向指定类型数组的指针，用来放置返回的数据。

　　如果 params 的类型与请求的状态变量值不同，则执行类型转换。如果调用 glBooleanv，当且仅当在浮点数是 0.0 ( 或 0 ) 时才会转换为 GL_FALSE。否则，它将转换为 GL_TRUE。如果调用 glGetIntegerv，则布尔值将返回 GL_TRUE 或 GL_FALSE，并且大多数浮点值将四舍五入为最接近的整数值。但是，浮点颜色和法线返回线性映射后的数值，该映射将 1.0 映射到最正可以表示的整数值，将 -1.0 映射到最负可以表示的整数值。如果调用 glGetFloatv，则布尔值将返回 GL_TRUE 或 GL_FALSE ，并且整数值将转换为浮点值。

### pname 接受以下符号常量

**GL_ACTIVE_TEXTURE：**params 返回一个指示活动的多重纹理单元的值。初始值为 **GL_TEXTURE0**。请参阅 **glActiveTexture**。

**GL_ALIASED_LINE_WIDTH_RANGE：**params 返回两个值，即锯齿线的最小和最大支持宽度。范围必须包括宽度 1。

**GL_ALIASED_POINT_SIZE_RANGE：**params 返回两个值，即锯齿点支持的最大和最小的尺寸。范围必须包括大小 1 。

**GL_ALPHA_BITS：**params 返回一个值，即当前绑定的帧缓冲区的颜色缓冲区中的 alpha 位平面的数量。

**GL_ARRAY_BUFFER_BINDING：**params 返回一个值，即当前绑定到目标 GL_ARRAY_BUFFER 的缓冲区对象的名称。如果没有缓冲区对象绑定到此目标，则返回 0 。初始值为 0 。请参阅 **glBindBuffer**。

**GL_BLEND：**params 返回一个布尔值，指示是否启用了混合。初始值为 GL_FALSE。请参阅 **glBiendFunc**。

**GL_BLEND_COLOR：**params 返回四个值，红色、绿色、蓝色和 alpha 值，它们是混合颜色的组成部分。请参阅 **glBlendColor**。

**GL_BLEND_DST_ALPHA：**params 返回一个值，该符号常量标识 alpha 目标混合函数。初始值为 GL_ZERO。请参阅 **glBlendFunc** 和 **glBlendFuncSeparate**。

**GL_BLEND_DST_RGE：**params 返回一个值，该符号常量标识 RGB 目标混合函数。初始值为 GL_ZERO。请参阅 **glBlendFunc** 和 **glBlendFuncSqparate**。

**GL_BLEND_EQUATION_ALPHA：**params 返回一个值，一个符号常量，指示 Alpha 混合方程是 GL_FUNC_ADD、GL_FUNC_SUBTRACT 还是 GL_FUNC_REVERSE_SUBTRACT。请参阅 **glBlendEquationSeparate**。

**GL_BLEND_EQUATION_RGB：**params 返回一个值，一个符号常量，指示 RGB 混合方程是 GL_FUNC_ADD、GL_FUNC_SUBTRACT 还是 GL_FUNC_REVERSE_SUBTRACT。请参阅 **glBlendEquationSeparate**。

**GL_BLEND_SRC_ALPHA：**params 返回一个值，这是一个标识  alpha 源混合函数的符号常量。初始值为 GL_ONE。请参阅 **glBlendFunc** 和 **glBlendFuncSeparate**。

**GL_BLEND_SRC_RGB：**params 返回一个值，这个符号常量标识 RGB 源混合函数。初始值为 GL_ONE。请参阅 **glBlendFunc** 和 **glBlendFuncSeparate**。

**GL_BLUE_BITS：**params 返回一个值，即当前绑定的帧缓冲区的颜色缓冲区中的蓝色位平面的数量。

**GL_COLOR_CLEAR_VALUE：**params 返回四个值：用于清除颜色缓冲区的红色、绿色、蓝色和 alpha 值。如果请求，使用线性映射将内存浮点数转为整数值，使得 1.0 返回最正可表示的整数值，-1.0 返回最负可表示的整数值。初始值为（0，0，0，0）.请参阅 **glClearColor**。

**GL_COLOR_WRITEMASK：**params 返回四个布尔值：启用红色、绿色、蓝色和 alpha 写入颜色缓冲区。初始值为 （GL_TRUE、GL_TRUE、GL_TRUE、GL_TRUE）。请参阅 **glColorMask**。

**GL_COMPRESSED_TEXTURE_FORMATS：**params 返回长度为 GL_NUM_COMPRESSED_TEXTURE_FORMATS 的符号常量列表，指示哪些压缩纹理格式可用。请参阅 **glCompressedTexImage2D**。

**GL_CULL_FACE：**params 返回一个布尔值，指示是否启用了多边形剔除。初始值为 GL_FALSE。请参阅 **glCullFace**。

**GL_CULL_FACE_MODE：**params 返回一个值，一个符号常量，指示要剔除哪些多边形面。初始值为 GL_BACK。请参阅 **glCullFace**。

**GL_CURRENT_PROGRAM：**params 返回一个值，即当前活动的程序对象的名称，如果没有程序对象处于活动状态，则返回 0。请参阅 **glUseProgram**。

**GL_DEPTH_BITS：**params 返回一个值，即当前绑定帧缓冲区的深度缓冲区中的位平面数。

**GL_DEPTH_CLEAR_VALUE：**params 返回一个值，该值用于清除深度缓冲区。如果请求，使用线性映射将内部浮点值转换为整数值。使得 1.0 返回最正可表示的整数值，-1.0 返回最负可表示的整数值。初始值为 1。请参阅 **glClearDepthf**。

**GL_DEPTH_FUNC：**params 返回一个值，该符号常量表示深度比较函数。初始值为 GL_LESS。请参阅 **glDepthFunc**。

**GL_DEPTH_RANGS：**params 返回两个值：深度缓冲区的映射限制和远映射限制。如果请求，使用线性映射将内部浮点值转换为整数值，使得 1.0 返回最正可表示的整数值，-1.0 返回最负可表示的整数值。初始值为 （0，1）。请参阅 **glDepthRangef**。

**GL_DEPTH_TEST：**params 返回一个布尔值，指示是否启用了片段的深度测试。初始值为 GL_FALSE。请参阅 **glDepthFunc** 和 **glDepthRangef**。

**GL_DEPTH_WRITEMASK：**params 返回一个布尔值，指示深度缓冲区是否已启用写入。初始值为 GL_TRUE。请参阅 **glDepthMask**。

**GL_DITHER：**params 返回一个布尔值，指示是否启用了片段颜色和索引的抖动。初始值为 GL_TRUE。

**GL_ELEMENTS_ARRAY_BUFFER_BINDING：**params 返回单个值，即当前绑定到目标 GL_ELEMENT_ARRAY_BUFFER 的缓冲区对象的名称。如果没有缓冲区对象绑定到此目标，则返回 0 。初始值为 0 。请参阅 **glBindBuffer**。

**GL_FRAMEBUFFER_BINDING：**params 返回一个值，即当前绑定的帧缓冲区的名称。初始值为 0 ，表示默认的帧缓冲区。请参阅 **glBindFramebuffer**。

**GL_FRONT_FACE：**params 返回一个值，一个符号常量，表示顺时针或逆时针多边形缠绕是否被视为正面。初始值为 GL_CCW。请参阅 **glDrontFace**。

**GL_GENERATE_MIPMAP_HINT：**params 返回一个值，一个符号常量，指示 mipmap 生成过滤提示的模式。初始值为 GL_DONT_CARE。见 **glHint**。

**GL_GREEN_BITS：**params 返回一个值，即当前绑定的帧缓冲区的颜色缓冲区中的绿色位平面的数量。

**GL_IMPLEMENTATION_COLOR_READ_FORMAT:**params 返回一个值，即通过实现选择的格式，实现是指结合 GL_IMPLEMENTATION_COLOR_READ_TYPE 从当前绑定的帧缓冲区的颜色缓冲区中读取像素。并结合 GL_IMPLEMENTATION_COLOR_READ_TYPE。除了这种依赖与实现的格式 / 类型对之外，无论当前绑定的渲染界面如何，每个实现始终允许将格式 GL_RGBA 和类型 GL_UNSIGNED_BYTE 结合使用。请参阅 **glReadPixels**。

**GL_IMPLEMENTATION_COLOR_READ_TYPE：**params 返回一个值，即通过实现选择的类型，实现是指结合GL_IMPLEMENTATION_COLOR_READ_FORMAT 从当前绑定的帧缓冲区的颜色缓冲区中读取像素。  该实现选择的类型可以从当前绑定的帧缓冲区的颜色缓冲区中读取像素，并结合 GL_IMPLEMENTATION_COLOR_READ_FORMAT。除了这种依赖于实现 / 类型对之外，武林当前绑定的渲染表面如何，每个实现始终允许格式 GL_RGBA 和 类型 GL_UNSIGNED_BYTE 结合使用。请参阅 **glReadPixels**。

**GL_LINE_WIDTH：**params 返回一个值，即 glLineWidth 指定的线宽。初始值为 1。

**GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS：**params 返回一个值，最大支持的纹理图像单元数，可用于从顶点着色器和片段处理器组合访问纹理贴图(texture maps)。如果顶点着色器和片段处理阶段都访问同一纹理图像单元，然后这就相当于使用两个纹理图像单元破坏了此限制。该值必须至少为 8 。请参阅 **glActiveTexture**。

**GL_MAXCUBE_MAP_TEXTURE_SIZE：**params 返回一个值。该值粗略估计了 GL 可以处理的最大立方体贴图纹理。该值必须至少为 16。请参阅 **glTexImage2D**。

**GL_MAX_FRAGMENT_UNIFORM_VECTORS：**params 返回一个值，可以保存在片段着色器的统一变量存储中的四个元素的浮点数、整数或布尔矢量的最大数量。该值必须至少为 16。请参阅 **glUniform**。

**GL_MAX_RENDERBUFFER_SIZE：**params 返回一个值。该值表示 GL 可以处理的最大渲染缓冲区宽度和高度。该值必须至少为 1。请参阅 **glRenderbufferStorage**。

**GL_MAX_TEXTURE_IMAGE_UNITS：**params 返回一个值，可用于从片段着色器访问纹理贴图的最大支持纹理图像单元。该值必须至少为 8 。请参阅 **glActiveTexture**。

**GL_MAX_TEXTURE_SIZE：**params 返回一个值。该值粗略估计了 GL 可以处理的最大纹理。该值必须至少为 64。请参阅 **glTexImage2D**。

**GL_MAX_VARYING_VECTORS：**params 返回一个值，最大数量的四元素浮点向量，可用于插值顶点和片段着色器使用的变化变量。声明为矩阵或数组的变量变量将使用多个插值器。该值必须至少为 8 。

**GL_MAX_VERTEX_ATTRIBS：**params 返回一个值，顶点着色器可以访问的 4 分量通用顶点属性的最大值。该值必须至少为 8 。请参阅 **glVertexAttrib**。

**GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS：**params 返回一个值，最大支持的纹理图像单元，可用于从顶点着色器访问纹理贴图。值可能为 0 。请参阅 **glActiveTexture**。

**GL_MAX_VERTEX_UNIFORM_VECTORS：**params 返回一个值，可以保存在顶点着色器的统一变量存储中的四元素浮点数、整数或布尔矢量的最大数量。该值必须至少为 128。请参阅 **glUniform**。

**GL_MAX_VIEWPORT_DIMS：**params 返回两个值：视口的最大支持宽度和高度。这些尺寸必须至少与要渲染的显示器的可见尺寸一样大。请参阅 **glViewport**。

**GL_NUM_COMPRESSED_TEXTURE_FORMATS：**params 返回一个整数值，表示可用的压缩纹理格式的数量。最小值是 0 。请参阅 **glCompressedTexImage2D**。

**GL_NUM_SHADER_BINARY_FORMATS：**params 返回一个整数值，表示可用着色器二进制格式的数量。最小值为 0 。请参阅 **glShaderBinary**。

**GL_PACK_ALIGNMENT：**params 返回一个值，用于将像素数据写入内存的字节对齐。初始值为 4 。请参阅 **glPolygonOffset**。

**GL_PACK_OFFSET_FACTOR：**params 返回一个值，缩放因子用于确定添加到多边形光栅化时生成的每个片段的深度值的变量偏移值。初始值为 0 。请参阅 **glPolygonOffset**。

**GL_POLYGON_OFFSET_FILL：**params 返回一个布尔值，表示在填充模式下是否为多边形启用了多边形偏移。初始值为 GL_FALSE。请参阅 **glPolygonOffset**。

**GL_POLYGON_OFFSET_UNITS：**params 返回一个值。此值乘以特定于实现的值，然后添加到光栅化多边形时生成的每个片段的深度值。初始值为 0 。请参阅 **glPolygonOffset**。

**GL_RED_BITS：**params 返回一个值，即当前绑定的帧缓冲区的颜色缓冲区中的红色位平面的数量。

**GL_RENDERBUFFER_BINDING：**params 返回一个值，即当前绑定的渲染缓冲区的名称。初始值为 0 ，表示没有绑定渲染缓冲区。请参阅 **glBindRenderbuffer**。

**GL_SAMPLE_ALPHA_TO_COVERAGE：**params 返回一个布尔值，表示片段覆盖值是否应与基于片段的 alpha 值的临时覆盖值进行 AND 运算。初始值为 GL_FALSE。请参阅 **glSampleCoverage**。

**GL_SAMPLE_BUFFERS：**params 返回一个整数值，表示与当前绑定的帧缓冲区关联的样本缓冲区的数量。请参阅 **glSampleCoverage**。

**GL_SAMEPLE_COVERAGE：**params 返回一个布尔值，表示片段覆盖值是否应与基于当前样本覆盖值的临时覆盖值进行 AND 运算。初始值为 GL_FALSE。请参阅 **glSampleCoverage**。

**GL_SAMPLE_COVERAGE_INVERT：**params 返回一个布尔值，指示是否应该反转临时覆盖值。请参阅 **glSampleConverage**。

**GL_SAMPLE_COVERAGE_VALUE：**params 返回一个正浮点值，表示当前样本覆盖值。请参阅 **glSampleConverage**。

**GL_SAMPLES：**params 返回一个整数值，表示当前绑定的帧缓冲区的覆盖掩码大小。请参阅 **glSampleConverage**。

**GL_SCISSOR_BOX：**params 返回四个值：裁剪框的 x 和 y 窗口坐标，后跟宽度和高度。最初，x 和 y 窗口坐标均为 0 ，宽度和高度设置为窗口大小。见  **glScissor**。

**GL_SCISSOR_TEST：**params 返回一个布尔值，表示是否启用裁剪。初始值为 GL_FALSE。见 **glScissor**。

**GL_SHADER_BINARY_FORMATS：**params 返回长度为 GL_NUM_SHADER_BINARY_FORMATS 的符号常量列表，表示哪些着色器二进制格式可用。请参阅 **glShaderBinary**。

**GL_SHADER_COMPILER：**params 返回一个布尔值，表示是否支持着色器编译器。GL_FALSE 表示对 **glShaderSource**、**glCompileShader** 或 **glReleaseShaderCompiler** 的任何调用都将导致生成 GL_INVALID_OPERATION 错误。

**GL_STENCIL_BACK_FAIL：**params 返回一个值，一个符号常量，表示当模板测试失败时对背面多边形采取的操作。初始值为 GL_KEEP。请参阅 **glStencilOpSeparate**。

**GL_STENCIL_BACK_FUNC：**params 返回一个值，一个符号常量，指示用于背面多边形比较模板参考值与模板缓冲区值的函数。初始值为 GL_ALWAYS。请参阅 **glStencilFuncSeparate**。

**GL_STENCIL_BACK_PASS_DEPTH_FAIL：**params 返回一个值，一个符号常量，表示当模板测试通过但深度测试失败时对背面多边形采取的操作。初始值为 GL_KEEP。请参阅 **glStencilOpSeparate**。

**GL_STENCIL_BACK_PASS_DEPTH_PASS：**params 返回一个值，一个符号常量，表示当模板测试通过并且深度测试通过时，对于背面多边形采取的操作。初始值为 GL_KEEP。请参阅 **glStencilOpSeparate**。

**GL_STENCIL_BACK_REF：**params 返回一个值，该值与背面多边形的模板缓冲区内容进行比较。初始值为 0 。请参阅 **glStencilFuncSeparate**。

**GL_STENCIL_BACK_VALUE_MASK：**params 返回一个值，即用于背面多边形的掩码，用于在比较模板参考值和模板缓冲区值之前对其进行掩码。初始值全是 1 。请参阅 **glStencilFuncSeparate**。

**GL_STENCIL_BACK_WRITEMASK：**params 返回一个值，即控制背面多边形的模板位平面的写入的掩码。初始值全是 1 。请参阅 **glStencilMask**。

**GL_STENCIL_BITS：**params 返回一个值，即当前绑定的帧缓冲区的模板缓冲区中的位平面的数量。

**GL_STENCIL_CLEAR_VALUE：**params 返回一个值，即模板位平面被清除的索引。初始值为 0 。请参阅 **glClearStencil**。

**GL_STENCIL_FAIL：**params 返回一个值，一个符号常量，表示当前面的多边形和非多边形的模板测试失败时采取的操作。初始值为 GL_KEEP。请参阅 **glStencilOp** 和 **glStencilOpSeparate**。

**GL_STENCIL_FUNC：**params 返回一个值，一个符号常量，指示用于比较前面多边形和非多边形的模板参考值与模板缓冲区值的函数。初始值为 GL_ALWAYS。请参阅 **glStencilFunc** 和 **glStencilFuncSeparate**。

**GL_STENCIL_PASS_DEPTH_FAIL：**params 返回一个值，一个符号常量，表示模板测试通过时执行的操作，但对于前向多边形和非多边形是深度测试失败。初始值为 GL_KEEP。请参阅 **glStencilOp** 和 **glStencilOpSeparate**。

**GL_STENCIL_PASS_DEPTH_PASS：** params 返回一个值。一个符号常量，表示模板测试通过时以及前向多边形和非多边形的深度测试通过时采取的操作。初始值为 GL_KEEP。请参阅 **glStencilOp** 和 **glStencilOpSeparate**。

**GL_STENCIL_REF：**params 返回一个值，是前向多边形和非多边形的模板缓冲区的内容进行比较的参考值。初始值为 0 。请参阅 **glStencilFunc** 和 **glStencilFuncSeparate**。

**GL_STENCIL_TEST：**params 返回一个布尔值，表示是否启用了片段的模板测试。初始值为 GL_FALSE。请参阅 **glStencilFunc** 和 **glStencilOp**。

**GL_STENCIL_VALUE_MASK：**params 返回一个值，该掩码用于在前向多边形和非多边形对模板参考值与模板缓冲区值比较之间进行掩盖。初始值全是 1。请参阅 **glStencilFunc** 和 **glStencilFuncSeparate**。

**GL_STENCIL_WRITEMASK：**params 返回一个值，该掩码用于控制前向多边形和非多边形的模板位平面的写入。初始值全是 1 。请参阅 **glStencilMask** 和 **glStencilMaskSeparate**。

**GL_SUBPIXEL_BITS：**params 返回一个值，即用于在窗口坐标中定位光栅化几何体的子像素分辨率的位数的估计值。该值必须至少为 4。

**GL_TEXTURE_BINDING_2D：**params 返回一个值，即当前绑定到活动多纹理单元的目标 GL_TEXTURE_2D 的纹理的名称。初始值为 0 。请参阅 **glBindTexture**。

**GL_TEXTURE_BINDING_CUBE_MAP：**params 返回单个值，当前绑定到活动多纹理单元的目标 GL_TEXTURE_CUBE_MAP 的纹理的名称。初始值为 0 。请参阅 **glBindTexture**。

**GL_UNPACK_ALIGNMENT：**params 返回一个值，用于从内存中读取像素数据的字节对齐。初始值为 4 。请参阅 **glPixelStorei**。

**GL_VIEWPORT：**params 返回四个值：视口的 x 和 y 窗口坐标，后跟其宽度和高度。最初，x 和 y 窗口坐标都设置为 0 ，宽度和高度设置为 GL 将进行渲染的窗口的宽度和高度。请参阅 **glViewport**。

　　使用 **glIsEnabled** 也可以更轻松的查询许多布尔参数。

### 错误

　　如果 pname 不是前面列出的值之一，则产生 GL_INVALID_ENUM 错误。

### 原文地址

[glGet](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGet.xml)