# Activity 的常见面试题，大家一起来看呀

> 前言：本文中提到的面试题都是从网络上查询出来的，至于面试题的解答只是本人的一些看法，如果有解答不对的地方希望大家提出会留言，也欢迎留言没有提到的面试问题。

## 1. 关于启动模式的问题

**问题 1 ：启动模式**


**问题 2 ：onNewIntent() 的调用时机**
　　启动一个 Activity 时，没有新建实例，而是复用任务中的 activity，会将新的 Intent 传递给复用的 activity 的 onNewIntent() 方法。

**问题 3 ：a-b-c界面，其中b是singleinstance的，那么c界面点back返回a界面，为什么？怎么管理栈的？**
　　singleinstance 启动模式会新建任务栈，b 就会在一个单独的栈中，而 a-c 是在一个栈中，在 b 跳转 c 的时候，b 就会销毁，b 所在的栈也销毁，所以 c 点击 back 返回后，栈顶是 a。

**问题 4 ：a启动b，b启动c,怎么样可以在c界面点back返回到a？**
　　一种方法就是上面的问题，将 b 的启动模式 launchMode 设置为 singleInstance。
　　另外一种方式就是通过 Intent 的 flag 来解决，使用 Intent 的 flag 有几种都可以解决这个问题。FLAG_ACTIVITY_NO_HISTORY、

**问题 5 ：在 SingleTop 模式中，如果打开一个已经存在栈顶的 Activity，他的生命流程是怎样的？**
　　onPause() -> onNewIntent() -> onResume()

## 2. 关于启动流程的问题

**问题 1 ： Activity 的冷启动流程**

**问题 2 ：从 framework 的角度将 activity 的启动流程（冷启动）**

## 3. 其他问题

**问题 1 ： 在oncreate里面可以得到view的宽高吗？**
　　不能，因为在 onCreate() 方法的时候，界面还没有显示出来，view 的宽高是不确定的。
**问题 2 ： Activity 的内部机制**

**问题 3 ： AMS 的作用**

