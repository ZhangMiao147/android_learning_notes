# Android 的常见问题

# 1. 内存优化、布局优化

Android布局优化之ViewStub、include、merge使用与源码分析 https://blog.csdn.net/bboyfeiyu/article/details/45869393 

Android 性能优化之旅4--UI卡顿分析 https://www.jianshu.com/p/72144b627bb0

# 2. View 的绘制流程

https://www.cnblogs.com/andy-songwei/p/10955062.html

　　每一个视图的绘制过程都必须经历三个最主要的阶段，即 onMeasure()、onLayout() 和 onDraw()。

 　　onMeasure()：

# 3. Android 进程保活

## 3.1. 保活手段

　　当前业界的 Android 进程保活手段主要分为黑、白、灰三种，其大致的实现思路如下：

　　**黑色保活**：不同的 app 进程，用广播相互唤醒（包括利用系统提供的广播进行唤醒）。

　　**白色保活**：启动前台 Service。

　　**灰色保活**：利用**系统的漏洞**启动前台Service。

## 3.2. 黑色保活

　　所谓黑色保活，就是利用不同的 app 进程使用广播来进行相互唤醒。举个 3 个比较常见的场景：

　　**场景1**：开机，网络切换、拍照、拍视频时候，利用系统产生的广播唤醒 app 。

　　**场景2**：接入第三方 SDK 也会唤醒相应的 app 进程，如微信 sdk 会唤醒微信，支付宝 sdk 会唤醒支付宝。由此发散开去，就会直接触发了下面的 **场景3**。

　　**场景3**：假如你手机里装了支付宝、淘宝、天猫、UC 等阿里系的app，那么你打开任意一个阿里系的 app 后，有可能就顺便把其他阿里系的 app 给唤醒了。（只是拿阿里打个比方，其实 BAT 系都差不多）

　　针对**场景1**，估计 Google 已经开始意识到这些问题，所以在最新的 Android N 取消了 ACTION_NEW_PICTURE（拍照），ACTION_NEW_VIDEO（拍视频），CONNECTIVITY_ACTION（网络切换）等三种广播，无疑给了很多 app 沉重的打击。

　　而开机广播的话，记得有一些定制 ROM 的厂商早已经将其去掉。

　　针对**场景2**和**场景3**，因为调用 SDK 唤醒 app 进程属于正常行为，此处不讨论。但是在借助 LBE 分析 app 之间的唤醒路径的时候，发现了两个问题：

1. 很多推送 SDK 也存在唤醒 app 的功能
2. app 之间的唤醒路径真是多，且错综复杂

## 3.3. 白色保活

　　白色保活手段非常简单，就是调用系统 api 启动一个前台的 Service 进程，这样会在系统的通知栏生成一个 Notification，用来让用户知道有这样一个 app 在运行着，哪怕当前的 app 退到了后台。如QQ音乐。

## 3.4. 灰色保活

　　灰色保活，这种保活手段是应用范围最广泛。它是利用系统的漏洞来启动一个前台的 Service 进程，与普通的启动方式区别在于，它不会在系统通知栏处出现一个 Notification，看起来就如同运行着一个后台 Service 进程一样。这样做带来的好处就是，用户无法察觉到你运行着一个前台进程（因为看不到 Notification）,但你的进程优先级又是高于普通后台进程的。那么如何利用系统的漏洞呢，大致的实现思路和代码如下：

- 思路一：API < 18，启动前台Service时直接传入new Notification()；
- 思路二：API >= 18，同时启动两个 id 相同的前台 Service，然后再将后启动的 Service 做 stop 处理；

```java
public class GrayService extends Service {

    private final static int GRAY_SERVICE_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    ...
    ...

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

    }
}
```

　　代码大致就是这样，能让你神不知鬼不觉的启动着一个前台Service。其实市面上很多app都用着这种灰色保活的手段。

　　其实 Google 察觉到了此漏洞的存在，并逐步进行封堵。这就是为什么这种保活方式分 API >= 18 和 API < 18 两种情况，从 Android5.0 的ServiceRecord 类的 postNotification 函数源代码中可以看到这样的一行注释

![img](https:////upload-images.jianshu.io/upload_images/912181-886c3f690e4f06d6.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/624/format/webp)

　　当某一天 API >= 18 的方案也失效的时候，就又要另谋出路了。需要注意的是，**使用灰色保活并不代表着你的 Service 就永生不死了，只能说是提高了进程的优先级。如果你的 app 进程占用了大量的内存，按照回收进程的策略，同样会干掉你的 app。**

## 3.5. 进程回收机制

　　熟悉 Android 系统的童鞋都知道，系统出于体验和性能上的考虑， app 在退到后台时系统并不会真正的 kill 掉这个进程，而是将其缓存起来。打开的应用越多，后台缓存的进程也越多。在系统内存不足的情况下，系统开始依据自身的一套进程回收机制来判断要kill掉哪些进程，以腾出内存来供给需要的app。这套杀进程回收内存的机制就叫 **Low Memory Killer** ，它是基于Linux内核的 **OOM Killer（Out-Of-Memory killer）**机制诞生。

了解完 **Low Memory Killer**，再科普一下**oom_adj**。什么是**oom_adj**？它是linux内核分配给每个系统进程的一个值，代表进程的优先级，进程回收机制就是根据这个优先级来决定是否进行回收。对于**oom_adj**的作用，你只需要记住以下几点即可：

- **进程的oom_adj越大，表示此进程优先级越低，越容易被杀回收；越小，表示进程优先级越高，越不容易被杀回收**
- **普通app进程的oom_adj>=0,系统进程的oom_adj才可能<0**

那么我们如何查看进程的**oom_adj**值呢，需要用到下面的两个shell命令



```cpp
ps | grep PackageName //获取你指定的进程信息
```

![img](https:////upload-images.jianshu.io/upload_images/912181-5a244b4256260e76.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/576/format/webp)

这里是以我写的demo代码为例子，红色圈中部分别为下面三个进程的ID

UI进程：**com.clock.daemon**
 普通后台进程：**com.clock.daemon:bg**
 灰色保活进程：**com.clock.daemon:gray**

当然，这些进程的id也可以通过AndroidStudio获得

![img](https:////upload-images.jianshu.io/upload_images/912181-5ae05cdd97ded04e.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/488/format/webp)

接着我们来再来获取三个进程的**oom_adj**



```undefined
cat /proc/进程ID/oom_adj
```

![img](https:////upload-images.jianshu.io/upload_images/912181-eb170317ab201e22.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/554/format/webp)

从上图可以看到UI进程和灰色保活Service进程的**oom_adj=0**，而普通后台进程**oom_adj=15**。到这里估计你也能明白，**为什么普通的后台进程容易被回收，而前台进程则不容易被回收了吧。**但明白这个还不够，接着看下图

![img](https:////upload-images.jianshu.io/upload_images/912181-b54d0a1eb4da6785.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/556/format/webp)

上面是我把app切换到后台，再进行一次**oom_adj**的检验，你会发现UI进程的值从0变成了6,而灰色保活的Service进程则从0变成了1。这里可以观察到，**app退到后台时，其所有的进程优先级都会降低。但是UI进程是降低最为明显的，因为它占用的内存资源最多，系统内存不足的时候肯定优先杀这些占用内存高的进程来腾出资源。所以，为了尽量避免后台UI进程被杀，需要尽可能的释放一些不用的资源，尤其是图片、音视频之类的**。

从Android官方文档中，我们也能看到优先级从高到低列出了这些不同类型的进程：**Foreground process**、**Visible process**、**Service process**、**Background process**、**Empty process**。而这些进程的oom_adj分别是多少，又是如何挂钩起来的呢？推荐大家阅读下面这篇文章：

[http://www.cnblogs.com/angeldevil/archive/2013/05/21/3090872.html](https://link.jianshu.com?t=http://www.cnblogs.com/angeldevil/archive/2013/05/21/3090872.html)



# 4. 动画的分类及其原理

https://www.jianshu.com/p/88d349009530

# 5. SparseArray 原理

https://www.cnblogs.com/xiaxveliang/p/12396049.html

https://www.jianshu.com/p/081b78dfe9f6

https://www.cnblogs.com/RGogoing/p/5095168.html

# 6. 一个 Android 程序至少包含几个线程

　　线程是指进程中的一个执行流程，一个进程中可以运行多个线程。

　　而进程就是一个内存中运行的应用程序，而且有它自己独立的一块内存空间，一个程序至少有一个进程，一个进程至少有一个线程。

　　在 Java 中，每次程序运行至少启动 2 个线程：一个是 main 线程，一个是垃圾收集线程。因为每当使用 java 命令执行一个类的时候，实际上都会启动一个 JVM，每一个 JVM 实际上就是在操作系统中启动了一个进程。

　	UI 线程即用户交互线程，用来处理用户消息和界面绘制。

　　其次，每个 Binder 对象对应一个线程；在 ActivityThread 中会创建 ApplicationThread，他们都是继承 Binder，这里会启动两个线程。

　　所以最少应该是 4 个线程，UI 线程、两个 Binder 、一个垃圾收集线程，然后开发人员自定义的子线程。



