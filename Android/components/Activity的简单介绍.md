# Activity 之简单介绍（一）

## Activity 介绍
	Activity 是 Android 的四大组件之一，主要用于提供窗口与用户进行交互。

https://juejin.im/entry/589847f7128fe10058ebd803 全面了解 Activity

## Activity 的生命周期
https://juejin.im/post/5aef0d215188253dc612991b
https://juejin.im/post/5adab7b6518825670c457de3
### 生命周期图
拿到官网的Activity的生命周期图：
![](./Activity生命周期图.png)
图中个方法的作用：

| 生命周期方法 | 作用 | 说明 |
| -------- | -------- | -------- |
| onCreate | 表示 Activity 正在被创建 | activity 被创建时调用，一般在这个方法中进行活动的初始化工作，如设置布局工作、加载数据、绑定控件等。 |
| onRestart | 表示 Activity 正在重新启动 | 这个回调代表了 Activity 由完全不可见重新变为可见的过程，当 Activity 经历了 onStop() 回调变为完全不可见后，如果用户返回原 Activity，便会触发该回调，并且紧接着会触发onStart()来使活动重新可见。 |
| onStart | 表示Activity正在被启动 | 经历该回调后，activity由不可见变为可见，但此时处于后台可见，还不能和用户进行交互。 |
| onResume | 表示Activity已经可见 | 已经可见的activity从后台来到前台，可以和用户进行交互。 |
| onPause | 表示Activity正在停止 | 当用户启动了新的activity，原来的activity不再处于前台，也无法与用户进行交互，并且紧接着就会调用onStop()方法，但如果用户这时立刻按返回键回到原activity，就会调用onResume()方法让活动重新回到前台。而且在官方文档中给出了说明，不允许在onPause()方法中执行耗时操作，因为这会影响到新Activity的启动。 |
| onStop | 表示Activity即将停止 | 这个回调代表了activity由可见变为完全不可见，在这里可以进行一些稍微重量级的操作。需要注意的是，处于onPause()和onStop()回调后的activity优先级很低，当有优先级更高的应用需要内存时，该应用就会被杀死，那么当再次返回原Activity的时候，会重新调用activity的onCreate()方法。 |
| onDestroy | 表示Activity即将被销毁 | 来到了这个回调，说明activity即将被销毁，应该将资源的回收和释放工作在该方法中执行。 |

### 正常情况下的生命周期分析

#### 由活动A启动活动B时。活动A的onPause()与活动B的onResume()哪一个先执行？
创建两个activity，由MainActivity跳转到FirstAcivity，运行结果如下：


可以看到，是MainActivity先执行了onPause，FirstActivity的onResume()后执行的。
点击返回看一下执行的顺序：

点击返回后，可以看到是FirstActivity的onPause()先执行，MainActivity的onResume()后执行。

所以，当活动A启动活动B时，是活动A的onPause()方法先执行，活动B的onResume()方法后执行。

#### dialog是否会对生命周期产生影响？

查看Activity声明周期的描述，如果activity不在前台，且并非完全不可见时，activity就会处在onPause()的暂停状态。但是事实如何，用代码说话，测试三种情况：一，弹出标准的AlertDialog；二，弹出全屏的AlertDialog；三，弹出主题为Theme.AppCompat.Dialog的activity，查看这三种情况下的生命周期的变化：



### 异常状态下活动的生命周期
当activity在运行过程中发生一些情况时，生命周期流程也会发生变化。
#### 资源配置改变导致activity重建
资源配置常见的情况就是横竖屏切换导致资源的变化，当程序启动时，会根据不同的配置加载不同的资源，例如横竖屏两个状态对应着两张不同的资源图片。如果在用用使用过程中屏幕突然旋转，那么activity就会因为系统配置发生改变而销毁重建，加载合适的资源。
下面是MainActivity在横竖屏切换时生命周期变化的过程：


##### onSaveInstanceState和onRestoreInstanceState
对于activity重新创建，应该如何保证activity中的已有数据不丢失呢？系统提供了onSaveInstanceState和onRestoreInstanceState来保存和获取数据。



##### android:configChanges

Android configChanges的属性值和含义(详细) https://blog.csdn.net/qq_33544860/article/details/54863895

#### 低优先级的activity由于内存不足被杀死

##### 进程的优先级

## 启动模式
https://juejin.im/post/5adab7b6518825670c457de3 老生常谈-Activity
https://juejin.im/post/5aef0d215188253dc612991b

和生命周期一样， activity 的四种 launchMode 也非常重要但又特别容易混淆，首先，activity 是以任务栈的形式创建和销毁的，栈是一种“后进先出”的数据结构，在默认情况下，启动第一个 actiivty 时，系统将会为它创建一个任务栈并将活动置于栈底，而从这个 activity 启动的其他 activity 将会依次入栈，当用户连续按下返回键时，任务栈中的 activity 会从栈顶开始依次销毁。但是这样有一个弊端，就是对于某些 activity 我们不希望它总要重新创建，这时就需要采用不同的启动模式。

### 四种启动模式

#### standard(标准模式)
activity 的默认启动模式，主要启动 activity 就会创建一个新实例。


#### singleTop(栈顶复用)

#### singleTask(栈内复用)

#### singleInstance(单例)

### onNewIntent()方法与回调时机

### TaskAffinity属性与allowTaskReparenting




## Activity中的Flags
https://juejin.im/post/5aef0d215188253dc612991b


参考文章：
https://juejin.im/post/5adab7b6518825670c457de3 老生常谈-Activity （已大概看过）
https://juejin.im/entry/589847f7128fe10058ebd803 全面了解 Activity （已大概看过）
https://juejin.im/post/5aef0d215188253dc612991b Activity 必知必会 （已大概看过）
https://juejin.im/entry/574b7ec52e958a005eed0788 Android:“万能”Activity 重构篇

https://juejin.im/entry/57db6332d203090069d3466d Android Activity 全面解析

https://www.cnblogs.com/jycboy/p/6367829.html Android之Activity系列总结(三)--Activity的四种启动模式
https://www.cnblogs.com/jycboy/p/6367330.html Android之Activity系列总结(二)--任务和返回栈
https://www.cnblogs.com/jycboy/p/6367282.html Android之Activity系列总结（一）--Activity概览
http://www.cnblogs.com/jycboy/p/save_state_data.html Android 旋转屏幕--处理Activity与AsyncTask的最佳解决方案
https://www.cnblogs.com/jycboy/p/overview_screen.html Activity之概览屏幕(Overview Screen)
https://www.cnblogs.com/caobotao/p/4987015.html Android四大组件之Activity详解
https://www.cnblogs.com/jycboy/p/6367282.html Android之Activity系列总结（一）--Activity概览
https://www.jianshu.com/p/9ecea420eb52 3分钟看懂Activity启动流程（已大概看过）