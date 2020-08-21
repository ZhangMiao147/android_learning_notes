# Glide 自定义模块功能

　　自定义模块功能可以将更改 Glide 配置、替换 Glide 组件等操作独立出来，使得能轻松地对 Glide 的各种配置进行自定义，并且又和 Glide 的图片加载逻辑没有任何交集，这也是一种低耦合编程方式的体现。

# 1. 自定义模块的基本用法

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

### 4.1. Glide 组件源码分析

　　首先来看 Glide 中目前有哪些组件，在 Glide 类的构造方法当中，如下所示：

```java
/**
 * A singleton to present a simple static interface for building requests with {@link BitmapRequestBuilder} and
 * maintaining an {@link Engine}, {@link BitmapPool}, {@link com.bumptech.glide.load.engine.cache.DiskCache} and
 * {@link MemoryCache}.
 */
public class Glide {
    Glide(Engine engine, MemoryCache memoryCache, BitmapPool bitmapPool, Context context, DecodeFormat decodeFormat) {
        ...

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

        ...
    }
    
}
```

　　可以看到，这里都是以调用 register() 方法的方式来注册一个组件，register() 方法中传入的参数表示 Glide 支持使用那种参数类型来加载图片，以及如何去处理这种类型的图片加载。例如：

```java
register(GlideUrl.class, InputStream.class, new HttpUrlGlideUrlLoader.Factory());
```

　　这句代码就表示，可以使用 Glide.with(context).load(new GlideUrl(“url...”)).into(imageView) 的方式来加载图片，而 HttpUrlGlideLoader.Factory 则是要负责处理具体的网络通讯逻辑。如果想要将 Glide 的 HTTP 通讯组件替换成 OkHttp 的话，那么只需要再自定义模块当中重新注册一个 GlideUrl 类型的组件就行了。

　　在平时使用 Glide 加载图片时，大多数情况下都是直接将图片的 URL 字符串传入到 load() 方法当中的，很少会将它封装成 GlideUrl 对象之后再传入到 load() 方法当中，那为什么只需要重新注册一个 GlideUrl 类型的组件，而不需要去重新注册一个 String 类型的组件呢？其实道理很简单，因为 load(String) 方法只是 Glide 提供的一种简易的 API 封装而已，它的底层仍然还是调用的 GlideUrl 组件，因此再替换组件的时候只需要直接替换最底层的，这样就一步到位了。

#### 4.1.1. 查看 HttpUrlGlideUrlLoader 源码

　　Glide 的网络通讯逻辑是由 HttpUrlGlideUrlLoader.Factory 来负责的，那么查看一下它的源码。

```java
/**
 * An {@link com.bumptech.glide.load.model.ModelLoader} for translating {@link com.bumptech.glide.load.model.GlideUrl}
 * (http/https URLS) into {@link java.io.InputStream} data.
 */
public class HttpUrlGlideUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private final ModelCache<GlideUrl, GlideUrl> modelCache;

    /**
     * The default factory for {@link com.bumptech.glide.load.model.stream.HttpUrlGlideUrlLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private final ModelCache<GlideUrl, GlideUrl> modelCache = new ModelCache<GlideUrl, GlideUrl>(500);

        @Override
        public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new HttpUrlGlideUrlLoader(modelCache);
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public HttpUrlGlideUrlLoader() {
        this(null);
    }

    public HttpUrlGlideUrlLoader(ModelCache<GlideUrl, GlideUrl> modelCache) {
        this.modelCache = modelCache;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
        // GlideUrls memoize parsed URLs so caching them saves a few object instantiations and time spent parsing urls.
        GlideUrl url = model;
        if (modelCache != null) {
            url = modelCache.get(model, 0, 0);
            if (url == null) {
                modelCache.put(model, 0, 0, model);
                url = model;
            }
        }
        return new HttpUrlFetcher(url);
    }
}
```

　　可以看到，HttpUrlGlideUrlLoader.Factory 是一个内部类，外层的 HttpUrlGlideUrlLoader 类实现了 ModelLoader < GlideUrl,InputStream > ，并重写了 getResourceFetcher() 方法。而在 getResourceFetcher() 方法中，又创建了一个 HttpUrlFectcher 的实例，在这里才是真正处理具体网络通讯逻辑的地方。

#### 4.2.1. HttpUrlFetcher 类

```java
/**
 * A DataFetcher that retrieves an {@link java.io.InputStream} for a Url.
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "HttpUrlFetcher";
    private static final int MAXIMUM_REDIRECTS = 5;
    private static final HttpUrlConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultHttpUrlConnectionFactory();

    private final GlideUrl glideUrl;
    private final HttpUrlConnectionFactory connectionFactory;

    private HttpURLConnection urlConnection;
    private InputStream stream;
    private volatile boolean isCancelled;

    public HttpUrlFetcher(GlideUrl glideUrl) {
        this(glideUrl, DEFAULT_CONNECTION_FACTORY);
    }

    // Visible for testing.
    HttpUrlFetcher(GlideUrl glideUrl, HttpUrlConnectionFactory connectionFactory) {
        this.glideUrl = glideUrl;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        return loadDataWithRedirects(glideUrl.toURL(), 0 /*redirects*/, null /*lastUrl*/, glideUrl.getHeaders());
    }

    private InputStream loadDataWithRedirects(URL url, int redirects, URL lastUrl, Map<String, String> headers)
            throws IOException {
        if (redirects >= MAXIMUM_REDIRECTS) {
            throw new IOException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
        } else {
            // Comparing the URLs using .equals performs additional network I/O and is generally broken.
            // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new IOException("In re-direct loop");
                }
            } catch (URISyntaxException e) {
                // Do nothing, this is best effort.
            }
        }
        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
          urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(2500);
        urlConnection.setReadTimeout(2500);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        // Connect explicitly to avoid errors in decoders if connection fails.
        urlConnection.connect();
        if (isCancelled) {
            return null;
        }
        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (statusCode / 100 == 3) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new IOException("Received empty or null redirect url");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
        } else {
            if (statusCode == -1) {
                throw new IOException("Unable to retrieve response code from HttpUrlConnection.");
            }
            throw new IOException("Request failed " + statusCode + ": " + urlConnection.getResponseMessage());
        }
    }

    private InputStream getStreamForSuccessfulRequest(HttpURLConnection urlConnection)
            throws IOException {
        if (TextUtils.isEmpty(urlConnection.getContentEncoding())) {
            int contentLength = urlConnection.getContentLength();
            stream = ContentLengthInputStream.obtain(urlConnection.getInputStream(), contentLength);
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got non empty content encoding: " + urlConnection.getContentEncoding());
            }
            stream = urlConnection.getInputStream();
        }
        return stream;
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    @Override
    public String getId() {
        return glideUrl.getCacheKey();
    }

    @Override
    public void cancel() {
        // TODO: we should consider disconnecting the url connection here, but we can't do so directly because cancel is
        // often called on the main thread.
        isCancelled = true;
    }

    interface HttpUrlConnectionFactory {
        HttpURLConnection build(URL url) throws IOException;
    }

    private static class DefaultHttpUrlConnectionFactory implements HttpUrlConnectionFactory {
        @Override
        public HttpURLConnection build(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }
}

```

　　其实就是一些 HttpURLConnection 的用法而已。

### 4.2. 将 HTTP 通讯组件修改成 OkHttp

　　首先第一步，是先将 OkHttp 的库引入到当前项目中，如下所示：

```groovy
dependencies {
    compile 'com.squareup.okhttp3:okhttp:3.9.0'
}
```

　　接着仿照着 HttpUrlFetcher 的代码来写，并且把 HTTP 的通讯组件替换成 OkHttp 就可以了。

　　现在新建一个 OkHttpFetcher 类，并且同样实现 DataFetcher< InputStream > 接口，代码如下所示：

```java
public class OkHttpFetcher implements DataFetcher<InputStream> {

    private final OkHttpClient client;
    private final GlideUrl url;
    private InputStream stream;
    private ResponseBody responseBody;
    private volatile boolean isCancelled;

    public OkHttpFetcher(OkHttpClient client, GlideUrl url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url.toStringUrl());
        for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        requestBuilder.addHeader("httplib", "OkHttp");
        Request request = requestBuilder.build();
        if (isCancelled) {
            return null;
        }
        Response response = client.newCall(request).execute();
        responseBody = response.body();
        if (!response.isSuccessful() || responseBody == null) {
            throw new IOException("Request failed with code: " + response.code());
        }
        stream = ContentLengthInputStream.obtain(responseBody.byteStream(),
                responseBody.contentLength());
        return stream;
    }

    @Override
    public void cleanup() {
        try {
            if (stream != null) {
                stream.close();
            }
            if (responseBody != null) {
                responseBody.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return url.getCacheKey();
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }
}
```

　　上面的代码就是按照 HttpUrlFetcher 写出来的，用的也都是一些 OkHttp 的基本用法。可以看到，使用 OkHttp 来编写网络通讯的代码要比使用 HttpURLConnetcion 简单很多，代码行数也少了很多。而且添加了一个 httplib:OkHttp 的请求头。

　　接着仿照 HttpUrlGlideUrlLoader 再写一个 OkHttpGlideUrlLoader ，新建一个  OkHttpGlideLoader 类，并且实现 ModelLoader< GlideUrl，InputStram > 接口，代码如下：

```java
public class OkHttpGlideUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private OkHttpClient okHttpClient;

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {

        private OkHttpClient client;

        public Factory() {
        }

        public Factory(OkHttpClient client) {
            this.client = client;
        }

        private synchronized OkHttpClient getOkHttpClient() {
            if (client == null) {
                client = new OkHttpClient();
            }
            return client;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new OkHttpGlideUrlLoader(getOkHttpClient());
        }

        @Override
        public void teardown() {
        }
    }

    public OkHttpGlideUrlLoader(OkHttpClient client) {
        this.okHttpClient = client;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
        return new OkHttpFetcher(okHttpClient, model);
    }
}
```

　　注意这里的 Factory 提供了两个构造方法，一个是不带任何参数的，一个是带 OkHttpClient 参数的。如果对 OkHttp 不需要进行任何自定义的配置，那么就调用无参的 Factory 构造函数即可，这样会在内部自动创建一个 OkHttpClient 实例。但如果需要添加拦截器，或者修改 OkHttp 的默认超时等等配置，那么就自己创建一个 OkHttpClient 的实例，然后传入到 Factory 的构造方法当中就行了。

　　最后将创建的 OkHttpGlideUrlLoader 和 OkHttpFetcher 注册到 Glide 当中，将原来的 HTTP 通讯组件给替换掉，如下所示：

```java
public class MyGlideModule implements GlideModule {

    ...

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class, new OkHttpGlideUrlLoader.Factory());
    }

}
```

　　可以看到，这里也是调用了 Glide 的 register() 方法来注册组件的。register() 方法中使用的 Map 类型来存储已注册的组件，因此这里重新注册了一遍 GlideUrl.class 类型的组件，就把原来的组件给替换掉了。

　　然后再修改一下 Glide 加载图片的代码，如下所示：

```java
String url = "http://guolin.tech/book.png";
Glide.with(this)
     .load(url)
     .skipMemoryCache(true)
     .diskCacheStrategy(DiskCacheStrategy.NONE)
     .into(imageView);
```

## 5. 更简单的组件替换

　　Glide 官方提供了非常简便的 HTTP 组件替换方式，并且除了支持 OkHttp3 之外，还支持 OkHttp2 和 Volley。

　　只需要在 gradle 当中添加几行库的配置就行了。

### 5.1. 使用 OkHttp3

　　使用 OkHttp3 来作为 HTTP 通讯组件的配置如下：

```groovy
dependencies {
    compile 'com.squareup.okhttp3:okhttp:3.9.0'
    compile 'com.github.bumptech.glide:okhttp3-integration:1.5.0@aar'
}
```

### 5.2. 使用 OkHttp2

　　使用 OkHttp2 来作为 HTTP 通讯组件的配置如下：

```groovy
dependencies {
    compile 'com.github.bumptech.glide:okhttp-integration:1.5.0@aar'
    compile 'com.squareup.okhttp:okhttp:2.7.5'
}
```

### 5.3. 使用 Volley

　　使用 Volley 来作为 HTTP 通讯组件的配置如下：

```groovy
dependencies {
    compile 'com.github.bumptech.glide:volley-integration:1.5.0@aar'  
    compile 'com.mcxiaoke.volley:library:1.0.19'  
}
```

　　当然了，这些库背后的工作原理和自己手动实现替换 HTTP 组件的原理是一摸一样的。而学会了手动替换组件的原理就能更加轻松地扩展更多丰富地功能，因此掌握这一技能还是非常重要的。


## 6. 参考文章
1. [Android图片加载框架最全解析（六），探究Glide的自定义模块功能](https://blog.csdn.net/guolin_blog/article/details/78179422)




































