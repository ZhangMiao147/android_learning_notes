# SurfaceView

## SurfaceView 官方说明
　　SurfaceView 是视图（View）的继承类，这个视图里内嵌了一个专门用于绘制的 Surface。你可以控制这个 Surface 的格式和尺寸。SurfaceView 控制这个 Surface 的绘制位置。

　　Surface 是纵深排序（Z-ordered）的，这表明它总在自己所在窗口的后面。SurfaceView 提供了一个可见区域，只有在这个可见区域内的 surface 部分内容才可见，可见区域外的部分不可见。Surface 的排版显示受到视图层级关系的影响，它的兄弟视图结点会在顶端显示。这意味着 surface 的内容会被它的兄弟视图遮挡，这一特性可以用来防止遮盖物（overlays）（例如，文本和按钮等控件）。注意，如果 surface 上面有透明控件，那么它的每次变化都会引起框架重新计算它和顶层控件的透明效果，这会影响性能。

　　你可以通过 SurfaceHolder 接口访问这个 Surface，getHolder() 方法可以得到这个接口。

　　SurfaceView 变得可见时，surface 被创建；SurfaceView 隐藏时，surface 被销毁。这样能节省资源。如果要查看 Surface 被创建和销毁的时机，可以重载 surfaceCreated(SurfaceHolder) 和 SurfaceDestroyed(SurfaceHolder)。

　　SurfaceView 的核心在于提供了两个线程：UI 线程和渲染线程。这里应注意：
　　1>所有 SurfaceView 和 SurfaceHolder.Callback 的方法都应该在 UI 线程里调用，一般来说就是应用程序主线程。渲染线程所要访问的各种变量应该作同步处理。
　　2>由于 Surface 可能被销毁，它只在 SurfaceHolder.Callback.surfaceCreated() 和 SurfaceHolder.Callback.surfaceDestroyed() 之间有效，所以要确保渲染线程访问的是合法有效的 surface。

## SurfaceView 简介
　　SurfaceView 继承自 View，但拥有独立的绘制表面，即它不与其宿主窗口共享同一个绘图表面，可以单独在一个线程进行绘制，并不会占用主线程的资源。这样，绘制就会比较高效。

　　SurfaceView 有两个子类 GLSurfaceView 和 VideoView。

　　SurfaceView 和 View 的区别：
　　1.SurfaceView 允许其他线程更新视图对象（执行绘制方法）而 View 不允许这么做，它只允许 UI 线程更新视图对象。
　　2.SurfaceView 是放在其他最底层的视图层次中，所有其他视图层都在它上面，所以在它之上可以添加一些层，而且它不能是透明的。
　　3.SurfaceView 执行动画的效率比 View 高，而且可以控制帧数，可以频繁地刷新。
　　4.View 在绘图时没有使用双缓冲机制，而 Surface 在底层实现机制中就已经实现了双缓冲机制。
　　5.因为 SurfaceView 的定义和使用比 View 复杂，占用的资源也比较多，除非使用 View 不能完成，才使用 SurfaceView，否则最好使用 View 。

　　如果自定义 View 需要频繁刷新，或者刷新时数据处理量比较大，就可以考虑使用 SurfaceView 来取代 View 了。

　　SurfaceView 的绘制效率非常高，因为 SurefaceView 的窗口刷新的时候不需要重绘应用程序的窗口（android普通窗口的视图绘制机制时一层一层的，任何一个资源素或者是局部的刷新都会导致整个试图结构全部重绘一次，因此效率非常低下）。

　　SurfaceFling 服务是系统服务，负责绘制 Android 应用程序的 UI，SurfaceFling 服务运行在 Android 系统的 System 进程中，它负责管理 Android 系统的帧缓冲区（Frame Buffer），Android 应用程序为了能够将自己的 UI 绘制在系统的帧缓冲区上，它们就必须要与 SurfaceFling 服务进行通信，它们采用 Binder 进程间通信机制来进行通信，每一个 Android 应用程序与 SurfaceFling 服务都有一个连接，这个连接通过一个类型为 Client 的 Binder 对象来描述，有了这些 Binder 代理接口之后，Android 应用程序就可以通知 SurfaceFling 服务来绘制自己的 UI 了。

　　应用程序在通知 SurfaceFling 服务来绘制自己的 UI 的时候，需要将 UI 元数据传递给 SurfaceFling 服务，例如，要绘制 UI 的区域、位置等信息，一个 Android 应用程序可能会有很多个窗口，而每一个窗口都有自己的 UI 元数据，因此，Android 应用程序需要传递给 SurfaceFlinger 服务的 UI 元数据是相当可观的。在这种情况下，通过 Binder 进程间通信机制来在 Android 应用程序与 SurfaceFlinger 服务之间传递 UI 元数据是不合适的，真正使用的是 Android 系统的共享内存机制（Anonymous Shared Memory），在每一个 Android 应用程序与 SurfaceFling 服务之间的连接上加上一块用来传递 UI 元数据的匿名共享内存。

## SurfaceView 的使用模板
　　SurfaceView 使用过程有一套模板代码，大部分的 SurfaceView 都可以套用。

　　3 步走套路：1.创建 SurfaceView；2.初始化 SurfaceView；3.使用 SurfaceView。

　　对 SurfaceView 执行绘制方法就是操作 Surface，使用 SurfaceHolder 来处理 Surface 的生命周期。也就是说 SurfaceView 的生命周期其实就是 Surface 的生命周期，而 SurfaceHolder 保存对 Surface 的引用，所以使用 SurfaceHolder 来处理生命周期的初始化。

## 使用 SurfaceView


#### 使用注意
1. 因为 SurfaceView 允许自定义的线程操作 Surface 对象执行绘制方法，而有可能同时定义多个线程执行绘制，所以当获取 SurfaceHolder 中的 Canvsa 对象时记得加同步操作，避免两个不同的线程同时操作同一个 Canvas 对象，当操作完成后记得调用 SurfaceHolder.unlockCanvasAndPost 方法释放掉 Canvas 锁。
2. 在调用 doDraw 执行绘制时，因为 SurfaceView 的特点，它会保留之前绘制的图形，所以需要先清空掉上一次绘制时留下的图形（View 则不会，它会默认在调用 View.onDraw() 方法时自动清空掉视图里的东西）。
3. 记得在回调方法 onSurfaceDestroyed() 方法里将后台执行绘制的 LoopThread （绘制的线程）关闭，这里是使用 join 方法。这设计到线程如何关闭的问题，多数人建议是通过一个标志位 isRunning 来判断线程是否该停止运行，如果想关闭线程只需要将 isRunning 改为 false 即可，线程会自动执行完 run 方法后退出。



## 查阅资料
1.[Android SurfaceView 入门学习](https://www.cnblogs.com/senior-engineer/p/7867783.html) - 简单概念与手里实现案例
2.[android SurfaceView 详解](https://blog.csdn.net/TuGeLe/article/details/79199119)
3.[Android 中 SurfaceView 的使用详解](https://www.xuebuyuan.com/3236956.html) - 简单概念与使用画圆
4.[SurfaceView - Android SDK | Android Developers](https://www.android-doc.com/reference/android/view/SurfaceView.html) - 官方文档
