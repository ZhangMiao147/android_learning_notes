# LongSparseArray

## 介绍
**(基本是官网文档的翻译)**

　　LongSparseArray 就是 SparseArray 映射 Long 到对象。与正常的对象数组不同，索引中可能是不连续的，LongSparseArray 要比使用 HashMap 映射 Long 到对象更节省内存，这是因为 LongSparseArray 避免了自动装箱，并且它的数据结构对于每个映射不依赖额外的存储对象。

　　注意：LongSparseArray 将其映射保存在一个数组数据结构中，使用二分检索法来查找键。如果存储大量的数据，LongSparseArray 是不适合的，这种情况下，它要比使用传统的 HashMap 还要慢，这是因为查找是采用二分检索法，添加和删除需要在数组中插入和删除数据。对于在几百个之内的数据，LongSparseArray 和 HashMap 之间的性能差异不大，小于 50%。

　　为了提供性能，LongSparseArray 在删除键的时候还做了一个优化：它不会立即压缩其数组，而是将删除的项标记为已删除，然后可以让同一个键的数值使用这个项，或者在后面垃圾回收机制时将所有需要回收的项全部回收。垃圾回收机制会在数组需要增加、检查映射表大小或者检索数值的时候执行。

　　可以使用 keyAt(int) 和 valueAt(int) 方法遍历容器中的项。使用 keyAt(int) 获取某个位置对应的键，或者使用 valueAt(int) 方法获取某个位置对应的值。

## 总结



## 参考文章
[LongSparseArray | Android Developers](https://developer.android.google.cn/reference/kotlin/androidx/collection/LongSparseArray)