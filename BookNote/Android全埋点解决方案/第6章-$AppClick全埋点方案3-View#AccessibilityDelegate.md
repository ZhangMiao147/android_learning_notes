# 第 6 章 $AppClick 全埋点方案 3：代理 View.AccessibilityDelegate

## 6.1. 关键技术

### 6.1.1. Accessibility

​		Accessibility，即辅助功能。许多 Android 用户有不同的能力（限制），这就要求他们可能会以不同的方式来使用他们的 Android 设备。这些限制包括视力、肢体、年龄等，这些限制可能会阻碍他们看到或充分使用触摸屏，而用户的听力丧失，有可能会让他们无法感知声音信息和警报信息。

​		Android 系统提供了辅助功能的特性和服务，可以帮助这些用户更容易的使用他们的 Android 设备，这些功能包括语音合成、触觉反馈、手势导航、轨迹球和方向键导航等。

​		Android 应用程序开发人员可以利用这些服务，使他们的应用程序更贴近用户的真实情况。该辅助服务在后台工作，由系统调用，用户界面的一些状态（比如 Button 被点击了）的改变可以通过回调 Accessibilityservice 的相应方法来通知用户（比如语音）。

### 6.1.2. View.AccessibilityDelegate

​		当一个 View 被点击的时候，系统会先调用当前 View 已设置的 mOnClickListener 对象的 onClick(view) 方法，然后再调用 sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED) 内部方法。在 sendAccessibilityEvent(int eventType) 方法的内部实现里，其实是调用 mAccessibilityDelegate 对象的 sendAccessibility-Event 方法，并传入当前 View 对象和 AccessibilityEvent.TYPE_VIEW_CLICKED 参数。

​		只需要代理 View 的 mAccessibilityDelegate 对象，当一个 View 被点击时，在原有 mOnClickListener 对象的相应方法执行之后，我们就能收到这个点击的 “消息”。代理 mAccessibilityDelegate 对象之后，我们就能拿到当前被点击的 View 对象，从而可以加入自动埋点的逻辑，进而实现 “插入” 埋点代码的效果。

## 6.2. 原理概述

​		在应用程序自定义的 Application 的 onCreate() 方法中初始化埋点 SDK，并传入当前的 Application 对象。埋点 SDK 就可以拿到这个 Application 对象，然后我们就可以通过 Application 的 registerActivityLifecycleCallback 方法来注册 Application.ActivityLifecycleCallbacks 回调。这样埋点 SDK 就可以对应用程序中所有的 Activity 的生命周期事件进行集中处理（监控）了。在 ActivityLifecycleCallbacks 的 onActivityResumed(Activity activity，Bundle bundle) 回调方法中，我们可以拿到当前正在显示的 Activity 对象，然后再通过 activity.getWindow().getDecorView() 方法或者 activity.findViewById(android.R.id.content) 方法拿到当前 Activity 的 RootView，通过 rootView.getViewTreeObserver() 方法可以拿到 RootView 的 ViewTreeObserver 对象，然后再通过 addOnGlobalLayoutListener() 方法给 RootView 注册 ViewTreeObserver.OnGlobalLayoutListener 监听器，这样我们就可以在收到当前 Activity 的视图状态发生改变时去主动遍历一次 RootView，并用我们自定义的 SensorsDataAccessibilityDelegate 代理当前 View 的 mAccessibilityDelegate 对象。在我们自定义的 SensorsDataAccessibilityDelegate 类中的 sendAccessibilityEvent(View host，int eventType) 方法实现里，我们先调用原有的 mAccessibilityDelegate 对象的 sendAccessibilityEvent 方法，然后再插入埋点代码，其中 host 即是当前被点击的 View 对象，从而可以做到自动埋点的效果。

## 6.3. 案例

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAppClick3

​		采集能力与前面代理 Window.callback 的方案一样。但该方案有一个很大的问题，就是辅助功能需要用户手动启动，而且在部分 Android ROM 上辅助功能可能会失效。

## 6.4. 扩展采集能力

### 6.4.1. 扩展 1：支持采集 RatingBar 的点击事件

​		RatingBar 是一个比较特殊的控件。当前这种全埋点方案，无法直接支持采集 RatingBar 的点击事件（以及滑动事件），我们还是需要参考前面的方案，即通过代理 RatingBar 的 RatingBar.OnRatingBarChangeListener 的方式曲线支持。

​		在代理的 onRatingChanged 方法中，先调用原 listener 对象的 onRatingChanged 方法，然后再调用埋点代码，这样即可实现 “插入”埋点代码的效果。

### 6.4.2. 扩展 2：支持采集 SeekBar 的点击事件

​		SeekBar 与 RatingBar 的情况是类似的，也是需要代理其相应的 listener 对象才能支持其点击事件。

### 6.4.3. 扩展 3：支持采集 Spinner 的点击事件

​		与 RatingBar、SeekBar 情况一样，也是需要通过代理其 listener 对象来实现，即代理 AdapterView.OnItemSelectedListener。

​		WrapperAdapterViewOnItemSelectedListener 继承 AdapterView.OnItemSelectedListener，在代理的 onItemSelected 方法中，先调用原有 listener 对象的相应方法，然后再调用我们自定义的 SensorsDataPrivate.trackAdapterView 方法来触发点击事件，这样即可实现 “插入” 埋点代码的效果。

### 6.4.4. 扩展 4：支持采集 ListView、GridView 的点击事件

​		当前是可以支持采集 ListView 和 GridView 的点击事件的，但这个只是把它们的 item 当作普通的自定义 View 来处理了。也就是说，我们可以采集点击事件，但在点击事件的详细属性信息里，无法知道当前是 ListView 还是 GridView，同时也无法知道点击的是第几个 item 和 Spinner 类似，ListView 和 GridView 都是 AdapterView 的子类，所以也需要采用代理其相应的 listener 对象，只不过它们设置的 listener 类型与 Spinner 不同而已。

​		WrapperAdapterViewOnItemClick 继承 AdapterView.OnItemClickListener，在代理的 onItemClick 方法中，先调用原有 listener 对象的相应方法，然后再调用自定义的 trackAdapterView 方法来触发点击事件，这样即可实现 “插入” 埋点代码的效果。

### 6.4.5. 扩展 5：支持采集 ExpandableListView 的点击事件

​		ExpandableListView 也是 AdapterView 的子类，同时也是 ListView 的子类。所以采用的方案与前面的 ListView、GridView 类似。

## 6.5. 缺点

* Application.ActivityLifecycleCallbacks 要求 API 14+；
* view.hasOnClickListeners() 要求 API 15+；
* removeOnGlobalLayoutListener 要求 API 16+；
* 由于使用反射，效率相对来说比较低，可能会引入兼容性方面问题的风险；
* 无法采集 Dialog、PopupWindow 等游离于 Activity 之外的控件的点击事件；
* 辅助功能需要用户手动开启，在部分 Android ROM 上辅助功能可能会失效。