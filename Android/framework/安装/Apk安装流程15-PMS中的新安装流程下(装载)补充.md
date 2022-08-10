# APK安装流程详解15——PMS中的新安装流程下(装载)补偿

本片文章的主要内容如下：

```
1、PackageParser#setSeparateProcesses(String[] procs)方法解析
2、PackageManagerService#shouldCheckUpgradeKeySetLP(PackageSetting, int) 方法解析
3、PackageManagerService#checkUpgradeKeySetLP(PackageSetting, PackageParser.Package) 方法解析
4、PackageManagerService#verifySignaturesLP(PackageSetting, PackageParser.Package)方法解析
5、PackageDexOptimizer#performDexOp(PackageParser.Package, String[], String[], boolean, String,CompilerStats.PackageStats)方法解析
6、args.doRename(res.returnCode, pkg, oldCodePath)方法解析
7、startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg)方法解析
```

## 一、 PackageParser#setSeparateProcesses(String[] procs)方法解析

代码位置在PackageManagerService的installPackageLI方法里面会调用到，代码如下：
[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
        PackageParser pp = new PackageParser();
        pp.setSeparateProcesses(mSeparateProcesses);
        pp.setDisplayMetrics(mMetrics);
        ...
    }
```

可以看到，这里构造了一个PackageParser对象，然后设置了mSeparateProcesses属性。

mSeparateProcesses是一个数组，表示独立的进程名列表，这个参数是在PackageManagerService的构造函数中调用到，以后会分析一下函数是在什么地方调用，所以看mSeparateProcesses的获取过程：

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1838行

```csharp
        String separateProcesses = SystemProperties.get("debug.separate_processes");
        if (separateProcesses != null && separateProcesses.length() > 0) {
            if ("*".equals(separateProcesses)) {
                mDefParseFlags = PackageParser.PARSE_IGNORE_PROCESSES;
                mSeparateProcesses = null;
                Slog.w(TAG, "Running with debug.separate_processes: * (ALL)");
            } else {
                mDefParseFlags = 0;
                mSeparateProcesses = separateProcesses.split(",");
                Slog.w(TAG, "Running with debug.separate_processes: "
                        + separateProcesses);
            }
        } else {
            mDefParseFlags = 0;
            mSeparateProcesses = null;
        }    
```

从系统属性中读取debug.separate_processes属性，如果改属性返回值不为空，表示设置了该属性，否则系统未设置改属性，如果值等于则mSeparateProcesses为空，如果不为空，则逗号分隔该字符串，解析每个独立的进程名，那个debug.separate_processes究竟有什么用？

那我们就来看下debug.separate_processes的作用：

separate_processes可以让应用程序的组件运行在自己的进程里面，separate_processes一般有两种设置：

- 如果设置了"setprop debug.separate_processes",则将设置这个每个包中的每个进程。
- 如果设置"setprop debug.separate_processes 'com.google.process.content,com.google.android.samples' "它只会影响项目清单中的指定进程("com.google.process.content,com.google.android.samples")。或者在AndroidManifest里面显式的设置"android:process"标记。

PS:虽然这样可以将一个进程拆分出来，或者多个进程组合成一个进程(他们必须来自同一个包)。它会强制所有受影响的组件在自己的.apk运行。

## 二、 PackageManagerService#shouldCheckUpgradeKeySetLP(PackageSetting, int) 方法解析

这个方法在PackageManagerService的installPackageLI方法里面被调用。代码在代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12346行

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
        if (shouldCheckUpgradeKeySetLP(ps, scanFlags)) {
             ...
        }
        ...
   }
```

那我们来看下shouldCheckUpgradeKeySetLP这个方法的内部实现
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11807行

```java
    private boolean shouldCheckUpgradeKeySetLP(PackageSetting oldPs, int scanFlags) {
        // Can't rotate keys during boot or if sharedUser.
         // 判断是否可以进行升级验证的条件
        if (oldPs == null || (scanFlags&SCAN_INITIAL) != 0 || oldPs.sharedUser != null
                || !oldPs.keySetData.isUsingUpgradeKeySets()) {
            return false;
        }
        // app is using upgradeKeySets; make sure all are valid
        KeySetManagerService ksms = mSettings.mKeySetManagerService;
        // 获取老的keySet数组
        long[] upgradeKeySets = oldPs.keySetData.getUpgradeKeySets();
        for (int i = 0; i < upgradeKeySets.length; i++) {
            // 遍历keySet数组，检查是否有对应的密钥集
            if (!ksms.isIdValidKeySetId(upgradeKeySets[i])) {
                // 如果对应的密钥集合，说明签名密钥有问题，则返回false
                Slog.wtf(TAG, "Package "
                         + (oldPs.name != null ? oldPs.name : "<null>")
                         + " contains upgrade-key-set reference to unknown key-set: "
                         + upgradeKeySets[i]
                         + " reverting to signatures check.");
                return false;
            }
        }
     // 如果所有的密钥都能对上，说明密钥没有问题，则返回true
        return true;
    }
```

通过注释我们知道，方法主要检查密钥集合是否和老版本的一致，如果不一致，则返回false。如果一致则返回true。

## 三、 PackageManagerService#checkUpgradeKeySetLP(PackageSetting, PackageParser.Package) 方法解析

这个方法在PackageManagerService的installPackageLI方法里面被调用。代码在代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12347行

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
        if (!checkUpgradeKeySetLP(ps, pkg)) {
             ...
        }
        ...
   }
```

那来看下checkUpgradeKeySetLP这个方法的内部实现
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11829行

```java
    private boolean checkUpgradeKeySetLP(PackageSetting oldPS, PackageParser.Package newPkg) {
        // Upgrade keysets are being used.  Determine if new package has a superset of the
        // required keys.
        // 如果升级KeySet，确保新的安装包是否有超集的keys
        // 获取旧版本的KeySet数组
        long[] upgradeKeySets = oldPS.keySetData.getUpgradeKeySets();
        KeySetManagerService ksms = mSettings.mKeySetManagerService;
        // 遍历KeySet数组
        for (int i = 0; i < upgradeKeySets.length; i++) {
             // 根据密钥获取公钥
            Set<PublicKey> upgradeSet = ksms.getPublicKeysFromKeySetLPr(upgradeKeySets[i]);
            if (upgradeSet != null && newPkg.mSigningKeys.containsAll(upgradeSet)) {
                 // 如果对应上 则返回true，
                return true;
            }
        }
        // 遍历都没有符合的，则返回false
        return false;
    }
```

这个方法内部主要检查是否有匹配的公钥，如果有则返回true，没有则返回false。

## 四、 PackageManagerService#verifySignaturesLP(PackageSetting, PackageParser.Package)方法解析

这个方法在PackageManagerService的installPackageLI方法里面被调用。代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12355行

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
         verifySignaturesLP(pkgSetting, pkg);
        ...
   }
```

那来看下verifySignaturesLP这个方法的内部实现
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11829行

```java
    private void verifySignaturesLP(PackageSetting pkgSetting, PackageParser.Package pkg)
            throws PackageManagerException {
         // 第一步
        if (pkgSetting.signatures.mSignatures != null) {
            // Already existing package. Make sure signatures match
            //如果有旧版本，则查看旧版本的签名是否匹配
            boolean match = compareSignatures(pkgSetting.signatures.mSignatures, pkg.mSignatures)
                    == PackageManager.SIGNATURE_MATCH;
            if (!match) {
                match = compareSignaturesCompat(pkgSetting.signatures, pkg)
                        == PackageManager.SIGNATURE_MATCH;
            }
            if (!match) {
                match = compareSignaturesRecover(pkgSetting.signatures, pkg)
                        == PackageManager.SIGNATURE_MATCH;
            }
            if (!match) {
                throw new PackageManagerException(INSTALL_FAILED_UPDATE_INCOMPATIBLE, "Package "
                        + pkg.packageName + " signatures do not match the "
                        + "previously installed version; ignoring!");
            }
        }

        // Check for shared user signatures
        // 第二步
        if (pkgSetting.sharedUser != null && pkgSetting.sharedUser.signatures.mSignatures != null) {
           // 如果有共享用户，则检验共享用户的签名
            // Already existing package. Make sure signatures match
            boolean match = compareSignatures(pkgSetting.sharedUser.signatures.mSignatures,
                    pkg.mSignatures) == PackageManager.SIGNATURE_MATCH;
            if (!match) {
                match = compareSignaturesCompat(pkgSetting.sharedUser.signatures, pkg)
                        == PackageManager.SIGNATURE_MATCH;
            }
            if (!match) {
                match = compareSignaturesRecover(pkgSetting.sharedUser.signatures, pkg)
                        == PackageManager.SIGNATURE_MATCH;
            }
            if (!match) {
                throw new PackageManagerException(INSTALL_FAILED_SHARED_USER_INCOMPATIBLE,
                        "Package " + pkg.packageName
                        + " has no signatures that match those in shared user "
                        + pkgSetting.sharedUser.name + "; ignoring!");
            }
        }
```

这个方法其实很简单，分为两个部分

- 如果有老版本的签名 则检查老版本的签名和新安装包的签名是否一致
- 如果有共享用户的签名，则检查共享用户的签名与新安装包的签名是否一致。

里面验证签名都是三重机制，如下：

* 第一重校验：调用compareSignatures方法，比较旧的APK的签名和新的APK签名是否相同，如果返回值是PackageManager.SIGNATURE_MATCH，则通过并且不用后续校验，没有通过则进行第二重校验。

* 第二重校验：调用compareSignaturesCompat方法，比较旧的APK的签名和新的APK签名是否相同，如果返回值是PackageManager.SIGNATURE_MATCH，则通过并且不进行后续校验，没有通过则进行第二重校验。

* 第三重校验：调用compareSignaturesRecover方法，比较旧的APK的签名和新的APK签名是否相同，如果返回值是PackageManager.SIGNATURE_MATCH，则通过并且不进行后续校验，没有通过则抛出异常，结束执行。

那就依次看下这三个方法

### (一)、compareSignatures(Signature[] s1, Signature[] s2)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 3951行

```dart
    /**
     * Compares two sets of signatures. Returns:
     * <br />
     * {@link PackageManager#SIGNATURE_NEITHER_SIGNED}: if both signature sets are null,
     * <br />
     * {@link PackageManager#SIGNATURE_FIRST_NOT_SIGNED}: if the first signature set is null,
     * <br />
     * {@link PackageManager#SIGNATURE_SECOND_NOT_SIGNED}: if the second signature set is null,
     * <br />
     * {@link PackageManager#SIGNATURE_MATCH}: if the two signature sets are identical,
     * <br />
     * {@link PackageManager#SIGNATURE_NO_MATCH}: if the two signature sets differ.
     */
    static int compareSignatures(Signature[] s1, Signature[] s2) {
        if (s1 == null) {
            return s2 == null
                    ? PackageManager.SIGNATURE_NEITHER_SIGNED
                    : PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
        }

        if (s2 == null) {
            return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
        }

        if (s1.length != s2.length) {
            return PackageManager.SIGNATURE_NO_MATCH;
        }

        // Since both signature sets are of size 1, we can compare without HashSets.
        if (s1.length == 1) {
            return s1[0].equals(s2[0]) ?
                    PackageManager.SIGNATURE_MATCH :
                    PackageManager.SIGNATURE_NO_MATCH;
        }

        ArraySet<Signature> set1 = new ArraySet<Signature>();
        for (Signature sig : s1) {
            set1.add(sig);
        }
        ArraySet<Signature> set2 = new ArraySet<Signature>();
        for (Signature sig : s2) {
            set2.add(sig);
        }
        // Make sure s2 contains all signatures in s1.
        if (set1.equals(set2)) {
            return PackageManager.SIGNATURE_MATCH;
        }
        return PackageManager.SIGNATURE_NO_MATCH;
    }
```

上代码很简单就是先做非空判断，然后把两个数组转化成ArraySet，然后判断两个ArraySet是否相同，如果相同则返回PackageManager.SIGNATURE_MATCH，如果不相同则返回PackageManager.SIGNATURE_NO_MATCH。

### (二)、compareSignaturesCompat(PackageSignatures,PackageParser.Package)方法解析

如果上面的匹配不符合则说明当前不匹配，所我们要考虑是不是版本的的问题，所以就有了这个方法。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 4004行

```csharp
    /**
     * Used for backward compatibility to make sure any packages with
     * certificate chains get upgraded to the new style. {@code existingSigs}
     * will be in the old format (since they were stored on disk from before the
     * system upgrade) and {@code scannedSigs} will be in the newer format.
     */
    private int compareSignaturesCompat(PackageSignatures existingSigs,
            PackageParser.Package scannedPkg) {
        // 第一步
        // 更新安装包名的签名版本是否小于数据库中签名版本
        if (!isCompatSignatureUpdateNeeded(scannedPkg)) {
            // 如果大于，则直接返回不匹配
            return PackageManager.SIGNATURE_NO_MATCH;
        }
       // 第二步
        ArraySet<Signature> existingSet = new ArraySet<Signature>();
        for (Signature sig : existingSigs.mSignatures) {
            existingSet.add(sig);
        }
        ArraySet<Signature> scannedCompatSet = new ArraySet<Signature>();
        for (Signature sig : scannedPkg.mSignatures) {
            try {
                Signature[] chainSignatures = sig.getChainSignatures();
                for (Signature chainSig : chainSignatures) {
                    scannedCompatSet.add(chainSig);
                }
            } catch (CertificateEncodingException e) {
                scannedCompatSet.add(sig);
            }
        }
        // 第三步
        /*
         * Make sure the expanded scanned set contains all signatures in the
         * existing one.
         */
        if (scannedCompatSet.equals(existingSet)) {
            // Migrate the old signatures to the new scheme.
            // 签名替换
            existingSigs.assignSignatures(scannedPkg.mSignatures);
            // The new KeySets will be re-added later in the scanning process.
            synchronized (mPackages) {
                mSettings.mKeySetManagerService.removeAppKeySetDataLPw(scannedPkg.packageName);
            }
            return PackageManager.SIGNATURE_MATCH;
        }
        // 如果最后不匹配则返回 不匹配
        return PackageManager.SIGNATURE_NO_MATCH;
    }
```

先来看下注释：

这个方法主要是保证向后的兼容性，这样可以确保证书链上的包可以升级到最新的版本。existingSigs是旧格式，因为它在升级前是在磁盘空间上，scansSigs是新的格式。

将这个方法分为三个步骤

* 第一步：判断升级包的签名版本是否小于当前系统中签名的数据库版本号，上面一层判断已经不匹配才会走到这个方法里面，所以如果更新的安装包的签名版本大于当前数据库中的签名版本号，则一定是不匹配的。所以会返回PackageManager.SIGNATURE_NO_MATCH

* 第二步：同样是通过遍历的方式把旧的签名数组转化为ArraySet对象existingSet，同时遍历新的安装包中每个签名的签名链，并把签名链加入到ArraySet对象

* 第三步：确保扩展的扫描集包含现有的所有签名。如果scannedCompatSet和existingSet一致，则进行签名替换，并且在mSettings.mKeySetManagerService删除签名。

PS：上文说的DatabaseVersion 其实是[Settings.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/Settings.java)的内部类

### (三)、compareSignaturesRecover(PackageSignatures,PackageParser.Package)方法解析

如果上面两个匹配规则都没有匹配，考虑是不是在证书有过变动导致的匹配失败，所以这个方法主要考虑是否恢复证书进行匹配
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 4046行

```kotlin
    private int compareSignaturesRecover(PackageSignatures existingSigs,
            PackageParser.Package scannedPkg) {
        // 第一步
        if (!isRecoverSignatureUpdateNeeded(scannedPkg)) {
            return PackageManager.SIGNATURE_NO_MATCH;
        }
        // 第二步
        String msg = null;
        try {
            if (Signature.areEffectiveMatch(existingSigs.mSignatures, scannedPkg.mSignatures)) {
                logCriticalInfo(Log.INFO, "Recovered effectively matching certificates for "
                        + scannedPkg.packageName);
                return PackageManager.SIGNATURE_MATCH;
            }
        } catch (CertificateException e) {
            msg = e.getMessage();
        }

        logCriticalInfo(Log.INFO,
                "Failed to recover certificates for " + scannedPkg.packageName + ": " + msg);
        return PackageManager.SIGNATURE_NO_MATCH;
    }
```

将上面的方法分为两步：

* 第一步：调用isCompatSignatureUpdateNeeded判断是否有恢复的需求，这里随带说下isCompatSignatureUpdateNeeded方法。Android LOLLIPOP这个版本是一个时间窗口，会对证书进行修改。如果不用进行证书恢复，则整个这个方法就无意义了，直接返回PackageManager.SIGNATURE_NO_MATCH

* 第二步：调用Signature的静态方法areEffectiveMatch进行匹配。由于在极少数情况下，证书可能会有错误的编码，导致匹配失败。这个方法就是避免这种情况的解决方案。

## 五、PackageDexOptimizer#performDexOp(PackageParser.Package, String[], String[], boolean, String,CompilerStats.PackageStats)方法解析

代码位置在PackageManagerService的installPackageLI方法里面会调用到，代码如下：
[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12451行

```csharp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
           // Run dexopt before old package gets removed, to minimize time when app is unavailable
            int result = mPackageDexOptimizer
                    .performDexOpt(pkg, null /* instruction sets */, false /* forceDex */,false /* defer */, false /* inclDependencies */, true /* boot complete */);
        ...
    }
```

那就来看下PackageDexOptimizer#performDexOp方法
代码在[PackageDexOptimizer.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageDexOptimizer.java) 73行

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
             // 除了在启动启动时段，其他时段 mSystemReady一般为true
            if (useLock) {
                mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                mDexoptWakeLock.acquire();
            }
            try {
                // 核心方法
                return performDexOptLI(pkg, instructionSets, forceDex, defer, bootComplete, done);
            } finally {
                if (useLock) {
                    mDexoptWakeLock.release();
                }
            }
        }
    }
```

有注释，先来看下注释

对指定包内的代码和库执行dexopt。

方法内部主要用mInstallLock来加锁，然后调用performDexOptLI(PackageParser.Package, String[],String[], boolean, String,CompilerStats.PackageStats packageStats)方法

下面来看下这个方法

### 1、performDexOptLI(PackageParser.Package, String[],String[], boolean, String,CompilerStats.PackageStats packageStats)方法解析

代码在[PackageDexOptimizer.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageDexOptimizer.java) 98行

```dart
    private int performDexOptLI(PackageParser.Package pkg, String[] targetInstructionSets,
            boolean forceDex, boolean defer, boolean bootComplete, ArraySet<String> done) {
        final String[] instructionSets = targetInstructionSets != null ?
                targetInstructionSets : getAppDexInstructionSets(pkg.applicationInfo);

        if (done != null) {
            done.add(pkg.packageName);

            // 是否有一些共享库的apk，也要进行dex优化
            //usesLibraries 保存着AndroidManifest中的<uses-library>标签中android:required=true库
            if (pkg.usesLibraries != null) {
                //  进行dexopt优化 
                performDexOptLibsLI(pkg.usesLibraries, instructionSets, forceDex, defer,
                        bootComplete, done);
            }
            // usesOptionalLibraries 保存着AndroidManifest中<uses-library>标签中的 android:required=false的库
            if (pkg.usesOptionalLibraries != null) {
                 //  进行dexopt优化 
                performDexOptLibsLI(pkg.usesOptionalLibraries, instructionSets, forceDex, defer,
                        bootComplete, done);
            }
        }

        // 没有代码的包直接跳过
        if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_HAS_CODE) == 0) {
            return DEX_OPT_SKIPPED;
        }

        // 是否是虚拟机的安全模式
        final boolean vmSafeMode = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) != 0;
        //  是否是debug模式
        final boolean debuggable = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        // 获取所有代码的路径
        final List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        boolean performedDexOpt = false;
        // There are three basic cases here:
        // 1.) we need to dexopt, either because we are forced or it is needed
        // 2.) we are deferring a needed dexopt
        // 3.) we are skipping an unneeded dexopt
        // 通过ArraySet 复制一份 数组
        final String[] dexCodeInstructionSets = getDexCodeInstructionSets(instructionSets);
        // 开始遍历 数组
        for (String dexCodeInstructionSet : dexCodeInstructionSets) {
            if (!forceDex && pkg.mDexOptPerformed.contains(dexCodeInstructionSet)) {
                // 没有强制优化或者已经dex优化过 直接continue
                continue;
            }
            // 遍历所有 代码路径
            for (String path : paths) {
                final int dexoptNeeded;
                if (forceDex) {
                    // 如果是强制dex优化
                    dexoptNeeded = DexFile.DEX2OAT_NEEDED;
                } else {
                    try {
                        // 调用的DexFile的静态方法获取虚拟机对代码的优化意图
                        dexoptNeeded = DexFile.getDexOptNeeded(path, pkg.packageName,
                                dexCodeInstructionSet, defer);
                    } catch (IOException ioe) {
                        Slog.w(TAG, "IOException reading apk: " + path, ioe);
                        return DEX_OPT_FAILED;
                    }
                }
                // 如果不是强制优化且要求延迟优化，并且优化策略是不需要优化，则延迟优化
                if (!forceDex && defer && dexoptNeeded != DexFile.NO_DEXOPT_NEEDED) {
                  
                    // We're deciding to defer a needed dexopt. Don't bother dexopting for other
                    // paths and instruction sets. We'll deal with them all together when we process
                    // our list of deferred dexopts.
                    // 把包放到延迟优化列表 内部是add到一个ArraySet中
                    addPackageForDeferredDexopt(pkg);
                     // 返回延迟优化
                    return DEX_OPT_DEFERRED;
                }
                 // 如果不是 没必要优化，则意味着要做优化
                if (dexoptNeeded != DexFile.NO_DEXOPT_NEEDED) {
                    final String dexoptType;
                    String oatDir = null;
                   // 如果优化意图是dex->oat
                    if (dexoptNeeded == DexFile.DEX2OAT_NEEDED) {
                        dexoptType = "dex2oat";
                        try {
                            // 获取 oat目录
                            oatDir = createOatDirIfSupported(pkg, dexCodeInstructionSet);
                        } catch (IOException ioe) {
                            Slog.w(TAG, "Unable to create oatDir for package: " + pkg.packageName);
                            return DEX_OPT_FAILED;
                        }
                    } else if (dexoptNeeded == DexFile.PATCHOAT_NEEDED) {
                        // 优化意图为 补丁优化
                        dexoptType = "patchoat";
                    } else if (dexoptNeeded == DexFile.SELF_PATCHOAT_NEEDED) {
                        // 优化意图 为 用虚拟机的循环 补丁优化
                        dexoptType = "self patchoat";
                    } else {
                        throw new IllegalStateException("Invalid dexopt needed: " + dexoptNeeded);
                    }

                    Log.i(TAG, "Running dexopt (" + dexoptType + ") on: " + path + " pkg="
                            + pkg.applicationInfo.packageName + " isa=" + dexCodeInstructionSet
                            + " vmSafeMode=" + vmSafeMode + " debuggable=" + debuggable
                            + " oatDir = " + oatDir + " bootComplete=" + bootComplete);
                    // 获取sharedGid
                    final int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
                    // 调用mPackageManagerService的mInstaller的dexopt方法来进行优化
                    final int ret = mPackageManagerService.mInstaller.dexopt(path, sharedGid,
                            !pkg.isForwardLocked(), pkg.packageName, dexCodeInstructionSet,
                            dexoptNeeded, vmSafeMode, debuggable, oatDir, bootComplete);

                    // Dex2oat might fail due to compiler / verifier errors. We soldier on
                    // regardless, and attempt to interpret the app as a safety net.
                    if (ret == 0) {
                        // 优化成功
                        performedDexOpt = true;
                    }
                }
            }

            // At this point we haven't failed dexopt and we haven't deferred dexopt. We must
            // either have either succeeded dexopt, or have had getDexOptNeeded tell us
            // it isn't required. We therefore mark that this package doesn't need dexopt unless
            // it's forced. performedDexOpt will tell us whether we performed dex-opt or skipped
            // it.
            // 将他添加到已经优化过的缓存中
            pkg.mDexOptPerformed.add(dexCodeInstructionSet);
        }

        // If we've gotten here, we're sure that no error occurred and that we haven't
        // deferred dex-opt. We've either dex-opted one more paths or instruction sets or
        // we've skipped all of them because they are up to date. In both cases this
        // package doesn't need dexopt any longer.
        return performedDexOpt ? DEX_OPT_PERFORMED : DEX_OPT_SKIPPED;
    }
```

上面这个方法遍历了 APK 的所有代码路径，根据解析得到了dexoptType，最后用installd来完成了dexopt工作，其中如果dexoptType为dex2oat时，会调用createOatDirIfSupported方法获得oatdir。其他情况oatdir为空。

createOatDirIfSupported方法很简单，用 Install 在该目录下创建一个目录。

## 六、args.doRename(res.returnCode, pkg, oldCodePath)方法解析

代码位置在PackageManagerService的installPackageLI方法里面会调用到，代码如下：
[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12461行

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
        if (!args.doRename(res.returnCode, pkg, oldCodePath)) {
            res.setError(INSTALL_FAILED_INSUFFICIENT_STORAGE, "Failed rename");
            return;
        }
        ...
    }
```

这里面的args指的是**FileInstallArgs**对象，所以**args.doRename(res.returnCode, pkg, oldCodePath)**方法就是**FileInstallArgs#doRename(int, PackageParser.Package, String)**方法

那就来看下FileInstallArgs#doRename(int, PackageParser.Package, String)方法
 代码在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)  11115行

```kotlin
        boolean doRename(int status, PackageParser.Package pkg, String oldCodePath) {
             // 如果没有成功，清理并返回改名失败
            if (status != PackageManager.INSTALL_SUCCEEDED) {
                cleanUp();
                return false;
            }
             // 获取父目录
            final File targetDir = codeFile.getParentFile();
            // 获取旧的文件
            final File beforeCodeFile = codeFile;
            // 获取目录下的新的文件 调用getNextCodePath方法，后面会详解讲解
            final File afterCodeFile = getNextCodePath(targetDir, pkg.packageName);

            if (DEBUG_INSTALL) Slog.d(TAG, "Renaming " + beforeCodeFile + " to " + afterCodeFile);
            try {
                // 调用 Os的rename方法进行重命名
                Os.rename(beforeCodeFile.getAbsolutePath(), afterCodeFile.getAbsolutePath());
            } catch (ErrnoException e) {
                Slog.w(TAG, "Failed to rename", e);
                return false;
            }

            //  设置改名后文件的SELinux的上下文，后续会有在SELinux专题中详细讲解
            if (!SELinux.restoreconRecursive(afterCodeFile)) {
                Slog.w(TAG, "Failed to restorecon");
                return false;
            }

            // Reflect the rename internally
            codeFile = afterCodeFile;
            resourceFile = afterCodeFile;

            // Reflect the rename in scanned details
            // 重命名后一些变量也需要跟着变化
            pkg.codePath = afterCodeFile.getAbsolutePath();
            pkg.baseCodePath = FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile,
                    pkg.baseCodePath);
            pkg.splitCodePaths = FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile,
                    pkg.splitCodePaths);

            // Reflect the rename in app info
            pkg.applicationInfo.volumeUuid = pkg.volumeUuid;
            pkg.applicationInfo.setCodePath(pkg.codePath);
            pkg.applicationInfo.setBaseCodePath(pkg.baseCodePath);
            pkg.applicationInfo.setSplitCodePaths(pkg.splitCodePaths);
            pkg.applicationInfo.setResourcePath(pkg.codePath);
            pkg.applicationInfo.setBaseResourcePath(pkg.baseCodePath);
            pkg.applicationInfo.setSplitResourcePaths(pkg.splitCodePaths);

            return true;
        }
```

上面这个方法，主要是就调用getNextCodePath方法来获取新的apk的目录名字，然后调用os的rename函数重命名，然后进行重命名后变量属性的变更。

这里面涉及到一个重要方法，即getNextCodePath(File,String)方法，来看一下
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11689行

```cpp
    private File getNextCodePath(File targetDir, String packageName) {
        int suffix = 1;
        File result;
        do {
            result = new File(targetDir, packageName + "-" + suffix);
            suffix++;
        } while (result.exists());
        return result;
    }
```

代码很简单，就是获取一个新的APK目录名字在， 然后在APK的机上一个"-"+数字的后缀。

## 七、startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg);方法解析

代码位置在PackageManagerService的installPackageLI方法里面会调用到，代码如下：
[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12466行

```cpp
    private void installPackageLI(InstallArgs args, PackageInstalledInfo res) {
        ...
        startIntentFilterVerifications(args.user.getIdentifier(), replace, pkg);
        ...
    }
```

那就来看下PackageManagerService#startIntentFilterVerifications(int, boolean,PackageParser.Package)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11115行

```java
    private void startIntentFilterVerifications(int userId, boolean replacing,
            PackageParser.Package pkg) {
         // intentFilter的验证组件
        if (mIntentFilterVerifierComponent == null) {
            Slog.w(TAG, "No IntentFilter verification will not be done as "
                    + "there is no IntentFilterVerifier available!");
            return;
        }
  
        // 获取验证的uid
        final int verifierUid = getPackageUid(
                mIntentFilterVerifierComponent.getPackageName(),
                (userId == UserHandle.USER_ALL) ? UserHandle.USER_OWNER : userId); 
        // 删除what值为START_INTENT_FILTER_VERIFICATIONS的message，避免重复
        mHandler.removeMessages(START_INTENT_FILTER_VERIFICATIONS);
        // 创建一个 what值为START_INTENT_FILTER_VERIFICATIONS的Message
        final Message msg = mHandler.obtainMessage(START_INTENT_FILTER_VERIFICATIONS);
        // 构造这个IFVerificationParams并把它赋值给Message的obj字段
        msg.obj = new IFVerificationParams(pkg, replacing, userId, verifierUid);
        // 发送一个Message
        mHandler.sendMessage(msg);
    }
```

这个方法内部主要是获取了一个Message对象，然后构造了一个IFVerificationParams，并且把这个IFVerificationParams对象指向了Message的obj。然后发送了这个Message对象。

先来看下IFVerificationParams这个类。

### 1、IFVerificationParams类

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 606行

```java
    private static class IFVerificationParams {
        PackageParser.Package pkg;
        boolean replacing;
        int userId;
        int verifierUid;

        public IFVerificationParams(PackageParser.Package _pkg, boolean _replacing,
                int _userId, int _verifierUid) {
            pkg = _pkg;
            replacing = _replacing;
            userId = _userId;
            replacing = _replacing;
            verifierUid = _verifierUid;
        }
    }
```

看到这个类其实就是一个包装类，包装了4个字段而已。

IFVerificationParams 这个类看完，来看下what值为START_INTENT_FILTER_VERIFICATIONS的Message对应的处理逻辑。

### 2、what值为START_INTENT_FILTER_VERIFICATIONS的Message对应的处理逻辑

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1582行

```csharp
        void doHandleMessage(Message msg) {
            switch (msg.what) {
                ...
                case START_INTENT_FILTER_VERIFICATIONS: {
                    IFVerificationParams params = (IFVerificationParams) msg.obj;
                    verifyIntentFiltersIfNeeded(params.userId, params.verifierUid,
                            params.replacing, params.pkg);
                    break;
                }
                ...
      }
```

看到在case START_INTENT_FILTER_VERIFICATIONS里面就做了两件事：

* 获取IFVerificationParams 对象params

* 调用 **verifyIntentFiltersIfNeeded(int, int,boolean,PackageParser.Package)**方法

那来看下verifyIntentFiltersIfNeeded方法的内部执行情况。

### 3、PackageManagerService#verifyIntentFiltersIfNeeded(int, int,boolean,PackageParser.Package)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 12501行

```swift
    private void verifyIntentFiltersIfNeeded(int userId, int verifierUid, boolean replacing,
            PackageParser.Package pkg) {
         // 获取安装包中所有activity的数量
        int size = pkg.activities.size();
        // 如果没有activity则不需要验证，直接返回
        if (size == 0) {
            if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG,
                    "No activity, so no need to verify any IntentFilter!");
            return;
        }
      
        // 判断 这个安装包内是否设置了url的过滤限制
        final boolean hasDomainURLs = hasDomainURLs(pkg);
        // 如果没有设置url过滤限制，则直接返回
        if (!hasDomainURLs) {
            if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG,
                    "No domain URLs, so no need to verify any IntentFilter!");
            return;
        }

        if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG, "Checking for userId:" + userId
                + " if any IntentFilter from the " + size
                + " Activities needs verification ...");

        int count = 0;
        final String packageName = pkg.packageName;

        synchronized (mPackages) {
            // If this is a new install and we see that we've already run verification for this
            // package, we have nothing to do: it means the state was restored from backup.
            // 不是提前，即是新安装
            if (!replacing) {
                 // 如果是新安装，我们只需要判断是不是之前是不是验证，过
                IntentFilterVerificationInfo ivi =
                        mSettings.getIntentFilterVerificationLPr(packageName);
                if (ivi != null) {
                   // 如果ivi不为null，则意味着验证过，不需要继续验证了
                    if (DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Package " + packageName+ " already verified: status="
                                + ivi.getStatusString());
                    }
                    return;
                }
            }

            // If any filters need to be verified, then all need to be.
            //首先判断是否需要intent验证，即遍历所有的activity，判断每一个activity是否需要进行验证，
            //只要有一个需要验证，则需要进行验证， 如果一个都没有，则不需要验证
            boolean needToVerify = false;
            for (PackageParser.Activity a : pkg.activities) {
                for (ActivityIntentInfo filter : a.intents) {
                    if (filter.needsVerification() && needsNetworkVerificationLPr(filter)) {
                        if (DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Intent filter needs verification, so processing all filters");
                        }
                        needToVerify = true;
                        break;
                    }
                }
            }

            // 如果需要验证
            if (needToVerify) {
                final int verificationId = mIntentFilterVerificationToken++;
                for (PackageParser.Activity a : pkg.activities) {
                    for (ActivityIntentInfo filter : a.intents) {
                        // 如果需要验证
                        if (filter.handlesWebUris(true) && needsNetworkVerificationLPr(filter)) {
                            if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG,
                                    "Verification needed for IntentFilter:" + filter.toString());
                            // 把验证加入到mIntentFilterVerifier里面
                            mIntentFilterVerifier.addOneIntentFilterVerification(
                                    verifierUid, userId, verificationId, filter, packageName);
                             // 需要验证数量+1
                            count++;
                        }
                    }
                }
            }
        }

       // 如果验证数量大于0，开启验证
        if (count > 0) {
            if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG, "Starting " + count
                    + " IntentFilter verification" + (count > 1 ? "s" : "")
                    +  " for userId:" + userId);
            mIntentFilterVerifier.startVerifications(userId);
        } else {
            if (DEBUG_DOMAIN_VERIFICATION) {
                Slog.d(TAG, "No filters or not all autoVerify for " + packageName);
            }
        }
    }
```

这个方法内部首先判断这个安装包中的Activity的个数，如果一个Activity都没有，则不需要验证。然后用获取是否设置url验证。如果没设置，同样不需要验证。如果经历了前面的两重验证， 还没返回则说明activity的个数大于0，并且有url验证。这时候还要考虑一种情况，即新安装且已经检验过了。所以再进行判断是新安装且已经安装过的情况。最后开始遍历安装包的每一个activity，判断是否有验证的设置。如果连一个activity的验证设置都没有，则不需要验证。如果有验证设置则将验证设置添加到mIntentFilterVerifier中，并给count+1。最后如果count>0，则说明有验证设置，最后调用mIntentFilterVerifier.startVerifications(userId)这行代码进行验证。

那就来看下mIntentFilterVerifier.startVerifications(userId)这行代码的内部执行逻辑。

### 4、IntentFilterVerifier#startVerifications(int)方法解析

首先知道IntentFilterVerifier是一个接口
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 622行

```java
    private interface IntentFilterVerifier<T extends IntentFilter> {
        boolean addOneIntentFilterVerification(int verifierId, int userId, int verificationId,
                                               T filter, String packageName);
        void startVerifications(int userId);
        void receiveVerificationResponse(int verificationId);
    }
```

既然是一个接口，要找到它的具体实现类
在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)的构造函数里面有对mIntentFilterVerifier进行初始化。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 2335行

```java
    public PackageManagerService(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {
            ...
            mIntentFilterVerifier = new IntentVerifierProxy(mContext,
                    mIntentFilterVerifierComponent);
            ...
    }
```

```java

```

所以知道了mIntentFilterVerifier其实就是IntentVerifierProxy对象

那来看下IntentVerifierProxy的startVerifications(int)方法的具体实现
 代码在[PackageManagerService.java](https://link.jianshu.com?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 644行

```dart
        @Override
        public void startVerifications(int userId) {
            // Launch verifications requests
            // 获取总体验证的数量
            int count = mCurrentIntentFilterVerifications.size();
            // 开始遍历
            for (int n=0; n<count; n++) {
                // 首先获取验证id
                int verificationId = mCurrentIntentFilterVerifications.get(n);
                // 根据验证id 获取对应的IntentFilter
                final IntentFilterVerificationState ivs =
                        mIntentFilterVerificationStates.get(verificationId);
                // 获取包名
                String packageName = ivs.getPackageName();
                 // 根据验证获取其对应的filters
                ArrayList<PackageParser.ActivityIntentInfo> filters = ivs.getFilters();
                // 获取filters的数量，方便后续的遍历
                final int filterCount = filters.size();
                // 创建ArraySet
                ArraySet<String> domainsSet = new ArraySet<>();
               // 开始遍历filters
                for (int m=0; m<filterCount; m++) {
                    // 获取其对应的具体某一个 PackageParser.ActivityIntentInfo
                    PackageParser.ActivityIntentInfo filter = filters.get(m);
                     // 把PackageParser.ActivityIntentInfo添加到domainsSet中
                    domainsSet.addAll(filter.getHostsList());
                }
                //把ArraySet转化为ArrayList
                ArrayList<String> domainsList = new ArrayList<>(domainsSet);
                synchronized (mPackages) {
                     // 根据包名获取其对应的PackageSetting，然后调用setIntentFilterVerificationInfo把其对应的IntentFilterVerificationInfo添加到PackageSetting中去
                    if (mSettings.createIntentFilterVerificationIfNeededLPw(
                            packageName, domainsList) != null) {
                        // 延迟写入
                        scheduleWriteSettingsLocked();
                    }
                }
                // 发送验证广播
                sendVerificationRequest(userId, verificationId, ivs);
            }
           // 清空
            mCurrentIntentFilterVerifications.clear();
        }
```

注释已经很清楚了，关注下最后的一个方法sendVerificationRequest方法。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 673行

```cpp
        private void sendVerificationRequest(int userId, int verificationId,
                IntentFilterVerificationState ivs) {

            Intent verificationIntent = new Intent(Intent.ACTION_INTENT_FILTER_NEEDS_VERIFICATION);
            verificationIntent.putExtra(
                    PackageManager.EXTRA_INTENT_FILTER_VERIFICATION_ID,
                    verificationId);
            verificationIntent.putExtra(
                    PackageManager.EXTRA_INTENT_FILTER_VERIFICATION_URI_SCHEME,
                    getDefaultScheme());
            verificationIntent.putExtra(
                    PackageManager.EXTRA_INTENT_FILTER_VERIFICATION_HOSTS,
                    ivs.getHostsString());
            verificationIntent.putExtra(
                    PackageManager.EXTRA_INTENT_FILTER_VERIFICATION_PACKAGE_NAME,
                    ivs.getPackageName());
            verificationIntent.setComponent(mIntentFilterVerifierComponent);
            verificationIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

            UserHandle user = new UserHandle(userId);
            mContext.sendBroadcastAsUser(verificationIntent, user);
            if (DEBUG_DOMAIN_VERIFICATION) Slog.d(TAG,
                    "Sending IntentFilter verification broadcast");
        }
```

看到这个方法里面什么也没做，就是发送了一个广播。

## 参考文章

1. [APK安装流程详解15——PMS中的新安装流程下(装载)补充](https://www.jianshu.com/p/6f8fc521512e)

