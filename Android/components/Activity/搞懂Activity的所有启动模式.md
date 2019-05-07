# 搞懂 Activity 的所有启动模式

## 启动模式简述

　　和生命周期一样， activity 的四种 launchMode 非常重要但也特别容易混淆，首先，activity 是以任务栈的形式创建和销毁的，栈是一种“后进先出”的数据结构，在默认情况下，启动第一个 actiivty 时，系统将会为它创建一个任务栈并将活动置于栈底，而从这个 activity 启动的其他 activity 将会依次入栈，当用户连续按下返回键时，任务栈中的 activity 会从栈顶开始依次销毁。

　　Android 中的 activity 全都归属于 task 管理（task 是一个具有栈结构的容器），task 是一系列被操作的 activity 的集合，用户进行操作时将与这些 activity 进行交互。这些 activity 按照启动顺序排队存入一个栈。在默认情况下，启动一个 activity 的时候，会将 activity 入栈，当用户按下返回键的时候，activity 出栈并销毁。

　　Android 默认情况下会为每个 App 维持一个 task 来存放 App 的所有 activities (在默认情况下)，task 的默认 name 为该 app 的 packagename(包名)。

　　大部分 task 都启动自 Home 屏幕。当用户触摸 application launcher 中的图标（或 Home 屏幕上的快捷图标）时，应用程序的 task 就进入前台。如果该应用不存在 task (最近没有使用过此应用)，则会新建一个 task ，该应用的 “main” activity 作为栈的根 activity 被打开。

　　当用户返回到 home 屏幕执行另一个 task 时，当前 task 被移动到后台执行，此时它的返回栈（back statck） 也被保存在后台，同时 android 为新 task 创建一个新的返回栈（back stack），当它被再次运行从而返回前台时，它的返回栈(back stack)被移到前台，并恢复其之前执行的 activity 。如果后台有太多运行 task ，当内存不足时，系统将会杀死一些 task 释放内存。

　　而启动模式就是定义 Activity 实例与 task 的关联方式。

#### 为什么需要定义启动模式？

　　task 有一个弊端，就是对于某些 activity 我们不希望它总要重新创建，比如，让某个 Activity 启动在一个新的 task 中，让 Activity 启动时只调用栈内已有的某个实例，或者当用户离开 task 时只想保留根 Activity ，并且清空 task 里面的其他 Activity ，这样的需求，使用默认的任务栈模式是无法实现的，这时就需要采用不同的启动模式。


## 如何定义 Activity 的启动模式

　　设置 Activity 的启动模式有两种方式，一种是在 AndroidManifest.xml 中设置，一种就是使用 Intent 标识设置 Activity 的启动模式。

#### 在 AndroidManifest.xml 中设置 Activity 的启动模式

　　在 AndroidManidest.xml 中设置 Activity 的启动模式非常简单，直接在想要设置的 Activity 中添加 `android:launchMode=""` 属性即可，`android:launchMode=""` 属性有四个可供选择的值，分别是 `standard`、`singleTop`、`singTask` 与 `singleInstance`，这四个值分别对应四种启动模式：标准模式、栈顶复用、栈内复用与单例模式。

　　例如，设置 Activity 的启动模式为 `singleTop` ,在 AndroidManifest.xml 中应该是：

```
   <activity
            android:name=".FirstActivity"
            android:launchMode="singleTop" />
```

#### 使用 Intent 标识定义启动模式

　　Intent 的 flags 有 20 个，能够设置启动模式更加多样化。

　　在 startActivity 时，设置 Intent 的 flag 值，如下:

```
     Intent intent = new Intent(MainActivity.this, FirstActivity.class);
	 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     startActivity(intent);

```

#### 注意

　　如果你同时在 AndroidManifest 中用 lauchMode 属性和代码中用 Intent 设置了 Activity 的启动模式，则会以代码中 Intent 的设置为准。

## launchMode 的四种启动模式

#### standard(标准模式)

　　Activity 的默认启动模式，不设置启动模式时，就是标准模式。只要启动 Activity 就会创建一个新实例，并将该 Activity 添加到当前任务栈中。

###### 标准模式的应用场景

　　正常打开一个新的页面，这种启动模式使用最多，最普通。一般没有特殊需求都是使用标准模式。

#### singleTop(栈顶复用)

　　在这种启动模式下，首先会判断要启动的活动是否已经存在于栈顶，如果是的话就不创建新实例，直接复用栈顶活动。如果要启动的活动不位于栈顶，则会创建新实例入栈，并且调用 activity 的 onNewIntent() 方法。

###### 栈顶复用模式的应用场景

　　栈顶复用模式避免了同一个页面被重复打开，应用场景例如一个新闻客户端，在通知栏收到多条推送，点击一条推送就会打开新闻的详情页，如果是默认的启动模式，点击一次将会打开一个详情页，栈中就会有三个详情页，如果使用栈顶复用模式，点击第一条推送之后，接着点击其他的推送，都只会有一个详情页，可以避免重复打开页面。

#### singleTask(栈内复用)

　　singleTask 是一种栈内单例模式，当一个 activity 启动时，如果栈中没有 activity 则会创建 activity 并让它入栈；如果栈中有 activity ，则会将位于 activity 之上的 activities 出栈，然后复用栈中的 activity ，调用 activity 的 onNewIntent() 方法。

　　这种模式会保证 Activity 在栈内只有一个或者没有。

###### 栈内复用模式的应用场景

　　栈内复用模式适合作为程序的入口。最常用的就是一个 APP 的首页，一般 App 的首页长时间保留在栈内，并且是栈的第一个 activity。例如浏览器的主界面，不管从多少个应用启动浏览器，只会启动主界面一次，并清空主界面上面的其他页面，根据 onNewIntent 方法传递的数值，显示新的界面。

#### singleInstance(单例模式)

　　这种模式是真正的单例模式，以这种模式启动的活动会单独创建一个任务栈，并且依然遵循栈内复用的特性，保证了这个栈中只能存在这一个活动。并且系统不会在这个单例模式的 Activity 的实例所在栈中中启动任何其他的 Activity 。单例模式的 Activity 的实例永远是这个栈中的唯一一个成员。

###### 单例模式的应用场景

　　单例模式使用需要与程序分离开的页面。电话拨号页面，通过自己的应用或者其他应用打开拨打电话页面，只要系统的栈中存在该实例，那么就会直接调用，还有闹铃提醒。

　　关于 launchMode 的四种模式验证，可以查看 [Activity四种launchMode有什么不同，代码走起来](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%9B%9B%E7%A7%8DlaunchMode%E9%AA%8C%E8%AF%81/Activity%E5%9B%9B%E7%A7%8DlaunchMode%E6%9C%89%E4%BB%80%E4%B9%88%E4%B8%8D%E5%90%8C%EF%BC%8C%E4%BB%A3%E7%A0%81%E8%B5%B0%E8%B5%B7%E6%9D%A5.md) 文章。

## Intent 的 flags

　　Intent 的 flags 有 20 个，这里只列出了常用的flag。查看全部的 flags ，请查看 [关于 Intent 的全部 flags 介绍](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%85%B3%E4%BA%8EIntent%E7%9A%84%E5%85%A8%E9%83%A8flags%E4%BB%8B%E7%BB%8D.md) 。

**常用的 flags 的使用**

　　操作中的 A,B,C 表示不同的 Activity，A->B 表示从 A 跳转到 B，栈情况中的 t1,t2 表示不同的栈，栈中的记录都是记录的从栈底到栈顶的顺序。

| 使用标志 | 解释 | 举例 | 其他 |
| -------- | -------- | -------- | -------- | -------- | -------- |
| FLAG_ACTIVITY_CLEAR_TOP | 如果设置此标签，activity 已经在栈中，则不会启动 activity 的新实例，而是将栈中 activity 之上的 activities 进行出栈关闭，Intent 将被传递给旧的 activity 作为新的 Intent 。 | C 跳转 B 的 flag 设置为 FLAG_ACTIVITY_CLEAR_TOP ，当前栈中（从栈底到栈顶）的情况是：A->B->C，然后 C 跳转 B，栈的情况就成了：A->B | 与 launchMode 的 SingleTask 相同 |

　　关于 flag 的验证，可以查看 [将 Intent 的 flags 使用一下呀呀呀](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Intent%E7%9A%84flags%E9%AA%8C%E8%AF%81/%E5%B0%86Intent%E7%9A%84flags%E4%BD%BF%E7%94%A8%E4%B8%80%E4%B8%8B%E5%91%80%E5%91%80%E5%91%80.md) 文章。

## onNewIntent() 方法与回调时机

　　onNewIntent() 方法会在 activity 复用的时候调用，也就是说调用 activity ，并不会创建 activity 的新实例，而是复用栈中的 activity ，复用时就会调用 onNewIntent() 方法，将新的 Intent 传递给 oNewIntent() 方法。

　　关于 onNewIntent() 方法的验证，可以查看 [onNewIntent 方法何时回调呢](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/onNewIntent%E6%96%B9%E6%B3%95%E7%9A%84%E9%AA%8C%E8%AF%81/onNewIntent%E4%BD%95%E6%97%B6%E5%9B%9E%E8%B0%83%E7%94%A8%E5%91%A2.md) 文章。

## 关联任务

　　“关联”指示 Activity 优先属于哪个任务，默认情况下，同一个应用的 Activity 倾向于在同一个 task 中。可以通过 < activity > 标签的 taskAffinity 来修改这种行为。

　　taskAffinity 属性取字符串值，该值必须不同于在 < manifest > 元素中声明的默认软件包名称，因为软件包名称是应用的默认任务关联。

　　在两种情况下，关联会起作用：

* 启动 Activity 的 Intent 包含 FLAG_ACTIVITY_NEW_TASK 标志。

　　默认情况下，新 Activity 会启动到调用 startActivity() 的 Activity 任务中，它将与调用方相同的任务。但是，如果传递给 startActivity() 的 Intent 包含 FLAG_ACTIVITY_NEW_TASK 标志，则系统会先寻找与 Activity 相关联的任务来存储新 Activity，如果没有符合的栈，则会新建任务并将新 Activity 入栈。

　　如果此标志导致 Activity 开始新任务，且用户按 home 键离开，则必须为用户提供导航回任务的方式。有些实体（如通知管理器）始终在外部任务中启动 Activity，而从不作为其自身的一部分启动 Activity ，因此它们始终将 FLAG_ACTIVITY_NEW_TASK 放入传递给 startActivity() 的 Intent 中。请注意，如果 Activity 能够由可以使用此标志的外部实体调用，则用户可以通过独立方式返回到启动的任务，例如，使用启动器图标（任务的根 Activity 具有 CATEGORY_ACTIVITY Intent 过滤器）。

* Activity 将其 allowTaskReparenting 属性设置为“true”

　　在这种情况下，Activity 可以从其启动的任务移动到与其具有关联的任务（如果该任务出现在前台）。

　　例如，假设将报告所选城市天气状况的 Activity 定义为旅行应用的一部分。它与同一应用中的其他 Activity 具有相同的关联（默认应用关联），并允许利用此属性重定父级。当你的一个 Activity 启动天气预报 Activity 时，它最初所属的任务与你的 Activity 相同。但是，当旅游应用的任务出现在前台时，系统会将天气预报 Activity 重新分配给该任务并显示在其中。

## 清理返回栈

　　如果用户长时间离开任务，则系统会清除所有 Activity 的任务，根 Activity 除外。当用户再次返回到任务时，仅恢复根 Activity 。系统这样做的原因是，经过很长一段时间后，用户可能已经放弃之前执行的操作。返回到任务是要开始执行新的操作。

　　可以使用下列几个 Activity 属性修改此行为：

#### alwaysRetainTaskState

　　如果在任务的根 Activity 中将此属性设置为 “true”，则不会发生刚才所述的默认行为。即使在很长一段时间后，任务仍将所有 Activity 保留在其堆栈中。

#### clearTaskOnLaunch

　　如果在任务的根 Activity 中将该属性设置为 “true”，则每当用户离开任务然后返回时，系统都会将堆栈清除到只剩下根 Activity 。换而言之，它与 alwaysRetainTaskState 正好相反。即使只离开任务片刻时间，用户也始终会返回到任务的初始状态。

#### finishOnTaskLaunch

　　此属性类似于 clearTaskOnLaunch ，但它对单个 Activity 起作用，而非整个任务。此外，它还有可能会导致任何 Acivity 停止，包括根 Activity 。设置为 “true” 时，Activity 仍是任务的一部分，但是仅限于当前会话。如果用户离开然后返回任务，则任务将不复存在。


## launchMode 与 Intent 的 flags 的对比

1. Intent 的 flags 的优先于 launchMode 。

2. launchMode 设置的一些启动模式 Intent 的 flags 无法代替，Intent 的 flag 的一些使用 launchMode 也无法代替。

3. 指定 launchMode 的 activity 被任何对象任何地方调用，启动模式都一样；而 Intent 的 flags 只对 startActivity 的 activity 有效，其他调用同样的 activity 可以设置其他的启动模式，并不会相互影响。

## 参考文章：
1. [老生常谈-Activity](https://juejin.im/post/5adab7b6518825670c457de3)
2. [全面了解 Activity](https://juejin.im/entry/589847f7128fe10058ebd803)
3. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)
4. [Android之Activity系列总结(二)--任务和返回栈](https://www.cnblogs.com/jycboy/p/6367330.html)
5. [Android四大组件之Activity详解](https://www.cnblogs.com/caobotao/p/4987015.html)