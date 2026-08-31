# Kotlin 协程概念

> 简介
> 
> 1. 线程是什么
>    
>    线程是操作系统调用的执行单元，有独立的调用栈、程序计数器。
> 
> 2. 协程是什么
>    
>    协程是跑在线程上的轻量级任务。
>    
>    协程是一种非抢占式或者说协作式的计算机程序并发调度的实现，程序可以主动挂起或者恢复执行。
>    
>    线程之间是抢占式的调度。
> 
> 3. 协程的本质
>    
>    suspend fun 编译后不是新线程，而是状态机+continuation。
>    协程的【暂停/继续】是函数级别的，不是 OS 级别的线程阻塞。
>    很多线程复用少量线程，挂起时不占线程，线程可以去跑别的协程。
>    Job 树、取消传播、SupervisorJob 等这些结构化并发的能力，协程做成了库的一部分，线程只能自行处理。

## 1. 线程是什么

线程是操作系统调用的执行单元：

* 有独立的调用栈、程序计数器

* 由 OS 内核做抢占式调度（时间片到了就切换，线程本身不用配合）

* 创建、切换成本较高（通常 MB 级栈、内核态切换）

* 数量一多，上下文切换、锁竞争会明显拖慢性能

## 2. 协程是什么

协程是跑在线程上的轻量级任务。

协程 = 可以被挂起（suspend）和恢复（resume）的计算，由运行时在线程上协作式调度。

协程是一种非抢占式或者说协作式的计算机程序并发调度的实现，程序可以主动挂起或者恢复执行。

Java 虚拟机上的线程大多数实现是映射到内核的线程的，也就是说线程当中的代码逻辑在线程抢到 CPU 的时间片的时候才可以执行，否则就得歇着，当然这对于我们开发者来说是透明的。协程更轻量的意思是，协程并不会映射成内核线程或者其他这么重的资源，它的调度在用户态就可以搞定，任务之间的调度并非抢占式，而是协作式的。

``关于并发和并行：正因为 CPU 时间片足够小，因此即便一个单核的 CPU，也可以给我们营造多任务同时运行的假象，这就是所谓的“并发”。并行才是真正的同时运行。``

从执行机制上来讲，协程跟回调没有什么本质的区别。

协程的概念最核心的点其实就是函数或者一段程序能够被挂起，待会儿再恢复，挂起和恢复是开发者的程序逻辑自己控制的，协程是通过主动挂起让出运行权来实现协作的。

它跟线程最大的区别在于线程一旦开始执行，从任务的角度来看，就不会被暂停，直到任务结束这个过程都是连续的，线程之间是抢占式的调度，因此也不存在协作的问题。

## 3. 协程的本质

1. 语言/编译层：带状态机的函数
   
   suspend fun 编译后不是新线程，而是状态机 + Continuation(续体)。
   
   协程的【暂停/继续】是函数级别的，不是 OS（operating system 操作系统） 级别的线程阻塞。

2. 运行时层：跑在线程池上的任务
   
   很多线程复用少量线程；挂起时不占线程，线程可以去跑别的协程。这是比 【一请求一线程】更高效的原因。

3. 设计层：结构化并发
   
   Job 树、取消传播、SupervisorJob 等这些结构化并发的能力，协程把这些的能力做成了库的一部分，线程只提供【能并行执行】，不管你怎么组织、怎么取消、怎么隔离失败。
   
   * Job 树：协程之间的父子层级关系
   
   * 取消传播：父协程被取消时，所有子协程也会被取消
   
   * supervisorJob：默认父子关系下，一个子协程失败，会取消父协程，进而取消所有兄弟，使用 SupervisorJob 子协程并行，一个挂了，另外两个还要跑完（子失败不取消兄弟，只取消自己）。
* suspend ≠ 自动在后台线程
  
  只是表示这里可以等，在哪个线程，要看 Dispatchers（比如 Main、IO）

* 协程里如果写 Thread.sleep(1000)
  
  还是会占住线程 1 秒，和挂起不一样。

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
