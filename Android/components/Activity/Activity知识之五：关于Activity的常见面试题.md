# Activity 知识之五：关于 Activity 的常见面试题

## 前言
　　本文中提到的面试题都是从网络上查询出来的，至于面试题的解答只是本人的一些看法，如果有解答不对的地方希望大家提出会留言，也欢迎留言没有提到的面试问题。

## 关于启动模式的问题

#### 启动模式

#### onNewIntent() 的调用时机

#### a-b-c界面，其中b是singleinstance的，那么c界面点back返回a界面，为什么？怎么管理栈的？


#### a启动b，b启动c,怎么样可以在c界面点back返回到a？


#### 在 SingleTop 模式中，如果打开一个已经存在栈顶的 Activity，他的生命流程是怎样的？ onPause() -> onNewIntent() -> onResume()

## 关于启动流程的问题

#### Activity 的冷启动流程

#### 从framework 的角度将 activity 的启动流程（冷启动）

## 其他问题

#### 在oncreate里面可以得到view的宽高吗？

#### Activity 的内部机制

#### AMS 的作用

#### 为什么要 引入 activity 这个组件？

