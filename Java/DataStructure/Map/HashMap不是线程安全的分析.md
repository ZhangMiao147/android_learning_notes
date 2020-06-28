# HashMap 不是线程安全的分析

　　这里分析一下 HashMap 为什么是线程不安全的：

　　HashMap 底层是一个 Entry 数组，当发生 hash 冲突的时候，HashMap 是采用链表的方式来解决的，在对应的数组位置存访链表的头节点。对链表而言，新加入的节点会从头结点加入。

　　来分析一下多线程访问：

1. 在 HashMap 做 put 操作的时候会调用下面方法：

```java

	// 新增 Entry。将 “key-value” 插入指定位置，bucketIndex 是位置索引。      
    void addEntry(int hash, K key, V value, int bucketIndex) {      
        // 保存 “bucketIndex” 位置的值到 “e” 中      
        Entry<K,V> e = table[bucketIndex];      
        // 设置 “bucketIndex” 位置的元素为 “新 Entry”，      
        // 设置 “e”为“ 新 Entry 的下一个节点”      
        table[bucketIndex] = new Entry<K,V>(hash, key, value, e);      
        // 若 HashMap 的实际大小 不小于 “阈值”，则调整 HashMap 的大小      
        if (size++ >= threshold)      
            resize(2 * table.length);      
    } 
```

　　在 HashMap 做 put 操作的时候会调用到以上的方法。现在假如 A 线程和 B 线程同时对同一个数组调用 addEntry，两个线程会同时得到现在的头结点，然后 A 写入新的头结点之后，B 也写入新的头结点，那 B 的写入操作就会覆盖 A 的写入操作造成 A 的写入操作丢失。

2. 删除键值对的代码

```java
    final Entry<K,V> removeEntryForKey(Object key) {      
        // 获取哈希值。若 key 为 null，则哈希值为 0；否则调用 hash() 进行计算      
        int hash = (key == null) ? 0 : hash(key.hashCode());      
        int i = indexFor(hash, table.length);      
        Entry<K,V> prev = table[i];      
        Entry<K,V> e = prev;      
     
        // 删除链表中 “ 键为 key” 的元素      
        // 本质是 “ 删除单向链表中的节点 ”      
        while (e != null) {      
            Entry<K,V> next = e.next;      
            Object k;      
            if (e.hash == hash &&      
                ((k = e.key) == key || (key != null && key.equals(k)))) {      
                modCount++;      
                size--;      
                if (prev == e)      
                    table[i] = next;      
                else     
                    prev.next = next;      
                e.recordRemoval(this);      
                return e;      
            }      
            prev = e;      
            e = next;      
        }      
     
        return e;      
    }  
```

　　当多个线程同时操作同一个数组位置的时候，也都会先取得现在状态下该位置存储的头节点，然后各自去进行计算操作，之后再把结果写回到该数组位置去，其实写回的时候可能其他的线程已经把这个位置给修改过了，就会覆盖其他线程的修改。

3. addEntry 中当加入新的键值对后键值对总数量超过门限值的时候会调用一个 resize 操作，代码如下：

```java
    // 重新调整 HashMap 的大小，newCapacity 是调整后的容量      
    void resize(int newCapacity) {      
        Entry[] oldTable = table;      
        int oldCapacity = oldTable.length;     
        // 如果就容量已经达到了最大值，则不能再扩容，直接返回    
        if (oldCapacity == MAXIMUM_CAPACITY) {      
            threshold = Integer.MAX_VALUE;      
            return;      
        }      
     
        // 新建一个 HashMap，将 “ 旧 HashMap ” 的全部元素添加到 “ 新 HashMap ” 中，      
        // 然后，将 “ 新 HashMap” 赋值给 “ 旧 HashMap”。      
        Entry[] newTable = new Entry[newCapacity];      
        transfer(newTable);      
        table = newTable;      
        threshold = (int)(newCapacity * loadFactor);      
    }  
```

　　这个操作会生成一个新的容量的数组，然后对原数组的所有键值对重新进行计算和写入新的数组，之后指向新生成的数组。

　　当多个线程同时检测到总数量超过门限值的时候就会调用 resize 操作，各自生成新的数组并 rehash 后赋给该 map 底层的数组 table，结果最终只有最后一个线程生成的新数组被赋给 table 变量，其他线程的均会丢失。而且当某些线程已经完成赋值而其他线程刚开始的时候，就会用已经被赋值的 table 作为原始数组，这样也会有问题。



## 参考文章

1. [HashTable和HashMap的区别详解](https://www.cnblogs.com/williamjie/p/9099141.html)
