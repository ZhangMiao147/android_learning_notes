# ConstraintLayout 的 helper

![](image/ConstraintLayout.png)

> 功能增强组件：
> 
> 1. MotionLayout
> 
> 2. ConstraintLayoutStates
> 
> 3. SharedValues

## 一、功能增强组件

### 7.ImageFilterButton & ImageFilterView

项目引入了 ConstraintLayout 依赖，就可以在任何布局使用它们，并不仅仅限制使用在 ConstraintLayout 中。

ImageFilterButton 和 ImageFilterView 是两个控件，他们之间的关系就和 ImageButton 与 ImageView 是一样的，所以这里就只拿 ImageFilterView 来做讲解。从名字上来看，它们的定位是和过滤有关系的，它们的大致作用有两部分，一是可以用来做圆角图片，二是可以叠加图片资源进行混合过滤，下面一一展示：

#### 7.1. 圆角图片

ImageFilterButton 和 ImageFilterView 可以使用两个属性来设置图片资源的圆角，分别是 roundPercent 和 round，roundPercent 接受的值类型是 0-1 的小数，根据数值的大小会使图片在方形和圆形之间按比例过渡，round=可以设置具体圆角的大小，在使用的过程中发现 AndroidStudio 没有这两个属性的代码提示，也没有预览效果，但是运行起来是由效果的，可能是没有做好优化吧。

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1BSVf3HK2c36chH9UGjicvdVaNzO6Zia7JrMWhs7EkU6gtOhbviaGANwNQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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

    <androidx.constraintlayout.utils.widget.ImageFilterView
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@drawable/mi"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:roundPercent="0.7" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

可以看到使用 rounfPercent 设置了圆角为 0.7(70%)，实现一个圆角图片就是如此简单。

#### 7.2. 图片过滤

ImageFilterButton 和 ImageFilterView 不但可以使用 src 来设置图片资源，还可以使用 altSrc 来设置第二个图片资源，altSrc 提供的资源将会和 src 提供的资源通过 crossfade 属性形成交叉淡化效果，默认情况下，crossfade=0，altSrc 所引用的资源不可见，取值在 0-1。下面看例子：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1zf3O5iaxicZ0RfCSmjubnK6VgCSbSEzpBaOhtAeOWjeML4zhX7LUTXQg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1AA1YXkY5QpjQKPPvto1zoh9lavwGSH7QjgAMMVjWtDL5mRAvWHfgMQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1iaBQES9ckiav6U8bFXsH9UTHibGv0wRhlQxKUrlzuCoAnYcOqibKqj6L8g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

除此之外，warmth 属性可以用来调节色温，brightness 属性用来调节亮度，seturation 属性用来调节饱和度，constrast 属性用来调节对比度，下面展示一下各自属性和取值的效果：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1NMTMThsoaIWvRiblEicjfwIb2mmLqI2lo0ztXZzQJ738fedqGhockRWA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 8.MockView

可以使用 MockView 来充当原型图，下面看例子：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1ickR1s4GXfkdV5rdCeS5HaDM7EpATiaXSnok2sG5GQ6LnQShDETkamBg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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

    <androidx.constraintlayout.utils.widget.MockView
        android:id="@+id/Avatar"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_marginStart="80dp"
        android:layout_marginTop="100dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.constraintlayout.utils.widget.MockView
        android:id="@+id/Name"
        android:layout_width="100dp"
        android:layout_height="30dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/Avatar"
        app:layout_constraintTop_toTopOf="@id/Avatar" />

    <androidx.constraintlayout.utils.widget.MockView
        android:id="@+id/Age"
        android:layout_width="100dp"
        android:layout_height="30dp"
        app:layout_constraintBottom_toBottomOf="@id/Avatar"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/Avatar" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

## 四、ConstraintProperties(流式API)

2.0 提供了 ConstraintProperties 可以使用流式 API 修改属性

```kotlin
val properties = ConstraintProperties(findViewById(R.id.image))
    properties.translationZ(32f)
          .margin(ConstraintSet.START, 43)
          .apply()
```

## 四、MotionLayout

MotionLayout 是 Constraint Layout 2.0 中最令人期待的功能之一。它提供了一个丰富的动画系统来协调多个视图之间的动画效果。MotionLayout 基于 ConstraintLayout，并在其之上进行了扩展，允许您在多组约束（或者 ConstraintSets）之间进行动画的处理。您可以对视图的移动、滚动、缩放、旋转、淡入淡出等一系列动画行为进行自定义，甚至可以定义各个动画半身的自定义属性。它还可以处理手势操作所产生的物理移动效果，以及控制动画的速度。使用 MotionLayout 构建的动画是可追溯且可逆的，这意味着您可以随意切换到动画过程中人一一个点，甚至可以倒着执行动画效果。Android Studio 继承了 Motion Editor（动作编辑器），可以利用它来操作 MotionLayout 对动画进行生成、预览和编辑等操作。这样一来，在协调多个视图的动画时，就可以做到对各个细节进行精细操控。

# 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)
