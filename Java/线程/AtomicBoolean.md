# AtomicBoolean

## 为什么使用 AtomicBoolean？
　　平时一般使用的 boolean 来表示布尔变量，但是在多线程框架下 boolean 是非线程安全的。

　　**boolean 非线程安全原因：**对于 boolean 变量主要有两个值，true 和 false。但是 true 和 false 是两个不同的常量对象，使用 synchronized 关键字时锁住的只是常量 true 或者常量 false。并没有锁住 boolean 变量。

　　使用 AutomicBoolean 类就可以解决 boolean 非线程安全的问题。

## AtomicBoolean 的源码分析
　　在使用时，是通过调用 AtomicBoolean 的 compareAndSet(boolean expect， boolean update) 方法进行同步的。接下来查看该方法的代码：
```
    public final boolean compareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }
```

　　在 compareAndSet() 方法里面调用了 unsafe 的 compareAndSwapInt 方法，也就是使用了 CAS 机制。expect 和 update 的意思是现在的 boolean 如果不是 except 那就不更新，如果是我们预期的 except，那就更新，更新的值就是 update，也就是 CAS 原理。

　　在源码中还会发现，boolean 其实转换成了 int 类型，1 表示 true，0 表示 false。

**CAS 原理**

　　比较和交换（Compare And Swap）是用于实现多线程同步的原子指令。它将内存位置的内容与给定值进行比较，只有在相同的情况下，将该内存位置的内容修改为新的给定值。这是作为单个原子操作完成。原则性保证新值基于最新信息计算。如果该值在同一时间被另一个线程更新，则写入将失败。操作结果必须说明是否进行替换，这可以通过一个简单的布尔响应（这个变体通常称为比较和设置），或通过返回从内存位置读取的值来完成。

　　**ABA 问题：**CAS 可以有效的提升并发的效率，但同时也会引入 ABA 问题。如线程 1 从内存 X 中取出 A，这时候另一个线程 2 也从内存 X 中取出 A ，并且线程 2 进行了一些操作将内存 X 中的值变成了 B，然后线程 2 又将内存 X 中的数据变成 A，这时候线程 1 进行 CAS 操作发现内存 X 中仍然是 A ，然后线程 1 操作成功。虽然线程 1 的 CAS 操作成功，但是整个过程就是有问题的。比如链表的头在变化了两次后恢复了原值，但是不代表链表没有变化。所以 Java 中提供了 **AtomicStampedReference** / **AtomicMarkableReference** 来处理会发生 ABA 问题的场景，主要是在对象中额外再增加一个标记来标识对象是否有过变更。

**unsafe的知识**
　　Unsafe 类在 jdk 源码的多个类中用到，这个类提供了一些绕开 JVM 的更底层功能，基于它的实现可以提供效率。但是它所分配的内存需要手动 free（不被 GC 回收）。Unsafe 类，提供了 JNI 某些功能的简单替代：确保高效性的同时，使事情变得更简单。

　　这类使属于 sun.* API 中的类，并且它不是 J2ES 中真正的一部分，因此可能会找不到任何的官方文档。

　　Unsafe API 的大部分方法都是 native 实现，它由 105 个方法组成，主要包括以下几类：

　　（1）Info 相关。主要返回某些低级别的内存存次：addressSize()、pageSize()。
　　（2）Object 相关。主要提供 Object 和它的域操作方法：allocateInstance()、objectFieldOffset()。
　　（3）Class 相关。主要提供 Class 和它的静态操纵方法：staticFieldOffset()、defineClass()、defineAnonymousClass()、ensureClassInitialized()。
　　（4）Arrays 相关。数据操纵方法：arrayBaseOffset()、arrayIndexScale()。
　　（5）Synchronization 相关。主要提供低级别同步原语（如基于 CPU 的 CAS(Compare-And-Swap)原语）：monitorEnter()、tryMonitorEnter()、monitorExit()、**compareAndSwapInt()**、putOrderedInt()。
　　（6）Memory 相关。直接内存访问方法（绕过 JVM 堆直接操纵本地内存）：allocateMemory()、copyMemory()、getAddress()、getInt()、putInt()。

　　Unsafe 的 


**AtomicBoolean  的其他方法**

```
	//返回当前值
	public final boolean get() {
        return value != 0;
    }
    //与 compareAndSet 方法相同
    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }
    //设置新值
    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }
	//设置新值，该操作会让 Java 插入 Store 内存屏障，避免发生写操作重排序
    public final void lazySet(boolean newValue) {
        int v = newValue ? 1 : 0;
        unsafe.putOrderedInt(this, valueOffset, v);
    }
	//返回旧值，设置新值
    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }
```

## 参考文章

[一个 java 并发原子类 AtomicBoolean 解析](http://baijiahao.baidu.com/s?id=1647915101064077163&wfr=spider&for=pc)

[AutomicBoolean](https://github.com/tinyking/jdk1.8/blob/master/src/java/util/concurrent/atomic/AtomicBoolean.java) - 源码

[Java中的CAS实现原理](https://www.cnblogs.com/javalyy/p/8882172.html)

[sun.misc.Unsafe详解和CAS底层实现](https://blog.csdn.net/lvbaolin123/article/details/80527598)

[Java中的魔法类：sun.misc.Unsafe示例详解](https://www.jb51.net/article/140721.htm)