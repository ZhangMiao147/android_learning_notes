# LruCache 知识

## 1. Android 中的缓存策略

　　一般来说，缓存策略主要包含缓存的添加、获取和删除这三类操作。如何添加和获取缓存这个比较好理解，那么为什么还要删除缓存呢？这是因为不管是内存缓存还是硬盘缓存，它们的缓存大小都是有限的。当缓存满了之后，再想向其添加缓存，这个时候就需要删除一些旧的缓存并添加新的缓存。

　　因此 LRU（Least Recently Used）缓存算法便应运而生，LRU 是近期最少使用的算法，它的核心思想是当缓存满时，会优先淘汰那些近期最少使用的缓存对象。采用 LRU 算法的缓存有两种：LruCache 和 DisLruCache，分别用于实现内存缓存和硬盘缓存，其核心思想都是 LRU 缓存算法。

## 2. LruCache 的使用

　　LruCache 是 Android 3.1 所提供的一个缓存类，所以在 Andriod 中可以直接使用 LruCache 实现内存缓存。而 DisLruCache 目前在 Android 还不是 Android SDK 的一部分，但 Android 官方文档推荐使用该算法来实现硬盘缓存。

### 2.1 LruCache 的介绍

　　LruCache 的使用非常简单，以图片缓存为例。

```java
 				int maxMemory = (int) (Runtime.getRuntime().totalMemory()/1024);
        int cacheSize = maxMemory/8;
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight()/1024;
            }
        };
```

1. 设置 LruCache 缓存的大小，一般为当前进程可用容量的 1/8。
2. 重写 sizeOf 方法，计算出要缓存的每张图片的大小。

　　注意：缓存的总容量和每个缓存对象的大小所用单位要一致。

## 3. LruCache 的实现原理

　　LruCache 的核心思想很好理解，就是要维护一个缓存对象列表，其中对象列表的排列方式是按照访问顺序实现的，即一直没访问的对象，将放在队尾，即将被淘汰。而最近访问的对象将放在对头，最后被淘汰。

　　如下图所示：

![](image/LRU缓存淘汰过程.png)

### 3.1. LinkedHashMap

　　那么这个队列到底是由 LinkedHashMap 来维护的。

　　而  LinkedHashMap 是由数组 + 双向链表的数据结构来实现的。其中双向链表的结构可以实现访问顺序和插入顺序，使得 LinkedHashMap 中的 <key,value> 对按照一定顺序排列起来。

　　通过下面构造函数来指定 LinkedHashMap 中双向链表的结构是访问顺序还是插入顺序。

```java
public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }
```

　　其中 accessOrder 设置为 true 则为访问顺序，为 false ，则为插入顺序。

　　以具体例子解释，当设置为 true 时：

```java
public static final void main(String[] args) {
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>(0, 0.75f, true);
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        map.put(4, 4);
        map.put(5, 5);
        map.put(6, 6);
        map.get(1);
        map.get(2);

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());

        }
    }
```

　　输出结果：

```java
0:0
3:3
4:4
5:5
6:6
1:1
2:2
```

　　即最近访问的最后输出，那么这就正好满足 LRU 缓存算法的思想。可见 LruCache 的实现就是利用了 LinkedHashMap 的这种数据结构。

### 3.2. LruCache 的构造方法

　　查看 LruCache 的源码，查看怎么应用 LinkedHashMap 来实现缓存的添加、获得和删除的。

```java
 public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }
```

　　从 LruCache 的构造函数中可以看到正是用了 LinkedHashMap 的访问顺序。

### 3.3. put() 方法

```java
public final V put(K key, V value) {
         // 不可为空，否则抛出异常
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        V previous;
        synchronized (this) {
            // 插入的缓存对象值加1
            putCount++;
            // 增加已有缓存的大小
            size += safeSizeOf(key, value);
           	// 向 map 中加入缓存对象
            previous = map.put(key, value);
            // 如果已有缓存对象，则缓存大小减去之前缓存占用的大小
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }
        // entryRemoved() 是个空方法，可以自行实现
        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }
        // 调整缓存大小(关键方法)
        trimToSize(maxSize);
        return previous;
    }
```

　　可以看到 put() 方法并没有什么难点，重要的就是在添加过缓存对象后，调用 trimToSize() 方法，来判断缓存是否已满，如果满了就要删除近期最少使用的缓存对象。

### 3.4. trimToSize() 方法

```java
 public void trimToSize(int maxSize) {
        // 死循环
        while (true) {
            K key;
            V value;
            synchronized (this) {
                //如果 map 为空并且缓存 size 不等于 0 或者缓存 size 小于 0，抛出异常
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }
                // 如果缓存大小 size 小于最大缓存，或者 map 为空，不需要再删除缓存对象，跳出循环
                if (size <= maxSize || map.isEmpty()) {
                    break;
                }
                // 迭代器获取第一个对象，即队尾的元素，近期最少访问的元素
                Map.Entry<K, V> toEvict = map.entrySet().iterator().next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                // 删除该对象，并更新缓存大小
                map.remove(key);
                size -= safeSizeOf(key, value);
                evictionCount++;
            }
            entryRemoved(true, key, value, null);
        }
    }
```

　　trimToSize() 方法不断的删除 LinkedHashMap 中队尾的元素，即近期最少访问的，直到缓存大小小于最大值。

　　当调用 LruCache 的 get() 方法获取集合中的缓存对象时，就代表访问了一次该元素，将会更新队列，保持整个队列是按照访问顺序排序。这个更新过程就是在 LinkedHashMap 中的 get() 方法中完成的。

### 3.5. get() 方法

```java
public final V get(K key) {
        // key 为空抛出异常
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            // 获取对应的缓存对象
            // get() 方法会实现将访问的元素更新到队列头部的功能
            mapValue = map.get(key);
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            missCount++;
        }
        ...
}
```

　　LruCache 的 get() 方法调用 LinkedHashMap 的 get() 方法来获取对应的缓存。而 LinkedHashMap 的 get() 方法会将访问的元素更新到队头。

#### 3.5.1. LinkedHashMap#get

　　其中 LinkedHashMap 的 get() 方法如下：

```java
public V get(Object key) {
        LinkedHashMapEntry<K,V> e = (LinkedHashMapEntry<K,V>)getEntry(key);
        if (e == null)
            return null;
        // 实现排序的关键方法
        e.recordAccess(this);
        return e.value;
    }
```

　　LinkedHashMap 的 get() 方法调用了 recordAccess() 方法来更新元素的顺序。

#### 3.5.2. LinkedHashMap#recordAccess

　　调用 recordAccess() 方法如下：

```java
 void recordAccess(HashMap<K,V> m) {
            LinkedHashMap<K,V> lm = (LinkedHashMap<K,V>)m;
            // 判断是否是访问排序
            if (lm.accessOrder) {
                lm.modCount++;
                // 删除此元素
                remove();
                // 将此元素移动到队列的头部
                addBefore(lm.header);
            }
        }
```

　　如果是访问顺序，会将元素从集合中删除，然后重新插入到队头。

## 4. 总结

　　LruCache 中维护了一个集合 LinkedHashMap，该 LinkedHashMap 是以访问顺序排序的。当调用 put() 方法时，就会在集合中添加元素，并调用 trimToSize() 判断缓存是否已满，如果满了就用 LinkedHashMap 的迭代器删除队尾元素，即近期最少访问的元素。当调用 get() 方法访问缓存对象时，就会调用 LinkedHashMap 的 get() 方法获得对应集合元素，同时会更新该元素到队头。

　　以上便是 LruCache 实现的原理，理解了 LinkedHashMap 的数据结构就能理解整个原理。

## 5. 参考文章

1. [彻底解析 Android 缓存机制 -- LruCache](https://www.jianshu.com/p/b49a111147ee)

