# SparseArrayCompat

## 介绍
　　SparseArrays 是一个和 HashMap 一样的映射表，但是不同的是 SparseArrays 的 key 是 Integer 类型。当使用 Integer 为 key 值去映射对象时，使用 SparseArrays 要比使用 HashMap 更节省内存，这是因为 SparseArrays 避免了自动装箱，并且它的数据结构对于每个映射不依赖额外的存储对象。

　　注意：SparseArrays 将其映射保存在一个数组数据结构中，使用二分检索法来查找键。如果存储大量的数据，SparseArrays 是不适合的，这种情况下，它要比使用 HashMap 还要慢，这是因为查找是采用二分检索法，添加和删除需要在数组中插入和删除数据。对于在几百个之内的数据，SparseArray 和 HashMap 之间的性能差异不大，小于 50%。

　　为了提供性能，SpareArrayCompat 在删除键的时候还做了一个优化：它不会立即压缩其数组，而是将删除的项标记为已删除，然后可以让同一个键的数值使用这个项，或者在后面垃圾回收机制时将所有需要回收的项全部回收。垃圾回收机制会在数组需要增加、检查映射表大小或者检索数值的时候执行。

　　可以使用 keyAt(int) 和 valueAt(int) 方法遍历容器中的项。使用 keyAt(int) 获取某个位置对应的键，或者使用 valueAt(int) 方法获取某个位置对应的值。

#### 关于 SpareArrayCompat
　　上面说的都是 SpareArray，好像和 SpareArrayCompat 没什么关系，其实它们是相同的，但是 SparseArray 只能在 API 19 （Android 4.4）以上的系统才能使用，为了可以更低版本的，所以才有了 SpareArrayCompat，SpareArrayCompat 是 SparseArray 的兼容版本，可以在更低版本的 Android 上运行。


## 总结
1. SpareArrayCompat 是一个 key 为 int 的映射表，它使用数组结果保存数据，采用二分检索法查找键，在删除键的时候不能立即压缩数据，而是将删除的项标记为已删除，然后可以让同一个键的数组使用这一项，或者在后面的垃圾回收时将所有需要回收的项全部回收。
2. SpareArrayCompat 在数据数量不多时，性能要比 HashMap 好，所以优先选择 SpareArrayCompat；在数据数量是几百个时，两者差别不大，选择两者任何一个都可；当需要存储大量数据时，HashMap 的性能要比 SpareArrayCompat 好，优先选择 HashMap。
3. 优点：节省最高 50% 的缓存。
4. 缺点：只适用于键值为 int 的映射数据。


## 参考文章
[SparseArrayCompat | Android Developers](https://developer.android.google.cn/reference/kotlin/androidx/collection/SparseArrayCompat)