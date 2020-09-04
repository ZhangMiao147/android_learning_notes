# 各版本新特性
　　多进程通信

### Android4.0（api 14）

- Space 留白
- PopupMenu
- GlidLayout 网格布局

### Android5.0（api 21）

- MaterialDesign设计风格
- Material Theme
- Meterial Dialog
- CardView
- RecyclerView
- SwipeRefreshLayout
- Toolbar
- RippleDrawable 视图的水波纹效果
- android L 增加了一些 Activity 的转场动画 —— 爆炸、滑动、淡入淡出
- tint 前后背景着色
- Palette 从图像中提取突出的颜色，这样可以把色值赋给 ActionBar、Toolbar、或者其他，可以让界面整个色调统一。
- 支持64位ART虚拟机
- Heads-Up 风格通知。全新的通知中心设计（在锁屏界面也可以直接查看通知消息了，用户还可以直接在锁屏的情况下就行回复或进入应用。）
- setTaskDescription 最近使用。全新的“最近应用程序”。除了界面风格设计的改变之外，新的最近应用界面还借鉴了 Chrome 浏览器的理念，采用单独的标签展示方式。更重要的是，谷歌已经向开发者开放了 API，所以第三方开发人员可以利用这个改进为特定的应用增加全新的功能。
- Project Volta 电池续航改进计划。增加了 Battery Saver 模式，在低电量的时候系统会自动降低屏幕亮度、限制自动更换背景等功能。
   MediaProjection 截屏
- JobScheduler 通过为系统定义要在以后的某个时间或在指定的条件下（例如，当设备在充电时）异步运行的作业来优化电池寿命
   setClipToOutline 裁剪
- 支持多种设备（手机、平板电脑、笔记本电脑、智能电视、汽车、智能手表甚至是各种家用电子产品）
- 新的 API 支持，蓝牙 4.1、USB Audio、多人分享等其它特性
- Camera2
- PdfRenderer 使用位图来呈现 PDF 文件
- android.app.usage API 获取应用使用情况统计信息的 API

### Android6.0（api 23）

- 指纹识别
- 动态权限申请
- FloatingActionButton
- Snackbar
- TabLayout
- NavigationView
- CoordinatorLayout
- AppBarLayout
- CollapsingToolbarLayout
- App Linking
- TextInputLayout
- 大量漂亮流畅的动画
- 支持快速充电的切换
- 支持文件夹拖拽应用
- 相机新增专业模式
- 全新的电源键菜单 一般来说，安卓的电源键菜单都是关机/重启/飞行，安卓6.0变成了关机/重启/紧急，关机和重启就不用赘述了，这个紧急模式是为了手机快没电的时候设计的，相当于飞行模式的高级版，可以关闭一切耗电应用，尽最大可能节省电量。
- 可自定义锁界面样式 支持电话、信息、相机等快捷方式在锁屏界面的定制，用户可以根据自己的喜好调整这些图标的位置，或者开启或关闭这些快捷方式。
- 全新的快速设置风格 不但是锁屏界面可以定制，安卓6.0还采用了全新的快速面板的色彩方案，用户可以通过更换主题换颜色。
- 原生的应用权限管理 无需第三方应用和Root权限，原生的安卓6.0就支持应用权限管理，用户可以在安装应用时选择关闭一些应用权限，这一功能非常方便，再也不用担心流量偷跑和扣费了。
- Now on Tap功能 “Now on Tap ”功能，是指将Google Now(一种语音助手)作为底层植入到安卓6.0系统中，用户只要只要双击home键启动Google Now，“这意味着用户随时都能启动搜索功能，目前暂时不知道这个功能进入国内会不会阉割掉。
- 支持RAW格式照片 RAW格式的支持是众多拍照爱好者梦寐以求的， 然而绝大多数的安卓手机都没有或者剔除了这项功能。由于照片保存为jpg格式时或多或少都会损失一些画质，所以支持RAW格式是非常明智的。

### Android7.0（api 24）

- 画中画
- 分屏多任务
- 增强的Java8语言模式
- 通知栏快速回复
- 夜间模式
- OpenJDK替换Java API
- Android 7.0中采用了一项具有实时代码剖析功能的ARI JIT编译器，它能够在安卓应用程序在运行时不断提高自身的性能

### Android8.0（api 26）

- 通知变更
- 画中画模式
- 自适应图标
- 自动填充框架
- xml 字体和可下载字体
- Pinned Shortcut
- TextView 字体自动适配
- 媒体增强
- 其他特性

> - 可以设置 Activity 支持广色域；
> - 可以设置最大的屏幕宽高比；
> - 多屏幕支持，支持设备外接一个显示器；
> - 最新版本 emoji 支持，使用 EmojiCompat 类可以让应用在老版本的>* 应用上显示新的 emoji；
> - 支持点击位置的捕捉；
> - 支持设置应用类别，这些类别用于将应用呈现给用户的用途或功能相同的应用归类在一起，例如按流量消耗、电池消耗和存储消耗将应用归类。
> - Smart Text Selection，这个功能有人可能在今年老罗的锤子手机发布会上看到过，思想是很类似的，复制一段数字，就会出现直接拨打电话的选项，复制一段地址就会弹出进入地图的选项，地图 APP 可以一下这个地方能否成为一个很方便入口。

- 行为变更

> - 后台执行限制
> - 安全性
> - 网络连接和 HTTP(S) 连接
> - 权限：在 Android O 之前，如果应用在运行时请求权限并且被授予该权限，系统会错误地将属于同一权限组并且在清单中注册的其他权限也一起授予应用。对于针对 Android O 的应用，此行为已被纠正。系统只会授予应用明确请求的权限。然而，一旦用户为应用授予某个权限，则所有后续对该权限组中权限的请求都将被自动批准。
> - 媒体变更
> - Native libraries
> - 其他
>
> > - ContentProvider 支持分页，即获取内容的选中区域的子集；
> > - ContentProvider 和 ContentResolver 增加 refresh 方法，用来让客户端更容易的知道数据是不是最新；
> > - JobScheduler 更新，让应用更容易遵从后台执行限制；
> > - 集合的处理的变化，AbstractCollection.removeAll() 和 AbstractCollection.retainAll() 始终引发 NullPointerException；
> > - 语言区域和国际化变化(https://developer.android.com/preview/behavior-changes.html#lai)；
> > - 联系人提供程序使用情况统计方法的变更(https://developer.android.com/preview/behavior-changes.html#cpu)；
> > - 蓝牙 ScanRecord.getBytes() 方法检索的数据长度变更(https://developer.android.com/preview/behavior-changes.html#bt)；
> > - 输入和导航(https://developer.android.com/preview/behavior-changes.html#ian)；

- API 变更

> - WebView 新 API
> - findViewById：findViewById 函数现在返回的是 ，所以以后 findViewById 就不需要强转了。
> - 统一的 margins 和 padding：layout_marginVertical，layout_marginHorizontal，paddingVertical，paddingHorizontal
> - AnimationSet：支持了动画的 seek 和动画倒转播放
> - 提醒窗口

### Android9.0（api 28）

- 室内wifi定位
- 刘海屏的支持
- 通知
- 增强体验
- 通道设置，广播以及免打扰
- 多相机支持和相机更新
- 新的图片解码
- 动画
- HDR VP9视频，HEIF图像压缩和媒体API
- JobScheduler中的数据成本敏感度
- 神经网络API 1.1
- 改进表单自动填充
- 安全增强
- Android 备份加密


## 参考文章
1. [Android各大版本支持的新特性的汇总](https://www.jianshu.com/p/c4f1bf460c8b)

