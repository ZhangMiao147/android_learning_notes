# Android 的常见问题 3

[TOC]



# 1. ANR

　　ANR 全称是 Application Not Responding，意思就是应用程序未响应。如果一个应用无法响应用户的输入，系统就会弹出一个 ANR 对话框，用户可以自行选择继续等待或者是停止当前程序。

## 1.1. ANR 的发生原因

1. 代码自身引起，例如：

   * 主线程阻塞、IOWait 等；


   - 主线程进行耗时计算；
   - 错误的操作，比如调用了 Thread.wait 或者 Thread.sleep 等。

2. 其他进程间接引起，例如：

   * 当前应用进程进行进程间通信请求其他进程，其他进程的操作长时间没有反馈；
   * 其他进程的 CPU 占用率高，使得当前应用进程无法抢占到 CPU 时间片。

## 1.2. 发生 ANR 的条件

　　Andriod 系统中，ActivityManangerService(简称 AMS) 和 WindowManangerService(简称 WMS) 会检测 App 的响应事件，如果 App 在特定时间无法响应屏幕触摸或键盘输入事件，或者特定事件没有处理完毕，就会出现 ANR。

* InputDispatching Timeout：输入事件分发或屏幕触摸事件超时 5s 未响应完毕；
* BroadcastQueue Timeout：前台广播在 10 秒内、后台广播在 60 秒内未执行完成；
* Service Timeout：前台服务在 20 秒内、后台服务在 200 秒内未执行完成；
* ContentProvider Timeout：内容提供者，在 publish 超时 10s；

## 1.3. 分析 ANR 的方法

### 1.3.1. ANR 分析方法一：Log

![](J:/zhangmiao/android_learning_notes/Android/optimize/image/ANR_LOG.png)
　　可以看到 logcat 清晰地记录了 ANR 发生的时间，发生 ANR 所在的报名、类名以及线程的 tid 和一句话概括原因：WaitingInMainSignalCatcherLoop，大概意思为主线程等待异常。

　　最后一句 The application may be doing too much work on its main thread. 告知可能在主线程做了太多的工作。

### 1.3.2. ANR 分析方法二：traces.txt

　　上面的 log 有第二句 Wrote stack trances to '/data/anr/trances.txt'，说明 ANR 异常已经输出到 trances.txt 文件，使用 adb 命令把这个文件从手机里导出来：

1. 进入 adb.exe 所在的目录，也就是 Android SDK 的 platform-tools 目录：

```
cd D:\Android\AndroidSdk\platform-tools
```

2. 到指定目录后执行以下 adb 命令导出 trances.txt 文件：

```
adb pull /data/anr/trances.txt
```

　　traces.txt 默认会被导出到 Android SDK 的 \platform-tools 目录。一般来说 traces.txt 文件记录的东西会比较多，分析的时候需要有针对性地去找相关记录。

```java
----- pid 23346 at 2017-11-07 11:33:57 -----  ----> 进程id和ANR产生时间
Cmd line: com.sky.myjavatest
Build fingerprint: 'google/marlin/marlin:8.0.0/OPR3.170623.007/4286350:user/release-keys'
ABI: 'arm64'
Build type: optimized
Zygote loaded classes=4681 post zygote classes=106
Intern table: 42675 strong; 137 weak
JNI: CheckJNI is on; globals=526 (plus 22 weak)
Libraries: /system/lib64/libandroid.so /system/lib64/libcompiler_rt.so
/system/lib64/libjavacrypto.so
/system/lib64/libjnigraphics.so /system/lib64/libmedia_jni.so /system/lib64/libsoundpool.so
/system/lib64/libwebviewchromium_loader.so libjavacore.so libopenjdk.so (9)
Heap: 22% free, 1478KB/1896KB; 21881 objects    ----> 内存使用情况
...
"main" prio=5 tid=1 Sleeping    ----> 原因为Sleeping
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x733d0670 self=0x74a4abea00
  | sysTid=23346 nice=-10 cgrp=default sched=0/0 handle=0x74a91ab9b0
  | state=S schedstat=( 391462128 82838177 354 ) utm=33 stm=4 core=3 HZ=100
  | stack=0x7fe6fac000-0x7fe6fae000 stackSize=8MB
  | held mutexes=
  at java.lang.Thread.sleep(Native method)
  - sleeping on <0x053fd2c2> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:373)
  - locked <0x053fd2c2> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:314)
  at android.os.SystemClock.sleep(SystemClock.java:122)
  at com.sky.myjavatest.ANRTestActivity.onCreate(ANRTestActivity.java:20) ----> 产生ANR的包名以及具体行数
  at android.app.Activity.performCreate(Activity.java:6975)
  at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1213)
  at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2770)
  at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2892)
  at android.app.ActivityThread.-wrap11(ActivityThread.java:-1)
  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593)
  at android.os.Handler.dispatchMessage(Handler.java:105)
  at android.os.Looper.loop(Looper.java:164)
  at android.app.ActivityThread.main(ActivityThread.java:6541)
  at java.lang.reflect.Method.invoke(Native method)
  at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)
```

　　在文件中使用 ctrl + F 查找包名可以快速定位相关代码。

　　通过上方 log 可以看出相关问题：

* 进程 id 和包名：pid 23346 com.sky.myjavatest
* 造成 ANR 的原因：Sleeping
* 造成 ANR 的具体行数：ANRTestActivity.java:20 类的第 20 行

**注意**：产生新的 ANR，原来的 traces.txt 文件会被覆盖。

### 1.3.3. ANR 分析方法三：Java 线程调用分析

　　通过 JDK 提供的命令可以帮助分析和调试 Java 应用，命令是：

```java
jstack {pid}
```

　　其中 pid 可以通过 jps 命令获得，jps 命令会列出当前系统中运行的所有 Java 虚拟机进程，比如：

```java
7266 Test
7267 Jps
```

### 1.3.4. ANR 分析方法四：DDMS 分析 ANR 问题

* 使用 DDMS-----Update Threads 工具
* 阅读 Update Threads 的输出

## 1.4. 如何避免 ANR

　　不是所有的 ANR 都可找到原因，也受限于当时发生的环境或系统 bug，因此对 ANR ，是避免而不是分析。

​		基本的思路就是尽量避免在主线程（UI 线程）中做耗时操作，将耗时操作放在子线程中。将 IO 操作在工作线程来处理，减少其他耗时操作和错误操作。

* 使用 AsyncTask 处理耗时 IO 操作。
* 使用 Thread 或者 HandlerThread 时，调用 Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)设置优先级，否则仍然会降低程序响应，因为默认 Thread 的优先级和主线程相同。
* 使用 Handler 处理工作线程结果，而不是使用 Thread.wait() 或者 Thread.sleep() 来阻塞主线程。
* 四大组件的生命周期方法中尽量避免耗时的代码。
* 如果要在后台进行耗时操作，建议使用 IntentService 处理。
* 在程序启动时，如果要做一些耗时操作，可以选择加上欢迎界面，避免用户察觉卡顿。
* 主程序需要等待其他线程返回结果时，可以加上进度显示，比如使用 ProgressBar 控件，让用户得知进度。
* 使用 Systrace 和 TraceView 找到影响响应的问题，进一步优化。
* 如果是由于内存不足引起的问题，AndroidManifest.xml 文件 < application > 中可以设置 android:largeHeap="true"，以此增大 App 使用内存。不过不建议使用此法，从根本上防止内存泄漏，优化内存使用才是正确的做法。

# 2. OOM

　　OOM 是指 out of memory，内存泄漏，一个程序中，已经不需要使用某个对象，但是仍然有引用指向它，垃圾回收器无法回收它，该对象占用的内存无法被回收时，就容易造成内存泄漏。

## 2.1. 单例造成的内存泄漏

　　Android的单例模式非常受开发者的喜爱，不过使用的不恰当的话也会造成内存泄漏。因为单例的静态特性使得单例的生命周期和应用的生命周期一样长，这就说明了如果一个对象已经不需要使用了，而单例对象还持有该对象的引用，那么这个对象将不能被正常回收，这就导致了内存泄漏。

```csharp
public class AppManager {
    private static AppManager instance;
    private Context context;
    private AppManager(Context context) {
        this.context = context;
    }
    public static AppManager getInstance(Context context) {
        if (instance != null) {
            instance = new AppManager(context);
        }
        return instance;
    }
}
```

这是一个普通的单例模式，当创建这个单例的时候，由于需要传入一个Context，所以这个Context的生命周期的长短至关重要：
 1、传入的是Application的Context：这将没有任何问题，因为单例的生命周期和Application的一样长 ；
 2、传入的是Activity的Context：当这个Context所对应的Activity退出时，由于该Context和Activity的生命周期一样长（Activity间接继承于Context），所以当前Activity退出时它的内存并不会被回收，因为单例对象持有该Activity的引用。

正确的单例应该修改为下面这种方式：

```csharp
public class AppManager {
    private static AppManager instance;
    private Context context;
    private AppManager(Context context) {
        this.context = context.getApplicationContext();
    }
    public static AppManager getInstance(Context context) {
        if (instance != null) {
            instance = new AppManager(context);
        }
        return instance;
    }
}
```

　　这样不管传入什么Context最终将使用Application的Context，而单例的生命周期和应用的一样长，这样就防止了内存泄漏。

## 2.2. 非静态内部类创建静态实例造成的内存泄漏

　　有的时候我们可能会在启动频繁的Activity中，为了避免重复创建相同的数据资源，会出现这种写法：

```java
public class MainActivity extends AppCompatActivity {
   private static TestResource mResource = null;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       if(mManager == null){
           mManager = new TestResource();
       }
       //...
   }
   class TestResource {
       //...
   }
}
```

　　这样就在Activity内部创建了一个非静态内部类的单例，每次启动Activity时都会使用该单例的数据，这样虽然避免了资源的重复创建，不过这种写法却会造成内存泄漏，因为非静态内部类默认会持有外部类的引用，而又使用了该非静态内部类创建了一个静态的实例，该实例的生命周期和应用的一样长，这就导致了该静态实例一直会持有该Activity的引用，导致Activity的内存资源不能正常回收。

　　正确的做法为：将该内部类设为静态内部类或将该内部类抽取出来封装成一个单例，如果需要使用Context，请使用ApplicationContext 。

## 2.3. Handler造成的内存泄漏

```java
public class MainActivity extends AppCompatActivity {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //...
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadData();
    }
    private void loadData(){
        //...request
        Message message = Message.obtain();
        mHandler.sendMessage(message);
    }
}
```

　　mHandler是Handler的非静态匿名内部类的实例，所以它持有外部类Activity的引用，我们知道消息队列是在一个Looper线程中不断轮询处理消息，那么当这个Activity退出时消息队列中还有未处理的消息或者正在处理消息，而消息队列中的Message持有mHandler实例的引用，mHandler又持有Activity的引用，所以导致该Activity的内存资源无法及时回收，引发内存泄漏，所以另外一种做法为：

```java
public class MainActivity extends AppCompatActivity {
   private MyHandler mHandler = new MyHandler(this);
   private TextView mTextView ;
   private static class MyHandler extends Handler {
       private WeakReference<Context> reference;
       public MyHandler(Context context) {
           reference = new WeakReference<>(context);
       }
       @Override
       public void handleMessage(Message msg) {
           MainActivity activity = (MainActivity) reference.get();
           if(activity != null){
               activity.mTextView.setText("");
           }
       }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       mTextView = (TextView)findViewById(R.id.textview);
       loadData();
   }

   private void loadData() {
       //...request
       Message message = Message.obtain();
       mHandler.sendMessage(message);
   }
}
```

　　创建一个静态Handler内部类，然后对Handler持有的对象使用弱引用，这样在回收时也可以回收Handler持有的对象，这样虽然避免了Activity泄漏，不过Looper线程的消息队列中还是可能会有待处理的消息，所以我们在Activity的Destroy时或者Stop时应该移除消息队列中的消息，更准确的做法如下：

```java
public class MainActivity extends AppCompatActivity {
   private MyHandler mHandler = new MyHandler(this);
   private TextView mTextView ;
   private static class MyHandler extends Handler {
       private WeakReference<Context> reference;
       public MyHandler(Context context) {
           reference = new WeakReference<>(context);
       }
       @Override
       public void handleMessage(Message msg) {
           MainActivity activity = (MainActivity) reference.get();
           if(activity != null){
               activity.mTextView.setText("");
           }
       }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       mTextView = (TextView)findViewById(R.id.textview);
       loadData();
   }

   private void loadData() {
       //...request
       Message message = Message.obtain();
       mHandler.sendMessage(message);
   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       mHandler.removeCallbacksAndMessages(null);
   }
}
```

　　使用mHandler.removeCallbacksAndMessages(null);是移除消息队列中所有消息和所有的Runnable。当然也可以使用mHandler.removeCallbacks();或mHandler.removeMessages();来移除指定的Runnable和Message。

## 2.4. 线程造成的内存泄漏

```java
//——————test1
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(10000);
                return null;
            }
        }.execute();
//——————test2
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(10000);
            }
        }).start();
```

　　上面的异步任务和 Runnable 都是一个匿名内部类，因此它们对当前 Activity 都有一个隐式引用。如果 Activity 在销毁之前，任务还未完成， 那么将导致 Activity 的内存资源无法回收，造成内存泄漏。正确的做法还是使用静态内部类的方式，如下：

```java
   static class MyAsyncTask extends AsyncTask<Void, Void, Void> {
       private WeakReference<Context> weakReference;

       public MyAsyncTask(Context context) {
           weakReference = new WeakReference<>(context);
       }

       @Override
       protected Void doInBackground(Void... params) {
           SystemClock.sleep(10000);
           return null;
       }

       @Override
       protected void onPostExecute(Void aVoid) {
           super.onPostExecute(aVoid);
           MainActivity activity = (MainActivity) weakReference.get();
           if (activity != null) {
               //...
           }
       }
   }
   static class MyRunnable implements Runnable{
       @Override
       public void run() {
           SystemClock.sleep(10000);
       }
   }
//——————
   new Thread(new MyRunnable()).start();
   new MyAsyncTask(this).execute();
```

　　这样就避免了Activity的内存资源泄漏，当然在Activity销毁时候也应该取消相应的任务AsyncTask::cancel()，避免任务在后台执行浪费资源。

## 2.5. 资源未关闭造成的内存泄漏

　　对于使用了 BraodcastReceiver，ContentObserver，File，Cursor，Stream，Bitmap 等资源的使用，应该在 Activity 销毁时及时关闭或者注销，否则这些资源将不会被回收，造成内存泄漏。

# 3. MVP

## 3.1. MVC

![](架构与设计模式/image/mvc.png)

　　视图层（View）对应于 xml 布局文件和 java 代码动态 view 部分。

　　控制层（Controller）MVC 中 Android 的控制层是由 Activity 来承担的，Activity 本来主要是作为初始化页面，展示数据的操作，但是因为 XML 视图功能太弱，所以 Activity 既要负责视图的显示又要加入控制逻辑，承担的功能太多。

　　模型层（Model）针对业务模型，建立的数据结构和相关的类，它主要负责网络请求、数据库处理、I/O 操作。

　　优点：具有一定的分层，mode 彻底戒耦，业务逻辑被放置在 mode 层，能够更好的复用和修改增加业务。

　　缺点：controller 和 view 在 android 中无法做到彻底分离，并没有解耦。

## 3.2. MVP

　　MVP 和 MVC 很像，MVP 也是三层，唯一的差别是 Mode 和 View 之间不进行通讯，都是通过 Presenter 完成。MVC 有一个缺点就是在 Android 中由于 Activity 的存在，Controller 和 View 很难做到完全解耦，但在 MVP 中就可以很好的解决这个问题。

![](架构与设计模式/image/mvp.png)

　　MVP 中也有一个 Contract 类，Contract 在 MVP 中是一个契约类，契约类用于定义同一个界面的 view 接口和 presenter 的具体实现，好处是通过规范的方法命名和注释可以清晰的看到整个页面的逻辑。

　　优点：实现了视图层的独立，通过中间层 presnter 实现了 model 和 view 的完全解耦。

　　缺点：随着业务逻辑的增加，一个页面可能会非常复杂，UI 的改变是非常多的，会有非常多的 case，这样就会造成 view 接口会很庞大。

## 3.3. MVVM

　　MVP 会随着业务逻辑的增加、UI 的改变多的情况下，会有非常多的跟 UI 相关的 case，这样就会造成 View 的接口会很庞大。而 MVVM 就解决了这个问题，通过双向绑定的机制，实现数据和 UI 内容，只要想改其中一方，另一方都能及时更新的一种设计理念，这样就省去了很多在 View 层中写很多 case 的情况，只需要改变数据就行。MVVM 的设计图：
![](架构与设计模式/image/MVVM.png)

　　一般情况就这两种情况，看起来跟 MVP 好像没什么差别，其实区别还是挺大的，在 MVP 中 View 和 Presenter 要相互持有，方便调用对方，而在 MVP 中 View 和 ViewModel 通过 Binding 进行关联，他们之间的关联处理通过 DataBinding 完成。

　　优点：很好的解决了 MVC 和 MVP 的不足。

　　缺点：由于数据和视图的双向绑定，导致出现问题时不太好定位来源，有可能数据问题导致，也有可能业务逻辑中对数据属性的修改导致。

### 3.3.1. MVVM 与 DataBinding 的关系

　　MVVM 是一种思想，DataBinding 是谷歌推出的方便实现 MVVM 的工具。在 google 推出 DataBinding 之前，因为 xml layout 功能较弱，想实现 MVVM 非常困难，而 DataBinding 的出现可以很方便的实现 MVVM。

　　DataBinding 是实现视图和数据双向绑定的工具。

## 3.4. 关于 MVC、MVP、MVVM 如何选择

　　在 MVP 中要实现根据业务逻辑和页面逻辑做很多 Present 和 View 的具体实现，如果这些 case 太多，会导致代码的可读性变差，但是通过引入 contract 契约类，会让业务逻辑变得清晰许多。

　　简单建议：

1. 如果项目简单，没什么复杂性，未来改动也不大的话，那就不要用设计模式或者架构方法，只需要将每个模块封装好，方便调用即可，不要为了使用设计模式或架构方法而使用。
2. 对于偏向展示型的 app，绝大多数业务逻辑都在后端，app 主要功能就是展示数据、交互等，建议使用 mvvm。
3. 对于工具类或者需要写很多业务逻辑 app，使用 mvp 或者 mvvm 都可。

https://www.jianshu.com/p/e1e10211621d

# 4. Activity

　　Activity 是 Android 的四大组件之一，主要用于提供窗口与用户进行交互。

## 4.1. Activity 的生命周期

![](components/Activity/image/Activity生命周期图.png)

　　解释图中个方法的作用：

| 生命周期方法 | 作用                       | 说明                                                         |
| ------------ | -------------------------- | ------------------------------------------------------------ |
| onCreate     | 表示 Activity 正在被创建   | activity 被创建时调用，一般在这个方法中进行活动的初始化工作，如设置布局工作、加载数据、绑定控件等。 |
| onRestart    | 表示 Activity 正在重新启动 | 这个回调代表了 Activity 由完全不可见重新变为可见的过程，当 Activity 经历了 onStop() 回调变为完全不可见后，如果用户返回原 Activity，便会触发该回调，并且紧接着会触发 onStart() 来使活动重新可见。 |
| onStart      | 表示 Activity 正在被启动   | 经历该回调后，Activity 由不可见变为可见，但此时处于后台可见，还不能和用户进行交互。 |
| onResume     | 表示 Activity 已经可见     | 已经可见的 Activity 从后台来到前台，可以和用户进行交互。     |
| onPause      | 表示 Activity 正在停止     | 当用户启动了新的 Activity ，原来的 Activity 不再处于前台，也无法与用户进行交互，并且紧接着就会调用 onStop() 方法，但如果用户这时立刻按返回键回到原 Activity ，就会调用 onResume() 方法让活动重新回到前台。而且在官方文档中给出了说明，不允许在 onPause() 方法中执行耗时操作，因为这会影响到新 Activity 的启动。<br /><br />一般会导致变为 onPause 状态的原因除了 onStop 中描述的四个原因外，还包括当用户按 Home 键出现最近任务列表时。 |
| onStop       | 表示 Activity 即将停止     | 这个回调代表了 Activity 由可见变为完全不可见，在这里可以进行一些稍微重量级的操作。需要注意的是，处于 onPause() 和 onStop() 回调后的 Activity 优先级很低，当有优先级更高的应用需要内存时，该应用就会被杀死，那么当再次返回原 Activity 的时候，会重新调用 Activity 的onCreate()方法。<br /><br />一般会导致变为 stop 状态的原因：1.用户按 Back 键后、用户正在运行 Activity 时，按 Home 键、程序中调用 finish() 后、用户从 A 启动 B 后，A 就会变为 stop 状态。 |
| onDestroy    | 表示 Activity 即将被销毁   | 来到了这个回调，说明 Activity 即将被销毁，应该将资源的回收和释放工作在该方法中执行。<br /><br />当 Activity 被销毁时，销毁的情况包括：当用户按下 Back 键后、程序中调用 finish() 后。 |
| onNewIntent  | 重用栈中 Activity          | 当在 AndroidManifest 里面声明 Activty 的时候设置了 launchMode 或者调用 startActivity 的时候设置了 Intent 的 flag ，当启动 Activity 的时候，复用了栈中已有的 Activity，则会调用 Activity 的该回调。 |

## 4.2. 冷启动与热启动

　　所谓冷启动就是启动该应用时，后台没有该应用的进程，此时系统会创建一个进程分配给它，之后会创建和初始化 Application，然后通过反射执行 ActivityThread 中的 main 方法。而热启动则是，当启动应用的时候，后台已经存在该应用的进程，比如按 home 键返回主界面再打开该应用，此时会从已有的进程中来启动应用，这种方式下，不会重新走 Application 这一步。

![](components/Activity/image/冷启动流程图.png)

# 5. Service

　　Service 是可以在后台执行长时间运行操作并且不需要和用户交互的应用组件。

　　服务可以在很多场合使用，比如播放多媒体的时候用户启动了其他 activity，此时要在后台继续播放；比如检测 sd 卡上文件的变化；比如在后台记录你的地理位置的改变；也可以执行进程间通信（IPC）等等。

## 5.1. 两种启动方式

　　服务有两种启动方式，一种是启动服务，一种就是绑定服务。

### 5.1.1. 启动服务

　　当应用组件（如 Activity）通过调用 `startService()` 启动服务时，服务即处于 “ 启动 ” 状态。一旦启动，服务即可在后台无限期运行，即使启动服务的组件已被销毁也不受影响，除非手动调用才能停止服务，已启动的服务通常是执行单一操作，而且不会将结果返回给调用方。

### 5.1.2. 绑定服务

　　当应用组件通过调用 `bindService()` 绑定到服务时，服务即处于 “ 绑定 ” 状态。绑定服务提供了一个客户端 - 服务端接口，允许组件与服务进行交互、发送请求、获取结果，甚至是利用进程间通信（IPC）执行这些操作。仅当与另一个应用组件绑定时，绑定服务才会运行。多个组件可以同时绑定到该服务，但全部取消绑定后，该服务即会被销毁。

　　应用程序组件（客户端）通过调用 `bindService()`方法能够绑定服务，然后 Android 系统会调用服务的 `onBind()` 回调方法，这个方法会返回一个跟服务器端交互的 Binder 对象。

　　这个绑定是异步的，`bindService()` 方法立即返回，并且不给客户端返回 IBinder 对象。要接收 IBinder 对象，客户端必须创建一个 `ServiceConnection` 类的实例，并且把这个实例传递给 `bindService()` 方法。`ServiceConnection` 对象包含了一个系统调用的传递 IBinder 对象的回调方法。

　　注意：只有 Activity、Service、ContentProvider 能够绑定服务；BroadcastReceiver 广播接收器不能绑定服务。

### 5.1.3. 关于绑定服务的注意点

1. 多个客户端可同时连接到一个服务。不过，只有在第一个客户端绑定时，系统才会调用服务的 `onBind()` 方法来检索 IBinder。系统随后无需再次调用 `onBind()`，便可将同一 IBinder 传递至任何其他绑定的客户端。当最后一个客户端取消与服务的绑定时，系统会将服务销毁（除非 `startService()` 也启动了该服务）。

2. 通常情况下，应该在客户端生命周期（如 Activity 的生命周期）的引入和退出时刻设置绑定和解绑操作，以便控制绑定状态下的 Service，一般有以下两种情况：

   a. 如果只需要在 Activity 可见时与服务交互，则应在 `onStart()` 期间绑定，在 `onStop()` 期间解绑。

   b. 如果希望 Activity 在后台停止运行状态下仍可接收响应，则可在 `onCreate()` 期间绑定，在 `onDestory()` 期间取消绑定。需要注意的是，这意味着 Activity 在其整个运行过程中（甚至包括后台运行期间）都需要使用服务，因此如果服务位于其他进程内，那么当提高该进程的权重时，系统很可能会终止该进程。

3. 通常情况下，请勿在 Activity 的 `onResume()` 和 `onPause() `期间绑定和解绑，因为每一次生命周期转换都会发生这些回调，这样反复绑定和解绑是不合理的。此外，如果应用内的多个 Activity 绑定到同一服务，并且其中两个 Activity 之间发生了转换，则如果当前 Activity 在下一次绑定（恢复期间）之前取消绑定（暂停期间），系统可能会销毁服务并重建服务，因此服务的绑定不应该发生在 Activity 的 `onResume()` 和 `onPause()` 中。

4. 应该始终捕获 DeadObjectException 异常，该异常是在连接中断时引发的，表示调用的对象已死亡，也就是 Service 对象已销毁，这是远程方法引用的唯一异常，DeadObjectException 继承自 RemoteException，因此也可以捕获 RemoteException 异常。

5. 应用组件（客户端）可通过调用 `bindService()` 绑定到服务，Android 系统随后调用服务的 `onBind() `方法，该方法返回用于与服务交互的 IBinder，而该绑定是异步执行的。

## 5.2. Service 的生命周期

![](J:/zhangmiao/android_learning_notes/Android/components/Service/image/Service生命周期图.png)

### 5.2.1. onCreate()

　　首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 `onStartCommand()` 或 `onBind()` 之前），如果服务已在运行，则不会调用此方法，该方法只调用一次。

　　`onCreate()` 方法只会在 Service 第一次被创建的时候调用，而 `onStartCommand()` 方法在每次启动服务的时候都会调用。

### 5.2.2. onBind()

　　当另一个组件想通过调用 `bindService()` 与服务绑定（例如执行 RPC）时，系统将调用此方法。在此方法实现中，必须返回一个 IBinder 接口的实现类，供客户端用来与服务进行通信。无论是启动状态还是绑定状态，此方法必须重写，但在启动状态就会直接返回 null 。

### 5.2.3. onStartCommand()

　　当另一个组件（Activity）通过调用 `startService()` 请求启动服务时系统将调用此方法，一旦执行此方法，服务即会启动并可在后台无限期运行。如果自己实现此方法，则需要在服务工作完成后，通过调用 `stopSelf()` 或 `stopService()` 来停止服务（在绑定状态无需实现此方法）。

#### 5.2.3.1. onStartCommand()方法的返回值

　　`onStartCommand()` 方法执行后，返回的是一个 int 型。这个整型可以有三个返回值：START_NOT_STICKY、START_STICKY、START_REDELIVER_INTENT。

* **START_NOT_STICKY**：“非粘性的”。使用这个返回值时，如果在执行完 `onStartComment()` 方法后，服务被异常 kill 掉，系统不会自动重启该服务。

* **START_STICKY**：如果 Service 进程被 kill 掉，保留 Service 的状态为开始状态，但不保留递送的 intent 对象。随后系统会尝试重新创建 Service，由于服务状态为开始状态，所以创建服务后一定会调用 `onStartCommand(Intent,int,int)` 方法。如果在此期间没有任何启动命令被传递到 Service，那么参数 Intent 将为 null 。

* **START_REDELIVER_INTENT**：重传 Intent。使用这个返回值时，系统会自动重启该服务，并将 Intent 的值传入。

### 5.2.4. onUnbind()

　　当另一个组件通过调用 `unbindServicer()` 与服务解绑时，系统将调用此方法。

### 5.2.5. onDestroy()

　　当服务不再使用且被销毁时，系统将调用此方法，服务应该实现此方法来清理所有的资源，如线程、注册的监听器、接收器等，这是服务接收的最后一个调用。

## 5.3. 启动服务和绑定服务

### 5.3.1. 启动服务和绑定服务的生命周期

#### 5.3.1.1. 启动服务生命周期

　　第一次调用 `startService()` 启动服务，会调用 `onCreate()` 和 `onStartCommand()` 方法，之后调用 `startService()` 启动服务，只会调用 `onStartCommand()` 方法。调用 `stopService()` 方法停止服务，会调用 `onDestory()` 方法。停止服务之后再次 `startService()` 启动服务，会再次调用 `onCreate()` 和 `onStartCommand()` 方法。

#### 5.3.1.2. 绑定服务生命周期

　　第一次调用 `bindService()` 启动服务，调用 `onCreate() `和 `onBind()` 方法，之后调用 `bindService()` 没有任何方法调用，调用 `unbindService()` 方法解绑服务，会调用 `onUnbind()` 和 `onDestory()` 方法。在 Activity 退出的时候不调用 `unbindService()` 解绑的话会报错。

#### 5.3.1.3. 启动并绑定服务生命周期

　　Android 系统仅会为一个 Service 创建一个实例对象，所以不管是启动服务还是绑定服务，操作的是同一个 Service 实例，而且由于绑定服务或者启动服务之间顺序问题将会出现两种情况：

##### 5.3.1.3.1. 先绑定服务后启动服务

![](J:/zhangmiao/android_learning_notes/Android/components/Service/image/先绑定再启动.png)

　　先调用 `bindService()` 方法，调用 `onCreate()` 和 `onBind()` 方法，再调用 `startService()` 方法，调用 `onStartCommand()` 方法，调用 `unbindService()` 方法解绑，调用 `onUnbind()` 方法，再调用 `stopService()` 方法，调用 `onDestory()` 方法，如果是先调用 `stopService()` 没有方法回调，再调用 `unbindService()` 方法解绑会调用 `onUnbind()` 和 `onDestory()` 方法。

　　如果当前 Service 实例先以绑定状态运行，然后再以启动状态运行，如果之前绑定的宿主（Activity）被销毁了，也不会影响服务的运行，服务还是会一直运行下去，直到调用停止服务或者内存不足时才会销毁该服务。

##### 5.3.1.3.2. 先启动服务后绑定服务

![](J:/zhangmiao/android_learning_notes/Android/components/Service/image/先启动再绑定.png)

　　先调用 `startService()` 方法，调用 `onCreate()` 和 `onStartCommand()` 方法，（之后再调用 `startService()` 方法，只会回调 `onStartCommand()` 方法）再调用 `bindService()` 方法，调用 `onBind()` 方法，调用 `unbindService()` 方法解绑，调用 `onUnbind()` 方法，再调用 `stopService()` 方法，调用 `onDestory()` 方法，如果是先调用 `stopService()` 没有方法回调，再调用 `unbindService()` 方法解绑会调用 `onUnbind()` 和 `onDestory()` 方法。

　　如果当前 Service 实例先以启动状态运行，然后再以绑定状态运行，即使宿主解绑后，服务依然在后台运行，直到有 Context 调用了 `stopService()` 或是服务本身调用了 `stopSelf()` 方法抑或内存不足时才会销毁服务。

　　以上两种情况显示出启动服务的优先级要比绑定服务高一些。不过无论 Service 是处于启动状态还是绑定状态，或者是启动并绑定状态，都可以像使用 Activity 那样通过调用 Intent 来使用服务。当然，也可以通过清单文件将服务声明为私有服务，阻止其他应用访问。

　　在调用 `startService()` 和 `bindService()` 方法之后，如果 Service 的 `onUnbind()` 返回的是 true，调用 `unbindService()` 解绑之后，再次调用 `bindService()` 绑定服务，会调用 Service 的 `onRebind()` 方法，而不是什么方法都不调用（会回调 ServiceConnection 的 `onServiceConnected()` 方法）。

### 5.3.2. 启动服务与绑定服务的区别

**区别一：生命周期**
　　通过 started 方式的服务会一直运行在后台，需要由组件本身或外部组件来停止服务才会以结束结束。

　　bind 方式的服务，生命周期就会依赖绑定的组件。

**区别二：参数传递**
　　started 服务可以给启动的服务对象传递参数，但无法获取服务中方法的返回值。

　　bind 服务可以给启动的服务对象传递参数，也可以用过绑定的业务对象获取返回结果。

## 5.4. IntentService

　　服务不会自动开启线程，服务中的代码默认是运行在主线程中，如果直接在服务里执行一些耗时操作，容易造成 ANR(Application Not Responding)异常，为了可以简单的创建一个异步的、会自动停止的服务，Android 专门提供了一个 **IntentService** 类。可以启动 IntentService 多次，而每一个耗时操作会以工作队列的方式在 IntentService 的 onHandleIntent() 回调方法中执行，并且每次只会执行一个工作线程，执行完第一个，再执行第二个，以此类推。

# 6. BroadcastReceiver

　　BroadcastReceiver ，广播接受者，用来接收来自系统和应用中的广播，是 Android 四大组件之一。

## 6.1. BroadcastReceiver 的两种常用类型

### 6.1.1. Normalbroadcasts：默认广播

　　发送一个默认广播使用 `Context.sendBroadcast()` 方法，普通广播对于多个接受者来说是完全异步的，通常每个接受者都无需等待即可接收到广播，接受者相互之间不会有影响。对于这种广播，接收者无法终止广播，即无法阻止其他接收者的接收动作。

### 6.1.2. orderedbroadcasts：有序广播

　　发送一个有序广播使用 `Context.sendorderedBroadcast()` 方法，有序广播比较特殊，它每次只发送到优先级较高的接受者那里，然后由优先级高的接受者再传播到优先级低的接受者那里，优先级高的接受者有能力终止这个广播。

　　在注册广播中的 < intent - filter > 中使用 `android:priority` 属性。这个属性的范围在 -1000 到 1000 ，数值越大，优先级越高。在广播接受器中使用 `setResultExtras` 方法将一个 Bundle 对象设置为结果集对象，传递到下一个接收者那里，这样优先级低的接受者可以用 `getResultExtras` 获取到最新的经过处理的信息集合。

　　使用 `sendorderedBroadcast` 方法发送有序广播时，需要一个权限参数，如果为 null 则表示不要求接受者声明指定的权限，如果不为 null 则表示接受者若要接收此广播，需声明指定权限。这样做是从安全角度考虑的，例如系统的短信就是有序广播的形式，一个应用可能是具有拦截垃圾短信的功能，当短信到来时它可以先接收到短信广播，必要时终止广播传递，这样的软件就必须声明接收短信的权限。

## 6.2. 静态和动态注册方式

　　构建 Intent ，使用 sendBroadcast 方法发出广播。接收者定义一个广播接收器，该广播接收器继承 BroadcastReceiver，并且覆盖 onReceive() 方法来接收事件。注册该广播接收器，可以在代码中注册（动态注册），也可以在 AndroidManifest.xml 配置文件中注册（静态注册）。

### 6.2.1. 两种注册方式区别

　　广播接收器注册一种有两种形式：静态注册和动态注册。

　　两者及其接收广播的区别：

　　（1）动态注册广播不是常驻型广播，也就是说广播跟随 Activity 的生命周期。注意在 Activity 结束前，移除广播接收器。静态注册是常驻型，也就是说当应用程序关闭后，如果有信息广播来，程序也会被系统调用自动运行。

　　（2）当广播为有序广播时：优先级高的先接收（不分静态和动态）。同优先级的广播接收器，动态优先于静态。当广播为默认广播时：无视优先级，动态广播接收器优先于静态广播接收器。

　　（3）同优先级的同类广播接收器，静态：先扫描的优先于后扫描的。动态：先注册优先于后注册的。

　　（4）静态注册是在 AndroidManifesy.xml 里通过< receiver > 标签声明的。不受任何组件的生命周期影响，缺点是耗电和占内存，适合在需要时刻监听使用。动态注册在代码中调用 `Context.registerReceiver()` 方法注册，比较灵活，适合在需要特定时刻监听使用。

### 6.2.2. 不同注册方式的生命周期

　　动态注册，指的是在代码中调用方法 registerReceiver 和 unregisterReceiver；它的生命周期开始于registerReceiver，结束于 unregisterReceiver，通常伴随某个 Activity 的生命周期。

　　静态注册，指的是在 AndroidManifest.xml 中注册 receiver 接收器，receiver 节点与 activity 和 service 节点是平级关系；它的生命周期开始于系统启动，结束于系统关机，在系统运行过程中，只要收到符合条件的广播，接收器便会启动工作。

## 6.3. 广播发送者向 AMS 发送广播

### 6.3.1. 广播的发送

　　广播是用 “ 意图（Intent）” 标识，定义广播的本质是定义广播所具备的 “ 意图（Intent）”，广播发送就是广播发送者将此广播的 “ 意图（Intent）” 通过 `sendBroadcast()` 方法发送出去。

### 6.3.2. 广播的类型

　　广播的类型主要分为 5 类：

* 普通广播（Normal Broadcast）
* 系统广播（System Broadcast）
* 有序广播（Ordered Broadcast）
* 粘性广播（Sticky Broadcast）
* App 应用内广播（Local Broadcast）

#### 6.3.2.1. 普通广播（Normal Broadcast）

　　开发者自身定义 Intent 的广播（最常用）。使用 `sendBroadcast()` 发送广播。

#### 6.3.2.2. 系统广播（System Broadcast）

　　Android 中内置了多个系统广播：只要涉及到手机的基本操作（如开机、网络状态变化、拍照等等），都会发出相应的广播。

　　每个广播都有特定的 Intent - Filter ( 包括具体的 action )，Android 系统广播 action 如下：

| 系统操作                                                     | action                               |
| ------------------------------------------------------------ | ------------------------------------ |
| 监听网络变化                                                 | android.net.conn.CONNECTIVITY_CHANGE |
| 关闭或打开飞行模式                                           | Intent.ACTION_AIRPLANE_MODE_CHANGED  |
| 充电时或电量发生变化                                         | Intent.ACTION_BATTERY_CHANGED        |
| 电池电量低                                                   | Intent.ACTION_BATTERY_LOW            |
| 电池电量充足（即从电量低变化到饱满时会发出广播）             | Intent.ACTION_BATTERY_OKAY           |
| 系统启动完成后（仅广播一次）                                 | Intent.ACTION_BOOT_COMPLETE          |
| 按下照相时的拍照按钮（硬件按键）时                           | Intent.ACTION_CAMERA_BUTTON          |
| 屏幕锁屏                                                     | Intent.ACTION_CLOSE_SYSTEM_DIALOGS   |
| 设备当前设置被改变时（界面语言、设备方向等）                 | Intent.ACTION_CONFIGURATION_CHANGED  |
| 插入耳机时                                                   | Intent.ACTION_HEADSET_PLUG           |
| 未正确移除 SD 卡但已取出来时（正确移除方法：设置-SD 卡和设备内存-卸载 SD 卡） | Intent.ACTION_MEDIA_BAD_REMOVAL      |
| 插入外部存储装置（如 SD 卡）                                 | Intent.ACTION_MEDIA_CHECKING         |
| 成功安装 APK                                                 | Intent.ACTION_PACKAGE_ADDED          |
| 成功删除 APK                                                 | Intent.ACTION_PACKAGE_REMOVED        |
| 重启设备                                                     | Intent.ACTION_REBOOT                 |
| 屏幕被关闭                                                   | Intent.ACTION_SCREEN_OFF             |
| 屏幕被打开                                                   | Intent.ACTION_SCREEN_ON              |
| 关闭系统时                                                   | Intent.ACTION_SHUTDOWN               |
| 重启设备                                                     | Intent.ACTION_REBOOT                 |

　　当使用系统广播时，只需要在注册广播接收者时定义相关的 action 即可，并不需要手动发送广播，当系统有相关操作时会自动进行系统广播。

#### 6.3.2.3. 有序广播

　　有序广播：发送出去的广播被广播接受者按照先后顺序接收。

　　广播接受者接收广播的顺序规则（同时面向静态和动态注册的广播接收者）：1.按照 Priority 属性值从大-小排序；2.Priority 属性相同者，动态注册的广播优先。

　　特定：1.接收广播按顺序接收；2.先接收的广播接收者可以对广播进行截断，即后接收的广播接收者不再接收到此广播；3.先接收的广播接收者可以对广播进行修改，那么后接收的广播接收者将接收到被修改后的广播。

　　具体使用：有序广播的使用过程与普通广播非常类似，差异在于广播的发送方式：`sendOrderBroadcast()`。

#### 6.3.2.4. App 应用内广播（Local Broadcast）

　　Android 中的广播可以跨 App 直接通信（exported 对于有 intent-filter 情况下默认值为 true）。

　　冲突：可能出现的问题：1.其他 App 针对性发出与当前 App intent-filter 相匹配的广播，由此导致当前 App 不断接收广播并处理；2.其他 App 注册与当前 App 一致的 intent-filter 用于接收广播，获取广播具体信息。即会出现安全性和效率性的问题。

　　解决方案：使用 App 应用内广播（Local Broadcast）。1.App 应用内广播可以理解为一种局部广播，广播的发送者和接收者都同属于一个 App。2.相比于全局广播（普通广播），App 应用内广播优势体现在：安全性高和效率高。

　　具体使用1：将全局广播设置为局部广播：1.注册广播时将 exported 属性设置为 false，使得非本 App 内部发出的此广播不被接收；2.在广播发送和接收时，增设相应权限 permission，用于权限验证；3.发送广播时指定该广播接收器所在的包名，此广播将只会发送到此包中的 App 内与之相匹配的有效广播接收器中。

　　具体使用2：使用封装好的 **LocalBroadcastManager** 类，使用方式上与全局广播几乎相同，只是注册 / 取消广播接收器和发送广播是将参数的 context 变成了 LocalBroadcastManager 的单一实例。对于 LocalBroadcastManager 方式发送的应用内广播，只能通过 LocalBroadcastManager 动态注册，不能静态注册。

#### 6.3.2.5. 粘性广播（Sticky Broadcast）

　　在 Android 5.0 (API 21)中已经失效，不建议使用。

## 6.4. 注意

　　对于不同注册方式的广播接收器回调 onReceiver(Context context,Intent intent)中的 context 返回值是不一样的：

* 对于静态注册（全局+应用内广播），回调onReceiver(Context，intent)中的 context 返回值是：广播接收者受限的 Context；
* 对于全局广播的动态注册，回调 onReceiver(Context,intent)中的 context 返回值是：Activity context；
* 对于应用内广播的动态注册（LocalBroadcastMananger 方式），回调 onReceive(Context,intent)中的 context 返回值是：Application context；
* 对于应用内广播的动态注册（非 LocalBroadcastManager 方法），回调 onReceive(context,intent)中的 context 返回值：Activity context。

## 6.5. 局部广播和全局广播的区别？分别用什么实现的？

1、本地广播：发送的广播事件**不被其他应用程序获取**，也**不能响应其他应用程序发送的广播事件**。本地广播**只能被动态注册**，不能静态注册。动态注册或方法时需要用到**LocalBroadcastManager。**

2、全局广播：发送的广播事件**可被其他应用程序获取**，也能**响应其他应用程序发送的广播事件**（可以通过 exported–是否监听其他应用程序发送的广播 在清单文件中控制） 全局广播**既可以动态注册，也可以静态注册。**

通过 LocalBroadcastManager 实现，设置 IntentFilter 的 action 过滤来实现广播的局部接收。

平时使用的 BroadcastReceiver 就是全局广播。

# 7. ContentProvider

　　ContentProvider 可以实现在应用程序之间共享数据。

　　Android 为常见的一些数据提供了默认的 ContentProvider（包括音频、视频、图片和通讯录等）。所以可以在其他应用中通过那些 ContentProvider 获取这些数据。

　　Android 所提供的 ContentProvider 都存放在 android.provider 包中。

## 7.1. ContentProvider 的 特点

1. ContentProvider 为存储和获取数据提供了统一的接口。ContentProvider 对数据进行了封装，不用关心数据存储的细节。统一了数据的访问方式。
2. 使用 ContentProvider 可以在不同的应用程序之间共享数据。
3. Android 为常见的一些数据提供了默认的 ContentProvider（包括音频、视频、图片和通讯录等）。
4. 不同于文件存储和 SharedPreferences 存储中的两种全局可读写操作模式，ContentProvider 可以选择只对哪一部分进行共享，从而保证程序中的隐私数据不会有泄漏的风险。

## 7.2. 对 ContentProvider 封装的理解

　　继承 ContentProvider 的类在 onCreate()、insert()、delete()、update()、query()、getType() 方法中实现对数据增删改查的操作，而数据的存储可以使用文件、数据库、网络等各种方式去实现。而对数据的操作使用的是 ContentResolver 类，不管 ContentProvider 如何对数据进行实质操作，ContentReselver 的使用都是一样的。将实现与使用进行了分割，完成了对数据的封装，也统一了对数据的使用方式。

## 7.3. Uri 介绍

　　每一个 ContentProvider 都拥有一个公共的 URI，这个 URI 用于表示这个 ContentProvider 所提供的数据。

![](J:/zhangmiao/android_learning_notes/Android/components/ContentProvider/image/Uri示例图.jpg)

　　A：scheme，标准前缀，用来说明一个 ContentProvider 控制这些数据，无法改变。ContentProvider 的 scheme 已经由 Android 所规定，scheme 为：content://。

　　B：主机名，URI 的标识，用来唯一标识这个 ContentProvider ，外部调用者可以根据这个标识来找到它。它定义了是哪个 ContentProvider 提供这些数据。对于第三方应用程序，为了保证 URI 标识的唯一性，它必须是一个完整的、小写的类名。这个标识在元素的 authorities 属性中说明，一般是定义该 ContentProvider 的包类的名称。

　　C：路径（path），就是要操作的数据，路径的构建根据业务而定。

　　D：如果 URI 中包含表示需要获取的记录的 ID，则就返回该 id 对应的数据，如果没有 ID，就表示返回全部。

　　在配置 ContentProvider 的时候，最重要的就是指定它的 **authorities** 属性了，只有配置了这个属性，第三方应用程序才能通过它来找到这个 ContentProvider，authorities 就是 ContentProvider 的身份证。另外一个属性 **multiprocess** 是一个布尔值，它表示这个 ContentProvider 是否可以在每个客户进程中创建一个实例，这样做的目的是为了减少进程间通信的开销。

## 7.4. 操作 Uri 的工具类

　　Uri 代表了要操作的数据，所以有时需要解析 Uri，并从 Uri 中获取数据。

　　Android 系统提供了两个用于操作 Uri 的工具类，分别是 UriMatcher 和 ContentUris。

### 7.4.1. UriMatcher 类

　　UriMatcher 类用于匹配 Uri。

#### 7.4.1.1. UriMatcher 的用法

1. 把需要匹配的 Uri 路径使用 UriMatcher 的 addURI 方法全部注册上。

```java
public void addURI(String authority, //Uri 的 B 部分
                   String path, //Uri 的 C 部分
                   int code); //匹配成功的返回码
```

2. 使用 UriMatcher 的 match 方法进行匹配。如果匹配就返回匹配码。

```java
public int match(Uri uri);
```

​		UriMatch.NO_MATCH（-1） 表示当不匹配任何路径时的返回码。

### 7.4.2. ContentUris

​		ContentUris 类用于操作 Uri 路径后面的 ID 部分。

#### 7.4.2.1. ContentUris 的方法

1. `withAppendedId(Uri,id)` 方法用于为路径加上 ID 部分。

2. `parseId(Uri)` 方法用于从路径中获取 ID 部分。

## 7.5. ContentProvider 与 ContentResolver

​		ContentProvider 负责组织应用程序的数据，向其他应用程序提供数据。

​		ContentResolver 负责获取 ContentProvider 提供的数据，修改、添加、删除、更新数据等。

### 7.5.1. 使用 ContentProvider 共享数据

#### 7.5.1.1. ContentProvider 类的主要方法

##### 7.5.1.1.1 onCreate

​		pubilc boolean onCreate()

​		该方法在 ContentProvider 创建后就会被调用，Android 开机后，ContentProvider 在其他应用第一次访问它时才会被创建。通常会在这里完成对数据库的创建和升级操作。返回 true，表示 ContentProvider 初始化成功，false 则失败。

##### 7.5.1.1.2. insert

​		public Uri insert(Uri uri, ContentValues values)

​		该方法用于添加数据。

##### 7.5.1.1.3. delete

​		public int delete(Uri uri, String selection,String[] selectionArgs)

​		该方法用于删除数据。

##### 7.5.1.1.4. update

​		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)

​		该方法用于更新数据。

##### 7.5.1.1.5. query

​		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)

​		该方法用于获取数据。

##### 7.5.1.1.6. getType

​		public String getType(Uri uri)

​		该方法用于返回 Uri 所代表数据的 MIME 类型。

#### 7.5.1.2. MIME 类型

​		指定某个扩展名的文件用某种应用程序来打开。

​		MIME 类型组成：类型 + 子类型。

　　如果操作的数据属于集合类型，那么 MIME 类型字符串应该以 vnd.android.cursor.dir/ 开头。

​		如果操作的数据属于非集合类型，那么 MIME 类型字符串应该以 vnd.android.cursor.item/ 开头。

###  7.5.2. ContentResolver 操作 ContentProvider 中的数据

​		当外部应用需要对 ContentProvider 中的数据进行添加、删除、修改和查询操作时，可以使用 ContentResolver 类来完成。

​		要获取 ContentResolver 对象，可以使用 Activity 提供的 getContentResolver() 方法。

#### 7.5.2.1. ContentResolver 的方法

##### 7.5.2.1.1. insert

​		public Uri insert(Uri uri, ContentValues values)

​		该方法用于往 ContentProvider 添加数据。

##### 7.5.2.1.2. delete

​		public int delete(Uri uri, String selection, String[] selectionArgs)

​		该方法用于从 ContentProvider 删除数据。

##### 7.5.2.1.3. update

​		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)

​		该方法用于更新 ContentProvider 中的数据。

##### 7.5.2.1.4. query

​		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)

​		该方法用于从 ContentProvider 中获取数据。

​		方法的第一个参数为 Uri，代表要操作的 ContentProvider 和对其中的什么数据进行操作。

#### 7.5.2.2 其他查询方法

​		查询 ContentProvider 的方法有两个：ContentResolver 的 `query()` 和 Activity 对象的 `managerQuery()` ，二者接收的参数均相同，返回的都是 Cursor 对象，唯一不同的是使用 `managerQuery()` 方法可以让 Activity 来管理 Cursor 的生命周期。

　　被管理的 Cursor 会在 Activity 进入暂停状态的时候调用自己的 `deactivate()` 方法进行卸载，而在 Activity 进入暂停状态时会调用自己的 request 方法重新查询生成的 Cursor 对象。如果一个未被管理的 Cursor 对象想被 Activity 管理，可以调用 Activity 的 startManagerCursor 方法来实现。但是这个方法已经不推荐使用了。

## 7.6. 监听 ContentProvider 中数据的变化

​		如果 ContentProvider 的访问者需要知道 ContentProvider 中的数据发生变化，可以在 ContentProvider 发生数据变化时调用 `getContentResolver().notifyChange(uri, null)` 来通知注册在此 Uri 上的访问者。

​		如果 ContentProvider 的访问者需要得到数据变化通知，必须使用 ContentObserver 对数据（数据采用 uri 描述 ）进行监听，当监听到数据数据变化通知时，系统就会调用 ContentObserver 的 `onChange()` 方法。

## 7.7. ContentProvider 的生命周期

​		contentProvider 的生命周期、理解应该跟进程一样，它作为系统应用组件、其生命周期应该跟 app 应用的生命周期类似，只是它属于系统应用、所以随系统启动而初始化，随系统关机而结束；但也存在其他状态下结束进程、比如说系统内存不够时，进行内存回收、会根据生成时间态、用户操作等情况进行是否内存回收。

# 8. Profile（性能分析器）

Android Profiler分为三大模块： cpu、内存 、网络。

CPU 分析器可帮助实时检查应用程序的 CPU 使用情况和线程活动，并记录方法跟踪，以便可以优化和调试应用程序的代码。

内存分析器是 Android Profiler 中的一个组件，它可以帮助识别内存泄漏和内存溢出，从而导致存根、冻结甚至应用程序崩溃。它显示了应用程序内存使用的实时图，捕获堆转储、强制垃圾收集和跟踪内存分配。

网络分析器在时间轴上显示实时网络活动，显示发送和接收的数据，以及当前连接的数量。这让您可以检查应用程序如何和何时传输数据，并适当地优化底层代码。