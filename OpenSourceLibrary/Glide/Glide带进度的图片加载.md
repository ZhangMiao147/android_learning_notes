# Glide 带进度的图片加载

## 1. 扩展目标

　　虽说 Glide 本身就已经十分强大了，但是有一个功能却长期以来都不支持，那就是监听下载进度功能。

 　　使用 Glide 来加载一张网络上的图片是非常简单的，但是让人头疼的是，却无从得知当前图片的下载进度。如果这张图片很小的话，那么问题也不大，反正很快就会被加载出来。但如果这是一张比较大的 GIF 图，用户耐心等了很久结果图片还没显示出来，这个时候就会觉得下载进度功能是十分有必要的了。

## 2. 准备

 　　需要将必要的依赖库引入到当前的项目当中，目前必须要依赖的两个库就是 Glide 和 OkHttp。

 　　在 app/build.gradle 文件当中添加如下配置：

```groovy
dependencies { 
    compile 'com.github.bumptech.glide:glide:3.7.0' 
    compile 'com.squareup.okhttp3:okhttp:3.9.0' 
}
```

 　　另外，由于 Glide 和 OkHttp 都需要用到网络功能，因此还得在 AndroidManifest.xml 中声明一下网络权限才行：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

　　这样准备工作就完成了。

## 3. 替换通讯组件

　　Glide 内部 HTTP 通讯组件的底层实现是基于 HttpUrlConnection 来进行定制的。但是  HttpUrlConnection 的可扩展性比较有限，在它的基础之上无法实现监听下载进度的功能，因此首先就是要将 Glide 中的 HTTP 通讯组件替换成 OkHttp。

　　新建一个 OkHttpFetcher 类，并且实现 DataFetcher 接口，代码如下所示：

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

　　然后新建一个 OkHttpGlideUrlLoader 类，并且实现 ModelLoader。

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

　　接下来，新建一个 MyGlideModule 类并实现 GlideModule 接口，然后在 registerComponents() 方法中将刚刚创建的 OkHttpGliderUrlLoader 和 OkHttpFetcher 注册到 Glide 当中，将原来的 HTTP 通讯组件给替换掉，如下所示：

```java
public class MyGlideModule implements GlideModule { 
    @Override 
    public void applyOptions(Context context, GlideBuilder builder) { 
    } 

    @Override 
    public void registerComponents(Context context, Glide glide) { 
        glide.register(GlideUrl.class, InputStream.class, new OkHttpGlideUrlLoader.Factory());
    } 
}
```

　　最后，为了让 Glide 能够识别自定义的 MyGlideModule，还得在 AndroidManifext.xml 文件当中加入如下配置：

```xml
<manifest> 
    ... 
    <application> 
        <meta-data 
            android:name="com.example.glideprogresstest.MyGlideModule" 
            android:value="GlideModule" /> 
        ... 
    </application> 
</manifest>
```

　　这样就把 Glide 中的 HTTP 通讯组件成功替换成 OkHttp 了。

## 4. 实现下载进度监听

　　需要依靠 OkHttp 强大的拦截器机制来实现监听下载进度的功能。

　　只要向 OkHttp 中添加一个自定义的拦截器，就可以在拦截器中捕获到整个 HTTP 的通讯过程，然后加入一些自己的逻辑来计算下载进度，这样就可以实现下载进度监听的功能了。

### 4.1. 创建拦截器

　　首先创建一个没有任何逻辑的空拦截器，新建 ProgressInterceptor 类并实现 Interceptor 接口，代码如下：

```java
public class ProgressInterceptor implements Interceptor { 

    @Override 
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request(); 
        Response response = chain.proceed(request); 
        return response; 
    } 

}
```

　　这个拦截器就是拦截到了 OkHttp 的请求，然后调用 proceed() 方法去处理这个请求，最终将服务器响应的 Response 返回。

### 4.2. 启动拦截器

　　接下来需要启动这个拦截器，修改 MyGlideModule 中的代码，如下所示：

```java
public class MyGlideModule implements GlideModule { 
    @Override 
    public void applyOptions(Context context, GlideBuilder builder) { 
    } 

    @Override 
    public void registerComponents(Context context, Glide glide) { 
        OkHttpClient.Builder builder = new OkHttpClient.Builder(); 
        builder.addInterceptor(new ProgressInterceptor()); 
        OkHttpClient okHttpClient = builder.build(); 
        glide.register(GlideUrl.class, InputStream.class, new OkHttpGlideUrlLoader.Factory(okHttpClient));
    } 
}
```

　　在这里创建了一个 OkHttpClient.Builder，然后调用 addInterceptor() 方法将创建的 ProgressInterceptor 添加进去，最后将构建出来的新 OkHttpClient 对象传入到 OkHttpGlideUrlLoader.Factory 中即可。

　　现在自定义的拦截器已经启用了，接下来就可以开始去实现下载进度监听的具体逻辑了。

### 4.3. 创建监听回调

　　首先新建一个 ProgressListener 接口，用于作为进度监听回调的工具，如下所示：

```java
public interface ProgressListener {

    void onProgress(int progress);

}
```

　　然后在 ProgressInterceptor 中加入注册下载监听和取消注册下载监听的方法。修改 ProgressInterceptor 中的代码，如下所示：

```java
public class ProgressInterceptor implements Interceptor { 

    static final Map<String, ProgressListener> LISTENER_MAP = new HashMap<>();

    public static void addListener(String url, ProgressListener listener) {
        LISTENER_MAP.put(url, listener); 
    } 

    public static void removeListener(String url) { 
        LISTENER_MAP.remove(url); 
    } 

    @Override 
    public Response intercept(Chain chain) throws IOException { 
        Request request = chain.request(); 
        Response response = chain.proceed(request); 
        return response; 
    } 

}
```

　　可以看到，这里使用了一个 Map 来保存注册的监听器，Map 的键是一个 URL 地址。之所以要这么做，是因为可能会使用 Glide 同时加载很多张图片，而这种情况下，必须要能区分出来每个下载进度的回调到底是对应哪个图片 URL 地址的。

### 4.4. 下载进度的具体计算

　　接下来就是下载进度的具体计算。

　　需要新建一个 ProgressResponseBody 类，并让它继承自 OkHttp 的 ResponseBody，然后在这个类当中去编写具体的监听下载进度的逻辑，代码如下所示：

```java
public class ProgressResponseBody extends ResponseBody {

    private static final String TAG = "ProgressResponseBody";

    private BufferedSource bufferedSource;

    private ResponseBody responseBody;

    private ProgressListener listener;

    public ProgressResponseBody(String url, ResponseBody responseBody) {
        this.responseBody = responseBody;
        listener = ProgressInterceptor.LISTENER_MAP.get(url);
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override 
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(new ProgressSource(responseBody.source()));
        }
        return bufferedSource;
    }

    private class ProgressSource extends ForwardingSource {

        long totalBytesRead = 0;

        int currentProgress;

        ProgressSource(Source source) {
            super(source);
        }

        @Override 
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            long fullLength = responseBody.contentLength();
            if (bytesRead == -1) {
                totalBytesRead = fullLength;
            } else {
                totalBytesRead += bytesRead;
            }
            int progress = (int) (100f * totalBytesRead / fullLength);
            Log.d(TAG, "download progress is " + progress);
            if (listener != null && progress != currentProgress) {
                listener.onProgress(progress);
            }
            if (listener != null && totalBytesRead == fullLength) {
                listener = null;
            }
            currentProgress = progress;
            return bytesRead;
        }
    }

}
```

　　首先，定义了一个 ProgressResponseBody 的构造方法，该构造方法中要求传入一个 url 参数和 ResponseBody 参数。那么很显然，url 参数就是图片的 url 地址了，而 ResponseBody 参数则是 OkHttp 拦截到的原始 ResponseBody 对象。然后再构造方法中，调用了 ProgressInterceptor 中的 LISTENER_MAP 来获取该 url 对应的监听器回调对象，有了这个对象，就可以回调计算出来的下载进度了。

　　由于继承了 ResponseBody 类之后一定要重写 contentType()、contentLength() 和 source() 这三个方法，在 contentType() 和 contentLength() 方法中直接就调用传入的原始 ResponseBody 的 contentType() 和 contentLength() 方法即可，这相当于一种委托模式。但是在 source() 方法中，就必须加入点自己的逻辑了，因为这里要设计到具体的下载进度计算。

　　source() 方法中先是调用了原始 ResponseBody 的 source() 方法来获取 Source 对象，接下来将这个 Source 对象封装到了一个 ProgressSource 对象当中，最终再用 Okio 的 buffer() 方法封装成 BufferedSource 对象返回。

　　ProgressSource 是一个自定义的继承自 ForwardingSource 的实现类。ForwardingSource 也是一个使用委托模式的工具，它部处理任何具体的逻辑，只是负责将传入的原始 Source 对象进行中转。但是，使用 ProgressSource 继承自 ForwardingSource，那么就可以在中转的过程中加入自己的逻辑了。

　　可以看到，在 ProgressSource 中重写了 read() 方法，然后在 read() 方法中获取该次读取到的字节数以及下载文件的总字节数，并进行一些简单的数学计算就能算出当前的下载进度了。这里先使用 Log 工具将算出的结果打印了一下，再通过前面获取到的回调监听器对象将结果进行回调。

### 4.5. 使用下载进度

　　现在计算下载进度的逻辑已经完成了，接下来就是在拦截器当中使用它。修改 ProgressInterceptor 中的代码，如下所示：

```java
public class ProgressInterceptor implements Interceptor { 

    ... 

    @Override 
    public Response intercept(Chain chain) throws IOException { 
        Request request = chain.request(); 
        Response response = chain.proceed(request); 
        String url = request.url().toString(); 
        ResponseBody body = response.body(); 
        Response newResponse = response.newBuilder().body(new ProgressResponseBody(url, body)).build();
        return newResponse; 
    } 

}
```

　　这里也都是一些 OkHttp 的简单用法。通过 Response 的 newBuilder() 方法来创建一个新的 Response 对象，并把它的 body 替换成刚才实现的 ProgressResponseBody，最终将新的 Response 对象进行返回，这样计算下载进度的逻辑就能生效了。

　　代码到这里，现在无论是加载任何网络上的图片，都应该是可以监听到它的下载进度的。

## 5. 进度显示

　　如果想要将下载进度显示在界面上，就需要未 ProgressInterceptor 添加下载回调。

```java
public void loadImage(View view) {
        ProgressInterceptor.addListener(url, new ProgressListener() {
            @Override
            public void onProgress(int progress) {
                progressDialog.setProgress(progress);
            }
        });
        Glide.with(this)
             .load(url)
             .diskCacheStrategy(DiskCacheStrategy.NONE)
             .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
             .into(new GlideDrawableImageViewTarget(image) {
                 @Override
                 public void onLoadStarted(Drawable placeholder) {
                     super.onLoadStarted(placeholder);
                     progressDialog.show();
                 }

                 @Override 
                 public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                     super.onResourceReady(resource, animation);
                     progressDialog.dismiss();
                     ProgressInterceptor.removeListener(url);
                 }
             });
    }
```

　　在这里新增了一个 ProgressDialog 用来显示下载进度，然后在 loadImage() 方法中，调用了 ProgressInterceptor.addListener() 方法去注册一个下载监听器，并在 onProgress() 回调方法中更新当前的下载进度。

　　最后，Glide 的 into() 方法也做了修改，这次是 into 到了一个 GlideDrawableImageViewTarget 当中。重写了它的 onLoadStarted() 方法和 onResourceReady() 方法，从而实现当图片开始加载的时候显示进度对话框，当图片加载完成时关闭进度对话框的功能。




## 6. 参考文章
1. [Android图片加载框架最全解析（七），实现带进度的Glide图片加载功能](https://blog.csdn.net/guolin_blog/article/details/78357251)




































