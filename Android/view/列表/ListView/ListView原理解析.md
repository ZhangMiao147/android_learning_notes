# ListView 原理解析

# 一. 模块分析

## 1. 列表控件

**(1) 继承关系**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730192458111.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)

**(2)层次说明**

　　AdapterView 是一种 ViewGroup，负责展示与一组数据相关的 view，这些数据由一种 adapter 进行管理，该 ViewGroup 通过 adapter 来监听数据的改变以及相应的处理。

　　AbsListView 继承自 AdapterView，泛型参数为 ListAdapter，一个列表 adapter，该 ViewGroup 有一套 view 的回收复用机制供子类使用，专门负责对多数据多 view 的控件的展示优化。

　　ListView 与 GridView 都继承自 AbsListView，使用了其父类的回收机制，并重写相应的布局方法来实现各自的布局。

　　ExpandableListView 继承了 ListView，只是对数据的展示划分的更细一层，分为 section 和 child 。

## 2. Adapter 适配器

　　列表控件是为了展示一组关联数据的 view 的，他的工作模式就是一个列表控件加上一个数据源即可，但是列表控件并不需要关心具体是什么类型的数据源，因为如果要列表控件直接和数据源打交道，那么控件所要做的工作就很繁琐而且没有任何扩展性。

　　所以只需要为这个数据源定义一个通用的实现接口，使控件通过指定的方法去完成对数据的监控和使用，而不需要关心数据是什么类型。

　　Adapter 就是一个接口，定义了一组数据的统一方法，可以通过实现该接口完成各种各样的子类，比如 ArrayAdapter 专门存放一个简单数组的 adapter、SimpleCursorAdapter 存放游标数据的 adapter 等等，而列表控件只需知道这是一种数据源、并可以通过同样的方法来访问数据即可。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730192524388.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)

## 3. RecycleBin 回收机制

　　这个类就是回收 view 的主要类，他是 AbsListView 一个内部类 ( 刚刚提到，回收是由列表控件基类 AbsListView 实现的 )，下面是一些主要的代码和简单的注解，先有个概念，下面分析流程时会重点讲到他的运作流程。

```java
/**
* RecycleBin 有助于跨布局重用视图。RecycleBin 有两个级别的存储：ActiveViews 和 ScrapViews。ActiveViews 是那些在布局开始时出现在屏幕上的视图。通过构造，它们显示当前信息。布局结束时，ActiveViews 中的所有视图都将降级为 ScrapViews。ScrapViews 是旧视图，适配器可能会使用它来避免不必要地分配视图。
*/
class RecycleBin{
 
// 主要 field
private View[] mActiveViews = new View[0]; // 这是存储正在显示中的 view 的集合
private int mViewTypeCount;// itemTypeCount，有多少种 item 的type，也就是我们常写的 adapter里的 getViewTypeCount 的值
private ArrayList<View>[] mScrapViews;// 这是已被回收，等待重用的集合数组，为什么是集合的数组呢，因为要根据不同的itemType来存储相应 type 的已回收的 view
private ArrayList<View> mCurrentScrap;// 这是已被回收的 view 的集合，与上面的集合数组不同，这是 itemTypeCount 为 1 的时候存放回收 view 的集合，上面那个是按 type 存的所以要多个集合
	
// 主要方法
// 1. 初始化设置(每次 setAdapter 时候会更新)
public void setViewTypeCount(int viewTypeCount) {
    if (viewTypeCount < 1) { // viewTypeCount 必须得大于 0
        throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
    }
    //noinspection unchecked
    ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];//这里要注意，viewType 范围得是 0～viewTypeCount-1 
    for (int i = 0; i < viewTypeCount; i++) {
        scrapViews[i] = new ArrayList<View>();
    }
    mViewTypeCount = viewTypeCount;// 初始化设置一些参数
    mCurrentScrap = scrapViews[0];
    mScrapViews = scrapViews;
}
 
// 2. 添加正在显示的 view 到集合
void fillActiveViews(int childCount, int firstActivePosition) {
    if (mActiveViews.length < childCount) {
        mActiveViews = new View[childCount];
    }
    mFirstActivePosition = firstActivePosition;

    //noinspection MismatchedReadAndWriteOfArray
    final View[] activeViews = mActiveViews;
    for (int i = 0; i < childCount; i++) {
        // 拿到子 view
        View child = getChildAt(i);
        AbsListView.LayoutParams lp = (AbsListView.LayoutParams) child.getLayoutParams();
        // Don't put header or footer views into the scrap heap
        if (lp != null && lp.viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
            // Note:  We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in active views.
            //        However, we will NOT place them into scrap views.
            // 将子 view 存储到 activeViews 中，只填充 childCount 个
            activeViews[i] = child;
            // Remember the position so that setupChild() doesn't reset state.
            lp.scrappedFromPosition = firstActivePosition + i;
        }
    }
}
 
// 3.获取 activeView
View getActiveView(int position) {
    int index = position - mFirstActivePosition;// 真实的 child 的 index，就是显示的 view 在 activeViews 的 index
    final View[] activeViews = mActiveViews;
    if (index >=0 && index < activeViews.length) {
        final View match = activeViews[index];
        activeViews[index] = null;// 取出返回，不能再次使用
        return match;
    }
    return null;
}
 
// 4. 添加 view 到回收池
void addScrapView(View scrap, int position) {
    final AbsListView.LayoutParams lp = (AbsListView.LayoutParams) scrap.getLayoutParams();
    ...
    lp.scrappedFromPosition = position;// 记录 lp 的 position, 后续先根据 position 取 view

    // Remove but don't scrap header or footer views, or views that
    // should otherwise not be recycled.
    final int viewType = lp.viewType;
    if (!shouldRecycleViewType(viewType)) { // 不应该被回收
        ...
        return;
    }

    ...
        if (mViewTypeCount == 1) { // 就一种 viewType 放入到 mCurrentScrap 中
            mCurrentScrap.add(scrap);
        } else {
            // 放入指定 viewType 的 scrapViews 集合里
            mScrapViews[viewType].add(scrap);
        }
	  ...
    }
}
 
// 5.获取一个 scrapView
View getScrapView(int position) {
    final int whichScrap = mAdapter.getItemViewType(position);
    ...
	// 从指定的 scrap 集合取 view
    if (mViewTypeCount == 1) {
        return retrieveFromScrap(mCurrentScrap, position);
    } else if (whichScrap < mScrapViews.length) {
        return retrieveFromScrap(mScrapViews[whichScrap], position);
    }
    return null;
}
 
private View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
    final int size = scrapViews.size();
    if (size > 0) {
        // 如果仍然有以该 position 或者 getItemId 所指定的 id 相关联的 view 则取出
        for (int i = 0; i < size; i++) {
            final View view = scrapViews.get(i);
            final AbsListView.LayoutParams params =
                    (AbsListView.LayoutParams) view.getLayoutParams();
            if (mAdapterHasStableIds) {
                final long id = mAdapter.getItemId(position);//itemId
                if (id == params.itemId) {
                    return scrapViews.remove(i);
                }
            } else if (params.scrappedFromPosition == position) {// addScrapView 时记录的 position
                final View scrap = scrapViews.remove(i);
                clearAccessibilityFromScrap(scrap);
                return scrap;
            }
        }
        final View scrap = scrapViews.remove(size - 1);// 否则拿出并移除 scrapView 集合的最后一个来使用
        clearAccessibilityFromScrap(scrap);
        return scrap;
    } else {
        return null;
    }
}
}
```

　　RecycleBin 主要管理着回收的 view 的集合以及正在显示的 view 的集合，还有对这些的集合的增删查功能，列表控件会在自己的不同处理时机调用这些方法来完成对 view 的复用。

# 二. View 布局流程分析

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730192620190.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)
　　View 的执行流程无非就分为三步，onMeasure() 用于测量 View 的大小，onLayout() 用于确定 View 的布局，onDraw() 用于将 View 绘制到界面上。而在 ListView 当中，onMeasure() 并没有什么特殊的地方，因为它终归是一个 View，占用的空间最多并且通常也就是整个屏幕。onDraw() 在 ListView 当中也没有什么意义，因为 ListView 本身并不负责绘制，而是由 ListView 当中的子元素来进行绘制的。那么 ListView 大部分的神奇功能其实都是在 onLayout() 方法中进行的，AbsListView 里的 onLayout 没有进行过多的处理，真正的布局子 view 的方法是 layoutChildren()，由子类重写实现。

## 1. ListView 的 layoutChildren() 方法

```java
protected void layoutChildren() {
    ...
        if (mAdapter == null) {// adapter 为 null
            resetList();// 清空已有 views 和各种集合、状态
            invokeOnItemScrollListener();
            return;
        }

        ...

        boolean dataChanged = mDataChanged;
        if (dataChanged) {
            handleDataChanged();//处理一些逻辑，mLayoutMode 的改变
        }
		...        

        // Pull all children into the RecycleBin.
        // These views will be reused if possible
        final int firstPosition = mFirstPosition;
        final RecycleBin recycleBin = mRecycler;
        if (dataChanged) {// 数据改变，现有 view 全部添加到 scrapView 集合，准备重新设置 item 时复用
            for (int i = 0; i < childCount; i++) {
                recycleBin.addScrapView(getChildAt(i), firstPosition+i);
            }
        } else {// 没有改变数据，只是重新 layout，那么将所有现有 view 加入到 activeViews 准备复用
            recycleBin.fillActiveViews(childCount, firstPosition);
        }

        // Clear out old views
        detachAllViewsFromParent();// 将所有 view 从 parent 上 detach 掉，后续这些复用的view 只需 attach 上即可，不会重新 inflate
        recycleBin.removeSkippedScrap();

        switch (mLayoutMode) {// 根据 mLayoutMode 决定加载 view 的方向和方式
        case LAYOUT_SET_SELECTION:
            if (newSel != null) {
                sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
            } else {
                sel = fillFromMiddle(childrenTop, childrenBottom);
            }
            break;
        case LAYOUT_SYNC:
            sel = fillSpecific(mSyncPosition, mSpecificTop);
            break;
        case LAYOUT_FORCE_BOTTOM:
            sel = fillUp(mItemCount - 1, childrenBottom);
            adjustViewsUpOrDown();
            break;
        case LAYOUT_FORCE_TOP:
            mFirstPosition = 0;
            sel = fillFromTop(childrenTop);
            adjustViewsUpOrDown();
            break;
        case LAYOUT_SPECIFIC:
            sel = fillSpecific(reconcileSelectedPosition(), mSpecificTop);
            break;
        case LAYOUT_MOVE_SELECTION:
            sel = moveSelection(oldSel, newSel, delta, childrenTop, childrenBottom);
            break;
        default:// NORMAL 情况(一般情况)
            if (childCount == 0) {// 当前无 view 显示
                if (!mStackFromBottom) {// 从顶到底布局
                    final int position = lookForSelectablePosition(0, true);
                    setSelectedPositionInt(position);
                    sel = fillFromTop(childrenTop);// 从顶到底布局
                } else {
                    final int position = lookForSelectablePosition(mItemCount - 1, false);
                    setSelectedPositionInt(position);
                    sel = fillUp(mItemCount - 1, childrenBottom);
                }
            } else {// 已有显示的 view
                if (mSelectedPosition >= 0 && mSelectedPosition < mItemCount) {
                    sel = fillSpecific(mSelectedPosition,
                            oldSel == null ? childrenTop : oldSel.getTop());
                } else if (mFirstPosition < mItemCount) {//通常情况
					          // 从mFirstPosition位置开始设置 view (从 top 到 mFirstPosition-1，从 mFirstPosition+1 到 bottom)
                    sel = fillSpecific(mFirstPosition,
                            oldFirst == null ? childrenTop : oldFirst.getTop());
                } else {
                    sel = fillSpecific(0, childrenTop);
                }
            }
            break;
        }

        ...
}
```

　　layoutChildren 主要是根据数据是否改变来更新 activeViews 或者 scrapViews 的状态，然后根据数据状态来进行不同方向不同 position 的 itemView 的添加。

　　有一点需要注意，在更新完 active 和 scrapView 后要将全部 view 从 parent 上 detach 掉，因为当 view 因为一些原因多次调用 layoutChildren 时，会执行相同的逻辑，如果不 detach 掉会有相同的 view 会在后面 attach。

## 2. fillFromTop() 方法 → fillDown() 方法 ViewGroup 顶部到底部设置 view 

　　fillFromTop() 方法主要调用 fillDown() 方法，从 ViewGroup 的顶部到底部进行 view 的添加。

　　其余的 fillUp、fillSpecified 等方法就是从不同的位置不同的方向开始添加 itemView，方法和 fillDown 一样，这里只拿 fillDown 来举例。

```java
private View fillDown(int pos, int nextTop) {
    ...
	  // 从第 pos 个 item 开始，拿到其 view，根据 view 的大小增加 top 的值，循环添加 itemView，直到下一个 view 的 top 已经大于 ViewGroup 的 bottom 或者全部 item 添加完成
    while (nextTop < end && pos < mItemCount) {
        // is this the selected item?
        boolean selected = pos == mSelectedPosition;
        View child = makeAndAddView(pos, nextTop, true, mListPadding.left, selected);
        nextTop = child.getBottom() + mDividerHeight;
        ...
        pos++;
    }
	...
}
```

　　由代码可见，从 top 开始逐个添加 itemView，并计算下一个 item 的 top，直到下一个 top 大于底部就停止添加，所以一次只加载一屏的数据。

　　makeAndAddView() 方法就是获取和添加每个 itemView 的方法，也是复用 view 的地方。

## 3. ListView#makeAndAddView()

```java
private View makeAndAddView(int position, int y, boolean flow, int childrenLeft,
        boolean selected) {
    View child;
    if (!mDataChanged) {//没有数据改变时
        // Try to use an existing view for this position
        child = mRecycler.getActiveView(position);// 从 activeViews 里拿到 view (上面说到在没有改变时将已有 view 全部放入到了 activeViews 里)
        if (child != null) {// 没有拿到还需要进行 obtainView 获取
            // Found it -- we're using an existing child
            // This just needs to be positioned
            setupChild(child, position, y, flow, childrenLeft, selected, true);// 添加 view 到层级上
            return child;
        }
    }

    // Make a new view for this position, or convert an unused view if possible
    child = obtainView(position, mIsScrap);// 复用或新建 view

    // This needs to be positioned and measured
    setupChild(child, position, y, flow, childrenLeft, selected, mIsScrap[0]);// 添加 view 到层级上

    return child;
}
```

　　由代码可知，没有数据改变时就获取 activeViews 里的 view 继续显示即可，否则需要去 obtainView 进行复用或者新建 view 来添加。

　　obtainView 就是从 scrapViews 里复用或者新建的方法；当拿到 view 时，setupChild 方法就是将 view 添加到层级中的方法。

## 4. AbsListView#obtainView()

```java
View obtainView(int position, boolean[] isScrap) {
    ...
    final View scrapView = mRecycler.getScrapView(position);// 拿到 view
    final View child = mAdapter.getView(position, scrapView, this);// 调用 adapter 的 getView 并传入该 view(就是我们常用的 getView 方法)
    if (scrapView != null) {// 为 null 说明 child 是新建的 view
        if (child != scrapView) {// 当 child 没有复用而是新建的 view 时，要将 scrapView 再添加回 scrapViews 里(这就是为什么我们每次新建的话都会创建新的 view 到 scrapViews 里，没有复用效果)
            // Failed to re-bind the data, return scrap to the heap.
            mRecycler.addScrapView(scrapView, position);
        } else {
            isScrap[0] = true;// 将该标志位置为 true 说明 view 是复用的，后续不用重新 measure 

            // Finish the temporary detach started in addScrapView().
            child.dispatchFinishTemporaryDetach();
        }
    }
    ...
}
```

　　上面说到 getScrapView 会先根据 position 拿 view，如果还存有对应 position ( addScrapView 时会设置对应的 position ) 的 scrapView 就拿出来，否则就从 scrapViews 里拿出最后一个来复用。

## 5. ListView#setupChild()

　　ListView 的 makeAndAddView() 方法中，调用了 obtainView() 获取了 View 之后就会调用 setupChild() 方法。

```java
/**
* 将视图添加为子视图，并确保其已测量（如有必要）并正确定位。
*/
private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft,
        boolean selected, boolean recycled) {
  
    final boolean isSelected = selected && shouldShowSelector();
  	final boolean updateChildSelected = isSelected != child.isSelected();
    final int mode = mTouchMode;
    final boolean isPressed = mode > TOUCH_MODE_DOWN && mode < TOUCH_MODE_SCROLL
                && mMotionPosition == position;
    final boolean needToMeasure = !isAttachedToWindow || updateChildSelected
                || child.isLayoutRequested();
	  ...
    // Respect layout params that are already in the view. Otherwise make some up...
    // noinspection unchecked
    AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
    if (p == null) {
        p = (AbsListView.LayoutParams) generateDefaultLayoutParams();// 没有则设置默认的 lp
    }
    p.viewType = mAdapter.getItemViewType(position);// 将 viewType 设置到 lp 里( add 和 get 时根据 type 来处理)

    if ((recycled && !p.forceAdd) || (p.recycledHeaderFooter
            && p.viewType == AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER)) {// 如果是复用的 view ( 还记的上面说的将 isScrap[0] 标志置为 true 么)
        attachViewToParent(child, flowDown ? -1 : 0, p);// 将 view 重新 attach 到 parent 上即可
    } else {// 是新建的 view
        p.forceAdd = false;
        if (p.viewType == AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
            p.recycledHeaderFooter = true;
        }
        addViewInLayout(child, flowDown ? -1 : 0, p, true);// 第一次需要 add 到 parent 里
    }
    ...
    if (needToMeasure) {// 是否需要测量和是否是回收的 view 以及 AbsListView 的 onLayout 时设置 child 是否需要 requestLayout 有关
        final int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mListPadding.left + mListPadding.right, p.width);
        final int lpHeight = p.height;
        final int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(),
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    } else {
        cleanupLayoutState(child);
    }

    final int w = child.getMeasuredWidth();
    final int h = child.getMeasuredHeight();
    final int childTop = flowDown ? y : y - h;

    if (needToMeasure) {// measure 完计算 ltrb 进行 child 的 layout
        final int childRight = childrenLeft + w;
        final int childBottom = childTop + h;
        child.layout(childrenLeft, childTop, childRight, childBottom);
    } else {
        child.offsetLeftAndRight(childrenLeft - child.getLeft());
        child.offsetTopAndBottom(childTop - child.getTop());
    }
	...
}
```

　　主要是根据是否是使用复用的 view 来判断应该将该 view 重新 attach 到 parent 上还是首次 add 到 parent 上。

　　然后根据需要来 measure 和 layout 该 child 完成添加。

# 三. 滚动时 view 的展示与复用

## 1. 拖动滑动

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730192827185.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)
　　在 AbsListView 的 onTouchEvent 方法中，当捕获到 MOVE 事件时交由 onTouchMove 方法处理，该方法通过判断 scroll 的类型是普通的 SCROLL 时调用 scrollIfNeeded方法，计算从开始触摸到此刻的 deltaY 距离以及增量 incrementY，调用 trackMotionScroll 方法进行处理 view。

```java
boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
    ...
    final boolean down = incrementalDeltaY < 0;
	...
    if (down) {// ListView 向上滚动
        int top = -incrementalDeltaY;
        if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            top += listPadding.top;
        }
        for (int i = 0; i < childCount; i++) {// 从顶到底遍历 view，看是否产生不可见 view(bottom<top)
            final View child = getChildAt(i);
            if (child.getBottom() >= top) {
                break;
            } else {
                count++;
                int position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    // The view will be rebound to new data, clear any
                    // system-managed transient state.
                    child.clearAccessibilityFocus();
                    mRecycler.addScrapView(child, position);// 回收
                }
            }
        }
    } else {// ListView 向下滚动
        int bottom = getHeight() - incrementalDeltaY;
        if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            bottom -= listPadding.bottom;
        }
        for (int i = childCount - 1; i >= 0; i--) {// 从底到顶遍历 view，看是否产生不可见 view(top>bottom)
            final View child = getChildAt(i);
            if (child.getTop() <= bottom) {
                break;
            } else {
                start = i;
                count++;
                int position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    // The view will be rebound to new data, clear any
                    // system-managed transient state.
                    child.clearAccessibilityFocus();
                    mRecycler.addScrapView(child, position);// 回收
                }
            }
        }
    }
	...
    if (count > 0) {// 将回收的 view 从 parent 上 detach 掉
        detachViewsFromParent(start, count);
        mRecycler.removeSkippedScrap();
    }
	offsetChildrenTopAndBottom(incrementalDeltaY);// 通过改变每个 view 的 top 来相当于移动 view
	...
    final int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
    if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
        fillGap(down);// 有新的 view 可见，调用此方法进行添加(此方法由子类重写实现自己的填充新 view )
    }
	...
}
```

　　由代码可知，通过判断 incrementY 的正负来确定是向上滑动还是向下滑动，并判断相应的 view 是否不可见，如果处于不可见则要 addScrapView，并将这些不可见的 view 从 parent 中 detach 掉；然后要将所有的 view 通过改变 top 来移动位置；最后，如果有新的 view 从顶部或底部可见，调用 fillGap 来填充该 view，fillGap 核心还是调用的 fillDown 或者 fillUp，不再叙述。

## 2. Fling 滑动

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730192912839.jpg)
　　在 fling 状态下，onTouchUp 处理 up 事件，然后通过 VelocityTracker 计算滑动速度，以此速度开启一个 scroller 对象的 fling 状态，然后 post 执行一个 runnable 对象，在 run 方法里不断计算 scroller 当前的滑动量，调用 trackMotionScroll 方法进行当前滚动位置后的回收、移动和 fill 。

```java
// 1. onTouchUp
final VelocityTracker velocityTracker = mVelocityTracker;
velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

final int initialVelocity = (int)
        (velocityTracker.getYVelocity(mActivePointerId) * mVelocityScale);
// Fling if we have enough velocity and we aren't at a boundary.
// Since we can potentially overfling more than we can overscroll, don't
// allow the weird behavior where you can scroll to a boundary then
// fling further.
boolean flingVelocity = Math.abs(initialVelocity) > mMinimumVelocity;
if (flingVelocity &&
        !((mFirstPosition == 0 &&
                firstChildTop == contentTop - mOverscrollDistance) ||
          (mFirstPosition + childCount == mItemCount &&
                lastChildBottom == contentBottom + mOverscrollDistance))) {
    if (!dispatchNestedPreFling(0, -initialVelocity)) {
        if (mFlingRunnable == null) {
            mFlingRunnable = new FlingRunnable();
        }
        reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
        mFlingRunnable.start(-initialVelocity);// 开始 scroller 的 fling 操作
        dispatchNestedFling(0, -initialVelocity, true);
    } else {
        mTouchMode = TOUCH_MODE_REST;
        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
    }
}...
// 2. FlingRunnable 的 start 方法
void start(int initialVelocity) {
    ...
    mScroller.fling(0, initialY, 0, initialVelocity,
            0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);//fling
    mTouchMode = TOUCH_MODE_FLING;
    postOnAnimation(this);//post runnable
	...
}
// 3. FlingRunnable 的 run 方法
final OverScroller scroller = mScroller;
boolean more = scroller.computeScrollOffset();
final int y = scroller.getCurrY();
int delta = mLastFlingY - y;
...
final boolean atEdge = trackMotionScroll(delta, delta);// 执行滚动操作
...
```

# 四. 数据刷新- AdapterDataSetObserver 观察者模式

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730193000470.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)
　　adapter 用于管理一个数据集合及其种种操作，并且还应该可以在数据发生改变的时候得到相应的通知，所以， Adapter 使用了观察者模式，允许向其注册多个 DataSetObserver 对象，当 adapter 的数据发生改变时，通知这些观察者，使其完成自己的操作。

```java
// 1.Adapter 可以注册/取消观察者
void registerDataSetObserver(DataSetObserver observer);
void unregisterDataSetObserver(DataSetObserver observer);
 
// 2.AdapterView 自己有一个观察者内部类，子类注册到 adapter 上，以便在数据发生改变时做相应处理
// AdapterView 的内部类
class AdapterDataSetObserver extends DataSetObserver {
	...
    @Override
    public void onChanged() {
        mDataChanged = true;// 数据已更新
        mOldItemCount = mItemCount;// 更新 oldItemCount
        mItemCount = getAdapter().getCount();// 更新 itemCount
		...
        requestLayout();// 重新请求布局
    }
	...
}
 
//3. AbsListView 在创建时会主动创建一个继承自上述观察者类的观察者并在 adapter 更新时注册到 adapter 上
mDataSetObserver = new AdapterDataSetObserver();
mAdapter.registerDataSetObserver(mDataSetObserver);
```

　　由代码可知，ListView 自己创建一个观察者，并在 adapter 更新的地方保证给其注册一个该观察者，这样，当我们数据更新时经常调用的 notifyDataSetChanged 方法被调用后，会触发相应观察者的方法，也就是 onChanged 方法，此时会更新状态及数据信息，然后请求重新布局绘制，最终会回到 layoutChildren 方法开始布局。

　　除此之外，还可以创建自己的观察者注册到 adapter 上，不用担心被覆盖，因为 BaseAdapter 实现 Observable 来注册观察者，该类维护的是一个观察者数组，需要注意的是，notify 的时候会从后往前一次调用观察者的 onChanged 方法。

# 五. ListView 的 Adapter 装饰器模式

　　注意到 ListView 是可以添加多个 Header 和 Footer 的，ListView 也会将其作为 adapter 的一部分(占用与普通数据一样的 position)，但是自己写的 adapter 并没有去处理这些特殊 item，那 ListView 如何知道并管理的呢？

## 1. Header 和 Footer 的 Adapter

　　ListView 对其管理的 adapter 使用了装饰器模式，构建 adapter 时，当有 header 或 footer 时，ListView 会将 adapter 作为被装饰者，连同 Header 和 Footer 信息一起创建一个自己维护的带 Header 和 Footer 的 adapter 对象，作为真正管理的 adapter，在管理过程中调用的 adapter 的任何方法都是在这个 adapter 上调用的，只不过该 adapter 的方法又都调用自己的 adapter (被装饰者)的相应方法，所以可以完全通过使用自己的 adapter 的方法达到任何效果。

```java
public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
    private final ListAdapter mAdapter;//被装饰者
	// Header 和 Footer 信息
    ArrayList<ListView.FixedViewInfo> mHeaderViewInfos;
    ArrayList<ListView.FixedViewInfo> mFooterViewInfos;
    ...
    @Override
    public boolean isEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }
    @Override
    public int getCount() {
        if (mAdapter != null) {
            return getFootersCount() + getHeadersCount() + mAdapter.getCount();
        } else {
            return getFootersCount() + getHeadersCount();
        }
    }
	@Override
    public Object getItem(int position) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).data;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItem(adjPosition);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).data;
    }
	@Override
    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }
	@Override
    public boolean hasStableIds() {
        if (mAdapter != null) {
            return mAdapter.hasStableIds();
        }
        return false;
    }
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Header (negative positions will throw an IndexOutOfBoundsException)
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return mHeaderViewInfos.get(position).view;
        }

        // Adapter
        final int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getView(adjPosition, convertView, parent);
            }
        }

        // Footer (off-limits positions will throw an IndexOutOfBoundsException)
        return mFooterViewInfos.get(adjPosition - adapterCount).view;
    }
	@Override
    public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }

        return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
    }
	@Override
    public int getViewTypeCount() {
        if (mAdapter != null) {
            return mAdapter.getViewTypeCount();
        }
        return 1;
    }
	@Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(observer);
        }
    }
	@Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
        }
    }
	...
}
```

　　由源码可知，该 adapter 维护了我们自己的 adapter，在重写的方法里，自己处理了关于 header 和 footer 的各种情况，并把普通 item 的情况交由我们的 adapter 来处理，所以我们的 adapter 只需关注自己的数据集合即可。

## 2. Listview 创建 Adapter

　　在 4.4 版本之前，通过 ListView 的 addHeaderView 和 addFooterView 添加 header 和 footer，然后在 setAdapter 的时候，会根据有没有 header 或 footer 信息决定要不使用装饰器；如果在 setAdapter 之后再去调用这些方法将会出现问题。

　　从 4.4 版本开始，ListView 解决了这个问题，在 setAdapter 之后添加 header 或 footer时，增加了对 adapter 的再创建，如果原来没有使用装饰器，则重新构建一个装饰器作为 adapter，并且还会调用上面说到的自带的观察者的 onChanged 方法进行刷新。

```java
//1.设置(更改) adapter
public void setAdapter(ListAdapter adapter) {
    if (mAdapter != null && mDataSetObserver != null) {// 取消旧 adapter 的观察者
        mAdapter.unregisterDataSetObserver(mDataSetObserver);
    }

    resetList();// 重置 view 状态
    mRecycler.clear();

    if (mHeaderViewInfos.size() > 0|| mFooterViewInfos.size() > 0) {// 有 header 或 footer 信息则要使用装饰器 adapter
        mAdapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
    } else {
        mAdapter = adapter;
    }
	...
    // AbsListView#setAdapter will update choice mode states.
    super.setAdapter(adapter);

    if (mAdapter != null) {
        mAreAllItemsSelectable = mAdapter.areAllItemsEnabled();// 更新信息
        mOldItemCount = mItemCount;
        mItemCount = mAdapter.getCount();
        checkFocus();

        mDataSetObserver = new AdapterDataSetObserver();
        mAdapter.registerDataSetObserver(mDataSetObserver);// 新 adapter 注册观察者

        mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());// 更新 RecycleBin 信息
		...
    }...

    requestLayout();// 刷新布局
}
 
//2. 增加 header( footer 类似 header)
public void addHeaderView(View v, Object data, boolean isSelectable) {
    ...
    mHeaderViewInfos.add(info);//添加header信息
    mAreAllItemsSelectable &= isSelectable;

    // Wrap the adapter if it wasn't already wrapped.
    if (mAdapter != null) {
        if (!(mAdapter instanceof HeaderViewListAdapter)) {// 如果还没有使用装饰器则使用装饰器构建 adapter
            mAdapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, mAdapter);
        }

        // In the case of re-adding a header view, or adding one later on,
        // we need to notify the observer.
        if (mDataSetObserver != null) {// 通知刷新布局
            mDataSetObserver.onChanged();
        }
    }
}
```

# 六. ListView 的 Item 点击事件

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190730193250343.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE1ODI3MDEz,size_16,color_FFFFFF,t_70)

## 1. onItemClickListener

　　ListView 的 item 点击事件可以通过 setOnItemClickListener 设置，该监听器是 AdapterView 维护的一个监听器，可以监听到每个 item 的点击事件，他并不是通过给每个 itemView 设置 OnClickListener 来监听点击事件，否则每次使用 view 都会创建新的监听器对象。

　　ListView ( 实际上是 AbsListView 管理 ) 是在 onTouchEvent 时判断当前触摸的 item 的 position，然后postDelay 一个 PerformClick 内部类对象，该对象的 run 方法，取到相应 position 的 childView，然后调用 onItemClickListener 回调。

```java
//1.onTouchDown 获取触摸 item 的 position
int motionPosition = pointToPosition(x, y);
public int pointToPosition(int x, int y) {
    ...
    final int count = getChildCount();
    for (int i = count - 1; i >= 0; i--) {
        final View child = getChildAt(i);
        if (child.getVisibility() == View.VISIBLE) {
            child.getHitRect(frame);
            if (frame.contains(x, y)) {// 遍历每个 view，判断是否触摸点在该 view 内
                return mFirstPosition + i;
            }
        }
    }
    return INVALID_POSITION;
 
// 2. onTouchUp 时执行通过 handler 发送一个点击事件处理的 runnable
if (mPerformClick == null) {
    mPerformClick = new PerformClick();
}
final AbsListView.PerformClick performClick = mPerformClick;
performClick.mClickMotionPosition = motionPosition;
...
performClick.run();
 
// 3. 执行 run 方法
private class PerformClick extends WindowRunnnable implements Runnable {
    int mClickMotionPosition;

    @Override
    public void run() {
        // The data has changed since we posted this action in the event queue,
        // bail out before bad things happen
        if (mDataChanged) return;

        final ListAdapter adapter = mAdapter;
        final int motionPosition = mClickMotionPosition;
        if (adapter != null && mItemCount > 0 &&
                motionPosition != INVALID_POSITION &&
                motionPosition < adapter.getCount() && sameWindow()) {
            final View view = getChildAt(motionPosition - mFirstPosition);// 根据 itemPosition 找到 child 的 position
            // If there is no view, something bad happened (the view scrolled off the
            // screen, etc.) and we should cancel the click
            if (view != null) {
                performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
            }
        }
    }
}

// 4.AdapterView 处理点击事件
public boolean performItemClick(View view, int position, long id) {
    final boolean result;
    if (mOnItemClickListener != null) {
        playSoundEffect(SoundEffectConstants.CLICK);
        // this 就是 AdapterView，也就是 ListView,view 是点击的 ViewGroup 的子 View
        mOnItemClickListener.onItemClick(this, view, position, id);
        result = true;
    }...
    return result;
}
}
```

## 2. onItemLongClickListener

　　onItemLongClickListener 是监听 item 的长按事件对象，与 onItemClickListener 类似，他也不是通过给每个 child 设置监听器来实现。

　　在 onTouchDown 时，postDelay 一个点击 runnable，在其 run 方法里，找到对应 position 的 child，如果可以长按( isLongClickable )，则 postDelay 一个长按事件runnable，delay 为系统默认的长按事件的响应时间，在该 run 方法中执行 onItemLongClickListener 回调。

　　除此之外，当点击或者长按事件发生后，另一个事件(runnable)在相应的方法中就会被 removeCallback 调不再执行；在 onTouchMove 时也会取消相应的 runnable 。

```java
// 1.触发 TapRunnable
if (mPendingCheckForTap == null) {
    mPendingCheckForTap = new CheckForTap();
}
mPendingCheckForTap.x = ev.getX();
mPendingCheckForTap.y = ev.getY();
postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());// 执行轻触事件
 
// 2.TapRunnable 的 run 方法
if (longClickable) {
    if (mPendingCheckForLongPress == null) {
        mPendingCheckForLongPress = new CheckForLongPress();
    }
    mPendingCheckForLongPress.rememberWindowAttachCount();
    postDelayed(mPendingCheckForLongPress, longPressTimeout);// 提交一个长按事件 runnable，delay 时间为系统默认长按响应时间
} else {
    mTouchMode = TOUCH_MODE_DONE_WAITING;
}
 
// 3. 长按事件响应
public void run() {
    final int motionPosition = mMotionPosition;
    final View child = getChildAt(motionPosition - mFirstPosition);
    if (child != null) {
        final int longPressPosition = mMotionPosition;
        final long longPressId = mAdapter.getItemId(mMotionPosition);

        boolean handled = false;
        if (sameWindow() && !mDataChanged) {
            handled = performLongPress(child, longPressPosition, longPressId);// 处理事件
        }
        if (handled) {
            mTouchMode = TOUCH_MODE_REST;
            setPressed(false);// 处理 view 状态
            child.setPressed(false);
        } else {
            mTouchMode = TOUCH_MODE_DONE_WAITING;
        }
    }
}
 
boolean performLongPress(final View child,
    ...
    boolean handled = false;
    if (mOnItemLongClickListener != null) {// 调用回调
        handled = mOnItemLongClickListener.onItemLongClick(AbsListView.this, child,
                longPressPosition, longPressId);
    }
    ...
    return handled;
}
```

## 3. position 和 itemId 的区别

　　在我们设置 onItemClickListener 时，重写的方法里的参数有 position 和 id，我们一般习惯性的使用 getItem(position) 取得相应的数据，但是这在有 header 和 footer 的时候是有问题的，我们先来看下代码

```java
public long getItemId(int position) {
    int numHeaders = getHeadersCount();
    if (mAdapter != null && position >= numHeaders) {
        int adjPosition = position - numHeaders;
        int adapterCount = mAdapter.getCount();
        if (adjPosition < adapterCount) {
            return mAdapter.getItemId(adjPosition);//返回用户定义的id
        }
    }
    return -1;
}
```

　　这是 HeaderViewListAdapter 的 getItemId 方法，还记得上面说过的，在 AbsListView 里的 PerformClick 里来处理 onItemClick 事件，传入的 position 时触摸点所在的 view 的 position，他是包含了 header 和 footer 的位置的，所以在有 header 和 footer 的情况下通过 getItem(position) 拿数据时就会错位，而且会有越界异常。

　　而 getItemId() 方法，会将 header 和 footer 的 id 返回 -1，否则会返回我们自定义 adapter 的 getItemId() 方法返回的 id，此时传入的参数是数据正确的 position，所以只要我们在自定义的 adapter 中，重写 getItemId() 方法，并直接返回这个 position，就可以保证 OnItemClickListener 中拿到的 id，不是 -1(headers) 就是正确数据的 position，稍加判断即可。

　　要想使用这个 itemId，需要重写 hasStableIds() 方法并返回 true，否则无效。

# 参考文章

1. [Android ListView 原理解析](https://blog.csdn.net/qq_15827013/article/details/97809431)



 