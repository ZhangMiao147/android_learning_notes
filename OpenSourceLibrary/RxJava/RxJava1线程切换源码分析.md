# RxJava 1 线程切换源码分析

　　RxJava 进行异步非常简单，只需要使用 subscribeOn 和 observeOn 这两个操作符即可。

　　subscribeOn 操作 OnSubscribe （订阅操作）的运行线程。

　　observeOn 操作观察者的运行线程。一般都是主线程，也就是 UI 线程。

# 1. subscribeOn 流程分析

## 1.1. 简单使用

　　subscribeOn(Schedulers.computation()) 方法让 OnSubscribe()（订阅操作） 运行在计算线程。
　　简单使用：

```java
        Thread th=Thread.currentThread();
        System.out.println("onResume Tread name:"+th.getName()); //out:onResume Tread name:main
        Observable.create(new Observable.OnSubscribe<String>() { // OnSubscribe1
            @Override
            public void call(Subscriber<? super String> subscriber) { 
                Log.d(TAG, "call subscriber:" + subscriber );
                Thread th=Thread.currentThread();
                System.out.println("call Tread name:"+th.getName()); //out:call Tread name:RxComputationScheduler-1
                subscriber.onNext("Hello");
                subscriber.onCompleted();
            }
        }) //Observable1
                .subscribeOn(Schedulers.computation())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                    }

                    @Override
                    public void onNext(String s) {
                        Thread th=Thread.currentThread();
                        System.out.println("onNext Tread name:"+th.getName()); //out:onNext Tread name:RxComputationScheduler-1
                        Log.d(TAG, "onNext s:" + s);
                    }
                });
```

## 1.2. create() 方法

　　create() 方法会创建一个 Observable 对象，并设置 onSubscribe 成员的值为传递的 OnSubscribe 对象，这里为了下面流程看起来清晰，把 create() 方法创建的 Observable 对象称为 Observable1，它的 onSubscribe 成员称为 onSubscribe1。

## 1.3. subscribeOn() 方法

```java
public class Observable<T> {
	...
    public final Observable<T> subscribeOn(Scheduler scheduler) {
        if (this instanceof ScalarSynchronousObservable) {
            return ((ScalarSynchronousObservable<T>)this).scalarScheduleOn(scheduler);
        }
        // 生成 OperatorSubscribeOn 对象，上一步生成的 Observable 作为 OperatorSubscribeOn 的 source 成员,scheduler 就是 Schedulers.computation() 对象
    	  // create() 方法生成新的 Observable，OperatorSubscribeOn 作为新的 Observable 的 onSubscribe 成员
        return create(new OperatorSubscribeOn<T>(this, scheduler));
    }
	...
}
```
　　在 subscribeOn() 方法里再次调用了 create() 方法，不过这次是一个 OperatorSubscribeOn 对象，所以当前 Observable 的 onSubscribe 的值被设置为了 OperatorSubscribeOn 对象，并且将之前的 Observable1 作为参数传递过去。

　　调用 subscribeOn() 方法后会返回一个新的 Observable 对象，也就是当前 Observable，在这里为了区分，将它称之为 Observable2。

　　OperatorSubscribeOn 是 OnSubscribe 的实现类。

　　subscribeOn 就是 create + OperatorSubscribeOn 实现。

## 1.4. OperatorSubscribeOn

```java
public final class OperatorSubscribeOn<T> implements OnSubscribe<T> {

    final Scheduler scheduler; // Schedulers.computation()
    final Observable<T> source; // Observable1

    public OperatorSubscribeOn(Observable<T> source, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.source = source;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        // 拿到设置的线程
        final Worker inner = scheduler.createWorker(); 		
        // Worker 也实现了 Subscription，所以可以加入到 Subscriber 中，用于集体取消订阅。
        subscriber.add(inner);
        // 启动线程
        inner.schedule(new Action0() {
            @Override
            public void call() {
                final Thread t = Thread.currentThread();

                Subscriber<T> s = new Subscriber<T>(subscriber) { // Subscriber 3
                    @Override
                    public void onNext(T t) {
                      	// 在匿名 Subscriber 中，收到上游的数据后，转发给下游。
                        subscriber.onNext(t); 
                    }

                    @Override
                    public void onError(Throwable e) {
                        try {
                            subscriber.onError(e);
                        } finally {
                            inner.unsubscribe();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            subscriber.onCompleted();
                        } finally {
                            inner.unsubscribe();
                        }
                    }
									  // 同时设置了 observeOn() 方法，setProducer 方法会被调用，从而确保 OnSubscribe 的 call 方法运行在指定的线程中
                    @Override
                    public void setProducer(final Producer p) {
                        subscriber.setProducer(new Producer() {
                            @Override
                            public void request(final long n) {
                                if (t == Thread.currentThread()) {
                                  	// Producer#request 被调用时，如果调用线程就是 worker 的线程（t），就直接把请求转发给上游。
                                    p.request(n);
                                } else {
                                  	// 否则还需要进行一次调度，确保调用上游的 request 一定是在 worker 的线程。
                                    inner.schedule(new Action0() {
                                        @Override
                                        public void call() {
                                            p.request(n);
                                        }
                                    });
                                }
                            }
                        });
                    }
                };
								// 在 worker 线程中，把自己（匿名 Subscriber）和上游连接起来。
                source.unsafeSubscribe(s); 
            }
        });
    }
}

```
　　create() 方法返回的 Observable1，它的 onSubscribe 值是 onSubscribe1，而调用 subscribeOn() 方法后返回的是 Observable2，它的onSubscribe 值是 OperatorSubscribeOn 对象，而 OperatorSubscribeOn 对象的 source 成员是 Observable1 。 

　　连接上游（可能会触发请求）、向上游发请求，都是在 worker 的线程上执行的，所以如果上游处理请求的代码没有进行异步操作，那上游的代码就是在 subscribeOn 指定的线程上执行的。即 subscribeOn 影响它上面的调用执行时所在的线程。

　　关于使用多次调用 subscribeOn 的效果，后面的 subscribeOn 只会改变前面的 subscribeOn 调度操作所在的线程，并不能改变最终被调度的代码执行的线程，但对于中途的代码执行的线程，还是会影响到的。

## 1.5. subscribe() 方法

　　从 RxJava 的简单流程可以得知，subscribe() 方法会调用 Observable 的 onSubscribe 的 call() 方法，当前的 Observable 是 Observable2，Observable2 的 onSubscribe 的值是 OperatorSubscribeOn 对象，所以就会调用 OperatorSubscribeOn 对象的方法，而 OperatorSubscribeOn 的 call() 方法会在线程池中调用 `source.unsafeSubscribe(s);`这句代码，s 是在 call 方法中生成的，称他为 Subscriber3，而 source 是 Observable1。

### 1.5.1. Observable#unsafeSubscribe

```java
    public final Subscription unsafeSubscribe(Subscriber<? super T> subscriber) {
        try {
            // new Subscriber so onStart it
            subscriber.onStart();
            // allow the hook to intercept and/or decorate
          	// hook.onSubscribeStart 返回的就是 onSubscribe
            hook.onSubscribeStart(this, onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
            ...
        }
    }
```
　　在 Observable1 的 unsafeSubscribe() 方法中调用了 onSubscribe 的 call() 方法，也就是 onSubscribe1 的 call 方法（也就是自己调用 create() 传入的参数），并且将 subscriber3 作为参数进行了传递：
```java
new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d(TAG, "call subscriber:" + subscriber );
                Thread th=Thread.currentThread();
                System.out.println("call Tread name:"+th.getName());
                subscriber.onNext("Hello"); //subscribe:Subscribe3
                subscriber.onCompleted();
            }
        }
```
　　在 onSubscribe1 的 call 方法中调用了 subscriber.onNext() 方法，也就是 Subscribe3 的 onNext() 方法，所以回到 OperatorSubscribeOn 的 call() 方法中查看 Subscribe3 的 onNext() 方法：
```java
                    public void onNext(T t) {
                        subscriber.onNext(t);
                    }
```
　　在 Subscribe3 的 onNext() 方法中调用了 subscriber.next() 方法，而 subscriber 是什么？就是我们调用 subscriber() 方法是传递的参数，就是：
```java
new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                    }

                    @Override
                    public void onNext(String s) {
                        Thread th=Thread.currentThread();
                        System.out.println("onNext Tread name:"+th.getName());
                        Log.d(TAG, "onNext s:" + s);
                    }
                }
```
　　就这样被观察者将消息传递到了观察者。

## 1.6. 总结

　　从调用 OperatorSubscribeOn 的 call 方法，自己实现的 OnSubscribe1 对象的 call() 方法是在指定线程中运行，所以如果设置一个 subscribeOn 会导致 OnSubscribe1 对象的 call() 方法在指定线程中运行，而且 subscribeOn() 方法的 OnSubscribe1 只是指调用 subscribeOn() 方法的 Observable 对象，之后的 Observable 对象是没有用的。而 Subscriber 的 onNext() 方法也在指定线程运行，是因为在 call 中调用的时候没有切换线程，所以 onNext() 方法也在指定线程中运行。

# 2. observeOn 流程分析

## 2.1. 简单使用

```java
        Observable.create(new Observable.OnSubscribe<String>() { // Observable 1
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d(TAG, "call subscriber:" + subscriber );
                Thread th=Thread.currentThread();
                System.out.println("call Tread name:"+th.getName());
                subscriber.onNext("Hello");
                subscriber.onCompleted();
            }
        })
          			// 订阅操作的运行线程
                .subscribeOn(Schedulers.computation())
          			// 观察者运行线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() { // Subscriber 1
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                    }

                    @Override
                    public void onNext(String s) {
                        Thread th=Thread.currentThread();
                        System.out.println("onNext Tread name:"+th.getName());
                        Log.d(TAG, "onNext s:" + s);
                    }
                });
```
　　将 create() 方法传入的参数 Observable.OnSubscribe 记为 OnSubscribe1，将 subscribe() 方法的参数 Subscriber 记为 Subscriber1（为了后面的分析）。

## 2.2. AndroidSchedulers.mainThread() 返回的是什么？

```java
public final class AndroidSchedulers {
	 // 单例模式，返回 AndroidSchedulers 的实例
    private static AndroidSchedulers getInstance() {
        for (;;) {
            AndroidSchedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new AndroidSchedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private AndroidSchedulers() {
        RxAndroidSchedulersHook hook = RxAndroidPlugins.getInstance().getSchedulersHook();
				// 主线程调度
        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
			      // 设置 mainThreadScheduler 为 LooperScheduler 的实例对象
			      // 将主线程的 Looper 传入 LooperScheduler 的构造方法作为参数
            mainThreadScheduler = new LooperScheduler(Looper.getMainLooper());
        }
    }

    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }


}
```
### 2.2.1. LooperScheduler

　　mainThread 返回的是一个 LooperScheduler(Looper.getMainLooper()) 的实例对象。

```java
class LooperScheduler extends Scheduler {
    private final Handler handler;

    LooperScheduler(Looper looper) {
				// 创建一个 handler ，用于发送和处理消息
				// 再次强调，looper 是主线程的 looper
        handler = new Handler(looper);
    }

    LooperScheduler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Worker createWorker() {
        return new HandlerWorker(handler);
    }

    static class HandlerWorker extends Worker {
        private final Handler handler;
        private final RxAndroidSchedulersHook hook;
        private volatile boolean unsubscribed;

        HandlerWorker(Handler handler) {
            this.handler = handler;
            this.hook = RxAndroidPlugins.getInstance().getSchedulersHook();
        }

        @Override
        public void unsubscribe() {
            unsubscribed = true;
            handler.removeCallbacksAndMessages(this /* token */);
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed;
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            if (unsubscribed) {
                return Subscriptions.unsubscribed();
            }

            action = hook.onSchedule(action);

            ScheduledAction scheduledAction = new ScheduledAction(action, handler);

            Message message = Message.obtain(handler, scheduledAction);
            message.obj = this; // Used as token for unsubscription operation.
						//使用 handler 发送消息出去
						//发送的消息会进入主线程的 MessageQueue 中，在 Looper.loop() 方法中会将 message 取出，然后调用 scheduledAction 的 run() 方法对消息进行处理
            handler.sendMessageDelayed(message, unit.toMillis(delayTime));

            if (unsubscribed) {
                handler.removeCallbacks(scheduledAction);
                return Subscriptions.unsubscribed();
            }

            return scheduledAction;
        }

        @Override
        public Subscription schedule(final Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }
    }

    static final class ScheduledAction implements Runnable, Subscription {
        private final Action0 action;
        private final Handler handler;
        private volatile boolean unsubscribed;

        ScheduledAction(Action0 action, Handler handler) {
            this.action = action;
            this.handler = handler;
        }

				//接收到消息进行处理
        @Override public void run() {
            try {
								// 调用 ScheduledAction 的 call 方法
                action.call();
            } catch (Throwable e) {
                // nothing to do but print a System error as this is fatal and there is nowhere else to throw this
                IllegalStateException ie;
                if (e instanceof OnErrorNotImplementedException) {
                    ie = new IllegalStateException("Exception thrown on Scheduler.Worker thread. Add `onError` handling.", e);
                } else {
                    ie = new IllegalStateException("Fatal Exception thrown on Scheduler.Worker thread.", e);
                }
                RxJavaPlugins.getInstance().getErrorHandler().handleError(ie);
                Thread thread = Thread.currentThread();
                thread.getUncaughtExceptionHandler().uncaughtException(thread, ie);
            }
        }

        @Override public void unsubscribe() {
            unsubscribed = true;
            handler.removeCallbacks(this);
        }

        @Override public boolean isUnsubscribed() {
            return unsubscribed;
        }
    }
}

```

　　AndroidSchedulers.mainThread 就是通过向主线程的 MessageQueue 中发消息，主线程的 Looper 会从 MessageQueue 取出来进行消费，处理消息也就到了主线程。

## 2.3. observeOn(AndroidSchedulers.mainThread()) 方法：

```java
public class Observable<T> {
    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, RxRingBuffer.SIZE);
    }

    public final Observable<T> observeOn(Scheduler scheduler, int bufferSize) {
        return observeOn(scheduler, false, bufferSize);
    }

    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        if (this instanceof ScalarSynchronousObservable) {
            return ((ScalarSynchronousObservable<T>)this).scalarScheduleOn(scheduler);
        }
        return lift(new OperatorObserveOn<T>(scheduler, delayError, bufferSize));
    }
}
```
　　observeOn(AndroidSchedulers.mainThread()) 方法最后返回了 lift(new OperatorObserveOn<T>(scheduler, delayError, bufferSize)) 方法的返回值。

　　observeOn 有好几个重载版本，支持指定 buffer 大小、是否延迟 Error 事件，这个 delayError 是从 v1.1.1 引入的。

　　所以 observeOn 就是 lift + OperatorObserveOn 实现。

## 2.4. lift() 

```java
public class Observable<T> {
    public final <R> Observable<R> lift(final Operator<? extends R, ? super T> operator) {
        return new Observable<R>(new OnSubscribeLift<T, R>(onSubscribe, operator));
    }
}
```
　　将 onSubscribe （也就是 onSubscribe1 ）与 operator (也就是 OperatorObserveOn )作为参数，创建 onSubscribeList，当前的 Observable 的 onSubscribe 成了 OnSubscribeLift 对象，而 onSubscribe1 成为 OnSubscribeLift 的 parent 变量，而 OperatorObserverOn 成为 OnSubscribeLift 的 operator 变量。

## 2.5. OnSubscribeLift

　　从 RxJava 的简单流程可知 subscribe() 方法调用 Observable 的 onSubscribe 的 call() 方法，也就是  OnSubscribeLift 的 call() 方法。
```java
public final class OnSubscribeLift<T, R> implements OnSubscribe<R> {

    static final RxJavaObservableExecutionHook hook = RxJavaPlugins.getInstance().getObservableExecutionHook();

    final OnSubscribe<T> parent;

    final Operator<? extends R, ? super T> operator;

    public OnSubscribeLift(OnSubscribe<T> parent, Operator<? extends R, ? super T> operator) {
        this.parent = parent;
        this.operator = operator;
    }

    @Override
    public void call(Subscriber<? super R> o) { // o - subscriber1
        try {
          	// hook.onList(operator) 返回就是 operator
          	// 也就是调用了 OperatorObserverOn 的 call 方法
            Subscriber<? super T> st = hook.onLift(operator).call(o); // subscriber2
            try {
                // new Subscriber created and being subscribed with so 'onStart' it
                st.onStart();
              	// onSubscribe1 的 call 方法，也就是发出订阅消息
                parent.call(st);
            } catch (Throwable e) {
                ...
            }
        } catch (Throwable e) {
            ...
        }
    }
}
```
　　先调用了 OperatorObserveOn 的 call 方法，即对下游 subscriber 用操作符进行处理，然后又调用了 onSubscribe1 的 call 方法通知处理后的 subscriber。而参数 o 就是自己写的 Subscriber1。将这里生成的 Subscriber st 记录为 Subscriber2。

### 2.5.1. OperatorObserveOn 

```java
public final class OperatorObserveOn<T> implements Operator<T, T> {

    private final Scheduler scheduler;
    private final boolean delayError;
    private final int bufferSize;

    /**
     * @param scheduler the scheduler to use
     * @param delayError delay errors until all normal events are emitted in the other thread?
     */
    public OperatorObserveOn(Scheduler scheduler, boolean delayError) {
        this(scheduler, delayError, RxRingBuffer.SIZE);
    }

    /**
     * @param scheduler the scheduler to use
     * @param delayError delay errors until all normal events are emitted in the other thread?
     * @param bufferSize for the buffer feeding the Scheduler workers, defaults to {@code RxRingBuffer.MAX} if <= 0
     */
    public OperatorObserveOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.bufferSize = (bufferSize > 0) ? bufferSize : RxRingBuffer.SIZE;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) { // child - subscriber1
        if (scheduler instanceof ImmediateScheduler) {
            // avoid overhead, execute directly
            return child;
        } else if (scheduler instanceof TrampolineScheduler) {
            // avoid overhead, execute directly
            return child;
        } else {
          	// 创建了一个 ObserveOnSubscriber 对象
            ObserveOnSubscriber<T> parent = new ObserveOnSubscriber<T>(scheduler, child, delayError, bufferSize);
          	// 调用 ObserveOnSubscriber 的 init() 方法
            parent.init();
          	// 返回 ObserveOnSubscriber 对象
            return parent;
        }
    }

    public static <T> Operator<T, T> rebatch(final int n) {
        return new Operator<T, T>() {
            @Override
            public Subscriber<? super T> call(Subscriber<? super T> child) {
                ObserveOnSubscriber<T> parent = new ObserveOnSubscriber<T>(Schedulers.immediate(), child, false, n);
                parent.init();
                return parent;
            }
        };
    }
}
```

　　OperatorObserveOn 的 call 方法很简单，创建了  ObserveOnSubscriber 对象，调用其 init 方法，然后返回  ObserveOnSubscriber 对象。创建 ObserveOnSubScriber 的 child 变量是 Subscriber1，而 scheduler 是 调用 observeOn() 传入的 Scheduler 参数，也就是 LooperScheduler 对象，而上面提到的 Subscriber2 就是 ObserveOnSubscriber。

　　作为操作符的逻辑，还是很简单的，如果 scheduler 是 ImmediateScheduler/TrampolineScheduler，就什么都不做，否则就把  subscriber 包装为 ObserveOnSubscriber。

#### 2.5.1.1.  ObserveOnSubscriber

　　ObserveOnSubscriber 除了负责把向下游发送数据的操作调度到指定的线程，还负责 backpressure 支持。

```java
 /** Observe through individual queue per observer. */
    private static final class ObserveOnSubscriber<T> extends Subscriber<T> implements Action0 {
        final Subscriber<? super T> child; //Subscriber1
        final Scheduler.Worker recursiveScheduler; //HandlerWorker

        public ObserveOnSubscriber(Scheduler scheduler, Subscriber<? super T> child, boolean delayError, int bufferSize) { // child - subscriber1
            this.child = child;
            this.recursiveScheduler = scheduler.createWorker();
            this.delayError = delayError;
            this.on = NotificationLite.instance();
            int calculatedSize = (bufferSize > 0) ? bufferSize : RxRingBuffer.SIZE;
            // this formula calculates the 75% of the bufferSize, rounded up to the next integer
            this.limit = calculatedSize - (calculatedSize >> 2);
            if (UnsafeAccess.isUnsafeAvailable()) {
                queue = new SpscArrayQueue<Object>(calculatedSize);
            } else {
                queue = new SpscAtomicArrayQueue<Object>(calculatedSize);
            }
            // signal that this is an async operator capable of receiving this many
						// 在构造方法中调用了 Subscriber 类实现的 request() 方法
            request(calculatedSize);
        }

        void init() {
            // don't want this code in the constructor because `this` can escape through the 
            // setProducer call
            Subscriber<? super T> localChild = child;
						// 调用 Subscriber 的 setProducer 方法
            localChild.setProducer(new Producer() {

                @Override
                public void request(long n) {
                    if (n > 0L) {
                        BackpressureUtils.getAndAddRequest(requested, n);
                        schedule();
                    }
                }

            });
            localChild.add(recursiveScheduler);
            localChild.add(this);
        }
        @Override
        public void onNext(final T t) {
            if (isUnsubscribed() || finished) {
                return;
            }
            if (!queue.offer(NotificationLite.next(t))) {
                onError(new MissingBackpressureException());
                return;
            }
            schedule();
        }

        @Override
        public void onCompleted() {
            if (isUnsubscribed() || finished) {
                return;
            }
            finished = true;
            schedule();
        }

        @Override
        public void onError(final Throwable e) {
            if (isUnsubscribed() || finished) {
                RxJavaHooks.onError(e);
                return;
            }
            error = e;
            finished = true;
            schedule();
        }      

        @Override
        public void call() {
            long missed = 1L;
            long currentEmission = emitted;

            // these are accessed in a tight loop around atomics so
            // loading them into local variables avoids the mandatory re-reading
            // of the constant fields
            final Queue<Object> q = this.queue;
            final Subscriber<? super T> localChild = this.child;

            // requested and counter are not included to avoid JIT issues with register spilling
            // and their access is is amortized because they are part of the outer loop which runs
            // less frequently (usually after each bufferSize elements)

            for (;;) {
                long requestAmount = requested.get();

                while (requestAmount != currentEmission) {
                    boolean done = finished;
                    Object v = q.poll();
                    boolean empty = v == null;

                    if (checkTerminated(done, empty, localChild, q)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }
										// 调用了 localChild.onNext()方法
                    localChild.onNext(NotificationLite.<T>getValue(v));

                    currentEmission++;
                    if (currentEmission == limit) {
                        requestAmount = BackpressureUtils.produced(requested, currentEmission);
                        request(currentEmission);
                        currentEmission = 0L;
                    }
                }

                if (requestAmount == currentEmission) {
                    if (checkTerminated(finished, q.isEmpty(), localChild, q)) {
                        return;
                    }
                }

                emitted = currentEmission;
                missed = counter.addAndGet(-missed);
                if (missed == 0L) {
                    break;
                }
            }
        } 
      
        protected void schedule() {
            if (counter.getAndIncrement() == 0L) {
              	// recursiveScheduler 就是 AndroidSchedulers 的 Work 线程
              	// 传递给主线程的消息的 Runnable 就是 ObserveOnSubcribe
              	// 主线程调用的 handle 处理消息的时候会调用 ObserveOnSubcribe 的 call() 方法，这样 ObserveOnSubcribe 的 call 方法就运行在了主线程
               	recursiveScheduler.schedule(this);
            }

        }
}
```
　　调用了 schedule() 方法(onError() 与 onComplete() 方法都会调用 schedule() 方法)，而 schedule() 方法会在主线程发送 message 出去，最终会调用 ObserveOnSubscriber 的 call() 方法，而在 ObserveOnSubscriber 的 call() 方法中调用了 localChild 的 onNext() 方法，而 localChild 是什么呢？就是在创建 ObserveOnSubscriber 时传递的参数，也就是 Subscriber1。

　　这样也就向观察者发送了消息，而 ObserveOnSubscriber 的 onNext 是通过 handler 向主线程发送消息，处理消息是在主线程，所以 Subscriber1 的 onNext() 就会运行在主线程（onError() 与 onComplete() 方法相同）。

　　observable 调度了每个单独的 subscriber.onXXX() 调用，使得数据向下游传递的时候可以切换到指定的线程，即 observeOn 影响它下面的调用执行时所在的线程。

　　多次调用 observable 的效果，每次调用都会改变数据向下传递时所在的线程。

## 2.6. 总结

　　ObserveOn() 方法会生成 OperatorObserveOn 对象，并且将其设置为 Observable 的 onSubscribe 对象，并且将下游的 Subscriber 作为对象进行封装，在调用 onNext()、onError()、onComplete() 方法时通过向主线程发送 message 消息，在主线程中处理消息，从而确保 Subscriber 的 onNext()、onError()、onComplete() 运行在主线程。

# 3. 完整过程

![](image/RxJava_call_stack_just_map_subscribeOn_observeOn.png)

　　subscribeOn() 方法会使用 OperatorSubscribeOn 类作为 Observable 的 onSubscribe 对象，将上游的 Observable 进行封装，从而确保上游的 OnSubscribe 的 call() 方法运行在指定线程。ObserveOn() 方法会使用 OperatorObserveOn 类作为 Observable 的 onSubscribe 对象，将下游的 Subscriber 进行封装，从而确保 Subscriber 的 onNext()、onError()、onComplete() 运行在指定的线程。

# 4. 参考文章

1. [拆轮子系列：拆 RxJava](https://blog.piasy.com/2016/09/15/Understand-RxJava/index.html)

2. [RxJava 源码解析之观察者模式](https://juejin.im/post/58dcc66444d904006dfd857a)