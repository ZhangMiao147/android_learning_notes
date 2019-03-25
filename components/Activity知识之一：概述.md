# Activity 知识之一：概述

## Activity 介绍
　　Activity 是 Android 的四大组件之一，主要用于提供窗口与用户进行交互。

## Activity 的生命周期
### 生命周期图
　　拿到官网的 Activity 的生命周期图：
![](./Activity生命周期图.png)

　　解释图中个方法的作用：

| 生命周期方法 | 作用 | 说明 |
| -------- | -------- | -------- |
| onCreate | 表示 Activity 正在被创建 | activity 被创建时调用，一般在这个方法中进行活动的初始化工作，如设置布局工作、加载数据、绑定控件等。 |
| onRestart | 表示 Activity 正在重新启动 | 这个回调代表了 Activity 由完全不可见重新变为可见的过程，当 Activity 经历了 onStop() 回调变为完全不可见后，如果用户返回原 Activity，便会触发该回调，并且紧接着会触发 onStart() 来使活动重新可见。 |
| onStart | 表示 Activity 正在被启动 | 经历该回调后，Activity 由不可见变为可见，但此时处于后台可见，还不能和用户进行交互。 |
| onResume | 表示 Activity 已经可见 | 已经可见的 Activity 从后台来到前台，可以和用户进行交互。 |
| onPause | 表示 Activity 正在停止 | 当用户启动了新的 Activity ，原来的 Activity 不再处于前台，也无法与用户进行交互，并且紧接着就会调用 onStop() 方法，但如果用户这时立刻按返回键回到原 Activity ，就会调用 onResume() 方法让活动重新回到前台。而且在官方文档中给出了说明，不允许在 onPause() 方法中执行耗时操作，因为这会影响到新 Activity 的启动。 |
| onStop | 表示 Activity 即将停止 | 这个回调代表了 Activity 由可见变为完全不可见，在这里可以进行一些稍微重量级的操作。需要注意的是，处于 onPause() 和 onStop() 回调后的 Activity 优先级很低，当有优先级更高的应用需要内存时，该应用就会被杀死，那么当再次返回原 Activity 的时候，会重新调用 Activity 的onCreate()方法。 |
| onDestroy | 表示 Activity 即将被销毁 | 来到了这个回调，说明 Activity 即将被销毁，应该将资源的回收和释放工作在该方法中执行。 |

### 正常情况下的生命周期分析





##### 关于生命周期的一些常见问题

###### 1.由活动A启动活动B时。活动A的 onPause() 与活动B的 onResume() 哪一个先执行？
　　创建两个 Activity ，由 MainActivity 跳转到 FirstAcivity ，运行结果如下：


　　可以看到，是 MainActivity 先执行了 onPause ， FirstActivity 的 onResume() 后执行的。
点击返回看一下执行的顺序：

　　点击返回后，可以看到是 FirstActivity 的 onPause() 先执行，MainActivity 的 onResume() 后执行。

　　所以，当活动 A 启动活动B时，是活动 A 的 onPause() 方法先执行，活动 B 的 onResume() 方法后执行。

###### 2.dialog 是否会对生命周期产生影响？

　　查看 Activity 声明周期的描述，如果 Activity 不在前台，且并非完全不可见时， Activity 就会处在 onPause() 的暂停状态。但是事实如何，用代码说话，测试三种情况：一，弹出标准的 AlertDialog ；二，弹出全屏的 AlertDialog ；三，弹出主题为 Theme.AppCompat.Dialog 的 Activity ，查看这三种情况下的生命周期的变化：



### 异常状态下活动的生命周期
　　当 Activity 在运行过程中发生一些情况时，生命周期流程也会发生变化。常见的异常情况有两种，一种是资源配置改变；另一是内存不足导致生命周期流程发生变化。

#### 资源配置改变导致 Activity 重建
　　资源配置最常见的情况就是横竖屏切换导致资源的变化，当程序启动时，会根据不同的配置加载不同的资源，例如横竖屏两个状态对应着两张不同的资源图片。如果在使用过程中屏幕突然旋转，那么 Activity 就会因为系统配置发生改变而销毁重建，加载合适的资源。
下面是 MainActivity 在横竖屏切换时生命周期变化的过程：

#### 低优先级 Activity 由于内存不足被杀死
　　当设备的内存空间不足时，系统为了保证用户的体验，会按照进程优先级将一些低优先级的进程杀死，以来保证用户的体验。
　　系统回收进程的优先级：
（1） 前台进程
　　持有用户正在交互的 Activty，即生命周期处于 onResume 状态的活动。
　　该进程有绑定到正在交互的 Activity 的 service  或前台 service。
（2） 可见进程
　　这种进程虽然不在前台，但是仍然可见。
　　该进程持有的 Activity 执行了 onPause 但未执行 onStop 。例如原活动启动了一个 dialog 主题的 Activity，但此时原活动并非完全不可见。
　　该进程有 service 绑定到可见的或前台 Activity。
（3）服务进程
　　进程中持有一个 service，同时不属于上面两种情况。
（4）后台进程
　　不属于上面三种情况，但进程持有一个不可见的 Activity，即执行了 onStop 但未执行 onDestory 的状态。
（5）空进程
　　不包含任何活跃的应用组件，作用是加快下次启动这个进程中组件所需要的时间，优先级低。


#### 异常情况下的处理
　　在发生异常情况后，用户再次回到 Activity，原 Activity 会重新建立，原已有的数据就会丢失，比如用户选择了对，重建之后用户就看不到之前的选择，在异常的情况下如何给用户带来好的体验，有两种办法。

##### 数据保存
　　第一种就是系统提供的 onSaveInstanceState 和 onRestoreInstanceState 方法，onSaveInstanceState 方法会在 Activity 异常销毁之前调用，用来保存需要保存的数据，onRestoreInstanceState 方法在 Activity 重建之后获取保存的数据。



##### 防止重建
　　在默认情况下，资源配置改变会导致活动的重新创建，但是可以通过对活动的 android:configChanges 属性的设置使活动防止重新被创建。
Android configChanges的属性值和含义(详细) https://blog.csdn.net/qq_33544860/article/details/54863895

#### 低优先级的 Activity 由于内存不足被杀死

##### 进程的优先级



## 参考文章：
1. [老生常谈-Activity](https://juejin.im/post/5adab7b6518825670c457de3)
2. [全面了解 Activity](https://juejin.im/entry/589847f7128fe10058ebd803)
3. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)
4. [Android:“万能”Activity 重构篇](https://juejin.im/entry/574b7ec52e958a005eed0788)
5. [Android Activity 全面解析](https://juejin.im/entry/57db6332d203090069d3466d)
6. [Android之Activity系列总结（一）--Activity概览](https://www.cnblogs.com/jycboy/p/6367282.html)
7. [Android之Activity系列总结(二)--任务和返回栈](https://www.cnblogs.com/jycboy/p/6367330.html)
8. [Android之Activity系列总结(三)--Activity的四种启动模式](https://www.cnblogs.com/jycboy/p/6367829.html)
9. [Android 旋转屏幕--处理Activity与AsyncTask的最佳解决方案](http://www.cnblogs.com/jycboy/p/save_state_data.html)
10. [Activity之概览屏幕(Overview Screen)](https://www.cnblogs.com/jycboy/p/overview_screen.html)
11. [Android四大组件之Activity详解](https://www.cnblogs.com/caobotao/p/4987015.html)
12. [Android之Activity系列总结（一）--Activity概览](https://www.cnblogs.com/jycboy/p/6367282.html)
13. [3分钟看懂Activity启动流程](https://www.jianshu.com/p/9ecea420eb52)