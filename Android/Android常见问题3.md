# Android 的常见问题 3

# 1. ANR

　　ANR 全称是 Application Not Responding，意思就是应用程序未响应。如果一个应用无法响应用户的输入，系统就会弹出一个 ANR 对话框，用户可以自行选择继续等待或者是停止当前程序。

# 2. ANR 的发生原因

1. 代码自身引起，例如：

   * 主线程阻塞、IOWait 等；


   - 主线程进行耗时计算；
   - 错误的操作，比如调用了 Thread.wait 或者 Thread.sleep 等。

2. 其他进程间接引起，例如：

   * 当前应用进程进行进程间通信请求其他进程，其他进程的操作长时间没有反馈；
   * 其他进程的 CPU 占用率高，使得当前应用进程无法抢占到 CPU 时间片。

# 3. 发生 ANR 的条件

　　Andriod 系统中，ActivityManangerService(简称 AMS) 和 WindowManangerService(简称 WMS) 会检测 App 的响应事件，如果 App 在特定时间无法响应屏幕触摸或键盘输入事件，或者特定事件没有处理完毕，就会出现 ANR。

* InputDispatching Timeout：输入事件分发或屏幕触摸事件超时 5s 未响应完毕；
* BroadcastQueue Timeout：前台广播在 10秒 内、后台广播在 60 秒内未执行完成；
* Service Timeout：前台服务在 20 秒内、后台服务在 200 秒内未执行完成；
* ContentProvider Timeout：内容提供者，在 publish 超时 10s；

# 4. 分析 ANR 的方法

#### ANR 分析方法一：Log

![](J:/zhangmiao/android_learning_notes/Android/optimize/image/ANR_LOG.png)
　　可以看到 logcat 清晰地记录了 ANR 发生的时间，发生 ANR 所在的报名、类名以及线程的 tid 和一句话概括原因：WaitingInMainSignalCatcherLoop，大概意思为主线程等待异常。

　　最后一句 The application may be doing too much work on its main thread. 告知可能在主线程做了太多的工作。

#### ANR 分析方法二：traces.txt

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

#### ANR 分析方法三：Java 线程调用分析

　　通过 JDK 提供的命令可以帮助分析和调试 Java 应用，命令是：

```java
jstack {pid}
```

　　其中 pid 可以通过 jps 命令获得，jps 命令会列出当前系统中运行的所有 Java 虚拟机进程，比如：

```java
7266 Test
7267 Jps
```

#### ANR 分析方法四：DDMS 分析 ANR 问题

* 使用 DDMS-----Update Threads 工具
* 阅读 Update Threads 的输出

# 5. 如何避免 ANR

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

# 6. BroadcastReceiver

# 7. ContentProvider

# 8. Profile（性能分析器）

Android Profiler分为三大模块： cpu、内存 、网络。

CPU 分析器可帮助实时检查应用程序的 CPU 使用情况和线程活动，并记录方法跟踪，以便可以优化和调试应用程序的代码。

内存分析器是 Android Profiler 中的一个组件，它可以帮助识别内存泄漏和内存溢出，从而导致存根、冻结甚至应用程序崩溃。它显示了应用程序内存使用的实时图，捕获堆转储、强制垃圾收集和跟踪内存分配。

网络分析器在时间轴上显示实时网络活动，显示发送和接收的数据，以及当前连接的数量。这让您可以检查应用程序如何和何时传输数据，并适当地优化底层代码。