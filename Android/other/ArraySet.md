# ArraySet

## 介绍
　　ArraySet 是一种通用的 Set 数据结构，它的设计比传统的 HashSet 更节省内存。ArraySet 的设计和 ArrayMap 很相似，ArraySet 和 ArrayMap 的特点是一样的。但是，ArraySet 和 ArrayMap 的实现是分来的，因此，ArraySet 的对象数组中只包含了集合中每个项的一个项（而不是映射的一对项）。


　　注意：ArraySet 不适用于可能包含大量项的数据结构。在这种情况下，ArraySet 要比传统的 HashSet 慢，这是因为查找是采用二分检索法，添加和删除需要在数组中插入和删除数据。对于在几百个之内的数据，ArraySet 和 HashSet 之间的性能差异不大，小于 50%。

　　ArraySet 在内存使用方面有很好的的平衡，并不会像大多数其他标准的 Java 容器，其他标准的 Java 容器会在从数组中删除项时压缩数组。目前，是没有办法去控制这种压缩的 --- 如果设置了一个容量，然后从中删除了一项，则可能会为了更好的匹配当前的大小而减少容量。在将来，一个显示调用方法设置了容器的容量应该会禁止这种压缩的行为。

## 总结



## 参考文章
[ArraySet | Android Developers](https://developer.android.google.cn/reference/kotlin/androidx/collection/ArraySet)