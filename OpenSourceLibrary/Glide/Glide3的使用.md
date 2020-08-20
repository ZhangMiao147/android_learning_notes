# Glide 3 的使用

# 1. 概述

　　Android 上的图片加载框架非常成熟，从最早的老牌图片加载框架 UniversalImageLoader，到后来 Google 推出的 Volley，再到后来的 Glide 和 Picasso，还有 Facebook 的 Fresco。每一个都非常稳定，功能也都十分强大。但是它们的使用场景基本都是重合的，基本只需要选择其中一个来进行学习和使用就足够了。

　　从易用性上来讲，Glide 和 Picasso 应该都是完胜其他框架的，这两个框架都实在是太简单好用了，大多数情况下加载图片都是一行代码就能解决的，而 UniversalImageLoader 和 Fresco 则在这方面略逊一些。

　　Glide 和 Picasso 这两个框架的用法非常相似，但其实它们各有特色。Picasso 比 Glide 更加简洁和轻量，Glide 比 Picasso 功能更为丰富。

# 2. 引入库

　　Glide 是一款由 Bump Technologies 开发的图片加载框架，可以在 Android 平台上以极度简单的方式加载和展示图片。

　　要想使用 Glide，首先需要将这个库引入到项目当中，在 app/build.gradle 文件当中添加如下依赖：

```groovy
dependencies {
    compile 'com.github.bumptech.glide:glide:3.7.0'
}
```

　　另外，Glide 中需要用到网络功能，因此还得在 AndroidManifest.xml 中声明一下网络权限才行：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

　　这样就可以使用 Glide 中的功能了。

# 3. 使用

## 3.1. 加载图片

　　在 Activity 中使用 Glide 显示图片：

```java
Glide.with(Context context).load(Strint url).into(ImageView imageView);
```

　　首先调用 Glide.with() 方法用于创建一个加载图片的实例。

　　with() 方法可以接收 Context、Activity 或者 Fragment 类型的参数。也就是说选择的范围非常广，不管是在 Activity 还是 Fragment 中调用 with() 方法，都可以直接传 this。那如果调用的地方既不在 Activity 中也不在 Fragment 中也没有关系，可以获取当前应用程序的 ApplicationContext，传入到 with() 方法当中。

　　注意 with() 方法中传入的实例会决定 Glide 加载图片的生命周期，如果传入的是 Activity 或者 Fragment 的实例，那么当这个 Activity 或 Fragment 被销毁的时候，图片加载也会停止。如果传入的是 ApplicaationContext，那么只有当应用程序被杀掉的时候，图片加载才会停止。

　　load() 方法用于指定待加载的图片资源。Glide 支持加载各种各样的图片资源，包括网络图片、本地图片、应用资源、二进制流、Uri 对象等的。因此 load() 方法也有很多个方法重载。

```java
// 加载网络图片
String url = "http://cn.bing.com/az/hprichbg/rb/Dongdaemun_ZH-CN10736487148_1920x1080.jpg";
Glide.with(this).load(url).into(imageView);

// 加载本地图片
File file = new File(getExternalCacheDir() + "/image.jpg");
Glide.with(this).load(file).into(imageView);

// 加载应用资源
int resource = R.drawable.image;
Glide.with(this).load(resource).into(imageView);

// 加载二进制流
byte[] image = getImageBytes();
Glide.with(this).load(image).into(imageView);

// 加载 Uri 对象
Uri imageUri = getImageUri();
Glide.with(this).load(imageUri).into(imageView);
```

　　into() 方法中传入图片显示的 ImageView 的实例，将图片显示在这个 ImageView 上。into() 方法不仅仅是只能接收 ImageView 类型的参数，还支持很多更丰富的用法。

　　Glide 的关键三步是：先 with()、再 load()、最后 into()。

## 3.2. 加载占位图

　　Glide 提供了各种各样非常丰富的 API 支持，其中就包括了占位图功能。

　　占位图就是指在图片的加载过程中，先显示一张临时的图片，等图片加载出来了再替换要加载的图片。

```java
Glide.with(this)
     .load(url)
     .placeholder(R.drawable.loading)
     .into(imageView);
```

　　调用 placeholder() 方法，将占位图的资源 id 传到这个方法中即可。

　　Glide 当中绝大多数 API 的用法就是在 lode() 和 into() 方法之间串接任意想添加的功能就可以了。

## 3.3. 异常占位图

　　异常占位图是指如果因为某些异常情况导致图片加载失败，比如说手机网络信号不好，这个时候就显示这张异常占位图。

```java
Glide.with(this)
     .load(url)
     .error(R.drawable.error)
     .into(imageView);
```

　　加上 error() 方法就可以指定异常占位图了。

## 3.4. 指定图片格式

　　Glide 是支持 GIF 图片的，而 Picasso 是不支持加载 GIF 图片的。

　　而使用 Glide 加载 GIF 图片并不需要编写什么额外的代码，Glide 内部会自动判断图片格式。

　　也就是说，不管传入的是一张普通图片，还是一张 GIF 图片，Glide 都会自动进行判断，并且可以正确地把它解析并展示出来。

### 3.4.1. 指定加载静态图片

　　如果希望加载的图片是一张静态图片，不需要 Glide 自动判断它是静态图片还是 GIF 图片:

```java
Glide.with(this)
     .load(url)
     .asBitmap()
     .into(imageView);
```

　　加入了一个 asBitmap() 方法，这个方法的意思就是说这里只允许加载静态图片，不需要 Glide 自动进行图片格式的判断了。

　　调用了 asBitmap() 方法后，GIF 图就无法正常播放了，而是会在界面上显示第一帧的图片。

### 3.4.2. 指定加载动态图片

　　类似的，也能强制指定加载动态图片：

```java
Glide.with(this)
     .load(url)
     .asGif()
     .into(imageView);
```

　　加入了一个 asBitmap() 方法，这个方法的意思就是说这里只允许加载动态图片。

　　指定了只允许加载动态图片，如果传入了一个静态图片的 URL 地址，结果是加载失败。

## 3.5. 指定图片大小

　　实际上，使用 Glide 在绝大多数情况下都是不需要指定图片大小的。

　　Glide 从来都不会直接将图片的完整尺寸全部加载到内存中，而是用多少加载多少。Glide 会自动判断 ImageView 的大小，然后只将这么大的图片像素加载到内存当中，从而节省内存开支。

　　所以说 Glide 在绝大多数情况下都是不需要指定图片大小的，因为  Glide 会自动根据 ImageView 的大小来决定图片的大小。

　　如果必须给图片指定一个固定的大小：

```java
Glide.with(this)
     .load(url)
     .placeholder(R.drawable.loading)
     .error(R.drawable.error)
     .diskCacheStrategy(DiskCacheStrategy.NONE)
     .override(100, 100)
     .into(imageView);
```

　　使用 override() 方法指定一个图片的尺寸，这样 Glide 只会将图片加载成指定大小像素的尺寸，而不会管 ImageView 的大小是多少了。

## 3.6. 关闭硬件缓存

　　Glide 有非常强大的缓存机制，第一次加载的时候会把图片缓存下来，下次加载的时候将会直接从缓存中读取，不会再去网络下载，因而加载的速度非常快。

```java
//DiskCacheStrategy.NONE： 表示不缓存任何内容。
//DiskCacheStrategy.SOURCE： 表示只缓存原始图片。
//DiskCacheStrategy.RESULT： 表示只缓存转换过后的图片（默认选项）。
//DiskCacheStrategy.ALL ： 表示既缓存原始图片，也缓存转换过后的图片。
Glide.with(this)
     .load(url)
	 .diskCacheStrategy(DiskCacheStrategy.NONE)
     .into(imageView);
```

　　加上 diskCacheStrategy() 方法，并传入 DiskCacheStrategy.NONE 参数，这样就可以禁用掉 Glide 的缓存功能。

# 4. 参考文章

1. [Android图片加载框架最全解析（一），Glide的基本用法](https://blog.csdn.net/guolin_blog/article/details/53759439)

2. [Glide使用总结](https://www.jianshu.com/p/791ee473a89b)
