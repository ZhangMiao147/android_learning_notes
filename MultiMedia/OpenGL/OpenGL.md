# OpenGL

## OpenGL 简述
　　Android 使用 OpenGL 图形库支持高性能的 2D 和 3D 图形，特别是 OpenGL ES API。OpenGL 是一个跨平台的图形 API，它为 3D 图形处理硬件指定了标准的软件接口。 Opengl ES 是面向嵌入式设备的 OpenGL 规范的一种风格。Android 支持多种版本的 OpenGL ES API:

　　OpenGL SE 1.0 和 1.1 - Android 1.0 以及更高版本支持此 API 规范。

　　OpenGL ES 2.0 - Android 2.2(API level 8) 以及更高版本支持此 API 规范。

　　OpenGL ES 3.0 - Android 4.3（API level 18）以及更高支持此 API 规范。

　　OpenGL ES 3.1 - Android 5.0(API level 21)以及更高版本支持此 API 规范。

　　注意：在设备上支持 OpenGL ES 3.0 API 需要设备制造商提供图形管道的实现。运行 Android 4.3 或更低版本的设备可能不支持 OpenGL ES 3.0 API。

　　在 Android 应用程序中使用 OpenGL ，有两个基础类：GLSurfaceView 和 GLSurfaceView.Renderer，用于创建和操作图形。

**GLSurfaceView**

　　GLSurfaceView 是一个 View，在使用 OpenGl API 时调用用于绘制和操作对象，和 SurfaceView 类似。可以通过创建 GLSurfaceView 并且为它添加 Renderer 来使用它。但是，如果箱套捕获触摸事件，则需要扩展 GLSurfaceView 类实现触摸监听器。

**GLSurfaceView.Renderer**

　　此接口定义了在 GLSurfaceView 绘制图形所需要的方法。必须将该接口的实现类作为单独的类，并调用 GLSurfaceView.setRenderer() 方法将其附加给 GLSurfaceView 实例上。

　　GLSurfaceView.Renderer 接口要求实现以下方法：

* onSurfaceCreated()：系统只会在创建 GLSurfaceView 的时候调用此方法一次。使用此方法执行只需要执行一次的操作，例如设置 OpenGL 的环境参数或者初始化 OpenGL 图形对象。
* onDrawFrame()：系统在 GLSurfaceView 的每一次重绘时调用此方法，使用此方法作为绘制（和重绘）图形对象的主要执行点。
* onSurfaceChanged()：系统在 GLSurfaceView 集合变化时调用此方法，包括改变 GLSurfaceView 的大小，或者设备屏幕方向变化。举例，当设备屏幕方向从纵向改变为横向是系统会调用此方法。使用此方法去响应 GLSurfaceView 容器的变化。


## 使用

#### 声明 OpenGL 要求

##### OpenGL ES 版本要求

　　如果应用使用在并非所有设备都支持 OpenGL 功能，就需要在 AndroidManifest.xml 文件中声明该要求，下面是最常见的 OpenGL 清单声明：

　　对于 OpenGL ES 2.0：

```
<!-- Tell the system this app requires OpenGL ES 2.0. -->
<uses-feature android:glEsVersion="0x00020000" android:required="true" />

```
　　添加此声明后，如果设备不支持 OpenGL ES 2.0 ，Google Play 将拒绝你的应用安装到设备上。

　　如果你的应用程序需要支持 OpenGL ES 3.0，则需要在清单中添加如下：

　　对于 OpenGL ES 3.0：
```
<!-- Tell the system this app requires OpenGL ES 3.0. -->
<uses-feature android:glEsVersion="0x00030000" android:required="true" />

```

　　对于 OpenGL ES 3.1：
```
<!-- Tell the system this app requires OpenGL ES 3.1. -->
<uses-feature android:glEsVersion="0x00030001" android:required="true" />

```

　　注意：OpenGL 3.x API 是向后兼容 2.0 API，这意味着你可以更加灵活的在应用程序中使用 OpenGL ES。在清单中声明 OpenGL ES 2.0 API 为必需项，将 2.0 版本作为默认版本，在运行时检查 3.x API 的可用性，然后在设备支持的情况下使用 OpenGL ES 3.x 功能。

##### 纹理压缩要求
　　如果应用程序使用纹理压缩格式，则必须使用 < supports-gl-texture > 在清单文件中声明应用程序支持的格式。

　　在清单中声明纹理压缩会要求


## 查阅资料
1. [Learn OpenGL](learnopengl.com)
2. [Android OpenGL 的基本使用](https://www.jianshu.com/p/6581703e1d98)
3. [Android OpenGL 顶点及绘制基础知识](https://www.jianshu.com/p/0701d9c7f01b)
4. [Android openGL 开发详解（1） - 绘制简单图形](https://blog.csdn.net/qq_32175491/article/details/79091647#4-glsurfaceviewrenderer%E6%98%AF%E4%BB%80%E4%B9%88glsurfaceviewrenderer%E7%9A%84%E4%BD%9C%E7%94%A8glsurfaceviewrenderer%E7%9A%84%E7%94%A8%E6%B3%95)
5. [SurfaceView、TextureView、GLSurfaceView 显示相机预览](https://blog.csdn.net/qq_32175491/article/details/79755424)
6. [通俗易懂的Android OpenGL入门 ](http://dy.163.com/v2/article/detail/E9TAA40N0511IFOV.html)
7. [android opengl教程](https://blog.csdn.net/lf12345678910/article/details/73832423)
8. [Android 开发之 OpenGL、OpenGL ES 的概念和实例讲解](https://www.2cto.com/kf/201806/752471.html)
8. [android opengl | Android Developers](https://developer.android.google.cn/reference/android/opengl/package-summary.html)

