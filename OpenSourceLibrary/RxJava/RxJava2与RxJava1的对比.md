# RxJava 2 与 RxJava 1 的对比

## 1. 接口变化

　　RxJava 2.x 拥有了新的特性，其依赖于 4 个基本接口，它们分别是：

* Publisher
* Subscriber
* Subscription
* Processor

　　其中最核心的莫过于 Publisher 和 Subscriber。Publisher 可以发出一系列的事件，而 Subscriber 负责和处理这些事件。

　　其中用的比较多的自然是 Publisher 的 Flowable，它支持背压。

　　很明显，RxJava 2.x 最大的改动就是对于 backpressure 的处理，为此将原来的 Observable 拆分成了新的 Observable 和 Flowable，同时其他相关部分也同时进行了拆分。

## 2. 背压概念

　　异步环境下产生的问题：同步环境下会等待一件事处理完后再进行下一步，而异步环境下是处理完一件事，未等它得出结果接着处理下一步，在获得结果之后进行回调，再处理结果。

　　发送和处理速度不统一：例如生产者生产的产品放置到缓存队列中，供消费者消费。若生产者生产的速度大于消费者消耗的速度，则会出现缓存队列溢出的问题。

　　背压是一种流速控制即解决策略，例如背压中的丢弃策略，一旦发现缓存队列已满，为了整个过程顺利进行，则会丢弃最新产生的产品，避免溢出，因此背压也是一种流速控制的解决策略。


## 3. 参考文章

[浅析RxJava 1.x&2.x版本使用区别及原理（一）：Observable、Flowable等基本元素源码解析](https://blog.csdn.net/itermeng/article/details/80139074)