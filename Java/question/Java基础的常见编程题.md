# Java 基础的常见编程题

[TOC]

# 1. 请编程实现Java的生产者-消费者模型 

## 1.1. 使用 synchronized、wait、notifyAll 实现生产者-消费者模式

### 1.1.1. 概述

　　对于任何一种模式，在实现之前都应该明确这种模式对线程同步及互斥的要求。对于生产者 - 消费者模式，有如下同步及互斥要求：

1. 线程互斥要求
   * 生产者之间是互斥的，即同时只能有一个生产者进行生产
   * 消费者之间是互斥的，即同时只能有一个消费者进行消费
   * 生产者消费者之间是互斥的，也即生产者消费者不能同时进行生产和消费
2. 线程同步要求
   * 容器满时，生产者进行等待。
   * 容器空时，消费者进行等待。

　　有了上述需求，就可以选择互斥及同步工具了。对于互斥，采用 synchornized 关键字，对于线程同步，采用 wait()、notifyAll()。

### 1.1.2. 实现 

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

## 1.2. 使用信号量实现生产者-消费者模式

### 1.2.1. 问题描述

　　问题描述：使用一个缓冲区来保存物品，只有缓冲区没有满，生产者才可以放入物品；只有缓冲区不为空，消费者才可以拿走物品。

　　因为缓冲区属于临界资源，因此需要使用一个互斥量 mutex 来控制对缓冲区的互斥访问。

### 1.2.2. 信号量

　　信号量 Semaphore，跟交通信号灯非常类似（Semaphore 翻译过来就是信号灯的意思），以下面这幅图为例：

![](线程/image/信号量1.png)　　如果两个铁轨都是空的，那么此时信号灯是绿色（信号量为 2），允许火车通行。如果有列车请求通行则放行，同时信号灯变为黄色（信号量减一）：

![](线程/image/信号量2.png)

　　当两条铁轨都有列车通行时，信号灯为红色（信号量为 0），不允许火车通过。如果有列车请求通行，则阻塞：

![](线程/image/信号量3.png)

　　当一辆列车离开轻轨后，信号灯变为黄色（信号量为 1），此时等待的通行的列车被放行：

![](线程/image/信号量4.png)

![](线程/image/信号量5.png)

### 1.2.3. 代码实现

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

## 1.3. 使用管程实现生产者-消费者模式

### 1.3.1. 管程

　　使用信号量机制实现的生产者消费者问题需要客户端代码做很多控制，而管程把控制的代码独立出来，不仅不容易出错，也会使客户端代码调用更容易。

　　管程有一个重要特性：在一个时刻只能有一个进程使用管程。进程在无法继续执行的时候不能一直占用管程，否则其他进程永远不能使用管程。

　　管程引入了条件变量以及相关的操作：wait() 和 signal() 来实现同步操作。对条件变量执行 wait() 操作会导致调用进程阻塞，把管程让出来给另一个进程持有。signal() 操作用于唤醒被阻塞的进程。

### 1.3.2. 代码实现

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

# 2. 算法题：两个线程分别持续打印奇数和偶数，实现两个线程的交替打印（从小到大）

## 2.1.  使用 volatile 和 synchronized 实现

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

## 2.2. 使用 syncrhonzied + volatile+wait+notify 实现

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

## 2.3. ReentrantLock 实现

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

## 2.4. flag 实现

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

