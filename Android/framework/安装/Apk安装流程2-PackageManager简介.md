# Apk 安装流程2-PackageManager 简介

本片文章主要内容如下：

```
1、PackageManager介绍
2、PackageManager类概述
3、PackageManager与APK安装
4、PackageManager的功能
5、PackageManager常用方法
6、PackageManager中关于"安装"的几个方法
```

## 一、PackageManager 简介

[PackageManager源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)

Android 系统提供了很多服务的管理类，比如ActivityManager、PowrManager，那么和安装 APK 有关就是 PackageManager了，它负责管理应用程序包，通过它就可以获取应用程序信息。

## 二、PackageManager 类概述

```java
/**
 * Class for retrieving various kinds of information related to the application
 * packages that are currently installed on the device.
 *
 * You can find this class through {@link Context#getPackageManager}.
 */
public abstract class PackageManager {
   ...
}
```

PackageManager这个类是检测当前已经安装在当前设备上的应用程序包的信息。你可以调用Context类的getPackageManager()方法来获取PackageManager方法。

## 三、PackageManager 与 APK 安装

PackageManager是一个实际上管理应用程序安装、卸载和升级的API。当我们安装APK文件时，PackageManager会解析APK包文件和显示确认信息。当我们点击OK按钮后，PackageManager会调用一个叫"InstallPackage"的方法，这个方法有4个参数，也就是uri、installFlags、observer、installPackagename。PackageManager会启动一个叫"package"的servcie服务，现在所有模糊的东西会发生在这个service中。

![](https://upload-images.jianshu.io/upload_images/5713484-0a2df5e912885229.png)

## 四、PaackageManager 的功能

1、安装、卸载应用

2、查询permission相关信息

3、查询Application相关信息(application、activity、receiver、service、provider及相应属性等)

4、查询已安装应用

5、增加、删除permission

6、清除用户数据、缓存、代码等

## 五、PackageManager 常用方法

### 1、public abstract PackageInfo getPackageInfo(String packageName, int flags)方法：

通过包名获取该包名对应的应用程序的PackageInfo对象。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2031行。

```java
    /**
     * Retrieve overall information about an application package that is
     * installed on the system.
     * <p>
     * Throws {@link NameNotFoundException} if a package with the given name can
     * not be found on the system.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of the
     *            desired package.
     * @param flags Additional option flags. Use any combination of
     *            {@link #GET_ACTIVITIES}, {@link #GET_GIDS},
     *            {@link #GET_CONFIGURATIONS}, {@link #GET_INSTRUMENTATION},
     *            {@link #GET_PERMISSIONS}, {@link #GET_PROVIDERS},
     *            {@link #GET_RECEIVERS}, {@link #GET_SERVICES},
     *            {@link #GET_SIGNATURES}, {@link #GET_UNINSTALLED_PACKAGES} to
     *            modify the data returned.
     * @return Returns a PackageInfo object containing information about the
     *         package. If flag GET_UNINSTALLED_PACKAGES is set and if the
     *         package is not found in the list of installed applications, the
     *         package information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     * @see #GET_ACTIVITIES
     * @see #GET_GIDS
     * @see #GET_CONFIGURATIONS
     * @see #GET_INSTRUMENTATION
     * @see #GET_PERMISSIONS
     * @see #GET_PROVIDERS
     * @see #GET_RECEIVERS
     * @see #GET_SERVICES
     * @see #GET_SIGNATURES
     * @see #GET_UNINSTALLED_PACKAGES
     */
    public abstract PackageInfo getPackageInfo(String packageName, int flags)
            throws NameNotFoundException;
```

检索出有关系统上安装应用程序包的总体信息。

### 2、public abstract String[] currentToCanonicalPackageNames(String[] names)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2041行

```
    /**
     * Map from the current package names in use on the device to whatever
     * the current canonical name of that package is.
     * @param names Array of current names to be mapped.
     * @return Returns an array of the same size as the original, containing
     * the canonical name for each package.
     */
    public abstract String[] currentToCanonicalPackageNames(String[] names);
```

从设备上使用当前包名映射到该软件包名的当前规范名称。

- 入参params names 表示要映射的当前名称的数组
- 出参return 表示与原始数组大小相同的数组，其中包含每个包的规范名称

如果修改包名会用到，没有修改过包名一般不会用到

### 3、public abstract String[] canonicalToCurrentPackageNames(String[] names)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2049行

```java
    /**
     * Map from a packages canonical name to the current name in use on the device.
     * @param names Array of new names to be mapped.
     * @return Returns an array of the same size as the original, containing
     * the current name for each package.
     */
    public abstract String[] canonicalToCurrentPackageNames(String[] names);
```

将软件包规范名称映射到设备上正在使用的当前名称。

- 入参params names 表示要映射的新名称数组
- 出参return 表示返回与原始数组大小相同的数组，其中包含每个包的当前名称。

其中canonicalToCurrentPackageNames()和currentToCanonicalPackageNames()方法是相反的两个方法

### 4、public abstract Intent getLaunchIntentForPackage(String packageName)方法：

获取一个应用程序的 Launch 的 Intent。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 2066行

```java
    /**
     * Returns a "good" intent to launch a front-door activity in a package.
     * This is used, for example, to implement an "open" button when browsing
     * through packages.  The current implementation looks first for a main
     * activity in the category {@link Intent#CATEGORY_INFO}, and next for a
     * main activity in the category {@link Intent#CATEGORY_LAUNCHER}. Returns
     * <code>null</code> if neither are found.
     *
     * @param packageName The name of the package to inspect.
     *
     * @return A fully-qualified {@link Intent} that can be used to launch the
     * main activity in the package. Returns <code>null</code> if the package
     * does not contain such an activity, or if <em>packageName</em> is not
     * recognized.
     */
    public abstract Intent getLaunchIntentForPackage(String packageName);
```

返回一个"包"中的"入口"Activity的Intent，例如，这是类似于在浏览包的"打开"按钮。这个当前的安装启动第一步在category(CATEGORY_INFO)中寻找main Activity，然后在category(CATEGORY_LAUNCHER)寻找main Activity。如果找不到就返回null。

入参是包名

### 5、public abstract Intent getLeanbackLaunchIntentForPackage(String packageName)方法：

获取一个TV应用的Leanback的Intent。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2083行

```java
    /**
     * Return a "good" intent to launch a front-door Leanback activity in a
     * package, for use for example to implement an "open" button when browsing
     * through packages. The current implementation will look for a main
     * activity in the category {@link      * return null if no main leanback activities are found.
     * <p>
     * Throws {@link NameNotFoundException} if a package with the given name
     * cannot be found on the system.
     *
     * @param packageName The name of the package to inspect.
     * @return Returns either a fully-qualified Intent that can be used to launch
     *         the main Leanback activity in the package, or null if the package
     *         does not contain such an activity.
     */
    public abstract Intent getLeanbackLaunchIntentForPackage(String packageName);
```

Leanback activity一般在TV上使用的比较多，上面这个方法返回的Intent的一般在AndroidManifest如下：

```java
  <activity
    android:name="com.example.android.TvActivity"
    android:label="@string/app_name"
    android:theme="@style/Theme.Leanback">
    <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
    </intent-filter>
  </activity>
```

其实就是返回的是com.example.android.TvActivity的打开Intent。

返回一个"合适的"Intent，这个Intent是打开LeanbackActivity的入口Intent。例如，类似于在浏览包的"打开"按钮。这个将找匹配CATEGORY_LEANBACK_LAUNCHER的Activity。如果没有找到则返回null。

### 6、public abstract int[] getPackageGids(String packageName)方法

获取相应包的Group ids。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2097行

```java
    /**
     * Return an array of all of the POSIX secondary group IDs that have been
     * assigned to the given package.
     * <p>
     * Note that the same package may have different GIDs under different
     * {@link UserHandle} on the same device.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of the
     *            desired package.
     * @return Returns an int array of the assigned GIDs, or null if there are
     *         none.
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     */
    public abstract int[] getPackageGids(String packageName)
            throws NameNotFoundException;
```

* 返回已分配给包的所有的POSIX辅助组ID的数组

* 请注意，相同的包可能会有不同的GID， 因为可能存在在同一个设备开启了不同的"用户模式“下

* 入参params packageName 是全包名

* 出参 表示 返回应用程序对应的GID的int 数组，如果没有应用程序，则返回null。

### public abstract int[] getPackageUid(String packageName)方法：

获取相应包的UID
代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2113行

```java
    /**
     * Return the UID associated with the given package name.
     * <p>
     * Note that the same package will have different UIDs under different
     * {@link UserHandle} on the same device.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of the
     *            desired package.
     * @return Returns an integer UID who owns the given package name.
     * @throws NameNotFoundException if a package with the given name can not be
     *             found on the system.
     */
    public abstract int getPackageUid(String packageName, @PackageInfoFlags int flags)
            throws NameNotFoundException;
```

* 返回与给定包名的对应的UID

* 请注意，相同的包可能会有不同的UID， 因为可能存在在同一个设备开启了不同的"用户模式“下

* 入参params packageName 是全包名

* 出参 表示 返回给定包名的int 型的UID

### 8、 public abstract PermissionInfo getPermissionInfo(String name, int flags)方法：

根据包名和指定的flags获取指定的授权信息。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2130行

```dart
    /**
     * Retrieve all of the information we know about a particular permission.
     *
     * @param name The fully qualified name (i.e. com.google.permission.LOGIN)
     *         of the permission you are interested in.
     * @param flags Additional option flags.  Use {@link #GET_META_DATA} to
     *         retrieve any meta-data associated with the permission.
     *
     * @return Returns a {@link PermissionInfo} containing information about the
     *         permission.
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     *
     * @see #GET_META_DATA
     */
    public abstract PermissionInfo getPermissionInfo(String name,  int flags)
throws NameNotFoundException;
```

* 检测出我们想要知道所有关于权限信息

* 入参params name 是权限的全名称，比如：com.google.permission.LOGIN

* 入参params name 附加选项的标志位，用来获取检索出与权限相关联的元数据(通过使用"GET_META_DATA")

* 出参 表示 返回权限信息的对象，里面包含我们关于权限信息的的所有信息。

### 10、 public abstract List<PermissionInfo> queryPermissionsByGroup(String group,int flags)方法：

获取所有的PermissionInfo集合。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2148行。

```java
    /**
     * Query for all of the permissions associated with a particular group.
     *
     * @param group The fully qualified name (i.e. com.google.permission.LOGIN)
     *         of the permission group you are interested in.  Use null to
     *         find all of the permissions not associated with a group.
     * @param flags Additional option flags.  Use {@link #GET_META_DATA} to
     *         retrieve any meta-data associated with the permissions.
     *
     * @return Returns a list of {@link PermissionInfo} containing information
     *             about all of the permissions in the given group.
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     *
     * @see #GET_META_DATA
     */
    public abstract List<PermissionInfo> queryPermissionsByGroup(String group,int flags) throws NameNotFoundException;
```

* 查询与特定组相关的所有权限

* 入参params group 需要查询组的全名称，例如：com.google.permission.LOGIN，如果使用NULL则可以查询与组无关的所有权限

* 入参params name 附加选项的标志位，用来获取检索出与权限相关联的的元数据(通过使用"GET_META_DATA")

* 出参 表示 返回权限信息的对象的集合

### 10、 public abstract List\<PermissionInfo> queryPermissionsByGroup(String group,int flags)方法：

根据指定Group名称获取PermissionGroupInfo对象。
代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2166行

```java
   /**
     * Retrieve all of the information we know about a particular group of
     * permissions.
     *
     * @param name The fully qualified name (i.e. com.google.permission_group.APPS)
     *         of the permission you are interested in.
     * @param flags Additional option flags.  Use {@link #GET_META_DATA} to
     *         retrieve any meta-data associated with the permission group.
     *
     * @return Returns a {@link PermissionGroupInfo} containing information
     *         about the permission.
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     *
     * @see #GET_META_DATA
     */
    public abstract PermissionGroupInfo getPermissionGroupInfo(String name,int flags) throws NameNotFoundException;
```

* 检索出我们知道的关于一组特殊权限的所有信息

* 入参params name 一组权限的全限定名称，例如com.google.permission_group.APPS

* 入参params flags 附加选项的标志位，用来获取检索出与权限相关联的的元数据(通过使用"GET_META_DATA")

* 出参 表示 返回一个包含权限组信息的PermissionGroupInfo对象

### 11、public abstract List<PermissionGroupInfo> getAllPermissionGroups( int flags)方法：

获取所有的PermissGroup集合。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2178行。

```java
    /**
     * Retrieve all of the known permission groups in the system.
     *
     * @param flags Additional option flags.  Use {@link #GET_META_DATA} to
     *         retrieve any meta-data associated with the permission group.
     *
     * @return Returns a list of {@link PermissionGroupInfo} containing
     *         information about all of the known permission groups.
     *
     * @see #GET_META_DATA
     */
    public abstract List<PermissionGroupInfo> getAllPermissionGroups(
            @PermissionGroupInfoFlags int flags);
```

- 检索出系统中所有已知的权限
- 入参params flags 附加选项的标志位，用来获取检索出与权限相关联的的元数据(通过使用"GET_META_DATA")
- 出参 表示 返回有关所有权限的组的信息

### 12、public abstract ApplicationInfo getApplicationInfo(String packageName,int flags) throws NameNotFoundException;方法：

根据包名返回其对应的ApplicationInfo信息。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2207行。

```java
    /**
     * Retrieve all of the information we know about a particular
     * package/application.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of an
     *         application.
     * @param flags Additional option flags. Use any combination of
     *         {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},
     *         {@link #MATCH_SYSTEM_ONLY}, {@link #MATCH_UNINSTALLED_PACKAGES}
     *         to modify the data returned.
     *
     * @return An {@link ApplicationInfo} containing information about the
     *         package. If flag {@code MATCH_UNINSTALLED_PACKAGES} is set and if the
     *         package is not found in the list of installed applications, the
     *         application information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     *
     * @see #GET_META_DATA
     * @see #GET_SHARED_LIBRARY_FILES
     * @see #MATCH_DISABLED_UNTIL_USED_COMPONENTS
     * @see #MATCH_SYSTEM_ONLY
     * @see #MATCH_UNINSTALLED_PACKAGES
     */
    public abstract ApplicationInfo getApplicationInfo(String packageName,
            int flags) throws NameNotFoundException;
```

* 检索出一个应用程序的所有信息(ApplicationInfo)

* 入参params packageName 包全名例如com.google.apps.contacts

* 入参params flags 附加选项的标志位，可以使用下面这四个的任何组合过滤返回值
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)
  * MATCH_DISABLED_UNTIL_USED_COMPONENTS：PackageInfo的标志位，表示包含禁用的组件。如果已处于禁用状态程序将变更为启用。
  * MATCH_SYSTEM_ONLYL：查询标志，仅包含有系统的应用程序组件
  * MATCH_UNINSTALLED_PACKAGES：参数标志位，表示检索出所有有数据的目录的应用程序(主要是卸载的)的信息

* 出参 表示 返回一个ApplicationInfo，里面有关包的所有信息。

### 13、public abstract ApplicationInfo getApplicationInfo(String packageName,int flags) throws NameNotFoundException;方法：

根据组件和要求返回特定的ActivityInfo

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2230行

```java
    /**
     * Retrieve all of the information we know about a particular activity
     * class.
     *
     * @param component The full component name (i.e.
     *            com.google.apps.contacts/com.google.apps.contacts.
     *            ContactsList) of an Activity class.
     * @param flags Additional option flags. Use any combination of
     *            {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},{@GET_INTENT_FILTERS}
     *           
     *            returned.
     * @return An {@link ActivityInfo} containing information about the
     *         activity.
     * @throws NameNotFoundException if a package with the given name cannot be
     *             found on the system.
     */
    public abstract ActivityInfo getActivityInfo(ComponentName component,int flags) throws NameNotFoundException;
```

* 检索出一个特定的Activity类的所有信息

* 入参params component 组件的全名称例如:com.google.apps.contacts/com.google.apps.contacts. ContactsList中的一个Activity类

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)
  * GET_INTENT_FILTERS：包的标志位，返回支持IntentFilter的相关组件。

* 出参 表示 返回一个ActivityInfo，里面包含类的所有信息。

### 14、public abstract ActivityInfo getReceiverInfo(ComponentName component, int flags)方法：

根据组件和要求返回特定的ActivityInfo。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2253行

```java
    /**
     * Retrieve all of the information we know about a particular receiver
     * class.
     *
     * <p>Throws {@link NameNotFoundException} if a receiver with the given
     * class name cannot be found on the system.
     *
     * @param component The full component name (i.e.
     * com.google.apps.calendar/com.google.apps.calendar.CalendarAlarm) of a Receiver
     * class.
     * @param flags Additional option flags.  Use any combination of
     * {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},
     * to modify the data returned.
     *
     * @return {@link ActivityInfo} containing information about the receiver.
     *
     * @see #GET_INTENT_FILTERS
     * @see #GET_META_DATA
     * @see #GET_SHARED_LIBRARY_FILES
     */
    public abstract ActivityInfo getReceiverInfo(ComponentName component,
            int flags) throws NameNotFoundException;
```

* 检索出一个特定的Receiver类的所有信息(这里主要指ActivityInfo)

* 入参params component 组件的全名称例如:com.google.apps.calendar/com.google.apps.calendar.CalendarAlarm中的一个Receiver类

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)

* 出参 表示 返回一个ActivityInfo，里面包含Receiver类的所有信息。

### 15、public abstract ServiceInfo getServiceInfo(ComponentName component, int flags))方法：

根据组件和要求返回特定的ServiceInfo。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2275行：

```java
    /**
     * Retrieve all of the information we know about a particular service
     * class.
     *
     * <p>Throws {@link NameNotFoundException} if a service with the given
     * class name cannot be found on the system.
     *
     * @param component The full component name (i.e.
     * com.google.apps.media/com.google.apps.media.BackgroundPlayback) of a Service
     * class.
     * @param flags Additional option flags.  Use any combination of
     * {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},
     * to modify the data returned.
     *
     * @return ServiceInfo containing information about the service.
     *
     * @see #GET_META_DATA
     * @see #GET_SHARED_LIBRARY_FILES
     */
    public abstract ServiceInfo getServiceInfo(ComponentName component,
            int flags) throws NameNotFoundException;
```

* 检索出一个特定的 Service 类的所有信息(这里主要指 ServiceInfo)

* 入参 params component 组件的全名称例如:com.google.apps.media/com.google.apps.media.BackgroundPlayback 中的一个 Service 类

* 入参 params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)

* 出参 表示 返回一个ServiceInfo，里面包含Service类的所有信息。

### 16、public abstract ProviderInfo getProviderInfo(ComponentName component, int flags) 方法：

根据组件和要求返回特定的ProviderInfo。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2297行：

```dart
    /**
     * Retrieve all of the information we know about a particular content
     * provider class.
     *
     * <p>Throws {@link NameNotFoundException} if a provider with the given
     * class name cannot be found on the system.
     *
     * @param component The full component name (i.e.
     * com.google.providers.media/com.google.providers.media.MediaProvider) of a
     * ContentProvider class.
     * @param flags Additional option flags.  Use any combination of
     * {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},
     * to modify the data returned.
     *
     * @return ProviderInfo containing information about the service.
     *
     * @see #GET_META_DATA
     * @see #GET_SHARED_LIBRARY_FILES
     */
    public abstract ProviderInfo getProviderInfo(ComponentName component,
            int flags) throws NameNotFoundException;
```

* 检索出一个特定的content provider类的所有信息(这里主要指ProviderInfo)

* 入参params component 组件的全名称例如:com.google.providers.media/com.google.providers.media.MediaProvider中的一个ContentProvider类

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)

* 出参 表示 返回一个ProviderInfo。

### 17、public abstract List\<PackageInfo> getInstalledPackages(int flags) 方法：

获取设备上安装的所有软件包。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2334行：

```dart
    /**
     * Return a List of all packages that are installed
     * on the device.
     *
     * @param flags Additional option flags. Use any combination of
     * {@link #GET_ACTIVITIES},
     * {@link #GET_GIDS},
     * {@link #GET_CONFIGURATIONS},
     * {@link #GET_INSTRUMENTATION},
     * {@link #GET_PERMISSIONS},
     * {@link #GET_PROVIDERS},
     * {@link #GET_RECEIVERS},
     * {@link #GET_SERVICES},
     * {@link #GET_SIGNATURES},
     * {@link #GET_UNINSTALLED_PACKAGES} to modify the data returned.
     *
     * @return A List of PackageInfo objects, one for each package that is
     *         installed on the device.  In the unlikely case of there being no
     *         installed packages, an empty list is returned.
     *         If flag GET_UNINSTALLED_PACKAGES is set, a list of all
     *         applications including those deleted with {@code DONT_DELETE_DATA}
     *         (partially installed apps with data directory) will be returned.
     *
     * @see #GET_ACTIVITIES
     * @see #GET_GIDS
     * @see #GET_CONFIGURATIONS
     * @see #GET_INSTRUMENTATION
     * @see #GET_PERMISSIONS
     * @see #GET_PROVIDERS
     * @see #GET_RECEIVERS
     * @see #GET_SERVICES
     * @see #GET_SIGNATURES
     * @see #GET_UNINSTALLED_PACKAGES
     */
    public abstract List<PackageInfo> getInstalledPackages(int flags);
```

* 返回设备上所有已经安装的应用程序集合

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_ACTIVITIES ：(packageInfo的标志)表示 返回包(packageInfo)中包含的所有Activity信息
  * GET_GIDS ：(packageInfo的标志)表示 返回关联的GID(groupId)
  * GET_CONFIGURATIONS ：(packageInfo的标志)表示 配置选项信息
  * GET_INSTRUMENTATION ：(PackageInfo的标志)表示 是否使用了instrumentation
  * GET_PERMISSIONS ：(PackageInfo的标志)表示 是否使用了permissions
  * GET_PROVIDERS ：(PackageInfo的标志)表示 是否使用了providers
  * GET_RECEIVERS ：(PackageInfo的标志)表示 是否使用了recevier
  * GET_SERVICES ：(PackageInfo的标志)表示 是否使用了service
  * GET_SIGNATURES ：(PackageInf的标志) 表示是否使用包的签名信息
  * GET_UNINSTALLED_PACKAGES：参数标志位，表示检索出所有有数据的目录的应用程序(主要是卸载的)的信息

* 出参 表示 PackageInfo的List集合

### 18、public abstract List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags)方法：

获取具有特定权限的PackageInfo。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2366行：

```dart
    /**
     * Return a List of all installed packages that are currently
     * holding any of the given permissions.
     *
     * @param flags Additional option flags. Use any combination of
     * {@link #GET_ACTIVITIES},
     * {@link #GET_GIDS},
     * {@link #GET_CONFIGURATIONS},
     * {@link #GET_INSTRUMENTATION},
     * {@link #GET_PERMISSIONS},
     * {@link #GET_PROVIDERS},
     * {@link #GET_RECEIVERS},
     * {@link #GET_SERVICES},
     * {@link #GET_SIGNATURES},
     * {@link #GET_UNINSTALLED_PACKAGES} to modify the data returned.
     *
     * @return Returns a List of PackageInfo objects, one for each installed
     * application that is holding any of the permissions that were provided.
     *
     * @see #GET_ACTIVITIES
     * @see #GET_GIDS
     * @see #GET_CONFIGURATIONS
     * @see #GET_INSTRUMENTATION
     * @see #GET_PERMISSIONS
     * @see #GET_PROVIDERS
     * @see #GET_RECEIVERS
     * @see #GET_SERVICES
     * @see #GET_SIGNATURES
     * @see #GET_UNINSTALLED_PACKAGES
     */
    public abstract List<PackageInfo> getPackagesHoldingPermissions(
            String[] permissions, int flags);
```

* 返回当前设备上所有已安装应用程序中的具有一些特殊权限的安装包集合

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_ACTIVITIES ：(packageInfo的标志)表示 返回包(packageInfo)中包含的所有Activity信息
  * GET_GIDS ：(packageInfo的标志)表示 返回关联的GID(groupId)
  * GET_CONFIGURATIONS ：(packageInfo的标志)表示 配置选项信息
  * GET_INSTRUMENTATION ：(PackageInfo的标志)表示 是否使用了instrumentation
  * GET_PERMISSIONS ：(PackageInfo的标志)表示 是否使用了permissions
  * GET_PROVIDERS ：(PackageInfo的标志)表示 是否使用了providers
  * GET_RECEIVERS ：(PackageInfo的标志)表示 是否使用了recevier
  * GET_SERVICES ：(PackageInfo的标志)表示 是否使用了service
  * GET_SIGNATURES ：(PackageInf的标志) 表示是否使用包的签名信息
  * GET_UNINSTALLED_PACKAGES：参数标志位，表示检索出所有有数据的目录的应用程序(主要是卸载的)的信息

* 出参 表示 PackageInfo的List集合

### 

#### 19、public abstract List\<PackageInfo> getInstalledPackages(int flags, int userId)方法：

获取具有特定用户的PackageInfo。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2406行：

```dart
    /**
     * Return a List of all packages that are installed on the device, for a specific user.
     * Requesting a list of installed packages for another user
     * will require the permission INTERACT_ACROSS_USERS_FULL.
     * @param flags Additional option flags. Use any combination of
     * {@link #GET_ACTIVITIES},
     * {@link #GET_GIDS},
     * {@link #GET_CONFIGURATIONS},
     * {@link #GET_INSTRUMENTATION},
     * {@link #GET_PERMISSIONS},
     * {@link #GET_PROVIDERS},
     * {@link #GET_RECEIVERS},
     * {@link #GET_SERVICES},
     * {@link #GET_SIGNATURES},
     * {@link #GET_UNINSTALLED_PACKAGES} to modify the data returned.
     * @param userId The user for whom the installed packages are to be listed
     *
     * @return A List of PackageInfo objects, one for each package that is
     *         installed on the device.  In the unlikely case of there being no
     *         installed packages, an empty list is returned.
     *         If flag GET_UNINSTALLED_PACKAGES is set, a list of all
     *         applications including those deleted with {@code DONT_DELETE_DATA}
     *         (partially installed apps with data directory) will be returned.
     *
     * @see #GET_ACTIVITIES
     * @see #GET_GIDS
     * @see #GET_CONFIGURATIONS
     * @see #GET_INSTRUMENTATION
     * @see #GET_PERMISSIONS
     * @see #GET_PROVIDERS
     * @see #GET_RECEIVERS
     * @see #GET_SERVICES
     * @see #GET_SIGNATURES
     * @see #GET_UNINSTALLED_PACKAGES
     *
     * @hide
     */
    public abstract List<PackageInfo> getInstalledPackages(int flags, int userId);
```

* 返回当前设备上某个用户的所有安装软件的安装包信息，这里要求一个INTERACT_ACROSS_USERS_FULL权限

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_ACTIVITIES ：(packageInfo的标志)表示 返回包(packageInfo)中包含的所有Activity信息
  * GET_GIDS ：(packageInfo的标志)表示 返回关联的GID(groupId)
  * GET_CONFIGURATIONS ：(packageInfo的标志)表示 配置选项信息
  * GET_INSTRUMENTATION ：(PackageInfo的标志)表示 是否使用了instrumentation
  * GET_PERMISSIONS ：(PackageInfo的标志)表示 是否使用了permissions
  * GET_PROVIDERS ：(PackageInfo的标志)表示 是否使用了providers
  * GET_RECEIVERS ：(PackageInfo的标志)表示 是否使用了recevier
  * GET_SERVICES ：(PackageInfo的标志)表示 是否使用了service
  * GET_SIGNATURES ：(PackageInf的标志) 表示是否使用包的签名信息
  * GET_UNINSTALLED_PACKAGES：参数标志位，表示检索出所有有数据的目录的应用程序(主要是卸载的)的信息

* 入参params userId 用户的id

* 出参 PackageInfo对象的List集合，返回的每一个包都是安装在这个设备上。如果一个安装包都没有，则返回一个空的List，当然这种情况不太可能发生。如果设置了GET_UNINSTALLED_PACKAGES标志位，则List包含使用DONT_DELETE_DATA标志的已经删除的应用程序。

### 20、public abstract List\<ApplicationInfo> getInstalledApplications(int flags)方法：

获取所有已经安装的应用程序集合。

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java)2746行：

```dart
    /**
     * Return a List of all application packages that are installed on the
     * device. If flag GET_UNINSTALLED_PACKAGES has been set, a list of all
     * applications including those deleted with {@code DONT_DELETE_DATA} (partially
     * installed apps with data directory) will be returned.
     *
     * @param flags Additional option flags. Use any combination of
     * {@link #GET_META_DATA}, {@link #GET_SHARED_LIBRARY_FILES},
     * {@link #GET_UNINSTALLED_PACKAGES} to modify the data returned.
     *
     * @return Returns a List of ApplicationInfo objects, one for each application that
     *         is installed on the device.  In the unlikely case of there being
     *         no installed applications, an empty list is returned.
     *         If flag GET_UNINSTALLED_PACKAGES is set, a list of all
     *         applications including those deleted with {@code DONT_DELETE_DATA}
     *         (partially installed apps with data directory) will be returned.
     *
     * @see #GET_META_DATA
     * @see #GET_SHARED_LIBRARY_FILES
     * @see #GET_UNINSTALLED_PACKAGES
     */
    public abstract List<ApplicationInfo> getInstalledApplications(int flags);
```

* 返回设备上已经安装的所有应用程序的集合。如果设置了GET_UNINSTALLED_PACKAGES标志位，则集合中包含已经设置为DONT_DELETE_DATA的已经卸载的应用程序。

* 入参params flags 附加选项的标志位，你可以理解为筛选条件，可以使用的标志位为:
  * GET_META_DATA ：ComponentInfo的标志位，返回与该组件(ComponentInfo)相关联的(metaData)数据(android.os.Bundle)。
  * GET_SHARED_LIBRARY_FILES：ApplicationInfo的标志位，返回与应用程序关联的共享库(ApplicationInfo路径)
  * GET_UNINSTALLED_PACKAGES：参数标志位，表示检索出所有有数据的目录的应用程序(主要是卸载的)的信息

* 出参 ApplicationInfo对象的List集合，返回的每一个ApplicationInfo都是安装在这个设备上。如果一个安装ApplicationInfo都没有，则返回一个空的List，当然这种情况不太可能发生。如果设置了GET_UNINSTALLED_PACKAGES标志位，则List包含使用DONT_DELETE_DATA标志的已经删除的应用程序。

## 六、PackageManager 中关于 “安装”的几个方法

### 1、public abstract void installPackage(Uri, IPackageInstallObserver, int,String)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3586行：

```dart
    /**
     * @hide Install a package. Since this may take a little while, the result
     *       will be posted back to the given observer. An installation will
     *       fail if the calling context lacks the
     *       {@link android.Manifest.permission#INSTALL_PACKAGES} permission, if
     *       the package named in the package file's manifest is already
     *       installed, or if there's no space available on the device.
     * @param packageURI The location of the package file to install. This can
     *            be a 'file:' or a 'content:' URI.
     * @param observer An observer callback to get notified when the package
     *            installation is complete.
     *            {@link IPackageInstallObserver#packageInstalled(String, int)}
     *            will be called when that happens. This parameter must not be
     *            null.
     * @param flags - possible values: {@link #INSTALL_FORWARD_LOCK},
     *            {@link #INSTALL_REPLACE_EXISTING},
     *            {@link #INSTALL_ALLOW_TEST}.
     * @param installerPackageName Optional package name of the application that
     *            is performing the installation. This identifies which market
     *            the package came from.
     * @deprecated Use {@link #installPackage(Uri, PackageInstallObserver, int,
     *             String)} instead. This method will continue to be supported
     *             but the older observer interface will not get additional
     *             failure details.
     */
    // @SystemApi
    public abstract void installPackage(
            Uri packageURI, IPackageInstallObserver observer, int flags,
            String installerPackageName);
```

通过代码我们发现它是一个 系统API(SystemApi)。

* 安装一个安装包的时候，需要经过一定的时间之后才能把安装的结果返回一个观察者。如果在安装并调用Context的时候 在android.Manifest.permission缺少INSTALL_PACKAGES权限将会导致安装失败。如果设备上已经安装了这个同一个包名的应用程序或者在设备已经没有了合适的空间都会导致安装失败。

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

* 注意事项：不推荐使用这个方法(@deprecated)：
   建议使用installPackage(Uri,PackageInstallObserver,int,String)这个方法，在后续版本将支持installPackage(Uri,PackageInstallObserver,int,String)这个方法，因为老版本的observer无法获得额外的故障细节。

### 2、 public abstract void installPackageWithVerification(Uri,IPackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams);方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3624行：

```dart
    /**
     * Similar to
     * {@link #installPackage(Uri, IPackageInstallObserver, int, String)} but
     * with an extra verification file provided.
     *
     * @param packageURI The location of the package file to install. This can
     *            be a 'file:' or a 'content:' URI.
     * @param observer An observer callback to get notified when the package
     *            installation is complete.
     *            {@link IPackageInstallObserver#packageInstalled(String, int)}
     *            will be called when that happens. This parameter must not be
     *            null.
     * @param flags - possible values: {@link #INSTALL_FORWARD_LOCK},
     *            {@link #INSTALL_REPLACE_EXISTING},
     *            {@link #INSTALL_ALLOW_TEST}.
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
     * @deprecated Use {@link #installPackageWithVerification(Uri,
     *             PackageInstallObserver, int, String, Uri, ManifestDigest,
     *             ContainerEncryptionParams)} instead. This method will
     *             continue to be supported but the older observer interface
     *             will not get additional failure details.
     */
    // @SystemApi
    public abstract void installPackageWithVerification(Uri packageURI,
            IPackageInstallObserver observer, int flags, String installerPackageName,
            Uri verificationURI, ManifestDigest manifestDigest,
            ContainerEncryptionParams encryptionParams);
```

通过代码我们发现它是一个 系统API(SystemApi)。

* 和installPackage(Uri,IPackageInstallObserver,int,String)方法类似，就是比它多了一个额外的文件验证功能

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

* 入参 verificationURI ：验证文件的位置，可以是"file:"或者"content:"的URI，该入参可能为null

* 入参 manifestDigest ：一个包含可用于验证所有权的包的摘要的对象，该入参可能为null

* 入参 encryptionParams ：一个描述加密和认证状态的对象，这个入参能为null。

* 注意事项：不推荐使用这个方法(@deprecated)：
   建议使用installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)这个方法，在后续版本将支持installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)这个方法，因为老版本的observer无法获得额外的故障细节。

### 3、public abstract void installPackageWithVerificationAndEncryption(Uri,IPackageInstallObserver, int, String, VerificationParams, ContainerEncryptionParams)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3660行：

```dart
    /**
     * Similar to
     * {@link #installPackage(Uri, IPackageInstallObserver, int, String)} but
     * with an extra verification information provided.
     *
     * @param packageURI The location of the package file to install. This can
     *            be a 'file:' or a 'content:' URI.
     * @param observer An observer callback to get notified when the package
     *            installation is complete.
     *            {@link IPackageInstallObserver#packageInstalled(String, int)}
     *            will be called when that happens. This parameter must not be
     *            null.
     * @param flags - possible values: {@link #INSTALL_FORWARD_LOCK},
     *            {@link #INSTALL_REPLACE_EXISTING},
     *            {@link #INSTALL_ALLOW_TEST}.
     * @param installerPackageName Optional package name of the application that
     *            is performing the installation. This identifies which market
     *            the package came from.
     * @param verificationParams an object that holds signal information to
     *            assist verification. May be {@code null}.
     * @param encryptionParams if the package to be installed is encrypted,
     *            these parameters describing the encryption and authentication
     *            used. May be {@code null}.
     * @hide
     * @deprecated Use {@link #installPackageWithVerificationAndEncryption(Uri,
     *             PackageInstallObserver, int, String, VerificationParams,
     *             ContainerEncryptionParams)} instead. This method will
     *             continue to be supported but the older observer interface
     *             will not get additional failure details.
     */
    @Deprecated
    public abstract void installPackageWithVerificationAndEncryption(Uri packageURI,
            IPackageInstallObserver observer, int flags, String installerPackageName,
            VerificationParams verificationParams,
            ContainerEncryptionParams encryptionParams);
```

通过代码我们发现它是一个 系统API(SystemApi)。

* 和installPackage(Uri,IPackageInstallObserver,int,String)方法类似，就是比它多了一个额外的文件验证功能

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

* 入参 verificationParams ：持有验证信息的对象，可能是null。

* 入参 encryptionParams ：一个描述加密和认证状态的对象，这个入参能为null。

* 注意事项：不推荐使用这个方法(@deprecated)：
   建议使用installPackageWithVerification(Uri,PackageInstallObserver, int, String, VerificationParams,ContainerEncryptionParams)这个方法，在后续版本将支持installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)这个方法，因为老版本的observer无法获得额外的故障细节。

### 4、 public abstract void installPackage(Uri,PackageInstallObserver,int, String)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3688行：

```dart
    /**
     * @hide
     *
     * Install a package. Since this may take a little while, the result will
     * be posted back to the given observer.  An installation will fail if the calling context
     * lacks the {@link android.Manifest.permission#INSTALL_PACKAGES} permission, if the
     * package named in the package file's manifest is already installed, or if there's no space
     * available on the device.
     *
     * @param packageURI The location of the package file to install.  This can be a 'file:' or a
     * 'content:' URI.
     * @param observer An observer callback to get notified when the package installation is
     * complete. {@link PackageInstallObserver#packageInstalled(String, Bundle, int)} will be
     * called when that happens. This parameter must not be null.
     * @param flags - possible values: {@link #INSTALL_FORWARD_LOCK},
     * {@link #INSTALL_REPLACE_EXISTING}, {@link #INSTALL_ALLOW_TEST}.
     * @param installerPackageName Optional package name of the application that is performing the
     * installation. This identifies which market the package came from.
     */
    public abstract void installPackage(
            Uri packageURI, PackageInstallObserver observer,
            int flags, String installerPackageName);
```

* 安装一个安装包的时候，需要经过一定的时间之后才能把安装的结果返回个观察者。如果在安装并调用Context的时候 在android.Manifest.permission缺少INSTALL_PACKAGES权限将会导致安装失败。如果设备上已经安装了这个同一个包名的应用程序或者在设备已经没有了合适的空间都会导致安装失败。

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

### 5、public abstract void installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3717行：

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

* 和installPackage(Uri,IPackageInstallObserver,int,String)方法类似，就是比它多了一个额外的文件验证功能

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

* 入参 verificationURI ：验证文件的位置，可以是"file:"或者"content:"的URI，该入参可能为null

* 入参 manifestDigest ：一个包含可用于验证所有权的包的摘要的对象，该入参可能为null

* 入参 encryptionParams ：一个描述加密和认证状态的对象，这个入参能为null。

### 6、public abstract void installPackageWithVerificationAndEncryption(Uri,PackageInstallObserver, int, String,VerificationParams, ContainerEncryptionParams)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3745行：

```dart
    /**
     * Similar to
     * {@link #installPackage(Uri, IPackageInstallObserver, int, String)} but
     * with an extra verification information provided.
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
     * @param verificationParams an object that holds signal information to
     *            assist verification. May be {@code null}.
     * @param encryptionParams if the package to be installed is encrypted,
     *            these parameters describing the encryption and authentication
     *            used. May be {@code null}.
     *
     * @hide
     */
    public abstract void installPackageWithVerificationAndEncryption(Uri packageURI,
            PackageInstallObserver observer, int flags, String installerPackageName,
            VerificationParams verificationParams, ContainerEncryptionParams encryptionParams);
```

* 和installPackage(Uri,IPackageInstallObserver,int,String)方法类似，就是比它多了一个额外的文件验证功能

* 入参 packageURI ：表示安装的路径，可以是"file:"或者"content:"的URI

* 入参 observer ：一个回调的观察者，有了这个观察者，就可以在软件包安装完成后得到安装结果的通知。如果安装完成会调用这个观察者IPackageInstallObserver的packageInstalled(String，int)方法。observer这个入参不能为空。

* 入参 flags ：标志位参数，可能是以下的几个值
  * INSTALL_FORWARD_LOCK：安装时候的标志位，表示应用程序为向前锁定，即仅应用程序本身可以访问其代码和非资源的assets
  * INSTALL_REPLACE_EXISTING：安装时候的标志位，表示如果在设备存在同一个包名的安装包，则你要替换已安装的软件包。
  * INSTALL_ALLOW_TEST：安装时候的标志位，表示是否允许安装测试包(在AndroidManifest里面设置了android:testOnly)

* 入参 installerPackageName ：正在进行安装的安装包包名

* 入参 verificationParams ：持有验证信息的对象，可能是null。

* 入参 encryptionParams ：一个描述加密和认证状态的对象，这个入参能为null。

### 7、public abstract int installExistingPackage(String)方法：

代码在[PackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageManager.java) 3755行：

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

- 如果系统上已经安装相同包名的应用程序，则重复重新安装。

## 参考文章

1. [APK安装流程详解2——PackageManager简介](https://www.jianshu.com/p/c56376916d5e)

