# Android 知识结构图

https://www.processon.com/view/5bebf0ebe4b01280769c0098#map

# 软件基础课程

* 计算机基础
* 计算机网络
  * HTTP、HTTPS
  * TCP、UDP
  * 网络请求过程
    * 三次握手
    * 四次挥手
    * 证书
    * 大图片
    * 断线续传
  * TCP/IP
  * Socket
* 面向对象开发
* 软件工程
* 计算方法
* 计算机图形学
* 算法与数据结构
* 操作系统原理
* 设计模式
  * 单例
  * 构造器
  * 装饰器模式
  * 中介模式
  * 观察者模式
  * 工厂模式
  * 代理模式
  * 适配器模式
  * 建造者模式

# Java 基础

* 面向对象
* 语法
* io
* 反射
  * class.getInstance()
  * getMethod
  * getFiled
  * setAccess
  * Method.invoke
* 泛型
* 集合
  * List
    * ArrayList
    * LinkList
    * SkipList
    * 线程安全
  * Map
    * HashMap
    * LinkedHashMap
    * TreeMap
  * Tree
  * Queue
    * LinkedList
    * ArrayDeque
    * 线程安全
  * Stack
* 内存模型
* 多线程
  * 线程池
  * Synchronized
  * volatile
  * ReentrantLock
  * 线程
* JVM
  * GCRoot
  * 类加载
  * 类的初始化实例化
* HashMap
* 注解
* 方法、变量修饰符

# 开源框架

* 异步处理
  * RxJava
  * RxAndroid
  * EventBUs
* 网络请求框架
  * OKHttp
  * Retrofit
* 图片加载
  * Glide
  * Picasso
* 依赖注入
  * ButterKnife
  * Dagger2
* 性能优化
  * LeakCanary
  * BlockCanary
* Jetpack
  * ViewModel
  * Livedata
  * Lifecycles
  * Navigation
  * Room
  * Paging
  * WorkManager
  * DataBinding

# 工具类库

* 文件操作
* 下载操作
* 图片操作

# Android 基础

* 基本控件使用
  * Toast
  * TextView
  * Button
  * EditText
  * ImageView
  * ProgressBar
  * AlertDialog
  * ProgressDialog
* android 布局
  * LinearLayout
  * FrameLayout
  * RelativeLayout
  * ConstraintLayout
  * 百分比布局
* 高级控件使用
  * ListView
    * 简单用法
    * 界面
    * 提升运行效率
  * RecyclerView
    * 简单用法
    * 定制布局
    * 点击事件
    * 缓存机制
  * WebView
    * js 通信
    * 优化
* android 四大组件 
  * Activity
    * 生命周期
    * 启动模式
      * Standard
      * SingleTask
      * SingleTop
      * SingleInstance
    * 横竖屏处理
  * Service
    * 生命周期
    * 两种启动方式
    * 后台进程保活技术
    * Binder
    * IntentService
  * Fragment
    * 生命周期
    * 回退栈
    * Fragment 间通信
    * Fragment 与 Activity 通信
    * Fragment+ViewPager 的懒加载
  * ContentProvider
    * 共享
    * 增删改查
  * BroadcastReceiver
    * 注册广播两种方式
    * 全局广播的优缺点
    * 搞笑的局部广播 LocalBroadcastReceiver
* Android studio
  * 使用和调试
  * 常用插件
    * 代码规范检测插件
  * 快捷键
* 日志使用
* 序列化
  * Serializable
  * Parcelable
* 数据存储
  * 文件存储
  * SharedPreferences
  * SQLite
* Handler、Looper、Message、Application
* Android 动画
  * FrameAnimation
  * TweenAnimation
  * PropertyAnimation
  * 第三方动画库
    * Loto
* Android 多媒体
  * 拍照
  * 录像
  * 选择照片或视频
  * 音频
  * 视频
* Materal Design
  * Toolbar
  * 沉浸式状态栏
  * CardView
  * AppBarLayout
  * DrawerLayout
  * NavigationView
  * RecyclerView
  * FloatingActionButton
  * Snackbar
  * 下拉刷新
* 网络
  * WebView
  * Http 请求
  * 下载文件
  * json & Gson
* Android framework
  * 系统启动流程
  * 应用启动流程
  * 进程间通信
    * Binder
    * 序列化
    * AIDL
  * apk 打包流程
  * SystemServer
    * ActivityManagerService
    * PackageManagerService
    * WindowManagerService
* 多线程
  * Handler
  * ThreadLocal
  * AsyncTask

# Android 进阶

* 视图
  * View
    * 自定义 View
      * MeasureSpec
      * layout
      * Draw
    * View 的事件分发机制
  * Window
    * ViewRoot
    * PhoneWindow
  * 事件分发机制
    * 滑动冲突
    * dispatch
  * 动画
    * 帧动画
    * View 动画
    * 属性动画
  * RecyclerView
    * 缓存机制
  * webview
    * js 通信
    * 优化
* 开发架构
  * MVC
  * MVP
  * MVVM
* Git
  * 基本使用
  * 分支
  * 代码合并与冲突处理
* 权限适配
  * Android 6 权限适配
  * Android 8 适配
    * apk 下载失败
    * 通知栏不限时
    * 无法安装 APK（权限）
* Gradle
  * Gradle 插件
  * Gradle 多渠道打包
  * gradle 混淆瘦身
  * Lint
  * 加固
* 进程间通讯
  * IPC
* 性能优化
  * UI 卡顿
  * 内存管理
  * 内存泄漏
  * 启动优化
  * UI 优化
  * 内存优化
  * 卡顿优化
  * 网络优化
  * 数据库优化
  * 包体积优化
* 与 H5 交互
* 图片
  * Bitmap
    * 记载效率
    * 缓存策略
    * 内存计算
  * Drawable

# 新技术

* Kotlin 语言
  * 扩展函数
  * 构造函数
  * Latent、by lazy
  * 协程
* Lambda 表达式
* 模块化
* 插件化
* 组件化
* 热更新
* Flutter
* React Native