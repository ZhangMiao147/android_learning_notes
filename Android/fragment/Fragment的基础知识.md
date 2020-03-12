# Fragment 的基础知识

## 概述

　　为了让界面可以在平板上更好的展示，Android 3.0 版本引入了 Fragment（碎片）功能，它非常类似于 Activity，可以像 Activity 一样包含布局。

　　Fragment 通常是嵌套在 Activity 中使用的。

　　使用 Fragment 可以更加充分地利用平板的屏幕空间。

　　Fragment 是在 3.0 版本引入的，如果使用的是 3.0 之前的系统，需要先导入 android-support-v4 的 jar 包才能使用 Fragment 功能。

　　Fragment 真正的强大之处在于可以动态地添加到 Activity 当中，程序的界面可以定制的更加多样化。

## Fragment 的生命周期

![](image/生命周期图.png)

* onAttach 方法：Fragment 和 Activity 建立关联的时候调用。
* onCreateView 方法：为 Fragment 加载布局时调用。
* onActivityCreated 方法：当 Activty 中的 onCreate 放啊执行完后调用。
* onDestoryView 方法：Fragment 中的布局被移除时调用。
* onDetach 方法：Fragment 和 Activity 解除关联的时候调用。


## 参考文章
[Android Fragment完全解析，关于碎片你所需知道的一切](https://blog.csdn.net/guolin_blog/article/details/8881711)

[Activity与Fragment生命周期探讨](https://www.jianshu.com/p/1b3f829810a1)

