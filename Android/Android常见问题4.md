# Android 的常见问题 4

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



