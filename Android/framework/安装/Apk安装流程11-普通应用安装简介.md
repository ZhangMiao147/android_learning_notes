# Apk 安装流程11-普通应用安装简介

本片文章的主要内容如下：

```
1、概述
2、Android应用程序的几种安装方式
3、应用安装涉及到的目录
4、安装流程概述
5、PackageInstaller.apk与PackageManger
6、普通的APK安装方式的界面
7、PackageInstallerActivity类的安装流程
8、InstallAppProgress类的安装流程
9、InstallAppProgress中涉及到PackageManager的三个方法
```

## 一、 概述

众所周知，Android应用最终是打包成.apk格式(其实就是一个压缩包)，然后安装至手机并运行的。其中APK是Android Package的缩写。

Android系统在启动的过程中，会启动一个应用程序管理服务PackageManagerService，这个服务负责扫描系统中特定的目录，找到里面的应用程序文件，以.apk为后缀的文件，然后对这些文件进行解析，得到应用程序的相关信息，完成应用程序的安装过程。应用程序管理服务PackageManagerService安装应用程序的过程，其实就是解析应用程序配置文件的AndroidManifest.xml的过程，并从里面得到应用程序的相关信息，例如得到引用程序的组件Activity、Service、Receiver和Content Provider等信息，有了这些信息后，通过ActivityManagerService这个服务，我们就可以在系统中正常地使用这些应用程序了。

## 二、Android应用程序的几种安装方式

Android上应用安装可以分为以下几种方式：

* 1、系统安装：开机的时候，没有安装界面

* 2、adb 命令安装：通过abd命令行安装，没有安装界面

* 3、应用市场安装，这个要视应用的权限，有系统的权限无安装界面(例如MUI的小米应用商店)

* 4、第三方安装，有安装界面，通过packageinstaller.apk来处理安装及卸载的过程的界面

## 三、应用安装涉及到的目录

* **/system/app：**系统自带的应用程序，获得adb root 权限才能删除

* **/data/app：**用户程序安装的目录。安装时把apk文件复制到此目录

* **/data/data：**存放应用程序的数据

* **/data/dalvik-cache：**将apk中的dex文件安装到dalvik-cache目录下(dex文件是dalvik虚拟机的可执行文件，当然，ART-Android Runtime的可执行文件格式为.oat，启动ART时，系统会执行dex文件转换至oat文件)

* **/data/system：**该目录下的packages.xml文件。类似于Window的注册表，这个文件是解析apk时由writeLP()创建的，里面记录了系统的permissons，以及每个apk的name，codePath，flag，ts，version，userid等信息，这些信息主要通过apk的AndroidManifest解析获取，解析完apk后将更新信息写入这个文件并保存到flash，下次开机的时候直接从里面读取相关信息并添加到内存相关列表中。当有apk升级，安装或删除时会更新这个文件。
   -**/data/system/package.xml与/data/system/package.list：**packages.list指定了应用的默认存储位置/data/data/com.xxx.xxx；package.xml中包含了该应用申请的权限、签名和代码所在的位置等信息系，并且两者都有同一个userld。之所以每个应用都要一个userId，是因为Android在系统设计上把每个应用当做Linux系统上的一个用户对待，这样就可以利用已有的Linux用户管理机制来设计Android应用，比如应用目录，应用权限，应用进程管理等。

## 四、安装流程概述

apk的大体流程如下：

* **第一步：拷贝文件到指定的目录：**
   在Android系统中，apk安装文件是会被保存起来的，默认情况下，用户安装的apk首先会被拷贝到/data/app目录下，/data/app目录是用户有权限访问的目录，在安装apk的时候会自动选择该目录存放用户安装的文件，而系统出场的apk文件则被放到了/system分区下，包括/system/app，/system/vendor/app，以及/system/priv-app等等，该分区只有ROOT权限的用户才能访问，这也就是为什么在没有Root手机之前，我们没法删除系统出场的app的原因了。

* **第二步：解压缩apk，拷贝文件，创建应用的数据目录**
   为了加快app的启动速度，apk在安装的时候，会首先将app的可执行文件dex拷贝到/data/dalvik-cache目录，缓存起来。然后，在/data/data/目录下创建应用程序的数据目录(以应用的包名命名)，存放在应用的相关数据，如数据库、xml文件、cache、二进制的so动态库等。

* **第三步：解析apk的AndroidManifest.xml文件**

Android系统中，也有一个类似注册表的东西，用来记录当前所有安装的应用的基本信息，每次系统安装或者卸载了任何apk文件，都会更新这个文件。这个文件位于如下目录：/data/system/packages.xml。系统在安装这个apk的过程中，会解析apk的AndroidManifest.xml文件，提取出这个apk的重要信息写入到packages.xml文件中，这些信息包括：权限、应用包名、APK的安装位置、版本、userID等等。由此，我们就知道了为什么一些应用市场和软件管理类的app能够很清楚地知道当前手机所安装的所有app，以及这些app的详细信息了。另外一件事就是Linux的用户Id和用户组Id，以便他们可以获得合适的运行权限。以上都是由PackageServcieManager完成的。

* **第四步：显示快捷方式**
   如果这些应用程序在PackageManagerService服务注册好了，如果我们想要在Android桌面上看到这些应用程序，还需要有一个Home应用程序，负责从PackageManagerService服务中把这些安装好的应用程序取出来，并以友好的方式在桌面上展现出来，例如以快捷图标的形式。在Android系统中，负责把系统中已经安装的应用程序在桌面中展现出来的Home应用就是Launcher了。

## 五、PackageInstaller.apk与PackageManger

[PackageInstaller.apk地址](https://link.jianshu.com/?t=https://android.googlesource.com/platform/packages/apps/PackageInstaller/+/lollipop-release/)

![](https://upload-images.jianshu.io/upload_images/5713484-f55c1566163401fa.png)

### (一)、PackageInstaller概述

PackagInstaller是安卓上默认的应用程序，用它来安装普通文件。PackageInstaller提供了用户界面来管理应用或者包文件。PackageInstaller调用一个叫做InstallAppProgress的activity来获取用户发出的指令。InstallAppProgress会请求Package Manager服务，然后通过installed来安装包文件。

installed这个守护进程的首要角色就是获取来自Package Manager服务的请求，而该请求是通过Linux套接字/dev/socket/installed获得的。installed使用管理员权限执行一系列步骤来安装APK。

### (二)、PackageInstaller内容解析

PackageInstaller的结构如下:

![](https://upload-images.jianshu.io/upload_images/5713484-41152bac4ad453d5.png)

![](https://upload-images.jianshu.io/upload_images/5713484-019f4848c3a18636.png)

这里面重点介绍以下两个类

* [PackageInstallerActivity](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java)：主要是检查各种权限，展示被安装应用的相关信息，最后跳转到实际安装应用的InstallAppProgress

* [InstallAppProgress](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java)：也是进行了一系列的操作，最终把安装交给了PackageManager.installPackageWithVerificationAndEncryption(mPackageURI, observer, installFlags, installerPackageName, verificationParams, null);

## 六、普通的APK安装方式的界面

普通的APK安装方式 一般是经过下面的两个界面的

![](https://upload-images.jianshu.io/upload_images/5713484-c2a3b274d31ff077.png)

上面的两个界面分别是[PackageInstallerActivity](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java)和[InstallAppProgress](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java)

## 七、PackageInstallerActivity类的安装流程

### (一)、PackageInstallerActivity类

```php
/*
 * This activity is launched when a new application is installed via side loading
 * The package is first parsed and the user is notified of parse errors via a dialog.
 * If the package is successfully parsed, the user is notified to turn on the install unknown
 * applications setting. A memory check is made at this point and the user is notified of out
 * of memory conditions if any. If the package is already existing on the device,
 * a confirmation dialog (to replace the existing package) is presented to the user.
 * Based on the user response the package is then installed by launching InstallAppConfirm
 * sub activity. All state transitions are handled in this activity
 */
public class PackageInstallerActivity extends Activity implements OnCancelListener, OnClickListener {
   ...
   ...
}
```

我们是知道PackageInstallerActivity是一个Activity并且实现了OnCancelListener和OnClickListener接口，下面我们来看一下注释。

当通过渠道安装一个应用程序的时候，会启动这个Activity。如果在首次解析这个安装包的时候出现解析错误，会通过对话框的形式告诉用户。如果首次解析安装包的时候，成功解析了，则会通知用户去打开"安装未知应用程序设置"。在启动Activity的时候会进行内存检查，如果内存不足会通知用户。如果这个应用程序已经在这个设备安装过了，则会向用户弹出一个对话框询问用户是否"替换现有应用程序的安装包"。基于用户的回应，然后通过InstallAppConfirm的子Activity来安装应用程序。在这Activity中处理所有状态的转换。

### (二)、PackageInstallerActivity类的onCreate()方法

代码在[PackageInstallerActivity.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java) 439行

```kotlin
    PackageManager mPm;
    UserManager mUserManager;
    PackageInstaller mInstaller;
    PackageInfo mPkgInfo;
    ApplicationInfo mSourceInfo;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
         //第一步
        // 一个PackageManager对象，具体用来执行安装操作
        mPm = getPackageManager();
        // PackageInstaller对象，在该对象中包含了安装APK的基本信息
        mInstaller = mPm.getPackageInstaller();
        mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);

        // 第二步
        final Intent intent = getIntent();
        if (PackageInstaller.ACTION_CONFIRM_PERMISSIONS.equals(intent.getAction())) {
            final int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);
            final PackageInstaller.SessionInfo info = mInstaller.getSessionInfo(sessionId);
            if (info == null || !info.sealed || info.resolvedBaseCodePath == null) {
                Log.w(TAG, "Session " + mSessionId + " in funky state; ignoring");
                finish();
                return;
            }

            mSessionId = sessionId;
            mPackageURI = Uri.fromFile(new File(info.resolvedBaseCodePath));
            mOriginatingURI = null;
            mReferrerURI = null;
        } else {
            mSessionId = -1;
            mPackageURI = intent.getData();
            mOriginatingURI = intent.getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
            mReferrerURI = intent.getParcelableExtra(Intent.EXTRA_REFERRER);
        }

       // 第三步
        final boolean unknownSourcesAllowedByAdmin = isUnknownSourcesAllowedByAdmin();
        final boolean unknownSourcesAllowedByUser = isUnknownSourcesEnabled();

        boolean requestFromUnknownSource = isInstallRequestFromUnknownSource(intent);

        //第四步
        mInstallFlowAnalytics = new InstallFlowAnalytics();
        mInstallFlowAnalytics.setContext(this);
        mInstallFlowAnalytics.setStartTimestampMillis(SystemClock.elapsedRealtime());
        mInstallFlowAnalytics.setInstallsFromUnknownSourcesPermitted(unknownSourcesAllowedByAdmin
                && unknownSourcesAllowedByUser);
        mInstallFlowAnalytics.setInstallRequestFromUnknownSource(requestFromUnknownSource);
        mInstallFlowAnalytics.setVerifyAppsEnabled(isVerifyAppsEnabled());
        mInstallFlowAnalytics.setAppVerifierInstalled(isAppVerifierInstalled());
        mInstallFlowAnalytics.setPackageUri(mPackageURI.toString());

        //第五步
        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme) && !"package".equals(scheme)) {
            Log.w(TAG, "Unsupported scheme " + scheme);
            setPmResult(PackageManager.INSTALL_FAILED_INVALID_URI);
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_FAILED_UNSUPPORTED_SCHEME);
            finish();
            return;
        }

        final PackageUtil.AppSnippet as;
        //处理scheme为package的情况
        if ("package".equals(mPackageURI.getScheme())) {
            mInstallFlowAnalytics.setFileUri(false);
            try {
                //获取package对应的Android应用信息PackageInfo如果应用名称，权限列表等...
                mPkgInfo = mPm.getPackageInfo(mPackageURI.getSchemeSpecificPart(),
                        PackageManager.GET_PERMISSIONS | PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
            }
            //如果无法获取PackageInfo，弹出一个错误的对话框，然后直接退出安装
            if (mPkgInfo == null) {
                Log.w(TAG, "Requested package " + mPackageURI.getScheme()
                        + " not available. Discontinuing installation");
                showDialogInner(DLG_PACKAGE_ERROR);
                setPmResult(PackageManager.INSTALL_FAILED_INVALID_APK);
                mInstallFlowAnalytics.setPackageInfoObtained();
                mInstallFlowAnalytics.setFlowFinished(
                        InstallFlowAnalytics.RESULT_FAILED_PACKAGE_MISSING);
                return;
            }
            //创建AppSnipet对象，该对象封装了待安装Android应用的标题和图标
            as = new PackageUtil.AppSnippet(mPm.getApplicationLabel(mPkgInfo.applicationInfo),
                    mPm.getApplicationIcon(mPkgInfo.applicationInfo));
        } else {
           // 处理scheme为file的情况
            mInstallFlowAnalytics.setFileUri(true);
            // 获取APK文件的实际路径
            final File sourceFile = new File(mPackageURI.getPath());
            // 创建APK文件的分析器 parsed。同时分析安装包，后面会单独讲解
            PackageParser.Package parsed = PackageUtil.getPackageInfo(sourceFile);

            // Check for parse errors
            if (parsed == null) {
               //如果parsed == null，则说明解析出错，则弹出对话框，并退出安装
                Log.w(TAG, "Parse error when parsing manifest. Discontinuing installation");
                showDialogInner(DLG_PACKAGE_ERROR);
                setPmResult(PackageManager.INSTALL_FAILED_INVALID_APK);
                mInstallFlowAnalytics.setPackageInfoObtained();
                mInstallFlowAnalytics.setFlowFinished(
                        InstallFlowAnalytics.RESULT_FAILED_TO_GET_PACKAGE_INFO);
                return;
            }
            //解析没出错，生成PackageInfo，这里面包含APK文件的相关信息
            mPkgInfo = PackageParser.generatePackageInfo(parsed, null,
                    PackageManager.GET_PERMISSIONS, 0, 0, null,
                    new PackageUserState());
            //manifest校验
            mPkgDigest = parsed.manifestDigest;
            // 设置apk的程序名称和图标，这是另一种创建AppSnippet的方式
            as = PackageUtil.getAppSnippet(this, mPkgInfo.applicationInfo, sourceFile);
        }
        mInstallFlowAnalytics.setPackageInfoObtained();

        //set view
        // 第六步
        setContentView(R.layout.install_start);
        mInstallConfirm = findViewById(R.id.install_confirm_panel);
        mInstallConfirm.setVisibility(View.INVISIBLE);
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);

        mOriginatingUid = getOriginatingUid(intent);

         // 第七步
        // Block the install attempt on the Unknown Sources setting if necessary.
        // 如果必须要禁止来自未知来源的安装
        if (!requestFromUnknownSource) {
             //初始化操作
            initiateInstall();
            return;
        }

       // 第八步
        // If the admin prohibits it, or we're running in a managed profile, just show error
        // and exit. Otherwise show an option to take the user to Settings to change the setting.
        // 未知来源检查，如果admin禁止则直接提示错误退出。否则显示选项提示用户去设置修改上修改设置
        final boolean isManagedProfile = mUserManager.isManagedProfile();
        if (!unknownSourcesAllowedByAdmin
                || (!unknownSourcesAllowedByUser && isManagedProfile)) {
            showDialogInner(DLG_ADMIN_RESTRICTS_UNKNOWN_SOURCES);
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_BLOCKED_BY_UNKNOWN_SOURCES_SETTING);
        } else if (!unknownSourcesAllowedByUser) {
            // Ask user to enable setting first
            showDialogInner(DLG_UNKNOWN_SOURCES);
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_BLOCKED_BY_UNKNOWN_SOURCES_SETTING);
        } else {
            initiateInstall();
        }
    }
```

代码很多，那我们来看重点

* **第一步：** 给mPm、mInstaller、mUserManager进行初始化。

* **第二步：** 获取mSessionId、mPackageURI、mOriginatingURI、mReferrerURI 这四个是重要参数

* **第三步：** 判断是否是来自未知来源的包，看是否是非官网下载的app，这里面有三个判断依次为：
  * isUnknownSourcesAllowedByAdmin()：设备管理员是否限制来自未知来源的安装
  * isUnknownSourcesEnabled()：在“设置”中用户是否启用未知来源
  * isInstallRequestFromUnknownSource(Intent)：安装请求是否来自一个未知的源

* **第四步：** 创建mInstallFlowAnalytics对象，并进行一些字段的赋值[InstallFlowAnalytics.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallFlowAnalytics.java)是用来安装软件包的分析工具。

* **第五步：** 检查scheme是否支持，如果不支持则直接结束，如果支持scheme，这里面又分为两种情况
  * 处理scheme为package的情况
  * 处理scheme为file的情况
     无论是上面的哪种情况，都是要首先获取PackageInfo对象，如果scheme是package的情况下是直接调用PackageManager. getPackageInfo()方法获取的；如果scheme是file则是通过APK的实际路径即mPackageURI.getPath()来构造一个File，然后通过 PackageParser.generatePackageInfo()方法来获取的。然后创建[AppSnippet](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageUtil.java)对象，AppSnippet是PackageUtil的静态内部类，内部封装了icon和label。注意不同sheme

* **第六步：** 设置activity的主界面

* **第七步：** 判断是限制未知来源的安装包

* **第八步：** 进行未知来源的检查
   大家如果仔细阅读源码的话，就会知道，无论是否限制未知来源的安装包，如果没有问题都会调用initiateInstall();来进行初始化操作

**OK**，PackageInstallerActivity类的onCreate()方法分析完毕，为了让大家更好的理解，下面讲解下这里面涉及到几个核心方法:

- PackageUtil.getPackageInfo(sourceFile)
- initiateInstall()

#### 1、PackageUtil.getPackageInfo(sourceFile)方法解析

```java
    /**
     * Utility method to get package information for a given {@link File}
     */
    public static PackageParser.Package getPackageInfo(File sourceFile) {
        final PackageParser parser = new PackageParser();
        try {
            PackageParser.Package pkg = parser.parseMonolithicPackage(sourceFile, 0);
            parser.collectManifestDigest(pkg);
            return pkg;
        } catch (PackageParserException e) {
            return null;
        }
    }
```

我们看到这个方法里面主要就是new了一个PackageParser对象
 然后调用parser.parseMonolithicPackage(sourceFile, 0);方法和  parser.collectManifestDigest(pkg);方法，如果不抛异常就直接返回 PackageParser.Package对象，如果抛异常则直接返回null。看来上面这两个方法很重，关于parser.parseMonolithicPackage(sourceFile, 0);方法请参考APK安装流程详解9——PackageParser解析APK(上)中**五、PackageParse#parseMonolithicPackage(File, int)方法解析**（解析给定的 APK 文件，返回 Package 对象）。

#### 2、initiateInstall()方法解析

代码在[PackageInstallerActivity.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java) 400行‘

```tsx
    private void initiateInstall() {
        String pkgName = mPkgInfo.packageName;
        // Check if there is already a package on the device with this name
        // but it has been renamed to something else.
        //第一步
        String[] oldName = mPm.canonicalToCurrentPackageNames(new String[] { pkgName });
        if (oldName != null && oldName.length > 0 && oldName[0] != null) {
            pkgName = oldName[0];
            mPkgInfo.packageName = pkgName;
            mPkgInfo.applicationInfo.packageName = pkgName;
        }
        // Check if package is already installed. display confirmation dialog if replacing pkg
         // 第二步
        try {
            // This is a little convoluted because we want to get all uninstalled
            // apps, but this may include apps with just data, and if it is just
            // data we still want to count it as "installed".
            // 获取设备上的残存数据，并且标记为“installed”,实际上已经被卸载的应用
            mAppInfo = mPm.getApplicationInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if ((mAppInfo.flags&ApplicationInfo.FLAG_INSTALLED) == 0) {
如果应用是被卸载的，但是又是被标识成安装过的，则认为是新安装
                 // 如果应用是被卸载的，但是又是被标识成安装过的，则认为是新安装
                mAppInfo = null;
            }
        } catch (NameNotFoundException e) {
            mAppInfo = null;
        }

        mInstallFlowAnalytics.setReplace(mAppInfo != null);
        mInstallFlowAnalytics.setSystemApp(
                (mAppInfo != null) && ((mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0));
        // 第三步
        startInstallConfirm();
    }
```

initiateInstall()方法里面主要做了三件事

* **第一步**：检查设备是否有一个现在不同名，但是曾经是相同的包名，即是否是同名安装，如果是则后续是替换安装。

* **第二步**：检查设备上是否已经安装了这个安装包，如果是，后面是替换安装

* **第三步**：调用 startInstallConfirm() 这个方法是安装的核心代码。

下面我们就来看下startInstallConfirm()方法里面的具体实现

##### ①、startInstallConfirm()方法解析

代码在[PackageInstallerActivity.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java) 114行

```csharp
    private void startInstallConfirm() {
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        TabsAdapter adapter = new TabsAdapter(this, tabHost, viewPager);
        adapter.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (TAB_ID_ALL.equals(tabId)) {
                    mInstallFlowAnalytics.setAllPermissionsDisplayed(true);
                } else if (TAB_ID_NEW.equals(tabId)) {
                    mInstallFlowAnalytics.setNewPermissionsDisplayed(true);
                }
            }
        });
        // If the app supports runtime permissions the new permissions will
        // be requested at runtime, hence we do not show them at install.

         // 根据sdk版本来判断app是否支持运行时权限，这里会显示运行时权限
        boolean supportsRuntimePermissions = mPkgInfo.applicationInfo.targetSdkVersion
                >= Build.VERSION_CODES.M;

       //显示权限列表的变量，true显示权限列表，false 未显示权限列表
        boolean permVisible = false;
        mScrollView = null;
        mOkCanInstall = false;
        int msg = 0;
 //perms这个对象包括了该应用的用户的uid以及相应的一些权限，以及权限组信息
         // perms这个对象包括了该应用的用户的uid以及相应的一些权限，以及权限组的信息
        AppSecurityPermissions perms = new AppSecurityPermissions(this, mPkgInfo);
        // 获取隐私相关权限的数量
        final int N = perms.getPermissionCount(AppSecurityPermissions.WHICH_ALL);
        //判断是否为已经安装过的应用
        if (mAppInfo != null) {
           // 如果已经安装过则继续判断是否为系统应用
            msg = (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    ? R.string.install_confirm_question_update_system
                    : R.string.install_confirm_question_update;
            
            // 用来显示权限列表的scrollview
            mScrollView = new CaffeinatedScrollView(this);
            // 如果显示的内容超过了mScrollView,则就会折叠可以滚动 
            mScrollView.setFillViewport(true);
            boolean newPermissionsFound = false;
            if (!supportsRuntimePermissions) {
                //针对更新应用程序相对于旧版本而判断是否加入新的权限
                newPermissionsFound =
                        (perms.getPermissionCount(AppSecurityPermissions.WHICH_NEW) > 0);
                mInstallFlowAnalytics.setNewPermissionsFound(newPermissionsFound);
                if (newPermissionsFound) {
                     //将新的权限列表视频添加到滚动视图中
                    permVisible = true;
                    mScrollView.addView(perms.getPermissionsView(
                            AppSecurityPermissions.WHICH_NEW));
                }
            }
            if (!supportsRuntimePermissions && !newPermissionsFound) {
                // 没有设置任何权限，只显示应用程序名称和图标
                LayoutInflater inflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                TextView label = (TextView)inflater.inflate(R.layout.label, null);
                label.setText(R.string.no_new_perms);
                mScrollView.addView(label);
            }
            adapter.addTab(tabHost.newTabSpec(TAB_ID_NEW).setIndicator(
                    getText(R.string.newPerms)), mScrollView);
        } else  {
            // 应用没有被安装过，则将相应的控件隐藏
            findViewById(R.id.tabscontainer).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.VISIBLE);
        }
        // 如果至少设置了一个权限
        if (!supportsRuntimePermissions && N > 0) {
            permVisible = true;
            LayoutInflater inflater = (LayoutInflater)getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            //解析权限列表的视图
            View root = inflater.inflate(R.layout.permissions_list, null);
            if (mScrollView == null) {
                mScrollView = (CaffeinatedScrollView)root.findViewById(R.id.scrollview);
            }
           // 添加到权限列表的视图
            ((ViewGroup)root.findViewById(R.id.permission_list)).addView(
                       perms.getPermissionsView(AppSecurityPermissions.WHICH_ALL));
            adapter.addTab(tabHost.newTabSpec(TAB_ID_ALL).setIndicator(
                    getText(R.string.allPerms)), root);
        }
        mInstallFlowAnalytics.setPermissionsDisplayed(permVisible);
        
        if (!permVisible) {
            // 如果不显示权限列表
            if (mAppInfo != null) {
                // 如果是更新安装包，并且没有任何权限要求
                // This is an update to an application, but there are no
                // permissions at all.
                //判断是否是系统应用来设置布局文件 
                msg = (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                        ? R.string.install_confirm_question_update_system_no_perms
                        : R.string.install_confirm_question_update_no_perms;
            } else {
                // 是新安装的app并且没有权限列表
                // This is a new application with no permissions.
                msg = R.string.install_confirm_question_no_perms;
            }
            //设置相应的UI
            tabHost.setVisibility(View.GONE);
            mInstallFlowAnalytics.setAllPermissionsDisplayed(false);
            mInstallFlowAnalytics.setNewPermissionsDisplayed(false);
            findViewById(R.id.filler).setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.GONE);
            mScrollView = null;
        }
        if (msg != 0) {
            ((TextView)findViewById(R.id.install_confirm_question)).setText(msg);
        }
        mInstallConfirm.setVisibility(View.VISIBLE);
        //这个是关键的控件，即点击安装button
        mOk = (Button)findViewById(R.id.ok_button);
       //这个是关键的控件，即点击取消button
        mCancel = (Button)findViewById(R.id.cancel_button);
        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        if (mScrollView == null) {
            // There is nothing to scroll view, so the ok button is immediately
            // set to install.
            mOk.setText(R.string.install);
            mOkCanInstall = true;
        } else {
            mScrollView.setFullScrollAction(new Runnable() {
                @Override
                public void run() {
                    mOk.setText(R.string.install);
                    mOkCanInstall = true;
                }
            });
        }
    }
```

这个方法其实主要是根据不同的情况来设置相应的UI，主要是将安装包分为新安装和更新安装，在更新安装里面又分为系统应用和非系统应用，然后根据不同的情况来显示不同的UI，UI这块主要是通过getPermissionsView方法来获取不同的权限View。

PS:**AppSecurityPermissions.WHICH_NEW**：新加入的权限

这个重点说下mOk这个Button，因为后面咱们点击"安装"按钮的流程就是从这个按钮开始的。

### (三)、PackageInstallerActivity类中点击"安装"的流程详解

由于PackageInstallerActivity实现了OnClickListener接口，所以点击事件我们直接找onClick(View)方法即可

#### 1、onClick(View v)方法解析

代码在[PackageInstallerActivity.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java) 114行

```csharp
    public void onClick(View v) {
        if (v == mOk) {
            if (mOkCanInstall || mScrollView == null) {
                mInstallFlowAnalytics.setInstallButtonClicked();
                if (mSessionId != -1) {
                   //如果原来是确认权限请求则赋予安装权限则退出
                    mInstaller.setPermissionsResult(mSessionId, true);

                    // We're only confirming permissions, so we don't really know how the
                    // story ends; assume success.
                    mInstallFlowAnalytics.setFlowFinishedWithPackageManagerResult(
                            PackageManager.INSTALL_SUCCEEDED);
                    finish();
                } else {
                    startInstall();
                }
            } else {
                mScrollView.pageScroll(View.FOCUS_DOWN);
            }
        } else if(v == mCancel) {
            // Cancel and finish
            setResult(RESULT_CANCELED);
            if (mSessionId != -1) {
                mInstaller.setPermissionsResult(mSessionId, false);
            }
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_CANCELLED_BY_USER);
            finish();
        }
    }
```

我们重点看v == mOk的情况，里面其实就是做了两件事

- 1、判断是否可以安装，mScrollVieww是否为空
- 2、如果可以安装，那么调用startInstall()方法

那下面我们来看下startInstall()的具体实现

```java
    private void startInstall() {
        // Start subactivity to actually install the application
        Intent newIntent = new Intent();

         //带上安装包的applicationInfo
        newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO,
                mPkgInfo.applicationInfo);
    
        // 带上安装包的URI
        newIntent.setData(mPackageURI);

        //设置目标类
        newIntent.setClass(this, InstallAppProgress.class);
        
        //带上安装包的mPkgDigest
newIntent.putExtra(InstallAppProgress.EXTRA_MANIFEST_DIGEST, mPkgDigest);

         // 带上mInstallFlowAnalytics 
        newIntent.putExtra(
                InstallAppProgress.EXTRA_INSTALL_FLOW_ANALYTICS, mInstallFlowAnalytics);
        String installerPackageName = getIntent().getStringExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
        if (mOriginatingURI != null) {

            //带上安装包的mOriginatingURI
            newIntent.putExtra(Intent.EXTRA_ORIGINATING_URI, mOriginatingURI);
        }
        if (mReferrerURI != null) {
            //带上安装包的mReferrerURI
            newIntent.putExtra(Intent.EXTRA_REFERRER, mReferrerURI);
        }
        if (mOriginatingUid != VerificationParams.NO_UID) {
             //带上安装包的mOriginatingUid 这个uid不是安装应用的uid
            newIntent.putExtra(Intent.EXTRA_ORIGINATING_UID, mOriginatingUid);
        }
        if (installerPackageName != null) {
            newIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                    installerPackageName);
        }
        if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
            newIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        }
        if(localLOGV) Log.i(TAG, "downloaded app uri="+mPackageURI);
        startActivity(newIntent);
        finish();
    }
```

#### 2、startInstall()方法解析

代码在[PackageInstallerActivity.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/PackageInstallerActivity.java) 683行

```java
    private void startInstall() {
        // Start subactivity to actually install the application
        Intent newIntent = new Intent();

         //带上安装包的applicationInfo
        newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO,
                mPkgInfo.applicationInfo);
    
        // 带上安装包的URI
        newIntent.setData(mPackageURI);

        //设置目标类
        newIntent.setClass(this, InstallAppProgress.class);
        
        //带上安装包的mPkgDigest
newIntent.putExtra(InstallAppProgress.EXTRA_MANIFEST_DIGEST, mPkgDigest);

         // 带上mInstallFlowAnalytics 
        newIntent.putExtra(
                InstallAppProgress.EXTRA_INSTALL_FLOW_ANALYTICS, mInstallFlowAnalytics);
        String installerPackageName = getIntent().getStringExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
        if (mOriginatingURI != null) {

            //带上安装包的mOriginatingURI
            newIntent.putExtra(Intent.EXTRA_ORIGINATING_URI, mOriginatingURI);
        }
        if (mReferrerURI != null) {
            //带上安装包的mReferrerURI
            newIntent.putExtra(Intent.EXTRA_REFERRER, mReferrerURI);
        }
        if (mOriginatingUid != VerificationParams.NO_UID) {
             //带上安装包的mOriginatingUid 这个uid不是安装应用的uid
            newIntent.putExtra(Intent.EXTRA_ORIGINATING_UID, mOriginatingUid);
        }
        if (installerPackageName != null) {
            newIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                    installerPackageName);
        }
        if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
            newIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        }
        if(localLOGV) Log.i(TAG, "downloaded app uri="+mPackageURI);
        startActivity(newIntent);
        finish();
    }
```

上面代码很简单，主要是就是构造在一个Intent，并且传递必须要的数据，

可以看到在startInstall方法中，主要构造一个intent，并且将安装包信息封装到intent中，然后跳转到InstallAppProgress类。

下面我们就来看下InstallAppProgress这个类的安装流程。

## 八、InstallAppProgress类的安装流程

### (一)、InstallAppProgress类简介

```dart
/**
 * This activity corresponds to a download progress screen that is displayed
 * when the user tries
 * to install an application bundled as an apk file. The result of the application install
 * is indicated in the result code that gets set to the corresponding installation status
 * codes defined in PackageManager. If the package being installed already exists,
 * the existing package is replaced with the new one.
 */
public class InstallAppProgress extends Activity implements View.OnClickListener, OnCancelListener {
        ...
        ...
}
```

通过上面代码我们知道InstallAppProgress其实是一个Activity并且实现了OnClickListener和OnCancelListener接口。

为了让大家更好的理解这个类，我们还是看一下这个类的注释，翻译如下：

这个Activity是负责当用户尝试安装APK应用程序时的进度显示的Activity。关于这个应用程序的安装结果的状态码是和PackageManager里面定义的安装状态码一一映射的。如果正在安装的应用已经在设备上存在了，则新的应用程序将会替换掉老的应用程序。

既然它是一个Activity，那么我们先看下他的onCreate()方法。

### (二)、InstallAppProgress的onCreate(Bundle)方法

代码在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 167行

```java
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //第一步
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        mInstallFlowAnalytics = intent.getParcelableExtra(EXTRA_INSTALL_FLOW_ANALYTICS);
        mInstallFlowAnalytics.setContext(this);
        mPackageURI = intent.getData();
      
        //第二步
        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme) && !"package".equals(scheme)) {
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_FAILED_UNSUPPORTED_SCHEME);
            throw new IllegalArgumentException("unexpected scheme " + scheme);
        }
        
        // 第三步 
        initView();
    }
```

通过上面代码我们知道onCreate方法里面主要做了3三件事：

- **首先**：初始化数据，把Intent里面的数据取出
- **其次**：做scheme数据过滤，只支持scheme为file或者package格式的
- **最后**：最后调用initView()方法

#### 1、initView()方法详解

代码在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 226行

```csharp
    public void initView() {
         // 第一步
        setContentView(R.layout.op_progress);

         // 第二步
        // 安装模式 分为安装和更新
        int installFlags = 0;
        PackageManager pm = getPackageManager();

        // 第三步
        try {
            PackageInfo pi = pm.getPackageInfo(mAppInfo.packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if(pi != null) {
                installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
            }
        } catch (NameNotFoundException e) {
        }

         // 第四步
        if((installFlags & PackageManager.INSTALL_REPLACE_EXISTING )!= 0) {
            Log.w(TAG, "Replacing package:" + mAppInfo.packageName);
        }

        // 第五步
        final PackageUtil.AppSnippet as;
        if ("package".equals(mPackageURI.getScheme())) {
            as = new PackageUtil.AppSnippet(pm.getApplicationLabel(mAppInfo),
                    pm.getApplicationIcon(mAppInfo));
        } else {
            final File sourceFile = new File(mPackageURI.getPath());
            as = PackageUtil.getAppSnippet(this, mAppInfo, sourceFile);
        }

        // 第六步
        mLabel = as.label;
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.installing);
        mExplanationTextView = (TextView) findViewById(R.id.center_explanation);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        // Hide button till progress is being displayed
        mOkPanel = (View)findViewById(R.id.buttons_panel);
        mDoneButton = (Button)findViewById(R.id.done_button);
        mLaunchButton = (Button)findViewById(R.id.launch_button);
        mOkPanel.setVisibility(View.INVISIBLE);

        // 第七步
        String installerPackageName = getIntent().getStringExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
        Uri originatingURI = getIntent().getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
        Uri referrer = getIntent().getParcelableExtra(Intent.EXTRA_REFERRER);
        int originatingUid = getIntent().getIntExtra(Intent.EXTRA_ORIGINATING_UID,
                VerificationParams.NO_UID);
        ManifestDigest manifestDigest = getIntent().getParcelableExtra(EXTRA_MANIFEST_DIGEST);
        VerificationParams verificationParams = new VerificationParams(null, originatingURI,
                referrer, originatingUid, manifestDigest);

        // 第八步
        PackageInstallObserver observer = new PackageInstallObserver();

        // 第九步
        if ("package".equals(mPackageURI.getScheme())) {
            try {
                pm.installExistingPackage(mAppInfo.packageName);
                observer.packageInstalled(mAppInfo.packageName,
                        PackageManager.INSTALL_SUCCEEDED);
            } catch (PackageManager.NameNotFoundException e) {
                observer.packageInstalled(mAppInfo.packageName,
                        PackageManager.INSTALL_FAILED_INVALID_APK);
            }
        } else {
            pm.installPackageWithVerificationAndEncryption(mPackageURI, observer, installFlags,
                    installerPackageName, verificationParams, null);
        }
    }
```

上面代码还是比较简洁的，大体上将initView()分为九个步骤，如下：

* **第一步**：设置当前Activity的布局文件

* **第二步**：获取PackageManager对象

* **第三步**：获取PackgeInfo对象，这里使用pm.getPackageInfo()方法来获取，主要是判断待安装的应用程序是否已经安装，因为如果已经安装了，则返回PackgeInfo对象，则安装模式设为更新模式，如果没有安装，则返回null

* **第四步**：如果是替换安装则打印日志

* **第五步**：根据不同的scheme来给AppSnippet进行赋值，如果scheme为package则意味着更新应用程序；scheme为file则意味着是新安装应用程序。

* **第六步**：获取布局文件中的控件

* **第七步**：从Intent中获取相应的数据信息，为下一步做准备

* **第八步**：创建安装的监听器对象

* **第九步**：根据不用的scheme来进行不同安装模式下的安装操作

这个方法里面涉及到三个重要内容如下：

* 1、PackageInstallObserver

* 2、PackageManager的installExistingPackage(String)方法

* 3、PackageManager的installPackageWithVerificationAndEncryption(PackageInstallObserver , int , String ,VerificationParams , ContainerEncryptionParams )方法

##### (1)、PackageInstallObserver类详解

###### (1.1)、PackageInstallObserver类源码

代码在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 218行

```java
    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
            msg.arg1 = returnCode;
            mHandler.sendMessage(msg);
        }
    }
```

通过上面代码我们知道
 PackageInstallObserver其实是继承IPackageInstallObserver.Stub 类的，在packageInstalled(String, int)方法里面其实向mHandler发送了一个Message。

PS：**注意这个Message的what值是INSTALL_COMPLETE，Message的arg1是安装结果的code。**

那我们看下这个mHandler的里面是怎么操作的

###### (1.2)、InstallAppProgress类中的Handler对象mHandler解析

代码在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 76行

```csharp
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
                     //记录安装结果
                    mInstallFlowAnalytics.setFlowFinishedWithPackageManagerResult(msg.arg1);
                    
                    // 第一步
                    // 判断是否需要安装结束后立即结束当前界面
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, msg.arg1);
                        setResult(msg.arg1 == PackageManager.INSTALL_SUCCEEDED
                                ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                                        result);
                        finish();
                        return;
                    }
                     
                    // 第二步
                    // Update the status text
                    mProgressBar.setVisibility(View.INVISIBLE);
                    // Show the ok button
                    int centerTextLabel;
                    int centerExplanationLabel = -1;
                    LevelListDrawable centerTextDrawable =
                            (LevelListDrawable) getDrawable(R.drawable.ic_result_status);

                     // 第三步 
                    if (msg.arg1 == PackageManager.INSTALL_SUCCEEDED) {
                        // 安装成功
                        mLaunchButton.setVisibility(View.VISIBLE);
                        centerTextDrawable.setLevel(0);
                        centerTextLabel = R.string.install_done;
                        // Enable or disable launch button
                        // 获取应用程序启动的Intent
                        mLaunchIntent = getPackageManager().getLaunchIntentForPackage(
                                mAppInfo.packageName);
                        boolean enabled = false;
                         // 判断应用程序启动Intent是否可用
                        if(mLaunchIntent != null) {
                            List<ResolveInfo> list = getPackageManager().
                                    queryIntentActivities(mLaunchIntent, 0);
                            if (list != null && list.size() > 0) {
                                enabled = true;
                            }
                        }
                        if (enabled) {
                            mLaunchButton.setOnClickListener(InstallAppProgress.this);
                        } else {
                            mLaunchButton.setEnabled(false);
                        }
                    } else if (msg.arg1 == PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE){
                        // 由于剩余空间不足导致安装失败
                        showDialogInner(DLG_OUT_OF_SPACE);
                        return;
                    } else {
                        // 安装失败
                        // Generic error handling for all other error codes.
                        centerTextDrawable.setLevel(1);
                        centerExplanationLabel = getExplanationFromErrorCode(msg.arg1);
                        centerTextLabel = R.string.install_failed;
                        mLaunchButton.setVisibility(View.INVISIBLE);
                    }

                    // 第四步
                    if (centerTextDrawable != null) {
                    centerTextDrawable.setBounds(0, 0,
                            centerTextDrawable.getIntrinsicWidth(),
                            centerTextDrawable.getIntrinsicHeight());
                        mStatusTextView.setCompoundDrawablesRelative(centerTextDrawable, null,
                                null, null);
                    }
                    mStatusTextView.setText(centerTextLabel);
                    if (centerExplanationLabel != -1) {
                        mExplanationTextView.setText(centerExplanationLabel);
                        mExplanationTextView.setVisibility(View.VISIBLE);
                    } else {
                        mExplanationTextView.setVisibility(View.GONE);
                    }
                    mDoneButton.setOnClickListener(InstallAppProgress.this);
                    mOkPanel.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
```

由于Message的what值为INSTALL_COMPLETE，我们只需要关心case值为INSTALL_COMPLETE的情况(不过貌似没有别的case)，case的值为INSTALL_COMPLETE的内容主要分为四个步骤，如下：

* **第一步**：判断是否有安装结束后(不论成功或者失败)，立即离开当前的需求，如果有这个要求，则完成结束后，立即返回，如果是安装成功则resultCode为PackageManager.INSTALL_SUCCEEDED，如果失败resultCode为Activity.RESULT_FIRST_USER。

* **第二步**：更变UI，并且给centerTextDrawable赋值

* **第三步**：根据安装结果的code(是msg.arg1)来更新UI及后续的操作，这里面分为三种情况：
  * 安装结果code为PackageManager.INSTALL_SUCCEEDED：
     表示安装成功，首先进行UI更新，然后通过调用getPackageManager().getLaunchIntentForPackage(mAppInfo.packageName);来获取这个应用程序的启动Intent，接着判断这个Intent是否可能用，如果可用最后设置mLaunchButton的监听事件。
  * 安装结果code为PackageManager. INSTALL_FAILED_INSUFFICIENT_STORAGE：
     表示由于设备没有足够的存储空间来安装该应用程序
  * 安装结果code为其他值：
     如果不是上面两个code值则表示安装失败，通过调用getExplanationFromErrorCode(int) 方法来获取失败的原因文案提示并更新UI

* **第四步**：根据上面的安装结果，更新UI。

这里面涉及到两个重要方法：

* 1、getExplanationFromErrorCode(int)方法

* 2、getPackageManager().getLaunchIntentForPackage(mAppInfo.packageName)方法

下面我们就简单介绍这两个方法：

###### (1.3)、 getExplanationFromErrorCode(int)方法

代码在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 76行

```cpp
    private int getExplanationFromErrorCode(int errCode) {
        Log.d(TAG, "Installation error code: " + errCode);
        switch (errCode) {
            case PackageManager.INSTALL_FAILED_INVALID_APK:
                return R.string.install_failed_invalid_apk;
            case PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES:
                return R.string.install_failed_inconsistent_certificates;
            case PackageManager.INSTALL_FAILED_OLDER_SDK:
                return R.string.install_failed_older_sdk;
            case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:
                return R.string.install_failed_cpu_abi_incompatible;
            default:
                return -1;
        }
    }
```

通过上面代码我知道getExplanationFromErrorCode(int) 主要是根据不同的code返回不同的String用以提醒用户。那我们就把上面四个case依次说下：

* **PackageManager.INSTALL_FAILED_INVALID_APK：**他表示无效的APK文件，一般是APK文件有问题

* **PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES：**表示签名问题，一般是签名冲突

* **PackageManager.INSTALL_FAILED_OLDER_SDK：**表示SDK版本和APK的要求的版本冲突

* **PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE：**由于native的代码与设备上的CPU_ABI不兼容

##### (2)、PackageManager的installExistingPackage(String)方法简介

代码在[PackageManager.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageManager.java) 3755行

```dart
    /**
     * If there is already an application with the given package name installed
     * on the system for other users, also install it for the calling user.
     * @hide
     */
    // @SystemApi
    public abstract int installExistingPackage(String packageName)
            throws NameNotFoundException;
```

咦，它也是一个抽象方法啊？先看下注释：

如果系统上已经有其他用户安装了相同包名的应用程序，则让用户继续安装。

##### (3)、PackageManager的installPackageWithVerificationAndEncryption(PackageInstallObserver , int , String ,VerificationParams , ContainerEncryptionParams )方法类简介

代码在[PackageManager.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageManager.java) 3660行

```dart
    /**
     * Similar to
     * {@link #installPackage(Uri, IPackageInstallObserver, int, String)} but
     * with an extra verification file provided.
     *
     * @param packageURI The location of the package file to install. This can
     *            be a 'file:' or a 'content:' URI.
     * @param observer An observer callback to get notified when the package installation is
     * complete. {@link PackageInstallObserver#packageInstalled(String, Bundle, int)} will be
     * called when that happens. This parameter must not be null.
     * @param flags - possible values: {@link #INSTALL_FORWARD_LOCK},
     *            {@link #INSTALL_REPLACE_EXISTING}, {@link #INSTALL_ALLOW_TEST}.
     * @param installerPackageName Optional package name of the application that
     *            is performing the installation. This identifies which market
     *            the package came from.
     * @param verificationURI The location of the supplementary verification
     *            file. This can be a 'file:' or a 'content:' URI. May be
     *            {@code null}.
     * @param manifestDigest an object that holds the digest of the package
     *            which can be used to verify ownership. May be {@code null}.
     * @param encryptionParams if the package to be installed is encrypted,
     *            these parameters describing the encryption and authentication
     *            used. May be {@code null}.
     * @hide
     */
    public abstract void installPackageWithVerification(Uri packageURI,
            PackageInstallObserver observer, int flags, String installerPackageName,
            Uri verificationURI, ManifestDigest manifestDigest,
            ContainerEncryptionParams encryptionParams);
```

既然它也是一个抽象方法，那我们先来看下注释：

和installPackage(Uri, IPackageInstallObserver, int, String)有点类似，但是多了一个额外的验证文件。

这里先简单的说下我们上面涉及到监听器PackageInstallObserver，因为PackageInstallObserver还是蛮重要的。

### (三)、PackageInstallObserver简介

#### 1、PackageInstallObserver

[PackageInstallObserver.java源码地址](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/PackageInstallObserver.java)
代码如下：

```java
public class PackageInstallObserver {
    private final IPackageInstallObserver2.Stub mBinder = new IPackageInstallObserver2.Stub() {
        @Override
        public void onUserActionRequired(Intent intent) {
            PackageInstallObserver.this.onUserActionRequired(intent);
        }

        @Override
        public void onPackageInstalled(String basePackageName, int returnCode,
                String msg, Bundle extras) {
            PackageInstallObserver.this.onPackageInstalled(basePackageName, returnCode, msg,
                    extras);
        }
    };

    /** {@hide} */
    public IPackageInstallObserver2 getBinder() {
        return mBinder;
    }

    public void onUserActionRequired(Intent intent) {
    }

    /**
     * This method will be called to report the result of the package
     * installation attempt.
     *
     * @param basePackageName Name of the package whose installation was
     *            attempted
     * @param extras If non-null, this Bundle contains extras providing
     *            additional information about an install failure. See
     *            {@link android.content.pm.PackageManager} for documentation
     *            about which extras apply to various failures; in particular
     *            the strings named EXTRA_FAILURE_*.
     * @param returnCode The numeric success or failure code indicating the
     *            basic outcome
     * @hide
     */
    public void onPackageInstalled(String basePackageName, int returnCode, String msg,
            Bundle extras) {
    }
}
```

我们看到PackageInstallObserver的getBinder()方法返回的是本地成员变量mBinder，而mBinder又是IPackageInstallObserver2.Stub。所以我们可以说mBinder也是一个AIDL的通信的服务端。

我们看来下onPackageInstalled(String,int,String,Bundle)方法的注释：

调用这个方法来获取程序安装的结果

- 入参 basePackageName：表示安装包的包名
- 入参 extras，可能为空，如果非空，则在里面包含导致安装失败的附加信息。可以参考android.content.pm.PackageManage类中以EXTRA_FAILURE_开头的字段
- 入参 returnCode：表示安装成功或者失败的状态码

这时候就要看一下IPackageInstallObserver2.aidl

#### 2、IPackageInstallObserver2.aidl简介

[IPackageInstallObserver2.aidl源码地址](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/IPackageInstallObserver2.aidl)

```dart
/**
 * API for installation callbacks from the Package Manager.  In certain result cases
 * additional information will be provided.
 * @hide
 */
oneway interface IPackageInstallObserver2 {
    void onUserActionRequired(in Intent intent);

    /**
     * The install operation has completed.  {@code returnCode} holds a numeric code
     * indicating success or failure.  In certain cases the {@code extras} Bundle will
     * contain additional details:
     *
     * <p><table>
     * <tr>
     *   <td>INSTALL_FAILED_DUPLICATE_PERMISSION</td>
     *   <td>Two strings are provided in the extras bundle: EXTRA_EXISTING_PERMISSION
     *       is the name of the permission that the app is attempting to define, and
     *       EXTRA_EXISTING_PACKAGE is the package name of the app which has already
     *       defined the permission.</td>
     * </tr>
     * </table>
     */
    void onPackageInstalled(String basePackageName, int returnCode, String msg, in Bundle extras);
}
```

##### (1)、"oneway"关键字

首先说下AIDL的关键字 "oneway"，AIDL可以用关键字"oneway"来表明远程调用的行为属性，使用了该关键字，那么远程调用将紧紧是调用所有的数据传输过来并立即返回，而不会等待结果的返回，也是说不会阻塞远程线程的运行。AIDL接口将最终获得一个从Binder线程池中产生的调用(和普通的远程调用类似)。如果关键字oneway在本地调用中被使用，将不会对函数调用有任何影响。

##### (2)、理解"注释"

为了更好的理解设计者最初的设想，我们来看下"类"的注释

包管理其用于安装的的回调API。在某些情况下，它可以提供一些必要的信息。

那我们来看下onPackageInstalled的注释：

安装操作完成后，会包含一个code，由这个code标识成功或者失败。在特定的情形下它也会包含一些附加信息：
 比如INSTALL_FAILED_DUPLICATE_PERMISSION这个安装失败：
 它的extras 的Bundle 里面可能会存在EXTRA_EXISTING_PERMISSION这个字符串，表示定义app中定义权限的内容，和EXTRA_EXISTING_PACKAGE这个字符串，表示应定义了权限的应用程序包名称。

##### (3)、为什么要设计成AIDL

因为你在InstallAppProgress是一个单独的进程，而PackageManagerService也是一个单独的进程，假设InstallAppProgress所在的进程名称为"InstallAppProgress"，你想在"InstallAppProgress"监听"PackageManagerService"进程的结果，这里面涉及到两个进程的调用，是属于跨进程调用，所以需要使用Binder来进行进程间通信。

### (四)、总结

PackageInstallObserver类内部含有一个AIDL的Binder跨进程通信，当在PackageManagerService中安装完成，会调用IPackageInstallObserver2.Stub的onPackageInstalled方法，然后在IPackageInstallObserver2.Stub 的onPackageInstalled方法里面调用PackageInstallObserver的onPackageInstalled。这样就通知到了InstallAppProgress的observer对象了

## 九、InstallAppProgress中涉及到PackageManager的三个方法

在[InstallAppProgress.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/packages/apps/PackageInstaller/src/com/android/packageinstaller/InstallAppProgress.java) 中的

大体流程如下，如果已经存在安装包了，则更新(调用PackageManager的installExistingPackage(String)方法)，如果没有新安装，则进行安装(PackageManager的installPackageWithVerificationAndEncryption方法)，最后无论是更新还是新安装成功都会调用PackageManager的getLaunchIntentForPackage方法。

## 参考文章

1. [APK安装流程详解11——普通应用安装简介](https://www.jianshu.com/p/cbf8e73f41ed)

