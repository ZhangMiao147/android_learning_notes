# Android 的常见问题 4

[TOC]



# 1. 屏幕适配

　　屏幕适配

## 1.1. dp 直接适配

　　dp 指的是设备独立像素，以 dp 为尺寸单位的控件，在不同分辨率和尺寸的手机上代表了不同的真实像素，比如在分辨率较低的手机中，可能 1dp=1px，而在分辨率较高的手机中，可能1dp=2px，这样的话，一个 96*96dp 的控件，在不同的手机中就能表现出差不多的大小了。那么这个 dp 是如何计算的呢？ 一个公式： px = dp(dpi/160) 系统都是通过这个来判断 px 和 dp 的数学关系，

dpi是像素密度，指的是在**系统软件上指定**的单位尺寸的像素数量，它往往是写在系统出厂配置文件的一个固定值。

ppi的参数，这个在手机屏幕中指的也是像素密度，但是这个是物理上的概念，它是客观存在的不会改变。dpi是软件参考了物理像素密度后，人为指定的一个值，这样保证了某一个区间内的物理像素密度在软件上都使用同一个值。这样会有利于我们的UI适配。

通过dp加上自适应布局和weight比例布局可以基本解决不同手机上适配的问题，这基本是最原始的Android适配方案。

这种方式存在两个小问题，第一，这只能保证我们写出来的界面适配绝大部分手机，部分手机仍然需要单独适配，为什么dp只解决了90%的适配问题，因为并不是所有的1080P的手机dpi都是480，比如Google 的Pixel2（1920*1080）的dpi是420，也就是说，在Pixel2中，1dp=2.625px,这样会导致相同分辨率的手机中，这样，一个100dp*100dp的控件，在一般的1080P手机上，可能都是300px,而Pixel 2 中 ，就只有262.5px,这样控件的实际大小会有所不同。

第二个问题，这种方式无法快速高效的把设计师的设计稿实现到布局代码中，通过dp直接适配，我们只能让UI基本适配不同的手机,但是在设计图和UI代码之间的鸿沟，dp是无法解决的，因为dp不是真实像素。而且，设计稿的宽高往往和Android的手机真实宽高差别极大，以我们的设计稿为例，设计稿的宽高是375px*750px，而真实手机可能普遍是1080*1920,

那么在日常开发中我们是怎么跨过这个鸿沟的呢？基本都是通过百分比啊，或者通过估算，或者设定一个规范值等等。总之，当我们拿到设计稿的时候，设计稿的ImageView是128px*128px，当我们在编写layout文件的时候，却不能直接写成128dp*128dp。在把设计稿向UI代码转换的过程中，我们需要耗费相当的精力去转换尺寸，这会极大的降低我们的生产力，拉低开发效率。

## 1.2. 宽高限定符配置

为了高效的实现UI开发，出现了新的适配方案，把它称作**宽高限定符适配**。简单说，就是穷举市面上所有的Android手机的宽高像素值：

![img](https:////upload-images.jianshu.io/upload_images/689802-7ade63d364870c5c?imageMogr2/auto-orient/strip|imageView2/2/w/870)

设定一个基准的分辨率，其他分辨率都根据这个基准分辨率来计算，在不同的尺寸文件夹内部，根据该尺寸编写对应的dimens文件。

![img](https:////upload-images.jianshu.io/upload_images/689802-4f88182107be5a90?imageMogr2/auto-orient/strip|imageView2/2/w/964)

这个时候，如果UI设计界面使用的就是基准分辨率，那么就可以按照设计稿上的尺寸填写相对应的dimens引用了,而当APP运行在不同分辨率的手机中时，这些系统会根据这些dimens引用去该分辨率的文件夹下面寻找对应的值。这样基本解决了适配问题，而且极大的提升了UI开发的效率，

但是这个方案有一个致命的缺陷，那就是需要精准命中才能适配，比如1920x1080的手机就一定要找到1920x1080的限定符，否则就只能用统一的默认的dimens文件了。而使用默认的尺寸的话，UI就很可能变形，简单说，就是容错机制很差。

不过这个方案有一些团队用过，可以认为它是一个比较成熟有效的方案了。

## 1.3. smallestWidth适配

smallestWidth适配，或者叫sw限定符适配。指的是Android会识别**屏幕可用高度和宽度的最小尺寸**的dp值（其实就是手机的宽度值），然后根据识别到的结果去资源文件中寻找对应限定符的文件夹下的资源文件。

这种机制和上文提到的宽高限定符适配原理上是一样的，都是系统通过特定的规则来选择对应的文件。

举个例子，小米5的dpi是480,横向像素是1080px，根据px=dp(dpi/160)，横向的dp值是1080/(480/160),也就是360dp,系统就会去寻找是否存在value-sw360dp的文件夹以及对应的资源文件。

![img](https:////upload-images.jianshu.io/upload_images/689802-6db507f8665651a7?imageMogr2/auto-orient/strip|imageView2/2/w/494)

smallestWidth限定符适配和宽高限定符适配最大的区别在于，前者有很好的容错机制，如果没有value-sw360dp文件夹，系统会向下寻找，比如离360dp最近的只有value-sw350dp，那么Android就会选择value-sw350dp文件夹下面的资源文件。这个特性就完美的解决了上文提到的宽高限定符的容错问题。

这套方案是上述几种方案中最接近完美的方案。
首先，**从开发效率上，它不逊色于上述任意一种方案**。根据固定的放缩比例，基本可以按照UI设计的尺寸不假思索的填写对应的dimens引用。

比如values-sw360dp和values-sw400dp,

![img](https:////upload-images.jianshu.io/upload_images/689802-9fffc4b8d779edf5?imageMogr2/auto-orient/strip|imageView2/2/w/535)

![img](https:////upload-images.jianshu.io/upload_images/689802-5bbab1e5442e4966?imageMogr2/auto-orient/strip|imageView2/2/w/530)

当系统识别到手机的smallestWidth值时，就会自动去寻找和目标数据最近的资源文件的尺寸。

其次，从稳定性上，它也优于上述方案。原生的dp适配可能会碰到Pixel 2这种有些特别的手机需要单独适配，但是在smallestWidth适配中，通过计算Pixel 2手机的的smallestWidth的值是411，只需要生成一个values-sw411dp(或者取整生成values-sw410dp也没问题)就能解决问题。

smallestWidth的适配机制由系统保证，只需要针对这套规则生成对应的资源文件即可，不会出现什么难以解决的问题，也根本不会影响业务逻辑代码，而且只要生成的资源文件分布合理，，即使对应的smallestWidth值没有找到完全对应的资源文件，它也能向下兼容，寻找最接近的资源文件。

当然，smallestWidth适配方案有一个小问题，那就是它是在Android 3.2 以后引入的，Google的本意是用它来适配平板的布局文件（但是实际上显然用于diemns适配的效果更好），不过目前所有的项目应该最低支持版本应该都是4.0了（糗事百科这么老的项目最低都是4.0哦），所以，这问题其实也不重要了。

还有一个缺陷那就是多个dimens文件可能导致apk变大，这是事实，根据生成的dimens文件的覆盖范围和尺寸范围，apk可能会增大300kb-800kb左右。

## 1.4. 今日头条适配方案

[文章链接](https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA)

这也是相对比较完美的方案，这个方案的思路，它是通过修改density值，强行把所有不同尺寸分辨率的手机的宽度dp值改成一个统一的值，这样就解决了所有的适配问题。

比如，设计稿宽度是360px，那么开发这边就会把目标dp值设为360dp，在不同的设备中，动态修改density值，从而保证(手机像素宽度)px/density这个值始终是360dp,这样的话，就能保证UI在不同的设备上表现一致了。

这个方案侵入性很低，而且也没有涉及私有API，应该也是极不错的方案，暂时也想不到强行修改density是否会有其他影响，既然有今日头条的大厂在用，稳定性应当是有保证的。

但是根据我的观察，这套方案**对老项目是不太友好的**，因为修改了系统的density值之后，整个布局的实际尺寸都会发生改变，如果想要在老项目文件中使用，恐怕整个布局文件中的尺寸都可能要重新按照设计稿修改一遍才行。因此，如果你是在维护或者改造老项目，使用这套方案就要三思了。

```java
    private static float sNoncompatDensity;
    private static float sNonCompatScaledDensity;
    private static float targetDensity;

    public static void setCustomDensity(Activity activity, @NonNull final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        Log.d(TAG, "setCustomDensity appDisplayMetrics:" + appDisplayMetrics);
        LogUtil.d(TAG, "setCustomDensity: sNoncompatDensity=" + sNoncompatDensity + ",sNonCompatScaledDensity=" + sNonCompatScaledDensity);
        if (sNoncompatDensity == 0) {
            sNoncompatDensity = appDisplayMetrics.density;
            sNonCompatScaledDensity = appDisplayMetrics.scaledDensity;
            LogUtil.d(TAG, "setCustomDensity 1 sNonCompatScaledDensity:" + sNonCompatScaledDensity + ",sNoncompatDensity:" + sNoncompatDensity);
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sNonCompatScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        //获取异常或者系统转向问题，导致获取到平板宽<高
        if (appDisplayMetrics.widthPixels < appDisplayMetrics.heightPixels) {
            Log.d(TAG, "setCustomDensity: 宽高异常，需转换");
            targetDensity = (float) appDisplayMetrics.heightPixels / 1280; // 1280 是设计图的高度
        } else {
            targetDensity = (float) appDisplayMetrics.widthPixels / 1280;
            Log.d(TAG, "setCustomDensity: 宽高正常，无需转换");
        }
        final float targetScaledDensity = targetDensity * (sNonCompatScaledDensity / sNoncompatDensity);
        final int targetDensityDpi = (int) (160 * targetDensity);

        appDisplayMetrics.density = targetDensity; // 密度
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi; // dpi
        final DisplayMetrics activityDisplayMetrics;
        if (activity == null) {
            activityDisplayMetrics = application.getResources().getDisplayMetrics();
        } else {
            activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        }
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;

    }


    public static void setCustomDensity4Oriention(Activity activity, @NonNull final Application application) {
        sNoncompatDensity = 0.0f;
        setCustomDensity(activity, application);
    }
```

# 2. 单元测试

　　单元测试是应用程序测试策略中的基本测试，通过对代码进行单元测试，一方面可以轻松地验证单个单元的逻辑是否正确，另一方面在每次构建之后运行单元测试，可以快速捕获和修复因代码更改（重构、优化等）带来的回归问题。

## 2.1. 为什么要进行单元测试？

* 提高稳定性，能够明确地了解是否正确的完成开发；
* 快速反馈 bug ，跑一遍单元测试用例，定位 bug ；
* 在开发周期中尽早通过单元测试检查 bug ，最小化技术债，越往后可能修复 bug 的代价会越大，严重的情况下会影响项目进度；
* 为代码重构提供安全保障，在优化代码时不用担心回归问题，在重构后跑一遍测试用例，没通过说明重构可能是有问题的，更加易于维护。

## 2.2. 单元测试要测什么

* 列出想要测试覆盖的正常、异常情况，进行测试验证；
* 性能测试，例如某个算法的耗时等等。

## 2.3. 单元测试的分类

1. 本地测试（ Local tests）：只在本地机器 JVM 上运行，以最小化执行时间，这种单元测试不依赖于 Android 框架，或者即使有依赖，也很方便使用模拟框架来模拟依赖，以达到隔离 Android 依赖的目的，模拟框架如 google 推荐的 Mockito；
2. 仪器化测试（Instrumented tests）：在真机或模拟器上运行的单元测试，由于需要跑到设备上，比较慢，这些测试可以访问仪器（Android 系统）信息，比如被测应用程序的上下文，一般地，依赖不太方便通过模拟框架模拟时采用这种方式。

## 2.4. 测试代码存放的位置

```
app/src
   |-- androidTest/java（仪器化单元测试、 UI 测试）
   |-- main/java（业务代码）
   |-- test/java（本地单元测试）
```

## 2.5. 本地测试

　　本地测试（ Local tests）：只在本地机器 JVM 上运行，以最小化执行时间，这种单元测试不依赖于 Android 框架，或者即使有依赖，也很方便使用模拟框架来模拟依赖，以达到隔离 Android 依赖的目的，模拟框架如 google 推荐的 Mockito 。

　　本地测试比较适合一些工具类测试，不需要使用任何 Android 系统的东西，只适用于测试公共方法，比如字符处理，数据整理等这些方法。

　　如果是一些工具类方法的测试，如计算两数之和的方法，本地 JVM 虚拟机就能提供足够的运行环境，但如果要测试的单元依赖了 Android 框架，比如用到了 Android 中的 Context 类的一些方法，本地 JVM 将无法提供这样的环境，这时候模拟框架 Mockito 就派上用场了。

## 2.6. 仪器化测试

　　在某些情况下，虽然可以通过模拟的手段来隔离 Android 依赖，但代价很大，这种情况下可以考虑仪器化的单元测试，有助于减少编写和维护模拟代码所需的工作量。

　　仪器化测试是在真机或模拟器上运行的测试，它们可以利用 Android framework APIs 和 supporting APIs 。如果测试用例需要访问仪器（instrumentation）信息（如应用程序的 Context ），或者需要 Android 框架组件的真正实现（如 Parcelable 或 SharedPreferences 对象），那么应该创建仪器化单元测试，由于要跑到真机或模拟器上，所以会慢一些。

　　测试使用 SharedPreferences 的工具类，使用 SharedPreferences 需要访问 Context 类以及 SharedPreferences 的具体实现，采用模拟隔离的话代价会比较大，所以采用仪器化测试比较合适。

# 3. RecyclerView

## 3.1. 缓存

RecyclerView拥有**四级缓存**：

1. **屏幕内缓存** ：指在屏幕中显示的ViewHolder，这些ViewHolder会缓存在**mAttachedScrap**、**mChangedScrap**中 。
   - mChangedScrap 表示数据已经改变的 ViewHolder 列表。
   - mAttachedScrap 未与 RecyclerView 分离的 ViewHolder 列表。
2. **屏幕外缓存**：当列表滑动出了屏幕时，ViewHolder会被缓存在 **mCachedViews**，其大小由 mViewCacheMax 决定，默认 DEFAULT_CACHE_SIZE 为 2，可通过 Recyclerview.setItemViewCacheSize() 动态设置。
3. **自定义缓存**：可以自己实现 **ViewCacheExtension **类实现自定义缓存，可通过 Recyclerview.setViewCacheExtension() 设置。通常我们也不会去设置他，系统已经预先提供了两级缓存了，除非有特殊需求，比如要在调用系统的缓存池之前，返回一个特定的视图，才会用到他。
4. **缓存池** ：ViewHolder 首先会缓存在 mCachedViews 中，当超过了 2 个（默认为2），就会添加到 mRecyclerPool 中。mRecyclerPool 会根据 ViewType 把 ViewHolder 分别存储在不同的集合中，每个集合最多缓存 5 个 ViewHolder。

在 RecyclewView 的 onLaout() 中会去调用 LayoutManager 的 onLayoutChildren() 方法，在 onLayourChild() 方法中会调用 RecyclewView 的 getViewForPosition() 方法，这个方法用来获取给定位置的初始化 View。在 getViewForPosition() 方法中获取 View 的步骤是：

1. 从 mChangedScrap  缓存中获取 View，如果存在，则返回，如果不存在进行下一步。
2. 从 mChangedScrap 和 mCachedViews 缓存中获取 View，如果存在，则返回，如果不存在则进行下一步。
3. 从 ViewCacheExtension 自定义缓存中获取 View，如果存在，则返回，如果不存在则进行下一步。
4. 从 mRecyclerPool  缓存池中获取缓存，如果存在，则返回，如果不存在，则进行下一步。
5. 调用 adapter 的 createViewHolder() 方法新键 ViewHolder，Adapter 的 createViewHolder() 方法会调用抽象方法 onCreateViewHolder() 方法，在得到 adapter 之后，还会调用 Adapter 的 bindViewHolder() 方法，这个方法调用了抽象方法 onBindViewHolder() 方法。

RecyclerView最多可以缓存N（屏幕最多可显示的item数）+ 2 (屏幕外的缓存) + 5*M (M代表M个ViewType，缓存池的缓存)。

RecyclerViewPool可以被多个RecyclerView共享。

## 3.2. 优化

### 3.2.1. 预取功能（Prefetch）

RecyclerView 会预取接下来可能要显示的 item，在下一帧到来之前提前处理完数据，然后将得到的 itemHolder 缓存起来，等到真正要使用的时候直接从缓存取出来即可。

实现预取功能的一个关键类就是 gapworker。

RecyclerView 通过在 onTouchEvent 中触发预取的判断逻辑，通过每次 move 操作来判断是否预取下一个可能要显示的 item 数据，判断的依据就是通过传入的 dx 和 dy 得到手指接下来可能要移动的方向，如果 dx 或者 dy 的偏移量会导致下一个 item 要被显示出来则预取出来，但是并不是预取下一个可能要显示的 item 一定都是成功的，其实每次rv取出要显示的一个item本质上就是取出一个viewholder，根据viewholder上关联的itemview来展示这个item。

### 3.2.2. 四级缓存

rv设计中另一个提高滑动流畅性的东西就是这个四级缓存了。

rv中通过recycler来管理缓存机制，关于如何使用缓存可以在tryGetViewHolderForPositionByDeadline找到。

tryGetViewHolderForPositionByDeadline依次会从各级缓存中去取viewholer，如果取到直接丢给rv来展示，如果取不到最终才会执行我们非常熟悉的oncreatviewholder和onbindview方法。

```java
public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

        private ViewCacheExtension mViewCacheExtension;

        RecycledViewPool mRecyclerPool;
}
```

其中两个scrap就是第一级缓存，是recycler在获取viewholder时最先考虑的缓存，接下来的mCachedViews，mViewCacheExtension，mRecyclerPool分别对应2,3,4级缓存。

#### 3.2.2.1 各级缓存作用

scrap:
 rv之所以要将缓存分成这么多块肯定在功能上是有一定的区分的，它们分别对应不同的使用场景，scrap是用来保存被rv移除掉但最近又马上要使用的缓存，比如说rv中自带item的动画效果，本质上就是计算item的偏移量然后执行属性动画的过程，这中间可能就涉及到需要将动画之前的item保存下位置信息，动画后的item再保存下位置信息，然后利用这些位置数据生成相应的属性动画。如何保存这些viewholer呢，就需要使用到scrap了，因为这些viewholer数据上是没有改变的，只是位置改变而已，所以放置到scrap最为合适。稍微仔细看的话就能发现scrap缓存有两个成员mChangedScrap和mAttachedScrap，它们保存的对象有些不一样，一般调用adapter的notifyItemRangeChanged被移除的viewholder会保存到mChangedScrap，其余的notify系列方法(不包括notifyDataSetChanged)移除的viewholder会被保存到mAttachedScrap中。

cached:
 也是rv中非常重要的一个缓存，就linearlayoutmanager来说cached缓存默认大小为2，它的容量非常小，所起到的作用就是rv滑动时刚被移出屏幕的viewholer的收容所，因为rv会认为刚被移出屏幕的viewholder可能接下来马上就会使用到，所以不会立即设置为无效viewholer，会将它们保存到cached中，但又不能将所有移除屏幕的viewholder都视为有效viewholer，所以它的默认容量只有2个，当然我们可以通过 setViewCacheSize() 方法来改变这个容量大小，这个就看实际应用场景了。

extension:
 第三级缓存，这是一个自定义的缓存，没错rv是可以自定义缓存行为的，在这里你可以决定缓存的保存逻辑，但是这么个自定义缓存一般都没有见过具体的使用场景，而且自定义缓存需要你对rv中的源码非常熟悉才行，否则在rv执行item动画，或者执行notify的一系列方法后你的自定义缓存是否还能有效就是一个值得考虑的问题，所以一般不太推荐使用该缓存，更多的我觉得这可能是google自已留着方便扩展来使用的，目前来说这还只是个空实现而已，从这点来看其实rv所说的四级缓存本质上还只是三级缓存。

又一个重要的缓存，这也是唯一一个我们开发者可以方便设置的一个(虽然extension也能设置，但是难度大)，而且设置方式非常简单，new一个pool传进去就可以了，其他的都不用我们来处理，google已经给我们料理好后事了，这个缓存保存的对象就是那些无效的viewholer，虽说无效的viewholer上的数据是无效的，但是它的rootview还是可以拿来使用的，这也是为什么最早的listview有一个convertView参数的原因，当然这种机制也被rv很好的继承了下来。pool一般会和cached配合使用，这么来说，cached存不下的会被保存到pool中毕竟cached默认容量大小只有2，但是pool容量也是有限的当保存满之后再有viewholder到来的话就只能会无情抛弃掉，它也有一个默认的容量大小 5。

这个大小也是可以通过调用方法来改变，具体看应用场景，一般来说正常使用的话使用默认大小即可。

### 3.2.3. 可以做的 RecyclewView 的优化

#### 3.2.3.1. 降低 item 的布局层次

其实这个优化不光适用于rv，activity的布局优化也同样适用，降低页面层次可以一定程度降低cpu渲染数据的时间成本，反应到rv中就是降低mCreateRunningAverageNs的时间，不光目前显示的页面能加快速度，预取的成功率也能提高，关于如何降低布局层次还是要推荐下google的强大控件ConstraintLayout，具体使用就自行百度吧，比较容易上手，这里吐槽下另一个控件CoordinatorLayout的上手难度确实是有点大啊，不了解CoordinatorLayout源码可能会遇到一些奇葩问题。降低item的布局层次可以说是rv优化中一个对于rv源码不需要了解也能完全掌握的有效方式。

#### 3.2.3.2. 去除冗余的 setItemClick 事件

rv和listview一个比较大的不同之处在于rv居然没有提供setitemclicklistener方法，这是当初自己在使用rv时一个非常不理解的地方，其实现在也不是太理解，但是好在我们可以很方便的实现该功能，一种最简单的方式就是直接在onbindview方法中设置，这其实是一种不太可取的方式，onbindview在item进入屏幕的时候都会被调用到(cached缓存着的除外)，而一般情况下都会创建一个匿名内部类来实现setitemclick，这就会导致在rv快速滑动时创建很多对象，从这点考虑的话setitemclick应该放置到其他地方更为合适。

自己的做法就是将setitemclick事件的绑定和viewholder对应的rootview进行绑定，viewholer由于缓存机制的存在它创建的个数是一定的，所以和它绑定的setitemclick对象也是一定的。还有另一种做法可以通过rv自带的addOnItemTouchListener来实现点击事件，原理就是rv在触摸事件中会使用到addOnItemTouchListener中设置的对象，然后配合GestureDetectorCompat实现点击item。

相对来说这是一个比较优雅点的实现，但是有一点局限在于这种点击只能设置整个item的点击，如果item内部有两个textview都需要实现点击的话就可能不太适用了，所以具体使用哪种看大家的实际应用场景，可以考虑将这两种方式都封装到adapter库中，目前项目中使用的adapter库就是采用两种结合的形式。

#### 3.2.3.3. 复用 pool 缓存

并不是说每次使用rv场景的情况下都需要设置一个pool，这个复用pool是针对item中包含rv的情况才适用，如果rv中的item都是普通的布局就不需要复用pool。

#### 3.2.3.4. 保存嵌套 rv 的滑动状态

需求，需要将滑动位置进行保存，否则每次位置被重置开起来非常奇怪。

linearlayoutmanager中有对应的onSaveInstanceState和onRestoreInstanceState方法来分别处理保存状态和恢复状态，它的机制其实和activity的状态恢复非常类似，我们需要做的就是当rv被移除屏幕调用onSaveInstanceState，移回来时调用onRestoreInstanceState即可。



需要注意点的是onRestoreInstanceState需要传入一个参数parcelable，这个是onSaveInstanceState提供给我们的，parcelable里面就保存了当前的滑动位置信息，如果自己在封装adapter库的时候就需要将这个parcelable保存起来



```cpp
    private Map<Integer, SparseArrayCompat<Parcelable>> states;
```

map中的key为item对应的position，考虑到一个item中可能嵌套多个rv所以value为SparseArrayCompat。

在删除其中一个item后states中的信息能得到同步的更新，更新的实现就是利用rv的registerAdapterDataObserver方法，在adapter调用完notify系列方法后会在对应的回调中响应，对于map的更新操作可以放置到这些回调中进行处理。

#### 3.2.3.5. 视情况设置 itemanimator 动画

使用过listview的都知道listview是没有item改变动画效果的，而rv默认就是支持动画效果的，之前说过rv内部源码有1万多行，其实除了rv内部做了大量优化之外，为了支持item的动画效果google也没少下苦功夫，也正是因为这样才使得rv源码看起来非常复杂。默认在开启item动画的情况下会使rv额外处理很多的逻辑判断，notify的增删改操作都会对应相应的item动画效果，所以如果你的应用不需要这些动画效果的话可以直接关闭掉，这样可以在处理增删改操作时大大简化rv的内部逻辑处理，关闭的方法直接调用setItemAnimator(null)即可。

#### 3.2.3.6. diffutil 一个神奇的工具类

diffutil是配合rv进行差异化比较的工具类，通过对比前后两个data数据集合，diffutil会自动给出一系列的notify操作，避免我们手动调用notifiy的繁琐。

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

areItemsTheSame表示的就是两个数据集对应position上的itemtype是否一样，areContentsTheSame就是比较在itemtype一致的情况下item中内容是否相同，可以理解成是否需要对item进行局部刷新。实现完callback之后接下来就是如何调用了。



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

#### 3.2.3.7. setHasFixedSize

又是一个google提供给我们的方法，主要作用就是设置固定高度的rv，避免rv重复measure调用。这个方法可以配合rv的wrap_content属性来使用，比如一个垂直滚动的rv，它的height属性设置为wrap_content，最初的时候数据集data只有3条数据，全部展示出来也不能使rv撑满整个屏幕，如果这时我们通过调用notifyItemRangeInserted增加一条数据，在设置setHasFixedSize和没有设置setHasFixedSize你会发现rv的高度是不一样的，设置过setHasFixedSize属性的rv高度不会改变，而没有设置过则rv会重新measure它的高度，这是setHasFixedSize表现出来的外在形式。

可以得到一个优化的地方在于，当item嵌套了rv并且rv没有设置wrap_content属性时，我们可以对该rv设置setHasFixedSize，这么做的一个最大的好处就是嵌套的rv不会触发requestLayout，从而不会导致外层的rv进行重绘。

## 3.3. 其他

### 3.3.1. swapAdapter

rv的setadapter大家都会使用，没什么好说的，但关于swapadapter可能就有些人不太知道了，这两个方法最大的不同之处就在于setadapter会直接清空rv上的所有缓存，而swapadapter会将rv上的holder保存到pool中，google提供swapadapter方法考虑到的一个应用场景应该是两个数据源有很大的相似部分的情况下，直接使用setadapter重置的话会导致原本可以被复用的holder全部被清空，而使用swapadapter来代替setadapter可以充分利用rv的缓存机制，可以说是一种更为明智的选择。

### 3.3.2. getAdapterPosition和getLayoutPosition

大部分情况下调用这两个方法得到的结果是一致的，都是为了获得holder对应的position位置，但getAdapterPosition获取位置更为及时，而getLayoutPosition会滞后到下一帧才能得到正确的position，如果你想及时得到holder对应的position信息建议使用前者。

### 3.3.4. removeview和detachview

这两个方法在rv进行排布item的时候会遇到，removeview就是大家很常见的操作，但是detachview就不太常见了，其实removeview是一个更为彻底的移除view操作，内部是会调用到detachview的，并且会调用到我们很熟悉的ondetachfromwindow方法，而detachview是一个轻量级的操作，内部操作就是简单的将该view从父view中移除掉，rv内部调用detachview的场景就是对应被移除的view可能在近期还会被使用到所以采用轻量级的移除操作，removeview一般都预示着这个holder已经彻底从屏幕消失不可见了。



# 4. 介绍一下classloader的实现原理，讲解一下项目中热修复的实现原理？

　　classLoader 是遵循双亲委派原则的。当需要加载一个类时，并不是由该类的类加载器加载，而是交给类加载器的父类去加载，如果父类加载了就不用再去加载了，如果没有加载，才会由本类的类加载器加载。

　　双亲委派原则主要是为了防止类的重复加载和系统类被随意替换的风险。



# 5. 介绍一下Android资源文件R.id的生成规则？

　　资源文件会在编译过程中被打包进 APK 中（res 文件夹）或者被打包成独立的资源 APK 包（比如 framework-res.apk）。资源文件都会由 Android 的工具 （Android Asset Packing Tool）生成八位十六进制整数型。

　　例如：0x7F020003

　　前两位表示资源所属包类型：7F 表示资源属于应用 APK 包资源，01 表示资源属于系统包在资源。

　　中间两位代表资源 ID 对应的资源的类型，分别是：02：drawable，03：layout，04：values，05：xml，06：raw，07：color，08：menu。

　　最后四位表示的是资源在资源包该资源类型中的编号。

# 6. 介绍一下livedata的实现原理



# 7. LinearLayout 和 RelativeLayout 的源码比较，区别

https://www.jianshu.com/p/8a7d059da746

# 8. 模块化、工程化架构思想 

　　工程化是一个高层次的思想，而模块化是为工程化思想下相对较具体的开发方式，因此可以简单的认为模块化是工程化的表现形式。

　　将项目当作一项系统工程进行分析、组织和构建从而达到项目结构清晰、分工明确、团队配合默契、开发效率提高的目的。

　　一个模块就是一个实现特定功能的文件，有了模块就可以更方便的使用别人的代码，要用什么功能就加载什么模块。

　　模块化开发的好处：

1. 避免变量污染，命名冲突。
2. 提高代码复用率。
3. 提高维护性。
4. 依赖关系的管理。

　　层次划分后每一层内部之间应该是模块化的，每个模块封装了自己的功能，然后对外暴露了供外界调用的接口。

　　模块划分的依据就是职责边界是否明确、逻辑是否独立。划分模块后的代码是更容易伟华的，不需要深入细节，只需要和某一个模块耦合就可以了。

# 9. Dalvik和Art虚拟机区别

DVM (Dalvik VM)

https://www.jianshu.com/p/713d24fa9982 

JVM的作用是把平台无关的.class里面的字节码翻译成平台相关的机器码，来实现**跨平台**。DVM就是安卓中使用的虚拟机。

Dalvik允许多个实例，每一个实例作为一个独立的linux进程执行，可以防止一个程序的崩溃导致所有程序都崩溃。

区别：

1. jvm通过解码class文件来运行程序；dvm则是dex文件。

   dex文件是由多个class文件打包而成，一个dex文件方法数不能超过65535。

2. dvm基于寄存器架构（句柄引用），jvm基于栈架构（指针引用）

3. Dalvik可执行文件体积更小。

# 10. 多渠道打包如何实现(Flavor、Dimension应用)？从母包生出渠道包实现方法？渠道标识替换原理？ 

## 10.1. Flavor

https://www.jianshu.com/p/40ae577a5a40

Android新增Flavor与模块化

### Android Flavor

为了支持同一套代码能够生成多个版本的应用，Android提供flavor来实现该需求。

### Flavor使用

### 在模块化中新增Flavor

在单一模块的工程中，新增Flavor很简单，但是在多模块的工程中，可能会有诸多问题。根据已经了解的情况，模块的依赖关系可以是一个有向无环图（禁止环形依赖）。在架构图中，一般约定俗成地将被依赖的模块放到下层，依赖其他模块的模块放到上层。

经过实践，发现有如下规律

- 模块化的工程中，不要求所有模块都新增对应的Flavor。例如，对于某些模块，可以区分国内/海外不同版本，但是其他模块可以不区分。
- 被依赖的模块允许不区分flavor。例如A依赖B，允许A区分国内/海外版本，B不区分。
- 被依赖的模块再区分flavor时，依赖它的模块必须区分flavor。例如A依赖B，若B区分国内/海外版本，则A也需要区分。

