# BroadcastReceiver 的基础知识

## 介绍
　　BroadcastReceiver ，广播接受者，用来接收来自系统和应用中的广播，是 Android 四大组件之一。

　　在 Android 系统中，广播体现在方方面面，例如当开机完成后系统会产生一条广播，接收到这条广播就能实现开机启动服务的功能；当网络状态改变时系统会产生一条广播，接收到这条广播就能及时地做出提示和保护数据等操作；当电池电量改变时，系统会产生一条广播，接收到这条广播就能在电量低时告知用户及时保存进程等等。Android 中的广播机制设计的非常出色，很多事情原本需要开发者亲自操作的，现在只需等待广播告知自己就可以了，大大减少了开发的工作量和开发周期。

　　应用场景：Android 不同组件间的通信（含：应用内/不同应用之间）、多线程通信、与 Android 系统在特性情况下的通信（如：电话呼入时、网络可用时）。

## BroadcastReceiver 的两种常用类型

#### Normalbroadcasts：默认广播
　　发送一个默认广播使用 Context.sendBroadcast() 方法，普通广播对于多个接受者来说是完全异步的，通常每个接受者都无需等待即可接收到广播，接受者相互之间不会有影响。对于这种广播，接收者无法终止广播，即无法阻止其他接收者的接收动作。

#### orderedbroadcasts：有序广播
　　发送一个有序广播使用 Context.sendorderedBroadcast() 方法，有序广播比较特殊，它每次只发送到优先级较高的接受者那里，然后由优先级高的接受者再传播到优先级低的接受者那里，优先级高的接受者有能力终止这个广播。

　　发送有序广播：sendorderedBroadcast()

　　在注册广播中的 <intent-filter> 中使用 android:priority 属性。这个属性的范围在 -1000 到 1000 ，数值越大，优先级越高。在广播接受器中使用 setResultExtras 方法将一个 Bundle 对象设置为结果集对象，传递到下一个接收者那里，这样优先级低的接受者可以用 getResultExtras 获取到最新的经过处理的信息集合。使用 sendorderedBroadcast 方法发送有序广播时，需要一个权限参数，如果为 null 则表示不要求接受者声明指定的权限，如果不为 null 则表示接受者若要接收此广播，需声明指定权限。这样做是从安全角度考虑的，例如系统的短信就是有序广播的形式，一个应用可能是具有拦截垃圾短信的功能，当短信到来时它可以先接收到短信广播，必要时终止广播传递，必要时终止广播传递，这样的软件就必须声明接收短信的权限。

## 静态和动态注册方式
　　构建 Intent ，使用 sendBroadcast 方法发出广播定义一个广播接收器，该广播接收器继承 BroadcastReceiver，并且覆盖 onReceive() 方法来响应事件。注册该广播接收器，可以在代码中注册（动态注册），也可以在 AndroidManifest.xml 配置文件中注册（静态注册）。

#### 两种注册方式区别
　　广播接收器注册一种有两种形式：静态注册和动态注册。

　　两者及其接收广播的区别：

　　（1）动态注册广播不是常驻型广播，也就是说广播跟随 Activity 的生命周期。注意在 Activity 结束前，移除广播接收器。静态注册是常驻型，也就是说当应用程序关闭后，如果有信息广播来，程序也会被系统调用自动运行。

　　（2）当广播为有序广播时：优先级高的先接收（不分静态和动态）。同优先级的广播接收器，动态优先于静态。当广播为默认广播时：无视优先级，动态广播接收器优先于静态广播接收器。

　　（3）同优先级的同类广播接收器，静态：先扫描的优先于后扫描的。动态：先注册优先于后注册的。

　　（4）静态注册是在 AndroidManifesy.xml 里通过< receive > 标签声明的。不受任何组件的声明周期影响，缺点是耗电和占内存，适合在需要时刻监听使用。动态注册在代码中调用 Context.registerReceiver() 方法注册，比较灵活，适合在需要特定时刻监听使用。

## 使用
　　自定义广播接收者 BroadcastReceiver 需要继承 BroadcastReceiver 继承，并复写抽象方法  onReceive() 方法。

　　广播接收器接收到相应广播后，会自动回调 onReceive() 方法。一般情况下，onReceive 方法会涉及与其他组件之间的交互，如发送 Notification、启动 Service 等。默认情况下，广播接收器运行在 UI 线程，因此，onReceive() 方法不能执行耗时操作，否则将导致 ANR 。

#### 静态注册
　　注册方式：在 AndroidManifest.xml 里通过 receiver 标签声明。

　　属性说明：
```
<receiver
	//broadcastReceiver 是否接收其他 App 发出的广播
	android:enabled=["true" | "false"]
	//默认值是由 receiver 中有无 intent-filter 决定的：如果有 intetn-filter，默认为 true，否则为 false
	android:exported=["true" | "false"]
	android:icon="drawable resource"
	android:label="string resource"
	//继承 BroadcastReceiver 子类的类名
	android:name=".MyBroadcastReceiver"
	//具有相应权限的广播发送者发送的广播才能被此 BroadcastReceiver 所接收。
	android:permission="string"
	//BroadcastReceiver 运行所处的进程，默认为 app 进程，可以指定独立进程
	android:process="string"
>
	//用于指定此广播接收器将接收的广播类型
	<intent-filter>
		<action android:name="android.net.conn.CONNECTIVITY_CHANGE">
	</intent-filter>
</receiver>
```
　　当 App 首次启动时，系统会自动实例化 BroadcastReceiver 类，并注册到系统中。

#### 动态注册
　　注册方式：在代码中调用 Context.registerReceiver() 方法。

　　使用：调用 registerReceiver() 注册广播，使用 unregisterReceiver() 销毁广播。

　　动态广播最好在 Activity 的 onResume() 注册、onPause() 注销。有注册就必然得有注销，否则会导致内存泄漏。不在 onCreate() 与 onDestory() 或 onStart() 与 onStop() 注册、注销是因为当系统因为内存不足要回收 Activity 占用的资源时，Activity 在执行完 onPause() 方法后就会被销毁，有些生命周期方法 onStop()、onDestory() 就不会执行。当再回到此 Activity 时，是从 onCreate() 方法开始执行的。

　　假设将广播的注销放在 onStop()、onDestory() 方法里的话，有可能在 Activity 被销毁后还为执行 onStop()、onDestory() 方法，即广播仍还未注销，从而导致内存泄漏。但是，onPause() 一定会被执行，从而保证了广播在 App 死亡前一定会被注销，从而防止内存泄漏。

## 广播发送者向 AMS 发送广播

#### 广播的发送
　　广播是用“意图（Intent）”标识，定义广播的本质是定义广播所具备的“意图（Intent）”，广播发送就是广播发送者将此广播的“意图（Intent）”通过 sendBroadcast() 方法发送出去。

#### 广播的类型
　　广播的类型主要分为 5 类：
* 普通广播（Normal Broadcast）
* 系统广播（System Broadcast）
* 有序广播（Ordered Broadcast）
* 粘性广播（Sticky Broadcast）
* App 应用内广播（Local Broadcast）

###### 普通广播（Normal Broadcast）
　　开发者自身定义 Intent 的广播（最常用）。使用 sendBroadcast() 发送广播。

###### 系统广播（System Broadcast）
　　Android 中内置了多个系统广播：只要涉及到手机的基本操作（如开机、网络状态变化、拍照等等），都会发出相应的广播。

　　每个广播都有特定的 Intent-Filter(包括具体的 action)，Android 系统广播 action 如下：

| 系统操作 | action |
|--------|--------|
| 监听网络变化 | android.net.conn.CONNECTIVITY_CHANGE |
| 关闭或打开飞行模式 | Intent.ACTION_AIRPLANE_MODE_CHANGED |
| 充电时或电量发生变化 | Intent.ACTION_BATTERY_CHANGED |
| 电池电量低 | Intent.ACTION_BATTERY_LOW |
| 电池电量充足（即从电量低变化到饱满时会发出广播） | Intent.ACTION_BATTERY_OKAY |
| 系统启动完成后（仅广播一次） | Intent.ACTION_BOOT_COMPLETE |
| 按下照相时的拍照按钮（硬件按键）时 | Intent.ACTION_CAMERA_BUTTON |
| 屏幕锁屏 | Intent.ACTION_CLOSE_SYSTEM_DIALOGS |
| 设备当前设置被改变时（界面语言、设备方向等） | Intent.ACTION_CONFIGURATION_CHANGED |
| 插入耳机时 | Intent.ACTION_HEADSET_PLUG |
| 未正确移除 SD 卡但已取出来时（正确移除方法：设置-SD 卡和设备内存-卸载 SD 卡） | Intent.ACTION_MEDIA_BAD_REMOVAL |
| 插入外部存储装置（如 SD 卡） | Intent.ACTION_MEDIA_CHECKING |
| 成功安装 APK | Intent.ACTION_PACKAGE_ADDED |
| 成功删除 APK | Intent.ACTION_PACKAGE_REMOVED |
| 重启设备 | Intent.ACTION_REBOOT |
| 屏幕被关闭 | Intent.ACTION_SCREEN_OFF |
| 屏幕被打开 | Intent.ACTION_SCREEN_ON |
| 关闭系统时 | Intent.ACTION_SHUTDOWN |
| 重启设备 | Intent.ACTION_REBOOT |

　　当使用系统广播时，只需要在注册广播接收者时定义相关的 action 即可，并不需要手动发送广播，当系统有相关操作时会自动进行系统广播。

###### 有序广播
　　有序广播：发送出去的广播被广播接受者按照先后顺序接收。

　　广播接受者接收广播的顺序负责（同时面向静态和动态注册的广播接收者）：1.按照 Priority 属性值从大-小排序；2.Priority 属性相同者，动态注册的广播优先。

　　特定：1.接收广播按顺序接收；2.先接收的广播接收者可以对广播进行截断，即后接收的广播接收者不再接收到此广播；3.先接收的广播接收者可以对广播进行修改，那么后接收的广播接收者将接收到被修改后的广播。

　　具体使用：有序广播的使用过程与普通广播非常类似，差异在于广播的发送方式：sendOrderBroadcast()。

###### App 应用内广播（Local Broadcast）
　　Android 中的广播可以跨 App 直接通信（exported 对于有 intent-filter 情况下默认值为 true）。

　　冲突：可能出现的问题:1.其他 App 针对性发出与当前 App intent-filter 相匹配的广播，由此导致当前 App 不断接收广播并处理；2.其他 App 注册与当前 App 一致的 intent-filter 用于接收广播，获取广播具体信息。即会出现安全性和效率性的问题。

　　解决方案：使用 App 应用内广播（Local Broadcast）。1.App 应用内广播可以理解为一种局部广播，广播的发送者和接收者都同属于一个 App。2.相比于全局广播（普通广播），App 应用内广播优势体现在：安全性高和效率高。

　　具体使用1-将全局广播设置为局部广播：1.注册广播时将 exported 属性设置为 false，使得非本 App 内部发出的此广播不被接收；2.在广播发送和接收时，增设相应权限 permission，用于权限验证；3.发送广播时指定该广播接收器所在的包名，此广播将只会发送到此包中的 App 内与之相匹配的有效广播接收器中。

　　具体使用2-使用封装好的 LocalBroadcastManager 类，使用方式上与全局广播几乎相同，只是注册/取消注册广播接收器和发送广播是将参数的 context 变成了 LocalBroadcastManager 的单一实例。对于 LocalBroadcastManager 方式发送的应用内广播，只能通过 LocalBroadcastMananger 动态注册，不能静态注册。

###### 粘性广播（Sticky Broadcast）
　　在 Android 5.0 (API 21)中已经失效，不建议使用。

#### 注意
　　对于不同注册方式的广播接收器回调 onReceiver(Context context,Intent intent)中的 context 返回值是不一样的：
* 对于静态注册（全局+应用内广播），回调onReceiver(Context，intent)中的 context 返回值是：广播接收者受限的 Context；
* 对于全局广播的动态注册，回调 onReceiver(Context,intent)中的 context 返回值是：Activity context；
* 对于应用内广播的动态注册（LocalBroadcastMananger 方式），回调 onReceive(Context,intent)中的 context 返回值是：Application context；
* 对于应用内广播的动态注册（非 LocalBroadcastManager 方法），回调 onReceive(context,intent)中的 context 返回值：Activity context。

## 实现原理

#### 采用的模型
　　Android 中的广播使用了设计模式中的观察者模式：基于消息的发布 / 订阅事件模型。Android 将广播的发送者和接受者解耦，使得系统方便集成、更易扩展。

#### 模型讲解
　　模型中有 3 个角色：1.消息订阅者（广播接受者），2.消息发布者（广播发布者），3.消息中心（AMS，即 Activity Manager Service）。
![](image/广播原理图.png)

#### 静态广播的注册
　　静态广播是通过 PackageManagerService 在启动的时候扫描已安装的应用去注册的。

　　在 PackageManagerservice 的构造方法中，会去扫描应用安装目录，顺序是先扫描系统应用安装目录再去扫描第三方应用安装目录。

　　PaackageManagerService.scanDirLI 就是用于扫描目录的方法：
```
    private void scanDirLI(File dir, final int parseFlags, int scanFlags, long currentTime) {
        final File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Log.d(TAG, "No files in app dir " + dir);
            return;
        }

        ...

        for (File file : files) {
            final boolean isPackage = (isApkFile(file) || file.isDirectory())
                    && !PackageInstallerService.isStageName(file.getName());
            if (!isPackage) {
                // Ignore entries which are not packages
                continue;
            }
            try {
                scanPackageTracedLI(file, parseFlags | PackageParser.PARSE_MUST_BE_APK,
                        scanFlags, currentTime, null);
            } catch (PackageManagerException e) {
                Slog.w(TAG, "Failed to parse " + file + ": " + e.getMessage());

                // Delete invalid userdata apps
                if ((parseFlags & PackageParser.PARSE_IS_SYSTEM) == 0 &&
                        e.error == PackageManager.INSTALL_FAILED_INVALID_APK) {
                    logCriticalInfo(Log.WARN, "Deleting invalid package at " + file);
                    removeCodePathLI(file);
                }
            }
        }
    }
```
　　可以看到，它通过 isApkFile() 方法将目录下的所有后缀为 “.apk” 的文件传给 scanPackageTrancedLI() 方法去处理。

　　而 scanPackageTrancedLI(File scanFile, final int parseFlags, int scanFlags, long currentTime, UserHandle user) 内部会调用 scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user)方法：
```
    /**
     *  Traces a package scan.
     *  追踪包扫描
     *  @see #scanPackageLI(File, int, int, long, UserHandle)
     */
    private PackageParser.Package scanPackageTracedLI(File scanFile, final int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        Trace.traceBegin(TRACE_TAG_PACKAGE_MANAGER, "scanPackage");
        try {
            return scanPackageLI(scanFile, parseFlags, scanFlags, currentTime, user);
        } finally {
            Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
        }
    }
```
　　在 scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user)内部会调用它的重载方法 scanPackageLI(PackageParser.Package pkg, File scanFile, final int policyFlags, int scanFlags, long currentTime, UserHandle user)：
```
    /**
     *  Scans a package and returns the newly parsed package.
     *  扫描包并返回一个新解析的包
     *  Returns {@code null} in case of errors and the error code is stored in mLastScanError
     */
    private PackageParser.Package scanPackageLI(File scanFile, int parseFlags, int scanFlags,
            long currentTime, UserHandle user) throws PackageManagerException {
        if (DEBUG_INSTALL) Slog.d(TAG, "Parsing: " + scanFile);
        PackageParser pp = new PackageParser();
        pp.setSeparateProcesses(mSeparateProcesses);
        pp.setOnlyCoreApps(mOnlyCore);
        pp.setDisplayMetrics(mMetrics);

        if ((scanFlags & SCAN_TRUSTED_OVERLAY) != 0) {
            parseFlags |= PackageParser.PARSE_TRUSTED_OVERLAY;
        }

        Trace.traceBegin(TRACE_TAG_PACKAGE_MANAGER, "parsePackage");
        final PackageParser.Package pkg;
        try {
            pkg = pp.parsePackage(scanFile, parseFlags);
        } catch (PackageParserException e) {
            throw PackageManagerException.from(e);
        } finally {
            Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
        }

        return scanPackageLI(pkg, scanFile, parseFlags, scanFlags, currentTime, user);
    }
```
　　在 scanPackageLI(PackageParser.Package pkg, File scanFile, final int policyFlags, int scanFlags, long currentTime, UserHandle user) 内部会调用 scanPackageInternalLI(PackageParser.Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user) 扫描文件：
```
    /**
     *  Scans a package and returns the newly parsed package.
     *  扫描包并返回一个新解析的包
     *  @throws PackageManagerException on a parse error.
     */
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, File scanFile,
            final int policyFlags, int scanFlags, long currentTime, UserHandle user)
            throws PackageManagerException {
        // If the package has children and this is the first dive in the function
        // we scan the package with the SCAN_CHECK_ONLY flag set to see whether all
        // packages (parent and children) would be successfully scanned before the
        // actual scan since scanning mutates internal state and we want to atomically
        // install the package and its children.
        if ((scanFlags & SCAN_CHECK_ONLY) == 0) {
            if (pkg.childPackages != null && pkg.childPackages.size() > 0) {
                scanFlags |= SCAN_CHECK_ONLY;
            }
        } else {
            scanFlags &= ~SCAN_CHECK_ONLY;
        }

        // Scan the parent
        PackageParser.Package scannedPkg = scanPackageInternalLI(pkg, scanFile, policyFlags,
                scanFlags, currentTime, user);

        // Scan the children
        final int childCount = (pkg.childPackages != null) ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            PackageParser.Package childPackage = pkg.childPackages.get(i);
            scanPackageInternalLI(childPackage, scanFile, policyFlags, scanFlags,
                    currentTime, user);
        }


        if ((scanFlags & SCAN_CHECK_ONLY) != 0) {
            return scanPackageLI(pkg, scanFile, policyFlags, scanFlags, currentTime, user);
        }

        return scannedPkg;
    }
```
　　在 scanPackageInternalLI(PackageParser.Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user) 方法会调用 scanPackageLI(PackageParser.Package pkg, final int policyFlags, int scanFlags, long currentTime, UserHandle user) 方法：
```
    /**
     *  Scans a package and returns the newly parsed package.
     *  扫描包并返回一个新的解析包
     *  @throws PackageManagerException on a parse error.
     */
    private PackageParser.Package scanPackageInternalLI(PackageParser.Package pkg, File scanFile,
            int policyFlags, int scanFlags, long currentTime, UserHandle user)
            throws PackageManagerException {
        PackageSetting ps = null;
        PackageSetting updatedPkg;
		...

        // Note that we invoke the following method only if we are about to unpack an application
        PackageParser.Package scannedPkg = scanPackageLI(pkg, policyFlags, scanFlags
                | SCAN_UPDATE_SIGNATURE, currentTime, user);

        /*
         * If the system app should be overridden by a previously installed
         * data, hide the system app now and let the /data/app scan pick it up
         * again.
         */
        if (shouldHideSystemApp) {
            synchronized (mPackages) {
                mSettings.disableSystemPackageLPw(pkg.packageName, true);
            }
        }

        return scannedPkg;
    }
```
　　在 scanPackageInternalLI(PackageParser.Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user) 方法中调用了 scanPackageLI(PackageParser.Package pkg, final int policyFlags, int scanFlags, long currentTime, UserHandle user) 方法：

```
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, final int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        boolean success = false;
        try {
            final PackageParser.Package res = scanPackageDirtyLI(pkg, policyFlags, scanFlags,
                    currentTime, user);
            success = true;
            return res;
        } finally {
            if (!success && (scanFlags & SCAN_DELETE_DATA_ON_FAILURES) != 0) {
                // DELETE_DATA_ON_FAILURES is only used by frozen paths
                destroyAppDataLIF(pkg, UserHandle.USER_ALL,
                        StorageManager.FLAG_STORAGE_DE | StorageManager.FLAG_STORAGE_CE);
                destroyAppProfilesLIF(pkg, UserHandle.USER_ALL);
            }
        }
    }
```
　　在  scanPackageLI(PackageParser.Package pkg, final int policyFlags, int scanFlags, long currentTime, UserHandle user) 方法调用了 scanPackageDirtyLI(PackageParser.Package pkg, final int policyFlags, final int scanFlags, long currentTime, UserHandle user) 方法，scanPackageDirtyLI() 方法会解析 Package 并且将 AndroidManifest.xml 中注册的 BroadcastReceiver 保存下来 ：
```
    private PackageParser.Package scanPackageDirtyLI(PackageParser.Package pkg,
            final int policyFlags, final int scanFlags, long currentTime, UserHandle user)
            throws PackageManagerException {
		...
		    N = pkg.services.size();
            r = null;
            for (i=0; i<N; i++) {
                PackageParser.Service s = pkg.services.get(i);
                s.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        s.info.processName, pkg.applicationInfo.uid);
                mServices.addService(s);
                if ((policyFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(s.info.name);
                }
            }
		...
	}
```
　　所以从上面获取静态广播的流程可以看出：系统应用的广播先于第三方应用的广播注册，而安装在同一目录下的应用的静态广播的注册顺序是按照 File.list 列出来的 apk 的顺序注册的，他们的注册顺序就决定了它们接收广播的顺序。

　　通过静态广播的注册流程，已经将静态广播注册到了 PackageManagerService 的 mReceivers 中，可以使用 PackageManagerService.queryIntentReceivers 方法查询 intent 对应的静态广播：
```
    @Override
    public @NonNull ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent,
            String resolvedType, int flags, int userId) {
        return new ParceledListSlice<>(
                queryIntentReceiversInternal(intent, resolvedType, flags, userId));
    }
```
　　queryIntentReceivers() 方法调用了 queryIntentReceiversInternal(Intent intent, String resolvedType, int flags, int userId) 方法来查询：
```
    private @NonNull List<ResolveInfo> queryIntentReceiversInternal(Intent intent,
            String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) return Collections.emptyList();
        flags = updateFlagsForResolve(flags, userId, intent);
        ComponentName comp = intent.getComponent();
        if (comp == null) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                comp = intent.getComponent();
            }
        }
        if (comp != null) {
            List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
            ActivityInfo ai = getReceiverInfo(comp, flags, userId);
            if (ai != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            return list;
        }

        // reader
        synchronized (mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName == null) {
                return mReceivers.queryIntent(intent, resolvedType, flags, userId);
            }
            final PackageParser.Package pkg = mPackages.get(pkgName);
            if (pkg != null) {
                return mReceivers.queryIntentForPackage(intent, resolvedType, flags, pkg.receivers,
                        userId);
            }
            return Collections.emptyList();
        }
    }
```

#### 动态广播的注册
　　调用 Context.registerReceiver 最后会调用 ActivityManagerService.registerReceiver：
```
    public Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter, String permission, int userId) {
		...
		ReceiverList rl = mRegisteredReceivers.get(receiver.asBinder());
		...
		BroadcastFilter bf = new BroadcastFilter(filter, rl, callerPackage,
                    permission, callingUid, userId);
        rl.add(bf);
		mReceiverResolver.addFilter(bf);
		...
	}
```
　　通过 mReceiverResolver.queryIntent() 就能获得 intent 对应的动态广播了。

#### 发送广播
　　ContextImpl.sendBroadcast 中会调用 ActivityManagerService.getDefault().broadcastIntent()：
```
    @Override
    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManagerNative.getDefault().broadcastIntent(
                    mMainThread.getApplicationThread(), intent, resolvedType, null,
                    Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                    getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```
　　实际是调用 ActivityManagerService.broadcastIntent：
```
    public final int broadcastIntent(IApplicationThread caller,
            Intent intent, String resolvedType, IIntentReceiver resultTo,
            int resultCode, String resultData, Bundle resultExtras,
            String[] requiredPermissions, int appOp, Bundle bOptions,
            boolean serialized, boolean sticky, int userId) {
        enforceNotIsolatedCaller("broadcastIntent");
        synchronized(this) {
            intent = verifyBroadcastLocked(intent);

            final ProcessRecord callerApp = getRecordForAppLocked(caller);
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            int res = broadcastIntentLocked(callerApp,
                    callerApp != null ? callerApp.info.packageName : null,
                    intent, resolvedType, resultTo, resultCode, resultData, resultExtras,
                    requiredPermissions, appOp, bOptions, serialized, sticky,
                    callingPid, callingUid, userId);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }
```
　　ActivityManangerService.broadcastIntent() 中又会调用 ActivityManangerService.broadcastIntentLocked() 方法：
```
    final int broadcastIntentLocked(ProcessRecord callerApp,
            String callerPackage, Intent intent, String resolvedType,
            IIntentReceiver resultTo, int resultCode, String resultData,
            Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions,
            boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
		...
	    // Figure out who all will receive this broadcast.
		// 静态广播
        List receivers = null;
		//动态广播
        List<BroadcastFilter> registeredReceivers = null;
		 // Need to resolve the intent to interested receivers...
        if ((intent.getFlags()&Intent.FLAG_RECEIVER_REGISTERED_ONLY)
                 == 0) {
			// 查询静态广播
            receivers = collectReceiverComponents(intent, resolvedType, callingUid, users);
        }
		if (intent.getComponent() == null) {
			//查询动态广播
            if (userId == UserHandle.USER_ALL && callingUid == Process.SHELL_UID) {
                // Query one target user at a time, excluding shell-restricted users
                for (int i = 0; i < users.length; i++) {
                    if (mUserController.hasUserRestriction(
                            UserManager.DISALLOW_DEBUGGING_FEATURES, users[i])) {
                        continue;
                    }
                    List<BroadcastFilter> registeredReceiversForUser =
                            mReceiverResolver.queryIntent(intent,
                                    resolvedType, false, users[i]);
                    if (registeredReceivers == null) {
                        registeredReceivers = registeredReceiversForUser;
                    } else if (registeredReceiversForUser != null) {
                        registeredReceivers.addAll(registeredReceiversForUser);
                    }
                }
            } else {
                registeredReceivers = mReceiverResolver.queryIntent(intent,
                        resolvedType, false, userId);
            }
        }

		final boolean replacePending =
                (intent.getFlags()&Intent.FLAG_RECEIVER_REPLACE_PENDING) != 0;

        int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
        if (!ordered && NR > 0) {
            // If we are not serializing this broadcast, then send the
            // registered receivers separately so they don't wait for the
            // components to be launched.
            if (isCallerSystem) {
                checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                        isProtectedBroadcast, registeredReceivers);
            }
            final BroadcastQueue queue = broadcastQueueForIntent(intent);
            BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                    callerPackage, callingPid, callingUid, resolvedType, requiredPermissions,
                    appOp, brOptions, registeredReceivers, resultTo, resultCode, resultData,
                    resultExtras, ordered, sticky, false, userId);
            final boolean replaced = replacePending && queue.replaceParallelBroadcastLocked(r);
            if (!replaced) {
				//发送动态广播
                queue.enqueueParallelBroadcastLocked(r);
                queue.scheduleBroadcastsLocked();
            }
            registeredReceivers = null;
            NR = 0;
        }
		...
		 if ((receivers != null && receivers.size() > 0)
                || resultTo != null) {
            BroadcastQueue queue = broadcastQueueForIntent(intent);
            BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                    callerPackage, callingPid, callingUid, resolvedType,
                    requiredPermissions, appOp, brOptions, receivers, resultTo, resultCode,
                    resultData, resultExtras, ordered, sticky, false, userId);

            boolean replaced = replacePending && queue.replaceOrderedBroadcastLocked(r);
            if (!replaced) {
				//发送静态广播
                queue.enqueueOrderedBroadcastLocked(r);
                queue.scheduleBroadcastsLocked();
            }
        }
	}
```
　　动态广播会优先于静态广播，从上面代码可以看到，原因就是因为安卓的源代码就是按这个顺序写的。

　　再来看一下 ActivityManagerService.collectReceiverComponents 方法，实际上静态广播就是从 PackageManagerService 中查询的：
```
    private List<ResolveInfo> collectReceiverComponents(Intent intent, String resolvedType,
            int callingUid, int[] users) {
        ...
        List<ResolveInfo> newReceivers = AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, pmFlags, user).getList();
		...
	}
```

#### 广播队列
　　从 ActivityManangerService.broadcastIntentLocked 中可以看到，实际上它不是直接将广播发送到 BroadcastReceiver 中的，而是将它包装到 BroadcastRecord 中，再放进 BroadcastQueue：
```
	public Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter, String permission, int userId) {
 			final BroadcastQueue queue = broadcastQueueForIntent(intent);
            BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                    callerPackage, callingPid, callingUid, resolvedType, requiredPermissions,
                    appOp, brOptions, registeredReceivers, resultTo, resultCode, resultData,
                    resultExtras, ordered, sticky, false, userId);
            final boolean replaced = replacePending && queue.replaceParallelBroadcastLocked(r);
            if (!replaced) {
                queue.enqueueParallelBroadcastLocked(r);
                queue.scheduleBroadcastsLocked();
            }
	}
```
　　enqueueParallelBroadcastLocked() 方法用于并发执行广播的发送，很简单，就是将 BroadcastRecord 放到了 mParallelBroadcasts 中：
```
    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        mParallelBroadcasts.add(r);
        r.enqueueClockTime = System.currentTimeMillis();
    }
```
　　scheduleBroadcastsLocked 方法同样很简单，就是向 mHandler 发送了一个 BROADCAST_INTENT_MSG 消息：
```
    public void scheduleBroadcastsLocked() {
        if (mBroadcastsScheduled) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(BROADCAST_INTENT_MSG, this));
        mBroadcastsScheduled = true;
    }
```
　　再去看看BroadcastQueue： mHandler 在接收到 BROADCAST_INTENT_MSG 消息的处理：
```
   private final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BROADCAST_INTENT_MSG: {
                    processNextBroadcast(true);
                } break;
                case BROADCAST_TIMEOUT_MSG: {
                    synchronized (mService) {
                        broadcastTimeoutLocked(true);
                    }
                } break;
            }
        }
    }
```
　　processNextBroadcast 方法用于从队列中获取广播消息并发送给 BroadcastReceiver，它内部有两个分支，并行处理和串行处理。

#### 并行处理
　　例如动态注册的非有序广播等就是使用并行处理，先看并行处理的分支：
```
    final void processNextBroadcast(boolean fromMsg) {
        synchronized(mService) {
            BroadcastRecord r;

            mService.updateCpuStats();

            if (fromMsg) {
                mBroadcastsScheduled = false;
            }

            // First, deliver any non-serialized broadcasts right away.
            while (mParallelBroadcasts.size() > 0) {
                r = mParallelBroadcasts.remove(0);
                r.dispatchTime = SystemClock.uptimeMillis();
                r.dispatchClockTime = System.currentTimeMillis();
                final int N = r.receivers.size();
                for (int i=0; i<N; i++) {
                    Object target = r.receivers.get(i);
           			// 发送消息给 Receiver
                    deliverToRegisteredReceiverLocked(r, (BroadcastFilter)target, false, i);
                }
                addBroadcastToHistoryLocked(r);
            }
			...
		}
		...
	}

    private void deliverToRegisteredReceiverLocked(BroadcastRecord r,
            BroadcastFilter filter, boolean ordered, int index) {
		...
		//获取 BroadcastReceiver 的 Binder
		r.receiver = filter.receiverList.receiver.asBinder();
		...
		//使用 Binder 机制将消息传递给 BroadcastReceiver
		performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver,
                        new Intent(r.intent), r.resultCode, r.resultData,
                        r.resultExtras, r.ordered, r.initialSticky, r.userId);
		...
	}

    void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
            Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
		...
		//通过 Binder 将消息处理传到应用进程，应用进程内部再使用 Handler 机制，将消息处理放到主线程中
		app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,data, extras, ordered, sticky, sendingUser, app.repProcState);
		...
	}
```

#### 串行处理
　　例如有序广播和静态广播等，会通过 enqueueOrderedBroadcastLocked 传给 BroadcastQueue:
```
    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        mOrderedBroadcasts.add(r);
        r.enqueueClockTime = System.currentTimeMillis();
    }
```
　　然后在 processNextBroadcast 里面会对 mOrderedBroadcasts 进行特殊处理。
```
    final void processNextBroadcast(boolean fromMsg) {
		synchronized(mService) {
            BroadcastRecord r;
			//开始处理有序广播
			// Now take care of the next serialized one...

            // If we are waiting for a process to come up to handle the next
            // broadcast, then do nothing at this point.  Just in case, we
            // check that the process we're waiting for still exists.
            if (mPendingBroadcast != null) {
                if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG_BROADCAST,
                        "processNextBroadcast [" + mQueueName + "]: waiting for "
                        + mPendingBroadcast.curApp);

                boolean isDead;
                synchronized (mService.mPidsSelfLocked) {
                    ProcessRecord proc = mService.mPidsSelfLocked.get(mPendingBroadcast.curApp.pid);
                    isDead = proc == null || proc.crashing;
                }
                if (!isDead) {
                    // It's still alive, so keep waiting
                    return;
                } else {
                    Slog.w(TAG, "pending app  ["
                            + mQueueName + "]" + mPendingBroadcast.curApp
                            + " died before responding to broadcast");
                    mPendingBroadcast.state = BroadcastRecord.IDLE;
                    mPendingBroadcast.nextReceiver = mPendingBroadcastRecvIndex;
                    mPendingBroadcast = null;
                }
            }

            boolean looped = false;
			//循环处理有序广播
			do {
                if (mOrderedBroadcasts.size() == 0) {
                    // No more broadcasts pending, so all done!
                    mService.scheduleAppGcsLocked();
                    if (looped) {
                        // If we had finished the last ordered broadcast, then
                        // make sure all processes have correct oom and sched
                        // adjustments.
                        mService.updateOomAdjLocked();
                    }
                    return;
                }
                r = mOrderedBroadcasts.get(0);
                boolean forceReceive = false;

                // Ensure that even if something goes awry with the timeout
                // detection, we catch "hung" broadcasts here, discard them,
                // and continue to make progress.
                //
                // This is only done if the system is ready so that PRE_BOOT_COMPLETED
                // receivers don't get executed with timeouts. They're intended for
                // one time heavy lifting after system upgrades and can take
                // significant amounts of time.
                int numReceivers = (r.receivers != null) ? r.receivers.size() : 0;
                if (mService.mProcessesReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if ((numReceivers > 0) &&
                            (now > r.dispatchTime + (2*mTimeoutPeriod*numReceivers))) {
                        Slog.w(TAG, "Hung broadcast ["
                                + mQueueName + "] discarded after timeout failure:"
                                + " now=" + now
                                + " dispatchTime=" + r.dispatchTime
                                + " startTime=" + r.receiverTime
                                + " intent=" + r.intent
                                + " numReceivers=" + numReceivers
                                + " nextReceiver=" + r.nextReceiver
                                + " state=" + r.state);
                        broadcastTimeoutLocked(false); // forcibly finish this broadcast
                        forceReceive = true;
                        r.state = BroadcastRecord.IDLE;
                    }
                }

                if (r.state != BroadcastRecord.IDLE) {
                    if (DEBUG_BROADCAST) Slog.d(TAG_BROADCAST,
                            "processNextBroadcast("
                            + mQueueName + ") called when not idle (state="
                            + r.state + ")");
                    return;
                }

                if (r.receivers == null || r.nextReceiver >= numReceivers
                        || r.resultAbort || forceReceive) {
                    // No more receivers for this broadcast!  Send the final
                    // result if requested...
					//还有下一个接收广播的 Receiver
                    if (r.resultTo != null) {
                        try {
                            if (DEBUG_BROADCAST) Slog.i(TAG_BROADCAST,
                                    "Finishing broadcast [" + mQueueName + "] "
                                    + r.intent.getAction() + " app=" + r.callerApp);
                            // 看这里，将intent 的结果传递给下一个接受者
                            performReceiveLocked(r.callerApp, r.resultTo,
                                new Intent(r.intent), r.resultCode,
                                r.resultData, r.resultExtras, false, false, r.userId);
                            // Set this to null so that the reference
                            // (local and remote) isn't kept in the mBroadcastHistory.
                            r.resultTo = null;
                        } catch (RemoteException e) {
                            r.resultTo = null;
                            Slog.w(TAG, "Failure ["
                                    + mQueueName + "] sending broadcast result of "
                                    + r.intent, e);

                        }
                    }

                    if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Cancelling BROADCAST_TIMEOUT_MSG");
                    cancelBroadcastTimeoutLocked();

                    if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG_BROADCAST,
                            "Finished with ordered broadcast " + r);

                    // ... and on to the next...
                    addBroadcastToHistoryLocked(r);
                    if (r.intent.getComponent() == null && r.intent.getPackage() == null
                            && (r.intent.getFlags()&Intent.FLAG_RECEIVER_REGISTERED_ONLY) == 0) {
                        // This was an implicit broadcast... let's record it for posterity.
                        mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage,
                                r.manifestCount, r.manifestSkipCount, r.finishTime-r.dispatchTime);
                    }
                    mOrderedBroadcasts.remove(0);
                    r = null;
                    looped = true;
                    continue;
                }
            } while (r == null);
		...
		}
	...
	}
```
　　在 processNextBroadcast(boolean fromMsg) 方法中，通过 do-while 循环处理有序广播，如果 BroadcastRecord 有下一个需要传递的接收者，则调用 performReceiveLocked() 方法：
```
    void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
            Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        ...
            receiver.performReceive(intent, resultCode, data, extras, ordered,
                    sticky, sendingUser);
        }
    }
```
　　在 processNextBroadcast(boolean fromMsg) 方法中调用 IIntentReceiver 的 performReceive 方法（也就是LoadApk 的内部类 InnerReceiver 的方法）：
```
            @Override
            public void performReceive(Intent intent, int resultCode, String data,
                    Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                final LoadedApk.ReceiverDispatcher rd;
                if (intent == null) {
                    rd = null;
                } else {
                    rd = mDispatcher.get();
                }
                if (rd != null) {
					//结束接收
                    rd.performReceive(intent, resultCode, data, extras,
                            ordered, sticky, sendingUser);
                } else {
                    // The activity manager dispatched a broadcast to a registered
                    // receiver in this process, but before it could be delivered the
                    // receiver was unregistered.  Acknowledge the broadcast on its
                    // behalf so that the system's broadcast sequence can continue.
                    IActivityManager mgr = ActivityManagerNative.getDefault();
                    try {
                        if (extras != null) {
                            extras.setAllowFds(false);
                        }
						//结束接收
                        mgr.finishReceiver(this, resultCode, data, extras, false, intent.getFlags());
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }

```
　　在 performReceive() 方法中调用 ReceiverDispatcher 的 performReceive 方法或者 ActivityManangerService 的 finishReceiver() 方法，ReceiverDispatcher 的 performReceive 方法也会调用到 ActivityManangerService 的 finishReceiver() 方法：
```
    public void finishReceiver(IBinder who, int resultCode, String resultData,
            Bundle resultExtras, boolean resultAbort, int flags) {
        if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Finish receiver: " + who);

        // Refuse possible leaked file descriptors
        if (resultExtras != null && resultExtras.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }

        final long origId = Binder.clearCallingIdentity();
        try {
            boolean doNext = false;
            BroadcastRecord r;

            synchronized(this) {
                BroadcastQueue queue = (flags & Intent.FLAG_RECEIVER_FOREGROUND) != 0
                        ? mFgBroadcastQueue : mBgBroadcastQueue;
                r = queue.getMatchingOrderedReceiver(who);
                if (r != null) {
					//调用finishReceiverLocked方法
                    doNext = r.queue.finishReceiverLocked(r, resultCode,
                        resultData, resultExtras, resultAbort, true);
                }
            }
			//继续下一个，循环调用了 BroadcastQueue 的 processNextBroadcast() 方法
            if (doNext) {
                r.queue.processNextBroadcast(false);
            }
            trimApplications();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }
```
　　在 ActivityManangerService 的 finishReceiver() 方法中调用了 BroadcastQueue 的 finishReceiverLocked() 方法：
```
    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode,
            String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        final int state = r.state;
        final ActivityInfo receiver = r.curReceiver;
        r.state = BroadcastRecord.IDLE;
        r.receiver = null;
        r.intent.setComponent(null);
        if (r.curApp != null && r.curApp.curReceiver == r) {
            r.curApp.curReceiver = null;
        }
        if (r.curFilter != null) {
            r.curFilter.receiverList.curBroadcast = null;
        }
        r.curFilter = null;
        r.curReceiver = null;
        r.curApp = null;
        mPendingBroadcast = null;

        r.resultCode = resultCode;
        r.resultData = resultData;
        r.resultExtras = resultExtras;
        if (resultAbort && (r.intent.getFlags()&Intent.FLAG_RECEIVER_NO_ABORT) == 0) {
            r.resultAbort = resultAbort;
        } else {
            r.resultAbort = false;
        }

        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices
                && r.queue.mOrderedBroadcasts.size() > 0
                && r.queue.mOrderedBroadcasts.get(0) == r) {
            ActivityInfo nextReceiver;
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = (obj instanceof ActivityInfo) ? (ActivityInfo)obj : null;
            } else {
                nextReceiver = null;
            }
            // Do not do this if the next receive is in the same process as the current one.
            if (receiver == null || nextReceiver == null
                    || receiver.applicationInfo.uid != nextReceiver.applicationInfo.uid
                    || !receiver.processName.equals(nextReceiver.processName)) {
                // In this case, we are ready to process the next receiver for the current broadcast,
                // but are on a queue that would like to wait for services to finish before moving
                // on.  If there are background services currently starting, then we will go into a
                // special state where we hold off on continuing this broadcast until they are done.
                if (mService.mServices.hasBackgroundServices(r.userId)) {
                    Slog.i(TAG, "Delay finish: " + r.curComponent.flattenToShortString());
                    r.state = BroadcastRecord.WAITING_SERVICES;
                    return false;
                }
            }
        }

        r.curComponent = null;

        // We will process the next receiver right now if this is finishing
        // an app receiver (which is always asynchronous) or after we have
        // come back from calling a receiver.
		//如果已经完成了一个一个应用的接收（始终是异步的）则即可进行一下个接受者的处理，或者返回到调用接受者的地方。
        return state == BroadcastRecord.APP_RECEIVE
                || state == BroadcastRecord.CALL_DONE_RECEIVE;
    }
```


#### 总结
　　广播队列传送广播给 Receiver 的原理其实就是将 BroadcastReceiver 和消息都放到 BroadcastRecord 里面，然后通过 Handler 机制遍历 BroadcastQueue 里面的 BroadcastRecord ，将消息发送给 BroadcastReceiver：
![](image/广播队列传送广播原理.png)

　　整个广播的机制总结成下图：
![](image/广播机制图.png)


## 参考文章
[Android 广播Broadcast的两种注册方式静态和动态](https://blog.csdn.net/csdn_aiyang/article/details/68947014)
[Android四大组件：BroadcastReceiver史上最全面解析](https://blog.csdn.net/carson_ho/article/details/52973504)
[安卓广播的底层实现原理](https://www.jianshu.com/p/02085150339c)