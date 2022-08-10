## RecyclerView 的工作原理

# 一、引言

在平时的开发过程中，当用到滑动布局时，用的比较多的是 ListView 或 ScrollView，但对于 RecyclerView 的使用却比较少，也就是在需要用到水平滑动布局时才会想到 RecyclerView。那在有了 ListView 的情况下，为什么 Google 还要推出 RecyclerView 呢？下面从源码角度来分析一下该 RecyclerView 的布局与缓存原理，看看其与 ListView 有什么区别。

# 二、源码分析

`RecyclerView` 一般使用方式是在 `Layout` 中定义布局文件，然后在 `Activity` 中通过`findViewById` 来拿到 `RecyclerView` 的实例对象，因此从 `RecyclerView` 的构造函数入手进行分析。

```csharp
public RecyclerView() {
    ...
    if (attrs != null) {
        int defStyleRes = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, styleable.RecyclerView, defStyle, defStyleRes);
        String layoutManagerName = a.getString(styleable.RecyclerView_layoutManager);
        ...
        a.recycle();
        this.createLayoutManager(context, layoutManagerName, attrs, defStyle, defStyleRes);
        ...
    } 
    ...
}
```

构造函数中告诉我们，可以在布局文件中通过 `app:layoutManager` 来设置 `RecyclerView` 的 `LayoutManager` 对象。`LayoutManager` 主要负责 `RecyclerView` 的布局。

拿到 `RecyclerView` 对象后，如果在构造函数中没有设置 `LayoutManager`，可以通过调用 `RecyclerView` 的 `setLayoutManager(RecyclerView.LayoutManager layout)` 方法进行设置。



```cpp
public void setLayoutManager(RecyclerView.LayoutManager layout) {
    if (layout != this.mLayout) {
        ...
        this.mLayout = layout;   
        ...       
    }
}
```

然后 `RecyclerView` 会调用 `setAdapter` 方法。

```kotlin
public void setAdapter(RecyclerView.Adapter adapter) {
    this.setLayoutFrozen(false);
    this.setAdapterInternal(adapter, false, true);
    this.requestLayout();
}
```

`setAdapterInternal` 方法主要作用是将传进来的 `adapter` 保存到 `mAdapter` 变量。之后调用了 `requestLayout` 方法。



```kotlin
public void requestLayout() {
    if (this.mEatRequestLayout == 0 && !this.mLayoutFrozen) {
        super.requestLayout();
    } else {
        this.mLayoutRequestEaten = true;
    }
}
```

`requestLayout` 方法又调用了父类的 `requestLayout` 方法，最终调用了 `View` 的 `requestLayout` 方法。

```csharp
public void requestLayout() {
    ...
    if (mParent != null && !mParent.isLayoutRequested()) {
        mParent.requestLayout();
    }
    ...
}
```

上面的 `mParent` 的真正实例为 `ViewRootImpl`，也就是说执行了 `ViewRootImpl` 的 `requestLayout` 方法。

```cpp
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```

`scheduleTraversals` 方法被执行，意味着后续开始执行 `RecyclerView` 的 `onMeasure`、`onLayout`、`onDraw` 方法，之后`RecyclerView` 中子视图就展示出来了。

这里我们将 `onLayout` 方法单拎出来进行分析，因为 `RecyclerView` 之所以能适配多种滚动布局，主要是 `onLayout` 方法发挥作用。



```java
protected void onLayout(boolean changed, int l, int t, int r, int b) {
    ...
    this.dispatchLayout();
    ...
}
```

`onLayout` 方法接着调用了 `dispatchLayout` 方法。

```cpp
void dispatchLayout() {
    ...
    mState.mIsMeasuring = false;
    if (mState.mLayoutStep == State.STEP_START) {
        dispatchLayoutStep1();
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } 
    ...
    dispatchLayoutStep3();
}
```

`dispatchLayout` 方法依次调用了 `dispatchLayoutStep1`、`dispatchLayoutStep2`、`dispatchLayoutStep3` 方法。

我们首先看 `dispatchLayoutStep1` 方法。



```cpp
private void dispatchLayoutStep1() {
    ...  
    if (mState.mRunPredictiveAnimations) {
        ...
        mLayout.onLayoutChildren(mRecycler, mState);
        ...
    } else {
        clearOldPositions();
    }
    ...
}
```

`dispatchLayoutStep1` 方法中调用了 `mLayout` 的 `onLayoutChildren` 方法。上面分析告诉我们，`mLayout` 就是 `LayoutManager`，所以我们转到 `LayoutManager` 的 `onLayoutChildren` 方法。

```cpp
public void onLayoutChildren(Recycler recycler, State state) {
    Log.e(TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
}
```

`onLayoutChildren` 方法是一个空实现，其具体实现在各个子类中。我们拿 `LinearLayoutManager` 进行分析，看其中 `onLayoutChildren` 的实现。

```java
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    // layout algorithm:
    // 1) by checking children and other variables, find an anchor coordinate and an anchor
    //  item position.
    // 2) fill towards start, stacking from bottom
    // 3) fill towards end, stacking from top
    // 4) scroll to fulfill requirements like stack from bottom.
    // create layout state
    ...
    if (mAnchorInfo.mLayoutFromEnd) {
        ...
    } else {
        // fill towards end
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtra = extraForEnd;
        fill(recycler, mLayoutState, state, false);
        endOffset = mLayoutState.mOffset;
        final int lastElement = mLayoutState.mCurrentPosition;
        if (mLayoutState.mAvailable > 0) {
            extraForStart += mLayoutState.mAvailable;
        }
        // fill towards start
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtra = extraForStart;
        mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        fill(recycler, mLayoutState, state, false);
        startOffset = mLayoutState.mOffset;

        if (mLayoutState.mAvailable > 0) {
            extraForEnd = mLayoutState.mAvailable;
            // start could not consume all it should. add more items towards end
            updateLayoutStateToFillEnd(lastElement, endOffset);
            mLayoutState.mExtra = extraForEnd;
            fill(recycler, mLayoutState, state, false);
            endOffset = mLayoutState.mOffset;
        }
    }
    ...
}
```

`onLayoutChildren` 方法中的注释已经为我们说明了 `RecyclerView` 的布局算法，`mAnchorInfo` 为布局锚点信息，包含了子控件在Y轴上起始绘制偏移量（coordinate），`itemView` 在 `Adapter` 中的索引位置（position）和布局方向（mLayoutFromEnd)-表示start、end方向。该方法的功能是：确定布局锚点，并以此为起点向开始和结束方向填充 `ItemView`，如下图所示。

![img](https://upload-images.jianshu.io/upload_images/7025056-5c51306e82ec53cb.png?imageMogr2/auto-orient/strip|imageView2/2/w/507/format/webp)

在 `onLayoutChildren` 方法中，调用了 `fill` 方法，从该方法名可以知道，该方法应该是将子控件加入到`RecyclerView` 中的。

```java
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    ...
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        ...
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        ...
    }
    ...
    return start - layoutState.mAvailable;
}
```

`fill` 方法中循环调用了 `layoutChunkResult` 方法。



```csharp
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    View view = layoutState.next(recycler);
    ...
    LayoutParams params = (LayoutParams) view.getLayoutParams();
    if (layoutState.mScrapList == null) {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addView(view);
        } else {
            addView(view, 0);
        }
    } else {
        ...
    }
    measureChildWithMargins(view, 0, 0);
    result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
    int left, top, right, bottom;
    ...
    layoutDecoratedWithMargins(view, left, top, right, bottom);
    ...
    result.mFocusable = view.hasFocusable();
}
```

`layoutChunk` 方法中，layoutState的next方法将从 `Recycler` 获取的 `View` 添加到 `RecyclerView` 中，从而完成了整个 `RecyclerView` 的布局。

以上就是 `RecyclerView` 渲染过程的源码分析，接下来我们来分析一下 `RecyclerView` 的滑动过程。

`RecyclerView` 本质上就是一个 `View`，所以我们从它的 `onTouchEvent` 方法入手进行分析。

```java
public boolean onTouchEvent(MotionEvent e) {
    ...
    switch (action) {
        case MotionEvent.ACTION_DOWN: {
            ...
        } break;
        case MotionEvent.ACTION_POINTER_DOWN: {
            ...
        } break;

        case MotionEvent.ACTION_MOVE: {
            ...

            if (mScrollState == SCROLL_STATE_DRAGGING) {
                mLastTouchX = x - mScrollOffset[0];
                mLastTouchY = y - mScrollOffset[1];

                if (scrollByInternal(
                        canScrollHorizontally ? dx : 0,
                        canScrollVertically ? dy : 0,
                        vtev)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mGapWorker != null && (dx != 0 || dy != 0)) {
                    mGapWorker.postFromTraversal(this, dx, dy);
                }
            }
        } break;

        case MotionEvent.ACTION_POINTER_UP: {
            ...
        } break;

        case MotionEvent.ACTION_UP: {
            ...
        } break;

        case MotionEvent.ACTION_CANCEL: {
            ...
        } break;
    }
    ...
    return true;
}
```

`onTouchEvent` 方法中主要关注的是 `action` 为 `MotionEvent.ACTION_MOVE` 的情况，在滑动过程中调用了`scrollByInternal` 方法。

```java
boolean scrollByInternal(int x, int y, MotionEvent ev) {
    ...
    if (mAdapter != null) {
        ...
        if (x != 0) {
            consumedX = mLayout.scrollHorizontallyBy(x, mRecycler, mState);
            unconsumedX = x - consumedX;
        }
        if (y != 0) {
            consumedY = mLayout.scrollVerticallyBy(y, mRecycler, mState);
            unconsumedY = y - consumedY;
        }
        ...
    }
    ...
    return consumedX != 0 || consumedY != 0;
}
```

当上下滑动时，垂直方向上的y偏移量是不等于0的，从而执行了 `LayoutManager` 的 `scrollVerticallyBy` 方法。我们拿 `LinearLayoutManager` 的 `scrollVerticallyBy` 来举例。



```cpp
public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
        RecyclerView.State state) {
    if (mOrientation == HORIZONTAL) {
        return 0;
    }
    return scrollBy(dy, recycler, state);
}
```

当上下滑动时，执行了 `scrollBy` 方法。

```java
int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    ...
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);
    ...
    mOrientationHelper.offsetChildren(-scrolled);
    ...
    return scrolled;
}
```

`scrollBy` 方法中又执行了 `fill` 方法，该方法的作用是向可填充区域填充 `itemView`，我们具体看一下 `fill` 方法的实现。



```java
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    ...
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        layoutChunkResult.resetInternal();
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        if (layoutChunkResult.mFinished) {
            break;
        }
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        ...
    }
    return start - layoutState.mAvailable;
}
```

fill方法中又调用了layoutChunk方法。



```csharp
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    View view = layoutState.next(recycler);
    ...
    LayoutParams params = (LayoutParams) view.getLayoutParams();
    if (layoutState.mScrapList == null) {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addView(view);
        } else {
            addView(view, 0);
        }
    } else {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addDisappearingView(view);
        } else {
            addDisappearingView(view, 0);
        }
    }
    ...
    
    result.mFocusable = view.isFocusable();
}
```

`layoutChunk` 方法中出现了一个很重要的方法，就是 `LayoutManager.LayoutState` 的 `next` 方法，该方法的实现如下。



```kotlin
View next(RecyclerView.Recycler recycler) {
    if (mScrapList != null) {
        return nextViewFromScrapList();
    }
    final View view = recycler.getViewForPosition(mCurrentPosition);
    mCurrentPosition += mItemDirection;
    return view;
}
```

这里通过Recycler去获取了一个可重复利用的View，若该View不存在则创建一个新View，原理和ListView的Recycler基本无异。下图展示了RecyclerView循环复用View的原理。

![img](https://upload-images.jianshu.io/upload_images/7025056-bb0fe0a93e18badb.png?imageMogr2/auto-orient/strip|imageView2/2/w/459/format/webp)

# 三、总结

本文从源码的角度分析了RecyclerView的布局与滑动过程中View的缓存原理。相对于ListView来说，RecyclerView的布局和View的缓存原理与ListView差不多一致，但是RecyclerView扩展了ListView的特性，不但可以做到垂直滑动，也能做到水平滑动，并且在创建多样式滚动View方面也做得比ListView出色。可以说，RecyclerView就是ListView的一个增强版本。

# 参考文章

3. [【源码解析】RecyclerView的工作原理](https://www.jianshu.com/p/8c508b51a7b5)











