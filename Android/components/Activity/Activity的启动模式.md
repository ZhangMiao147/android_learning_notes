# 搞懂 Activity 的所有启动模式

	本文内容
	1. 启动模式简述
		1.1. 为什么需要定义启动模式？
	2. 如何定义 Activity 的启动模式
		2.1. 在 AndroidManifest.xml 中设置 Activity 的启动模式
		2.2. 使用 Intent 标识定义启动模式
	3. LaunchMode 的四种启动模式
		3.1. standard(标准模式)
			3.1.1. 标准模式的应用场景
		3.2. singleTop(栈顶模式)
			3.2.1. 栈顶复用模式的应用场景
		3.3. singleTask(栈内复用)
			3.3.1. 栈内复用模式的应用场景
		3.4. singleInstance(单例模式)
			3.4.1. 单例模式的应用场景
	4. Intent 的 flags
		4.1. 常用的 flags 介绍
	5. onNewIntent() 方法与回调时机
	6. 关联任务
	7. 清理返回栈
		7.1. alwaysRetainTaskState
		7.2. clearTaskOnLaunch
		7.3. finishOnTaskLaunch
	8. launchMode 与 Intent 的 flags 的对比



[TOC]

# 1. 启动模式简述

　　和生命周期一样， activity 的四种 launchMode 非常重要但也特别容易混淆，首先，activity 是以任务栈（task）的形式创建和销毁的，栈是一种 “ 后进先出 ” 的数据结构，是一系列被操作的 activities 的集合。在默认情况下，启动第一个 activity 时，系统将会为它创建一个任务栈并将活动置于栈底，而从这个 activity 启动的其他 activity 将会依次入栈，当用户连续按下返回键时，任务栈中的 activity 会从栈顶开始依次出栈并销毁。

　　Android 默认情况下会为每个 App 维持一个 task 来存放 App 的所有 activities (在默认情况下)，task 的默认 name 为该 app 的 packagename(包名)。

　　大部分 task 都启动自 Home 屏幕。当用户触摸 application launcher 中的图标（或 Home 屏幕上的快捷图标）时，应用程序的 task 就进入前台。如果该应用不存在 task (最近没有使用过此应用)，则会新建一个 task ，该应用的 “main” activity 作为栈的根 activity 被打开。

　　当用户返回到 home 屏幕执行另一个 task 时，当前 task 被移动到后台执行，此时它的返回栈（back statck） 也被保存在后台，同时 android 为新 task 创建一个新的返回栈（back stack），当它被再次运行从而返回前台时，它的返回栈(back stack)被移到前台，并恢复其之前执行的 activity 。如果后台有太多运行 task ，当内存不足时，系统将会杀死一些 task 释放内存。

　　而启动模式就是定义 Activity 实例与 task 的关联方式。

## 1.1. 为什么需要定义启动模式？

　　task 有一个弊端，就是对于某些 activity 不希望它总要重新创建，比如，让某个 Activity 启动在一个新的 task 中，让 Activity 启动时只调用栈内已有的某个实例，或者当用户离开 task 时只想保留根 Activity ，并且清空 task 里面的其他 Activity ，这样的需求，使用默认的任务栈模式是无法实现的，这时就需要采用不同的启动模式。

# 2. 如何定义 Activity 的启动模式

　　设置 Activity 的启动模式有两种方式，一种是在 AndroidManifest.xml 中设置，另一种就是使用 Intent 标识设置 Activity 的启动模式。

## 2.1. 在 AndroidManifest.xml 中设置 Activity 的启动模式

　　在 AndroidManidest.xml 中设置 Activity 的启动模式非常简单，直接在想要设置的 Activity 中添加 `android:launchMode=""` 属性即可，`android:launchMode=""` 属性有四个可供选择的值，分别是 `standard`、`singleTop`、`singTask` 与 `singleInstance`，这四个值分别对应四种启动模式：标准模式、栈顶复用、栈内复用与单例模式。

　　例如，设置 Activity 的启动模式为 `singleTop` ,在 AndroidManifest.xml 中应该是：

```xml
   <activity
            android:name=".FirstActivity"
            android:launchMode="singleTop" />
```

## 2.2. 使用 Intent 标识定义启动模式

　　Intent 的 flags 有 20 个，能够设置启动模式更加多样化。

　　在 startActivity 时，设置 Intent 的 flag 值，如下:

```java
Intent intent = new Intent(MainActivity.this, FirstActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
startActivity(intent);
```

 **注意:** 如果同时在 AndroidManifest 中用 lauchMode 属性和代码中用 Intent 设置了 Activity 的启动模式，则会以代码中 Intent 的设置为准。

# 3. launchMode 的四种启动模式

## 3.1. standard(标准模式)

　　Activity 的默认启动模式，不设置启动模式时，就是标准模式。只要启动 Activity 就会创建一个新实例，并将该 Activity 添加到当前任务栈中。

### 3.1.1. 标准模式的应用场景

　　正常打开一个新的页面，这种启动模式使用最多，最普通。一般没有特殊需求都是使用标准模式。

## 3.2. singleTop(栈顶复用)

　　在这种启动模式下，首先会判断要启动的活动是否已经存在于栈顶，如果是的话就不创建新实例，直接复用栈顶活动，并且调用 activity 的 onNewIntent() 方法。如果要启动的活动不位于栈顶，则会创建新实例入栈。

### 3.2.1. 栈顶复用模式的应用场景

　　栈顶复用模式避免了同一个页面被重复打开，应用场景例如一个新闻客户端，在通知栏收到多条推送，点击一条推送就会打开新闻的详情页，如果是默认的启动模式，点击一次将会打开一个详情页，栈中就会有三个详情页，如果使用栈顶复用模式，点击第一条推送之后，接着点击其他的推送，都只会有一个详情页，可以避免重复打开页面。

## 3.3. singleTask(栈内复用)

　　singleTask 是一种栈内单例模式，当一个 activity 启动时，如果栈中没有 activity 则会创建 activity 并让它入栈；如果栈中有 activity ，则会将位于 activity 之上的 activities 出栈，然后复用栈中的 activity ，调用 activity 的 onNewIntent() 方法。

　　这种模式会保证 Activity 在栈内只有一个或者没有。

### 3.3.1. 栈内复用模式的应用场景

　　栈内复用模式适合作为程序的入口。最常用的就是一个 APP 的首页，一般 App 的首页长时间保留在栈内，并且是栈的第一个 activity。例如浏览器的主界面，不管从多少个应用启动浏览器，只会启动主界面一次，并清空主界面上面的其他页面，根据 onNewIntent 方法传递的数值，显示新的界面。

## 3.4. singleInstance(单例模式)

　　这种模式是真正的单例模式，以这种模式启动的活动会单独创建一个任务栈，并且依然遵循栈内复用的特性，保证了这个栈中只能存在这一个活动。并且系统不会在这个单例模式的 Activity 的实例所在栈中中启动任何其他的 Activity 。单例模式的 Activity 的实例永远是这个栈中的唯一一个成员。

## 3.4.1. 单例模式的应用场景

　　单例模式使用需要与程序分离开的页面。电话拨号页面，通过自己的应用或者其他应用打开拨打电话页面，只要系统的栈中存在该实例，那么就会直接调用，还有闹铃提醒。

　　关于 launchMode 的四种模式验证，可以查看 [验证Activity四种launchMode](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Activity%E5%9B%9B%E7%A7%8DlaunchMode.md) 文章。

# 4. Intent 的 flags

　　Intent 的 flags 有 20 个，这里只列出了常用的flag。查看全部的 flags ，请查看 [关于Intent的全部flags介绍](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%85%B3%E4%BA%8EIntent%E7%9A%84%E5%85%A8%E9%83%A8flags%E4%BB%8B%E7%BB%8D.md) 。

## 4.1. 常用的 flags 介绍

　　操作中的 A,B,C 表示不同的 Activity，A->B 表示从 A 跳转到 B，栈情况中的 t1,t2 表示不同的栈，栈中的记录都是记录的从栈底到栈顶的顺序。

### 4.1.1. FLAG_ACTIVITY_CLEAR_TOP

**描述**：设置此标志，如果 activity 已经在栈中，会将栈中 activity 之上的 activities 进行出栈关闭，如果启动模式是默认的（标准模式），设置了 FLAG_ACTIVITY_CLEAR_TOP 标志的 activity 会结束并重新创建；如果是其他模式或者 Intent 设置了 FLAG_ACTIVITY_SINGLE_TOP，则 activity 会将新的 intent 传递给栈中的 activity 的 onNewIntent() 方法。

**举例**：C 跳转 B 的 flag 设置为 FLAG_ACTIVITY_CLEAR_TOP ，当前栈中（从栈底到栈顶）的情况是：A->B->C，然后 C 跳转 B，栈的情况就成了：A->B。

### 4.1.2. FLAG_ACTIVITY_NO_HISTORY

**描述**：如果这只此 flag，则启动的 activity 将不会保留在历史栈中，一旦用户离开它，activity 将结束。

**举例**：A 跳转 B 的 flag 设置为 FLAG_ACTIVITY_NO_HISTORY，B 跳转 C，在 C 界面点击返回键，则会直接回到 A 界面。

### 4.1.3. FLAG_ACTIVITY_NO_ANIMATION

**描述**：设置此标签，则跳转启动的 activity 动画不会显示。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NO_ANIMATION，则 A 跳转 B 时，没有动画。

### 4.1.4. FLAG_ACTIVITY_NEW_TASK

**描述**：设置 FLAG_ACTIVITY_NEW_TASK 标签后，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果没有，则新建一个栈让 activity 入栈；如果有，则保持栈中 activity 的顺序不变，如果栈中没有 activity，将 activity 入栈，如果栈中有 activity，则将整个栈移动到前台。

**举例**：设置 A 跳转 B 的 flag 为 FLAG_ACTIVITY_NEW_TASK，设置 B 的 taskAffinity 的值。A 跳转 B ，B 跳转 C，C 跳转回到 A，A 跳转 B,会显示 C 界面。

### 4.1.5. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK

**描述**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK 联合使用时，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果有则先将栈清空，将被启动的 activity 会入栈，并将栈整体移动到前台；如果没有，则新建栈来存放被启动的 activity。

**举例**：设置 A 跳转 B 的 flag 为 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TASK，设置 B 的 taskAffinity 的值。A 跳转 B ，B 跳转 C，C 跳转回到 A，A 跳转 B，显示 B 界面。

**其他**：与单独使用 FLAG_ACTIVITY_NEW_TASK 不同的是，启动设置的 activity，如果存在 activity 亲和性的栈，会先将栈中的 activity 全部清除，不管栈中是否存在启动的 activity 的实例，然后将启动的 activity 入栈。

### 4.1.6. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TOP

**描述**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_CLEAR_TOP 联合使用时，首先会查找是否存在和被启动的 activity 具有相同亲和性的任务栈，如果有，栈中如果包含 activity ，则将栈中 activity 之上包括栈中的 activity 移除，将被启动的 activity 入栈，并将栈整体移动到前台，如果栈中没有要启动 activity，则直接将 activity 入栈；如果没有，则新建栈来存放被启动的 activity。

**举例**：C 跳转 B 设置 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_CLEAR_TOP , A 跳转 B，B 跳转 C，栈中情况是:A->B->C，C 跳转 B ,栈中情况是:A->B（B是新启动的B）。

### 4.1.7. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

**描述**：如果设置 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS，则新的 activity 将不会被保留在最近启动 activities 的列表中。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS，设置 B 的亲和性。A 跳转 B，然后查看多任务管理器，可以看到多任务管理器列表中只有 A 所在的任务栈，并没有 B 所在的任务栈，所以在 B 界面按 home 退出应用后，再次打开只会回到 A 界面。

**其他**：FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 与使用 FLAG_ACTIVITY_NO_HISTORY 标志不同，使用 FLAG_ACTIVITY_NO_HISTORY 标志时，在经过 A -> B -> C 的界面跳转后，在 C 界面点击 back 键就会回到 A 界面，而 FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS 一起使用时，在经过 A -> B -> C 的界面跳转后 ，在 C 点击 back 返回，还是会回到 B 界面的。

### 4.1.8. FLAG_ACTIVITY_REORDER_TO_FRONT

**描述**：设置此标志，如果 activity 已经在栈中运行，将会把 activity 带到栈的顶部。

**举例**：C 跳转 B 设置 FLAG_ACTIVITY_REORDER_TO_FRONT，A 跳转 B，B跳转 C，C 跳转 B，当前栈情况是：A->C->B。

### 4.1.9. FLAG_ACTIVITY_FORWARD_RESULT

**描述**：如果设置这个标志并用于启动一个新的 activity，则回复对象从本 activity 移动到新的 activity 上。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_FORWARD_RESULT，A 使用 startActivityForResult 跳转 B，B 跳转 C，点击 C 按钮 setResult 设置返回值，并结束 C，返回到 A 时，A 在 onActivityResult 中接收到 C 的返回值。

### 4.1.10.FLAG_ACTIVITY_NEW_DOCUMENT

**描述**：被用于基于 Intent 的 activity 活动开一个新的任务。同一个 activity 的不同实例将会在最近的任务列表中显示不同的记录。

**举例**：A 跳转 B 设置为 FALG_ACTIVITY_NEW_DOCUMENT，A 跳转 B，B 跳转 C，当前栈情况是：栈1（A），栈2（B->C）,C 跳转 A，A 跳转 B，当前栈情况是：栈1（A），栈2（B->C），显示 C 界面。

**其他**：相当于在 manifest 中定义 android.R.attr#documentLaunchMode="intoExisting"，如果之前已经打开过，则会打开之前的。

### 4.1.11.FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULITIPLE_TASK

**描述**：单独使用 FLAG_ACTIVITY_NEW_DOCUMENT 时，会先从存在的任务栈中搜索匹配 Intent 的栈，如果没有任务栈被发现则创建新的任务栈，当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配任务栈而是直接开启一个新的任务栈。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用，A 跳转 B，B 跳转 C，C 跳转 A，当前栈情况是:栈1（A），栈2（B->C），A 跳转 B，当前栈情况是：栈1（A），栈2（B->C），栈3：B。

**其他**：FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用，相比较 FLAG_ACTIVITY_NEW_DOCUMENT 的单独使用就是，不管是否存在 activity 所在的任务栈，都新建任务栈。效果等同于documentLaunchMode=“always”，不管之前有没有打开，都新创建一个。

### 4.1.12. FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_MULITIPLE_TASK

**描述**：单独使用  FLAG_ACTIVITY_NEW_TASK 时，会先从存在的任务栈中搜索匹配 Intent 的栈，如果没有任务栈被发现则创建新的任务栈，当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配任务栈而是直接开启一个新的任务栈。

**举例**：FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用的情况和 FLAG_ACTIVITY_NEW_DOCUMENT 与 FLAG_ACTIVITY_MULTIPLE_TASK 联合使用的情况基本相同，不同的点就在于 FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_NEW_DOCUMENT 的不同上。

### 4.1.13. FLAG_ACTIVITY_RETAIN_IN_RECENTS

**描述**：默认情况下，进入最近任务栈的记录由 FLAG_ACTIVITY_NEW_DOCUMENT 创建，当用户关闭 activity 时任务栈就会被移除，如果想要允许任务栈保留方便它能被重新启动，可以使用此标志。

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_RETAIN_IN_RECENTS 与 FLAG_ACTIVITY_NEW_DOCUMENT，A 跳转到 B，在 B 界面点击 back 键，然后点击 home 键回到桌面，进入最近使用的任务栈，可以看到有 A 和 B 两个任务栈。

### 4.1.14. FLAG_ACTIVITY_NO_USER_ACTION

**描述**：如果设置此标志，在 activity 被前台的新启动的 activity 造成 paused 之前，将会阻止当前最顶部的 activity 的 onUserLeaveHint 回调。通常，当 activity 在用户的操作下被移除栈顶则会调用 onUserLeaveHint 回调，这个回调标志着 activity 生命周期的一个点，以便隐藏任何 “ 直到用户看到它们 ” 的通知，比如闪烁的 LED 灯。 

**举例**：A 跳转 B 设置 FLAG_ACTIVITY_NO_USER_ACTION，A 跳转 B ,A 的 onUserLeaveHint() 方法没有被调用。

　　关于 flag 的验证，可以查看 [验证Intent的flags](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81Intent%E7%9A%84flags.md) 文章。

# 5. onNewIntent() 方法与回调时机

　　onNewIntent() 方法会在 activity 复用的时候调用，也就是说调用 activity ，并不会创建 activity 的新实例，而是复用栈中的 activity ，复用时就会调用 onNewIntent() 方法，将新的 Intent 传递给 oNewIntent() 方法。

　　关于 onNewIntent() 方法的验证，可以查看 [验证 onNewIntent 方法](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E9%AA%8C%E8%AF%81%20onNewIntent%20%E6%96%B9%E6%B3%95.md) 文章。

# 6. 关联任务

　　“ 关联 ” 指示 Activity 优先属于哪个任务，默认情况下，同一个应用的 Activity 倾向于在同一个 task 中。可以通过 < activity > 标签的 taskAffinity 来修改这种行为。

　　taskAffinity 属性取字符串值，该值必须不同于在 < manifest > 元素中声明的默认软件包名称，因为软件包名称是应用的默认任务关联。

　　在两种情况下，关联会起作用：

* 启动 Activity 的 Intent 包含 FLAG_ACTIVITY_NEW_TASK 标志。

　　默认情况下，新 Activity 会启动到调用 startActivity() 的 Activity 任务中，它将与调用方相同的任务。但是，如果传递给 startActivity() 的 Intent 包含 FLAG_ACTIVITY_NEW_TASK 标志，则系统会先寻找与 Activity 相关联的任务来存储新 Activity，如果没有符合的栈，则会新建任务并将新 Activity 入栈。

　　如果此标志导致 Activity 开始新任务，且用户按 home 键离开，则必须为用户提供导航回任务的方式。有些实体（如通知管理器）始终在外部任务中启动 Activity，而从不作为其自身的一部分启动 Activity ，因此它们始终将 FLAG_ACTIVITY_NEW_TASK 放入传递给 startActivity() 的 Intent 中。请注意，如果 Activity 能够由可以使用此标志的外部实体调用，则用户可以通过独立方式返回到启动的任务，例如，使用启动器图标（任务的根 Activity 具有 CATEGORY_ACTIVITY Intent 过滤器）。

* Activity 将其 allowTaskReparenting 属性设置为“true”

　　在这种情况下，Activity 可以从其启动的任务移动到与其具有关联的任务（如果该任务出现在前台）。

　　例如，假设将报告所选城市天气状况的 Activity 定义为旅行应用的一部分。它与同一应用中的其他 Activity 具有相同的关联（默认应用关联），并允许利用此属性重定父级。当你的一个 Activity 启动天气预报 Activity 时，它最初所属的任务与你的 Activity 相同。但是，当旅游应用的任务出现在前台时，系统会将天气预报 Activity 重新分配给该任务并显示在其中。

# 7. 清理返回栈

　　如果用户长时间离开任务，则系统会清除所有 Activity 的任务，根 Activity 除外。当用户再次返回到任务时，仅恢复根 Activity 。系统这样做的原因是，经过很长一段时间后，用户可能已经放弃之前执行的操作。返回到任务是要开始执行新的操作。

　　可以使用下列几个 Activity 属性修改此行为：

## 7.1. alwaysRetainTaskState

　　如果在任务的根 Activity 中将此属性设置为 “true”，则不会发生刚才所述的默认行为。即使在很长一段时间后，任务仍将所有 Activity 保留在其堆栈中。

## 7.2. clearTaskOnLaunch

　　如果在任务的根 Activity 中将该属性设置为 “true”，则每当用户离开任务然后返回时，系统都会将堆栈清除到只剩下根 Activity 。换而言之，它与 alwaysRetainTaskState 正好相反。即使只离开任务片刻时间，用户也始终会返回到任务的初始状态。

## 7.3. finishOnTaskLaunch

　　此属性类似于 clearTaskOnLaunch ，但它对单个 Activity 起作用，而非整个任务。此外，它还有可能会导致任何 Acivity 停止，包括根 Activity 。设置为 “true” 时，Activity 仍是任务的一部分，但是仅限于当前会话。如果用户离开然后返回任务，则任务将不复存在。

# 8. launchMode 与 Intent 的 flags 的对比

1. Intent 的 flags 的优先于 launchMode 。

2. launchMode 设置的一些启动模式 Intent 的 flags 无法代替，Intent 的 flag 的一些使用 launchMode 也无法代替。

3. 指定 launchMode 的 activity 被任何对象任何地方调用，启动模式都一样；而 Intent 的 flags 只对 startActivity 的 activity 有效，其他调用同样的 activity 可以设置其他的启动模式，并不会相互影响。

# 9. 参考文章

1. [老生常谈-Activity](https://juejin.im/post/5adab7b6518825670c457de3)
2. [全面了解 Activity](https://juejin.im/entry/589847f7128fe10058ebd803)
3. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)
4. [Android之Activity系列总结(二)--任务和返回栈](https://www.cnblogs.com/jycboy/p/6367330.html)
5. [Android四大组件之Activity详解](https://www.cnblogs.com/caobotao/p/4987015.html)