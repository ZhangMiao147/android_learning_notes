# 常用的开源库

## 1. 网络

　　解决各种协议（GET,POST,PUT,HEAD,DELETE...）的网络数据的获取及请求，支持异步、同步请求；文件多线程下载断点续传、上传；请求自动重试，gzip 压缩，Cookies 自动解析并持久化，数据的缓存。目标是让网络请求更方便、简洁、高效、稳定。

| 名称 | 概要 | 详情 |
|--------|--------|--------|
| [Retrofit2.0]() | 以接口/注解的形式定义请求和响应 | Square 开源的项目。是一套 RESTful 家口的 Android(Java) 客户端实现，基于注解，提供 JSON to POJO(Plain Ordinary Java Object，简单的 Java 对象)，POJO to JSON，网络请求（POST,GET,PUT,DELETE等）封装。本身的网络核心可以替换，如 Apache HTTP Client,URL connection,OKHTTP 等，数据解析核心也可以替换如 Gson,Jackson,fastjson,xStream等。力求用最少的代码，实现最强大的功能。 [官网地址](http://square.github.io/retrofit/)|
| [okhttp](https://github.com/square/okhttp) | 一个为安卓和 java 应用诞生的 Http+SPDY 的网络处理库 | square 开源项目。支持 HTTP,HYTTPS,HTTP/2.0 和 SPDY 协议；自动缓存数据，节省流量；内部自动 GZIP 压缩内容。 |
| [android-async-http](https://github.com/loopj/android-async-http) | 一个异步的 AndroidHttp 库 | 比较经典的网络请求库，基于 Apache 的 HttpClient 库实现，但是由于 Android M(6.0)去除了对 HttpClient 相关 API，意味着 google 不再推荐使用。 |
| [Volley](https://github.com/mcxiaoke/android-volley) | 一个能让 Android 的网络请求更简单快捷的 Http 库 | Volley 集成了 AsyncHttpClient 和 Universal-Image-Loader 的优点，既可以像 AsyncHttpClient 一样非常简单地进行 HTTP 通信，也可以像 Universal-Image-Loader 一样轻松加载网络上的图片。但是对大数据量的网络操作如文件的下载支持较差。 |



#### 1.1 Retrofit

* [Retrofit 官方文档翻译](https://github.com/ZhangMiao147/android_learning_notes/blob/master/OpenSourceLibrary/Retrofit%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3%E7%BF%BB%E8%AF%91.md)

#### 1.2 okhttp



## 2. 图片加载

　　解决网络、文件、res、assets 等图片的获取、解析、展示、缓存等需求...

| 名称 | 概要 | 详情 |
|--------|--------|--------|
| [Picasso](https://github.com/square/picasso) | 强大的图片下载和缓存库 | Square 开源的项目，主导者是 JakeWharton。 |
| [Glide](https://github.com/bumptech/glide) | Google 推荐的图片加载和缓存的库 | 专注于平滑滚动时的流畅加载，Google 开源项目，2014 年 Google I/O 上被推荐 |
| [Fresco](https://github.com/facebook/fresco) | Facebook 推荐的 Android 图片加载库 | 自动管理图片的加载和图片的缓存。Facebook 在 2015 年上半年开源的图片加载库 |
| [Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader) | 早期广泛使用的开源图片加载库 | 强大又灵活的 Adroid 库，用于加载、缓存、显示图片。 |
| [Volley](https://github.com/mcxiaoke/android-volley) | 2013 年 Google I/O 推荐的网络通讯框架 | 使用 Volley 加载网络图片，主要用到其中的 ImageLoader，NetworkImageView 类，注意它不仅仅是个图片加载库。 |
| [Cube-sdk](https://github.com/etao-open-source/cube-sdk) | 轻量级的 Android 开发框架 | 高效方便地加载网络图片，更简易地处理网络 API 请求。 |



#### 2.1 Picasso

#### 2.2 Glide


#### 2.3 Fresco


#### 2.4 Android-Universal-Image-Loader



## 3. 图片处理库
　　解决图片缩放、裁剪、平移、旋转等需求。

| 名称 | 概要 | 详情 |
|--------|--------|--------|
| [PinchImageView](https://github.com/boycy815/PinchImageView) | 体验很好的图片手势控件 | 支持双击放大、双击缩小，超出边界会回弹，滑动惯性，不同分辨率无缝切换，可与 ViewPager 结合使用。 |
| [GestureViews](https://github.com/alexvasilkov/GestureViews) | 包含 ImageView 的自定义 FrameLayout | 项目的目的是让图片的查看尽可能流畅平滑，让开发者更加方便地集成到自己的应用中，支持手势控制和动画。 |
| [PhotoView](https://github.com/chrisbanes/PhotoView)  | 致力于帮助开发者高效的创建可缩放的 ImageView | 重写 ImageView 的实现，支持多点触摸的图片缩放。 |
| [subsampling-scale-image-view](https://github.com/davemorrissey/subsampling-scale-image-view) | 一个 Android 自定义图片视图，专为图片画廊设计 | 丰富的配置选项，更方便的实现图片的手势缩放、旋转、平移。无损展示大图，完美地嵌入画廊、地图等。可显示大图（地图、建筑设计图）等而不造成 OutOfMemoryErrors(OOM n内存溢出异常) |
| [TouchImageView](https://github.com/MikeOrtiz/TouchImageView) | 一个 ImageView 的扩展类 | 支持 ImageView 所有功能，添加了平移、缩放、拖拽、滑动、双击缩放等动画。 |
| [ImageViewZoom](https://github.com/sephiroth74/ImageViewZoom) | 自定义 ImageView 控件 | 一个可以缩放、平移的自定义 ImageView 控件。 |


#### 3.1 PhotoView


