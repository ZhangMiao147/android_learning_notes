# Java 注解

　　Java 注解用于为 Java 代码提供元数据。作为元数据，注解不直接影响你的代码执行，但也有一些类型的注解实际上可以用于这一目的。

　　Java 注解是从 Java5 开始添加到 Java 的。

## 1. 注解的定义

　　日常开发中新建 Java 类，使用 class、interface 比较多，而注解和它们一样，也是一种类的类型，它是用的修饰符为 @interface。

## 2. 元注解

　　元注解顾名思义可以理解为注解的注解，它是作用在注解中，方便使用注解实现想要的功能。元注解分别有 @Retension、@Target、@Document、@Inherited 和 @Repeatable（JDK 1.8 加入）五种。

### 2.1. @Retention

　　Retention 英文意思有保留、保持的意思，它表示注解存在阶段是保留在源码（编译期）、字节码（类加载）或者运行期（JVM 中运行）。在 @Retention 注解中使用枚举 RententionPolicy 来表示注解保留时期。

　　@Retention(RetentionPolicy.SOURCE)，注解仅存在于源码中，在 class 字节码文件中不包含 @Retention(RetentionPolicy.CLASS)，默认的保留策略，注解会在 class 字节码文件中存在，但运行时无法获得 @Rentention(RetentionPolity.RUNTIME)，注解会在 class 字节码文件中存在，在运行时可以通过反射获得到。








## 参考文章

1. [Java 注解完全解析](https://www.jianshu.com/p/9471d6bcf4cf)
2. [Java 注解 - 最通俗易懂的讲解](https://blog.csdn.net/qq1404510094/article/details/80577555)
3. [Java 注解的基本原理](https://www.cnblogs.com/yangming1996/p/9295168.html)

