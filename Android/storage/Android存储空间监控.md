# Android 存储空间监控

## 前言

在 Android 系统是怎样对设备存储空间进行管理和监控的呢？

如果在使用 Android 手机时有过把 memory 填满或者即将填满的经历，也许会注意到在这种情况下手机的 Notifications 栏会有 “**Storage space running out**” 的通知。当点开该通知会发现 Setting–>**Storage** settings –>Device memory 下会有如下提示：Not enough**storage space**.

这个服务的实现是在 android/framework/base/services/java/com/android/server/**DeviceStorageMonitorService.java**。

DeviceStorageMonitorService 类实现了一个监控设备上存储空间的服务。如果设备的剩余存储空间小于某一个阀值（默认是存储空间的10%，这个值可以设置）时将会向用户发送剩余空间不足的警告，让用户释放一些空间。

## 源码分析

下面就分析一下 DeviceStorageMonitorService 这个类。

首先看一下该服务是如何被添加进来的。在 android/frameworks/base/services/java/com/android/server/**SystemServer.java** 中使用 ServiceManager.addService（）来添加系统服务:

```java
// 在 SystemServer 中添加 DSMS 服务：
try {
   Slog.i(TAG, “Device Storage Monitor”);
   ServiceManager.addService(DeviceStorageMonitorService.SERVICE,
   new DeviceStorageMonitorService(context));
} catch (Throwable e) {
   reportWtf(“starting DeviceStorageMonitor service”, e);
}
```

DSMS的构造函数的代码如下：

```java
  /**
  \* Constructor to run service. initializes the disk space threshold value
  \* and posts an empty message to kickstart the process.
  */
  public DeviceStorageMonitorService(Context context) {
    
    mLastReportedFreeMemTime = 0;
    mContext = context;
    mResolver = mContext.getContentResolver();

    //create StatFs object
    mDataFileStats = new StatFs(DATA_PATH.getAbsolutePath());  // 获取 Data 分区信息；
    mSystemFileStats = new StatFs(SYSTEM_PATH.getAbsolutePath()); // 获取 System 分区信息；
    mCacheFileStats = new StatFs(CACHE_PATH.getAbsolutePath()); // 获取 Cache 分区信息；


    //initialize total storage on device，初始化设备 总空间信息；
    mTotalMemory = (long)mDataFileStats.getBlockCount() *
            mDataFileStats.getBlockSize();

	 /* 
   创建 4 个 Intent，分别用于通知存储空间不足（ACTION_DEVICE_STORAGE_LOW）、
   存储空间回复正常（ACTION_DEVICE_STORAGE_OK）和存储空间满（ACTION_DEVICE_STORAGE_FULL）。
   由于每个 Intent 都设置了 FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT 标志，因此这三个 Intent 只
   能由注册了的 BroadcastReceiver 接收。
   */
    mStorageLowIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_LOW);
    mStorageLowIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);


    mStorageOkIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_OK);
    mStorageOkIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);


    mStorageFullIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_FULL);
    mStorageFullIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);


    mStorageNotFullIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_NOT_FULL);
    mStorageNotFullIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);

​  // cache storage thresholds

   /*
   查询 Seetings 数据库中 sys_storage_threshod_percentage 的值，**默认是 10**，即当 DATA_PATH
   目录下剩余空间少于其总空间的 10% 时，认为空间不足（ACTION_DEVICE_STORAGE_LOW）。
   */
    final StorageManager sm = StorageManager.from(context);
    **mMemLowThreshold** = sm.getStorageLowBytes(DATA_PATH);



  /*
   查询 Settings 数据库中的 sys_storage_full_threshold_bytes 的值，默认是 1MB，即当 DATA_PATH
   目录下剩余空间小于等于 1M 时，任务空间已满，剩余的部分是保留给系统使用的。
   */
    mMemFullThreshold = sm.getStorageFullBytes(DATA_PATH);

​    mMemCacheStartTrimThreshold = ((mMemLowThreshold*3)+mMemFullThreshold)/4;
​    mMemCacheTrimToThreshold = mMemLowThreshold
​        \+ ((mMemLowThreshold-mMemCacheStartTrimThreshold)*2);
​    mFreeMemAfterLastCacheClear = mTotalMemory;

   /*
   开始检查，存储空间；**
   */
    **checkMemory**(true);

​    mCacheFileDeletedObserver = new CacheFileDeletedObserver();
​    mCacheFileDeletedObserver.startWatching();
  }
```

下面再来看一下 checkMemory() 方法的实现。

```java
  private final void **checkMemory**(boolean checkCache) {
    //if the thread that was started to clear cache is still running do nothing till its
    //finished clearing cache. Ideally this flag could be modified by clearCache
    // and should be accessed via a lock but even if it does this test will fail now and
    //hopefully the next time this flag will be set to the correct value.

​    //如果线程 正在清除缓存 CACHE_PATH ，那么不进行空间检查；
​    if(mClearingCache) {
​      if(localLOGV) Slog.i(TAG, "Thread already running just skip");
​      //make sure the thread is not hung for too long
​      long diffTime = System.currentTimeMillis() - mThreadStartTime;
​      if(diffTime > (10*60*1000)) {
​        Slog.w(TAG, "Thread that clears cache file seems to run for ever");
​      }
​    } else {
      restatDataDir(); //重新计算3个分区的剩余空间大小；
      if (localLOGV) Slog.v(TAG, "freeMemory="+mFreeMem);

​      //post intent to NotificationManager to display icon if necessary
​      if (mFreeMem < mMemLowThreshold) {
​        if (checkCache) {
​          // We are allowed to clear cache files at this point to
​          // try to get down below the limit, because this is not
​          // the initial call after a cache clear has been attempted.
​          // In this case we will try a cache clear if our free
​          // space has gone below the cache clear limit.
​          if (mFreeMem < mMemCacheStartTrimThreshold) {
​            // We only clear the cache if the free storage has changed
​            // a significant amount since the last time.
​            if ((mFreeMemAfterLastCacheClear-mFreeMem)
​                \>= ((mMemLowThreshold-mMemCacheStartTrimThreshold)/4)) {
​              // See if clearing cache helps
​              // Note that clearing cache is asynchronous and so we do a
​              // memory check again once the cache has been cleared.
​              mThreadStartTime = System.currentTimeMillis();
​              mClearSucceeded = false;
​              **clearCache**(); // 如果剩余空间低于 mMemLowThreshold(10M)，先做一次缓存清理；
​            }
​          }
​        } else { // checkCache = false；
​          // This is a call from after clearing the cache. Note
​          // the amount of free storage at this point.
​          mFreeMemAfterLastCacheClear = mFreeMem;
​          if (!mLowMemFlag) {
​            // We tried to clear the cache, but that didn't get us
​            // below the low storage limit. Tell the user.
​            Slog.i(TAG, "Running low on memory. Sending notification");
​            **sendNotification**(); **//如果空间仍然低于 mMemLowThreshold, 发送广播并在状态来设置一个警告通知；**
​            mLowMemFlag = true;
​          } else {
​            if (localLOGV) Slog.v(TAG, "Running low on memory " +
​                "notification already sent. do nothing");
​          }
​        }
​      } else { //else **mFreeMem < mMemLowThreshold**
​        mFreeMemAfterLastCacheClear = mFreeMem;
​        if (mLowMemFlag) { // 如果剩余空间不小于 mMemLowThreshold，且已经设置了 mLowMemFlag，则取消空间不足广播。
​          Slog.i(TAG, "Memory available. Cancelling notification");
​          **cancelNotification**();
​          mLowMemFlag = false;
​        }
​      }
​      if (mFreeMem < mMemFullThreshold) {
​        if (!mMemFullFlag) {
​          **sendFullNotification**(); //如果空间已满，则发送空间已满的广播；
​          mMemFullFlag = true;
​        }
​      } else {
​        if (mMemFullFlag) {
​          **cancelFullNotification**(); **//如果空间不满且已经发送了空间已满的广播，则在此取消。**
​          mMemFullFlag = false;
​        }
​      }
​    }
​    if(localLOGV) Slog.i(TAG, "Posting Message again");
​    //keep posting messages to itself periodically
​    **postCheckMemoryMsg**(true, DEFAULT_CHECK_INTERVAL); **// DEFAULT_CHECK_INTERVAL 为 1 分钟，即每 1 分钟会触发一次检查；** 
  }



  // mLowMemFlag 和 mMemFullFlag 为是否发送了广播的标识。
  private final void **clearCache**() {

​    if (mClearCacheObserver == null) { //创建一个 CachePackageDataObserver 对象, 当 PMS 清理完空间时会回调该对象的 onRemoveCompleted 函数；
​      // Lazy instantiation
​      mClearCacheObserver = **new CachePackageDataObserver**();
​    }

​    mClearingCache = true; //设置 mClearingCache 的值为 true，表示我们正在清理空间； 
​    try {
​      if (localLOGV) Slog.i(TAG, "Clearing cache");
​      IPackageManager.Stub.asInterface(ServiceManager.getService("package")). //调用PMS的freeStorageAndNotify函数以清理空间；
​          **freeStorageAndNotify**(mMemCacheTrimToThreshold, mClearCacheObserver);
​    } catch (RemoteException e) {
​      Slog.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
​      mClearingCache = false;
​      mClearSucceeded = false;
​    }

  }

// CachePackageDataObserver 是 DSMS 定义的内部类，其 onRemoveCompleted 函数很简单，就是重新发送消息 DEVICE_MEMORY_WHAT  (postCheckMemoryMsg(false, 0);)，让 DSMS 再检测一次存储空间。如果剩余空间小于 10% **，则发送 sendNotification() 提示 storage space running out。

  class **CachePackageDataObserver** extends IPackageDataObserver.Stub {
    
​    public void **onRemoveCompleted**(String packageName, boolean succeeded) {
​      mClearSucceeded = succeeded;
​      mClearingCache = false;
​      if(localLOGV) Slog.i(TAG, " Clear succeeded:"+mClearSucceeded
​          +", mClearingCache:"+mClearingCache+" Forcing memory check");
​      **postCheckMemoryMsg**(false, 0);
​    }
  }
```

总结：
（1）首先在构造函数中，获取 data，system，cache 分区信息，然后注册四个 intent，分别为低内存，内存 ok，内存满，内存没有满四种情况。然后获取 settings 数据库里的 data 目录下剩余空间少于其总空间的百分比值，获取数据库中 data 目录下剩余空间的大小临界值（用于提示用户空间已满）。然后调用 checkMemory(true) 方法开始检查存储空间。
（2）在检查存储空间时，首先判断如果线程正在清除缓存 CACHE_PATH ，那么不进行空间检查。否则重新计算 3 个分区的剩余空间大小。如果剩余空间低于百分比 10%，如果需要做缓存清理（checkMemory 方法里面传递的参数是true，就是需要做缓存清理），先做一次缓存清理；清理完毕后会再次进行新一轮的 checkMemory()，如果剩余空间低于百分比 10% 并不用做缓存清理并且没有发通知，则发送通知告诉用户内部空间超出最低值 10%，如果此时空间百分比正常，但已发送通知，则将通知取消。同样的，如果空间已满，大于 full 的临界值，则发送空间已满的广播；空间不满且已经发送了空间已满的广播，则取消。最后会每 1 分钟会触发一次检查空间 checkMemory。

（3）清理完毕后会再次进行新一轮的 checkMemory()：做一下 onRemoveCompleted 动作，该动作发送检查空间的消息（postCheckMemoryMsg(false, 0);这里的 false 就是不再执行clearCache），然后 handle 处理该消息 DEVICE_MEMORY_WHAT，再次进入 checkMemory（false），发送通知告诉用户 storage space running out，空間剩 10% 時會出現该提示。

## 其他

使用一些命令可以快速充满内存，触发这个广播的发送：

* dd 充满设备分区
* df -h 查看分区大小

## 参考文章

1. [分析 Android 4.4.4 设备的存储空间监控](http://t.zoukankan.com/liulaolaiu-p-11744511.html)

