# Android 的常见问题 6

# 1. application,activity，service,contentprovider他们的 context有什么区别。

[Activity、Service和Application的Context的区别](https://blog.csdn.net/qq475703980/article/details/88430891)

## 1、Context的类图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190531205848781.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDc1NzAzOTgw,size_16,color_FFFFFF,t_70)

- Context:是一个接口类，主要提供通用接口
- ContextImpl:Context接口的具体实现类
- ContextWrapper：Context的包装类，内部持有一个ContextImpl的实例对象mBase,对Context的操作最终都进入ContextImpl类
- ContextThemeWrapper：该类内部包含了主题(Theme)相关的接口，即android:theme属性指定的。Service不需要主题，所以Service直接继承于ContextWrapper类。而**Activity继承此类。**

1 个应用包含的 Context 个数：Service 个数+ Activity 个数+1(Application类本身对应一个Context对象)。

## 2、Context的类型

并不是所有的context实例都是等价的。根据Android应用的组件不同，访问的context推向有些细微的差别。

- **Application** － 是一个运行在你的应用进程中的单例。在Activity或者Service中，它可以通过getApplication()函数获得，或者人和继承于context的对象中，通过getApplicationContext()方法获得。不管你是通过何种方法在哪里获得的，在一个进程内，你总是获得到同一个实例。
- **Activity/Service** － 继承于ContextWrapper，它实现了与context同样API，但是代理这些方法调用到内部隐藏的Context实例，即我们所知道的基础context。任何时候当系统创建一个新的Activity或者Service实例的时候，它也创建一个新的ContextImpl实例来做所有的繁重的工作。每一个Activity和Service以及其对应的基础context，对每个实例来说都是唯一的。
- **BroadcastReciver** － 它本身不是context，也没有context在它里面，但是每当一个新的广播到达的时候，框架都传递一个context对象到onReceive()。这个context是一个ReceiverRestrictedContext实例，它有两个主要函数被禁掉：registerReceiver()和bindService()。这两个函数在BroadcastReceiver.onReceive()不允许调用。每次Receiver处理一个广播，传递进来的context都是一个新的实例。
- **ContentProvider** － 它本身也不是一个Context，但是它可以通过getContext()函数给你一个Context对象。如果ContentProvider是在调用者的的本地（例如，在同一个应用进程），getContext()将返回的是Application单例。然而，如果调用这和ContentProvider在不同的进程的时候，它将返回一个新创建的实例代表这个Provider所运行的包。

## 3、不同Context的能力

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190531205240514.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxNDc1NzAzOTgw,size_16,color_FFFFFF,t_70)

注：NO1 表示Application context的确可以开始一个Activity，但是它需要创建一个新的task。这可能会满足一些特定的需求，但是在你的应用中会创建一个不标准的回退栈（back stack），这通常是不推荐的或者不是是好的实践。

NO2 表示这是非法的，但是这个填充（inflation）的确可以完成，但是是使用所运行的系统默认的主题（theme），而不是你app定义的主题。

NO3 在Android4.2以上，如果Receiver是null的话（这是用来获取一个sticky broadcast的当前 值的），这是允许的。

## 4、与UI相关的都用Activity

从前面的表格中可以看到，application context有很多功能并不是合适去做，而这些功能都与UI相关。**实际上，只有Activity能够处理所有与UI相关的任务**。其他类别的context实例功能都差不多。

尝试显示一个使用Aplication context创建的Dialog，或者使用Application context开始一个Activity，系统会抛出一个异常，让你的application崩溃，非常强的告诉你某些地方出了问题。

填充View(inflating layout)和使用的Context有很大关系。当你使用Application context来inflate一个布局的时候，框架并不会报错，并返回一个使用系统默认的主题创建一个完美的view给你，而没有考虑你的applicaiton自定义的theme和style。这是因为Acitivity是唯一的绑定了在manifast文件种定义主题的Context。其他的Context实例将会使用系统默认的主题来inflater你的view。导致显示的结果并不是你所希望的。可以参考博客 http://www.doubleencore.com/2013/05/layout-inflation-as-intended/

## 5、 使用Context的小经验

1、可能有些情况下，在某些Application的设计中，我们可能既必须长期保存一个的引用，并且为了完成与UI相关的工作又必须保存一个Activity。如果出现这种情况，我将会强烈建议你重新考虑你的设计，它将是一个很好的“反框架”教材。

2、绝大多数情况下，使用在你的所工作的组建内部能够直接获取的Context。只要这个引用没有超过这个组建的生命周期，你可以安全的保存这个引用。一旦你要保存一个context的引用，它超过了你的Activity或者Service的生命周期范围，甚至是暂时的，你就需要转换你的引用为Application context。

## 6、一个APP中有多少个Context

通常等于 Application + Activity + Service 的数量，而Application通常为1，因为应用通常是单进程。

## 7、Activity、Service、Applciation的对比总结

**Activity**

- 1、UI相关的Context都推荐使用Activity，否则有可能会有异常
- 2、**Activity的功能最全面**，可以在任何需要Context的地方使用Activity

**Service**

- 1、不能用来创建Dialog
- 2、如果用来进行视图填充（LayoutInflation），设置的主题不会生效，只会显示默认主题
- 3、如果用来启动一个Activity，新的Activity会在一个新的task任务栈，不建议这么使用

**Applciation**

- 1、直接用来创建创建Dialog，会抛异常Crash，除非进行特殊设置，所以不建议这么使用

- 2、和Service相同，不建议用来进行视图填充（LayoutInflation），否则只显示默认主题
- 3、和Service相同，不建议用来启动Activity



# 2. 问下载一个图片的时候直接下载了一个5g的图片，不压缩一定会产生OOM问题，那么怎么去获取这个图片的长宽呢，或者说这个图片的大小的大小在你没下载之前如何得到。



# 3. ActivityA -> Activity B -> Activity A 

  Activity A 启动模式为 singleTask 

  Activity B 启动模式为常规模式 

  问  A 启动 B，B 又启动 A 的生命周期调用顺序？ 

启动 A ：A onCreate() ->A onStart() ->A onResume() 

A 启动 B： A onPause() -> B onCreate() -> B onStart() -> B onResume() -> A onStop() -> A onDestory()

B 又启动 A：B onPause() -> A onNewIntent() -> A onRestart() -> A onStart() -> A onResume() -> B onStop() -> B onDestory()

# 4. onsaveinstancestate() ，说一下调用时机，它用来干什么的。 

　　onSaveInstanceState 方法会在 Activity 异常销毁之前调用，用来保存需要保存的数据，onRestoreInstanceState 方法在 Activity 重建之后获取保存的数据。

　　关于 onSaveInstanceState 与 onRestoreInstanceState 方法需要注意的一些问题：

     　　1. onSaveInstanceState 方法的调用时机是在 onStop 之前，与 onPause 没有固定的时序关系。而 onRestoreInstanceState 方法则是在 onStart 之后调用。
     　　2. 正常情况下的活动销毁并不会调用这两个方法，只有当活动异常销毁并且有机会重现展示的时候才会进行调用，除了资源配置的改变外，activity 因内存不足被销毁也是通过这两个方法保存数据。
     　　3. 在 onRestoreInstanceState 和 onCreate 都可以进行数据恢复工作，但是根据官方文档建议采用在 onRestoreInstanceState 中去恢复。
     　　4. 在 onSaveInstanceState 和 onRestoreInstanceState 这两个方法中，系统会默认为我们进行一定的恢复工作，具体地讲，默认实现会为布局中的每个 View 调用相应的 onSaveInstanceState() 方法，让每个视图都能提供有关自身的应保存信息。Android 框架中几乎每个小部件都会根据需要实现此方法，以便在重建 Activity 时自动保存和恢复 UI 所做的任何可见更改，例如 EditText 中的文本信息、ListView 中的滚动位置、TextView 的文本信息（需要设置 freezesText 属性才能自动保存）等（注意，组件一定要添加 id 才可以）。也可以通过 android:saveEnabled 属性设置为 “false” 或通过调用 setSaveEnabled() 方法显式阻止布局内的视图保存其状态，通常不会将该属性停用，除非想要以不同方式恢复 Activity UI 的状态。
     　　5. onSaveInstanceState() 常见的触发场景有：横竖屏切换、按下电源键、按下菜单键、切换到别的 Activity 等；onRestoreInstanceState() 常见的触发场景有：横竖屏切换、切换语言等等。

# 5. onsaveinstancestate() 保存的那个参数叫什么？Bundle 里面都放一些什么东西？怎么实现序列化？Parcelable 和 Serializable 有什么区别？ 

参数叫 Bundle 。

系统回收Activity调用了`onDestroy()`，但调用了`onDestroy()`并不表示出栈，返回栈中还有Activity的部分信息，返回键和`finish()`才会导致出栈。

Activity 出栈 Bundle 对象也会销毁。Activity 只要不出栈，Bundle 对象就还在。

返回键和`finish()`会使当前Activity出栈，Bundle对象也会销毁。

系统回收Activity后，返回栈中还有该Activity的部分信息，所以按返回键还可以回到该Activity只是需要重新生成，此时Bundle对象还在。屏幕方向切换属于这种情况。

Bundle主要用于传递数据；它保存的数据，是以key-value(键值对)的形式存在的。

Bundle经常使用在Activity之间或者线程间传递数据，传递的数据可以是boolean、byte、int、long、float、double、string等基本类型或它们对应的数组，也可以是对象或对象数组。

当Bundle传递的是对象或对象数组时，必须实现Serializable或Parcelable接口。

Bundle提供了各种常用类型的putXxx()/getXxx()方法，用于读写基本类型的数据。（各种方法可以查看API）

同时实现 Parcelable 或者 Serializable 实现序列化。



# 6. 请简述Android事件传递机制， ACTION_CANCEL事件何时触发？ 

step1. 父View收到ACTION_DOWN，如果没有拦截事件，则ACTION_DOWN前驱事件被子视图接收，父视图后续事件会发送到子View。

step2. 此时如果在父View中拦截ACTION_UP或ACTION_MOVE，在第一次父视图拦截消息的瞬间，父视图指定子视图不接受后续消息了，同时子视图会收到ACTION_CANCEL事件。



一般ACTION_CANCEL和ACTION_UP都作为View一段事件处理的结束。

子View在处理一个Touch事件中，父View的onInterceptTouchEvent返回true，此时子View会接收到MotionEvent.Action_Cancel。

# 7. asynctask的原理 AsyncTask是对Thread和Handler的组合包装。 

# 8. activity从后台进程切换到前台进程的生命周期； 

onRestart()->onStart() ->onResume()

# 9. Bitmap resize相关，设置option，decode 

[android BitmapFacty.Options的用法](https://blog.csdn.net/b1047368489/article/details/101093075/)

通常我们在开发android应用程序时，在加载图片时常常需要与Bitmap打交道，一般会使用BitmapFactory中提供的相关decode方法获取；

如果一张很大的图片，我们不加处理直接decode的话常常会抛出oom即 out of memory的异常。为了尽量避免这种情况的发生，我们就会

用到BitmapFactory中的一个内部类Options提供相关选项进行设置。

 **inJustDecodeBounds** 如果将其设为true的话，在decode时将会返回null,通过此设置可以去查询一个bitmap的属性，比如bitmap的长与宽，而不占用内存大小。

####  

**inPreferredConfig** 通过设置此值可以用来降低内存消耗，默认为ARGB_8888: 每个像素4字节. 共32位。 
   Alpha_8: 只保存透明度，共8位，1字节。 
   ARGB_4444: 共16位，2字节。 
   RGB_565:共16位，2字节。 
   如果不需要透明度，可把默认值ARGB_8888改为RGB_565,节约一半内存。 

 

**inSampleSize** 对大图片进行压缩，可先设置Options.inJustDecodeBounds，获取Bitmap的外围数据，宽和高等。然后计算压缩比例，进行压缩。

**inPurgeable与inInputShareable** 二个是并列使用，如果设置了inPurgeable = false，则inInputShareable的值会被忽略；这二个选项的作用主要是便于系统及时回收bitmap占用的内存; **inPurgeable**:设置为True,则使用BitmapFactory创建的Bitmap用于存储Pixel的内存空间，在系统内存不足时可以被回收，当应用需要再次访问该Bitmap的Pixel时，系统会再次调用BitmapFactory 的decode方法重新生成Bitmap的Pixel数组。 *设置为False时，表示不能被回收。* 

 ****inInputShareable**：设置是否深拷贝，与inPurgeable结合使用，inPurgeable为false时，该参数无意义**

[Bitmap详解与Bitmap的内存优化](https://www.jianshu.com/p/8206dd8b6d8b)

## 一、Bitmap：

Bitmap是Android系统中的图像处理的最重要类之一。用它可以获取图像文件信息，进行图像剪切、旋转、缩放等操作，并可以指定格式保存图像文件。
**常用方法：**

- public void recycle() 　// 回收位图占用的内存空间，把位图标记为Dead

- public final boolean isRecycled() 　//判断位图内存是否已释放

- public final int getWidth()　//获取位图的宽度

- public final int getHeight()　//获取位图的高度

- public final boolean isMutable()　//图片是否可修改

- public int getScaledWidth(Canvas canvas)　//获取指定密度转换后的图像的宽度

- public int getScaledHeight(Canvas canvas)　//获取指定密度转换后的图像的高度

- public boolean compress(CompressFormat format, int quality, OutputStream stream)　//按指定的图片格式以及画质，将图片转换为输出流。
  format：压缩图像的格式,如Bitmap.CompressFormat.PNG或Bitmap.CompressFormat.JPEG
  quality：画质，0-100.0表示最低画质压缩，100以最高画质压缩。对于PNG等无损格式的图片，会忽略此项设置。

  stream: OutputStream中写入压缩数据。
  return: 是否成功压缩到指定的流。

* public static Bitmap createBitmap(Bitmap src)　 //以src为原图生成不可变得新图像

* public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter)　//以src为原图，创建新的图像，指定新图像的高宽以及是否可变。

* public static Bitmap createBitmap(int width, int height, Config config)　//创建指定格式、大小的位图

* public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height)　//以source为原图，创建新的图片，指定起始坐标以及新图像的高宽。

## 二、**BitmapFactory工厂类：**

**Option 参数类：**

- public boolean inJustDecodeBounds　//如果设置为true，不获取图片，不分配内存，但会返回图片的高度宽度信息。
  如果将这个值置为true，那么在解码的时候将不会返回bitmap，只会返回这个bitmap的尺寸。这个属性的目的是，如果你只想知道一个bitmap的尺寸，但又不想将其加载到内存时。这是一个非常有用的属性。
- public int inSampleSize　//图片缩放的倍数
  这个值是一个int，当它小于1的时候，将会被当做1处理，如果大于1，那么就会按照比例（1 / inSampleSize）缩小bitmap的宽和高、降低分辨率，大于1时这个值将会被处置为2的倍数。例如，width=100，height=100，inSampleSize=2，那么就会将bitmap处理为，width=50，height=50，宽高降为1 / 2，像素数降为1 / 4。
- public int outWidth　//获取图片的宽度值
- public int outHeight　//获取图片的高度值
  表示这个Bitmap的宽和高，一般和inJustDecodeBounds一起使用来获得Bitmap的宽高，但是不加载到内存。
- public int inDensity　//用于位图的像素压缩比
- public int inTargetDensity　//用于目标位图的像素压缩比（要生成的位图）
- public byte[] inTempStorage　 //创建临时文件，将图片存储
- public boolean inScaled　//设置为true时进行图片压缩，从inDensity到inTargetDensity
- public boolean inDither 　//如果为true,解码器尝试抖动解码
- public Bitmap.Config inPreferredConfig　 //设置解码器
  这个值是设置色彩模式，默认值是ARGB_8888，在这个模式下，一个像素点占用4bytes空间，一般对透明度不做要求的话，一般采用RGB_565模式，这个模式下一个像素点占用2bytes。
- public String outMimeType　 //设置解码图像

* public boolean inPurgeable　//当存储Pixel的内存空间在系统内存不足时是否可以被回收

* public boolean inInputShareable 　//inPurgeable为true情况下才生效，是否可以共享一个InputStream

* public boolean inPreferQualityOverSpeed 　//为true则优先保证Bitmap质量其次是解码速度

* public boolean inMutable　 //配置Bitmap是否可以更改，比如：在Bitmap上隔几个像素加一条线段

* public int inScreenDensity　 //当前屏幕的像素密度

**工厂方法:**

- public static Bitmap decodeFile(String pathName, Options opts) 　//从文件读取图片
- public static Bitmap decodeFile(String pathName)
- public static Bitmap decodeStream(InputStream is)　 //从输入流读取图片
- public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts)
- public static Bitmap decodeResource(Resources res, int id)　 //从资源文件读取图片
- public static Bitmap decodeResource(Resources res, int id, Options opts)
- public static Bitmap decodeByteArray(byte[] data, int offset, int length)　 //从数组读取图片
- public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts)
- public static Bitmap decodeFileDescriptor(FileDescriptor fd)　//从文件读取文件 与decodeFile不同的是这个直接调用JNI函数进行读取 效率比较高
- public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts)

** Bitmap.Config inPreferredConfig :**
枚举变量 （位图位数越高代表其可以存储的颜色信息越多，图像越逼真，占用内存越大）

- public static final Bitmap.Config ALPHA_8 　//代表8位Alpha位图 每个像素占用1byte内存
- public static final Bitmap.Config ARGB_4444 　//代表16位ARGB位图 每个像素占用2byte内存
- public static final Bitmap.Config ARGB_8888 　//代表32位ARGB位图 每个像素占用4byte内存
- public static final Bitmap.Config RGB_565 　//代表8位RGB位图 每个像素占用2byte内存

Android中一张图片（BitMap）占用的内存主要和以下几个因数有关：图片长度，图片宽度，单位像素占用的字节数。一张图片（BitMap）占用的内存=图片长度 * 图片宽度 * 单位像素占用的字节数。

# 10. 用MultiDex解决何事？其根本原因在于？Dex如何优化？主Dex放哪些东西？主Dex和其他Dex调用、关联？Odex优化点在于什么？ 

 答：MultiDex解决方法数65535的限制问题，即方法数不能超过65535个；方法id是short类型4个字节来存储的，所以数目范围应在0-2^32即0-65535；MultiDex工作原理分析和优化方案; 主dex中：应用启动就必须加载的类，有一个[keep]()文件来控制；其他dex文件都是通过主dex加载进来的；odex优化点：预加载； 

# 11. 多渠道打包如何实现(Flavor、Dimension应用)？从母包生出渠道包实现方法？渠道标识替换原理？ 

## 12. Android打包哪些类型文件不能混淆？ 

[Android打包不可混淆哪些资源](https://www.jianshu.com/p/2555e8edbade)

1、反射中使用的元素；

2、GSON的序列化与反序列化（本质还是用到了反射）

3、枚举也不要混淆（用到反射）

4、四大组件不要混淆（会导致Manifest名称与混淆后名称不一致）

5、其他：

①jni调用的java方法

②java的native方法

③js调用的java方法

④第三方库不建议混淆

⑤其他和反射相关的一些情况

⑥类之间有调用 static final常量

个人理解：不可混淆项目主要分两类：一类是反射；第二类是常量（final和Manifest等，都可以理解为常量）