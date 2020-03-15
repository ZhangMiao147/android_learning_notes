# Synchronized 知识

## 基本知识
　　Synchronized ：

## 三种使用方式的总结

1. 修饰实例方法，作用于当前对象实例加锁，进入同步代码前要获取当前对象实例的锁。

2. 修饰静态方法，作用于当前类对象加锁，进入同步代码前要获得当前类对象的锁。

   也就是给当前类加锁，会作用于类的所有对象实例，因为静态成员不属于任何一个实例对象，是类成员（static 表明这是该类的一个静态资源，不管 new 了多少个对象，只有一份，所以对该类的所有对象都加了锁）。

   所以如果一个线程 A 调用一个实例对象的非静态 synchronized 方法，而线程 B 需要调用这个实例对象所属类的静态 synchronized 方法，是允许的，不会发生互斥现象，因为访问静态 synchronized 方法占用的锁是当前类的锁，而访问非静态 synchronized 方法占用的锁是当前实例对象锁。

3. 修饰代码块，指定加锁对象，对给定对象加锁，进入同步代码库前要获得给定对象的锁。

   和 synchronized 方法一样，sychronized (this) 代码块也是锁定当前对象的。synchronized 关键字加到 static 静态方法和 synchronized(class) 代码块上都是是给 Class 类上锁。

   尽量不要使用 synchronized(String a)，因为 JVM 中，字符串常量吃具有缓冲功能。 

## synchronzied 关键字底层原理

　　synchronzied 关键字底层原理属于 JVM 层面。

### synchronzied 同步语句块的情况

```java
public class SyncrhonziedDemo {
	public void method(){
		synchronized (this) {
			System.out.println("synchronized 代码块");
		}
	}
}
```

　　synchronized 同步语句块的实现使用的是 monitoeenter 和 monitorexit 指令，其中 monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置。

　　当执行 monitorenter 指令时，线程试图获取锁也是获取 monitor（monitor 对象存在于每个 Java 对象的对象头中，synchronized 锁便是通过这种方式获取锁的，也是为什么 Java 中任意对象可以作为锁的原因）的持有权。

　　当计数器为 0 则可以成功获取，获取后将锁计数器设为 1 也就是加 1。

　　相应的在执行 monitorexit 指令后，将锁计数器设为 0 ，表明锁被释放。

　　如果获取对象锁失败，那当前线程就要阻塞等待，直到锁被另外一个线程释放为止。

### synchronized 修饰方法的情况

```java
public class SynchronizedDemo2 {
	public synchronized void method() {
		System.out.println("synchronized 方法");
	}
}
```

　　synchonzied 修饰的方法并没有 monitorenter 指令和 monitorexit 指令，取而代之的是 ACC_SYNCHRONIZED 标识，该标识指明了该方法是一个同步方法，JVM 通过该 ACC_SYNCHONRIZED 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

## JDK 1.6 之后的底层优化

　　在 Java 早期版本中，synchronized 属于重量级锁，效率低下，因为监视器锁（monitor）是依赖于底层的操作系统的 Mutex Lock 来实现的，Java 的线程是映射到操作系统的原生线程之上的。如果要挂起或者唤醒一个线程，都需要操作系统帮王成，而操作系统实现线程之间的切换时需要从用户态转换到内核态，这个状态之间的转换需要相对比较长的时间，时间成本相对较高，这也是为什么早期的 synchronized 效率低的原因。

　　在 Java 6 之后 Java 官方对从 JVM 层面对 synchronized  较大优化，所以现在的 synchronized 锁效率也优化的很不错了。JDK 1.6 对锁的实现引入了大量的优化，如自旋锁、适应性自旋锁、锁消除、锁粗化、偏向锁、轻量级锁等技术来减少锁操作的开销。

　　锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。注意锁可以升级补课降级，这种策略是为了提高获得锁和释放锁的效率。

### 1. 偏向锁




## 参考文章
[Synchronized 锁定的到底是什么](https://www.zhihu.com/question/57794716?sort=created)

[synchronized详解](https://www.jianshu.com/p/16cec9b50ad2)

[老生常谈为什么需要synchronized，以及synchronized 的注意事项](https://blog.csdn.net/wangyadong317/article/details/84065828)