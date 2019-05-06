# Activity 知识之三：启动流程

	 本文内容
	 1. Activity 的启动流程图
	 2. 看源码查看启动流程

## 1. Activity 的启动流程图
![](./Activity启动流程图.jpg)

## 2. 看源码查看启动流程
　　Android 中，一个应用程序的开始可以说就是从 ActivityThread.java 中的 main() 方法开始的。

#### 1. 查看 ActivityThread.java 的 main() 方法
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
　　可以看到，main() 方法中主要做的事情有：

* 初始化主线程的 Looper、主 Handler 。并使主线程进入等待接收 Message 消息的无限循环状态。
* 调用 attach() 方法，主要就是为了发送出初始化 Application 的消息。

#### 2. 查看 attach() 方法
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
　　可以看到，在 attach() 方法中获取到了 ActivityManager 实例（至于 ActivityManager 是什么，在后面解释），并调用其 attachApplication() 方法（就是调用到了 ActivityManangerNative 的 attachApplication() 方法）。

#### 3. 查看 ActivityManangerNative 的 attachApplication() 方法
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

#### 3.1 查看 ATTACH_APPLICATION_TRANSACTION 的处理
```
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        switch (code) {
			...
			case ATTACH_APPLICATION_TRANSACTION: {
            	data.enforceInterface(IActivityManager.descriptor);
            	IApplicationThread app = ApplicationThreadNative.asInterface(
                    data.readStrongBinder());
            	if (app != null) {
                	attachApplication(app);
            	}
            	reply.writeNoException();
            	return true;
        	}
			...
		}
	}
```
　　ATTACH_APPLICATION_TRANSACTION 消息的处理是在 ActivityManangerNative 类中，调用了 ActivityManangerNative 类的 attachApplication() 方法，而 ActivityManangerNative 是一个抽象类，ActivityManagerService 类继承了 ActivityManagerNative 类，所以接着查看 ActivityManangerService 的 attachApplication() 方法。

#### 4. 查看 ActivityManangerService 的 attachApplication() 方法
```
    @Override
    public final void attachApplication(IApplicationThread thread) {
        synchronized (this) {
            int callingPid = Binder.getCallingPid();
            final long origId = Binder.clearCallingIdentity();
            attachApplicationLocked(thread, callingPid);
            Binder.restoreCallingIdentity(origId);
        }
    }

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
　　attachApplication 方法调用了 ApplicationThread 的 bindApplication() 方法。


#### 5. 查看 ApplicationThread 的 bindApplication() 方法

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
　　在 bindApplication() 方法中发出了一条 H.BIND_APPLICATION (初始化 Application)消息，接着程序开始了。

　　ApplicationThread （是 ActivityThread 的内部类）以 IApplicationThread 的身份到了 ActivityManagerService 中，经过一系列的操作，最终被调用了自己的 bindApplication() 方法，发出初始化 Application 的消息。

#### 6. 查看 H.BIND_APPLICATION 消息的处理
```
 public void handleMessage(Message msg) {
                 case BIND_APPLICATION:
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "bindApplication");
                    AppBindData data = (AppBindData)msg.obj;
                    handleBindApplication(data);
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    break;
 }
```
　　BIND_APPLICATION 的处理是调用了 handleBindApplication() 方法，继续查看 handleBindApplication() 方法。
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
　　接收到 BIND_APPLICATION 消息之后就创建了 Application 实例，并且调用了 Application 的 onCreate() 方法。

#### 6.1 查看 LoadedApk 的 makeApplication() 方法
```
    public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
        if (mApplication != null) {
            return mApplication;
        }

        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "makeApplication");

        Application app = null;

        String appClass = mApplicationInfo.className;
        if (forceDefaultAppClass || (appClass == null)) {
            appClass = "android.app.Application";
        }

        try {
            java.lang.ClassLoader cl = getClassLoader();
            if (!mPackageName.equals("android")) {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
                        "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }
            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
			//命令 mActivityThread 的 mInstrumentation 创建 Application
            app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
            appContext.setOuterContext(app);
       ...
        mActivityThread.mAllApplications.add(app);
        mApplication = app;
        ...

        return app;
    }
```

　　从方法中看到：在取得 Application 的实际类名之后，最后的创建工作还是交由 Instrumentation 去完成。


#### 6.1.1 查看 Instrumentation 的 newApplication() 方法
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

#### 6.2 查看 Instrumentation 的 callApplicationOnCreate()
```
    public void callApplicationOnCreate(Application app) {
        app.onCreate();
    }
```
　　只是调用了一个 Application 的 onCreate() 方法。


　　到这一步，Application 已经创建了。

**总结：**
　　ActivityThread 的 main() 方法作为程序的入口，在 main() 方法中，初始化了主线程的 Looper，主 Handler，并使主线程进入等待接收 Message 消息的无限循环状态，调用 attach() 方法，而attach() 方法通过调用 ActivityManager 的 attachApplication() 方法，最后调用到 ApplicationThread 的 bindApplication() 方法，在 bindApplication() 方法中发送出 BIND_APPLICATION 的消息，ActivityThread 类处理 BIND_APPLICATION 消息，接收到 BIND_APPLICATION 消息之后，创建一个 Application 实例，初始化一个 Instrumentation 对象，通过 Instrumentation 的 callApplicationOnCreate() 方法去调用 Application 的 onCreate() 方法。

## 关于 IActivityManager、ActivityManagerNative、ActivityManagerProxy、ActivityMnanagerService

#### 1. 查看 IActivityManager 接口
```
/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 *
 * {@hide}
 */
public interface IActivityManager extends IInterface {
	...
}
```
　　IActivityManager 是一个接口，并且继承 IInterface 接口。IActivityManager 是一个用来与活动管理服务交流的系统私有 API，并且提供应用返回活动管理的回调。

#### 1.1 查看 IInterface 接口
```
/**
 * Base class for Binder interfaces.  When defining a new interface,
 * you must derive it from IInterface.
 */
public interface IInterface
{
    /**
     * Retrieve the Binder object associated with this interface.
     * You must use this instead of a plain cast, so that proxy objects
     * can return the correct result.
     */
    public IBinder asBinder();
}
```
　　IInterface 是 Binder 接口的基础类，通过 asBinder() 方法获取 Binder 对象实例。

#### 2. 查看 ActivityManagerNative 类
```
public abstract class ActivityManagerNative extends Binder implements IActivityManager
{
    /**
     * Cast a Binder object into an activity manager interface, generating
     * a proxy if needed.
     */
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

    /**
     * Retrieve the system's default/global activity manager.
     */
    static public IActivityManager getDefault() {
        return gDefault.get();
    }

	public IBinder asBinder() {
        return this;
    }

    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };
}
```
　　ActivityManagerNative 是一个抽象类，实现 IActivityManager 接口，并且继承 Binder 类。

　　asInterface() 方法是 ActivityManagerNative 中的一个静态方法，它会调用到 ActivityManagerProxy 的构造方法，并将 ActivityManagerProxy 对象返回。这个静态方法需要一个 IBinder 作为参数。

　　gDefault 是 ActivityManagerNative 的静态常量，是一个单例对象。获取到了 IBinder 对象，调用 asInterface() 方法得到 IActivityManager 对象并返回。所以当调用 ActivityManagerNative.getDefault() 获得的实际是一个代理类的实例 ActivityManagerProxy。

　　通过 ServiceManager 获取到 IBinder 实例的。如果了解 AIDL 通讯流程的话，就会理解这只是通过另一种方式获取 IBinder 实例罢了。获取 IBinder 的目的就是为了通过这个 IBinder 和 ActivityManager 进行通讯，进而 ActivityManager 会调度发送 H.BIND_APPLICATION 即初始化 Application 的 Message 消息。

#### 3. 查看 ActivityManagerProxy 类
```
class ActivityManagerProxy implements IActivityManager
{
    public ActivityManagerProxy(IBinder remote)
    {
        mRemote = remote;
    }

    public IBinder asBinder()
    {
        return mRemote;
    }

    private IBinder mRemote;
}

```
　　ActivityManagerProxy 的构造函数非常的简单，首先它需要一个 IBinder 参数，然后赋值给 mRemote 变量。这个 mRemote 就是在 ActivityManagerNative 的 asInterface 方法中设置的。 mRemote 是 ActivityManagerProxy 的成员变量，对它的操作是由 ActivityManagerProxy 来代理间接进行的。这样设计的好处是保护了 mRemote，并且能够在操作 mRemote 前执行一些别的事务，并且是以 ActivityManager 来进行这些操作的。


#### 4. 查看 ActivityManagerService
```
public final class ActivityManagerService extends ActivityManagerNative
        implements Watchdog.Monitor, BatteryStatsImpl.BatteryCallback {

}
```
　　ActiviyManagerService 继承 ActivityManagerNative 抽象类，所以 ActivityManagerNative 方法的具体实现在 ActiviyManagerService 类中。


**总结：**

　　IActivityManager 是一个接口，用于与活动管理服务通讯。ActivityManagerProxy 实现了 IActivityManager 接口，ActivityManagerProxy 主要代理了内核中与 ActivityManager 通讯的 Binder 实例。ActivityManagerProxy 持有一个 ActivityManagerNative 的对象实例，当调用 IActivityManager 的方法时，调用 ActivityManagerNative 的实例来完成。ActivityManagerNative 是一个抽象类，实现 IActivityManager 接口，并且继承 Binder 类，提供 ActivityManagerProxy 实例供外部使用。ActivityManagerService 类继承 ActivityManagerNative 类，真正实现 IActivityManager 接口的方法。

　　很明显 ActivityManager 使用的是代理模式，ActivityManagerProxy 代理了与活动管理服务通讯。

## 关于 ApplicationThread

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

**总结：**

　　ApplicationThread 作为 IApplicationThread 的一个实例，承担了发送 Activity 生命周期以及它一些消息的任务，也就是说发送消息。

　　至于为什么在 ActivityThread 中已经创建出了 ApplicationThread 了还要绕弯路发消息，是为了让系统根据情况来控制这个过程。


## 关于 Instrumentation
　　Instrumentation 会在应用程序的任何代码运行之前被实例化，它能够允许你监视应用程序和系统的所有交互。

#### Instrumentation 是如何实现监视应用程序和系统交互的？
　　Instrumentation 类将 Application 的创建、Activity 的创建以及生命周期这些操作包装起来，通过操作 Instrumentation 进而实现上述操作。

#### Instrumentation 封装有什么好处？
　　Instrumentation 作为抽象，放约定好需要实现的功能之后，只需要给 Instrumentation 添加这些抽象功能，然后调用就好了。关于怎么实现这些功能，都会交给 Instrumentation 的实现对象就好了。这就是多态的运用，依赖抽象，不依赖具体的实践。就是上层提出需求，底层定义接口，即依赖倒置原则的践行。


## 参考文章：
1. [3分钟看懂Activity启动流程](https://www.jianshu.com/p/9ecea420eb52)


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



