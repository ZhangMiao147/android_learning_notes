# Glide 图片变化功能

# 1. 问题

　　使用 Glide 显示图片时，ImageView 的宽高设置的都是 wrap_content，但是图片却充满了全屏。之所以会出现这个现象，就是因为 Glide 的图片变换功能所导致的。

　　在没有明确指定的情况下，ImageView 默认的 scaleType 是 FIT_CENTER。

## 1.1. GenericRequestBuilder#into

```java
    /**
     * Sets the {@link ImageView} the resource will be loaded into, cancels any existing loads into the view, and frees
     * any resources Glide may have previously loaded into the view so they may be reused.
     *
     * @see Glide#clear(android.view.View)
     *
     * @param view The view to cancel previous loads for and load the new resource into.
     * @return The {@link com.bumptech.glide.request.target.Target} used to wrap the given {@link ImageView}.
     */
    public Target<TranscodeType> into(ImageView view) {
        Util.assertMainThread();
        if (view == null) {
            throw new IllegalArgumentException("You must pass in a non null View");
        }

        if (!isTransformationSet && view.getScaleType() != null) {
            switch (view.getScaleType()) {
                case CENTER_CROP:
                    applyCenterCrop();
                    break;
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
                    applyFitCenter();
                    break;
                //$CASES-OMITTED$
                default:
                    // Do nothing.
            }
        }

        return into(glide.buildImageViewTarget(view, transcodeClass));
    }

```

　　在 into() 方法中会进行一个 switch 判断，如果 ImageView 的 scaleType 是 CENTER_CROP，则会去调用 applyCenterCrop() 方法，如果 scaleType 是 FIT_CENTER、FIT_START 或 FIT_END，则会去调用 applyFitCenter() 方法。这里的 applyCenterCrop() 和 applyFitCenter() 方法其实就是向 Glide 的加载流程中添加了一个图片变换操作。

　　由于 ImageView 默认的 scaleType 是 FIT_CENTER，因此会自动添加一个 FitCenter 的图片变换，而在这个图片变换过程中做某些操作，导致图片充满了全屏。

　　一种解决方法就是根据源码来改，当 ImageView 的 scaleType 是 CENTER_CROP、FIT_CENTER、FIT_START 或 FIT_END 时是会自动添加一个图片变化操作的，那么把 scaleType 改成其他值就可以了。ImageView 的 scaleType 可选值还有 CENTER、CENTER_INSIDE、FIT_XY 等。这是一种解决方案，但是是一种比较笨的解决方案，因为是为了解决问题而去改动了 ImageView 原有的 scaleType，而真的需要 ImageView 的 scaleType 为 CENTER_CROP 或 FIT_CENTER 时可能就不好了。所以不推荐。

　　实际上，Glide 提供了专门的 API 来添加和取消图片变换，想解决上面的问题只需要使用如下代码即可：

```java
Glide.with(this)
     .load(url)
     .dontTransform()
     .into(imageView);
```

　　可以看到，这里调用了一个 dontTransform() 方法，表示让 Glide 在加载图片的过程中不进行图片变换，这样刚才调用的 applyCenterCrop()、applyFitCenter() 就统统无效了。

　　但是使用 dontTransform() 方法存在着一个问题，就是调用了这个方法之后，所有的图片变换操作就全部失效了，如果有一些图片变换操作时必须执行的，这种情况下只需要借助 override() 方法强制将图片尺寸指定成原始大小就可以了，代码如下所示：

```java
Glide.with(this)
     .load(url)
     .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
     .into(imageView);
```

　　通过 override() 方法将图片的宽和高都指定成 Target.SIZE_ORIGINAL，问题同样被解决了。

# 2. 图片变换的基本用法

　　图片变换的意思就是说，Glide 从加载了原始图片到最终展示给用户之前，又进行了一些变换处理，从而能够实现一些更加丰富的图片效果，如图片圆角化、图形化、模糊化等等。

　　添加图片变换的用法非常简单，只需要调用 transform() 方法，并将想要执行的图片变换操作作为参数传入 transform() 方法即可，如下所示：

```java
Glide.with(this)
     .load(url)
     .transform(...)
     .into(imageView);
```

　　至于具体要进行什么样的图片变换操作，这个通常都是需要自己来写的。不过 Glide 已经内置了两种图片变换操作，可以直接拿来使用，一个是 CenterCrop，一个是 FitCenter。

　　但这两种内置的图片变换操作其实都不需要使用 transform() 方法，Glide 为了方便使用直接提供了现成的 API：

```java
Glide.with(this)
     .load(url)
     .centerCrop()
     .into(imageView);

Glide.with(this)
     .load(url)
     .fitCenter()
     .into(imageView);
```

## 2.1. DrawableRequestBuilder#centerCrop#fitCenter

　　centerCrop() 和 fitCenter() 方法其实也只是对 transform() 方法进行了一层封装而已，它们背后的源码仍然还是借助 transform() 方法来实现的。

```java
/**
 * A class for creating a request to load a {@link GlideDrawable}.
 *
 * <p>
 *     Warning - It is <em>not</em> safe to use this builder after calling <code>into()</code>, it may be pooled and
 *     reused.
 * </p>
 *
 * @param <ModelType> The type of model that will be loaded into the target.
 */
public class DrawableRequestBuilder<ModelType>
        extends GenericRequestBuilder<ModelType, ImageVideoWrapper, GifBitmapWrapper, GlideDrawable>
        implements BitmapOptions, DrawableOptions {
    /**
     * Transform {@link GlideDrawable}s using {@link com.bumptech.glide.load.resource.bitmap.CenterCrop}.
     *
     * @see #fitCenter()
     * @see #transform(com.bumptech.glide.load.resource.bitmap.BitmapTransformation...)
     * @see #bitmapTransform(com.bumptech.glide.load.Transformation[])
     * @see #transform(com.bumptech.glide.load.Transformation[])
     *
     * @return This request builder.
     */
    @SuppressWarnings("unchecked")
    public DrawableRequestBuilder<ModelType> centerCrop() {
        return transform(glide.getDrawableCenterCrop());
    }

    /**
     * Transform {@link GlideDrawable}s using {@link com.bumptech.glide.load.resource.bitmap.FitCenter}.
     *
     * @see #centerCrop()
     * @see #transform(com.bumptech.glide.load.resource.bitmap.BitmapTransformation...)
     * @see #bitmapTransform(com.bumptech.glide.load.Transformation[])
     * @see #transform(com.bumptech.glide.load.Transformation[])
     *
     * @return This request builder.
     */
    @SuppressWarnings("unchecked")
    public DrawableRequestBuilder<ModelType> fitCenter() {
        return transform(glide.getDrawableFitCenter());
    }
        
}
```

　　FitCenter 的效果就是会将图片按照原始的长宽比充满全屏。CenterCrop 的效果是展示的图片是对原图的中心区域进行裁剪后得到的图片。

## 3. 源码分析

## 3.1. CenterCrop 类

```java
/**
 * Scale the image so that either the width of the image matches the given width and the height of the image is
 * greater than the given height or vice versa, and then crop the larger dimension to match the given dimension.
 *
 * Does not maintain the image's aspect ratio
 */
public class CenterCrop extends BitmapTransformation {

    public CenterCrop(Context context) {
        super(context);
    }

    public CenterCrop(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    // Bitmap doesn't implement equals, so == and .equals are equivalent here.
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null
                ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);
        Bitmap transformed = TransformationUtils.centerCrop(toReuse, toTransform, outWidth, outHeight);
        // poo.put(toResuse) 放入缓存池
        if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
            toReuse.recycle();
        }
        return transformed;
    }

    @Override
    public String getId() {
        return "CenterCrop.com.bumptech.glide.load.resource.bitmap";
    }
}
```

　　首先，CenterCrop 是继承 BitmapTransformation 的，整个图片变换功能都是建立在这个继承结构基础上的。

　　接下来 CenterCrop 中最重要的就是 transform() 方法，transform() 方法中有四个参数：

1. 第一个参数 pool，这个是 Glide 中的一个 Bitmap 缓存池，用于对 Bitmap 对象进行重用，否则每次图片变换都重新创建 Bitmap 对象将会非常消耗内存。
2. 第二个参数 toTransform，这个是原始图片的 Bitmap 对象，就是要对它进行图片变换。
3. 第三和第四个参数分别代表图片变换后的宽度和高度，其实也就是 override() 方法中传入的宽和高的值。

　　transform() 方法首先会从 Bitmap 缓存池中尝试获取一个可重用的 Bitmap 对象，然后把这个对象连同 toTransform、outWidth、outHeight 参数一起传入到了 TransformationUtils.centerCrop() 方法当中。

## 3.2. TransformationUtils#centerCrop

```java
/**
 * A class with methods to efficiently resize Bitmaps.
 */
public final class TransformationUtils {
    
    /**
     * A potentially expensive operation to crop the given Bitmap so that it fills the given dimensions. This operation
     * is significantly less expensive in terms of memory if a mutable Bitmap with the given dimensions is passed in
     * as well.
     *
     * @param recycled A mutable Bitmap with dimensions width and height that we can load the cropped portion of toCrop
     *                 into.
     * @param toCrop The Bitmap to resize.
     * @param width The width in pixels of the final Bitmap.
     * @param height The height in pixels of the final Bitmap.
     * @return The resized Bitmap (will be recycled if recycled is not null).
     */
    public static Bitmap centerCrop(Bitmap recycled, Bitmap toCrop, int width, int height) {
        if (toCrop == null) {
            return null;
        } else if (toCrop.getWidth() == width && toCrop.getHeight() == height) {
            return toCrop;
        }
        // From ImageView/Bitmap.createScaledBitmap.
        final float scale;
        float dx = 0, dy = 0;
        Matrix m = new Matrix();
        if (toCrop.getWidth() * height > width * toCrop.getHeight()) {
            scale = (float) height / (float) toCrop.getHeight();
            dx = (width - toCrop.getWidth() * scale) * 0.5f;
        } else {
            scale = (float) width / (float) toCrop.getWidth();
            dy = (height - toCrop.getHeight() * scale) * 0.5f;
        }

        m.setScale(scale, scale);
        m.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        final Bitmap result;
        if (recycled != null) {
            result = recycled;
        } else {
            result = Bitmap.createBitmap(width, height, getSafeConfig(toCrop));
        }

        // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
        TransformationUtils.setAlpha(toCrop, result);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(PAINT_FLAGS);
        canvas.drawBitmap(toCrop, m, paint);
        return result;
    }
    
}
```

　　这段代码就是整个图片变换功能的核心代码。先是做了一些校验，如果原图为空，或者原图的尺寸和目标裁剪尺寸相同，那么就放弃裁剪。接下来是通过数学计算来算出画布的缩放的比例以及偏移值。然后判断缓存池中取出的 Bitmap 对象是否为空，如果不为空就可以直接使用，如果为空则要创建一个新的 Bitmap 对象。接着将原图 Bitmap 对象的 alpha 值复制到裁剪 Bitmap 对象上面。最后是裁剪 Bitmap 对象进行绘制，并将最终的结果进行返回。

　　在得到了裁剪后的 Bitmap 对象，回到 CenterCrop 当中，在最终返回这个 Bitmap 对象之前，还会尝试将复用的 Bitmap 对象重新放回到缓存池当中，以便下次继续使用。

# 4. 自定义图片变换

　　Glide 定制好了一个图片变换的框架，大致的流程是获取到原始的图片，然后对图片进行变换，再将变换完成后的图片返回给 Glide，最终由 Glide 将图片显示出来。

　　自定义图片变换的功能的实现逻辑比较固定，其实就是自定义一个类让它继承自 BitmapTransformation，然后重写 transform() 方法，并在其中实现具体的图片变换逻辑就可以了。

　　一个空的图片变换实现大概如下所示：

```java
public class CircleCrop extends BitmapTransformation {

    public CircleCrop(Context context) {
        super(context);
    }

    public CircleCrop(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    @Override
    public String getId() {
        return "com.example.glidetest.CircleCrop";
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return null;
    }
}
```

　　这里有一点需要注意，就是 getId() 方法中要求返回一个唯一的字符串来作为 id，以和其他的图片变换做区分。通常情况下，直接返回当前类的完整类名就可以了。

　　这里选择继承 BitmapTransformation 还有一个限制，就是只能对静态图进行图片变换。当然，这已经足够覆盖日常 95% 以上的开发需求了。如果由特殊的需求要对 GIF 图进行图片变换，那就得自己去实现 Transformation 接口就可以了，这个就非常复杂了。

## 4.1. 图片图形化功能

　　以对图片进行圆形化变化为例自定义图片变换效果。

　　图形圆形化的功能现在在手机应用中非常常见，比如手机 QQ 就会将用户的头像进行圆形化变换，从而使得界面变得更加好看。

```java
public class CircleCrop extends BitmapTransformation {

    public CircleCrop(Context context) {
        super(context);
    }

    public CircleCrop(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    @Override
    public String getId() {
        return "com.example.glidetest.CircleCrop";
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int diameter = Math.min(toTransform.getWidth(), toTransform.getHeight());

        final Bitmap toReuse = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        final Bitmap result;
        if (toReuse != null) {
            result = toReuse;
        } else {
            result = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        }

        int dx = (toTransform.getWidth() - diameter) / 2;
        int dy = (toTransform.getHeight() - diameter) / 2;
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(toTransform, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (dx != 0 || dy != 0) {
            Matrix matrix = new Matrix();
            matrix.setTranslate(-dx, -dy);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        if (toReuse != null && !pool.put(toReuse)) {
            toReuse.recycle();
        }
        return result;
    }
}
```

　　首先先算出原图宽度和高度中较小的值，因为对图片进行图形化变化肯定要以较小的那个值作为直径来进行裁剪。接下来，从 Bitmap 缓存池中尝试获取一个 Bitmap 对象来进行重用，如果没有可重用的 Bitmap 对象的话就创建一个。然后是具体进行圆形化变化的部分，这里算出了画布的偏移值，并且根据刚才得到的直径算出半径来进行画圆。最后，尝试将复用的 Bitmap 对象重新放回到缓存池当中，并将图形化变换后的 Bitmap 对象进行返回。

　　这样，一个自定义图片变换的功能就写好了。使用方法非常简单，就是把这个自定义图形变换的实例传入到 transform() 方法中即可，如下所示：

```java
Glide.with(this)
     .load(url)
     .transform(new CircleCrop(this))
     .into(imageView);
```

# 5. 更多图片变换功能

　　虽说 Glide 的图片变换功能框架已经很强大了，可以轻松地自定义图片变换效果，但是如果每一种图片变换都要自己去写还是蛮吃力的。

　　事实上，确实也没有必要完全靠自己去实现各种各样的图片变换效果，因为大多数的图片变换都是比较通用的。网上出现了很多 Glide 的图片变换开源库，其中做的最出色的就是 glide-transformations 这个库了。它实现了很多通用的图片变换效果，如裁剪变换、颜色变换、模糊变换等等，使得可以非常轻松地进行各种各样地图片变换。

　　glide-transformations 地项目主页地址是 https://github.com/wasabeef/glide-transformations 。

## 5.1. 使用 glide-transformation 库

#### 5.1.1. 引入库

　　首先需要将这个库引入到项目当中，在 app/build.gradle 文件当中添加如下依赖：

```groovy
dependencies {
    compile 'jp.wasabeef:glide-transformations:2.0.2'
}
```

### 5.1.2. 模糊化处理

　　如果相对图片进行模糊化处理，那么就可以使用 glide-transformations 库中的 BlurTransformation 这个类，代码如下所示：

```java
Glide.with(this)
     .load(url)
     .bitmapTransform(new BlurTransformation(this))
     .into(imageView);
```

　　这里调用的是 bitmapTransform() 方法而不是 transform() 方法，因为 glide-transforms 库都是专门针对静态图片变换来进行设计的。

### 5.1.3. 黑白化

　　图片黑白化的效果使用的是 GrayscaleTransformation 这个类，代码如下：

```java
Glide.with(this)
     .load(url)
     .bitmapTransform(new GrayscaleTransformation(this))
     .into(imageView);
```

### 5.1.4. 组合使用

　　还可以将多个图片变换效果组合在一起使用，比如同时执行模糊化和黑白花的变换：

```java
Glide.with(this)
     .load(url)
     .bitmapTransform(new BlurTransformation(this), new GrayscaleTransformation(this))
     .into(imageView);
```

　　可以看到，同时执行多种图片变换的时候，只需要将它们都传入到 bitmaoTransformation() 方法中即可。

　　上面的只是 glide-transformations 库的一小部分功能而已，还有更多的图片变化效果。

# 6. 参考文章

1. [Android图片加载框架最全解析（五），Glide强大的图片变换功能](https://blog.csdn.net/guolin_blog/article/details/71524668)
