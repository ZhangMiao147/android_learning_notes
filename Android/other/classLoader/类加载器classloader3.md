# 类加载器 ClassLoader ( Android 的八种类加载器 | ClassLoader | BaseDexClassLoader | DexClassLoader )

## Android 类加载器

**Android 中的 类加载器 ClassLoader 继承结构 如下 :**

![](https://img-blog.csdnimg.cn/fcd9efc5182b4b10bc700ee47036b0c3.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

## ClassLoader 抽象类

ClassLoader 是抽象类，是所有类加载器 ClassLoader 的父类。

## BootClassLoader

BootClassLoader 是 ClassLoader 子类，与 Java  虚拟机中的 BootstrapClassLoader 启动类加载器作用相同，用于加载 Java 核心类库。

BootClassLoader 是单例类，全局唯一。

BootClassLoader 是由 Java 代码实现的，这与 JVM 中的启动类加载器不同。

## BaseDevClassLoader

加载 Dex 字节码文件的业务逻辑就是在 BaseDexClassLoader 中实现的。

BaseDexClassLoader 的 3 个子类：

* InMemoryDexClassLoader
* PathClassLoader
* DexClassLoader

没有实现核心业务逻辑，只是调用父类的方法，进行不同类型的操作。

## PathClassLoader

PathClassLoader 是 BaseDexClassLoader 子类，应用中的类，都是由 PathClassLoader 进行加载；

Android 系统相关的类，如四大组件、Android 自带类，都是由 PathClassLoader 加载的。

## DexClassLoader

DexClassLoader 是 BaseDexClassLoader 的子类，该类可以加载任意目录下的 DEX 或 JAR 格式的字节码文件。

热修复就是加载 SD 卡中的 DEX 文件，插件化也是加载 SD 卡中的 APK 文件中的 DEX 文件。

DexClassLoader 在动态字节码技术、热修复、插件化、DEX 加固等方面使用广泛。



## SecureClassLoader

SecureClassloader 继承了 ClassLoader 抽象类，该类主要实现了一些权限相关的功能。

## URLClassLoader

URLClassLoader 是 SecureClassLoader 的子类，其可以使用 url 路径加载 JAR 文件中的类。

## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( Android 的八种类加载器 | ClassLoader | BaseDexClassLoader | DexClassLoader )](https://blog.csdn.net/shulianghan/article/details/121763949)
