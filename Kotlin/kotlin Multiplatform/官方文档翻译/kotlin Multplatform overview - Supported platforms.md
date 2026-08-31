# 支持的平台

## 支持平台的稳定性

kotlin Multiplatform 允许你为各个平台创建应用并在他们之间共享代码，所以能在用户最喜欢的设备上联系他们。基于 Kotlin Multiplatform 核心技术代码共享和 Compose Multiplatform UI 框架，不同的平台也许具有不同程度的稳定性。

此页面包含的信息帮助您确定哪些平台符合您的项目需求，以及它们的稳定性级别的详细信息。

## 常规的 Kotlin 稳定性级别

下面是 Kotlin 稳定性级别和其含义的快速指南：

**Experimental** 意思是“只在玩具项目中尝试”

* 我们只是尝试一个想法，并且希望一些用户能够尝试并给出反馈。如果不成功，我们可能随时放弃它。

**Alpha** 意思是“使用风险自负，预计会出现迁移问题”

* 我们打算将这个想法产品化，但它还没有达到最终形态。

**Beta**意思是“你可以使用它，我们将尽最大的努力为你减少迁移问题”

* 它几乎完成了，用户现在的反馈尤其重要。

* 尽管如此，它还没有100%完成，所以可以进行更改（包括基于您自己的反馈的更改）。

* 提前注意弃用警告，以获得最佳更新体验。
  
  我们统称Experimental、Alpha和Beta为 pre-stable 水平。

**Stable**意思是“在最保守的情况下使用它”

* 它完成了，我们将根据严格的向后兼容性规则对其进行改进。

### 核心 Kotlin Multiplatform 技术的当前平台稳定性级别

下面是核心 Kotlin Multiplatform 技术的当前平台稳定性级别：

| Platform                 | Stability level |
| ------------------------ | --------------- |
| Android                  | Stable          |
| iOS                      | Stable          |
| Desktop (JVM)            | Stable          |
| Server-side (JVM)        | Stable          |
| Web based on Kotlin/Wasm | Beta            |
| Web based on Kotlin/JS   | Stable          |
| watchOS                  | Beta            |
| tvOS                     | Beta            |

* Kotlin Multiplatform 支持的原生平台比这里还要多。要了解它们各自的支持级别，查看 [Kotlin/Native target support](https://kotlinlang.org/docs/native-target-support.html).

* 有关 Kotlin 组件（如 Kotlin Multiplatform）稳定性级别的更多信息，查看 [Current stability of Kotlin components](https://kotlinlang.org/docs/components-stability.html#current-stability-of-kotlin-components).

## Compose Multiplatform UI 框架稳定性水平

下面是 Compose Multiplatform UI 框架的平台稳定性级别和其含义的快速指南：

**Experimental** 意思是“正在开发中”

- 有些功能可能还不可用，并且现有的能通可能有性能问题或错误。
- 未来可能会有变化，突破性的变化可能会经常发生。

**Alpha** 意思是“使用风险自负，预计会出现迁移问题”

- 我们打算将平台支持产品化，但它还没有达到最终形态。

**Beta**意思是“你可以使用它，我们将尽最大的努力为你减少迁移问题”

- 它几乎完成了，用户现在的反馈尤其重要。

- 尽管如此，它还没有100%完成，所以可以进行更改（包括基于您自己的反馈的更改）。
  
  我们统称Experimental、Alpha 和 Beta 为 pre-stable 水平。

**Stable**意思是“在最保守的情况下使用它”

- 该框架提供了一个全面的 API 表面（surface），允许您编写漂亮的、可用于生产的应用程序，而不会在框架本身遇到性能或其他问题。
- API 破坏性更改只能在正式宣布弃用后的2个版本中进行。

### Compose Multiplatform UI 框架当前平台稳定性级别

| Platform                 | Stability level |
| ------------------------ | --------------- |
| Android                  | Stable          |
| iOS                      | Stable          |
| Desktop (JVM)            | Stable          |
| Web based on Kotlin/Wasm | Beta            |
