# Activity 知识之四：Activity 的使用

	本文目录
	1. 使用 Activity 显式简单界面
	2. Activity 的启动
		（1）显式启动
		（2）隐式启动
	3. Activity 之间的跳转

## 使用 Activity 显式简单界面

#### 创建一个 Activity 的子类 MainActivity ，继承 Activity


#### 创建 MainActivity 的界面 activity_main.xml

#### 在清单文件中声明 MainActivity

## Activity 的启动

#### 显示启动
　　显示启动的方式：

1. 直接在 Intent 构造方法启动：

2. setComponent

3. setClass / setClassName

#### 隐式启动

　　不想提供给其他应用的 Activity 不应有任何 Intent 过滤器。
　　不过，如果你想让 Activity 对衍生自其他应用（以及您的自有应用）的隐式 Intent 做出响应，则必须为 Activity 定义其他 Intent 过滤器。对于你想要作出响应的每一个 Intent 类型，你都必须加入相应的 < intent-filter >，其中包括一个 < action > 元素，还可选择性地包括一个 < category > 元素或一个 < data > 元素。这些元素指定 activity 可以响应的 Intent 类型。

## Activity 之间的跳转

#### 相关的 API
1. startActivity
　　通过调用 startActivity() 方法可以启动其他 Activity，在 Intent 中指定想要启动的 Activity ，Intent 对象还可能携带少量数据传递给想要启动的 Activity。
　　

2. startActivityForResult
　　有时，可能需要从启动的 Activity 获得结果。在这种情况下，就通过 startActivityForResult() 方法来启动 Activity ，被启动的 Activity 通过 setResult() 方法返回数据，启动的 Activity 通过 onActivityResult() 方法获取返回的数据。

## 结束 Activity
　　可以通过调用 Activity 的 finish() 方法来结束 Activity 。还可以通过调用 finishActivity() 方法结束之前启动的另一个 Activity 。
　　大多数情况下，不应该使用 finish() 或 finishActivity() 方法来结束 Activity 。Android 系统会管理 Activity 的生命周期，会在合适的时机结束 Activity 。调用结束 Activity 的方法可能对预期的用户体验产生不良影响，因此只应该在确实不想让用户返回此 Activity 实例时使用。

## 参考文章：
3. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)

6. [Android之Activity系列总结（一）--Activity概览](https://www.cnblogs.com/jycboy/p/6367282.html)

8. [Android之Activity系列总结(三)--Activity的四种启动模式](https://www.cnblogs.com/jycboy/p/6367829.html)
9. [Android 旋转屏幕--处理Activity与AsyncTask的最佳解决方案](http://www.cnblogs.com/jycboy/p/save_state_data.html)
10. [Activity之概览屏幕(Overview Screen)](https://www.cnblogs.com/jycboy/p/overview_screen.html)
11. [Android四大组件之Activity详解](https://www.cnblogs.com/caobotao/p/4987015.html)
13. [3分钟看懂Activity启动流程](https://www.jianshu.com/p/9ecea420eb52)