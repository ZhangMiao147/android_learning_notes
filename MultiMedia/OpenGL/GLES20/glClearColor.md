## glClearColor

### 功能

　　为颜色缓冲区指定清除值。

### 方法

```java
public static native void glClearColor(float red,float green,float blue,float alpha);
```

##### 参数

　　red、green、blue、alpha：指定颜色缓冲区清除时使用的REBG 值，默认都是 0。

### 描述

　　glClearColor 为 goClear 清除颜色缓冲区时指定 RGBA 值（也就是所有的颜色都会被替换成指定的 RGBA 值）。每个值的取值范围都是 0.0~1.0，超出范围的将被截断。

### 原文地址

[glClearColor](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glClearColor.xml)