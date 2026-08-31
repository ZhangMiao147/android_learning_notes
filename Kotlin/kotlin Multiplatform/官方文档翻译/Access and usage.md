

# Using multiplatform resources in your app

[set up the resources for your project](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html)后，构建项目以生成特殊的 “Res” 类，该类提供对资源的访问。要重新生成 Res 类或所有资源方问题，再次构建项目或在 IDE 中重新导入项目。

之后，你可以使用生成的类从代码或外部库中访问已配置的多平台资源。

阅读一下主题的详细信息：

- [Importing the generated `Res` class and accessors](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#importing-the-generated-class).

- [Customizing the accessor class generation](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#customizing-accessor-class-generation): 如何将其设置为公共、分配给包或无条件生成。

- 使用特定资源类型:
  
  - [Drawable resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#images), 例如简单图片、柵格化图像（rasterized images）或 XML 矢量图。
  
  - [Vector Android XML icons](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#icons) 来自 Material Symbols 库,
  
  - [Strings](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#strings), 包含简单字符、模版、数组和复数形式。
  
  - [Storing and loading custom fonts](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#fonts),
  
  - [Raw files](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#raw-files) 以及将字节数组转换为图像。

- [Accessing resources mapped with string IDs](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#generated-maps-for-resources-and-string-ids).

- [Using multiplatform resources as Android assets](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#compose-multiplatform-resources-as-android-assets).

- 处理 web 特有资源：
  
  - [Preloading of resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#preloading-of-resources-for-web-targets) 使用浏览器功能和预加载 api。
  
  - [Caching web resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#caching-web-resources).

- 处理外部资源： [from external libraries](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#accessing-multiplatform-resources-from-external-libraries), [remote files](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#remote-files), 和 [Java resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#using-java-resources).

## Importing the generated class

准备好需要的资源，导入生成类，例如：

```
import project.composeapp.generated.resources.Res
import project.composeapp.generated.resources.example_image
```

其中：

* project 是项目的名称

* composeapp 是你放置资源目录的模块

* Res 是生成的类的默认名称。

* example_image 是 composeResources/drawable 目录中图像文件的名称（例如 example_image.png）

## Customizing accessor class generation

可以使用 Gradle 设置自定义生成 Res 类以满足你的需求。

在 build.gradle.kts 文件的 compose.resources {} 块中，能指定多个设置来影响项目中生成 Res 类的生成方式。配置示例如下：

```
compose.resources {
    publicResClass = false
    packageOfResClass = "me.sample.library.resources"
    generateResClass = auto
}
```

* publicResClass 设置为 true 会使 生成的 Res 类公开。默认情况下，生成的类是 [internal](https://kotlinlang.org/docs/visibility-modifiers.html)

* packageOfResClass 允许将生成的 Res 类分配给特定的包（为了在代码中访问，以及在最终工作中实现隔离）。默认情况下，Compose Multiplatform 指定 {group name}.{module name}.generated.resources 包分配给类。

* generateResClass 设置为 always 会使项目无条件生成 Res 类。但资源库仅通过传递可用时，这可能很有用。默认情况下，仅当当前项目有明确的 implementation 或 api 依赖在资源库中，Compose Multiplatform 使用 auto 值来生成 Res 类。

## Resource usage

### Images

你可以将简单图像、柵格化图片或 XML 矢量图作为图像资源访问。除了 Android，所有平台都支持 SVG 图像。

* 将 drawable 资源作为 Painter 图像访问，使用 painterResource() 函数：
  
  ```
  @Composable
  fun painterResource(resource: DrawableResource): Painter {...}
  ```
  
  painterResource() 函数接收一个资源路径并返回一个 Painter 值。除了 web 平台，该函数在所有平台上同步运行，它会在首次重组时返回一个空的 Painter，并在后续重新组合时将其替换为加载的图像。
  
  * painterResource() 会加载 BitmapPainter 用于柵格化图像格式，像是 `.png`, `.jpg`, `.bmp`, `.webp`或者 VectorPainter 用于 Android XML 矢量可绘制格式。
  
  * XML 矢量可绘制格式与 [Android](https://developer.android.com/reference/android/graphics/drawable/VectorDrawable) 一样，但不支持对 Android 资源的外部引用。



* 将 drawable 资源作为 ImageBitmap 柵格化图像访问，使用 imageResource() 函数：
  
  ```
  @Composable
  fun imageResource(resource: DrawableResource): ImageBitmap {...}
  ```

* 将 drawable 资源作为 ImageVector XML 矢量访问，使用 vectorResource() 函数：
  
  ```
  @Composable
  fun vectorResource(resource: DrawableResource): ImageVector {...}
  ```

以下示例展示了如何在 Compose Multiplatform 代码中访问图像：

```
Image(
    painter = painterResource(Res.drawable.my_image),
    contentDescription = null
)
```

### Icons



### Strings

将所有字符串资源存储在 composeResources/values 目录下的 XML 文件中。每个文件文件中的每个条目都会生成一个静态访问器。

关于如何针对不同语言环境本地化字符串的更多信息，请参阅[guide on localizing strings](https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html).

#### Simple strings

要存储简单字符串，在 XML 中添加 \<string>元素：

```
<resources>
    <string name="app_name">My awesome app</string>
    <string name="title">Some title</string>
</resources>
```

要将字符串资源作为字符串获取，请使用以下代码：

```
@Composable
fun stringResource(resource: StringResource): String {...}

@Composable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String {...}
```

例如：

```
Text(stringResource(Res.string.app_name))
```

你可以在字符串资源中使用特殊符号：

* \n - 表示换行符

* \t - 表示制表符

* \uXXXX - 表示特定的 Unicode 字符

你无需处理像 "@" 或 "?"  特殊的 XML 字符，就和 [for Android strings](https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes) 一样。

#### String templates



#### String arrays



#### Plurals 复数





### Fonts

### Raw files

### Generated maps for resources and string IDs

### Compose Multiplatform resources as Android assets

## Preloading of resources for web targets - 暂时不看

## Caching web resources - 暂时不看

## Interaction with other libraries and resources

## What's nest?
