# Collection 与 Collections

## Collection
　　它提供了对集合对象进行基本操作的通用接口方法。Collection 接口在 Java 类库中有很多具体的实现。Collection 接口的意义是为各种具体的几个提供了最大化的统一操作方式。

![](./image/Collection类图.jpg)

　　注意 Collection 跟 Map 没有联系。

## Collections
　　一个 util 包下的工具类，其类不能被实例化，提供了许多实用的 static 方法。

　　常用的方法有：

　　1.Collections.sort                对集合排序（需要实现 int compareTo）

　　2.Collections.synchronizedMap     返回一个线程安全的 map

　　3.Collections.binarySearch        二分查找一个元素

　　4.Collections.shuffle             对集合进行随机排序（就是指每次排序后都不同）