## glGenTextures

### 功能

　　生成纹理名称（ID）。

### 方法

```java
public static native void glGenTextures(int n,int[] textures,int offset);

public static native void glGenTextures(int n,java.nio.IntBuffer textures);
```

##### 参数

n：指定要生成的纹理名称（ID）的数量。

textures：指定存储生成的纹理名称（ID）的数组。

### 描述

　　glGenTextures 产生 n 个纹理 ID 存储在 texture 数组中，这个方法并不保证返回的是一个连续的整数集，但是可以保证这些名称（ID）在调用 glGenTextures 之前都没有正在被使用。

　　生成的 textures 是没有维度的，当它们第一次绑定到纹理目标时才被指定维度（见 **glBindTexture**）。

　　通过调用 glGenTextures 返回的纹理名称（ID）不会被后续调用返回，除非首先使用 **glDeleteTextures** 删除它们。

### 错误

　　如果参数 n 是负数，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glGenTextures](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGenTextures.xml)