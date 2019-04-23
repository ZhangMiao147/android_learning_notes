# Activity 知识之三：启动流程

	 本文内容


## 热启动与冷启动
　　所谓冷启动就是启动该应用时，后台没有该应用的进程，此时系统会创建一个进程分配给它，之后会创建和初始化 Application，然后通过反射执行 ActivityThread 中的 main 方法。而热启动则是，当启动应用的时候，后台已经存在该应用的进程，比如按 home 键返回主界面再打开该应用，此时会从已有的进程中来启动应用，这种方式下，不会重新走 Application 这一步。


## 启动任务
　　通过为 Activity 提供一个以 “android.intent.action.MAIN” 为指定操作、以 “android.intent.category.LAUNCHER” 为执行类别的 Intent 过滤器，可以将 Activity 设置为任务的入口点。

　　此类 Intent 过滤器会使 Activity 的图标和标签显示在应用启动器中，让用户能够启动 Activity 并在启动之后随时返回到创建的任务中。

　　用户必须能够在离开任务后，再使用此 Activity 启动器返回该任务。因此，只有在 Activity 具有 ACTION_MAIN 和  CATEGORY_LAUNCHER 过滤器时，才应该使用将 Activity 标记为“始终启动任务“的两种启动模式，即 ”singleTask“ 和 ”singleInstance“。

　　如果并不想用户能够返回到 Activity ，对于这些情况，可以将 < activity > 元素的 finishOnTaskLaunch 设置为 ”true“ 。

## Activity 的启动流程图
![](./Activity启动流程图.jpg)

## 开始：ActivtyThread.java 中的 main() 方法
　　Android 中，一个应用程序的开始可以说就是从 ActivityThread.java 中的 main() 方法开始的。

　　从 Activity 的启动流程图可以看到，main() 方法中主要做的事情有：

1. 初始化主线程的 Looper、主 Handler 。并使主线程进入等待接收 Message 消息的无限循环状态。下面是 main() 方法中比较关键的代码：
```
public static void main(String[] args) {
		...
		//初始化 Looper
        Looper.prepareMainLooper();

		...

		//实例化一个 ActivityThread 对象
        ActivityThread thread = new ActivityThread();
		//这个方法最后就是为了发送出创建 Application 的消息
        thread.attach(false);

		...

        //主线程进入无限循环状态，等待接收消息
        Looper.loop();
		...
    }
```

2. 调用 attach() 方法，主要就是为了发送出初始化 Application 的消息。

## 创建 Application 的消息是如何发送的呢？
　　在 ActivityThread 类中的 main() 方法中调用了 attach() 方法，这个方法最终的目的是发送一条创建 Application 的消息-H.BIND_APPLICATION 到主线程的主 Handler 中。

　　attach() 方法的关键代码：
```
    private void attach(boolean system) {
        ...
        if (!system) {
            ..
		    /获得 IActivityManager 的实例
            final IActivityManager mgr = ActivityManagerNative.getDefault();
			//调用 IActivityManager 的 attachApplication() 方法，传递 mAppThread 对象
            try {
                mgr.attachApplication(mAppThread);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
            ...
        } else {
            ...
        }
		...
    }

```

#### IActivityManager mgr 是什么？
　　从 attach() 方法中可以看到 IActivityManager 是一个接口，当调用 ActivityManagerNative.getDefault() 获得的实际是一个代理类的实例-ActivityManagerProxy，ActivityManagerProxy 实现了 IActivityManager 接口。而 ActivityManagerProxy 是 ActivityManagerNative 的一个内部类。

1. ActivityManagerProxy 的构造函数：
```
    public ActivityManagerProxy(IBinder remote)
    {
        mRemote = remote;
    }
```
　　ActivityManagerProxy 的构造函数非常的简单，首先它需要一个 IBinder 参数，然后赋值给 mRemote 变量。这个 mRemote 是 ActivityManagerProxy 的成员变量，对它的操作是由 ActivityManagerProxy 来代理间接进行的。这样设计的好处是保护了 mRemote，并且能够在操作 mRemote 前执行一些别的事务，并且是以 ActivityManager 来进行这些操作的。

2. ActivityManagerProxy 的构造函数是在哪里调用的？
```
    static public IActivityManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IActivityManager in =
            (IActivityManager)obj.queryLocalInterface(descriptor);
		//先检查有没有
        if (in != null) {
            return in;
        }
		//调用 ActivityManagerProxy 的构造函数
        return new ActivityManagerProxy(obj);
    }
```
　　asInterface() 方法是 ActivityManagerNative 中的一个静态方法，它会调用到 ActivityManagerProxy 的构造方法。然而，这个静态方法也需要一个 IBinder 作为参数。

3. asInsterface() 方法是在哪里调用的？
```
    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
			//IBinder 实例是在这里获取的
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
			//调用了 asInterface() 方法
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };
```
　　gDefault 是 ActivityManagerNative 的静态常量，是一个单例。在启动找到了 IBinder 实例。

　　这里是通过 ServiceManager 获取到 IBinder 实例的。如果了解 AIDL 通讯流程的话，就会理解这只是通过另一种方式获取 IBinder 实例罢了。获取 IBinder 的目的就是为了通过这个 IBinder 和 ActivityManager 进行通讯，进而 ActivityManager 会调度发送 H.BIND_APPLICATION 即初始化 Application 的 Message 消息。

4. attachApplication(mAppThread) 方法
```
    public void attachApplication(IApplicationThread app) throws RemoteException
    {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app.asBinder());

        mRemote.transact(ATTACH_APPLICATION_TRANSACTION, data, reply, 0);

        reply.readException();
        data.recycle();
        reply.recycle();
    }
```
　　attachApplication 方法调用了 IBinder 实例的 transact() 方法，并且把参数 app 放到了 data 中，最终传递给了 ActivityManager 。

　　IActivityManager 是一个接口，ActivityManagerProxy 实现了 IActivityManager 接口，ActivityManagerProxy 主要代理了内核中与 ActivityManager 通讯的 Binder 实例。

#### ApplicationThread mAppThread 又是什么？

1. 在 ActivityThread 的成员变量中对发现 mAppThread
```
final ApplicationThread mAppThread = new ApplicationThread();
```
　　ApplicationThread 是作为 ActivityThread 中的一个常量出现的。这表明系统不喜欢这个变量中途被修改，可见这个变量具有特定而十分重要的作用。

2. 查看 ApplicationThread 类
```
    private class ApplicationThread extends ApplicationThreadNative {
		...
    }
```
　　ApplicationThread 是 ActivityThread 中的一个内部类，并且 ApplicationThread 是继承 ApplicationThreadNative 类的。

3. 查看 ApplicationThreadNative 类
```
public abstract class ApplicationThreadNative extends Binder
        implements IApplicationThread {
    ...
	//无参构造函数
    public ApplicationThreadNative() {
		//这是 Binder 的方法
        attachInterface(this, descriptor);
    }
}
```
　　可以看到 ApplicationThreadNative 继承于 Binder ，所以 ApplicationThreadNative 是一个 Binder，同时也实现了 IAPPlicationThread 接口，所以 ApplicationThreadNative 也是一个 IApplicationThread 。

4. 查看 attachInterface() 方法
```
    public void attachInterface(IInterface owner, String descriptor) {
        mOwner = owner;
        mDescriptor = descriptor;
    }
```
　　attachInterface() 方法没有什么，只是简单的赋值。

5. 查看 IApplicationThread 类
```
public interface IApplicationThread extends IInterface {
    ...
    String descriptor = "android.app.IApplicationThread";
	...
```
　　IApplicationThread 实现了 IInterface 接口，

　　ApplicationThread 作为 IApplicationThread 的一个实例，承担了最后发送 Activity 生命周期以及它一些消息的任务，也就是说发送消息。

　　至于为什么在 ActivityThread 中已经创建出了 ApplicationThread 了还要绕弯路发消息，是为了让系统根据情况来控制这个过程。

#### ActivityManagerService 调度发送初始化消息
　　ActivityManagerService 的 attachApplicationLocked() 方法
```
    private final boolean attachApplicationLocked(IApplicationThread thread,
            int pid) {
		...
            thread.bindApplication(processName, appInfo, providers, app.instrumentationClass,
                    profilerInfo, app.instrumentationArguments, app.instrumentationWatcher,
                    app.instrumentationUiAutomationConnection, testMode,
                    mBinderTransactionTrackingEnabled, enableTrackAllocation,
                    isRestrictedBackupMode || !normalMode, app.persistent,
                    new Configuration(mConfiguration), app.compat,
                    getCommonServicesLocked(app.isolated),
                    mCoreSettingsObserver.getCoreSettingsLocked());
        ...
    }
```

　　ApplicationThread （是 ActivityThread 的内部类）以 IApplicationThread 的身份到了 ActivityManagerService 中，经过一系列的操作，最终被调用了自己的 bindApplication() 方法，发出初始化 Application 的消息。
```
        public final void bindApplication(String processName, ApplicationInfo appInfo,
                List<ProviderInfo> providers, ComponentName instrumentationName,
                ProfilerInfo profilerInfo, Bundle instrumentationArgs,
                IInstrumentationWatcher instrumentationWatcher,
                IUiAutomationConnection instrumentationUiConnection, int debugMode,
                boolean enableBinderTracking, boolean trackAllocation,
                boolean isRestrictedBackupMode, boolean persistent, Configuration config,
                CompatibilityInfo compatInfo, Map<String, IBinder> services, Bundle coreSettings) {           ...
				sendMessage(H.BIND_APPLICATION, data);
        }
```
　　在方法中发出了一条 H.BIND_APPLICATION  消息，接着程序开始了。

#### 收到初始化消息之后
　　在 bindApplication() 方法中发送 H.BIND_APPLICATION  消息之后，接收到这个消息就开始创建 Application 了。这个过程是在 handleBindApplication() 中完成的。
```
    private void handleBindApplication(AppBindData data) {
       ...
	   	  // 通过反射初始化一个 Instrumentation 仪表。
          final ClassLoader cl = instrContext.getClassLoader();
          mInstrumentation = (Instrumentation)
          cl.loadClass(data.instrumentationName.getClassName()).newInstance();
		...
            // 通过 LoadedApp 命令创建 Application 实例
            Application app = data.info.makeApplication(data.restrictedBackupMode, null);
            mInitialApplication = app;
			...

         try {
  //让 mInstrumentation 调用 Application 的 onCreate() 方法
  				mInstrumentation.callApplicationOnCreate(app);
            } catch (Exception e) {
                ...
            }
        } finally {
        	...
        }
    }
```

#### Instrumentation 仪表是什么？
　　Instrumentation 会在应用程序的任何代码运行之前被实例化，它能够允许你监视应用程序和系统的所有交互。

###### Instrumentation 是如何实现监视应用程序和系统交互的？
　　Instrumentation 类将 Application 的创建、Activity 的创建以及生命周期这些操作包装起来，通过操作 Instrumentation 进而实现上述操作。

###### Instrumentation 封装有什么好处？
　　Instrumentation 作为抽象，放约定好需要实现的功能之后，只需要给 Instrumentation 仪表添加这些抽象功能，然后调用就好了。关于怎么实现这些功能，都会交给 Instrumentation 仪器的实现对象就好了。这就是多态的运用，依赖抽象，不依赖具体的实践。就是上层提出需求，底层定义接口，即依赖倒置原则的践行。

　　在代码里，实例化 Instrumentation 方法的反射，而反射的 ClassName 是来自于从 ActivityManagerService 中传过来的 Binder 的，就是为了隐藏具体的实现对象。但是这样耦合性会很低。

###### 查看 callApplicationOnCreate() 的方法
```
    public void callApplicationOnCreate(Application app) {
        app.onCreate();
    }
```
　　只是调用了一个 Application 的 onCreate() 方法。

#### LoadedApk 就是 data.info
　　查看 makeApplication 的方法：
```
    public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
        ...
        String appClass = mApplicationInfo.className;
        ...
        ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
		//通过仪表创建 Application
        app = mActivityThread.mInstrumentation.newApplication(cl, appClass, appContext);
        ...
    }
```
　　从方法中看到：在取得 Application 的实际类名之后，最后的创建工作还是交由 Instrumentation 去完成。

#### 查看 Instrumentation 的 newApplication() 方法
```
    static public Application newApplication(Class<?> clazz, Context context) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        //反射创建
		Application app = (Application)clazz.newInstance();
		//绑定 Context
        app.attach(context);
        return app;
    }
```
　　newApplication() 方法中就是反射创建 Application 对象，并调用 attach() 方法绑定 Context。

#### LaunchActivity
　　当 Application 初始化完成后，系统会根据 Manifests 中的配置的启动 Activity 发送一个 Intent 去启动相应的 Activity。

　　收到一条 LAUNCH_ACTIVITY 的消息，然后开始初始化 Activity 。收到消息后，真正处理是在 ActivityThread 中的 handleLaunchActivity() 中进行的。
```
    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        ...
		//创建 Activity
        Activity a = performLaunchActivity(r,customIntent);
        if (a != null) {
            ...
			//Activity 创建成功，处理 onResume()		handleResumeActivity(r.token, false, r.isForward,!r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
            ...
		}
    }
```

###### 查看 performLaunchActivity() 方法
```
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
       ...
	   //通过仪表来创建 Activity
            activity = mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
           ...
		   //获取 Application
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
            ...
               activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window);
				...
				//根据是否可持久化选择 onCreate() 方法
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
               ...
    }
```

###### 查看 newActivity() 方法
```
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,IllegalAccessException,ClassNotFoundException {
		//反射实例化 Activity
        return (Activity)cl.loadClass(className).newInstance();
    }
```
　　newActivity() 方法主要就是反射实例化 Activity。

###### 查看 Instrumentation.callActivityOnCreate() 方法
```
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        prePerformCreate(activity);
        activity.performCreate(icicle);
        postPerformCreate(activity);
    }
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        prePerformCreate(activity);
		activity.performCreate(icicle, persistentState);
        postPerformCreate(activity);
    }
```
　　callActivityOnCreate() 方法调用了 Activity 的 performCreate() 方法。

###### 查看 Activity 的 performCreate() 方法
```
    final void performCreate(Bundle icicle) {
        restoreHasCurrentPermissionRequest(icicle);
        onCreate(icicle);
        mActivityTransitionState.readState(icicle);
        performCreateCommon();
    }

    final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        restoreHasCurrentPermissionRequest(icicle);
        onCreate(icicle, persistentState);
        mActivityTransitionState.readState(icicle);
        performCreateCommon();
    }
```
　　performCreate() 方法调用了我们常用的 onCreate() 方法。

　　所以 handleLaunchActivity() 方法，创建 Activity 之后，就调用了 Activity 的 onCreate() 方法。


## 参考文章：
1. [Android之Activity系列总结(二)--任务和返回栈](https://www.cnblogs.com/jycboy/p/6367330.html)
2. [3分钟看懂Activity启动流程](https://www.jianshu.com/p/9ecea420eb52)

