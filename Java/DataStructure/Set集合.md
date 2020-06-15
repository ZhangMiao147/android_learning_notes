# Set 知识

## 1. Set 是什么？

　　Java 中的 Set 集合是继承 Collection 的接口，是一个不包含重复元素的集合。

　　下图是 Set 集合的源码：

![](image/Set源码.png)

　　Set 和 List 都是以接口的形式来进行声明。Set 主要包含三种存访数据类型的变量，分别是 HashSet、LinkedHashSet、TreeSet。

## 2. HashSet、LinkedHashSet、TreeSet 的主要使用情景

### 2.1. HashSet

　　HashSet 从名称就可以看出肯定是和 Hash 这样的数据接口有关，打开 HashSet 源码可以看到一个很熟悉的对象：

```java
public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    static final long serialVersionUID = -5024744406713321676L;
	// 是用 HashMap 存储数据的
    private transient HashMap<E,Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public HashSet() {
        map = new HashMap<>();
    }
    
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }
    
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }
    
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
    
    // hashSet 添加元素，即向 hashmap 下 put 元素，这也是为什么 hashSet 不会出现重复的元素
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
}
```

　　从 HashSet  1.9 版本的构造器可以看出 HashSet 的底层就是 HashMap 来构建的，并可以添加初始容量和加载因子，来调整反应时间或是空间容量。

### 2.2. LinkedHashSet

```java
public class LinkedHashSet<E>
    extends HashSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2851667679971038690L;

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, .75f, true);
    }

    public LinkedHashSet() {
        super(16, .75f, true);
    }

    public LinkedHashSet(Collection<? extends E> c) {
        super(Math.max(2*c.size(), 11), .75f, true);
        addAll(c);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
    }
}

```

　　LinkedHashSet 的构造函数调用了父类 HashSet 的构造方法：

```java
   HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
```

　　而 map 被初始成成 LinkedHashMap 对象，也就是说这完完全全就是 LinkedHashMap 的数据结构，并且符合其所有属性和性质，有序、不可重复。

### 2.3. TreeSet

```java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    /**
     * The backing map.
     */
    private transient NavigableMap<E,Object> m;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

    public TreeSet() {
        this(new TreeMap<E,Object>());
    }

    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }

    public TreeSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public TreeSet(SortedSet<E> s) {
        this(s.comparator());
        addAll(s);
    }
}
```

　　可以看出 TreeSet 同样也是以 TreeMap 作为存储结构，有序，不可重复。

　　实际上，可以看出，Set 的实体类主要就是以 map 为基础，相对应的使用环境也和对应的 map 相同。

### 2.4. 区别

　　TreeSet 采用红黑树的数据结构排序元素。

　　HashSet 采用散列函数，这是专门为快速查询而设计的。

　　LinkedHashSet 内部使用散列以加快查询速度，同样使用链表维护元素的次序。

　　使用 HashSet/TreeSet 时，必须为类定义 equals()，而 HashCode() 是针对 HashSet，作为一种编程风格，当覆盖 equals() 的时候，就应该同时覆盖 hashCode()。

## 4. 那为什么会构造 Set 这个集合呢？

　　实际上就是利用 map 的 key-value 键值对的方式，通过 key 的唯一的特性，主要将 set 构建的对象放入 key 中，以这样的方式来使用集合的遍历一些特性，从而可以直接用 Set 来进行调用。

## 5. 参考文章

1. [Java Set集合详解及Set与List的区别](https://blog.csdn.net/xiaoxiaovbb/article/details/80439643)