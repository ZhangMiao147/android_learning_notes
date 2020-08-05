# Android 的常见问题

# 1. Activity 的启动流程

activity 启动过程全解析 https://blog.csdn.net/zhaokaiqiang1992/article/details/49428287

## 1.1. 主要对象功能介绍

- ActivityManagerServices，简称AMS，服务端对象，负责系统中所有Activity的生命周期
- ActivityThread，App的真正入口。当开启App之后，会调用main()开始运行，开启消息循环队列，这就是传说中的UI线程或者叫主线程。与ActivityManagerServices配合，一起完成Activity的管理工作
- ApplicationThread，用来实现ActivityManagerService与ActivityThread之间的交互。在ActivityManagerService需要管理相关Application中的Activity的生命周期时，通过ApplicationThread的代理对象与ActivityThread通讯。
- ApplicationThreadProxy，是ApplicationThread在服务器端的代理，负责和客户端的ApplicationThread通讯。AMS就是通过该代理与ActivityThread进行通信的。
- Instrumentation，每一个应用程序只有一个Instrumentation对象，每个Activity内都有一个对该对象的引用。Instrumentation可以理解为应用进程的管家，ActivityThread要创建或暂停某个Activity时，都需要通过Instrumentation来进行具体的操作。
- ActivityStack，Activity在AMS的栈管理，用来记录已经启动的Activity的先后关系，状态信息等。通过ActivityStack决定是否需要启动新的进程。
- ActivityRecord，ActivityStack的管理对象，每个Activity在AMS对应一个ActivityRecord，来记录Activity的状态以及其他的管理信息。其实就是服务器端的Activity对象的映像。
- TaskRecord，AMS抽象出来的一个“任务”的概念，是记录ActivityRecord的栈，一个“Task”包含若干个ActivityRecord。AMS用TaskRecord确保Activity启动和退出的顺序。如果你清楚Activity的4种launchMode，那么对这个概念应该不陌生。

## 1.2. 启动流程

Android 是基于 Linux 系统的，而在 Linux 中，所有的进程都是由 Init 进程直接或者间接 fork 出来的，在 Android 系统里面 ，zygote 是一个进程的名字。Android 是基于 Linux System 的，当手机开机的时候，Linux 的内核加载完成之后就会启动一个叫 "init" 的进程。在 Linux System 里面，所有的进程都是由 init 进程 fork 出来的，zygote 进程也一样。

每一个 App 其实都是：

* 一个单独的 dalvik 虚拟机
* 一个单独的进程













# 2.Activity 的启动模式，及其使用场景

启动模式就是定义 Activity 实例与 task 的关联方式。

## 2.1. Acitivity 的四种启动模式

### 2.1.1. standard(标准模式)

　　Activity 的默认启动模式，不设置启动模式时，就是标准模式。只要启动 Activity 就会创建一个新实例，并将该 Activity 添加到当前任务栈中。

#### 2.1.1.1. 标准模式的应用场景

　　正常打开一个新的页面，这种启动模式使用最多，最普通。一般没有特殊需求都是使用标准模式。

### 2.1.2. singleTop(栈顶复用)

　　在这种启动模式下，首先会判断要启动的活动是否已经存在于栈顶，如果是的话就不创建新实例，直接复用栈顶活动，并且调用 activity 的 onNewIntent() 方法。如果要启动的活动不位于栈顶，则会创建新实例入栈。

#### 2.1.2.1. 栈顶复用模式的应用场景

　　栈顶复用模式避免了同一个页面被重复打开，应用场景例如一个新闻客户端，在通知栏收到多条推送，点击一条推送就会打开新闻的详情页，如果是默认的启动模式，点击一次将会打开一个详情页，栈中就会有三个详情页，如果使用栈顶复用模式，点击第一条推送之后，接着点击其他的推送，都只会有一个详情页，可以避免重复打开页面。

### 2.1.3. singleTask(栈内复用)

　　singleTask 是一种栈内单例模式，当一个 activity 启动时，如果栈中没有 activity 则会创建 activity 并让它入栈；如果栈中有 activity ，则会将位于 activity 之上的 activities 出栈，然后复用栈中的 activity ，调用 activity 的 onNewIntent() 方法。

　　这种模式会保证 Activity 在栈内只有一个或者没有。

#### 2.1.3.1. 栈内复用模式的应用场景

　　栈内复用模式适合作为程序的入口。最常用的就是一个 APP 的首页，一般 App 的首页长时间保留在栈内，并且是栈的第一个 activity。例如浏览器的主界面，不管从多少个应用启动浏览器，只会启动主界面一次，并清空主界面上面的其他页面，根据 onNewIntent 方法传递的数值，显示新的界面。

　　比如说自定义的相机界面，不论从哪里启动相机，只会启动相机的主界面一次，并且会清除相机主界面上面的其他页面。

### 2.1.4. singleInstance(单例模式)

　　这种模式是真正的单例模式，以这种模式启动的活动会单独创建一个任务栈，并且依然遵循栈内复用的特性，保证了这个栈中只能存在这一个活动。并且系统不会在这个单例模式的 Activity 的实例所在栈中启动任何其他的 Activity 。单例模式的 Activity 的实例永远是这个栈中的唯一一个成员。

#### 2.1.4.1. 单例模式的应用场景

　　单例模式使用需要与程序分离开的页面。电话拨号页面，通过自己的应用或者其他应用打开拨打电话页面，只要系统的栈中存在该实例，那么就会直接调用，还有闹铃提醒。

## 2.2. Intent 的 flags

也可以通过 Intent 的 setFlags() 方法设置应用的启动方式。

### 2.2.1. 常用的 flags 介绍

　　操作中的 A,B,C 表示不同的 Activity，A->B 表示从 A 跳转到 B，栈情况中的 t1,t2 表示不同的栈，栈中的记录都是记录的从栈底到栈顶的顺序。

#### 2.2.1.1. FLAG_ACTIVITY_CLEAR_TOP

**描述**：设置此标志，如果 activity 已经在栈中，会将栈中 activity 之上的 activities 进行出栈关闭，如果启动模式是默认的（标准模式），设置了 FLAG_ACTIVITY_CLEAR_TOP 标志的 activity 会结束并重新创建；如果是其他模式或者 Intent 设置了 FLAG_ACTIVITY_SINGLE_TOP，则 activity 会将新的 intent 传递给栈中的 activity 的 onNewIntent() 方法。

**举例**：C 跳转 B 的 flag 设置为 FLAG_ACTIVITY_CLEAR_TOP ，当前栈中（从栈底到栈顶）的情况是：A->B->C，然后 C 跳转 B，栈的情况就成了：A->B。

#### 2.2.1.2. FLAG_ACTIVITY_NO_HISTORY

**描述**：如果这只此 flag，则启动的 activity 将不会保留在历史栈中，一旦用户离开它，activity 将结束。

**举例**：A 跳转 B 的 flag 设置为 FLAG_ACTIVITY_NO_HISTORY，B 跳转 C，在 C 界面点击返回键，则会直接回到 A 界面。

#### 2.2.1.3. FLAG_ACTIVITY_NEW_TASK

**描述**：设置 FLAG_ACTIVITY_NEW_TASK 标签后，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果没有，则新建一个栈让 activity 入栈；如果有，则保持栈中 activity 的顺序不变，如果栈中没有 activity，将 activity 入栈，如果栈中有 activity，则将整个栈移动到前台。

**举例**：设置 A 跳转 B 的 flag 为 FLAG_ACTIVITY_NEW_TASK，设置 B 的 taskAffinity 的值。A 跳转 B ，B 跳转 C，C 跳转回到 A，A 跳转 B,会显示 C 界面。

#### 2.2.1.4. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK

**描述**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK 联合使用时，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果有则先将栈清空，将被启动的 activity 会入栈，并将栈整体移动到前台；如果没有，则新建栈来存放被启动的 activity。

**举例**：设置 A 跳转 B 的 flag 为 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK，设置 B 的 taskAffinity 的值。A 跳转 B ，B 跳转 C，C 跳转回到 A，A 跳转 B，显示 B 界面。

**其他**：与单独使用 FLAG_ACTIVITY_NEW_TASK 不同的是，启动设置的 activity，如果存在 activity 亲和性的栈，会先将栈中的 activity 全部清除，不管栈中是否存在启动的 activity 的实例，然后将启动的 activity 入栈。

#### 2.2.1.5. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TOP

**描述**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TOP 联合使用时，首先会查找是否存在和被被启动的 activity 具有相同亲和性的任务栈，如果有，栈中如果包含 activity ，则将栈中 activity 之上包括栈中的 activity 移除，将被启动的 activity 入栈，并将栈整体移动到前台，如果栈中没有要启动 activity，则直接将 activity 入栈；如果没有，则新建栈来存放被启动的 activity。

**举例**：C 跳转 B 设置 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_CLEAR_TOP , A 跳转 B，B 跳转 C，栈中情况是:A->B->C，C 跳转 B ,栈中情况是:A->B（B是新启动的B）。

#### 2.2.1.6. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

**描述**：如果设置 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS，则新的 activity 将不会被保留在最近启动 activities 的列表中。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS，设置 B 的亲和性。A 跳转 B，然后查看多任务管理器，可以看到多任务管理器列表中只有 A 所在的任务栈，并没有 B 所在的任务栈，所以在 B 界面按 home 退出应用后，再次打开只会回到 A 界面。

**其他**：FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 与使用 FLAG_ACTIVITY_NO_HISTORY 标志不同，使用 FLAG_ACTIVITY_NO_HISTORY 标志时，在经过 A -> B -> C 的界面跳转后，在 C 界面点击 back 键就会回到 A 界面，而 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 一起使用时，在经过 A -> B -> C 的界面跳转后 ，在 C 点击 back 返回，还是会回到 B 界面的。

#### 2.2.1.7. FLAG_ACTIVITY_NEW_DOCUMENT

**描述**：被用于基于 Intent 的 activity 活动开一个新的任务。同一个 activity 的不同实例将会在最近的任务列表中显示不同的记录。

**举例**：A 跳转 B 设置为 FALG_ACTIVITY_NEW_DOCUMENT，A 跳转 B，B 跳转 C，当前栈情况是：栈1（A），栈2（B->C）,C 跳转 A，A 跳转 B，当前栈情况是：栈1（A），栈2（B->C），显示 C 界面。

**其他**：相当于在 manifest 中定义 android.R.attr#documentLaunchMode="intoExisting"，如果之前已经打开过，则会打开之前的。FALG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_NEW_TASK 的不同点在于亲和性上，FLAG_ACTIVITY_NEW_TASK 会寻找与自己亲和性的栈，如果有，则进入，如果没有，则创建栈，而 FALG_ACTIVITY_NEW_DOCUMENT 是没有打开过，则直接创建栈。

#### 2.2.1.8. FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULITIPLE_TASK

**描述**：单独使用 FLAG_ACTIVITY_NEW_DOCUMENT 时，会先从存在的任务栈中搜索匹配 Intent 的栈，如果没有任务栈被发现则创建新的任务栈，当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配任务栈而是直接开启一个新的任务栈。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用，A 跳转 B，B 跳转 C，C 跳转 A，当前栈情况是:栈1（A），栈2（B->C），A 跳转 B，当前栈情况是：栈1（A），栈2（B->C），栈3：B。

**其他**：FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用，相比较 FLAG_ACTIVITY_NEW_DOCUMENT 的单独使用就是，不管是否存在 activity 所在的任务栈，都新建任务栈。效果等同于documentLaunchMode=“always”，不管之前有没有打开，都新创建一个。

#### 2.2.1.9. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_MULITIPLE_TASK

**描述**：单独使用  FLAG_ACTIVITY_NEW_TASK 时，会先从存在的任务栈中搜索匹配 Intent 的栈，如果没有任务栈被发现则创建新的任务栈，当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配任务栈而是直接开启一个新的任务栈。

**举例**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用的情况和 FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用的情况基本相同，不同的点就在于 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_NEW_DOCUMENT 的不同上。

### 2.3. launchMode 与 Intent 的 flags 的对比

1. Intent 的 flags 的优先于 launchMode 。

2. launchMode 设置的一些启动模式 Intent 的 flags 无法代替，比如 ，Intent 的 flag 的一些使用 launchMode 也无法代替，比如。

3. 指定 launchMode 的 activity 被任何对象任何地方调用，启动模式都一样；而 Intent 的 flags 只对 startActivity 的 activity 有效，其他调用同样的 activity 可以设置其他的启动模式，并不会相互影响。

## 3. Activity 的事件分发机制

Android事件分发机制完全解析，带你从源码的角度彻底理解(上) https://blog.csdn.net/guolin_blog/article/details/9097463



## 4. Android 的缓存机制

Android缓存机制 https://www.jianshu.com/p/2608f036f362

## 5. gradle 构建周期



## 6. Android 的消息机制

![](image/2.jpeg)

![](image/3.jpeg)

## 7. Android 的缓存机制

彻底解析Android缓存机制——LruCache https://www.jianshu.com/p/b49a111147ee



## 8. Fragment 的生命周期，与 Activity 生命周期的比较

Activity和Fragment的生命周期，以及对比 https://blog.csdn.net/copy_yuan/article/details/51159552

Android Fragment学习与使用—高级篇 https://blog.csdn.net/qq_24442769/article/details/77679147

【Android】Fragment之间数据传递的三种方式 https://www.jianshu.com/p/f87baad32662



解决方法：在onCreate方法中判断参数Bundle savedInstanceState，为空时初始化Fragment实例，然后在Fragment中通过onSaveInstanceState的方法恢复数据

![](image/1.jpeg)

## 9. Handler 通信原理

Android子线程创建Handler方法 https://blog.csdn.net/hongdameng/article/details/42639961

# 10 .跨进程通信方式



# 