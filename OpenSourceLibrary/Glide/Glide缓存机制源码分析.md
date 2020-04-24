# Glide 的缓存机制源码分析

## 1. Glide 缓存简介

　　Glide 的缓存设计可以说是非常先进的，考虑的场景也很周全。在缓存这一功能上，Glide 又将它分成了两个模块，一个是内存缓存，一个是硬件缓存。

　　这两个缓存模块的作用各不相同，内存缓存的主要作用是防止应用重复将图片数据读取到内存当中，而硬盘缓存的主要作用是防止应用城府从网络或其他地方重复下载和读取数据。

　　内存缓存和硬盘缓存的相互结合才构成了 Glide 极佳的图片缓存效果。

## 2. 缓存 Key

　　既然是缓存功能，就必然会有用于进行缓存的 Key。那么 Glide 的缓存 Key 是怎么生成的呢？Glide 的缓存 Key 生成规则非常繁琐，决定缓存 Key 的参数竟然有 10 个之多。不过逻辑还是比较简单的。

　　生成缓存 Key 的代码在 Engine 类的 load() 方法当中开始。

### 2.1. Engine#load

```java
    public <T, Z, R> LoadStatus load(Key signature, int width, int height, DataFetcher<T> fetcher,
            DataLoadProvider<T, Z> loadProvider, Transformation<Z> transformation, ResourceTranscoder<Z, R> transcoder,
            Priority priority, boolean isMemoryCacheable, DiskCacheStrategy diskCacheStrategy, ResourceCallback cb) {
        Util.assertMainThread();
        long startTime = LogTime.getLogTime();

        final String id = fetcher.getId();
        EngineKey key = keyFactory.buildKey(id, signature, width, height, loadProvider.getCacheDecoder(),
                loadProvider.getSourceDecoder(), transformation, loadProvider.getEncoder(),
                transcoder, loadProvider.getSourceEncoder());

        ...
    }
```

　　可以看到，在 lode() 方法里面调用了 fetcher.getId() 方法获得了一个 id 字符串，这个字符串就是要加载的图片的唯一标识，比如说如果是一张网络上的图片的话，那么这个 id 就是这张图片的 url 地址。

　　接下来将这个 id 连同着 signature、width、height 等等 10 个参数一起传入到 EngineKeyFactory 的 buildKey() 方法当中，从而构建出了一个 EngineKey 对象，这个 EngineKey 也就是 Glide 中的缓存 Key 了。

　　可见，决定缓存 Key 的条件非常多，即使用 override() 方法改变了一下图片的 width 或者 height，也会生成一个完全不同的缓存 Key。

　　EngineKey 的源码主要就是重写了 enquals() 和 hashCode() 方法，保证只有传入 EngineKey 的所有参数都相同的情况下才认为是同一个 EngineKey 对象。

## 3. 内存缓存

　　有了缓存 Key，接下来就开始进行缓存了，先看内存缓存。

　　在默认情况下，Glide 自动就是开启内存缓存的。也就是说，当使用 Glide 加载了一张图片之后，这张图片就会被缓存到内存当中，主要在它还没从内存中被清除之前，下次使用 Glide 再加载这张图片都会直接从内存当中读取，而不用重新从网络或硬盘上读取了，这样无疑就可以大幅度提升图片的加载效率。比如再一个 RecyclerView 当中反复上下滑动，RecyclerView 中只要是 Glide 加载过的图片都可以直接从内存当中迅速读取并展示出来，从而大大提升了用户体验。

　　而 Glide 最为人性化的是，甚至不需要编写任何额外的代码就能自动享受到这个极为便利的内存缓存功能，因为 Glide 默认就已经将它开启了。

　　Glide 也提供了接口来关闭 Glide 的默认内存缓存：

```java
Glide.with(this)
     .load(url)
     .skipMemoryCache(true)
     .into(imageView);
```

　　只需要调用 skipMemoryCache() 方法并传入 true，就标识禁用了 Glide 的内存缓存功能。

　　Glide 内存缓存的实现是使用的 LruCache 算法，LruCache 算法（Least Recently Used）也叫近期最少使用算法。它的主要算法原理就是把最近使用的对象用强引用存储在 LInkedHashMap 中，并且把最近最少使用的对象在缓存值达到预设定值之前从内存中移除。Glide 还结合了一种弱引用的机制，共同完成了内存缓存功能。

　　在 load() 方法中，调用了 RequestManager 的  loadGeneric() 方法，而 loadGeneric() 方法中会调用 Glide.buildStreamModelLoader() 方法来获取一个 ModelLoader 对象。

### 3.1. Glide#buildStreamModelLoader

```java
public class Glide {
	...
	/**
     * A method to build a {@link ModelLoader} for the given model that produces {@link InputStream}s using a registered
     * factory.
     *
     * @see #buildModelLoader(Class, Class, android.content.Context)
     */
    public static <T> ModelLoader<T, InputStream> buildStreamModelLoader(Class<T> modelClass, Context context) {
        return buildModelLoader(modelClass, InputStream.class, context);
    }
    
    
        /**
     * Build a {@link ModelLoader} for the given model class using registered {@link ModelLoaderFactory}s.
     *
     * @see  #buildModelLoader(Object, Class, Context)
     * @see  #buildStreamModelLoader(Class, Context)
     * @see  #buildFileDescriptorModelLoader(Class, Context)
     *
     * @param modelClass The class to get a {@link ModelLoader} for.
     * @param resourceClass The resource class to get a {@link ModelLoader} for.
     * @param context Any context.
     * @param <T> The type of the model.
     * @param <Y> The type of the resource.
     * @return A new {@link ModelLoader} for the given model class.
     */
    public static <T, Y> ModelLoader<T, Y> buildModelLoader(Class<T> modelClass, Class<Y> resourceClass,
            Context context) {
         if (modelClass == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unable to load null model, setting placeholder only");
            }
            return null;
        }
        return Glide.get(context).getLoaderFactory().buildModelLoader(modelClass, resourceClass);
    }
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
    ...
}
```

　　在构建 ModelLoader 对象的时候，先调用课一个 Glide.get() 方法，而这个方法就是关键。

　　get() 方法中实现的是一个单例功能，而创建 Glide 对象则是调用 GlideBuilder 的 createGlide() 方法来创建的。

### 3.2. GlideBuilder#createGlide

```java
/**
 * A builder class for setting default structural classes for Glide to use.
 */
public class GlideBuilder {
    ...
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

　　这里就是构建 Glide 对象的地方了。在 createGlide() 方法中会发现 new 出了一个 LruResourceCache，并把它赋值到了 memoryCache 这个对象上面。这个就是 Glide 实现内存缓存所使用的 LruCache 对象了。








## 参考文章
[Android图片加载框架最全解析（三），深入探究Glide的缓存机制](https://blog.csdn.net/guolin_blog/article/details/54895665)


































