# 关于 Handler

## 1. 消息机制概述

### 1.1. 消息机制的模型

　　消息机制主要包含：MessageQueue、Handler 和 Looper 这三大部分，以及 Message。

　　Message：需要传递的消息，可以传递数据。

　　MessageQueue：消息队列，但是它的内部实现并不是用的队列，实际上是通过一个单链表的数据结构来维护消息列表，因为单链表在插入和删除上比较有优势。主要功能向消息池投递消息（MessageQueue.enqueueMessage）和取走消息池的消息（MessageQueue.next）。

　　Handler：消息辅助类，主要功能向消息池发送各种消息事件（Hnadler sendMessage）和处理相应消息事件（Handler.handleMessage）。

　　Looper：不断循环执行（Looper.loop），从MessageQueue 中读取消息，按分发机制将消息分发给目标处理者。

#### 消息机制的架构
　　消息机制的运行流程：在子线程执行完耗时操作，当 Handler 发送消息时，将会调用 MessageQueue.enqueueMessae，向消息队列中添加消息。当通过 Looper.loop 开启循环后，会不断地从线程池中读取消息，即调用 MessageQueue.next，然后调用目标 Handler(即发送该消息的 Handler)的 dispatchMessage 方法传递消息，然后返回到 Handler 所在线程，目标 Handler 收到消息，调用 handleMessage 方法，接收消息，处理消息。

![](image/消息机制的框架.png)

　　Message、Handler 和 Looper 三者之间的关系：每个线程中只能存在一个 Looper，Looper 是保存在 ThreadLocal 中的。主线程（UI 线程）已经创建了一个 Looper，所以在主线程不需要再创建 Looper，但是在其他线程中需要创建 Looper。每个线程中可以有多了 Handler，即一个 Looper 可以处理来自多个 Handler 的消息。Looper 中维护一个 MessageQueue，来维护消息队列，消息队列中的 Message 可以来自不同的 Handler。

　　消息机制的整体架构图：
![](image/消息架构图.png)

　　Looper 有一个 MessageQueue 消息队列；MessageQueue 有一组待处理的 Message；Message 中记录发送和处理消息的 Handler；Handler 中有 Looper 和 MessageQueue。

## 消息机制的源码解析

### Looper

　　要想使用消息机制，首先要创建一个 Looper。

#### 初始化 Looper

　　无参情况下，默认调用 prepare(true)，表示的是这个 Looper 可以退出，而对于 false 的情况则表示当前 Looper 不可以退出。

```java
public final class Looper {
    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }
}
```
　　这里看出，不能重复创建 Looper，只能创建一个。创建 Looper，并保存在 ThreadLocal。其中 ThreadLocal 是线程本地存储区（Thred Local Storage，简称为 TLS），每一个线程都有自己的私有的本地存储区域，不同线程之间彼此不能访问对方的 TSL 区域。

#### 开启 Looper

```java
public final class Looper {
    /**
     * Run the message queue in this thread. Be sure to call
     * {@link #quit()} to end the loop.
     */
    public static void loop() {
		//获取 TLS 存储的 Looper 对象
        final Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
		//获取 Looper 对象中的消息队列
        final MessageQueue queue = me.mQueue;

        // Make sure the identity of this thread is that of the local process,
        // and keep track of what that identity token actually is.
        Binder.clearCallingIdentity();
        final long ident = Binder.clearCallingIdentity();

		//进入 loop 的主循环方法
        for (;;) {
			//可能会阻塞，因为 next() 方法可能会无限循环
            Message msg = queue.next(); // might block
			//消息为空，则退出循环
            if (msg == null) {
                // No message indicates that the message queue is quitting.
                return;
            }

            // This must be in a local variable, in case a UI event sets the logger
			//默认为 null，可通过 setMessageLogging() 方法来指定输出，用于 debug 功能
            final Printer logging = me.mLogging;
            if (logging != null) {
                logging.println(">>>>> Dispatching to " + msg.target + " " +
                        msg.callback + ": " + msg.what);
            }

            final long traceTag = me.mTraceTag;
            if (traceTag != 0 && Trace.isTagEnabled(traceTag)) {
                Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
            }
            try {
				//获取 msg 的目标 Handler，然后用于分发 Message
                msg.target.dispatchMessage(msg);
            } finally {
                if (traceTag != 0) {
                    Trace.traceEnd(traceTag);
                }
            }

            if (logging != null) {
                logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
            }

            // Make sure that during the course of dispatching the
            // identity of the thread wasn't corrupted.
            final long newIdent = Binder.clearCallingIdentity();
            if (ident != newIdent) {
                Log.wtf(TAG, "Thread identity changed from 0x"
                        + Long.toHexString(ident) + " to 0x"
                        + Long.toHexString(newIdent) + " while dispatching to "
                        + msg.target.getClass().getName() + " "
                        + msg.callback + " what=" + msg.what);
            }

            msg.recycleUnchecked();
        }
    }
}
```

　　loop() 进入循环模式，不断重复下面的操作。读取 MessageQueue 的下一条 Message，如果为空则退出循环，不为空则把 Message 分发给相应的 target。

　　当 next() 取出下一条消息时，队列中已经没有消息时，next() 会无限循环，产生阻塞。等待 MessageQueue 中加入消息，然后重新唤醒。

　　主线程中不需要自己创建 Looper，这是由于在程序启动的时候，系统已经自动调用了 Looper.prepare() 方法。

　　查看 ActivityThread 中的 main() 方法：

```java
public final class ActivityThread {
    public static void main(String[] args) {
        ...
        Looper.prepareMainLooper();
		...
        Looper.loop();
		...
    }
}
```
　　其中 prepareMainLooper() 方法会调用 prepare(false) 方法。

### Handler

#### 创建 Handler

```java
    public Handler() {
        this(null, false);
    }

    public Handler(@Nullable Callback callback) {
        this(callback, false);
    }

    public Handler(@NonNull Looper looper) {
        this(looper, null, false);
    }

    public Handler(@NonNull Looper looper, @Nullable Callback callback) {
        this(looper, callback, false);
    }

    @UnsupportedAppUsage
    public Handler(boolean async) {
        this(null, async);
    }

    public Handler(@Nullable Callback callback, boolean async) {
        if (FIND_POTENTIAL_LEAKS) {
            final Class<? extends Handler> klass = getClass();
            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                    (klass.getModifiers() & Modifier.STATIC) == 0) {
                Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                    klass.getCanonicalName());
            }
        }
		// 必须先执行 Looper.prepare()，才能获取 Looper 对象，否则为 null。
        mLooper = Looper.myLooper(); //从当前线程的 TLS 中获取 Looper 对象
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread " + Thread.currentThread()
                        + " that has not called Looper.prepare()");
        }
        mQueue = mLooper.mQueue; // 消息队列，来自 Looper 对象
        mCallback = callback; // 回调方法
        mAsynchronous = async; // 设置消息是否为异步处理方式
    }
	// 设置了 Looper，用于非转线程的 Handler 创建
    @UnsupportedAppUsage
    public Handler(@NonNull Looper looper, @Nullable Callback callback, boolean async) {
        mLooper = looper;
        mQueue = looper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }
```

　　对于 Handler 的午餐构造方法，默认采用当前线程 TLS 中的 Looper 对象，并且 callback 回调方法为 null，且消息为同步处理方式。

　　只要执行的 Looper.prepare() 方法，那么便可以获取有效的 Looper 对象。

### 发送消息

　　发送消息有几种方式，但是归根结底都是调用了 sendMessageAtTime() 方法。

　　在子线程中通过 Handler 的 post() 方式或 send() 方式发送消息，最终都是调用了 sendMessageAtTime() 方法。

#### post 方法

```java
    public final boolean post(@NonNull Runnable r) {
       return  sendMessageDelayed(getPostMessage(r), 0);
    }

    public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r), uptimeMillis);
    }

    public final boolean postAtTime(
            @NonNull Runnable r, @Nullable Object token, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r, token), uptimeMillis);
    }

    public final boolean postDelayed(@NonNull Runnable r, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r), delayMillis);
    }
    
    /** @hide */
    public final boolean postDelayed(Runnable r, int what, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r).setWhat(what), delayMillis);
    }

    public final boolean postDelayed(
            @NonNull Runnable r, @Nullable Object token, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r, token), delayMillis);
    }
```

#### send 方法

```java
    public final boolean sendMessage(@NonNull Message msg) {
        return sendMessageDelayed(msg, 0);
    }
    
    public final boolean sendEmptyMessage(int what)
    {
        return sendEmptyMessageDelayed(what, 0);
    }
    
    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
    }

```

　　就连子线程中调用 Activity 中的 runOnUiThread() 中更新 UI，其实也是发送消息通知主线程更新 UI，最终也会调用 sendMessageAtTime() 方法。

```java
   public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
```

　　如果当前的线程不等于 UI 线程（主线程），就去调用 Handler 的 post() 方法，最终会调用 sendMessageAtTime() 方法，否则就直接调用 Runnable 对象的 run() 方法。

#### sendMeeageAtTime

```java
    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        // 其中 mQueue 是消息队列，从 Looper 中获取
        MessageQueue queue = mQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            Log.w("Looper", e.getMessage(), e);
            return false;
        }
        // 调用 enqueueMessage 方法
        return enqueueMessage(queue, msg, uptimeMillis);
    }

    private boolean enqueueMessage(@NonNull MessageQueue queue, @NonNull Message msg,
            long uptimeMillis) {
        msg.target = this;
        msg.workSourceUid = ThreadLocalWorkSource.getUid();

        if (mAsynchronous) {
            msg.setAsynchronous(true);
        }
        // 调用 MessageQueue 的 enqueueMessage 方法
        return queue.enqueueMessage(msg, uptimeMillis);
    }
```

　　可以看到 sendMessageAtTime() 方法的作用很简单，就是调用 MessageQueue 的 enqueueMessage() 方法，往消息队列中添加一个消息。

　　接下来看 MessageQueue 的 enqueueMessage() 方法的具体执行逻辑。

#### MessageQueue#enqueueMessage

```java
    boolean enqueueMessage(Message msg, long when) {
        // 每一个 Message 必须有一个 target
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        }
        if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        }

        synchronized (this) {
            if (mQuitting) { //正在退出时，回收 msg，加入到消息池
                IllegalStateException e = new IllegalStateException(
                        msg.target + " sending message to a Handler on a dead thread");
                Log.w(TAG, e.getMessage(), e);
                msg.recycle();
                return false;
            }

            msg.markInUse();
            msg.when = when;
            Message p = mMessages;
            boolean needWake;
            if (p == null || when == 0 || when < p.when) {
                // p 为 null （代表 MessageQueue 没有消息）或者 msg 的触发时间是队列中最早的，则进入该分支
                // New head, wake up the event queue if blocked.
                msg.next = p;
                mMessages = msg;
                needWake = mBlocked;
            } else {
                // Inserted within the middle of the queue.  Usually we don't have to wake
                // up the event queue unless there is a barrier at the head of the queue
                // and the message is the earliest asynchronous message in the queue.
                // 将消息按时间顺序插入到 MessageQueue。一般地，不需要唤醒事件队列，除非消息对头存在 barrier，并且同时 Message 是队列中最早的异步消息。
                needWake = mBlocked && p.target == null && msg.isAsynchronous();
                Message prev;
                for (;;) {
                    prev = p;
                    p = p.next;
                    if (p == null || when < p.when) {
                        break;
                    }
                    if (needWake && p.isAsynchronous()) {
                        needWake = false;
                    }
                }
                msg.next = p; // invariant: p == prev.next
                prev.next = msg;
            }

            // We can assume mPtr != 0 because mQuitting is false.
            if (needWake) {
                nativeWake(mPtr);
            }
        }
        return true;
    }
```

　　MessageQueue 是按照 Message 触发时间的先后顺序排列的，队头的消息是将要最早触发的消息。当有消息需要加入消息队列时，会从队列头开始遍历，直到找到消息应该插入的合适位置，以保证所有消息的时间顺序。

### 获取消息




## 参考文章
[Android消息机制的原理及源码解析](https://www.jianshu.com/p/f10cff5b4c25)

[Handler 都没搞懂，拿什么去跳槽啊？](https://juejin.im/post/5c74b64a6fb9a049be5e22fc)

[Android Handler 消息机制（解惑篇）](https://juejin.im/entry/57fb3c53128fe100546ea4f2)

[Android异步消息处理机制完全解析，带你从源码的角度彻底理解](https://blog.csdn.net/guolin_blog/article/details/9991569)

[Android的消息机制之ThreadLocal的工作原理](https://blog.csdn.net/singwhatiwanna/article/details/48350919)

