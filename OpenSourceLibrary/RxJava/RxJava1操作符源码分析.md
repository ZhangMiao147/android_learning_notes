# RxJava 1 操作符源码分析

# 1. 简单使用

```java
        Observable.just("hello word")
                .map(new Func1<String, Long>() {
                    @Override
                    public Long call(String s) {
                        return s != null ? s.length() : 0l;
                    }
                })
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {

                    }
                });
```

　　just() 方法会将传入的参数依次发送出来。

　　使用 map() 操作符，把字符串转换为它的长度。

# 2. just

## 2.1. Observable#just

```java
    public static <T> Observable<T> just(final T value) {
        return ScalarSynchronousObservable.create(value);
    }

    public static <T> ScalarSynchronousObservable<T> create(T t) {
        return new ScalarSynchronousObservable<T>(t);
    }

    protected ScalarSynchronousObservable(final T t) {
        super(RxJavaHooks.onCreate(new JustOnSubscribe<T>(t)));
        this.t = t;
    }
```

　　创建的是 ScalarSynchronousObservable ，是一个 Observable 的子类。将 just 的参数设置为 ScalarSynchronousObservable  的 t 对象。

　　而传给父类构造函数的就是 JustOnSubscribe，一个 onSubscribe 的实现类。

　　而 Observable 的构造函数接受一个 OnSubscribe，OnSubscribe 是一个回调，会在 Observable#subscribe 中使用，用于通知 observable 自己被订阅。

## 2.2. JustOnSubscribe#call

　　在 just() 的实现里面，创建了一个 JustOnSubscribe，并将其设置为 Observable 的 onSubscribe 成员。所以在 subscribe() 方法中执行 `hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber)` 方法实际执行的就是 JustOnSubscribe 的 call 方法。

```java
    static final class JustOnSubscribe<T> implements OnSubscribe<T> {
        final T value; // just 传入的数值

        JustOnSubscribe(T value) {
            this.value = value;
        }
		// s 是 subscribe 方法的参数
        @Override
        public void call(Subscriber<? super T> s) {
            s.setProducer(createProducer(s, value));
        }
    }

    static <T> Producer createProducer(Subscriber<? super T> s, T v) {
        if (STRONG_MODE) {
            return new SingleProducer<T>(s, v);
        }
        return new WeakSingleProducer<T>(s, v);
    }
```

　　在 RxJava 1.x 中，数据都是从 observable push 到 subscriber 的，但要是 observable 发的太快，subscriber 处理不过来，该怎么办？一种办法是，把数据保存起来，但这显然可能导致内存耗尽；另一种办法是，多余的数据来了之后就丢掉，至于丢掉和保留的策略可以按需指定；还有一种办法就是让 subscriber 向  observale 主动请求数据，subscriber 不请求，onservable 就不发出数据。它两互相协调，避免出现过多的数据，而协调的桥梁，就是 producer。

## 2.3. Subscriber#setProducer

```java
    public void setProducer(Producer p) {
        long toRequest;
        boolean passToSubscriber = false;
        synchronized (this) {
            toRequest = requested;
            producer = p;
            // 一次包装，ActionSubscriber 包装为 SafeSubscriber
            if (subscriber != null) {
                // middle operator ... we pass through unless a request has been made
                if (toRequest == NOT_SET) {
                    // we pass through to the next producer as nothing has been requested
                    passToSubscriber = true;
                }
            }
        }
        // do after releasing lock
        // 发生一次 pass through ，然后回进入 else 代码块
        if (passToSubscriber) {
            subscriber.setProducer(producer);
        } else {
            // we execute the request with whatever has been requested (or Long.MAX_VALUE)
            // 这列所有的 requested 初始值都是 NOT_SET,所以回请求 Long.MAX_VALUE ，即无限个数据。
            if (toRequest == NOT_SET) {
                producer.request(Long.MAX_VALUE);
            } else {
                producer.request(toRequest);
            }
        }
    }
```

　　最后调用了 producer 的 request() 方法。

## 2.4. WeakSingleProducer#request

```java
    static final class WeakSingleProducer<T> implements Producer {
        final Subscriber<? super T> actual; // subscribe 的参数
        final T value; // just 传入的数值
        boolean once;

        public WeakSingleProducer(Subscriber<? super T> actual, T value) {
            this.actual = actual;
            this.value = value;
        }

        @Override
        public void request(long n) {
            if (once) {
                return;
            }
            if (n < 0L) {
                throw new IllegalStateException("n >= required but it was " + n);
            }
            if (n == 0L) {
                return;
            }
            once = true;
            Subscriber<? super T> a = actual;
            if (a.isUnsubscribed()) {
                return;
            }
            T v = value;
            try {
                // 向观察者发送通知
                // v 是 just 设置的数值
                a.onNext(v);
            } catch (Throwable e) {
                Exceptions.throwOrReport(e, a, v);
                return;
            }

            if (a.isUnsubscribed()) {
                return;
            }
            // 向观察者发送通知
            a.onCompleted();
        }
    }
```

　　在 request() 中，调用了 subscriber 的 onNext() 和 onCompleted()，那么 Hello World 就传递到了 Action 中，并被打印出来了。

## 2.5. 完整的过程

![](image/RxJava_call_stack_just.png)

　　一切行为都由 subscribe 触发，而且都是直接的函数调用，所以在调用 subscribe 的线程执行。

# 3. map 操作符

## 3.1. Observable#map

```java
    public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
      	// 新建了一个 Observable 并使用新的 OnSubscribeMap 来封装传入的数据，OnSubscribeMap 作为新的 Observable 的 onSubscribe 成员
        return unsafeCreate(new OnSubscribeMap<T, R>(this, func));
    }
    public static <T> Observable<T> unsafeCreate(OnSubscribe<T> f) {
        return new Observable<T>(RxJavaHooks.onCreate(f));
    }
```

　　subcribe() 方法调用的时候会调用 `hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber)` 方法，在这里也就是 OnSubscribeMap 的 call 方法。

## 3.2. OnSubscribeMap

```java
// OnSubscribeMap 是 OnSubscribe 的子类
public final class OnSubscribeMap<T, R> implements OnSubscribe<R> {

    final Observable<T> source;

    final Func1<? super T, ? extends R> transformer;

    public OnSubscribeMap(Observable<T> source, Func1<? super T, ? extends R> transformer) {
      	// 上一次生成的 Observable
        this.source = source;
        // map 的 Func1 成员
        this.transformer = transformer;
    }

  	// 传入的 o 就是 subscribe() 处传入的 Subscriber
    @Override
    public void call(final Subscriber<? super R> o) {
      	// 对传入的 Subscriber 进行再次封装成 MapSubscriber
      	// 具体 Observable.map() 的逻辑是在 MapSubscriber 中
        MapSubscriber<T, R> parent = new MapSubscriber<T, R>(o, transformer);  
        // 加入到另一个 subscriber 中，为了之后一起取消订阅
      	o.add(parent);
      	// 执行订阅
        source.unsafeSubscribe(parent); 
    }

}
```

　　call() 方法的逻辑很简单，只是将例子中 Observable.subscribe() 传入的 Subscriber 分装成 MapSubscriber ，调用 Observable.unsafeSubscribe 方法。

### 3.2.1. Observable#unsafeSubscribe

```java
    public final Subscription unsafeSubscribe(Subscriber<? super T> subscriber) {
        try {
            // new Subscriber so onStart it
            subscriber.onStart();
            // allow the hook to intercept and/or decorate
            // 调用 onSubscribe 的 call 方法，也就是 MapSubscriber 的 call 方法
            RxJavaHooks.onObservableStart(this, onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // if an unhandled error occurs executing the onSubscribe we will propagate it
            try {
                subscriber.onError(RxJavaHooks.onObservableError(e));
            } catch (Throwable e2) {
                Exceptions.throwIfFatal(e2);
                // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                // so we are unable to propagate the error correctly and will just throw
                RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                // TODO could the hook be the cause of the error in the on error handling.
                RxJavaHooks.onObservableError(r);
                // TODO why aren't we throwing the hook's return value.
                throw r; // NOPMD
            }
            return Subscriptions.unsubscribed();
        }
    }
```

　　Observable 的 unsafeSubscribe 方法调用了 MapSubscriber 的 call 方法。

## 3.3. MapSubscriber

```java
	static final class MapSubscriber<T, R> extends Subscriber<T> {

        final Subscriber<? super R> actual;

        final Func1<? super T, ? extends R> mapper;

        boolean done;

        public MapSubscriber(Subscriber<? super R> actual, Func1<? super T, ? extends R> mapper) {
          	// Observable.subscribe() 传入的 Subscriber
            this.actual = actual;
            // map 的 Func1 成员
            this.mapper = mapper;
        }

        @Override
        public void onNext(T t) {
            R result;

            try {
              	// 调用 map 的 Func1 对象的 call 方法，对数据进行了交换
                result = mapper.call(t);  //1
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                unsubscribe();
                onError(OnErrorThrowable.addValueAsLastCause(ex, t));
                return;
            }
			// 将转换后的数据发送给观察者，也就是传递给下流
            actual.onNext(result); //2
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaHooks.onError(e);
                return;
            }
            done = true;

            actual.onError(e);
        }


        @Override
        public void onCompleted() {
            if (done) {
                return;
            }
            actual.onCompleted();
        }

        @Override
        public void setProducer(Producer p) {
            actual.setProducer(p);
        }
    }
```

　　just 在 map 的上面，Action1 在 map 的下面，数据从 just 传递到 map 再传递到 Action1，所以对于 map 来说，just 就是上游，Action1 就是下游。数据是从上游（Observable）一路传递到下游（Subscriber）的，请求则相反，从下游传递到上游。

　　MapSubscriber 的 call 方法中解决了对数据的转换。

## 3.4. 完整的过程

　　map 的完整调用过程图：

![](image/RxJava_call_stack_just_map.png)

　　上面的流程依然由 subscribe 触发，而且都是直接的函数调用，所以都在调用 subscribe 的线程执行。

# 4. interval

　　interval 操作符是创建一个按固定时间间隔发送整数序列的 Observable。

```java
    public static Observable<Long> interval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        return unsafeCreate(new OnSubscribeTimerPeriodically(initialDelay, period, unit, scheduler));
    }
```

　　可以看出 interval() 和 map() 一样都是通过生成新的 Observable 并向 Observable 中传入与之对应的 OnSubscribe 的子类来完成具体操作。

## 4.1. OnSubscribeTimerPeriodically

```java
public final class OnSubscribeTimerPeriodically implements OnSubscribe<Long> {
    final long initialDelay;
    final long period;
    final TimeUnit unit;
    final Scheduler scheduler;

    public OnSubscribeTimerPeriodically(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }
		
  	// 传入的 Subscriber 为 OnSubscribeMap.call() 方法中 source.unsafeSubscribe(parent)；
    @Override
    public void call(final Subscriber<? super Long> child) {
        final Worker worker = scheduler.createWorker();
        child.add(worker);
        worker.schedulePeriodically(new Action0() {
            long counter;
            @Override
            public void call() {
                try {
                    child.onNext(counter++);
                } catch (Throwable e) {
                    try {
                        worker.unsubscribe();
                    } finally {
                        Exceptions.throwOrReport(e, child);
                    }
                }
            }

        }, initialDelay, period, unit);
    }
}

```

　　在指定的 scheduler 线程中按固定时间调用 subscriber 的 onNext() 方法，也就是通知观察者。

# 5. 参考文章

1. [拆轮子系列：拆 RxJava](https://blog.piasy.com/2016/09/15/Understand-RxJava/index.html)

2. [RxJava 源码解析之观察者模式](https://juejin.im/post/58dcc66444d904006dfd857a)