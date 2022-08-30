# Activity 的四种状态

[TOC]

# 1. 基本状态

## 1.1.Active/Running

一个新的 Activity 启动入栈后，它显示在屏幕最前端，Activity 处于活动状态，此时 Activity 处于栈顶，此时它处于可见并可和用户交互的激活状态，叫做活动状态或者运行状态（active or running）

## 1.2. Paused

当 Activity 失去焦点时，或被一个新的非全屏的 Activity，或被一个透明的 Activity 放置在栈顶时，Activity 就转换为暂停状态。但我们需要明白，此时 Activity 只是失去了焦点故不可与用户进行交互，它依然与窗口管理器保持连接，其所有的状态信息及其成员变量都还存在，只有在系统内存紧张的情况下，才有可能被系统回收掉。

## 1.3. Stopped

当一个 Activity 被另一个 Activity 完全覆盖时，被覆盖的 Activity 就会进入 Stopped 状态。它依然保持所有状态和成员信息，此时它不再可见，当系统内存需要被用在其他地方的时候，Stopped 的 Activity 将被强行终止掉。

## 1.4. Killed

当 Activity 被系统回收掉时，Activity 就处于 killed 状态。如果一个 Activity 是 Paused 或者 Stopped 状态，系统可以将该 Activity 从内存中删除，Android 系统采用两种方式进行删除，要么要求该 Activity 结束，要么直接终止它的进程。当该 Activity 再次显示给用户时，它必须重新开始和重置之前的状态。

**优先级顺序：运行状态>暂停状态>停止状态>销毁状态**

# 2. 状态转换

当一个 Activity 实例被创建、销毁或者启动另外一个 Activity 时，它在这四种状态之间进行转换，这种转换的发生依赖于用户程序的动作。

下图说明了 Activity 在不同状态间转换的时机和条件：

![](https://pics3.baidu.com/feed/4b90f603738da977b798007c652a091c8718e3b0.jpeg?token=2e882d9f74d99dce2df2a2019fab1d38&s=A190E432214254EE10EDC8CA0200C0B2)

程序员可以启动一个 activity，但是却不能手动的”结束“一个 activity。当你调用 Activity.finish() 方法时，这是一个结束 activity 的动作，当此方法调用的时候，系统会将其栈顶的 activity 移出，并没有及时的释放资源，Android 系统当没有可用的资源时候会按照优先级，释放掉一部分。

# 3. 参考文章

1. [Activity的四种状态](https://baijiahao.baidu.com/s?id=1653706103430980843&wfr=spider&for=pc)

