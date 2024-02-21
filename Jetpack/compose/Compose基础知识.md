# Jetpack Compose 基础知识

Jetpack Compose 是一款新型工具包，旨在帮助简化界面开发。该工具包将响应式编程模型与简洁易用的 Kotlin 编程语言相结合，并采用完全声明式的代码编写方式，让您可以通过调用一系列函数来描述界面，这些函数会将数据转换为页面层次结构。当底层数据发生变化时，框架会自动重新执行这些函数，为您更新界面层次结构。

Compose 应用由可组合函数构成。可组合函数即带有 @Composable 标记的常规函数，这些函数可以调用其他可组合函数。使用一个函数就可以创建一个新的界面组件。该注解会告知 Compose 为函数添加特殊支持，以便后续更新和维护界面。借助 Compose，您可以将代码设计成多个小代码块。可组合函数通常简称为 “可组合项”。

通过创建可重用的小型可组合项，可以轻松构建应用中所用界面元素的库。每个可组合项对应于屏幕的一个部分，可以单独修改。

“界面组件”、“可组合函数”和“可组合项” 几个术语可以互换使用来指代同一个概念。

配置项目时需要确保选择的 minimumSdkVersion 至少为 API 级别 21，这是 Compose 支持的最低 API 级别。

## Compose 使用入门

### 可组合函数

可组合函数是带有 @Compose 注解的常规函数。这类函数自身可以调用其他 @Compose 函数。Text 是由库提供的可组合函数。

```kotlin
@Composable
private fun Greeting(name: String) {
   Text(text = "Hello $name!")
}
```

### Android 应用中的 Compose

使用 Compose 时，Activity 仍然是 Android 应用的入口点。在项目中，用户打开应用时会启动 MainActivity（如 AndroidManifest.xml 文件中所指定）。使用 setContent 来定义布局，但不同于在传统 View 系统中使用 XML 文件，将在该函数中调用可组合函数。

```kotlin
class MainActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			BasicsCodelabTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				){
					Greeting("Android")
				}
			}
		}
	}
}
```

BasicsCodelabTheme 是为可组合函数设置样式的一种方式。如需查看文本在屏幕上的显示效果，可以在模拟器或设备上运行应用，或使用 Android Studio 预览进行查看。

若要使用 Andriod Studio 预览，只需使用 @Preview 注解标记所有无参数可组合函数或采用模型形参的函数，然后构建项目即可。现在 MainActivity.kt 文件中已经包含了一个 Preview Composable 函数。可以在同一个文件中包含多个预览，并为它们指定名称。

```kotlin
@Preview(showbackground = true, name = "Text preview")
@Compose
fun DefaultPreview(){
	BasicsCodelabTheme {
		Greeting(name = "Android")
	}
}
```

在项目中导入与 Jetpack Compose 相关的类时，请从以下位置导入：

* androidx.compose.* (针对编译器和运行时类)
* androidx.compose.ui.* （针对界面工具包和库）

### 微调界面

首先，为 Greeting 设置不同的背景颜色。为此，可以用 Surface 包围 Text 可组合项。Surface 会采用一种颜色，因此请使用 MaterialTheme.colorScheme.primary。

Surface 和 MaterialTheme 是与 Material Design 相关的概念。Material Design 是 Google 提供的一个设计系统，旨在帮助您构建界面和体验。

```kotlin
@Composabel
private fun Greeting(name: String) {
	Surface(color = MaterialTheme.colorScheme.primary) {
		Text (text = "Hello $name!")
	}
}
```

嵌套在 Surface 内的组件将在该背景颜色之上绘制。

Material 组件（例如 androidx.compose.material3.Surface）旨在提供应用中可能需要的常见功能（例如为文本选择适当的颜色），获得更好的体验。之所以说 Material 很实用，是因为它提供在大多数应用中都会用到的实用默认值和模式。Compose 中的 Material 组件是在其他基础组件（位于 androidx.compose.fundation 中）的基础上构建的。如果需要更高的灵活性，也可以从您的应用组件中访问这些组件。

在这种情况下，Surface 会了解，当该背景设置为 primary 颜色后，其上的任何文本都应使用 onPrimary 颜色，此颜色也在主题中进行了定义。

### 修饰符

大多数 Compose 界面元素（例如 Surface 和 Text）都接受可选的 modifier 参数。修饰符会指示界面元素如何在其父布局中放置、显示或表现。

例如，padding 修饰符会在其修饰的元素周围应用一定的空间。可以使用 Modifier.padding() 创建内边距修饰符。

有数十种修饰符可用于实现对齐、添加动画、设置布局、使可点击或可滚动以及转换等效果。[C ompose 修饰符列表](https://developer.android.com/jetpack/compose/modifiers-list?hl=zh-cn)

### 重复使用可组合项

添加到界面的组件越多，创建的嵌套层级就越多，如果函数变得非常大，可能会影响可读性。通过创建可重用的小型组件，可以轻松构建应用中所用界面元素的库。每个组件对应于屏幕的一个部分，可以单独修改。

最佳实践是，函数应包含一个修饰符参数，系统默认为该参数分配空修饰符。将此修饰符转发到在函数内调用的第一个可组合项。这样，调用点就可以在可组合函数之外调整布局指令和行为了。

创建一个名为 MyApp 的可组合项，该组合项中包含问候语。

```kotlin
@Composable
private fun MyApp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Greeting("Android")
    }
}
```

这样一来，由于现在可以重复使用 MyApp 可组合项，就可以省去 onCreate 回调和预览，从而避免重复编写代码。

### 创建列和行

Compose 中的三个基本准备布局元素是 Column、Row 和 Box 可组合项。

![](img/标准布局元素.png)

它们是接受可组合内容的可组合函数，因此可以在其中放置项目。例如，Column 中的每个子集都将垂直放置。

```kotlin
// Don't copy over
Column {
    Text("First row")
    Text("Second row")
}
```

#### Compose 和 Kotlin

可组合函数可以像 Kotlin 中的其他函数一样使用。这会使洁面构建变得非常有效，因为可以添加语句来影响界面的显示方式。

例如，可以使用 for 循环向 Column 中添加元素：

```kotlin
@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    names: List<String> = listOf("World", "Compose")
) {
    Column(modifier) {
        for (name in names) {
            Greeting(name = name)
        }
    }
}
```

修饰符在 Compose 中使用得非常广泛，尝试使用 fillMaxWidth 和 padding 修饰符：

```kotlin
@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    names: List<String> = listOf("World", "Compose")
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        for (name in names) {
            Greeting(name = name)
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(text = "Hello, ")
            Text(text = name)
        }
    }
}
```

请注意：

* 修饰符可以包含重载，因为具有相应的优势，例如可以指定不同的方式来创建内边距。
* 若要向一个元素添加多个修饰符，只需要将它们链接起来即可。

#### 添加按钮

Button 是 material3 软件包提供的一种可组合项，它采用可组合项作为最后一个参数。由于尾随 lambda 可以移到括号之外，因此可以向按钮添加任何内容作为子极，例如 Text：

```kotlin
// Don't copy yet
Button(
    onClick = { } // You'll learn about this callback later
) {
    Text("Show less")
}
```

Compose 根据 [Material Design 按钮规范]提供了不同类型的 Button：Button、Elevatedbutton、FilledTonalButton、OutlinedButton 和 TextButton。

想要在行尾放置可组合项，由于没有 alignEnd 修饰符，因此需要在开始时为该可组合项赋予一定的 weight。weight 修饰符会让元素填满所有可用空间，使其“具有弹性”，也就是会推开其他没有权重的元素（即“无弹性”元素）。该修饰符还会使 fillMaxWidth 修饰符变得多余。

### Compose 中的状态

在了解如何使按钮可点击以及如何调整内容大小之前，需要在某个位置存储某个值，用来指示每项内容是否展开（即内容的状态）。由于需要为每条问候语设定这两个值之一，因此其逻辑位置位于 Greeting 可组合项中，看看此 expanded 布尔值及其在代码中的使用方式：

```kotlin
// Don't copy over
@Composable
private fun Greeting(name: String) {
    var expanded = false // Don't do this!

    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Hello, ")
                Text(text = name)
            }
            ElevatedButton(
                onClick = { expanded = !expanded }
            ) {
                Text(if (expanded) "Show less" else "Show more")
            }
        }
    }
}
```

但是，此设置无法按预期发挥作用。为 expanded 变量设置不同的值不会使 Compose 将其检测为状态更改，因此不会产生任何效果。

Compose 应用通过调用可组合函数将数据转换为界面。如果数据发生变化，Compose 会使用新数据重新执行这些函数，从而创建更新后的界面，此过程称为重组。Compose 还会查看各个可组合项需要哪些数据，以便只需重组数据发生了变化的组件，而避免重组未受影响的组件。

可组合函数可以按任意顺序频繁执行，因此不能以代码的执行顺序或该函数的重组次数为判断依据。

更改此变量不会触发重组的原因是 Compose 并未跟踪此更改。此外，每次调用 Greeting 时，都会将该变量重置为 false。

如需向可组合项添加内部状态，可以使用 mutableStateOf 函数，该函数可让 Compose 重组读取该 State 的函数。

State 和 MutableState 是两个接口，它们具有特定的值，每当该值发生变化时，它们就会触发界面更新（重组）。

但是，不能只是将 mutableStateOf 分配给可组合项中的某个变量。如前所述，重组可能会随时发生，这会在此调用可组合项，从而将状态重置为值为 false 的新可变状态。

如需在重组后保留状态。请使用 remember 记住可变状态。

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
...

@Composable
fun Greeting() {
	val expanded = remember { mutableStateOf(false) }
	...
}
```

remember 可以起到保护作用，防止状态在重组时被重置。

请注意，如果从屏幕的不同部分调用同一可组合项，则会创建不同的界面元素，且每个元素都会拥有自己的状态版本。可以将内部状态视为类中的私有变量。

可组合函数会自动 “订阅”状态。如果状态发生变化，读取这些字段的可组合项将会重组以显示更新。

#### 更改状态和响应状态更改

为了更改状态，Button 具有一个名为 onClick 的形参，但它不接受值，而接受函数。

可以通过为 “onClick” 指定 lambda 表达式，定义点击时将执行的操作。例如，切换展开状态的值，并根据该值显示不同的文本。

```kotlin
ElevatedButton(
	onClick = {expanded.value = !expanded.value}
) {
	Text(if(expanded.value) "Show less" else "Show more")
}
```

如果在模拟器中运行应用，会看到点击该按钮时，expanded 会切换，从而触发重组该按钮内的文本。每个 Greeting 都具有自己的展开状态，因为它们属于不同的界面元素。

```kotlin
@Composable
private fun Greeting(name: String) {
	val expanded = remember { mutableStateOf(false) }
	
	Surface(
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
	) {
		Row(modifier = Modifier.padding(24.dp)) {
			Column(modifier = Modifier.weight(1f)) {
				Text(text = "Hello, ")
				Text(text = name)
			}
			ElevatedButton(
				onClick = { expanded.value = !expanded.value }
			) {
				Text(if (expanded.value) "Show less" else "Show more")
			}
		}
	}
}
```

#### 展开内容

添加一个依赖于状态的额外变量：

```kotlin
@Composable
private fun Greeting(name: String) {
	val expanded = remember { mutableStateOf(false) }
	
	val extraPadding = if (expanded.value) 48.dp else 0.dp
	
	...
}
```

无需在重组后记住 extraPadding，因为它仅执行简单的计算。

现在可以将新的内边距修饰符应用于 Column：

```kotlin
@Composable
private fun Greeting(name: String) {
	val expanded = remember { mutableStateOf(false) }
	
	val extraPadding = if (expanded.value) 48.dp else 0.dp
	
	Surface(
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
	){
		Row(modifier = Modifier.padding(24.dp)) {
			Column(modifier = Modifier
												.weight(1f)
												.padding(bottom = extraPadding)
			){
				Text(text = "Hello, ")
				Text(text = name)
			}
			ElevatedButton(
				onClick = { expanded.value = !expanded.value }
			){
				Text(if(expanded.value) "Show less" else "Show more")
			}
		}
	}
}
```

### 状态提升

在可组合函数中，被多个函数读取或修改的状态应位于共同祖先实体中，此过程称为状态提升。“提升”意为“提高”或“升级”。

使状态可提升，可以避免复制状态和引入 bug，有助于重复使用可组合项，并大大降低可组合项的测试难度。相反，不需要由可组合项的父级控制的状态则不应该被提升。可信来源属于该状态的创建者和控制者。

例如，为应用创建一个初始配置界面。

将以下代码添加到 MainActivity.kt：

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
// ...

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    // TODO: This state should be hoisted
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Basics Codelab!")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = { shouldShowOnboarding = false }
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    BasicsCodelabTheme {
        OnboardingScreen()
    }
}
```

此代码包含多个新功能：

* 您已经添加了一个名为 OnboardingScreen 的新可组合项以及一个新的预览。构建项目时，会发现可以同时拥有多个预览。还添加了一个固定高度，以验证内容是否正确对齐。
* 可以配置 Column，使其在屏幕中心显示其内容。
* shouldShowOnboarding 使用的是 by 关键字，而不是 =。这是一个属性委托，可以无需每次都输入 .value 。
* 点击该按钮时，会将 shouldShowOnboarding 设为 false，尽管并未从任何位置读取该状态。

现在，即可将这个新的初始配置屏幕添加到应用。希望该屏幕在应用启动时显示，然后在用户按 “继续” 时隐藏。

在 Compose 中，不会隐藏界面元素，因此不会将它们添加到组合中，因此它们也不会添加到 Compose 生成的界面树中。只需要使用简单的 Kotlin 田间逻辑就可以做到这一点。例如，如需显示初始配置屏幕或问候语列表，需要执行以下操作：

```kotlin
// Don't copy yet
@Composable
fun MyApp(modifier: Modifier = Modifier) {
    Surface(modifier) {
        if (shouldShowOnboarding) { // Where does this come from?
            OnboardingScreen()
        } else {
            Greetings()
        }
    }
}
```

但是，无法访问 shouldShowOnboarding。很明显，需要与 MyApp 可组合项共享在 OnboardingScreen 中创建的状态。

我们不会以某种方式与状态的父级共享状态值，而是会提升该状态，也就是将该状态移到需要访问它的共同祖先实体中。

首先，将 MyApp 的内容移到名为 Greetings 的新可组合项中。此外，调整预览以改为调用 Greetings 方法：

```kotlin
@Composable
fun MyApp(modifier: Modifier = Modifier) {
     Greetings()
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = listOf("World", "Compose")
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        for (name in names) {
            Greeting(name = name)
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingsPreview() {
    BasicsCodelabTheme {
        Greetings()
    }
}
```

为新的顶级 MyApp 可组合项添加预览，以便测试其行为：

```kotlin
@Preview
@Composable
fun MyAppPreview() {
    BasicsCodelabTheme {
        MyApp(Modifier.fillMaxSize())
    }
}
```

现在，添加相应的逻辑来显示 MyApp 中的不同屏幕，并提升状态。

```kotlin
@Composable
fun MyApp(modifier: Modifier = Modifier) {

    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Surface(modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(/* TODO */)
        } else {
            Greetings()
        }
    }
}
```

还需要与初始配置屏幕共享 shouldShowOnboarding，但不会直接传递它。与其让 OnboardingScreen 更改状态，不如让它在用户点击 “Continue” 按钮时通知我们。

如何向上传递事件？通过向下传递回调来传递。回调时这样一类函数，它们以实参的形式传递给其他函数，并在事件发生时执行。

尝试向初始配置屏幕添加定义为 onContinueClicked: () -> Unit 的函数参数，以便可以从 MyApp 更改状态。

解决方案：

```kotlin
@Composable
fun MyApp(modifier: Modifier = Modifier) {

    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Surface(modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { shouldShowOnboarding = false })
        } else {
            Greetings()
        }
    }
}

@Composable
fun OnboardingScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Basics Codelab!")
        Button(
            modifier = Modifier
                .padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text("Continue")
        }
    }

}
```

通过向 OnboardingScreen 传递函数而不是状态，可以提高该可组合项的可重用性，并防止状态被其他可组合项更改。一般而言，这可以让事情变得简单。一个很好的例子就是，现在需要如何修改初始配置屏幕预览来调用 OnboardingScreen：

```kotlin
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    BasicsCodelabTheme {
        OnboardingScreen(onContinueClicked = {}) // Do nothing on click.
    }
}
```

将 onContinueClicked 分配给空 lambda 表达式就等于“什么也不做”，这非常适合于预览。

在 MyApp 可组合项中，首次使用了 by 属性委托，以比啊面每次都使用值。不妨也在 expanded 属性的 Greeting 可组合项中使用 by （而非 =）。务必要将 expanded 从 val 更改为 var。

### 创建高效延迟列表

到目前为止，已经在 Column 中显示了两条问候语。但是，它可以处理成千上万条问候语吗？

更改 Greetings 形参中的默认列表值以使用其他列表构造函数，这可以设置列表的大小病使用其 lambda 中包含的值来填充列表（这里的 $it 代表列表索引）：

```kotlin
names: List<String> = List(1000) { "$it" }
```

这会创建 1000 条问候语，即使屏幕上放不下这些问候语。显然，这样做效果并不好。可以尝试在模拟器上运行此代码（警告：此代码可能会使模拟器卡住）。

为显示可滚动列，需要使用 LazyColumn。LazyColumn 只会渲染屏幕上可见的内容，从而在渲染大型列表时提升效率。

注意：LazyColumn 和 LazyRow 相当于 Android View 中的 RecyclerView。

在其基本用法中，LazyColumn API 会在其作用域内提供一个 items 元素，并在该元素中编写各项内容的渲染逻辑：

```kotlin
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// ...

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = List(1000) { "$it" }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}
```

注意：请确保导入 androids.compose.foundation.lazy.items，因为 Android Studio 默认会选择另一个 items 函数。

注意：LazyColumn 不会像 RecyclerView 一样回收其子极。它会在滚动它时发出新的可组合项，并保持高效运行，因为与实例化 Android Views 相比，发出可组合项的成本相对较低。

### 保留状态

应用存在两个问题：

**保留初始配置界面状态**

如果在设备上运行应用，点击按钮，然后旋转，系统会在此显示初始配置界面。remember 函数仅在可组合项包含在组合中时起作用。旋转屏幕后，这个 activity 都会重启，所有状态都将丢失。当发生任何配置更改或者进程终止时，也会出现这种情况。

可以使用 rememberSaveable，而不使用 remember。这会保存每个在配置更改（或旋转）和进程终止后保留下来的状态。

现在，将 shouldShowOnboarding 中的 remember 替换为 rememberSaveable：

```kotlin
    import androidx.compose.runtime.saveable.rememberSaveable
    // ...

    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }
```

运行应用，旋转屏幕，更改为深色模式，或者终止进程。除非之前退出了应用，否则系统不会显示初始配置界面。

**保持列表项的展开状态**

如果展开某个列表项并滚动列表直至该项不在视野范围内，或者旋转设备并返回到展开的项，会看到该项现已恢复为初始状态。

解决方法是也为展开状态使用 rememberSaveable：

```kotlin
var expanded by rememberSaveable { mutableStateOf(false) }
```

### 为列表添加动画效果

在 Compose 中，有多种方式可以为界面添加动画效果：从用于添加简单动画的高阶 API 到用于实现完全控制和复杂过度的低阶方法，不一而足。可以在[文档](https://developer.android.com/jetpack/compose/animation/introduction?hl=zh-cn)中了解相关信息。

使用 animateDpAsState 可组合项为已经实现的尺寸变化添加动画效果。该可组合项会返回一个 State 对象，该对象的 value 会被动画持续更新，直到动画播放完毕。该可组合项需要一个类型为 Dp 的 “ 目标值 ”。

创建一个依赖于展开状态的动画 extraPadding。

```kotlin
import androidx.compose.animation.core.animateDpAsState

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    val extraPadding by animateDpAsState(
        if (expanded) 48.dp else 0.dp
    )
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding)
            ) {
                Text(text = "Hello, ")
                Text(text = name)
            }
            ElevatedButton(
                onClick = { expanded = !expanded }
            ) {
                Text(if (expanded) "Show less" else "Show more")
            }

        }
    }
}
```

运行应用并查看该动画的效果。

注意：如果展开第一项内容，然后滚动到第 20 项内容，再返回到第一项内容，会发现第一项内容已经恢复为原始尺寸。如果需要，可以使用 rememberSaveable 保存此数据。

animateDpAsState 接受可选的 animationSpec 参数供您自定义动画。来做一些更有趣的尝试，比如添加基于弹簧的动画：

```kotlin
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    val extraPadding by animateDpAsState(
        if (expanded) 48.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
    // ...
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding.coerceAtLeast(0.dp))

    // ...

    )
}
```

请注意，还要确保内边距不会为负数，否则可能会导致应用崩溃。这会引入一个席位的动画 bug，稍后修复。

Spring 规范不接受任何与事件有关的参数。它仅依赖于物理属性（阻尼和刚度），使动画更自然。立即运行该应用。

使用 `animate*AsState` 创建的任何动画都是可中断的。这意味着，如果目标值在动画播放过程中发生变化，`animate*AsState` 会重启动画并指向新值。中断在基于弹簧的动画中看起来有其自然。

如果想探索不同类型的动画，请尝试为 spring 提供不同的参数，尝试使用不同的规范（tween、repeatable）和不同的函数（animateColorAsState 或[不同类型的动画API](https://developer.android.com/jetpack/compose/animation/introduction?hl=zh-cn)）。

### 设置应用的样式和主题

如果打开 ui/theme/Theme.kt 文件，会看到 BasicsCodelabTheme 在其实现中使用了 MaterialTheme：

```kotlin
// Do not copy
@Composable
fun BasicsCodelabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // ...

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

MaterialTheme 是一个可组合函数，体现了 [Material Design 规范](https://m3.material.io/) 中的样式设置原则。样式设置信息会逐级向下传递到位于其 content 内的组件，这些组件会读取该信息来设置自身的样式。在界面中已经使用了 BasicsCodelabTheme，如下所示：

```kotlin
    BasicsCodelabTheme {
        MyApp(modifier = Modifier.fillMaxSize())
    }
```

由于 BasicsCodelabTheme 将 MaterialTheme 包围在其内部，因此 MyApp 会使用该主题中定义的属性来设置样式。从任何后代可组合项中都可以检索 MaterialTheme 的三个属性：colorScheme、typography 和 shapes。使用它们设置其中一个 Text 的标题样式：

```kotlin
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding.coerceAtLeast(0.dp))
            ) {
                Text(text = "Hello, ")
                Text(text = name, style = MaterialTheme.typography.headlineMedium)
            }
```

上例中的 Text 可组合项会设置新的 TextStyle。可以创建自己的 TextStyle，也可以使用 MaterialTheme.typography 检索由主题定义的样式（首选）。此结构支持访问由 Material 定义的文本样式，例如 displayLarge，headlineMedium，titleSmall，bodyLarge，labelMedium 等。在本例中，将使用主题中定义的 headlineMedium 样式。

通常来说，最好是将颜色、样式和字体样式放在 MaterialTheme 中。例如，如果对颜色进行硬编码，将会很难实现深色模式，并且需要进行大量修正工作，而这很容易造成错误。

不过，有时除了选择颜色和字体样式，还可以基于现有的颜色和样式进行设置。

为此，可以使用 copy 函数修改预定义的样式。将数字加粗：

```kotlin
import androidx.compose.ui.text.font.FontWeight
// ...
Text(
	text = name,
	style = MaterialTheme.typography.headlineMedium.copy(
					fontWeight = FontWeight.ExtraBold				
	)
)
```

这样一来，如果需要更改 headlineMedium 的字体系列或其他任何属性，就不必担心出现细微偏差了。

**设置深色模式预览**

目前，预览仅会显示应用在浅色模式下的显示效果。使用 UI_MODE_NIGHT_YES 向 GreetingPreview 添加额外的 @Preview 注解：

```kotlin
import android.content.res.Configuration.UI_MODE_NIGHT_YES

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "GreetingPreviewDark"
)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingPreview() {
    BasicsCodelabTheme {
        Greetings()
    }
}
```

**微调应用的主题**

可以在 ui/theme 文件夹内的文件中找到与当前主题相关的所有内容。例如，到目前为止所使用的默认颜色均在 Color.kt 中定义。

首先，来定义新的颜色，将以下代码添加到 Color.kt 中：

```kotlin
val Navy = Color(0xFF073042)
val Blue = Color(0xFF4285F4)
val LightBlue = Color(0xFFD7EFFE)
val Chartreuse = Color(0xFFEFF7CF)
```

现在，将这些颜色分配给 Theme.kt 中的 MaterialTheme 的调色板：

```kotlin
private val LightColorScheme = lightColorScheme(
	surface = Blue,
	onSurface = Color.White,
	primary = LightBlue,
	onPrimary = Navy
)
```

如果返回 MainActivity.kt 并刷新预览，预览颜色实际上并不会改变！这是因为，预览将默认使用[动态配色](https://m3.material.io/styles/color/dynamic/choosing-a-source) 。可以在 Theme.kt 中查看使用 dynamicColor 布尔值参数添加动态配色的逻辑。

如需查看非自适应版本的配色方案，请在 API 级别低于 31 （对应引入了自适应配色的 Android S） 的设备上运行应用，将会看到新颜色。

在 Theme.kt 中，定义针对深色的调色板：

```kotlin
private val DarkColorScheme = darkColorScheme(
    surface = Blue,
    onSurface = Navy,
    primary = Navy,
    onPrimary = Chartreuse
)
```

当运行应用时，会看到深色的实际效果。

Theme.kt 的最终代码：

```kotlin
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    surface = Blue,
    onSurface = Navy,
    primary = Navy,
    onPrimary = Chartreuse
)

private val LightColorScheme = lightColorScheme(
    surface = Blue,
    onSurface = Color.White,
    primary = LightBlue,
    onPrimary = Navy
)

@Composable
fun BasicsCodelabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 其他

**用图标替换按钮**

* 将 IconButton 可组合项与子级 Icon 结合使用。
* 使用 material-icon-extended 工作中提供的 Icons.Filled.ExpandLess 和 Icons.Filled.ExpandMore。将以下代码行添加到 app/build.gradle.kts 文件中的依赖项中。

```xml
implementation("androidx.compose.material:material-icons-extended")
```

* 修改内边距以修正对齐问题。
* 为无障碍功能添加内容说明。

**使用字符串资源**

应该为 “Show more” 和 "show less" 提供内容说明，可以通过简单的 if 语句进行添加：

```kotlin
contentDescription = if (expanded) "Show less" else "Show more"
```

不过，硬编码字符串的方式并不可取，应该从 strings.xml 文件中获取字符串。

可以通过对每个字符串使用 "Extract string resource"（在 Andriod Studio 中的 “Context Actions” 中提供）来自动执行此操作。

或者，打开 app/src/res/values/strings.xml 并添加以下资源：

```kotlin
<string name="show_less">Show less</string>
<string name="show_more">Show more</string>
```

**展开**

“Composem ipsum” 文字会在显示后消失，触发每张卡片的大小变化。

* 将新的 Text 添加到 Greeting 中当内容展开时显示的 Column 中。
* 移除 extraPadding 并改为将 animateContentSize 修饰符应用于 Row。这会自动执行创建动画的过程，而手动执行该过程会很困难。此外，也不需要再使用 coerceAtLeast。

**添加高度和形状**

* 可以结合使用 shadow 修饰符和 clip 修饰符来实现卡片外观。不过，有一种 Material 可组合项也可以做到这一点：Card。可以通过调用 CardDefaults.cardColors 并覆盖想要更改的颜色，以此来更改 Card 的颜色。

#### 最终代码

```kotlin
package com.example.basicscodelab

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.basicscodelab.ui.theme.BasicsCodelabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicsCodelabTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }

    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { shouldShowOnboarding = false })
        } else {
            Greetings()
        }
    }
}

@Composable
fun OnboardingScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Basics Codelab!")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = List(1000) { "$it" }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        CardContent(name)
    }
}

@Composable
private fun CardContent(name: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Text(text = "Hello, ")
            Text(
                text = name, style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (expanded) {
                Text(
                    text = ("Composem ipsum color sit lazy, " +
                        "padding theme elit, sed do bouncy. ").repeat(4),
                )
            }
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Filled.ExpandLess else Filled.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.show_less)
                } else {
                    stringResource(R.string.show_more)
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "GreetingPreviewDark"
)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingPreview() {
    BasicsCodelabTheme {
        Greetings()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    BasicsCodelabTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}

@Preview
@Composable
fun MyAppPreview() {
    BasicsCodelabTheme {
        MyApp(Modifier.fillMaxSize())
    }
}
```



## 参考文章

[Jetpack Compose 基础知识](https://developer.android.com/codelabs/jetpack-compose-basics?hl=zh#0)
