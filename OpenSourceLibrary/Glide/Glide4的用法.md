# Glide 4 的用法

## 1. Glide 4 概述

　　相比于 Glide 3 的 API，Glide 4 进行了更加科学合理地调整，使得易读性、易写性、可扩展性等方便都有了不错地提升。

　　但如果已经对 Glide 3 非常熟悉地话，并不是就必须要切换到 Glide 4 上来，因为 Glide 4 上能实现地功能 Glide 3 也都能实现，而且 Glide 4 在性能方面也并没有什么提升。

 ## 2. Glide 4 的引入

　　要想使用 Glide，首先需要将这个库引入到项目当中。在 app/build.gradle 文件当中添加如下依赖：

```groovy
dependencies {
    implementation 'com.github.bumptech.glide:glide:4.4.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.4.0'
}
```

　　注意，相比于 Glide 3，这里要多添加一个 compile 的库，这个库是用于生成 Generated API 的。

　　另外，Glide 中需要用到网络功能，因此还得在 AndroidManifest.xml 中声明一下网络权限才行：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

　　就是这么简单，然后就可以自由的使用 Glide 中的任意功能了。

## 3. 加载图片

　　比如这是一张图片的地址：

```html
http://image.test/test.png
```

　　然后想要在程序当中去加载这张图片：

```java
String url = "http://guolin.tech/book.png";
        Glide.with(this).load(url).into(imageView);
```

　　这样就可以将一张网络上的图片成功下载，并且显示到 ImageView 上了。

　　会发现，到目前为止，Glide 4 的用法和 Glide 3 是完全一样的。仍然还是传统的三步走：先 with()，再 load()，最后 into()。

## 4. 占位图

　　Glide 提供了各种各样非常丰富的 API 支持，其中就包括了占位图功能。

### 4.1. 加载占位图

　　占位图就是指在图片的加载过程中，先显示一张临时的图片，等图片加载出来了再替换成要加载的图片。

　　首先事先准备好一张 loading.jpg 图片，用来作为占位图显示，然后修改 Glide 加载部分的代码，如下所示：

```java
RequestOptions options = new RequestOptions()
        .placeholder(R.drawable.loading);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　就是这么简单，这里先创建了一个 RequestOptions 对象，然后调用它的 placeholder() 方法来指定占位图，再将占位图片的资源 id 传入到这个方法中。最后，在 Glide 的三步走之间加入一个 apply() 方法，来应用刚才创建的 RequestOptions 对象。

　　当使用 Glide 来显示图片时，会立即显示一张占位图，然后等真正的图片加载完成之后会将占位图替换掉。

### 4.2. 异常占位图

　　除了这种加载占位图之外，还有一种异常占位图。异常占位图就是指，如果因为某些异常情况导致图片加载失败，比如说手机网络信号不好，这个时候就显示这张异常占位图。

　　首先准备一张 error.jpg 图片，然后修改 Glide 加载部分的代码，如下所示：

```java
RequestOptions options = new RequestOptions()
        .placeholder(R.drawable.ic_launcher_background)
        .error(R.drawable.error)
        .diskCacheStrategy(DiskCacheStrategy.NONE);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　很简单，这里又串接了一个 error() 方法就可以指定异常占位图了。

　　在 Glide 3 当中，像 placeholder()、error()、diskCacheStrategy() 等等一系列的 API，都是直接串联在 Glide 三步走方法中使用的。

　　而 Glide 4 中引入了一个 RequestOptions 对象，将这一系列的 API 都移动到了 RequestOptions 当中。这样做的好处是可以拜托 冗长的 Glide 加载语句，而且还能进行自己的 API 封装，因为 ReqeustOptions 是可以作为参数传入到方法中的。

　　比如可以写出这样的 Glide 加载工具类：

```java
public class GlideUtil {

    public static void load(Context context,
                            String url,
                            ImageView imageView,
                            RequestOptions options) {
        Glide.with(context)
             .load(url)
             .apply(options)
             .into(imageView);
    }

}
```

## 5. 指定图片大小

　　实际上，使用 Glide 在大多数情况下都是不需要指定图片大小的，因为 Glide 会自动根据 ImageView 的大小来决定图片的大小，以此保证图片不会占用过多的内存从而引发 OOM。

　　不过，如果真的有这样的需求，必须给图片指定一个固定的大小， Glide 仍然是支持这个功能的。修改 Glide 加载部分的代码，如下所示：

```java
RequestOptions options = new RequestOptions()
        .override(200, 100);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　仍然非常简单，这里使用 override() 方法指定了一个图片的尺寸。也就是说，Glide 现在只会将图片加载成 200*100 像素的尺寸，而不会管 ImageView 的大小是多少了。

　　如果想加载一张图片的原始尺寸的话，可以使用 Target.SIZE_ORIGINAL 关键字，如下所示：

```java
RequestOptions options = new RequestOptions()
        .override(Target.SIZE_ORIGINAL);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　这样的话，Glide 就不会再去自动压缩图片，而是会去加载图片的原始尺寸。当然，这种写法也会面临着更高的 OOM 风险。

## 6. 缓存机制

　　Glide 的缓存设计可以说是非常先进的，考虑的场景也是很周全。再缓存这一功能上，Glide 又将它分成了两个模块，一个是内存模块，一个是硬盘缓存。

　　这两个缓存模块的作用各不相同，内存缓存的主要作用是防止应用重复将图片数据读取到内存当中，而硬盘缓存的主要作用是防止应用重复从网络或其他地方重复下载和读取数据。

　　内存缓存和硬盘缓存的相互结合菜构成了 Glide 极佳的图片缓存效果。

### 6.1. 内存缓存

　　在默认情况下，Glide 自动就是开启内存缓存的。也就是说，当使用 Glide 加载一张图片之后，这张图片就会被缓存到内存当中，只要在它还没从内存中被清除之前，下次使用 Glide 再加载这张图片都会直接从内存当中读取，而不用重新从网络或硬盘中读取了。这样无疑就可以大幅度提升图片的加载效率。比如说在一个 RecyclerView 当中反复上下滑动，RecyclerView 中只要是 Glide 加载过的图片都可以直接从内存当中迅速读取并展示出来，从而大大提升了用户体验。

　　而 Glide 最为人性化的是，甚至不需要编写任何额外的代码就能自动享受到这个极为便利的内存缓存功能，因为 Glide 默认就已经将它开启了。

　　如果有什么特殊的原因需要禁用内存缓存功能，Glide 对此提供了接口：

```java
RequestOptions options = new RequestOptions()
        .skipMemoryCache(true);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　可以看到，只需要调用 skipMemoryCache() 方法并传入 true，就表示禁用掉 Glide 的内存缓存功能。

### 6.2. 硬盘缓存

　　禁止 Glide 对图片进行硬盘缓存而使用如下代码：

```java
RequestOptions options = new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.NONE);
Glide.with(this)
     .load(url)
     .apply(options)
     .into(imageView);
```

　　调用 diskCacheStrategy() 方法并传入 DiskCacheStrategy.NONE，就可以禁用掉 Glide 的硬盘缓存功能了。

　　这个 diskCacheStrategy() 方法基本上就是 Glide 硬盘缓存功能的一切，它可以接收五种参数：

```java
DiskCacheStrategy.NONE：表示不缓存任何内容。
DiskCacheStrategy.DATA：表示只缓存原始图片。
DiskCacheStrategy.RESOURCE：表示只缓存转换过后的图片。
DiskCacheStrategy.ALL：表示既缓存原始图片，也缓存转换过后的图片。
DiskCacheStrategy.AUTOMATIC：表示让 Glide 根据图片资源智能地选择使用哪一种缓存策略（默认选项）。
```

　　其中，DiskCacheStrategy.DATA 对应 Glide 3 中的 DiskCacheStrategy.SOURCE，DiskCacheStrategy.RESOURCE 对应 Glide 3 中的 DiskCacheStrategy.RESULT。而 DiskCacheStrategy.AUTOMATIC 是 Glide 4 中新增的一种缓存策略，并且在不指定 diskCacheStrategy 的情况下默认使用的就是这种缓存策略。

　　当使用 Glide 去加载一张图片的时候，Glide 默认并不会将原始图片展示出来，而是会对图片进行压缩和转换。总之就是经过一系列操作之后得到的图片，就叫转换过后的图片。

## 7. 指定加载格式



## 8. 回调与监听



### 8.1. into() 方法



### 8.2. preload() 方法



### 8.3. submit() 方法



### 8.4. listener() 方法



## 9. 图片变换



## 10. 自定义模块



## 11. 使用 Generated API










## 参考文章
[Android图片加载框架最全解析（八），带你全面了解Glide 4的用法](https://blog.csdn.net/guolin_blog/article/details/78582548)




































