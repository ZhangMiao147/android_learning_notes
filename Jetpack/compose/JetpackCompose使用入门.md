# Jetpack Compose 使用入门

## 简介

### 为什么采用 Compose

Jetpack Compose 是用于构建原生 Android 界面的新款工具包。它使用更少的代码、强大的工具和直观的 Kotlin API，可以帮助简化并加快 Android 界面开发，打造主动而精彩的应用。它可让您更快速、更轻松地构建 Android 界面。

#### 精简代码

编写更少的代码会影响到所有开发阶段：作为代码撰写者，需要测试和调试的代码会更少，出现 bug 的可能性也更小，就可以专注于解决手头的问题；作为审核人员或维护人员，需要阅读、理解、审核和维护的代码就更少。

与使用 Android View 系统（按钮、列表或动画）相比，Compose 可让您使用更少的代码实现更多的功能。无论需要构建什么内容，需要编写的代码都更少了。

编写代码只需要采用 Kotlin，而不必拆分成 Kotlin 和 XML 部分。

无论构建什么，使用 Compose 编写的代码都很简洁且易于维护。

#### 直观易读

Compose 使用声明性 API，这意味着只需要描述界面，Compose 会负责完成其余工作，这类 API 十分直观 - 易于探索和使用。

利用 Compose，可以构建不与特定 activity 或 fragment 相关联的小型无状态组件。可以轻松重用和测试这些组件。

在 Compose 中，状态是显式的，并且会传递给相应的可组合项。这样一来，状态便具有单一可信来源，因而是封装和分离的。然后，应用状态变化时，界面会自动更新。

#### 加速开发

Compose 与所有的现有代码兼容：可以从 View 调用 Compose 代码，也可以从 Compose 调用 View。大多数常用库（如 Navigation、ViewModel 和 Kotlin 协程）都适用于 Compose，因此可以随时随地地开始采用。

借助全面地 Android Studio 支持以及实时预览等功能，可以更快地迭代和交付代码。

#### 功能强大

利用 Compose，可以凭借对 Android 平台 API 的直接访问和对于 Material Design、深色主题、动画等的内置支持，创建精美的应用。

利用 Compose，可以轻松快速地通过动画让应用变得生动有趣。

无论是使用 Material Design 还是自己的设计系统进行构建，Compose 都可以让您灵活地实现所需的设计。

### Compose 编程思想

Jetpack Compose 是一个适用于 Android 的新式声明性界面工具包。Compose 提供声明性 API，可在不以命令方式改变前端实图的情况下呈现应用界面，从而使编写和维护应用界面变得更加容易。

#### 声明性编程范式

长期以来，Android 视图层次结构一直可以表示为界面 widget 树。由于应用的状态会因用户交互等因素而发生变化，因此界面层次结构需要进行更新以显示当前数据。最常见的界面更新方式是使用 findViewById() 等函数遍历树，并通过调用 button.setText(String)、container.addChild(View) 或 img.setImageBitmap(Bitmap) 等方法更改节点。这些方法会改变 widget 的内部状态。

手动操纵视图会提高出错的可能性。如果一条数据在多个位置呈现，很容易忘记更新显示它的某个视图。此外，当两项更新以出人意料的方式发生冲突时，也很容易造成异常状态。例如，某项更新可能会尝试设置刚刚从界面中移除的节点的值。一般来说，软件维护的复杂性会随着需要更新的视图数量而增长。

在过去的几年中，整个行业已开始转向声明性界面模型，该模型大大简化了与构建和更新界面关联的工程任务。该技术的工作原理是在概念上从头开始重新生成整个屏幕，然后仅执行必要的更改。此方法可避免手动更新有状态层次结构的复杂性。Compose 是一个声明性界面框架。

重新生成整个屏幕所面临的一个难题是，在时间、计算能力和电池用量方面可能成本高昂。为了减少在这方面耗费的资源，Conpose 会只能地选择在任何给定时间需要重新绘制界面的哪些部分。这会对设计页面组件的方式有一定影响。

#### 简单的可组合函数

使用 Compose，可以通过定义一组接受数据而发出界面元素的可组合函数来构建界面。

* 所有可组合函数都必须带有 @Composable 注释，此注释可告知 Compose 编译器：此函数旨在将数据转换为界面。

* 可组合函数通过调用其他可组合函数来发出界面层次结构。

* 发出界面的 Compose 函数不需要返回任何内容，因为它们所需的屏幕状态，而不是构造界面 weight。

#### 声明性范式转变

在许多面向对象的命令式界面工具包中，可以通过实例化 widget 树来初始化界面。通常通过膨胀 XML 布局文件来实现此目的。每个 widget 都维护自己的内部状态，并且提供 getter 和 setter 方法，允许应用逻辑与 widget 进行交互。

在 Compose 的声明性方法中，widget 相对无状态，并且不提供 setter 或 getter 函数。实际上，widget 不会以对象形式提供。可以通过调用带有不同参数的同一可组合函数来更新界面。这使得向架构模式（如 ViewModel）提供状态变得很容易，然后，可组合项负责在每次可观察数据更新时将当前应用状态转换为界面。

![](https://developer.android.com/static/develop/ui/compose/images/mmodel-flow-data.png?hl=zh-cn)

(图.应用逻辑为顶级可组合函数提供数据。该函数通过调用其他可组合函数来使用这些数据描述界面，将适当的数据传递给这些可组合函数，并沿层次结构向下传递数据。)

当用户与界面交互时，界面会发出 onClick 等事件。这些事件应通知应用逻辑，应用逻辑随后可以改变应用的状态，当状态发生变化时，系统会使用新数据再次调用可组合函数。者会导致重新绘制界面元素，此过程称为“重组”。

![](https://developer.android.com/static/develop/ui/compose/images/mmodel-flow-events.png?hl=zh-cn)

(图.用户与界面元素进行了交互，导致触发了一个事件。应用逻辑响应该事件，然后系统根据需要使用新参数自动再次调用可组合函数。)

#### 动态内容

由于可组合函数是用 Kotlin 而不是 XML 编写的，因此它们可以像其他任何 Kotlin 代码一样动态。例如，假设想要构建一个界面，用来问候一些用户：

```kotlin
@Composable
fun Greeting(names: List<String>) {
    for (name in names) {
       Text("Hello $name") 
    }
}
```

此函数接受名称的列表，并为每个用户生成一句问候语。可组合函数可能非常复杂。可以使用 if 语句来确定是否要显示特定的界面元素。可以使用循环。可以调用辅助函数。拥有底层语言的全部灵活性。这种强大的功能和灵活性是 Jetpack Compose 的主要优势之一。

#### 重组

在命令式界面模型中，如需更改某个 widget，可以在该 widget 上调用 setter 以更改其内部状态。在 Compose 中，可以使用新数据再次调用可组合函数。这样做会导致函数进行重组 -- 系统会根据需要使用新数据重新绘制函数发出的 widget。Compose 框架可以智能地仅重组已更改的组件。

例如，假设有以下可组合函数，它用于显示一个按钮：

```kotlin
@Composable
fun ClickCounter(clicks:Int, onClick:() -> Unit){
    Button(onClick = onClick){
    Text("I've been clicked $clicks times")
}
}
```

每次点击该按钮，调用方都会更新 clicks 的值。Compose 会再次调用 lambda 与 Text 函数以显示新值；此过程称为 “ 重组 ”。不依赖与该值的其他函数不会进行重组。

如前文所述，重组整个界面树在计算上成本高昂，因为会消耗计算能力并缩短电池续航时间。Compose 使用智能重组来解决此问题。

重组是指在输入更改时再次调用可组合函数的过程。当函数的输入更改时，会发生这种情况。当 Compose 根据新输入重组时，它仅调用可能已更改的函数或 lambda，而跳过其余函数或 lambda。通过跳过所有未更改参数的函数或 lambda，Compose 可以高效地重组。

切勿依赖于执行可组合函数所产生的附带效应，因为可能会跳过函数的重组。如果这样做，用户可能会在您的应用中遇到奇怪且不可预测的行为。附带效应是指应用的其余部分可见的任何更改。例如，以下操作全部都是危险的附带效应：

* 写入共享对象的属性

* 更新 ViewModel 中的可观察项

* 更新共享偏好设置

可组合函数可能会像每一帧一样频繁地重新执行，例如在呈现动画时。可组合函数应快速执行，以避免在播放动画期间出现卡顿。如果需要执行成本高昂的操作（例如从共享偏好设置读取数据），请在后台协程中执行，并将值结果作为参数传递给可组合函数。

例如，以下代码会创建一个可组合项以更新 SharedPreferences 中的值。该可组合项不应从共享偏好设置本身读取或写入，于是此代码将读取和写入操作移至后台协程中的 ViewModel。应用逻辑会使用回调传递当前值以触发更新。

```kotlin
@Composable
fun SharedPrefsToggle(
    text:String,
    value:Boolean,
    onValueChanged: (Boolean) -> Unit
) {
    Row {
        Text(text)
        Checkbox(checked = value, onCheckedChange = onValueChanged)
    }
}
```

使用 Compose 时需要注意的一些事项：

* 可组合函数可以按任何顺序执行。

* 可组合函数可以并行执行。

* 重组会跳过尽可能多的可组合函数和 lambda。

* 重组是乐观的操作，可能会被取消。

* 可组合函数可能会像动画的每一帧一样非常频繁地运行。

下面几部分都是介绍如何构建可组合函数以支持重组。在每种情况下，最佳时间都是使可组合函数保持快速、幂等且没有附带效应。

##### 可组合函数可以按任何顺序执行

如果看一下可组合函数的代码，可能会认为这些代码按其出现的顺序执行。但其实未必是这样。如果某个可组合函数包含对其他可组合函数的调用，这些函数可以按任何顺序运行。Compose 可以选择识别出某些界面元素的优先级高于其他界面元素，因而首先绘制这些元素。

例如，假设您有如下代码，用于在标签页布局中绘制三个屏幕：

```kotlin
@Composable
fun ButtonRow(){
    MyFancyNavigation {
        StartScreen()
        MiddleScreen()
        EndScreen()
    }
}
```

对 StartScreen、MiddleScreen 和 EndScreen 的调用可以按任何顺序进行。这意味着，举例来说，您不能让 StartScreen() 设置某个全局变量（附带效应）并让 MiddleScreen() 利用这项更改。相反，其中某个函数都需要保持独立。

##### 可组合函数可以并行运行

Compose 可以通过并行运行可组合函数来优化重组。这样一来，Compose 就可以利用多个核心，并以较低的优先级运行可组合函数（不在屏幕上）。

这种优化意味着，可组合函数可能会在后台线程池中执行。如果某个可组合函数对 ViewModel 调用一个函数，则 Compose 可能会同时从多个线程调用该函数。

为了确保应用正常运行，所有可组合函数都不应有附带效应，而应通过始终在界面县城上执行的 onClick 等回调触发附带效应。

调用某个可组合函数时，调用可能发生在与调用方不同的线程上。这意味着，应避免使用修改可组合 lambda 中的变量的代码，即因为此类代码并非线程安全代码，又因为它是可组合 lambda 不允许的附带效应。

以下示例展示了一个可组合项，它显示一个列表及其项数：

```kotlin
@Composable
fun ListComposable(myList: List<String>) {
    Row(horizontalArrangement = Arrangment.SpaceBetween) {
        Column {
          for (item in myList) {
              Text("Item: $item")
          }  
        }
        Text("Counr: ${myList.size}")
    }
}
```

此代码没有附带效应，它会将输入列表转换为界面。此代码非常适合显示小列表。不过，如果函数写入局部变量，则这并非线程安全或正确的代码。

```kotlin
@Composable
@Deprecated("Example with bug")
fun ListWithBug(myList: List<String>) {
    var items = 0

    Row(horizontalArrangement = Arrangment.SpaceBetween) {
        Column {
          for (item in myList) {
              Text("Item: $item")
              items++ // Avoid! Side-effect of the column recomposing.
          }  
        }
        Text("Counr: ${items}")
    }
}
```

在本例中，每次重组时，都会修改 items。这可以是动画的每一帧，或是在列表更新时。但不管怎样，界面都会显示错误的项数。因此，Compose 不支持这样的写入操作；通过禁止此类写入操作，我们允许框架更改线程以执行可组合 lambda。

##### 重组会跳过尽可能多的内容

如果界面的某些部分失效，Compose 会尽力只重组需要更新的部分。这意味着，它可以跳过某些内容以重新运行单个按钮的可组合项，而不执行界面树中在其上面或下面的任何可组合项。

每个可组合函数和 lambda 都可以自行重组。以下示例演示了在呈现列表时重组如何跳过某些元素：

```kotlin
/**
* Display a list of names the user can click with a header
* /
@Composable
fun NamePicker(
    header:String,
    names:List<String>,
    onNameClicked: (String) -> Unit
) {
    Column {
        // this will recompose when [header] changes, but not when [names] changes
        Text(header, style = MaterialTheme.typography.bodyLarge)
        Divider()

        // LazyColumn is the Compose version of a RecyclerView.
        // Thelambda passed to items() is similar to a RecyclerView.ViewHolder.
        LazyColumn {
            items(names) { name ->
                // When an item's [name] updates, the adapter for that item
                // will recompose. This will not recompose whrn [header] changes
                NamePickerItem(name, onNameClicked)
            }
        }
    }
}

/**
* Display a single name the user can click.
*/
@Composable
private fun NamePickerItem(name: String, onClicked:(String) -> Unit){
    Text(name, Modifier.clickable(onClick = { onClicked(name) }))
}
```

这些作用域中的每一个都可能是在重组期间执行的唯一一个作用域。当 header 发生更改时，Compose 可能会跳至 Column lambda，而不执行它的任何父项。此外，执行 Column 时，如果 names 未更改，Compose 可能会选择跳过 LazyColumn 的项。

同样，执行所有可组合函数或 lambda 都应该没有附带效应。当您需要执行附带效应时，应通过回调触发。

##### 重组是乐观的操作

只要 Compose 认为某个可组合项的参数可能已更改，就会开始重组。重组是乐观的操作，也就是说，Compose 预计会在参数再次更改之前完成重组。如果某个参数在重组完成之前发生更改，Compose 可能会取消重组，并使用新参数重新开始。

取消重组后，Compose 会从重组中舍弃界面树。如有任何附带效应依赖于显示的界面，则即使取消了组合操作，也会应用该附带效应。这可能会导致应用状态不一致。

确保所有可组合函数和 lambda 都幂等且没有附带效应，以处理乐观的重组。

##### 可组合函数可能会非常频繁地运行

在某些情况下，可能会针对界面动画的每一帧运行一个可组合函数。如果该函数执行成本高昂的操作（例如从设备存储空间读取数据），可能会导致界面卡顿。

例如，如果您的 widget 尝试读取设备设置，它可能会在一秒内读取这些设置数百次，这会对应用的性能造成灾难性的影响。

如果您的可组合函数需要数据，它应为相应的数据定义参数。然后，您可以将成本高昂的工作移至组成操作线程之外的其他线程，并使用 mutableStateOf 或 LiveData 将相应的数据传递给 Compose。

### 构建自适应应用

#### 什么是自适应应用？

自适应应用会根据应用显示界面的变化重组布局，主要是应用窗口大小的变化但自适应应用还能够可折叠设备的折叠状态变化，例如桌面折叠状态或图书折叠状态；以及屏幕密度和字体大小的变化。

不仅仅是拉伸或收缩界面元素窗口大小，自适应应用会取代布局组件并显示或隐藏内容。例如，在标准手机上，自适应应用可能会显示底部但在大屏设备上则是侧边导航栏。在大屏设备上自适应应用会显示更多内容，例如双窗格列表-详情布局；已开启小屏幕，内容较少，不论是列表还是详情。

#### 为什么要构建自适应界面？

用户希望应用在所有设备上都能完美运行，并且能够提供针对大屏设备的增强功能用户在多窗口模式下处理多项任务提升应用体验和工作效率。

应用只能在标准手机上执行单一任务，无法满足用户需求不断增加各种可能性的基础。

#### 如何开始使用

从规划到开发，在应用开发的所有阶段都要考虑自适应设计部署。向平面设计师介绍自适应设计。在设计应用时将构建一款易于管理、可扩展。

如果创建支持所有显示屏尺寸和配置的自适应应用，请执行以下操作：

* 使用窗口大小类型来决定布局

* 使用 Compose Material 3 Adaptive 库进行构建

* 支持除触控之外的输入

* 在所有设备类型上测试

##### 窗口大小类别

不同设备或同一设备上的应用窗口尺寸可以不同、不同的设备屏幕方向会产生不同的宽高比。在多窗口模式、应用窗口大小、宽高比和屏幕方向与设备屏幕不同。

自适应应用可简化和泛化确定和管理窗口大小、宽高比和屏幕方向（）

###### 内容窗口

##### Compose Material 3 自适应

###### 配置和连续性

###### 状态

##### 触控以外的输入

#### 如何测试自适应应用

##### 大屏设备应用质量指南

##### 多个配置

##### 主机端屏幕截图

##### Compose 预览

##### 模拟器

##### 远程设备流式传输



### 物料清单

## 界面架构

## 应用布局

## 组件

## 主题

## 文本和排版

## 图片和图形

## 动画

## 无障碍

## 轻触和输入

## 效果

## 样式指南

## 界面测试

## 迁移到 Compose

## 工具

## 利用系统功能

## 创建 widget

## 参考文章

[Jetpack Compose 使用入门 &nbsp;|&nbsp; Android Developers](https://developer.android.com/develop/ui/compose/documentation?hl=zh-cn)
