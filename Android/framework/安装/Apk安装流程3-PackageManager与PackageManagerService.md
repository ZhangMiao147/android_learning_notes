# Apk 安装流程3-PackageManager与PackageManagerSrervice

本片文章主要内容如下：

```
1、PackageManager的具体实现类
2、ApplicationPackageManager类
3、IPackageManager类
4、PackageManager、IPackageManager与PackageManagerService
5、PackageManagerService类简介
6、ServiceManager与PackageManagerService关系
7、总结
```

## 一、PackageManager 的具体实现类

PackageManager是一个抽象类，它里面很重要的方法都是抽象的，所以在具体执行的时候，肯定是他的实现子类，那么就来看下他具体实现类，官网推荐获取PackageManager对象的方法是Context的Context#getPackageManager()方法：

### 1、Context#getPackageManager()

代码在[Context.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2FContext.java) 322行

```csharp
    /** Return PackageManager instance to find global package information. */
    public abstract PackageManager getPackageManager();
```

Context是一个抽象类，而他的getPackageManager()也是抽象方法，我们知道Context的具体实现类是ContextImpl，那我们就去ContextImpl里面去看下

### 2、ContextImpl#getPackageManager()

代码在[ContextImpl.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FContextImpl.java) 208行

```java
    @Override
    public PackageManager getPackageManager() {
        // 第一步
        if (mPackageManager != null) {
            return mPackageManager;
        }
        // 第二步
        IPackageManager pm = ActivityThread.getPackageManager();
         // 第三步
        if (pm != null) {
            // Doesn't matter if we make more than one instance.
            return (mPackageManager = new ApplicationPackageManager(this, pm));
        }
        return null;
    }
```

这个方法的内部流程大体上分为三个步骤如下：

**第一步**：判断mPackageManager是否为空，如果为空，则说明是的第一次调用，走第二步，如果不为空，则直接返回mPackageManager

**第二步**：能走到第二步，说明这是第一次调用，则调用ActivityThread的静态方法getPackageManager()获取一个IPackageManager对象

**第三步**：如果获取的IPackageManager对象不为空，则构造一个ApplicationPackageManager对象，而ApplicationPackageManager是PackageManager的子类，把mPackageManager指向ApplicationPackageManager，然后返回ApplicationPackageManager，也就是返回的ApplicationPackageManager

通过上面代码分析我们知道，在我们平时调用Context的getPackageManager()方法返回的是ApplicationPackageManager这个类。

在这个方法里面涉及到两个重要类：

1、ActivityThread.getPackageManager()中的返回值IPackageManager类

2、new ApplicationPackageManager(this, pm)中的ApplicationPackageManager类

## 二、ApplicationPackageManager类

[ApplicationPackageManager.java源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java)

### 1、ApplicationPackageManager类简介

```dart
final class ApplicationPackageManager extends PackageManager {
    ...
}
```

通过源代码知道ApplicationPackageManager继承自PackageManager，而且这ApplicationPackageManager类不是抽象的，所以ApplicationPackageManager必然实现了PackageManager的所有抽象方法，而且ApplicationPackageManager是final的，所以它没有子类。

### 2、ApplicationPackageManager类的构造函数

代码在[ApplicationPackageManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1123行

ApplicationPackageManager就一个构造函数

```java
    ApplicationPackageManager(ContextImpl context,
                              IPackageManager pm) {
        mContext = context;
        mPM = pm;
    }
```

1、这个构造函数不是 public ，所以它的活动区域只有"包"内，而它的包是"android.app"，而且它的构造函数就一个，所以只有系统才能调用。

2、两个入参，一个是ContextImpl，一个IPackageManager对象。

### 3、ApplicationPackageManager类中对PackageManager的具体实现

我们首先来看和安装有关的几个方法：

1、public abstract void installPackage(Uri, IPackageInstallObserver, int,String)方法

2、 public abstract void installPackageWithVerification(Uri,IPackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams)方法

3、public abstract void installPackageWithVerificationAndEncryption(Uri,IPackageInstallObserver, int, String, VerificationParams, ContainerEncryptionParams)方法

4、 public abstract void installPackage(Uri,PackageInstallObserver,int, String)方法

5、public abstract void installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)方法

6、public abstract void installPackageWithVerificationAndEncryption(Uri,PackageInstallObserver, int, String,VerificationParams, ContainerEncryptionParams)方法

7、public abstract int installExistingPackage(String)方法

#### 3.1、public abstract void installPackage(Uri, IPackageInstallObserver, int,String)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1335行

```dart
    @Override
    public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags,String installerPackageName) {
        // 第一步
        final VerificationParams verificationParams = new VerificationParams(null, null,
                null, VerificationParams.NO_UID, null);
        // 第二步
        installCommon(packageURI, new LegacyPackageInstallObserver(observer), flags,
                installerPackageName, verificationParams, null);
    }
```

这个方法内部主要分为两块：

- 第一步，构造VerificationParams对象
- 第二步，调用installCommon(Uri,PackageInstallObserver, int, String, int)方法

看下installCommon(Uri,PackageInstallObserver, int, String, int)方法：

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1388行

```dart
    private void installCommon(Uri packageURI,PackageInstallObserver observer, int flags, String installerPackageName, int userId) {
        // 第一步
        if (!"file".equals(packageURI.getScheme())) {
            throw new UnsupportedOperationException("Only file:// URIs are supported");
        }
        // 第二步
        final String originPath = packageURI.getPath();
        try {
            // 第三步
            mPM.installPackageAsUser(originPath, observer.getBinder(), flags, installerPackageName,
                    userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```

方法内部主要分为三个部分，如下：

第一步，scheme判断，如果非"file"则抛异常，因为只支持file格式的URI

第二步，获取相应的路径

第三步，调用IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法

所以总结下就是：

public abstract void installPackage(Uri, IPackageInstallObserver, int,String)方法其内部本质是调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)

#### 3.2、 public abstract void installPackageWithVerification(Uri,IPackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1344行

```dart
    @Override
    public void installPackageWithVerificationAndEncryption(Uri packageURI,IPackageInstallObserver observer, int flags, String installerPackageName,VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
        installCommon(packageURI, new LegacyPackageInstallObserver(observer), flags,
                installerPackageName, verificationParams, encryptionParams);
    }
```

我们发现这个方法内部其实也是调用的是installCommon(Uri,PackageInstallObserver, int, String, int)方法，通过上面的内容我们知道，所以我们可以说

public abstract void installPackageWithVerification(Uri,IPackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams)方法其内部本质也是**调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法**

#### 3.3、 public abstract void installPackageWithVerificationAndEncryption(Uri,IPackageInstallObserver, int, String, VerificationParams, ContainerEncryptionParams)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1353行

```dart
    @Override
    public void installPackageWithVerificationAndEncryption(Uri packageURI,
            IPackageInstallObserver observer, int flags, String installerPackageName,
            VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
        installCommon(packageURI, new LegacyPackageInstallObserver(observer), flags,
                installerPackageName, verificationParams, encryptionParams);
    }
```

我们发现这个方法内部其实也是调用的是installCommon(Uri,PackageInstallObserver, int, String, int)方法，通过上面的内容我们知道，所以我们可以说

public abstract void installPackageWithVerificationAndEncryption(Uri,IPackageInstallObserver, int, String, VerificationParams, ContainerEncryptionParams)方法其内部本质也是**调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法**

#### 3.4、 public abstract void installPackage(Uri,PackageInstallObserver,int, String)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1362行

```dart
    @Override
    public void installPackage(Uri packageURI, PackageInstallObserver observer,int flags, String installerPackageName) {
        // 第一步
        final VerificationParams verificationParams = new VerificationParams(null, null,
                null, VerificationParams.NO_UID, null);
        // 第二步
        installCommon(packageURI, observer, flags, installerPackageName, verificationParams, null);
    }
```

这个方法内部主要分为两块：

- 第一步，构造VerificationParams对象
- 第二步，调用installCommon(Uri,PackageInstallObserver, int, String, int)方法

所以可以说：

public abstract void installPackage(Uri,PackageInstallObserver,int, String)方法其内部本质也是**调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法**

#### 3.5、public abstract void installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1370行

```dart
    @Override
    public void installPackageWithVerification(Uri packageURI,PackageInstallObserver observer, int flags, String installerPackageName,Uri verificationURI, ManifestDigest manifestDigest,ContainerEncryptionParams encryptionParams) {
        // 第一步
        final VerificationParams verificationParams = new VerificationParams(verificationURI, null,null, VerificationParams.NO_UID, manifestDigest);
        // 第二步
        installCommon(packageURI, observer, flags, installerPackageName, verificationParams,
                encryptionParams);
```

这个方法内部主要分为两块：

- 第一步，构造 VerificationParams 对象
- 第二步，调用installCommon(Uri,PackageInstallObserver, int, String, int)方法

所以可以说：

public abstract void installPackageWithVerification(Uri,PackageInstallObserver, int, String, Uri, ManifestDigest,ContainerEncryptionParams)方法其内部本质也是**调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法**

#### 3.6、public abstract void installPackageWithVerificationAndEncryption(Uri,PackageInstallObserver, int, String,VerificationParams, ContainerEncryptionParams)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 1380行

```dart
    @Override
    public void installPackageWithVerificationAndEncryption(Uri packageURI,PackageInstallObserver observer, int flags, String installerPackageName,VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
        installCommon(packageURI, observer, flags, installerPackageName, verificationParams,
                encryptionParams);
    }
```

发现这个方法内部其实也是调用的是installCommon(Uri,PackageInstallObserver, int, String, int)方法，通过上面的内容知道，所以可以说

public abstract void installPackageWithVerificationAndEncryption(Uri,PackageInstallObserver, int, String,VerificationParams, ContainerEncryptionParams)方法其内部本质也是**调用的IPackageManager的installPackageAsUser(String, IPackageInstallObserver2,int, String, int)方法**

#### 3.7、public abstract int installExistingPackage(String)方法

代码在[ApplicationPackageManager.java)](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FApplicationPackageManager.java) 147行

```java
    @Override
    public int installExistingPackage(String packageName)
            throws NameNotFoundException {
        try {
            // 第一步
            int res = mPM.installExistingPackageAsUser(packageName, UserHandle.myUserId());
            // 第二步
            if (res == INSTALL_FAILED_INVALID_URI) {
                throw new NameNotFoundException("Package " + packageName + " doesn't exist");
            }
            return res;
        } catch (RemoteException e) {
            // 第三步
            // Should never happen!
            throw new NameNotFoundException("Package " + packageName + " doesn't exist");
        }
    }
```

把这个方法内内部代码大体上分为 3 块如下：

* 第一步：首先调用 IPackageMnager 的 installExistingPackageAsUser(String,int) 方法，并将返回值赋值给 res

* 第二步：如果 res 等于 INSTALL_FAILED_INVALID_URI，则表示是无用的 URI，并抛异常

* 第三步：如果走到这一步，则说明，在调用 IPackageMnager 的 installExistingPackageAsUser(String,int) 方法的时候抛 RemoteException 异常，则抛出异常说明。

通过上面的方法，大家有没有发现什么规律，对了就是 ApplicationPackageManager 实现的PackageManager的抽象方法，其内部都是调用其内部变量mPm即IPackageManager类来实现的，大家可自行去看下，并验证下。

### 4、总结

ApplicationPackageManager 中关于PackageManager的具体实现，其实是调用IPackageManager来是实现的。

## 三、IPackageManager类

首先，先来看下上面代码涉及到的ActivityThread.getPackageManager()方法内部是怎么实现的

### (一)、ActivityThread的静态方法getPackageManager()

代码在[ActivityThread.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fapp%2FActivityThread.java) 1669行

```csharp
    public static IPackageManager getPackageManager() {
         //第一步
        if (sPackageManager != null) {
            //Slog.v("PackageManager", "returning cur default = " + sPackageManager);
            return sPackageManager;
        }
        // 第二步
        IBinder b = ServiceManager.getService("package");
        //Slog.v("PackageManager", "default service binder = " + b);
        // 第三步 
        sPackageManager = IPackageManager.Stub.asInterface(b);
        //Slog.v("PackageManager", "default service = " + sPackageManager);
        return sPackageManager;
    }
```

这个方法的内部流程大体上分为三个步骤如下：

**第一步**：判断sPackageManager是否为空，如果为空，则说明是的第一次调用，走第二步，如果不为空，则直接返回sPackageManager

**第二步**：能走到第二步，说明这是第一次调用，则调用 ServiceManager 的 getService(String) 方法获取一个 IBinder 对象

**第三步**：通过调用 IPackageManager.Stub.asInterface(IBinder); 获取一个 sPackageManager 对象

这是明显的 AIDL 。

### (二)、IPackageManager.aidl

[IPackageManager.aidl源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fcontent%2Fpm%2FIPackageManager.aidl)

IPackageManager的AIDL结构如下：

![](https://upload-images.jianshu.io/upload_images/5713484-4748a479a25f57a7.png)

里面涉及IIterface、IPackageManager、IPackageManager.Stub、IPackageManager.Stub.Proxy这几类：

* IPackageManager 接口继承自 IIterface.java 接口。在 IIterface.java 接口定义了方法 asBinder()，其作用是将 IPackageManager 转换成 IBinder 对象。

* IPackageManager.Stub 是 IPackageManager 接口的的一个内部类，Stub 类实现了IBinder 和IPackageManager 接口。
  * IPackageManager.Stub 定义了  asInterface(IBinder) 方法；该方法将 IBinder 对象转换成 IPackageManager 类型的对象，返回的是 IPackageManager.Stub.Proxy 对象
  * IPackageManager.Stub 类重写了 Binder 类的 onTransact() 方法；该方法根据命令类型，处理数据传输。
  * IPackageManager.Stub 类实现了asBinder()方法，该方法直接返回 IPackageManager.Stub 对象。
  * IPackageManager.Stub 类并未实现IPackageManager 实现的方法。

* IPackageManager.Stub 类有一个内部类 Proxy，其中 Proxy 类实现了 IPackageManager 接口
  * IPackageManager.Stub 类的内部类 Proxy 持有一个 mRemote 对象，该对象是对 IPackageManager.Stub 的引用。
  * IPackageManager.Stub 类的内部类 Proxy 实现了 IPackageManager 的方法，这些方法通过 mRemote 调用 Binder 中的 transact() 方法，最终调用 IPackageManager.Stub 的 onTransact() 方法处理。
  * IPackageManager.Stub 类的内部类 Proxy 也实现了 IInterface.java 中定义的 asBinder 方法，该方法返回的是 mRemote

## 四、PackageMnager、IPackageManager与PackageManagerService

### (一)、ApplicationPackageManager 和 PackageManagerService 在 IPackageManager 的角色

在上面分析ContextImpl的getPackageManager()方法里面，我们知道

```kotlin
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm != null) {
            // Doesn't matter if we make more than one instance.
            return (mPackageManager = new ApplicationPackageManager(this, pm));
```

而在ActivityThread的静态方法getPackageManager()里面

```java
 sPackageManager = IPackageManager.Stub.asInterface(b);
```

所以可以在ApplicationPackageManager里面的mPM其实就是 IPackageManager.Stub内部类Proxy对象。那对应的IPackageManager.Stub是什么？对了就是PackageManagerService.java。为什么是它因为如下：

```java
public class PackageManagerService extends IPackageManager.Stub {
   ...
}
```

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java)

所以总结如下图

![](https://upload-images.jianshu.io/upload_images/5713484-3e75d4de4b56b02f.png)

所以结合上面的知识在结合PackageManager、ApplicationPackageManager和PackageManagerService总结如下：

* **IPackageManager 负责通信**。IPackageManager 接口类中定义了很多业务方法，但是由于安全等方面的考虑，Android 对外(即 SDK )提供的仅仅是一个子集，该子集被封装在抽象类 PackageManager 中。客户端一般通过Context的getPackageManager函数返回一个类型为PackageManager的对象，该对象的实际类型是PackageManager的子类ApplicationPackageManager。ApplicationPackageManager并没有直接参与Binder通信，而是通过mPM成员变量指向了一个IPackageManager.Stub.Proxy类型的对象

* AIDL中的Binder服务端是**PackageManagerService**，因为PackageManagerService继承自IPackageManager.Stub。由于IPackageManager.Stub类从Binder派生，所以PackageManagerService将作为服务端参与Binder通信。

* AIDL中的 Binder 客户端是**ApplicationPackageManager中成员变量mPM**，因为mPM内部指向的是IPackageManager.Stub.Proxy

整体流程的Binder结构大致如下：

![](https://upload-images.jianshu.io/upload_images/5713484-5bc8b7f82efd3cf3.png)

### (二)、获取Client的过程

从上面的图中知道通过 AIDL 结束，Client 通过 PackageManagerService 去快进程调用Server端的Stub，底层依然是依靠Binder机制进行机制，Client获取PackageManangerService的代理对象过程：

![](https://upload-images.jianshu.io/upload_images/5713484-680aed8e94348c87.png)

通过一层层的封装，Client调用PackageManagerService的过程最终是通过IPackageManager.Stub.Proxy类对象进行方法调用的

### (三)、在"安装"的角色与分工

一图以示之，如下：

![](https://upload-images.jianshu.io/upload_images/5713484-976927488080eec4.png)

### (四)、另类的理解

类比举例：

假设你是一个公司的商务负责人，正在和客户商谈事务，在涉及公司的具体业务的同事，你要请示你的老板，你需要给你老板打电话，交流商务谈判的具体细节，这里面，你就是应用进程里面的ApplicationPackageManager，IPackageManager就是你们的通信工具——电话，你老板就是SystemServer进程里面的PackageManagerService，你的电话就是IPackageManager.Stub.Proxy，老板的电话是IPackageManager.Stub。IPackageManager其实就是一个具体业务场景下的数据交换的工具而已。

## 五、PackageManagerService类简介

[PackageManagerService源码地址](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java)

### (一)、PackageManagerService概述与演化史

Android 的应用管理主要是通过PackageManagerService来完成的。PackageManagerService服务负责各种APK包的安装、卸载、优化和查询。

PackageManagerService演化史
如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-7ec235b67f39a822.png)

### (二)、PackageManagerService类

PackageManagerService继承自IPackageManager.Stub，Stub类从Binder派生，因此PackageManagerService将作为服务端参与Binder通信。

#### 1、重要的成员支持类

* ①**PackageParser**：
   这个类主要用于解析APK，解析其AndroidManifest.xml文件得到package的所有信息。补充一下：PackageParser.Package这个类用于容纳解析出的信息。

* ②**Settings**：
   这个类表示它服务处理设置和读取包的各种状态，它是动态的，比如userId，shareUser、permission、signature以及origPackg相关信息。安装 包即install package其实就从要安装的的package中抽取信息更新Settings中的内容，特别的是Settings针对shareUser和origPackage做了特别的关照。另外，为了加速启动速度，Settings的内容会写入/data/system/packages.xml、packages-backup.xml和packages.list，下次启动时会直接载入。

* ②**Installer**：这个类协助安装过程，更多的是将针对文件/路径的操作放在c和cpp里面去实现，真正的工作是由install承担的，Install只是通过named socket "installd" 连接 install，使用简单的cmd-respond协议只会intall完成工作，在其"install"命令中可以看到，其实只是创建了/data/data/\<packageName>目录而已。

#### 2、重要的成员变量

**final PackageInstallerService mInstallerService**：
 PackageInstallService实例，一个应用的安装时间比较长，Android就是用PackageInstallerService来管理应用的安装过程。在构造函数的最后创建。

**final Installer mInstaller**：
 被@GuardedBy 注解标记，它是Install的实例，用于和Demon进行install交互。实际上系统上进行APK格式转换、建立数据目录等工作，都是install进程来完成的。

**final Settings mSettings**：
 Setting的实例，保存一些PackageManagner动态设置信息

**final ArrayMap<String, PackageParser.Package> mPackages**：
 是被@GuardedBy注解标记的，代表系统已经安装的package

**final private ArrayMap<String, File> mExpectingBetter**：
 被升级过的应用列表

**final SparseArray<HashSet<String>> mSystemPermissions**：系统权限的集合

**final HashMap<String, String> mSharedLibraries**：当前已知的共享库

**final boolean mOnlyCore**：用于判断是否只扫描系统库

**final ActivityIntentResolver mActivities**：所有已知的Activity用于与其对应的Intent一一对应

**final ActivityIntentResolver mReceivers**：所有已知的Receiver用于与其对应的Intent一一对应

**final ServiceIntentResolver mServices**：所有已知的Service用于与其对应的Intent一一对应

**final ProviderIntentResolver mProviders**：所有已知的provider用于与其对应的Intent一一对应

## 六、ServiceManager与PackageManagerService关系

### (一)、ServiceManager回顾

ServiceManager 顾名思义是 Service 的管理，该类具有一个 HashMap<String, IBinder> 持有一个已经注册的 Servce，并提供相应的方法以便 Framework 层的调用。如入发现某个 Service 未注册，并会通过 ServiceManagerNative.java 这个 Service 获取。其中 ServiceManager 的 UML 类图如下：

![](https://upload-images.jianshu.io/upload_images/5713484-0556f58058dc1d7b.png)

ServiceManager 提供的 public 方法都是静态方法，这些方法实现大同小异。这里以getService(String)方法实现为例进行说明，其代码如下：

代码在[ServiceManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fos%2FServiceManager.java) 49行

```kotlin
public static IBinder getService(String name) {
    try {
        IBinder service = sCache.get(name);
        if (service != null) {
            return service;
        } else {
            return getIServiceManager().getService(name);
        }
    } catch (RemoteException e) {
        Log.e(TAG, "error in getService", e);
    }
    return null;
}
```

其中mRemote是一个ServiceNativeManager对象。从上述实现我们发现，getService方法在执行的过程中，可能会调用ServiceManager类中getIServiceManager()中用于创建ServieManagerNative对象，其代码如下：

代码在[ServiceManager.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fos%2FServiceManager.java) 33行

```csharp
private static IServiceManager getIServiceManager() {
    if (sServiceManager != null) {
        return sServiceManager;
    }
    // Find the service manager
    sServiceManager = ServiceManagerNative.asInterface(BinderInternal.getContextObject());
    return sServiceManager;
}
```

其中ServiceManagerNative.asInterface(XXX)方法，该方法实际返回IServiceManager对象，其实实际上一个ServiceManagerNative.Proxy对象。ServiceManagerNative.java中addService(),getService()都是native方法，最终会调用service_manager.c类中方法

### (二)、PackageManagerService注册

在Android启动的过程中，会启动SystemServer进行。在SystemServer启动的过程中，会调用PackageManagerService的main()函数来初始化一个PackageManagerService对象。其中main()实现如下：

```java
public static PackageManagerService main(Context context, Installer installer,boolean factoryTest, boolean onlyCore) {
    PackageManagerService m = new PackageManagerService(context, installer,factoryTest, onlyCore);
    ServiceManager.addService("package", m);
    return m;
}
```

## 七、总结

本片文章主要讲解了 PackageManager 与 PackageManagerService 的关系， PackageManagerService 事实上是一个 binder(PackageManagerService 继承自 IPackageManager.Stub，而 IPackageManager.Stub 继承自 Binder)，Client 端通过获取 PackageManagerService 的服务代理对象 IPackageManager.Stub.Proxy，Proxy 和 Stub 都实现了 IPackageManager 接口，Client 调用了 Proxy 中的接口和方法，通过 Proxy 中的 BinderProxy 对象传递经过 Binder 驱动调用服务端的 Binder 中的方法，即 Stub 中的接口实现，PackageManagerService 是 Stub 的子类，Stub中的接口方法在子类中具体实现，如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-76c35dec3b469730.png)

Binder框架如下：

![](https://upload-images.jianshu.io/upload_images/5713484-f4d275a686816e1c.png)

## 参考文章

1. [APK安装流程详解3——PackageManager与PackageManagerService](https://www.jianshu.com/p/a301291ca845)

