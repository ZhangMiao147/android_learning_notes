# APK安装流程详解14——PMS中的新安装流程上(拷贝)补偿

本片文章的主要内容如下：

```
1、在PackageManagerService的installPackageAsUser方法里面的代码
2、DefaultContainerService详解
3、mContainerService.getMinimalPackageInfo(String.int,String)方法与calculateInstalledSize(String,boolean,String)方法的讲解
4、为什么说mContext.bindServiceAsUser等于mContext.bindService
5、HandlerParams与InstallParams简介
6、InstallArgs家族成员
7、为什么新安装的情况下 origin.staged等于false
8、LocalSocket的跨进程通信
9、createInstallArgs(InstallParams)方法解答
10、sVerificationEnabled(int userId, int installFlags) 的理解
11、Context.sendBroadcast(Intent intent)的功能是和Context.sendBroadcastAsUser(Intent,UserHandle)一样的解答
12、Split APK(APK拆分)与Instant Run简介
```

## 一、在PackageManagerService的installPackageAsUser方法里面的代码mContext.enforceCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES, null)

这个方法调用在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9524行

### (一) mContext是什么？

mContext.enforceCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES, null)

要看这个方法内部执行，首先要知道这个mContext是什么，我们知道这个mContext是通过PackageManagerService的main方法传入的，所以这个mContext就是SystemServer里面的mSystemContext。

代码在[SystemServer.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/java/com/android/server/SystemServer.java) 366行如下：

```java
        mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
```

找到了mSystemContext的初始化的地方在createSystemContext()里面
代码在[SystemServer.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/java/com/android/server/SystemServer.java) 311行如下：

```cpp
    private void createSystemContext() {
        ActivityThread activityThread = ActivityThread.systemMain();
        mSystemContext = activityThread.getSystemContext();
        mSystemContext.setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
    }
```

那就来追踪下ActivityThread 的getSystemContext()方法

```kotlin
    public ContextImpl getSystemContext() {
        synchronized (this) {
            if (mSystemContext == null) {
                mSystemContext = ContextImpl.createSystemContext(this);
            }
            return mSystemContext;
        }
    }
```

进而追踪到ContextImpl里面代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1774行。

```csharp
    static ContextImpl createSystemContext(ActivityThread mainThread) {
        LoadedApk packageInfo = new LoadedApk(mainThread);
        ContextImpl context = new ContextImpl(null, mainThread,
                packageInfo, null, null, false, null, null, Display.INVALID_DISPLAY);
        context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(),
                context.mResourcesManager.getDisplayMetricsLocked());
        return context;
    }
```

所以我们知道这个SystemContext其实就是ContextImpl，直接找ContextImpl对应的enforceCallingOrSelfPermission方法。

代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1468行

```tsx
    @Override
    public void enforceCallingOrSelfPermission(
            String permission, String message) {
        enforce(permission,
                checkCallingOrSelfPermission(permission),
                true,
                Binder.getCallingUid(),
                message);
    }
```

这个方法首先调用了checkCallingOrSelfPermission方法，然后再调用enforce方法。

### (二)ContextImpl#checkCallingOrSelfPermission(String) 方法 简介

代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1416行

```java
    @Override
    public int checkCallingOrSelfPermission(String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }

        return checkPermission(permission, Binder.getCallingPid(),
                Binder.getCallingUid());
    }
```

这个方法首先做了入参permission的非空判断，然后调用了checkPermission(String,int,int )方法。

而在ContextImpl里面checkPermission(String,int,int )方法如下，代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1374行:

```java
    @Override
    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }

        try {
            return ActivityManagerNative.getDefault().checkPermission(
                    permission, pid, uid);
        } catch (RemoteException e) {
            return PackageManager.PERMISSION_DENIED;
        }
    }
```

看到的是最后调用的是 ActivityManagerNative.getDefault().checkPermission(permission, pid, uid);这里先提前说下这个方法其实是调用的ActivityServcieManager的checkPermission(String,int ,int)方法。

在ActivityManagerService.java里面checkPermission(String,int,int )方法如下，代码在[ActivityManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java) 7108行:

```dart
    /**
     * As the only public entry point for permissions checking, this method
     * can enforce the semantic that requesting a check on a null global
     * permission is automatically denied.  (Internally a null permission
     * string is used when calling {@link #checkComponentPermission} in cases
     * when only uid-based security is needed.)
     *
     * This can be called with or without the global lock held.
     */
    @Override
    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return PackageManager.PERMISSION_DENIED;
        }
        return checkComponentPermission(permission, pid, uid, -1, true);
    }
```

先简单翻译一下注释的内容：

权限检查的唯一公共入口。这个方法可以强制执行对全局权限请求的检查和如果是空的权限可以自动拒绝。如果是在uid安全的情况，如果想使用空的字符串来检查权限可以调用checkComponentPermission这个方法。
 这个方法可以在有或者没有全局锁定的情况下使用。

通过上面注释，知道了这是一个全局的检查权限的入口了。看方法内部最后调用了checkComponentPermission方法了，那就继续跟踪

int checkComponentPermission(String , int , int , int , boolean )方法如下，代码在[ActivityManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java) 7089行:

```cpp
    /**
     * This can be called with or without the global lock held.
     */
    int checkComponentPermission(String permission, int pid, int uid,
            int owningUid, boolean exported) {
        if (pid == MY_PID) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return ActivityManager.checkComponentPermission(permission, uid,
                owningUid, exported);
    }
```

通过注释知道，这个方法可以在有全局锁定或者没有全局锁定的时候调用。这个方法内部只做了一个MY_PID的判断，如果pid=MY_PID，而MY_PID其实就是当前进程的pid，则直接返回PackageManager.PERMISSION_GRANTED，而PackageManager.PERMISSION_GRANTED表示的意思是"授予"，即检查通过。那我们来看下ActivityManager.checkComponentPermission(permission, uid, owningUid, exported);这个方法的具体执行

代码在[ActivityManager.java)](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ActivityManager.java) 2617行

```java
    /** @hide */
    public static int checkComponentPermission(String permission, int uid,
            int owningUid, boolean exported) {
        // Root, system server get to do everything.
        // 首先判断是不是root和system，如果是直接返回"授予"，因为它们拥有最大权限
        final int appId = UserHandle.getAppId(uid);
        if (appId == Process.ROOT_UID || appId == Process.SYSTEM_UID) {
            return PackageManager.PERMISSION_GRANTED;
        }

        // Isolated processes don't get any permissions. 一般用不到，需要在AndroidManifest里面设置android:isolatedProcess=true
        // 判断是否是隔离进程 如果是隔离进程则直接拒绝
        if (UserHandle.isIsolated(uid)) {
            return PackageManager.PERMISSION_DENIED;
        }
        // If there is a uid that owns whatever is being accessed, it has
        // blanket access to it regardless of the permissions it requires.
       // 如果是同一个应用，则不需要监测
        if (owningUid >= 0 && UserHandle.isSameApp(uid, owningUid)) {
            return PackageManager.PERMISSION_GRANTED;
        }
        // If the target is not exported, then nobody else can get to it.
        //  如果设置了exported=false，(比如在AndroidManifest里面设置了exported=false) 则表明这个APP没有授权，所以拒绝
        if (!exported) {
            /*
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.w(TAG, "Permission denied: checkComponentPermission() owningUid=" + owningUid,
                    here);
            */
            return PackageManager.PERMISSION_DENIED;
        }
       // 如果permission==null 则通过
        if (permission == null) {
            return PackageManager.PERMISSION_GRANTED;
        }
        // 否则调用AppGlobals.getPackageManager().checkUidPermission(permission,uid)
        try {
            return AppGlobals.getPackageManager()
                    .checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            // Should never happen, but if it does... deny!
            Slog.e(TAG, "PackageManager is dead?!?", e);
        }
        return PackageManager.PERMISSION_DENIED;
    }
```

在Android的系统中，每一个APP都会分配一个uid，但是一个APP内部可能会有多进程，所以APP的内部就可能存在不同的pid，但是其APP内部的进程共享一个uid。

方法内部注释已经解释的很清楚了，这里说下最后的AppGlobals.getPackageManager().checkUidPermission(permission,uid)方法，如果成功调用则直接返回，如果抛异常了，则返回拒绝(PackageManager.PERMISSION_DENIED)，那来看下AppGlobals.getPackageManager().checkUidPermission(permission, uid);里面的具体实现

这里首先要看下AppGlobals.getPackageManager()的值是什么？
代码在[AppGlobals.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/AppGlobals.java) 46行。

```java
    /**
     * Return the raw interface to the package manager.
     * @return The package manager.
     */
    public static IPackageManager getPackageManager() {
        return ActivityThread.getPackageManager();
    }
```

前面的文章[APK安装流程详解3——PackageManager与PackageManagerService]知道最后到了PackageManagerService里面。所以AppGlobals.getPackageManager()=PackageManagerService对象。所以AppGlobals.getPackageManager().checkUidPermission(permission, uid);这个方法其实可以理解为PackageManagerService#checkUidPermission(permission, uid)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 3190行

```kotlin
    @Override
    public int checkUidPermission(String permName, int uid) {
        final int userId = UserHandle.getUserId(uid);
        // 判断这个userId 对应的App是否存在
        if (!sUserManager.exists(userId)) {
            return PackageManager.PERMISSION_DENIED;
        }

        synchronized (mPackages) {
            // 获取这个uid对应的SettingBase
            Object obj = mSettings.getUserIdLPr(UserHandle.getAppId(uid));
             // PackageManagerService.Setting.mUserIds数组中，根据uid查找uid也就是package的权限列表
            if (obj != null) {
                final SettingBase ps = (SettingBase) obj;
                // 获取对应的permissionsState 
                final PermissionsState permissionsState = ps.getPermissionsState();
                 // 如果permissionsState  里面包含这个permName，则通过
                if (permissionsState.hasPermission(permName, userId)) {
                    return PackageManager.PERMISSION_GRANTED;
                }
                // Special case: ACCESS_FINE_LOCATION permission includes ACCESS_COARSE_LOCATION
                 // ACCESS_COARSE_LOCATION的特殊情况，也是通过
                if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permName) && permissionsState
                        .hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, userId)) {
                    return PackageManager.PERMISSION_GRANTED;
                }
               // 如果上面都没满足，则拒绝权限
            } else {
                 // 系统级应用uid 对应的permission
                ArraySet<String> perms = mSystemPermissions.get(uid);
                if (perms != null) {
                    if (perms.contains(permName)) {
                        return PackageManager.PERMISSION_GRANTED;
                    }
                    if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permName) && perms
                            .contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        return PackageManager.PERMISSION_GRANTED;
                    }
                }
            }
        }
        return PackageManager.PERMISSION_DENIED;
    }
```

这里是Android 6.0的源码，所以这个的代码和Android  5.0是不同的，所以有心的同学可以去对比下，Android6.0里面多了一个PermissionsState，Android  6.0以后是对权限的操作是PermissionsState。有兴趣可以研究下PermissionsState，和它的hasPermission(String name, int userId)方法，这里面包含了除了声明的权限，还必须是授权的权限。

上面代码注释已经写的很清楚了，自此)ContextImpl#checkCallingOrSelfPermission(String)整个方法流程就已经跟踪完毕。

### (三)、ContextImpl#enforce(String,int,boolean,int,String)方法简介

代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1434行

```java
    private void enforce(
            String permission, int resultOfCheck,
            boolean selfToo, int uid, String message) {
        if (resultOfCheck != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    (message != null ? (message + ": ") : "") +
                    (selfToo
                     ? "Neither user " + uid + " nor current process has "
                     : "uid " + uid + " does not have ") +
                    permission +
                    ".");
        }
    }
```

这里面很简单，主要判断是上面checkCallingOrSelfPermission方法的返回值，如果不是PackageManager.PERMISSION_GRANTED则直接抛异常，如果是，则什么也不做。

### (四)、总结

通过上述方法的解析，知道ContextImple#enforceCallingOrSelfPermission经过一些列的调用，最后还是判断这个APP的权限。

## 二、DefaultContainerService详解

[DefaultContainerService.java源码地址](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java)

### (一)、DefaultContainerService类简介

```dart
/**
 * Service that offers to inspect and copy files that may reside on removable
 * storage. This is designed to prevent the system process from holding onto
 * open files that cause the kernel to kill it when the underlying device is
 * removed.
 */
public class DefaultContainerService extends IntentService {
          ...
}
```

首先知道 DefaultContainerService 继承自 IntentService，然后为了更好的理解设计者的意图，还是看下面的注释

提供检查和复制文件的Service，这个Service既可以提供保存在存储空间的服务也可以是提供删除服务。这样设计的的目的是防止：在系统进程打开文件的时候，同时如果底层设备删除了文件而内核将其杀死的情况的发生。

所以总结一下DefaultContainerService是一个应用服务，具体负责实现APK等相关资源文件在内部或者外部存储器上的存储工作。

### (二)、DefaultContainerService类结构

![](https://upload-images.jianshu.io/upload_images/5713484-b47007efd833dfd9.png)

通过上面类结构的图，发现这个类的方法大多数是私有的或者"包"内的方法，所以只要找到源头，基本上能捋顺这个类。而想要捋顺这个类很简单，因为它继承IntentService，所以一般的Android开发工程师都是知道只要找他到的onHandlerIntent方法即可。那下面就来研究下这个方法。

### (三)、onHandlerIntent(Intent)方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 271行

```java
    @Override
    protected void onHandleIntent(Intent intent) {
        if (PackageManager.ACTION_CLEAN_EXTERNAL_STORAGE.equals(intent.getAction())) {
           // 第一步
            final IPackageManager pm = IPackageManager.Stub.asInterface(
                    ServiceManager.getService("package"));
            PackageCleanItem item = null;
            try {
               // 第二步
                while ((item = pm.nextPackageToClean(item)) != null) {
                    final UserEnvironment userEnv = new UserEnvironment(item.userId);
                    eraseFiles(userEnv.buildExternalStorageAppDataDirs(item.packageName));
                    eraseFiles(userEnv.buildExternalStorageAppMediaDirs(item.packageName));
                    if (item.andCode) {
                        eraseFiles(userEnv.buildExternalStorageAppObbDirs(item.packageName));
                    }
                }
            } catch (RemoteException e) {
            }
        }
    }
```

把这个方法里面的内容分为三大部分

* 第一步：获取IPackageManager对象，其实也就是PackageManagerService的代理对象pm

* 第二步：遍历pm即PackageManagerService中的已经卸载了，但是仍然占用存储空间的对象PackageCleanItem

* 第三步：调用eraseFiles()方法来清除文件

这里面涉及三个内容

- 1、pm. nextPackageToClean(PackageCleanItem)方法
- 2、UserEnvironment类及其方法
- 3、本地的eraseFiles方法

那就依次来看下

#### 1、pm. nextPackageToClean(PackageCleanItem)方法

pm其实是PackageManagerService的代理类，所以直接找PackageManagerService的nextPackageToClean(PackageCleanItem)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9457行

```kotlin
    @Override
    public PackageCleanItem nextPackageToClean(PackageCleanItem lastPackage) {
        // writer
        synchronized (mPackages) {
            // 第一步
            if (!isExternalMediaAvailable()) {
                // If the external storage is no longer mounted at this point,
                // the caller may not have been able to delete all of this
                // packages files and can not delete any more.  Bail.
                return null;
            }
           
            // 第二步
            final ArrayList<PackageCleanItem> pkgs = mSettings.mPackagesToBeCleaned;
            // 第三步
            if (lastPackage != null) {
                pkgs.remove(lastPackage);
            }
            // 第四步
            if (pkgs.size() > 0) {
                return pkgs.get(0);
            }
        }
        return null;
    }
```

这个方法内部主要分为4步：

* 第一步：判断外部设备是否可用

* 第二步：获取已经删除了，但仍然占用存储空间的列表

* 第三步：这一步其实是递归的一个思路，如果是第一次调用nextPackageToClean，则lastPackage为null。如果不是第一次调用，则lastPackage为pkgs中目前元素的上一个元素。

* 第四步：获取pkgs中的第0位的元素，注意这里是get方法，这里获取的元素，会在第三步中删除的。

这里涉及到了mSettings.mPackagesToBeCleaned的概念，那来看下这个变量是什么？

```dart
    // Packages that have been uninstalled and still need their external
    // storage data deleted.
    final ArrayList<PackageCleanItem> mPackagesToBeCleaned = new ArrayList<PackageCleanItem>();
```

通过注释知道，这个mPackagesToBeCleaned变量表示的是：已经卸载了，但是仍占用外部存储空间的软件包。

至此pm. nextPackageToClean(PackageCleanItem)方法已经跟踪完毕。

#### 2、UserEnvironment类及其方法

UserEnvironment是[Environment.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/os/Environment.java)的静态内部类。

UserEnvironment 我的理解就是某个应用的存储空间访问类
我们常用的几个方法是：

* buildExternalStorageAppCacheDirs(packageName)：对应sdcard/android/0/包名/cache 目录

* buildExternalStorageAppDataDirs(packageName)：对应sdcard/android/0/包名/data 目录

* buildExternalStorageAppMediaDirs(packageName)：对应sdcard/android/0/包名/media 目录

* buildExternalStorageAppObbDirs(packageName)：对应sdcard/android/0/包名/obb 目录

#### 3、eraseFiles(File[])方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 290行

```cpp
    void eraseFiles(File[] paths) {
        for (File path : paths) {
            eraseFiles(path);
        }
    }
```

看到这个方法最后调用的重载的eraseFiles(String)方法
代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 296行

```cpp
    void eraseFiles(File path) {
        if (path.isDirectory()) {
            String[] files = path.list();
            if (files != null) {
                for (String file : files) {
                    eraseFiles(new File(path, file));
                }
            }
        }
        path.delete();
    }
```

我们发现它使用递归的方法，依次删除文件。

### (四)、DefaultContainerService的重要变量mBinder

在DefaultContainerService里面有一个重要变量mBinder。
代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 72行

```dart
  private IMediaContainerService.Stub mBinder = new IMediaContainerService.Stub() {
        /**
         * Creates a new container and copies package there.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         * @param containerId the id of the secure container that should be used
         *            for creating a secure container into which the resource
         *            will be copied.
         * @param key Refers to key used for encrypting the secure container
         * @return Returns the new cache path where the resource has been copied
         *         into
         */
        @Override
        public String copyPackageToContainer(String packagePath, String containerId, String key,
                boolean isExternal, boolean isForwardLocked, String abiOverride) {
            if (packagePath == null || containerId == null) {
                return null;
            }

            if (isExternal) {
                // Make sure the sdcard is mounted.
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED)) {
                    Slog.w(TAG, "Make sure sdcard is mounted.");
                    return null;
                }
            }

            PackageLite pkg = null;
            NativeLibraryHelper.Handle handle = null;
            try {
                final File packageFile = new File(packagePath);
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                handle = NativeLibraryHelper.Handle.create(pkg);
                return copyPackageToContainerInner(pkg, handle, containerId, key, isExternal,
                        isForwardLocked, abiOverride);
            } catch (PackageParserException | IOException e) {
                Slog.w(TAG, "Failed to copy package at " + packagePath, e);
                return null;
            } finally {
                IoUtils.closeQuietly(handle);
            }
        }

        /**
         * Copy package to the target location.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         * @return returns status code according to those in
         *         {@link PackageManager}
         */
        @Override
        public int copyPackage(String packagePath, IParcelFileDescriptorFactory target) {
            if (packagePath == null || target == null) {
                return PackageManager.INSTALL_FAILED_INVALID_URI;
            }

            PackageLite pkg = null;
            try {
                final File packageFile = new File(packagePath);
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                return copyPackageInner(pkg, target);
            } catch (PackageParserException | IOException | RemoteException e) {
                Slog.w(TAG, "Failed to copy package at " + packagePath + ": " + e);
                return PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE;
            }
        }

        /**
         * Parse given package and return minimal details.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         */
        @Override
        public PackageInfoLite getMinimalPackageInfo(String packagePath, int flags,
                String abiOverride) {
            final Context context = DefaultContainerService.this;
            final boolean isForwardLocked = (flags & PackageManager.INSTALL_FORWARD_LOCK) != 0;

            PackageInfoLite ret = new PackageInfoLite();
            if (packagePath == null) {
                Slog.i(TAG, "Invalid package file " + packagePath);
                ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                return ret;
            }

            final File packageFile = new File(packagePath);
            final PackageParser.PackageLite pkg;
            final long sizeBytes;
            try {
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                sizeBytes = PackageHelper.calculateInstalledSize(pkg, isForwardLocked, abiOverride);
            } catch (PackageParserException | IOException e) {
                Slog.w(TAG, "Failed to parse package at " + packagePath + ": " + e);

                if (!packageFile.exists()) {
                    ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_URI;
                } else {
                    ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                }

                return ret;
            }

            ret.packageName = pkg.packageName;
            ret.splitNames = pkg.splitNames;
            ret.versionCode = pkg.versionCode;
            ret.baseRevisionCode = pkg.baseRevisionCode;
            ret.splitRevisionCodes = pkg.splitRevisionCodes;
            ret.installLocation = pkg.installLocation;
            ret.verifiers = pkg.verifiers;
            ret.recommendedInstallLocation = PackageHelper.resolveInstallLocation(context,
                    pkg.packageName, pkg.installLocation, sizeBytes, flags);
            ret.multiArch = pkg.multiArch;

            return ret;
        }

        @Override
        public ObbInfo getObbInfo(String filename) {
            try {
                return ObbScanner.getObbInfo(filename);
            } catch (IOException e) {
                Slog.d(TAG, "Couldn't get OBB info for " + filename);
                return null;
            }
        }

        @Override
        public long calculateDirectorySize(String path) throws RemoteException {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final File dir = Environment.maybeTranslateEmulatedPathToInternal(new File(path));
            if (dir.exists() && dir.isDirectory()) {
                final String targetPath = dir.getAbsolutePath();
                return MeasurementUtils.measureDirectory(targetPath);
            } else {
                return 0L;
            }
        }

        @Override
        public long[] getFileSystemStats(String path) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            try {
                final StructStatVfs stat = Os.statvfs(path);
                final long totalSize = stat.f_blocks * stat.f_bsize;
                final long availSize = stat.f_bavail * stat.f_bsize;
                return new long[] { totalSize, availSize };
            } catch (ErrnoException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void clearDirectory(String path) throws RemoteException {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            final File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                eraseFiles(directory);
            }
        }

        /**
         * Calculate estimated footprint of given package post-installation.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         */
        @Override
        public long calculateInstalledSize(String packagePath, boolean isForwardLocked,
                String abiOverride) throws RemoteException {
            final File packageFile = new File(packagePath);
            final PackageParser.PackageLite pkg;
            try {
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                return PackageHelper.calculateInstalledSize(pkg, isForwardLocked, abiOverride);
            } catch (PackageParserException | IOException e) {
                Slog.w(TAG, "Failed to calculate installed size: " + e);
                return Long.MAX_VALUE;
            }
        }
    };
```

通过上面代码我们知道，mBinder是IMediaContainerService.Stub类型，看到这个类型，大家一定很熟了，对的一看就是AIDL。而且是AIDL的"服务端"。

看到AIDL我们首先要找他的源码地址[IMediaContainerService.aidl地址](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/com/android/internal/app/IMediaContainerService.aidl)

PS：DefaultContainerService的onBind方法返回的就是mBinder

```java
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
```

## 三、mContainerService.getMinimalPackageInfo(String.int,String)方法与calculateInstalledSize(String,boolean,String)方法的讲解

### (一)、mContainerService是什么？

先说下这个方法调用的位置：
在PackageManagerService中的handleStartCopy()方法里面
在代码[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10597行

```java
                pkgLite = mContainerService.getMinimalPackageInfo(origin.resolvedPath, installFlags,
                        packageAbiOverride);
```

要想知道这个方法的具体流程，首要先要明确mContainerService是一个什么东西。
而mContainerService其实是IMediaContainerService类型的，如下

```csharp
    private IMediaContainerService mContainerService = null;
```

再来找下mContainerService初始化的位置：
 在doHandleMessage(Message)方法里面MCS_BOUND的时候初始化的，在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1173行

```csharp
                case MCS_BOUND: {
                    if (DEBUG_INSTALL) Slog.i(TAG, "mcs_bound");
                    if (msg.obj != null) {
                        mContainerService = (IMediaContainerService) msg.obj;
                    }
```

而这里面的msg.obj是在DefaultContainerConnection对象mDefContainerConn"绑定"连接DefaultContainerService的时候执行onServiceConnected的时候初始化的。如下：
 在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 928行

```java
    class DefaultContainerConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "onServiceConnected");
            IMediaContainerService imcs =
                IMediaContainerService.Stub.asInterface(service);
            mHandler.sendMessage(mHandler.obtainMessage(MCS_BOUND, imcs));
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "onServiceDisconnected");
        }
    }
```

通过上面的跟踪我们知道了mContainerService其实就是上面方法通过 IMediaContainerService.Stub.asInterface(service)来获取的，通过AIDL知识我们知道其实对应的是DefaultContainerService的内部变量mBinder。所以说

mContainerService对应着DefaultContainerService的成员变量mBinder。所以mContainerService.getMinimalPackageInfo(String.int,String)方法对应的是DefaultContainerService的成员变量mBinder的getMinimalPackageInfo(String.int,String)方法。

### (二)、mContainerService.getMinimalPackageInfo(String.int,String)方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 152行

```dart
        /**
         * Parse given package and return minimal details.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         */
        @Override
        public PackageInfoLite getMinimalPackageInfo(String packagePath, int flags,
                String abiOverride) {
            // 第一步
            final Context context = DefaultContainerService.this;
            final boolean isForwardLocked = (flags & PackageManager.INSTALL_FORWARD_LOCK) != 0;

            PackageInfoLite ret = new PackageInfoLite();
             // 第二步
            if (packagePath == null) {
                Slog.i(TAG, "Invalid package file " + packagePath);
                ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                return ret;
            }

            final File packageFile = new File(packagePath);
            final PackageParser.PackageLite pkg;
            final long sizeBytes;
            try {
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                sizeBytes = PackageHelper.calculateInstalledSize(pkg, isForwardLocked, abiOverride);
            } catch (PackageParserException | IOException e) {
                Slog.w(TAG, "Failed to parse package at " + packagePath + ": " + e);

                if (!packageFile.exists()) {
                    ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_URI;
                } else {
                    ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                }

                return ret;
            }

            // 第三步
            ret.packageName = pkg.packageName;
            ret.splitNames = pkg.splitNames;
            ret.versionCode = pkg.versionCode;
            ret.baseRevisionCode = pkg.baseRevisionCode;
            ret.splitRevisionCodes = pkg.splitRevisionCodes;
            ret.installLocation = pkg.installLocation;
            ret.verifiers = pkg.verifiers;
            ret.recommendedInstallLocation = PackageHelper.resolveInstallLocation(context,
                    pkg.packageName, pkg.installLocation, sizeBytes, flags);
            ret.multiArch = pkg.multiArch;

            return ret;
        }
```

先来看下注释：

解析包并获取小的安装包内容

- 入参 packagePath：要复制包的绝对路径。这个目录可以包含单个APK也可以包含多个APK

将这个方法分为三个部分

- 第一步：初始化一些信息
- 第二步：解析packagePath对应的安装包，获取解析的出来的"轻"安装包pkg
- 第三步：把解析出来的"轻"安装包的属性赋值给PackageInfoLite对象ret并返回

### (三)、calculateInstalledSize(origin.resolvedPath, isForwardLocked(), packageAbiOverride);方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 251行

```dart
        /**
         * Calculate estimated footprint of given package post-installation.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         */
        @Override
        public long calculateInstalledSize(String packagePath, boolean isForwardLocked,
                String abiOverride) throws RemoteException {
            final File packageFile = new File(packagePath);
            final PackageParser.PackageLite pkg;
            try {
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                return PackageHelper.calculateInstalledSize(pkg, isForwardLocked, abiOverride);
            } catch (PackageParserException | IOException e) {
                Slog.w(TAG, "Failed to calculate installed size: " + e);
                return Long.MAX_VALUE;
            }
        }
```

先来看下注释：

计算安装包安装后可能的大小

- 入参 packagePath：这个目录可以包含单个APK也可以包含多个APK

## 四、为什么说mContext.bindServiceAsUser等于mContext.bindService

### (一)先说下这个mContext.bindServiceAsUser在哪里被调用

在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)1109
在connectToService()方法里面被调用

```java
        private boolean connectToService() {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "Trying to bind to" +
                    " DefaultContainerService");
            Intent service = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            if (mContext.bindServiceAsUser(service, mDefContainerConn,
                    Context.BIND_AUTO_CREATE, UserHandle.SYSTEM)) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                mBound = true;
                return true;
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return false;
        }
```

通过前文我们知道这里的mContext其实就是ContextImpl，所以我们看下这个bindServiceAsUser方法的具体实现。

代码在[ContextImpl.java)](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1291

```java
    @Override
    public boolean bindService(Intent service, ServiceConnection conn,
            int flags) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, mMainThread.getHandler(),
                Process.myUserHandle());
    }

    /** @hide */
    @Override
    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags,
            UserHandle user) {
        return bindServiceCommon(service, conn, flags, mMainThread.getHandler(), user);
    }
```

bindService和bindServiceAsUser的内部其实是用调用bindServiceCommon这个方法来实现的具体的逻辑的，所以说bindService方法和bindServiceAsUser其实内部的执行逻辑是一直的。

## 五、HandlerParams与InstallParams简介

在PackageManagerService进行安装的时候会涉及两个概念即HandlerParams与InstallParams，那我们就依次介绍下：

### (一)、HandlerParams类

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10233行

```java
    private abstract class HandlerParams {
        private static final int MAX_RETRIES = 4;

        /**
         * Number of times startCopy() has been attempted and had a non-fatal
         * error.
         */
        private int mRetries = 0;

        /** User handle for the user requesting the information or installation. */
        private final UserHandle mUser;

       HandlerParams(UserHandle user) {
            mUser = user;
        }

        UserHandle getUser() {
            return mUser;
        }

        final boolean startCopy() {
            boolean res;
            try {
                if (DEBUG_INSTALL) Slog.i(TAG, "startCopy " + mUser + ": " + this);

                if (++mRetries > MAX_RETRIES) {
                    Slog.w(TAG, "Failed to invoke remote methods on default container service. Giving up");
                    mHandler.sendEmptyMessage(MCS_GIVE_UP);
                    handleServiceError();
                    return false;
                } else {
                    handleStartCopy();
                    res = true;
                }
            } catch (RemoteException e) {
                if (DEBUG_INSTALL) Slog.i(TAG, "Posting install MCS_RECONNECT");
                mHandler.sendEmptyMessage(MCS_RECONNECT);
                res = false;
            }
            handleReturnCode();
            return res;
        }

        final void serviceError() {
            if (DEBUG_INSTALL) Slog.i(TAG, "serviceError");
            handleServiceError();
            handleReturnCode();
        }

        abstract void handleStartCopy() throws RemoteException;
        abstract void handleServiceError();
        abstract void handleReturnCode();
    }
```

我们看到这类，就一个构造函数。而且在构造这个类的时候，需要传入一个UserHandle
它里面有三个抽象方法

* abstract void handleStartCopy() throws RemoteException;

* abstract void handleServiceError();

* abstract void handleReturnCode();

有两个核心非抽象方法，注意这两个方法都是final的

- final startCopy()：
- final serviceError()：

startCopy()已经在文章[APK安装流程详解10——PMS中的新安装流程]中**HandlerParams的startCopy方法**讲解了，而serviceError()里面其实是调用了两个handleServiceError()和handleReturnCode()抽象方法

小结

其实这个HandlerParams类主要就做了2件事，一个是抽象出三行为，即三个抽象方法，然后定义了重试4次，如果超过4次则放弃重试的规则。

下面我们就来看下InstallParams类。

### (二)、InstallParams类与HandlerParams的关系

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10464行

```dart
    class InstallParams extends HandlerParams {
             ...
    }
```

首先知道InstallParams类继承自HandlerParams，且InstallParams不是抽象方法，所以InstallParams必然实现了HandlerParams所对应的三个方法。

所以InstallParams与HandlerParams关系如下：

![](https://upload-images.jianshu.io/upload_images/5713484-3d6b0b0fb2891d0d.png)

所以说HandlerParams有两个子类，分别是InstallParams和MeasureParams。

- InstallParams：用于处理APK的安装
- MeasureParams：用于查询某个已安装的APK占据的存储空间的大小，例如在设置程序中得到某个APK使用缓存文件的大小。

## 五、InstallArgs家族成员

InstallArgs是PackageManagerService的静态内部类
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10907行

```csharp
    static abstract class InstallArgs {
       ...
    }
```

通过上面代码知道InstallArgs是抽象类，看到InstallArgs是静态类，且不是"public"的，所以InstallArgs的所有子类肯定都在PackageManagerService中。

找到了三个子类如下：

* 1、 FileInstallArgs：APK安装在内部存储空间的时候使用的子类

* 2、 AsecInstallArgs：安装到sdcard或者ForwardLocked的时候使用的子类

* 3、MoveInstallArgs：移动包的位置，比如从内部存储移动到sdcard上的构造方法中根据InstallParams会构造出具体类型

![](https://upload-images.jianshu.io/upload_images/5713484-12aa484e70af8d2f.png)

设计这四个类的目的是什么意义？

这样设计的目的是：APK可以安装在内部存储空间或者SD卡上，已经安装的APK也可以在内部存储和SD之间进行移动，PackageManagerService为此设计了InstallArgs这个抽象类的数据结构，它代表这三种情况通用的属性，

这里用FileInstallArgs类举例，说一下FileInstallArgs与InstallParams的关系
代码如下：

```csharp
    /**
     * Logic to handle installation of non-ASEC applications, including copying
     * and renaming logic.
     */
    class FileInstallArgs extends InstallArgs {
        private File codeFile;
        private File resourceFile;

        // Example topology:
        // /data/app/com.example/base.apk
        // /data/app/com.example/split_foo.apk
        // /data/app/com.example/lib/arm/libfoo.so
        // /data/app/com.example/lib/arm64/libfoo.so
        // /data/app/com.example/dalvik/arm/base.apk@classes.dex

        /** New install */
        FileInstallArgs(InstallParams params) {
            super(params.origin, params.move, params.observer, params.installFlags,
                    params.installerPackageName, params.volumeUuid, params.getManifestDigest(),
                    params.getUser(), null /* instruction sets */, params.packageAbiOverride,
                    params.grantedRuntimePermissions);
            if (isFwdLocked()) {
                throw new IllegalArgumentException("Forward locking only supported in ASEC");
            }
        }

        /** Existing install */
        FileInstallArgs(String codePath, String resourcePath, String[] instructionSets) {
            super(OriginInfo.fromNothing(), null, null, 0, null, null, null, null, instructionSets,
                    null, null);
            this.codeFile = (codePath != null) ? new File(codePath) : null;
            this.resourceFile = (resourcePath != null) ? new File(resourcePath) : null;
        }
    ...
    }
```

注意：它的两个构造函数通过注释知道，带有InstallParams参数的构造函数是新安装，而三个参数的构造函数则是更新操作的构造函数。

所以他们的关系如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-98d0b54d02fa347b.png)

同理：AsecInstallArgs类和FileInstallArgs一样 也有两个构造函数，一个是一个InstallParams参数的，用于新安装，其中还有一个多参数的构造函数，用于更新安装。

## 七、为什么新安装的情况下 origin.staged等于false

先找到这个问题的位置，这个问题是在handleStartCopy()方法里面涉及到下面代码：

```csharp
            if (origin.staged) {
                if (origin.file != null) {
                    installFlags |= PackageManager.INSTALL_INTERNAL;
                    installFlags &= ~PackageManager.INSTALL_EXTERNAL;
                } else if (origin.cid != null) {
                    installFlags |= PackageManager.INSTALL_EXTERNAL;
                    installFlags &= ~PackageManager.INSTALL_INTERNAL;
                } else {
                    throw new IllegalStateException("Invalid stage location");
                }
            }
```

里面的if判断为false。

如果想获取origin.staged，就必须要要知道origin是什么时候初始化的。origin是在发送what值为INIT_COPY的Message的时候初始化的
 代码在PackageManagerService里面的installPackageAsUser方法里面：
 代码在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9569行

```dart
      final OriginInfo origin = OriginInfo.fromUntrustedFile(originFile);
```

由于OriginInfo是PackageManagerService的内部类，我们直接找到OriginInfo的fromUntrustedFile静态方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10408行

```csharp
        static OriginInfo fromUntrustedFile(File file) {
            return new OriginInfo(file, null, false, false);
        }
```

看到fromUntrustedFile方法直接new了一个OriginInfo对象，而OriginInfo就一个构造函数，来看下构造函数。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10424行

```java
        private OriginInfo(File file, String cid, boolean staged, boolean existing) {
            this.file = file;
            this.cid = cid;
            this.staged = staged;
            this.existing = existing;

            if (cid != null) {
                resolvedPath = PackageHelper.getSdDir(cid);
                resolvedFile = new File(resolvedPath);
            } else if (file != null) {
                resolvedPath = file.getAbsolutePath();
                resolvedFile = file;
            } else {
                resolvedPath = null;
                resolvedFile = null;
            }
        }
```

我们看到staged对应的第三个入参，而这个new OriginInfo(file, null, false, false)方法中第三个参数是false。所以我们说如果在新安装的情况下origin.staged等于false。

## 八、LocalSocket的跨进程通信

### (一)、Socket

Socket最初用于基于TCP/IP网络间进程通信中，以客户端/服务器模式进行通信。实现异步操作，共享资源集中处理，提高客户端响应能力。

socketAPI 原本是为网络通讯设计的，但后来在socket的框架上发展出一种IPC机制，就是UNIX Demain Socket。虽然网络socket也可用于同一台主机的进程间通信(通过loopback地址127.0.0.1)，但是UNIX Demain Socket 用于IPC更有效率：不需要经过网络协议栈，不需要打包拆包、计算校验和、维护序列号和应答等等，只是将应用层数据从一个进程宝贝到另一个进程。这是因为，IPC机制本质上是可靠的通信，而网络协议是为不可靠的通讯设计的。UNIX Demain Socket也提供面向流和面向数据包两种API接口，类似于TCP和UDP，但是面向消息的UNIX Domain Socket也是可靠的，消息既不会丢失也不会顺序错乱。

UNIX Domain Socket是全双工的，API接口语义丰富，相比其他IPC机制有明显的优越性，目前已成为使用最广泛的IPC机制，比如Window服务器和GUI程序之间就是通过UNIX Domain Socket通讯的。

### (二)、Android的进程间通信

Android上常见的进程间通信有以下几种情况：

- AIDL进程通信接口
- Binder进程通信接口
- Message通信框架
- Messager通信框架
- BroadCastReciever广播
- ContentProvider

其实还有一种方案上就是基于Unix进程通信的LocalSocket

### (三)、LocalSocket的相关结构

如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-7653ce372ceb2116.png)

里面涉及几个概念

* LocalSocket：客户端的套接字，在Unix域名空间创建的一个套接字，是对Linux中Socket进行了封装，采用JNI方式调用，实现进程间通信。具体就是Native层Server和Framework层Client进行通信，或在各层次中能使用Client/Server模式实现通信

* LocalSocketAddress：套接字地址，其实就是文件描述符(主要是服务器地址，当然也可以客户端自己绑定地址)

* LocalServerSocket：服务端的套接字，与LocalSocket相对应，创建套接字同时制定文件描述符

* LocalSocketImpl：Framework层Socket的实现，通过JNI调用系统socket API

* JNI访问接口：[frameworks/base/core/jni/android_net_LocalSocketImpl.cpp](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/jni/android_net_LocalSocketImpl.cpp)里面几个核心方法
  * socket_connect_local
  * socket_bind_local
  * socket_listen

看下这几个类的对应关系，如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-289afc197288980b.png)

使用Android的LocalSocket建立socket通信，是基于网络socket过程一致的。

## 九、createInstallArgs(InstallParams)方法解答

先看下这个方法在哪里被调用了？
是在handleStartCopy()方法里面被调用
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10669行

```kotlin
final InstallArgs args = createInstallArgs(this);
```

我们来看下方法内部的执行

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10669行

```csharp
   private InstallArgs createInstallArgs(InstallParams params) {
        if (params.move != null) {
            return new MoveInstallArgs(params);
        } else if (installOnExternalAsec(params.installFlags) || params.isForwardLocked()) {
            return new AsecInstallArgs(params);
        } else {
            return new FileInstallArgs(params);
        }
    }
```

这里面分别根据move字段和installOnExternalAsec方法来进入不同分支来进行分支判断
那一个一个来判断，看下InstallParams的move的值。

### 1、判断params.move是否为null

这时候要看下 InstallParams 的初始化地方在在 PackageManagerService 的 installPackageAsUser() 方法里面
 代码在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)  9572行

```csharp
        msg.obj = new InstallParams(origin, null, observer, installFlags, installerPackageName,
                null, verificationParams, user, packageAbiOverride, null);
```

注意下InstallParams的参数，下面来看下InstallParams的构造函数。

```kotlin
        InstallParams(OriginInfo origin, MoveInfo move, IPackageInstallObserver2 observer,
                int installFlags, String installerPackageName, String volumeUuid,
                VerificationParams verificationParams, UserHandle user, String packageAbiOverride,
                String[] grantedPermissions) {
            super(user);
            this.origin = origin;
            this.move = move;
            this.observer = observer;
            this.installFlags = installFlags;
            this.installerPackageName = installerPackageName;
            this.volumeUuid = volumeUuid;
            this.verificationParams = verificationParams;
            this.packageAbiOverride = packageAbiOverride;
            this.grantedRuntimePermissions = grantedPermissions;
        }
```

其中我们看到第二个参数是对应的move字段，而在new InstallParams对象的时候，我看到第二个参数是null。而在后续的整个流程，并没有给这move字段赋值，所以params.move等于null

结论：params.move等于null。

### 2、判断installOnExternalAsec(params.installFlag)和params.isForwardLocked()的值

首先来看下params.installFlag的值，通过上面InstallParams的值知道InstallParams的installFlag其实在构造InstallParams的时候，传入的变量installFlags，那向前捋捋，看看这个这个installFlags是什么时候初始化的，后续是否有发生什么值变化。发现这个installFlags其实是installPackageAsUser()的入参，那就再向前找.

发现在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 228行的的initView() 方法里面

```cpp
    public void initView() {
        ...
        int installFlags = 0;
        ...
    }
```

发现installFlags等于0，并且"新安装"的情况下，是没有变更installFlags的值的。所以在PackageService的installPackageAsUser方法里面的入参installFlags也是0，在进入installPackageAsUser里面有变更installFlags的值地方即在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)  9546行

```java
            installFlags &= ~PackageManager.INSTALL_FROM_ADB;
            installFlags &= ~PackageManager.INSTALL_ALL_USERS;
```

通过代码知道
 它显示先"取反"，然后依次进行位"与"操作。最后的结果是installOnExternalAsec(params.installFlags)是false和params.isForwardLocked()也是false。所以这个方法最后返回的是**FileInstallArgs**。

## 十、isVerificationEnabled(int userId, int installFlags) 的理解

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9957行

```kotlin
    /**
     * Check whether or not package verification has been enabled.
     *
     * @return true if verification should be performed
     */
    private boolean isVerificationEnabled(int userId, int installFlags) {
         // DEFAULT_VERIFY_ENABLE是个常量，为true
        if (!DEFAULT_VERIFY_ENABLE) {
            return false;
        }
        // 检查是否是否受限用户，如果是受限用户，则要进行检查
        boolean ensureVerifyAppsEnabled = isUserRestricted(userId, UserManager.ENSURE_VERIFY_APPS);

        // Check if installing from ADB
         // 如果是 从通过ADB安装
        if ((installFlags & PackageManager.INSTALL_FROM_ADB) != 0) {
            // Do not run verification in a test harness environment
           // 如果是测试工具，则不用检查
            if (ActivityManager.isRunningInTestHarness()) {
                return false;
            }
             // 如果是受限用户，则要进行检查
            if (ensureVerifyAppsEnabled) {
                return true;
            }
            // Check if the developer does not want package verification for ADB installs
             // 如果开发设置了在ADB安装的时候不需要检查包，则不用检查
            if (android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    android.provider.Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 1) == 0) {
                return false;
            }
        }

        // 如果是受限用户，则一定要进行包检验
        if (ensureVerifyAppsEnabled) {
            return true;
        }

        return android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                android.provider.Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) == 1;
    }
```

先翻译一下注释：

检查是否启用包验证
如果执行验证，则返回true

上面的注释已经解释的很清楚了，来看下最后一行代码

```css
android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                android.provider.Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) 
```

这行的代码意思如下：

PackageManagerServcie在安装之前是否发送广播以验证应用

- 1：表示 如果验证者存在，则在安装应用之前进行包验证，
- 0：表示 安装器那不要验证应用程序

## 十一、Context.sendBroadcast(Intent intent)的功能是和Context.sendBroadcastAsUser(Intent,UserHandle)一样的解答

### 1、Context.sendBroadcast(Intent intent)的具体实现

知道Context是一个抽象类，而具体实现类是ContextImpl。所以Context.sendBroadcast(Intent intent)的具体实现如下：
 代码在[ContextImpl.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java)   762行

```java
    @Override
    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess();
            ActivityManagerNative.getDefault().broadcastIntent(
                    mMainThread.getApplicationThread(), intent, resolvedType, null,
                    Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                    getUserId());
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }
```

### 2、Context. sendBroadcastAsUsersendBroadcastAsUser(Intent, UserHandle)的具体实现

Context是一个抽象类，而具体实现类是ContextImpl。所以Context.sendBroadcast(Intent intent)的具体实现如下：
 代码在[ContextImpl.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java)   923行

```java
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess();
            ActivityManagerNative.getDefault().broadcastIntent(mMainThread.getApplicationThread(),
                    intent, resolvedType, null, Activity.RESULT_OK, null, null, null,
                    AppOpsManager.OP_NONE, null, false, false, user.getIdentifier());
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }
```

其实对比两个方法，会发现，这两个方法其实都是调用ActivityManagerNative.getDefault().broadcastIntent方法而已，唯一的不同是，最后一个参数不同：sendBroadcast最后一个参数是getUserId()，而sendBroadcastAsUser方法最后一个参数是user.getIdentifier()。

这样我们在看下getUserId()方法里面的具体内容，如下图：
代码在[ContextImpl.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ContextImpl.java) 1770行

```java
    /** {@hide} */
    @Override
    public int getUserId() {
        return mUser.getIdentifier();
    }
```

我们发现getUserId()方法内部也是调用的mUser.getIdentifier()，所以我们说

Context.sendBroadcast(Intent intent)的功能是和Context.sendBroadcastAsUser(Intent,UserHandle)一样的。

## 十二、Split APK(APK拆分)与Instant Run简介

如果想了解官网，推荐[Android官方技术文档翻译——Apk 拆分机制](https://link.jianshu.com/?t=http://blog.csdn.net/maosidiaoxian/article/details/41692535)

### (一)、什么是Split APK(APK 拆分)

Split APK是Google为了解决66536上线，以及APK安装包越来越大等问题，在Android 5.0中引入的一种机制。Split APK可以将一个庞大的APK文件，按屏幕密度、ABI等形式拆分成多个独立的APK，在应用程序更新时，不必下载整个APK，只需要下载某个某块即可安装更新。Split APK 将原来一个APK中多个模块共享一份资源的模型分离成多个APK使用各自的资源，并且可以继承Base APK中的资源，多个APK有相同的data、cache目录、多个dex文件、相同的进程，在Settings.apk中只显示一个APK，并且使用相同的包名。

如下图

![](https://upload-images.jianshu.io/upload_images/5713484-f2b317ca1e65d20a.png)

PS：在Android Studio 2.3上，instant run的部署方案与之前的版本相比有了很大变化，之前是通过分dex来实现动态部署，而从Android Studio 2.3上则是通过Split APK技术。而在Android Studio 2.2，只有部署到Android Studio 6.0以上的设备才会使用Split APK 方案。 Android Studio 2.3则是连Android 5.0都会使用Split APK。在安装时会通过adb install-multiple 指令一次性安装。

### (二)、Splite APK效果图及解析

通俗的理解，之前是一个APK，而现在是通过Splite APK，则在APK安装目录下有多个APK。如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-0adf3262f03ce929.png)

看上图，一个外壳base.apk，一个依赖split_lib_dependencies_apk，然后将业务代码分成了10份，其中base.apk中的dex只包含了instant-run-server的代码以及AndroidManifest、资源文件等。查看这些分割的APK文件，会发现里面只有一个dex、AndroidManifest和mf文件夹。打开AndroidManifest文件，仅有一个manifest标签，然后有个split属性。而base.apk的manifest文件是没有这个属性的。从安装的源码可以看出，安装时必须要有一个apk是没有这个属性的，这个就是base.apk。

整个原理就是比较简单了，生成多个apk文件，把资源文件、manifest等放到base.apk，然后把业务dex分散到其他apk去，并且加入一个空的manifest文件，并指定split属性。在打包的过程中，业务dex不再打入主apk，而是和各自的manifest文件打包成新的apk。

要说Split APK就不得不说下 Instant Run，在这里简单的介绍下Instant Run

### (三)、Instant Run简介

[Instant Run官网](https://link.jianshu.com/?t=https://developer.android.com/studio/run/index.html#instant-run)

#### 1、 Instant Run 介绍

Instant Run，是android studio 2.0新增的一个运行机制，在编码开发、测试或debug的时候，它能显著减少你对当前APP"构建"和"部署"的时间。当第一次点击run、debug按钮的时候，它运行时间和我们平常一样，但是在后续的流程中，你每次修改代码后，点击run、debug按钮，对应的"改变"将迅速的部署到你正在运行的程序上，速度超级快。

#### 2、产生Instant Run的背景

在没有Instant Run的时候，一般修改代码，然后点击"run"的流程如此：构建->部署->安装->app登录->activity创建
如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-8375dbce83b5c852.png)

每一次都是重新安装，但是这样会导致大量的时间花在"构建->部署->安装->app登录->activity创建"上，这样就产生了一个需求，能否缩短这个时间。所有就有了Instant Run。

Instant Run产生的目的就是： **尽可能多的剔除不必要的流程，然后提升必要的流程的效率，从而缩短时间。**

只对代码改变部分做构建和部署，并不重新安装应用，并不重启应用，不重启Activity，就会大大缩短时间。

#### 3、 Instant Run的分类

按照是否需要重启当前Activity、是否需要重启APP(不是重新安装)这两个条件，把Instant Run分为3类：

* **Hot Swp——热插拔**：
   改变的代码被应用投射到APP上，不需要重启应用，不需要重新启动当前Activity。
   一般适用简单的改变的场景，比如一些方法简单的修改等

* **Warm Swap——温插拔**：
   Activity需要被重启才能看到所需修改
   一般适用涉及到了资源文件的修改，比如Resources。

* **Cold Swap——冷插拔**：
   APP需要被重启，这里说的重启，并不是重新安装。
   一般适用结构性变化的场景，比如修改了继承规则等。

如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-c5c7477470aa69c5.png)

#### 4、Instant Run的原理

Manifest整合，然后跟res、dex.file一起被合并到APK

![](https://upload-images.jianshu.io/upload_images/5713484-d23d8bda036206da.png)

manifest 文件合并、打包，和res一起被AAPT合并到APK中，同样项目代码被编译成字节码，然后转换成.dex文件，也被合并到APK中。

下面来看下首次运行Instant Run，Gradle执行的操作。

![](https://upload-images.jianshu.io/upload_images/5713484-5041ad2ef7e2b12d.png)

在有 Instant Run 的环境下：一个新的 App Server 类会被注入到App中，与Bytecode instrumentation协同监控代码的变化。同时会有一个新的Application类，它注入了一个自定义类加载器(Class Loader)，同时该Application类会启动所需要的新注入的App Server。于是Manifest会被修改来确保应用能使用这个新的Application类(这里不必担心自己继承定义了Application类，Instant Run添加的这个新Application类会代理自定义的Application类)。至此Instant Run可以跑起来了，在使用的时候，它会通过决策，合理运用热温冷插拔来协助我们大量地缩短构建程序时间。

## 参考文章

1. [APK安装流程详解14——PMS中的新安装流程上(拷贝)补充](https://www.jianshu.com/p/2d16ec2c9620)

