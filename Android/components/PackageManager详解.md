# PackageManager 详解

　　今天要讲的是PackageManager。Android系统为我们提供了很多服务管理的类，包括 ActivityManager、PowerManager(电源管理)、AudioManager(音频管理)等。除此之外，还提供了一个 PackageManger 管理类，它的主要职责是管理应用程序包。 通过PackageManager，我们就可以获取应用程序信息。

　　提到PackageManager，就得提一下AndroidManifest.XML文件了。AndroidManifest.xml是Android应用程序中最重要的文件之一。它是Android程序的全局配置文件，是每个 android程序中必须的文件。它位于我们开发的应用程序的根目录下，描述了package中的全局数据，包括package中暴露的组件 （activities, services, 等等），以及他们各自的实现类，各种能被处理的数据和启动位置等重要信息。 
　　因此，该文件提供了Android系统所需要的关于该应用程序的必要信息，即在该应用程序的任何代码运行之前系统所必须拥有的信息。

　　PackageManager获取的信息即来自AndroidManifest.XML。为了便于理解，从网上找了一张AnroidManifest.xml文件节点说明图：

![img](https://images0.cnblogs.com/blog/481760/201408/241503542538902.gif)

 

**一、PackageManager的功能：**

1、安装，卸载应用 
2、查询permission相关信息 
3、查询Application相关信息(application，activity，receiver，service，provider及相应属性等） 
4、查询已安装应用 
5、增加，删除permission 
6、清除用户数据、缓存，代码段等 

 

**二、PackageManager相关类和方法介绍：**

1、PackageManager类

```
说明： 获得已安装的应用程序信息 。可以通过getPackageManager()方法获得。 

常用方法： 
public abstract PackageManager getPackageManager()  
功能：获得一个PackageManger对象  

public abstract Drawable getApplicationIcon(String packageName)
参数： packageName 包名
功能：返回给定包名的图标，否则返回null

public abstract ApplicationInfo getApplicationInfo(String packageName, int flags)
参数：
　　packagename 包名
　　flags 该ApplicationInfo是此flags标记，通常可以直接赋予常数0即可
功能：返回该ApplicationInfo对象

public abstract List<ApplicationInfo> getInstalledApplications(int flags)
参数：
　　flag为一般为GET_UNINSTALLED_PACKAGES，那么此时会返回所有ApplicationInfo。我们可以对ApplicationInfo
　　的flags过滤,得到我们需要的。
功能：返回给定条件的所有PackageInfo

public abstract List<PackageInfo> getInstalledPackages(int flags) 
参数如上
功能：返回给定条件的所有PackageInfo

public abstract ResolveInfo resolveActivity(Intent intent, int flags)
参数：  
　　intent 查寻条件，Activity所配置的action和category
　　flags： MATCH_DEFAULT_ONLY     ：Category必须带有CATEGORY_DEFAULT的Activity，才匹配
　　　　　　 GET_INTENT_FILTERS     ：匹配Intent条件即可
　　　　　　 GET_RESOLVED_FILTER    ：匹配Intent条件即可
功能 ：返回给定条件的ResolveInfo对象(本质上是Activity)
 
public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int flags)
参数同上
功能 ：返回给定条件的所有ResolveInfo对象(本质上是Activity)，集合对象

public abstract ResolveInfo resolveService(Intent intent, int flags)
参数同上
功能 ：返回给定条件的ResolveInfo对象(本质上是Service)
 
public abstract List<ResolveInfo> queryIntentServices(Intent intent, int flags)
参数同上
功能 ：返回给定条件的所有ResolveInfo对象(本质上是Service)，集合对象
```

2、PackageItemInfo类

```
说明： AndroidManifest.xml文件中所有节点的基类，提供了这些节点的基本信息：label、icon、 meta-data。它并不直接使用，而是由子类继承然后调用相应方法。
```

3、ApplicationInfo类 继承自 PackageItemInfo类

```
说明：获取一个特定引用程序中<application>节点的信息。

字段说明：
        flags字段： FLAG_SYSTEM　系统应用程序
                    FLAG_EXTERNAL_STORAGE　表示该应用安装在sdcard中

常用方法继承至PackageItemInfo类中的loadIcon()和loadLabel()
```

4、ActivityInfo类 继承自 PackageItemInfo类

```
说明： 获得应用程序中<activity/>或者 <receiver/>节点的信息 。我们可以通过它来获取我们设置的任何属性，包括theme 、launchMode、launchmode等

常用方法继承至PackageItemInfo类中的loadIcon()和loadLabel()
```

5、ServiceInfo类 继承自 PackageItemInfo类

```
说明：与ActivityInfo类似，代表<service>节点信息
```

6、ResolveInfo类

```
说明：根据<intent>节点来获取其上一层目录的信息，通常是<activity>、<receiver>、<service>节点信息。
常用方法有loadIcon(PackageManager pm)和loadLabel(PackageManager pm)
```

 **三、实例讲解：**

1、通过PackageManager的queryIntentActivities方法，查询系统中所有满足ACTION_MAIN和CATEGORY_LAUNCHER的应用程序，获取他们的程序名、包名、入口类名。（水平有限，ListView没学好，不能做一个简易启动器，不过启动应用的原理在之前的文章中有提到，有兴趣的可以去看看：[Android随笔之——Activity中启动另一应用](http://www.cnblogs.com/travellife/p/3930179.html)）

MainActivity.java

```
 1 package com.example.packagemanager;
 2 
 3 import java.util.Collections;
 4 import java.util.List;
 5 
 6 import android.app.Activity;
 7 import android.content.Intent;
 8 import android.content.pm.PackageManager;
 9 import android.content.pm.ResolveInfo;
10 import android.os.Bundle;
11 
12 public class MainActivity extends Activity {
13 
14     @Override
15     protected void onCreate(Bundle savedInstanceState) {
16         super.onCreate(savedInstanceState);
17         setContentView(R.layout.activity_main);
18         getAppInfo();
19     }
20 
21     private void getAppInfo() {
22         // 获取PackageManager对象
23         PackageManager pm = this.getPackageManager();
24         // 设置<intent-filter>标签内需要满足的条件
25         Intent intent = new Intent(Intent.ACTION_MAIN, null);
26         intent.addCategory(Intent.CATEGORY_DEFAULT);
27 
28         // 通过queryIntentActivities获取ResolveInfo对象
29         List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent,
30                 PackageManager.MATCH_DEFAULT_ONLY);
31 
32         // 调用系统排序，根据name排序
33         // 该排序很重要，否则只能显示系统应用，不能显示第三方应用
34         // 其实我测试发现有没有其实是一样的，就是输出的顺序是乱的
35         Collections.sort(resolveInfos,
36                 new ResolveInfo.DisplayNameComparator(pm));
37 
38         for (ResolveInfo resolveInfo : resolveInfos) {
39             String appName = resolveInfo.loadLabel(pm).toString();// 获取应用名称
40             String packageName = resolveInfo.activityInfo.packageName;// 包名
41             String className = resolveInfo.activityInfo.name;// 入口类名
42             System.out.println("程序名：" + appName + " 包名:" + packageName
43                     + " 入口类名：" + className);
44         }
45     }
46 
47 }
```

 输出结果：

![img](https://images0.cnblogs.com/blog/481760/201408/241612333938824.jpg)

2、通过PackageManager的queryInstalledApplications方法，过滤掉出系统应用、第三方应用、安装在SDCard上的应用。

MainActivity.java

```
  1 package com.example.packagemanager;
  2 
  3 import java.util.Collections;
  4 import java.util.List;
  5 
  6 import android.app.Activity;
  7 import android.content.pm.ApplicationInfo;
  8 import android.content.pm.PackageManager;
  9 import android.os.Bundle;
 10 import android.view.View;
 11 import android.view.View.OnClickListener;
 12 
 13 public class MainActivity extends Activity implements OnClickListener {
 14 
 15     public static final int FILTER_ALL_APP = 0; // 所有应用程序
 16     public static final int FILTER_SYSTEM_APP = 1; // 系统程序
 17     public static final int FILTER_THIRD_APP = 2; // 第三方应用程序
 18     public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序
 19     private PackageManager pm;
 20 
 21     @Override
 22     protected void onCreate(Bundle savedInstanceState) {
 23         super.onCreate(savedInstanceState);
 24         setContentView(R.layout.activity_main);
 25 
 26         findViewById(R.id.btn_all).setOnClickListener(this);
 27         findViewById(R.id.btn_system).setOnClickListener(this);
 28         findViewById(R.id.btn_third).setOnClickListener(this);
 29         findViewById(R.id.btn_sdcard).setOnClickListener(this);
 30     }
 31 
 32     /**
 33      * 过滤，选择是系统应用、第三方应用或者SDCard应用
 34      */
 35     private void filterApp(int type) {
 36         // 获取PackageManager对象
 37         pm = getPackageManager();
 38         // 查询已经安装的应用程序
 39         List<ApplicationInfo> applicationInfos = pm
 40                 .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
 41         // 排序
 42         Collections.sort(applicationInfos,
 43                 new ApplicationInfo.DisplayNameComparator(pm));
 44 
 45         switch (type) {
 46         case FILTER_ALL_APP:// 所有应用
 47             for (ApplicationInfo applicationInfo : applicationInfos) {
 48                 getAppInfo(applicationInfo);
 49             }
 50             break;
 51         case FILTER_SYSTEM_APP:// 系统应用
 52             for (ApplicationInfo applicationInfo : applicationInfos) {
 53                 if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
 54                     getAppInfo(applicationInfo);
 55                 }
 56             }
 57         case FILTER_THIRD_APP:// 第三方应用
 58 
 59             for (ApplicationInfo applicationInfo : applicationInfos) {
 60                 // 非系统应用
 61                 if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
 62                     getAppInfo(applicationInfo);
 63                 }
 64                 // 系统应用，但更新后变成不是系统应用了
 65                 else if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
 66                     getAppInfo(applicationInfo);
 67                 }
 68             }
 69         case FILTER_SDCARD_APP:// SDCard应用
 70             for (ApplicationInfo applicationInfo : applicationInfos) {
 71                 if (applicationInfo.flags == ApplicationInfo.FLAG_SYSTEM) {
 72                     getAppInfo(applicationInfo);
 73                 }
 74             }
 75         default:
 76             break;
 77         }
 78     }
 79 
 80     /**
 81      * 获取应用信息
 82      */
 83     private void getAppInfo(ApplicationInfo applicationInfo) {
 84         String appName = applicationInfo.loadLabel(pm).toString();// 应用名
 85         String packageName = applicationInfo.packageName;// 包名
 86         System.out.println("应用名：" + appName + " 包名：" + packageName);
 87     }
 88 
 89     @Override
 90     public void onClick(View arg0) {
 91         switch (arg0.getId()) {
 92         case R.id.btn_all:
 93             System.out.println("输出所有应用信息:\n");
 94             filterApp(FILTER_ALL_APP);
 95             break;
 96         case R.id.btn_system:
 97             System.out.println("输出系统应用信息:\n");
 98             filterApp(FILTER_SYSTEM_APP);
 99             break;
100         case R.id.btn_third:
101             System.out.println("输出第三方应用信息:\n");
102             filterApp(FILTER_THIRD_APP);
103             break;
104         case R.id.btn_sdcard:
105             System.out.println("输出SDCard应用信息:\n");
106             filterApp(FILTER_SDCARD_APP);
107             break;
108 
109         default:
110             break;
111         }
112     }
113 
114 }
```

activity_main.xml

```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" >

    <Button
        android:id="@+id/btn_all"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="所有应用" />

    <Button
        android:id="@+id/btn_system"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="系统应用" />

    <Button
        android:id="@+id/btn_third"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="第三方应用" />

    <Button
        android:id="@+id/btn_sdcard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="SDCard应用" />

</LinearLayout>
```

# 参考文章

1. [Android随笔之——PackageManager详解](https://www.cnblogs.com/travellife/p/3932823.html)

