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
- TaskRecord，AMS抽象出来的一个“任务”的概念，是记录ActivityRecord的栈，一个“Task”包含若干个ActivityRecord。AMS用TaskRecord确保Activity启动和退出的顺序。

## 1.2. 启动流程

Android 是基于 Linux 系统的，而在 Linux 中，所有的进程都是由 Init 进程直接或者间接 fork 出来的，在 Android 系统里面 ，zygote 是一个进程的名字。Android 是基于 Linux System 的，当手机开机的时候，Linux 的内核加载完成之后就会启动一个叫 "init" 的进程。在 Linux System 里面，所有的进程都是由 init 进程 fork 出来的，zygote 进程也一样。

每一个 App 其实都是：

* 一个单独的 dalvik 虚拟机
* 一个单独的进程

所以当系统里面的第一个 zygote 进程运行之后，在这之后再开启 App，就相当于开启一个新的进程。而为了实现资源共用和更快的启动速度，Android 系统开启新进程的方式，是通过 fork 第一个 zygote 进程实现的。所以说，除了第一个 zygote 进程，其他应用所在的进程都是 zygote 的子进程。

### SystemService

SystemService 也是一个进程，而且是由 zygote 进程 fork 出来的。

系统里面重要的服务都是在这个进程里面开启的，比如 
ActivityManagerService、PackageManagerService、WindowManagerService等等。

在zygote开启的时候，会调用ZygoteInit.main()进行初始化，初始化的时候会fork SystemService 进程。

ActivityManagerService，简称AMS，服务端对象，负责系统中所有Activity的生命周期。

ActivityManagerService进行初始化的时机很明确，就是在SystemServer进程开启的时候，就会初始化ActivityManagerService。

在 SystemServer 的 main() 方法中运行了 `new SystemServer().run()`，而在 run() 方法中创建 ActivityManagerService对象，并且完成了成员变量初始化。而且在这之前，调用createSystemContext()创建系统上下文的时候，也已经完成了mSystemContext和ActivityThread的创建。注意，这是系统进程开启时的流程，在这之后，会开启系统的Launcher程序，完成系统界面的加载与显示。

Android系统里面的服务器和客户端的概念：其实服务器客户端的概念不仅仅存在于Web开发中，在Android的框架设计中，使用的也是这一种模式。服务器端指的就是所有App共用的系统服务，比如我们这里提到的ActivityManagerService，和前面提到的PackageManagerService、WindowManagerService等等，这些基础的系统服务是被所有的App公用的，当某个App想实现某个操作的时候，要告诉这些系统服务，比如你想打开一个App，那么我们知道了包名和MainActivity类名之后就可以打开。但是，我们的App通过调用startActivity()并不能直接打开另外一个App，这个方法会通过一系列的调用，最后还是告诉AMS说：“我要打开这个App，我知道他的住址和名字，你帮我打开吧！”所以是AMS来通知zygote进程来fork一个新进程，来开启我们的目标App的。这就像是浏览器想要打开一个超链接一样，浏览器把网页地址发送给服务器，然后还是服务器把需要的资源文件发送给客户端的。

App和AMS(SystemServer进程)还有zygote进程分属于三个独立的进程，他们之间如何通信呢：App与AMS通过Binder进行IPC通信，AMS(SystemServer进程)与zygote通过Socket进行IPC通信。

AMS有什么用？如果想打开一个App的话，需要AMS去通知zygote进程，除此之外，其实所有的Activity的开启、暂停、关闭都需要AMS来控制，所以说，AMS负责系统中所有Activity的生命周期。

在Android系统中，任何一个Activity的启动都是由AMS和应用程序进程（主要是ActivityThread）相互配合来完成的。AMS服务统一调度系统中所有进程的Activity启动，而每个Activity的启动过程则由其所属的进程具体来完成。

### Launcher

当点击手机桌面上的图标的时候，App就由Launcher开始启动了。Launcher本质上也是一个应用程序，和我们的App一样，也是继承自Activity。

Launcher实现了点击、长按等回调接口，来接收用户的输入。通过捕捉图标点击事件，然后startActivity()发送对应的Intent请求。

### Instrumentation

每个Activity都持有Instrumentation对象的一个引用，但是整个进程只会存在一个Instrumentation对象。当startActivityForResult()调用之后，实际上还是调用了mInstrumentation.execStartActivity()。

所以当我们在程序中调用startActivity()的 时候，实际上调用的是Instrumentation的相关的方法。

这个类里面的方法大多数和Application和Activity有关，是的，这个类就是完成对Application和Activity初始化和生命周期的工具类。

### AMS 和 ActivityThread 之间的 Bindler 通信

Binder本质上只是一种底层通信方式，和具体服务没有关系。为了提供具体服务，Server必须提供一套接口函数以便Client通过远程访问使用各种服务。这时通常采用Proxy设计模式：将接口函数定义在一个抽象类中，Server和Client都会以该抽象类为基类实现所有接口函数，所不同的是Server端是真正的功能实现，而Client端是对这些函数远程调用请求的包装。

ActivityManagerService和ActivityManagerProxy都实现了同一个接口——IActivityManager。

虽然都实现了同一个接口，但是代理对象ActivityManagerProxy并不会对这些方法进行真正地实现，ActivityManagerProxy只是通过这种方式对方法的参数进行打包(因为都实现了相同接口，所以可以保证同一个方法有相同的参数，即对要传输给服务器的数据进行打包)，真正实现的是ActivityManagerService。

但是这个地方并不是直接由客户端传递给服务器，而是通过Binder驱动进行中转。

客户端调用ActivityManagerProxy接口里面的方法，把数据传送给Binder驱动，然后Binder驱动就会把这些东西转发给服务器的ActivityManagerServices，由ActivityManagerServices去真正的实施具体的操作。

客户端：ActivityManagerProxy =====>Binder驱动=====> ActivityManagerService：服务器

而且由于继承了同样的公共接口类，ActivityManagerProxy提供了与ActivityManagerService一样的函数原型，使用户感觉不出Server是运行在本地还是远端，从而可以更加方便的调用这些重要的系统服务。

但是！这里Binder通信是单方向的，即从ActivityManagerProxy指向ActivityManagerService的，如果AMS想要通知ActivityThread做一些事情，应该咋办呢？

还是通过Binder通信，不过是换了另外一对，换成了ApplicationThread和ApplicationThreadProxy。

客户端：ApplicationThread <=====Binder驱动<===== ApplicationThreadProxy：服务器

他们也都实现了相同的接口IApplicationThread。

### AMS接收到客户端的请求之后，会如何开启一个Activity？



### Application是在什么时候创建的？onCreate()什么时候调用的？

也是在ActivityThread.main()的时候。

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

1. 新活动已在当前任务中时，在新活动上面的活动会被关闭，新活动不会重新启动，只会接收new intent。
2.  新活动已在任务最上面时：如果启动模式是"multiple" (默认的)，并且没添加 FLAG_ACTIVITY_SINGLE_TOP，那么活动会被销毁重新创建；如果启动模式是其他的，或者添加了FLAG_ACTIVITY_SINGLE_TOP，那么只会调用活动的onNewIntent()。
3. 跟 FLAG_ACTIVITY_NEW_TASK 联合使用效果很好：如果用于启动一个任务中的根活动，会把该任务移到前面并清空至root状态。这特别有用，比如用于从 notification manager 中启动活动。

#### 2.2.1.2. FLAG_ACTIVITY_NO_HISTORY

1.  新活动不会保留在历史栈中，一旦用户切换到其他页面，新活动会马上销毁。
2. 旧活动的onActivityResult()方法永远不会被触发。

**举例**：A 跳转 B 的 flag 设置为 FLAG_ACTIVITY_NO_HISTORY，B 跳转 C，在 C 界面点击返回键，则会直接回到 A 界面。

#### 2.2.1.3. FLAG_ACTIVITY_SINGLE_TOP

1. 新活动已存在历史栈的顶端时就不会重新启动。
2. 与 launchMode 的 “singleTask” 相同。

#### 2.2.1.4. FLAG_ACTIVITY_NEW_TASK

1. 新活动会成为历史栈中的新任务（一组活动）的开始。
2. 通常用于具有"launcher"行为的活动：让用户完成一系列事情，完全独立于之前的活动。
3. 如果新活动已存在于一个为它运行的任务中，那么不会启动，只会把该任务移到屏幕最前。
4. 如果新活动要返回result给启动自己的活动，就不能用这个flag。
5. 与 launchModel 的 “singleTask” 启动模式效果相同？

**描述**：设置 FLAG_ACTIVITY_NEW_TASK 标签后，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果没有，则新建一个栈让 activity 入栈；如果有，则保持栈中 activity 的顺序不变，如果栈中没有 activity，将 activity 入栈，如果栈中有 activity，则将整个栈移动到前台。

**举例**：设置 A 跳转 B 的 flag 为 FLAG_ACTIVITY_NEW_TASK，设置 B 的 taskAffinity 的值。A 跳转 B ，B 跳转 C，C 跳转回到 A，A 跳转 B,会显示 C 界面。

#### 2.2.1.5. FLAG_ACTIVITY_NEW_DOCUMENT

1. 本 flag 会给启动的活动开一个新的任务记录。使用了本 flag 或 documentLaunchMode 属性时，相同的活动的多实例会在最近任务列。
2. 使用本 flag 比使用 documentLaunchMode 属性好，因为 documentLaunchMode 属性会跟活动绑定，而 flag 只在需要时添加。
3. 注意本 flag 的默认词义，活动销毁后最近任务列表中的入口不会移除。这跟使用 FLAG_ACTIVITY_NEW_TASK 不一样，后者活动销毁后入口马上移除。可以用 FLAG_ACTIVITY_RETAIN_IN_RECENTS 改变这个行为。
4. 本 flag 可以跟 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用。单独使用时跟 manifest 活动中定义 documentLauchMode = "intoExisting" 效果相同，联合使用时跟 manifest 活动中定义 documentLaunchMode = "always" 效果相同。

## 2.3. launchMode 与 Intent 的 flags 的对比

1. Intent 的 flags 的优先于 launchMode 。
2. launchMode 设置的一些启动模式只有四种，而 Intent 的 flags 比较多，可以搭配使用，实现效果也多种多样。
3. 指定 launchMode 的 activity 被任何对象任何地方调用，启动模式都一样；而 Intent 的 flags 只对 startActivity 的 activity 有效，其他调用同样的 activity 可以设置其他的启动模式，并不会相互影响。

## 2.4. Activity 属性

### 2.4.1. allowTaskReparenting

　　在这种情况下，Activity 可以从其启动的任务移动到与其具有关联的任务（如果该任务出现在前台）。

　　例如，假设将报告所选城市天气状况的 Activity 定义为旅行应用的一部分。它与同一应用中的其他 Activity 具有相同的关联（默认应用关联），并允许利用此属性重定父级。当你的一个 Activity 启动天气预报 Activity 时，它最初所属的任务与你的 Activity 相同。但是，当旅游应用的任务出现在前台时，系统会将天气预报 Activity 重新分配给该任务并显示在其中。

### 2.4.2. alwaysRetainTaskStat

　　如果用户长时间将某个task移入后台，则系统会将该task的栈内容弹出只剩下栈底的activity，此时用户再返回，则只能看到根activity了。如果栈底的activity的这个属性设置成true，则将阻止这一行为，从而保留所有的栈内容。

### 2.4.3. clearTaskOnLaunch

　　根activity的这个属性设置成true时，则每当用户离开任务然后返回时，系统都会将堆栈清除到只剩下根 Activity 。换而言之，它与 alwaysRetainTaskState 正好相反。即使只离开任务片刻时间，用户也始终会返回到任务的初始状态。

### 2.4.4. finishOnTaskLaunch

　　此属性类似于 clearTaskOnLaunch ，但它对单个 Activity 起作用，而非整个任务。此外，它还有可能会导致任何 Acivity 停止，包括根 Activity 。设置为 “true” 时，Activity 仍是任务的一部分，但是仅限于当前会话。如果用户离开然后返回任务，则任务将不复存在。

# 3. Activity 的事件分发机制

　　Android 的事件分发机制基本会遵从 Activity -> ViewGroup -> View 的顺序进行事件分发，然后通过调用 onTouchEvent() 方法进行事件的处理。

　　一般情况下，事件列都是从用户按下（ACTION_DOWN）的那一刻产生的，不得不提到，三个非常重要的于事件相关的方法。

* dispatchTouchEvent() - 分发事件
* onTouchEvent() - 处理事件
* onInterceptTouchEvent() - 拦截事件

## 3.1. Activity

　　当点击事件产生后，事件首先会传递给当前的 Activity，这会调用 Activity 的 dispatchTouchEvent() 方法，在这个方法中会调用 getWindow().superDispatchTouchEvent() 方法，其实就是调用了 DecorView 的 superDispatchTouchEvent()，DecoreView 的父类是 ViewGroup，其实也就是 ViewGroup 的 dispatchEvent() 方法。

![](view/image/Activity的事件分发示意图.png)

## 3.2. View

　　不管是 DOWN、MOVE 还是 UP 都是按照下面的顺序执行：

1. dispatchTouchEvrnt
2. setOnTouchListener 的 onTouch
3. onTouchEvent

　　如果 setOnToucheListener 和 setClickListener 都注册了，onTouch 是优先于 onClick 执行的。

　　onTouch 方法是由返回值的，如果把 onTouch 方法里的返回值改成 true，onClick 方法不再执行了。

　　只要触摸到了任何一个控件，就一定会调用该控件的 dispatchTouchEvent 方法。当去点击按钮的时候，就回去调用 Buttong 类里面的 dispatchTouchEvent 方法，就是调用的 View 的 dispatchTouchEvent 方法。

### 3.1.1. View#dispatchTouchEvent

```java
...
			// 必须满足三个条件都为真，才会返回 true
          	// 1. mOnTouchListener 不为 null，即调用了 setOnTouchListener()
          	// 2. (mViewFlags & ENABLED_MASK) == ENABLED
            // 3. li.mOnTouchListener.onTouch(this, event) 
            if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                	// 调用 li.mOnTouchListener.onTouch 方法
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
			// 调用 onTouchEvent 方法
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }
...
```

　　在 View 的 dispatchTouchEvent() 方法内，会进行一个判断，如果 `li != null && li.mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED && li.mOnTouchListener.onTouch(this, event)` ，这个判断结果为 true ，则返回 true，否则就会调用 onTouchEvent(event) 方法。

　　`li != null && li.mOnTouchListener != null` ：如果调用了 setTouchListener() 方法设置了触摸监听，`li.mOnTouchListener`就是设置的 OnTouchListener 对象，这个判断结果就是 true。

　　`(mViewFlags & ENABLED_MASK) == ENABLED`：判断当前点击的空间是否是 enable 的，按钮默认都是 enable 的，所以这个条件为 true。

　　`li.mOnTouchListener.onTouch(this, event)`：li.mOnTouchListener 是触摸监听的 OnTouchListener 对象，调用它的 onTouch() 方法，如果 onTouch() 返回 true，则三个条件成立，则 dispatchTouchEvent() 方法返回 true。

　　如果控件是不可点击的或者 OnTouchListener.onTouch() 方法不存在或 OnTouchListener.onTouch() 存在并返回 false，就会执行 onTouchEvent() 方法。

### 3.1.2. View#onTouchEvent

　　如果控件可点击或者长按，View 的 onTouchEvent() 方法最终一定会 return true。

* ACTION_DOWN：如果父控件是可以滚动的，则延时 100ms 处理点击（滚动检测回调），为了防止点击是滚动；如果父控件是不可以滚动的，则刷新背景，检查长按。

  延时到了后，也是执行刷新背景，检测长按。

  刷新背景的方法中在刷新背景后，还会调用 dispatchSetPressed() 方法将消息传递给子 View。

  检查长按是，如果支持是长按，就会发送延迟消息（长按的检测回调），延时时间到了后，就会调用 performLongClick() 方法，而 performLongClick() 方法调用了 li.mOnLongClickListener.onLongClick() 方法。

* ACTION_MOVE：显示判断触摸点是否还在当前 View 上，如果不再当前 View 上，则移除滚动和长按的检测回调，并刷新背景；如果现在当前 View 上，则移除原来的长按检测回调，重新发出长按检测回调。

* ACTION_UP：控件可以点击或者长按时并且设置的长按回调 onLongClick() 方法返回了 false，则移除长按检测回调，并且接着运行 performClickInternal() 方法；如果 onLongClick() 方法返回了 true，就不会执行 performClickInternal() 方法。如果检测滚动还没完，则刷新背景，最后移除滚动检测。

  performClickInternal() 方法就是调view用设置的 onClickListener() 的 OnClick() 方法。

　　View的事件分发示意图：

![](view/image/View的事件分发示意图.png)

## 3.3. ViewGroup

　　ViewGroup事件分发示意图

![](view/image/ViewGroup事件分发示意图.png)

1. Android 事件分发是先传递给 ViewGroup，再由 ViewGroup 传递给 View 的。

2. ViewGroup 的 dispatchTouchEvent() 方法中回去调用 onInterceptTouchEvent() 方法，在 ViewGroup 中可以通过 onInterceptTouchEvent() 方法对事件传递进行拦截，onInterceptTouchEvent() 方法返回 true 代表不允许事件继续向子 View 传递，返回 false 代表不对事件进行拦截，默认返回 false。

3. ViewGroup 的 dispatchTouchEvent() 方法中在 ACTION_DOWN 时会先去遍历所有的子 View，判断看点击的地方是否是子 View 的控件，如果是，则调用子 View  的 dispatchTouchEvent() 方法；如果点击的地方不是子 View 控件，则调用父类 View 的 dispatchTouchEvent() 方法。在 ACTION_MOVE、ACTION_UP 事件中如果触摸的子 View 存在，也是去调用子 View 的 dispatchTouchEvent() 方法，不存在，则调用父类 View 的 dispatchTouchEvent() 方法。

   子 View 中如果将传递的事件消费掉，ViewGroup 中将无法接收到任何事件。

   如果 ViewGroup 找到了能够处理该事件的 View，则直接交给子 View 处理，自己的 onTouchEvent() 不会被触发。

4. 可以通过覆写 onInterceptTouchEvent(ev) 方法，拦截子 View 的事件（即 return true），把事件交给自己处理，则会执行自己对应的 onTouchEvent() 方法。

5. 子 View 可以通过调用 getParent().requestDisallowInterceptTouchEvent(true); 阻止 ViewGroup 对其 ACTION_MOVE 事件进行拦截。

   也可以在 ACTION_DOWN 和 ACTION_UP 中 return true，但是触摸事件是父控件先执行 dispatchTouchEvent() 方法，然后父控件分发事件调用子控件的 dispatchTouchEvent9) 方法，而子控件在 dispatchTouchEvent() 方法中执行 getParent().requestDisallowInterceptTouchEvent(true) 设置已经不能影响到父控件的  dispatchTouchEvent() 方法了。

   而 ACTION_UP，事件都是最后一个了，return true 拦截子控件，如果不想拦截，直接 return false 就好了，没有必要。

## 3.4. onTouch 和 onTouchEvent 有什么区别？又该如何使用

　　从源码中可以看出，这两个方法都是在 View 的 dispatchTouchEvent 中调用的，onTouch 优先于 onTouchEvent 执行。如果在 onTouch 方法中通过返回 true 将事件消费掉，onTouchEvent 将不会再执行。

　　另外需要注意的是，onTouch 能够得到执行需要两个前提条件，第一 mOnTouchListener 的值不能为空，第二当前点击的控件必须是 enable 的。因此如果有一个控件是非 enable 的，那么给它注册 onTouch 事件将永远得不到执行。对于这一类控件，如果想要监听它的 touch 事件，就必须通过在该控件中重写 onTouchEvent 方法来实现。

# 4. Android 的缓存机制

Android缓存机制 https://www.jianshu.com/p/2608f036f362

彻底解析Android缓存机制——LruCache https://www.jianshu.com/p/b49a111147ee

# 5. gradle 构建周期

https://www.jianshu.com/p/2e19268bf387

# 6. Handler

## 6.1. Handler 消息机制的流程

1. 准备阶段

   * 在子线程调用 Looper.prepare() 方法或在主线程调用 Looper.prepareMainLooper() 方法创建当前线程的 Looper 对象。

     主线程中这一步由 Android 系统在应用启动时完成。

     使用 ThreadLocal 将 Loop 对象存储在当前线程中。

   * 在创建 Looper 对象时会创建一个消息队列 MessageQueue。

   * Looper 通过 loop() 方法获取到当前线程的 Looper 和 MessageQyeye，并启动循环，从 MessageQueue 不断提取 Message，若 MessageQueue 没有消息，处于阻塞状态。

2. 发送消息

   * 使用当前线程创建的 Handler 在其他线程通过 sendMessage() 发送 Message 到 MessageQueue。在这个时候会将 Message 对象的 target 设置为 Handler 对象，将 Message 对象的 callback 设置为 Runnable 对象（hanle.post(Runnable runnable)）。
   * 使用 synchronized(this) 保证同步，将 Message 根据消息时间插入 MessageQueue 的消息链表中。如果是链表中的第一个消息，则唤醒阻塞。

3. 获取消息

   * Looper 的 loop() 方法从 MessageQueue 获取新插入的 Message。

   * MessageQueue 的 next() 方法中是一个死循环，循环重会有一个阻塞唤醒操作，当等待 nextPollTimeoutMillis 时长，或者消息队列被唤醒，都会唤醒。使用 synchronized(this) 保证同步，查找可用的消息并返回。

   * Looper 获取到 Message 后，通过 Message 的 target 即 Handler 调用 dispatchMessage(Message msg) 方法分发提取到的 Message，然后回收 Message 并继续循环获取下一个 Message。

     消息分发的优先级：

     * Message 的回调方法：message.callback.run()，优先级最高。（handle.post() 情况）
     * Handler 中 Callback 的回调方法：Handler.mCallback.handleMessage(msg)。
     * Handler.handleMessage(msg)。（handle.sendMessage() 的情况）

     第三种情况是最常用的，通过覆写 Handler.handleMessage() 方法从而实现自己的业务逻辑。

4. 阻塞等待

   * MessageQueue 没有 Message 时，重新进入阻塞状态。

![](handler/image/Handler消息机制图.png)

### 6.1.1.  Message

　　Message 封装了任务携带的信息和处理该任务的 handler，可以被发送给 Handler。

　　尽管 Message 有 public 的默认构造方法，但是应该通过 Message.obtail() 来从消息池中获取空消息对象，以节省资源。

　　如果 Message 只需要携带简单的 int 消息，请优先使用 Message.arg1 和 Message.arg2 来传递消息，这比用 Bundle 更省内存。

　　用 Message.what 来标识消息，以便用不同方式处理 message。

### 6.1.2. MessageQueue

　　Message 并不是直接加到 MessageQueue 的，而是通过 Handler 对象和 Looper 关联在一起。可以通过 Looper.myQueue() 方法来检索当前线程的 MessageQueue。

　　MessageQueue 的内部存储了一组消息，其以队列的形式对外提供插入和删除的工作，虽然叫做消息队列，但是它的内部存储结构并不是真正的队列，而是采取单链表的数据结构来存储列表，因为单链表在插入和删除上比较有优势。

　　MessageQueue 主要功能是像消息池投递消息（MessageQueue.enqueMessage）和取走消息池的消息（MessageQueue.next）。

### 6.1.3. Handler

　　主要功能是向消息池发送各种消息事件（Hadnler.sendMessage）和处理相应消息事件（Handler.handleMessage）。

　　每个 Handler 都会跟一个线程绑定，并与该线程的 MessageQueue 关联在一起，从而实现消息的管理以及线程间通信。

### 6.1.4. Looper

　　不断循环执行（Looper.loop），从 MessageQueue 中读取消息，按分发机制将消息分发给目标处理者。　　

## 6.2. Android子线程创建 Handler 方法

　　如果想在子线程上创建 Handler，通过直接 new 的出来是会报异常:

```java
AndroidRuntime(2226): java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
```

### 6.2.1. 方法1（直接获取当前子线程的 looper ）

　　既然说要 Looper.prepare()，那就给 prepare()，并且要调用 loop.loop()：

```java
new Thread(new Runnable() {
			public void run() {
				Looper.prepare();  // 此处获取到当前线程的Looper，并且 prepare()
				Handler handler = new Handler(){
					@Override

					public void handleMessage(Message msg) {						Toast.makeText(getApplicationContext(), "handler msg", Toast.LENGTH_LONG).show();
					}
				};
				handler.sendEmptyMessage(1);
                Looper.loop();
			};
		}).start();
```


### 6.2.2.  方法2（获取主线程的 looper，或者说是 UI 线程的 looper）

　　这个方法简单粗暴，不过和上面的方法不一样的是，这个是通过主线程的looper来实现的。

```java
new Thread(new Runnable() {
			public void run() {
				Handler handler = new Handler(Looper.getMainLooper()){ // 区别在这！！！！
					@Override
					public void handleMessage(Message msg) {
					Toast.makeText(getApplicationContext(), "handler msg", Toast.LENGTH_LONG).show();
					}
				};
				handler.sendEmptyMessage(1);
			};
		}).start();
```

## 6.3. Handler 的内存泄漏的处理



# 7. Fragment 的生命周期，与 Activity 生命周期的比较

Activity和Fragment的生命周期，以及对比 https://blog.csdn.net/copy_yuan/article/details/51159552

Android Fragment学习与使用—高级篇 https://blog.csdn.net/qq_24442769/article/details/77679147

【Android】Fragment之间数据传递的三种方式 https://www.jianshu.com/p/f87baad32662



解决方法：在onCreate方法中判断参数Bundle savedInstanceState，为空时初始化Fragment实例，然后在Fragment中通过onSaveInstanceState的方法恢复数据

![](image/1.jpeg)

# 8. 跨进程通信方式

　　IPC 是 Inter-Process Communication 的缩写，含义为进程间通信或者跨进程通信，是指两个进程之间进行数据交换的过程。

Android 开发艺术碳素第 2 章