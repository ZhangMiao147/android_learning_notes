# WindowManangerService

## 1. WMS 概述

　　WMS 是系统的其他服务，无论对于应用开发还是 Framework 开发都是重点的知识，它的职责有很多，主要有以下几点：

### 1. 窗口管理

　　WMS 是窗口的管理者，它负责窗口的启动、添加和删除，另外窗口的大小和层级也是由 WMS 进行管理的。窗口管理的核心成员有 DisplayContent、WindowToken 和 WindowState。

### 2. 窗口动画

　　窗口间进行切换时，使用窗口动画可以显得更炫一些，窗口动画由 WMS 的动画子系统来负责，动画自系统的管理者为 WindowAnimator。

### 3. 输入系统的中转站

　　通过对窗口的触摸从而产生触摸事件，InputManangerService（IMS）会对触摸事件进行处理，它会寻找一个最合适的窗口来处理触摸反馈信息，WMS 是窗口的管理者，因此，WMS "理所应当" 的成为了输入系统的中转站。

### 4. Surface 管理

　　窗口并不具备有绘制的功能，因此每个窗口都需要有一块 Surface 来供自己绘制。为每个窗口分配 Surface 是由 WMS 来完成的。

　　WMS 的职责可以简单总结为下图：

![](image/WMS职责.png)

## 2. WMS 的产生



## 参考文章

4. [Android 解析 WindowManagerService（一）WMS 的诞生](https://blog.csdn.net/itachi85/article/details/78186741)
5. [Android解析WindowManagerService（二）WMS的重要成员和Window的添加过程](https://blog.csdn.net/itachi85/article/details/78357437)
6. [Android解析WindowManagerService（三）Window的删除过程](https://blog.csdn.net/itachi85/article/details/79134490)