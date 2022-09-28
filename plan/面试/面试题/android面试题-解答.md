# 安卓面试题-解答

## 1.Android 四大组件相关

### 1.1.Activity 与 Fragment 之间常见的几种通信方式？

- [Activity 与 Fragment 之间常见的几种通信方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/2)

  1、对于Activity和Fragment之间的相互调用
  （1）Activity调用Fragment
  直接调用就好，Activity一般持有Fragment实例，或者通过Fragment id 或者tag获取到Fragment实例
  （2）Fragment调用Activity
  通过activity设置监听器到Fragment进行回调，或者是直接在fragment直接getActivity获取到activity实例
  2、Activity如果更好的传递参数给Fragment
  如果直接通过普通方法的调用传递参数的话，那么在fragment回收后恢复不能恢复这些数据。google给我们提供了一个方法 setArguments(bundle) 可以通过这个方法传递参数给fragment，然后在fragment中用getArguments获取到。能保证在fragment销毁重建后还能获取到数据

  viewModel 做数据管理，activity 和 fragment 公用同个viewModel 实现数据传递

### 1.2.大家是怎么处理 activity 会 fragment 重建后数据恢复问题的？

可以使用 setRetaininstance 方法

此方法可以有效地提高系统的运行效率，对流畅性要求较高的应用可以适当采用此方法进行设置。

Fragment 有一个强大的功能，可以在 Activity 重新创建时可以不完全销毁 Fragment，以便 Fragment 可以恢复。在 onCreate() 方法中调用 setRetainInstance(true/false) 方法是最佳位置。

　　当在 onCreate() 方法中调用了 setRetainInstance(true) 后，Fragment 恢复时会跳过 onCreate() 和 onDestory() 方法，因此不能在 onCreate() 中放置一些初始化逻辑。

### 1.3.谈谈 android 中几种 launchmode 的特定和应用场景？

- [谈谈 Android 中几种 LaunchMode 的特点和应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/4)

Standrad：标准模式，打开一个 activity 就新建一个 activity，没有特殊需求就用标准模式。

Singleton:栈顶复用模式，打开一个 activity，如果这个 activity 就在栈顶，则复用，如果没有在栈顶，则新建入栈。重复打开界面可以用栈顶复用模式，像是新闻详情页。

Singletask：栈内复用模式，打开一个 activity，如果栈内有这个 activity，则让栈内 activity 之上的 activity 都出栈，复用栈内的 activity，功能的入口页面适合用。

Single instance：单例模式，栈内只有这一个 activity，适用于单独功能，像是相机、空间清理等。

### 1.4.Broadcast 与 LocalBroadcastReceiver 有什么区别？

- [BroadcastReceiver 与 LocalBroadcastReceiver 有什么区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/11)

  BroadcastReceiver是针对应用间、应用与系统间、应用内部进行通信的一种方式
  LocalBroadcastReceiver仅在自己的应用内发送接收广播，也就是只有自己的应用能收到，数据更加安全广播只在这个程序里，而且效率更高。

  broadcastreceiver 是跨应用广播，利用 binder 机制实现，支持动态和静态两种方式注册。

  localbroadcastreceiver 是应用内广播，利用 handler 实现，利用了 intentfilter 的 match 功能，提供消息的发布与接收功能，实现应用内通信，效率和安全性比较高，仅支持动态注册。

  特别注意：
  1.如果BroadcastReceiver在onReceiver（）方法中在10秒内没有执行完成，会造成ANR异常。
  2.对于不同注册方式的广播接收者回调方法onReceive（）返回的Context是不一样的。
  静态注册：context为ReceiverRestrictedContext。
  动态注册：context为Activity的Context。
  LocalBroadcastManager的动态注册：context为Application的Context。

### 1.5.对于 context，你了解多少？

- [对于 Context，你了解多少?](https://github.com/Moosphan/Android-Daily-Interview/issues/14)

  `Context` 宏观来说是一个描述应用程序全局信息的场景，当然，本质上来说，这个“场景”其实是一个抽象类，它是一个应用程序的“灵魂人物”，从下面的图中就可以发现 `Activity`、`Service`、`Application` 都是 `Context` 的子类：

  ![](https://user-images.githubusercontent.com/31280310/55070252-d2ace580-50c0-11e9-9dbb-0064e03da3d8.png)

  Context 虽然无所不在，但是某些特定场景下需要使用特定的 `Context`，如启动一个弹窗，必须是依赖于 `Activity` 的，所以，使用 `Dialog` 必须传入当前 `Activity` 场景下的 “context”，关于 `Context` 的作用域可以参考下图：

  ![](https://user-images.githubusercontent.com/31280310/55070060-6336f600-50c0-11e9-8ebd-3fcee3f4d9f2.png)

  1.Context是个抽象类 Activity跟Application跟Service都间接继承于他
  2.Context简称上下文 。Android不像java程序一样，随便创建一个类写个main方法就能跑了，它需要有一个完整的上下文环境，即context，像启动activity/broadcast receiver /service 都需要用到他。
  3.context导致的内存泄漏多是长生命周期去持有了短生命周期的实例造成的。像工具类如果需要context的话，能传applicationContext 最好传它。
  4.getApplication()跟getApplicationContext() 本质没有区别 后者就是把返回的application强转成context传回来而已。返回的都是同一个对象，只是前者只能在Activity跟Service中可以使用，作用域不同。
  5.Context数量 = Activity数量 + Service数量 + 1 如果多个进程的话 后面就不是+1 而是加多个
  6.ContextWrapper里面有一个attachBaseContext()方法，目的是将Context参数传给mBase，之后的调用系统方法如getPackageName()之类的都是委托mBase实例来做的，所以在调用这个方法之前 是无法调用系统方法的。
  构造函数-->attachBaseContext()-->onCreate()
  。所以在onCreate()中或者重写这个方法后调用都可以。

  ```java
      @Override
  	protected void attachBaseContext(Context base) {
  		// 在这里调用Context的方法会崩溃
  		super.attachBaseContext(base);
  		// 在这里可以正常调用Context的方法
  	}
  ```

### 1.6. IntentFilter 是什么？有哪些使用场景？匹配机制是怎么样的？

- [IntentFilter 是什么？有哪些使用场景？匹配机制是怎样的？](https://github.com/Moosphan/Android-Daily-Interview/issues/26)

  隐式调用需要 Intent 能够匹配目标组件的 IntentFilter 中所设置的过滤信息，如果匹配不成功就不能启动目标 Actiivty。

  IntentFilter 中过滤的信息包括：action、category、data。

  　　关于 IntentFilter 的一些描述：

  * 匹配过滤列表时需要同时匹配过滤列表中的 action、category、data。
  * 一个 Activity 中可以有多组 intent-filter。
  * 一个 intent-filter 可以有多个 action、category、data，并各自构成不同类别，一个 Intent 必须同时匹配 action 类别、category 类别和 data 类别才算完全匹配。
  * 一个 Intent 只要能匹配任何一组 intent-filter 就算匹配成功。

### 1.7.谈一谈 startService 和 bindService 方法的区别，生命周期以及使用场景？

- [谈一谈 startService 和 bindService 方法的区别，生命周期以及使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/53)

startService 是启动服务，没有办法与 service 进行通信，bindService 是绑定服务，可以与 service 进行通信。

startservice 的生命周期:oncreate,onstartcommend,ondestory

Bindservice 的生命周期：oncreate,onbind,unbind,ondestory.

使用场景：不需要交换数据就使用 startservice，需要交换数据则使用bindservice.

### 1.8.service 是如何保活的

- [Service 如何进行保活？](https://github.com/Moosphan/Android-Daily-Interview/issues/98)

  进程划分为：前台服务、可见服务、后台服务、空服务，优先级依次下降。

  保活可以通过1.提供优先级，将服务设置为前台服务；2.使用 jobservice 定时触发任务，判断进程是否存活，不存活了，就拉起；3.双进程守护模式保活，两个进程互相监视，一旦有一个进程死了，另一个进程监听到就拉起；4.利用广播对服务进行唤醒。

### 1.9.简单介绍下 contentprovider 是如何实现数据共享的？ 

- [简单介绍下 ContentProvider 是如何实现数据共享的？](https://github.com/Moosphan/Android-Daily-Interview/issues/100)

  contentprovider 提供数据，contentresolver 获取数据，contentresolver 会通过调用 activitymanagerservice 获取到 contentprovider 信息，从而调用到 contentprovider 的 query 等方法。

  ContentProvider（内容提供者）：对外提供了统一的访问数据的接口。
  ContentResolver（内容解析者）：通过URI的不同来操作不同的ContentProvider中的数据。
  ContentObserver（内容观察者）：观察特定URI引起的数据库的变化。通过ContentResolver进行注册，观察数据是否发生变化及时通知刷新页面（通过Handler通知主线程更新UI）。

### 1.10.说下切换横竖屏时 activity 的生命周期变化？

- [说下切换横竖屏时 Activity 的生命周期变化？](https://github.com/Moosphan/Android-Daily-Interview/issues/115)

  1. AndroidManifest没有设置configChanges属性
     竖屏启动：

  onCreate -->onStart-->onResume

  切换横屏：

  onPause -->onSaveInstanceState -->onStop -->onDestroy -->onCreate-->onStart -->

  onRestoreInstanceState-->onResume -->onPause -->onStop -->onDestroy

  总结：没有设置configChanges属性Android 6.0 7.0 8.0 系统手机 表现都是一样的，当前的界面调用onSaveInstanceState走一遍流程，然后重启调用onRestoreInstanceState再走一遍完整流程，最终destory。

  2.AndroidManifest设置了configChanges android:configChanges="orientation" 只设置了 orientation ，android 6.0 同没有设置是一样的，Android 7.0 会先回调 onconfigurationchanged 方法，后面和原来一样走销毁后重建的生命周期，android  8.0 回调了 onconfigurationchanged 方法
竖屏启动：
  
onCreate -->onStart-->onResume
  
切换横屏：
  
onPause -->onSaveInstanceState -->onStop -->onDestroy -->onCreate-->onStart -->
  
onRestoreInstanceState-->onResume -->onPause -->onStop -->onDestroy
  
（Android 6.0）
  
onConfigurationChanged-->onPause -->onSaveInstanceState -->onStop -->onDestroy -->
  onCreate-->onStart -->onRestoreInstanceState-->onResume -->onPause -->onStop -->onDestroy
  
（Android 7.0）
  
onConfigurationChanged
  
（Android 8.0）
  
总结：设置了configChanges属性为orientation之后，Android6.0 同没有设置configChanges情况相同，完整的走完了两个生命周期，调用了onSaveInstanceState和onRestoreInstanceState方法；Android 7.0则会先回调onConfigurationChanged方法，剩下的流程跟Android 6.0 保持一致；Android 8.0 系统更是简单，只是回调了onConfigurationChanged方法，并没有走Activity的生命周期方法。
  
4.AndroidManifest设置了configChanges
  android:configChanges="orientation|screenSize"
竖(横)屏启动：onCreate -->onStart-->onResume
  切换横(竖)屏：onConfigurationChanged （Android 6.0 Android 7.0 Android 8.0）
  
总结：没有了keyboardHidden跟3是相同的，orientation代表横竖屏切换 screenSize代表屏幕大小发生了改变，
  设置了这两项就不会回调Activity的生命周期的方法，只会回调onConfigurationChanged 。

  5.AndroidManifest设置了configChanges
android:configChanges="orientation|keyboardHidden"
  
总结：跟只设置了orientation 属性相同，Android6.0 Android7.0会回调生命周期的方法，Android8.0则只回调onConfigurationChanged。说明如果设置了orientation 和 screenSize 都不会走生命周期的方法，keyboardHidden不影响。
  
1.不设置configChanges属性不会回调onConfigurationChanged，且切屏的时候会回调生命周期方法。
  2.只有设置了orientation 和 screenSize 才会保证都不会走生命周期，且切屏只回调onConfigurationChanged。
3.设置 orientation，没有设置 screenSize，切屏会回调onConfigurationChanged，但是还会走生命周期方法。
  
注：这里只选择了Android部分系统的手机做测试，由于不同系统的手机品牌也不相同，可能略微会有区别。
  
另：
  代码动态设置横竖屏状态（onConfigurationChanged当屏幕发生变化的时候回调）
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

  获取屏幕状态（int ORIENTATION_PORTRAIT = 1; 竖屏 int ORIENTATION_LANDSCAPE = 2; 横屏）
  int screenNum = getResources().getConfiguration().orientation;

  configChanges属性

  1. orientation 屏幕在纵向和横向间旋转
   2.keyboardHidden 键盘显示或隐藏
     3.screenSize 屏幕大小改变了
   4.fontScale 用户变更了首选的字体大小
     5.locale 用户选择了不同的语言设定
     6.keyboard 键盘类型变更，例如手机从12键盘切换到全键盘
   7.touchscreen或navigation 键盘或导航方式变化，一般不会发生这样的事件
     常用的包括：orientation keyboardHidden screenSize，设置这三项界面不会走Activity的生命周期，只会回调onConfigurationChanged方法。
  
  screenOrientation属性 1.unspecified 默认值，由系统判断状态自动切换 2.landscape 横屏 3. portrait 竖屏 4.user 用户当前设置的orientation值 5. behind 下一个要显示的Activity的orientation值 6. sensor 使用传感器 传感器的方向 7. nosensor 不使用传感器 基本等同于unspecified 仅landscape和portrait常用，代表界面默认是横屏或者竖屏，还可以再代码中更改。

### 1.11.activity 中 onnewintent 方法的调用实际和使用场景？

- [Activity 中 onNewIntent 方法的调用时机和使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/138)

栈内的 intent 内复用，就会调用 onnewintent，如果使用 singleton，singletask，singleinstance 等模式，在 onnewintent 中接收新的 intent 携带的数据刷新布局。

onNewIntent的调用时机：只要该Activity实例在栈中存在，每次复用Activity的时候，都会调用onNewIntent，而不会重新创建Activity实例。

onNewIntent的调用时机：只要该Activity实例在栈中存在，每次复用Activity的时候，都会调用onNewIntent，而不会重新创建Activity实例。
singleTop、singleTask、singleInstance模式下都会调用onNewIntent()。
调用onNewIntent()生命周期如下：onNewIntent()->onRestart()->onStart()->onResume()。
注意：在onNewIntent()中一定要设置setIntent(intent)，否则getIntent()时获取到的是旧的intent，而不是新的intent。

Activity 的 onNewIntent方法的调用可总结如下:

　　在该Activity的实例已经存在于Task和Back stack中(或者通俗的说可以通过按返回键返回到该Activity )时,当使用intent来再次启动该Activity的时候,如果此次启动不创建该Activity的新实例,则系统会调用原有实例的onNewIntent()方法来处理此intent.

　　且在下面情况下系统不会创建该Activity的新实例:

　　1,如果该Activity在Manifest中的android:launchMode定义为singleTask或者singleInstance.

　　2,如果该Activity在Manifest中的android:launchMode定义为singleTop且该实例位于Back stack的栈顶.

　　3,如果该Activity在Manifest中的android:launchMode定义为singleTop,且上述intent包含Intent.FLAG_ACTIVITY_CLEAR_TOP标志.

　　4,如果上述intent中包含 Intent.FLAG_ACTIVITY_CLEAR_TOP 标志和且包含 Intent.FLAG_ACTIVITY_SINGLE_TOP 标志.

　　5,如果上述intent中包含 Intent.FLAG_ACTIVITY_SINGLE_TOP 标志且该实例位于Back stack的栈顶.

　　上述情况满足其一,则系统将不会创建该Activity的新实例.

　　根据现有实例所处的状态不同onNewIntent()方法的调用时机也不同,总的说如果系统调用onNewIntent()方法则系统会在onResume()方法执行之前调用它.这也是官方API为什么只说"you can count on onResume() being called after this method",而不具体说明调用时机的原因.

### 1.12.intent 传输数据的大小有限制吗？如何解决？

- [Intent 传输数据的大小有限制吗？如何解决？](https://github.com/Moosphan/Android-Daily-Interview/issues/151)

  Intent传输数据的大小受Binder的限制，上限是1M。不过这个1M并不是安全的上限，Binder可能在处理别的工作，安全上限是多少这个在不同的机型上也不一样。

  传 512K 以下的数据的数据可以正常传递。
  传 512K~1024K 的数据有可能会出错，闪退。
  传 1M以上的数据会报错：TransactionTooLargeException
  考虑到 Intent 还包括要启动的 Activity 等信息，实际可以传的数据略小于 512K

  解决办法

  1. 减少传输数据量
  2. Intent通过绑定一个Bundle来传输，这个可以超过1M，不过也不能过大
  3. 通过内存共享，使用静态变量或者使用EventBus等类似的通信工具
  4. 通过文件共享

### 1.13.说说 content provider、content resolver、contentobserver 之间的关系？

- [说说 ContentProvider、ContentResolver、ContentObserver 之间的关系？](https://github.com/Moosphan/Android-Daily-Interview/issues/155)

  ContentProvider：内容提供者, 用于对外提供数据,比如联系人应用中就是用了ContentProvider,
  一个应用可以实现ContentProvider来提供给别的应用操作,通过ContentResolver来操作别的应用数据

  ContentResolver：内容解析者, 用于获取内容提供者提供的数据
  ContentResolver.notifyChange(uri)发出消息

  ContentObserver：内容监听者,可以监听数据的改变状态，观察(捕捉)特定的Uri引起的数据库的变化
  ContentResolver.registerContentObserver()：监听消息

  概括:
  使用ContentResolver来获取ContentProvider提供的数据, 同时注册ContentObserver监听数据的变化

### 1.14.说说 activity 加载的流程

- [说说 Activity 加载的流程？](https://github.com/Moosphan/Android-Daily-Interview/issues/168)

  App 启动流程（基于Android8.0）：

  - 点击桌面 App 图标，Launcher 进程采用 Binder IPC（具体为ActivityManager.getService 获取 AMS 实例） 向 system_server 的 AMS 发起 startActivity 请求
  - system_server 进程收到请求后，向 Zygote 进程发送创建进程的请求；
  - Zygote 进程 fork 出新的子进程，即 App 进程
  - App 进程创建即初始化 ActivityThread，然后通过 Binder IPC 向 system_server 进程的 AMS 发起 attachApplication 请求
  - system_server 进程的 AMS 在收到 attachApplication 请求后，做一系列操作后，通知 ApplicationThread bindApplication，然后发送 H.BIND_APPLICATION 消息
  - 主线程收到 H.BIND_APPLICATION 消息，调用 handleBindApplication 处理后做一系列的初始化操作，初始化 Application 等
  - system_server 进程的 AMS 在 bindApplication 后，会调用 ActivityStackSupervisor.attachApplicationLocked，之后经过一系列操作，在 realStartActivityLocked 方法通过 Binder IPC 向 App 进程发送 scheduleLaunchActivity 请求；
  - App进程的 binder 线程（ApplicationThread）在收到请求后，通过 handler 向主线程发送 LAUNCH_ACTIVITY 消息；
  - 主线程收到 message 后经过 handleLaunchActivity，performLaunchActivity 方法，然后通过反射机制创建目标 Activity；
  - 通过 Activity attach 方法创建 window 并且和 Activity 关联，然后设置 WindowManager 用来管理 window，然后通知 Activity 已创建，即调用 onCreate
  - 然后调用 handleResumeActivity，Activity 可见

  附图：
  [![img](https://github.com/huazidev/Android-Notes/blob/master/image/app-launch.jpg?raw=true)](https://github.com/huazidev/Android-Notes/blob/master/image/app-launch.jpg)

  补充：

  - ActivityManagerService 是一个注册到 SystemServer 进程并实现了 IActivityManager 的 Binder，可以通过 ActivityManager 的 getService 方法获取 AMS 的代理对象，进而调用 AMS 方法
  - ApplicationThread 是 ActivityThread 的内部类，是一个实现了 IApplicationThread 的 Binder。AMS通过 Binder IPC 经 ApplicationThread 对应用进行控制
  - 普通的 Activity 启动和本流程差不多，至少不需要再创建 App 进程了
  - Activity A 启动 Activity B，A 先 pause 然后 B 才能 resume，因此在 onPause 中不能做耗时操作，不然会影响下一个 Activity 的启动

## 2. Android 异步任务和消息机制

### 2.1. handlerThread 的使用场景和实现原理？

- [HandlerThread 的使用场景和实现原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/37)

**HandlerThread** 是 Android 封装的一个线程类，将 Thread 跟 Handler 封装。使用步骤如下：

1. 创建 `HandlerThread` 实例对象

```java
    HandlerThread mHandlerThread = new HandlerThread("mHandlerThread");
```

2. 启动线程

```java
    mHandlerThread .start();
```

3. 创建 Handler 对象，重写 handleMessage 方法

```java
    Handler mHandler= new Handler( mHandlerThread.getLooper() ) {
           @Override
           public boolean handleMessage(Message msg) {
               //消息处理
               return true;
           }
     });
```

4. 使用工作线程Handler向工作线程的消息队列发送消息:

```java
     Message  message = Message.obtain();
     message.what = “2”
     message.obj = "骚风"
     mHandler.sendMessage(message);
```

5. 结束线程，即停止线程的消息循环

```java
     mHandlerThread.quit()；
```

HandlerThread=Handler+Thread，是一个非常方便的封装类，要注意的是 HandlerThread 不会自动停止，必须手动调用 quit() 方法。

关于使用场景，我一般都是开一个HandlerThread当全局的工作线程处理那些杂物（而且我会设置为守护线程，同时把优先级调成背景线程），比如不重要的文件读写操作直接扔给这个线程，免得卡主线程。至于怎么用楼上已经说的很清楚了，不再赘述。

HandlerThread 其实是 Handler+thread，在内部的实现方法中就是通过获取 thread 的 looper，来初始化 Handler，HandlerThread 的使用可以参考 IntentService。

HandlerThread 继承于 Thread 是一个线程类，在 run 方法中通过 Looper.prepare 来创建消息队列并通过 Looper.loop() 来开启消息循环，通过创建HandlerThread 来启动一个子线程的 Handler 来做一些耗时操作，比如数据查询，完成后可创建一个 UI 线程的 Handle 来做界面更新。

HandlerThread 的 run 方法中会创建 looper 对象：

```java
    @Override
    public void run() {
        mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        onLooperPrepared();
        Looper.loop();
        mTid = -1;
    }
```

HandlerThread 就相当于封装了 thread 和 handler，让子线程也可以通过 handler 传递消息。

### 2.2.主线程与子线程通信

 主线程与子线程通信 https://www.jb51.net/article/101974.htm

使用 handler 子线程与主线程通信。

主线程发消息到子线程：

```java
package com.zhuozhuo;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class LooperThreadActivity extends Activity{
 /** Called when the activity is first created. */
 
 private final int MSG_HELLO = 0;
 private Handler mHandler;
 
 @Override
 public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);
  new CustomThread().start();//新建并启动CustomThread实例
  
  findViewById(R.id.send_btn).setOnClickListener(new OnClickListener() {
   
   @Override
   public void onClick(View v) {//点击界面时发送消息
    String str = "hello";
    Log.d("Test", "MainThread is ready to send msg:" + str);
    mHandler.obtainMessage(MSG_HELLO, str).sendToTarget();//发送消息到CustomThread实例
    
   }
  });
  
 }
 
 
 
 
 
 class CustomThread extends Thread {
  @Override
  public void run() {
   //建立消息循环的步骤
   Looper.prepare();//1、初始化Looper
   mHandler = new Handler(){//2、绑定handler到CustomThread实例的Looper对象
    public void handleMessage (Message msg) {//3、定义处理消息的方法
     switch(msg.what) {
     case MSG_HELLO:
      Log.d("Test", "CustomThread receive msg:" + (String) msg.obj);
     }
    }
   };
   Looper.loop();//4、启动消息循环
  }
 }
}
```

### 2.3. intentservice 的应用场景和内部实现原理？

- [IntentService 的应用场景和内部实现原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/25)

https://blog.csdn.net/weixin_42625019/article/details/91422098

**`IntentService`** 是 `Service` 的子类，默认为我们开启了一个工作线程，使用这个工作线程逐一处理所有启动请求，在任务执行完毕后会自动停止服务，使用简单，只要实现一个方法 `onHandleIntent`，该方法会接收每个启动请求的 `Intent`，能够执行后台工作和耗时操作。可以启动 `IntentService` 多次，而每一个耗时操作会以队列的方式在 IntentService 的 `onHandlerIntent` 回调方法中执行，并且，每一次只会执行一个工作线程，执行完第一个再执行第二个。并且等待所有消息都执行完后才终止服务。

**`IntentService`** 适用于 APP 在不影响当前用户的操作的前提下，在后台默默的做一些操作。

IntentService源码：

1. 通过 `HandlerThread` 单独开启一个名为 `IntentService` 的线程
2. 创建一个名叫 `ServiceHandler` 的内部 `Handler`
3. 把内部Handler与HandlerThread所对应的子线程进行绑定
4. 通过 `onStartCommand()` 传递给服务 `intent`，依次插入到工作队列中，并逐个发送给 `onHandleIntent()`
5. 通过 `onHandleIntent()` 来依次处理所有 `Intent` 请求对象所对应的任务

使用示例：

```java
public class MyIntentService extends IntentService {
    public static final String TAG ="MyIntentService";
    public MyIntentService() {
        super("MyIntentService");
    }
 
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
 
       boolean isMainThread =  Thread.currentThread() == Looper.getMainLooper().getThread();
        Log.i(TAG,"is main thread:"+isMainThread); // 这里会打印false，说明不是主线程
 
        // 模拟耗时操作
        download();
    }
 
    /**
     * 模拟执行下载
     */
    private void download(){
       try {
           Thread.sleep(5000);
           Log.i(TAG,"下载完成...");
       }catch (Exception e){
           e.printStackTrace();
       }
    }
}
```

源码：

```java
public abstract class IntentService extends Service {
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
          onHandleIntent((Intent)msg.obj); // 收到消息调用 onHandleIntent() 抽象方法
          stopSelf(msg.arg1);
        }
    }

    public IntentService(String name) {
        super();
        mName = name;
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]"); // 创建 HandlerThread 对象
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg); // onstart() 就是发送消息
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        onStart(intent, startId); // 发送消息
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit(); // 退出 loop 的循环
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    @WorkerThread
    protected abstract void onHandleIntent(@Nullable Intent intent); // 处理消息的抽象方法
}

```

IntentService是一个存在单独工作线程，onHandleIntent()在工作线程中执行，执行完毕自动销毁的Service，
使用场景是工作量较大，但具体任务明确的Service
使用姿势：首先创建一个类继承IntentService，重写onHandleIntent(Intent)方法，在清单文件中注册，然后使用context.startService等方法启动服务。

stopSelf的源码中根据startId最后找到AMS.stopServiceToken->ActiveServices.stopServiceTokenLocked，看到源码注释，原来只在完成所有任务后才会停止服务，它会根据startId来判断任务是否还在进行中。

### 2.4.AsyncTask 的优点和缺点？内部实现原理是怎么样的？

- [AsyncTask 的优点和缺点？内部实现原理是怎样的？](https://github.com/Moosphan/Android-Daily-Interview/issues/28)

Asynctask是一个抽象类，它是Android封装的一个轻量级异步类（轻量级体现在使用方便，代码简洁）,它可以在线程池中执行后台任务，然后把执行的进度和最终的结果呈现给主线程，并且更新UI。

AsyncTask 的简单使用：

```java
public class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            while (true) {
                int downloadPercent = doDownload();
                publishProgress(downloadPercent);
                if (downloadPercent >= 100) {
                    break;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog.setMessage("当前下载进度： " + values[0] + "%");
    }
    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result) {
            Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
        }
    }
}
```

这里模拟了一个下载任务，在doInBackground方法中，去执行具体的下载逻辑，在onProgressUpdate()方法中显示下载的进度。在onPostExecute()方法中提示下载任务的结果。
 调用方法：

```java
  new DownloadTask().execute();
```

优点：AsyncTask操作简单，轻便，适用简单的异步操作。
缺点：存在新开大量线程，消耗系统资源的风险

优点：使用方便，既可以执行串行任务，也可以执行并行任务
缺点：默认使用串行任务执行效率低，不能充分利用多线程加快执行速度；如果使用并行任务执行，在任务特别多的时候会阻塞UI线程获得CPU时间片，后续做线程收敛需要自定义AsynTask，将其设置为全局统一的线程池，改动量比较大

优点：方便
缺点：不可控制

AsyncTask的实现原理：
1.AsyncTask是一个抽象类，主要由Handler+2个线程池构成，SERIAL_EXECUTOR是任务队列线程池，用于调度任务，按顺序排列执行，THREAD_POOL_EXECUTOR是执行线程池，真正执行具体的线程任务。Handler用于工作线程和主线程的异步通信。

2.AsyncTask<Params，Progress，Result>，其中Params是doInBackground()方法的参数类型，Result是doInBackground()方法的返回值类型，Progress是onProgressUpdate()方法的参数类型。

3.当执行execute()方法的时候，其实就是调用SERIAL_EXECUTOR的execute()方法，就是把任务添加到队列的尾部，然后从头开始取出队列中的任务，调用THREAD_POOL_EXECUTOR的execute()方法依次执行，当队列中没有任务时就停止。

4.AsyncTask只能执行一次execute(params)方法，否则会报错。但是SERIAL_EXECUTOR和THREAD_POOL_EXECUTOR线程池都是静态的，所以可以形成队列。

Q：AsyncTask只能执行一次execute()方法，那么为什么用线程池队列管理 ？
因为SERIAL_EXECUTOR和THREAD_POOL_EXECUTOR线程池都是静态的，所有的AsyncTask实例都共享这2个线程池，因此形成了队列。

Q：AsyncTask的onPreExecute()、doInBackground()、onPostExecute()方法的调用流程？
AsyncTask在创建对象的时候，会在构造函数中创建mWorker(workerRunnable)和mFuture(FutureTask)对象。
mWorker实现了Callable接口的call()方法，在call()方法中，调用了doInBackground()方法，并在最后调用了postResult()方法，也就是通过Handler发送消息给主线程，在主线程中调用AsyncTask的finish()方法，决定是调用onCancelled()还是onPostExecute().
mFuture实现了Runnable和Future接口，在创建对象时，初始化成员变量mWorker，在run()方法中，调用mWorker的call()方法。
当asyncTask执行execute()方法的时候，会先调用onPreExecute()方法，然后调用SERIAL_EXECUTOR的execute(mFuture)，把任务加入到队列的尾部等待执行。执行的时候调用THREAD_POOL_EXECUTOR的execute(mFuture).

### 2.5.谈谈你对 activity.runOnUiThread 的理解？

- [谈谈你对 Activity.runOnUiThread 的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/49)



- [Android 的子线程能否做到更新 UI？](https://github.com/Moosphan/Android-Daily-Interview/issues/91)

  

- [谈谈 Android 中消息机制和原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/9)

  Loop 中会有一个消息队列，调用 loop 方法后会从消息队列中循环获取消息。

  在 handler 对象中会有从当前线程的中取出 looper 对象，在发送消息时，会将消息放到消息队列中。

  Loop 方法会从消息队列中获取消息，然后处理消息。

- [为什么在子线程中创建 Handler 会抛异常？](https://github.com/Moosphan/Android-Daily-Interview/issues/93)

  在线程中创建 handler 的时候会从当前线程的中取出 looper 对象，没有取到 looper 对象，所以会抛异常。

- [试从源码角度分析 Handler 的 post 和 sendMessage 方法的区别和应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/239)

  区别：

  1. 使用方法不同

  2. 消息处理不同

     分发消息时会先判断 mg.callback 是否为空，如果不为空，则处理消息，而 msg.callback 就是 post 方法的 runnable，处理消息就是调用 runnable 的run 方法，接着是判断 handler 的 callback 是否为空，调用 callback 的 handlerMessage 方法，最后时调用 handler 的 handlemessage 方法。

  post一般用于单个场景 比如单一的倒计时弹框功能 sendMessage的回调需要去实现handleMessage Message则做为参数 用于多判断条件的场景 比如3个按钮 实现不同的效果

- [Handler 中有 Loop 死循环，为什么没有阻塞主线程，原理是什么？](https://github.com/Moosphan/Android-Daily-Interview/issues/170)

  handler 机制是使用 pipe 来实现的，主线程没有消息处理时会阻塞在管道的读端。

  　　binder 线程会往主线程消息队列里添加消息，然后往管道写端写一个字段，这样就能唤醒主线程从管道读端返回，也就是说 queue.next() 会调用返回。

  　　主线程大多数都是出于休眠状态，并不会消耗大量 CPU 资源。

## 4.Android UI 绘制相关

此类主要涵盖 Android 的 View 绘制过程、常见 UI 组件、自定义 View、动画等。

- [Android 补间动画和属性动画的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/7)

- [Window 和 DecorView 是什么？DecorView 又是如何和 Window 建立联系的?](https://github.com/Moosphan/Android-Daily-Interview/issues/13)

- [简述一下 Android 中 UI 的刷新机制？](https://github.com/Moosphan/Android-Daily-Interview/issues/17)

  

- [你认为 LinearLayout、FrameLayout 和 RelativeLayout 哪个效率高, 为什么？](https://github.com/Moosphan/Android-Daily-Interview/issues/30)

  

- [说一下 Android 中的事件分发机制？](https://github.com/Moosphan/Android-Daily-Interview/issues/35)

  显示调用 view 的 dispatchTouchEvent() 方法分发事件，先调用 viewe. ontouch 方法，如果 ontouch 返回为 true，则事件被消费，不再继续，如果为 false，则调用 ontouchevent 方法，调用 onclick 方法。

  Viewgboup 会多一个 dispatchtouchevent 方法，用来拦截事件，如果为 false，则允许事件继续向子 view 传递，如果为 true，则表示自己处理事件，事件不继续向子 view 传递。

- view group 如何找到点击的子 view？

- [谈谈自定义 View 的流程？](https://github.com/Moosphan/Android-Daily-Interview/issues/41)

  

- [有针对 RecyclerView 做过哪些优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/38)

  1. item 布局优化

  2. 刷新时可以指定 position 刷新

  3. 更新单个布局时可以使用 manager 获取到adapter 更新

  4. 取出多余的 setitemclick，将 setitemclick 放在 holder 使用，而不是在 bindview 中设置，导致快速滑动时创建很多对象

  5. 对比前后两个数据，只刷新数据变化的 item，避免调用 notify
  6. 使用 swapadapter 充分利用 rv 的缓存机制，与 setadapter 的不同之处在于 setadapter 会直接清空 rv 上的所有缓存，而 swapadapter 会将 rv 上的 holder 保存到 pool 上。

  

- [谈谈你是如何优化 ListView 的？](https://github.com/Moosphan/Android-Daily-Interview/issues/42)

  1. 复用 convertview

  2. 使用 holder 减少重复的 findviewbyid

  3. 分页加载

  4. 图片优化

- [谈一谈自定义 RecyclerView.LayoutManager 的流程？](https://github.com/Moosphan/Android-Daily-Interview/issues/47)

  

- [什么是 RemoteViews？使用场景有哪些？](https://github.com/Moosphan/Android-Daily-Interview/issues/62)

- [谈一谈获取View宽高的几种方法？](https://github.com/Moosphan/Android-Daily-Interview/issues/131)

  getmeasureWidth：在 onmeasure 方法之后就可以拿到 view 的宽高。

  getwidth 在 layout 之后才可以拿到 view 的宽高。

- [View.post() 为什么可以获取到宽高信息？](https://github.com/Moosphan/Android-Daily-Interview/issues/126)

- [谈一谈属性动画的插值器和估值器？](https://github.com/Moosphan/Android-Daily-Interview/issues/148)

- [getDimension、getDimensionPixelOffset 和 getDimensionPixelSize 三者的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/156)

  

- [请谈谈源码中 StaticLayout 的用法和应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/174)

- [有用过ConstraintLayout吗？它有哪些特点？](https://github.com/Moosphan/Android-Daily-Interview/issues/175)

  相对布局，布局扁平化，不会嵌套多层。

- [关于LayoutInflater，它是如何通过 inflate 方法获取到具体View的？](https://github.com/Moosphan/Android-Daily-Interview/issues/177)

- [谈一谈如何实现 Fragment 懒加载？](https://github.com/Moosphan/Android-Daily-Interview/issues/178)

  

- [谈谈 RecyclerView的缓存机制？](https://github.com/Moosphan/Android-Daily-Interview/issues/181)

  rv 拥有四层缓存：

  1. 屏幕内缓存：指在屏幕中显示的 viewholder，这些 viewholder 会缓存在 mattachedscrap、mchangedscrap 中
     * mchangedscrap 表示数据已经改变的 viewholder 列表
     * Mattachscrap 未与 rv 分离的 viewholder 列表
  2. 屏幕外缓存，当列表滑动出了屏幕时，viewholder 会被混存在 mcacheviews 
  3. 自定义缓存，可以自己实现 viewcacheextension 类实现自定义缓存
  4. pool 缓存，当 viewholder 缓存在 cacheviews 中超过 2 个时，就会添加到 mrecyclerpool 中。

- [请说说 View.inflate 和 LayoutInflater.inflate 的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/185)

- [请谈谈 invalidate() 和 postInvalidate() 方法的区别和应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/186)

- [谈一谈自定义View和ViewGroup的流程以及区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/191)

- [谈一谈 SurfaceView 与 TextureView 的使用场景和用法？](https://github.com/Moosphan/Android-Daily-Interview/issues/199)

- [谈一谈 RecyclerView.Adapter 的几种数据刷新方式有何不同？](https://github.com/Moosphan/Android-Daily-Interview/issues/202)

  

- [说说你对 Window 和 WindowManager 的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/203)

- [谈一谈 Activity、View 和 Window 三者的关系？](https://github.com/Moosphan/Android-Daily-Interview/issues/213)

- [有了解过WindowInsets吗？它有哪些应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/216)

- [Android 中 View 的几种位移方式的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/220)

  

- [为什么 ViewPager 嵌套 ViewPager，内部的 ViewPager 滚动没有被拦截？](https://github.com/Moosphan/Android-Daily-Interview/issues/235)

- [请谈谈 Fragment 的生命周期？](https://github.com/Moosphan/Android-Daily-Interview/issues/161)

  onattach,oncrease,oncreateview,onactivitycreate,onstart,onresume,onpause,onstop,ondestroyview,ondestory,ondeatch

- [请谈谈什么是同步屏障？](https://github.com/Moosphan/Android-Daily-Interview/issues/171)

  

- [有了解过 ViewDragHelper 的工作原理吗？](https://github.com/Moosphan/Android-Daily-Interview/issues/204)

  

- [谈一谈Android的屏幕刷新机制？](https://github.com/Moosphan/Android-Daily-Interview/issues/210)

  

## 5.Android 性能调优相关

- [谈谈你对Android性能优化方面的了解？](https://github.com/Moosphan/Android-Daily-Interview/issues/58)

- [一般什么情况下会导致内存泄漏问题？如何解决](https://github.com/Moosphan/Android-Daily-Interview/issues/3)

- [自定义 Handler 时如何有效地避免内存泄漏问题？](https://github.com/Moosphan/Android-Daily-Interview/issues/1)

- [哪些情况下会导致OOM问题？如何解决？](https://github.com/Moosphan/Android-Daily-Interview/issues/5)

- [ANR 出现的场景以及解决方案？](https://github.com/Moosphan/Android-Daily-Interview/issues/8)

- [谈谈 Android 中内存优化的方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/18)

- [谈谈布局优化的技巧？](https://github.com/Moosphan/Android-Daily-Interview/issues/95)

- [对于 Android 中图片资源的优化方案你知道哪些？](https://github.com/Moosphan/Android-Daily-Interview/issues/70)

- [Android Native Crash 问题如何分析定位？](https://github.com/Moosphan/Android-Daily-Interview/issues/88)

- [该如何给 Apk 瘦身？](https://github.com/Moosphan/Android-Daily-Interview/issues/80)

- [说一下你是如何优化 App 启动过程的？](https://github.com/Moosphan/Android-Daily-Interview/issues/85)

- [谈谈代码混淆的步骤？](https://github.com/Moosphan/Android-Daily-Interview/issues/109)

- [说说 App 的电量优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/102)

- [谈谈如何对 WebView 进行优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/123)

- [如何处理大图的加载？](https://github.com/Moosphan/Android-Daily-Interview/issues/111)

- [谈谈如何对网络请求进行优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/114)

- [请谈谈如何加载Bitmap并防止内存溢出？](https://github.com/Moosphan/Android-Daily-Interview/issues/207)

  使用 option 进行压缩

## 6.Android 中的 IPC

- [请回答一下 Android 中进程间通信有哪些方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/21)

  1. bundle ，activity\service\broadcast 
  2. 文件
  3. socket
  4. aidl
  5. contentprovider
  6. Messenger

- [请谈谈你对 Binder 机制的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/105)

  使用内存映射。

- [什么是 AIDL？它的使用场景是什么？](https://github.com/Moosphan/Android-Daily-Interview/issues/119)

  

## 7.Android 系统 SDK 相关

- [请简要谈谈 Android 系统的架构组成？](https://github.com/Moosphan/Android-Daily-Interview/issues/149)

  

- [SharedPreferences 是线程安全的吗？它的 commit 和 apply 方法有什么区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/15)

  是线程安全的，

  commit是同步写入，会返回执行结果，apply方法是异步写入，并不会返回执行结果；但是SharedPreferences文件的写入是全量写入，即使只是修改了其中一条key-value，也会执行全部的写入操作，因为SharedPreferences只能用于存储体积较小的数据，太大了就容易引发OOM，同时如果需要修改多条数据，必须使用Editor来一次性完成修改再提交。

  1. apply没有返回值而commit返回boolean表明修改是否提交成功
  2. apply是将修改数据原子提交到内存, 而后异步真正提交到硬件磁盘, 而commit是同步的提交到硬件磁盘，因此，在多个并发的提交commit的时候，他们会等待正在处理的commit保存到磁盘后在操作，从而降低了效率。而apply只是原子的提交到内容，后面有调用apply的函数的将会直接覆盖前面的内存数据，这样从一定程度上提高了很多效率。

  由于在一个进程中，sharedPreference是单实例，一般不会出现并发冲突，如果对提交的结果不关心的话，建议使用apply，当然需要确保提交成功且有后续操作的话，还是需要用commit的。

- [Serializable 和 Parcelable 有哪些区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/19)

  1. 都是安卓常用到的序列化对象
  2. 实现上，serilizable 简单，parcelable 复杂
  3. 性能上,paracelable 避 serializable 更适合 android

  两者最大的区别在于 存储媒介的不同，Serializable 使用 I/O 读写存储在硬盘上，而 Parcelable 是直接 在内存中读写。很明显，内存的读写速度通常大于 IO 读写，所以在 Android 中传递数据优先选择 Parcelable。

  Android中序列化有两种方式：Serializable以及Parcelable。其中Serializable是Java自带的，而Parcelable是安卓专有的。
  Seralizable相对Parcelable而言，好处就是非常简单，只需对需要序列化的类class执行就可以，不需要手动去处理序列化和反序列化的过程，所以常常用于网络请求数据处理，Activity之间传递值的使用。
  Parcelable是android特有的序列化API，它的出现是为了解决Serializable在序列化的过程中消耗资源严重的问题，但是因为本身使用需要手动处理序列化和反序列化过程，会与具体的代码绑定，使用较为繁琐，一般只获取内存数据的时候使用。

  Serializable是属于Java自带的，本质是使用了反射。序列化的过程比较慢，这种机制在序列化的时候会创建很多临时的对象，引起频繁的GC。Parcelable 是属于 Android 专用。不过不同于Serializable，Parcelable实现的原理是将一个完整的对象进行分解。而分解后的每一部分都是Intent所支持的数据类型。 如果在内存中使用建议Parcelable。持久化操作建议Serializable

  都是安卓常用到的序列化对象，Parcelable要手动写构造函数和writeToParcel，不过现在as都是自动生成的，Serializable是声明一下接口就行了。Parcelable比Serializable性能强，Serializable在使用是会产生大量临时变量，增加GC回收频率。但是Serializable的数据是以IO流在磁盘，而Parcelable是写在内存，所以Parcelable无法将数据更好的持久化。

- [请说一下 Android 7.0 的新特性？](https://github.com/Moosphan/Android-Daily-Interview/issues/31)

- [谈谈 ArrayMap 和 HashMap 的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/54)

  ArrayMap采用的是数组+数组的形式，一个数组存储key的hash值，一个数组存储value，所以如果数据较大时，效率相对较低；
  HashMap采用的是数组+链表/红黑树的形式，数组存放key的hash值，链表存放值，如果值数量超过8，会转成红黑树。

   1.查找效率
  HashMap因为其根据hashcode的值直接算出index,所以其查找效率是随着数组长度增大而增加的。
  ArrayMap使用的是二分法查找，所以当数组长度每增加一倍时，就需要多进行一次判断，效率下降

  2.扩容数量
  HashMap初始值16个长度，每次扩容的时候，直接申请双倍的数组空间。
  ArrayMap每次扩容的时候，如果size长度大于8时申请size*1.5个长度，大于4小于8时申请8个，小于4时申请4个。这样比较ArrayMap其实是申请了更少的内存空间，但是扩容的频率会更高。因此，如果数据量比较大的时候，还是使用HashMap更合适，因为其扩容的次数要比ArrayMap少很多。

  3.扩容效率
  HashMap每次扩容的时候重新计算每个数组成员的位置，然后放到新的位置。
  ArrayMap则是直接使用System.arraycopy，所以效率上肯定是ArrayMap更占优势。

  4.内存消耗
  以ArrayMap采用了一种独特的方式，能够重复的利用因为数据扩容而遗留下来的数组空间，方便下一个ArrayMap的使用。而HashMap没有这种设计。 由于ArrayMap之缓存了长度是4和8的时候，所以如果频繁的使用到Map，而且数据量都比较小的时候，ArrayMap无疑是相当的是节省内存的。

  5.总结
  综上所述，数据量比较小，并且需要频繁的使用Map存储数据的时候，推荐使用ArrayMap。 而数据量比较大的时候，则推荐使用HashMap。

  HashMap内部是使用一个默认容量为16的数组来存储数据的，而数组中每个元素却又是一个链表的头结点。所以，更准确的来说，HashMap内部存储结构是使用哈希表的拉链结构（数组+链表/红黑树），其中链表和红黑树是为了解决 hash 冲突而设计了。详细可参照[hashMap 思维导图](https://github.com/xianfeng92/Awsome-Android/blob/master/notes/DataStructure/hashMap_Intro.md)

  关注点： 1. 扩容时需要元素移动以及重 hash； 2. 加载因子值选择，以空间换时间； 3. hash 冲突时，使用链表或红黑树存储数据； 4. hash 算法的随机和均匀：扰动函数

  ArrayMap是一个<key,value>映射的数据结构，内部是使用两个数组进行数据存储，一个数组记录key的hash值。另外一个数组记录Value值。它会对key使用二分法进行从小到大排序，在加入、删除、查找数据的时候都是先使用二分查找法得到相应的index，然后通过index来进行加入、查找、删除等操作。

  

- [简要说说 LruCache 的原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/79)

  使用 linkedhashmap 实现，缓存超过最大值时，最近未使用的被清除。

  LruCache是使用一个LinkedHashMap简单内存的缓存，是强引用；没有软引用。如果添加的数据大于设置的数据，就删除最先缓存数据来调整内存数据。
  maxside数据是初始化的时候构造函数设置最大值。表示能够缓存最大数据。

  

- [Android 中为什么推荐用 SparseArray 代替 HashMap？](https://github.com/Moosphan/Android-Daily-Interview/issues/130)

  并不能替换所有的HashMap。只能替换以int类型为key的HashMap。

  HashMap中如果以int为key，会强制使用Integer这个包装类型，当我们使用int类型作为key的时候系统会自用装箱成为Integer，这个过程会创建对象一想效率。SparseArray内部是一个int数组和一个object数组。可以直接使用int减少了自动装箱操作。

  首先并不能说完全用SparseArray替代。在key是int并且数据量小于1000的情况下，用SparseArray替代确实在空间上性能要好，但是在时间上只能接近HashMap而已。
  SparseArray的key固定是int所以减少了装箱和拆箱的操作
  SpareArray内部是运用两个数组进行维护一个是keys存储key的，一个是values存储value的。由于key是int所以没有hash碰撞，在查找位置时用二分查找的方式，时间上比较有优势。
  存储方法由于SparseArray存储的结构比HashMap简单，不需要维护链表等。所以存储上比HashMap好

- [PathClassLoader 和 DexClassLoader 有何区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/163)

  

- [说说 HttpClient 与 HttpUrlConnection 的区别？为何前者会被替代？](https://github.com/Moosphan/Android-Daily-Interview/issues/221)

  

- [什么是Lifecycle？请分析其内部原理和使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/222)

- [谈一谈 Android 的签名机制？不同版本下的签名有什么不同？](https://github.com/Moosphan/Android-Daily-Interview/issues/187)

  

- [谈谈安卓 Apk 构建的流程？](https://github.com/Moosphan/Android-Daily-Interview/issues/150)

  

- [简述一下 Android 8.0、9.0 分别增加了哪些新特性？](https://github.com/Moosphan/Android-Daily-Interview/issues/87)

- [谈谈 Android 10 更新了哪些内容？如何进行适配?](https://github.com/Moosphan/Android-Daily-Interview/issues/145)

- [请简述 Apk 的安装过程？](https://github.com/Moosphan/Android-Daily-Interview/issues/157)

  1. 复制 apk 到 data/app 目录，解压并扫描安装包，向资源管理器注入 apk 资源
  2. 解析 androidmanifest 文件，并在 /data/data 目录下创建对应的应用数据目录，然后针对 Dalvik/art 环境优化 dex 文件，保存到 Dalvik-cache 牧区，将 androidmanifest 文件解析出的组件、权限注册到 PackageManagerService 中，完成后发送广播。

- [Java 与 JS 代码如何互调？有做过相关优化吗？](https://github.com/Moosphan/Android-Daily-Interview/issues/166)

  

- [什么是 JNI？具体说说如何实现 Java 与 C++ 的互调？](https://github.com/Moosphan/Android-Daily-Interview/issues/142)

  1： java 调用 c++ 方法， 通过 java类定义 native 方法， 使用javah 生成.h 头文件，定义cpp文件实现该方法， java 层就可以直接调用该方法了。
  2： c++ 调用java 方法，分静态方法和非静态方法.
  静态方法调用
  JniHelper::callStaticStringMethod(classPath, "getAppName");
  非静态方法调用
  JniMethodInfo->CallVoidMethod(jobj,info.methodID,forceLogin);

  - 在Java类中声明native方法
  - 使用javac命令将Java类生成class文件
  - 使用javah文件生成头文件
  - 编写cpp文件实现jni方法
  - 生成so库
    到此为止，java就可以通过调用native方法调用c++函数了，对于在c++中调用java方法，
  - 通过完整类名获取jclass
  - 根据方法签名和名称获取构造方法id
  - 创建对象（如果要调用的是静态方法则不需要创建对象）
  - 获取对象某方法id
  - 通过JNIEnv根据返回值类型、是否是静态方法调用对应函数即可

- [请谈谈 App 的启动流程？](https://github.com/Moosphan/Android-Daily-Interview/issues/24)

  

## 8.第三方框架分析

- [谈一谈 LeakCanray 的工作原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/160)

  LeakCanary 主要利用了弱引用的对象, 当 GC 回收了这个对象后, 会被放进 ReferenceQueue 中;
  在页面消失, 也就是 activity.onDestroy 的时候, 判断利用 idleHandler 发送一条延时消息, 5秒之后,
  分析 ReferenceQueue 中存在的引用, 如果当前 activity 仍在引用队列中, 则认为可能存在泄漏, 再利用系统类 VMDebug 提供的方法, 获取内存快照,
  找出 GC roots 的最短强引用路径, 并确定是否是泄露, 如果泄漏, 建立导致泄露的引用链;
  System.runFinalization(); // 强制调用已经失去引用的对象的 finalize 方法

- [说说 EventBus 的实现原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/176)

  通过反射机制

- [谈谈网络请求中的拦截器 - Interceptor 的实现原理和使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/193)

  

- [谈一谈 Glide 中的缓存机制？](https://github.com/Moosphan/Android-Daily-Interview/issues/197)

  　　Glide 的缓存设计可以说是非常先进的，考虑的场景也很周全。在缓存这一功能上，Glide 又将它分成了两个模块，一个是内存缓存，一个是硬件缓存。

    　　这两个缓存模块的作用各不相同，内存缓存的主要作用是防止应用重复将图片数据读取到内存当中，而硬盘缓存的主要作用是防止应用重复从网络或其他地方重复下载和读取数据。

    　　内存缓存和硬盘缓存的相互结合才构成了 Glide 极佳的图片缓存效果。

- [ViewModel 的出现是为了解决什么问题？并简要说说它的内部原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/205)

  

- [请说说依赖注入框架 ButterKnife 的实现原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/211)

- [谈一谈 RxJava 背压原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/228)

## 9.综合技术

- [请谈谈你对 MVC 和 MVP 的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/33)

- [分别介绍下你所知道的 Android 中几种存储方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/57)

- [简述下热修复的原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/73)

- [谈谈你是如何适配更多机型的？](https://github.com/Moosphan/Android-Daily-Interview/issues/92)

  1. Dp 适配
  2. 宽高限定符
  3. smallestWidth 适配
  4. 今日头条适配，修改 density 适配

- [请谈谈你是如何进行多渠道打包的？](https://github.com/Moosphan/Android-Daily-Interview/issues/132)

  

- [MVP 中你是如何处理 Presenter 层以防止内存泄漏的？](https://github.com/Moosphan/Android-Daily-Interview/issues/139)

  

- [如何计算一张图片所占的内存空间大小？](https://github.com/Moosphan/Android-Daily-Interview/issues/144)

- [有没有遇到 64k 问题，应该如何解决？](https://github.com/Moosphan/Android-Daily-Interview/issues/152)

- [如何优化 Gradle 的构建速度？](https://github.com/Moosphan/Android-Daily-Interview/issues/154)

- [如何获取 Android 设备唯一 ID？](https://github.com/Moosphan/Android-Daily-Interview/issues/159)

- [谈一谈 Android P 禁用 HTTP 协议对我们开发有什么影响？](https://github.com/Moosphan/Android-Daily-Interview/issues/173)

- [什么是 AOP？在 Android 中它有哪些应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/179)

- [什么是 MVVM？你是如何将其应用于具体项目中的？](https://github.com/Moosphan/Android-Daily-Interview/issues/182)

  

- [请谈谈你会如何实现数据埋点？](https://github.com/Moosphan/Android-Daily-Interview/issues/184)

  

- [假如让你实现断点上传功能，你认为应该怎样去做？](https://github.com/Moosphan/Android-Daily-Interview/issues/192)

  

- [webp 和 svg 格式的图片各自有什么特点？应该如何在 Android 中使用？](https://github.com/Moosphan/Android-Daily-Interview/issues/200)

  

- [说说你是如何进行单元测试的？以及如何应用在 MVP 和 MVVM 中？](https://github.com/Moosphan/Android-Daily-Interview/issues/212)

- [如何绕过 Android 9.0 针对反射的限制？](https://github.com/Moosphan/Android-Daily-Interview/issues/214)

- [对于 GIF 格式的图片加载有什么思路和建议？](https://github.com/Moosphan/Android-Daily-Interview/issues/219)

- [为什么要将项目迁移到 AndroidX？如何进行迁移？](https://github.com/Moosphan/Android-Daily-Interview/issues/232)

- [你了解过哪些Android屏幕适配方面的技巧？](https://github.com/Moosphan/Android-Daily-Interview/issues/234)

## 10.数据结构方面

- [什么是冒泡排序？如何去优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/56)
- [请用 Java 实现一个简单的单链表？](https://github.com/Moosphan/Android-Daily-Interview/issues/59)
- [如何反转一个单链表？](https://github.com/Moosphan/Android-Daily-Interview/issues/89)
- [谈谈你对时间复杂度和空间复杂度的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/97)
- [谈一谈如何判断一个链表有环？](https://github.com/Moosphan/Android-Daily-Interview/issues/122)
- [手写二叉树结构？](https://github.com/Moosphan/Android-Daily-Interview/issues/125)
- [什么是红黑树？为什么要用红黑树？](https://github.com/Moosphan/Android-Daily-Interview/issues/147)
- [什么是快速排序？如何优化？](https://github.com/Moosphan/Android-Daily-Interview/issues/167)
- [说说循环队列？它有哪些应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/189)
- [如何判断单链表交叉？](https://github.com/Moosphan/Android-Daily-Interview/issues/230)

## 11.设计模式

- [请简要谈一谈单例模式？](https://github.com/Moosphan/Android-Daily-Interview/issues/12)
- [对于面向对象的六大基本原则了解多少？](https://github.com/Moosphan/Android-Daily-Interview/issues/29)
- [请列出几种常见的工厂模式并说明它们的用法？](https://github.com/Moosphan/Android-Daily-Interview/issues/50)
- [说说项目中用到的设计模式和使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/72)
- [什么是代理模式？如何使用？Android源码中的代理模式？](https://github.com/Moosphan/Android-Daily-Interview/issues/81)
- [谈一谈单例模式，建造者模式，工厂模式的使用场景？如何合理选择？](https://github.com/Moosphan/Android-Daily-Interview/issues/86)
- [谈谈你对原型模式的理解？](https://github.com/Moosphan/Android-Daily-Interview/issues/121)
- [请谈谈策略模式原理及其应用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/143)
- [静态代理和动态代理的区别，什么场景使用？](https://github.com/Moosphan/Android-Daily-Interview/issues/165)
- [谈一谈责任链模式的使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/183)

## 12.计算机网络方面

- [请简述 Http 与 Https 的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/71)
- [说一说 HTTPS、UDP、Socket 之间的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/78)
- [请简述一次 HTTP 网络请求的过程？](https://github.com/Moosphan/Android-Daily-Interview/issues/94)
- [谈一谈 TCP/IP 三次握手、四次挥手过程？](https://github.com/Moosphan/Android-Daily-Interview/issues/129)
- [为什么说Http是可靠的数据传输协议？](https://github.com/Moosphan/Android-Daily-Interview/issues/218)
- [TCP/IP 协议分为哪几层？TCP 和 HTTP 分别属于哪一层？](https://github.com/Moosphan/Android-Daily-Interview/issues/223)
- [Post 中请求参数放在了哪个位置？](https://github.com/Moosphan/Android-Daily-Interview/issues/226)

## 13.Kotlin 方面

- [请简述一下什么是 Kotlin？它有哪些特性？](https://github.com/Moosphan/Android-Daily-Interview/issues/67)
- [Kotlin 中注解 @JvmOverloads 的作用？](https://github.com/Moosphan/Android-Daily-Interview/issues/76)
- [Kotlin 中 List 与 MutableList 的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/90)
- [Kotlin 中实现单例的几种常见方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/96)
- [谈谈你对 Kotlin 中的 data 关键字的理解？相比于普通类有哪些特点？](https://github.com/Moosphan/Android-Daily-Interview/issues/101)
- [什么是委托属性？请简要说说其使用场景和原理？](https://github.com/Moosphan/Android-Daily-Interview/issues/107)
- [请举例说明 Kotlin 中 with 与 apply 函数的应用场景和区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/116)
- [Kotlin中 Unit 类型的作用以及与Java中 Void 的区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/124)
- [Kotlin 中 infix 关键字的原理和使用场景？](https://github.com/Moosphan/Android-Daily-Interview/issues/128)
- [Kotlin中的可见性修饰符有哪些？相比于 Java 有什么区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/140)
- [你觉得 Kotlin 与 Java 混合开发时需要注意哪些问题？](https://github.com/Moosphan/Android-Daily-Interview/issues/146)
- [在 Kotlin 中，何为解构？该如何使用？](https://github.com/Moosphan/Android-Daily-Interview/issues/164)
- [在 Kotlin 中，什么是内联函数？有什么作用？](https://github.com/Moosphan/Android-Daily-Interview/issues/169)
- [谈谈Kotlin中的构造方法？有哪些注意事项？](https://github.com/Moosphan/Android-Daily-Interview/issues/180)
- [谈谈 Kotlin 中的 Sequence，为什么它处理集合操作更加高效？](https://github.com/Moosphan/Android-Daily-Interview/issues/188)
- [请谈谈 Kotlin 中的 Coroutines，它与线程有什么区别？有哪些优点？](https://github.com/Moosphan/Android-Daily-Interview/issues/190)
- [Kotlin中可见型修饰符有哪些？相比于Java有什么区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/196)
- [谈谈Kotlin中的Unit？它和Java中的void有什么区别？](https://github.com/Moosphan/Android-Daily-Interview/issues/198)
- [Kotlin中该如何安全地处理可空类型？](https://github.com/Moosphan/Android-Daily-Interview/issues/201)
- [说说 Kotlin中 的 Any 与Java中的 Object 有何异同？](https://github.com/Moosphan/Android-Daily-Interview/issues/209)
- [Kotlin中的数据类型有隐式转换吗？为什么？](https://github.com/Moosphan/Android-Daily-Interview/issues/217)
- [分别通过对象表达式 object 和 lambda 表达式实现的函数式接口内部有何不同？](https://github.com/Moosphan/Android-Daily-Interview/issues/224)
- [Kotlin 中集合遍历有哪几种方式？](https://github.com/Moosphan/Android-Daily-Interview/issues/231)
- [为什么协程比线程要轻量？](https://github.com/Moosphan/Android-Daily-Interview/issues/233)

## 14.开放性问题

- [你知道哪些提升开发效率的骚操作？](https://github.com/Moosphan/Android-Daily-Interview/issues/112)
- [在开发过程中你遇到过的最大的难题是什么？如何解决的？](https://github.com/Moosphan/Android-Daily-Interview/issues/227)
- [说说你未来的职业规划是怎样的？](https://github.com/Moosphan/Android-Daily-Interview/issues/236)
- [你是如何看待 Flutter，React Native 与 Android 关系的？](https://github.com/Moosphan/Android-Daily-Interview/issues/158)

