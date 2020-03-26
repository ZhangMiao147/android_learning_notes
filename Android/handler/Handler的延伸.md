# Handler 的延伸

## 1. 在子线程中进行 UI 操作

　　除了 handle 的 sendMessage() 方法外，还有以下几种方式可以在子线程中进行 UI 操作：

1. Handler 的 post() 方法
2. View 的 post() 方法
3. Activity 的 runOnUiThread() 方法

### 1.1. Handler#post()

```java
    public final boolean post(Runnable r)
    {
       return  sendMessageDelayed(getPostMessage(r), 0);
    }
    private static Message getPostMessage(Runnable r) {
        Message m = Message.obtain();
        m.callback = r;
        return m;
    }
```

　　还是调用了 sendMessageDelayed() 去发送一条消息，并且还使用了 getPostMessage() 方法将 Runnable 对象转换成一条消息。将消息的 callback 字段指定为传入的 Runnable 对象，在 Handler 的 dispatchMessage() 方法中，如果 Message 的 callback 为 null 才会去调用 handleMessage() 方法，否则就调用 handleCallBack 方法：

```java
private static void handleCallback(Message message) {
    message.callback.run();
}
```

　　就是直接调用了传入的 Runnable 对象的 run() 方法。

　　因此在子线程中通过 handler 的 post() 方法进行 UI 操作：

```java
public class MainActivity extends Activity {
 
	private Handler handler;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// 在这里进行UI操作
					}
				});
			}
		}).start();
	}
}
```

### 1.2. View#post

```java
    public boolean post(Runnable action) {
        final AttachInfo attachInfo = mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler.post(action);
        }

        // Postpone the runnable until we know on which thread it needs to run.
        // Assume that the runnable will be successfully placed after attach.
        getRunQueue().post(action);
        return true;
    }
```

　　View 的 post 方法就是调用了 Handler 中 post() 方法。

### 1.3. Activity#runOnUiThread()

```java
   public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
```

　　如果当前的线程不等于 UI 线程（主线程），就去调用 Handler 的 post() 方法，否则就直接调用 Runnable 对象的 run() 方法。 

## 2. 为什么要有 Handler 机制

　　如果想要在子线程更新 UI，就需要使用 Handler 机制传递消息到主线程（UI 线程），这是因为 Android 是单线程模型，所以子线程中是不能更新 UI 的，而 Android 为什么要做成单线程模型，这是因为多线程并发访问 UI 可能会导致 UI 空间处于不可预期的状态，如果加锁，虽然能解决，但是缺点也很明显：1. 锁机制让 UI 访问逻辑变得复杂；2. 加锁导致效率低下。

## 3. Handler 引起的内存泄漏原因以及最佳解决方案

　　Handler 允许发送延时消息，如果在延时期间用户关闭了 Activity，那么该 Activity 会泄漏。

　　这个泄漏是因为 Message 会持有 Handler，而又因为 Java 的特性，内部类会持有外部类，使得 Activity 会被 Handler 持有，这样最终就导致 Activity 泄漏。

　　解决该问题的最有效的方法是：将 Handler 定义成静态的内部类，在内部持有 Activity 的弱引用，并及时移除所有消息。

　　而单纯的在 onDestory 中移除消息并不保险，因为 onDestory 并不一定执行。

　　让 Handler 持有 Activity 的弱引用：

```java
// Handler 是静态的
private static class SafeHandler extends Handler {
		//持有 Activity 的弱引用
    private WeakReference<HandlerActivity> ref;

    public SafeHandler(HandlerActivity activity) {
        this.ref = new WeakReference(activity);
    }

    @Override
    public void handleMessage(final Message msg) {
        HandlerActivity activity = ref.get();
        if (activity != null) {
            activity.handleMessage(msg);
        }
    }
}
```

　　在 Activity 的 onDestroy() 的方法里移除所有消息：

```java
@Override
protected void onDestroy() {
  // 移除并停止 handler 的消息
  safeHandler.removeCallbacksAndMessages(null);
  super.onDestroy();
}
```

　　这样，就能完全避免内存泄漏了。

## 4. 为什么能在主线程直接使用 Handler，而不需要创建 Looper？

　　Android 上一个应用的入口应该是 ActivityThread，和普通的 Java 类一样，入口是一个 main  方法。

　　在 ActivityThread.main() 方法中有如下代码：

```java
    public static void main(String[] args) {
        ...
      	// 创建 Looper 和 MessageQueue 对象，用来处理主线程消息
        Looper.prepareMainLooper();
      	// 创建 ActivityThread 对象
        ActivityThread thread = new ActivityThread();
      	// 建立 Binder 通道（创建新线程）
        thread.attach(false);

        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        ...
        // 消息循环运行
        Looper.loop();
        
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }
```

　　Looper.prepareMainLooper() 代码如下：

```java
    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper();
        }
    }
```

　　可以看到在 ActivityThread 里调用了 Looper.prepareMainLooper() 方法创建了主线程的 Looper，并且调用了 loop() 方法，所以可以在主线程中直接使用 Handler 了。

　　主线程（UI）的 Looper 启动后，就可以处理子线程和其他组件发来的消息了。

## 5. 主线程的 Looper 不允许退出

　　如果尝试退出主线程的 Looper，会得到以下错误：

```java
Caused by: java.lang.IllegalStateException: Main thread not allowed to quit.
  at android.os.MessageQueue.quit(MessageQueue.java:415)
  at android.os.Looper.quit(Looper.java:240)
```

　　原因是主线程不允许退出，退出就意味 App 要挂。

## 6. 为什么主线程不会因为 Looper.loop() 里的死循环卡死或者不能处理其他事务？

### 6.1. 为什么不会卡死？

　　handler 机制是使用 pipe 来实现的，主线程没有消息处理时会阻塞在管道的读端。

　　binder 线程会往主线程消息队列里添加消息，然后往管道写端写一个字段，这样就能唤醒主线程从管道读端返回，也就是说 queue.next() 会调用返回。

　　主线程大多数都是出于休眠状态，并不会消耗大量 CPU 资源。

### 6.2. 既然是死循环又如何去处理其他事务呢？

　　答案是通过创建新线程的方式。

　　在 main() 方法里调用了 thread.attach(false)，这里便会创建一个 Binder 线程（具体是指 ApplicationThread，Binder 的服务端，用于接收系统服务 AMS 发送来的事件），该 Binder 线程通过 Handler 将 Message 发送给主线程。

　　ActivityThread 对应的 Handler 是一个内部类 H，里面包含了启动 Acitivity、处理 Activity 生命周期等方法。

## 7. Handler 里的 Callback 能干什么？

　　先看一下 Handler.dispatchMessage() 方法：

```java
    public void dispatchMessage(Message msg) {
        // 这里的 callback 是 Runnable 
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            // 如果 callback 处理了该 msg 并且返回 true，就不会再回调 handleMessage
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }
```

　　可以看到 Handler.Callback 有优先处理消息的权利，当一条消息被 Callback 处理并拦截（返回 true），那么 Handler 的 handleMessage(msg) 方法就不会被调用了。

　　如果 Callback 处理了消息，但是并没有拦截，那么就意味着一个消息可以同时被 Callback 以及 Handler 处理。

　　就可以利用 Callback 这个拦截机制来拦截 Handler 的消息。

　　场景：Hook ActivityThread.mH，在 ActivityThread 中有个成员变量 mH，它是个 Handler，又是个极其重要的类，几乎所有的插件化框架都使用了这个方法。

## 8. 创建 Message 实例的最佳方式

　　由于 Handler 极为常用，所以为了节省开销，Android 给 Message 设计了回收机制，所以在使用的时候尽量复用 Message，减少内存消耗。

　　方法有二：

1. 通过 Message 的静态方法 `Message.obtain();` 获取。
2. 通过 Handler 的共有方法 `handler.obtainMessage()`。

## 9. 子线程里弹 Toast 的正确姿势

　　如果尝试在子线程里直接区弹 Toast 的时候，会 crash：

```java
java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
```

　　本质上是因为 Toast 的实现依赖于 Handler，按子线程使用 Handler 的要求修改即可，同理的还有 Dialog。

　　正确实例代码：

```java
new Thread(new Runnable() {
  @Override
  public void run() {
    // 创建 Looper
    Looper.prepare();
    Toast.makeText(HandlerActivity.this, "不会崩溃啦！", Toast.LENGTH_SHORT).show();
    // 开启 Looper
    Looper.loop();
  }
}).start();
```

　　在调用 Looper.loop() 之前，确保已经调用了 prepared() 方法，并且可以调用 quite 方法结束循环。

## 10. 妙用 Looper 机制

　　可以利用 Looper 的机制来做一些事情：

1. 将 Runnable post 到主线程执行；
2. 利用 Looper 判断当前线程是否是主线程。

　　代码如下：

```java
public final class MainThread {

    private MainThread() {
    }

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void run(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        }else{
          	// 将 runnable 放到主线程去运行
            HANDLER.post(runnable);
        }
    }

  	// 判断是否是主线程
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
```


## 11. 参考文章
[Android消息机制的原理及源码解析](https://www.jianshu.com/p/f10cff5b4c25)

[Handler 都没搞懂，拿什么去跳槽啊？](https://juejin.im/post/5c74b64a6fb9a049be5e22fc)

[Android Handler 消息机制（解惑篇）](https://juejin.im/entry/57fb3c53128fe100546ea4f2)

[Android异步消息处理机制完全解析，带你从源码的角度彻底理解](https://blog.csdn.net/guolin_blog/article/details/9991569)

