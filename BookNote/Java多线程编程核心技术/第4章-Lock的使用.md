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
　　


#### 4.1.4 正确使用 Condition 实现等待 / 通知
　　

#### 4.1.5 使用多个 Condition 实现通知部分线程：错误用法
　　

#### 4.1.6 使用多个 Condition 实现通知部分线程：正确用法
　　

#### 4.1.7 实现生产者 / 消费者模式：一对一交替打印
　　

#### 4.1.8 实现生产者 / 消费者模式：多对多交替打印
　　

#### 4.1.9 公平锁与非公平锁
　　


#### 4.1.10 方法 getHoldCount()、getQueueLength() 和 getWaitQueueLength() 的测试
　　

#### 4.1.11 方法 hasQueuedThread()、hasQueuedThreads() 和 hasWaiters() 的测试
　　


#### 4.1.12 方法 isFair()、isHeldByCurrentThread() 和 isLocked() 的测试
　　


#### 4.1.13 方法 lockInterruptibly()、tryLock() 和 tryLock(long timeout,TimeUnit unit) 的测试
　　


#### 4.1.14 方法 awitUniterruptibly() 的使用
　　

#### 4.1.15 方法 awaitUnit() 的使用
　　

#### 4.1.16 使用 Condition 实现顺序执行
　　

## 4.2 使用 ReentrantReadWriteLock 类
　　

#### 4.2.1 类 ReentrantReadWriteLock 的使用：读读共享
　　

#### 4.2.2 类 ReentrantReadWriteLock 的使用：写写互斥
　　

#### 4.2.3 类 ReentrantReadWriteLock 的使用：读写互斥
　　

#### 4.2.4 类 ReentrantReadWriteLock 的使用：写读互斥
　　

## 4.3 本章总结
　　


