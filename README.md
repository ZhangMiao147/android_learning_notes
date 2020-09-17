# Android 开发工程师知识目录

# Android

## 四大组件

#### Activity

* [Activity 的生命周期](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.md)
* [验证 Activity 生命周期的问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Activity%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E7%9A%84%E9%97%AE%E9%A2%98.md)
* [Activity 的启动模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E5%90%AF%E5%8A%A8%E6%A8%A1%E5%BC%8F.md)
* [验证 Activity 四种 launchMode ](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Activity%E5%9B%9B%E7%A7%8DlaunchMode.md)
* [验证 onNewIntent 方法](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81%20onNewIntent%20%E6%96%B9%E6%B3%95.md)
* [验证 Intent 的 flags ](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Intent%E7%9A%84flags.md)
* [关于 Intent 的全部 flags 介绍](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%85%B3%E4%BA%8EIntent%E7%9A%84%E5%85%A8%E9%83%A8flags%E4%BB%8B%E7%BB%8D.md)
* [Activity 的使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E4%BD%BF%E7%94%A8.md)
* [Activity 的启动流程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B.md)
* [Activity 的常见问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md)
* Binder
* Intent
* Window、Activity、DecorView 以及 ViewRoot 之间的关系
* IPC 多线程通信方式

#### Service

* [Service 的基础知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Service/Service%E7%9A%84%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86.md)
* [Service 的常见问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Service/Service%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md)
* JobService、JobScheduler
* Messenger
* IntentService

#### BroadcaseReceiver

* [BroadcaseReceiver 的基础知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/BroadcastReceiver/BroadcastRecevier的基础知识.md)
* [BroadcaseReceiver 的实现原理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/BroadcastReceiver/BroadcastRecevier的实现原理.md)
*  [BroadcaseReceiver 的常见问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/BroadcastReceiver/BroadcastRecevier的常见问题.md)
*  LocalBroadcastManager 

#### ContentProvider

* [ContentProvider 的知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/ContentProvider/ContentProvider%E7%9A%84%E7%9F%A5%E8%AF%86.md)
* [ContentProvider 的常见问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/ContentProvider/ContentProvider%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md)
* [ContentProvider 运行过程源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/ContentProvider/ContentProvider运行过程源码分析.md)
* [ContentProvider 的共享数据更新通知机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/ContentProvider/ContentProvider的共享数据更新通知机制.md)
* [守护进程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/守护进程.md)
* [ActivityManagerService](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/ActivityManagerService.md)
* [WindowManagerService](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/WindowManagerService.md)
* [PackageManagerService之启动解析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/PackageManagerService之启动解析.md)
* [IntentFilter 的匹配规则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/IntentFilter的匹配规则.md)
* pms
* [ContentProvider 之文件储存](https://blog.csdn.net/kaiwii/article/details/7780593)

### Fragment

* [Fragment 的基础知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/fragment/Fragment的基础知识.md)
* [Fragment 的使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/fragment/Fragment的使用.md)

### Handler

* [Handler 机制分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/handler/Handler机制分析.md)
* [Handler 的延伸](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/handler/Handler的延伸.md)
* [ThreadLocal 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/handler/ThreadLocal知识.md)
* [AsyncTask 的知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/handler/AysncTask的知识.md)
* HandlerThread
* IdleHandler

### View

* [Activity 的布局绘制过程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/Activity的布局绘制过程.md)
* [View 绘制流程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/View绘制流程.md)
* [视图状态与重绘流程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/视图状态与重绘流程.md) 
* [View 事件分发机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/View事件分发机制.md)
* [ViewGroup 事件分发机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/ViewGroup事件分发机制.md)
* [自定义View的实现方式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/自定义View的实现方式.md)
* GestureDetector
* ActionBarDrawerToggle 

#### View 应用

* [Android实现卡片翻转的动画（翻牌动画）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/view%E7%9A%84%E5%AE%9E%E8%B7%B5/Android%E5%AE%9E%E7%8E%B0%E5%8D%A1%E7%89%87%E7%BF%BB%E8%BD%AC%E7%9A%84%E5%8A%A8%E7%94%BB%EF%BC%88%E7%BF%BB%E7%89%8C%E5%8A%A8%E7%94%BB%EF%BC%89.md)
* [系统控件的常用使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/view%E7%9A%84%E5%AE%9E%E8%B7%B5/%E7%B3%BB%E7%BB%9F%E6%8E%A7%E4%BB%B6%E7%9A%84%E5%B8%B8%E7%94%A8%E4%BD%BF%E7%94%A8.md)

### AIDL

* AIDL

### 缓存

* [缓存机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/cache/缓存机制.md)
* [LruCache知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/cache/LruCache知识.md)

### 优化

* [ANR 相关知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/ANR%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86.md)
* [发生 ANR 条件的源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/%E5%8F%91%E7%94%9F%20ANR%20%E6%9D%A1%E4%BB%B6%E7%9A%84%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)
* [性能优化](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96.md)
* [Android 内存知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/Android内存知识.md)
* [内存泄漏](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/内存泄漏.md)
* [内存溢出](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/内存溢出.md)
* [布局优化之 include](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/布局优化之include.md)
* [布局优化之 merge](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/布局优化之merge.md)
* [布局优化之 ViewStub](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/optimize/布局优化之ViewStub.md)

### 测试

#### 单元测试

* [Android单元测试之一：基本概念](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/test/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95/Android%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95%E4%B9%8B%E4%B8%80%EF%BC%9A%E5%9F%BA%E6%9C%AC%E6%A6%82%E5%BF%B5.md)
* [Android单元测试之二：本地测试](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/test/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95/Android%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95%E4%B9%8B%E4%BA%8C%EF%BC%9A%E6%9C%AC%E5%9C%B0%E6%B5%8B%E8%AF%95.md)
* [Android单元测试之三：使用模拟框架模拟依赖](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/test/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95/Android%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95%E4%B9%8B%E4%B8%89%EF%BC%9A%E4%BD%BF%E7%94%A8%E6%A8%A1%E6%8B%9F%E6%A1%86%E6%9E%B6%E6%A8%A1%E6%8B%9F%E4%BE%9D%E8%B5%96.md)
* [Android单元测试之四：仪器化测试](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/test/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95/Android%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95%E4%B9%8B%E5%9B%9B%EF%BC%9A%E4%BB%AA%E5%99%A8%E5%8C%96%E6%B5%8B%E8%AF%95.md)

### 适配

* [Android 4.4.4 的 setResult 失效的问题适配](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/adaptation/Android4.4.4%E7%9A%84setResult%E5%A4%B1%E6%95%88%E7%9A%84%E9%97%AE%E9%A2%98%E9%80%82%E9%85%8D.md)
* [Android 4.4.4 支持分包后找不到勒的问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/adaptation/Android%204.4.4支持分包后找不到类的问题.md)
* 适配
* 屏幕适配

### Gradle

* gradle
* [api 与 implementation 的区别](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/gradle/api%E4%B8%8Eimplementation%E7%9A%84%E5%8C%BA%E5%88%AB.md)

### 其他

* [多渠道打包](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/Software/%E5%A4%9A%E6%B8%A0%E9%81%93%E6%89%93%E5%8C%85.md)

* [jar 包和 aar 包的区别](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/other/jar%E5%8C%85%E4%B8%8Eaar%E5%8C%85%E7%9A%84%E5%8C%BA%E5%88%AB.md)

* [日常问题记录](https://github.com/ZhangMiao147/android_learning_notes/tree/master/Android/question)

* [Android 中 Home 键的监听](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/other/Android中Home键的监听.md)

* ArrayDeque

* ArrayMap

* SpareArray

* nano ptotobufs

* 协程

* Android Dex 分包 https://www.jianshu.com/p/e96f345e822f

* SurfaceView 

* WindowManager

  [Android解析WindowManager（一）WindowManager体系](https://blog.csdn.net/itachi85/article/details/77888668)

* DiskLruCache

  [Android DiskLruCache完全解析，硬盘缓存的最佳方案](https://blog.csdn.net/guolin_blog/article/details/28863651)

* Protocal buffers 

* 

### 架构与设计模式

* [设计模式选择](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E9%80%89%E6%8B%A9.md)

### 混淆

* ProGuard 

## 网络

* [计算机网络基础知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/计算机网络基础知识.md)
* [TCP 和 UDP](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/TCP与UDP/TCP和UDP.md)
* [TCP 粘包](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/TCP与UDP/TCP粘包.md)
* [Socket 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/Socket/Socket知识.md)
* [Socket 常用函数接口详解](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/Socket/Socket常用函数接口详解.md)
* [HTTP](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/HTTP/HTTP.md)
* [HTTPS](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/HTTPS/HTTPS.md)
* [IP 协议](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/IP/IP协议.md)
* [IPv4 和 IPv6](https://github.com/ZhangMiao147/android_learning_notes/blob/master/network/IP/IPv4和IPv6.md)
* KCP

## 数据结构

* [线性表顺序存储](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/线性表顺序存储.md) 
* [线性表链式存储](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/线性表链式存储.md)
* [堆](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/堆.md)
* [Set 集合](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/Set集合.md)
* 图
* 并查集
* 栈

### 树

* [树的基本知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/树的基本知识.md)
* [二叉树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/二叉树.md)
* [二分搜索树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/二分搜索树.md)
* [平衡二叉树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/平衡二叉树.md)
* [线段树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/线段树.md)
* [字典树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/字典树.md)
* [Huffman 树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/Huffman树.md)
* [红黑树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/红黑树.md)
* [B 树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/B树.md)
* [B+ 树](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/数据结构/树/B%2B树.md)

## 算法

* [递归](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/递归.md)
* 回溯算法

* [分治算法](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/分治算法.md)

* [分支定界法](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/分支定界法.md)

* [贪心算法](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/贪心算法.md)

* [动态规划（Dynamic Programming）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/动态规划.md)

### 查找算法

* 查找算法知识
* [顺序查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/顺序查找.md)
* [二分查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/二分查找.md)
* [插值查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/插值查找.md)
* [斐波那契查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/斐波那契查找.md)
* [二叉树查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/二叉树查找.md)
* [平衡查找树之2-3查找树（2-3 Tree）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/平衡查找树之2-3查找树.md)
* [平衡查找树之红黑树（Red-Black Tree）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/平衡查找树之红黑树.md)
* [B 树和 B+ 树（B Tree/B+ 树）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/B树和B%2B树.md)
* [分块查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/分块查找.md)
* [哈希查找](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/查找算法/哈希查找.md)

### 排序算法

* 排序算法总结
* [直接插入排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/直接插入排序.md)
* [希尔排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/希尔排序.md)
* [简单选择排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/简单选择排序.md)
* [堆排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/堆排序.md)
* [冒泡排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/冒泡排序.md)
* 快速排序
* [归并排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/归并排序.md)
* [基数排序](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DataStructure/算法/排序算法/基数排序.md)

## Java

### 基础

* [Java 泛型](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/基础/Java泛型.md)

* [Java 代理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/基础/Java代理.md)

* [Java 反射](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/基础/Java反射.md)

* [在Java反射中Class.forName和ClassLoader的区别.md](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/基础/在Java反射中Class.forName和ClassLoader的区别.md)

* [Java 注解](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/基础/Java注解.md)

* NIO

* Java 中字符串 String switch 的实现原理

  [Java中字符串String Switch的实现原理](https://blog.csdn.net/MOLIILOM/article/details/51166697)

* Java 线程的创建

* Java 数据类型

* java 读写文件的几种方式

* Java 注解

  [Java 注解（Annotation）](https://www.runoob.com/w3cnote/java-annotation.html)

* Vector

* ConcurrentModificationException 异常

### 数据结构

* [Collection 与 Collections](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Colletion%E4%B8%8EColletions.md)
* [ArrayList 与 LinkedList](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/ArrayList%E4%B8%8ELinkedList.md)
* [String、StringBuffer 与 StringBuilder 的区别](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/String%E3%80%81StringBuilder%E4%B8%8EStringBuffer%E7%9A%84%E5%8C%BA%E5%88%AB.md)
* [HashMap](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/HashMap.md)
* [HashMap 不是线程安全的分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/HashMap不是线程安全的分析.md)
* [HashMap 和 HashTable 的区别](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/HashMap和HashTable的区别.md)
* [ConcurrentHashMap](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/ConcurrentHashMap.md)
* [JDK1.7ConcurrentHashMap的源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/JDK1.7ConcurrentHashMap的源码分析.md)
* [JDK1.8ConcurrentHashMap的源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/DataStructure/Map/JDK1.8ConcurrentHashMap的源码分析.md)
* hashCode
* treeMap
* String
* 动态代理类 Proxy
* PriorityQueue
* transient
* CopyOnWriteArrayList
* SortedMap / TreeMap
* ArrayBlockingQueue 阻塞队列

### 线程

* [AtomicBoolean](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/%E7%BA%BF%E7%A8%8B/AtomicBoolean.md)
* [Executor、Executors、ExecutorService 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/%E7%BA%BF%E7%A8%8B/Executor%E3%80%81Executors%E3%80%81ExecrtorService%E7%9F%A5%E8%AF%86.md)
* [同步的知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/同步的知识.md)
* [volatile 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/volatile知识.md)
* [synchronized 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/Synchronized知识.md)
* [锁的知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/锁的知识.md)
* [Java 中常见的锁](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/Java中常见的锁.md)
* [死锁](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/死锁.md)
* [ReentrantLock 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/ReentrantLock知识.md)
* [Condition 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/Condition知识.md)
* [使用synchronzied、wait、notifyAll实现生产者-消费者模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/使用synchronzied-wait-notifyAll实现生产者-消费者模式.md)
* [使用信号量实现生产者-消费者模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/使用信号量实现生产者-消费者模式.md)
* [使用管程实现生产者-消费者模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/使用管程实现生产者-消费者模式.md)
* [哲学家就餐问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/哲学家就餐问题.md)
* [读者写者问题](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Java/线程/读者写者问题.md)
* Semaphore
* countdownlatch



## 开源库

### Retrofit

* [Retrofit 官方文档翻译](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/Retrofit/Retrofit官方文档翻译.md)
* [Retrofit 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/Retrofit/Retrofit知识.md)
* [Retrofit 源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/Retrofit/Retrofit源码分析.md)

### RabbitMQ

* [RabbitMQ 知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/MessageQueue/RabbitMQ知识.md)
* [提高 RabbitMQ 传输消息数据的可靠性途径](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/MessageQueue/提高RabbitMQ传输消息数据的可靠性途径.md)
* [RabbitMQ 的使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/MessageQueue/RabbitMQ的使用.md)
* [RabbitMQ 消息幂等性](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/MessageQueue/RabbitMQ消息幂等性.md)

### OkHttp

* [OkHttp 基础知识](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/OkHttp/OkHttp基础知识.md)
* [OkHttp 设置自定义拦截器](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/OkHttp/OkHttp设置自定义拦截器.md)
* [OkHttp 源码解析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/OkHttp/OkHttp源码分析.md)

### RxJava

* [RxJava 1 源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/RxJava/RxJava1源码分析.md)
* [RxJava 1 线程切换源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/RxJava/RxJava1线程切换源码分析.md)
* [RxJava 1 操作符源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/RxJava/RxJava1操作符源码分析.md)
* [RxJava 2 与 RxJava 1 的对比](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/RxJava/RxJava2与RxJava1的对比.md)
* [RxJava 2 背压源码分析](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/RxJava/RxJava2背压源码分析.md)



### EventBus



### ButterKnife



### RxAndroid



### XWalkView



### Okio



### 组件化与插件化



### 热修复



### TagSoup

* 解析 html内容

[在 Android 应用中使用 HTML Parser 便捷的解析 html 内容](https://www.ibm.com/developerworks/cn/opensource/os-cn-android-hp/index.html)

## 多媒体

* Opengl
  * GLES20 API



## 设计模式

* [UML类图](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/UML%E7%B1%BB%E5%9B%BE.md)
* 创建型模式
  * [简单工厂模式（Simple Factory）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E7%AE%80%E5%8D%95%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F.md)
  * [工厂方法模式（Factory Method）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95%E6%A8%A1%E5%BC%8F.md)
  * [抽象工厂模式（Abstract Method）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E6%8A%BD%E8%B1%A1%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F.md)
  * [单例模式（Singleton）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%8D%95%E4%BE%8B%E6%A8%A1%E5%BC%8F.md)
* 结构型模式
  * [适配器模式（Adapter）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E9%80%82%E9%85%8D%E5%99%A8%E6%A8%A1%E5%BC%8F.md)
  * [代理模式（Proxy）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8F.md)
* 行为型模式
  * [职责链模式（Chain of Responsibility）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/责任链模式.md)
  * [观察者模式（Observer）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E8%A7%82%E5%AF%9F%E8%80%85%E6%A8%A1%E5%BC%8F.md)

## 操作系统

* [操作系统概述](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/操作系统概述.md)
* [进程与线程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/进程与线程.md)
* [死锁](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/死锁.md)
* [内存管理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/内存管理.md)
* [设备管理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/设备管理.md)
* [文件管理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/文件管理.md)
* [作业管理](https://github.com/ZhangMiao147/android_learning_notes/blob/master/system/作业管理.md)

## 数据库



## Flutter



## RN



## kotlin



## 其他

* 类图、时序图的绘制

* Fiddler 使用教程

  [fiddler4使用教程](https://blog.csdn.net/chaoyu168/article/details/51065644)

* 


## 读书笔记
### Java 核心技术卷一：基础知识

* 第二章 [Java程序设计环境](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC2%E7%AB%A0-Java%E7%A8%8B%E5%BA%8F%E8%AE%BE%E8%AE%A1%E7%8E%AF%E5%A2%83.md)
* 第三章 [Java的基本程序设计结构](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC3%E7%AB%A0-Java%E7%9A%84%E5%9F%BA%E6%9C%AC%E7%A8%8B%E5%BA%8F%E8%AE%BE%E8%AE%A1%E7%BB%93%E6%9E%84.md)

* 第五章 [继承](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC5%E7%AB%A0-%E7%BB%A7%E6%89%BF.md)

* 第六章 [接口与内部类](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC6%E7%AB%A0-%E6%8E%A5%E5%8F%A3%E4%B8%8E%E5%86%85%E9%83%A8%E7%B1%BB.md)

* 第七章 [图形程序设计](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC7%E7%AB%A0-%E5%9B%BE%E5%BD%A2%E7%A8%8B%E5%BA%8F%E8%AE%BE%E8%AE%A1.md)

* 第八章 [事件处理](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC8%E7%AB%A0-%E4%BA%8B%E4%BB%B6%E5%A4%84%E7%90%86.md)

* 第九章 [Swing用户界面组件](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC9%E7%AB%A0-Swing%E7%94%A8%E6%88%B7%E7%95%8C%E9%9D%A2%E7%BB%84%E4%BB%B6.md)

* 第十章 [部署应用程序和applet](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC10%E7%AB%A0-%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E5%92%8Capplet.md)

* 第十一章 [异常、断言、日志与调试](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC11%E7%AB%A0-%E5%BC%82%E5%B8%B8%E3%80%81%E6%96%AD%E8%A8%80%E3%80%81%E6%97%A5%E5%BF%97%E5%92%8C%E8%B0%83%E8%AF%95.md)

* 第十二章 [泛型程序设计](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC12%E7%AB%A0-%E6%B3%9B%E5%9E%8B%E7%A8%8B%E5%BA%8F%E8%AE%BE%E8%AE%A1.md)

* 第十三章 [集合](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC13%E7%AB%A0-%E9%9B%86%E5%90%88.md)

* 第十四章 [多线程](https://github.com/havenBoy/java-book-notes/blob/master/Java%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF%E5%8D%B7%E4%B8%80%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/%E7%AC%AC14%E7%AB%A0-%E5%A4%9A%E7%BA%BF%E7%A8%8B.md)

### 第一行代码

* 第 2 章 [先从看得到的入手，探究活动](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC2%E7%AB%A0-%E5%85%88%E4%BB%8E%E7%9C%8B%E5%BE%97%E5%88%B0%E7%9A%84%E5%85%A5%E6%89%8B%EF%BC%8C%E6%8E%A2%E7%A9%B6%E6%B4%BB%E5%8A%A8.md)
* 第 3 章 [软件也要拼脸蛋，UI 开发的点点滴滴](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC3%E7%AB%A0-%E8%BD%AF%E4%BB%B6%E4%B9%9F%E8%A6%81%E6%8B%BC%E8%84%B8%E8%9B%8B%2CUI%E5%BC%80%E5%8F%91%E7%9A%84%E7%82%B9%E7%82%B9%E6%BB%B4%E6%BB%B4.md)

* 第 4 章 [手机平板要兼顾，探究碎片](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC4%E7%AB%A0-%E6%89%8B%E6%9C%BA%E5%B9%B3%E6%9D%BF%E8%A6%81%E5%85%BC%E9%A1%BE%EF%BC%8C%E6%8E%A2%E7%A9%B6%E7%A2%8E%E7%89%87.md)

* 第 5 章 [全部大喇叭，详解广播机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC5%E7%AB%A0-%E5%85%A8%E9%83%A8%E5%A4%A7%E5%96%87%E5%8F%AD%EF%BC%8C%E8%AF%A6%E8%A7%A3%E5%B9%BF%E6%92%AD%E6%9C%BA%E5%88%B6.md)

* 第 6 章 [数据存储全方案，详解持久化技术](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC6%E7%AB%A0-%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8%E5%85%A8%E6%96%B9%E6%A1%88%EF%BC%8C%E8%AF%A6%E8%A7%A3%E6%8C%81%E4%B9%85%E5%8C%96%E6%8A%80%E6%9C%AF.md)

* 第 7 章 [跨程序共享数据，探究内容提供器](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC7%E7%AB%A0-%E8%B7%A8%E7%A8%8B%E5%BA%8F%E5%85%B1%E4%BA%AB%E6%95%B0%E6%8D%AE%EF%BC%8C%E6%8E%A2%E7%A9%B6%E5%86%85%E5%AE%B9%E6%8F%90%E4%BE%9B%E5%99%A8.md)

* 第 8 章 [丰富你的程序，运行手机多媒体](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC8%E7%AB%A0-%E4%B8%B0%E5%AF%8C%E4%BD%A0%E7%9A%84%E7%A8%8B%E5%BA%8F%EF%BC%8C%E8%BF%90%E8%A1%8C%E6%89%8B%E6%9C%BA%E5%A4%9A%E5%AA%92%E4%BD%93.md)

* 第 9 章 [后台默默的劳动者，探究服务](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC9%E7%AB%A0-%E5%90%8E%E5%8F%B0%E9%BB%98%E9%BB%98%E7%9A%84%E5%8A%B3%E5%8A%A8%E8%80%85%EF%BC%8C%E6%8E%A2%E7%A9%B6%E6%9C%8D%E5%8A%A1.md)

* 第 10 章 [看看精彩的世界，使用网络技术](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC10%E7%AB%A0-%E7%9C%8B%E7%9C%8B%E7%B2%BE%E5%BD%A9%E7%9A%84%E4%B8%96%E7%95%8C%EF%BC%8C%E4%BD%BF%E7%94%A8%E7%BD%91%E7%BB%9C%E6%8A%80%E6%9C%AF.md)

* 第 11 章 [Android 特色开发，基于位置的服务](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC11%E7%AB%A0-Android%E7%89%B9%E8%89%B2%E5%BC%80%E5%8F%91%EF%BC%8C%E5%9F%BA%E4%BA%8E%E4%BD%8D%E7%BD%AE%E7%9A%84%E6%9C%8D%E5%8A%A1.md)

* 第 12 章 [Android 特色开发，使用传感器](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC12%E7%AB%A0-Android%E7%89%B9%E8%89%B2%E5%BC%80%E5%8F%91%EF%BC%8C%E4%BD%BF%E7%94%A8%E4%BC%A0%E6%84%9F%E5%99%A8.md)

* 第 13 章 [继续进阶，你还应该掌握的高级技巧](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E7%AC%AC%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81/%E7%AC%AC13%E7%AB%A0-%E7%BB%A7%E7%BB%AD%E8%BF%9B%E9%98%B6%EF%BC%8C%E4%BD%A0%E8%BF%98%E5%BA%94%E8%AF%A5%E6%8E%8C%E6%8F%A1%E7%9A%84%E9%AB%98%E7%BA%A7%E6%8A%80%E5%B7%A7.md)

### Effective Java

* [读书笔记](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Effective%20Java/EffectiveJavaReadRecord.txt)

### Java 多线程编程核心技术

* 第 1 章 [Java 多线程技能](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC1%E7%AB%A0-Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E6%8A%80%E8%83%BD.md)
* 第 2 章 [对象及变量的并发访问](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC2%E7%AB%A0-%E5%AF%B9%E8%B1%A1%E5%8F%8A%E5%8F%98%E9%87%8F%E7%9A%84%E5%B9%B6%E5%8F%91%E8%AE%BF%E9%97%AE.md)
* 第 3 章 [线程间通信](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC3%E7%AB%A0-%E7%BA%BF%E7%A8%8B%E9%97%B4%E9%80%9A%E4%BF%A1.md)
* 第 4 章 [Lock的使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC4%E7%AB%A0-Lock%E7%9A%84%E4%BD%BF%E7%94%A8.md)
* 第 5 章 [定时器 Timer](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC5%E7%AB%A0-%E5%AE%9A%E6%97%B6%E5%99%A8Timer.md)
* 第 6 章 [单例模式与多线程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC6%E7%AB%A0-%E5%8D%95%E4%BE%8B%E6%A8%A1%E5%BC%8F%E4%B8%8E%E5%A4%9A%E7%BA%BF%E7%A8%8B.md)
* 第 7 章 [拾遗增补](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E7%BC%96%E7%A8%8B%E6%A0%B8%E5%BF%83%E6%8A%80%E6%9C%AF/%E7%AC%AC7%E7%AB%A0-%E6%8B%BE%E9%81%97%E5%A2%9E%E8%A1%A5.md)

### 代码整洁之道

* 第一章 [整洁代码](https://github.com/havenBoy/java-book-notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC1%E7%AB%A0-%E6%95%B4%E6%B4%81%E4%BB%A3%E7%A0%81.md)
* 第二章 [有意义的命名](https://github.com/havenBoy/java-book-notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC2%E7%AB%A0-%E6%9C%89%E6%84%8F%E4%B9%89%E7%9A%84%E5%91%BD%E5%90%8D.md)

* 第三章 [函数](https://github.com/havenBoy/java-book-notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC3%E7%AB%A0-%E5%87%BD%E6%95%B0.md)

* 第四章 [注释](https://github.com/havenBoy/java-book-notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC4%E7%AB%A0-%E6%B3%A8%E9%87%8A.md)

* 第五章 [格式](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC5%E7%AB%A0-%E6%A0%BC%E5%BC%8F.md)

* 第六章 [对象和数据结构](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC6%E7%AB%A0-%E5%AF%B9%E8%B1%A1%E5%92%8C%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.md)

* 第七章 [错误处理](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC7%E7%AB%A0-%E9%94%99%E8%AF%AF%E5%A4%84%E7%90%86.md)

* 第八章 [边界](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC8%E7%AB%A0-%E8%BE%B9%E7%95%8C.md)

* 第九章 [单元测试](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC9%E7%AB%A0-%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95.md)

* 第十章 [类](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC10%E7%AB%A0-%E7%B1%BB.md)

* 第十一章 [系统](https://github.com/havenBoy/JavaBook-Notes/blob/master/%E4%BB%A3%E7%A0%81%E6%95%B4%E6%B4%81%E4%B9%8B%E9%81%93/%E7%AC%AC11%E7%AB%A0-%E7%B3%BB%E7%BB%9F.md)

### 深入理解 Java 虚拟机

* 第 1 章 [走进 Java](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Java%20%E8%99%9A%E6%8B%9F%E6%9C%BA/%E7%AC%AC1%E7%AB%A0-%E8%B5%B0%E8%BF%9BJava.md)
* 第 2 章 [Java 内存区域与内存溢出异常](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第2章-Java内存区域与内存溢出异常.md)
* 第 3 章 [垃圾收集器与内存分配策略](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第3章-垃圾收集器与内存分配策略.md)
* 第 4 章 [虚拟机性能监控、故障处理工具](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第4章-虚拟机性能监控、故障处理工具.md)
* 第 5 章 [调优案例分析与实战](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第5章-调优案例分析与实战.md)
* 第 6 章 [类文件结构](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第6章-类文件结构.md)
* 第 7 章 [虚拟机类加载机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第7章-虚拟机类加载机制.md)
* 第 8 章 [虚拟机字节码执行引擎](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第8章-虚拟机字节码执行引擎.md)
* 第 9 章 [类加载及执行子系统的案例与实战](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第9章-类加载及执行子系统的案例与实战.md)
* 第 10 章 [前端编译与优化](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Java%20%E8%99%9A%E6%8B%9F%E6%9C%BA/%E7%AC%AC10%E7%AB%A0-%E5%89%8D%E6%AE%B5%E7%BC%96%E8%AF%91%E4%B8%8E%E4%BC%98%E5%8C%96.md)
* 第 11 章 [后端编译与优化](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Java%20%E8%99%9A%E6%8B%9F%E6%9C%BA/%E7%AC%AC11%E7%AB%A0-%E5%90%8E%E7%AB%AF%E7%BC%96%E8%AF%91%E4%B8%8E%E4%BC%98%E5%8C%96.md)
* 第 12 章 [Java 内存模型与线程](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第12章-Java内存模型与线程.md)
* 第 13 章 [线程安全与锁优化](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/深入理解%20Java%20虚拟机/第13章-线程安全与锁优化.md)

### 设计模式之禅

* 第 1 章 [单一职责原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC1%E7%AB%A0-%E5%8D%95%E4%B8%80%E8%81%8C%E8%B4%A3%E5%8E%9F%E5%88%99.md)
* 第 2 章 [里氏置换原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC2%E7%AB%A0-%E9%87%8C%E6%B0%8F%E6%9B%BF%E6%8D%A2%E5%8E%9F%E5%88%99.md)
* 第 3 章 [依赖倒置原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC3%E7%AB%A0-%E4%BE%9D%E8%B5%96%E5%80%92%E7%BD%AE%E5%8E%9F%E5%88%99.md)
* 第 4 章 [接口隔离原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC4%E7%AB%A0-%E6%8E%A5%E5%8F%A3%E9%9A%94%E7%A6%BB%E5%8E%9F%E5%88%99.md)
* 第 5 章 [迪米特原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC5%E7%AB%A0-%E8%BF%AA%E7%B1%B3%E7%89%B9%E6%B3%95%E5%88%99.md)
* 第 6 章 [开闭原则](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B9%8B%E7%A6%85/%E7%AC%AC6%E7%AB%A0-%E5%BC%80%E9%97%AD%E5%8E%9F%E5%88%99.md)

### 图解 HTTP

* 第 1 章 [了解 Web 及网络基础](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/图解HTTP/第1章-了解Web及网络基础.md)
* 第 2 章 [简单的 HTTP 协议](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/图解HTTP/第2章-简单的HTTP协议.md)
* 第 3 章 [HTTP 报文内的 HTTP 信息](https://github.com/ZhangMiao147/android_learning_notes/tree/master/BookNote/图解HTTP)
* 第 4 章 [返回结果的 HTTP 状态码](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/图解HTTP/第4章-返回结果的HTTP状态码.md)
* 第 5 章 [与 HTTP 协作的 Web 服务器](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/图解HTTP/第5章-与HTTP协作的Web服务器.md)
* 第 6 章 [HTTP 首部](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/图解HTTP/第6章-HTTP首部.md)

### Android 开发艺术探索

* 第 1 章 [Activity 的生命周期和启动模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Android开发艺术探索/第1章-Activity%20的声明周期和启动模式.md)
* 第 2 章 [IPC 机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/BookNote/Android开发艺术探索/第2章-IPC机制.md)

