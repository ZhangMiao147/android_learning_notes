# Kotlin 协程疑问

> 简介
> 
> 1. 协程有哪些主流的实现
>    按照有没有栈来分：
>    
>    * 有栈协程：每一个协程都有自己的调用栈，这种情况下的协程实现其实很大程度上接近线程，主要不同体现在调度上。
>      
>      比如 goroutine。
>    
>    * 无栈协程：协程没有自己的调用栈。
>      
>      比如 JavaScript、C# 还有 Python 的协程，Kotlin 协程
>    
>    Kotlin 协程的本质：它是一种无栈协程实现，它的本质就是一段代码+Continuation 实例。
> 
> 2. Kotlin 协程真的只是一个线程框架吗？
>    
>    不是，
> 
> 3. 协程真的比线程有优势吗？
>    
>    协程的优势更多体现在 IO 密集型程序上

## 1. 协程有哪些主流的实现

按照有没有栈来分，即：

- 有栈协程 Stackful Coroutine：每一个协程都会有自己的调用栈，有点儿类似于线程的调用栈，这种情况下的协程实现其实很大程度上接近线程，主要不同体现在调度上。
- 无栈协程 Stackless Coroutine：协程没有自己的调用栈。

递归调用函数的层次太多就会导致 StackOverflowException，因为栈内存是有限的；程序出现了异常总是希望看到异常点的调用关系，这样方便定位问题，这也需要栈。

有栈协程有什么好处呢？因为有栈，所以在任何一个调用的地方运行时都可以选择把栈保存起来，暂停这个协程，听起来就跟线程一样了，只不过挂起和恢复执行的权限在程序自己，而不是操作系统。缺点也是非常明显的，每创建一个协程不管有没有在运行都要为它开辟一个栈，这也是目前无栈协程流行的原因。

goroutine 看上去似乎不像协程，因为开发者自己无法决定一个协程的挂起和恢复，这个工作是 go 运行时自己处理的。为了支持 goroutine 在任意位置能挂起，goroutine 其实是一个有栈协程，go 运行时在这里做了大量的优化，它的栈内存可以根据需要进行扩容和缩容，最小一般为内存页长 4KB。

JavaScript、C# 还有 Python 的协程，或者干脆就说 async/await，相比之下就轻量多了，它们看起来更像是针对回调加了个语法糖的支持 -- 它们其实就是无栈协程的实现了。无栈，顾名思义，每一个协程都不会单独开辟调用栈，那么问题来了，它的上下文是如何保存的？

CPS，即 continuation-passing-style。程序被挂起，或者说中断，最关键的是什么？是保存挂起点，或者中断点，对于线程被操作系统中断，中断点就是被保护在调用栈当中的，而无栈协程要保存到哪儿呢？保存到 Continuation 对象当中，这个东西可能在不同的语言当中叫法不一样，但本质上都是一个 Continuation，它就是一个普通的对象，占用内存非常小，还是很抽象，常见的 Callback，它其实就是一个 Continuation 的实现。

Kotlin 的协程的根基就是一个叫做 Continuation 的类，长得就是一个回调，resume 就是 onSuccess，resumeWithexception 就是 onFailure。

Continuation 携带了协程继续执行所需要的上下文，同时它自己又是挂起点，因为待会儿恢复执行的时候只需要执行它回调的函数体就可以了。对于 Kotlin 来讲，每一个 suspend 函数都是一个挂起点，意味着**对于当前协程来说，每遇到一个 suspend 函数的调用，它都有可能会被挂起。每一个 suspend 函数都被编译器插入了一个 Continuation 类型的参数来保存当前的调用点。**

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

**Kotlin 协程的本质：它是一种无栈协程实现，它的本质就是一段代码 + Continuation 实例。**

<mark>Continuation是什么？</mark>

## 2. Kotlin 协程真的只是一个线程框架吗？

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

Kotlin 协程的设计者没有这么做，他们当然也有他们的道理，毕竟他们不愿意强迫开发者一定要用协程，甚至立刻马上就得对原有得代码进行改造，他们希望 Kotlin 只是一门编程语言，一门提供足够安全保障和灵活语法的编程语言，剩下的交给开发者去选择。

## 3. 协程真的比线程有优势吗？

Kotlin 协程刚出来的时候，有人就做过性能对比，觉得协程没有任何性能优势。在一些场景确实用协程不会有任何性能上的优势，这就好比需要在一个单核上跑一个计算密集型的程序还要开多个线程跑一样，任何特性都有适合它的场景和不适合它的领域。

协程比线程轻量，编程语言级别实现的协程就是程序内部的逻辑，不会涉及操作系统的资源之间的切换，操作系统的内核线程自然会重一些，且不说每创建一个线程就会开辟的栈带来的内存开销，线程在上下文切换的时候需要 CPU 把高速缓存清掉并从内存中替换下一个线程的内存数据，并且处理上一个内存的中断点保存就是一个开销很大的事儿。

线程除了包含内存线程本身执行代码能力的含义以外，通常也被赋予了逻辑任务的概念，所以协程是一种轻量级的 【线程】的说法，更多描述的是它的使用场景。

**协程更像一种轻量级的【线程】。** 这样说更贴切一些。线程自然可以享受到并行计算的优待，协程则只能依赖程序内部的线程来实现并行计算。**协程的优势其实更多是体现在 IO 密集型程序上**，这对于 Java 开发者来说可能又是一个很迷惑的事情，很少有人用上 NIO，绝大多数都是用 BIO 来读写 IO，因此不管开线程还是开协程，读写 IO 的时候总是要有一个线程在等待 IO，所以看上去似乎也没有什么区别。但用 NIO 就不一样了，IO 不阻塞，通过开一个和很少的几个线程来 select IO 的事件，有 IO 事件到达时再分配相应的线程去读写 IO，比起传统的 IO 就已经又了很大的提升。

用了 NIO 以后，本身就可以减少线程的使用。可是协程呢？协程可以给予这个思路进一步简化代码的组织，虽然线程就能解决问题，但写起来其实是很累的，协程可以更轻松，特别是遇到多个任务需要访问公共资源时，如果每隔任务都分配一个线程去处理，那么少不了就有线程会花费大量的时间在等待获取锁上，但如果用协程来承载任务，用极少量的线程来承载协程，那么锁优化就变简单了：协程如果无法获取到锁，那么协程挂起，对应的线程就可以让出去运行其他协程了。

线程可以让程序并发的跑，协程可以让并发程序跑的看起来更美好。

不管是异步代码同步化，还是并发代码简介化，协程的出现其实是为代码从计算机向人类思维的贴近提供了可能。

## 参考文章

1. [破解 Kotlin 协程（1）：入门篇](https://www.bennyhuo.com/2019/04/01/basic-coroutines/)
2. [破解 Kotlin 协程（2）：协程启动篇](https://www.bennyhuo.com/2019/04/08/coroutines-start-mode/)
3. [破解 Kotlin 协程（3）：协程调度篇](https://www.bennyhuo.com/2019/04/11/coroutine-dispatchers/)
4. [破解 Kotlin 协程（4）：异常处理篇](https://www.bennyhuo.com/2019/04/23/coroutine-exceptions/)
5. [破解 Kotlin 协程（5）：协程取消篇](https://www.bennyhuo.com/2019/04/30/coroutine-cancellation/)
6. [破解 Kotlin 协程（6）：协程挂起篇](bennyhuo.com/2019/05/07/coroutine-suspend/)
7. [破解 Kotlin 协程（7）：序列生成器篇](https://www.bennyhuo.com/2019/05/26/coroutine-sequence/)
8. [破解 Kotlin 协程（8）：Android 篇](https://www.bennyhuo.com/2019/05/27/coroutine-android/)
9. [破解 Kotlin 协程（9）：Channel 篇](https://www.bennyhuo.com/2019/09/16/coroutine-channel/)
10. [破解 Kotlin 协程（10）：Select 篇](https://www.bennyhuo.com/2020/02/03/coroutine-select/)
11. [破解 Kotlin 协程（11）：Flow 篇](https://www.bennyhuo.com/2020/03/14/coroutine-flow/)
12. [破解 Kotlin 协程（12）：协程为什么被称为『轻量级线程』？](https://www.bennyhuo.com/2019/10/19/coroutine-why-so-called-lightweight-thread/)
13. [破解 Kotlin 协程（13）：协程的几类常见的实现](https://www.bennyhuo.com/2019/12/01/coroutine-implementations/)
