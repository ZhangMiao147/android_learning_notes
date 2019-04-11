# Activity 知识之三：启动模式

## 启动模式简述

　　和生命周期一样， activity 的四种 launchMode 非常重要但也特别容易混淆，首先，activity 是以任务栈的形式创建和销毁的，栈是一种“后进先出”的数据结构，在默认情况下，启动第一个 actiivty 时，系统将会为它创建一个任务栈并将活动置于栈底，而从这个 activity 启动的其他 activity 将会依次入栈，当用户连续按下返回键时，任务栈中的 activity 会从栈顶开始依次销毁。
　　而启动模式就是定义 Activity 实例与 task 的关联方式。

#### 为什么需要定义启动模式？
　　任务栈有一个弊端，就是对于某些 activity 我们不希望它总要重新创建，比如，让某个 Activity 启动在一个新的 task 中，让 Activity 启动时只调用栈内已有的某个实例，或者当用户离开 task 时只想保留根 Activity ，并且清空 task 里面的其他 Activity ，这样的需求，使用默认的任务栈模式是无法实现的，这时就需要采用不同的启动模式。

## 关于栈
　　Android 中的 activity 全都归属于 task 管理（task 是一个具有栈结构的容器），task 是多个 activity 的集合，android 默认情况下会为每个 App 维持一个 task 来存放 App 的所有 activity (在默认情况下)，task 的默认 name 为该 app 的 packagename(包名)。

#### 开启一个 task
　　可以通过给 activity 一个 intent filter (action 是 “android.intent.action.MAIN”，category 是“android.intent.category.LAUNCHER”)，让这个 activity 是一个 task 的进入点。例如，我们最常见的 MainActivity：
```
        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
```
　　一个这样的 intent filter 会使得这个 activity 的 icon 和 label 显示在程序启动处，提供了一种方法，使得用户可以启动这个 activity，当它启动后，用户也可以通过它来返回到这个 task 。
　　用户必须能够离开一个 task ，然后通过 activity launcher 返回到它。
　　因为这个原因，两个让 Activity 永远实例化一个 task 的启动模式："singleTask" 和 "singleInstance"，应该仅在 Activity 有一个 ACTION_MAIN 和 CATEGORY_LAUNCHER filter 的时候用它们。

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
　　在 startActivity 时，设置 Intent 的 flag值，如下:
```
    Intent intent = new Intent(MainActivity.this, FirstActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
```

#### 注意
　　如果你同时在 AndroidManifest 中用 lauchMode 属性和代码中用 Intent 设置了 Activity 的启动模式，则会以代码中 Intent 的设置为准。

## launchMode 的 四种启动模式

#### standard(标准模式)
　　Activity 的默认启动模式，只要启动 Activity 就会创建一个新实例，并将该 Activity 添加到当前 Task 栈中。

###### 标准模式的使用场景

#### singleTop(栈顶复用)
　　在这种启动模式下，首先会判断要启动的活动是否已经存在于栈顶，如果是的话就不创建新实例，直接复用栈顶活动。如果要启动的活动不位于栈顶或在栈中或在栈中无实例，则会创建新实例入栈。

###### 栈顶复用模式的使用场景

#### singleTask(栈内复用)

　　这种模式比较复杂，是一种栈内单例模式，当一个 activity 启动时，会进行两次判断：
（1） 首先会寻找是否有这个活动需要的任务栈，如果没有就创建这个任务栈并将活动入栈，如果有的话就进入下一步判断。
（2） 第二次判断这个栈中是否存在该 activity 的实例，如果不存在就新建 activity 入栈，如果存在的话就直接复用，并且带有 clearTop 效果，会将该实例上方的所有活动全部出栈，令这个 activity 位于栈顶。
　　这种模式会保证 Activity 在所需要的栈内只有一个或者没有。

###### 栈内复用模式的使用场景

#### singleInstance(单例模式)
　　这种模式是真正的单例模式，以这种模式启动的活动会单独创建一个任务栈，并且依然遵循栈内复用的特性，保证了这个栈中只能存在这一个活动。并且系统不会在这个单例模式的 Activity 的实例所在 task 中启动任何其他的 Activity 。单例模式的 Activity 的实例永远是这个 task 中的唯一一个成员。

###### 单例模式的使用场景


　　关于 launchMode 的四种模式验证，可以查看 [Activity四种launchMode的实践验证](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%9B%9B%E7%A7%8DlaunchMode%E9%AA%8C%E8%AF%81/Activity%E5%9B%9B%E7%A7%8DlaunchMode%E7%9A%84%E5%AE%9E%E8%B7%B5%E9%AA%8C%E8%AF%81.md) 文章。

## Intent 的 flags
　　Intent 的 flags 有 20 个，这里只列出常用的一些。如果想看全部的 flags ，请查看 [Intent的20个flags](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Intent%E7%9A%8420%E4%B8%AAflags.md) 。

#### FLAG_ACTIVITY_CLEAR_TASK & FLAG_ACTIVITY_NEW_TASK

#### FLAG_ACTIVITY_CLEAR_TOP
　　如果设置此标签，activity 已经在栈中，则不会启动 activity 的新实例，而是将栈中 activity 之上的 activities 进行出栈关闭，Intent 将被传递给旧的 activity 作为新的 Intent 。

#### FLAG_ACTIVITY_CLEAR_TOP & FLAG_ACTIVITY_NEW_TASK
　　如果被使用给栈的根 activity ，activity 会成为前台 activity，并且将其清除到根状态。这是特别有用的，比如，从通知管理器开启 activity 。


#### FLAG_ACTIVITY_MULTIPLE_TASK & FLAG_ACTIVITY_NEW_TASK 与 FLAG_ACTIVITY_MULTIPLE_TASK & FLAG_ACTIVITY_NEW_DOCUMENT
　　FLAG_ACTIVITY_MULTIPLE_TASK 用于创建新的 task ，并在 task 中启动 activity，此 flag 经常与 FLAG_ACTIVITY_NEW_DOCUMENT  或者 FLAG_ACTIVITY_NEW_TASK 一起使用。单独使用 FLAG_ACTIVITY_NEW_DOCUMENT 或 FLAG_ACTIVITY_NEW_TASK 时，会先从存在的栈中搜索匹配 Intent 的栈 ，如果没有栈被发现则创建新的栈。当与 FLAG_ACTIVITY_MULTIPLE_TASK 配合使用时，会跳过搜索匹配的栈而是直接开启一个新栈。
　　如果使用了 FLAG_ACTIVITY_NEW_TASK 就不要使用此标签，除非你启动的是应用的 launcher 。

#### FLAG_ACTIVITY_NO_HISTORY
　　如果设置此 flag ，则新的 activity 将不会保留在历史栈中。一旦用户离开它，activity 将结束。也可以通过 `android.R.styleable#AndroidManifestActivity_noHistory noHistory` 属性设置。
　　如果设置此 flag ，当前的 activity 启动一个新的 activity ，新的 activity 设置返回值并结束，但旧的 activity 的 onActivityResult() 不会被触发。

#### FLAG_ACTIVITY_NO_ANIMATION
　　如果在 Context.startActivity 给 Intent 设置这个标签，将不会展示系统在 activity 从当前状态跳转到下一个 activity 的切换动画。
　　https://juejin.im/post/5aef0d215188253dc612991b


## onNewIntent() 方法与回调时机
　　onNewIntent() 方法会在 activity 复用的时候调用，就是说调用 activity ，但是在启动模式下，不会新建 activity ，而是复用栈中的 activity ，复用时就会调用 onNewIntent() 方法。

## TaskAffinity属性与allowTaskReparenting
　　Affinity 指示了 Activity 更倾向于属于哪个 Task。
　　默认情况下，同一个应用的 Activity 倾向于在同一个 task 中。可以通过 <activity> 标签的 taskAffinity 来修改这种行为。

## 参考文章：
1. [老生常谈-Activity](https://juejin.im/post/5adab7b6518825670c457de3)
2. [全面了解 Activity](https://juejin.im/entry/589847f7128fe10058ebd803)