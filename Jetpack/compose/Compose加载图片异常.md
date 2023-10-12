# 解决 Compose 在 Android 上面加载图片出现异常

在使用 Compose 的项目上线之后，多了一些加载图片的 Crash，Firebase 查看异常堆栈信息：

```
Fatal Exception: java.lang.IllegalArgumentException: Only VectorDrawables and rasterized asset types are supported ex. PNG, JPG
       at androidx.compose.ui.res.PainterResources_androidKt.loadImageBitmapResource(PainterResources.android.kt:111)
       at androidx.compose.ui.res.PainterResources_androidKt.access$loadImageBitmapResource(PainterResources.android.kt:1)
       at androidx.compose.ui.res.PainterResources_androidKt.painterResource(PainterResources.android.kt:70)
```

然后定位到异常代码：

```kotlin
Image(
    painter = painterResource(id = R.drawable.ic_pic),
    contentDescription = ""
)
```

出现问题图片资源都是放在 drawable- 这个目录下面，实际用真机测试也难以复现。

painterResource 是一个 Compose 函数，官方使用说明如下：

从 Android 资源 ID 创建一个 [Painter]。这可以分别为基于 [ImageBitmap] 的资产或基于矢量的资产加载 [BitmapPainter] 或 [VectorPainter] 的实例。具有给定 id 的资源必须指向完全光栅化的图像（例如 PNG 或 JPG 文件）或 VectorDrawable xml 资产。此处不支持基于 API 的 xml Drawable。

通过调用 [drawIntoCanvas] 并使用通过 [nativeCanvas] 提供的 Android 框架画布进行绘图，可以将替代的 Drawable 实现与 compose 一起使用。

这里说支持传入 png、jpg 以及矢量图格式，然而实际传入的是 png 格式的图片。

看看 painterResource 函数的源码：

```kotlin
@Composable
fun painterResource(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val res = resources()
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string
    // Assume .xml suffix implies loading a VectorDrawable resource
    return if (path?.endsWith(".xml") == true) {
        val imageVector = loadVectorResource(context.theme, res, id, value.changingConfigurations)
        rememberVectorPainter(imageVector)
    } else {
        // Otherwise load the bitmap resource
        val imageBitmap = remember(path, id, context.theme) {
            loadImageBitmapResource(res, id)
        }
        BitmapPainter(imageBitmap)
    }
}
```

可以看到 png 格式图片会走到 loadImageBitmapResource(res, id)，继续跟进：

```kotlin
private fun loadImageBitmapResource(res: Resources, id: Int): ImageBitmap {
    try {
        return ImageBitmap.imageResource(res, id)
    } catch (throwable: Throwable) {
        throw IllegalArgumentException(errorMessage)
    }
}
```

这里看到了 throw IllegalArgumentException(errorMessage)，抛出异常，继续查看 errorMessage，发现 errorMessage 正是所报的错误信息。

```kotlin
private const val errorMessage =
    "Only VectorDrawables and rasterized asset types are supported ex. PNG, JPG"
```

那么就明白了，实际上 ImageBitmap.imageResource 这里出现的异常信息，继续跟进可以看到：

```kotlin
fun ImageBitmap.Companion.imageResource(res: Resources, @DrawableRes id: Int): ImageBitmap {
    return (res.getDrawable(id, null) as BitmapDrawable).bitmap.asImageBitmap()
}
```

这里使用了 getDrawable 获取 drawable，传入 drawable id 和 theme 为 null，继续查看源码，发现会有一个异常抛出，猜测 Android 的 bug 导致了 NotFoundException。

```kotlin
public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme)
        throws NotFoundException {
    return getDrawableForDensity(id, 0, theme);
}
```

为什么 View 系列加载图片不会出现这个问题呢？通常使用 View 系列获取 drawable 的方式是使用 ContextCompat.getDrawable，查看源码：

```kotlin
@Nullable
    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int id) {
        if (Build.VERSION.SDK_INT >= 21) {
            return Api21Impl.getDrawable(context, id);
        } else if (Build.VERSION.SDK_INT >= 16) {
            return context.getResources().getDrawable(id);
        } else {
            // Prior to JELLY_BEAN, Resources.getDrawable() would not correctly
            // retrieve the final configuration density when the resource ID
            // is a reference another Drawable resource. As a workaround, try
            // to resolve the drawable reference manually.
            final int resolvedId;
            synchronized (sLock) {
                if (sTempValue == null) {
                    sTempValue = new TypedValue();
                }
                context.getResources().getValue(id, sTempValue, true);
                resolvedId = sTempValue.resourceId;
            }
            return context.getResources().getDrawable(resolvedId);
        }
    }
```

这是一个适配加载 drawable 的类，Android 7.0 对应的版本号是 24，可以看到获取图片会执行到 Api21Impl.getDrawable(context, id)。另外，上面的代码还发现了几句话：

```
在 JELLY_BEAN 之前，当资源 ID 引用另一个 Drawable 资源时，Resources.getDrawable() 将无法正确检索最终配置密度。作为解决方法，请尝试手动解析可绘制对象引用。
```

上面的 painterResource 函数调用到 (res.getDrawable(id, null) as BitmapDrawable).bitmap.asImageBitmap() 和 Api21Impl.getDrawable(context, id) 最终都会执行到同一个函数，如下：

```kotlin
public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme)
            throws NotFoundException {
        return getDrawableForDensity(id, 0, theme);
    }
```

其中前者传入 theme 为 null，后者在内部调用处传入了 getTheme()，区别就是有没有传 theme，那么到这里就明白 Compose 加载图片这个报错事怎么来的了。

那么可总结原因如下：

1. 直接调用 Resources.getDrawable() 获取 drawable 在低版本机器会有可能出现解析不到资源引用的问题，因为没有传入 theme。
2. Compose 提供的 painterResource() 实际最后是调用了 Resources.getDrawable()，所以才会有以上 Crash。

解决办法：

1. 图片资源移动到 drawable-xxxhdpi 限定了相关分辨率的文件夹内。
2. 适配 painterResource。
3. 或者用 Coil 的 remeberAsyncImagePainter 加载图片。

贴出适配代码：

```java
@Composable
fun painterResourceCompat(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val res = context.resources
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string
    if (path?.endsWith(".xml") == true) {
        return painterResource(id = id)
    }
    val imageBitmap = remember(path, id, context.theme) {
        try {
            ImageBitmap.imageResource(res, id)
        } catch (throwable: Throwable) {
            val drawable: Drawable =
                ContextCompat.getDrawable(context, id) ?: throw IllegalArgumentException("not found drawable, path: $path")
            drawable.toBitmap().asImageBitmap()
        }
    }
    return BitmapPainter(imageBitmap)
}
```



## 参考文章

[解决Compose在Android 7上面加载图片出现异常：Only VectorDrawables and rasterized asset types are supported ex. P...](https://www.jianshu.com/p/e4c34c4c4862)
