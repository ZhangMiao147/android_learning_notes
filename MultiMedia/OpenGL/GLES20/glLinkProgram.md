## glLinkProgram

### 功能

　　连接一个 program 对象。

### 方法

```java
public static native void glLinkProgram(int program);
```

##### 参数

program：指定要连接的 program 对象的句柄。

### 描述

　　glLinkProgram 连接 program 指定的 program 对象。附加到 program 的类型为 GL_VERTEX)SHADER 的着色器对象用于创建将在可编程顶点处理器上运行的可执行文件。附加到 program 的类型为 GL_FRAGMENT_SHADER 的着色器对象用于创建将在可编程片段处理器上运行的可执行文件。

　　连接操作的状态将存储为 program 对象状态的一部分。如果程序对象连接时没有错误并且可以使用，则此值将设置为 GL_TRUE，否则将设置为 GL_FALSE。可以通过使用参数 program 和 GL_LINK_STATUS 调用 **glGetShaderiv** 来查询它。

　　作为连接操作成功的结果，属于 program 的所有活动用户定义的统一变量的值将被初始化为 0 ，并且将为每个 program 对象的活动统一变量分配一个可以通过调用 **glGetUniformLocation** 来查询的位置。此外，任何尚未绑定到通用顶点属性索引的活动用户定义属性变量此时也将绑定到一个索引。

　　由于 OpenGL ES 着色语言规范中指定的多种原因，program 对象的连接可能会失败。以下列出了导致连接错误的一些条件：

* 程序对象中不同时存在顶点着色器和片段着色器。
* 已超出支持的活动属性变量数。
* 已超出统一变量的存储限制。
* 已超出实现支持的活动统一变量的数量。
* 顶点着色器或片段着色器缺失 main 函数。
* 片段着色器中实际使用的是可变变量在顶点着色器中的声明方式不同（或根本未声明）。
* 对函数或变量名的引用尚未解析。
* 使用两种不同类型或两种不同的初始值声明全局共享变量。
* 一个或多个附加的着色器对象尚未成功编译（通过 **glCompileSahder**）或未成功加载预编译的着色器二进制文件（通过 **glShaderBinary**）。
* 绑定通用属性矩阵导致矩阵的某些行超出允许的最大值 GL_MAX_VALUE_ATTRIBS。
* 没有足够的连续顶点属性槽来绑定属性矩阵。

　　成功连接 program 对象后，可以通过调用 **glUseProgram** 使 program 对象成为当前状态的一部分。无论连接操作是否成功，program 对象的信息日志都将被覆盖。可以通过调用 **glGetProgramInfoLog** 来检索信息日志。

　　如果连接操作成功并且由于先前调用了 **glUseProgram** ，而指定的 program 对象当前已被使用，glLinkProgram 将配置生成的可执行文件作为当前呈现状态的一部分。如果当前正在使用的 program 对象重新连接失败，其连接状态将设置为 GL_FALSE，但可执行文件和关联状态将保持为当前状态的一部分，直到后续调用 glUseProgram 将其从使用中删除。从使用中删除后，在成功重新连接之前，它不能成为当前状态的一部分。

　　连接操作完成时，program 对象的信息日志就被更新了，而且程序也被生成了。在连接操作之后，应用程序可以自由修改附加的着色器对象、编译附加的着色器对象、分离着色器对象、删除着色器对象以及附加其他着色器对象。这些操作都不会影响信息日志或作为 program 对象的一份子的 program。

### 注意

　　如果连接操作不成功，则关于程序上先前连接操作的任何信息都将丢失（即失败的连接不会还原旧的程序状态）。即使连接操作失败，仍然可以从程序中检索某些信息。例如，**glGetActiveAttrib** 和 **glGetActiveUniform**。

### 错误

　　如果 program 不是由 OpenGL 生成的值，则产生 GL_INVALID_VALUE 错误。

　　如果 program 参数传的不是 program 对象，则产生 GL_INVALID_OPERATION 错误。

### 原文地址

[glLinkProgram](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glLinkProgram.xml)