# 类加载器 ClassLoader ( 类加载器源码简介 | BaseDexClassLoader | DexClassLoader | PathClassLoader )

## ClassLoader 源码简介

ClassLoader 抽象类中的 private final classLoader parent 成员，用于实现双亲委派机制，所有的 ClassLoader 子类，如 PathCLassLoader、DexClassLoader 等类加载器，都会存在一个 ClassLoader parent 成员，用于表示该类加载器的父节点是哪个类加载器。

BootClassLoader 的 ClassLoader parent 成员是空的，是最顶层的类加载器。

注意该 ClassLoader parent 成员是 final 修饰的，只能进行一次赋值。

ClassLoader 源码参考：

```java
 public abstract class ClassLoader {
      // 委托的父类加载器
      // 注意：VM硬编码此字段的偏移量，因此所有新字段
      // 必须在*之后添加*。
      private final ClassLoader parent;
  
 }
```

**源码路径 :** [/libcore/ojluni/src/main/java/java/lang/ClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/ojluni/src/main/java/java/lang/ClassLoader.java)

## BaseDexClassLoader 源码简介

BaseDexClassLoader 中实现了类加载的核心业务逻辑，这个类很大，很复杂。

BaseDexClassLoader 源码：

```java
  /**
   * 用于各种基于dex的数据库之间的通用功能的基类
   * {@link ClassLoader} 实现.
   */
  public class BaseDexClassLoader extends ClassLoader {
  }

```

**源码路径 :** [/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java)

## DexClassLoader 源码简介

DexClassLoader 继承了 BaseDexClassLoader 类，类中没有实现任何业务逻辑，只是提供了一个构造函数。

```java
/**
 * 从{@code.jar}和{@code.apk}文件加载类的类加载器
 * 包含{@code classes.dex}项。这可用于执行未作为应用程序一部分安装的代码。
 *
 * <p>这个类加载器需要一个应用程序私有的可写目录来缓存优化的类。
 * 使用{@code Context.getCodeCacheDir（）}创建
 * 这样一个目录：<pre>{@code
 * File dexOutputDir = context.getCodeCacheDir();
 * }</pre>
 *
 * <p><strong>不要在外部存储上缓存优化的类。</strong>
 * 外部存储不提供保护您的计算机所需的访问控制
 * 防止代码注入攻击的应用程序。
 */
public class DexClassLoader extends BaseDexClassLoader {
    /**
     * 创建一个{@code-DexClassLoader}来查找解释的和本机的
     * 密码解释类可以在包含的一组DEX文件中找到
     * 在Jar或APK文件中。
     *
     * <p>使用指定的字符分隔路径列表
     * {@code path.separator}系统属性，默认为{@code:}。
     *
     * @param dexPath 包含类和
     * 资源，由{@code File.pathSeparator}分隔，其中
     * Android上的默认值为{@code”：“}
     * @param optimizedDirectory 目录，其中包含优化的dex文件
     * 应该是书面的；不能为{@code null}
     * @param librarySearchPath 包含本机
     * 库，由{@code File.pathSeparator}分隔；可能是
     * {@code null}
     * @param parent 父类加载器
     */
    public DexClassLoader(String dexPath, String optimizedDirectory,
                          String librarySearchPath, ClassLoader parent) {
        super(dexPath, new File(optimizedDirectory), librarySearchPath, parent);
    }
}

```

**源码路径 :** [/libcore/dalvik/src/main/java/dalvik/system/DexClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/dalvik/src/main/java/dalvik/system/DexClassLoader.java)



## PathClassLoader 源码简介

PathClassLoader 继承了 BaseDexClassLoader 类，类中没有实现任何业务逻辑，只是提供了一个构造函数。

````java
/**
 * 提供对列表进行操作的简单{@link ClassLoader}实现
 * 本地文件系统中的文件和目录，但不尝试
 * 从网络加载类。Android将该类用作其系统类
 * 加载器及其应用程序类加载器。
 */
public class PathClassLoader extends BaseDexClassLoader {
    /**
     * 创建一个{@code PathClassLoader}，它在给定的文件列表上运行
     * 和目录。此方法相当于调用
     * {@link#PathClassLoader（String，String，ClassLoader）}与
     * 第二个参数的{@code null}值（请参见此处的说明）。
     * 
     * @param dexPath 包含类和
     * 资源，由{@code File.pathSeparator}分隔，其中
     * Android上的默认值为{@code”：“}
     * @param parent 父类加载器
     */
    public PathClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, null, null, parent);
    }

    /**
     * 创建一个{@code PathClassLoader}，它在两个给定的
     * 文件和目录的列表。第一个列表的条目
     * 应为以下内容之一：
     * 
     * <ul>
     * <li>JAR/ZIP/APK文件，可能包含“classes.dex”文件
     * 以及任意资源。
     * <li>原始“.dex”文件（不在zip文件中）。
     * </ul>
     * 
     * 第二个列表的条目应该是包含
     * 本机库文件。
     * 
     * @param dexPath 包含类和
     * 资源，由{@code File.pathSeparator}分隔，其中
     * Android上的默认值为{@code”：“}
     * @param librarySearchPath 包含本机
     * 库，由{@code File.pathSeparator}分隔；可能是
     * {@code null}
     * @param parent 父类加载器
     */
    public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, null, librarySearchPath, parent);
    }
}

````

**源码路径 :** [/libcore/dalvik/src/main/java/dalvik/system/PathClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/dalvik/src/main/java/dalvik/system/PathClassLoader.java)



## InMemoryDexClassLoader 源码简介

InMemoryDexClassLoader 类加载器继承了 BaseDexClassLoader 类，与 PathClassLoader、DexClassLoader 类似，只提供了构造函数，没有实现业务逻辑。

InMemoryDexClassLoader 主要用于加载内存中的 Dex 字节码文件，在 Android 8.0 中加入到系统中。

InMemoryDexClassLoader 源码：

```java
/**
 * 一个{@link ClassLoader}实现，从
 * 包含DEX文件的缓冲区。这可用于执行以下代码：
 * 尚未写入本地文件系统。
 */
public final class InMemoryDexClassLoader extends BaseDexClassLoader {
    /**
     * 使用给定的DEX缓冲区创建内存中的DEX类装入器。
     * 
     * @param dexBuffers 包含之间的DEX文件的缓冲区数组
     * <tt>buffer.position（）</tt>和<tt>buffer.limit（）</tt>。
     * @param parent 委托的父类加载器。
     * @隐藏
     */
    public InMemoryDexClassLoader(ByteBuffer[] dexBuffers, ClassLoader parent) {
        super(dexBuffers, parent);
    }

    /**
     * 创建一个新的内存中DEX类装入器。
     * 
     * @param dexBuffer 缓冲区，包含之间的DEX文件内容
     * <tt>buffer.position（）</tt>和<tt>buffer.limit（）</tt>。
     * @param parent 委托的父类加载器。
     */
    public InMemoryDexClassLoader(ByteBuffer dexBuffer, ClassLoader parent) {
        this(new ByteBuffer[] { dexBuffer }, parent);
    }
}

```

**源码路径 :** [/libcore/dalvik/src/main/java/dalvik/system/InMemoryDexClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/dalvik/src/main/java/dalvik/system/InMemoryDexClassLoader.java)



## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( 类加载器源码简介 | BaseDexClassLoader | DexClassLoader | PathClassLoader )](https://hanshuliang.blog.csdn.net/article/details/121773414)

