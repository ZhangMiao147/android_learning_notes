# 第2章 $AppViewScreen全埋点方案

​		$AppViewScreen 事件，即页面浏览事件。在 Android 系统中，页面浏览其实就是指切换不同的 Activity 或 Fragment（本书暂时只讨论切换 Activity 的情况）。

​		对于一个 Activity，它的哪个生命周期执行了，代表该页面显示出来了呢？通过对 Activity 生命周期的了解可知，其实就是 onResume(Activity activity) 的回调方法。所以，当一个 Activity 执行到 onResume(Activity activity) 生命周期时，也就代表该页面已经显示出来了，即该页面被浏览了。我们只要自动地在 onResume 里触发 $AppViewScreen 事件，即可解决 $AppViewScreen 事件的全埋点。

## 2.1. 关键技术 Application.ActivityLifecycleCallbacks

​		ActivityLifecycleCallbacks 是 Application 的一个内部接口，是从 API 14（即Android 4.0）开始提供的。

​		Application 类通过此接口提供了一系列的回调方法，用于让开发者可以对 Activity 的所有生命周期事件进行集中处理（或称监控）。我们可以通过 Application 类提供的 registerActivityLifecycleCallback(ActivityLifecycleCallbacks callback) 方法来注册 ActivityLifecycleCallbacks 回调。

​		以 Activity 的 onResume(Activity activity) 生命周期为例，如果我们注册了 Activity-LifecycleCallbacks 回调，Android 系统会先回调 ActivityLifecycleCallbacks的 onActivity-Resumed(Activity activity) 方法，然后再执行 Activity 本身的 onResume 函数（请注意这个调用顺序，因为不同的生命周期的执行顺序略有差异）。

## 2.2. 原理概述

​		为了实现全埋点中的页面浏览事件，最优的方案还是基于我们上面讲的 Application.ActivityLifecycleCallbacks。

​		不过，使用 Application.ActivityLifecycleCallbacks 机制实现全埋点的页面浏览事件，也有一个明显的缺点，就是注册 Application.ActivityLifecycleCallbacks 回调要求 API 14+。

​		在应用程序自定义的 Application 类的 onCreate() 方法中初始化埋点 SDK，并传入当前的 Application 对象。埋点 SDK 拿到 Application 对象之后，通过调用 Application 的 registerActivityLifecycleCallback(ActivityLifecycleCallbacks callback) 方法注册 Application.ActivityLifecycleCallbacks 回调。这样埋点 SDK 就能对当前应用程序中所有的 Activity 的生命周期事件进行集中处理（监控）了。

​		在注册的 Application.ActivityLifecycleCallbacks 的 onActivityResumed(Activity activity) 回调方法中，我们可以拿到当前正在显示的 Activity 对象，然后调用 SDK 的相关接口触发页面浏览事件（$AppViewScreen）即可。

## 2.3. 案例

​		完整的项目源码可以参考以下网址：https://github.com/wangzhzh/AutoTrackAppViewScreen

​		通过调用 SDK 的内部私有类 SensorsDataPrivate 的 registerActivityLifecycleCallbacks(Application application) 方法来注册 ActivityLifecycleCallbacks 的。

​		只有 API 14+ 才能注册 ActivityLifecycleCallbacks 回调。

## 2.4. 完善方案

​		在申请权限的 Activity 中，调用ActivityCompat.requestPermissions 方法申请权限之后，不管用户选择了 “允许” 还是 “禁止” 按钮，系统都会先调用 onRequestPermissionsResult 回调方法，然后再调用当前 Activity的onResume 生命周期函数。方案就是通过 onResume 生命周期函数来采集页面浏览事件的，这个现象会直接导致我们的埋点 SDK 再一次触发页面浏览事件。

​		在申请权限的 Activity 中，在它的 onRequestPermissionsResult 回调中首先调用 ignoreAutoTrackActivity 方法来忽略当前 Activity 的页面浏览事件，然后在 onStop 生命周期函数中恢复采集当前 Activity 的页面浏览事件。

​		这样处理之后，就可以解决申请权限再次触发页面浏览事件的问题了。

## 2.5 扩展采集能力

​		采集当前 Activity 的名称（包名+类名）是远远不够的，还需要采集当前 Activity 的title（标题）。

​		但是一个 Activity 的 title 的来源是非常复杂的，因为可以通过不同的方式来设置一个 Activity 的 title，甚至可以使用自定义的 View 来设置 title。比如说，可以在 Android-Manifest.xml 文件中声明 activity 时通过 android：label 属性来设置，还可以通过 activity.setTitle() 来设置，也可以通过 ActionBar、ToolBar 来设置。所以，在获取 Activity 的 title 时，需要兼容不同的设置 title 的方式，同时更需要考虑其优先级顺序。

​		方案：首先通过 activity.getTitle() 获取当前 Activity 的 title，因为用户有可能会使用 ActionBar 或 ToolBar，所以我们还需要获取 ActionBar 或 ToolBar 设置的 title，如果能获取到，就以这个为准（即覆盖通过 activity.getTitle() 获取的 title）。如果以上两个步骤都没有获取到 title，那我们就要尝试获取 android：label 属性的值。