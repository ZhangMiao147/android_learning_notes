## glGetProgramiv

### 功能

　　从 program 对象返回一个参数的值。

### 方法

```java
public static native void glGetProgramiv(int program,int pname,int[] params,int offset);

public static native void glGetProgramiv(int program,int pname,java.nio.IntBuffer params);
```

##### 参数

program：指定要查询的 program 对象。

pname：指定 program 对象参数。接受的符号名称为 GL_DELETE_STATUS、GL_LINK_STATUS、GL_VALIDATE_STATUS、GL_INFO_LOG_LENGTH、GL_ATTACHED_SHAERS、GL_ACTIVE_ATTRIBUTES、GL_ACTIVE_UNIFORMS、GL_ACTIVE_ATTRIBUTE_MAX_LENGTH、GL_ACTIVE_UNIFORM_MAX_LENGTH。

params：返回请求的对象参数的值。

### 描述

　　glGetProgramiv 以 programs 形式返回指定的 program 对象的参数值。定义了以下参数：

**GL_DELETE_STATUS：**如果 program 当前标记为删除，则 programs 返回 GL_TRUE，否则返回 GL_FALSE。

**GL_LINK_STATUS：**如果 program 的最后一个链接操作成功，则 program 返回 GL_TRUE，否则返回 GL_FALSE。

**GL_VALIDATE_STATUS：**如果 program 的最后一次验证操作成功则返回 GL_TRUE，否则返回 GL_FALSE。

**GL_INFO_LOG_LENGTH：**params 返回 program 信息日志中的字符串，包括空终止字符（即存储信息日志所需的字符缓冲区的大小）。如果程序没有信息日志，则返回值 0 。

**GL_ATTACHED_SHADERS：**params 返回附加到 program 的着色器对象的数量。

**GL_ACTIVE_ATTRIBUTES：**params 返回 program 的活动状态的属性变量的数量。

**GL_ACTIVE_ATTRIBUTE_MAX_LENGTH：**params 返回 program 的最长活动状态的属性名称的长度，包括空终止字符（即存储最长属性名称所需的字符缓冲区的大小）。如果不存在活动属性，则返回 0 。

**GL_ACTIVE_UNIFORMS：**params 返回 program 的活动状态的统一变量的数量。

**GL_ACTIVE_UNIFORM_MAX_LENGTH：**params 返回 program 的最长活动状态的统一变量名称的长度，包括空终止符号（即存储最长统一变量名称所需的字符缓冲区的大小）。如果不存在活动的统一变量，则返回 0。

### 注意

　　如果产生错误，则不会更改 params 的内容。

### 错误

　　如果 pname 不是一个可接受的值，则产生 GL_INVALID_ENUM 错误。

　　如果 program 不是 OpenGl 生成的值，则产生 GL_INVALID_VALUE 错误。

　　如果 program 没有关联 program 对象，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glGetProgramiv](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGetProgramiv.xml)

