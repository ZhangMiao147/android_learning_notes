# OkHttp 基础知识

## 1. 概述

　　从 Android 4.4 之后，系统已经抛弃了原生的 HttpUrlConnection 的内部实现，转而是封装了 OkHttp 的实现，HttpUrlConnection 对外只是一层接口供开发者来调用。而使用 HttpUrlConnection 还是 Volley，其实都是在间接使用 OkHttp。

　　OkHttp 是一个用于进行 Http / Http2 通信的客户端，并且同时适用于 Android 和 Java。

### 1.1. 特点

　　OkHttp 是一个非常强大而有效的网络架构，其主要特点在于：

* 对于同一个主机的所有请求，允许其在 Http /Http2 上共享同一个套接字，这就避免了重复的 TCP 连接带来的 3 次握手的时间。
* 对于 Http 协议，其支持连接池用于减少请求延迟。
* 数据都使用了 gzip 压缩传输，从而减少网络传输 size 的大小。
* 对响应进行缓存，避免缓存有效期内重复的网络请求。
* 弱网情况下，在连接失败后，OkHttp 会自动进行重试，特别是有备用地址时还会通过备用地址进行连接。而安全上，其支持新一代的 TLS 功能、SNL 和 ALPN，如果服务器不支持的化则会自动降级到 TLS 1.0。

　　OkHttp 的使用是很简单的，它在 request/reponse API 上采用了链式 Builder 的设计模式，使得它具备一旦构建便不可修改性。

　　OkHttp 还支持同步和异步请求。其实网络请求的实现原理上也是一次 I/O 通信，并且还是同步的 I/O。

## 2. 使用

### 2.1. 使用 gradle 中集成

```ruby
implementation 'com.squareup.okhttp3:okhttp:3.10.0'
```

### 2.2. Http Get

```java
// 创建 okHttpClient 实例
OkHttpClient mOkHttpClient = new OkHttpClient();
// 通过链式 Builder 设计提的 Builder 创建一个Request
final Request request = new Request.Builder()
             .url("https://github.com/hongyangAndroid")
             .build();
// new call
Call call = mOkHttpClient.newCall(request); 
// 请求加入调度
// 异步请求
call.enqueue(new Callback()
        {
          	// 请求失败
            @Override
            public void onFailure(Request request, IOException e)
            {
            }
			// 请求成功
            @Override
            public void onResponse(final Response response) throws IOException
            {
               String htmlStr =  response.body().string();
            }
        });   
```

### 2.3. Http Post

```java
Request request = buildMultipartFormRequest(
        url, new File[]{file}, new String[]{fileKey}, null);
// 构建 Body
FormEncodingBuilder builder = new FormEncodingBuilder();   
builder.add("username","张三");
// 构建 Request，对于 post 请求，除了设置 url 还需要设置 post(body)
Request request = new Request.Builder()
                   .url(url)
               	   .post(builder.build())
                   .build();
//执行一个异步请求。
 mOkHttpClient.newCall(request).enqueue(new Callback(){});
```

　　post 的时候，参数是包含在请求体中的，通过 FormEncodingBuilder 添加多个 String 键值对，然后去构造 RequestBody，最后完成 Request 的构造。 

### 2.4. 基于 Http 的文件上传

```java
// 文件
File file = new File(Environment.getExternalStorageDirectory(), "balabala.mp4");
// 构建 Body
RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

RequestBody requestBody = new MultipartBuilder()
     .type(MultipartBuilder.FORM) // 表单上传
     .addPart(Headers.of(
          "Content-Disposition", 
              "form-data; name=\"username\""), 
          RequestBody.create(null, "张三"))
     .addPart(Headers.of(
         "Content-Disposition", 
         "form-data; name=\"mFile\"; 
         filename=\"wjd.mp4\""), fileBody)
     .build();

Request request = new Request.Builder()
    .url("http://192.168.1.103:8080/okHttpServer/fileUpload")
    .post(requestBody)
    .build();
// 发送异步请求
Call call = mOkHttpClient.newCall(request);
call.enqueue(new Callback()
{
    //...
});
```

　　通过 MultipartBuilder 的 addPart 方法可以添加键值对或者文件。

## 3. 参考文章

1. [Android OkHttp完全解析 是时候来了解OkHttp了](https://blog.csdn.net/lmj623565791/article/details/47911083)

2. [OkHttp深入分析——基础认知部分](https://www.jianshu.com/p/b38bd9d1ae76) 