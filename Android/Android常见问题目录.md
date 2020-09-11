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

   