# Kotlin 协程

## 1. 协程是什么

协程是一种非抢占式或者说协作式的计算机程序并发调度的实现，程序可以主动挂起或者恢复执行。这里还是需要有点儿操作系统的知识的，我们在 Java 虚拟机上所认识到的线程大多数的实现是映射到内核的线程的，也就是说线程当中的代码逻辑在线程抢到 CPU 的时间片的时候才可以执行，否则就得歇着，当然这对于我们开发者来说是透明的；而经常听到所谓的协程更轻量的意思是，协程并不会映射成内核线程或者其他这么重的资源，它的调度在用户态就可以搞定，任务之间的调度并非抢占式，而是协作式的。

``关于并发和并行：正因为 CPU 时间片足够小，因此即便一个单核的 CPU，也可以给我们营造多任务同时运行的假象，这就是所谓的“并发”。并行才是真正的同时运行。并发的话，更像是 Magic。``

从执行机制上来讲，协程跟回调没有什么本质的区别。

## 2. 协程启动

### 2.1. kotlin 启动线程

Kotlin 为线程提供了一个便捷方法：

```kotlin
val myThread = thread {
    //do what you want
}
```

这个 thread 方法有个参数 start 默认为 true，换句话说，这样创造出来的线程默认就是启动的，除非实在不想让它马上投入工作：

```kotlin
val myThread = thread(start = false) {
    //do what you want
}
//later on ...
myThread.start()
```

### 2.2. 协程的启动

简单的启动协程的方式：

```kotlin
GlobalScope.launch {
    //do what you want
}
```

启动协程需要三样东西，分别是上下文、启动模式、协程体，协程体就好比 Thread.run 当中的代码。

在 Kotlin 协程当中，启动模式是一个枚举：

```kotlin
public enum class CoroutineStart {
    DEFAULT,
    LAZY,
    @ExperimentalCoroutinesApi
    ATOMIC,
    @ExperimentalCoroutinesApi
    UNDISPATCHED;
}
```

| 模式         | 功能                                              |
| :----------- | :------------------------------------------------ |
| DEFAULT      | 立即执行协程体                                    |
| ATOMIC       | 立即执行协程体，但在开始运行之前无法取消          |
| UNDISPATCHED | 立即在当前线程执行协程体，直到第一个 suspend 调用 |
| LAZY         | 只有在需要的情况下运行                            |

#### 2.2.1. DEFAULT

四个启动模式当中我们最常用的其实是 DEFAULT 和 LAZY。

DEFAULT 是饿汉式启动，launch 调用后，会立即进入待调度状态，一旦调度器 OK 就可以开始执行。

``JVM 上默认调度器的实现就是开了一个线程池，但区区几个线程足以调度成千上万个协程，而且每一个协程都有自己的调用栈，这与纯粹的开线程池去执行异步任务有本质的区别。``

#### 2.2.2. LAZY

LAZY 是懒汉式启动，launch 后并不会有任何调度行为，协程体也自然不会进入执行状态，直到需要它执行的时候。什么叫需要它执行的时候呢？就是需要它的运行结果的时候，launch 调用后会返回一个 Job 实例，对于这种情况，可以：

* 调用 `Job.start`，主动触发协程的调度执行
* 调用 `Job.join`，隐式的触发协程的调度执行

#### 2.2.3. ATOMIC

ATOMIC 只能涉及 cancel 的时候才有意义，cancel 后协程会被取消掉，也就是不再执行了。那么调用 cancel 的时机不同，结果也是有差异的，例如协程调度之前、开始调度但尚未执行、已经开始执行、执行完毕等等。

```kotlin
log(1)
val job = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
    log(2)
}
job.cancel()
log(3)
```

创建了协程后立即 cancel，但由于是 ATOMIC 模式，因此协程一定会被调度，因此 1、2、3 一定都会输出，只是 2 和 3 的顺序就难说了。

对应的，如果是 DEFAULT 模式，在第一次调度该协程时如果 cancel 就已经调用，那么协程就会直接被 cancel 而不会有任何调用，当然也有可能协程开始时尚未被 cancel，那么它就可以正常启动了。所以上面的代码如果改用 DEFAULT 模式，那么 2 有可能会输出，也可能不会。

需要注意的是，cancel 调用一定会将该 job 的状态置为 canceling，只不过 ATOMIC 模式的协程在启动时无视了这一状态。

```kotlin
log(1)
val job = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
    log(2)
    delay(1000)
    log(3)
}
job.cancel()
log(4)
job.join()
```

在 2 和 3 之间加了一个 delay，delay 会使得协程体的执行被挂起，1000ms 之后再次调度后面的部分，因此 3 会在 2 执行之后 1000ms 时输出。对于 ATOMIC 模式，它一定会被启动，实际上在遇到第一个挂起点之前，它的执行是不会停止的，而 delay 是一个 suspend 函数，这是协程引来了自己的第一个挂起点，恰好 delay 是支持 cancel 的，因此后面的 3 将不会被打印。

使用线程的时候，想要让线程里面的任务停止执行也会面临类似的问题，但遗憾的是线程中看上去与 cancel 相近的 stop 接口已经被废弃，因为存在一些安全的问题。不过随着不断地深入探讨，会发现协程的 cancel 某种意义上更像线程的 interrupt。

#### 2.2.4. UNDISPATCHED

协程在这种模式下会直接开始在当前线程下执行，直到第一个挂起点，这听起来有点像 ATOMIC，不同之处在于 UNDISPATCHED 不经过任何调度器即开始执行协程体。当然遇到挂起点之后的执行就取决于挂起点本身的逻辑以及上下文当中的调度器了。

```kotlin
log(1)
val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
    log(2)
    delay(100)
    log(3)
}
log(4)
job.join()
log(5)
```

协程启动后会立即在当前线程执行，因此 1、2 会连续在同一线程中执行，delay 是挂起点，因此 3 会等 100ms 后再次调度，这时候 4 执行，join 要求等待协程执行完，因此等 3 输出后再执行 5。

## 3. 协程调度

### 3.1. 协程上下文

调度器本质上就是一个协程上下文的实现。

launch 函数有三个参数，第一个参数叫上下文，它的接口类型是 CoroutineContext，通常见到的上下文的类型是 CombinedContext 或者 EmptyCoroutineContext，一个表示上下文的组合，另一个表示什么都没有。

CoroutineContext 的接口方法：

```kotlin
@SinceKotlin("1.3")
public interface CoroutineContext {
    public operator fun <E : Element> get(key: Key<E>): E?
    public fun <R> fold(initial: R, operation: (R, Element) -> R): R
    public operator fun plus(context: CoroutineContext): CoroutineContext = ...
    public fun minusKey(key: Key<*>): CoroutineContext

    public interface Key<E : Element>

    public interface Element : CoroutineContext {
        public val key: Key<*>
        ...
    }
}
```

它简直就是一个以 Key 为索引的 List：

| CoroutineContext       | List          |
| :--------------------- | :------------ |
| get(Key)               | get(Int)      |
| plus(CoroutineContext) | plus(List)    |
| minusKey(Key)          | removeAt(Int) |

表中的 List.plus(List) 实际上指的是扩展方法 Collection<\T>.plus(elements: Interable<\T>):List<\T>

CoroutineContext 作为一个集合，它的元素就是源码中看到的 Element，每一个 Element 都有一个 key，因此它可以作为元素出现，同时它也是 CoroutineContext 的子接口，因此也可以作为集合出现。

CoroutineContext 原来是个数据结构。例如 List 是这么定义的：

```scala
sealed abstract class List[+A] extends ... {
    ...
    def head: A
    def tail: List[A]
    ...
}
```

在模式匹配的时候，List(1,2,3,4) 是可以匹配 x::y 的， x 就是1，y 则是 List(2,3,4)。

CombinedContext 的定义也非常类似：

```kotlin
internal class CombinedContext(
    private val left: CoroutineContext,
    private val element: Element
) : CoroutineContext, Serializable {
    ...
}
```

只不过它是反过来的，前面是集合，后面是单独的一个元素。我们在协程体里面访问到的 coroutineContext 大多是这个 CombinedContext 类型，表示有很多具体的上下文实现的集合，我们如果想要找到某一个特别的上下文时间，就需要用对应的 Key 来查找，例如：

```kotlin
suspend fun main(){
    GlobalScope.launch {
        println(coroutineContext[Job]) // "coroutine#1":StandaloneCoroutine{Active}@1ff62014
    }
    println(coroutineContext[Job]) // null，suspend main 虽然也是协程体，但它是更底层的逻辑，因此没有 Job 实例
}
```

这里的 Job 实际上是对它的 companion object 的引用

```kotlin
public interface Job : CoroutineContext.Element {
    /**
     * Key for [Job] instance in the coroutine context.
     */
    public companion object Key : CoroutineContext.Key<Job> { ... }
    ...
}
```

所以可以来一个获取当前 Job 的方法：

```kotlin
suspend inline fun Job.Key.currentJob() = coroutineContext[Job]

suspend fun coroutineJob(){
    GlobalScope.launch {
        log(Job.currentJob())
    }
    log(Job.currentJob())
}
```

可以通过指定上下文为协程添加一些特性，一个很好的例子就是为协程添加名称，方便调试：

```kotlin
GlobalScope.launch(CoroutineName("Hello")) {
    ...
}
```

如果有多个上下文需要添加，直接用 + 就可以了：

```kotlin
GlobalScope.launch(Dispatchers.Main + CoroutineName("Hello")) {
    ...
}
```

Dispatchers.Main 是调度器的一个实现。

### 3.2. 协程拦截器

```kotlin
public interface ContinuationInterceptor : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<ContinuationInterceptor>
    
    public fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T>
    ...
}

```

拦截器也是一个上下文的实现方式，拦截器可以左右你的协程的执行，同时为了保证它的功能的正确性，协程上下文集合永远将它放在最后面。

它拦截协程的方法也很简单，因为协程的本质就是回调 + “黑魔法”，而这个回调就是被拦截的 Continuation 了。Okhttp 用拦截器做缓存，打日志，还可以模拟请求，协程拦截器也是一样的道理。调度器就是基于拦截器实现的，换句话说调度器就是拦截器的一种。

可以自己定义一个拦截器放在协程上下文中，看看会发生什么。

```kotlin
class MyContinuationInterceptor: ContinuationInterceptor{
    override val key = ContinuationInterceptor
    override fun <T> interceptContinuation(continuation: Continuation<T>) = MyContinuation(continuation)
}

class MyContinuation<T>(val continuation: Continuation<T>): Continuation<T> {
    override val context = continuation.context
    override fun resumeWith(result: Result<T>) {
        log("<MyContinuation> $result" )
        continuation.resumeWith(result)
    }
}
```

只是在回调处打了一行日志。

```kotlin
suspend fun main() {
    GlobalScope.launch(MyContinuationInterceptor()) {
        log(1)
        val job = async {
            log(2)
            delay(1000)
            log(3)
            "Hello"
        }
        log(4)
        val result = job.await()
        log("5. $result")
    }.join()
    log(6)
}
```

通过 launch 启动了一个协程，为它指定了自己的拦截器作为上下文，紧接着在其中用 async 启动了一个协程，async 与 launch 从功能上是同等类型的函数，它们都被称作协程的 Build 函数，不同之处在于 async 启动的 Job 也就是实际上的 Deferred 可以有返回结果，可以通过 await 方法获取。

运行结果如下：

```
15:31:55:989 [main] <MyContinuation> Success(kotlin.Unit)  // ①
15:31:55:992 [main] 1
15:31:56:000 [main] <MyContinuation> Success(kotlin.Unit) // ②
15:31:56:000 [main] 2
15:31:56:031 [main] 4
15:31:57:029 [kotlinx.coroutines.DefaultExecutor] <MyContinuation> Success(kotlin.Unit) // ③
15:31:57:029 [kotlinx.coroutines.DefaultExecutor] 3
15:31:57:031 [kotlinx.coroutines.DefaultExecutor] <MyContinuation> Success(Hello) // ④
15:31:57:031 [kotlinx.coroutines.DefaultExecutor] 5. Hello
15:31:57:031 [kotlinx.coroutines.DefaultExecutor] 6
```

Continuation 回调了 4 次。

首先，所有协程启动的时候，都会有一次 Continuation.resumeWith 的操作，这一次操作对于调度器来说就是一次调度的机会，协程有机会调度到其他线程的关键之处就在于此。①、② 两处都是这种情况。

其次，delay 是挂起点，1000ms 之后需要继续调度执行该协程，因此就有了 ③ 处的日志。

最后，④ 处理的日志，正是返回结果。

并没有在拦截器当中切换线程，为什么从 ③ 处开始有了线程切换的操作？这个切换线程的逻辑源自于 delay，在 JVM 上 delay 实际上是在一个 ScheduledExcecutor 里面添加了一个延时任务，因此会发生线程切换。

### 3.3. 调度器

#### 3.3.1. 概述

```kotlin
public abstract class CoroutineDispatcher :
    AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    ...
    public abstract fun dispatch(context: CoroutineContext, block: Runnable)
    ...
}
```

调度器本身是协程上下文的子类，同时实现了拦截器的接口，dispatch 方法会在拦截器的方法 interceptContinuation 中调用，进而实现协程的调度。所以如果我们想要实现自己的调度器，继承这个类就可以了，不过通常都用现成的，它们定义在 Dispatchers 当中：

```
val Default: CoroutineDispatcher
val Main: MainCoroutineDispatcher
val Unconfined: CoroutineDispatcher
```

这个类的定义涉及到了 Kotlin MPP 的支持，因此在 Jvm 版本当中还会看到 val IO：CoroutineDispatcher。

| Jvm        | Js       | Native          |                 |
| ---------- | -------- | --------------- | --------------- |
| Default    | 线程池   | 主线程循环      | 主线程循环      |
| Main       | UI 线程  | 与 Default 相同 | 与 Default 相同 |
| Unconfined | 直接执行 | 直接执行        | 直接执行        |
| IO         | 线程池   | –               | –               |

* IO 仅在 Jvm 上有定义，它基于 Default 调度器背后的线程池，并实现了独立的队列和限制，因此协程调度器从 Default 切换到 IO 并不会触发线程切换。
* Main 主要用于 UI 相关程序，在 Jvm 上包括 Swing、JavaFx、Android，可将协程调度到各自的 UI 线程上。
* Js 本身就是单线程的事件循环，与 Jvm 上的 UI 程序比较类似。

#### 3.3.2. 编写 UI 相关程序

举一个很常见的场景，点击一个按钮做点儿异步的操作再回调刷新 UI：

```kotlin
suspend fun getUserCoroutine() = suspendCoroutine<User> {
    continuation ->
    getUser {
        continuation.resume(it)
    }
}
```

按钮点击时，可以：

```kotlin
getUserBtn.setOnClickListener {
    GlobalScope.launch(Dispatchers.Main) {
        userNameView.text = getUserCoroutine().name
    }
}
```

也可以用 anko-coroutines 当中的 View.onClick 扩展，这样就无需自己在这里用 launch 启动写成了。

suspendCoroution 这个方法并不是启动协程的，它运行在协程当中并且帮我们获取到当前协程的 Continuation 实例，也就是拿到回调，方便后面调用它的 resume 或者 resumeWithException 来返回结果或者抛出异常。

如果重复调用 resume 或者 resumeWithException 会收获一枚 IllegalStateException。

这里用到了 Dispatchers.Main 来确保 launch 启动的协程在调度时始终调度到 UI 线程。

看一下 Dispatchers.Main 的具体实现，在 Jvm 上，Main 的实现也比较有意思：

```kotlin
internal object MainDispatcherLoader {
    @JvmField
    val dispatcher: MainCoroutineDispatcher = loadMainDispatcher()

    private fun loadMainDispatcher(): MainCoroutineDispatcher {
        return try {
            val factories = MainDispatcherFactory::class.java.let { clz ->
                ServiceLoader.load(clz, clz.classLoader).toList()
            }
            factories.maxBy { it.loadPriority }?.tryCreateDispatcher(factories)
                ?: MissingMainCoroutineDispatcher(null)
        } catch (e: Throwable) {
            MissingMainCoroutineDispatcher(e)
        }
    }
}
```

在 Android 当中，协程框架通过注册 AndroidDispatcherFactory 使得 Main 最终被赋值为 HandleDispathcer 的实例，可以看 kotlin-coroutines-android 的源码实现。

#### 3.3.3. 绑定到任意线程的调度器

调度器的目的就是切线程，只要提供线程，调度器就应该很方便的创建出来：

```kotlin
suspend fun main() {
    val myDispatcher= Executors.newSingleThreadExecutor{ r -> Thread(r, "MyThread") }.asCoroutineDispatcher()
    GlobalScope.launch(myDispatcher) {
        log(1)
    }.join()
    log(2)
}
```

输出的信息iu表明协程运行在自己的线程上。

```
16:10:57:130 [MyThread] 1
16:10:57:136 [MyThread] 2
```

由于这个线程池是自己创建的，因此需要在合适的时候关闭它，不然程序并没有停止运行。

![](https://kotlinblog-1251218094.costj.myqcloud.com/9ab6e571-684b-4108-9600-a9e3981e7aca/media/15546248040111.jpg)

可以通过主动关闭线程池或者调用 `myDispatcher.close()` 来结束它的生命周期，再次运行程序就会正常退出了。

![](https://kotlinblog-1251218094.costj.myqcloud.com/9ab6e571-684b-4108-9600-a9e3981e7aca/media/15546249279403.jpg)

有人会说你创建的线程池的线程不是 daemon 的，所以主线程结束时 Jvm 不会停止运行。但是该释放的还是要及时释放，如果只是在程序的整个生命周期当中短暂的用了一下这个调度器，那么一直不关闭它对应的线程池岂不是会有线程泄漏吗？

Kotlin 协程设计者还废除了两个 API 并且开了一个 issue 说要重做这套 API。

**废弃的两个机遇线程池创建调度器的 API**

```kotlin
fun newSingleThreadContext(name: String): ExecutorCoroutineDispatcher
fun newFixedThreadPoolContext(nThreads: Int, name: String): ExecutorCoroutineDispatcher
```

这两者可以很方便的创建绑定到特定线程的调度器，但过于简洁的 API 似乎会让人忘记它的风险。

其实在多个线程上运行协程，线程总是这样切来切去其实并不会显得很轻量级，例如下面的例子就是比较可怕的了：

```kotlin
Executors.newFixedThreadPool(10)
        .asCoroutineDispatcher().use { dispatcher ->
            GlobalScope.launch(dispatcher) {
                log(1)
                val job = async {
                    log(2)
                    delay(1000)
                    log(3)
                    "Hello"
                }
                log(4)
                val result = job.await()
                log("5. $result")
            }.join()
            log(6)
        }
```

这里面除了 delay 那里有一次不可避免的线程切换外，其他几处协程挂起点的继续操作（Continuation.resume）都会切线程：

```
16:28:04:771 [pool-1-thread-1] 1
16:28:04:779 [pool-1-thread-1] 4
16:28:04:779 [pool-1-thread-2] 2
16:28:05:790 [pool-1-thread-3] 3
16:28:05:793 [pool-1-thread-4] 5. Hello
16:28:05:794 [pool-1-thread-4] 6
```

如果线程池只开 1 个线程，那么这里所有的输出都将在这唯一的线程中打印：

```
16:40:14:685 [pool-1-thread-1] 1
16:40:14:706 [pool-1-thread-1] 4
16:40:14:710 [pool-1-thread-1] 2
16:40:15:723 [pool-1-thread-1] 3
16:40:15:725 [pool-1-thread-1] 5. Hello
16:40:15:725 [pool-1-thread-1] 6
```

对比这二者，10 个线程的抢狂线程切换次数最少 3 次，而 1 个线程的情况则只要 delay 1000ms 之后恢复执行的时候那一次。多两次线程切换平均能多出 1ms 的耗时。生产环境当中的代码会更复杂，如果这样用线程池去调度，结果可想而克。

实际上通常只需要在一个线程当中处理自己的业务逻辑，只有一些耗时的 IO 才需要切换到 IO 线程中去处理，所以好的做法可以参考 UI 对应的调度器，自己通过线程池定义调度器的做法本身没什么问题，但最好只用一个线程，因为多线程除了前面说的线程切换的开销外，还有线程安全的问题。

#### 3.3.4. 线程安全问题

如果大家在协程代码中使用锁之类的并发工具九反而增加了代码的复杂度，对此建议是大家在编写协程代码时尽量避免对外部作用域的可变变量进行引用，尽量使用参数传递而非对全局变量进行引用。

下面是一个错误的例子，大家很容易就能想明白：

```kotlin
suspend fun main(){
    var i = 0
    Executors.newFixedThreadPool(10)
            .asCoroutineDispatcher().use { dispatcher ->
                List(1000000) {
                    GlobalScope.launch(dispatcher) {
                        i++
                    }
                }.forEach {
                    it.join()
                }
            }
    log(i)
}
```

输出的结果：

```
16:59:28:080 [main] 999593
```

### 3.4. suspend main 函数如何调度？

suspend main 会启动一个协程，实例中的协程都是它的子协程，可是这个最外面的协程到底是怎么来的呢？

```kotlin
suspend fun main() {
    log(1)
    GlobalScope.launch {
        log(2)
    }.join()
    log(3)
}
```

它等价于下面的写法：

```kotlin
fun main() {
    runSuspend {
        log(1)
        GlobalScope.launch {
            log(2)
        }.join()
        log(3)
    }
}
```

runSuspend 是 Kotlin 标准库的一个方法，注意它不是 kotlinx.coroutines 当中的，它实际上属于更底层的 API 了。

```kotlin
internal fun runSuspend(block: suspend () -> Unit) {
    val run = RunSuspend()
    block.startCoroutine(run)
    run.await()
}
```

而这里面的 RunSuspend 则是 Continuation 的实现：

```kotlin
private class RunSuspend : Continuation<Unit> {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    var result: Result<Unit>? = null

    override fun resumeWith(result: Result<Unit>) = synchronized(this) {
        this.result = result
        (this as Object).notifyAll()
    }

    fun await() = synchronized(this) {
        while (true) {
            when (val result = this.result) {
                null -> (this as Object).wait()
                else -> {
                    result.getOrThrow() // throw up failure
                    return
                }
            }
        }
    }
}
```

它的上下文是空的，因此 suspend main 启动的协程并不会有任何调度行为。

通过这个例子可以知道，实际上启动一个协程只需要有一个 lamdba 表达式就可以了。上述的代码在标准库当中被修饰为 internal，因此无法直接使用它们。不过可以把 RunSuspend.kt 当中的内容复制到自己的工程当中，这样就可以直接使用啦，其中的 `var result: Result<Unit>? = null` 可能会报错，改成 `private var reseult: Result<Unit>? = null` 就可以了。

## 4. 异常处理

定义回调接口实现异步数据的请求：

```kotlin
typealias Callback = (User) -> Unit

suspend fun getUserCoroutine() = suspendCoroutine<User> {
    continuation ->
    getUser {
        continuation.resume(it)
    }
}
```

将结果交给按钮点击事件或者其他事件去出发这个异步请求：

```kotlin
getUserBtn.setOnClickListener {
    GlobalScope.launch(Dispatchers.Main) {
        userNameView.text = getUserCoroutine().name
    }
}
```

既然是请求，总会有失败的情形。

### 4.1. 添加异常处理逻辑

首先加上异常回调接口函数：

```kotlin
interface Callback<T> {
    fun onSuccess(value: T)

    fun onError(t: Throwable)
}
```

改造一下 getUserCoroutine：

```kotlin
suspend fun getUserCoroutine() = suspendCoroutine<User> { continuation ->
    getUser(object : Callback<User> {
        override fun onSuccess(value: User) {
            continuation.resume(value)
        }

        override fun onError(t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
}
```

就是完全把 Callback 转换成了一个 Continuation，在调用的时候只需要：

```kotlin
GlobalScope.launch(Dispatchers.Main) {
    try {
        userNameView.text = getUserCoroutine().name
    } catch (e: Exception) {
        userNameView.text = "Get User Error: $e"
    }
}
```

一个异步的请求异常，只需要在代码中捕获就可以了，这样做的好处就是，请求的全流程异常都可以在一个 try... catch... 当中捕获。

### 4.2. 全部异常处理

线程也好、RxJava 也好，都有全局处理异常的方式，例如：

```kotlin
fun main() {
    Thread.setDefaultUncaughtExceptionHandler {t: Thread, e: Throwable ->
        //handle exception here
        println("Thread '${t.name}' throws an exception with message '${e.message}'")
    }

    throw ArithmeticException("Hey!")
}
```

可以为线程设置全局的异常捕获，当然也可以为 RxJava 来设置全局异常捕获：

```kotlin
RxJavaPlugins.setErrorHandler(e -> {
        //handle exception here
        println("Throws an exception with message '${e.message}'")
});
```

协程显然也可以做到这一点。类似于通过 Thread.setUncaughtExceptionHandler 为线程设置一个异常捕获器，也可以为每一个协程单独设置 CoroutineExceptionHandler，这样协程内部未捕获的异常就可以通过它来捕获：

```kotlin
private suspend fun main(){
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        log("Throws an exception with message: ${throwable.message}")
    }

    log(1)
    GlobalScope.launch(exceptionHandler) {
        throw ArithmeticException("Hey!")
    }.join()
    log(2)
}
```

运行结果：

```
19:06:35:087 [main] 1
19:06:35:208 [DefaultDispatcher-worker-1 @coroutine#1] Throws an exception with message: Hey!
19:06:35:211 [DefaultDispatcher-worker-1 @coroutine#1] 2
```

CoroutineExceptionHandler 竟然也是一个上下文。

这并不算是一个全局的异常捕获，因为它只能捕获对应协程内未捕获的异常，如果想做到真正的全局捕获，在 Jvm 上可以自己定义一个捕获类实现：

```kotlin
class GlobalCoroutineExceptionHandler: CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        println("Coroutine exception: $exception")
    }
}
```

然后在 classpath 中创建 META-INF/services/kotlinx.coroutines.coroutineExceptionHandler，文件名实际上就是 CoroutineExceptionHandler 的全类名，文件内容就写实现类的全类名：

com.xxx.xxx.xxx.exceptions.GlobalCoroutineExceptionHandler

这样协程中没有被捕获的异常就会最终交给它处理。

```
Jvm 上全局 CoroutineExceptionHandler 的配置，本质上是对 ServiceLoader 的应用，Jvm 上 Dispatchers.Main 的实现也是通过 ServiceLoader 来加载的。
```

需要明确的一点是，通过 async 启动的协程出现未捕获的异常时会忽略 CoroutineExceptionHandler，这与 launch 的设计思路是不同的。

### 4.3. 异常传播

异常传播还涉及到协程作用域的概念，例如启动协程的时候一直都是用的 GlobalScope，意味着这是一个独立的顶级协程作用域，此外还有 coroutineScope{...} 以及 supervisorScope{...}。

* 通过 GlobeScope 启动的协程单独启动一个协程作用域，内部的子协程遵从默认的作用域规则。通过 GlobalScope 启动的协程“自成一派”。
* coroutineScope 是继承外部 Job 的上下文创建作用域，在其内部的取消操作是双向传播的，子协程未捕获的异常也会向上传递给父协程。它更适合一系列对等的协程并发的完成一项工作，任何一个子协程异常退出，那么整体都将退出，简单来说就是“一损俱损”。这也是协程内部再启动子协程的默认作用域。
* supervisorScope 同样继承外部作用域的上下文，但其内部的取消操作是单向传播的，父协程向子协程传播，反过来则不然，这意味着子协程出了异常并不会影响父协程以及其他兄弟协程。它更适合一些单独不相干的任务，任何一个任务出问题，并不会影响其他任务的工作，简单来说就是“自作自受”，例如 UI，点击一个按钮出了异常，其实并不会影响手机状态栏的刷新。需要注意的是，supercisorScope 内部启动的子协程内部再启动子协程，如无明确指出，则遵守默认作用域规则，也即 supervisorScope 只作用域其直接子协程。

```kotlin
suspend fun main() {
    log(1)
    try {
        coroutineScope { //①
            log(2)
            launch { // ②
                log(3)
                launch { // ③ 
                    log(4)
                    delay(100)
                    throw ArithmeticException("Hey!!")
                }
                log(5)
            }
            log(6)
            val job = launch { // ④
                log(7)
                delay(1000)
            }
            try {
                log(8)
                 job.join()
                log("9")
            } catch (e: Exception) {
                log("10. $e")
            }
        }
        log(11)
    } catch (e: Exception) {
        log("12. $e")
    }
    log(13)
}
```

在一个 coroutineScope 当中启动了两个协程 ②④，在 ② 当中启动了一个子协程 ③，作用域直接创建的协程记为 ①。那么 ③ 当中抛出异常会发生什么？输出：

```
11:37:36:208 [main] 1
11:37:36:255 [main] 2
11:37:36:325 [DefaultDispatcher-worker-1] 3
11:37:36:325 [DefaultDispatcher-worker-1] 5
11:37:36:326 [DefaultDispatcher-worker-3] 4
11:37:36:331 [main] 6
11:37:36:336 [DefaultDispatcher-worker-1] 7
11:37:36:336 [main] 8
11:37:36:441 [DefaultDispatcher-worker-1] 10. kotlinx.coroutines.JobCancellationException: ScopeCoroutine is cancelling; job=ScopeCoroutine{Cancelling}@2bc92d2f
11:37:36:445 [DefaultDispatcher-worker-1] 12. java.lang.ArithmeticException: Hey!!
11:37:36:445 [DefaultDispatcher-worker-1] 13
```

注意两个位置，一个是 10，调用 join，收到一个取消异常，在协程当中支持取消的操作的 suspend 方法在取消时会抛出一个 CancellationException，这类似于线程中对 InterruptException 的响应，遇到这种情况表示 join 调用所在的协程已经被取消了，那么这个取消是怎么回事？

原来协程 ③ 抛出了未捕获的异常，进入了异常完成的状态，它与父协程 ② 之间遵循默认的作用域作用，因此 ③ 会通知它的父协程也就是 ② 取消，② 根据作用域规则通知父协程 ① 也就是整个作用域取消，这是一个自下而上的一次传播，这样身处  ① 当中的 job.join 调用就会抛异常，也就是 10 处的结果了。coroutineScope 内部启动的协程就是 “一损俱损”。实际上由于父协程 ① 被取消，协程 ④ 也不能幸免，如果对 ④ 当中的 delay 进行捕获，一样会收获取消异常。

还有一个位置就是 12，这个是对 coroutineScope 整体的一个捕获，如果 coroutineScope 内部以为异常而结束，那么可以对它直接 try... catch... 来捕获这个异常的，这再一次表明协程把异步的异常处理到同步代码逻辑当中。

那么如果把 coroutineScope 换成 supervisorScope，其他不变

```
11:52:48:632 [main] 1
11:52:48:694 [main] 2
11:52:48:875 [main] 6
11:52:48:892 [DefaultDispatcher-worker-1 @coroutine#1] 3
11:52:48:895 [DefaultDispatcher-worker-1 @coroutine#1] 5
11:52:48:900 [DefaultDispatcher-worker-3 @coroutine#3] 4
11:52:48:905 [DefaultDispatcher-worker-2 @coroutine#2] 7
11:52:48:907 [main] 8
Exception in thread "DefaultDispatcher-worker-3 @coroutine#3" java.lang.ArithmeticException: Hey!!
	at com.bennyhuo.coroutines.sample2.exceptions.ScopesKt$main$2$1$1.invokeSuspend(Scopes.kt:17)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(Dispatched.kt:238)
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:594)
	at kotlinx.coroutines.scheduling.CoroutineScheduler.access$runSafely(CoroutineScheduler.kt:60)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:742)
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 9
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 11
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 13
```

可以看到，1-8 的输出其实没有本质区别，顺序上的差异是线程调度的前后造成的，并不会影响协程的语义。差别主要在于 9 与 10、11 与 12 的区别，如果把 scope 换成 supervisorScope，发现 ③ 的异常并没有影响作用域以及作用域内的其他子协程的执行，也就是所说的“自作自受”。

再稍做一些改动，为 ② 和 ③ 增加一个 CoroutineExceptionHandler，就可以证明前面提到的另外一个结论：

首先定义一个 CoroutineExceptionHandler，通过上下文获取一下异常对应的协程的名字：

```kotlin
val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    log("${coroutineContext[CoroutineName]} $throwable")
}
```

接着，为 ② 和 ③ 添加 CoroutineExceptionHandler 和名字：

```kotlin
...
supervisorScope { //①
    log(2)
    launch(exceptionHandler + CoroutineName("②")) { // ②
        log(3)
        launch(exceptionHandler + CoroutineName("③")) { // ③
            log(4)
...

```

再运行这段程序

```
...
07:30:11:519 [DefaultDispatcher-worker-1] CoroutineName(②) java.lang.ArithmeticException: Hey!!
...
```

触发的 CoroutineExceptionHandler 竟然是协程 ② 的，对于 supervisorScope 的子协程（例如 ②）的子协程（例如 ③），如果没有明确指出，它是遵循默认的作用于规则的，也就是 coroutineScope 的规则了，出现未捕获的异常会尝试传递给父协程并尝试取消父协程。

究竟使用什么 Scope，根据实际情况来确定，一些建议：

* 对于没有协程作用域，但需要启动协程的时候，适合用 GlobalScope
* 对于已经有协程作用域的情况（例如通过 GlobalScope 启动的协程体内），直接用协程启动器启动
* 对于明确要求子协程之间相互独立不干扰时，使用 supervisorScope
* 对于通过标准库 API 创建的协程，这样的协程比较底层，没有 Job、作用域等概念的支撑，例如前面提到过 suspend main 就是这种情况，对于这种情况有限考虑通过 coroutineScope 创建作用域；更进一步，大家尽量不要直接使用标准库 API，除非对 Kotlin 的协程机制非常熟悉。

当然，对于可能出异常的情况，大家尽量做好异常处理，不要将问题复杂化。

### 4.4. join 和 await

启动协程除了 launch，其实常用的还有 async、actor 和 produce，其实 actor 和 launch 的行为类似，在未捕获的异常出现以后，会被当做为处理的异常抛出。而 async 和 produce 则主要是用来输出结果的，他们内部的异常只在外部消费他们的结果时抛出。这两组协程的启动器，也可以认为分别是 “消费者” 和 “生产者”，消费者异常立即抛出，生产者只有结果消费时抛出异常。

actor 和 produce 这两个 API 目前处于比较微妙的境地，可能会被废弃或者后续提供替代方案，不建议大家使用。

那么消费结果指的是什么？对于 async 来讲，就是 await，例如：

```kotlin
suspend fun main() {
    val deferred = GlobalScope.async<Int> { 
        throw ArithmeticException()
    }
    try {
        val value = deferred.await()
        log("1. $value")
    } catch (e: Exception) {
        log("2. $e")
    }
}
```

这个从逻辑上很好理解，调用 await 时，期望 deferred 能够提供一个合适的结果，但它因为出异常，没有办法做到这一点，因此只好丢出一个异常了。

```
13:25:14:693 [main] 2. java.lang.ArithmeticException
```

自己实现的 getUserCoroutine 也属于类似的情况，在获取结果时，如果请求出了异常，就只能拿到一个异常，而不是正常的结果。相比之下，join 就有趣的多，它只关心是否执行玩，至于是因为什么完成，它不关心，因此在这里替换成 join：

```kotlin
suspend fun main() {
    val deferred = GlobalScope.async<Int> {
        throw ArithmeticException()
    }
    try {
        deferred.join()
        log(1)
    } catch (e: Exception) {
        log("2. $e")
    }
}
```

就会发现，异常被吞掉了。

```
13:26:15:034 [main] 1
```

如果例子当中用 launch 替换 async，join 出仍然不会有任何异常抛出，它只关心有没有完成，至于怎么完成的它不关心。不同之处在于，launch 中未捕获的异常与 aysnc 的处理方式不同，launch 会直接抛出给父协程，如果没有父协程（顶级作用域中）或者处于 supervisorScope 中父协程不响应，那么就交给上下文中指定的 CoroutineExceptionHandler 处理，如果没有制定，那传给全局的 CoroutineExceptionHandler 等等，而 aysnc 则要等 await 来消费。

不管是哪个启动器，在应用了作用域之后，都会按照作用域的语义进行异常扩散，进而触发相应的取消操作，对于 async 来说就算不调用 await 来获取这个异常，它也会在 coroutineScope 当中触发父协程的取消逻辑。这一点请注意。

### 4.5. 结论

1. **协程内部异常处理流程：**launch 会在内部出现未捕获的异常时尝试触发对父协程的取消，能否取消要看作用域的定义，如果取消成功，那么异常传递给父协程，否则传递给启动时上下文中配置的 CoroutineExceptionHandler 中，如果没有配置，会查找全局（JVM 上）的 CoroutineExceptionHandler 进行处理，如果仍然没有，那么就将异常交给当前线程的 UncaughtExceptionHandler 处理；而 async 则在未捕获的异常出现时同样会尝试取消父协程，但不管是否能够取消成功都不会影响其他后续的异常处理，直到用户主动调用 await 时将异常抛出。
2. **异常在作用域内的传播：**当协程出现异常时，会根据当前作用域触发异常传递，GlobalScope 会创建一个独立的作用域，所谓“自成一派”，而在 coroutineScope 当中协程异常会触发父协程的取消，进而将整个协程作用域取消掉，如果对 coroutineScope 整体进行捕获，也可以捕获到该异常，所谓 “一损俱损”；如果是 supervisorScope，那么子协程的异常不会向上传递，所谓“自作自受”。
3. **join 和 await 的不同：**join 只关心协程是否执行完，await 则关心运行的结果，因此 join 在协程出现异常时也不会抛出该异常，而 await 则会；考虑到作用域的问题，如果协程抛异常，可能会导致父协程的取消，因此调用 join 时尽管不会对协程本身的异常进行抛出，但如果 join 调用所在的协程被取消，那么它会抛出取消异常，这一点需要留意。

## 5. 协程取消篇

### 5.1. 线程的中断

线程有一个被废弃的 stop 方法，这个方法会让线程立即死掉，并且释放它持有的锁，这样会让它正在读写的存储处于一个不安全的状态，因此 stop 被废弃了。如果启动了一个线程并让它执行一些任务，但很快就后悔了，stop 还不让用，那该怎么办？

```kotlin
val thread = thread {
    ...
}
thread.stop() // !!! Deprecated!!!
```

应该想办法让线程内部正在运行的任务跟我们合作把任务停掉，这样线程内部的任务停止之前还有机会清理一些资源，比如关闭流等等。

```kotlin
val thread = thread {
    try {
        Thread.sleep(10000)
    } catch (e: InterruptedException) {
        log("Interrupted, do cleaning stuff.")
    }
}
thread.interrupt()
```

像 sleep 这样的方法调用，文档明确指出它支持 InterruptException，因此当线程被标记为中断状态时，它就会抛出 InterruptedException，那么自然就可以捕获异常并做资源清理了。

所以请注意所谓的协作式的任务终止，协程的取消也就是 cancel 机制的思路也是如此。

### 5.2. 协程类似的例子

来看一些协程取消的例子：

```kotlin
fun main() = runBlocking {
    val job1 = launch { // ①
        log(1)
        delay(1000) // ②
        log(2)
    }
    delay(100)
    log(3)
    job1.cancel() // ③
    log(4)
}
```

这次没有用 suspend main，而是直接用 runBlocking 启动协程，这个方法在 Native 上也存在，都是基于当前线程启动一个类似于 Android 的 Lopper 的死循环，或者叫消息队列，可以不断的发送消息给它进行处理。runBlocking 会启动一个 Job，因此这里也存在默认的作用域。

这段代码 ① 处启动了一个子协程，它内部先输出 1，接着开始 delay，delay 与线程的 sleep 不同，它不会阻塞线程，可以认为它实际上就是触发了一个延时任务，告诉协程调度系统 1000ms 之后再来执行后面的这段代码（也就是 log(2)）；而在这期间，在 ③ 处对刚才启动的协程触发了取消，因此在 ② 处的 delay 还没有回调的时候协程就取消了，因为 delay 可以响应取消，因此 delay 后面的代码就不会再次调度了，不调度的原因也很简单，② 处的 delay 会抛一个 CancellationException：

```kotlin
...
log(1)
try {
    delay(1000)
} catch (e: Exception) {
    log("cancelled. $e")
}
log(2)
...
```

那么输出的结果就不一样了：

```
06:54:56:361 [main] 1
06:54:56:408 [main] 3
06:54:56:411 [main] 4
06:54:56:413 [main] cancelled. kotlinx.coroutines.JobCancellationException: Job was cancelled; job=StandaloneCoroutine{Cancelling}@e73f9ac
06:54:56:413 [main] 2
```

与线程的中断逻辑非常的类似。

### 5.3. 例子

给之前的例子加上取消逻辑。之前是这样的：

```kotlin
suspend fun getUserCoroutine() = suspendCoroutine<User> { continuation ->
    getUser(object : Callback<User> {
        override fun onSuccess(value: User) {
            continuation.resume(value)
        }

        override fun onError(t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
}
```

加取消逻辑，那需要 getUser 回调版本支持取消

```kotlin
fun getUser(callback: Callback<User>) {
    val call = OkHttpClient().newCall(
            Request.Builder()
                    .get().url("https://api.github.com/users/bennyhuo")
                    .build())

    call.enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback.onError(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body()?.let {
                try {
                    callback.onSuccess(User.from(it.string()))
                } catch (e: Exception) {
                    callback.onError(e) // 这里可能是解析异常
                }
            }?: callback.onError(NullPointerException("ResponseBody is null."))
        }
    })
}
```

发了个网络请求给 Github，让它把一个叫 bennyhuo 的用户信息返回来，OkHttp 的这个 Call 是支持 cancel 的，取消后，网络请求过程中如果读取到这个取消的状态，就会把请求给停止掉。既然这样，干脆直接改造 getUser 好了，这样还能省掉 Callback 回调过程：

```kotlin
suspend fun getUserCoroutine() = suspendCancellableCoroutine<User> { continuation ->
    val call = OkHttpClient().newCall(...)

    continuation.invokeOnCancellation { // ①
        log("invokeOnCancellation: cancel the request.")
        call.cancel()
    }

    call.enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            log("onFailure: $e")
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            log("onResponse: ${response.code()}")
            response.body()?.let {
                try {
                    continuation.resume(User.from(it.string()))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            } ?: continuation.resumeWithException(NullPointerException("ResponseBody is null."))
        }
    })
}
```

这里用到了 suspendCancelableCoroutine，而不是之前的 suspendCoroutine，这就是为了让挂起函数支持协程的取消。该方法将获取到的 Continuation 包装成一个 CancellableContinuation，通过调用它的 invokeOnCancellation 方法可以设置一个取消时间的回调，一旦这个回调被调用，那么意味着 getUserCoroutine 调用所在的协程被取消了，这时候也要相应的做出取消的响应，也就是把 OkHttp 发出去的请求给取消掉。

那么在调用它的时候，如果遇到了取消，会怎么样呢？

```kotlin
val job1 = launch { //①
    log(1)
    val user = getUserCoroutine()
    log(user)
    log(2)
}
delay(10)
log(3)
job1.cancel()
log(4)
```

注意启动 ① 之后仅仅延迟了 10ms 就取消了它，网络请求的速度一般来讲还不会这么快，因此取消的时候大概率 getUserCoroutine 被挂起了，因此结果大概率是：

```
07:31:30:751 [main] 1
07:31:31:120 [main] 3
07:31:31:124 [main] invokeOnCancellation: cancel the request.
07:31:31:129 [main] 4
07:31:31:131 [OkHttp https://api.github.com/...] onFailure: java.io.IOException: Canceled
```

取消的回调被调用了，OkHttp 在收到取消指令之后，也确实停止了网络请求，并且回调了一个 IO 异常，这时候协程已经被取消了，在处于取消状态的协程上调用 Continuation.resume、Continuation.sumeWithException 或者 Continuation.resumeWithException(e) 不会有任何副作用。

### 5.4. Retrofit 的协程扩展

#### 5.4.1. Jake Wharton 的 Adapter 存在的问题

Jake Wharton 为 Retrofit 写的协程 Adapter，

```xml
implementation 'com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2'
```

它确实可以完成网络请求，但怎么取消？使用代码如下：

```kotlin
interface GitHubServiceApi {
    @GET("users/{login}")
    fun getUserCoroutine(@Path("login") login: String): Deferred<User>
}
```

定义好接口，创建 Retrofit 实例的时候传入对应的 Adapter：

```kotlin
val gitHubServiceApi by lazy {
    val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory()) // 这里添加 Adapter
            .build()

    retrofit.create(GitHubServiceApi::class.java)
}
```

用的时候就这样：

```kotlin
val deferred = gitHubServiceApi.getUserCoroutine("bennyhuo")
try {
    showUser(deferred.await())
} catch (e: Exception) {
    showError(e)
}
```

如果要取消，可以直接调用 deferred.cancel()。例如：

```kotlin
log("1")
val deferred = gitHubServiceApi.getUserCoroutine("bennyhuo")
log("2")
withContext(Dispatchers.IO){
    deferred.cancel()
}
try {
    showUser(deferred.await())
} catch (e: Exception) {
    showError(e)
}
```

运行结果如下：

```kotlin
12:59:54:185 [DefaultDispatcher-worker-1] 1
12:59:54:587 [DefaultDispatcher-worker-1] 2
kotlinx.coroutines.JobCancellationException: Job was cancelled; job=CompletableDeferredImpl{Cancelled}@36699211
```

这种情况下，其实网络请求确实是被取消的，这一点可以看下源码的处理：

```kotlin
...
override fun adapt(call: Call<T>): Deferred<T> {
      val deferred = CompletableDeferred<T>()

      deferred.invokeOnCompletion { // ①
        if (deferred.isCancelled) {
          call.cancel()
        }
      }

      call.enqueue(object : Callback<T> {
        ...
      }     
}
...
```

注意 ① 处，invokeOnCompletion 在协程进入完成状态时触发，包括异常和正常完成，那么在这时候如果发现它的状态是已经取消的，那么结果就直接调用 Call 的取消即可。

但是有一个 Case，会有问题

```kotlin
val job = GlobalScope.launch {
    log("1")
    val deferred = gitHubServiceApi.getUserCoroutine("bennyhuo")
    log("2")
    deferred.invokeOnCompletion {
        log("invokeOnCompletion, $it, ${deferred.isCancelled}")
    }
    try {
        showUser(deferred.await())
    } catch (e: Exception) {
        showError(e)
    }
    log(3)
}
delay(10)
job.cancelAndJoin()
```

启动一个协程，在其中执行网络请求，那么正常来说，这时候 getUserCoroutine 返回的 Deferred 可以当做一个子协程，它应当遵循默认的作用域规则，在父作用域取消时被取消掉，但现实却并不是这样：

```kotlin
13:06:54:332 [DefaultDispatcher-worker-1] 1
13:06:54:829 [DefaultDispatcher-worker-1] 2
kotlinx.coroutines.JobCancellationException: Job was cancelled; job=StandaloneCoroutine{Cancelling}@19aea38c
13:06:54:846 [DefaultDispatcher-worker-1] 3
13:06:56:937 [OkHttp https://api.github.com/...] invokeOnCompletion, null, false
```

在调用 deferred.await() 的时候抛了个取消异常，这主要是因为 await() 所在的协程已经被用 cancelAndJoin() 取消，但从随后 invokeOnCompletion 的回调结果来看，getUserCoroutine 返回的 Deferred 并没有被取消，再仔细一看，时间上这个回调比前面的操作晚了 2s，那必然是网络请求返回之后才回调的。

问题在哪里？在 CoroutineCallAdapterFactory 的实现中，为了实现异步转换，手动创建了一个 CompletableDeferred：

```kotlin
override fun adapt(call: Call<T>): Deferred<T> {
  val deferred = CompletableDeferred<T>() // ①
  ...
}
```

这个 CompletableDeferred 本身就是一个 Job 的实现，它的构造可接受一个 Job 实例做为它的父协程，那么问题来了，这里并没有钙素它父协程究竟是谁，因此也就谈不上作用域的事儿了，这好像用 GlobalScope.launch 启动了一个协程一样。如果大家在 Android 当中使用 MainScope，那么同样因为前面说到的这个原因，导致 CompletableDeferred 没有办法被取消。

作用域主要有 GlobalScope、coroutineScope、supervisorScope，对于取消，除了 supervisorScope 比较特别是单向取消，即父协程取消后子协程都取消，Android 中 MainScope 就是一个调度到 UI 线程的 supervisorScope；coroutineScope 的逻辑则是父子相互取消的逻辑；而 GlobalScope 会启动一个全新的作用域，与它外部隔离，内部遵循默认的协程作用域规则。

直接解决比较困难，因为 Completabledeferred 构造所处的调用环境不是 suspend 函数，因而也没有办法拿到父协程。

#### 5.4.2. 如何正确的将回调转换为协程

既然 adapt 方法不是 suspend 方法，那么是不是应该在其他位置创建协程呢？

将一个回调转换为协程调用的方法：

```
suspend fun getUserCoroutine() = suspendCancellableCoroutine<User> { continuation ->
    ...
}
```

suspendCancelableCoroutine 跟 suspendCoroutine 一样，都是要获取当前协程的 Continuation 实例，实际上就相当于要继承当前协程的上下文，因此只需要在真正需要切换协程的时候再去做这个转换即可：

```kotlin
public suspend fun <T : Any> Call<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T?>) {
                continuation.resumeWith(runCatching { // ①
                    if (response.isSuccessful) {
                        response.body()
                            ?: throw NullPointerException("Response body is null: $response")
                    } else {
                        throw HttpException(response)
                    }
                })
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (continuation.isCancelled) return // ②
                continuation.resumeWithException(t)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {  // ③
                //Ignore cancel exception 
            }
        }
    }
}
```

与 getUserCoroutine 的写法几乎一样，不过有几处细节值得关注：

* ① 处 runCatching 可以将一段代码的运行结果或者抛出的异常封装到一个 Result 类型当中，Kotlin 1.3 开始新增了 Continuation.resumeWith(Result) 这个方法，这个点比起之前的写法更具 Kotlin 风格。
* ② 处在异常抛出时，判断了是否已经被取消。实际上如果网络请求被取消，这个回调确实会被调用到，那么由于取消的操作是协程的由 Continuation 的取消发起的，因此这时候没必要再调用 continuation.resumeWithException(t) 来将异常再抛回来了。尽管这时候继续调用 continuation.resumeWithException(t) 也没有任何逻辑上的副作用，但性能上多少还是会有一些开销。
* ③ 处，尽管 Call.cancel 的调用比较安全，但网络环境和状态难免情况复杂，因此对异常进行捕获会让这段代码更加健壮。如果 cancel 抛异常而没有捕获的话，那么等同于协程体内部抛出异常，具体如何传播看所在作用域的相关定义了。

## 6. 协程挂起

### 6.1. delay

最常见的模拟各种延时用的就是 Thread.sleep 了，而在协程里面，对应的就是 delay。sleep 让线程进入休眠状态，直到指定时间之后某种信号或者条件到达，线程就尝试恢复执行，而 delay 会让协程挂起，这个过程并不会阻塞 CPU，甚至可以说从硬件使用效率上来讲是“什么都不耽误”，从这个意义上讲 delay 也可以是让协程休眠的一种很好的手段。

delay 的源码其实很简单：

```kotlin
public suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return // don't delay
    return suspendCancellableCoroutine sc@ { cont: CancellableContinuation<Unit> ->
        cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
    }
}
```

cont.context.delay.scheduleResumeAfterDelay 这个操作，可以类比 JavaScript 的 setTimeout，Android 的 handler.postDelay，本质上就是设置了一个延时回调，时间一到就调用 cont 的 resume 系列方法让协程继续执行。

剩下的最关键的就是 suspendCancelableCoroutine 了，前面用它实现了回调到协程的各种转换 -- delay 也是基于它实现的，类似的还有 join、await 等等。

### 6.2. suspendCancellableCoroutine

```kotlin
private suspend fun joinSuspend() = suspendCancellableCoroutine<Unit> { cont ->
    cont.disposeOnCancellation(invokeOnCompletion(handler = ResumeOnCompletion(this, cont).asHandler))
}
```

Job.join() 这个方法会首先检查调用者 Job 的状态是否已经完成，如果是，就直接返回并继续执行后面的代码而不再挂起，否则就会走到这个 joinSuspend 的分支当中。这里只是注册了一个完成时的回调，那么 suspendCancellableCoroutine 内部究竟做了什么呢？

```kotlin
public suspend inline fun <T> suspendCancellableCoroutine(
    crossinline block: (CancellableContinuation<T>) -> Unit
): T =
    suspendCoroutineUninterceptedOrReturn { uCont ->
        val cancellable = CancellableContinuationImpl(uCont.intercepted(), resumeMode = MODE_CANCELLABLE)
        block(cancellable)
        cancellable.getResult() // 这里的类型是 Any?
    }
```

suspendCoroutineUninterceptedOrReturn 这个方法调用的源码是看不到的，因为它根本没有源码：它的逻辑就是拿到 Continuation 实例。不过这样说起来还是很抽象，因为有一处非常的可疑：suspendCoroutineUninterceptedOrReturn 的返回值类型是 T，而传入的 lambda 的返回值类型是 Any？，也就是 cancelable.getResult() 的类型是 Any？这是为什么？

suspend 函数的签名，以 await 为例，大致相当于：

```kotlin
fun await(continuation: Continuation<User>): Any {
    ...
}
```

suspend 一方面为这个方法添加了一个 Continuation 的参数，另一方面，原先的返回值类型 User 成了 Continuation 的泛型实参，而真正的返回值类型竟然是 Any。当然，这里因为定义的逻辑返回值类型 User 是不可控的，因此真实的返回值类型也用了 Any 来示意，如果泛型实参是个可空的类型，那么真实的返回值类型也就是 Any 了，这正与前面提到的 cancelable.getResult() 返回的这个 Any 相对应。

如果去查 await 的源码，同样会看到这个 getResult() 的调用。

简单来说就是，对于 suspend 函数，不是一定要挂起的，可以在需要的时候挂起，也就是要等待的协程还没有执行完的时候，等待协程执行完再继续执行；而如果在开始 join 或者 await 或者其他 suspend 函数，如果目标协程已经完成，那么就没必要等了，直接拿着结果走人即可。那么这个神奇的逻辑就在于 cancellable.getResult() 究竟返回了什么？

```kotlin
internal fun getResult(): Any? {
    ...
    if (trySuspend()) return COROUTINE_SUSPENDED // ① 触发挂起逻辑
    ...
    if (state is CompletedExceptionally)  // ② 异常立即抛出
        throw recoverStackTrace(state.cause, this) 
    return getSuccessfulResult(state) // ③ 正常结果立即返回
}
```

这段代码 ① 处就是挂起逻辑了，表示这时候目标协程还没有执行完，需要等待结果，②③ 是协程已经执行完可以直接拿到异常和正常结果的两种情况。②③ 好理解，关键是 ①，它要挂起，这返回的是个什么东西？

```kotlin
public val COROUTINE_SUSPENDED: Any get() = CoroutineSingletons.COROUTINE_SUSPENDED

internal enum class CoroutineSingletons { COROUTINE_SUSPENDED, UNDECIDED, RESUMED }
```

这是 1.3 的实现，1.3 以前的实现更有趣，就是一个白板 Any。其实是什么不重要，关键是这个东西是一个单例，任何时候协程见到它就知道自己该挂起了。

### 6.3. 深入挂起操作

```kotlin
suspend fun hello() = suspendCoroutineUninterceptedOrReturn<Int>{
    continuation ->
    log(1)
    thread {
        Thread.sleep(1000)
        log(2)
        continuation.resume(1024)
    }
    log(3)
    COROUTINE_SUSPENDED
}
```

这么一个 suspend 函数，在 suspendCoroutineUninterceptedOrReturn 当中直接返回了这个传说中的白板 COROUTINE_SUSPENDED，正常来说应该是在一个协程当中调用这个方法，写一段 Java 代码去调用这个方法，结果会如何呢？

```kotlin
public class CallCoroutine {
    public static void main(String... args) {
        Object value = SuspendTestKt.hello(new Continuation<Integer>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NotNull Object o) { // ①
                if(o instanceof Integer){
                    handleResult(o);
                } else {
                    Throwable throwable = (Throwable) o;
                    throwable.printStackTrace();
                }
            }
        });

        if(value == IntrinsicsKt.getCOROUTINE_SUSPENDED()){ // ②
            LogKt.log("Suspended.");
        } else {
            handleResult(value);
        }
    }

    public static void handleResult(Object o){
        LogKt.log("The result is " + o);
    }
}
```

这段代码看上去比较奇怪，可能会让人困惑的有两处：

① 处，在 Kotlin 当中看到的 resumeWith 的参数类型是 Result，怎么这儿成了 Object 了？因为 Result 是内联类，编译时会用它唯一的成员替换掉它，因此就替换成了 Object（在 Kotlin 里面是 Any?）

 ②处 IntrinsicsKt.getCOROUTINE_SUSPENDED() 就是 Kotlin 的 COROUTINE_SUSPENDED

运行结果如下所示：

```
07:52:55:288 [main] 1
07:52:55:293 [main] 3
07:52:55:296 [main] Suspended.
07:52:56:298 [Thread-0] 2
07:52:56:306 [Thread-0] The result is 1024
```

其实这段 Java 代码的调用方式与 Kotlin 下面的调用已经很接近了：

```kotlin
suspend fun main() {
    log(hello())
}
```

只不过在 Kotlin 当中还是不太容易拿到 hello 在挂起时的真正返回值，其他的返回结果完全相同。

```
12:44:08:290 [main] 1
12:44:08:292 [main] 3
12:44:09:296 [Thread-0] 2
12:44:09:296 [Thread-0] 1024
```

### 6.4. 深入理解协程的状态转移

```kotlin
suspend fun returnSuspended() = suspendCoroutineUninterceptedOrReturn<String>{
    continuation ->
    thread {
        Thread.sleep(1000)
        continuation.resume("Return suspended.")
    }
    COROUTINE_SUSPENDED
}

suspend fun returnImmediately() = suspendCoroutineUninterceptedOrReturn<String>{
    log(1)
    "Return immediately."
}
```

首先定义两个挂起函数，第一个会真正挂起，第二个则会直接返回结果，这类似于前面讨论 join 或者 await 的两条路径。再用 Kotlin 给出一个调用它们的例子：

```kotlin
suspend fun main() {
    log(1)
    log(returnSuspended())
    log(2)
    delay(1000)
    log(3)
    log(returnImmediately())
    log(4)
}
```

运行结果如下：

```kotlin
08:09:37:090 [main] 1
08:09:38:096 [Thread-0] Return suspended.
08:09:38:096 [Thread-0] 2
08:09:39:141 [kotlinx.coroutines.DefaultExecutor] 3
08:09:39:141 [kotlinx.coroutines.DefaultExecutor] Return immediately.
08:09:39:141 [kotlinx.coroutines.DefaultExecutor] 4
```

为了揭示这段协程代码的真实面貌，用 Java 来仿写一下这段逻辑：

（注意，下面的代码逻辑上并不能做到十分严谨，不应该出现在生产当中，仅供学习理解协程使用。）

```kotlin
public class ContinuationImpl implements Continuation<Object> {

    private int label = 0;
    private final Continuation<Unit> completion;

    public ContinuationImpl(Continuation<Unit> completion) {
        this.completion = completion;
    }

    @Override
    public CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
    }

    @Override
    public void resumeWith(@NotNull Object o) {
        try {
            Object result = o;
            switch (label) {
                case 0: {
                    LogKt.log(1);
                    result = SuspendFunctionsKt.returnSuspended( this);
                    label++;
                    if (isSuspended(result)) return;
                }
                case 1: {
                    LogKt.log(result);
                    LogKt.log(2);
                    result = DelayKt.delay(1000, this);
                    label++;
                    if (isSuspended(result)) return;
                }
                case 2: {
                    LogKt.log(3);
                    result = SuspendFunctionsKt.returnImmediately( this);
                    label++;
                    if (isSuspended(result)) return;
                }
                case 3:{
                    LogKt.log(result);
                    LogKt.log(4);
                }
            }
            completion.resumeWith(Unit.INSTANCE);
        } catch (Exception e) {
            completion.resumeWith(e);
        }
    }

    private boolean isSuspended(Object result) {
        return result == IntrinsicsKt.getCOROUTINE_SUSPENDED();
    }
}
```

定义了一个 Java 类 ContinuationImpl，它就是一个 Continuation 的实现。

在 Kotlin 的标准库当中找到一个名叫 ContinuationImpl 的类，只不过，它的 resumeWith 最终调用到了 invokeSuspend，而这个 invokeSuspend 实际上就是协程体，通常也就是一个 Lambda 表达式 -- 通过 launch 启动协程，传入的那个 Lambda 表达式，实际上会被编译成一个 SuspendLambda 的子类，而它又是 ContinuationImpl 的子类。

有了这个类还需要准备一个 completion 用来接收结果，这个类仿照标准库的 RunSuspend 类实现，suspend main 的实现就是基于这个类：

```kotlin
public class RunSuspend implements Continuation<Unit> {

    private Object result;

    @Override
    public CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
    }

    @Override
    public void resumeWith(@NotNull Object result) {
        synchronized (this){
            this.result = result;
            notifyAll(); // 协程已经结束，通知下面的 wait() 方法停止阻塞
        }
    }

    public void await() throws Throwable {
        synchronized (this){
            while (true){
                Object result = this.result;
                if(result == null) wait(); // 调用了 Object.wait()，阻塞当前线程，在 notify 或者 notifyAll 调用时返回
                else if(result instanceof Throwable){
                    throw (Throwable) result;
                } else return;
            }
        }
    }
}
```

这段代码的关键点在于 await() 方法，它在其中起了一个死循环，如果 result 是 null，那么当前线程会被立即阻塞，直到结果出现。具体的使用方法如下：

```kotlin
...
    public static void main(String... args) throws Throwable {
        RunSuspend runSuspend = new RunSuspend();
        ContinuationImpl table = new ContinuationImpl(runSuspend);
        table.resumeWith(Unit.INSTANCE);
        runSuspend.await();
    }
...
```

这写法简直就是 suspend main 的真实面貌了。

作为 completion 传入的 RunSuspend 实例的 resumeWith 实际上是在 ContinuationImpl 的 resumeWith 的最后才会被调用，因此它的 await() 一旦进入阻塞态，直到 ContinuationImpl 的整体状态流转完毕才会停止阻塞，此时进程也就运行完毕正常退出了。

于是这段代码的运行结果如下：

```kotlin
08:36:51:305 [main] 1
08:36:52:315 [Thread-0] Return suspended.
08:36:52:315 [Thread-0] 2
08:36:53:362 [kotlinx.coroutines.DefaultExecutor] 3
08:36:53:362 [kotlinx.coroutines.DefaultExecutor] Return immediately.
08:36:53:362 [kotlinx.coroutines.DefaultExecutor] 4
```

这段普通的 Java 代码与前面的 Kotlin 协程调用完全一样。这样对协程的本质有了进一步的认识：

* 协程的挂起函数本质上就是一个回调，回调类型就是 Continuation
* 协程体的执行就是一个状态机，每一次遇到挂起函数，都是一次状态转移，就像前面例子中的 label 不断的自增来实现状态流转一样

## 7. 序列生成器

### 7.1. Sequence

在 Kotlin 当中，Sequence 概念确切的说是 “懒序列”，产生懒序列的方式可以有多种，下面是一种由基于协程实现的序列生成器。需要注意的是，这个功能内置于 Kotlin 标准库当中，不需要额外添加依赖。

一个斐波那契数列生成的例子：

```kotlin
 val fibonacci = sequence {
    yield(1L) // first Fibonacci number
    var cur = 1L
    var next = 1L
    while (true) {
        yield(next) // next Fibonacci number
        val tmp = cur + next
        cur = next
        next = tmp
    }
}

fibonacci.take(5).forEach(::log)
```

这个 sequence 实际上也是启动了一个协程，yield 则是一个挂起点，每次调用时先将参数保存起来作为生成的序列迭代器的下一个值，之后返回 COROUTINE_SUSPENDED，这样协程就不再继续执行，而是等待下一次 resume 或者 resumeWithException 的调用，而实际上，这下一次的调用就在生成的序列的迭代器的 next() 调用时执行。如此依赖，外部在遍历序列时，每次需要读取新值时，协程内部就会执行到下一次 yield 调用。

程序运行输出的结果如下：

```
10:44:34:071 [main] 1
10:44:34:071 [main] 1
10:44:34:071 [main] 2
10:44:34:071 [main] 3
10:44:34:071 [main] 5
```

除了使用 yield(T) 生成序列的下一个元素以外，还可以用 yieldAll() 来生成多个元素：

```kotlin
val seq = sequence {
    log("yield 1,2,3")
    yieldAll(listOf(1, 2, 3))
    log("yield 4,5,6")
    yieldAll(listOf(4, 5, 6))
    log("yield 7,8,9")
    yieldAll(listOf(7, 8, 9))
}

seq.take(5).forEach(::log)
```

从运行结果可以看到，在读取 4 的时候才会去执行到 yieldAll(listOf(4,5,6))，而由于 7 以后都没有被访问到，yieldAll(listOf(7,8,9)) 并不会被执行，这就是所谓的 “懒”。

```
10:44:34:029 [main] yield 1,2,3
10:44:34:060 [main] 1
10:44:34:060 [main] 2
10:44:34:060 [main] 3
10:44:34:061 [main] yield 4,5,6
10:44:34:061 [main] 4
10:44:34:066 [main] 5
```

### 7.2. 深入序列生成器

yield 和 yieldAll 都是 suspend 函数，既然能做到 “懒”，那么必然在 yield 和 yieldAll 处是挂起的，因此它们的返回值一定是 COROUTINE_SUSPENDED

```kotlin
override suspend fun yield(value: T) {
    nextValue = value
    state = State_Ready
    return suspendCoroutineUninterceptedOrReturn { c ->
        nextStep = c
        COROUTINE_SUSPENDED
    }
}
```

这是 yield 的实现，看到了老朋友 suspendCoroutineUninterceptedOrReturn，还看到了 COROUTINE_SUSPENDED，那么挂起的问题就很好理解了。而 yieldAll 是如出一辙：

```kotlin
override suspend fun yieldAll(iterator: Iterator<T>) {
    if (!iterator.hasNext()) return
    nextIterator = iterator
    state = State_ManyReady
    return suspendCoroutineUninterceptedOrReturn { c ->
        nextStep = c
        COROUTINE_SUSPENDED
    }
}
```

唯一的不同在于 state 的值，一个流转到了 State_Ready，一个是 State_ManyReady。

既然有了挂起，那么什么时候执行 resume? 在迭代序列的时候呢，也就是序列迭代器的 next() 的时候，那么这事儿就好办了，找下序列的迭代器实现即可，这个类型也很容易找到，显然 yield 就是它的方法，看一下 next 方法的实现：

```kotlin
override fun next(): T {
    when (state) {
        State_NotReady, State_ManyNotReady -> return nextNotReady() // ①
        State_ManyReady -> { // ②
            state = State_ManyNotReady
            return nextIterator!!.next()
        }
        State_Ready -> { // ③
            state = State_NotReady
            val result = nextValue as T
            nextValue = null
            return result
        }
        else -> throw exceptionalState()
    }
}
```

来一次看下这三个条件：

* ① 是下一个元素还没有准备好的情况，调用 nextNotReady 会首先调用 hasNext 检查是否有下一个元素，检查的过程其实就是调用 Continuation.resume，如果有元素，就会再次调用 next，否则就抛异常
* ② 表示调用了 yieldAll，一下子传入了很多元素，目前还没有读取完，因此需要继续从传入的这个元素集合当中去迭代
* ③ 表示调用了一次 yield，而这个元素的值就存在 nextValue 当中

hasNext 的实现也不是很复杂：

```kotlin
override fun hasNext(): Boolean {
    while (true) {
        when (state) {
            State_NotReady -> {} // ①
            State_ManyNotReady -> // ②
                if (nextIterator!!.hasNext()) {
                    state = State_ManyReady
                    return true
                } else {
                    nextIterator = null
                }
            State_Done -> return false // ③
            State_Ready, State_ManyReady -> return true // ④
            else -> throw exceptionalState()
        }

        state = State_Failed
        val step = nextStep!!
        nextStep = null
        step.resume(Unit)
    }
}
```

在通过 next 读取完一个元素之后，如果已经传入的元素已经没有剩余，状态会转为 State_NotReady，下一次取元素的时候就会在 next 中触发到 hasNext 的调用，① 处什么都没有干，因此会直接落到后面的 step.resume()，这样就会继续执行序列生成器的代码，直到遇到 yield 或者 yieldAll。

序列生成器很好的利用了协程的状态机特性，将序列生成的过程从形式上整合到了一起，让程序更加紧凑，表现力更强。本节讨论的序列，某种意义上更像是生产 - 消费者模型中的生产者，而迭代序列的一方则像是消费者。

协程的回调特性可以在实践当中很好的替代传统回调的写法，同时它的状态机特性也可以让曾经的状态机实现获得新的写法，除了序列之外，也许还会有更多有趣的使用场景等待去发掘。

## 8. Android

### 8.1. 配置依赖

如果在 Android 上做开发，需要引入

```groovy
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutine_version'
```

这个框架里面包含了 Android 专属的 Dispatcher，可以通过 Dispatchers.Main 来拿到这个实例，也包含了 MainScope，用于与 Android 作用域相结合。

简单来说：kotlinx-coroutines-android 这个框架是必选项，主要提供了专属调度器。

### 8.2. UI 生命周期作用域

Andrid 开发经常想到的一点就是让发出去的请求能够在当前 UI 或者 Activity 退出或者销毁的时候能够自动取消，在用 RxJava 的时候也有过各种各样的方案来解决这个问题。

#### 8.2.1. 使用 MainScope

协程又一个很天然的特性能够支持这一点，那就是作用域，官方也提供了 MainScope 这个函数，它的使用方法：

```kotlin
val mainScope = MainScope()
launchButton.setOnClickListener {
    mainScope.launch {
        log(1)
        textView.text = async(Dispatchers.IO) {
            log(2)
            delay(1000)
            log(3)
            "Hello1111"
        }.await()
        log(4)
    }
}
```

它其实与其他的 CoroutineScope 用什么没什么不一样的地方，通过同一个叫 mainScope 的实例启动的协程，都会遵循它的作用域定义，那么 MainScope 的定义是怎样的呢？

```kotlin
public fun MainScope(): CoroutineScope = ContextScope(SupervisorJob() + Dispatchers.Main)
```

运来就是 SupervisorJob 整合了 Dispatchers.Main 而已，它的异常传播是自上而下的，这一点与 supervisorScope 的行为一致，此外，作用域内的调度是基于 Android 主线程的调度器的，因此作用域内除非明确声明调度器，协程体都调度在主线程执行。因此上述示例的运行结果如下：

```kotlin
2019-04-29 06:51:00.657 D: [main] 1
2019-04-29 06:51:00.659 D: [DefaultDispatcher-worker-1] 2
2019-04-29 06:51:01.662 D: [DefaultDispatcher-worker-2] 3
2019-04-29 06:51:01.664 D: [main] 4
```

如果在触发前面的操作之后立即在其他位置触发作用域的取消，那么该作用域内的协程将不再继续执行：

```kotlin
val mainScope = MainScope()

launchButton.setOnClickListener {
    mainScope.launch {
        ...
    }
}

cancelButton.setOnClickListener {
    mainScope.cancel()
    log("MainScope is cancelled.")
}
```

如果在触发前面的操作之后立即在其他位置触发作用域的取消，那么该作用域内的协程将不再继续执行：

```kotlin
val mainScope = MainScope()

launchButton.setOnClickListener {
    mainScope.launch {
        ...
    }
}

cancelButton.setOnClickListener {
    mainScope.cancel()
    log("MainScope is cancelled.")
}
```

如果快速依次点击上面的两个按钮，结果：

```kotlin
2019-04-29 07:12:20.625 D: [main] 1
2019-04-29 07:12:20.629 D: [DefaultDispatcher-worker-2] 2
2019-04-29 07:12:21.046 D: [main] MainScope is cancelled.
```

#### 8.2.2. 构造带有作用域的抽象 Activity

尽管 MainScope 可以很方便的控制所有它范围内的协程的取消，以及能够无缝将异步任务切回主线程，这都是想要的特性，不过写法上还是不够美观。

官方推荐定义一个抽象的 Activity，例如：

```kotlin
abstract class ScopedActivity: Activity(), CoroutineScope by MainScope(){
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
```

这样在 Activity 退出的时候，对应的作用域就会被取消，所有在该 Activity 中发起的请求都会被取消掉。使用时，只需要继承这个抽象类即可：

```kotlin
class CoroutineActivity : ScopedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        launchButton.setOnClickListener {
            launch { // 直接调用 ScopedActivity 也就是 MainScope 的方法
                ...
            }
        }
    }
    
    suspend fun anotherOps() = coroutineScope {
        ...
    }
}
```

除了在当前 Activity 内部获得 MainScope 的能力外，还可以将这个 Scope 示例传递给其他需要的模块，例如 Presenter 通常也需要与 Activity 保持同样的生命周期，因此必要时也可以将该作用域传递过去：

```kotlin
class CoroutinePresenter(private val scope: CoroutineScope): CoroutineScope by scope{
    fun getUserData(){
        launch { ... }
    }
}
```

多数情况下，Presenter 的方法也会被 Activity 直接调用，因此也可以将 Presenter 的方法声明成 suspend 方法，然后用 coroutineScope 嵌套作用域，这样 MainScope 被取消后，嵌套的子作用域一样也会被取消，进而达到取消全部子协程的目的：

```kotlin
class CoroutinePresenter {
    suspend fun getUserData() = coroutineScope {
        launch { ... }
    }
}
```

#### 8.2.3. 更友好地为 Activity 提供作用域

抽象类很多时候会打破继承体系，这对于开发体验的伤害还是很大的，因此可以考虑构造一个接口，只要 Activity 实现这个接口就可以拥有作用域以及自动取消的能力呢？

首先定义一个接口：

```kotlin
interface ScopedActivity {
    val scope: CoroutineScope
}
```

问题是这个 scope 成员要怎么实现呢？留给接口实现方的话显然不是很理想，自己实现吧，又碍于自己是个接口，因此只能这样处理：

```kotlin
interface MainScoped {
    companion object {
        internal val scopeMap = IdentityHashMap<MainScoped, MainScope>()
    }
    val mainScope: CoroutineScope
        get() = scopeMap[this as Activity]!!
}
```

接下来的事情就是在合适的实际去创建和取消对应的作用域了，接着定义两个方法：

```kotlin
interface MainScoped {
    ...
    fun createScope(){
        //或者改为 lazy 实现，即用到时再创建
        val activity = this as Activity
        scopeMap[activity] ?: MainScope().also { scopeMap[activity] = it }
    }

    fun destroyScope(){
        scopeMap.remove(this as Activity)?.cancel()
    }
}
```

因此需要 Activity 去实现这个接口，因此直接强转即可，当然如果考虑健壮性，可以做一些异常处理，这里仅提供核心实现。

接下来就是考虑在哪儿完成创建和取消呢？显然这件事儿用 Application.ActivityLifecycleCallbacks 最合适不过了：

```kotlin
class ActivityLifecycleCallbackImpl: Application.ActivityLifecycleCallbacks {
    ...
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? MainScoped)?.createScope()
    }

    override fun onActivityDestroyed(activity: Activity) {
        (activity as? MainScoped)?.destroyScope()
    }
}
```

剩下的就是在 Application 里面注册一下这个监听了。

看下如何使用：

```kotlin
class CoroutineActivity : Activity(), MainScoped {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        launchButton.setOnClickListener {            
            scope.launch {
                ...
            }
        }
    }
}
```

也可以增加一些有用的方法来简化这个操作：

```kotlin
interface MainScoped {
    ...
    fun <T> withScope(block: CoroutineScope.() -> T) = with(scope, block)
}
```

这样在 Activity 当中还可以这样写：

```kotlin
withScope {
    launch { ... }
}   
```

注意，示例当中用到了 IdentityHashMap，这表明对于 scope 的读写是非线程安全的，因此不要在其他线程试图去获取它的值，除非引入第三方或者自己实现一个 IdentityConcurrentHashMap，即便如此，从设计上 scope 也不太应该在其他线程访问。

#### 8.2.4. Androidx 的协程支持

Android 官方对于协程的支持也是非常积极的。

KTX 为 Jetpack 的 Lifecycle 相关的组件都提供了已经绑定了生命周期的作用域供直接使用，添加 Lifecycle 相应的基础组件之后，再添加以下组件即可：

```groovy
implementation "androidx.lifecycle:lifecycle-runtime-ktx:$ktx_version"
```

lifecycle-runtime-ktx 提供了 LifecycleCoroutineScope 类以及其获得方式，例如可以直接在 MainActivity 中使用 lifecycleScope 来获取这个实例：

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            lifecycleScope.launch {
                ...// 执行协程体
            }
        }
    }
}
```

 这当然是因为 MainActivity 的父类实现了 LifecycleOwner 这个接口，而 lifecycleScope 则正是它的扩展成员。

如果想要在 ViewModel 当中使用作用域，需要再添加以下依赖：

```groovy
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$ktx_version"
```

使用方法类似：

```kotlin
class MainViewModel : ViewModel() {
    fun fetchData() {
        viewModelScope.launch {
            ... // 执行协程体
        }
    }
}
```

ViewModel 的作用域会在它的 clear 函数调用时取消。

### 8.3. 谨慎使用 GlobalScope

GlobalScope 不会继承外部作用域，因此使用时一定要注意，如果在使用了绑定生命周期的 MainScope 之后，内部再使用 GlobalScope 启动协程，意味着 MainScope 就不会起到应有的作用。

这里需要小心的是如果使用了一些没有依赖作用域的构造器，那么一定要小心。例如 Anko 当中的 onClick 扩展：

```kotlin
fun View.onClick(
        context: CoroutineContext = Dispatchers.Main,
        handler: suspend CoroutineScope.(v: View) -> Unit
) {
    setOnClickListener { v ->
        GlobalScope.launch(context, CoroutineStart.DEFAULT) {
            handler(v)
        }
    }
}
```

也许就是图个方便，毕竟 onClick 写起来可比 setOnClickListener 要少很多字符，同时名称上看也更加有事件机制的味道，但隐藏的风险就是通过 onClick 启动的协程并不会随着 Activity 的销毁而被取消，其中的风险需要自己思考清楚。

当然，Anko 会这么做的根本原因在于 OnClickListener 根本拿不到有生命周期加持的作用域。

另外需要注意的是，Anko 已经停止维护，不再建议使用了。

#### 8.3.2. 协程版 AutoDisposable

当然除了直接使用一个合适的作用域来启动协程之外，还有别的办法来确保协程及时被取消。

RxJava 发个任务，任务还没结束页面就被关闭了，如果任务迟迟不回来，页面就会被泄露；如果任务后面回来了，执行回调更新 UI 的时候也会大概率空指针。

因此大家一定会用到 Uber 的开源框架 AutoDispose。它其实就是利用 View 的 onAttachStateChangeListener，当 View 被拿下的时候，就取消所有之前用 RxJava 发出来的请求。

```kotlin
static final class Listener extends MainThreadDisposable implements View.OnAttachStateChangeListener {
  private final View view;
  private final CompletableObserver observer;

  Listener(View view, CompletableObserver observer) {
    this.view = view;
    this.observer = observer;
  }

  @Override public void onViewAttachedToWindow(View v) { }

  @Override public void onViewDetachedFromWindow(View v) {
    if (!isDisposed()) {
    //看到没看到没看到没？
      observer.onComplete();
    }
  }

  @Override protected void onDispose() {
    view.removeOnAttachStateChangeListener(this);
  }
}
```

考虑到前面提到的 Anko 扩展 onClick 无法取消协程的问题，也可以搞一个 onClickAutoDisposable。

```kotlin
fun View.onClickAutoDisposable (
        context: CoroutineContext = Dispatchers.Main,
        handler: suspend CoroutineScope.(v: View) -> Unit
) {
    setOnClickListener { v ->
        GlobalScope.launch(context, CoroutineStart.DEFAULT) {
            handler(v)
        }.asAutoDisposable(v)
    }
}
```

launch 会启动一个 Job，因此可以通过 asAutoDisposable 来将其转换成支持自动取消的类型：

```kotlin
fun Job.asAutoDisposable(view: View) = AutoDisposableJob(view, this)
```

那么 AutoDisposableJob 的实现只要参考 Auto

Disposable 的实现依样画葫芦就好了：

```kotlin
class AutoDisposableJob(private val view: View, private val wrapped: Job)
    //我们实现了 Job 这个接口，但没有直接实现它的方法，而是用 wrapped 这个成员去代理这个接口
     : Job by wrapped, OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View?) = Unit

    override fun onViewDetachedFromWindow(v: View?) {
        //当 View 被移除的时候，取消协程
        cancel()
        view.removeOnAttachStateChangeListener(this)
    }

    private fun isViewAttached() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow || view.windowToken != null

    init {
        if(isViewAttached()) {
            view.addOnAttachStateChangeListener(this)
        } else {
            cancel()
        }

        //协程执行完毕时要及时移除 listener 免得造成泄露
        invokeOnCompletion() {
            view.removeOnAttachStateChangeListener(this)
        }
    }
}
```

这样的话，就可以使用这个扩展了：

```kotlin
button.onClickAutoDisposable{
    try {
        val req = Request()
        val resp = async { sendRequest(req) }.await()
        updateUI(resp)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```

当 button 这个对象从 window 上撤下来的时候，协程就会收到 cancel 的指令，尽管这种情况下协程的执行不会跟随 Activity 的 onDestory 而取消，但它与 View 的点击事件紧密结合，即便 Activity 没有被销毁，View 本身被移除时也会直接将监听中的协程取消掉。

### 8.4. 合理使用调度器

在 Android 上使用协程，更多的就是简化异步逻辑的写法，使用场景更多与 RxJava 类似。在使用 RxJava 的时候，就发现有不少开发者仅仅用到了它的切线程的功能，而且由于本身 RxJava 切线程 API 简单易用，还会造成很多无脑线程切换的操作，这样实际上是不好的。那么使用协程就更要注意这个问题了，因为协程切换线程的方式被 RxJava 更简洁、更透明，本来这是好事情，就怕被滥用。

比较推荐的写法是，绝大多数 UI 逻辑在 UI 线程中处理，即使在 UI 中用 Dispatchers.Main 来启动协程，如果涉及到一些 io 操作，使用 async 将其调度到 Dispatchers.IO 上，结果返回时协程会切回到主线程 -- 这非常类似 Nodejs 这样的单线程的工作模式。

对于一些 UI 不相关的逻辑，例如批量离线数据下载任务，通常默认的调度器就足够使用了。

相比其他类型的应用，Android 作为 UI 程序最大的特点就是异步要协调好 UI 的生命周期，协程也不例外。

## 9. Channel

Channel 实际上就是协程在生产消费者模型上的应用。

### 9.1. 认识 channel

Channel 实际上就是一个队列，而且是并发安全的，它可以用来连接协程，实现不同协程的通信。

```kotlin
suspend fun main() {
    val channel = Channel<Int>()

    val producer = GlobalScope.launch {
        var i = 0
        while (true){
            channel.send(i++)
            delay(1000)
        }
    }

    val consumer = GlobalScope.launch {
        while(true){
            val element = channel.receive()
            Logger.debug(element)
        }
    }

    producer.join()
    consumer.join()
}
```

构造了两个协程，分别叫他们 producer 和 consumer，没有明确的指定调度器，所以他们的调度器都是默认的，在 Java 虚拟机上就是线程池：他们可以运行在不同的线程上，当然也可以运行在同一个线程上。

例子的运行机制是，producer 当中每隔 1s 向 Channel 中发送一个数字，而 consumer 那边则是一直在读取 Channel 来获取这个数字并打印，能够发现这里发端是比收端慢的，在没有值可以读到的时候，receive 是挂起的，直到有新元素 send 过来 -- 所以 receive 是一个挂起函数。

### 9.2. Channel 的容量

send 也是挂起函数。BlockingQueue 让里面添加元素的时候，元素在队列里实际上是占用了空间的，如果这个队列空间不足，那么再往里面添加的时候就是两种情况：1. 阻塞，等待队列腾出空间；2. 抛异常，拒绝添加元素。send 也会面临同样的问题，Channel 实际上就是一个队列，队列不应该有缓冲区吗，那么这个缓冲区一旦满了，并且也一直没有人调用 receive 取走元素的话，send 不就挂起了嘛。

那么看一下 Channel 的缓冲区的定义：

```kotlin
public fun <E> Channel(capacity: Int = RENDEZVOUS): Channel<E> =
    when (capacity) {
        RENDEZVOUS -> RendezvousChannel()
        UNLIMITED -> LinkedListChannel()
        CONFLATED -> ConflatedChannel()
        else -> ArrayChannel(capacity)
    }
```

构造 Channel 的时候调用了一个叫 channel 的函数，这个确实不是它的构造器，在 Kotlin 当中可以随便定义一个顶级函数跟某些类名一样来伪装成构造器，这本质上就是个工厂方法。

它又一个参数叫 capacity，指定缓冲区的容量，默认值 RENDEZVOUS 就是 0，不来 receive，send 就一直搁这儿挂起等着。换句话说，如果 consumer 不 receive，producer 里面的第一个 send 就给挂起了：

```kotlin
val producer = GlobalScope.launch {
    var i = 0
    while (true){
        i++ //为了方便输出日志，我们将自增放到前面
        Logger.debug("before send $i")
        channel.send(i)
        Logger.debug("before after $i")
        delay(1000)
    }
}

val consumer = GlobalScope.launch {
    while(true){
        delay(2000) //receive 之前延迟 2s
        val element = channel.receive()
        Logger.debug(element)
    }
}
```

故意让收端的节奏放慢，就会发现，send 总是会挂起，直到 receive 之后才会继续往下执行：

```kotlin
07:11:23:119 [DefaultDispatcher-worker-2 @coroutine#1]  before send 1
07:11:24:845 [DefaultDispatcher-worker-2 @coroutine#2]  1
07:11:24:846 [DefaultDispatcher-worker-2 @coroutine#1]  before after 1
07:11:25:849 [DefaultDispatcher-worker-4 @coroutine#1]  before send 2
07:11:26:850 [DefaultDispatcher-worker-2 @coroutine#2]  2
07:11:26:850 [DefaultDispatcher-worker-3 @coroutine#1]  before after 2
```

UNLIMITED 比较好理解，来者不拒，从给出的实现 LinkedListChannel 来看，这一点业余 LinkedBlockingQueue 有异曲同工之妙。

CINFLATED，这个词是合并的意思，跟 inflate 是同一个词根，con- 前缀表示反着来，那是不是说发了个 1、2、3、4、5 那边收的时候就会收到一个 [1,2,3,4,5] 的集合呢？但实际上这个的效果是只保留最后一个元素，并不是合并，应该是指置换，换句话说，这个类型的 Channel 有一个元素大小的缓冲区，但每次有新元素过来，都会用新的替换旧的，也就是说发了个 1、2、3、4、5 之后收端才接收的话，就只能收到 5 了。

剩下的就是 ArrayChannel 了，它接收一个值作为缓冲区容量的大小，这也比较类似于 ArrayBlockingQueue。

### 9.3. 迭代 Channel

前面在发送和读取 Channel 的时候用了 while(true)，因为想要去不断的进行读写操作，Channel 本身实际上也有点儿像序列，可以一个一个读，所以在读取的时候也可以直接获取一个 Channel 的 iterator：

```kotlin
val consumer = GlobalScope.launch {
    val iterator = channel.iterator()
    while(iterator.hasNext()){ // 挂起点
        val element = iterator.next()
        Logger.debug(element)
        delay(2000)
    }
}
```

那么这个时候，iterator.hasNext() 是挂起函数，在判断是否有下一个元素的时候实际上就需要去 Channel 当中读取元素了。

这个写法可以简化成 for each：

```kotlin
val consumer = GlobalScope.launch {
    for (element in channel) {
        Logger.debug(element)
        delay(2000)
    }
}
```

### 8.4. produce 和 actor

前面在协程外部定义 Channel，并在协程当中访问它，实现了一个简单的生产-消费者的示例，那么有没有便捷的办法构造生产者和消费者呢？

```kotlin
val receiveChannel: ReceiveChannel<Int> = GlobalScope.produce {
    while(true){
        delay(1000)
        send(2)
    }
}
```

可以通过 produce 这个方法启动一个生产者协程，并返回一个 ReceiveChannel，其他协程就可以拿着这个 Channel 来接收数据了。反过来，可以用 actor 启动一个消费者协程：

```kotlin
val sendChannel: SendChannel<Int> = GlobalScope.actor<Int> {
    while(true){
        val element = receive()
    }
}
```

ReceiveChannel 和 SendChannel 都是 Channel 的父接口，前者定义了 receive，后者定义了 send，Channel 也因此即可以 receive 又可以 send。

product 和 actor 与 launch 一样都被称作 “协程启动器”。通过这两个协程的启动器启动的协程也自然的与返回的 Channel 绑定到了一起，因此 Channel 的关闭也会在协程结束时自动完成，以 produce 为例，它构造出了一个 ProducerCoroutine 的对象：

```kotlin
internal open class ProducerCoroutine<E>(
    parentContext: CoroutineContext, channel: Channel<E>
) : ChannelCoroutine<E>(parentContext, channel, active = true), ProducerScope<E> {
    ...
    override fun onCompleted(value: Unit) {
        _channel.close() // 协程完成时
    }

    override fun onCancelled(cause: Throwable, handled: Boolean) {
        val processed = _channel.close(cause) // 协程取消时
        if (!processed && !handled) handleCoroutineException(context, cause)
    }
}
```

注意到在协程完成和取消的方法调用中，对应的 _channel 都会被关闭。

这样看上去还是挺有用的。不过截止这俩 API product 和 actor 目前都没有稳定，前者仍被标记为 ExperimentalCoroutinesApi，后者则标记为 ObsoleteCoroutineApi，这就比较尴尬了，明摆着不让用。actor 的文档中提到的 issue 的讨论相比基于 Actor 模型的并发框架，Kotlin 协程提供的这个 actor API 也不过就是提供了一个 SendChannel 的返回值而已。

虽然 produce 没有被标记为 ObsoleteCoroutinesApi，显然它作为 actor 的另一半，不可能单独转正的，这俩 API 建议是看看就好了。

### 9.5. Channel 的关闭

Channel 与 Flow 不同，它是在线的，是一个热数据源，换句话说就是有想要收数据，就要有人在对面给他发。既然这样，就难免曲终人散，对于一个 Channel，如果调用了它的 close，它会立即停止接受新元素，也就是说这时候它的 isClosedForSend 会立即返回 true，而由于 Channel 缓冲区的存在，这时候可能还有一些元素没有被处理完，所以要等所有的元素都被读取之后 isClosedForReceive 才会返回 true。

```kotlin
val channel = Channel<Int>(3)

val producer = GlobalScope.launch {
    List(5){
        channel.send(it)
        Logger.debug("send $it")
    }
    channel.close()
    Logger.debug("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
}

val consumer = GlobalScope.launch {
    for (element in channel) {
        Logger.debug("receive: $element")
        delay(1000)
    }

    Logger.debug("After Consuming. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
}
```

把栗子稍作修改，开了一个缓冲区大小为 3 的 Channel，在 producer 协程里面快速的发送元素出去，发送 5 个之后关闭 Channel，而在 consumer 协程当中每秒读取一个，结果如下：

```kotlin
11:05:20:678 [DefaultDispatcher-worker-1]  send 0
11:05:20:678 [DefaultDispatcher-worker-3]  receive: 0
11:05:20:678 [DefaultDispatcher-worker-1]  send 1
11:05:20:678 [DefaultDispatcher-worker-1]  send 2
11:05:20:678 [DefaultDispatcher-worker-1]  send 3
11:05:21:688 [DefaultDispatcher-worker-3]  receive: 1
11:05:21:688 [DefaultDispatcher-worker-3]  send 4
11:05:21:689 [DefaultDispatcher-worker-3]  close channel. ClosedForSend =true ClosedForReceive = false
11:05:22:693 [DefaultDispatcher-worker-3]  receive: 2
11:05:23:694 [DefaultDispatcher-worker-4]  receive: 3
11:05:24:698 [DefaultDispatcher-worker-4]  receive: 4
11:05:25:700 [DefaultDispatcher-worker-4]  After Consuming. ClosedForSend =true ClosedForReceive = true
```

Channel 其实内部的资源就是个缓冲区，这个东西本质上就是个线性表，就是一块儿内存，所以如果开了一个 Channel 而不去关闭它，其实也不会造成什么资源泄漏，发端如果自己已经发完，它就可以不理会这个 Channel。

但是，这时候在接收端就比较尴尬了，它不知道会不会有数据发过来，如果 Channel 是微信，那么接收端打开微信的窗口可能一直看到的是“对方正在输入”，然后它就一直这样了。所以这里的关闭更像是一种约定。

那么 Channel 的关闭究竟应该有谁来处理呢？正常的通信，如果是单向的，就好比领导讲话，讲完都会说“我讲完了”，你不能在领导还没讲完的时候就说“我听完了”，所以单向通信的情况比较推荐由发端处理关闭；而对于双向通信的情况，就要考虑协商了，双向通信从技术上两端是对等的，但业务场景下通常来说不是，建议由主导的一方处理关闭。

还有一个复杂的情况，一对多、多对多的手法情况，这种也仍然存在主导一方，Channel 的生命周期最好由主导方来维护官方文档给出的扇入（fan-in）和扇出（fan-out），其实就是这种情况。

```
扇入和扇出的概念，可以想象它是一把折扇，折扇的边射向圆心就是扇入，这种情况圆心如果是通信的一段，那它就是接收方，如果是一个函数，那它就是被调用方。扇入越大，说明模块的复用程度越高，以函数为例，如果一个函数被调用的次数越多，那说明复用的程度越高。扇出就是反过来的情况，描述的是复杂度高的情形，例如一个 Model，负责调用网络模块、数据库、文件等很多模块。
```

### 9.6. BroadcastChannel

前面提到了一对多的情形，从数据处理的本身来讲，虽然有多个接收端，同一个元素只会被一个接收端读到。广播则不然，多个接收端不存在互斥行为。

直接创建 broadcastChannel 的方法跟普通的 Channel 似乎也没有太多的不一样：

```kotlin
val broadcastChannel = broadcastChannel<Int>(5)
```

如果要订阅，那么只需要调用：

```kotlin
val receiveChannel = broadcastChannel.openSubscription()
```

这样就得到了一个 ReceiveChannel，获取订阅的消息，只需要调用它的 receive。

例子中在发端发送 1-5，并启动 3 个协程同时接收广播：

```kotlin
val producer = GlobalScope.launch {
    List(5) {
        broadcastChannel.send(it)
        Logger.debug("send $it")
    }
    channel.close()
}
    
List(3) { index ->
    GlobalScope.launch {
        val receiveChannel = broadcast.openSubscription()
        for (element in receiveChannel) {
            Logger.debug("[$index] receive: $element")
            delay(1000)
        }
    }
}.forEach { it.join() }
    
producer.join()
```

输出结果如下：

```
12:34:59:656 [DefaultDispatcher-worker-6]  [2] receive: 0
12:34:59:656 [DefaultDispatcher-worker-3]  [1] receive: 0
12:34:59:656 [DefaultDispatcher-worker-5]  [0] receive: 0
12:34:59:656 [DefaultDispatcher-worker-7]  send 0
12:34:59:657 [DefaultDispatcher-worker-7]  send 1
12:34:59:658 [DefaultDispatcher-worker-7]  send 2
12:35:00:664 [DefaultDispatcher-worker-3]  [0] receive: 1
12:35:00:664 [DefaultDispatcher-worker-5]  [1] receive: 1
12:35:00:664 [DefaultDispatcher-worker-6]  [2] receive: 1
12:35:00:664 [DefaultDispatcher-worker-8]  send 3
12:35:01:669 [DefaultDispatcher-worker-8]  [0] receive: 2
12:35:01:669 [DefaultDispatcher-worker-3]  [1] receive: 2
12:35:01:669 [DefaultDispatcher-worker-6]  [2] receive: 2
12:35:01:669 [DefaultDispatcher-worker-8]  send 4
12:35:02:674 [DefaultDispatcher-worker-8]  [0] receive: 3
12:35:02:674 [DefaultDispatcher-worker-7]  [1] receive: 3
12:35:02:675 [DefaultDispatcher-worker-3]  [2] receive: 3
12:35:03:678 [DefaultDispatcher-worker-8]  [1] receive: 4
12:35:03:678 [DefaultDispatcher-worker-3]  [0] receive: 4
12:35:03:678 [DefaultDispatcher-worker-1]  [2] receive: 4
```

每一个收端协程都可以读取到每一个元素。

日志顺序不能非常直观的反映数据的读写顺序。

除了直接创建意外，也可以直接用普通的 Channel 来做个转换：

```kotlin
val channel = Channel<Int>()
val broadcast = channel.broadcast(3)
```

其中，参数表示缓冲区的大小。

实际上这里得到的这个 broadcastChannel 可以认为与原 Channel 是级联关系，这个扩展方法的源码展示了这一点：

```kotlin
fun <E> ReceiveChannel<E>.broadcast(
    capacity: Int = 1,
    start: CoroutineStart = CoroutineStart.LAZY
): broadcastChannel<E> =
    GlobalScope.broadcast(Dispatchers.Unconfined, capacity = capacity, start = start, onCompletion = consumes()) {
        for (e in this@broadcast) {  //这实际上就是在读取原 Channel
            send(e)
        }
    }
```

对于 BroadcastChannel，官方也提供类似于 produce 和 actor 的方式，可以通过 CoroutineScope.broadcast 来直接启动一个协程，并返回一个 BroadcastChannel。

需要注意的是，从原始的 Channel 转换到 BroadcastChannel 其实就是对原 Channel 的一个读取操作，如果还有其他协程也在读这个原始的 Channel，那么会与 BroadcastChannel 产生互斥关系。

另外，BroadcastChannel 相关的 API 大部分被标记为 ExperimentalCoroutinesApi。

### 9.7. Channel 版本的序列生成器

Sequence 的生成器是基于标准库的协程的 API 实现的，实际上 Channel 本身也可以用来生成序列，例如：

```kotlin
val channel = GlobalScope.produce(Dispatchers.Unconfined) {
    Logger.debug("A")
    send(1)
    Logger.debug("B")
    send(2)
    Logger.debug("Done")
}

for (item in channel) {
    Logger.debug("Got $item")
}
```

produce 创建的协程返回了一个缓冲区大小为 0 的 Channel，传入了一个 Dispatchers.Uncofined 调度器，意味着协程会立即在当前协程执行到第一个挂起点，所以会理解输出 A 并在 send(1) 处挂起，直到后面的 for 循环读到第一个值时，实际上就是 channel 的 iterator 的 hasNext 方法的调用，这个 hasNext 方法会检查是否有下一个元素，是一个挂起函数，在检查的过程中就会让前面启动的协程从 send(1) 挂起的位置继续执行，因此会看到日志 B 输出，然后再挂起到 send(2) 这里，这时候 hasNext 结束挂起，for 循环终于输出第一个元素，依次类推。输出结果如下：

```kotlin
22:33:56:073 [main @coroutine#1]  A
22:33:56:172 [main @coroutine#1]  B
22:33:56:173 [main]  Got 1
22:33:56:173 [main @coroutine#1]  Done
22:33:56:176 [main]  Got 2
```

B 居然比 Got 1 先输出，同样，Done 也比 Got 2 线输出，这个看上去比较不符合直觉，不过挂起恢复的执行顺序确实如此，关键点就是 hasNext 方法会挂起并触发了协程内部从挂起点继续执行的操作。如果选择了其他调度器，当然也会有其他合理的结果输出。如果类似的代码换作 sequence，是这样的：

```kotlin
val sequence = sequence {
    Logger.debug("A")
    yield(1)
    Logger.debug("B")
    yield(2)
    Logger.debug("Done")
}

Logger.debug("before sequence")

for (item in sequence) {
    Logger.debug("Got $item")
}
```

sequence 的执行顺序要直观的多，它没有调度器的概念，而且 sequence 的 iterator 的 hasNext 和 next 都不是挂起函数，在 hasNext 的时候同样会触发元素的查找，这时候就会触发 sequence 内部逻辑的执行，因此这次实际上是先触发了 hasNext 才会输出 A，yield 把 1 穿出来作为 sequence 的第一个元素，这样就会有 Got 1 这样的输出，完整输出如下：

```kotlin
22:33:55:600 [main]  A
22:33:55:603 [main]  Got 1
22:33:55:604 [main]  B
22:33:55:604 [main]  Got 2
22:33:55:604 [main]  Done
```

sequence 本质上就是基于标准库的协程 API 实现的，没有上层协程框架的作用域以及 Job 这样的概念。

所以可以在 Channel 的例子里面切换不同的调度器来生成元素，例如：

```kotlin
val channel = GlobalScope.produce(Dispatchers.Unconfined) {
    Logger.debug(1)
    send(1)
    withContext(Dispatchers.IO){
        Logger.debug(2)
        send(2)
    }
    Logger.debug("Done")
}
```

sequence 就不行。

当然，单纯的用 Channel 当做序列生成器来使用有点儿小题大做。

### 9.8. Channel 的内部结构

sequence 无法享受更上层的协程框架概念下的各种能力，还有一点 sequence 显然不是线程安全的，而 Channel 可以在并发场景下使用。

Channel 内部结构主要说下缓冲区分别是链表和数组的版本。链表版本的定义主要是在 AbstractSendChannel 当中：

```kotlin
internal abstract class AbstractSendChannel<E> : SendChannel<E> {
    protected val queue = LockFreeLinkedListHead()
    ...    
}
```

LockFreeLinkedListHead 本身其实就是一个双向链表的节点，实际上 Channel 把它首尾相连成为了循环链表，而这个 queue 就是哨兵(sentinel) 节点。有新的元素添加时，就在 queue 的前面插入，实际上就相当于在整个队列的最后插入元素了。

它所谓的 LockFree 在 Java 虚拟机上其实是通过原子读写来实现的，对于链表来说，需要修改的无非就是前后节点的引用：

```kotlin
public actual open class LockFreeLinkedListNode {
    private val _next = atomic<Any>(this) // Node | Removed | OpDescriptor
    private val _prev = atomic<Any>(this) // Node | Removed
    ...   
}
```

它的实现基于无锁链表的实现，由于 CAS 原子操作通常只能修改一个引用，对于需要原子同时修改前后节点引用的情形是不适用的，例如单链表插入节点时需要修改两个引用，分别是操作节点的前一个节点的 next 和自己的 next，即 Head -> A -> B -> C 在 A、B 之间插入 X 时需要先修改 X -> B 再修改 A->X，如果这个过程中 A 被删除，那么可能的结果是 X 一并被删除，得到的链表是 Head -> B -> C。

这个无锁链表的实现通过引入 prev 来辅助解决这个问题，即在 A 被删除的问题发生的同时，可以做到 X.next = B，X.prev = A 的，这时候判断如果 A 已经被移除了，那么 B.prev 本来是 A，结果是变成了 Head，这时候就可以将 X.prev 再次赋值为 B.prev 来修复，当然这个过程稍稍有些复杂。

而对于数组版本，ArrayChannel 内部就是一个数组：

```kotlin
//缓冲区大小大于 8，会先分配大小为 8 的数组，在后续进行扩容
private var buffer: Array<Any?> = arrayOfNulls<Any?>(min(capacity, 8))
```

对这个数组读写时则直接用了一个 ReentrantLock 进行加锁。

其实对于数组的元素，同样可以进行 CAS 读写，可以参考下 ConcurrentHashMap 的实现，JDK 7 的实现中对于段数组的读写采用了 UnSafe 的 CAS 读写，JDK 1.8 直接干掉了分段，对于桶的读写也采用 UnSafe 的 CAS。

Channel 的出现，应该说为协程注入了灵魂。每一个独立的协程不再是孤独的个体，Channel 可以让他们更加方便的协作起来。

## 10. Select

### 10.1. 复用多个 await

如果有这样一个场景，两个 API 分别从网络和本地缓存获取数据，期望哪个先返回就先用哪个做展示：

```kotlin
fun CoroutineScope.getUserFromApi(login: String) = async(Dispatchers.IO){
    gitHubServiceApi.getUserSuspend(login)
}

fun CoroutineScope.getUserFromLocal(login:String) = async(Dispatchers.IO){
    File(localDir, login).takeIf { it.exists() }?.readText()?.let { gson.fromJson(it, User::class.java) }
}
```

不管先调用哪个 API 返回的 Deferred 的 await，都会被挂起，如果想要实现这一需求就要启动两个协程来调用 await，这样反而将问题复杂化了。

接下来用 select 来解决这个问题：

```kotlin
GlobalScope.launch {
    val localDeferred = getUserFromLocal(login)
    val remoteDeferred = getUserFromApi(login)

    val userResponse = select<Response<User?>> {
        localDeferred.onAwait { Response(it, true) }
        remoteDeferred.onAwait { Response(it, false) }
    }
    ...
}.join()
```

可以看到，没有直接调用 await，而是调用了 onAwait 在 select 当中注册了个回调，不管哪个先回调，select 立即返回对应回调中的结果。假设 localDeferred.onAwait 先返回，那么 userResponse 的值就是 Response(it, true)，当然由于本地缓存可能不存在，因此 select 的结果类型是 Response\<User?>。

如果先返回的是本地缓存，那么还需要获取网络结果来展示最新结果：

```kotlin
GlobalScope.launch {
    ...
    userResponse.value?.let { log(it) }
    userResponse.isLocal.takeIf { it }?.let {
        val userFromApi = remoteDeferred.await()
        cacheUser(login, userFromApi)
        log(userFromApi)
    }
}.join()
```

### 10.2. 复用多个 Channel

对于多个 Channel 的情况，也比较类似：

```kotlin
val channels = List(10) { Channel<Int>() }

select<Int?> {
    channels.forEach { channel ->
        channel.onReceive { it }
        // OR
        channel.onReceiveOrNull { it }
    }
}
```

对于 onReceive，如果 Channel 被关闭，select 回直接抛出异常，而对于 onReceiveOrNull 如果遇到了 Channel 被关闭的情况，it 的值就是 null。

### 10.3. SelectClause

怎么知道哪些事件可以被 select 呢？其实所有能够被 select 的事件都是 selectClauseN 类型，包括：

* SelectClause0: 对应事件没有返回值，例如 join 没有返回值，对应的 onJoin 就是这个类型，使用时 onJoin 的参数是一个无参函数：

  ```kotlin
  select<Unit> {
      job.onJoin { log("Join resumed!") }
  }
  ```

* SelectClause1：对应事件有返回值，前面的 onAwait 和 onReceive 都是此类情况。

* SelectClause2：对应事件有返回值，此外还需要额外的一个参数，例如 Channel.onSend 有两个参数，第一个就是一个 Channel 数据类型的值，表示即将发送的值，第二个是发送成功时的回调：

  ```kotlin
  List(100) { element ->
      select<Unit> {
          channels.forEach { channel ->
              channel.onSend(element) { sentChannel -> log("sent on $sentChannel") }
          }
      }
  }
  ```

  在消费者的消费效率较低时，数据能发给哪个就发给哪个进行处理，onSend 的第三个参数的参数是数据成功发送到的 Channel 对象。

因此如果想要确认挂起函数是否支持 select，只需要查看其是否存在对应的 SelectClauseN 即可。

在协程中，Select 的语义与 Java NIO 或者 Unix 的 IO 多路复用类似，它的存在可以轻松实现 1 拖 N，实现哪个先来就处理哪个。尽管 Select 和 Channel 比起标准库的协程 API 已经更接近业务开发了，不过它们仍属于相对底层的 API 封装，在实践当中多数情况下也可以使用 Flow API 来解决。

而 Flow API，完全就是响应式编程的协程版 API。

## 11. Flow

Flow 就是 Kotlin 协程与响应式编程模型结合的产物。

### 11.1. 认识 Flow

**代码清单 1: 序列生成器**

```kotlin
val ints = sequence {
  (1..3).forEach { 
    yield(it)
  }  
}
```

每次访问 ints 的下一个元素的时候它就执行内部的逻辑直到遇到 yield，如果希望在元素之间加上延时呢？

**代码清单 2: 序列生成器中不能调用其他挂起函数**

```kotlin
val ints = sequence {
  (1..3).forEach { 
    yield(it)
    delay(1000) // ERROR!
  }  
}
```

受 RestrictsSuspension 注解的约束，delay 不能在  SequenceScope 的扩展成员当中被调用，因为不能在序列生成器的协程体内调用了。

假设序列生成器不受这个限制，调用 delay 回导致后续的执行流程的线程发生变化，外部的调用者发现在访问 ints 的下一个元素的时候居然还会有切换线程的副作用，不仅如此，想通过指定调度器来限定序列创建所在的线程，同样是不可以的，甚至没有办法为它设置协程上下文。

既然序列生成器有这个多的限制，需要认识一下 Flow 了。它的 API 与序列生成器极为相似：

**代码清单 3: 创建 Flow**

```kotlin
val intFlow = flow {
  (1..3).forEach { 
    emit(it)
    delay(100)
  }
}
```

新元素通过 emit 函数提供，Flow 的执行体内部也可以调用其他挂起函数，这样就可以在每次提供一个新元素后再延时 100ms 了。

Flow 也可以设定它运行时所使用的调度器：

```kotlin
intFlow.flowOn(Dispatchers.IO)
```

通过 flowOn 设置的调度器只对它之前的操作有影响，因此这里意味着 intFlow 的构造逻辑会在 IO 调度器上执行。

最终消费 intFlow 需要调用 collect 函数，这个函数也是一个挂起函数，启动一个协程来消费 intFlow：

**代码清单 4: 消费 Flow**

```kotlin
GlobalScope.launch(myDispatcher) {
  intFlow.flowOn(Dispatchers.IO)
    .collect { println(it) }
}.join()
```

为了区分调度器，为协程设置了一个自定义的调度器，它会将协程调度到名为 MyThread 的协程上，结果如下：

```kotlin
[MyThread] 1
[MyThread] 2
[MyThread] 3
```

### 11.2. 对比 RxJava 的线程切换

RxJava 也是一个基于响应式编程模型的异步框架，它提供了两个切换调度器的 API 分别是 subscribeOn 和 observeOn：

**代码清单 5: RxJava 的调度器切换**

```kotlin
Observable.create<Int> {
  (1..3).forEach { e ->
    it.onNext(e)
  }
  it.onComplete()
}.subscribeOn(Schedulers.io())
.observeOn(Schedulers.from(myExecutor))
.subscribe {
  println(it)
}
```

其中 subscribeOn 指定的调度器影响前面的逻辑，observeOn 影响的是后面的逻辑，因此 it.onNext(e) 执行在它的 io 这个调度器上，而最后的 println(it) 执行在通过 myExecutor 创建出来的调度器上。

Flow 的调度器 API 中看似只有 flowOn 与 subscribeOn 对应，其实不然，collect 所在协程的调度器则与 observeOn 指定的调度器对应。

在 RxJava 的学习和使用过程中，subscribeOn 和 observeOn 经常容易被混淆；而在 Flow 当中 collect 所在的协程自然就是观察者，它想运行在什么调度器上它自己指定即可，非常容易区分。

### 11.3. 冷数据流

一个 Flow 创建出来之后，不消费则不生产，多次消费则多次生产，生产和消费总是相对应的。

**代码清单 6：Flow 可以被重复消费**

```kotlin
GlobalScope.launch(dispatcher) {
  intFlow.collect { println(it) }
  intFlow.collect { println(it) }
}.join()
```

消费 intFlow 会输出 1,2,3, 重复消费它会重复输出 1,2,3。

这一点类似于前面提到的 sequence 和 RxJava 例子，它们也都是自己的消费端。创建一个序列然后去迭代它，每次迭代都会创建一个新的迭代器从头开始迭代；RxJava 的 Observable 也是如此，每次调用它的 subscribe 都会重新消费一次。

所谓冷数据流，就是只有消费时才会生产的数据流，这一点与 Channel 正对应：Channel 的发送端并不依赖于接收端。

说明 RxJava 也存在热数据流，可以通过一定的手段实现冷热数据流的转化。不过相比之下，冷数据流的应用场景更为丰富。

### 11.4. 异常处理

Flow 的异常处理也比较直接，直接调用 catch 函数即可：

**代码清单 7：捕获 Flow 的异常**

```kotlin
flow {
  emit(1)
  throw ArithmeticException("Div 0")
}.catch { t: Throwable ->
  println("caught error: $t")
}
```

在 Flow 的参数中抛出了一个异常，在 catch 函数中就可以直接捕获到这个异常。如果没有调用 catch 函数，未捕获异常会在消费时抛出。请注意，catch 函数只能捕获它的上游的异常。

如果想要在流完成时执行逻辑，可以使用 onCompletion：

**代码清单 8：订阅流的完成**

```kotlin
flow {
  emit(1)
  throw ArithmeticException("Div 0")
}.catch { t: Throwable ->
  println("caught error: $t")
}.onCompletion { t: Throwable? ->
  println("finally.")
}
```

onCompletion 用起来比较类似于 try ... catch ... finally 中的 finally，无论前面是否存在异常，它都会被调用，参数 t 则是前面未捕获的异常。

Flow 的设计初衷是希望确保流操作中异常透明。因此，以下写法是违反 Flow 的设计原则的：

**代码清单 9：命令式的异常处理（不推荐）**

```kotlin
flow { 
  try {
    emit(1)
    throw ArithmeticException("Div 0")
  } catch (t: Throwable){
    println("caught error: $t")
  } finally {
    println("finally.")
  }
}
```

在流操作内部使用 try ... catch ... finally 这样的写法后续可能被禁用。

在 RxJava 当中还有 onErrorReturn 类似的操作：

**代码清单 10：RxJava 从异常中恢复**

```kotlin
val observable = Observable.create<Int> {
  ...
}.onErrorReturn {
  println(t)
  10
}
```

捕获异常后，返回 10 作为下一个值。

在 Flow 当中也可以模拟这样的操作：

**代码清单 11：Flow 从异常中恢复**

```kotlin
flow {
  emit(1)
  throw ArithmeticException("Div 0")
}.catch { t: Throwable ->
  println("caught error: $t")
  emit(10)
}
```

这里可以使用 emit 重新生产新元素出来。emit 定义在 FlowCollector 当中，因此只要遇到 Receiver 为 FlowCollector 的函数，就可以生产新元素。

**说明** onCompletion 预计在协程框架的 1.4 版本中会被重新设计，之后它的作用类似于 RxJava 中 Subscriber 的 onComplete，即作为整个 Flow 的完成回调使用，回调的参数也将包含整个 Flow 的未捕获异常。

### 11.5. 末端操作符

collect 是最基本的末端操作符，功能与 RxJava 的 subscribe 类似。除了 collect 之外，还有其他常见的末端操作符，大体分为两类：

1. 集合类型转换操作，包括 toList、toSet 等。
2. 聚合操作，包括将 Flow 规约到单值的 reduce、fold 等操作，以及获得耽搁元素的操作包括 single、singleOrNull、first 等。

实际上，识别是否为末端操作符，还有一个简单方法，由于 Flow 的消费端一定需要运行在协程当中，因此末端操作符都是挂起函数。

### 11.6. 分离 flow 的消费和触发

除了可以在 collect 处消费 Flow 的元素之外，还可以通过 onEach 来做到这一点。这样消费的具体操作就不需要与末端操作符放在一起，collect 函数可以放到其他任意位置调用，例如：

**代码清单 12：分离 Flow 的消费和触发**

```kotlin
fun createFlow() = flow<Int> {
    (1..3).forEach {
      emit(it)
      delay(100)
    }
  }.onEach { println(it) }

fun main(){
  GlobalScope.launch {
    createFlow().collect()
  }
}
```

由此又可以衍生出一种新的消费 Flow 的写法：

**代码清单 13：使用协程作用域直接触发 Flow**

```kotlin
fun main(){
  createFlow().launchIn(GlobalScope)
}
```

### 11.7. Flow 的取消

Flow 没有提供取消操作，原因很简单：不需要。

Flow 的消费依赖于 collect 这样的末端操作符，而它们又必须在协程当中调用，因此 Flow 的取消主要依赖于末端操作符所在的协程的状态。

**代码清单 14：Flow 的取消**

```kotlin
val job = GlobalScope.launch {
  val intFlow = flow {
    (1..3).forEach {
      delay(1000)
      emit(it)
    }
  }

  intFlow.collect { println(it) }
}

delay(2500)
job.cancelAndJoin()
```

每隔 1000ms 生产一个元素，2500ms 以后协程被取消，因此最后一个元素生产前 Flow 就已经被取消，输出为：

```kotlin
1
▶ 1000ms later
2
```

如此看来，想要取消 Flow 只需要取消它所在的协程即可。

### 11.8. 其他 Flow 的创建方式

已经知道了 flow{...}这种形式的创建方式，不过在这当中无法随意切换调度器，这是因为 emit 函数不是线程安全的：

**代码清单 15：不能在 Flow 中直接切换调度器**

```kotlin
flow { // BAD!!
  emit(1)
  withContext(Dispatchers.IO){
    emit(2)
  }
}
```

想要在生成元素时切换调度器，就必须使用 channelFlow 函数来创建 Flow：

```kotlin
channelFlow {
  send(1)
  withContext(Dispatchers.IO) {
    send(2)
  }
}
```

此外，也可以通过集合框架来创建 Flow：

```kotlin
listOf(1, 2, 3, 4).asFlow()
setOf(1, 2, 3, 4).asFlow()
flowOf(1, 2, 3, 4)
```

### 11.9. Flow 的背压

只要是响应式编程，就一定会有背压问题。

背压问题在生产者的生产速率高于消费者的处理速率的情况下出现。为了保证数据不丢失，也会考虑添加缓存来缓解问题：

**代码清单 16：为 Flow 添加缓冲**

```kotlin
flow {
  List(100) {
    emit(it)
  }
}.buffer()
```

也可以为 buffer 指定一个容量。不过，如果只是单纯地添加缓存，而不是从根本上解决问题就始终会造成数据积压。

问题产生的根本原因是生产和消费速率的不匹配，除直接优化消费者的性能意外，也可以采取一些取舍的手段。

第一种是 conflate。与 Channel 的 Conflate 模式一致，新数据会覆盖老数据，例如：

**代码清单 17：使用 conflate 解决背压问题**	

```kotlin
flow {
  List(100) {
    emit(it)
  }
}.conflate()
.collect { value ->
  println("Collecting $value")
  delay(100) 
  println("$value collected")
}
```

快速发送了 100 个元素，最后接收到的只有两个，当然这个结果每次都不一定一样：

```kotlin
Collecting 1
1 collected
Collecting 99
99 collected
```

第二种是 collectLatest。顾名思义，只处理最新的数据，这看上去似乎与 conflate 没有区别，其实区别大了：它并不会直接用新数据覆盖老数据，而是每一个都会被处理，只不过如果前一个还没被处理完后一个就来了的话，处理前一个数据的逻辑就会被取消。

**代码清单 18：使用 collectLatest 解决背压问题**	

```kotlin
flow {
  List(100) {
    emit(it)
  }
}.collectLatest { value ->
  println("Collecting $value")
  delay(100)
  println("$value collected")
}
```

运行结果如下：

```kotlin
Collecting 0
Collecting 1
...
Collecting 97
Collecting 98
Collecting 99
▶ 100ms later
99 collected
```

前面的 Collecting 输出了 0 ~ 99 的所有结果，而 collected 却只有 99，因为后面的数据到达时，处理上一个数据的操作正好被挂起了（请注意 delay(100)）。

除 collectLatest 之外还有 mapLatest、flatMapLatest 等等，都是这个作用。

### 11.10. Flow 的变换

Flow 的变换与 RxJava 的 Observable 的表现也基本一致。

例如可以使用 map 来变换 Flow 的数据：

**代码清单 19：Flow 的元素变换**	

```kotlin
flow {
  List(5){ emit(it) } 
}.map { 
  it * 2
}
```

也可以映射成其他 Flow:

**代码清单 20：Flow 的嵌套**	

```kotlin
flow {
  List(5){ emit(it) } 
}.map {
  flow { List(it) { emit(it) } }
}
```

这实际上得到的是一个数据类型为 Flow 的 Flow，如果希望将它们拼接起来，可以使用 flattenConcat：

**代码清单 21：拼接 Flow**	

```kotlin
flow {
  List(5){ emit(it) } 
}.map {
  flow { List(it) { emit(it) } }
}.flattenConcat()
  .collect { println(it) }
```

拼接的操作中 flattenConcat 是按顺序拼接的，结果的顺序仍然是生产时的顺序；还有一个是 flattenMerge，它会并发拼接，因此结果不会保证顺序。

### 11.11. 使用 Flow 实现多路复用

多数情况下，可以通过构造合适的 Flow 来实现多路复用的效果。

**代码清单 22：使用 Flow 实现对 await 的多路复用**

```kotlin
coroutineScope {
  val login = "..."
  listOf(::getUserFromApi, ::getUserFromLocal) ... ①
    .map { function ->
      function.call(login) ... ②
    }
    .map { deferred ->
      flow { emit(deferred.await()) } ... ③
    }
    .merge() ... ④
    .onEach { user ->
      println("Result: $user")
    }.launchIn(this)
}
```

这其中，① 处用创建了两个函数引用组成的 List；② 处调用了它们得到 deferred；③ 处比较关键，对于每一个 deferred 创建了一个单独的 Flow，并在 Flow 内部发送 deferred.await() 返回的结果，即返回的 User 对象；现在有了两个 Flow 实例，需要将它们整合成一个 Flow 进行处理，调用 merge 函数即可。

![](https://kotlinblog-1251218094.costj.myqcloud.com/9e300468-a645-433d-ae41-60b3eaa97f5a/media/9ff28c5395881742a6878225807e2dd75c150d63.png)

图：使用 merge 合并 Flow

同样的，对 Channel 的读取复用的场景也可以使用 Flow 来完成。

**代码清单 23：使用 Flow 实现对 Channel 的复用**

```kotlin
val channels = List(10) { Channel<Int>() }
...
val result = channels.map {
    it.consumeAsFlow()
  }
  .merge()
  .first()
```

这比 select 的版本看上去要更简洁明了，每个 Channel 都通过 consumeAsFlow 函数被映射成 Flow，再 merge 成一个 Flow，取第一个元素。

Flow 是协程当中比较重要的异步工具，它的用法与其他类似的响应式编程框架非常相近。

## 12. 协程为什么被称为“轻量级线程”？

### 12.1. 协程是什么？

协程的概念最核心的点其实就是函数或者一段程序能够被挂起（说暂停其实也没啥问题），待会儿再恢复，挂起和恢复是开发者的程序逻辑自己控制的，协程是通过主动挂起出让运行权来实现协作的。

它跟线程最大的区别在于线程一旦开始执行，从任务的角度来看，就不会被暂停，直到任务结束这个过程都是连续的，线程之间是抢占式的调度，因此也不存在协作的问题。

那么再来理一理协程的概念：

* 挂起恢复
* 程序自己处理挂起恢复
* 程序自己处理挂起恢复来实现协程的协作运行

关键核心就是协程是一个能挂起并且待会儿恢复执行的东西。

### 12.2. 为什么协程的概念会有混乱的感觉？

Java 对线程做了很好的支持，这也是 Java 在高并发场景风生水起的一个关键支柱，虚拟机底层对线程的支持，例如 Android 虚拟机，其实就是 pthread。Java 的 Object 还有一个 wait 方法，这个方法几乎支撑了各种锁的实现，它底层是 condition。

绝大多数协程都是语言层面自己的实现，不同的编程语言有不同的使用场景，自然在实现上也看似有很大的差异，甚至还有的语言自己没有实现协程，单开发者通过第三方框架的方式提供了协程的能力，例如 Java 的框架 Quasar，加上协程实现本身在操作系统层面就有过一系列演进，因此出现了虽然理论上看起来很简单，单实现上却多样化的局面。

### 12.3. 协程有哪些主流的实现？

按照有没有栈来分，即：

*  有栈协程 Stackful Coroutine：每一个协程都会有自己的调用栈，有点儿类似于线程的调用栈，这种情况下的协程实现其实很大程度上接近线程，主要不同体现在调度上。
* 无栈协程 Stackless Coroutine：协程没有自己的调用栈。

递归调用函数的层次太多就会导致 StackOverflowException，因为栈内存是有限的；程序出现了异常总是希望看到异常点的调用关系，这样方便定位问题，这也需要栈。

有栈协程有什么好处呢？因为有栈，所以在任何一个调用的地方运行时都可以选择把栈保存起来，暂停这个协程，听起来就跟线程一样了，只不过挂起和恢复执行的权限在程序自己，而不是操作系统。缺点也是非常明显的，每创建一个协程不管有没有在运行都要为它开辟一个栈，这也是目前无栈协程流行的原因。

goroutine 看上去似乎不像协程，因为开发者自己无法决定一个协程的挂起和恢复，这个工作是 go 运行时自己处理的。为了支持 goroutine 在任意位置能挂起，goroutine 其实是一个有栈协程，go 运行时在这里做了大量的优化，它的栈内存可以根据需要进行扩容和缩容，最小一般为内存页长 4KB。

JavaScript、C# 还有 Python 的协程，或者干脆就说 async/await，相比之下就轻量多了，它们看起来更像是针对回调加了个语法糖的支持 -- 它们其实就是无栈协程的实现了。无栈，顾名思义，每一个协程都不会单独开辟调用栈，那么问题来了，它的上下文是如何保存的？

CPS，即 continuation-passing-style。程序被挂起，或者说中断，最关键的是什么？是保存挂起点，或者中断点，对于线程被操作系统中断，中断点就是被保护在调用栈当中的，而无栈协程要保存到哪儿呢？保存到 Continuation 对象当中，这个东西可能在不同的语言当中叫法不一样，但本质上都是一个 Continuation，它就是一个普通的对象，占用内存非常小，还是很抽象，常见的 Callback，它其实就是一个 Continuation 的实现。

Kotlin 的协程的根基就是一个叫做 Continuation 的类，长得就是一个回调，resume 就是 onSuccess，resumeWithexception 就是 onFailure。

Continuation 携带了协程继续执行所需要的上下文，同时它自己又是挂起点，因为待会儿恢复执行的时候只需要执行它回调的函数体就可以了。对于 Kotlin 来讲，每一个 suspend 函数都是一个挂起点，意味着对于当前协程来说，每遇到一个 suspend 函数的调用，它都有可能会被挂起。每一个 suspend 函数都被编译器插入了一个 Continuation 类型的参数来保存当前的调用点。

```kotlin
suspend fun hello() = suspendCoroutine<Int>{ continuation ->
    println("Hello")
    continuation.resumeWith(Result.success(10086))
}
```

定义了一个 suspend 函数 hello，它看起来没有接收任何参数，如果真是这样，在后面调用的 resumeWith 的 continuation 是哪里来的？

都说挂起函数必须在协程内部调用，其实也不是，用 Java 代码直接取调用 suspend 函数，会发现这些 suspend 函数都需要传入一个额外的 Continuation，就是这个意思。

当然，Java 也不是必须的，需要用点儿 Kotlin 反射，一样可以让 suspend 函数现出原形：

```kotlin
val helloRef = ::hello
val result = helloRef.call(object: Continuation<Int>{
    override val context = EmptyCoroutineContext

    override fun resumeWith(result: Result<Int>) {
        println("resumeWith: ${result.getOrNull()}")
    }
})
```

虽然没有办法直接调用 hello()，但可以拿到它的函数引用，用反射调用它，调用的时候如果什么参数都不传，编译器就会提示你它需要一个参数 -- 需要的这个参数正是 Continuation。

这段代码不需要运行在协程体内，或者其他的 suspend 函数中。为什么官方要求 suspend 函数一定要运行在协程体内或者其他 suspend 函数中呢？

答案自然就是任何一个协程体或者 suspend 函数中都有一个隐含的 Continuation 实例，编译器能够对这个实例进行正确传递，并将这个细节隐藏在协程的背后，让一步代码看起来像同步代码一样。

Kotlin 协程的本质：它是一种无栈协程实现，它的本质就是一段代码 + Continuation 实例。

### 12.4. Kotlin 协程真的只是一个线程框架吗？

协程的世界可以没有线程，如果操作系统的 CPU 调度模型是协程的话；反过来也成立。Kotlin 协程是不是可以没有线程呢？至少从 Java 虚拟机的实现上来看是不太行，不过这不是 Kotlin 协程的问题，是 Java 虚拟机的问题。

Kotlin 除了支持 Java 虚拟机之外，还支持 JavaScript，还支持 Native。JavaScript 无论是跑在 Web 还是 Node.js 当中，都是单线程玩耍的；Kotlin Native 虽然可以调用 pthread，但官方表示有自己的兵法模型（Worker），不建议直接使用线程。在这两个平台跑，Kotlin 的协程其实都是单线程的，又怎么讲是个线程框架呢？

单线程要协程能做什么呢？异步不一定要多线程。Android 刚创建的时候想要拿到一个 View 的大小一般返回都是 0，因为 Activity 的布局是在 onResume 方法调用之后完成的，所以 handle.post 一下就好了：

```kotlin
override fun onResume(){
    super.onResume()
    handler.post {
        val width = myView.width
        ...
    }
}
```

这就是异步代码，但这代码其实都运行在主线程的，当然可以用协程改写一下：

```kotlin
override fun onResume() {
    super.onResume()
    GlobalScope.launch(Dispatchers.Main) {
        val width = handler.postSuspend {
            myView.width
        }
        Log.d("MyView", width.toString())
    }
}

suspend fun <T> Handler.postSuspend(block: () -> T) = suspendCoroutine<T> {
    post {
        it.resume(block())
    }
}
```

其实个人觉得如果 Kotlin 协程的默认的调度器是 Main，并且这个 Main 会根据各自平台选择一个合适的事件循环，这样更能体现 Kotlin 协程在不同平台的一致性，例如对于 Android 来说 Main 就是 UI 线程上的事件循环，对于 Swing 的 UI 事件循环，只要是有事件循环的平台就默认基于这个循环来一个调度器，没有默认事件循环的也好办，Kotlin 协程本身就有 runBlocking，对于普通 Java 程序来说没有事件循环就给它构造一个就行了。

Kotlin 协程的设计者没有这么做，他们当然也有他们的道理，毕竟他们不愿意抢播开发者一定要用协程，甚至立刻马上就得对原有得代码进行改造，他们希望 Kotlin 只是一门编程语言，一门提供足够安全保障和灵活语法的编程语言，剩下的交给开发者去选择。

### 12.5. 协程真的比线程有优势吗？

Kotlin 协程刚出来的时候，有人就做过性能对比，觉得协程没有任何性能优势。在一些场景确实用协程不会有任何性能上的优势，这就好比需要再一个单核上跑一个计算密集型的程序还要开多个线程跑一样，任何特性都有适合它的场景和不适合它的领域。

协程比线程轻量，编程语言级别实现的协程就是程序内部的逻辑，不会设计操作系统的资源之间的切换，操作系统的内核线程自然会重一些，且不说每创建一个线程就会开辟的栈带来的内存开销，线程在上下文切换的时候需要 CPU 把高速缓存清掉并从内存中替换下一个线程的内存数据，并且处理上一个内存的中断点保存就是一个开销很大的事儿。

线程除了包含内存线程本身执行代码能力的含义以外，通常也被赋予了逻辑任务的概念，所以协程是一种轻量级的 【线程】的说法，更多描述的是它的使用场景。

**协程更像一种轻量级的【线程】。**

这样说更贴切一些。线程自然可以享受到并行计算的优待，协程则只能依赖程序内部的线程来实现并行计算。协程的优势其实更多是体现在 IO 密集型程序上，这对于 Java 开发者来说可能又是一个很迷惑的事情，很少有人用上 NIO，绝大多数都是用 BIO 来读写 IO，因此不管开线程还是开协程，读写 IO 的时候总是要有一个线程在等待 IO，所以看上去似乎也没有什么区别。但用 NIO 就不一样了，IO 不阻塞，通过开一个和很少的几个线程来 select IO 的事件，有 IO 事件到达时再分配相应的线程去读写 IO，比起传统的 IO 就已经又了很大的提升。

用了 NIO 以后，本身就可以减少线程的使用。可是协程呢？协程可以给予这个思路进一步简化代码的组织，虽然线程就能解决问题，但写起来其实是很累的，协程可以更轻松，特别是遇到多个任务需要访问公共资源时，如果每隔任务都分配一个线程去处理，那么少不了就有线程会花费大量的时间在等待获取锁上，但如果用协程来承载任务，用极少量的线程来承载协程，那么锁优化就变简单了：协程如果无法获取到锁，那么协程挂起，对应的线程就可以让出去运行其他协程了。

线程可以让程序兵法的跑，协程可以让并发程序跑的看起来更美好。

不管是异步代码同步化，还是并发代码简介化，协程的出现其实是为代码从计算机向人类思维的贴近提供了可能。

## 13. 协程的几种常见的实现

### 13.1. 协程的分类

#### 13.1.1. 按调用栈分类

由于协程需要支持挂起、恢复，因此对于挂起点的状态保存就显得极其关键。类似地，线程会因为 CPU 调度权的切换而被中断，它的中断状态会保存在调用栈当中，因为协程的实现也按照是否开辟相应的调用栈存在以下两种类型：

* 有栈协程 Stackful Coroutine：每一个协程都会有自己的调用栈，有点儿类似于线程的调用栈，这种情况下的协程实现其实很大程度上接近线程，主要不同体现在调度上。
* 无栈协程 Stackless Coroutine：协程没有自己的调用栈，挂起点的状态通过状态机或者闭包等语法来实现。

有栈协程的优点就是可以在任意函数调用层级的任意位置进行挂起，并转移调度权，例如 Lua 的协程，这方面多数无栈协程就显得力不从心了，例如 Python 的 Generator；通常来讲，有栈协程因为总是会给协程开辟一块儿栈内存，因此内存开销也相对可观，而无栈协程在内存方面就比较有优势了。

当然也有反例。

Go 语言的 go routine 可以认为是有栈协程的一个实现，不过 Go 运行时在这里做了大量的优化，它的栈内存可以根据需要进行扩容和缩容，最小一般为内存页长 4kb，相比之下线程的栈空间通常是 MB 级别，因而它在内存方面的表现也相对轻量。

Kotlin 的协程是一种无栈协程的实现，它的控制流转依靠对协程体本身编译生成的状态机的状态流转来实现，变量保存也是通过闭包语法来实现的，不过 Kotlin 的协程可以在任意调用层次挂起，换句话说启动一个 Kotlin 协程，可以在其中任意嵌套 suspend 函数，而这又恰恰是有栈协程最重要的特性之一：

```kotlin
suspend fun level_0() {
    println("I'm in level 0!")
    level_1() // ............ ①
}

suspend fun level_1() {
    println("I'm in level 1!")
    suspendNow() // ............ ②
}

suspend fun suspendNow() 
        = suspendCoroutine<Unit> {
    ... 
}
```

示例中 ① 处并没有真正直接挂起，② 处的调用才会真正挂起，Kotlin 通过 suspend 函数嵌套调用的方式可以实现任意函数调用层次的挂起。

当然，想要在任意位置挂起，那就需要调用栈了，与开发者通过调用 API 显式地挂起协程相比，任意位置的挂起主要用于运行时对协程执行的干预，这种挂起方式对于开发者不可见，因而是一种隐式的挂起操作。Go 语言的 go routine 可以通过 channel 的读写来实现挂起和恢复，除了这种显式地切换调度权之外，Go 运行时还会对长期占用调度权的 go routine 进行隐式挂起，并将调度权转移给其他 go routine，这实际上就是线程的抢占式调度了。

#### 13.1.2. 按调用方式分类

调度过程中，根据协程转移调度权的目标又将协程分为对称协程和非对称协程：

* 对称协程 Symmetric Coroutine：任何一个协程都是相互独立且平等的，调度权可以在任意协程之间转移。
* 非对称协程 Asymmetric Coroutine：协程出让调度权的目标只能是它的调用者，即协程之间存在调用和被调用关系。

对称协程实际上已经非常接近线程的样子了，例如 Go 语言中的 go routine 可以通过读写不同的 channel 来实现控制权的自由转移。而非对程协程的调用关系实际上也更符合思维方式，常见的语言对协程的实现大多是非对程实现，例如 Lua 的协程中当前协程调用 yield 总是会将调度权转移给 resume 它的协程；还有就是 async/await，await 时将调度权转移到异步调用中，异步调用返回结果或抛出异常时总是将调度权转换回 await 的位置。

从实现的角度来讲，非对称协程的实现更自然，也相对容易；不过，只要对非对程协程稍作修改，即可实现对称协程的能力。在非对称协程的基础上，只需要添加一个中立的第三方作为协程调度权的分发中心，所有的协程在挂起时都将控制权转移给分发中心，分发中心根据参数来决定将调度权转移给哪个协程，例如 Lua 的第三方库 coro，以及 Kotlin 协程框架中基于 Channel 的通信等。

### 13.2. 协程的实现举例

简单来说协程需要关注的就是程序自己处理挂起和恢复，不过在分类的时候又根据解决挂起和恢复时具体实现细节的不同又区分了按照栈的有无和调度权转移的对称性的分类。不管怎样，协程的关注点就是程序自己处理挂起和恢复。

#### 13.2.1. Python 的 Generator

Python 的 Generator 也是协程，是一个典型的无栈协程的实现，可以在任意 Python 函数中调用 yield 来实现当前函数调用的挂起，yield 的参数作为对下一次 next(num_generator) 调用的返回值：

```python
import time

def numbers():
    i = 0
    while True:
        yield(i) # ..................... ①
        i += 1
        time.sleep(1)

num_generator = numbers()

print(f"[0] {next(num_generator)}") # ... ②
print(f"[1] {next(num_generator)}") # ... ③

for i in num_generator: # ............... ④
    print(f"[Loop] {i}")
```

所以运行这段程序时，首先会在 ① 处 yield，并将 0 传出，在 ② 处输出：

```kotlin
[0] 0
```

接着自 ③ 处调用 next，将调度权从主流程转移到 numbers 函数当中，从上一次挂起的位置 ①  处继续执行，i 的值修改为 1，1s 后，再次通过 yield(1) 挂起，③ 处输出：

```kotlin
[1] 1
```

后续就以同样的逻辑在 for 循环中一直输出 [Loop] n，直到程序被终止。

之所以称 Python 的 Generator 为协程，就是因为它具备了通过 yield 来挂起当前 Generator 函数的执行，通过 next 来恢复参数对应的 Generator 执行来实现挂起、恢复的协程调度权控制转移的。

当然，如果在 numbers 函数中嵌套调用 yeld，就无法对 numbers 的调用进行中断了：

```python
def numbers():
    i = 0
    while True:
        yield_here(i) # ................. ①
        i += 1
        time.sleep(1)

def yield_here(i):
    yield(i)
```

这时候再调用 numbers 函数，就会潜入死循环而无法返回，因为这次 yield_here 的返回值才是 Generator。

说明 Python 的 Generator 属于非对称无栈协程的一种实现。从 Python 3.5 开始也支持 async/await，原理与 JavaScript 的实现类似，与 Generator 的不同之处在于可以通过这一组关键字实现再函数嵌套调用挂起。

#### 13.2.2. Lua 标准库的协程实现

Lua 的协程实现可以认为是一个教科书式的案例了，它提供了几个 API 允许开发者灵活控制协程的执行：

* coroutine.create: 创建协程，参数为函数，作为协程的执行体，返回协程实例。
* coroutine.yield：挂起协程，第一个参数为被挂起的协程实例，后面的参数则作为外部调用 resume 来继续当前协程时的返回值，而它的返回值则又是外部下一次 resume 调用时传入的参数。
* coroutine.resume：继续协程，第一个参数为被继续的协程实例，后面的参数则作为协程内部 yield 时的返回值，返回值则为协程内部下一次 yield 时传出的参数；如果是第一次对该协程实例执行 resume，参数回作为协程函数的参数传入。

Lua 的协程也有几个状态，挂起（suspended）、运行（running）、结束（dead）。其中，调用 yield 之后的协程处于挂起态，获得执行权而正在运行的协程则是处于运行态，协程对应的函数运行结束后，则处于结束态。

```lua
function producer() 
    for i = 0, 3 do
        print("send "..i)
        coroutine.yield(i) -- ④
    end
    print("End Producer")
end            

function consumer(value)
    repeat
        print("receive "..value)
        value = coroutine.yield() -- ⑤
    until(not value)
    print("End Consumer")
end

producerCoroutine = coroutine.create(producer) -- ①
consumerCoroutine = coroutine.create(consumer) -- ②

repeat
    status, product = coroutine.resume(producerCoroutine) -- ③
    coroutine.resume(consumerCoroutine, product) -- ⑥
until(not status)
print("End Main")
```

这段代码在 ①、② 两处创建协程，③ 处开始执行，producer 在 ④ 处 yield(0)，意味着 ③ 的返回值 product 就是 0，把 0 作为参数又传给 consumer，第一次 resume 参数 0 会作为 consumer 的参数 value 传入，因此会打印出：

```
send 0
receive 0
```

接下来 consumer 通过 ⑤ 处的 yield 挂起，它的参数会作为 ⑥ 处的返回值，不过没有传任何参数。这时控制权又回到主流程，status 的值在对应的协程结束后会返回 false，这时候 producer 尚未结束，因此是 true，于是循环继续执行，后续流程类似，输出结果：

```
send 1
receive 1
send 2
receive 2
send 3
receive 3
End Producer
End Consumer
End Main
```

通过这个例子，能够对协程有一个更加具体的认识，对于协程来讲，它包括：

* 协程的执行体，主要是指启动协程时对应的函数
* 协程的控制实例，可以通过协程创建时返回的实例控制协程的调用流转
* 协程的状态，在调用流程转移前后，协程的状态会发生相应的变化

说明 Lua 标准库的协程属于非对称有栈协程，不过第三方提供了基于标准库的对称协程的实现 coro。这也是对称协程的实现可以基于非对称协程来实现的例子。

#### 13.2.3. Go 语言中的 go routine

go routine 的调度没有 Lua 那么明显，没有类似 yield 和 resume 的函数。

```go
channel := make(chan int) // .......... ①
var readChannel <-chan int = channel
var writeChannel chan<- int = channel

// reader
go func() { // ........................ ②
    fmt.Println("wait for read")
    for i := range readChannel { // ... ③
        fmt.Println("read", i)
    }
    fmt.Println("read end")
}()  // ............................... ④


// writer
go func() {
    for i := 0; i < 3; i++{
        fmt.Println("write", i)
        writeChannel <- i // .......... ⑤
        time.Sleep(time.Second)
    }
    close(writeChannel)
}()
```

在任意函数调用前面加关键字 go 即可启动一个 go routine，并在该 go routine 中调用这个函数，例如 ② 处实际上是创建了一个匿名函数，并在后面 ④ 处立即调用了该函数。把这两个 go routine 依次称为 “reader” 和 “writer”。

① 处创建了一个双向的 channel，可读可写，接着创建的 readChannel 声明为只读类型，writeChannel 声明为只写类型，这二者实际上是同一个 channel，并且由于这个 channel 没有缓冲区，因此写操作会一直挂起直到读操作执行，反过来也是如此。

在 reader 中，③ 处的 for 循环会对 readChannel 进行读操作，如果此时还没有对应的写操作，就会挂起，直到有数据写入；在 writer 中，⑤ 处表示向 writeChannel 中写入 i，同样，如果写入时尚未有对应的读操作，就会挂起，直到有数据读取。整段程序的输出如下：

```
wait for read
write 0
read 0
write 1
read 1
write 2
read 2
read end
```

如果有多个 go routine 对 channel 进行读写，或者有多个 channel 供多个 go routine 读写，那么这时的读写操作实际上就是在 go routine 之间平等的转移调度权，因此可以认为 go routine 是对称的协程实现。

这个示例看上去对于 channel 的读写操作有点儿类似两个线程中的阻塞时 IO 操作，不过 go routine 相对操作系统的内核线程来说要轻量的多，切换的成本也很低，因此在读写过程中挂起的成本也远比线程阻塞的调用切换成本低。实际上这两个 go routine 在切换时，很大概率不会有线程的切换，为了让示例更加能说明问题，为输出添加了当前的线程 id，同时将每次向 writeChannel 写入数据之后的 Sleep 操作去掉：

```kotlin
go func() {
    fmt.Println(windows.GetCurrentThreadId(), "wait for read")
    for i := range readChannel {
        fmt.Println(windows.GetCurrentThreadId(), "read", i)
    }
    fmt.Println(windows.GetCurrentThreadId(), "read end")
}()
go func() {
    for i := 0; i < 3; i++{
        fmt.Println(windows.GetCurrentThreadId(), "write", i)
        writeChannel <- i
    }
    close(writeChannel)
}()
```

修改后的运行结果可以看到程序在输出时所在的线程 id：

```
181808 write 0
183984 wait for read
181808 read 0
181808 write 1
181808 write 2
181808 read 1
181808 read 2
181808 read end
```

两个 go routine 除了开始运行时占用了两个线程，后续都在一个线程中转移调度权（不同场景的实际运行结果可能有细微差异，这取决了 Go 运行时的调度器）。

**获取线程 id** 本例在 windows 上调试，通过 sys 库的 windows 包下提供的 GetCurrentThreadId 函数来获取线程 id。Linux 系统可以通过 sys call.Gettid 来获取。

**说明**虽然一直在用 go routine 做例子，并把它称作为对称有栈协程的一种实现，但考虑到 Go 运行时本身做了足够多超出其他语言的能力，例如栈优化，调度优化等，特别是调度器还支持特定场景下的抢占式调度，某种意义上已经超越了协程概念的讨论范围，因此也有很多人认为 go routine 不能简单的认为就是协程。

不管怎么分类，协程的本质就是程序自己处理挂起和恢复。协程描述了多个程序之间如何通过相互出让运行调度权来完成执行，基于这一对基本的控制转移操作进而衍生出各种异步模型，并发模型例如 async/await，Channel 等。

相比之下，Kotlin 的协程没有其他语言的 async/await 那么容易上手，也没有 go routine 那么容易使用，原因也很简单，Kotlin 的协程用一个最基本的 suspend 关键字来支持了最基本的挂起恢复逻辑，进而在上层封装，衍生出了以上提到的几乎所有的模型，在 Kotlin 当中可以有机会使用 async/await、Channel，以及最新出的 Flow API，将来还会有更多。

## 参考文章

1. [破解 Kotlin 协程（1）：入门篇](https://www.bennyhuo.com/2019/04/01/basic-coroutines/)
1. [破解 Kotlin 协程（2）：协程启动篇](https://www.bennyhuo.com/2019/04/08/coroutines-start-mode/)
1. [破解 Kotlin 协程（3）：协程调度篇](https://www.bennyhuo.com/2019/04/11/coroutine-dispatchers/)
1. [破解 Kotlin 协程（4）：异常处理篇](https://www.bennyhuo.com/2019/04/23/coroutine-exceptions/)
1. [破解 Kotlin 协程（5）：协程取消篇](https://www.bennyhuo.com/2019/04/30/coroutine-cancellation/)
1. [破解 Kotlin 协程（6）：协程挂起篇](bennyhuo.com/2019/05/07/coroutine-suspend/)
1. [破解 Kotlin 协程（7）：序列生成器篇](https://www.bennyhuo.com/2019/05/26/coroutine-sequence/)
1. [破解 Kotlin 协程（8）：Android 篇](https://www.bennyhuo.com/2019/05/27/coroutine-android/)
1. [破解 Kotlin 协程（9）：Channel 篇](https://www.bennyhuo.com/2019/09/16/coroutine-channel/)
1. [破解 Kotlin 协程（10）：Select 篇](https://www.bennyhuo.com/2020/02/03/coroutine-select/)
1. [破解 Kotlin 协程（11）：Flow 篇](https://www.bennyhuo.com/2020/03/14/coroutine-flow/)
1. [破解 Kotlin 协程（12）：协程为什么被称为『轻量级线程』？](https://www.bennyhuo.com/2019/10/19/coroutine-why-so-called-lightweight-thread/)
1. [破解 Kotlin 协程（13）：协程的几类常见的实现](https://www.bennyhuo.com/2019/12/01/coroutine-implementations/)
