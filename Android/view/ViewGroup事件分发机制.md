# ViewGroup 事件分发机制

## 1. ViewGroup

　　ViewGroup 就是一组 View 的集合，它包含很多的子 View 和子 ViewGroup，是 Android 中所有布局的父类或间接父类，像 LinearLayout、RelativeLayout 等都是继承自 ViewGroup 的。但 ViewGroup 实际上也是一个 View，只不过比起 View，它多了可以包含子 View 和定义布局参数的功能。

　　ViewGroup 继承结构示意图如下所示：

![](image/ViewGroup继承结构示意图.png)

　　可以看到，经常用到的各种布局，全部属于 ViewGroup 的子类。

## 2. 案例探究

　　通过一个 Demo 来演示一下 Android 中 ViewGroup 的事件分发流程。

　　首先自定义一个布局，命名为 MyLayout，继承自 LinearLayout，然后复写了与事件分发机制有关的代码，添加上了日志的打印，如下所示：

```java
public class MyLayout extends LinearLayout {

    public static final String TAG = MyLayout.class.getSimpleName();

    public MyLayout(Context context) {
        super(context);
    }

    public MyLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "dispatchTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

}
```

　　然后，打开主布局文件 activity_main.xml，在其中加入自定义的布局：

```java
<?xml version="1.0" encoding="utf-8"?>
<com.example.viewgroupdistribute.MyLayout xmlns:android="http://schemas.android.com/apk/res/android"
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

</com.example.viewgroupdistribute.MyLayout>
```

　　MyButton 就是 [View 事件分发机制](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/view/View事件分发机制.md) 里面的 MyButton。

　　给 MyLayout 设置触摸监听：

```java
        myLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "myLayout on touch");
                return false;
            }
        });
```

　　运行程序后，点击 Button，打印结果如下：

```java
07-03 16:22:55.220 17839-17839/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_DOWN
07-03 16:22:55.220 17839-17839/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_DOWN
07-03 16:22:55.220 17839-17839/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_DOWN
07-03 16:22:55.220 17839-17839/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_DOWN
07-03 16:22:55.220 17839-17839/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_DOWN
    
07-03 16:22:55.342 17839-17839/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_MOVE
07-03 16:22:55.342 17839-17839/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_MOVE
07-03 16:22:55.342 17839-17839/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_MOVE
07-03 16:22:55.342 17839-17839/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_MOVE
07-03 16:22:55.342 17839-17839/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_MOVE
    
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_UP
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_UP
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_UP
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_UP
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_UP
07-03 16:22:55.424 17839-17839/com.example.viewgroupdistribute D/MainActivity: you clicked button
```

　　点击空白区域输出的日志：

```java
07-03 16:23:53.450 17839-17839/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_DOWN
07-03 16:23:53.450 17839-17839/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_DOWN
07-03 16:23:53.450 17839-17839/com.example.viewgroupdistribute D/MainActivity: you clicked myLayout
07-03 16:23:53.450 17839-17839/com.example.viewgroupdistribute D/MyLayout: onTouchEvent ACTION_DOWN
```

　　会发现，当点击按钮的时候，MyLayout 注册的 onTouch() 方法并不会执行。只有点击空白区域的时候才会执行该方法。

　　可以看到大体的事件流程为：MyLayout.dispatchTouchEvent -> MyLayout.onInterceptTouchEvent -> MyButton.dispatchTouchEvent -> MyButton.onTouchEvent。

　　可以看出，在 View 上触发事件，最先捕获到事件的是 View 所在的 ViewGroup，然后才会到 View 自身。

## 3. ViewGroup 的事件分发源码分析

### 3.1. ViewGroup#dispatchTouchEvent

```java
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(ev, 1);
        }

        // If the event targets the accessibility focused view and this is it, start
        // normal event dispatch. Maybe a descendant is what will handle the click.
        if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
            ev.setTargetAccessibilityFocus(false);
        }

        boolean handled = false;
        if (onFilterTouchEventForSecurity(ev)) {
            final int action = ev.getAction();
            final int actionMasked = action & MotionEvent.ACTION_MASK;

            // Handle an initial down.
            // 事件列一般都是从 ACTION_DOWN 开始的
            // ACTION_DOWN
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                // Throw away all previous state when starting a new touch gesture.
                // The framework may have dropped the up or cancel event for the previous gesture
                // due to an app switch, ANR, or some other state change.
                // 
                // 取消并清除所有触摸目标
                cancelAndClearTouchTargets(ev);
                // 重置所有触摸状态以准备新的循环
                resetTouchState();
            }

            // Check for interception.
            // ACTION_DOWN
            final boolean intercepted;
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {
                // 是否允许拦截
                // disallowIntercept 一般都会 false，只有在调用 requestDisallowInterceptTouchEvent(true) 方法才会返回 true
                final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                // 不允许拦截
                if (!disallowIntercept) {
                  // 调用 onInterceptTouchEvent() 方法，并将返回值设置给 intercepted
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
                    // 允许拦截
                    intercepted = false;
                }
            } else {
                // There are no touch targets and this action is not an initial down
                // so this view group continues to intercept touches.
                intercepted = true;
            }

            // If intercepted, start normal event dispatch. Also if there is already
            // a view that is handling the gesture, do normal event dispatch.
            if (intercepted || mFirstTouchTarget != null) {
                ev.setTargetAccessibilityFocus(false);
            }

            // Check for cancelation.
            final boolean canceled = resetCancelNextUpFlag(this)
                    || actionMasked == MotionEvent.ACTION_CANCEL;

            // Update list of touch targets for pointer down, if needed.
            final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0;
            TouchTarget newTouchTarget = null;
            boolean alreadyDispatchedToNewTouchTarget = false;
            // 没有拦截也没有停止
            // ACTION_DOWN
            // 虽然说 ACTION_DOWN、ACTION_MOVE、ACTION_UP 三个事件都可以进入这个判断中,但是里面有个判断，大部分的代码只有 ACTION_DOWN 可以进入
            // canceled ：是否取消
            if (!canceled && !intercepted) {

                // If the event is targeting accessibility focus we give it to the
                // view that has accessibility focus and if it does not handle it
                // we clear the flag and dispatch the event to all children as usual.
                // We are looking up the accessibility focused host to avoid keeping
                // state since these events are very rare.
                View childWithAccessibilityFocus = ev.isTargetAccessibilityFocus()
                        ? findChildWithAccessibilityFocus() : null;

                // ACTION_DOWN
                if (actionMasked == MotionEvent.ACTION_DOWN
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                    final int actionIndex = ev.getActionIndex(); // always 0 for down
                    final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex)
                            : TouchTarget.ALL_POINTER_IDS;

                    // Clean up earlier touch targets for this pointer id in case they
                    // have become out of sync.
                    removePointersFromTouchTargets(idBitsToAssign);

                    final int childrenCount = mChildrenCount;
                    if (newTouchTarget == null && childrenCount != 0) {
                        final float x = ev.getX(actionIndex);
                        final float y = ev.getY(actionIndex);
                        // Find a child that can receive the event.
                        // Scan children from front to back.
                        final ArrayList<View> preorderedList = buildTouchDispatchChildList();
                        final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                        final View[] children = mChildren;
                        // 遍历所有的子 View
                        for (int i = childrenCount - 1; i >= 0; i--) {
                            final int childIndex = getAndVerifyPreorderedIndex(
                                    childrenCount, i, customOrder);
                            final View child = getAndVerifyPreorderedView(
                                    preorderedList, children, childIndex);

                            // If there is a view that has accessibility focus we want it
                            // to get the event first and if not handled we will perform a
                            // normal dispatch. We may do a double iteration but this is
                            // safer given the timeframe.
                            if (childWithAccessibilityFocus != null) {
                                if (childWithAccessibilityFocus != child) {
                                    continue;
                                }
                                childWithAccessibilityFocus = null;
                                i = childrenCount - 1;
                            }

                            if (!child.canReceivePointerEvents()
                                    || 
 // 判断当前的 x,y 坐标是否落在子 View 身上                               
                                !isTransformedTouchPointInView(x, y, child, null)) {
                                ev.setTargetAccessibilityFocus(false);
                                // 如果触摸的不是当前子 View，则进行下一个子 view 处理
                                continue;
                            }

                            newTouchTarget = getTouchTarget(child);
                            if (newTouchTarget != null) {
                                // Child is already receiving touch within its bounds.
                                // Give it the new pointer in addition to the ones it is handling.
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }

                            resetCancelNextUpFlag(child);
                            // 调用 child 的 dispatchTouchEvent() 方法，如果返回的是 true，那么当前的子 view 会被插入到 mFirstTouchTarget 链表的头部
                            if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                // Child wants to receive touch within its bounds.
                                mLastTouchDownTime = ev.getDownTime();
                                if (preorderedList != null) {
                                    // childIndex points into presorted list, find original index
                                    for (int j = 0; j < childrenCount; j++) {
                                        if (children[childIndex] == mChildren[j]) {
                                            mLastTouchDownIndex = j;
                                            break;
                                        }
                                    }
                                } else {
                                    mLastTouchDownIndex = childIndex;
                                }
                                mLastTouchDownX = ev.getX();
                                mLastTouchDownY = ev.getY();
                                // 将指定子对象的触摸目标添加到列表的开头
                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                // 标记已经分发了事件
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }

                            // The accessibility focus didn't handle the event, so clear
                            // the flag and do a normal dispatch to all children.
                            ev.setTargetAccessibilityFocus(false);
                        }
                        if (preorderedList != null) preorderedList.clear();
                    }

                    if (newTouchTarget == null && mFirstTouchTarget != null) {
                        // Did not find a child to receive the event.
                        // Assign the pointer to the least recently added target.
                        newTouchTarget = mFirstTouchTarget;
                        while (newTouchTarget.next != null) {
                            newTouchTarget = newTouchTarget.next;
                        }
                        newTouchTarget.pointerIdBits |= idBitsToAssign;
                    }
                }
            }

            // Dispatch to touch targets.
            // ACTION_DOWN、ACTION_MOVE、ACTION_UP
            if (mFirstTouchTarget == null) {
                // No touch targets so treat this as an ordinary view.
                // 没有触摸目标，将其视为普通视图
                // 调用了 dispatchTransformedTouchEvent() 方法，第三个参数为 null
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            } else {
                // Dispatch to touch targets, excluding the new touch target if we already
                // dispatched to it.  Cancel touch targets if necessary.
                TouchTarget predecessor = null;
                TouchTarget target = mFirstTouchTarget;
                // 对所有触摸的控件进行循环处理
                while (target != null) {
                    final TouchTarget next = target.next;
                    // 是否重复调用
                    // alreadyDispatchedToNewTouchTarget 在 ACTION_DOWN 调用了 dispatchTransformedTouchEvent() 方法后返回 true 时，设置成了 true，防止重复调用 dispatchTransformedTouchEvent() 方法
                    if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                        handled = true;
                    } else {
                        final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                || intercepted;
                        // 执行了子 View 的 dispatchTouchEvent() 方法
                        if (dispatchTransformedTouchEvent(ev, cancelChild,
                                target.child, target.pointerIdBits)) {
                            handled = true;
                        }
                        // 设置下一个空控件
                        if (cancelChild) {
                            if (predecessor == null) {
                                mFirstTouchTarget = next;
                            } else {
                                predecessor.next = next;
                            }
                            target.recycle();
                            target = next;
                            continue;
                        }
                    }
                    predecessor = target;
                    target = next;
                }
            }

            // Update list of touch targets for pointer up or cancel, if needed.
            // ACTION_UP
            if (canceled
                    || actionMasked == MotionEvent.ACTION_UP
                    || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                // 将 mFirstTouchTarget 置为 null
                resetTouchState();
            } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
                final int actionIndex = ev.getActionIndex();
                final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
                removePointersFromTouchTargets(idBitsToRemove);
            }
        }

        if (!handled && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(ev, 1);
        }
        return handled;
    }
```

　　将 dispatchTouchEvent() 方法分为 ACTION_DOWN、ACTION_MOVE、ACTION_UP 三个事件来分析。

#### 3.1.1. ACTION_DOWN

　　在 ACTION_DOWN 事件开始的时候，先是调用了 cancelAndClearTouchTargets() 和 resetTouchState() 方法，取消并清除所有触摸目标和重置所有触摸状态以准备新的循环，这个操作会将 mFirstTouchTarget 链表清空。

　　接下来会判断是否允许拦截，是否允许拦截的判断是检测 mGroupFlag 字段是否包含 FLAG_DISALLOW_INTERCEPT 标志。mGroupFlag 一般是不包含 FLAG_DISALLOW_INTERCEPT 标志的，所以一般是不拦截。只有在调用 requestDisallowInterceptTouchEvent(true) 方法 mGroupFlag 才会包含 FLAG_DISALLOW_INTERCEPT 标志，才会拦截。

　　如果拦截，变量 intercepted 置为 true。

　　如果不拦截，则调用 onInterceptTouchEvent() 方法。会将 onInterceptEvent() 方法的返回值赋给 intercepted 变量。

　　如果 intercepted 为 true，那么后面的操作就不会执行了。

　　如果 intercepted 为 false，那么接下来就是遍历所有的子 View，会判断当前点击的 x,y 坐标是否在子 View 上，如果是，那么就会调用子 view 的 dispatchTransformedTouchEvent() 方法，如果 dispatchTransformedTouchEvent() 返回 true，则将子 view 加入到 mFirstTouchTarget 链表的头部。而 dispatchTransformedTouchEvent() 就是调用子 view 的 dispatchTouchEvent() 方法。

　　**ACTION_DOWN 总结**：ViewGroup 实现捕获 DOWN 事件，如果代码中不做 TOUCH 事件拦截，则判断当前子 View 是否在当前 x,y 的区域内，如果在，将其添加到 mFirstTouchTarget 链表的头部，并且调用子 View 的 dispatchTouchEvent() 方法把事件分发下去。

#### 3.1.2. ACTION_MOVE

　　ACTION_MOVE 的时候如果有子 View，则 mFirstTouchTarget 不会为 null，所以也会 `if (actionMasked == MotionEvent.ACTION_DOWN
        || mFirstTouchTarget != null)` 进入这个判断，检测 disallowIntercept 是否允许打断。

　　接着会遍历 mFirstTouchTarget 链表，调用 dispatchTransformedTouchEvent() 方法，调用链表中的子 View 的 dispatchTouchEvent() 方法。

　　**ACTION_MOVE 总结**：ACTION_MOVE 在检测完是否拦截以后，直接调用了子 View 的 dispatchTouchEvent，事件分发下去。

#### 3.1.3. ACTION_UP

　　ACTION_UP 也会运行 ACTION_MOVE 部分的代码，除此之外，还会调用 resetTouchState() 方法重置所有触摸状态以准备新的循环，包括将 mFirstTouchTarget 清空。

　　**ACTION_UP 总结**：ACTION_UP 在检测完是否拦截以后，直接调用了子 View 的 dispatchTouchEvent，事件分发下去，最后重置触摸状态，将 mFirstTouchTarget 清空。

#### 3.1.4. ViewGroup#cancelAndClearTouchTargets

```java
    /**
     * Cancels and clears all touch targets.
     * 取消并清除所有触摸目标
     */
    private void cancelAndClearTouchTargets(MotionEvent event) {
        if (mFirstTouchTarget != null) {
            boolean syntheticEvent = false;
            if (event == null) {
                final long now = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(now, now,
                        MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                syntheticEvent = true;
            }

            for (TouchTarget target = mFirstTouchTarget; target != null; target = target.next) {
                resetCancelNextUpFlag(target.child);
                // 分发 MotionEvent.ACTION_CANCEL 事件下去
                dispatchTransformedTouchEvent(event, true, target.child, target.pointerIdBits);
            }
            // 清除所有触摸目标
            clearTouchTargets();

            if (syntheticEvent) {
                event.recycle();
            }
        }
    }
```

#### 3.1.5. ViewGroup#clearTouchTargets

```java
    /**
     * Clears all touch targets.
     * 清除所有触摸目标
     */
    private void clearTouchTargets() {
        TouchTarget target = mFirstTouchTarget;
        if (target != null) {
            do {
                TouchTarget next = target.next;
                target.recycle();
                target = next;
            } while (target != null);
            mFirstTouchTarget = null;
        }
    }
```

#### 3.1.6. ViewGroup#resetTouchState

```java
    /**
     * Resets all touch state in preparation for a new cycle.
     * 重置所有触摸状态以准备新的循环
     */
    private void resetTouchState() {
        // 清除所有触摸目标
        clearTouchTargets();
        // 重置取消上一个标志。
        resetCancelNextUpFlag(this);
        mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
        mNestedScrollAxes = SCROLL_AXIS_NONE;
    }
```

#### 3.1.7. ViewGroup#dispatchTransformedTouchEvent

```java
    /**
     * Transforms a motion event into the coordinate space of a particular child view,
     * filters out irrelevant pointer ids, and overrides its action if necessary.
     * If child is null, assumes the MotionEvent will be sent to this ViewGroup instead.
     */
    private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
            View child, int desiredPointerIdBits) {
        final boolean handled;

        // Canceling motions is a special case.  We don't need to perform any transformations
        // or filtering.  The important part is the action, not the contents.
        final int oldAction = event.getAction();
       
        if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            // 如果子 view 为 null，则调用 View 的 dispatchTouchEvent() 方法分发事件
            if (child == null) {
                handled = super.dispatchTouchEvent(event);
            } else {
                // 如果子 view 不为null，则调用子 view 的 dispatchTouchEvent 事件
                // 而 View 只要支持点击或者长按事件一定会返回 true
                handled = child.dispatchTouchEvent(event);
            }
            event.setAction(oldAction);
            return handled;
        }

        // Calculate the number of pointers to deliver.
        final int oldPointerIdBits = event.getPointerIdBits();
        final int newPointerIdBits = oldPointerIdBits & desiredPointerIdBits;

        // If for some reason we ended up in an inconsistent state where it looks like we
        // might produce a motion event with no pointers in it, then drop the event.
        if (newPointerIdBits == 0) {
            return false;
        }

        // If the number of pointers is the same and we don't need to perform any fancy
        // irreversible transformations, then we can reuse the motion event for this
        // dispatch as long as we are careful to revert any changes we make.
        // Otherwise we need to make a copy.
        final MotionEvent transformedEvent;
        if (newPointerIdBits == oldPointerIdBits) {
            if (child == null || child.hasIdentityMatrix()) {
                if (child == null) {
                    handled = super.dispatchTouchEvent(event);
                } else {
                    final float offsetX = mScrollX - child.mLeft;
                    final float offsetY = mScrollY - child.mTop;
                    // 修改坐标系统，把当前的 x,y 分别减去 child.left 和 child.top
                    event.offsetLocation(offsetX, offsetY);

                    handled = child.dispatchTouchEvent(event);

                    event.offsetLocation(-offsetX, -offsetY);
                }
                return handled;
            }
            transformedEvent = MotionEvent.obtain(event);
        } else {
            transformedEvent = event.split(newPointerIdBits);
        }

        // Perform any necessary transformations and dispatch.
        if (child == null) {
            // 如果子 view 为 null，则调用 View 的 dispatchTouchEvent() 方法分发事件
            handled = super.dispatchTouchEvent(transformedEvent);
        } else {
            final float offsetX = mScrollX - child.mLeft;
            final float offsetY = mScrollY - child.mTop;
            transformedEvent.offsetLocation(offsetX, offsetY);
            if (! child.hasIdentityMatrix()) {
                transformedEvent.transform(child.getInverseMatrix());
            }
			// 如果子 view 不为 null，则调用子 view 的 dispatchTouchEvent 事件
            handled = child.dispatchTouchEvent(transformedEvent);
        }

        // Done.
        transformedEvent.recycle();
        return handled;
    }
```

　　需要注意一下，调用子 View 的 dispatchTouchEvent 后是有返回值的。如果一个控件是可点击的，那么点击该控件时，dispatchTouchEvent 的返回值必定是 true，这样 handled 就是 true。

　　如果点击的不是按钮，而是空白区域，那么 child 就是 null，就会调用 `super.dispatchTouchEvent(transformedEvent)` 方法，而 super 就是 View，然后处理逻辑又和上面的相同了。

#### 3.1.8. ViewGroup#addTouchTarget

```java
    /**
     * Adds a touch target for specified child to the beginning of the list.
     * Assumes the target child is not already present.
     * 将指定子 view 的触摸目标添加到列表的开头
     */
    private TouchTarget addTouchTarget(@NonNull View child, int pointerIdBits) {
        final TouchTarget target = TouchTarget.obtain(child, pointerIdBits);
        // 将 child 添加到 mFirstTouchTarget 链表头部
        target.next = mFirstTouchTarget;
        mFirstTouchTarget = target;
        return target;
    }
```

#### 3.1.9. TouchTarget#obtain

```java
        public static TouchTarget obtain(@NonNull View child, int pointerIdBits) {
            if (child == null) {
                throw new IllegalArgumentException("child must be non-null");
            }

            final TouchTarget target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new TouchTarget();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                     sRecycledCount--;
                    target.next = null;
                }
            }
            target.child = child; // 存储子 view
            target.pointerIdBits = pointerIdBits;
            return target;
        }
```

#### 3.1.10. 总结

1. ViewGroup 实现捕获 DOWN 事件，如果代码中不做 TOUCH 事件拦截，则开始查找当前 x,y 是否在某个子 View 的区域内，如果在，将其添加到 mFirstTouchTarget 链表的头部，并且调用子 View 的 dispatchTouchEvent() 方法把事件分发下去。
2. ACTION_MOVE 中，ViewGroup 捕获到事件，然后判断是否拦截，如果没有拦截，则直接调用子 View 的 dispatchTouchEvent(ev) 将事件分发下去。
3. ACTION_UP 中，ViewGroup 捕获到事件，然后判断是否拦截，如果没有拦截，则直接调用子 View 的 dispatchTouchEvent(ev) 将事件分发小区，最后重置触摸状态，将 mFirstToychTarget 清空。

　　在分发之前都会修改一下坐标系统，把当前的 x,y 分别减去 child.left 和 child.top，然后传给 child。

### 3.2. ViewGroup#onInterceptTouchEvent

　　ViewGroup 中有一个 onInterceptTouchEvent 方法，看一下这个方法源码：

```java
    /**
     * Implement this method to intercept all touch screen motion events.  This
     * allows you to watch events as they are dispatched to your children, and
     * take ownership of the current gesture at any point.
     *
     * <p>Using this function takes some care, as it has a fairly complicated
     * interaction with {@link View#onTouchEvent(MotionEvent)
     * View.onTouchEvent(MotionEvent)}, and using it requires implementing
     * that method as well as this one in the correct way.  Events will be
     * received in the following order:
     *
     * <ol>
     * <li> You will receive the down event here.
     * <li> The down event will be handled either by a child of this view
     * group, or given to your own onTouchEvent() method to handle; this means
     * you should implement onTouchEvent() to return true, so you will
     * continue to see the rest of the gesture (instead of looking for
     * a parent view to handle it).  Also, by returning true from
     * onTouchEvent(), you will not receive any following
     * events in onInterceptTouchEvent() and all touch processing must
     * happen in onTouchEvent() like normal.
     * <li> For as long as you return false from this function, each following
     * event (up to and including the final up) will be delivered first here
     * and then to the target's onTouchEvent().
     * <li> If you return true from here, you will not receive any
     * following events: the target view will receive the same event but
     * with the action {@link MotionEvent#ACTION_CANCEL}, and all further
     * events will be delivered to your onTouchEvent() method and no longer
     * appear here.
     * </ol>
     *
     * @param ev The motion event being dispatched down the hierarchy.
     * @return Return true to steal motion events from the children and have
     * them dispatched to this ViewGroup through onTouchEvent().
     * The current target will receive an ACTION_CANCEL event, and no further
     * messages will be delivered here.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.isFromSource(InputDevice.SOURCE_MOUSE)
                && ev.getAction() == MotionEvent.ACTION_DOWN
                && ev.isButtonPressed(MotionEvent.BUTTON_PRIMARY)
                && isOnScrollbarThumb(ev.getX(), ev.getY())) {
            return true;
        }
        return false;
    }
```

　　如果上面的判断为 false，那么 onInterceptTouchEvent() 方法返回的就是 false。

　　既然是布尔型的返回，那么只有两种可能，在 MyLayout 中重写这个方法，然后返回一个 true 试试，代码如下所示：

```java
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return true;
    }
```

　　再次运行然后分别点击 MyButton，打印结果如下所示：

```java
07-07 19:06:46.801 13732-13732/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_DOWN
07-07 19:06:46.801 13732-13732/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_DOWN
07-07 19:06:46.801 13732-13732/com.example.viewgroupdistribute D/MainActivity: you clicked myLayout
07-07 19:06:46.801 13732-13732/com.example.viewgroupdistribute D/MyLayout: onTouchEvent ACTION_DOWN
```

　　会发现，只会触发 MyLayout 的 touch 事件了，按钮的点击事件完全被屏蔽掉了！

　　只要触摸了任何控件，就一定会调用该控件的 dispatchTouchEvent() 方法。这个说法没错，只不过还不完整而已。实际情况是，当点击了某个控件，首先会去调用该控件所在布局的 dispatchTouchEvent 方法，然后在布局的 dispatchTouchEvent 方法中找到被点击的相应控件，再去调用该控件的 dispatchTouchEvent 方法。

　　如果点击了 MyLayout 中的按钮，会先去调用 MyLayout 的 dispatchTouchEvent 方法，而这个方法是在 MyLayout 的祖类 ViewGroup 中的，按钮的 dispatchTouchEvent 方法就是在这里调用的。

　　点击的事件传递图：

![](image/点击的事件传递.png)



## 4. 关于拦截

### 4.1. 如何拦截

　　在 ViewGroup 的 onInterceptTouchEvent() 方法中，如果判断是允许拦截的，然后就会调用 onInterceptTouchEvent() 方法来设置 intercepted：

```java
     			final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                if (!disallowIntercept) {
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
                    intercepted = false;
                }
```

　　而 intercepted 为 true 的话，那么后续的代码都不会被执行，事件也不会分发下去。所以拦截代码就覆写 onInterceptTouchEvent() 方法，并返回 true。

```java
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                // 如果觉得需要拦截
                return true;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                // 如果觉得需要拦截
                return true;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                // 如果觉得需要拦截
                return true;
            default:
                break;
        }
        return false;
    }
```

　　默认是不拦截的，即返回 false，如果需要拦截，只要 return true 就行了，这样该事件就不会往子 View 传递了，并且如果在 ACTION_DOWN 就 return true，则 ACTION_DOWN、ACTION_MOVE、ACTION_UP 子 View 都不会捕获事件，如果在 ACTION_MOVE return true，则子 View 在 ACTION_MOVE 和 ACTION_UP 都不会捕获事件。

　　原因很简单，当 onInterceptTouchEvent(ev) return true 的时候，会把 mFirstTouchTarget 清空。

### 4.2. 如何不被拦截

　　如果 ViewGroup 的 onInterceptTouchEvent(ev) 当 ACTION_MOVE 时 return true，即拦截了子 View 的 ACTION_MOVE 以及 ACTION_UP 事件。

　　修改 MyLayout 的 onInterceptTouchEvent() 方法：

```java
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                return true;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return false;
    }
```

　　输出为：

```
07-04 14:49:30.017 14790-14790/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_DOWN
07-04 14:49:30.017 14790-14790/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_DOWN
07-04 14:49:30.017 14790-14790/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_DOWN
07-04 14:49:30.017 14790-14790/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_DOWN
07-04 14:49:30.017 14790-14790/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_DOWN
07-04 14:49:30.070 14790-14790/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_MOVE
07-04 14:49:30.070 14790-14790/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_MOVE
07-04 14:49:30.114 14790-14790/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_UP
07-04 14:49:30.114 14790-14790/com.example.viewgroupdistribute D/MainActivity: you clicked myLayout
07-04 14:49:30.114 14790-14790/com.example.viewgroupdistribute D/MyLayout: onTouchEvent ACTION_UP
```

　　MyLayout 的 ACTION_MOVE 拦截后，MyButton 就没有收到 ACTION_MOVE 和 ACTION_UP 了。此时子 View 希望依然能够响应 ACTION_MOVE 和 ACTION_UP 事件该如何做。

　　Android 提供了一个 requestDisallowInterceptTouchEvent(boolean) 用于设置是否允许拦截，在子 View （MyButton）的 dispatchTouchEvent 中直接这么写：

```java
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 调用 requestDisallowInterceptTouchEvent(true) 方法
        getParent().requestDisallowInterceptTouchEvent(true);
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
```

　　输出为：

```java
07-04 14:54:13.517 15480-15480/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_DOWN
07-04 14:54:13.517 15480-15480/com.example.viewgroupdistribute D/MyLayout: onInterceptTouchEvent ACTION_DOWN
07-04 14:54:13.517 15480-15480/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_DOWN
07-04 14:54:13.517 15480-15480/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_DOWN
07-04 14:54:13.517 15480-15480/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_DOWN
07-04 14:54:13.583 15480-15480/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_MOVE
07-04 14:54:13.583 15480-15480/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_MOVE
07-04 14:54:13.583 15480-15480/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_MOVE
07-04 14:54:13.583 15480-15480/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_MOVE
07-04 14:54:13.591 15480-15480/com.example.viewgroupdistribute D/MyLayout: dispatchTouchEvent ACTION_UP
07-04 14:54:13.591 15480-15480/com.example.viewgroupdistribute D/MyButton: dispatchTouchEvent ACTION_UP
07-04 14:54:13.591 15480-15480/com.example.viewgroupdistribute D/MainActivity: onTouch ACTION_UP
07-04 14:54:13.592 15480-15480/com.example.viewgroupdistribute D/MyButton: onTouchEvent ACTION_UP
07-04 14:54:13.638 15480-15480/com.example.viewgroupdistribute D/MainActivity: you clicked button
```

　　可以看到 MyButton 子 View 在父 View 拦截 ACTION_MOVE 的情况下（ACTION_MOVE return true）依然可以捕获到 ACTION_MOVE 和 ACTION_UP 事件。

　　子控件依次执行了 dispatchEvent -> onTouch -> OnTouchListener#onTouch。而父控件只执行了 dispatchTouchEvent() 方法。

#### 4.2.1. ViewGroup#requestDisallowInterceptTouchEvent

```java
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        if (disallowIntercept == ((mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0)) {
            // We're already in this state, assume our ancestors are too
            return;
        }

        if (disallowIntercept) {
            mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
        } else {
            mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
        }

        // Pass it up to our parent
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }
```

　　可以看到 requestDisallowInterceptTouchEvent(true) 把 mGroupFlags 标签设置了 FLAG_DISALLOW_INTERCEPT 标识符。而在 dispatchTouchEvent() 方法中，判断是否拦截就是判断 mGroupFlags 有没有 FLAG_DISALLOW_INTERCEPT 标识符：

```java
        final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                if (!disallowIntercept) {
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
                    intercepted = false;
                }
```

　　这时 disallowIntercept 为 true，那么 就会跳过 onInterceptTouchEvent(ev) 方法的返回值设置 intercepted，intercepted 为 false。

　　也可以在 ACTION_DOWN 和 ACTION_UP 中 return true，但是触摸事件是父控件先执行 dispatchTouchEvent() 方法，然后父控件分发事件调用子控件的 dispatchTouchEvent() 方法，而子控件在 dispatchTouchEvent() 方法中执行 getParent().requestDisallowInterceptTouchEvent(true) 设置已经不能影响到父控件的  dispatchTouchEvent() 方法了。

　　而 ACTION_UP，事件都是最后一个了，return true 拦截子控件，如果不想拦截，直接 return false 就好了，没有必要。

## 5. 总结

　　ViewGroup事件分发示意图

![](image/ViewGroup事件分发示意图.png)

1. Android 事件分发是先传递给 ViewGroup，再由 ViewGroup 传递给 View 的。
2. 在 ViewGroup 中可以通过 onInterceptTouchEvent() 方法对事件传递进行拦截，onInterceptTouchEvent() 方法返回 true 代表不允许事件继续向子 View 传递，返回 false 代表不对事件进行拦截，默认返回 false。
3. 子 View 中如果将传递的事件消费掉，ViewGroup 中将无法接收到任何事件。

4. 如果 ViewGroup 找到了能够处理该事件的 View，则直接交给子 View 处理，自己的 onTouchEvent() 不会被触发。

5. 可以通过符写 onInterceptTouchEvent(ev) 方法，拦截子 View 的事件（即 return true），把事件交给自己处理，则会执行自己对应的 onTouchEvent() 方法。

6. 子 View 可以通过调用 getParent().requestDisallowInterceptTouchEvent(true); 阻止 ViewGroup 对其 ACTION_MOVE 或者 ACTION_UP 事件进行拦截。

## 6. 事件分发工作流程图

![](image/事件分发工作流程图.png)


## 7. 参考文章
1. [Android事件分发机制完全解析，带你从源码的角度彻底理解(下)](https://blog.csdn.net/sinyu890807/article/details/9153747)

2. [Android ViewGroup事件分发机制](https://blog.csdn.net/lmj623565791/article/details/39102591)

3. [面试：讲讲 Android 的事件分发机制](https://www.jianshu.com/p/d3758eef1f72)