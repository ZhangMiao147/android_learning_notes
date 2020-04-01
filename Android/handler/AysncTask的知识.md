# AsyncTask 的知识

　　Android UI 是线程不安全的，如果想要在子线程里进行 UI 操作，就需要借助 Android 的异步消息处理机制 ，为了更加方便在子线程中更新 UI 元素，Android 5.1 版本就引入了一个 AsyncTask 类，使用它就可以非常灵活方便的从子线程切换到 UI 线程。

　　AsyncTask 内部封装了 Thread 和 Handler ，可以在后台进行计算并且把计算的结果及时更新到 UI 上，而这些正是 Thread + Handler 所做的事情，AsyncTask 的作用就是简化 Thread + Handler，能够通过更少的代码来完成一样的功能。

　　AsyncTask 只是简化 Thread + Handler 而不是替代，实际上太饿替代不了。

## 基本用法

### 使用规则

1. AsyncTask 的类必须在 UI 线程加载（从 4.1 开始系统回自动完成）。
2. AsyncTask 对象必须在 UI 线程创建。
3. execute 方法必须在 UI 线程调用。
4. 不要再程序中去直接调用 onPreExecute()、onPostExecute()、doInBackground()、onProgressUpdate() 方法。
5. 一个 AsyncTask 对象只能执行一次，即只能调用一次 execute() 方法，否则会报运行时异常。
6. AsyncTask 不是被设计为处理耗时操作的，耗时上限为几秒钟，如果要做长耗时操作，强烈建议使用 Executoe、ThreadPoolExecutor 以及 FutureTask。
7. 在 1.6 之前，AsyncTask 是串行执行任务的，1.6 的时候 AsyncTask 开始采用线程池处理并行任务，但是从 3.0 开始，为了避免 AsyncTask 所带来的并发错误，AsyncTask 又采用一个线程来串行执行任务。

　　AsyncTask 是一个抽象类，所以想要使用它，就必须要创建一个子类来继承它。

　　在继承时，可以为 AysncTask 类执行三个泛型参数，这三个参数的用途如下：

* params：在执行 AsyncTask 时需要传入的参数，可用于在后台任务中使用。
* Progress：后台任务执行时，如果需要在界面上显示当前的进度，则使用这里指定的泛型作为返回值类型。
* Result：当任务执行完毕后，如果需要对结果进行返回，则使用这里指定的泛型作为返回值类型。

　　一个最简单的自定义 AsyncTask 可以写成如下方式：

```java
class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
	...
}
```

　　把 AsyncTask 的第一个泛型参数指定为 void，表示在执行 AsyncTask 的时候不需要传入参数给后台任务。第二个泛型参数指定为 Integer，表示使用整型数据来作为进度显示单位。第三个泛型参数指定为 Boolean，则表示使用布尔型数据来反馈执行结果。

　　还需要重写 AsyncTask 中的几个方法才能完成对任务的定制。经常需要去重写的方法有以下四个：

1. onPreExecute

   这个方法会在后台任务开始执行之前调用，用于进行一些界面上的初始化操作，比如显示一个进度条对话框等。

2. doInBackground(Params ...)

   这个方法中的所有代码都会在子线程中运行，应该在这里处理所有的耗时任务。任务一旦完成就可以通过 return 语句来将任务的执行结果进行返回，如果 AsyncTask 的第三个泛型参数执行的 Void，就可以不反悔任务执行结果。注意，在这个方法中时不可以进行 UI 操作的，如果需要更新 UI 元素，比如说反馈当前任务的执行进度，可以调用 publishProgress(Progress,,,) 方法来完成。

3. onProgressUpdate(Progress...)

   当在后台任务中调用了 publishProgress(Progress...) 方法后，这个方法就很快会被调用，方法中携带的参数就是在后台任务中传递过来的。这个方法中可以对 UI 进行操作，利用参数中的数值就可以对界面元素进行相应的更新。

　　一个完整的自定义 AsyncTask ：

```java

class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
 
	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}
 
	@Override
	protected Boolean doInBackground(Void... params) {
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
		progressDialog.setMessage("当前下载进度：" + values[0] + "%");
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

　　如果想要启动任务，只需要简单的调用 new DownTask().execute() 即可。



## 源码分析

### AsyncTask 构造方法

```java
   public AsyncTask(@Nullable Looper callbackLooper) {
        mHandler = callbackLooper == null || callbackLooper == Looper.getMainLooper()
            ? getMainHandler()
            : new Handler(callbackLooper);

        mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                Result result = null;
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    //noinspection unchecked
                    result = doInBackground(mParams);
                    Binder.flushPendingCommands();
                } catch (Throwable tr) {
                    mCancelled.set(true);
                    throw tr;
                } finally {
                    postResult(result);
                }
                return result;
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    android.util.Log.w(LOG_TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occurred while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }
```

　　初始化了三个变量，mHandler、mWorker 和 mFuture，并在初始化 mFutire 的时候将 mWorker 作为参数传入。mWorker 是一个 Callable 对象，mFuture 是一个 FutureTask 对象。

### AsyncTask#execute

　　如果想要启动某一个任务，就需要调用该任务的 execute() 方法。

```java
    @MainThread
    public final AsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    @MainThread
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;
		// 最先调用了 onPreExecutor 方法
        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }
```

　　可以看到，调用了 onPreExecute() 方法，因此证明了 onPreExecute() 方法会第一个得到执行。

　　接着调用了 Executor 的 execute() 方法，并将前面初始化的 mFuture 对象传了进去，而 Executor 对象是 AysncTask 的 sDefaultExecutor 成员，

```java
    private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();
```

　　所以在 executeOnExecutor 方法中调用的 `exec.execute(mFuture);` 其实就是调用了 SerialExecutor 的 execute 方法。

　　SerialExecutor 是一个常量对象，因此在整个应用程序中的所有 AsyncTask 实例都会共用同一个 SerialExecutor。

### SerialExecutor

```java
    private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        // mFuture.run
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }
```

　　SerialExecuot 是使用 ArrayDeque 这个队列来管理 Runnable 对象的，如果一次性启动了很多和任务，首先在第一次运行 execute() 方法的时候，会调用 ArrayDeque 的 offer() 方法将传入的 Runnable 对象添加到队列的尾部，然后判断 mActive 对象是不是等于 null，第一次运行当然是等于 null 了，于是会调用 scheduleNext() 方法。在这个方法中会从队列的头部取值，并复制给 mActive 对象，然后调用 THREAD_POOL_EXECUTOR 去执行取出的 Runnable 对象。之后如果又有新的任务被执行，同样还会调用 offer() 方法将传入的 Runnable 添加到队列的尾部，但是再去给 mActive 对象做非空检查的时候就会发现 mActive 对象已经不再是 null 了，于是就不会再调用 scheduleNext() 方法。

　　在 runnable 的 run 方法中，在 finally 中调用了 scheduleNext() 方法，保证无论发生什么情况，这方法都会被调用。也就是说，每当一个任务执行完毕后，下一个任务才会得到执行。

　　SerialExecutor 是单一线程池的效果，如果快速地启动了很多任务，同一时刻只会有一个线程正在执行，其余地均处于等待状态。

　　在 3.0 版本中 AysncTask 并没有 SerialExecutor 类，而是构建了一个 sExecutor 常量，并对线程池总大小、同一时刻能够运行的线程数做了规定，同一时刻能够运行的线程数为 5 个，线程池总大小为 128。

　　所以 AysncTask 是串行执行。

　　SerialExecutor 的 execute 方法里的所有逻辑就是在子线程中执行的了，在其中还调用了 mFuture 的 run 方法。



#### FutureTask#run

```java
    public void run() {
        if (state != NEW ||
            !U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    // 调用 c.call，而 c 就是 mWorker
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }
```

　　FutureTask 的 run 方法会调用 c.call，而 c 就是在创建 Fucture 对象时传入的参数，也就是 mWorker。将 mWorker 的 call 方法单独列出来：

```java
        public Result call() throws Exception {
                mTaskInvoked.set(true);
                Result result = null;
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    //noinspection unchecked
                    // 调用了 doInBackground 方法
                    result = doInBackground(mParams);
                    Binder.flushPendingCommands();
                } catch (Throwable tr) {
                    mCancelled.set(true);
                    throw tr;
                } finally {
                    // 调用了 postResult 方法
                    postResult(result);
                }
                return result;
            }
```

　　在 mWorker 的 call 方法里面显示调用了 doInBackground() 方法去处理耗时的操作，因为在 SerialExecutor 的 execute 方法里面开启了子线程运行，所以 doInBackground 方法是在子线程中运行。

　　在 doInBackground() 方法之后接着将 doBackground() 方法返回的结果传递给了 postResult() 方法。

### AsyncTask#postResult

```java
    private Result postResult(Result result) {
        @SuppressWarnings("unchecked")
        Message message = getHandler().obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Result>(this, result)); // this-AysncTaskResult.mTask,result-AysncTaskResult.mData
        message.sendToTarget();
        return result;
    }
```

　　getHandler() 就是 AysncTask 构造方法中创建的 mHandler 对象，如果指定了 Looper，则使用指定的 Looper，如果没有指定 Looper，则调用 getMainHandler() 方法来初始化 mHandler。

#### AsyncTask#getMainHandler

```java
    private static Handler getMainHandler() {
        synchronized (AsyncTask.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler(Looper.getMainLooper());
            }
            return sHandler;
        }
    }
```

　　所以 mHandler 是一个 InternalHandler 对象，运行在主线程上。

#### AysncTask#InternalHandler

```java
    private static class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }
```

　　在 postResult 向指定线程（通常都是主线程）发出 MESSAGE_POST_RESULT 消息后，主线程开始处理消息，就会调用到 `result.mTask.finish(result.mData[0])` ，而 result.mTask 就是 AsyncTaskResult 的 mTask，而 mTask 就是 AsyncTask。

### AysncTask#finish

```
    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
        mStatus = Status.FINISHED;
    }
```

　　如果当前任务被取消掉了，就会调用 onCancelled() 方法，如果没有被取消，则调用 onPostExecute() 方法，这样当前任务的执行就全部结束了。

### AysncTask#publishProgress

```java
    @WorkerThread
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            getHandler().obtainMessage(MESSAGE_POST_PROGRESS,
                    new AsyncTaskResult<Progress>(this, values)).sendToTarget();
        }
    }
```

　　mHandler 接收到 MESSAGE_POST_PROGRESS 类型消息，会调用 ` result.mTask.onProgressUpdate(result.mData);`方法，也就是 AsyncTask 的 onProgressUpdate() 方法。

　　所以在 doInBackground() 方法中调用 publishProgress() 方法会从子线程切换到 UI 线程，从而完成对 UI 元素的更新操作。



## AsyncTask 并行





## 参考文章

[Android AsyncTask完全解析，带你从源码的角度彻底理解](https://blog.csdn.net/guolin_blog/article/details/11711405)

[Android源码分析—带你认识不一样的AsyncTask](https://blog.csdn.net/singwhatiwanna/article/details/17596225)

