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

#### GLES20.glClearColor(float var0, float var1, float var2, float var3)



#### GLES20.glClear(int var0)



#### GLES20.glUniformMatrix4fv(int var0, int var1, boolean var2, float[] var3, int var4)





#### GLES20.glActiveTexture(int var0)



#### GLES20.glBindTexture(int var0, int var1)





#### GLES20.glUniform1i(int var0, int var1)



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








