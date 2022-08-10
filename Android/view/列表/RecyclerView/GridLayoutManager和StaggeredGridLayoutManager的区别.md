# GridLayoutManager和StaggeredGridLayoutManager的区别

　　GridLayoutManager和StaggeredGridLayoutManager的区别

　　先看下图，是 GridLayoutManager 的效果，item 的布局，宽高都是 wrap_content 



![img](https:////upload-images.jianshu.io/upload_images/4625080-3418cc3cbc92f98e.png?imageMogr2/auto-orient/strip|imageView2/2/w/310/format/webp)

　　然后看下 StaggeredGridLayoutManager 的效果图，和名字一样，错列的 gridLayout，人们常说的瀑布流。



![img](https:////upload-images.jianshu.io/upload_images/4625080-042b3b28c17f8eb5.png?imageMogr2/auto-orient/strip|imageView2/2/w/294/format/webp)

　　为了省事，下边用 GL 代表 GridLayoutManager，SGL 代表 StaggeredGridLayoutManager

　　另外下边的都按照垂直布局来说的。

# 首先分析 GL，它是继承 LineaLayoutManager 的，也就是线性有的它都有

　　构造方法有 2 种，第一种默认是垂直方向，reverse 为 false 的。

```java
    public GridLayoutManager(Context context, int spanCount) {
        super(context);
        setSpanCount(spanCount);
    }
    public GridLayoutManager(Context context, int spanCount, int orientation,
            boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        setSpanCount(spanCount);
    }

  public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        mSpanSizeLookup = spanSizeLookup;
    }
```

　　另外它可以单独设置每个 item 的跨度 span，构造方法里返回的就是每行的总跨度，默认每个 item 占一个span。

　　如下代码，就是第 1 张图的效果，第一个和第二个元素，宽度直接就是 3，所以他们都各自占了一行。

```kotlin
 spanSizeLookup=object :GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        when(position){
                            0 or 1->{
                                return 3
                            }
                            else ->return 1;
                        }
                    }
                }
```

　　最后从效果图也能看出，GL 每行顶部是对齐的。

# SGL 分析

看效果图，会发现，下一行的元素位置，是按照上一行item的bottom哪个最小就先放在哪个下边的。所以啊会出现错位的情况的。

　　构造方法只有一个

```java
    public StaggeredGridLayoutManager(int spanCount, int orientation) {
        mOrientation = orientation;
        setSpanCount(spanCount);
        setAutoMeasureEnabled(mGapStrategy != GAP_HANDLING_NONE);
        mLayoutState = new LayoutState();
        createOrientationHelpers();
    }
```

　　这个布局管理器里有个方法,这个值只能是 0 或者 2，默认是 2.

```dart
 /**
     * Sets the gap handling strategy for StaggeredGridLayoutManager. If the gapStrategy parameter
     * is different than the current strategy, calling this method will trigger a layout request.
     *
     * @param gapStrategy The new gap handling strategy. Should be
     *                    {@link #GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS} or {@link
     *                    #GAP_HANDLING_NONE}.
     * @see #getGapStrategy()
     */
    public void setGapStrategy(int gapStrategy)
```

　　下边是它的默认值以及解释

```dart
    /**
     * When scroll state is changed to {@link RecyclerView#SCROLL_STATE_IDLE}, StaggeredGrid will
     * check if there are gaps in the because of full span items. If it finds, it will re-layout
     * and move items to correct positions with animations.
     * <p>
     * For example, if LayoutManager ends up with the following layout due to adapter changes:
     * <pre>
     * AAA
     * _BC
     * DDD
     * </pre>
     * <p>
     * It will animate to the following state:
     * <pre>
     * AAA
     * BC_
     * DDD
     * </pre>
     */
    public static final int GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS = 2;
```

　　这玩意好像是加载网络图片，比如我们滑动到底部，然后手指往下滑，他会自动滚动一段距离，停下来的时候会重新计算布局如果有空白它就进行动画移动。我个人感觉还可以接受吧。看网上都不能接受在想办法弄掉。

　　**分析下原因，图片高度都不一样，而布局是复用的。快速滚动的时候中间一堆item其实真实的高度并不准确，应该还是复用的item的高度，所以倒回去的时候最终的位置是不准确的，只有停下来的时候才开始计算最终显示的 item 的高度，这时候自然得对最终的位置进行修正了**.

　　那么如果要避免这种问题咋办？

　　我觉得让后台把图片的宽高比列告诉我们是最合理的。这样我们就可以提前在adapter里设定imageView的大小了。这样就不管图片有没有加载出来，它的高度都固定了，自然也就不会出现上边的问题拉。

 var lp=layoutParams;
 lp.height=图片宽*宽高比列  //图片宽就是本地的ImageView的宽，这个如果你列数设定了，这个根据屏幕宽是可以算出来的。

　　**下边说的是设置 ItemDecoration 的问题**

　　因为这玩意是瀑布流，本来就应该高度不一样的，如果你高度弄成固定的，再设置个 ItemDecoration，你就会发现问题了。

部分代码如下，用的是高度固定的item来测试，能明显看出效果

```kotlin
//布局
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    android:clickable="true"
    android:gravity="center"
    android:background="#215980"
    android:layout_width="wrap_content"
    android:layout_height="50dp">
<TextView/>
</LinearLayout>
class ItemDecorationSpace:RecyclerView.ItemDecoration{
    constructor() : super()
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left=5
        outRect.right=5
        outRect.bottom=20
    }
}

itemDecoration=ItemDecorationSpace()
addItemDecoration(itemDecoration)

rv_test.layoutManager=StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL).apply{
         
            }
```

　　效果图不帖了，简单说下结果。

　　就是我们 item 高度是 50，ItemDecoration 的 bottom 是20，最后你会发现你看到的item大小高度只有30.。

　　要知道如果是线性的或者 gridLayoutManager 的话，结果应该是 50+20 的。

　　所以这里需要注意，对于 SGL 来说，只有 item 的高度是 wrap_content 的时候，才应该设置 ItemDecoration。

# 参考文章

[GridLayoutManager和StaggeredGridLayoutManager的区别](https://www.jianshu.com/p/4f0b175f9a64)



