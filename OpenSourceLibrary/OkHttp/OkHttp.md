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

　　post 的时候，参数时包含在请求体中的，通过 FormEncodingBuilder 添加多个 String 键值对，然后去构造 RequestBody，最后完成 Request 的构造。 

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

　　通过 MultipartBuilder 的 addPart 放啊可以添加键值对或者文件。

## 参考文章

[Android OkHttp完全解析 是时候来了解OkHttp了](https://blog.csdn.net/lmj623565791/article/details/47911083)



