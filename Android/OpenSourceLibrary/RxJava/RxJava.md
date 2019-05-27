# RxJava

## 1. RxJava 的基本订阅流程
　　一个简单的 RxJava 的使用：
```
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<String> subscriber) {
                subscriber.onNext("next");
				subscriber.onCompleted();
            }
        })
        .subscribe(new Subscriber<String>() {
        	@Override
        	public void onCompleted() {
        		Log.d(TAG, "onCompleted");
        	}

        	@Override
        	public void onError(Throwable e) {
        		Log.d(TAG, "showQuestionView onError");
        	}

        	@Override
        	public void onNext(String string) {
        		Log.d(TAG, "onNext string:"+string);
        	}
       });
```

#### 1.1. 先看 Observable.create（） 方法做了什么
```
public class Observable< T > {

    final OnSubscribe<T> onSubscribe;
    public static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(hook.onCreate(f)); //返回 Observable 的实例
    }

	protected Observable(OnSubscribe<T> f) {
        this.onSubscribe = f; //设置 onSubscribe 的值
    }
	...
}
```
　　Observable.create() 方法返回了一个 Observable 实例对象，并且将参数 OnSubscribe< T > f 存储为成员 onSubscribe。

#### 1.2 查看 Obervable.subscribe() 方法
```
public class Observable< T > {

	...

    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }

	static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
        ...

        // new Subscriber so onStart it
        subscriber.onStart();

        /*
         * See https://github.com/ReactiveX/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls
         * to user code from within an Observer"
         */
        // if not already wrapped
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
            hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
            ...
        }
    }
	...
}
```
　　调用 subscribe() 方法时，如果 subscriber 不是 SafeSubscriber 类型，就会将 subscriber 设置为 subscriber 对象，之后 hook.onSubscribeStart(observable, observable.onSubscribe) 返回的就是是 observable 的 onSubscribe 变量，而 observable 就是调用上一步 create 返回的 Observable 的实例对象，而它的 onSubscribe 变量就是我们自己传入 create() 方法的参数：
```
new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<String> subscriber) {
                subscriber.onNext("next");
				subscriber.onCompleted();
            }
        }
```
　　所以调用 subscribe() 方法之后就会调用到 observable 的 onSubscribe 变量的 call() 方法，而在 call() 方法中的参数 subscriber 是刚才创建的 SafeSubscriber 对象，调用 SafeSubscribe 的 onNext、onComplete 方法，会调用 SafeSubscribe 的 actual 变量的 onNext、onComplete 方法，而 actual 就是 SafeSubcribe 构造方法中传入的参数，也就是调用 Observable 的 subscribe() 的参数，所以 onSubscribe 变量的 call() 方法会调用Observable 的 subscribe() 的参数的方法，也就是：
```
new Subscriber<String>() {
        	@Override
        	public void onCompleted() {
        		Log.d(TAG, "onCompleted");
        	}

        	@Override
        	public void onError(Throwable e) {
        		Log.d(TAG, "showQuestionView onError");
        	}

        	@Override
        	public void onNext(String string) {
        		Log.d(TAG, "onNext string:"+string);
        	}
       }
```
　　到这里流程就过完了。方法的主导只要由 Observable 来，在创建 Observable 的时候，会将 OnSubscribe(订阅操作)传给 Observable(被观察) 作为成员变量，在调用 subscribe 的方法（订阅）时，将 Subcriber (观察者)作为变量传入，将 Subcriber （观察者）作为参数调用 onSubscribe 的 call 方法来处理订阅的事件，并且会调用 Subcriber 的相关方法（通知观察者）。

## subscribeOn(Schedulers.computation()) 流程分析
　　简单使用：
```
        Thread th=Thread.currentThread();
        System.out.println("onResume Tread name:"+th.getName()); //out:onResume Tread name:main
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d(TAG, "call subscriber:" + subscriber );
                Thread th=Thread.currentThread();
                System.out.println("call Tread name:"+th.getName()); //out:call Tread name:RxComputationScheduler-1
                subscriber.onNext("Hello");
                subscriber.onCompleted();
            }
        })
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

#### 2.1. create() 方法
　　create() 方法与上面的流程是相同的，会创建一个 Observable 对象，并设置 onSubscribe 成员的值，这里为了下面流程看起来清晰，我把 create() 方法创建的 Observable 对象称为 Observable1，它的 onSubscribe 成员称为 onSubscribe1。

#### 2.2. subscribeOn() 方法
```
public class Observable<T> {
	...
    public final Observable<T> subscribeOn(Scheduler scheduler) {
        if (this instanceof ScalarSynchronousObservable) {
            return ((ScalarSynchronousObservable<T>)this).scalarScheduleOn(scheduler);
        }
        return create(new OperatorSubscribeOn<T>(this, scheduler));
    }
	...
}
```
　　在 subscribeOn() 方法里再次调用了 create() 方法，不过这次是一个 OperatorSubscribeOn 对象，所以当前 Observable 的 onSubscribe 的值被设置为了 OperatorSubscribeOn 对象，并且将之前的 Observable1 作为参数传递过去。调用 subscribeOn() 方法后会返回一个新的 Observable 对象，也就是当前 Observable，在这里为了区分，将它称之为 Observable2。

###### 2.2.1. 查看 OperatorSubscribeOn 类：
```
public final class OperatorSubscribeOn<T> implements OnSubscribe<T> {

    final Scheduler scheduler;
    final Observable<T> source; //Observable1

    public OperatorSubscribeOn(Observable<T> source, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.source = source;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final Worker inner = scheduler.createWorker(); //线程池
        subscriber.add(inner);
        //启动线程
        inner.schedule(new Action0() {
            @Override
            public void call() {
                final Thread t = Thread.currentThread();

                Subscriber<T> s = new Subscriber<T>(subscriber) {
                    @Override
                    public void onNext(T t) {
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

                    @Override
                    public void setProducer(final Producer p) {
                        subscriber.setProducer(new Producer() {
                            @Override
                            public void request(final long n) {
                                if (t == Thread.currentThread()) {
                                    p.request(n);
                                } else {
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

                source.unsafeSubscribe(s);
            }
        });
    }
}

```
　　也可以看到 OperatorSubscribeOn 对象会持有 Observable1 成员。

　　在这里理清一下 create() 方法返回的 Observable1，它的 onSubscribe 值是我们调用 create() 方法是传入的 Observable.OnSubscribe<String>() 对象（也就是 onSubscribe1），而调用 subscribeOn() 方法后返回的是 Observable2，它的onSubscribe 值是 OperatorSubscribeOn 对象，并且 OperatorSubscribeOn 对象持有 Observable1 成员。

#### 2.3. subscribe() 方法
　　从 1.x 的简单流程可以得知，subscribe() 方法会调用 Observable 的 onSubscribe 的 call() 方法，当前的 Observable 是 Observable2，Observable2 的 onSubscribe 的值是 OperatorSubscribeOn 对象，所以就会调用 OperatorSubscribeOn 对象的方法，从 2.2.1 的 OperatorSubscribeOn 的代码可以看到，OperatorSubscribeOn 的 call() 方法会在线程池中调用 `source.unsafeSubscribe(s);`这句代码，s是在 call 方法中生成的，我们称他为 Subscriber3，而 source 是 Observable1，所以查看 Observable 的 unsafeSubscribe(s) 方法：
```
    public final Subscription unsafeSubscribe(Subscriber<? super T> subscriber) {
        try {
            // new Subscriber so onStart it
            subscriber.onStart();
            // allow the hook to intercept and/or decorate
            hook.onSubscribeStart(this, onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
            ...
        }
    }
```
　　在 Observable1 的 unsafeSubscribe() 方法中调用了 onSubscribe 的 call() 方法，也就是 onSubscribe1 的 call 方法（也就是自己调用 create() 传入的参数），并且将 subscriber3 作为参数进行了传递：
```
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
```
                    public void onNext(T t) {
                        subscriber.onNext(t);
                    }
```
　　在 Subscribe3 的 onNext() 方法中调用了 subscriber.next() 方法，而 subscriber 是什么？就是我们调用 subscriber() 方法是传递的参数，就是：
```
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
　　到这里流程就走完了，注意一下，从调用 OperatorSubscribeOn 的 call 方法，我们自己写的 OnSubscribe1 对象的 call() 方法和 Subscriber 的 onNext() 方法都是在线程中运行，所以如果你只设置一个 subscribeOn 会导致 OnSubscribe1 对象的 call() 方法和 Subscriber 的 onNext() 方法都在线程中运行，而且 subscribeOn() 方法的 OnSubscribe1 只是指调用 subscribeOn() 方法的 Observable 对象，之后的 Observable 对象是没有用的。

## 3. observeOn(AndroidSchedulers.mainThread()) 流程分析

```
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d(TAG, "call subscriber:" + subscriber );
                Thread th=Thread.currentThread();
                System.out.println("call Tread name:"+th.getName());
                subscriber.onNext("Hello");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
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
                        System.out.println("onNext Tread name:"+th.getName());
                        Log.d(TAG, "onNext s:" + s);
                    }
                });
```
　　将 create() 方法传入的参数 Observable.OnSubscribe 记为 OnSubscribe1，将 subscribe() 方法的参数 Subscriber 记为 Subscriber1（为了后面的分析）。

#### 3.1. AndroidSchedulers.mainThread() 返回的是什么？
```
public final class AndroidSchedulers {
	//单例模式，返回 AndroidSchedulers 的实例
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

        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
			// 设置 mainThreadScheduler 为 LooperScheduler 的实例对象
			// 将住线程的 Looper 传入 LooperScheduler 的构造方法作为参数
            mainThreadScheduler = new LooperScheduler(Looper.getMainLooper());
        }
    }

    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }


}
```
　　mainThread 返回的是一个 LooperScheduler(Looper.getMainLooper()) 的实例对象。
```
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

#### 3.2. observeOn(AndroidSchedulers.mainThread()) 方法：
```
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

#### 3.3. lift() 方法
```
public class Observable<T> {
    public final <R> Observable<R> lift(final Operator<? extends R, ? super T> operator) {
        return new Observable<R>(new OnSubscribeLift<T, R>(onSubscribe, operator));
    }
}
```
　　将 onSubscribe （也就是 onSubscribe1 ）与 operator (也就是 OperatorObserveOn )作为参数，创建 onSubscribeList，当前的 Observable 的 onSubscribe 成了 OnSubscribeLift 对象，而 onSubscribe1 成为 OnSubscribeLift 的 parent 变量，而 OperatorObserOn 成为 OnSubscribeLift 的 operator 变量。

#### 3.4. subscribe() 方法流程
　　从 RxJava 的简单流程可知 subscribe() 方法调用 Observable 的 onSubscribe 的 call() 方法，也就是 onSubscribe 的 call() 方法，即 OnSubscribeLift 的 call() 方法。
```
public final class OnSubscribeLift<T, R> implements OnSubscribe<R> {

    static final RxJavaObservableExecutionHook hook = RxJavaPlugins.getInstance().getObservableExecutionHook();

    final OnSubscribe<T> parent;

    final Operator<? extends R, ? super T> operator;

    public OnSubscribeLift(OnSubscribe<T> parent, Operator<? extends R, ? super T> operator) {
        this.parent = parent;
        this.operator = operator;
    }

    @Override
    public void call(Subscriber<? super R> o) {
        try {
            Subscriber<? super T> st = hook.onLift(operator).call(o);
            try {
                // new Subscriber created and being subscribed with so 'onStart' it
                st.onStart();
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
　　先调用了 OperatorObserOn 的 call 方法，然后又调用了 onSubscribe1 的 call 方法。而参数 o 就是自己写的 Subscriber1。

###### 3.4.1. OperatorObserOn 的 call 方法
```
    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        if (scheduler instanceof ImmediateScheduler) {
            // avoid overhead, execute directly
            return child;
        } else if (scheduler instanceof TrampolineScheduler) {
            // avoid overhead, execute directly
            return child;
        } else {
            ObserveOnSubscriber<T> parent = new ObserveOnSubscriber<T>(scheduler, child, delayError, bufferSize);
            parent.init();
            return parent;
        }
    }
```
　　创建了一个 ObserveOnSubscriber 实例对象，并且调用 init() 方法。

###### 3.4.2.

#### 3.5. OperatorObserveOn 类

## 参考文章
[拆轮子系列：拆 RxJava](https://blog.piasy.com/2016/09/15/Understand-RxJava/index.html)
[RxJava 源码解析之观察者模式](https://juejin.im/post/58dcc66444d904006dfd857a)
[友好 RxJava2.x 源码解析（一）基本订阅流程](https://juejin.im/post/5a209c876fb9a0452577e830)

