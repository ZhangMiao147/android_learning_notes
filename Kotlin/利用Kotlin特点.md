# 利用 Kotlin 特点

## 1. kotlin-first but no kotlin-must

Kotlin 的语法糖更加提高了开发效率，加快了开发速度，使开发工作变得有趣，又可以有更多时间写注释了。但是其实对于 Kotlin 和 Java 在 Android 开发上的选择，除了开发人员对语言的喜好，同时也会应该到各自语言的魅力和特点，甚至项目的需求以及后续维护等等各种因素，没有绝对的选择的。要做到的是放大不同语言有点并加以拓展，不是一味只选择某个语言，语言不是问题，怎么用才是关键。

## 2. 利用 Lazy 帮助实现初始化

lazy() 是一个函数，接受一个 Lambda 表达式作为参数，返回一个 Lazy\<T> 实例的函数，返回的实例可以作为实现延迟属性的委托；第一次调用 get() 会执行已传递给 lazy() 的 lamda 表达式并记录结果，后续调用 get() 只是返回记录的结果。先贴上代码：

```kotlin
fun startInit(component: Components.()->Unit){
    component.invoke(Components.get())
}

class Components {

    companion object{

        private val entry = ArrayMap<String,Any?>()

        private val instant by lazy { Components() }

        fun get() = instant

        fun getEntry() = entry
    }


    inline fun <reified T>single(single: ()->T){
        val name = T::class.java.name
        getEntry()[name] = single()
    }


}

inline fun <reified T> get(name: String = T::class.java.name) : T{
   return Components.getEntry()[name] as T
}

inline fun <reified T> inject(name: String = T::class.java.name) : Lazy<T> {
    return lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { Components.getEntry()[name]  as T }
}


// 使用例子
startInit {
            single {  RoomApi.getDao() }
            single {  RetroHttp.createApi(Main::class.java) }
        }

 
 private val main : Main by inject()
 private val dao : MainDao by inject()
```

简单的代码优化，提高开发效率。

## 3. 借助协程实现倒计时和超时等待任务

### 3.1. 倒计时

这个一直都是安卓开发的常见需求，普遍可采用如下方案

1. RxJava
2. CountDownTimer
3. Timer + TimerTask
4. 线程

这些都是属于常见的可行方案。可以借助协程这个工具，自己实现其中一种方案，优雅地编写代码，一样贴代码

```kotlin
fun counter(dispatcher: CoroutineContext,start:Int, end:Int, delay:Long,
onProgress:((value:Int)->Unit),onFinish: (()->Unit)?= null){
    val out = flow<Int> {
        for (i in start..end) {
            emit(i)
            kotlinx.coroutines.delay(delay) 
        }
    }
    GlobalScope.launch {
        withContext(dispatcher) {
            out.collect {
                onProgress.invoke(it)
            }
            onFinish?.invoke()
        }
    }
}
```

利用 flow 实现这种异步输出数字同时，达到每个输出结果之间延迟，这样就能更好地计算出结果，其中的 GlobalScope.launch 这个方法十分建议替换成 lifecycleScope.launchWhenStarted 在安卓上生命周期状态十分重要，也是近年 Google 推出这个框架的原因：

```
androidx.lifecycle:lifecycle-***-***
```

其中的 lifecycleScope.launchWhenStarted 来自这里。

```
androidx.lifecycle:lifecycle-runtime-ktx
```

### 3.2. 超时等待任务

在平时的开发中，或多或少遇到一种需求就是，需要执行一个超时任务，然后这个任务有个最大超时时间，超过了这个时间，任务作废或执行另一个任务，超时之内的要提前执行其他逻辑等等。例如不少的启动页拿广告的，无论是广告来自哪里，涉及到网络获取，总有遇到延时问题，要拿到广告才能进入主界面，也不能等太长时间，影响正常的功能使用，而的确需要这个广告的相关收益。那只好执行类似的超时任务。解决方案是如下：

还是借助协程来进行，同样 GlobalScope.launch 这个方法十分建议替换成 lifecycleScope.launchWhenStarted

```kotlin
fun waitUtil(dispatcher: CoroutineContext,outTimeMills:Long,onTime:(result:Boolean)->Unit, doWork: suspend () ->Unit){
    GlobalScope.launch(dispatcher) {
        val result = withTimeoutOrNull(outTimeMills){
            doWork.invoke()
        }
        onTime.invoke(result != null)
    }
}
```

利用协程里的超时方法来帮助判断任务的执行是否超时了。

总结：其实协程是一个对线程池封装的好框架。

## 4. 浮点变量转 dp、px、sp 等等

这个也是平时开发经常用到，px 转 dp，px 转 sp，等等。先贴代码：

```kotlin
val Float.dp
   get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this,Resources.getSystem().displayMetrics)

val Float.px
  get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this,Resources.getSystem().displayMetrics)

val Float.sp
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this,Resources.getSystem().displayMetrics)

```

这种扩展方法在日常的 Kotlin 卡发经常用到。

## 5. 在适应位置跳出 Kotlin 的 ForEach

ForEach 是这种偷懒写法 list.forEach{}，这种情况下用 break 跳出是有问题的。优化方案如下：

```kotlin
inline fun <T> Iterable<T>.forEachBreak(action: (T) -> Boolean ){
    kotlin.run breaking@{
        for (element in this)
            if(!action(element)){
                return@breaking
            }
    }
}
```

利用最后的返回值为 false 的时候跳出去循环，当然其实如果使用 for(element in this) 是可以正常使用 break、countine 的，举个例子：

```kotlin
val list = ArrayList<Int>()
    for(i in 0..10){
        list.add(i)
    }

    list.forEach {
        if(it>5){
            break;//编译器会报错 'break' and 'continue' are only allowed inside a loop
        }
        print("Test 1 Loop in $it")
    }

    for(i in list){
        if(i>5){
            break
        }
        print("Test 2 Loop in $i")
    }
    
       list.forEachBreak {
        if(it>5){
           return@forEachBreak false
        }
        println("Test 3 Loop in $it")
        true
    }
```

除了报错的那个下面两个输出的结果是一样的

```kotlin
Test 2 Loop in 0
Test 2 Loop in 1
Test 2 Loop in 2
Test 2 Loop in 3
Test 2 Loop in 4
Test 2 Loop in 5
Test 3 Loop in 0
Test 3 Loop in 1
Test 3 Loop in 2
Test 3 Loop in 3
Test 3 Loop in 4
Test 3 Loop in 5
```

## 6. EditText 的 addTextChangedListener 和 addOnTabSelectedListener

### 6.1. EditText 的 addTextChangedListener

```kotlin
fun EditText.textWatcher(textWatch: SimpleTextWatcher.() -> Unit) {
    val simpleTextWatcher = SimpleTextWatcher(this)
    textWatch.invoke(simpleTextWatcher)
}

class SimpleTextWatcher(var view: EditText) {

    private var afterText: (Editable?.() -> Unit)? = null
    fun afterTextChanged(afterText: (Editable?.() -> Unit)) {
        this.afterText = afterText
    }

    private var beforeText: ((s: CharSequence?, start: Int, count: Int, after: Int) -> Unit)? = null
    fun beforeTextChanged(beforeText: ((s: CharSequence?, start: Int, count: Int, after: Int) -> Unit)) {
        this.beforeText = beforeText
    }

    private var onTextChanged: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? =
        null

    fun onTextChanged(onTextChanged: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)) {
        this.onTextChanged = onTextChanged
    }

    init {
        view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterText?.invoke(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeText?.invoke(s, start, count, after)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged?.invoke(s, start, before, count)
            }
        })
    }
}
```

### 6.2. TabLayout 的 addOnTabSelectedListener

```kotlin
fun TabLayout.onTabSelected(tabSelect: TabSelect.() -> Unit) {
    tabSelect.invoke(TabSelect(this))
}

class TabSelect(tab: TabLayout) {
    private var tabReselected: ((tab: TabLayout.Tab) -> Unit)? = null
    private var tabUnselected: ((tab: TabLayout.Tab) -> Unit)? = null
    private var tabSelected: ((tab: TabLayout.Tab) -> Unit)? = null

    fun onTabReselected(tabReselected: (TabLayout.Tab.() -> Unit)) {
        this.tabReselected = tabReselected
    }

    fun onTabUnselected(tabUnselected: (TabLayout.Tab.() -> Unit)) {
        this.tabUnselected = tabUnselected
    }

    fun onTabSelected(tabSelected: (TabLayout.Tab.() -> Unit)) {
        this.tabSelected = tabSelected
    }

    init {
        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.apply { tabReselected?.invoke(tab) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.apply { tabUnselected?.invoke(tab) }
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.apply { tabSelected?.invoke(tab) }
            }

        })
    }
}
  //使用
  tab.onTabSelected {
            onTabSelected {
                pos = position
            }
        }
```

其实上面这两个都是很典型的 DSL 语法，利用 Kotlin 的 DSL，充分的发挥 Kotlin 的优雅，适当的方法命名，恰当的设计，能让开发者或者维护人员更好地理解代码。

## 7. Kotlin 的扩展函数

Kotlin 的扩展函数，在日常 Android 开发中经常用到，当然前提是使用 Kotlin 来开发。

举几个常见的，已经写好了直接用的：

```kotlin
@kotlin.internal.InlineOnly
public inline fun <T, R> T.let(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(this)
}

@kotlin.internal.InlineOnly
public inline fun <T> T.apply(block: T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}

@kotlin.internal.InlineOnly
@SinceKotlin("1.1")
public inline fun <T> T.also(block: (T) -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block(this)
    return this
}

public inline fun String.filter(predicate: (Char) -> Boolean): String {
    return filterTo(StringBuilder(), predicate).toString()
}
```

有了官方给的这些好用的扩展函数，的却能很好地减少代码量，如下：

```kotlin
 val stringTo20 = "20"
 Log.e(TAG, "value:${stringTo20.toInt()}")
 

 E/MainActivity: value:20
```

点击这个 `toInt()` 方法，进去看看就是进场用到的字符串转数字方法

```kotlin
@kotlin.internal.InlineOnly
public actual inline fun String.toInt(): Int = java.lang.Integer.parseInt(this)
```

但是如果写成这样呢？`Crash` 是必然的了

```kotlin
 val stringTo20 = "www"
 Log.e(TAG, "value:${stringTo20.toInt()}")
 
 java.lang.NumberFormatException: For input string: "www"
```

当然，如果那个字符串是正常的数字字符串，那代码确实直接用 `toInt()`，但是，凡事都有例外，例如有个需求是要从后台获取一个 “数字” 字符串，平时开发是不是应该尽力去避免这种情况呢？

```kotlin
 val stringTo20 = "www"
 fun String.forceToInt(default: Int = 0) : Int {
        return if(this.filter { it.isDigit() }.any { 	
          this.isNotEmpty() 
        })
   {
     toInt()
   }else{
     default
   }
 }
 Log.e(TAG, "value:${stringTo20.forceToInt(-1)}")
```

可以利用扩展函数处理这种情况，简单又方便。

## 8. Kotlin 的 infix 函数

这个 infix 符号修饰的是中缀函数，多用于修饰扩展函数，记得有且只有一个参数。

举应用 MutableLiveData 作为例子

```kotlin
infix fun <T:Any> MutableLiveData<T>?.post(newValue:T){
    this?.postValue(newValue)
}

infix fun <T:Any> MutableLiveData<T>?.set(newValue:T){
    this?.value = newValue
}

//可以直接使用变成
(MutableLiveData) data   post  newValue
(MutableLiveData) data   set  newValue
```

这种方法并不会带来什么性能上的优化，代码相比，执行方法西安的直观，特别是用于数字计算或者合并某些内容等等，切勿乱起名字，否则会很难拿维护，尽量保证名字能和功能相关。

如下：

```kotlin
button translationX 200f
infix fun View.translationX(float: Float){
        translationX = float
    }
    
//如果像这样，很难有第一反应是translationX，我会以为是view.x = float    
 button toX 200f
 infix fun View.toX(float: Float){
        translationX = float
    }
```

## 参考文章

1. [Android开发: 分享如何利用好Kotlin的特点（一）---- 提高开发效率](https://juejin.cn/post/6844904177781604360)
1. [Android开发: 分享如何利用好Kotlin的特点（二）---- 优化你的代码](https://juejin.cn/post/6844904177940824078)
