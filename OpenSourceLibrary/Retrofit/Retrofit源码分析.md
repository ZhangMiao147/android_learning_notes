# Retrofit 源码分析

　　Retrofit 的主要原理是利用了 Java 的动态代理技术，把 ApiService 的方法调用集中到了 InvocationHandler.invoke，在构建了 ServiceMethod 、OkHttpClient，返回 callAdapter.adapter 的结果。

　　Retrofit 的最大特点就是解耦。

## 基本使用

```java
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://test")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Observable<ApiService.BaseResponse> observable = service.getMessage(1);
		...

```

　　Retrofit 就这样经过简单的配置后就可以向服务器请求数据了，超级简单。

## 分析

### Retrofit.create 方法分析

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
          // 平台的抽象，指定默认的 Callback
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

