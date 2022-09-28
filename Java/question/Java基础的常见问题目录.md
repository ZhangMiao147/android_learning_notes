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
   * sleep,wait,yield 在多线程应用中的区别 
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
    * final 的用途
      * 用来修饰一个引用
      * 用来修饰一个方法
      * 用来修饰类
    * final 关键字，为什么匿名内部类使用局部饮用用 final

# Java 基础的常见问题 3

1. 抽象与接口
   * 抽象类与接口的区别
   * 类可以继承多个类吗
   * 接口可以继承多个接口吗
   * 类可以实现多个接口吗
2. IO 模型有哪些，讲讲你理解的 nio，它和 bio、aio 的区别是啥？谈谈 reactor 模型。
3. hashCode
   * 如何在父类中为子类自动完成所有的 hashcode 和 equals 实现？这样做有何优劣？
     * 覆盖 equals 时需要遵守的通用约定
     * 覆盖 equals 时总要覆盖 hashCode
   * 说一说你对 java.lang.object 对象中 hashCode 和 equals 方法的理解。在什么场景下需要重新实现这两个方法
   * 这样的 a.hashcode() 有什么用，与 a.equals(b) 有什么关系
     * hashcode() 有什么用
     * equals 与 hashcode 的关系
     * hashcode 是否唯一
   * 有没有可能 2 个不相等的对象有相同的 hashcode
4. 请结合 OO 设计理念，谈谈访问修饰符 public、private、protected、default 在应用设计中的作用
5. String
   * 在自己的代码中，如果创建一个 java.lang.String 类，这个类是否可以被类加载器加载？为什么？
     * 双亲委派模型
     * 为什么要使用这种双亲委派模型呢？
     * 定义自己的 ClassLoader
     * 怎么打破双亲委派机制
       * 双亲委派模型破坏史
6. 深拷贝和浅拷贝的区别
7. HashSet 内部是如何工作的
8. 序列化
   * 什么是序列化与反序列化
   * 序列化的应用场景
   * 序列化的好处
   * 如何实现序列化
     * Serizable
       * 注意事项
     * Parcelable
     * Parcelable 与 Serializable 区别
       * 两者的实现差异
       * 两者的设计初衷
       * 两者效率选择
9. Java 8 的新特性
   * 接口的默认方法
   * Lambda 表达式
   * 函数式接口
   * 方法与构造函数引用
   * Lambda 作用域
   * 访问局部变量
   * 访问对象字段与静态变量
   * 访问接口的默认方法
   * Data API
   * Annotation 注解
10. 运算符
    * java 运算符与（&）、非（~）、或（|）、异或（^）
      * 与运算符
      * 或运算符
      * 非运算符
      * 异或运算符

# Java 基础的常见问题 4

1. 为什么 Java 枚举文字不能具有泛型类型参数

2. arrayList、LInkedList、vector 效率

3. final、finally、finalize 区别

4. 介绍一下所有的 map，以及他们之间的对比，适用场景。

   * list 与 set、map 区别及适用场景
   * ArrayList 与 LinkedList 的区别和适用场景
   * ArrayList 与 Vector 的区别和适用场景
   * HashSet 和 TreeSet 的适用场景
   * HashMap 与 TreeMap、HashTable 的区别及适用场景

5. Map 集合几种遍历方式的性能比较

6. 对象锁与类锁的区别

   * 实例锁
   * 类锁

7. 讲讲类的实例化顺序，比如父类静态数据，构造函数，字段，子类静态数据，构造函数，字段，当 new 的时候，他们的执行顺序。

8. reentrantlock/synchronized 区别？如何实现上锁

   * synchroznied
   * ReentrantLock
     * ReentrantLock 实现的原理
9. list 的 remove


# Java 基础的常见编程题

1. 请编程实现 Java 的生产者-消费者模型
   * 使用 synchronzied、wait、notifyAll 实现生产者-消费者模式
   * 使用信号量实现生产者-消费者模式
   * 使用 ReentrantLock 实现生产者-消费者模式
2. 算法题：两个线程分别持续打印奇数和偶数，实现两个线程的交替打印（从小到大）
   * 使用 volatile 和 synchronzied 实现
   * 使用 synchronzied + volatile + wait + notify 实现
   * ReentrantLock 实现
   * flag 实现



https://www.jianshu.com/p/1280aa1ca58a?utm_campaign=haruki

