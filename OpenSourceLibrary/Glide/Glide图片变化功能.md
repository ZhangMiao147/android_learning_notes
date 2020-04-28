# Glide 图片变化功能

## 1. 问题

　　使用 Glide 显示图片时，ImageView 的宽高设置的都是 wrap_content，但是图片却充满了全屏。之所以会出现这个现象，就是因为 Glide 的图片变换功能所导致的。

　　在没有明确指定的情况下，ImageView 默认的 scaleType 是 FIT_CENTER。

### 1.1. GenericRequestBuilder#into

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

　　最直接的解决方法就是根据源码来改，当 ImageView 的 scaleType 是 CENTER_CROP、FIT_CENTER、FIT_START 或 FIT_END 时是会自动添加一个图片变化操作的，那么把 scaleType 改成其他值就可以了。ImageView 的 scaleType 可选值还有 CENTER、CENTER_INSIDE、FIT_XY 等。



## 2. 图片变换的基本用法



## 3. 源码分析



## 4. 自定义图片变换



## 5. 更多图片变换功能






## 参考文章
1. [Android图片加载框架最全解析（五），Glide强大的图片变换功能](https://blog.csdn.net/guolin_blog/article/details/71524668)




































