# ConstraintLayout概念与布局

![](image/ConstraintLayout.png)

>  **主要内容**
> 
> ConstraintLayout 是一个相对布局，主要解决开发过程中过于复杂的页面层级嵌套过多的问题（层级过深会增加绘制界面需要的时间）。
> 
> ConstraintLayout 优势：
> 
> 1. 较高的性能优势
> 
> 2. 完美的屏幕适配
> 
> 3. 可视化编辑
> 
> 布局的使用：
> 
> 1. app:layout_constraintxxx_toxxxOf（start/top/end/bottom/right/left）：基本方向约束
> 
> 2. app:layout_constraintBaseline_toBaselineOf：基线对齐
> 
> 3. app:layout_constraintCirclexxx：角度约束
> 
> 4. app:layout_constraintxxx_bias=""(Horizontal/Vertical)：百分比偏移
> 
> 5. android:layout_marginXXX/android:layout_paddingXXX：内外边距限制
> 
> 6. app:layout_goneMarginXXX：当 view 隐藏的时候才会生效，生效外边距限制
> 
> 7. android:minWidth/maxWidth/minHeight/maxHeight：尺寸限制
> 
> 8. app:layout_constraintWidth_default/app:layout_constraintHeight_default
>    
>    取值有：spread|percent|wrap
>    
>    将 width或者height设置为0，使用这个属性。
>    
>    > spread：默认，占用所有的符合约束的控制，自身 view 的大小充满可以配置的剩余空间
>    > 
>    > parcent：按照父布局的百分比设置
>    > 
>    > warp：匹配内容大小但不超过约束限制
>    
>    其他控件宽度设置为 wrap_content，宽度适应内容大小，并且设置了 margin，但是文字内容多的时候显示的宽度会超过 margin 的设置值。可以用 app:layout_constrainedWidth/Height 设置强制约束.
> 
> 9. app:layout_constraintDimensionRatio: 比例宽高
>    
>    对宽高设置比例，前提是至少有一个约束维度设为 0dp，这样比例才会生效。
> 
> 10. app:layout_constraintHorizontal_chainStyle、layout_constraintVertical_chainStyle：链
>     
>     模式可选的值有：spread、packed、spread_inside
>     
>     > spread：默认，均分剩余空间
>     > spread_inside：两侧的控件贴近两边，剩余的控件均分剩余空间
>     > packed：所有控件贴紧居中
>     
>     链还支持 weight(权重)的配置，使用 layout_constraintHorizontal_weight 和 layout_constraintVertical_weight（chainstyle 与 weight 搭配使用）

## 一、前言

ConstraintLayout 是一个使用 “相对定位”灵活地确定控件的位置的大小的一个布局，它的出现是为了解决开发中过于复杂的页面层级嵌套过多的问题 -- 层级过深会增加绘制界面需要的时间，影响用户体验，以灵活的方式定位和调整小部件。

ConstraintLayout 具有以下优势：

1. 较高的性能优势
   
   布局嵌套层次较高，性能开销较大。而使用 ConstraintLayout，经常就一层嵌套就搞定了，所以其性能要好很多。

2. 完美的屏幕适配
   
   ConstraintLayout 的大小、距离都可以用比例来设置，所以其适配性更好。

3. 书写简单

4. 可视化编辑
   
   ConstraintLayout 也有非常方便完善的可视化编辑器，不用写 xml 也基本上能实现大部分功能。但个人还是比较喜欢写 xml。

## 二、布局的使用

### 1. 位置约束

ConstraintLayout 采用方向约束的方法对控件进行定位，至少要保证水平和垂直方向都至少有一个约束才能确定控件的位置。

#### 1.1.基本方向约束

比如我们想实现这个位置，顶部和界面顶部对齐，左部和界面左部对齐：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1z9CiaVQC2VTXzoc0ZbK0cffkVFHpFNmfGTqOCgERNlc6h214WqEfmWQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#DAF3FE"
    tools:context=".MainActivity">

    <TextView
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:ignore="HardcodedText" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

核心代码就这两行：

```xml
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintTop_toTopOf="parent"
```

这两行代码的意思就是，控件的开始方向与父容器的开始方向对齐，控件的顶部方向与父容器的顶部方向对齐，其实 layout_constraintStart_toStartOf 也可以使用 layout_constraintLeft_toLeftOf，但是使用 start 和 end 来表示左和右是为了考虑别的国家的习惯，有的国家开始方向是右，所以使用 start 和 end 可以兼容这种情况。到这里就可以看到该控件使用 layout_constraintStart_toStartOf 和 layout_constraintaTop_toTopOf 两条约束确定了自己的位置。这里有一个使用技巧，就是，该控件的 ？？ 方向在哪个控件的 ?? 方向，记住这一点就可以了。那么下面就介绍下全部的约束属性：

```xml
<!-- 基本方向约束 -->
<!-- 我的什么位置在谁的什么位置 -->
app:layout_constraintTop_toTopOf=""           我的顶部和谁的顶部对齐
app:layout_constraintBottom_toBottomOf=""     我的底部和谁的底部对齐
app:layout_constraintLeft_toLeftOf=""         我的左边和谁的左边对齐
app:layout_constraintRight_toRightOf=""       我的右边和谁的右边对齐
app:layout_constraintStart_toStartOf=""       我的开始位置和谁的开始位置对齐
app:layout_constraintEnd_toEndOf=""           我的结束位置和谁的结束位置对齐

app:layout_constraintTop_toBottomOf=""        我的顶部位置在谁的底部位置
app:layout_constraintStart_toEndOf=""         我的开始位置在谁的结束为止
<!-- ...以此类推 -->
```

那么 ConstraintLayout 就是使用这些属性来确定控件的位置，虽然比较多，但是有个规律可循，没有任何记忆压力。

#### 1.2. 基线对齐

看一个场景：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1NTOg4vjWNLUAJol80nFia1anPQelEPyOnib9ebMY2cRQI4UY7twRuvgg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

有时候需要写这样的需求：两个文本是基线对齐的，那就可以用到我们的一个属性 layout_constraintBaseline_toBaselineOf 来实现，它的意思就是这个控件的基线与谁的基线对齐，代码如下：

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
        android:id="@+id/tv1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="20"
        android:textColor="@color/black"
        android:textSize="50sp"
        android:textStyle="bold"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/tv2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="¥"
        android:textColor="@color/black"
        android:textSize="20sp"
        app:layout_constraintBaseline_toBaselineOf="@id/tv1"
        app:layout_constraintStart_toEndOf="@id/tv1" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

通过 layout_constraintBaseline_toBaselineOf 就可以让两个不同大小的文案基线对齐。

对于一个 View 的边界界定，官方给了下面这张图：

![](https://upload-images.jianshu.io/upload_images/4987670-665466298d9d46a7.png)

### 2. 角度约束

有些时候我们需要一个控件在某个控件的某个角度的位置，那么通过其他的布局其实是不太好实现的，但是 constraintLayout 为我们提供了角度位置相关的属性

```java
app:layout_constraintCircle=""         目标控件id
app:layout_constraintCircleAngle=""    对于目标的角度(0-360)
app:layout_constraintCircleRadius=""   到目标中心的距离
```

来实现一下下图的 UI，jectpack 图标在 android 图标的 45 度方向，距离为 60dp

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1GibU603stOXPVKe7AdwkBNFYgiaqD8VIB68WkHewToJcPoxf28Gaq4uQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:id="@+id/android"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@drawable/android"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <ImageView
        android:id="@+id/jetpack"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:src="@drawable/jetpack"
        app:layout_constraintCircle="@+id/android"
        app:layout_constraintCircleAngle="45"
        app:layout_constraintCircleRadius="70dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 4. 百分比偏移

有的时候需要让控件在父控件的水平方向或者垂直方向的百分之多少的位置，可以使用如下属性：

```xml
app:layout_constraintHorizontal_bias=""   水平偏移 取值范围是0-1的小数
app:layout_constraintVertical_bias=""     垂直偏移 取值范围是0-1的小数
```

示例：控件 A 在父布局水平方向偏移 0.3(30%)，垂直方向偏移 0.8(80%)

注意：在使用百分比偏移时，需要指定对应位置的约束条件

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1XD6lpjOlpKQMYLQ91a6Pg4ALlZqBO47iaTs2GeYTxo3dm6DpLicUIWeQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="100dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.3"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.8" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 5. 控件内边距、外边距、GONE Margin

ConstraintLayout 的内边距和外边距的使用方式其实是和其他布局一致的。

```xml
<!--  外边距  -->
android:layout_margin="0dp"
android:layout_marginStart="0dp"
android:layout_marginLeft="0dp"
android:layout_marginTop="0dp"
android:layout_marginEnd="0dp"
android:layout_marginRight="0dp"
android:layout_marginBottom="0dp"

<!--  内边距  -->
android:padding="0dp"
android:paddingStart="0dp"
android:paddingLeft="0dp"
android:paddingTop="0dp"
android:paddingEnd="0dp"
android:paddingRight="0dp"
android:paddingBottom="0dp" 
```

ConstraintLayout 除此之外还有 GONE Margin，当依赖的目标 view 隐藏时会生效的属性，例如 B 被 A 依赖约束，当 B 隐藏时 B 会缩成一个点，自身的 margin 效果失效，属性如下：

```xml
<!--  GONE Margin  -->
app:layout_goneMarginBottom="0dp"
app:layout_goneMarginEnd="0dp"
app:layout_goneMarginLeft="0dp"
app:layout_goneMarginRight="0dp"
app:layout_goneMarginStart="0dp"
app:layout_goneMarginTop="0dp"
```

示例：当目标控件是显示的时候 GONE Margin 不会生效

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1Wc9JEeicN035EBzfcoObbfTA1hmRttQU1iauk6jWQSpLBf6ziaEvB6VYQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_marginStart="100dp"
        android:layout_marginTop="100dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <!--  该控件设置了 layout_goneMarginStart="100dp" 当A控件隐藏时才会生效  -->
    <TextView
        android:id="@+id/B"
        android:layout_width="60dp"
        android:layout_height="40dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="B"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintBottom_toBottomOf="@id/A"
        app:layout_constraintStart_toEndOf="@id/A"
        app:layout_constraintTop_toTopOf="@id/A"
        app:layout_goneMarginStart="100dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

当目标 A 控件隐藏时，B 的 GONE Margin 就会生效

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1MNE5jqYPZ6zQvDsF0TgrLYEiaLJiaiaSa4LzSH1Tx9JsKFhmDRvJicQXqw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 6. 控件尺寸

* 尺寸限制

在 ConstraintLayout 中提供了一些尺寸限制的属性，可以用来限制最大、最小宽高度，这些属性只有在给出的宽度或高度为 wrap_content 时才会生效，比如想给宽度设置最小或最大值，那宽度就必须设置为 wrap_content，具体的属性如下：

```xml
android:minWidth=""   设置view的最小宽度
android:minHeight=""  设置view的最小高度
android:maxWidth=""   设置view的最大宽度
android:maxHeight=""  设置view的最大高度
```

* 0dp(MATCH_PARENT)

设置 view 的带下除了传统的 wrap_content、指定尺寸、match_parent 外，ConstraintLayout 还可以设置为 0dp(MATCH_PARENT)，并且 0dp 的作用会根据设置的类型而产生不同的作用，进行设置类型的属性是 layout_constraintWidth_default 和 layout_constraintHeight_default，取值可为 spread、percent、wrap。具体的属性及示例如下：

```xml
app:layout_constraintWidth_default="spread|percent|wrap"
app:layout_constraintHeight_default="spread|percent|wrap"
```

#### 6.2.**spread(默认)**：占用所有的符合约束的空间

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1ps7eTyXg2roLdKsymoEfw7mp8S8fAtmdf1d2lRN84WbNsicqG6YFo0Q/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="0dp"
        android:layout_height="60dp"
        android:layout_marginStart="50dp"
        android:layout_marginTop="50dp"
        android:layout_marginEnd="50dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_default="spread" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

可以看到，view 的宽度适应了所有有效的约束空间，左右留出了 margin 的设置值 50dp，这种效果就是：自身 view 的大小充满可以配置的剩余空间，因为左右约束的都是父布局，所以 view 可配置的空间是整个父布局的宽度，有因为设置了 margin，所以会留出 margin 的大小，因为 spread 是默认值，所以可以不写 app:layout_constraintWidth_default = "spread"。

#### 6.1.**percent**：按照父布局的百分比设置

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1ggChDnpicPY61oIkTURxBAh9C2SejFpslXdTB4iblGtFtlqOYLjkursA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<?xml version="1.0" encoding="utf-8"?>
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
        android:layout_width="0dp"
        android:layout_height="60dp"
        android:layout_marginTop="50dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_default="percent"
        app:layout_constraintWidth_percent="0.5" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

percent 模式的意思是自身 view 的尺寸是父布局尺寸的一定比例，上图所展示的是宽度是父布局宽度的 0.5（50%，取值是 0-1 的小数），该模式需要配合 layout_constraintWidth_percent 使用，但是写了 layout_constraintWidth_percent 后，layout_constraintWidth_default = "percent" 其实就可以省略掉了。

#### 6.3.wrap: 匹配内容大小但不超过约束限制

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1Seao6gzY4ics2qK64kjw00CDRvgzEzickiamgUd6xQUjlqbb4LQH4l0yQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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

    <!--  宽度设置为wrap_content  -->
    <TextView
        android:id="@+id/A"
        android:layout_width="wrap_content"
        android:layout_height="60dp"
        android:layout_marginStart="100dp"
        android:layout_marginTop="50dp"
        android:layout_marginEnd="100dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="AAAAAAAAAAAAAAAAAA"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_default="spread" />

    <!--  宽度设置为0dp wrap模式  -->
    <TextView
        android:id="@+id/B"
        android:layout_width="0dp"
        android:layout_height="60dp"
        android:layout_marginStart="100dp"
        android:layout_marginTop="150dp"
        android:layout_marginEnd="100dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="BBBBBBBBBBBBBBBBBBBBBBB"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_default="wrap" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

这里写了两个空间作为对比，控件 A 宽度设置为 wrap_content，宽度适应内容大小，并且设置了 margin，但是显然宽度已经超过 margin 的设置值了，而控件 B 宽度设置为 0dp wrap 模式，宽度适应内容大小，并且不会超过 margin 的设置值，也就是不会超过约束限制，这就是这两者的区别。Google 还提供了两个属性用于强制约束：

```xml
<!--  当一个view的宽或高,设置成wrap_content时  -->
app:layout_constrainedWidth="true|false"
app:layout_constrainedHeight="true|false"
```

还是上一个例子，这里将控件 A 设置了强制约束，展示出的效果和控件 B 是一样的了：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1CHUSRaquNIqVj9ODAxFCOn5LkXjMxDiaofl2ibOsoWhiaujZY8Fba02aw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="wrap_content"
        android:layout_height="60dp"
        android:layout_marginStart="100dp"
        android:layout_marginTop="50dp"
        android:layout_marginEnd="100dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="AAAAAAAAAAAAAAAAAA"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constrainedWidth="true"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_default="spread" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

除此之外，还有一些其他的独特属性用于设置尺寸的大小限制

```xml
app:layout_constraintWidth_min=""   0dp下，宽度的最小值
app:layout_constraintHeight_min=""  0dp下，高度的最小值
app:layout_constraintWidth_max=""   0dp下，宽度的最大值
app:layout_constraintHeight_max=""  0dp下，高度的最大值
```

#### 6.4. 比例宽高（Ratio）

ConstraintLayout 中可以对宽高设置比例，前提是至少有一个约束维度设置为 0 dp，这样比例才会生效，该属性可使用两种设置：

1. 浮点值，表示宽度和高度之间的比率
2. 宽度:高度，表示宽度和高度之间形成的比率

```xml
app:layout_constraintDimensionRatio="" 宽高比例
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1dfVWLCmVVgVhvsuAurJVs4f0KXWFRXlXzHiaXP032giaXJYR4w6jcAog/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="0dp"
        android:layout_height="100dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintDimensionRatio="1:1"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 7. Chains(链)

Chains（链）也是一个非常好用的特性，它是将许多个控件在水平或者垂直方向，形成一条链，用于平衡这些控件的位置，那么如何形成一条链呢？形成一条链要求链中的控件在水平或者垂直方向，首位互相约束，这样就可以形成一条链，水平方向互相约束形成的就是一条水平链，反之则是垂直链，下面看示例：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT16jymEqibVEfQ06TKusDKc8Yriclk1icX6JjInmYnUlO3CjQ82GLLYW8Dg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toStartOf="@id/B"
        app:layout_constraintHorizontal_chainStyle="spread" # 链模式
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/B"
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="B"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toStartOf="@id/C"
        app:layout_constraintStart_toEndOf="@id/A"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/C"
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="C"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/B"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

A、B、C，三个控件在水平方向上首尾互相约束，这样就形成了一条水平链，他们默认的模式是 spread，均分剩余控件，我们可以使用 layout_constraintHorizontal_chainStyle 和 layout_constraintVertical_chainStyle 分别对水平和垂直链设置模式，模式可选的值有：spread、packed、spread_inside

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1DMXRAQfHJ9sJW3WwQd3Wl72zo364VKB3MJKC4dm2y7eWAG4strs7Kw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT17gich7cbHiajicv8TwpYmaSXF7B0sGpbAwHwhLxHXgGodaddZoFib5IDGA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1ibEmPAIyZwIFkia522Nkvk1TCPpiccnMuvgghccoNof6nMSfEw81Cz10A/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

Chains（链）还支持 weight（权重）的配置，使用 layout_constraintHorizontal_weight 和 layout_constraintVertical_weight 进行配置链元素的权重。

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1IgYbnEVCiakuVR5tibfzUa6wC13XJFjCBjpaPKliaP9FcYmXEgw2EpkLg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
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
        android:layout_width="0dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toStartOf="@id/B"
        app:layout_constraintHorizontal_weight="2"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed" />
           <TextView
        android:id="@+id/B"
        android:layout_width="0dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="B"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toStartOf="@id/C"
        app:layout_constraintHorizontal_weight="1"
        app:layout_constraintStart_toEndOf="@id/A"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/C"
        android:layout_width="0dp"
        android:layout_height="80dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="C"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_weight="3"
        app:layout_constraintStart_toEndOf="@id/B"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

# 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)
