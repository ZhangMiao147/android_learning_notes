## glEnableVertexAttribArray与glDisableVertexAttribArray

### 功能

　　启用或者禁用通用顶点属性数组。

### 方法

```java
public static native void glEnableVertexAttribArray(int index);

public static native void glDisableVertexAttribArray(int index);
```

##### 参数

index：指定要启用或禁用的通用顶点属性的索引。

### 描述

　　glEnableVertexAttribArray 启用 index 指定的通用顶点属性数组。 glDisableVertexAttribArray 禁用 index 指定的通用顶点属性数组。默认情况下，禁用所有客户端功能，包括所有通用顶点属性数组。如果启用，当调用顶点数组命令（如 **glDrawArrays** 或 **glDrawElements**）时，将访问通用顶点属性数组中的值并用于渲染。

### 错误

　　如果 index 大于等于 GL_MAX_VERTEX_ATTRIBS 时，则产生 GL_INVALID_VALUE 错误。

### 原文地址

[glEnableVertexAttribArray](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glEnableVertexAttribArray.xml)

