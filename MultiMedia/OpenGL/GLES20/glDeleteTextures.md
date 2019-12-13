## glDeleteTexture

### 功能

　　删除命名纹理。 

### 方法

```java
public static native void glDeleteTextures(int n,int[] textures,int offset);

public static native void glDeleteTextures(int n,java.nio.IntBuffer textures);
```

##### 参数

　　n：指定要删除的纹理数量。

　　textures：指定要删除的纹理数组。

### 描述

　　glDeleteTextures 删除由数组纹理元素命名的 n 个纹理。纹理被删除后，它就没有内容或维度了，其名称也可以重用（例如，通过 glGenTextures）。如果当前绑定的纹理被删除，绑定将还原为 0 （默认纹理）。

　　glDeleteTextures 默认忽略 0 和与现有纹理对象不对应的名称。

###  错误

　　如果参数 n 是负数时，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glDeleteTextures](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDeleteTextures.xml)