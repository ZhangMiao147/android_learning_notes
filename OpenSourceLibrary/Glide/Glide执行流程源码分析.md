# Glide 执行流程源码分析

　　Glide 的简单使用是：

```java
Glide.with(this).load(url).into(imageView);
```

　　所以通过阅读源码明白上面代码是如何实现将一张网络图片展示到 ImageView 上面的。

## 1. with()

　　with() 方法是 Glide 类中的一组静态方法，它有好几个方法重载。

```java
public class Glide {
    ...
	/**
     * Begin a load with Glide by passing in a context.
     *
     * <p>
     *     Any requests started using a context will only have the application level options applied and will not be
     *     started or stopped based on lifecycle events. In general, loads should be started at the level the result
     *     will be used in. If the resource will be used in a view in a child fragment,
     *     the load should be started with {@link #with(android.app.Fragment)}} using that child fragment. Similarly,
     *     if the resource will be used in a view in the parent fragment, the load should be started with
     *     {@link #with(android.app.Fragment)} using the parent fragment. In the same vein, if the resource will be used
     *     in a view in an activity, the load should be started with {@link #with(android.app.Activity)}}.
     * </p>
     *
     * <p>
     *     This method is appropriate for resources that will be used outside of the normal fragment or activity
     *     lifecycle (For example in services, or for notification thumbnails).
     * </p>
     *
     * @see #with(android.app.Activity)
     * @see #with(android.app.Fragment)
     * @see #with(android.support.v4.app.Fragment)
     * @see #with(android.support.v4.app.FragmentActivity)
     *
     * @param context Any context, will not be retained.
     * @return A RequestManager for the top level application that can be used to start a load.
     */
    public static RequestManager with(Context context) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(context);
    }

    /**
     * Begin a load with Glide that will be tied to the given {@link android.app.Activity}'s lifecycle and that uses the
     * given {@link Activity}'s default options.
     *
     * @param activity The activity to use.
     * @return A RequestManager for the given activity that can be used to start a load.
     */
    public static RequestManager with(Activity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }

    /**
     * Begin a load with Glide that will tied to the give {@link android.support.v4.app.FragmentActivity}'s lifecycle
     * and that uses the given {@link android.support.v4.app.FragmentActivity}'s default options.
     *
     * @param activity The activity to use.
     * @return A RequestManager for the given FragmentActivity that can be used to start a load.
     */
    public static RequestManager with(FragmentActivity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }

    /**
     * Begin a load with Glide that will be tied to the given {@link android.app.Fragment}'s lifecycle and that uses
     * the given {@link android.app.Fragment}'s default options.
     *
     * @param fragment The fragment to use.
     * @return A RequestManager for the given Fragment that can be used to start a load.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static RequestManager with(android.app.Fragment fragment) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(fragment);
    }

    /**
     * Begin a load with Glide that will be tied to the given {@link android.support.v4.app.Fragment}'s lifecycle and
     * that uses the given {@link android.support.v4.app.Fragment}'s default options.
     *
     * @param fragment The fragment to use.
     * @return A RequestManager for the given Fragment that can be used to start a load.
     */
    public static RequestManager with(Fragment fragment) {
      	// 调用 RequestManagerRetriever 的 get() 方法获取 RequestManagerRetriever 对象实例 retriever
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
      	// 调用 retriever 的 get() 方法获取 RequestManager 对象
        return retriever.get(fragment);
    }
    ...
}
```

　　可以看到，with() 方法的重载方法非常多，既可以传入 Activity，也可以传入 Fragment 或者是 Context。每一个 with() 方法重载的代码都非常简单，都是先调用 RequestManagerRetriever 的静态 get() 方法得到一个 RequestManagerRetriever 对象，这个静态 get() 方法就是一个单例实现。然后再调用 RequestManagerRetriever 的实例 get() 方法，去获取 RequestManager 对象。

### 1.1. RequestManagerRetriever#get

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

　　RequestManagerRetriever 类中有很多个 get() 方法的重载，Context 参数、Activity 参数、Fragment 参数等等，实际上只有两种情况而已，即传入 Application 类型的参数和传入非 Application 类型的参数。

　　传入 Application 参数的 get() 方法：如果在 Glide.with() 方法中传入的是一个 Application 对象，那么这里就会调用带有 Context 参数的 get() 方法重载，然后会调用 getApplicationManager() 方法来获取一个 RequestManager 对象。其实这是最简单的一种情况，因为 Application 对象的生命周期即应用程序的生命周期，因此 Glide 并不需要做什么特殊的处理，它自动就是和应用程序的生命周期是同步的，如果应用程序关闭的话，Glide 的加载也会同时终止。

　　传入非 Application 参数的 get() 方法：在使用 Glide.with() 方法中不管传入的是 Activity、FragmeActivity、v4 包下的 Fragment 还是 app 包下的 Fragment，最终的流程都是一样的，那就是调用 getSupportRequestManagerFragment 方法向当前的 Activity 当中添加一个隐藏的 Fragment。为什么要添加一个隐藏的 Fragment ?因为 Glide 需要知道加载的生命周期。Glide 并没有办法知道 Activity 的生命周期，于是 Glide 就是用了添加隐藏 Fragment 这种小技巧，因为 Fragment 的生命周期和 Activity 是同步的，如果 Activity 被销毁了，Fragment 是可以监听到的，这样 Glide 就可以捕获这个事件并停止图片加载了。

　　with() 方法就是为了得到一个 RequestManager 对象而已，然后 Glide 会根据传入的 with() 方法的参数来确定图片加载的生命周期。

## 2. load()

　　with() 方法返回的是一个 RequestManager 对象，那么 lode() 方法就是在 RequestManager 类当中的。

```java
public class RequestManager implements LifecycleListener {
    ...
    // lode 的重载方法
        
    ...
        
    /**
     * Returns a request builder to load the given {@link java.lang.String}.
     * signature.
     *
     * @see #fromString()
     * @see #load(Object)
     *
     * @param string A file path, or a uri or url handled by {@link com.bumptech.glide.load.model.UriLoader}.
     */
    public DrawableTypeRequest<String> load(String string) {
        return (DrawableTypeRequest<String>) fromString().load(string);
    }
    
    ...

    /**
     * Returns a request builder that loads data from {@link String}s using an empty signature.
     *
     * <p>
     *     Note - this method caches data using only the given String as the cache key. If the data is a Uri outside of
     *     your control, or you otherwise expect the data represented by the given String to change without the String
     *     identifier changing, Consider using
     *     {@link com.bumptech.glide.GenericRequestBuilder#signature(com.bumptech.glide.load.Key)} to mixin a signature
     *     you create that identifies the data currently at the given String that will invalidate the cache if that data
     *     changes. Alternatively, using {@link com.bumptech.glide.load.engine.DiskCacheStrategy#NONE} and/or
     *     {@link com.bumptech.glide.DrawableRequestBuilder#skipMemoryCache(boolean)} may be appropriate.
     * </p>
     *
     * @see #from(Class)
     * @see #load(String)
     */
    public DrawableTypeRequest<String> fromString() {
        return loadGeneric(String.class);
    }
    
    ...
    
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
    ...
}
    
```

　　RequestManager 中有很多 load() 方法的重载，这里只列出了加载图片 URL 字符串的 load() 方法来研究。

　　load() 方法的逻辑非常简单，就是先调用了 fromString() 方法，再调用 load() 方法，然后把传入的图片 URL 地址传进去。而 fromString() 方法也极为简单，就是调用了 loadGeneric() 方法，并且指定参数为 String.class，因为 load() 方法传入的是一个字符串参数。所以主要的工作是在 loadGeneric() 方法中进行的。

　　loadGeneric() 方法分别调用了 Glide.buildStreamModelLoader() 方法和 Glide.buildFileDescriptorModelLoader() 方法来获得 ModelLoader 对象。ModelLoader 对象是用于加载图片的，而给 load() 方法传入不同类型的参数，这里也会得到不同的 ModelLoader 对象。由于传入的参数是 String.class，因此最终得到的是 StreamStringLoader 对象，它是实现了 ModelLoader 接口的。 

　　最后，loadGeneric() 方法是要返回一个 DrawableTypeRequest 对象的，因此在 loadGeneric() 方法的最后又去 new 了一个 DrawableTypeRequest 对象，然后把刚才获得的 ModelLoader 对象，还有一些数据都传了进去。

### 2.1. DrawableTypeRequest类

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
      	// 设置成员变量 transcoder 为传递的 transcoder
      	// 设置成员变量 dataLoadProvider 为传递的 dataLoadProvider，也就是 DataLoadProvider 对象
        return new FixedLoadProvider<A, ImageVideoWrapper, Z, R>(modelLoader, transcoder, dataLoadProvider);
    }

  	// 构造方法
    DrawableTypeRequest(Class<ModelType> modelClass, ModelLoader<ModelType, InputStream> streamModelLoader,
            ModelLoader<ModelType, ParcelFileDescriptor> fileDescriptorModelLoader, Context context, Glide glide,
            RequestTracker requestTracker, Lifecycle lifecycle, RequestManager.OptionsApplier optionsApplier) {
        super(context, modelClass,
              	// 调用 buildProvider 方法，返回一个 FixedLoadProvider 对象
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

### 2.2. DrawableRequestBuilder

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

    DrawableRequestBuilder(Context context, Class<ModelType> modelClass,
            LoadProvider<ModelType, ImageVideoWrapper, GifBitmapWrapper, GlideDrawable> loadProvider, Glide glide,
            RequestTracker requestTracker, Lifecycle lifecycle) {
        super(context, modelClass, loadProvider, GlideDrawable.class, glide, requestTracker, lifecycle);
        // Default to animating.
        crossFade();
    }

    /**
     * Loads and displays the {@link GlideDrawable} retrieved by the given thumbnail request if it finishes before this
     * request. Best used for loading thumbnail {@link GlideDrawable}s that are smaller and will be loaded more quickly
     * than the fullsize {@link GlideDrawable}. There are no guarantees about the order in which the requests will
     * actually finish. However, if the thumb request completes after the full request, the thumb {@link GlideDrawable}
     * will never replace the full image.
     *
     * @see #thumbnail(float)
     *
     * <p>
     *     Note - Any options on the main request will not be passed on to the thumbnail request. For example, if
     *     you want an animation to occur when either the full {@link GlideDrawable} loads or the thumbnail loads,
     *     you need to call {@link #animate(int)} on both the thumb and the full request. For a simpler thumbnail
     *     option where these options are applied to the humbnail as well, see {@link #thumbnail(float)}.
     * </p>
     *
     * <p>
     *     Only the thumbnail call on the main request will be obeyed, recursive calls to this method are ignored.
     * </p>
     *
     * @param thumbnailRequest The request to use to load the thumbnail.
     * @return This builder object.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> thumbnail(float sizeMultiplier) {
        super.thumbnail(sizeMultiplier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> sizeMultiplier(float sizeMultiplier) {
        super.sizeMultiplier(sizeMultiplier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> decoder(ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> decoder) {
        super.decoder(decoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> cacheDecoder(ResourceDecoder<File, GifBitmapWrapper> cacheDecoder) {
        super.cacheDecoder(cacheDecoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> encoder(ResourceEncoder<GifBitmapWrapper> encoder) {
        super.encoder(encoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> priority(Priority priority) {
        super.priority(priority);
        return this;
    }

    /**
     * Transform {@link GlideDrawable}s using the given
     * {@link com.bumptech.glide.load.resource.bitmap.BitmapTransformation}s.
     *
     * <p>
     *     Note - Bitmap transformations will apply individually to each frame of animated GIF images and also to
     *     individual {@link Bitmap}s.
     * </p>
     *
     * @see #centerCrop()
     * @see #fitCenter()
     * @see #bitmapTransform(com.bumptech.glide.load.Transformation[])
     * @see #transform(com.bumptech.glide.load.Transformation[])
     *
     * @param transformations The transformations to apply in order.
     * @return This request builder.
     */
    public DrawableRequestBuilder<ModelType> transform(BitmapTransformation... transformations) {
        return bitmapTransform(transformations);
    }

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

    /**
     * Transform {@link GlideDrawable}s using the given {@link android.graphics.Bitmap} transformations. Replaces any
     * previous transformations.
     *
     * @see #fitCenter()
     * @see #centerCrop()
     * @see #transform(com.bumptech.glide.load.resource.bitmap.BitmapTransformation...)
     * @see #transform(com.bumptech.glide.load.Transformation[])
     *
     * @return This request builder.
     */
    public DrawableRequestBuilder<ModelType> bitmapTransform(Transformation<Bitmap>... bitmapTransformations) {
        GifBitmapWrapperTransformation[] transformations =
                new GifBitmapWrapperTransformation[bitmapTransformations.length];
        for (int i = 0; i < bitmapTransformations.length; i++) {
            transformations[i] = new GifBitmapWrapperTransformation(glide.getBitmapPool(), bitmapTransformations[i]);
        }
        return transform(transformations);
    }



    /**
     * {@inheritDoc}
     *
     * @see #bitmapTransform(com.bumptech.glide.load.Transformation[])
     * @see #centerCrop()
     * @see #fitCenter()
     */
    @Override
    public DrawableRequestBuilder<ModelType> transform(Transformation<GifBitmapWrapper>... transformation) {
        super.transform(transformation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> transcoder(
            ResourceTranscoder<GifBitmapWrapper, GlideDrawable> transcoder) {
        super.transcoder(transcoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public final DrawableRequestBuilder<ModelType> crossFade() {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public DrawableRequestBuilder<ModelType> crossFade(int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public DrawableRequestBuilder<ModelType> crossFade(Animation animation, int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(animation, duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public DrawableRequestBuilder<ModelType> crossFade(int animationId, int duration) {
        super.animate(new DrawableCrossFadeFactory<GlideDrawable>(context, animationId,
                duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> dontAnimate() {
        super.dontAnimate();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> animate(ViewPropertyAnimation.Animator animator) {
        super.animate(animator);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> animate(int animationId) {
        super.animate(animationId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public DrawableRequestBuilder<ModelType> animate(Animation animation) {
        super.animate(animation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> placeholder(int resourceId) {
        super.placeholder(resourceId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> error(int resourceId) {
        super.error(resourceId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> error(Drawable drawable) {
        super.error(drawable);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> listener(
            RequestListener<? super ModelType, GlideDrawable> requestListener) {
        super.listener(requestListener);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> diskCacheStrategy(DiskCacheStrategy strategy) {
        super.diskCacheStrategy(strategy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> skipMemoryCache(boolean skip) {
        super.skipMemoryCache(skip);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> override(int width, int height) {
        super.override(width, height);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrawableRequestBuilder<ModelType> sourceEncoder(Encoder<ImageVideoWrapper> sourceEncoder) {
        super.sourceEncoder(sourceEncoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
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
      	// 调用父类 load 方法，设置成员变量 model 为参数 model
        super.load(model);
        return this;
    }

    @Override
    public DrawableRequestBuilder<ModelType> clone() {
        return (DrawableRequestBuilder<ModelType>) super.clone();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     *     Note - If no transformation is set for this load, a default transformation will be applied based on the
     *     value returned from {@link android.widget.ImageView#getScaleType()}. To avoid this default transformation,
     *     use {@link #dontTransform()}.
     * </p>
     *
     * @param view {@inheritDoc}
     * @return {@inheritDoc}
     */
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

　　load() 方法会先调用其 父类 GenericRequestBuilder 的 load() 方法，然后将自己返回到这里就分析完了，也就是说，最终 load() 方法返回的其实就是一个 DrawableRequestBuilder 对象，并且 DrawableRequestBuilder 类中有一个 into() 方法。

## 3. into()

　　into() 方法实现在 DrawableRequestBuilder 的父类 GenericRequestBuilder 类中。

　　into 方法的实现代码很长，只跟着请求网络图片的代码看 Glide 执行流程，并且在这里分为三部分来解析：

1. 网络请求；
2. 解析网络结果；
3. 将图片显示到界面上。

### 3.1. 第一步：网络请求

#### 3.1.1. GenericRequestBuilder#into

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

　　在最后一行先调用了 glide.buildImageViewTarge() 方法，这个方法构建出一个 Targe 对象，Targe 对象则是用来最终展示图片用的。

##### 3.1.1.1. Glide#buildImageViewTarget

```java
    <R> Target<R> buildImageViewTarget(ImageView imageView, Class<R> transcodedClass) {
        return imageViewTargetFactory.buildTarget(imageView, transcodedClass);
    }
```

　　调用了 ImageViewTargetFactory 的 buildTargetFactory 的 buildTarget() 方法。

##### 3.1.1.2. ImageViewTargetFactory#buildTarget

```java
/**
 * A factory responsible for producing the correct type of {@link com.bumptech.glide.request.target.Target} for a given
 * {@link android.view.View} subclass.
 */
public class ImageViewTargetFactory {

    @SuppressWarnings("unchecked")
    public <Z> Target<Z> buildTarget(ImageView view, Class<Z> clazz) {
        if (GlideDrawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new GlideDrawableImageViewTarget(view);
        } else if (Bitmap.class.equals(clazz)) {
            return (Target<Z>) new BitmapImageViewTarget(view);
        } else if (Drawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new DrawableImageViewTarget(view);
        } else {
            throw new IllegalArgumentException("Unhandled class: " + clazz
                    + ", try .as*(Class).transcode(ResourceTranscoder)");
        }
    }
}
```

　　可以看到，在 buildTarget() 方法中会根据传入的 class 参数来构建不同的 Target 对象。这个 class 参数其实基本上只有两种轻量，如果在使用 Glide 加载图片的时候调用了 asBitmap() 方法，那么这里就会构建出 BitmapImageViewTarget 对象，否则的话构建的都是 GlideDrawableImageViewTarget 对象。至于 DrawableImageViewTarget 对象，这个通常都是用不到的。

　　也就是说，通过 glide.buildImageViewTarget() 方法，构建出了一个 GlideDrawableImageViewTarget 对象。

#### 3.1.2. GenericRequestBuilder#into

　　在 into() 方法的最后一行，又将这个参数传入到了 GenericRequestBuilder 另一个接收 Target 对象的 into() 方法当中了。

```java
    /**
     * Set the target the resource will be loaded into.
     *
     * @see Glide#clear(com.bumptech.glide.request.target.Target)
     *
     * @param target The target to load the resource into.
     * @return The given target.
     */
    public <Y extends Target<TranscodeType>> Y into(Y target) {
        Util.assertMainThread();
        if (target == null) {
            throw new IllegalArgumentException("You must pass in a non null Target");
        }
        if (!isModelSet) {
            throw new IllegalArgumentException("You must first set a model (try #load())");
        }

        Request previous = target.getRequest();

        if (previous != null) {
            previous.clear();
            requestTracker.removeRequest(previous);
            previous.recycle();
        }
        
        // 构建 Request 对象 request

        Request request = buildRequest(target);
        target.setRequest(request);
        lifecycle.addListener(target);
        // 执行这个 request
        requestTracker.runRequest(request);

        return target;
    }
```

　　这个方法最关键的两步：1. 调用 buildRequest() 方法构建出了一个 Request 对象，2. 执行这个 request。

##### 3.1.2.1. GenericRequestBuilder#buildRequest

```java
    private Request buildRequest(Target<TranscodeType> target) {
        if (priority == null) {
            priority = Priority.NORMAL;
        }
        return buildRequestRecursive(target, null);
    }

    private Request buildRequestRecursive(Target<TranscodeType> target, ThumbnailRequestCoordinator parentCoordinator) {
        if (thumbnailRequestBuilder != null) {
            if (isThumbnailBuilt) {
                throw new IllegalStateException("You cannot use a request as both the main request and a thumbnail, "
                        + "consider using clone() on the request(s) passed to thumbnail()");
            }
            // Recursive case: contains a potentially recursive thumbnail request builder.
            if (thumbnailRequestBuilder.animationFactory.equals(NoAnimation.getFactory())) {
                thumbnailRequestBuilder.animationFactory = animationFactory;
            }

            if (thumbnailRequestBuilder.priority == null) {
                thumbnailRequestBuilder.priority = getThumbnailPriority();
            }

            if (Util.isValidDimensions(overrideWidth, overrideHeight)
                    && !Util.isValidDimensions(thumbnailRequestBuilder.overrideWidth,
                            thumbnailRequestBuilder.overrideHeight)) {
              thumbnailRequestBuilder.override(overrideWidth, overrideHeight);
            }

            ThumbnailRequestCoordinator coordinator = new ThumbnailRequestCoordinator(parentCoordinator);
            Request fullRequest = obtainRequest(target, sizeMultiplier, priority, coordinator);
            // Guard against infinite recursion.
            isThumbnailBuilt = true;
            // Recursively generate thumbnail requests.
            Request thumbRequest = thumbnailRequestBuilder.buildRequestRecursive(target, coordinator);
            isThumbnailBuilt = false;
            coordinator.setRequests(fullRequest, thumbRequest);
            return coordinator;
        } else if (thumbSizeMultiplier != null) {
            // Base case: thumbnail multiplier generates a thumbnail request, but cannot recurse.
            ThumbnailRequestCoordinator coordinator = new ThumbnailRequestCoordinator(parentCoordinator);
            Request fullRequest = obtainRequest(target, sizeMultiplier, priority, coordinator);
            Request thumbnailRequest = obtainRequest(target, thumbSizeMultiplier, getThumbnailPriority(), coordinator);
            coordinator.setRequests(fullRequest, thumbnailRequest);
            return coordinator;
        } else {
            // Base case: no thumbnail.
          	//	调用 obtainRequest 方法
            return obtainRequest(target, sizeMultiplier, priority, parentCoordinator);
        }
    }

    private Request obtainRequest(Target<TranscodeType> target, float sizeMultiplier, Priority priority,
            RequestCoordinator requestCoordinator) {
        return GenericRequest.obtain(
          			// 这个 loadProvider 就是在 load()方法中设置的 FixedLoadProvider
                loadProvider,
                model,
                signature,
                context,
                priority,
                target,
                sizeMultiplier,
                placeholderDrawable,
                placeholderId,
                errorPlaceholder,
                errorId,
                fallbackDrawable,
                fallbackResource,
                requestListener,
                requestCoordinator,
                glide.getEngine(),
                transformation,
                transcodeClass,
                isCacheable,
                animationFactory,
                overrideWidth,
                overrideHeight,
                diskCacheStrategy);
    }
```

　　可以看到，buildRequest() 方法的内部其实又调用了 buildRequestRecursive()  方法，而 buildRequestRecurisive() 方法中的代码主要都是在处理缩略图。

　　这里调用了 obtainRequest() 方法来获取一个 Request 对象，而 obtainRequest() 方法中又去调用了 GenericRequest 的 obtain() 方法。注意这个 obtain() 方法需要传入非常多的参数，而其中很多参数都是比较熟悉的，像什么 placeholderId、errorPlaceholder、diskCacheStrategy 等等。因此，基本上在 load() 方法中调用的所有 API，都是在这里组装到 Request 对象当中的。

##### 3.1.2.2. GenericRequest#obtain

```java
    public static <A, T, Z, R> GenericRequest<A, T, Z, R> obtain(
            LoadProvider<A, T, Z, R> loadProvider,
            A model,
            Key signature,
            Context context,
            Priority priority,
            Target<R> target,
            float sizeMultiplier,
            Drawable placeholderDrawable,
            int placeholderResourceId,
            Drawable errorDrawable,
            int errorResourceId,
            Drawable fallbackDrawable,
            int fallbackResourceId,
            RequestListener<? super A, R> requestListener,
            RequestCoordinator requestCoordinator,
            Engine engine,
            Transformation<Z> transformation,
            Class<R> transcodeClass,
            boolean isMemoryCacheable,
            GlideAnimationFactory<R> animationFactory,
            int overrideWidth,
            int overrideHeight,
            DiskCacheStrategy diskCacheStrategy) {
        @SuppressWarnings("unchecked")
        GenericRequest<A, T, Z, R> request = (GenericRequest<A, T, Z, R>) REQUEST_POOL.poll();
        if (request == null) {
            request = new GenericRequest<A, T, Z, R>();
        }
   			// 设置 GenericRequest 的 loadProvider 成员变量为 FixedLoadProvider
        request.init(loadProvider,
                model,
                signature,
                context,
                priority,
                target,
                sizeMultiplier,
                placeholderDrawable,
                placeholderResourceId,
                errorDrawable,
                errorResourceId,
                fallbackDrawable,
                fallbackResourceId,
                requestListener,
                requestCoordinator,
                engine,
                transformation,
                transcodeClass,
                isMemoryCacheable,
                animationFactory,
                overrideWidth,
                overrideHeight,
                diskCacheStrategy);
        return request;
    }
```

　　方法中 new 了一个 GenericRequest 对象，然后调用了 GeneticRequest 的 init() ，里面主要就是一些赋值的代码。将传入的这些参数赋值到 GenericRequest 的成员变量当中，最后将 new 的 GenericRequest 对象返回，也就是说，**obtain() 方法实际上获得的就是一个 GenericRequest 对象**。

　　在 init() 方法中会调用 requestTracker.runRequest() 方法来执行这个 Request。

#### 3.1.3. RequestTracker#runRequest

```java
    /**
     * Starts tracking the given request.
     */
    public void runRequest(Request request) {
        requests.add(request);
        if (!isPaused) {
            request.begin();
        } else {
            pendingRequests.add(request);
        }
    }
```

　　这里有一个简单的逻辑判断，就是先判断 Glide 当前是不是处于暂停状态，如果不是暂停状态就调用 Request 的 begin() 方法来执行 Request，否则的话就先将 Request 添加到待执行队列里面，等暂停状态解除了之后再执行。

#### 3.1.4. GenericRequest#begin

```java
    /**
     * {@inheritDoc}
     */
    @Override
    public void begin() {
        startTime = LogTime.getLogTime();
        if (model == null) {
            onException(null);
            return;
        }

        status = Status.WAITING_FOR_SIZE;
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
          	// 	调用 onSizeReady() 方法
            onSizeReady(overrideWidth, overrideHeight);
        } else {
          	//	调用 target.getSize() 方法，对图片的大小进行调整，最后也会调用到 onSizeReady() 方法
            target.getSize(this);
        }

        if (!isComplete() && !isFailed() && canNotifyStatusChanged()) {
          	// 调用了 target 的 onLoadStarted() 方法，将 plceholder() 设置的图片替换给 view
            target.onLoadStarted(getPlaceholderDrawable());
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished run method in " + LogTime.getElapsedMillis(startTime));
        }
    }
```

　　首先如果 model 等于 null，model 就是在调用 load() 方法中传入的图片 URL 地址，这个时候会调用 onException() 方法。而 onException() 方法最终会调用到一个 setErrorPlaceholder() 当中。

##### 3.1.4.1. GenericRequest#setErrorPlaceholder

```java
    private void setErrorPlaceholder(Exception e) {
        if (!canNotifyStatusChanged()) {
            return;
        }

        Drawable error = model == null ? getFallbackDrawable() : null;
        if (error == null) {
          error = getErrorDrawable();
        }
        if (error == null) {
            error = getPlaceholderDrawable();
        }
        target.onLoadFailed(e, error);
    }
```

　　这个方法中会先去获取一个 error 的占位图，如果获取不到的话会再去获取一个 loading 占位图，然后调用 target.onLoadFailed() 方法并将占位图传入。

##### 3.1.4.2. ImageViewTarget#onLoadFailed

```java
/**
 * A base {@link com.bumptech.glide.request.target.Target} for displaying resources in
 * {@link android.widget.ImageView}s.
 *
 * @param <Z> The type of resource that this target will display in the wrapped {@link android.widget.ImageView}.
 */
public abstract class ImageViewTarget<Z> extends ViewTarget<ImageView, Z> implements GlideAnimation.ViewAdapter {

    /**
     * Sets the given {@link android.graphics.drawable.Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param placeholder {@inheritDoc}
     */
    @Override
    public void onLoadStarted(Drawable placeholder) {
      	// 替换了 view 的显示图片
        view.setImageDrawable(placeholder);
    }

    /**
     * Sets the given {@link android.graphics.drawable.Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param errorDrawable {@inheritDoc}
     */
    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
      	// 替换了 view 的显示图片
        view.setImageDrawable(errorDrawable);
    }
    
}
```

　　onLoadFailed 方法很简单，就是将这张 error 占位图显示到 ImageView 上而已，因为现在出现了异常，没办法展示正常的图片了。然后在 begin() 方法调用 setErrorPlaceholder() 方法的后面又调用了一个 target.onLoadStarted() 方法，并传入了一个 loading 占位图，也就是说，在图片请求开始之前，会先使用这张占位图代替最终的图片显示。这也就是 placeholder() 和 error() 这两个占位图 API  底层的实现原理。

　　在 beigin() 方法中，具体的图片加载是由 onSizeReady() 和 target.getSize() 两个方法来完成的。这里分为两种情况，一种是使用了 override() API 为图片指定了一个固定的宽高，一种是没有指定。如果指定了的话，就会执行 onSizeReady() 方法。如果没指定的话，就会执行 target.getSize() 方法。而 target.getSize() 方法的内部会根据 ImageView 的 layout_width 和 layout_height 值做一系列的计算，来算出图片应该的宽高，在计算完之后，它也会调用 onSizeReady() 方法。也就是说，不管是哪种情况，最终都会调用到 onSizeReady() 方法。

#### 3.1.5. GenericRequest#onSizeReady

```java
    /**
     * A callback method that should never be invoked directly.
     */
    @Override
    public void onSizeReady(int width, int height) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("Got onSizeReady in " + LogTime.getElapsedMillis(startTime));
        }
        if (status != Status.WAITING_FOR_SIZE) {
            return;
        }
        status = Status.RUNNING;

        width = Math.round(sizeMultiplier * width);
        height = Math.round(sizeMultiplier * height);
				// 这个 loadProvider 就是传递进来的 FixedLoadProvider 对象
      	// 而 loadProvider.getModelLoader() 返回的是在 DrawableTypeRequest 类的 buildProvider 方法中创建的 ImageVideoModelLoader
        ModelLoader<A, T> modelLoader = loadProvider.getModelLoader();
      	// 而 modelLoader.getResourceFetcher 返回的 ImageVideoFetcher 对象
        final DataFetcher<T> dataFetcher = modelLoader.getResourceFetcher(model, width, height);

        if (dataFetcher == null) {
            onException(new Exception("Failed to load model: \'" + model + "\'"));
            return;
        }
        ResourceTranscoder<Z, R> transcoder = loadProvider.getTranscoder();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished setup for calling load in " + LogTime.getElapsedMillis(startTime));
        }
        loadedFromMemoryCache = true;
        loadStatus = engine.load(signature, width, height,dataFetcher, 
                                 // 这个 loadProvider 就是传递进来的 FixedLoadProvider 对象
                                 loadProvider, 
                                 transformation, transcoder,
                priority, isMemoryCacheable, diskCacheStrategy, 
                                 // 自己就是回调
                                 this);
        loadedFromMemoryCache = resource != null;
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("finished onSizeReady in " + LogTime.getElapsedMillis(startTime));
        }
    }
```

　　在方法中先调用了 loadProvider.getModelLoader() 方法，而 loadProvider 是什么？

##### 3.1.5.1. loadProvider 是什么

　　在调用 load() 方法的时候，返回的是一个 DrawableTypeRequest 对象，而 DrawableTypeRequest 的构造函数为：

```java
public class DrawableTypeRequest<ModelType> extends DrawableRequestBuilder<ModelType> implements DownloadOptions {
    private final ModelLoader<ModelType, InputStream> streamModelLoader;
    private final ModelLoader<ModelType, ParcelFileDescriptor> fileDescriptorModelLoader;
    private final RequestManager.OptionsApplier optionsApplier;

    private static <A, Z, R> FixedLoadProvider<A, ImageVideoWrapper, Z, R> buildProvider(Glide glide,
            ModelLoader<A, InputStream> streamModelLoader,
            ModelLoader<A, ParcelFileDescriptor> fileDescriptorModelLoader, Class<Z> resourceClass,
            Class<R> transcodedClass,
            ResourceTranscoder<Z, R> transcoder) {
        if (streamModelLoader == null && fileDescriptorModelLoader == null) {
            return null;
        }

        if (transcoder == null) {
          	// 调用 glide.buildTranscoder 方法创建 transcoder 对象
            transcoder = glide.buildTranscoder(resourceClass, transcodedClass);
        }
      	// 调用 glide.buildDataProvider 方法创建 DataLoadProvder 对象
        DataLoadProvider<ImageVideoWrapper, Z> dataLoadProvider = glide.buildDataProvider(ImageVideoWrapper.class,
                resourceClass);
      	// 创建 ImageVideoModelLoader 对象
        ImageVideoModelLoader<A> modelLoader = new ImageVideoModelLoader<A>(streamModelLoader,
                fileDescriptorModelLoader);
        // 返回的是一个 FixedLoadProvider 对象，参数有 modelLoader、transcoder、dataLoadProvider
        return new FixedLoadProvider<A, ImageVideoWrapper, Z, R>(modelLoader, transcoder, dataLoadProvider);
    }

    DrawableTypeRequest(Class<ModelType> modelClass, ModelLoader<ModelType, InputStream> streamModelLoader,
            ModelLoader<ModelType, ParcelFileDescriptor> fileDescriptorModelLoader, Context context, Glide glide,
            RequestTracker requestTracker, Lifecycle lifecycle, RequestManager.OptionsApplier optionsApplier) {
        // 给父类设置 provider 变量的值是 buildProvider 的返回值
        super(context, modelClass,
                buildProvider(glide, streamModelLoader, fileDescriptorModelLoader, GifBitmapWrapper.class,
                        GlideDrawable.class, null),
                glide, requestTracker, lifecycle);
        this.streamModelLoader = streamModelLoader;
        this.fileDescriptorModelLoader = fileDescriptorModelLoader;
        this.optionsApplier = optionsApplier;
    }
}
```

　　在构造函数中，调用了一个 buildProvider() 方法，并把 streamModelLoader 和 fileDescriptorModelLoader 等参数传入到这个方法中，这两个 ModelLoader 就是之前在 loadGeneric() 方法中构建出来的。

　　而 buildProvider() 方法里面调用了 glide.buildTranscoder() 方法来构建一个 ResourceTranscodel，它是用于对图片进行转码的，由于 ResourceTranscodel 是一个接口，这里实际会构建出一个 GifBitmapWrapperDrawableTranscoder 对象。

　　然后调用了 glide.buildDataProvider() 方法来构建一个 DataLoadProvider，它是用于对图片进行编解码的，由于 DataLoadProvider 是一个接口，这里实际会构建出一个 ImageVideoGifDrawableLoadProvider 对象。

　　接着 new 了一个 ImageVideoModelLoader 的实例，并把之前 loadGeric() 方法中构建的两个 ModelLoader 封装到了 ImageVideoModelLoader 当中。

　　最后 new 出一个 FixedLoadProvider，并把前面构建出来的 GifBitmapWrapperDrawableTranscoder、ImageVideoModelLoader、ImageVideoGifDrawableLoadProvider 都封装进去，这个也就是 onSizeReady() 方法中的 loadProvider 了。**所以 onSizeReady() 中的 loadProvider 就是 FixedLoadProvider 对象**。

　　在 onSizeReady() 方法中分别调用了 loadProvicer 的 getModelLoader() 方法和 getTranscodel() 方法，那么得到的对象也就是上面说的 ImageVideoModelLoader 和 GifBitmapWrapperDrawableTranscoder 了。接着调用了 ImageVideoModelLoader 的 getResourceFetcher() 方法。

##### 3.1.5.2. ImageVideoModelLoader#getResourceFetcher #

```java
/**
 * A wrapper model loader that provides both an {@link java.io.InputStream} and a
 * {@link android.os.ParcelFileDescriptor} for a given model type by wrapping an
 * {@link com.bumptech.glide.load.model.ModelLoader} for {@link java.io.InputStream}s for the given model type and an
 * {@link com.bumptech.glide.load.model.ModelLoader} for {@link android.os.ParcelFileDescriptor} for the given model
 * type.
 *
 * @param <A> The model type.
 */
public class ImageVideoModelLoader<A> implements ModelLoader<A, ImageVideoWrapper> {
    private static final String TAG = "IVML";

  	// streamLoader 成员变量
    private final ModelLoader<A, InputStream> streamLoader;
  	// fileDescriptorLoader 成员变量
    private final ModelLoader<A, ParcelFileDescriptor> fileDescriptorLoader;

    public ImageVideoModelLoader(ModelLoader<A, InputStream> streamLoader,
            ModelLoader<A, ParcelFileDescriptor> fileDescriptorLoader) {
        if (streamLoader == null && fileDescriptorLoader == null) {
            throw new NullPointerException("At least one of streamLoader and fileDescriptorLoader must be non null");
        }
        this.streamLoader = streamLoader;
        this.fileDescriptorLoader = fileDescriptorLoader;
    }

    @Override
    public DataFetcher<ImageVideoWrapper> getResourceFetcher(A model, int width, int height) {
        DataFetcher<InputStream> streamFetcher = null;
        if (streamLoader != null) {
          	// 调用 streamLoader 的 getResourceFetcher 方法创建 streamFetcher 对象
            streamFetcher = streamLoader.getResourceFetcher(model, width, height);
        }
        DataFetcher<ParcelFileDescriptor> fileDescriptorFetcher = null;
        if (fileDescriptorLoader != null) {
          	// 调用 fileDescriptorLoader 的 getResourceFetcher 方法，创建 fileDescriptorFetcher 对象
            fileDescriptorFetcher = fileDescriptorLoader.getResourceFetcher(model, width, height);
        }

        if (streamFetcher != null || fileDescriptorFetcher != null) {
          	// 返回 ImageVideoFetcher 对象
            return new ImageVideoFetcher(streamFetcher, fileDescriptorFetcher);
        } else {
            return null;
        }
    }

    static class ImageVideoFetcher implements DataFetcher<ImageVideoWrapper> {
      	// streamFetcher 成员变量
        private final DataFetcher<InputStream> streamFetcher;
      	// fileDescriptorFetcher 成员变量
        private final DataFetcher<ParcelFileDescriptor> fileDescriptorFetcher;

        public ImageVideoFetcher(DataFetcher<InputStream> streamFetcher,
                DataFetcher<ParcelFileDescriptor> fileDescriptorFetcher) {
            this.streamFetcher = streamFetcher;
            this.fileDescriptorFetcher = fileDescriptorFetcher;
        }
        ...
   }
}
```

　　先调用了 streamLoader.getResourceFetcher() 方法获取一个 DataFetcher，而这个 streamLoader 就是在 loadGeneric() 方法中构建出的 StreamStringLoader，调用它的 getResourceFetcher() 方法会得到一个 HttpUrlFetcher 对象。然后 new 出了一个 ImageVideoFetcher 对象，并把获得的 HttpUrlFetcher 对象传进去。也就是说，ImageVideoModelLoader 的 getResourceFetcher() 方法得到的是一个 ImageVideoFetcher。

　　在 onSizeReady() 方法会将获得的 ImageVideoFetcher、GitBitmapWrapperDrawableTranscoder 等等一系列的值一起传入到 Engine 的 load() 方法当中。

##### 3.1.5.3. Engine#load

```java
/**
 * Responsible for starting loads and managing active and cached resources.
 */
public class Engine implements EngineJobListener,
        MemoryCache.ResourceRemovedListener,
        EngineResource.ResourceListener {
        
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
                                     // loadProvider 就是传递进来的 FixedLoadProvider
            DataLoadProvider<T, Z> loadProvider, 
                                     Transformation<Z> transformation, ResourceTranscoder<Z, R> transcoder,
            Priority priority, boolean isMemoryCacheable, DiskCacheStrategy diskCacheStrategy, 
                                     // 这个 ResourceCallback 就是 GenericRequest 类型对象
                                     ResourceCallback cb) {
        Util.assertMainThread();
        long startTime = LogTime.getLogTime();

        final String id = fetcher.getId();
      	// 调用 keyFactory.buildKey 生成 EngineKey 的对象 key 值
        EngineKey key = keyFactory.buildKey(id, signature, width, height, loadProvider.getCacheDecoder(),
                loadProvider.getSourceDecoder(), transformation, loadProvider.getEncoder(),
                transcoder, loadProvider.getSourceEncoder());

        // 处理缓存
        EngineResource<?> cached = loadFromCache(key, isMemoryCacheable);
        if (cached != null) {
          	// 缓存存在
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
				// 没有缓存
        // 构建了一个 EngineJob
        EngineJob engineJob = engineJobFactory.build(key, isMemoryCacheable);
      	// 创建 DecodeJob 对象
        DecodeJob<T, Z, R> decodeJob = new DecodeJob<T, Z, R>(key, width, height, fetcher, 
                                                              // 这个 loadProvider 就是传递进来的 FixedLoadProvider 对象
                                                              // 将 DeocodeJob 的 loadProvider 成员变量设置为 FixedLoadProvider 对象
                                                              loadProvider, 
                                                              transformation,
                transcoder, diskCacheProvider, diskCacheStrategy, priority);
      	// 创建 EngineRunnable 对象
        EngineRunnable runnable = new EngineRunnable(engineJob, decodeJob, priority);
        jobs.put(key, engineJob);
       	// 添加回调
        engineJob.addCallback(cb);
      	// 调用 engineJob 的 start 方法
        engineJob.start(runnable);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Started new load", startTime, key);
        }
      	// 返回一个 LoadStatus 对象
        return new LoadStatus(cb, engineJob);
    }
    ...
    
}      
```

　　先是处理缓存，如果有缓存，就调用回调的 onResourceReady(cached) 方法，并返回，如果没有缓存就先构建了一个 EngineJob，它的主要作用就是用来开启线程的，为后面的异步加载图片做准备。接着创建了一个 DecodeJob 对象，主要用来对图片进行解码的，然后创建了一个 EngineRunnable 对象，并且调用了 EngineJob 的 start() 方法来运行 EngineRunnable 对象，这实际上就是让 EngineRunnable 的 run() 方法在子线程当中执行了。

##### 3.1.5.4. EngineRunnable#run

```java
    @Override
    public void run() {
        if (isCancelled) {
            return;
        }

        Exception exception = null;
        Resource<?> resource = null;
        try {
          	// 调用 decode() 方法，返回 Resource 对象
            resource = decode();
        } catch (Exception e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Exception decoding", e);
            }
            exception = e;
        }

        if (isCancelled) {
            if (resource != null) {
                resource.recycle();
            }
            return;
        }

        if (resource == null) {
          	// 调用 onLadFailed() 方法
            onLoadFailed(exception);
        } else {
          	// 调用 onLoadComplete() 方法
            onLoadComplete(resource);
        }
    }
```

　　方法里调用了一个 decode() 方法，并且这个方法返回了一个 Resource 对象。

##### 3.1.5.5. EngineRunnable#decode

```java
    private Resource<?> decode() throws Exception {
        if (isDecodingFromCache()) {
          	// 从缓存中取图片，调用 decodeFromCache() 方法
            return decodeFromCache();
        } else {
          	// 没有缓存，则调用 decodeFromSource() 方法
            return decodeFromSource();
        }
    }
```

　　decode() 方法中又分了两种情况，从缓存当中取 decode 图片的话就会执行 decodeFromCache() ，否则的话就会执行 decodeFromSource()。先忽略缓存的情况，直接看 decodeFromSource() 方法。

##### 3.1.5.6. EngineRunnable#decodeFromSource

```java
    private Resource<?> decodeFromSource() throws Exception {
      	// 调用 decodeJob 的 decodeFromSource() 方法
        return decodeJob.decodeFromSource();
    }
```

　　这里调用了 DecodeJob 的 decodeFromSource() 方法。

##### 3.1.5.7. DecodeJob#decodeFromSource

```java
class DecodeJob<A, T, Z> {
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
      	// 调用 decodeSource() 方法，得到 Resource 对象 decode
        Resource<T> decoded = decodeSource();
      	// 调用 transformEncodeAndTranscode() 方法
        return transformEncodeAndTranscode(decoded);
    }

    private Resource<T> decodeSource() throws Exception {
        Resource<T> decoded = null;
        try {
            long startTime = LogTime.getLogTime();
          	// 调用 fetcher.loadData() 方法，如果是请求网络，最终会返回请求结果 InputStream 对象
            final A data = fetcher.loadData(priority);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Fetched data", startTime);
            }
            if (isCancelled) {
                return null;
            }
          	// 调用 decodeFromSourceData() 方法，并返回方法的返回值
            decoded = decodeFromSourceData(data);
        } finally {
            fetcher.cleanup();
        }
        return decoded;
    }
    ...

}
```

　　decodeFromSource() 方法的工作分为两部，第一步是调用 decodeSource() 方法来获取一个 Resource 对象，第二步是调用 transformEncodeAndTranscode() 方法来处理这个 Resource 对象。

　　decodeSource() 方法的逻辑并不复杂，调用了 fetcher.loadData() 方法，而 fetcher 就是在 onSizeReady() 方法中得到的 ImageVideoFetcher 对象。

##### 3.1.5.8. ImageVideoFetcher#loadData

　　ImageVideoFetcher 是 ImageVideoModelLoader 的内部类。

```java
    static class ImageVideoFetcher implements DataFetcher<ImageVideoWrapper> {
        @SuppressWarnings("resource")
        // @see ModelLoader.loadData
        @Override
        public ImageVideoWrapper loadData(Priority priority) throws Exception {
            InputStream is = null;
            if (streamFetcher != null) {
                try {
                  	// 调用 streamFetcher 的 loadData 方法
                    is = streamFetcher.loadData(priority);
                } catch (Exception e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Exception fetching input stream, trying ParcelFileDescriptor", e);
                    }
                    if (fileDescriptorFetcher == null) {
                        throw e;
                    }
                }
            }
            ParcelFileDescriptor fileDescriptor = null;
            if (fileDescriptorFetcher != null) {
                try {
                  	// 调用 fileDescriptorFetcher 的 loadData() 方法
                    fileDescriptor = fileDescriptorFetcher.loadData(priority);
                } catch (Exception e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Exception fetching ParcelFileDescriptor", e);
                    }
                    if (is == null) {
                        throw e;
                    }
                }
            }
          	// 创建 ImageVideoWrapper 对象，并返回
            return new ImageVideoWrapper(is, fileDescriptor);
        } 
}
```

　　在 ImageVideoFetcher 的 loadData() 方法调用了 streamFetcher.loadData() 方法，而 streamFetcher 就是在组装 ImageVideoFetcher 对象时传进来的 HttpUrlFetcher 了。

###### 3.1.5.8.1. HttpUrlFetcher#loadData

```java
/**
 * A DataFetcher that retrieves an {@link java.io.InputStream} for a Url.
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {

    @Override
    public InputStream loadData(Priority priority) throws Exception {
      	// 调用 loadDataWithRedirects 方法
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
      	// 网络请求开始
       	// 创建 urlConnection 对象
        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
          urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(2500);
        urlConnection.setReadTimeout(2500);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        // Connect explicitly to avoid errors in decoders if connection fails.
      	// 发起请求
        urlConnection.connect();
        if (isCancelled) {
            return null;
        }
      	// 请求返回码
        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
          	// 请求成功
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (statusCode / 100 == 3) {
          	// 重定向
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
          	// 获取了请求的结果
            stream = urlConnection.getInputStream();
        }
      	// 返回请求结果
        return stream;
    }

}
```

　　在这里找到了网络通讯的代码。可以看到，loadData() 方法只是返回了一个 InputStream，服务器返回的数据还没有开始读。回到 ImageVideoFetcher 的 loadData() 方法中，在这个方法的最后一行，创建了一个 ImageVideoWrapper 对象，并将得到的 InputStream 作为参数传了进去。

### 3.2. 第二步：解析请求结果

　　从 ImageVideoFetcher 的 loadData() 方法返回到 DecodeJob 的 decodeSource() 方法中，在得到了这个 ImageVideoWrapper 对象之后，紧接着又将这个对象传入到了 decodeFromSourceData() 当中，去解码这个对象。

#### 3.2.1. DecodeJob#decodeFromSourceData

```java
    private Resource<T> decodeFromSourceData(A data) throws IOException {
        final Resource<T> decoded;
        if (diskCacheStrategy.cacheSource()) {
            decoded = cacheAndDecodeSourceData(data);
        } else {
            long startTime = LogTime.getLogTime();
          	// 调用 loadProvider.getSourceDecoder() 方法得到 GifBitmapWrapperResourceDecoder 对象
          	// 调用 GifBitmapWrapperResourceDecoder 的 decode() 方法
            decoded = loadProvider.getSourceDecoder().decode(data, width, height);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Decoded from source", startTime);
            }
        }
        return decoded;
    }
```

　　调用了 loadProvider.getSourceDecoder().decode() 方法来进行解析。loadProvider 就是在 onSizeReady() 方法中得到的 FixLoadProvider，而 getSourceDecoder() 得到的则是一个 GifBitmapWrapperResourceDecoder 对象，也就是要调用这个对象的 decode() 方法来对图片进行解码。

#### 3.2.2.  GifBitmapWrapperResourceDecoder#decode

```java

public class GifBitmapWrapperResourceDecoder implements ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> {
    
    @SuppressWarnings("resource")
    // @see ResourceDecoder.decode
    @Override
    public Resource<GifBitmapWrapper> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        ByteArrayPool pool = ByteArrayPool.get();
        byte[] tempBytes = pool.getBytes();

        GifBitmapWrapper wrapper = null;
        try {
          	// 调用 decode() 方法
            wrapper = decode(source, width, height, tempBytes);
        } finally {
            pool.releaseBytes(tempBytes);
        }
      	// 返回 GifBitmapWrapperResouce() 对象
        return wrapper != null ? new GifBitmapWrapperResource(wrapper) : null;
    }

	private GifBitmapWrapper decode(ImageVideoWrapper source, int width, int height, byte[] bytes) throws IOException {
        final GifBitmapWrapper result;
        if (source.getStream() != null) {
          	// 调用 decodeStream() 方法
            result = decodeStream(source, width, height, bytes);
        } else {
          	// 调用 decodeBitmapWrapper 对象
            result = decodeBitmapWrapper(source, width, height);
        }
        return result;
    }
 
    private GifBitmapWrapper decodeStream(ImageVideoWrapper source, int width, int height, byte[] bytes)
            throws IOException {
        InputStream bis = streamFactory.build(source.getStream(), bytes);
        bis.mark(MARK_LIMIT_BYTES);
      	// 调用 parser.parse 方法解析
        ImageHeaderParser.ImageType type = parser.parse(bis);
        bis.reset();

        GifBitmapWrapper result = null;
      	// 如果是 gif 格式
        if (type == ImageHeaderParser.ImageType.GIF) {
          	// 调用 decodeGifWrapper 方法解析结果
            result = decodeGifWrapper(bis, width, height);
        }
        // Decoding the gif may fail even if the type matches.
        if (result == null) {
            // We can only reset the buffered InputStream, so to start from the beginning of the stream, we need to
            // pass in a new source containing the buffered stream rather than the original stream.
          	// 创建 ImageVideowrapper 对象 forBitmapDecoder
            ImageVideoWrapper forBitmapDecoder = new ImageVideoWrapper(bis, source.getFileDescriptor());
          	// 调用 decodeBitmapWrapper 按照图片格式解析结果
            result = decodeBitmapWrapper(forBitmapDecoder, width, height);
        }
        return result;
    }
  
    private GifBitmapWrapper decodeBitmapWrapper(ImageVideoWrapper toDecode, int width, int height) throws IOException {
        GifBitmapWrapper result = null;

      	// 调用 bitmapDecoder 的 decode 方法解析
        Resource<Bitmap> bitmapResource = bitmapDecoder.decode(toDecode, width, height);
        if (bitmapResource != null) {
            result = new GifBitmapWrapper(bitmapResource, null);
        }

        return result;
    }
    
}
```

　　首先，在 decode() 方法中，又去调用了另外一个 decode() 方法的重载，然后在 decode() 方法中调用了 decodeStream() 方法，准备从服务器返回的流当中读取数据。decodeStream() 方法中会先从流中读取 2 个字节的数据，来判断这张图是 GIF 图还是普通的静图，如果是 GIF 图就调用 decodeGifWrapper() 方法来进行解码，如果是普通的静图就调用 decodeBitmapWrapper() 方法来进行解码。

　　而 decodeBitmapWrapper() 方法调用了 bitmapDecoder.decode() 方法。这个 bitmapDecoder 是一个 ImageVideoBitmapDecoder 对象。

#### 3.2.3. ImageVideoBitmapDecoder#decode

```java
/**
 * A {@link ResourceDecoder} that decodes {@link ImageVideoWrapper}s using
 * a wrapped {@link ResourceDecoder} for {@link InputStream}s
 * and a wrapped {@link ResourceDecoder} for {@link ParcelFileDescriptor}s.
 * The {@link InputStream} data in the {@link ImageVideoWrapper} is always preferred.
 */
public class ImageVideoBitmapDecoder implements ResourceDecoder<ImageVideoWrapper, Bitmap> {
    private static final String TAG = "ImageVideoDecoder";
    private final ResourceDecoder<InputStream, Bitmap> streamDecoder;
    private final ResourceDecoder<ParcelFileDescriptor, Bitmap> fileDescriptorDecoder;

    public ImageVideoBitmapDecoder(ResourceDecoder<InputStream, Bitmap> streamDecoder,
            ResourceDecoder<ParcelFileDescriptor, Bitmap> fileDescriptorDecoder) {
        this.streamDecoder = streamDecoder;
        this.fileDescriptorDecoder = fileDescriptorDecoder;
    }

    @SuppressWarnings("resource")
    // @see ResourceDecoder.decode
    @Override
    public Resource<Bitmap> decode(ImageVideoWrapper source, int width, int height) throws IOException {
        Resource<Bitmap> result = null;
      	// 取出数据内容
        InputStream is = source.getStream();
        if (is != null) {
            try {
              	// 调用 streamDecoder 的 decode 方法解析数据
                result = streamDecoder.decode(is, width, height);
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Failed to load image from stream, trying FileDescriptor", e);
                }
            }
        }

        if (result == null) {
            ParcelFileDescriptor fileDescriptor = source.getFileDescriptor();
            if (fileDescriptor != null) {
                result = fileDescriptorDecoder.decode(fileDescriptor, width, height);
            }
        }
        return result;
    }

    ...
}

```

　　decode() 方法先调用了 source.getStream() 来获取服务器返回的 InputStream，然后调用了 streamDecoder.decode() 方法进行解码。

　　stramDecode 是一个 StreamBitmapDecoder 对象。

#### 3.2.4. StreamBitmapDecoder#decode

```java
/**
 * An {@link com.bumptech.glide.load.ResourceDecoder} that uses an
 * {@link com.bumptech.glide.load.resource.bitmap.Downsampler} to decode an {@link android.graphics.Bitmap} from an
 * {@link java.io.InputStream}.
 */
public class StreamBitmapDecoder implements ResourceDecoder<InputStream, Bitmap> {
    private static final String ID = "StreamBitmapDecoder.com.bumptech.glide.load.resource.bitmap";
    private final Downsampler downsampler;
    private BitmapPool bitmapPool;
    private DecodeFormat decodeFormat;
    private String id;

    ...

    public StreamBitmapDecoder(Downsampler downsampler, BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        this.downsampler = downsampler;
        this.bitmapPool = bitmapPool;
        this.decodeFormat = decodeFormat;
    }

    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height) {
      	// 调用 downsampler 的 decode 方法将数据转换为 Bitmap 数据
        Bitmap bitmap = downsampler.decode(source, bitmapPool, width, height, decodeFormat);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    ...
}
```

　　decode() 方法又去调用了 Downsampler 的 decode() 方法。

#### 3.2.5. DownSampler#decode

```java
/**
 * A base class with methods for loading and decoding images from InputStreams.
 */
public abstract class Downsampler implements BitmapDecoder<InputStream> {
    ...


    /**
     * Load the image for the given InputStream. If a recycled Bitmap whose dimensions exactly match those of the image
     * for the given InputStream is available, the operation is much less expensive in terms of memory.
     *
     * <p>
     *     Note - this method will throw an exception of a Bitmap with dimensions not matching
     *     those of the image for the given InputStream is provided.
     * </p>
     *
     * @param is An {@link InputStream} to the data for the image.
     * @param pool A pool of recycled bitmaps.
     * @param outWidth The width the final image should be close to.
     * @param outHeight The height the final image should be close to.
     * @return A new bitmap containing the image from the given InputStream, or recycle if recycle is not null.
     */
    @SuppressWarnings("resource")
    // see BitmapDecoder.decode
    @Override
    public Bitmap decode(InputStream is, BitmapPool pool, int outWidth, int outHeight, DecodeFormat decodeFormat) {
        final ByteArrayPool byteArrayPool = ByteArrayPool.get();
        final byte[] bytesForOptions = byteArrayPool.getBytes();
        final byte[] bytesForStream = byteArrayPool.getBytes();
        final BitmapFactory.Options options = getDefaultOptions();

        // Use to fix the mark limit to avoid allocating buffers that fit entire images.
        RecyclableBufferedInputStream bufferedStream = new RecyclableBufferedInputStream(
                is, bytesForStream);
        // Use to retrieve exceptions thrown while reading.
        // TODO(#126): when the framework no longer returns partially decoded Bitmaps or provides a way to determine
        // if a Bitmap is partially decoded, consider removing.
        ExceptionCatchingInputStream exceptionStream =
                ExceptionCatchingInputStream.obtain(bufferedStream);
        // Use to read data.
        // Ensures that we can always reset after reading an image header so that we can still attempt to decode the
        // full image even when the header decode fails and/or overflows our read buffer. See #283.
        MarkEnforcingInputStream invalidatingStream = new MarkEnforcingInputStream(exceptionStream);
        try {
            exceptionStream.mark(MARK_POSITION);
            int orientation = 0;
            try {
                orientation = new ImageHeaderParser(exceptionStream).getOrientation();
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Cannot determine the image orientation from header", e);
                }
            } finally {
                try {
                    exceptionStream.reset();
                } catch (IOException e) {
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Cannot reset the input stream", e);
                    }
                }
            }

            options.inTempStorage = bytesForOptions;

            final int[] inDimens = getDimensions(invalidatingStream, bufferedStream, options);
            final int inWidth = inDimens[0];
            final int inHeight = inDimens[1];

            final int degreesToRotate = TransformationUtils.getExifOrientationDegrees(orientation);
            final int sampleSize = getRoundedSampleSize(degreesToRotate, inWidth, inHeight, outWidth, outHeight);

          	// 对图片进行压缩，将数据转换成 Bitmap 
            final Bitmap downsampled =
                    downsampleWithSize(invalidatingStream, bufferedStream, options, pool, inWidth, inHeight, sampleSize,
                            decodeFormat);

            // BitmapFactory swallows exceptions during decodes and in some cases when inBitmap is non null, may catch
            // and log a stack trace but still return a non null bitmap. To avoid displaying partially decoded bitmaps,
            // we catch exceptions reading from the stream in our ExceptionCatchingInputStream and throw them here.
            final Exception streamException = exceptionStream.getException();
            if (streamException != null) {
                throw new RuntimeException(streamException);
            }

            Bitmap rotated = null;
            if (downsampled != null) {
                rotated = TransformationUtils.rotateImageExif(downsampled, pool, orientation);

                if (!downsampled.equals(rotated) && !pool.put(downsampled)) {
                    downsampled.recycle();
                }
            }

            return rotated;
        } finally {
            byteArrayPool.releaseBytes(bytesForOptions);
            byteArrayPool.releaseBytes(bytesForStream);
            exceptionStream.release();
            releaseOptions(options);
        }
    }

    ...

    private Bitmap downsampleWithSize(MarkEnforcingInputStream is, RecyclableBufferedInputStream  bufferedStream,
            BitmapFactory.Options options, BitmapPool pool, int inWidth, int inHeight, int sampleSize,
            DecodeFormat decodeFormat) {
        // Prior to KitKat, the inBitmap size must exactly match the size of the bitmap we're decoding.
        Bitmap.Config config = getConfig(is, decodeFormat);
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = config;
        if ((options.inSampleSize == 1 || Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) && shouldUsePool(is)) {
            int targetWidth = (int) Math.ceil(inWidth / (double) sampleSize);
            int targetHeight = (int) Math.ceil(inHeight / (double) sampleSize);
            // BitmapFactory will clear out the Bitmap before writing to it, so getDirty is safe.
            setInBitmap(options, pool.getDirty(targetWidth, targetHeight, config));
        }
      	// 调用 decodeStream 方法
        return decodeStream(is, bufferedStream, options);
    }

    ...

    /**
     * A method for getting the dimensions of an image from the given InputStream.
     *
     * @param is The InputStream representing the image.
     * @param options The options to pass to
     *          {@link BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect,
     *              android.graphics.BitmapFactory.Options)}.
     * @return an array containing the dimensions of the image in the form {width, height}.
     */
    public int[] getDimensions(MarkEnforcingInputStream is, RecyclableBufferedInputStream bufferedStream,
            BitmapFactory.Options options) {
      	// 拿到图片的宽高
        options.inJustDecodeBounds = true;
        decodeStream(is, bufferedStream, options);
        options.inJustDecodeBounds = false;
        return new int[] { options.outWidth, options.outHeight };
    }

    private static Bitmap decodeStream(MarkEnforcingInputStream is, RecyclableBufferedInputStream bufferedStream,
            BitmapFactory.Options options) {
         if (options.inJustDecodeBounds) {
             // This is large, but jpeg headers are not size bounded so we need something large enough to minimize
             // the possibility of not being able to fit enough of the header in the buffer to get the image size so
             // that we don't fail to load images. The BufferedInputStream will create a new buffer of 2x the
             // original size each time we use up the buffer space without passing the mark so this is a maximum
             // bound on the buffer size, not a default. Most of the time we won't go past our pre-allocated 16kb.
             is.mark(MARK_POSITION);
         } else {
             // Once we've read the image header, we no longer need to allow the buffer to expand in size. To avoid
             // unnecessary allocations reading image data, we fix the mark limit so that it is no larger than our
             // current buffer size here. See issue #225.
             bufferedStream.fixMarkLimit();
         }

      	// 调用 BitmapFactory.decodeStream 将 steam 转换为 Bitmap
        final Bitmap result = BitmapFactory.decodeStream(is, null, options);

        try {
            if (options.inJustDecodeBounds) {
                is.reset();
            }
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "Exception loading inDecodeBounds=" + options.inJustDecodeBounds
                        + " sample=" + options.inSampleSize, e);
            }
        }
				// 返回 result
        return result;
    }

    ...
}

```

　　可以看到，对服务器返回的 InputStream 的读取，以及对图片的加载全都在这里了。当然这里其实处理了很多的逻辑，包括对图片的压缩，甚至还有旋转、圆角等逻辑处理。decode() 方法执行之后，会返回一个 Bitmap 对象，那么图片在这里其实也就已经被加载出来了，剩下的工作就是如何让这个 Bitmap 显示到界面上。

### 3.3. 第三步：将图片显示在界面上

　　回到 StreamBitmapDecoder 当中，decode() 方法返回的是一个 Resource< Bitmap > 对象。而从 DownSampler 中得到的是一个 Bitmap。因此在 StreamBitmapDecoder 的 decode 方法中又调用了 BitmapResource.obtain() 方法，将 Bitmap 对象包装成了 Resource< Bitmap > 对象。

#### 3.3.1. BitmapResource 类

```java
/**
 * A resource wrapping a {@link android.graphics.Bitmap} object.
 */
public class BitmapResource implements Resource<Bitmap> {
    private final Bitmap bitmap;
    private final BitmapPool bitmapPool;

    /**
     * Returns a new {@link BitmapResource} wrapping the given {@link Bitmap} if the Bitmap is non-null or null if the
     * given Bitmap is null.
     *
     * @param bitmap A Bitmap.
     * @param bitmapPool A non-null {@link com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool}.
     */
    public static BitmapResource obtain(Bitmap bitmap, BitmapPool bitmapPool) {
        if (bitmap == null) {
            return null;
        } else {
            return new BitmapResource(bitmap, bitmapPool);
        }
    }

    public BitmapResource(Bitmap bitmap, BitmapPool bitmapPool) {
        if (bitmap == null) {
            throw new NullPointerException("Bitmap must not be null");
        }
        if (bitmapPool == null) {
            throw new NullPointerException("BitmapPool must not be null");
        }
        this.bitmap = bitmap;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Bitmap get() {
        return bitmap;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(bitmap);
    }

    @Override
    public void recycle() {
        if (!bitmapPool.put(bitmap)) {
            bitmap.recycle();
        }
    }
}
```

　　BitmapResource 的源码也非常简单，经过这样一层包装之后，如果还需要获取 Bitmap，只需要调用 Resource< Bitmap > 的 get() 方法就可以了。

　　然后一层一层的向上返回，StreamBitmapDecoder 会将值返回到 ImageVideoBitmapDecoder 当中，而 ImageVideoBitmapDecoder 又会将值返回到 GifBitmapWrapperResourceDecoder 的 decodeBitmapWrapper() 方法当中。

　　而 GifBitmapWrapperResourceDecoder 的 decodeBitmapWrapper() 方法中又将 Resource< Bitmap > 封装到了一个 GifBitmapWrapper 对象当中，并且返回的也是一个 GifBitmapWrapper 对象。

　　GifBitmapWrapper 就是既能封装 GIF，又能封装 Bitmap，从而保证了不管什么类型的图片 Glide 都能从容应对。

#### 3.3.2. GifBitmapWrapper 类

```java
/**
 * A wrapper that contains either an {@link android.graphics.Bitmap} resource or an
 * {@link com.bumptech.glide.load.resource.gif.GifDrawable} resource.
 */
public class GifBitmapWrapper {
    private final Resource<GifDrawable> gifResource;
    private final Resource<Bitmap> bitmapResource;

    public GifBitmapWrapper(Resource<Bitmap> bitmapResource, Resource<GifDrawable> gifResource) {
        if (bitmapResource != null && gifResource != null) {
            throw new IllegalArgumentException("Can only contain either a bitmap resource or a gif resource, not both");
        }
        if (bitmapResource == null && gifResource == null) {
            throw new IllegalArgumentException("Must contain either a bitmap resource or a gif resource");
        }
        this.bitmapResource = bitmapResource;
        this.gifResource = gifResource;
    }

    /**
     * Returns the size of the wrapped resource.
     */
    public int getSize() {
        if (bitmapResource != null) {
            return bitmapResource.getSize();
        } else {
            return gifResource.getSize();
        }
    }

    /**
     * Returns the wrapped {@link android.graphics.Bitmap} resource if it exists, or null.
     */
    public Resource<Bitmap> getBitmapResource() {
        return bitmapResource;
    }

    /**
     * Returns the wrapped {@link com.bumptech.glide.load.resource.gif.GifDrawable} resource if it exists, or null.
     */
    public Resource<GifDrawable> getGifResource() {
        return gifResource;
    }
}
```

　　GifBitmapWrapper 类比较简单，就是分别对 gifResource 和 bitmapResource 做了一层封装而已。

　　然后这个 GifBitmapWrapper 对象会一直向上返回，返回到 GifBitmapWrapperResourceDecoder 最外层的 decode() 方法的时候，会对它再做一次封装，将 GifBitmapWrapper 封装到了一个 GifBitmapWrapperResource 对象当中，最终返回的是一个 Respurce< GifBitmapWrapper > 对象。这个 GifBitmapResource 和 BitmapResource 是相似的，它们都实现了 Resource 接口，都可以通过 get() 方法来获取封装起来的具体内容。

#### 3.3.3. GifBitmapWrapperResource 类

```java
/**
 * A resource that wraps an {@link com.bumptech.glide.load.resource.gifbitmap.GifBitmapWrapper}.
 */
public class GifBitmapWrapperResource implements Resource<GifBitmapWrapper> {
    private final GifBitmapWrapper data;

    public GifBitmapWrapperResource(GifBitmapWrapper data) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        this.data = data;
    }

    @Override
    public GifBitmapWrapper get() {
        return data;
    }

    @Override
    public int getSize() {
        return data.getSize();
    }

    @Override
    public void recycle() {
        Resource<Bitmap> bitmapResource = data.getBitmapResource();
        if (bitmapResource != null) {
            bitmapResource.recycle();
        }
        Resource<GifDrawable> gifDataResource = data.getGifResource();
        if (gifDataResource != null) {
            gifDataResource.recycle();
        }
    }
}

```

　　经过这一层的封装之后，从网络上得到的图片就能够以 Resource 接口的形式返回，并且还能同时处理 Bitmap 图片和 GIF 图片这两种情况。

　　接着回到 DecodeJob 当中，它的 decodeFromSourceData() 方法返回的是一个 Resource< T > 对象，其实也就是 Resource< GifBitmapWrapper > 对象了。接着继续向上返回，最终返回到 DecodeJob 的 decodeFromSource() 方法当中，接着调用 transformEncodeAndTranscode() 方法， 而且 decodeFromSource() 方法最终返回的是一个 Resource< Z > 对象。

#### 3.3.4. DecodeJob#transformEncodeAndTranscode

```java
    private Resource<Z> transformEncodeAndTranscode(Resource<T> decoded) {
        long startTime = LogTime.getLogTime();
      	// 将 decode 转换为 Resource<T> 类型对象 transformed
        Resource<T> transformed = transform(decoded);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transformed resource from source", startTime);
        }

        // 缓存 transformed
        writeTransformedToCache(transformed);

        startTime = LogTime.getLogTime();
      	// 将 Resource<T> transformed 转换为 Resource<Z> result
        Resource<Z> result = transcode(transformed);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Transcoded transformed from source", startTime);
        }
        return result;
    }

    private Resource<Z> transcode(Resource<T> transformed) {
        if (transformed == null) {
            return null;
        }
        return transcoder.transcode(transformed);
    }
```

　　先是对 transform 进行的缓存，接着调用了一个 transcode() 方法，就把 Resource< T > 对象转换成 Resource< Z > 对象了。

　　而 transcode() 方法中又是调用了 transcoder 的 transcode() 方法。

　　在 lode() 方法中返回的那个 DrawableTypeRequest 对象，它的构建函数中去构建了一个 FixedLoadProvider 对象，然后将三个参数传入到了 FixedLoadProvider 当中，其中就有一个 GifBitmapWrapperDrawableTranscoder 对象。后来在 onSizeReady() 方法中获取到了这个参数，并传递到了 Engine 当中，然后又由 Engine 传递到了 DecodeJob 当中。因此，这里的 transcoder 其实就是 GifBitmapWrapperDrawableTranscoder 对象。

#### 3.3.5. GifBitmapWrapperDrawableTranscoder 类

```java
/**
 * An {@link com.bumptech.glide.load.resource.transcode.ResourceTranscoder} that can transcode either an
 * {@link Bitmap} or an {@link com.bumptech.glide.load.resource.gif.GifDrawable} into an
 * {@link android.graphics.drawable.Drawable}.
 */
public class GifBitmapWrapperDrawableTranscoder implements ResourceTranscoder<GifBitmapWrapper, GlideDrawable> {
    private final ResourceTranscoder<Bitmap, GlideBitmapDrawable> bitmapDrawableResourceTranscoder;

    public GifBitmapWrapperDrawableTranscoder(
            ResourceTranscoder<Bitmap, GlideBitmapDrawable> bitmapDrawableResourceTranscoder) {
        this.bitmapDrawableResourceTranscoder = bitmapDrawableResourceTranscoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Resource<GlideDrawable> transcode(Resource<GifBitmapWrapper> toTranscode) {
        GifBitmapWrapper gifBitmap = toTranscode.get();
        Resource<Bitmap> bitmapResource = gifBitmap.getBitmapResource();

        final Resource<? extends GlideDrawable> result;
        if (bitmapResource != null) {
          	// 是 bitmap 格式
            result = bitmapDrawableResourceTranscoder.transcode(bitmapResource);
        } else {
          	// 是 gif 格式
            result = gifBitmap.getGifResource();
        }
        // This is unchecked but always safe, anything that extends a Drawable can be safely cast to a Drawable.
      	// 返回的是 Resource<GlideDrawable> 类型对象
        return (Resource<GlideDrawable>) result;
    }

    @Override
    public String getId() {
        return "GifBitmapWrapperDrawableTranscoder.com.bumptech.glide.load.resource.transcode";
    }
}
```

　　GifBitmapWrapperDrawableTranscoder 的核心作用就是用来转码的。因为 GifBitmapWrapper 是无法直接显示到 ImageView 上面的，只有 Bitmap 或者 Drawable 才能显示到 ImageView 上。因此，这里的 transcode() 方法先从 Resource< GifBitmapWrapper > 中取出 GifBitmapWrapper 对象，然后再从 GifBitmapWrapper 中取出 Resource< Bitmap >对象。

　　接下来做了一个判断，如果 Resource< Bitmap > 为空，那么说明此时加载的是 GIF 图，直接调用 getGifResource() 方法将图片取出即可，因为 Glide 用于加载 GIF 图片使用的是 GifDrawable 这个类，它本身就是一个 Drawable 对象了。而如果 Resource< Bitmap > 不为空，那么就需要再做一次转码，将 Bitmap 转换成 Drawable 对象才行，因为要保证静图和动如的类型一致性，方便逻辑上处理的。

　　这里又进行了一次转码，是调用的 GlideBitmapDrawableTranscoder 对象的 transcode() 方法。

#### 3.3.6. GlideBitmapDrawableTranscoder 类

```java
/**
 * An {@link com.bumptech.glide.load.resource.transcode.ResourceTranscoder} that converts
 * {@link android.graphics.Bitmap}s into {@link android.graphics.drawable.BitmapDrawable}s.
 */
public class GlideBitmapDrawableTranscoder implements ResourceTranscoder<Bitmap, GlideBitmapDrawable> {
    private final Resources resources;
    private final BitmapPool bitmapPool;

    public GlideBitmapDrawableTranscoder(Context context) {
        this(context.getResources(), Glide.get(context).getBitmapPool());
    }

    public GlideBitmapDrawableTranscoder(Resources resources, BitmapPool bitmapPool) {
        this.resources = resources;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<GlideBitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
        GlideBitmapDrawable drawable = new GlideBitmapDrawable(resources, toTranscode.get());
        return new GlideBitmapDrawableResource(drawable, bitmapPool);
    }

    @Override
    public String getId() {
        return "GlideBitmapDrawableTranscoder.com.bumptech.glide.load.resource.transcode";
    }
}
```

　　这里 new 出了一个 GlideBitmapDrawable 对象，并把 Bitmap 封装到里面，然后对 GlideBitmapDrawable 再进行一次封装，返回一个 Resource< GlideBitmapDrawable > 对象。

  　　现在再返回到 GifBitmapWrapperDrawableTranscoder 的 transcode() 方法中，不管是静态的 Resource< GlideBitmapDrawable > 对象，还是动图的 Resource< GifDrawable > 对象，它们都是属于父类 Resource< GlideDrawable > 对象的。因此 transcode() 方法也是直接返回了 Resource< GlideDrawable >，而这个 Resource< GlideDrawable > 其实也就是转换过后的 Resource< Z > 了。

　　再回到 DecodeJob 当中，它的 decodeFromSource() 方法得到了 Resource< Z > 对象，当然也就是 Resource< GlideDrawable > 对象。然后继续向上返回会回到 EngineRunnable 的 decodeFromSource() 方法，再回到 decode() 方法，再回到 run() 方法当中。

　　在 EngineRunnable 的 run 方法中 decode() 方法执行之后最终得到了 Resource< GlideDrawable > 对象，那么接下来就是如何将它显示出来了。

　　在 EngineRunnable 的 run 方法中调用了 onLoadComplete() 方法，表示图片加载已经完成了。

#### 3.3.7. EngineRunnable#onLoadComplete

```java
private void onLoadComplete(Resource resource) {
    manager.onResourceReady(resource);
}
```

　　这个 manager 就是 EngineJob 对象，因此这里实际上调用的是 EngineJob 的 onResourceReady() 方法。

#### 3.3.8. EngineJob#onResourceReady

```java
/**
 * A class that manages a load by adding and removing callbacks for for the load and notifying callbacks when the
 * load completes.
 */
class EngineJob implements EngineRunnable.EngineRunnableManager {
    ...

    public void addCallback(ResourceCallback cb) {
        Util.assertMainThread();
        if (hasResource) {
            cb.onResourceReady(engineResource);
        } else if (hasException) {
            cb.onException(exception);
        } else {
            cbs.add(cb);
        }
    }

    ...

    @Override
    public void onResourceReady(final Resource<?> resource) {
        this.resource = resource;
      	// 向主线程发送 MSG_COMPLETE 消息，主线程接收到消息后，会回调到这里的 MainThreadCallback 的 handleMessage 方法
        MAIN_THREAD_HANDLER.obtainMessage(MSG_COMPLETE, this).sendToTarget();
    }

    @Override
    public void onException(final Exception e) {
        this.exception = e;
        MAIN_THREAD_HANDLER.obtainMessage(MSG_EXCEPTION, this).sendToTarget();
    }

    private void handleExceptionOnMainThread() {
        if (isCancelled) {
            return;
        } else if (cbs.isEmpty()) {
            throw new IllegalStateException("Received an exception without any callbacks to notify");
        }
        hasException = true;

        listener.onEngineJobComplete(key, null);

        for (ResourceCallback cb : cbs) {
            if (!isInIgnoredCallbacks(cb)) {
                cb.onException(exception);
            }
        }
    }

    // Visible for testing.
    static class EngineResourceFactory {
        public <R> EngineResource<R> build(Resource<R> resource, boolean isMemoryCacheable) {
            return new EngineResource<R>(resource, isMemoryCacheable);
        }
    }

    private static class MainThreadCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (MSG_COMPLETE == message.what || MSG_EXCEPTION == message.what) {
                EngineJob job = (EngineJob) message.obj;
                if (MSG_COMPLETE == message.what) {
                  	// 成功，调用 EngineJob 的 handleResultOnMainThread
                    job.handleResultOnMainThread();
                } else {
                  	// 异常，调用 EngineJob 的 handleExceptionOnMainThread
                    job.handleExceptionOnMainThread();
                }
                return true;
            }

            return false;
        }
    }
}

```

　　可以看到，在 onResourceReady() 方法使用 Handler 发出了一条 MSG_COMPLETE 消息，那么在 MainThreadCallback 的 handleMessage() 方法中就会收到这条消息。从这里开始，所有的逻辑又回到主线程当中进行了，因为很快就需要更新 UI 了。

　　在 MainThreadCallback 的 handleMessage 方法中，如果收到完成的消息，就会调用 EngineJob 的 handleResultOnMainThread 方法。

#### 3.3.9. EngineJob#handleResultOnMainThread

```java
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
              	// 调用回调的 onResourceReady 方法
                cb.onResourceReady(engineResource);
            }
        }
        // Our request is complete, so we can release the resource.
        engineResource.release();
    }
```

　　在 handleResultOnMainThread() 方法通过一个循环调用了所有 ResourceCallback 的 onResourceReady() 方法，而 ResourceCallback 是通过 addCallback() 方法向 cbs 集合中去添加的 ResourceCallback 。

　　而 addCallback() 方法是在 Engine 的 load() 方法中调用的。在 Engine 的 load() 方法里调用了 EngineJob 的 addCallback() 方法来注册的一个 ResourceCallback。而 Engine.load() 方法的 ResourceCallback 参数是在 GenericRequest 的 onSizeReady() 方法中调用 engine.load() 方法的时候传入的 GenericRequest 本身对象。GenericRequest 本身就实现了 ResourceCallback 的接口，所以 handleResultOnMainThread() 方法中调用的 ResourceCallback 的 onResourceReady 就是 GenericRequest  的 onResourceReady 方法。

#### 3.3.10. GenericRequest#onResourceReady

```java
    /**
     * A callback method that should never be invoked directly.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onResourceReady(Resource<?> resource) {
        if (resource == null) {
            onException(new Exception("Expected to receive a Resource<R> with an object of " + transcodeClass
                    + " inside, but instead got null."));
            return;
        }

        Object received = resource.get();
        if (received == null || !transcodeClass.isAssignableFrom(received.getClass())) {
            releaseResource(resource);
            onException(new Exception("Expected to receive an object of " + transcodeClass
                    + " but instead got " + (received != null ? received.getClass() : "") + "{" + received + "}"
                    + " inside Resource{" + resource + "}."
                    + (received != null ? "" : " "
                        + "To indicate failure return a null Resource object, "
                        + "rather than a Resource object containing null data.")
            ));
            return;
        }

        if (!canSetResource()) {
            releaseResource(resource);
            // We can't set the status to complete before asking canSetResource().
            status = Status.COMPLETE;
            return;
        }
				// 调用 onResourceReady 方法
        onResourceReady(resource, (R) received);
    }

    /**
     * Internal {@link #onResourceReady(Resource)} where arguments are known to be safe.
     *
     * @param resource original {@link Resource}, never <code>null</code>
     * @param result object returned by {@link Resource#get()}, checked for type and never <code>null</code>
     */
    private void onResourceReady(Resource<?> resource, R result) {
        // We must call isFirstReadyResource before setting status.
        boolean isFirstResource = isFirstReadyResource();
        status = Status.COMPLETE;
        this.resource = resource;

        if (requestListener == null || !requestListener.onResourceReady(result, model, target, loadedFromMemoryCache,
                isFirstResource)) {
            GlideAnimation<R> animation = animationFactory.build(loadedFromMemoryCache, isFirstResource);
          	// 调用 target 的 onResourceReady 方法
            target.onResourceReady(result, animation);
        }

        notifyLoadSuccess();

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logV("Resource ready in " + LogTime.getElapsedMillis(startTime) + " size: "
                    + (resource.getSize() * TO_MEGABYTE) + " fromCache: " + loadedFromMemoryCache);
        }
    }
```

　　首先在第一个 onResourceReady() 方法当中，调用 resource.get() 方法获取到了封装的图片对象，也就是 GlideBitmapDrawable 对象，或者是 GifDrawable 对象，然后将这个值传入到了第二个 onResourceReady() 方法当中，并且调用了 target.onResourceReady() 方法。

　　而这个 target 是在 into() 方法的最后一行调用了 glide.buildImageViewTarget() 方法来构建出一个 Target，而这个 Target 就是一个 GlideDrawableImageViewTarget 对象。

#### 3.3.10. GlideDrawableImageViewTarget 类

```java
/**
 * A {@link com.bumptech.glide.request.target.Target} that can display an {@link android.graphics.drawable.Drawable} in
 * an {@link android.widget.ImageView}.
 */
public class GlideDrawableImageViewTarget extends ImageViewTarget<GlideDrawable> {
    private static final float SQUARE_RATIO_MARGIN = 0.05f;
    private int maxLoopCount;
    private GlideDrawable resource;

    /**
     * Constructor for an {@link com.bumptech.glide.request.target.Target} that can display an
     * {@link com.bumptech.glide.load.resource.drawable.GlideDrawable} in an {@link android.widget.ImageView}.
     *
     * @param view The view to display the drawable in.
     */
    public GlideDrawableImageViewTarget(ImageView view) {
        this(view, GlideDrawable.LOOP_FOREVER);
    }

    /**
     * Constructor for an {@link com.bumptech.glide.request.target.Target} that can display an
     * {@link com.bumptech.glide.load.resource.drawable.GlideDrawable} in an {@link android.widget.ImageView}.
     *
     * @param view The view to display the drawable in.
     * @param maxLoopCount A value to pass to to {@link com.bumptech.glide.load.resource.drawable.GlideDrawable}s
     *                     indicating how many times they should repeat their animation (if they have one). See
     *                     {@link com.bumptech.glide.load.resource.drawable.GlideDrawable#setLoopCount(int)}.
     */
    public GlideDrawableImageViewTarget(ImageView view, int maxLoopCount) {
        super(view);
        this.maxLoopCount = maxLoopCount;
    }

    /**
     * {@inheritDoc}
     * If no {@link com.bumptech.glide.request.animation.GlideAnimation} is given or if the animation does not set the
     * {@link android.graphics.drawable.Drawable} on the view, the drawable is set using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param resource {@inheritDoc}
     * @param animation {@inheritDoc}
     */
    @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
        if (!resource.isAnimated()) {
            //TODO: Try to generalize this to other sizes/shapes.
            // This is a dirty hack that tries to make loading square thumbnails and then square full images less costly
            // by forcing both the smaller thumb and the larger version to have exactly the same intrinsic dimensions.
            // If a drawable is replaced in an ImageView by another drawable with different intrinsic dimensions,
            // the ImageView requests a layout. Scrolling rapidly while replacing thumbs with larger images triggers
            // lots of these calls and causes significant amounts of jank.
            float viewRatio = view.getWidth() / (float) view.getHeight();
            float drawableRatio = resource.getIntrinsicWidth() / (float) resource.getIntrinsicHeight();
            if (Math.abs(viewRatio - 1f) <= SQUARE_RATIO_MARGIN
                    && Math.abs(drawableRatio - 1f) <= SQUARE_RATIO_MARGIN) {
                resource = new SquaringDrawable(resource, view.getWidth());
            }
        }
      	// 调用父类的 onResourceReady 方法
        super.onResourceReady(resource, animation);
        this.resource = resource;
        resource.setLoopCount(maxLoopCount);
        resource.start();
    }

    /**
     * Sets the drawable on the view using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param resource The {@link android.graphics.drawable.Drawable} to display in the view.
     */
    @Override
    protected void setResource(GlideDrawable resource) {
        view.setImageDrawable(resource);
    }

    @Override
    public void onStart() {
        if (resource != null) {
            resource.start();
        }
    }

    @Override
    public void onStop() {
        if (resource != null) {
            resource.stop();
        }
    }
}
```

　　在 GlideDrawableImageViewTarget 的 onResourceReady() 方法中做了一些逻辑处理，包括如果是 GIF 图片的话，就调用 resource.start() 方法开始播放图片。其中还调用了 super.onResourceReady() 方法，GlideDrawableImageViewTraget 的父类是 ImageViewTarget。

#### 3.3.11 ImageViewTraget#onResourceReady

```java
public abstract class ImageViewTarget<Z> extends ViewTarget<ImageView, Z> implements GlideAnimation.ViewAdapter {

    @Override
    public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
        if (glideAnimation == null || !glideAnimation.animate(resource, this)) {
          	// 调用抽象方法 setResource
            setResource(resource);
        }
    }

    protected abstract void setResource(Z resource);

}
```

　　可以看到，在 ImageViewTarget 的 onResourceReady() 方法当中调用了 setResource() 方法，而 ImageViewTarget 的 setResource() 方法是一个抽象方法，具体的实现还是在子类那边实现的。

#### 3.3.12. GlideDrawableImageViewTarget#setResource

```java
    /**
     * Sets the drawable on the view using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param resource The {@link android.graphics.drawable.Drawable} to display in the view.
     */
    @Override
    protected void setResource(GlideDrawable resource) {
      	// 	设置 view 显示获取的资源
        view.setImageDrawable(resource);
    }
```

　　调用了 view.setImageDrawable() 方法，而这个 view 就是使用 Glide 时传递进来的 ImageView。这样图片就显示出来了。

　　到这里，Glide 执行流程的源码分析就结束了。


## 4. 参考文章
1. [Android图片加载框架最全解析（二），从源码的角度理解Glide的执行流程](https://blog.csdn.net/guolin_blog/article/details/53939176)

