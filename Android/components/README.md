# Android 四大组件相关知识
> 持续更新

## 1. Activity
　　Activity 通常展示为一个可视化的用户界面。

**相关文章：**
* [关于 Activity 的生命周期，来看一看这篇](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%85%B3%E4%BA%8EActivity%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%EF%BC%8C%E6%9D%A5%E7%9C%8B%E4%B8%80%E7%9C%8B%E8%BF%99%E7%AF%87.md)
* [Activity 生命周期的问题真多，让我们来一起代码验证](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E9%97%AE%E9%A2%98%E9%AA%8C%E8%AF%81/Activity%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E7%9A%84%E9%97%AE%E9%A2%98%E7%9C%9F%E5%A4%9A%EF%BC%8C%E8%AE%A9%E6%88%91%E4%BB%AC%E6%9D%A5%E4%B8%80%E8%B5%B7%E4%BB%A3%E7%A0%81%E9%AA%8C%E8%AF%81.md)
* [将 Intent 的 flags 使用一下呀呀呀](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Intent%E7%9A%84flags%E9%AA%8C%E8%AF%81/%E5%B0%86Intent%E7%9A%84flags%E4%BD%BF%E7%94%A8%E4%B8%80%E4%B8%8B%E5%91%80%E5%91%80%E5%91%80.md)
* [搞懂 Activity 的所有启动模式](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E6%90%9E%E6%87%82Activity%E7%9A%84%E6%89%80%E6%9C%89%E5%90%AF%E5%8A%A8%E6%A8%A1%E5%BC%8F.md)
* [Activity 四种 launchMode 有什么不同，代码走起来](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%9B%9B%E7%A7%8DlaunchMode%E9%AA%8C%E8%AF%81/Activity%E5%9B%9B%E7%A7%8DlaunchMode%E6%9C%89%E4%BB%80%E4%B9%88%E4%B8%8D%E5%90%8C%EF%BC%8C%E4%BB%A3%E7%A0%81%E8%B5%B0%E8%B5%B7%E6%9D%A5.md)
* [关于 Intent 的全部 flags 介绍](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%85%B3%E4%BA%8EIntent%E7%9A%84%E5%85%A8%E9%83%A8flags%E4%BB%8B%E7%BB%8D.md)
* [将 Intent 的 flags 使用一下呀呀呀](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Intent%E7%9A%84flags%E9%AA%8C%E8%AF%81/%E5%B0%86Intent%E7%9A%84flags%E4%BD%BF%E7%94%A8%E4%B8%80%E4%B8%8B%E5%91%80%E5%91%80%E5%91%80.md)
* [Activity 的使用走起来](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E4%BD%BF%E7%94%A8%E8%B5%B0%E8%B5%B7%E6%9D%A5.md)
* [Activity 启动流程的代码追踪](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B%E7%9A%84%E4%BB%A3%E7%A0%81%E8%BF%BD%E8%B8%AA.md)
* [Activity 的常见面试题，大家一起来看呀](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Activity%E7%9A%84%E5%B8%B8%E8%A7%81%E9%9D%A2%E8%AF%95%E9%A2%98%EF%BC%8C%E5%A4%A7%E5%AE%B6%E4%B8%80%E8%B5%B7%E6%9D%A5%E7%9C%8B%E5%91%80.md)


## 2. Service
　　Service 用于在后台完成用户指定的操作。Service 没有用户界面，但它会在后台一直执行。
**相关文章：**
　　关于 Service 的常见面试题
* Service 的基础知识，请看这一篇
* [关于 Service 的常见面试题，大家一起来看呀](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Service/%E5%85%B3%E4%BA%8EService%E7%9A%84%E5%B8%B8%E8%A7%81%E9%9D%A2%E8%AF%95%E9%A2%98%EF%BC%8C%E5%A4%A7%E5%AE%B6%E4%B8%80%E8%B5%B7%E6%9D%A5%E7%9C%8B%E5%91%80.md)

## 3. BroadcaseReceiver
　　BroadcaseReceiver 用于异步接收广播 Intent 。
**相关文章：**
　　关于 BroadcaseReceiver 的常见面试题

## 4. ContentProvider
　　ContentProvider 用于保存和获取数据，并使其对所有应用程序可见。这是不同应用程序间共享数据的唯一方式，因为 Android 没有提供所有应用共同访问的公共存储区。数据能够存储于文件系统、SQLite 数据库或其他方式。

**相关文章：**
　　关于 ContentProvider 的常见面试题

## 5. Handler、Message、Looper

**相关文章：**
　　关于 Handler、Message、Looper 的常见面试题

## 6. Binder
**相关文章：**
　　关于 Binder 的常见面试题


