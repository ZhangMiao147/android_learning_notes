# Kotlin Flow 的 collect 和 collectLatest 有什么区别？

在 Kotlin 协程库中，collect 和 collectLatest 是用于收集流数据的两种不同操作。理解它们的区别，将有助于确保在处理流数据时的效率和行为符合预期。

### 1. 基本定义

* collect：是一个挂起函数，用于启动流的收集过程。收集过程会逐个处理每个发射的值，并从上游流向下游执行相应的操作。由于是逐个处理，较耗时的操作会阻塞后续值的处理。

* collectLatest：类似于 collect，但有一个关键不同点，即当新的值发射时，如果上一个值的收集操作尚未完成，collectLatest 将取消上一个挂起操作，并立即启动新的值的收集过程。这样适用于需要处理最新数据，而可以忽略之前未完成任务的场景。

### 2. 行为对比

collect 的工作方式

```kotlin
flow<Int> {
    emit(1)
    delay(1000)
    emit(2)
    delay(1000)
}.collect { value ->
    delay(1500) // Simulates a long processing time
    println("Collecting value: $value")
}
```

输出：

```
Collecting value: 1
Collecting value: 2
```

在例子中，collect 逐个处理发射的值。由于每个值的处理事件（1500ms）大于发射值的间隔（1000ms），所以处理完第一个值后，第二个值的发射已经完成，单处理仍然按顺序进行。

collectLatest 的工作方式

```kotlin
flow<Int> {
    emit(1)
    delay(1000)
    emit(2)
    delay(1000)
}.collectLatest { value -> 
    delay(1500) // Simulates a long processing time
    println("Collecting latest value: ${value}")
}
```

输出：

```
Collecting latest value: 2
```

在这个例子中，collectLatest 会在每次发射新值时，取消之前未完成的收集操作。因此，虽然上面代码中有 delay(1500) 的长时间处理，但由于新值 2 的发射在 delay(1000) 时发生，使得对 1 的处理被取消，立即开始处理 2。

### 3. 应用场景

* collect
  
  * 需要保证每个发射的值都能被完整处理，不被中途丢弃。
  
  * 适用于需要逐个按顺序处理每个事件的场景。

* collectLatest
  
  * 适用于只关心最新值的场景，可以优先处理最新数据，而忽略未完成的旧数据处理。
  
  * 典型应用场景如搜索框自动完成，用户快速输入时不必等待之前所有的输入处理完成，可以终止未完成的处理来响应最新输入。

### 4. 选择指南

* 选择 collect：当你的需求是必须处理每一个流数据，不允许丢失任何一个事件，例如日志处理、分批任务等。

* 选择 collectLatest：当你的关注点在于处理最新的数据，而可以丢弃之前未完成的操作，例如实时数据展示、用户输入提示等。

### 总结

* collect：适合逐个、顺序处理所有发射的流数据，保证每个值都被完整处理。

* collectLatest：适合只关心最新数据的场景，可以中途取消未完成的处理以优先处理最新的发射值。

## 参考文章

1. [Android面试题之Kotlin Flow的collect和collectLatest有什么区别？-腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/2427540)
