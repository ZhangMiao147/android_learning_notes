# 性能优化之 ANR 

## 什么是 ANR

　　ANR 全称是 Application Not Responding，意思就是应用程序未响应。如果一个应用无法响应用户的输入，系统就会弹出一个 ANR 对话框，用户可以自行选择继续等待或者是停止当前程序。

## ANR 的发生原因

1. 代码自身引起，例如：
   * 主线程阻塞、IOWait 等；
   
     Andriod 系统中，ActivityManangerService(简称 AMS) 和 WindowManangerService(简称 WMS) 会检测 App 的响应事件，如果 App 在特定时间无法响应屏幕触摸或键盘输入事件，或者特定事件没有处理完毕，就会出现 ANR。
   
     * InputDispatching Timeout：输入事件分发或屏幕触摸事件超时 5s 未响应完毕；
     * BroadcastQueue Timeout：前台广播在 10秒 内、后台广播在 60 秒内未执行完成；
     * Service Timeout：前台服务在 20 秒内、后台服务在 200 秒内未执行完成；
     * ContentProvider Timeout：内容提供者，在 publish 超时 10s；
   
   - 主线程进行耗时计算；
   - 错误的操作，比如调用了 Thread.wait 或者 Thread.sleep 等。
   
2. 其他进程间接引起，例如：

   * 当前应用进程进行进程间通信请求其他进程，其他进程的操作长时间没有反馈；
   * 其他进程的 COU 占用率高，使得当前应用进程无法抢占到 CPU 时间片。



## 如何避免

　　基本的思路就是尽量避免在主线程（UI 线程）中作耗时操作，将耗时操作放在子线程中。将 IO 操作在工作线程来处理，减少其他耗时操作和错误操作。

* 使用 AsyncTask 处理耗时 IO 操作。
* 使用 Thread 或者 HandlerThread 时，调用 Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)设置优先级，否则仍然会降低程序响应，因为默认 Thread 的优先级和主线程相同。
* 使用 Handler 处理工作线程结果，而不是使用 Thread.wait() 或者 Thread.sleep() 来阻塞主线程。
* Activity 的 onCreate 和 onResume 回调中尽量避免耗时的代码。
* BroadcastReceiver 中的 onReceive 代码也要尽量减少耗时，建议使用 Intentservice 处理。

## 画龙点睛

　　通常 100 到 200 毫秒就会让任察觉程序反应慢，为了更加提升响应，可以使用下面的几种方法：

* 如果程序正在后台处理用户的输入，建议使用让用户得知进度，比如使用 ProgressBar 控件。
* 程序启动时可以选择加上欢迎界面，避免让用户察觉卡顿。
* 使用 Systrace 和 TraceView 找到影响响应的问题。

## 如何定位

　　如果开发机器上出现问题，可以通过查看 /data/anr/traces.txt 即可，最新的 ANR 信息在最开始部分。

　　如果是线上版本引起的，Google Play 后台有相关的数据可以帮助查看分析并解决问题。

## ANR 分析方法

#### ANR 分析方法一：Log

![](image/ANR_LOG.png)
　　可以看到 logcat 清晰地记录了 ANR 发生的时间，以及线程的 tid 和一句话概括原因：WaitingInMainSignalCatcherLoop，大概意思为主线程等待异常。

　　最后一句 The application may be doing too much work on its main thread. 告知可能在主线程做了太多的工作。

#### ANR 分析方法二：traces.txt

　　上面的 log 有第二句 Wrote stack trances to '/data/anr/trances.txt'，说明 ANR 异常已经输出到 trances.txt 文件，使用 adb 命令把这个文件从手机里导出来：

1. 进入 adb.exe 所在的目录，也就是 Android SDK 的 platform-tools 目录：

```
cd D:\Android\AndroidSdk\platform-tools
```

2. 到指定目录后执行以下 adb 命令导出 trances.txt 文件：

```
adb pull /data/anr/trances.txt
```

　　traces.txt 默认会被导出到 Android SDK 的 \platform-tools 目录。一般来说 traces.txt 文件记录的东西会比较多，分析的时候需要有针对性地去找相关记录。

```java
----- pid 23346 at 2017-11-07 11:33:57 -----  ----> 进程id和ANR产生时间
Cmd line: com.sky.myjavatest
Build fingerprint: 'google/marlin/marlin:8.0.0/OPR3.170623.007/4286350:user/release-keys'
ABI: 'arm64'
Build type: optimized
Zygote loaded classes=4681 post zygote classes=106
Intern table: 42675 strong; 137 weak
JNI: CheckJNI is on; globals=526 (plus 22 weak)
Libraries: /system/lib64/libandroid.so /system/lib64/libcompiler_rt.so
/system/lib64/libjavacrypto.so
/system/lib64/libjnigraphics.so /system/lib64/libmedia_jni.so /system/lib64/libsoundpool.so
/system/lib64/libwebviewchromium_loader.so libjavacore.so libopenjdk.so (9)
Heap: 22% free, 1478KB/1896KB; 21881 objects    ----> 内存使用情况
...
"main" prio=5 tid=1 Sleeping    ----> 原因为Sleeping
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x733d0670 self=0x74a4abea00
  | sysTid=23346 nice=-10 cgrp=default sched=0/0 handle=0x74a91ab9b0
  | state=S schedstat=( 391462128 82838177 354 ) utm=33 stm=4 core=3 HZ=100
  | stack=0x7fe6fac000-0x7fe6fae000 stackSize=8MB
  | held mutexes=
  at java.lang.Thread.sleep(Native method)
  - sleeping on <0x053fd2c2> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:373)
  - locked <0x053fd2c2> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:314)
  at android.os.SystemClock.sleep(SystemClock.java:122)
  at com.sky.myjavatest.ANRTestActivity.onCreate(ANRTestActivity.java:20) ----> 产生ANR的包名以及具体行数
  at android.app.Activity.performCreate(Activity.java:6975)
  at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1213)
  at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2770)
  at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2892)
  at android.app.ActivityThread.-wrap11(ActivityThread.java:-1)
  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593)
  at android.os.Handler.dispatchMessage(Handler.java:105)
  at android.os.Looper.loop(Looper.java:164)
  at android.app.ActivityThread.main(ActivityThread.java:6541)
  at java.lang.reflect.Method.invoke(Native method)
  at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)
```

　　在文件中使用 ctrl + F 查找包名可以快速定位相关代码。

　　通过上方 log 可以看出相关问题：

* 进程 id 和包名：pid 23346 com.sky.myjavatest
* 造成 ANR 的原因：Sleeping
* 造成 ANR 的具体行数：ANRTestActivity.java:20 类的第 20 行

**注意**：产生新的 ANR，原来的 traces.txt 文件会被覆盖。

#### ANR 分析方法三：Java 线程调用分析

　　通过 JDK 提供的命令可以帮助分析和调试 Java 应用，命令是：

```
jstack {pid}
```

　　其中 pid 可以通过 jps 命令获得，jps 命令会列出当前系统中运行的所有 Java 虚拟机进程，比如：

```
7266 Test
7267 Jps
```

#### ANR 分析方法四：DDMS 分析 ANR 问题

* 使用 DDMS-----Update Threads 工具
* 阅读 Update Threads 的输出

## 造成 ANR 的原因及解决方法

　　造成 ANR 的原因有很多：

* 主线程阻塞或主线程数据读取

> 解决方法：避免死锁的出现，使用子线程来处理耗时操作或阻塞任务。尽量避免在主线程 query provider、不要滥用 SharePreferences。

* CPU 满负荷、I/O 阻塞

> 解决方法：文件读写或数据库操作放在子线程异步处理。

* 内存不足

> 解决方法：AndroidManifest.xml 文件 < application > 中可以设置 android:largeHeap="true"，以此增大 App 使用内存。不过不建议使用此法，从根本上防止内存泄漏，优化内存使用才是正确的做法。

* 各大组件 ANR

> 四大组件的生命周期中也应避免耗时操作，注意 BroadcastReceiver 的 onReceive()、后台 Service 和 ContentProvider 也不要执行太长时间的任务。

## ANR 源码分析

#### Service 造成的 Service Timeout

　　Service Timeout 是位于“ActivityMananger”线程中的 AMS.MainHandler 收到 SERVICE_TIMEOUT_MSG 消息时触发。

##### 发送延时消息

　　Service 进程 attach 到 system_server 进程的过程中会调用 realStartServiceLocked ，紧接着 mAm.mHandler.sendMessageAtTime() 来发送一个延时消息，延时的时长是定义好的，如前台 Service 的 20 秒。ActivityMananger 线程中的 AMS.MainHandler 收到 SERVICE_TIMEOUT_MAG 消息时会触发。

　　ActiveServices.realStartServiceLocked

```java
    private final void realStartServiceLocked(ServiceRecord r,
            ProcessRecord app, boolean execInFg) throws RemoteException {
		...
		//发送延时消息（SERVICE_TIMEOUT_MSG），此处是 Service ANR 的源头
		bumpServiceExecutingLocked(r, execInFg, "create");
        try {
			...
			// 创建 Service 对象，并执行 Service 的 onCreate() 方法
            app.thread.scheduleCreateService(r, r.serviceInfo,
                    mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo),
                    app.repProcState);
			...
        } catch (DeadObjectException e) {
            Slog.w(TAG, "Application dead when creating service " + r);
            mAm.appDiedLocked(app);
            throw e;
        } finally {
			...
		}
	}
```

ActiveService 的 bumpServiceExecutingLocked() 方法：

```java
    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        ...
        scheduleServiceTimeoutLocked(r.app);
        ...
    }

	/**
	* 延时发送 SERVICE_TIMEOUT_MSG 消息，前后台 Service 的时间不一样。
	*/
    void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() == 0 || proc.thread == null) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        Message msg = mAm.mHandler.obtainMessage(
                ActivityManagerService.SERVICE_TIMEOUT_MSG);
        msg.obj = proc;
		//当超时后仍没有 remove 该 SERVICE_TIMEOUT_MSG 消息，则执行 service Timeout 流程
        mAm.mHandler.sendMessageAtTime(msg,
                proc.execServicesFg ? (now+SERVICE_TIMEOUT) : (now+ SERVICE_BACKGROUND_TIMEOUT));
    }
```

###### 进入目标进程的主线程创建 Service

　　经过 Binder 等层层调用进入目标进程的主线程 handleCreateService(CreateServiceData data)。

```java
    private void handleCreateService(CreateServiceData data) {
		...
		Service service = null;
        try {
            java.lang.ClassLoader cl = packageInfo.getClassLoader();
			// 创建 Service 对象
            service = (Service) cl.loadClass(data.info.name).newInstance();
        } catch (Exception e) {
            ...
        }
		...
        try {
			//创建 ContextImpl 对象
            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
            context.setOuterContext(service);
			//创建 Application 对象
            Application app = packageInfo.makeApplication(false, mInstrumentation);
            service.attach(context, this, data.info.name, data.token, app,
                    ActivityManagerNative.getDefault());
			//调用服务 onCreate() 方法
            service.onCreate();
            mServices.put(data.token, service);
            try {
				// 取消 AMS.MainHandler 的延时消息
                ActivityManagerNative.getDefault().serviceDoneExecuting(
                        data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } catch (Exception e) {
            ...
        }
	}
```

　　这个方法中会创建目标服务对象，以及回调常用的 Service 的 onCreate() 方法，紧接着通过 serviceDoneExecuting() 回到 system_server 执行取消 AMS.MainHandler 的延时消息。

###### 回到 system_server 执行取消 AMS.MainHandler 的延时消息

ActiveService 的 serviceDoneExecutingLocked() 方法，在 serviceDoneExecutingLocked() 方法中会移除刚刚延时发送的 Message ：

```java
    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying,
            boolean finishing) {
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                r.app.execServicesFg = false;
                r.app.executingServices.remove(r);
                if (r.app.executingServices.size() == 0) {
					//remove 掉刚刚延时发送的 Message
                    mAm.mHandler.removeMessages(ActivityManagerService.SERVICE_TIMEOUT_MSG, r.app);
                } else if (r.executeFg) {
                    ...
                }
                ...
            }
            ...
        }
    }
```

　　此方法中 Service 逻辑处理完成则移除之前延时的消息 SERVICE_TIMEOUT_MSG。如果没有执行完毕不调用这个方法，则超时后会发出 SERVICE_TIMEOUT_MSG 来告知 ANR 发生。

#### 规定时间之内未完成方法的调用，出现了 ANR

　　而如果 Message 没有被 mAm.mHandler(也就是 ActivityManagerService 中的 MainHandler)及时 remove 掉，被执行的话就会出现 ANR 的发生；执行到 ActivityMannagerService 中的 MainHandler 的 SERVICE_TIMEOUT_MSG 然后调用到 ActiveServices 的 serviceTimeout() 方法，最终执行到 AppErrors 的 appNotResponding() 方法。

```java
    final void appNotResponding(ProcessRecord app, ActivityRecord activity,
            ActivityRecord parent, boolean aboveSystem, final String annotation) {
        ArrayList<Integer> firstPids = new ArrayList<Integer>(5);
        SparseArray<Boolean> lastPids = new SparseArray<Boolean>(20);

        ...

        synchronized (mService) {
            // PowerManager.reboot() can block for a long time, so ignore ANRs while shutting down.

            // In case we come through here for the same app before completing
            // this one, mark as anring now so we will bail out.
            app.notResponding = true;

            // Log the ANR to the event log.
            EventLog.writeEvent(EventLogTags.AM_ANR, app.userId, app.pid,
                    app.processName, app.info.flags, annotation);

            // Dump thread traces as quickly as we can, starting with "interesting" processes.
            firstPids.add(app.pid);

            // Don't dump other PIDs if it's a background ANR
            isSilentANR = !showBackground && !app.isInterestingToUserLocked() && app.pid != MY_PID;
            if (!isSilentANR) {
                int parentPid = app.pid;
                if (parent != null && parent.app != null && parent.app.pid > 0) {
                    parentPid = parent.app.pid;
                }
                if (parentPid != app.pid) firstPids.add(parentPid);

                if (MY_PID != app.pid && MY_PID != parentPid) firstPids.add(MY_PID);

                for (int i = mService.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord r = mService.mLruProcesses.get(i);
                    if (r != null && r.thread != null) {
                        int pid = r.pid;
                        if (pid > 0 && pid != app.pid && pid != parentPid && pid != MY_PID) {
                            if (r.persistent) {
                                firstPids.add(pid);
                                if (DEBUG_ANR) Slog.i(TAG, "Adding persistent proc: " + r);
                            } else {
                                lastPids.put(pid, Boolean.TRUE);
                                if (DEBUG_ANR) Slog.i(TAG, "Adding ANR proc: " + r);
                            }
                        }
                    }
                }
            }
        }
		//获取 ANR 日志信息
        // Log the ANR to the main log.
        StringBuilder info = new StringBuilder();
        info.setLength(0);
        info.append("ANR in ").append(app.processName);
        if (activity != null && activity.shortComponentName != null) {
            info.append(" (").append(activity.shortComponentName).append(")");
        }
        info.append("\n");
        info.append("PID: ").append(app.pid).append("\n");
        if (annotation != null) {
            info.append("Reason: ").append(annotation).append("\n");
        }
        if (parent != null && parent != activity) {
            info.append("Parent: ").append(parent.shortComponentName).append("\n");
        }

        ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(true);

        String[] nativeProcs = NATIVE_STACKS_OF_INTEREST;
        // don't dump native PIDs for background ANRs
        File tracesFile = null;
        if (isSilentANR) {
            tracesFile = mService.dumpStackTraces(true, firstPids, null, lastPids,
                null);
        } else {
            tracesFile = mService.dumpStackTraces(true, firstPids, processCpuTracker, lastPids,
                nativeProcs);
        }

        String cpuInfo = null;
        if (ActivityManagerService.MONITOR_CPU_USAGE) {
            mService.updateCpuStatsNow();
            synchronized (mService.mProcessCpuTracker) {
                cpuInfo = mService.mProcessCpuTracker.printCurrentState(anrTime);
            }
            info.append(processCpuTracker.printCurrentLoad());
            info.append(cpuInfo);
        }

        info.append(processCpuTracker.printCurrentState(anrTime));

        Slog.e(TAG, info.toString());
        if (tracesFile == null) {
            // There is no trace file, so dump (only) the alleged culprit's threads to the log
            Process.sendSignal(app.pid, Process.SIGNAL_QUIT);
        }

		//添加 DropBox，2.3 之后出的功能，解决 trances.txt 被覆盖的问题
        mService.addErrorToDropBox("anr", app, app.processName, activity, parent, annotation,
                cpuInfo, tracesFile, null);

        if (mService.mController != null) {
            try {
                // 0 == show dialog, 1 = keep waiting, -1 = kill process immediately
                int res = mService.mController.appNotResponding(
                        app.processName, app.pid, info.toString());
                if (res != 0) {
                    if (res < 0 && app.pid != MY_PID) {
                        app.kill("anr", true);
                    } else {
                        synchronized (mService) {
                            mService.mServices.scheduleServiceTimeoutLocked(app);
                        }
                    }
                    return;
                }
            } catch (RemoteException e) {
                mService.mController = null;
                Watchdog.getInstance().setActivityController(null);
            }
        }

        synchronized (mService) {
            mService.mBatteryStatsService.noteProcessAnr(app.processName, app.uid);

            if (isSilentANR) {
                app.kill("bg anr", true);
                return;
            }

            // Set the app's notResponding state, and look up the errorReportReceiver
            makeAppNotRespondingLocked(app,
                    activity != null ? activity.shortComponentName : null,
                    annotation != null ? "ANR " + annotation : "ANR",
                    info.toString());

            // Bring up the infamous App Not Responding dialog
			//显示 ANR dialog
            Message msg = Message.obtain();
            HashMap<String, Object> map = new HashMap<String, Object>();
            msg.what = ActivityManagerService.SHOW_NOT_RESPONDING_UI_MSG;
            msg.obj = map;
            msg.arg1 = aboveSystem ? 1 : 0;
            map.put("app", app);
            if (activity != null) {
                map.put("activity", activity);
            }

            mService.mUiHandler.sendMessage(msg);
        }
    }

```

　　流程总结：

1. Service 创建之前会延迟发送一个消息，而这个消息就是 ANR 的起源；
2. Service 创建完毕，在规定的时间之内执行完毕 onCreate() 方法就移除这个消息，就不会产生 ANR 了；
3. 在规定的时间之内没有完成 onCreate() 的调用，消息被执行，ANR 发生。


#### BroadcastReceiver 造成的 BroadcastQueue Timeout

　　BroadcastReceiver Timeout 是位于 “ActivityMananger” 线程中的 BroadcastQueue.BroadcastHandler 收到 BROADCAST_TIMEOUT_MSG 消息时触发。

###### 处理广播函数 processNextBroadcast() 中 broadcastTimeoutLocked(false) 发送延时消息

　　广播处理顺序为先处理并行广播，再处理当前有序广播。

```java
BroadcastQueue.java
    final void processNextBroadcast(boolean fromMsg) {
        synchronized(mService) {
			...
			// 处理当前有序广播
            do {
                r = mOrderedBroadcasts.get(0);

                // 获取所有该广播所有的接收者
                int numReceivers = (r.receivers != null) ? r.receivers.size() : 0;
                if (mService.mProcessesReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if ((numReceivers > 0) &&
                            (now > r.dispatchTime + (2*mTimeoutPeriod*numReceivers))) {
                        // step 1.发送延时消息，这个函数处理了很多事情，比如广播处理超时结束广播
						broadcastTimeoutLocked(false); // forcibly finish this broadcast
                        ...
                    }
                }
				...

                if (r.receivers == null || r.nextReceiver >= numReceivers
                        || r.resultAbort || forceReceive) {
                    if (r.resultTo != null) {
                        try {
							//2. 处理广播消息
                            performReceiveLocked(r.callerApp, r.resultTo,
                                new Intent(r.intent), r.resultCode,
                                r.resultData, r.resultExtras, false, false, r.userId);
                            r.resultTo = null;
                        } catch (RemoteException e) {
                            ...
                        }
                    }

                    //3. 取消广播超时 ANR 消息
                    cancelBroadcastTimeoutLocked();
					...
                }
            } while (r == null);
			...
			//获取下有序广播
            r.receiverTime = SystemClock.uptimeMillis();
            ...
            if (! mPendingBroadcastTimeoutMessage) {
                long timeoutTime = r.receiverTime + mTimeoutPeriod;
                //设置广播超时
                setBroadcastTimeoutLocked(timeoutTime);
            }
			...
	   }
    }
```

　　上面的 step 1.broadcastTimeoutLocked(false) 函数：记录时间信息并调用函数设置发送延时消息

```java
    final void broadcastTimeoutLocked(boolean fromMsg) {
        ...
        long now = SystemClock.uptimeMillis();
        BroadcastRecord r = mOrderedBroadcasts.get(0);
        if (fromMsg) {
            if (mService.mDidDexOpt) {
                // Delay timeouts until dexopt finishes.
                mService.mDidDexOpt = false;
                long timeoutTime = SystemClock.uptimeMillis() + mTimeoutPeriod;
                setBroadcastTimeoutLocked(timeoutTime);
                return;
            }
            if (!mService.mProcessesReady) {
                // Only process broadcast timeouts if the system is ready. That way
                // PRE_BOOT_COMPLETED broadcasts can't timeout as they are intended
                // to do heavy lifting for system up.
                return;
            }

            long timeoutTime = r.receiverTime + mTimeoutPeriod;
            if (timeoutTime > now) {
                // step 2
                setBroadcastTimeoutLocked(timeoutTime);
                return;
            }
        }

        ...
    }

```

　　上面的 step 2 setBroadcastTimeoutLocked 函数：设置广播超时具体操作，同样是发送延时消息。

```java
    final void setBroadcastTimeoutLocked(long timeoutTime) {
        if (! mPendingBroadcastTimeoutMessage) {
            Message msg = mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG, this);
            mHandler.sendMessageAtTime(msg, timeoutTime);
            mPendingBroadcastTimeoutMessage = true;
        }
    }
```

###### setBroadcastTimeoutLocked(long timeoutTime) 函数的参数 timeoutTime 是当前时间加上设定好的超时时间。

　　也就是：

```java
	long timeoutTime = SystemClock.uptimeMillis() + mTimeoutPeriod;
```

　　mTimeoutPeriod 也就是前台队列的 10s 和后台队列的 60s。

```java
public final class ActivityManagerService extends ActivityManagerNative
        implements Watchdog.Monitor, BatteryStatsImpl.BatteryCallback {
	...
    // How long we allow a receiver to run before giving up on it.
    static final int BROADCAST_FG_TIMEOUT = 10*1000;
    static final int BROADCAST_BG_TIMEOUT = 60*1000;
	...
    // Note: This method is invoked on the main thread but may need to attach various
    // handlers to other threads.  So take care to be explicit about the looper.
    public ActivityManagerService(Context systemContext) {
		...
        mFgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "foreground", BROADCAST_FG_TIMEOUT, false);
        mBgBroadcastQueue = new BroadcastQueue(this, mHandler,
                "background", BROADCAST_BG_TIMEOUT, true);
		...
	}
	...
}
```

###### 在 processNextBroadcast() 过程，执行完 performReceiverLocked 后调用 cancelBroadcastTimeoutLocked

　　cancelBroadcastTimeoutLocked：处理广播消息函数 processNextBroadcast() 中 performReceiveLocked() 处理广播消息完毕则调用 cancelBroadcastTimeoutLocked() 取消超时消息。

```java
BroadcastQueue.java
    final void cancelBroadcastTimeoutLocked() {
        if (mPendingBroadcastTimeoutMessage) {
            mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            mPendingBroadcastTimeoutMessage = false;
        }
    }
```

#### ContentProvider 的 ContentProvider Timeout

　　ContentProvider Timeout 是位于 “ActivityMananger” 线程中的 AMS.MainHandler 收到 CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG 消息时触发。

## Android ANR 的信息收集

　　无论是四大组件或者进程等只要发生 ANR，最终都会调用 AMS.appNotResponding() 方法。


## 参考文章

[说说 Android 中的 ANR](https://droidyue.com/blog/2015/07/18/anr-in-android/)
[Android ANR：原理分析及解决办法](https://www.jianshu.com/p/388166988cef)
[Android性能优化（七）之你真的理解ANR吗？](https://juejin.im/post/58e5bd6dda2f60005fea525c)

