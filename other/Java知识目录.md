# Java 知识架构目录

## 线程
1. 线程池是如何设计的
2. Sleep() 和 wait() 的区别
3. Java 有哪些线程池？他们的区别是什么？线程池工作流程是怎么样的？线程池实现原理是怎样的？Cache 线程池有哪些弊端？
4. 多线程间的通信方式？
5. synchronize 关键字怎么用的？还知道哪些同步的方式？
6. Thred 直接调用 run 方法会怎么样？start 方法作用是什么？
7. volatile 关键字的作用是什么？
8. 怎么安全停止一个线程任务？原理是什么？线程池里有类似机制吗？
9. 如何实现线程同步？
10. 什么叫安全发布对象（多线程里面）final？
11. 多线程并发
12. threadlocal 原理

## 源码阅读
1. hashMap 以及 put 和 get 方法
2. HashMap 和 HashTable 的区别？和 CuncurrentHashMap 区别？和 LinkedHashMap 区别？内部实现原理？
3. LRUCache 的原理？
4. ArrayList 和 LinkedList 区别？为什么ArrayList 不是线程安全的？
5. 如果hashmap key 不一样，但是 hashcode 一样会怎么样？
6. hashmap 是否线程安全？不安全会出什么问题？
7. concurrenthashmap 读写分别是啥情况？
8. 遍历hashmap的原理？
9. arraymap 和 hashmap的区别？
10. collection与collections的区别？
11. 匿名内部类是否可以extends其他类？是否可以mplement interface(接口)
12. 手写 hashMap
13. treemap 、 hashmap 应用场景
14. arraylist 里面可以不可以 new 一个 t 泛型的数组？
15. java 内存结构，内存模式
16. arraymap
17. arraylist和linkedlist的应用场景

## 框架
1. MVC
2. MVP
3. MVVM

## 网络连接
1. HttpClient
2. HttpUrlConnetion
3. socket

## 虚拟机
1. 垃圾回收机制？有哪些对象可以作为 GC roots?
2. 跟 Art、Dalvik 对比
3. Java 内存模型？
4. 类加载机制？双亲委托模型？
5. art 和 dvm 在 gc 上有啥不同？有啥改进？
6. linux 和 windows 下进程怎么通信的？
7. dvm 和 art 的区别？

## 类
1. 静态内部类和内部类的区别？
2. 值传递类问题
3. 简书封装，继承，多态
4. 强软弱虚引用
5. 一个类实现一个接口，接口引用指向这个类对象，可以不可以调用它的 toString 方法？
6. 子类复写父类的equals 方法，凡是子类增加了一个成员变量int，请问equals 方法咋整？


## 同步异步
1. kotlin 为什么能和 Java 混编
2. atomicinteger内存模型
3. static 编译时有啥不同，static 语句块，static 变量,static 方法，构造初始化顺序（静态绑定）
4. 用四个线程计算数组和（join方法、countdoemlatch、线程池）







