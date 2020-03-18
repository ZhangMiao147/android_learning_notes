# OkHttp 源码分析

## 1. OkHttp 请求网络的流程图

![](image/OkHttp流程图.png)

## 2. 请求分析

### 2.1. OkHttpClient 的构建

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
      // 任务分发器
      dispatcher = new Dispatcher(); 
      // 支持的协议集，默认为 Http2 和 Http1.1
      protocols = DEFAULT_PROTOCOLS; 
      // 主要是针对 https 的 socket 连接的配置项，包括在协商安全连接时使用的 TLS 版本和密码套件
      connectionSpecs = DEFAULT_CONNECTION_SPECS; 
      // 创建 EventLinstener 的工厂方法类，重点在于 EventListener，通过 EventListener 我们可以得到更多关于 Http 通信的可度量数据，如数量、大小和持续时间，以便于我们更加详细的统计网络的状况。
      eventListenerFactory = EventListener.factory(EventListener.NONE);
      // 代理选择器，默认一般用系统的 DefaultProxySelector。
      proxySelector = ProxySelector.getDefault();
      // 定义了如何存储或者读取 cookies，如果不设置则不存储 cookie。
      cookieJar = CookieJar.NO_COOKIES;
      // SocketFactory 主要就是定义如何创建 socket，默认是 DefaultSocketFactory。 
      socketFactory = SocketFactory.getDefault();
      // HostnameVerifier 接口的实现，与证书校验相关。在握手期间，如果通信 URL 的主机名和服务器的标识主机名不匹配或者说不安全时，则底层的握手验证机制会回调 HostnameVerifier 接口的实现程序来确定是否应该允许此连接。
      hostnameVerifier = OkHostnameVerifier.INSTANCE;
      // 证书锁定，防止证书攻击。典型的用例时防止代理工具抓包。
      certificatePinner = CertificatePinner.DEFAULT;
      proxyAuthenticator = Authenticator.NONE;
      // 授权相关，如著名的 401 返回码。一般场景是在 token 过期的情况下发生，但在实际开发中，大部分服务器不会这样实现，而是正常返回在自定义码里面。
      authenticator = Authenticator.NONE;
      // 连接池
      connectionPool = new ConnectionPool();
      // DNS ，没有设置的话则为系统的 DNS 列表
      dns = Dns.SYSTEM;
   		// 是否允许 SSL 重定向
      followSslRedirects = true;
      // 是否允许重定向
      followRedirects = true;
      // 允许失败重连
      retryOnConnectionFailure = true;
      // 连接超时时间
      connectTimeout = 10_000;
      // 读取超时时间
      readTimeout = 10_000;
      // 写入超时时间
      writeTimeout = 10_000;
      // ping 的间隔时间
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
                .readTimeout(TIME_OUT, TimeUnit.SECONDS) //读取超时
                .build(); //生成 OkHttpClient() 对象，builder 作为其成员变量
```

　　OkHttpClient 的属性：

```java
  final Dispatcher dispatcher; //调度器
  final @Nullable Proxy proxy; //代理
  final List<Protocol> protocols; //协议
  final List<ConnectionSpec> connectionSpecs; //传输层版本和连接协议
  final List<Interceptor> interceptors; //拦截器
  final List<Interceptor> networkInterceptors; //网络拦截器，用户调用 addNetworkInterceptor 添加的拦截器
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

### 2.2. 请求网络

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

### 2.3. 异步请求

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
    //将 AsyncCall 实例交给 dispatcher 调度器进行调度
    client.dispatcher().enqueue(new AsyncCall(responseCallback));
  }
```

　　在 RealCall 的 enqueue() 的代码里，主要做了几件事：

1. synchronized(this) 确保每个 Call 只能被执行一次，不能重复执行，如果想要完全相同的 call，可以调用下面的方法克隆：

   ```java
     @SuppressWarnings("CloneDoesntCallSuperClone") 
     // We are a final type & this saves clearing state.
     @Override public RealCall clone() {
       return RealCall.newRealCall(client, originalRequest, forWebSocket);
     }
   ```

2. 利用 dispatcher 调度器，来进行实际的执行 client.dispatcher().enqueue(new AsyncCall(responseCallback));，dispatcher 是 OkHttpClient.Builder 的成员之一，是 HTTP 请求的执行策略， 在上面的 OkhttpClient.Build 可以看出已经初始化了 Dispatcher 实例。

#### 2.3.1 Dispatcher 调度器

　　Dispatcher 的 enqueue 的方法实现：

```java
  synchronized void enqueue(AsyncCall call) {
    // 同时请求不能超过并发数（64，可配置调度器调整）
    // okhttp 会使用共享主机 即地址相同的会共享 socket
    // 同一个 host 最多允许 5 条线程通知执行请求
    if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
      // 加入运行队列，并交给线程池执行
      runningAsyncCalls.add(call);
      // AysncCall 是一个 runnable，放到线程池中去执行
      executorService().execute(call);
    } else {
      // 加入等待队列
      readyAsyncCalls.add(call);
    }
  }
```

　　如果当前还能执行一个并发请求，将 call 加入到队列中，然后通过线程池来执行 call，否则加入 readyAsyncCalls 队列中。

　　Dispatcher 的几个属性和方法：

```java
public final class Dispatcher {
  // 同时能进行的最大请求数，默认是 64
  private int maxRequests = 64;
  // 同时请求的相同 HOST 的最大个数， SCHEME :// HOST [":" PORT ] [ PATH [ "?" QUERY ]]
  // 如 https://restqpi.amap.com， restapi.amap.com - host
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

#### 2.3.2 AysncCall

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
      // 调用了子类的 execute 方法，也就是 AsyncCall 的 execute 方法
      execute();
    } finally {
      Thread.currentThread().setName(oldName);
    }
  }

  protected abstract void execute();
}
```

　　NamedRunnable 实现了 Runnable，所以 AsyncCall 其实就是一个 Runnable，线程池执行到了 NamedRunnable 的 run 方法，会执行 AsyncCall 的 execute() 方法。

##### 2.3.2.1 execute

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
        // 调用拦截器链执行请求
        Response response = getResponseWithInterceptorChain();
        // 回调结果
        // 重试和重定向的拦截器已经关闭
        if (retryAndFollowUpInterceptor.isCanceled()) {
          // 返回请求失败的结果
          signalledCallback = true;
          responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
        } else {
          // 返回请求成功的结果，response 就是请求的结果
          signalledCallback = true;
          responseCallback.onResponse(RealCall.this, response);
        }
      } catch (IOException e) {
        // 请求抛出了异常
        if (signalledCallback) {
          // Do not signal the callback twice!
          Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
        } else {
          eventListener.callFailed(RealCall.this, e);
          responseCallback.onFailure(RealCall.this, e);
        }
      } finally {
        // 将当前的请求移除队列，如果还有等待请求，并且执行队列没有达到最大值，将等待队列变为执行队列，等待线程池的运行
        client.dispatcher().finished(this);
      }
    }
  }
```

　　从 AsyncCall 的 execute 方法可以看出真正执行请求的是 getResponseWithInterceptorChain() ，然后通过回调将 Response 返回给用户。

##### 2.3.2.2. dispatcher#finished()

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
      // idleCallback：也就是当 Dispatcher 中没有任何任务执行时，也就是进入了 idle 状态了，所执行的 runnable 类型的 Callback
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

　　正在执行的请求执行完毕了，会调用 promoteCalls() 函数，把 readyAsyncCalls 队列中的 AsyncCall “ 提升 ” 为 runningAsyncCalls，并等待线程池的执行。

##### 2.3.2.3. getResponseWithInterceptorChain

　　而真正执行网络请求和返回响应结果是在 getResponseWithInterceptorChain()：

```java
  //核心代码 开始真正的执行网络请求
  Response getResponseWithInterceptorChain() throws IOException {
    // Build a full stack of interceptors.
    // 责任链
    List<Interceptor> interceptors = new ArrayList<>();
    // 在配置 okhttpClient 时设置的 interceptor，就是用户自己设置的拦截器，addInterceptor
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
    // 连接服务器，负责和服务器建立连接，这里是真正的请求网络
    interceptors.add(new ConnectInterceptor(client));
    if (!forWebSocket) {
      // 配置 okhttpClient 时设置的 addNetworkInterceptions
      // 返回观察单个网络请求和响应的不可变拦截器列表
      
 interceptors.addAll(client.networkInterceptors());
    }
    // 执行流操作（写出请求体、获得响应数据）负责向服务器发送请求数据、从服务器读取响应数据
    // 进行 http 请求报文的封装与请求报文的解析
    interceptors.add(new CallServerInterceptor(forWebSocket));

    //创建责任链
    Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
        originalRequest, this, eventListener, client.connectTimeoutMillis(),
        client.readTimeoutMillis(), client.writeTimeoutMillis());
		// 执行责任链
    return chain.proceed(originalRequest);
  }
```

#### 2.3.3. RealInterceptorChain

　　okhttp 是通过责任链来进行传递返回数据的。

　　在 getResponseWithInterceptorChain() 的方法里通过调用 chain.proceed() 方法来执行责任链，而 chain 是 RealInterceptorChain 实例，接下来看 chain.proceed() 方法：

```java
  @Override public Response proceed(Request request) throws IOException {
    return proceed(request, streamAllocation, httpCodec, connection);
  }

  public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec,
      RealConnection connection) throws IOException {
    if (index >= interceptors.size()) throw new AssertionError();
		
    calls++;

    // If we already have a stream, confirm that the incoming request will use it.
    if (this.httpCodec != null && !this.connection.supportsUrl(request.url())) {
      throw new IllegalStateException("network interceptor " + interceptors.get(index - 1)
          + " must retain the same host and port");
    }

    // If we already have a stream, confirm that this is the only call to chain.proceed().
    if (this.httpCodec != null && calls > 1) {
      throw new IllegalStateException("network interceptor " + interceptors.get(index - 1)
          + " must call proceed() exactly once");
    }

    // Call the next interceptor in the chain.
    // 创建新的拦截链，是链中的下一个拦截器
    RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation, httpCodec,
        connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,
        writeTimeout);
    // 执行当前的拦截器 - 如果在配置 okHttpClient 时没有设置 intercept 模式时先执行 retryAndFollowUpInterceptor 拦截器
    Interceptor interceptor = interceptors.get(index);
    // 执行当前拦截器，并获得处理的结果，将下一个拦截器传递了过去
    Response response = interceptor.intercept(next);

    // Confirm that the next interceptor made its required call to chain.proceed().
    if (httpCodec != null && index + 1 < interceptors.size() && next.calls != 1) {
      throw new IllegalStateException("network interceptor " + interceptor
          + " must call proceed() exactly once");
    }

    // Confirm that the intercepted response isn't null.
    if (response == null) {
      throw new NullPointerException("interceptor " + interceptor + " returned null");
    }

    if (response.body() == null) {
      throw new IllegalStateException(
          "interceptor " + interceptor + " returned a response with no body");
    }
		// 返回当前拦截器执行完的结果
    return response;
  }
```

　　在 RealInterceptorChain 的 proceed 方法里面，用 index +1 新建了一个 RealInterceptorChain，也就是下一个拦截器，然后获得 index （当前）的拦截器 interceptor，最后执行 interceptor.intercept(next) 方法，这个方法的参数是新建的 RealInterceptorChain（下一个拦截器），其实就会按顺序执行拦截器，最终将结果返回到最开始调用的这里，将最终响应结果返回。 

　　这样设计的一个好处就是，责任链中每个拦截器都会执行 chain.proceed() 方法之前的代码，等责任链最后一个拦截器执行完毕后会返回最终的响应数据，而 chain.proceed() 方法会得到最终的响应数据，这时就会执行每个拦截器的 chain.proceed() 方法之后的代码，其实就是对响应数据的一些操作。

#### 2.3.4. Interceptor 拦截器

　　OkHttp 里面的拦截器有：

1. 在配置 OkHttpClient 时设置的 interceptors（addInterceptor）。
2. 负责失败重试以及重定向的 RetryAndFollowUpInterceptor。
3. 负责把用户构造的请求转换为发送服务器的请求、把服务器返回的响应转换为用户友好的响应的 BridgeInterceptor。
4. 负责读取缓存直接返回、更新缓存的 CacheInterceptor。
5. 负责和服务器建立连接的 ConnectInterceptor。
6. 配置 OkHttpClient 时设置的 networkInterceptors（addNetworkInterceptor）。
7. 负责向服务器发送请求数据、从服务器读取响应数据的 CallServerInterceptor。

　　位置决定了功能，最后一个 CallServerInterceptor 负责和服务器实际通讯，重定向、缓存等一定是在实际通讯之前的。

　　对于把 Request 变成 Response 这件事来说，每个 Interceptor 都可能完成这件事，链条让每一个 Interceptor 自行决定是否完成任务以及怎么完成任务（自己解决或者交给下一个 Interveptor）。这样完成网络请求这件事就彻底从 RealCall 类中剥离了出来，简化了各自的责任和逻辑。

##### 3.2.4.1. RetryAndFollowUpInterceptor 重试以及重定向

　　RetryAndFollowUpInterceptor 的 intercept 方法：

```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 1. 获取请求信息
    Request request = chain.request();
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    Call call = realChain.call();
    EventListener eventListener = realChain.eventListener();

    StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
        createAddress(request.url()), call, eventListener, callStackTrace);
    this.streamAllocation = streamAllocation;

    int followUpCount = 0;
    Response priorResponse = null;
    // 开启循环，请求完成或者遇到异常会退出
    while (true) {
      if (canceled) {
        streamAllocation.release();
        throw new IOException("Canceled");
      }

      Response response;
      boolean releaseConnection = true;
      try {
        // 2. 调用下一个责任链，并得到返回的结果
        // 执行责任链上的任务，获得请求网络后的响应
        // 请求失败后，会再次进入循环，再次请求
        response = realChain.proceed(request, streamAllocation, null, null);
        releaseConnection = false;
      } catch (RouteException e) {
        // The attempt to connect via a route failed. The request will not have been sent.
        if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
          throw e.getLastConnectException();
        }
        releaseConnection = false;
        continue;
      } catch (IOException e) {
        // An attempt to communicate with a server failed. The request may have been sent.
        boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
        if (!recover(e, streamAllocation, requestSendStarted, request)) throw e;
        releaseConnection = false;
        continue;
      } finally {
        // We're throwing an unchecked exception. Release any resources.
        if (releaseConnection) {
          streamAllocation.streamFailed(null);
          streamAllocation.release();
        }
      }
			// 3. 对返回的结果进行处理，重试或者重定向
      // Attach the prior response if it exists. Such responses never have a body.
      // 第一次进入时 priorResponse 是 null，在执行了后面的分析请求网络的响应后，priorResponse 就不会空了，为请求网络后的响应
      if (priorResponse != null) {
        response = response.newBuilder()
            .priorResponse(priorResponse.newBuilder()
                    .body(null)
                    .build())
            .build();
      }
	    // 判断是否需要重定向，如果需要，会对 request(请求)做一些修改，返回新的 request，如果不需要，则返回 null
      Request followUp = followUpRequest(response, streamAllocation.route());
	    // 不需要重定向，也就是请求数据成功，释放资源，返回响应结果。
      if (followUp == null) {
        if (!forWebSocket) {
          streamAllocation.release();
        }
        return response;
      }

      closeQuietly(response.body());
	   // 重复请求超过 20 次，就抛出异常，停止重试
      if (++followUpCount > MAX_FOLLOW_UPS) {
        streamAllocation.release();
        throw new ProtocolException("Too many follow-up requests: " + followUpCount);
      }

      if (followUp.body() instanceof UnrepeatableRequestBody) {
        streamAllocation.release();
        throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
      }
	   // 比较重定向前后的地址 url、host、scheme 是否一致，一致的话重用，否则重新创建
      if (!sameConnection(response, followUp.url())) {
        streamAllocation.release();
        streamAllocation = new StreamAllocation(client.connectionPool(),
            createAddress(followUp.url()), call, eventListener, callStackTrace);
        this.streamAllocation = streamAllocation;
      } else if (streamAllocation.codec() != null) {
        throw new IllegalStateException("Closing the body of " + response
            + " didn't close its backing stream. Bad interceptor?");
      }

      request = followUp;
      // priorResponse 设置为网络请求后的响应结果
      priorResponse = response;
    }
  }

  /**
   * Figures out the HTTP request to make in response to receiving {@code userResponse}. This will
   * either add authentication headers, follow redirects or handle a client request timeout. If a
   * follow-up is either unnecessary or not applicable, this returns null.
   */
  // 根据响应的结果，判断是否请求成功，请求不成功是否需要重试或者重定向，
  private Request followUpRequest(Response userResponse, Route route) throws IOException {
    if (userResponse == null) throw new IllegalStateException();
    int responseCode = userResponse.code();

    final String method = userResponse.request().method();
    switch (responseCode) {
      case HTTP_PROXY_AUTH:
        Proxy selectedProxy = route != null
            ? route.proxy()
            : client.proxy();
        if (selectedProxy.type() != Proxy.Type.HTTP) {
          throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
        }
        return client.proxyAuthenticator().authenticate(route, userResponse);

      case HTTP_UNAUTHORIZED:
        return client.authenticator().authenticate(route, userResponse);

      case HTTP_PERM_REDIRECT://307
      case HTTP_TEMP_REDIRECT://308
        // "If the 307 or 308 status code is received in response to a request other than GET
        // or HEAD, the user agent MUST NOT automatically redirect the request"
        if (!method.equals("GET") && !method.equals("HEAD")) {
          return null;
        }
        // fall-through
      case HTTP_MULT_CHOICE://300
      case HTTP_MOVED_PERM://301
      case HTTP_MOVED_TEMP://302
      case HTTP_SEE_OTHER://303
        // Does the client allow redirects?
        // 判断是否允许重定向，默认允许
        if (!client.followRedirects()) return null;
				// 重定向地址
        String location = userResponse.header("Location");
        if (location == null) return null;
        HttpUrl url = userResponse.request().url().resolve(location);

        // Don't follow redirects to unsupported protocols.
        if (url == null) return null;

        // If configured, don't follow redirects between SSL and non-SSL.
        boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
        if (!sameScheme && !client.followSslRedirects()) return null;

        // Most redirects don't include a request body.
        Request.Builder requestBuilder = userResponse.request().newBuilder();
        // 判断请求方式，将除了 PROPFIND 的重定向请求转为 GET 请求，请求方式若为 PROPFIND，则保持原来的 method 和 requestBody
        if (HttpMethod.permitsRequestBody(method)) {
          final boolean maintainBody = HttpMethod.redirectsWithBody(method);
          if (HttpMethod.redirectsToGet(method)) {
            requestBuilder.method("GET", null);
          } else {
            RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
            requestBuilder.method(method, requestBody);
          }
          if (!maintainBody) {
            requestBuilder.removeHeader("Transfer-Encoding");
            requestBuilder.removeHeader("Content-Length");
            requestBuilder.removeHeader("Content-Type");
          }
        }

        // When redirecting across hosts, drop all authentication headers. This
        // is potentially annoying to the application layer since they have no
        // way to retain them.
        if (!sameConnection(userResponse, url)) {
          requestBuilder.removeHeader("Authorization");
        }

        return requestBuilder.url(url).build();

      case HTTP_CLIENT_TIMEOUT:
        // 408's are rare in practice, but some servers like HAProxy use this response code. The
        // spec says that we may repeat the request without modifications. Modern browsers also
        // repeat the request (even non-idempotent ones.)
        if (!client.retryOnConnectionFailure()) {
          // The application layer has directed us not to retry the request.
          return null;
        }

        if (userResponse.request().body() instanceof UnrepeatableRequestBody) {
          return null;
        }

        if (userResponse.priorResponse() != null
            && userResponse.priorResponse().code() == HTTP_CLIENT_TIMEOUT) {
          // We attempted to retry and got another timeout. Give up.
          return null;
        }

        if (retryAfter(userResponse, 0) > 0) {
          return null;
        }

        return userResponse.request();

      case HTTP_UNAVAILABLE:
        if (userResponse.priorResponse() != null
            && userResponse.priorResponse().code() == HTTP_UNAVAILABLE) {
          // We attempted to retry and got another timeout. Give up.
          return null;
        }

        if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
          // specifically received an instruction to retry without delay
          return userResponse.request();
        }

        return null;

      default:
        // 返回 null 说明不需要重试或重定向
        return null;
    }
  }
```

　　RetryAndFollowUpInterceptor 的 intercept 方法主要是用了一个循环体来控制，如果请求没有成功就会再次执行 chain.process() 方法再次运行起 InterceptorChain，而循环的次数被 MAX_FOLLOW_UPS 限制，其默认大小是 20。

　　是否请求成功是通过 followUpRequest() 方法来进行的，通过 response code 来进行一系列的异常判断，从而决定是否要重新请求。返回为 null，则表示请求成功或者准确的说是不需要重试或者重定向，否则就是请求失败了需要重新请求。

##### 3.2.4.2. BridgeInterceptor 桥接

　　BridgeInterceptor 的 intercept 方法：

```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 1. 获取请求信息，依据 Http 配置请求头
    Request userRequest = chain.request();
    Request.Builder requestBuilder = userRequest.newBuilder();
	  // 处理 body，如果有的话
    RequestBody body = userRequest.body();
    if (body != null) {
      MediaType contentType = body.contentType();
      if (contentType != null) {
        requestBuilder.header("Content-Type", contentType.toString());
      }

      long contentLength = body.contentLength();
      if (contentLength != -1) {
        requestBuilder.header("Content-Length", Long.toString(contentLength));
        requestBuilder.removeHeader("Transfer-Encoding");
      } else {
        requestBuilder.header("Transfer-Encoding", "chunked");
        requestBuilder.removeHeader("Content-Length");
      }
    }
		// 处理 host
    if (userRequest.header("Host") == null) {
      requestBuilder.header("Host", hostHeader(userRequest.url(), false));
    }
		// 处理 Connection
    if (userRequest.header("Connection") == null) {
      requestBuilder.header("Connection", "Keep-Alive");
    }

    // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
    // the transfer stream.
    boolean transparentGzip = false;
    // 请求以 GZip 来压缩
    if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
      transparentGzip = true;
      requestBuilder.header("Accept-Encoding", "gzip");
    }
		// 处理 Cookies，如果有的话
    List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
    if (!cookies.isEmpty()) {
      requestBuilder.header("Cookie", cookieHeader(cookies));
    }
		// 处理 UA
    if (userRequest.header("User-Agent") == null) {
      requestBuilder.header("User-Agent", Version.userAgent());
    }
    // 2. 调用下一个责任链，并得到返回的结果
		// 调用 proceed() 等待 Response 返回
    Response networkResponse = chain.proceed(requestBuilder.build());
		// 3. 对返回的结果进行处理，将其构建成响应结果
    HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());
		// 构建响应结果
    Response.Builder responseBuilder = networkResponse.newBuilder()
        .request(userRequest);

    if (transparentGzip
        && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))
        && HttpHeaders.hasBody(networkResponse)) {
      GzipSource responseBody = new GzipSource(networkResponse.body().source());
      Headers strippedHeaders = networkResponse.headers().newBuilder()
          .removeAll("Content-Encoding")
          .removeAll("Content-Length")
          .build();
      responseBuilder.headers(strippedHeaders);
      String contentType = networkResponse.header("Content-Type");
      responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));
    }

    return responseBuilder.build();
  }
```

　　BridgeInterceptor 用于桥接 request 和 response，主要就是依据 Http 协议配置请求头，然后通过 chain.proceed() 发出请求，待结果返回后再构建响应结果。

##### 3.2.4.3. CacheInterceptor 缓存

　　CacheInterceptor 的 intercept 方法：

```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 1. 没有网络情况下处理获取缓存
    // 获取 request 对应缓存的 Response，如果用户没有配置缓存拦截器，则 cacheCandidate == null
    // 这里的 cache 就是 client.internalCache()，也就是在使用时 OkHttpClient build 的时候调用 cache 方法传入的 cache
    Response cacheCandidate = cache != null
        ? cache.get(chain.request())
        : null;
		// 执行响应缓存策略
    long now = System.currentTimeMillis();

    CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
    // 如果 networkRequest == null 则说明不使用网络请求
    Request networkRequest = strategy.networkRequest;
    // 获取缓存中（CacheStrategy）的 response
    Response cacheResponse = strategy.cacheResponse;

    if (cache != null) {
      cache.trackResponse(strategy);
    }
		// 缓存无效，关闭资源
    if (cacheCandidate != null && cacheResponse == null) {
      closeQuietly(cacheCandidate.body()); // The cache candidate wasn't applicable. Close it.
    }

    // If we're forbidden from using the network and the cache is insufficient, fail.
    // networkRquest == null 不使用网络请求，且没有缓存 cacheResponse == null，则返回响应失败的结果
    if (networkRequest == null && cacheResponse == null) {
      return new Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(504)
          .message("Unsatisfiable Request (only-if-cached)")
          .body(Util.EMPTY_RESPONSE)
          .sentRequestAtMillis(-1L)
          .receivedResponseAtMillis(System.currentTimeMillis())
          .build();
    }

    // If we don't need the network, we're done.
    // 不使用网络，且存在缓存，则直接返回响应
    if (networkRequest == null) {
      return cacheResponse.newBuilder()
          .cacheResponse(stripBody(cacheResponse))
          .build();
    }
		// 2. 有网络的时候的处理
    // 执行下一个拦截器
    Response networkResponse = null;
    try {
      networkResponse = chain.proceed(networkRequest);
    } finally {
      // If we're crashing on I/O or otherwise, don't leak the cache body.
      if (networkResponse == null && cacheCandidate != null) {
        closeQuietly(cacheCandidate.body());
      }
    }
		// 网络请求返回，更新缓存
    // If we have a cache response too, then we're doing a conditional get.
    // 如果存在缓存，则更新
    if (cacheResponse != null) {
      // 304 响应码，自从上次请求后，请求需要响应的内容未发生改变
      if (networkResponse.code() == HTTP_NOT_MODIFIED) {
        Response response = cacheResponse.newBuilder()
            .headers(combine(cacheResponse.headers(), networkResponse.headers()))
            .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
            .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
            .cacheResponse(stripBody(cacheResponse))
            .networkResponse(stripBody(networkResponse))
            .build();
        networkResponse.body().close();

        // Update the cache after combining headers but before stripping the
        // Content-Encoding header (as performed by initContentStream()).
        cache.trackConditionalCacheHit();
        cache.update(cacheResponse, response);
        return response;
      } else {
        closeQuietly(cacheResponse.body());
      }
    }
		// 缓存 response
    Response response = networkResponse.newBuilder()
        .cacheResponse(stripBody(cacheResponse))
        .networkResponse(stripBody(networkResponse))
        .build();

    if (cache != null) {
      if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
        // Offer this request to the cache.
        CacheRequest cacheRequest = cache.put(response);
        return cacheWritingResponse(cacheRequest, response);
      }

      if (HttpMethod.invalidatesCache(networkRequest.method())) {
        try {
          cache.remove(networkRequest);
        } catch (IOException ignored) {
          // The cache cannot be written.
        }
      }
    }

    return response;
  }
```

　　CacheInterceptor 的 intercept 方法可以分为两部分：

* 在无网络的情况下如何处理获取缓存
  1. 如果用户自己设置了缓存拦截器，cacheCandidate = cache.Response 获取用户自己存储的 Response，否则 cacheCandidate = null，同时从 CacheStrategy 获取 cacheResponse 和 networkRequest。
  2. 如果 cacheCandidate != null 而 cacheResponse == null 说明缓存无效，清除 cacheCandidate 缓存。
  3. 如果 networkRequest == null 说明没有网络，cacheResponse == null 说明没有缓存，在这种情况下，返回失败的信息，责任链此时也就终止（责任链上还剩下 ConnectInterceptor、client.networkInterceptors、CallServerInterceptor），不会再往下继续执行。
  4. 如果 networkRequest == null 说明没有网络，但是 cacheReponse != null 说明有缓存，在这种情况下，返回缓存的信息，责任链此时也就终止（责任链上还剩下 ConnectInterceptor、 client.networkInterceptors、 CallServerInterceptor），不会再往下继续执行。
* 在有网络的情况下如何处理获取缓存
  1. 执行下一个拦截器，也就是请求网络。
  2. 责任链执行完毕后，会返回最终响应数据，如果缓存存在，则更新缓存，如果缓存不存在，则加入到缓存中去。

　　OkHttp 的缓存是封装了一个 Cache 类来实现具体的缓存逻辑，它利用 DiskLruCache，用磁盘上的有限大小空间进行缓存，按照 LRU 算法进行缓存淘汰。

　　这样就体现除了责任链的好处，当责任链执行完毕，拦截器可以拿到最终的数据做其他的逻辑处理等操作，也不用再做其他的调用方法逻辑了，就可以直接在当前的拦截器拿到最终的数据。

##### 3.2.4.4. ConnectInterceptor 建立连接

　　ConnectInterceptor 的 intercept 方法：

```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 1. 获取请求信息
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    Request request = realChain.request();
    // 找到一个了用的 RealConnection
    StreamAllocation streamAllocation = realChain.streamAllocation();

    // We need the network to satisfy this request. Possibly for validating a conditional GET.
    boolean doExtensiveHealthChecks = !request.method().equals("GET");
    HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
    RealConnection connection = streamAllocation.connection();
		
    //2. 执行下一个拦截器并返回结果
    return realChain.proceed(request, streamAllocation, httpCodec, connection);
  }
```

　　实际上建立连接就是创建了一个 HttpCodec 对象，它是对 HTTP 协议操作的抽象，有两个实现：Http1Codec 和 Http2Codec，它们分别对应 HTTP/1.1 和 HTTP/2 版本的实现。

　　在 Http1Codec 中，它利用 Okio 对 Socket 的读写操作进行封装，也就是 Okio 对 java.io 和 hava.nio 进行了封装，让用户更便捷高效的进行 IO 操作。

　　而创建 HttpCodec 对象的过程涉及了 StreamAllocation、RealConnection，这个过程可以概括为：找到一个可用的 RealConnection，再利用 RealConnection 的输入、输出（BufferedSource 和 BufferedSink）创建 HttpCodec 对象，供后面步骤使用。

##### 3.2.4.5. CallServerInterceptor 发送和接收数据

　　CallServerInterceptor 的 intercept 方法：

```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 1. 获取请求信息
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    HttpCodec httpCodec = realChain.httpStream();
    StreamAllocation streamAllocation = realChain.streamAllocation();
    // 可用的 RealConnection，
    RealConnection connection = (RealConnection) realChain.connection();
    Request request = realChain.request();

    long sentRequestMillis = System.currentTimeMillis();

    realChain.eventListener().requestHeadersStart(realChain.call());
    // 写入申请头
    httpCodec.writeRequestHeaders(request);
    realChain.eventListener().requestHeadersEnd(realChain.call(), request);

    Response.Builder responseBuilder = null;
    if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
      // If there's a "Expect: 100-continue" header on the request, wait for a "HTTP/1.1 100
      // Continue" response before transmitting the request body. If we don't get that, return
      // what we did get (such as a 4xx response) without ever transmitting the request body.
      if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
        httpCodec.flushRequest();
        realChain.eventListener().responseHeadersStart(realChain.call());
        responseBuilder = httpCodec.readResponseHeaders(true);
      }

      if (responseBuilder == null) {
        // Write the request body if the "Expect: 100-continue" expectation was met.
        realChain.eventListener().requestBodyStart(realChain.call());
        long contentLength = request.body().contentLength();
        CountingSink requestBodyOut =
            new CountingSink(httpCodec.createRequestBody(request, contentLength));
        // 如果需要写入 request body 的话，则写入
        BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);

        request.body().writeTo(bufferedRequestBody);
        bufferedRequestBody.close();
        realChain.eventListener()
            .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
      } else if (!connection.isMultiplexed()) {
        // If the "Expect: 100-continue" expectation wasn't met, prevent the HTTP/1 connection
        // from being reused. Otherwise we're still obligated to transmit the request body to
        // leave the connection in a consistent state.
        streamAllocation.noNewStreams();
      }
    }
		// 完成请求的写入
    httpCodec.finishRequest();

    if (responseBuilder == null) {
      realChain.eventListener().responseHeadersStart(realChain.call());
      // 获取并构造 response
      responseBuilder = httpCodec.readResponseHeaders(false);
    }

    // 请求
    Response response = responseBuilder
        .request(request)
        .handshake(streamAllocation.connection().handshake())
        .sentRequestAtMillis(sentRequestMillis)
        .receivedResponseAtMillis(System.currentTimeMillis())
        .build();

    // 请求返回码
    int code = response.code();
    if (code == 100) {
      // server sent a 100-continue even though we did not request one.
      // try again to read the actual response
      responseBuilder = httpCodec.readResponseHeaders(false);

      response = responseBuilder
              .request(request)
              .handshake(streamAllocation.connection().handshake())
              .sentRequestAtMillis(sentRequestMillis)
              .receivedResponseAtMillis(System.currentTimeMillis())
              .build();

      code = response.code();
    }

    realChain.eventListener()
            .responseHeadersEnd(realChain.call(), response);

    if (forWebSocket && code == 101) {
      // Connection is upgrading, but we need to ensure interceptors see a non-null response body.
      response = response.newBuilder()
          .body(Util.EMPTY_RESPONSE)
          .build();
    } else {
      response = response.newBuilder()
          .body(httpCodec.openResponseBody(response))
          .build();
    }

    if ("close".equalsIgnoreCase(response.request().header("Connection"))
        || "close".equalsIgnoreCase(response.header("Connection"))) {
      streamAllocation.noNewStreams();
    }

    if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
      throw new ProtocolException(
          "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
    }

    return response;
  }
```

　　主要部分：

1. 向服务器发送 request header。
2. 如果有 request body，就向服务器发送。
3. 读取 response header，先构建一个 Response 对象。
4. 如果有 response body，就在上一步的基础上加上 body 构造一个新的 Response 对象。

　　核心的工作都是由 HttpCodec 执行完成，而 HttpCodec 实际上利用的是 Okio，而 Okio 实际上还是用的 Socket。

　　其实 Interceptor 的设计也是一种分层的思想，每个 Interceptor 就是一层。为什么要套这么多层呢？分层的思想在 TCP/IP 协议中就体现的淋漓尽致，分层简化了每一层的逻辑，每层只需要关注自己的责任（单一原则思想也在此体现），而各层之间通过约定的接口 / 协议进行合作（面向接口编程思想），共同完成复杂的任务。

### 2.4. 同步请求

　　OkHttp 的同步请求调用的是 RealCall 的 execute() 方法：

```java
// 同步执行请求，直接返回一个请求的结果  
@Override public Response execute() throws IOException {
   	// 避免重复执行
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    captureCallStackTrace();
    // 调用监听的开始方法
    eventListener.callStart(this);
    try {
      // 交给调度器去执行
      client.dispatcher().executed(this);
      // 获取请求的返回数据
      Response result = getResponseWithInterceptorChain();
      if (result == null) throw new IOException("Canceled");
      return result;
    } catch (IOException e) {
      eventListener.callFailed(this, e);
      throw e;
    } finally {
      // 执行调度器的完成方法，移除队列
      client.dispatcher().finished(this);
    }
  }
```

　　同步请求主要做了几件事：

1. synchronized (this) 避免重复执行。

2. client.dispatcher().executed(this)，实际上调度器只是将 call 加入到同步执行队列中。

   ```java
     /** Used by {@code Call#execute} to signal it is in-flight. */
     synchronized void executed(RealCall call) {
       runningSyncCalls.add(call);
     }
   ```

3. 调用 getResponseWithInterceptorChain() 执行责任链，请求网络得到响应数据，返回给用户。

4. client.dispatcher().finished(this) 执行调度器的完成方法，移除队列。

　　同步请求和异步请求的原理是一样的，都是在 getResponseWithInterceptorChain() 函数中通过 Interceptor 链条来实现的网络逻辑，只是异步是通过 ExectorService （线程池）实现。

## 3. 返回数据的获取

　　在同步（Call # execute() 执行之后）或者异步（Call # onResponse() 回调中）请求完成之后，就可以从 Response 对象中获取到响应数据了，包括 Http status code、status message、response header、response body 等。这里 body 部分最为特殊，因为服务器返回的数据可能非常大，所以必须通过数据流的方式来进行访问（也提供了诸如 string() 和 bytes() 这样的方法将流内的数据一次性读取完毕），而响应中其他部分则可以随意获取。

　　响应 body 被封装到 ResponseBody 类中，该类主要有两点需要注意：

1. 每个 body 只能被消费一次，多次消费会抛出异常。
2. body 必须被关闭，否则会发生资源泄漏。

　　在 CallServerInterceptor 的 interceptor 方法中：

```java
      response = response.newBuilder()
          .body(httpCodec.openResponseBody(response))
          .build();
```

　　由 HttpCodec#openResponseBody 提供具体 HTTP 协议版本的响应 body，而 HttpCodec 则是利用 Okio 实现具体的数据 IO 操作。

　　OkHttp 对响应的校验非常严格，Http status line 不能有任何杂乱的数据，否则就会抛出异常。

## 4. 总结

　　简述 OkHttp 的执行流程：

1. OkHttpClient 实现了 Call.Factory，负责为 Request 创建 call；
2. RealCall 为 Call 的具体实现，其  enqueue() 异步请求接口通过 Dispatcher() 调度器利用 ExcutorService 实现，而最终进行网络请求时和同步的 execute() 接口一致，都是通过 getResponseWithInterceptorChain() 函数实现。
3. getResponseWithInterceptorChain() 中利用 Interceptor 链条，分层实现缓存、透明压缩、网络 IO 等功能，最终将响应数据返回给用户。
4. OkHttp 的实现采用了责任链模式，它包含了一些命令对象和一系列的处理对象，每一个处理对象决定它能处理哪些命令对象，它也知道如何将它不能处理的命令对象传递给该链中的下一个处理对象，该模式还描述了往该处理链的末尾添加新的处理对象的方法。

## 5. 参考文章

[彻底理解 OkHttp - OkHttp 源码解析及 OkHttp 的设计思想](https://www.jianshu.com/p/cb444f49a777)

[拆轮子系列：拆 OkHttp](https://blog.piasy.com/2016/07/11/Understand-OkHttp/index.html)

[OkHttp深入分析——源码分析部分](https://www.jianshu.com/p/5bc1353ee933)