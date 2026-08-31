# ConstraintLayout

## [ConstraintLayout 概念布局]

ConstraintLayout 是一个相对布局，主要解决开发过程中过于复杂的页面层级嵌套过多的问题（层级过深会增加绘制界面需要的时间）。

ConstraintLayout 优势：

1. 较高的性能优势

2. 完美的屏幕适配

3. 可视化编辑

布局的使用：

1. app:layout_constraintxxx_toxxxOf（start/top/end/bottom/right/left）：基本方向约束

2. app:layout_constraintBaseline_toBaselineOf：基线对齐

3. app:layout_constraintCirclexxx：角度约束

4. app:layout_constraintxxx_bias=""(Horizontal/Vertical)：百分比偏移

5. android:layout_marginXXX/android:layout_paddingXXX：内外边距限制

6. app:layout_goneMarginXXX：当 view 隐藏的时候才会生效，生效外边距限制

7. android:minWidth/maxWidth/minHeight/maxHeight：尺寸限制

8. app:layout_constraintWidth_default/app:layout_constraintHeight_default
   
   取值有：spread|percent|wrap
   
   将 width或者height设置为0，使用这个属性。
   
   > spread：默认，占用所有的符合约束的控制，自身 view 的大小充满可以配置的剩余空间
   > 
   > parcent：按照父布局的百分比设置
   > 
   > warp：匹配内容大小但不超过约束限制
   
   其他控件宽度设置为 wrap_content，宽度适应内容大小，并且设置了 margin，但是文字内容多的时候显示的宽度会超过 margin 的设置值。可以用 app:layout_constrainedWidth/Height 设置强制约束.

9. app:layout_constraintDimensionRatio: 比例宽高
   
   对宽高设置比例，前提是至少有一个约束维度设为 0dp，这样比例才会生效。

10. app:layout_constraintHorizontal_chainStyle、layout_constraintVertical_chainStyle：链
    
    模式可选的值有：spread、packed、spread_inside
    
    > spread：默认，均分剩余空间
    > spread_inside：两侧的控件贴近两边，剩余的控件均分剩余空间
    > packed：所有控件贴紧居中
    
    链还支持 weight(权重)的配置，使用 layout_constraintHorizontal_weight 和 layout_constraintVertical_weight（chainstyle 与 weight 搭配使用）

## [ConstraintLayout Helper 辅助对象]

Helper 是 ConstraintLayout 里的特殊 View：通常不可见，用来辅助布局或控制一组 View。

它们都只能在 ConstraintLayout（或子类 MotionLayout）里使用。

- Guideline 参考线
  
  Guideline 是一条参考线，可以帮助开发者进行辅助定位，不会真正显示在布局中。

- Barrier 屏障
  
  Barrier 是形成一个屏障、障碍。控制 Barrier 在一些 view 的给定位置（top|bottom|left|right|start|end），然后其他 view 可以根据 Barrier 的位置进行约束。

- Group
  
  Group 的作用就是可以对一组控件同时隐藏或显示。

- Placeholder
  
  Placeholder 的作用就是占位，可以在布局中占好位置，通过 app:content="" 属性（或 setContent() 设置内容），来让某个空间移动到此占位符中。
  
  主要用途有：模块化布局、运行时动态换位。

- Layer
  
  Layer 是一个变换型 Helper：把一组 View 当成虚拟图层，对它们整体做旋转、平移、缩放，但不负责排版，也不增加 View 层级。
  
  典型场景：卡片整体反转、图标+标题一起弹出、一组控件统一 elevation/背景、MotionLayout 里整体位移。

## [ConstraintLayout VirtualLayout Helper]

在 ConstraintLayout 中，虚拟布局（Virtual Layouts）作为 virtual view group 的角色参与约束和布局中，但是它们并不会作为视图添加到视图层次结构中，而是仅仅引用其他视图来辅助它们在布局系统中完成各自的布局功能。

* Flow
  
  Flow 是 ConstraintLayout 2.0 引入的 VirtualLayout Help，用来把一组 View 按水平或垂直方向排列，并在空间不够时自动换行/换列。
  
  Flow 只能在 ConstraintLayout 中使用。
  
  app:flow_wrapMode 设置换行模式：
  
  * none(默认)：不换行，所有 View 排成一条 Chain
  
  * chain：空间不够时换行，每行/列是一个 Chain
  
  * aligned：换行，并且像表格一样行列对齐
  
  app:flow_verticalAlign和app:flow_horizontalAlign 设置对齐方式。
  
  app:flow_maxElementsWrap 控制每行最大的子 View 数量。当 flow_wrapMode 属性为 aligned 和 chain 时。

* Grid
  Grid 是固定行列网络。
  典型场景：计算器键盘、九宫格、表单网格、图标矩阵

* CircularFlow
  CircularFlow 把一组 View 绕中心 View 排成圆形/环形。
  典型场景：运行菜单/FAB 展开菜单、轮播图周围指示器、头像周围一圈 icon、配合 MotionLayout 做旋转轮播。

## ConstraintLayout 功能增强组件

在基础 Helper 之上，扩展复杂布局、动画、多状态能力。

* MotionLayout
  
  MotionLayout 是 ConstraintLayout 2.0 的布局容器，ConstraintLayout 的子类，用来在多个约束状态之间做过渡动画。
  
  * MotionLayout 专用 Helper
    
    - Carousel
    
    - MotionEffect
    
    - MotionPlaceholder

* ConstraintLayoutStates

* SharedValues

## ConstraintLayout 动画/编程系统

多数不是 View，而是 XML 资源或描述类，配合 MotionLayout 或 ConstraintLayout 使用。

* MotionScene

* ConstraintSet

* Transition

* KeyFrameSet

* ConstrantLayoutState

## ConstraintLayout 编程式 API

代码里操作约束和动画，本身不是布局控件

* ConstraintSet

* ConstraintProperties

* TransitionManager

* SharedValues

* 自定义 ConstraintHelper

## ConstraintLayout 工具型增强组件

* ImageFilterButton & ImageFilterView
* MotionLabel
* MotionButton
* MockView
* MotionTelltales
