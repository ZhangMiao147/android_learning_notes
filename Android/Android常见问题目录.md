# Android 的常见问题目录

# Android 的常见问题 1

1. Activity 的启动流程
   * 主要对象功能介绍
   * 启动流程
     * SystemService
     * Launcher
     * Instrumentation
     * AMS 和 ActivityThread 之间的 Bindler 通信
     * AMS 接收到客户端的请求之后，会如何开启一个 Activity?
2. Activity 的启动模式，及其使用场景
   * Activity 的四种启动模式
     * standard（标准模式）
       * 标准模式的应用场景
     * singleTop（栈顶复用）
       * 栈顶复用模式的应用场景
     * singleTask（栈内复用）
       * 栈内复用模式的应用场景
     * singleInstance（单例模式）
       * 单例模式的应用场景
   * Intent 的 flags
     * 常用的 flags 介绍
       * FLAG_ACTIVITY_CLEAR_TOP
       * FLAG_ACTOVOTY_NO_HISTORY
       * FLAG_ACTIVITY_SINGLE_TOP
       * FLAG_ACTIVITY_NEW_TASK
       * FLAG_ACTIVITY_NEW_DOCUMENT
   * launchMode 与 Intent 的 flags 的对比
   * Activity 属性
3. Activity 的事件分发机制
   * Activity
   * view
     * View#dispatchTouchEvent
     * View#onTouchEvent
   * ViewGroup
   * onTouch 和 onTouchEvent 有什么区别？又该如何使用
4. Android 的缓存机制
   * 图片缓存
     * 内存缓存
     * 磁盘缓存
     * 设备配置参数改变时加载问题
   * 使用 SQLite 进行缓存
   * 文件缓存
   * LruCache 的缓存原理
5.  gradle 构建周期
6. Handler
   * Handler 消息机制的流程
     * Message
     * MessageQueue
     * Handler
     * Looper
   * Android 子线程创建 Handler 方法
     * 方法1 （直接获取当前自线程的 looper）
     * 方法 2（获取主线程的 looper，或者说是 UI 线程的 looper）
   * Handler 的内存泄漏的处理
7.  Fragment 的生命周期，与 Activity 生命周期的比较
8. 跨进程通信方式
   * Android 中的 IPC 方式
     * 使用 Bundle
     * 使用文件共享
     * 使用 Messenger
     * 使用 AIDL
     * 使用 ContentProvider
     * 使用 socket
   * 选用合适的 IPC 方式

# Android 的常见问题 2

1. 性能优化
   * 内存优化
     * 内存泄漏
     * 内存移除
     * 内存分析工具
     * 内存优化的操作
   * UI 优化
     * 布局优化
       * 关于布局优化的方法
     * 绘制优化
       * 关于绘制优化的方法
     * 检查 UI 优化的放啊
       * 调试过度绘制
       * 查看每帧的渲染时长
   * 启动优化
     * 启动方式
       * 冷启动
       * 暖启动
       * 热启动
     * 如何优化
   * 网络优化
     * 图片优化
     * 网络请求处理的优化
   * 包体优化
     * 包体优化的工具
     * 检查包体的工具
   * 电量优化
     * 具体实践
     * 电量优化的法则
     * 电量优化的方法
2. View 的绘制流程
   * Measure 过程
     * MeasureSpec 简介
     * ViewGroup.LayoutParams 简介
     * View 测量的基本流程及重要方法分析
   * layout 过程
   * draw 过程
   * Android 的 wrap_content 是如何计算的
3. 进程保活
   * 进程划分
     * 前台进程
       * 常见场景
     * 可见进程
       * 常见场景
     * 服务进程
       * 常见场景
     * 后台进程
       * 常见场景
     * 空进程
       * 常见进程
   * 黑色保活
   * 白色保活
   * 灰色保活
   * 双进程守护
   * JobService
   * 具体的守护方案
     * 开启一个像素的 Activity
     * 前台进程
     * 互相唤醒
     * JobScheduler
     * 粘性服务 & 系统服务绑定
     * 双进程守护
     * 优化
   * 动画的分类及其原理
     * 视图动画和属性动画的区别
   * SparseArray 原理
   * 一个 Android 程序至少包含几个线程

# Andriod 的常见问题 3

1. ANR

   

# Android 常见问题 6

1. android launcher 的架构怎么样，用到什么模式
2. 问 rgb_565 位图，一个像素占多少位？
3. Android View 的刷新机制
4. 内存机制

5. JNI 线程需要对 java VM 做的操作
   * 创建线程
   * 附着在 Java 虚拟机上
   * 等待线程返回结果
   * 同步线程块
6. activity 和 service 的通信方式
7. 并发和并行分别是什么意思，多线程是并发还是并行
8. 一个按钮，手抖了连续点了两次，会跳转两次页面，怎么让这种情况不发生



# Android 常见问题 8

1. ListView 源码分析
2. RecyclerView 源码分析
3. Android 时间分发机制，如何处理冲突
4. webView 有哪些问题
5. Bitmap 图片优化
6. 使用 JNI 时，如何在 C++ 代码中访问到一个 java 对象
7. ServiceManager、ActivityManager、packageManager
8. Binder 原理
9. FrameWork 层的核心类
10. IPC 机制



TCP原理，如何确保稳定（与udp相比），阻塞， 

反射如何实现 

文件上传下载原理，下载中流的大小； 

操作系统，cpu调度 

•数据库

Linux指令

权限机制

ClassLoader和DexLoader会用吗

Monkey能跑多长时间？ 

Intent是如何实现Activity、Service等之间的解耦合的？ 

View的渲染机制

[View渲染机制](https://blog.csdn.net/say_from_wen/article/details/79093883)

Hook机制

[Android Hook 机制之简单实战](https://www.jianshu.com/p/c431ad21f071)

mmap + native 日志优化

广播和 EventBus 的区别

跨进程传递大内存数据如何做

启动优化怎么优化

synchronized 底层实现原理，ReentrantLock 公平锁与非公平锁

主线程等待所有线程执行完毕，再执行某个特定任务怎么实现？原理和源码看过没？

自定义 view 的一般流程，要注意些什么如何优化，点击事件和长按时间分别是怎么实现的？

四种启动模式，在源码分析中的原理是怎样的？

.讲讲 bindService 的过程，你当初是怎么优化后台服务进程的？

RxJava 在使用过程中碰到了某些不友好的错误一般怎么解决？发现了内存泄露一般怎么解决分析，有没有碰到过系统服务内存泄露的问题？

给你个数 1 吧，比如 1000011 里面有几个 1 ？

你看过 binder 驱动的源码，说说他的内存映射过程，说说客户端等待服务端处理返回的流程，如果要跨进程传递大内存数据你具体会怎么做？简单写一写吧。

