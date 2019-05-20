# Retrofit

## 简介


|  | Retrofit 简介 |
|--------|--------|
| 介绍 | 一个 RESTful 的 HTTP 网络请求框架（基于 OKHTTP） |
| 作者 | Square |
| 功能 | 1.基于 okhttp & 遵循 Restful API 设计风格； 2.通过注解配置网络请求参数； 3.支持同步 & 异步网络请求； 4.支持多种数据的解析 & 序列化格式（Gson、Json、XML、Protobuf）； 5.提供对 RxJava 支持。 |
| 优点 | 1.功能强大：支持同步 & 异步、支持多种数据的解析 & 序列化格式、支持 RxJava；2.简洁易用：通过注解配置网络请求参数、采用大量设计模式简化使用；3.可扩展性好：功能模块高度封装、解耦彻底，如自定义 Converters 等等。 |
| 应用场景 | 任何网络请求的需求场景都应优先选择（特别是后台 API 遵循 Resful API 设计风格 & 项目中使用到 RxJava） |

　　特别注意：
* 准确来说，Retrofit 是一个 RESTful 的 HTTP 网络请求框架的封装。
* 原因：网络请求的工作本质上是 OkHttp 完成，而 Retrofit 仅负责网络请求接口的封装。
![](./Retrofit框架.png)
* App 应用程序通过 Retrofit 请求网络，实际上是使用 Retrofit 接口层封装请求参数、Header、Url 等信息，之后由 OkHttp 完成后续的请求操作。
* 在服务端返回数据之后，OkHttp 将原始的结构交给 Retrofit，Retrofit 根据用户的需求对结果进行解析。

## 与其他开源请求库对比
　　除了 Retrofit，如今 Android 中主流的网络请求框架有：
* Android-Asynv-Http
* Volley
* OkHttp

　　网络请求开源库对比：

| 网络请求库/对比 | android-async-http | Volley | OkHttp | Retrofit |
|--------|--------|--------|--------|--------|
| 作者 | Loopj | Google | Square | Square |
| 面世时间 | android-async-http > Volley > OkHttp > Retrofit |
| 人们使用情况（GitHub start书） | Volley > android-async > OkHttp > Retrofit |
| 功能 | 1.基于 HttpClient；2.在 UI 线程外、异步处理 Http 请求；3.在匿名回调中处理请求结果，callback 使用了 Android 的 Handler 发送消息机制在创建它的线程中执行；4.自动智能请求重试；5.持久化 cookie 存储，保存 cookie 到你的应用程序的 SharedPreferences。 | 1.基于 HttpUrlConnection；2.封装了 UIL 图片加载框架，支持图片加载；3.网络请求的排序、优先级处理；4.缓存；5.多级别取消请求；6.Activity 和生命周期的联动（Activity 结束时同时取消所有网络请求）。 | 1.高性能 Http 请求库，可把它理解成一个封装之后的类似 HttpUrlConnection 的一个东西，属于同级并不时与上述两种；2.支持 SPDY，共享同一个 Socket 来处理同一个服务器的所有请求；3.支持 http 2.0、websocket；4.支持同步、异步；5.封装了线程池、数据转换、参数使用、错误处理等；6.无缝的支持 GZIP 来减少数据流量；7.缓存响应数据来减少重复的网络请求；8.能从很多常用的连接问题中自动恢复；9.解决了代理服务器问题和 SSL 握手是把你问题。 | 1.基于 OkHttp；2.RESTful API 设计风格；3.支持同步、异步；4.通过注解配置请求，包括请求方法、请求参数、请求头、返回值等；5.可以搭配多种 Converter 将获得的数据解析 & 序列化，支持 Gson(默认)、Jackson、Protobuf 等；6.提供对 RxJava 的支持。 |
| 性能 | 1.作者已经停止对该项目维护；2.Android 5.0 后不推荐使用 HttpClient；所以不推荐在项目中使用。 | 1.可扩展性好：可支持 HttpClient、HttpUrlConnection 和 OkHttp。 | 1.基于 NIO 和 Okio，所以性能更好：请求、处理速度快（IO：阻塞式；NIO：非阻塞式；Okio 是 Square 公司基于 IO 和 NIO 基础上做的一个更简单、高效处理数据流的一个库）。 | 1.性能最好，处理最快；2.扩展性差，高度封装所带来的必然后果；解析数据都是使用的统一的 converter，如果服务器不能给出统一的 API 的形式，将很难进行处理。 |
| 开发者使用 | 1.作者已经停止对该项目维护；2.Android 5.0 后不推荐使用 HttpClient；所以不推荐在项目中使用。 | 1.封装性好：简单易用。 | 1. API 调用更加简单、方便；2.使用时需要进行多一层封装。 | 1.简洁易用（RestfulAPI 设计分割）；2.代码简洁（更加高度的封装性和注解用法）；3.解耦的更彻底、职责更细分；4.易与其他框架联合使用（RxJava）；5.使用方法较多，原理复杂，存在一定门槛。 |
| 应用场景 |  1.作者已经停止对该项目维护；2.Android 5.0 后不推荐使用 HttpClient；所以不推荐在项目中使用。 | 1.适合轻量级网络交互：网络请求频繁、传输数据量小；2.不能进行大数据量的网络操作（比如下载视频、音频），所以不适合用来上传文件。 | 1.重量级网络交互场景：网络请求频繁、传输数据量大（其实会更推荐 Retrofit，反正 Retrofit 是基于 Okhttp 的）。 | 1.任何场景下优先选择，特别是：后台 Api 遵循 RESTful 的风格 & 项目中使用 RxJava。 |
| 备注 |  | Volley 的 request 和 response 都是把数据放到 byte 数组里，不支持输入输出流，把数据放到数组中，如果大文件多了，数组就会非常的大且多，消耗内存，所以不如直接返回 Stream 那样具备可操作性，比如下载一个大文件，不可能把整个文件都缓存内存之后再写到文件里。 | Android 4.4 的源码中可以看到 HttpURLConnection 已经替换成 OkHttp 实现了，所以有理由相信 OkHttp 的强大。 |  |


## 参考文章
[这是一份很详细的 Retrofit 2.0 使用教程（含实例讲解）](https://blog.csdn.net/carson_ho/article/details/73732076)
[Retrofit分析-漂亮的解耦套路](https://www.jianshu.com/p/45cb536be2f4)
[Retrofit是如何工作的？](https://www.jianshu.com/p/cb3a7413b448)



