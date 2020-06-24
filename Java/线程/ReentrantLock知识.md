# ReentrantLock

## 1. 什么是 ReentrantLock

### 1.1. ReentrantLock 与 synchronized 区别

**ReentrantLock**

* ReentrantLock 是 JDK 方法，需要手动声明上锁和释放锁，因此语法相对复杂些；如果忘记释放锁容易导致死锁。
* ReentrantLock 具有更好的细粒度，可以在 ReentrantLock 里面设置内部 Condition 类，可以实现分组唤醒需要唤醒的线程。
* ReentrantLock 能实现公平锁。

**synchronized**

* synchronized 语法上简洁方便。
* synchronized 是 JVM 方法，由编辑器保证加锁和释放。

### 1.2. ReentrantLock 特征介绍

　　Java 的 java.util.concurrent 框架中提供了 ReentrantLock 类（于 JAVA SE 5.0 时引入），ReentrantLock 实现了 lock 接口，具体在 JDK 中的定义如下：

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * Creates an instance of {@code ReentrantLock} with the
     * given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
}
```

　　看到一个类首先就需要知道它的构造方法有哪些，ReentrantLock 有两个构造方法，一个是无参的 ReentrantLock()；另一个含有布尔参数 public ReentrantLock(boolean fair)。后面一个构造函数说明 ReentrantLock 可以新建公平锁；而 synchronized 只能建立非公平锁。

　　那么 Lock 接口的方法：

```java
public interface Lock {
    void lock();
    void lockInterruptibly() throws InterruptedException;
    boolean tryLock();
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    void unlock();
    Condition newCondition();
}
```

　　Lock 接口中有 lock 和 unlock 方法，还有 newCondition() 方法，这就是上面说的 ReentrantLock 里面设置内部 Condition 类。由于 ReentrantLock 实现了 Lock 接口，因此它必须实现该方法，具体如下：

```java
    public Condition newCondition() {
        return sync.newCondition();
    }
```

　　返回 Condition 类的一个实例。

## 2. ReentrantLock 其它方法介绍

　　在介绍它的其他方法前，要先明白它的使用方法，以下 JDK 中的建议：

```java
     private final ReentrantLock lock = new ReentrantLock();
      // ...
  
      public void m() {
       lock.lock();  // block until condition holds
       try {
        // ... method body
        } finally {
        lock.unlock()
      }
    }
```

　　建议用 try，在 finally 里面一定要释放锁，防止被中断时锁没释放，造成死锁。

### lock()

```java
    public void lock() {
        sync.acquire(1);
    }
```

　　如果该锁没被其他线程获得，则立即返回；并且把 lock hold count 的值变为 1。

### unlock()

```java
    public void unlock() {
        sync.release(1);
    }
```

　　如果当前线程是该锁的持有者，则保持计数递减。如果保持计数现在为零，则锁定被释放。如果当前线程不是该锁的持有者，则派出 IllegalMonitorStateException。

### isFair()

```java
    public final boolean isFair() {
        return sync instanceof FairSync;
    }
```

　　判断该锁是不是公平锁。

### newCondition()

```java
    public Condition newCondition() {
        return sync.newCondition();
    }
```

　　返回新的 ConditionObject 对象。

## 3. Condiation 接口中的方法

* await()：

```java
    void await() throws InterruptedException;
```

　　Condition 接口中的方法，导致当前线程等待发信号。

* siginal()

```java
// AbstractQueuedSynchronzier 的内部类 ConditionObject 
    public class ConditionObject implements Condition, java.io.Serializable {
public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }
}
```

　　唤醒一个等待该条件的线程去获取锁（第一个）。

* signalAll()：唤醒所有等待线程。

## ReentrantLock 实例

　　使用 ReentrantLock 实现转账。

```java
/**
 * 模拟转账，把钱从一个账户转到另一个账户
 */
public class ReentrantLockUse {
    public static final int NACCOUNTS = 100;
    public static final double INITIAL_BALANCE = 1000;
    public static final double MAX_AMOUNT = 1000;
    public static final int DELAY = 10;

    public static void main(String[] args) {
        Bank bank = new Bank(NACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < NACCOUNTS; i++) {
            int fromAccount = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            int toAccount = (int) (bank.size() * Math.random());
                            double amount = MAX_AMOUNT * Math.random();
                            bank.transfer(fromAccount, toAccount, amount);
                            Thread.sleep((int)(DELAY*Math.random()));
                        }
                    } catch (InterruptedException e) {

                    }
                }
            };
            Thread t = new Thread(r); // 新键线程
            t.start();
        }
    }

    static class Bank {
        private final double[] account; // 账户
        private Lock bankLock; // 重复锁
        private Condition sufficientFunds; // 条件对象

        public Bank(int n, double initialBalance) {
            account = new double[n];
            Arrays.fill(account, initialBalance);
            bankLock = new ReentrantLock(); // 构造对象时，实例化锁
            sufficientFunds = bankLock.newCondition(); // 新键条件对象
        }

        /**
         * 转装，把 from 账户里面的钱转到 to 里面，金额是 amount
         *
         * @param from
         * @param to
         * @param amount
         */
        public void transfer(int from, int to, double amount) {
            bankLock.lock();
            try {
                while (account[from] < amount) {
                    sufficientFunds.await();
                }
                System.out.println(Thread.currentThread());
                account[from] -= amount;
                System.out.printf("%10.2f from %d to %d", amount, from, to);
                account[to] += amount;
                System.out.printf(",Total Balance:%10.2f%n", getTotalBalance());
                sufficientFunds.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bankLock.unlock();
            }
        }

        /**
         * 做的所有账户总额
         *
         * @return
         */
        public double getTotalBalance() {
            bankLock.lock();
            try {
                double sum = 0;
                for (double a : account) {
                    sum += a;
                }
                return sum;
            } finally {
                bankLock.unlock();
            }
        }

        public int size() {
            return account.length;
        }
    }
}
```

　　输出结果：

```java
Thread[Thread-0,5,main]
    630.57 from 0 to 26,Total Balance: 100000.00
Thread[Thread-0,5,main]
    552.96 from 0 to 98,Total Balance: 100000.00
Thread[Thread-39,5,main]
    116.30 from 39 to 64,Total Balance: 100000.00
Thread[Thread-86,5,main]
    309.93 from 86 to 72,Total Balance: 100000.00
Thread[Thread-8,5,main]
     37.50 from 8 to 35,Total Balance: 100000.00
Thread[Thread-35,5,main]
    243.91 from 35 to 18,Total Balance: 100000.00
Thread[Thread-74,5,main]
    332.62 from 74 to 22,Total Balance: 100000.00
Thread[Thread-20,5,main]
    452.08 from 20 to 28,Total Balance: 100000.00
```

　　结果分析：

　　循环建立 100 个线程，每个线程都在不停转账，由于 ReentrantLock 的使用，任何时刻所有账户的总额都保持不变。另外，把钱 amount 从 A 账户转到 B 账户，要先判断 A 账户中是否有这么多钱，不过没有就调用条件对象 ConditionObject 的 await() 方法，放弃该线程，等待其他线程转钱进来；转钱完成后调用 siginalAll()。


## 参考文章
1. [JAVA中ReentrantLock详解](https://www.cnblogs.com/java-learner/p/9651675.html)