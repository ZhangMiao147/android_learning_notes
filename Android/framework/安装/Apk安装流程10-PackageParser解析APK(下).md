# Apk 安装流程10-PackageParser 解析 APK（下）

本片文章的主要内容如下：

```
12、PackageParse#parseBaseApplication(Package, Resources,XmlPullParser, AttributeSet, int, String[])方法解析
13、PackageParse#parseClusterPackage(File,int)方法解析
14、PackageParse#parseClusterPackageLite(File,int)方法解析
15、PackageParse#loadApkIntoAssetManager(AssetManager, String, int)方法解析
16、PackageParse#parseSplitApk(Package, int, AssetManager, int)方法解析
17、PackageParse#parseSplitApk(Package, Resources, XmlResourceParser, int,int, String[])方法解析
18、PackageParse#parseSplitApplication(Package, Resources, XmlResourceParser, int, int, String[]) 方法解析
19、PackageParse#parseActivity(Package, Resources,XmlPullParser, AttributeSet, int, String[],boolean, boolean)方法解析
20、总结
```

## 十二、PackageParse#parseBaseApplication(Package, Resources,XmlPullParser, AttributeSet, int, String[])方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 2406行

```csharp
/**
     * Parse the {@code application} XML tree at the current parse location in a
     * <em>base APK</em> manifest.
     * <p>
     * When adding new features, carefully consider if they should also be
     * supported by split APKs.
     */
    private boolean parseBaseApplication(Package owner, Resources res,
            XmlPullParser parser, AttributeSet attrs, int flags, String[] outError)
        throws XmlPullParserException, IOException {
        // 获取ApplicationInfo对象ai
        final ApplicationInfo ai = owner.applicationInfo;
        // 获取包名
        final String pkgName = owner.applicationInfo.packageName;
         // 从资源里面获取AndroidManifest的数组 
        TypedArray sa = res.obtainAttributes(attrs,
                com.android.internal.R.styleable.AndroidManifestApplication);
         // 获取Application的名字
        String name = sa.getNonConfigurationString(
                com.android.internal.R.styleable.AndroidManifestApplication_name, 0);
        if (name != null) {
             // 如果有设置过Application，则设置ApplicationInfo的类名
            ai.className = buildClassName(pkgName, name, outError);
            if (ai.className == null) {
                // 如果设置过Application则直接返回
                sa.recycle();
                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
        }
        // 在AndroidManifest里面是否设置了android:manageSpaceActivity属性，如果设置了则manageSpaceActivity不为空，没有设置manageSpaceActivity为空
        String manageSpaceActivity = sa.getNonConfigurationString(
                com.android.internal.R.styleable.AndroidManifestApplication_manageSpaceActivity,
                Configuration.NATIVE_CONFIG_VERSION);
        if (manageSpaceActivity != null) {
             // 如果设置了，则添加类名
            ai.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity,
                    outError);
        }
        // 是否设置了androidMannifest.xml文件中android:allowBackup属性;
        boolean allowBackup = sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_allowBackup, true);
        // 如果设置了 允许备份
        if (allowBackup) {
            ai.flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;

            // backupAgent, killAfterRestore, fullBackupContent and restoreAnyVersion are only
            // relevant if backup is possible for the given application.
            // 获取backupAgent，如果在AndroidManifest里面设置了android:backupAgent属性，则backupAgent不为空，否则backupAgent为空
            String backupAgent = sa.getNonConfigurationString(
                    com.android.internal.R.styleable.AndroidManifestApplication_backupAgent,
                    Configuration.NATIVE_CONFIG_VERSION);
            if (backupAgent != null) {
                // 设置了backupAgent，这构建类名
                ai.backupAgentName = buildClassName(pkgName, backupAgent, outError);
                if (DEBUG_BACKUP) {
                    Slog.v(TAG, "android:backupAgent = " + ai.backupAgentName
                            + " from " + pkgName + "+" + backupAgent);
                }

                 //是否设置了killAfterRestore属性，即在AndroidManfest中是否设置了android:killAfterRestore=true，如果设置了，配置相应标志
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestApplication_killAfterRestore,
                        true)) {
                    ai.flags |= ApplicationInfo.FLAG_KILL_AFTER_RESTORE;
                }

                //是否设置了restoreAnyVersion属性，即在AndroidManfest中是否设置了restoreAnyVersion=boolean，如果设置了，配置相应标志
                // 这里restoreAnyVersion属性 是指是否允许回复任意版本的本分数据来恢复应用程序的数据。
                //将该属性设置为true,则将允许本分管理器尝试恢复操作，有的时候版本不匹配表明数据是不兼容的，
                // 这个时候如果可以恢复到不同的版本的数据，那么应用程序将承受很大风险，所以请谨慎使用此属性。
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestApplication_restoreAnyVersion,
                        false)) {
                    ai.flags |= ApplicationInfo.FLAG_RESTORE_ANY_VERSION;
                }

                 // 是否开启[Auto Backup for Apps](https://developer.android.com/guide/topics/data/autobackup.html)功能
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestApplication_fullBackupOnly,
                        false)) {
                    ai.flags |= ApplicationInfo.FLAG_FULL_BACKUP_ONLY;
                }
            }

            // 获取android:fullBackupContent的属性，这个标示用来指明备份数据的规则，该标示是配合[Auto Backup for Apps](https://developer.android.com/guide/topics/data/autobackup.html)来使用的
            TypedValue v = sa.peekValue(
                    com.android.internal.R.styleable.AndroidManifestApplication_fullBackupContent);
            if (v != null && (ai.fullBackupContent = v.resourceId) == 0) {
                if (DEBUG_BACKUP) {
                    Slog.v(TAG, "fullBackupContent specified as boolean=" +
                            (v.data == 0 ? "false" : "true"));
                }
                // "false" => -1, "true" => 0
                ai.fullBackupContent = (v.data == 0 ? -1 : 0);
            }
            if (DEBUG_BACKUP) {
                Slog.v(TAG, "fullBackupContent=" + ai.fullBackupContent + " for " + pkgName);
            }
        }

         // 获取<Application> 里面的"label"属性，并设置相应的属性
        TypedValue v = sa.peekValue(
                com.android.internal.R.styleable.AndroidManifestApplication_label);
        if (v != null && (ai.labelRes=v.resourceId) == 0) {
            ai.nonLocalizedLabel = v.coerceToString();
        }
        // 设置icon、logo、banner、theme、descriptionRes属性
        ai.icon = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestApplication_icon, 0);
        ai.logo = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestApplication_logo, 0);
        ai.banner = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestApplication_banner, 0);
        ai.theme = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestApplication_theme, 0);
        ai.descriptionRes = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestApplication_description, 0);

        // 判断是否是系统APP
        if ((flags&PARSE_IS_SYSTEM) != 0) {
            // 判断是否长期驻留
            if (sa.getBoolean(
                    com.android.internal.R.styleable.AndroidManifestApplication_persistent,
                    false)) {
                ai.flags |= ApplicationInfo.FLAG_PERSISTENT;
            }
        }

        // 获取应用的android:requiredForAllUsers属性，是否设置该应用是否需要对所有用户可用
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_requiredForAllUsers,
                false)) {
            owner.mRequiredForAllUsers = true;
        }
   
        // 设置android:restrictedAccountType属性
         // 是否允许受限用户访问机主的该账户，如果应用程序要使用Account 并且允许受限用户访问主账户
         // 本属性值必须与应用程序账户认证类型(由AuthenticatorDescription)定义吻合。
        // 如果设置了本属性将允许受限用户通过主账户使用你的应用程序，这可能会泄露个人身份信息。
        String restrictedAccountType = sa.getString(com.android.internal.R.styleable
                .AndroidManifestApplication_restrictedAccountType);
        if (restrictedAccountType != null && restrictedAccountType.length() > 0) {
            owner.mRestrictedAccountType = restrictedAccountType;
        }

         // 设置 android:restrictedAccountType中的值
         // 设置应用程序所需的账户类型。如果应用程序需要一个Account才能运行，本属性必须与账户认证类型(由AuthenticatorDescription 定义)吻合。 
        String requiredAccountType = sa.getString(com.android.internal.R.styleable
                .AndroidManifestApplication_requiredAccountType);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            owner.mRequiredAccountType = requiredAccountType;
        }

       // 是否设置了 android:debuggable
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_debuggable,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
        }

        // 是否设置了 android:vmSafeMode属性
        // 这个表示用来指明这个应用是否想让 VM虚拟机运行在安全模式，默认值为false
        //这个标示是API 18版本添加，如果设置 true 将会禁用 Dalvik just-in-time(JIT)编译器，
        // 这个标示在 API 22 版本之后为新版本做了改进，因为4.4 之后 Dalvik 虚拟机就被废弃
        // 在22版本 之后这个标示如果设置 为true 将会禁用ART ahead-of-time（AOT） 编译器
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_vmSafeMode,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_VM_SAFE_MODE;
        }

        // 设置 是否硬件加速
        owner.baseHardwareAccelerated = sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_hardwareAccelerated,
                owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
        if (owner.baseHardwareAccelerated) {
            ai.flags |= ApplicationInfo.FLAG_HARDWARE_ACCELERATED;
        }

        // 是否包含代码 对应AndroidManifest里面的android:hasCode
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_hasCode,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_HAS_CODE;
        }
        // 是否设置了"android:allowTaskReparenting=boolean"，这个标示和Application 的 标示意义一样，
        // 所以如果同时声明该标示，这个标示会覆盖Application 的标示
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_allowTaskReparenting,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
        }
  
       // 是否设置了android:allowClearUserData=boolean。是否给用户删除用户数据的权限，
       // 如果为true应用管理者就拥有了清楚数据的权限；false没有。默认为true
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_allowClearUserData,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
        }

        // 是否设置"android:testOnly=boolean"，这个标示用来指明这个应用是不是仅仅作为测试的用户，
        // 比如本应用程序可能会暴露一些不属于自己的功能或数据，这将引发安全漏洞，
        // 但对测试而言这又非常有用，而且这种应用程序只能通过 adb 进行安全
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_testOnly,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_TEST_ONLY;
        }

        // 对应"android:largeHeap=boolean"属性，这个标示用来表明这个应用的进程是否需要更大的运行内存空间，这个标示对该应用的所有进程有效
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_largeHeap,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_LARGE_HEAP;
        }

        // 对应"android:usesCleartextTraffic=boolean"属性。默认值为true
        //  它用来指明应用是否需要使用明文的网络连接，例如明文的HTTP连接
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_usesCleartextTraffic,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC;
        }

       // 对应"android:supportsRtl=boolean"，这个标示是用来声明应用是否支持从右到左的布局方式
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_supportsRtl,
                false /* default is no RTL support*/)) {
            ai.flags |= ApplicationInfo.FLAG_SUPPORTS_RTL;
        }

        // 是否支持多平台
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_multiArch,
                false)) {
            ai.flags |= ApplicationInfo.FLAG_MULTIARCH;
        }

        // 对应AndroidManifest的"android:extractNativeLibs=boolean"，这个标示是Android 6.0引入。
        // 该属性如果设置了 false，则系统在安装系统的时候不会把so文件从apk中解压出来了
        // 同时修改了System.loadLibrary 直接打开调用apk中的.so文件。但是，目前要让该熟悉感生效还有两个条件:
        // 一是apk中的.so文件不能被压缩；二是.so必须是用zipalign -p 4来对齐。该标示的默认值为true。
        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_extractNativeLibs,
                true)) {
            ai.flags |= ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS;
        }

        String str;
        // 对应"android:permission=string"属性
        str = sa.getNonConfigurationString(
                com.android.internal.R.styleable.AndroidManifestApplication_permission, 0);
        ai.permission = (str != null && str.length() > 0) ? str.intern() : null;

        // 解析"android:allowTaskReparenting=boolean" 设置吸附质，影响Activity的启动模式
        if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
            str = sa.getNonConfigurationString(
                    com.android.internal.R.styleable.AndroidManifestApplication_taskAffinity,
                    Configuration.NATIVE_CONFIG_VERSION);
        } else {
            // Some older apps have been seen to use a resource reference
            // here that on older builds was ignored (with a warning).  We
            // need to continue to do this for them so they don't break.
            str = sa.getNonResourceString(
                    com.android.internal.R.styleable.AndroidManifestApplication_taskAffinity);
        }
        // 设置吸附质
        ai.taskAffinity = buildTaskAffinityName(ai.packageName, ai.packageName,
                str, outError);

        if (outError[0] == null) {
            CharSequence pname;
     
            // 对应"android:process"属性。
            if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
                pname = sa.getNonConfigurationString(
                        com.android.internal.R.styleable.AndroidManifestApplication_process,
                        Configuration.NATIVE_CONFIG_VERSION);
            } else {
                // Some older apps have been seen to use a resource reference
                // here that on older builds was ignored (with a warning).  We
                // need to continue to do this for them so they don't break.
                pname = sa.getNonResourceString(
                        com.android.internal.R.styleable.AndroidManifestApplication_process);
            }

            // 设置进程名
            ai.processName = buildProcessName(ai.packageName, null, pname,
                    flags, mSeparateProcesses, outError);
android:enabled

            // 对应"android:enable=boolean"属性，这个标识用来表明系统能否实例化这个应用的组件。
            ai.enabled = sa.getBoolean(
                    com.android.internal.R.styleable.AndroidManifestApplication_enabled, true);

            // 对应 "android:isGame=boolean" 属性，这个标识用来表明应用是否是游戏，这样就能够将该应用和其他应用分离开，默认值为false
            if (sa.getBoolean(
                    com.android.internal.R.styleable.AndroidManifestApplication_isGame, false)) {
                ai.flags |= ApplicationInfo.FLAG_IS_GAME;
            }
            
            //  这里注意，if(false) 是一定不执行的 
            if (false) {
                // 对应的android:cantSaveState属性， 设置了则APP就可视为heavy-weight process
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestApplication_cantSaveState,
                        false)) {
                    ai.privateFlags |= ApplicationInfo.PRIVATE_FLAG_CANT_SAVE_STATE;

                    // A heavy-weight application can not be in a custom process.
                    // We can do direct compare because we intern all strings.
                    if (ai.processName != null && ai.processName != ai.packageName) {
                        outError[0] = "cantSaveState applications can not use custom processes";
                    }
                }
            }
        }

        // 对应"android:uiOptions"，标识分离式操作栏
        ai.uiOptions = sa.getInt(
                com.android.internal.R.styleable.AndroidManifestApplication_uiOptions, 0);

        // 回收
        sa.recycle();

        if (outError[0] != null) {
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }

        final int innerDepth = parser.getDepth();
        int type;
        // 开始解析
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
            // 解析activity
            if (tagName.equals("activity")) {
                Activity a = parseActivity(owner, res, parser, attrs, flags, outError, false,
                        owner.baseHardwareAccelerated);
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

                owner.activities.add(a);

            } else if (tagName.equals("receiver")) {
                // 解析receiver
                Activity a = parseActivity(owner, res, parser, attrs, flags, outError, true, false);
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

                owner.receivers.add(a);

            } else if (tagName.equals("service")) {
                // 解析service
                Service s = parseService(owner, res, parser, attrs, flags, outError);
                if (s == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

                owner.services.add(s);

            } else if (tagName.equals("provider")) {
                // 解析provider
                Provider p = parseProvider(owner, res, parser, attrs, flags, outError);
                if (p == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

                owner.providers.add(p);

            } else if (tagName.equals("activity-alias")) {
                 // 解析 activity-alias
                Activity a = parseActivityAlias(owner, res, parser, attrs, flags, outError);
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

                owner.activities.add(a);

            } else if (parser.getName().equals("meta-data")) {
                // 解析meta-data
                // note: application meta-data is stored off to the side, so it can
                // remain null in the primary copy (we like to avoid extra copies because
                // it can be large)
                if ((owner.mAppMetaData = parseMetaData(res, parser, attrs, owner.mAppMetaData,
                        outError)) == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

            } else if (tagName.equals("library")) {
                // 解析<library>标签
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestLibrary);
                // Note: don't allow this value to be a reference to a resource
                // that may change.
                String lname = sa.getNonResourceString(
                        com.android.internal.R.styleable.AndroidManifestLibrary_name);

                sa.recycle();

                if (lname != null) {
                    lname = lname.intern();
                    if (!ArrayUtils.contains(owner.libraryNames, lname)) {
                        owner.libraryNames = ArrayUtils.add(owner.libraryNames, lname);
                    }
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("uses-library")) {
                // 解析<uses-library>标签
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary);

                // Note: don't allow this value to be a reference to a resource
                // that may change.
                String lname = sa.getNonResourceString(
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary_name);
                boolean req = sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary_required,
                        true);

                sa.recycle();

                if (lname != null) {
                    lname = lname.intern();
                    if (req) {
                        owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                    } else {
                        owner.usesOptionalLibraries = ArrayUtils.add(
                                owner.usesOptionalLibraries, lname);
                    }
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("uses-package")) {
                // 解析<uses-package>标签
                // Dependencies for app installers; we don't currently try to
                // enforce this.
                XmlUtils.skipCurrentTag(parser);

            } else {
                if (!RIGID_PARSER) {
                    Slog.w(TAG, "Unknown element under <application>: " + tagName
                            + " at " + mArchiveSourcePath + " "
                            + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                    continue;
                } else {
                    outError[0] = "Bad element under <application>: " + tagName;
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
            }
        }

        modifySharedLibrariesForBackwardCompatibility(owner);

        // 检查IntentFilter之一是否包含DEFAULT / VIEW和HTTP / HTTPS数据URI
        if (hasDomainURLs(owner)) {
            owner.applicationInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_HAS_DOMAIN_URLS;
        } else {
            owner.applicationInfo.privateFlags &= ~ApplicationInfo.PRIVATE_FLAG_HAS_DOMAIN_URLS;
        }

        return true;
    }
```

先来翻译下注释：

解析"base APK" 的manifest的XML树中
添加新特性的时候，请仔细思考是否支持"拆分APK"。

其实这个方法就是解析了application节点下的所有信息，比如activity、service、receiver、provider、library、users-librayry等信息，同时将解析后的每一个属性生成相应的对象，添加到传入的package里面，这些信息最后都会在PackageManagerService中用到。

## 十三、PackageParse#parseClusterPackage(File,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 769行

```dart
    /**
     * Parse all APKs contained in the given directory, treating them as a
     * single package. This also performs sanity checking, such as requiring
     * identical package name and version codes, a single base APK, and unique
     * split names.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     */
    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
        // ************ 第一步 *************
        // 解析目录，并获取对应的PackageLite
        final PackageLite lite = parseClusterPackageLite(packageDir, 0);
         // 核心应用的判断
        if (mOnlyCoreApps && !lite.coreApp) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_MANIFEST_MALFORMED,
                    "Not a coreApp: " + packageDir);
        }

         // ************ 第二步 *************
        // 获取AssetManager对象
        final AssetManager assets = new AssetManager();
        try {
            // Load the base and all splits into the AssetManager
            // so that resources can be overriden when parsing the manifests.
             // 载入AssetManager到base APK中
            loadApkIntoAssetManager(assets, lite.baseCodePath, flags);
            // 把AssetManager 载入到每个"拆分APK"中
            if (!ArrayUtils.isEmpty(lite.splitCodePaths)) {
                for (String path : lite.splitCodePaths) {
                    loadApkIntoAssetManager(assets, path, flags);
                }
            }
             // ************ 第三步 *************
            final File baseApk = new File(lite.baseCodePath);
            // 开始解析"base" APK文件，并获得对应的Package 对象
            final Package pkg = parseBaseApk(baseApk, assets, flags);
            if (pkg == null) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                        "Failed to parse base APK: " + baseApk);
            }
            // ************ 第四步 *************
           // 开始解析"拆分APK"。
            if (!ArrayUtils.isEmpty(lite.splitNames)) {
                final int num = lite.splitNames.length;
                pkg.splitNames = lite.splitNames;
                pkg.splitCodePaths = lite.splitCodePaths;
                pkg.splitRevisionCodes = lite.splitRevisionCodes;
                pkg.splitFlags = new int[num];
                pkg.splitPrivateFlags = new int[num];

                for (int i = 0; i < num; i++) {
                    parseSplitApk(pkg, i, assets, flags);
                }
            }
            // 设置相应属性
            pkg.setCodePath(packageDir.getAbsolutePath());
            pkg.setUse32bitAbi(lite.use32bitAbi);
            return pkg;
        } finally {
           // 关闭
            IoUtils.closeQuietly(assets);
        }
    }
```

该方法有注释，先来看下方法的注释：

将目录视为一个单独的APK安装包，解析这个目录下的所有APK安装包。同样也执行例行检查，比如检查"base APK"和"拆分APK"是否有相同的安装包包名和版本号。
 注意：这个方法执行签名验证，所以要单独的调用collectCertificates(Package,int)方法。

这个方法理解：

**parseClusterPackage的主要内容，就是用于解析存在多个APK的文件的Package。我将本方法分为四个步骤：**

- 第一步：调用parseClusterPackageLite解析目录下的多APK文件，获取对应的PackageLite对象lite
- 第二步：创建AssetManager对象，并调用loadApkIntoAssetManager方法载入"base APK"。�
- 第三步：调用parseBaseApk方法获取对应的Package对象
- 第四步：遍历所有"拆分APK"，然后载入第二步创建的AssetManager对象，这样就实现了资源文件的载入。

这里面涉及到了几个方法：

* parseBaseApk(File, AssetManager, int)方法

* parseClusterPackageLite(File,int)方法

* loadApkIntoAssetManager(AssetManager,String,int)方法

* parseSplitApk(Package, int, AssetManager, int)方法

其中第一个parseBaseApk(File, AssetManager, int)方法，已经讲解过了，请参考APK安装流程详解9——PackageParser解析APK(上)中 ***五、PackageParse#parseMonolithicPackage(File, int)方法解析\***（解析给定的APK文件，返回值为Package）

下面我们依次讲解下上面的其他三个方法。

## 十四、PackageParse#parseClusterPackageLite(File,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 664行

```dart
    private static PackageLite parseClusterPackageLite(File packageDir, int flags)
            throws PackageParserException {
        // ************** 第一步 **************
        // 获取目录下的所有文件
        final File[] files = packageDir.listFiles();
        // 非空判断
        if (ArrayUtils.isEmpty(files)) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                    "No packages found in split");
        }
        // 初始化
        String packageName = null;
        int versionCode = 0;
       
        // 开始验证包名和版本号是否一致
        final ArrayMap<String, ApkLite> apks = new ArrayMap<>();
        // 遍历所有文件
        for (File file : files) {
            if (isApkFile(file)) {
                //解析单个APK文件成ApkLite对象
                final ApkLite lite = parseApkLite(file, flags);

                // Assert that all package names and version codes are
                // consistent with the first one we encounter.
                // 遍历的时候只有第一个文件的时候packageName为null
                if (packageName == null) {
                    packageName = lite.packageName;
                    versionCode = lite.versionCode;
                } else {
                    // 对比当前的lite对象和上一个lite对象的包名是否一致
                    if (!packageName.equals(lite.packageName)) {
                        throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_MANIFEST,
                                "Inconsistent package " + lite.packageName + " in " + file
                                + "; expected " + packageName);
                    }
                    // 对比当前的lite对象和上一个lite对象的版本号是否一致
                    if (versionCode != lite.versionCode) {
                        throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_MANIFEST,
                                "Inconsistent version " + lite.versionCode + " in " + file
                                + "; expected " + versionCode);
                    }
                }

                // Assert that each split is defined only once
                // 保证不重复添加
                if (apks.put(lite.splitName, lite) != null) {
                    throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_MANIFEST,
                            "Split name " + lite.splitName
                            + " defined more than once; most recent was " + file);
                }
            }
        }
        // ************** 第二步 **************
        // 获取base APK
        final ApkLite baseApk = apks.remove(null);
        if (baseApk == null) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_MANIFEST,
                    "Missing base APK in " + packageDir);
        }

        // Always apply deterministic ordering based on splitName

        final int size = apks.size();

        
        String[] splitNames = null;
        String[] splitCodePaths = null;
        int[] splitRevisionCodes = null;
        if (size > 0) {
            splitNames = new String[size];
            splitCodePaths = new String[size];
            splitRevisionCodes = new int[size];
            // 初始化 splitNames
            splitNames = apks.keySet().toArray(splitNames);
             //  splitNames排序
            Arrays.sort(splitNames, sSplitNameComparator);
            // 初始化splitCodePaths和splitRevisionCodes
            for (int i = 0; i < size; i++) {
                splitCodePaths[i] = apks.get(splitNames[i]).codePath;
                splitRevisionCodes[i] = apks.get(splitNames[i]).revisionCode;
            }
        }

        final String codePath = packageDir.getAbsolutePath();
        // ************** 第三步 **************
        return new PackageLite(codePath, baseApk, splitNames, splitCodePaths,
                splitRevisionCodes);
    }
```

我将这个方法分为3个步骤：

* 第一步：理性校验，主要是校验包名和版本号是否一致

* 第二步：给baseApk、splitNames、splitCodePaths、splitRevisionCodes、codePath完成初始化

* 第三步：根据第二步给出的变量，new一个PackageLite对象

## 十五、PackageParse#loadApkIntoAssetManager(AssetManager, String, int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 846行

```java
    private static int loadApkIntoAssetManager(AssetManager assets, String apkPath, int flags)
            throws PackageParserException {
        // 简单的检查
        if ((flags & PARSE_MUST_BE_APK) != 0 && !isApkPath(apkPath)) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                    "Invalid package file: " + apkPath);
        }

        // The AssetManager guarantees uniqueness for asset paths, so if this asset path
        // already exists in the AssetManager, addAssetPath will only return the cookie
        // assigned to it.
        // 添加路径
        // **************** 核心方法 ********************
        int cookie = assets.addAssetPath(apkPath);
        if (cookie == 0) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_MANIFEST,
                    "Failed adding asset path: " + apkPath);
        }
        return cookie;
    }
```

这个方法主要让AssetManager和安装包的路径的关联，主要是调用AssetManager的addAssetPath方法进行关联。

这里大家重点关注下 *addAssetPath* 方法，在Android系统中安装包路径和AssetManager的关联是使用AssetManager#addAssetPath(String)

## 十六、PackageParse#parseSplitApk(Package, int, AssetManager, int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 913行

```dart
    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags)
            throws PackageParserException {
         // 获取路径
        final String apkPath = pkg.splitCodePaths[splitIndex];

        mParseError = PackageManager.INSTALL_SUCCEEDED;
        mArchiveSourcePath = apkPath;

        if (DEBUG_JAR) Slog.d(TAG, "Scanning split APK: " + apkPath);
        // 关联AssetManager与路径
        final int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        // 获取资源
        Resources res = null;
        XmlResourceParser parser = null;
        try {
            // 初始化资源
            res = new Resources(assets, mMetrics, null);
            assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Build.VERSION.RESOURCES_SDK_INT);
            // 初始化Xml解析器
            parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);

            final String[] outError = new String[1];
            // 解析"拆分APK"
            pkg = parseSplitApk(pkg, res, parser, flags, splitIndex, outError);
            // 解析失败
            if (pkg == null) {
                throw new PackageParserException(mParseError,
                        apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
            }

        } catch (PackageParserException e) {
            throw e;
        } catch (Exception e) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
                    "Failed to read manifest from " + apkPath, e);
        } finally {
            IoUtils.closeQuietly(parser);
        }
    }
```

这个方法不复杂，主要为了调用parseSplitApk(Package, Resources, XmlResourceParser, int,int, String[])方法而初始化相关参数而已。

那我们来看下parseSplitApk(Package, Resources, XmlResourceParser, int,int, String[])方法

## 十七、PackageParse#parseSplitApk(Package, Resources, XmlResourceParser, int,int, String[])方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 957行

```dart
    /**
     * Parse the manifest of a <em>split APK</em>.
     * <p>
     * Note that split APKs have many more restrictions on what they're capable
     * of doing, so many valid features of a base APK have been carefully
     * omitted here.
     */
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags,
            int splitIndex, String[] outError) throws XmlPullParserException, IOException,
            PackageParserException {
        AttributeSet attrs = parser;

        // We parsed manifest tag earlier; just skip past it
         // 预解析
        parsePackageSplitNames(parser, attrs);

        mParseInstrumentationArgs = null;
        mParseActivityArgs = null;
        mParseServiceArgs = null;
        mParseProviderArgs = null;

        int type;

        boolean foundApp = false;
        
        // 解析 AndroidManifest里面的<Application> 标签
        int outerDepth = parser.getDepth();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
             //遇到<application> 标签
            if (tagName.equals("application")) {
                 // 保证只有一个<application> 标签
                if (foundApp) {
                    if (RIGID_PARSER) {
                        outError[0] = "<manifest> has more than one <application>";
                        mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return null;
                    } else {
                        Slog.w(TAG, "<manifest> has more than one <application>");
                        XmlUtils.skipCurrentTag(parser);
                        continue;
                    }
                }
                
                foundApp = true;
                // 解析 <application> 标签
                if (!parseSplitApplication(pkg, res, parser, flags, splitIndex, outError)) {
                    return null;
                }

            } else if (RIGID_PARSER) {
                outError[0] = "Bad element under <manifest>: "
                    + parser.getName();
                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;

            } else {
                Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName()
                        + " at " + mArchiveSourcePath + " "
                        + parser.getPositionDescription());
                XmlUtils.skipCurrentTag(parser);
                continue;
            }
        }

        if (!foundApp) {
            outError[0] = "<manifest> does not contain an <application>";
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }

        return pkg;
    }
```

有注释，先来翻译一下注释：

解析"拆分APK"的manifest
注意：由于对"拆分APK"限制比较多，所以像"base APK"的很多功能在"拆分APK"中已经省略了。

这个方法内部主要是解析"拆分APK"的AndroidManifest 文件，如果遇到\<application> 标签，则调用parseSplitApplication(Package, Resources, XmlResourceParser, int, int, String[]) 方法进行解析

那我们就来看下这个方法。

## 十八、PackageParse#parseSplitApplication(Package, Resources, XmlResourceParser, int, int, String[]) 方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 2854行

```csharp
    /**
     * Parse the {@code application} XML tree at the current parse location in a
     * <em>split APK</em> manifest.
     * <p>
     * Note that split APKs have many more restrictions on what they're capable
     * of doing, so many valid features of a base APK have been carefully
     * omitted here.
     */
    private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser,
            int flags, int splitIndex, String[] outError)
            throws XmlPullParserException, IOException {
        // 获取TypedArray对象
        TypedArray sa = res.obtainAttributes(parser,
                com.android.internal.R.styleable.AndroidManifestApplication);

        if (sa.getBoolean(
                com.android.internal.R.styleable.AndroidManifestApplication_hasCode, true)) {
            owner.splitFlags[splitIndex] |= ApplicationInfo.FLAG_HAS_CODE;
        }

        final int innerDepth = parser.getDepth();
        int type;
       // 开始解析manifest的XML
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            // 获取标签名
            String tagName = parser.getName();
            // 如果标签是<activity>
            if (tagName.equals("activity")) {
                // 解析<activity>标签
                Activity a = parseActivity(owner, res, parser, flags, outError, false,
                        owner.baseHardwareAccelerated);
               // 解析失败
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                // 将解析出来的activity添加到activities中
                owner.activities.add(a);

            } else if (tagName.equals("receiver")) {
                 // 解析<receiver>标签
                Activity a = parseActivity(owner, res, parser, flags, outError, true, false);
                // 解析失败
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                // 将解析出来的activity添加到activities中
                owner.receivers.add(a);

            } else if (tagName.equals("service")) {
                // 解析<service>标签
                Service s = parseService(owner, res, parser, flags, outError);
                 // 解析失败
                if (s == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                // 将解析出来的service添加到services中
                owner.services.add(s);

            } else if (tagName.equals("provider")) {
                // 解析<provider>标签
                Provider p = parseProvider(owner, res, parser, flags, outError);
                // 解析事变
                if (p == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                // 将解析出来的providers添加到providers中
                owner.providers.add(p);

            } else if (tagName.equals("activity-alias")) {
                // 解析<activity-alias>标签
                Activity a = parseActivityAlias(owner, res, parser, flags, outError);
                 // 解析失败
                if (a == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                // 将解析出来的activity添加到activities中
                owner.activities.add(a);

            } else if (parser.getName().equals("meta-data")) {
                // note: application meta-data is stored off to the side, so it can
                // remain null in the primary copy (we like to avoid extra copies because
                // it can be large)
                 // 解析<meta-data>标签
                if ((owner.mAppMetaData = parseMetaData(res, parser, owner.mAppMetaData,
                        outError)) == null) {
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }

            } else if (tagName.equals("uses-library")) {
                 // 如果标签是<uses-library>
                 // 重新获取TypedArray对象sa
                sa = res.obtainAttributes(parser,
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary);

                // Note: don't allow this value to be a reference to a resource
                // that may change.
                // 获取库名字
                String lname = sa.getNonResourceString(
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary_name);
                // 是否设置了required=true
                boolean req = sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestUsesLibrary_required,
                        true);
                //  回收 sa
                sa.recycle();

                if (lname != null) {
                    // 获取字符串常量池的值
                    lname = lname.intern();
                    if (req) {
                        // 如果设置了required=true
                        // Upgrade to treat as stronger constraint
                        则将这库添加进usesLibraries去
                        owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                        owner.usesOptionalLibraries = ArrayUtils.remove(
                                owner.usesOptionalLibraries, lname);
                    } else {
                         // 如果没有设置required=true并且则将这库不在usesLibraries中，则将这个库添加到usesOptionalLibraries中
                        // Ignore if someone already defined as required
                        if (!ArrayUtils.contains(owner.usesLibraries, lname)) {
                            owner.usesOptionalLibraries = ArrayUtils.add(
                                    owner.usesOptionalLibraries, lname);
                        }
                    }
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("uses-package")) {
                // Dependencies for app installers; we don't currently try to
                // enforce this.
                XmlUtils.skipCurrentTag(parser);

            } else {
                // RIGID_PARSER为常量一直未false
                if (!RIGID_PARSER) {
                    Slog.w(TAG, "Unknown element under <application>: " + tagName
                            + " at " + mArchiveSourcePath + " "
                            + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                    continue;
                } else {
                    outError[0] = "Bad element under <application>: " + tagName;
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
            }
        }

        return true;
    }
```

有注释，先来翻译一下注释：

解析"拆分APK"的XML树中的< application >标签节点。
注意：由于对"拆分APK"限制比较多，所以像"base APK"的很多功能在"拆分APK"中已经省略了。

这个方法主要就是解析几个对应的标签。

## 十九、PackageParse#parseActivity(Package, Resources,XmlPullParser, AttributeSet, int, String[],boolean, boolean)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 3026行

```tsx
    private Activity parseActivity(Package owner, Resources res,
            XmlPullParser parser, AttributeSet attrs, int flags, String[] outError,
            boolean receiver, boolean hardwareAccelerated)
            throws XmlPullParserException, IOException {
        // 获取资源数组
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestActivity);
        // 初始化解析Activity参数
        if (mParseActivityArgs == null) {
            mParseActivityArgs = new ParseComponentArgs(owner, outError,
                    R.styleable.AndroidManifestActivity_name,
                    R.styleable.AndroidManifestActivity_label,
                    R.styleable.AndroidManifestActivity_icon,
                    R.styleable.AndroidManifestActivity_logo,
                    R.styleable.AndroidManifestActivity_banner,
                    mSeparateProcesses,
                    R.styleable.AndroidManifestActivity_process,
                    R.styleable.AndroidManifestActivity_description,
                    R.styleable.AndroidManifestActivity_enabled);
        }
       
         // 判断标签类型是 receiver还是activity
        mParseActivityArgs.tag = receiver ? "<receiver>" : "<activity>";

        // 初始化mParseActivityArgs的两个属性
        mParseActivityArgs.sa = sa;
        mParseActivityArgs.flags = flags;
       
        // 创建一个Activity(这里的Activity不是我们平时说的Activity，而是PackageParse的静态内部类Activity)
        Activity a = new Activity(mParseActivityArgs, new ActivityInfo());
        if (outError[0] != null) {
            // 没有出现问题，则回收
            sa.recycle();
            return null;
        }
        // 是否在AndroidManifest中设置了"android:exported"属性
        boolean setExported = sa.hasValue(R.styleable.AndroidManifestActivity_exported);
        if (setExported) {
            // 如果设置了，则进行配置
            a.info.exported = sa.getBoolean(R.styleable.AndroidManifestActivity_exported, false);
        }
        // 设置AndroidManifest里面对应的theme的值
        a.info.theme = sa.getResourceId(R.styleable.AndroidManifestActivity_theme, 0);
        // 设置AndroidManifest里面对应的uiOptions的值
        a.info.uiOptions = sa.getInt(R.styleable.AndroidManifestActivity_uiOptions,
                a.info.applicationInfo.uiOptions);
        // 获取AndroidManifest里面"android:parentActivityName=String"的值
        String parentName = sa.getNonConfigurationString(
                R.styleable.AndroidManifestActivity_parentActivityName,
                Configuration.NATIVE_CONFIG_VERSION);
        // 如果设置了android:parentActivityName
        if (parentName != null) {
            // 构建parent的类名
            String parentClassName = buildClassName(a.info.packageName, parentName, outError);
            if (outError[0] == null) {
                a.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity " + a.info.name + " specified invalid parentActivityName " +
                        parentName);
                outError[0] = null;
            }
        }

        // 获取权限permission
        String str;
        str = sa.getNonConfigurationString(R.styleable.AndroidManifestActivity_permission, 0);
        if (str == null) {
            a.info.permission = owner.applicationInfo.permission;
        } else {
            a.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
  
       // 获取是否有在这个Activity中配置了taskAffinity(吸附值)这个属性
        str = sa.getNonConfigurationString(
                R.styleable.AndroidManifestActivity_taskAffinity,
                Configuration.NATIVE_CONFIG_VERSION);
        // 设置吸附值
        a.info.taskAffinity = buildTaskAffinityName(owner.applicationInfo.packageName,
                owner.applicationInfo.taskAffinity, str, outError);
      
        // 是否在AndroidManfest里面这个activity是否配置了"android:multiprocess=boolean" 属性，如果设置了true，则该activity支持多进程
        a.info.flags = 0;
        if (sa.getBoolean(
                R.styleable.AndroidManifestActivity_multiprocess, false)) {
            a.info.flags |= ActivityInfo.FLAG_MULTIPROCESS;
        }

        // 是否在AndroidManfest里面这个activity是否配置了"android:finishOnTaskLaunch=boolean" 属性
        // 该标识用来标识每当用户再次启动其任务(在主屏幕上选择任务)时，是否应该关闭现有Activity实例。
        // "true"表示应该关闭，"false"表示不关闭。默认值为"false"。(sa.getBoolean(R.styleable.AndroidManifestActivity_finishOnTaskLaunch, false)) {
            a.info.flags |= ActivityInfo.FLAG_FINISH_ON_TASK_LAUNCH;
        }

        //是否在AndroidManfest里面这个activity是否配置了"android:clearTaskOnLaunch=boolean" 属性
        // 这个标识用来指明当前应用从主屏幕重新启动时是否都从中移除根Activity之外的所有Activity。
        //"true"表示始终将任务清楚到只剩根Activity，"false"表示不清楚，默认值为false
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_clearTaskOnLaunch, false)) {
            a.info.flags |= ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH;
        }

        // 是否在AndroidManfest里面这个activity是否配置了"android:noHistory=boolean" 属性
        // 该标示用来指定 当用户离开Activity，并且其再屏幕上不在可见时，是否应从Activity 堆栈中将其移除并完成finish()操作
        // "true"表示应该将其finish，"false"表示不应该将其finish。默认值为false。
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_noHistory, false)) {
            a.info.flags |= ActivityInfo.FLAG_NO_HISTORY;
        }

        // 是否在AndroidManfest里面这个activity是否配置了"android:alwaysRetainTaskState=boolean" 属性
       // 这个标示用来指示系统是否始终保持Activity所在的任务的状态。
        // "true" 表示支持，"false"表示允许系统在特定的情况下将任务重置到初始状态。默认值是false。
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_alwaysRetainTaskState, false)) {
            a.info.flags |= ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE;
        }

        // 是否在AndroidManfest里面这个activity是否配置了"android:stateNotNeeded=boolean" 属性
        // 该标识用来指明能否在不保存Activity的情况下将其终止并成功重启
        // "true" 表示可在不考虑其之前状态情况下重新启动，"false"表示需要之前的状态，默认值"false"。
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_stateNotNeeded, false)) {
            a.info.flags |= ActivityInfo.FLAG_STATE_NOT_NEEDED;
        }

        // 是否在AndroidManifest里面设置"android:excludeFromRecents=boolean"
         // 该标识用来表示是否应该将Activity启动的任务是否排除在最近使用的应用列表之外。
         // "true"表示将任务排除在列表之外，"false"表示包含在内。默认值为"false"
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_excludeFromRecents, false)) {
            a.info.flags |= ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS;
        }

        // 是否在AndroidManifest里面设置"android:allowTaskReparenting=boolean"
        // 表明这个应用在reset task时，是不是关联对应的taskAffinity值。true，关联；false，不关联
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_allowTaskReparenting,
                (owner.applicationInfo.flags&ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING) != 0)) {
            a.info.flags |= ActivityInfo.FLAG_ALLOW_TASK_REPARENTING;
        }

        // 是否在AndroidManifest里面设置"android:finishOnCloseSystemDialogs=boolean"
         // 表示 当"关闭系统窗口"请求出现时，是否销毁Activity，true销毁，false不销毁
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_finishOnCloseSystemDialogs, false)) {
            a.info.flags |= ActivityInfo.FLAG_FINISH_ON_CLOSE_SYSTEM_DIALOGS;
        }

        // 是否在AndroidManifest里面设置"android:showOnLockScreen=boolean"
         // 指定该Activity是否显示在解锁界面，true显示，false 不显示
        // 是否在AndroidManifest里面设置"android:showOnLockScreen=boolean"
         // 指定该Activity是否可以显示给所有用户，true可以，false 不可以
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_showOnLockScreen, false)
                || sa.getBoolean(R.styleable.AndroidManifestActivity_showForAllUsers, false)) {
            a.info.flags |= ActivityInfo.FLAG_SHOW_FOR_ALL_USERS;
        }

        // 是否在AndroidManifest里面设置"android:immersive=boolean" 是否设置沉浸式显示，true是，false不是
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_immersive, false)) {
            a.info.flags |= ActivityInfo.FLAG_IMMERSIVE;
        }

        // 是否在AndroidManifest里面设置"android:primaryUserOnly=boolean"
        // 是否设置视为系统组件，true是，false不是
        if (sa.getBoolean(R.styleable.AndroidManifestActivity_primaryUserOnly, false)) {
            a.info.flags |= ActivityInfo.FLAG_PRIMARY_USER_ONLY;
        }

        // 如果不是receiver标签，同时是否设置了硬件加速
        if (!receiver) {
            if (sa.getBoolean(R.styleable.AndroidManifestActivity_hardwareAccelerated,
                    hardwareAccelerated)) {
                a.info.flags |= ActivityInfo.FLAG_HARDWARE_ACCELERATED;
            }

           // 设置对应的启动模式，对应launchMode
            a.info.launchMode = sa.getInt(
                    R.styleable.AndroidManifestActivity_launchMode, ActivityInfo.LAUNCH_MULTIPLE);

           // 设置对应的启动模式，对应documentLaunchMode，主要指概览屏幕（recent app)
            a.info.documentLaunchMode = sa.getInt(
                    R.styleable.AndroidManifestActivity_documentLaunchMode,
                    ActivityInfo.DOCUMENT_LAUNCH_NONE);

            // 概览屏幕中此Activity的根位置的任务数上线
            a.info.maxRecents = sa.getInt(
                    R.styleable.AndroidManifestActivity_maxRecents,
                    ActivityManager.getDefaultAppRecentsLimitStatic());

            // 是否设置了android:configChanges
            a.info.configChanges = sa.getInt(R.styleable.AndroidManifestActivity_configChanges, 0);
            a.info.softInputMode = sa.getInt(
                    R.styleable.AndroidManifestActivity_windowSoftInputMode, 0);

            a.info.persistableMode = sa.getInteger(
                    R.styleable.AndroidManifestActivity_persistableMode,
                    ActivityInfo.PERSIST_ROOT_ONLY);

            // 是否设置了android:allowEmbedded=boolean，true表示该Activity可以作为另一个Activity的的嵌入式子项启动。
             //主要适用于可穿戴设备。
            if (sa.getBoolean(R.styleable.AndroidManifestActivity_allowEmbedded, false)) {
                a.info.flags |= ActivityInfo.FLAG_ALLOW_EMBEDDED;
            }

            // 是否设置了android:autoRemoveFromRecents=boolean
            //  表示是否Activity一直保留在概览屏幕中，直到任务中的最后一个Activity finish为止。
            // true，则自动从概览屏幕中移除任务
            if (sa.getBoolean(R.styleable.AndroidManifestActivity_autoRemoveFromRecents, false)) {
                a.info.flags |= ActivityInfo.FLAG_AUTO_REMOVE_FROM_RECENTS;
            }


            // 是否设置了android:relinquishTaskIdentity=boolean
            // 是否将其任务的标识符交给任务栈中在其之上的Activity。如果任务根Activity的该属性设置"true"，
            // 则任务会用其内的下一个Activity的Intent替换基本的Intent，直到某个Activity将其属性设置为"false"为止。
            if (sa.getBoolean(R.styleable.AndroidManifestActivity_relinquishTaskIdentity, false)) {
                a.info.flags |= ActivityInfo.FLAG_RELINQUISH_TASK_IDENTITY;
            }

            // 是否设置了android:resumeWhilePausing=boolean
            // 表示前一个Activity执行onPause的时候，当前是否Activity继续显示
            if (sa.getBoolean(R.styleable.AndroidManifestActivity_resumeWhilePausing, false)) {
                a.info.flags |= ActivityInfo.FLAG_RESUME_WHILE_PAUSING;
            }

            // 是否设置了android:resizeableActivity=boolean
            // 这个标识表示 是否支持分屏
            a.info.resizeable = sa.getBoolean(
                    R.styleable.AndroidManifestActivity_resizeableActivity, false);

            if (a.info.resizeable) {
                // Fixed screen orientation isn't supported with resizeable activities.
                a.info.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            } else {
                a.info.screenOrientation = sa.getInt(
                        R.styleable.AndroidManifestActivity_screenOrientation,
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

             // 获取对应的锁屏模式
            a.info.lockTaskLaunchMode =
                    sa.getInt(R.styleable.AndroidManifestActivity_lockTaskMode, 0);
        } else {
            // 如果是receiver标签
            // 设置启动模式 
            a.info.launchMode = ActivityInfo.LAUNCH_MULTIPLE;
            a.info.configChanges = 0;

            if (sa.getBoolean(R.styleable.AndroidManifestActivity_singleUser, false)) {
                a.info.flags |= ActivityInfo.FLAG_SINGLE_USER;
                if (a.info.exported && (flags & PARSE_IS_PRIVILEGED) == 0) {
                    Slog.w(TAG, "Activity exported request ignored due to singleUser: "
                            + a.className + " at " + mArchiveSourcePath + " "
                            + parser.getPositionDescription());
                    a.info.exported = false;
                    setExported = true;
                }
            }
        }

        sa.recycle();
         // 判断是否是重量级的APP。如果是重量级的APP，则不应该有receiver
        if (receiver && (owner.applicationInfo.privateFlags
                &ApplicationInfo.PRIVATE_FLAG_CANT_SAVE_STATE) != 0) {
            // A heavy-weight application can not have receives in its main process
            // We can do direct compare because we intern all strings.
            if (a.info.processName == owner.packageName) {
                outError[0] = "Heavy-weight applications can not have receivers in main process";
            }
        }

        if (outError[0] != null) {
            return null;
        }

        int outerDepth = parser.getDepth();
        int type;
        // 开始解析<activity></activity>的内部标签
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && (type != XmlPullParser.END_TAG
                       || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

             //如果解析到<ntent-filter>标签
            if (parser.getName().equals("intent-filter")) {
                ActivityIntentInfo intent = new ActivityIntentInfo(a);
                 // 解析<ntent-filter>标签
                if (!parseIntent(res, parser, attrs, true, true, intent, outError)) {
                    return null;
                }
                if (intent.countActions() == 0) {
                    Slog.w(TAG, "No actions in intent filter at "
                            + mArchiveSourcePath + " "
                            + parser.getPositionDescription());
                } else {
                   // 添加进去
                    a.intents.add(intent);
                }
            } else if (!receiver && parser.getName().equals("preferred")) {
                // 如果是<activity>，且遇到<preferred> 标签
                ActivityIntentInfo intent = new ActivityIntentInfo(a);
                 // 解析<preferred> 标签
                if (!parseIntent(res, parser, attrs, false, false, intent, outError)) {
                    return null;
                }
                if (intent.countActions() == 0) {
                    Slog.w(TAG, "No actions in preferred at "
                            + mArchiveSourcePath + " "
                            + parser.getPositionDescription());
                } else {
                    if (owner.preferredActivityFilters == null) {
                        owner.preferredActivityFilters = new ArrayList<ActivityIntentInfo>();
                    }
                    // 添加
                    owner.preferredActivityFilters.add(intent);
                }
            } else if (parser.getName().equals("meta-data")) {
                //遇到<meta-data> 标签
                // 解析<meta-data> 标签
                if ((a.metaData=parseMetaData(res, parser, attrs, a.metaData,
                        outError)) == null) {
                    return null;
                }
            } else {
                 // 如果遇到其他的 莫名其妙的标签
                if (!RIGID_PARSER) {
                    Slog.w(TAG, "Problem in package " + mArchiveSourcePath + ":");
                    if (receiver) {
                        Slog.w(TAG, "Unknown element under <receiver>: " + parser.getName()
                                + " at " + mArchiveSourcePath + " "
                                + parser.getPositionDescription());
                    } else {
                        Slog.w(TAG, "Unknown element under <activity>: " + parser.getName()
                                + " at " + mArchiveSourcePath + " "
                                + parser.getPositionDescription());
                    }
                    XmlUtils.skipCurrentTag(parser);
                    continue;
                } else {
                    if (receiver) {
                        outError[0] = "Bad element under <receiver>: " + parser.getName();
                    } else {
                        outError[0] = "Bad element under <activity>: " + parser.getName();
                    }
                    return null;
                }
            }
        }
        if (!setExported) {
            a.info.exported = a.intents.size() > 0;
        }
        return a;
    }
```

这个方法主要是解析AndroidManifest.xml里面的\<activity>标签的内容，并将其映射到PackageParse.Activity对象。

![](https://upload-images.jianshu.io/upload_images/5713484-b0e346b70d6ab518.png)

扫描Package的第一部分工作，难度不大，但极其的繁琐，跟着流程走一边真是想死的心都有了。不过正如Torvalds大神所说的，”RTFSC, read the fucking source code”，耐着性子多看看，是提高的基础条件。

上图画出了PackageParser解析Apk文件，得到的主要的数据结构，实际的内容远多于这些，我们仅保留了四大组件和权限相关的内容。

上面这些类，全部是定义于PackageParser中的内部类，这些内部类主要的作用就是保存AndroidManifest.xml解析出的对应信息。
 以PackageParser.Activity为例，注意到该类持有ActivityInfo类，继承自Component< ActivityIntentInfo>。其中，ActivityInfo用于保存Activity的信息；Component类是一个模板，对应元素类型是ActivityIntentInfo，顶层基类为IntentFilter。四大组件中的其它成员，也有类似的继承结构。

这种设计的原因是：Package除了保存信息外，还需要支持Intent匹配查询。例如，当收到某个Intent后，由于ActivityIntentInfo继承自IntentFilter，因此它能判断自己是否满足Intent的要求。如果满足，则返回对应的ActivityInfo。

## 二十、总结

最后，我们结合上图回忆一下整个扫描过程：

* PackageParser 首先解析出了ApkLite，得到每个 Apk文件的简化信息（对于具有多个 Apk 文件的 Package 来说，将得到多个 ApkLite）；

* 利用所有的 ApkLite 及 XML 中的其它信息，解析出 PackageLite；

* 利用 PackageLite 中的信息及 XML 中的其它信息，解析出 Package 信息；Package 中就基本上涵盖了 AndroidManifest.xml 中涉及的所有信息。

注意在上述的解析过程中，PackageParser利用AssetManager存储了Package中资源文件的地址。

## 参考文章

1. [APK安装流程详解10——PackageParser解析APK(下)](https://www.jianshu.com/p/2fcd22326efb)

