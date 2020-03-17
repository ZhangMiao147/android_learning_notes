# OkHttp



## 使用教程

### 1. Http Get

```java
//创建okHttpClient对象
OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
final Request request = new Request.Builder()
                .url("https://github.com/hongyangAndroid")
                .build();
//new call
Call call = mOkHttpClient.newCall(request); 
//请求加入调度
call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
            }

            @Override
            public void onResponse(final Response response) throws IOException
            {
                    //String htmlStr =  response.body().string();
            }
        });   
```

### 2. Http Post

```java
Request request = buildMultipartFormRequest(
        url, new File[]{file}, new String[]{fileKey}, null);
FormEncodingBuilder builder = new FormEncodingBuilder();   
builder.add("username","张鸿洋");

Request request = new Request.Builder()
                   .url(url)
                .post(builder.build())
                .build();
 mOkHttpClient.newCall(request).enqueue(new Callback(){});
```

　　post 的时候，参数是包含在请求体中的，通过 FormEncodingBuilder 添加多个 String 键值对，然后去构造 RequestBody，最后完成 Request 的构造。 

### 3. 基于 Http 的文件上传

```java
File file = new File(Environment.getExternalStorageDirectory(), "balabala.mp4");

RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

RequestBody requestBody = new MultipartBuilder()
     .type(MultipartBuilder.FORM)
     .addPart(Headers.of(
          "Content-Disposition", 
              "form-data; name=\"username\""), 
          RequestBody.create(null, "张鸿洋"))
     .addPart(Headers.of(
         "Content-Disposition", 
         "form-data; name=\"mFile\"; 
         filename=\"wjd.mp4\""), fileBody)
     .build();

Request request = new Request.Builder()
    .url("http://192.168.1.103:8080/okHttpServer/fileUpload")
    .post(requestBody)
    .build();

Call call = mOkHttpClient.newCall(request);
call.enqueue(new Callback()
{
    //...
});
```

　　通过 MultipartBuilder 的 addPart 方法可以添加键值对或者文件。

## OkHttp 请求网络的流程图

![](image/OkHttp流程图.png)

## 请求分析

### OkHttpClient 的构建

```java
OkHttpClient client = new OkHttpClient();
```

　　先看一下 okhttp 的构造函数 OkHttpClient() 和一些配置相关的：

```java
  public OkHttpClient() {
    this(new Builder());
  }
```

　　Builder 的配置：

```java
    public Builder() {
      dispatcher = new Dispatcher();
      protocols = DEFAULT_PROTOCOLS;
      connectionSpecs = DEFAULT_CONNECTION_SPECS;
      eventListenerFactory = EventListener.factory(EventListener.NONE);
      proxySelector = ProxySelector.getDefault();
      cookieJar = CookieJar.NO_COOKIES;
      socketFactory = SocketFactory.getDefault();
      hostnameVerifier = OkHostnameVerifier.INSTANCE;
      certificatePinner = CertificatePinner.DEFAULT;
      proxyAuthenticator = Authenticator.NONE;
      authenticator = Authenticator.NONE;
      connectionPool = new ConnectionPool();
      dns = Dns.SYSTEM;
      followSslRedirects = true;
      followRedirects = true;
      retryOnConnectionFailure = true;
      connectTimeout = 10_000;
      readTimeout = 10_000;
      writeTimeout = 10_000;
      pingInterval = 0;
    }

```

　　可以在 Builder 的构造方法里面看到，就算用户不设置 client 的配置，OkHttp 也会为配置设置默认值。

　　如果要设置 client 的配置，如添加拦截器等，则需要这样调用：

```java
  OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new logInterceptor()) //设置自定义拦截器
                .retryOnConnectionFailure(true) //连接失败是否重试
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS) //连接超时
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)//读取超时
                .build(); //生成 OkHttpClient() 对象，builder 作为其成员变量
```

　　OkHttpClient 的属性：

```java
  final Dispatcher dispatcher; //调度器
  final @Nullable Proxy proxy; //代理
  final List<Protocol> protocols; //协议
  final List<ConnectionSpec> connectionSpecs; //传输层版本和连接协议
  final List<Interceptor> interceptors; //拦截器
  final List<Interceptor> networkInterceptors; //网络拦截器
  final EventListener.Factory eventListenerFactory; 
  final ProxySelector proxySelector; //代理选择器
  final CookieJar cookieJar; //cookie
  final @Nullable Cache cache; //缓存
  final @Nullable InternalCache internalCache; //内部缓存
  final SocketFactory socketFactory; //socket 工厂
  final @Nullable SSLSocketFactory sslSocketFactory; //安全套层 socket 工厂，用于 https
  final @Nullable CertificateChainCleaner certificateChainCleaner; //验证确认
  final HostnameVerifier hostnameVerifier; //主机名字确认
  final CertificatePinner certificatePinner; //证书链
  final Authenticator proxyAuthenticator; //代理身份验证
  final Authenticator authenticator; //本地身份验证
  final ConnectionPool connectionPool; //连接池，复用连接
  final Dns dns; //域名
  final boolean followSslRedirects; //安全套接层重定向
  final boolean followRedirects; //本地重定向
  final boolean retryOnConnectionFailure;//重试连接失败
  final int connectTimeout; //连接超时
  final int readTimeout;//读取超时
  final int writeTimeout;//写入超时
```

### 请求网络

```java
        //构建请求
		Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            //请求
            response = client.newCall(request).execute();
            //获得请求结果
            String reponseMessage = response.body().string();
            Log.i("OkHttp", reponseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
```

　　client.newCall() 方法：

```java
  @Override public Call newCall(Request request) {
    return RealCall.newRealCall(this, request, false /* for web socket */);
  }
```

　　client.newCall() 方法里面调用的是 RealCall.newRealCall() 的方法，可以看出真正的请求交给了 RealCall 类，并且 RealCall 实现了 Call 接口的方法，RealCall 是真正的核心代码。

　　RealCall 主要方法：

* 同步请求：client.newCall(request).execute()；
* 异步请求：client.newCall(request).enqueue()；

### 异步请求

　　异步的请求调用的就是 enqueue() 方法了：

```java
  			client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("OkHttp", "onFailure e：" + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("OkHttp", "onResponse response：" + response);
                }
            });
```

　　从 RealCall 的 enqueue() 方法开始：

```java
  @Override public void enqueue(Callback responseCallback) {
    //同步，不能重复执行
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    captureCallStackTrace();
    eventListener.callStart(this);
    //交给 dispatcher 调度器进行调度
    client.dispatcher().enqueue(new AsyncCall(responseCallback));
  }
```

　　在 RealCall 的 enqueue() 的代码里，主要做了几件事：

1. synchronized(this) 确保每个 Call 只能被执行一次，不能重复执行，如果想要完全相同的 call，可以调用下面的方法克隆：

   ```java
     @SuppressWarnings("CloneDoesntCallSuperClone") // We are a final type & this saves clearing state.
     @Override public RealCall clone() {
       return RealCall.newRealCall(client, originalRequest, forWebSocket);
     }
   ```

2. 利用 dispatcher 调度器，来进行实际的执行 client.dispatcher().enqueue(new AsyncCall(responseCallback)); 在上面的 OkhttpClient.Build 可以看出已经初始化了 Dispatcher。

#### Dispatcher 调度器

　　Dispatcher 的 enqueue 的方法实现：

```java
  synchronized void enqueue(AsyncCall call) {
    // 同时请求不能超过并发数（64，可配置调度器调整）
    // okhttp 会使用共享主机 即地址相同的会共享 socket
    // 同一个 host 最多允许 5 条线程通知执行请求
    if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
      // 加入运行队列，并交给线程池执行
      runningAsyncCalls.add(call);
      //AysncCall 是一个 runnable，放到线程池中去执行，查看其 execute 实现
      executorService().execute(call);
    } else {
      // 加入等待队列
      readyAsyncCalls.add(call);
    }
  }
```

　　从 Dispatcher 的 enqueue 方法可以看出 Dispatcher 将 call 加入到队列中，然后通过线程池来执行 call。

　　Dispatcher 的几个属性和方法：

```java
public final class Dispatcher {
  // 同时能进行的最大请求数
  private int maxRequests = 64;
  // 同时请求的相同 HOST 的最大个数 SCHEME :// HOST [":" PORT ] [ PATH [ "?" QUERY ]]
  // 如 https://restqpi.amap.com restapi.amap.com - host
  private int maxRequestsPerHost = 5;
  
  /** Ready async calls in the order they'll be run. 
  * 双端队列，支持首尾两端 双向开口可进可出，方便移除
  * 异步等待队列
  */
  private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();

  /** Running asynchronous calls. Includes canceled calls that haven't finished yet. 
  * 正在进行的异步队列
  */
  private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();

}
```

　　很明显，OkHttp 可以进行多个并发网络请求，并且可以设置最大的请求数。

　　executorService() 方法只是创建了一个线程池：

```java
  public synchronized ExecutorService executorService() {
    if (executorService == null) {
      //线程池
      executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
    }
    return executorService;
  }
```

　　`readyAsyncCalls.add(call)`，前面是将 call 放入了线程池中运行，而这个 call 是 AysncCall。

#### AysncCall

　　来看一下 AsyncCall 的类结构：

```java
  final class AsyncCall extends NamedRunnable 
```

　　看一下 NameRunnable 的实现：

```java
/**
 * Runnable implementation which always sets its thread name.
 */
public abstract class NamedRunnable implements Runnable {
  protected final String name;

  public NamedRunnable(String format, Object... args) {
    this.name = Util.format(format, args);
  }

  @Override public final void run() {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(name);
    try {
      execute();
    } finally {
      Thread.currentThread().setName(oldName);
    }
  }

  protected abstract void execute();
}
```

　　NamedRunnable 实现了 Runnable，所以 AsyncCall 其实就是一个 Runnable，线程池执行到了 NamedRunnable 的 run 方法，会执行 AsyncCall 的 execute() 方法。

　　接着来看 AsyncCall 的 execute 方法：

```java
  final class AsyncCall extends NamedRunnable {
    private final Callback responseCallback;

    AsyncCall(Callback responseCallback) {
      super("OkHttp %s", redactedUrl());
      this.responseCallback = responseCallback;
    }

    String host() {
      return originalRequest.url().host();
    }

    Request request() {
      return originalRequest;
    }

    RealCall get() {
      return RealCall.this;
    }

    @Override protected void execute() {
      boolean signalledCallback = false;
      try {
        // 责任链模式
        // 拦截器链 执行请求
        Response response = getResponseWithInterceptorChain();
        // 回调结果
        if (retryAndFollowUpInterceptor.isCanceled()) {
          signalledCallback = true;
          responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
        } else {
          signalledCallback = true;
          responseCallback.onResponse(RealCall.this, response);
        }
      } catch (IOException e) {
        if (signalledCallback) {
          // Do not signal the callback twice!
          Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
        } else {
          eventListener.callFailed(RealCall.this, e);
          responseCallback.onFailure(RealCall.this, e);
        }
      } finally {
        // 移除队列
        client.dispatcher().finished(this);
      }
    }
  }
```

　　从 AsyncCall 的 execute 方法可以看出真正执行请求的是 getResponseWithInterceptorChain() ，然后通过回调将 Response 返回给用户。

　　值得注意的是 finally 执行了 client.dispatcher().finished(this)；通过调度器移除队列，并且判断是否存在等待队列，如果存在，检查执行队列是否达到最大值，如果没有将等待队列变为执行队列。这样也就确保了等待队列被执行。

```java
  /** Used by {@code AsyncCall#run} to signal completion. */
  void finished(AsyncCall call) {
    finished(runningAsyncCalls, call, true);
  }

  /** Used by {@code Call#execute} to signal completion. */
  void finished(RealCall call) {
    finished(runningSyncCalls, call, false);
  }

  private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
    int runningCallsCount;
    Runnable idleCallback;
    synchronized (this) {
      // calls 移除队列
      if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
      // 检查是否为异步请求，检查等候的队列 readyAsyncCalls，如果存在等候队列，则将等候队列加入执行队列
      if (promoteCalls) promoteCalls();
      // 运行队列的数量
      runningCallsCount = runningCallsCount();
      idleCallback = this.idleCallback;
    }
	// 闲置调用
    if (runningCallsCount == 0 && idleCallback != null) {
      idleCallback.run();
    }
  }

  private void promoteCalls() {
    // 检查运行队列与等待队列
    if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
    if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.
	// 将等待队列加入到运行队列中
    for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
      AsyncCall call = i.next();
	  // 相同 host 的请求没有达到最大，加入运行队列
      if (runningCallsForHost(call) < maxRequestsPerHost) {
        i.remove();
        runningAsyncCalls.add(call);
        executorService().execute(call);
      }

      if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
    }
  }
```

　　而真正执行网络请求和返回响应结果是在 getResponseWithInterceptorChain()：

```java
  //核心代码 开始真正的执行网络请求
  Response getResponseWithInterceptorChain() throws IOException {
    // Build a full stack of interceptors.
    // 责任链
    List<Interceptor> interceptors = new ArrayList<>();
    // 在配置 okhttpClient 时设置的 interceptor，就是用户自己设置的拦截器
    interceptors.addAll(client.interceptors());
    // 负责处理失败后的重试与重定向
    interceptors.add(retryAndFollowUpInterceptor);
    // 负责把用户构造的请求转换为发送到服务器的请求、把服务器返回的响应转换为用户优化的响应，处理、配置请求头等信息
    // 从应用程序代码到网络代码的桥梁。首先，它根据用户请求构建网络请求，然后它继续呼叫网络，最后，它根据网络响应构建用户响应。
    interceptors.add(new BridgeInterceptor(client.cookieJar()));
    // 处理缓存配置，根据条件（存在响应缓存并被设置为不变的或者响应在有效期内）返回缓存响应
    // 设置请求头（If-Node-Match、If-Modified-Since 等）服务器可能返回 304（未修改）
    // 可配置用户自己设置的缓存拦截器
    interceptors.add(new CacheInterceptor(client.internalCache()));
    // 连接服务器，
    interceptors.add(new ConnectInterceptor(client));
    if (!forWebSocket) {
      interceptors.addAll(client.networkInterceptors());
    }
    interceptors.add(new CallServerInterceptor(forWebSocket));

    Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
        originalRequest, this, eventListener, client.connectTimeoutMillis(),
        client.readTimeoutMillis(), client.writeTimeoutMillis());

    return chain.proceed(originalRequest);
  }
```





## 参考文章

[Android OkHttp完全解析 是时候来了解OkHttp了](https://blog.csdn.net/lmj623565791/article/details/47911083)

[彻底理解 OkHttp - OkHttp 源码解析及 OkHttp 的设计思想](https://www.jianshu.com/p/cb444f49a777)

[拆轮子系列：拆 OkHttp](https://blog.piasy.com/2016/07/11/Understand-OkHttp/index.html)

[OkHttp深入分析——基础认知部分](https://www.jianshu.com/p/b38bd9d1ae76)

[OkHttp深入分析——源码分析部分](https://www.jianshu.com/p/5bc1353ee933)