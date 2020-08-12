# Abdroid Studio 中怎么使用 DDMS 工具

随着[android studio](http://www.maiziedu.com/course/android/504-6560/)的广泛使用，开发人员对相关工具的使用需求更加凸显。昨天在一个android studio教程网站上，看到一篇有关DDMS工具使用的相关知识，感觉很不错，分享给大家，一起来看看吧，新技能get走起~~

首先，我们需要了解的是DDMS工具是个什么鬼？

其实，DDMS（Dalvik Debug Monitor Service），是 Android 开发环境中的Dalvik虚拟机调试监控服务。可以进行的操作有：为测试设备截屏，查看特定行程中正在运行的线程以及堆信息、Logcat、广播状态信息、模拟电话呼叫、接收SMS、虚拟地理坐标等，功能非常强大，对于安卓开发者来说是一个非常好的工具，下面来看看其具体用法吧。

 

 Android Studio开发工具中，打开DDMS，具体的方式如图：

![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155046824-1608491873.png)

打开之后的窗口如图：

![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155054543-1611587229.png)

 

除了图上大概标注的功能外，详细的功能有：

1.Devices：查看到所有与DDMS连接的模拟器详细信息，以及每个模拟器正在运行的APP进程，每个进程最右边相对应的是与调试器链接的端口。

 

2.Emulator Control：实现对模拟器的控制，如：接听电话，根据选项模拟各种不同网络情况，模拟短信发送及虚拟地址坐标用于测试GPS功能等。

3.LogCat ：查看日志输入信息，可以对日志输入进行Filter过滤一些调试的信息筛选查看等。

4.File Exporler:File Exporler文件浏览器，查看Android模拟器中的文件，可以很方便的导入/出文件。

5.Heap：查看应用中内存使用情况。

6.Dump HPROF file:点击DDMS工具条上面的Dump HPROF文件按钮，选择文件存储位置，然后在运行hprof-conv。可以用MAT分析heap dumps启 动MAT然后加载刚才我们生成的HPROF文件。MAT是一个强大的工具，讲述它所有的特性超出了本文的范围，所以我只想演示一种你可以用来检测 泄露的方法：直方图（Histogram）视图。它显示了一个可以排序的类实例的列表，内容包括：shallow heap（所有实例的内存使用总和），或者retained heap（所有类实例被分配的内存总和，里面也包括他们所有引用的对象）等。

 

7.Screen captrue:截屏操作

 

8.Thread:查看进程中线程情况。

 

9.其它工具。

 

可能这样说，不太直观，下面我们通过图片，来简单展示几个的使用：

 

查看进程中的线程：

 

 ![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155110277-1489759632.png)

 

查看内存信息：

 

 ![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155148840-1055531119.png)

文件管理，可以对文件进行导入导出，真机很多操作可能需要Root权限才能进行。模拟器的话可以模拟发短信，打电话，定位等：

 

 ![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155507011-1638761428.png)

 

查看特定页面的展示及布局元素构成：

 

![img](https://images2015.cnblogs.com/blog/816222/201512/816222-20151208155520011-1323763090.png)

# 参考文章

1. [Android Studio中怎么使用DDMS工具](https://www.cnblogs.com/zhichao123/p/11794491.html)