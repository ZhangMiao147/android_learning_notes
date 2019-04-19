# Activity 知识之二：启动模式

## 启动模式简述

　　和生命周期一样， activity 的四种 launchMode 非常重要但也特别容易混淆，首先，activity 是以任务栈的形式创建和销毁的，栈是一种“后进先出”的数据结构，在默认情况下，启动第一个 actiivty 时，系统将会为它创建一个任务栈并将活动置于栈底，而从这个 activity 启动的其他 activity 将会依次入栈，当用户连续按下返回键时，任务栈中的 activity 会从栈顶开始依次销毁。

　　而启动模式就是定义 Activity 实例与 task 的关联方式。

#### 为什么需要定义启动模式？
　　任务栈有一个弊端，就是对于某些 activity 我们不希望它总要重新创建，比如，让某个 Activity 启动在一个新的 task 中，让 Activity 启动时只调用栈内已有的某个实例，或者当用户离开 task 时只想保留根 Activity ，并且清空 task 里面的其他 Activity ，这样的需求，使用默认的任务栈模式是无法实现的，这时就需要采用不同的启动模式。

## 关于栈
　　Android 中的 activity 全都归属于 task 管理（task 是一个具有栈结构的容器），task 是多个 activity 的集合，android 默认情况下会为每个 App 维持一个 task 来存放 App 的所有 activity (在默认情况下)，task 的默认 name 为该 app 的 packagename(包名)。

　　任务是指在执行特定作业时与用户交互的一系列 Activity 。这些 Activity 按照各自的打开顺序排列在堆栈中。

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

## launchMode 的四种启动模式

#### standard(标准模式)
　　Activity 的默认启动模式，不设置启动模式时，就是标准模式。只要启动 Activity 就会创建一个新实例，并将该 Activity 添加到当前任务栈中。一个任务栈可以拥有多个实例。

###### 标准模式的应用场景
　　正常打开一个新的页面，这种启动模式使用最多，最普通。

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

　　关于 launchMode 的四种模式验证，可以查看 [Activity四种launchMode的实践验证](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/%E5%9B%9B%E7%A7%8DlaunchMode%E9%AA%8C%E8%AF%81/Activity%E5%9B%9B%E7%A7%8DlaunchMode%E7%9A%84%E5%AE%9E%E8%B7%B5%E9%AA%8C%E8%AF%81.md) 文章。

## Intent 的 flags
　　Intent 的 flags 有 20 个，这里只列出常用的一些。查看全部的 flags ，请查看 [Intent的20个flags](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/Intent%E7%9A%8420%E4%B8%AAflags.md) 。

**常用的 flags 的使用**

　　操作中的 A,B,C 表示不同的 Activity，A->B 表示从 A 跳转到 B，栈情况中的 t1,t2 表示不同的栈，栈中的记录都是记录的从栈底到栈顶的顺序。

| 使用标志 | 解释 | 举例 | 其他 |
| -------- | -------- | -------- | -------- | -------- | -------- |
| FLAG_ACTIVITY_CLEAR_TOP | 如果设置此标签，activity 已经在栈中，则不会启动 activity 的新实例，而是将栈中 activity 之上的 activities 进行出栈关闭，Intent 将被传递给旧的 activity 作为新的 Intent 。 | C 跳转 B 的 flag 设置为 FLAG_ACTIVITY_CLEAR_TOP ，当前栈中（从栈底到栈顶）的情况是：A->B->C，然后 C 跳转 B，栈的情况就成了：A->B | 与 launchMode 的 SingleTask 相同 |


## onNewIntent() 方法与回调时机
　　onNewIntent() 方法会在 activity 复用的时候调用，也就是说调用 activity ，并不会常见 activity 的新实例，而是复用栈中的 activity ，复用时就会调用 onNewIntent() 方法，将新的 Intent 传递给 oNewIntent() 方法。

　　关于 onNewIntent() 方法的验证，可以查看 [onNewIntent方法的实践验证](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/components/Activity/onNewIntent%E6%96%B9%E6%B3%95%E7%9A%84%E9%AA%8C%E8%AF%81/onNewIntent%E6%96%B9%E6%B3%95%E7%9A%84%E5%AE%9E%E8%B7%B5%E9%AA%8C%E8%AF%81.md) 文章。

## TaskAffinity属性与allowTaskReparenting
　　Affinity 指示了 Activity 更倾向于属于哪个 Task。

　　默认情况下，同一个应用的 Activity 倾向于在同一个 task 中。可以通过 <activity> 标签的 taskAffinity 来修改这种行为。

## launchMode 与 Intent 的 flags 的对比
1. Intent 的 flags 的优先于要高于 launchMode 。
2. launchMode 设置的一些启动模式 Intent 的 flags 无法代替，Intent 的 flag 的一些使用 launchMode 也无法代替。
3. 指定 launchMode 的 activity 被任何对象热河地方调用，启动模式都一样；而 Intent 的 flags 只对 startActivity 的 activity 有效，其他调用同样的 activity 可以设置其他的启动模式，并不会相互影响。



## 参考文章：
1. [老生常谈-Activity](https://juejin.im/post/5adab7b6518825670c457de3)
2. [全面了解 Activity](https://juejin.im/entry/589847f7128fe10058ebd803)
3. [Activity 必知必会]https://juejin.im/post/5aef0d215188253dc612991b