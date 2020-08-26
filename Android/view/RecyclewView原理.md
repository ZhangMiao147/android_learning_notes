# RecyclerView 原理

　　支持RecyclerView高效运行的主要六大类：

1. **Adapter**：为每一项Item创建视图
2. **ViewHolder**：承载Item视图的子布局
3. **LayoutManager**：负责Item视图的布局的显示管理
4. **ItemDecoration**：给每一项Item视图添加子View，例如可以进行画分隔线之类
5. **ItemAnimator**：负责处理数据添加或者删除时候的动画效果
6. **Recycler**：负责RecyclerView中子View的回收与复用

## Adapter

适配器的作用都是类似的，用于提供每个 item 视图，并返回给RecyclerView 作为其子布局添加到内部。

但是，与ListView不同的是，ListView的适配器是直接返回一个View，将这个 View加入到ListView内部。而RecyclerView是返回一个ViewHolder并且不是直接将这个holder加入到视图内部，而是加入到一个缓存区域，在视图需要的时候去缓存区域找到holder再间接的找到holder包裹的View。

## ViewHolder

我们写ListView的时候一般也会自己实现一个ViewHolder，它会持有一个View，避免不必要的**findViewById()**，来提高效率。

我们使用RecyclerView的时候必须创建一个继承自RecyclerView.ViewHolder的类。这主要是因为RecyclerView内部的缓存结构并不是像ListView那样去缓存一个View，而是直接缓存一个ViewHolder，在ViewHolder的内部又持有了一个View。既然是缓存一个ViewHolder，那么当然就必须所有的ViewHolder 都继承同一个类才能做到了。

## LayoutManager

顾名思义，LayoutManager是布局的管理者。实际上，RecyclerView就是将 onMeasure()、onLayout()交给了LayoutManager去处理，因此如果给 RecyclerView设置不同的LayoutManager就可以达到不同的显示效果。

### onMeasure()

RecyclerView的onMeasure()最终都会调用：`mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);`，这里的**mLayout**就是**LayoutManager**，最终还是调用了RecyclerView自己的方法对布局进行了测量。

### onLayoutChildren()

RecyclerView的onLayout()中会调用：`mLayout.onLayoutChildren(mRecycler, mState);`，这个方法在LayoutManager中是空实现：

```java
        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }
```

所以LayoutManager的子类都应该实现**onLayoutChildren()**，这里就将layout()的工作交给了LayoutManager的实现类，来完成对子View的布局。

## ItemDecoration

ItemDecoration是为了显示每个item之间分隔样式的。它的本质实际上就是一个Drawable。当RecyclerView执行到onDraw()方法的时候，就会调用到他的 onDraw()，这时，如果你重写了这个方法，就相当于是直接在RecyclerView 上画了一个Drawable表现的东西。而最后，在他的内部还有一个叫getItemOffsets()的方法，从字面就可以理解，他是用来偏移每个item视图的。当我们在每个item视图之间强行插入绘画了一段 Drawable，那么如果再照着原本的逻辑去绘 item视图，就会覆盖掉Decoration了，所以需要getItemOffsets()这个方法，让每个item往后面偏移一点，不要覆盖到之前画上的分隔样式了。

## ItemAnimator

每一个item在特定情况下都会执行的动画。说是特定情况，其实就是在视图发生改变，我们手动调用notifyxxxx()的时候。通常这个时候我们会要传一个下标，那么从这个标记开始一直到结束，所有 item 视图都会被执行一次这个动画。

## Recycler

```java
    public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

        private final List<ViewHolder>
                mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

        private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
        int mViewCacheMax = DEFAULT_CACHE_SIZE;

        private RecycledViewPool mRecyclerPool;

        private ViewCacheExtension mViewCacheExtension;
        ……
    }
```

RecyclerView拥有**四级缓存**：

1. **屏幕内缓存** ：指在屏幕中显示的ViewHolder，这些ViewHolder会缓存在**mAttachedScrap**、**mChangedScrap**中 。
   - mChangedScrap 表示数据已经改变的 ViewHolder 列表。
   - mAttachedScrap 未与 RecyclerView 分离的 ViewHolder 列表。
2. **屏幕外缓存**：当列表滑动出了屏幕时，ViewHolder会被缓存在 **mCachedViews**，其大小由 mViewCacheMax 决定，默认 DEFAULT_CACHE_SIZE 为 2，可通过 Recyclerview.setItemViewCacheSize() 动态设置。
3. **自定义缓存**：可以自己实现 **ViewCacheExtension **类实现自定义缓存，可通过 Recyclerview.setViewCacheExtension() 设置。通常我们也不会去设置他，系统已经预先提供了两级缓存了，除非有特殊需求，比如要在调用系统的缓存池之前，返回一个特定的视图，才会用到他。
4. **缓存池** ：ViewHolder 首先会缓存在 mCachedViews 中，当超过了 2 个（默认为2），就会添加到 mRecyclerPool 中。mRecyclerPool 会根据 ViewType 把 ViewHolder 分别存储在不同的集合中，每个集合最多缓存 5 个 ViewHolder。

**缓存策略**：
在LayoutManager执行layoutChildren()中获取子View的时候，会调用RecyclerView的**getViewForPosition()**：

```java
        View getViewForPosition(int position, boolean dryRun) {
            if (position < 0 || position >= mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + position
                        + "(" + position + "). Item count:" + mState.getItemCount());
            }
            boolean fromScrap = false;
            ViewHolder holder = null;
            // 0) If there is a changed scrap, try to find from there
            if (mState.isPreLayout()) {
                // 从mChangedScrap找
                holder = getChangedScrapViewForPosition(position);
                fromScrap = holder != null;
            }
            // 1) Find from scrap by position
            if (holder == null) {
                // 通过position从mAttachedScrap找，找不到再通过position从mCachedViews找
                holder = getScrapViewForPosition(position, INVALID_TYPE, dryRun);
                if (holder != null) {
                    if (!validateViewHolderForOffsetPosition(holder)) {
                        // recycle this scrap
                        if (!dryRun) {
                            // we would like to recycle this but need to make sure it is not used by
                            // animation logic etc.
                            holder.addFlags(ViewHolder.FLAG_INVALID);
                            if (holder.isScrap()) {
                                removeDetachedView(holder.itemView, false);
                                holder.unScrap();
                            } else if (holder.wasReturnedFromScrap()) {
                                holder.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(holder);
                        }
                        holder = null;
                    } else {
                        fromScrap = true;
                    }
                }
            }
            if (holder == null) {
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item "
                            + "position " + position + "(offset:" + offsetPosition + ")."
                            + "state:" + mState.getItemCount());
                }

                final int type = mAdapter.getItemViewType(offsetPosition);
                // 2) Find from scrap via stable ids, if exists
                if (mAdapter.hasStableIds()) {
                    // 通过id从mAttachedScrap找，找不到再通过id从mCachedViews找
                    holder = getScrapViewForId(mAdapter.getItemId(offsetPosition), type, dryRun);
                    if (holder != null) {
                        // update position
                        holder.mPosition = offsetPosition;
                        fromScrap = true;
                    }
                }
                if (holder == null && mViewCacheExtension != null) {
                    // 从mViewCacheExtension找
                    // We are NOT sending the offsetPosition because LayoutManager does not
                    // know it.
                    final View view = mViewCacheExtension
                            .getViewForPositionAndType(this, position, type);
                    if (view != null) {
                        holder = getChildViewHolder(view);
                        if (holder == null) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view which does not have a ViewHolder");
                        } else if (holder.shouldIgnore()) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view that is ignored. You must call stopIgnoring before"
                                    + " returning this view.");
                        }
                    }
                }

                if (holder == null) { // fallback to recycler
                    // 从mRecyclerPool找
                    // try recycler.
                    // Head to the shared pool.
                    if (DEBUG) {
                        Log.d(TAG, "getViewForPosition(" + position + ") fetching from shared "
                                + "pool");
                    }
                    holder = getRecycledViewPool().getRecycledView(type);
                    if (holder != null) {
                        holder.resetInternal();
                        if (FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(holder);
                        }
                    }
                }
                if (holder == null) {
                    // 从缓存中找不到，创建新的ViewHolder
                    holder = mAdapter.createViewHolder(RecyclerView.this, type);
                    if (DEBUG) {
                        Log.d(TAG, "getViewForPosition created new ViewHolder");
                    }
                }
            }
            ……
        }
```

Recyclerview在获取ViewHolder时按四级缓存的顺序查找，如果没找到就创建。

通过了解RecyclerView的四级缓存，我们可以知道，RecyclerView最多可以缓存N（屏幕最多可显示的item数）+ 2 (屏幕外的缓存) + 5*M (M代表M个ViewType，缓存池的缓存)。

还需要注意的是，RecyclerViewPool可以被多个RecyclerView共享。



#### 预取功能(Prefetch)





这个功能是rv在版本25之后自带的，也就是说只要你使用了25或者之后版本的rv，那么就自带该功能，并且默认就是处理开启的状态，通过LinearLayoutManager的setItemPrefetchEnabled()我们可以手动控制该功能的开启关闭，但是一般情况下没必要也不推荐关闭该功能，预取功能的原理比较好理解，如图所示

![img](https:////upload-images.jianshu.io/upload_images/9809352-b2b11cc689b33683.png?imageMogr2/auto-orient/strip|imageView2/2/w/809/format/webp)

QQ截图1.png

我们都知道android是通过每16ms刷新一次页面来保证ui的流畅程度，现在android系统中刷新ui会通过cpu产生数据，然后交给gpu渲染的形式来完成，从上图可以看出当cpu完成数据处理交给gpu后就一直处于空闲状态，需要等待下一帧才会进行数据处理，而这空闲时间就被白白浪费了，如何才能压榨cpu的性能，让它一直处于忙碌状态，这就是rv的预取功能(Prefetch)要做的事情，rv会预取接下来可能要显示的item，在下一帧到来之前提前处理完数据，然后将得到的itemholder缓存起来，等到真正要使用的时候直接从缓存取出来即可

#### 预取代码理解

虽说预取是默认开启不需要我们开发者操心的事情，但是明白原理还是能加深该功能的理解。下面就说下自己在看预取源码时的一点理解。实现预取功能的一个关键类就是gapworker，可以直接在rv源码中找到该类



```undefined
GapWorker mGapWorker;
```

rv通过在ontouchevent中触发预取的判断逻辑，在手指执行move操作的代码末尾有这么段代码



```kotlin
case MotionEvent.ACTION_MOVE: {
               ......
                    if (mGapWorker != null && (dx != 0 || dy != 0)) {
                        mGapWorker.postFromTraversal(this, dx, dy);
                    }
                }
            } break;
```

通过每次move操作来判断是否预取下一个可能要显示的item数据，判断的依据就是通过传入的dx和dy得到手指接下来可能要移动的方向，如果dx或者dy的偏移量会导致下一个item要被显示出来则预取出来，但是并不是说预取下一个可能要显示的item一定都是成功的，其实每次rv取出要显示的一个item本质上就是取出一个viewholder，根据viewholder上关联的itemview来展示这个item。而取出viewholder最核心的方法就是



```java
tryGetViewHolderForPositionByDeadline(int position,boolean dryRun, long deadlineNs)
```

名字是不是有点长，在rv源码中你会时不时见到这种巨长的方法名，看方法的参数也能找到和预取有关的信息,deadlineNs的一般取值有两种，一种是为了兼容版本25之前没有预取机制的情况，兼容25之前的参数为



```java
    static final long FOREVER_NS = Long.MAX_VALUE;
```

，另一种就是实际的deadline数值，超过这个deadline则表示预取失败，这个其实也好理解，预取机制的主要目的就是提高rv整体滑动的流畅性，如果要预取的viewholder会造成下一帧显示卡顿强行预取的话那就有点本末倒置了。

关于预取成功的条件通过调用



```java
boolean willCreateInTime(int viewType, long approxCurrentNs, long deadlineNs) {
            long expectedDurationNs = getScrapDataForType(viewType).mCreateRunningAverageNs;
            return expectedDurationNs == 0 || (approxCurrentNs + expectedDurationNs < deadlineNs);
}
```

来进行判断，approxCurrentNs的值为



```csharp
long start = getNanoTime();
if (deadlineNs != FOREVER_NS
                            && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
         // abort - we have a deadline we can't meet
        return null;
}
```

而mCreateRunningAverageNs就是创建同type的holder的平均时间，感兴趣的可以去看下这个值如何得到，不难理解就不贴代码了。关于预取就说到这里，感兴趣的可以自己去看下其余代码的实现方式，可以说google对于rv还是相当重视的，煞费苦心提高rv的各种性能，据说最近推出的viewpager2控件就是通过rv来实现的，大有rv控件一统天下的感觉。

#### 四级缓存

rv设计中另一个提高滑动流畅性的东西就是这个四级缓存了，如果说预取是25版本外来的务工人员，那么这个四级缓存就是一个本地土著了，自rv出现以来就一直存在，相比较listview的2级缓存机制，rv的四级看起来是不是显得更加的高大上。借用一张示意图来看下rv的四级缓存



![img](https:////upload-images.jianshu.io/upload_images/9809352-9ce1395b5eb44a15.png?imageMogr2/auto-orient/strip|imageView2/2/w/796/format/webp)

QQ截图2.png

，rv中通过recycler来管理缓存机制，关于如何使用缓存可以在tryGetViewHolderForPositionByDeadline找到，没错又是这个方法，看来名字起的长存在感也会比较足。

tryGetViewHolderForPositionByDeadline依次会从各级缓存中去取viewholer，如果取到直接丢给rv来展示，如果取不到最终才会执行我们非常熟悉的oncreatviewholder和onbindview方法，一句话就把tryGetViewHolderForPositionByDeadline的功能给讲明白了，内部实现无非是如何从四级缓存中去取肯定有个优先级的顺序。可以先来看下recycler中关于这四级缓存的代码部分



```java
public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

        private ViewCacheExtension mViewCacheExtension;

        RecycledViewPool mRecyclerPool;
}
```

四级缓存的真面目可以在这看到，其中两个scrap就是第一级缓存，是recycler在获取viewholder时最先考虑的缓存，接下来的mCachedViews，mViewCacheExtension，mRecyclerPool分别对应2,3,4级缓存。

#### 各级缓存作用

scrap:
 rv之所以要将缓存分成这么多块肯定在功能上是有一定的区分的，它们分别对应不同的使用场景，scrap是用来保存被rv移除掉但最近又马上要使用的缓存，比如说rv中自带item的动画效果，本质上就是计算item的偏移量然后执行属性动画的过程，这中间可能就涉及到需要将动画之前的item保存下位置信息，动画后的item再保存下位置信息，然后利用这些位置数据生成相应的属性动画。如何保存这些viewholer呢，就需要使用到scrap了，因为这些viewholer数据上是没有改变的，只是位置改变而已，所以放置到scrap最为合适。稍微仔细看的话就能发现scrap缓存有两个成员mChangedScrap和mAttachedScrap，它们保存的对象有些不一样，一般调用adapter的notifyItemRangeChanged被移除的viewholder会保存到mChangedScrap，其余的notify系列方法(不包括notifyDataSetChanged)移除的viewholder会被保存到mAttachedScrap中。

cached:
 也是rv中非常重要的一个缓存，就linearlayoutmanager来说cached缓存默认大小为2，它的容量非常小，所起到的作用就是rv滑动时刚被移出屏幕的viewholer的收容所，因为rv会认为刚被移出屏幕的viewholder可能接下来马上就会使用到，所以不会立即设置为无效viewholer，会将它们保存到cached中，但又不能将所有移除屏幕的viewholder都视为有效viewholer，所以它的默认容量只有2个，当然我们可以通过



```cpp
public void setViewCacheSize(int viewCount) {
            mRequestedCacheMax = viewCount;
            updateViewCacheSize();
}
```

来改变这个容量大小，这个就看实际应用场景了。

extension:
 第三级缓存，这是一个自定义的缓存，没错rv是可以自定义缓存行为的，在这里你可以决定缓存的保存逻辑，但是这么个自定义缓存一般都没有见过具体的使用场景，而且自定义缓存需要你对rv中的源码非常熟悉才行，否则在rv执行item动画，或者执行notify的一系列方法后你的自定义缓存是否还能有效就是一个值得考虑的问题，所以一般不太推荐使用该缓存，更多的我觉得这可能是google自已留着方便扩展来使用的，目前来说这还只是个空实现而已，从这点来看其实rv所说的四级缓存本质上还只是三级缓存。

pool：
 又一个重要的缓存，这也是唯一一个我们开发者可以方便设置的一个(虽然extension也能设置，但是难度大)，而且设置方式非常简单，new一个pool传进去就可以了，其他的都不用我们来处理，google已经给我们料理好后事了，这个缓存保存的对象就是那些无效的viewholer，虽说无效的viewholer上的数据是无效的，但是它的rootview还是可以拿来使用的，这也是为什么最早的listview有一个convertView参数的原因，当然这种机制也被rv很好的继承了下来。pool一般会和cached配合使用，这么来说，cached存不下的会被保存到pool中毕竟cached默认容量大小只有2，但是pool容量也是有限的当保存满之后再有viewholder到来的话就只能会无情抛弃掉，它也有一个默认的容量大小



```java
private static final int DEFAULT_MAX_SCRAP = 5;
int mMaxScrap = DEFAULT_MAX_SCRAP;
```

这个大小也是可以通过调用方法来改变，具体看应用场景，一般来说正常使用的话使用默认大小即可。

以上就是rv的四级缓存介绍，rv在设计之初就考虑到了这些问题，当然里面的一些细节还是比较多的，这个就需要感兴趣的自己去研究了，也正是因为google给我们考虑到这么多的优化这些才会显得rv的源码有些庞大，光一个rv差不多就1万3千多行，这还不包括layoutmanager的实现代码，这也是为什么很多人在遇到rv崩溃问题的时候会比较抓狂，根本原因还是在于没能好好研究过一些相关源码。

------

#### 我们可以做的

上面都在说rv中自带的一些优化技术，虽然google爸爸千方百计给我们提供好了很多可以给rv使用的优化api，但是这也架不住很多人不会使用啊，饭都到你嘴边了你自己都不会张嘴那就没人能帮你了，所以接下来就可以来说说我们在代码可以做哪些事情来充分发挥rv的性能。

#### 降低item的布局层次

其实这个优化不光适用于rv，activity的布局优化也同样适用，降低页面层次可以一定程度降低cpu渲染数据的时间成本，反应到rv中就是降低mCreateRunningAverageNs的时间，不光目前显示的页面能加快速度，预取的成功率也能提高，关于如何降低布局层次还是要推荐下google的强大控件ConstraintLayout，具体使用就自行百度吧，比较容易上手，这里吐槽下另一个控件CoordinatorLayout的上手难度确实是有点大啊，不了解CoordinatorLayout源码可能会遇到一些奇葩问题。降低item的布局层次可以说是rv优化中一个对于rv源码不需要了解也能完全掌握的有效方式。

#### 去除冗余的setitemclick事件

rv和listview一个比较大的不同之处在于rv居然没有提供setitemclicklistener方法，这是当初自己在使用rv时一个非常不理解的地方，其实现在也不是太理解，但是好在我们可以很方便的实现该功能，一种最简单的方式就是直接在onbindview方法中设置，这其实是一种不太可取的方式，onbindview在item进入屏幕的时候都会被调用到(cached缓存着的除外)，而一般情况下都会创建一个匿名内部类来实现setitemclick，这就会导致在rv快速滑动时创建很多对象，从这点考虑的话setitemclick应该放置到其他地方更为合适

自己的做法就是将setitemclick事件的绑定和viewholder对应的rootview进行绑定，viewholer由于缓存机制的存在它创建的个数是一定的，所以和它绑定的setitemclick对象也是一定的。还有另一种做法可以通过rv自带的addOnItemTouchListener来实现点击事件，原理就是rv在触摸事件中会使用到addOnItemTouchListener中设置的对象，然后配合GestureDetectorCompat实现点击item，示例代码如下：



```java
    recyclerView.addOnItemTouchListener(this);
    gestureDetectorCompat = new GestureDetectorCompat(recyclerView.getContext(), new SingleClick());

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (gestureDetectorCompat != null) {
            gestureDetectorCompat.onTouchEvent(e);
        }
        return false;
    }

   private class SingleClick extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view == null) {
                return false;
            }
            final RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (!(viewHolder instanceof ViewHolderForRecyclerView)) {
                return false;
            }
            final int position = getAdjustPosition(viewHolder);
            if (position == invalidPosition()) {
                return false;
            }
            /****************/
            点击事件设置可以考虑放在这里
            /****************/
            return true;
        }
    }
```

相对来说这是一个比较优雅点的实现，但是有一点局限在于这种点击只能设置整个item的点击，如果item内部有两个textview都需要实现点击的话就可能不太适用了，所以具体使用哪种看大家的实际应用场景，可以考虑将这两种方式都封装到adapter库中，目前项目中使用的adapter库就是采用两种结合的形式。

#### 复用pool缓存

四级缓存中我已经介绍过了，复用本身并不难，调用rv的setRecycledViewPool方法设置一个pool进去就可以，但是并不是说每次使用rv场景的情况下都需要设置一个pool，这个复用pool是针对item中包含rv的情况才适用，如果rv中的item都是普通的布局就不需要复用pool



![img](https:////upload-images.jianshu.io/upload_images/9809352-9c4e801ba8349366.png?imageMogr2/auto-orient/strip|imageView2/2/w/396/format/webp)

QQ截图3.png



如上图所示红框就是一个item中嵌套rv的例子，这种场景还是比较常见，如果有多个item都是这种类型那么复用pool就非常有必要了，在封装adapter库时需要考虑的一个点就是如何找到item中包含rv，可以考虑的做法就是遍历item的根布局如果找到包含rv的，那么将对该rv设置pool，所有item中的嵌套rv都使用同一个pool即可,查找item中rv代码可以如下



```php
private List<RecyclerView> findNestedRecyclerView(View rootView) {
        List<RecyclerView> list = new ArrayList<>();
        if (rootView instanceof RecyclerView) {
            list.add((RecyclerView) rootView);
            return list;
        }
        if (!(rootView instanceof ViewGroup)) {
            return list;
        }
        final ViewGroup parent = (ViewGroup) rootView;
        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            list.addAll(findNestedRecyclerView(child));
        }
        return list;
    }
```

得到该list之后接下来要做的就是给里面的rv绑定pool了，可以将该pool设置为adapter库中的成员变量，每次找到嵌套rv的item时直接将该pool设置给对应的rv即可。

关于使用pool源码上有一点需要在意的是，当最外层的rv滑动导致item被移除屏幕时，rv其实最终是通过调用
 removeview(view)完成的，里面的参数view就是和holder绑定的rootview，如果rootview中包含了rv，也就是上图所示的情况，会最终调用到嵌套rv的onDetachedFromWindow方法



```java
@Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }
```

注意里面的if分支，如果进入该分支里面的主要逻辑就是会清除掉scrap和cached缓存上的holder并将它们放置到pool中，但是默认情况下mRecycleChildrenOnDetach是为false的，这么设计的目的就在于放置到pool中的holder要想被拿来使用还必须调用onbindview来进行重新绑定数据，所以google默认将该参数设置为了false，这样即使rv会移除屏幕也不会使里面的holder失效，下次再次进入屏幕的时候就可以直接使用避免了onbindview的操作。

但是google还是提供了setRecycleChildrenOnDetach方法允许我们改变它的值，如果要想充分使用pool的功能，最好将其置为true，因为按照一般的用户习惯滑出屏幕的item一般不会回滚查看，这样接下来要被滑入的item如果存在rv的情况下就可以快速复用pool中的holder，这是使用pool复用的时候一个需要注意点的地方。

#### 保存嵌套rv的滑动状态

原来开发的时候产品就提出过这种需求，需要将滑动位置进行保存，否则每次位置被重置开起来非常奇怪，具体是个什么问题呢，还是以上图嵌套rv为例，红框中的rv可以看出来是滑动到中间位置的，如果这时将该rv移出屏幕，然后再移动回屏幕会发生什么事情，这里要分两种情况，一种是移出屏幕一点后就直接重新移回屏幕，另一种是移出屏幕一段距离再移回来，你会发现一个比较神奇的事就是移出一点回来的rv会保留原先的滑动状态，而移出一大段距离后回来的rv会丢失掉原先的滑动状态，造成这个原因的本质是在于rv的缓存机制，简单来说就是刚滑动屏幕的会被放到cache中而滑出一段距离的会被放到pool中，而从pool中取出的holder会重新进行数据绑定，没有保存滑动状态的话rv就会被重置掉，那么如何才能做到即使放在pool中的holder也能保存滑动状态。

其实这个问题google也替我们考虑到了，linearlayoutmanager中有对应的onSaveInstanceState和onRestoreInstanceState方法来分别处理保存状态和恢复状态，它的机制其实和activity的状态恢复非常类似，我们需要做的就是当rv被移除屏幕调用onSaveInstanceState，移回来时调用onRestoreInstanceState即可。

需要注意点的是onRestoreInstanceState需要传入一个参数parcelable，这个是onSaveInstanceState提供给我们的，parcelable里面就保存了当前的滑动位置信息，如果自己在封装adapter库的时候就需要将这个parcelable保存起来



```cpp
    private Map<Integer, SparseArrayCompat<Parcelable>> states;
```

map中的key为item对应的position，考虑到一个item中可能嵌套多个rv所以value为SparseArrayCompat，最终的效果



![img](https:////upload-images.jianshu.io/upload_images/9809352-ead9b28a3230cfb6.gif?imageMogr2/auto-orient/strip|imageView2/2/w/360/format/webp)

demo.gif



可以看到几个rv在被移出屏幕后再移回来能够正确保存滑动的位置信息，并且在删除其中一个item后states中的信息也能得到同步的更新，更新的实现就是利用rv的registerAdapterDataObserver方法，在adapter调用完notify系列方法后会在对应的回调中响应，对于map的更新操作可以放置到这些回调中进行处理。

#### 视情况设置itemanimator动画

使用过listview的都知道listview是没有item改变动画效果的，而rv默认就是支持动画效果的，之前说过rv内部源码有1万多行，其实除了rv内部做了大量优化之外，为了支持item的动画效果google也没少下苦功夫，也正是因为这样才使得rv源码看起来非常复杂。默认在开启item动画的情况下会使rv额外处理很多的逻辑判断，notify的增删改操作都会对应相应的item动画效果，所以如果你的应用不需要这些动画效果的话可以直接关闭掉，这样可以在处理增删改操作时大大简化rv的内部逻辑处理，关闭的方法直接调用setItemAnimator(null)即可。

#### diffutil一个神奇的工具类

diffutil是配合rv进行差异化比较的工具类，通过对比前后两个data数据集合，diffutil会自动给出一系列的notify操作，避免我们手动调用notifiy的繁琐，看一个简单的使用示例



```csharp
        data = new ArrayList<>();
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello1"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello2"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello3"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello4"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello5"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello6"));
        data.add(new MultiTypeItem(R.layout.testlayout1, "hello7"));

       newData = new ArrayList<>();
        //改
        newData.add(new MultiTypeItem(R.layout.testlayout1, "new one"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello2"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello3"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello4"));
        //增
        newData.add(new MultiTypeItem(R.layout.testlayout1, "add one"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello5"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello6"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello7"));
```

先准备两个数据集合分别代表原数据集和最新的数据集,然后实现下Callback接口



```java
private class DiffCallBack extends DiffUtil.Callback {

        @Override
        public int getOldListSize() {
            return data.size();
        }

        @Override
        public int getNewListSize() {
            return newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return data.get(oldItemPosition).getType() == newData.get(newItemPosition).getType();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            String oldStr = (String) DiffUtilDemoActivity.this.data.get(oldItemPosition).getData();
            String newStr = (String) DiffUtilDemoActivity.this.newData.get(newItemPosition).getData();
            return oldStr.equals(newStr);
        }
    }
```

实现的方法比较容易看懂，diffutil之所以能判断两个数据集的差距就是通过调用上述方法实现，areItemsTheSame表示的就是两个数据集对应position上的itemtype是否一样，areContentsTheSame就是比较在itemtype一致的情况下item中内容是否相同，可以理解成是否需要对item进行局部刷新。实现完callback之后接下来就是如何调用了。



```cpp
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(), true);
        diffResult.dispatchUpdatesTo(adapter);
        adapter.setData(newData);
```

上述就是diffutil一个简单的代码范例，其实最开始的时候自己想将diffutil封装到adapter库，但实际在使用后发现了几个自认为的弊端，所以放弃使用该工具类，这也可能是自己没有完全掌握diffutil精髓所导致的吧，这里就直接说下我对diffutil使用的看法。

弊端一：
 看示例代码应该也能察觉到，要想使用diffutil必须准备两个数据集，这就是一个比较蛋疼的事情，原先我们只需要维护一个数据集就可以，现在就需要我们同时维护两个数据集，两个数据集都需要有一份自己的数据，如果只是简单将数据从一个集合copy到另一个集合是可能会导致问题的，会涉及到对象的深拷贝和浅拷贝问题，你必须保证两份数据集都有各自独立的内存，否则当你修改其中一个数据集可能会造成另一个数据集同时被修改掉的情况。

弊端二：
 为了实现callback接口必须实现四个方法，其中areContentsTheSame是最难实现的一个方法，因为这里涉及到对比同type的item内容是否一致，这就需要将该item对应的数据bean进行比较，怎么比较效率会高点，目前能想到的方法就是将bean转换成string通过调用equals方法进行比较，如果item的数据bean对应的成员变量很少如示例所示那倒还好，这也是网上很多推荐diffutil文章避开的问题。但是如果bean对应的成员很多，或者成员变量含有list，里面又包含各种对象元素，想想就知道areContentsTheSame很难去实现，为了引入一个diffutil额外增加这么多的逻辑判断有点得不偿失。

弊端三：
 diffutil看起来让人捉摸不透的item动画行为，以上面代码为例



```csharp
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello1"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello2"));
//        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello3"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello4"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello5"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello6"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello7"));
```

新的数据集和原有数据集唯一的不同点就在于中间删除了一条数据，按照原先我们对于rv的理解，执行的表现形式应该是hello3被删除掉，然后hello3下面的所有item整体上移才对，但在使用diffutil后你会发现并不是这样的，它的表现比较怪异会移除第一条数据，这种怪异的行为应该和diffutil内部复杂的算法有关。

基于上述几个弊端所以最终自己并没有在adapter库去使用diffutil，比较有意思的是之前在看关于diffutil文章的时候特意留言问过其中一个作者在实际开发中是否有使用过diffutil，得到的答案是并没有在实际项目使用过，所以对于一些工具类是否真的好用还需要实际项目来检验，当然上面所说的都只是我的理解，不排除有人能透彻理解diffutil活用它的开发者，只是我没有在网上找到这种文章。

#### setHasFixedSize

又是一个google提供给我们的方法，主要作用就是设置固定高度的rv，避免rv重复measure调用。这个方法可以配合rv的wrap_content属性来使用，比如一个垂直滚动的rv，它的height属性设置为wrap_content，最初的时候数据集data只有3条数据，全部展示出来也不能使rv撑满整个屏幕，如果这时我们通过调用notifyItemRangeInserted增加一条数据，在设置setHasFixedSize和没有设置setHasFixedSize你会发现rv的高度是不一样的，设置过setHasFixedSize属性的rv高度不会改变，而没有设置过则rv会重新measure它的高度，这是setHasFixedSize表现出来的外在形式，我们可以从代码层来找到其中的原因。

notifiy的一系列方法除了notifyDataSetChanged这种万金油的方式，还有一系列进行局部刷新的方法可供调用，而这些方法最终都会执行到一个方法



```cpp
void triggerUpdateProcessor() {
            if (POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
            } else {
                mAdapterUpdateDuringMeasure = true;
                requestLayout();
            }
        }
```

区别就在于当设置过setHasFixedSize会走if分支，而没有设置则进入到else分支，else分支直接会调用到requestLayout方法，该方法会导致视图树进行重新绘制，onmeasure，onlayout最终都会被执行到，结合这点再来看为什么rv的高度属性为wrap_content时会受到setHasFixedSize影响就很清楚了，根据上述源码可以得到一个优化的地方在于，当item嵌套了rv并且rv没有设置wrap_content属性时，我们可以对该rv设置setHasFixedSize，这么做的一个最大的好处就是嵌套的rv不会触发requestLayout，从而不会导致外层的rv进行重绘，关于这个优化应该很多人都不知道，网上一些介绍setHasFixedSize的文章也并没有提到这点。

------

上面介绍的这些方法都是自己在研究rv优化时自己总结的一些心得，文章到这里其实应该可以结束，但在看源码的过程中还发现了几个比较有意思的方法，现在分享出来

#### swapadapter

rv的setadapter大家都会使用，没什么好说的，但关于swapadapter可能就有些人不太知道了，这两个方法最大的不同之处就在于setadapter会直接清空rv上的所有缓存，而swapadapter会将rv上的holder保存到pool中，google提供swapadapter方法考虑到的一个应用场景应该是两个数据源有很大的相似部分的情况下，直接使用setadapter重置的话会导致原本可以被复用的holder全部被清空，而使用swapadapter来代替setadapter可以充分利用rv的缓存机制，可以说是一种更为明智的选择。

#### getAdapterPosition和getLayoutPosition

大部分情况下调用这两个方法得到的结果是一致的，都是为了获得holder对应的position位置，但getAdapterPosition获取位置更为及时，而getLayoutPosition会滞后到下一帧才能得到正确的position，如果你想及时得到holder对应的position信息建议使用前者。举个最简单的例子就是当调用完notifyItemRangeInserted在rv头部插入一个item后立即调用这两个方法获取下原先处于第一个位置的position就能立即看出区别，其实跟踪下
 getAdapterPosition的源码很快能发现原因



```java
public int applyPendingUpdatesToPosition(int position) {
        final int size = mPendingUpdates.size();
        for (int i = 0; i < size; i++) {
            UpdateOp op = mPendingUpdates.get(i);
            switch (op.cmd) {
                case UpdateOp.ADD:
                    if (op.positionStart <= position) {
                        position += op.itemCount;
                    }
                    break;
                case UpdateOp.REMOVE:
                    if (op.positionStart <= position) {
                        final int end = op.positionStart + op.itemCount;
                        if (end > position) {
                            return RecyclerView.NO_POSITION;
                        }
                        position -= op.itemCount;
                    }
                    break;
                case UpdateOp.MOVE:
                    if (op.positionStart == position) {
                        position = op.itemCount; //position end
                    } else {
                        if (op.positionStart < position) {
                            position -= 1;
                        }
                        if (op.itemCount <= position) {
                            position += 1;
                        }
                    }
                    break;
            }
        }
        return position;
    }
```

最终getAdapterPosition会进入到上述方法，在这个方法就能很清楚看出为什么getAdapterPosition总是能及时反应出position的正确位置。但是有一点需要注意的就是getAdapterPosition可能会返回-1



```ruby
if (viewHolder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID
                | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)
                || !viewHolder.isBound()) {
            return RecyclerView.NO_POSITION;
        }
```

这点需要特别留意，做好预防处理。

#### removeview和detachview

这两个方法在rv进行排布item的时候会遇到，removeview就是大家很常见的操作，但是detachview就不太常见了，其实removeview是一个更为彻底的移除view操作，内部是会调用到detachview的，并且会调用到我们很熟悉的ondetachfromwindow方法，而detachview是一个轻量级的操作，内部操作就是简单的将该view从父view中移除掉，rv内部调用detachview的场景就是对应被移除的view可能在近期还会被使用到所以采用轻量级的移除操作，removeview一般都预示着这个holder已经彻底从屏幕消失不可见了。

#### 总结

总算写完了，费了好大力气，写一篇技术文章真的很费时间，这样一直坚持了一年时间，每篇文章都是自己用心去写的，也是对自己之前研究过技术的一个总结，其实年前就已经想写这篇文章，但总是被各种事情耽搁，现在咬咬牙把它写完了。rv确实是一个比较复杂的控件，看源码最好的方式就是基于简单的应用场景切入，然后在此基础上尝试rv的各种方法，带着这些问题去分析源码往往会比干看更有动力。



# 参考文章

1. [RecyclerView原理分析](https://blog.csdn.net/sted_zxz/article/details/80781562)

2. [RecyclerView一些你可能需要知道的优化技术](https://www.jianshu.com/p/1d2213f303fc)

https://www.jianshu.com/p/eabb00c500ef

https://www.jianshu.com/p/a57608f2695f

http://www.apkbus.com/blog-705730-61960.html

https://zhuanlan.zhihu.com/p/165939600

https://www.jianshu.com/p/8c508b51a7b5

https://blog.csdn.net/qq_33275597/article/details/93849695



