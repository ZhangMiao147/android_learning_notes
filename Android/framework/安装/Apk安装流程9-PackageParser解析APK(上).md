# Apk 安装流程9-PackageParser 解析 APK（上）

本片文章的主要内容如下：

```
1、PackageParser类简介
2、PackageParser类的结构
3、PackageParser类的内部类简介
4、PackageParse#parsePackage(File, int)方法解析
5、PackageParse#parseMonolithicPackage(File, int)方法解析
6、PackageParse#parseMonolithicPackageLite(File, int)方法解析
7、PackageParse#parseApkLite(File,int)方法解析
8、PackageParse#parseApkLite(File,int)方法解析
9、PackageParse#parsePackageSplitNames(XmlpullParse，AttributeSet,int)方法解析
10、PackageParse#parseBaseApk(File,AssetManager,int)方法解析
11、PackageParse#parseBaseApk(Resources,XmlResourceParser,int,String[])方法解析
```

## 一 、PackageParser类简介

[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java)

Android 安装一个APK的时候首先会解析APK，而解析APK则需要用到一个工具类，这个工具类就是PackageParser。

### (一)、PackageParser的注释

```dart
/**
 * Parser for package files (APKs) on disk. This supports apps packaged either
 * as a single "monolithic" APK, or apps packaged as a "cluster" of multiple
 * APKs in a single directory.
 * <p>
 * Apps packaged as multiple APKs always consist of a single "base" APK (with a
 * {@code null} split name) and zero or more "split" APKs (with unique split
 * names). Any subset of those split APKs are a valid install, as long as the
 * following constraints are met:
 * <ul>
 * <li>All APKs must have the exact same package name, version code, and signing
 * certificates.
 * <li>All APKs must have unique split names.
 * <li>All installations must contain a single base APK.
 * </ul>
 *
 * @hide
 */
public class PackageParser {
          ...
}
```

解析磁盘上的APK安装包文件。它既能解析一个"单一"APK文件，也能解析一个"集群"APK文件(即一个APK文件里面包含多个APK文件)。
 一个"集群"APK有一个"基准"APK(base APK)组成和其他一些"分割"APK("split" APKs)构成，其中这些"分割"APK用一些数字来分割。这些"分割"APK的必须都是有效的安装，同时必须满足下面的几个条件：

- 所有的APK必须具有完全相同的软件包名称，版本代码和签名证书
- 所有的APK必须具有唯一的拆分名称
- 所有安装必须包含一个单一的APK。

### (二)、PackageParser的解析步骤

所以我们知道PackageParse类，它主要用来解析手机上的APK文件(支持Single APK和MultipleAPK)，解析一个APK主要是分为两个步骤：

- 1、将**APK**解析成**Package**：即解析APK文件为Package对象的过程。
- 2、将**Package**转化为**PackageInfo**：即由Package对象生成Package对象生成PackageInfo的过程。

### (三)、PackageParser中分类

上面翻译注释的时候，里面提到两个概念：**Single APK**与**Multiple APK**，那我们就来简单解释下这两种APK：

Single APK就是我们通常所开发的APK，即一个应用只有一个APK文件，而Google Play 还允许你为一个应用中发布不同的APK文件，这些APK文件适用于不同的设备，例如：你现在有一个APP叫DEMO1，但是目前由于APK的体积太大或者其他因素导致不能同时适用于手机和平板，此时你就可以将原先的DEMO.apk，拆分为Demo_phone和Demo_tablet分别用于运行在android手机和Android平板，只要保存两者拥有相同的包名，并用相同key进行签名就可以在发布Demo应用的时候，一起发布Demo_phone和Demo_tablet.apk，那么这种一个应用拥有多个APK文件的程序就称为Mutiple APK。

## 二、PackageParser类的结构

PackageParser类的结构体如下：

![](https://upload-images.jianshu.io/upload_images/5713484-633adf171cc93ff8.png)

## 三、PackageParser类的内部类简介

### (一) 静态内部类NewPermissionInfo 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 120行

```java
    /** @hide */
    public static class NewPermissionInfo {
        public final String name;
        public final int sdkVersion;
        public final int fileVersion;
        
        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            this.name = name;
            this.sdkVersion = sdkVersion;
            this.fileVersion = fileVersion;
        }
    }
```

这个类很简单，主要是记录新的权限：

- name成员变量：表示权限的名称
- sdkVersion成员变量：表示权限的开始版本号
- fileVersion成员变量：表示文件的版本号，一般为0

### (二) 静态内部类SplitPermissionInfo 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 133行

```java
    /** @hide */
    public static class SplitPermissionInfo {
        public final String rootPerm;
        public final String[] newPerms;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            this.rootPerm = rootPerm;
            this.newPerms = newPerms;
            this.targetSdk = targetSdk;
        }
    }
```

这个类很简单，主要是记录一个权限拆分为颗粒度更小的权限：

- rootPerm成员变量：表示旧的权限
- newPerms成员变量：表示旧的权限拆分为颗粒度更小的权限
- targetSdk成员变量：表示在那个版本上拆分的

### (三) 静态内部类ParsePackageItemArgs 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 203行

```dart
    static class ParsePackageItemArgs {
        final Package owner;
        final String[] outError;
        final int nameRes;
        final int labelRes;
        final int iconRes;
        final int logoRes;
        final int bannerRes;
        
        String tag;
        TypedArray sa;
        
        ParsePackageItemArgs(Package _owner, String[] _outError,
                int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes) {
            owner = _owner;
            outError = _outError;
            nameRes = _nameRes;
            labelRes = _labelRes;
            iconRes = _iconRes;
            logoRes = _logoRes;
            bannerRes = _bannerRes;
        }
    }
```

这个类很简单，主要为解析包单个item的参数：

- owner成员变量：表示安装包的包对象Package
- outError成员变量：表示错误信息
- nameRes成员变量：表示安装包中名字对应的资源id
- labelRes成员变量：表示安装包中label对应的资源id
- iconRes成员变量：表示安装包中icon对应的资源id
- logoRes成员变量：表示安装包中logo对应的资源id
- bannerRes成员变量：表示安装包中banner对应的资源id

### (四) 静态内部类ParseComponentArgs 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 227行

```dart
    static class ParseComponentArgs extends ParsePackageItemArgs {
        final String[] sepProcesses;
        final int processRes;
        final int descriptionRes;
        final int enabledRes;
        int flags;
        
        ParseComponentArgs(Package _owner, String[] _outError,
                int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes,
                String[] _sepProcesses, int _processRes,
                int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _logoRes, _bannerRes);
            sepProcesses = _sepProcesses;
            processRes = _processRes;
            descriptionRes = _descriptionRes;
            enabledRes = _enabledRes;
        }
    }
```

这个类很简单，主要为解析包中单个组件的参数：

- sepProcesses成员变量：表示该组件对应的进程，如果设置独立进程则为独立进程的名字
- processRes成员变量：表示该组件对应的进程的资源id
- descriptionRes成员变量：表示该组件对应的描述id
- enabledRes成员变量：表示该组件是否可用
- flags成员变量：表示该组件的标志位

### (五) 静态内部类PackageLite 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 249行

```kotlin
    /**
     * Lightweight parsed details about a single package.
     */
    public static class PackageLite {
        public final String packageName;
        public final int versionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;

        /** Names of any split APKs, ordered by parsed splitName */
        public final String[] splitNames;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public final String codePath;

        /** Path of base APK */
        public final String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
        public final String[] splitCodePaths;

        /** Revision code of base APK */
        public final int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        public final int[] splitRevisionCodes;

        public final boolean coreApp;
        public final boolean multiArch;
        public final boolean extractNativeLibs;

        public PackageLite(String codePath, ApkLite baseApk, String[] splitNames,
                String[] splitCodePaths, int[] splitRevisionCodes) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames;
            this.codePath = codePath;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitRevisionCodes = splitRevisionCodes;
            this.coreApp = baseApk.coreApp;
            this.multiArch = baseApk.multiArch;
            this.extractNativeLibs = baseApk.extractNativeLibs;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList<>();
            paths.add(baseCodePath);
            if (!ArrayUtils.isEmpty(splitCodePaths)) {
                Collections.addAll(paths, splitCodePaths);
            }
            return paths;
        }
    }
```

通过注释我们知道这个类表示在解析过程中的一个"轻量级的"、"独立的"安装包

- packageName成员变量：表示包名
- versionCode成员变量：表示版本号
- installLocation成员变量：安装位置的属性，有几个常量可以选择，比如**PackageInfo.INSTALL_LOCATION_AUTO的值**等。
- VerifierInfo成员变量：表示验证对象
- splitNames成员变量：如果有拆包，则拆包的名字数组。
- codePath成员变量：表示"代码"的路径，对于"单一APK"，则对应的是"base APK"的路径；如果是"集群APK"，则对应的是"集群APK"目录的路径
- baseCodePath成员变量：表示"base APK"的路径
- splitCodePaths成员变量：表示"拆分"APK的路径
- baseRevisionCode成员变量：表示"base APK"的调整版本号
- splitRevisionCodes成员变量：表示"拆分"APK的调整版本号
- coreApp成员变量：表示是不是"核心"APP
- multiArch成员变量：表示是不是支持多平台，这里主要是指CPU平台
- extractNativeLibs成员变量：表示是否需要提取"Native库"(主要是指so)

### (六) 静态内部类ApkLite 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 309行

```java
    /**
     * Lightweight parsed details about a single APK file.
     */
    public static class ApkLite {
        public final String codePath;
        public final String packageName;
        public final String splitName;
        public final int versionCode;
        public final int revisionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;
        public final Signature[] signatures;
        public final boolean coreApp;
        public final boolean multiArch;
        public final boolean extractNativeLibs;

        public ApkLite(String codePath, String packageName, String splitName, int versionCode,
                int revisionCode, int installLocation, List<VerifierInfo> verifiers,
                Signature[] signatures, boolean coreApp, boolean multiArch,
                boolean extractNativeLibs) {
            this.codePath = codePath;
            this.packageName = packageName;
            this.splitName = splitName;
            this.versionCode = versionCode;
            this.revisionCode = revisionCode;
            this.installLocation = installLocation;
            this.verifiers = verifiers.toArray(new VerifierInfo[verifiers.size()]);
            this.signatures = signatures;
            this.coreApp = coreApp;
            this.multiArch = multiArch;
            this.extractNativeLibs = extractNativeLibs;
        }
    }
```

通过注释我们知道这个类表示在解析过程中的一个"轻量级的"、"独立的"APK

- codePath成员变量：表示代码的路径
- packageName成员变量：表示包名
- splitName成员变量：表示"拆包"的包名
- versionCode成员变量：表示版本号
- revisionCode成员变量：表示调整的版本号
- installLocation成员变量：安装位置的属性，有几个常量可以选择，比如**PackageInfo.INSTALL_LOCATION_AUTO的值**等。
- VerifierInfo成员变量：表示验证对象
- Signature成员变量：表示签名对象
- coreApp成员变量：表示是不是"核心"APP
- multiArch成员变量：表示是不是支持多平台，这里主要是指CPU平台
- extractNativeLibs成员变量：表示是否需要提取"Native库"(主要是指so)

##### PS: PackageLite和ApkLite代表不同的含义，前者是指包，后者是指APK，一个包中是可能包含多个APK的。

### (七) 私有静态内部类SplitNameComparator 类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 634行

```dart
    /**
     * Used to sort a set of APKs based on their split names, always placing the
     * base APK (with {@code null} split name) first.
     */
    private static class SplitNameComparator implements Comparator<String> {
        @Override
        public int compare(String lhs, String rhs) {
            if (lhs == null) {
                return -1;
            } else if (rhs == null) {
                return 1;
            } else {
                return lhs.compareTo(rhs);
            }
        }
    }
```

这个类其实就是一个比较器，在"拆包"中的排序用的

### (八) 静态内部类Package类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4254行

```tsx
    /**
     * Representation of a full package parsed from APK files on disk. A package
     * consists of a single base APK, and zero or more split APKs.
     */
    public final static class Package {

        //表示包名
        public String packageName;

        /** Names of any split APKs, ordered by parsed splitName */
        // 表示"拆包"的包名，是个数组，每个元素代表一个"拆分"包名
        public String[] splitNames;

        // TODO: work towards making these paths invariant
        //对应一个volume的uid(后面会有专题去讲解volume)
        public String volumeUuid;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        // 表示代码的路径，如果是单个包，则表示"base"的APK的路径，如果是"集群"包，则表示的"集群"包的目录。
        public String codePath;

        /** Path of base APK */
        // "base APK"的路径
        public String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
       // "拆分 APK"的路径
        public String[] splitCodePaths;

        /** Revision code of base APK */
        // "base APK"的调整版本号
        public int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        //"拆分APK"的调整版本号
        public int[] splitRevisionCodes;

        /** Flags of any split APKs; ordered by parsed splitName */
        // "拆分APK"的标志数组
        public int[] splitFlags;

        /**
         * Private flags of any split APKs; ordered by parsed splitName.
         *
         * {@hide}
         */
        // "拆分APK"的私有标志数组
        public int[] splitPrivateFlags;
  
        // 是否支持硬件加速
        public boolean baseHardwareAccelerated;

        // For now we only support one application per package.
        // 对应ApplicationInfo对象 对应AndroidManifest里面的Application
        public final ApplicationInfo applicationInfo = new ApplicationInfo();

        // APK安装包中 AndroidManifest里面的<Permission>
        public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);

         // APK安装包中 AndroidManifest里面的<PermissionGroup>
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);

        // APK安装包中 AndroidManifest里面的<Activity>，这里面的Activity是不是我们通常说的Activity，而是PackageParse的内部类Activity
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);

        // APK安装包中 AndroidManifest里面的<Receiver>，这里面的Activity是不是我们通常说的Activity，而是PackageParse的内部类Activity
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);

        // APK安装包中 AndroidManifest里面的<Provider>，这里面的Provider是不是我们通常说的Provider，而是PackageParse的内部类Provider
        public final ArrayList<Provider> providers = new ArrayList<Provider>(0);

        // APK安装包中 AndroidManifest里面的<Service>，这里面的Service是不是我们通常说的Service，而是PackageParse的内部类Service
        public final ArrayList<Service> services = new ArrayList<Service>(0);

        // APK安装包中 AndroidManifest里面的<Instrumentation>，这里面的Instrumentation是不是我们通常说的Instrumentation，而是PackageParse的内部类Instrumentation
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(0);

        // APK安装包中请求的权限
        public final ArrayList<String> requestedPermissions = new ArrayList<String>();

        // APK安装包中 保内广播的Action
        public ArrayList<String> protectedBroadcasts;

        // APK安装包中 依赖库的名字
        public ArrayList<String> libraryNames = null;

        // APK安装包中 使用库的名字
        public ArrayList<String> usesLibraries = null;

        // APK安装包中 使用选项库的名字
        public ArrayList<String> usesOptionalLibraries = null;

        // APK安装包中 使用库的路径数组
        public String[] usesLibraryFiles = null;

        // APK安装包中 某个Activity信息的集合，在AndroidManifest里面的<preferred>标签(不在receiver里面)
        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;

        // APK安装包中 AndroidManifest中对应"original-package"的集合
        public ArrayList<String> mOriginalPackages = null;

        // 是真实包名，通常和mOriginalPackages一起使用
        public String mRealPackage = null;

         // APK安装包中 AndroidManifest中对应"adopt-permissions"集合
        public ArrayList<String> mAdoptPermissions = null;
        
        // We store the application meta-data independently to avoid multiple unwanted references
         // 我们独立的存储应用程序元数据，以避免多个不需要的引用
        public Bundle mAppMetaData = null;

        // The version code declared for this package.
        //  版本号
        public int mVersionCode;

        // The version name declared for this package.
        // 版本名
        public String mVersionName;
        
        // The shared user id that this package wants to use.
         // 共享id
        public String mSharedUserId;

        // The shared user label that this package wants to use.
        // 共享用户标签
        public int mSharedUserLabel;

        // Signatures that were read from the package.
         // 签名
        public Signature[] mSignatures;
        // 证书
        public Certificate[][] mCertificates;

        // For use by package manager service for quick lookup of
        // preferred up order. 
        // dexopt的位置，以便PackageManagerService跟踪要执行dexopt的位置
        public int mPreferredOrder = 0;

        // For use by package manager to keep track of where it needs to do dexopt.
         //  需要进行的dexopt的集合
        public final ArraySet<String> mDexOptPerformed = new ArraySet<>(4);

        // For use by package manager to keep track of when a package was last used.
        // 最后一次使用pakcage的时间的
        public long mLastPackageUsageTimeInMills;

        // // User set enabled state.
        // public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        //
        // // Whether the package has been stopped.
        // public boolean mSetStopped = false;

        // Additional data supplied by callers.
        // 附加数据
        public Object mExtras;

        // Applications hardware preferences
         // 硬件配置信息，对一个AndroidManifest里面的<uses-configuration> 标签
        public ArrayList<ConfigurationInfo> configPreferences = null;

        // Applications requested features
         //特性信息，对一个AndroidManifest里面的<uses-feature> 标签 
        public ArrayList<FeatureInfo> reqFeatures = null;

        // Applications requested feature groups
         //特性组信息，对一个AndroidManifest里面的<feature-group> 标签 
        public ArrayList<FeatureGroupInfo> featureGroups = null;

         // 安装的属性
        public int installLocation;

        // 是否是核心
        public boolean coreApp;

        /* An app that's required for all users and cannot be uninstalled for a user */
        // 是否是全局必要，所有用户都需要的应用程序，无法为用户卸载
        public boolean mRequiredForAllUsers;

        /* The restricted account authenticator type that is used by this application */
        // 受限账户的 验证类型
        public String mRestrictedAccountType;

        /* The required account type without which this application will not function */
          //  账户的类型
        public String mRequiredAccountType;

        /**
         * Digest suitable for comparing whether this package's manifest is the
         * same as another.
         */
        // 对应的AndroidManifest项目摘要清单
        public ManifestDigest manifestDigest;

        //AndroidManifest中 对应<overlay> 标签
        public String mOverlayTarget;
     
        //overlay对应的优先级
        public int mOverlayPriority;

         //  是否是受信任的Overlay
        public boolean mTrustedOverlay;

        /**
         * Data used to feed the KeySetManagerService
         */
         // 下面是用来给KeySetManagerService的数据
        public ArraySet<PublicKey> mSigningKeys;  // 签名
        public ArraySet<String> mUpgradeKeySets;  //升级
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;  //公钥

        /**
         * The install time abi override for this package, if any.
         *
         * TODO: This seems like a horrible place to put the abiOverride because
         * this isn't something the packageParser parsers. However, this fits in with
         * the rest of the PackageManager where package scanning randomly pushes
         * and prods fields out of {@code this.applicationInfo}.
         */
        // 如果有abi的话，abi覆盖
        public String cpuAbiOverride;

        public Package(String packageName) {
            this.packageName = packageName;
            applicationInfo.packageName = packageName;
            applicationInfo.uid = -1;
        }
         ...
    }
```

先来翻译一下注释：

这个类表示从磁盘上的APK文件解析出来的完整包。一个包由一个"基础"APK和多个"拆分"APK构成。

这个类其实就是通过解析APK而对应的一个"包"的类，这个包代表一个磁盘上的APK安装包。

### (九) 静态内部类Component类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5044行

```java
    public static class IntentInfo extends IntentFilter {
        public boolean hasDefault; //是否有默认
        public int labelRes;  // 标签的资源id
        public CharSequence nonLocalizedLabel; // 本地化的标签
        public int icon; // icon的资源id
        public int logo; // logo的资源id
        public int banner; // banner的资源id
        public int preferred; // preferred的资源id
    }
```

IntentInfo类继承自IntentFilter，就是对IntentFilter进行了封装。

### (十) 静态内部类Component类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4542行

```php
    public static class Component<II extends IntentInfo> {
        public final Package owner;  //包含该组件的包
        public final ArrayList<II> intents; // 该组件所包含的IntentFilter
        public final String className; // 组件的类名
        public Bundle metaData;   // 组件的元数据

        ComponentName componentName;  //组件名
        String componentShortName; //组件简称

        public Component(Package _owner) {
            owner = _owner;
            intents = null;
            className = null;
        }

        public Component(final ParsePackageItemArgs args, final PackageItemInfo outInfo) {
            owner = args.owner;
            intents = new ArrayList<II>(0);
            if (parsePackageItemInfo(args.owner, outInfo, args.outError, args.tag, args.sa,
                    true /*nameRequired*/, args.nameRes, args.labelRes, args.iconRes,
                    args.roundIconRes, args.logoRes, args.bannerRes)) {
                className = outInfo.name;
            } else {
                className = null;
            }
        }

        public Component(final ParseComponentArgs args, final ComponentInfo outInfo) {
            this(args, (PackageItemInfo)outInfo);
            if (args.outError[0] != null) {
                return;
            }

            if (args.processRes != 0) {
                CharSequence pname;
                if (owner.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.FROYO) {
                    pname = args.sa.getNonConfigurationString(args.processRes,
                            Configuration.NATIVE_CONFIG_VERSION);
                } else {
                    // Some older apps have been seen to use a resource reference
                    // here that on older builds was ignored (with a warning).  We
                    // need to continue to do this for them so they don't break.
                    pname = args.sa.getNonResourceString(args.processRes);
                }
                outInfo.processName = buildProcessName(owner.applicationInfo.packageName,
                        owner.applicationInfo.processName, pname,
                        args.flags, args.sepProcesses, args.outError);
            }

            if (args.descriptionRes != 0) {
                outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, 0);
            }

            outInfo.enabled = args.sa.getBoolean(args.enabledRes, true);
        }

        public Component(Component<II> clone) {
            owner = clone.owner;
            intents = clone.intents;
            className = clone.className;
            componentName = clone.componentName;
            componentShortName = clone.componentShortName;
        }
       ...
    }
```

组件类是个基类。里面包含组件对应的各个属性。

### (十一) 静态内部类Permission类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4663行

```java
    public final static class Permission extends Component<IntentInfo> {
        public final PermissionInfo info;  //权限信息
        public boolean tree;  //是否权限树
        public PermissionGroup group;  //对应的权限组

        public Permission(Package _owner) {
            super(_owner);
            info = new PermissionInfo();
        }

        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "Permission{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + info.name + "}";
        }
    }
```

该类继承自Component，对应AndroidManifest里面的\<Permission>标签，含义为标签

### (十二) 静态内部类PermissionGroup类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4690行

```java
    public final static class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;  //权限组对象

        public PermissionGroup(Package _owner) {
            super(_owner);
            info = new PermissionGroupInfo();
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + info.name + "}";
        }
    }
```

这个PermissionGroup继承自Component，意为权限组，对应AndroidManifest里面的\<PermissionGroup>。

### (十三) 静态内部类Activity类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4860行

```java
    public final static class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(final ParseComponentArgs args, final ActivityInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个Activity继承自Component，对应AndroidManifest里面的\<Activity>，其实就是ActivityInfo的封装类。

### (十四) 静态内部类Service类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4914行

```java
   public final static class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(final ParseComponentArgs args, final ServiceInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个Service继承自Component，对应AndroidManifest里面的\<Service>，其实就是ServiceInfo的封装类。

#### (十五) 静态内部类Provider类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 4955行

```java
    public final static class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(final ParseComponentArgs args, final ProviderInfo _info) {
            super(args, _info);
            info = _info;
            info.applicationInfo = args.owner.applicationInfo;
            syncable = false;
        }

        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个Provider继承自Component，对应AndroidManifest里面的\<Provider>，其实就是ProviderInfo的封装类。

### (十六) 静态内部类Instrumentation类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5009行

```java
    public final static class Instrumentation extends Component<IntentInfo> {
        public final InstrumentationInfo info;

        public Instrumentation(final ParsePackageItemArgs args, final InstrumentationInfo _info) {
            super(args, _info);
            info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个Instrumentation继承自Component，对应AndroidManifest里面的\<Instrumentation>，其实就是InstrumentationInfo的封装类。

### (十七) 静态内部类ActivityIntentInfo类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5054行

```java
    public final static class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个ActivityIntentInfo继承自IntentInfo，其实就是Activity的封装类。

### (十八) 静态内部类ServiceIntentInfo类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5054行

```java
    public final static class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo(Service _service) {
            service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个ServiceIntentInfo继承自IntentInfo，其实就是Service的封装类。

### (十九) 静态内部类ProviderIntentInfo类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5090行

```java
    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo(Provider provider) {
            this.provider = provider;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }
```

这个ProviderIntentInfo继承自IntentInfo，其实就是provider的封装类。

### (二十) 静态内部类PackageParserException类

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 5142行

```java
    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            this.error = error;
        }

        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error;
        }
    }
```

这个PackageParserException继承自Exception，其实就是Exception的封装类。

至此整个PackageParser的内部类全部简介完毕，下面我们来看下解析的方法

## 四、PackageParse#parsePackage(File, int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 752行

```dart
    /**
     * Parse the package at the given location. Automatically detects if the
     * package is a monolithic style (single APK file) or cluster style
     * (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * @see #parsePackageLite(File, int)
     */
    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackage(packageFile, flags);
        } else {
            return parseMonolithicPackage(packageFile, flags);
        }
    }
```

这个方法有注释，先来看下注释：

解析指定位置的安装包。它自动会检测安装包的模式的是单一APK或者集群APK模式。
 这样就可以对"集群APK"的安装包进行理性的检查，比如会检查"base APK"和"拆分APK"是否具有相同的包名和版本号。
 请注意，这里面是不执行签名验证的，所以必须要单独执行collectCertificates(Package，int)这个方法。

这个方法很简单，主要是判断是否是目录，如果是目录则调用parseMonolithicPackage(File,int)方法，如果不是目录，是文件则调用parseClusterPackage(File,int)

## 五、PackageParse#parseMonolithicPackage(File, int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 827行

```dart
    /**
     * Parse the given APK file, treating it as as a single monolithic package.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * @deprecated external callers should move to
     *             {@link #parsePackage(File, int)}. Eventually this method will
     *             be marked private.
     */
    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        if (mOnlyCoreApps) {
            final PackageLite lite = parseMonolithicPackageLite(apkFile, flags);
            if (!lite.coreApp) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_MANIFEST_MALFORMED,
                        "Not a coreApp: " + apkFile);
            }
        }

        final AssetManager assets = new AssetManager();
        try {
            final Package pkg = parseBaseApk(apkFile, assets, flags);
            pkg.codePath = apkFile.getAbsolutePath();
            return pkg;
        } finally {
            IoUtils.closeQuietly(assets);
        }
    }
```

先简单翻译一下注释：

解析给定的APK文件，将其视为单一APK包
注意：这个不执行签名，必须在collectCertificates方法中独立完成。

我们知道这个方法内部主要是：

* **首先** 判断是不是mOnlyCoreApps，mOnlyCoreApps该标示表明解析只考虑应用清单属性有效的应用，主要为了创建一个最小的启动环境，如果该标示为true则表示为轻量级解析，调用parseMonolithicPackageLite来进行解析

* **其次** 如果mOnlyCoreApps不为空，则new了一个AssetManager对象

* **再次** 调用parseBaseApk()方法解析一个apk并生成一个Package对象

* **最后** 给pkg的codePath赋值

这里面涉及了两个方法分别是**parseMonolithicPackageLite(apkFile, flags);**和**parseBaseApk()**，下面我们就详细看下

## 六、PackageParse#parseMonolithicPackageLite(File, int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 657行

```java
    private static PackageLite parseMonolithicPackageLite(File packageFile, int flags)
            throws PackageParserException {
        final ApkLite baseApk = parseApkLite(packageFile, flags);
        final String packagePath = packageFile.getAbsolutePath();
        return new PackageLite(packagePath, baseApk, null, null, null);
    }
```

这个方法就是调用parseApk()方法来获取一个ApkLite对象，然后用这个ApkLite对象构造一个PackageLite对象。

所以正在这个函数里面要解决三个问题：

- 1、parseApkLite(File,int)函数内部的实现
- 2、ApkLite类
- 3、PackageLite类

parseMonolithicPackageLite内部又调用了parseApkLite函数并且返回一个ApkLite对象，根据返回的ApkLite对象和包的绝对路径构造了一个PackegeLite对象作为返回值。

## 七、PackageParse#parseApkLite(File,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 1155行

```dart
    /**
     * Utility method that retrieves lightweight details about a single APK
     * file, including package name, split name, and install location.
     *
     * @param apkFile path to a single APK
     * @param flags optional parse flags, such as
     *            {@link #PARSE_COLLECT_CERTIFICATES}
     */
    public static ApkLite parseApkLite(File apkFile, int flags)
            throws PackageParserException {
        final String apkPath = apkFile.getAbsolutePath();

        AssetManager assets = null;
        XmlResourceParser parser = null;
        try {
            assets = new AssetManager();
            assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Build.VERSION.RESOURCES_SDK_INT);

            int cookie = assets.addAssetPath(apkPath);
            if (cookie == 0) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                        "Failed to parse " + apkPath);
            }

            final DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();

            final Resources res = new Resources(assets, metrics, null);
            parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);

            final Signature[] signatures;
            if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
                // TODO: factor signature related items out of Package object
                final Package tempPkg = new Package(null);
                collectCertificates(tempPkg, apkFile, 0);
                signatures = tempPkg.mSignatures;
            } else {
                signatures = null;
            }

            final AttributeSet attrs = parser;
            return parseApkLite(apkPath, res, parser, attrs, flags, signatures);

        } catch (XmlPullParserException | IOException | RuntimeException e) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
                    "Failed to parse " + apkPath, e);
        } finally {
            IoUtils.closeQuietly(parser);
            IoUtils.closeQuietly(assets);
        }
    }
```

这个方法内部的流程如下：

* 1、创建AssetManager对象assets

* 2、assets调用addAssetPath(apkPath)

* 3、用assets作为入参来创建Resources对象res

* 4、调用assets的openXmlResourceParser()来获取XmlResourceParser对象parser对象

* 5、设置签名

* 6、调用parseApkLite(String,Resource,XmlPullParser,AttributeSet,int,Signature[])方法来返回一个ApkLite对象

总结一下就是:

函数里面初始化了一个AssertMananger对象，一个DisplayMetrics对象，一个Resources对象，并调用collectCertificates函数获取了应用的签名信息，这些对象都是后续解析中需要用的，因此将这些函数传递给解析函数，这些对象都是后续解析中需要用的，因此将这些参数传递给解析函数，解析完成后关闭资源管理器与解析器，这里主要是轻量级解析，只解析了包名，安装位置等少量信息。

下面我们来看看解析过程：

## 八、PackageParse#parseApkLite(File,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 1274行

```dart
    private static ApkLite parseApkLite(String codePath, Resources res, XmlPullParser parser,
            AttributeSet attrs, int flags, Signature[] signatures) throws IOException,
            XmlPullParserException, PackageParserException {
        final Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs, flags);

        int installLocation = PARSE_DEFAULT_INSTALL_LOCATION;
        int versionCode = 0;
        int revisionCode = 0;
        boolean coreApp = false;
        boolean multiArch = false;
        boolean extractNativeLibs = true;

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            final String attr = attrs.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attrs.getAttributeIntValue(i,
                        PARSE_DEFAULT_INSTALL_LOCATION);
            } else if (attr.equals("versionCode")) {
                versionCode = attrs.getAttributeIntValue(i, 0);
            } else if (attr.equals("revisionCode")) {
                revisionCode = attrs.getAttributeIntValue(i, 0);
            } else if (attr.equals("coreApp")) {
                coreApp = attrs.getAttributeBooleanValue(i, false);
            }
        }

        // Only search the tree when the tag is directly below <manifest>
        int type;
        final int searchDepth = parser.getDepth() + 1;

        final List<VerifierInfo> verifiers = new ArrayList<VerifierInfo>();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() >= searchDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            if (parser.getDepth() == searchDepth && "package-verifier".equals(parser.getName())) {
                final VerifierInfo verifier = parseVerifier(res, parser, attrs, flags);
                if (verifier != null) {
                    verifiers.add(verifier);
                }
            }

            if (parser.getDepth() == searchDepth && "application".equals(parser.getName())) {
                for (int i = 0; i < attrs.getAttributeCount(); ++i) {
                    final String attr = attrs.getAttributeName(i);
                    if ("multiArch".equals(attr)) {
                        multiArch = attrs.getAttributeBooleanValue(i, false);
                    }
                    if ("extractNativeLibs".equals(attr)) {
                        extractNativeLibs = attrs.getAttributeBooleanValue(i, true);
                    }
                }
            }
        }

        return new ApkLite(codePath, packageSplit.first, packageSplit.second, versionCode,
                revisionCode, installLocation, verifiers, signatures, coreApp, multiArch,
                extractNativeLibs);
    }
```

* **首先** 这个方法又调用了parsePackageSplitNames()方法来获取Pair<String, String> packageSplit

* **其次** 遍历属性，并获取相应的值，这里面主要是循环解析出installLocation，versionCode，revisionCode，coreApp

* **再次** 继续解析package-verifier节点，该节点主要解析两个属性：
  * multiArch
  * extractNativeLibs

* **最后** 利用解析出来的数据去构造一个ApkLite对象

## 九、PackageParse#parsePackageSplitNames(XmlpullParse，AttributeSet,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 1230行

```dart
    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser,
            AttributeSet attrs, int flags) throws IOException, XmlPullParserException,
            PackageParserException {

        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }

        if (type != XmlPullParser.START_TAG) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_MANIFEST_MALFORMED,
                    "No start tag found");
        }
        if (!parser.getName().equals("manifest")) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_MANIFEST_MALFORMED,
                    "No <manifest> tag");
        }

        final String packageName = attrs.getAttributeValue(null, "package");
        if (!"android".equals(packageName)) {
            final String error = validateName(packageName, true, true);
            if (error != null) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME,
                        "Invalid manifest package: " + error);
            }
        }

        String splitName = attrs.getAttributeValue(null, "split");
        if (splitName != null) {
            if (splitName.length() == 0) {
                splitName = null;
            } else {
                final String error = validateName(splitName, false, false);
                if (error != null) {
                    throw new PackageParserException(INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME,
                            "Invalid manifest split: " + error);
                }
            }
        }

        return Pair.create(packageName.intern(),
                (splitName != null) ? splitName.intern() : splitName);
    }
```

这个方法主要是解析manifest，这里面调用了validateName(String, boolean,boolean)方法，这里简单说下，这个方法主要就是检测是否是数字、字母、下划线和点分隔符，这也是取包名的规则，比如是字母数字下划线加点分隔符，否则都不是合法的应用包名。并且合法的包名至少包含一个点分隔符。

![](https://upload-images.jianshu.io/upload_images/5713484-dd217d40f9cffa26.png)

## 十、PackageParse#parseBaseApk(File,AssetManager,int)方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 864行

```dart
    private Package parseBaseApk(File apkFile, AssetManager assets, int flags)
            throws PackageParserException {
        final String apkPath = apkFile.getAbsolutePath();

        String volumeUuid = null;
        if (apkPath.startsWith(MNT_EXPAND)) {
            final int end = apkPath.indexOf('/', MNT_EXPAND.length());
            volumeUuid = apkPath.substring(MNT_EXPAND.length(), end);
        }

        mParseError = PackageManager.INSTALL_SUCCEEDED;
        mArchiveSourcePath = apkFile.getAbsolutePath();

        if (DEBUG_JAR) Slog.d(TAG, "Scanning base APK: " + apkPath);

        final int cookie = loadApkIntoAssetManager(assets, apkPath, flags);

        Resources res = null;
        XmlResourceParser parser = null;
        try {
            res = new Resources(assets, mMetrics, null);
            assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Build.VERSION.RESOURCES_SDK_INT);
            parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);

            final String[] outError = new String[1];
            final Package pkg = parseBaseApk(res, parser, flags, outError);
            if (pkg == null) {
                throw new PackageParserException(mParseError,
                        apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
            }

            pkg.volumeUuid = volumeUuid;
            pkg.applicationInfo.volumeUuid = volumeUuid;
            pkg.baseCodePath = apkPath;
            pkg.mSignatures = null;

            return pkg;

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

这个方法内部主要就是做了两件事：

- 1、解析volumeUuid
- 2、调用parseBaseApk(res, parser, flags, outError)来获取Package对象pkg并返回

简单的来说：

这个方法和前面几步的轻量级解析一致，主要多了一个步骤解析volumeUuid，如果APK路径的前置为"/mnt/expand/"，则获取从前缀之后的uuid，从而可以根据这个路径获取文件的路径。

下面我们来看下parseBaseApk(res, parser, flags, outError)的具体实现：

## 十一、PackageParse#parseBaseApk(Resources,XmlResourceParser,int,String[])方法解析

代码在[PackageParser.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/content/pm/PackageParser.java) 1354行

```csharp
    /**
     * Parse the manifest of a <em>base APK</em>.
     * <p>
     * When adding new features, carefully consider if they should also be
     * supported by split APKs.
     */
    private Package parseBaseApk(Resources res, XmlResourceParser parser, int flags,
            String[] outError) throws XmlPullParserException, IOException {
        
        final boolean trustedOverlay = (flags & PARSE_TRUSTED_OVERLAY) != 0;

        AttributeSet attrs = parser;

        mParseInstrumentationArgs = null;
        mParseActivityArgs = null;
        mParseServiceArgs = null;
        mParseProviderArgs = null;

        // 因为是解析"base" APK，所以应该出现"拆包"APK的信息，如果出现，则返回
        final String pkgName;
        final String splitName;
        try {
            //调用parsePackageSplitNames获取packageSplit
            Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs, flags);
            pkgName = packageSplit.first;
            splitName = packageSplit.second;
        } catch (PackageParserException e) {
            mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }

        int type;

        if (!TextUtils.isEmpty(splitName)) {
            outError[0] = "Expected base APK, but found split " + splitName;
            mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }

        // 用包名构造一个Package
        final Package pkg = new Package(pkgName);
        boolean foundApp = false;

        // 获取资源数组
        TypedArray sa = res.obtainAttributes(attrs,
                com.android.internal.R.styleable.AndroidManifest);

        // 初始化pkg的属性mVersionCode、baseRevisionCode和mVersionName
        pkg.mVersionCode = pkg.applicationInfo.versionCode = sa.getInteger(
                com.android.internal.R.styleable.AndroidManifest_versionCode, 0);
        pkg.baseRevisionCode = sa.getInteger(
                com.android.internal.R.styleable.AndroidManifest_revisionCode, 0);
        pkg.mVersionName = sa.getNonConfigurationString(
                com.android.internal.R.styleable.AndroidManifest_versionName, 0);
        if (pkg.mVersionName != null) {
            pkg.mVersionName = pkg.mVersionName.intern();
        }

        // 获取sharedUID
        String str = sa.getNonConfigurationString(
                com.android.internal.R.styleable.AndroidManifest_sharedUserId, 0);

         // 如果sharedUID不为空
        if (str != null && str.length() > 0) {
            String nameError = validateName(str, true, false);
            // 如果不是系统包，即framework-res.apk(它的包名为"android")，则报错
            if (nameError != null && !"android".equals(pkgName)) {
                outError[0] = "<manifest> specifies bad sharedUserId name \""
                    + str + "\": " + nameError;
                mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
            pkg.mSharedUserId = str.intern();
            pkg.mSharedUserLabel = sa.getResourceId(
                    com.android.internal.R.styleable.AndroidManifest_sharedUserLabel, 0);
        }

        // 获取 安装路径的设置
        pkg.installLocation = sa.getInteger(
                com.android.internal.R.styleable.AndroidManifest_installLocation,
                PARSE_DEFAULT_INSTALL_LOCATION);
        pkg.applicationInfo.installLocation = pkg.installLocation;

        // 是不是核心app
        pkg.coreApp = attrs.getAttributeBooleanValue(null, "coreApp", false);

        sa.recycle();

        /* Set the global "forward lock" flag */
        if ((flags & PARSE_FORWARD_LOCK) != 0) {
            pkg.applicationInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_FORWARD_LOCK;
        }

        // 是否要安装在SD卡上
        /* Set the global "on SD card" flag */
        if ((flags & PARSE_EXTERNAL_STORAGE) != 0) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_EXTERNAL_STORAGE;
        }

        // Resource boolean are -1, so 1 means we don't know the value.
        int supportsSmallScreens = 1;
        int supportsNormalScreens = 1;
        int supportsLargeScreens = 1;
        int supportsXLargeScreens = 1;
        int resizeable = 1;
        int anyDensity = 1;

        int outerDepth = parser.getDepth();
    
        // 开始解析xml
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }

            String tagName = parser.getName();
            // 解析<application> 标签
            if (tagName.equals("application")) {
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
                if (!parseBaseApplication(pkg, res, parser, attrs, flags, outError)) {
                    return null;
                }
            } else if (tagName.equals("overlay")) {
                  // 解析<overlay> 标签
                pkg.mTrustedOverlay = trustedOverlay;

                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestResourceOverlay);
                pkg.mOverlayTarget = sa.getString(
                        com.android.internal.R.styleable.AndroidManifestResourceOverlay_targetPackage);
                pkg.mOverlayPriority = sa.getInt(
                        com.android.internal.R.styleable.AndroidManifestResourceOverlay_priority,
                        -1);
                sa.recycle();

                if (pkg.mOverlayTarget == null) {
                    outError[0] = "<overlay> does not specify a target package";
                    mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return null;
                }
                if (pkg.mOverlayPriority < 0 || pkg.mOverlayPriority > 9999) {
                    outError[0] = "<overlay> priority must be between 0 and 9999";
                    mParseError =
                        PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return null;
                }
                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("key-sets")) {
                 // 解析<key-sets> 标签
                if (!parseKeySets(pkg, res, parser, attrs, outError)) {
                    return null;
                }
            } else if (tagName.equals("permission-group")) {
                // 解析<permission-group> 标签
                if (parsePermissionGroup(pkg, flags, res, parser, attrs, outError) == null) {
                    return null;
                }
            } else if (tagName.equals("permission")) {
                // 解析<permission> 标签
                if (parsePermission(pkg, res, parser, attrs, outError) == null) {
                    return null;
                }
            } else if (tagName.equals("permission-tree")) {
                // 解析<permission-tree> 标签
                if (parsePermissionTree(pkg, res, parser, attrs, outError) == null) {
                    return null;
                }
            } else if (tagName.equals("uses-permission")) {
                 // 解析<uses-permission> 标签
                if (!parseUsesPermission(pkg, res, parser, attrs)) {
                    return null;
                }
            } else if (tagName.equals("uses-permission-sdk-m")
                    || tagName.equals("uses-permission-sdk-23")) {
                // 解析<uses-permission-sdk-m> 标签 或者 <uses-permission-sdk-23> 标签
                if (!parseUsesPermission(pkg, res, parser, attrs)) {
                    return null;
                }
            } else if (tagName.equals("uses-configuration")) {
                // 解析<uses-configuration>  标签
                ConfigurationInfo cPref = new ConfigurationInfo();
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration);
                cPref.reqTouchScreen = sa.getInt(
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqTouchScreen,
                        Configuration.TOUCHSCREEN_UNDEFINED);
                cPref.reqKeyboardType = sa.getInt(
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqKeyboardType,
                        Configuration.KEYBOARD_UNDEFINED);
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqHardKeyboard,
                        false)) {
                    cPref.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_HARD_KEYBOARD;
                }
                cPref.reqNavigation = sa.getInt(
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqNavigation,
                        Configuration.NAVIGATION_UNDEFINED);
                if (sa.getBoolean(
                        com.android.internal.R.styleable.AndroidManifestUsesConfiguration_reqFiveWayNav,
                        false)) {
                    cPref.reqInputFeatures |= ConfigurationInfo.INPUT_FEATURE_FIVE_WAY_NAV;
                }
                sa.recycle();
                pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("uses-feature")) {
                  // 解析<uses-feature>  标签
                FeatureInfo fi = parseUsesFeature(res, attrs);
                pkg.reqFeatures = ArrayUtils.add(pkg.reqFeatures, fi);

                if (fi.name == null) {
                    ConfigurationInfo cPref = new ConfigurationInfo();
                    cPref.reqGlEsVersion = fi.reqGlEsVersion;
                    pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("feature-group")) {
                 //解析  <feature-group> 标签
                FeatureGroupInfo group = new FeatureGroupInfo();
                ArrayList<FeatureInfo> features = null;
                final int innerDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                        && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                    if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                        continue;
                    }

                    final String innerTagName = parser.getName();
                    if (innerTagName.equals("uses-feature")) {
                        FeatureInfo featureInfo = parseUsesFeature(res, attrs);
                        // FeatureGroups are stricter and mandate that
                        // any <uses-feature> declared are mandatory.
                        featureInfo.flags |= FeatureInfo.FLAG_REQUIRED;
                        features = ArrayUtils.add(features, featureInfo);
                    } else {
                        Slog.w(TAG, "Unknown element under <feature-group>: " + innerTagName +
                                " at " + mArchiveSourcePath + " " +
                                parser.getPositionDescription());
                    }
                    XmlUtils.skipCurrentTag(parser);
                }

                if (features != null) {
                    group.features = new FeatureInfo[features.size()];
                    group.features = features.toArray(group.features);
                }
                pkg.featureGroups = ArrayUtils.add(pkg.featureGroups, group);

            } else if (tagName.equals("uses-sdk")) {
                 // 解析 <uses-sdk> 标签
                if (SDK_VERSION > 0) {
                    sa = res.obtainAttributes(attrs,
                            com.android.internal.R.styleable.AndroidManifestUsesSdk);

                    int minVers = 0;
                    String minCode = null;
                    int targetVers = 0;
                    String targetCode = null;

                    TypedValue val = sa.peekValue(
                            com.android.internal.R.styleable.AndroidManifestUsesSdk_minSdkVersion);
                    if (val != null) {
                        if (val.type == TypedValue.TYPE_STRING && val.string != null) {
                            targetCode = minCode = val.string.toString();
                        } else {
                            // If it's not a string, it's an integer.
                            targetVers = minVers = val.data;
                        }
                    }

                    val = sa.peekValue(
                            com.android.internal.R.styleable.AndroidManifestUsesSdk_targetSdkVersion);
                    if (val != null) {
                        if (val.type == TypedValue.TYPE_STRING && val.string != null) {
                            targetCode = minCode = val.string.toString();
                        } else {
                            // If it's not a string, it's an integer.
                            targetVers = val.data;
                        }
                    }

                    sa.recycle();

                    if (minCode != null) {
                        boolean allowedCodename = false;
                        for (String codename : SDK_CODENAMES) {
                            if (minCode.equals(codename)) {
                                allowedCodename = true;
                                break;
                            }
                        }
                        if (!allowedCodename) {
                            if (SDK_CODENAMES.length > 0) {
                                outError[0] = "Requires development platform " + minCode
                                        + " (current platform is any of "
                                        + Arrays.toString(SDK_CODENAMES) + ")";
                            } else {
                                outError[0] = "Requires development platform " + minCode
                                        + " but this is a release platform.";
                            }
                            mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
                            return null;
                        }
                    } else if (minVers > SDK_VERSION) {
                        outError[0] = "Requires newer sdk version #" + minVers
                                + " (current version is #" + SDK_VERSION + ")";
                        mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
                        return null;
                    }

                    if (targetCode != null) {
                        boolean allowedCodename = false;
                        for (String codename : SDK_CODENAMES) {
                            if (targetCode.equals(codename)) {
                                allowedCodename = true;
                                break;
                            }
                        }
                        if (!allowedCodename) {
                            if (SDK_CODENAMES.length > 0) {
                                outError[0] = "Requires development platform " + targetCode
                                        + " (current platform is any of "
                                        + Arrays.toString(SDK_CODENAMES) + ")";
                            } else {
                                outError[0] = "Requires development platform " + targetCode
                                        + " but this is a release platform.";
                            }
                            mParseError = PackageManager.INSTALL_FAILED_OLDER_SDK;
                            return null;
                        }
                        // If the code matches, it definitely targets this SDK.
                        pkg.applicationInfo.targetSdkVersion
                                = android.os.Build.VERSION_CODES.CUR_DEVELOPMENT;
                    } else {
                        pkg.applicationInfo.targetSdkVersion = targetVers;
                    }
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("supports-screens")) {
                 // 解析 <supports-screens> 标签
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens);

                pkg.applicationInfo.requiresSmallestWidthDp = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_requiresSmallestWidthDp,
                        0);
                pkg.applicationInfo.compatibleWidthLimitDp = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_compatibleWidthLimitDp,
                        0);
                pkg.applicationInfo.largestWidthLimitDp = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_largestWidthLimitDp,
                        0);

                // This is a trick to get a boolean and still able to detect
                // if a value was actually set.
                supportsSmallScreens = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_smallScreens,
                        supportsSmallScreens);
                supportsNormalScreens = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_normalScreens,
                        supportsNormalScreens);
                supportsLargeScreens = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_largeScreens,
                        supportsLargeScreens);
                supportsXLargeScreens = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_xlargeScreens,
                        supportsXLargeScreens);
                resizeable = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_resizeable,
                        resizeable);
                anyDensity = sa.getInteger(
                        com.android.internal.R.styleable.AndroidManifestSupportsScreens_anyDensity,
                        anyDensity);

                sa.recycle();

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("protected-broadcast")) {
                // 解析<protected-broadcast> 标签
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestProtectedBroadcast);

                // Note: don't allow this value to be a reference to a resource
                // that may change.
                String name = sa.getNonResourceString(
                        com.android.internal.R.styleable.AndroidManifestProtectedBroadcast_name);

                sa.recycle();

                if (name != null && (flags&PARSE_IS_SYSTEM) != 0) {
                    if (pkg.protectedBroadcasts == null) {
                        pkg.protectedBroadcasts = new ArrayList<String>();
                    }
                    if (!pkg.protectedBroadcasts.contains(name)) {
                        pkg.protectedBroadcasts.add(name.intern());
                    }
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("instrumentation")) {
                if (parseInstrumentation(pkg, res, parser, attrs, outError) == null) {
                    return null;
                }

            } else if (tagName.equals("original-package")) {
                  // 解析<original-package> 标签  
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestOriginalPackage);

                String orig =sa.getNonConfigurationString(
                        com.android.internal.R.styleable.AndroidManifestOriginalPackage_name, 0);
                if (!pkg.packageName.equals(orig)) {
                    if (pkg.mOriginalPackages == null) {
                        pkg.mOriginalPackages = new ArrayList<String>();
                        pkg.mRealPackage = pkg.packageName;
                    }
                    pkg.mOriginalPackages.add(orig);
                }

                sa.recycle();

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("adopt-permissions")) {
               // 解析<adopt-permissions> 标签  
                sa = res.obtainAttributes(attrs,
                        com.android.internal.R.styleable.AndroidManifestOriginalPackage);

                String name = sa.getNonConfigurationString(
                        com.android.internal.R.styleable.AndroidManifestOriginalPackage_name, 0);

                sa.recycle();

                if (name != null) {
                    if (pkg.mAdoptPermissions == null) {
                        pkg.mAdoptPermissions = new ArrayList<String>();
                    }
                    pkg.mAdoptPermissions.add(name);
                }

                XmlUtils.skipCurrentTag(parser);

            } else if (tagName.equals("uses-gl-texture")) {
                // 解析<uses-gl-texture> 标签  
                // Just skip this tag
                XmlUtils.skipCurrentTag(parser);
                continue;

            } else if (tagName.equals("compatible-screens")) {
                 // 解析<compatible-screens> 标签  
                // Just skip this tag
                XmlUtils.skipCurrentTag(parser);
                continue;
            } else if (tagName.equals("supports-input")) {
                // 解析<supports-input> 标签  
                XmlUtils.skipCurrentTag(parser);
                continue;

            } else if (tagName.equals("eat-comment")) {
                  // 解析<eat-comment> 标签  
                // Just skip this tag
                XmlUtils.skipCurrentTag(parser);
                continue;

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

        if (!foundApp && pkg.instrumentation.size() == 0) {
            outError[0] = "<manifest> does not contain an <application> or <instrumentation>";
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }

        final int NP = PackageParser.NEW_PERMISSIONS.length;
        StringBuilder implicitPerms = null;
        for (int ip=0; ip<NP; ip++) {
            final PackageParser.NewPermissionInfo npi
                    = PackageParser.NEW_PERMISSIONS[ip];
            if (pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                break;
            }
            if (!pkg.requestedPermissions.contains(npi.name)) {
                if (implicitPerms == null) {
                    implicitPerms = new StringBuilder(128);
                    implicitPerms.append(pkg.packageName);
                    implicitPerms.append(": compat added ");
                } else {
                    implicitPerms.append(' ');
                }
                implicitPerms.append(npi.name);
                pkg.requestedPermissions.add(npi.name);
            }
        }
        if (implicitPerms != null) {
            Slog.i(TAG, implicitPerms.toString());
        }

        final int NS = PackageParser.SPLIT_PERMISSIONS.length;
        for (int is=0; is<NS; is++) {
            final PackageParser.SplitPermissionInfo spi
                    = PackageParser.SPLIT_PERMISSIONS[is];
            if (pkg.applicationInfo.targetSdkVersion >= spi.targetSdk
                    || !pkg.requestedPermissions.contains(spi.rootPerm)) {
                continue;
            }
            for (int in=0; in<spi.newPerms.length; in++) {
                final String perm = spi.newPerms[in];
                if (!pkg.requestedPermissions.contains(perm)) {
                    pkg.requestedPermissions.add(perm);
                }
            }
        }

        if (supportsSmallScreens < 0 || (supportsSmallScreens > 0
                && pkg.applicationInfo.targetSdkVersion
                        >= android.os.Build.VERSION_CODES.DONUT)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
        }
        if (supportsNormalScreens != 0) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
        }
        if (supportsLargeScreens < 0 || (supportsLargeScreens > 0
                && pkg.applicationInfo.targetSdkVersion
                        >= android.os.Build.VERSION_CODES.DONUT)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
        }
        if (supportsXLargeScreens < 0 || (supportsXLargeScreens > 0
                && pkg.applicationInfo.targetSdkVersion
                        >= android.os.Build.VERSION_CODES.GINGERBREAD)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_XLARGE_SCREENS;
        }
        if (resizeable < 0 || (resizeable > 0
                && pkg.applicationInfo.targetSdkVersion
                        >= android.os.Build.VERSION_CODES.DONUT)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
        }
        if (anyDensity < 0 || (anyDensity > 0
                && pkg.applicationInfo.targetSdkVersion
                        >= android.os.Build.VERSION_CODES.DONUT)) {
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
        }

        return pkg;
    }
```

开始解析包名了，之后详细解析了AndroidManifest下面的每一个节点，可以看到有application、overlay、key-sets、permission-group，permisstion，permission-tree，uses-permission等等节点。

## 参考文章

1. [APK安装流程详解9——PackageParser解析APK(上)](https://www.jianshu.com/p/69fb6f9a6ac7)

