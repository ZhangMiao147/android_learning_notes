# 使用 synchronized、wait、notifyAll 实现生产者-消费者模式

　　对于任何一种模式，在实现之前都应该明确这种模式对线程同步及互斥的要求。对于生产者-消费者模式，有如下同步及互斥要求：

1. 线程互斥要求
   * 生产者之间是互斥的，也即同时只能有一个生产者进行生产
   * 消费者之间是互斥的，也即同时只能有一个消费者进行消费
   * 生产者消费者之间是互斥的，也即生产者消费者不能同时进行生产喝消费
2. 线程同步要求
   * 容器满时，生产者进行等待。
   * 容器空时，消费者进行等待。

　　有了上述需求，就可以选择互斥及同步工具了。对于互斥，采用 synchornized 关键字，对于线程同步，采用 wait()、notify()。

　　实现如下：

```java
/**
 * 使用synchronized, wait(), notifyAll() 实现生产者-消费者模式
 */
public class syncTest {

    public static void main(String[] args) {
        Cache cache = new Cache(10);
        Producer p = new Producer(cache);
        Consumer c = new Consumer(cache);
        int producerCount = 4,consumerCount = 4;
        for (int i = 0; i<producerCount;i++){
            new Thread(p).start();
        }
        for (int i = 0; i<consumerCount;i++){
            new Thread(c).start();
        }
    }

    public static class Consumer implements Runnable {
        private Cache cache;
        public Consumer(Cache cache){
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true){
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
        private Cache cache;
        public Producer(Cache cache){
            this.cache = cache;
        }

        @Override
        public void run() {
            while (true){
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
        private final static int MAX_SIZE = 10;
        private int cacheSize = 0;

        public Cache() {
            cacheSize = 0;
        }

        public Cache(int size) {
            cacheSize = size;
        }

        public void produce() {
            synchronized (this) {
                while (cacheSize >= MAX_SIZE) {
                    try {
                        System.out.println("缓存已满，生产者需要等待");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                cacheSize++;
                System.out.println("生产了一个产品。当前产品数量为：" + cacheSize);
                notifyAll();
            }
        }

        public void consume() {
            synchronized (this) {
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
            }
        }
    }
    
}
```




## 参考文章
1. [java 使用synchronized, wait(), notifyAll() 实现生产者-消费者模式](https://blog.csdn.net/whueratsjtuer/article/details/88964553)