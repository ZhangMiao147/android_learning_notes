# Android 静默安装

## 静默安装的条件

实现静默安装需要应用是系统应用。

1. 把应用的 uid 设置为系统级别的，在 mainfest 标签下添加以下属性：

```xml
 android:sharedUserId="android.uid.system"
```

2. 仅仅设置 uid，还是没法实现静默安装，因为系统并不认为这个 app 是系统级别的应用，所以，还应该对该应用的 APK 进行系统签名。

如果不是系统应用去做静默安装的操作，则会有异常：

```
error: java.lang.SecurityException: Neither user 10051 nor current process has android.permission.INSTALL_PACKAGES.
```

## 静默安装代码

### sdk <= O(27)

如果 sdk 版本小于等于 O(27)，则通过反射调用 PackageManager 的 installPackage 方法安装应用，Packagelnstaller 是安卓系统底层用于 apk 安装的管理类，只有系统级别的应用，才能使用这个类的功能。：

```java
public static void install(final String filestr,final OnInstallListener listener){
	try{
    File file = new File(fileStr);
    PackageManager pm = BoxApplication.getInstance().getPackageManager();
    Method[] method = pm.getclass().getMethods();
    for (Method m : method){
      if (m.getName().equals("installPackage")){
        Class<?>[] getTypeParameters = m.getParameterTypes();
        for (Class<?> class1 : getTypeParameters){
          String parameterName = class1.getName();
          if (parameterName.equals("android.content.pm.IPackageInstallobserver")){
            m.invoke(pm,Uri.fromFile(file),
                     new IPackageInstallobserver.Stub(){
                       @Override
                       public void packageInstalled(final String packageName,final int returnCode)
                         throws RemoteException{
                         if (!TextUtils.isEmpty(packageName)&&!"null".equals(packageName)&&returncode =1){
                           // 安装成功
                           if (listener != null){
                             listener.onInstallSuccess(packageName,returnCode);
                           }
                         } else {
                           // 安装失败
                           if (listener != null){
                             listener.onInstallFailed(packageName,returnCode);
                           }
                         }
                       }
                     }
                     ,0x00000002|0x00000004|0x00000010|0x00000020|0x00000080|0x00000100,
                     "what is use for ?");
          }
        }
      }
    }
  }catch(Exception e){
    if (listener !null){
      listener.onInstallException(e.toString(),-1002);
    }
    e.printStackTrace();
  }
  }
```

用
PackageManager的installPackage方法有两个，这边反射调用的是第一个。

```java
@Deprecated
public abstract void installPackage(
Uri packageURI,IPackageInstallobserver observer,@InstallFlags int flags,String installerPackageName);

/*
* @deprecated replaced by {@link PackageInstaller}
* @hide
*/
@Deprecated
public abstract yoid installPackage(
Uri packageURI,
PackageInstallobserver observer,
@InstallFlags int flags,String installerPackageName);
```

调用PackageManager的installPackage方法时传递的 flags 的含义：0x00000002|0x00000004|0x00000010|0x00000020|0x00000080|0x00000100

* 0x00000002:public static final int INSTALL_REPLACE_EXISTING=0x00000002;
  如果应用已存在，则替换已安装的包。

* 0x00000004:public static final int INSTALL_ALLOW_TEST=0x00000004;
  允许安装测试包。

* 0x00000010:public static final int INSTALL_INTERNAL 0x00000010;

  应用必须被安装到内部存储。

* 0x00000020:public static final int INSTALL_FROM_ADB=0x00000020;
  标识应用是通过adb安装的。

* 0x00000080:public static final int INSTALL_ALLOW_DOWNGRADE=0x00000080;
  在当前安装的应用标记为可调试时，允许新安装的应用程序的版本低于当前安装的应用程序版本更新。

* 0x00000100:public static final int INSTALL_GRANT_RUNTIME_PERMISS/ONS=0x00000100;
  标识应将所有运行时权限授予该包，如果设置了INSTALL_ALL_USERS，则运行时权限将授予所有用户，否则仅授予所有者。

### sdk > O(27)

9以上的版本是使用Packagelnstaller进行安装的。

```java
public static int installAndroidP(PackageManager pm,String apkFilePath){
File apkFile new File(apkFilePath);
//1. 写入APK到InstallerSession
  PackageInstaller packageInstaller = pm.getPackageInstaller();
  PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(
PackageInstaller.SessionParams.MODE_FULL_INSTALL);
  sessionParams.setSize(apkFile.length());
  int sessionId = createSession(packageInstaller,sessionParams);
  if（sessionId=-1){
    boolean copySuccess copyInstallFile(packageInstaller,sessionId,apkFilePath);
    if (copySuccess){
      LogUtil.d(TAG,"installAndroidP is copySuccess..");
      // 2. 安装APK
      return execInstallCommand(packageInstaller,sessionId);
    } else {
      LogUtil.e(TAG,"copyInstallFile failed.");
    }
  } else {
    LogUtil.e(TAG,"createSession and session id is -1.");
  }
  return INSTALL_FAILED_ANDROID_P;
}
```

步骤分为两步：

1. 写入APK到nstallerSession
2. 安装APK

写入 APK 到 InstallSession：

```java
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private static int createSession(PackageInstaller packageInstaller,PackageInstaller.SessionParams sessionParams){
  int sessionId =-1;
  try{
    sessionId = packageInstaller.createSession(packageInstaller,sessionParams);
  } catch (IOException e){
    e.printStackTrace();
  }
  return sessionId;
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private static boolean copyInstallFile(PackageInstaller packageInstaller,int sessionId,String apkFilePath){
  InputStream in = null;
  OutputStream out = null;
  PackageInstaller.Session session = null;
  boolean success = false;
  try{
    File apkFile = new File(apkFilePath);
    session = packageInstaller.openSession(sessionId);
    out =session.openWrite("base.apk",0,apkFile.length());
    in = new FileInputStream(apkFile);
    int total = 0,c;
    byte[] buffer = new byte[65535];
    while ((c in.read(buffer))!=-1){
      total +=c;
      out.write(buffer,0,c);
    }
    session.fsync(out);
    success = true;
  } catch (IOException e){
    e.printStackTrace();
  } finally {
    closeQuietly(out);
    closeQuietly(in);
    closeQuietly(session);
  }
  return success;
}
```

安装APK:

```java
public static final String PACKAGE_INSTALL_ACTION = "com.test.AppInstall.SESSION_API_PACKAGE_INSTALLED";

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private static int execInstallCommand(PackageInstaller packageInstaller,
int sessionId){
  PackageInstaller.Session session = null;
  try {
    session = packageInstaller.openSession(sessionId);
androidPInstallobserver = new AndroidPInstallobserver();
    Intent intent = new Intent(BoxApplication.getInstance(),InstallResultReceiver.class);
    intent.setAction(PACKAGE_INSTALL_ACTION);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
BoxApplication.getInstance(),
1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    session.commit(pendingIntent.getIntentSender());
  } catch (IOException e){
LogUtil.d(TAG,"execInstallCommand IOException--"+
e.getLocalizedMessage());
e.printStackTrace();
  } finally {
    closeQuietly(session);
  }
  
  synchronized (androidPInstallobserver){
    while (!androidPInstallObserver.finished){
			try{
        androidPInstallObserver.wait();

      } catch (InterruptedException e){
        LogUtil.d(TAG,"AppOperator.execInstallCommand installation interrupted。");
      }
    }
    int result = androidPInstallobserver.result;
androidPInstallobserver = null;
    return result;
  }
}

public static class InstallResultReceiver extends BroadcastReceiver {
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)I
@Override
public void onReceive(Context context,Intent intent){
    if (intent != null){
      int status  = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
PackageInstaller.STATUS_FAILURE);
      String packageName  = intent.getStringExtra(
PackageInstaller.EXTRA_PACKAGE_NAME);
      if (androidPInstallobserver !null){
        androidPInstallobserver.onResult(status,packageName);

      } else {
        LogUtil.d(TAG，"androidPInstallObserver is null "+packageName+",status:"+status);
      }
    }
  }
}


```

在AndroidManifest里面注册InstallResultReceiver时，要加上对应的action:

```java
<receiver
android:name=".common.installafter9.InstallAfter9$InstallResultReceiver"
android:process="@string/market_task_process">
<intent-filter>
<action android:name="com.test.AppInstall.SESSION_API_PACKAGE_INSTALLED"/>
</intent-filter>
</receiver>
```

## 参考文章

1. [安卓实现静默安装](https://www.jianshu.com/p/91969bcc5d9f)
2. [通过PackageInstaller静默安装apk](https://blog.csdn.net/u013718730/article/details/121566325)
3. [无论是用户还是10051当前进程具有android.permission.INSTALL_PACKAGES](https://www.it1352.com/139349.html)

