# 第 7 章 $AppClick 全埋点方案：透明层

​		该方案主要用到了 Android 系统事件处理机制方面的知识。

## 7.1. 原理概述

### 7.1.1. View onTouchEvent

​		onTouchEvent 是在 View 中定义的一个方法，用来处理传递到 View 的手势事件。手势事件类型主要包括 ACTION_DOWN、ACTION_MOVE、ACTION_UP、ACTION_CANCEL 四种。该方法的返回值为 Boolean 类型，返回 true 表明当前 View 消费当前事件；返回 false 表明当前 View 不消费当前事件，事件继续向上传递给父控件的 onTouchEvent 方法。

### 7.1.2 原理概述

​		结合 Android 系统的事件处理机制，我们可以自定义一个透明的 View，然后添加到每个 Activity 的最上层（面）。这样，每当用户点击任何控件时，直接点击的其实就是我们这个自定义的透明 View。然后我们再重写 View 的 onTouchEvent(MotionEvent event) 方法，在 return super.onTouchEvent(event) 之前，就可以根据 MontionEvent 里的点击坐标信息（x，y），在当前 Activity 的 RootView 里找到实际上被点击的那个 View 对象。找到被点击的 View 之后，我们再通过自定义的 WrapperOnClickListener 代理当前 View 的 mOnClickListener 对象。自定义的 WrapperOnClickListener 类实际上实现了 View.OnClickListener 接口，在 WrapperOnClickListener 的 onClick(View view) 方法里会先调用 View 的原有 mOnClickListener 的 onClick(View view) 处理逻辑，然后再插入埋点代码，就能达到自动埋点效果了。

## 7.2. 案例

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAppClick4

​		什么是 View Elevation？View Elevation，又叫视图高度。从 Android 5.0 开始，Google 引入了 View Elevation 的概念，同时也就引用了 Z 轴的概念。通过 Elevation，我们可以设置当前 View “ 浮 ” 起来的高度，即通过该属性可以让组件呈现出 3D 的效果。我们通过设置一个较大的 Elevation 值，可以尽量确保我们添加的透明层在当前 Activity 的最上层。

​		该方案的采集能力与方案二、方案三基本一致。每次点击时，同样需要遍历一次当前正在显示的 Activity 的 RootView。该方案同样也无法采集游离于 Activity 之上的 View 的点击行为事件，如无法采集 Dialog、PopupWindow 等的点击。

## 7.3. 扩展采集能力

* 扩展 1：支持采集 CheckBox 的点击事件

* 扩展 2：支持采集 SeekBar 的点击事件
* 扩展 3：支持采集 RatingBar 的点击事件
* 扩展 4：支持采集 Spinner 的点击事件
* 扩展 5：支持采集 ListView、GridView 的点击事件
* 扩展 6：支持采集 ExpandableListView 的点击事件

​		Github 上目前也有一个类似方案的开源项目，可以参考：https://github.com/foolchen/AndroidTracker。

## 7.4. 缺点

* Application.ActivityLifecycleCallbacks 要求API 14+；
* view.hasOnClickListeners() 要求API 15+；
* 无法采集 Dialog、PopupWindow 的点击事件；
* 每次点击都需要遍历一次 RootView，效率比较低。