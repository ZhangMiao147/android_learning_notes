# Retrofit 源码分析

　　Retrofit 的主要原理是利用了 Java 的动态代理技术，把 ApiService 的方法调用集中到了 InvocationHandler.invoke，再构建了 ServiceMethod 、OkHttpClient，返回 callAdapter.adapter 的结果。

　　Retrofit 的最大特点就是解耦。

## 1. 基本使用

### 1.1. 引入库

```ruby
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'io.reactivex:rxjava:1.1.6'
    api 'com.squareup.retrofit2:adapter-rxjava:2.3.0'
```

　　所以本文分析的是 Retrofit 2.3.0 的源码。

### 1.2. 创建 Retrofit 对象

```java
//构建 OkHttpClient 对象
OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10, TimeUnit.SECONDS) // 读取超时
                .retryOnConnectionFailure(true) // 是否重试
                .writeTimeout(10, TimeUnit.SECONDS) // 写入超时
                .build();
// 创建 Retrofit 对象，外观模式
Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test")
				.addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create())) // 解析使用 GsonConverterFactory
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 返回使用 RxJavaCallAdapterFactory
                .client(okHttpClient) // 请求使用 OkHttpClient
                .build();
```

### 1.3. 定义 API 

```java
public interface ApiService {
    @GET("data/")
    Observable<BaseResponse> getMessage(@Path("page") int page);

    public static class BaseResponse {

        /**
         * 业务错误码
         */
        @SerializedName("F_responseNo")
        public int responseNo;

        /**
         * 业务错误描述
         */
        @SerializedName("F_responseMsg")
        public String responseMsg;

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

### 1.4. 获取 API 实例

```java
// 获取 API 实例
ApiService service = retrofit.create(ApiService.class);
// 调用 getMessage 的方法请求数据
Observable<ApiService.BaseResponse> observable = service.getMessage(1);
...
```

　　Retrofit 就这样经过简单的配置后就可以向服务器请求数据了，超级简单。

## 2. 流程图

![](image/retrofit源码流程图.png)

## 3. 源码分析

### 3.1. Retrofit.create 方法（ 创建 API 实例 ）分析

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
            // 重点
            // 为 Method 生成一个 ServiceMethod
            ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method); // 4
            // 再包装成 OkHttpCall
            OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args); // 请求 5
            return serviceMethod.callAdapter.adapt(okHttpCall);
          }
        });
  }
```

　　从 create 方法中可以看出，Retrofit 的主要原理是利用了 Java 的**动态代理**技术创建了 API 实例，把 ApiService 的 方法调用集中到了 InvocationHandler.invoke，再构建了 ServiceMethod、OkHttpCall，返回 callAdapter.adapt() 的结果。

　　也就是当调用前面写的 ApiService Interface 中的请求方法，会被 proxy 拦截，调用 InvocationHandler.invoke 的方法：

```java
    @GET("data/")
    Observable<BaseResponse> getMessage(@Path("page") int page);
```

　　动态代理技术就是动态生成接口的实例类（当然生成实现类有缓存机制），并创建其实例（称之为代理），代理把对接口的调用转发给 InvocationHandler 实例，而在 InvocationHandler 的实现中，处理执行真正的逻辑（例如再次转发给真正的实现类对象），还可以进行一些有用的操作，例如统计执行时间，进行初始化和清理、对接口调用进行检查等。

　　为什么要用动态代理？因为对接口所有方法的调用都会集中转发到 InvocationHandler#invoke 函数中，这样就可以集中进行处理，更方便了。

　　在 invoke 方法处理方法调用的过程中，如果调用的是 Object 的方法，例如 equals、toString，那就直接调用。如果是 default ，就调用 default 方法。而真正重要的代码只有三行：

```java
ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
// 再包装成 OkHttpCall
OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args); // 请求
return serviceMethod.callAdapter.adapt(okHttpCall);
```

　　接下来从这三行代码开始分析。

## 4. ServiceMethod 

　　ServiceMethod 是接口方法的抽象，主要负责解析它对应的 method 的各种参数（它有各种如 parseHeaders 的方法），比如注解（@GET）、入参，另外还负责获取 callAdapter、responseConverter 等 Retrofit 配置，好为后面的 okhttp3/Request 做好参数准备，它的 toRequest 为 OkHttp 提供 Request，toResponse 将请求结果转换为想要的数据类，可以说它承载了后续 Http 请求所需的一切参数。总的来说就是 ServiceMethod 类的作用就是把对接口方法的调用转为一次 HTTP 调用。

　　一个 ServiceMethod 对象对应于一个 API interface 的一个方法。

### 4.1. loadServiceMethod 方法

　　loadServiceMethod(method) 方法负责创建 ServiceMethod：

```java
ServiceMethod<?, ?> loadServiceMethod(Method method) {
    // 先从缓存中获取 method 对应的 ServiceMethod，如果有则直接返回
    ServiceMethod<?, ?> result = serviceMethodCache.get(method);
    if (result != null) return result;

  	// 同步缓存
    synchronized (serviceMethodCache) {
      // 先从缓存去获取
      result = serviceMethodCache.get(method);
      if (result == null) {
        // 缓存中没有则新建，并存入缓存
        result = new ServiceMethod.Builder<>(this, method).build();
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }
```

　　loadServiceMethod 方法，负责为 method 生成一个 ServiceMethod，并且给 ServiceMethod 做了缓存。

　　实现了缓存逻辑后，同一个 API 的同一个方法，只会创建一次。这是由于每次获取 API 实例都是传入的 class 对象，而 class 对象是进程内单例的，所以获取到它的同一个方法 Method 实例也是单例的，所以这里的缓存是有效的。

　　动态代理是有一定的性能损耗的，并且 ServiceMethod 的创建伴随着各种注解参数解析，这也是耗时间的，再加上一个 App 调用接口是非常频繁的，如果每次接口请求都需要重新生成那么有浪费资源损害性能的可能，所以做了一份缓存来提供效率。

#### 4.1.1. ServiceMethod.Builder 的 build() 方法

```java
    public ServiceMethod build() {
      // 设置 callAdapter
      callAdapter = createCallAdapter(); // 4.1.2
      responseType = callAdapter.responseType();
      if (responseType == Response.class || responseType == okhttp3.Response.class) {
        throw methodError("'"
            + Utils.getRawType(responseType).getName()
            + "' is not a valid response body type. Did you mean ResponseBody?");
      }
      // 设置 responseConverter
      responseConverter = createResponseConverter(); // 4.1.3
	  	// 解析方法的注解
      for (Annotation annotation : methodAnnotations) {
        parseMethodAnnotation(annotation); // 4.1.4
      }

      if (httpMethod == null) {
        throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
      }

      if (!hasBody) {
        if (isMultipart) {
          throw methodError(
              "Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
        }
        if (isFormEncoded) {
          throw methodError("FormUrlEncoded can only be specified on HTTP methods with "
              + "request body (e.g., @POST).");
        }
      }

      int parameterCount = parameterAnnotationsArray.length;
      // 创建 parameterHandlers，为后面转换 Request 做准备
      parameterHandlers = new ParameterHandler<?>[parameterCount];
      for (int p = 0; p < parameterCount; p++) {
        Type parameterType = parameterTypes[p];
        if (Utils.hasUnresolvableType(parameterType)) {
          throw parameterError(p, "Parameter type must not include a type variable or wildcard: %s",
              parameterType);
        }

        Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
        if (parameterAnnotations == null) {
          throw parameterError(p, "No Retrofit annotation found.");
        }

        parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations); // 4.1.6
      }

      if (relativeUrl == null && !gotUrl) {
        throw methodError("Missing either @%s URL or @Url parameter.", httpMethod);
      }
      if (!isFormEncoded && !isMultipart && !hasBody && gotBody) {
        throw methodError("Non-body HTTP method cannot contain @Body.");
      }
      if (isFormEncoded && !gotField) {
        throw methodError("Form-encoded method must contain at least one @Field.");
      }
      if (isMultipart && !gotPart) {
        throw methodError("Multipart method must contain at least one @Part.");
      }
	  // 创建 ServiceMethod() 实例
      return new ServiceMethod<>(this);
    }
```

　　在 build() 方法里面：

1. 调用 createCallAdapter 方法设置 callAdapter。
2. 设置 responseConverter。
3. 解析方法的参数。
4. 创建 parameterHandlers 实例，为后面做准备。
5. 创建了 ServiceMethod() 实例。

#### 4.1.2. createCallAdapter() 方法

```java
    private CallAdapter<T, R> createCallAdapter() {
      // 获取返回类型
      Type returnType = method.getGenericReturnType();
      if (Utils.hasUnresolvableType(returnType)) {
        throw methodError(
            "Method return type must not include a type variable or wildcard: %s", returnType);
      }
      if (returnType == void.class) {
        throw methodError("Service methods cannot return void.");
      }
      // 方法的注解
      Annotation[] annotations = method.getAnnotations();
      try {
        //noinspection unchecked
        // 根据返回类型和注解返回对应的 CallAdapter
        return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create call adapter for %s", returnType);
      }
    }
```

　　在 retrofit 的 adapterFactories 列表里面寻找对应的 CallAdapter。　　 

#### 4.1.3. createResponseConverter() 方法

```java
    private Converter<ResponseBody, T> createResponseConverter() {
      // 注解
      Annotation[] annotations = method.getAnnotations();
      try {
        // 根据 responseType 和注解返回对应的 Converter
        return retrofit.responseBodyConverter(responseType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create converter for %s", responseType);
      }
    }
```

　　在 retrofit 的 converterFactories 列表里面寻找对应的 Converter。　

#### 4.1.4. parseMethodAnnotation() 方法

　　看解析方法注解的 parseMethodAnnotation() 方法：

```java
    private void parseMethodAnnotation(Annotation annotation) {
      if (annotation instanceof DELETE) { // 注解是 DELETE
        parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
      } else if (annotation instanceof GET) { // 注解是 GET
        parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
      } else if (annotation instanceof HEAD) { // 注解是 HEAD
        parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
        if (!Void.class.equals(responseType)) {
          throw methodError("HEAD method must use Void as response type.");
        }
      } else if (annotation instanceof PATCH) {// 注解是 PATCH
        parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
      } else if (annotation instanceof POST) {// 注解是 POST
        parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
      } else if (annotation instanceof PUT) {// 注解是 PUT
        parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
      } else if (annotation instanceof OPTIONS) {// 注解是 OPTIONS
        parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
      } else if (annotation instanceof HTTP) { // 注解是 HTTP
        HTTP http = (HTTP) annotation;
        parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
      } else if (annotation instanceof retrofit2.http.Headers) { // 注解是 retrofit2.http.Headers
        String[] headersToParse = ((retrofit2.http.Headers) annotation).value();
        if (headersToParse.length == 0) {
          throw methodError("@Headers annotation is empty.");
        }
        headers = parseHeaders(headersToParse);
      } else if (annotation instanceof Multipart) {// 注解是 Multipart
        if (isFormEncoded) {
          throw methodError("Only one encoding annotation is allowed.");
        }
        isMultipart = true;
      } else if (annotation instanceof FormUrlEncoded) {// 注解是 FromUrlEncoded
        if (isMultipart) {
          throw methodError("Only one encoding annotation is allowed.");
        }
        isFormEncoded = true;
      }
    }
```

　　parseMethodAnnotation 方法会解析方法的注解，除了 retrofit2.http.Headers 调用了 parseHeaders 方法、Multipart 设置了 isMultipart = true，FormUrlEncoded 设置了 isFormEncoded = true，剩下的 POST、GET 等注解都调用了 parseHttpMethodAndPath() 方法解析注解:

```java
    private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
      if (this.httpMethod != null) {
        throw methodError("Only one HTTP method is allowed. Found: %s and %s.",
            this.httpMethod, httpMethod);
      }
      // 设置 httpMethod，例如“GET”、“PUT”等
      this.httpMethod = httpMethod;
      this.hasBody = hasBody;

      if (value.isEmpty()) {
        return;
      }

      // Get the relative URL path and existing query string, if present.
      // 检查请求的 url 是否正确
      int question = value.indexOf('?');
      if (question != -1 && question < value.length() - 1) {
        // Ensure the query string does not have any named parameters.
        String queryParams = value.substring(question + 1);
        Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
        if (queryParamMatcher.find()) {
          throw methodError("URL query string \"%s\" must not have replace block. "
              + "For dynamic query parameters use @Query.", queryParams);
        }
      }
	  // 请求的 url
      this.relativeUrl = value;
      // 请求的 url 参数名称
      this.relativeUrlParamNames = parsePathParameters(value);
    }

  static Set<String> parsePathParameters(String path) {
    Matcher m = PARAM_URL_REGEX.matcher(path);
    Set<String> patterns = new LinkedHashSet<>();
    while (m.find()) {
      patterns.add(m.group(1));
    }
    return patterns;
  }
```

#### 4.1.5. parseParameter 方法

```java
    private ParameterHandler<?> parseParameter(
        int p, Type parameterType, Annotation[] annotations) {
      ParameterHandler<?> result = null;
      for (Annotation annotation : annotations) {
        // 为注解调用 parseParameterAnnotation 方法
        ParameterHandler<?> annotationAction = parseParameterAnnotation(
            p, parameterType, annotations, annotation);

        if (annotationAction == null) {
          continue;
        }

        if (result != null) {
          throw parameterError(p, "Multiple Retrofit annotations found, only one allowed.");
        }

        result = annotationAction;
      }

      if (result == null) {
        throw parameterError(p, "No Retrofit annotation found.");
      }

      return result;
    }

    private ParameterHandler<?> parseParameterAnnotation(
        int p, Type type, Annotation[] annotations, Annotation annotation) {
      if (annotation instanceof Url) { // Url 注解
        ... // 异常处理

        gotUrl = true;

        if (type == HttpUrl.class
            || type == String.class
            || type == URI.class
            || (type instanceof Class && "android.net.Uri".equals(((Class<?>) type).getName()))) {
          return new ParameterHandler.RelativeUrl(); // 返回 Url 地址
        } else {
          throw parameterError(p,
              "@Url must be okhttp3.HttpUrl, String, java.net.URI, or android.net.Uri type.");
        }

      } else if (annotation instanceof Path) { // 注解是 Path
        ... // 异常处理
        gotPath = true;

        Path path = (Path) annotation;
        String name = path.value();
        validatePathName(p, name);

        Converter<?, String> converter = retrofit.stringConverter(type, annotations);
        return new ParameterHandler.Path<>(name, converter, path.encoded()); // 返回 ParameterHandler.Path 实例对象

      } else if (annotation instanceof Query) { // 注解是 Query
        Query query = (Query) annotation;
        String name = query.value();
        boolean encoded = query.encoded();

        Class<?> rawParameterType = Utils.getRawType(type);
        gotQuery = true;
        if (Iterable.class.isAssignableFrom(rawParameterType)) {
          if (!(type instanceof ParameterizedType)) {
            throw parameterError(p, rawParameterType.getSimpleName()
                + " must include generic type (e.g., "
                + rawParameterType.getSimpleName()
                + "<String>)");
          }
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
          Converter<?, String> converter =
              retrofit.stringConverter(iterableType, annotations);
          return new ParameterHandler.Query<>(name, converter, encoded).iterable(); // 返回 ParameterHandler.Query 实例对象
        } else if (rawParameterType.isArray()) {
          Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
          Converter<?, String> converter =
              retrofit.stringConverter(arrayComponentType, annotations);
          return new ParameterHandler.Query<>(name, converter, encoded).array();
        } else {
          Converter<?, String> converter =
              retrofit.stringConverter(type, annotations);
          return new ParameterHandler.Query<>(name, converter, encoded);
        }

      } else if (annotation instanceof QueryName) { // 注解是 queryName
        QueryName query = (QueryName) annotation;
        boolean encoded = query.encoded();

        Class<?> rawParameterType = Utils.getRawType(type);
        gotQuery = true;
        if (Iterable.class.isAssignableFrom(rawParameterType)) {
          if (!(type instanceof ParameterizedType)) {
            throw parameterError(p, rawParameterType.getSimpleName()
                + " must include generic type (e.g., "
                + rawParameterType.getSimpleName()
                + "<String>)");
          }
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
          Converter<?, String> converter =
              retrofit.stringConverter(iterableType, annotations);
          return new ParameterHandler.QueryName<>(converter, encoded).iterable();
        } else if (rawParameterType.isArray()) {
          Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
          Converter<?, String> converter =
              retrofit.stringConverter(arrayComponentType, annotations);
          return new ParameterHandler.QueryName<>(converter, encoded).array();
        } else {
          Converter<?, String> converter =
              retrofit.stringConverter(type, annotations);
          return new ParameterHandler.QueryName<>(converter, encoded);
        }

      } else if (annotation instanceof QueryMap) {
        Class<?> rawParameterType = Utils.getRawType(type);
        if (!Map.class.isAssignableFrom(rawParameterType)) {
          throw parameterError(p, "@QueryMap parameter type must be Map.");
        }
        Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
        if (!(mapType instanceof ParameterizedType)) {
          throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) mapType;
        Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
        if (String.class != keyType) {
          throw parameterError(p, "@QueryMap keys must be of type String: " + keyType);
        }
        Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
        Converter<?, String> valueConverter =
            retrofit.stringConverter(valueType, annotations);

        return new ParameterHandler.QueryMap<>(valueConverter, ((QueryMap) annotation).encoded());

      } else if (annotation instanceof Header) {
        Header header = (Header) annotation;
        String name = header.value();

        Class<?> rawParameterType = Utils.getRawType(type);
        if (Iterable.class.isAssignableFrom(rawParameterType)) {
          if (!(type instanceof ParameterizedType)) {
            throw parameterError(p, rawParameterType.getSimpleName()
                + " must include generic type (e.g., "
                + rawParameterType.getSimpleName()
                + "<String>)");
          }
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
          Converter<?, String> converter =
              retrofit.stringConverter(iterableType, annotations);
          return new ParameterHandler.Header<>(name, converter).iterable();
        } else if (rawParameterType.isArray()) {
          Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
          Converter<?, String> converter =
              retrofit.stringConverter(arrayComponentType, annotations);
          return new ParameterHandler.Header<>(name, converter).array();
        } else {
          Converter<?, String> converter =
              retrofit.stringConverter(type, annotations);
          return new ParameterHandler.Header<>(name, converter);
        }

      } else if (annotation instanceof HeaderMap) {
        Class<?> rawParameterType = Utils.getRawType(type);
        if (!Map.class.isAssignableFrom(rawParameterType)) {
          throw parameterError(p, "@HeaderMap parameter type must be Map.");
        }
        Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
        if (!(mapType instanceof ParameterizedType)) {
          throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) mapType;
        Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
        if (String.class != keyType) {
          throw parameterError(p, "@HeaderMap keys must be of type String: " + keyType);
        }
        Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
        Converter<?, String> valueConverter =
            retrofit.stringConverter(valueType, annotations);

        return new ParameterHandler.HeaderMap<>(valueConverter);

      } else if (annotation instanceof Field) {
        if (!isFormEncoded) {
          throw parameterError(p, "@Field parameters can only be used with form encoding.");
        }
        Field field = (Field) annotation;
        String name = field.value();
        boolean encoded = field.encoded();

        gotField = true;

        Class<?> rawParameterType = Utils.getRawType(type);
        if (Iterable.class.isAssignableFrom(rawParameterType)) {
          if (!(type instanceof ParameterizedType)) {
            throw parameterError(p, rawParameterType.getSimpleName()
                + " must include generic type (e.g., "
                + rawParameterType.getSimpleName()
                + "<String>)");
          }
          ParameterizedType parameterizedType = (ParameterizedType) type;
          Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
          Converter<?, String> converter =
              retrofit.stringConverter(iterableType, annotations);
          return new ParameterHandler.Field<>(name, converter, encoded).iterable();
        } else if (rawParameterType.isArray()) {
          Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
          Converter<?, String> converter =
              retrofit.stringConverter(arrayComponentType, annotations);
          return new ParameterHandler.Field<>(name, converter, encoded).array();
        } else {
          Converter<?, String> converter =
              retrofit.stringConverter(type, annotations);
          return new ParameterHandler.Field<>(name, converter, encoded);
        }

      } else if (annotation instanceof FieldMap) {
        if (!isFormEncoded) {
          throw parameterError(p, "@FieldMap parameters can only be used with form encoding.");
        }
        Class<?> rawParameterType = Utils.getRawType(type);
        if (!Map.class.isAssignableFrom(rawParameterType)) {
          throw parameterError(p, "@FieldMap parameter type must be Map.");
        }
        Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
        if (!(mapType instanceof ParameterizedType)) {
          throw parameterError(p,
              "Map must include generic types (e.g., Map<String, String>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) mapType;
        Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
        if (String.class != keyType) {
          throw parameterError(p, "@FieldMap keys must be of type String: " + keyType);
        }
        Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
        Converter<?, String> valueConverter =
            retrofit.stringConverter(valueType, annotations);

        gotField = true;
        return new ParameterHandler.FieldMap<>(valueConverter, ((FieldMap) annotation).encoded());

      } else if (annotation instanceof Part) {
        if (!isMultipart) {
          throw parameterError(p, "@Part parameters can only be used with multipart encoding.");
        }
        Part part = (Part) annotation;
        gotPart = true;

        String partName = part.value();
        Class<?> rawParameterType = Utils.getRawType(type);
        if (partName.isEmpty()) {
          if (Iterable.class.isAssignableFrom(rawParameterType)) {
            if (!(type instanceof ParameterizedType)) {
              throw parameterError(p, rawParameterType.getSimpleName()
                  + " must include generic type (e.g., "
                  + rawParameterType.getSimpleName()
                  + "<String>)");
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
            if (!MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType))) {
              throw parameterError(p,
                  "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
            }
            return ParameterHandler.RawPart.INSTANCE.iterable();
          } else if (rawParameterType.isArray()) {
            Class<?> arrayComponentType = rawParameterType.getComponentType();
            if (!MultipartBody.Part.class.isAssignableFrom(arrayComponentType)) {
              throw parameterError(p,
                  "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
            }
            return ParameterHandler.RawPart.INSTANCE.array();
          } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
            return ParameterHandler.RawPart.INSTANCE;
          } else {
            throw parameterError(p,
                "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
          }
        } else {
          Headers headers =
              Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"",
                  "Content-Transfer-Encoding", part.encoding());

          if (Iterable.class.isAssignableFrom(rawParameterType)) {
            if (!(type instanceof ParameterizedType)) {
              throw parameterError(p, rawParameterType.getSimpleName()
                  + " must include generic type (e.g., "
                  + rawParameterType.getSimpleName()
                  + "<String>)");
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
            if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType))) {
              throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                  + "include a part name in the annotation.");
            }
            Converter<?, RequestBody> converter =
                retrofit.requestBodyConverter(iterableType, annotations, methodAnnotations);
            return new ParameterHandler.Part<>(headers, converter).iterable();
          } else if (rawParameterType.isArray()) {
            Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
            if (MultipartBody.Part.class.isAssignableFrom(arrayComponentType)) {
              throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                  + "include a part name in the annotation.");
            }
            Converter<?, RequestBody> converter =
                retrofit.requestBodyConverter(arrayComponentType, annotations, methodAnnotations);
            return new ParameterHandler.Part<>(headers, converter).array();
          } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
            throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                + "include a part name in the annotation.");
          } else {
            Converter<?, RequestBody> converter =
                retrofit.requestBodyConverter(type, annotations, methodAnnotations);
            return new ParameterHandler.Part<>(headers, converter);
          }
        }

      } else if (annotation instanceof PartMap) {
        if (!isMultipart) {
          throw parameterError(p, "@PartMap parameters can only be used with multipart encoding.");
        }
        gotPart = true;
        Class<?> rawParameterType = Utils.getRawType(type);
        if (!Map.class.isAssignableFrom(rawParameterType)) {
          throw parameterError(p, "@PartMap parameter type must be Map.");
        }
        Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
        if (!(mapType instanceof ParameterizedType)) {
          throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) mapType;

        Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
        if (String.class != keyType) {
          throw parameterError(p, "@PartMap keys must be of type String: " + keyType);
        }

        Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
        if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(valueType))) {
          throw parameterError(p, "@PartMap values cannot be MultipartBody.Part. "
              + "Use @Part List<Part> or a different value type instead.");
        }

        Converter<?, RequestBody> valueConverter =
            retrofit.requestBodyConverter(valueType, annotations, methodAnnotations);

        PartMap partMap = (PartMap) annotation;
        return new ParameterHandler.PartMap<>(valueConverter, partMap.encoding());

      } else if (annotation instanceof Body) {
        if (isFormEncoded || isMultipart) {
          throw parameterError(p,
              "@Body parameters cannot be used with form or multi-part encoding.");
        }
        if (gotBody) {
          throw parameterError(p, "Multiple @Body method annotations found.");
        }

        Converter<?, RequestBody> converter;
        try {
          converter = retrofit.requestBodyConverter(type, annotations, methodAnnotations);
        } catch (RuntimeException e) {
          // Wide exception range because factories are user code.
          throw parameterError(e, p, "Unable to create @Body converter for %s", type);
        }
        gotBody = true;
        return new ParameterHandler.Body<>(converter);
      }

      return null; // Not a Retrofit annotation.
    }
```

　　parseParameter 方法主要是解析方法的注解，Url、Path 等，根据不同的注解，返回对应的 ParameterHandler 的对象。

#### 4.1.6. ServiceMethod 的构造函数

```java
  ServiceMethod(Builder<R, T> builder) {
    // 负责创建 HTTP 请求，retorfit 的 callFactory
    this.callFactory = builder.retrofit.callFactory();
    // 负责发送 HTTP 请求，拿到服务器返回的数据
    this.callAdapter = builder.callAdapter;
    this.baseUrl = builder.retrofit.baseUrl();
    // 负责把服务器返回的数据转为想要的对象格式
    this.responseConverter = builder.responseConverter;
    this.httpMethod = builder.httpMethod;
    this.relativeUrl = builder.relativeUrl;
    this.headers = builder.headers;
    this.contentType = builder.contentType;
    this.hasBody = builder.hasBody;
    this.isFormEncoded = builder.isFormEncoded;
    this.isMultipart = builder.isMultipart;
    // 负责解析 API 定义时每个方法的参数，并在构造 HTTP 请求时设置参数
    this.parameterHandlers = builder.parameterHandlers;
  }
```

　　成员很多，重点关注四个成员：callFactory、callAdapter、responseConverter 和 parameterHandlers。

##### 4.1.6.1. callFactory

　　callFactory 负责创建 HTTP 请求，HTTP 请求被抽象为了 okHttp3.Call 类，它表示一个已经准备好，可以随时执行的 HTTP 请求。

　　this.callFactory = build.retrofit.callFactory()，所以 callFactory 实际上由 Retrofit 类提供，在构造 Retrofit 对象时可以通过 client() 方法指定 callFactory，如果不指定，将默认设置为一个 okHttp3.OkHttpClient。

　　在前面的使用中就是通过方法 client() 设置的 OkHttpClient，OkHttpClient 实现了 Call.Factory 的接口。

##### 4.1.6.2. callAdapter

　　callAdapter 把 retrofit2.Call< T > 转为 T（注意和 okHttp3.Call 区分开来，retrofit2.call< T > 表示的是一个 Retrofit 方法的调用），这个过程会发送一个 HTTP 请求，拿到请求返回的数据（通过 okHttp3.Call 实现），并把数据转换为声明的 T 类型对象（通过 Converter< F,T > 实现）。

　　可以看到，callAdapter 还是由 Retrofit 类提供。在 Retrofit 类内部，将遍历一个 CallAdapter.Factory 列表，让工厂们提供，如果最终没有工厂能（根据 returnType 和 annotations）提供需要的 CallAdapter，那将抛出异常。而这个工厂列表可以在构造 Retrofit 对象时通过 addCallAdapterFactory() 方法进行添加。

　　在前面的使用中就是通过调用 addCallAdapterFactory() 方法传输的对象 RxJavaCallAdapterFactory，而 RxJavaCallAdapterFactory 的 callAdapter 是 RxJavaCallAdapter。

##### 4.1.6.3. responseConverter

　　responseConverter 是 Converter< ResponseBody, T > 类型，负责把服务器返回的数据（JSON、XML、二进制或者其他格式，由 ResponseBody 封装）转为 T 类型的对象。

　　同样，responseConverter 还是由 Retrofit 类提供，而在其内部，逻辑和创建 callAdapter 基本一致，通过遍历 Converter.Factory 列表，看看有没有工厂能够提供需要的需要的 responseBodyConverter，工厂列表也可以在构造 Retrofit 对象时通过 addConverterFactory() 方法进行添加。

　　在前面使用中就是通过调用 addConverterFactory() 方法传输的对象 GsonConverterFactory，而 GsonConverterFactory 对应的 responseConverter 是 GsonResponseBodyConverter。

　　上面提到了三种工厂：okHttp3.Call.Factory，CallAdapter.Factory 和 Converter.Factory，分别负责提供不同的模块，至于怎么提供、提供何种模块，统统交给工厂，Retrofit 完全不掺和，它只负责提供用于决策的信息，例如参数 / 返回值类型、注解等。

　　这样就是高内聚低耦合的效果。解耦的第一步就是面向接口编程，模块之间、类之间通过接口进行依赖，创建怎样的实例，则交给工厂负责，工厂同样也是接口，添加怎么的工厂，则在最初构造 Retrofit 对象时决定，各个模块之间完全解耦，每个模块只专注于自己的职责。

##### 4.1.6.4. parameterHandlers

　　parameterHandlers 则负责解析 API 定义时每个方法的参数，并在构造 HTTP 请求时设置参数。parameterHandler 是一个抽象类，其子类有 Header、Path、Query、QueryName、QueryMap、HeaderMap、Field、FieldMap、Part、RawPart、PartMap、Body，这些子类对应着定义的 API 方法的参数的那些注解。

　　每个参数都会有一个 ParameterHandler ，由 ServiceMethod#parseParameter 方法负责创建，其主要内容就是解析每个参数使用的注解类型（诸如 Path、Query、Field 等），对每种类型进行单独处理。

## 5. OkHttpCall

　　OkHttpCall 实现了 retrofit2.Call，通常会使用它的 execute() 和 enqueue(Callback< T > callback) 接口。前者用于同步执行 HTTP 请求，后者用于异步执行。 

　　将 serviceMethod 和 args 作为参数生成了一个 OkHttpCall。

```java
OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod,args);
```

### 5.1. okhttp3.Call

　　OkHttpCall 是对 okhttp3.call 的组合包装。

#### 5.1.1. OkHttpCall#createRawCall

　　OkHttpCall 中有一个成员 okhttp3.Call rawCall，在调用 execute() 或者 enqueue() 方法请求时，如果 rawCall 为空，则会调用 createRawCall 方法创建实例：

```java
  private okhttp3.Call createRawCall() throws IOException {
    // 这里调用了 serviceMethod # toRequest，会将创建时传递的 args 转为请求时需要的 Request 对象
    Request request = serviceMethod.toRequest(args);
    // 创建 okhttp3.Call 实例对象，serviceMethod.callFactory 就是 OkHttpClient ,OkHttpClient 返回的是 OkHttpClient 的 RealCall 对象，用来执行请求
    okhttp3.Call call = serviceMethod.callFactory.newCall(request);
    if (call == null) {
      throw new NullPointerException("Call.Factory returned null.");
    }
    // 返回创建的实例对象，会赋值给 rawCall
    return call;
  }
```

#### 5.1.2. ServiceMethod#toRequest

```java
  /** Builds an HTTP request from method arguments. */
  Request toRequest(@Nullable Object... args) throws IOException {
    // 创建 RequestBuilder 对象
    RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl, headers,
        contentType, hasBody, isFormEncoded, isMultipart);

    @SuppressWarnings("unchecked") // It is an error to invoke a method with the wrong arg types.
    ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;

    int argumentCount = args != null ? args.length : 0;
    if (argumentCount != handlers.length) {
      throw new IllegalArgumentException("Argument count (" + argumentCount
          + ") doesn't match expected count (" + handlers.length + ")");
    }

    for (int p = 0; p < argumentCount; p++) {
      handlers[p].apply(requestBuilder, args[p]);
    }
		// 创建 Request，并返回
    return requestBuilder.build();
  }
```

　　ServiceMethod#toRequest 方法主要是将 MethodService 解析的方法包装成一个 Request 实例。

#### 5.1.3. ServiceMethod#toResponse

```java
  R toResponse(ResponseBody body) throws IOException {
    // 调用了 responseConverter 的 convert 方法，而 responseConverter 则是 GsonResponseBodyConverter
    return responseConverter.convert(body);
  }
```

　　GsonResponseBodyConverter 实例的生成过程：ServiceMethod.builder#build -> ServiceMethod.createResponseConverter() ->Retrofit#responseBodyConverter() -> Retrofit#nextResponseBodyConverter -> converterFactories.get(i).responseBodyConverter() 循环遍历 -> GsonConverterFactory#responseBodyConverter retrurn GsonResponseBodyConverter。

　　ServiceMethod#toResponse 方法主要是将请求结果 ResponseBody 转为想要的 R 类型对象。

## 6. CallAdapter

　　serviceMethod.callAdapter 是从 Rretrofit 的 adapterFactories 工厂列表中寻找的，而 Retrofit 的adapterFactories 就是通过调用 addCallAdapterFactory() 方法传输的对象 RxJavaCallAdapterFactory，而 RxJavaCallAdapterFactory 的 get() 方法返回的是 RxJavaCallAdapter：

```java
  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    Class<?> rawType = getRawType(returnType);
    boolean isSingle = rawType == Single.class;
    boolean isCompletable = rawType == Completable.class;
    if (rawType != Observable.class && !isSingle && !isCompletable) {
      return null;
    }

    if (isCompletable) {
      return new RxJavaCallAdapter(Void.class, scheduler, isAsync, false, true, false, true);
    }

    boolean isResult = false;
    boolean isBody = false;
    Type responseType;
    if (!(returnType instanceof ParameterizedType)) {
      String name = isSingle ? "Single" : "Observable";
      throw new IllegalStateException(name + " return type must be parameterized"
          + " as " + name + "<Foo> or " + name + "<? extends Foo>");
    }

    Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
    Class<?> rawObservableType = getRawType(observableType);
    if (rawObservableType == Response.class) {
      if (!(observableType instanceof ParameterizedType)) {
        throw new IllegalStateException("Response must be parameterized"
            + " as Response<Foo> or Response<? extends Foo>");
      }
      responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
    } else if (rawObservableType == Result.class) {
      if (!(observableType instanceof ParameterizedType)) {
        throw new IllegalStateException("Result must be parameterized"
            + " as Result<Foo> or Result<? extends Foo>");
      }
      responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
      isResult = true;
    } else {
      responseType = observableType;
      isBody = true;
    }
		// 返回 RxJavaCallAdapter 对象
    return new RxJavaCallAdapter(responseType, scheduler, isAsync, isResult, isBody, isSingle,
        false);
  }
```

　　CallAdapter< T > # adapt(Call < R > call) 函数负责把 retrofit2.call< R > 转为 T，这里 T 当然可以就是 retrofit2.call < R > ，这时直接返回参数就可以了，实际上这正是 DefaultCallAdapterFactory  创建的 CallAdapter 的行为。  

　　RxJavaCallAdapter 实例的生成大致流程为：ServiceMethod.Build() -> ServiceMethod.createCallAdapter() -> retrofit.callAdapter() -> adapterFactories 遍历 -> 最终到 RxJavaCallAdapterFactory.get() # getCallAdapter() -> return new RxJavaCallAdapter(Observable,scheduler)；

　　由于使用了 RxJava，所以最终得到的 callAdapter 为 RxJavaCallAdapter。

### 6.1. RxJavaCallAdapter 的 adapt 方法

```java
  @Override public Object adapt(Call<R> call) {
    // 这个 call 是 OkHttpCall
    // OkHttpCall = new OkHttpCall<>(serviceMethod, args) 生成的 OkHttpCall
    // 是否是异步，异步就是 CallEnqueueOnSubscribe，同步是 CallExecuteOnSubscribe
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
    // 创建 Observable 对象
    Observable<?> observable = Observable.create(func);
	  // 在子线程中运行
    if (scheduler != null) {
      observable = observable.subscribeOn(scheduler);
    }

    if (isSingle) {
      return observable.toSingle();
    }
    if (isCompletable) {
      return observable.toCompletable();
    }
    // 返回 observable
    return observable;
  }
```

　　RxJavaCallAdapter 的 adapt() 方法很简单，创建一个 Observable 获取 CallEnqueueOnSubscribe （异步请求）或者 CallExecuteOnSubscribe（同步请求） 中的 Response < T > ，通过  observable.toSingle() 或者 toCompletable() 转为 Object 返回。这里去发送请求获取数据的任务在 OnSubscribe 的实现类中（例如 CallEnqueueOnSubscribe），并且最后走到了 okHttpCall.execute 或者 okHttpCall.enqueue 中去了。

### 6.2. CallExecuteOnSubscribe() 方法-同步

```java
final class CallExecuteOnSubscribe<T> implements OnSubscribe<Response<T>> {
  private final Call<T> originalCall;

  CallExecuteOnSubscribe(Call<T> originalCall) {
    this.originalCall = originalCall;
  }

  @Override public void call(Subscriber<? super Response<T>> subscriber) {
    // Since Call is a one-shot type, clone it for each new subscriber.
    // 克隆 call
    Call<T> call = originalCall.clone();
    // 创建 CallArbiter 的 producer
    CallArbiter<T> arbiter = new CallArbiter<>(call, subscriber);
    subscriber.add(arbiter);
    // 设置 subscriber 的 producer
    subscriber.setProducer(arbiter);

    Response<T> response;
    try {
      // 调用 call 的 execute() 方法
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

　　CallExecuteOnSubscribe 的方法做了四件事：

1. clone 了原来的 call，因为 okhttp3.call 是只能用一次的，所以每次都是新 clone 一个进行网络请求；
2. 创建了一个叫做 CallArbiter 的 producer；
3. 把这个 producer 设置给 subscriber。
4. 调用 call 的 execute() 方法请求数据。

　　producer 的工作机制：大部分情况下 Subscriber 都是被动接收 Observable push 过来的数据，但要是 Observable 发的太快，Subscriber 处理不过来，那就有问题了，所有就有了一种 Subscriber 主动 pull 的机制，而这种机制就是通过 producer 实现的。给 Subscriber 设置 Producer 之后（通过 Subscriber#setProducer 方法）Subscriber 就会通过 Producer 向上流根据自己的能力请求数据（通过 Producer#request 方法），而 Producer 收到请求之后（通常都是 Observable 管理 Producer，所以 “相当于” 就是 Observable 收到了请求），再根据请求的量给 Subscriber 发数据。

#### 6.2.1. CallAbviter 

　　来看 CallAbviter 的 request 方法：

```java
  @Override public void request(long amount) {
    if (amount == 0) {
      return;
    }
    while (true) {
      int state = get();
      switch (state) {
        // 等待状态
        case STATE_WAITING:
          // 切换到请求状态
          if (compareAndSet(STATE_WAITING, STATE_REQUESTED)) {
            return;
          }
          break; // State transition failed. Try again.
				// 有回复了
        case STATE_HAS_RESPONSE:
          // 切换到结束状态
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
        // 调用了 subscriber 的 onNext 方法
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
        // 出现异常，调用了 subscriber 的 onError 方法
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
      // 如果处理完毕，调用了 subscriber 的 onComplete 方法
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

　　调用 onNext() 将 response 发送给下游，异常则调用 onError()，处理完毕则调用了 onComplete() ，这样就将请求的结果 response 发回了用户。

#### 6.2.2. OkHttpCall 的 execute() 方法

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
          // 如果 call = null，则创建 call 实例
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
	  // 调用 call.execute() 方法，并调用 parseResponse() 方法解析答案并返回
    return parseResponse(call.execute());
  }
```

　　主要包括三步：

1. 如果 call 为空，则调用 createRawCall() 方法创建 okHttp3.call，调用了 serviceMethod#toRequest() 方法获得请求的参数。
2. 调用 call.execute() 方法执行网络请求。
3. 调用 parseResponse() 方法解析网络请求返回的数据。

　　createRawCall() 函数中，调用了 serviceMethod.toRequest(args) 来创建 okHttp3.Request，而在其中，之前准备好的 parameterHandlers 就派上了用场。

　　然后再调用 serviceMethod.callFactory.newCall(request) 来创建 okHttp3.Call，之前准备好的 callFactory 同样也派上了用场，由于工厂在构造 Retrofit 对象时可以指定，所以也可以指定其他的工厂（例如 HttpUrlConnection 的工厂），来使用其他的底层 HttpClient 实现。

　　调用 okhttp3.Call#execute() 来执行网络请求，这个方法是阻塞的，执行完毕之后将返回收到的响应数据。收到响应数据之后，进行了状态码的检查，通过检查之后调用了 serviceMethod.toResponse(catchingBody) 来把响应数据转换为需要的数据类型对象。在 toResponse 函数中，之前准备好的 responseConverter 也派上了用场。

#### 6.2.3. OkHttpCall 的 parseResponse

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
      // 调用 ServideMthod 的 toResponse 方法，将其转为 T 类型
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

　　ServiceMethod 的 toResponse 方法最终会调用到调用设置的 GsonRequestBodyConverter的 convert 方法，将 response 处理后返回。

　　经过一连串的处理，最终在 OkHttpCall.enqueue() 方法中生成 okhttp3.call 交给 OkHttpClient 去发送请求，再由配置的 Converter 处理 Response，返回给 SimpleCallAdapter 处理，返回最终所需要的 Observable。

#### 6.2.4. GsonResponseBodyConverter 的 convert 方法

```java
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  private final Gson gson;
  private final TypeAdapter<T> adapter;

  GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody value) throws IOException {
    // 使用 GSON 将 ResponseBody 转换 T
    JsonReader jsonReader = gson.newJsonReader(value.charStream());
    try {
      return adapter.read(jsonReader);
    } finally {
      value.close();
    }
  }
}
```

　　根据目标类型，利用 Gson#getAdapter 获取相应的 adapter，转换时利用 Gson 的 API 即可。

### 6.3. CallEnqueueOnSubscribe 类

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

　　做了四件事：

1. clone 了原来的 call，因为 okhttp3.call 是只能用一次的，所以每次都是新clone 一个进行网络请求；
2. 创建了一个叫做 CallArbiter 的 producer；
3. 把这个 producer 设置给 subscriber。
4. 调用 call 的 enqueue() 方法请求数据。（就这里和 execute 不同）

#### 6.3.1. OkHttpCall 的 enqueue 方法

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
	  // 请求
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

　　OkHttpCall 的 enqueue 方法 和 execute 方法实现基本相同，唯一不同的就是调用的 call 的方法不同，也就对应的 OkHttpClient 的方法不同。

## 7. retrofit-adapters 模块

　　retrofit 模块内置了 DefaultCallAdapterFactory 和 ExecutorCallAdapterFactory ，它们都适用于 API 方法得到的类型为 retrofit2.Call 的情形，前者生产的 adapter 啥也不做，直接把参数返回，后者生产的 adapter 则会在异步调用时在指定的 Executor 上执行回调。

　　retorfit-adapters 的各个子模块则实现了更多的工厂：GuavaCallAdapterFactory、Java8CallAdapterFactory 和 RxJavaCallAdapterFactory。

　　RxJavaCallAdapterFactory#getCallAdapter 方法中对返回值的泛型类型进行了进一步检查，例如声明的返回值类型为 Observable< List< Repo > >，泛型类型就是 List< Repo >，所有类型都由 RxJavaCallAdapter 负责转换。

## 8. retrofit-converters 模块

　　retrofit 模块内置了 responseBodyConverter、requestBodyConverter 和 StringConverter，，只能处理 ResponseBody、ResponseBody 和 String 类型的转换（实际上不需要转），API 方法中除了 @Body 和 @Part 类型的参数，都利用 stringConverter 进行转换，而 @Body 和 @Part 类型的参数则利用 requesyBodyConverter 进行转换。而 retrofit-converters 中的子模块则提供了 JSON、XML、ProtoBuf 等类型数据的转换功能，而且还有多种转换方式可以选择。

　　这三种 converter 都是通过 “ 询问 ” 工厂列表进行提供，而工厂列表可以在构造 Retrofit 对象时进行添加。

## 9. 问题

1. 调用接口的方法后是怎么发送请求的？这背后发生了什么？

   Retrofit 使用了动态代理给定义的接口设置了代理，当调用接口的方法时，Retrofit 会拦截下来，然后经过一系列处理，比如解析方法的注解等，生成了 call Request 等 OkHttp 所需的资源，最后交给 OkHttp 去发送请求，此间经过 callAdapter、converter 的处理，最后拿到所需要的数据。

2. Retrofit 与 OkHttp 是怎么合作的？

   在 Retrofit 中，ServiceMethod 承载了一个 Http 请求的所有参数，OkHttpCall 为 okhttp3.call 的组合包装，由它们两合作，生成用于 OkHttp 所需的 Request 以及 okhttp3.call，交给 OkHttp 去发送请求。

   可以说 Retrofit 为 OkHttp 再封装了一层，并添加了不少功能以及扩展，减少了开发使用成本。

3. Retrofit 中的数据究竟是怎么处理的？它是怎么返回 RxJava.Observable 的?

   Retrofit 中的数据其实是交给了 callAdapter 以及 converter 去处理，callAdapter 负责把 okHttpCall 转为用户所需的 Observable 类型，converter 负责把服务器返回的数据转成具体的实体类。

## 10. 总结

1. Retrofit 是使用动态代理 Proxy 对定义的接口进行处理的，当调用接口的方法时，会在动态代理的 InvocationHandler # invoke 方法对请求进行处理。 
2. ServiceMethod 会解析接口的方法，将方法的注解解析为请求的 Request，根据用户设置配置生成具体的 CallAdapter、ResponseConverter，将请求的结果使用 ResponseConverter 转为合适的 R 对象。 ServiceMethod 类的作用就是把对接口方法的调用转为一次 HTTP 调用，而且 ServiceMethod 有缓存，减少了一定的消耗。
3. 实际是调用了 call 的 exercute()（同步） 或者 enqueue()（异步） 来完成请求。OkHttpCall 算是 OkHttp 的包装类，用它跟 OkHttp 对接。会在 OkHttpCall 中将 OkHttp 的 response 包装成 retrofit 标准下的 response，再使用 RespouseConverter 转成想要的 R 对象。默认是 OkHttpClient，当然还可以扩展一个新的 Call ，比如 HttpUrlConnectionCall。
4. Retrofit 提供了很多的 ConverterFactory，比如 Gson、Jackson、xml、protobuff 等等，需要什么，就配置相对应的工厂，在 Service 方法上声明泛型具体类型就可以了。
5. 生成的 CallAdapter 有四个工厂，分别对应不同的平台：RxJava、Java8、Guava 还有一个 Retrofit 默认的。简单来说就是用来将 Call 转成 T 的一个策略。因为这里具体请求时耗时操作，所以需要 CallAdapter 去管理线程。比如 RxJava 会根据调用方法的返回值，如 Response < T > | Result < T > | Observable < T >，生成不同的 CallAdapter。实际上就是对 RxJava 的回调方式做封装。比如将 response 再拆解为 success 和 error 等。

## 11. 参考文章
1. [Retrofit是如何工作的？](https://www.jianshu.com/p/cb3a7413b448)

2. [拆轮子系列：拆 Retrofit](https://blog.piasy.com/2016/06/25/Understand-Retrofit/index.html)

