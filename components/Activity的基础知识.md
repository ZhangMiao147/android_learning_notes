# Activity的基础知识

## Activity介绍
https://juejin.im/entry/589847f7128fe10058ebd803 全面了解 Activity

## 生命周期
https://juejin.im/post/5aef0d215188253dc612991b
https://juejin.im/post/5adab7b6518825670c457de3
### 生命周期图


### 正常情况下的生命周期分析

#### 由活动A启动活动B时。活动A的onPause()与活动B的onResume()哪一个先执行？

#### dialog是否会对生命周期产生影响？

### 异常状态下活动的生命周期

#### 资源配置改变导致activity重建

##### onSaveInstanceState和onRestoreInstanceState

##### android:configChanges

Android configChanges的属性值和含义(详细) https://blog.csdn.net/qq_33544860/article/details/54863895

#### 低优先级的activity由于内存不足被杀死

##### 进程的优先级

## 启动模式
https://juejin.im/post/5adab7b6518825670c457de3 老生常谈-Activity
https://juejin.im/post/5aef0d215188253dc612991b

### 四种启动模式

#### standard(标准模式)

#### singleTop(栈顶复用)

#### singleTask(栈内复用)

#### singleInstance(单例)

### onNewIntent()方法与回调时机

### TaskAffinity属性与allowTaskReparenting




## Activity中的Flags
https://juejin.im/post/5aef0d215188253dc612991b

## Activity的使用

### Activity之间的跳转
https://juejin.im/post/5aef0d215188253dc612991b
#### 相关API

##### startActivity

##### startActivityForResult()

#### 显示启动

#### 隐式启动

##### IntentFilter的使用

##### action的匹配规则

##### category匹配规则

##### data匹配规则


### 指定启动模式的方式


## Android启动流程


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