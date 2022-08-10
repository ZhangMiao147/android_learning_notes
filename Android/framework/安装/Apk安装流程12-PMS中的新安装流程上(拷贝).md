# APK安装流程详解12——PMS中的新安装流程上(拷贝)

本片文章的主要内容如下：

```
1、ApplicationPackageManager中相关方法的跟踪
1.1 、installPackageWithVerification(Uri,PackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams)方法
1.2、installCommon(Uri,PackageInstallObserver, int, String,int)方法解析
2、PackageManagerService中相关方法的跟踪
2.1 installPackage(String, IPackageInstallObserver2, int, String,VerificationParams,String)方法解析
2.2 installPackageAsUser(String, IPackageInstallObserver2, int, String, VerificationParams,String, int)方法解析
2.3 PackageHandler的HandlerMesaage方法中what值为INIT_COPY的情况：
2.4 PackageHandler的connectToService方法解析
2.5 PackageHandler的HandlerMesaage方法中what值为MCS_BOUND的情况：
2.6 HandlerParams的startCopy方法
2.7 InstallParams的handleStartCopy方法
```

从上面一片文章我们知道InstallAppProgress里面最后更新的代码是调用到PackageManager#installPackageWithVerificationAndEncryption方法，那我们就从这个方法开始进行跟踪分析

总体流程大致如下：

![](https://upload-images.jianshu.io/upload_images/5713484-43ff27569c8d6e68.png)

涉及到类的流程如下：

![](https://upload-images.jianshu.io/upload_images/5713484-04583b3000dea3c0.png)

将上面整个安装流程分为两大步骤：

- 1、第一步：拷贝安装包
- 2、第二步：装载代码

本片文章主要讲解"拷贝"，即将安装包拷贝到/data目录下。

## 一、ApplicationPackageManager中相关方法的跟踪

通过前面的研究我们知道PackageManager是个抽象类，具体的实现类是ApplicationPackageManager，那我们就来看下ApplicationPackageManager类的installExistingPackage(String)方法

### (一)、installPackageWithVerification(Uri,PackageInstallObserver, int, String,Uri, ManifestDigest,ContainerEncryptionParams)方法解析

代码在[ApplicationPackageManager.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ApplicationPackageManager.java) 1370行

```dart
    @Override
    public void installPackageWithVerification(Uri packageURI,
            PackageInstallObserver observer, int flags, String installerPackageName,
            Uri verificationURI, ManifestDigest manifestDigest,
            ContainerEncryptionParams encryptionParams) {
        final VerificationParams verificationParams = new VerificationParams(verificationURI, null,
                null, VerificationParams.NO_UID, manifestDigest);
        installCommon(packageURI, observer, flags, installerPackageName, verificationParams,
                encryptionParams);
    }
```

这个方法内部很简单，就是调用了installCommon方法。

### (二)、installCommon(Uri,PackageInstallObserver, int, String,int)方法解析

代码在[ApplicationPackageManager.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/app/ApplicationPackageManager.java) 1370行

```dart
    private void installCommon(Uri packageURI,
                               PackageInstallObserver observer, int flags, String installerPackageName,
                               VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
        if (!"file".equals(packageURI.getScheme())) {
            throw new UnsupportedOperationException("Only file:// URIs are supported");
        }
        if (encryptionParams != null) {
            throw new UnsupportedOperationException("ContainerEncryptionParams not supported");
        }

        final String originPath = packageURI.getPath();
        try {
            mPM.installPackage(originPath, observer.getBinder(), flags, installerPackageName,
                    verificationParams, null);
        } catch (RemoteException ignored) {
        }
    }
```

这个方法内部做了两次判断，然后调用的mPM的installPackage方法。我们知道IPackageManager仅仅是AIDL的Binder通道，其实真正的调用方是PackageManagerService的installPackage(String, IPackageInstallObserver2, int, String, VerificationParams,String)方法。

## 二、PackageManagerService中相关方法的跟踪

### (一) installPackage(String, IPackageInstallObserver2, int, String,VerificationParams,String)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9513行

```dart
    @Override
    public void installPackage(String originPath, IPackageInstallObserver2 observer,
            int installFlags, String installerPackageName, VerificationParams verificationParams,
            String packageAbiOverride) {
        installPackageAsUser(originPath, observer, installFlags, installerPackageName,
                verificationParams, packageAbiOverride, UserHandle.getCallingUserId());
    }
```

看下这6个参数：

* originPath：安装包的位置，它必须是File类型在content的URI类型。这里传递过来的是toString的一个字符串。

* observer：是IPackageInstallObserver2类型的回调。通知调用者安装完成

* installFlags：它的值可能是如下的值中的一个
  * INSTALL_FORWARD_LOCK：表示安装过程中是否锁定
  * INSTALL_REPLACE_EXISTING：表示是否替换包
  * INSTALL_ALLOW_TEST：表示是否是测安装包
     比如在AndoridManifestL里面配置了"android:testOnly"，则这里的标志就是INSTALL_ALLOW_TEST

* installerPackageName：安装包的包名

* verificationParams：代表验证参数用于验证包安装

* packageAbiOverride：一般传null

PackageManagerService的installPackage方法什么都没做，直接调用了PackageManagerService的installPackageAsUser方法。

### (二) installPackageAsUser(String, IPackageInstallObserver2, int, String, VerificationParams,String, int)方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 9521行

```dart
    @Override
    public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer,
            int installFlags, String installerPackageName, VerificationParams verificationParams,
            String packageAbiOverride, int userId) {
        // 第一步
        // 检查是否具有install权限
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES, null);

        final int callingUid = Binder.getCallingUid();
        enforceCrossUserPermission(callingUid, userId, true, true, "installPackageAsUser");

         // 判断当前用户是否被Restricted，回调onPackageInstalled方法，安装失败
        if (isUserRestricted(userId, UserManager.DISALLOW_INSTALL_APPS)) {
            try {
                if (observer != null) {
                    observer.onPackageInstalled("", INSTALL_FAILED_USER_RESTRICTED, null, null);
                }
            } catch (RemoteException re) {
            }
            return;
        }
       
        // 第二步
        // 判断是安装来源是 ADB、shell和all_user
        if ((callingUid == Process.SHELL_UID) || (callingUid == Process.ROOT_UID)) {  
            // 如果发起端进程是shell或者root，则添加flag:INSTALL_FROM_ADB
            installFlags |= PackageManager.INSTALL_FROM_ADB;

        } else {
            // Caller holds INSTALL_PACKAGES permission, so we're less strict
            // about installerPackageName.
            // 如果不是则从flags中去掉INSTALL_FROM_ADB和INSTALL_ALL_USERS
            installFlags &= ~PackageManager.INSTALL_FROM_ADB;
            installFlags &= ~PackageManager.INSTALL_ALL_USERS;
        }

        UserHandle user;
        if ((installFlags & PackageManager.INSTALL_ALL_USERS) != 0) {
            user = UserHandle.ALL;
        } else {
            user = new UserHandle(userId);
        }

        // Only system components can circumvent runtime permissions when installing.
        // Android 6.0 时，当权限属于运行时权限时，需要弹出框，让用户授权，对于system app，应该取消弹框授权，而是直接授权
        if ((installFlags & PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS) != 0
                && mContext.checkCallingOrSelfPermission(Manifest.permission
                .INSTALL_GRANT_RUNTIME_PERMISSIONS) == PackageManager.PERMISSION_DENIED) {
            throw new SecurityException("You need the "
                    + "android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS permission "
                    + "to use the PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS flag");
        }

        verificationParams.setInstallerUid(callingUid);

        final File originFile = new File(originPath);
        final OriginInfo origin = OriginInfo.fromUntrustedFile(originFile);

        // 第三步
        // 发送"INIT_COPY"消息，构造InstallParams参数
        final Message msg = mHandler.obtainMessage(INIT_COPY);
        msg.obj = new InstallParams(origin, null, observer, installFlags, installerPackageName,
                null, verificationParams, user, packageAbiOverride, null);
        mHandler.sendMessage(msg);
    }
```

这里面涉及到一个mContext.enforceCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES, null);关于这个方法的详解请参考APK安装流程详解14——PMS中的新安装流程上(拷贝)补充中的 "***一、在PackageManagerService的installPackageAsUser方法里面的代码\***" 部分。

就像这个方法的名字叫"installPackageAsUser"一样，这段代码主要是对用户是否有权限安装进行检查，以及安装app是 仅仅给当前用户安装，还是给所有用户安装。通过上面代码可以看出，当安装进程是shell或者root时，否则大多数情况下，仅仅安装给当前用户。

这里主要是对当前用户是否有权限安装app进行检查，以及安装的app是仅仅为当前用户安装，还是给所有的用户安装。从以上代码可以得出，当安装进程是shell或者root时，flags中又包含了INSTALL_ALL_USERS时，才会给所有用户安装，否则大多数情况下，仅仅安装给当前的用户。当我们使用pm命令安装的时候，可以选择安装给哪个用户，也可以是全部用户，就是这个原因。

该函数主要做了以下操作：

* 第一步：获取权限，如果被拒绝，则退出执行

* 第二步：设置installFlags参数，即判断安装来源

* 第三步：发送了一个what值为INIT_COPY的message
   PS：这里注意这个msg的obj变量对应的InstallParams对象，这个InstallParams对象后面会多次用到；在构造InstallParams的时候注意他的两个参数为null。

该方法的主要流程如下：

![](https://upload-images.jianshu.io/upload_images/5713484-91f8780861b90c09.png)

PackageHandler来执行的，PackageHandler继承自 Handler，那我们来具体看看执行代码：

### (三) PackageHandler的HandlerMesaage方法中what值为INIT_COPY的情况：

因为PackageHandler继承Handler，所以我们来看下PackageHandler的HandlerMessage方法
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1131行

```csharp
        public void handleMessage(Message msg) {
            try {
                doHandleMessage(msg);
            } finally {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            }
        }
```

PackageHandler 是 PackageServiceManager 的内部类，并且继承 Handler，所以我们直接看 PackageHandler 的 HandlerMesaage 方法，我们看到PackageHandler的HandlerMesaage方法其实是调用doHandleMessage(Message)，然后在finally代码块里面设置了线程的优先级为后台线程。

那我们就来看下PackageHandler的doHandleMessage(Message)方法。

#### 1、PackageHandler的doHandleMessage方法

Message的what值是INIT_COPY。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1139行。

```csharp
        void doHandleMessage(Message msg) {
            switch (msg.what) {
                case INIT_COPY: {
                    // 第一步
                   // 取出InstallParams
                    HandlerParams params = (HandlerParams) msg.obj;
                     //  idx为当前需要安装的APK个数，mPendingInstalls里面保存所有需要安装的APK解析出来的HandlerParams参数
                    int idx = mPendingInstalls.size();
                    if (DEBUG_INSTALL) Slog.i(TAG, "init_copy idx=" + idx + ": " + params);
                    // If a bind was already initiated we dont really
                    // need to do anything. The pending install
                    // will be processed later on.
                    // 第二步 分两种情况
                    // 判断绑定的连接是否已经存在，如果已经绑定了，则mBound为true。如果是第一次调用mBound为false。
                    if (!mBound) {
                        // If this is the only one pending we might
                        // have to bind to the service again.
                         // 第三步 
                         // 连接安装Service
                        if (!connectToService()) {
                            Slog.e(TAG, "Failed to bind to media container service");
                            params.serviceError();
                            return;
                        } else {
                            // Once we bind to the service, the first
                            // pending request will be processed.
                             // 如果成功绑定Service后，将新的安装请求放入到mPendingIntalls中，等待处理
                            mPendingInstalls.add(idx, params);
                        }
                    } else {
                         // 如果之前已经绑定过服务，同样将新的请求到mPendingIntalls中，等待处理
                        mPendingInstalls.add(idx, params);
                        // Already bound to the service. Just make
                        // sure we trigger off processing the first request.
                        if (idx == 0) {
                            // 如果是第一个安装请求，则直接发送事件MCS_BOUND触发处理流程
                            mHandler.sendEmptyMessage(MCS_BOUND);
                        }
                    }
                    break;
                }
             ...
          }
```

将这个方法分为三个部分：

* 第一步，取出参数params，这个params就是之前传入的InstallParams

* 第二步，获取等待安装队列的个数，并根据mBound的值进行不同的处理。mBound为true，表示已经绑定，mBound为false表示未绑定。第一次调用则mBound为默认值为false。

* 第三步，如果是第一次调用，则调用connectToService()方法，如果不是第一次调用，且已经绑定则将params添加到mPendingInstalls(等待安装队列)的最后一个位置。如果是第一个安装请求，则发送MCS_BOUND事件触发接下来的流程。

这个方法整体流程如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-27cdb281cd1e593a.png)

如果是第一次走这个方法，则方法里面主要有两个流程

- 调用connectToService()方法
- 然后调用mPendingInstalls.add(idx, params);

如果不是第一次走这个方法，则方法里面的主要流程

- 先调用mPendingInstalls.add(idx, params);
- 发送一个what值是MCS_BOUND 的Message

假设第一次，则先调用connectToService()方法。

### (四) PackageHandler的connectToService方法解析

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1104行

```java
        private boolean connectToService() {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "Trying to bind to" +
                    " DefaultContainerService");
            Intent service = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            if (mContext.bindServiceAsUser(service, mDefContainerConn,
                    Context.BIND_AUTO_CREATE, UserHandle.OWNER)) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                mBound = true;
                return true;
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return false;
        }
```

这里可以看到bind到了一个service，这个service的ComponentName是"DEFAULT_CONTAINER_COMPONENT"这个常量，那我们就来看下这个ComponentName。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 377行

```dart
    static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";
    static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(
            DEFAULT_CONTAINER_PACKAGE,
            "com.android.defcontainer.DefaultContainerService");
```

所以知道bind的service是DefaultContainerService。绑定DefaultContainerService之后，设定进程的优先级为THREAD_PRIORITY_DEFAULT。然后等bindServiceAsUser这个方法执行完则又把线程的优先级设为THREAD_PRIORITY_BACKGROUND。

这里面涉及到一个变量mDefContainerConn。

#### 1、mDefContainerConn变量

要想研究mDefContainerConn，我们先来看下它的类型
代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 921行

```java
    final private DefaultContainerConnection mDefContainerConn =
            new DefaultContainerConnection();
```

mDefContainerConn的类型是DefaultContainerConnection，那我们来看下DefaultContainerConnection这个类。

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 923行

```java
    class DefaultContainerConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "onServiceConnected");
            IMediaContainerService imcs =
                IMediaContainerService.Stub.asInterface(service);
            mHandler.sendMessage(mHandler.obtainMessage(MCS_BOUND, imcs));
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG_SD_INSTALL) Log.i(TAG, "onServiceDisconnected");
        }
    }
```

通过上面代码知道**DefaultContainerConnection**实现了ServiceConnection。所以在连接成功的时候会调用onServiceConnected方法，看到当连接成功的时候会向mHandler发送一个Message去，这个Message的what值为MCS_BOUND。

#### 2、mDefContainerConn变量与DefaultContainerService

上文提及到mContext.bindServiceAsUser(service, mDefContainerConn,Context.BIND_AUTO_CREATE, UserHandle.OWNER)方法，其实就是"绑定"DefaultContainerService。我们知道bind一个Service，其中负责通信的ServiceConnection，而本方法中负责通信的就是mDefContainerConn。所以一旦绑定成功会执行mDefContainerConn的onServiceConnected方法。而现实是当绑定成功后在onServiceConnected中将一个IBinder转换成了一个IMediaContainerService。这个就是onServiceConnected回调函数中根据参数传进来的IMediaContainerService.Stub的对象引用创建的一个远程代理对象，后面PacakgeManagerServic通过该代理对象访问DefaultContainerService服务。

关于DefaultContainerService在文章[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**二、DefaultContainerService详解**中查看。

关于mContext.bindServiceAsUser(service, mDefContainerConn,
 Context.BIND_AUTO_CREATE, UserHandle.OWNER)在文章[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中查看。

### (五) PackageHandler的HandlerMesaage方法中what值为MCS_BOUND的情况：

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java)

```csharp
        void doHandleMessage(Message msg) {
            switch (msg.what) {
                   ...

                case MCS_BOUND: {
                    if (DEBUG_INSTALL) Slog.i(TAG, "mcs_bound");
                     // 第一步
                    if (msg.obj != null) {
                        mContainerService = (IMediaContainerService) msg.obj;
                    }
                     // 第二步 
                     // 如果绑定DefaultContainerService服务失败，则不能安装程序
                    if (mContainerService == null) {
                        // 第二步的情况A
                        if (!mBound) {
                            // Something seriously wrong since we are not bound and we are not
                            // waiting for connection. Bail out.
                            Slog.e(TAG, "Cannot bind to media container service");
                            for (HandlerParams params : mPendingInstalls) {
                                // Indicate service bind error
                                params.serviceError();
                            }
                            mPendingInstalls.clear();
                        } else {
                            Slog.w(TAG, "Waiting to connect to media container service");
                        }
                              // 如果安装请求队列不为空
                    } else if (mPendingInstalls.size() > 0) {
                      // 第二步的情况B
                        HandlerParams params = mPendingInstalls.get(0);
                        if (params != null) {
                            // 调动startCopy函数处理安装请求
                            if (params.startCopy()) {
                                // We are done...  look for more work or to
                                // go idle.
                                if (DEBUG_SD_INSTALL) Log.i(TAG,
                                        "Checking for more work or unbind...");
                                // Delete pending install
                                if (mPendingInstalls.size() > 0) {
                                    // 删除请求安装队列的头元素，即下标为0的元素
                                    mPendingInstalls.remove(0);
                                }
                               // 第二步的情况B下的分支甲              
                                if (mPendingInstalls.size() == 0) {
                                    // 如果安装请求都处理完了
                                    if (mBound) {
                                        if (DEBUG_SD_INSTALL) Log.i(TAG,
                                                "Posting delayed MCS_UNBIND");
                                        // 通过发送MCS_UNBIND消息处理断开绑定请求
                                        removeMessages(MCS_UNBIND);
                                        Message ubmsg = obtainMessage(MCS_UNBIND);
                                        // Unbind after a little delay, to avoid
                                        // continual thrashing.
                                        sendMessageDelayed(ubmsg, 10000);
                                    }
                                } else {
                                    // 第二步的情况B下的分支乙
                                    // There are more pending requests in queue.
                                    // Just post MCS_BOUND message to trigger processing
                                    // of next pending install.
                                    // 如果还有未处理的请求，则继续发送MCS_BOUND消息
                                    if (DEBUG_SD_INSTALL) Log.i(TAG,
                                            "Posting MCS_BOUND for next work");
                                    mHandler.sendEmptyMessage(MCS_BOUND);
                                }
                            }
                        }
                    } else {
                        // Should never happen ideally.
                        Slog.w(TAG, "Empty queue");
                    }
                    break;
                }
       }
```

将上面整体流程分为两个步骤

* 第一步，获取mContainerService对象
* 第二步，根据上面的mContainerService是否为空，进入下面两个分支
  * 情况A：如果mContainerService为null，则绑定DefaultContainerService失败，不能安装，则调用HandlerParams的serviceError方法，并清空mPendingInstalls列表
  * 情况B：如果mContainerService不为null。则获取安装等待队列mPendingInstalls的第一个元素，其实也就是我们最开始添加进去的InstallParams对象。并执行startCopy()方法，执行startCopy()方法后，删除我们我们获取的mPendingInstalls第一个元素。这时候又分甲乙两种情况，甲情况下是没有元素了，即mPendingInstalls.size=0；乙情况下还有元素，那我们就依次来看下
    * 分支甲：mPendingInstalls为空，且当前的绑定状态还是"绑定"(mBound为true)，则发送一个what为MCS_UNBIND的Message消息解除绑定
    * 分支乙：mPendingInstalls不为空，继续发送MCS_BOUND消息，继续处理下一个，只到队列为空

上面这个方法涉及到一个核心方法即startCopy()来处理安装请求，通过方法名叫"startCopy"我们猜测是"开始拷贝"的意思。

### (六) HandlerParams的startCopy方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10253行

```java
        final boolean startCopy() {
            boolean res;
            // 第一步
            try {
                if (DEBUG_INSTALL) Slog.i(TAG, "startCopy " + mUser + ": " + this);
                //MAX_RETIRES目前为4，表示尝试4次安装，如果还不成功，则认为安装失败
                if (++mRetries > MAX_RETRIES) {
                    Slog.w(TAG, "Failed to invoke remote methods on default container service. Giving up");
                    mHandler.sendEmptyMessage(MCS_GIVE_UP);
                    handleServiceError();
                    return false;
                } else {
                    // 调用handleStartCopy抽象方法
                    handleStartCopy();
                    res = true;
                }
            } catch (RemoteException e) {
                if (DEBUG_INSTALL) Slog.i(TAG, "Posting install MCS_RECONNECT");
                mHandler.sendEmptyMessage(MCS_RECONNECT);
                res = false;
            }
            // 第二步
             // 调用handleReturnCode抽象方法，这个方法会在handleStartCopy执行完拷贝相关行为之后，根据handleStartCopy做进一步的处理，主要返回状态码
            handleReturnCode();
            return res;
        }
```

涉及到一个HandlerParams和InstallParams的关系请参考[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**五、HandlerParams与InstallParams简介**

将上面方法内部划分为两个部分：

* 第一部分，即try-catch块内部，判断是错次数，是否大于四次，这里有两种情况：
  * 如果超过4次，即mRetries>=4则，表示已经尝试了4次(4次是极限)，则发送一个what值为MCS_GIVE_UP的Message放弃本次安装。
  * 如果没超过4次， 则调用handleStartCopy()方法，如果在这个方法中出现异常。其中handleStartCopy()为真正的核心方法。如果在handleStartCopy()方法调用的时候产生了异常则发送一个what为MCS_RECONNECT的Message

* 上面完成之后，调用handleReturnCode()。

这个方法涉及了到两个Message的what值的处理逻辑，这两个what值分别是MCS_GIVE_UP和MCS_RECONNECT，一起来看下：

MCS_RECONNECT代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1226行
MCS_GIVE_UP代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1262行。

```csharp
        void doHandleMessage(Message msg) {
            switch (msg.what) {
                ...
                case MCS_RECONNECT: {
                    if (DEBUG_INSTALL) Slog.i(TAG, "mcs_reconnect");
                    if (mPendingInstalls.size() > 0) {
                        if (mBound) {
                            disconnectService();
                        }
                        if (!connectToService()) {
                            Slog.e(TAG, "Failed to bind to media container service");
                            for (HandlerParams params : mPendingInstalls) {
                                // Indicate service bind error
                                params.serviceError();
                                Trace.asyncTraceEnd(TRACE_TAG_PACKAGE_MANAGER, "queueInstall",
                                        System.identityHashCode(params));
                            }
                            mPendingInstalls.clear();
                        }
                    }
                    break;
                }
                case MCS_GIVE_UP: {
                    if (DEBUG_INSTALL) Slog.i(TAG, "mcs_giveup too many retries");
                    HandlerParams params = mPendingInstalls.remove(0);
                    Trace.asyncTraceEnd(TRACE_TAG_PACKAGE_MANAGER, "queueInstall",
                            System.identityHashCode(params));
                    break;
                }
                ...
            }
        }
```

* MCS_RECONNECT：判断安装请求队列mPendingInstalls是否还有元素，如果有元素先断开绑定，则再次重新调用connectToService方法，我们知道connectToService()内部会再次执行绑定DefaultContainerService，而在绑定成功后会再次发送一个what值为MCS_BOUND的Message，从而又回到了startCopy里面。

* MCS_GIVE_UP：直接删除了安装请求队列mPendingInstalls里面下标为0的元素。

通过上文知道这个的HandlerParams的具体实现类是**InstallParams**，即handleStartCopy方法的具体实现是在InstallParams的handleStartCopy里面。

综上所述：

startCopy()方法调用其子类的InstallParams的handleStartCopy()来完成拷贝工作的，startCopy主要工作是进行错误处理，当捕获到handleStartCopy跑出的异常时，startCopy将发送MCS_RECONNECT，在MCS_RECONNECT消息处理中，将会重新绑定
 DefaultContainerService，如果绑定成功，那么安装过程将会重新开始。startCopy也就将会再次调用，重试的次数记录在mRetries中，当累计重试超过4次时，安装将失败，如果安装失败，startCopy将会调用handleReturnCode()方法来处理。

下面就来看下handleStartCopy的具体实现。

### (七) InstallParams的handleStartCopy方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 10307行

```java
        /*
         * Invoke remote method to get package information and install
         * location values. Override install location based on default
         * policy if needed and then create install arguments based
         * on the install location.
         */
        public void handleStartCopy() throws RemoteException {
            int ret = PackageManager.INSTALL_SUCCEEDED;
            // 第一步
            // If we're already staged, we've firmly committed to an install location
            // 是安装在手机内部存储空间还是sdcard中，设置对应标志位
            // 新安装的情况下stage为false
            if (origin.staged) {
                if (origin.file != null) {
                    installFlags |= PackageManager.INSTALL_INTERNAL;
                    installFlags &= ~PackageManager.INSTALL_EXTERNAL;
                } else if (origin.cid != null) {
                    installFlags |= PackageManager.INSTALL_EXTERNAL;
                    installFlags &= ~PackageManager.INSTALL_INTERNAL;
                } else {
                    throw new IllegalStateException("Invalid stage location");
                }
            }

             // 是否安装在SD卡上
            final boolean onSd = (installFlags & PackageManager.INSTALL_EXTERNAL) != 0;
             // 是否安装在内部空间上
            final boolean onInt = (installFlags & PackageManager.INSTALL_INTERNAL) != 0;

            PackageInfoLite pkgLite = null;

             // 检查APK的安装位置是否正确
            if (onInt && onSd) {
                // 如果既要安装到SD卡上也要安装上内部空间，则由冲突安装失败
                // Check if both bits are set.
                Slog.w(TAG, "Conflicting flags specified for installing on both internal and external");
                ret = PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION;
            } else {
                pkgLite = mContainerService.getMinimalPackageInfo(origin.resolvedPath, installFlags,
                        packageAbiOverride);

                /*
                 * If we have too little free space, try to free cache
                 * before giving up.
                 */
                 // 释放存储空间
                if (!origin.staged && pkgLite.recommendedInstallLocation
                        == PackageHelper.RECOMMEND_FAILED_INSUFFICIENT_STORAGE) {
                    // TODO: focus freeing disk space on the target device
                    final StorageManager storage = StorageManager.from(mContext);
                    final long lowThreshold = storage.getStorageLowBytes(
                            Environment.getDataDirectory());

                    final long sizeBytes = mContainerService.calculateInstalledSize(
                            origin.resolvedPath, isForwardLocked(), packageAbiOverride);

                    if (mInstaller.freeCache(null, sizeBytes + lowThreshold) >= 0) {
                        pkgLite = mContainerService.getMinimalPackageInfo(origin.resolvedPath,
                                installFlags, packageAbiOverride);
                   }

                    /*
                     * The cache free must have deleted the file we
                     * downloaded to install.
                     *
                     * TODO: fix the "freeCache" call to not delete
                     *       the file we care about.
                     */
                    if (pkgLite.recommendedInstallLocation
                            == PackageHelper.RECOMMEND_FAILED_INVALID_URI) {
                        pkgLite.recommendedInstallLocation
                            = PackageHelper.RECOMMEND_FAILED_INSUFFICIENT_STORAGE;
                    }
                }
            }
            
            // 第二步
            if (ret == PackageManager.INSTALL_SUCCEEDED) {
                int loc = pkgLite.recommendedInstallLocation;
                if (loc == PackageHelper.RECOMMEND_FAILED_INVALID_LOCATION) {
                    ret = PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION;
                } else if (loc == PackageHelper.RECOMMEND_FAILED_ALREADY_EXISTS) {
                    ret = PackageManager.INSTALL_FAILED_ALREADY_EXISTS;
                } else if (loc == PackageHelper.RECOMMEND_FAILED_INSUFFICIENT_STORAGE) {
                    ret = PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE;
                } else if (loc == PackageHelper.RECOMMEND_FAILED_INVALID_APK) {
                    ret = PackageManager.INSTALL_FAILED_INVALID_APK;
                } else if (loc == PackageHelper.RECOMMEND_FAILED_INVALID_URI) {
                    ret = PackageManager.INSTALL_FAILED_INVALID_URI;
                } else if (loc == PackageHelper.RECOMMEND_MEDIA_UNAVAILABLE) {
                    ret = PackageManager.INSTALL_FAILED_MEDIA_UNAVAILABLE;
                } else {
                    // Override with defaults if needed.
                    loc = installLocationPolicy(pkgLite);
                    if (loc == PackageHelper.RECOMMEND_FAILED_VERSION_DOWNGRADE) {
                        ret = PackageManager.INSTALL_FAILED_VERSION_DOWNGRADE;
                    } else if (!onSd && !onInt) {
                        // Override install location with flags
                        if (loc == PackageHelper.RECOMMEND_INSTALL_EXTERNAL) {
                            // Set the flag to install on external media.
                            installFlags |= PackageManager.INSTALL_EXTERNAL;
                            installFlags &= ~PackageManager.INSTALL_INTERNAL;
                        } else {
                            // Make sure the flag for installing on external
                            // media is unset
                            installFlags |= PackageManager.INSTALL_INTERNAL;
                            installFlags &= ~PackageManager.INSTALL_EXTERNAL;
                        }
                    }
                }
            }
            // 第三步
           //createInstallArgs 用于创建一个安装参数对象
            final InstallArgs args = createInstallArgs(this);
            mArgs = args;

             // 第四步
            if (ret == PackageManager.INSTALL_SUCCEEDED) {
                 /*
                 * ADB installs appear as UserHandle.USER_ALL, and can only be performed by
                 * UserHandle.USER_OWNER, so use the package verifier for UserHandle.USER_OWNER.
                 */
                int userIdentifier = getUser().getIdentifier();
                if (userIdentifier == UserHandle.USER_ALL
                        && ((installFlags & PackageManager.INSTALL_FROM_ADB) != 0)) {
                    userIdentifier = UserHandle.USER_OWNER;
                }

                /*
                 * Determine if we have any installed package verifiers. If we
                 * do, then we'll defer to them to verify the packages.
                 */
                final int requiredUid = mRequiredVerifierPackage == null ? -1
                        : getPackageUid(mRequiredVerifierPackage, userIdentifier);
                if (!origin.existing && requiredUid != -1
                        && isVerificationEnabled(userIdentifier, installFlags)) {
                    // 对当前包做验证操作
                    final Intent verification = new Intent(
                            Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
                    verification.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    verification.setDataAndType(Uri.fromFile(new File(origin.resolvedPath)),
                            PACKAGE_MIME_TYPE);
                    verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    final List<ResolveInfo> receivers = queryIntentReceivers(verification,
                            PACKAGE_MIME_TYPE, PackageManager.GET_DISABLED_COMPONENTS,
                            0 /* TODO: Which userId? */);

                    if (DEBUG_VERIFY) {
                        Slog.d(TAG, "Found " + receivers.size() + " verifiers for intent "
                                + verification.toString() + " with " + pkgLite.verifiers.length
                                + " optional verifiers");
                    }

                    final int verificationId = mPendingVerificationToken++;

                    verification.putExtra(PackageManager.EXTRA_VERIFICATION_ID, verificationId);

                    verification.putExtra(PackageManager.EXTRA_VERIFICATION_INSTALLER_PACKAGE,
                            installerPackageName);

                    verification.putExtra(PackageManager.EXTRA_VERIFICATION_INSTALL_FLAGS,
                            installFlags);

                    verification.putExtra(PackageManager.EXTRA_VERIFICATION_PACKAGE_NAME,
                            pkgLite.packageName);

                    verification.putExtra(PackageManager.EXTRA_VERIFICATION_VERSION_CODE,
                            pkgLite.versionCode);

                    if (verificationParams != null) {
                        if (verificationParams.getVerificationURI() != null) {
                           verification.putExtra(PackageManager.EXTRA_VERIFICATION_URI,
                                 verificationParams.getVerificationURI());
                        }
                        if (verificationParams.getOriginatingURI() != null) {
                            verification.putExtra(Intent.EXTRA_ORIGINATING_URI,
                                  verificationParams.getOriginatingURI());
                        }
                        if (verificationParams.getReferrer() != null) {
                            verification.putExtra(Intent.EXTRA_REFERRER,
                                  verificationParams.getReferrer());
                        }
                        if (verificationParams.getOriginatingUid() >= 0) {
                            verification.putExtra(Intent.EXTRA_ORIGINATING_UID,
                                  verificationParams.getOriginatingUid());
                        }
                        if (verificationParams.getInstallerUid() >= 0) {
                            verification.putExtra(PackageManager.EXTRA_VERIFICATION_INSTALLER_UID,
                                  verificationParams.getInstallerUid());
                        }
                    }

                    final PackageVerificationState verificationState = new PackageVerificationState(
                            requiredUid, args);

                    mPendingVerification.append(verificationId, verificationState);

                    final List<ComponentName> sufficientVerifiers = matchVerifiers(pkgLite,
                            receivers, verificationState);

                    // Apps installed for "all" users use the device owner to verify the app
                    UserHandle verifierUser = getUser();
                    if (verifierUser == UserHandle.ALL) {
                        verifierUser = UserHandle.OWNER;
                    }

                    /*
                     * If any sufficient verifiers were listed in the package
                     * manifest, attempt to ask them.
                     */
                    if (sufficientVerifiers != null) {
                        final int N = sufficientVerifiers.size();
                        if (N == 0) {
                            Slog.i(TAG, "Additional verifiers required, but none installed.");
                            ret = PackageManager.INSTALL_FAILED_VERIFICATION_FAILURE;
                        } else {
                            for (int i = 0; i < N; i++) {
                                final ComponentName verifierComponent = sufficientVerifiers.get(i);

                                final Intent sufficientIntent = new Intent(verification);
                                sufficientIntent.setComponent(verifierComponent);
                                mContext.sendBroadcastAsUser(sufficientIntent, verifierUser);
                            }
                        }
                    }

                    final ComponentName requiredVerifierComponent = matchComponentForVerifier(
                            mRequiredVerifierPackage, receivers);
                    if (ret == PackageManager.INSTALL_SUCCEEDED
                            && mRequiredVerifierPackage != null) {
                        /*
                         * Send the intent to the required verification agent,
                         * but only start the verification timeout after the
                         * target BroadcastReceivers have run.
                         */
                        verification.setComponent(requiredVerifierComponent);
                        mContext.sendOrderedBroadcastAsUser(verification, verifierUser,
                                android.Manifest.permission.PACKAGE_VERIFICATION_AGENT,
                                new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        final Message msg = mHandler
                                                .obtainMessage(CHECK_PENDING_VERIFICATION);
                                        msg.arg1 = verificationId;
                                        mHandler.sendMessageDelayed(msg, getVerificationTimeout());
                                    }
                                }, null, 0, null, null);

                        /*
                         * We don't want the copy to proceed until verification
                         * succeeds, so null out this field.
                         */
                        mArgs = null;
                    }
                } else {
                     // 不需要做验证
                    /*
                     * No package verification is enabled, so immediately start
                     * the remote call to initiate copy using temporary file.
                     */
                    // 调用InstallArgs的copyApk函数
                    ret = args.copyApk(mContainerService, true);
                }
            }

            mRet = ret;
        }
```

* 第一步：判断origin.staged的值，要判断origin.staged的值，这里origin.staged为false，关于为什么origin.staged为false，请查看[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**七、为什么新安装的情况下 origin.staged等于false**

* 第二步：设置ret参数

* 第三步：创建安装参数对象args

* 第四步：这里根据是否需要验证分为两种情况
  * 情况1——需要验证：需要构建一个Intent对象verifaction，并设置相应的参数，然后发送一个广播进行包验证。在广播的onReceive中，则发送了一个what值为CHECK_PENDING_VERIFICATION的message，这个message的arg1的值为verificationId，
  * 情况2——不需要验证：直接调用InstallArgs的copyApk方法进行包拷贝

整体流程图下图：

![](https://upload-images.jianshu.io/upload_images/5713484-57832a382b45db18.png)

这方面里面涉及到的几个比较难理解的地方，如下：

* 1、mContainerService.getMinimalPackageInfo(origin.resolvedPath, installFlags, packageAbiOverride)方法：

* 2、mContainerService.calculateInstalledSize(origin.resolvedPath, isForwardLocked(), packageAbiOverride)方法：
   请参考[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的 **三、mContainerService.getMinimalPackageInfo(String.int,String)方法与calculateInstalledSize(String,boolean,String)方法的讲解** 里面会想详细讲解

* 3、createInstallArgs(InstallParams)方法：
   请参考[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**九、createInstallArgs(InstallParams)方法解答**

* 4、isVerificationEnabled(int userId, int installFlags) 的理解：
   请参考[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**十、sVerificationEnabled(int userId, int installFlags) 的理解方法解答**

* 5、Context.sendBroadcast(Intent intent)的功能是否和Context.sendBroadcastAsUser一样：
   请参考[APK安装流程详解14——PMS中的新安装流程上(拷贝)补充]中的**十一、Context.sendBroadcast(Intent intent)的功能是和Context.sendBroadcastAsUser(Intent,UserHandle)一样的解答**

这里面会涉及两个问题如下：

* 1、what值为CHECK_PENDING_VERIFICATION的Message的处理内容

* 2、InstallArgs的copyApk(IMediaContainerService, boolean)方法的具体实现

##### 1、what值为CHECK_PENDING_VERIFICATION的Message的处理内容

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 1506行

```dart
                case CHECK_PENDING_VERIFICATION: {
                    // 获取verificationId
                    final int verificationId = msg.arg1;
                    // mPendingVerification 是一个SparseArray，键值对是verificationId和其对应的PackageVerificationState
                  // PackageVerificationState 可以理解为安装包验证的状态
                   final PackageVerificationState state = mPendingVerification.get(verificationId);
                    // 如果包的验证状态不为空，并且验证没有超时
                    if ((state != null) && !state.timeoutExtended()) {
                        final InstallArgs args = state.getInstallArgs();
                        final Uri originUri = Uri.fromFile(args.origin.resolvedFile);

                        Slog.i(TAG, "Verification timed out for " + originUri);
                        // 移除verificationId对应的PackageVerificationState对象
                        mPendingVerification.remove(verificationId);
                        // 初始化ret值为 验证失败导致安装失败
                        int ret = PackageManager.INSTALL_FAILED_VERIFICATION_FAILURE;
                         // 如果默认的包验证器允许继续安装
                        if (getDefaultVerificationResponse() == PackageManager.VERIFICATION_ALLOW) {
                            Slog.i(TAG, "Continuing with installation of " + originUri);
                            // 设置验证跟踪类别，PackageManager.VERIFICATION_ALLOW_WITHOUT_SUFFICIENT表示：允许在没有验证者的情况下进行安装
                            state.setVerifierResponse(Binder.getCallingUid(),
                                    PackageManager.VERIFICATION_ALLOW_WITHOUT_SUFFICIENT);
                            //发送广播，对应的ACTION为ACTION_PACKAGE_VERIFIED
                            broadcastPackageVerified(verificationId, originUri,
                                    PackageManager.VERIFICATION_ALLOW,
                                    state.getInstallArgs().getUser());
                            try {
                                // 调用args的copyApk(IMediaContainerService,boolean)方法
                                ret = args.copyApk(mContainerService, true);
                            } catch (RemoteException e) {
                                Slog.e(TAG, "Could not contact the ContainerService");
                            }
                        } else {
                             // 如果默认的包验证器，拒绝验证，则发送广播告诉之
                            broadcastPackageVerified(verificationId, originUri,
                                    PackageManager.VERIFICATION_REJECT,
                                    state.getInstallArgs().getUser());
                        }
                        // 调用processPendingInstall方法
                        processPendingInstall(args, ret);
                        // 发送一个what值为MCS_UNBIND的Message
                        mHandler.sendEmptyMessage(MCS_UNBIND);
                    }
                    break;
                }
```

发现无论是否需要验证，最后都是调用InstallArgs的copyApk(IMediaContainerService,boolean)方法，而且这两个入参都是固定的，第一个参数为mContainerService，第二个参数为true。所以最后的拷贝肯定是通过copyApk来执行的，通过上面的代码我们知道这个InstallArgs其实是FileInstallArgs。所以我们只需要关注FileInstallArgs的copyApk(IMediaContainerService,boolean)方法即可

##### 2、FileInstallArgs的copyApk(IMediaContainerService,boolean)方法

代码在[PackageManagerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerService.java) 11050行

```java
        int copyApk(IMediaContainerService imcs, boolean temp) throws RemoteException {
            // 上面我们已经说了，在新安装的情况下origin.staged=false
            if (origin.staged) {
                if (DEBUG_INSTALL) Slog.d(TAG, origin.file + " already staged; skipping copy");
                codeFile = origin.file;
                resourceFile = origin.file;
                return PackageManager.INSTALL_SUCCEEDED;
            }
           // 第一步
           // 获取目录
            try {
                // 创建目录
                final File tempDir = mInstallerService.allocateStageDirLegacy(volumeUuid);
                codeFile = tempDir;
                resourceFile = tempDir;
            } catch (IOException e) {
                Slog.w(TAG, "Failed to create copy file: " + e);
                return PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE;
            }

            final IParcelFileDescriptorFactory target = new IParcelFileDescriptorFactory.Stub() {
                @Override
                public ParcelFileDescriptor open(String name, int mode) throws RemoteException {
                    if (!FileUtils.isValidExtFilename(name)) {
                        throw new IllegalArgumentException("Invalid filename: " + name);
                    }
                    try {
                        final File file = new File(codeFile, name);
                        final FileDescriptor fd = Os.open(file.getAbsolutePath(),
                                O_RDWR | O_CREAT, 0644);
                        Os.chmod(file.getAbsolutePath(), 0644);
                        return new ParcelFileDescriptor(fd);
                    } catch (ErrnoException e) {
                        throw new RemoteException("Failed to open: " + e.getMessage());
                    }
                }
            };

            int ret = PackageManager.INSTALL_SUCCEEDED;
             // 第二步
            // 真正的文件拷贝
            ret = imcs.copyPackage(origin.file.getAbsolutePath(), target);
            if (ret != PackageManager.INSTALL_SUCCEEDED) {
                Slog.e(TAG, "Failed to copy package");
                return ret;
            }
             // 第三步
            // 获取库的根目录
            final File libraryRoot = new File(codeFile, LIB_DIR_NAME);
            NativeLibraryHelper.Handle handle = null;
            // 拷贝 Native代码 即so文件
            try {
                handle = NativeLibraryHelper.Handle.create(codeFile);
                ret = NativeLibraryHelper.copyNativeBinariesWithOverride(handle, libraryRoot,
                        abiOverride);
            } catch (IOException e) {
                Slog.e(TAG, "Copying native libraries failed", e);
                ret = PackageManager.INSTALL_FAILED_INTERNAL_ERROR;
            } finally {
                IoUtils.closeQuietly(handle);
            }
            return ret;
        }
```

将上面的内容大体上分为三个步骤：

- 第一步：创建目录文件
- 第二步：进行代码拷贝
- 第三步：进行Native代码拷贝

这里重点说下这行代码**ret = imcs.copyPackage(origin.file.getAbsolutePath(), target)**。我们知道imc其实是一个远程的代理，实际的调用方式DefaultContainerService的成员变量mBinder。那我们就一起来看下DefaultContainerService的成员变量mBinder的copyPackage方法的执行。

##### 3、DefaultContainerService#mBinder#copyPackage方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 128行

```dart
        /**
         * Copy package to the target location.
         *
         * @param packagePath absolute path to the package to be copied. Can be
         *            a single monolithic APK file or a cluster directory
         *            containing one or more APKs.
         * @return returns status code according to those in
         *         {@link PackageManager}
         */
        @Override
        public int copyPackage(String packagePath, IParcelFileDescriptorFactory target) {
            // 第一步：
            if (packagePath == null || target == null) {
                return PackageManager.INSTALL_FAILED_INVALID_URI;
            }

             // 第二步：
            PackageLite pkg = null;
            try {
                final File packageFile = new File(packagePath);
                pkg = PackageParser.parsePackageLite(packageFile, 0);
                // 第三步
                return copyPackageInner(pkg, target);
            } catch (PackageParserException | IOException | RemoteException e) {
                Slog.w(TAG, "Failed to copy package at " + packagePath + ": " + e);
                return PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE;
            }
        }
```

来看下注释

将包复制到目标位置

- 入参：packagePath：要复制包的绝对路径，可以是一个APK文件，也可以是多个APK文件
- 出参：安装的状态码

将这方法内部整体流程分为三步：

* 第一步，判断入参packagePath和target是否为空，如果为空则直接返回安装失败，失败原因不正确的地址路径

* 第二步，通过PackageParser的PackageParser方法解析出"轻量级"的安装包内容。

* 第三步，调用copyPackageInner(PackageLite, IParcelFileDescriptorFactory)方法

在第二步和第三步中有个try-catch块，如果其中出现异常，则直接返回安装失败，失败原因是空间不足。

所以看出这个copyPackage方法内部，其实没有真正的实现包拷贝，而是类似于"预加载"的作用，先获取一个"轻量级"的安装包。

那来看下copyPackageInner方法。

##### 4、DefaultContainerService#copyPackageInner方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 367行

```java
    private int copyPackageInner(PackageLite pkg, IParcelFileDescriptorFactory target)
            throws IOException, RemoteException {
         // 第一部分
        copyFile(pkg.baseCodePath, target, "base.apk");
         // 第二部分
        if (!ArrayUtils.isEmpty(pkg.splitNames)) {
            for (int i = 0; i < pkg.splitNames.length; i++) {
                copyFile(pkg.splitCodePaths[i], target, "split_" + pkg.splitNames[i] + ".apk");
            }
        }
        return PackageManager.INSTALL_SUCCEEDED;
    }
```

将这个方法的内部执行分为两个部分：

* 第一部分：调用copyFile，注意这里传入两个的参数是baseCodePath和"base.apk"

* 第二部分：如果PackageLite有APK拆分，则遍历所有的分APK，再依次调用copyFile

看到这里调用了copyFile(String, IParcelFileDescriptorFactory, String)方法来进行的代码拷贝，那就来看下copyFile里面是怎么执行的。

##### 5、DefaultContainerService#copyFile(String, IParcelFileDescriptorFactory, String)方法

代码在[DefaultContainerService.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/frameworks/base/packages/DefaultContainerService/src/com/android/defcontainer/DefaultContainerService.java) 379行

```csharp
    private void copyFile(String sourcePath, IParcelFileDescriptorFactory target, String targetName)
            throws IOException, RemoteException {
        Slog.d(TAG, "Copying " + sourcePath + " to " + targetName);
        InputStream in = null;
        OutputStream out = null;
        try {
            // 输入流
            in = new FileInputStream(sourcePath);
             // 输出流
            out = new ParcelFileDescriptor.AutoCloseOutputStream(
                    target.open(targetName, ParcelFileDescriptor.MODE_READ_WRITE));
            // 进行拷贝
            Streams.copy(in, out);
        } finally {
            IoUtils.closeQuietly(out);
            IoUtils.closeQuietly(in);
        }
    }
```

这个方法很简单，就是构建输入流，构建输出流，然后调用Streams的方法进行拷贝。

顺带说下[Streams.java](https://link.jianshu.com/?t=http://androidxref.com/6.0.1_r10/xref/libcore/luni/src/main/java/libcore/io/Streams.java)这个类，这个类是libcore/io/Streams.java。copy方法如下：

```csharp
    /**
     * Copies all of the bytes from {@code in} to {@code out}. Neither stream is closed.
     * Returns the total number of bytes transferred.
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        int total = 0;
        byte[] buffer = new byte[8192];
        int c;
        while ((c = in.read(buffer)) != -1) {
            total += c;
            out.write(buffer, 0, c);
        }
        return total;
    }
```

通过这个方法完成了安装包的拷贝功能。

至此 PMS中的新安装流程上(拷贝)已经全部讲解完成。

## 参考文章

1. [APK安装流程详解12——PMS中的新安装流程上(拷贝)](https://www.jianshu.com/p/c4333c7eb409)

