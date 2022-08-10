# APK安装流程详解13——PMS中的新安装流程下(装载)

本片文章的主要内容如下：

```
1、装载代码的入口
2、PackageManagerService#processPendingInstall(InstallArgs,int)方法
3、PackageManagerService#installPackageLI(InstallArgs,PackageInstalledInfo)方法解析
4、PackageManagerService#installNewPackageLI(PackageParser.Package, int, int, UserHandle, String, String,PackageInstalledInfo)方法解析
5、PackageManagerService#scanPackageLI(File scanFile, int parseFlags, int scanFlags,long currentTime, UserHandle user) 方法解析
6、PackageManagerService#updateSettingsLIupdateSettingsLI(PackageParser.Package, String,String, int[], boolean[], PackageInstalledInfo,UserHandle)方法解析
7、 PackageHandler的处理Message的what值为POST_INSTALL的情况解析
8、总结
```

## 一、装载代码的入口

上篇文章说到进行完新安装流程中的"拷贝代码流程后"，分两种情况：

* 1、如果前面是走"验证流程"，则会调用processPendingInstall(args, ret)方法，如下：

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1537行

```cpp
                        if (getDefaultVerificationResponse() == PackageManager.VERIFICATION_ALLOW) {
                            Slog.i(TAG, "Continuing with installation of " + originUri);
                            state.setVerifierResponse(Binder.getCallingUid(),
                                    PackageManager.VERIFICATION_ALLOW_WITHOUT_SUFFICIENT);
                            broadcastPackageVerified(verificationId, originUri,
                                    PackageManager.VERIFICATION_ALLOW,
                                    state.getInstallArgs().getUser());
                            try {
                                ret = args.copyApk(mContainerService, true);
                            } catch (RemoteException e) {
                                Slog.e(TAG, "Could not contact the ContainerService");
                            }
                        } else {
                            broadcastPackageVerified(verificationId, originUri,
                                    PackageManager.VERIFICATION_REJECT,
                                    state.getInstallArgs().getUser());
                        }

                        processPendingInstall(args, ret);
                        mHandler.sendEmptyMessage(MCS_UNBIND);
```

* 2、如果不走"验证流程"，则会调用handleReturnCode()方法，如下：

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10253行

```java
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
```

而在handleReturnCode()方法里面也是调用processPendingInstall(args, ret)方法，如下：

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10253行

```java
        @Override
        void handleReturnCode() {
            // If mArgs is null, then MCS couldn't be reached. When it
            // reconnects, it will try again to install. At that point, this
            // will succeed.
            if (mArgs != null) {
                processPendingInstall(mArgs, mRet);
            }
        }
```

看到handleReturnCode()来不也是调用processPendingInstall(InstallArgs,int)方法的，所以我们说：**"装载代码"的入口是processPendingInstall(InstallArgs,int)方法。**

## 二、PackageManagerService#processPendingInstall(InstallArgs,int)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10156行

```java
    private void processPendingInstall(final InstallArgs args, final int currentStatus) {
        // Queue up an async operation since the package installation may take a little while.
        // 向mHandler中发送一个Runnable对象，这里是异步操作，因为安装一个程序包可能需要一些时间
        mHandler.post(new Runnable() {
            public void run() {
               // 第一部分
               // 清除任务
                mHandler.removeCallbacks(this);
                 // Result object to be returned
                // 创建一个PackageInstalledInfo对象
                PackageInstalledInfo res = new PackageInstalledInfo();
                res.returnCode = currentStatus;
                res.uid = -1;
                res.pkg = null;
                res.removedInfo = new PackageRemovedInfo();
                // 第二部分
                if (res.returnCode == PackageManager.INSTALL_SUCCEEDED) {
                    // 预安装阶段，主要是检查安装包的状态，确保安装环境正常，如果安装环境有问题会清理拷贝文件
                    args.doPreInstall(res.returnCode);
                    synchronized (mInstallLock) {
                       // 安装阶段，调用installPackageLI进行安装
                        installPackageLI(args, res);
                    }
                    // 安装收尾
                    args.doPostInstall(res.returnCode, res.uid);
                }
                // A restore should be performed at this point if (a) the install
                // succeeded, (b) the operation is not an update, and (c) the new
                // package has not opted out of backup participation.
                final boolean update = res.removedInfo.removedPackage != null;
                final int flags = (res.pkg == null) ? 0 : res.pkg.applicationInfo.flags;
                boolean doRestore = !update
                        && ((flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0);

                // Set up the post-install work request bookkeeping.  This will be used
                // and cleaned up by the post-install event handling regardless of whether
                // there's a restore pass performed.  Token values are >= 1.
                // 第三部分
                // 计算一个ID号
                int token;
                if (mNextInstallToken < 0) mNextInstallToken = 1;
                token = mNextInstallToken++;

                PostInstallData data = new PostInstallData(args, res);
                // 保存到mRunningInstalls结构中，以token为key，而mRunningInstalls是SparseArray结构的
                mRunningInstalls.put(token, data);
                if (DEBUG_INSTALL) Log.v(TAG, "+ starting restore round-trip " + token);

                 // 第四部分
                 // 安装成功，且需要备份的情况
                if (res.returnCode == PackageManager.INSTALL_SUCCEEDED && doRestore) {
                    // Pass responsibility to the Backup Manager.  It will perform a
                    // restore if appropriate, then pass responsibility back to the
                    // Package Manager to run the post-install observer callbacks
                    // and broadcasts.
                    IBackupManager bm = IBackupManager.Stub.asInterface(
                            ServiceManager.getService(Context.BACKUP_SERVICE));
                    if (bm != null) {
                        if (DEBUG_INSTALL) Log.v(TAG, "token " + token
                                + " to BM for possible restore");
                        try {
                            if (bm.isBackupServiceActive(UserHandle.USER_OWNER)) {
                                bm.restoreAtInstall(res.pkg.applicationInfo.packageName, token);
                            } else {
                                doRestore = false;
                            }
                        } catch (RemoteException e) {
                            // can't happen; the backup manager is local
                        } catch (Exception e) {
                            Slog.e(TAG, "Exception trying to enqueue restore", e);
                            doRestore = false;
                        }
                    } else {
                        Slog.e(TAG, "Backup Manager not found!");
                        doRestore = false;
                    }
                }
              // 第五部分
                if (!doRestore) {
                    // No restore possible, or the Backup Manager was mysteriously not
                    // available -- just fire the post-install work request directly.
                    if (DEBUG_INSTALL) Log.v(TAG, "No restore - queue post-install for " + token);
                    Message msg = mHandler.obtainMessage(POST_INSTALL, token, 0);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }
```

processPendingInstall()方法内容不多，主要是post了一个消息，这样安装过程将以异步的方式继续执行。

将这个方法大致分为4个部分

* 第一部分：初始化阶段

* 第二部分：安装阶段
   这个阶段有可以细分为3个阶段如下：
  * 预安装阶段：检查当前安装包的状态以及确保SDCARD的挂载，并返回状态信息。在安装前确保安装环境的可靠
  * 安装阶段：对mInstallLock加锁，表明同时只能由一个安装包进行安装，然后调用installPackageLI方法完成具体的安装操作。
  * 安装收尾阶段：检查状态，如果安装失败，删除相关目录文件。

* 第三部分：加入缓存
   用InstallArgs和PackageInstalledInfo构造一个PostInstallData对象，让后把这个PostInstallData对方存放在mRunningInstalls里面，mRunningInstalls是一个SparseArray结构，key是token，这样后续查询相关信息的东西可以直接访问mRunningInstalls就可以了。

* 第四部分：备份部分
   如果需要备份，则调用BackupManagerService来完成备份，这里注意备份完毕后，设置doRestore = false

* 第五部分：安装阶段结束
   无论该APK是否已经安装成功(失败)，都会向mHandler发送一个what值为POST_INSTALL的Message消息。该Message消息的arg1为token，这样它可以从mRunningInstalls中取得PostInstallData对象

这方法里面涉及到四个重要的函数，即

* args.doPreInstall(res.returnCode)

* args.doPostInstall(res.returnCode, res.uid)

* installPackageLI(args, res)

* PackageHandler的处理Message的what值为POST_INSTALL的情况

其中installPackageLI(args, res)方法至关重要，installPackageLI方法进行APK的装载，该函数内部将调用InstallArgs的doRename对临时文件进行改名。另外，还需要扫描此APK文件。这样实现了APK的信息映射到PackageManagerService内部了。

args其实是FileInstallArgs，所以上面的args.doPreInstall(res.returnCode)方法和args.doPostInstall(res.returnCode, res.uid)方法其实对应的都是FileInstallArgs的方法。

### (一)、FileInstallArgs#doPreInstall(int)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11108行

```cpp
        int doPreInstall(int status) {
            if (status != PackageManager.INSTALL_SUCCEEDED) {
                cleanUp();
            }
            return status;
        }
```

看到这个方法里面判断是否已经成功安装完成。如果没有成功安装完成则调用cleanUp()方法，但是安装过程中的预安装阶段status一定是等于PackageManager.INSTALL_SUCCEEDED，因为只有等于才能进入到这个方法。
 毕竟是如下的代码

```java
                if (res.returnCode == PackageManager.INSTALL_SUCCEEDED) {
                    args.doPreInstall(res.returnCode);
                }
```

### (二)、FileInstallArgs#doPostInstall(int,int)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11161行

```cpp
        int doPostInstall(int status, int uid) {
            if (status != PackageManager.INSTALL_SUCCEEDED) {
                cleanUp();
            }
            return status;
        }
```

在调用这个的方法时候，当然有一种可能就是安装成功了，如果安装成了，则这个方法什么也不做；还有一种可能就是安装失败了，安装失败了即status不等于PackageManager.INSTALL_SUCCEEDED，所以会调用 cleanUp()方法。

假设安装失败了，来看下cleanUp()方法里面做了什么操作，不过看方法名字，猜测是"清理"操作。

#### FileInstallArgs#cleanUp()方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11178行

```java
        private boolean cleanUp() {
            //判断代码文件是否存在，如果不存在，直接返回，不需要清理
            if (codeFile == null || !codeFile.exists()) {
                return false;
            }
            // 如果代码文件是个文件夹，则调用mInstaller.rmPackageDir进行清理
            if (codeFile.isDirectory()) {
                mInstaller.rmPackageDir(codeFile.getAbsolutePath());
            } else {
                // 如果代码文件是文件，则直接删除文件夹
                codeFile.delete();
            }
            // 如果存在资源文件，则删除资源文件
            if (resourceFile != null && !FileUtils.contains(codeFile, resourceFile)) {
                resourceFile.delete();
            }
            // 返回清除成功
            return true;
        }
```

可见cleanUp方法就是清理代码文件和资源文件。

这里有个方法就是代码文件是个文件夹的时候，调用mInstaller.rmPackageDir(codeFile.getAbsolutePath())进行删除工作，在前面文章[APK安装流程详解5——Installer、InstallerConnection和Installd守护进程]知道mInstallerq其实只是一个代理类，具体制定的是Native的intalld。那来找下Installer的rmPackageDir方法对应的intalld什么操作？

代码在[installd.cpp](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/native/cmds/installd/installd.cpp) 218行

```cpp
struct cmdinfo cmds[] = {
    { "ping",                 0, do_ping },
    { "install",              5, do_install },
    { "dexopt",               10, do_dexopt },
    { "markbootcomplete",     1, do_mark_boot_complete },
    { "movedex",              3, do_move_dex },
    { "rmdex",                2, do_rm_dex },
    { "remove",               3, do_remove },
    { "rename",               2, do_rename },
    { "fixuid",               4, do_fixuid },
    { "freecache",            2, do_free_cache },
    { "rmcache",              3, do_rm_cache },
    { "rmcodecache",          3, do_rm_code_cache },
    { "getsize",              8, do_get_size },
    { "rmuserdata",           3, do_rm_user_data },
    { "cpcompleteapp",        6, do_cp_complete_app },
    { "movefiles",            0, do_movefiles },
    { "linklib",              4, do_linklib },
    { "mkuserdata",           5, do_mk_user_data },
    { "mkuserconfig",         1, do_mk_user_config },
    { "rmuser",               2, do_rm_user },
    { "idmap",                3, do_idmap },
    { "restorecondata",       4, do_restorecon_data },
    { "createoatdir",         2, do_create_oat_dir },
    { "rmpackagedir",         1, do_rm_package_dir },
    { "linkfile",             3, do_link_file }
};
```

注意下倒数第二行，**rmpackagedir**对应着**do_rm_package_dir**

代码在[installd.cpp](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/native/cmds/installd/installd.cpp) 176 行

```cpp
static int do_rm_package_dir(char **arg, char reply[REPLY_MAX] __unused)
{
    /* oat_dir */
    return rm_package_dir(arg[0]);
}
```

那我们看下rm_package_dir函数

代码在[commands.cpp](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/native/cmds/installd/commands.cpp) 1825 行

```cpp
int rm_package_dir(const char* apk_path)
{
    if (validate_apk_path(apk_path)) {
        ALOGE("invalid apk path '%s' (bad prefix)\n", apk_path);
        return -1;
    }
    return delete_dir_contents(apk_path, 1 /* also_delete_dir */ , NULL /* exclusion_predicate */);
}
```

首先通过validate_apk_path来检验下路径，然后调用delete_dir_contents函数来删除，这个函数在[utils.cpp](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/native/cmds/installd/utils.cpp) 291行。

下面来看下installPackageLI方法的具体实现。

## 三、PackageManagerService#installPackageLI(InstallArgs,PackageInstalledInfo)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12224行

```dart
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
         // ******* 第一步 *******
         //获取 installFlags属性，这个属性表明APP安装到哪里
        final int installFlags = args.installFlags;
         // 安装包应用程序的 包名
        final String installerPackageName = args.installerPackageName;
        // volume的Uuid，后续讲解Android存储系列的详细讲解
        final String volumeUuid = args.volumeUuid;
        //根据安装包代码的路径生成一个文件
        final File tmpPackageFile = new File(args.getCodePath());
         // 是否需要锁定
        final boolean forwardLocked = ((installFlags & PackageManager.INSTALL_FORWARD_LOCK) != 0);
         // 是否安装到外部存储
        final boolean onExternal = (((installFlags & PackageManager.INSTALL_EXTERNAL) != 0)
                || (args.volumeUuid != null));
        // 新安装还是更新安装的标志位
        boolean replace = false;
        int scanFlags = SCAN_NEW_INSTALL | SCAN_UPDATE_SIGNATURE;
        // 判断是是否是移动一个APP，由于我们是讲解的新安装，所以args.move=null
        if (args.move != null) {
            // moving a complete application; perfom an initial scan on the new install location
            scanFlags |= SCAN_INITIAL;
        }
        // Result object to be returned
        res.returnCode = PackageManager.INSTALL_SUCCEEDED;

        if (DEBUG_INSTALL) Slog.d(TAG, "installPackageLI: path=" + tmpPackageFile);
        // Retrieve PackageSettings and parse package
        // 获取解析包配置的标志位
        final int parseFlags = mDefParseFlags | PackageParser.PARSE_CHATTY
                | (forwardLocked ? PackageParser.PARSE_FORWARD_LOCK : 0)
                | (onExternal ? PackageParser.PARSE_EXTERNAL_STORAGE : 0);
        PackageParser pp = new PackageParser();
        // 设置解析包的独立进程属性
        pp.setSeparateProcesses(mSeparateProcesses);
         // 设置解析包的屏幕属性
        pp.setDisplayMetrics(mMetrics);

        final PackageParser.Package pkg;
        // ******* 第二步 *******
        try {
            // 解析APK，主要是解析AndroidManifest.xml文件，将结果记录在PackageParser.Package中
            pkg = pp.parsePackage(tmpPackageFile, parseFlags);
        } catch (PackageParserException e) {
            res.setError("Failed parse during installPackageLI", e);
            return;
        }

        // ******* 第三步 *******
        // Mark that we have an install time CPU ABI override.
        pkg.cpuAbiOverride = args.abiOverride;

        String pkgName = res.name = pkg.packageName;
        // 如果这个待安装的APP不是测试包，但是如果环境为仅允许测试包则返回
        if ((pkg.applicationInfo.flags&ApplicationInfo.FLAG_TEST_ONLY) != 0) {
            if ((installFlags & PackageManager.INSTALL_ALLOW_TEST) == 0) {
                res.setError(INSTALL_FAILED_TEST_ONLY, "installPackageLI");
                return;
            }
        }

        try {
            //收集APK的签名信息
            // collectCertificates做签名验证，collectManifestDigest主要是做包的项目清单摘要的收集，主要适合用来比较两个包的是否一样
            pp.collectCertificates(pkg, parseFlags);
            pp.collectManifestDigest(pkg);
        } catch (PackageParserException e) {
            res.setError("Failed collect during installPackageLI", e);
            return;
        }
        /** 如果安装程序此前传入了一个项目清单文件(manifest)，那么将解析到项目清单文件与传入的进行对比。
          * 安装器的确传入了一个清单，PackageInstallerActivity中也解析出APK，那么记录了这个清单，然后进行对比，判断是否是同一个APK
          **/
        /* If the installer passed in a manifest digest, compare it now. */
        if (args.manifestDigest != null) {
            if (DEBUG_INSTALL) {
                final String parsedManifest = pkg.manifestDigest == null ? "null"
                        : pkg.manifestDigest.toString();
                Slog.d(TAG, "Comparing manifests: " + args.manifestDigest.toString() + " vs. "
                        + parsedManifest);
            }

            if (!args.manifestDigest.equals(pkg.manifestDigest)) {
                res.setError(INSTALL_FAILED_PACKAGE_CHANGED, "Manifest digest changed");
                return;
            }
        } else if (DEBUG_INSTALL) {
            final String parsedManifest = pkg.manifestDigest == null
                    ? "null" : pkg.manifestDigest.toString();
            Slog.d(TAG, "manifestDigest was not present, but parser got: " + parsedManifest);
        }

        // Get rid of all references to package scan path via parser.
        pp = null;
        String oldCodePath = null;
        boolean systemApp = false;
        synchronized (mPackages) {
            // Check if installing already existing package
            if ((installFlags & PackageManager.INSTALL_REPLACE_EXISTING) != 0) {
                 // 如果安装的升级应用，继续使用以前的老包名
                String oldName = mSettings.mRenamedPackages.get(pkgName);
                 // 如果要进行安装的应用，已经存在，将是替换安装，则设置replace=true
                if (pkg.mOriginalPackages != null
                        && pkg.mOriginalPackages.contains(oldName)
                        && mPackages.containsKey(oldName)) {
                    // This package is derived from an original package,
                    // and this device has been updating from that original
                    // name.  We must continue using the original name, so
                    // rename the new package here.
                    // 包名设置为老的包名
                    pkg.setPackageName(oldName);
                    pkgName = pkg.packageName;
                    replace = true;
                    if (DEBUG_INSTALL) Slog.d(TAG, "Replacing existing renamed package: oldName="
                            + oldName + " pkgName=" + pkgName);
                } else if (mPackages.containsKey(pkgName)) {
                    // This package, under its official name, already exists
                    // on the device; we should replace it.
                    replace = true;
                    if (DEBUG_INSTALL) Slog.d(TAG, "Replace existing pacakge: " + pkgName);
                }

                // Prevent apps opting out from runtime permissions
                if (replace) {
                  // 如果是替换，即升级安装
                    PackageParser.Package oldPackage = mPackages.get(pkgName);
                    // 分别获取老、新版本的TargetSdk 
                    final int oldTargetSdk = oldPackage.applicationInfo.targetSdkVersion;
                    final int newTargetSdk = pkg.applicationInfo.targetSdkVersion;
                     // 如果老的TargetSdk 大于android 5.1 而新的TargetSdk 小于5.1,
                    if (oldTargetSdk > Build.VERSION_CODES.LOLLIPOP_MR1
                            && newTargetSdk <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        res.setError(PackageManager.INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE,
                                "Package " + pkg.packageName + " new target SDK " + newTargetSdk
                                        + " doesn't support runtime permissions but the old"
                                        + " target SDK " + oldTargetSdk + " does.");
                        return;
                    }
                }
            }

            PackageSetting ps = mSettings.mPackages.get(pkgName);
            // 如果 ps 不为null，同样说明，已经存在一个具有相同安装包包名的程序，被安装，所以还是处理覆盖安装的问题。
            // 这里主要验证包名的签名，不一致的话，是不能覆盖安装的，另外版本号也不能比安装的地，否则不能替换安装
            if (ps != null) {
                if (DEBUG_INSTALL) Slog.d(TAG, "Existing package: " + ps);

                // Quick sanity check that we're signed correctly if updating;
                // we'll check this again later when scanning, but we want to
                // bail early here before tripping over redefined permissions.
                //检查密钥集合是否一致
                if (shouldCheckUpgradeKeySetLP(ps, scanFlags)) {
                    if (!checkUpgradeKeySetLP(ps, pkg)) {
                        res.setError(INSTALL_FAILED_UPDATE_INCOMPATIBLE, "Package "
                                + pkg.packageName + " upgrade keys do not match the "
                                + "previously installed version");
                        return;
                    }
                } else {
                    try {
                        verifySignaturesLP(ps, pkg);
                    } catch (PackageManagerException e) {
                        res.setError(e.error, e.getMessage());
                        return;
                    }
                }
 
                // 判断安装的应用是否存在同名的应用，如果存在，判断应用是否带有系统应用的标志
                oldCodePath = mSettings.mPackages.get(pkgName).codePathString;
                if (ps.pkg != null && ps.pkg.applicationInfo != null) {
                    systemApp = (ps.pkg.applicationInfo.flags &
                            ApplicationInfo.FLAG_SYSTEM) != 0;
                }
                res.origUsers = ps.queryInstalledUsers(sUserManager.getUserIds(), true);
            }

            // ******* 第四步 *******
            // Check whether the newly-scanned package wants to define an already-defined perm
            // 检查APK中定义的所有的权限是否已经被其他应用定义了，如果重定义的是系统应用定义的权限，那么忽略本app定义的这个权限。如果重定义的是非系统引用的权限，那么本次安装就以失败返回。
            int N = pkg.permissions.size();
            for (int i = N-1; i >= 0; i--) {
                PackageParser.Permission perm = pkg.permissions.get(i);
                BasePermission bp = mSettings.mPermissions.get(perm.info.name);
                if (bp != null) {
                    // If the defining package is signed with our cert, it's okay.  This
                    // also includes the "updating the same package" case, of course.
                    // "updating same package" could also involve key-rotation.
                    final boolean sigsOk;
                    if (bp.sourcePackage.equals(pkg.packageName)
                            && (bp.packageSetting instanceof PackageSetting)
                            && (shouldCheckUpgradeKeySetLP((PackageSetting) bp.packageSetting,
                                    scanFlags))) {
                        sigsOk = checkUpgradeKeySetLP((PackageSetting) bp.packageSetting, pkg);
                    } else {
                        sigsOk = compareSignatures(bp.packageSetting.signatures.mSignatures,
                                pkg.mSignatures) == PackageManager.SIGNATURE_MATCH;
                    }
                    if (!sigsOk) {
                        // If the owning package is the system itself, we log but allow
                        // install to proceed; we fail the install on all other permission
                        // redefinitions.
                        if (!bp.sourcePackage.equals("android")) {
                            res.setError(INSTALL_FAILED_DUPLICATE_PERMISSION, "Package "
                                    + pkg.packageName + " attempting to redeclare permission "
                                    + perm.info.name + " already owned by " + bp.sourcePackage);
                            res.origPermission = perm.info.name;
                            res.origPackage = bp.sourcePackage;
                            return;
                        } else {
                            Slog.w(TAG, "Package " + pkg.packageName
                                    + " attempting to redeclare system permission "
                                    + perm.info.name + "; ignoring new declaration");
                            pkg.permissions.remove(i);
                        }
                    }
                }
            }
        }
        // 如果是带带有系统应用标志的应用，却要安装在SD卡上，则报错返回，安装失败原因错误的安装路径
        if (systemApp && onExternal) {
            // Disable updates to system apps on sdcard
            res.setError(INSTALL_FAILED_INVALID_INSTALL_LOCATION,
                    "Cannot install updates to system apps on sdcard");
            return;
        }

         // ******* 第五步 *******
         // 如果是移动APP
        if (args.move != null) {
            // We did an in-place move, so dex is ready to roll
            scanFlags |= SCAN_NO_DEX;
            scanFlags |= SCAN_MOVE;

            synchronized (mPackages) {
                final PackageSetting ps = mSettings.mPackages.get(pkgName);
                if (ps == null) {
                    res.setError(INSTALL_FAILED_INTERNAL_ERROR,
                            "Missing settings for moved package " + pkgName);
                }

                // We moved the entire application as-is, so bring over the
                // previously derived ABI information.
                pkg.applicationInfo.primaryCpuAbi = ps.primaryCpuAbiString;
                pkg.applicationInfo.secondaryCpuAbi = ps.secondaryCpuAbiString;
            }

        } else if (!forwardLocked && !pkg.applicationInfo.isExternalAsec()) {
            // Enable SCAN_NO_DEX flag to skip dexopt at a later stage
            scanFlags |= SCAN_NO_DEX;

            try {
                derivePackageAbi(pkg, new File(pkg.codePath), args.abiOverride,
                        true /* extract libs */);
            } catch (PackageManagerException pme) {
                Slog.e(TAG, "Error deriving application ABI", pme);
                res.setError(INSTALL_FAILED_INTERNAL_ERROR, "Error deriving application ABI");
                return;
            }

            // Run dexopt before old package gets removed, to minimize time when app is unavailable
            int result = mPackageDexOptimizer
                    .performDexOpt(pkg, null /* instruction sets */, false /* forceDex */,
                            false /* defer */, false /* inclDependencies */,
                            true /* boot complete */);
            // 实际为dex2oat操作，用来将apk中的dex文件转换为oat文件
            if (result == PackageDexOptimizer.DEX_OPT_FAILED) {
                res.setError(INSTALL_FAILED_DEXOPT, "Dexopt failed for " + pkg.codePath);
                return;
            }
        }

         // ******* 第六步 *******
        // 重命名，将/data/app/vmdl{安装会话}.tmp重命名为/data/apppp/包名-suffix,suffix为1、2...
        if (!args.doRename(res.returnCode, pkg, oldCodePath)) {
            // 如果重命名失败，则报错，退出，安装失败原因：无法重命名
            res.setError(INSTALL_FAILED_INSUFFICIENT_STORAGE, "Failed rename");
            return;
        }

         // ******* 第七步 *******
        startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg);

        // ******* 第八步 *******
        if (replace) {
          // 覆盖安装
            replacePackageLI(pkg, parseFlags, scanFlags | SCAN_REPLACING, args.user,
                    installerPackageName, volumeUuid, res);
        } else {
            // 首次安装
            installNewPackageLI(pkg, parseFlags, scanFlags | SCAN_DELETE_DATA_ON_FAILURES,
                    args.user, installerPackageName, volumeUuid, res);
        }

        // ******* 第九步 *******
        synchronized (mPackages) {
            final PackageSetting ps = mSettings.mPackages.get(pkgName);
            if (ps != null) {
                res.newUsers = ps.queryInstalledUsers(sUserManager.getUserIds(), true);
            }
        }
    }
```

上面注释已经很清楚了，这里主要这个方法的整体流程分为9个部分如下：

* 第一步：初始化变量

* 第二步：解析APK

* 第三步：判断是新安装还是升级安装，无论是新安装还是升级安装都是需要获取签名信息。

* 第四步：检查权限

* 第五步：根据不同的安装标志，来进行操作，分为三种情况
  * 移动操作：
  * 非锁定安装且没有安装在SD卡上：**新安装走这里**，这里面主要做两个操作：第①步是**so拷贝**，第②步是**进行dex优化**，第③步机械性dex2oat操作，将dex文件转化为oat。
  * 如果上面两个条件都不满足，则什么也不做

* 第六步：重命名安装：将/data/app/vmdl{安装会话}.tmp重命名为/data/apppp/包名-suffix,suffix为1、2...

* 第七步：开始intent filter验证

* 第八步：这里根据不同的安装方式进行不同的方式，主要有两种情况
  * 覆盖安装即更新安装：调用replacePackageLI方法进行覆盖安装
  * 首次安装：调用installNewPackageLI方法进行首次安装

* 第九步：安装收尾，调用PackageSetting的queryInstalledUsers设置安装用户

这里面涉及到7个比较复杂的方法：

* 1、pp.setSeparateProcesses(mSeparateProcesses)：设置独立进程属性，这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中的 **一、PackageParser#setSeparateProcesses(String[] procs)方法解析** 。

* 2、shouldCheckUpgradeKeySetLP(ps, scanFlags)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中**二、PackageManagerService#shouldCheckUpgradeKeySetLP(PackageSetting, int) 方法解析**

* 3、checkUpgradeKeySetLP(ps, pkg)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中 **三、PackageManagerService#checkUpgradeKeySetLP(PackageSetting, PackageParser.Package) 方法解析**

* 4、verifySignaturesLP(ps, pkg)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中 **四、PackageManagerService#verifySignaturesLP(PackageSetting, PackageParser.Package)方法解析**

* 5、mPackageDexOptimizer.performDexOpt(pkg, null , false, false , false , true)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中 **五、PackageDexOptimizer#performDexOp(PackageParser.Package, String[], String[], boolean, String,CompilerStats.PackageStats)方法解析方法解析**

* 6、args.doRename(res.returnCode, pkg, oldCodePath)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中**六、args.doRename(res.returnCode, pkg, oldCodePath)方法解析**

* 7、startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg)：这块内容请参考[APK安装流程详解15——PMS中的新安装流程下(装载)补充]中**七、startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg)方法解析**

## 四、PackageManagerService#installNewPackageLI(PackageParser.Package, int, int, UserHandle, String, String,PackageInstalledInfo)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11757行

```dart
    /*
     * Install a non-existing package.
     */
     // 安装一个不存在的安装包。
    private void installNewPackageLI(PackageParser.Package pkg, int parseFlags, int scanFlags,
            UserHandle user, String installerPackageName, String volumeUuid,
            PackageInstalledInfo res) {
        // ******** 第一部分 ********
        // Remember this for later, in case we need to rollback this install
        String pkgName = pkg.packageName;

        if (DEBUG_INSTALL) Slog.d(TAG, "installNewPackageLI: " + pkg);

        // 判断是否存在APK的数据。如果一个APK不是经过经常的卸载流程，那其历史数据可能还是保留下来的。
        final boolean dataDirExists = Environment
                .getDataUserPackageDirectory(volumeUuid, UserHandle.USER_OWNER, pkgName).exists();
        // 如果package已经存在了
        synchronized(mPackages) {
            if (mSettings.mRenamedPackages.containsKey(pkgName)) {
            // 已经安装了具有相同名称的包，尽管它已经被重命名为较旧的名称。所以应该走更新流程而不是新安装流程
                // A package with the same name is already installed, though
                // it has been renamed to an older name.  The package we
                // are trying to install should be installed as an update to
                // the existing one, but that has not been requested, so bail.
                res.setError(INSTALL_FAILED_ALREADY_EXISTS, "Attempt to re-install " + pkgName
                        + " without first uninstalling package running as "
                        + mSettings.mRenamedPackages.get(pkgName));
                return;
            }
            if (mPackages.containsKey(pkgName)) {
                 // 如果已经有了相同的包名，则禁止安装
                // Don't allow installation over an existing package with the same name.
                res.setError(INSTALL_FAILED_ALREADY_EXISTS, "Attempt to re-install " + pkgName
                        + " without first uninstalling.");
                return;
            }
        }
      // ******** 第二部分 ********
        try {
             //核心调用： 安装APK
            PackageParser.Package newPackage = scanPackageLI(pkg, parseFlags, scanFlags,
                    System.currentTimeMillis(), user);
            // 更新Settings
            updateSettingsLI(newPackage, installerPackageName, volumeUuid, null, null, res, user);
            // delete the partially installed application. the data directory will have to be
            // restored if it was already existing
            // 如果安装失败，则执行回退操作，删除创建的文件夹等缓存文件
            if (res.returnCode != PackageManager.INSTALL_SUCCEEDED) {
                // remove package from internal structures.  Note that we want deletePackageX to
                // delete the package data and cache directories that it created in
                // scanPackageLocked, unless those directories existed before we even tried to
                // install.
                deletePackageLI(pkgName, UserHandle.ALL, false, null, null,
                        dataDirExists ? PackageManager.DELETE_KEEP_DATA : 0,
                                res.removedInfo, true);
            }

        } catch (PackageManagerException e) {
            res.setError("Package couldn't be installed in " + pkg.codePath, e);
        }
    }
```

这个方法主要分为两个部分：

* 第一部分：安装前检索，主要是根据两个检索条件来排除相同的包名的情况：
   \-  判断重命名的包中是否含有相同的包名
   \-  判断已有的安装包中是否有相同的包名

* 第二部分：进行安装：主要是调用scanPackageLI进行安装，通过这个方法APK的包中信息都会记录在PackageManagerService中；之后调用了updateSettingsLI进行设置信息的更新，主要是更新了权限信息与安装完成信息。如果安装失败就会删除安装包信息。

这个方法内部有两个核心方法如下：

* PackageParser.Package newPackage = scanPackageLI(pkg, parseFlags, scanFlags,System.currentTimeMillis(), user)：负责安装

* updateSettingsLI(newPackage, installerPackageName, null, null, res)：安装后的Setting信息的更新。

## 五、PackageManagerService#scanPackageLI(File scanFile, int parseFlags, int scanFlags,long currentTime, UserHandle user) 方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 6466行

```java
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        boolean success = false;
        try {
            final PackageParser.Package res = scanPackageDirtyLI(pkg, parseFlags, scanFlags,
                    currentTime, user);
            success = true;
            return res;
        } finally {
            if (!success && (scanFlags & SCAN_DELETE_DATA_ON_FAILURES) != 0) {
                removeDataDirsLI(pkg.volumeUuid, pkg.packageName);
            }
        }
    }
```

scanPackageLI主要调用了scanPackageDirtyLI，如果调用失败则调用removeDataDirsLI来移除安装信息。

这块代码之前在[APK安装流程详解8——PackageManagerService的启动流程(下)]中的**六、6、PackageManagerService#scanPackageLI(PackageParser.Package, int, int, long, UserHandle)方法解析(首参数为Package)**（检测安装包是否支持安装，创建 data 目录，调用 createDataDirsLI 进行包的安装，创建用户数据，对包进行 dex 优化，将安装包的内容从 pkg 里面映射到 PackageManagerService 里面）。

## 六、PackageManagerService#updateSettingsLIupdateSettingsLI(PackageParser.Package, String,String, int[], boolean[], PackageInstalledInfo,UserHandle)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12158行

```java
    private void updateSettingsLI(PackageParser.Package newPackage, String installerPackageName,
            String volumeUuid, int[] allUsers, boolean[] perUserInstalled, PackageInstalledInfo res,
            UserHandle user) {

        //************** 第一步 **************
        // 包名
        String pkgName = newPackage.packageName;
        synchronized (mPackages) {
            //write settings. the installStatus will be incomplete at this stage.
            //note that the new package setting would have already been
            //added to mPackages. It hasn't been persisted yet.
            // 设置安装状态，并写入
            mSettings.setInstallStatus(pkgName, PackageSettingBase.PKG_INSTALL_INCOMPLETE);
            mSettings.writeLPr();
        }

        if (DEBUG_INSTALL) Slog.d(TAG, "New package installed in " + newPackage.codePath);
        //************** 第二步 **************
        synchronized (mPackages) {
            // 更新权限
            updatePermissionsLPw(newPackage.packageName, newPackage,
                    UPDATE_PERMISSIONS_REPLACE_PKG | (newPackage.permissions.size() > 0
                            ? UPDATE_PERMISSIONS_ALL : 0));
            // For system-bundled packages, we assume that installing an upgraded version
            // of the package implies that the user actually wants to run that new code,
            // so we enable the package.
            // 对于绑定的软件，如果是更新，我们可以理解用户要运行新程序，我们启动这个软件

             //************** 第三步 **************
            PackageSetting ps = mSettings.mPackages.get(pkgName);
            if (ps != null) {
                 // 如果系统中拥有同样包名的设置PackageSetting
                if (isSystemApp(newPackage)) {
                   // 如果是系统应用
                    // NB: implicit assumption that system package upgrades apply to all users
                    // 系统应用适用于所有用户
                    if (DEBUG_INSTALL) {
                        Slog.d(TAG, "Implicitly enabling system package on upgrade: " + pkgName);
                    }
                    
                    //如果存在已经安装该应用的用户组
                    if (res.origUsers != null) {
                        for (int userHandle : res.origUsers) {
                            // 设置它为启用状态 
                            ps.setEnabled(COMPONENT_ENABLED_STATE_DEFAULT,
                                    userHandle, installerPackageName);
                        }
                    }
                    // Also convey the prior install/uninstall state
                    // 修改其对应的用户状态
                    if (allUsers != null && perUserInstalled != null) {
                        for (int i = 0; i < allUsers.length; i++) {
                            if (DEBUG_INSTALL) {
                                Slog.d(TAG, "    user " + allUsers[i]
                                        + " => " + perUserInstalled[i]);
                            }
                            ps.setInstalled(perUserInstalled[i], allUsers[i]);
                        }
                        // these install state changes will be persisted in the
                        // upcoming call to mSettings.writeLPr().
                    }
                }
                // It's implied that when a user requests installation, they want the app to be
                // installed and enabled.
                // 用户进行安装的时候，其实他们是希望安装并启用应用程序的。所以设置他们的属性
                int userId = user.getIdentifier();
                if (userId != UserHandle.USER_ALL) {
                    ps.setInstalled(true, userId);
                    ps.setEnabled(COMPONENT_ENABLED_STATE_DEFAULT, userId, installerPackageName);
                }
            }
            res.name = pkgName;
            res.uid = newPackage.applicationInfo.uid;
            res.pkg = newPackage;
            mSettings.setInstallStatus(pkgName, PackageSettingBase.PKG_INSTALL_COMPLETE);
            mSettings.setInstallerPackageName(pkgName, installerPackageName);
            res.returnCode = PackageManager.INSTALL_SUCCEEDED;
            //to update install status
            // 写入
           //************** 第四步 **************
            mSettings.writeLPr();
        }
    }
```

将这个方法分为四个部分：

- 第一步：设置更新状态
- 第二步：更新权限
- 第三步：调整包状态
- 第四步：Settings写入

至此安装结束。

## 七、 PackageHandler的处理Message的what值为POST_INSTALL的情况解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)1333行

```java
        void doHandleMessage(Message msg) {
            switch (msg.what) {
            ...
                case POST_INSTALL: {
                    if (DEBUG_INSTALL) Log.v(TAG, "Handling post-install for " + msg.arg1);
                    //***************** 第一步 ****************
                    // 我们知道msg的arg1是token，arg2是0
                    PostInstallData data = mRunningInstalls.get(msg.arg1);
                   // 因为已经安装成功了，所以在 正在安装列表中删除了这个选项
                    mRunningInstalls.delete(msg.arg1);
                    boolean deleteOld = false;

                    if (data != null) {
                        InstallArgs args = data.args;
                        PackageInstalledInfo res = data.res;
   
                        // 如果安装成功
                        if (res.returnCode == PackageManager.INSTALL_SUCCEEDED) {
                            final String packageName = res.pkg.applicationInfo.packageName;
                            res.removedInfo.sendBroadcast(false, true, false);
                            Bundle extras = new Bundle(1);
                            extras.putInt(Intent.EXTRA_UID, res.uid);

                            // Now that we successfully installed the package, grant runtime
                            // permissions if requested before broadcasting the install.
                             // 如果已经成功的安装了应用，在发送广播之前先授予一些必要的权限
                            // 这些权限在 installPackageAsUser 中创建 InstallParams 时传递的，为null。
                            if ((args.installFlags
                                    & PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS) != 0) {

                                 //***************** 第二步 ****************
                                grantRequestedRuntimePermissions(res.pkg, args.user.getIdentifier(),
                                        args.installGrantPermissions);
                            }

                            // Determine the set of users who are adding this
                            // package for the first time vs. those who are seeing
                            // an update.
                            // 看一下当前应用对于那些用户是第一次安装，那些用户是更新升级安装
                            int[] firstUsers;
                            int[] updateUsers = new int[0];
                            if (res.origUsers == null || res.origUsers.length == 0) {
                                //所有用户都是第一次安装
                                firstUsers = res.newUsers;
                            } else {
                                firstUsers = new int[0];
                                // 这里通过刚刚已经安装该包的用户中选出 那些之前安装过该包的用户
                                for (int i=0; i<res.newUsers.length; i++) {
                                    int user = res.newUsers[i];
                                    boolean isNew = true;
                                    for (int j=0; j<res.origUsers.length; j++) {
                                        if (res.origUsers[j] == user) {
                                             // 找到以前安装过该包的用户
                                            isNew = false;
                                            break;
                                        }
                                    }
                                    if (isNew) {
                                        int[] newFirst = new int[firstUsers.length+1];
                                        System.arraycopy(firstUsers, 0, newFirst, 0,
                                                firstUsers.length);
                                        newFirst[firstUsers.length] = user;
                                        firstUsers = newFirst;
                                    } else {
                                        int[] newUpdate = new int[updateUsers.length+1];
                                        System.arraycopy(updateUsers, 0, newUpdate, 0,
                                                updateUsers.length);
                                        newUpdate[updateUsers.length] = user;
                                        updateUsers = newUpdate;
                                    }
                                }
                            }

                            //***************** 第三步 ****************
                           // 安装完成之后发送"ACTION_PACKAGE_ADDED"广播
                            sendPackageBroadcast(Intent.ACTION_PACKAGE_ADDED,
                                    packageName, extras, null, null, firstUsers);
                            final boolean update = res.removedInfo.removedPackage != null;
                            if (update) {
                                extras.putBoolean(Intent.EXTRA_REPLACING, true);
                            }

                            sendPackageBroadcast(Intent.ACTION_PACKAGE_ADDED,
                                    packageName, extras, null, null, updateUsers);

                            //***************** 第四步 ****************
                            if (update) {
                                // 如果是升级更新安装，还会发送ACTION_PACKAGE_REPLACED和ACTION_MY_PACKAGE_REPLACED广播
                                // 这两个广播不同之处在于PACKAGE_REPLACE将携带一个extra信息
                                sendPackageBroadcast(Intent.ACTION_PACKAGE_REPLACED,
                                        packageName, extras, null, null, updateUsers);
                                sendPackageBroadcast(Intent.ACTION_MY_PACKAGE_REPLACED,
                                        null, null, packageName, null, updateUsers);


                                //***************** 第五步 ****************
                                // treat asec-hosted packages like removable media on upgrade
                                // 判断该包是否设置了PRIVATE_FLAG_FORWARD_LOCK标志或者是要求安装在SD卡上
                                if (res.pkg.isForwardLocked() || isExternal(res.pkg)) {
                                    if (DEBUG_INSTALL) {
                                        Slog.i(TAG, "upgrading pkg " + res.pkg
                                                + " is ASEC-hosted -> AVAILABLE");
                                    }
                                    int[] uidArray = new int[] { res.pkg.applicationInfo.uid };
                                    ArrayList<String> pkgList = new ArrayList<String>(1);
                                    pkgList.add(packageName);
                                    sendResourcesChangedBroadcast(true, true,
                                            pkgList,uidArray, null);
                                }
                            }
                            if (res.removedInfo.args != null) {
                                // Remove the replaced package's older resources safely now
                                // 删除被替换应用的资源目录
                                deleteOld = true;
                            }


                             //***************** 第六步 ****************
                            // If this app is a browser and it's newly-installed for some
                            // users, clear any default-browser state in those users
                             // 针对Browser做一些处理
                            if (firstUsers.length > 0) {
                                // the app's nature doesn't depend on the user, so we can just
                                // check its browser nature in any user and generalize.
                                // 判断是否是浏览器应用
                                if (packageIsBrowser(packageName, firstUsers[0])) {
                                    synchronized (mPackages) {
                                        for (int userId : firstUsers) {
                                            mSettings.setDefaultBrowserPackageNameLPw(null, userId);
                                        }
                                    }
                                }
                            }
                            // Log current value of "unknown sources" setting
                            EventLog.writeEvent(EventLogTags.UNKNOWN_SOURCES_ENABLED,
                                getUnknownSourcesSettings());
                        }

                        //***************** 第七步 ****************
                        // Force a gc to clear up things
                        // 执行gc操作
                        Runtime.getRuntime().gc();
                        // We delete after a gc for applications  on sdcard.

                        //***************** 第八步 ****************
                         // 执行删除操作
                        if (deleteOld) {
                            synchronized (mInstallLock) {
                                // 调用FileInstallArgs的doPostDeleteLI进行资源清理
                                res.removedInfo.args.doPostDeleteLI(true);
                            }
                        }

                        //***************** 第九步 ****************
                        if (args.observer != null) {
                            try {
                                Bundle extras = extrasForInstallResult(res);
                                // 回调onPackageInstalled方法
                                args.observer.onPackageInstalled(res.name, res.returnCode,
                                        res.returnMsg, extras);
                            } catch (RemoteException e) {
                                Slog.i(TAG, "Observer no longer exists.");
                            }
                        }
                    } else {
                        Slog.e(TAG, "Bogus post-install token " + msg.arg1);
                    }
                } break;
       ...
      }
```

终于到了装载的最后一个流程了，将这个方法内部分为9个部分：

* 第一步：这里主要是先将安装信息从安装列列表中移除，这个也是前面在processPendingInstall中添加的

* 第二步：安装成功后，获取运行时权限

* 第三步：获取权限后，发送ACTION_PACKAGE_ADDED广播，告诉Laucher之流，来新客人了，赶紧把icon啥的放上去。

* 第四步：如果是升级更新则在发送两条广播
  * ACTION_PACKAGE_REPLACED：一个新版本的应用安装到设备上，替换换之前已经存在的版本
  * ACTION_MY_PACKAGE_REPLACED：应用的新版本替换旧版本被安装，只发给被更新的应用自己

* 第五步：如果安装包中设置了PRIVATE_FLAG_FORWARD_LOCK或者被要求安装在SD卡上，则调用sendResourcesChangedBroadcast方法来发送一个资源更改的广播

* 第六步：如果该应用是一个浏览器，则要清除浏览器设置，重新检查浏览器设置。

* 第七步：强制调用gc，触发 JVM 进行垃圾回收操作。

* 第八步：删除旧的安装信息。

* 第九步：回调args.observer.packageInstalled方法。告诉PackageInstaller安装结果。从而实现了安装回调到UI层。

## 八、总结

### 1、安装大致流程图

![](https://upload-images.jianshu.io/upload_images/5713484-70c0a869e6c3ad26.png)

安装过程：复制apk安装包到/data/app目录下，解压并扫描安装包，向资源管理器注入apk资源，解析AndroidManifest文件，并在/data/data目录下创建对应的应用数据目录，然后针对dalvik/art环境优化dex文件，保存到dalvik-cache目录，将AndroidManifest文件解析出的组件、权限注册到PackageManagerService，完成后发送广播。

### 2、安装详细时序图

![](https://upload-images.jianshu.io/upload_images/5713484-b1e751279a7dc51f.png)

### 3、整体架构图

![](https://upload-images.jianshu.io/upload_images/5713484-c26e53b6ede25f84.png)

## 参考文章

1. [APK安装流程详解13——PMS中的新安装流程下(装载)](https://www.jianshu.com/p/9ddb930153b7)

