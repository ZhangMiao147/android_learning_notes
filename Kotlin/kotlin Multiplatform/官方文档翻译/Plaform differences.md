# Default UI behavior on different platforms 不同平台上的默认UI行为

Compose Multiplatform 旨在帮助你创建的应用在不同平台上行为尽可能的相似。在这篇文章，你可以了解到关于使用 Compose Multiplatform 在不同的平台上编写共享 ui 代码时，不可避免的差异或临时的妥协。

## 项目结构

无论你针对哪个平台，每个平台都需要一个专门的入口点：

* 对于 Android，它是 Activity，它的作用是从通用代码中显示主要的可组合组件。

* 对于 iOS 应用，它是初始化应用的 @main 类或结构。

* 对于一个 JVM 应用，它是启动应用程序的 main() 函数，它会启动主要的公共可组合组件。

* 对于一个 Kotlin/JS 或 Kotlin/Wasm 应用，它是 main() 函数，它将主要的公共代码组合起来添加到网页中。

您的应用程序所需的某些特定于平台的 API 可能不支持多平台，因此您需要在特定平台的源代码集中实现对这些 API 的调用。在此之前，建议查看  [klibs.io](https://klibs.io/)，这是一个 JetBrains 项目，旨在全面收录所有可用的 Kotlin Multiplatform 库。目前已经有可用于网络代码、数据库、协程等诸多方面的可使用的库。

## 输入法

### 软件键盘

每个平台对软件键盘的处理方式可能略有不同，包括文本区域激活时键盘的显示方式。

Compose Multiplatform 采用 [Compose window insets approach](https://developer.android.com/develop/ui/compose/system/insets)，并在 iOS 上进行模拟它以确保  [safe areas](https://developer.apple.com/documentation/UIKit/positioning-content-relative-to-the-safe-area) 。依赖于你的具体实现，软件键盘在 iOS 上的位置可能有一点不同。务必检查在两个平台上键盘是否遮挡了重要的 UI 元素。

### 触摸和鼠标支持

当前的桌面实现会讲所有指针操作解释为鼠标手势，因此不支持多点触控手势。例如，常见的捏合缩放手势不能在 Compose Multiplaform 桌面版中实现，因为它需要同时处理两个触摸点。

## UI 行为和外观

### 平台特定功能

Compose Multiplaform 没有覆盖一些常见的 UI 元素，并且不能使用框架自定义。因此，在不同平台上你应该预料到他们看起来不同。

原生的弹窗视图就是一个例子：当你在 Compose Multiplaform 文本字段中选择一个文本时，默认建议操作像复制或翻译会根据程序运行的平台而有所不同。

### 滚动物理效果

对于 Android 和 iOS，滚动手感与平台保持一致。对于 desktop，滚动支持仅限于鼠标滚轮（如“触摸与鼠标支持”部分所述）。

### 互操作视图

如果你想在通用可组合组件中嵌入原生视图，或者反之亦然，你需要熟悉 Compose Multiplaform 支持的特定平台机制。

对于 iOS，有与  [SwiftUI](https://kotlinlang.org/docs/multiplatform/compose-swiftui-integration.html) 和 [UIKit](https://kotlinlang.org/docs/multiplatform/compose-uikit-integration.html) 互操作代码的单独指南。

对于桌面，Compose Multiplaform 提供  [Swing interoperability](https://kotlinlang.org/docs/multiplatform/compose-desktop-swing-interoperability.html) 。

### 返回手势

Android 设备默认支持返回收支，并且每个屏幕都会以某个方式响应返回按钮。

iOS 默认没有返回手势，不过鼓励开发者实现相似的功能以满足用户体验需求。Compose Multiplaform 对于 iOS 默认支持返回手势，以模拟 Android 功能。

在桌面端，Compose Multiplaform 使用 Esc 健作为默认返回触发键。

详情请查看  [Back gesture](https://kotlinlang.org/docs/multiplatform/compose-navigation.html#back-gesture) 部分。

### 文本

对于文本，Compose Multiplaform 无法保证在不同平台之间像素完美匹配。

如果你没有显式设置字体，每个系统会为你的文本制定一个不同的默认字体。

即使是同一个字体，每个平台特有的字母锯齿机制（letter aliasing mechanisms）也可能导致明显的差异。

这不会对用户体验有重大影响。相反，默认字体在每个平台上都能正常显示。但是，像素差异可能会干扰屏幕截图测试等操作。

## 开发者经验

### 预览

预览是 IDE 中可组合组件可用的非交互式布局展示。

要查看可组合组件的预览：

1. 如果项目中没有一个 Android 目标（target），请添加一个（预览机制使用 Android 库）。
   
   注意：从 Compose Multiplaform 1.10.0 版本开始，你可以在 commonMain 源集中为所有目标平台使用 @Preview 注解。

2. 在通用代码中使用 @Preview 注解标记你想要预览的可组合组件。

3. 在编辑窗口切换到 Split 或 Design 视图。如果你尚未创建项目，系统会提醒你首次构建。

在 IntelliJ IDEA 和 Android Studio 中，你可以在当前文件中看到每一个被 @Preview 注解的可组合组件的初始界面。

### 热加载

热加载指应用程序无需额外输入即可动态反映代码更改。在 Compose Multiplaform，热加载仅适用于 JVM(desktop) 目标。不过，你可以使用它快速排查问题，然后切换到目标平台进行微调。

要了解更多，查看 [Compose Hot Reload](https://kotlinlang.org/docs/multiplatform/compose-hot-reload.html) 文章。

## 接下来是什么

阅读更多关于一下组件的 Compose Multiplaform 实现：

- [Resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html)

- [Lifecycle](https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html)

- [Common ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html)

- [Navigation and routing](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
