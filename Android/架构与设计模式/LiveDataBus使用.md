# LiveDataBus 使用

## LiveDataBus

LiveDataBus 是基于 LiveData 实现的类似 [EventBus](https://so.csdn.net/so/search?q=EventBus&spm=1001.2101.3001.7020) 的消息通信框架，它是基于 LiveData 实现的。

## LiveDataBus 的优点

1. LiveData 具有的这种可观察性和生命周期感知的能力。
2. 使用者不用显示调用反注册方法，由于 LiveData 具有生命周期感知能力，所以 LiveDataBus 只需要调用注册回调方法，而不需要显示的调用反注册方法。
3. LiveDataBus 的实现极其简单，相对 EventBus 复杂的实现，LiveDataBus 只需要一个类就可以实现。
4. LiveDataBus 可以减小 APK 包的大小，由于 LiveDataBus 只依赖 Android 官方 Android Architecture Components 组件的 LiveData，没有其他依赖，本身实现只有一个类。 

## LiveDataBus 的组成

- 消息
  - 消息可以是任何的 Object，可以定义不同类型的消息，如 Boolean、String。也可以定义自定义类型的消息。
- 消息通道
  - LiveData 扮演了消息通道的角色，不同的消息通道用不同的名字区分，名字是 String 类型的，可以通过名字获取到一个 LiveData 消息通道。
- 消息总线
  - 消息总线通过单例实现，不同的消息通道存放在一个 HashMap 中。
- 订阅
  - 订阅者通过 getChannel 获取消息通道，然后调用 observe 订阅这个通道的消息。
- 发布
  - 发布者通过 getChannel 获取消息通道，然后调用 setValue 或者 postValue 发布消息。

## LiveDataBus 的具体实现

```java
public final class LiveDataBus {

    private final Map<String, BusMutableLiveData<Object>> bus;

    private LiveDataBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveDataBus DEFAULT_BUS = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    public <T> MutableLiveData<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new BusMutableLiveData<>());
        }
        return (MutableLiveData<T>) bus.get(key);
    }

    public MutableLiveData<Object> with(String key) {
        return with(key, Object.class);
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        private Observer<T> observer;

        public ObserverWrapper(Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (observer != null) {
                if (isCallOnObserve()) {
                    return;
                }
                observer.onChanged(t);
            }
        }

        private boolean isCallOnObserve() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if ("android.arch.lifecycle.LiveData".equals(element.getClassName()) && "observeForever".equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static class BusMutableLiveData<T> extends MutableLiveData<T> {

        private Map<Observer, Observer> observerMap = new HashMap<>();

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, observer);
            try {
                hook(observer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void observeForever(@NonNull Observer<T> observer) {
            if (!observerMap.containsKey(observer)) {
                observerMap.put(observer, new ObserverWrapper(observer));
            }
            super.observeForever(observerMap.get(observer));
        }

        @Override
        public void removeObserver(@NonNull Observer<T> observer) {
            Observer realObserver = null;
            if (observerMap.containsKey(observer)) {
                realObserver = observerMap.remove(observer);
            } else {
                realObserver = observer;
            }
            super.removeObserver(realObserver);
        }

        private void hook(@NonNull Observer<T> observer) throws Exception {
            //get wrapper's version
            Class<LiveData> classLiveData = LiveData.class;
            Field fieldObservers = classLiveData.getDeclaredField("mObservers");
            fieldObservers.setAccessible(true);
            Object objectObservers = fieldObservers.get(this);
            Class<?> classObservers = objectObservers.getClass();
            Method methodGet = classObservers.getDeclaredMethod("get", Object.class);
            methodGet.setAccessible(true);
            Object objectWrapperEntry = methodGet.invoke(objectObservers, observer);
            Object objectWrapper = null;
            if (objectWrapperEntry instanceof Map.Entry) {
                objectWrapper = ((Map.Entry) objectWrapperEntry).getValue();
            }
            if (objectWrapper == null) {
                throw new NullPointerException("Wrapper can not be bull!");
            }
            Class<?> classObserverWrapper = objectWrapper.getClass().getSuperclass();
            Field fieldLastVersion = classObserverWrapper.getDeclaredField("mLastVersion");
            fieldLastVersion.setAccessible(true);
            //get livedata's version
            Field fieldVersion = classLiveData.getDeclaredField("mVersion");
            fieldVersion.setAccessible(true);
            Object objectVersion = fieldVersion.get(this);
            //set wrapper's version
            fieldLastVersion.set(objectWrapper, objectVersion);
        }
    }
}
```

## 使用方法

* 注册订阅

```java
LiveDataBus.get()
        .with("tag", String.class)
        .observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

```

* 发送消息

```java
LiveDataBus.get().with("tag").setValue(str);
```

# 参考文章

1. [LiveDataBus使用](https://blog.csdn.net/LZK_Dreamer/article/details/107974309)



