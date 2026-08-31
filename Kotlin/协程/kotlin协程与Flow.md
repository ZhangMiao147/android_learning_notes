# Kotlin 协程与Flow

协程是一种并发设计模式，使用它可以在 Android 平台上简化异步执行的代码。简单来说，协程是 Kotlin 语言中一组特有的线程 API，基于 Kotlin 所具有的语法优势，开发者可以更加轻松地写出异步任务代码。

## 协程的 基本用法

如何启动一个协程？通过 GlobalScope.launch 函数创建一个协程：

```kotlin
GlobalScope.launch {
    Log.d("当前线程：", Thread.currentThread().name)
}
```

如果需要在开启协程的时候指定线程，可以设置 Dispatchers 参数值：

<mark>补充其他参数值</mark>

```kotlin
GlobalScope.launch(Dispatchers.IO) {
    Log.d("当前线程：", Thread.currentThread().name)
}
```

如何取消一个协程，launch 方法返回了一个 Job 对象，只需要在开启协程时声明一个 Job，在需要取消协程时调用其 cancel 方法即可：

```kotlin
var job = GlobalScope.launch(Dispatchers.IO) {
    Log.d("当前线程：", Thread.currentThread().name)
}
job.cancel()
```

GlobeScope.launch 函数创建的是一个顶层协程，在实际开发中很少使用，除此之外，Kotlin 还提供了 runBlocking、CorotineScope、async、withContext 等方法来创建协程作用域。

<mark">runBlocking大概了解一下</mark>

**CoroutineScope**

<mark>rememberCoroutineScope()这个知识查一下</mark>

使用 CoroutineScope 创建协程是实际项目中比较常用的方式：

```kotlin
val job = Job()
CoroutineScope(job).launch {
    //逻辑处理
}
job.cancel()
```

先创建一个 Job 对象，然后将其作为传参传给 CoroutineScope 函数。

CoroutineScope 是一个函数，它会返回一个 CoroutineScope 对象，有了 CoroutineScope 对象之后就可以调用 launch 方法来创建协程了。如何获取协程的执行结果？需要使用 async 函数了。

**async**

async 函数同样可以构建一个协程作用域并返回 Deferred 对象，但是与 Coroutine-Scope 函数不同的是，async 函数必须在协程作用域内才能调用:

```kotlin
val job = Job()
CoroutineScope(job).launch {
    val result= async {
        //模拟耗时操作
        delay(3000)
        "操作成功"
    }.await()
    Log.d(TAG, result)
}
```

当程序调用 await 方法时，await 方法会阻塞当前协程，直到获取执行结果。

使用 async 开启两个协程，分别进行两个耗时操作并打印出程序耗时时间：

```kotlin
CoroutineScope(job).launch {
    val startTime = System.currentTimeMillis()
    val result = async {
        delay(2000)
        "操作成功"
    }.await()
    val result2 = async {
        delay(1000)
        "获取成功"
    }.await()
    Log.d(TAG, "执行结果:$result-$result2")
    val endTime = System.currentTimeMillis()
    Log.d(TAG, "执行时间:" + (endTime - startTime))
}
```

打印结果：

```
执行结果：操作成功-获取成功 
执行时间：3026
```

result 调用 await 方法之后，会阻塞当前协程，2秒后 result 执行结束，开始执行 result2。针对此种情况，开发者可以只在用到执行结果的时候调用 await 方法。

可以让 result 和 result2 同时执行：

```kotlin
CoroutineScope(job).launch {
    val startTime = System.currentTimeMillis()
    val result = async {
        delay(2000)
        "操作成功"
    }
    val result2 = async {
        delay(1000)
        "获取成功"
    }
    Log.d(TAG, "执行结果:${result.await()}-${result2.await()}")
    val endTime = System.currentTimeMillis()
    Log.d(TAG, "执行时间:" + (endTime - startTime))
}
```

打印的结果：

```
执行结果：操作成功-获取成功
执行时间：2012
```

程序同时调用了 result 和 result2 的 await 方法，这样 result 和 result2 相当于并行的关系，在实际项目中常用需要合并不同接口执行结果的需要，这事就可以采用这种当大来提高运行效率。

**withContext**







## 参考文章

1. 《Android Jetpack 开发：原理解析与应用实战》- 第9章 优雅地实现异步任务：Kotlin 协程与 Flow
