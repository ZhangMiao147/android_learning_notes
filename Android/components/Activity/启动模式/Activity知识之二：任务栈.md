# Activity 知识之二：任务栈

## 关于栈
　　Android 中的 activity 全都归属于 task 管理（task 是一个具有栈结构的容器），task 是多个 activity 的集合，android 默认情况下会为每个 App 维持一个 task 来存放 App 的所有 activity (在默认情况下)，task 的默认 name 为该 app 的 packagename(包名)。

## 开启一个 task
　　可以通过给 activity 一个 intent filter (action 是 “android.intent.action.MAIN”，category 是“android.intent.category.LAUNCHER”)，让这个 activity 是一个 task 的进入点。例如，我们最常见的 MainActivity：
```
        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
```
　　一个这样的 intent filter 会使得这个 activity 的 icon 和 label 显示在程序启动处，提供了一种方法，使得用户可以启动这个 activity，当它启动后，用户也可以通过它来返回到这个 task 。
　　用户必须能够离开一个 task ，然后通过 activity launcher 返回到它。
　　因为这个原因，两个让 Activity 永远实例化一个 task 的启动模式："singleTask" 和 "singleInstance"，应该仅在 Activity 有一个 ACTION_MAIN 和 CATEGORY_LAUNCHER filter 的时候用它们。

## TaskAffinity属性与allowTaskReparenting
　　Affinity 指示了 Activity 更倾向于属于哪个 Task。
　　默认情况下，同一个应用的 Activity 倾向于在同一个 task 中。可以通过 <activity> 标签的 taskAffinity 来修改这种行为。

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