# Glide 自定义模块功能

　　自定义模块功能可以将更改 Glide 配置、替换 Glide 组件等操作独立出来，使得能轻松地对 Glide 地各种配置进行自定义，并且又和 Glide 的图片加载逻辑没有任何交集，这也是一种低耦合编程方式的体现。

## 1. 自定义模块的基本用法

　　首先需要定义一个自己的模块类，并让它实现 GlideModule 接口，如下所示：

```java
public class MyGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
```

　　可以看到，在 MyGlideModule 类当中，重写了 applyOptions() 和 registerComponents() 方法，这两个方法分别就是用来更改 Glide 配置以及替换 Glide 组件的。只需要在这两个方法中加入具体的逻辑，就能实现更改 Glide 配置或者替换 Glide 组件的功能了。

　　不过，目前 Glide 还无法识别自定义的 MyGlideModule，如果想要让它生效，还得在 AndroidManifest.xml 文件当中加入如下配置才行：

```xml
<manifest>

    ...

    <application>

        <meta-data
            android:name="com.example.glidetest.MyGlideModule"
            android:value="GlideModule" />

        ...

    </application>
</manifest>
```

　　在 < application > 标签中加入一个 meta-data 配置项，其中 android:name 指定成自定义的这个 MyGlideModule 了。 

## 2. 自定义模块的原理

　　Glide 类是有创建实例的，只不过在内部由 Glide 自动创建和管理了，对于开发者而言，大多数情况下是不用关心它的，只需要调用它的静态方法就可以了。

### 2.1. Glide#get

　　Glide 的实例创建从 Glide 类中的 get() 方法中实现。

```java
/**
 * A singleton to present a simple static interface for building requests with {@link BitmapRequestBuilder} and
 * maintaining an {@link Engine}, {@link BitmapPool}, {@link com.bumptech.glide.load.engine.cache.DiskCache} and
 * {@link MemoryCache}.
 */
public class Glide {

    /**
     * Get the singleton.
     *
     * @return the singleton
     */
    public static Glide get(Context context) {
        if (glide == null) {
            synchronized (Glide.class) {
                if (glide == null) {
                    Context applicationContext = context.getApplicationContext();
                    List<GlideModule> modules = new ManifestParser(applicationContext).parse();

                    GlideBuilder builder = new GlideBuilder(applicationContext);
                    for (GlideModule module : modules) {
                        module.applyOptions(applicationContext, builder);
                    }
                    glide = builder.createGlide();
                    for (GlideModule module : modules) {
                        module.registerComponents(applicationContext, glide);
                    }
                }
            }
        }

        return glide;
    }

}
```

　　首先使用了一个单例模式来获取 Glide 对象的实例，可以看到，这是一个非常典型的双重锁模式。接下来调用 ManifestParser 的 parse() 方法去解析 AndroidManifest.xml 文件中的配置，实际上就是将 AndroidManifest 中所有值为 GlideModule 的 meta-data 配置读取出来，并将相应的自定义模块实例化。由于可以自定义任意多个模块，因此这里将会得到一个 GlideModule 的 List 集合。

　　然后创建了一个 GlideBuilder 对象，并通过一个循环调用了每一个 GlideModule 的 applyOptions() 方法，同时也把 GlideBuilder 对象作为参数传入到这个方法中。而 applyOptions() 方法就是可以加入自己的逻辑的地方了。

　　再往下调用了 GlideBuilder 的 createGlide() 方法，并返回了一个 Glide 对象。也就是说，Glide 对象的实例就是在这里创建的。

### 2.2. GlideBuilder#createGlide

```java
/**
 * A builder class for setting default structural classes for Glide to use.
 */
public class GlideBuilder {
    
    private final Context context;

    private Engine engine;
    private BitmapPool bitmapPool;
    private MemoryCache memoryCache;
    private ExecutorService sourceService;
    private ExecutorService diskCacheService;
    private DecodeFormat decodeFormat;
    private DiskCache.Factory diskCacheFactory;
    
    Glide createGlide() {
        if (sourceService == null) {
            final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            sourceService = new FifoPriorityThreadPoolExecutor(cores);
        }
        if (diskCacheService == null) {
            diskCacheService = new FifoPriorityThreadPoolExecutor(1);
        }

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        if (bitmapPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int size = calculator.getBitmapPoolSize();
                bitmapPool = new LruBitmapPool(size);
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());
        }

        if (diskCacheFactory == null) {
            diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }

        if (engine == null) {
            engine = new Engine(memoryCache, diskCacheFactory, diskCacheService, sourceService);
        }

        if (decodeFormat == null) {
            decodeFormat = DecodeFormat.DEFAULT;
        }

        return new Glide(engine, memoryCache, bitmapPool, context, decodeFormat);
    }
    
}
```

　　这个方法中会创建 BitmapPool、MemoryCache、DiskCache、DecodeFormat 等对象的实例，并在最后一行创建一个 Glide 对象的实例，然后将前面创建的这些实例传入到 Glide 对象当中，以供后续的图片加载操作使用。

　　有一个细节，createGlide() 方法中创建任何对象的时候都做了一个空检查，只有在对象为空的时候才回去创建它的实例。也就是说，如果可以在 applyOptions() 方法中提前就给这些对象初始化并赋值，那么在 createGlide() 方法中就不会再去重新创建它们的实例了，从而也就实现了更改 Glide 配置的功能。

　　接着回到 Glide 的 get() 方法中，得到了 Glide 对象的实例之后，接下来又通过一个循环调用了每个 GlideModule 的 registerComponents() 方法，在这里可以加上替换 Glide 的组件的逻辑。

　　这就是 Glide 自定义模块的全部工作原理。

## 3. 更改 Glide 配置

　　如果想要更改 Glide 的默认配置，其实只需要在 applyOptions() 方法中提前将 Glide 的配置项进行初始化就可以了。

### 3.1. Glide 的配置项

1. setMemoryCache()

   用于配置 Glide 的内存缓存策略，默认配置是 LruResourceCache。

2. setBitmapPool()

   用于配置 Glide 的 Bitmap 缓存池，默认配置是 LruBitmapPool。

3. setDiskCache()

   用于配置 Glide 的硬盘缓存策略，默认配置是 InternalCacheDiskCacheFactory。

4. setDiskCacheService()

   用于配置 Glide 读取缓存中图片的异步执行器，默认配置是 FifoPriorityThreadPoolExecutor，也就是先入先出原则。

5. setResizeService()

   用于配置 Glide 读取非缓存中图片的异步处理器，默认配置也是 FifoPriorityThreadPoolExecutor。

6. setDecodeFormat()

   用于配置 Glide 加载图片的解码模式，默认配置是 RGB_565。

　　其实 Glide 的这些默认配置都非常科学且合理，使用的缓存算法也都是效率极高的，因此在绝大多数情况下并不需要去修改这些默认配置，这也是 Glide 用法能如此简洁的一个原因。

　　但是 Glide 科学的默认配置并不影响学习自定义 Glide 模块的功能，因为总有某些情况下，默认的配置可能将无法满足需求，这个时候就需要自己动手去修改默认配置了。

### 3.2. 更改 Glide 硬盘缓存策略

　　Glide 默认的硬盘缓存策略使用的是 InternalCacheDiskCacheFactory，这种缓存会将所有 Glide 加载的图片都存储到当前应用的私有目录下，这是一种非常安全的做法，但同时这种做法也造成了一些不便，因为私有目录下即使是开发者自己也是无法查看的，如果想要去验证一下图片到底有没有成功缓存下来，这就有点不太好办了。

　　这种情况下，就非常适合使用自定义模块来更改 Glide 的默认配置。完全可以自己去实现 DiskCache.Factory 接口来自定义一个硬盘缓存策略，不过却大大没有必要这么做，因为 Glide 本身就内置了一个 ExternalCacheDiskCacheFactory，可以允许将加载的图片都缓存到 SD 卡。

　　那么接下来，就尝试使用这个 ExternalCacheDiskCacheFactory 来替换默认的 InternalCacheDiskFactory，从而将所有 Glide 加载的图片都缓存到 SD 卡上。

　　在前面已经创建好了一个自定义模块 MyGlideModule，那么现在就可以直接在这里编写逻辑了，代码如下：

```java
public class MyGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

}
```

　　就是这么简单，现在所有 Glide 加载的图片都会缓存到 SD 卡上了。

### 3.3. 修改缓存大小

　　另外，InternalCacheDiskCacheFactory 和 ExternalCacheDiskFactory 的默认硬盘缓存大小都是 250M。也就是说，如果应用缓存的图片总大小超过了 250M，那么 Glide 就会按照 DiskLruCache 算法的原则来清理缓存的图片。

　　当然，也是可以对这个默认的缓存大小进行修改的，而且修改方式非常简单，如下所示：

```java
public class MyGlideModule implements GlideModule {

    public static final int DISK_CACHE_SIZE = 500 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

}
```

　　只需要向 ExternalCacheDiskCacheFactory 或者 InternalCacheDiskFactory 再传入一个参数就可以了，这样就将 Glide 硬盘缓存的大小调整成了 500M。

　　更改Glide 配置的功能就是这么简单。

### 3.4. 更改图片格式

　　Glide 和 Picasso 的用法是非常相似的，但是有一点差别很大。Glide 加载图片的默认格式是 RGB_565，而 Picasso 加载图片的默认格式是 ARGB_8888。ARGB_8888 格式的图片效果会更加细腻，但是内存开销会比较大，而 RGB_565 格式的图片则更加节省内存，但是图片效果上会差一些。

　　Glide 和 Picasso 各自采用的默认图片格式谈不上孰优孰劣，只能说各自的取舍不一样。但是如果希望 Glide 也能使用 ARGB_888 的图片格式，这当然也是可以的，只需要在 MyGlideModule 中更改一下默认配置即可，如下所示：

```java
public class MyGlideModule implements GlideModule {

    public static final int DISK_CACHE_SIZE = 500 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

}
```

　　通过这样配置之后，使用 Glide 加载的所有图片都将会使用 ARGB_8888 的格式，虽然图片质量变好了，但同时内存开销也会明显增大。

## 4. 替换 Glide 组件

　　替换 Glide 组件功能需要在自定义模块的 registerComponents() 方法中加入具体的替换逻辑。相比于更改 Glide 配置，替换 Glide 组件这个功能的难度就明显大了不少。

　　Glide 中的组件非常繁多，也非常复杂，但其实大多数情况下并不需要去做什么替换。不过，有一个组件却有着比较大的替换需求，那就是 Glide 的 HTTP 通讯组件。

　　默认情况下，Glide 使用的是基于原生 HttpURLConnection 进行订制的 HTTP 通讯组件，但是现在大多数的 Android 开发者都更喜欢使用 OkHttp，因此将 Glide 中的 HTTP 通讯组件修改成 OkHttp 的这个需求比较常见。

### 4.1. Glide 有哪些组件

　　首先来看 Glide 中目前有哪些组件，在 Glide 类的构造方法当中，如下所示：

```java
/**
 * A singleton to present a simple static interface for building requests with {@link BitmapRequestBuilder} and
 * maintaining an {@link Engine}, {@link BitmapPool}, {@link com.bumptech.glide.load.engine.cache.DiskCache} and
 * {@link MemoryCache}.
 */
public class Glide {
    Glide(Engine engine, MemoryCache memoryCache, BitmapPool bitmapPool, Context context, DecodeFormat decodeFormat) {
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.memoryCache = memoryCache;
        this.decodeFormat = decodeFormat;
        loaderFactory = new GenericLoaderFactory(context);
        mainHandler = new Handler(Looper.getMainLooper());
        bitmapPreFiller = new BitmapPreFiller(memoryCache, bitmapPool, decodeFormat);

        dataLoadProviderRegistry = new DataLoadProviderRegistry();

        StreamBitmapDataLoadProvider streamBitmapLoadProvider =
                new StreamBitmapDataLoadProvider(bitmapPool, decodeFormat);
        dataLoadProviderRegistry.register(InputStream.class, Bitmap.class, streamBitmapLoadProvider);

        FileDescriptorBitmapDataLoadProvider fileDescriptorLoadProvider =
                new FileDescriptorBitmapDataLoadProvider(bitmapPool, decodeFormat);
        dataLoadProviderRegistry.register(ParcelFileDescriptor.class, Bitmap.class, fileDescriptorLoadProvider);

        ImageVideoDataLoadProvider imageVideoDataLoadProvider =
                new ImageVideoDataLoadProvider(streamBitmapLoadProvider, fileDescriptorLoadProvider);
        dataLoadProviderRegistry.register(ImageVideoWrapper.class, Bitmap.class, imageVideoDataLoadProvider);

        GifDrawableLoadProvider gifDrawableLoadProvider =
                new GifDrawableLoadProvider(context, bitmapPool);
        dataLoadProviderRegistry.register(InputStream.class, GifDrawable.class, gifDrawableLoadProvider);

        dataLoadProviderRegistry.register(ImageVideoWrapper.class, GifBitmapWrapper.class,
                new ImageVideoGifDrawableLoadProvider(imageVideoDataLoadProvider, gifDrawableLoadProvider, bitmapPool));

        dataLoadProviderRegistry.register(InputStream.class, File.class, new StreamFileDataLoadProvider());

        register(File.class, ParcelFileDescriptor.class, new FileDescriptorFileLoader.Factory());
        register(File.class, InputStream.class, new StreamFileLoader.Factory());
        register(int.class, ParcelFileDescriptor.class, new FileDescriptorResourceLoader.Factory());
        register(int.class, InputStream.class, new StreamResourceLoader.Factory());
        register(Integer.class, ParcelFileDescriptor.class, new FileDescriptorResourceLoader.Factory());
        register(Integer.class, InputStream.class, new StreamResourceLoader.Factory());
        register(String.class, ParcelFileDescriptor.class, new FileDescriptorStringLoader.Factory());
        register(String.class, InputStream.class, new StreamStringLoader.Factory());
        register(Uri.class, ParcelFileDescriptor.class, new FileDescriptorUriLoader.Factory());
        register(Uri.class, InputStream.class, new StreamUriLoader.Factory());
        register(URL.class, InputStream.class, new StreamUrlLoader.Factory());
        register(GlideUrl.class, InputStream.class, new HttpUrlGlideUrlLoader.Factory());
        register(byte[].class, InputStream.class, new StreamByteArrayLoader.Factory());

        transcoderRegistry.register(Bitmap.class, GlideBitmapDrawable.class,
                new GlideBitmapDrawableTranscoder(context.getResources(), bitmapPool));
        transcoderRegistry.register(GifBitmapWrapper.class, GlideDrawable.class,
                new GifBitmapWrapperDrawableTranscoder(
                        new GlideBitmapDrawableTranscoder(context.getResources(), bitmapPool)));

        bitmapCenterCrop = new CenterCrop(bitmapPool);
        drawableCenterCrop = new GifBitmapWrapperTransformation(bitmapPool, bitmapCenterCrop);

        bitmapFitCenter = new FitCenter(bitmapPool);
        drawableFitCenter = new GifBitmapWrapperTransformation(bitmapPool, bitmapFitCenter);
    }
    
}
```









4.1. 将 HTTP 通讯组件修改成 OkHttp

## 5. 更简单的组件替换






## 参考文章
[Android图片加载框架最全解析（六），探究Glide的自定义模块功能](https://blog.csdn.net/guolin_blog/article/details/78179422)

[Android图片加载框架最全解析（七），实现带进度的Glide图片加载功能](https://blog.csdn.net/guolin_blog/article/details/78357251)

[Android图片加载框架最全解析（八），带你全面了解Glide 4的用法](https://blog.csdn.net/guolin_blog/article/details/78582548)




































