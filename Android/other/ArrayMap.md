# ArrayMap

## 介绍
　　ArrayMap 是一种通用的 key -> value 映射数据结构，它要比传统的 HashMap 更节省内存。ArrayMap 是 Android 平台实现的一个版本，也可以在旧版本的平台上使用。ArrayMap 将映射保存在数组数据结构 --- 一个整数数组是每一项的哈希值，一个对象数组是键值对。这样 ArrayMap 就避免了为存储在映射表中的每个条目创建额外的对象，并且 ArrayMap 更加积极的尝试控制数组大小的增长（在数组大小增长时，只需要复制数组条目，而不需要重建映射表）。

　　如果不需要这里提供的标准 Java 容器 API(迭代器等)，请考虑使用 SimpleArrayMap 替换。

　　注意：ArrayMap 不适用于可能包含大量项的数据结构。在这种情况下，ArrayMap 要比传统的 HashMap 慢，这是因为查找是采用二分检索法，添加和删除需要在数组中插入和删除数据。对于在几百个之内的数据，ArrayMap 和 HashMap 之间的性能差异不大，小于 50%。

　　ArrayMap 在内存使用方面有很好的的平衡，并不会像大多数其他标准的 Java 容器，其他标准的 Java 容器会在从数组中删除项是压缩数组。目前，是没有办法去控制这种压缩的 --- 如果设置了一个容量，然后从中删除了一项，则可能会为了更好的匹配当前的大小而减少容量。在将来，一个显示调用方法设置了容器的容量应该会禁止这种压缩的行为。

## 总结



## 参考文章
[ArrayMap | Android Developers](https://developer.android.google.cn/reference/kotlin/androidx/collection/ArrayMap)