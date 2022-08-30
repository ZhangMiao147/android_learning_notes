# 类加载器 ClassLoader ( 使用 DexClassLoader 动态加载字节码文件 | 拷贝 DEX 文件到内置存储 | 加载并执行 DEX 字节码文件 )

## 拷贝 Assets 目录下的 classes.dex 字节码文件到内置存储区

在上篇中，准备了 classes.dex 字节码文件，将字节码文件拷贝到了 app\src\main\assets\classes.dex 目录中。

解析字节码文件时，首先将该 DEX 字节码文件从  app\src\main\assets\classes.dex 路径拷贝到 /data/user/0/com.example.classloader_demo/files/classes.dex 内置存储空间中。

下面的代码，是拷贝字节码文件的代码：

代码示例：

```java
    /**
     * 将 app\src\main\assets\classes.dex 文件 ,
     * 拷贝到 /data/user/0/com.example.classloader_demo/files/classes.dex 位置
     */
    private String copyFile() {
        // DEX 文件
        File dexFile = new File(getFilesDir(), "classes.dex");
        // DEX 文件路径
        String dexPath = dexFile.getAbsolutePath();

        Log.i(TAG, "开始拷贝文件 dexPath : " + dexPath);

        // 如果之前已经加载过 , 则退出
        if (dexFile.exists()) {
            Log.i(TAG, "文件已经拷贝 , 退出");
            return dexPath;
        }

        try {
            InputStream inputStream = getAssets().open("classes.dex");
            FileOutputStream fileOutputStream = new FileOutputStream(dexPath);

            byte[] buffer = new byte[1024 * 4];
            int readLen = 0;
            while ( (readLen = inputStream.read(buffer)) != -1 ) {
                fileOutputStream.write(buffer, 0, readLen);
            }

            inputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.i("HSL", "文件拷贝完毕");
        }
        return dexPath;
    }

```

## 加载 DEX 文件并执行其中的方法

使用 DexClassLoader 加载字节码文件时，要准备几个参数：

* DEX 字节码文件路径：必须指定准确的 DEX 字节码文件目录。

```
/data/user/0/com.example.classloader_demo/files/classes.dex
```

* 优化目录：设置一个空的文件目录即可

```java
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
```

* 依赖库目录：可以为空，也可以设置一个文件目录

```java
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");
```

* 父节点类加载器：直接获取当前类的父类类加载器节点。

```java
		context.getClassLoader()
```

从字节码文件中，加载的类是 Class 对象，通过反射调用其方法即可。

代码示例：

```java
    /**
     * 测试调用 Dex 字节码文件中的方法
     * @param context
     * @param dexFilePath
     */
    private void testDex(Context context, String dexFilePath) {
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");

        // 初始化 DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexFilePath,                    // Dex 字节码文件路径
                optFile.getAbsolutePath(),      // 优化目录
                libFile.getAbsolutePath(),      // 依赖库目录
                context.getClassLoader()        // 父节点类加载器
        );

        // 加载 com.example.dex_demo.DexTest 类
        // 该类中有可执行方法 test()
        Class<?> clazz = null;
        try {
            clazz = dexClassLoader.loadClass("com.example.dex_demo.DexTest");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 获取 com.example.dex_demo.DexTest 类 中的 test() 方法
        if (clazz != null) {
            try {
                // 获取 test 方法
                Method method = clazz.getDeclaredMethod("test");
                // 获取 Object 对象
                Object object = clazz.newInstance();
                // 调用 test() 方法
                method.invoke(object);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

```

## MainActivity 及执行结果

完整代码示例：

```java
package com.example.classloader_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * Dex 文件路径
     */
    private String mDexPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 打印类加载器及父节点
        classloaderLog();

        // 拷贝 dex 文件
        mDexPath = copyFile();

        // 测试 DEX 文件中的方法
        testDex(this, mDexPath);
    }

    /**
     * 打印当前的类加载器及父节点
     */
    private void classloaderLog(){
        // 获取当前 Activity 的 类加载器 ClassLoader
        ClassLoader classLoader = MainActivity.class.getClassLoader();

        // 打印当前 Activity 的 ClassLoader 类加载器
        Log.i(TAG, "MainActivity ClassLoader : " + classLoader);

        // 获取 类加载器 父类
        ClassLoader parentClassLoader = classLoader.getParent();

        // 打印当前 Activity 的 ClassLoader 类加载器 的父类
        Log.i(TAG, "MainActivity Parent ClassLoader : " + parentClassLoader);
    }

    /**
     * 将 app\src\main\assets\classes.dex 文件 ,
     * 拷贝到 /data/user/0/com.example.classloader_demo/files/classes.dex 位置
     */
    private String copyFile() {
        // DEX 文件
        File dexFile = new File(getFilesDir(), "classes.dex");
        // DEX 文件路径
        String dexPath = dexFile.getAbsolutePath();

        Log.i(TAG, "开始拷贝文件 dexPath : " + dexPath);

        // 如果之前已经加载过 , 则退出
        if (dexFile.exists()) {
            Log.i(TAG, "文件已经拷贝 , 退出");
            return dexPath;
        }

        try {
            InputStream inputStream = getAssets().open("classes.dex");
            FileOutputStream fileOutputStream = new FileOutputStream(dexPath);

            byte[] buffer = new byte[1024 * 4];
            int readLen = 0;
            while ( (readLen = inputStream.read(buffer)) != -1 ) {
                fileOutputStream.write(buffer, 0, readLen);
            }

            inputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.i("HSL", "文件拷贝完毕");
        }
        return dexPath;
    }

    /**
     * 测试调用 Dex 字节码文件中的方法
     * @param context
     * @param dexFilePath
     */
    private void testDex(Context context, String dexFilePath) {
        // 优化目录
        File optFile = new File(getFilesDir(), "opt_dex");
        // 依赖库目录 , 用于存放 so 文件
        File libFile = new File(getFilesDir(), "lib_path");

        // 初始化 DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexFilePath,                    // Dex 字节码文件路径
                optFile.getAbsolutePath(),      // 优化目录
                libFile.getAbsolutePath(),      // 依赖库目录
                context.getClassLoader()        // 父节点类加载器
        );

        // 加载 com.example.dex_demo.DexTest 类
        // 该类中有可执行方法 test()
        Class<?> clazz = null;
        try {
            clazz = dexClassLoader.loadClass("com.example.dex_demo.DexTest");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 获取 com.example.dex_demo.DexTest 类 中的 test() 方法
        if (clazz != null) {
            try {
                // 获取 test 方法
                Method method = clazz.getDeclaredMethod("test");
                // 获取 Object 对象
                Object object = clazz.newInstance();
                // 调用 test() 方法
                method.invoke(object);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}

```

打印日志：

```java
2021-12-10 13:25:22.915 31065-31065/com.example.classloader_demo I/MainActivity: MainActivity ClassLoader : dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.example.classloader_demo-kqe1gP2jfD1FRwkny0hX1w==/base.apk"],nativeLibraryDirectories=[/data/app/com.example.classloader_demo-kqe1gP2jfD1FRwkny0hX1w==/lib/arm64, /system/lib64]]]
2021-12-10 13:25:22.915 31065-31065/com.example.classloader_demo I/MainActivity: MainActivity Parent ClassLoader : java.lang.BootClassLoader@6457c5
2021-12-10 13:25:22.916 31065-31065/com.example.classloader_demo I/MainActivity: 开始拷贝文件 dexPath : /data/user/0/com.example.classloader_demo/files/classes.dex
2021-12-10 13:25:22.980 31065-31065/com.example.classloader_demo I/HSL: 文件拷贝完毕
2021-12-10 13:25:23.951 31065-31065/com.example.classloader_demo I/lassloader_dem: The ClassLoaderContext is a special shared library.
2021-12-10 13:25:23.955 31065-31065/com.example.classloader_demo I/DexTest: DexTest : Hello World!!!

```

![](https://img-blog.csdnimg.cn/ddacc70663c149a9b33dffb69fbc1fe0.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

拷贝到 /data/user/0/com.example.classloader_demo/files/classes.dex 位置的文件，以及在 /data/user/0/com.example.classloader_demo/files/opt/ 目录生成的字节码优化相关目录。

![](https://img-blog.csdnimg.cn/9890d15103cf496885ef91ee9e83d146.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( 使用 DexClassLoader 动态加载字节码文件 | 拷贝 DEX 文件到内置存储 | 加载并执行 DEX 字节码文件 )](https://hanshuliang.blog.csdn.net/article/details/121854179)

