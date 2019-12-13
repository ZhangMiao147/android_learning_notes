## glDrawArrys

### 功能

　　从数组数据中渲染图元。

### 方法

```java
public static native void glDrawArrays(int mode,int first,int count);
```

##### 参数

mode：指定要渲染的图元类型。接受符号常量 GL_POINTS、GL_LINE_STRIP、GL_LINE_LOOP、GL_LINES、GL_TRIANGLE_STRIP、GL_TRIANGLE_FAN 和 GL_TRIANGLES。

first：指定启用数组中的起始索引。

count：指定要渲染的索引数。

### 描述

　　glDrawArrays 指定了几个子程序调用的几何图元。不用调用 GL 过程来传递每个单独的顶点属性，可以使用 **glVertexAttribPointer** 预先指定单独的顶点、法线和颜色数组，并使用它们通过单次调用 glDrawArrays 来构造图元序列。

　　当调用 glDrawArrays 时，它从元素 first 开始，按照计数顺序使用数组每一个可用的元素来构造几何图元序列，从元素 first 开始。mode 指定构造什么类型的图元以及数组元素如何构造这些图元。

　　要启用和禁用通用顶点属性数组，请调用 **glEnableVertexAttribArray** 和 **glDisableVertexAttribArray**。

### 注意

　　使用 **glUseProgram** 设置的当前程序对象无效，则渲染结果未定义，但是，这种情况不会产生错误。

### 错误

　　mode 不是一个可接受的值，则产生 GL_INVALID_ENUM 错误。

　　count 是负值，则产生 GL_INVALID_VALUE 错误。

　　如果当前绑定的帧缓冲区未完成（即 glCheckFramebufferStatus 的返回值不是 GL_FRAMEBUFFER_COMPLETE），则产生 GL_INVALID_FRAMEBUFFER_OPERATION 错误。

### 原文地址

[glDrawArrays](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glDrawArrays.xml)