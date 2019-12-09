# GLES20

#### GLES20.glGetAttribLocation()

　　获取着色器程序中，指定为 attribute 类型变量的 id。



#### GLES20.glGetUniformLocation()

　　获取着色器程序中，指定为 uniform 类型变量的 id。



#### GLES20.glUseProgram()
　　使用 shader 程序。


#### 启用或者禁用顶点属性数组
　　调用 GLES20.glEnableAttribArray 和 GLES20.glDisableVertexAttribArray 传入参数 index。

```
GLES20.glEnableVertexAttribArray(glHPosition);
GLES20.glDisableVertexAttribArray(glHCoordinate);
```
　　如果启用，那么当 GLES20.glDrawArrays 或者 GLES20.glDraeElements 被调用时，顶点属性数组会被使用。

GLES20.glEnableVertexAttribArray()
　　允许使用顶点坐标数组。


#### GLES20.glVertexAttribPointer(int index,int size,int type,boolean normalized,int stride,buffer ptr)
　　定义顶点属性数组。
参数含义：
index：指定要修改的顶点着色器中顶点变量 id。
size：指定每个顶点属性的组件数量。必须为1、2、3 或者 4.如 position 是由 3 个 （x,y,z）组成，而颜色是 4 个（r,g,b,a）。
type：指定数组中每个组件的数据类型。可用的符号常量有 GL_BYTE、GL_UNSIGNED_BYTE、GL_SHORT、GL_FIXED 和 GL_FLOAT,初始值为 GL_FLOAT。
normalized：指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）。
stride：指定连续顶点属性之间的偏移量。如果为 0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0，如果 normalized 被设置为 GL_TRUE，意味着整数型的值会被映射至区间 -1,1，或者区间[0,1]（无符号整数），反之，这些值会被直接转换为浮点值而不进行归一化处理。
ptr：顶点的缓冲数据。


#### GLES20.glDrawArrays()
　　图形绘制。


#### GLES20.glDisableVertexAttribArray()
　　允许使用顶点颜色数组。

#### 选择活动纹理单元
```
void glActiveTexture(int texture)
```

　　texture 指定哪一个纹理单元被置为活动状态。texture 必须是 GL_TEXTUTEi 之一，其中 0 <= i < GL_MAX_COMNIBED_TEXTURE_IMAGE_UNITS，初始值为 GL_TEXTURE0。

　　GLES20.glActiveTexture() 确定了后续的纹理状态改变影响哪个纹理，纹理单元的数量是一句所被支持的具体实现。

#### GLES20.glClearColor(float red, float green, float blue, float alpha)
　　为颜色缓冲区指定清除值。

　　glClearColor 为 glClear 清楚颜色缓冲区时指定 RGBA 值（也就是所有的颜色都会被替换成指定的 RGBA 值）。每个值的取值范围都是 0.0~1.0，超出范围的将被截断。

#### GLES20.glClear(int var0)
　　把窗口清除为当前颜色。
```
   glClearColor (0.0, 0.0, 0.0, 0.0);//设置清除颜色
   glClear(GL_COLOR_BUFFER_BIT);//把窗口清除为当前颜色
   glClearDepth(1.0);//指定深度缓冲区中每个像素需要的值
   glClear(GL_DEPTH_BUFFER_BIT);//清除深度缓冲区
```

　　像素颜色在图形硬件中的存储方法有两种：1.RGBA，2.像素索引。

`glClear(glBitField mask);`缓冲区的种类：
缓冲区   名称
颜色缓冲区  GL_COLOR_BUFFER_BIT
深度缓冲区  GL_DEPTH_BUFFER_BIT
累积缓冲区  GL_ACCUM_BUFFER_BIT
模版缓冲区  GL_STENCIL_BUFFER_BIT


#### GLES20.glUniformMatrix4fv(int location, int count, boolean transpose, float[] matrix, int var4)
　　更新 matrix 的值，即更新矩阵数组。

[glUnitormMatrix4fv](https://blog.csdn.net/suyimin2010/article/details/99706540)


#### GLES20.glBindTexture(int target, int texture)
　　将一个指定的纹理 ID 绑定到一个纹理目标上。

　　target：指定之前激活了的纹理要绑定到的一个目标。必须是 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP。

　　texture：指定纹理 ID。

　　glBindTexture 可以让你创建或使用一个纹理 ID。调用 glBindTexture，target 设置为 GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP，texture 设置为已经激活了的要绑定到目标的纹理 ID。当一个纹理 ID 绑定到目标时，这个目标之前的绑定关系就会自动解除。

　　纹理 ID 都是无符整型的（unsigned intege），数值 0 被系统保留用于表示每个纹理目标的默认纹理。纹理 ID 和相应的纹理内容对当前的 GL 渲染环境的共享对象来说都是本地的。

　　可以调用 glGenTextures 来创建一系列的纹理 ID。

　　当一个纹理首次被绑定时，它采用指定的目标：一个纹理首次绑定到 GL_TEXTURE_2D 将称为二维纹理，首次绑定到 GL_TEXTURE_CUBE_MAP 的将称为立方体图纹理，紧接在第一次绑定后的二维纹理的状态等效于 GL 初始化时的默认 GL_TEXTURE_2D 的状态，对于立方体映射的纹理也是如此。

　　当一个纹理被绑定后，那 GL 对该纹理所绑定到的目标进行的操作也将影响绑定的纹理，而对目标的查询将是返回该纹理的状态值，也就是相当于目标变成了纹理 ID 的另一个索引 ID。纹理 ID = 0 会引用在初始化时绑定到它们的默认纹理。

　　由 glBindTexture 创建的绑定关系将一直保持激活状态，除非当前目标被另一个纹理 ID 绑定，或者是绑定的纹理 ID 通过调用 glDeleteTextures 删除了。

　　一旦被创建，一个纹理 ID 是可以重复绑定到它原来的目标上的。使用 glBindTexture 将一个已经存在的纹理 ID 绑定到一个目标上要比通过 glTexImage2D 重新加载纹理图像快得多。

　　如果 target 不是一个被允许的值（GL_TEXTURE_2D 或 GL_TEXTURE_CUBE_MAP）则会出现 GL_INVALID_ENUM 错误。

　　如果 texture 是之前创建的，并且绑定了一个目标，而这一次调用 glBindTexture 绑定的目标和之前的目标不一致，则会出现 GL_INVALID_OPERATION 错误。

[LES2.0中文API-glBindTexture](https://blog.csdn.net/flycatdeng/article/details/82664549)

#### GLES20.glUniform()
　　指定当前程序对象的统一变量的值。

```
void glUniform1f(    GLint location, GLfloat v0);
void glUniform2f(    GLint location,  GLfloat v0, GLfloat v1);
void glUniform3f(    GLint location, GLfloat v0, GLfloat v1, GLfloat v2);
void glUniform4f(    GLint location, GLfloat v0, GLfloat v1, GLfloat v2, GLfloat v3);
void glUniform1i(    GLint location, GLint v0);
void glUniform2i(    GLint location,GLint v0,GLint v1);
void glUniform3i(    GLint location,GLint v0,GLint v1,GLint v2);
void glUniform4i(    GLint location,GLint v0,GLint v1,GLint v2,GLint v3);

void glUniform1fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform2fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform3fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform4fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform1iv(    GLint location,GLsizei count,const GLint *value);
void glUniform2iv(    GLint location,GLsizei count,const GLint *value);
void glUniform3iv(    GLint location, GLsizei count, const GLint *value);
void glUniform4iv(    GLint location, GLsizei count, const GLint *value);

void glUniformMatrix2fv(    GLint location,GLsizei countM,GLboolean transpose,const GLfloat *valueM);
void glUniformMatrix3fv(    GLint location,GLsizei countM,GLboolean transpose,const GLfloat *valueM);
void glUniformMatrix4fv(    GLint location,GLsizei countM,GLboolean transpose, const GLfloat *valueM);

```
##### 参数
location：指定要修改的统一变量的位置。


#### GLES20.glCreateProgram()



#### GLES20.glAttachShader(int var0, int var1)



#### GLES20.glLinkProgram(int var0)



#### GLES20.glGetProgramiv(int var0, int var1, int[] var2, int var3)



#### GLES20.glDeleteProgram(int var0)



#### GLES20.glCreateShader(int var0)



#### GLES20.glShaderSource(int var0, String var1)





#### GLES20.glCompileShader(int var0)



#### GLES20.glGetShaderiv(int var0, int var1, int[] var2, int var3)





#### GLES20.glDeleteShader(int var0)



#### GLES20.glDeleteRenderbuffers(int var0, int[] var1, int var2)



#### GLES20.glDeleteFramebuffers(int var0, int[] var1, int var2)



#### GLES20.glDeleteTextures(int var0, int[] var1, int var2)





#### GLES20.glDisable(int var0)



#### GLES20.glTexParameterf()



#### GLES20.glBindFramebuffer(int target,int framebuffer)



#### GLES20.glFramebufferTexture2D(int target,int attachment,int textarget,int texture,int level)



#### GLES20.glFramebufferRenderbuffer()



#### GLES20.glGenTextures(int var0, int[] var1, int var2)



#### GLES20.glTexImage2D()







## 查阅资料

1. [OpenGL ES之八——GLES20类和Matrix类](https://blog.csdn.net/gongxiaoou/article/details/89367319)








