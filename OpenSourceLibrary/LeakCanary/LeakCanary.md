# Android - LeakCanary

# 1. 概述

- LeakCanary 是 Square 出品的基于 Android 平台的内存泄漏监测工具；
- `Activity` 和 `Fragment` 是自动监测，业务层有需要对特定对象进行内存泄漏监测，也可以自动调用相关方法；
- LeakCanary1.x 是基于 Java，LeakCanary2.x 是基于 Kotlin ，背后原理是类似的；
- [LeakCanry 官方网站](https://links.jianshu.com/go?to=%5Bhttps%3A%2F%2Fsquare.github.io%2Fleakcanary%2F%5D(https%3A%2F%2Fsquare.github.io%2Fleakcanary%2F))
- 源码基于 LeakCanary2.2 ；

# 2. 引入

- 在 2.x 版本中，引入只需要在`build.gradle`中`dependencies`

  中加入以下代码即可

  ```csharp
  // debugImplementation because LeakCanary should only run in debug builds.
  debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.2'
  ```

- 在 1.x 版本中，引入需要在`build.gradle`中`dependencies`中加入以下代码：

  ```bash
  debugImplementation 'com.squareup.leakcanary:leakcanary-android:1.5.4'
  releaseImplementation 'com.squareup.leakcanary:leakcanary-android-no-op:1.5.4'
  ```

  并且在 `Application#onCreate` 中初始化

  ```kotlin
  if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
  }
  LeakCanary.install(this);
  ```

# 3. 初始化

- 在 1.x 中需要在 `Application#onCreate` 中显式初始化；

- 在 2.x 中不需要，是因为初始化放在`ContentProvider`中

  ```kotlin
// ContentProvider 在 Application 创建前加载，LeakCanary就是在ConentProvider中初始化的
  internal sealed class AppWatcherInstaller : ContentProvider() {
  
    override fun onCreate(): Boolean {
    val application = context!!.applicationContext as Application
      InternalAppWatcher.install(application)
    return true
    }
}
  
fun install(application: Application) {
    InternalAppWatcher.application = application
  
    val configProvider = { AppWatcher.config }
    //对Activity监听
    ActivityDestroyWatcher.install(application, objectWatcher, configProvider)
    //对Fragment监听
    FragmentDestroyWatcher.install(application, objectWatcher, configProvider)
    //初始化
    onAppWatcherInstalled(application)
  }
  ```
  
  - ContentProvider 在创建 Application 之前加载，LeakCanary 在 ContentProvider 中初始化；
  - 初始化之前先对 `Activity` `Fragment` 绑定生命周期监听；
  - 初始化其实是在 `onAppWatcherInstalled(application)` 中进行的；
  
  ```kotlin
  override fun invoke(application: Application) {
    this.application = application
    //监测到有可达对象时的回调
    AppWatcher.objectWatcher.addOnObjectRetainedListener(this)
    //处理堆
    val heapDumper = AndroidHeapDumper(application, leakDirectoryProvider)
  //GC触发器
    val gcTrigger = GcTrigger.Default
    
    val configProvider = { LeakCanary.config }
  //开启线程处理 dump 文件
    val handlerThread = HandlerThread(LEAK_CANARY_THREAD_NAME)
  handlerThread.start()
    val backgroundHandler = Handler(handlerThread.looper)
  
    heapDumpTrigger = HeapDumpTrigger(
        application, backgroundHandler, AppWatcher.objectWatcher, gcTrigger, heapDumper,
        configProvider
    )
    //App前台后台监听
    application.registerVisibilityListener { applicationVisible ->
      this.applicationVisible = applicationVisible
      heapDumpTrigger.onApplicationVisibilityChanged(applicationVisible)
    }
    registerResumedActivityListener(application)
    addDynamicShortcut(application)
  
    disableDumpHeapInTests()
  }
  ```
  
  - 初始化

# 4. 监听

- LeakCanary 可以对任意对象监听内存泄漏；初始化是只是对所有的 `Activity` `Fragment` 对象做了自动监听；

- Activity 监听

  ```kotlin
internal object InternalAppWatcher {
    fun install(application: Application) {
      InternalAppWatcher.application = application
      val configProvider = { AppWatcher.config }
      //Activity 自动监听
      ActivityDestroyWatcher.install(application, objectWatcher, configProvider)
    }
  }
  
  internal class ActivityDestroyWatcher private constructor(
    private val objectWatcher: ObjectWatcher,
    private val configProvider: () -> Config
  ) {
  
    private val lifecycleCallbacks =
      object : Application.ActivityLifecycleCallbacks by noOpDelegate() {
        override fun onActivityDestroyed(activity: Activity) {
          if (configProvider().watchActivities) {
            objectWatcher.watch(
                //Activity 监听
                activity, "${activity::class.java.name} received Activity#onDestroy() callback"
            )
          }
        }
      }
  
    companion object {
      fun install(
        application: Application,
        objectWatcher: ObjectWatcher,
        configProvider: () -> Config
      ) {
        val activityDestroyWatcher =
          ActivityDestroyWatcher(objectWatcher, configProvider)
        //Activity 自动监听
        application.registerActivityLifecycleCallbacks(activityDestroyWatcher.lifecycleCallbacks)
      }
    }
  }
  ```
  
  - `Activity` 自动监听是通过 `Application#registerActivityLifecycleCallbacks` 绑定监听，应用内所有的 `Activity` 在 `onDestory` 时监听 `Activity` 对象；

- Fragment 监听

  ```kotlin
internal object InternalAppWatcher {
    fun install(application: Application) {
      InternalAppWatcher.application = application
  
      val configProvider = { AppWatcher.config }
      //自动监听Fragment
      FragmentDestroyWatcher.install(application, objectWatcher, configProvider)
    }
  }
  
  internal object FragmentDestroyWatcher {
    fun install(
    application: Application,
    objectWatcher: ObjectWatcher,
    configProvider: () -> AppWatcher.Config
    ) {
      val fragmentDestroyWatchers = mutableListOf<(Activity) -> Unit>()
      
      if (SDK_INT >= O) {
        SDK 大于等于 26，Android-SDK支持 Fragment 的声明周期回调
        fragmentDestroyWatchers.add(
            AndroidOFragmentDestroyWatcher(objectWatcher, configProvider)
        )
      }
  
      // androidx.fragment.app.Fragment 对应 leakcanary.internal.AndroidXFragmentDestroyWatcher
      getWatcherIfAvailable(
          ANDROIDX_FRAGMENT_CLASS_NAME,
          ANDROIDX_FRAGMENT_DESTROY_WATCHER_CLASS_NAME,
          objectWatcher,
          configProvider
      )?.let {
        fragmentDestroyWatchers.add(it)
      }
  
      //android.support.v4.app.Fragment 对应 leakcanary.internal.AndroidSupportFragmentDestroyWatcher
      getWatcherIfAvailable(
          ANDROID_SUPPORT_FRAGMENT_CLASS_NAME,
          ANDROID_SUPPORT_FRAGMENT_DESTROY_WATCHER_CLASS_NAME,
          objectWatcher,
          configProvider
      )?.let {
        fragmentDestroyWatchers.add(it)
      }
  
      if (fragmentDestroyWatchers.size == 0) {
        return
      }
  
      application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks by noOpDelegate() {
        override fun onActivityCreated(
          activity: Activity,
          savedInstanceState: Bundle?
        ) {
          //自动绑定监听
          for (watcher in fragmentDestroyWatchers) {
            watcher(activity)
          }
        }
      })
    }
  }
  ```
  
  - 因为历史版本问题，Fragment 声明周期监听存在多个版本，包括 Android-SDK 中的 `Fragment`，`androidx` 中的 `Fragment`，`support-v4` 中的 `Fragment`；
- 在 Fragment 自动监听时，如果当前系统版本大于等于 26，则添加 `Android-SDK` 中的 Fragment 生命周期监听，如果当前应用能加载到 `androidx` 中的 Fragment 类，则添加 `androix` 中的 Fragment 生命周期监听，如果当前应用能加载到 `support-v4` 中的 Fragment 类，则添加 `support-v4` 中的 Fragment 生命周期监听；
  - Fragment 依赖于 Activity，Fragment 监听依赖于 `FragmentManager` ；在 Activity 创建时，将 Fragment 生命周期监听注册进去；当 Fragment 的 `FragmentViewDestory` `FragmentDestory` 时，监听 `FragmentView` `Fragment` 对象；
  - Fragment 监听两种对象：Fragment对象本身和关联的View对象；
  
- 自定义监听

  ```css
AppWatcher.objectWatcher.watch(myDetachedView, "View was detached")
  ```
  
  - LeakCanary 也支持自定义监听，包括任何对象；包括 `Activity` `Fragment` 的监听，最后也是调用 `ObjectWatcher#watch` 方法；
- 自定义监听一定要在对象确实不再需要的时间才监听；尤其是业务层的对象，因为业务的调整或者开发人员的更替，很容易导致业务层的对象生命周期（死亡）的变更；最好是比较占用资源并且业务相对比较稳定的对象，才对其进行内存泄漏监听；
  
  ```kotlin
class ObjectWatcher constructor(
    private val clock: Clock,
  private val checkRetainedExecutor: Executor,
    private val isEnabled: () -> Boolean = { true }
  ) {
    // 监测对象仍然可达时的回调
    private val onObjectRetainedListeners = mutableSetOf<OnObjectRetainedListener>()
  
    //监测对象Map
    private val watchedObjects = mutableMapOf<String, KeyedWeakReference>()
    private val queue = ReferenceQueue<Any>()
  
    val hasRetainedObjects: Boolean
      @Synchronized get() {
        removeWeaklyReachableObjects()
        return watchedObjects.any { it.value.retainedUptimeMillis != -1L }
      }
  
    val retainedObjectCount: Int
      @Synchronized get() {
        removeWeaklyReachableObjects()
        return watchedObjects.count { it.value.retainedUptimeMillis != -1L }
      }
  
    val hasWatchedObjects: Boolean
      @Synchronized get() {
        removeWeaklyReachableObjects()
        return watchedObjects.isNotEmpty()
      }
  
    val retainedObjects: List<Any>
      @Synchronized get() {
        removeWeaklyReachableObjects()
        val instances = mutableListOf<Any>()
        for (weakReference in watchedObjects.values) {
          if (weakReference.retainedUptimeMillis != -1L) {
            val instance = weakReference.get()
            if (instance != null) {
              instances.add(instance)
            }
          }
        }
        return instances
      }
  
    @Synchronized fun addOnObjectRetainedListener(listener: OnObjectRetainedListener) {
      onObjectRetainedListeners.add(listener)
    }
  
    @Synchronized fun removeOnObjectRetainedListener(listener: OnObjectRetainedListener) {
      onObjectRetainedListeners.remove(listener)
    }
  
    @Synchronized fun watch(
      watchedObject: Any,
      description: String
    ) {
      if (!isEnabled()) {
        return
      }
      //删除Reference对象
      removeWeaklyReachableObjects()
      val key = UUID.randomUUID()
          .toString()
      val watchUptimeMillis = clock.uptimeMillis()
      //创建Reference对象
      val reference =
        KeyedWeakReference(watchedObject, key, description, watchUptimeMillis, queue)
      //添加到监听Map
      watchedObjects[key] = reference
      //在主线程，5s 后监测对象是否可达
      checkRetainedExecutor.execute {
        //监测对象是否可达
        moveToRetained(key)
      }
    }
  
    @Synchronized private fun moveToRetained(key: String) {
      removeWeaklyReachableObjects()
      val retainedRef = watchedObjects[key]
      if (retainedRef != null) {
        retainedRef.retainedUptimeMillis = clock.uptimeMillis()
        //监测到有可达对象时，通知回调，即
        onObjectRetainedListeners.forEach { it.onObjectRetained() }
      }
    }
  
  private fun removeWeaklyReachableObjects() {
      var ref: KeyedWeakReference?
      do {
        ref = queue.poll() as KeyedWeakReference?
        if (ref != null) {
          watchedObjects.remove(ref.key)
        }
      } while (ref != null)
    }
  }
  ```
  
  - `ObjectWatcher` 用来管理需要观察的对象，`Map` 存储 观察对象，key时调用 `watch` 时生成的随机数，value 是 `WeakReference` ；
  - 监测对象时，添加到Map中，并在主线程 5s 后通过 `ReferenceQueue` 来判断对象是否将要或者已经被回收；
  
```kotlin
  override fun onObjectRetained() {
    if (this::heapDumpTrigger.isInitialized) {
    heapDumpTrigger.onObjectRetained()
    }
}
  
  fun onObjectRetained() {
    scheduleRetainedObjectCheck(
        reason = "found new object retained",
        rescheduling = false
    )
  }
  
  private fun scheduleRetainedObjectCheck(
    reason: String,
    rescheduling: Boolean,
    delayMillis: Long = 0L
  ) {
    val checkCurrentlyScheduledAt = checkScheduledAt
    if (checkCurrentlyScheduledAt > 0) {
      val scheduledIn = checkCurrentlyScheduledAt - SystemClock.uptimeMillis()
      SharkLog.d { "Ignoring request to check for retained objects ($reason), already scheduled in ${scheduledIn}ms" }
      return
    } else {
      val verb = if (rescheduling) "Rescheduling" else "Scheduling"
      val delay = if (delayMillis > 0) " in ${delayMillis}ms" else ""
      SharkLog.d { "$verb check for retained objects${delay} because $reason" }
    }
    checkScheduledAt = SystemClock.uptimeMillis() + delayMillis
    backgroundHandler.postDelayed({
      checkScheduledAt = 0
      checkRetainedObjects(reason)
    }, delayMillis)
  }
  ```
  
  - 如果检测到有可达对象，通过回调，调用 `InternalLeakCanary#onObjectRetained` 最终调用 `HeapDumpTrigger#scheduleRetainedObjectCheck` 进入 Dump 阶段；

# 5. Dump

- 在真正进行 Dump 操作之前，还有很多前置条件判断；比如当前是否已经安排了 Dump 任务（有些 Dump 任务可能 Delay 安排），当前是否正在Debug，GC之后是否还有可达对象，上次Dump是否在 60s 之内（两次 Dump 间隔小于 60s 时，只是通知显示）；

- Dump

  ```kotlin
### HeapDumpTrigger
  private fun dumpHeap(
    retainedReferenceCount: Int,
    retry: Boolean
  ) {
    //Dump 文件
    val heapDumpFile = heapDumper.dumpHeap()
    lastDisplayedRetainedObjectCount = 0
    lastHeapDumpUptimeMillis = SystemClock.uptimeMillis()
    objectWatcher.clearObjectsWatchedBefore(heapDumpUptimeMillis)
    HeapAnalyzerService.runAnalysis(application, heapDumpFile)
  }
  
  ### AndroidHeapDumper
  override fun dumpHeap(): File? {
    val heapDumpFile = leakDirectoryProvider.newHeapDumpFile() ?: return null
  
      //真正执行Dump操作
      Debug.dumpHprofData(heapDumpFile.absolutePath)
  }
  ```
  
  - 真正执行 Dump 操作的是 `Debug#dumpHprofData` 方法；

- Analysis

  ```kotlin
### HeapAnalyzerService
  fun runAnalysis(
      context: Context,
      heapDumpFile: File
    ) {
      val intent = Intent(context, HeapAnalyzerService::class.java)
      intent.putExtra(HEAPDUMP_FILE_EXTRA, heapDumpFile)
      startForegroundService(context, intent)
    }
  
  ### HeapAnalyzerService
  override fun onHandleIntentInForeground(intent: Intent?) {
    if (intent == null || !intent.hasExtra(HEAPDUMP_FILE_EXTRA)) {
      SharkLog.d { "HeapAnalyzerService received a null or empty intent, ignoring." }
      return
    }
  
    // Since we're running in the main process we should be careful not to impact it.
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    val heapDumpFile = intent.getSerializableExtra(HEAPDUMP_FILE_EXTRA) as File
  
    val config = LeakCanary.config
    val heapAnalysis = if (heapDumpFile.exists()) {
      //分析 Dump 文件
      analyzeHeap(heapDumpFile, config)
    } else {
      missingFileFailure(heapDumpFile)
    }
    onAnalysisProgress(REPORTING_HEAP_ANALYSIS)
    config.onHeapAnalyzedListener.onHeapAnalyzed(heapAnalysis)
  }
  ```
  
  - 上面是分析 Dump 文件的发起和处理的入口；
- 最终 Dump 文件的分析是在 `HeapAnalyzer#analyze` 中进行，是在 Shark 库；有时间会单独讲；
  
- Notification & Activity

  - 最终展示给开发者的就是Notifiation & Activity，这里就不讲了，可以自行查看；

# 6. 参考文章

1. [Android-LeakCanary](https://www.jianshu.com/p/61860529ee1b)
