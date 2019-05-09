# BroadcastReceiver 知识

## 介绍
　　BroadcastReceiver ，广播接受者，用来接收来自系统和应用中的广播。
　　在 Android 系统中，广播体现在方方面面，例如当开机完成后系统会产生一条广播，接收到这条广播就能实现开机启动服务的功能；当网络状态改变时系统会产生一条广播，接收到这条广播就能及时地做出提示和保护数据等操作；当电池电量改变时，系统会产生一条广播，接收到这条广播就能在电量低时告知用户及时保存进程等等。Android 中的广播机制设计的非常出色，很多事情原本需要开发者亲自操作的，现在只需等待广播告知自己就可以了，大大减少了开发的工作量和开发周期。

## 静态和动态注册方式
　　构建 Intent ，使用 sendBroadcast 方法发出广播定义一个广播接收器，该广播接收器继承 BroadcastReceiver，并且覆盖 onReceive() 方法来响应事件。注册该广播接收器，可以在代码中注册（动态注册），也可以在 AndroidManifest.xml 配置文件中注册（静态注册）。

## BroadcastReceiver 的两种常用类型

#### Normalbroadcasts：默认广播
　　发送一个默认广播使用 Context.sendBroadcast() 方法，普通广播对于多个接受者来说是完全异步的，通常每个接受者都无需等待即可接收到广播，接受者相互之间不会有影响。对于这种广播，接收者无法终止广播，即无法阻止其他接收者的接收动作。

#### orderedbroadcasts：有序广播

## 参考文章
[Android 广播Broadcast的两种注册方式静态和动态](https://blog.csdn.net/csdn_aiyang/article/details/68947014)
[Android四大组件：BroadcastReceiver史上最全面解析](https://blog.csdn.net/carson_ho/article/details/52973504)
[安卓广播的底层实现原理](https://www.jianshu.com/p/02085150339c)