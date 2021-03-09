# 第 5 章 $AppClick 全埋点方案 2：Window.Callback

## 5.1. 关键技术

**Window.Callback**

​		Window.Callback 是 Window 类的一个内部接口。该接口包含了一系列类似于 dispatchXXX 和 onXXX 的接口。当 Window 接收到外界状态改变的通知时，就会回调其中的相应方法。比如，当用户点击某个控件时，就会回调 Window.Callback 中的 dispatchTouchEvent(MotionEvent event)方法。

## 5.2. 原理概述

​		在应用程序自定义的 Application 的 onCreate() 方法中初始化埋点 SDK，并传入当前的 Application 对象。埋点 SDK 在拿到这个 Application 对象之后，就可以调用 Application 的 registerActivityLifecycleCallback 方法来注册 Application.ActivityLifecycleCallbacks 回调。这样，埋点 SDK 就能对应用程序中所有的 Activity 的生命周期事件进行集中处理（监控）了。在 Application.ActivityLifecycleCallbacks 的 onActivityCreated(Activity activity，Bundle bundle) 回调方法中，我们可以拿到当前正在显示的 Activity 对象，通过 activity.getWindow() 方法可以拿到这个 Activity 对应的 Window 对象，再通过 window.getCallback() 方法就可以拿到当前对应的 Window.Callback 对象，最后通过自定义的 WrapperWindowCallback 代理这个 Window.Callback 对象。然后，在 WrapperWindowCallback 的 dispatchTouchEvent(MotionEvent event) 方法中通过 MotionEvent 参数找到那个被点击的 View 对象，并插入埋点代码，最后再调用原有 Window.Callback 的 dispatchTouchEvent(MotionEvent event) 方法，即可达到 “插入” 埋点代码的效果。

## 5.3. 案例

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAppClick2

​		与方案一相比，这种方案更好地解决了动态创建的 View 无法采集其点击事件的问题。但是，由于每次点击时都需要遍历一次 RootView，所以这种方案的效率相对较低，同时对应用程序的整体性能影响也比较大。而方案一只会在视图树状态发生变化的时候，才会去遍历一次 RootView。同时，这种方案也同样无法采集游离于 Activity 之外的 View 的点击事件，比如 Dialog、PopupWindow 等。

## 5.4. 扩展采集能力

### 5.4.1. 扩展 1：支持采集 RatingBar 的点击事件

​		根据上面的判断条件可知，支持采集 RatingBar 点击事件的其中一个条件就是要求 view.isClickable() 必须返回真。但通过测试发现，view.isClickable() 的判断条件对 RatingBar 总是失效的，即使给 RatingBar 设置了 listener 对象，isClickable() 方法返回的也是 false。其实，对于 RatingBar 的判断我们可以换一种思路，即只需要满足下面三个条件即可：

* View 处于显示状态，即 view.getVisibility() 是 View.VISIBLE；
* MotionEvent 的（x，y）坐标必须在 View 的内部；
* 当前 View 是 RatingBar 类型或者其子类类型。所以，对于支持采集 RatingBar 点击事件，我们只需要在 parent.isClickable() 上或（||）一个 (parentinstanceof RatingBar) 的条件即可。

### 5.4.2. 扩展 2：支持采集 SeekBar 的点击事件

​		与 RatingBar 一样，view.isClickable() 的判断条件对 SeekBar 也总是失效的，所以其解决方案与 RatingBar 的方案相同。

### 5.4.3. 扩展 3：支持采集 Spinner 的点击事件

​		Spinner 是一个更为特殊的控件，因为它的操作实际上是分为两步进行的。第一步是点击 Spinner 控件，然后会弹出一个选择窗口；第二步是选择其中某一项。第一步的点击和普通的 View 一样，很容易采集，但第二步选择某一项时，完全没有执行 Window.Callback 的 dispatchTouchEvent(MotionEvent event) 回调方法，所以我们根本无法截获到这个动作，从而导致无法采集选择某一项的点击事件。

​		Spinner 本身就提供了一个方法用来获取其设置的 mOnItemSelectedListener 对象，即 getOnItemSelectedListener() 方法，如果获取到的 mOnItemSelectedListener 对象不为空，并且又不是我们自定义的 WrapperAdapterViewOnItemSelectedListener 类型，则去代理即可。

### 5.4.4. 扩展 4：支持采集 ListView、GridView 的点击事件

​		有两个地方值得我们注意：第一，我们如何知道点击的是哪个 item？第二，我们该如何获取对应 item 的显示文本信息？这里有一个相对比较简单的处理方法，就是获取 ListView 或 GridView 的 SubViews，然后判断点击时对应的 (x，y) 坐标坐落在哪个 SubView 里，如果找到了 SubView，就能知道是哪个 item 了，同时也就能获取到显示文本了。

### 5.4.5. 扩展 5：支持采集 ExpandableListView点 击事件

​		无法获取点击时对应的 groupPosition 和 childPosition 信息。

​		通过代理分别获取 ExpandableListView 的 mOnChildClickListener 和 mOnGroupClick-Listener 对象，如果对应的 listener 对象不为空，并且又不是我们自定义的 WrapperOnChildClickListener 或 WrapperOnGroupClickListener 类型，则去代理其相应的 listener 对象即可。

## 5.5. 缺点

* 由于每次点击时，都需要去遍历一次 RootView，所以效率相对来说比较低，对应用程序的整体性能影响也比较大；
* view.hasOnClickListeners() 要求 API 15+；
* Application.ActivityLifecycleCallbacks 要求 API 14+；
* 无法采集像 Dialog、PopupWindow 等游离于 Activity 之外的控件的点击事件。