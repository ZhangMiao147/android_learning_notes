# 资源 ID

# 资源 ID 的定义

　　在 Android 中资源的使用几乎无处不在，layout、string、drawable、raw、style、theme  等等都是。

　　这些资源会在编译过程中被打包进 APK 中（ res 文件夹 ）或者被打包成独立的资源 APK 包（比如 framework-res.apk ）。

　　但是这些资源都会被赋予独一无二的 ID 即资源索引来方便系统访问。

　　这些资源索引由 Android 的工具 AAPT（Android Asset Packing Tool）生成的八位十六进制整数型。

![img](https:////upload-images.jianshu.io/upload_images/4302219-791ec956783d296e.png?imageMogr2/auto-orient/strip|imageView2/2/w/552/format/webp)

　　中间 02 所在位置值代表资源ID对应的资源的类型，分别是：

* 02：drawable
* 03：layout
* 04：values
* 05：xml
* 06：raw
* 07：color
* 08：menu

　　分配 resource id 的主要逻辑实现是在 framework/base/tools/aapt/Resource.cpp 和 ResourceTable.cpp。

# 资源 ID 生成规则

　　android 里面，资源文件和资源 ID 之间的映射是如何工作的？

　　问题描述：
　　做 Android 应用开发的时候，我们知道可以通过 R.id.xxx 来非常方便的访问应用程序的资源。
但是任何资源最终要编译成二进制格式的，那么在这种机制下，系统是如何工作的？

　　例如，在 layout1.xml 里面，我们这样写：
 < Button android:id="@+id/button1" >

　　然后 AAPT 会生成 R.java 文件：
 public static final int button1=0x7f05000b;
接下来在生成 *.apk 的时候，像 @+id/button1  这样的 ID 会用 ”0x7f05000b” 这样的数字代替。
所以，我们在调用
 findViewById(R.id.button1);
的时候，实际上调用的是像 ”0x7f05000b” 这样的资源 ID。

　　那么这中间到底发生了什么呢？例如系统是如何把一个图片和数字 ID 对应起来的呢？
　　回答：在编译的时候，AAPT 会扫描你所定义的所有资源（在不同文件中定义的以及单独的资源文件），然后给它们指定不同的资源 ID。

　　资源 ID 是一个 32bit 的数字，格式是 PPTTNNNN ， PP 代表资源所属的包(package) ,TT 代表资源的类型(type)，NNNN 代表这个类型下面的资源的名称。 对于应用程序的资源来说， PP 的取值是 0×77。

　　TT 和 NNNN 的取值是由 AAPT 工具随意指定的–基本上每一种新的资源类型的数字都是从上一个数字累加的（从 1 开始）；而每一个新的资源条目也是从数字 1 开始向上累加的。

　　所以如果我们的这几个资源文件按照下面的顺序排列，AAPT 会依次处理：
 <code>layout/main.xml </code>

<code>drawable/icon.xml </code>

<code>layout/listitem.xml</code>
　　按照顺序，第一个资源的类型是 ”layout” 所以指定 TT == 1， 这个类型下面的第一个资源是 ”main” ，所以指定 NNNN == 1 ，最后这个资源就是 0x7f010001。

　　第二个资源类型是 ”drawable”，所以指定TT == 2，这个类型下的 ”icon” 指定 NNNN  == 1，所以最终的资源 ID 是 0x7f020001。

　　第三个资源类型是 ”layout”，而这个资源类型在前面已经有定义了，所以 TT 仍然是1，但是 ”listitem” 这个名字是新出现的，所以指定 NNNN==2，因此最终的资源 ID 就是 0x7f010002。

　　注意的是，AAPT 在每一次编译的时候不会去保存上一次生成的资源 ID 标示，每当 /res 目录发生变化的时候，AAPT 可能会去重新给资源指定 ID 号，然后重新生成一个 R.java 文件。因此，在做开发的时候，你不应该在程序中将资源 ID 持久化保存到文件或者数据库。而资源 ID 在每一次编译后都有可能变化。

　　一旦资源被编译成二进制文件的时候，AAPT 会生成 R.java 文件和 “resources.arsc” 文件，“R.java” 用于代码的编译，而 ”resources.arsc” 则包含了全部的资源名称、资源 ID 和资源的内容（对于单独文件类型的资源，这个内容代表的是这个文件在其 .apk 文件中的路径信息）。这样就把运行环境中的资源 ID 和具体的资源对应起来了。

　　在调试的时候，你可以使用 “ aapt dump resources <apk的路径>” 来看到对 resources.arsc 文件的详细描述信息。

# 参考文章

1. [Android_R.java文件中资源ID的含义](https://www.jianshu.com/p/5cb55ef61048)
2. [android 资源ID生成规则](https://blog.csdn.net/nio96/article/details/23737075)

