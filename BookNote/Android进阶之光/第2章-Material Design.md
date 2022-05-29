# 第 2 章 Material Design
　　Material Design(质感设计)是由 Google 退出的设计语言，旨在为手机、平板电脑等平台提供一致的设计和体验。

## 2.1 Material Design 概述
　　Material Design 的官方英文文档：http://www.google.com/design/spec/material-design/。

#### 2.1.1 核心思想
　　Material Design 的核心思想，就是将物理世界中的体验带入屏幕，并且去掉物理世界中的杂质，再配合虚拟世界的灵活特性，达到最贴近真实的体验。到目前为止可以说，Material Design 是最重视跨平台体验的一套设计语言。它的规范严格细致，这样就保证了其在各个平台的使用体验高度一致。

#### 2.1.2 材质与空间

魔法纸片是 Material Design 中最重要的信息载体。它拥有现实中的厚度、惯性和反馈，并且能够自由伸展变形。魔法纸片引入了 Z 轴的概念，Z 轴垂直于屏幕，用来表现元素的层叠关系。Z 值越高，元素离界面底层的距离越远，投影就越重。

#### 2.1.3 动画


###### 1. 真实的动作

在 Material Design 设计规范中，动作不仅仅要展示物体的运动轨迹，还需要展示其在空间中的关系、功能以及整个系统中的趋势。

###### 2.响应式交互

响应式交互能吸引很多用户，在用户操作一个既美观又符合常理的应用时，这会是一个很美好的体验，这会让用户产生愉悦感。响应式交互是一种有目的的、非随机的、有些异想天开但不会让人分心的交互。响应式交互分为 3 种交互形式，分别是表层响应、元素响应和径向响应。

* 表层响应 -- 当用户点击屏幕时，系统会立即在交互的触点上绘制一个可视化的图形让用户感知到：如在点击屏幕时会出现类似于墨水扩散那样的视觉效果形状。
* 元素响应 -- 元素本身也能做出交互响应，物体可以在触控或点击的时候浮起来，以表示该元素正处于激活可操作状态。比如我们长按一个应用图标时，这个应用图标会浮起来，我们可以拖动该应用图标完成位置更换或者卸载等操作。
* 径向响应 -- 所有的用户交互行为中都会有一个中心点，作为用户关注的中心点，当用户进行操作时应用绘制一个明显的视觉效果来让用户清晰地感知到自己的操作。

###### 3.转场动画

当一个界面跳转到另一个界面时，这一过程可以编排转场动画，这些动画不仅可以带来良好的视觉效果，更重要的是吸引用户的注意，比如我们可以利用转场动画引导用户做下一步的操作。转场动画不仅要提升用户体验的整体美感，还要为业务服务。

###### 4.细节动画

动画最基本的使用场景是过渡效果，但是最基本的动画，只要恰到好处并且足够出色，同样也能打动用户。


#### 2.1.4 样式

###### 1.色彩

色彩从当代建筑、路标、人行横道和运动场馆中获得灵感，采用了大胆的颜色表法，这与单调乏味的周边环境形成了鲜明的对比。其强调大胆的阴影和高光，从而产生了意想不到且充满活力的颜色。

###### 2.字体

自从 Android 4.0 发布以来，Roboto 一直都是 Android 系统的默认字体集。在 Material Design 中，为了适配更多的平台，将 Roboto 做了进一步的全面优化。宽度和圆度都又了轻微提高，从二提升了清晰度，并且看起来更令人赏心悦目。中文字体则选用 Noto。

###### 3.字体排版

一个优秀的布局不会使用过多的字体尺寸和样式。字体排版的缩放是包含了有限个字体尺寸的集合，它们能够良好地适应布局结构。


#### 2.1.5 图标

###### 1.桌面图标

桌面图标作为 App 的门面，传达了产品的核心理念和内涵。虽然每个产品的桌面图标都不相同，但对于一个特定的品牌，桌面图标应在理念和实践中统一设计。桌面图标建议模仿现实中的折纸效果，通过扁平色彩表现空间和光影，一般情况下请使用 48dpX48dp 的尺寸，必要时可以放大到 192dpX192dp 。

###### 2.小图标

优先使用 Material Design 默认图标。设计小图标时，使用最简练的图形来表达，图形不要带空间感。小图标尺寸是 24dpX24dp，图形限制在中央 20dpX20dp 区域内。

#### 2.1.6 图像

在 Material Design 中，图像都不应该是人为策划的，而是组建生成的，不要给用户一种过度制作的感觉。这种风格比较强调场景的实质性、质感和深度，让人意想不到的色彩运用，以及对环境背景的关注。这些原则都旨在创建目的性强、美观、具有深度的用户界面。

###### 布局

布局设计使用相同的视觉元素、结构网格和通用的行距规则，这样会使得 App 在不同平台与屏幕尺寸上都拥有一致的外观和体验。这样就创造了一个识别度高的跨平台产品的用户环境，这个环境会给用户提供高度的熟悉感和舒适性。

#### 2.1.7 组件

###### 1.底部动作条（Bottom Sheets）

底部动作条是一个从屏幕底部边缘向上滑出的面板，旨在向用户呈现一组功能。底部动作条呈现了简单、清晰的一组操作。

###### 2.卡片（Cards）

卡片是包含了一组特定数据集合的纸片，数据集合中包含了各种相关信息，例如，主题的照片、文本和链接。卡片通常用于展示一些基础和概述的信息，并且作为更详细信息的入口。卡片有固定的宽度和可变的高度，其最大高度限制于可适应平台上单一视图的内容：但如果需要它，可以临时扩展。

###### 3.提示框（Dialogs）

提示框用户向用户提示一些信息，或者是一些决定的选择。提示框可以采用一种“取消/确定”的简单应答模式，也可以采用自定义布局的复杂模式，比如说增加一些文本设置或是文本输入。

###### 4.菜单（Menus）

菜单能够影响到应用、视图或者视图中选中的按钮。它由按钮、动作、点或者包含至少两个菜单项的其他控件触发。每一个菜单项是一个选项或者是动作，菜单不应该用作应用中主要的导航方法。

###### 5.选择器

选择器提供了一个简单的方法来从一个预定义的集合中选取单个值。最常用的例子就是用户要输入日期的场景，这时使用时间选择器可以帮你保证用户指定的日期是正确格式化的。

###### 6.滑块控件（Sliders）

当我们调节音乐音量或者音乐的播放进度时会使用滑块控件，它让我们在一个区间内滑动锚点来选择一个合适的数值。区间最小值放在左边，最大值放在右边。

###### 7.进度和动态

在用户于内容进行交互之前，为了给用户展现出更完整的、变化更少的界面，需要使用进度指示器，并且要尽量使加载的过程令人愉快。每次加载操作只能由一个进度指示器呈现，例如，对于刷新操作，你不能既用刷新条，又用动态圆圈来指示。

###### 8.Snackbar 和 Toast

Snackbar 是一种轻量级的弹出框，通常显示在屏幕的底部并在屏幕所有层的最上方，包括浮动操作按钮。它会在超时或者用户在屏幕其他地方触摸之后自动消失。Snackbar 可以在屏幕上滑动关闭，Snackbar 不会阻碍用户在屏幕上的输入，并且也不支持输入，屏幕上同时最多只能展示一个 Snackbar。Android 也提供了一种主要用户提示系统消息的胶囊状的提示框 Toast。Toast 同 Snackbar 非常相似，但是 Toast 并不包含操作，也不能从屏幕上滑动关闭。

###### 9.Tab

在一个 App 中，Tab 使得在不同的视图和功能间切换更加简单。Tab 用来显示有关联的分组内容，Tab 标签则用来简要地描述 Tab 的内容。

## 2.2 Design Support Library 常用控件详解

#### 2.2.1 Snackbar 的使用

为一个操作提供轻量级的、快速的反馈是使用 Snackbar 的最好时机。Snackbar 显示在屏幕的底部，包含了文字信息与一个可选的操作按钮。它在指定时间结束之后会自动消失。另外，配合 CooordinatorLayout 使用，还可以在超时之前将它滑动删除。

#### 2.2.2 用 TextInputLayout 实现登录界面

TextInputLayout 在输入时，设置的 hint 值不在原本的 EditText 中，而是浮在上方，当将焦点移动到其他框时，输入框上浮的文字回到原本的 EditText 中并伴有动画。

除了能更加友好地展示提示这一个优点之外，TextInputLayout 还有一个优点，那就是可以友好地显示错误信息。

#### 2.2.3 FloatingActionButton 的使用

它是一个负责显示用户基本操作的圆形按钮。

#### 2.2.4 用 TabLayout 实现类似网易选项卡的动态滑动效果

此前要实现类似网易选项卡的动态效果稍微有些麻烦，还需要动态地加载布局等技术，控制滑动的时候还需要用 HorizontalScrollView。这一次 Design Support Library 给我们带来了 TabLayout，故此可以很轻松地实现这一效果。

#### 2.2.5 用 NavigationView 实现抽屉菜单界面

Design Support Library 提供了 NavigationView 来帮助我们实现抽屉式菜单界面。和普通的侧拉菜单实现方式一样，所有的东西还是都放在一个 DrawerLayout 中，用 NavigationView 来替代我们此前自定义的控件。


#### 2.2.6 用 CoordinatorLayout 实现 Toolbar 隐藏和折叠

CoordinatorLayout 是 Andriod Design Support Library 中比较难的控件。顾名思义，它是用来组织其子 View 之间协作的一个父 View。CoordinatorLayout 默认情况下可被理解为一个 FrameLayout，它的布局方式默认是一层一层叠上去的。

###### 1. CoordinatorLayout 实现 Toolbar 隐藏效果

当点击 FloatingActionButton 弹出 Snackbar 的时候，为了给 Snackbar 留出空间，浮动的 FloatingActionButton 会向上移动。这是因为配合 CoordinatorLayout，FloatingActionButton 有一个默认的 Behavior 来检测 Snackbar 的添加并让按钮在 Snackbar 之上呈现上移与 Snackbar 等高的动画。

###### 2. CoordinatorLayout 结合 CollapsingToolbarLayout 实现 Toolbar 折叠效果

要实现折叠效果，需要引入一个新的布局 CollapsingToolbarLayout，其作用是提供一个可以折叠的 Toolbar。CollapsingToolbarLayout 继承自 FrameLayout。CollapsingToolbarLayout 设置 layout_scrollFlag 属性，可以控制包含在 CollapsingToolbarLayout 中的控件，比如 ImageView、Toolbar 在响应 layout_behavior 事件时做出相应的 scrollFlags 滚动事件。在布局文件中用 CollapsingToolbarLayout 将 ImageView 和 Toolbar 包含起来作为一个可折叠的 Toolbar，再用 AppBarLayout 包裹起来作为一个 Appbar 的整体。当然，AppBarLayout 目前必须是第一个嵌套在 CoordinatorLayout 里面的子 View。

CollapsingToolbarLayout 有几个关键属性需要说明一下：

* app:contentScrim=""，用来设置 CollapsingToolbarLayout 收缩后最顶层的颜色。
* app:expandedTitleGravity="left|bottom"，表示将此 CollapsingToolbarLayout 完全展开后，title 所处的位置，默认的值为 left+bottom。
* app:collapsedTitleGravity="left"，表示当头部的衬图 ImageView 消失后，此 title 将回归到 Toolbar 的位置，默认的值为 left。
* app:layout_scrollFlags=""，这个属性用来设置滚动事件，属性里面必须至少启用 scroll 这个 flag，这样这个 View 才会滚动出屏幕，否则它将一直固定在顶部。设置的是 app:layout_scrollFlags="scroll|exitUntilCollapsed"，这样能实现折叠效果。如果想要隐藏效果，可以设置 app:layout_scrollFlags="scroll|enterAlways"。

###### 3.自定义 Behavior

CoordinatorLayout 中最经典的设计就是 Behavior，app:layout_behavior="@string/appbar_scrolling_view_behavior"，其实@string/appbar_scrolling_view_behavior 对应着的是 AppBarLayout.ScrollingViewBehavior。也可以自定义 Behavior 来实现自己的组件和滑动交互。自定义 Behavior 可以分为两种方式：第一种是定义的 View 监听 CoordinatorLayout 里的滑动状态；第二种是定义的 View 监听另一个 View 的状态变化，例如 View 的大小、位置和显示状态等。对于第一种方法，需要注意 onStartNestedScroll() 和 onNestedPreScroll 方法了人对于第二种方法，则需要注意 layoutDependsOn() 和 onDependentViewChanged() 方法。


## 2.3 本章小结

本章主要介绍了 Material Design 的基础概念和 Android Design Support Library 库中主要控件的使用方法。