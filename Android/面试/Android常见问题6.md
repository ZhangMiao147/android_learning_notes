# Android 的常见问题 5

# 1. andoird lanucher的架构怎么样，用到什么模式

[Android Launcher构建之系统框架模型](Android Launcher构建之系统框架模型)

　　Launcher 采用 MVC 模式设计已不是什么秘密。MVC 全名是 Model View Controller，是模型(model)－视图(view)－控制器(controller)的缩写。一种软件设计典范，用于组织代码用一种业务逻辑和数据显示分离的方法。模型 Model 是应用对象，视图 View 是他在屏幕上的表示，控制器 Controller 定义用户界面对用户输入的响应方式。不使用 MVC 模式将导致代码在不经意间将他们混在一起，维护与修改带来的问题可想而知。

![img](https://img-blog.csdn.net/20130624230127578?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2h5Y2hj/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![img](https://blog.csdn.net/chychc/article/details/9165985)

  上图是省略了 Controller 的模型，模型包含了一些数据值，当数据发生变化时，对应的视图就会接收到来之模型的通知，视图可以进行同步更新，对应 launcher 中的 APK 安装、卸载、语言切换等。反之用户可以通过操作修改 View 显示，当用户动作生效后 Controller 将通知 Model 修改对应的数据以保持向下同步，对应 Launcher 的 ShotCut 移动、删除、文件夹操作等。于是我们可以得到如下闭环模型。

![img](https://img-blog.csdn.net/20130624230157968?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2h5Y2hj/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![img](https://blog.csdn.net/chychc/article/details/9165985)

　　而将视图和模型分离的设计，即将对象分离，使得一个对象的改变能够影响另一些对象，而这个对象并不需要知道那些被影响的对象的细节。这个更一般的设计被描述成 Observer 模式。

　　Observer 模式定义了对象间的一种 “ 一对多 ” 类型依赖关系；当模型发生改变时，依赖他的相应视图都要更新，即当卸载 APK、移除安装有 APK 的 SD 卡等，模型必须负责更新 WorkSpace Folder AllAppList 等视图的状态，否则就会出现应用运行报错。

# 2. 问 rgb_565 位图，一个像素占多少位？ 

* A：透明度（Alpha）

* R：红色（Red）

* G：绿（Green）

* B：蓝（Blue）

　　Bitmap.Config ARGB_8888：由 4 个 8 位组成，即 A=8，R=8，G=8，B=8，那么**一个像素点占8+8+8+8=32位**（4字节）。

　　Bitmap.Config ARGB_4444：由 4 个 4 位组成，即 A=4，R=4，G=4，B=4，那么一个像素点占 4+4+4+4=16位 （ 2 字节）。

　　Bitmap.Config RGB_565：没有透明度，R=5，G=6，B=5，那么一个像素点占 5+6+5=16 位（ 2 字节）。

　　Bitmap.Config ALPHA_8：每个像素占 8 位，只有透明度，没有颜色。

　　一般情况下我们使用的色彩模式为 ARGB_8888，这种模式下一个像素所占的大小为 4 字节，一个像素的位数总和越高，图像也就越逼真。

　　假设有一张 480x800 的图片，在色彩模式为 ARGB_8888 的情况下，会占用 480 * 800 * 4 / 1024 KB = 1500 KB 的内存；而在 RGB_565 的情况下，占用的内存为：480 * 800 * 2 / 1024 KB = 750 KB。 

# 3.  Android View的刷新机制

在Android的布局体系中，父View负责刷新、布局显示子View；而当子View需要刷新时，则是通知父View来完成。


步骤就是：
1、调用子View的invalidate（）
2、跳转到上一层的invalidateChild函数中区
3、在依次次调用invalidateChildInParent的函数一次层层刷新； 


Android中对View的更新有很多种方式，使用时要区分不同的应用场合。我感觉最要紧的是分清：多线程和双缓冲的使用情况。

1.不使用多线程和双缓冲
   这种情况最简单了，一般只是希望在View发生改变时对UI进行重绘。你只需在Activity中显式地调用View对象中的invalidate()方法即可。系统会自动调用 View的onDraw()方法。

2.使用多线程和不使用双缓冲
   这种情况需要开启新的线程，新开的线程就不好访问View对象了。强行访问的话会报：android.view.ViewRoot$CalledFromWrongThreadException：Only the original thread that created a view hierarchy can touch its views.
   这时候你需要创建一个继承了android.os.Handler的子类，并重写handleMessage(Message msg)方法。android.os.Handler是能发送和处理消息的，你需要在Activity中发出更新UI的消息，然后再你的Handler（可以使用匿名内部类）中处理消息（因为匿名内部类可以访问父类变量， 你可以直接调用View对象中的invalidate()方法 ）。也就是说：在新线程创建并发送一个Message，然后再主线程中捕获、处理该消息。

3.使用多线程和双缓冲
  Android中SurfaceView是View的子类，她同时也实现了双缓冲。你可以定义一个她的子类并实现SurfaceHolder.Callback接口。由于实现SurfaceHolder.Callback接口，新线程就不需要android.os.Handler帮忙了。SurfaceHolder中lockCanvas()方法可以锁定画布，绘制玩新的图像后调用unlockCanvasAndPost(canvas)解锁（显示），还是比较方便得。

**invalidate()和postInvalidate() 的区别及使用**

当Invalidate()被调用的时候，View的OnDraw()就会被调用；Invalidate()是刷新UI，UI更新必须在主线程，所以invalidate必须在UI线程中被调用，如果在子线程中更新视图的就调用postInvalidate()。

postInvalidate()实际调用的方法，mHandler.sendMessageDelayed，在子线程中用handler发送消息，所以才能在子线程中使用。



```cpp
    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        Message msg = mHandler.obtainMessage(MSG_INVALIDATE, view);
        mHandler.sendMessageDelayed(msg, delayMilliseconds);
    }
```

# 4. 内存机制

[Android 内存管理机制](https://www.jianshu.com/p/2787a0661742)

## Android 的内存管理机制

> ###### Android使用虚拟内存和分页，不支持交换

### 垃圾收集

无论是ART还是Dalvik虚拟机，都和众多Java虚拟机一样，属于一种**托管内存环境**（程序员不需要显示的管理内存的分配与回收，交由系统自动管理）。托管内存环境会跟踪每个内存分配， 一旦确定程序不再使用一块内存，它就会将其释放回堆中，而无需程序员的任何干预。 回收托管内存环境中未使用内存的机制称为**垃圾回收**。

垃圾收集有两个目标：

- 在程序中查找将来无法访问的数据对象;
- 回收这些对象使用的资源。

Android的垃圾收集器不带压缩整理功能（Compact），即不会对Heap做碎片整理。

Android的内存堆是**分代式（Generational）**的，意味着它会将所有分配的对象进行分代，然后分代跟踪这些对象。 例如，最近分配的对象属于**年轻代（Young Generation）**。 当一个对象长时间保持活动状态时，它可以被提升为**年老代（Older Generation）**，之后还能进一步提升为**永久代（Permanent Generation）**。

每一代的对象可占用的内存总量都有其专用上限。 每当一代开始填满时，系统就会执行垃圾收集事件以试图释放内存。 **垃圾收集的持续时间取决于它在收集哪一代的对象以及每一代中有多少活动对象**。

虽然垃圾收集速度非常快，但它仍然会影响应用程序的性能。通常情况下你**不需要**控制代码中何时执行垃圾收集事件。 系统有一组用于确定何时执行垃圾收集的标准。 满足条件后，**系统将停止执行当前进程并开始垃圾回收**。 如果在像动画或音乐播放这样的密集处理循环中发生垃圾收集，则会增加处理时间。 这种增加可能会导致你的应用程序中的代码执行超过建议的16ms阈值。

> 为实现高效，流畅的帧渲染，Android建议绘制一帧的时间不要超过16ms。

此外，你的代码可能会执行各种工作，这些工作会导致垃圾收集事件更频繁地发生，或使其持续时间超过正常范围。 例如，如果在Alpha混合动画的每个帧期间在for循环的最内部分配多个对象，则大量的对象就会污染内存堆。 此时，垃圾收集器会执行多个垃圾收集事件，并可能降低应用程序的性能。

### 共享内存

Android可以跨进程共享RAM页面（Pages）。 它可以通过以下方式实现：

- **每个应用程序进程都是从名为Zygote的现有进程分叉（fork）出来的**。 Zygote进程在系统引导并加载framework代码和资源（例如Activity Themes）时启动。 要启动新的应用程序进程，系统会fork Zygote进程，然后在新进程中加载并运行应用程序的代码。 这种方法允许**在所有应用程序进程中共享大多数的为framework代码和资源分配的RAM页面**。

![img](https:////upload-images.jianshu.io/upload_images/6549967-538b41d98ae7cb0c.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/720)

- 大多数静态数据都被映射到一个进程中。 该技术允许在进程之间共享数据，并且还允许在需要时将其Page out。这些静态数据包括：Dalvik代码（通过将其置于预链接的.odex文件中进行直接的memory-mapping），app资源（通过将资源表设计为可以mmap的结构并通过对齐APK的zip条目） 和传统的项目元素，如.so文件中的本地代码。
- 在许多地方，Android使用显式分配的共享内存区域（使用ashmem或gralloc）在进程间共享相同的动态RAM。 例如，Window surface在应用程序和屏幕合成器之间使用共享内存，而游标缓冲区在Content Provider和客户端之间使用共享内存。

### 分配和回收应用的内存

Android为每个进程分配内存的时候，采用了**弹性分配方式**，也就是刚开始并不会一下分配很多内存给每个进程，而是给每一个进程分配一个 “ 够用 ” 的虚拟内存范围。这个范围是根据每一个设备实际的物理内存大小来决定的，并且可以随着应用后续需求而增加，但最多也只能达到系统为每个应用定义的上限。

**堆的逻辑大小与其使用的物理内存总量并不完全相同**。 在检查应用程序的堆时，Android 会计算一个名为 “ 比例集大小 ”（PSS）的值，该值会考虑与其他进程共享的脏页面和干净页面，但其总量与共享该 RAM 的应用程序数量成正比。 此 PSS 总量就是系统认为是你的物理内存占用量。

Android 会在内存中尽量长时间的保持应用进程，即使有些进程不再使用了。这样，当用户下次启动应用的时候，只需要恢复当前进程就可以了，不需要重新创建进程，进而减少应用的启动时间。只有当 Android 系统发现内存不足，而其他为用户提供更紧急服务的进程又需要内存时，Android 就会决定关闭某些进程以回收内存。

### 限制应用的内存

为了维护高效的多任务环境，Android 为每个应用程序设置了堆大小的硬性限制。 该限制因设备而异，取决于设备总体可用的 RAM。 如果应用程序已达到该限制并尝试分配更多内存，则会收到 `OutOfMemoryError` 。

在某些情况下，你可能希望查询系统以准确确定当前设备上可用的堆空间大小，例如，确定可以安全地保留在缓存中的数据量。 你可以通过调用 `getMemoryClass()` 来查询系统中的这个数字。 此方法返回一个整数，指示应用程序堆可用的兆字节数。

### 切换应用

当用户在应用程序之间切换时，Android 会将**非前台应用程序**（即用户不可见或并没有运行诸如音乐播放等前台服务的进程）缓存到一个**最近最少使用缓存**（LRU Cache）中。例如，当用户首次启动应用程序时，会为其创建一个进程; 但是当用户离开应用程序时，该进程不会退出。 系统会缓存该进程。 如果用户稍后返回应用程序，系统将重新使用该进程，从而使应用程序切换更快。

如果你的应用程序具有缓存进程并且它保留了当前不需要的内存，那么即使用户未使用它，你的应用程序也会影响系统的整体性能。 当系统内存不足时，就会从最近最少使用的进程开始，终止 LRU Cache 中的进程。另外，系统还会综合考虑保留了最多内存的进程，并可能终止它们以释放 RAM。

> 当系统开始终止 LRU Cache 中的进程时，它主要是自下而上的。 系统还会考虑哪些进程占用更多内存，因为在它被杀时会为系统提供更多内存增益。 因此在整个 LRU 列表中消耗的内存越少，保留在列表中并且能够快速恢复的机会就越大。

## Android对Linux系统的内存管理机制进行的优化

Android对内存的使用方式同样是 “ 尽最大限度的使用 ”，这一点继承了 Linux 的优点。只不过有所不同的是，Linux 侧重于尽可能多的缓存磁盘数据以降低磁盘 IO 进而提高系统的数据访问性能，而 Android 侧重于尽可能多的缓存进程以提高应用启动和切换速度。Linux 系统在进程活动停止后就结束该进程，而 **Android 系统则会在内存中尽量长时间的保持应用进程，直到系统需要更多内存为止**。这些保留在内存中的进程，通常情况下不会影响系统整体运行速度，反而会在用户再次激活这些进程时，加快进程的启动速度，因为不用重新加载界面资源了，这是 Android 标榜的特性之一。所以，**Android 现在不推荐显式的 “ 退出 ” 应用**。

那为什么内存少的时候运行大型程序会慢呢，原因是：在内存剩余不多时打开大型程序会触发系统自身的进程调度策略，这是十分消耗系统资源的操作，特别是在一个程序频繁向系统申请内存的时候。这种情况下系统并不会关闭所有打开的进程，而是选择性关闭，频繁的调度自然会拖慢系统。

## Android 中的进程管理

说到 Android 的内存管理，就不得不提到进程管理，因为进程管理确确切切的影响着系统内存。在了解进程管理之前，我们首先了解一些基础概念。

当某个应用组件启动且该应用没有运行其他任何组件时，Android 系统会使用单个执行线程为应用启动新的 Linux 进程。**默认情况下，同一应用的所有组件在相同的进程和线程（称为 “ 主 ” 线程）中运行**。 如果某个应用组件启动且该应用已存在进程（因为存在该应用的其他组件），则该组件会在此进程内启动并使用相同的执行线程。 但是，你也可以安排应用中的其他组件在单独的进程中运行，并为任何进程创建额外的线程。

> Android 应用模型的设计思想取自 Web 2.0 的 Mashup 概念，是**基于组件的应用设计模式**。在该模型下，每个应用都由一系列的组件搭建而成，组件通过应用的配置文件描述功能。Android 系统依照组件的配置信息，了解各个组件的功能并进行统一调度。这就意味着，来自不同应用的组件可以有机地结合在一起，共同完成任务，各个 Android 应用，只有明确的组件边界，而不再有明确的进程边界和应用边界。这种设计，也令得开发者无需耗费精力去重新开发一些附属功能，而是可以全身心地投入到核心功能的开发中。这样不但提高了应用开发的效率，也增强了用户体验（比如电子邮件中选择图片作为附件的功能，可以直接调用专门的图片应用的功能，不用自己从头开发）。

系统不会为每个组件实例创建单独的线程。**运行于同一进程的所有组件均在 UI 线程中实例化，并且对每个组件的系统调用均由该线程进行分派**。 因此，响应系统回调的方法（例如，报告用户操作的 onKeyDown() 或生命周期回调方法）始终在进程的 UI 线程中运行（四大组件的各个生命周期回调方法都是在 UI 线程中触发的）。

### 进程的生命周期

Android 的一个不寻常的基本特征是应用程序进程的生命周期并非是由应用本身直接控制的。相反，**进程的生命周期是由系统决定的，系统会权衡每个进程对用户的相对重要程度，以及系统的可用内存总量来确定**。比如说相对于终止一个托管了正在与用户交互的 Activity 的进程，系统更可能终止一个托管了屏幕上不再可见的 Activity 的进程，否则这种后果是可怕的。因此，**是否终止某个进程取决于该进程中所运行组件的状态**。Android 会有限清理那些已经不再使用的进程，以保证最小的副作用。

作为应用开发者，了解各个应用组件（特别是 Activity、Service 和 BroadcastReceiver ）如何影响应用进程的生命周期非常重要。不正确的使用这些组件，有可能导致系统在应用执行重要工作时终止进程。

举个常见的例子， `BroadcastReceiver` 在其 `onReceive()` 方法中接收到Intent时启动一个线程，然后从该函数返回。而一旦返回，系统就认为该 `BroadcastReceiver` 不再处于活动状态，因此也就不再需要其托管进程（除非该进程中还有其他组件处于活动状态）。这样一来，系统就有可能随时终止进程以回收内存，而这也最终会导致运行在进程中的线程被终止。此问题的解决方案通常是从 `BroadcastReceiver` 中安排一个 `JobService` ，以便系统知道在该进程中仍有活动的工作。

为了确定在内存不足时终止哪些进程，Android 会根据进程中正在运行的组件以及这些组件的状态，将每个进程放入 “ **重要性层次结构** ” 中。必要时，系统会首先杀死重要性最低的进程，以此类推，以回收系统资源。这就相当于为进程分配了优先级的概念。

### 进程优先级

Android中总共有5个进程优先级（按重要性降序）：

![img](https:////upload-images.jianshu.io/upload_images/6549967-43f7c647a29e4b44.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/291)

#### Foreground Process：前台进程（正常不会被杀死）

用户当前操作所必需的进程。有很多组件能以不同的方式使得其所在进程被判定为前台进程。如果一个进程满足以下任一条件，即视为前台进程：

- 托管用户正在交互的 Activity（已调用 Activity 的 onResume() 方法）
- 托管某个 Service，后者绑定到用户正在交互的 Activity
- 托管正执行一个生命周期回调的 Service（onCreate()、onStart() 或 onDestroy()）
- 托管正执行其 onReceive() 方法的 BroadcastReceiver

通常，在任意给定时间前台进程都为数不多。只有在内存不足以支持它们同时继续运行这一万不得已的情况下，系统才会终止它们。 此时，设备往往已达到内存分页状态，因此需要终止一些前台进程来确保用户界面正常响应。

#### Visible Process：可见进程（正常不会被杀死）

没有任何前台组件、但仍会影响用户在屏幕上所见内容的进程。杀死这类进程也会明显影响用户体验。 如果一个进程满足以下任一条件，即视为可见进程：

- 托管不在前台、但仍对用户可见的 Activity（已调用其 onPause() 方法）。例如，启动了一个对话框样式的前台 activity ，此时在其后面仍然可以看到前一个Activity。

  > 运行时权限对话框就属于此类。
  > 考虑一下，还有哪种情况会导致只触发onPause而不触发onStop?

- 托管通过 Service.startForeground() 启动的前台Service。

  > Service.startForeground()：它要求系统将它视为用户可察觉到的服务，或者基本上对用户是可见的。

- 托管系统用于某个用户可察觉的特定功能的 Service，比如动态壁纸、输入法服务等等。

可见进程被视为是极其重要的进程，除非为了维持所有前台进程同时运行而必须终止，否则系统不会终止这些进程。如果这类进程被杀死，从用户的角度看，这意味着当前 activity 背后的可见 activity 会被黑屏代替。

#### Service Process：服务进程（正常不会被杀死）

正在运行已使用 `startService()` 方法启动的服务且不属于上述两个更高类别进程的进程。尽管服务进程与用户所见内容没有直接关联，但是它们通常在执行一些用户关心的操作（例如，后台网络上传或下载数据）。因此，除非内存不足以维持所有前台进程和可见进程同时运行，否则系统会让服务进程保持运行状态。

**已经运行很久（例如30分钟或更久）的Service，有可能被降级**，这样一来它们所在的进程就可以被放入Cached LRU 列表中。这有助于避免一些长时间运行的Service由于内存泄漏或其他问题而消耗过多的RAM，进而导致系统无法有效使用缓存进程的情况。

#### Background / Cached Process：后台进程（可能随时被杀死）

这类进程一般会持有一个或多个目前对用户不可见的 Activity （已调用 Activity 的 onStop() 方法）。它们不是当前所必须的，因此当其他更高优先级的进程需要内存时，系统可能**随时终止**它们以回收内存。但如果正确实现了 Activity 的生命周期，即便系统终止了进程，当用户再次返回应用时也不会影响用户体验：关联 Activity 在新的进程中被重新创建时可以恢复之前保存的状态。

> 在一个正常运行的系统中，缓存进程是内存管理中**唯一**涉及到的进程：一个运行良好的系统将始终具有多个缓存进程（为了更高效的切换应用），并根据需要定期终止最旧的进程。只有在非常严重（并且不可取）的情况下，系统才会到达这样一个点，此时所有的缓存进程都已被终止，并且必须开始终止服务进程。

> **Android系统回收后台进程的参考条件**：
> **LRU算法**：自下而上开始终止，先回收最老的进程。越老的进程近期内被用户再次使用的几率越低。杀死的进程越老，对用户体验的影响就越小。
> **回收收益**：系统总是倾向于杀死一个能回收更多内存的进程，因为在它被杀时会为系统提供更多内存增益，从而可以杀死更少的进程。杀死的进程越少，对用户体验的影响就越小。换句话说，应用进程在整个LRU列表中消耗的内存越少，保留在列表中并且能够快速恢复的机会就越大。

这类进程会被保存在一个伪LRU列表中，系统会优先杀死处于列表尾部（最老）的进程，以确保包含用户最近查看的 Activity 的进程最后一个被终止。这个LRU列表排序的确切策略是平台的实现细节，但通常情况下，相对于其他类型的进程，系统会优先尝试保留更有用的进程（比如托管用户主应用程序的进程，或者托管用户看到的最后一个Activity的进程，等等）。还有其他一些用于终止进程的策略：对允许的进程数量硬限制，对进程可以持续缓存的时间量的硬限制，等等。

> 在一个健康的系统中，只有缓存进程或者空进程会被系统随时终止，如果服务进程，或者更高优先级的可见进程以及前台进程也开始被系统终止（不包括应用本身糟糕的内存使用导致OOM），那就说明系统运行已经处于一个亚健康甚至极不健康的状态，可用内存已经吃紧。

#### Empty Process：空进程（可以随时杀死）

不含任何活跃组件的进程。保留这种进程的的唯一目的是用作缓存（为了更加有效的使用内存而不是完全释放掉），以缩短下次启动应用程序所需的时间，因为启动一个新的进程也是需要代价的。只要有需要，Android会随时杀死这些进程。

> 内存管理中对于前台/后台应用的定义，与用于Service限制目的的后台应用定义不同。从Android 8.0开始，出于节省系统资源、优化用户体验、提高电池续航能力的考量，系统进行了前台/后台应用的区分，对于后台service进行了一些限制。在该定义中，如果满足以下任意条件，应用将被视为处于前台：
>
> - 具有可见 Activity（不管该 Activity 已启动还是已暂停）。
> - 具有前台 Service。
> - 另一个前台应用已关联到该应用（不管是通过绑定到其中一个 Service，还是通过使用其中一个内容提供程序）。 例如，如果另一个应用绑定到该应用的 Service，那么该应用处于前台：
>   IME
>   壁纸 Service
>   通知侦听器
>   语音或文本 Service
>   如果以上条件均不满足，应用将被视为处于后台。 详见[后台Service限制](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.google.cn%2Fabout%2Fversions%2Foreo%2Fbackground%23services)

### Android系统如何评定进程的优先级

**根据进程中当前活动组件的重要程度，Android 会将进程评定为它可能达到的最高级别**。例如，如果某进程同时托管着 Service 和可见 Activity，则会将此进程评定为可见进程，而不是服务进程。

此外，**一个进程的级别可能会因其他进程对它的依赖而有所提高，即服务于另一进程的进程其级别永远不会低于其所服务的进程**。 例如，如果进程 A 中的内容提供程序为进程 B 中的客户端提供服务，或者如果进程 A 中的服务绑定到进程 B 中的组件，则进程 A 始终被视为至少与进程 B 同样重要。

由于运行服务的进程其级别高于托管后台 Activity 的进程，因此，在 Activity 中启动一个长时间运行的操作时，最好为该操作启动服务，而不是简单地创建工作线程，当操作有可能比 Activity 更加持久时尤要如此。例如，一个文件上传的操作就可以考虑使用服务来完成，这样一来，即使用户退出 Activity，仍可在后台继续执行上传操作。使用服务可以保证，无论 Activity 发生什么情况，该操作至少具备“服务进程”优先级。 同理， `BroadcastReceiver` 也应使用服务，而不是简单地将耗时冗长的操作放入线程中。

### Home键退出和返回键退出的区别

Home键退出，程序保留状态为后台进程；而返回键退出，程序保留状态为空进程，空进程更容易被系统回收。Home键其实主要用于进程间切换，返回键则是真正的退出程序。

从理论上来讲，无论是哪种情况，在没有任何后台工作线程（即便应用处于后台，工作线程仍然可以执行）的前提下，被置于后台的进程都只是保留他们的运行状态，并不会占用CPU资源，所以也不耗电。只有音乐播放软件之类的应用需要在后台运行Service，而Service是需要占用CPU时间的，此时才会耗电。所以说没有带后台服务的应用是不耗电也不占用CPU时间的，没必要关闭，这种设计本身就是Android的优势之一，可以让应用下次启动时更快。然而现实是，很多应用多多少少都会有一些后台工作线程，这可能是开发人员经验不足导致（比如线程未关闭或者循环发送的Handler消息未停止），也可能是为了需求而有意为之，导致整个Android应用的生态环境并不是一片干净。

# 5. JNI 线程需要对 java VM 做的操作. 

[Android JNI学习-线程操作](https://www.jianshu.com/p/65b59bf75ec0)

Android Native 中支持的线程标准是 POSIX 线程。POSIX 线程也被简称为Pthreads，是一个线程的 POSIX 标准，它为创建和处理线程定义了一个通用的 API。

POSIX Thread 的 Android 实现是 Bionic 标准库的一部分，在编译的时候不需要链接任何其他的库，只需要包含一个头文件。

```c
#include <pthread.h>
```

## 5.1. 创建线程

线程创建函数：

```c
int pthread_create(
    pthread_t* thread, 
    pthread_attr_t const* attr, 
    void* (*start_routine)(void*), 
    void* arg);
```

- thread：指向 pthread_t 类型变量的指针，用它代表返回线程的句柄。

- attr：指向 pthread_attr_t 结构的指针形式存在的新线程属性，可以通过该结构来指定新线程的一些属性，比如栈大小、调度优先级等，具体看 pthread_attr_t 结构的内容。如果没有特殊要求，可使用默认值，把该变量取值为 NULL 。

- 第三个参数是指向启动函数的函数指针，它的函数签名格式如下：

  ```c
  void* start_routine(void* args)
  ```

  启动程序将线程参数看成 void 指针，返回 void 指针类型结果。

- 线程启动程序的参数，也就是函数的参数，如果不需要传递参数，它可以为 NULL 。

`pthread_create` 函数如果执行成功了则返回 0 ，如果返回其他错误代码。

```c
void sayHello(void *){
    LOGE("say %s","hello");
}

JNIEXPORT jint JNICALL Java_com_david_JNIController_sayhello
        (JNIEnv *jniEnv, jobject instance) {
    pthread_t handles; // 线程句柄
    int ret = pthread_create(&handles, NULL, sayHello, NULL);
    if (ret != 0) {
        LOGE("create thread failed");
    } else {
        LOGD("create thread success");
    }
}
```

调用函数就可以在线程执行打印 say hello 了。

## 5.2. 附着在Java虚拟机上

创建了线程后，只能做一些简单的 Native 操作，如果想要对 Java 层做一些操作就不行了，因为它没有 Java 虚拟机环境，这个时候为了和 Java 空间进行交互，就要把 POSIX 线程附着在 Java 虚拟机上，然后就可以获得当前线程的 JNIEnv 指针了。

通过 `AttachCurrentThread` 方法可以将当前线程附着到 Java 虚拟机上，并且可以获得 JNIEnv 指针。而`AttachCurrentThread` 方法是由 JavaVM 指针调用的，可以在`JNI_OnLoad`函数中将JavaVM 保存为全局变量。



```c
static JavaVM *jVm = NULL;
JNIEXPORT int JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jVm = vm;
    return JNI_VERSION_1_6;
}
```

如上一个例子，我们想要在 sayHello 函数中调用一个 Java 层的函数`javaSayHello()`



```java
private void javaSayHello() {
    Log.e(TAG,"java say hello");
}
```



```c
void sayHello(void *){
    LOGE("say %s","hello");
     JNIEnv *env = NULL;
    // 将当前线程添加到 Java 虚拟机上
    if (jVm->AttachCurrentThread(&env, NULL) == 0) {
        ......
        env->CallVoidMethod(Obj, javaSayHello);
        // 从 Java 虚拟机上分离当前线程
        jVm->DetachCurrentThread();  
    }
    return NULL;
}
```

这样就在 Native 线程中调用 Java 相关的函数了。

## 5.3. 等待线程返回结果

前面提到的方法是新线程运行后，该方法也就立即返回退出，执行完了。我们也可以通过另一个函数可以在等待线程执行完毕后，拿到线程执行完的结果之后再退出。

```c
int pthread_join(pthread_t pthread, void** ret_value);
```

- pthread 代表创建线程的句柄
- ret_value 代表线程运行函数返回的结果

```c
    pthread_t* handles = new pthread_t[10];
    
    for (int i = 0; i < 10; ++i) {
        pthread_t pthread;
        // 创建线程，
        int result = pthread_create(&handles[i], NULL, run, NULL;
        }
    }
    for (int i = 0; i < 10; ++i) {
        void *result = NULL; // 线程执行返回结果
        // 等待线程执行结束
        if (pthread_join(handles[i], &result) != 0) {
            env->ThrowNew(env, runtimeException, "Unable to join thread");
        } else {
            LOGD("return value is %d",result);
        }
    }
```

pthread_join 返回为 0 代表执行成功，非 0 则执行失败。

## 5.4. 同步代码块

在 Java 中，JDK 为我们提供了 synchronized 来处理多线程同步代码块。

```java
 synchronized (object.class) {
        // 业务处理
    }
```

本地代码中，JNI 提供了两个函数来完成上面的同步：

（1）MonitorEnter：进入同步代码块

（2）MonitorExit：退出同步代码块

```c
if(env->MonitorEnter(obj)!= JNI_OK){
    // 错误处理
}

// 同步代码块

// 出现错误释放代码块
if(env->ExceptionCheck()){
    if(env->MonitorExit(obj)!= JNI_OK);
       return;
}

if(env->MonitorExit(obj)!= JNI_OK){
    // 错误处理
}
```

可以发现在本地代码中处理同步代码块要比 Java 中复杂的多，所以，尽量用 Java 来做同步吧，把与同步相关的代码都移到 Java 中去。

# 6. activity和service的通信方式

1. 在 Activity 中启动后台 Service，通过 Intent 来启动，Intent 中可以传递数据给 Service。

2. Activity 调用 bindService (Intent service, ServiceConnection conn, int flags) 方法，得到 Service 对象的一个引用，这样 Activity 可以直接调用到 Service 中的方法，如果要主动通知 Activity，可以利用回调方法。

3. Service 向 Activity 发送消息，可以使用广播，当然 Activity 要注册相应的接收器。比如 Service 要向多个 Activity 发送同样的消息的话，用这种方法就更好。

# 7. 并发和并行分别是什么意思，多线程是并发还是并行

并发是两个队列交替使用一台咖啡机，并行是两个队列同时使用两台咖啡机，如果串行，一个队列使用一台咖啡机，那么哪怕前面那个人便秘了去厕所呆半天，后面的人也只能死等着他回来才能去接咖啡，这效率无疑是最低的。

多线程是并发，OS 的线程调度机制将时间划分为很多时间片段（时间片），尽可能均匀分配给正在运行的程序，获取 CPU 时间片的线程或进程得以被执行，其他则等待。而 CPU 则在这些进程或线程上来回切换运行。

# 8.  一个按钮，手抖了连续点了两次，会跳转两次页面，怎么让这种情况不发生。

https://www.jianshu.com/p/dc63a4b636fa

按钮点击后就设置 enable 为 false。

根据上一次点击的时间，计算两次点击的时间间隔，时间间隔太小，就不响应。

页面的 Activity 设置为 singTop 启动模式。

# 9. 项目中定时为什么用AlarmManager，不用postDelayed



# 10. 项目中后台网络请求为什么用service不用线程

 [Android中的Service 与 Thread 的区别](http://blog.csdn.net/jiangwei0910410003/article/details/17008687)

 1). Thread：Thread 是程序执行的最小单元，它是分配CPU的基本单位。可以用 Thread 来执行一些异步的操作。 

2). Service：Service 是android的一种机制，当它运行的时候如果是 Local Service，那么对应的 Service 是运行在主进程的 main 线程上的。如：onCreate，onStart 这些函数在被系统调用的时候都是在主进程的 main 线程上运行的。如果是 Remote Service，那么对应的 Service 则是运行在独立进程的 main 线程上。因此请不要把 Service 理解成线程，它跟线程半毛钱的关系都没有！既然这样，那么我们为什么要用 Service 呢？其实这跟 android 的系统机制有关，我们先拿 Thread 来说。Thread 的运行是独立于 Activity 的，也就是说当一个 Activity 被 finish 之后，如果你没有主动停止 Thread 或者 Thread 里的 run 方法没有执行完毕的话，Thread 也会一直执行。因此这里会出现一个问题：当 Activity 被 finish 之后，你不再持有该 Thread 的引用。另一方面，你没有办法在不同的 Activity 中对同一 Thread 进行控制。

举个例子：如果你的 Thread 需要不停地隔一段时间就要连接服务器做某种同步的话，该 Thread 需要在 Activity 没有start的时候也在运行。这个时候当你 start 一个 Activity 就没有办法在该 Activity 里面控制之前创建的 Thread。因此你便需要创建并启动一个 Service ，在 Service 里面创建、运行并控制该 Thread，这样便解决了该问题（因为任何 Activity 都可以控制同一 Service，而系统也只会创建一个对应 Service 的实例）。

因此你可以把 Service 想象成一种消息服务，而你可以在任何有 Context 的地方调用 Context.startService、Context.stopService、Context.bindService，Context.unbindService，来控制它，你也可以在 Service 里注册 BroadcastReceiver，在其他地方通过发送 broadcast 来控制它，当然这些都是 Thread 做不到的。



