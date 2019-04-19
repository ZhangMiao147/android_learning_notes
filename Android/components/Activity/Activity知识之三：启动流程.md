# Activity 知识之三：启动流程

	 本文内容


## 启动任务
　　通过为 Activity 提供一个以 “android.intent.action.MAIN” 为指定操作、以 “android.intent.category.LAUNCHER” 为执行类别的 Intent 过滤器，可以将 Activity 设置为任务的入口点。

　　此类 Intent 过滤器会使 Activity 的图标和标签显示在应用启动器中，让用户能够启动 Activity 并在启动之后随时返回到创建的任务中。

　　用户必须能够在离开任务后，再使用此 Activity 启动器返回该任务。因此，只有在 Activity 具有 ACTION_MAIN 和  CATEGORY_LAUNCHER 过滤器时，才应该使用将 Activity 标记为“始终启动任务“的两种启动模式，即 ”singleTask“ 和 ”singleInstance“。

　　如果并不想用户能够返回到 Activity ，对于这些情况，可以将 < activity > 元素的 finishOnTaskLaunch 设置为 ”true“ 。

## 参考文章：
1. [Android之Activity系列总结(二)--任务和返回栈](https://www.cnblogs.com/jycboy/p/6367330.html)





13. [3分钟看懂Activity启动流程](https://www.jianshu.com/p/9ecea420eb52)
