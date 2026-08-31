# Kotlin 协程实现

> 简介
> 
> 1. 线程与协程的实现

## 线程与协程的实现

Java 对线程做了很好的支持，这也是 Java 在高并发场景风生水起的一个关键支柱，**虚拟机底层对线程的支持，例如 Android 虚拟机，其实就是 pthread**。Java 的 Object 还有一个 **wait 方法，这个方法几乎支撑了各种锁的实现，它底层是 condition**。

绝大多数协程都是语言层面有自己的实现，不同的编程语言有不同的使用场景，自然在实现上也看似有很大的差异，甚至还有的语言自己没有实现协程，单开发者通过第三方框架的方式提供了协程的能力，例如 Java 的框架 Quasar，加上协程实现本身在操作系统层面就有过一系列演进，因此出现了虽然理论上看起来很简单，单**实现上却多样化**的局面。

<mark>在 Android 虚拟机，kotlin 的协程实现是什么样的？</mark>







## 13. 协程的几种常见的实现

### 13.1. 协程的分类

#### 13.1.1. 按调用栈分类

由于协程需要支持挂起、恢复，因此对于挂起点的状态保存就显得极其关键。类似地，线程会因为 CPU 调度权的切换而被中断，它的中断状态会保存在调用栈当中，因为协程的实现也按照是否开辟相应的调用栈存在以下两种类型：

- 有栈协程 Stackful Coroutine：每一个协程都会有自己的调用栈，有点儿类似于线程的调用栈，这种情况下的协程实现其实很大程度上接近线程，主要不同体现在调度上。
- 无栈协程 Stackless Coroutine：协程没有自己的调用栈，挂起点的状态通过状态机或者闭包等语法来实现。

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

- 对称协程 Symmetric Coroutine：任何一个协程都是相互独立且平等的，调度权可以在任意协程之间转移。
- 非对称协程 Asymmetric Coroutine：协程出让调度权的目标只能是它的调用者，即协程之间存在调用和被调用关系。

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

接着自 ③ 处调用 next，将调度权从主流程转移到 numbers 函数当中，从上一次挂起的位置 ① 处继续执行，i 的值修改为 1，1s 后，再次通过 yield(1) 挂起，③ 处输出：

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

- coroutine.create: 创建协程，参数为函数，作为协程的执行体，返回协程实例。
- coroutine.yield：挂起协程，第一个参数为被挂起的协程实例，后面的参数则作为外部调用 resume 来继续当前协程时的返回值，而它的返回值则又是外部下一次 resume 调用时传入的参数。
- coroutine.resume：继续协程，第一个参数为被继续的协程实例，后面的参数则作为协程内部 yield 时的返回值，返回值则为协程内部下一次 yield 时传出的参数；如果是第一次对该协程实例执行 resume，参数回作为协程函数的参数传入。

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

- 协程的执行体，主要是指启动协程时对应的函数
- 协程的控制实例，可以通过协程创建时返回的实例控制协程的调用流转
- 协程的状态，在调用流程转移前后，协程的状态会发生相应的变化

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
