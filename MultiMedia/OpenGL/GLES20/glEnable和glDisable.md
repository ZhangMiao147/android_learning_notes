## glEnable 和 glDisable

### 功能

　　启用或禁用服务器 GL 功能。

### 方法

```java
public static native void glEnable(int cap);
public static native void glDisable(int cap);
```

##### 参数

　　指定表示 GL 功能的符号常量。

### 描述

　　glEnable 和 glDiable 启用和禁用各种功能。使用 **glIsEnable** 或 **glGet** 确定任何功能的当前设置。除 GL_DITHER(抖动)外，每个功能的初始值为 GL_FALSE。GL_DITHER 的初始值为 GL_TRUE。

### 错误

　　如果 cap 不是之前列出的值之一，则产生 GL_INVALID_ENUM 错误。

### 原文地址

[]()

　　允许使用顶点颜色数组。
