# 第 4 章 $AppClick 全埋点方案 1：View.OnClickListener

​		目前大概有 8 种常用的技术可以实现采集 $AppClick 事件。它们整体上可以分为 “动态方案” 和 “静态方案”。综合来说，“静态方案” 明显优于 “动态方案”，它不仅效率高，更容易扩展，而且兼容性也比较好。

## 4.1. 关键技术

**android.R.id.content**

​		android.R.id.content 对应的视图是一个 FrameLayout 布局，它目前只有一个子元素，就是我们平时开发的时候，在 onCreate 方法中通过 setContentView 设置的 View。换句说法就是，当我们在 layout 文件中设置一个布局文件时，实际上该布局会被一个 FrameLayout 容器所包含，这个 FrameLayout 容器的 android：id 属性值就是 android.R.id.content。

​		需要注意的是，在不同的 SDK 版本下， android.R.id.content 所指的显示区域有所不同。具体差异如下：

* SDK 14+(Native ActionBar)：该显示区域指的是 ActionBar 下面的那部分；
* Support Library Revision lower than 19：使用 AppCompat，则显示区域包含 ActionBar；
* Support Library Revision 19(or greater)：使用 AppCompat，则显示区域不包含 ActionBar，即与第一种情况相同。

​		所以，如果不使用 Support Library 或使用 Support Library 的最新版本，则 android.R.id.content 所指的区域都是 ActionBar 以下的内容。

​		关于这个差异的更详细信息可以参考如下链接：https://stackoverflow.com/questions/24712227/android-r-id-content-as-container-for-fragment

## 4.2. 原理概述

​		在 Application.ActivityLifecycleCallbacks 的onActivityResumed(Activity activity) 回调方法中，我们可以拿到当前正在显示的 Activity 实例，通过 activity.findViewById(android.R.id.content) 方法就可以拿到整个内容区域对应的 View（是一个 FrameLayout）。本书有可能会用 RootView、ViewTree 和根视图概念来混称这个 View。然后，埋点 SDK 再逐层遍历这个 RootView，并判断当前 View 是否设置了 mOnClickListener 对象，如果已设置 mOnClickListener 对象并且 mOnClickListener 又不是我们自定义的 WrapperOnClickListener 类型，则通过 WrapperOnClickListener 代理当前 View 设置的 mOnClickListener。WrapperOnClickListener 是我们自定义的一个类，它实现了 View.OnClickListener 接口，在 WrapperOnClickListener 的 onClick 方法里会先调用 View 的原有 mOnClickListener 处理逻辑，然后再调用埋点代码，即可实现 “插入” 埋点代码，从而达到自动埋点的效果。

## 4.3. 案例

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAppClick

## 4.4. 引入 DecorView

​		基于代理 View 的 mOnClickListener 对象的方案是无法采集 MenuItem 控件的点击事件的。

​		这又是什么原因呢？其实，这是因为我们通过 android.R.id.content 获取到的 RootView 是不包含 Activity 标题栏的，也就是不包括 MenuItem 的父容器，一开始介绍 android.R.id.content 时也提到过。所以，当我们去遍历 RootView 时是无法遍历到 MenuItem 控件的，因此也无法去代理其 mOnClickListener 对象，从而导致无法采集 MenuItem 的点击事件。

​		DecorView 是整个 Window 界面的最顶层的 View（下图中编号为 0 的 View）。DecorView 只有一个子元素为 LinearLayout（下图中编号1），代表整个 Window 界面，它包含通知栏、标题栏、内容显示栏三块区域。这个 LinearLayout 里含有两个 FrameLayout 子元素。第一个 FrameLayout（下图中编号 20）为标题栏显示界面。第二个 FrameLayout（下图中编号 21）为内容栏显示界面，就是上面所说的 android.R.id.content。

![DecorView结构](https://res.weread.qq.com/wrepub/epub_25123578_9)

## 4.5. 引入 ViewTreeObserver.OnGlobalLayoutListener

​		通过继续测试还可以发现，当前的方案还有一个问题，即：该方案无法采集 onResume() 生命周期之后动态创建的 View 点击事件。

​		是因为我们是在 Activity 的 onResume 生命周期之前去遍历整个 RootView 并代理其 mOnClickListener 对象的。如果是在 onResume 生命周期之后动态创建的 View，当时肯定是无法被遍历到的，后来我们又没有再次去遍历，所以它的 mOnClickListener 对象就没有被我们代理过。因此，点击控件时，是无法采集到其点击事件的。

​		什么是 ViewTreeObserver.OnGlobalLayoutListener？

​		OnGlobalLayoutListener 其实是 ViewTreeObserver 的一个内部接口。当一个视图树的布局发生改变时，如果我们给当前的 View 设置了 ViewTreeObserver.OnGlobalLayoutListener 监听器，就可以被 ViewTreeObserver.OnGlobalLayoutListener 监听器监听到（实际上是触发了它的 onGlobalLayout 回调方法）。

​		可以给当前 Activity 的 RootView 也添加一个 ViewTreeObserver.OnGlobalLayoutListener 监听器，当收到 onGlobalLayout 方法回调时（即视图树的布局发生变化，比如新的View被创建），我们重新去遍历一次 RootView，然后找到那些没有被代理过 mOnClickListener 对象的 View 并进行代理，即可解决上面提到的问题。

​		关于 ViewTreeObserver.OnGlobalLayoutListener 监听器，建议在页面退出的时候 remove 掉，即在 onStop 的时候调用 removeOnGlobalLayoutListener 方法。

​		由于该方案遍历的是 Activity 的 RootView，所以游离于 Activity 之上的 View 的点击是无法采集的，比如 Dialog、PopupWindow 等。　

## 4.6 扩展采集能力

### 4.6.1. 扩展 1：支持获取 TextView 的显示文本

​		点击 TextView 控件时，虽然可以正常触发点击事件，但却没有 $element_content 属性。其实原因很简单，在前面的示例中，我们仅仅获取了 Button 和 MenuItem 的显示文本信息。下面稍微修改一下获取显示文本信息的代码逻辑，就可以支持获取 TextView 的 $element_content 属性了。

### 4.6.2. 扩展 2：支持获取 ImageView 的显示文本信息

​		其实，ImageView 控件本身没有显示文本这个概念。当我们点击 ImageView 的时候又该如何获取它的显示文本信息 ($element_content) 呢？或者说，对于图片的点击，我们如何知道更多关于 ImageView 的信息呢？其实，可以通过 android：contentDescription 的属性值来代替显示文本。

​		什么是 android：contentDescription 属性？

​		该属性可以为视力有障碍的用户提供方便。当我们为一个控件设置 android：content-Description 属性后，如果用户设备的可访问性选项作了相应的设置，当用户点击相应的按钮时，设备会用语音读出属性值的内容。

### 4.6.3. 扩展 3：支持采集 CheckBox 的点击事件

​		CheckBox 设置的 listener 是CompoundButton.OnCheckedChangeListener，处理方式与代理 Button 的 mOnClickListener 对象类似。如果发现当前 View 是 CheckBox 类型或者其子类类型，首先通过反射获取它已经设置的 mOnCheckedChangeListener 对象，如果获取到的 mOnCheckedChangeListener 对象不为空并且又不是我们自定义的 Wrapper-OnCheckedChangeListener 类型，然后用 WrapperOnCheckedChangeListener 代理即可。

### 4.6.4. 扩展 4：支持采集 RadioGroup 的点击事件

​		RadioGroup 设置的 listener 是 RadioGroup.OnCheckedChangeListener 类型。我们只需要判断当前 View 属于 RadioGroup 类型，然后通过反射获取其已经设置的 mOnChecked-ChangeListener 对象，如果获取到的 mOnCheckedChangeListener 对象不为空，并且又不是我们自定义的 WrapperRadioGroupOnCheckedChangeListener 类型，则通过 WrapperRadioGroupOnCheckedChangeListener 代理即可。

### 4.6.5. 扩展 5：支持采集 RatingBar 的点击事件

​		RatingBar 设置的 listener 是 RatingBar.OnRatingBarChangeListener 类型。RatingBar 与前面讲到的 View 相比有一个好处就是，它已经有一个 getOnRatingBarChangeListener() 方法了，通过这个方法可以直接获取它已设置的 mOnRatingBarChangeListener 对象，无须通过反射获取。遍历 RootView 的时候，如果发现当前 View 是 RatingBar 类型，就通过 getOnRatingBarChangeListener() 方法获取它的 mOnRatingBarChangeListener 对象。如果 mOnRatingBarChangeListener 对象不为空，并且又不是我们自定义的 WrapperOnRating-BarChangeListener 类型，则去代理即可。

### 4.6.6. 扩展 6：支持采集 SeekBar 的点击事件

​		根据实际的业务分析需求，我们只需要关注  onStopTrackingTouch(SeekBar seekBar) 回调即可。SeekBar 的处理方案与 RatingBar 的处理方案非常类似，也是通过反射获取 SeekBar 已经设置的 mOnSeekBarChangeListener 对象，如果获取到的 mOnSeekBarChangeListener 对象不为空，并且又不是我们自定义的 WrapperOnSeekBarChangeListener 类型，则去代理即可，

### 4.6.7. 扩展 7：支持采集 Spinner 的点击事件

​		和 RatingBar 一样，Spinner 也有相应的获取其设置的 AdapterView.OnItemSelectedListener 的方法，即 getOnItemSelectedListener() 方法。如果返回的 AdapterView.OnItem-SelectedListener 对象不为空，并且又不是我们自定义的 WrapperAdapterViewOnItemSelecte-dListener 类型，则去代理即可。

### 4.6.8. 扩展 8：支持采集 ListView、GridView 的点击事件

​		与 Spinner 类似，ListView 和 GridView 也有一个 getOnItemClickListener() 方法用来获取其已设置的 AdapterView.OnItemClickListener 对象。如果返回的 AdapterView.OnItemClickListener 对象不为空，并且又不是我们自定义的 WrapperAdapterViewOnItemSelectedListener 类型，则去代理即可。

### 4.6.9. 扩展 9：支持采集 ExpandableListView 的点击事件

​		首先通过反射分别获取 mOnChildClickListener 对象和 mOnGroupClickListener 对象，如果获取的 listener 对象不为空，并且又不是我们自定义的类型，然后分别通过 OnChildClickListenerWrapper 和 WrapperOnGroupClickListener 代理。

### 4.6.10. 扩展 10：支持采集 Dialog 的点击事件

​		目前这种全埋点方案无法采集游离于 Activity 之上的 View 的点击事件，比如 Dialog、PopupWindow 等。之所以无法采集，是因为无法遍历到被点击的 View。对于这种比较特殊的情况，可以采用代码埋点来辅助的办法解决。对于 Dialog，我们可以通过 dialog.getWindow().getDecorView() 拿到它的 RootView，然后手动触发遍历并代理即可。

## 4.7. 缺点

* 由于使用反射，效率比较低，对 App 的整体性能有一定的影响，也可能会引入兼容性方面的风险；
* Application.ActivityLifecycleCallbacks 要求 API 14+；
* View.hasOnClickListeners() 要求 API 15+；
* removeOnGlobalLayoutListener 要求 API 16+；
* 无法直接支持采集游离于 Activity 之上的 View 的点击，比如 Dialog、Popup-Window 等。