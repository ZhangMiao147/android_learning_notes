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
