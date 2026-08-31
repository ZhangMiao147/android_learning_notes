# Resources overview 资源概述

Compose Multiplatform 提供了一个特殊的 compose-multiplatform-resources 库，并且Gradle 插件支持，用于跨所有支持平台访问通用代码中的资源。资源是静态内容，例如 images、fonts 和 straing，你可以在应用程序中使用。

在 Compose Multiplatform 中使用资源时，考虑当前条件：

* 几乎所有的资源都是在调用者线程中同步读取的。唯一的例外是 raw 文件和 web 资源是异步读取的。

* 尚不支持以流的形式读取大的 raw 文件，像是长视频。使用 getUri() 函数将单独的文件传给系统 API，例如 [kotlinx-io](https://github.com/Kotlin/kotlinx-io) 库。

* 从 1.6.0 开始，只要你使用 Kotlin 2.0.0 或更高版本以及 Gradle 7.6 或更高版本，你可以将资源方放置在任何模块或资源集中。

想要知道如何在 Compose Multiplatform 中使用资源，请参阅一下关键部分：

- [Setup and configuration for multiplatform resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html)
  
  Add the `resources` library dependency and set up all resources that your app should be able to access.
  
  添加 resources 库依赖并设置你应用程序应该能够被访问的所有资源。

- [Using multiplatform resources in your app](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html)
  
  Learn how to use the automatically generated accessors to access resources directly in your UI code.
  
  了解如何使用自动生成的访问器直接在 UI 代码中访问资源。

- [Local resource environment](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)
  
  管理应用程序的资源环境，比如应用程序内主题和语言。
