# 类加载器 ClassLoader ( 使用 DexClassLoader 动态加载字节码文件 | 准备 DEX 字节码文件 )

## DexClassLoader 构造函数简介

DexClassLoader 构造函数如下：

```java
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

```

**源码路径 :** [/libcore/dalvik/src/main/java/dalvik/system/DexClassLoader.java](http://aospxref.com/android-8.0.0_r36/xref/libcore/dalvik/src/main/java/dalvik/system/DexClassLoader.java)

DexClassLoader 构造函数参数：

* String dexPath：要加载 DEX 或 JAR 格式字节码的路径。
* String optimizedDirectory：优化目录，加载 zip 或 app 文件，需要对 dex 优化生成 odex，优化后的文件需要存放在该优化目录中。
* String librarySearchPath：相关函数库搜索路径，如果没有引用外部函数库，可以设置为 null。
* ClassLoader parent：父节点类加载器。

## 准备 DEX 文件

在 Android Studio 工程中，创建 Module：

![](https://img-blog.csdnimg.cn/2cc2db9f71b8410ca89bd85f66d304c7.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

并在其中，设置一个测试类，之后要使用 DexClassLoader 加载该 DEX 字节码文件。

```java
package com.example.dex_demo;

import android.util.Log;

/**
 * 测试 DEX 字节码加载并执行
 */
public class DexTest {
    public void test(){
        Log.i("DexTest", "DexTest : Hello World!!!");
    }
}
```

选择“菜单栏/Build”编译程序，生成 APK 文件。

![](https://img-blog.csdnimg.cn/361fb9ebcfb3419b9f49cd60efa6a452.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

 编译后的 dex_demo-debug.apk 文件，在当前位置解压，获取其中的 class.dex 字节码文件。

![](https://img-blog.csdnimg.cn/446ab1aaca7348f19a9f93756fffcce9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

将该 DEX 文件拷贝到主应用的 assets 目录下：

![](https://img-blog.csdnimg.cn/f4562c976fd9493cb46399c734ba80e7.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( 使用 DexClassLoader 动态加载字节码文件 | 准备 DEX 字节码文件 )](https://hanshuliang.blog.csdn.net/article/details/121776627)

