# RecyclerView 优化技术

介绍下 RecyclerView 自带的系统优化。

## 预取功能(Prefetch)

这个功能是 rv 在版本 25 之后自带的，也就是说只要使用了 25 或者之后版本的 rv，那么就自带该功能，并且默认就是处理开启的状态，通过 LinearLayoutManager 的 setItemPrefetchEnabled() 可以手动控制该功能的开启关闭，但是一般情况下没必要也不推荐关闭该功能，预取功能的原理比较好理解，如图所示

![img](https:////upload-images.jianshu.io/upload_images/9809352-b2b11cc689b33683.png)

android 是通过每 16ms 刷新一次页面来保证 ui 的流畅程度，现在 android 系统中刷新 ui 会通过 cpu 产生数据，然后交给 gpu 渲染的形式来完成，从上图可以看出当 cpu 完成数据处理交给 gpu 后就一直处于空闲状态，需要等待下一帧才会进行数据处理，而这空闲时间就被白白浪费了，如何才能压榨 cpu 的性能，让它一直处于忙碌状态，这就是 rv 的预取功能(Prefetch)要做的事情，rv 会预取接下来可能要显示的 item，在下一帧到来之前提前处理完数据，然后将得到的 itemholder 缓存起来，等到真正要使用的时候直接从缓存取出来即可。

### 预取代码理解

虽说预取是默认开启不需要开发者操心的事情，但是明白原理还是能加深该功能的理解。实现预取功能的一个关键类就是 gapworker，可以直接在 rv 源码中找到该类：

```java
GapWorker mGapWorker;
```

rv 通过在 ontouchevent 中触发预取的判断逻辑，在手指执行 move 操作的代码末尾有这么段代码：

```java
case MotionEvent.ACTION_MOVE: {
      ......
         if (mGapWorker != null && (dx != 0 || dy != 0)) {
            mGapWorker.postFromTraversal(this, dx, dy);
         }
      }
} break;
```

通过每次 move 操作来判断是否预取下一个可能要显示的 item 数据，判断的依据就是通过传入的 dx 和 dy 得到手指接下来可能要移动的方向，如果 dx 或者 dy 的偏移量会导致下一个 item 要被显示出来则预取出来，但是并不是说预取下一个可能要显示的 item 一定都是成功的，其实每次 rv 取出要显示的一个 item 本质上就是取出一个 viewholder，根据 viewholder 上关联的 itemview 来展示这个 item。而取出 viewholder 最核心的方法就是：

```java
tryGetViewHolderForPositionByDeadline(int position,boolean dryRun, long deadlineNs)
```

名字是不是有点长，在 rv 源码中会时不时见到这种巨长的方法名，看方法的参数也能找到和预取有关的信息，deadlineNs 的一般取值有两种，一种是为了兼容版本 25 之前没有预取机制的情况，兼容 25 之前的参数为：

```java
    static final long FOREVER_NS = Long.MAX_VALUE;
```

另一种就是实际的 deadline 数值，超过这个 deadline 则表示预取失败，这个其实也好理解，预取机制的主要目的就是提高 rv 整体滑动的流畅性，如果要预取的 viewholder 会造成下一帧显示卡顿强行预取的话那就有点本末倒置了。

关于预取成功的条件通过调用

```java
boolean willCreateInTime(int viewType, long approxCurrentNs, long deadlineNs) {
    long expectedDurationNs = getScrapDataForType(viewType).mCreateRunningAverageNs;
    return expectedDurationNs == 0 || (approxCurrentNs + expectedDurationNs < deadlineNs);
}
```

来进行判断，approxCurrentNs 的值为

```csharp
long start = getNanoTime();
if (deadlineNs != FOREVER_NS
             && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
         // abort - we have a deadline we can't meet
        return null;
}
```

而 mCreateRunningAverageNs 就是创建同 type 的 holder 的平均时间。可以说 google 对于 rv 还是相当重视的，煞费苦心提高 rv 的各种性能，据说最近推出的 viewpager2 控件就是通过 rv 来实现的，大有 rv 控件一统天下的感觉。

## 四级缓存

rv 设计中另一个提高滑动流畅性的东西就是这个四级缓存了，如果说预取是 25 版本外来的务工人员，那么这个四级缓存就是一个本地土著了，自 rv 出现以来就一直存在，相比较 listview 的 2 级缓存机制，rv 的四级看起来是不是显得更加的高大上。借用一张示意图来看下rv的四级缓存：

![img](https:////upload-images.jianshu.io/upload_images/9809352-9ce1395b5eb44a15.png)

rv 中通过 recycler 来管理缓存机制，关于如何使用缓存可以在 tryGetViewHolderForPositionByDeadline 找到，没错又是这个方法，看来名字起的长存在感也会比较足。

tryGetViewHolderForPositionByDeadline 依次会从各级缓存中去取 viewholer，如果取到直接丢给 rv 来展示，如果取不到最终才会执行 oncreatviewholder 和 onbindview 方法，一句话就把 tryGetViewHolderForPositionByDeadline 的功能给讲明白了，内部实现无非是如何从四级缓存中去取肯定有个优先级的顺序。可以先来看下 recycler 中关于这四级缓存的代码部分：

```java
public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

        private ViewCacheExtension mViewCacheExtension;

        RecycledViewPool mRecyclerPool;
}
```

四级缓存的真面目可以在这看到，其中两个 scrap 就是第一级缓存，是 recycler 在获取 viewholder 时最先考虑的缓存，接下来的 mCachedViews，mViewCacheExtension，mRecyclerPool 分别对应 2、3、4 级缓存。

### 各级缓存作用

* scrap
   rv 之所以要将缓存分成这么多块肯定在功能上是有一定的区分的，它们分别对应不同的使用场景，scrap 是用来保存被 rv 移除掉但最近又马上要使用的缓存，比如说 rv 中自带 item 的动画效果，本质上就是计算 item 的偏移量然后执行属性动画的过程，这中间可能就涉及到需要将动画之前的 item 保存下位置信息，动画后的 item 再保存下位置信息，然后利用这些位置数据生成相应的属性动画。如何保存这些 viewholer 呢，就需要使用到 scrap 了，因为这些 viewholer 数据上是没有改变的，只是位置改变而已，所以放置到 scrap 最为合适。稍微仔细看的话就能发现 scrap 缓存有两个成员 mChangedScrap 和 mAttachedScrap，它们保存的对象有些不一样，一般调用 adapter 的 notifyItemRangeChanged 被移除的 viewholder 会保存到 mChangedScrap，其余的 notify 系列方法(不包括 notifyDataSetChanged ) 移除的 viewholder 会被保存到 mAttachedScrap 中。

* cached:
  也是 rv 中非常重要的一个缓存，就 linearlayoutmanager 来说 cached 缓存默认大小为 2，它的容量非常小，所起到的作用就是 rv 滑动时刚被移出屏幕的 viewholer 的收容所，因为 rv 会认为刚被移出屏幕的 viewholder 可能接下来马上就会使用到，所以不会立即设置为无效 viewholer，会将它们保存到 cached 中，但又不能将所有移除屏幕的 viewholder 都视为有效 viewholer，所以它的默认容量只有2个，当然可以通过

```cpp
public void setViewCacheSize(int viewCount) {
            mRequestedCacheMax = viewCount;
            updateViewCacheSize();
}
```

来改变这个容量大小，这个就看实际应用场景了。

* extension:
   第三级缓存，这是一个自定义的缓存，没错 rv 是可以自定义缓存行为的，在这里可以决定缓存的保存逻辑，但是这么个自定义缓存一般都没有见过具体的使用场景，而且自定义缓存需要对 rv 中的源码非常熟悉才行，否则在 rv 执行 item 动画，或者执行 notify 的一系列方法后自定义缓存是否还能有效就是一个值得考虑的问题，所以一般不太推荐使用该缓存，更多的觉得这可能是 google 自已留着方便扩展来使用的，目前来说这还只是个空实现而已，从这点来看其实 rv 所说的四级缓存本质上还只是三级缓存。

* pool：
   又一个重要的缓存，这也是唯一一个开发者可以方便设置的一个(虽然 extension 也能设置，但是难度大)，而且设置方式非常简单， new 一个 pool 传进去就可以了，其他的都不用处理，google 已经料理好后事了，这个缓存保存的对象就是那些无效的 viewholer，虽说无效的 viewholer 上的数据是无效的，但是它的 rootview 还是可以拿来使用的，这也是为什么最早的 listview 有一个 convertView 参数的原因，当然这种机制也被 rv 很好的继承了下来。pool 一般会和 cached 配合使用，这么来说，cached 存不下的会被保存到 pool 中毕竟 cached 默认容量大小只有 2，但是 pool 容量也是有限的当保存满之后再有 viewholder 到来的话就只能会无情抛弃掉，它也有一个默认的容量大小

```java
private static final int DEFAULT_MAX_SCRAP = 5;
int mMaxScrap = DEFAULT_MAX_SCRAP;
```

这个大小也是可以通过调用方法来改变，具体看应用场景，一般来说正常使用的话使用默认大小即可。

以上就是 rv 的四级缓存介绍，rv 在设计之初就考虑到了这些问题，当然里面的一些细节还是比较多的，这个就需要感兴趣的自己去研究了，也正是因为 google 考虑到这么多的优化这些才会显得 rv 的源码有些庞大，光一个 rv 差不多就 1 万 3 千多行，这还不包括 layoutmanager 的实现代码，这也是为什么很多人在遇到 rv 崩溃问题的时候会比较抓狂，根本原因还是在于没能好好研究过一些相关源码。

## 我们可以做的

上面都在说 rv 中自带的一些优化技术，虽然 google 爸爸千方百计提供好了很多可以给 rv 使用的优化 api，但是这也架不住很多人不会使用啊，所以接下来就可以来说说在代码可以做哪些事情来充分发挥 rv 的性能。

### 降低 item 的布局层次

其实这个优化不光适用于 rv，activity 的布局优化也同样适用，降低页面层次可以一定程度降低 cpu 渲染数据的时间成本，反应到 rv 中就是降低 mCreateRunningAverageNs 的时间，不光目前显示的页面能加快速度，预取的成功率也能提高，关于如何降低布局层次还是要推荐下 google 的强大控件 ConstraintLayout，比较容易上手，这里吐槽下另一个控件 CoordinatorLayout 的上手难度确实是有点大啊，不了解 CoordinatorLayout 源码可能会遇到一些奇葩问题。降低 item 的布局层次可以说是 rv 优化中一个对于 rv 源码不需要了解也能完全掌握的有效方式。

### 去除冗余的 setitemclick 事件

rv 和 listview 一个比较大的不同之处在于 rv 居然没有提供 setitemclicklistener 方法，这是当初自己在使用 rv 时一个非常不理解的地方，其实现在也不是太理解，但是好在可以很方便的实现该功能，一种最简单的方式就是直接在 onbindview 方法中设置，这其实是一种不太可取的方式，onbindview 在 item 进入屏幕的时候都会被调用到 ( cached 缓存着的除外)，而一般情况下都会创建一个匿名内部类来实现 setitemclick，这就会导致在 rv 快速滑动时创建很多对象，从这点考虑的话 setitemclick 应该放置到其他地方更为合适。

自己的做法就是将 setitemclick 事件的绑定和 viewholder 对应的 rootview 进行绑定，viewholer 由于缓存机制的存在它创建的个数是一定的，所以和它绑定的 setitemclick 对象也是一定的。还有另一种做法可以通过 rv 自带的 addOnItemTouchListener 来实现点击事件，原理就是 rv 在触摸事件中会使用到 addOnItemTouchListener 中设置的对象，然后配合 GestureDetectorCompat 实现点击 item，示例代码如下：

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

相对来说这是一个比较优雅点的实现，但是有一点局限在于这种点击只能设置整个 item 的点击，如果 item 内部有两个 textview 都需要实现点击的话就可能不太适用了，所以具体使用哪种看大家的实际应用场景，可以考虑将这两种方式都封装到 adapter 库中，目前项目中使用的 adapter 库就是采用两种结合的形式。

### 复用pool缓存

四级缓存中已经介绍过了，复用本身并不难，调用 rv 的 setRecycledViewPool 方法设置一个 pool 进去就可以，但是并不是说每次使用 rv 场景的情况下都需要设置一个 pool，这个复用 pool 是针对 item 中包含 rv 的情况才适用，如果 rv 中的 item 都是普通的布局就不需要复用 pool。

![img](https:////upload-images.jianshu.io/upload_images/9809352-9c4e801ba8349366.png)

如上图所示红框就是一个 item 中嵌套 rv 的例子，这种场景还是比较常见，如果有多个 item 都是这种类型那么复用 pool 就非常有必要了，在封装 adapter 库时需要考虑的一个点就是如何找到 item 中包含 rv，可以考虑的做法就是遍历 item 的根布局如果找到包含 rv 的，那么将对该 rv 设置 pool，所有 item 中的嵌套 rv 都使用同一个 pool 即可，查找 item 中 rv 代码可以如下：

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

得到该 list 之后接下来要做的就是给里面的 rv 绑定 pool 了，可以将该 pool 设置为 adapter 库中的成员变量，每次找到嵌套 rv 的 item 时直接将该 pool 设置给对应的 rv 即可。

关于使用 pool 源码上有一点需要在意的是，当最外层的 rv 滑动导致 item 被移除屏幕时，rv 其实最终是通过调用
  removeview(view) 完成的，里面的参数 view 就是和 holder 绑定的 rootview，如果 rootview 中包含了 rv，也就是上图所示的情况，会最终调用到嵌套 rv 的 onDetachedFromWindow 方法

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

注意里面的 if 分支，如果进入该分支里面的主要逻辑就是会清除掉 scrap 和 cached 缓存上的 holder 并将它们放置到 pool 中，但是默认情况下 mRecycleChildrenOnDetach 是为 false 的，这么设计的目的就在于放置到 pool 中的 holder 要想被拿来使用还必须调用 onbindview 来进行重新绑定数据，所以 google 默认将该参数设置为了 false，这样即使 rv 会移除屏幕也不会使里面的 holder 失效，下次再次进入屏幕的时候就可以直接使用避免了 onbindview 的操作。

但是 google 还是提供了 setRecycleChildrenOnDetach 方法允许改变它的值，如果要想充分使用 pool 的功能，最好将其置为 true，因为按照一般的用户习惯滑出屏幕的 item一般不会回滚查看，这样接下来要被滑入的 item 如果存在 rv 的情况下就可以快速复用 pool 中的 holder，这是使用 pool 复用的时候一个需要注意点的地方。

### 保存嵌套 rv 的滑动状态

原来开发的时候产品就提出过这种需求，需要将滑动位置进行保存，否则每次位置被重置开起来非常奇怪，具体是个什么问题呢，还是以上图嵌套 rv 为例，红框中的 rv 可以看出来是滑动到中间位置的，如果这时将该 rv 移出屏幕，然后再移动回屏幕会发生什么事情，这里要分两种情况，一种是移出屏幕一点后就直接重新移回屏幕，另一种是移出屏幕一段距离再移回来，会发现一个比较神奇的事就是移出一点回来的 rv 会保留原先的滑动状态，而移出一大段距离后回来的 rv 会丢失掉原先的滑动状态，造成这个原因的本质是在于 rv 的缓存机制，简单来说就是刚滑动屏幕的会被放到 cache 中而滑出一段距离的会被放到 pool 中，而从 pool 中取出的 holder 会重新进行数据绑定，没有保存滑动状态的话 rv 就会被重置掉，那么如何才能做到即使放在 pool 中的 holder 也能保存滑动状态。

其实这个问题 google 也考虑到了，linearlayoutmanager 中有对应的 onSaveInstanceState 和 onRestoreInstanceState 方法来分别处理保存状态和恢复状态，它的机制其实和 activity 的状态恢复非常类似，需要做的就是当 rv 被移除屏幕调用 onSaveInstanceState，移回来时调用 onRestoreInstanceState 即可。

需要注意点的是 onRestoreInstanceState 需要传入一个参数 parcelable，这个是 onSaveInstanceState 提供的，parcelable 里面就保存了当前的滑动位置信息，如果自己在封装 adapter 库的时候就需要将这个 parcelable 保存起来：

```cpp
    private Map<Integer, SparseArrayCompat<Parcelable>> states;
```

map 中的 key 为 item 对应的 position，考虑到一个 item中可能嵌套多个 rv 所以 value 为 SparseArrayCompat，最终的效果：

![img](https:////upload-images.jianshu.io/upload_images/9809352-ead9b28a3230cfb6.gif)

可以看到几个 rv 在被移出屏幕后再移回来能够正确保存滑动的位置信息，并且在删除其中一个 item 后 states 中的信息也能得到同步的更新，更新的实现就是利用 rv 的  registerAdapterDataObserver 方法，在 adapter 调用完 notify 系列方法后会在对应的回调中响应，对于 map 的更新操作可以放置到这些回调中进行处理。

### 视情况设置itemanimator动画

使用过 listview 的都知道 listview 是没有 item 改变动画效果的，而 rv 默认就是支持动画效果的，之前说过 rv 内部源码有 1 万多行，其实除了 rv 内部做了大量优化之外，为了支持 item 的动画效果 google 也没少下苦功夫，也正是因为这样才使得 rv 源码看起来非常复杂。默认在开启 item 动画的情况下会使 rv 额外处理很多的逻辑判断，notify 的增删改操作都会对应相应的 item 动画效果，所以如果应用不需要这些动画效果的话可以直接关闭掉，这样可以在处理增删改操作时大大简化 rv 的内部逻辑处理，关闭的方法直接调用 setItemAnimator(null) 即可。

### diffutil 一个神奇的工具类

diffutil 是配合 rv 进行差异化比较的工具类，通过对比前后两个 data 数据集合，diffutil 会自动给出一系列的 notify 操作，避免手动调用 notifiy 的繁琐，看一个简单的使用示例：

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

先准备两个数据集合分别代表原数据集和最新的数据集，然后实现下 Callback 接口：

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

实现的方法比较容易看懂，diffutil 之所以能判断两个数据集的差距就是通过调用上述方法实现，areItemsTheSame 表示的就是两个数据集对应 position 上的 itemtype 是否一样，areContentsTheSame 就是比较在 itemtype 一致的情况下 item 中内容是否相同，可以理解成是否需要对 item 进行局部刷新。实现完 callback 之后接下来就是如何调用了。

```cpp
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(), true);
        diffResult.dispatchUpdatesTo(adapter);
        adapter.setData(newData);
```

上述就是 diffutil 一个简单的代码范例，其实最开始的时候自己想将 diffutil 封装到 adapter 库，但实际在使用后发现了几个自认为的弊端，所以放弃使用该工具类，这也可能是自己没有完全掌握 diffutil 精髓所导致的吧，这里就直接说下我对 diffutil 使用的看法。

弊端一：
 看示例代码应该也能察觉到，要想使用 diffutil 必须准备两个数据集，这就是一个比较蛋疼的事情，原先只需要维护一个数据集就可以，现在就需要同时维护两个数据集，两个数据集都需要有一份自己的数据，如果只是简单将数据从一个集合 copy 到另一个集合是可能会导致问题的，会涉及到对象的深拷贝和浅拷贝问题，必须保证两份数据集都有各自独立的内存，否则当修改其中一个数据集可能会造成另一个数据集同时被修改掉的情况。

弊端二：
为了实现 callback 接口必须实现四个方法，其中 areContentsTheSame 是最难实现的一个方法，因为这里涉及到对比同 type 的 item 内容是否一致，这就需要将该 item 对应的数据 bean 进行比较，怎么比较效率会高点，目前能想到的方法就是将 bean 转换成 string 通过调用 equals 方法进行比较，如果 item 的数据 bean 对应的成员变量很少如示例所示那倒还好，这也是网上很多推荐 diffutil 文章避开的问题。但是如果 bean 对应的成员很多，或者成员变量含有 list，里面又包含各种对象元素，想想就知道 areContentsTheSame 很难去实现，为了引入一个 diffutil 额外增加这么多的逻辑判断有点得不偿失。

弊端三：
 diffutil 看起来让人捉摸不透的 item 动画行为，以上面代码为例：

```csharp
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello1"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello2"));
//        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello3"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello4"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello5"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello6"));
        newData.add(new MultiTypeItem(R.layout.testlayout1, "hello7"));
```

新的数据集和原有数据集唯一的不同点就在于中间删除了一条数据，按照原先对于 rv 的理解，执行的表现形式应该是 hello3 被删除掉，然后 hello3 下面的所有 item 整体上移才对，但在使用 diffutil 后会发现并不是这样的，它的表现比较怪异会移除第一条数据，这种怪异的行为应该和 diffutil 内部复杂的算法有关。

基于上述几个弊端所以最终自己并没有在 adapter 库去使用 diffutil，比较有意思的是之前在看关于 diffutil 文章的时候特意留言问过其中一个作者在实际开发中是否有使用过 diffutil，得到的答案是并没有在实际项目使用过，所以对于一些工具类是否真的好用还需要实际项目来检验，当然上面所说的都只是我的理解，不排除有人能透彻理解 diffutil 活用它的开发者，只是我没有在网上找到这种文章。

### setHasFixedSize

又是一个 google 提供的方法，主要作用就是设置固定高度的 rv，避免 rv 重复 measure 调用。这个方法可以配合 rv 的 wrap_content 属性来使用，比如一个垂直滚动的 rv，它的 height 属性设置为 wrap_content，最初的时候数据集 data 只有 3 条数据，全部展示出来也不能使 rv 撑满整个屏幕，如果这时通过调用 notifyItemRangeInserted 增加一条数据，在设置 setHasFixedSize 和没有设置 setHasFixedSize 会发现 rv 的高度是不一样的，设置过 setHasFixedSize 属性的 rv 高度不会改变，而没有设置过则 rv 会重新 measure 它的高度，这是 setHasFixedSize 表现出来的外在形式，可以从代码层来找到其中的原因。

notifiy 的一系列方法除了 notifyDataSetChanged 这种万金油的方式，还有一系列进行局部刷新的方法可供调用，而这些方法最终都会执行到一个方法：

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

区别就在于当设置过 setHasFixedSize 会走 if 分支，而没有设置则进入到 else 分支，else 分支直接会调用到 requestLayout 方法，该方法会导致视图树进行重新绘制，onmeasure、onlayout 最终都会被执行到，结合这点再来看为什么 rv 的高度属性为 wrap_content 时会受到 setHasFixedSize 影响就很清楚了，根据上述源码可以得到一个优化的地方在于，当 item 嵌套了 rv 并且 rv 没有设置 wrap_content 属性时，可以对该 rv 设置 setHasFixedSize，这么做的一个最大的好处就是嵌套的 rv 不会触发 requestLayout，从而不会导致外层的 rv 进行重绘，关于这个优化应该很多人都不知道，网上一些介绍setHasFixedSize的文章也并没有提到这点。

### swapadapter

rv 的 setadapter 大家都会使用，没什么好说的，但关于 swapadapter 可能就有些人不太知道了，这两个方法最大的不同之处就在于 setadapter 会直接清空 rv 上的所有缓存，而 swapadapter 会将 rv 上的 holder 保存到 pool 中，google 提供 swapadapter 方法考虑到的一个应用场景应该是两个数据源有很大的相似部分的情况下，直接使用 setadapter 重置的话会导致原本可以被复用的 holder 全部被清空，而使用 swapadapter 来代替 setadapter 可以充分利用 rv 的缓存机制，可以说是一种更为明智的选择。

### getAdapterPosition和getLayoutPosition

大部分情况下调用这两个方法得到的结果是一致的，都是为了获得 holder 对应的 position 位置，但 getAdapterPosition 获取位置更为及时，而 getLayoutPosition 会滞后到下一帧才能得到正确的 position，如果想及时得到 holder 对应的 position 信息建议使用前者。举个最简单的例子就是当调用完 notifyItemRangeInserted 在 rv 头部插入一个 item 后立即调用这两个方法获取下原先处于第一个位置的 position 就能立即看出区别，其实跟踪下 
 getAdapterPosition 的源码很快能发现原因：

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

最终 getAdapterPosition 会进入到上述方法，在这个方法就能很清楚看出为什么 getAdapterPosition 总是能及时反应出 position 的正确位置。但是有一点需要注意的就是 getAdapterPosition 可能会返回 -1。

```ruby
if (viewHolder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID
                | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)
                || !viewHolder.isBound()) {
            return RecyclerView.NO_POSITION;
        }
```

这点需要特别留意，做好预防处理。

### removeview 和 detachview

这两个方法在 rv 进行排布 item 的时候会遇到，removeview 就是大家很常见的操作，但是 detachview 就不太常见了，其实 removeview 是一个更为彻底的移除 view 操作，内部是会调用到 detachview 的，并且会调用到 ondetachfromwindow 方法，而 detachview 是一个轻量级的操作，内部操作就是简单的将该 view 从父 view 中移除掉，rv 内部调用 detachview 的场景就是对应被移除的 view 可能在近期还会被使用到所以采用轻量级的移除操作，removeview 一般都预示着这个 holder 已经彻底从屏幕消失不可见了。

# 参考文章

1. [RecyclerView一些你可能需要知道的优化技术](https://www.jianshu.com/p/1d2213f303fc)

   











