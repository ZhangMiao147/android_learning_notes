# Android 4.4.4 支持分包后找不到类的问题

## 问题描述
　　打包程序后，点击打开程序时报错：

```java
AndroidRuntime: FATAL EXCEPTION: main
    Process: xxx, PID: 26658
    java.lang.RuntimeException: Unable to get provider com.***.MobProvider: java.lang.ClassNotFoundException: Didn't find class "com.***.MobProvider" on path: DexPathList[[zip file "/data/app/com.xxx-1.apk"],nativeLibraryDirectories=[/data/app-lib/xxx-1, /vendor/lib, /system/lib]]
```

　　说是找不着 MobProvider，检查了在 AndroidManifest.xml 中 provider 的配置，确定类名和 authorities 准确无误。

　　机器的 Android 版本是 4.4.4 ，在 Android 5 以上是没有问题的。

## 问题原因
　　问题的原因是 Android 方法的 id 的数目不能够超过 65535 个，如果超出，就要考虑 dex 分包。

　　之所以存在方法数不能超过 65536 的限制主要有两个原因：

1. dex 文件格式的限制：dex 文件中的方法个数使用 short 类型来存储的，即 2 个字节，最大值为 65535，即单个 dex 文件的方法数上限为 65536。
2. 系统对 dex 文件进行优化操作时分配的缓冲区大小的限制：在 Android 2.x 的系统上缓冲区只有 5MB，android 4.x 为 8MB 或者 16MB，如果方法数量超过缓冲区的大小时，会造成 dexopt 奔溃。

　　在 Android 4.x 的 Android 系统上开启了分包的话，分包的时候系统会随机分配 dex，有可能导致启动时主 dex 中找不到分 dex 中的类，而 5.0 以上系统会自动处理这个问题（自带 multidex），所以 5.0 以及以上的系统设置了分包也不会出现问题。

## 解决问题
1. 在 build.gradle 中开启分包

   ```ruby
       defaultConfig {
           multiDexEnabled true //开启分包
       }
   ```

2. 引入 multidex 库

   ```ruby
   implementation com.android.support:multidex:1.0.3
   ```

3. 在 Application 中加入 multidex 的使用

   ```java
   public class App extends Application {
   	@Override
       protected void attachBaseContext(Context base) {
           super.attachBaseContext(base);
           Log.e("eBag", "--attachBaseContext--");
           MultiDex.install(this);
       }
   }
   ```

   使用 MultiDex 有两种方法：

   1. 继承 MultiDexApplication 。不用在 attachBaseContext() 方法中调用 MultiDex.install(this) 。
   2. 如果有自定义的 Application，不想继承了 MultiDexApplication，就在 attachBaseContext() 方法中调用 MultiDex.install(this)  即可。

   两种方法都是一样的作用，选择其一即可。


