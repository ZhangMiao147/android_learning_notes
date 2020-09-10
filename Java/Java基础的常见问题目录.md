# Java 基础的常见问题目录

# Java 基础的常见问题 1

1. 面向对象的特性
   * 封装
   * 继承
   * 抽象
   * 多态
2. switch-case
   * switch 语句支持的数据类型
3. String、StringBuffer、StringBuilder
   * 三者的区别
   * "+" 与 append 的区别
   * StringBuffer 是如何实现线程安全的
   * String 的 concat 方法与 append 的区别
   * 比较 string="aaa" 和 string=new String("aaa")
4. 异常
   * Try-catch-finally
     * 在 try 中 return 还会不会调用 finally
     * throw 和 throws
   * error 和 exception
     * Error 和 Exception 的父类子类
     * Error 和 Exception 的区别
     * 常见的 Exception
     * 常见的 Error
     * CheckedException、RuntimeException 的区别
     * 异常处理的注意事项
5. Java 的基本数据类型
   * 基本数据类型
     * byte
     * short
     * int
     * long
     * float
     * double
     * boolean
   * 引用数据类型
   * 数据类型之间的转换
   * BigDecimal
6. 进程与线程
   * 线程的状态有哪些？
   * 如何实现线程
     * 继承 Thread 类创建线程
     * 实现 Runnable 接口创建线程
     * 实现 Callable 接口的源码
   * start 与 run 的区别
   * Thread 与 Runnable 的区别
   * 线程间通信
     * 使用同一个共享变量控制
     * 使用管道流
     * 利用 BlockingQueue
   * 线程的常用方法
     * 设置或获取多线程的线程名称的放啊
     * sleep：线程休眠
     * yield()：线程让步
     * join()：等待线程终止
     * 线程停止
     * wait()：线程等待
     * notify()：线程唤醒
     * notifyAll()：唤醒所有线程
   * wait() 与 sleep() 的区别
7. 进程
   * 线程与进程的区别
     * 进程（线程+内存+文件/网络句柄）
       * 内存
       * 文件/网络句柄
     * 线程（栈+PC+TLS）
       * 栈
       * PC
       * TLS
     * 进程与线程的区别
   * 进程间通信
8. 线程池
   * 线程池是什么？
   * 线程池分为几类？
     * CacheThreadPool：可缓存线程池
     * FixedThreadPool：定长线程池
     * ScheduledThreadPool：定时线程池
     * SingleThreadPool：单线程化的线程池
   * 线程池的使用和原理
     * 线程池带来的好处
     * 线程池中的几个概念
     * 线程池的线程是如何做到复用的
     * 线程池是如何做到高效并发的
     * 线程池的实现原理
       * 线程池处理任务流程
       * 线程池中的线程执行任务
     * 线程池的使用
       * 创建线程池
       * 线程池中提交任务
       * 关闭线程池
       * 如何合理的配置线程池参数
       * 监控线程池
9. ThreadLocal 原理
10. 对象的引用类型有哪些？
    * 强引用 StrongReference
    * 软引用 SoftReference
    * 弱引用 WeakReference
    * 虚引用 PhantomRefence

# Java 基础的常见问题 2

1. 泛型
   * 在 jdk 1.5 中，引入了泛型，泛型的存在是用来解决什么问题的。
2. 反射
   * Class 类
     * 获得类相关的方法
     * 获得类中属性相关的方法
     * 获得类中注解相关的方法
     * 获得类中构造器相关的方法
     * 获得类中方法相关的方法
     * 类中其他重要的方法
   * Field 类
   * Method 类
   * Constructor 类
   * 反射的原理
   * 反射创建实例的三种方式是什么？
   * 反射中，Class.forName 和 ClassLoader 区别
3. 代理
   * 三种代理模式
     * 静态代理
     * 动态代理
     * Cglib 代理
   * Java 动态代理实现与原理详细分析
   * 描述动态代理的集中实现方式，分别说出相应的优缺点
   * jdk 动态代理与 cglib 实现的区别
   * 为什么 cglib 方式可以对接口实现代理
4. 注解
   * 元注解
     * @Retension
     * @Target
     * @Documented
     * @Inherited
     * @Repeatable
   * 获得注解属性
   * 反射注解的工作原理
   * JDK 提供的注解
   * 注解的作用
5. 锁
   * 死锁
   * 造成死锁的四个条件
   * 解决死锁的方法
   * 死锁预防
   * 死锁避免
   * 死锁检查
     * 每种类型一个资源的死锁检测
     * 每种类型多个资源的死锁检测
   * 死锁解除
6. ArrayList 和 LinkedList 的区别
7. List、Set 和 Map 的区别
   * 区别
   * List 接口的三个实现类
   * Map 接口的四个实现类
   * Set 接口的三个实现类
     * Set 源码
   * 有没有有序的 Map 实现类，如果有，他们是怎么保证有序的？
8. HashMap
   * HashMap 的源码
   * HashMap 的数组长度为什么一定要保持为 2 的次幂？
   * 为什么 HashMap 的 key 要实现 hashCode() 方法
   * HashMap 不同版本代码的区别
   * 为什么有了 HashMao 还要有 HashTable
   * HashMap 和 HashTable 的区别
   * HashMap 线程不安全的体现
9. ConcurrentHashMap
   * JDK 1.7 版本的 ConcurrentHashMap 的实现原理
     * ConcurrentHashMap 如何体现线程安全
   * JDK 1.8 版本的 ConcurrentHashMap 的实现原理
   * JDK 1.8  的 ConcurrentHashMap 为什么放弃了分段锁，有什么问题吗？如果你来设计，你如何设计？
     * JDK 1.8 的 ConcurrentHashMap 为什么放弃了分段锁
     * JDK 1.8 的 ConcurrentHashMap 放弃了分段锁，有什么问题
     * 如果你来设计，你如何设计？
10. final



https://www.jianshu.com/p/1280aa1ca58a?utm_campaign=haruki

