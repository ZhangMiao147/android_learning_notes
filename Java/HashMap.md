# HashMap
　　HashMap 是一个哈希表，它存储的内容是键值对（key-value）映射。

　　HashMao 由数组+链表组成，数组是 HashMap 的主体，链表则是主要为了解决哈希冲突的。

## 哈希表
　　哈希表（Hash Table，也叫散列表），是根据键（key）而直接访问在内存存储位置的数据结构。也就是说，它通过计算一个关于键值的函数，将所需查询的数据映射到表中一个位置来访问记录，这加快了查找速度。这个映射函数称为哈希函数，存放记录的数组称为哈希表。

　　简单解释就是：存储位置 = f(关键字)。f 表示的就是哈希函数。

#### 哈希冲突
　　对不同的关键字可能得到同一哈希地址，这种现象称为哈希冲突。

　　若对于关键字集合中的任一个关键字，经哈希函数映射到地址集合中任何一个地址的概率是相等的，则称此类哈希函数为均匀哈希函数（Uniform Hash function），这就使关键字经过散列函数得到一个“随机的地址”，从而减少冲突。

##### 处理冲突
　　处理冲突一方面可以重新选用其他的散列函数，或者就是对冲突的结果进行处理。一般来说，很难去确保一个散列函数绝对没有冲突，所以通常是对冲突结果进行处理。

1.开放定址法

　　查询地址表，直到查找到一个空单元，把哈希地址存放在该空单元。散列到散列表中的关键字可能需要查找多次试选单元才能插入表中，解决冲突，造成时间浪费，后续的查找也是多次定位才能找到正确的位置。

2.单独链表法

　　将哈希到同一个存储位置的所有元素保存在一个链表中。HashMap 就是采用这种方法。

3.再散列

　　将冲突的哈希函数地址产生新的哈希函数地址，直到冲突不在发生，但增加了计算时间。

#### 载荷因子
　　哈希表的载荷因子定义为：a = 填入表中的元素合数 / 哈希表的长度。

## 源码分析
　　以 Java 1.7 源码为例，分析 HashMap 的实现。

　　HashMap 继承 AbstractMap，并且实现 Map、Cloneable、Serializable 接口。
#### 关键参数
```
    /**
     HashMap 的主干是一个 HashMaoEnter 数组，初始值是空数组，数组的长度必须是 2 的倍数。
     */
    transient HashMapEntry<K,V>[] table = (HashMapEntry<K,V>[]) EMPTY_TABLE;

    /**
     映射表包含的 key-value 映射的数量。
     */
    transient int size;

	// 阈值，当 table == {} 时，该值为初始容量（初始容量默认为 16）；当 table 被填充了，也就是为 table 分配内存空间后，threshold 一般为 capacity * loadFactory。HashMap 在进行扩容时需要参考 threshold。
    int threshold;

	//负载因子，代表了 table 的填充度是多少，默认是 0.75。
    final float loadFactor = DEFAULT_LOAD_FACTOR;
```

##### HashMapEntry 结构

```
   static class HashMapEntry<K,V> implements Map.Entry<K,V> {
        final K key; // 关键字
        V value; //映射数据
        HashMapEntry<K,V> next; //指向下一个数据
        int hash; //hash 值，并不是 hashCode() 的值，而是将 hashCode() 的值在进一步处理

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
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        ...
    }

```

#### 构造函数
　　HashMap 有 4 个构造函数，无参构造函数和一个 initialCapacity 参数的构造函数都会调用 HashMap(int initialCapacity, float loadFactor) 这个构造函数。还有一个构造函数的参数是 Map。

```
    
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        } else if (initialCapacity < DEFAULT_INITIAL_CAPACITY) {
            initialCapacity = DEFAULT_INITIAL_CAPACITY;
        }

        if (loadFactor <= 0 | | Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

		//设置 threshold 初始为 initialCapacity。
        threshold = initialCapacity;
        init(); //空方法
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    //阈值默认是 16，负载因子模式是 0.75
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

	//使用 Map 为参数初始化
    public HashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
		//初始化 table
        inflateTable(threshold);
		//将参数 m 的值添加到 table 中
        putAllForCreate(m);
    }
```

##### inflateTable(int toSize) 方法
```
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
		capacity 是初始的 threshold 值的最接近 2 的倍数的向上取值
        int capacity = roundUpToPowerOf2(toSize);

		// 可以看出 threshold 通常是等于 capacity * loadFactor
        float thresholdFloat = capacity * loadFactor;
        if (thresholdFloat > MAXIMUM_CAPACITY + 1) {
            thresholdFloat = MAXIMUM_CAPACITY + 1;
        }

        threshold = (int) thresholdFloat;
		//创建了数组
        table = new HashMapEntry[capacity];
    }
```


##### putAllForCreate(Map<? extends K, ? extends V> m) 方法
```
    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : sun.misc.Hashing.singleWordWangJenkinsHash(key);
        int i = indexFor(hash, table.length);

        for (HashMapEntry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
			//如果已经存在，则覆盖值
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
		//创建新的 HashMapEntry 对象，将其插入合适的位置
        createEntry(hash, key, value, i);
    }


    void createEntry(int hash, K key, V value, int bucketIndex) {
		//获取当前 bucketIndex 位置的 HashMaoEntry
        HashMapEntry<K,V> e = table[bucketIndex];
		//创建新的 HashMapEntry 对象，将其插入到 backetIndex 位置
        table[bucketIndex] = new HashMapEntry<>(hash, key, value, e);
		//存储数量加 1
        size++;
    }

```

##### singleWordWangJenkinsHash(Object k) 方法
```
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


##### roundUpToPowerOf2(int number)方法
```
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

##### 构造方法总结
1. 除了参数为 map 的构造方法，HashMap 在构造方法中是不会初始化映射数组的。
2. HashMap 中用一个数组来保存映射数组，用链表来存储映射的数值。

#### put() 方法
```
    public V put(K key, V value) {
        if (table == EMPTY_TABLE) {
			//初始化映射数组
            inflateTable(threshold);
        }
		//允许 key 为 null
        if (key == null)
            return putForNullKey(value);
		//得到 hash 数值
        int hash = sun.misc.Hashing.singleWordWangJenkinsHash(key);
		//获取存储数据的数组下标
        int i = indexFor(hash, table.length);
		//如果已经有 key 对应的数据，则覆盖
        for (HashMapEntry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
		//创建新的 HashMapEntry 结构，插入到合适的位置
        addEntry(hash, key, value, i);
        return null;
    }
```

　　在 put 方法中，如果数组还是空的，则会调用 inflateTable() 方法初始化数组，所以在除去 map 参数的构造方法外，其他的构造方法会在 put() 方法中初始化数据。

#### indexFor(int h, int length) 方法
```
    static int indexFor(int h, int length) {
		// 下标是 hash & (数组长度-1)
        return h & (length-1);
    }
```

#### get() 方法
```
    public V get(Object key) {
        if (key == null)
            return getForNullKey();
        Entry<K,V> entry = getEntry(key);

        return null == entry ? null : entry.getValue();
    }
```

##### getEntry(Object key) 方法
```
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : sun.misc.Hashing.singleWordWangJenkinsHash(key);
		//循环遍历下标下面的链表
        for (HashMapEntry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
			//注意这里的判断，hash 要相同，key值也要相同才会返回 value。
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }
```


## Java 1.7 和 Java 1.8 HashMap 的区别
　　这里简单罗列一下 Java 1.8 的 HashMap 与 Java 1.7 的不同之处。




## 总结

## 参考文章
1. [一文读懂 HashMap](https://www.jianshu.com/p/ee0de4c99f87)
2. [Java 集合之一 -- HashMap - 深入浅出学 Java -- HashMap](https://blog.csdn.net/woshimaxiao1/article/details/83661464)
3. [散列表](https://zh.wikipedia.org/wiki/%E5%93%88%E5%B8%8C%E8%A1%A8)
4. [jdk1.8 -- HashMap](https://github.com/tinyking/jdk1.8/blob/master/src/java/util/HashMap.java)
