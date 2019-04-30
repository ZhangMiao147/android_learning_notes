# Activity 之冷启动流程

	 本文内容
	 1.冷启动与热启动
	 2.冷启动流程图
	 3.看源码分析冷启动流程

## 冷启动与热启动
　　所谓冷启动就是启动该应用时，后台没有该应用的进程，此时系统会创建一个进程分配给它，之后会创建和初始化 Application，然后通过反射执行 ActivityThread 中的 main 方法。而热启动则是，当启动应用的时候，后台已经存在该应用的进程，比如按 home 键返回主界面再打开该应用，此时会从已有的进程中来启动应用，这种方式下，不会重新走 Application 这一步。

## 冷启动流程图
![](./冷启动流程图.png)

　　图中设计的几个类：
（1）Launcher：Launcher 本质上也是一个应用程序，和一个简单的 App 一样，也继承自 Activity，实现了点击、长按等回调接口，来接收用户的输入。

（2）


## 看源码分析冷启动流程

　　在线查看源码地址：https://www.androidos.net.cn/sourcecode .


#### 1. 查看 Launcher 类

　　Launcher 类的地址：
https://www.androidos.net.cn/android/8.0.0_r4/xref/packages/apps/Launcher2/src/com/android/launcher2/Launcher.java

```
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener {
	...
}
```
　　可以看到 Launcher 继承自 Activity ，是默认启动应用程序。

#### 2. 在 Launcher 中找到点击应用快捷图标的点击事件
　　在 Launcher 类中有一个 onClick(View v)的方法，是点击快捷图标的点击事件：
```
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

#### 3. 查看 startActivitySafely 方法
```
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

#### 4. 查看 startActivity() 方法
```
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
                    startActivity(intent);
                } else {
                    launcherApps.startMainActivity(intent.getComponent(), user,
                            intent.getSourceBounds(), null);
                }
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }
```
　　调用了 Activity 的 startActivity() 方法。

#### 5. 查看 Activity 的 startActivity() 方法

```
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
        Instrumentation.ActivityResult ar =
             mInstrumentation.execStartActivity(
                 this, mMainThread.getApplicationThread(), mToken, this,
                 intent, requestCode, options);
		...
    }


```
　　startActivity 最终调用 mInstrumentation.execStartActivity() 方法。

#### 6. 查看 Instrumentation 的 execStartActivity() 方法
```
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        ...
            int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
        ...
    }
```
　　execStartActivity() 方法中调用了 ActivityManagerNative.getDefault().startActivity() 方法，实质上就是 ActivityManagerService 类的 startActivity() 方法。

#### 7. 查看 ActivityManagerService 的 startActivity() 方法
```
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
        return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
                resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
                profilerInfo, null, null, bOptions, false, userId, null, null,
                "startActivityAsUser");
    }
```
　　startActivity() 方法最后调用到了 ActivityStarter 的 startActivityMayWait() 方法。

#### 8. 查看 ActivityStarter 的 startActivityMayWait() 方法
```
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
			mTargetStack.startActivityLocked(mStartActivity, newTask, mKeepCurTransition, mOptions);
			...
			mSupervisor.resumeFocusedStackTopActivityLocked(mTargetStack, mStartActivity,
                        mOptions);
			...

	}
```
　　ActivityStarter 的 startActivityMayWait() 方法最后调用到了 ActivityStack 的 startActivityLocked() 方法和 ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked（） 方法。

　　ActivityStack 的 startActivityLocked() 方法将 actiivty 放到了栈顶。

#### ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked（） 方法
```
    boolean resumeFocusedStackTopActivityLocked(
            ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack)) {
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

#### ActivityStack 的 resumeTopActivityUncheckedLocked() 方法
```
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
		prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing,
                        userLeaving, prev.configChangeFlags, dontWait);
		...
	}
```
　　prev.app.thread 是一个实现 IApplicationThread 的接口，ApplicationThreadNative 抽象类实现了 ApplicationThreadNative 接口，ApplicationThread 类继承 ApplicationThreadNative 类（ApplicationThread 类是 Activity 的内部类）。


#### 查看 ApplicationThread 类的 schedulePauseActivity() 方法
```
        public final void schedulePauseActivity(IBinder token, boolean finished,
                boolean userLeaving, int configChanges, boolean dontReport) {
            int seq = getLifecycleSeq();
            ...
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

#### 查看 H.PAUSE_ACTIVITY 消息的处理
```
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
      	ActivityManagerNative.getDefault().activityPaused(token);
		...
    }

```
　　ActivityManagerNative.getDefault() 获取的是一个实现 IActivityManager 接口的对象，而 ActivityManagerNative 抽象类实现了 IActivityManager 接口，ActivityManagerService 继承了 IActivityManagerNative 抽象类。所以在收到 H.PAUSE_ACTIVITY 消息之后，调用了 ActivityManagerService 的 activityPaused() 方法。

#### 查看 ActivityManagerService 的 activityPaused() 方法
```
    @Override
    public final void activityPaused(IBinder token) {
        ...
        stack.activityPausedLocked(token, false);
        ...
    }
```

#### 查看 ActivityStack 的 activityPausedLocked() 方法
```
    final void activityPausedLocked(IBinder token, boolean timeout) {
        ...
        completePauseLocked(true, null);
        ...
    }

    private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
			...
			mSupervisor.resumeFocusedStackTopActivityLocked();
			...
	}
```

#### 查看 ActivityStackSupervisor 的 resumeFocusedStackTopActivityLocked() 方法
```
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
            mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        }
        return false;
    }
```

#### 查看 ActivityStack 的 resumeTopActivityUncheckedLocked（） 方法

```
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

#### ActivityStackSupervisor 类的 startSpecificActivityLocked() 方法 
```
    void startSpecificActivityLocked(ActivityRecord r,
            boolean andResume, boolean checkConfig) {
        ...
        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false, false, true);
    }
```

#### ActivityManagerService 的 startProcessLocked() 方法
```
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

#### ActiivtyThread 的 main()方法
```
    public static void main(String[] args) {
	...
	    ActivityThread thread = new ActivityThread();
        thread.attach(false);
	...
	}

    private void attach(boolean system) {
	...
	        final IActivityManager mgr = ActivityManagerNative.getDefault();
            try {
                mgr.attachApplication(mAppThread);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
	...
	}

```
　　调用了 ActivityManagerService 的 attachApplication() 方法。

#### ActivityManagerService 的 attachApplication() 方法
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
　　调用了 ApplicationThread 的 bindApplication() 方法。

#### ApplicationThread 的 bindApplication() 方法
```
        public final void bindApplication(String processName, ApplicationInfo appInfo,
                List<ProviderInfo> providers, ComponentName instrumentationName,
                ProfilerInfo profilerInfo, Bundle instrumentationArgs,
                IInstrumentationWatcher instrumentationWatcher,
                IUiAutomationConnection instrumentationUiConnection, int debugMode,
                boolean enableBinderTracking, boolean trackAllocation,
                boolean isRestrictedBackupMode, boolean persistent, Configuration config,
                CompatibilityInfo compatInfo, Map<String, IBinder> services, Bundle coreSettings) {
		...
		sendMessage(H.BIND_APPLICATION, data);
		...
		}

```
　　发送 H.BIND_APPLICATION 消息，查看 H.BIND_APPLICATION 消息的处理。

#### H.BIND_APPLICATION 消息的处理
```
        public void handleMessage(Message msg) {
			...
			   case BIND_APPLICATION:
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "bindApplication");
                    AppBindData data = (AppBindData)msg.obj;
                    handleBindApplication(data);
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    break;
			...
		}

```















## 参考文章：
1. [Android 7.0应用冷启动流程分析](https://blog.csdn.net/dd864140130/article/details/60466394)
2. [Android APP 冷启动流程](https://www.jianshu.com/p/f5e79998cd66)
3. [Android爬坑之路（二十）App启动过程](https://baijiahao.baidu.com/s?id=1617072163535121555&wfr=spider&for=pc)
