# HashMap
　　HashMap 是一个哈希表，它存储的内容是键值对（key-value）映射。

　　HashMap 由数组+链表组成，数组是 HashMap 的主体，链表则是主要为了解决哈希冲突的。

## 1. 哈希表
　　哈希表（Hash Table，也叫散列表），是根据键（key）直接访问在内存存储位置的数据结构。也就是说，它通过计算一个关于键值的函数，将所需查询的数据映射到表中一个位置来访问记录，这样加快了查找速度。这个映射函数称为**哈希函数**，存放记录的数组称为**哈希表**。

　　简单解释就是：存储位置 = f(关键字)。f 表示的就是哈希函数。

### 1.1. 哈希冲突

　　对不同的关键字可能得到同一哈希地址，这种现象称为**哈希冲突**。

　　若对于关键字集合中的任一个关键字，经哈希函数映射到地址集合中任何一个地址的概率是相等的，则称此类哈希函数为**均匀哈希函数（Uniform Hash function）**，这就使关键字经过哈希函数得到一个 “ 随机的地址 ”，从而减少冲突。

#### 1.1.1. 处理冲突

　　处理冲突一方面可以重新选用其他的哈希函数，或者就是对冲突的结果进行处理。一般来说，很难去确保一个哈希函数绝对没有冲突，所以通常是对冲突结果进行处理。

　　而对冲突的结构进行处理的方式有：开放定址法、单独链表法、再散列。

##### 1.1.1.1. 开放定址法

　　查询地址表，直到查找到一个空单元，把哈希地址存放在该空单元。散列到散列表中的关键字可能需要查找多次试选单元才能插入表中，从而解决冲突，造成时间浪费，后续的查找也是多次定位才能找到正确的位置。

##### 1.1.2. 单独链表法

　　将哈希到同一个存储位置的所有元素保存在一个链表中。**HashMap** 就是采用这种方法。

##### 1.1.1.3. 再散列

　　将冲突的哈希函数地址产生新的哈希函数地址，直到冲突不再发生，但增加了计算时间。

### 1.2. 负载因子

　　哈希表的负载因子（也称为载荷因子）定义为：a = 填入表中的元素合数 / 哈希表的长度。负载因子越大，意味着哈希表越满，越容易导致冲突，性能也就越低。一般情况下，当负载因子大于某个常量值（HashMap 是 0.75）时，会对哈希表进行扩容。

## 2. 关键参数

　　HashMap 继承 AbstractMap，并且实现 Map、Cloneable、Serializable 接口。

　　以 Java 1.7 源码为例，分析 HashMap 的实现。

　　HashMap 的主干是一个 Entry 数组。Entry 是 HashMap 的基本组成单元，每一个 Entry 包含一个 key-value 键值对。 

```java
    /**
     HashMap 的主干是一个 HashMapEnter 数组，初始值是空数组，数组的长度必须是 2 的倍数。
     */
   transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;

    /**
     映射表包含的 key-value 映射的数量。
     */
    transient int size;

    /**
    阈值，当 table == {} 时，该值为初始容量（初始容量默认为 16）；当 table 被填充了，也就是为 table 分配内存空间后，threshold 一般为 capacity * loadFactory。HashMap 在进行扩容时需要参考 threshold。
    */
    int threshold;

    /** 
    负载因子，代表了 table 的填充度是多少，默认是 0.75。
    */
    final float loadFactor;
```

### 2.1. Entry 结构

```java
   static class Entry<K,V> implements Map.Entry<K,V> {
        final K key;// 关键字
        V value;// 映射数据
        Entry<K,V> next;// 指向下一个数据
        int hash;// hash 值，并不是 hashCode() 的值，而是将 hashCode() 的值再进一步处理

        ...
        
        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return java.util.Objects.hashCode(getKey()) ^ java.util.Objects.hashCode(getValue());
        }
		...
    }
    

```

![HashMap的总体结构](./image/HashMap的总体结构.png)

## 3. 构造函数

　　HashMap 有 4 个构造函数，无参构造函数和一个 initialCapacity 参数的构造函数都会调用 HashMap(int initialCapacity, float loadFactor) 这个构造函数。还有一个构造函数的参数是 Map。

### 3.1. HashMap 构造方法

```java
    public HashMap(int initialCapacity, float loadFactor) { 
    		// 当指定的初始容量 < 0 时抛出 IllegalArgumentException 异常
       	if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        // 当指定的初始容量 > MAX_CAPACITY 时，就让初始容量 = MAX_CAPACITY
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        // 当负载因子小于等于 0 或者不是数字时，抛出 IllegalArgumentException 异常
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

        this.loadFactor = loadFactor;
        // 设置 threshold 初始为 initialCapacity。
        threshold = initialCapacity;
        init();//空方法
        
    }

		// 设置负载因子为默认值 0.75
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    // 初始默认是 16，负载因子默认是 0.75
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

	  // 使用 Map 为参数初始化
    public HashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
				// 初始化 table
        inflateTable(threshold); //3.2 解析方法
				// 将参数 m 的值添加到 table 中
        putAllForCreate(m); //3.3 解析该方法
    }
```

### 3.2. inflateTable(int toSize) 方法

　　inflateTable() 方法用于为主干数组 table 在内存中分配存储空间，通过 roundUpToPowerOf2(toSize) 可以确保 capacity 为大于或等于 toSize 的最接近 toSize 的二次幂。

```java
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
				// roundUpToPowerOf2 返回一个比给定整数大且最接近 2 的幂次方整数，所以 capacity 必然是 2 的幂
        int capacity = roundUpToPowerOf2(toSize); //3.2.1 解析方法
        
        // 可以看出 threshold 通常是等于 capacity * loadFactor，并且 threshold 不会超过 MAXIMUM_CAPACITY + 1
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        // 创建了 Entry 数组
        table = new Entry[capacity];
        initHashSeedAsNeeded(capacity);
    }
```

　　inflateTable() 方法中会将设置 capacity 为指定的表大小的最小 2 的幂次方整数，所以 capacity 必然是 2 的整数。threadshold 在 capacity 不超过 MAXIMUM_CAPACITY 的时候等于 capacity * loadFactor，table 的初始容量为 capacity。

#### 3.2.1. roundUpToPowerOf2(int number) 方法

```java
    private static int roundUpToPowerOf2(int number) {
        int rounded = number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (rounded = Integer.highestOneBit(number)) != 0
                    ? (Integer.bitCount(number) > 1) ? rounded << 1 : rounded
                    : 1;

        return rounded;
    }
```

　　Integer.highestOneBit(number)) 返回的是数值的最高位数值，例如 0010 1101 返回的则是 0010 0000，而 Integer.bitCount(number) 返回的是二进制中为 1 的数量，如果最高位数值不为 0 ，并且二进制中 1 的数量也是大于 1 的，那么会返回 rounded << 1 的高一位数值。

　　所以 capacity 一定是 2 的倍数。

### 3.3. putAllForCreate(Map<? extends K, ? extends V> m) 方法

```java
    private void putAllForCreate(Map<? extends K, ? extends V> m) {
    		// 遍历参数 m, 将 m 中的数组依次添加到映射表中
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue()); //3.3.1 解析方法
    }

```

　　putAllForCreate() 方法循环遍历参数 m 中的元素数据，然后调用 putForCreate() 方法将这些元素数据添加到 table 映射表中。

#### 3.3.1. putForCreate(K key,V value) 方法

```java
    // 向映射表中添加关键字为 key，映射值为 value 
    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
         * Look for preexisting entry for key.  This will never happen for
         * clone or deserialize.  It will only happen for construction if the
         * input Map is a sorted map whose ordering is inconsistent w/ equals.
         */
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            // 如果已经存在，则覆盖值
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
				// 创建新的 Entry 对象，将其插入合适的位置
        createEntry(hash, key, value, i); // 3.3.1.1 解析方法
    }
```

　　putForCreate() 方法，先调用 hash(key) 方法得到 hash 值，然后得到 key 在哈希表中的位置 i。先遍历 table 哈希表中是否已经存在 key，如果存在，则更新 key 对应的值，如果没有，则调用 createEntry() 方法创建 Entry 对象，并将其插入到哈希表 i 位置。

##### 3.3.1.1. createEntry(int hash, K key, V value, int bucketIndex) 方法

```java
    void createEntry(int hash, K key, V value, int bucketIndex) {
    		// 获取当前 bucketIndex 位置的 Entry
        Entry<K,V> e = table[bucketIndex];
        // 创建新的 Entry 对象，将其插入到 backetIndex 位置
        // 将数据插入到了链表的头部，头插法
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        //存储数量加 1
        size++;
    }
```

　　createEntry() 方法显示将 bucketIndex 存储的数据保存在 e 变量中，然后创建一个新的 Entry 对象带存储传递进来的 hash、key、value 数值，并将新的 Entry 的 next 设置为 e，最后 size 加 1。

### 3.4. 构造方法总结

1. 除了参数为 map 的构造方法，HashMap 在构造方法中是不会初始化映射数组的。
2. HashMap 中用一个数组（Entry[] table）来保存映射数组，用链表（Entry）来存储映射的数值。

## 4. put() 方法

```java
    public V put(K key, V value) {
    		// 如果 table 数组为空数组{}，进行数组填充（为 table 分配实际内存空间），参数为 threshold
        if (table == EMPTY_TABLE) {
						// 初始化映射数组
            inflateTable(threshold); // 3.2 解析方法
        }
				// 允许 key 为 null，如果 key 为 null，存储位置为 table[0] 或 table[0] 的冲突链
        if (key == null)
            return putForNullKey(value);
				// 对 key 的 hashcode 进行进一步计算得到 hash 数值，确保散列均匀
        int hash = hash(key); // 4.1 解析方法
				// 获取存储数据的数组下标
        int i = indexFor(hash, table.length); // 4.2 解析方法
				// 如果已经有 key 对应的数据，则覆盖，并返回旧的 value
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
				// 保证并发访问时，若 HashMap 内部结构发生变化，快速响应失败
        modCount++;
				// 创建新的 HashMapEntry 结构，插入到合适的位置
        addEntry(hash, key, value, i); //4.3 解析方法
        return null;
    }
```

　　在 put 方法中，如果数组还是空的，则会调用 inflateTable() 方法初始化数组，所以在除去 map 参数的构造方法外，其他的构造方法会在 put() 方法中初始化数据。

### 4.1. hash(Object k) 方法

```java
   // 对 key 的 hashcode 进一步进行计算以及二进制位的调整等来保证最终获取的存储位置尽量分布均匀
   final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k); // 4.1.1 解析方法
        }
        h ^= k.hashCode();
 
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
```

　　HashMap 的 hash() 方法对 key 的 hashCode() 继续进行了一些运算，这样是为了保证最终的哈希位置尽可能分布均匀。

#### 4.1.1. stringHash32() 方法

　　sun.misc.Hashing.stringHash32((String) k) 实际调用的是 String.hash32 方法：

```java
int hash32() {
    int h = hash32;
    // h==0 表示未计算过 hash code
    // 可以看到 hash code 只会计算一次
    if (0 == h) {
       // HASHING_SEED 是根据时间戳等计算出来的随机数
       h = sun.misc.Hashing.murmur3_32(HASHING_SEED, value, 0, value.length);

       // 确保结果非 0，避免重新计算
       h = (0 != h) ? h : 1;

       hash32 = h;
    }

    return h;
}
```

### 4.2. indexFor(int h, int length) 方法

```java
    static int indexFor(int h, int length) {
				// 下标是 hash & (数组长度-1)
        return h & (length-1);
    }
```

　　h & (length-1) 保证获取的 index 一定在数组范围内。位运算对计算机来说，性能更高一些（HashMap 中有大量位运算）。

　　所以最终存储位置确认流程为：

![最终位置的确定流程](./image/最终位置的确定流程.png)

### 4.3. addEnter() 方法

```java
    void addEntry(int hash, K key, V value, int bucketIndex) {
    		// 如果 size 超过了阈值，则将数组的大小扩充为当前的两倍，也就是扩容
        if ((size >= threshold) && (null != table[bucketIndex])) {
            // 数组被扩大为原来的二倍
            resize(2 * table.length); // 4.3.1 解析方法
            hash = (null != key) ? hash(key) : 0;
            bucketIndex = indexFor(hash, table.length); // 4.2 解析方法
        }

        createEntry(hash, key, value, bucketIndex); // 3.3.1.1 解析方法
    }
```

　　当 HashMap 的 size 大于或等于 threshold ，就要进行 resize，也就是扩容。

#### 4.3.1. resize(int newCapacity) 方法

```java
// 重新设置数组大小
void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable, initHashSeedAsNeeded(newCapacity)); // 4.3.2. 解析方法
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }
```

　　当数组长度发生变化时，存储位置 index = h & (length-1)，index 也可能会发生变化，需要重新计算 index。

#### 4.3.2. transfer() 方法

```java
    // 数组扩容后，重新调整位置
    void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        // for 循环中的代码，逐个遍历链表，重新计算索引位置，将老数组数据复制到新数组中去（数组不存储实际数据，所以仅仅是拷贝引用而已）
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                // 将当前 entry 的 next 链指向新的索引位置，newTable[i] 有可能为空，有可能也是个 entry 链，如果是 entry 链，直接在链表头部插入
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }
```

　　这个方法将老数组中的数据逐个链表的遍历，扔到新的扩容后的数组中，数组索引的计算是通过对 key 的 hashCode 进行 hash 运算后，再通过和 length-1 进行位运算得到最终数组索引位置。

　　当发生哈希冲突并且 size 大于阈值的时候，需要进行数组扩容，扩容时，需要新建一个长度为之前数组 **2** 倍的新数组，然后将当前的 Entry 数组中的元素全部传输过去，扩容后的新数组长度为之前的 2 倍，所以**扩容相对来说是个耗资源的操作**。

### 4.4. HashMap 的数组长度为什么一定要保持为 2 的次幂？

1. **扩容后减少数组数组的移动**

　　将 HashMap 的数组长度保持为 2 的次幂，在扩容后，与之前的 length-1 相比，只有最高位的一位差异，这样在通过 h & (length-1) 的时候，只要 h 对应的最高位的一位的差异位为 0 ，就能保证得到的新的数组索引和老数组索引一致（减少了之前已经散列好的老数组的数据位置重新调换。）

![数组长度为何为2](./image/数组长度为何为2.png)

2. **使索引更加均匀**

　　数组长度保持 2 的次幂，length-1 的低位都为 1，会使得获得的数组索引 index 更加均匀。高位不会对结果产生影响，但是对于 h  的低位部分，任何一位的变化都会对结果产生影响。

![数组索引更加均匀](./image/数组索引更加均匀.png)

## 5. get() 方法

```java
    public V get(Object key) {
        // 如果 key 为 null，则直接去 tab[0] 处去检索即可。
        if (key == null)
            return getForNullKey();
        Entry<K,V> entry = getEntry(key); // 5.1 解析方法

        return null == entry ? null : entry.getValue();
    }
```

　　get 方法通过 key 值返回对应 value，如果 key 为 null，则直接去 table[0] 处检索，如果不为 null，则调用 getEntry(key) 获取。

### 5.1. getEntry(Object key) 方法

```java
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }
        // 通过 key 的 hashCode 值计算 hash 值
        int hash = (key == null) ? 0 : sun.misc.Hashing.singleWordWangJenkinsHash(key); // 5.2 解析方法
				// 循环遍历下标下面的链表
        // indexFor (hash&length-1) 获取最终数组索引，然后遍历链表，通过 equals 方法比对找出对应记录
        for (HashMapEntry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
						//注意这里的判断，hash 要相同，key 值也要相同才会返回 value。
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }
```

　　getEntry 方法，先通过 key 计算得到 hash 值，然后调用 indexFor 得到 key 对应哈希表的下标，然后遍历 哈希表对应的 key 的 HashMapEntry 链表，如果 e 的 hash 相同并且 key 值也相同，则返回 e，否则就是没找到，返回 null。

### 5.2. singleWordWangJenkinsHash(Object k) 方法 

```java
    // 对 key 的 hashcode 进一步进行计算以及二进制位的调整等来保证最终获取的存储位置尽量分布均匀，返回值就是 hash 值
    public static int singleWordWangJenkinsHash(Object k) {
        int h = k.hashCode();

        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }
```

　　HashMapEntry 结构的 hash 值是将 key 的 hashCode() 值进行一系列的位移操作作为 hash 值的。

　　在根据 key 值获取对应的值时，判断时需要 hash 值相同，并且 key 值相同才判断为是同一个 key 值，为什么除了判断 key 值相同还要保证 hash 值相同，这是因为如果传入的 key 对象重写了 equals 方法却没有重写 hashCode ，而恰巧此对象定位到这个数组位置，如果仅仅用 equals 判断可能是相等的，但其 hashCode 和当前对象时不一致的，这种情况，根据 Object 的 hashCode 的约定，不能返回当前对象，而应该返回 null。

## 6.  JDK 1.7 和 JDK 1.8 HashMap 的区别
　　这里简单罗列一下 JDK 1.8 的 HashMap 与 JDK 1.7 的不同之处。

### 6.1. JDK 1.8 是数组 + 链表 + 树的结构，JDK 是 数组 + 链表的结构 

　　JDK 1.8 在 JDK 1.7 在基础上增加了红黑树来进行优化，即当链表超过 **TREEIFY_THRESHOLD（默认为 8）** 时，将链表转换为红黑树，利用红黑树快速增删改查的特点提高 HashMap 的性能，其中会用到红黑树的插入、删除、查找等算法。当小于 **UNTREEIFY_THRESHOLD(默认为6)** 时，又会转回链表以达到性能均衡。

```java
    // Java 1.8 put 方法的具体实现
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // 如果 table 为空或者长度为 0，则 resize()
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 确定插入 table 的位置，算法是 （n-1）&hash，在 n 为 2 的幂时，相当于取模操作。
        // 找到 key 值对应的槽并且是第一个，直接加入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        // 在 table 的 i 位置发生碰撞，有两种情况：
      	// 1.key 值是一样的，替换 value 值
        // 2.key 值不一样的有两种处理方式：
      	//   2.1. 存储在 i 位置的链表；
      	//   2.2. 存储在红黑树中
        else {
            Node<K,V> e; K k;
            // 第一个 node 的 hash 值即为要加入元素的 hash
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            // 2.2 如果节点是树节点
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
            	// 2.1 不是 TreeNode，即为链表，遍历链表
                for (int binCount = 0; ; ++binCount) {
              			// 链表的尾端也没有找到 key 值相同的节点，则生成一个新的 node
              			// 并且判断链表的结点个数是不是到达转换成红黑树的上限，达到则转换成红黑树
                    if ((e = p.next) == null) {
                    	// 将新数据插入到链表的最末端，尾插法
                        p.next = newNode(hash, key, value, null);
                        // TODO 新添加一个数据之后，如果数据的数量已经超过了或者等于 TREEIFY_THRESHOLD - 1 就会将链表转换成红黑树
                        //static final int TREEIFY_THRESHOLD = 8;
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // 如果 e 不为空就替换旧的 oldValue 值
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
      	// 扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

　　hash 冲突发生的几种情况：

1. 两节点 key 值相同（hash 值一定相同），导致冲突；
2. 两节点 key 值不同，由于 hash 函数的局限性导致 hash 值相同，冲突；
3. 两节y点 key 值不同，hash 值不同，但是 hash 值对数组长度取模后相同（hash&(length-1)），冲突。

### 6.2. hash() 方法不同

```java
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

　　JDK 1.8 的 hash() 方法可以将 hashCode 的高位和低位的值进行混合做异或运算，而且混合后，低位的信息中加入了高位的信息，这样高位的信息被保留了下来。掺杂的元素多了，那么生成的 hash 值的随机性会增大。

　　JDK 1.7 的 hash() 方法是将 key 的 hashCode 值进行了一系列的位移运算获取的 hash 值。

### 6.3. 插入的方式不同

　　JDK 1.7 采用的是头插法，而 JDK 1.8 及之后使用的都是尾插法。这是因为 JDK 1.7 是用单链表进行的纵向延伸，采用头插法能够提高插入的效率，但是也会容易出现逆序且环形链表死循环问题。在 JDK 1.8 之后是因为加入了红黑树使用尾插法，能够避免出现逆序且链表死循环的问题。

### 6.4. 扩容后数据存储位置的计算方式不同

　　在 JDK 1.7 的时候是直接用 hash 值和需要扩容的二进制数进行 & 运算（hash 值 & length-1）。

　　而在 JDK 1.8 的时候直接使用了 JDK 1.7 计算的规律，就是扩容前的原始位置 + 扩容的大小值 = JDK 1.8 的计算方式，而不再是 JDK 1.7 的那种异或的方法。凡是这种方式就相当于只需要判断 Hash 值的新增参加运算的位是 0 还是 1 就直接迅速计算出扩容后的存储方式。

　　JDK 1.8 的 resize() 方法：

```java
    final Node<K,V>[] resize() {
        // 保存当前 table
        Node<K,V>[] oldTab = table;
        // 保存当前 table 的容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 保存当前阈值
        int oldThr = threshold;
        // 初始化新的 table 容量和阈值
        int newCap, newThr = 0;
        // 1. resize() 函数在 size > threshold 时被调用。oldCap 大于 0 表示原来的 table 表非空，oldCap 为原表的大小，oldThr(threashold) 为 oldCap * load_factor
        if (oldCap > 0) {
            // 若旧 table 容量已超过最大容量，更新阈值为 Integer.MAX_VALUE(最大整形值)，这样以后就不会自动扩容了。
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 容量翻倍，使用左移，效率更高
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 阈值翻倍
                newThr = oldThr << 1; // 阈值*2
        }
        // 2.resize() 函数在 table 为空被调用。oldCap 小于等于 0 且 oldThr 大于 0 ，代表用户创建了一个 HashMap，但是使用的构造函数为 HashMap(int initialCapacity,float loadFactor)或 Hash(int initialCapacoty) 或 HashMap（Map<? extends K,? extends v> m） 导致 oldTab 为 null，oldCap 为 0，oldThr 为用户指定的 HashMap 的初始容量。
        else if (oldThr > 0) // 使用旧值初始化容量
        	// 当 table 没初始化时，threashold 持有初始容量。
            newCap = oldThr;
        // 3. resize() 函数在 table 为空被调用。oldCap 小于等于 0 且 oldThr 等于 0 ，用户调用 HashMap() 构造函数创建的 HashMap，所有值均采用默认值。
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 新阈值为 0
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;

        @SuppressWarnings({"rawtypes","unchecked"})
        // 初始化 table
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            // 把 oldTab 中的节点 reHash 到 newTab 中去
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    // 若节点是单个节点，直接在 newTab 中进行重定位
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    // 若节点是 ThreeNode 节点，要进行红黑树的 reHash 操作
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order 如果节点是链表节点
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // 将同一桶中的元素根据（e.hash & oldCap）是否为 0 进行分割，分成不同的链表，重新 rehash
                        do {
                            next = e.next;
                            // 根据算法，e.hash & oldCap 判断节点位置 rehash 后是否发生改变
                            // 最高位 == 0，这是索引不变的链表
                            if ((e.hash & oldCap ) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            // 最高位 == 1（这是索引发生改变的链表）
                            else {
                                // TODO 重新创建高位链表
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);

                        if (loTail != null) { // 原 bucket 位置为尾指针不为空（即还有 node）
                            loTail.next = null; // 链表最后有个 null
                            newTab[j] = loHead; // 链表头指针放在新桶的相同下表 j 处
                        }
      
                        if (hiTail != null) {
                            hiTail.next = null;
                            // rehash 后节点新的位置一定为原来基础上加上 oldCap。
                            newTab[j + oldCap] = hiHead; 
                        }
                    }
                }
            }
        }
        return newTab;
    }
```

　　长度扩展为原来的 2 倍，使用的是 2 次幂的扩展，所以，元素的位置要么是在原位置，要么是在原位置再移动 2 次幂的位置。

![JDK1.8扩展下标解释](./image/JDK1.8扩展下标解释.webp)

　　(a) 是扩容前的 key1 和 key2 两种 key 确定索引位置，（b）是扩容后 key1 和 key2 两种 key 确定索引位置。

　　元素在重新计算 hash 之后，因为 n 变为 2 倍，那么 n-1 的二进制的高位就多了 1 bit，因此新的 index 就会发生下面变化：

![](./image/JDK1.8扩容下标.webp)

　　因此，扩容的时候，只需要看原来的 hash 值新增的那个 bit 是 1 还是 0 就好了，是 0 的话索引没变，是 1 的话索引变成->原索引+ oldCap。

![](./image/JDK1.8扩容例图.webp)

### 6.5. 将 capacity 设置为 2 的次幂的方式不同

　　JDK 1.8 的是调用 tableSizeFor(int cap) 方法来返回一个比给定整数大且最接近的 2 的幂次方整数的：

```java
    static final int tableSizeFor(int cap) {
    		// 为了防止 cap 已经是 2 的幂次
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```

![tableSizeFor示例图](./image/tableSizeFor示例图.webp)

## 7. 总结

1. HashMap 不是线程安全的。
2. HashMap 在 JDK 1.7 的时候结构是数组 + 链表，在 JDK 1.8 的时候结构是 数组 + 链表 + 树。
3. HashMap 的数组长度达到阈值时，会将当前数组的容量扩容到之前数组容量的 2 倍，将之前的数据重新存储到新的结构中。
4. 在 HashMap 引起哈希冲突的原因有三个：key 值，计算后的 hash 值，hash & (length-1) 的值。
5. 如果将一个自定义的类作为 key 值，重写 equals() 方法的同时也要重写 hashCode() 方法。
6. 为何 HashMap 的数组容量要是 2 的次幂？a.扩容后减少数组数据的移动；b.使索引更加均匀，hash 数值低位的每一位都对索引产生影响。

## 8. 参考文章

1. [一文读懂 HashMap](https://www.jianshu.com/p/ee0de4c99f87)
2. [Java 集合之一 -- HashMap - 深入浅出学 Java -- HashMap](https://blog.csdn.net/woshimaxiao1/article/details/83661464)
3. [散列表](https://zh.wikipedia.org/wiki/%E5%93%88%E5%B8%8C%E8%A1%A8)
4. [jdk1.8 -- HashMap](https://github.com/tinyking/jdk1.8/blob/master/src/java/util/HashMap.java)
5. [jdk1.7 -- HashMap](  https://github.com/linglanty/jdk1.7/blob/master/src/main/java/java/util/HashMap.java  )
