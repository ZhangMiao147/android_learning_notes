## glClear

### 功能

　　清除预设值的缓冲区。

### 方法

```
public static native void glClear(int mask);
```



##### 参数







### GLES20.glClear(int var0)

　　把窗口清除为当前颜色。
```
   glClearColor (0.0, 0.0, 0.0, 0.0);//设置清除颜色
   glClear(GL_COLOR_BUFFER_BIT);//把窗口清除为当前颜色
   glClearDepth(1.0);//指定深度缓冲区中每个像素需要的值
   glClear(GL_DEPTH_BUFFER_BIT);//清除深度缓冲区
```

　　像素颜色在图形硬件中的存储方法有两种：1.RGBA，2.像素索引。

`glClear(glBitField mask);`缓冲区的种类：

| 缓冲区     | 名称                  |
| ---------- | --------------------- |
| 颜色缓冲区 | GL_COLOR_BUFFER_BIT   |
| 深度缓冲区 | GL_DEPTH_BUFFER_BIT   |
| 累积缓冲区 | GL_ACCUM_BUFFER_BIT   |
| 模版缓冲区 | GL_STENCIL_BUFFER_BIT |



### 原文地址

[glClear](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glClear.xml)




