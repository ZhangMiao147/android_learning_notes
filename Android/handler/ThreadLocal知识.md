# ThreadLocal 知识

## 1. 概述

　　ThreadLocal 并不是一个 Thread，而是 Thread 的局部变量，它的作用是可以在每个线程中存储数据。

　　ThreadLocal 是一个线程内部的数据存储类，通过它可以在指定的线程中存储数据，数据存储以后，只是在指定线程中可以获取到存储的数据，对于其他线程来说无法获取到数据。

　　当使用 ThreadLocal 维护变量时，ThreadLocal 为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其他线程所对应的副本。

## 2. ThreadLocal 的使用场景

　　在日常开发中用到 ThreadLocal 的地方较少，但是在某些特殊的场景下，通过 ThreadLocal 可以轻松地实现一些看起来很复杂的功能，这一点在 Android 的源码中也有所体现，比如 Looper、ActivityThread 以及 AMS 中都用到了 ThreadLocal。

1. 一般来说，当某些数据是以线程为作用域并且不同线程具有不同的数据副本的时候，就可以考虑采用 ThreadLocal。

   比如对于 Handler 来说，它需要获取当前线程的 Looper，很显然 Looper 的作用域就是线程并且不同线程具有不同的 Looper，这个时候通过 ThreadLocal 就可以轻松实现 Looper 在线程中的存取，如果不采用 ThreadLocal，那么系统就必须提供一个全局的哈希表供 Handler 查找指定线程的 Looper，这样一来就必须提供一个类似于 LooperManager 的类了，但是系统并没有这么做而是选择了 ThreadLocal，这就是 ThreadLocal 的好处。

2. ThreadLocal 另一个使用场景是复杂逻辑下的对象传递。

   比如监听器的传递，有些时候一个线程中的任务过于复杂，这可能表现为函数调用栈比较深以及代码入口的多样性，在这种情况下，又需要监听器能够贯穿整个线程的执行过程，其实可以采用 ThreadLocal，采用 ThreadLocal 可以让监听器作为线程内的全局对象而存在，在线程内部只要通过 get 方法就可以获取到监听器。

   而如果不采用 ThreadLocal，那么能想到的可能是如下两种方法：第一种方法是将监听器通过参数的形式在函数调用栈中进行传递，第二种方法就是将监听器作为静态变量供线程访问。

   上述这两种方法都是有局限性的。第一种方法的问题是当程序调用栈很深的时候，通过函数参数来传递监听器对象这几乎是不可接受的，这会让程序的设计看起来很糟糕。第二种方法是可以接受的，但是这种状态是不具有可扩充性的，比如如果同时有两个线程在执行，那么就需要提供两个静态的监听器对象，如果有很多线程在并发执行，就需要提供很多个静态的监听器对象，这显然是不可思议的，而采用 ThreadLocal 每个监听器对象都在自己的线程内部存储，根本就不会有方法二的这种问题。

## 3. 使用

```java
// 声明 ThreadLocal 存储 Boolean 类型的数据    

final ThreadLocal<Boolean> mBooleanThreadLocal = new ThreadLocal<Boolean>();
// 主线程设置当前线程的 mBooleanThreadLocal 的值为 true
mBooleanThreadLocal.set(true);
System.out.println("[Thread#main]mBooleanThreadLocal=" + mBooleanThreadLocal.get());

	new Thread("Thread#1") {
            @Override
            public void run() {
                // 线程 1 设置线程 1 的 mBooleanThreadLocal 的值为 false
                mBooleanThreadLocal.set(false);
                System.out.println("[Thread#1]mBooleanThreadLocal=" + mBooleanThreadLocal.get());
            }
        }.start();
  	new Thread("Thread#2") {
            @Override
            public void run() {
                // 线程 2 不设置线程 2 的 mBooleanThreadLocal 的值
                System.out.println("[Thread#2]mBooleanThreadLocal=" + mBooleanThreadLocal.get());
            }
        }.start();
```

　　分别在主线程、子线程 1 和子线程 2 中设置和打印它的值，结果如下：

```java
[Thread#main]mBooleanThreadLocal=true
[Thread#1]mBooleanThreadLocal=false
[Thread#2]mBooleanThreadLocal=null
```

　　虽然在不同线程中访问的是同一个 ThreadLocal 对象，但是它们通过 ThreadLocal 来获取到的值却是不一样的，这就是 ThreadLocal 的奇妙之处。

　　ThreadLocal 之所以有这么奇妙的效果，是因为不同线程访问同一个 ThreadLocal 的 get 方法，ThreadLocal 内部会从各自的线程中取出一个数组，然后再从数据中根据当前 ThreadLocal 的索引去查找对应的 value，很显然，不同线程中的数组是不同的，这就是为什么通过 ThreadLocal 可以在不同的线程中维护一套数据的副本并且彼此互不干扰。

## 4. ThreadLocal 的内部实现

　　ThreadLocal 是一个泛型类，它的定义为 public class ThreadLocal< T > ，只需要弄清楚 ThreadLocal 的 get 和 set 方法就可以明白它的工作原理。

### 4.1. ThreadLocal#set

```java
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            map.set(this, value);
        } else {
            createMap(t, value);
        }
    }
```

　　在 set 方法中，首先会通过 getMap 方法来获取当前线程中的 ThreadLocalMap 数据，获取的方式也是很简单的，getMap 方法会获取 Thread 的 threadLocals 字段，`ThreadLocal.ThreadLocalMap threadLocals`，threadLocals  在 Thread 类专门用来存储线程的 ThreadLocalMap 数据。ThreadLocalMap 类 里面有一个 `private Entry[] table;` 用来存储 ThreadLocal 数据，因此获取当前线程的 ThreadLocal 数据就变得异常简单了。如果 map 的值为 null，那么就需要对其进行初始化，初始化后再将 ThreadLocal 的值进行存储。

　　接下来看 ThreadLocal 的值是怎么在 ThreadLocalMap 中进行存储的。在 ThreadLocalMap 内部有静态类 Entry：

```java
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
```

　　在 ThreadLocalMap 内部有一个 Entry 的数组：private Entry[] table，所以 ThreadLocal 就存储在这个数组中，下面看 ThreadLocalMap 是如何使用 set 方法将 ThreadLocal 的值存储到 table 数组中的。

### 4.2. ThreadLocalMap#set

```java
        private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            // ThreadLocal 的存储位置
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
```

　　ThreadLocal 的值在 table 数组中的存储位置总是为 ThreadlLocal 的 threadLocalHashCode & (len-1) 的位置。

### 4.3. ThreadLocal#get

```java
    public T get() {
        Thread t = Thread.currentThread();
        // 获取 t 线程的 ThreadLocalMap
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                // 拿到线程对应的 T
                return result;
            }
        }
        return setInitialValue();
    }
```

　　ThreadLocal 的 get 方法是取出当前线程的 ThreadLocalMap 对象，如果这个对象为 null 那么就返回初始值，如果不为空，则从ThreadLocalMap 中拿出当前当前线程对应的 Entry，拿到 entry 存储的 value。

　　初始值由 setInitialValue() 方法来描述，默认情况下为 null，也可以重写 setInitialValue() 方法，它的默认实现如下所示：

``` java
    private T setInitialValue() {
        // 初始化 T 类型的数值
        T value = initialValue();
        // 当前线程
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            // 调用 ThreadLocalMap 的 set 方法将默认值存储起来
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }
```

　　如果 map 对象不为 null，那就调用 ThreadLocalMap 的 set 方法，将当前线程和 T 的默认值加入。

　　从 ThreadLocal 的 set 和 get 方法可以看出，它们所操作的对象都是当前线程的 Entry 数组，因此在不同线程中访问同一个 ThreadLocal 的 set 和 get 方法，它们对 ThreadLocal 所做的读写操作仅限于各自线程的内部，这就是为什么 ThreadLocal 可以在多个线程中互不干扰地存储和修改数据，理解 ThreadLocal 的实现方式有助于理解 Looper 的工作原理。

## 5. 参考文章

[Android的消息机制之ThreadLocal的工作原理](https://blog.csdn.net/singwhatiwanna/article/details/48350919)

