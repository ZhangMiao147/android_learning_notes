# Apk 安装流程6-PackageManagerService 启动前奏

本片文章的主要内容如下：

```
1、Settings类简介
2、SystemConfig类简介
3、ServiceThread类与PackageHandler类简介
4、PackageManagerServcie的systemReady方法简介
5、PackageManagerServcie的performBootDexOpt方法简介
6、PackageManagerService启动的预热
7、关于shared UID相关问题
8、PackageManagerService方法名中"LI"、"LP"、"LPw"、"LPr"的含义
9、@GuardBy、@SystemApi、@hide Android注解简介
```

## 一、Settings类简介

由于在后面讲解PackageManager流程启动的时候会 涉及到Setting类，我们就先预热下
[Settings.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FSettings.java)

### 1、重要成员变量

* **private final File mSettingsFilename**：代表的是"/data/system/packages.xml"文件

* **private final File mBackupSettingsFilename**：代表的是"/data/system/packages_backup/xml"文件，这个文件不一定存在，如果存在，因为他是备份文件，如果它存在，则说明上次更新packages.xml文件出错了。

* **private final File mPackageListFilename**：代表的是"/data/system/pcakages.list"文件

* **final ArrayMap<String, PackageSetting> mPackages = new ArrayMap<>()**：是一个ArrayMap的结构，key是包名，value是PackageSetting。PackageSetting主要包含了一个APP的基本信息，如安装位置，lib位置等信息。

* **final ArrayMap<String, SharedUserSetting> mSharedUsers =new ArrayMap<String, SharedUserSetting>()**：表示的是一个ArrayMap，key是类似于"android.ui.system"这样的字段，在Android中每一个应用都有一个UID，两个相同的UID的应用可以运行在同一个进程中，所以为了让两个应用运行在一个进程中，往往会在AndroidManifest.xml文件中设置shareUserId这个属性，这个属性就是一个字符串，但是我们知道Linux系统中一个uid是一个整型，所以为了将字符串和整形对应起来，就有了的ShareUserSetting类型，刚才说key是shareUserId这个属性的值，那么值就是SharedUserSetting类型了，ShareUserdSetting中除了name(其实就是key)，uid对应Linux系统的uid，还有一个列表字段，记录了当前系统中有相同的shareUserId的应用。

* **final ArrayMap<String, BasePermission> mPermissions=new ArrayMap<String, BasePermission>()**：代表的是主要保存的是"/system/etc/permissions/platform.xml"中的permission标签内容，因为Android系统是基于Linux系统，所以也有用户组的概念，在platform.xml中定义了一些权限，并且制定了哪些用户具有这些权限，一旦一个应用属于某一个用户组，那么它就拥有了这个用户组的所有权限

* **final ArrayMap<String, BasePermission> mPermissionTrees=new ArrayMap<String, BasePermission>()**：代表的是"packages.xml"文件的permission-trees标签

### 2、先来看下它的构造函数

代码在[Settings.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FSettings.java) 342行

```csharp
    Settings(Object lock) {
        this(Environment.getDataDirectory(), lock);
    }

    Settings(File dataDir, Object lock) {
        mLock = lock;
        // 创建RuntimePermissionPersistence对象，其中 RuntimePermissionPersistence是Settings的内部类
        mRuntimePermissionsPersistence = new RuntimePermissionPersistence(mLock);
        //初始化mSystemDir
        mSystemDir = new File(dataDir, "system");
        mSystemDir.mkdirs();
        // 设置权限
        FileUtils.setPermissions(mSystemDir.toString(),
                FileUtils.S_IRWXU|FileUtils.S_IRWXG
                |FileUtils.S_IROTH|FileUtils.S_IXOTH,
                -1, -1);
        // 初始化mSettingsFilename、mBackupSettingsFilename、mPackageListFilename，请注意他们的文件目录
        mSettingsFilename = new File(mSystemDir, "packages.xml");
        mBackupSettingsFilename = new File(mSystemDir, "packages-backup.xml");
        mPackageListFilename = new File(mSystemDir, "packages.list");
        FileUtils.setPermissions(mPackageListFilename, 0640, SYSTEM_UID, PACKAGE_INFO_GID);

        // Deprecated: Needed for migration
        // 通过注释 ，知道下面两个文件目录是不推荐使用的
        mStoppedPackagesFilename = new File(mSystemDir, "packages-stopped.xml");
        mBackupStoppedPackagesFilename = new File(mSystemDir, "packages-stopped-backup.xml");
    }
```

我们看到Settings的构造函数主要工作就是创建了系统文件夹，一些包管理的文件：

- packages.xml和package-backup.xml为一组，用于描述系统所安装的Package信息，其中packages-backup.xml是package.xml的备份。
- packages-list用于描述系统中存在的所有非系统自带的apk信息及UID大于10000的apk。当这些APK有变化时，PackageManagerService就会更新该文件

### 3、重要方法SharedUserSetting addSharedUserLPw(String, int, int, int)方法

添加特殊用户名称和UID并关联。

```csharp
    SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        // 获取SharedUserSetting对象，其中mSharedUsers是ArrayMap<String, SharedUserSetting> 来存储的
        SharedUserSetting s = mSharedUsers.get(name);
        if (s != null) {
            if (s.userId == uid) {
                return s;
            }
            PackageManagerService.reportSettingsProblem(Log.ERROR,
                    "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
        //如果在ArrayMap中没有找到对应的SharedUserSetting
        s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
        s.userId = uid;
        if (addUserIdLPw(uid, s, name)) {
            // 将 name和SharedUserSetting对象保存到mShareUsers的一个ArrayMap中
            mSharedUsers.put(name, s);
            return s;
        }
        return null;
    }
```

通过上面代码可知，Setting中有一个mSharedUsers成员，该成员存储的是字符串和SharedUserSetting键值对，通过addSharedUserLPw(String,int,int, int)方法将name和SharedUserSetting对象加到mSharedUsers列表中，这里我们主要关心两点，一是ShareUserSetting的架构、二是ShareUserSetting的作用：

#### 3.1、举例说明

在SystemUI的AndroidManifest.xml里面，如下所示

```go
<manifestxmlns:android="http://schemas.android.com/apk/res/android"
       package="com.android.systemui"
       coreApp="true"
       android:sharedUserId="android.uid.system"
       android:process="system">
```

在xml中，声明了一个名为android:sharedUserId的属性，其值为"android.uid.system"。shareUserId看起来和UID有关，确实如此，它有两个作用：

* 1 两个或者多个声明了同一种shareUserIds的APK可以共享彼此的数据，并且可以运行在同一个进程中。

* 2 通过声明特定的shareUserId，该APK所在进程将被赋予指定的UID。例如，本例中的SystemUI声明了system的uid，运行SystemUI进程就可共享system用户所对应的权限(实际上就是将该进程的uid设置为system的uid)

除了在AndroidManifest.xml中声明shareUserId外，APK在编译时还必须使用对应的证书进行签名。例如本例中的SystemUI，在Android.mk中需要额外声明LOCAL_CERTIFICATE := platform，如此，才可获得指定的UID。

还需要有三点需要引起大家注意：

* xml中的sharedUserId属性指定了一个字符串，它是UID的字符串描述，故对应数据结构中也应该有一个字符串，这样就把代码和XML中的属性联系起来。

* 在Linxu系统中，真正的UID是一个整形，所以该数据结构中必然有一个整形变量。

* 多个Package可以声明同一个shareUserId，因此数据结构必然会保存那些声明了相同的sharedUserId的Package的某些信息。

#### 3.2、ShareUserSetting的架构

PackageManagerService的构造函数中创建了一个Settings的实例mSettings，mSettings中有三个成员变量mSharedUsers，mUserIds，mOtherUserIds。addSharedUserLPw()方法都涉及到了这三个成员变量，看到PackageManagerService中创建了Settings的实例对象mSettings，addSharedUserLPw()函数是对mSetting成员变量mShareUsers进行操作。mShareUsers是以String类型的name为key，ShareUserSetting为value的ArrayMap，SharedUserSetting中的成员变量packages是一个PackageSetting类型的ArraySet；PackageSetting继承自PackageSettingBase，我们可以看到PackageSetting保存着package的多种信息。

![](https://upload-images.jianshu.io/upload_images/5713484-c7acf17cd8cbd947.png)

![](https://upload-images.jianshu.io/upload_images/5713484-17dea9556c978996.png)

#### 3.3、SharedUserId作用

我们在系统应用的AndroidManifest.xml中

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:androidprv="http://schemas.android.com/apk/prv/res/android"
    coreApp="true"
    package="com.android.settings"
    android:sharedUserId="android.uid.system"
    android:versionCode="1"
    android:versionName="1.0">
</manifest>
```

这里的android:shareUserId的属性对应着ShareUserSetting中的name，上面的addSharedUserLPw函数将shareUserId name和一个int 类型的UID对应起来，UID的定义在Process.java中

```dart
   //系统进程使用的UID/GID，值为1000
   publicstatic final int SYSTEM_UID = 1000;
   //Phone进程使用的UID/GID，值为1001
   publicstatic final int PHONE_UID = 1001;
   //shell进程使用的UID/GID，值为2000
   publicstatic final int SHELL_UID = 2000;
   //使用LOG的进程所在的组的UID/GID为1007
   publicstatic final int LOG_UID = 1007;
   //供WIF相关进程使用的UID/GID为1010
   publicstatic final int WIFI_UID = 1010;
  //mediaserver进程使用的UID/GID为1013
   publicstatic final int MEDIA_UID = 1013;
   //设置能读写SD卡的进程的GID为1015
   publicstatic final int SDCARD_RW_GID = 1015;
   //NFC相关的进程的UID/GID为1025
   publicstatic final int NFC_UID = 1025;
   //有权限读写内部存储的进程的GID为1023
   publicstatic final int MEDIA_RW_GID = 1023;
   //第一个应用Package的起始UID为10000
   publicstatic final int FIRST_APPLICATION_UID = 10000;
   //系统所支持的最大的应用Package的UID为99999
   publicstatic final int LAST_APPLICATION_UID = 99999;
   //和蓝牙相关的进程的GID为2000
   publicstatic final int BLUETOOTH_GID = 2000;
```

shareUserId与UID相关，作用是：

* 1、两个或多个APK或者进程声明了一种shareUserId的APK可以共享彼此的数据，并可以运行在同一进程中(相当于进程是系统的用户，某些进程可以归为同一用户使用，相当于Linux系统的GroupId)。

* 2、通过声明特定的sharedUserId，该APK所在的进程将被赋予指定的UID，将被赋予该UID特定的权限。

#### 3.4、总结

一图胜千言

![](https://upload-images.jianshu.io/upload_images/5713484-62dd6104baf8ede0.png)

## 二、SystemConfig类简介

[SystemConfig.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2FSystemConfig.java)
SystemCofig代表系统配置信息

### 1、SystemConfig的构造函数

代码在[SystemConfig.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2FSystemConfig.java) 147行

```cpp
    SystemConfig() {
        // Read configuration from system
        // 从系统中读取配置信息  目录是etc/sysconfig
        readPermissions(Environment.buildPath(
                Environment.getRootDirectory(), "etc", "sysconfig"), false);
        // Read configuration from the old permissions dir
         // 从系统中读取配置信息  目录是etc/permissions 
        readPermissions(Environment.buildPath(
                Environment.getRootDirectory(), "etc", "permissions"), false);
        // Only read features from OEM config
        //从oem 目录下读取sysconfig和permission目录下的文件
        readPermissions(Environment.buildPath(
                Environment.getOemDirectory(), "etc", "sysconfig"), true);
        readPermissions(Environment.buildPath(
                Environment.getOemDirectory(), "etc", "permissions"), true);
    }
```

SystemConfig的构造函数中主要通过readPermission函数将对应目录下的xml文件中定义的各个节点读取出来保存到SystemConfig成员变量中。第一个参数对应文件目录；第二个参数是从xml文件中解析内容的范围，比如对于system目录，是全部解析：ALLOW_ALL。我们到system/etc/permission目录下可以看到很多xml类的配置文件，如下：

```css
android.hardware.bluetooth.xml
android.hardware.bluetooth_le.xml
android.hardware.camera.flash-autofocus.xml
android.hardware.camera.front.xml
...
platform.xml
...
```

这些都是编译时从framwork指定位置拷贝过来的(framework/native/data/etc/)

readPermission(File,int)方法内部调用readPermissionsFromXml()方法来解析xml中各个节点，其中xml涉及到的标签内容有feature、library、permission、assign-permission等，这些标签的内容都将解析出来保存到SystemConfig的对应数据结构的全局变量中以便以后查询管理。festure标签用来描述设备是否支持硬件特性；library用于指定系统库，当应用程序运行时，系统会为进程加载一些必要的库，permission用于将permission与gid关联，系统会为进程加载一些必要库，permission用于将permission与gid关联，assign-permission将system中描述的permission与uid关联等等；其中解析permission调用了readPermission()方法进行权限的解析。

总结下SystemConfig()初始化时解析的xml文件节点及对应的全局变量

如下图

![](https://upload-images.jianshu.io/upload_images/5713484-008d430e2fb78ac5.png)

## 三、ServiceThread类与PackageHandler类简介

### 1、ServiceThread类

[ServiceThread.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2FServiceThread.java)
代码很简单如下：

```java
/**
 * Special handler thread that we create for system services that require their own loopers.
 */
public class ServiceThread extends HandlerThread {
    private static final String TAG = "ServiceThread";

    private final boolean mAllowIo;

    public ServiceThread(String name, int priority, boolean allowIo) {
        super(name, priority);
        mAllowIo = allowIo;
    }

    @Override
    public void run() {
        Process.setCanSelfBackground(false);

        // For debug builds, log event loop stalls to dropbox for analysis.
        if (!mAllowIo && StrictMode.conditionallyEnableDebugLogging()) {
            Slog.i(TAG, "Enabled StrictMode logging for " + getName() + " looper.");
        }
        super.run();
    }
}
```

通过注释我们知道，这个类其实是System server创建的线程，由于它是继承自HandlerThread，所以它有一个自己的Looper。

### 2、PackageHandler类

PackageHandler是PackageManagerService的内部类。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 1099行

PackageManageService启动的时候会将PackageHandler和ServiceThread进行绑定。ServiceThread其实就是PackageManageService的工作线程，PackageManageService的各种操作都将利用PackageHandler分发到HandlerThread去处理。

## 四、PackageManagerServcie的systemReady方法简介

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 14658行

```java
    @Override
    public void systemReady() {
        mSystemReady = true;

        // Read the compatibilty setting when the system is ready.
        boolean compatibilityModeEnabled = android.provider.Settings.Global.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.Global.COMPATIBILITY_MODE, 1) == 1;
        PackageParser.setCompatibilityModeEnabled(compatibilityModeEnabled);
        if (DEBUG_SETTINGS) {
            Log.d(TAG, "compatibility mode:" + compatibilityModeEnabled);
        }

        int[] grantPermissionsUserIds = EMPTY_INT_ARRAY;
        synchronized (mPackages) {
            // Verify that all of the preferred activity components actually
            // exist.  It is possible for applications to be updated and at
            // that point remove a previously declared activity component that
            // had been set as a preferred activity.  We try to clean this up
            // the next time we encounter that preferred activity, but it is
            // possible for the user flow to never be able to return to that
            // situation so here we do a sanity check to make sure we haven't
            // left any junk around.
            ArrayList<PreferredActivity> removed = new ArrayList<PreferredActivity>();
            for (int i=0; i<mSettings.mPreferredActivities.size(); i++) {
                PreferredIntentResolver pir = mSettings.mPreferredActivities.valueAt(i);
                removed.clear();
                for (PreferredActivity pa : pir.filterSet()) {
                    if (mActivities.mActivities.get(pa.mPref.mComponent) == null) {
                        removed.add(pa);
                    }
                }
                if (removed.size() > 0) {
                    for (int r=0; r<removed.size(); r++) {
                        PreferredActivity pa = removed.get(r);
                        Slog.w(TAG, "Removing dangling preferred activity: "
                                + pa.mPref.mComponent);
                        pir.removeFilter(pa);
                    }
                    mSettings.writePackageRestrictionsLPr(
                            mSettings.mPreferredActivities.keyAt(i));
                }
            }

            for (int userId : UserManagerService.getInstance().getUserIds()) {
                if (!mSettings.areDefaultRuntimePermissionsGrantedLPr(userId)) {
                    grantPermissionsUserIds = ArrayUtils.appendInt(
                            grantPermissionsUserIds, userId);
                }
            }
        }

         // 多用户服务
        sUserManager.systemReady();

        // If we upgraded grant all default permissions before kicking off.
        // 升级所有已获的默认权限
        for (int userId : grantPermissionsUserIds) {
            mDefaultPermissionPolicy.grantDefaultPermissions(userId);
        }

        // Kick off any messages waiting for system ready
        // 处理所有等待系统准备就绪的消息
        if (mPostSystemReadyMessages != null) {
            for (Message msg : mPostSystemReadyMessages) {
                msg.sendToTarget();
            }
            mPostSystemReadyMessages = null;
        }

        // Watch for external volumes that come and go over time
         // 观察外部存储设备
        final StorageManager storage = mContext.getSystemService(StorageManager.class);
        storage.registerListener(mStorageListener);

        mInstallerService.systemReady();
        mPackageDexOptimizer.systemReady();
        MountServiceInternal mountServiceInternal = LocalServices.getService(
                MountServiceInternal.class);
        mountServiceInternal.addExternalStoragePolicy(
                new MountServiceInternal.ExternalStorageMountPolicy() {
            @Override
            public int getMountMode(int uid, String packageName) {
                if (Process.isIsolated(uid)) {
                    return Zygote.MOUNT_EXTERNAL_NONE;
                }
                if (checkUidPermission(WRITE_MEDIA_STORAGE, uid) == PERMISSION_GRANTED) {
                    return Zygote.MOUNT_EXTERNAL_DEFAULT;
                }
                if (checkUidPermission(READ_EXTERNAL_STORAGE, uid) == PERMISSION_DENIED) {
                    return Zygote.MOUNT_EXTERNAL_DEFAULT;
                }
                if (checkUidPermission(WRITE_EXTERNAL_STORAGE, uid) == PERMISSION_DENIED) {
                    return Zygote.MOUNT_EXTERNAL_READ;
                }
                return Zygote.MOUNT_EXTERNAL_WRITE;
            }

            @Override
            public boolean hasExternalStorage(int uid, String packageName) {
                return true;
            }
        });
    }
```

## 五、PackageManagerServcie的performBootDexOpt方法简介

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 6024行

```java
    @Override
    public void performBootDexOpt() {

        // 确保只有system或者 root uid有权限执行该方法
        enforceSystemOrRoot("Only the system can request dexopt be performed");

        // Before everything else, see whether we need to fstrim.
        try {

            // 运行在同一个进程，此处拿到MountServcie服务端
            IMountService ms = PackageHelper.getMountService();
            if (ms != null) {

               //处于更新状态，则执行fstrim 
                final boolean isUpgrade = isUpgrade();
                boolean doTrim = isUpgrade;
                if (doTrim) {
                    Slog.w(TAG, "Running disk maintenance immediately due to system update");
                } else {

                     // interval默认值为3天
                    final long interval = android.provider.Settings.Global.getLong(
                            mContext.getContentResolver(),
                            android.provider.Settings.Global.FSTRIM_MANDATORY_INTERVAL,
                            DEFAULT_MANDATORY_FSTRIM_INTERVAL);
                    if (interval > 0) {
                        final long timeSinceLast = System.currentTimeMillis() - ms.lastMaintenance();
                        if (timeSinceLast > interval) {
                            // 距离他上次fstrim时间超过3天，则执行fstrim
                            doTrim = true;
                            Slog.w(TAG, "No disk maintenance in " + timeSinceLast
                                    + "; running immediately");
                        }
                    }
                }
                if (doTrim) {
                    if (!isFirstBoot()) {
                        try {
                            ActivityManagerNative.getDefault().showBootMessage(
                                    mContext.getResources().getString(
                                            R.string.android_upgrading_fstrim), true);
                        } catch (RemoteException e) {
                        }
                    }
                    // 此处ms是指MountServcie，该过程发送消息H_FSTRIM给handler，然后再向vold发送fstrim命令
                    ms.runMaintenance();
                }
            } else {
                Slog.e(TAG, "Mount service unavailable!");
            }
        } catch (RemoteException e) {
            // Can't happen; MountService is local
        }

        final ArraySet<PackageParser.Package> pkgs;
        synchronized (mPackages) {
             // 清空延迟执行dexopt操作的app，获取dexopt操作的app集合
            pkgs = mPackageDexOptimizer.clearDeferredDexOptPackages();
        }

        if (pkgs != null) {
            // Sort apps by importance for dexopt ordering. Important apps are given more priority
            // in case the device runs out of space.
            ArrayList<PackageParser.Package> sortedPkgs = new ArrayList<PackageParser.Package>();
            // Give priority to core apps.
            for (Iterator<PackageParser.Package> it = pkgs.iterator(); it.hasNext();) {
                PackageParser.Package pkg = it.next();
                 // 将pkgs中的核心app添加到sortedPkgs
                if (pkg.coreApp) {
                    if (DEBUG_DEXOPT) {
                        Log.i(TAG, "Adding core app " + sortedPkgs.size() + ": " + pkg.packageName);
                    }
                    sortedPkgs.add(pkg);
                    it.remove();
                }
            }
            // Give priority to system apps that listen for pre boot complete.
             // 获取监听PRE_BOOT_COMPLETE的系统app集合
            Intent intent = new Intent(Intent.ACTION_PRE_BOOT_COMPLETED);
            ArraySet<String> pkgNames = getPackageNamesForIntent(intent);
            for (Iterator<PackageParser.Package> it = pkgs.iterator(); it.hasNext();) {
                PackageParser.Package pkg = it.next();
                // 将pkg监听PRE_BOOT_COMPLETE的app添加到sortedPkgs
                if (pkgNames.contains(pkg.packageName)) {
                    if (DEBUG_DEXOPT) {
                        Log.i(TAG, "Adding pre boot system app " + sortedPkgs.size() + ": " + pkg.packageName);
                    }
                    sortedPkgs.add(pkg);
                    it.remove();
                }
            }
            // Filter out packages that aren't recently used.
             // 获取pkgs中最近一周使用过的app
            filterRecentlyUsedApps(pkgs);
            // Add all remaining apps.
            for (PackageParser.Package pkg : pkgs) {
                if (DEBUG_DEXOPT) {
                    Log.i(TAG, "Adding app " + sortedPkgs.size() + ": " + pkg.packageName);
                }

                // 将最近一周的app添加到sortedPkgs
                sortedPkgs.add(pkg);
            }

            // If we want to be lazy, filter everything that wasn't recently used.
            if (mLazyDexOpt) {

                filterRecentlyUsedApps(sortedPkgs);
            }
            int i = 0;
            int total = sortedPkgs.size();
            File dataDir = Environment.getDataDirectory();
            long lowThreshold = StorageManager.from(mContext).getStorageLowBytes(dataDir);
            if (lowThreshold == 0) {
                throw new IllegalStateException("Invalid low memory threshold");
            }
            for (PackageParser.Package pkg : sortedPkgs) {
                long usableSpace = dataDir.getUsableSpace();
                if (usableSpace < lowThreshold) {
                    Log.w(TAG, "Not running dexopt on remaining apps due to low memory: " + usableSpace);
                    break;
                }
                performBootDexOpt(pkg, ++i, total);
            }
        }
    }
```

本方法的主要功能：

当处于升级或者三天未执行fstrim，则本地会是否会执行fstrim操作，对sortedPkgs中的app执行dexopt优化，其中包含：

- mDeferredDexOpt中的核心app
- mDeferredDexopt中监听PRE_BOOT_COMPLETE的app
- mDeferredDexOpt中最近一周使用过的app

上面方法中涉及了2个核心方法：

* private void filterRecentlyUsedApps(Collection<PackageParser.Package>) 方法

* private void performBootDexOpt(PackageParser.Package, int, int)方法

### 1、filterRecentlyUsedApps(Collection<PackageParser.Package>) 方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 6133行

```csharp
    private void filterRecentlyUsedApps(Collection<PackageParser.Package> pkgs) {
        // Filter out packages that aren't recently used.
        //
        // The exception is first boot of a non-eng device (aka !mLazyDexOpt), which
        // should do a full dexopt.
        if (mLazyDexOpt || (!isFirstBoot() && mPackageUsage.isHistoricalPackageUsageAvailable())) {
            int total = pkgs.size();
            int skipped = 0;
            long now = System.currentTimeMillis();
            for (Iterator<PackageParser.Package> i = pkgs.iterator(); i.hasNext();) {
                PackageParser.Package pkg = i.next();
                // 过滤最近使用过的app
                long then = pkg.mLastPackageUsageTimeInMills;
                if (then + mDexOptLRUThresholdInMills < now) {
                    if (DEBUG_DEXOPT) {
                        Log.i(TAG, "Skipping dexopt of " + pkg.packageName + " last resumed: " +
                              ((then == 0) ? "never" : new Date(then)));
                    }
                    i.remove();
                    skipped++;
                }
            }
            if (DEBUG_DEXOPT) {
                Log.i(TAG, "Skipped optimizing " + skipped + " of " + total);
            }
        }
    }
```

这个方法主要是就是过滤掉最近使用过的app，过滤条件就是筛选mDexOptLRUThresholdInMills时间。不同版本的条件不同：

- 对于Eng版本，则只会对30分钟之内使用过的app执行优化
- 对于用户版本，则会将用户最近以后组使用过的app执行优化

### 2、performBootDexOpt(PackageParser.Package, int, int)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 6176行

```csharp
    private void performBootDexOpt(PackageParser.Package pkg, int curr, int total) {
        if (DEBUG_DEXOPT) {
            Log.i(TAG, "Optimizing app " + curr + " of " + total + ": " + pkg.packageName);
        }
        if (!isFirstBoot()) {
            try {
                ActivityManagerNative.getDefault().showBootMessage(
                        mContext.getResources().getString(R.string.android_upgrading_apk,
                                curr, total), true);
            } catch (RemoteException e) {
            }
        }
        PackageParser.Package p = pkg;
        synchronized (mInstallLock) {
            mPackageDexOptimizer.performDexOpt(p, null /* instruction sets */,
                    false /* force dex */, false /* defer */, true /* include dependencies */,
                    false /* boot complete */);
        }
    }
```

我们看到这个方法其实内部很简单，主要就是调用mPackageDexOptimizer的performDexOpt方法，那我们就来看下这个方法的具体实现：

#### 2.1、performBootDexOpt(PackageParser.Package, int, int)方法

代码在[PackageDexOptimizer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageDexOptimizer.java) 73行

```java
    /**
     * Performs dexopt on all code paths and libraries of the specified package for specified
     * instruction sets.
     *
     * <p>Calls to {@link com.android.server.pm.Installer#dexopt} are synchronized on
     * {@link PackageManagerService#mInstallLock}.
     */
    int performDexOpt(PackageParser.Package pkg, String[] instructionSets,
            boolean forceDex, boolean defer, boolean inclDependencies, boolean bootComplete) {
        ArraySet<String> done;
        if (inclDependencies && (pkg.usesLibraries != null || pkg.usesOptionalLibraries != null)) {
            done = new ArraySet<String>();
            done.add(pkg.packageName);
        } else {
            done = null;
        }
        synchronized (mPackageManagerService.mInstallLock) {
            final boolean useLock = mSystemReady;
            if (useLock) {
                mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                mDexoptWakeLock.acquire();
            }
            try {
                return performDexOptLI(pkg, instructionSets, forceDex, defer, bootComplete, done);
            } finally {
                if (useLock) {
                    mDexoptWakeLock.release();
                }
            }
        }
    }
```

#### 2.2、performBootDexOpt(PackageParser.Package, int, int)方法

代码在[PackageDexOptimizer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageDexOptimizer.java) 73行

```java
    /**
     * Performs dexopt on all code paths and libraries of the specified package for specified
     * instruction sets.
     *
     * <p>Calls to {@link com.android.server.pm.Installer#dexopt} are synchronized on
     * {@link PackageManagerService#mInstallLock}.
     */
    int performDexOpt(PackageParser.Package pkg, String[] instructionSets,
            boolean forceDex, boolean defer, boolean inclDependencies, boolean bootComplete) {
        ArraySet<String> done;
        // 是否有依赖库
        if (inclDependencies && (pkg.usesLibraries != null || pkg.usesOptionalLibraries != null)) {
            done = new ArraySet<String>();
            done.add(pkg.packageName);
        } else {
            done = null;
        }
        synchronized (mPackageManagerService.mInstallLock) {
            final boolean useLock = mSystemReady;
            if (useLock) {
                mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                mDexoptWakeLock.acquire();
            }
            try {
               // 核心代码
                return performDexOptLI(pkg, instructionSets, forceDex, defer, bootComplete, done);
            } finally {
                if (useLock) {
                    mDexoptWakeLock.release();
                }
            }
        }
    }
```

通过这个方法我们知道，其实它本质是调用performDexOptLI(PackageParser.Package,String[],String[],boolean.String,CompilerStats.PackageStats)方法

#### 2.3、performDexOptLI(PackageParser.Package,String[],String[],boolean.String,CompilerStats.PackageStats)方法

代码在[PackageDexOptimizer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageDexOptimizer.java) 98行

```dart
    private int performDexOptLI(PackageParser.Package pkg, String[] sharedLibraries,
            String[] targetInstructionSets, boolean checkProfiles, String targetCompilerFilter,
            CompilerStats.PackageStats packageStats) {
        final String[] instructionSets = targetInstructionSets != null ?
                targetInstructionSets : getAppDexInstructionSets(pkg.applicationInfo);

        if (!canOptimizePackage(pkg)) {
            return DEX_OPT_SKIPPED;
        }

        final List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        final int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);

        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter);
        // If any part of the app is used by other apps, we cannot use profile-guided
        // compilation.
        if (isProfileGuidedFilter && isUsedByOtherApps(pkg)) {
            checkProfiles = false;

            targetCompilerFilter = getNonProfileGuidedCompilerFilter(targetCompilerFilter);
            if (DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter)) {
                throw new IllegalStateException(targetCompilerFilter);
            }
            isProfileGuidedFilter = false;
        }

        // If we're asked to take profile updates into account, check now.
        boolean newProfile = false;
        if (checkProfiles && isProfileGuidedFilter) {
            // Merge profiles, see if we need to do anything.
            try {
                newProfile = mInstaller.mergeProfiles(sharedGid, pkg.packageName);
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to merge profiles", e);
            }
        }

        final boolean vmSafeMode = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) != 0;
        final boolean debuggable = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        boolean performedDexOpt = false;
        boolean successfulDexOpt = true;

        final String[] dexCodeInstructionSets = getDexCodeInstructionSets(instructionSets);
        for (String dexCodeInstructionSet : dexCodeInstructionSets) {
            for (String path : paths) {
                int dexoptNeeded;
                try {
                    dexoptNeeded = DexFile.getDexOptNeeded(path,
                            dexCodeInstructionSet, targetCompilerFilter, newProfile);
                } catch (IOException ioe) {
                    Slog.w(TAG, "IOException reading apk: " + path, ioe);
                    return DEX_OPT_FAILED;
                }
                dexoptNeeded = adjustDexoptNeeded(dexoptNeeded);
                if (PackageManagerService.DEBUG_DEXOPT) {
                    Log.i(TAG, "DexoptNeeded for " + path + "@" + targetCompilerFilter + " is " +
                            dexoptNeeded);
                }

                final String dexoptType;
                String oatDir = null;
               // 判断类别
                switch (dexoptNeeded) {
                    case DexFile.NO_DEXOPT_NEEDED:
                        continue;
                    case DexFile.DEX2OAT_NEEDED:
                        // 需要把dex转化为oat
                        dexoptType = "dex2oat";
                        oatDir = createOatDirIfSupported(pkg, dexCodeInstructionSet);
                        break;
                    case DexFile.PATCHOAT_NEEDED:
                        dexoptType = "patchoat";
                        break;
                    case DexFile.SELF_PATCHOAT_NEEDED:
                        dexoptType = "self patchoat";
                        break;
                    default:
                        throw new IllegalStateException("Invalid dexopt:" + dexoptNeeded);
                }

                String sharedLibrariesPath = null;
                if (sharedLibraries != null && sharedLibraries.length != 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String lib : sharedLibraries) {
                        if (sb.length() != 0) {
                            sb.append(":");
                        }
                        sb.append(lib);
                    }
                    sharedLibrariesPath = sb.toString();
                }
                Log.i(TAG, "Running dexopt (" + dexoptType + ") on: " + path + " pkg="
                        + pkg.applicationInfo.packageName + " isa=" + dexCodeInstructionSet
                        + " vmSafeMode=" + vmSafeMode + " debuggable=" + debuggable
                        + " target-filter=" + targetCompilerFilter + " oatDir = " + oatDir
                        + " sharedLibraries=" + sharedLibrariesPath);
                // Profile guide compiled oat files should not be public.
                final boolean isPublic = !pkg.isForwardLocked() && !isProfileGuidedFilter;
                final int profileFlag = isProfileGuidedFilter ? DEXOPT_PROFILE_GUIDED : 0;
                final int dexFlags = adjustDexoptFlags(
                        ( isPublic ? DEXOPT_PUBLIC : 0)
                        | (vmSafeMode ? DEXOPT_SAFEMODE : 0)
                        | (debuggable ? DEXOPT_DEBUGGABLE : 0)
                        | profileFlag
                        | DEXOPT_BOOTCOMPLETE);

                try {
                    long startTime = System.currentTimeMillis();

                    // 核心代码
                    mInstaller.dexopt(path, sharedGid, pkg.packageName, dexCodeInstructionSet,
                            dexoptNeeded, oatDir, dexFlags, targetCompilerFilter, pkg.volumeUuid,
                            sharedLibrariesPath);
                    performedDexOpt = true;

                    if (packageStats != null) {
                        long endTime = System.currentTimeMillis();
                        packageStats.setCompileTime(path, (int)(endTime - startTime));
                    }
                } catch (InstallerException e) {
                    Slog.w(TAG, "Failed to dexopt", e);
                    successfulDexOpt = false;
                }
            }
        }

        if (successfulDexOpt) {
            // If we've gotten here, we're sure that no error occurred. We've either
            // dex-opted one or more paths or instruction sets or we've skipped
            // all of them because they are up to date. In both cases this package
            // doesn't need dexopt any longer.
            return performedDexOpt ? DEX_OPT_PERFORMED : DEX_OPT_SKIPPED;
        } else {
            return DEX_OPT_FAILED;
        }
    }
```

通过上面代码我们知道这块代码最后是调用的mInstaller的dexopt方法。

## 六、PackageManagerService启动的预热

PackageManagerService 在启动时会扫描所有APK文件和Jar包，然后把它们的信息读取出来，保存在内存中，这样系统运行时就能迅速找到各种应用和组件的信息。扫描过程中如果遇到没有优化过的文件，还要执行转化工作。(Android 5.0是odex格式，Android 5.0之后是oat格式)。启动后，PackageManagerService将提供安装包的信息查询服务以及应用的安装和卸载服务。

PackageManagerService中有一些规则比较难以理解，而在代码中有很大的区域在描述这些规则，这些规则设计安装包的关系。所以说如果能理解它们的关系，对于后续理解PackageManagerService的启动有很大的帮助。

### (一)、应用分类

Android 中应用可以简单地分成两大类："系统应用"和"普通应用"

* 1、系统应用是指位于/system/app 或者 /System/priv-app 目录下的应用。priv-app目录是从Android 4.4开始出现的目录，它存放的是一些系统底层的应用，如Settin、SystemUI等。/system/app中存放的是一些系统级别的应用，如：Phone、Contact等。在PackageManagerService 中，所谓的system 应用包括这两个目录下的应用，而所谓的private应用就是指 /priv-app 目录下的应用。

* 2、普通应用就是用户安装的应用，位于目录/data/app下。普通应用也可以安装在SD上，但是系统应用不可以。

### (二)、几种特殊情况的

#### 1、系统应用升级不在同一个目录里面

通常情况下系统应用是不能删除的，但是可以升级。升级的方法是安装一个包名相同，但是如果更高的版本号的应用在/data/app目录下。对于系统中这种的升级情况，Android会在/data/system/package.xml文件中用\<update-package>记录被覆盖的系统应用信息。

#### 2、两个包名的情况

我们知道应用的包名通常是AndroidManifest.xml里面用"package"属性来指定，同时还可以用\<original-package>来指定原始包的名字。但是，这样的一个应用运行时，看到的包名是什么?答案是两种都有可能。如果安装的设备中不存在和原始包名相同的系统应用，看到的包名将是package属性定义的名称。如果设备上还存在低版本的，而且包名和原始包名不相同的应用，这样虽然最后安装运行的是新安装的应用，但是我们看到应用的名称还是原始的包名，因为，这样也构成了升级关系。Android会在package.xml中用标签\<rename-package>记录这种改名的情况。

#### 3、其他内容

每个应用还有保存数据的目录，位于/data/data/packageName/目录下。数据目录中常见的两个子目录：shared_prefs目录中保存的是应用的设置文件，database目录中保存的是应用的数据库文件

## 七、关于shared UID相关问题

### (一)、Android中的UID、GID与GIDS的简介

说ShardUID就不能不说下Android中的UID、GID与GIDS

Android 是一个权限分离的操作系统。这是因为Android是基于Linux系统的权限管理机制，通过为每一个Application分配不同的uid和gid，从而使得不同的Application之间的私有数据和访问达到隔离的目的。Android的权限分离的基础是Linux已有的uid、gid、gids基础上的。

#### 1、UID

在Android系统上安装一个应用程序，就会为他分配一个UID，其中普通的Android APP的UID是从10000开始分配的。而10000以下则是系统UID。

#### 2、GID

对于普通的应用程序来说，GID等于UID。由于每个应用程序的UID和GID不相同，因为不管是Native层还是Java层都能够达到保护私有数据的作用。

#### 3、GIDS

GIDS是由框架在Application安装过程中生成的，与Application申请的权限有关。如果Application申请相应的权限被granted，而且其中有对应的GIDS，那么这个Application的GIDS将包含这个GIDS

### (二)、shared UID

假设app A要和app B 共享一个UID，那么 app B 不需要配置 Share UID，它会获得一个普通的 UID，需要配置 Share UID 的app A ，这时候系统会将 app B的 UID分配给 app A，这样就达到了共享 UID的目的。所以 两个app 有相同的 UID，并不意味着 它们会运行在同一个进程中。一个 app 运行在哪一个进程，一般是由它们 package名称和 UID来决定的，也就是说，UID相同，但是package名称不同的两个 app是运行在两个不同的进程中的。如果两个app，具有相同的UID和包名相同，加上签名相同，一般认为是一个应用程序，即覆盖安装。
 系统给每一个app都分配一个 UID 是用来控制权限的，因为两个程序具有相同的UID，就意味着它们有相同的权限，可以进行资源共享。相同的UID的资源共享只是针对Linux文件系统的访问全权限控制，不同进程间的数据是无法共享访问的。

## 八、PackageManagerService方法名中"LI"、"LP"、"LPw"、"LPr"的含义

### (一)、结尾是"LI"、"LP"、"LPw"、"LPr"的方法

Android 7.0多了一个"LIF"方法，我也加上去了，主要是Android 6.0的部分"LI"方法变更为"LIF"方法。

![](https://upload-images.jianshu.io/upload_images/5713484-b53f9d5915849d85.png)

### (二) L、I、P、w、r的含义

要想明白方法名中 LI、LIF、LPw、LPr的含义需要首先了解PackageManagerService内部使用的两个锁。因为LI、LIF、LPw 、LPr中的"L" 指的是Lock，而后面跟的"I"和"P"指的是两个锁，"I" 表示 *mInstallLock* 同步锁。"P"表示 *mPackage* 同步锁。为什么我会这么说，大家请看下面代码

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 469行

```dart
// Lock for state used when installing and doing other long running
// operations.  Methods that must be called with this lock held have
// the suffix "LI".
final Object mInstallLock = new Object();
// Keys are String (package name), values are Package.  This also serves
// as the lock for the global state.  Methods that must be called with
// this lock held have the prefix "LP".
@GuardedBy("mPackages")
final ArrayMap<String, PackageParser.Package> mPackages =
        new ArrayMap<String, PackageParser.Package>();
```

先来简单的翻译一下注释：

- 在安装和执行其他耗时操作的锁定状态。必须使用此锁定调用的方法，该方法后缀带有"LI"
- 键(key)是String类型，值(value)是包。这也是全局的锁，必须使用这个锁来调用具有前缀"LP"的方法

所以说：

* **mPackage 同步锁**：是指操作*mPackage*时，用*synchronized (mPackages) {}*保护起来。*mPackage*同步锁用来保护内存中已经解析的包信息和其他相关状态。*mPackage*同步锁是细颗粒度的锁，只能短时间持有这个锁，因为挣抢这个锁的请求很多。短时间持有*mPackage*锁，可以让其他请求等待的时间短些

* **mInstallLock同步锁**：是指安装APK的时候，对安装的处理要用用*synchronized (mInstallLock) {}*保护起来。*mInstallLock* 用来保护所有对 installd 的访问。installd通常包含对应用数据的繁重操作。

PS：由于installd 是单线程的，并且installd的操作通常很慢，所以在已经持有*mPackage*同步锁的时候，不要再去请求*mInstallLock* 同步锁。反之，在已经持有*mInstallLock* 同步锁的时候，可以去请求*mPackage*同步锁

- r 表示读
- w 表示写

### (三) 、LI、LIF、LPw、LPr的含义

| 方法名   |                           使用方式                           |
| -------- | :----------------------------------------------------------: |
| xxxLI()  |                  必须先持有mInstallLock的锁                  |
| xxxLP()  |                    必须先持有mPackage的锁                    |
| xxxLIF() | 必须先持有mInstallLock的锁，并且正在被修改的包(package) 必须被冻结(be frozen) |
| xxxLPr() |           必须先持有mPackages锁，并且只用于读操作            |
| xxxLPw() |           必须先持有 mPackage锁，并且只用于写操作            |

### (四) 、总结

简单的总结下就是
上面中的"L"代表lock的首字母L，”I“表示InstallLock的首字母，"P"表示package的首字母，"r"表示read的首字母，”w“表示write的首字母

## 九、@GuardBy、@SystemApi、@hide Android注解简介

### (一)、@GuardBy注解

在阅读PackageManagerService.java的源码时，里面有一个重要的成员变量mInstaller，它使用了@GuardBy注解。

如下，代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 448行

```kotlin
    @GuardedBy("mInstallLock")
    final Installer mInstaller;
```

它类似于Java关键字——synchronized关键字，但使用代替锁。可以这样使用这个注解

```kotlin
public class demo {
  @GuardedBy("this")
  public String string;
}
```

正如我们看到的，用法是@GuardedBy(lock)，这意味着有保护的字段或者方法只能在线程持有锁时被某些线程访问。我们可以将锁指定为以下类型：

* this：在其类中定义字段的对象的固有锁。

* classs-name.this：对于内部类，可能有必要消除"this"的歧义；class-name.this指定允许你指定"this"引用的意图。

* itself：仅供参考字段；字段引用的对象。

* field-name：锁定对象由字段名指定的(实例或静态)字段引用。

* class-name.field-name：锁对象由class-name.field-name指定的静态字段引用

* method-name()：锁定对象通过调用命名的nil-ary方法返回。

* class-name:指定类的Class对象做锁定对象

说白了，就是告知开发者，被@GuardedBy 注解标注的变量要用同步锁保护起来

举例说明

```java
public class BankAccount {
  private Object personInfo = new Object();

  @GuardedBy("personInfo")
  private int amount;
}
```

在上面的代码中，当有人获取了个人信息的同步锁时，可以访问金额，因此BankAccount中的金额由个人信息保护。

### (二)、@SystemApi、@PrivateApi与@hide注解简介

在阅读PackageManager.java的源码时，里面有会大量用到两个注解@SystemApi和@hide注解

如下，代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 448行

```java
     /**
     * Listener for changes in permissions granted to a UID.
     *
     * @hide
     */
    @SystemApi
    public interface OnPermissionsChangedListener {

        /**
         * Called when the permissions for a UID change.
         * @param uid The UID with a change.
         */
        public void onPermissionsChanged(int uid);
    }
```

@SystemApi 其实是@PrivateApi的别名；使用@hide标记的API可以不使用@SystemApi进行标记；但是当使用@SystemApi标记的API则必须使用@hide标记，在Android源码中，有两种类型的API无法通过标准的SDK进行访问。

@SystemApi和@hide的区别：

* 隐藏的方法(使用@hide修饰的)仍然可以通过Java反射机制进行访问；@hide标示只是javadoc的一部分(也是Android doc的一部分)，所以@hide修饰符仅仅指示了method/class/field不会被暴露到API中。

* 使用@SystemApi修饰的method/class/field，无法通过java反射机制进行访问(会触发invocationTargetException异常)

## 参考文章

1. [APK安装流程详解6——PackageManagerService启动前奏](https://www.jianshu.com/p/6bff82d8e0e6)

