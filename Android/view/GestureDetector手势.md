# Android手势---GestureDetector

# 概述

　　GestureDetector

GestureDetector 可以使用 MotionEvents 检测各种手势和事件。

这个类只能用于检测触摸事件的 MotionEvent

------

GestureDetector有三个内部接口，两个内部类

### 三个核心接口

- OnGestureListener
- OnDoubleTapListener
- OnContextClickListener

他们都是起到监听器的作用

### 两个内部类：

- SimpleOnGestureListener

**注意：SimpleOnGestureListener继承了GestureDetector的三个内部接口**

- GestureHandler
   处理GestureDetector类的数据

### 构造方法

GestureDetector自带了五个构造方法，但是有两个被废弃了



```java
public GestureDetector(Context context, OnGestureListener listener) 
public GestureDetector(Context context, OnGestureListener listener, Handler handler) 
public GestureDetector(Context context, OnGestureListener listener, Handler handler, boolean unused){this(context, listener, handler);} 
```

因为第三个调用第二个，所以只要关注前面两个就行了

我们发现第二个构造方法中多了一个Handler变量
 一般来说，不会使用这个构造方法

但是因为GestureDetector中的数据是给GestureHandler内部类进行处理，这个类会使用Handle，由Handler的知识知道，创建Handler必须有Looper，但是在一些新开的线程中没有创建Looper，所以我们需要传入一个带了Looper的Handler变量，否则，GestureDetector对象会创建失败

### SimpleOnGestureListener类的方法：



```java
//下面的6个方法继承自OnGestureListener
public boolean onSingleTapUp( MotionEvent e)
当用户单击时触发

public void onLongPress (MotionEvent e )
当用户手指在长按屏幕时触发

public boolean onScroll (MotionEvent e1 , MotionEvent e2 ,float distanceX , float distanceY)
当用户手指在屏幕上拖动时触发
后面两个变量时在X,Y上移动的距离

public boolean onFling ( MotionEvent e1 ,MotionEvent e2 , float velocityX , float velocityY)
当用户手指拖动后，手指离开屏幕时触发
这个方法常用来使手指离开后页面仍然可以滑动（速度慢慢变小）
后两个变量表示手指在X,Y两个方向上的速度

public void onShowPress (MotionEvent  e)
当用户手指按下，但没有移动时触发该方法

public boolean onDown ( MotionEvent e)  
当按下时触发该方法，所有手势第一个必定触发该方法

//下面的三个方法继承自OnDoubleTapListener
public boolean onDoubleTap (MotionEvent e)
当用户双击时触发

public boolean onDoubleTapEvent (MotionEvent e)
在双击事件确定发生时会对第二次按下产生的 MotionEvent 信息进行回调。

public boolean onSingleTapConfirmed ( MotionEvent e)
当单击事件确定后进行回调

//下面的这个方法继承自ContextClickListener
onContextClick ( MotionEvent e)
用于检测外部设备上的按钮是否按下的，例如蓝牙触控笔上的按钮，一般情况下，忽略即可。

如果侦听 onContextClick（MotionEvent），
则必须在 View 的onGenericMotionEvent（MotionEvent）中
调用 GestureDetector OnGenericMotionEvent（MotionEvent）。
```

## 手势的常用事件

### 按下



```java
public boolean onDown (MotionEvent e)
```

按下是所以手势必定有的动作
 所以无论什么手势，第一个调用的方法就是onDown

**注意：
 GestureDetector的点击对图片，按钮等无用**

为了解决这个问题，我们可以

- onDown返回true（根据响应中的先回调，后监听）
- 图片等的xml文件中设置android:Clickable="true"

### 单击：



```java
public boolean onSingleTapUp( MotionEvent e)

public boolean onSingleTapConfirmed ( MotionEvent e)
```

这两个方法都可以响应到单击事件，但是他们之间还是有区别的

当我们同时在监听器中覆写这两个方法，并且进行单击事件
 我们会发现当进行单击事件的时候，这几个方法响应的顺序是这样的

> onDown() -> onSingleTapUp() -> onSingleCofirmed()

首先onDown()必定是第一个执行的，但是会发现onSingleTapUp在onSingleComfirmed之前执行
 我查阅了相关文档，发现他们虽然同样响应的是当手指离开屏幕的活动，但是**onSingleTapUp是立即执行**，而**onSingleComfirmed却要在离开后300ms后才执行** ，这样的目的是确认我们进行的是单击事件（为了防止我们在300ms内再次进行单击事件），所以他们的名字分别是Up和Comfirmed

所以，在**设置双击事件时，最好使用onSingleComfirmed（），进行双击时不会回调单击方法**

### 双击



```java
public boolean onDoubleTap (MotionEvent e)

public boolean onDoubleTapEvent (MotionEvent e)
```

这两个方法都可以响应双击事件，为了验证他们的区别，我们同样在一个响应器中覆写这两个方法，并且进行双击事件(我们这里单击事件使用onSingleComfirmed)

我们使用Log.e()把方法响应的顺序弄出来，发现他们规律很神奇

> onDown() -> onDoubleTap() -> onDoubleTapEvent() -> onDown()-> onDoubleTapEvent() ->onDoubleTapEvent()(不定数目个)

然后我在onDoubleTapEvent()中吧MotionEvent e 输出,
 然后方法响应的顺序变成：

> onDown() -> onDoubleTap() -> onDoubleTapEvent() -> onDown()-> onDoubleTapEvent() Down -> onDoubleTapEvent()Move(不定数目个) ->onDoubleTapEvent() Up

我们对以上信息进行分析：
 DoubleTap在DoubleTapEvent前面执行，但是根据DoubleTapEvent的第一个MotionEvent是Down，所以判断**双击的响应条件是在第一次单击后的300ms内按下手指**

我们又尝试在双击后手指不离开屏幕，可见随时间的延长，Move也变得更多，
 由此判断，**onDoubleTapEvent是实时回调的，并且是用来检测MotionEvent**

### 长按



```java
public void onShowPress (MotionEvent e)

public void onLongPress (MotionEvent e)
```

这次我们在响应器中覆写所有方法，进行长按操作
 发现方法响应的顺序为：

> onDown -> onShowPress -> onLongPress

也就是说，在长按时，onShowPress在onLongPress前面执行

### 滑动/拖动



```java
public void onScroll (MotionEvent e)

public void onFling (MotionEvent e)
```

同样，我们覆写所有方法，进行滑动操作,
 发现方法的响应顺序为

> onDown -> onScroll ->  不定量个onScroll ->onScroll -> onFiling

由此可见，在滑动/拖动过程中，不断调用onScroll，最后调用onFiling

当然，Android自带的手势不能完全满足我们的需求，于是Android提供了自建手势的方法，这些内容将在下一篇博客中陈述.

# 参考文章

2. [Android手势---GestureDetector](https://www.jianshu.com/p/956f39570de8)

