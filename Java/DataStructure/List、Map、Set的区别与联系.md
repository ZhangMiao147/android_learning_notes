# List、Map、Set 的区别与联系

## 1. 结构特点

1. List 和 Set 是存储单列数据的集合，两个接口都是继承自 Collection；

   Map 是存储键值对这样的双列数据的集合；

2. List 中存储的数据是有顺序的，并且值允许重复；

   Map 中存储的数据是无序的，它的键是不允许重复的，但是值是允许重复的；

   Set 中存储的数据是无顺序的，并且不允许重复，但元素在集合中的位置是由元素的 hashcode 决定，即位置是固定的（Set 集合是根据 hashcode 来进行数据存储的，所以位置是固定的，但是这个位置不是用户可以控制的，所以对于用户来说 Set 中的元素还是无序的）。

## 2. 实现类

### 2.1. List 接口有三个实现类

　　次序是 List 的最重要特点，它确保维护元素特定的顺序。

1. LinkedList

   基于链表实现，链表内存是散列的，增删快，查找慢。

   对顺序访问进行优化，向 List 中间插入与移除的开销并不大，具有 addFirst()、addLast()、getFirst()、getLast()、removeFirst() 和 removeLast()。这些方法使得 LinkedList 可当作堆栈 / 队列 / 双向队列。

2. ArrayList

   基于数组实现，非线程安全，效率高，增删慢，查找快。

   允许对元素快速随机访问。

3. Vector

   基于数组实现，线程安全，效率低，增删慢，查找慢。

### 2.2. Map 接口有四个实现类

1. HashMap

   基于 hash 表的 Map 接口实现，非线程安全，高效，支持 null 值和 null 键；

2. HashTable

   线程安全，低效，不支持 null 值和 null 键；

3. LinkedHashMap

   是 HashMap 的一个子类，保存了记录的插入顺序；

4. SortedMap 接口

   SortedMap 接口需要数据的 key 支持 Comparable，或者可以被支持的 Comparator 接受。
   
   TreeMap 是 SortedMap 接口的基于红黑树的实现，此类保证了映射按照升序顺序排列关键字，根据使用的构造方法不同，可能会按照键的类的自然顺序进行排序（参见 Comparable），或者按照创建时所提供的比较器进行排序。此实现为 containsKey、get、put 和 remove 操作提供了保证的 log(n) 时间开销。

### 2.3. Set 接口有两个实现类

　　存入 Set 的每个元素必须唯一，不保证维护元素的次序。加入 Set 的 Object 必须定义 equals() 方法。

1. HashSet

   为快速查找而设计的 Set，底层是由 HashMap 实现，不允许集合中有重复的值，存入 HashSet 对象需要重写 equals() 和 hashCode() 方法。支持存 null。

2. LinkedHashSet

   继承于 HashSet，同时又基于 LinkedHashMap 来实现，底层使用的是 LinkedHashMap。支持存 null。
   
   具有 HashSet 的查询速度，但内部使用链表维护元素的次序。
   
3. TreeSet

   TreeSet 保护次序的 Set，使用它可以从 Set 中提取有序序列。不支持存 null。

## 3. 区别

1. List 集合中对象按照索引位置排序，可以有重复对象，允许按照对象在集合中的索引位置检索对象，例如通过 list.get(i) 方法来获取集合中的元素。
2. Map 中的每一个元素包含一个键和一个值，成对出现，键对象不可以重复，值对象可以重复；
3. Set 集合中的对象不按照特定的方式排序，并且没有重复对象，但它的实现类能对集合中的对象按照特定的方式排序，例如 TreeSet 类，可以按照默认顺序，也可以通过实现 Java.util.Comparator< Type > 接口来自定义排序方式。

## 4. 补充：HashMap 和 HashTable

　　HashMap 是线程不安全的，HashMap 是一个接口，是 Map 的一个子接口，是将键映射到值的对象，不允许键值重复，允许空键和空值；由于非线程安全，HashMap 的效率要较 HashTable 的效率高一些。

　　HashTable 是线程安全的一个集合，不允许 null 值作为一个 key 值或者 value 值。

　　HashTable 是 synchronzied（同步化），多个线程访问时不需要自己为它的方法实现同步，而 HashMap 在被多个线程访问的时候需要自己为它的方法实现同步。

## 5. 参考文章

1. [Java Set集合详解及Set与List的区别](https://blog.csdn.net/xiaoxiaovbb/article/details/80439643)
2. [三大集合：List、Map、Set的区别与联系](https://blog.csdn.net/yangxingpa/article/details/81023138)
3. [Java中Set与List的关系与区别介绍](https://www.jb51.net/article/62073.htm)