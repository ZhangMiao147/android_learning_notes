# View 绘制流程

## View 绘制
　　每一个视图的绘制过程都必须经历三个最主要的阶段，即 onMeasure()、onLayout() 和 onDraw()。

### onMeasure()
　　measure 是测量的意思，那么 onMeasure() 方法顾名思义就是用来测量视图的大小的。View 系统的绘制流程会从 ViewRoot 的 performTravesals() 方法中开始，在其内部调用 View 的 measure() 方法。measure() 方法接收两个参数，widthMeasureSpec 和 heightMeasureSpec，这两个值分别用于确定视图的宽度和高度的规格和大小。

　　MeasureSpec 的值由 specSize 和 specMode 共同组成的，其中 specSize 记录的是大小，specMode 记录的是规格。

　　specMode 一共有三种类型：

　　1.EXACTLY

　　表示父视图希望子视图的大小应该是由 specSize 的值来决定的，系统默认会按照这个规则来设置子视图的大小，开发人员当然也可以按照自己的意愿设置成任意的大小。

　　2.AT_MOST

　　表示子视图最多只能是 specSize 中指定的大小，开发人员应该尽可能小得去设置这个视图，并且保证不会超过 specSize。系统默认会按照这个规则来设置子视图的大小，开发人员当然也可以按照自己的意愿设置成任意的大小。

　　3.UNSPECIFIED

　　表示开发人员可以将视图按照自己的意愿设置成任意的大小，没有任何限制。这种情况比较少见，不太会用到。

　　通常情况下，这两个值都是由父视图经过计算后传递给子视图的，说明父视图会在一定程度上决定子视图的大小。但是最外层的根视图，它的 widthMeasureSpace 和 heightMeasureSpec 从哪里得到？这就需要去分析 ViewRoot 中的源码了，观察 performTraversals() 方法：
```
ViewRootImpl.java
/**
 * The top of a view hierarchy, implementing the needed protocol between View
 * and the WindowManager.  This is for the most part an internal implementation
 * detail of {@link WindowManagerGlobal}.
 *
 * {@hide}
 */
@SuppressWarnings({"EmptyCatchBlock", "PointlessBooleanExpression"})
public final class ViewRootImpl implements ViewParent,
        View.AttachInfo.Callbacks, ThreadedRenderer.HardwareDrawCallbacks {

    private void performTraversals() {
		...
        int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
        int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
		...
	}
}
```

　　可以看到，调用了 getRootMeasureSpec() 方法去获取 widthMeasureSpec 和 heightMeasureSpec 的值，注意方法中传入的参数，其中 lp.width 和 lp.height 在创建 ViewGroup 实例的时候就被赋值了，它们都等于 MATCH_PARENT。然后看下 getRootMeasureSpec() 方法中的代码：
```
    /**
     * Figures out the measure spec for the root view in a window based on it's
     * layout params.
     * 在一个窗口中根据父布局的布局参数计算出它的测量规范
     * @param windowSize
     *            The available width or height of the window
     *
     * @param rootDimension
     *            The layout params for one dimension (width or height) of the
     *            window.
     *
     * @return The measure spec to use to measure the root view.
     */
    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        int measureSpec;
        switch (rootDimension) {

        case ViewGroup.LayoutParams.MATCH_PARENT:
            // Window can't resize. Force root view to be windowSize.
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
            break;
        case ViewGroup.LayoutParams.WRAP_CONTENT:
            // Window can resize. Set max size for root view.
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
            break;
        default:
            // Window wants to be an exact size. Force root view to be that size.
            measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
            break;
        }
        return measureSpec;
    }
```

　　可以看到，这里使用了 MeasureSpec.makeMeasureSpec() 方法来组装一个 MeasureSpec，当 rootDimension 参数等于 MATCH_PARENT 的时候，MeasureSpec 的 specMode 就等于 EXACTLY，当 rootDimension 等于 WRAP_CONTENT 的时候，MeasureSpec 的 specMode 就等于 AT_MOST。并且 MATCH_PARENT 和 WRAP_CONTENT 时的 specSize 都是等于 windowSize 的，也就意味着根视图总是会充满全屏的。

　　查看 View 的 mesaure() 方法：
```
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {
    /**
     * <p>
     * This is called to find out how big a view should be. The parent
     * supplies constraint information in the width and height parameters.
     * </p>
     *
     * <p>
     * The actual measurement work of a view is performed in
     * {@link #onMeasure(int, int)}, called by this method. Therefore, only
     * {@link #onMeasure(int, int)} can and must be overridden by subclasses.
     * </p>
     *
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the
     *        parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the
     *        parent
     *
     * @see #onMeasure(int, int)
     */
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int oWidth  = insets.left + insets.right;
            int oHeight = insets.top  + insets.bottom;
            widthMeasureSpec  = MeasureSpec.adjust(widthMeasureSpec,  optical ? -oWidth  : oWidth);
            heightMeasureSpec = MeasureSpec.adjust(heightMeasureSpec, optical ? -oHeight : oHeight);
        }

        // Suppress sign extension for the low bytes
        long key = (long) widthMeasureSpec << 32 | (long) heightMeasureSpec & 0xffffffffL;
        if (mMeasureCache == null) mMeasureCache = new LongSparseLongArray(2);

        final boolean forceLayout = (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;

        // Optimize layout by avoiding an extra EXACTLY pass when the view is
        // already measured as the correct size. In API 23 and below, this
        // extra pass is required to make LinearLayout re-distribute weight.
        final boolean specChanged = widthMeasureSpec != mOldWidthMeasureSpec
                || heightMeasureSpec != mOldHeightMeasureSpec;
        final boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY;
        final boolean matchesSpecSize = getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec)
                && getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec);
        final boolean needsLayout = specChanged
                && (sAlwaysRemeasureExactly || !isSpecExactly || !matchesSpecSize);

        if (forceLayout || needsLayout) {
            // first clears the measured dimension flag
            mPrivateFlags &= ~PFLAG_MEASURED_DIMENSION_SET;

            resolveRtlPropertiesIfNeeded();

            int cacheIndex = forceLayout ? -1 : mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                // measure ourselves, this should set the measured dimension flag back
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            } else {
                long value = mMeasureCache.valueAt(cacheIndex);
                // Casting a long to int drops the high 32 bits, no mask needed
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                mPrivateFlags3 |= PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            }

            // flag not set, setMeasuredDimension() was not invoked, we raise
            // an exception to warn the developer
            if ((mPrivateFlags & PFLAG_MEASURED_DIMENSION_SET) != PFLAG_MEASURED_DIMENSION_SET) {
                throw new IllegalStateException("View with id " + getId() + ": "
                        + getClass().getName() + "#onMeasure() did not set the"
                        + " measured dimension by calling"
                        + " setMeasuredDimension()");
            }

            mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
        }

        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;

        mMeasureCache.put(key, ((long) mMeasuredWidth) << 32 |
                (long) mMeasuredHeight & 0xffffffffL); // suppress sign extension
    }
}
```
　　measure() 这个方法是 final 的，因此是无法在子类中重写这个方法，说明 Android 是不允许改变 View 的 measure 框架的。在 measure() 方法中调用了 onMeasure() 方法，这才是真正去测量并设置 View 大小的地方，默认会调用 getDefaultSize() 方法来获取视图的大小。
```
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    /**
     * Utility to return a default size. Uses the supplied size if the
     * MeasureSpec imposed no constraints. Will get larger if allowed
     * by the MeasureSpec.
     *
     * @param size Default size for this view
     * @param measureSpec Constraints imposed by the parent
     * @return The size this view should be.
     */
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
}
```
　　参数 measureSpec 是一直从 measure() 方法中传递过来的。然后调用 MeasureSpec.getMode() 方法可以解析出 specMode，调用 MeasureSpec.getSize() 方法可以解析出 specMode，调用 MeasureSpec.getSize() 方法可以解析出 specSize。接下来进行判断，如果 specMode 等于 AT_MOST 或 EXACTLY 就可以返回 specSize，这也是系统默认的行为，之后会在 onMeasure() 方法中调用 setMeasuredDimension() 方法来设定测量出的大小，这样一次 measure 过程就结束了。

　　一个界面的展示可能会涉及到很多次的 measure，因为一个布局中一般都会包含多个子视图，每个视图都需要经历一次 measure 过程。ViewGroup 中定义了一个 measureChildren() 方法来去测量子视图的大小：
```
    /**
     * Ask one of the children of this view to measure itself, taking into
     * account both the MeasureSpec requirements for this view and its padding.
     * The heavy lifting is done in getChildMeasureSpec.
     *
     * @param child The child to measure
     * @param parentWidthMeasureSpec The width requirements for this view
     * @param parentHeightMeasureSpec The height requirements for this view
     */
    protected void measureChild(View child, int parentWidthMeasureSpec,
            int parentHeightMeasureSpec) {
        final LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                mPaddingLeft + mPaddingRight, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                mPaddingTop + mPaddingBottom, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
```
　　调用 getChildMeasureSpec() 方法来计算子视图的 MeasureSpec，计算的依据就是布局文件中定义的 MATCH_PARENT、WRAP_CONTENT 等值，然后调用字数图的 measure() 方法，并把计算出的 MeasureSpec 传递进去。

　　onMeasure() 方法是可以重写的，也就是说，如果你不想使用系统默认的测量方式，可以按照自己的意愿进行定制。

　　需要注意的是，在 setMeasuredDimension() 方法调用之后，才能使用 getMeasuredWidth() 和 getMeasuredHeight() 来获取视图测量出的宽高，以此之前调用这两个方法得到的值都会是 0。

　　由此可见，视图大小的控制是由父视图、布局文件以及视图本身共同完成的，父视图会提供给子视图参考的大小，而开发人员可以在 XML 文件中指定视图的大小，然后视图本身会对最终的大小进行拍板。

#### onLayout()
　　measure 过程结束后，视图的大小就已经测量好了，接下来就是 layout 的过程了。正如其名字所描述的一样，这个方法是用于给视图进行布局的，也就是确定视图的位置。ViewRoot 的 performTraversals() 方法会在 measure 结束后继续执行，会调用 performLayout() 方法，在 performLayout() 方法中会调用 View 的 layout() 方法来执行此过程：
```
public final class ViewRootImpl implements ViewParent,
        View.AttachInfo.Callbacks, ThreadedRenderer.HardwareDrawCallbacks {
    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth,
            int desiredWindowHeight) {
		Trace.traceBegin(Trace.TRACE_TAG_VIEW, "layout");
        try {
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
			...
		}
		...
	}
}
```
　　layout() 方法接收四个参数，分别表达着左、上、右、下的坐标，当然这个坐标是相对于当前视图的父视图而言的。可以看到，这里把刚才测量出的宽度和高度传到了 layout() 方法中：
```
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {
    public void layout(int l, int t, int r, int b) {
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }

        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;

        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
            onLayout(changed, l, t, r, b);

            if (shouldDrawRoundScrollbar()) {
                if(mRoundScrollbarRenderer == null) {
                    mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
                }
            } else {
                mRoundScrollbarRenderer = null;
            }

            mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;

            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<OnLayoutChangeListener> listenersCopy =
                        (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; ++i) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }

        mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
        mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;
    }
}
```
　　在 layout() 方法中，首先会调用 setFrame() 方法来判断视图的大小是否发生过变化，以确定有没有必要对当前的视图进行重绘，同时还会在这里把传递过来的四个参数分别赋值给 mLeft、mTop、mRight 和 mBottom 这几个变量。接下来会调用 onLayout() 方法。
```
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }
}
```
　　onLayout 是一个空方法，因为 onLayout() 过程是为了确定视图在布局中所在的位置，而这个操作应该是由布局来完成的，即父视图决定字数图的显示位置。接着查看 ViewGroup 的 onLayout() 方法:
```
public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    @Override
    protected abstract void onLayout(boolean changed,
            int l, int t, int r, int b);
}
```
　　可以看到，ViewGroup 中的 onLayout() 方法竟然是一个抽象方法，这就意味着所有 ViewGrouup 的子类都必须重写这个方法。像 LinearLayout、RelativeLayout 和 FrameLayout 等布局，都是重写了这个方法，然后在内部按照各自的规则对子视图进行布局的。由于 LinearLayout 和 RelativeLayout 的布局规则都比较复杂，在这里通过分析 FrameLayout 的 onLayout() 方法来解析这部分的知识：
```
public class FrameLayout extends ViewGroup {
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false /* no force left gravity */);
    }

    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeftWithForeground();
        final int parentRight = right - left - getPaddingRightWithForeground();

        final int parentTop = getPaddingTopWithForeground();
        final int parentBottom = bottom - top - getPaddingBottomWithForeground();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                        lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                        lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }
}
```
　　在 onLayout() 方法中，对子视图进行循环处理，调用子视图的 layout() 方法来确定它在 FrameLayout 布局中的位置，传入的 childLeft、childTop、childLeft + width、childTop + height，分别代表着子视图在 FrameLayout 中左上右下四个点的坐标。其中，调用 childView.getMeasuredWidth() 和 childView.getMeasuredHeight() 方法得到的值就是在 onMeasure() 方法中测量出的宽和高。

　　在 onLayout() 过程结束后，就可以调用 geiWidth() 方法和 getHeight() 方法来虎丘视图的宽高了。

　　gieMeasureWidth() 和 getWidth() 方法的区别：首先 getMeasureWidth() 方法在 measure() 过程结束后就可以获取到了，而 getWidth() 方法要在 layout() 过程结束后才能获取到。另外，getMeasureWidth() 方法中的值是通过 setMeasuredDimension() 方法来进行设置的，而 getWidth() 方法中值则是通过视图右边的坐标减去左边的坐标计算出来的。

#### onDraw()
　　measure 和 layout 的过程都结束后，接下来就进入到 draw 的过程了。到这里才真正地开始对视图进行绘制。ViewRoot 中的代码会继续执行并创建出一个 Canvas 对象，然后调用 View 的 draw() 方法来执行具体的绘制工作:
```
public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {
    /**
     * Manually render this view (and all of its children) to the given Canvas.
     * The view must have already done a full layout before this function is
     * called.  When implementing a view, implement
     * {@link #onDraw(android.graphics.Canvas)} instead of overriding this method.
     * If you do need to override this method, call the superclass version.
     *
     * @param canvas The Canvas to which the View is rendered.
     */
    @CallSuper
    public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;

        /*
         * Draw traversal performs several drawing steps which must be executed
         * in the appropriate order:
         *
         *      1. Draw the background
         *      2. If necessary, save the canvas' layers to prepare for fading
         *      3. Draw view's content
         *      4. Draw children
         *      5. If necessary, draw the fading edges and restore layers
         *      6. Draw decorations (scrollbars for instance)
         * 绘制必须按照适当的顺序遍历执行几个绘图步骤：
         * 		1. 绘制背景
         * 		2. 如果有必要，保存画布层以准备褪色（不常用）
         * 		3. 绘制视图的内容
         * 		4. 绘制子视图
         * 		5. 如果有必要，绘制褪色边缘并恢复层（不常用）
         * 		6. 绘制装饰（例如滚动条）
         */

        // Step 1, draw the background, if needed
        int saveCount;

        if (!dirtyOpaque) {
            drawBackground(canvas);
        }

        // skip step 2 & 5 if possible (common case)
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
        if (!verticalEdges && !horizontalEdges) {
            // Step 3, draw the content
            if (!dirtyOpaque) onDraw(canvas);

            // Step 4, draw the children
            dispatchDraw(canvas);

            // Overlay is part of the content and draws beneath Foreground
            if (mOverlay != null && !mOverlay.isEmpty()) {
                mOverlay.getOverlayView().dispatchDraw(canvas);
            }

            // Step 6, draw decorations (foreground, scrollbars)
            onDrawForeground(canvas);

            // we're done...
            return;
        }

        /*
         * Here we do the full fledged routine...
         * (this is an uncommon case where speed matters less,
         * this is why we repeat some of the tests that have been
         * done above)
         */

        boolean drawTop = false;
        boolean drawBottom = false;
        boolean drawLeft = false;
        boolean drawRight = false;

        float topFadeStrength = 0.0f;
        float bottomFadeStrength = 0.0f;
        float leftFadeStrength = 0.0f;
        float rightFadeStrength = 0.0f;

        // Step 2, save the canvas' layers
        int paddingLeft = mPaddingLeft;

        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
        }

        int left = mScrollX + paddingLeft;
        int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
        int top = mScrollY + getFadeTop(offsetRequired);
        int bottom = top + getFadeHeight(offsetRequired);

        if (offsetRequired) {
            right += getRightPaddingOffset();
            bottom += getBottomPaddingOffset();
        }

        final ScrollabilityCache scrollabilityCache = mScrollCache;
        final float fadeHeight = scrollabilityCache.fadingEdgeLength;
        int length = (int) fadeHeight;

        // clip the fade length if top and bottom fades overlap
        // overlapping fades produce odd-looking artifacts
        if (verticalEdges && (top + length > bottom - length)) {
            length = (bottom - top) / 2;
        }

        // also clip horizontal fades if necessary
        if (horizontalEdges && (left + length > right - length)) {
            length = (right - left) / 2;
        }

        if (verticalEdges) {
            topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
            drawTop = topFadeStrength * fadeHeight > 1.0f;
            bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
            drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
        }

        if (horizontalEdges) {
            leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
            drawLeft = leftFadeStrength * fadeHeight > 1.0f;
            rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
            drawRight = rightFadeStrength * fadeHeight > 1.0f;
        }

        saveCount = canvas.getSaveCount();

        int solidColor = getSolidColor();
        if (solidColor == 0) {
            final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;

            if (drawTop) {
                canvas.saveLayer(left, top, right, top + length, null, flags);
            }

            if (drawBottom) {
                canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
            }

            if (drawLeft) {
                canvas.saveLayer(left, top, left + length, bottom, null, flags);
            }

            if (drawRight) {
                canvas.saveLayer(right - length, top, right, bottom, null, flags);
            }
        } else {
            scrollabilityCache.setFadeColor(solidColor);
        }

        // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);

        // Step 4, draw the children
        dispatchDraw(canvas);

        // Step 5, draw the fade effect and restore layers
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;

        if (drawTop) {
            matrix.setScale(1, fadeHeight * topFadeStrength);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, right, top + length, p);
        }

        if (drawBottom) {
            matrix.setScale(1, fadeHeight * bottomFadeStrength);
            matrix.postRotate(180);
            matrix.postTranslate(left, bottom);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, bottom - length, right, bottom, p);
        }

        if (drawLeft) {
            matrix.setScale(1, fadeHeight * leftFadeStrength);
            matrix.postRotate(-90);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, left + length, bottom, p);
        }

        if (drawRight) {
            matrix.setScale(1, fadeHeight * rightFadeStrength);
            matrix.postRotate(90);
            matrix.postTranslate(right, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(right - length, top, right, bottom, p);
        }

        canvas.restoreToCount(saveCount);

        // Overlay is part of the content and draws beneath Foreground
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }

        // Step 6, draw decorations (foreground, scrollbars)
        onDrawForeground(canvas);
    }
}
```
　　第一步的作用是对视图的背景进行绘制。调用了 drawBackground() 方法来绘制：
```
    /**
     * Draws the background onto the specified canvas.
     *
     * @param canvas Canvas on which to draw the background
     */
    private void drawBackground(Canvas canvas) {
        final Drawable background = mBackground;
        if (background == null) {
            return;
        }
		// 根据 layout 过程确定的视图位置来设置背景的绘制区域
        setBackgroundBounds();

        // Attempt to use a display list if requested.
        if (canvas.isHardwareAccelerated() && mAttachInfo != null
                && mAttachInfo.mHardwareRenderer != null) {
            mBackgroundRenderNode = getDrawableRenderNode(background, mBackgroundRenderNode);

            final RenderNode renderNode = mBackgroundRenderNode;
            if (renderNode != null && renderNode.isValid()) {
                setBackgroundRenderNodeProperties(renderNode);
                ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
                return;
            }
        }

        final int scrollX = mScrollX;
        final int scrollY = mScrollY;
        if ((scrollX | scrollY) == 0) {
            background.draw(canvas);
        } else {
            canvas.translate(scrollX, scrollY);
			//背景的绘制
            background.draw(canvas);
            canvas.translate(-scrollX, -scrollY);
        }
    }

	    /**
     * Sets the correct background bounds and rebuilds the outline, if needed.
     * <p/>
     * This is called by LayoutLib.
     */
    void setBackgroundBounds() {
        if (mBackgroundSizeChanged && mBackground != null) {
            mBackground.setBounds(0, 0, mRight - mLeft, mBottom - mTop);
            mBackgroundSizeChanged = false;
            rebuildOutline();
        }
    }
```
　　在 drawBackground() 方法中先将 mBackground 对象设置给了 background，然后调用 setBackgroundounds() 方法来设置 mBackground 的位置，最后调用了 background.draw() 方法绘制背景。而启动 mBackground 对象是在 XML 中通过 android:background 属性或者 setBackgroundColor() 、 setBackgroundResource() 方法设置的图片或者颜色。

　　第三步的作用是对视图的内容进行绘制。调用了 onDraw() 方法：
```
    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    }
```
　　而 onDraw() 是一个空方法，因为每个视图的内容部分肯定都是各不相同的，这部分的功能交给子类来实现是理所当然的。

　　第四步的作用是对当前视图的所有子视图进行绘制。调用了 dispatchDraw() 方法：
```
    /**
     * Called by draw to draw the child views. This may be overridden
     * by derived classes to gain control just before its children are drawn
     * (but after its own view has been drawn).
     * @param canvas the canvas on which to draw the view
     */
    protected void dispatchDraw(Canvas canvas) {

    }
```
　　dispatchDraw() 方法也是一个空方法，也是交由子类去实现，如果当前的视图没有子视图，那么就不需要进行绘制了。比如 TextView 继承 View，但是没有重写 dispatchDraw() 方法，它没有子视图，也就没有必要实现这个方法，而 ViewGroup 类就重写了 dispatchDraw() 方法，去实现子视图的绘制。

　　第六步的作用是对视图的滚动条进行绘制。任何一个视图都是有滚动条的，只是一般情况下都没有让它显示出来。

　　View 是不会绘制内容部分的，因此需要每个视图根据想要展示的内容来自行绘制。绘制的方式主要是借助 Canvas 这个类，它会作为参数传入到 onDraw() 方法中，供给每个视图使用。


## 参考文章
[Android视图绘制流程完全解析，带你一步步深入了解View(二)](https://blog.csdn.net/guolin_blog/article/details/16330267)

