# View 事件分发机制

　　Android 的事件分发机制基本会遵从 Activity -> ViewGroup -> View 的顺序进行事件分发，然后通过调用 onTouchEvent() 方法进行事件的处理。

　　一般情况下，事件列都是从用户按下（ACTION_DOWN）的那一刻产生的，不得不提到，三个非常重要的于事件相关的方法。

* dispatchTouchEvent() - 分发事件
* onTouchEvent() - 处理事件
* onInterceptTouchEvent() - 拦截事件

## 1. Activity 的事件分发机制

　　dispatchTouchEvent() 是负责事件分发的。当点击事件产生后，事件首先会传递给当前的 Activity，这会调用 Activity 的 dispatchTouchEvent() 方法。

### 1.1. Activity#dispatchTouchEvent

```java
    /**
     * Called to process touch screen events.  You can override this to
     * intercept all touch screen events before they are dispatched to the
     * window.  Be sure to call this implementation for touch screen events
     * that should be handled normally.
     *
     * @param ev The touch screen event.
     *
     * @return boolean Return true if this event was consumed.
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
          	// 由于事件开始一般都为 down 事件（按下）
          	// 所以一般都会调用该方法
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
          	// 若 getWindow().superDispatchTouchEvent(ev) 返回 true
          	// 则 Activity.dispatchTouchEvent() 也返回 true，停止事件传递
            return true;
        }
      	// 否则直接调用 onTouchEvent(ev)
        return onTouchEvent(ev);
    }
```

　　一般开始产生点击事件都是 MotionEvent.ACTION_DOWN，所以一般都会调用到 onUserInteraction() 这个方法。

　　下面的 if 判断，getWindow().superDispatchTouchEvent()，getWindow() 明显是获取 Window，由于 Window 是一个抽象类，所以能拿到的是其子类 PhoneWindow。而 PhoneWindow 的 superDispatchTouchEvent() 方法就是调用了 ViewGroup 的 dispatchTouchEvent()。

### 1.2. Activity#onUserInteraction

```java
    /**
     * Called whenever a key, touch, or trackball event is dispatched to the
     * activity.  Implement this method if you wish to know that the user has
     * interacted with the device in some way while your activity is running.
     * This callback and {@link #onUserLeaveHint} are intended to help
     * activities manage status bar notifications intelligently; specifically,
     * for helping activities determine the proper time to cancel a notfication.
     *
     * <p>All calls to your activity's {@link #onUserLeaveHint} callback will
     * be accompanied by calls to {@link #onUserInteraction}.  This
     * ensures that your activity will be told of relevant user activity such
     * as pulling down the notification pane and touching an item there.
     *
     * <p>Note that this callback will be invoked for the touch down action
     * that begins a touch gesture, but may not be invoked for the touch-moved
     * and touch-up actions that follow.
     *
     * @see #onUserLeaveHint()
     */
    public void onUserInteraction() {
    }
```

　　这个方法实现是空的，该方法主要的作用是实现屏保功能，并且当此 Activity 在栈顶的时候，触屏点击 Home、Back、Recent 键等都会触发这个方法。

### 1.3. PhoneWindow#superDispatchTouchEvent

```java
    @Override
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return mDecor.superDispatchTouchEvent(event);
    }
```

　　PhoneWindow 的 superDispatchTouchEvent() 方法直接调用了 DecorView 的 superDispatchTrackballEvent() 方法。DecorView 继承于 FrameLayour，作为顶层 View，是所有界面的父类。而 FrameLayout 作为 ViewGroup 的子类，所以直接调用了 ViewGroup 的 dispatchTouchEvent()。

### 1.4. Activity的事件分发示意图

![](image/Activity的事件分发示意图.png)

## 2. 案例探究

　　为了研究 View 的事件分发，自定义了一个 MyButton 继承 Button，然后把跟事件传播有关的方法进行重写，并且添加日志：

```java
public class MyButton extends Button {

    public static final String TAG = MyButton.class.getSimpleName();

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG,"dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG,"dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG,"dispatchTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG,"onTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG,"onTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG,"onTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
```

　　在 onTouchEvent 和 dispatchTouchEvent 中打印了日志。然后使用自定义按钮：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/my_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <com.example.viewgroupdistribute.MyButton
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Button" />

</LinearLayout>
```

```java
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MyButton button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (MyButton) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "you clicked button1");
            }
        });
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG,"onTouch ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG,"onTouch ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG,"onTouch ACTION_UP");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }
}

```

　　在 MainActivity 中，还给 MyButton 设置了 onTouchListener 这个监听。

　　然后运行程序，点击按钮，日志输入如下：

```java
07-01 20:49:57.801 10972-10972/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_DOWN
07-01 20:49:57.804 10972-10972/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_DOWN
07-01 20:49:57.804 10972-10972/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_DOWN
07-01 20:49:57.875 10972-10972/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_MOVE
07-01 20:49:57.875 10972-10972/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_MOVE
07-01 20:49:57.875 10972-10972/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_MOVE
07-01 20:49:57.887 10972-10972/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_UP
07-01 20:49:57.887 10972-10972/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_UP
07-01 20:49:57.887 10972-10972/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_UP
07-01 20:49:57.952 10972-10972/com.example.viewgroupdistribute D/MainActivity: you clicked button1
```

　　可以看到，不管是 DOWN、MOVE 还是 UP 都是按照下面的顺序执行：

1. dispatchTouchEvent
2. setOnTouchListener 的 onTouch
3. onTouchEvent

　　如果想给按钮注册一个点击事件，只需要调用：

```java
button.setOnClickListener(new OnClickListener() {
	@Override
	public void onClick(View v) {
		Log.d("TAG", "onClick execute");
	}
});
```

　　这样在 onClick 方法里面写实现，就可以在按钮被点击的时候执行。

　　如果想给这个按钮再添加一个 touch 事件，只需要调用:

```java
button.setOnTouchListener(new OnTouchListener() {
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("TAG", "onTouch execute, action " + event.getAction());
        ...
		return false;
	}
});
```

　　onTouch 方法里能做的事情比 onClick 要多一些，比如判断手指按下、抬起、移动等事件。

　　那么如果两个事件都注册了，onTouch 是优先于 onClick 执行的，并且 onTouch 执行了两次，一次是 ACTION_DOWN，一次是 ACTION_UP。因此事件传递的顺序是先经过 onTouch，再传递给 onClick。

　　onTouch 方法是有返回值的，如果把 onTouch 方法里的返回值改成 true，onClick 方法不再执行了。

## 3. 源码分析

　　只要触摸到了任何一个控件，就一定会调用该控件的 dispatchTouchEvent 方法。当去点击按钮的时候，就会去调用 Button 类里的 dispatchTouchEvent 方法，其实调用的是 View 里的 dispatchToucheEvent 方法。

### 3.1. View#dispatchTouchEvent

```java
    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        // If the event should be handled by accessibility focus first.
        if (event.isTargetAccessibilityFocus()) {
            // We don't have focus or no virtual descendant has it, do not handle the event.
            if (!isAccessibilityFocusedViewOrHost()) {
                return false;
            }
            // We have focus and got the event, then use normal event dispatch.
            event.setTargetAccessibilityFocus(false);
        }

        boolean result = false;

        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(event, 0);
        }

        final int actionMasked = event.getActionMasked();
        // MotionEvent.ACTION_DOWN
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            // Defensive cleanup for new gesture
            stopNestedScroll();
        }

        if (onFilterTouchEventForSecurity(event)) {
            if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
                result = true;
            }
            //noinspection SimplifiableIfStatement
            ListenerInfo li = mListenerInfo;
          	// 必须满足三个条件都为真，才会返回 true
          	// 1. mOnTouchListener 不为 null，即调用了 setOnTouchListener()
          	// 2. (mViewFlags & ENABLED_MASK) == ENABLED
            // 3. li.mOnTouchListener.onTouch(this, event) 
            if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                	// 调用 li.mOnTouchListener.onTouch 方法
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
			// 调用 onTouchEvent 方法
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }

        if (!result && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }

        // Clean up after nested scrolls if this is the end of a gesture;
        // also cancel it if we tried an ACTION_DOWN but we didn't want the rest
        // of the gesture.
        if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL ||
                (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
            stopNestedScroll();
        }

        return result;
    }
```

　　在 View 的 dispatchTouchEvent 方法内，会进行一个判断，如果 if (li != null && li.mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED && li.mOnTouchListener.onTouch(this, event)) 为 true，则返回 true，否则就会执行 onTouchEvent(event) 方法。

1. 第一个条件 li != null

   li 是一个 ListenerInfo 对象，会在 getListenerInfo() 方法中初始化对象。

   ```java
       @UnsupportedAppUsage
       ListenerInfo getListenerInfo() {
           if (mListenerInfo != null) {
               return mListenerInfo;
           }
           mListenerInfo = new ListenerInfo();
           return mListenerInfo;
       }
   ```

2. 第二个条件 li.mOnTouchListener != null

    li.mOnTouchListener 是一个 OnTouchListener 对象，是在 View 的 setTouchListener() 方法中设置的：

   ```java
       /**
        * Register a callback to be invoked when a touch event is sent to this view.
        * @param l the touch listener to attach to this view
        */
       public void setOnTouchListener(OnTouchListener l) {
           getListenerInfo().mOnTouchListener = l;
       }
   ```

   也就是说只要给控件注册了 touch 回调，mListenerInfo 会被初始化了，mOnTouchListener 也会被赋值了。

3. 第三个条件：mViewFlags & ENABLED_MASK) == ENABLED && li.mOnTouchListener.onTouch(this, event)

   (mViewFlags & ENABLE_MASK) == ENABLED 是判断当前点击的控件是否是 enable 的，按钮默认都是 enable 的，因此这个条件恒定为 true。

   li.mOnTouchListener.onTouch(this, event) 其实就是去回调控件注册 touch 事件的 onTouch 方法。

　　也就是说如果控件注册了 onTouchListener 事件，并且在 onTouch 方法里返回 true，就会让这三个条件全部成立，从而整个方法直接返回 true。如果控件是不可点击的或者OnTouchListener.OnTouch 存在并返回 false，就会去调用到下面的代码，从而执行 onTouchEvent(event) 方法。

### 3.2. View#onTouchEvent

```java
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int viewFlags = mViewFlags;
        final int action = event.getAction();
		// 判断是否是可点击的，可以点击或者长按
        final boolean clickable = ((viewFlags & CLICKABLE) == CLICKABLE
                || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                || (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE;
		// 判断当前 View 是不是 disable 状态
        if ((viewFlags & ENABLED_MASK) == DISABLED) {
            if (action == MotionEvent.ACTION_UP && (mPrivateFlags & PFLAG_PRESSED) != 0) {
                setPressed(false);
            }
            mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return clickable;
        }
        // 如果设置了 onTouchDelegate，则会将事件交给代理者处理
        if (mTouchDelegate != null) {
            if (mTouchDelegate.onTouchEvent(event)) {
                // 直接 return true
                return true;
            }
        }
		// 可点击或者长按，最终一定 return true
        if (clickable || (viewFlags & TOOLTIP) == TOOLTIP) {
            switch (action) {
                // 处理 MotionEvent.ACTION_UP 事件
                case MotionEvent.ACTION_UP:
                    mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                    if ((viewFlags & TOOLTIP) == TOOLTIP) {
                        handleTooltipUp();
                    }
                    if (!clickable) {
                        removeTapCallback();
                        removeLongPressCallback();
                        mInContextButtonPress = false;
                        mHasPerformedLongPress = false;
                        mIgnoreNextUpEvent = false;
                        break;
                    }
                    // 判断 mPrivateFlags 是否包含 PFLAG_PREPRESSED 标识
                    boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                    // 如果 mPrivateFlags 包含 PFLAG_PRESSED 或者 PFLAG_PREPRESSED 标志
                    if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }
						// 如果还是 PFLAG_PREPRESSED 标志
                        if (prepressed) {
                            // The button is being released before we actually
                            // showed it as pressed.  Make it show the pressed
                            // state now (before scheduling the click) to ensure
                            // the user sees it.
                            // 给 mPrivateFlags 设置 PFLAG_PRESSED 标志，并刷新背景
                            setPressed(true, x, y);
                        }

                        // mHasPerformedLongPress 为 false
                        // 可以点击或者长按时，mHasPerformedLongPress 为 false
                        // 只有在 performLongPressCallback() 方法返回 true 的时候才会设置为 true，而只有调用 setOnLongClickListener() 设置了长按回调，并且在 onLongClick 方法中返回了 true，才会设置为 true。
                        if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                // mPerformClick 为 null
                                if (mPerformClick == null) {
                                    // 初始化 mPerformClick 实例
                                    mPerformClick = new PerformClick();
                                }
                                // 将 mPerformClick 添加到消息队列尾部
                                // 如果失败，则运行 performClick() 方法
                                // 如果成功，则会调用 mPerformClick 的 run 
                                if (!post(mPerformClick)) {
                                    performClick();
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }

                        // 如果 PFLAG_PREPRESSED 标志
                        if (prepressed) {
                            // 发送延时消息，延时时间为 64ms
                            // 时间到了后，会运行 mUnsetPressedState 的 run() 方法
                            postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } 
                        // 如果不是 PFLAG_PREPRESSED 标志
                        // 将 mUnsetPressedState 添加到消息队列尾部
                        // 添加成功，等待运行 mUnsetPressedState 的 run() 方法
                        else if (!post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            // 添加失败直接运行 mUnsetPressedState 的 run() 方法
                            mUnsetPressedState.run();
                        }
						
                        // 移除单击回调
                        removeTapCallback();
                    }
                    mIgnoreNextUpEvent = false;
                    break;
				// 处理 ACTION_DOWN 事件
                case MotionEvent.ACTION_DOWN:
                    if (event.getSource() == InputDevice.SOURCE_TOUCHSCREEN) {
                        mPrivateFlags3 |= PFLAG3_FINGER_DOWN;
                    }
                    mHasPerformedLongPress = false;

                    if (!clickable) {
                        checkForLongClick(0, x, y);
                        break;
                    }

                    if (performButtonActionOnTouchDown(event)) {
                        break;
                    }

                    // Walk up the hierarchy to determine if we're inside a scrolling container.
                    boolean isInScrollingContainer = isInScrollingContainer();

                    // For views inside a scrolling container, delay the pressed feedback for
                    // a short period in case this is a scroll.
                    // 判断父控件是可以滚动的
                    if (isInScrollingContainer) {
                        // 给 mPrivateFlags 设置 PFLAG_PREPRESSED 标志
                        mPrivateFlags |= PFLAG_PREPRESSED;
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap();
                        }
                        mPendingCheckForTap.x = event.getX();
                        mPendingCheckForTap.y = event.getY();
                        //延时处理点击，为了防止点击时滚动，延时时间是 100ms，到时间后调用 mPendingCheckForTap 的 run() 方法
                        postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    } else {
                        // Not inside a scrolling container, so show the feedback right away
                        setPressed(true, x, y);
                        checkForLongClick(0, x, y);
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (clickable) {
                        setPressed(false);
                    }
                    removeTapCallback();
                    removeLongPressCallback();
                    mInContextButtonPress = false;
                    mHasPerformedLongPress = false;
                    mIgnoreNextUpEvent = false;
                    mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (clickable) {
                        drawableHotspotChanged(x, y);
                    }

                    // Be lenient about moving outside of buttons
                    // 判断触摸点是否还在当前 View 上
                    if (!pointInView(x, y, mTouchSlop)) {
                        // Outside button
                        // Remove any future long press/tap checks
                        // 不在当前 View 上了
                        // 移除点击的回调
                        removeTapCallback();
                        // 移除长按的回调
                        removeLongPressCallback();
                        if ((mPrivateFlags & PFLAG_PRESSED) != 0) {
                            setPressed(false);
                        }
                        mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                    }
                    // 深压
                   	final boolean deepPress =
                            motionClassification == MotionEvent.CLASSIFICATION_DEEP_PRESS;
                    if (deepPress && hasPendingLongPressCallback()) {
                        // process the long click action immediately
                        removeLongPressCallback();
                        checkForLongClick(
                                0 /* send immediately */,
                                x,
                                y,
                                TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__DEEP_PRESS);
                    }
                    break;
            }

            return true;
        }

        return false;
    }
```



　　如果当前 View 是 Disable 状态，(viewFlags & ENABLED_MASK) == DISABLED，并且是可点击则会消费掉事件，return false。

　　如果设置了 onTouchDelegate，则会将事件交给代理者处理，直接 return true，如果希望自己的 View 增加它的 touch 范围，可以尝试使用 TouchDelegate。

　　如果 clickable || (viewFlags & TOOLTIP) == TOOLTIP 为 true，clickable 表示 View 是可以点击或者可以长按，最终一定 return true，通常都会采用 setOnClickListener() 和 setOnLongClickListener() 做设置。

 　　接下来就是 switch(event.getAction) 了，判断事件类型，DOWN、MOVE、UP 等

### 3.3. MotionEvent.ACTION_DOWN

```java
                    if (event.getSource() == InputDevice.SOURCE_TOUCHSCREEN) {
                        mPrivateFlags3 |= PFLAG3_FINGER_DOWN;
                    }
                    mHasPerformedLongPress = false;

                    if (!clickable) {
                        checkForLongClick(0, x, y);
                        break;
                    }

                    if (performButtonActionOnTouchDown(event)) {
                        break;
                    }

                    // Walk up the hierarchy to determine if we're inside a scrolling container.
                    boolean isInScrollingContainer = isInScrollingContainer();

                    // For views inside a scrolling container, delay the pressed feedback for
                    // a short period in case this is a scroll.
                    // 判断父控件是可以滚动的
                    if (isInScrollingContainer) {
                        // 给 mPrivateFlags 设置 PFLAG_PREPRESSED 标志
                        mPrivateFlags |= PFLAG_PREPRESSED;
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap();
                        }
                        mPendingCheckForTap.x = event.getX();
                        mPendingCheckForTap.y = event.getY();
                        //延时处理点击，为了防止点击时滚动，延时时间是 100ms，到时间后调用 mPendingCheckForTap 的 run() 方法
                        postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    } else {
                        // Not inside a scrolling container, so show the feedback right away
                        setPressed(true, x, y);
                        checkForLongClick(0, x, y);
                    }
```

　　在代码中，显示判断是否父控件是可以滚动的，如果是将 mProvateFlags 设置为 PFLAG_PREPRESSED，然后调用 postDelayed() 方法延时处理点击，防止点击是滚动，延时时间为 TAP_TIMEOUT = 100，到了时间之后，会调用 CheckForTap 的 run 方法。

#### 3.3.1. CkeckForTap#run

```java
    private final class CheckForTap implements Runnable {
        public float x;
        public float y;

        @Override
        public void run() {
            // 取消 PFLAG_PREPRESSED 标志
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            // 设置 PFLAG_PRESSED 标志，并刷新背景
            setPressed(true, x, y);
            // 检测长按的时间 500 - 100
            final long delay =
                    ViewConfiguration.getLongPressTimeout() - ViewConfiguration.getTapTimeout();
            // 检测长按
            checkForLongClick(delay, x, y, TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__LONG_PRESS);
        }
    }
```

　　在 CheckForTap 的 run 方法里面取消了 mPrivateFlags 的 PFLAG_PREPRESSED 标志，然后设置 PFLAG_PRESSED 标志，刷新背景，如果 View 支持长按事件，则再发一个延时消息，检测长按，延时时间的时间为 500（长按的检测时间）- 100（PFLAG_PREPRESSED 滚动的检测时间）。这的逻辑和 onTouchEvent() 方法里面如果父类没有滑动的处理基本相同：

```java
setPressed(true, x, y);
checkForLongClick(
                                ViewConfiguration.getLongPressTimeout(),
                                x,
                                y,
                                TOUCH_GESTURE_CLASSIFIED_iew#_CLASSIFICATION__LONG_PRESS);
```

　　检测长按的时长不同。如果父类没有滚动的控件，则延时检测长按的时间为 500，不需要减去检测有没有滚动的时间。

#### 3.3.2. View#setPressed

　　不同参数的 setPressed() 方法最终都会调用到下面的方法： 

```java
    public void setPressed(boolean pressed) {
        // 是否需要刷新
        final boolean needsRefresh = pressed != 
            // mPrivateFlags 是否有 PFLAG_PRESSED 标志
            ((mPrivateFlags & PFLAG_PRESSED) == PFLAG_PRESSED);

        if (pressed) {
            // mPrivateFlags 设置 PFLAG_PRESSED 标志
            mPrivateFlags |= PFLAG_PRESSED;
        } else {
            // mPrivateFlags 清除 PFLAG_PRESSED 标志
            mPrivateFlags &= ~PFLAG_PRESSED;
        }

        if (needsRefresh) {
            // 刷新背景
            refreshDrawableState();
        }
        // 传递给子 view
        dispatchSetPressed(pressed);
    }
```

　　根据传递的 pressed 与 mPrivateFlag 有没有 PFLAG_PRESSED 是否相同来判断出是否需要重新刷新，如果是按下，则 mPrivateFlag  设置PFLAG_PRESSED，如果不是按下，则 mPrivateFlag  取消 PFLAG_PRESSED 状态。如果需要刷新，则调用 refreshDrawableState() 刷新背景。

#### 3.3.3. View#checkForLongClick

```java
    private CheckForLongPress mPendingCheckForLongPress;

	private void checkForLongClick(long delay, float x, float y, int classification) {
        // 支持长按
        if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE || (mViewFlags & TOOLTIP) == TOOLTIP) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            mPendingCheckForLongPress.setAnchor(x, y);
            mPendingCheckForLongPress.rememberWindowAttachCount();
            mPendingCheckForLongPress.rememberPressedState();
            mPendingCheckForLongPress.setClassification(classification);
            // 发送延迟消息
            // 时间到了后，会执行 mPendingCheckForLongPress 的 run() 方法
            postDelayed(mPendingCheckForLongPress, delay);
        }
    }
```

　　checkForLongClick() 方法就是设置  mPendingCheckForLongPress 的一些参数，然后发送延时消息出去。

#### 3.3.4. View#CheckForLongPress

```java
    private final class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;
        private float mX;
        private float mY;
        private boolean mOriginalPressedState;
        /**
         * The classification of the long click being checked: one of the
         * StatsLog.TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__* constants.
         */
        private int mClassification;

        @Override
        public void run() {
            if ((mOriginalPressedState == isPressed()) && (mParent != null)
                    && mOriginalWindowAttachCount == mWindowAttachCount) {
                recordGestureClassification(mClassification);
                // 调用 performLongClick() 方法
                // 如果 performLongClick() 返回 true，则将 mHasPerformedLongPress 置为 true，将来在处理 ACTION_UP 的时候将不会运行 performClick() 方法，也就是不会执行单击的回调方法
                if (performLongClick(mX, mY)) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void setAnchor(float x, float y) {
            mX = x;
            mY = y;
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = mWindowAttachCount;
        }

        public void rememberPressedState() {
            mOriginalPressedState = isPressed();
        }

        public void setClassification(int classification) {
            mClassification = classification;
        }
    }
```

　　长按的处理就是调用 performLongClick() 方法。

#### 3.3.5. View#performLongClick

```java
    public boolean performLongClick(float x, float y) {
        mLongClickX = x;
        mLongClickY = y;
        // 调用 performLongClick() 方法
        final boolean handled = performLongClick();
        mLongClickX = Float.NaN;
        mLongClickY = Float.NaN;
        // 返回 performLongClick() 方法的返回值
        return handled;
    }

    public boolean performLongClick() {
        // 调用 performLongClickInternal() 方法
        return performLongClickInternal(mLongClickX, mLongClickY);
    }

    private boolean performLongClickInternal(float x, float y) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);

        boolean handled = false;
        final ListenerInfo li = mListenerInfo;
        // li.mOnLongClickListener 就是 setOnLongClickListener() 方法设置的长按的回调
        if (li != null && li.mOnLongClickListener != null) {
            handled = li.mOnLongClickListener.onLongClick(View.this);
        }
        if (!handled) {
            final boolean isAnchored = !Float.isNaN(x) && !Float.isNaN(y);
            handled = isAnchored ? showContextMenu(x, y) : showContextMenu();
        }
        if ((mViewFlags & TOOLTIP) == TOOLTIP) {
            if (!handled) {
                handled = showLongClickTooltip((int) x, (int) y);
            }
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }
```

　　如果调用 setOnLongClickListener 设置了长按的回调，则执行长按时的回调。

　　如果长按的回调返回 true，会把 mHasPerformedLongPress 置为 true。否则，如果没有设置长按回调或者长按回调返回的是 false，则 mHasPerformedLongPress  依然是 false。

#### 3.3.6. 总结

　　可以看到，当用户按下，如果父类有滑动，首先会设置标识为 PFLAG_PREPRESSED，然后发送一个延时操作，延时时间为 100ms，到时间后，会取消 PFLAG_PREPRESSED 的标志，然后和父类没有滑动的处理一样。

　　将 View 的标识设置为 PFLAG_PRESSED，如果需要刷新，则刷新背景，如果按下没有抬起，则会发出一个检测长按的延时任务，父类有滑动的延时时间为 ViewConfiguration.getLongPressTimeout() - ViewConfiguration.getTapTimeout()（500-100）ms，ViewConfiguration.getTapTimeout() 就是父类有滑动，发送延时的时间 100，而父类没有滑动的延时时间为 ViewConfiguration.getLongPressTimeout()，也就是用户从 DOWN 触发开始算起，如果 500ms 内没有抬起则认为触发了长按事件。

### 3.4. MotionEvent.ACTION_MOVE

```java
                    if (clickable) {
                        drawableHotspotChanged(x, y);
                    }

                    // Be lenient about moving outside of buttons
                    // 判断触摸点是否还在当前 View 上
                    if (!pointInView(x, y, mTouchSlop)) {
                        // Outside button
                        // Remove any future long press/tap checks
                        // 不在当前 View 上了
                        // 移除滚动的回调
                        removeTapCallback();
                        // 移除长按的回调
                        removeLongPressCallback();
                        if ((mPrivateFlags & PFLAG_PRESSED) != 0) {
                            setPressed(false);
                        }
                        mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                    }
                    // 深压
                   	final boolean deepPress =
                            motionClassification == MotionEvent.CLASSIFICATION_DEEP_PRESS;
                    if (deepPress && hasPendingLongPressCallback()) {
                        // process the long click action immediately
                        removeLongPressCallback();
                        checkForLongClick(
                                0 /* send immediately */,
                                x,
                                y,
                                TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__DEEP_PRESS);
                    }
```

　　MotionEvent.ACTION_MOVE 主要是判断如果触摸的位置已经不在控件上了，则移除点击或者长按的回调。调用 paintInView() 来检查当前触摸点有没有移出 View。

#### 3.4.1. View#pointInView

```java
    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     *
     * @hide
     */
    @UnsupportedAppUsage
    public boolean pointInView(float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((mRight - mLeft) + slop) &&
                localY < ((mBottom - mTop) + slop);
    }
```

　　判断是长按，然后判断出触摸点不在 view 了，那么调用 removeLongPressCallback() 移除长按回调，并调用 checkForLongClick() 方法检查长按。

　　如果不是长按，并且触摸点不在 View 了，则调用 removeTapCallback() 方法移除点击回调，调用 removeLongPressCallback() 移除长按回调，如果 mRivateFlags 包含 PFLAG_PRESSED 标志，则调用 setPressed(false) 方法刷新，注意这里 setPressed(false)，传递的参数是 false，这样 mPrivateFlags 会清除 PFLAG_PRESSED 标志。

#### 3.4.2. View#removeTapCallback AND View#removeLongPressCallback

　　移除长按回调和点击回调是：

```java
    private CheckForLongPress mPendingCheckForLongPress; // 延时检查长按的事件处理
    @UnsupportedAppUsage
    private CheckForTap mPendingCheckForTap = null; // 延时检查点击的事件处理

	/**
     * Remove the tap detection timer.
     */
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }
    /**
     * Remove the longpress detection timer.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    public boolean removeCallbacks(Runnable action) {
        if (action != null) {
            final AttachInfo attachInfo = mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mHandler.removeCallbacks(action);
                attachInfo.mViewRootImpl.mChoreographer.removeCallbacks(
                        Choreographer.CALLBACK_ANIMATION, action, null);
            }
            getRunQueue().removeCallbacks(action);
        }
        return true;
    }
```

#### 3.4.3. View#AttachInfo

　　在 View 的布局绘制过程中，其中会调用到 ViewRootImpl 的 performTraversals() 方法，在 performTranversals() 方法中会调用：

```java
            mAttachInfo.mUse32BitDrawingCache = true;
            mAttachInfo.mWindowVisibility = viewVisibility;
            mAttachInfo.mRecomputeGlobalAttributes = false;
            mLastConfigurationFromResources.setTo(config);
            mLastSystemUiVisibility = mAttachInfo.mSystemUiVisibility;
            // Set the layout direction if it has not been set before (inherit is the default)
            if (mViewLayoutDirectionInitial == View.LAYOUT_DIRECTION_INHERIT) {
                host.setLayoutDirection(config.getLayoutDirection());
            }
// 调用了 view 的 dispatchAttachedToWindow() 方法，设置了 View 的 mAttachInfo 变量
            host.dispatchAttachedToWindow(mAttachInfo, 0);
            mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
            dispatchApplyInsets(host);
```

　　而 ViewRootImpl 的 mAttachInfo 变量是在 ViewRootImpl 的构造方法中初始化的：

```java
        mAttachInfo = new View.AttachInfo(mWindowSession, mWindow, display, this, mHandler, this,
                context);
```

　　设置的 AttachInfo 的 mHandler 是：

```java
final ViewRootHandler mHandler = new ViewRootHandler();
```

#### 3.4.4. 总结

　　只要用户触摸位置移出了控件，则将长按回调、点击回调移除，清除 mPrivateFlags 的 PFLAG_PRESSED 标识，刷新背景。

### 3.5. MotionEvent.ACTION_UP

```java
                    mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                    if ((viewFlags & TOOLTIP) == TOOLTIP) {
                        handleTooltipUp();
                    }
                    if (!clickable) {
                        removeTapCallback();
                        removeLongPressCallback();
                        mInContextButtonPress = false;
                        mHasPerformedLongPress = false;
                        mIgnoreNextUpEvent = false;
                        break;
                    }
                    // 判断 mPrivateFlags 是否包含 PFLAG_PREPRESSED 标识
                    boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                    // 如果 mPrivateFlags 包含 PFLAG_PRESSED 或者 PFLAG_PREPRESSED 标志
                    if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                        // take focus if we don't have it already and we should in
                        // touch mode.
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }
												// 如果还是 PFLAG_PREPRESSED 标志
                        if (prepressed) {
                            // The button is being released before we actually
                            // showed it as pressed.  Make it show the pressed
                            // state now (before scheduling the click) to ensure
                            // the user sees it.
                            // 给 mPrivateFlags 设置 PFLAG_PRESSED 标志，并刷新背景
                            setPressed(true, x, y);
                        }

                        // mHasPerformedLongPress 为 false
                        // 可以点击或者长按时，mHasPerformedLongPress 为 false
                        // 只有在 performLongPressCallback() 方法返回 true 的时候才会设置为 true，而只有调用 setOnLongClickListener() 设置了长按回调，并且在 onLongClick 方法中返回了 true，才会设置为 true。
                        if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();

                            // Only perform take click actions if we were in the pressed state
                            if (!focusTaken) {
                                // Use a Runnable and post this rather than calling
                                // performClick directly. This lets other visual state
                                // of the view update before click actions start.
                                // mPerformClick 为 null
                                if (mPerformClick == null) {
                                    // 初始化 mPerformClick 实例
                                    mPerformClick = new PerformClick();
                                }
                                // 将 mPerformClick 添加到消息队列尾部
                                // 如果失败，则运行 performClick() 方法
                                // 如果成功，则会调用 mPerformClick 的 run 
                                if (!post(mPerformClick)) {
                                    performClick();
                                }
                            }
                        }

                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }

                        // 如果 PFLAG_PREPRESSED 标志
                        if (prepressed) {
                            // 发送延时消息，延时时间为 64ms
                            // 时间到了后，会运行 mUnsetPressedState 的 run() 方法
                            postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } 
                        // 如果不是 PFLAG_PREPRESSED 标志
                        // 将 mUnsetPressedState 添加到消息队列尾部
                        // 添加成功，等待运行 mUnsetPressedState 的 run() 方法
                        else if (!post(mUnsetPressedState)) {
                            // If the post failed, unpress right now
                            // 添加失败直接运行 mUnsetPressedState 的 run() 方法
                            mUnsetPressedState.run();
                        }
						
                        // 移除单击回调
                        removeTapCallback();
                    }
                    mIgnoreNextUpEvent = false;
```

　　判断 mPrivateFlags 是否包含 PFLAG_PREPRESSED，如果包含，则进入执行体，也就是无论是 100 ms 内或者之后抬起都会进入执行体。

　　如果 prepressed 为 true，调用 setPressed() 方法，给 mPrivateFlags 设置 PFLAG_PRESSED 标志，并刷新背景。

　　如果 mHasPerformedLongPress 为 false，则调用 removeLongPressCallback() 方法移除长按的检测。mHasPerformedLongPress  这个变量只有在 performLongClick() 方法返回 true 的时候才会设置为 true，而只有调用 setOnLongClickListener() 设置了长按回调，并且在 onLongClick 方法中返回了 true，才会设置为 true，也就是说，如果 onLongClick 方法返回了 true，那么就不会进入判断体，也就不会执行 performClickInternal() 方法，那么调用 setOnClickListener() 设置的点击回调就不会执行。

　　如果 mPerformClick 为 null，则初始化 mPerformClick 实例，并通过 handler 将 mPerformClick 添加到消息队列尾部，如果添加失败则直接执行 performClickInternal()；添加成功，执行 mPerformClick 的 run() 方法。

　　最后如果 prepressed 为 true，调用 postDelayed() 方法发送延时消息，64 秒后会调用 mUnsetPressedState 的 run 方法；如果 prepressed 为 false，会直接调用 mUnsetPressedState 的 run 方法，所以最后都会调用 mUnsetPressedState 的 run 方法。

　　ACTION_UP 的最后是 removeTapCallback()，移除点击回调。

#### 3.5.1. View#UnsetPressedState

```java
    private final class UnsetPressedState implements Runnable {
        @Override
        public void run() {
            setPressed(false);
        }
    }
```

　　就是清除 mPrivateFlags 的 PFLAG_PRESSED 标志，刷新背景，把 setPressed 转发下去。

#### 3.5.2. View#PerformClick

```java
    private PerformClick mPerformClick;
	private final class PerformClick implements Runnable {
        @Override
        public void run() {
            recordGestureClassification(TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__SINGLE_TAP);
            performClickInternal();
        }
    }
```

　　mPerformClick 的 run 方法也是调用了 performClickInternal() 方法。

#### 3.5.3. View#performClickInternal

```java
    /**
     * Entry point for {@link #performClick()} - other methods on View should call it instead of
     * {@code performClick()} directly to make sure the autofill manager is notified when
     * necessary (as subclasses could extend {@code performClick()} without calling the parent's
     * method).
     */
    private boolean performClickInternal() {
        // Must notify autofill manager before performing the click actions to avoid scenarios where
        // the app has a click listener that changes the state of views the autofill service might
        // be interested on.
        notifyAutofillManagerOnClick();

        return performClick();
    }
```

　　performClickInternal() 方法调用了 performClick() 方法。

#### 3.5.4. View#performClick()

　　在 View 的 onTouchEvent() 方法中如果该控件是可以点击的就会进入到 switch 判断中去，而如果当前的事件是抬起手指，则会进入到 MotionEvent.ACTION_UP 这个 case 当中。在经过种种判断之后，会执行到 performClick() 方法。

```java
    public boolean performClick() {
        final boolean result;
        final ListenerInfo li = mListenerInfo;
        if (li != null && li.mOnClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            li.mOnClickListener.onClick(this);
            result = true;
        } else {
            result = false;
        }

        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        notifyEnterOrExitForAutoFillIfNeeded(true);

        return result;
    }
```

　　可以看到，只要 mOnClickListener 不是 null，就会去调用它的 onClick 方法，而 mOnClickListener 就是调用 setOnClickListener() 方法设置的点击回调。

#### 3.5.5. View#setOnClickListener

```java
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        getListenerInfo().mOnClickListener = l;
    }
```

　　当通过调用 setOnClickListener 方法来给控件注册一个点击事件时，就会给 mOnClickListener 赋值。然后每当控件被点击时，就会在 performClick() 方法里回调被点击控件的 onClick 方法。

### 3.6. touch 事件的层级传递

　　如果给一个控件注册了 touch 事件，每次点击它的时候都会触发一系列的 ACTION_DOWN、ACTION_MOVE、ACTION_UP 等事件。这里需要注意，如果在执行 ACTION_DOWN 的时候返回了 false，后面一系列其他的 action 就不会再得到执行了。简单地说，就是当 dispatchTouchEvent 在进行事件分发的时候，只有前一个 action 返回 true，才会触发后一个 action。

　　前面的例子中，明明在 onTouch 事件里面返回了 false，ACTION_DOWN 和 ACTION_UP 也得到执行了？参考前面分析的源码，首先在 onTouchEvent 方法的细节。由于点击了按钮，就会进入到 `if (clickable || (viewFlags & TOOLTIP) == TOOLTIP)` 这个 if 判断中，然后不管当前的 action 是什么，最后都一定会走到最后返回一个 true。

　　明明在 onTouch 事件里返回了 false，系统还是在 onTouchEvent 方法中返回了 true。就因为这个原因，才使得前面的例子中 ACTION_UP 可以得到执行。

　　那可以换一个控件，将按钮替换成 ImageView，然后给它也注册一个 touch 事件，并返回 false。如下所示：

```java
imageView.setOnTouchListener(new OnTouchListener() {
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("TAG", "onTouch execute, action " + event.getAction());
		return false;
	}
});
```

　　运行一下程序，点击 ImageView，会发现结果如下：

````
onTouch execute, action 0
````

　　在 ACTION_DOWN 执行完后，后面的一系列 action 都不会得到执行了。这又是为什么呢？因为 ImageView 和按钮不同，它是默认不可点击的，因此在 onTouchEvent 的  `if (clickable || (viewFlags & TOOLTIP) == TOOLTIP)` 这个 if 判断无法进入 if 的内部，直接跳过返回了 false，也就导致后面其他的 action 都无法执行了。

## 4. 常见问题

### 4.1. onTouch 和 onTouchEvent 有什么区别，又该如何使用？

　　从源码中可以看出，这两个方法都是在 View 的 dispatchTouchEvent 中调用的，onTouch 优先于 onTouchEvent 执行。如果在 onTouch 方法中通过返回 true 将事件消费掉，onTouchEvent 将不会再执行。

　　另外需要注意的是，onTouch 能够得到执行需要两个前提条件，第一 mOnTouchListener 的值不能为空，第二当前点击的控件必须是 enable 的。因此如果有一个控件是非 enable 的，那么给它注册 onTouch 事件将永远得不到执行。对于这一类控件，如果想要监听它的 touch 事件，就必须通过在该控件中重写 onTouchEvent 方法来实现。

### 4.2. 为什么给 ListView 引入了一个滑动菜单的功能，ListView 就不能滚动了？

　　滚动菜单的功能是通过给 ListView 注册了一个 touch 事件来实现的。如果在 onTouch 方法里处理完了滑动逻辑后返回 true，那么 ListView 本身的滚动事件就被屏蔽了，自然也就无法滑动（原理同前面例子中按钮不能点击），因此解决方法就是在 onTouch 方法里返回 false。

## 5. 总结

　　View的事件分发示意图：

![](image/View的事件分发示意图.png)

### 5.1. 整个 View 的事件转发流程是

　　View.dispatchTouchEvent -> View.setOnTouchListener -> View.onTouchEvent

　　在 dispatchTouchEvent 中会进行 OnTouchListener 的判断，如果 onTouchEvent 不为 null 且返回 true，则表示事件被消费，onTouchEvent 不会被执行，否则执行 onTouchEvent。

### 5.2. onTouchEvent 中的 DOWN、MOVE、UP

#### 5.2.1. DOWN

　　如果父控件支持滑动，首先设置标志为 PFLAG_PREPRESSED，设置 mHasPerformedLongPress = false，然后发出了一个 100ms 后的 mPendingCheckForTag。

　　如果 100ms 内没有触发 UP，则将标志置为 PFLAG_PRESSED，清除 PREPRESSED 标志，同时发出一个延时为 500-100 ms 的检查长按任务的消息。

　　如果父控件不支持滑动，则是将标记置为 PFLAG_PRESSED，同时发出一个延时为 500ms 的检查长按任务的消息。

　　检查长按任务的消息时间到了后，则会触发 LongClickListener。

　　此时如果 LongClickListener 不为 null，则会执行回调，但是如果 LongClickListener.onClick 返回 true，才把 mHasPerformedLongPress 设置为 true，否则 mHasPerformedLongPress 依然为 false。

#### 5.2.2. MOVE

　　主要就是检查用户是否滑出了控件，如果触摸的位置已经不在当前 view 上了，则移除点击和长按的回调。

#### 5.2.3. UP

　　如果 100ms 内，触发 UP，此时标志为 PFLAG_PREPRESSED ，则执行 UnSetPressedState，setPressed(false)，会把 setPress 转发下去，可以在 View 中复写 dispatchSetPressed 方法接收。

　　如果是 100ms - 500ms 之间，即长按还未发生，则首先移除长按检测，执行 onClick 回调；

　　如果是 500ms 以后，那么有两种情况：

* 设置了 onLongClickListener，且 onLongClickListener.onClick 返回 true，则点击事件 onClick 无法触发。
* 没有设置 onLongClickListener 或者 onLongClickListener.onClick 返回 false，则点击事件 onClick 事件触发。
* 最后执行 mUnSetPressedState.run()，将 setPressed 传递下去，然后将 PFLAG_PRESSED 标识清除。


## 6. 参考文章
1. [Android事件分发机制完全解析，带你从源码的角度彻底理解(上)](https://blog.csdn.net/guolin_blog/article/details/9097463)
3. [Android View 事件分发机制 源码解析 （上）](https://blog.csdn.net/lmj623565791/article/details/38960443)
5. [面试：讲讲 Android 的事件分发机制](https://www.jianshu.com/p/d3758eef1f72)