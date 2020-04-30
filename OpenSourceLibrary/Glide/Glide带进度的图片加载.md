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



## 5. 进度显示






## 参考文章
1. [Android图片加载框架最全解析（七），实现带进度的Glide图片加载功能](https://blog.csdn.net/guolin_blog/article/details/78357251)




































