# WindowManangerService

## 1. WMS 概述

　　WMS 是系统的其他服务，无论对于应用开发还是 Framework 开发都是重点的知识，它的职责有很多，主要有以下几点：

### 1. 窗口管理

　　WMS 是窗口的管理者，它负责窗口的启动、添加和删除，另外窗口的大小和层级也是由 WMS 进行管理的。窗口管理的核心成员有 DisplayContent、WindowToken 和 WindowState。

### 2. 窗口动画

　　窗口间进行切换时，使用窗口动画可以显得更炫一些，窗口动画由 WMS 的动画子系统来负责，动画自系统的管理者为 WindowAnimator。

### 3. 输入系统的中转站

　　通过对窗口的触摸从而产生触摸事件，InputManangerService（IMS）会对触摸事件进行处理，它会寻找一个最合适的窗口来处理触摸反馈信息，WMS 是窗口的管理者，因此，WMS "理所应当" 的成为了输入系统的中转站。

### 4. Surface 管理

　　窗口并不具备有绘制的功能，因此每个窗口都需要有一块 Surface 来供自己绘制。为每个窗口分配 Surface 是由 WMS 来完成的。

　　WMS 的职责可以简单总结为下图：

![](image/WMS职责.png)

## 2. WMS 的产生

　　WMS 是在 SystemServer 进程中启动的。

　　先来看 SystemServer 的 main 方法:

### 2.1. SysterServer#main

```java
    public static void main(String[] args) {
        new SystemServer().run();
    }
```

　　main 方法中只调用了 SystemServer 的 run 方法。

### 2.2. SystemServer#run

```java
    private void run() {
        try {
            ...

            // Prepare the main looper thread (this thread).
            android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_FOREGROUND);
            android.os.Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();

            // Initialize native services.
            // 加载了 libandroid_server.so
            System.loadLibrary("android_servers");

            ...

            // Create the system service manager.
            // 创建 SystemServerManager
            // 它会对系统的服务进行创建、启动和生命周期管理
            mSystemServiceManager = new SystemServiceManager(mSystemContext);
            mSystemServiceManager.setRuntimeRestarted(mRuntimeRestart);
            LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);
            // Prepare the thread pool for init tasks that can be parallelized
            SystemServerInitThreadPool.get();
        } finally {
            traceEnd();  // InitBeforeStartServices
        }

        // Start services.
        // 启动各种服务
        try {
            traceBeginAndSlog("StartServices");
            // 用 SystemServiceManager 启动了 ActivityManangerService、PowerManagerService、PackageManagerService 等服务。
            startBootstrapServices();
            // 启动了 BatteryService、UsageStatsService 和 WebVideUpdateService。
            startCoreServices();
            // 启动了 CameService、AlarmManagerService、VrManagerService 等服务。
            startOtherServices();
            SystemServerInitThreadPool.shutdown();
        } catch (Throwable ex) {
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting system services", ex);
            throw ex;
        } finally {
            traceEnd();
        }

        ...

        // Loop forever.
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }
```

　　使用 startBootstrapServices()、startCoreServices()、startOtherServices() 三个方法启动的服务的父类为 SystemService。

　　官方把大概 100 多个系统服务分为了三种类型，分别是引导服务、核心服务和其他服务，其中其他服务为一些非紧要和一些不需要立即启动的服务，WMS 就是其他服务的一种。

### 3. SystemServer#startOtherServices

```java
    /**
     * Starts a miscellaneous grab bag of stuff that has yet to be refactored
     * and organized.
     */
    private void startOtherServices() {
        ...

        try {
            
            ...

            traceBeginAndSlog("InitWatchdog");
            // 得到 Watchdog 实例
            final Watchdog watchdog = Watchdog.getInstance();
            // 初始化 watchdog
            // watchdog 用来监控系统的一些关键服务的运行状况
            watchdog.init(context, mActivityManagerService);
            traceEnd();

            traceBeginAndSlog("StartInputManagerService");
            // 创建 IMS，并赋值给 IMS 类型的 inputManager 对象
            inputManager = new InputManagerService(context);
            traceEnd();

            traceBeginAndSlog("StartWindowManagerService");
            // WMS needs sensor service ready
            ConcurrentUtils.waitForFutureNoInterrupt(mSensorServiceStart, START_SENSOR_SERVICE);
            mSensorServiceStart = null;
            // 执行了 WMS 的 main 方法。其内部会创建 WMS，需要注意的是 main 方法其中一个传入的参数就是上面创建的 IMS，WMS 是输入书简的中转站，其内部包含了 IMS 引用并不意外。
            wm = WindowManagerService.main(context, inputManager,
                    mFactoryTestMode != FactoryTest.FACTORY_TEST_LOW_LEVEL,
                    !mFirstBoot, mOnlyCore, new PhoneWindowManager());
            // 将 WMS 注册到 ServiceManager 中
            ServiceManager.addService(Context.WINDOW_SERVICE, wm);
            // 将 IMS 注册到 ServiceManager 中。
            ServiceManager.addService(Context.INPUT_SERVICE, inputManager);
            traceEnd();
            
            ...        
                
        }
        
        ...
            
        traceBeginAndSlog("MakeDisplayReady");
        try {
            // 初始化显示信息
            wm.displayReady();
        } catch (Throwable e) {
            reportWtf("making display ready", e);
        }
        traceEnd();
        
        ...
            
               traceBeginAndSlog("MakeWindowManagerServiceReady");
        try {
            // 通知 WMS，系统的初始化工作已经完成
            // 内部调用了 WindowManagerPolicy 的 systemReady 方法
            wm.systemReady();
        } catch (Throwable e) {
            reportWtf("making Window Manager Service ready", e);
        }
        traceEnd();
            
        ...
    }
```

　　startOtherServices 方法用于启动其他服务，其他服务大概有 70 多个，上面的代码只列出了 WMS 以及它相关的 IMS 的启动逻辑，剩余的其他服务的启动逻辑也都大同小异。

　　WMS 的 main 方法是运行在 SystemServer 的 run 方法中，换句话说就是运行在 “system_server” 线程中。

　　将 WMS 和 IMS 注册到 ServiceManager 中，如果某个客户端想要使用 WMS，就需要先去 ServiceManager 中查询信息，然后根据信息与 WMS 所在的进程建立通信通路，客户端就可以使用 WMS 了。

### 4. WindowManagerService#main

```java
    public static WindowManagerService main(final Context context, final InputManagerService im,
            final boolean haveInputMethods, final boolean showBootMsgs, final boolean onlyCore,
            WindowManagerPolicy policy) {
        // 调用了 DisplayThread 的 getHandler 方法，用来得到 DisplayThread 的 Handler 实例。
        DisplayThread.getHandler().runWithScissors(() ->
                                                   // 创建了 WMS 的实例。
                sInstance = new WindowManagerService(context, im, haveInputMethods, showBootMsgs,
                        onlyCore, policy), 0);
        return sInstance;
    }
```

　　DisplayThread 是一个单例的前台线程，这个线程用来处理需要低延时显示的相关操作，并只能由 WindowManager、DisplayManager 和 InputManager 实时执行快速操作。

　　runWithScissors 方法中使用了 Lambda 表达式，它等价于如下代码：

```java
    DisplayThread.getHandler().runWithScissors(new Runnable() {
            @Override
            public void run() {
             sInstance = new WindowManagerService(context, im, haveInputMethods, showBootMsgs,
                        onlyCore, policy);//2
            }
        }, 0);
```

　　创建 WMS 的过程是运行在 Runnable 的 run 方法中，而 Runnable 则传入到 DisplayThread 对应 Handler 的 runWithScissors 方法中，说明 WMS 的创建是运行在 “android:display” 线程中。需要注意的是，runWithScissors 方法的第二个参数传入的是 0.

### 5. Handler#runWithScissors

```java
    public final boolean runWithScissors(final Runnable r, long timeout) {
        if (r == null) {
            throw new IllegalArgumentException("runnable must not be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be non-negative");
        }
		// 判断当前线程是否是主线程
        if (Looper.myLooper() == mLooper) {
            r.run();
            return true;
        }

        BlockingRunnable br = new BlockingRunnable(r);
        return br.postAndWait(this, timeout);
    }
```

　　对传入的 Runnable 和 timeout 进行了判断，如果 Runnable 为 null 或者 timeout 小于 0 则抛出异常。

　　根据每个线程只有一个 Looper 的原理来判断当前的线程（“System_server” 线程）是否是 Handler 所指向的线程（“android.display” 线程），如果是则直接执行 Runnable 的 run 方法，如果不是则调用 BlockingRunnable 的 postAndWait 方法，并将当前线程的 Runnable 作为参数传进去。

### 6. BlockingRunnable

　　BlockingRunnable 是 Handler 的内部类。

```java
    private static final class BlockingRunnable implements Runnable {
        private final Runnable mTask;
        private boolean mDone;

        public BlockingRunnable(Runnable task) {
            mTask = task;
        }

        @Override
        public void run() {
            try {
                // 执行了传入的 Runnable 的 run 方法（运行在 “android.display” 线程）
                mTask.run();
            } finally {
                synchronized (this) {
                    mDone = true;
                    notifyAll();
                }
            }
        }

        public boolean postAndWait(Handler handler, long timeout) {
            // 将当前的 BlockingRunnable 添加到 Handler 的任务队列中
            if (!handler.post(this)) {
                return false;
            }

            synchronized (this) {
                if (timeout > 0) {
                    final long expirationTime = SystemClock.uptimeMillis() + timeout;
                    while (!mDone) {
                        long delay = expirationTime - SystemClock.uptimeMillis();
                        if (delay <= 0) {
                            return false; // timeout
                        }
                        try {
                            wait(delay);
                        } catch (InterruptedException ex) {
                        }
                    }
                    // time = 0 
                } else {
                    while (!mDone) {
                        try {
                            // 当前线程（system_server 线程）进入等待状态
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            return true;
        }
    }
```

　　BlockingRunnable 的 postAndWait 方法会把当前的 BlockingRunnable 添加到 Handler 的任务队列中，之前调用 postAndWait 方法传递的 timeout 为 0，如果 mDone 为 false 的话会一直调用 wait() 方法使得当前线程（“system_server” 线程）进入等待状态，那么等待的是哪个线程呢？在 BlockingRunnable 的 run 方法中执行了传入的 Runnable 的 run 方法（运行在 “android.display” 线程），执行完毕后在 finally 代码块中将 mDone 设置为 true，并调用 notifyAll 方法唤醒处于等待状态的线程，这样就不会继续调用 postAndWait  中的 wait 方法。

　　因此得出结论，“system_server” 线程等待的就是 “android.display” 线程，一直到 “android.display” 线程执行完毕再执行 “system_server” 线程，这是因为 “android.display” 线程内部执行了 WMS 的创建，显然 WMS 的创建优先级更高些。

### 7. WindowManagerService 的构造方法

```java
    private WindowManagerService(Context context, InputManagerService inputManager,
            boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore,
            WindowManagerPolicy policy) {
        
        ...
            
        // 保存传进来的 IMS，这样 WMS 就持有 IMS 的引用
        mInputManager = inputManager;
        
        ...
           
        mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        // 通过 DisplayManager 的 getDisplays 方法得到 Display 数组（每个显示设备都有一个 Display 实例）
        mDisplays = mDisplayManager.getDisplays();
        // 遍历 Display 数组
        for (Display display : mDisplays) {
            // 将 Display 封装成 DisplayContent,DisplayContent 用来描述一块屏幕
            createDisplayContentLocked(display);
        }

        ...

            // 得到 AMS 实例，并赋值给 mActivityManager，这样 WMS 就持有了 AMS 的引用
        mActivityManager = ActivityManager.getService();
        
        ...
        // 创建 WindowAnimator，它用于管理所有的窗口动画

        mAnimator = new WindowAnimator(this);

        mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromWindowLayout);


        LocalServices.addService(WindowManagerInternal.class, new LocalService());
        // 初始化了窗口管理策略的接口类 WindowManagerPolicy(WMP)，它用来定义一个窗口策略所有遵循的通用规范
        initPolicy();

        // Add ourself to the Watchdog monitors.
        // 将自身也就是 WMS 通过 addMonitor 方法添加到 Watdog 中。
        Watchdog.getInstance().addMonitor(this);

        ...
    }
```

　　Watchdog 用来监控系统的一些关键服务的运行状态（比如传入的 WMS 的运行状况），这些被监控的服务都会实现 Watchdog.Monitor 接口。Watchdog 每分钟都会对监控的系统服务进行检查，如果被监控的系统服务出现了死锁，则会杀死 Watchdog 所在的进程，也就是 SystemServer 进程。

### 8. WindowManagerService#initPolicy

```java
    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            @Override
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                // 执行 WMP 的 init 方法

                mPolicy.init(mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }
```

　　initPolicy 方法和 WMS 的 main 方法类似。WMP 是一个接口，init 方法的具体实现在 PhoneWindowManager（PWM）中。PWM 的 init 方法运行在 “android.ui” 线程中，它的优先级要高于 initPolicy 方法所在的 "android.display" 线程，因此 “android.display” 线程要等 PWM 的 init 方法执行完毕后，处于等待状态的 “android.display” 线程才会被唤醒从而继续执行下面的代码。

## 3. 三个线程之间的关系

　　“system_server”、“android.display” 和 “android.io” 这三个线程之间的关系：

![](image/三个线程的关系.png)

　　"system_server" 线程中会调用 WMS 的 main 方法，main 方法中会创建 WMS，创建 WMS 的过程运行在 “andriod.display” 线程中，它的优先级更高一些，因此要等创建 WMS 完毕后才会唤醒处于等待状态的 “system_server” 线程。

　　WMS 初始化时会执行 initPolicy 方法，initPolicy 方法会调用 PWM 的 init 方法，这个 init 方法运行在 "android.ui" 线程，并且优先级更高，因此要先执行 PWN 的 init 方法后，才能唤醒处于等待状态的 "android.display" 线程。

　　PWM 的 init 方法执行完毕后会接着执行运行在 "system_server" 线程的代码，比如前面提到的 WMS 的 systemReady 方法。

## 4. WMS 的重要成员

　　所谓 WMS 的重要成员是指 WMS 中的重要的成员变量：

```java
    final WindowManagerPolicy mPolicy;

    final IActivityManager mActivityManager;
    final ActivityManagerInternal mAmInternal;

    final AppOpsManager mAppOps;

    final DisplaySettings mDisplaySettings;

	...

    /**
     * All currently active sessions with clients.
     */
    final ArraySet<Session> mSessions = new ArraySet<>();

    /**
     * Mapping from an IWindow IBinder to the server's Window object.
     * This is also used as the lock for all of our state.
     * NOTE: Never call into methods that lock ActivityManagerService while holding this object.
     */
    final WindowHashMap mWindowMap = new WindowHashMap();

    /**
     * List of window tokens that have finished starting their application,
     * and now need to have the policy remove their windows.
     */
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList<>();

    /**
     * List of window tokens that have finished drawing their own windows and
     * no longer need to show any saved surfaces. Windows that's still showing
     * saved surfaces will be cleaned up after next animation pass.
     */
    final ArrayList<AppWindowToken> mFinishedEarlyAnim = new ArrayList<>();

    /**
     * List of app window tokens that are waiting for replacing windows. If the
     * replacement doesn't come in time the stale windows needs to be disposed of.
     */
    final ArrayList<AppWindowToken> mWindowReplacementTimeouts = new ArrayList<>();

    /**
     * Windows that are being resized.  Used so we can tell the client about
     * the resize after closing the transaction in which we resized the
     * underlying surface.
     */
    final ArrayList<WindowState> mResizingWindows = new ArrayList<>();

    /**
     * Windows whose animations have ended and now must be removed.
     */
    final ArrayList<WindowState> mPendingRemove = new ArrayList<>();

    /**
     * Used when processing mPendingRemove to avoid working on the original array.
     */
    WindowState[] mPendingRemoveTmp = new WindowState[20];

    /**
     * Windows whose surface should be destroyed.
     */
    final ArrayList<WindowState> mDestroySurface = new ArrayList<>();

    /**
     * Windows with a preserved surface waiting to be destroyed. These windows
     * are going through a surface change. We keep the old surface around until
     * the first frame on the new surface finishes drawing.
     */
    final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList<>();

	...
    final H mH = new H();

	...
        
    final WindowAnimator mAnimator;

	...
    final InputManagerService mInputManager;
```









## 参考文章

4. [Android 解析 WindowManagerService（一）WMS 的诞生](https://blog.csdn.net/itachi85/article/details/78186741)
5. [Android解析WindowManagerService（二）WMS的重要成员和Window的添加过程](https://blog.csdn.net/itachi85/article/details/78357437)
6. [Android解析WindowManagerService（三）Window的删除过程](https://blog.csdn.net/itachi85/article/details/79134490)