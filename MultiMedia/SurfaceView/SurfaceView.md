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


　　双缓冲：SurfaceView 在更新视图时用了两个Canvas，一张 frontCanvas 和一张 backCanvas，每次实际显示的是 frontCanvas，backCanvas 存储的是上一次更改前的视图，当使用 lockCanvas（） 获取画布时，得到的实际上是 backCanvas 而不是正在显示的 frontCanvas，当你在获取到的 backCanvas 上绘制完成后，再使用 unlockCanvasAndPost(canvas) 提交 backCanvas 视图，那么这张 backCanvas 将替换正在显示的 frontCanvas 被显示出来，原来的 frontCanvas 将切换到后台作为 backCanvas，这样做的好处是在绘制期间不会出现黑屏。

　　如果自定义 View 需要频繁刷新，或者刷新时数据处理量比较大，就可以考虑使用 SurfaceView 来取代 View 了。

　　SurfaceView 的绘制效率非常高，因为 SurefaceView 的窗口刷新的时候不需要重绘应用程序的窗口（android普通窗口的视图绘制机制时一层一层的，任何一个资源素或者是局部的刷新都会导致整个试图结构全部重绘一次，因此效率非常低下）。

#### SurfaceView 绘制的原理

　　SurfaceFling 服务是系统服务，负责绘制 Android 应用程序的 UI，SurfaceFling 服务运行在 Android 系统的 System 进程中，它负责管理 Android 系统的帧缓冲区（Frame Buffer），Android 应用程序为了能够将自己的 UI 绘制在系统的帧缓冲区上，它们就必须要与 SurfaceFling 服务进行通信，它们采用 Binder 进程间通信机制来进行通信，每一个 Android 应用程序与 SurfaceFling 服务都有一个连接，这个连接通过一个类型为 Client 的 Binder 对象来描述，有了这些 Binder 代理接口之后，Android 应用程序就可以通知 SurfaceFling 服务来绘制自己的 UI 了。

　　应用程序在通知 SurfaceFling 服务来绘制自己的 UI 的时候，需要将 UI 元数据传递给 SurfaceFling 服务，例如，要绘制 UI 的区域、位置等信息，一个 Android 应用程序可能会有很多个窗口，而每一个窗口都有自己的 UI 元数据，因此，Android 应用程序需要传递给 SurfaceFlinger 服务的 UI 元数据是相当可观的。在这种情况下，通过 Binder 进程间通信机制来在 Android 应用程序与 SurfaceFlinger 服务之间传递 UI 元数据是不合适的，真正使用的是 Android 系统的共享内存机制（Anonymous Shared Memory），在每一个 Android 应用程序与 SurfaceFling 服务之间的连接上加上一块用来传递 UI 元数据的匿名共享内存。

　　这个共享内存是通过 SharedClient 来描述的，一个 SharedClient 对应一个应用程序，在每一个 SharedClient 里面，有很多歌 SharedBufferStack 共享缓冲区堆栈，是 Android 应用程序和 SurfaceFlinger 服务共享内存的地方，这个堆栈的内容是用来描述 UI 元数据的缓冲区，每一个 SharedBufferStack 在应用程序端都对应一个 Surface，在 SurfaceFlinger 端对应一个 Layer，而一个应用程序可能包含有多个 Surface，所以每一个 SharedClient 里面包含的是一系列 SharedBufferStack 而不是单个的 SharedBufferStack。

　　Surface 是原始图像缓冲区 SharedBufferStack 的一个句柄，通过 Surface 就可以获取原始图像缓冲区中的 GraphicBuffer，Surface 可以理解为：它是共享内存中一块区域的一个句柄，当得到一个 Surface 对象时，同事会得到一个 Canvas(画布)对象，Canvas 的方法大多数是设置画布的大小、形状、画布背景颜色等等，要想在画布上面画画，一般要与 Paint 对象结合使用，Paint 就是画笔的风格、颜料的色彩之类的，所以得到了 Surface 这个句柄就可以得到其中的 Canvas，还有原生缓冲器的 GraphicBuffer，可以通过 Canvas 往 GraphicBuffer 填充绘制的图形数据，之后 GraphicBuffer 的数据会被 SurfaceFling 服务处理绘制到屏幕上。

　　Canvas 与 Surface 的区别：Canvas 是由 Surface 产生的给 View 绘制用的画布，ViewGroup 会把自己的 Canvas 拆分成子 View，View 会在 onDraw 方法里将图形数据绘制在它获得的 Canvas 上，一个应用窗口对应一个 Surface，也就是窗口最顶层的 View（通常是 DecorView）对应一个 Surface，这个 Surface 是 ViewRoot 是一个成员变量，这个 Surface 在屏幕窗口建立时会被创建，SurfaceFling 服务会负责将各个应用窗口的 Surface 进行合成，然后绘制到屏幕上，最终屏幕上显示的 Ciew 都是通过 Surface 产生的 Canvas 把内容绘制到 GraphicBuffer 缓冲区中的，SurfaceFlinger 把 GraphicBuffer 缓冲区中的内容绘制在屏幕上然后才能被看到。

　　当 Android 应用程序需要更新一个 Surface 的时候，它就会找到与它所对应的 SharedBufferStack，并且从它的空闲缓冲区列表的尾部取出一个空闲的 Buffer，接下来 Android 应用程序就请求 SurfaceFlinger 服务为这个 Buffer 分配一个图形缓冲区 GraphicBuffer，分配好以后将这个图形缓冲区 GraphicBuffer 返回给应用程序访问，应用程序得到了图形缓冲区 GraphicBuffer 之后，就可以利用 Surface 的 Canvas 往里面绘制写入 UI 数据，写完之后，就将与 GraphicBuffer 所对应的缓冲区 Buffer，插入到对应的 SharedBufferStack 的缓冲区列表的头部去，这一步完成之后，应用程序就通知 SurfaceFling 服务去绘制 GraphicBuffer 的内容了。

　　由于 SharedBufferStack 是在应用程序和 SurfaceFling 服务之间共享的，应用程序关心的是它里面可以写入数据的空闲缓冲区列表，而 SurfaceFlinger 服务关心的是它里面的已经使用了的缓冲区列表，保存在 SharedBufferStack 中的已经使用了的缓冲区其实就是在排队等待渲染的数据。

　　可以将 Surface 理解为一个绘图表面，在 Android 应用程序这一侧，每一个绘图表面都使用一个 Surface 对象来描述，Android 应用程序负责往这个绘图表面填内容，在 SurfaceFlinger 服务这一侧，每一个窗口的绘图表面使用 Layer 类来描述，而 SurfaceFlinger 服务负责将这个绘图表面的内容取出来，并且渲染在显示屏上，有了 Surface 之后，Android 应用程序就可以在上面绘制自己的 UI 了，接着再请求 SurfaceFling 服务将这个已经绘制好了 UI 的 Surface 渲染到设备显示屏上去。

　　Android 系统每隔 16 ms 发出 VSYNC 信号，触发 GPU 对 UI 进行渲染，如果每次渲染都成功结束，就能够达到流畅的画面锁需要的 60 fps，这意味着程序的操作都必须在 16ms 内完成，如果某个操作花费时间是 24ms，系统在得到 VSYNC 信号的时候就无法进行正常渲染，用户在 32ms 内看到的回事同一帧画面，这样就发生了丢帧（卡顿现象）。

　　将更新分为两类：
1. 被动更新：比如棋类，用 View 就好，因为画面的更新是依赖于 onTouch 来更新，可以使用使用 invalidate。
2. 主动更新：比如一个人在一直跑动，这就需要一个单独的 Thread 不停的重绘人的状态，避免阻塞 main UI thread。

　　SurfaceView 从 API Level 1 时就有，继承自 View，拥有 View 的特性，SurfaceView 可以嵌入到 View 结构树中，因此它能够叠加在其他的视图中，它拥有一个专门绘制的 Surface，它的目的是给应用窗口提供一个额外的 Surface。

　　每个 Activity 包含多个 View 会组成的 View hierachy 树形结构窗口，但是只有最顶层的根布局 DecorView，才拥有一个 Surface 用来展示窗口内的所有内容，才是对 SurfaceFlinger 可见的，才在 SurfaceFlinger 中有一个对应的 Layer，这个 Surface 是根布局 ViewRootImpl 的一个成员变量。

　　每个 SurfaceView 也有一个自己的绘图表面 Surface，内部也有一个 Surface 成员变量，区别于它的宿主窗口的绘图表面，在 SurfaceFlinger 服务中也对应有一个独立的 Layer，SurefaceView 可以控制它的 Surface 的格式和尺寸，以及 Surface 的绘制位置。

　　SurfaceView 里面镶嵌的 Surface 是在包含 SurfaceView 的宿主 Activity 窗口（顶层视图对应的 Surface）后面，用来描述 SurfaceView 的 Layer 的 Z 轴位置是小于用来描述其宿主 Activity 窗口的 Layer 的 Z 轴位置，这样 SurfaceView 的 Layer 就被挡住看不见了，SurfaceView 提供了一个可见区域，只有在这个可见区域内的 surface 部分内容才可见，就好像 SurfaceView 会在宿主 Activity 窗口上面挖一个 “洞”出来，以便它的 UI 可以漏出来对用户可见，实际上，SurfaceView 只不过是在其宿主 Activity 窗口上设置了一块透明区域。

　　虽然布局中的 SurfaceView 在 View hierachy 树结构中，但它的 Surface 与 宿主窗口是分离的，因此这个 Surface 不在 View hierachy 中，它的显示也不受 View 的属性控制，所以和普通的 View 不同的地方是不能执行 Transition、Rotation、Scale 等转换，不能进行 Alpha 透明度运算，一些 View 中的特性也无法使用。

　　SurfaceView 类的成员变量 mRequestedType 描述的是 Surfacaview 的绘图表面 Surface 的类型，一般来说，它的值可能等于 SURFACE_TYPE_NORMAL 或者 SURFACE_TYPE_PUSH_BUFFERS，当一个 SurfaceView 的绘图表面的类型等于 SURFACE_TYPE_NORMAL 的时候，就表示该 SurfaceView 的绘图表面使用的内存是一块普通的内存，一般来说，这块内存是由 SurfaceFlinger 服务来分配的，我们可以在应用程序内部自由地访问它，即可以在它上面填充任意的 UI 数据，然后交给 SurfaceFlinger 服务来合成，并且显示在屏幕上，在这种情况下，在 SurfaceFlinger 服务一端使用一个 Layer 对象来描述该 SurfaceView 的绘图表面。

　　当一个 SurfaceView 的绘图表面的类型等于 SURFACE_TYPE_PUSH_BUFFERS 的时候，就表示该 SurfaceView 的绘图表面所使用的内存不是由 SurfaceFlinger 服务分配的，我们不能够在应用程序内部对它进行操作，所以不能调用 lockCanvas 来获取 Canvas 对象进行了，例如当一个 SurfaceView 是用来显示摄像头预览或者视频播放的时候，我们就会将它的绘图表面的类型设置为 SURFACE_TYPE_PUSH_BUFFERS，这样摄像头服务或者视频播放服务就会为该 SurfaceView 绘图表面创建一块内存，并且将菜鸡的预览图像数据或者视频帧数据源源不断地填充懂啊内存中去，在这种情况下，在 SurfaceFlinger 服务一端使用一个 LayerBuffer 对象来描述该 SurfaceView 的绘图表面。

　　所以，决定 surfaceView 的内存是普通内存（ 由开发者自己决定用来绘制什么 ）还是专用的内存（ 显示摄像头或者视频等，开发者无法使用这块内存 ）由 mRequestType 决定，我们在创建了一个 SurfaceView 之后，可以调用它的 SurfaceHolder 对象的成员函数 setType 来修改该 SurfaceView 的绘图表面的类型，绘图表面类型为 SURFACE_TYPE_PUSH_BUFFERS 的 SurfaceView 的 UI 是不能由应用程序来控制的，而是由专门的服务来控制的，例如，摄像头服务或者ship播放服务。

　　SurfaceView 类的成员变量 mRequestedType 目前接收如下的参数：
SURFACE_TYPE_NORMAL：用 RAM 缓存原生数据的普通 Surface。
SURFACE_TYPE_HARDWARE：适用于 DMA(Direct memory access)引擎和硬件加速的 Surface。
SURFACE_TYPE_GPU：适用于 GPU 加速的 Surface。
SURFACE_TYPE_PUSH_BUFFERS：表明该 Surface 不包含原生数据，Surface 用到的数据由其他对象提供。


## SurfaceView 的使用模板
　　SurfaceView 使用过程有一套模板代码，大部分的 SurfaceView 都可以套用。

　　3 步走套路：1.创建 SurfaceView；2.初始化 SurfaceView；3.使用 SurfaceView。

　　对 SurfaceView 执行绘制方法就是操作 Surface，使用 SurfaceHolder 来处理 Surface 的生命周期。也就是说 SurfaceView 的生命周期其实就是 Surface 的生命周期，而 SurfaceHolder 保存对 Surface 的引用，所以使用 SurfaceHolder 来处理生命周期的初始化。

## 使用 SurfaceView

　　SurfaceView 、Surface 和 SurfaceHolder，他们三者之间的关系实质上就是 MVC，Model 就是数据模型的意思也就是 Surface，View 即视图也就是 SurfaceView，SurfaceHolder 就是 Controll （ 控制器 ）。

　　在 SurfaceView 中可以通过 SurfaceHolder 接口访问它内部的 surface，而执行绘制的方法就是操作这个 Surface 内部的 Canvas，处理 Canvas 画的效果、动画、大小、像素等，getHolder() 方法可以得到 SurfaceHolder，通过 SurfaceHolder 来控制 surface 的尺寸和格式，或者修改监视 surface 的变化等等。

　　SurfaceHolder 有三个回调方法可以监听 SurfaceView 中的 surface 的声明周期，SurfaceView 一开始创建出来后，它拥有的 Surface 不一定会一起创建出来，SurfaceView 变得可见时，surface 被创建，SurfaceView 隐藏时，surface 被销毁，被创建了表示可以准备绘制了，而被销毁后就要释放其他资源，SurfaceView 一般会继承 SurfaceHolder 的 Callback 接口，SurfaceHolder.Callback 具有如下的方法：

　　**surfaceVreate(SurfaceHolder holder)：**当 Surface 第一次创建后立即调用该函数，可以在该函数中做些和绘制界面相关的初始化工作，一般情况下都是在新线程来绘制界面，所以不要在这个函数中绘制 Surface。

　　**surfaceChanged(SurfaceHolder holder,int format,int width,int height)：**当 Surface 的状态（ 大小和格式 ）发生变化的时候会调用该函数，在 surfaceCreate 调用后该函数至少会被调用一次。

　　**surfaceDestoryed(SurfaceHolder holder)：**当 Surface 被摧毁前会调用该函数，该函数被调用后就不能继续使用 Surface 了，一般在该函数中来清理使用的资源。

　　特别需要注意的是 SurfaceView 和 SurfaceHolder.Callback 的所有回调方法都是在主线程中回调的，在绘制前必须先合法的获取 Surface 才能开始绘制内容，SurfaceHolder.Callback.surfaceCreated() 和 SurfaceHolder.Callback.surfaceDestoryed() 之间的状态为合法的，在这之外使用 Surface 都会出错。

　　在使用 SurfaceView 过程中不直接和 Surface 打交道，由 SurfaceHolder 的 Canvas.lockCanvas() 或者 Cnavas.lock(Rect dirty) 函数来锁定并且获得 Surface 中的 Canvas 画布对象，通过在 Canvas 上绘制内容来修改 Surface 中的数据，如果 Surface 被别的线程占用不可编辑或者尚未创建或者已经被销毁，调用该函数会返回 null。

　　在 unlockCanvas() 和 lockCanvas() 之间 Surface 的内容是不缓存的，所以需要完全重绘 Surface 的内容，如果为了提高效率只重绘变化的部分则可以调用 lockCanvas(Rect dirty) 函数来指定一个 dirty 区域，这样该区域外的内容会缓存起来，只更新需要重绘的区域，相对部分内存要求比较高的游戏来说，不重画 dirty 外的其他区域的像素，可以提高速度。

　　在调用 lockCanvas 函数获取 Surface 的 Canvas 后，SurfaceView 会利用 Surface 的一个同步锁锁住画布 Canvas，直到调用 unlockCanvasAndPost(Canvas canvas) 函数，才解锁画布并提交改变，将图形显示，这里的同步机制保证 Surface 的 Canvas 在绘制过程中不会被改变（被摧毁、修改），避免多个不同的线程同时操作同一个 Canvas 对象。



#### 使用注意
1. 因为 SurfaceView 允许自定义的线程操作 Surface 对象执行绘制方法，而有可能同时定义多个线程执行绘制，所以当获取 SurfaceHolder 中的 Canvsa 对象时记得加同步操作，避免两个不同的线程同时操作同一个 Canvas 对象，当操作完成后记得调用 SurfaceHolder.unlockCanvasAndPost 方法释放掉 Canvas 锁。
2. 在调用 doDraw 执行绘制时，因为 SurfaceView 的特点，它会保留之前绘制的图形，所以需要先清空掉上一次绘制时留下的图形（View 则不会，它会默认在调用 View.onDraw() 方法时自动清空掉视图里的东西）。
3. 记得在回调方法 onSurfaceDestroyed() 方法里将后台执行绘制的 LoopThread （绘制的线程）关闭，这里是使用 join 方法。这设计到线程如何关闭的问题，多数人建议是通过一个标志位 isRunning 来判断线程是否该停止运行，如果想关闭线程只需要将 isRunning 改为 false 即可，线程会自动执行完 run 方法后退出。



## 查阅资料
1.[Android SurfaceView 入门学习](https://www.cnblogs.com/senior-engineer/p/7867783.html) - 简单概念与手里实现案例
2.[android SurfaceView 详解](https://blog.csdn.net/TuGeLe/article/details/79199119)
3.[Android 中 SurfaceView 的使用详解](https://www.xuebuyuan.com/3236956.html) - 简单概念与使用画圆
4.[SurfaceView - Android SDK | Android Developers](https://www.android-doc.com/reference/android/view/SurfaceView.html) - 官方文档
