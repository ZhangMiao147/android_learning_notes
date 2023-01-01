# Navigation 使用

## 1. 基本概念

### 1.1. 背景

在 Android 中，页面的切换和管理包括应用程序 Appbar 的管理、Fragment 的动画切换以及 Fragment 之间的参数传递等内容。并且，纯代码的方式使用起来不是特别友好，并且 Appbar 在管理和使用的过程中显得很混乱。因此，Jetpack 提供了一个名为 Navigation 的组件，旨在方便开发者管理 Fragment 页面和 Appbar。

相比之前 Fragment 的管理需要借助 FragmentManager 和 FragmentTransaction，使用 Navigation 组件有如下一些优点：

* 可视化的页面导航图，方便理清页面之间的关系
* 通过 destination 和 action 完成页面间的导航
* 方便添加页面切换动画
* 页面间类型安全的参数传递
* 通过 Navigation UI 类，对菜单/底部导航/抽屉蓝菜单导航进行统一的管理
* 支持深层链接 DeepLink

### 1.2. 含义

* Navigation 是 Android Jetpack 组件包中的重要一员，借助于 Single Activity 和多个 Fragment 碎片，优化 Android Activity 启动的开销和简化 Activity 之间的数据通信问题。
* 内置支持普通 Fragment、Activity 和 DialogFragment 组件的跳转，也就是所有 Dialog 或 PopupWindow 都建议使用 DialogFragment 实现，这样可以涵盖所有常用的跳转场景，统一返回栈的管理。
* 另外，基于 Fragment 实现可以做到状态存储和恢复。

## 2. 组成

### 2.1. Navigation graph

一个包含所有导航相关信息的 XML 资源

* xml 文档，包含所有被管理的 Fragment、起始目标、换页目标、返回目标。

### 2.2. NavHostFragment

一种特殊的 Fragment，用于承载导航内容的容器

```xml
<fragment
        android:id="@+id/navHostFragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNaHost="true"
        app:navGraph="@navigation/nav_graph_main" />

```

android:name 是固定的，不能修改。

### 2.3. NavController

管理应用导航的对象，实现 Fragment 之间的跳转等操作。

* 用来管理 NavHost 中的导航动作，通常是写在点击事件内完成 Fragment 的切换。

```kotlin
textView.setOnClickListener {
     findNavController().navigate(R.id.action_Fragment1_to_Fragment2)
  }
```

## 3. 基本使用

### 3.1. 引入依赖

```groovy
implementation 'androidx.navigation:navigation-fragment-ktx:2.5.0'
implementation 'androidx.navigation:navigation-ui-ktx:2.5.0'

```

### 3.2. 创建导航视图

首先确保 AndriodStudio 为 3.3 以上：

1. 左键 res，点击 New -> Android Resource Directory
2. 在出现的面板第二行 Resource type 下拉列表中选择 Navigation，然后点击 OK
3. res 目录下会多出一个 navigation 的资源目录，右键该目录，点击 New -> Navigation Resource File，输入需要新建的资源文件名，这里命名 nav_graph，点击 ok，一个 nav_graph.xml 就创建好了。

![](https://img-blog.csdnimg.cn/img_convert/de81dd969ee9b640f643ba6e7f54504d.webp)

### 3.3. 配置 graph：添加 fragment

新建好的 nav_graph.xml 切换到  design 模式下，点击 2 处的加号，选择  Create new destination，即可快速创建新的 Fragment，这里分别新建一个 FragmentA、FragmentB、FragmentC 三个 fragment

![](https://img-blog.csdnimg.cn/636dd080df174956b2f5b07ac86e1319.png)

![](https://img-blog.csdnimg.cn/img_convert/3a3c780ee8130cf1bb040abd6abe331f.webp)

建好后，可通过手动配置页面之间的跳转关系，点击某个页面，右边会出现一个小圆点，拖拽小圆点指向跳转的页面，这里设置跳转的关系为 FragmentA -> FragmentB -> FragmentC。

![](https://img-blog.csdnimg.cn/img_convert/a035a3ba4053e6d7003be11bd928f43a.webp)

切换到 Code 栏，可以看到生成了如下代码

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/nav_graph"
    app:startDestination="@id/fragmentA">

    <fragment
        android:id="@+id/fragmentA"
        android:name="com.example.testnavigation.FragmentA"
        android:label="fragment_a"
        tools:layout="@layout/fragment_a" >
        <action
            android:id="@+id/action_fragmentA_to_fragmentB2"
            app:destination="@id/fragmentB" />
    </fragment>
    <fragment
        android:id="@+id/fragmentB"
        android:name="com.example.testnavigation.FragmentB"
        android:label="fragment_b"
        tools:layout="@layout/fragment_b" >
        <action
            android:id="@+id/action_fragmentB_to_fragmentC2"
            app:destination="@id/fragmentC" />
    </fragment>
    <fragment
        android:id="@+id/fragmentC"
        android:name="com.example.testnavigation.FragmentC"
        android:label="fragment_c"
        tools:layout="@layout/fragment_c" />
</navigation>
```

* navigation 是根标签，通过 startDestination 配置默认启动的第一个页面，这里配置的是 FragmentA
* fragment 标签代表一个 fragment，其实这里不仅可以配置 fragment，也可以配置 activity，甚至还可以自定义
* action 标签定义了页面跳转的行为，相当于上图中的每条线，destination 定义跳转的目标页，还可以定义跳转时的动画等等
  * 当调用到 action_FragmentA_to_FragmentB2 这个 action，会从 FragmentA -> FragmentB

### 3.4. 添加 NavHostFragment

在 MainActivity 的布局文件中配置 NavHostFragment

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <fragment
        android:id="@+id/fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="true"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/nav_graph" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

* android:name 指定 NavHostFragment
* app:navGraph 指定导航视图，即建好的 nav_graph.xml
* app:defaultNavHost=true 意思是可以拦截系统的返回键，可以理解为默认给 fragment 实现了返回键的功能，这样在 fragment 的跳转过程中，当我们按返回键时，就可以使得 fragment 跟 activity 一样可以回到上一个页面了。

现在运行程序，就可以正常跑起来了，并且看到了 FragmentA 展示的页面，这是因为 MainActivity 的布局文件中配置了 NavHostFragment，并且给 NavHostFragment 指定了导航视图，而导航视图中通过 startDestination 指定了默认展示 FragmentA。

### 3.5. 通过 NavController 管理 fragment 之间的跳转

上面说到三个 fragment 之间的跳转关系是 FragmentA -> FragmentB ->FragmentC，并且已经可以展示了 FragmentA，那怎么跳转到 FragmentB，这就需要用到 NavController 了

打开 FragmentA 类，给布局中的 TextView 定义一个点击事件

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tv.setOnClickListener {
        val navController = Navigation.findNavController(it)
        navController.navigate(R.id.action_fragmentA_to_fragmentB2)
    }
}
```

如果发现不能自动导入布局文件，大概率是要给 app.build 添加插件 'kotlin-android-extensions'

```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-android-extensions'
}
```

可以看到，通过 navController 管理 fragment 的跳转非常简单，首先得到 navController 对象，然后调用它的 navigation 方法，传入前面 nav_graph 中定义的 action 的 id 即可。

按同样的方法给 FragmentB 中的 TextView 也设置一个点击事件，使得点击时跳转到 FragmentC。

运行程序，FragmentA -> FragmentB -> FragmentC，此时按返回键，也是一个一个页面返回，如果把前面的 app:defaultNavHost 设置为 false，按返回键后会发现直接返回到桌面。

#### 3.5.1. NavController 的获取及其能力

通过 Fragment 的扩展方法可以拿到此  Fragmen 从属的 NavController，另外还有一些重载的方法：

```java
// 根据 viewId 向上查找
NavController findNavController(Activity activity, int viewId)
```

```java
// 根据 view 向上查找
NavController findNavController(View view)
```

本质上 findNavController 就是在当前 view 树中，查找距离指定 view 最近的父 NavHostFragment 对应的 NavController。

##### NavController 的能力

对于应用层来说，整个 Navigation 框架，只跟 NavController 打交道，它提供了常用的跳转、返回和获取返回栈等能力。

![](https://img-blog.csdnimg.cn/3f11f7a28ac649c7a9d6ba85e145798d.png)

![](https://img-blog.csdnimg.cn/7ec3fa094b584129bc1e1d7e87fee64e.png)

## 4. 跳转时传递参数

### 4.1. 通过带 bundle 参数的 navigate 方法传递参数

通过指定 bundle 参数可以为目的地传递参数，比如：

```java
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tv.setOnClickListener {
        val navController = Navigation.findNavController(it)
        val bundle = Bundle()
        bundle.putString("key", "test")
        navController.navigate(R.id.action_fragmentA_to_fragmentB2, bundle)
    }
}
```

在目的地 Fragment 可以直接通过 getArguments() 方法获取这个 bundle。

```java
super.onCreate(savedInstanceState)
    val value = arguments?.getString("key")
```

### 4.2. 通过 safeArgs 插件

safe args 与传统传参方式相比，好处在于安全的参数类型，并且通过谷歌官方的支持，能很方便的进行参数传值。

#### 1. 在项目的根 build.gradle 下添加插件

```groovy
buildscript {
    ext.kotlin_version = "1.3.72"
    repositories {
        google()
        jcenter()
    }
    dependencies {
         classpath "com.android.tools.build:gradle:7.0.4"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
       classpath "androidx.navigation:navigation-safe-args-gradle-plugin:2.5.0"
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
```

#### 2. 然后在 app 的 build.gradle 中引用 'androidx.navigation.safeargs.kotlin'

```groovy
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-android-extensions'
    id 'androidx.navigation.safeargs.kotlin'
```

#### 3. 添加完插件后，回到 nav_graph，切到  design 模式，给目标页面添加需要接收的参数

这里需要在 FragmentA 跳转到 FragmentB 时传参数，所以给 FragmentB 设置参数，点击 FragmentB，点击右侧面板的 Arguments 右侧的 +，输入参数的 key 值，指定参数类型和默认值，即可快速添加参数

![](https://img-blog.csdnimg.cn/img_convert/37df6d18fea8502cb9703a98a59249dd.webp)

#### 4. 添加完后，rebuild 一下功能，safeArgs 会自动生成一些代码

在 /build/generated/source/navigation-args 目录下可以看到

![](https://img-blog.csdnimg.cn/b1f6ac668e4e441897d3072d4bdc231a.png)

safeArgs 会根据 nav_graph 中的 fragment 标签生产对应的类

* action 标签会以 "类名+Directions" 命名
* argument 标签会以 “类名+Args” 命名。

使用 safeArgs 后，传递参数是这样的

```java
 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv.setOnClickListener {
            val navController = Navigation.findNavController(it)
            //通过safeArgs传递参数
            val navDestination = FragmentADirections.actionFragmentAToFragmentB2("test")
            navController.navigate(navDestination)
            
            // 普通方式传递参数
		   // val bundle = Bundle()
            // bundle.putString("key", "test")
            // navController.navigate(R.id.action_fragmentA_to_fragmentB2, bundle)
        }
    }
```

接收参数是这样的

```java
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val value = FragmentBArgs.fromBundle(it).key
            .......
        }
        .......
    }

```

## 5. 动画

### 5.1. action 参数设置动画

```
enterAnim: 跳转时的目标页面动画

exitAnim: 跳转时的原页面动画

popEnterAnim: 回退时的目标页面动画

popExitAnim:回退时的原页面动画
```

![](https://img-blog.csdnimg.cn/12b4e61de9ea451c8b9969eb1639bdc8.png)

新增  anim

![](https://img-blog.csdnimg.cn/8122a59700264c0daeba669eed0426fe.png)

slide_in_right.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">

    <translate
        android:duration="500"
        android:fromXDelta="100%"
        android:fromYDelta="0%"
        android:toXDelta="0%"
        android:toYDelta="0%" />
</set>
```

slide_out_left.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">

    <translate
        android:duration="500"
        android:fromXDelta="0%"
        android:fromYDelta="0%"
        android:toXDelta="-100%"
        android:toYDelta="0%" />
</set>
```

slide_in_left.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">

    <translate
        android:duration="500"
        android:fromXDelta="-100%"
        android:fromYDelta="0%"
        android:toXDelta="0%"
        android:toYDelta="0%" />
</set>
```

slide_out_right.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">

    <translate
        android:duration="500"
        android:fromXDelta="0%"
        android:fromYDelta="0%"
        android:toXDelta="100%"
        android:toYDelta="0%" />
</set>
```

添加到 action：可以根据不同需求使用 alpha、scale、rotate、translate 这几种效果

```xml
<action
    android:id="@+id/action_page1_to_action_page2"
    app:destination="@id/page2Fragment"
    app:enterAnim="@anim/slide_in_right"
    app:exitAnim="@anim/slide_out_left"
    app:popEnterAnim="@anim/slide_in_left"
    app:popExitAnim="@anim/slide_out_right" />

```

### 5.2. 共享元素

如果两个页面有类似的元素，可以用这种方式让视觉有连续被带过去的感觉。

在两个页面共用的元件加上 transitionName 这个属性，属性的值药一样。

fragment_one.xml

```xml
<ImageView
        android:id="@+id/catImageView"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:src="@mipmap/cat"
        android:transitionName="catImage" />

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="cat"
        android:transitionName="catText" />
```

fragment_two.xml

```xml
<ImageView
        android:id="@+id/catImageView"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:src="@mipmap/cat"
        android:transitionName="catImage" />

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="cat"
        android:transitionName="catText" />
```

PageOneFragment.kt

把 xml 元件的 transitionName 赋值给 NavController

```kotlin
val extras = FragmentNavigatorExtras(
            catImageView to "catImage",
            textView to "catText")
            
            catImageView.setOnClickListener {

            findNavController().navigate(
            R.id.action_page1_to_action_page2,
            null,
            null, 
            extras)
        }
```

PageTwoFragment.java

```kotlin
@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedElementEnterTransition( TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_image));
    }

```

shared_image.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<transitionSet xmlns:android="http://schemas.android.com/apk/res/android">
    <changeBounds/>
    <changeImageTransform/>
</transitionSet>
```

## 6. 常见问题

### 6.1. Fragment 跳转的启动类型（singleTask、singleTop）如何提供支持？

栈管理：点击 destination，右侧面板中还可以看到 popUpTo、popUpToInclusive、launchSingleTop

![](https://img-blog.csdnimg.cn/4ff4d49cb4d64632975c09f1b0e93196.png)

1. launchSingleTop：如果栈中已经包含了指定要跳转的界面，那么只会保留一个，不指定则栈中会出现两个界面相同的 Fragment 数据，可以理解为类似 activity 的 singleTop，即栈顶复用模式，但又有点不一样，比如 FragmentA@1 -> FragmentA@2，FragmentA@1 会被销毁，但如果是 FragmentA@1 -> FragmentB@1 -> FragmentA@3，FragmentA@1 不会被销毁。
2. popUpTo(tag)：表示跳转到某个 tag，并在 tag 之上的元素出栈。
3. popUpToInclusive：为 true 表示会弹出 tag，false 则不会

例子：FragmentA -> FragmentB -> FragmentC -> FragmentA

设置 FragmentC -> FragmentA 的 action 为 popUpTo = FragmentA，popUpToInclusive = false，那么站内元素变化为

![](https://img-blog.csdnimg.cn/0e45640100944ef3869155d3017aef37.png)

最后会发现需要按两次返回键才会回退到桌面。

设置 popUpInclusive=true 时，栈内元素变化为：

![](https://img-blog.csdnimg.cn/73826c12c10d4002b4170a6a2d7270cb.png)

此时只需要按一次返回键就回退到桌面了，从中可以体会到 popUpTo 和 popUpToInclusive 的含义了。

### 6.2. Fragment 之间如何通信？

Fragment 中的通信还可以分为两种场景，假设目前返回栈中有两个 Fragment 分别为 A 和 B。

* 若 A 与 B 在同级子图中，可以在两端通过创建导航图级别的 ViewModel 完成交互。

例如当前返回栈为：

```
NavGraphA -> NavDestinationB -> NavDestinationC -> NavDestinationD
```

**1. 若想实现 C 与 D 的通信，可以使用节点 B 创建 ViewModel**

```
val vm by navGraphViewModels<TitleVm>(R.id.nav_destination_b)
```



**2. 若 A 与 B 不在同级子图中，可以使用距离二者最近的公共父 Graph 完成通信**

例如当前返回栈为

```
NavGraphA -> NavDestinationB -> NavGraphC -> NavDestinationD
```

若想实现 B 与 D 的通信，需要使用 A 界面创建 ViewModel。

```
val vm by navGraphViewModels<TitleVm>(R.id.home)
```

R.id.home 为二者的最近的公共父 Graph，在父 Graph 销毁前，二者通信都是有效的。

### 6.3. navigation fragment 的重绘

##### 6.3.1. Fragment 生命周期

Navigation 出现之前官方给出的 Fragment 生命周期如下图：（注意 onDestroyView 之处）

![](https://img-blog.csdnimg.cn/0d791eca59354772b6fa7a81d0629124.png)

而 Lifecycle、Navigation 等组件出现之后，官方给出的 Fragment 生命周期图为下图：（PS：Fragment Lifecycle && View Lifecycle）

![](https://img-blog.csdnimg.cn/b4bc74afd5da4d999887065c5001dc06.png)

Navigation 框架下的 Fragment 生命周期分为 Fragment Lifecycle 和 View Lifecycle，View Lifecycle 被单独拎出来了，原因就在于 Navigation 框架下的非栈顶的 Fragment 均会被销毁 View，也即是 A 跳转到 B 界面：A 会执行 onDestroyView 销毁其 View（凡是和 View 相关的，如：Databinding、RecyclerView 都会被销毁），但是 Fragment 本身会存在（Fragment 本身的成员变量等是不会被销毁的）

Navigation 框架之下的正确状态流转应该是类似这的：

![](https://img-blog.csdnimg.cn/51aad612800a4f09a6d4da71213b0604.png)

A 通过 action 打开 B，A 从 onResume 转到 onDestroyView，B 从 onAttach 执行到 onResume，当 B 通过系统返回键返回到 A 时候，A 从上图的 onCreateView 流转到 onResume，此过程中  A 的 View 经历销毁和重建，View（binding 实例）的对象实例是不一样的，但是 FragmentA 这个实例始终相同。

这样的场景下，假设 A 存在一个网络新闻列表 RecyclerView，RecyclerView 随着 view 被销毁、重建。如何保存其中的数据，避免每次返回到 A 的时候重新刷新数据（造成：上次浏览数据、位置丢失、额外的网络资源消耗），因此 RecyclerView 中的 Adapter 的数据项非常关键！

常见的保存方式有：

1. 通过 Fragment 的成员变量
2. ViewModel。在 ViewModel 的 ViewModelScope 通过协程请求网络数据，保存在 ViewModel（ViewModel 生命周期贯穿 Fragment），可通过 LiveData、普通变量保存数据，在 onViewCreated 之后恢复数据。

## 参考文章

1. [Android：安卓学习笔记之navigation的简单理解和使用](https://blog.csdn.net/JMW1407/article/details/125714708)