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

# 3. final finally finalize区别 

final 关键字可以用于类、方法、变量前，用来表示该关键字修饰的类、方法、变量具有不可变的特性。

final 关键字用于基本数据类型前：这是表明该关键字修饰的变量是一个常量，在定义后该变量的值就不能被修改。

final 关键字用于方法声明前：这是意味着该方法是最终方法，只能被调用，不能被覆盖，但是可以被重载。

final 关键字用于类名前：这是该类被称为最终类，该类不能被其他类继承。

finally：当代码抛出一个异常时，就会终止方法中剩余代码的处理，并退出这个方法的执行，可能会产生资源没有回收的问题。java 提供的解决方案就是 finally 子句，finally 子句中的语句是一定会被执行的，所以在 finally 子句中回收资源即可。

finalize：finalize() 方法来自于 java.lang.object，用于回收资源。可以为任何一个类添加 finalize 方法。finalize 方法将在垃圾回收器清除对象之前调用。在实际应用中，不要依赖该方法回收任何短缺的资源，因为很难知道这个方法什么时候被调用。

# 4. 输入一个数组，想一种方法让这个数组尽可能的乱序，保证功能能实现的情况下时间复杂度和空间复杂度尽可能的小，可使用随机数函数。

Fisher-Yates Shuffle ：算法思想就是从原始数组中随机抽取一个新的数字到新数组中。

Knuth-Durstendeld Shuffle：每次从未处理的数据中随机取出一个数组，然后把该数字放在数组的尾部，即数组尾部存放的是已经处理过的数组。

Inside-Out Shuffle：是一个 in-place 算法，原始数据被直接打乱，有些应用中可能需要保留原始数据，因此需要开辟一个新数组来存储打乱后的序列。Inside-Out Algorithm 算法的基本思想是设一游标 i 从前向后扫描原始数据的拷贝，在 [0,i] 之间随机一个小标 j，然后用位置 j 的元素替换掉位置 i 的数字，再用原始数据位置 i 的元素替换掉拷贝数据位置 j 的元素。

```java
    /**
     * Fisher-Yates Shuffle ：
     * 算法思想就是从原始数组中随机抽取一个新的数字到新数组中。
     *
     * @param nums
     * @return
     */
    public static int[] FYShuffle(int[] nums) {
        int[] result = new int[nums.length];
        boolean[] flags = new boolean[nums.length];
        Random random = new Random();
        int n = 0;
        while (n != nums.length) {
            int r = random.nextInt(nums.length);
            if (!flags[r]) {
                result[n++] = nums[r];
                flags[r] = true;
            }
        }
        return result;
    }

    /**
     * Knuth-Durstendeld Shuffle：
     * 每次从未处理的数据中随机取出一个数组，然后把该数字放在数组的尾部，即数组尾部存放的是已经处理过的数组。
     *
     * @param nums
     */
    public static void KDShuffle(int[] nums) {
        Random random = new Random();
        for (int i = nums.length - 1; i > 0; i--) {
            int r = random.nextInt(i);
            int temp = nums[r];
            nums[r] = nums[i];
            nums[i] = temp;
        }
    }

    /**
     * Inside-Out Shuffle：是一个 in-place 算法，原始数据被直接打乱，有些应用中可能需要保留原始数据，因此需要开辟一个新数组来存储打乱后的序列。
     * Inside-Out Algorithm 算法的基本思想是设一游标 i 从前向后扫描原始数据的拷贝，
     * 在 [0,i] 之间随机一个小标 j，然后用位置 j 的元素替换掉位置 i 的数字，再用原始数据位置 i 的元素替换掉拷贝数据位置 j 的元素。
     * @param nums
     */
    public static void IOShuffle(int[] nums){
        Random random = new Random();
        for (int i = 1;i<nums.length;i++){
            int r = random.nextInt(i);
            int temp = nums[i];
            nums[i] = nums[r];
            nums[r] = temp;
        }
    }
```

# 5. 介绍一下所有的map，以及他们之间的对比，适用场景。

## 5.1. list 与 Set、Map 区别及适用场景

1. List、Set 都是继承自 Collection 接口，Map 则不是。

2. List 特点：元素有放入顺序，元素可重复 。

   Set 特点：元素无放入顺序，元素不可重复，重复元素会覆盖掉，（注意：元素虽然无放入顺序，但是元素在set 中的位置是有该元素的 HashCode 决定的，其位置其实是固定的，加入 Set 的 Object 必须定义 equals() 方法 ，另外 list 支持 for 循环，也就是通过下标来遍历，也可以用迭代器，但是 set 只能用迭代，因为他无序，无法用下标来取得想要的值。） 

3. Set 和 List 对比： 
   Set：检索元素效率低下，删除和插入效率高，插入和删除不会引起元素位置改变。 
   List：和数组类似，List 可以动态增长，查找元素效率高，插入删除元素效率低，因为会引起其他元素位置改变。 

4. Map 适合储存键值对的数据。

5. 线程安全集合类与非线程安全集合类 

   LinkedList、ArrayList、HashSet 是非线程安全的，Vector 是线程安全的;
   HashMap 是非线程安全的，HashTable 是线程安全的;
   StringBuilder 是非线程安全的，StringBuffer 是线程安全的。

## 5.2. ArrayList 与 LinkedList 的区别和适用场景

* Arraylist：

优点：ArrayList 是实现了基于动态数组的数据结构，因为地址连续，一旦数据存储好了，查询操作效率会比较高（在内存里是连着放的）。

缺点：因为地址连续， ArrayList 要移动数据，所以插入和删除操作效率比较低。  

* LinkedList：

优点：LinkedList 基于链表的数据结构，地址是任意的，所以在开辟内存空间的时候不需要等一个连续的地址，对于新增和删除操作 add 和 remove，LinedList 比较占优势。LinkedList 适用于要头尾操作或插入指定位置的场景

缺点：因为 LinkedList 要移动指针，所以查询操作性能比较低。

* 适用场景分析：

当需要对数据进行对此访问的情况下选用 ArrayList，当需要对数据进行多次增加删除修改时采用 LinkedList。

## 5.3. ArrayList 与 Vector 的区别和适用场景

 ArrayList 有三个构造方法：

1. **public** ArrayList(**int** initialCapacity)//构造一个具有指定初始容量的空列表。  
2. **public** ArrayList()//构造一个初始容量为 10 的空列表。  
3. **public** ArrayList(Collection<? **extends** E> c)//构造一个包含指定 collection 的元素的列表。

 Vector 有四个构造方法：

1. **public** Vector()//使用指定的初始容量和等于零的容量增量构造一个空向量。  
2. **public** Vector(**int** initialCapacity)//构造一个空向量，使其内部数据数组的大小，其标准容量增量为零。  
3. **public** Vector(Collection<? **extends** E> c)//构造一个包含指定 collection 中的元素的向量  
4. **public** Vector(**int** initialCapacity,**int** capacityIncrement)//使用指定的初始容量和容量增量构造一个空的向量  

ArrayList 和 Vector 都是用数组实现的，主要有这么三个区别：

1. Vector 是多线程安全的，线程安全就是说多线程访问同一代码，不会产生不确定的结果。而 ArrayList 不是，这个可以从源码中看出，Vector 类中的方法很多有 synchronized 进行修饰，这样就导致了 Vector 在效率上无法与 ArrayList 相比；

2. 两个都是采用的线性连续空间存储元素，但是当空间不足的时候，两个类的增加方式是不同。

3. Vector可以设置增长因子，而 ArrayList 不可以。

4. Vector是一种老的动态数组，是线程同步的，效率很低，一般不赞成使用。

适用场景分析：

1. Vector 是线程同步的，所以它也是线程安全的，而 ArrayList 是线程异步的，是不安全的。如果不考虑到线程的安全因素，一般用 ArrayList 效率比较高。
2. 如果集合中的元素的数目大于目前集合数组的长度时，在集合中使用数据量比较大的数据，用 Vector 有一定的优势。

## 5.4. HashSet  与 Treeset 的适用场景

1. TreeSet 是二叉树（红黑树的树据结构）实现的，Treeset 中的数据是自动排好序的，不允许放入 null 值。 

2. HashSet 是哈希表实现的，HashSet 中的数据是无序的，可以放入 null，但只能放入一个 null，两者中的值都不能重复，就如数据库中唯一约束 。

3. HashSet 要求放入的对象必须实现 HashCode() 方法，放入的对象，是以 hashcode 码作为标识的，而具有相同内容的 String 对象，hashcode 是一样，所以放入的内容不能重复。但是同一个类的对象可以放入不同的实例。

适用场景分析: HashSet 是基于 Hash 算法实现的，其性能通常都优于 TreeSet。为快速查找而设计的 Set，我们通常都应该使用 HashSet，在我们需要排序的功能时，我们才使用 TreeSet。

## 5.5. HashMap 与 TreeMap、HashTable 的区别及适用场景

HashMap 非线程安全 

HashMap：基于哈希表实现。使用 HashMap 要求添加的键类明确定义了 hashCode() 和 equals( ) [ 可以重写 hashCode() 和 equals()]，为了优化 HashMap 空间的使用，您可以调优初始容量和负载因子。 

TreeMap：非线程安全，基于红黑树实现。TreeMap没有调优选项，因为该树总处于平衡状态。

HashTable：线程安全。 

适用场景分析：

HashMap 和 HashTable：HashMap 去掉了 HashTable 的 contains 方法，但是加上了 containsValue() 和 containsKey() 方法。HashTable 同步的，而 HashMap 是非同步的，效率上比 HashTable 要高。HashMap 允许空键值，而 HashTable 不允许。

HashMap：适用于 Map 中插入、删除和定位元素。 

Treemap：适用于按自然顺序或自定义顺序遍历键(key)。 

# 6. Map集合几种遍历方式的性能比较

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
1 //1、分别获取key和value的集合
2 for(String key : map.keySet()){
3     System.out.println(key);
4 }
5 for(Object value : map.values()){
6     System.out.println(value);
7 }
```

　　②、获取 key 集合，然后遍历 key 集合，根据 key 分别得到相应 value

```java
1 //2、获取key集合，然后遍历key，根据key得到 value
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

基本上使用第三种方法是性能最好的，

第一种遍历方法在我们只需要 key 集合或者只需要 value 集合时使用；

第二种方法效率很低，不推荐使用；

第四种方法效率也挺好，关键是在遍历的过程中我们可以对集合中的元素进行删除。

# 7. 请编程实现Java的生产者-消费者模型 

## 7.1. 使用 synchronized、wait、notifyAll 实现生产者-消费者模式

### 7.1.1. 概述

　　对于任何一种模式，在实现之前都应该明确这种模式对线程同步及互斥的要求。对于生产者-消费者模式，有如下同步及互斥要求：

1. 线程互斥要求
   * 生产者之间是互斥的，即同时只能有一个生产者进行生产
   * 消费者之间是互斥的，即同时只能有一个消费者进行消费
   * 生产者消费者之间是互斥的，也即生产者消费者不能同时进行生产和消费
2. 线程同步要求
   * 容器满时，生产者进行等待。
   * 容器空时，消费者进行等待。

　　有了上述需求，就可以选择互斥及同步工具了。对于互斥，采用 synchornized 关键字，对于线程同步，采用 wait()、notifyAll()。

### 7.1.2. 实现 

```java
/**
 * 使用 synchronized, wait(), notifyAll() 实现生产者-消费者模式
 */
public class syncTest {

    public static void main(String[] args) {
      	// 仓库
        Cache cache = new Cache(10);
        Producer p = new Producer(cache);
        Consumer c = new Consumer(cache);
        int producerCount = 4,consumerCount = 4;
      	// 4 个生产者
        for (int i = 0; i<producerCount;i++){
            new Thread(p).start();
        }
      	// 4 个消费者
        for (int i = 0; i<consumerCount;i++){
            new Thread(c).start();
        }
    }

    public static class Consumer implements Runnable {
        private Cache cache; // 仓库
        public Consumer(Cache cache){
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true){
              	// 消费
                cache.consume();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Producer implements Runnable {
        private Cache cache; // 仓库
        public Producer(Cache cache){
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true){
              	// 生产
                cache.produce();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Cache {
        private final static int MAX_SIZE = 10; // 最多容纳 10 个产品
        private int cacheSize = 0; // 当前产品的数量

        public Cache() {
            cacheSize = 0;
        }

        public Cache(int size) {
            cacheSize = size;
        }

      	// 生产
        public void produce() {
            synchronized (this) {
                while (cacheSize >= MAX_SIZE) { // 生产时，库存已满则等待
                    try {
                        System.out.println("缓存已满，生产者需要等待");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                cacheSize++;
                System.out.println("生产了一个产品。当前产品数量为：" + cacheSize);
              	// 生产之后，唤醒等待的消费者
                notifyAll();
            }
        }

      	// 消费
        public void consume() {
            synchronized (this) {
              	// 如果库存为空，则等待
                while (cacheSize <= 0) {
                    try {
                        System.out.println("缓存为空，消费者需要等待");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                cacheSize--;
                System.out.println("消费了一个产品。当前产品数量为：" + cacheSize);
                // 消费之后，唤醒等待的生产者。
              	notifyAll();
            }
        }
    }
    
}
```

## 7.2. 使用信号量实现生产者-消费者模式

### 7.2.1. 问题描述

　　问题描述：使用一个缓冲区来保存物品，只有缓冲区没有满，生产者才可以放入物品；只有缓冲区不为空，消费者才可以拿走物品。

　　因为缓冲区属于临界资源，因此需要使用一个互斥量 mutex 来控制对缓冲区的互斥访问。

### 7.2.2. 信号量

　　信号量 Semaphore，跟交通信号灯非常类似（Semaphore 翻译过来就是信号灯的意思），以下面这幅图为例：

![](/Users/miaomiao/Desktop/android/android_learning_notes/Java/线程/image/信号量1.png)　　如果两个铁轨都是空的，那么此时信号灯是绿色（信号量为 2），允许火车通行。如果有列车请求通行则放行，同时信号灯变为黄色（信号量减一）：

![](/Users/miaomiao/Desktop/android/android_learning_notes/Java/线程/image/信号量2.png)

　　当两条铁轨都有列车通行时，信号灯为红色（信号量为 0），不允许火车通过。如果有列车请求通行，则阻塞：

![](/Users/miaomiao/Desktop/android/android_learning_notes/Java/线程/image/信号量3.png)

　　当一辆列车离开轻轨后，信号灯变为黄色（信号量为 1），此时等待的通行的列车被放行：

![](/Users/miaomiao/Desktop/android/android_learning_notes/Java/线程/image/信号量4.png)

![](/Users/miaomiao/Desktop/android/android_learning_notes/Java/线程/image/信号量5.png)

### 7.2.3. 代码实现

　　为了同步生产者和消费者的行为，需要记录缓冲区中物品的数量。数量可以使用信号量来统计，这里需要使用两个信号量：empty 记录空缓冲区的数量，full 记录满缓冲区的数量。其中，empty 信号量是在生产者进程中使用，当 empty 不为 0 时，生产者才可以放入物品；full 信号量是在消费者进程中使用，当 full 信号量不为 0 时，消费者才可以取走物品。

　　注意，不能先对缓冲区进行加锁，再测试信号量。也就是说，不能先执行 down(mutex) 再执行 down(empty)。如果这么做了，那么可能会出现这种情况：生产者对缓冲区加锁后，执行 down(empty) 操作，发现 empty = 0，此时生产者睡眠。消费者不能进入临界区，因为生产者对缓冲区加锁了，消费者就无法执行 up(empty) 操作，empty 永远都为 0 ，导致生产者永远等待下去，不会释放锁，消费者因此也会永远等待下去。

　　这里先用一个二进制信号量来等效互斥操作。

　　由于信号量只能通过 0 值来进行阻塞和唤醒，所以这里必须使用两个信号量来模拟容器空和容器满两种状态。

　　库存对象类：

```java
public class Cache {

    private int cacheSize = 0;
    // 互斥量，控制对缓冲区的互斥访问。
    public Semaphore mutex;

    // empty 记录空缓冲区的数量
    // empty 信号量是在生产者进程中使用，当 empty 不为 0 时，生产者才可以放入物品
    // 保证了容器空的时候（empty 的信号量 <= 0），消费者等待
    public Semaphore empty;

    // full 记录满缓冲区的数量
    // full 信号量是在消费者进程中使用，当 full 信号量不为 0 时，消费者才可以取走物品
    // 保证了容器满的时候（full 的信号量 <= 0），生产者等待
    public Semaphore full;

    public Cache(int size) {
        mutex = new Semaphore(1);
        empty = new Semaphore(size);
        full = new Semaphore(0);
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void produce() throws InterruptedException {
        empty.acquire(); // 消耗一个空位
        mutex.acquire(); // 开启互斥
        cacheSize++;
        System.out.println("生产了一个产品，当前产品数为：" + cacheSize);
        mutex.release();
        full.release(); // 增加了一个产品
    }

    public void consume() throws InterruptedException {
        full.acquire(); // 消耗了一个产品
        mutex.acquire(); // 开启互斥
        cacheSize--;
        System.out.println("消费了一个产品，当前产品数为：" + cacheSize);
        mutex.release();
        empty.release(); // 增加了一个空位
    }
}
```

　　测试类，即生产与消费：

```java
public class ProducerAndConsumer {

    public static void main(String[] args) {
        Cache cache = new Cache(10); // 默认可存放的库存最大为 10

        Producer producer = new Producer(cache);
        // 四个生产者和四个消费者
        int producerCount = 4, consumerCount = 4;
        for (int i = 0; i < producerCount; i++) {
            new Thread(producer).start();
        }
        Consumer consumer = new Consumer(cache);
        for (int i = 0; i < consumerCount; i++) {
            new Thread(consumer).start();
        }

    }
	// 生产者
    public static class Producer implements Runnable {
        private Cache cache;

        public Producer(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cache.produce();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	// 消费者
    private static class Consumer implements Runnable {
        private Cache cache;

        public Consumer(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cache.consume();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

## 7.3. 使用管程实现生产者-消费者模式

### 7.3.1. 管程

　　使用信号量机制实现的生产者消费者问题需要客户端代码做很多控制，而管程把控制的代码独立出来，不仅不容易出错，也会使客户端代码调用更容易。

　　管程有一个重要特性：在一个时刻只能有一个进程使用管程。进程在无法继续执行的时候不能一直占用管程，否则其他进程永远不能使用管程。

　　管程引入了条件变量以及相关的操作：wait() 和 signal() 来实现同步操作。对条件变量执行 wait() 操作会导致调用进程阻塞，把管程让出来给另一个进程持有。signal() 操作用于唤醒被阻塞的进程。

### 7.3.2. 代码实现

　　Java 中可以使用 ReentrantLock 来实现管程。

　　使用两个 Condition 来完成，empty 来记录空缓冲区的数量，在生产者进程中使用，当 size 不大于 cacheSize 的时候才可以放入物品；full 来记录满缓冲区的数量，在消费者进程中使用，当 size 大于 0 的时候才 可以取出物品。

```java
public class Monitor {

    public static void main(String[] args) {
        Cache cache = new Cache(10);// 默认库房最多只能存放 10 个产品
        new Thread(new Producer(cache)).start();
        new Thread(new Consumer(cache)).start();
    }

    /**
     * 生产者
     */
    public static class Producer implements Runnable {
        private Cache cache;

        public Producer(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cache.produce();
                    Thread.sleep((int) (1000 * Math.random()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 消费者
     */
    public static class Consumer implements Runnable {
        private Cache cache;

        public Consumer(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cache.consume();
                    Thread.sleep((int) (1000 * Math.random()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 仓库
     */
    public static class Cache {
        private int cacheSize; // 最大库存
        private ReentrantLock reentrantLock; // 锁
        // 记录空缓冲区的数量
        // 在生产者进程中使用，当 size 不大于 cacheSize 的时候才可以放入物品
        private Condition empty;
        // 满缓冲区的数量
        // 在消费者进程中使用，当 size 大于 0 的时候才可以取出物品
        private Condition full;
        private int size = 0; // 现有库存

        public Cache(int size) {
            this.cacheSize = size;
            reentrantLock = new ReentrantLock();
            empty = reentrantLock.newCondition();
            full = reentrantLock.newCondition();
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public void produce() {
            try {
                // 加锁
                reentrantLock.lock();
                // 如果库存已满，则等待
                while (size >= cacheSize) {
                    empty.await();
                }
                size++;
                System.out.println("生产者生产，size:" + size);
                // 生产后，唤醒消费者
                full.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
        }

        public void consume() {
            try {
                // 加锁
                reentrantLock.lock();
                // 如果没有库存，则等待
                while (size <= 0) {
                    full.await();
                }
                size--;
                System.out.println("消费者消费，size:" + size);
                // 消费后，唤醒生产者
                empty.signal();
            } catch (
                    InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
        }
    }
}
```

# 8. 算法题：两个线程分别持续打印奇数和偶数，实现两个线程的交替打印（从小到大） 

使用 volatile 和 synchronized 实现

```java
public static class NumThread1 {
    private volatile int count = 1; // 可以使用 AutomInteger
    private int max = 100;

    public void showCount() {
        Thread A = new Thread(new CountA());
        Thread B = new Thread(new CountB());
        A.start();
        B.start();
    }

    public class CountA implements Runnable {

        @Override
        public void run() {
            while (count <= max) {
                synchronized (NumThread1.class) {
                    if (count <= max && count % 2 == 1) {
                        System.out.println(count++);
                    }
                }
            }
        }
    }

    public class CountB implements Runnable {

        @Override
        public void run() {
            while (count <= max) {
                synchronized (NumThread1.class) {
                    if (count <= max && count % 2 == 0) {
                        System.out.println(count++);
                    }
                }
            }
        }
    }
}
```

使用 syncrhonzied + volatile+wait+notify 实现：

```java
    // synchronized+wait+notify
    public static class NumThread2 {
        private volatile int count = 1;
        private int max = 100;
        private Object oneObject = new Object();
        private Object twoObject = new Object();

        public void showCount() {
            Thread A = new Thread(new CountA());
            Thread B = new Thread(new CountB());
            A.start();
            B.start();

        }

        public class CountA implements Runnable {

            @Override
            public void run() {
                while (count <= max) {
                    if (count % 2 == 0) {
                        synchronized (oneObject) {
                            try {
                                oneObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println(count++);
                        synchronized (twoObject) {
                            twoObject.notify();
                        }
                    }
                }
            }
        }

        public class CountB implements Runnable {

            @Override
            public void run() {
                while (count <= max) {
                    if (count % 2 == 1) {
                        synchronized (twoObject) {
                            try {
                                twoObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println(count++);
                        synchronized (oneObject) {
                            oneObject.notify();
                        }
                    }
                }
            }
        }
    }
```

ReentrantLock 实现：

```java
public static class NumThread3 {
        private AtomicInteger count = new AtomicInteger(1);
        private ReentrantLock reentrantLock = new ReentrantLock();
        private Condition one = reentrantLock.newCondition();
        private Condition two = reentrantLock.newCondition();
        private int max = 100;

        public void showCount() {
            Thread threadA = new Thread(new CountA());
            Thread threadB = new Thread(new CountB());
            threadA.start();
            threadB.start();
        }


        public class CountA implements Runnable {
            @Override
            public void run() {
                while (count.get() <= max) {
                    reentrantLock.lock();
                    try {
                        if (count.get() % 2 == 0) {
                            one.await();
                        }
                        if (count.get() <= max) {
                            System.out.println(count.getAndIncrement());
                        }
                        two.signal();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            }
        }

        public class CountB implements Runnable {
            @Override
            public void run() {
                while (count.get() <= max) {
                    reentrantLock.lock();
                    try {
                        if (count.get() % 2 == 1) {
                            two.await();
                        }
                        if (count.get() <= max) {
                            System.out.println(count.getAndIncrement());
                        }
                        one.signal();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            }
        }

    }
```

flag 实现

```java
    public static class NumThread4 {
        private AtomicInteger count = new AtomicInteger(1);
        private boolean flag = false;
        private static int MAX = 100;

        public void showCount() {
            Thread threadA = new Thread(new CountA());
            Thread threadB = new Thread(new CountB());
            threadA.start();
            threadB.start();
        }

        public class CountA implements Runnable {
            @Override
            public void run() {
                while (count.get() <= MAX) {
                    if (flag) {
                        System.out.println(count.getAndIncrement());
                        flag = !flag;
                    }
                }
            }
        }

        public class CountB implements Runnable {
            @Override
            public void run() {
                while (count.get() <= MAX) {
                    if (!flag) {
                        System.out.println(count.getAndIncrement());
                        flag = !flag;
                    }
                }
            }
        }

    }
```

# 9. 写一个单例

```java
public class Single {
    /**
     * 懒汉模式，线程不安全
     * 这种方式是最基本的实现方式，这种实现最大的问题就是不支持多线程。因为没有加锁 synchronized，所以严格意义上它并不算单例模式。
     * 这种方式 lazy loading 很明显，不要求线程安全，在多线程不能正常工作。
     */
    public static class Single1 {
        private static Single1 instance;

        private Single1() { }

        public static Single1 getInstance() {
            if (instance == null) {
                instance = new Single1();
            }
            return instance;
        }
    }

    /**
     * 饿汉模式，线程安全
     * 这种方式比较常用，但容易产生垃圾对象。 　　
     * 优点：没有加锁，执行效率会提高。 　　
     * 缺点：类加载时就初始化，浪费内存。 　　
     * 它基于 classloder 机制避免了多线程的同步问题，不过，instance 在类装载时就实例化，虽然导致类装载的原因有很多种，在单例模式中大多数都是调用 getInstance 方法，
     * 但是也不能确定有其他的方式（或者其他的静态方法）导致类装载，这时候初始化 instance 显然没有达到 lazy loading 的效果。
     */
    public static class Single2 {
        private static Single2 instance = new Single2();

        private Single2() { }

        public static Single2 getInstance() {
            return instance;
        }
    }

    /**
     * 线程安全,双重锁
     * 这种方式采用双锁机制，安全且在多线程情况下能保持高性能。
     * getInstance() 的性能对应用程序很关键。
     */
    public static class Single3 {
        private volatile static Single3 instance;

        private Single3() { }

        public static Single3 getInstance() {
            if (instance == null) {
                synchronized (Single3.class) {
                    if (instance == null) {
                        instance = new Single3();
                    }
                }
            }
            return instance;
        }
    }

    /**
     * 静态内部类
     * 这种方式能达到双检锁方式一样的功效，但实现更简单。
     * 对静态域使用延迟初始化，应使用这种方式而不是双检锁方式。
     * 这种方式只适用于静态域的情况，双检锁方式可在实例域需要延迟初始化时使用。
     * 这种方式同样利用了 classloder 机制来保证初始化 instance 时只有一个线程，
     * 它跟饿汉模式方式不同的是：饿汉模式方式只要 Singleton 类被装载了，那么 instance 就会被实例化（没有达到 lazy loading 效果），
     * 而这种方式是 SingletonHolder 类被装载了，instance 不一定被初始化。因为 SingletonHolder 类没有被主动使用，只有显示通过调用 getInstance 方法时，
     * 才会显示装载 SingletonHolder 类，从而实例化 instance。想象一下，如果实例化 instance 很消耗资源，所以想让它延迟加载，
     * 另外一方面，又不希望在 Singleton 类加载时就实例化，因为不能确保 Singleton 类还可能在其他的地方被主动使用从而被加载，那么这个时候实例化 instance 显然是不合适的。
     * 这个时候，这种方式相比饿汉模式方式就显得很合理。
     */
    public static class Single5 {

        private static class SingleHolder {
            private static final Single5 instance = new Single5();
        }

        private Single5() { }

        public static final Single5 getInstance() {
            return SingleHolder.instance;
        }
    }


    /**
     * 懒汉模式，线程安全
     * 这种方式具备很好的 lazy loading，能够在多线程中很好的工作，但是，效率很低，99% 情况下不需要同步。 　　
     * 优点：第一次调用才初始化，避免内存浪费。 　　
     * 缺点：必须加锁 synchronized 才能保证单例，但加锁会影响效率。getInstance() 的性能对应用程序不是很关键（该方法使用不太频繁）。
     */
    public static class Single6{
        private static Single6 instance;
        private Single6(){}
        public static synchronized Single6 getInstance(){
            if (instance == null){
                instance = new Single6();
            }
            return instance;
        }

    }

    /**
     * 枚举
     *
     * 这种实现方式还没有被广泛采用，但这是实现单例模式的最佳方法。它更简洁，自动支持序列化机制，绝对防止多次实例化。
     * 这种方式是 Effective Java 作者 Josh Bloch 提倡的方式，它不仅能避免多线程同步问题，而且还自动支持序列化机制，
     * 防止反序列化重新创建新的对象，绝对防止多次实例化。
     * 不过，由于 JDK1.5 之后才加入 enum 特性，用这种方式写不免让人感觉生疏，在实际工作中，也很少用。 　　
     * 不能通过 reflection attack 来调用私有构造方法。
     */
    public enum Single7{
        INSTANCE;
        private void method(){

        }
    }
    /**
     * 一般情况下，不建议使用懒汉方式，建议使用第 3 种饿汉方式。
     * 只有在要明确实现 lazy loading 效果时，才会使用静态内部类登记方式。
     * 如果涉及到反序列化创建对象时，可以尝试使用枚举方式。如果有其他特殊的需求，可以考虑使用双检锁方式。
     */
}

```

# 4. 文件 IO 操作

# 5. AtomInteger

AtomInteger