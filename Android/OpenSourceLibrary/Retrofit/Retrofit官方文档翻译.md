# Retrofit官方文档翻译

## 介绍
　　Retrofit 将你的 HTTP API 转换为 Java 接口。
```java
public interface GitHubService {
  	@GET("users/{user}/repos")
  	Call<List<Repo>> listRepos(@Path("user") String user);
}
```
　　Retrofit 类会生成一个 GitHubService 接口的实现对象。
```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .build();

GitHubService service = retrofit.create(GitHubService.class);
```
　　创建的 GitHubService 中的每一个 Call 都会向远程 Web 服务端发送一个同步或异步的 HTTP 请求。
```java
	Call<List<Repo>> repos = service.listRepos("octocat");
```
　　使用注解描述 HTTP 的请求：
* URL 参数替换和查询参数支持
* 对象转换为请求体（如， JSON ，协议缓存区）
* 多方面请求体和文件上传

## API 声明
　　接口方法及其参数注释说明如何处理请求。

#### 请求方法
　　每个方法必须有一个提供请求方法和相关的 URL 的 HTTP 注解。有 5 个内置的注解：`GET`、`POST`、`PUT`、`DELETE`和`HEAD`。资源相关的 URL 由注解指定。
```java
	@GET("users/list")
```
　　还可以在 URL 中指定查询参数。
```java
	@GET("users/list?sort=desc")
```

#### URL 操作
　　可以使用替换块和方法的参数动态更新请求的URL。替换块是一个由 { 和 } 包裹的字符字符串。对应的参数必须使用同样的字符串 @Path 注解。
```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id") int groupId)
```
　　请求参数也可以增加。
```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id") int groupId,@Query("sort") String sort)
```
　　对于复杂的请求参数组合可以使用 `Map`。
```java
@GET("group/{id}/users")
Call<List<User>> groupList(@Path("id")int groupId,@QueryMap Map<String,String> options);
```

#### 请求体
　　@Body 注解被指定作为 HTTP 的请求体去使用。
```java
@POST("users/new")
Call<User> createUser(@Body User user)
```
　　对象可以在 `Retrofit` 实例中使用指定的转换器来进行转换。如果不添加转换器，则只能使用 `RequestBody` 。

#### 格式转码和表单
　　也可以声明方法发送格式变化和表单数据。　　

　　当方法中提供了 `@FormUrlEncoded` 时，发送表单数据。`@Field` 的每一个键值对注解包含名称和提供的值。

```java
@FormUrlEncoded
@POST("user/edit")
Call<User> updateUser(@Field("first_name") String first,@Field("last_name") String last)
```
　　当方法中提供了 `@Multipart` 时，表单请求被使用。 单子声明使用 `@Part` 注解。
```java
@Multipart
@PUT("user/photo")
Call<User> updateUser(@Part("photo") RequestBody photo,@Part("description") RequestBody description);
```
　　表单部分使用 `Retrofit` 的一个转换器，也可以通过实现 `RequestBody` 处理它们自己的序列化。

#### 头部操作
　　可以使用 `@Headers` 注解为方法设置静态的头。
```
@Headers("Cache-Control: max-age=640000")
@GET("widget/list")
Call<List<Widget>> widgetList();
```
```
@Headers({
	"Accept: application/vnd.github.v3.full+json",
	"User-Agent: Retrofit-Sample-App"
})
@GET("users/{username}")
Call<User> getUser(@Path("username") String username);
```
　　注意，头不会覆盖彼此，相同名字的所有头都被包含在请求中。

　　一个请求头可以使用 `@Header` 注解动态更新。相应的参数必须提供 `@Header`。如果该值为空，头将会被忽略。否则，在数值上调用 `toString` 并且使用结果。

```
	@GET("user")
	Call<User> getUser(@Header("Authorization") String authorization)
```
　　与查询参数相同，对于复杂的头组合，可以使用 `Map`。
```
	@GET("user")
	Call<User> getUser(@HeaderMap Map<String,String> headers)
```
　　可以使用 OkHttp 拦截器指定需要添加到每一个请求的头。

#### 同步与异步
　　调用实例可以同步或异步执行。每一个实例只能使用一次，但调用 `clone()` 将创建一个可以使用的新的实例。

　　在 Android ，回调将在主线程上执行，在JVM ,回调将发生在执行 HTTP 请求的统一线程上。

## Retrofit 配置
　　`Retrofit` 是将 API 接口转换为回调对象的类。默认情况下， `Retrofit` 将提供平台合适的默认值并且允许自定义。

#### 转换器
　　默认情况下，`Retrofit` 只能将 HTTP 主体反序列化为 `OkHttp` 的 `ResponseBody` 类型，并且只能接收 `@Body` 的 `ResponseBody` 类型。

　　可以添加转换器来支持其他类型。可以方便的使用 6 个常用的模块模式序列化库。

* Gson : com.squareup.retrofit2:cenverter-gson
* Jackson : com.squareup.retrofit2:converter-jackson
* Moshi : con.squareup.retrofit2:converter-moshi
* Protobuf : com.squareup.retrofit2:converter-protobuf
* Wire : com.squareup.retrofit2:cenver-wire
* Simple XML : com.squareup.retrofir2:converter-simplexml
* Scalars(primitives,boxed,and String):com.square.retrofit2:converter-scalars

　　下面是使用 GsonConverterFactory 类生成 GitHubService 接口实现的例子，该接口使用 Gson 进行反序列化。

```java
Retrofit retrofit = new Retrofit.Builder()
	.baseUrl("https://api.github.com")
	.addConverterFactory(GsonConverterFactory.create())
	.build()
GitHubService service = retrofit.create(GithubService.class);
```

#### 自定义转换器
　　如果需要与使用 Retrofit 不支持的内容格式（如 YAML 、 txt 、自定义格式）的 API 或者希望使用实现现有格式的不同库，可以轻松创建自己的转换器。创建继承 **Converter.Factory** 类的类并且在创建适配器时传入实例。

## 下载
　　Retrofit的源码、例子和解释都在 [Github](https://github.com/square/retrofit) 上。

#### MAVEN
```xml
<dependency>
<groupId>com.squareup.retrofit2</groupId>
<artifactId>retrofit</artifactId>
<version>(insert latest version)</version>
```

#### GRADLE
```xml
implementation 'com.squareup.retrofit2:retrofit:(insert latest version)'
```
　　Retrofit请求的最低是 Java 7 或 Android 2.3 。

#### R8/PROGUARD
　　如果使用 R8 或者 ProGuard ，则添加选项到文件中。

　　如果使用 OKHTTP 和 Okio 的库，也需要同样的方法。



## 官方文档地址

[retrofit](http://square.github.io/retrofit/)

























