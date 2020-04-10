# ActivityManagerService

## 概述

　　AMS 是系统的引导服务，应用进程的启动、切换和调度、四大组件的启动和管理都需要 AMS 的支持。从这里可以看出 AMS 的功能会十分的繁多，当然它并不是一个类承担这个重责，它有一些关联类。

## AMS 的启动流程

　　AMS 的启动是在 SystemServer 进程中启动的，从 SystemServer 的 main 方法进入。

### SystemServer#main

```java
    public static void main(String[] args) {
        new SystemServer().run();
    }
```

　　main 方法中只调用了 SystemServer 的 run 方法。

### SystemServer#run

```java
    private void run() {
            ...

            // Initialize native services.
                // 加载了动态库 libandroid_servers.so
            System.loadLibrary("android_servers");

            ...

            // Create the system service manager.
                // 创建 SystemServiceMananger ,它会对系统的服务进行创建、启动和生命周期管理。
            mSystemServiceManager = new SystemServiceManager(mSystemContext);
            mSystemServiceManager.setRuntimeRestarted(mRuntimeRestart);
            LocalServices.addService(SystemServiceManager.class, mSystemServiceManager);
            ...

        // Start services.
        try {
            traceBeginAndSlog("StartServices");
            // 用 SystemServiceManager 启动 ActivityManangerService、PowerManangerService、PackageManangerService 等服务
            startBootstrapServices();
            // 启动 BatteryService、UsageStatsService 和 WebViewUpdateService
            startCoreServices();
            // 启动 CameraService、AlarmManagerService、VrManangerService 等服务。
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
    }
```

　　SystemServer 的 run 方法中调用 startBootstrapServices、startCoreServices、startOtherServices 三个方法去启动服务，这些服务的父类均为 SystemService。官方把系统服务分为了三种类型，分别是引导服务、核心服务和其他服务，其中其他服务是一些非紧要和一些不需要立即启动的服务。系统服务总共大约有 80多个。

### SystemServer#startBootstrapServices

```java
    private void startBootstrapServices() {
        ...

        // Activity manager runs the show.
        traceBeginAndSlog("StartActivityManager");
                // 调用了 SystemServiceMananger 的 startService 方法，参数是 ActivityManagerService.Lifecycle.class
        mActivityManagerService = mSystemServiceManager.startService(
                ActivityManagerService.Lifecycle.class).getService();
        mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
        mActivityManagerService.setInstaller(installer);
        traceEnd();

        ...
    }
```

　　在 SystemServer 的 startBootstrapService 方法中调用了 SystemServiceMananger 的 startService 方法，参数是 ActivityManagerService.Lifecycle.class。

### SystemServiceManager#startService

```java
    public <T extends SystemService> T startService(Class<T> serviceClass) {
        try {
            ...

            // Create the service.
            ...
            final T service;
            try {
                // 得到传进来的 Lifecycle 的构造器 constructor
                Constructor<T> constructor = serviceClass.getConstructor(Context.class);
                // 创建 Lifecycle 类型的 service 对象
                service = constructor.newInstance(mContext);
            } catch (xxxException ex) {
                ...
            }

            startService(service);
            // 返回 service
            return service;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_SYSTEM_SERVER);
        }
    }

    public void startService(@NonNull final SystemService service) {
        // Register it.
        // 将创建的 service 添加到 ArrayList 类型的 mServices 对象中完成注册
        mServices.add(service);
        // Start it.
        long time = System.currentTimeMillis();
        try {
            // 调用 service 的 onStart 方法来启动 service
            service.onStart();
        } catch (RuntimeException ex) {
            ...
        }
        ...
    }
```

　　startService 方法传入的参数是 Lifecycle.class，Lifecycle 继承自 SystemService。

　　首先通过反射创建 Lifecycle 实例：先得到 Lifecycle 的构造器 constructor，然后调用 constructor 的 newInstance 方法来创建 Lifecycle 类型的 service 对象。

　　接着将刚创建的 service 添加到 ArrayList 类型的 mService 对象中完成注册。

　　然后调用 service 的 onStart 方法来启动 service。

　　最后返回该 service。

### Lifecycle

　　Lifecycle 是 ActivityManagerService 的内部类。

```java
    public static final class Lifecycle extends SystemService {
        private final ActivityManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            // 创建 ActivityManagerService 实例
            mService = new ActivityManagerService(context);
        }

        @Override
        public void onStart() {
            // 调用 ActivityManagerService 的 start 方法
            mService.start();
        }

        public ActivityManagerService getService() {
            // 返回 ActivityManagerService 实例
            return mService;
        }
    }
```

　　在 SystemServiceManager  的 startService 方法中通过反射创建了 Lifecycle 实例的时候，会调用 Lifecycle 的构造方法，在里面会创建 AMS 实例。

　　在 SystemServiceManager  的 startService 方法中调用 Lifecycle 类型的 service 的 onStart 方法时，实际上时调用了 AMS 的 start 方法。

　　在 Systemserver 的 startBootstrapServices 方法中调用的 `mActivityManagerService = mSystemServiceManager.startService(
                ActivityManagerService.Lifecycle.class).getService();` 就是 ActivityManagerService 的对象。

### 总结

　　SystemServiceMananger 的 startService 方法最终会返回 Lifecycle 的 getService 方法，这个方法会返回 AMS 类型的 mService 对象，这样 AMS 实例就会被创建并且返回。

## AMS 与进程启动

　　要启动一个应用程序，首先要保证这个应用程序所需要的应用程序进程已经被启动。

　　AMS 在启动应用程序时会检查这个应用程序需要的应用程序进程是否存在，不存在就会请求进程将需要的应用程序进程启动。

　　Service 的启动过程中会调用 ActiveServices 的 bringUpServiceLocked 方法。

### ActiveServices#bringUpServiceLocked

```java
    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg,
            boolean whileRestarting, boolean permissionsReviewRequired)
            throws TransactionTooLargeException {
        ...
         

        // 得到 ServiceRecord 的 processName 的值赋值给 procName
        // 其中 ServiceRecord 用来描述 Service 的 android:process 属性
        final String procName = r.processName;
        String hostingType = "service";
        ProcessRecord app;

        if (!isolated) {
            // 将 procName 和 Service 的 uid 传入到 AMS 的 getProcessRecordLocked 方法中，来查询是否存在一个与 Service 对应的 ProcessRecord 类型的对象 app
            // ProcessRecord 主要用来记录运行的应用程序进程的信息
            app = mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
            if (DEBUG_MU) Slog.v(TAG_MU, "bringUpServiceLocked: appInfo.uid=" + r.appInfo.uid
                        + " app=" + app);
            if (app != null && app.thread != null) {
                try {
                    app.addPackage(r.appInfo.packageName, r.appInfo.versionCode, mAm.mProcessStats);
                    realStartServiceLocked(r, app, execInFg);
                    return null;
                } catch (TransactionTooLargeException e) {
                    throw e;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception when starting service " + r.shortName, e);
                }

                // If a dead object exception was thrown -- fall through to
                // restart the application.
            }
        } else {
            // If this service runs in an isolated process, then each time
            // we call startProcessLocked() we will get a new isolated
            // process, starting another process if we are currently waiting
            // for a previous process to come up.  To deal with this, we store
            // in the service any current isolated process it is running in or
            // waiting to have come up.
            app = r.isolatedProc;
            if (WebViewZygote.isMultiprocessEnabled()
                    && r.serviceInfo.packageName.equals(WebViewZygote.getPackageName())) {
                hostingType = "webview_service";
            }
        }

        // Not running -- get it started, and enqueue this service record
        // to be executed when the app comes up.
        // 判断 service 对应的 app 为 null 则说明用来运行 Service 的 应用进程不存在
        if (app == null && !permissionsReviewRequired) {
            // 调用 AMS 的 startProcessLocked 方法来创建对应的应用程序进程。
            if ((app=mAm.startProcessLocked(procName, r.appInfo, true, intentFlags,
                    hostingType, r.name, false, isolated, false)) == null) {
                String msg = "Unable to launch app "
                        + r.appInfo.packageName + "/"
                        + r.appInfo.uid + " for service "
                        + r.intent.getIntent() + ": process is bad";
                Slog.w(TAG, msg);
                bringDownServiceLocked(r);
                return msg;
            }
            if (isolated) {
                r.isolatedProc = app;
            }
        }
        
        ...
    }
```





## AMS 家族

　　ActivityManager 是一个和 AMS 相关联的类，它主要对运行中的 Activity 进行管理，这些管理工作并不是由 ActivityManager 来处理的，而是交由 AMS 来处理，ActivityManager 中的方法就会通过 ActivityManagerNative（AMN）的 getDefault 方法来得到 ActivityManagerProxy（AMP），通过 AMP 就可以和 AMN 进行通信，而 AMN 是一个抽象类，它会将功能交由它的子类 AMS 来处理，因此，AMP 就是 AMS 的代理类。AMS 作为系统核心服务，很多 API 是不会暴露给 ActivityManager 的，因此 ActivityManager 并不算 AMS 家族一份子。

　　以 Activity 的启动过程举例，Activity 的启动过程中会调用 Instrumentation 的 execStartActivity 方法。

### Instrumentation#execStartActivity

```java
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        ...
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
```

　　execStartActivity 方法中会调用 AMN 的 getDefault 来获取 AMS 的代理类 AMP，接着调用了 AMP 的 startActivity 方法。

#### ActivityManagerNative#getDefault

```java
    static public IActivityManager getDefault() {
        return gDefault.get();
    }

    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
          	// 得到名为 “activity” 的 Service 引用，也就是 IBinder 类型 的 AMS 的引用
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
          	// 将 b 封装成 AMP 类型对象，并将它保存到 gDefault 中
          	// 此后调用 AMN 的 getDefault 方法就会直接获得 AMS 的代理对象 AMP
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };
```

　　getDefault 方法调用了 gDefault 的 get 方法，gDefault 是一个 Singleton 类。最后返回 AMP 对象。

##### ActivityManagerNative#asInterface

```java
    static public IActivityManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IActivityManager in =
            (IActivityManager)obj.queryLocalInterface(descriptor);
        if (in != null) {
            return in;
        }

        return new ActivityManagerProxy(obj);
    }
```

　　asInterface 方法的主要作用就是将 IBinder 类型的 AMS 引用封装成 AMP。

##### ActivityManagerProxy 的构造方法

　　ActivityManagerProxy 是 ActivityManagerNative 的内部类。

```java
class ActivityManagerProxy implements IActivityManager
{
    public ActivityManagerProxy(IBinder remote)
    {
        mRemote = remote;
    }
}
```

　　AMP 的构造方法中将 AMS 的引用赋值给变量 mRemote，这样在 AMP 中就可以使用 AMS 了。

　　其中 IActivityManager 是一个接口，AMN 和 AMP 都实现了这个接口，用于实现代理模式和 Binder 通信。

#### ActivityManagerProxy#startActivity

　　再回到 Instrumentation 的 execStartActivity 方法，查看 AMP 的 startActivity 方法。

　　AMP 是 AMN 的内部类。

```java
    public int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
            String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        ...
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        ...
        // 通过 IBinder 类型对象 mRemote 向服务端 AMS 发送一个 START_ACTIVITY_TRANSACTION 类型的进程间通信请求
        mRemote.transact(START_ACTIVITY_TRANSACTION, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

```

　　首先会将传入的参数写入到 Parcel 类型的 data 中，通过 IBinder 类型对象 mRemote（AMS 的引用）向服务端的 AMS 发送一个 START_ACTIVITY_TRANSACTION 类型的进程间通信请求。那么服务端 AMS 就会从 Binder 线程池中读取客户端发送来的数据，最终会调用 AMN 的 onTransact 方法。

##### ActiveManagerNative#onTransact

```java
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        switch (code) {
        case START_ACTIVITY_TRANSACTION:
        {
            ...
            int result = startActivity(app, callingPackage, intent, resolvedType,
                    resultTo, resultWho, requestCode, startFlags, profilerInfo, options);
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        }
        ...
    }
      
    return super.onTransact(code, data, reply, flags);
}
```

　　ActivityManagerNative 的 onTransact 方法中会调用 AMS 的 startActivity 方法。

##### ActivityManagerService#startActivity

```java
    @Override
    public final int startActivity(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions,
                UserHandle.getCallingUserId());
    }
```

　　ActivityManagerService 的 startActivity 会 return startActivityAsUser 方法。

##### ActivityManagerService#startActivityAsUser

```java
    @Override
    public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivity");
        userId = mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(),
                userId, false, ALLOW_FULL_ONLY, "startActivity", null);
        // TODO: Switch to user app stacks here.
        return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
                resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
                profilerInfo, null, null, bOptions, false, userId, null, null);
    }
```

　　startActivityAsUser 方法最后会 return mActivityStarter.startActivityMayWait 方法。

### 总结

　　在 Activity 的启动过程中提到了 AMP、AMN 和 AMS ，它们共同组成了 AMS 家族的主要部分，如下图所示：

![](image/AMS家族.png)

　　AMP 是 AMN 的内部类，它们都实现了 IActivityManager 接口，这样它们就可以实现代理模式。

　　具体来讲是远程代理：AMP 和 AMN 都是运行在两个进程的，AMP 是 Client 端，AMN 则是 Server 端，而 Server 端中具体的功能都是由 AMN 的子类 AMS 来实现的，因此，AMP 就是 AMS 在 Client 端的代理类。AMN 又实现了 Binder 类，这样 AMP 可以和 AMS 通过 Binder 来进行进程间通信。

　　ActivityManager 通过 AMN 的 getDefault 方法得到 AMP，通过 AMP 就可以和 AMN 进行通信，也就是间接的与 AMS 进行通信。

　　除了 ActivityManager ，其他想要与 AMS 进行通信的类都需要通过 AMP，如下图：

![](image/AMS通信.png)

## ActivityStack

　　ActivityStack 是一个管理类，用来管理系统所有 Activity 的各种状态，其内部维护了 TaskRecord 的列表，因此从 Activity 任务栈这一角度来说，ActivityStack 也可以理解为 Activity 堆栈。它由 ActivityStackSupervicor 来进行管理的，而 ActivityStackSupervisor 在 AMS 中的构造方法中被创建。

### ActivityManagerService 的构造方法

```java
    public ActivityManagerService(Context systemContext) {
                mStackSupervisor = new ActivityStackSupervisor(this);
    }
```

### ActivityStack 的实例类型

　　ActivityStackSupervisor 中有多种 ActivityStack 实例。

```java
public class ActivityStackSupervisor extends ConfigurationContainer implements DisplayListener {
	/** The stack containing the launcher app. Assumed to always be attached to
     * Display.DEFAULT_DISPLAY. */
    ActivityStack mHomeStack;

    /** The stack currently receiving input or launching the next activity. */
    ActivityStack mFocusedStack;
 
    /** If this is the same as mFocusedStack then the activity on the top of the focused stack has
     * been resumed. If stacks are changing position this will hold the old stack until the new
     * stack becomes resumed after which it will be set to mFocusedStack. */
    private ActivityStack mLastFocusedStack;
	...
}
```

　　mHomeStack 用来存储 Launcher App 的所有 Activity，mFocusedStack 表示当前正在接收输入或启动下一个 Activity 的所有 Activity。mLastFocusStack 表示此前接收输入的所有 Activity。

　　通过 ActivityStackSupervisor 提供了获取上述 ActivityStack 的方法，比如要获取 mFocusStack，主要调用 ActivityStackSupervisor 的 getFocusedStack 方法就可以了：

 ```java
    ActivityStack getFocusedStack() {
        return mFocusedStack;
    }
 ```

### ActivityState

　　ActivityState 是 ActivityStack 的内部类。

　　ActivityState 中通过枚举存储了 Activity 的所有状态，如下：



　　通过名称可以很轻易的知道这些状态所代表的意义。应用 ActivityState 的场景有很多，比如：

```java
public class ActivityManagerService extends IActivityManager.Stub
        implements Watchdog.Monitor, BatteryStatsImpl.BatteryCallback {
	@Override
    public void overridePendingTransition(IBinder token, String packageName,
            int enterAnim, int exitAnim) {
        synchronized(this) {
            ...

                // 只有 ActivityState 为 RESUMNED 状态或者 PAUSING 状态时才会调用 WMS 类型的 mWindowManager 对象的 overridePendingAppTransition 方法来进行切换动画
            if (self.state == ActivityState.RESUMED
                    || self.state == ActivityState.PAUSING) {
                mWindowManager.overridePendingAppTransition(packageName,
                        enterAnim, exitAnim, null);
            }

            Binder.restoreCallingIdentity(origId);
        }
    }
}
```

　　overridePendingTransition 方法用于设置 Activity 的切换动画。

### 特殊状态的 Activity

　　在  ActivityStack 中定义了一些特殊状态的 Activity，如下所示：



　　这些特殊的状态都是 ActivityRecord 类型的，ActivityRecord 用来记录一个 Activity 的所有信息。从 Activity 任务栈的角度来说，一个或多个 ActivityRecord 会组成一个 TaskRecord，TaskRecord 用来记录 Activity 的栈，而 ActivityStack 包含了一个或多个 TaskRecord。

![](image/ActivityStack.png)

### 维护的 ArrayList

　　ActivityStack 中维护了很多 ArrayList，这些 ArrayList 中的元素类型主要有 ActivityRecord 和 TaskRecord，其中 TaskRecord 用来记录 Activity 的 Task。

| ArrayList          | 元素类型       | 说明                                                        |
| ------------------ | -------------- | ----------------------------------------------------------- |
| mTaskHistory       | TaskRecord     | 所有没有被销毁的 Task                                       |
| mLRUActivities     | ActivityRecord | 正在运行的 Activity，列表中的第一个条目是最近最少使用的元素 |
| mNoAnimActivity    | ActivityRecord | 不考虑转换动画的 Activity                                   |
| mValidateAppTokens | TaskGroup      | 用于与窗口管理器验证应用令牌                                |

## Activity 栈管理

　　Activity 是由任务栈来进行管理的，有了栈管理，就可以对应用程序进行操作，应用可以复用自身应用中以及其他应用的 Activity，节省了资源。

　　为了更灵活的进行栈管理，Android 系统提供了很多配置，下面分别对他们进行介绍。

### Launch Mode

　　Launch Mode 都不会陌生，用于设定 Activity 的启动方式，无论是哪种启动方式，所启动的 Activity 都会位于 Activity 栈的栈顶。

　　Launch Mode 有以下四种：

1. standerd：标准模式，每次启动 Activity 都会创建一个新的 Activity 实例。
2. singleTop：如果要启动的 Activity 已经在栈顶，则不会重新创建 Activity，同时该 Activity 的  onNewIntent 方法会被调用。如果要启动的 Activity 不在栈顶，则会重新创建该 Activity 的实例。
3. singleTask：如果要启动的 Activity 已经存在于它想要归属的栈中，那么不会创建该 Activity 实例，将栈中位于该 Activity 上的所有的 Activity 出栈，同时该 Activity 的 onNewIntent 方法会被调用。如果要启动的 Activity 不存在于它想要归属的栈中，并且该栈存在，则会重新创建该 Activity 的实例。如果要启动的 Activity 想要归属的栈不存在，则首先要创建一个新栈，然后创建该 Activity 实例并压入到新栈中。
4. singleInstance：和 singleTask 基本类似，不同的是启动 Activity 时，首先要创建在一个新栈，然后创建该 Activity 实例并压入新栈中，新栈中只会存在这一个 Activity 实例。

### Intent 的 FLAG

　　Intent 中定义了很多 FLAG，其中有几个 FLAG 也可以设定 Activity 的启动方式，如果 Launch Mode 设定和 FLAG 设定的 Activity 的启动方式有冲突，则以 FLAG 设定的为准。

1. FLAG_ACTIVITY_SINGLE_TOP：和 Launch Mode 中的 singleTop 效果是一样的。
2. FLAG_ACTIVITY_NEW_TASK：和 Launch Mode 中的 singleTask 效果是一样的。
3. FLAG_ACTIVITY_CLEAR_TOP：Launch Mode 中没有与此对应的模式，如果要启动的 Activity 已经存在于栈中，则将所有位于它上面的 Activity 出栈。singleTask 默认具有此标记位的效果。

　　除了这三个 FLAG，还有一些 FLAG 对分析栈管理有些帮助。

1. FLAG_ACTIVITY_NO_HISTORY：Activity 一旦退出，就不会存在于栈中。同样的，也可以在 AndroidManifest.xml 中设置 “android:noHistory”。
2. FLAG_ACTIVITY_MULTIPLE_TASK：需要和 FLAG_ACTIVITY_NEW_TASK 一同使用才有效果，系统会启动一个新的栈来容纳新启动的 Activity。
3. FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS：Activity 不会被放入到 “ 最近启动的 Activity ” 列表中。
4. FLAG_ACTIVITY_BROUGHT_TO_FRONT：这个标志位通常不是由应用程序中的代码设置的，而是 Launch Mode 为 singleTask 时，由系统自动加上的。
5. FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY：这个标志为通常不是由应用程序中的代码设置的，而是从历史记录中启动的（长按 Home 键调出）。
6. FLAG_ACTIVITY_CLEAR_TASK：需要和 FLAG_ACTIVITY_NEW_TASK 一同使用才有效果，用于清除与启动的 Activity 相关栈的所有其他 Activity。

#### ActivityStarter#startActivityUnchecked

　　根 Activity 启动时会调用 AMS 的 startActivity 方法，经过层层调用会调用 ActivityStarter 的 startActivityUnchecked 方法，如下图的时序图所示：

![](image/activity启动时序图.png)



```java
class ActivityStarter {
    private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity) {
        // 用于初始化启动 Activity 的各种配置
        // 在初始化前会重置各种配置再进行配置
        // 这些配置包括：ActivityRecord、Intent、TaskRecord 和 LaunchFlags（启动的 FLAG）等等

        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession,
                voiceInteractor);
        // 用于计算出启动的 FLAG，并将计算的值赋值给 mLaunchFlags。

        computeLaunchingTaskFlags();

        computeSourceStack();

        // 将 mLaunchFlags 设置为 Intent，达到设定 Activity 的启动方式的目的。
        mIntent.setFlags(mLaunchFlags);
        ...
    }
}
```

##### ActivityStarter#computeLaunchingTaskFlags

```java
    private void computeLaunchingTaskFlags() {
        ...

        // TaskRecord 类型的 mInTask 为 null 时，说明 Activity 要加入的栈不存在，因此，这一小段代码主要解决的问题就是 Activity 要加入的栈不存在时如何计算出启动的 FLAG。
        if (mInTask == null) {
            // ActivityRecord 类型的 mSourceRecord 用于描述 “ 初始化 Activity ”
            // 初始化 Activity：ActivityA 启动了 ActivityB，ActivityA 就是初始 Activity
            if (mSourceRecord == null) {
                // This activity is not being started from another...  in this
                // case we -always- start a new task.
                // 同时满足 mSourceRecord == null 和下面的条件则需要创建一个新栈
                if ((mLaunchFlags & FLAG_ACTIVITY_NEW_TASK) == 0 && mInTask == null) {
                    Slog.w(TAG, "startActivity called from non-Activity context; forcing " +
                            "Intent.FLAG_ACTIVITY_NEW_TASK for: " + mIntent);
                    mLaunchFlags |= FLAG_ACTIVITY_NEW_TASK;
                }
                // 如果 “ 初始 Activity ” 所在的栈只允许有一个 Activity 实例，则也需要创建一个新栈
            } else if (mSourceRecord.launchMode == LAUNCH_SINGLE_INSTANCE) {
                // The original activity who is starting us is running as a single
                // instance...  this new activity it is starting must go on its
                // own task.
                mLaunchFlags |= FLAG_ACTIVITY_NEW_TASK;
                // 如果 Launch Mode 设置了 singleTask 或 singleInstance，则也要创建一个新栈。
            } else if (mLaunchSingleInstance || mLaunchSingleTask) {
                // The activity being started is a single instance...  it always
                // gets launched into its own task.
                mLaunchFlags |= FLAG_ACTIVITY_NEW_TASK;
            }
        }
    }
```

### taskAffinity

　　可以在 AndroidManifest.xml 设置 android:taskAffinity，用来指定 Activity 希望归属的栈，默认情况下，同一个应用程序的所有的 Activity 都有着相同的 taskAffinity。

　　taskAffinity 在下面两种情况下会产生效果：

1. taskAffinity 与 FLAG_ACTIVITY_NEW_TASK 或者 singleTask 配合。如果新启动 Activity 的 taskAffinity 和栈的 taskAffinity 相同（栈的 taskAffinity 取决于根 Activity 的 taskAffinity）则加入到该栈中。如果不同，就会创建新栈。
2. taskAffinity 与 allowTaskReparenting 配合，如果 allowTaskReparenting 为 true，说明 Activity 具有转移的能力。如果 ActivityA 启动 ActivityB，ActivityB 的 allowTaskReparenting 为 false，ActivityA 和 ActivityB 就处于同一个栈中。如果 ActivityB 的 allowTaskReparenting 设置为 true，此后 ActivityB 所在的栈位于前台，这是 AcvitiyB 就会由 ActivityA 的栈中转移到与它更亲近的 ActivityB（taskAffinity相同）所在的栈中。

#### ActivityStack#findTaskLocked

　　ActivityStackSupervisor 的 findTaskLocked 方法用于找到 Activity 最匹配的栈，最终会调用 ActivityStack 的 findTaskLocked 方法。



　　上面的代码只是与 taskAffinity 相关的部分。

## 参考文章

1. [请从 AMS、WMS、PMS 的角度考虑，以及继承是如何启动的？开机 SystemServer 到 ActivityManagerService 启动过程](https://www.cnblogs.com/sunkeji/articles/7650482.html)
2. [Android 解析 ActivityManagerService（一）AMS 启动流程和 AMS 家族](https://blog.csdn.net/itachi85/article/details/76405596)
3. [Android解析ActivityManagerService（二）ActivityTask和Activity栈管理](https://blog.csdn.net/itachi85/article/details/77542286)