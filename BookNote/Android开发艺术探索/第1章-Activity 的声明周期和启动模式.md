# 第 1 章 Activity 的声明周期和启动模式

## 1.1 Activity 的生命周期全面分析
　　典型情况下的生命周期，是指在有用户参与的情况下，Activity 锁经过的生命周期的改变；而异常情况下的生命周期是指 Activity 被系统回收或者由于当前设备的 Configuration 发生改变从而导致 Activity 被销毁重建，异常情况下的生命周期的关注点和典型情况下略有不同。

#### 1.1.1 典型情况下的生命周期分析
　　在正常情况下，Activity 会经历如下生命周期。

　　（1）onCreate：表示 Activity 正在被创建，这是生命周期的第一个方法。在这个方法中，我们可以做一些初始化工作，比如调用 setContentView 去加载界面布局资源、初始化 Activity 所需数据等。

　　（2）onRestart：表示 Activity 正在重新启动。一般情况下，当当前 Activity 从不可见重新变为可见状态时，onRestart 就会被调用。这种情形一般是用户行为所导致的，比如用户按 Home 键切换到桌面或者用户打开了一个新的 Activity，这时当前的 Activity 就会暂停，也就是 onPause 和 onStop 被执行了，











