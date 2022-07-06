# EventBus 的原理

## EventBus 的使用

### 1. 定义事件

定义一个事件的封装对象。在程序内部就使用该对象作为通信的信息：

```java
public class MessageWrap {

    public final String message;

    public static MessageWrap getInstance(String message) {
        return new MessageWrap(message);
    }

    private MessageWrap(String message) {
        this.message = message;
    }
}
```

### 2. 发送事件

然后定义一个Activity：

```java
@Route(path = BaseConstants.LIBRARY_EVENT_BUS_ACTIVITY1)
public class EventBusActivity1 extends CommonActivity<ActivityEventBus1Binding> {

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        // 为按钮添加添加单击事件
        getBinding().btnReg.setOnClickListener(v -> EventBus.getDefault().register(this));
        getBinding().btnNav2.setOnClickListener( v ->
                ARouter.getInstance()
                        .build(BaseConstants.LIBRARY_EVENT_BUS_ACTIVITY2)
                        .navigation());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(MessageWrap message) {
        getBinding().tvMessage.setText(message.message);
    }
}
```

当按下按钮的时候向 EventBus 注册监听，然后按下另一个按钮的时候跳转到另一个Activity，并在另一个 Activity 发布输入的事件。在上面的Activity中，会添加一个监听的方法，即 `onGetMessage`，这里需要为其加入注解`Subscribe`并指定线程模型为主线程`MAIN`。最后，就是在Activity的`onDestroy`方法中取消注册该Activity。

下面是另一个 Activity 的定义，在这个 Activity 中，当按下按钮的时候从 EditText 中取出内容并进行发布，然后退出到之前的 Activity，以测试是否正确监听到发布的内容。

```java
@Route(path = BaseConstants.LIBRARY_EVENT_BUS_ACTIVITY2)
public class EventBusActivity2 extends CommonActivity<ActivityEventBus2Binding> {

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        getBinding().btnPublish.setOnClickListener(v -> publishContent());
    }

    private void publishContent() {
        String msg = getBinding().etMessage.getText().toString();
        EventBus.getDefault().post(MessageWrap.getInstance(msg));
        ToastUtils.makeToast("Published : " + msg);
    }
}
```

根据测试的结果，的确成功地接收到了发送的信息。

## EventBus 的原理

### 1. 订阅

`EventBus.getDefault().register(this)` 就会注册 EventBusActivity1 对象为订阅者。

这个过程分为两步：
1、获取 **EventBusActivity1对象** 中方法有 @Subscribe 修饰且参数有且仅有一个的列表。
2、把这些信息记录起来，以供后续发送者发送消息时通知使用。

```java
    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * <p/>
     * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
     * The {@link Subscribe} annotation also allows configuration like {@link
     * ThreadMode} and priority.
     */
    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
      // 1、获取订阅者有 @Subscribe 修饰且参数有且仅有一个的方法的列表。
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
      	//2、把这些信息记录起来，以供后续发送者发送消息时通知使用。
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
              	// 注册订阅
                subscribe(subscriber, subscriberMethod);
            }
        }
    }
```

#### 1.1. 获取订阅者中方法有 @Subscribe 修饰且参数有且仅有一个的列表

```java
		// 存着注册类与其所有需要回调的 Event 方法列表的键值对
		private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();

		List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
      	// 先从缓存中取
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

      	// 判断是否忽略注解生成器生成的 MyEventBusIndex
        if (ignoreGeneratedIndex) {
          	// 通过反射获取
            subscriberMethods = findUsingReflection(subscriberClass);
        } else {
            subscriberMethods = findUsingInfo(subscriberClass);
        }
      	// 如果订阅者中不存在被 @Subscribe 注解的 public 的方法，则抛出异常
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        } else {
          	// 如果订阅者中存在订阅方法，放入缓存中
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
          	// 返回集合
            return subscriberMethods;
        }
    }
```

1. 类的 SubscribeMethod 有个缓存，如果有缓存，则直接返回缓存了。
2. 如果没有缓存，ignoreGeneratedIndex 默认是 false，则调用 findUsingInfo(subscriberClass) 方法获取，但是如果没有引入注解，则注解信息也是空的，也和 findUsingReflection() 方法一样，调用 findUsingReflectionInSingleClass() 方法，详细可以看 1.2。
3. 最后会将查询到的订阅方法存入缓存。

```java
    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
      	// 初始化 FindState
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
      	// while 循环中，不停地去反射获取当前类和其父类的订阅方法并添入列表中
      	// 注意，在 java 中，如果当前类实现了一个接口，即使该接口的方法被 @Subscribe 所修饰，
      	// 当前类中的方法也是不包含该注解属性的，所以如果在接口中对某个方法使用了 @Subscribe 修饰然后让类去实现这个接口是没有任何作用的
        while (findState.clazz != null) {
            findUsingReflectionInSingleClass(findState);
            findState.moveToSuperclass();
        }
      	// 最终返回这个列表并重置 FindState 对象利于下一次重复使用
        return getMethodsAndRelease(findState);
    }
```

反射方法中调用了 findUsingReflectionInSingleClass 方法，findUsingReflectionInSingleClass 通过 Java 的反射和对注解的解析查找订阅方法，并保存到 FindState 中，代码如下：

```java
private void findUsingReflectionInSingleClass(FindState findState) {
    Method[] methods;
    try {
        // This is faster than getMethods, especially when subscribers are fat classes like Activities
      	// 获得该类某个方法
        methods = findState.clazz.getDeclaredMethods();
    } catch (Throwable th) {
        // Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
        methods = findState.clazz.getMethods();
        findState.skipSuperClasses = true;
    }
    for (Method method : methods) {
        int modifiers = method.getModifiers();
      	// 是否是 public 方法
        if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
            Class<?>[] parameterTypes = method.getParameterTypes();
          	// 参数为 1
            if (parameterTypes.length == 1) {
              	// 有 Subscribe 注解
                Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                if (subscribeAnnotation != null) {
                  	// eventType 是接收的参数，也就是 MessageWrap
                    Class<?> eventType = parameterTypes[0];
                    if (findState.checkAdd(method, eventType)) {
                        ThreadMode threadMode = subscribeAnnotation.threadMode();
                      	// 创建一个 SubscriberMethod 对象，包含 method、eventType、threadMode、subscribeAnnotation.priority()、subscribeAnnotation.sticky() 等信息
                      	// 将创建的 SubscriberMethod 对象存储在 findState 的 subscriberMethods 对象中
                        findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                    }
                }
            } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new EventBusException("@Subscribe method " + methodName +
                        "must have exactly 1 parameter but has " + parameterTypes.length);
            }
        } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
            String methodName = method.getDeclaringClass().getName() + "." + method.getName();
            throw new EventBusException(methodName +
                    " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
        }
    }
}
```
看一下 findState.subscriberMethods 的内容：

```java
final List<SubscriberMethod> subscriberMethods = new ArrayList<>();
```

findState.subscriberMethods 是一个列表。

SubscriberMethod 类，有订阅者事件处理方法的所有信息：

```java
/** Used internally by EventBus and generated subscriber indexes. */
public class SubscriberMethod {
    final Method method; // 方法
    final ThreadMode threadMode; // 线程模式
    final Class<?> eventType; // 事件
    final int priority; // 优先级
    final boolean sticky; // 是否黏性
    }
    
```

#### 1.2. subscribe

在 register 方法里面，获取到 subscriberMethods 列表后，会调用 subscribe(subscriber, subscriberMethod); 对所有的订阅方法进行注册：

```java
				//2、把这些信息记录起来，以供后续发送者发送消息时通知使用。
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
              	// 注册订阅
              	// subscriber 就是 EventBusActivity1.this，subscriberMethod 就是 @subscrie 注解的方法
                subscribe(subscriber, subscriberMethod);
            }
        }
```

```java
    // Must be called in synchronized block
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
      	// 根据订阅者何订阅方法构造一个订阅事件
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        // 根据 EventType （EventType 就是 MessageWrap ）找到订阅事件，从而去分发事件，处理事件
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
          // 订阅者已经注册则抛出 EventBusException
            if (subscriptions.contains(newSubscription)) {
                throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                        + eventType);
            }
        }
				// 遍历订阅事件，找到比 subscriptions 中订阅事件优先级小的位置，然后插进去
      	// 对所有订阅 eventType 进行优先级排序
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

        // 通过订阅者获取该订阅者所订阅事件的集合
        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
      
        // 将当前的订阅事件添加到 subscribedEvents 中
        subscribedEvents.add(eventType);

      	// 如果是黏性事件的话，就立即投递、执行
        if (subscriberMethod.sticky) {
          	// 默认为 true
            if (eventInheritance) {
                // Existing sticky events of all subclasses of eventType have to be considered.
                // Note: Iterating over all events may be inefficient with lots of sticky events,
                // thus data structure should be changed to allow a more efficient lookup
                // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
                Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                for (Map.Entry<Class<?>, Object> entry : entries) {
                    Class<?> candidateEventType = entry.getKey();
                    if (eventType.isAssignableFrom(candidateEventType)) {
                        Object stickyEvent = entry.getValue();
                        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                    }
                }
            } else {
                Object stickyEvent = stickyEvents.get(eventType);
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    }
```

subscriptionsByEventType 一个 map：

* Key：事件类型，如 MessageWrap.class
* value：一个按照订阅方法优先级排序的订阅者的列表集合。Subscription

subscriptionsByEventType 存储了订阅事件到所有该事件订阅者的一个映射，只有发送消息的时候，会直接从这里取出所有订阅了此事件的订阅者，依次通知，就完成事件的分发。

Subscription 代表了订阅者。包含订阅者对象和订阅者事件订阅方法。

```java
final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;
    }
```

至此，整个订阅过程完毕。

### 2. post

下面来看一下 post 方法，代码如下：

```java
    /** Posts the given event to the event bus. */
    public void post(Object event) {
      	// PostingThreadState 保存着事件队列和线程状态信息
        PostingThreadState postingState = currentPostingThreadState.get();
      	// 获取事件队列，并将当前时间插入到事件队列中
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);

      	// 保证不会被多次执行
        if (!postingState.isPosting) {
            postingState.isMainThread = isMainThread();
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
                // 遍历处理队列中的事件
                while (!eventQueue.isEmpty()) {
                    
                  // post 单个事件
                  postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
              	// 重置状态
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }
```

post 方法中调用了 postSingleEvent 处理单个事件，代码如下：

```java
    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;
      	// eventInheritance 表示是否向上查找事件的父类，默认为 true
        if (eventInheritance) {
          	// 取出 Event 及其父类和接口的 class 列表
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        } else {
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }
      	// 找不到该事件时的异常处理
        if (!subscriptionFound) {
            if (logNoSubscriberMessages) {
                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
            }
            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                    eventClass != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, event));
            }
        }
    }
```

postSingleEvent中又调用了postSingleEventForEventType，代码如下

```java
    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
          	// 取出该事件对应的 Subscription 集合
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                  	// 对事件进行处理
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }
```

postSingleEventForEventType 中又调用了 postToSubscription，代码如下,通过下面代码也可以知道之前介绍五种 ThreadMode 的不同之处。

```java
    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
      	// 取出订阅方法的线程模式，之后根据线程模式来分别处理
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
           			// 直接执行 invokeSubscriber() 方法，内部直接采用反射调用
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
            		// 判断当前是否在 UI 线程
                if (isMainThread) {
                  	// 直接采用反射调用
                    invokeSubscriber(subscription, event);
                } else {
                  	// 把当前的方法加入到队列之中，然后通过 handler 去发送一个消息，在 handler 的 handleMessage 中去执行方法
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;
            case MAIN_ORDERED:
            		// 与 MAIN 类似，不过是确保是顺序执行的
                if (mainThreadPoster != null) {
                    mainThreadPoster.enqueue(subscription, event);
                } else {
                    // temporary: technically not correct as poster not decoupled from subscriber
                    invokeSubscriber(subscription, event);
                }
                break;
            case BACKGROUND:
            		// 判断当前是否在 UI 线程
                if (isMainThread) {
                    backgroundPoster.enqueue(subscription, event);
                } else {
                  	// 直接采用反射调用
                    invokeSubscriber(subscription, event);
                }
                break;
            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
            default:
                throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }
```

ThreadMode l列表：

- POSTING
- MAIN
- MAIN_ORDERED
- BACKGROUND
- ASYNC

`POSTING` 默认的线程模式。订阅者的订阅方法将被调用的线程 和 post 事件时所在的线程一致，通过反射直接调用。这个方式会阻塞 `posting` thread，所以尽量避免做一些耗时的操作，因为有可能阻塞的是 主线程。

`MAIN`  如果发送事件时在Android的主线程，则订阅者将被直接调用（blocking）。 如果发送事件时不在Android 主线程，则会把事件放入一个队列，等待挨个处理（not-blocking）。

`MAIN_ORDERED` 和 `MAIN` 不一样的是，它总是通过 Android的 `Handler`机制把事件包装成消息，放入主线程的消息队列。它总是 `not-blocing` 的。

`BACKGROUND` 代表执行订阅者订阅方法总是在子线程中。如果 post 事件所在的线程是子线程，则就在当前线程执行 订阅者的订阅方法（blocking）； 如果调用post 事件所在的线程是主线程，会开启一个线程，执行订阅者 订阅方法（有用到线程池）(not-blocking)。

通过 `BACKGROUND` 会尝试尽量在开启的线程中多处理几次发送的事件，虽然是通过线程池开启的线程，可能想一次尽可能的使用线程的资源。如果在此线程从 事件队列里取事件分发时，一直有事件塞进事件队列的话，则它就会一直循环处理事件的分发。

`ASYNC`  也是表示订阅者的订阅方法会在子线程中处理。 和 `BACKGROUND` 不一样的是，无论如何它每次都会把事件包装在一个新线程中去执行（not-blocking）（这句话有瑕疵，因为是通过线程池控制的，所以运行时难免是线程池中缓存的线程）。

#### 2.1. 线程

看一下 AsyncPoster 的实现：

```java
class AsyncPoster implements Runnable, Poster {

    private final PendingPostQueue queue;
    private final EventBus eventBus;

    AsyncPoster(EventBus eventBus) {
        this.eventBus = eventBus;
      	// 队列
        queue = new PendingPostQueue();
    }

  	// 入队
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        queue.enqueue(pendingPost);
        eventBus.getExecutorService().execute(this);
    }

  	// 运行
    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if(pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        eventBus.invokeSubscriber(pendingPost);
    }

}
```

`AsyncPoster` 是每当有事件发送到消息队列中时，都会使用线程池开启一个子线程，去处理这段耗时的操作。

看一下 PendingPostQueue：

```java
final class PendingPostQueue {
    private PendingPost head;
    private PendingPost tail;

  	// 入队列
    synchronized void enqueue(PendingPost pendingPost) {}

  	// 出队列
    synchronized PendingPost poll() {}

}
```

`PendingPostQueue` 是有链表组成的队列，保存了 `head` 和 `tail` 引用方便入队、出队的操作。 此队列中的数据元素是 `PendingPost` 类型。封装了 `event` 实体 和  `Subscription`订阅者实体。

```java
final class PendingPost {
  	// 是一个静态的 List，保存了回收的 PendingPost
    private final static List<PendingPost> pendingPostPool = new ArrayList<PendingPost>();

    Object event; // 事件
    Subscription subscription; // 订阅者
    PendingPost next; // 下一个事件
  	...
}
```

PendingPost 使用了一个容器缓存使用过的元素，可以节省元素的创建事件，针对这种频繁创建的对象，使用这种方式挺不错的（提升性能、避免内存移除，就是享元模式）。

#### 2.2. 回调订阅方法

postToSubscription 处理完成后又会调用invokeSubscriber 去回调订阅方法

```java
    void invokeSubscriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        if (subscription.active) {
            invokeSubscriber(subscription, event);
        }
    }

		void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            handleSubscriberException(subscription, event, e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }
```

这样就将消息通知过去了。

### 3. unregister

接下来看一下注销的方法 unregister，代码如下

```java
    /** Unregisters the given subscriber from all event classes. */
    public synchronized void unregister(Object subscriber) {
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            for (Class<?> eventType : subscribedTypes) {
              // 对 subscribeByEventType 移除了该 subscriber 的所有订阅消息
                unsubscribeByEventType(subscriber, eventType);
            }
            
					// 移除了注册对象和其对应的所有 Event 事件链表
          typesBySubscriber.remove(subscriber);
        } else {
            logger.log(Level.WARNING, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }
```



## EventBusAnnotationProcessor

如果使用 EventBusAnnotationProcessor（注解分析生成索引）技术，就会大大提高 EventBus 的运行效率。

### 引入

```groovy
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ eventBusIndex : 'com.example.myapp.MyEventBusIndex' ]
            }
        }
    }
}

dependencies {
    def eventbus_version = '3.2.0'
    implementation "org.greenrobot:eventbus:$eventbus_version"
    annotationProcessor "org.greenrobot:eventbus-annotation-processor:$eventbus_version"
}
```

此时需要先编译一次，生成索引类。编译成功之后，就会发现在\ProjectName\app\build\generated\source\apt\PakageName\（可能路径不一样，不过都是在 generated目录下）下看到通过注解分析生成的索引类，这样便可以在初始化 EventBus 时应用生成的索引了。

```java
/** This class is generated by EventBus, do not edit. */
public class MyEventBusIndex implements SubscriberInfoIndex {
    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;

    static {
        SUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();
        
        putIndex(new SimpleSubscriberInfo(com.daddyno1.eventbusdemo.MainActivity.class, true,
                new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("onHandlerMsg", com.daddyno1.eventbusdemo.MsgEvent.class, ThreadMode.POSTING, 2, false),
            new SubscriberMethodInfo("onHandlerMsg2", com.daddyno1.eventbusdemo.MsgEvent.class, ThreadMode.POSTING, 4,
                    false),
        }));

        putIndex(new SimpleSubscriberInfo(com.daddyno1.eventbusdemo.BaseActivity.class, true,
                new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("onHandlerMsg3", com.daddyno1.eventbusdemo.MsgEvent.class, ThreadMode.POSTING, 6,
                    false),
        }));

    }

    private static void putIndex(SubscriberInfo info) {
        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);
    }

    @Override
    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);
        if (info != null) {
            return info;
        } else {
            return null;
        }
    }
}
```

### 使用

```java
EventBus.builder().addIndex(MyEventBusIndex()).installDefaultEventBus();
```

#### 订阅

```java
@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public fun onEvent() {

    }
```

#### 发布

```java
EventBus.getDefault().postSticky(this)
```



### 源码解析

在上面 findSubscriberMethods 方法代码中 ignoreGeneratedIndex 默认为 false 的，项目中就会通过 EventBus 单例模式来获取默认的 EventBus 对象，也就是 ignoreGeneratedIndex 为 false 的情况，这种情况会调用了 findUsingInfo 方法：

```java
    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
          	// 获取订阅者信息，没有配置 MyEventBusIndex 返回 null
            findState.subscriberInfo = getSubscriberInfo(findState);
            if (findState.subscriberInfo != null) {
              	// 如果通过 EventBusBuilder 配置了 MyEventBusIndex，便会获取到 subscriberInfo，
                // 调用 subscriberInfo 的 getSubscriberMethods 方法可以得到订阅方法相关的信息,
              	// 这个时候就不再需要通过注解进行获取订阅方法
                SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                for (SubscriberMethod subscriberMethod : array) {
                  	// 将订阅方法保存到 findState
                    if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
              	// 通过反射查找订阅方法
                findUsingReflectionInSingleClass(findState);
            }
            findState.moveToSuperclass();
        }
      	// 对 findState 做回收处理并返回订阅方法的 List 集合
        return getMethodsAndRelease(findState);
    }
```

在 findUsingInfo 方法中，如果配置了 MyEventBusIndex，那么就会获取到 subscriberInfo，然后拿到 SubscriberMethod 数组信息，将这些信息添加到 findState 的 subscriberMethods 列表中。

后面就和反射查询到 subscriberMethods 列表后一样了。

## EventBus 的黏性事件原理

发送黏性事件是通过 postSticky() 方法发送的：

```java
    public void postSticky(Object event) {
    		// 为避免多线程操作 postSticky(Object) 和 removeStickyEvent(Class<?>) 引发的冲突，所以
        synchronized (stickyEvents) {
            stickyEvents.put(event.getClass(), event);
        }
        // Should be posted after it is putted, in case the subscriber wants to remove immediately
        post(event);
    }
```

在 subscribe 方法中如果是黏性事件，就会从 stickyEvents 中取出事件，然后调用 checkPostStickyEventToSubscription 方法分发事件。

## 问题

### 问题1：不同的 activity 注册接收消息，同样的优先级，谁会先收到消息？

谁先调用了 register，谁的消息就会先插入到列表中，就会先收到消息。

### 问题2：同一个 activity 注接受消息，同样的优先级，谁会先收到消息？

哪个方法在前，就会先收到消息。

### 问题3：不同的 activity 注册接收消息，不同的优先级，谁会先收到消息？

高优先级的会先收到消息。

## 其他

### 为什么使用事件总线机制来替代广播？

1. 广播：耗时、容易被捕获（不安全）。
2. 事件总线：更节省资源、更高效，能将信息传递给原生以外的各种对象。

### EventBus2.x 的版本和 3.x 是有很大区别的

1. 2.x 使用的是运行时注解，采用了反射的方式对整个注册的类的所有方法进行扫描来完成注册，因而会对性能有一定影响;
2. 3.x 使用的是编译时注解，Java 文件会编译成 .class 文件，再对 class 文件进行打包等一系列处理。在编译成 .class 文件时，EventBus 会使用 EventBusAnnotationProcessor 注解处理器读取 @Subscribe() 注解并解析、处理其中的信息，然后生成 Java 类来保存所有订阅者的订阅信息。这样就创建出了对文件或类的索引关系，并将其编入到 apk 中；
3. 从 EventBus3.0 开始使用了对象池缓存减少了创建对象的开销；

### 跨进程问题

目前 EventBus 只支持跨线程，而不支持跨进程。如果一个 app 的 service 起到了另一个进程中，那么注册监听的模块则会收不到另一个进程的 EventBus 发出的事件。这里可以考虑利用 IPC 做映射表，并在两个进程中各维护一个 EventBus，不过这样就要自己去维护 register 和 unregister 的关系，比较繁琐，而且这种情况下通常用广播会更加方便。

### RxBus 与 EventBus 比较

其实也就是 rxJava 的优点:

1. RxJava 的 Observable 有 onError、onComplete 等状态回调；
2. RxJava 使用组合而非嵌套的方式，避免了回调地狱；
3. RxJava 的线程调度设计的更加优秀，更简单易用；
4. RxJava 可使用多种操作符来进行链式调用来实现复杂的逻辑；
5. RxJava 的信息效率高于 EventBus2.x，低于 EventBus3.x；

那么技术选型时如何取舍呢？如果项目中使用了RxJava，则使用 RxBus，否则使用 EventBus3.x；

### LiveDataBus

LiveDataBus 是基于 LiveData 实现的类似 EventBus 的消息通信框架，它是基于 LiveData 实现的，完全可以代替 EventBus，RxBus。

#### 为什么会有 LiveDataBus 呢？

* Handler : 容易导致内存泄漏，空指针，高耦合，不利于维护。

* EventBus ：原理实现复杂，无法混淆，需要手动绑定生命周期。

* RxBus：依赖于 RxJava，包太大，影响 apk 大小，app 启动时间。

了解 LiveDataBus：https://github.com/JeremyLiao/LiveEventBus。

### 事件总线的考量

其实目前常用的各种事件总线 xxBus 原理都差不多，那么在项目中如何使用这些事件总线呢：

1. EventBus、RxBus：将 xxEvent 消息容器和事件总线框架的依赖放到 base module，其他模块组件依赖于 base module；但是这样每个模块改动都需要增删改 baseModule 中的消息容器, 组件化要求功能模块独立, 各组件应该尽量避免影响 base module；
2. LiveDataBus: 无需建立消息模型，但无法像前两者一样拥有类名索引，无法引导正确的编写代码，也无法传递自定义实体到其他模块；
3. 使用EventBus、RxBus，为了更大程度的解耦，可以独立出一个事件总线 module，添加事件的实体都在这个 module 中，base module 依赖这个事件总线 module 对事件通信的解耦，抽离事件到事件总线 module 中减少对 base module 的影响。

## 参考文章
2. [一篇讲明白EventBus](https://www.jianshu.com/p/633348569198)
2. [EventBus的粘性事件原理](https://www.jianshu.com/p/47d0a0cd975d)
3. [探索Android开源框架 - 5. EventBus使用及源码解析](https://www.jianshu.com/p/e7b4cae546f3)

