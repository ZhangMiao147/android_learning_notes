# 第 4 章 Lock 的使用

## 本章主要内容
	ReentrantLocal 类的使用。
	ReentrantReadWriteLock 类的使用。

## 4.1 使用 ReentrantLock 类
　　在 Java 多线程中，可以使用 synchronized 关键字来实现线程之间同步互斥，但在 JDK 1.5 中新增加了 ReentrantLock 类也是达到同样的效果，并且在扩展功能上也更加强大，比如具有嗅探锁定、多路分支通知等功能，而且在使用上也比 synchronized 更加的灵活。

#### 4.1.1 使用 ReentrantLock 实现同步：测试 1
　　调用 ReentrantLock 对象的 lock() 方法获得锁，调用 unlock() 方法释放锁。

　　当前线程打印完毕之后将锁进行释放，其他线程才可以继续打印。线程打印的数据是分组打印（一个线程打印的数据在一块），因为当前线程已经持有锁，但线程之间打印的顺序是随机的。

#### 4.1.2 使用 ReentrantLock 实现同步：测试 2
　　调用 lock.lock() 代码的线程就持有了“对象监视器”，其他线程只有等待锁被释放时再次争抢。效果和使用 synchronized 关键字一样，线程之间还是顺序执行的。

#### 4.1.3 使用 Condition 实现等待 / 通知：错误用法与解决
　　关键字 synchronized 与 wait() 和 notify()/notifyAll() 方法相结合可以实现等待 / 通知模式，类 ReentrantLock 也可以实现同样的功能，但需要借助于 Condition 对象。Condition 类是 JDK5 中出现的技术，使用它有更好的灵活性，比如可以实现多路通知功能，也就是在一个 Lock 对象里面可以创建多个 Condition(即对象监视器)实例，线程对象可以注册在指定的 Condition 中，从而可以有选择性地进行线程通知，在调度线程上更加灵活。

　　在使用 notify()/notifyAll() 方法进行通知时，被通知的线程却是由 JVM 随机选择的。但是用 ReentrantLock 结合 Condition 类是可以实现“选择性通知”，这个功能是非常重要的，而且在 Condition 类中是默认提供的。

　　而 synchronized 就相当于整个 Lock 对象中只有一个单一的 Condition 对象，所有的线程都注册在它一个对象的身上。线程开始 notifyAll() 时，需要通知所有的 WAITING 线程，没有选择权，会出现相当大的效率问题。

　　在 condition.wait() 方法调用之前调用 lock.lock() 代码获得同步监视器，不然就会报 java.lang.IllegalMonitorStateException 异常。

　　调用 Condition 对象的 await() 方法，使当前执行任务的线程进入了等待 WAITING 状态。

#### 4.1.4 正确使用 Condition 实现等待 / 通知
　　Object 类中的 wait() 方法相当于 Condition 类中的 await() 方法。

　　Object 类中的 wait(long timeout) 方法相当于 Condition 类中的 await(long time,TimeUnit unit) 方法。

　　Object 类中的 notify() 方法相当于 Condition 类中的 signal() 方法。

　　Object 类中的 notifyAll() 方法相当于 Ccondition 类中的 signalAll() 方法。

#### 4.1.5 使用多个 Condition 实现通知部分线程：错误用法
　　使用一个 Condition 对象，多个方法等待，当调用 signalAll() 方法唤醒等待线程时，所有的等待线程都会被唤醒。

　　如果想要单独唤醒部分线程该怎么处理呢？这时就有必要使用多个 Condition 对象了，也就是 Condition 对象可以唤醒部分指定线程，有助于提升程序运行的效率。可以先对线程进行分组，然后再唤醒指定组中的线程。

#### 4.1.6 使用多个 Condition 实现通知部分线程：正确用法
　　使用多个 Condition 对象，可以实现通知部分线程。

　　使用 ReentrantLock 对象可以唤醒指定种类的线程，这是控制部分线程行为的方便方式。

#### 4.1.7 实现生产者 / 消费者模式：一对一交替打印
　　通过使用 Condition 对象，成功实现交替打印的效果。

#### 4.1.8 实现生产者 / 消费者模式：多对多交替打印
　　使用多个线程来交替打印，如果使用 signal() 方法就会出现假死，要使用 signalAll() 方法来解决。

#### 4.1.9 公平锁与非公平锁
　　公平与非公平锁：锁 Lock 分为 “公平锁” 和 “非公平锁”，公平锁表示线程获得锁的顺序是按照线程加锁的顺序来分配的，即先来先得的 FIO 先进先出顺序。而非公平锁就是一种获取锁的抢占机制，是随机获得锁的，和公平锁不一样的就是先来的不一定先得到锁，这个方式可能造成某些线程一直拿不到锁，结果也就是不公平的了。

　　new ReentrantLock(boolean isFair) 传递参数 isFair 表示是否是公平锁。公平锁的结果是基本程序有序的状态，不保证完全有序。

#### 4.1.10 方法 getHoldCount()、getQueueLength() 和 getWaitQueueLength() 的测试
　　1）方法 int getHoldCount() 的作用是查询当前线程保持此锁定的个数，也就是调用 lock() 方法的次数。

　　2）方法 int getQueueLength() 的作用是返回正等待获取此锁定的线程估计数。比如有 5 个线程，1 个线程首先执行 await() 方法，那么在调用 getQueueLength() 方法后返回值是 4，说明有 4 个线程同时再等待 lock 的释放。

　　3）方法 int getWaitQueueLength(Condition condition) 的作用是返回等待与此锁定相关的给定条件 Condition 的线程估计数，比如 5 个线程，每个线程都执行了同一个 condition 对象的 await() 方法，则调用 getWaitQueueLength(Condition condition) 方法时返回的 int 值是 5 。


#### 4.1.11 方法 hasQueuedThread()、hasQueuedThreads() 和 hasWaiters() 的测试
　　1）方法 boolean hasQueuedThread(Thread thread) 的作用是查询指定的线程是否正在等待获取此锁定。

　　方法 boolean hasQueuedThreads() 的作用是查询是否有线程正在等待获取此锁定。 

　　2）方法 boolean hasWaiters(Condition condition) 的作用是查询是否有线程正在等待与此锁定有关的 condition 条件。

#### 4.1.12 方法 isFair()、isHeldByCurrentThread() 和 isLocked() 的测试
　　1）方法 boolean isFair() 的作用是判断是不是公平锁。

　　在默认的情况下，ReentrantLock 类使用的是非公平锁。

　　2）方法 boolean isHeldByCurrentThread() 的作用是查询当前线程是否保持此锁定。

　　3）方法 boolean isLocked() 的作用是查询此锁定是否由任意线程保持。

#### 4.1.13 方法 lockInterruptibly()、tryLock() 和 tryLock(long timeout,TimeUnit unit) 的测试
　　1）方法 void lockInterruptibly() 的作用是：如果当前线程未被中断，则获取锁定，如果已经被中断则出现异常。

　　当调用 lock.lockInterruptibly() 方法时，调用 thread.interrupt() 方法是会报异常。

　　2）方法 boolean tryLock() 的作用是，仅在调用时锁定未被另一个线程保持的情况下，才获得该锁定。

　　3）方法 boolean tryLock(long timeout,TimeUnit unit) 的作用是，如果锁定在给定等待时间内没有被另一个线程保持，且当前线程未被中断，则获取该锁定。

#### 4.1.14 方法 awitUniterruptibly() 的使用
　　当调用了thread.interrupt()方法之后，condition.await() 方法会报异常，但是调用 awaitUniterruptibly() 方法不会报异常。

#### 4.1.15 方法 awaitUntil() 的使用
　　调用 awaitUntil(long time) 是等待 time 时间后自动唤醒。

#### 4.1.16 使用 Condition 实现顺序执行
　　使用 Condition 对象可以对线程执行的业务进行排序规划。

　　使用 Condition 对线程进行分类，然后进行分类唤醒，从而实现排序规划。

## 4.2 使用 ReentrantReadWriteLock 类
　　类 ReentrantLock 具有完全互斥排他的效果，即同一时间只有一个线程在执行 ReentrantLock.lock() 方法后面的任务。这样做虽然保证了实例变量的线程安全性，但效率却是非常低下的。所以在 JDK 中提供了一种读写锁 ReentrantReadWriteLock 类，使用它可以加快运行效率，在某些不需要操作实例变量的方法中，完全可以使用读写锁 ReentrantReadWriteLock 来提升钙方法的代码运行速度。

　　读写锁表示也有两个锁，一个是读操作相关的锁，也称为共享锁；另一个是写操作相关的锁，也叫排他锁。也就是多个读锁之间不互斥，读锁与写锁互斥，写锁与写锁互斥。在没有线程 Thread 进行写入操作时，进行读取操作的多个 Thread 都可以获取读锁，而进入写入操作的 Thread 只有在获取写锁后才能进行写入操作。即多个 Thread 可以同时进行读取操作，但是同一时刻只允许一个 Thread 进行写入操作。

#### 4.2.1 类 ReentrantReadWriteLock 的使用：读读共享
　　使用 lock.readLock() 读锁可以提高程序运行效率，允许多个线程同时执行 lock() 方法后面的代码。两个线程几乎同时进入 lock() 方法后面的代码。

#### 4.2.2 类 ReentrantReadWriteLock 的使用：写写互斥
　　使用写锁代码 lock.writeLock() 的效果就是同一时间只允许一个线程执行 lock() 方法后面的代码。

#### 4.2.3 类 ReentrantReadWriteLock 的使用：读写互斥
　　“读写”操作是互斥的。

#### 4.2.4 类 ReentrantReadWriteLock 的使用：写读互斥
　　“写读”操作也是互斥的。即只要出现 “写操作” 的过程，就是互斥的。

　　“读写”、“写读” 和 “写写” 都是互斥的；而 “读读” 是异步的，非互斥的。

## 4.3 本章总结
　　


