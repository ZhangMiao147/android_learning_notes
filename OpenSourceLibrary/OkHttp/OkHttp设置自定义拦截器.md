# OkHttp 设置自定义拦截器

　　OkHttp 的使用是直接用户自定义拦截器的，而且自定义的拦截器会最先执行，最后处理响应结果。

## 1 自定义拦截器

　　自定义拦截器主要的逻辑就是：

1. 实现 Interceptor 接口，重写 intercept(Interceptor.Chain chain) 方法，在使用责任链的时候，可以调用自定义拦截器的处理。

2. 调用 Response response = chain.proceed(request) 调用下一个拦截器，并获取响应结果。

　　下面是一个 log 拦截器的实现，在 intercept 的操作分为三部分：

1. 获取请求信息，打印
2. 调用下一个拦截器
3. 获取响应信息，打印

```java
    /**
     * 打印日志使用
     */
    public static final class LoggerInterceptor implements Interceptor {
        private String tag;

        public LoggerInterceptor(String tag) {
            this.tag = tag;
        }
		
        // 重写 intercept 方法
        @Override
        public Response intercept(Chain chain) throws IOException {
            // 1.获取请求信息，打印
            Request request = chain.request();

            long t1 = System.nanoTime();
            LogUtil.i(tag, String.format("Sending request %s on %s%n%s\n%s",
                    request.url(), chain.connection(), request.headers(), request.body()));
			// 2. 调用下一个拦截器
            // response 就是响应信息
            Response response = chain.proceed(request);
			// 3. 获取响应信息，打印
            long t2 = System.nanoTime();
            LogUtil.i(tag, String.format("Received response for %s in %.1fms%n%s\n%s",
                    response.toString(), (t2 - t1) / 1e6d, response.headers(),response.body()));
            return response;
        }
    }
```

## 2 使用自定义拦截器

　　使用自定义拦截器是通过 OkHttpCilent.Builder() 来配置的，有两种方式：

1. addInterceptor()

```java
client = new OkHttpClient.Builder()
        .addInterceptor(new LoggerInterceptor())
        .build();
```

　　addInterceptor() 方法无需担心中间响应，例如重定向和重试。即使从缓存提供 HTTP 响应，也总是被调用一次。遵守应用程序的原始意图，不关心 OkHttp 注入的标头，例如 If-None-Match，允许短路而不是 chain.proceed()，允许重试并多次调用 chain.proceed()。

2. addNetworkInterceptor()

```java
client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new LoggerInterceptor())
        .build();
```

　　addNetworkInterceptor() 方法能够对诸如重定向和重试之类的中间响应进行操作，不会再读取缓存时调用，观察数据，就像通过网络传输数据一样，访问 Connection 带有请求的。

　　addInterceptor 和 addNetworkInterceptor 主要的区别是 addInterceptor 是最先执行的拦截器，addNetworkInterceptor 是在 ConnectInterceptor 之后执行的拦截器。

## 3 参考文章

[okhttp之自定义拦截器](https://www.jianshu.com/p/56b8d513fc93)