# glGet

### 功能

　　返回所选参数的值。

### 方法

```
public static native void glGetBooleanv(int pname,java.nio.IntBuffer params);

public static native void glGetFloatv(int pname,java.nio.FloatBuffer params);

public static native void glGetIntegerv(int pname,java.nio.IntBuffer params);
```

#### 参数

　　**pname：**指定要返回的参数值。接受下面列表中的符号常量。

　　**params：**返回指定参数的值。

### 描述

　　这些命令返回 GL 中简单状态变量的值。pname 是一个符号常量，表示要返回的状态变量。params 是一个指向指定类型数组的指针，用来放置返回的数据。

　　如果 params 的类型与请求的状态变量值不同，则执行类型转换。如果调用 glBooleanv，当且仅当它是 0.0 ( 或 0 ) 时，浮点（或整数）值才会转换为 GL_FALSE。否则，它将转换为 GL_TRUE。如果调用 glGetIntegerv，则布尔值将返回 GL_TRUE 或 GL_TRUE，并且大多数浮点值将四舍五入为最接近的整数值。但是，浮点颜色和法线将返回一个线性映射，该映射将 1.0 映射到最正可以表示的整数值，将 -1.0 映射到最负可以表示的整数值。如果调用 glGetFloatv，则布尔值将作为 GL_TRUE 或 GL_FALSE 返回，并且整数值将转换为浮点值。

### pname 接受的符号常量

**GL_ACTIVE_TEXTURE：**params 返回一个表示活动多重纹理单元的值。初始值为 **GL_TEXTURE0**。

**GL_ALIASED_LINE_WIDTH_RANGE：**params 返回两个值，即锯齿线的最小和最大支持宽度。范围必须包括宽度 1。

**GL_ALIASED_POINT_SIZE_RANGE：**params 返回两个值，即别名点支持的最大和最小的尺寸。范围必须包括 1 号。







　　






