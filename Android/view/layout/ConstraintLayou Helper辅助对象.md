# ConstraintLayout Helper 辅助对象

![](image/ConstraintLayout.png)

> Helper 辅助对象：
> 
> 1. Guideline 参考线
>    
>    Guideline 是一条参考线，可以帮助开发者进行辅助定位，不会真正显示在布局中。
>    
>    Guideline 只能在 ConstraintLayout 中使用。
> 
> 2. Barrier 屏障
>    
>    Barrier 是形成一个屏障、障碍。控制 Barrier 在一些 view 的给定位置（top|bottom|left|right|start|end），然后其他 view 可以根据 Barrier 的位置进行约束。
>    
>    Barrier 只能在 ConstraintLayout 中使用。
> 
> 3. Group
>    
>    Group 的作用就是可以对一组控件同时隐藏或显示。
>    
>    Group 只能在 ConstraintLayout 中使用。
> 
> 4. Placeholder
>    
>    Placeholder 的作用就是占位，可以在布局中占好位置，通过 app:content="" 属性（或 setContent() 设置内容），来让某个空间移动到此占位符中。
>    
>    主要用途有：模块化布局、运行时动态换位
>    
>    Placeholder 只能在 ConstraintLayout 中使用。
> 
> 5. Layer
>    
>    Layer 是一个变换型 Helper：把一组 View 当成虚拟图层，对它们整体做旋转、平移、缩放，但不负责排版，也不增加 View 层级。
>    
>    典型场景：卡片整体反转、图标+标题一起弹出、一组控件统一 elevation/背景、MotionLayout 里整体位移。

## Helper 辅助对象

ConstraintLayout 的 Helper 是一类辅助布局的特殊 View：运行时通常不可见（或仅设计时可见），用来管理、排列或动画一组子 View。它们都只能在 ConstraintLayout（或子类 MotionLayout）里使用。

Guideline、Placeholder 直接继承 View，不继承 constraintHelper，但是仍把它们算作 Helper 对象。

* 基础 Helper （布局辅助）
  
  | Helper      | 包名                                      | 版本  | 作用                                      |
  | ----------- | --------------------------------------- | --- | --------------------------------------- |
  | Guideline   | androidx.constraintlayout.widget        | 1.0 | invisible 参考线，按 dp 或百分比定位，供其他 View 约束对齐 |
  | Barrier     | androidx.constraintlayout.widget        | 1.1 | 根据一组 View 的最远边缘形成动态屏障，可约束到【最长/最高】那一侧    |
  | Group       | androidx.constraintlayout.widget        | 1.1 | 批量控制一组 View 的 visibility/elevation      |
  | Placeholder | androidx.constraintlayout.widget        | 1.1 | [槽位]，把指定 View 显示在槽位位置（模版布局、运行时换位）       |
  | Layer       | androidx.constraintlayout.helper.widget | 2.0 | 把一组 View 当作整理平移、旋转、缩放（绕共同中心）            |

* VirtualLayout （虚拟布局）
  
  都通过 app:constraint_referenced_ids 引用子 View，由 Helper 负责排列。
  
  | Helper       | 版本   | 作用                                |
  | ------------ | ---- | --------------------------------- |
  | Flow         | 2.0  | 水平/垂直流式排列，空间不够时自动换行（类似 Flex wrap） |
  | Grid         | 2.1+ | 按行列排除网格，可设行数、列数、间距等               |
  | CircularFlow | 2.1+ | 把 View 排成圆形/环形（适合轮播、圆形菜单）         |

* MotionLayout 专用 Helper
  
  需在 MotionLayout 中使用（MotionLayouts 是 ConstraintLayout 的子类）。
  
  | Helper            | 作用                                      |
  | ----------------- | --------------------------------------- |
  | Carousel          | 在 MotionLayout 里实现类似 RecyclerView 轮播的效果 |
  | MotionEffect      | 自动为指定方向的 View 插入运动关键帧（飞入/飞出等）           |
  | MotionPlaceholder | MotionLayout 场景下的 Placeholder，配合过渡动画    |

与 ViewGroup 的区别：

* 多个 Helper 可以同时引用同一个 View

* Helper 不是容器，不会增加额外 View 层级

* 被引用 View 仍是 ConstraintLayout 的直接子 View

### 1. Guideline（参考线）

Guideline 是一条参考线，可以帮助开发者进行辅助定位，并且实际上它并不会真正显示在布局中，像是数据几何中的辅助线一样，使用起来非常方便，出场率很高，Guideline 也可以用来做一些百分比分割之类的需求，有着很好的屏幕适配效果，Guideline 有水平和垂直方向之分，位置可以使用针对父级的百分比或者针对父级位置的距离。

Guideline 只能在 ConstraintLayout 中使用。

```xml
android:orientation="horizontal|vertical"  辅助线的对齐方式
app:layout_constraintGuide_percent="0-1"   距离父级宽度或高度的百分比(小数形式)
app:layout_constraintGuide_begin=""        距离父级起始位置的距离(左侧或顶部)
app:layout_constraintGuide_end=""          距离父级结束位置的距离(右侧或底部)
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1b44LZFQZGdfoOQGX7mI9JQhmg6e219ymbNbDMT5Z7n8KXpoWyheAKQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity"
    tools:ignore="HardcodedText">

    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/Guideline"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        app:layout_constraintGuide_percent="0.5" />

    <TextView
        android:id="@+id/A"
        android:layout_width="120dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="@id/Guideline" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

上图中设置了一条水平方向位置在父级垂直方向 0.5(50%) 的 Guideline，控件 A 的顶部依赖于 Guideline，这样无论布局如何更改，Guideline 的位置始终都会是父级垂直方向 50% 的位置，控件 A 的位置也不会偏离预设。

### 2. Barrier（屏障）

这个 Barrier 和 Guideline 一样，也不会实际出现在布局中，它的作用如同其名，形成一个屏蔽、障碍，使用也非常多。

当创建布局时，有时会遇到布局可以根据本地化而更改的情况。这里借助有一个非常简单的例子：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT11fP48nZicMEZxyGyk8M6HXhyCQDbicJqhJ0icFhGMdxsYicGVubTdbPsZw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

这里有三个文本视图：左边的 textView1 和 textView2；右边的 textView3。textView3 被限制在 textView1 的末尾，这工作得很好 -- 它完全根据我们需要来定位和大小 textView3。

然而，如果我们需要支持多种语言，事情会变得更加复杂。如果我们添加德语翻译，那么我们就会遇到一个问题，因为在英文版本中，textView1 中的文本比 textView2 中的文本长，而在德语中，textView2 的文本比 textView2 长：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1ISGpqQtv3FvrvXxflVc6d6Ericx6dVkCsnEwn6lNepoicdfpn88VJ1cg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

这样的问题在于 textView3 仍然是相对于 textView1 的，所以 textView2 直接插入了 textView3 中。在设计视图里看起来更明显（白色背景的那个）。比较直接的解决办法是使用 TabLayout，或者把 textView1 & textView2 包裹在一个垂直的，android:layout_width="wrap_content" 的 LinearLayout 中。然后让 textView3 约束在这个 LinearLayout 的后面。但是有更好的办法：Barriers。Barriers 的配置属性如下：

```xml
<!--  用于控制Barrier相对于给定的View的位置  -->
app:barrierDirection="top|bottom|left|right|start|end"  

<!--  取值是要依赖的控件的id，Barrier将会使用ids中最大的一个的宽/高作为自己的位置  -->
app:constraint_referenced_ids="id,id"
```

修改过后的代码如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/textView1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="16dp"
        android:text="@string/warehouse"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/textView2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="8dp"
        android:text="@string/hospital"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textView1" />

    <androidx.constraintlayout.widget.Barrier
        android:id="@+id/barrier7"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:barrierDirection="end"
        app:constraint_referenced_ids="textView2,textView1" />

    <TextView
        android:id="@+id/textView3"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:text="@string/lorem_ipsum"
        app:layout_constraintStart_toEndOf="@+id/barrier7"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1qTMhA3fD6IWa0hIZbKda3v0raVfHGAZ6JLGjTVuPK0rU1o6GRiba1lg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

为了看到整体的效果，可以切换语言，此时你会看到 Barrier 会自动位于较宽的那个 textView 后面，也就间接让 textView3 也位于了正确的位置。

### 3. Group（组）

工作当中常常会有很多个控件同时隐藏或者显示的场景，传统做法要么是进行嵌套，对父布局进行隐藏或显示，要么就是一个一个设置，这显然都不是很好的办法，ConstraintLayout 中的 Group 就是来解决这个问题的。Group 的作用就是可以对一组控件同时隐藏或显示，没有其他的作用，它的属性如下：

```xml
app:constraint_referenced_ids="id,id"  加入组的控件id
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1nWJnzAbSeU1j9TV3u7lX6jic7LdHfOiaoSPzZ10nibe4oztpor27VsibrA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity"
    tools:ignore="HardcodedText">

    <TextView
        android:id="@+id/A"
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:layout_marginTop="56dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.115"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    <TextView
        android:id="@+id/B"
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:layout_marginTop="280dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="B"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.758"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/C"
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:layout_marginTop="164dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="C"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.437"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    <androidx.constraintlayout.widget.Group
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="visible"
        app:constraint_referenced_ids="A,B,C" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

A、B、C 三个 view，受 Group 控件，当 Group 的 visibility 为 visible 时，它们搜是正常显示的，设置为 gone 时，它们都会隐藏：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT152FEXHKRu7ZDkiagCKIxW9nU2K4ojl0be4QANJUBpwbtj2oqFeB4yug/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 4. Placeholder（占位符）

Placeholder 的作用就是占位，它可以在布局中占好位置，通过 app:content="" 属性，或者动态调用 setContent() 设置内容，来让某个控件移动到此占位符中。

Placeholder 只能在 ConstraintLayout 中使用。

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1CNtEZ4aqsymkfHHiaFxyhJXkw28rCZrdp3ZhUtjubFpHsOKmGc4tbuw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity"
    tools:ignore="HardcodedText">

    <TextView
        android:id="@+id/A"
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.constraintlayout.widget.Placeholder
        android:layout_width="100dp"
        android:layout_height="60dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

当我们设置 app:content="@+id/A" 或者调用 setContent() 时，控件 A 就会被移动到 placeholder 中，当然在布局中使用 app:content="" 显然就失去了它的作用。

Placeholder 能做什么？

| 场景      | 说明                                                    |
| ------- | ----------------------------------------------------- |
| 模块化布局   | 用 <merge> + 多个 placeholder 定义“槽位”，内容 View 只声明 id，不写约束 |
| 运行时动态换位 | placeholder.setContentId(view.getId()) 把 View 移到不同槽位  |

Placeholder 是一个“槽位”：

1. 槽位本身用约束定义位置和大小

2. 通过 app:content="@id/xxx" 或者 setContentId() 绑定某个 View

3. 绑定后，该 View 在原位置视为 Gone，显示在槽位里

4. 调用 TransitionManager.beginDelayedTransition() 后再改 contentId，位置变化会自动动画。

**模块化布局**

适合：竖屏/横屏布局结构不同，但空间是同一批。

思路：控件只声明 id 和属性，不写约束；约束写在 <merge> 模版中，用 Placeholder 占位。

1. 主布局 - 只放控件
   
   res/layout/activity_detail.xml
   
   ```xml
   <androidx.constraintlayout.widget.ConstraintLayout
       xmlns:android="http://schemas.android.com/apk/res/android"
       android:id="@+id/root"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <!-- 控件不写约束，只声明 id -->
       <ImageView
           android:id="@+id/cover_image"
           android:layout_width="200dp"
           android:layout_height="200dp"
           android:scaleType="centerCrop"
           android:src="@drawable/cover" />
   
       <TextView
           android:id="@+id/title"
           android:layout_width="wrap_content"
           android:layout_height="wrap_content"
           android:text="标题"
           android:textSize="20sp" />
   
       <Button
           android:id="@+id/action_btn"
           android:layout_width="wrap_content"
           android:layout_height="wrap_content"
           android:text="操作" />
   
       <!-- 引入竖屏模板 -->
       <include layout="@layout/template_detail_portrait" />
   
   </androidx.constraintlayout.widget.ConstraintLayout>
   ```
   
   2. 竖屏布局 - Placeholder 定义槽位
      
      res/layout/template_detail_portrait.xml
      
      ```xml
      <merge
          xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:app="http://schemas.android.com/apk/res-auto">
      
          <!-- 封面：顶部居中 -->
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="0dp"
              android:layout_height="200dp"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintEnd_toEndOf="parent"
              app:content="@id/cover_image" />
      
          <!-- 标题：封面下方 -->
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="0dp"
              android:layout_height="wrap_content"
              android:layout_marginTop="16dp"
              app:layout_constraintTop_toBottomOf="@id/cover_image"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintEnd_toEndOf="parent"
              app:content="@id/title" />
      
          <!-- 按钮：底部 -->
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:layout_marginBottom="24dp"
              app:layout_constraintBottom_toBottomOf="parent"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintEnd_toEndOf="parent"
              app:content="@id/action_btn" />
      
      </merge>
      ```
   
   3. 横屏模版 - 同一批控件，不同槽位
      
      res/layout-land/template_detail_portrait.xml（同名文件，横竖屏自动选用）
      
      ```xml
      <merge
          xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:app="http://schemas.android.com/apk/res-auto">
      
          <!-- 封面：左侧 -->
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="0dp"
              android:layout_height="0dp"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintBottom_toBottomOf="parent"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintWidth_percent="0.4"
              app:content="@id/cover_image" />
      
          <!-- 标题 + 按钮：右侧 -->
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="0dp"
              android:layout_height="wrap_content"
              android:layout_marginStart="16dp"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintStart_toEndOf="@id/cover_image"
              app:layout_constraintEnd_toEndOf="parent"
              app:content="@id/title" />
      
          <androidx.constraintlayout.widget.Placeholder
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              app:layout_constraintTop_toBottomOf="@id/title"
              app:layout_constraintStart_toStartOf="@id/title"
              app:content="@id/action_btn" />
      
      </merge>
      ```

如果整页布局大改、多 View 一起动，更适合 ConstraintSet+TransitionManager，不一定用 Placeholder。

**运行时动态换位**

配合动画 = 运行时动态换位+TransitionManager

只有运行时动态换位（无动画）：View 会瞬间跳到槽位

```kotlin
previewSlot.setContentId(thumb2.id)
```

运行时动态换位+配合动画：View 会平滑移动/缩放到槽位

```kotlin
TransitionManager.beginDelayedTransition(root) // 先声明“接下来布局变化要动画”
previewSlot.setContentId(thumb2.id) // 再触发换位
```

适合：运行时把 View 从一个槽位移到另一个槽位（例如选中放大、详情页切换）

1. 配合动画
   
   1. 布局 - 多个槽位 + 可移动 View
      
      res/layout/activity_gallery.xml
      
      ```xml
      <androidx.constraintlayout.widget.ConstraintLayout
          xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:app="http://schemas.android.com/apk/res-auto"
          android:id="@+id/root"
          android:layout_width="match_parent"
          android:layout_height="match_parent">
      
          <!-- 三个缩略图，各自有约束 -->
          <ImageView
              android:id="@+id/thumb1"
              android:layout_width="64dp"
              android:layout_height="64dp"
              android:src="@drawable/img1"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintEnd_toStartOf="@id/thumb2" />
      
          <ImageView
              android:id="@+id/thumb2"
              android:layout_width="64dp"
              android:layout_height="64dp"
              android:src="@drawable/img2"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintStart_toEndOf="@id/thumb1"
              app:layout_constraintEnd_toStartOf="@id/thumb3" />
      
          <ImageView
              android:id="@+id/thumb3"
              android:layout_width="64dp"
              android:layout_height="64dp"
              android:src="@drawable/img3"
              app:layout_constraintTop_toTopOf="parent"
              app:layout_constraintStart_toEndOf="@id/thumb2"
              app:layout_constraintEnd_toEndOf="parent" />
      
          <!-- 底部预览槽位 -->
          <androidx.constraintlayout.widget.Placeholder
              android:id="@+id/preview_slot"
              android:layout_width="120dp"
              android:layout_height="120dp"
              android:layout_marginBottom="32dp"
              app:layout_constraintBottom_toBottomOf="parent"
              app:layout_constraintStart_toStartOf="parent"
              app:layout_constraintEnd_toEndOf="parent" />
      
      </androidx.constraintlayout.widget.ConstraintLayout>
      ```

2. Kotlin - 点击缩略图，动画移入槽位
   
   ```kotlin
   class GalleryActivity : AppCompatActivity() {
   
       private lateinit var root: ConstraintLayout
       private lateinit var previewSlot: Placeholder
   
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           setContentView(R.layout.activity_gallery)
   
           root = findViewById(R.id.root)
           previewSlot = findViewById(R.id.preview_slot)
   
           val onThumbClick = View.OnClickListener { view ->
               // 关键：先开启动画，再改 contentId
               TransitionManager.beginDelayedTransition(root)
               previewSlot.setContentId(view.id)
           }
   
           findViewById<View>(R.id.thumb1).setOnClickListener(onThumbClick)
           findViewById<View>(R.id.thumb2).setOnClickListener(onThumbClick)
           findViewById<View>(R.id.thumb3).setOnClickListener(onThumbClick)
       }
   }
   ```

3. 在两个槽位之间来回切换
   
   setContentId() 不会保存 View 原来的约束，所以不能简单 setContentId(-1) 就还原。常见做法是用两个 Placeholder 轮流承载：
   
   ```kotlin
   private var isTop = true
   
   fun toggle(view: View) {
       TransitionManager.beginDelayedTransition(root)
   
       if (isTop) {
           placeholderTop.setContentId(view.id)
           placeholderBottom.setContentId(-1)  // 清空另一个槽位
       } else {
           placeholderBottom.setContentId(view.id)
           placeholderTop.setContentId(-1)
       }
       isTop = !isTop
   }
   ```

大范围布局变化更适合 ConstraintSet + TransitionManager，Placeholder 适合 “把已有 View 挪到指定槽位”。

和 ConstraintSet 怎么选

| 场景                | 推荐                                     |
| ----------------- | -------------------------------------- |
| 竖屏/横屏同一批控件，结构不同   | Placeholder+<merge>模版                  |
| 点击后把某个 View 移到预览区 | Placeholder+TransitionManager          |
| 整夜布局大改（如详情页展开）    | ConstraintSet 加载两套约束+TransitionManager |
| 精细关键帧动画           | MotionLayout                           |

### 5 .Layer（层布局）

Layer 是 ConstraintLayour 2.0 的变换型 Helper：把一组 View 当成虚拟图层，对它们整体做旋转、平移、缩放，但不负责排版，也不增加 View 层级。

以前要对【图片+文字】一起旋转/缩放，通常会再包一层 FrameLayout/LinearLayout，再对容器做动画。代价是：

- 多一层 View 层级

- 内部 View 很难再和外面的兄弟 View 互相约束

Layer 的做法：子 View 仍是 ConstraintLayout 的直接子节点，各自约束照旧；用 constraint_referenced_ids 声明【哪些算同一层】，之后对 Layer 做 rotation/translation/scale 即可。

典型场景：卡片整体反转、图标+标题一起弹出、一组控件统一 elevation/背景、MotionLayout 里整体位移。

Layer 继承自 ConstraintHelper，是一个约束助手，相对于 Flow 来说，Layer 的使用较为简单，常用来增加背景，或者共同动画。

**使用注意：**

1. 只能放在 ConstraintLayout（或 MotionLayout）里，和其他 Helper 一样。

2. constraint_referenced_ids 只能写真实控件 id，不要写 Group/Barrier/Flow/Layer 自己的 id。

3. 应用的 id 必须存在，写错 id 会走到 Resources.getIdentifier()，严重时布局 inflate 会很慢甚至 ANR。

4. Layer 的 visibility 会覆盖被引用 View 的可见性。只想做动画、不想管显隐时，保持 Layer 为 VISIBLE，用 Group 单独管显隐。

5. 子 View 的约束仍然生效，Layer 不会替代 Chain/Barrier 去对齐。

**和 Group 的区别**

Group 只管显隐，Layer 管整组变换。两者都是 Helper，都不增加 View 层级，都用 constraint_referenced_ids 引用一组控件。

Group 布局结束后宽高会被收成 0，自己不可见、不占位。

Layer 会按引用 View 算出一个包围盒，所以能画背景、做整体动画。

1. Layer 也能改 visibility。它继承了 ConstraintHelper 的显隐同步。只做动画，让 Layer 保持 VISIBLE，显隐交给 Group，避免两套互相覆盖。

2. 不要用 Group 当容器做动画。它没有包围盒，变换不会作用到子 View。

3. 两者都可以应用同一批 View，但显隐以最后一次 applyLayoutFeatures 为准，不要混着用两套 visibility。

4. constraint_references_ids 只能写真实控件 id，不要写另一个 Group/Layer/Barrier/Flow 的 id。

图层（Layer）在布局期间会调整大小，其大小会根据其引用的所有视图进行调整，代码的先后顺序也会决定着它的位置，如果代码在所有引用 view 最后面，那么它就会在所有 view 的最上面，反之则是最下面，在最上面的时候如果添加背景，就会把引用的 view 覆盖掉，下面展示下添加背景的例子

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1BKNg2Vx0wyuausu9MKB7ctKOPwtJOQRKFRMEzsZ4w1Qy5EzEx5Ek5Q/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity"
    tools:ignore="HardcodedText">

    <androidx.constraintlayout.helper.widget.Layer
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/common_rect_white_100_10"
        android:padding="10dp"
        app:constraint_referenced_ids="AndroidImg,NameTv" />

    <ImageView
        android:id="@+id/AndroidImg"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:src="@drawable/android"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/NameTv"
        android:layout_width="100dp"
        android:layout_height="40dp"
        android:gravity="center"
        android:text="Android"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="@id/AndroidImg"
        app:layout_constraintStart_toStartOf="@id/AndroidImg"
        app:layout_constraintTop_toBottomOf="@id/AndroidImg" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

可以看到，当 Layer 的代码在所有引用 view 的上面时，效果是正常的，因为此时所有的 view 都在 Layer 的上面，下面来看一下 Layer 代码在最后面时的情况：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1r78qBej2MFueyJh51A42Bryb5WzJbSw4g6IGYeGw0WmXceYc0ib3jkA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity"
    tools:ignore="HardcodedText">

    <ImageView
        android:id="@+id/AndroidImg"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:src="@drawable/android"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/NameTv"
        android:layout_width="100dp"
        android:layout_height="40dp"
        android:gravity="center"
        android:text="Android"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="@id/AndroidImg"
        app:layout_constraintStart_toStartOf="@id/AndroidImg"
        app:layout_constraintTop_toBottomOf="@id/AndroidImg" />

    <androidx.constraintlayout.helper.widget.Layer
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/common_rect_white_100_10"
        android:padding="10dp"
        app:constraint_referenced_ids="AndroidImg,NameTv" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

可以看到，此时 Layer 已经把所有的 view 覆盖住了。

# 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)
