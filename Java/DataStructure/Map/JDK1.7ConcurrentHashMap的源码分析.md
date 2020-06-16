# JDK 1.7 ConcurrentHashMap的源码分析

## 1. 概述

　　ConcurrentHashMap 是由 Segment 和 HashEntry 组成的。

　　Segment 是一种可重入的锁（Reentranlock），Segment 在其中扮演锁的角色，HashEntry 用于存储数据。

　　一个 ConcurrentHashMap 包括一个 Segment 数组。一个 Segment 元素包括一个 HashEntry 数组，HashEntry 是一种链表式的结构，每一个 Segment 维护着 HashEntry 数组中的元素，当要对 HashEntry 中的数据进行修改的时候，必须先要获得与它对应的 Segment。这样的话，当修改该容器的不同的段时，将不会存在并发的问题。

　　得到一个元素需要进行两次 hash 操作，第一次得到 Segment，第二次得到 HashEntry 中的链表头部，这样做会使得 Hash 的过程比普通的 HashMap 要长。

　　写操作的时候可以只对元素所在的 Segment 进行加锁即可，不会影响到其他的 Segment，这样，在最理想的情况下，ConcurrentHashMap 可以最高同时支持 Segment 数量大小的写操作（刚好这些写操作都非常平均的分布在所有的 Segment 上）。

　　ConcurrentHashMap 采用了非常精妙的 “ 分段锁 ” 策略，ConcurrentHashMap 的主干是个 Segment 数组。

```java
final Segment<K,V>[] segments;
```

　　Segment 继承了 ReentrantLock，所以它就是一个可重入锁（ReentrantLock）。在 ConcurrentHashMap，一个 Segment 就是一个子哈希表，Segment 里维护了一个 HashEntry 数组，并发环境下，对于不同 Segment 的数据进行操作是不用考虑锁竞争的。

　　所以，对于同一个 Segment 的操作才需考虑线程同步，不同的 Segment 则无需考虑。

　　HashEntry 是目标提到的最小的逻辑处理单元了。一个 ConcurrentHashMap 维护一个 Segment 数组，一个 Segment 维护一个 HashEntry 数组。

```java
static final class HashEntry<K,V> {
        final int hash;
        final K key;
        volatile V value;
        volatile HashEntry<K,V> next;
        //其他省略
}
```

## 2. ConcurrentHashMap 的构造方法

```java
public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
    		// MAX_SEGMENTS 为 1<< 16= 65535，也就是最大并发数为 65535
        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;
        // Find power-of-two sizes best matching arguments
    		// 2 的 sshift 次方等于 ssize
        int sshift = 0;
    		// ssize 为 segments 数组长度，根据 concurrentLevel 计算得出
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;//代表 ssize 转换的次数
            ssize <<= 1;
        }
    		// segmentShift 和 segmentMask 这两个变量在定位 segment 时会用到
        this.segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
    	// 计算 cap 的大小，即 Segment 中 HashEntry 的数组长度，cap 也一定为 2 的 n 次方
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity)
            ++c;
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        while (cap < c)
            cap <<= 1;
        // 创建 segments 数组并初始化第一个 Segment，其余的 Segment 延迟初始化
        Segment<K,V> s0 =
            new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
                             (HashEntry<K,V>[])new HashEntry[cap]);
        Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
        UNSAFE.putOrderedObject(ss, SBASE, s0); // ordered write of segments[0]
        this.segments = ss;
    }
```

　　初始化方法有三个参数，如果用户不指定则会使用默认值，initialCapacity 为 16，loadFactor 为 0.75（负载因子，扩容时需要参考），concurrencyLevel 为 16。

　　从上面的代码可以看出来，Segment 数组的大小 ssize 是由 concurrencyLevel 来决定的，但是却不一定等于 concurrencyLevel，ssize 一定是大于或等于 concurrencyLevel 的最小的 2 的次幂。比如：默认情况下 concurrencyLevel 是 16，则 sszie 为 16，若 concurrencyLevel 为 14，ssize 为 16，若 concurrencyLevel 为 17，则 ssize 为 32。

　　为什么 Segment 的数组大小一定是 2 的次幂？其实主要是便于通过按位与的散列算法来定位 Segment 的 index。

　　和 HashMap 对比来分析，HashMap 是 entry< K,V >[]，而 chm 就是 segments< K,V >。可以说每一个 segment 都是一个 HashMap，想要进入 Segment 还需要获取对应的锁。默认 ConcurrentHashMap 的 Segment 数是 16。每个 segment 内的 HashEntry 数组大小也是 16 个。threshold 是 16*0.75。

　　ConcurrentHashMap 如何发生 ReHash？

　　concurrencyLevel 一旦设定的话，就不会改变。ConcurrentHashMap 当元素个数大于临界值的时候，就会发生扩容。但是 ConcurrentHashMap 与其他的 HashMap 不同的是，它不会对 Segment 数量增大，只会增加 Segment 后面的链表容量的大小。即对每个 Segment 的元素进行的 ReHash 操作。

### 2.1. Segment

```java
    static final class Segment<K,V> extends java.util.concurrent.locks.ReentrantLock implements Serializable {

        private static final long serialVersionUID = 2249069246763182397L;

        /**
         * 在可能阻塞获取以准备锁定段操作之前，在预扫描中尝试锁定的最大次数。
         * 在多处理器上，使用有限的重试次数来维护在定位节点时获取的缓存。
         */
        static final int MAX_SCAN_RETRIES =
            Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;

        /**
         * 每段表。
         * 在定位节点时，元素通过 entryAt/setEntryAt 来提供 volatile 语义。
         */
        transient volatile HashEntry<K,V>[] table;

        /**
         * 元素的数量。
         * 只能在锁内或其他保持可见性的 volatile 读取中访问。
         */
        transient int count;

        /**
         * 此段 segment 中变化的总数。
         */
        transient int modCount;

        /**
         * 当表的大小超过此阈值时，将重新 hash 此表。
         * threshold = (int)(capacity*loadFactory)。
         */
        transient int threshold;

        /**
         * 哈希表的加载因子。
         * @serial
         */
        final float loadFactor;
        
        Segment(float lf, int threshold, HashEntry<K,V>[] tab) {
            this.loadFactor = lf;//负载因子
            this.threshold = threshold;//阈值
            this.table = tab;//主干数组即HashEntry数组
        }
        
    }

```

　　Segment 类似于 HashMap，一个 Segment 维护着一个 HashEntry 数组。

　　Segment 类似哈希表，那么一些属性就和 HashMap 差不多，比如负载因子 loadFactory，比如阈值 threshold 等等。

## 3. put 方法 - 添加数据

```java
public V put(K key, V value) {
    Segment<K,V> s;
    // concurrentHashMap 不允许 key/value 为空
    if (value == null)
        throw new NullPointerException();
    // hash 函数对 key 的 hashCode 重新散列，避免差劲的不合理的 hashcode，保证散列均匀
    int hash = hash(key);
   	// 返回的 hash 值无符号右移 segmentShift 位与段掩码进行位运算，定位 segment
    int j = (hash >>> segmentShift) & segmentMask;
    // 返回的 hash 值无符号右移 segmentShift 位与段掩码进行位运算，定位 segment
    if ((s = (Segment<K,V>)UNSAFE.getObject          
         // nonvolatile; recheck
         (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
         	s = ensureSegment(j);
     return s.put(key, hash, value, false);
}   
```

　　在 JDK 中，native 方法的实现是没办法看的，需要下载 OpenJDK 来看。在 put 方法中实际是需要判断是否需要扩容的，扩容的时机选在阈值（threadshold）装满时，而不像 hashmap 是在装入后，再判断是否装满并扩容。这就是 ConcurrentHashMap 的好处，有可能会出现扩容后就没有新数据的情况。

　　从源码看出，put 的主要逻辑也就两步：

1. 定位 segment 并确保 Segment 已初始化。
2. 调用 Segment 的 put 方法。

### 3.1. 关于 segmentShift 和 segmentMask

　　segmentMask：段掩码，加如 segment 数组长度为 16，则段掩码为 16-1 = 15；segments 长度为 32，段掩码为 32-1 = 31。这样得到的所有 bit 位都为1，可以更好地保证散列地均匀性。

　　segmentShift：2 的 sshift 次方等于 ssize，segmentShift = 32 -sshift。若 segments 长度为 16。segmentShift  = 32- 4 = 28，若 segments 长度为 32，segmentShift = 32-5 = 27。而计算得出的 hash 值最大为 32，无符号右移 segmentShift，则意味着只保留高几位（其余位是没用的），然后与段掩码 segmentMask 位运算来定位 Segment。

### 3.2. ConcurrentHashMap#hash()

```java
private int hash(Object k) {
        int h = hashSeed;

        if ((0 != h) && (k instanceof String)) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

        h ^= k.hashCode();

        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }
```

　　这里对 key 的 hash 值再哈希了一次。使用的方法是 wang/jenking 的哈希算法，这里再 hash 是为了减少 hash 冲突。如果不这样做的话，会出现大多数值都在一个 segment 上，这样就失去了分段锁的意义。

### 3.3. Segment#put

　　put() 方法最后调用的是 Segment 的 put 方法，Segment 中的 put 方法是要加锁的，只不过是锁粒度细了而已。

```java
        final V put(K key, int hash, V value, boolean onlyIfAbsent) {
            // tryLock() 是 ReentrantLock 获取锁一个方法。如果当前线程获取锁成功，返回 true，如果别的线程获取了锁返回 false，
            // tryLock 不成功时会遍历定位到的 HashEntry 位置的链表（遍历主要是为了使 CPU 缓存链表），若找不到，则创建 HashEntry。
            // tryLock 一定次数后（MAX_SCAN_RETRIES 变量决定），则 lock。若遍历过程中，由于其他线程的操作导致链表头结点变化，则需要重新遍历。
            HashEntry<K,V> node = tryLock() ? null :
                scanAndLockForPut(key, hash, value);
            V oldValue;
            try {
                HashEntry<K,V>[] tab = table;
                int index = (tab.length - 1) & hash;
                // 定位 HashEntry，可以看到，这个 hash 值在定位 Segment 时和在 Segment 中定位 HashEntry 都会用到，只不过定位 Segment 时只用了高几位。
                HashEntry<K,V> first = entryAt(tab, index);
                for (HashEntry<K,V> e = first;;) {
                    
                    if (e != null) {
                        K k;
                        // 已经存在这个 key 值了则替换
                        if ((k = e.key) == key ||
                            (e.hash == hash && key.equals(k))) {
                            oldValue = e.value;
                            if (!onlyIfAbsent) {
                                e.value = value;
                                ++modCount;
                            }
                            break;
                        }
                        e = e.next;
                    }
                    else {
                        if (node != null)
                            // 插入
                            node.setNext(first);
                        else
                            // 新建
                            node = new HashEntry<K,V>(hash, key, value, first);
                        int c = count + 1;
                        // 若 c 超出阈值 threshold，需要扩容并 rehash。扩容后的容量是当前容量的 2 倍。这样可以最大程序避免之前散列好的 entry 重新散列。
                        if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                            // 重新散列
                            rehash(node);
                        else
                            // 插入
                            setEntryAt(tab, index, node);
                        ++modCount;
                        count = c;
                        oldValue = null;
                        break;
                    }
                }
            } finally {
                unlock();
            }
            return oldValue;
        }
```

　　Segment 的 put 是加锁中完成的。

1. 先调用 tryLock() 方法加锁。
2. 通过 `int index = (tab.length - 1) & hash;` 定位 HashEntry 在 HashEntry<K,V>[] tab 中位置，然后调用 `entryAt(tab, index)` 方法来找到链表。
3. 然后遍历 HashEntry ，通过 ` if ((k = e.key) == key ||(e.hash == hash && key.equals(k)))` 判断是否存在相同的 key。如果存在，则更新 key 所对应的 value 值；如果没有，如果链表不为空，则加到链表的尾部，否则，新建链表存储数据。如果插入数据后，元素数大于最大临界值，则会调用 rehash() 方法重新散列，如果没有大于临界值，则调用 setEntryAt() 方法将数据插入到 HashEntry[] tab 的指定位置。
4. 最后调用 unlock() 方法解锁。

### 3.4. Segment#rehash() - 扩容

```java
        private void rehash(HashEntry<K,V> node) {
            HashEntry<K,V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            // 扩大 2 倍
            int newCapacity = oldCapacity << 1;
            threshold = (int)(newCapacity * loadFactor);
            HashEntry<K,V>[] newTable =
                (HashEntry<K,V>[]) new HashEntry[newCapacity];
            int sizeMask = newCapacity - 1;
            for (int i = 0; i < oldCapacity ; i++) {
                HashEntry<K,V> e = oldTable[i];
                if (e != null) {
                    HashEntry<K,V> next = e.next;
                    // 计算新的位置
                    int idx = e.hash & sizeMask;
                    if (next == null)   //  Single node on list
                        // 链表上只有一个结点
                        newTable[idx] = e;
                    else { // Reuse consecutive sequence at same slot
                        // 插入到合适位置
                        HashEntry<K,V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K,V> last = next;
                             last != null;
                             last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        // Clone remaining nodes
                        for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                            V v = p.value;
                            int h = p.hash;
                            int k = h & sizeMask;
                            HashEntry<K,V> n = newTable[k];
                            newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                        }
                    }
                }
            }
            // 插入新结点
            int nodeIndex = node.hash & sizeMask; // add the new node
            node.setNext(newTable[nodeIndex]);
            newTable[nodeIndex] = node;
            table = newTable;
        }
```

### 3.3. put 方法总结

　　ConcurrentHashMap 的 hash() 方法只是算出了 key 的新 hash 值，但是如何用这个 hash 值定位：如果要取得一个值，首先肯定需要先知道哪个 segment，然后再知道 hashentry 的 index，最后一次循环遍历该 index 下的元素。

* sement 定位：(h >>> segmentShift ) & segmentMask。默认使用 h 的前 4 位，segmentMask 为 15。

* 确定HashEntry 的 index：（tab.length -1）& h，HashEntry 的长度减 1，用之前确定了segment 的新 h 计算。

* 循环：

  ```java
  for(HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile(tab,((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE ); e != null; e = e.next)
  ```

* 比较

  ```java
  if((k = e.key) == key || (e.hash == h && key.equals(k))) return e.value;
  ```

### 

1. put 时候，通过 Hash 函数将即将要 put 的元素均匀的放到所需要的 Segment 段中，调用 Segment 的 put 方法进行数据。
2. Segment 的 put 是加锁中完成的。如果当前元素数大于最大临界值的话将会产生 rehash。先通过 getFirst 找到链表的表头部分，然后遍历链表，调用 equals 比配是否存在相同的 key，如果找到的话，则将最新的 key 对应 value 值。如果没有找到，新增一个 HashEntry 它加到整个 Segment 的头部。

## 5. get 方法 - 获取数据

```java
public V get(Object key) {
        Segment<K,V> s; // manually integrate access methods to reduce overhead
        HashEntry<K,V>[] tab;
        int h = hash(key);
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
    	// 先定位 Segment，再定位 HashEntry
        if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
            for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                     (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
                 e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return e.value;
            }
        }
        return null;
    }
```

　　如果要取得一个值，首先肯定需要知道哪个 segment，然后再知道 hashEntry 的 index，最后循环遍历该 index 下的元素。

* 确定 segment：（h >>> segmentShift）& segmentMask。默认使用 h 的前 4 位，segmentMask 为 15。

* 确定 index：（tab.length - 1）& h，hashEntry 的长度减 1，用之前确定了 segment 的新 h 计算。

* 循环：

  ```java
  for (HashEntry<K,V> e = (HashEntry<K,V> UNSAFE.getObjectVolatile(tab,((long)(((tab.length-1) & h)) << TSHIFT) + TABLE); e != null; e = e.next)
  ```

* 比较

  ```java
  if ((k = e.key) == key || (e.hash == h && key.equals(k)))
  	return e.value;
  ```

　　get 方法无需加锁，由于其中设计到的共享变量都使用 volatile 修饰，volatile 可以保证内存可见性，所以不会读取到过期数据。



## 6. ConcurrentHashMap#remove - 删除元素

```java
    public V remove(Object key) {
        int hash = hash(key);
        Segment<K,V> s = segmentForHash(hash);
        return s == null ? null : s.remove(key, hash, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if the specified key is null
     */
    public boolean remove(Object key, Object value) {
        int hash = hash(key);
        Segment<K,V> s;
        return value != null && (s = segmentForHash(hash)) != null &&
            s.remove(key, hash, value) != null;
    }
```

### 6.1. Segment#remove

```java
final V remove(Object key, int hash, Object value) {
    		// 加锁
            if (!tryLock())
                scanAndLock(key, hash);
            V oldValue = null;
            try {
                HashEntry<K,V>[] tab = table;
                int index = (tab.length - 1) & hash;
                HashEntry<K,V> e = entryAt(tab, index);
                HashEntry<K,V> pred = null;
                while (e != null) {
                    K k;
                    HashEntry<K,V> next = e.next;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        V v = e.value;
                        if (value == null || value == v || value.equals(v)) {
                            if (pred == null)
                                setEntryAt(tab, index, next);
                            else
                                pred.setNext(next);
                            ++modCount;
                            --count;
                            oldValue = v;
                        }
                        break;
                    }
                    pred = e;
                    e = next;
                }
            } finally {
                // 解锁
                unlock();
            }
            return oldValue;
        }   
        
```



## 7. ConcurrentHashMap#size()  - 容量判断

```java
public int size() {
        final Segment<K,V>[] segments = this.segments;
        int size;
        boolean overflow; // true if size overflows 32 bits
        long sum;         // sum of modCounts
        long last = 0L;   // previous sum
        int retries = -1; // first iteration isn't retry
        try {
            for (;;) {
                if (retries++ == RETRIES_BEFORE_LOCK) {
                    for (int j = 0; j < segments.length; ++j)
                        ensureSegment(j).lock(); // force creation
                }
                sum = 0L;
                size = 0;
                overflow = false;
                for (int j = 0; j < segments.length; ++j) {
                    Segment<K,V> seg = segmentAt(segments, j);
                    if (seg != null) {
                        sum += seg.modCount;
                        int c = seg.count;
                        if (c < 0 || (size += c) < 0)
                            overflow = true;
                    }
                }
                if (sum == last)
                    break;
                last = sum;
            }
        } finally {
            if (retries > RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j)
                    segmentAt(segments, j).unlock();
            }
        }
        return overflow ? Integer.MAX_VALUE : size;
    }
```

　　在统计 ConcurrentHashMap 的数量时，有多线程情况，但是并不是一开始就锁住修改结构的方法，比如 put、remove 等。先执行一次统计，然后再执行一次统计，如果两次统计结果都一样，则没问题。反之就锁修改结构的方法。这样做效率会高很多，在统计的时候查询依旧可以进行。

## 8. ConcurrentHashMap#isEmpty() - chm 是否为空判断

```java
public boolean isEmpty() {
       
        long sum = 0L;
        final Segment<K,V>[] segments = this.segments;
        for (int j = 0; j < segments.length; ++j) {
            Segment<K,V> seg = segmentAt(segments, j);
            if (seg != null) {
                if (seg.count != 0)
                    return false;
                sum += seg.modCount;
            }
        }
        if (sum != 0L) { // recheck unless no modifications
            for (int j = 0; j < segments.length; ++j) {
                Segment<K,V> seg = segmentAt(segments, j);
                if (seg != null) {
                    if (seg.count != 0)
                        return false;
                    sum -= seg.modCount;
                }
            }
            if (sum != 0L)
                return false;
        }
        return true;
    }
```

　　即是在空的情况下也不能仅仅只靠 segment 的计数器来判断，还是因为多线程，count 的值随时在变，所以追加判断 modcount 前后是否一致，如果一直，说明期间没有修改。



## 9 .总结



## 10.参考文章 

1. [CurrentHashMap源码剖析](https://segmentfault.com/a/1190000015083593)

2. [HashMap实现原理及源码分析](https://www.cnblogs.com/jing99/p/11330341.html)

3. [jdk1.7 - ConcurrentHashMap](https://github.com/linglanty/jdk1.7/blob/master/src/main/java/java/util/concurrent/ConcurrentHashMap.java )