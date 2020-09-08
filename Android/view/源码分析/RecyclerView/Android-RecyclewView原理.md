# RecyclerView 原理

# 一. 模块分析

## 1. 列表控件

　　继承关系
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730173153248.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)

## 2. LayoutManager

　　RecyclerView 支持列表、表格、瀑布流三种布局样式，与 ListView 自身决定布局样式不同的是，RecyclerView 使用其静态内部类 LayoutManager 来管理具体的布局样式，其子类可以实现自己的对于子 view 的测量，布局等功能。

　　RecyclerView 提供了三种 LayoutManager，LinearLayoutManager、GridLayoutManager 和 StaggeredGridLayoutManager，用户还可以通过继承 LayoutManager 来实现自己的布局管理器。

## 3. Recycler 回收机制

　　这货就是回收 view 的主要类，他是 RecyclerView 的一个内部类，管理着对 view 的分类分级回收，与 ListView 的 RecycleBin 类功能类似。

```java
class Recycler{
 
// 主要 field
final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();// 回收 attached 的 view 的,与 ListView 的 activeViews 类似
final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();// 一级缓存,可设置大小,默认缓存大小为 2
private RecycledViewPool mRecyclerPool;// 缓存池对象，多个 RecyclerView 可以指定使用一个 pool 对象
private ViewCacheExtension mViewCacheExtension;// 自定义的缓存机制对象，用户可以继承之实现自己的一个缓存对象，在指定时机会使用该对象里的缓存对象
	
// 主要方法
// 1. 获取缓存 view
View getViewForPosition(int position, boolean dryRun) {
    ...
    // 1) Find from scrap by position
    if (holder == null) {
        holder = getScrapViewForPosition(position, INVALID_TYPE, dryRun);// 从 scrapView 里 ( attachViews 或者一级缓存cachedViews ) 根据 position 获取 view
        if (holder != null) {
            if (!validateViewHolderForOffsetPosition(holder)) {
                // recycle this scrap
                ...
            } else {
                fromScrap = true;
            }
        }
    }
    if (holder == null) {
        final int offsetPosition = mAdapterHelper.findPositionOffset(position);
        ...
        final int type = mAdapter.getItemViewType(offsetPosition);
        // 2) Find from scrap via stable ids, if exists
        if (mAdapter.hasStableIds()) { // 有 stableId 就从 scrapView 里 ( attachViews 或者一级缓存 cachedViews ) 根据 itemId 获取 view
            holder = getScrapViewForId(mAdapter.getItemId(offsetPosition), type, dryRun);
            if (holder != null) {
                // update position
                holder.mPosition = offsetPosition;
                fromScrap = true;
            }
        }
        if (holder == null && mViewCacheExtension != null) {// 此时如果有自定义的 cache 会尝试从中复用
            // We are NOT sending the offsetPosition because LayoutManager does not
            // know it.
            final View view = mViewCacheExtension
                    .getViewForPositionAndType(this, position, type);
            ...
        }
        if (holder == null) { // fallback to recycler
            ...
            holder = getRecycledViewPool().getRecycledView(type);// 从 pool 里获取
            ...
        }
        if (holder == null) {
            holder = mAdapter.createViewHolder(RecyclerView.this, type);// 新建 ViewHolder
            ...
        }
    }
	...
    boolean bound = false;
    if (mState.isPreLayout() && holder.isBound()) {
        // do not update unless we absolutely have to.
        holder.mPreLayoutPosition = position;
    } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
        ...
        final int offsetPosition = mAdapterHelper.findPositionOffset(position);
        holder.mOwnerRecyclerView = RecyclerView.this;
        mAdapter.bindViewHolder(holder, offsetPosition);// 需要绑定数据
        attachAccessibilityDelegate(holder.itemView);
        bound = true;
        if (mState.isPreLayout()) {
            holder.mPreLayoutPosition = position;
        }
    }
	// 设置 lp
    final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
    final LayoutParams rvLayoutParams;
    if (lp == null) {
        rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
        holder.itemView.setLayoutParams(rvLayoutParams);
    } else if (!checkLayoutParams(lp)) {
        rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
        holder.itemView.setLayoutParams(rvLayoutParams);
    } else {
        rvLayoutParams = (LayoutParams) lp;
    }
    rvLayoutParams.mViewHolder = holder;
    rvLayoutParams.mPendingInvalidate = fromScrap && bound;
    return holder.itemView;
}
 
// 2. 从 pool 里获取指定 type 的缓存对象
public ViewHolder getRecycledView(int viewType) {
    final ArrayList<ViewHolder> scrapHeap = mScrap.get(viewType);
    if (scrapHeap != null && !scrapHeap.isEmpty()) {
        final int index = scrapHeap.size() - 1;
        final ViewHolder scrap = scrapHeap.get(index);
        scrapHeap.remove(index);
        return scrap;
    }
    return null;
}
 
// 3. 回收 ViewHolder
void recycleViewHolderInternal(ViewHolder holder) {
    ...
    if (forceRecycle || holder.isRecyclable()) {
        if (!holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED
                | ViewHolder.FLAG_UPDATE)) {
            // Retire oldest cached view
            final int cachedViewSize = mCachedViews.size();
            if (cachedViewSize == mViewCacheMax && cachedViewSize > 0) {// 一级缓存满了的话移除一个到 pool 里，然后再放入到一级缓存里
                recycleCachedViewAt(0);
            }
            if (cachedViewSize < mViewCacheMax) {
                mCachedViews.add(holder);
                cached = true;
            }
        }
        if (!cached) { // 如果没有加入到一级缓存中，则放入 pool 里缓存
            addViewHolderToRecycledViewPool(holder);
            recycled = true;
        }
    }...
}
 
// 4. 根据 type 放入到指定 type 的缓存集合中
public void putRecycledView(ViewHolder scrap) {
    final int viewType = scrap.getItemViewType();
    final ArrayList scrapHeap = getScrapHeapForType(viewType);
    ...
    scrap.resetInternal();
    scrapHeap.add(scrap);
}
 
// 5. 缓存 attach 的 view
void scrapView(View view) {
    final ViewHolder holder = getChildViewHolderInt(view);
    if (holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID)
            || !holder.isUpdated() || canReuseUpdatedViewHolder(holder)) {// 满足条件的 attach 的 view 放入 attached 集合中
        ...
        holder.setScrapContainer(this, false);
        mAttachedScrap.add(holder);
    }...
}
}
```

　　可以通过 RecyclerView 的 setRecycledViewPool 来设置该 view 的 pool 对象，exa，一个 ViewPager 的多个 RecyclerView 展示使用相同种类的 view 和 type，就可以公用一个 pool 来缓存对象，提升缓存效率。

　　可以通过 RecyclerView 的 setItemViewCacheSize 来设置一级缓存对象个数，默认为 2。

　　可以通过 RecyclerView 的 setViewCacheExtension 来设置自定义的扩展缓存，用户实现时重写 getViewForPositionAndType 方法，根据 position 和 type 来决定复用哪个对象 ( 该 cache 并不会去缓存对象 )，再从 pool 里取之前调用该方法进行复用。

# 二. View 布局流程分析

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730173334460.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)
　　这里说明一下 23.2.0 后 RecyclerView 支持 height 为 WRAP_CONTENT 情况 ( 之前版本都是和 MATCH_PARENT 一样处理 )，该情况会在 onMeausre 时就调用 dispatchLayoutStep2 进行布局，尽量不要使用。

　　布局子 view 主要是通过不同的 LayoutManager 实现的 onLayoutChildren 方法实现，大体实现思路就是计算当前绘制点的信息，包括坐标点和 item 的 position 等，并循环在剩余空间 ( 最多一屏 ) 里获取 view 并填充，该 view 的获取由多级缓存实现，包括 attach 的集合、一级缓存的集合、自定义的缓存以及可公用缓存池。

　　与 ListView 的 detach+attach 填充 view 不同的是，RecyclerView 在回收 view 时 remove 掉，再次复用时 add 进来，理论上会触发多次 requestLayout，但是 RecyclerView 重写了该方法，在里面有拦截逻辑，使得不会触发多次，所以不用担心效率问题。

　　dispatchLayoutStep1 和 3 主要处理 notifyItem 及其动画事件，后续会单独分析，这里只说实际布局子 view 的 dispatchLayoutStep2。

## 1. LinearLayoutManager 的 onLayoutChildren() 方法

```java
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    // layout algorithm:
    // 1) by checking children and other variables, find an anchor coordinate and an anchor
    //  item position.
    // 2) fill towards start, stacking from bottom
    // 3) fill towards end, stacking from top
    // 4) scroll to fulfill requirements like stack from bottom.
    ...
    updateAnchorInfoForLayout(recycler, state, mAnchorInfo);// 更新布局锚点信息
    ...
    detachAndScrapAttachedViews(recycler);// detach 和 recycle 当前所有 view
    ...else {// 垂直向下布局
        // fill towards end
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtra = extraForEnd;
        fill(recycler, mLayoutState, state, false);// 从锚点开始向下填充
        endOffset = mLayoutState.mOffset;
        final int lastElement = mLayoutState.mCurrentPosition;
        if (mLayoutState.mAvailable > 0) {
            extraForStart += mLayoutState.mAvailable;
        }
        // fill towards start
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtra = extraForStart;
        mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        fill(recycler, mLayoutState, state, false);// 从锚点开始向上填充
        startOffset = mLayoutState.mOffset;
		...
    }
	...
}
```

　　onLayoutChildren 主要是确定锚点信息，一般是根据滚动偏移量确定或者离布局方向开始/结束地方最近的 view 来确定绘制起始点和起始 adapter 的 position 。

　　然后进行所有 view 的 detach/remove 以及相应的 recycle 回收。

　　填充时根据布局方向和锚点信息开始 fill，类似与 ListView 的 fillDown/fillUp 这类的操作。

## 2. fill() 方法

　　fillFromTop 方法主要调用 fillDown 方法，从 ViewGroup 的顶部到底部进行 view 的添加。

　　其余的 fillUp、fillSpecified 等方法就是从不同的位置不同的方向开始添加 itemView，方法和 fillDown 一样，这里只拿 fillDown 来举例。

```java
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    // max offset we should set is mFastScroll + available
    final int start = layoutState.mAvailable;
    if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {
        // TODO ugly bug fix. should not happen
        if (layoutState.mAvailable < 0) {
            layoutState.mScrollingOffset += layoutState.mAvailable;
        }
        recycleByLayoutState(recycler, layoutState);// 根据滚动量来 remove 和 recycle 一部分 view
    }
    int remainingSpace = layoutState.mAvailable + layoutState.mExtra;// 计算总共可绘制空间
    LayoutChunkResult layoutChunkResult = new LayoutChunkResult();
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {// 循环添加 view 直至没有剩余绘制空间或数据绘制完毕
        layoutChunkResult.resetInternal();
        layoutChunk(recycler, state, layoutState, layoutChunkResult);// 复用并添加 view 的核心方法
        if (layoutChunkResult.mFinished) {
            break;
        }
		// 改变剩余量等信息
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        if (!layoutChunkResult.mIgnoreConsumed || mLayoutState.mScrapList != null
                || !state.isPreLayout()) {
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            // we keep a separate remaining space because mAvailable is important for recycling
            remainingSpace -= layoutChunkResult.mConsumed;
        }

        if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {
            layoutState.mScrollingOffset += layoutChunkResult.mConsumed; // 增加已滚动量
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState); // 根据新的滚动量再继续回收
        }
        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    ...
}
```

　　由代码可见，remainingSpace 为一共可绘制的区域，scrollingOffset 为滚动量，每次填充 view 后增加到滚动量上，根据新的滚动量继续回收 ( fling 状态下可能会快速滚动好几屏，此时新填充的 view 可能在下个 view 填充并更新滚动量后再次被回收)。

## 3. recycleByLayoutState()

```java
private void recycleViewsFromStart(RecyclerView.Recycler recycler, int dt) {
    ...
    // ignore padding, ViewGroup may not clip children.
    final int limit = dt;
    final int childCount = getChildCount();
    if (mShouldReverseLayout) {
        ...
    } else {
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedEnd(child) > limit) {// 从顶部开始遍历，找到第一个大于 limit ( 此次滚动后还可见 ) 的 view，回收之前的
                recycleChildren(recycler, 0, i);
                return;
            }
        }
    }
}
```

　　由代码可知，回收时会根据布局方向和滚动方向来找到一组在此次滚动量后不可见的 view 进行回收。

## 4. layoutChunk()

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    View view = layoutState.next(recycler);// 拿到 view
    ...
    LayoutParams params = (LayoutParams) view.getLayoutParams();
    if (layoutState.mScrapList == null) {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addView(view);// addView, 会根据 view 状态进行 attach 或者 add
        } else {
            addView(view, 0);
        }
    } else {
        ...
    }
    measureChildWithMargins(view, 0, 0);// measure 该 child
    result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
    ...
    layoutDecorated(view, left + params.leftMargin, top + params.topMargin,// layout 该 child
            right - params.rightMargin, bottom - params.bottomMargin);
    ...
}
```

　　该方法会获取复用的或者新建的 view，进行 add/attach，再去 measure 和 layout 即可完成布局子 view。

　　next() 方法为获取复用 view 的核心方法，上面的 Recycler 的 getViewForPosition 方法已经说过。

# 三. 滚动时 view 的展示与复用

## 1. 拖动滚动

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730173547915.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)

```java
int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (getChildCount() == 0 || dy == 0) {
        return 0;
    }
    mLayoutState.mRecycle = true;
    ensureLayoutState();
    final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    final int absDy = Math.abs(dy);
    updateLayoutState(layoutDirection, absDy, true, state);// 根据偏移量 dy 来更新 layoutState 准备填充
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);/填充view
    ...
    final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
    mOrientationHelper.offsetChildren(-scrolled);// 改变 top 和 bottom 移动 view
    ...
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}
```

　　由代码可知，根据偏移量调用 fill 填充 view，并改变 top 和 bottom 来移动 view，与 ListView 的处理方法类似。

## 2. Fling 滚动

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019073017363525.jpg)
　　与 ListView 处理 Fling 事件极为相似，在 UP 事件时计算滑动速度，并通过一个 Runnable 对象和一个 Scroller 对象，不断计算当前滑动量，并调用相应 LayoutManager 的 scrollBy 方法进行回收、添加和移动 child。

```java
// 1. ACTION_UP 事件处理
case MotionEvent.ACTION_UP: {
	// 计算速度
    mVelocityTracker.addMovement(vtev);
    eventAddedToVelocityTracker = true;
    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
    final float xvel = canScrollHorizontally ?
            -VelocityTrackerCompat.getXVelocity(mVelocityTracker, mScrollPointerId) : 0;
    final float yvel = canScrollVertically ?
            -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId) : 0;
    if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) { // 执行滚动
        setScrollState(SCROLL_STATE_IDLE);
    }
    resetTouch();
}
// 2.fling 方法
...
mViewFlinger.fling(velocityX, velocityY); // ViewFlinger 是一个 Runnable 对象
...
// 3. ViewFlinger 的 fling 方法
public void fling(int velocityX, int velocityY) {
    ...
    mScroller.fling(0, 0, velocityX, velocityY,
            Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);// scroller 的 fling 操作
    postOnAnimation();// post runnable
}
// 4.ViewFlinger 的 run 方法
// 计算当前滚动的各种值
final int x = scroller.getCurrX();
final int y = scroller.getCurrY();
final int dx = x - mLastFlingX;
final int dy = y - mLastFlingY;
...
int overscrollX = 0, overscrollY = 0;
if (mAdapter != null) {
    eatRequestLayout();
    onEnterLayoutOrScroll();
    TraceCompat.beginSection(TRACE_SCROLL_TAG);
    if (dx != 0) {
        hresult = mLayout.scrollHorizontallyBy(dx, mRecycler, mState); // 调用各自 LayoutManager 的滚动方法
        overscrollX = dx - hresult;
    }
    if (dy != 0) {
        vresult = mLayout.scrollVerticallyBy(dy, mRecycler, mState); // 调用各自 LayoutManager 的滚动方法
        overscrollY = dy - vresult;
    }
    ...
}
```

## 3. 滑动时如何使用 LayoutState 中的值来填充 view

　　LayoutState 记录用于填充布局的一些状态，几个重要的字段:

- mOffset：填充起始坐标
- mCurrentPosition：填充起始数据的 position
- mAvailable：本次滑动可填充的距离
- mScrollingOffset：滑动过的总量

### (1) layout 阶段根据锚点信息确定 LayoutState 的值：

　　在 layout 阶段，首先会更新 AnchorInfo —锚点信息，然后在 fill 之前，根据锚点的信息来设置 LayoutState 的值，mOffset 就是锚点的 coordinate 坐标 ( 起始坐标 )、mCurrentPosition 就是锚点的 position ( 起始数据的 position )、mAvailable 就是从起始点 offset 开始到最底端的距离、mScrollingOffset 设置成为一个 NaN 标志位，以便填充时考虑滑动事件。

　　根据锚点信息更新 LayoutState。

```java
// 在 LinearLayoutManager 的 onLayoutChildren() 方法中会调用
private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
    updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
}

private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
    mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
    mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD :
            LayoutState.ITEM_DIRECTION_TAIL;
    mLayoutState.mCurrentPosition = itemPosition;
    mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
    mLayoutState.mOffset = offset;
    mLayoutState.mScrollingOffset = LayoutState.SCOLLING_OFFSET_NaN;
}
```

### (2) 滑动时根据滑动量确定 LayoutState 的值：

　　在滑动阶段，会根据当前滑动量来确定 LayoutState 的值，比如向上滑动(手指向上滑动)，dy>0，那么会找到最下部的 child，其 position 作为 mCurrentPosition 以及其 bottom ( 指最底部坐标 ) 作为 mOffset — 表示从最后一个 child 后开始填充、mAvailable 就是 dy — 滚动多少就可以绘制多少；mScrollingOffset 此时应该为 0 ( 因为还未开始滚动 )，而在更新 LayoutState 的方法里将其设置成为 child 的 bottom - 整个 view 的 bottom，也就是说这里开始第一次滚动，滚动距离为最后一个 view 的不可见部分，目的是作为稍后整个填充过程的一个初始化(稍后会说到)，当然，相应的 mAvailable 也要减去相应大小表示已经滑动了那么多。

　　根据 ScrollDistance 更新 LayoutState：

```java
// LinearLayoutManager 的方法
// LinearLayoutManager 的 scrollBy() 方法中会调用
private void updateLayoutState(int layoutDirection, int requiredSpace,
        boolean canUseExistingSpace, RecyclerView.State state) {
    ...
    int scrollingOffset;
    if (layoutDirection == LayoutState.LAYOUT_END) {
        mLayoutState.mExtra += mOrientationHelper.getEndPadding();
        // get the first child in the direction we are going
        final View child = getChildClosestToEnd(); // 找到最下方的 child
        // the direction in which we are traversing children
        mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD
                : LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
        mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child);
        // calculate how much we can scroll without adding new children (independent of layout)
        scrollingOffset = mOrientationHelper.getDecoratedEnd(child)
                - mOrientationHelper.getEndAfterPadding(); // 初始化已滚动距离

    }...
    mLayoutState.mAvailable = requiredSpace;
    if (canUseExistingSpace) {
        mLayoutState.mAvailable -= scrollingOffset; // 相应的要减去已滚动距离
    }
    mLayoutState.mScrollingOffset = scrollingOffset; // 已滚动距离
}
```

### (3) 根据 LayoutState 来填充整个 view

　　fill 方法填充 view：

```java
// LinearLayoutManager 的方法
// 1.填充
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    // max offset we should set is mFastScroll + available
    final int start = layoutState.mAvailable;// 记录下原始 mAvailable 值
 
	// while 前的这次滚动时发生的回收，就是为了给上面说的给 mScrollingOffset 赋初值后的一次回收
    if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) { // 滚动时的 fill ( 还记得上面说的在 layout 阶段填充时将该变量设为 NaN 么 )
        // TODO ugly bug fix. should not happen
        if (layoutState.mAvailable < 0) {// 可用 <0 说明已经超过了可滚动距离
            layoutState.mScrollingOffset += layoutState.mAvailable;// 要将滚动总量减去超出的部分
        }
        recycleByLayoutState(recycler, layoutState);// 根据当前滚动总量做一次回收
    }
    int remainingSpace = layoutState.mAvailable + layoutState.mExtra;// 拿到剩余可填充空间，通常就是 mAvailable
    LayoutChunkResult layoutChunkResult = new LayoutChunkResult();
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) { // 如果还有可剩余填充空间且还有数据可填充时，不断的做填充、滚动、回收的操作
        layoutChunkResult.resetInternal();
        layoutChunk(recycler, state, layoutState, layoutChunkResult);// 填充一个 view
        if (layoutChunkResult.mFinished) {
            break;
        }
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;// 填充起始位置后移，consumed 为此次填充的 view 消耗的距离
        
        if (!layoutChunkResult.mIgnoreConsumed || mLayoutState.mScrapList != null
                || !state.isPreLayout()) {
            layoutState.mAvailable -= layoutChunkResult.mConsumed;//mAvailable可用距离相应减少consumed
            // we keep a separate remaining space because mAvailable is important for recycling
            remainingSpace -= layoutChunkResult.mConsumed;//剩余填充空间相应减少consumed
        }

        if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {//滚动时发生
            layoutState.mScrollingOffset += layoutChunkResult.mConsumed;//滚动总量相应增加consumed
            if (layoutState.mAvailable < 0) {//可用<0说明已经超过了可滚动距离
                layoutState.mScrollingOffset += layoutState.mAvailable;//要将滚动总量减去超出的部分
            }
            recycleByLayoutState(recycler, layoutState);//根据当前滚动总量做一次回收
        }
    }
    return start - layoutState.mAvailable;//返回总滚动量，mAvailable正好被消费完则返回值就是mAvailable，如果mAvailable<0则返回值就大于mAvailable，但是没关系，因为外部拿到次消费量后会与当前整个的dy做对比，不会使滚动量大于dy，如下代码所示
}
//2.填充后滚动 views ,在 ACTION_MOVE 的处理中会调用 scrollByInternal() 方法，会调用 scrollBy() 方法
int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    ...
    final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    final int absDy = Math.abs(dy);
    updateLayoutState(layoutDirection, absDy, true, state); // 更新 LayoutState 信息
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);//填充并拿到总的滚动量(由于fill时的mAvailable是初始化滚动后的值，所以要加上初始化滚动的值)
    ...
    final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;//这里就是上面说的，最终滚动量不会超过dy
    mOrientationHelper.offsetChildren(-scrolled);//平移整个view的child
    ...
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}
```

上述代码添加了详细的注释，基本思路就是：

1. 初始化LayoutState信息(如果是滚动需要初始化滚动量)
2. 开始填充，如果是滚动则要根据之前的初始化滚动量回收一次，如果不是直接进入3
3. 循环(直到剩余空间用完或者没有填充数据了)填充view，根据此次填充的view的consumed更新LayoutState信息
4. 如果是滚动则要根据新的滚动总量再回收
5. 返回本次填充的总距离，并于dy比较获取正确的滚动量，来平移整个view

# 四.数据刷新-局部刷新

## 1.布局和动画过程

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730173852485.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)
与ListView一样，通过adapter管理数据，通过Observer来通知刷新，但是RecyclerView提供的Observer支持局部刷新，也就是可以增删改单个item进行notify,下面代码主要以insert通知事件为主

```java
//1.dispatchLayoutStep1进行pre-layout
void addToPreLayout(ViewHolder holder, ItemHolderInfo info) {
    InfoRecord record = mLayoutHolderMap.get(holder);//将pre-layout阶段动画信息与相关的holder保存起来(保存ltrb等view信息)
    if (record == null) {
        record = InfoRecord.obtain();
        mLayoutHolderMap.put(holder, record);
    }
    record.preInfo = info;
    record.flags |= FLAG_PRE;
}
 
void preProcess() {
    mOpReorderer.reorderOps(mPendingUpdates);
    final int count = mPendingUpdates.size();
    for (int i = 0; i < count; i++) {
        UpdateOp op = mPendingUpdates.get(i);
        switch (op.cmd) {
            case UpdateOp.ADD:
                applyAdd(op);//根据添加的操作类型来执行不同的操作
                break;
            ...
        }
        ...
    }
    mPendingUpdates.clear();
}
 
private void postponeAndUpdateViewHolders(UpdateOp op) {
    ...
    mPostponedList.add(op);
    switch (op.cmd) {
        case UpdateOp.ADD:
            mCallback.offsetPositionsForAdd(op.positionStart, op.itemCount);//通知add事件，改变各holder的信息等(改变了holder的position)
            break;
        ...
    }
}
 
void offsetPositionRecordsForInsert(int positionStart, int itemCount) {
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for (int i = 0; i < childCount; i++) {
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if (holder != null && !holder.shouldIgnore() && holder.mPosition >= positionStart) {
            ...
            holder.offsetPosition(itemCount, false);//改变该holder位置信息
            mState.mStructureChanged = true;
        }
    }
    mRecycler.offsetPositionRecordsForInsert(positionStart, itemCount);//将该动作影响的holder的position全部更新
    requestLayout();
}
 
//2.dispatchLayoutStep2布局子view，局部刷新Holder
View getViewForPosition(int position, boolean dryRun) {
    ...
    boolean bound = false;
    if (mState.isPreLayout() && holder.isBound()) {
        // do not update unless we absolutely have to.
        holder.mPreLayoutPosition = position;
    } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {//满足需要重新绑定数据才会调用bind,下面会细说
        if (DEBUG && holder.isRemoved()) {
            throw new IllegalStateException("Removed holder should be bound and it should"
                    + " come here only in pre-layout. Holder: " + holder);
        }
        final int offsetPosition = mAdapterHelper.findPositionOffset(position);
        holder.mOwnerRecyclerView = RecyclerView.this;
        mAdapter.bindViewHolder(holder, offsetPosition);//调用bind绑定数据
        attachAccessibilityDelegate(holder.itemView);
        bound = true;
        if (mState.isPreLayout()) {
            holder.mPreLayoutPosition = position;
        }
    }
	...
}
 
//3.dispatchLayoutStep3实现view动画
void addToPostLayout(ViewHolder holder, ItemHolderInfo info) {
    InfoRecord record = mLayoutHolderMap.get(holder);//将post-layout阶段动画信息与相关的holder保存起来(view布局后的ltrb等信息)
    if (record == null) {
        record = InfoRecord.obtain();
        mLayoutHolderMap.put(holder, record);
    }
    record.postInfo = info;
    record.flags |= FLAG_POST;
}
 
void process(ProcessCallback callback) {
    for (int index = mLayoutHolderMap.size() - 1; index >= 0; index --) {//根据每个事件前后layout的动画信息来设置动画
        final ViewHolder viewHolder = mLayoutHolderMap.keyAt(index);
        final InfoRecord record = mLayoutHolderMap.removeAt(index);
        ...else if ((record.flags & FLAG_APPEAR_PRE_AND_POST) == FLAG_APPEAR_PRE_AND_POST) {
            // Appeared in the layout but not in the adapter (e.g. entered the viewport)
            callback.processAppeared(viewHolder, record.preInfo, record.postInfo);//处理insert事件
        }...
        InfoRecord.recycle(record);
    }
}
 
public boolean animateAppearance(@NonNull ViewHolder viewHolder,
        @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    ...else {
        return animateAdd(viewHolder);
    }
}
public boolean animateAdd(final ViewHolder holder) {DefaultItemAnimator为默认动画事件处理动画
    resetAnimation(holder);
    ViewCompat.setAlpha(holder.itemView, 0);//透明度的动画
    mPendingAdditions.add(holder);//加入集合后续使用
    return true;
}
 
public void runPendingAnimations() {//DefaultItemAnimator的执行动画方法
	//各种动画事件的集合,在设置时已经放入
    boolean removalsPending = !mPendingRemovals.isEmpty();
    boolean movesPending = !mPendingMoves.isEmpty();
    boolean changesPending = !mPendingChanges.isEmpty();
    boolean additionsPending = !mPendingAdditions.isEmpty();
    if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
        // nothing to animate
        return;
    }
    // First, remove stuff
    for (ViewHolder holder : mPendingRemovals) {
        animateRemoveImpl(holder);
    }
    mPendingRemovals.clear();
    // Next, move stuff
    if (movesPending) {
        ...
    }
    // Next, change stuff, to run in parallel with move animations
    if (changesPending) {
        ...
    }
    // Next, add stuff
    if (additionsPending) {//insert动画
        final ArrayList<ViewHolder> additions = new ArrayList<>();
        additions.addAll(mPendingAdditions);
        mAdditionsList.add(additions);
        mPendingAdditions.clear();
        Runnable adder = new Runnable() {
            public void run() {
                for (ViewHolder holder : additions) {
                    animateAddImpl(holder);//使用view.animate来添加动画
                }
                additions.clear();
                mAdditionsList.remove(additions);
            }
        };
        if (removalsPending || movesPending || changesPending) {//有其他的动画则要延迟执行该动画
            long removeDuration = removalsPending ? getRemoveDuration() : 0;
            long moveDuration = movesPending ? getMoveDuration() : 0;
            long changeDuration = changesPending ? getChangeDuration() : 0;
            long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
            View view = additions.get(0).itemView;
            ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
        } else {
            adder.run();//执行
        }
    }
}
123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162
```

在pre-layout阶段获取保存动画信息，在实际layout阶段复用view并根据需要部分holder需要rebind数据，在post-layout阶段获取保存最终动画信息

根据前后动画信息设置最终的view动画效果

执行时按照一定顺序排列动画，再执行

## 2.实现局部刷新(指定ViewHolder完成rebind数据)

上面说到notifyXxx后，会重新layout，也就会重新获取ViewHolder并且为需要的ViewHolder rebind数据，来实现局部刷新，那么如何确定哪个ViewHolder需要重新bind数据呢？这里讲几种常见情况

### (1)notifyDataSetChanged刷新全部

在调用该方法后，onChanged方法会将所有holder添加一个ADAPTER_POSITION_UNKNOWN的flag标识其position未知，并将一个boolean变量mDataSetHasChangedAfterLayout置为true

在重新re-layout时，会根据此变量为true而将所有holder添加INVALID和UPDATE的flag

这样在真正layout时，回收时因为holder的invalid标志而放入pool中回收，复用从pool中取出时holder的所有标志都已重置，就需要rebind数据了

```java
//1.onChanged调用setDataSetChangedAfterLayout方法
private void setDataSetChangedAfterLayout() {
    mDataSetHasChangedAfterLayout = true;
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for (int i = 0; i < childCount; i++) {
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if (holder != null && !holder.shouldIgnore()) {
            holder.addFlags(ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);//添加tag
        }
    }
    mRecycler.setAdapterPositionsAsUnknown();
}
//2.dispatchLayoutStep1时调用processAdapterUpdatesAndSetAnimationFlags方法处理操作信息
private void processAdapterUpdatesAndSetAnimationFlags() {
    if (mDataSetHasChangedAfterLayout) {//该变量在notifyDataSetChanged时置为true
        // Processing these items have no value since data set changed unexpectedly.
        // Instead, we just reset it.
        mAdapterHelper.reset();
        markKnownViewsInvalid();//更新holder标志
        mLayout.onItemsChanged(this);
    }
    ...
}
//3.markKnownViewsInvalid
void markKnownViewsInvalid() {
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for (int i = 0; i < childCount; i++) {
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if (holder != null && !holder.shouldIgnore()) {
            holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);//所有holder加入INVALID和UPDATE的tag
        }
    }...
}
//4.在dispatchLayoutStep2时回收所有当前view
private void scrapOrRecycleView(Recycler recycler, int index, View view) {
    final ViewHolder viewHolder = getChildViewHolderInt(view);
    if (viewHolder.isInvalid() && !viewHolder.isRemoved() &&
            !mRecyclerView.mAdapter.hasStableIds()) {//有INVALID标志位则收入pool中并重置holder属性
        removeViewAt(index);
        recycler.recycleViewHolderInternal(viewHolder);
    }...
}
//5.从pool中取出复用时
holder = getRecycledViewPool().getRecycledView(type);
if (holder != null) {
    holder.resetInternal();//重置属性
}
if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {//重置后的holder没有BOUND属性
    final int offsetPosition = mAdapterHelper.findPositionOffset(position);//获取布局后position设入holder(考虑了各种增删改操作后)
    holder.mOwnerRecyclerView = RecyclerView.this;
    mAdapter.bindViewHolder(holder, offsetPosition);//rebind数据
}
12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152
```

### (2)notifyItemInserted添加item

上面说过，在dispatchLayoutStep1时会根据操作集合(增删改)mPendingUpdates来执行相应的offsetPosition操作，就是将因为此操作而将要改变位置的holder的position全部更新

比如在2的位置insert了一个item，那么offsetPosition后，当前所有从2开始的holder的position全部后移1，包括cached里的holder

那么在dispatchLayoutStep2复用position为2的holder时就没有，只能从pool里取或者新建holder，那么holder肯定标志位是重置的，需要rebind数据，代码参照上面的例子

### (3)notifyItemUpdate更新某个item

该方法与add不一样的是，他不是通过offsetPosition来改变holderposition来做的，因为并没有holder因为他而改变，他时通过将update的holder添加一个UPDATE的tag

在复用时，会因为有update标志位而rebind数据

```java
//1.处理操作集合
private void postponeAndUpdateViewHolders(UpdateOp op) {
    mPostponedList.add(op);
    switch (op.cmd) {
        ...
        case UpdateOp.UPDATE:
            mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
            break;
    }
}
//2.markViewHoldersUpdated调用viewRangeUpdate方法来更新holder
void viewRangeUpdate(int positionStart, int itemCount, Object payload) {
    ...
    for (int i = 0; i < childCount; i++) {
        final View child = mChildHelper.getUnfilteredChildAt(i);
        final ViewHolder holder = getChildViewHolderInt(child);
        if (holder == null || holder.shouldIgnore()) {
            continue;
        }
        if (holder.mPosition >= positionStart && holder.mPosition < positionEnd) {
            // We re-bind these view holders after pre-processing is complete so that
            // ViewHolders have their final positions assigned.
            holder.addFlags(ViewHolder.FLAG_UPDATE);//添加UPDATE的tag
            holder.addChangePayload(payload);
            // lp cannot be null since we get ViewHolder from it.
            ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
        }
    }
    mRecycler.viewRangeUpdate(positionStart, itemCount);//包括cached里的holder
}
//3.复用时rebind
if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {//重置后的holder有UPDATE属性
    final int offsetPosition = mAdapterHelper.findPositionOffset(position);//获取布局后position设入holder(考虑了各种增删改操作后)
    holder.mOwnerRecyclerView = RecyclerView.this;
    mAdapter.bindViewHolder(holder, offsetPosition);//rebind数据
}
123456789101112131415161718192021222324252627282930313233343536
```

其他局部刷新类似，不再赘述

# 五.获取ViewHolder的position

有时我们需要通过ViewHolder知道其view所在的position，RecyclerView提供了两个方法来获取position：

- getLayoutPosition：获取最近一次布局后该ViewHolder所在的position
- getAdapterPosition：获取该ViewHolder对应的在Adapter的position

我们先看相关代码

```java
//1.获取最近一次layout后的position
public final int getLayoutPosition() {
    return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
}
//2.在pre-layout过程中上面说过，会处理相关holder因为notify事件产生的position的改变
void offsetPosition(int offset, boolean applyToPreLayout) {
    if (mOldPosition == NO_POSITION) {
        mOldPosition = mPosition;
    }
	//更新最新位置
    if (mPreLayoutPosition == NO_POSITION) {
        mPreLayoutPosition = mPosition;
    }
    if (applyToPreLayout) {
        mPreLayoutPosition += offset;
    }
    mPosition += offset;
    if (itemView.getLayoutParams() != null) {
        ((LayoutParams) itemView.getLayoutParams()).mInsetsDirty = true;
    }
}
//3.获取在adapter里的position
private int getAdapterPositionFor(ViewHolder viewHolder) {
    if (viewHolder.hasAnyOfTheFlags( ViewHolder.FLAG_INVALID |
            ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)
            || !viewHolder.isBound()) {
        return RecyclerView.NO_POSITION;//在某些操作发生后获取会得到NO_POSITION
    }
    return mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
}
public int applyPendingUpdatesToPosition(int position) {
    final int size = mPendingUpdates.size();
    for (int i = 0; i < size; i ++) {//根据每个事件的影响来更新position
        UpdateOp op = mPendingUpdates.get(i);
        switch (op.cmd) {
            case UpdateOp.ADD://比如add了两个item在position上，则该position的对应的holder的位置要向后移两个位置
                if (op.positionStart <= position) {
                    position += op.itemCount;
                }
                break;
            ...
        }
    }
    return position;
}
123456789101112131415161718192021222324252627282930313233343536373839404142434445
```

如代码所示，这两种获取方式有不一致的可能性：

getLayoutPosition是在布局完成后真实的position，而getAdapterPosition是依赖于对item做的Operation的情况，如果在notifyXxx后下次layout前，通过getAdapterPosition获取position，那么拿到的是layout后的position，因为操作集合mPendingUpdates已经有了数据，提前计算出了新的position

而且在notifyDataSetChanged方法刷新全部后，由于不知道具体的是增删改哪个操作，那么会将adapterPosition置为NO_POSITION，直到下次layout完成

当layout完成后，getAdapterPosition和getLayoutPosition拿到的是一样的

# 六.position和itemId

与ListView一样，RecyclerView的adapter也可以设置itemId以及是否需要stableId。

但是RecyclerView本身没有实现HeaderFooter的功能，所以自己实现的时候可以使用装饰器模式包装adapter，在需要position的地方时处理好header和footer的情况，也就不需要stableId了，比如onBindViewHolder和onItemClickListener的时候

# 七.Divider的绘制

RecyclerView的divider不像ListView那样简单设置一下即可，RecyclerView提供了一个内部类ItemDecoration，通过继承实现自己的ItemDecoration加入到RecyclerView中，在RecyclerView绘制的时候就调用其相应方法绘制在view上

```java
public void draw(Canvas c) {
    super.draw(c);
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        mItemDecorations.get(i).onDrawOver(c, this, mState);//draw的最后调用drawOver进行最终绘制，不会被其他绘制覆盖
    }
    ...
}
12345678
```

需要注意的是ItemDecoration有个getItemOffstes方法，传入一个Rect对象允许用户设置ltrb的值，该方法的作用是：

在测量布局view时，用户设置了这个rect，该rect的尺寸会被计算到整个view里，比如你想要的divider高度时5px，那么在该方法里设置rect的bottom为5即可；这个Rect的ltrb都会在layout时算在child的marginXxx里

而在onDrawOver方法里找到坐标点直接画一个高度为5的divider不也绘制了5px的divider么？其实是在下一个view的5px上画了divider

也就是说drawOver和rect设置高度是两回事，rect设置是两个view的真正间距，而drawOver只是你自己决定在哪里开始画divider的。

# 参考文章

1. [Android RecyclerView 原理解析](https://blog.csdn.net/qq_15827013/article/details/97798716)



