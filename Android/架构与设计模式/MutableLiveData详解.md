# MutableLiveData 详解

## 一、LiveData 是干什么的？

1. 首先 LiveData 其实与数据实体类 ( POJO 类 ) 是一样的东西，它负责暂存数据。

2. 其次 LiveData 其实也是一个观察者模式的数据实体类，它可以跟它注册的观察者回调数据是否已经更新。

3. LiveData 还能知晓它绑定的 Activity 或者 Fragment 的生命周期，它只会给前台活动的 activity 回调。这样可以放心的在它的回调方法里直接将数据添加到 View，而不用担心会不会报错。

## 二、LiveData 与 MutableLiveData 区别

LiveData 与 MutableLiveData 的其实在概念上是一模一样的。

唯一几个的区别如下：

1. MutableLiveData 的父类是 LiveData

2. LiveData 在实体类里可以通知指定某个字段的数据更新。

3. MutableLiveData 则是完全是整个实体类或者数据类型变化后才通知，不会细节到某个字段。

## 三、LiveData 简单使用 Demo

### 创建 LiveData

```java
public class DemoData extends LiveData<DemoData> {
    private int tag1;
    private int tag2;
    
    public int getTag1() {
        return tag1;
 
    }
    public void setTag1(int tag1) {
        this.tag1 = tag1;
        postValue(this);
    }
 
    public int getTag2() {
        return tag2;
    }
 
    public void setTag2(int tag2) {
        this.tag2 = tag2;
        postValue(this);
    }
}
```

很简单，只要继承 LiveData 并且在泛型里写下你的实体类，唯一需要注意的，postValue(this); 这个方法是用于回调数据更新的方法.。可以在需要被观察的数据里添加。

### 创建 ViewModel

需要在 ViewModel 实例化 DemoData 这个类。 ViewModel 这个是用于管理多个 Activity 或者 Fragment 数据的类。ViewModel 是 MVVM 的概念。

```java
public class DemoViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private DemoData mDemoData = new DemoData();
 
    public DemoData getDemoData() {
        return mDemoData;
    }
}
```

### 在 Activity 或者 Fragment 绑定

```java
public class Demo2Activity extends AppCompatActivity {
    private static final String TAG = "Demo2Activity";
    private Button mBtnAddData;
    private DemoViewModel mDemoViewModel;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo2);
        mBtnAddData = findViewById(R.id.btn_add_data);
        mDemoViewModel = ViewModelProviders.of(this).get(DemoViewModel.class);//获取ViewModel,让ViewModel与此activity绑定
        mDemoViewModel.getDemoData().observe(this, new Observer<DemoData>() { //注册观察者,观察数据的变化
            @Override
            public void onChanged(DemoData demoData) {
                Log.e(TAG, "onChanged: 数据有更新");
            }
        });
        
        mBtnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: 已经点击");
                mDemoViewModel.getDemoData().setTag1(123); //这里手动用按键点击更新数据
 
            }
        });
    }
}
```

## 四、MutableLiveData 简单使用 Demo

### 创建 MutableLiveData

```java
public class DemoViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private MutableLiveData<String> myString = new MutableLiveData<>();
 
    public MutableLiveData<String> getMyString(){
        return myString;
    }
 
    public void setMyString(String string) {
        this.myString.setValue(string);
    }
}
```

因为 MutableLiveData 只是作用于变量所以直接就可以在 ViewModel 里实例化它，并且在泛型里标注变量的类型。

```java
public class MutableLiveData<T> extends LiveData<T> {
    @Override
    public void postValue(T value) {
        super.postValue(value);
    }
 
    @Override
    public void setValue(T value) {
        super.setValue(value);
    }
}
```

### 在 Activity 或者 Fragment 绑定

```java
public class Demo1Activity extends AppCompatActivity {
    private static final String TAG = "Demo1Activity";
    private DemoViewModel mDemoViewModel;
    private Button mBtn1;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mBtn1 = findViewById(R.id.btn_1);
        mDemoViewModel = ViewModelProviders.of(this).get(DemoViewModel.class);//获取ViewModel,让ViewModel与此activity绑定
        mDemoViewModel.getMyString().observe(this, new Observer<String>() { //注册观察者
            @Override
            public void onChanged(String s) {
                Log.e(TAG, "onChanged: 值有变化="+s);
            }
        });
 
        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDemoViewModel.setMyString("测试"); //用手动按键点击改变值
 
 
            }
        });
    }
}
```

## 五、API 全解

### postValue()

**postValue的特性如下：**

1. 此方法可以在其他线程中调用。

2. 如果在主线程执行发布的任务之前多次调用此方法，则仅将分配最后一个值。

3. 如果同时调用 .postValue(“a”) 和 .setValue(“b”)，一定是值 b 被值 a 覆盖。

### setValue()

**setValue()的特性如下：**

1. 此方法**只能在主线程**里调用。

### getValue()

　　返回当前值。 注意，在后台线程上调用此方法并不能保证将接收到最新的值。

### removeObserver(@NonNull final Observer<? super T> observer) 

移除指定的观察者。

```java
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mText.setText("内容改变=" + s);
            }
        };
        mMainViewModel.getContent().observe(this, observer);//绑定
        mMainViewModel.getContent().removeObserver(observer);//解除
```

### removeObservers(@NonNull final LifecycleOwner owner)

移除当前 Activity 或者 Fragment 的全部观察者。

```java
mMainViewModel.getContent().removeObservers(this);
```

###  hasActiveObservers()

如果此 LiveData 具有活动（Activity 或者 Fragment 在前台，当前屏幕显示）的观察者，则返回 true。其实如果这个数据的观察者在最前台就返回 true，否则 false。

### hasObservers()

如果此 LiveData 具有观察者，则返回 true。

### observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer)

设置此 LiveData 数据当前 activity 或者 Fragment 的观察者，会给此 activity 或者 Fragment 在前台时回调数据。

### observeForever(@NonNull Observer<? super T> observer)

1. 设置永远观察者，永远不会被自动删除。需要手动调用 removeObserver（Observer）以停止观察此 LiveData。

2. 设置后此 LiveData，一直处于活动状态，不管是否在前台哪里都会获得回调。

# 参考文章

1. [MutableLiveData详解](https://blog.csdn.net/li6472/article/details/120288422)



