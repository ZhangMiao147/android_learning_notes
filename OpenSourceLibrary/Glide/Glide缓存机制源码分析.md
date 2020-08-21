# Glide 的缓存机制源码分析

# 1. Glide 缓存简介

　　Glide 的缓存设计可以说是非常先进的，考虑的场景也很周全。在缓存这一功能上，Glide 又将它分成了两个模块，一个是内存缓存，一个是硬件缓存。

　　这两个缓存模块的作用各不相同，内存缓存的主要作用是防止应用重复将图片数据读取到内存当中，而硬盘缓存的主要作用是防止应用重复从网络或其他地方重复下载和读取数据。

　　内存缓存和硬盘缓存的相互结合才构成了 Glide 极佳的图片缓存效果。

# 2. 缓存 Key

　　既然是缓存功能，就必然会有用于进行缓存的 Key。那么 Glide 的缓存 Key 是怎么生成的呢？Glide 的缓存 Key 生成规则非常繁琐，决定缓存 Key 的参数竟然有 10 个之多。不过逻辑还是比较简单的。

　　生成缓存 Key 的代码在 Engine 类的 load() 方法当中开始。

## 2.1. Engine#load

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

# 3. 内存缓存

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

　　Glide 内存缓存的实现是使用的 LruCache 算法，LruCache 算法（Least Recently Used）也叫近期最少使用算法。它的主要算法原理就是把最近使用的对象用强引用存储在 LinkedHashMap 中，并且把最近最少使用的对象在缓存值达到预设定值之前从内存中移除。Glide 还结合了一种弱引用的机制，共同完成了内存缓存功能。

## 3.1. 从内存缓存中获取

　　在 load() 方法中，调用了 RequestManager 的  loadGeneric() 方法，而 loadGeneric() 方法中会调用 Glide.buildStreamModelLoader() 方法来获取一个 ModelLoader 对象。

### 3.1.1. Glide#buildStreamModelLoader

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

　　在构建 ModelLoader 对象的时候，先调用了一个 Glide.get() 方法，而这个方法就是关键。

　　get() 方法中实现的是一个单例功能，而创建 Glide 对象则是调用 GlideBuilder 的 createGlide() 方法来创建的。

### 3.1.2. GlideBuilder#createGlide

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
		// 内存缓存
        if (memoryCache == null) {
            memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());
        }
		// 磁盘缓存
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

　　现在创建好了 LruResourceCache 对象只能说是把准备工作做好了，接下来研究 Glide 中的内存缓存到底是如何实现的。

　　Engine 的 load() 方法中有生成缓存 Key 的代码，而内存缓存的代码也是在这里实现的。

### 3.1.3. Engine#load

```java
    /**
     * Starts a load for the given arguments. Must be called on the main thread.
     *
     * <p>
     *     The flow for any request is as follows:
     *     <ul>
     *         <li>Check the memory cache and provide the cached resource if present</li>
     *         <li>Check the current set of actively used resources and return the active resource if present</li>
     *         <li>Check the current set of in progress loads and add the cb to the in progress load if present</li>
     *         <li>Start a new load</li>
     *     </ul>
     * </p>
     *
     * <p>
     *     Active resources are those that have been provided to at least one request and have not yet been released.
     *     Once all consumers of a resource have released that resource, the resource then goes to cache. If the
     *     resource is ever returned to a new consumer from cache, it is re-added to the active resources. If the
     *     resource is evicted from the cache, its resources are recycled and re-used if possible and the resource is
     *     discarded. There is no strict requirement that consumers release their resources so active resources are
     *     held weakly.
     * </p>
     *
     * @param signature A non-null unique key to be mixed into the cache key that identifies the version of the data to
     *                  be loaded.
     * @param width The target width in pixels of the desired resource.
     * @param height The target height in pixels of the desired resource.
     * @param fetcher The fetcher to use to retrieve data not in the disk cache.
     * @param loadProvider The load provider containing various encoders and decoders use to decode and encode data.
     * @param transformation The transformation to use to transform the decoded resource.
     * @param transcoder The transcoder to use to transcode the decoded and transformed resource.
     * @param priority The priority with which the request should run.
     * @param isMemoryCacheable True if the transcoded resource can be cached in memory.
     * @param diskCacheStrategy The strategy to use that determines what type of data, if any,
     *                          will be cached in the local disk cache.
     * @param cb The callback that will be called when the load completes.
     *
     * @param <T> The type of data the resource will be decoded from.
     * @param <Z> The type of the resource that will be decoded.
     * @param <R> The type of the resource that will be transcoded from the decoded resource.
     */
    public <T, Z, R> LoadStatus load(Key signature, int width, int height, DataFetcher<T> fetcher,
            DataLoadProvider<T, Z> loadProvider, Transformation<Z> transformation, ResourceTranscoder<Z, R> transcoder,
            Priority priority, boolean isMemoryCacheable, DiskCacheStrategy diskCacheStrategy, ResourceCallback cb) {
        Util.assertMainThread();
        long startTime = LogTime.getLogTime();

        final String id = fetcher.getId();
        EngineKey key = keyFactory.buildKey(id, signature, width, height, loadProvider.getCacheDecoder(),
                loadProvider.getSourceDecoder(), transformation, loadProvider.getEncoder(),
                transcoder, loadProvider.getSourceEncoder());

        // 获取缓存图片
        EngineResource<?> cached = loadFromCache(key, isMemoryCacheable);
        if (cached != null) {
            cb.onResourceReady(cached);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Loaded resource from cache", startTime, key);
            }
            return null;
        }

        EngineResource<?> active = loadFromActiveResources(key, isMemoryCacheable);
        if (active != null) {
            cb.onResourceReady(active);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Loaded resource from active resources", startTime, key);
            }
            return null;
        }

        EngineJob current = jobs.get(key);
        if (current != null) {
            current.addCallback(cb);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Added to existing load", startTime, key);
            }
            return new LoadStatus(cb, current);
        }

        EngineJob engineJob = engineJobFactory.build(key, isMemoryCacheable);
        DecodeJob<T, Z, R> decodeJob = new DecodeJob<T, Z, R>(key, width, height, fetcher, loadProvider, transformation,
                transcoder, diskCacheProvider, diskCacheStrategy, priority);
        EngineRunnable runnable = new EngineRunnable(engineJob, decodeJob, priority);
        jobs.put(key, engineJob);
        engineJob.addCallback(cb);
        engineJob.start(runnable);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Started new load", startTime, key);
        }
        return new LoadStatus(cb, engineJob);
    }
```

　　方法中调用了 lodeFromCache() 方法来获取缓存图片，如果获取到就直接调用 cd.onResourceReady() 方法进行回调。如果没有获取到，则会调用 loadFromActiveResources() 方法来获取缓存图片，获取到的话也直接进行回调。只有在这两个方法都没有获取到缓存的情况下，才会继续向下执行，从而开启线程来加载图片。

　　也就是说，Glide 的图片加载过程中会调用两个方法来获取内存缓存，loadFromCache() 和 loadFromActiveResources()。这两个方法中一个使用的就是 LruCache 算法，另一个使用的就是弱引用。

### 3.1.4. Engine#loadFromCache#loadFromActiveResources

```java
/**
 * Responsible for starting loads and managing active and cached resources.
 */
public class Engine implements EngineJobListener,
        MemoryCache.ResourceRemovedListener,
        EngineResource.ResourceListener {
        
    private final MemoryCache cache;
    private final Map<Key, WeakReference<EngineResource<?>>> activeResources;
   
    private EngineResource<?> loadFromCache(Key key, boolean isMemoryCacheable) {
        if (!isMemoryCacheable) {
            return null;
        }

        EngineResource<?> cached = getEngineResourceFromCache(key);
        if (cached != null) {
            cached.acquire();
            activeResources.put(key, new ResourceWeakReference(key, cached, getReferenceQueue()));
        }
        return cached;
    }
            
    @SuppressWarnings("unchecked")
    private EngineResource<?> getEngineResourceFromCache(Key key) {
        Resource<?> cached = cache.remove(key);

        final EngineResource result;
        if (cached == null) {
            result = null;
        } else if (cached instanceof EngineResource) {
            // Save an object allocation if we've cached an EngineResource (the typical case).
            result = (EngineResource) cached;
        } else {
            result = new EngineResource(cached, true /*isCacheable*/);
        }
        return result;
    }    
            
    private EngineResource<?> loadFromActiveResources(Key key, boolean isMemoryCacheable) {
        if (!isMemoryCacheable) {
            return null;
        }

        EngineResource<?> active = null;
        WeakReference<EngineResource<?>> activeRef = activeResources.get(key);
        if (activeRef != null) {
            active = activeRef.get();
            if (active != null) {
                active.acquire();
            } else {
                activeResources.remove(key);
            }
        }

        return active;
    }
}            
```

　　在 loadFromCache() 方法的一开始，首先就判断了 isMemoryCacheable 是不是 false，如果是 false 的话就直接返回 null。如果调用了 skipMemoryCache() 方法传入 true，那么这里的 isMemoryCacheable 就会是 false，标识内存缓存已被禁用。

　　接着调用了 getEngineResourceFromCache() 方法来获取缓存。在这个方法中，会使用缓存 Key 来从 cache 当中取值，而这里的 cache 对象就是在创建 Glide 对象时创建的 LruResourceCache，那么说明这里其实使用的就是 LruCache 算法了。

　　但是，当从 LruResourceCache 中获取到缓存图片之后会将它从缓存中移除，然后将这个缓存图片存储到 activeResources 当中。activeResouces 就是一个弱引用的 HashMap，用来缓存正在使用中的图片，可以看到，loadFromActiveResources() 方法就是从 activeResources 这个 HashMap 当中取值的。使用 activeResources 来缓存正在使用中的图片，可以保护这些图片不会被 LruCache 算法回收掉。

　　从内存缓存中获取数据的逻辑大概就是这些了。概括一下来说，就是如果能从内存缓存当中读取到要加载的图片，那么就直接进行回调，如果读取不到的话，才会开启线程执行后面的图片加载逻辑。

## 3.2. 写入内存缓存

　　在图片加载完成之后，会在 EngineJob 当中通过 Handler 发送一条消息将执行逻辑切回到主线程当中，从而执行 handleResultOnMainThread() 方法。

### 3.2.1. EngineJob#handleResuleOnMainThread

```java
/**
 * A class that manages a load by adding and removing callbacks for for the load and notifying callbacks when the
 * load completes.
 */
class EngineJob implements EngineRunnable.EngineRunnableManager {

    private void handleResultOnMainThread() {
        if (isCancelled) {
            resource.recycle();
            return;
        } else if (cbs.isEmpty()) {
            throw new IllegalStateException("Received a resource without any callbacks to notify");
        }
        engineResource = engineResourceFactory.build(resource, isCacheable);
        hasResource = true;

        // Hold on to resource for duration of request so we don't recycle it in the middle of notifying if it
        // synchronously released by one of the callbacks.
        engineResource.acquire();
        listener.onEngineJobComplete(key, engineResource);

        for (ResourceCallback cb : cbs) {
            if (!isInIgnoredCallbacks(cb)) {
                engineResource.acquire();
                cb.onResourceReady(engineResource);
            }
        }
        // Our request is complete, so we can release the resource.
        engineResource.release();
    }
    
    // Visible for testing.
    static class EngineResourceFactory {
        public <R> EngineResource<R> build(Resource<R> resource, boolean isMemoryCacheable) {
            return new EngineResource<R>(resource, isMemoryCacheable);
        }
    }

}
```

　　这里通过 EngineResourceFactory 构建出了一个包含图片资源的 EngineResource 对象，然后将这个对象回调到了 Engine 的 onEngineJobComplete() 方法当中。

### 3.2.2. Engine#onEngineJobComplete

```java
    @SuppressWarnings("unchecked")
    @Override
    public void onEngineJobComplete(Key key, EngineResource<?> resource) {
        Util.assertMainThread();
        // A null resource indicates that the load failed, usually due to an exception.
        if (resource != null) {
            resource.setResourceListener(key, this);

            if (resource.isCacheable()) {
                activeResources.put(key, new ResourceWeakReference(key, resource, getReferenceQueue()));
            }
        }
        // TODO: should this check that the engine job is still current?
        jobs.remove(key);
    }
```

　　可以看到，回调回来的 EngineResource 被 out 到了 activeResources 方法，也就是在这里写入的缓存。

## 3.3. LruCache 缓存

　　上面的只是弱引用缓存，接下来看 LruCache 缓存。

　　EngineResource 中的一个引用机制：观察刚才的 handleResultOnMainThread() 方法，有调用 EngineResource 的 acquire() 方法，接着又调用了 EngineResource 的 release() 方法。其实，EngineResource 是用一个 acquired 变量用来记录图片被引用的次数，调用 acquire() 方法会让变量加 1，调用 release() 方法会让变量减 1。

### 3.3.1. EngineResource#acquire#release

```java
/**
 * A wrapper resource that allows reference counting a wrapped {@link com.bumptech.glide.load.engine.Resource}
 * interface.
 *
 * @param <Z> The type of data returned by the wrapped {@link Resource}.
 */
class EngineResource<Z> implements Resource<Z> {
    /**
     * Increments the number of consumers using the wrapped resource. Must be called on the main thread.
     *
     * <p>
     *     This must be called with a number corresponding to the number of new consumers each time new consumers
     *     begin using the wrapped resource. It is always safer to call acquire more often than necessary. Generally
     *     external users should never call this method, the framework will take care of this for you.
     * </p>
     */
    void acquire() {
        if (isRecycled) {
            throw new IllegalStateException("Cannot acquire a recycled resource");
        }
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalThreadStateException("Must call acquire on the main thread");
        }
        ++acquired;
    }

    /**
     * Decrements the number of consumers using the wrapped resource. Must be called on the main thread.
     *
     * <p>
     *     This must only be called when a consumer that called the {@link #acquire()} method is now done with the
     *     resource. Generally external users should never callthis method, the framework will take care of this for
     *     you.
     * </p>
     */
    void release() {
        if (acquired <= 0) {
            throw new IllegalStateException("Cannot release a recycled or not yet acquired resource");
        }
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalThreadStateException("Must call release on the main thread");
        }
        if (--acquired == 0) {
            listener.onResourceReleased(key, this);
        }
    }
}  
```

　　当 acquired 变量大于 0 的时候，说明图片正在使用中，也就应该放在 activeResources 弱引用缓存当中，而经过 release() 之后，如果 acquired 变量等于 0 了，说明图片已经不再被使用了，那么此时会调用 listener 的 onResourceReleased() 方法来释放资源，这个 listener 就是 Engine 对象。

### 3.3.2. Engine#onResourceReleased

```java
    @Override
    public void onResourceReleased(Key cacheKey, EngineResource resource) {
        Util.assertMainThread();
        activeResources.remove(cacheKey);
        if (resource.isCacheable()) {
            cache.put(cacheKey, resource);
        } else {
            resourceRecycler.recycle(resource);
        }
    }
```

　　可以看到，这里首先会将缓存图片从 activeResources 中删除，然后再将它 put 到 LruResourceCache 当中。这样也就实现了正在使用中的图片使用弱引用来进行缓存，不在使用中的图片使用 LruCache 来进行缓存的功能。

# 4. 硬盘缓存

　　禁止 Glide 对图片进行硬件缓存使用的代码是：

```java
Glide.with(this)
     .load(url)
     .diskCacheStrategy(DiskCacheStrategy.NONE)
     .into(imageView);
```

　　调用 diskCacheStrategy() 方法并传入 DiskCacheStrategy.NONE，就可以禁用掉 Glide 的硬盘缓存功能了。

　　这个 diskCacheStrategy() 方法基本就是 Glide 硬盘缓存功能的一切，它可以接收四种参数：

* DiskCacheStrategy.NONE：表示不缓存任何内容。
* DiskCacheStrategy.SOURCE：表示只缓存原始图片。
* DiskCacheStrategy.RESULT：表示只缓存转换过后的图片（默认选项）。
* DiskCacheStrategy.ALL：表示既缓存原始图片，也缓存转换过后的图片。

　　用 Glide 去加载一张图片的时候，Glide 默认并不会将原始图片展示出来，而是会对图片进行压缩和转换。总之就是经过种种一系列操作之后得到的图片，就叫转换过后的图片，而 Glide 默认情况下在硬盘缓存的就是转换过后的图片，通过调用 diskCacheStrategy() 方法则可以改变这一默认行为。

　　和内存缓存类似，硬盘缓存的实现也是使用 LruCache 算法，而且 Google 还提供了一个现成的工具类 DiskLruCache。

## 4.1. 从硬件缓存中读取

　　Glide 开启线程来加载图片后会执行 EngineRunnable 的 run 方法，run() 方法中又会调用一个 decode() 方法。

### 4.1.1. EngineRunnable#decode

```java
class EngineRunnable implements Runnable, Prioritized {

    private Resource<?> decode() throws Exception {
        if (isDecodingFromCache()) {
            return decodeFromCache();
        } else {
            return decodeFromSource();
        }
    }

}
```

　　可以看到，这里会分为两种情况，一种是调用 decodeFromCache() 方法从硬盘缓存当中读取图片，一种是调用 decodeFromSource() 方法读取原始图片。

　　默认情况下 Glide 会优先从缓存当中读取，只有缓存中不存在要读取的图片时，才会去读取原始图片。

### 4.1.2. EngineRunnable#decodeFromCache

```java
    private Resource<?> decodeFromCache() throws Exception {
        Resource<?> result = null;
        try {
            result = decodeJob.decodeResultFromCache();
        } catch (Exception e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Exception decoding result from cache: " + e);
            }
        }

        if (result == null) {
            result = decodeJob.decodeSourceFromCache();
        }
        return result;
    }
```

　　可以看到，这里会先去调用 DecodeJob 的 decodeResultFromCache() 方法来获取缓存，如果获取不到，会再调用 decodeSourceFromCache() 方法获取缓存，这两个方法的区别其实就是 DiskCacheStrategy.RESULT 和 DiskCacheStrategy.SOURCE 这两个参数的区别。

### 4.1.3. DecodeJob#decodeResultFromCache#decodeSourceFromCache

```java
    /**
     * Returns a transcoded resource decoded from transformed resource data in the disk cache, or null if no such
     * resource exists.
     *
     * @throws Exception
     */
    public Resource<Z> decodeResultFromCache() throws Exception {
        if (!diskCacheStrategy.cacheResult()) {
            return null;
        }

        long startTime = LogTime.getLogTime();
        Resource<T> transformed = loadFromCache(resultKey);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Decoded transformed from cache", startTime);
        }
        startTime = LogTime.getLogTime();
        Resource<Z> result = transcode(transformed);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transcoded transformed from cache", startTime);
        }
        return result;
    }

    /**
     * Returns a transformed and transcoded resource decoded from source data in the disk cache, or null if no such
     * resource exists.
     *
     * @throws Exception
     */
    public Resource<Z> decodeSourceFromCache() throws Exception {
        if (!diskCacheStrategy.cacheSource()) {
            return null;
        }

        long startTime = LogTime.getLogTime();
        Resource<T> decoded = loadFromCache(resultKey.getOriginalKey());
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Decoded source from cache", startTime);
        }
        return transformEncodeAndTranscode(decoded);
    }
```

　　可以看到，它们都是调用了 loadFromCache() 方法从缓存当中读取数据，如果是 decodeResultFromCache() 方法就直接将数据解析并返回，如果是 decodeSourceFromCache() 方法，还要调用一下 transformEncodeAndTranscode() 方法先将数据转换一下再解析并返回。

　　这两个方法中在调用 loadFromCache() 方法时传入的参数却不一样，一个传入的是 resultKey，另外一个却又调用了 resultKey 的 getOriginalKey() 方法。Glide 的缓存 Key 是由 10 个参数共同组成的，包括图片的 width、height 等等。但如果要缓存的原始图片，其实并不需要这么多的参数，因为不用对图片做任何的变化。

### 4.1.4. EngineKey#getOriginalKey

```java
    public Key getOriginalKey() {
        if (originalKey == null) {
            originalKey = new OriginalKey(id, signature);
        }
        return originalKey;
    }
```

　　可以看到，这里其实就是忽略了绝大部分的参数，只使用了 id 和 signature 这两个参数来构成缓存 Key。而 signature 参数绝大多数情况下都是用不到的，因此基本上可以说就是由 id （也就是图片 url）来决定的 Original 缓存 Key。

### 4.1.5. DecodeJob#loadFromCache

```java
    private Resource<T> loadFromCache(Key key) throws IOException {
        File cacheFile = diskCacheProvider.getDiskCache().get(key);
        if (cacheFile == null) {
            return null;
        }

        Resource<T> result = null;
        try {
            result = loadProvider.getCacheDecoder().decode(cacheFile, width, height);
        } finally {
            if (result == null) {
                diskCacheProvider.getDiskCache().delete(key);
            }
        }
        return result;
    }
```

　　这个方法的逻辑非常简单，调用 getDiskCache() 方法获取到的就是 Glide 自己编写的 DiskLruCache 工具类的实例，然后调用它的 get() 方法并把缓存 Key 传入，就能得到硬件缓存的文件了。如果文件为空就返回 null，如果文件不为空则将它解码成 Resource 对象后返回即可。

## 4.2. 写入硬盘缓存

　　在 EngineRunnable 的 decode() 方法中，在没有缓存的情况下，会调用 decodeFromSource() 方法来读取原始图片，而 deocderFromSource() 方法调用了 DecodeJob 的 decodeFromSource() 方法。

### 4.2.1. DecodeJob#decodeFromSource

```java
    /**
     * Returns a transformed and transcoded resource decoded from source data, or null if no source data could be
     * obtained or no resource could be decoded.
     *
     * <p>
     *     Depending on the {@link com.bumptech.glide.load.engine.DiskCacheStrategy} used, source data is either decoded
     *     directly or first written to the disk cache and then decoded from the disk cache.
     * </p>
     *
     * @throws Exception
     */
    public Resource<Z> decodeFromSource() throws Exception {
        Resource<T> decoded = decodeSource();// SOURCE 资源的缓存会在这里
        return transformEncodeAndTranscode(decoded); // RESULT 资源的缓存在这里
    }
```

　　这个方法中只有两行代码，decodeSource() 顾名思义是用来解析原图片的，而 transformEncodeAndTranscode() 则是用来对图片进行转码和解码的。

### 4.2.2. DecodeJob#decodeSource

```java
    private Resource<T> decodeSource() throws Exception {
        Resource<T> decoded = null;
        try {
            long startTime = LogTime.getLogTime();
            // 加载拿到数据
            final A data = fetcher.loadData(priority);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Fetched data", startTime);
            }
            if (isCancelled) {
                return null;
            }
            // 解析数据
            decoded = decodeFromSourceData(data);
        } finally {
            fetcher.cleanup();
        }
        return decoded;
    }


    private Resource<T> decodeFromSourceData(A data) throws IOException {
        final Resource<T> decoded;
        // 检查缓存
        if (diskCacheStrategy.cacheSource()) {
            decoded = cacheAndDecodeSourceData(data);
        } else {
            long startTime = LogTime.getLogTime();
            decoded = loadProvider.getSourceDecoder().decode(data, width, height);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Decoded from source", startTime);
            }
        }
        return decoded;
    }

    private Resource<T> cacheAndDecodeSourceData(A data) throws IOException {
        long startTime = LogTime.getLogTime();
        SourceWriter<A> writer = new SourceWriter<A>(loadProvider.getSourceEncoder(), data);
        // 将数据存储到硬盘缓存中
        diskCacheProvider.getDiskCache().put(resultKey.getOriginalKey(), writer);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Wrote source to cache", startTime);
        }

        startTime = LogTime.getLogTime();
        Resource<T> result = loadFromCache(resultKey.getOriginalKey());
        if (Log.isLoggable(TAG, Log.VERBOSE) && result != null) {
            logWithTimeAndKey("Decoded source from cache", startTime);
        }
        return result;
    }
```

　　在 decodeSource() 方法中会先调用 fetcher 的 loadData() 方法读取图片数据，然后调用 decodeFromSourceData() 方法来对图片进行解码。

　　在 decodeFromSourceData() 方法中先判断是否允许缓存原始图片，如果允许的话又会调用 cacheAndDecodeSourceData() 方法。而在这个方法中同样调用了 getDiskCache() 方法来获取 DiskLruCache 实例，接着调用它的 put() 方法就可以写入磁盘缓存了，注意原始图片的缓存 Key 使用的 result.getOriginalKey()。

　　原始图片的缓存写入就是这么简单。接着来看 transformEncodeAndTranscode() 方法如何写入转换过后的图片缓存。

### 4.2.3. DecodeJob#transformEncodeAndTranscode

```java
    private Resource<Z> transformEncodeAndTranscode(Resource<T> decoded) {
        long startTime = LogTime.getLogTime();
        Resource<T> transformed = transform(decoded);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transformed resource from source", startTime);
        }
		// 调用 writeTransformedToCache() 方法
        writeTransformedToCache(transformed);

        startTime = LogTime.getLogTime();
        // 转换数据格式
        Resource<Z> result = transcode(transformed);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transcoded transformed from source", startTime);
        }
        return result;
    }

    private void writeTransformedToCache(Resource<T> transformed) {
        if (transformed == null || !diskCacheStrategy.cacheResult()) {
            return;
        }
        long startTime = LogTime.getLogTime();
        SourceWriter<Resource<T>> writer = new SourceWriter<Resource<T>>(loadProvider.getEncoder(), transformed);
        // 缓存
        diskCacheProvider.getDiskCache().put(resultKey, writer);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Wrote transformed from source to cache", startTime);
        }
    }
```

　　在 transformEncodeAndTranscode() 方法中先是调用 transform() 方法来对图片进行转换，然后在 writeTransformedToCache() 方法中将转换过后的图片写入到硬盘缓存中，调用的同样是 DiskLruCache 实例的 put() 方法，不过这里用的缓存 Key 是 resultKey。

　　到这里 Glide 硬盘缓存的实现原理也分析完了。

# 5. 七牛云问题

## 5.1. 问题描述

　　在使用七牛云来保存图片的时候有个问题：七牛云为了对图片资源进行保护，会在图片 url 地址的基础之上再加上一个 token 参数。也就是说，一张图片的 url 地址可能会是如下格式：

```java
http://url.com/image.jpg?token=d9caa6e02c990b0a
```

　　而使用 Glide 加载这张图片的话，也就会使用这个 url 地址来组成缓存 Key。那么 token 作为一个验证身份的参数并不是一成不变的，很可能时时刻刻都在变化，而如果 token 变了，那么图片的 url 也就跟着变了，图片 url 变了，缓存 Key 也就跟着变了。结果就造成了，明明是同一张图片，就因为 token 不断在变化，导致 Glide 的缓存功能完全失效了。

## 5.2. 问题分析

　　先从源码的层面进行分析，首先再来看一下 Glide 生成 Key 这部分的代码。

### 5.2.1. Engine#load

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

　　这里的 id 其实就是图片的 url 地址。那么，这里是通过调用 fetcher.getId() 方法来获取的图片 url 地址。那么，这里是通过调用 fetcher.getId() 方法来获取的图片 url 地址，fetcher 是 HttpUrlFetcher 的实例。

### 5.2.2. HttpUrlFetcher#getId

```java
/**
 * A DataFetcher that retrieves an {@link java.io.InputStream} for a Url.
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {
    private final GlideUrl glideUrl;
    
    public HttpUrlFetcher(GlideUrl glideUrl) {
        this(glideUrl, DEFAULT_CONNECTION_FACTORY);
    }

    // Visible for testing.
    HttpUrlFetcher(GlideUrl glideUrl, HttpUrlConnectionFactory connectionFactory) {
        this.glideUrl = glideUrl;
        this.connectionFactory = connectionFactory;
    }

   	@Override
    public String getId() {
        return glideUrl.getCacheKey();
    }
}
```

　　可以看到，getId() 方法中又调用了 GlideUrl 的 getCacheKey() 方法，而这个 GlideUrl 就是在 lode() 方法中传入的图片 url 地址，然后 Glide 在内部把这个 url 地址包装成了一个 GlideUrl 对象。

### 5.2.3. GlideUrl 类

``` java
/**
 * A wrapper for strings representing http/https URLs responsible for ensuring URLs are properly escaped and avoiding
 * unnecessary URL instantiations for loaders that require only string urls rather than URL objects.
 *
 * <p>  Users wishing to replace the class for handling URLs must register a factory using GlideUrl. </p>
 *
 * <p> To obtain a properly escaped URL, call {@link #toURL()}. To obtain a properly escaped string URL, call
 * {@link #toStringUrl()}. To obtain a less safe, but less expensive to calculate cache key, call
 * {@link #getCacheKey()}. </p>
 *
 * <p> This class can also optionally wrap {@link com.bumptech.glide.load.model.Headers} for convenience. </p>
 */
public class GlideUrl {
    ...
    private final URL url;
    ...
    private final String stringUrl;

    ...

    public GlideUrl(URL url) {
        this(url, Headers.DEFAULT);
    }

    public GlideUrl(String url) {
        this(url, Headers.DEFAULT);
    }

    public GlideUrl(URL url, Headers headers) {
        ...
        this.url = url;
        stringUrl = null;
        ...
    }

    public GlideUrl(String url, Headers headers) {
        ...
        this.stringUrl = url;
        this.url = null;
        ...
    }

    ...

    /**
     * Returns an inexpensive to calculate {@link String} suitable for use as a disk cache key.
     *
     * <p> This method does not include headers. </p>
     *
     * <p> Unlike {@link #toStringUrl()}} and {@link #toURL()}, this method does not escape input. </p>
     */
    public String getCacheKey() {
      return stringUrl != null ? stringUrl : url.toString();
    }

    ...
}
```

　　GlideUrl 类的构造函数接收两种类型的参数，一种是 url 字符串，一种是 URL 对象。然后 getCacheKey() 方法中的判断逻辑非常简单，如果传入的是 url 字符串，那么就直接返回这个字符串本身，如果传入的是 URL 对象，那么就返回这个对象 toString() 后的结果。

　　getCacheKey() 方法直接就是将图片的 url 地址进行返回来作为缓存 Key 的。那么解决方法就是重写这个 getCacheKey() 方法，加上一些自己的逻辑判断，就能轻松解决掉问题了。

## 5.3. 解决问题

　　创建一个 MyGlideUrl 继承自 GlideUrl，代码如下所示：

```java
public class MyGlideUrl extends GlideUrl {

    private String mUrl;

    public MyGlideUrl(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    public String getCacheKey() {
        return mUrl.replace(findTokenParam(), "");
    }

    private String findTokenParam() {
        String tokenParam = "";
        int tokenKeyIndex = mUrl.indexOf("?token=") >= 0 ? mUrl.indexOf("?token=") : mUrl.indexOf("&token=");
        if (tokenKeyIndex != -1) {
            int nextAndIndex = mUrl.indexOf("&", tokenKeyIndex + 1);
            if (nextAndIndex != -1) {
                tokenParam = mUrl.substring(tokenKeyIndex + 1, nextAndIndex + 1);
            } else {
                tokenParam = mUrl.substring(tokenKeyIndex);
            }
        }
        return tokenParam;
    }

}
```

　　重写了 getCacheKey() 方法，在里面加入了一段逻辑用于将图片 url 地址中 token 参数的这一部分移除掉。这样 getCacheKey() 方法得到的就是一个没有 token 参数的 url 地址，从而不管 token 怎么变化，最终 Glide 的缓存 key 都是固定不变的了。

　　定义好了 MyGlideUrl，还得使用它才行，将加载图片的代码改成如下即可：

```java
Glide.with(this)
     .load(new MyGlideUrl(url))
     .into(imageView);
```

　　也就是说，需要在 load() 方法中传入这个自定义的 MyGlideUrl 对象，而不能再像之前那样直接传入 url 字符串了。不然的话 Glide 在内部还是会使用原始的 GlideUrl 类，而不是自定义的 MyGlideUrl 类。

# 6. 参考文章

1. [Android图片加载框架最全解析（三），深入探究Glide的缓存机制](https://blog.csdn.net/guolin_blog/article/details/54895665)


































