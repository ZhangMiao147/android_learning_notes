# Glide 执行基本流程源码分析1

　　Glide 的最基本的使用是：

```java
Glide.with(this).load(url).into(imageView);
```

　　所以本文就是通过阅读源码明白上面代码是如何实现将一张网络图片展示到 ImageView 上面的。

　　源码分析分为三部分，也就是 with()、load()、into() 这三个方法来分析。

　　这篇只分析 with() 和 load() 两个方法的源码流程。into() 的分析看[Glide 执行基本流程源码分析 2](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/Glide/Glide执行基本流程源码分析2.md)。

# 1. with()

　　with() 方法是 Glide 类中的一组静态方法，它有好几个方法重载。

```java
public class Glide {
    ...
    public static RequestManager with(Context context) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(context);
    }

    public static RequestManager with(Activity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }
    
    public static RequestManager with(FragmentActivity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static RequestManager with(android.app.Fragment fragment) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(fragment);
    }

    public static RequestManager with(Fragment fragment) {
      	// 调用 RequestManagerRetriever 的 get() 方法获取 RequestManagerRetriever 对象实例 retriever
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
      	// 调用 retriever 的 get() 方法获取 RequestManager 对象
        return retriever.get(fragment);
    }
    ...
}
```

　　可以看到，with() 方法的重载方法非常多，既可以传入 Activity，也可以传入 Fragment 或者是 Context。但是每一个 with() 方法重载的代码都非常简单：

1. 先调用 RequestManagerRetriever 的静态 get() 方法得到一个 RequestManagerRetriever 对象，这个静态 get() 方法就是一个单例实现。
2. 再调用 RequestManagerRetriever 的实例 get() 方法，去获取 RequestManager 对象。

　　所以 with() 方法返回的就是一个 RequestManager 对象。

## 1.1. RequestManagerRetriever#get

```java
/**
 * A collection of static methods for creating new {@link com.bumptech.glide.RequestManager}s or retrieving existing
 * ones from activities and fragment.
 */
public class RequestManagerRetriever implements Handler.Callback {
    private static final String TAG = "RMRetriever";
    static final String FRAGMENT_TAG = "com.bumptech.glide.manager";

    /** The singleton instance of RequestManagerRetriever. */
    private static final RequestManagerRetriever INSTANCE = new RequestManagerRetriever();

    private static final int ID_REMOVE_FRAGMENT_MANAGER = 1;
    private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;

    /** The top application level RequestManager. */
    private volatile RequestManager applicationManager;

    // Visible for testing.
    /** Pending adds for RequestManagerFragments. */
    final Map<android.app.FragmentManager, RequestManagerFragment> pendingRequestManagerFragments =
            new HashMap<android.app.FragmentManager, RequestManagerFragment>();

    // Visible for testing.
    /** Pending adds for SupportRequestManagerFragments. */
    final Map<FragmentManager, SupportRequestManagerFragment> pendingSupportRequestManagerFragments =
            new HashMap<FragmentManager, SupportRequestManagerFragment>();

    /** Main thread handler to handle cleaning up pending fragment maps. */
    private final Handler handler;

    /**
     * Retrieves and returns the RequestManagerRetriever singleton.
     */
    public static RequestManagerRetriever get() {
        return INSTANCE;
    }

    // Visible for testing.
    RequestManagerRetriever() {
        handler = new Handler(Looper.getMainLooper(), this /* Callback */);
    }

    private RequestManager getApplicationManager(Context context) {
        // Either an application context or we're on a background thread.
        if (applicationManager == null) {
            synchronized (this) {
                if (applicationManager == null) {
                    // Normally pause/resume is taken care of by the fragment we add to the fragment or activity.
                    // However, in this case since the manager attached to the application will not receive lifecycle
                    // events, we must force the manager to start resumed using ApplicationLifecycle.
                    applicationManager = new RequestManager(context.getApplicationContext(),
                            new ApplicationLifecycle(), new EmptyRequestManagerTreeNode());
                }
            }
        }

        return applicationManager;
    }

    public RequestManager get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("You cannot start a load on a null Context");
            // 如果是主线程，并且 context 不是 Application 对象
        } else if (Util.isOnMainThread() && !(context instanceof Application)) {
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return get((Activity) context);
            } else if (context instanceof ContextWrapper) {
                return get(((ContextWrapper) context).getBaseContext());
            }
        }

        // context 是 Application 对象或者不是在主线程（在非主线程当中使用 Glide，不管传入的 Activity 还是 Fragment，都会被强制当成 Application 来处理）
        return getApplicationManager(context);
    }

    public RequestManager get(FragmentActivity activity) {
        if (Util.isOnBackgroundThread()) {
          	// 是后台线程
          	// 调用 get() 方法，参数是 Application 的 context
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            FragmentManager fm = activity.getSupportFragmentManager();
          	// 调用 supportFragmentGet 方法
            return supportFragmentGet(activity, fm);
        }
    }

    public RequestManager get(Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("You cannot start a load on a fragment before it is attached");
        }
        if (Util.isOnBackgroundThread()) {
            // 是后台线程
          	// 调用 get() 方法，参数是 Application 的 context
            return get(fragment.getActivity().getApplicationContext());
        } else {
            FragmentManager fm = fragment.getChildFragmentManager();
          	// 调用 supportFragmentGet 方法
            return supportFragmentGet(fragment.getActivity(), fm);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public RequestManager get(Activity activity) {
        if (Util.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // 是后台线程
          	// 调用 get() 方法，参数是 Application 的 context
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            android.app.FragmentManager fm = activity.getFragmentManager();
          	// 调用 fragmentGet 方法
            return fragmentGet(activity, fm);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void assertNotDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public RequestManager get(android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("You cannot start a load on a fragment before it is attached");
        }
        if (Util.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 是后台线程
          	// 调用 get() 方法，参数是 Application 的 context
            return get(fragment.getActivity().getApplicationContext());
        } else {
            android.app.FragmentManager fm = fragment.getChildFragmentManager();
            // 调用 fragmentGet 方法
            return fragmentGet(fragment.getActivity(), fm);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    RequestManagerFragment getRequestManagerFragment(final android.app.FragmentManager fm) {
        RequestManagerFragment current = (RequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = pendingRequestManagerFragments.get(fm);
            if (current == null) {
                current = new RequestManagerFragment();
                pendingRequestManagerFragments.put(fm, current);
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    RequestManager fragmentGet(Context context, android.app.FragmentManager fm) {
        // RequestManagerFragment 是继承 Fragment 的
      	// 调用 getSupportRequestManagerFragment 方法
        RequestManagerFragment current = getRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(context, current.getLifecycle(), current.getRequestManagerTreeNode());
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }

    SupportRequestManagerFragment getSupportRequestManagerFragment(final FragmentManager fm) {
        SupportRequestManagerFragment current = (SupportRequestManagerFragment) fm.findFragmentByTag(


            FRAGMENT_TAG);
        if (current == null) {
            current = pendingSupportRequestManagerFragments.get(fm);
            if (current == null) {
                current = new SupportRequestManagerFragment();
                pendingSupportRequestManagerFragments.put(fm, current);
                // 向当前的 Activity 当中添加一个 Fragment
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }

    RequestManager supportFragmentGet(Context context, FragmentManager fm) {
      	// 调用 getSupportRequestManagerFragment 方法获取 SupportRequestManagerFragment 对象 current
        SupportRequestManagerFragment current = getSupportRequestManagerFragment(fm);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(context, current.getLifecycle(), current.getRequestManagerTreeNode());
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }

    @Override
    public boolean handleMessage(Message message) {
        boolean handled = true;
        Object removed = null;
        Object key = null;
        switch (message.what) {
            case ID_REMOVE_FRAGMENT_MANAGER:
                android.app.FragmentManager fm = (android.app.FragmentManager) message.obj;
                key = fm;
                removed = pendingRequestManagerFragments.remove(fm);
                break;
            case ID_REMOVE_SUPPORT_FRAGMENT_MANAGER:
                FragmentManager supportFm = (FragmentManager) message.obj;
                key = supportFm;
                removed = pendingSupportRequestManagerFragments.remove(supportFm);
                break;
            default:
                handled = false;
        }
        if (handled && removed == null && Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, "Failed to remove expected request manager fragment, manager: " + key);
        }
        return handled;
    }
}
```

　　RequestManagerRetriever 类中有很多个 get() 方法的重载，Context 参数、Activity 参数、Fragment 参数等等，但是实际上只有两种情况而已：

1. 传入 Application 类型的参数。
2. 传入非 Application 类型的参数。

　　传入 Application 参数的 get() 方法：如果在 Glide.with() 方法中传入的是一个 Application 对象，那么这里就会调用带有 Context 参数的 get() 方法重载，然后会调用 getApplicationManager() 方法来获取一个 RequestManager 对象。其实这是最简单的一种情况，因为 Application 对象的生命周期即应用程序的生命周期，因此 Glide 并不需要做什么特殊的处理，它自动就是和应用程序的生命周期是同步的，如果应用程序关闭的话，Glide 的加载也会同时终止。

　　传入非 Application 参数的 get() 方法：在使用 Glide.with() 方法中不管传入的是 Activity、FragmeActivity、v4 包下的 Fragment 还是 app 包下的 Fragment，最终的流程都是一样的，那就是调用 getSupportRequestManagerFragment 方法向当前的 Activity 当中添加一个隐藏的 Fragment。为什么要添加一个隐藏的 Fragment ? 因为 Glide 需要知道加载的生命周期。Glide 并没有办法知道 Activity 的生命周期，于是 Glide 就是用了添加隐藏 Fragment 这种小技巧，因为 Fragment 的生命周期和 Activity 是同步的，如果 Activity 被销毁了，Fragment 是可以监听到的，这样 Glide 就可以捕获这个事件并停止图片加载了。

## 1.2. with() 方法总结

　　with() 方法就是为了得到一个 RequestManager 对象而已，然后 Glide 会根据传入的 with() 方法的参数来确定图片加载的生命周期。

# 2. load()

　　with() 方法返回的是一个 RequestManager 对象，那么 lode() 方法就是在 RequestManager 类当中的。

```java
public class RequestManager implements LifecycleListener {
    ...
    // lode 的重载方法
        
    ...
    public DrawableTypeRequest<String> load(String string) {
        // 调用 fromString() 方法，返回一个 DrawableTypeRequest 对象
        // 调用 DrawableTypeRequest 的 load 方法
        // 最后返回的是一个 DrawableTypeRequest 对象
        return (DrawableTypeRequest<String>) fromString().load(string);
    }
    ...
}
    
```

　　RequestManager 中有很多 load() 方法的重载，这里只列出了加载图片 URL 字符串的 load() 方法来研究。

　　load() 方法的逻辑非常简单，就是先调用了 fromString() 方法，fromString() 方法返回的是一个 DrawableTypeRequest 对象，接着调用了 DrawableTypeRequest 的 load() 方法，然后把传入的图片 URL 地址传进去。

## 2.1. RequestManager#from

```java
    public DrawableTypeRequest<String> fromString() {
        // 调用 loadGeneric() 方法
        return loadGeneric(String.class);
    }
```

　　fromString() 方法也极为简单，就是调用了 loadGeneric() 方法，并且指定参数为 String.class，因为 load() 方法传入的是一个字符串参数。所以主要的工作是在 loadGeneric() 方法中进行的。

## 2.2. RequestManager#loadGeneric

```java
    private <T> DrawableTypeRequest<T> loadGeneric(Class<T> modelClass) {
      	// 创建 ModelLoader streamModelLoader 对象
        ModelLoader<T, InputStream> streamModelLoader = Glide.buildStreamModelLoader(modelClass, context);
      	// 创建 ModelLoader fileDescriptorModelLoader 对象
        ModelLoader<T, ParcelFileDescriptor> fileDescriptorModelLoader =
                Glide.buildFileDescriptorModelLoader(modelClass, context);
        if (modelClass != null && streamModelLoader == null && fileDescriptorModelLoader == null) {
            throw new IllegalArgumentException("Unknown type " + modelClass + ". You must provide a Model of a type for"
                    + " which there is a registered ModelLoader, if you are using a custom model, you must first call"
                    + " Glide#register with a ModelLoaderFactory for your custom model class");
        }

        return optionsApplier.apply(
          			// 创建了一个 DrawableTypeRequest 对象，
          			// 传递给 DrawableTypeRequest 创建的 streamModelLoader 和 fileDescriptorModelLoader
                new DrawableTypeRequest<T>(modelClass, streamModelLoader, fileDescriptorModelLoader, context,
                        glide, requestTracker, lifecycle, optionsApplier));
    }
```

　　loadGeneric() 方法分别调用了 Glide.buildStreamModelLoader() 方法和 Glide.buildFileDescriptorModelLoader() 方法来获得 ModelLoader 对象。ModelLoader 对象是用于加载图片的，而给 load() 方法传入不同类型的参数，这里也会得到不同的 ModelLoader 对象。由于传入的参数是 String.class，因此最终得到的是 StreamStringLoader 对象，它是实现了 ModelLoader 接口的。

　　最后，loadGeneric() 方法是要返回一个 DrawableTypeRequest 对象的，因此在 loadGeneric() 方法的最后又去 new 了一个 DrawableTypeRequest 对象，然后把刚才获得的 ModelLoader 对象，还有一些数据都传了进去。 

## 2.3. Glide.buildStreamModelLoader

```java
public class Glide {
   public static <T> ModelLoader<T, InputStream> buildStreamModelLoader(Class<T> modelClass, Context context) {
     	// 调用 buildModelLoader() 方法
        return buildModelLoader(modelClass, InputStream.class, context);
    }
    public static <T, Y> ModelLoader<T, Y> buildModelLoader(Class<T> modelClass, Class<Y> resourceClass,
            Context context) {
         if (modelClass == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unable to load null model, setting placeholder only");
            }
            return null;
        }
      	// 调用 Glide.get(context).getLoaderFactory().buildModelLoader(modelClass, resourceClass) 方法
        return Glide.get(context).getLoaderFactory().buildModelLoader(modelClass, resourceClass);
    }
}
```

　　Glide 的 buildStreamModelLoader() 方法中直接调用了 buildModelLoad() 方法，而在 buildModelLoad() 方法里面调用了 Glide.get(context).getLoaderFactory().buildModelLoader(modelClass, resourceClass) 这一段代码。

### 2.3.1. Glide#get()

```java
      private static volatile Glide glide; // 线程安全的单例模式
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
                    // 初始化 glide 对象
                    glide = builder.createGlide();
                    for (GlideModule module : modules) {
                        module.registerComponents(applicationContext, glide);
                    }
                }
            }
        }

        return glide;
    }
```

　　Glide 的 get() 方法其实就是初始化 Glide glide 对象，并返回。可以看到 Glide 是单例模式，调用了 build.createGlide() 来初始化 glide 对象。

### 2.3.2. GlideBuilder#createGlide

```java
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
		// 内存缓存的处理类
        if (memoryCache == null) {
            memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());
        }
		// 磁盘缓存的处理类
        if (diskCacheFactory == null) {
            diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }
		// 下载的处理类
        if (engine == null) {
            engine = new Engine(memoryCache, diskCacheFactory, diskCacheService, sourceService);
        }

        if (decodeFormat == null) {
            decodeFormat = DecodeFormat.DEFAULT;
        }
		// 调用 Glide 构造函数来创建对象
        return new Glide(engine, memoryCache, bitmapPool, context, decodeFormat);
    }
```

　　在 GlideBuilder 的 createGlide() 方法中初始化了一些加载图片中需要使用的对象，像是处理缓存的 memoryCache 和 diskCacheFactory，下载的 engine，最后调用了 Glide 的构造函数来创建一个 Glide 对象，并且将创建的对象都传递了进去。

### 2.3.3. Glide 的构造函数

```java
   Glide(Engine engine, MemoryCache memoryCache, BitmapPool bitmapPool, Context context, DecodeFormat decodeFormat) {
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.memoryCache = memoryCache;
        this.decodeFormat = decodeFormat;
        loaderFactory = new GenericLoaderFactory(context);
        mainHandler = new Handler(Looper.getMainLooper());
        bitmapPreFiller = new BitmapPreFiller(memoryCache, bitmapPool, decodeFormat);
		// 编解码器注册
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
		// 重点注意这里，ModelLoader 的工厂注册
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
```

　　在 Glide 的构造函数中，除了一些变量的初始化和赋值之后，还调用了 register() 方法。

　　register() 方法传入了三个参数，modelClass 就是调用 load() 方法是传入的参数类型，像是 load("http://image.png") 那么 modelClass 就是 String.class，而 resourceClass 在 Glide 的 buildStreamModelLoader 设置的是 InputStream.class ，而 factory 在 Glide 的构造函数中传入的是一个 `new StreamStringLoader.Factory()`。register() 方法的第三个参数是一个实现了 ModelLoaderFactory 接口的类。

### 2.3.4. Glide#register

```java
    public <T, Y> void register(Class<T> modelClass, Class<Y> resourceClass, ModelLoaderFactory<T, Y> factory) {
        ModelLoaderFactory<T, Y> removed = loaderFactory.register(modelClass, resourceClass, factory);
        if (removed != null) {
            removed.teardown();
        }
    }
```

　　Glide 的 register() 方法中继续调用了 loaderFactory.register() 方法，而 loadFactory 是  GenericLoaderFactory 对象。

### 2.3.5. GenericLoaderFactory#register

```java
public class GenericLoaderFactory {
    private final Map<Class/*T*/, Map<Class/*Y*/, ModelLoaderFactory/*T, Y*/>> modelClassToResourceFactories =
            new HashMap<Class, Map<Class, ModelLoaderFactory>>();
    // register 方法
	public synchronized <T, Y> ModelLoaderFactory<T, Y> register(Class<T> modelClass, Class<Y> resourceClass,
            ModelLoaderFactory<T, Y> factory) {
        cachedModelLoaders.clear();

        Map<Class/*Y*/, ModelLoaderFactory/*T, Y*/> resourceToFactories = modelClassToResourceFactories.get(modelClass);
        if (resourceToFactories == null) {
            resourceToFactories = new HashMap<Class/*Y*/, ModelLoaderFactory/*T, Y*/>();
            modelClassToResourceFactories.put(modelClass, resourceToFactories);
        }

        ModelLoaderFactory/*T, Y*/ previous = resourceToFactories.put(resourceClass, factory);

        if (previous != null) {
            // This factory may be being used by another model. We don't want to say it has been removed unless we
            // know it has been removed for all models.
            for (Map<Class/*Y*/, ModelLoaderFactory/*T, Y*/> factories : modelClassToResourceFactories.values()) {
                if (factories.containsValue(previous)) {
                    previous = null;
                    break;
                }
            }
        }

        return previous;
    }
}
```

　　GenericLoaderFactory 的 register() 方法将 modelClass、resourceClass、factory 信息都存储在了 modelClassToResourceFactories 变量中。

### 2.3.6. ModelLoaderFactory 接口

```java
/**
 * 用于为给定模型类创建 ModelLoader 的接口。
 * T：ModelLoader 模型的类型由这家工厂生产
 * Y：ModelLoader 的数据类型由这个工厂装载
 */
public interface ModelLoaderFactory<T, Y> {

    /**
     * 为此模型类型生成具体的模型加载器。
     * factories 是工厂类的映射，可用于构造此工厂的 ModelLoader
     */
    ModelLoader<T, Y> build(Context context, GenericLoaderFactory factories);

    /**
     * 当这个工厂即将被替换时将被调用的声明周期方法。
     */
    void teardown();
}
```

　　看到 ModelLoaderFactory 接口，可以看到 Glide 使用了工厂模式，对于不同的 modelClass 和 resourceClass 会调用不同的工厂去创建 ModelClass 对象。

　　对于传入 String.Class 和 InputStream.class ，ModelLoaderFactory 是一个 new StreamStringLoader.Factory()，接下来看一下这个对象。

### 2.3.7. StreamStringLoader

```java
public class StreamStringLoader extends StringLoader<InputStream> implements StreamModelLoader<String> {

    /**
     * The default factory for {@link com.bumptech.glide.load.model.stream.StreamStringLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<String, InputStream> {
        @Override
        public ModelLoader<String, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new StreamStringLoader(factories.buildModelLoader(Uri.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public StreamStringLoader(Context context) {
        this(Glide.buildStreamModelLoader(Uri.class, context));
    }

    public StreamStringLoader(ModelLoader<Uri, InputStream> uriLoader) {
        super(uriLoader);
    }
}
```

　　StreamStringLoader 的 Factory 内部类的 build() 方法返回的是一个 StreamStringLoader 对象，而 StreamStringLoader 继承 StringLoader类，而 StringLoader 是实现 ModelLoader 对象的。

　　在 Factory 的 build 的方法中创建 StreamStringLoader 对象时，传入的参数是 factories.buildModelLoader(Uri.class, InputStream.class)，这个方法返回的是一个 ModelLoader 对象。

### 2.3.8. GenericLoaderFactory#buildModelLoader

```java
    public synchronized <T, Y> ModelLoader<T, Y> buildModelLoader(Class<T> modelClass, Class<Y> resourceClass) {
        // 从缓存中取
        ModelLoader<T, Y> result = getCachedLoader(modelClass, resourceClass);
        if (result != null) {
            // We've already tried to create a model loader and can't with the currently registered set of factories,
            // but we can't use null to demonstrate that failure because model loaders that haven't been requested
            // yet will be null in the cache. To avoid this, we use a special signal model loader.
            if (NULL_MODEL_LOADER.equals(result)) {
                return null;
            } else {
                return result;
            }
        }
		// 缓存中没有，则调用 getFactory() 方法
        final ModelLoaderFactory<T, Y> factory = getFactory(modelClass, resourceClass);
        if (factory != null) {
            // 存入缓存
            result = factory.build(context, this);
            cacheModelLoader(modelClass, resourceClass, result);
        } else {
            // We can't generate a model loader for the given arguments with the currently registered set of factories.
            // 存入缓存
            cacheNullLoader(modelClass, resourceClass);
        }
        return result;
    }
```

　　GenericLoaderFactory 的 buildModelLoader() 方法中会先从缓存中获取，如果缓存中没有则调用 getFactory() 方法来得到 ModelLoaderFactory 对象，最后将 factory.build 得到的 ModelLoader 存储到缓存中。

### 2.3.9. GenericLoaderFactory#getFactory

```java
    private <T, Y> ModelLoaderFactory<T, Y> getFactory(Class<T> modelClass, Class<Y> resourceClass) {
        Map<Class/*Y*/, ModelLoaderFactory/*T, Y*/> resourceToFactories = modelClassToResourceFactories.get(modelClass);
        ModelLoaderFactory/*T, Y*/ result = null;
        if (resourceToFactories != null) {
            result = resourceToFactories.get(resourceClass);
        }

        if (result == null) {
            for (Class<? super T> registeredModelClass : modelClassToResourceFactories.keySet()) {
                // This accounts for model subclasses, our map only works for exact matches. We should however still
                // match a subclass of a model with a factory for a super class of that model if if there isn't a
                // factory for that particular subclass. Uris are a great example of when this happens, most uris
                // are actually subclasses for Uri, but we'd generally rather load them all with the same factory rather
                // than trying to register for each subclass individually.
                if (registeredModelClass.isAssignableFrom(modelClass)) {
                    Map<Class/*Y*/, ModelLoaderFactory/*T, Y*/> currentResourceToFactories =
                            modelClassToResourceFactories.get(registeredModelClass);
                    if (currentResourceToFactories != null) {
                        result = currentResourceToFactories.get(resourceClass);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }
```

　　GenericLoaderFactory 的 getFactory() 方法就是从 modelClassToResourceFactories 取出 modelClass 和 resourceClass 对应的 ModelLoaderFactory 对象，而 modelClassToResourceFactories 就是在 GenericLoaderFactory 的 register() 方法中注册的 Glide 构造函数中的东西。

　　查看一下 Glide 构造函数中 Uri.class 和 InputStream.class 对应的 ModelLoaderFacroty 对象：

```java
register(Uri.class, InputStream.class, new StreamUriLoader.Factory());
```

　　Uri.class 和 InputStream.class 对应的 ModelLoaderFacroty 对象是 StreamUrlLoader.Factory()。

### 2.3.10. StreamUriLoader

```java
public class StreamUriLoader extends UriLoader<InputStream> implements StreamModelLoader<Uri> {

    /**
     * THe default factory for {@link com.bumptech.glide.load.model.stream.StreamUriLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

        @Override
        public ModelLoader<Uri, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new StreamUriLoader(context, factories.buildModelLoader(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public StreamUriLoader(Context context) {
        this(context, Glide.buildStreamModelLoader(GlideUrl.class, context));
    }

    public StreamUriLoader(Context context, ModelLoader<GlideUrl, InputStream> urlLoader) {
        super(context, urlLoader);
    }

    @Override
    protected DataFetcher<InputStream> getLocalUriFetcher(Context context, Uri uri) {
        return new StreamLocalUriFetcher(context, uri);
    }

    @Override
    protected DataFetcher<InputStream> getAssetPathFetcher(Context context, String assetPath) {
        return new StreamAssetPathFetcher(context.getApplicationContext().getAssets(), assetPath);
    }
}

```

　　StreamUrlLoader 的 Factory 类的 build() 方法返回的是一个 StramUriLoader 对象，在调用 StreamUriLoader 构造方法中传入了 factories.buildModelLoader(GlideUrl.class, InputStream.class) 创建的 ModelLoader 对象。这个 ModelLoader 对象直接看 Glide 的构造函数中 GlideUrl.class, InputStream.class 中对应的是什么：

```java
register(GlideUrl.class, InputStream.class, new HttpUrlGlideUrlLoader.Factory());
```

　　继续查看 HttpUrlGlideUrlLoader.Factory()。

### 2.3.11. HttpUrlGlideUrlLoader

```java
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

　　HttpUrlGlideUrlLoader 的 Factory 类的 build() 方法返回的是 HttpUrlGlideUrlLoader() 对象。

　　回到 StreamUrlLoader 类中，StreamUrlLoader 的 Factory 类的 build() 方法调用了 StreamUriLoader() 的构造方法，而这个构造方法直接调用了 StreamUriLoader 符类  UriLoader 的构造方法。

### 2.3.12. UriLoader

```java
public abstract class UriLoader<T> implements ModelLoader<Uri, T> {
    private final Context context;
    private final ModelLoader<GlideUrl, T> urlLoader;

    public UriLoader(Context context, ModelLoader<GlideUrl, T> urlLoader) {
        this.context = context;
        // 设置变量 urlLoader 
        this.urlLoader = urlLoader;
    }

    @Override
    public final DataFetcher<T> getResourceFetcher(Uri model, int width, int height) {
        final String scheme = model.getScheme();

        DataFetcher<T> result = null;
        if (isLocalUri(scheme)) {
            if (AssetUriParser.isAssetUri(model)) {
                String path = AssetUriParser.toAssetPath(model);
                result = getAssetPathFetcher(context, path);
            } else {
                result = getLocalUriFetcher(context, model);
            }
        } else if (urlLoader != null && ("http".equals(scheme) || "https".equals(scheme))) {
            result = urlLoader.getResourceFetcher(new GlideUrl(model.toString()), width, height);
        }

        return result;
    }

    protected abstract DataFetcher<T> getLocalUriFetcher(Context context, Uri uri);

    protected abstract DataFetcher<T> getAssetPathFetcher(Context context, String path);

    private static boolean isLocalUri(String scheme) {
        return ContentResolver.SCHEME_FILE.equals(scheme)
                || ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme);
    }
}
```

　　只是将传入的 HttpUrlGlideUrlLoader 赋值给了成员变量 urlLoader。

　　再回到 SreamStringLoader 中，StreamStringLoader 的 Factory 类的 build 调用了 StreamStringLoader() 构造方法，参入的参数是 StreamUriLoader 对象，而 StreamStringLoader() 的构造方法调用了父类 StringLoader 的构造方法。

### 2.3.13. StringLoader

```java
public class StringLoader<T> implements ModelLoader<String, T> {
    private final ModelLoader<Uri, T> uriLoader;

    public StringLoader(ModelLoader<Uri, T> uriLoader) {
        this.uriLoader = uriLoader;
    }

    @Override
    public DataFetcher<T> getResourceFetcher(String model, int width, int height) {
        Uri uri;
        if (TextUtils.isEmpty(model)) {
            return null;
        } else if (model.startsWith("/")) {
            uri = toFileUri(model);
        } else {
            uri = Uri.parse(model);
            final String scheme = uri.getScheme();
            if (scheme == null) {
                uri = toFileUri(model);
            }
        }

        return uriLoader.getResourceFetcher(uri, width, height);
    }

    private static Uri toFileUri(String path) {
        return Uri.fromFile(new File(path));
    }
}
```

　　也是将传入的 StreamUriLoader 设置给成员变量 uriLoader。

## 2.4.  ModelLoader 

```java
/**
* 一种工厂接口，用于将任意复杂的数据模型转换为具体的数据类型，该数据类型可由 DataFaetcher 用于获取由该模型表示的资源的数据。
* 该接口有两个目标：
* 1. 将特定模型转换为可解码为资源的数据类型。
* 2. 允许模型与视图的维度组合以获取特定大小的资源。
* T：模型的类型。
* Y：可以被用于通过一个 com.bumptech.glide.load.ResourceDecoder 解码资源的数据类型
*/
public interface ModelLoader<T, Y> {

    /**
     * 获取 DataFetcher，DataFetcher 可以获取解码此模型表示的资源所需的数据。
     * 如果资源已缓存，DataFetcher 将不使用。
     * T model：表示资源的模型。
     */
    DataFetcher<Y> getResourceFetcher(T model, int width, int height);
}
```

　　ModelLoader 是一个工厂接口，只有一个 getResourceFetcher() 方法。而 getResourceFacher() 方法用于获取 DataFetcher 对象，DaraFetcher 对象可以获取解码资源的数据。

## 2.5. DataFetcher

```java
/**
 * 用于延迟检索加载资源的数据的接口。
 * ModelLoader 为每次资源加载创建一个新实例。
 * load() 方法可以调用也可以不调用任务给定的加载，这取决于是否缓存了相应的资源。
 * cancel() 也可以调用，也可以不调用。
 * 如果调用 loadData() 方法，那么也要调用 cleanup() 方法。
 */
public interface DataFetcher<T> {

    /**
     * 异步从解码资源中获取数据。这将始终在后台线程上调用，因此在这里执行长时间运行的任务是安全的。
     * 调用的任何第三方库都必须是线程安全的，因为此方法将从多个后台线程的 ExecutorService 中的一个线程调用。
     * 只有当相应的资源不在缓存中时，才会调用此方法。
     * 注意：此方法将在后台线程上运行，因此阻塞 I/O 是安全的。
     * priority：请求完成的优先级。
     * cleanup() 重新调整将被清除的位置。
     */
    T loadData(Priority priority) throws Exception;

    /**
     * 清除或回收此数据获取程序使用的任何资源。在 loadData() 返回的数据被 ResourceDecoder 解码后，将在 finnally 块中调用此方法。
     * 注意：此方法将在后台线程中运行，因此阻塞 I/O 是安全的。
     */
    void cleanup();

    /**
     * 返回一个字符串，该字符串唯一标识此获取程序将获取的数据，包括特定大小。
     * 将要获取的数据字节的散列是理想的 id，但由于在许多情况下这是不实际的，因为 url、文件路径和 uri 通常就足够了。
     * 注意：此方法将在主线程上运行，因此它不应执行阻塞操作，并且应快速完成。
     */
    String getId();

    /**
     * 当加载不再进行并且已经关闭将调用的方法。这种方法不需要确保任何正在进行的加载任何进程中没有完成的加载，它也可以在加载开始之前或加载完成之后调用。
     * 使用此方法的最佳方法是取消尚未启动的任何加载，但允许正在进行的加载完成，因为通常希望在不久的将来在不同的视图中显示相同的资源。
     * 注意：此方法将在主线程上运行，因此它不应执行阻塞操作，并且应快速完成。
     */
    void cancel();
}
```

## 2.6. Glide.buildFileDescriptorModelLoader

```java
    public static <T> ModelLoader<T, ParcelFileDescriptor> buildFileDescriptorModelLoader(Class<T> modelClass,
            Context context) {
        return buildModelLoader(modelClass, ParcelFileDescriptor.class, context);
    }

```

　　Glide 的 buildFileDescriptorModelLoader() 方法直接调用了 buildModelLoader() 方法，返回值的类型是 ModelLoader 对象。

　　而 buildModelLoader() 方法在之前分析 Glide 的 buildStreamModelLoader() 方法是就分析过了，所以直接看 Glide 构造方法中 String.class 和 ParcelFileDescriptor.class 对应的 ModelLoader 对应的是什么吧。

```java
register(String.class, ParcelFileDescriptor.class, new FileDescriptorStringLoader.Factory());
```

　　接着看 FileDescriptorStringLoader.Factory。

### 2.6.1. FileDescriptorStringLoader

```java
public class FileDescriptorStringLoader extends StringLoader<ParcelFileDescriptor>
        implements FileDescriptorModelLoader<String> {

    /**
     * The default factory for {@link com.bumptech.glide.load.model.file_descriptor.FileDescriptorStringLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<String, ParcelFileDescriptor> {
        @Override
        public ModelLoader<String, ParcelFileDescriptor> build(Context context, GenericLoaderFactory factories) {
            return new FileDescriptorStringLoader(factories.buildModelLoader(Uri.class, ParcelFileDescriptor.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public FileDescriptorStringLoader(Context context) {
        this(Glide.buildFileDescriptorModelLoader(Uri.class, context));
    }

    public FileDescriptorStringLoader(ModelLoader<Uri, ParcelFileDescriptor> uriLoader) {
        super(uriLoader);
    }
}
```

　　FileDescriptorStringLoader 的 Factory 的 build 方法返回的是一个 FileDescriptorStringLoader 对象，传入的是 factories.buildModelLoader(Uri.class, ParcelFileDescriptor.class) 参数。

　　直接在 Glide 的构造函数中查找 Uri.class 和  ParcelFileDescriptor.class 对应的 ModelLoader：

```java
   register(Uri.class, ParcelFileDescriptor.class, new FileDescriptorUriLoader.Factory());
```

　　接着看 FileDescriptorUriLoader.Factory。

### 2.6.2. FileDescriptorUriLoader

```java
public class FileDescriptorUriLoader extends UriLoader<ParcelFileDescriptor> implements FileDescriptorModelLoader<Uri> {

    /**
     * The default factory for {@link com.bumptech.glide.load.model.file_descriptor.FileDescriptorUriLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<Uri, ParcelFileDescriptor> {
        @Override
        public ModelLoader<Uri, ParcelFileDescriptor> build(Context context, GenericLoaderFactory factories) {
            return new FileDescriptorUriLoader(context, factories.buildModelLoader(GlideUrl.class,
                    ParcelFileDescriptor.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public FileDescriptorUriLoader(Context context) {
        this(context, Glide.buildFileDescriptorModelLoader(GlideUrl.class, context));
    }

    public FileDescriptorUriLoader(Context context, ModelLoader<GlideUrl, ParcelFileDescriptor> urlLoader) {
        super(context, urlLoader);
    }

    @Override
    protected DataFetcher<ParcelFileDescriptor> getLocalUriFetcher(Context context, Uri uri) {
        return new FileDescriptorLocalUriFetcher(context, uri);
    }

    @Override
    protected DataFetcher<ParcelFileDescriptor> getAssetPathFetcher(Context context, String assetPath) {
        return new FileDescriptorAssetPathFetcher(context.getApplicationContext().getAssets(), assetPath);
    }
}

```

　　FileDescriptorUriLoader 的 Factory 的 build 方法返回的一个 FileDescritporUriLoader 对象，其中传入了 factories.buildModelLoader（GlideUrl.class,
        ParcelFileDescriptor.class）对象，而 Glide 的构造方法中是没有注册这个的，所以将会在 Glide 的 buildModelLoader() 方法中返回 null。

​        所以在 FileDescriptorUriLoader 的 Factory 的 build() 方法中创建 FileDescritorUriLoader 的时候传入的 urlLoader 为 null，而 FileDescritorUriLoader 的构造方法直接调用了父类 UriLoader 的构造方法。而 UriLoader 的构造方法是将参数 urlLoader 设置给成员变量 urlLoader。

​        接着回到 FileDescriptorStringLoader 的 构造函数中，这里传入的 ModelLoader 就是 FileDescriptorUriLoader 对象，而 FileDescriptorStringLoader 直接调用了父类 UriLoader 的构造函数，而 UriLoader 的构造方法是将参数 urlLoader 设置给成员变量 urlLoader。

**总结一下**

​        到这里，RequestManager 的 loadGeneric() 方法中创建的 streamModelLoader 是 StreamStringLoader 对象，StreamStringLoader 对象的成员变量 uriLoader 是 StreamUrlLoader，而 StreamUrlLoader 对象的成员变量 uriLoader 是 HttpUrlGlideLoader；fileDescriptorModelLoader 是 FileDescriptorStringLoader 对象，FileDescriptorStringLoader 对象的成员变量 uriLoader 是 FileDescriptorUriLoader 对象，而 FileDescriptorUriLoader 对象的成员变量 uriLoader 是 null。

## 2.7. DrawableTypeRequest类

```java
/**
 * A class for creating a load request that loads either an animated GIF drawable or a Bitmap drawable directly, or
 * adds an {@link com.bumptech.glide.load.resource.transcode.ResourceTranscoder} to transcode the data into a
 * resource type other than a {@link android.graphics.drawable.Drawable}.
 *
 * @param <ModelType> The type of model to use to load the {@link android.graphics.drawable.BitmapDrawable} or
 * {@link com.bumptech.glide.load.resource.gif.GifDrawable}.
 */
public class DrawableTypeRequest<ModelType> extends DrawableRequestBuilder<ModelType> implements DownloadOptions {
    private final ModelLoader<ModelType, InputStream> streamModelLoader;
    private final ModelLoader<ModelType, ParcelFileDescriptor> fileDescriptorModelLoader;
    private final RequestManager.OptionsApplier optionsApplier;

  	// streamModelLoader 为 StreamStringLoader 对象实例
  	// fileDescriptorModelLoader 为 FileDescriptorStringLoader 对象实例
    private static <A, Z, R> FixedLoadProvider<A, ImageVideoWrapper, Z, R> buildProvider(Glide glide,
            ModelLoader<A, InputStream> streamModelLoader,
            ModelLoader<A, ParcelFileDescriptor> fileDescriptorModelLoader, Class<Z> resourceClass,
            Class<R> transcodedClass,
            ResourceTranscoder<Z, R> transcoder) {
        if (streamModelLoader == null && fileDescriptorModelLoader == null) {
            return null;
        }

        if (transcoder == null) {
            transcoder = glide.buildTranscoder(resourceClass, transcodedClass);
        }
        DataLoadProvider<ImageVideoWrapper, Z> dataLoadProvider = glide.buildDataProvider(ImageVideoWrapper.class,
                resourceClass);
      	// 创建 ImageVideoModelLoader 对象
      	// 设置成员变量 streamLoader 为传递的 streamModelLoader
        // 设置成员变量 fileDescriptorLoader 为传递的 fileDescriptorModelLoader
        ImageVideoModelLoader<A> modelLoader = new ImageVideoModelLoader<A>(streamModelLoader,
                fileDescriptorModelLoader);
      	// 创建 FixedLoadProvider 对象
      	// 设置成员变量 modelLoader 为传递的 modelLoader，这就是 ImageVideoModelLoader 对象
      	// 设置成员变量 transcoder 为传递的 transcoder，该对象用于图片转码
      	// 设置成员变量 dataLoadProvider 为传递的 dataLoadProvider，也就是 DataLoadProvider 对象
        return new FixedLoadProvider<A, ImageVideoWrapper, Z, R>(modelLoader, transcoder, dataLoadProvider);
    }

  	// 构造方法
    DrawableTypeRequest(Class<ModelType> modelClass, ModelLoader<ModelType, InputStream> streamModelLoader,
            ModelLoader<ModelType, ParcelFileDescriptor> fileDescriptorModelLoader, Context context, Glide glide,
            RequestTracker requestTracker, Lifecycle lifecycle, RequestManager.OptionsApplier optionsApplier) {
        super(context, modelClass,
              	// 调用 buildProvider 方法，返回一个 FixedLoadProvider 对象，将 streamModelLoader 和 fileDescriptorModelLoader 设置为成员变量
              	// 设置给父类的父类 GenericRequestBuilder 的成员变量 loadProvider 
                buildProvider(glide, streamModelLoader, fileDescriptorModelLoader, GifBitmapWrapper.class,
                        GlideDrawable.class, null),
                glide, requestTracker, lifecycle);
        this.streamModelLoader = streamModelLoader;
        this.fileDescriptorModelLoader = fileDescriptorModelLoader;
        this.optionsApplier = optionsApplier;
    }

    /**
     * Attempts to always load the resource as a {@link android.graphics.Bitmap}, even if it could actually be animated.
     *
     * @return A new request builder for loading a {@link android.graphics.Bitmap}
     */
    public BitmapTypeRequest<ModelType> asBitmap() {
        return optionsApplier.apply(new BitmapTypeRequest<ModelType>(this, streamModelLoader,
                fileDescriptorModelLoader, optionsApplier));
    }

    /**
     * Attempts to always load the resource as a {@link com.bumptech.glide.load.resource.gif.GifDrawable}.
     * <p>
     *     If the underlying data is not a GIF, this will fail. As a result, this should only be used if the model
     *     represents an animated GIF and the caller wants to interact with the GIfDrawable directly. Normally using
     *     just an {@link com.bumptech.glide.DrawableTypeRequest} is sufficient because it will determine whether or
     *     not the given data represents an animated GIF and return the appropriate animated or not animated
     *     {@link android.graphics.drawable.Drawable} automatically.
     * </p>
     *
     * @return A new request builder for loading a {@link com.bumptech.glide.load.resource.gif.GifDrawable}.
     */
    public GifTypeRequest<ModelType> asGif() {
        return optionsApplier.apply(new GifTypeRequest<ModelType>(this, streamModelLoader, optionsApplier));
    }

    /**
     * {@inheritDoc}
     */
    public <Y extends Target<File>> Y downloadOnly(Y target) {
        return getDownloadOnlyRequest().downloadOnly(target);
    }

    /**
     * {@inheritDoc}
     */
    public FutureTarget<File> downloadOnly(int width, int height) {
        return getDownloadOnlyRequest().downloadOnly(width, height);
    }

    private GenericTranscodeRequest<ModelType, InputStream, File> getDownloadOnlyRequest() {
        return optionsApplier.apply(new GenericTranscodeRequest<ModelType, InputStream, File>(File.class, this,
                streamModelLoader, InputStream.class, File.class, optionsApplier));
    }
}

```

　　DrawTypeRequest 类提供了 asBitmap() 和 asGif() 这两个方法，这两个方法分别是用于强制指定加载静态图片和动态图片的。从源码中可以看出，它们分别又创建了一个 BitmapTypeRequest 和 GifTypeRequest，如果没有进行强制指定的话，那默认就是使用 DrawableTypeRequest。

　　在 load() 方法中，fromString() 方法会返回一个 DrawableTypeRequest 对象，接下来会调用这个对象的 load() 方法，把图片的 URL 地址传进去，DrawableTypeRequest 中并没有 load() 方法，那么很容易就能猜想到，load() 方法是在父类当中的。

　　DrawableTypeRequest 的父类是 DrawableRequestBuilder。

## 2.8. DrawableRequestBuilder

```java

public class DrawableRequestBuilder<ModelType>
        extends GenericRequestBuilder<ModelType, ImageVideoWrapper, GifBitmapWrapper, GlideDrawable>
        implements BitmapOptions, DrawableOptions {

    DrawableRequestBuilder(Context context, Class<ModelType> modelClass,
            LoadProvider<ModelType, ImageVideoWrapper, GifBitmapWrapper, GlideDrawable> loadProvider, Glide glide,
            RequestTracker requestTracker, Lifecycle lifecycle) {
        super(context, modelClass, loadProvider, GlideDrawable.class, glide, requestTracker, lifecycle);
        // Default to animating.
        crossFade();
    }

    public DrawableRequestBuilder<ModelType> thumbnail(
            DrawableRequestBuilder<?> thumbnailRequest) {
        super.thumbnail(thumbnailRequest);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> thumbnail(
            GenericRequestBuilder<?, ?, ?, GlideDrawable> thumbnailRequest) {
        super.thumbnail(thumbnailRequest);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> thumbnail(float sizeMultiplier) {
        super.thumbnail(sizeMultiplier);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> sizeMultiplier(float sizeMultiplier) {
        super.sizeMultiplier(sizeMultiplier);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> decoder(ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> decoder) {
        super.decoder(decoder);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> cacheDecoder(ResourceDecoder<File, GifBitmapWrapper> cacheDecoder) {
        super.cacheDecoder(cacheDecoder);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> encoder(ResourceEncoder<GifBitmapWrapper> encoder) {
        super.encoder(encoder);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> priority(Priority priority) {
        super.priority(priority);
        return this;
    }

    public DrawableRequestBuilder<ModelType> transform(BitmapTransformation... transformations) {
        return bitmapTransform(transformations);
    }

    @SuppressWarnings("unchecked")
    public DrawableRequestBuilder<ModelType> centerCrop() {
        return transform(glide.getDrawableCenterCrop());
    }

    @SuppressWarnings("unchecked")
    public DrawableRequestBuilder<ModelType> fitCenter() {
        return transform(glide.getDrawableFitCenter());
    }

    public DrawableRequestBuilder<ModelType> bitmapTransform(Transformation<Bitmap>... bitmapTransformations) {
        GifBitmapWrapperTransformation[] transformations =
                new GifBitmapWrapperTransformation[bitmapTransformations.length];
        for (int i = 0; i < bitmapTransformations.length; i++) {
            transformations[i] = new GifBitmapWrapperTransformation(glide.getBitmapPool(), bitmapTransformations[i]);
        }
        return transform(transformations);
    }

    @Override
    public DrawableRequestBuilder<ModelType> transform(Transformation<GifBitmapWrapper>... transformation) {
        super.transform(transformation);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> transcoder(
            ResourceTranscoder<GifBitmapWrapper, GlideDrawable> transcoder) {
        super.transcoder(transcoder);
        return this;
    }

    public final DrawableRequestBuilder<ModelType> crossFade() {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>());
        return this;
    }

    public DrawableRequestBuilder<ModelType> crossFade(int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(duration));
        return this;
    }

    @Deprecated
    public DrawableRequestBuilder<ModelType> crossFade(Animation animation, int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(animation, duration));
        return this;
    }

    public DrawableRequestBuilder<ModelType> crossFade(int animationId, int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(context, animationId,
                duration));
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> dontAnimate() {
        super.dontAnimate();
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> animate(ViewPropertyAnimation.Animator animator) {
        super.animate(animator);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> animate(int animationId) {
        super.animate(animationId);
        return this;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public DrawableRequestBuilder<ModelType> animate(Animation animation) {
        super.animate(animation);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> placeholder(int resourceId) {
        super.placeholder(resourceId);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> placeholder(Drawable drawable) {
        super.placeholder(drawable);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> fallback(Drawable drawable) {
        super.fallback(drawable);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> fallback(int resourceId) {
        super.fallback(resourceId);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> error(int resourceId) {
        super.error(resourceId);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> error(Drawable drawable) {
        super.error(drawable);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> listener(
            RequestListener<? super ModelType, GlideDrawable> requestListener) {
        super.listener(requestListener);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> diskCacheStrategy(DiskCacheStrategy strategy) {
        super.diskCacheStrategy(strategy);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> skipMemoryCache(boolean skip) {
        super.skipMemoryCache(skip);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> override(int width, int height) {
        super.override(width, height);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> sourceEncoder(Encoder<ImageVideoWrapper> sourceEncoder) {
        super.sourceEncoder(sourceEncoder);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> dontTransform() {
        super.dontTransform();
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> signature(Key signature) {
        super.signature(signature);
        return this;
    }
		
  	// load 方法，返回本对象
    @Override
    public DrawableRequestBuilder<ModelType> load(ModelType model) {
      	// 调用父类 load 方法，父类的方法就是设置成员变量 model 为参数 model
        super.load(model);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> clone() {
        return (DrawableRequestBuilder<ModelType>) super.clone();
    }

    @Override
    public Target<GlideDrawable> into(ImageView view) {
        return super.into(view);
    }

    @Override
    void applyFitCenter() {
        fitCenter();
    }

    @Override
    void applyCenterCrop() {
        centerCrop();
    }
}
```

　　DrawableRequestBuilder 中有很多个方法，这些方法其实就是 Glide 绝大多数的 API 了。里面很多方法都是经常使用的，比如说 placeholder() 方法、error() 方法、diskCacheSrategy() 方法、override() 方法等。

　　load() 方法会先调用其 父类 GenericRequestBuilder 的 load() 方法，然后将自己返回，到这里就分析完了，也就是说，最终 load() 方法返回的其实就是一个 DrawableRequestBuilder 对象，并且 DrawableRequestBuilder 类中有一个 into() 方法。

# 3. 参考文章

1. [Android图片加载框架最全解析（二），从源码的角度理解Glide的执行流程](https://blog.csdn.net/guolin_blog/article/details/53939176)

