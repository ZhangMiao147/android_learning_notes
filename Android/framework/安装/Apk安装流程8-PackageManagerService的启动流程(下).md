# Apk 安装流程8-PackageManagerService 的启动流程（下）

本片文章的主要内容如下：

```
5、PackageManagerService#scanPackageLI(File , int , int ,long , UserHandle )方法解析(首参数为File)
6、PackageManagerService#scanPackageLI(PackageParser.Package, int, int, long, UserHandle)方法解析(首参数为Package)
7、PackageManagerService#scanPackageDirtyLI(PackageParser.Package,int, int, long, UserHandle) 方法解析
8、总结
```

## 五、PackageManagerService#scanPackageLI方法(首参数为File)

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 5735行

```java
    /*
     *  Scan a package and return the newly parsed package.
     *  Returns null in case of errors and the error code is stored in mLastScanError
     */
    private PackageParser.Package scanPackageLI(File scanFile, int parseFlags, int scanFlags,
            long currentTime, UserHandle user) throws PackageManagerException {
         // ************** 第一步************
        
        if (DEBUG_INSTALL) Slog.d(TAG, "Parsing: " + scanFile);
        parseFlags |= mDefParseFlags;

        // 初始化PackageParse对象，用于解析包
        PackageParser pp = new PackageParser();
        // 设置PackageParse的三个属性
        pp.setSeparateProcesses(mSeparateProcesses);
        pp.setOnlyCoreApps(mOnlyCore);
        pp.setDisplayMetrics(mMetrics);

        // 判断扫描模式
        if ((scanFlags & SCAN_TRUSTED_OVERLAY) != 0) {
            parseFlags |= PackageParser.PARSE_TRUSTED_OVERLAY;
        }

        // 解析APK获取对应PackageParser.Package对象 pkg
        final PackageParser.Package pkg;
        try {
            //真正的解析
            // ************** 第二步************
            pkg = pp.parsePackage(scanFile, parseFlags);
        } catch (PackageParserException e) {
            throw PackageManagerException.from(e);
        }

       // ************** 第三步************
        PackageSetting ps = null;
        PackageSetting updatedPkg;
        // reader
        // 判断系统APP是否需要更新
        synchronized (mPackages) {
            // Look to see if we already know about this package.
            // 查看是否已经有该安装包，通过mSetting查找
            String oldName = mSettings.mRenamedPackages.get(pkg.packageName);
             // 如果存在同一个包名的老的安装包，且已经改回原始名称了。
            if (pkg.mOriginalPackages != null && pkg.mOriginalPackages.contains(oldName)) {
                // This package has been renamed to its original name.  Let's
                // use that.
                ps = mSettings.peekPackageLPr(oldName);
            }
            // If there was no original package, see one for the real package name.
            // 如果没有原始包，则使用真实包名
            if (ps == null) {
                ps = mSettings.peekPackageLPr(pkg.packageName);
            }
            // Check to see if this package could be hiding/updating a system
            // package.  Must look for it either under the original or real
            // package name depending on our state.
            // 检查这个包是否是一个 隐藏或者可以更新的 系统包
            updatedPkg = mSettings.getDisabledSystemPkgLPr(ps != null ? ps.name : pkg.packageName);
            if (DEBUG_INSTALL && updatedPkg != null) Slog.d(TAG, "updatedPkg = " + updatedPkg);
        }
        boolean updatedPkgBetter = false;
        // 首先检查是否是需要更新的系统应用
        // First check if this is a system package that may involve an update
        if (updatedPkg != null && (parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0) {
            // If new package is not located in "/system/priv-app" (e.g. due to an OTA),
            // it needs to drop FLAG_PRIVILEGED.
             // 如果新的安装包不位于"/system/pri-app"(例如由于OTA)，则需要删除FLAG_PRIVILEGED标志
            if (locationIsPrivileged(scanFile)) {
                updatedPkg.pkgPrivateFlags |= ApplicationInfo.PRIVATE_FLAG_PRIVILEGED;
            } else {
                updatedPkg.pkgPrivateFlags &= ~ApplicationInfo.PRIVATE_FLAG_PRIVILEGED;
            }

            if (ps != null && !ps.codePath.equals(scanFile)) {
                // The path has changed from what was last scanned...  check the
                // version of the new path against what we have stored to determine
                // what to do.
                //  如果路径和上次扫描的发生变化了，根据我们存储的路径检查新路径的版本。
                if (DEBUG_INSTALL) Slog.d(TAG, "Path changing from " + ps.codePath);
                if (pkg.mVersionCode <= ps.versionCode) {
                    // 如果系统包已经是最新版本了。所以最终应该是终止安装
                    // The system package has been updated and the code path does not match
                    // Ignore entry. Skip it.
                    if (DEBUG_INSTALL) Slog.i(TAG, "Package " + ps.name + " at " + scanFile
                            + " ignored: updated version " + ps.versionCode
                            + " better than this " + pkg.mVersionCode);
                    if (!updatedPkg.codePath.equals(scanFile)) {
                        // 如果安装路径还不一致，则进行相应设置
                        Slog.w(PackageManagerService.TAG, "Code path for hidden system pkg : "
                                + ps.name + " changing from " + updatedPkg.codePathString
                                + " to " + scanFile);
                        updatedPkg.codePath = scanFile;
                        updatedPkg.codePathString = scanFile.toString();
                        updatedPkg.resourcePath = scanFile;
                        updatedPkg.resourcePathString = scanFile.toString();
                    }
                    updatedPkg.pkg = pkg;
                    throw new PackageManagerException(INSTALL_FAILED_DUPLICATE_PACKAGE,
                            "Package " + ps.name + " at " + scanFile
                                    + " ignored: updated version " + ps.versionCode
                                    + " better than this " + pkg.mVersionCode);
                } else {
                    // The current app on the system partition is better than
                    // what we have updated to on the data partition; switch
                    // back to the system partition version.
                    // At this point, its safely assumed that package installation for
                    // apps in system partition will go through. If not there won't be a working
                    // version of the app
                    // writer
                    // 更新安装包到系统分区中
                    synchronized (mPackages) {
                        // Just remove the loaded entries from package lists.
                        // 从PackageManagerService的安装包列表中删除该包
                        mPackages.remove(ps.name);
                    }

                    logCriticalInfo(Log.WARN, "Package " + ps.name + " at " + scanFile
                            + " reverting from " + ps.codePathString
                            + ": new version " + pkg.mVersionCode
                            + " better than installed " + ps.versionCode);
                    // 创建安装参数InstallArgs
                    InstallArgs args = createInstallArgsForExisting(packageFlagsToInstallFlags(ps),
                            ps.codePathString, ps.resourcePathString, getAppDexInstructionSets(ps));
                    synchronized (mInstallLock) {
                        // 清空dex文件及安装包的挂载点
                        args.cleanUpResourcesLI();
                    }
                    synchronized (mPackages) {
                        mSettings.enableSystemPackageLPw(ps.name);
                    }
                    updatedPkgBetter = true;
                }
            }
        }

        if (updatedPkg != null) {
            // An updated system app will not have the PARSE_IS_SYSTEM flag set
            // initially
            // 更新的系统应用程序最初不会设置PARSE_IS_SYSTEM的flag
            parseFlags |= PackageParser.PARSE_IS_SYSTEM;

            // An updated privileged app will not have the PARSE_IS_PRIVILEGED
            // flag set initially
            // 已经更新的应用不设置PARSE_IS_PRIVILEGED的flag
            if ((updatedPkg.pkgPrivateFlags & ApplicationInfo.PRIVATE_FLAG_PRIVILEGED) != 0) {
                parseFlags |= PackageParser.PARSE_IS_PRIVILEGED;
            }
        }

        // ************** 第四步************
        // Verify certificates against what was last scanned
        // 安装包校验
        collectCertificatesLI(pp, ps, pkg, scanFile, parseFlags);

        /*
         * A new system app appeared, but we already had a non-system one of the
         * same name installed earlier.
         */

        // ************** 第五步************
        //我们安装一个系统APP的时候，发现已经有了一个相同包名的APP，而且这个相同包名APP是在非系统的分区中
        boolean shouldHideSystemApp = false;
        if (updatedPkg == null && ps != null
                && (parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) != 0 && !isSystemApp(ps)) {
            /*
             * Check to make sure the signatures match first. If they don't,
             * wipe the installed application and its data.
             */
            if (compareSignatures(ps.signatures.mSignatures, pkg.mSignatures)
                    != PackageManager.SIGNATURE_MATCH) {
                // 如果两个APK 签名不匹配，则调用deletePackageLI方法清除APK文件及其数据
                logCriticalInfo(Log.WARN, "Package " + ps.name + " appeared on system, but"
                        + " signatures don't match existing userdata copy; removing");
                deletePackageLI(pkg.packageName, null, true, null, null, 0, null, false);
                ps = null;
            } else {
                // 如果签名匹配了
                /*
                 * If the newly-added system app is an older version than the
                 * already installed version, hide it. It will be scanned later
                 * and re-added like an update.
                 */
                if (pkg.mVersionCode <= ps.versionCode) {
                    // 如果新安装的系统APP的版本号比之前已安装的版本号还低，
                    // 则说明当前已安装的APP是较新的，则将新安装的系统APP隐藏
                    shouldHideSystemApp = true;
                    logCriticalInfo(Log.INFO, "Package " + ps.name + " appeared at " + scanFile
                            + " but new version " + pkg.mVersionCode + " better than installed "
                            + ps.versionCode + "; hiding system");
                } else {
                    // 如果新安装的系统APP的版本号比当前已经安装的版本号要高，所以要要删之前已安装的APP，并安装新的系统APP
                    /*
                     * The newly found system app is a newer version that the
                     * one previously installed. Simply remove the
                     * already-installed application and replace it with our own
                     * while keeping the application data.
                     */
                    logCriticalInfo(Log.WARN, "Package " + ps.name + " at " + scanFile
                            + " reverting from " + ps.codePathString + ": new version "
                            + pkg.mVersionCode + " better than installed " + ps.versionCode);
                    // 更新系统APK程序
                    InstallArgs args = createInstallArgsForExisting(packageFlagsToInstallFlags(ps),
                            ps.codePathString, ps.resourcePathString, getAppDexInstructionSets(ps));
                    synchronized (mInstallLock) {
                        args.cleanUpResourcesLI();
                    }
                }
            }
        }

        // The apk is forward locked (not public) if its code and resources
        // are kept in different files. (except for app in either system or
        // vendor path).
        // TODO grab this value from PackageSettings
        // 如果 其代码和资源保存在不同的文件中，则该APK是前向锁定(非公开)
        if ((parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) == 0) {
            if (ps != null && !ps.codePath.equals(ps.resourcePath)) {
                parseFlags |= PackageParser.PARSE_FORWARD_LOCK;
            }
        }

        // TODO: extend to support forward-locked splits
        String resourcePath = null;
        String baseResourcePath = null;
         
        // 设置resourcePath和baseResourcePath的值
        if ((parseFlags & PackageParser.PARSE_FORWARD_LOCK) != 0 && !updatedPkgBetter) {
            if (ps != null && ps.resourcePathString != null) {
                resourcePath = ps.resourcePathString;
                baseResourcePath = ps.resourcePathString;
            } else {
                // Should not happen at all. Just log an error.
                Slog.e(TAG, "Resource path not set for pkg : " + pkg.packageName);
            }
        } else {
            resourcePath = pkg.codePath;
            baseResourcePath = pkg.baseCodePath;
        }

        // Set application objects path explicitly.
        pkg.applicationInfo.volumeUuid = pkg.volumeUuid;
        pkg.applicationInfo.setCodePath(pkg.codePath);
        pkg.applicationInfo.setBaseCodePath(pkg.baseCodePath);
        pkg.applicationInfo.setSplitCodePaths(pkg.splitCodePaths);
        pkg.applicationInfo.setResourcePath(resourcePath);
        pkg.applicationInfo.setBaseResourcePath(baseResourcePath);
        pkg.applicationInfo.setSplitResourcePaths(pkg.splitCodePaths);

        // Note that we invoke the following method only if we are about to unpack an application
        // 调用另外一个scanPackageLI()方法，对包进行扫描
        // ************** 第六步************
        PackageParser.Package scannedPkg = scanPackageLI(pkg, parseFlags, scanFlags
                | SCAN_UPDATE_SIGNATURE, currentTime, user);

        /*
         * If the system app should be overridden by a previously installed
         * data, hide the system app now and let the /data/app scan pick it up
         * again.
         */
         // 如果新安装的系统APP 会被旧的APP数据覆盖，所以需要隐藏隐藏系统应用程序。并重新扫描/data/app目录
        if (shouldHideSystemApp) {
            synchronized (mPackages) {
                mSettings.disableSystemPackageLPw(pkg.packageName);
            }
        }
        return scannedPkg;
    }
```

有方法注释，我们先简单翻译下注释：

扫描包并返回解析后获得包，如果发生了错误，则返回null。错误码保存在mLastScanError中。

我们把上面整体流程梳理下：

* 第一步：初始化包解析器PackageParse，关于PackageParse请参考APK安装流程详解9——PackageParser解析APK(上)中***一 、PackageParser类简介\***

* 第二步：用包解析器PackageParse对APK文件进行解析，最终得到一个内存中的"包"的数据结构PackageParser.Package的对象pkg。一个APK包的所有信息都在这个pkg对象中。具体请参考APK安装流程详解9——PackageParser解析APK(上)中***四、PackageParse#parsePackage(File, int)方法解析\***

* 第三步：判断系统APP是否需要更新：怎么判断系统应用是否需要升级？所以我们就要获取同一个安装包的的历史信息，我们可以通过PackageManagerService中Setting来获取保存在PackageManagerService中的的APK信息。即ps对象。而pkg对象则是当前扫描的APK的信息。通过对比两个变量。我们就能知道当前扫描的APK与已经安装的历史APK的差异。如果当前扫描的系统APK版本比已经安装的系统APK版本要低，则中断扫描过程，直接抛出异常。如果当前扫描的系统APK版本比已经安装的系统APK版本要高，则需要重新将系统APK设置为Enable状态。(PS：系统应用升级后会安装在data分区，之前的system分区的应用会被标记为Disable状态)。

* 第四步：之前在构建Package对象时，还没有APK的签名信息，现在正是要把APK签名信息填进去的时候，因为到这一步已经确认要安装APK了，APK能安装的前提就是一定要有签名信息。如果对已有APK进行升级，则签名必须与已有APK相匹配。PacakgeManagerService.collectCertificatesLI()方法就是从APK包中的META-INF目录中读取签名信息。这个方法后续在讲解签名时单独讲解。

* 第五步：处理系统APK已经被安装过的场景。已经被安装过的APK位于data分区。shouldHideSystemApp表示是否需要将系统APK设置为Disable状态，默认情况下为false；如果安装过APK的版本号比当前扫描系统APK的版本要高则意味着要使用data分区的APK，隐藏系统APK，则shouldHideSystem被设置为true。

* 第六步：调用scanPackageLI(PackageParser.Package, int, int, long, UserHandle)方法对解析出来的PackageParse.Package进行处理。

* 

  作者：隔壁老李头
  链接：https://www.jianshu.com/p/b9b2b0f650e9
  来源：简书
  著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

那我们就来看下scanPackageLI(PackageParser.Package, int, int, long, UserHandle)方法。

## 六、PackageManagerService#scanPackageLI(PackageParser.Package, int, int, long, UserHandle)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 6646行

```java
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        boolean success = false;
        try {
            // 调用scanPackageDirtyLI开始解析PackageParser.Package
            final PackageParser.Package res = scanPackageDirtyLI(pkg, parseFlags, scanFlags,
                    currentTime, user);
            success = true;
            return res;
        } finally {
             // 如果解析失败，则删除相应目录
            if (!success && (scanFlags & SCAN_DELETE_DATA_ON_FAILURES) != 0) {
                removeDataDirsLI(pkg.volumeUuid, pkg.packageName);
            }
        }
    }
```

这个方法内部其实很简答， 主要就是调用scanPackageDirtyLI(PackageParser.Package,int, int, long, UserHandle) 方法来进一步解析PackageParser.Package。

## 七、PackageManagerService#scanPackageDirtyLI(PackageParser.Package,int, int, long, UserHandle) 方法解析

为了让后续大家更好的理解这个方法的原理，我这里先说几个变量，这样有助于后续代码的理解

* 1、PackageManagerService的成员变量mPlatformPackage用于保存该Package信息，同理PackageManagerService的成员变量mAndroidApplication用于保存此Package中的ApplicationInfo

* 2、这里先说一下framework-res.apk，它的包名为"android"。这个APK里面有两个常用的Acitvity：①ChooserActivity(当多个Activity符合某个Intent的时候，系统会弹出的Activity)和②ShutdownActivity(长按电源键关机时，弹出的Activity)。这个APK它被定义在frameworks/base/core/res中，对应的AndroidManifest.xml为

```go
<manifest xmlns:android="http://schemas.android.comapk/res/android" 
 package="android" coreApp="true"  
 android:sharedUserId="android.uid.system" 
 android:sharedUserLabel="@stringandroid_system_label" >
```

* 3、mResolveActivity指向用于表示ChooserActivity信息的ActivityInfo。mResolveInfo为ResolveInfo类型，它用于存储系统解析Intent(经IntentFilter过滤)得到的结果信息，例如满足某个Intnet的Activity信息。在PackageManagerService中查询某个Intent的Activity时，返回就是的ResolveInfo，然后再根据ResolveInfo的信息得到具体的Activity。可能是因为ChooserActivity使用地方很多，因此PackageManagerService在此处保存这些信息，以较高运行过程中的效率。
* 4、PackageManagerService的mPackages变量和Settings的mPackages变量，搞懂这两个变量对于理解PackageManagerService很重要：
  * PackageManagerService的mPackages变量：
     当一个Package独享创建以后，就需要把它加入到PackageManagerService中去，所以PackageManagerService有一个mPackages对象它保存着<PackageName, PackageParser.Package>键值对，所有已经安装的包全部都保存在这里面。当一个APK顺利通过扫描过程之后，其Package对象便会被添加到mPackages这个映射表中。所以，在这个方法里面有很多"mPackages的锁"，因为需要对其进行操作。
  * Settings的mPackages变量：保存着<PackageName, PackageSetting>键值对，每次开机PackageManagerService初始化时会从/data/system/packages.xml中恢复出package到Settings.mPackages中。PackageManagerService的Setting最终会序列化到/data/system/packages.xml中。
     开机安装APK判断安装的APK是否是new Package，就是通过.apk解析的信息更Settings.mPackages的信息对比。
* 5、PackageManagerService的mPlatformPackage指的是/system/framework/framework-res.apk包
* 6、PackageManagerService的mDisabledSysPackages指的是正在被替换的system APK会暂时保存在mDisabledSysPackages列表中，等待安装完成后便会从mDisabledSysPackages中删除。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 6481行

```dart
    private PackageParser.Package scanPackageDirtyLI(PackageParser.Package pkg, int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
         // *********** 第一步 ******************
        final File scanFile = new File(pkg.codePath);
        // 非空路径判断
        if (pkg.applicationInfo.getCodePath() == null ||
                pkg.applicationInfo.getResourcePath() == null) {
            // Bail out. The resource and code paths haven't been set.
            throw new PackageManagerException(INSTALL_FAILED_INVALID_APK,
                    "Code and resource paths haven't been set correctly");
        }

         // *********** 第二步 ******************
        // 根据policyFlags设置package以及其中applicationInfo等成员信息
         // 对非系统应用设置pkg.coreApp = false
        if ((parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM;
        } else {
            // Only allow system apps to be flagged as core apps.
            pkg.coreApp = false;
        }

         //判断在解析APK的时候是不是私有的，即是不是在AndroidManifest里面设置了exported。
        // 如果设置，则设置对应pkg.applicationInfo.privateFlags
        if ((parseFlags&PackageParser.PARSE_IS_PRIVILEGED) != 0) {
            pkg.applicationInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_PRIVILEGED;
        }


         // *********** 第三步 ******************
         //mCustomResolverComponentName是从系统资源中读出的，可以配置
        if (mCustomResolverComponentName != null &&
                mCustomResolverComponentName.getPackageName().equals(pkg.packageName)) {
           // 这里的用途和下面判断packageName是否为"android"有联系，因为调用setUpCustomResolverActivity(pkg)后mResolverReplaced为true。
            setUpCustomResolverActivity(pkg);
        }

        // 针对包名为"android" 的APK进行处理
        if (pkg.packageName.equals("android")) {
            synchronized (mPackages) {
                 // 如果mAndroidApplication已经被初始化了，则抛异常
                if (mAndroidApplication != null) {
                    Slog.w(TAG, "*************************************************");
                    Slog.w(TAG, "Core android package being redefined.  Skipping.");
                    Slog.w(TAG, " file=" + scanFile);
                    Slog.w(TAG, "*************************************************");
                    throw new PackageManagerException(INSTALL_FAILED_DUPLICATE_PACKAGE,
                            "Core android package being redefined.  Skipping.");
                }

                // Set up information for our fall-back user intent resolution activity.
                // 为我们回退的页面配置信息
                mPlatformPackage = pkg;
                pkg.mVersionCode = mSdkVersion;
                mAndroidApplication = pkg.applicationInfo;
                // 如果上面的代码中调用了setUpCustomResolverActivity方法，在setUpCustomResolverActivity方法里面设置了mResolverReplaced=true.
                if (!mResolverReplaced) {

                    // *********** 第四步 ******************

                    // 如果没有调用setUpCustomResolverActivity方法，配置相应mResolveActivity的属性
                    mResolveActivity.applicationInfo = mAndroidApplication;
                    mResolveActivity.name = ResolverActivity.class.getName();
                    mResolveActivity.packageName = mAndroidApplication.packageName;
                    mResolveActivity.processName = "system:ui";
                    mResolveActivity.launchMode = ActivityInfo.LAUNCH_MULTIPLE;
                    mResolveActivity.documentLaunchMode = ActivityInfo.DOCUMENT_LAUNCH_NEVER;
                    mResolveActivity.flags = ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS;
                    mResolveActivity.theme = R.style.Theme_Holo_Dialog_Alert;
                    mResolveActivity.exported = true;
                    mResolveActivity.enabled = true;
                    mResolveInfo.activityInfo = mResolveActivity;
                    mResolveInfo.priority = 0;
                    mResolveInfo.preferredOrder = 0;
                    mResolveInfo.match = 0;
                    mResolveComponentName = new ComponentName(
                            mAndroidApplication.packageName, mResolveActivity.name);
                }
            }
        }

        if (DEBUG_PACKAGE_SCANNING) {
            if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                Log.d(TAG, "Scanning package " + pkg.packageName);
        }

        // *********** 第五步 ******************

        // 如果这个安装包的包名存在已经安装的列表中，说明该APP已经安装了，则不能重复安装
       // mPackage用于保存系统内所有Package，以packageName为key
        if (mPackages.containsKey(pkg.packageName)
                || mSharedLibraries.containsKey(pkg.packageName)) {
            throw new PackageManagerException(INSTALL_FAILED_DUPLICATE_PACKAGE,
                    "Application package " + pkg.packageName
                    + " already installed.  Skipping duplicate.");
        }

        // If we're only installing presumed-existing packages, require that the
        // scanned APK is both already known and at the path previously established
        // for it.  Previously unknown packages we pick up normally, but if we have an
        // a priori expectation about this package's install presence, enforce it.
        // With a singular exception for new system packages. When an OTA contains
        // a new system package, we allow the codepath to change from a system location
        // to the user-installed location. If we don't allow this change, any newer,
        // user-installed version of the application will be ignored.

        // 如果我们只安装已经存在的APP的包，因为是已经存在APP，所以可以通过PackageSetting获取它的路径，如果路径不一致，则抛异常
        if ((scanFlags & SCAN_REQUIRE_KNOWN) != 0) {
            if (mExpectingBetter.containsKey(pkg.packageName)) {
                logCriticalInfo(Log.WARN,
                        "Relax SCAN_REQUIRE_KNOWN requirement for package " + pkg.packageName);
            } else {
                PackageSetting known = mSettings.peekPackageLPr(pkg.packageName);
                if (known != null) {
                    if (DEBUG_PACKAGE_SCANNING) {
                        Log.d(TAG, "Examining " + pkg.codePath
                                + " and requiring known paths " + known.codePathString
                                + " & " + known.resourcePathString);
                    }
                    if (!pkg.applicationInfo.getCodePath().equals(known.codePathString)
                            || !pkg.applicationInfo.getResourcePath().equals(known.resourcePathString)) {
                        throw new PackageManagerException(INSTALL_FAILED_PACKAGE_CHANGED,
                                "Application package " + pkg.packageName
                                + " found at " + pkg.applicationInfo.getCodePath()
                                + " but expected at " + known.codePathString + "; ignoring.");
                    }
                }
            }
        }

        // *********** 第六步 ******************

        // Initialize package source and resource directories
        // 初始化安装包的代码和资源目录
        File destCodeFile = new File(pkg.applicationInfo.getCodePath());
        File destResourceFile = new File(pkg.applicationInfo.getResourcePath());

        // 代表Package的SharedUserSettings对象
        SharedUserSetting suid = null;
        // 代表Pacakge的PacakgeSettings对象
        PackageSetting pkgSetting = null;

        // 如果不是系统APP
        if (!isSystemApp(pkg)) {
            // Only system apps can use these features.
            // 只有系统APP才用到下面三个特性
            pkg.mOriginalPackages = null;
            pkg.mRealPackage = null;
            pkg.mAdoptPermissions = null;
        }

         // *********** 第七步 ******************
        // writer
        // 锁上mPackages对象，意味着要对这个数据结构进行写操作，里面保存的就是解析出来的包信息
        synchronized (mPackages) {

            if (pkg.mSharedUserId != null) {
                // 如果已经定义ShareUserId，则创建Package对应的ShareduserSetting
                //然后加入到PackageManangerService中的Settings对象维护的数据结构中
                suid = mSettings.getSharedUserLPw(pkg.mSharedUserId, 0, 0, true);
                if (suid == null) {
                   //创建ShareduserSetting失败，抛异常
                    throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                            "Creating application package " + pkg.packageName
                            + " for shared user failed");
                }
                if (DEBUG_PACKAGE_SCANNING) {
                    if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                        Log.d(TAG, "Shared UserID " + pkg.mSharedUserId + " (uid=" +                                 + "): packages=" + suid.packages);
                }
            }

            // 创建出Package对应的PackageSettings，必要时还要处理Package新旧信息的转化
            // Check if we are renaming from an original package name.
            // 检查 原始包重命名的情况
            PackageSetting origPackage = null;
            String realName = null;
          
            // 如果存在重命名前的包
            if (pkg.mOriginalPackages != null) {
                // This package may need to be renamed to a previously
                // installed name.  Let's check on that...
               // 获取重命名的包名
                final String renamed = mSettings.mRenamedPackages.get(pkg.mRealPackage);
                if (pkg.mOriginalPackages.contains(renamed)) {
                    // This package had originally been installed as the
                    // original name, and we have already taken care of
                    // transitioning to the new one.  Just update the new
                    // one to continue using the old name.
                   // 如果这个包原来是使用原始的名字，后面变更为新的名字，所以我们只需要更新到新的名字。
                    realName = pkg.mRealPackage;
                    // 进行重命名操作
                    if (!pkg.packageName.equals(renamed)) {
                        // Callers into this function may have already taken
                        // care of renaming the package; only do it here if
                        // it is not already done.
                        pkg.setPackageName(renamed);
                    }

                } else {
                    //如果不包含在mOriginalPackages中
                    // 遍历mOriginalPackages
                    for (int i=pkg.mOriginalPackages.size()-1; i>=0; i--) {
                        // 判断pkg的原始包的某一个是否出现在mSettings里面，如果出现过，则说明之前有过
                        if ((origPackage = mSettings.peekPackageLPr(
                                pkg.mOriginalPackages.get(i))) != null) {
                            // We do have the package already installed under its
                            // original name...  should we use it?
                            // 包名非空验证
                            if (!verifyPackageUpdateLPr(origPackage, pkg)) {
                                // New package is not compatible with original.
                                origPackage = null;
                                continue;
                            } else if (origPackage.sharedUser != null) {
                                // Make sure uid is compatible between packages.
                                // 确保uid一致，如果uid不一致，则跳过
                                if (!origPackage.sharedUser.name.equals(pkg.mSharedUserId)) {
                                    Slog.w(TAG, "Unable to migrate data from " + origPackage.name
                                            + " to " + pkg.packageName + ": old uid "
                                            + origPackage.sharedUser.name
                                            + " differs from " + pkg.mSharedUserId);
                                    origPackage = null;
                                    continue;
                                }
                            } else {
                                if (DEBUG_UPGRADE) Log.v(TAG, "Renaming new package "
                                        + pkg.packageName + " to old name " + origPackage.name);
                            }
                            break;
                        }
                    }
                }
            }

            if (mTransferedPackages.contains(pkg.packageName)) {
                Slog.w(TAG, "Package " + pkg.packageName
                        + " was transferred to another, but its .apk remains");
            }

            // Just create the setting, don't add it yet. For already existing packages
            // the PkgSetting exists already and doesn't have to be created.
            // 生成PackageSetting对象，对应的数据结构将序列化在/data/system/packages.xml文中中
            pkgSetting = mSettings.getPackageLPw(pkg, origPackage, realName, suid, destCodeFile,
                    destResourceFile, pkg.applicationInfo.nativeLibraryRootDir,
                    pkg.applicationInfo.primaryCpuAbi,
                    pkg.applicationInfo.secondaryCpuAbi,
                    pkg.applicationInfo.flags, pkg.applicationInfo.privateFlags,
                    user, false);

            if (pkgSetting == null) {
                // 如果失败，则抛异常
                throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                        "Creating application package " + pkg.packageName + " failed");
            }

            // 如果是有原始包的，则需要进进行原始包操作
            if (pkgSetting.origPackage != null) {
                // If we are first transitioning from an original package,
                // fix up the new package's name now.  We need to do this after
                // looking up the package under its new name, so getPackageLP
                // can take care of fiddling things correctly.
                // 设置 这个APP的包名为原始包名，并设置origPackage为null
                pkg.setPackageName(origPackage.name);

                // File a report about this.
                String msg = "New package " + pkgSetting.realName
                        + " renamed to replace old package " + pkgSetting.name;
                reportSettingsProblem(Log.WARN, msg);

                // Make a note of it.
                mTransferedPackages.add(origPackage.name);

                // No longer need to retain this.
                pkgSetting.origPackage = null;
            }

            // 如果真是的名字不为空，即有过重命名的，则添加进去
            if (realName != null) {
                // Make a note of it.
                mTransferedPackages.add(pkg.packageName);
            }
             // 如果是这个安装包在mDisabledSysPackages列表中，则设置其flag为FLAG_UPDATED_SYSTEM_APP
            // mSetting.mDisabledSysPackages中保存了所有已替换的安装包
            if (mSettings.isDisabledSystemPackageLPr(pkg.packageName)) {

                // 如果当前安装的APK 在mDisabledSysPackages列表中表示当前正在升级的system apk
                pkg.applicationInfo.flags |= ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            }

           // *********** 第八步 ******************

            // 创建共享库
            // 如果Package声明了需要library或option-libaray，PackageManagerService需要确保这些library已经被加载到mSharedLibraries中
            if ((parseFlags&PackageParser.PARSE_IS_SYSTEM_DIR) == 0) {
             //如果不是系统目录
                // Check all shared libraries and map to their actual file path.
                // We only do this here for apps not on a system dir, because those
                // are the only ones that can fail an install due to this.  We
                // will take care of the system apps by updating all of their
                // library paths after the scan is done.
                //检查所有共享库并映射到其具体的文件路径。我们只为非系统目录下的应用进行如下操作为，因为只有他们才需要如此做
                // 扫描完成后，我们将通过更新所有库路径来处理系统应用程序
                updateSharedLibrariesLPw(pkg, null);
            }
     
           // *********** 第九步 ******************
            if (mFoundPolicyFile) {
                // 根据policy文件，找到Package对应的seinfo，然后存入Package的applicationInfo中
                SELinuxMMAC.assignSeinfoValue(pkg);
            }

           // 处理Package的签名信息，还包括更新和验证
            pkg.applicationInfo.uid = pkgSetting.appId;
            // 将pkgSetting保存到pkg.mExtras中
            pkg.mExtras = pkgSetting;
            //shouldCheckUpgradeKeySetLP方法进行密钥检查，是否一致
            if (shouldCheckUpgradeKeySetLP(pkgSetting, scanFlags)) {
                if (checkUpgradeKeySetLP(pkgSetting, pkg)) {
                    //检查签名是否正确
                    // We just determined the app is signed correctly, so bring
                    // over the latest parsed certs.
                    pkgSetting.signatures.mSignatures = pkg.mSignatures;
                } else {
                    // 签名错误
                    if ((parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) == 0) {
                        throw new PackageManagerException(INSTALL_FAILED_UPDATE_INCOMPATIBLE,
                                "Package " + pkg.packageName + " upgrade keys do not match the "
                                + "previously installed version");
                    } else {
                        pkgSetting.signatures.mSignatures = pkg.mSignatures;
                        String msg = "System package " + pkg.packageName
                            + " signature changed; retaining data.";
                        reportSettingsProblem(Log.WARN, msg);
                    }
                }
            } else {
                // 如果检查升级签名错误
                try {
                   // 重新验证签名
                    verifySignaturesLP(pkgSetting, pkg);
                    // We just determined the app is signed correctly, so bring
                    // over the latest parsed certs.
                    pkgSetting.signatures.mSignatures = pkg.mSignatures;
                } catch (PackageManagerException e) {
                    // 验证签名错误
                    if ((parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) == 0) {
                        throw e;
                    }
                    // The signature has changed, but this package is in the system
                    // image...  let's recover!
                    pkgSetting.signatures.mSignatures = pkg.mSignatures;
                    // However...  if this package is part of a shared user, but it
                    // doesn't match the signature of the shared user, let's fail.
                    // What this means is that you can't change the signatures
                    // associated with an overall shared user, which doesn't seem all
                    // that unreasonable.
                    if (pkgSetting.sharedUser != null) {
                        if (compareSignatures(pkgSetting.sharedUser.signatures.mSignatures,
                                              pkg.mSignatures) != PackageManager.SIGNATURE_MATCH) {
                            throw new PackageManagerException(
                                    INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES,
                                            "Signature mismatch for shared user : "
                                            + pkgSetting.sharedUser);
                        }
                    }
                    // File a report about this.
                    String msg = "System package " + pkg.packageName
                        + " signature changed; retaining data.";
                    reportSettingsProblem(Log.WARN, msg);
                }
            }

           // *********** 第十步 ******************
            // Verify that this new package doesn't have any content providers
            // that conflict with existing packages.  Only do this if the
            // package isn't already installed, since we don't want to break
            // things that are installed.
           // 验证新的包的provider不会与现有的包冲突
            if ((scanFlags & SCAN_NEW_INSTALL) != 0) {
                // 获取安装包中provider的数量
                final int N = pkg.providers.size();
                int i;
                for (i=0; i<N; i++) {
                    PackageParser.Provider p = pkg.providers.get(i);
                    if (p.info.authority != null) {
                        String names[] = p.info.authority.split(";");
                        for (int j = 0; j < names.length; j++) {
                            // 如果包含同样的provider，其中mProvidersByAuthority是系统中已有的provider
                            if (mProvidersByAuthority.containsKey(names[j])) {
                                PackageParser.Provider other = mProvidersByAuthority.get(names[j]);
                                final String otherPackageName =
                                        ((other != null && other.getComponentName() != null) ?
                                                other.getComponentName().getPackageName() : "?");
                                throw new PackageManagerException(
                                        INSTALL_FAILED_CONFLICTING_PROVIDER,
                                                "Can't install because provider name " + names[j]
                                                + " (in package " + pkg.applicationInfo.packageName
                                                + ") is already used by " + otherPackageName);
                            }
                        }
                    }
                }
            }

            // *********** 第十一步 ******************

             // 是否需要获取其他包的权限
            if (pkg.mAdoptPermissions != null) {
             // 如果设置mAdoptPermissions属性，对应的AndroidManifest里面的"android:adopt-permissions"，
             // 则对应设置对应的权限！该处有不明白的，请百度"android:adopt-permissions"。就明白了
                // This package wants to adopt ownership of permissions from
                // another package.
                for (int i = pkg.mAdoptPermissions.size() - 1; i >= 0; i--) {
                    final String origName = pkg.mAdoptPermissions.get(i);
                    final PackageSetting orig = mSettings.peekPackageLPr(origName);
                    if (orig != null) {
                        if (verifyPackageUpdateLPr(orig, pkg)) {
                            Slog.i(TAG, "Adopting permissions from " + origName + " to "
                                    + pkg.packageName);
                            //将origName的权限转给pkg
                            mSettings.transferPermissionsLPw(origName, pkg.packageName);
                        }
                    }
                }
            }
        }
  
         // 确定 进程的名称，一般为packageName
        final String pkgName = pkg.packageName;

        final long scanFileTime = scanFile.lastModified();
        final boolean forceDex = (scanFlags & SCAN_FORCE_DEX) != 0;
        pkg.applicationInfo.processName = fixProcessName(
                pkg.applicationInfo.packageName,
                pkg.applicationInfo.processName,
                pkg.applicationInfo.uid);

       // *********** 第十二步 ******************
        // 获取应用的data路径，这里分系统应用和普通应用
        File dataPath;
        if (mPlatformPackage == pkg) {
            // 如果是系统APP
            // The system package is special.
            dataPath = new File(Environment.getDataDirectory(), "system");

            pkg.applicationInfo.dataDir = dataPath.getPath();

        } else {
            // 如果是普通应用
            // This is a normal package, need to make its data directory.
            // 获取应用的 /data/data/packageName目录
            dataPath = Environment.getDataUserPackageDirectory(pkg.volumeUuid,
                    UserHandle.USER_OWNER, pkg.packageName);

            boolean uidError = false;
            // 如果该目录已经存在了
            if (dataPath.exists()) {
                int currentUid = 0;
                try {
                   // 通过系统调用获取StructStat
                    StructStat stat = Os.stat(dataPath.getPath());
                    currentUid = stat.st_uid;
                } catch (ErrnoException e) {
                    Slog.e(TAG, "Couldn't stat path " + dataPath.getPath(), e);
                }

                // If we have mismatched owners for the data path, we have a problem.
                // 处理同样的APK，修改了UID的情况
                if (currentUid != pkg.applicationInfo.uid) {
                    boolean recovered = false;
                    if (currentUid == 0) {
                        // currentUid等于0，表示root用户，可能是由于在安装过程中由于系统停止导致的目录错乱。
                        // The directory somehow became owned by root.  Wow.
                        // This is probably because the system was stopped while
                        // installd was in the middle of messing with its libs
                        // directory.  Ask installd to fix that.
                        // 调用mInstaller的fixUid方法进行修复
                        int ret = mInstaller.fixUid(pkg.volumeUuid, pkgName,
                                pkg.applicationInfo.uid, pkg.applicationInfo.uid);
                        if (ret >= 0) {
                            // 设置recovered=true
                            recovered = true;
                            String msg = "Package " + pkg.packageName
                                    + " unexpectedly changed to uid 0; recovered to " +
                                    + pkg.applicationInfo.uid;
                            reportSettingsProblem(Log.WARN, msg);
                        }
                    }

                     // 系统APK 需要删除data目录后重新创建 
                    if (!recovered && ((parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0
                            || (scanFlags&SCAN_BOOTING) != 0)) {
                        // If this is a system app, we can at least delete its
                        // current data so the application will still work.
                       // 如果是系统APP，我们要清楚当前数据以便应用未来可以使用
                        int ret = removeDataDirsLI(pkg.volumeUuid, pkgName);
                        if (ret >= 0) {
                            // 如果删除成功
                            // TODO: Kill the processes first
                            // Old data gone!
                            String prefix = (parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0
                                    ? "System package " : "Third party package ";
                            String msg = prefix + pkg.packageName
                                    + " has changed from uid: "
                                    + currentUid + " to "
                                    + pkg.applicationInfo.uid + "; old data erased";
                            reportSettingsProblem(Log.WARN, msg);

                           // 设置recovered=true
                            recovered = true;

                            // And now re-install the app.
                            // 重新安装 APP
                            ret = createDataDirsLI(pkg.volumeUuid, pkgName, pkg.applicationInfo.uid,
                                    pkg.applicationInfo.seinfo);
                            if (ret == -1) {
                                 // 如果重新安装失败
                                // Ack should not happen!
                                msg = prefix + pkg.packageName
                                        + " could not have data directory re-created after delete.";
                                reportSettingsProblem(Log.WARN, msg);
                                throw new PackageManagerException(
                                        INSTALL_FAILED_INSUFFICIENT_STORAGE, msg);
                            }
                        }
                        if (!recovered) {
                            mHasSystemUidErrors = true;
                        }
                    } else if (!recovered) {
                        // If we allow this install to proceed, we will be broken.
                        // Abort, abort!
                        throw new PackageManagerException(INSTALL_FAILED_UID_CHANGED,
                                "scanPackageLI");
                    }

                    // 最后 如果还不需要恢复
                    if (!recovered) {
                        // 设置pkg的相关目录路径
                        pkg.applicationInfo.dataDir = "/mismatched_uid/settings_"
                            + pkg.applicationInfo.uid + "/fs_"
                            + currentUid;
                        pkg.applicationInfo.nativeLibraryDir = pkg.applicationInfo.dataDir;
                        pkg.applicationInfo.nativeLibraryRootDir = pkg.applicationInfo.dataDir;
                        String msg = "Package " + pkg.packageName
                                + " has mismatched uid: "
                                + currentUid + " on disk, "
                                + pkg.applicationInfo.uid + " in settings";
                        // writer
                        synchronized (mPackages) {
                            mSettings.mReadMessages.append(msg);
                            mSettings.mReadMessages.append('\n');
                            uidError = true;
                            if (!pkgSetting.uidError) {
                                reportSettingsProblem(Log.ERROR, msg);
                            }
                        }
                    }
                }
                pkg.applicationInfo.dataDir = dataPath.getPath();

               // 如果需要回复seinfo执行回复操作，一般不恢复
                if (mShouldRestoreconData) {
                    Slog.i(TAG, "SELinux relabeling of " + pkg.packageName + " issued.");
                    mInstaller.restoreconData(pkg.volumeUuid, pkg.packageName,
                            pkg.applicationInfo.seinfo, pkg.applicationInfo.uid);
                }
            } else {
                // 如果不存在安装目录
                if (DEBUG_PACKAGE_SCANNING) {
                    if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                        Log.v(TAG, "Want this data dir: " + dataPath);
                }
                //invoke installer to do the actual installation
                // 调用安装程序进行安装从而获取相应目录
                int ret = createDataDirsLI(pkg.volumeUuid, pkgName, pkg.applicationInfo.uid,
                        pkg.applicationInfo.seinfo);
                if (ret < 0) {
                     // 如果安装失败
                    // Error from installer
                    throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                            "Unable to create data dirs [errorCode=" + ret + "]");
                }

                if (dataPath.exists()) {
                    // 安装以后，如果安装目录存在
                    pkg.applicationInfo.dataDir = dataPath.getPath();
                } else {
                     // 如果安装之后，还没有安装目录
                    Slog.w(TAG, "Unable to create data directory: " + dataPath);
                    pkg.applicationInfo.dataDir = null;
                }
            }

            pkgSetting.uidError = uidError;
        }

       // *********** 第十三步 ******************

        // 设置native相关属性
        final String path = scanFile.getPath();
         // 获取该应用支持的平台
        final String cpuAbiOverride = deriveAbiOverride(pkg.cpuAbiOverride, pkgSetting);

         // 如果是新安装
        if ((scanFlags & SCAN_NEW_INSTALL) == 0) {
             // 在/data/data/packageName/lib下建立和CPU类型对应的目录，例如ARM平台 arm，MIP平台 mips/
            derivePackageAbi(pkg, scanFile, cpuAbiOverride, true /* extract libs */);

            // Some system apps still use directory structure for native libraries
            // in which case we might end up not detecting abi solely based on apk
            // structure. Try to detect abi based on directory structure.
        
            // 如果是系统APP，系统APP的native库统一放到/system/lib下
             // 所以系统不会提取系统APP目录apk包中 native库
            if (isSystemApp(pkg) && !pkg.isUpdatedSystemApp() &&
                    pkg.applicationInfo.primaryCpuAbi == null) {
                setBundledAppAbisAndRoots(pkg, pkgSetting);
                setNativeLibraryPaths(pkg);
            }

        } else {
            if ((scanFlags & SCAN_MOVE) != 0) {
                // 如果是移动
                // We haven't run dex-opt for this move (since we've moved the compiled output too)
                // but we already have this packages package info in the PackageSetting. We just
                // use that and derive the native library path based on the new codepath.
                // 设置支持的类型
                pkg.applicationInfo.primaryCpuAbi = pkgSetting.primaryCpuAbiString;
                pkg.applicationInfo.secondaryCpuAbi = pkgSetting.secondaryCpuAbiString;
            }

            // Set native library paths again. For moves, the path will be updated based on the
            // ABIs we've determined above. For non-moves, the path will be updated based on the
            // ABIs we determined during compilation, but the path will depend on the final
            // package path (after the rename away from the stage path).
           // 设置native 库的路径
            setNativeLibraryPaths(pkg);
        }

         // *********** 第十四步 ******************
        if (DEBUG_INSTALL) Slog.i(TAG, "Linking native library dir for " + path);
        // 创建库链接
        final int[] userIds = sUserManager.getUserIds();
        synchronized (mInstallLock) {
            // Make sure all user data directories are ready to roll; we're okay
            // if they already exist
            // 确保所有用户数据目录已经准备好了。
            if (!TextUtils.isEmpty(pkg.volumeUuid)) {
                for (int userId : userIds) {
                    if (userId != 0) {
                        mInstaller.createUserData(pkg.volumeUuid, pkg.packageName,
                                UserHandle.getUid(userId, pkg.applicationInfo.uid), userId,
                                pkg.applicationInfo.seinfo);
                    }
                }
            }

            // Create a native library symlink only if we have native libraries
            // and if the native libraries are 32 bit libraries. We do not provide
            // this symlink for 64 bit libraries.
            // 有且仅有 本地库，且本地库是32位的时候，才能创建一个本地库的链接，不为64位的库提供链接
            if (pkg.applicationInfo.primaryCpuAbi != null &&
                    !VMRuntime.is64BitAbi(pkg.applicationInfo.primaryCpuAbi)) {
                final String nativeLibPath = pkg.applicationInfo.nativeLibraryDir;
                for (int userId : userIds) {
                    // 创建本地库 链接
                    if (mInstaller.linkNativeLibraryDirectory(pkg.volumeUuid, pkg.packageName,
                            nativeLibPath, userId) < 0) {
                        throw new PackageManagerException(INSTALL_FAILED_INTERNAL_ERROR,
                                "Failed linking native library dir (user=" + userId + ")");
                    }
                }
            }
        }

        // This is a special case for the "system" package, where the ABI is
        // dictated by the zygote configuration (and init.rc). We should keep track
        // of this ABI so that we can deal with "normal" applications that run under
        // the same UID correctly.
         // "系统"安装包有特殊情况，其中ABI是由zygote配置(init.rc)指定。我们应该跟踪这个ABI。这样我们就可以在相同的UID下，正确处理"正常"的应用程序。
        if (mPlatformPackage == pkg) {
            pkg.applicationInfo.primaryCpuAbi = VMRuntime.getRuntime().is64Bit() ?
                    Build.SUPPORTED_64_BIT_ABIS[0] : Build.SUPPORTED_32_BIT_ABIS[0];
        }

        // If there's a mismatch between the abi-override in the package setting
        // and the abiOverride specified for the install. Warn about this because we
        // would've already compiled the app without taking the package setting into
        // account.
        if ((scanFlags & SCAN_NO_DEX) == 0 && (scanFlags & SCAN_NEW_INSTALL) != 0) {
            if (cpuAbiOverride == null && pkgSetting.cpuAbiOverrideString != null) {
                // 如果程序中设置的abi-override和为安装指定的abiOverride之间不匹配
                Slog.w(TAG, "Ignoring persisted ABI override " + cpuAbiOverride +
                        " for package: " + pkg.packageName);
            }
        }

        // 初始化abi属性
        pkgSetting.primaryCpuAbiString = pkg.applicationInfo.primaryCpuAbi;
        pkgSetting.secondaryCpuAbiString = pkg.applicationInfo.secondaryCpuAbi;
        pkgSetting.cpuAbiOverrideString = cpuAbiOverride;

        // Copy the derived override back to the parsed package, so that we can
        // update the package settings accordingly.
        // 复制 abi属性到解析包pkg里面
        pkg.cpuAbiOverride = cpuAbiOverride;

        if (DEBUG_ABI_SELECTION) {
            Slog.d(TAG, "Resolved nativeLibraryRoot for " + pkg.applicationInfo.packageName
                    + " to root=" + pkg.applicationInfo.nativeLibraryRootDir + ", isa="
                    + pkg.applicationInfo.nativeLibraryRootRequiresIsa);
        }

        // Push the derived path down into PackageSettings so we know what to
        // clean up at uninstall time.
        // 保存lib 路径、方便卸载时清理
        pkgSetting.legacyNativeLibraryPathString = pkg.applicationInfo.nativeLibraryRootDir;

        if (DEBUG_ABI_SELECTION) {
            Log.d(TAG, "Abis for package[" + pkg.packageName + "] are" +
                    " primary=" + pkg.applicationInfo.primaryCpuAbi +
                    " secondary=" + pkg.applicationInfo.secondaryCpuAbi);
        }

        if ((scanFlags&SCAN_BOOTING) == 0 && pkgSetting.sharedUser != null) {
            // We don't do this here during boot because we can do it all
            // at once after scanning all existing packages.
            //
            // We also do this *before* we perform dexopt on this package, so that
            // we can avoid redundant dexopts, and also to make sure we've got the
            // code and package path correct.
            // 调整共享用户的abi
            adjustCpuAbisForSharedUserLPw(pkgSetting.sharedUser.packages,
                    pkg, forceDex, (scanFlags & SCAN_DEFER_DEX) != 0, true /* boot complete */);
        }


         // *********** 第十五步 ******************

        if ((scanFlags & SCAN_NO_DEX) == 0) {
            // 对APK进行dex优化
            int result = mPackageDexOptimizer.performDexOpt(pkg, null /* instruction sets */,
                    forceDex, (scanFlags & SCAN_DEFER_DEX) != 0, false /* inclDependencies */,
                    (scanFlags & SCAN_BOOTING) == 0);
            if (result == PackageDexOptimizer.DEX_OPT_FAILED) {
                throw new PackageManagerException(INSTALL_FAILED_DEXOPT, "scanPackageLI");
            }
        }

        // 如果是工厂模式
        if (mFactoryTest && pkg.requestedPermissions.contains(
                android.Manifest.permission.FACTORY_TEST)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_FACTORY_TEST;
        }

        ArrayList<PackageParser.Package> clientLibPkgs = null;

        // 处理系统APK更新时，链接库的改变
        // writer
        synchronized (mPackages) {
            if ((pkg.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) != 0) {
                // 只有系统应用才可以添加新的共享库
                // Only system apps can add new shared libraries.
                if (pkg.libraryNames != null) {
                    // 遍历它的库
                    for (int i=0; i<pkg.libraryNames.size(); i++) {
                        String name = pkg.libraryNames.get(i);
                        boolean allowed = false;
                        // 是需要更新的系统APP
                        if (pkg.isUpdatedSystemApp()) {
                            // New library entries can only be added through the
                            // system image.  This is important to get rid of a lot
                            // of nasty edge cases: for example if we allowed a non-
                            // system update of the app to add a library, then uninstalling
                            // the update would make the library go away, and assumptions
                            // we made such as through app install filtering would now
                            // have allowed apps on the device which aren't compatible
                            // with it.  Better to just have the restriction here, be
                            // conservative, and create many fewer cases that can negatively
                            // impact the user experience.
                            // 调用getDisabledSystemPkgLPr方法获取其对应的PackageSetting
                            final PackageSetting sysPs = mSettings
                                    .getDisabledSystemPkgLPr(pkg.packageName);
                             // 进行对比
                            if (sysPs.pkg != null && sysPs.pkg.libraryNames != null) {
                                for (int j=0; j<sysPs.pkg.libraryNames.size(); j++) {
                                    if (name.equals(sysPs.pkg.libraryNames.get(j))) {
                                       //如果包名相同，可以添加共享库
                                        allowed = true;
                                        allowed = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                             //可以添加共享库
                            allowed = true;
                        }
                        // 如果可以添加共享库
                        if (allowed) {
                            // 如果共享库里面没有这个库的名字，则添加
                            if (!mSharedLibraries.containsKey(name)) {
                                mSharedLibraries.put(name, new SharedLibraryEntry(null, pkg.packageName));
                            } else if (!name.equals(pkg.packageName)) {
                                Slog.w(TAG, "Package " + pkg.packageName + " library "
                                        + name + " already exists; skipping");
                            }
                        } else {
                            Slog.w(TAG, "Package " + pkg.packageName + " declares lib "
                                    + name + " that is not declared on system image; skipping");
                        }
                    }
                    if ((scanFlags&SCAN_BOOTING) == 0) {
                         // 如果不是启动，我们需要更新共享库应用程序。如果我们在启动过程中，这一切都将在扫描完成后完成
                        // If we are not booting, we need to update any applications
                        // that are clients of our shared library.  If we are booting,
                        // this will all be done once the scan is complete.
                        // 更新共享库
                        clientLibPkgs = updateAllSharedLibrariesLPw(pkg);
                    }
                }
            }
        }

        // We also need to dexopt any apps that are dependent on this library.  Note that
        // if these fail, we should abort the install since installing the library will
        // result in some apps being broken.
      
       // 对共享库进行dex优化
        if (clientLibPkgs != null) {
            if ((scanFlags & SCAN_NO_DEX) == 0) {
                for (int i = 0; i < clientLibPkgs.size(); i++) {
                    PackageParser.Package clientPkg = clientLibPkgs.get(i);
                    int result = mPackageDexOptimizer.performDexOpt(clientPkg,
                            null /* instruction sets */, forceDex,
                            (scanFlags & SCAN_DEFER_DEX) != 0, false,
                            (scanFlags & SCAN_BOOTING) == 0);
                    if (result == PackageDexOptimizer.DEX_OPT_FAILED) {
                        throw new PackageManagerException(INSTALL_FAILED_DEXOPT,
                                "scanPackageLI failed to dexopt clientLibPkgs");
                    }
                }
            }
        }

        // Request the ActivityManager to kill the process(only for existing packages)
        // so that we do not end up in a confused state while the user is still using the older
        // version of the application while the new one gets installed.

         // *********** 第十六步 ******************

        if ((scanFlags & SCAN_REPLACING) != 0) {
            // 如果该APK已经存在了，要先杀掉该APK的进程
            killApplication(pkg.applicationInfo.packageName,
                        pkg.applicationInfo.uid, "replace pkg");
        }

        // Also need to kill any apps that are dependent on the library.
        if (clientLibPkgs != null) {
            for (int i=0; i<clientLibPkgs.size(); i++) {
                PackageParser.Package clientPkg = clientLibPkgs.get(i);
                killApplication(clientPkg.applicationInfo.packageName,
                        clientPkg.applicationInfo.uid, "update lib");
            }
        }

        // Make sure we're not adding any bogus keyset info
        // 确保我们不添加任何虚假的密钥集信息
        KeySetManagerService ksms = mSettings.mKeySetManagerService;
        ksms.assertScannedPackageValid(pkg);

        // writer
        synchronized (mPackages) {
            // We don't expect installation to fail beyond this point

            // Add the new setting to mSettings
            // 把pkgSetting保存到Settings的变量mPackages中，String对应此包名，
            mSettings.insertPackageSettingLPw(pkgSetting, pkg);
            // Add the new setting to mPackages
            // 将pkg保存到PackageManagerService的成员变量mPackages中，key为包名
            mPackages.put(pkg.applicationInfo.packageName, pkg);

            // Make sure we don't accidentally delete its data.
            //清理空间 删除 已经的卸载的、但还占用存储空间的软件
            final Iterator<PackageCleanItem> iter = mSettings.mPackagesToBeCleaned.iterator();
            while (iter.hasNext()) {
                PackageCleanItem item = iter.next();
                if (pkgName.equals(item.packageName)) {
                    iter.remove();
                }
            }

            // Take care of first install / last update times.
            // 更新安装时间：即首次安装或者最后一次更新时间
            // 如果有当前时间
            if (currentTime != 0) {
                // 若有没有首次安装时间，则说明是首次安装，即设置安装时间
                if (pkgSetting.firstInstallTime == 0) {
                    pkgSetting.firstInstallTime = pkgSetting.lastUpdateTime = currentTime;
                } else if ((scanFlags&SCAN_UPDATE_TIME) != 0) {
                     // 如果有首次安装时间，则说明是更新安装，则设置最后更新时间
                    pkgSetting.lastUpdateTime = currentTime;
                }
            } else if (pkgSetting.firstInstallTime == 0) {
                // We need *something*.  Take time time stamp of the file.
                // 如果没有当前时间且没有首次安装时间，则设置首次时间和最后更新时间等于当前扫描时间
                pkgSetting.firstInstallTime = pkgSetting.lastUpdateTime = scanFileTime;
            } else if ((parseFlags&PackageParser.PARSE_IS_SYSTEM_DIR) != 0) {
                 // 如果没有当前时间，但是有首次安装时间
                if (scanFileTime != pkgSetting.timeStamp) {
                     //扫描时间并不等于pkgSetting上的时间，则认为是更新
                    // A package on the system image has changed; consider this
                    // to be an update.
                    pkgSetting.lastUpdateTime = scanFileTime;
                }
            }

            // Add the package's KeySets to the global KeySetManagerService
            // 添加安装包的到全局的KeySetManagerService里面
            ksms.addScannedPackageLPw(pkg);


           // *********** 第十七步 ******************

            // 在此之前，四大组件的信息都是Package对象的私有变量，通过栽面的代码，将他们注册到PackageManagerService里面。这样PackageManagerService就有了所有的组件信息

            // 注册pkg里面的provider到PackageManagerService上的mProvider上
            int N = pkg.providers.size();
            StringBuilder r = null;
            int i;
            for (i=0; i<N; i++) {
                PackageParser.Provider p = pkg.providers.get(i);
                // 设置进程名称。如果在AndroidManifest里面配置了进程名称，就以配置为准，如果没有配置，就是默认包名
                p.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        p.info.processName, pkg.applicationInfo.uid);
                mProviders.addProvider(p);
                p.syncable = p.info.isSyncable;
                if (p.info.authority != null) {
                    // 获取对应的
                    String names[] = p.info.authority.split(";");
                    p.info.authority = null;
                    for (int j = 0; j < names.length; j++) {
                        if (j == 1 && p.syncable) {
                            // We only want the first authority for a provider to possibly be
                            // syncable, so if we already added this provider using a different
                            // authority clear the syncable flag. We copy the provider before
                            // changing it because the mProviders object contains a reference
                            // to a provider that we don't want to change.
                            // Only do this for the second authority since the resulting provider
                            // object can be the same for all future authorities for this provider.
                            p = new PackageParser.Provider(p);
                            p.syncable = false;
                        }
                        // 对provider中的所有authority保存与Provider组成键值对保存在mProvidersByAuthority。
                        // 而mProvidersByAuthority是一个ArrayMap，意味着只要authority已经保存过，则就不会再保存了。
                        if (!mProvidersByAuthority.containsKey(names[j])) {
                            mProvidersByAuthority.put(names[j], p);
                            if (p.info.authority == null) {
                                p.info.authority = names[j];
                            } else {
                                p.info.authority = p.info.authority + ";" + names[j];
                            }
                            if (DEBUG_PACKAGE_SCANNING) {
                                if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                                    Log.d(TAG, "Registered content provider: " + names[j]
                                            + ", className = " + p.info.name + ", isSyncable = "
                                            + p.info.isSyncable);
                            }
                        } else {
                            PackageParser.Provider other = mProvidersByAuthority.get(names[j]);
                            Slog.w(TAG, "Skipping provider name " + names[j] +
                                    " (in package " + pkg.applicationInfo.packageName +
                                    "): name already used by "
                                    + ((other != null && other.getComponentName() != null)
                                            ? other.getComponentName().getPackageName() : "?"));
                        }
                    }
                }
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(p.info.name);
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Providers: " + r);
            }

            // 注册该Package中的service到PackageManagerService的mServices上
            N = pkg.services.size();
            r = null;
            for (i=0; i<N; i++) {
                PackageParser.Service s = pkg.services.get(i);
                s.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        s.info.processName, pkg.applicationInfo.uid);
                mServices.addService(s);
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(s.info.name);
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Services: " + r);
            }

            // 注册pkg里面的receiver到PackageManagerService上的receivers上
            N = pkg.receivers.size();
            r = null;
            for (i=0; i<N; i++) {
                PackageParser.Activity a = pkg.receivers.get(i);
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName, pkg.applicationInfo.uid);
                mReceivers.addActivity(a, "receiver");
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(a.info.name);
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Receivers: " + r);
            }

            // 注册pkg里面的activity到PackageManagerService上的activities1上
            N = pkg.activities.size();
            r = null;
            for (i=0; i<N; i++) {
                PackageParser.Activity a = pkg.activities.get(i);
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName, pkg.applicationInfo.uid);
                mActivities.addActivity(a, "activity");
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(a.info.name);
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Activities: " + r);
            }

            // 注册pkg里面的PermissionGroup到PackageManagerService上的mPermissionGroups上
            N = pkg.permissionGroups.size();
            r = null;
            for (i=0; i<N; i++) {
                // 获取安装包中的权限组
                PackageParser.PermissionGroup pg = pkg.permissionGroups.get(i);
                // 获取mPermissionGroups中对应的权限的组，如果没有对应的权限组则cur=null，如果有对应的权限组则cur不为空
                PackageParser.PermissionGroup cur = mPermissionGroups.get(pg.info.name);
                if (cur == null) {
                 // 如果在mPermissionGroups里面没有相应的权限组，则添加这个权限组
                    mPermissionGroups.put(pg.info.name, pg);
                    if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                        if (r == null) {
                            r = new StringBuilder(256);
                        } else {
                            r.append(' ');
                        }
                        r.append(pg.info.name);
                    }
                } else {
                 // 如果在mPermissionGroups里面有相应的权限组，则打印日志
                    Slog.w(TAG, "Permission group " + pg.info.name + " from package "
                            + pg.info.packageName + " ignored: original from "
                            + cur.info.packageName);
                    if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                        if (r == null) {
                            r = new StringBuilder(256);
                        } else {
                            r.append(' ');
                        }
                        r.append("DUP:");
                        r.append(pg.info.name);
                    }
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Permission Groups: " + r);
            }

            // 注册pkg里面的Permission到PackageManagerService上的mPermissionGroups上
            N = pkg.permissions.size();
            r = null;
            for (i=0; i<N; i++) {
                // 获取安装包中的权限
                PackageParser.Permission p = pkg.permissions.get(i);

                // Assume by default that we did not install this permission into the system.
                p.info.flags &= ~PermissionInfo.FLAG_INSTALLED;

                // Now that permission groups have a special meaning, we ignore permission
                // groups for legacy apps to prevent unexpected behavior. In particular,
                // permissions for one app being granted to someone just becuase they happen
                // to be in a group defined by another app (before this had no implications).
                if (pkg.applicationInfo.targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    p.group = mPermissionGroups.get(p.info.group);
                    // Warn for a permission in an unknown group.
                    if (p.info.group != null && p.group == null) {
                        Slog.w(TAG, "Permission " + p.info.name + " from package "
                                + p.info.packageName + " in an unknown group " + p.info.group);
                    }
                }

                // 判断是否有权限树，如果有权限树，则对应的 mSettings.mPermissionTrees；如果没有权限树对应mSettings.mPermissions
                ArrayMap<String, BasePermission> permissionMap =
                        p.tree ? mSettings.mPermissionTrees
                                : mSettings.mPermissions;
                // 获取系统中是否包含该权限，如果没有该权限bp为null。有该权限bp不为null
                BasePermission bp = permissionMap.get(p.info.name);

                // Allow system apps to redefine non-system permissions
                 // 允许系统应用程序重新定义非系统权限
                if (bp != null && !Objects.equals(bp.sourcePackage, p.info.packageName)) {
                   // 如果 bp权限不为空，并且这个权限的所对应的原包的包名和当前安装包的包名不一致
                     // currentOwnerIsSystem 是判断系统中这个权限对应的应用是否系统应用
                    final boolean currentOwnerIsSystem = (bp.perm != null
                            && isSystemApp(bp.perm.owner));
                    // 判断安装包是否是系统应用
                    if (isSystemApp(p.owner)) {
                        // 安装包是系统应用
                        if (bp.type == BasePermission.TYPE_BUILTIN && bp.perm == null) {           
                          // 如果是内置权限
                            // It's a built-in permission and no owner, take ownership now
                            bp.packageSetting = pkgSetting;
                            bp.perm = p;
                            bp.uid = pkg.applicationInfo.uid;
                            bp.sourcePackage = p.info.packageName;
                            p.info.flags |= PermissionInfo.FLAG_INSTALLED;
                        } else if (!currentOwnerIsSystem) {
                             // 如果这个权限对应的应用不是系统应用，而当前安装包又是系统权限
                            String msg = "New decl " + p.owner + " of permission  "
                                    + p.info.name + " is system; overriding " + bp.sourcePackage;
                            reportSettingsProblem(Log.WARN, msg);
                            bp = null;
                        }
                    }
                }

               // 如果系统中没有这个权限
                if (bp == null) {
                   // 创建这个权限
                    bp = new BasePermission(p.info.name, p.info.packageName,
                            BasePermission.TYPE_NORMAL);
                    // 把新创建的权限添加进去
                    permissionMap.put(p.info.name, bp);
                }


                if (bp.perm == null) {
                    // 如果BasePermission中的PackageParser.Permission变量perm为空。
                    if (bp.sourcePackage == null
                            || bp.sourcePackage.equals(p.info.packageName)) {
                       // 如果bp的sourcePackage为空，则应该是上一步刚刚new的
                       // 如果bp的sourcePackage等于当前安装包包名，应该是升级安装的
                        //  无论上面哪一步，都要进行变量初始化
                        BasePermission tree = findPermissionTreeLP(p.info.name);
                        if (tree == null
                                || tree.sourcePackage.equals(p.info.packageName)) {
                            bp.packageSetting = pkgSetting;
                            bp.perm = p;
                            bp.uid = pkg.applicationInfo.uid;
                            bp.sourcePackage = p.info.packageName;
                            p.info.flags |= PermissionInfo.FLAG_INSTALLED;
                            if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                                if (r == null) {
                                    r = new StringBuilder(256);
                                } else {
                                    r.append(' ');
                                }
                                r.append(p.info.name);
                            }
                        } else {
                            Slog.w(TAG, "Permission " + p.info.name + " from package "
                                    + p.info.packageName + " ignored: base tree "
                                    + tree.name + " is from package "
                                    + tree.sourcePackage);
                        }
                    } else {
                        Slog.w(TAG, "Permission " + p.info.name + " from package "
                                + p.info.packageName + " ignored: original from "
                                + bp.sourcePackage);
                    }
                } else if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append("DUP:");
                    r.append(p.info.name);
                }
                if (bp.perm == p) {
                    bp.protectionLevel = p.info.protectionLevel;
                }
            }

            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Permissions: " + r);
            }
          
            //  注册pkg里面的instrumentation到PackageManagerService的mInstrumentation中
            // Instrumentation用来跟踪本应用内的application及activity的生命周期
            N = pkg.instrumentation.size();
            r = null;
            for (i=0; i<N; i++) {
                PackageParser.Instrumentation a = pkg.instrumentation.get(i);
                a.info.packageName = pkg.applicationInfo.packageName;
                a.info.sourceDir = pkg.applicationInfo.sourceDir;
                a.info.publicSourceDir = pkg.applicationInfo.publicSourceDir;
                a.info.splitSourceDirs = pkg.applicationInfo.splitSourceDirs;
                a.info.splitPublicSourceDirs = pkg.applicationInfo.splitPublicSourceDirs;
                a.info.dataDir = pkg.applicationInfo.dataDir;

                // TODO: Update instrumentation.nativeLibraryDir as well ? Does it
                // need other information about the application, like the ABI and what not ?
                a.info.nativeLibraryDir = pkg.applicationInfo.nativeLibraryDir;
                mInstrumentation.put(a.getComponentName(), a);
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append(a.info.name);
                }
            }
            if (r != null) {
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Instrumentation: " + r);
            }

            // 如果有包内广播
            if (pkg.protectedBroadcasts != null) {
                N = pkg.protectedBroadcasts.size();
                for (i=0; i<N; i++) {
                    mProtectedBroadcasts.add(pkg.protectedBroadcasts.get(i));
                }
            }

            // 设置扫描时间
            pkgSetting.setTimeStamp(scanFileTime);

            // Create idmap files for pairs of (packages, overlay packages).
            // Note: "android", ie framework-res.apk, is handled by native layers.
             // 如果有 overlay设置
            if (pkg.mOverlayTarget != null) {
                // This is an overlay package.
                if (pkg.mOverlayTarget != null && !pkg.mOverlayTarget.equals("android")) {
                    if (!mOverlays.containsKey(pkg.mOverlayTarget)) {
                        mOverlays.put(pkg.mOverlayTarget,
                                new ArrayMap<String, PackageParser.Package>());
                    }
                    ArrayMap<String, PackageParser.Package> map = mOverlays.get(pkg.mOverlayTarget);
                    map.put(pkg.packageName, pkg);
                    PackageParser.Package orig = mPackages.get(pkg.mOverlayTarget);
                    if (orig != null && !createIdmapForPackagePairLI(orig, pkg)) {
                        throw new PackageManagerException(INSTALL_FAILED_UPDATE_INCOMPATIBLE,
                                "scanPackageLI failed  to createIdmap");
                   }
                }
            } else if (mOverlays.containsKey(pkg.packageName) &&
                    !pkg.packageName.equals("android")) {
                // This is a regular package, with one or more known overlay packages.
                createIdmapsForPackageLI(pkg);
            }
        }
        return pkg;
    }
```

将该方法内部分为17步，主要流程如下：

* 第一步：检查代码路径是否存在。如果不存在则抛出异常
* 第二步：初始化PackageParser.Package的pkg的一些属性主要是applicationInfo信息。
* 第三步：设置ResolverActivity信息。
* 第四步：如果是系统应用程序，则变更ResolverActivity信息
* 第五步：如果是更新安装(即只安装已经存在的包)，检查它的PackageSetting信息，如果路径不一致，则抛出异常
* 第六步：初始化包的安装目录(代码目录与资源目录)。
* 第七步：检查是否需要重命名
* 第八步：检测所有共享库：并且映射到真实的路径
* 第九步：如果是升级更新安装，则检查升级更新包的签名，如果是新安装，则验证签名。其中shouldCheckUpgradeKeySetLP方法可以参考APK安装流程详解15——PMS中的新安装流程下(装载)补充 中***二、PackageManagerService#shouldCheckUpgradeKeySetLP(PackageSetting, int) 方法解析\***
* 第十步：检查安装包中的provider是不是和现在系统中已经存在包的provider冲突
* 第十一步：检测当前安装包对其他包的所拥有的权限(比如系统应用)
* 第十二步：创建data目录，并且重新调整uid，调用createDataDirsLI进行包的安装。其中framework-res.apk比较特殊，它的data目录位于/data/system/，其他APK的data目录都位于/data/data/packageName下
* 第十三步：设置Native Library的路径。即so文件目录。
* 第十四步：创建用户数据，主要是调用createUserData方法来实现的
* 第十五步：对包进行dex优化，主要是调用performDexOpt方法来进行，最终还是要调用Install的dexopt函数，这里具体请参考APK安装流程详解15——PMS中的新安装流程下(装载)补充中 ***五、PackageDexOptimizer#performDexOp(PackageParser.Package, String[], String[], boolean, String,CompilerStats.PackageStats)方法解析\***
* 第十六步：如果该包已经存在了，需要杀死该进程
* 第十七步：将一个安装包的内容从pkg里面映射到PackageManagerService里面。这样一个安装包中的所有组件信息里面主要分为：
  * 1、解析provider，并映射到PackageManagerService的变量xx里面
  * 2、解析service，并映射到PackageManagerService的变量xx里面
  * 3、解析receive，并映射到PackageManagerService的变量xx里面
  * 4、解析activity，并映射到PackageManagerService的变量xx里面
  * 5、解析GroupPermission与Permission，并映射到PackageManagerService的变量xx里面
  * 6、解析instrumentation，并映射到PackageManagerService的变量xx里面

## 八、总结：

PackageManagerService的构造函数它完成了了对"/system/app"、"/data/app"、"/system/framework"、"/data/app-private"下的apk文件解析，大体流程如下：

* 1、创建 Java 层的 installer 与 C 层 installdsocket 连接，是的上层的 install、remove、dexopt 等功能最终由 installd 在底层实现。

* 2、创建PackageHandler对象并建立对应的消息循环，用于处理外部apk安装请求信息，如adb install、packageinstaller安装apk时会发送消息。

* 3、解析相应文件(/system/etc/permission和(framework/base/data/etc/下的文件)，包括platform.xml和系统支持的各种硬件模块的feature，主要工作：
  * ① 建立底层的uid和 group id同行上层permission之间的映射，可以指定一个权限与几个id的对应。当一个APK被授予这个权限时，它也同时属于这几个组。
  * ② 给一些底层用户分配权限，如给shell授予各种permission权限，把一个权限赋予uid，当进程只是用这个uid运行时，就具备了这个权限。
  * ③ libary，系统增加的一些应用需要link扩展的jar。
  * ④ feature，系统每增加一个硬件，都要添加相应的feature，将解析结果放入mSystemPermissions，mShareLibrariest，mSettings.mPermissions，mAvailableFeatures等几个集合供系统查询和权限配置使用。
* 4、检查/data/system/packages.xml是否存在，这个文件是在解析apk时由writeLP()创建的，里面记录了系统的permissions，以及每个apk的name，codePath，flags，version，userId等信息，这些信息主要通过apk的AndroidManifest.xml解析获取，解析完APK后将更新信息写入这个文件并保存到flash，下次开机直接从里面读取相关信息添加到内存相关列表中。当APK升级、安装或删除时会更新这个文件。
* 5、检查BootClassPath、mShareLibraries及/system/framework下的jar是否需要检查dexopt，需要的则通过dexopt进行优化
* 6、对"/system/framework"、"/system/app"、"/data/app"、"/data/app-private"目录下的apk逐个解析，主要是解析每个apk的AndroidManifest文件，处理assert/res等资源文件，建立起每个APK的配置结构信息。并将每个APK的配置信息添加到全局列表中进行管理
* 7、将解析每个APK信息保存到packages.xml和packages.list文件里，packages.xml记录了如下数据：packageName，userId，debugFlag，dataPath(包的数据路径)

## 参考文章

1. [APK安装流程详解8——PackageManagerService的启动流程(下)](https://www.jianshu.com/p/b9b2b0f650e9)

