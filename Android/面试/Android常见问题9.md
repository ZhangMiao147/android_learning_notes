# Android 常见问题 9

# 1. SparseArray 和 hashmap 区别 

ArrayMap 和 SparseArray 是位于 android.util 包下，是 Android 版本 19 起引入的。

HashMap 是位于 java.util 包下。

## 1.1. SparseArray

SparseArray采用时间换取空间的方式来提高手机App的运行效率，这也是其与HashMap的区别；HashMap通过空间换取时间，查找迅速；HashMap中当table数组中内容达到总容量0.75时，则扩展为当前容量的两倍，关于HashMap可查看[HashMap实现原理学习](http://blog.csdn.net/xiaxl/article/details/72621758))

- SparseArray的key为int，value为Object。
- 在Android中，数据长度小于千时，用于替换HashMap
- 相比与HashMap，其采用 时间换空间 的方式，使用更少的内存来提高手机APP的运行效率(HashMap中当table数组中内容达到总容量0.75时，则扩展为当前容量的两倍，

## 1.2. ArrayList

ArrayMap和SparseArray有点类似；其中含有两个数组，一个是mHashes（key的hash值数组，为一个有序数组），另一个数组存储的是key和value，其中key和value是成对出现的，key存储在数组的偶数位上，value存储在数组的奇数位上。

# 2. Bitmap 和 Drawable 的区别



## 2.1. Bitmap 可以做缓存吗

