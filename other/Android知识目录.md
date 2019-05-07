# Android 面试题收集

## UI
1. ListView
2. RecyclerView
3. RecyclerView 与 ListView 的对比
4. TextView
5. ImageView
6. Dialog
7. adapter 的泛型和不指定参数数量的优化
8. Android 自定义 View 或者自定义 ViewGroup 的一般步骤
9. Android 的视图层级优化
10. Android 实现卡片翻转的动画（翻牌动画）
11. 如何显示 git 图片
12. 滑动控件的深度优化
13. 图表
14. view 的事件分发机制？滑动冲突怎么解决？
15. 自定义 view 的原理和流程？
16. LinearLayout 的布局流程？
17. view 的getWidth和geimeasurewidth有啥区别？
18. view的绘制流程
19. viewpager嵌套滑动冲突怎么解决？
20. 阅读界面书架用什么控件实现？布局怎么做到每行的文字左右对齐？
21. 直播界面，微信对话界面实现？
22. Android:gravity和android:layout_gravity的区别？
23. assets与res/raw的区别？
24. 解释layout_weight的作用，view如何刷新？
25. android常用布局及排版效率？
26. recyclerView 缓存机制相比 listView 缓存机制有啥改进？
27. constainlayout
28. bitmap 有几种格式，分别占多少字节？
29. 系统进程可以用webview吗？
30. 计算viewgroup的层级，递归实现和非递归实现
31. Bitmap resize 相关 (设置 option 然后 decode)

## 解析xml
1. sax解析xml的优点

## 动画
1. animation.animationlistener干什么用？
2. 属性动画画一个抛物线怎么弄？
3. svg动画
4. animation 和 animator 的用法，概述实现原理
5. 不见动画常见的效果？有哪几个常见的插入器？
6. 补间动画click事件还在原位怎么解决？

## Android 源码分析
1. 主线程使用 Handler 的过程
2.  AsyncTask 和 Handler + Thread 机制的区别
3. 事件的处理机制
4. Handler、Message、Looper 的原理
5. Handler,looper,messagequeue,thread,message,每个类功能、关系？
6. 有哪些多进程通信方式？Binder机制？
7. ams是怎么找到启动的那个activity的？
8. android 事件分发机制
9. Contentvalue键值类型
10. androidvm的进程与linux的进程说法？
11. override与ovrtload的区别？overload的方法是否可以改变返回值的类型？
12. sleep与wait有什么区别？
13. 在android中，请简述jni的调用过程？请结束android.mk的作用，并试写一个android.mk文件（包含一个.c源文件即可）
14. binder 的原理与机制
15. 原子类的了解一个app多进程的好处
16. 主线程 looper 如果没有消息，就会阻塞在那，为什么不会anr？
17. 自己写一个应用，包名就叫 android 行不行，为什么不行？
18. 主线程 Looper 一直循环查消息为何没卡主线程？
19. 用MultiDex解决何事？其根本原因在于？Dex如何优化？主Dex放哪些东西？主Dex和其他Dex调用、关联？Odex优化点在于啥？Dalvik和Art虚拟机区别？多渠道打包如何实现（Flavor、Dimension应用）？从母包生出渠道包实现方法？渠道标识替换原理？
20. Android 打包哪些类型文件不能混淆？

## 常用的开源库总结
* 这个库是做什么用的？
* 为什么要在项目中使用这个库？
* 这个库都有哪些用法？对应什么样的使用场景？
* 这个库的优缺点是什么，跟同类型库的比较？
* 这个库的核心原理是什么？如果让你实现这个库的某些核心原理，你会考虑怎么去实现？
* 你从这个库中学到什么有价值的或者说可借鉴的设计思想？

**网络加载相关博文**

[用 Retrofit 2 简化 HTTP 请求](https://realm.io/cn/news/droidcon-jake-wharton-simple-http-retrofit-2/)
[快速Android开发系列网络篇之Retrofit](http://www.cnblogs.com/angeldevil/p/3757335.html)
[Android OkHttp完全解析 是时候来了解OkHttp了](http://blog.csdn.net/lmj623565791/article/details/47911083)
[快速Android开发系列网络篇之Android-Async-Http](http://www.cnblogs.com/angeldevil/p/3729808.html)
[Android Volley完全解析(一)，初识Volley的基本用法](http://blog.csdn.net/guolin_blog/article/details/17482095)
[Android Volley完全解析(二)，使用Volley加载网络图片](http://blog.csdn.net/guolin_blog/article/details/17482165)
[Android 网络通信框架Volley简介(Google IO 2013)](http://blog.csdn.net/t12x3456/article/details/9221611)

** 图片加载相关博文 **
[Android 网络通信框架Volley简介(Google IO 2013)](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/0731/1639.html)
[Android的媒体管理框架:Glide 3.0发布](http://www.infoq.com/cn/news/2014/09/android-glide?utm_source=tuicool&utm_medium=referral)
[*开源选型之 Android 三大图片缓存原理、特性对比](http://mp.weixin.qq.com/s?__biz=MzAxNjI3MDkzOQ==&mid=400056342&idx=1&sn=894325d70f16a28bfe8d6a4da31ec304&scene=2&srcid=10210byVbMGLHg7vXUJLgHaR&from=timeline&isappinstalled=0#rd)
[Android Universal Image Loader 源码分析](http://a.codekk.com/blogs/detail/54cfab086c4761e5001b2540)
[Android DiskLruCache源码解析硬盘缓存的绝佳方案](http://blog.csdn.net/lmj623565791/article/details/47251585)
[android中图片的三级cache策略（内存、文件、网络）](http://blog.csdn.net/singwhatiwanna/article/details/9054001)

** 图片处理相关博文 **
[Android高效加载大图、多图解决方案，有效避免程序OOM](http://blog.csdn.net/guolin_blog/article/details/9316683)
[Android照片墙应用实现，再多的图片也不怕崩溃](http://blog.csdn.net/guolin_blog/article/details/9526203)
[Android多点触控技术实战，自由地对图片进行缩放和移动](http://blog.csdn.net/guolin_blog/article/details/11100327)
[Android 高清加载巨图方案 拒绝压缩图片](http://blog.csdn.net/lmj623565791/article/details/49300989)
[Android 优化Bitmap避免OutOfMemoryError](http://chjmars.iteye.com/blog/1157137)

#### Retrofit
1. Retrofit 官方文档翻译
2. Retrofit 的使用
3. Retrofit 的实现原理

#### okHttp
1. okHttp 实现原理
2. okHttp 有哪些拦截器
3. okhttp 有什么优秀的设计模式？builder 模式有什么好处？责任链模式有什么好处？

#### RxJava

#### zxing
1. zxing二维码开源框架流程
2. zxing 有过优化提高识别率吗？

#### Glide
1. Glide 缓存特点。

#### 第三方开源数据库



## 适配
1. 权限代码中请求
2. 安装 apk 的版本不同的处理
3. UI 适配
4. Android 各个版本的差异
5. 为了适配多分辨率，引入什么开源框架？

## 安全
1. 数据加密
2. 代码混淆
3. WebView/Js调用
4. https

## 数据库
1. sqlite 可以执行多线程操作吗？如何保证多线程操作数据库的安全性
2. 数据库的使用
3. 隔代数据库升级
4. shareprefrence 不是进程安全，假设一个 apk 两个进程同时修改 shareprefrence 怎么办？
5. shareprefrence 原理？是否线程安全和进程安全？

## 优化（怎么评测和具体优化）
#### 内存优化
1. 造成内存泄漏的情况有哪些
2. 自定义 Handler 时如何避免内存泄漏
3. ANR 出现的场景及解决方案
4. 怎么分析内存泄漏

#### APK 瘦身

#### 电量优化

#### 布局优化

#### 流畅性优化
1. 怎么评测是否流畅（GPU程序模式）
2. 工具或者技巧（腾讯GT 插件的SM指标，用了 TraceView、Systrace、Hierarchy Viewer 等等）
3. 哪些情况会导致卡顿？（FPS、垂直同步的原理）

#### 网络优化

#### 一个app如果性能不好，怎么分析？

#### 性能优化怎么弄？
1. 避免内存泄漏，为什么说handler用成员内部类会内存泄漏？activity不是已经到gcroot被切断了吗？还有静态context持有activity的引用会内存泄漏，必须要持有怎么办？（及时释放）

## 混淆
1. Progrard 介绍
2. 为什么要使用代码混淆

## 线程与进程
1. 线程间通信和进程间通信有什么不同
2. 进程切换时需要保存哪些东西（进程上下文）
3. 如果一个app存在多进程，请列出全部的ipc方法
4. 操作系统中进程和线程有什么联系和区别，系统什么时候会在用户态和内核态中切换？如何加载ndk库？如何在jni中注册native函数，有几种注册方式？
5. 一个app启动页另开一个进程，启动页 10s后启动mainactivity，请问5s的时候有几个进程？

## 研发工具
1. IDE
2. 模拟器
3. 网络代理
4. 日志

## 架构
1. 模块化怎么做的？怎么设计的？接口发现暴露怎么做？基于什么思想？MVC、MVP、MVVM 应用和彼此本质区别？


## 其他
1. Android 的打包流程？apk里有哪些东西？签名算法的原理？
2. 了解哪些插件化技术？
3. Android 怎么做保活？
4. 动态配置manifest文章完成 （记录到记录文章列表中）
* 参考文章 https://www.jianshu.com/p/e64f0b89e570  https://blog.csdn.net/DG_summer/article/details/53678247
5. 动态代理静态代理区别？


## 平时开发遇到的系列问题记录
5. app闪退的原因有哪些？每种情况简述分析过程？
6. 如何避免out of menmory 和 anr?

## 面试准备资源推荐
1. 《Android开发艺术探索》 任玉刚
2. 《Android 进阶之光》刘望舒
3. 《Android 进阶解密》 刘望舒
4. 《深入理解 Java 虚拟机》 周志明
5. 《Android 源码设计模式》 何红辉 关爱明
6. 《深入理解 Android 内核设计思想》 林学森


## 收集的文章
[收集Android方方面面的经典知识, 最新技术](https://github.com/itheima1/Android#%E5%A4%9A%E5%AA%92%E4%BD%93%E7%BC%96%E7%A8%8B)
