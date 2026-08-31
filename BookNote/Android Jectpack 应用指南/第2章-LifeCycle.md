# 第 2 章 LifeCycle

### 2.1. LifeCycle 的诞生

LifeCycle 可以帮助开发者创建可感知生命周期的组件。这样，组件便能够在其内部管理自己的生命周期，从而降低模块间的耦合度，并降低内存泄漏发生的可能性。LifeCycle 不只对 Activity/Fragment 有用，在 Service 和 Application 中也能大显身手。

### 2.2. 使用 LifeCycle 解耦页面与组件

#### 2.2.1. 案例分析

假设有这样一个常见的需求：在用户打开某个页面时，获取用户当前的地理位置。面对该需求，通常会这样写代码。

```kotlin
public class MainActivity extends AppCompatActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState){
    // 初始化位置管理器
    iniLocationManager();
  }
  
  @Override
  protected void onResume(){
    // 开始获取用户的地理位置
    setGetLocation();
  }
  
  @Override
  protected void onPause(){
    // 停止获取用户的地理位置
    stopGetLocation();
  }
}
```

从以上代码可以看出，获取地理位置这个需求的实现，与页面的生命周期息息相关。如果希望将获取地理位置这一功能独立成一个组件，那么生命周期是必须要考虑在内的。不得不在页面生命周期的各个回调方法中，对组件进行通知，因为组件不能主动感知生命周期的变化。

#### 2.2.2. LifeCycle 的原理

Jetpack 提供了两个类：LifecycleOwner（被观察者）和 LifecycleObserver（观察者）。即通过观察着模式，实现对页面生命周期的监听。

通过查看 SupportActivity 的源码，可以看到，在新版本的 SDK 包中，Activity 已经默认实现了 LifecycleOwner 接口。LifecycleOwner 接口中只有一个 getLifecycle(LifecycleObserver observer) 方法，LifecycleOwner 正是通过该方法实现观察者模式的。源码示例如下：

```kotlin
public class SupportActivity extends Activity implements LifecycleOwner, Component {
	...
  private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
  
  public SupportActivity(){
    
  }
  
  public Lifecycle getLifecycle() {
    return this.mLifecycleRegistry;
  }
  
  ...
}
```

从以上源码可知，SupportActivity 已经实现了被观察者应该实现的那一部分代码。因此，不需要再去实现这部分代码。当希望监听 Activity 的生命周期时，只需要实现观察者那一部分的代码，即让自定义组件实现 LifecycleObserver 接口即可。该接口没有接口方法，无须任何具体实现。

#### 2.2.3. 解决方案

现在利用 LifeCycle 改写该需求。目的是将该功能从 Activity 中独立出去，在减少耦合度的同时，又不影响对生命周期的监听。

1. 编写一个名为 MyLocationListener 的类。该类就是自定义组件，需要让该组件实现 LifecycleObserver 接口。与获取地理位置相关的代码在该类中完成。

   对于组件中那些需要在页面生命周期发生变化时得到通知的方法，需要在这些方法中使用 @OnLifecycleEvent(Lifecycle.Event.ON_XXX) 标签进行标识。这样，当页面生命周期发生变化时，这些被标识过的方法便会被自动调用。如下所示：

   ```kotlin
   public class MyLocationListener implements LifecycleObserver{
   	public MyLocationListener(Activity context, OnLocationChangedListener onLocationChangedListener){
        // 初始化操作
       iniLocationManager();
     }
     
     /**
     * 当 Activity 执行 onResume() 方法时，该方法会被自动调用
     */
     @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
     private void startGetLocation(){
       Log.d(TAG,"startGetLocation()");
     }
     
     /**
     * 当 Activity 执行 onPause() 方法时，该方法会被自动调用
     */
     @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
     private void stopGetLocation(){
       Log.d(TAG,"stopGetLocation()");
     }
     
     /**
     * 当地理位置发生变化时，通过该接口通知调用者
     */
     public interface OnLocationChangedListener{
       void onChanged(double latitude, double longitude);
     }
     // 其他一些业务代码
   }
   ```

2. 在 MainActivity 中，只需要引用 MyLocationListener 即可，不用再关心 Activity 生命周期变化对该组件所带来的影响。生命周期的管理完全交给 MyLocationListener 内部自行处理。在 Activity 中要做的只是通过 getLifecycle().addObserver() 方法，将观察者与被观察者绑定起来，代码如下所示：

   ```kotlin
   public class MainActivity extends AppCompatActivity
   {
   	private MyLocationListener myLocationListener;
   	
   	@Override
     protected void onCreate(Bundle savedinstanceState)
     {
       myLocationListener = new MyLocationListener(this,
           new MyLocationListener.OnLocationChangeListener(){
             @Override
             public void onChanged(double latitude, double longitude){
               // 展示收到的位置信息
             }
           });
       
       //将观察者与被观察者绑定
       getLifecycle().addObserver(myLocationListener);
     }
   	
   }
   ```

   LifeCycle 完美解决了组件对页面生命周期的依赖问题，使组件能够自己管理其生命周期，而无须在页面中对其进行管理。这无疑大大降低了代码的耦合度，提供了组件的复用程度，也杜绝了由于对页面生命周期管理的疏忽而引发的内存泄漏问题，这在项目工程量大的情况下是非常有帮助的。

   除 Activity 之外，在新版本的 SDK 中，Fragment 同样也默认实现了 LifecycleOwner 接口。因此，以上案例同样适用于 Fragment。Fragment 的源码如下所示。

   ```kotlin
   public class Fragment implements ComponentCallbacks,
   																	OnCreateContextMenuListener,
   																	LifecycleOwner,
   																	ViewModelStoreOwner
   {
     ...
     LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
     
     public Lifecycle getLifecycle(){
       return this.mLifecycleRegistry;
     }
     
     ...
   }
   ```

### 2.3. 使用 LifecycleService 解耦 Service 与组件

#### 2.3.1. LifeCycleService 基本介绍

拥有生命周期概念的组件除了 Activity 和 Fragment，还有一个非常重要的组件是 Service。为了便于对 Service 生命周期的监听，达到解耦 Service 与组件的目的，Android 提供了一个名为 LifecycleService 的类。该类继承自 Service，并实现了 LifecycleOwner 接口。与 ActivityFragment 类似，它也提供了一个名为 getLifecycle() 的方法以供使用。LifecycleService 的源码如下所示：

```kotlin
public class LifecycleService extends Service implements LifecycleOwner{
  ...
  private final ServiceLifecycleDispatcher mDispatcher = new ServiceLifecycleDispatcher(this);
  
  @Override
  public Lifecycle getLifecycle() {
    return mDispatcher.getLifecycle();
  }
  ...
}
```

#### 2.3.2. LifecycleService 的具体使用方法

1. 首先，需要在 app 的 build.gradle 文件中添加相关依赖。

   ```groovy
   dependencies {
   	implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
   }
   ```

2. 添加依赖后，便可以使用 LifecycleService 类了。创建一个名为 MyService 的类，并让它继承自 LifecycleService。由于 LifecycleService 是 Service 的直接子类，所以使用与普通 Service 没有差别。

   ```kotlin
   public class MyService extends LifecycleService{
   	private MyServiceObserver myServiceObserver;
     
     public MyService()
     {
       myServiceObserver = new MyServiceObserver();
       // 将观察者与被观察者绑定
       getLifecycle().addObserver(myServiceObserver);
     }
   }
   ```

3. 接下来是 MyServiceObserver 类，该类需要实现 LifecycleObserver 接口。与此同时，使用 @OnLifecycleEvent 标签对那些希望能够在 Service 生命周期发生变化时得到同步调用的方法进行标识。

   ```kotlin
   public class MyServiceObserver implements LifecycleObserver{
     private String TAG = this.getClass().getName();
     
     // 当 Service 的 onCreate() 方法被调用时，该方法会被调用
     @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
     private void startGetLocation(){
       Log.d(TAG,"startGetLocation()")
     }
     
     // 当 Service 的 onDestory() 方法被调用时，该方法会被调用
     @OnLifecycleEvent(Lifecycle.Event.ON_DESTORY)
     private void stopGetLocation(){
       Log.d(TAG,"stopGetLocation()")
     }
   }
   ```

4. 最后，在页面中利用两个 Button，控制 Service 的启动和停止，测试代码。

   ```kotlin
   findViewById(R.id.btnStartService).setOnClickListener(
   	new View.OnClickListener(){
       @Override
       public void onClick(View v){
         // 启动服务
         Intent intent = new Intent(MainActivity.this,MyService.class);
         startService(intent);
       }
     }
   );
   
   findViewById(R.id.btnStopService).setOnClickListener(
   	new View.OnClickListener(){
       @Override
       public void onClick(View v){
         // 停止服务
         Intent intent = new Intent(MainActivity.this,MyService.class);
         stopService(intent);
       }
     }
   );
   ```

5. 通过 LogCat 中的日志可以看到，随着 Service 生命周期的变化，MyServiceObserver 中带有 @OnLifecycleEvent 标签的方法被自动调用了。这样，便实现了组件对 Service 生命周期的监听。

   ```
   com.xxx.xxx.xxx.MyServiceObserver: startGetLocation()
   com.xxx.xxx.xxx.MyServiceObserver: stopGetLocation()
   ```

通过以上实例可以看出，当 Service 的生命周期发生变化时，不再需要主动对组件进行通知，组件能在其内部自行管理好生命周期所带来的变化。LifecycleService 很好地实现了组件与 Service 之间的解耦。

### 2.4. 使用 ProcessLifecycleOwner 监听应用程序的生命周期

#### 2.4.1. ProcessLifecycleOwner 存在的意义

具有生命周期的系统组件除 Activity、Fragment、Service 外，还有 Application。很多时候，会遇到这样的需求：想知道应用程序当前处在前台还是后台，或者当应用程序从后台回到前台时，能够得到通知。有不少方案能够实现该需求，但都不够好。在此之前，Google 并没有为该需求提供官方解决方案，直到 LifeCycle 的出现。LifeCycle 提供了一个名为 ProcessLifecycleOwner 的类，以方便知道整个应用程序的生命周期情况。

#### 2.4.2. ProcessLifecycleOwner 的具体使用方法

1. 首先，需要在 app 的 build.gradle 文件中添加相关依赖。

   ```groovy
   dependencies{
   	implementation "androidx.lifecycle:lifecycle-extensions: 2.2.0"
   }
   ```

2. ProcessLifecycleOwner 的使用方法与 Activity、Fragment 和 Service 是类似的，其本质也是观察者模式。由于要观察的是整个应用程序，因此，需要在 Ap plication 中进行相关代码的编写。

   ```java
   public class MyApplication extends Application{
   	@Override
   	public void onCreate(){
   		super.onCreate();
   		ProcessLifecycleOwner.get().getLifecycle()
   				.addObserver(new ApplicationObserver());
   	}
   }
   ```

3. 定义一个名为 ApplicationObserver 的类，让该类实现 LifecycleObserer 接口，以负责对应用程序生命周期的监听。

   ```java
   public class ApplicationObserver implements LifecycleObserer
   {
   	private String TAG = this.getClass().getName();
   	
   	/**
   	* 在应用程序的整个生命周期中只会被调用一次
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
     public void onCreate(){
       Log.d(TAG, "Lifecycle.Event.ON_CREATE");
     }
     
   	/**
   	* 当应用程序在前台出现时被调用
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_START)
     public void onStart(){
       Log.d(TAG, "Lifecycle.Event.ON_START");
     }
     
     /**
   	* 当应用程序在前台出现时被调用
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
     public void onResume(){
       Log.d(TAG, "Lifecycle.Event.ON_RESUME");
     }
     
     /**
   	* 当应用程序退出到后台时被调用
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
     public void onPause(){
       Log.d(TAG, "Lifecycle.Event.ON_PAUSE");
     }
     
     /**
   	* 当应用程序退出到后台时被调用
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
     public void onStop(){
       Log.d(TAG, "Lifecycle.Event.ON_STOP");
     }
     
     /**
   	* 永远不会被调用，系统不会分发调用 ON_DESTORY 事件
   	*/
     @OnLifecycleEvent(Lifecycle.Event.ON_DESTORY)
     public void oDestory(){
       Log.d(TAG, "Lifecycle.Event.ON_DESTORY");
     }
   }
   ```

通过以上实例可以看出，有了 ProcessLifecycleOwner，可以轻而易举地获知应用程序何时退到后台，何时进入前台，进而执行一些业务操作。它使用起来非常简单，并且不会给项目增加任何的耦合度，但有一下几点需要注意。

* ProcessLifecycleOwner 是正对整个应用程序的监听，与 Activity 数量无关，有一个 Activity 和多个 Activity，对 ProcessLifecycleOwner 来说是没有区别的。
* Lifecycle.Event.ON_CREATE 只会被调用一次，而 Lifecycle.Event.ON_DESTORY 永远不会被调用。
* 当应用程序从后台回到前台，或者应用程序被首次打开时，会一次调用 Lifecycle.Event.ON_START 和 Lifecycle.Event.ON_RESUME。
* 当应用程序从前台退到后台（用户按下 Home 键或任务菜单键），会依次调用 Lifecycle.Event.ON_PAUSE 和 Lifecycle.Event.ON_STOP。需要注意的是，这两个方法的调用会有一定的延后。这是因为系统需要为 “屏幕旋转，由于配置发生变化而导致 Activity 重新创建” 的情况预留一些时间。也就是说，系统需要保证当设备出现这种情况时，这两个事件不会被调用。因为当旋转屏幕时，应用程序并没有退出后台，它只是进入了横/竖屏模式而已。

### 2.5. 总结

所有具有生命周期的组件都能够使用 LifeCycle。这包括 Activity、Fragment、Service 和 Application。LifeCycle 组件存在的主要意义是帮助解耦，让自定义组件也能够感受到生命周期的变化。在没有 LifeCycle 之前，每次当系统组件的生命周期发生变化时，都需要留意这会对自定义组件造成哪些影响。有了 LifeCycle 之后，在自定义组件内部便可以管理好其生命周期，不再需要担心组件的内存泄漏等问题了，组件使用起来也更加方便和安全。
