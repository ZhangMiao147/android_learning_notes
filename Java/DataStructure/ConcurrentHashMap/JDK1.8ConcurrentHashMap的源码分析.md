# JDK 1.8 ConcurrentHashMap的源码分析

## 1. 结构

　　ConcurrentHashMap 相比 HashMap 而言，其底层数据与 HashMap 的数据结构相同，数据结构如下：

![](image/ConcurrentHashMap1.8数据结构.png)

　　说明：ConcurrentHashMap 的数据结构（数组+链表+红黑树），桶中的结构可能是链表，也可能是红黑树，红黑树是为了提高查找效率。

## ConcurrentHashMap 类

### 类的继承关系

```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable {
}
```

　　说明：ConcurrentHashMap 继承了 AbstractMap 抽象类，该抽象类定义了一些基本操作，同时，也实现了 ConcurrentMap 接口，ConcurrentMap 接口也定义了一系列操作，实现了 Serializable 接口表示 ConcurrentHashMap 可以被序列化。

### 类的内部类

　　ConcurrentHashMap 包含了很多内部类，其中主要的内部类框架图如下图所示：

![](image/ConcurrentHashMap的内部类1.png)

![](image/ConcurrentHashMap的内部类2.png)

　　说明：可以看到，ConcurrentHashMap 的内部类非常的庞大，第二个图是在 JDK 1.8 下增加的类，下面对其中主要的内部类进行分析和讲解。

* Node 类

  Node 类主要用于存储具体键值对，其子类有 ForwardingNode、ReservationNode、TreeNode 和 TreeBin 四个子类。

* Traverser 类

  Traverser 类主要用于遍历操作，其子类有 BaseIterator、KeySpliterator、ValueSpliterator、EntrySpliterator 四个类，BaseIterator 用于遍历操作。KeySpliterator、ValueSpliterator、EntrySpliterator 则用于键、值、键值对的划分。

* CollectionView 类

  CollectionView 抽象类主要定义了视图操作，其子类 KeySetView、ValueSetView、EntrySetView 分别表示键视图、值视图、键值对视图。对视图均可以进行操作。

* Segment 类

  Segment 类在 JDK 1.8 中与之前的版本的 JDK 作用存在很大的差别，JDK 1.8 下，其在普通的 ConcurrentHashMap 操作中依然没有失效，其在序列化与反序列化的时候会发挥作用。

* CounterCell

  CounterCell 类主要用于对 baseCount 的计数。

### 类的属性

```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;
    // 表的最大容量
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    // 默认表的大小
    private static final int DEFAULT_CAPACITY = 16;
    // 最大数组大小
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    // 默认并发数
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    // 装载因子
    private static final float LOAD_FACTOR = 0.75f;
    // 转化为红黑树的阈值
    static final int TREEIFY_THRESHOLD = 8;
    // 由红黑树转化为链表的阈值
    static final int UNTREEIFY_THRESHOLD = 6;
    // 转化为红黑树的表的最小容量
    static final int MIN_TREEIFY_CAPACITY = 64;
    // 每次进行转移的最小值
    private static final int MIN_TRANSFER_STRIDE = 16;
    // 生成sizeCtl所使用的bit位数
    private static int RESIZE_STAMP_BITS = 16;
    // 进行扩容所允许的最大线程数
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;
    // 记录 sizeCtl 中的大小所需要进行的偏移位数
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;    
    // 一系列的标识
    static final int MOVED     = -1; // hash for forwarding nodes
    static final int TREEBIN   = -2; // hash for roots of trees
    static final int RESERVED  = -3; // hash for transient reservations
    static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
    // 
    /** Number of CPUS, to place bounds on some sizings */
    // 获取可用的CPU个数
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    // 
    /** For serialization compatibility. */
    // 进行序列化的属性
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("segments", Segment[].class),
        new ObjectStreamField("segmentMask", Integer.TYPE),
        new ObjectStreamField("segmentShift", Integer.TYPE)
    };
    
    // 表
    transient volatile Node<K,V>[] table;
    // 下一个表
    private transient volatile Node<K,V>[] nextTable;

    // 基本计数
    private transient volatile long baseCount;

    // 对表初始化和扩容控制
    private transient volatile int sizeCtl;
    
    /**
     * The next table index (plus one) to split while resizing.
     */
    // 扩容下另一个表的索引
    private transient volatile int transferIndex;

    // 旋转锁
    private transient volatile int cellsBusy;

    // counterCell表
    private transient volatile CounterCell[] counterCells;

    // views
    // 视图
    private transient KeySetView<K,V> keySet;
    private transient ValuesView<K,V> values;
    private transient EntrySetView<K,V> entrySet;
    
    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long SIZECTL;
    private static final long TRANSFERINDEX;
    private static final long BASECOUNT;
    private static final long CELLSBUSY;
    private static final long CELLVALUE;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = U.objectFieldOffset
                (k.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset
                (k.getDeclaredField("transferIndex"));
            BASECOUNT = U.objectFieldOffset
                (k.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset
                (k.getDeclaredField("cellsBusy"));
            Class<?> ck = CounterCell.class;
            CELLVALUE = U.objectFieldOffset
                (ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
```

　　说明：ConcurrentHashMap 的属性很多，其中不少属性在 HashMap 中就已经介绍过了，而对于 ConcurrentHashMap 而言，添加了 Unsafe 实例，主要用于反射获取对象相应的字段。

## 类的构造函数

```java
    public ConcurrentHashMap() {
    }
```

　　说明：该构造函数用于创建一个带有默认初始容量（16）、加载因子（0.75）和 concurrencyLevel(16) 的新的空映射。

```java
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0) // 初始化容量小于 0，抛出异常
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1)); // 找到最接近该容量的 2 的幂次方数
        this.sizeCtl = cap; // 初始化
    }
```

　　说明：该构造函数用于创建一个带有指定初始容量、默认加载因子（0.75）和 concurrencyLevel（16）的新的空映射。

```java
    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = DEFAULT_CAPACITY;
        putAll(m);
    }
```

　　说明：该构造函数用于构造一个与给定映射具有相同映射关系的新映射。

```java
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }
```

　　说明：该构造函数用于创建一个带有指定初始容量、加载因子和默认 concurrencyLevel(1) 的新的空映射。

```java
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)   // Use at least as many bins
            initialCapacity = concurrencyLevel;   // as estimated threads
        long size = (long)(1.0 + (long)initialCapacity / loadFactor);
        int cap = (size >= (long)MAXIMUM_CAPACITY) ?
            MAXIMUM_CAPACITY : tableSizeFor((int)size);
        this.sizeCtl = cap;
    }
```

　　说明：该构造函数用于创建一个带有指定初始容量、加载因子和并发级别的新的空映射。

　　对于构造函数而言，会根据输入的 initialCapacity 的大小来确定一个最小的且大于等于 initialCapacity 大小的 2 的 n 次幂，如 initialCapacity 为 15，则 sizeCtl 为 16，若 initialCapacity 为 16，则 sizeCtl 为 16。若 initialCapacity 大小超过了允许的最大值，则 sizeCtl 为最大值。值得注意的是，构造函数中的 concurrencyLevel 参数以及在 JDK 1.8 中的意义发生了很大的变化，其并不代表所允许的并发数，其只是用来确定 sizeCtl 大小，在 JDK 1.8 中的并发控制都是针对具体的桶而言，既有多少个桶就可以允许多少个并发数。

## put  - 添加

```java
    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    /** Implementation for put and putIfAbsent */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException(); // 键或值为空，则抛出异常
        // 键的 hash 值经过计算获得 hash 值
        int hash = spread(key.hashCode());
        int binCount = 0;
        // 无限循环
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            // 表为空或者表的长度为 0
            if (tab == null || (n = tab.length) == 0)
                // 初始化表
                tab = initTable();
            // 表不为空并且表的长度大于 0 ，并且该桶不为空
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 比较并且交换值，如 tab 的第 i 项为空则用新生成的 node 替换
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // no lock when adding to empty bin
            }
            // 该结点的 hash 值为 MOVED
            else if ((fh = f.hash) == MOVED)
                // 进行结点的转移（在扩容的过程中）
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                synchronized (f) { // 加锁同步
                    if (tabAt(tab, i) == f) { // 找到 table 表下表为 i 的结点
                        if (fh >= 0) { // 该 table 表中该结点的 hash 值大于 0
                            // binCount 赋值为 1
                            binCount = 1;
                            // 无限循环
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                // 结点的 hash 值相等并且 key 也相等
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     
                                     (ek != null && key.equals(ek)))) {
                                    // 保存该结点的 cal 值
                                    oldVal = e.val;
                                    // 进行判断
                                    if (!onlyIfAbsent)
                                        // 将指定的 calue 保存至结点，即进行了结点值的更新
                                        e.val = value;
                                    break;
                                }
                                // 保存当前结点
                                Node<K,V> pred = e;
                                // 当前结点的下一个结点为空，即为最后一个结点
                                if ((e = e.next) == null) {
                                    // 新生一个结点并且赋值给 next 域
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    // 退出循环
                                    break;
                                }
                            }
                        }
                        // 结点为红黑树结点类型
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            // binCount 赋值为 2
                            binCount = 2;
                            // 将 hash、key、value 放入红黑树
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                // 保存结点的 val
                                oldVal = p.val;
                                // 判断
                                if (!onlyIfAbsent)
                                    // 赋值结点 value 值
                                    p.val = value;
                            }
                        }
                        else if (f instanceof ReservationNode)
                            throw new IllegalStateException("Recursive update");
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
```







## 参考文章 

1. [ConcurrentHashMap实现原理](https://www.jianshu.com/p/8d7915d13cfc)
2. [ConcurrentHashMap底层实现原理(JDK1.8)源码分析](https://www.cnblogs.com/jing99/p/11330348.html)