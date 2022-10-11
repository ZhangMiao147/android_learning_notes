# LiveData 问题

LiveData 是能感知生命周期的、可观察的、粘性的、数据持有者。LiveData 用于以 “数据驱动” 方式更新界面。

换一种描述方式：LiveData 缓存了最新的数据并将其传递给正活跃的组件。

## 1. LiveData 如何感知生命周期的变化？

先总结，再分析：

Jetpack 引入了 Lifecycle，让任何组件都能方便地感知界面生命周期的变化。只需实现 LifecycleEventObserver 接口并注册给生命周期对象即可。

LiveData 的数据观察者在内部被包装成另一个对象（实现了 LifecycleEventobserver 接口），它同时具备了数据观察能力和生命周期观察能力。

常规的观察者模式中，只要被观察者发生变化，就会无条件地通知所有观察者。比如 java.util.Observable：

```java
public class Observable {
    private boolean changed = false;
    private Vector<Observer> obs;
    public void notifyObservers(Object arg) {
        Object[] arrLocal;
        synchronized (this) {
            if (!hasChanged())
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }
        // 无条件地遍历所有观察者并通知
        for (int i = arrLocal.length-1; i>=0; i--)
            ((Observer)arrLocal[i]).update(this, arg);
    }
}
// 观察者
public interface Observer {
    void update(Observable o, Object arg);
}
```

LiveData 在常规的观察者模式上附加了条件，若生命周期未达标，即使数据发生变化也不通知观察者。这是如何实现的。

### 生命周期

生命周期是一个对象从构建到消亡过程中的各个状态的统称。

比如 Activity 的生命周期用如下函数依次表达：

```
onCreate()
onStart()
onResume()
onPause()
onStop()
onDestroy()
```

要观察生命周期就不得不继承 Activity 重写这些方法，想把生命周期的变化分发给其他组件就很麻烦。

于是 Jetpack 引入了 Lifecycle，以让任何组件都可方便地感知生命周期的变化：

```java
public abstract class Lifecycle {AtomicReference<>();
    // 添加生命周期观察者
    public abstract void addObserver(LifecycleObserver observer);
    // 移除生命周期观察者
    public abstract void removeObserver(LifecycleObserver observer);
    // 获取当前生命周期状态
    public abstract State getCurrentState();
    // 生命周期事件
    public enum Event {
        ON_CREATE,
        ON_START,
        ON_RESUME,
        ON_PAUSE,
        ON_STOP,
        ON_DESTROY,
        ON_ANY;
    }
    // 生命周期状态
    public enum State {
        DESTROYED,
        INITIALIZED,
        CREATED,
        STARTED,
        RESUMED;
    }
    // 判断至少到达了某生命周期状态
    public boolean isAtLeast(State state) {
        return compareTo(state) >= 0;
    }
}
```

Lifecycle 即是生命周期对应的类，提供了添加/移除生命周期观察者的方法，在其内部还能定义了全部生命周期的状态及对应事件。

生命周期状态是有先后次序的，分别对应着由小到大的 int 值。

### 生命周期拥有者

描述生命周期的对象已经有了，如何获取这个对象需要个统一的接口（不然直接在 Activity 或者 Fragment 中新增一个方法吗？），这个接口叫 LifecycleOwner：

```java
public interface LifecycleOwner {
    Lifecycle getLifecycle();
}
```

Activity 和 Fragment 都实现了这个接口。只要拿到 LifecycleOwner，就能拿到 Lifecycle，然后就能注册生命周期观察者。

### 生命周期 & 数据观察者

生命周期观察者是一个接口：

```java
// 生命周期观察者（空接口，用于表征一个类型）
public interface LifecycleObserver {}
// 生命周期事件观察者
public interface LifecycleEventObserver extends LifecycleObserver {
    void onStateChanged(LifecycleOwner source, Lifecycle.Event event);
}
```

要观察生命周期只要实现 LifecycleEventObserver 接口，并注册给 Lifecycler 即可。

除了生命周期观察者外，LiveData 场景中还有一个数据观察者：

```java
// 数据观察者
public interface Observer<T> {
    // 数据发生变化时回调
    void onChanged(T t);
}
```

数据观察者会和生命周期拥有者进行绑定：

```java
public abstract class LiveData<T> {
    // 数据观察者容器
    private SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers =
            new SafeIterableMap<>();

    public void observe(
        LifecycleOwner owner, // 被绑定的生命周期拥有者
        Observer<? super T> observer // 数据观察者
    ) {
        ...
        // 将数据观察者包装成 LifecycleBoundObserver
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        // 存储观察者到 map 结构
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        ...
        // 注册生命周期观察者。
        owner.getLifecycle().addObserver(wrapper);
    }
}
```

在观察 LiveData 时，需传入两个参数，生命周期拥有者和数据观察者。这两个对象经过 LifecycleBoundObserver 的包装被绑定在了一起：

```java
class LifecycleBoundObserver extends ObserverWrapper implements LifecycleEventObserver {
    // 持有生命周期拥有者
    final LifecycleOwner mOwner;

    LifecycleBoundObserver(LifecycleOwner owner, Observer<? super T> observer) {
        super(observer);
        mOwner = owner;
    }
    // 生命周期变化回调
    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) { 
        ...
        activeStateChanged(shouldBeActive())
        ...
    }
}

// 观察者包装类型
private abstract class ObserverWrapper {
    // 持有原始数据观察者
    final Observer<? super T> mObserver;
    // 注入数据观察者
    ObserverWrapper(Observer<? super T> observer) {mObserver = observer;}
    // 尝试将最新值分发给当前数据观察者
    void activeStateChanged(boolean newActive) {...}
    ...
}
```

LifecycleBoundObserver 实现了 LifecycleEventObserver 接口，并且它被注册给了绑定的生命周期对象，遂具备了生命周期感知能力。同时它还持有了数据观察者，所以它还具备了数据观察能力。

## 2. LiveData 是如何避免内存泄漏的？

总结：

LiveData 的数据观察者通常是匿名内部类，它持有界面的引用，可能造成内存泄漏。

LiveData 内部会将数据观察者进行封装，使其具备生命周期感知能力。当生命周期状态为 DESTROYED 时，自动移除观察者。

内存泄漏是因为长生命周期的对象持有了短生命周期对象，阻碍了其被回收。

观察 LiveData 数据的代码通常这样写：

```kotlin
class LiveDataActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProviders.of(this@LiveDataActivity).get(MyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.livedata.observe(this@LiveDataActivity) {
            // 观察 LiveData 数据更新（匿名内部类）
        }
    }
}
```

Observer 作为界面的匿名内部类，它会持有界面的引用，同时 Observer 被 LiveData 持有，LiveData 被 ViewModel 持有，而 ViewModel 的生命周期比 Activity 长。

最终的持有链如下：NonConfigurationinstances 持有 ViewModelStore 持有 ViewModel 持有 LiveData 持有 Observer 持有 Activity。

所以得在界面生命周期结束的时候移除 Observer，这件事情，LiveData 帮我们做了。

在 LiveData 内部 Observer 会被包装成 LifecycleBoundObserver。

```java
class LifecycleBoundObserver extends ObserverWrapper 
    implements LifecycleEventObserver {
    final LifecycleOwner mOwner;

    LifecycleBoundObserver(LifecycleOwner owner, Observer<? super T> observer) {
        super(observer);
        mOwner = owner;
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        // 获取当前生命周期
        Lifecycle.State currentState = mOwner.getLifecycle().getCurrentState();
        // 若生命周期为 DESTROYED 则移除数据观察者并返回
        if (currentState == DESTROYED) {
            removeObserver(mObserver);
            return
        }
        ...
    }
    ...
}
```



## 3. LiveData 是粘性的吗？若是，它是怎么做到的？

总结：

* LiveData 的值被存储在内部的字段中，直到有更新的值覆盖，所以值是持久的。
* 两种场景下 LiveData 会将存储的值分发给观察者。一是值被更新，此时会遍历所有观察者并分发之。而是新增观察者或观察者生命周期发生变化（至少为 STARTED），此时只会给单个观察者分发值。
* LiveData 的观察者会维护一个“值的版本号”，用于判断上次分发的值是否是最新值。该值的初始值是 -1，每次更新 LiveData 值都会让版本号自增。
* LiveData 并不会无条件地将值分发给观察者，在分发之前会经历三道坎：1. 数据观察者是否活跃。2.数据观察者绑定的生命周期组件是否活跃。3.数据观察者的版本号是否是最新的。
* “新观察者”被“老值”通知的现象叫“粘性”。因为新观察者的版本号总是小于最新版本，且添加观察者时会触发一次老值的分发。

如果把 sticky 翻译成 “持久的”，会更好理解一些。数据是持久的，意味着它不会转瞬即逝的，不会因为被消费了就不见了，它会一直在那。而且当新的观察者被注册时，持久的数据会将最新的值分发给它。

“持久的数据”是怎么做到的？

显然是被存起来了。以更新 LiveData 数据的方法为切入点找找线索：

```java
public abstract class LiveData<T> {
    // 存储数据的字段
    private volatile Object mData;
    // 值版本号
    private int mVersion;
    // 更新值
    protected void setValue(T value) {
        assertMainThread("setValue");
        // 版本号自增
        mVersion++;
        // 存储值
        mData = value;
        // 分发值
        dispatchingValue(null);
    }
}
```

setValue() 是更新 Livedata 值时必然会调用的一个方法，即使是通过 postValue() 更新值，最终也会走这个方法。

LiveData 持有一个版本号字段，用于标识“值的版本”，就像软件版本号一样，这个数字用于判断“当前值是否是最新的”，若版本号小于最新版本号，则表示当前值需要更新。

LiveData 用一个 Object 字段存储了 “值”。所以这个值会一直存在，直到被更新的值覆盖。

LiveData 分发值即是通知数据观察者：

```java
public abstract class LiveData<T> {
    // 用键值对方式持有一组数据观察者
    private SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers =
            new SafeIterableMap<>();
    void dispatchingValue(ObserverWrapper initiator) {
            ...
            // 指定分发给单个数据观察者
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } 
            // 遍历所有数据观察者分发值
            else {
                for (Iterator<Map.Entry<Observer<? super T>, ObserverWrapper>> iterator =
                        mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                    considerNotify(iterator.next().getValue());
                }
            }
            ...
    }

    // 真正地分发值
    private void considerNotify(ObserverWrapper observer) {
        // 1. 若观察者不活跃则不分发给它
        if (!observer.mActive) {
            return;
        }
        // 2. 根据观察者绑定的生命周期再次判断它是否活跃，若不活跃则不分发给它
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false);
            return;
        }
        // 3. 若值已经是最新版本，则不分发
        if (observer.mLastVersion >= mVersion) {
            return;
        }
        // 更新观察者的最新版本号
        observer.mLastVersion = mVersion;
        // 真正地通知观察者
        observer.mObserver.onChanged((T) mData);
    }

}
```

分发值有两种情况："分发给单个观察者"和“分发给所有观察者”。当 LiveData 值更新时，需分发给所有观察者。

所有的观察者被存在一个 Map 结构中，分发的方式是通过遍历 Map 并逐个调用 consideNotify()。在这个方法中需要跨过三道坎，才能真正地将值分发给数据观察者，分别是：

1. 数据观察者是否活跃。
2. 数据观察者绑定的生命周期组件是否活跃。
3. 数据观察者的版本号是否是最新的。

跨过三道坎后，会将最新的版本号存储在观察者的 mLastVersion 字段中，即版本号除了保存在 LiveData.mVersion，还会在每个观察者中保存一个副本 mLastVersion，最后才将之前暂存的 mData 的值分发给数据观察者。

每个数据观察者都和一个组件的生命周期对象绑定，当组件生命周期发生变化时，会尝试将这个最新值分发给给数据观察者。

每一个数据观察者都会被包装，包装类型为 observerWrapper：

```java
// 原始数据观察者
public interface Observer<T> {
    void onChanged(T t);
}

// 观察者包装类型
private abstract class ObserverWrapper {
    // 持有原始数据观察者
    final Observer<? super T> mObserver;
    // 当前观察者是否活跃
    boolean mActive;
    // 当前观察者最新值版本号，初始值为 -1
    int mLastVersion = START_VERSION;
    // 注入原始观察者
    ObserverWrapper(Observer<? super T> observer) {mObserver = observer;}
    // 当数据观察者绑定的组件生命周期变化时，尝试将最新值分发给当前观察者
    void activeStateChanged(boolean newActive) {
        // 若观察者活跃状态未变，则不分发值
        if (newActive == mActive) {
            return;
        }
        // 更新活跃状态
        mActive = newActive;
        // 若活跃，则将最新值分发给当前观察者
        if (mActive) {
            dispatchingValue(this);
        }
    }
    // 是否活跃，供子类重写
    abstract boolean shouldBeActive();
}
```

观察者的包装类型通过组合的方式持有了一个原始观察者，并在此基础上为其扩展了活跃状态和版本号的概念。

观察者包装类型是抽象的，是否活跃由子类定义：

```java
class LifecycleBoundObserver extends ObserverWrapper implements LifecycleEventObserver {
    final LifecycleOwner mOwner;

    LifecycleBoundObserver(LifecycleOwner owner, Observer<? super T> observer) {
        super(observer);
        mOwner = owner;
    }

    // 当与观察者绑定的生命周期组件至少为STARTED时，表示观察者活跃
    @Override
    boolean shouldBeActive() {
        return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
    }

    @Override
    public void onStateChanged( LifecycleOwner source, Lifecycle.Event event) {
        Lifecycle.State currentState = mOwner.getLifecycle().getCurrentState();
        // 当生命周期状态发生变化，则尝试将最新值分发给数据观察者
        while (prevState != currentState) {
            prevState = currentState;
            // 调用父类方法，进行分发
            activeStateChanged(shouldBeActive());
            currentState = mOwner.getLifecycle().getCurrentState();
        }
    }
}
```

总结一下，LiveData 有两次机会通知观察者，与之对应的有两种分发值的方式：

1. 当值更新时，遍历所有观察者将最新值分发给它们。
2. 当与观察者绑定组件的生命周期发生变化时，将最新值分发给指定观察者。

假设这样一种场景：LiveData 的值被更新了一次，随后它被添加了一个新的数据观察者，与之绑定组件的生命周期也正好发生了变化（变化到 RESUME），即数据更新在添加观察者之前，此时更新值会被分发到新的观察者吗？

会！首先，更新值会被存储在 mData 字段中。

其次，在添加观察者时会触发一次生命周期变化：

```java
// androidx.lifecycle.LifecycleRegistry
public void addObserver(@NonNull LifecycleObserver observer) {
    State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
    ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
    ...
    // 将生命周期事件分发给新进的观察者
    statefulObserver.dispatchEvent(lifecycleOwner, upEvent(statefulObserver.mState));
    ...
}

// LifecycleBoundObserver 又被包了一层
static class ObserverWithState {
    State mState;
    GenericLifecycleObserver mLifecycleObserver;

    ObserverWithState(LifecycleObserver observer, State initialState) {
        mLifecycleObserver = Lifecycling.getCallback(observer);
        mState = initialState;
    }

    void dispatchEvent(LifecycleOwner owner, Event event) {
        State newState = getStateAfter(event);
        mState = min(mState, newState);
        // 分发生命周期事件给 LifecycleBoundObserver
        mLifecycleObserver.onStateChanged(owner, event);
        mState = newState;
    }
}
```

最后，这次尝试必然能跨过三道坎，因为新建观察者版本号总是小于 LiveData 的版本号（-1 《 0，LiveData.mVersion 经过一次值更新后自增为 0）。

这种“新观察者”会被“老值”通知的现象称为粘性。

## 4. 粘性的 LiveData 会造成什么问题？怎么解决？

购物车-结算场景：假设有一个购物车界面，点击结算后跳转到结算界面，结算界面可以回退到购物车界面。这两个界面都是 Fragment。

结算界面和购物车界面通过共享 ViewModel 的方式共享商品列表：

```java
class MyViewModel:ViewModel() {
    // 商品列表
    val selectsListLiveData = MutableLiveData<List<String>>()
    // 更新商品列表
    fun setSelectsList(goods:List<String>){
       selectsListLiveData.value = goods
    }
}
```

下面是两 Fragment 界面依托的 Activity：

```java
class StickyLiveDataActivity : AppCompatActivity() {
    // 用 DSL 构建视图
    private val contentView by lazy {
        ConstraintLayout {
            layout_id = "container"
            layout_width = match_parent
            layout_height = match_parent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
        // 加载购物车界面
        supportFragmentManager.beginTransaction()
            .add("container".toLayoutId(), TrolleyFragment())
            .commit()
    }
}
```

购物车页面如下：

```kotlin
class TrolleyFragment : Fragment() {
    // 获取与宿主 Activity 绑定的 ViewModel
    private val myViewModel by lazy { 
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java) 
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConstraintLayout {
            layout_width = match_parent
            layout_height = match_parent
            // 向购物车添加两件商品
            onClick = {
                myViewModel.setSelectsList(listOf("meet","water"))
            }

            TextView {
                layout_id = "balance"
                layout_width = wrap_content
                layout_height = wrap_content
                text = "balance"
                gravity = gravity_center
                // 跳转结算页面
                onClick = {
                    parentFragmentManager.beginTransaction()
                        .replace("container".toLayoutId(), BalanceFragment())
                        .addToBackStack("trolley")
                        .commit()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 观察商品列表变化
        myViewModel.selectsListLiveData.observe(viewLifecycleOwner) { goods ->
            // 若商品列表超过2件商品，则 toast 提示已满
            goods.takeIf { it.size >= 2 }?.let {
                Toast.makeText(context,"购物车已满",Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

在 onViewCreate() 中观察购物车的变化，如果购物车超过 2 件商品，则 toast 提示。

下面是结算界面：

```kotlin
class BalanceFragment:Fragment() {
    private val myViewModel by lazy { 
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java) 
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConstraintLayout {
            layout_width = match_parent
            layout_height = match_parent
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 结算界面获取购物列表的方式也是观察商品 LiveData
        myViewModel.selectsListLiveData.observe(viewLifecycleOwner) {...}
    }
}
```

跑一下 demo，当跳转到结算界面后，点击返回购物车，toast 会再次提示购物车已满。

因为在跳转结算页面之前，购物车列表 Livedata 已经被更新过。当购物车页面重新展示时，onViewCreate() 会再次执行，这样一个新观察者被添加，因为 LiveData 是粘性的，所以上一次购物车列表会分发给新观察者，这样 toast 逻辑再一次被执行。

### 解决方案一：带消费记录的值

```kotlin
// 一次性值
open class OneShotValue<out T>(private val value: T) {
    // 值是否被消费
    private var handled = false
    // 获取值，如果值未被处理则返回，否则返回空
    fun getValue(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            value
        }
    }
    // 获取上次被处理的值
    fun peekValue(): T = value
}
```

在值的外面套一层，新增一个标记位标识是否被处理过。

用这个方法重构下 ViewModel：

```kotlin
class MyViewModel:ViewModel() {
    // 已选物品列表
    val selectsListLiveData = MutableLiveData<OneShotValue<List<String>>>()
    // 更新已选物品
    fun setSelectsList(goods:List<String>){
       selectsListLiveData.value = OneShotValue(goods)
    }
}
```

观察购物车的逻辑也要做修改：

```kotlin
class TrolleyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewModel.selectsListLiveData.observe(viewLifecycleOwner) { goods ->
            goods.getValue()?.takeIf { it.size >= 2 }?.let {
                Toast.makeText(context,"购物车满了",Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

重复弹 toast 的问题是解决了，但引出了一个新的问题：当购物车满弹出 toast 时，购物车列表已经被消费掉了，导致结算界面就无法再消费了。

这时候只能用 peekValue() 来获取已经被消费的值：

```kotlin
class BalanceFragment:Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewModel.selectsListLiveData.observe(viewLifecycleOwner) {
            val list = it.peekValue()// 使用 peekValue() 获取购物车列表
        }
    }
}
```

用 “一次性值” 封装 LiveData 的值，以去除其粘性。使用该方案得甄别出哪些观察者需要粘性值，哪些观察者需要非粘性值。当观察者很多的时候，就很难招架了。若把需要粘性处理和非粘性处理的逻辑写在一个观察者中，就 GG，还得新建观察者将它们分开。

### 解决方案二：带有最新版本号的观察者

通知观察者前需要跨过三道坎，其中有一道坎是版本号的对比。若新建的观察者版本号小于最新版本号，则表示观察者落后了，需要将最新值分发给它。

LiveData 源码中，新建观察者的版本号总是 -1.

```java
// 观察者包装类型
private abstract class ObserverWrapper {
    // 当前观察者最新值版本号，初始值为 -1
    int mLastVersion = START_VERSION;
    ...
}
```

若能够让新建观察者的版本号被最新版本号赋值，那版本号对比的那道坎就过不了，新值就无法分发到新建观察者。

所以得通过反射修改 mLastVersion 字段。

该方案除了侵入性强之外，把 LiveData 粘性彻底破坏了。但有的时候，我们还是想利用粘性的。

### 解决方案三：SingleLiveEvent

这是谷歌给出的一个解决方案，源码可以点击这里。

*https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java*

```java
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    // 标志位，用于表达值是否被消费
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    public void observe(LifecycleOwner owner, final Observer<T> observer) {
        // 中间观察者
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                // 只有当值未被消费过时，才通知下游观察者
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });
    }

    public void setValue(@Nullable T t) {
        // 当值更新时，置标志位为 true
        mPending.set(true);
        super.setValue(t);
    }

    public void call() {
        setValue(null);
    }
}
```

专门设立一个 LiveData，它不具备粘性。它通过新增的 “中间观察者”，拦截上游数据变化，然后再转发给下游。拦截之后通常可以做一点手脚，比如增加一个标记为 mPending 是否消费过的判断，若消费过则不转发给下游。

在数据驱动的 App 界面下，存在两种值：1.非暂态数据；2.暂态数据。

demo 中用于提示 “购物车已满” 的数据就是 “暂态数据”，这种数据是一次性的，转瞬即逝的，可以消费一次就扔掉。

demo 中购物车中的商品列表就是 “非暂态数据”，它的生命周期要比暂态数据长一点，在购物车界面和结算界面存活的期间都应该能被重复消费。

SingleLiveEvent 的设计正是基于对数据的这种分类方法，即暂态数据使用 SingleLiveEvent，非暂态数据使用常规的 LiveData。

这种尘归尘土归土的解决方案是符合现实情况的，将 demo 改造一下：

```kotlin
class MyViewModel : ViewModel() {
    // 非暂态购物车列表 LiveData
    val selectsListLiveData = MutableLiveData<List<String>>()
    // 暂态购物车列表 LiveData
    val singleListLiveData = SingleLiveEvent<List<String>>()
    // 更新购物车列表，同时更新暂态和非暂态
    fun setSelectsList(goods: List<String>) {
        selectsListLiveData.value = goods
        singleListLiveData.value = goods
    }
}
```

在购物车界面做相应的改动：

```kotlin
class TrolleyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 只观察非暂态购物车列表
        myViewModel.singleListLiveData.observe(viewLifecycleOwner) { goods ->
            goods.takeIf { it.size >= 2 }?.let {
                Toast.makeText(context,"full",Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

但该方案有局限性，若为 SingleLiveEvent 添加多个观察者，则当第一个观察者消费了数据后，其他观察者就没机会消费了。因为 mPending 是所有观察者共享的。

解决方案也很简单，为每个中间观察者都持有是否消费过数据的标记为：

```java
open class LiveEvent<T> : MediatorLiveData<T>() {
    // 持有多个中间观察者
    private val observers = ArraySet<ObserverWrapper<in T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observers.find { it.observer === observer }?.let { _ ->
            return
        }
        // 构建中间观察者
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        observers.find { it.observer === observer }?.let { _ ->
            return
        }
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observeForever(wrapper)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        if (observer is ObserverWrapper && observers.remove(observer)) {
            super.removeObserver(observer)
            return
        }
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val wrapper = iterator.next()
            if (wrapper.observer == observer) {
                iterator.remove()
                super.removeObserver(wrapper)
                break
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        // 通知所有中间观察者，有新数据
        observers.forEach { it.newValue() }
        super.setValue(t)
    }

    // 中间观察者
    private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {
        // 标记当前观察者是否消费了数据
        private var pending = false

        override fun onChanged(t: T?) {
            // 保证只向下游观察者分发一次数据
            if (pending) {
                pending = false
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending = true
        }
    }
}
```

### 解决方案四：Kotlin Flow

```kotlin
class MyViewModel : ViewModel() {
    // 商品列表流
    val selectsListFlow = MutableSharedFlow<List<String>>()
    // 更新商品列表
    fun setSelectsList(goods: List<String>) {
        viewModelScope.launch {
            selectsListFlow.emit(goods)
        }
    }
}
```

购物车代码如下：

```kotlin
class TrolleyFragment : Fragment() {
    private val myViewModel by lazy { 
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1.先产生数据
        myViewModel.setSelectsList(listOf("food_meet", "food_water", "book_1"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2.再订阅商品列表流
        lifecycleScope.launch {
            myViewModel.selectsListFlow.collect { goods ->
                goods.takeIf { it.size >= 2 }?.let {
                    Log.v("ttaylor", "购物车满")
                }
            }
        }
    }
}
```

数据生产在订阅之前，订阅后并不会打印 log。

如果这样修改 ShareFlow 的构建参数，则可以让其变得粘性：

```kotlin
class MyViewModel : ViewModel() {
    val selectsListFlow = MutableSharedFlow<List<String>>(replay = 1)
}
```

replay = 1 表示会将最新的那个数据通知给新进的订阅者。

这只是解决了粘性/非粘性之间方便切换的问题，并未解决仍需多个流的问题。

## 5. 什么情况下 LiveData 会丢失数据？

总结：

在高频数据更新的场景下使用 LiveData.postValue() 时，会造成数据丢失。因为 “设值” 和 “分发值” 是分开执行的，之间存在延迟。值先被缓存在变量中，再向主线程抛出一个分发值的任务。若在这延迟之间再一次调用 postValue()，则变量中缓存的值被更新，之前的值在没有被分发之前就被擦除了。

下面是 LiveData.postValue() 的源码：

```kotlin
public abstract class LiveData<T> {
    // 暂存值字段
    volatile Object mPendingData = NOT_SET;
    private final Runnable mPostValueRunnable = new Runnable() {
        @Override
        public void run() {
            Object newValue;
            synchronized (mDataLock) {
                // 同步地获取暂存值
                newValue = mPendingData;
                mPendingData = NOT_SET;
            }
            // 分发值
            setValue((T) newValue);
        }
    };

    protected void postValue(T value) {
        boolean postTask;
        synchronized (mDataLock) {
            postTask = mPendingData == NOT_SET;
            // 暂存值
            mPendingData = value;
        }
        ...
        // 向主线程抛 runnable
        ArchTaskExecutor.getInstance().postToMainThread(mPostValueRunnable);
    }
}
```



## 6. 在 Fragment 中使用 LiveData 需注意些什么？

总结：

在 Fragment 中观察 LiveData 时使用 viewLifecycleOwner 而不是 this。因为 Fragment 和其中的 View 生命周期不完全一致。LiveData 内部判定生命周期为 DESTROYED 时，才会移除数据观察者。存在一种情况，当 Fragment 之间切换时，被替换的 Fragment 不执行 onDestory()，当它再次展示时会再次订阅 LiveData，于是乎就多出一个订阅者。

还是购物-结算的场景：购物车和结算页都是两个 Fragment，将商品列表存在共享 ViewMode 的 LiveData 中，购物车及结算页都观察它，结算页除了用它列出购物清单之外，还可以通过更改商品数量来修改 LiveData。当从结算页返回购物车页面时，购物车界面得刷新商品数量。

上述场景，若购物车页面观察 LiveData 时使用 this 会发生什么？

```kotlin
// 购物车界面
class TrolleyFragment : Fragment() {
    private val myViewModel by lazy { 
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java) 
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConstraintLayout {
            layout_width = match_parent
            layout_height = match_parent
            onClick = {
                parentFragmentManager.beginTransaction()
                    .replace("container".toLayoutId(), BalanceFragment())
                    .addToBackStack("trolley")// 将购物车页面添加到 back stack
                    .commit()
            }
        }
    }

    // 不得不增加这个注释，因为 this 会飘红
    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 将 this 作为生命周期拥有者传给 LiveData
        myViewModel.selectsListLiveData.observe(this, object : Observer<List<String>> {
            override fun onChanged(t: List<String>?) {
                Log.v("ttaylor", "商品数量发生变化")
            }
        })
    }
}
```

这样写 this 会飘红，Android Studio 不推荐使用它作为生命周期拥有者，不得不加 @SuppressLint("FragmentLiveDataObserve")。

结算界面修改商品数量的代码如下：

```kotlin
// 结算界面
class BalanceFragment:Fragment() {
    private val myViewModel by lazy { 
        ViewModelProvider(requireActivity()).get(MyViewModel::class.java) 
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 模拟结算界面修改商品数量
        myViewModel.selectsListLiveData.value = listOf("数量+1")
    }
}
```

当从结算页返回购物车时，“商品数量发生变化”会打印两次，如果再进一次结算页并返回购物车，就会打印三次。

若换成 viewLifecycleOwner 就不会有这个烦恼。因为使用 replace 更换 Fragment 时，Fragment.onDestoryView() 会执行，即 Fragment 对应 View 的生命周期状态会变为 DESTROYED。

LiveData 内部会将生命周期为 DESTORYED 的数据观察者移除。当再次返回购物车时，onViewCreated() 重新执行，LiveData 会添加一个新的观察者。一删一增，整个过程 LiveData 始终只有一个观察者。又因为 LiveData 时粘性的，即使修改商品数量发生在观察之前，最新的商品数量还是会被分发到新观察者。

但当使用 replace 更换 Fragment 并将其压入 back stack 时，Fragment.onDestory() 不会调用（因为被压栈了，并未被销毁）。这导致 Fragment 的生命周期状态不会变为 DESTORYED，所以 LiveData 的观察者不会被自动移除。当重新返回购物车时，又添加了新的观察者。如果不停地在购物车和结算页面间横跳，则观察者数据会不停地增加。

在写 demo 的时候遇到一个坑：

```kotlin
// 购物车界面
class TrolleyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 故意使用 object 语法
        myViewModel.selectsListLiveData.observe(this, object : Observer<List<String>> {
            override fun onChanged(t: List<String>?) {
                Log.v("ttaylor", "商品数量发生变化")
            }
        })
    }
}
```

在构建 Observer 实例的时候，我特意使用了 Kotlin 的 object 语法，其实明明可以使用 lamdba 将其写得更简洁：

```kotlin
class TrolleyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewModel.selectsListLiveData.observe(this) {
            Log.v("ttaylor", "商品数量发生变化")
        }
    }
}
```

如果这样写，那 bug 就无法复现了。。。

因为 java 编译器会擅作主张地将同样的 lambda 优化成静态的，可以提升性能，不用每次都重新构建内部类。但不巧的是 LiveData 在添加观察者时会校验是否已存在，若存在则直接返回：

```java
// `androidx.lifecycle.LiveData
public void observe( LifecycleOwner owner,  Observer<? super T> observer) {
    ...
    LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
    // 调用 map 结构的写操作，若 key 已存在，则返回对应 value
    ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
    ...
    // 已存在则直接返回
    if (existing != null) {
        return;
    }
    owner.getLifecycle().addObserver(wrapper);
}
```

这样的话，Fragment 界面之间反复横跳也不会新增观察者。

## 7. 如何变换 LiveData 数据及注意事项？

总结：

Andoidx.lifecycle.Transformations 类提供了三个变换 LiveData 作为数据的中间消费者，并将变换后的数据传递给最终消费者。需要注意的是，数据变化操作都发生在主线程，主线程有可能被耗时操作阻塞。解决方法是将 LiveData 数据变换操作异步化，比如通过 CoroutineLiveData。

还是购物-结算的场景：购物车和结算页都是两个 Fragment，将商品列表存在 LiveData 中，购物车及结算车都观察它。结算界面对打印商品有一个特殊的 UI 展示。

此时就可以将商品列表 LiveData 进行一次变化（过滤）得到一个新的打折商品列表：

```kotlin
class MyViewModel : ViewModel() {
    // 商品列表
    val selectsListLiveData = MutableLiveData<List<String>>()
    // 打折商品列表
    val foodListLiveData = Transformations.map(selectsListLiveData) { list ->
        list.filter { it.startsWith("discount") }
    }
}
```

每当商品列表发生变化，打折商品列表都会收到通知，并过滤出新的打折商品。打折商品列表是一个新的 LiveData，可以单独被观察。

其中的过滤列表操作发生在主线程，如果业务略复杂，数据变换操作耗时的话，可以阻塞主线程。

如果将 LiveData 变换数据异步化？

LiveData 的 kotlin 扩展包里提供了一个将 LiveData 和协城结合的产物：

```kotlin
class MyViewModel : ViewModel() {
    // 商品列表
    val selectsListLiveData = MutableLiveData<List<String>>()
    // 用异步方式获取打折商品列表
    val asyncLiveData = selectsListLiveData.switchMap { list ->
        // 将源 LiveData 中的值转换成一个 CoroutineLiveData
        liveData(Dispatchers.Default) {
            emit( list.filter { it.startsWith("discount") } )
        }
    }
}
```

其中 switchMap() 是 LiveData 的扩展方法，它是对  Transformations.switchMap() 的封装，用于方便链式调用：

```kotlin
public inline fun <X, Y> LiveData<X>.switchMap(
    crossinline transform: (X) -> LiveData<Y>
): LiveData<Y> = Transformations.switchMap(this) { transform(it) }
```

switchMap() 内部将源 LiveData 的每个值都转换成一个新的 LiveData 并订阅。

liveData 是一个顶层方法，用于构建 CoroutineLiveData：

```kotlin
public fun <T> liveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMs: Long = DEFAULT_TIMEOUT,
    block: suspend LiveDataScope<T>.() -> Unit
): LiveData<T> = CoroutineLiveData(context, timeoutInMs, block)
```

CoroutineLiveData 将更新 LiveData 值的操作封装到一个挂起方法中，可以通过协程上下文指定执行的线程。

使用 CoroutineLivedata 需要添加一下依赖：

```groovy
implementation  "androidx.lifecycle:lifecycle-livedata-ktx:2.3.1"
```

## 参考文章

1. [LiveData 面试 7 连问！](https://mp.weixin.qq.com/s/txOLO-cLrOR9JwqfktAtnA)

