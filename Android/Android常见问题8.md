# Android 的常见问题 8

# 1. ListView 源码分析

## 1.1. ListView 的观察者模式

ListView 的父类 AbsListView 有一个变量 AdapterDataSetObserver ，作为观察者，用来通知数据变换。

在 onAttachedToWindow() 方法中注册观察者，在 onDetachedFromWindow() 方法中解除观察者。

在调用了 notifyDataSetChanged() 方法后，会采用观察者模式通知 ListView 重新绘制界面。调用 AdapterDataSetObserver 的 onChanged() 方法，而 onChanged() 方法中调用了 requestLayout() 方法。而 reqyestLayout() 就是 View 的 requestLayout() 方法，调用 scheduleTraversals() 方法，依次调用 onMeasure()、onLayout()、onDraw() 方法。

# 2. RecyclerView 源码分析

