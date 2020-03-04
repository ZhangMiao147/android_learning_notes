# jar 包与 aar 包的区别

## 1. jar
　　jar 只包含了 class 文件和清单文件，不包含资源文件，如图片等所有 res 中的文件。

　　JAR（Java Archive，Java 归档文件）是与平台无关的文件格式，它允许将许多文件组合成一个压缩文件。

　　jar 的优点：安全性、减少下载实践、传输平台扩展、包密封、包版本控制、可移植性。

　　打 jar 包时，项目里的 res 文件是用不了的，若想用图片文件，可以将图片文件放进 assets 文件里面打进 jar 包在进行调用，但必须注意 jar 里面的 assets 文件夹里面的文件不能和调用项目里面 assets 文件夹里面的文件重名。

## 2. aar

　　aar 是 Android 库项目的二进制归档文件，包含所有资源，class 以及 res 资源文件全部包含。

　　将 aar 解压（后缀改为 .zip，再解压文件）打开后，可以看到每个 aar 解压后的内容可能不完全一样，但是都会包含 AndroidManifest.xml、classes.jar、res、R.txt。

## 3. 如何选择

　　如果只是一个简单的类库那么使用生成的 *.jar 文件即可。如果是一个 UI 库，包含一些控件布局文件以及字体等资源文件那么就使用 *.aar 文件。

## 4. 参考文章

[Android 中 aar 与 jar 的区别](https://www.jianshu.com/p/0a2572a63ed5)