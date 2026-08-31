# ConstraintLayout VritualLayout Helper

![](image/ConstraintLayout.png)

> ContraintLayout VirtualLayout Helper：
> 
> 在 ConstraintLayout 中，虚拟布局（Virtual Layouts）作为 virtual view group 的角色参与约束和布局中，但是它们并不会作为视图添加到视图层次结构中，而是仅仅引用其他视图来辅助它们在布局系统中完成各自的布局功能。
> 
> 1. Flow
>    
>    Flow 是 ConstraintLayout 2.0 引入的 VirtualLayout Help，用来把一组 View 按水平或垂直方向排列，并在空间不够时自动换行/换列。
>    
>    Flow 只能在 ConstraintLayout 中使用。
>    
>    app:flow_wrapMode 设置换行模式：
>    
>        * none(默认)：不换行，所有 View 排成一条 Chain
>    
>        * chain：空间不够时换行，每行/列是一个 Chain
>    
>        * aligned：换行，并且像表格一样行列对齐
>    
>    app:flow_verticalAlign和app:flow_horizontalAlign 设置对齐方式。
>    
>    app:flow_maxElementsWrap 控制每行最大的子 View 数量。当 flow_wrapMode 属性为 aligned 和 chain 时。
> 
> 2. Gride
> 
> 3. CircularFlow

ConstraintLayout 2.0 引入的 VirtualLayout Helper（有 Flow、Grid和CircularFlow）。

在 ConstraintLayout 中，虚拟布局（Virtual layouts）作为 virtual view group 的角色参与约束和布局中，但是它们并不会作为视图添加到视图层次结构中，而是仅仅引用其他视图来辅助它们在布局系统中完成各自的布局功能。

### 1. Flow(流式虚拟布局)

Flow 是 ConstraintLayout 2.0 引入的 VirtualLayout Helper（有 Flow、Grid和CircularFlow），用来把一组 View 按水平或垂直方向排列，并在空间不够时自动换行/换列。可以把它理解成：带自动换行能力的 Chain，或者 ConstraintLayout 内置的轻量“流式布局”。

和 Group、Barrier 一样，Flow 只能在 ConstraintLayout 中使用。

**它能解决什么问题**

传统做法里，标签云、筛选 chips、九宫格按钮等场景最常选择的是：

| 方案                 | 问题                 |
| ------------------ | ------------------ |
| 多个 LinearLayout 嵌套 | 层级深、难维护            |
| FlexboxLayout      | 额外依赖               |
| 手写 Chain           | 只能单行/单列，不能自动 wrap  |
| RecyclerView       | 动态列表合适，静态少量 tag 过重 |

Flow 的做法是：你只管声明 “哪些 View 参与排列”，由 Flow 负责链式排列和换行。

Flow 是用于构建链的新虚拟布局，当链用完时可以缠绕到下一行甚至屏幕的另一部分。当在一个链中布置多个项目时，但是又不确定容器在运行时的大小，Flow 就很有用。可以使用它来根据应用程序中的动态尺寸（例如旋转时的屏幕宽度）构建布局。Flow 是一种虚拟布局。

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

要点：

1. 被引用的 View 是 ConstraintLayout 的直接子 View

2. 通过 app:constraint_referenced_ids 指定参与排列的 id

3. Flow 自身用约束定位（例如贴顶部、撑满宽度）

4. 被引用 View 不需要互相写约束

#### 5.1. 链约束

app:flow_wrapMode 属性设置换行模式：

| 值        | 含义                     |
| -------- | ---------------------- |
| none（默认） | 不换行，所有 View 排成一条 Chain |
| chain    | 空间不够时换行，每行/列是一个 Chain  |
| aligned  | 换行，并且像表格一样行列对齐         |

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

#### **和 Chain/Flexbox 怎么选**

| 场景                                   | 推荐                               |
| ------------------------------------ | -------------------------------- |
| 单行等分（如底部3个Tab）                       | Chain                            |
| 多行标签、筛选 chips                        | Flow                             |
| 固定每行 N 个的网格                          | Flow+flow_maxElementsWrap        |
| 复杂 flex 对齐、order（排序）、grow/shrink（伸缩） | FlexboxLayout 或 Compose FlowRow  |
| 大量动态数据                               | RecyclerView / LazyVerticalGride |

### 2. Grid

和 Flow 同属 VirtualLayout，但 Grid 是固定行列网络，Flow 时流式换行。

Grid 把 constraint_referenced_ids 里的一组 View，按行列排成网络，类似简化版 GridLayout，但：

* 不增加 View 层级

* 只在 ConstraintLayout 里用

* 支持 span、skip、行列权重

典型场景：计算器键盘、九宫格、表单网格、图标矩阵。

基本用法：

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button android:id="@+id/btn1" android:layout_width="0dp" android:layout_height="0dp" android:text="1" />
    <Button android:id="@+id/btn2" android:layout_width="0dp" android:layout_height="0dp" android:text="2" />
    <Button android:id="@+id/btn3" android:layout_width="0dp" android:layout_height="0dp" android:text="3" />
    <!-- ... btn4 ~ btn9, btn0 ... -->

    <androidx.constraintlayout.helper.widget.Grid
        android:id="@+id/keypad"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:constraint_referenced_ids="btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0"
        app:grid_rows="4"
        app:grid_columns="3" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

要点：

* 被引用 View 是 ConstraintLayout 的直接子 View

* 子 View 一般设 0dp 宽高，由 Grid 分配单元格大小

* 排列顺序 = constraint_referenced_ids 里的 id 顺序（从左到右、从上到下）

核心属性：

| 属性                  | 作用                       |
| ------------------- | ------------------------ |
| grid_rows           | 行数                       |
| grid_columns        | 列数                       |
| grid_orientation    | 填充方向：horizontal/vertical |
| grid_horizontalGaps | 水平间距                     |
| grid_vertivalGaps   | 垂直间距                     |
| grid_rowWeights     | 每行权重，如“1,2,1”            |
| grid_columnWeights  | 每列权重                     |
| grid_spans          | 跨行跨列，格式 位置：行数x列数         |
| grid_skips          | 跳过某些格子，留空                |

只设 grid_rows 或 grid_columns 时，另一个会根据引用 View 数量自动算。

* grid_spans 跨格
  
  格式：位置：行数x列数（位置从 0 开始，左上角为 0）
  
  ```xml
  <!-- 第 0 格占 2 行 x 2 列 -->
  app:grid_spans="0:2x2"
  ```
  
  适合：计算器里 0 占两格、大图占多格等。

* grid_skips （留空）
  
  跳过指定格子，不放置 View:
  
  ```xml
  app:grid_skips="2,5"
  ```
  
  适合：中间留空、不规则布局。

注意事项：

1. 只能在 ConstraintLayout 中使用

2. 被引用 View 必须是直接子 View

3. 子 View 不要再写互相约束，由 Grid 接管

4. 需要 ConstraintLayout 2.1+

5. View 很多、数据驱动 -> 仍优先考虑 RecyclerView + GridLayoutManager

### 3. CircularFlow

CirculaFlow 是 constraintLayout 2.1+ 的 VirtualLayout Helper，用来把一组 View 绕中心 View 排成圆形/环形。

CircularFlow 把 constraint_referenced_ids 里的 View，按角度+半径绕 circularflow_viewCenter 指定的中心 View 排列。

典型场景：

* 圆形菜单/FAB 展开菜单

* 轮播图周围指示器

* 头像周围一圈 icon

* 配合 MotionLayout 做旋转轮播

基本用法：

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- 中心 View -->
    <ImageView
        android:id="@+id/center"
        android:layout_width="64dp"
        android:layout_height="64dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:srcCompat="@drawable/avatar" />

    <!-- 环绕的 View -->
    <ImageView android:id="@+id/icon1" android:layout_width="40dp" android:layout_height="40dp" ... />
    <ImageView android:id="@+id/icon2" android:layout_width="40dp" android:layout_height="40dp" ... />
    <ImageView android:id="@+id/icon3" android:layout_width="40dp" android:layout_height="40dp" ... />
    <ImageView android:id="@+id/icon4" android:layout_width="40dp" android:layout_height="40dp" ... />
    <ImageView android:id="@+id/icon5" android:layout_width="40dp" android:layout_height="40dp" ... />

    <androidx.constraintlayout.helper.widget.CircularFlow
        android:id="@+id/circularFlow"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:circularflow_viewCenter="@id/center"
        app:constraint_referenced_ids="icon1,icon2,icon3,icon4,icon5"
        app:circularflow_angles="0,72,144,216,288"
        app:circularflow_radiusInDP="120,120,120,120,120" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

核心属性：

| 属性                         | 作用                            |
| -------------------------- | ----------------------------- |
| circularflow_viewCenter    | 中心 View 的 id（必填）              |
| constraint_referenced_ids  | 环绕 View 的 id 列表（必填）           |
| circularflow_angles        | 每个 View 的角度，逗号分隔（与 id 顺序一一对应） |
| circularflow_radiusInDP    | 每个 View 到中心的半径（dp），逗号分隔       |
| circularflow_defaultAngle  | 未单独指定时的默认角度                   |
| circularflow_defaultRadius | 未单独指定时的默认半径                   |

角度说明：

* 单位：度

* 0 通常在右侧，按常见坐标系逆时针增加

* 每个 View 可以有不同的角度和半径（不必在同一个圆上）

运行时动态操作

```kotlin
val circularFlow = findViewById<CircularFlow>(R.id.circularFlow)

// 添加
circularFlow.addViewToCircularFlow(newView, radius = 120, angle = 180f)

// 更新单个 View
circularFlow.updateAngle(icon1, 90f)
circularFlow.updateRadius(icon1, 150)
circularFlow.updateReference(icon1, radius = 150, angle = 90f)

// 移除
circularFlow.removeViewFromCircularFlow(icon2)
```

注意：被操作的 View 必须是 ConstraintLayout 的直接子 View，且已设置 id。

和 ConstraintLayout 圆形约束的区别

ConstraintLayout 本身也有 layout_constraintCircle 系列属性，可以单个 View 绕锚点定位：

```xml
app:layout_constraintCircle="@id/center"
app:layout_constraintCircleRadius="120dp"
app:layout_constraintCircleAngle="45"
```

|      | layout_constraintCircle | CircularFlow              |
| ---- | ----------------------- | ------------------------- |
| 作用范围 | 单个 View                 | 一组 View 统一管理              |
| 动态增删 | 每个都要写约束                 | addViewCircularFlow 等 API |
| 适合   | 1-2 个 View 绕圈           | 多个 View 环形菜单/轮播           |

注意事项：

1. 只能在 ConstraintLayout 中使用

2. 中心 View 和环绕 View 都必须是直接子 View

3. 环绕 View 不需要手动写圆形约束，由 CircularFlow 计算位置

4. 需要 ConstraintLayout 2.1+

5. angles 和 radiusInDP 列表长度建议与 constraint_referances_ids 一一对应

6. 复杂、数据驱动的环形列表 -> 可能仍用自定义 View 或 Compose

### 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)
