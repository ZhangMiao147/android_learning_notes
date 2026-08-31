# Hilt

一个类中使用的依赖类不是类本省创建的，而是通过构造函数或者属性方法实现的，这种实现方法就称为依赖注入。

如果在方法中创建对象，这种实现方式中，虽然有依赖类，但使用时依赖类是自己创建的，所以这种实现方式并没有使用依赖注入。

使用依赖注入不仅可以提高代码的可扩展性，还可以分离依赖项。

Dragger 最初版本是采用反射的方式实现的，过多使用反射方法会影响程序的运行效率。由于反射方法在编译阶段是不会产生错误的，因此只有在程序运行时才可以验证方法是否正确。Dragger2 是通过注解的方式实现的，如此一来，在编译时就可以发现依赖注入使用的问题。Hilt 组件是机遇 Dragger 开发、专门面向 Android 开发者的依赖注入框架，它知识为依赖注入提供了更简便的实现方法，而不是实现依赖注入的唯一方式。

### 使用

在使用 Hilt 时，开发者必须自定义一个 Application，并为其添加 @HiltAndroidApp 注解。

Hilt 支持的 Android 类及其注解与注意事项

![](/Users/zhangmiao/Desktop/资料/android_learning_notes/Jetpack/hilt/img/注解.png)

Hilt 通过为被依赖类的构造函数添加 @Inject 注解，来告知 Hilt 应如何提供该类的实例。

**依赖第三方组件**

```kotlin
@Module
@InstallIn(ApplicationComponent::class)
class NetWorkUtil {
    @Singleton
    @Provides
    fun getOkHttpClient(): OkHttpClient {
        var okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
        return okHttpClient
    }
}
```

@Module 注解表示这是一个用来提供依赖注入实例的模块。

@InstallIn 注解表示要装载到哪个模块中。

@Provides 注解提供获取方法。

Hilt组件类型与注入场景以及生命周期的对比关系（InstallIn 的选择->组件名称）：

![](/Users/zhangmiao/Desktop/资料/android_learning_notes/Jetpack/hilt/img/注入场景.png)

@Singleton 注解是 Application 组件类的作用域，Hilt 只为绑定作用域中的组件实例创建一次作用域绑定，并对该绑定的所有请求共享同一实例。（还有其他的组件作用域）

各组件对应作用域的关系：

![](/Users/zhangmiao/Desktop/资料/android_learning_notes/Jetpack/hilt/img/作用域.png)

绑定的作用域必须与其安装的组件的作用域一致，否则在运行程序中会发生异常。

无法直接添加注解（第三方库），可以新建一个 Util 类，使用 @Module 、 @InstallIn 与 @Provides 提供实例，使用 @Singleton （单例，作用域）设置组件实例的作用域。

@Qualifier 注解的作用就是为相同类型的类注入不同的实例。

@Retention 用于声明注解的作用范围。

* AnnotationRetention.BINARY 
  
  * 表示注解在编译后将会被保留，保留到 class 字节码，但运行时反射不可见。
  
  * 使用场景：最常用，自定义标记注解基本都用这个。

* AnnotationRetention.SOURCE 
  
  * 仅源码期保留，编译后直接丢弃，class 文件里没有
  
  * 用途：只给编译器/IDE 看的标记，不参加运行时。
  
  * 使用场景：很少用

* AnnotationRetention.RUNTIME
  
  * 保留到 class，运行时可通过反射读取
  
  * 使用场景：需要反射读取注解信息时才用。

### 原理

```kotlin
class UserManager @Inject constructor() {
    val TAG = "UserManager"
    fun getUserToken() {
        Log.d(TAG, "获取用户token")
    }
}
```

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
        @Inject
        lateinit var userManager: UserManager
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        user.getUserToken()
        }
}
```

在 MainActivity 类中依赖注入了 UserManager 类。

为 UserManager 对象声明 @Inject 注解后，系统会生成 UserManager_Factory 类：

```kotlin
// UserManager_Factory 类继承自 Provider<T> 的子类 Factory<UserManager>
public final class UserManager_Factory implements Factory<UserManager> {
    @Override
    public UserManager get() {
        return newInstance();
    }
    public static UserManager_Factory create() {
        return InstanceHolder.INSTANCE;
    }
    public static UserManager newInstance() {
        return new UserManager();
    }
    private static final class InstanceHolder {
        private static final UserManager_Factory INSTANCE = new UserManager_Factory();
    }
}
```

为 MainActivity 声明了 @AndroidEntryPoint 注解，系统会自动生成基类 Hilt_MainActivity，Hilt_MainActivity 的 onCreate 方法的代码如下：

```kotlin
@CallSuper
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    inject();
    super.onCreate(savedInstanceState);
}
```

Hilt 在编译期把 MainActivity 的父类偷偷换成了 Hilt 生成的 Hilt_Mainactivity，从而让系统在启动 Activity 时，自动先跑 Hilt_Mainactivity 的 onCreate，再跑自己的 onCreate。

Hilt 怎么做到改父类：

1. 注解处理器：监测到 @AndroidEntryPoint，生成 Hilt_MainActivity 类。

2. 字节码插桩
   
   Hilt 借助 Dagger + AGP Transform，在编译阶段把 MainActivity extends AppCompatActivity 改成 MainActivity extends Hilt_MainActivity。

```kotlin
protected void inject() {
    ((MainActivity_GeneratedInjector) generatedComponent()).injectMainActivity(UnsafeCasts.<MainActivity>unsafeCast(this));
}
```

MainActivity_GeneratedInjector 的 injectMainActivity 方法最终调用 DaggerBaseApplication_HiltComponents_ApplicationC 的 injectMainActivity2 方法：

```kotlin
private MainActivity injectMainActivity2(MainActivity instance) {
    MainActivity_MembersInjector.injectMainViewModel(instance, new MainViewModel());
    MainActivity_MembersInjector.injectOkHttpClient(instance, DaggerBaseApplication_HiltComponents_ApplicationC.this.getOkHttpClientStandardOkHttpClient());
    MainActivity_MembersInjector.injectOtherOkHttpClient(instance, DaggerBaseApplication_HiltComponents_ApplicationC.this.getOkHttpClientStandardOkHttpClient());
    MainActivity_MembersInjector.injectUser(instance, new UserManager());
    MainActivity_MembersInjector.injectMobilePhone(instance, getMobilePhone());
    return instance;
}
```

injectUser 方法每次都会创建一个 UserManager 对象，这是 Hilt 作用域默认的实现效果。 

@Singleton 全局单例：首次注入时创建全局唯一实例，后续所有地方再注入，直接复用这个实例，不会重复创建。

### 其他

**字节码插桩**

字节码插桩：在 Java/Kotlin 源码编译为 .class 字节码之后、打包成 APK 之前，修改、新增、替换字节码指令/类结构的技术。

* 源码文件（.kt/.java）不会改动，打开源码看不到变化。

* 改动发生在二进制字节码层面，最终运行的 Class 类已经被改造。

Android 中主流依托 AGP Transform + ASM 实现，Hilt、ARouter、埋点、热修复、无痕权限框架都在用。

普通编译流程：源码 -》 编译器 -〉 .class 字节码 -》 打包 dex - > apk

带字节码插桩流程（Hilt走的流程）：源码 -〉 编译生成 .class -》 Transform 阶段插桩修改字节码 -〉 新的 .class -> 转 dex -> APK

插桩常见两类场景：

1. 类结构修改（Hilt 主力用法）
   
   * 改写父类、实现接口、新增字段/方法
   
   * 例子：Hilt 替换 Activity/Fragment 的父类

2. 方法内代码植入（埋点、监控常用）
   
   * 在方法开头/结尾/指定位置插入一段字节码指令
   
   * 例子：所有 Activity 的 onResult 自动加页面统计代码

特点：

1. 无侵入：业务源码不用改，零耦合

2. 编译器生效：运行时不做反射，性能远优于动态代理/反射

3. 全局批量处理：一次规则，作用于所有匹配的类/方法

优点：

* 统一逻辑（注入、埋点、监控）集中处理，业务代码干净

* 编译器完成，运行时开销极小

缺点：

* 字节码可读性差，出问题排查难度高

* 多框架同时插桩时，可能出现顺序冲突、类重复、方法冲突

* 混淆、R8 压缩如果配置不当，会破坏插桩逻辑（Hilt 必须配专属混淆规则）

### 资料

* 《Android Jetpack开发：原理解析与应用实战》- 第 8 章 减少手动依赖，探究 Hilt
