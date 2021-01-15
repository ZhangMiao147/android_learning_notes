# 第2章 $AppViewScreen全埋点方案

**$AppViewScreen事件，即页面浏览事件。在Android系统中，页面浏览其实就是指切换不同的Activity或Fragment（本书暂时只讨论切换Activity的情况）**

**对于一个Activity，它的哪个生命周期执行了，代表该页面显示出来了呢？通过对Activity生命周期的了解可知，其实就是onResume(Activity activity)的回调方法。所以，当一个Activity执行到onResume(Activity activity)生命周期时，也就代表该页面已经显示出来了，即该页面被浏览了。我们只要自动地在onResume里触发$AppViewScreen事件，即可解决$AppViewScreen事件的全埋点。**

### 2.1 关键技术Application.ActivityLifecycleCallbacks

**ActivityLifecycleCallbacks是Application的一个内部接口，是从API 14（即Android 4.0）开始提供的**

**Application类通过此接口提供了一系列的回调方法，用于让开发者可以对Activity的所有生命周期事件进行集中处理（或称监控）。我们可以通过Application类提供的registerActivityLifecycleCallback(ActivityLifecycleCallbacks callback)方法来注册ActivityLifecycleCallbacks回调。**

**以Activity的onResume(Activity activity)生命周期为例，如果我们注册了Activity-LifecycleCallbacks回调，Android系统会先回调ActivityLifecycleCallbacks的onActivity-Resumed(Activity activity)方法，然后再执行Activity本身的onResume函数（请注意这个调用顺序，因为不同的生命周期的执行顺序略有差异）**

### 2.2 原理概述

**为了实现全埋点中的页面浏览事件，最优的方案还是基于我们上面讲的Application.ActivityLifecycleCallbacks。**

**不过，使用Application.ActivityLifecycleCallbacks机制实现全埋点的页面浏览事件，也有一个明显的缺点，就是注册Application.ActivityLifecycleCallbacks回调要求API 14+。**

**在应用程序自定义的Application类的onCreate()方法中初始化埋点SDK，并传入当前的Application对象。埋点SDK拿到Application对象之后，通过调用Application的registerActivityLifecycleCallback(ActivityLifecycleCallbacks callback)方法注册Application.ActivityLifecycleCallbacks回调。这样埋点SDK就能对当前应用程序中所有的Activity的生命周期事件进行集中处理（监控）了。**

**在注册的Application.ActivityLifecycleCallbacks的onActivityResumed(Activity activity)回调方法中，我们可以拿到当前正在显示的Activity对象，然后调用SDK的相关接口触发页面浏览事件（$AppViewScreen）即可。**

### 2.3 案例

**通过调用SDK的内部私有类SensorsDataPrivate的registerActivityLifecycleCallbacks(Application application)方法来注册ActivityLifecycleCallbacks的。**

**只有API 14+才能注册ActivityLifecycleCallbacks回调。**

### 2.4 完善方案

**在申请权限的Activity中，在它的onRequestPermissionsResult回调中首先调用ignoreAutoTrackActivity方法来忽略当前Activity的页面浏览事件，然后在onStop生命周期函数中恢复采集当前Activity的页面浏览事件。**

**这样处理之后，就可以解决申请权限再次触发页面浏览事件的问题了。**

## 第3章 $AppStart、$AppEnd全埋点方案

**对于$AppStart和$AppEnd事件而言，归根结底就是判断当前应用程序是处于前台还是处于后台。而Android系统本身并没有给应用程序提供相关的接口来判断这些状态，所以我们只能借助其他方式来间接判断。**

**这个开源项目提供了6种方案。这6种方案的综合对比可以参考表3-1。表3-1　6种方案的对比**