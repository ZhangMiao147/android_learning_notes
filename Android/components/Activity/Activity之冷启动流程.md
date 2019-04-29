# Activity 之冷启动流程

	 本文内容
	 1.冷启动与热启动
	 2.冷启动流程图
	 3.看源码分析冷启动流程

## 冷启动与热启动
　　所谓冷启动就是启动该应用时，后台没有该应用的进程，此时系统会创建一个进程分配给它，之后会创建和初始化 Application，然后通过反射执行 ActivityThread 中的 main 方法。而热启动则是，当启动应用的时候，后台已经存在该应用的进程，比如按 home 键返回主界面再打开该应用，此时会从已有的进程中来启动应用，这种方式下，不会重新走 Application 这一步。

## 冷启动流程图
![](./冷启动流程图.png)

　　图中设计的几个类：
（1）Launcher：Launcher 本质上也是一个应用程序，和一个简单的 App 一样，也继承自 Activity，实现了点击、长按等回调接口，来接收用户的输入。

（2）


## 看源码分析冷启动流程

　　在线查看源码地址：https://www.androidos.net.cn/sourcecode 


#### 1. 查看 Launcher 类

　　Launcher 类的地址：
https://www.androidos.net.cn/android/8.0.0_r4/xref/packages/apps/Launcher2/src/com/android/launcher2/Launcher.java

```
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener {
	...
}
```
　　可以看到 Launcher 继承自 Activity ，是默认启动应用程序。

#### 2. 在 Launcher 中找到点击应用快捷图标的点击事件
　　在 Launcher 类中有一个 onClick(View v)的方法，是点击快捷图标的点击事件：
```
    /**
     * Launches the intent referred by the clicked shortcut.
     * 启动单击的快捷方式引用的意图。
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        ...
            boolean success = startActivitySafely(v, intent, tag);
		...
    }
```

#### 3. 查看 startActivitySafely 方法
```
    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }
```

#### 4. 查看 startActivity() 方法
```
    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            UserHandle user = (UserHandle) intent.getParcelableExtra(ApplicationInfo.EXTRA_PROFILE);
            LauncherApps launcherApps = (LauncherApps)
                    this.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());
                if (user == null || user.equals(android.os.Process.myUserHandle())) {
                    // Could be launching some bookkeeping activity
                    startActivity(intent, opts.toBundle());
                } else {
                    launcherApps.startMainActivity(intent.getComponent(), user,
                            intent.getSourceBounds(),
                            opts.toBundle());
                }
            } else {
                if (user == null || user.equals(android.os.Process.myUserHandle())) {
                    startActivity(intent);
                } else {
                    launcherApps.startMainActivity(intent.getComponent(), user,
                            intent.getSourceBounds(), null);
                }
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }
```
　　调用了 Activity 的 startActivity() 方法。

#### 5. 查看 Activity 的 startActivity() 方法

```
    @Override
    public void startActivity(Intent intent) {
        this.startActivity(intent, null);
    }
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
	public void startActivityForResult(@RequiresPermission Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }


    public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
        if (mParent == null) {
            options = transferSpringboardActivityOptions(options);
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);
            if (ar != null) {
                mMainThread.sendActivityResult(
                    mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                    ar.getResultData());
            }
            if (requestCode >= 0) {
                // If this start is requesting a result, we can avoid making
                // the activity visible until the result is received.  Setting
                // this code during onCreate(Bundle savedInstanceState) or onResume() will keep the
                // activity hidden during this time, to avoid flickering.
                // This can only be done when a result is requested because
                // that guarantees we will get information back when the
                // activity is finished, no matter what happens to it.
                mStartedActivity = true;
            }

            cancelInputsAndStartExitTransition(options);
            // TODO Consider clearing/flushing other event sources and events for child windows.
        } else {
            if (options != null) {
                mParent.startActivityFromChild(this, intent, requestCode, options);
            } else {
                // Note we want to go through this method for compatibility with
                // existing applications that may have overridden it.
                mParent.startActivityFromChild(this, intent, requestCode);
            }
        }
    }


```










## 参考文章：
1. [Android 7.0应用冷启动流程分析](https://blog.csdn.net/dd864140130/article/details/60466394)
2. [Android APP 冷启动流程](https://www.jianshu.com/p/f5e79998cd66)
3. [Android爬坑之路（二十）App启动过程](https://baijiahao.baidu.com/s?id=1617072163535121555&wfr=spider&for=pc)
