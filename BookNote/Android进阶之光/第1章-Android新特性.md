# 第 1 章 Android 新特性

## 1.1 Android 5.0 新特性

#### 1.1.1 Android 5.0 主要新特性

###### 1. 全新的 Material Design 新风格
　　Material Design 是一种大胆的平面化创新。换句话说，谷歌希望能够让Material Design 给用户带来纸张化的体验。这种新的视觉语言，在基本元素的处理上，借鉴了传统的印刷设计，以及字体版式、网络系统、空间、比例、配色和图像使用等这些基础的平面设计规范。另外，Material Design 还推崇实体隐喻理念，利用实体的表面与边缘的质感打造出视觉线索。让用户感受到真实性。熟悉的触感让用户可以快速地理解并认知。在设计中可以在符合物理规律的基础上灵活地运用物质，打造出不同的使用体验。为了吸引用户的注意力，Material Design 还带来了有意义而且更合理的动态效果，以及维持整个系统的连续性体验。

###### 2. 支持多种设备
　　Android 系统的身影早已出现在多种设备中。

###### 3. 全新的通知中心设计
　　谷歌在 Android 5.0 中加入了全新风格的通知系统。改进后的通知系统会优先显示对用户来说比较重要的信息，而将不太紧急的内容隐藏起来。用户只需要向下滑动就可以查看全部的通知内容。

###### 4. 支持 64 位 ART 虚拟机（ART:Android runtime）
　　Android 5.0 内部的性能上也提升了不少，它放弃了之前一直使用的 Dalvik 虚拟机，改用了 ART 虚拟机，实现了真正的跨平台编译，在 ART、X86、MIPS 等无处不在。

###### 5. Overview
　　多任务视窗现在有了一个新的名字，Overview 。在界面上，每一个 App 都是一个独立的卡片，拥有立体式的层叠效果，用户可以指定 “最近应用程序”，通过滑动来快速切换 App。

###### 6. 设备识别解锁
　　Android 5.0 增加了针对特定设备（例如智能手表）识别解锁的模式。当设备没有检测到附近有可用的信任设备时，就会启动安全模式以防止未授权访问。

###### 7. Ok Google 语音指令
　　只需要说出简单的语言指令，就能操作手机。

###### 8. Face unlock 面部解锁
　　在 Android 5.0 中，Google 花费大力气优化了面部解锁功能。当用户拿起手机处理锁屏界面上的消息通知时，面部解锁功能便自动被激活。

#### 1.1.2 替换 ListView 和 GridView 的 RecyclerView
　　有了 ListView、GridView，为什么还需要 RecyclerView 这样的控件呢？从整体上看，RecyclerView 架构提供了一种插拔式的体验，它具有高度的解耦、异常的灵活性和更高的效率，通过设置它提供的不同 LayoutManager、ItemDecoration、ItemAnimator 可实现更加丰富多样的效果。但是 RecyclewView 也有缺点和让人头疼的地方：设置列表的分割线需要自定义，另外列表的点击事件需要自己去实现。

###### 1. 配置 build.gradle
　　要想使用 RecyclerView，首先要倒入 support-7 包。

###### 2. 使用 RecyclerView
　　与 ListView 不同的一点就是，需要设置布局管理器用于设置条目的排列样式，可以是垂直排列或者水平排列。

　　此外，RecyclerView 比 ListView 的设置要复杂一些，主要是它需要自己去自定义分割线，设置动画和布局管理器，等等。

　　RecyclerView 的 Adapter 最大的改进就是对 ViewHolder 进行了封装定义，只需要自定义一个 ViewHolder 继承 RecyclerView.ViewHolder 就可以了。另外，Adapter 继承了 RecyclerView.Adapter，在 onCreateViewHolder 中加载布局，在 onBindViewHolder 中将视图与数据进行绑定。

###### 3. 设置分割线
　　可以使用 mRecyclerView.addItemDecoration() 来加入分割线。谷歌目前没有提供默认的分割线，这就需要我们继承 RecyclerView.ItemDecoration 来自定义分割线。

　　虽然没有默认的分割线，但是好处是：可以更灵活地自定义分割线。

###### 4. 自定义点击事件
　　列表中条目的点击事件需要自己来定义，这是一个不尽如人意的地方。但是，自定义点击事件也并不是很难。在 Adapter 中定义接口并提供回调。

###### 5. 实现 GridView
　　只需要自定义横向的分割线。

###### 6. 实现瀑布流
　　RecyclerView 支持瀑布流，它更稳定、效率更高、自定义能力更强。

　　实现瀑布流很简单，只要在 Adapter 写一个随机的高度来控制每个 item 的高度就可以了。在 Adapter 的 onBindViewHolder 中设置每个 item 的高度。

#### 1.1.3 卡片 CardView
　　Android 5.0 版本中新增了 CardView，CardView 继承自 FrameLayout 类，并且可以设置圆角和阴影，使得空间具有立体性，也可以包含其他的布局容器和控件。

###### 1. 配置 build.gradle
　　如果 SDK 低于 5.0，仍旧要引入 v7 包。在 build.gradle 中引入 support-v7 包。

###### 2. 使用 CardView
　　CardView 的重要属性：card_view:cardCornerRadius，设置圆角半径：card_view:cardElevation，设置阴影的半径。

　　初次之外，CardView 还有其他属性：
* CardView_cardBackgroundColor：设置背景色。
* CardView_cardElevation：设置 Z 轴阴影。
* CardView_cardMaxElevation：设置 Z 轴最大高度值。
* CardView_cardUseCompatPadding：是否使用 CompadPadding。
* CardView_cardPreventComerOverlap：是否使用 PreventCornerOverlap。
* CardView_contentPadding；内容的 padding。
* CardView_contentPaddingLeft：内容的左 padding。
* CardView_contentPaddingTop：内容的上 padding。
* CardView_contentPaddingRight：内容的右 padding。
* CardView_contentPaddingBottom：内容的底 padding。

#### 1.1.4 3 种 Notification
　　Notification 可以让我们在获得消息的时候，在状态栏，锁屏界面来现实相应的信息。

###### 1. 普通 Notification
　　普通 Notification 的创建。

###### 2. 折叠式 Notification
　　折叠式 Notification 是一种自定义视图的 Notification，用来显示长文本和一些自定义的布局场景。它的两种状态：一种是普通状态下的视图（如果不是自定义的话，和上面普通 Notification 的视图样式一样），另一种是展开状态下的视图。和普通 Notification 不同的是，需要自定义视图，而这个视图现实的进程和我们创建视图的进程不在一个进程，所以需要使用 RemoteViews。

###### 3. 悬挂式 Notification
　　悬挂式 Notification 是 Android 5.0 新增加的方式。和前两种显示方式不同的是，前两种需要下拉通知栏才能看到通知；而悬挂式 Notification 不需要下拉通知栏就直接显示出来悬挂在屏幕上方，并且焦点不变，仍在用户操作的界面，因此不会打断用户的操作。其过几秒就会自动消失。和前两种 Notification 不同的是，它需要调用 setFullScreenIntent 来将 Notification 变为悬挂式 Notification 。

###### 4. Notification 的显示等级
　　Android 5.0 加入了一种新的模式 Notification 的显示等级，共有以下 3 种。
* VISIBILITY_PUBLIC：任何情况都会显示通知。
* VISIBILITY_PRIVATE：只有在没有锁屏时会显示通知。
* VISIBILITY_SECRET：在 pin、password 等安全锁和没有锁屏的情况下才能显示通知。

　　设置非常简单，只要调用 setVisibility 方法就可以了。

#### 1.1.5 Toolbar 与 Palette
　　Toolbar 是应用内容的标准工具栏，可以说是 Actionbar 的升级版。这两者不是独立关系，要使用 Toolbar，还是得跟 Actionbar 有关系的。相比于 Actionbar，Toolbar 最明显的一点就是变得很自由，可随处放置，其具体使用方法和 Actionbar 很类似。

###### 1. 引入 Toolbar
　　引入 v7 支持包。

　　为了显示 Toolbar 控件，先要在 style 里把 Actionbar 去掉。

###### 2. 自定义 Toolbar
　　可以设置 Toolbar 的标题和图标以及 Menu Item 等属性。Menu Item 的设置和 Actionbar 类似。

###### 3. 添加 DrawerLayout 实现侧滑
　　使用 DrawerLayout 实现侧滑。

###### 4. Palette 的应用
　　Android 5.x 用 Palette 来提取颜色，从而让主题能够动态适应当前界面的色调，做到整个 App 颜色的基调和谐统一。Android 内置了几种提取色调的种类：
* Vibrant (充满活力的)
* Vibrant dark (充满活力的黑)
* Vibrant light (充满活力的亮)
* Muted (柔和的)
* Muted dark (柔和的黑)
* Muted light (柔和的亮)
　　要使用 Palette ，需要引用 'com.android.support:palette-v7:23.0.1'。这在之前已经配置过了，实现提取颜色非常容易，只要将 bitmap 传递给 Palette，调用 generate 即可。在 onGenerated 回调中得到图片的色调。

## 1.2 Android 6.0 新特性
　　6.0 新系统的整体设计风格依然保持扁平化的 Material Design 风格。Android 6.0 在对软件体验与运行性能上进行了大幅度的优化。据测试，Android 6.0 可使设备续航时间提升 30%。

#### 1.2.1 Android 6.0 主要新特性概述

###### 1. 应用权限管理
　　在 Android 6.0 中，应用许可提示可以自定义了。它允许对应用的权限进行高度管理，比如应用能否使用位置、相机、网络和通信录等，这些都开放给开发者和用户。此前的 Android 系统的应用权限管理只能靠第三方应用来实现，在 Android 6.0 中应用权限管理成为系统级的功能。

###### 2. Android Pay
　　Android Pay 是 Android 支付统一标准。Android 6.0 系统中集成了 Android Pay，其特性在于简洁、安全和可选性。它是一个开放性平台，用户可以选择谷歌的服务或者使用银行的 App 来使用它。Android Pay 支持 Android 4.4 以后的系统设备并且可以使用指纹来进行支付。

###### 3. 指纹支持
　　虽然很多厂商的 Android 手机实现了指纹的支持，但是这些手机都使用了非谷歌认证的技术。这一次谷歌提供的指纹识别支持，旨在统一指纹识别的技术方案。

###### 4. Doze 电量管理
　　Android 6.0 自带 Doze 电量管理功能。手机静止不动一段时间后，会进入 Doze 电量管理模式。谷歌表示，当屏幕处于关闭状态时，平均续航时间可提高 30%。

###### 5. App Links
　　Android 6.0 加强了软件间的关联，允许开发者将 App 和他们的 Web 域名关联。

###### 6. Now on Tap
　　在桌面或 App 的任意界面，长按 Home 键即可激活 Now on Tap，它会识别当前屏幕上的内容并创建 Now 卡片。

#### 1.2.2 运行时权限机制
　　在 Android 6.0 时，将不会在安装的时候授予权限；取而代之的是，App 不得不在运行时一个一个询问用户来授予权限。

###### 1. Android 6.0 之前版本的应对之策
　　Android 6.0 系统默认为 targetSdkVersion 小于 23 的应用授予了所申请的所有权限，所以如果你以前的 App 设置的 targetSdkVersion 低于 23，在运行时也不会崩溃。

###### 2. Normal Permissions 与 Dangerous Permission
　　Google 将权限分为两类，一类是 Normal Permissions，这类权限一般不涉及用户隐式，是无须用户进行授权的，比如手机振动、访问网络等，这些权限只需要在 AndroidManifest.xml 中简单声明就好，安装时就授权，无须每次使用时都检查权限，而且用户不能取消以上授权；另一类是 Dangerous Permission，一般会设计用户隐私，需要用户进行授权，比如读取 adcard、访问通信录等。

| Normal Permissions |
|--------|
| android.permission.ACCESS_LOCATION_EXTRA_COMMANDS |
| android.permission.ACCESS_NETWORK_STATE |
| android.permission.ACCESS_NOTIFICATION_POLICY |
| android.permission.ACCESS_WIFI_STATE |
| android.permission.ACCESS_WIMAX_STATE |
| android.permission.BLUETOOTH |
| android.permission.BLUETOOTH_ADMIN |
| android.permission.BROADCAST_STICKY |
| android.permission.CHANGE_NETWORK_STATE |
| android.permission.CHANGE_WIFI_MULTICAST_STATE |
| android.permission.CHANGE_WIFI_STATE |
| android.permission.KILL_BACKGROUND_PROCESSED |
| android.permission.MODIFY_AUDIO_SETTINGS |
| android.permission.NFC |
| android.permission.READ_SYNC_SETTINGS |
| android.permission.READ_SYNC_STATS |
| android.permission.RECEIVE_BOOT_COMPLETED |
| android.permission.REORDER_TASKS |
| android.permission.REQUEST_INSTALL_PACKAGES |
| android.permission.SET_TIME_ZONE |
| android.permission.SET_WALLPAPER |
| android.permission.SET_WALLPAPER_HINTS |
| android.permission.TRANSMIT_IR |
| android.permission.USE_FINGERPRINT |
| android.permission.VIBRATE |
| android.permission.WAKE_LOCK |
| android.permission.WRITE_SYNC_SETTINGS |
| com.android.alarm.permission.SET_ALARM |
| com.android.launcher.permission.INSTALL_SHORTCUT |
| com.android.launcher.permission.UNINSTALL_SHORTCUT |

　　Dangerous Permission

| Permission Group | Permissions |
|--------|--------|
| android.permission-group.CALENDAR | android.permission.READ_CALENDAR android.permission.WRITE_CALENDAR |
| android.permission-group.CAMERA | android.permission.CAMERA |
| android.permission-group.LOCATION | android.permission.ACCESS_FINE_LOCATION android.permission.ACCESS_COARSE_LOCATION|
| android.permission-group.PHONE| android.permission.READ_PHOTO_STATE android.permission.CALL_PHOTO android.permission.READ_CALL_LOG android.permission.WRITE_CALL_LOG com.android.voicemail.permission.ADD_VOICEMAIL android.permission.USE_SIP android.permission.PROCESS_OUTGOING_CALLS|
| android.permission-group.SENSORS | android.permission.BODY_SENSORS |
| android.permission-group.SMS | android.permission.SEND_SMS android.permission.RECEIVE_SMS android.permission.READ_SMS android.permission.RECEIVE_WAP_PUSH android.permission.RECEIVE_MMS android.permission.READ_CELL_BROADCASTS |
| android.permission-group.STORAGE | android.permission.READ_EXTERNAL_STORAGE android.permission.WRITE_EXTERNAL_STORAGE |

　　同一组的任何一个权限被授权了，其他权限也自动被授权。此外，对于申请时弹出的提示框上面的文本说明也是对整个权限组的说明，而不是单个权限的说明。

#### 3. 实现支持运行时权限
　　举例获取电话权限。

#### 4. 处理“不再询问”选项
　　如果用户选择了“不再询问”，那么每次我们调用需要访问该权限的 API 时都会失效，这显然不会带来好的用户体验，所以我们需要做的就是给用户一个友好的提示。这时候需要使用 shouldShowRequestPermissionRationale 方法，这个方法可以帮助开发者向用户解释权限的情况。如果用户选择了“不再询问”选项，则 shouldShowRequestPermissionRationale 方法会返回 false，这时候就可以弹出 AlertDialog 来提醒用户允许访问该权限的重要性。

#### 5. PermissionDispatcher 框架的使用
　　PermissionDispatcher 框架用来封装请求权限。
  　使用 PermissionDispatcher 的例子。

## 1.3 Android 7.0 新特性

#### 1.3.1 Android 7.0 主要新特性概述

###### 1. 多窗口模式
　　Android 7.0 中支持多窗口多任务处理，你只要在一个应用程序中长按 Overview 按钮，就能进入多窗口模式。在大屏幕设备中，同时打开两个应用程序窗口显然可以提升执行效率。

###### 2. Data Server
　　Android 7.0 中引入了 Data Server 模式，它是一种流量保护机制。启动 Data Server 模式时，系统将拦截后台的数据使用，并在可能的情况下减少前台运行应用使用的数据量。例如限制流媒体服务的码率，下调画质，以及减少缓存等。而通过白名单设置，用户可以让应用避免受到 Data Server 模式的影响。

###### 3. 改进的 Java 8 语言支持
　　Android 7.0 可以支持 Java 8 语言平台，使得 Android 的 Jack 编译器现在能够有助于减少系统的冗余代码、降低占用和运行时间。开发者可以直接用 Lambda 表达式。

###### 4. 自定义壁纸
　　在 Android 7.0 中，可以为主屏幕设置壁纸，为锁屏设置另外一张壁纸。

###### 5. 快速回复
　　Android 7.0 还支持通知栏直接回复的功能。值得注意的是，这个功能不仅仅限于即时通信应用，它还适用于诸如 Twitter 这样的社交应用。

###### 6. Daydream VR 支持
　　Android 7.0 内置谷歌的全新 VR 平台 Daydream。Daydream 是一个虚拟现实平台，由 Daydream 头盔、手柄和只能手机构成，支持 Daydream 的智能手机要满足一定的硬件要求。

###### 7. 后台省电
　　Android 7.0 在后台省电方面也做了不小的改进，屏幕关闭后所有的后台进程都将会被系统限制活动，使这些应用不会在后台中持续唤醒，从而达到省电的目的。此外，Project Svelte 功能也在持续地改善，这最大限度地减少了 Android 设备中系统和应用所占用的内存。

###### 8. 快速设置
　　下拉通知栏顶部可以展开快捷开关界面。在快捷开关界面右下角有个“编辑”（EDIT）按钮，点击之后即可自定义添加/删除快捷开关，或拖动进行排序。

###### 9. Unicode 9 支持和全新的 emoji 表情符号
　　Android 7.0 支持 Unicode 9，并且新增了大约 70 种 emoji 表情符号。这些表情符号大多数都是人形的，并且提供不同的肤色。

###### 10. Google Assistant
　　Google Assistant 号称融合了谷歌搜索的深度学习技术以及 Google Now 的个人信息学习技术，它能够分辨用户的自然语言，并具备练习上下文的理解能力。它能够按照你的谈话内容和习惯来调整自己的推荐建议，最终能够形成一种适合于用户本人的模式，为用户的日常生活提供帮助。

#### 1.3.2 多窗口模式

###### 1. 进入多窗口模式
　　进入多窗口模式有两种方式：
* 点击手机导航栏最右边的 Overview 按钮进入 Overview 列表，长按列表中的活动窗口并拖入到屏幕最上方的分屏显示区域。
* 打开一个程序，长按 Overview 按钮也可以进入多窗口模式。

###### 2. 多窗口模式的生命周期
　　长按进入多窗口模式时，生命周期方法：onPause() -> onStop() -> onDestroy() -> onCreate() -> onStart() -> onResumt() -> onPause()。经历了一个重新创建的过程，最终会停留在 onPause() 状态，当点击项目的窗口时，才会获取焦点进入 onResume 状态。

　　长按 Overview 按钮退出多窗口模式，生命周期方法：onstop() -> onDestory() -> onCreate() -> onStart() -> onResume() -> onPause() -> onResume() 。先经历了销毁的过程，然后是一个重新恢复的过程，最终会停留在 onResume 状态。

###### 3. 禁用多窗口模式
　　多窗口模式未必适用于所有应用。如果想要禁用多窗口模式，只需要在 AndroidManifest.xml 中加入如下属性即可：
```
<application
	...
	android:resizeableAcivity = "false"
	...
</application>
```
　　设置 `android:resizeableAcivity` 属性为 false ，当长按 Overview 按钮想要进入多窗口模式时，会发现项目无法进入多窗口模式，并会弹出 Toast 来提醒用户当前应用不支持多窗口模式。

## 1.4 本章小结