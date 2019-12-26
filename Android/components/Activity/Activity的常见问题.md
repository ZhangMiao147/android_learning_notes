# Activity 的常见问题

## 1. 关于启动模式的问题

**问题 1 ：启动模式**

　　启动模式分为四种，标准模式、栈顶复用模式、栈内复用模式和单例模式。

　　标准模式，就是默认模式，启动一个 activity 就将新启动的 activity 入栈。

　　栈顶复用模式，启动的 activity 与栈顶的 activity 是一个类型，则不新建 activity 实例，直接复用栈顶的 activity，适用于通知栏打开界面。

　　栈内复用模式，如果启动的 activity 栈内已经存在，则不新建 activity 实例，将栈中 activity 之上的 activities 出栈，复用栈中的 activity，适用于根 activity。

　　单例模式，启动一个 activity，如果不存在栈包含 activity， 则新建栈，将 activitiy 入栈，如果存在则直接复用，适用相机等 activity ，其他应用也会调用。

　　除了标准模式，其他三种启动模式复用栈中的 activity 时，都会将新的 Intent 传递给 onNewIntent() 方法。

​	Intent 的 flag 有几个常用的启动模式：

　　FLAG_ACTIVITY_NEW_TASK：寻找与启动 activity 亲和性的栈，没有则建栈，activity 入栈，如果有，则将栈整体移动前台。

　　FLAG_ACTIVITY_NEW_DOCUMENT：与 FLAG_ACTIVITY_NEW_TASK 基本相同，不同的点在于 FLAG_ACTIVITY_NEW_DOCUEMENT 不寻找与 activity 亲和性的栈，直接新建栈；

　　FLAG_ACTIVITY_CLEAR_TOP：将栈中 activity 之上的 activities 全部出栈，如果启动模式是标准模式，会将 activity 出栈新建再入栈，其他模式则会复用栈中的 activity，并传递新的 intent 给 onNewIntent() 方法。

**问题 2 ：onNewIntent()  的调用时机**

　　启动一个 Activity 时，没有新建实例，而是复用任务中的 activity，会将新的 Intent 传递给复用的 activity 的 onNewIntent() 方法。

**问题 3 ：a-b-c 界面，其中b是 singleinstance 的，那么 c 界面点back 返回 a 界面，为什么？怎么管理栈的？**

　　singleinstance 启动模式会新建任务栈，b 就会在一个单独的栈中，而 a-c 是在一个栈中，在 b 跳转 c 的时候，b 就会销毁，b 所在的栈也销毁，所以 c 点击 back 返回后，栈顶是 a。

**问题 4 ：a 启动 b，b 启动 c,怎么样可以在 c 界面点 back 返回到 a ？**

　　一种方法就是上面的问题，将 b 的启动模式 launchMode 设置为 singleInstance。

　　另外一种方式就是通过 Intent 的 flag 来解决，设置 Intent 的 flag 为 FLAG_ACTIVITY_NO_HISTORY  也可以解决这个问题。

**问题 5 ：在 SingleTop 模式中，如果打开一个已经存在栈顶的 Activity，他的生命流程是怎样的？**

　　onPause() -> onNewIntent() -> onResume()

## 2. 关于启动流程的问题

**问题 1 ： Activity 的冷启动流程**

　　从 Launcher 的 onClick() 开始，调用 Activity 的 startActivity() 方法，进入到 ActivityThread 的 main() 方法，先创建了 Application 对象，然后启动 Activity 。

**问题 2 ： AMS 的作用**

　　AMS ( ActivityManagerService ) 负责了所有四大组件的管理，统一调度各应用进程；AMS 由 Binder 类派生，实现了 IActivityMananger 接口，客户端使用 ActivityManager 类，因为 AMS 是系统核心服务，很多 API 不能直接访问，需要通过 ActivityManager ，而 ActivityManager 内部通过 ActivityManagerNative 的 getDefault 方法得到一个 ActivityManangerProxy 代理对象，通过代理对象与 AMS 通信。

**问题 3 ：ams 是怎么找到启动的那个 activity 的？**

　　ActivityManagerService 有一个 ActivityStackSupervisor 变量，启动 Activity 时，会调用 ActivityStackSupervisor 的相应方法，在 ActivityStackSupervisor 的方法中根据 ActivityManagerService 传递的 ProcessRecord 信息找到对应的 ActivityStack 信息，ActivityStack 记录着已经启动的 Activity 的先后关系、状态信息等，从 ActivityStack 得到启动的 ActivityRecord 信息， ActivityRecord 记录 Activity 的状态以及其他的管理信息，然后将 ActivityRecord 的 intent 和其他信息传递给 ApplicationThread 的方法去进行启动操作。

## 3. 其他问题

**问题 1 ： 在oncreate里面可以得到view的宽高吗？**

　　不能，因为在 onCreate() 方法的时候，界面还没有显示出来，view 的宽高是不确定的。

**问题 2 ： Activity 的内部机制**

　　Activity 继承自 Context 对象，创建和生命周期由 Instrumentation 类来处理，而生命周期是由 ActivityManagerService 来管理和统一调度的。

