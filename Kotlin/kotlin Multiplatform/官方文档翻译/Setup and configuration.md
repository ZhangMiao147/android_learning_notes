# Setup and configuration for multiplatform resources 多平台资源的设置和配置

要正确配置项目以使用多平台资源，请执行以下操作：

1. 添加库依赖项。

2. 为每种资源创建必要的目录。

3. 为特定资源（例如：深色 UI 主题的不同图像或本地化字符串）创建额外的目录。

## 构建脚本和目录设置

在多平台项目中访问资源，添加库依赖并在项目目录中组件文件：

1. 在 composeApp 目录下的 build.gradle.kts 文件中，添加对 commonMain 源集的依赖项：

```
kotlin {
    //...
    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
        }
    }
}
```

要直接引用库，请使用 Maven Central 中构件页面上的完整限定名称。

2. 在你想要添加资源的源集目录（案例为 commonMain）中创建一个名为 composeResources 的新目录：
   
   ![](https://kotlinlang.org/docs/multiplatform/images/compose-resources-structure.png)

3. 请按照以下规则 composeResources 目录结构：
   
   * images 应该在 drawable 目录。Compose Multiplatform 支持栅格化图像（JPEG,PNG, bitmap 和 WebP）和矢量 Android XML 图像（不引用 Android 资源）。
   
   * Fonts 应该在 font 目录中。
   
   * Strings 应该位于 values 目录中。
   
   * 其他文件应该在 files 目录中，你可以根据需要使用任何合适的文件夹层级结构。

### 自定义资源目录

在 build.gradle.kts 文件的 compose.respurces{} 代码块中，你可以为每一个源集制定自定义资源目录。每个自定义文件也应该像默认的 composeResources 目录一样的包含文件：例如，drawable 子目录存放 images，font 子目录存放 fonts，等等。

一个简单的指向特定文件夹的例子：

```
compose.resources {
    customDirectory(
        sourceSetName = "jvmMain",
        directoryProvider = provider { layout.projectDirectory.dir("desktopResources") }
    )
}
```

你也可以设置一个由 Gradle 任务填充的文件夹，例如，用于存放下载文件：

```
abstract class DownloadRemoteFiles : DefaultTask() {

    @get:OutputDirectory
    val outputDir = layout.buildDirectory.dir("downloadedRemoteFiles")

    @TaskAction
    fun run() { /* your code for downloading files */ }
}

compose.resources {
    customDirectory(
        sourceSetName = "iosMain",
        directoryProvider = tasks.register<DownloadRemoteFiles>("downloadedRemoteFiles").map { it.outputDir.get() }
    )
}
```

### 自定义 web 资源路径

你可以使用 configureWebResources() 函数为 web 资源制定路径和 URL：

* 使用一个相对路径（以 / 开始）引用域根目录中的资源。

* 使用一个绝对路径（以 http:// 或 https:// 开始）引用托管在外部域或 CDN 的资源。

```
// Maps resources to an application-specific path
configureWebResources {
    resourcePathMapping { path -> "/myApp/resources/$path" }
}

// Maps resources to an external CDN
configureWebResources {
    resourcePathMapping { path -> "https://mycdn.com/myApp/res/$path" }
}
```

### androidLibrary 目标平台中的资源

从 Android Gradle 插件版本 8.8.0 开始，你可以在 androidLibrary 目标中使用生成的 Res 类和资源访问器。要在 androidLibrary 中启动多平台资源的支持，请如下更新你的配置：

```
kotlin {
  androidLibrary {
    androidResources.enable = true
  }
}
```

## 限定符

有时，同一资源需要根据不同的环境（例如语言区域、屏幕密度或界面主题）以不同的方式显示。例如，你可能需要将文本本地化为不同的语言，或调整图像以适应深色主题。为此，该库提供了特殊的限定符。

了解如何处理与资源相关的设置，请参阅 [Manage local resource environment](https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html)教程。

除了文件目录中的 raw 文件外的所有资源类型都支持限定符。使用连字符将限定符添加到目录名称中：

![](https://kotlinlang.org/docs/multiplatform/images/compose-resources-qualifiers.png)

不同类型的限定度可以同时应用。例如，“drawable-en-rUS-mdpi-dark” 是一个适用于美国地区英语语言、160dpi 屏幕采用深色主题的图像。

如果请求的限定符对应的资源无法访问，则会使用默认资源（不带限定符）。

### 语言与区域限定符

你可以组合使用语言和区域限定符：

* 语言由两位字母（ISO 639-1）或三位字母（ISO 639-2）的 [language code](https://www.loc.gov/standards/iso639-2/php/code_list.php) 定义。

* 你可以将两位字母的  [ISO 3166-1-alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) 区域代码添加到语言代码中。区域代码必须以小写字母 r 作为前缀，例如：drawable-spa-rMX。

语言代码和区域代码区分大小写。

### 主题限定符

你可以添加 “light” 或 “dark” 限定符。Compose Multiplatform 会根据当前系统主题选择所需的资源。

### Density 限定符

可以使用下面 density 限定符：

- "ldpi" – 120 DPI, 0.75x density

- "mdpi" – 160 DPI, 1x density

- "hdpi" – 240 DPI, 1.5x density

- "xhdpi" – 320 DPI, 2x density

- "xxhdpi" – 480 DPI, 3x density

- "xxxhdpi" – 640dpi, 4x density

资源的选择取决于系统中定义的 screen density。

## 出版

从 Compose Multiplatform 1.6.10 开始，所有必要的资源都包含在 publication maven artifacts 中。

要启动此功能，你的项目需要使用 Kotlin 2.0.0 或更高版本以及 Gradle 7.6 或更高版本。

## 接下来是什么

了解如何访问你设置的资源，以及如何在 [Using multiplatform resources in your app](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html) 页面自定义默认生成的访问器。

查看官方  [demo project](https://github.com/JetBrains/compose-multiplatform/tree/master/components/resources/demo)，了解如何在面向 iOS, Android 和 desktop 的 Compose Multiplatform 项目中处理资源。
