# 发生 ANR 条件的源码分析

## Service 造成的 Service Timeout

　　Service Timeout 是位于 “ ActivityMananger ” 线程中的 AMS.MainHandler 收到 SERVICE_TIMEOUT_MSG 消息时触发。

### 1. 发送延时消息

　　Service 进程 attach 到 system_server 进程的过程中会调用 realStartServiceLocked ，紧接着 mAm.mHandler.sendMessageAtTime() 来发送一个延时消息，延时的时长是定义好的，如前台 Service 的 20 秒。ActivityMananger 线程中的 AMS.MainHandler 收到 SERVICE_TIMEOUT_MAG 消息时会触发。

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

在 ActiveService 的 bumpServiceExecutingLocked() 方法中发出一条 SERVICE_TIMEOUT_MSG 的延时消息：

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

### 2. 创建 Service

　　经过 Binder 等层层调用进入目标进程的主线程 handleCreateService(CreateServiceData data)。在主线程会创建 Service。

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

### 3. 完成则取消延时消息

​		在 system_server 会执行取消 AMS.MainHandler 的延时消息，ActiveService 的 serviceDoneExecutingLocked() 方法，在 serviceDoneExecutingLocked() 方法中会移除刚刚延时发送的 Message ：

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

### 4. 超时出现 ANR

　　规定时间之内，如果没有完成方法的执行，则会出现 ANR。

​		如果 Message 没有被 mAm.mHandler(也就是 ActivityManagerService 中的 MainHandler)及时 remove 掉，被执行的话就会出现 ANR 的发生；执行到 ActivityMannagerService 中的 MainHandler 的 SERVICE_TIMEOUT_MSG 然后调用到 ActiveServices 的 serviceTimeout() 方法，最终执行到 AppErrors 的 appNotResponding() 方法。

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

### 5. 流程总结

1. Service 创建之前会延迟发送一个消息，而这个消息就是 ANR 的起源；
2. Service 创建完毕，在规定的时间之内执行完毕 onCreate() 方法就移除这个消息，就不会产生 ANR 了；
3. 在规定的时间之内没有完成 onCreate() 的调用，消息被执行，ANR 发生。

## BroadcastReceiver 造成的 BroadcastQueue Timeout

　　BroadcastReceiver Timeout 是位于 “ ActivityMananger ” 线程中的 BroadcastQueue.BroadcastHandler 收到 BROADCAST_TIMEOUT_MSG 消息时触发。

### 1. 发送延时消息

　　广播函数 processNextBroadcast() 中 broadcastTimeoutLocked(false) 会发送延时消息。广播处理顺序为先处理并行广播，再处理当前有序广播。

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
                        //1.发送延时消息，这个函数处理了很多事情，比如广播处理超时结束广播
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

​		setBroadcastTimeoutLocked(long timeoutTime) 函数的参数 timeoutTime 是当前时间加上设定好的超时时间。

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

### 2. 取消广播

　　在 processNextBroadcast() 过程，执行完 performReceiverLocked 后调用 cancelBroadcastTimeoutLocked

　　cancelBroadcastTimeoutLocked：处理广播消息函数 processNextBroadcast() 中 performReceiveLocked() 处理广播消息完毕则调用 cancelBroadcastTimeoutLocked() 取消超时消息。

```java
    final void cancelBroadcastTimeoutLocked() {
        if (mPendingBroadcastTimeoutMessage) {
            mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            mPendingBroadcastTimeoutMessage = false;
        }
    }
```

### 3.流程总结

1. BroadcastReceiver 在获取广播接收者后，会延迟发送一个消息，而这个消息就是 ANR 的起源，前台广播延时 10 秒，后台广播延时 60 秒；
2. 处理广播消息之后就会移除延时消息，就不会产生 ANR 了；
3. 如果在延时时间之内没有处理完广播消息，延时消息被执行，ANR 发生。

## 总结

​		四大组件发生 ANR 的流程基本都是：

1.  发送延时消息
2. 执行相应方法
3. 方法在延时时间内执行完成，取消消息
4. 方法在延时时间内未执行完成，消息触发
5. 接收延时消息，发生 ANR ，调用 AMS.appNotResponding 方法。


## 参考文章

[Android ANR：原理分析及解决办法](https://www.jianshu.com/p/388166988cef)

[Android性能优化（七）之你真的理解ANR吗？](https://juejin.im/post/58e5bd6dda2f60005fea525c)

