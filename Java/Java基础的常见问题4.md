# Java 基础的常见问题 4

[TOC]



# 1. 为什么 Java 枚举文字不能具有泛型类型参数

[enum泛型 - 为什么Java枚举文字不能具有泛型类型参数?](https://code-examples.net/zh-CN/q/41793e)

　　由于类型擦除。

　　这两种方法都不可能，因为参数类型被擦除。

```java
public <T> T getValue(MyEnum<T> param);
public T convert(Object);
```

　　可以用以下代码解决

```java
public enum MyEnum {
    LITERAL1(String.class),
    LITERAL2(Integer.class),
    LITERAL3(Object.class);

    private Class<?> clazz;

    private MyEnum(Class<?> clazz) {
      this.clazz = clazz;
    }

    ...

}
```

# 2. arraylist,linkedlist,vector 效率

　　ArrayList 底层是数组结构，查询快，增删慢，线程不安全，效率高。

　　LinkedList 底层是链表数据结构，查询慢，增删快，线程不安全，效率高。

　　Vector 底层是数组结构，查询快，增删慢，线程安全，效率低。

# 3. final finally finalize 区别 

　　final 关键字可以用于类、方法、变量前，用来表示该关键字修饰的类、方法、变量具有不可变的特性。

　　final 关键字用于基本数据类型前：这是表明该关键字修饰的变量是一个常量，在定义后该变量的值就不能被修改。

　　final 关键字用于方法声明前：这是意味着该方法是最终方法，只能被调用，不能被覆盖，但是可以被重载。

　　final 关键字用于类名前：这是该类被称为最终类，该类不能被其他类继承。

　　finally：当代码抛出一个异常时，就会终止方法中剩余代码的处理，并退出这个方法的执行，可能会产生资源没有回收的问题。java 提供的解决方案就是 finally 子句，finally 子句中的语句是一定会被执行的，所以在 finally 子句中回收资源即可。

　　finalize：finalize() 方法来自于 java.lang.object，用于回收资源。可以为任何一个类添加 finalize 方法。finalize 方法将在垃圾回收器清除对象之前调用。在实际应用中，不要依赖该方法回收任何短缺的资源，因为很难知道这个方法什么时候被调用。

# 4. 介绍一下所有的 map，以及他们之间的对比，适用场景。

## 4.1. list 与 Set、Map 区别及适用场景

1. List、Set 都是继承自 Collection 接口，Map 则不是。

2. List 特点：元素有放入顺序，元素可重复 。

   Set 特点：元素无放入顺序，元素不可重复，重复元素会覆盖掉，（注意：元素虽然无放入顺序，但是元素在 set 中的位置是有该元素的 HashCode 决定的，其位置其实是固定的，加入 Set 的 Object 必须定义 equals() 方法 ，另外 list 支持 for 循环，也就是通过下标来遍历，也可以用迭代器，但是 set 只能用迭代，因为他无序，无法用下标来取得想要的值。） 

3. Set 和 List 对比： 
   Set：检索元素效率低下，删除和插入效率高，插入和删除不会引起元素位置改变。 
   List：和数组类似，List 可以动态增长，查找元素效率高，插入删除元素效率低，因为会引起其他元素位置改变。 

4. Map 适合储存键值对的数据。

5. 线程安全集合类与非线程安全集合类 

   LinkedList、ArrayList、HashSet 是非线程安全的，Vector 是线程安全的;
   HashMap 是非线程安全的，HashTable 是线程安全的;
   StringBuilder 是非线程安全的，StringBuffer 是线程安全的。

## 4.2. ArrayList 与 LinkedList 的区别和适用场景

* Arraylist：

　　优点：ArrayList 是实现了基于动态数组的数据结构，因为地址连续，一旦数据存储好了，查询操作效率会比较高（在内存里是连着放的）。

　　缺点：因为地址连续， ArrayList 要移动数据，所以插入和删除操作效率比较低。  

* LinkedList：

　　优点：LinkedList 基于链表的数据结构，地址是任意的，所以在开辟内存空间的时候不需要等一个连续的地址，对于新增和删除操作 add 和 remove，LinedList 比较占优势。LinkedList 适用于要头尾操作或插入指定位置的场景

　　缺点：因为 LinkedList 要移动指针，所以查询操作性能比较低。

* 适用场景分析：

　　当需要对数据进行对此访问的情况下选用 ArrayList，当需要对数据进行多次增加删除修改时采用 LinkedList。

## 4.3. ArrayList 与 Vector 的区别和适用场景

　　 ArrayList 有三个构造方法：

1. **public** ArrayList(**int** initialCapacity) // 构造一个具有指定初始容量的空列表。  
2. **public** ArrayList() // 构造一个初始容量为 10 的空列表。  
3. **public** ArrayList(Collection<? **extends** E> c) // 构造一个包含指定 collection 的元素的列表。

　　Vector 有四个构造方法：

1. **public** Vector() // 使用指定的初始容量和等于零的容量增量构造一个空向量。  
2. **public** Vector(**int** initialCapacity) // 构造一个空向量，使其内部数据数组的大小，其标准容量增量为零。  
3. **public** Vector(Collection<? **extends** E> c) // 构造一个包含指定 collection 中的元素的向量  
4. **public** Vector(**int** initialCapacity,**int** capacityIncrement) // 使用指定的初始容量和容量增量构造一个空的向量  

　　ArrayList 和 Vector 都是用数组实现的，主要有这么三个区别：

1. Vector 是多线程安全的，线程安全就是说多线程访问同一代码，不会产生不确定的结果。而 ArrayList 不是，这个可以从源码中看出，Vector 类中的方法很多有 synchronized 进行修饰，这样就导致了 Vector 在效率上无法与 ArrayList 相比；

2. 两个都是采用的线性连续空间存储元素，但是当空间不足的时候，两个类的增加方式是不同。

3. Vector可以设置增长因子，而 ArrayList 不可以。

4. Vector是一种老的动态数组，是线程同步的，效率很低，一般不赞成使用。

　　适用场景分析：

1. Vector 是线程同步的，所以它也是线程安全的，而 ArrayList 是线程异步的，是不安全的。如果不考虑到线程的安全因素，一般用 ArrayList 效率比较高。
2. 如果集合中的元素的数目大于目前集合数组的长度时，在集合中使用数据量比较大的数据，用 Vector 有一定的优势。

## 4.4. HashSet  与 TreeSet 的适用场景

1. TreeSet 是二叉树（ 红黑树的树据结构 ）实现的，TreeSet 中的数据是自动排好序的，不允许放入 null 值。 

2. HashSet 是哈希表实现的，HashSet 中的数据是无序的，可以放入 null，但只能放入一个 null，两者中的值都不能重复，就如数据库中唯一约束 。

3. HashSet 要求放入的对象必须实现 HashCode() 方法，放入的对象，是以 hashcode 码作为标识的，而具有相同内容的 String 对象，hashcode 是一样，所以放入的内容不能重复。但是同一个类的对象可以放入不同的实例。

　　适用场景分析: HashSet 是基于 Hash 算法实现的，其性能通常都优于 TreeSet。为快速查找而设计的 Set，我们通常都应该使用 HashSet，在我们需要排序的功能时，才使用 TreeSet。

## 4.5. HashMap 与 TreeMap、HashTable 的区别及适用场景

　　HashMap 非线程安全 。

　　HashMap：基于哈希表实现。使用 HashMap 要求添加的键类明确定义了 hashCode() 和 equals( ) [ 可以重写 hashCode() 和 equals()]，为了优化 HashMap 空间的使用，您可以调优初始容量和负载因子。 

　　TreeMap：非线程安全，基于红黑树实现。TreeMap 没有调优选项，因为该树总处于平衡状态。

　　HashTable：线程安全。 

　　适用场景分析：

　　HashMap 和 HashTable：HashMap 去掉了 HashTable 的 contains 方法，但是加上了 containsValue() 和 containsKey() 方法。HashTable 同步的，而 HashMap 是非同步的，效率上比 HashTable 要高。HashMap 允许空键值，而 HashTable 不允许。

　　HashMap：适用于 Map 中插入、删除和定位元素。 

　　Treemap：适用于按自然顺序或自定义顺序遍历键(key)。 

# 5. Map集合几种遍历方式的性能比较

[Map集合几种遍历方式的性能比较](https://www.cnblogs.com/mr-wuxiansheng/p/12950332.html)

首先构造一个 HashMap 集合：

```java
1 HashMap<String,Object> map = new HashMap<>();
2 map.put("A","1");
3 map.put("B","2");
4 map.put("C","3");
```

　　①、分别获取 key 集合和 value 集合。

```java
1 // 1、分别获取 key 和 value 的集合
2 for(String key : map.keySet()){
3     System.out.println(key);
4 }
5 for(Object value : map.values()){
6     System.out.println(value);
7 }
```

　　②、获取 key 集合，然后遍历 key 集合，根据 key 分别得到相应 value

```java
1 // 2、获取 key 集合，然后遍历 key，根据 key 得到 value
2 Set<String> keySet = map.keySet();
3 for(String str : keySet){
4     System.out.println(str+"-"+map.get(str));
5 }
```

　　③、得到 Entry 集合，然后遍历 Entry

```java
1 //3、得到 Entry 集合，然后遍历 Entry
2 Set<Map.Entry<String,Object>> entrySet = map.entrySet();
3 for(Map.Entry<String,Object> entry : entrySet){
4     System.out.println(entry.getKey()+"-"+entry.getValue());
5 }
```

　　④、迭代

```java
1 //4、迭代
2 Iterator<Map.Entry<String,Object>> iterator = map.entrySet().iterator();
3 while(iterator.hasNext()){
4     Map.Entry<String,Object> mapEntry = iterator.next();
5     System.out.println(mapEntry.getKey()+"-"+mapEntry.getValue());
6 }
```

　　基本上使用第三种方法是性能最好的。

　　第一种遍历方法在我们只需要 key 集合或者只需要 value 集合时使用；

　　第二种方法效率很低，不推荐使用；

　　第四种方法效率也挺好，关键是在遍历的过程中我们可以对集合中的元素进行删除。

# 6. 对象锁与类锁的区别

[面试官：请说一下对象锁和类锁的区别](https://zhuanlan.zhihu.com/p/98145713)

## 6.1. 实例锁

　　类声明后，我们可以 new 出来很多的实例对象。这时候，每个实例在 JVM 中都有自己的引用地址和堆内存空间，这时候，我们就认为这些实例都是独立的个体，很显然，在实例上加的锁和其他的实例就没有关系，互不影响了。

　　使用实例锁的方式有三种：

1. 锁住实体里的非静态变量
2. 锁住 this 对象
3. 直接锁非静态方法

　　使用对象锁的情况，只有使用统一实例的线程才会受锁的影响，多个实例调用同一方法也不会受影响。

## 6.2. 类锁

　　类所是加载类上的，而类信息是存在 JVM 方法区的，并且整个 JVM 只有一份，方法区又是所有线程共享的，所以类锁是所有线程共享的。

　　使用类锁的方式有 3 种方式：

1. 锁住类中的静态变量
2. 直接在静态方法上加 synchronized
3. 锁住 xxx.class

　　类锁是所有线程共享的锁，所以同一时刻，只能有一个线程使用加了锁的方法或方法体，不管是不是同一个实例。

# 7. 讲讲类的实例化顺序，比如父类静态数据，构造函数，字段，子类静态数据，构造函数，字段，当 new 的时候，他们的执行顺序。
　　顺序为：父类静态变量 ->父类静态代码块->子类静态变量->子类静态代码块、
 父类非静态变量（父类实例成员变量）->父类构造函数->子类非静态变量（子类实例成员变量）->子类构造函数。

# 8. reentrantlock/synchronized 区别？如何实现上锁

[Synchronized与ReentrantLock区别总结（简单粗暴，一目了然）](https://blog.csdn.net/zxd8080666/article/details/83214089)

　　相似点：都是加锁方式同步，而且都是阻塞式的同步，也就是说如果一个线程获得了对象锁，进入了同步块，其他访问该同步块的线程都必须阻塞在同步块外面等待，而进行线程阻塞和唤醒的代码是比较高的。

　　功能区别：

1. Synchronized 是java语言的关键字，是原生语法层面的互斥，需要 jvm 实现。而 ReentrantLock 它是 JDK 1.5 之后提供的 API 层面的互斥锁，需要 lock() 和 unlock() 方法配合 try/finally 语句块来完成。
2. 便利性：很明显 Synchronized 的使用比较方便简洁，并且由编译器去保证锁的加锁和释放，而 ReenTrantLock 需要手工声明来加锁和释放锁，为了避免忘记手工释放锁造成死锁，所以最好在 finally 中声明释放锁。
3. 锁的细粒度和灵活度：很明显 ReenTrantLock 优于 Synchronized 。

　　性能的区别：在 Synchronized 优化以前，synchronized 的性能是比 ReenTrantLock 差很多的，但是自从 Synchronized 引入了偏向锁，轻量级锁（自旋锁）后，两者的性能就差不多了，在两种方法都可用的情况下，官方甚至建议使用 synchronized，其实 synchronized 的优化我感觉就借鉴了 ReenTrantLock 中的 CAS 技术。都是试图在用户态就把加锁问题解决，避免进入内核态的线程阻塞。

## 8.1. synchronized

　　Synchronized 经过编译，会在同步块的前后分别形成 monitorenter 和 monitorexit 这个两个字节码指令。在执行 monitorenter 时，首先要尝试获取对象锁。如果这个对象没被锁定，或者当前线程已经拥有了那个对象锁，把锁的计算器加 1，相应的，在执行 monitorexit 指令时会将锁计算器就减 1，当计算器为 0 时，锁就被释放了。如果获取对象锁失败，那当前线程就要阻塞，直到对象锁被另一个线程释放为止。

## 8.2. ReentrantLock

　　由于 ReentrantLock 是 java.util.concurrent 包下提供的一套互斥锁，相比 Synchronized，ReentrantLock 类提供了一些高级功能，主要有以下 3 项：

1. 等待可中断，持有锁的线程长期不释放的时候，正在等待的线程可以选择放弃等待，这相当于 Synchronized 来说可以避免出现死锁的情况。通过 lock.lockInterruptibly() 来实现这个机制。

2. 公平锁，多个线程等待同一个锁时，必须按照申请锁的时间顺序获得锁， Synchronized 锁非公平锁，ReentrantLock 默认的构造函数是创建的非公平锁，可以通过参数 true 设为公平锁，但公平锁表现的性能不是很好。
3. 锁绑定多个条件，一个 ReentrantLock 对象可以同时绑定对个对象。ReenTrantLock 提供了一个 Condition（条件）类，用来实现分组唤醒需要唤醒的线程们，而不是像 synchronized 要么随机唤醒一个线程要么唤醒全部线程。

### 8.2.1. ReentrantLock 实现的原理

　　ReenTrantLock 的实现是一种自旋锁，通过循环调用 CAS 操作来实现加锁。它的性能比较好也是因为避免了使线程进入内核态的阻塞状态。想尽办法避免线程进入内核的阻塞状态是我们去分析和理解锁设计的关键钥匙。

# 在多线程的情况下，在JAVA 中如何保证一个方法只被一个对象调用？

1. synchronized
2. volitle
3. Lock/unlock
4. 信号量
5. reentrantlock/synchronized 区别？如何实现上锁







# 文件 IO 操作

# AtomInteger

AtomInteger