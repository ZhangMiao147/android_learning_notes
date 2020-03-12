# Fragment 的基础知识

## Fragment 之间通信

　　主要是通过 getAcvitity 方法，getActivity 方法可以让 Fragment 获取到关联的 Activity，然后再调用 Activity 的 findViewById 方法，就可以获取到和这个 Activity 关联的其他 Fragment 的视图了。



## Fragment 的使用

　　动态添加 Fragment 主要分为 4 步：

1. 获取到 FragmentManager，在 Activity 中可以直接通过 getFragmentManager 得到。
2. 开启一个事务，通过调用 beginTransaction 方法开启。
3. 向容器内加入 Fragment，一般使用 replace 方法实现，需要传入容器的 id 和 Fragment 的实例。
4. 提交事务，调用 commit 方法提交。


## 参考文章
[Android Fragment完全解析，关于碎片你所需知道的一切](https://blog.csdn.net/guolin_blog/article/details/8881711)

[Activity与Fragment生命周期探讨](https://www.jianshu.com/p/1b3f829810a1)

