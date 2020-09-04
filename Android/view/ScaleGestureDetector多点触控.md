# 多点触控

# 概述

　　ScaleGestureDetector

public class ScaleGestureDetector extends Object

- java.lang.Object
  - android.view.ScaleGestureDetector

## 类概述

根据接收的 MotionEvent,  侦测由多个触点（多点触控）引发的变形手势。callback 方法ScaleGestureDetector.OnScaleGestureListener  会在特定手势事件发生时通知用户

。该类仅能和 Touch 事件引发的 MotionEvent 配合使用。使用该类需要

- 为你的 View 创建 ScaleGestureDetector  实例
- 确保在 onTouchEvent(MotionEvent)方法中调用 onTouchEvent (MotionEvent)
- 前者为该类的 onTouchEvent 方法，后者为 View 的 onTouchEvent 方法。在事件发生时，定义在 callback 中的方法会被调用。

ScaleGestureDetector 为 Android2.2 新增的类，允许 Views 可以通过提供的MotionEvents 检测和处理包括多点触摸在内的手势变化信息。

## 内部类

1. interface ScaleGestureDetector.OnScaleGestureListener手势发生时接收通知的监听器
2. classScaleGestureDetector.SimpleOnScaleGestureListener 若仅想监听一部分尺寸伸缩事件，可继承该类。

## 公共构造方法

public ScaleGestureDetector (Context context, ScaleGestureDetector.OnScaleGestureListener listener)
 

## 公共方法

#### public float getCurrentSpan ()

返回手势过程中，组成该手势的两个触点的当前距离。

返回值：以像素为单位的触点距离。



#### public long getEventTime ()

返回事件被捕捉时的时间。

返回值以毫秒为单位的事件时间。



#### public float getFocusX ()

返回当前手势焦点的 X 坐标。 



- 如果手势正在进行中，焦点位于组成手势的两个触点之间。
- 如果手势正在结束，焦点为仍留在屏幕上的触点的位置。
- 若 isInProgress（）返回 false，该方法的返回值未定义。



返回值：返回焦点的 X 坐标值，以像素为单位。



#### public float getFocusY ()

返回当前手势焦点的 Y 坐标。



-  如果手势正在进行中，焦点位于组成手势的两个触点之间。
- 如果手势正在结束，焦点为仍留在屏幕上的触点的位置。
- 若 isInProgress（）返回 false，该方法的返回值未定义。



返回值返回焦点的 Y 坐标值，以像素为单位。



#### public float getPreviousSpan ()

返回手势过程中，组成该手势的两个触点的前一次距离。

返回值两点的前一次距离，以像素为单位。



#### public float getScaleFactor ()

返回从前一个伸缩事件至当前伸缩事件的伸缩比率。该值定义为 (getCurrentSpan() / getPreviousSpan())。

返回值当前伸缩比率.



#### public long getTimeDelta ()

返回前一次接收到的伸缩事件距当前伸缩事件的时间差，以毫秒为单位。

返回值从前一次伸缩事件起始的时间差，以毫秒为单位。



#### public boolean isInProgress ()

如果手势处于进行过程中，返回 true.

返回值如果手势处于进行过程中，返回 true。否则返回 false。

# 参考文章

2. [Android_ScaleGestureDetector多点触控](https://blog.csdn.net/zhaoyazhi2129/article/details/40042693)

