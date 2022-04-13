# Activity 的启动流程

	 本文内容
	 	1. 冷启动与热启动
	 	2. 冷启动流程图
	 	3. 看源码分析冷启动流程
	 		3.1. 从应用快捷图标点击开始
	 			3.1.1. - 3.1.18. 代码追踪
	 			3.1.19. 梳理
	 		3.2. 从 ActivityThread.java 的 main() 方法开始
	 			3.2.1. - 3.2.7. 代码追踪
	 			3.2.8. 梳理
	 		3.3. 启动 Activity
	 			3.3.1. - 3.3.7. 代码追踪
	 			3.3.8. 梳理
	 	4. 关于 IActivityMananger、ActivityManangerNative、ActivityManagerProxy、ActivityManangerService
	 		4.1. - 4.4. 代码查看
	 		4.5. 总结
	 	5. 关于 ApplicationThread
	 		5.1. - 5.3. 代码查看
	 		5.4. 总结
	 	6. 关于 Instrumentation
	 		6.1. Instrumentation 是如何实现监视应用程序和系统交互的？
	 		6.2. Instrumentation 封装有什么好处？
	 		6.3. 总结
	 	7. 参考文章

[TOC]

# 1. 冷启动与热启动

　　所谓冷启动就是启动该应用时，后台没有该应用的进程，此时系统会创建一个进程分配给它，之后会创建和初始化 Application，然后通过反射执行 ActivityThread 中的 main 方法。而热启动则是，当启动应用的时候，后台已经存在该应用的进程，比如按 home 键返回主界面再打开该应用，此时会从已有的进程中来启动应用，这种方式下，不会重新走 Application 这一步。

# 2. 冷启动流程图

![](image/冷启动流程图.png)

　　图中设计的几个类：

（1）Launcher：Launcher 本质上也是一个应用程序，和一个简单的 App 一样，也继承自 Activity，实现了点击、长按等回调接口，来接收用户的输入。

（2）ActivityManagerServices：简称 AMS，服务端对象，负责系统中所有 Activity 的生命周期。

（3）ActivityThread：App 的真正入口。当开启 App 之后，会调用 main() 开始运行，开启消息循环队列，这就是 UI 线程（主线程）。与 ActivityManagerService 配合，一起完成 Activity 的管理工作。

（4）ApplicationThread：用来实现 ActivityManangerService 与 ActivityThread 之间的交互。在 ActivityManangerService 需要管理相关 Application 中的 Activity 的生命周期时，通过 ApplicationThread 的代理对象与 ActivityThread 通讯。

（5）ApplicationThreadProxy：是 ApplicationThread 在服务器端的代理，负责和客户端的 ApplicationThread 通讯。AMS 就是通过该代理与 ActivityThread 进行通信的。

（6）Instrumentation：每一个应用程序只有一个 Instrumentation 对象，每个 Activity 内都有一个对该对象的引用。Instrumentation 可以理解为应用进程的管家，ActivityThread 要创建或暂停某个 Activity 时，都需要通过 Instrumentation 来进行具体的操作。

（7）ActivityStack：Activity 在 AMS 的栈管理，用来记录已经启动的 Actiivty 的先后关系，状态信息等。通过 ActivityStack 决定是否需要启动新的进程。

（8）ActivityRecord：ActivityStack 的管理对象，每个 Activity 在 AMS 对应一个 ActivityRecord，来记录 Activity 的状态以及其他的管理信息。其实就是服务端的 Activity 对象的映像。

（9）TaskRecord：AMS 抽象出来的一个“任务”的概念，是记录 ActivityReacord 的栈，一个“Task”包含若干个 ActivityRecord 。AMS 用 TaskRecord 确保 Activity 启动和退出的顺序。

# 3. 看源码分析冷启动流程

　　在线查看源码地址：https://www.androidos.net.cn/sourcecode 。

　　设备桌面就是一个 Launcher 类，是一个 Activity，在这个类中处理桌面上应用快捷图标的点击事件，所以从 Launcher 类开始追踪代码。

## 3.1. 从应用快捷图标点击开始

### 3.1.1. 查看 Launcher 类

　　Launcher 类的地址：
https://www.androidos.net.cn/android/8.0.0_r4/xref/packages/apps/Launcher2/src/com/android/launcher2/Launcher.java

```java
/**
 * Default launcher application.
 * 默认启动应用程序
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener {
	...
}
```
　　可以看到 Launcher 继承自 Activity ，是默认启动应用程序。

### 3.1.2. 在 Launcher 中找到点击应用快捷图标的点击事件

　　在 Launcher 类中有一个 onClick(View v)的方法，是点击快捷图标的点击事件：
```java
    /**
     * Launches the intent referred by the clicked shortcut.
     * 启动单击的快捷方式引用的意图。
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        ...
            boolean success = startActivitySafely(v, intent, tag);
		...
    }
```
　　在点击应用的快捷图标之后调用了 startActivitySafely 方法，继续查看 startActivitySafely 方法。

### 3.1.3. 查看 startActivitySafely 方法

```java
    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }
```
　　在 startActivitySafely 方法中调用了 startActivity 方法，继续查看 startActivity 方法。

### 3.1.4. 查看 startActivity() 方法

```java
    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            UserHandle user = (UserHandle) intent.getParcelableExtra(ApplicationInfo.EXTRA_PROFILE);
            LauncherApps launcherApps = (LauncherApps)
                    this.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());
                if (user == null || user.equals(android.os.Process.myUserHandle())) {
                    // Could be launching some bookkeeping activity
                    startActivity(intent, opts.toBundle());
                } else {
                    launcherApps.startMainActivity(intent.getComponent(), user,
                            intent.getSourceBounds(),
                            opts.toBundle());
                }
            } else {
                if (user == null || user.equals(android.os.Process.myUserHandle())) {
                	//调用 startActivity 启动 activity
                    startActivity(intent);
                } else {
                    launcherApps.startMainActivity(intent.getComponent(), user,
                            intent.getSourceBounds(), null);
                }
            }
            return true;
        } catch (SecurityException e) {
            ...
        }
        return false;
    }
```
　　在 startActivity(View v, Intent intent, Object tag) 方法中，调用了 Activity 的 startActivity() 方法，继续查看 Activity 的 startActivity() 方法。

### 3.1.5. 查看 Activity 的 startActivity() 方法

```java
    @Override
    public void startActivity(Intent intent) {
        this.startActivity(intent, null);
    }
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
	public void startActivityForResult(@RequiresPermission Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }


    public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
        ...
        //调用了 mInstrumentation 的 execStartActivity 方法
        Instrumentation.ActivityResult ar =
             mInstrumentation.execStartActivity(
                 this, mMainThread.getApplicationThread(), mToken, this,
                 intent, requestCode, options);
		...
    }


```
　　startActivity 最终调用 mInstrumentation.execStartActivity() 方法。

### 3.1.6. 查看 Instrumentation 的 execStartActivity() 方法

```java
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        ...
        	//调用了 AMS 的 startActivity 方法
            int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
        ...
    }
```

　　接着继续看 ActivityManagerService 的 startActivity() 方法。

### 3.1.7. 查看 ActivityManagerService 的 startActivity() 方法

```java
    @Override
    public final int startActivity(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions,
                UserHandle.getCallingUserId());
    }

    @Override
    public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivity");
        userId = mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(),
                userId, false, ALLOW_FULL_ONLY, "startActivity", null);
        // TODO: Switch to user app stacks here.
        //调用了 ActivityStarter 的 startActivityMayWait 方法
        return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
                resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
                profilerInfo, null, null, bOptions, false, userId, null, null,
                "startActivityAsUser");
    }
```
　　startActivity() 方法最后调用到了 ActivityStarter 的 startActivityMayWait() 方法。

### 3.1.8. 查看 ActivityStarter 的 startActivityMayWait() 方法

```java
    final int startActivityMayWait(IApplicationThread caller, int callingUid,
            String callingPackage, Intent intent, String resolvedType,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int startFlags,
            ProfilerInfo profilerInfo, IActivityManager.WaitResult outResult, Configuration config,
            Bundle bOptions, boolean ignoreTargetSecurity, int userId,
            IActivityContainer iContainer, TaskRecord inTask) {
        ...
            int res = startActivityLocked(caller, intent, ephemeralIntent, resolvedType,
                    aInfo, rInfo, voiceSession, voiceInteractor,
                    resultTo, resultWho, requestCode, callingPid,
                    callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
                    options, ignoreTargetSecurity, componentSpecified, outRecord, container,
                    inTask);
		...
        }
    }


    final int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
            ActivityRecord[] outActivity, ActivityStackSupervisor.ActivityContainer container,
            TaskRecord inTask) {
        ...

            err = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags,
                    true, options, inTask);
        ...
    }

    private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
			...
			//调用 TargetStack 的 startActivityLocked 方法
            mTargetStack.startActivityLocked(mStartActivity, newTask, mKeepCurTransition, mOptions);
			...
			//调用 SctivityStackSupervisor 的 resumeFocusedStackTopActivityLocked 方法
			mSupervisor.resumeFocusedStackTopActivityLocked(mTargetStack, mStartActivity,
                        mOptions);
			...

	}
```
　　ActivityStarter 的 startActivityMayWait() 方法最后调用到了 ActivityStack 的 startActivityLocked() 方法和 ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked（） 方法。

### 3.1.9. ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked（） 方法

```java
    boolean resumeFocusedStackTopActivityLocked(
            ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack)) {
        //调用 ActivityStack 的 resumeTopActivityUncheckedLocked 方法
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }
        final ActivityRecord r = mFocusedStack.topRunningActivityLocked();
        if (r == null || r.state != RESUMED) {
            mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        }
        return false;
    }
```
　　调用了 ActivityStack 的 resumeTopActivityUncheckedLocked() 方法。

### 3.1.10. ActivityStack#resumeTopActivityUncheckedLocked() 

```java
    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        ...
            result = resumeTopActivityInnerLocked(prev, options);
        ...
	}

    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
		...
		pausing |= startPausingLocked(userLeaving, false, next, dontWaitForPause);
		...
	}

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping,
            ActivityRecord resuming, boolean dontWait) {
		...
		//调用 ApplicationThread 的 schedulePauseActivity 方法
		prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing,
                        userLeaving, prev.configChangeFlags, dontWait);
		...
	}
```
　　ActivityStack 的 resumrTopActivityUncheckedLocked() 方法，先调用了 resumeTopActivityInnerLocked() 方法，而 resumeTopActivityInnerLocked() 方法调用了 ApplicationThread 的 schedulePauseActivity() 方法，接着查看 ApplicationThread 的 schedulePauseActivity() 方法。

### 3.1.11. ApplicationThread#schedulePauseActivity()

```java
        public final void schedulePauseActivity(IBinder token, boolean finished,
                boolean userLeaving, int configChanges, boolean dontReport) {
            int seq = getLifecycleSeq();
            ...
            //发出 PAUSE_ACTIVITY 的消息
            sendMessage(
                    finished ? H.PAUSE_ACTIVITY_FINISHING : H.PAUSE_ACTIVITY,
                    token,
                    (userLeaving ? USER_LEAVING : 0) | (dontReport ? DONT_REPORT : 0),
                    configChanges,
                    seq);
        }

		private void sendMessage(int what, Object obj, int arg1, int arg2, int seq) {
        	...
        	mH.sendMessage(msg);
    	}
```
　　在 ApplicationThread 的 schedulePauseActivity() 方法中，发出一个一条 H.PAUSE_ACTIVITY 的消息，接下来查看 H.PAUSE_ACTIVITY 消息的处理。

### 3.1.12. 查看 H.PAUSE_ACTIVITY 消息的处理

```java
 public void handleMessage(Message msg) {
 	...
	          case PAUSE_ACTIVITY:
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityPause");
                    SomeArgs args = (SomeArgs) msg.obj;
                    handlePauseActivity((IBinder) args.arg1, false,
                            (args.argi1 & USER_LEAVING) != 0, args.argi2,
                            (args.argi1 & DONT_REPORT) != 0, args.argi3);
                    maybeSnapshot();
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    break;
	...
 }


    private void handlePauseActivity(IBinder token, boolean finished,
            boolean userLeaving, int configChanges, boolean dontReport, int seq) {
        ...
        //调用 AMS 的 activityPaused() 方法 
      	ActivityManagerNative.getDefault().activityPaused(token);
		...
    }

```
　　ActivityManagerNative.getDefault() 获取的是一个实现 IActivityManager 接口的对象（ActivityMannagerService），调用了 ActivityManagerService 的 activityPaused() 方法。

### 3.1.13. ActivityManagerService#activityPaused() 

```java
    @Override
    public final void activityPaused(IBinder token) {
        ...
        //调用 ActivityStack 的 activityPausedLocked 方法
        stack.activityPausedLocked(token, false);
        ...
    }
```
　　在 ActivityManagerService 的 activityPaused() 方法中调用了 ActivityStack 的 activityPauseLocked() 方法。

### 3.1.14. 查看 ActivityStack 的 activityPausedLocked() 方法

```java
    final void activityPausedLocked(IBinder token, boolean timeout) {
        ...
        completePauseLocked(true, null);
        ...
    }

    private int completePauseLocked(boolean resumeNext,ActivityRecord resuming) {
		...
		mSupervisor.resumeFocusedStackTopActivityLocked();
		...
	}
```

　　在 ActivityStack 的 activityPausedLocked() 方法中调用了 completePauseLocked() 方法，而在completePauseLocked() 方法中调用 ActivityStackSupertvisor 的 resumeFocusedStackTopActivityLocked() 方法。

### 3.1.15. 查看 ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked() 方法

```java
    boolean resumeFocusedStackTopActivityLocked() {
        return resumeFocusedStackTopActivityLocked(null, null, null);
    }

    boolean resumeFocusedStackTopActivityLocked(
            ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }
        final ActivityRecord r = mFocusedStack.topRunningActivityLocked();
        if (r == null || r.state != RESUMED) {
        	//调用 ActivityStack 的 resumeTopActivityUncheckedLocked
            mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        }
        return false;
    }
```

　　ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked() 方法最后调用到了 ActivityStack 的 resumeTopActivityUncheckedLocked()  方法。

### 3.1.16. ActivityStack #resumeTopActivityUncheckedLocked()

```java
    /**
     * Ensure that the top activity in the stack is resumed.
     * 确定栈顶的 activity 是 resumed 状态。
     */
    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
		...
		result = resumeTopActivityInnerLocked(prev, options);
		...
	}

	private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
	   	...
	   	mStackSupervisor.startSpecificActivityLocked(next, true, true);
	   	...
	}

```
　　ActivityStack 的 resumeTopActivityUncheckedLocked（） 方法最后调用到了 ActivityStackSupervisor 的 startSpecificActivityLocked（） 方法。

### 3.1.17. ActivityStackSupervisor#startSpecificActivityLocked()

```java
    void startSpecificActivityLocked(ActivityRecord r,
            boolean andResume, boolean checkConfig) {
        ...
        //调用 AMS 的 startProcessLocked() 方法
        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false, false, true);
    }
```
　　ActivityStackSupervisor 类的 startSpecificActivityLocked() 方法调用了 ActivityManagerService 的 startProcessLocked() 方法。

### 3.1.18. ActivityManagerService#startProcessLocked()

```java
    final ProcessRecord startProcessLocked(String processName,
            ApplicationInfo info, boolean knownToBeDead, int intentFlags,
            String hostingType, ComponentName hostingName, boolean allowWhileBooting,
            boolean isolated, boolean keepIfLarge) {
        return startProcessLocked(processName, info, knownToBeDead, intentFlags, hostingType,
                hostingName, allowWhileBooting, isolated, 0 /* isolatedUid */, keepIfLarge,
                null /* ABI override */, null /* entryPoint */, null /* entryPointArgs */,
                null /* crashHandler */);
    }

    final ProcessRecord startProcessLocked(String processName, ApplicationInfo info,
            boolean knownToBeDead, int intentFlags, String hostingType, ComponentName hostingName,
            boolean allowWhileBooting, boolean isolated, int isolatedUid, boolean keepIfLarge,
            String abiOverride, String entryPoint, String[] entryPointArgs, Runnable crashHandler) {

	     // Start the process.  It will either succeed and return a result containing
            // the PID of the new process, or else throw a RuntimeException.
			//调用了 ActivityThread 的 main() 方法
            boolean isActivityProcess = (entryPoint == null);
            if (entryPoint == null) entryPoint = "android.app.ActivityThread";
            ...
            Process.ProcessStartResult startResult = Process.start(entryPoint,
                    app.processName, uid, uid, gids, debugFlags, mountExternal,
                    app.info.targetSdkVersion, app.info.seinfo, requiredAbi, instructionSet,
                    app.info.dataDir, entryPointArgs);
	}
```
　　ActivityManagerService 的 startProcessLocked() 方法通过反射调用了 ActivityThread 的 main() 方法。

　　代码运行到这里，进入到了 ActivityThread 的 main() 方法。

### 3.1.19. 梳理

　　代码看到这里，梳理一下之前的逻辑：Launcher 类是手机桌面 Activity ，当点击手机桌面上的 Activity 就会触发 Launcher 的 onClick() 方法，在 onClick() 方法中启动 Activity，经过一系列的方法调用，最后将会进入 ActivityThread 类，启动 ActivityThread 的 main() 方法。
　　在 Android 中，一个应用程序的开始可以说就是从 ActivityThread.java 的 main() 方法开始的。

## 3.2. 从 ActivityThread.java 的 main() 方法开始

### 3.2.1. 从 ActivityThread.java 的 main() 方法开始之后的流程图

![](image/Activity启动流程图.jpg)

### 3.2.2. 查看 ActivityThread.java 的 main() 方法

```java
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

### 3.2.3. ActivityThread#attach()

```java
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

### 3.2.4. ActivityManangerNative#attachApplication()

```java
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

#### 3.2.4.1 查看 ATTACH_APPLICATION_TRANSACTION 的处理

```java
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

### 3.2.5. ActivityManangerService#attachApplication()

```java
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
			//在第 3 部分追踪
			mStackSupervisor.attachApplicationLocked(app);
        ...
    }
```
　　attachApplication 方法调用了 ApplicationThread 的 bindApplication() 方法。

### 3.2.6. ApplicationThread#bindApplication()

```java
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

### 3.2.7. 查看 H.BIND_APPLICATION 消息的处理

```java
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
```java
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

#### 3.2.7.1. LoadedApk#makeApplication()

```java
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

##### 3.2.7.1.1. Instrumentation#newApplication()

```java
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

#### 3.2.7.2. Instrumentation#callApplicationOnCreate()

```java
public void callApplicationOnCreate(Application app) {
    app.onCreate();
}
```
　　只是调用了一个 Application 的 onCreate() 方法。

　　到这一步，Application 已经创建了。

### 3.2.8. 梳理

　　ActivityThread 的 main() 方法作为程序的入口，在 main() 方法中，初始化了主线程的 Looper，主 Handler，并使主线程进入等待接收 Message 消息的无限循环状态，调用 attach() 方法，而 attach() 方法通过调用 ActivityManager 的 attachApplication() 方法，最后调用到 ApplicationThread 的 bindApplication() 方法，在 bindApplication() 方法中发送出 BIND_APPLICATION 的消息，ActivityThread 类处理 BIND_APPLICATION 消息，接收到 BIND_APPLICATION 消息之后，创建一个 Application 实例，初始化一个 Instrumentation 对象，通过 Instrumentation 的 callApplicationOnCreate() 方法去调用 Application 的 onCreate() 方法。

## 3.3. 启动 Activity

　　在 ActivityMannagerService 的 attachApplication() 方法中，在调用 ActivityStackSupervisor 的 attachApplicationLocked(app) 来启动 Activity。

### 3.3.1. ActivityStackSupervisor#attachApplicationLocked(app)

```java
    boolean attachApplicationLocked(ProcessRecord app) throws RemoteException {
        ...
        if (realStartActivityLocked(hr, app, true, true)) {
        ...
    }

    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app,
            boolean andResume, boolean checkConfig) throws RemoteException {
		...
		//调用 ApplicationThread 的 scheduleLaunchActivity() 方法
		     app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
                    System.identityHashCode(r), r.info, new Configuration(mService.mConfiguration),
                    new Configuration(task.mOverrideConfig), r.compat, r.launchedFromPackage,
                    task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results,
                    newIntents, !andResume, mService.isNextTransitionForward(), profilerInfo);
		...
	}
```
　　ActivityStackSupervisor 的 attachApplicationLocked(app) 方法最后调用到了 ApplicationThread 的 scheduleLaunchActivity() 方法。

### 3.3.2. ApplicationThread 的 scheduleLaunchActivity() 方法

```java
        @Override
        public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident,
                ActivityInfo info, Configuration curConfig, Configuration overrideConfig,
                CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor,
                int procState, Bundle state, PersistableBundle persistentState,
                List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
                boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {
		...
		//发出 LAUNCHE_ACTIVITY 消息
		sendMessage(H.LAUNCH_ACTIVITY, r);
		}
```

　　在 ApplicationThread 的 scheduleLaunchActivity() 方法中发出了一条 LAUNCH_ACTIVITY 的消息。

### 3.3.3. 查看 H.LAUNCH_ACTIVITY 的消息处理

```java
        public void handleMessage(Message msg) {
            if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

                    r.packageInfo = getPackageInfoNoCheck(
                            r.activityInfo.applicationInfo, r.compatInfo);
                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                } break;
			...
		}

    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        ...
		//创建 Activity
        Activity a = performLaunchActivity(r,customIntent);
        if (a != null) {
            ...
			//Activity 创建成功，处理 onResume()
			handleResumeActivity(r.token, false, r.isForward,!r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
            ...
		}
    }

```
　　LAUNCH_ACTIVITY 消息的处理是在 ActivityThread 类中。在 handleLaunchActivity() 方法中调用 performLaunchActivity() 方法获取到 Activity 实例，activity 创建成功后，处理 onResume() 状态。

### 3.3.4. 查看 performLaunchActivity() 方法

```java
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

### 3.3.5. Instrumentation#newActivity()

```java
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,IllegalAccessException,ClassNotFoundException {
		//反射实例化 Activity
        return (Activity)cl.loadClass(className).newInstance();
    }
```
　　newActivity() 方法主要就是反射实例化 Activity。

### 3.3.6. 查看 Instrumentation.callActivityOnCreate() 方法

```java
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

### 3.3.7. 查看 Activity 的 performCreate() 方法

```java
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

### 3.3.8. 梳理

　　ApplicationThread 发出 LAUNCH_ACTIVITY 消息来启动 activity，通过反射机制创建 activity 实例，创建完成之后就会调用 onCreate() 方法。

# 4. 关于 IActivityManager、ActivityManagerNative、ActivityManagerProxy、ActivityManagerService

## 4.1. 查看 IActivityManager 接口

```java
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

### 4.1.1. 查看 IInterface 接口

```java
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

## 4.2. 查看 ActivityManagerNative 类

```java
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

## 4.3. 查看 ActivityManagerProxy 类

```java
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

## 4.4. 查看 ActivityManagerService

```java
public final class ActivityManagerService extends ActivityManagerNative
        implements Watchdog.Monitor, BatteryStatsImpl.BatteryCallback {

}
```
　　ActiviyManagerService 继承 ActivityManagerNative 抽象类，所以 ActivityManagerNative 方法的具体实现在 ActiviyManagerService 类中。

## 4.5. 总结

　　IActivityManager 是一个接口，用于与活动管理服务通讯。ActivityManagerProxy 实现了 IActivityManager 接口，ActivityManagerProxy 主要代理了内核中与 ActivityManager 通讯的 Binder 实例。ActivityManagerProxy 持有一个 ActivityManagerNative 的对象实例，当调用 IActivityManager 的方法时，调用 ActivityManagerNative 的实例来完成。ActivityManagerNative 是一个抽象类，实现 IActivityManager 接口，并且继承 Binder 类，提供 ActivityManagerProxy 实例供外部使用。ActivityManagerService 类继承 ActivityManagerNative 类，真正实现 IActivityManager 接口的方法。

　　很明显 ActivityManager 使用的是代理模式，ActivityManagerProxy 代理了与活动管理服务通讯。

# 5. 关于 ApplicationThread

## 5.1. 查看 ApplicationThread 类

```java
private class ApplicationThread extends ApplicationThreadNative {
	...
}
```
　　ApplicationThread 是 ActivityThread 中的一个内部类，并且 ApplicationThread 是继承 ApplicationThreadNative 类的。
　　并且在 ActivityThread 的成员变量中对发现 mAppThread。

```java
final ApplicationThread mAppThread = new ApplicationThread();
```
　　ApplicationThread 是作为 ActivityThread 中的一个常量出现的。这表明系统不喜欢这个变量中途被修改，可见这个变量具有特定而十分重要的作用。

## 5.2. 查看 ApplicationThreadNative 类

```java
public abstract class ApplicationThreadNative extends Binder
        implements IApplicationThread {
    ...
	// 无参构造函数
    public ApplicationThreadNative() {
		// 这是 Binder 的方法
        attachInterface(this, descriptor);
    }

    public void attachInterface(IInterface owner, String descriptor) {
        mOwner = owner;
        mDescriptor = descriptor;
    }

	static public IApplicationThread asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IApplicationThread in =
            (IApplicationThread)obj.queryLocalInterface(descriptor);
        if (in != null) {
            return in;
        }

        return new ApplicationThreadProxy(obj);
    }
}
```
　　可以看到 ApplicationThreadNative 继承于 Binder ，所以 ApplicationThreadNative 是一个 Binder，同时也实现了 IAPPlicationThread 接口，所以 ApplicationThreadNative 也是一个 IApplicationThread 。
　　看到 ApplicationThreadNative 的 asInterface() 方法返回一个 ApplicationThreadProxy 实例，所以 ApplicationThread 也是代理模式。

## 5.3. 查看 IApplicationThread 类

```java
/**
 * System private API for communicating with the application.  This is given to
 * the activity manager by an application  when it starts up, for the activity
 * manager to tell the application about things it needs to do.

用于与应用程序通信的系统私有 API。当一个应用程序启动时，将传递消息给活动管理器，为了活动管理器告知应用需要做的事情。
 *
 * {@hide}
 */
public interface IApplicationThread extends IInterface {
    ...
    String descriptor = "android.app.IApplicationThread";
	...
```
　　IApplicationThread 实现了 IInterface 接口，

## 5.4. 总结

　　ApplicationThread 作为 IApplicationThread 的一个实例，承担了发送 Activity 生命周期以及它一些消息的任务，也就是说发送消息。

　　至于为什么在 ActivityThread 中已经创建出了 ApplicationThread 了还要绕弯路发消息，是为了让系统根据情况来控制这个过程。

# 6. 关于 Instrumentation

　　Instrumentation 会在应用程序的任何代码运行之前被实例化，它能够允许你监视应用程序和系统的所有交互。

## 6.1. Instrumentation 是如何实现监视应用程序和系统交互的？

　　Instrumentation 类将 Application 的创建、Activity 的创建以及生命周期这些操作包装起来，通过操作 Instrumentation 进而实现上述操作。

## 6.2. Instrumentation 封装有什么好处？

　　Instrumentation 作为抽象，在约定好需要实现的功能之后，只需要给 Instrumentation 添加这些抽象功能，然后调用就好了。关于怎么实现这些功能，都会交给 Instrumentation 的实现对象就好了。这就是多态的运用，依赖抽象，不依赖具体的实践。就是上层提出需求，底层定义接口，即依赖倒置原则的践行。

## 6.3. 总结

　　在 Application 的创建、Activity 的创建和生命周期过程中都会调用 Instrumentation 的方法，Application 的创建是调用 ActivityManagerService 的方法来实现，而 Activity 的创建是反射实现，Activity 的生命周期调用了 activity 的相关方法。

# 7. 参考文章

1. [Android 7.0应用冷启动流程分析](https://blog.csdn.net/dd864140130/article/details/60466394)
2. [Android APP 冷启动流程](https://www.jianshu.com/p/f5e79998cd66)
3. [Android爬坑之路（二十）App启动过程](https://baijiahao.baidu.com/s?id=1617072163535121555&wfr=spider&for=pc)
