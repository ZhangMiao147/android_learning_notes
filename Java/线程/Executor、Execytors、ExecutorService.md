# Executor、Executors、ExecrtorService

## Executors
　　Executor 框架是 Java 5 中引入的，其内部使用了线程池机制，它在 java.util.cocurrent 包中，通过该框架来控制线程的启动、执行和关闭，可以简化兵法编程的操作。因此，在 java 5 之后，通过 Executor 来启动线程比使用 Thread 的 start 方法更好，除了更易管理，效率更好（用线程池实现，节约开销）外，还有关键的一点：有助于避免 this 逃逸问题 -- 如果在构造器中启动一个线程，因为另一个任务可能会在构造器结束之前开始执行，此时可能会访问到初始化了一半的对象用 Executor 在构造器中。Executor 作为灵活且强大的异步执行框架，其支持多种不同类型的任务执行策略，提供了一种标准的方法将任务的提交过程和执行过程解耦开发，基于生产者-消费者模式，其提交任务的线程相当于生产者，执行任务的线程相当于消费者，并用 Runnable 来表示任务，Executor 的实现还提供了对生命周期的支持，以及统计信息收集，应用程序管理机制和性能监视等机制。

#### Executor 的 UML 图：（常用的几个接口和子类）
![](./image/Executor的UML图.png)

　　Executor 框架包括：线程池、Executor、Executors、ExecutorService、CompletionService、Future、Callable 等。

#### Executor 和 ExecutorService
　　Executor：一个接口，其定义了一个接收 Runnable 对象的方法 executor，其方法签名为 executor(Runnable command)，该方法接收一个 Runable 实例，它用来执行一个任务，任务即一个实现了 Runnable 接口的类，一般来说，Runnable 任务开辟在新线程中的使用方法为：new Thread(new RunnableTask()).start()，但在 Executor 中，可以使用 Executor 而不用显示地创建线程：executor.execute(new RunnableTask());(异步处理)。

　　ExecutorService：是一个比 Executor 使用更广泛的子类接口，其提供了生命周期管理的方法，返回 Future 对象，以及可跟踪一个或多个异步任务执行状况返回 Future 的方法；可以调用 ExecutorService 的 shutdowm() 方法来平滑地关闭 ExecutorService，调用该方法后，将导致 ExecutorService 停止接收任何新的任务且等待已经提交的任务执行完成（已经提交的任务会分两类：一类是已经在执行的，另一类是还没有开始执行的），当所有已经提交的任务完毕后将会关闭 ExecutorService。因此一般用该接口来实现和管理多线程。

　　通过 ExecutorService.submit() 方法返回的 Future 对象，可以调用 isDone() 方法查询 Future 是否已经完成。当任务完成时，它具有一个结果，可以调用 get() 方法来获取该结果。也可以不用 isDone() 进行检查就直接调用 get() 获取结果，在这种枪框下，get() 将阻塞，直到结果准备就绪，还可以取消任务的执行。Future 提供了 cancel() 放啊用来取消执行 pending 中的任务。


#### Executoes 类：主要用于提供线程池相关的操作
　　Executor 类，提供了一系列工厂方法用于创建线程池，返回的线程池都实现了 ExecutorService 接口。
1. public static ExecutorService newFiexedThreadPool(int Threads) 创建固定数目线程的线程池。
2. public static ExecutorService newCachedThreadPool()：创建一个可缓存的线程池，调用 execute 将重用以前构造的线程（如果线程可用）。如果没有可用的线程，则创建一个新线程并添加到池中。终止并从缓存中移除那些已有 60 秒钟未被使用的线程。
3. public static ExecutorService newSingleThreadExecutor()：创建一个单线程化的 Executor。
4. public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize)：创建一个支持定时及周期性的任务执行的线程池，多数情况下可用来替代 Timer 类。



| 方法                        | 解释                                                         |
| --------------------------- | ------------------------------------------------------------ |
| newCachedThreadPool()       | 缓存型池子，先查看池中有没有以前建立的线程，如果有，就 reuse。如果没有，就建一个新的线程加入池中。<br /><br />缓存型池子通常用于执行一个生存期很短的异步型任务。因此在一些面向连接的 daemon 型 SERVER 中用得不多。但对于生存期短的异步任务，它是 Executor 的首选。<br /><br />能 reuse 的线程，必须是 timeout IDLE 内的池中线程，缺省 timeout 是 60s，超过这个 IDLE 时长，线程实例将被终止及移出池。<br />注意，放入 ChachedThreadPool 的线程不必担心其结果，超过 TIMEOUT 不活动，其会自动被终止。 |
| newFixedThreadPool(int)     | newFixedThreadPool 与 cacheThreadPool 差不多，也是能 reuse 就用，但不能随时建新的线程。<br />其独特之处：任意时间点，最多只能有固定数目的活动线程存在，此时如果有新的线程要建立，只能放在另外的队列中等待，知道当前的线程中某个线程终止直接被移出池子。<br />和 cacheThreadPool 不同，FixedThreadPool 没有 IDLE 机制（可能也有，但既然文档没提，肯定非常长，类似依赖上层的 TCP 或 UDP IDLE 机制之类的），所以 FixedThreadPool 多数针对一些很稳定很固定的正规并发线程，多用于服务器。<br />从方法的源代码看，cache 池和 fixed 池调用的是同一个底层池，只不过参数不同：fixed 池线程数固定，并且是 0 秒 IDLE（无 IDLE）；cache 池线程数支持 0-Integer.MAX_VALUE（显然完全没考虑主机的资源承受能力），60 秒 IDLE。 |
| newScheduledThreadPool(int) | 调度型线程池。<br />这个池子里的线程可以按 schedule 依次 delay 执行，或周期执行。 |
| SingleThreadExecutor()      | 单例线程，任意时间池中只能有一个线程。<br />用的是和 cache 池和 fixed 池相同的底层池，但线程数目是 1，0 秒 IDLE（无 IDLE）。 |



## Executor VS ExecutorService VS Executors 
　　



## 参考文章
[java并发编程：Executor、Executors、ExecutorService](https://blog.csdn.net/weixin_40304387/article/details/80508236)