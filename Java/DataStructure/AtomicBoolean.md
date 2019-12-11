# AtomicBoolean

## 为什么使用 AtomicBoolean？
　　平时一般使用的 boolean 来表示不二变量，但是在多线程枪框下 boolean 是非线程安全的。

　　**boolean 非线程安全原因：**对于 boolean 变量主要有两个值，true 和 false。但是 true 和 false 是两个不同的常量对象，使用 synchronized 关键字时锁住的只是常量 true 或者常量 false。并没有锁住 boolean 变量。

　　使用 AutomicBoolean 类就可以解决 boolean 非线程安全的问题。

## AtomicBoolean 的源码分析
　　在使用时，是通过调用 AtomicBoolean 的 compareAndSet(boolean expect， boolean update) 方法进行同步的。接下来查看该方法的代码：
```

```

　　在 compareAndSet() 方法里面调用了 unsafe 的 compareAndSwapInt 方法，也就是使用了 CAS 机制。expect 和 update 的意思是现在的 boolean 如果不是 except 那就不更新，如果是我们预期的 except，那就更新，更新的值就是 update，也就是 CAS 原理。

**unsafe的解释？**

　　在源码中还会发现，boolean 其实转换成了 int 类型，1 表示 true，0 表示 false。

　　还有其他方法：
```

```

## 参考文章

[一个 java 并发原子类 AtomicBoolean 解析](http://baijiahao.baidu.com/s?id=1647915101064077163&wfr=spider&for=pc)