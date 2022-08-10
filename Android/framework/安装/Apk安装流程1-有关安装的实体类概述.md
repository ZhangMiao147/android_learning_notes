# Apk 安装流程-有关安装的实体类概述

本片文章主要内容如下：

```
1、AndroidManifest的另一种理解
2、PackageInfo类简介
3、PackageItemInfo类简介类
4、ApplicationInfo类简介
5、ComponentInfo类简介
6、ActivityInfo类简介
7、ServiceInfo类简介
8、ProviderInfo类简介
9、ResolveInfo类简介
10、PermissionInfo类简介
11、PermissionGroupInfo类简介
12、上述这些类的关系
13、Android中的UID、GID与GIDS的简介
14、@GuardBy、@SystemApi、@hide Android注解简介
```

## 一、AndroidManifest 的另一种理解

AndroidManifest.xml 文件节点的说明图：

![](https://upload-images.jianshu.io/upload_images/5713484-a3cbe6b49a24dc81.png)

下面根据上面的那张图来依次把涉及到的类都详细说明下。

## 二、PackageInfo 类简介

该类包含了从 AndroidManifest.xml 文件中收集的所有信息。

[PackageInfo.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageInfo.java)

PackageInfo 是实现 Parcelable 接口，所以它可以在进程间传递。

### 重要成员变量简介

**public String packageName**：包名

**public String versionName**：版本名

**public String versionCode**：版本号

**public String sharedUserId**：共享用户ID，签名相同的情况下程序之间数据共享

**public long firstInstallTime**：第一次安装时间，忽略之前安装后卸载的情况，单位 ms

**public long lastUpdateTime**：最后更新时间，相同版本号的 APK 覆盖安装，该值也会发生变化，单位 ms

**public String[] requestedPermissions**：请求的权限

**public ApplicationInfo applicationInfo**：Applicationinfo 对象，下面会讲解

**public ActivityInfo[] activities**：注册的 Activity

**public ActivityInfo[] receivers**：注册的Receiver，PS：注意这里是 ActivityInfo[]

**public ServiceInfo[] services**：注册的服务

**public ProviderInfo[] providers**：注册的Providers

### 重要方法简介

**public PackageInfo()**：构造函数

**private PackageInfo(Parcel source)**：构造函数，反序列时用到的，注意这个方法是 private，所以这个方法只是给反序列时用的，所以PackageInfo 对外就提供一个构造函数

**private void propagateApplicationInfo(ApplicationInfo appInfo, ComponentInfo[] components)**：主要是给入参的 components 中的每一项 ComponentInfo 的 applicationInfo 变量指向第一个入参 appInfo。

## 三、PackageItemInfo 类简介

[PackageItemInfo类源码位置](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageItemInfo.java)

PackageItemInfo 类：它是 AndroidManifest.xml 文件中所有节点的基类，代表一个应用包内所有组件和通用信息的基类。该类提供最基本的属性集合，如：label、icon、meta等。一般不会直接用这个类，设计它的目的就是为包内其他基本组件提供统一的基础定义。它没有实现接口 Parcelable，但它提供了传 Parcel 型的构造函数，以及 writeToParcel() 方法给它的子类来实现 PackageItemInfo 内部的成员 Parcel 化。

### 重要成员变量简介

**public int icon**：获取该组件项在 R 文件中 drawable 的资源 id 值，对应的是 "android:icon" 属性，如果不设置为 0。

**public int labelRes**：获取该组件项在 R 文件中 String 型的资源 idint 值，对应的是 "android:label"，如果不设置为 0。

**public String packageName**：获取该组件项的包名，对应的是 "android:packagename" 属性。

**public String name**：获取该组件项的公共名称，对应的是 "android:name"

**public int banner**：获取该组件项在 R 文件中 drawable 的资源 id 值，对应是 "android:banner"，不设置为 0

**public int logo**：获取该组件项在 R 文件中 drawable 的资源 id 值，比应用图标要大，一般用在 ToolBar 上面，对应是 "android: logo"，不设置为 0

**public Bundle metaData**：对应 AndroidManifest 中的 \<meta-data> 标签。只有 \<activity>、\<activity-alias>、\<service>、\<receiver>、\<application>标签中可能包含 \<meta-data> 子标签。

**public int showUserIcon**：默认值是serHandle.USER_NULL、也可能是
 UserHandle.USER_OWNER，只有实例的引用来访问。

### 重要方法简介

**PackageItemInfo()**：构造函数

**public PackageItemInfo(PackageItemInfo orig)**：构造函数，传入一个orig，进行变量拷贝而已

**protected PackageItemInfo(Parcel source)**：反序列化时用到的构造函数，注释他是protected，说只要是PackageItemInfo的子类局均可以调用

**public CharSequence loadLabel(PackageManager pm)**：返回该组件项的标签，优先级为：nonLocalizedLabel>labelRes>name>packageName

**public Drawable loadIcon(PackageManager pm)**：获取当前组件的图标，其实是通过PackageManager的loadItemIcon()来获取的。

**public Drawable loadBanner(PackageManager pm)**：获取当前组件的的banner，内部是通过PackageManager的getDrawable()来获取的banner对应的Drawable，如果banner为0，返回loadDefaultBanner()的结果。

**public Drawable loadLogo(PackageManager pm)**：返回该组件项的大图标，通过PackageManager的getDrawable()方法获取logo对应的Drawable，如果logo为0，返回loadDefaultLogo()的结果。

**public Drawable loadDefaultIcon(PackageManager pm)**：返回该组件项的默认图标，通过PackageManager的getDefaultActivityIcon()方法，返回的是com.android.internal.R.drawable.sym_def_app_icon对应的Drawable。

**protected Drawable loadDefaultBanner(PackageManager pm)**：返回null

**protected Drawable loadDefaultLogo(PackageManager pm)**：返回null

**public XmlResourceParser loadXmlMetaData(PackageManager pm, String name)**：找到 metaData 对应为 name 的资源 id，通过 PackageManager 的 getXml() 方法返回 id 对应的 XmlResourceParser。

**protected ApplicationInfo getApplicationInfo()**：返回 null

**public void writeToParcel(Parcel dest, int parcelableFlags)**：为 PackageItemInfo 的子类 Parcel 化提供基类部分成员的写入。

## 四、ApplicationInfo 类简介

[ApplicationInfo类源码位置](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FApplicationInfo.java)

ApplicationInfo类：它继承自 PackageItemInfo 并实现了 Parcelable 接口，它对应 manifest 里面的 \<application> 节点的信息。

### 特殊成员变量 flags 如下

int flags：代表 Application 的类型，它是进行"位与"操作的选项，大家可以看下面的每一个只占用"1位"：

**FLAG_SYSTEM：**系统应用程序

**FLAG_DEBUGGABLE：**应用程序允许 debug，对应 manifest 里面的 android:debuggable 属性。

**FLAG_HAS_CODE：**应用程序是否含有代码，平时比较少用，如果，对应 manifest 里面的 android:hasCode，为 true 表明有代码，为 false 表明无 代码，如果没有代码则加载组件时系统不会尝试加载任何应用程序的代码。应用程序一般没有它自己的任何代码，除非它仅仅是由组件类的构建而成的。

**FLAG_PERSISTENT：**应用程序是否永久驻留，对应 manifest 文件中的 android:persistent="true"，理论上意思是应用程序所在进程不会被 LMK 杀死。但是这里有个前提，就是应用程序必须是系统应用。也就是说普通的应用程序设置这个属性其实是没有用的。如果你的应用程序的 apk 直接放到 /system/app 目录下。而且必须重启系统才能生效。

**FLAG_FACTORY_TEST：**应用程序支持 Factory Test，关于 Factory Test 这里就不详细展开了，有兴趣的可以阅读这篇文章[Android FactoryTest框架](https://link.jianshu.com?t=http%3A%2F%2Fblog.csdn.net%2Fthl789%2Farticle%2Fdetails%2F8053574)

**FLAG_ALLOW_TASK_REPARENTING：**设置 activity 从一个 task 迁移到另一个 task 的标签，这块后面在 activity 启动流程中会详细讲解，对应的 manifest 文件是 android:allowTaskReparenting。

**FLAG_ALLOW_CLEAR_USER_DATA：**设置用户自动清除数据，对应 manifest 中为android:allowClearUserData，该值设为 true 时，用户可以自己自己清除用户数据，反之则用户不能清除。

**FLAG_UPDATED_SYSTEM_APP：** 表明系统应用程序被用户升级后，也算用户的应用程序

**FLAG_TEST_ONLY：**表示该应用仅仅用于测试，对应 manifest 里面的 android:testOnly，如果设置为 true，则表明仅仅用于测试

**FLAG_SUPPORTS_SMALL_SCREENS：** 设置应用程序的 window 可以缩小到较小屏幕的大小，对应的 manifest 里面的 android:smallScreens，值为 true，则表明可以缩小。

**FLAG_SUPPORTS_NORMAL_SCREENS：** 设置应用程序的 window 可以在正常的屏幕上显示，对应的 manifest 里面的 android:normalScreens，值为 true，则表明可以显示。

**FLAG_SUPPORTS_LARGE_SCREENS：** 设置应用的 window 可以放大到较大屏幕的大小，对应的 manifest 里面的 android:largeScreens，值为 true，则表明可以放大。

**FLAG_RESIZEABLE_FOR_SCREENS：** 设置应用程序自己知道如何去适应不同的屏幕密度，对应 manifest 里面是 android:anyDensity，值为 true，则应用程序自己调整。

**FLAG_VM_SAFE_MODE：** 设置应用程序在安全模式下运行 VM，即不运行 JIT，对应 manifest 里面的 android:vmSafeMode，值为 true 则设置为安全模式

**FLAG_ALLOW_BACKUP：** 设置允许操作系统备份数据，对应的 manifest 里面的 android:allowBackup，设置 true 则允许备份

**FLAG_KILL_AFTER_RESTORE：** **这块我也是不很清楚**，设置在未来的某个事件点并且版本 versionCode 要大于当前版本的 versionCode，则可以处理还原数据。对应的是 manifest 里面的 android:restoreAnyVersion，值为 true 则设置。

**FLAG_EXTERNAL_STORAGE：**表明应用程序安装在 SD 卡上

**FLAG_SUPPORTS_XLARGE_SCREENS：**表明应用程序的 window 可以增加尺寸适用于超大屏幕。在 manifes t里面对应的 android:xlargeScreens

**FLAG_LARGE_HEAP：**表明应用程序为其进程要求申请更大的内存堆。manifest 里面对应的是 android:largeHeap

**FLAG_STOPPED：**表明这个应用程序处于停止状态

**FLAG_SUPPORTS_RTL：** 表明应用程序支持从右到左，所有 Activity 将变更为从右到左。

**FLAG_INSTALLED：**表明该当前应用程序是被当前用户安装的。

**FLAG_IS_DATA_ONLY：**表明当该应用程序仅仅安装其数据，应用程序包本身并不存在设备上。

**FLAG_IS_GAME：**表明当该应用程序是一个程序

**FLAG_FULL_BACKUP_ONLY：**表明定义一个 android.app.backup.BackupAgent，通过这个 BackupAgent 对象来负责进行应用程序的全数据备份。

**FLAG_USES_CLEARTEXT_TRAFFIC：**表明该应用程序的网络请求是明文，对 WebView 无用，如果是在 Android N 上配置网络配置，也无用。对应 manifest 里面的 android:usesCleartextTraffic

**FLAG_EXTRACT_NATIVE_LIBS：**表明从 .apk 中提取 native 库

**FLAG_HARDWARE_ACCELERATED：**表明当该应用程序开启硬件加速渲染

**FLAG_SUSPENDED：**表明当该应用程序当前处于挂起状态

**FLAG_MULTIARCH：**表明当前应用程序的代码需要加载到其他应用程序的进程中。

### 重要成员变量简介

**public String taskAffinity**：和当前应用所有 Activity 的默认 task 有密切关系，可以参考下面 ActivityInfo 的 taskAffinity，可以通过 AndroidManifest 的 "android:taskAffinity" 属性得到。

**public String permission**：访问当前应用的所有组件需要声明的权限，在 AndroidManifest 的 "android:permission" 属性得到

**public String processName**：应用运行的进程名，可以在 AndroidManifest 的 "android:process" 得到，如果没有设置则默认为应用包名。

**public String className**：Application 类的类名，可以在 AndroidManifest 的 "android:class" 属性得到。

**public int descriptionRes**：对 Application 组件的描述，可以在 AndroidManifest 的 "android:description" 属性得到，不设置则为 0

**public int theme**：应用的主题，可以在 AndroidManifest 的 "android:theme" 属性得到，若不设置则为 0。

**public String manageSpaceActivityName**：用于指定一个 Activity 来管理数据，它最终会出现在"设置->应用程序管理"中，默认按钮为 " 清楚数据 "，可以在 AndroidManifest 的属性 "android:manageSpaceActivity" 中设置值，如果设定后，该按钮可点击跳转到该 Activity，让用户选择性清除哪些数据，若不设置则为 null。

**public String backupAgentName**：Android 原生的备份引擎 BackupManagerService 在应用端的实现类，是 backupAgent 的子类。默认不会由系统备份，可以在 AndroidManifest 属性 "android:backupAgent" 得到，如果设置 "android:allowBackup" 为 false，则该属性设置无效。

**public int fullBackupContent = 0**：表明应用是否支持自动备份

**public int uiOptions = 0**：为应用内所有的 Activity 设置默认的 UI 选项，可选值为 "none"、"splitActionBarWhenNarrow"。

**public String sourceDir**：应用 APK 的全路径

**public String publicSourceDir**：sourceDir 公开可访问的部分，被 forward lock 锁定的应用该值可能与 sourceDir 不一样

**public String[] resourceDirs**：如果当前应用有额外资源包时，表示全路径。通常为 null。

**public String seinfo**：来自 Linux 策略中 seiInfo 标签，这个值一般在设置应用进程的 SELinux 安全上下文时有用。

**public String dataDir**：应用数据目录

**public String dataDir**：应用 JNI 本地库路径

**public int uid**：Linux Kernel 的 user ID，目前对每个引用还不是唯一的，存在几个应用共享一个 UID 的情况。

**public int targetSdkVersion**：应用最低目标 SDK 版本号

**public int versionCode**：应用的版本号

**public boolean enabled = true**：表明当前应用所有组件是否可用。

### 重要方法简介

**public ApplicationInfo()**：构造函数

**public ApplicationInfo(ApplicationInfo orig)**：构造函数，传入一个ApplicationInfo，进行拷贝。

**private ApplicationInfo(Parcel source)**：私有的构造函数，序列化专用

**protected ApplicationInfo getApplicationInfo()**：返回当前ApplicationInfo对象

**public boolean isSystemApp()**：判断当前应用是否为系统应用。flags与ApplicationInfo.FLAG_SYSTEM按位与不等于0则返回true。

**public boolean isForwardLocked()**：判断当前应用是否被锁定。flags与ApplicationInfo.PRIVATE_FLAG_FORWARD_LOCK按位与不等于0则返回true。

## 五、ComponentInfo 类简介

[ComponentInfo.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FComponentInfo.java)

ComponentInfo，它代表一个应用内部的组件(如ActivityInfo、ServiceInfo、ProviderInfo)，一般不会直接使用这个类，它被设计出来是为了不同应用的组件共享统一的定义。它继承与 PackageItemInfo，但它不像 ApplicationInfo 一样实现了 Parcelable 接口。它是没有实现 Parcelable 接口，但是它提供了入参是 Parcel 的构造函数，以及 writeToParcel() 方法给它的子类来实现 ComponentInfo 内部这部分的成员的 Parcel 化。

### 重要成员变量简介

* public ApplicationInfo applicationInfo：组件所在的 application/package 信息，从 \<application> 标签得到。

* public String processName：组件所运行的进程名称，String 类型，从 "android:process" 属性得到
* public int descriptionRes：组件的描述， string 型的资源 id，从 "android:description"，如果不设置则为 0。
* public boolean enabled：当前组件是否被实例化，boolean 类型，从 "android:enabled" 属性得到，如果它所在的 Application 中的 enable 为 false，则这处的设置无效。
* public boolean exported：当前组件能否被其他 Application 的组件启动，boolean 类型，可以从 "android:exported" 属性得到。
  * 如果当前组件没有一个 \<intent-filter> 则它默认为 false(没有任何 \<intent-filter> 表明要组件的准确名称来启动)，exported=false 表明当前组件只能被当前应用内组件启动，或者有相同的 UID 的应用。
  * 当然也可以使用 permission 来限制外部应用对组件的访问，如果该组件有 "android:permission" 属性，则访问这必须声明该权限。当该组件无 permission 属性而 \<application> 标签有声明时，则访问者必须有 \<application> 签名的 permisson。

### 重要方法简介

**public ComponentInfo()**：构造函数

**public ComponentInfo(ComponentInfo orig)**：构造函数，传入一个 ComponentInfo，其实就是拷贝

**protected ComponentInfo(Parcel source)**：构造函数，传入一个 source，然后从 source 里面取出相应的值来完成字段的初始化

**public CharSequence loadLabel(PackageManager pm)**：返回该组件的标签，CharSequence 类型，优先级次序为： nonLocalizedLabel > labelRes > applicationInfo.nonLocalizedLabel > applicationInfo.labelRes

**public boolean isEnable()**：该组件是否启动该组件及包含的应用程序，有且只有当 enabled 和 applicationInfo.enable 同时为 true 时，返回 true。

**public final int getIconResource()**：返回该组件的 icon 的资源 id，类型是 int，如果是 0，则返回 applicationInfo 对应的资源 id

**public final int  getLogoResource()**：返回该组件的 logo 对应的资源 id，如果没有，则返回 applicationInfo 对应的资源 id。

**public final int  getBannerResource()**：返回该组件 banner 对应的资源id，如果没有，则返回 applicationInfo 对应的资源 id。

**@hide public Drawable loadDefaultIcon(PackageManager pm)**：返回组件默认的 icon，类型是 Drawable，返回的是 applicationInfo 的 loadIcon()

**@hide protected Drawable loadDefaultBanner(PackageManager pm)**：返回组件默认的 banner，类型是 Drawable，返回的是 applicationInfo 的 loadBanner()

**@hide protected Drawable loadDefaultLogo(PackageManager pm)**：返回组件默认的 logo，类型是 Drawable，返回的是 applicationInfo 的 loadLogo()

**@hide protected ApplicationInfo getApplicationInfo()**：返回该组件的 applicationInfo

**public void writeToParcel(Parcel dest, int parcelableFlags)**：先调用父类 PackageItemInfo 的 writeToParcel() 方法，再完成自己的成员  Parcel 化

## 六、ActivityInfo 类简介

[ActivityInfo.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FActivityInfo.java)

ActivityInfol类
 ApplicationInfo类：它继承自ComponentInfo并实现了Parcelable 接口，它对应manifest里面的\<activity>或者\<receiver>节点的信息。我们可以通过它来设置我们的任何属性，包括 theme、launchMode 等，常用方法继承至 PackageItemInfo 类中的 loadIcon() 和 loadLabel()

### 重要成员变量简介

**public int launchMode**：Activity的启动模式，对应Manifest的"launchMode"属性，可能是以下几种模式：

- public static final int LAUNCH_MULTIPLE = 0：普通模式
- public static final int LAUNCH_SINGLE_TOP = 1：同一个task如果是顶部，复用
- public static final int LAUNCH_SINGLE_TASK = 2：同一个task，无论是不是在顶部都复用
- public static final int LAUNCH_SINGLE_INSTANCE = 3：新开一个task

**public int documentLaunchMode**：总览画面--overview screenActivity的启动模式，关于总览画面可以参考[ ](https://link.jianshu.com?t=http%3A%2F%2Fblog.csdn.net%2Fllp1992%2Farticle%2Fdetails%2F43121195)[Android 5.0 Overview Screen--总览画面](https://link.jianshu.com?t=http%3A%2F%2Fblog.csdn.net%2Fllp1992%2Farticle%2Fdetails%2F43121195)，对应AndroidManifest里面的"documentLaunchMode"属性，如果一个Activity添加了这个属性，则该Activity被启动时永远会创建一个新的task。该属性有4个值，用户在应用中打开的一个document会有不同的效果如下：

- public static final int DOCUMENT_LAUNCH_NONE = 0：
   Activity不会为document创建新的task，App设置为single task模式。它会重新调用用户唤醒的所有activity中的最近的一个。
- public static final int DOCUMENT_LAUNCH_INTO_EXISTING = 1：
   activity 会为该document请求一个已经存在的task
- public static final int DOCUMENT_LAUNCH_ALWAYS = 2：
   即使docutment已经打开了，activity也会为document创建一个新的task。
- public static final int DOCUMENT_LAUNCH_NEVER = 3：
   activity不会为document创建一个新的task。

**public int persistableMode**：activity持久化的模式，对应着AndroidManifest的"android:persistableMode"属性，它有三个模式如下：

- public static final int PERSIST_ROOT_ONLY = 0：
   默认值，仅仅会作用在跟activity活着task中。
- public static final int PERSIST_NEVER = 1：
   不起作用，不用两个持久化页面数据或状态
- public static final int PERSIST_ACROSS_REBOOTS = 2：
   重启设备或持久化页面的数据或者状态，如果在这个界面上的界面也设置这个值，上面的页面也会被持久化，最后系统会将你保存的数据，在重新打开这个页面的时候，会调用onCreate()具有两个参数的方法。你只要在你第二个参数PersistableBundle中取出你保存的数据就可以了

**public String requestedVrComponent;**：跑在Activity上面的VrListenerService的组件名称。

**public int screenOrientation**：表示Activity运行的屏幕的方向。对应AndroidManifest里面的"android:screenOrientation"属性，具体属性值如下：

- public static final int SCREEN_ORIENTATION_UNSPECIFIED：未指定，它是默认值，由Android系统自己选择适当的方向，选择策略是具体设备的配置情况而定，因此不同的设备会有不同的方向选择。
- public static final int SCREEN_ORIENTATION_LANDSCAPE：表示横屏，显示时宽度大于高度
- public static final int SCREEN_ORIENTATION_PORTRAIT：表示竖屏，显示时高度大于宽度
- public static final int SCREEN_ORIENTATION_USER：表示用户当前的首选方向。
- public static final int SCREEN_ORIENTATION_BEHIND：表示用户继续Activity堆栈中当前Activity下面的那个Activity的方向。
- public static final int SCREEN_ORIENTATION_SENSOR：表示由物理感应器决定显示方向，它取决于用户如何持有设备，当设备被旋转时方向会随之变化——在横屏和竖屏之间切换
- public static final int SCREEN_ORIENTATION_NOSENSOR：忽略物理感应器——即显示方向和物理感应器无关，不管用户如何旋转设备，显示方向都不会发生改变。
- public static final int SCREEN_ORIENTATION_SENSOR_LANDSCAPE：表示Activity在横向屏幕上显示，但是可以根据方向传感器的指示来进行改变
- public static final int SCREEN_ORIENTATION_SENSOR_PORTRAIT：表示Activity在纵向屏幕上显示，可以根据方向传感器指示的方向来进行改变。
- public static final int SCREEN_ORIENTATION_REVERSE_LANDSCAPE：表示Activity横屏显示，当时与正常的横屏方向相反(比如原来很平方向是向左的，这时候也是横屏但是方向向右)
- public static final int SCREEN_ORIENTATION_REVERSE_PORTRAIT：表示Activity竖屏显示，但是与正常的纵向方向的屏幕方向相反
- public static final int SCREEN_ORIENTATION_FULL_SENSOR：表示Activity的方向由方向传感器决定，会根据用户设备的移动情况来旋转

## 七、ServiceInfo 类简介

[ServiceInfo.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FServiceInfo.java)

ServiceInfo类，它继承自ComponentInfo并实现了Parcelable接口，它对应manifest里面的\<service>节点的信息。

### 重要成员变量简介

**public String permission**：这个Service的访问权限

**public int flags**：表示service在AndroidManifest设置的选项

- public static final int FLAG_STOP_WITH_TASK：如果用户删除了预计应程序的Activitiest，系统将自动停止这个Service
- public static final int FLAG_ISOLATED_PROCESS：Service在其独立的进程中运行
- public static final int FLAG_EXTERNAL_SERVICE：这个Service可以被外部包调用
- public static final int FLAG_SINGLE_USER：表示Service就是一个单例。

## 八、ProviderInfo 类简介

[ProviderInfo.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FProviderInfo.java)

ProviderInfo类，它继承自ComponentInfo并实现了Parcelable接口，它对应manifest里面的\<provider>节点的信息。

### 重要成员变量简介

**public String authority**：提供者的名字

**public String readPermission**：只读的provider的访问权限

**public String writePermission**：读写的provider的访问权限

**public boolean grantUriPermissions**：是否授予provider提供特定的Uris访问权限

**public PatternMatcher[] uriPermissionPatterns**：provider的PatternMatcher数组

**public PathPermission[] pathPermissions**：provider的PathPermission数组

**public boolean multiprocess = false**：是否允许多进程多实例

**public int initOrder = 0**：同一个进程运行的的provider的初始顺序，数字越高，优先级越高

**public int flags**：provider的选项

- public static final int FLAG_SINGLE_USER = 0x40000000：设置为provider为单例模式。

## 九、ResolveInfo 类简介

[ResolveInfo.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FResolveInfo.java)

ResolveInfo就是解析 intent过程返回的信息，也是通过解析一个IntentFilter相对应的intent得到的信息。它部分的对应位于AndroidManifest.xml的\<intent>标签收集到的信息，ResolveInfo实现了Parcelable接口。

### 重要成员变量简介

**public ActivityInfo activityInfo**：和Intent相匹配的ActivityInfo(可能是Activity或者Receiver)

**public ServiceInfo serviceInfo**：和Intent相匹配的serviceInfo

**public ProviderInfo providerInfo**：和Intent相匹配的providerInfo

**public IntentFilter filter**：匹配的IntentFilter

**public int priority**：匹配优先级，数字越高，优先级约高，默认是0

**public int preferredOrder**：用户配置的优先级，默认是0，数值越大，优先级越高

**public int match**：系统评估Activity与Intent的匹配程度，是一个数字。

**public int specificIndex = -1**：如果设置了queryIntentActivityOptions，这个specificIndex表示返回列表的下标，0 是列表的第一个。<0则表示它来自通用的Intent查询。

**public boolean isDefault**：是否在过滤器中制定了Intent.CATEGORY_DEFAULT，意味着它被用户可以对数据进行默认的操作

### 重要方法简介

**public ResolveInfo()**：构造函数

**public ResolveInfo(ResolveInfo orig)**：带有ResolveInfo的构造函数，进行数据拷贝

**private ResolveInfo(Parcel source)**：私有的构造函数主要是给反序列化的时候。

**public CharSequence loadLabel(PackageManager pm) **：获取标签，获取优先级顺序如下：nonLocalizedLabel>labelRes(resolvePackageName)>labelRes(ci.packageName)> ci.loadLabel(pm)

**public Drawable loadIcon(PackageManager pm) **：获取Drawable类型的图标。

**final int getIconResourceInternal()**：注意这个方法的作用域是"包内"，返回匹配的图标资源标识符，如果匹配了直接使用，没有匹配使用应用程序图标

**public final int getIconResource()**：返回匹配的图标资源标识符，如果没有匹配就使用应用程序图标。

## 十、PermissionInfo 类简介

[PermissionInfo.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPermissionInfo.java)

PermissionInfo，它代表一个应用的权限描述，它既是权限信息的记录，也是权限的级别保护，在Android系统中，做任何操作都要申请权限，但是如果你的级别不够，有些权限不是你在代码中写了申请就能获取的。在使用PermissionInfo来指定一个权限的基本信息时，需要指定protectedLevel，并指定所属的group信息。PermissionInfo继承自PackageItemInfo，并实现了Parcelable接口。

### 重要成员变量简介

**public int protectionLevel**：保护权限的级别，可以是如下级别

- public static final int PROTECTION_NORMAL：表示只要是申请了就可以使用的级别。
- public static final int PROTECTION_DANGEROUS：表示在安装时需要用户确认才可以使用
- public static final int PROTECTION_SIGNATURE：表示使用者的APP和系统使用同一个证书，即系统权限级别

**public String group**：权限组

**public int flags**：附加权限的标志位，可能是以下几个值：

- FLAG_COSTS_MONEY：付费标志
- FLAG_REMOVED：删除标志
- FLAG_INSTALLED：安装标志

**public int descriptionRes**：资源中的表示权限的描述

### 重要方法简介

**public PermissionInfo()**：构造函数

**public PermissionInfo(PermissionInfo orig)**：带有PermissionInfo的构造函数，进行数据拷贝

**private PermissionInfo(Parcel source)**：私有的构造函数主要是给反序列化的时候。

## 十一、PermissionGroupInfo 简介

[PermissionGroupInfo.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPermissionGroupInfo.java)

PermissionGroupInfo类，它表示一个权限组的概念，Android系统内部定了很多权限组，比如android.permission-group.CONTACTS表示联系人相关权限组。PermissionGroupInfo继承自PackageItemInfo，并实现了Parcelable接口。

### 已知的权限组如下

android.permission-group.CONTACTS：联系人相关权限组

android.permission-group.PHONE：电话相关权限组

android.permission-group.APPSTORE_CLOUD：应用商店云服务相关权限组

android.permission-group.CALENDAR：日历相关权限组

android.permission-group.CAMERA：相机相关权限组

android.permission-group.SENSORS：传感器相关权限组

android.permission-group.LOCATION：位置服务相关权限组

android.permission-group.STORAGE：存储相关权限组

android.permission-group.MICROPHONE：话筒相关权限组

android.permission-group.SMS：短消息相关权限组

### 重要的成员变量简介

**public String nonLocalizedDescription**：如果这个权限组的名字字符串直接定义在AndroidManifest.xml中，那么通过这个值可以得到他的名字(String类型)，如果是空，则是调用的资源使用descriptionRes

**public int descriptionRes**：资源中的表示权限组的描述

**public int flags**：权限组的标志位

**public int priority**：权限组的优先级：

## 十二、类的关系

![](https://upload-images.jianshu.io/upload_images/5713484-695da3e646f268da.png)

![](https://upload-images.jianshu.io/upload_images/5713484-7ec737aa6ab61615.png)



## 参考文章

1. [APK安装流程详解1——有关"安装ing"的实体类概述](https://www.jianshu.com/p/71c1ce538ee8)

