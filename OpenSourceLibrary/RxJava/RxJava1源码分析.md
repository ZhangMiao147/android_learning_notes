# RxJava 1 源码分析

## 1. 概述

　　RxJava 是响应式编程（Reactive Extensions）在 JVM 平台上的实现，即用 Java 语言实现的一套基于观察者模式的异步编程接口。

　　RxJava 是使用观察者模式实现的。

## 2. 观察者模式

### 2.1. 传统观察者模式

　　观察者模式面向的需求是： A 对象（观察者）对 B 对象（被观察者）的某种变化高度敏感，需要在 B 变化的一瞬间做出反应。

　　在程序中，观察者采用注册（Register）或者称为订阅（Subscribe）的方式，告诉被观察者：我需要你的某某状态，你要在它变化的时候通知我。

　　Android 开发中一个比较典型的例子是点击监听器 onClickListener。对设置 onClickListener 来说，View 是被观察者，onClickListener 是观察者，二者通过 setOnClickListener() 方法达到订阅关系。订阅之后用户点击按钮的瞬间，Android Framework 就会将点击事件发送给已经注册的 onClickListener。

　　采取这样被动的观察方式，即省去了反复检索状态的资源消耗，也能够得到最高的反馈速度。

　　观察者模式：

![](image/观察者模式.png)

　　而 RxJava 作为一个工具库，使用的就是通用形式的观察者模式。

### 2.2. RxJava 中观察者模式

　　RxJava 有四个基本概念：Observable（可观察者，即被观察者）、Observer（观察者）、subscribe（订阅）、事件。Observable 和 Observer 通过 subscribe() 方法实现订阅关系，从而 Observable 可以在需要的时候发出事件来通知 Observer。

　　与传统观察者模式不同，RxJava 的事件回调方法除了普通的 onNext 之外，还定义了两个特殊的事件：onCompleted() 和 onError()。

* onCompleted()：事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志。
* onError()：事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
* 在一个正确运行的事件序列中，onCompleted() 和 onError() 有且只有一个，并且是事件序列中的最后一个。需要注意的是，omCompleted() 和 onError() 二者也是互斥的，即在队列中调用了其中一个，就不应该再调用另一个。并且只要 onCompleted() 和 onError() 中有一个调用了，都会中止 onNext() 的调用。

　　RxJava 的观察者模式大致如下图：

![](image/RxJava的观察者模式.png)

## 3. 基本实现

### 3.1. 创建 Observer

　　Observer 即观察者，它决定事件触发的时候将有怎样的行为。RxJava 中的 Observer 接口的实现方式：

```java
        Observer<String> observer = new Observer<String>() {
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
                Log.d(TAG, "onNext");
            }
        };
```

　　除了 Observer 接口之外，RxJava 还内置了一个实现了 Observer 的抽象类：Subscriber。

　　Subscriber 对 Observer 接口进行了一些扩展，但他们的基本使用方式是完全一样的：

```java
        Subscriber<String> subscriber = new Subscriber<String>() {
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
                Log.d(TAG, "onNext");
            }
        };
```

　　不仅基本使用方式一样，实质上，在 RxJava 的 subscribe 过程中，Observer 也总是会先被转换成一个 Subscriber 再使用。

#### 3.1.1. Observer 和 Subscriber 的区别

　　如果只是使用基本功能，选择 Observer 和 Subscriber 是完全一样的。它们的区别对于使用者来说主要有两点：

1. onStart()：这是 Subscriber 增加的方法。

   它会在 subscribe 刚开始，而事件还未发送之前被调用，可以用于做一些准备工作。例如数据的清零或重置。

   这是一个可选方法，默认情况下它的实现为空。

   需要注意的是，如果对准备工作的线程有要求（例如弹出一个显示进度的对相框，这必须在主线程执行），onStart() 就不适用了，因为它总是在 subscribe 所发生的线程被调用，而不能指定线程。

   要在指定的线程来做准备工作，可以使用 doOnSubscribe() 方法。

2. unsubscribe()：这是 Subscriber 所实现的另一个接口 Subscription 的方法，用于取消订阅。

   在这个方法被调用后，Subscriber 将不再接受事件。

   一般在这个方法调用前，可以使用 isUnsubscribed() 先判断一下状态。

   unsubscribe() 这个方法很重要，因为在 subscribe() 之后，Observable 会持有 Subscriber 的引用，这个引用如果不能及时被释放，将有内存泄漏的风险。

   所以最好保持一个原则：要在不再使用的时候尽快在合适的地方（例如 onPause()、onStop() 等方法中）调用 unsubscribe() 来解除引用关系，以避免内存泄漏的发生。

### 3.2. 创建 Observable

　　Observable 即被观察者，它决定什么时候触发事件以及触发怎样的事件。例如 create() 方法：

```java
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("1");
                subscriber.onNext("2");
                subscriber.onNext("3");
                subscriber.onCompleted();
            }
        });
```

　　可以看到，这里传入了一个 OnSubscribe 对象作为参数。OnSubscribe 会被存储在返回的 Observable 对象中，它的作用相当于一个计划表，当 Observable 被订阅的时候，OnSubscribe 的 call() 方法会自动被调用，事件序列就会依照设定一次触发。这样，由被观察者调用了观察者的回调方法，就实现了由被观察者向观察者的事件传递，即观察者模式。

　　create() 方法是 RxJava 最基本的创造事件序列的方法。基于这个方法，RxJava 还提供了一些方法用来快捷创建事件队列。例如 just()、from()。

### 3.3. 订阅 Subscribe

　　创建了 Observable 和 Observer 之后，再用 subscribe() 方法将它们联结起来，整条链子就可以工作了。

　　代码形式很简单：

```java
observable.subscribe(observer);

// 或者
observable.subscribe(subscribe);
```

## 4. RxJava 的基本订阅流程

　　一个简单的 RxJava 的使用：
```java
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

### 4.1.  Observable#create

```java
public class Observable< T > {

    final OnSubscribe<T> onSubscribe;
    public static <T> Observable<T> create(OnSubscribe<T> f) {
        // 返回 Observable 的实例
        // hook.onCreate(f) 返回的就是 f，也就是 OnSubscribe 对象
        return new Observable<T>(hook.onCreate(f)); 
    }

	protected Observable(OnSubscribe<T> f) {
        // 将传递进来的 OnSubscribe 设置为 onSubscribe 的值
        this.onSubscribe = f; 
    }
	...
}
```
　　Observable.create() 方法返回了一个 Observable 实例对象，并且将参数 OnSubscribe< T > f 存储为成员 onSubscribe。

### 4.2. Obervable#subscribe()

```java
public class Observable< T > {

	...
    public final Subscription subscribe(final Action1<? super T> onNext) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }

        Action1<Throwable> onError = InternalObservableUtils.ERROR_NOT_IMPLEMENTED;
        Action0 onCompleted = Actions.empty();
        // 对传入的 Action 进行包装，包装为 ActionSubscriber，一个 Subscriber 的实现类。
        return subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }
    
    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }

        Action0 onCompleted = Actions.empty();
        return subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onCompleted) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }
        if (onCompleted == null) {
            throw new IllegalArgumentException("onComplete can not be null");
        }

        return subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }
    
    public final Subscription subscribe(final Observer<? super T> observer) {
        // 如果是 Subscriber 的子类，直接转化为 Subscriber
        if (observer instanceof Subscriber) {
            return subscribe((Subscriber<? super T>)observer);
        }
        if (observer == null) {
            throw new NullPointerException("observer is null");
        }
        // ObserverSubscriber 是 Subscriber 的子类
        return subscribe(new ObserverSubscriber<T>(observer));
    }
    
    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }

	static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
        ...

        // new Subscriber so onStart it
        // 调用 subscriber.onStart() 通知 subscriber 它已经和 onservable 连接起来了。
        // 在这里就直到，onStart() 就是在调用 subscriber() 的线程执行的。
        // 可以用于一些准备工作，例如数据的清零或重制，默认情况下它的实现为空
        subscriber.onStart();

        /*
         * See https://github.com/ReactiveX/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls
         * to user code from within an Observer"
         */
        // if not already wrapped
        // 如果传入的 subscriber 不是 SafeSubscriber，那就把它包装为一个 SafeSubscriber
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
          	// 强制转化为 SafeSubscriber 是为了保证 onCompleted 或 onError 调用的时候会中止 onNext 的调用
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
          	// hook.onObservableStart() 默认返回的就是 observable.onSubscribe
           	// observable.onSubscribe 就是调用 Observable#create 方法时传递的参数
            // 调用 onSubscribe 的 call 方法
            hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber);
          	// hook.onObservableReturn() 默认也是返回 subscriber
            // subscriber 继承了 Subscription，用于取消订阅。
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
            ...
        }
    }
	...
}
```
　　SafeSubscriber 的作用：保证 Subscriber 实例遵循 Observable contract。

　　subscribe() 的重载方法很多，但是最后都会调用到 Subscription subscribe(Subscriber<? super T> subscriber) 方法中。

　　通过源码可以看到：subscriber() 实际就做了 4 件事情：

1. 调用 Subscriber.onStart() 。
2. 如果 subscriber 不是 SafeSubscriber 类型，将传入的 Subscriber 转化为 SafeSubscriber，这是为了保证 onCompleted 或 onError 调用的时候会中止 onNext() 的调用，而将 subscriber 作为 SafeSubscriber 的 actual 成员。
3. 调用 Observable 中的 OnSubscribe.call(Subscriber)。在这里，事件发送的逻辑开始运行。从这也可以看出，在 RxJava 中，Observable 并不是在创建的时候就立即开始发送事件，而是在它被订阅的时候，即当 subscribe() 方法执行的时候开始运行。
4. 被转化后的 SafeSubscriber 作为 Subscription 返回。这是为了方便 unsubscribe()。

　　这时调用到了在 Observable#call() 方法，在 call() 方法中一般会调用 onNext()、onError()、onCompleted() 方法向下游传递数据，接着来看 SafeSubscribe 的 响应方法。

### 4.4. SafeSubscriber

```java
public class SafeSubscriber<T> extends Subscriber<T> {

    private final Subscriber<? super T> actual;
	  // 通过改标志来保证 onCompleted 或 onError 调用的时候会中止 onNext 的调用
    boolean done; 

    public SafeSubscriber(Subscriber<? super T> actual) {
        super(actual);
        this.actual = actual;
    }

    @Override
    public void onCompleted() {
        if (!done) {
            done = true;
            try {
                actual.onCompleted();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaHooks.onError(e);
                throw new OnCompletedFailedException(e.getMessage(), e);
            } finally { // NOPMD
                try {
                    unsubscribe(); // 取消订阅，结束事务
                } catch (Throwable e) {
                    RxJavaHooks.onError(e);
                    throw new UnsubscribeFailedException(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        Exceptions.throwIfFatal(e);
        if (!done) {
            done = true;
            _onError(e);
        }
    }

    @Override
    public void onNext(T t) {
        try {
          	// done 为 true 时，中止传递
            if (!done) {
                // 调用了 actual 的 onNext() 方法，也就是调用 subscribe() 方法传入的 Subscriber 对象的 onNext()
                // 这样消息就通知到观察者
                actual.onNext(t);
            }
        } catch (Throwable e) {
            Exceptions.throwOrReport(e, this);
        }
    }

    @SuppressWarnings("deprecation")
    protected void _onError(Throwable e) { // NOPMD
        RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
        try {
            actual.onError(e);
        } catch (OnErrorNotImplementedException e2) { // NOPMD
            try {
              	// 取消订阅
                unsubscribe();
            } catch (Throwable unsubscribeException) {
                RxJavaHooks.onError(unsubscribeException);
                throw new OnErrorNotImplementedException("Observer.onError not implemented and error while unsubscribing.", new CompositeException(Arrays.asList(e, unsubscribeException))); // NOPMD
            }
            throw e2;
        } catch (Throwable e2) {
            RxJavaHooks.onError(e2);
            try {
                unsubscribe();
            } catch (Throwable unsubscribeException) {
                RxJavaHooks.onError(unsubscribeException);
                throw new OnErrorFailedException("Error occurred when trying to propagate error to Observer.onError and during unsubscription.", new CompositeException(Arrays.asList(e, e2, unsubscribeException)));
            }

            throw new OnErrorFailedException("Error occurred when trying to propagate error to Observer.onError", new CompositeException(Arrays.asList(e, e2)));
        }
        try {
            unsubscribe();
        } catch (Throwable unsubscribeException) {
            RxJavaHooks.onError(unsubscribeException);
            throw new OnErrorFailedException(unsubscribeException);
        }
    }
    public Subscriber<? super T> getActual() {
        return actual;
    }
}

```

　　通过 SafeSubscriber 中的布尔变量 done 来做标记保证 onCompleted() 和 onError() 二者的互斥性，即在队列中调用了其中一个，就不应该再调用另一个。并且只要 onCompleted() 和 onError() 中有一个调用了，都会中止 onNext() 的调用。

　　在 SafeSubscriber 的 onNext() 方法调用的时 actual 的 onNext() 方法，从上一步可以直到 actual 就是在使用时传递给 subscribe() 方法的参数，这样就将信息通知到了观察者。

### 4.5. 基本订阅流程总结

　　方法的主导只要由 Observable（被观察者） 来，在创建 Observable 的时候，会将 OnSubscribe(订阅操作)传给 Observable(被观察者) 作为成员变量，在调用 subscribe 的方法（订阅）时，将 Subscriber (观察者)作为参数传入，调用 onSubscribe 的 call 方法来处理订阅的事件，OnSubscribe 的 call 方法中调用 Subcriber 的相关方法来通知观察者。

## 5. hook

　　在多种重要的节点上，都有 hook，例如创建 Observable（create）时，有 onCreate，可以进行任意想要的操作，记录、修饰、甚至抛出异常；以及和 scheduler 相关的内容，获取 scheduler 时，都可以继续想要的操作，例如让 Scheduler.io() 返回立即执行的 scheduler。

　　这些内容可以执行高度自定义的操作，其中就包括便于测试。

　　其实 hook 的原理并不复杂，在关心的节点（hook point）插桩，这样就可以操控程序在这些节点的行为，至于操控的策略，有一系列函数进行设置以及清理。

　　目前和 hook 相关的内容主要在 RxJavaPlugins 和 RxJavaHooks 这两个类中，后者在 v1.1.7 引入，功能更加强大，使用更加方便。

## 6. 测试

　　RxJava 项目本身测试覆盖率高达 84%，为了便于对使用 RxJava 的代码进行测试，它还专门提供了 TestSubscriber，可以用它来获取事件流中的事件、进行验证、进行等待，使用起来非常简便。

　　此外，hook 机制也可以用来帮助进行测试，例如对线程调度进行一些操控。

## 7. 概括

1. RxJava 主要采用的是观察者模式，Obervable 作为被观察者，负责接收原始的 Observable 发出的事件，并在处理后发送给 Observer，Observer 作为观察者。
2. Observable 并不是在创建的时候就立即开始发送事件，而是在它被订阅的时候，也就是 subscribe() 方法执行的时候开始。
3. subscribe() 方法里会调用 OnSubscribe#call 方法，在 OnSubscribe 的 call 方法会把消息传递给观察者 Subscriber。


## 8. 参考文章
1. [拆轮子系列：拆 RxJava](https://blog.piasy.com/2016/09/15/Understand-RxJava/index.html)

2. [RxJava 源码解析之观察者模式](https://juejin.im/post/58dcc66444d904006dfd857a)