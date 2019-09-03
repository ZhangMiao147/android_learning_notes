# XWalkView

## 引用 XWalkView 库

#### 方式一：使用 maven 地址引入


#### 方式二：下载 aar 文件引入


#### 注意
　　注意如果你的项目使用的 sdk 版本低，就不要通过添加 maven 地址和 complie 方法去引入库，不然就会在 build 的时候报如下错误：
```
Execution failed for task ':app:processReadboyDebugResources'.
```
　　这个错误主要是因为 build 的版本不一致，也就是 XWalkView 库的 build 版本与你项目的 build 版本不一致（[错误查询](https://blog.csdn.net/zxccxzzxz/article/details/82986956)），如果你的项目不方便修改 build 版本，那么就不要使用这个方式。

　　通过 maven 地址无法引用库，那么可以直接去下载 aar 文件，然后直接将它放到 libs 文件夹下引入。



## 参考文章
[用 Crosswalk 的 XWalkView 做混合式 App](https://www.jianshu.com/p/93f88e87f34f)
[有关 XWalkView 技术分享](https://blog.csdn.net/tluffy/article/details/93771926)
[腾讯 x5 内核集成，优化 web 加载速度](https://www.jianshu.com/p/4cf6dff6657b)
[Crosswalk 学习笔记](https://www.jianshu.com/p/372731b31f5c)