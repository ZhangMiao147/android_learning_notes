# Retrofit 源码分析

　　Retrofit 的主要原理是利用了 Java 的动态代理技术，把 ApiService 的方法调用集中到了 InvocationHandler.invoke，在构建了 ServiceMethod 、OkHttpClient，返回 callAdapter.adapter 的结果。

　　Retrofit 的最大特点就是解耦。

## 基本使用

### 引入库

```ruby
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'io.reactivex:rxjava:1.1.6'
    api 'com.squareup.retrofit2:adapter-rxjava:2.3.0'
```

### 创建 Retrofit 对象

```java
OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
// 创建 Retrofit 对象，外观模式
Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test")
		.addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();
```

### 定义 API 

```java
public interface ApiService {
    @GET("data/")
    Observable<BaseResponse> getMessage(@Path("page") int page);

    public static class BaseResponse {

        /**
         * 业务错误码
         */
        @SerializedName("F_responseNo")
        int responseNo;

        /**
         * 业务错误描述
         */
        @SerializedName("F_responseMsg")
        String responseMsg;

        public int getResponseNo() {
            return responseNo;
        }

        public void setResponseNo(int responseNo) {
            this.responseNo = responseNo;
        }

        public String getResponseMsg() {
            return responseMsg;
        }

        public void setResponseMsg(String responseMsg) {
            this.responseMsg = responseMsg;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }

        @Override
        public String toString() {
            return "BaseResponse{" +
                    "responseNo=" + responseNo +
                    ", responseMsg='" + responseMsg + '\'' +
                    '}';
        }
    }
}
```

### 获取 API 实例

```java
ApiService service = retrofit.create(ApiService.class);
Observable<ApiService.BaseResponse> observable = service.getMessage(1);
...
```

　　Retrofit 就这样经过简单的配置后就可以向服务器请求数据了，超级简单。

## 分析

### Retrofit.create 方法（创建 API实例）分析

　　Retrofit 的 create 方法作为 Retrofit 的入口。

```java
  public <T> T create(final Class<T> service) {
    // 验证接口是否合理
    Utils.validateServiceInterface(service);
    // 默认 false
    if (validateEagerly) {
      eagerlyValidateMethods(service);
    }
    // 动态代理
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          // 平台的抽象，指定默认的 CallbackExecutor CallAdapterFactory 用，这里 Android 平台是 Android（还有 Java8 和 ios）
          private final Platform platform = Platform.get();
		  // ApiService 中的方法调用会走到这里
          @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args)
              throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            // Object 的方法不管
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            // java8 的默认方法，Android 暂不支持默认方法，所以暂时也需要管
            if (platform.isDefaultMethod(method)) {
              return platform.invokeDefaultMethod(method, service, proxy, args);
            }
            //重点
            // 为 Method 生成一个 ServiceMethod
            ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
            // 再包装成 OkHttpCall
            OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args); // 请求
            return serviceMethod.callAdapter.adapt(okHttpCall);
          }
        });
  }
```

　　从 create 方法中可以看出，Retrofit 的主要原理是利用了 Java 的**动态代理**技术创建了 API 实例，把 ApiService 的 方法调用集中到了 InvocationHandler.invoke，再构建了 ServiceMethod、OkHttpCall，返回 callAdapter.adapt() 的结果。

　　简而言之，就是动态生成接口的实力类（当然生成实现类由缓存机制），并创建其实例（称之为代理），代理把对接口的调用转发给 InvocationHandler 实例，而在 InvocationHandler 的实现中，处理执行真正的逻辑（例如再次转发给真正的实现类对象），还可以进行一些有用的操作，例如统计执行时间，进行初始化和清理、对接口调用进行检查等。

　　为什么要用动态代理？因为对接口所有方法的调用都会集中转发到 InvocationHandler#invoke 函数中，这样就可以集中进行处理，更方便了。

　　在 invoke 方法处理方法调用的过程中，如果调用的是 Object 的方法，例如 equals、toString，那就直接调用。如果是 default ，就调用 default 方法。而真正有用的代码只有三行：

```java
ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
// 再包装成 OkHttpCall
OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args); // 请求
return serviceMethod.callAdapter.adapt(okHttpCall);
```



### ServiceMethod 的职责以及 loadServiceMethod 分析

　　ServiceMethod 是接口方法的抽象，主要负责解析它对应的 method 的各种参数（它有各种如 parseHeaders 的方法），比如注解（@GET）、入参，另外还负责获取 callAdapter、responseConverter 等 Retrofit 配置，好为后面的 pkhttp3/Request 做好参数准备，它的 toRequest 为 OkHttp 提供 Request，可以说它承载了后续 Http 请求所需的一切参数。

　　ServiceMethod 类的作用就是把对接口方法的调用转为一次 HTTP 调用。

　　一个 ServiceMethod 对象对应于一个 API interface 的一个方法，loadServiceMethod(method) 方法负责 ServiceMethod：

#### loadServiceMethod 方法

```java
// serviceMethodCache 的定义
ServiceMethod<?, ?> loadServiceMethod(Method method) {
    // 获取 method 对应的 ServiceMethod
    ServiceMethod<?, ?> result = serviceMethodCache.get(method);
    if (result != null) return result;

    synchronized (serviceMethodCache) {
      // 先从缓存去获取
      result = serviceMethodCache.get(method);
      if (result == null) {
        // 缓存中更没有则新建，并存入缓存
        result = new ServiceMethod.Builder<>(this, method).build();
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }
```

　　loadServiceMethod 方法，负责为 method 生成一个 ServiceMethod，并且给 ServiceMethod 做了缓存。

　　这里实现了缓存逻辑，同一个 API 的同一个方法，只会创建一次。这是由于每次获取 API 实例都是传入的 class 对象，而 class 对象是进程内单例的，所以获取到它的同一个方法 Method 实例也是单例的，所以这里的缓存是有效的。

　　动态代理是有一定的性能损耗的，并且 ServiceMethod 的创建伴随着各种注解参数解析，这也是耗时间的，再加上一个 App 调用接口是非常频繁的，如果每次接口请求都需要重新生成那么有浪费资源损害性能的可能，所以做了一份缓存来提供效率。

#### ServiceMethod 的构造函数

```java
  ServiceMethod(Builder<R, T> builder) {
    this.callFactory = builder.retrofit.callFactory();
    this.callAdapter = builder.callAdapter;
    this.baseUrl = builder.retrofit.baseUrl();
    this.responseConverter = builder.responseConverter;
    this.httpMethod = builder.httpMethod;
    this.relativeUrl = builder.relativeUrl;
    this.headers = builder.headers;
    this.contentType = builder.contentType;
    this.hasBody = builder.hasBody;
    this.isFormEncoded = builder.isFormEncoded;
    this.isMultipart = builder.isMultipart;
    this.parameterHandlers = builder.parameterHandlers;
  }
```

　　成员很多，重点关注四个成员：callFactory、callAdapter、responseConverter 和 parameterHandlers。

1. callFactory（就是 OkHttpClient，实现了 Call.Factory 的接口） 负责创建 HTTP 请求，HTTP 请求被抽象为了 okHttp3.Call 类，它表示一个已经准备好，可以随时执行的 HTTP 请求。
2. callAdapter （就是通过调用 addCallAdapterFactory() 方法传输的对象，这里就是 RxJavaCallAdapterFactory）把 retrofit2.Call< T > 转为 T（注意和 okHttp3.Call 区分开来，retrofit2.call< T > 表示的是一个 Retrofit 方法的调用），这个过程会发送一个 HTTP 请求，拿到服务器返回的数据（通过 okHttp3.Call 实现），并把数据转换为声明的 T 类型对象（通过 Converter< F,T > 实现）。
3. responseConverter （就是通过调用 addConverterFactory() 方法传输的对象，这里就是 GsonConverterFactory）是 Converter< ResponseBody, T > 类型，负责把服务器返回的数据（JSON、XML、二进制或者其他格式，由 ResponseBody 封装）转为 T 类型的对象。
4.  parameterHandlers 则负责解析 API 定义时每个方法的参数，并在构造 HTTP 请求时设置参数。parameterHandler 是一个抽象类，其子类有 Header、Path、Query、QueryName、QueryMap、HeaderMap、Field、FieldMap、Part、RawPart、PartMap、Body，这些子类对应着我们定义的 API 方法的参数注解。

#### CallFactory

　　this.callFactory = build.retrofit.callFactory()，所以 callFactory 实际上由 Retrofit 类提供，而我们在构造 Retrofit 对象时，可以指定 callFactory，如果不指定，将默认设置为一个 okHttp3.OkHttpClient。

#### callAdapter

```java
    private CallAdapter<T, R> createCallAdapter() {
      Type returnType = method.getGenericReturnType();
      if (Utils.hasUnresolvableType(returnType)) {
        throw methodError(
            "Method return type must not include a type variable or wildcard: %s", returnType);
      }
      if (returnType == void.class) {
        throw methodError("Service methods cannot return void.");
      }
      // 获取方法的所有注解
      Annotation[] annotations = method.getAnnotations();
      try {
        //noinspection unchecked
        return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create call adapter for %s", returnType);
      }
    }
```

　　可以看到，callAdapter 还是由 Retrofit 类提供。在 Retrofit 类内部，将遍历一个 CallAdapter.Factory 列表，让工厂们提供，如果最终没有工厂能（根据 returnType 和 annotations）提供需要的 CallAdapter，那将抛出异常。而这个工厂列表我们可以在在构造 Retrofit 对象时进行添加。

#### responseConverter

```java
    private Converter<ResponseBody, T> createResponseConverter() {
      Annotation[] annotations = method.getAnnotations();
      try {
        return retrofit.responseBodyConverter(responseType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create converter for %s", responseType);
      }
    }
```

　　同样，responseConverter 还是由 Retrofit 类提供，而在其内部，逻辑和创建 callAdapter 基本一致，通过遍历 Converter.Factory 列表，看看有没有工厂能够提供需要的需要的 responseBodyConverter，工厂列表同样可以在构造 Retrofit 对象时进行添加。

#### parameterHandlers

　　每个参数都会有一个 ParameterHandler ，由 ServiceMethod#parseParameter 方法负责创建，其主要内容就是解析每个参数使用的注解类型（诸如 Path、Query、Field 等），对每种类型进行单独处理。构造 HTTP 请求时，我们传毒的参数都是字符串，那 Retrofit 是如何把我们传递的各种参数都转化为 String 的呢？还是由 Retrofit 类提供 converter！

　　Converter.Factory 除了提供 responseBodyConverter，还提供 requestBodyConverter 和 StringConverter，API 方法中除了 @Body 和 @Part 类型的参数，都利用 stringConverter 进行转换，而 @Body 和 @Part 类型的参数则利用 requesyBodyConverter 进行转换。

　　这三种 converter 都是通过 “询问” 工厂列表进行提供，而工厂列表可以在构造 Retrofit 对象时进行添加。

#### 工厂让各个模块得以高度解耦

　　上面提到了三种工厂：okHttp3.Call.Factory，CallAdapter.Factory 和 Converter.Factory，分别负责提供不同的模块，至于怎么提供、提供何种模块，统统交给工厂，Retrofit 完全不掺和，它只负责提供用于决策的信息，例如参数 / 返回值类型、注解等。

　　这样就是高内聚低耦合的效果。解耦的第一步就是面向接口编程，模块之间、类之间通过接口进行依赖，创建怎样的实例，则交给工厂负责，工厂同样也是接口，添加怎么的工厂，则在最初构造 Retrofit 对象时决定，各个模块之间完全解耦，每个模块只专注于自己的职责。

### OkHttpCall

　　OkHttpCall 实现了 retrofit2.Call，通常会使用它的 execute() 和 enqueue(Callback< T > callback) 接口。前者用于同步执行 HTTP 请求，后者用于异步执行。 

　　将 serviceMethod 和 args 作为参数生成了一个 OkHttpCall。

```java
OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod,args);
```

　　OkHttpCall 是对 OkHttp3.call 的组合包装，OkHttpCall 中有一个成员 OkHttp3.Call rawCall。

### CallAdapter 流程分析

　　CallAdapter< T > # adapt(Call < R > call) 函数负责把 retrofit2.call< R > 转为 T，这里 T 当然可以就是 retrofit2.call < R > ，这时直接返回参数就可以了，实际上这正是 DefaultCallAdapterFactory  创建的 CallAdapter 的行为。  

　　这里涉及到的 callAdapter 是由配置 Retrofit 的 addCallAdapterFactory 方法中传入的 RxJavaCallAdapterFactory.create() 生成，实例为 RxJavaCallAdapterFactory。

　　实例的生成大致流程为：ServiceMethod.Build() -> ServiceMethod.createCallAdapter() -> retrofit.callAdapter() -> adapterFactories 遍历 -> 最终到 RxJavaCallAdapterFactory.get() # getCallAdapter() -> return new RxJavaCallAdapter(Observable,scheduler)；

　　由于使用了 RxJava，所以最终得到的 callAdapter 为 RxJavaCallAdapter。

#### RxJavaCallAdapter 的 adap 方法

```java
  @Override public Object adapt(Call<R> call) {
    // 这个 call 是 OkHttpCall
    // OkHttpCall = new OkHttpCall<>(serviceMethod, args) 生成的 OkHttpCall
    //是否是异步，异步就是CallEnqueueOnSubscribe，同步是 CallExecuteOnSubscribe
    OnSubscribe<Response<R>> callFunc = isAsync
        ? new CallEnqueueOnSubscribe<>(call)
        : new CallExecuteOnSubscribe<>(call);

    OnSubscribe<?> func;
    if (isResult) {
      func = new ResultOnSubscribe<>(callFunc);
    } else if (isBody) {
      func = new BodyOnSubscribe<>(callFunc);
    } else {
      func = callFunc;
    }
    Observable<?> observable = Observable.create(func);

    if (scheduler != null) {
      observable = observable.subscribeOn(scheduler);
    }

    if (isSingle) {
      return observable.toSingle();
    }
    if (isCompletable) {
      return observable.toCompletable();
    }
    return observable;
  }
```

　　查看 CallEnqueueOnSubscribe 类：

```java
final class CallEnqueueOnSubscribe<T> implements OnSubscribe<Response<T>> {
  private final Call<T> originalCall;

  CallEnqueueOnSubscribe(Call<T> originalCall) {
    this.originalCall = originalCall;
  }

  @Override public void call(Subscriber<? super Response<T>> subscriber) {
    // Since Call is a one-shot type, clone it for each new subscriber.
    Call<T> call = originalCall.clone();
    final CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
    subscriber.add(arbiter);
    subscriber.setProducer(arbiter);
	// 进行异步请求。call 是 OkHttpCall 的实例
    call.enqueue(new Callback<T>() {
      @Override public void onResponse(Call<T> call, Response<T> response) {
        arbiter.emitResponse(response);
      }

      @Override public void onFailure(Call<T> call, Throwable t) {
        Exceptions.throwIfFatal(t);
        arbiter.emitError(t);
      }
    });
  }
}
```

　　RxJavaCallAdapter 的 adap 方法很简单，创建一个 Observable 获取 CallOnSubscribe 中的 Response < T > 通过  observable.toSingle() 或者 toCompletable() 转为 Object 返回。这里取发送请求获取数据的任务在 OnSubscribe 的实现类中（例如 CallEnqueueOnSubscribe），并且最后走到了 okHttpCall.execute 或者 okHttpCall.enqueue 中去了。

#### OkHttpCall 的 execute() 方法

```java
  @Override public Response<T> execute() throws IOException {
    okhttp3.Call call;

    synchronized (this) {
      if (executed) throw new IllegalStateException("Already executed.");
      executed = true;

      if (creationFailure != null) {
        if (creationFailure instanceof IOException) {
          throw (IOException) creationFailure;
        } else {
          throw (RuntimeException) creationFailure;
        }
      }

      call = rawCall;
      if (call == null) {
        try {
          call = rawCall = createRawCall();
        } catch (IOException | RuntimeException e) {
          creationFailure = e;
          throw e;
        }
      }
    }

    if (canceled) {
      call.cancel();
    }

    return parseResponse(call.execute());
  }

  private okhttp3.Call createRawCall() throws IOException {
    Request request = serviceMethod.toRequest(args);
    okhttp3.Call call = serviceMethod.callFactory.newCall(request);
    if (call == null) {
      throw new NullPointerException("Call.Factory returned null.");
    }
    return call;
  }

  Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();

    // Remove the body's source (the only stateful object) so we can pass the response along.
    rawResponse = rawResponse.newBuilder()
        .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
        .build();

    int code = rawResponse.code();
    if (code < 200 || code >= 300) {
      // 返回错误
      try {
        // Buffer the entire body to avoid future I/O.
        ResponseBody bufferedBody = Utils.buffer(rawBody);
        return Response.error(bufferedBody, rawResponse);
      } finally {
        rawBody.close();
      }
    }

    if (code == 204 || code == 205) {
      rawBody.close();
      return Response.success(null, rawResponse);
    }

    ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
    try {
      T body = serviceMethod.toResponse(catchingBody);
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      // If the underlying source threw an exception, propagate that rather than indicating it was
      // a runtime exception.
      // 异常处理
      catchingBody.throwIfCaught();
      throw e;
    }
  }
```

　　主要包括三步：

1. 创建 okHttp3.call，包括构造参数。
2. 执行网络请求。
3. 解析网络请求返回的数据。

　　createRawCall() 函数中，调用了 serviceMethod.toRequest(args) 来创建 okHttp3.Request，而在后者中，之前准备好的 parameterHandlers 就派上了用场。

　　然后再调用 serviceMethod.callFactory.newCall(request) 来创建 okHttp3.Call，之前准备好的 callFactory 同样也派上了用场，由于工厂在构造 Retrofit 对象时可以指定，所以也可以指定其他的工厂（例如 HttpUrlConnection 的工厂），来使用其他的底层 HttpClient 实现。

　　调用 okhttp3.Call#execute() 来执行网络请求，这个方法时阻塞的，执行完毕之后将返回收到的响应数据。收到响应数据之后，进行了状态码的检查，通过检查之后调用了 serviceMethod.toResponse(catchingBody) 来把响应数据转换为需要的数据类型对象。在 toResponse 函数中，之前准备好的 responseConverter 也派上了用场。

#### OkHttpCall 的 enqueue 方法

　　异步交给了 okhttp3.Call#enqueue（Callback callback）来实现，并在它的 callback 中调用 paseResponse 解析响应数据，并转发给传入的 callback。

```java
  @Override public void enqueue(final Callback<T> callback) {
    checkNotNull(callback, "callback == null");

    okhttp3.Call call;
    Throwable failure;

    synchronized (this) {
       // 同一个请求，不能执行两次
      if (executed) throw new IllegalStateException("Already executed.");
      executed = true;

      call = rawCall;
      failure = creationFailure;
      if (call == null && failure == null) {
        try {
          // 创建 okHttp3.call
          call = rawCall = createRawCall();
        } catch (Throwable t) {
          failure = creationFailure = t;
        }
      }
    }

    if (failure != null) {
      callback.onFailure(this, failure);
      return;
    }

    if (canceled) {
      call.cancel();
    }
	//请求
    call.enqueue(new okhttp3.Callback() {
      @Override public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse)
          throws IOException {
        Response<T> response;
        try {
          // 解析 rawResponse
          response = parseResponse(rawResponse);
        } catch (Throwable e) {
          callFailure(e);
          return;
        }
        callSuccess(response);
      }

      @Override public void onFailure(okhttp3.Call call, IOException e) {
        try {
          callback.onFailure(OkHttpCall.this, e);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }

      private void callFailure(Throwable e) {
        try {
          callback.onFailure(OkHttpCall.this, e);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }

      private void callSuccess(Response<T> response) {
        try {
          callback.onResponse(OkHttpCall.this, response);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });
  }
```

#### OkHttpCall 的 parseResponse

　　解决请求数据得到的响应结果：

```java
  Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();

    // Remove the body's source (the only stateful object) so we can pass the response along.
    rawResponse = rawResponse.newBuilder()
        .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
        .build();

    int code = rawResponse.code();
    if (code < 200 || code >= 300) {
      try {
        // Buffer the entire body to avoid future I/O.
        ResponseBody bufferedBody = Utils.buffer(rawBody);
        return Response.error(bufferedBody, rawResponse);
      } finally {
        rawBody.close();
      }
    }

    if (code == 204 || code == 205) {
      rawBody.close();
      return Response.success(null, rawResponse);
    }

    ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
    try {
      // 调用 ServideMthod 的 toResponse 方法，将其转为 T 类型
      T body = serviceMethod.toResponse(catchingBody);
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      // If the underlying source threw an exception, propagate that rather than indicating it was
      // a runtime exception.
      catchingBody.throwIfCaught();
      throw e;
    }
  }
```

　　ServiceMethod 的 toResponse 方法最终会调用到调用设置的 GsonRequestBodyConverter的 convert 方法，将 response 处理后返回。

　　经过一连串的处理，最终在 OkHttpCall.enqueue() 方法中生成 okhttp3.call 交给 OkHttpClient 去发送请求，再由配置的 Converter 处理 Response，返回给 SimpleCallAdapter 处理，返回最终所需要的 Observable。

## retrofit-adapters 模块

　　retrofit 模块内置了 DefaultCallAdapterFactory 和 ExecutorCallAdapterFactory ，它们都适用于 API 方法得到的类型为 retrofit2.Call 的情形，前者生产的 adapter 啥也不做，直接把参数返回，后者生产的 adapter 则会在异步调用时在指定的 Executor 上执行回调。

　　retorfit-adapters 的各个子模块则实现了更多的工厂：GuavaCallAdapterFactory、Java8CallAdapterFactory 和 RxJavaCallAdapterFactory。

　　RxJavaCallAdapterFactory#getCallAdapter 方法中对返回值的泛型类型进行了进一步检查，例如声明的返回值类型为 Observable< List< Repo > >，泛型类型就是 List< Repo >，所有类型都由 RxJavaCallAdapter 负责转换。

　　来看看 RxJavaCallAdapter #adapter：

```java
  @Override public Object adapt(Call<R> call) {
    OnSubscribe<Response<R>> callFunc = isAsync
        ? new CallEnqueueOnSubscribe<>(call)
        : new CallExecuteOnSubscribe<>(call);

    OnSubscribe<?> func;
    if (isResult) {
      func = new ResultOnSubscribe<>(callFunc);
    } else if (isBody) {
      func = new BodyOnSubscribe<>(callFunc);
    } else {
      func = callFunc;
    }
    Observable<?> observable = Observable.create(func);

    if (scheduler != null) {
      observable = observable.subscribeOn(scheduler);
    }

    if (isSingle) {
      return observable.toSingle();
    }
    if (isCompletable) {
      return observable.toCompletable();
    }
    return observable;
  }
```

　　这里创建了一个 Observable，它的逻辑由 CallEnqueueOnSubscribe （异步）或者  CallExecuteOnSubscribe （同步）类实现。

### CallEnqueueOnSubscribe

　　再来看 CallEnqueueOnSubscribe#call：

```java
  @Override public void call(Subscriber<? super Response<T>> subscriber) {
    // Since Call is a one-shot type, clone it for each new subscriber.
    Call<T> call = originalCall.clone();
    final CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
    subscriber.add(arbiter);
    subscriber.setProducer(arbiter);

    call.enqueue(new Callback<T>() {
      @Override public void onResponse(Call<T> call, Response<T> response) {
        arbiter.emitResponse(response);
      }

      @Override public void onFailure(Call<T> call, Throwable t) {
        Exceptions.throwIfFatal(t);
        arbiter.emitError(t);
      }
    });
  }
```

　　做了三件事：

1. clone 了原来的 call，因为 okhttp3.call 是只能用一次的，所以每次都是新clone 一个进行网络请求；
2. 创建了一个叫做 CallArbiter 的 producer；
3. 把这个 producer 设置给 subscriber。
4. 调用 call 的 enqueue() 方法请求数据。

　　producer 的工作机制：大部分情况下 Subscriber 都是被动接收 Observable push 福哦来的数据，但要是 Observable 发的太快，Subscriber 处理不过来，那就有问题了，所有就有了一种 Subscriber 主动 pull 的机制，而这种机制就是通过 producer 实现的。给 Subscriber 设置 Producer 之后（通过 Subscriber#setProducer 方法）Subscriber 就会通过 Producer 向上流根据自己的能力请求数据（通过 Producer#request 方法），而 Producer 收到请求之后（通常都是 Observable 管理 Producer，所以 “相当于” 就是 Observable 收到了请求），再根据请求的量给 Subscriber 发数据。

### CallAbviter 

　　来看 CallAbviter 的 request 方法：

```java
  @Override public void request(long amount) {
    if (amount == 0) {
      return;
    }
    while (true) {
      int state = get();
      switch (state) {
        case STATE_WAITING:
          if (compareAndSet(STATE_WAITING, STATE_REQUESTED)) {
            return;
          }
          break; // State transition failed. Try again.

        case STATE_HAS_RESPONSE:
          if (compareAndSet(STATE_HAS_RESPONSE, STATE_TERMINATED)) {
            deliverResponse(response);
            return;
          }
          break; // State transition failed. Try again.

        case STATE_REQUESTED:
        case STATE_TERMINATED:
          return; // Nothing to do.

        default:
          throw new IllegalStateException("Unknown state: " + state);
      }
    }
  }
```

　　是一些运行状态的处理。在 STATE_HAS_RESPONSE 有返回了的时候，调用了 deliverResponse 方法:

```java
  private void deliverResponse(Response<T> response) {
    try {
      if (!isUnsubscribed()) {
        subscriber.onNext(response);
      }
    } catch (OnCompletedFailedException
        | OnErrorFailedException
        | OnErrorNotImplementedException e) {
      RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
      return;
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      try {
        subscriber.onError(t);
      } catch (OnCompletedFailedException
          | OnErrorFailedException
          | OnErrorNotImplementedException e) {
        RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
      } catch (Throwable inner) {
        Exceptions.throwIfFatal(inner);
        CompositeException composite = new CompositeException(t, inner);
        RxJavaPlugins.getInstance().getErrorHandler().handleError(composite);
      }
      return;
    }
    try {
      if (!isUnsubscribed()) {
        subscriber.onCompleted();
      }
    } catch (OnCompletedFailedException
        | OnErrorFailedException
        | OnErrorNotImplementedException e) {
      RxJavaPlugins.getInstance().getErrorHandler().handleError(e);
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      RxJavaPlugins.getInstance().getErrorHandler().handleError(t);
    }
  }
```

　　调用 onNext() 将 response 发送给下游，异常则调用 onError()，没有异常，则调用了 onComplete() ，这样就将请求的结果 response 发回了用户。

## retrofit-converters 模块

　　retrofit 模块内置了 BuiltInConverters，只能处理 ResponseBody、ResponseBody 和 String 类型的转换（实际上不需要转），而 retrofit-converters 中的子模块则提供了 JSON、XML、ProtoBuf 等类型数据的转换功能，而且还有多种转换方式可以选择。

　　以 GsonConverterFactory 为例。







## 问题

1. 调用接口的方法后是怎么发送请求的？这背后发生了什么？

   Retrofit 使用了动态代理给定义的接口设置了代理，当调用接口的方法时，Retrofit 会拦截下来，然后经过一系列处理，比如解析方法的注解等，生成了 call Request 等 OkHttp 所需的资源，最后交给 OkHttp 去发送请求，此间经过 callAdapter、converter 的处理，最后拿到所需要的数据。

2. Retrofit 与 OkHttp 是怎么合作的？

   在 Retrofit 中，ServiceMethod 承载了一个 Http 请求的所有参数，OkHttpCall 为 okhttp3.call 的组合包装，由它们两合作，生成用于 OkHttp 所需的 Request 以及 okhttp3.call，交给 OkHttp 去发送请求。

   可以说 Retrofit 为 OkHttp 再封装了一层，并添加了不少功能以及扩展，减少了开发使用成本。

3. Retrofit 中的数据究竟是怎么处理的？它是怎么返回 RxJava.Observable 的?

   Retrofit 中的数据其实是交给了 callAdapter 以及 converter 去处理，callAdapter 负责把 okHttpCall 转为用户所需的 Observable 类型，converter 负责把服务器返回的数据专程具体的实体类。





## 流程图

![](image/retrofit源码流程图.webp)

　　

## 流程

1. 通过门面 Retrofit 来 build 一个 Service Interface 的 proxy

```java
  public <T> T create(final Class<T> service) {
    Utils.validateServiceInterface(service);
    if (validateEagerly) {
      eagerlyValidateMethods(service);
    }
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          private final Platform platform = Platform.get();

          @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args)
              throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            if (platform.isDefaultMethod(method)) {
              return platform.invokeDefaultMethod(method, service, proxy, args);
            }
            ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
            OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
            return serviceMethod.callAdapter.adapt(okHttpCall);
          }
        });
  }
```

2. 当调用这个 Service Interface 中的某个请求方法，会被 proxy 拦截。

```java
    @POST("v1/student/wrong")
    Observable<BaseResponse> postClassroomWorngNoteAnswer(@Query("F_student_id") String studentId,
                                                          @Query("F_accesstoken") String accesstoken,
                                                          @Query("F_resource_id") String resourceId);
```

3. 通过 ServiceMethod 来解析 invoke 的那个方法，通过解析注解，传参，将它们封装成熟悉的 reequest，然后通过具体的返回值类型，让之前配置的工厂生成具体的 CallAdapter、ResponseConveter。

4. new 一个 OkHttpCall，这个 OkHttpCall 算是 OkHttp 的包装类，用它跟 OkHttp 对接，所有 OkHttp 需要的参数都可以看这个类。当然还可以扩展一个新的 Call ，比如 HttpUrlConnectionCall，但是有些耦合。

   ```java
   // Retrofic 的 create 方法中的部分代码
   ServiceMethod<Object, Object> serviceMethod =
                   (ServiceMethod<Object, Object>) loadServiceMethod(method);
               OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
               return serviceMethod.callAdapter.adapt(okHttpCall);
   ```

   明确的指明了 OkHttpCall，而不是通过工厂来生成 Call。所以如果不想改源码，重新编译，那就只能使用 OkHttp 了。

5. 生成的 CallAdapter 有四个工厂，分别对应不同的平台：RxJava、Java8、Guava 还有一个 Retrofit 默认的。简单来说就是用来将 Call 转成 T 的一个策略。因为这里具体请求时耗时操作，所以需要 CallAdapter 去管理线程。

6. 比如 RxJava 会根据调用方法的返回值，如 Response < T > | Result < T > | Observable < T >，生成不同的 CallAdapter。实际上就是对 RxJava 的回调方式做封装。比如将 response 再拆解为 success 和 error 等。

7. 在第 5 步中，说的 CallAdapter 还管理线程。比如说 RxJava，它最大的优点是可以指定方法在什么县城下执行。

   ```java
    Observable.interval(1, 1, TimeUnit.SECONDS)
   		.subscribeOn(Schedulers.computation())
           .observeOn(AndroidSchedulers.mainThread())
   ...
   ```

   在计算线程订阅（subscribeOn），在主线程观察（observeOn）。而这些是如何做的呢，可以看一下源码：

   ```java
   final class RxJavaCallAdapter<R> implements CallAdapter<R, Object> {
       
     ...
     @Override public Type responseType() {
       return responseType;
     }
   
     @Override public Object adapt(Call<R> call) {
       OnSubscribe<Response<R>> callFunc = isAsync
           ? new CallEnqueueOnSubscribe<>(call)
           : new CallExecuteOnSubscribe<>(call);
       ...
       Observable<?> observable = Observable.create(func);
   
       if (scheduler != null) {
         observable = observable.subscribeOn(scheduler);
       }
   
       if (isSingle) {
         return observable.toSingle();
       }
       if (isCompletable) {
         return observable.toCompletable();
       }
       return observable;
     }
   }
   ```

   在 adapt call 时，subscribeOn 了，所以就切换到子线程中了。

8. 在 adapt cal  中，具体的调用了 Call execute()，execute() 是同步的，enqueue() 是异步的。因为 RxJava 已经切换了线程，所以这里用同步方法 execute()。

   ```java
   final class CallExecuteOnSubscribe<T> implements OnSubscribe<Response<T>> {
     private final Call<T> originalCall;
   
     CallExecuteOnSubscribe(Call<T> originalCall) {
       this.originalCall = originalCall;
     }
   
     @Override public void call(Subscriber<? super Response<T>> subscriber) {
       // Since Call is a one-shot type, clone it for each new subscriber.
       Call<T> call = originalCall.clone();
       CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
       subscriber.add(arbiter);
       subscriber.setProducer(arbiter);
   
       Response<T> response;
       try {
         response = call.execute();
       } catch (Throwable t) {
         Exceptions.throwIfFatal(t);
         arbiter.emitError(t);
         return;
       }
       arbiter.emitResponse(response);
     }
   }
   
   ```

9. 接下来的具体请求，就是 OkHttp 的事情了，retrofit 要做的就是等待返回值，在第 4步中，说到 OkHttpCall 是 OkHttp 的包装类，所以将 OkHttp 的 response 转换称需要的 T，就是在 OkHttpCall 中执行的。

10. 当然具体的解析转换操作也不是 OkHttpCall 来做的，因为它也不知道数据格式是什么样的，所以它知识将 response 包装成 retrofit 标准下的 response。

11. Converter -> ResponseConverter，很明显，它是数据转换器。它将 response 转换称具体想要的 T。Retrofit 提供了很多的 converter factory，比如 Gson、Jackson、xml、protobuff 等等，需要什么，就配置相对应的工厂，在 Service 方法上声明泛型具体类型就可以了。

12. 最后，通过声明的 observeOn 线程回调给上层，这样上层就拿到了最终结果。至于结果再如何处理，那就是上层的事情了。

## 参考文章
[Retrofit分析-漂亮的解耦套路](https://www.jianshu.com/p/45cb536be2f4)

[Retrofit是如何工作的？](https://www.jianshu.com/p/cb3a7413b448)

[拆轮子系列：拆 Retrofit](https://blog.piasy.com/2016/06/25/Understand-Retrofit/index.html)

