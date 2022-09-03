# ConstraintLayout 

![](image/ConstraintLayout.png)

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

那么 ConstraintLayout 就是使用这些属性来确定控件的位置，虽然比较多，但是有个 规律可循，没有任何记忆压力。

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

可以看到，view 的宽度适应了所有有效的约束空间，左右留出了 margin 的设置值 50dp，这种效果就是：自身 view 的大小充满可以配置的声誉空间，因为左右约束的都是父布局，所以 view 可配置的空间是整个父布局的宽度，有因为设置了 margin，所以会留出 margin 的大小，因为 spread 是默认值，所以可以不写 app:layout_constraintWidth_default = "spread"。

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

#### 6.3.**wrap:**匹配内容大小但不超过约束限制

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

除此之外，odp 还有一些其他的独特属性用于设置尺寸的大小限制

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
        app:layout_constraintHorizontal_chainStyle="spread"
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

## 三、辅助类

ConstraintLayout 为了解决嵌套问题还提供了一系列的辅助控件帮助开发者布局，这些工具十分的方便，在日常开发工作中也是使用的非常频繁。

### 1. Guideline（参考线）

Guideline 是一条参考线，可以帮助开发者进行辅助定位，并且实际上它并不会真正显示在布局中，像是数据几何中的辅助线一样，使用起来非常方便，出场率很高，Guideline 也可以用来做一些百分比分割之类的需求，有着很好的屏幕适配效果，Guideline 有水平和垂直方向之分，位置可以使用针对父级的百分比或者针对父级位置的距离

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

### 2. GBarrier（屏障）

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

Placeholder 的作用就是占位，它可以在布局中占好位置，通过 app:content="" 属性，或者动态调用 setContent() 设置内容，来让某个控件移动到此占位符中

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

### 5. Flow(流式虚拟布局)

Flow 是用于构建链的新虚拟布局，当链用完时可以缠绕到下一行甚至屏幕的另一部分。当在一个链中布置多个项目时，这很有用，但是不确定容器在运行时的大小。可以使用它来根据应用程序中的动态尺寸（例如旋转时的屏幕宽度）构建布局。Flow 是一种虚拟布局。在 ConstraintLayout 中，虚拟布局（Virtual layouts）作为 virtual view group 的角色参与约束和布局中，但是它们并不会作为视图添加到视图层次结构中，而是仅仅引用其他视图来辅助它们在布局系统中完成各自的布局功能。

下面使用动画来展示 Flow 创建多个链将布局元素充裕地填充一整行：

![](https://mmbiz.qpic.cn/mmbiz_gif/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1WeBr0VAzjnofJzYBXNSUFwZronRzxxZlsxc08gib4uquicehtxzoj8Aw/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1TojhfaXQOXgdOaUOibOUVsYHFdiaXt85nybRIPjYrGaltWgMVcakia5ug/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

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
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="A"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold" />
        
    <TextView
        android:id="@+id/B"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="B"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/C"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="C"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/D"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="D"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/E"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:background="@drawable/tv_bg"
        android:gravity="center"
        android:text="E"
        android:textColor="@color/black"
        android:textSize="25sp"
        android:textStyle="bold" />

    <androidx.constraintlayout.helper.widget.Flow
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:constraint_referenced_ids="A,B,C,D,E"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### 5.1. 链约束

Flow 的 constraint_references_ids 关联的控件是没有设置约束的，这一点和普通的链是不一样的，这种排列方式是 Flow 的默认方式 none，可以使用 app:flow_wrapMode ="" 属性来设置排列方式，并且还可以使用 flow_horizontalGap 和 flow_vertialGap 分别设置两个 view 在水平和垂直方向的间隔，下面再添加几个控件来展示三种排列方式：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1vlIQWMHq0NT0cEFibqKTQ8upQkVBcuics8oo6v2X8InW0kaA2HlmhO7Q/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1FfV349C63YziaK5IrjzdkMc0bDqr91DRSLKxkyQgyhgwcWic42eSegNA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1Yh2zdFMIxy6oHwXXsEQNMD64u0CD26maF6mCgxIljtZbqoliaGjWPQA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

下面使用动画来展示三种效果的变化

![](https://mmbiz.qpic.cn/mmbiz_gif/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1XoVdwowG1k3RmeZ3vSbFriaeJuBGnsia8OJQeiczJJ4L1rR9YrGibSUt3g/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

当 flow_wrapMode 的值是 chain 或 aligned 时，还可以针对不同的链进行配置，具体属性如下：

```xml
app:flow_horizontalStyle="packed｜spread｜spread_inside"  所有水平链的配置
app:flow_verticalStyle="packed｜spread｜spread_inside"    所有垂直链的配置

app:flow_firstHorizontalStyle="packed｜spread｜spread_inside" 第一条水平链的配置，其他条不生效
app:flow_firstVerticalStyle="packed｜spread｜spread_inside"   第一条垂直链的配置，其他条不生效
app:flow_lastHorizontalStyle="packed｜spread｜spread_inside"  最后一条水平链的配置，其他条不生效 
app:flow_lastVerticalStyle="packed｜spread｜spread_inside"    最后一条垂直链的配置，其他条不生效
```

#### 5.2. 对齐约束

上面展示的都是相同大小的 view，那么不同大小 view 的对齐方式，Flow 也提供了相应的属性进行配置（flow_wrapMode="aligned"时，我试着没有效果）

```xml
<!--  top:顶对齐、bottom:底对齐、center:中心对齐、baseline:基线对齐  -->
app:flow_verticalAlign="top｜bottom｜center｜baseline"

<!--  start:开始对齐、end:结尾对齐、center:中心对齐  -->
app:flow_horizontalAlign="start|end|center"
```

使用 flow_verticalAlign 时，要求 orinentation 的方向是 horizontal，而使用 flow_horizontalAlign 时，要求 orientation 的方向是 vertical。

下面展示下各个效果：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1XpP7MtPiaUswuk99v2F8mJVao8ClcjbR9fknKkwXpLJibiaTCVVI8dxlg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<androidx.constraintlayout.helper.widget.Flow
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        app:constraint_referenced_ids="A,B,C,D,E,F,G,H,I,J"
        app:flow_verticalAlign="top"
        app:flow_wrapMode="chain"
        app:layout_constraintTop_toTopOf="parent" />
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1hSXmfCysKaJPk2F9vsa96Xf98KRViaEtPWEe229Otp3NSg3hrCDtAxQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<androidx.constraintlayout.helper.widget.Flow
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        app:constraint_referenced_ids="A,B,C,D,E,F,G,H,I,J"
        app:flow_verticalAlign="bottom"
        app:flow_wrapMode="chain"
        app:layout_constraintTop_toTopOf="parent" />
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1icPahoM4UnVwT04X8VwXof2wFOt8NdtTW8A8YCHb1pZAVGNr8TOU25g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<androidx.constraintlayout.helper.widget.Flow
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        app:constraint_referenced_ids="A,B,C,D,E,F,G,H,I,J"
        app:flow_verticalAlign="center"
        app:flow_wrapMode="chain"
        app:layout_constraintTop_toTopOf="parent" />
```

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1n3ib70h3tfZiavILGbbknw23GX5NtOicykuP2o5ZpwpEpG7vOqroSYTAQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```xml
<androidx.constraintlayout.helper.widget.Flow
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        app:constraint_referenced_ids="A,B,C,D,E,F,G,H,I,J"
        app:flow_verticalAlign="baseline"
        app:flow_wrapMode="chain"
        app:layout_constraintTop_toTopOf="parent" />
```

#### 5.3. 数量约束

当 flow_wrapMode 属性为 aligned 和 chain 时，通过 flow_maxElementsWrap 属性控制每行最大的子 View 数量，例如我们设置为 flow_maxElementsWrap = 4，效果图如下：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6Sf5UJpuDjpG03kD1YHMT1Ebv1b2JCK1uWXFAiaRoDd2TdAb8k8NcFeW8BzbIQibNk8fXv8sDibjQ5g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 6.Layer（层布局）

Layer 继承自 ConstraintHelper，是一个约束助手，相对于 Flow 来说，Layer 的使用较为简单，常用来增加背景，或者共同动画，图层（Layer）在布局期间会调整大小，其大小会根据其引用的所有视图进行调整，代码的先后顺序也会决定着它的位置，如果代码在所有引用 view  最后面，那么它就会在所有 view 的最上面，反之则是最下面，在最上面的时候如果添加背景，就会把引用的 view 覆盖掉，下面展示下添加背景的例子

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

### 7.ImageFilterButton & ImageFilterView

ImageFilterButton 和 ImageFilterView 是两个控件，他们之间的关系就和 ImageButton 与 ImageView 是一样的，所以这里就只拿 ImageFilterView 来做讲解。从名字上来看，它们的定位是和过滤有关系的，它们的大致作用有两部分，一是可以用来做圆角图片，二是可以叠加图片资源进行混合过滤，下面一一展示：

#### 7.1. 圆角图片

ImageFilterButton 和 ImageFilterView 可以使用两个属性来设置图片资源的圆角，分别是 roundPercent 和 round，roundPercent 接受的值类型是 0-1 的小数，根据数值的大小会使图片在方形和圆形之间按比例过渡，round=可以设置具体圆角的大小，在使用的过程中发现 AndroidStudio，没有这两个属性的代码提示，也没有预览效果，但是运行起来是由效果的，可能是没有做好优化吧。

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

Motion Layout 是 Constraint Layout 2.0 中最令人期待的功能之一。它提供了一个丰富的动画系统来协调多个视图之间的动画效果。MotionLayout 基于 ConstraintLayout，并在其之上进行了扩展，允许您在多组约束（或者 ConstraintSets）之间进行动画的处理。您可以对视图的移动、滚动、缩放、旋转、淡入淡出等一系列动画行为进行自定义，甚至可以定义各个动画半身的自定义属性。它还可以处理手势操作所产生的物理移动效果，以及控制动画的速度。使用 MotionLayout 构建的动画是可追溯且可逆的，这意味着您可以随意切换到动画过程中人一一个点，甚至可以倒着执行动画效果。Android Studio 继承了 Motion Editor（动作编辑器），可以利用它来操作 MotionLayout 对动画进行生成、预览和编辑等操作。这样一来，在协调多个视图的动画时，就可以做到对各个细节进行精细操控。

# 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)

