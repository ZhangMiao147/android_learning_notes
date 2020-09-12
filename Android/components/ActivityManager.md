# ActivityManager

　　ActivityManager

# 前言

　　Activity可以获取运行中的应用信息，可以获取到servcie(服务),process（进程）,app（应用程序/包）,memory,Task（任务）信息等。



#### 获取信息

- ActivityManager.MemoryInfo 获取全局的内存使用信息
  MemoryInfo中重要的字段：availMem（系统可用内存），totalMem（总内存），threshold（低内存阈值，即低内存的临界线），lowMemory（是否为低内存状态）
- Debug.MemoryInfo 统计进程下的内存信息
  Debug.MemoryInfo主要用于获取进程下的内存信息。
- ActivityManager.RunningAppProcessInfo 获取运行进程信息
  封装运行进程的信息，相关字段：processName（进程名），pid（进程pid），uid（进程uid），pkgList（该进程下所有的包）。
- ActivityManager.RunningServiceInfo 封装了运行服务的信息
  用于封装运行的服务信息，但是其中除了服务进程信息外还有一些其它信息，activeSince（第一次被激活的时间、方式），foreground（服务是否在后台执行）。
- ActivityManager.RunningTaskInfo
  用于封装Task信息，包含id（该任务的唯一标识），baseActivity（该任务栈的基础Activity），topActivity（该任务栈栈顶的Activity），numActivities（该任务栈中Activity数量），description（任务当前状态描述）等。



#### ActivityManager常用方法

- clearApplicationUserData() ：用于清除用户数据，等同于在手机设置中清除用户数据。
- addAppTask (Activity activity, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) ：为Activity创建新的任务栈，activity（需要创建任务栈的Activity），intent（用于跳转页面的Intent），description（描述信息），thumbnail（缩略图）
- getDeviceConfigurationInfo () ：获取设备信息
- getLauncherLargeIconSize () ： 获取Launcher（启动器）图标大小
- getMemoryInfo (ActivityManager.MemoryInfo outInfo) ： 获取系统当前内存信息
- getProcessMemoryInfo（）：返回一个或者多个进程使用内存的情况
- getRunningAppProcesses() ：获取该设备上应用程序进程列表
- getAppTasks() ：获取当前应用任务列表
- isUserAMonkey() ：是否用户是一个猴子，用于判断键盘是否被乱按
- killBackgroundProcesses(String packageName) ：根据包名杀死对应进程
- getRunningTasks (int maxNum) ：获取正在运行的任务列表
- getRecentTasks (int maxNum, int flags) ：获取用户启动的任务列表
- getMyMemoryState (ActivityManager.RunningAppProcessInfo outState) ：获取该进程的全局内存状态



#### 判断应用是否在前台运行，应用是否在运行

```
//判断应用是否在前台运行
public boolean isRunningForeground(Context context){
        String packageName=getPackageName(context);
        String topActivityClassName=getTopActivityName(context);
        System.out.println("packageName="+packageName+",topActivityClassName="+topActivityClassName);
        if (packageName!=null&&topActivityClassName!=null&&topActivityClassName.startsWith(packageName)) {
            System.out.println("应用在前台执行");
            return true;
        } else {
            System.out.println("应用在后台执行");
            return false;
        }
    }

// 判断应用是否在运行
public boolean isRun(Context context,String mPackageName){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        //100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表示系统后台有此进程在运行
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(mPackageName) || info.baseActivity.getPackageName().equals(mPackageName)) {
                isAppRunning = true;
                Log.i("ActivityService",info.topActivity.getPackageName() + " info.baseActivity.getPackageName()="+info.baseActivity.getPackageName());
                break;
            }
        }
        if(isAppRunning){
            Log.i("ActivityService", "该程序正在运行");
        }else{
            Log.i("ActivityService", "该程序没有运行");
        }
        return isAppRunning;
}

//获取栈顶ActivityName
public  String getTopActivityName(Context context){
        String topActivityClassName=null;
         ActivityManager activityManager =
        (ActivityManager)(context.getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
         List<runningtaskinfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
         if(runningTaskInfos != null){
             ComponentName f=runningTaskInfos.get(0).topActivity;
             topActivityClassName=f.getClassName();
         }
         return topActivityClassName;
    }

    public String getPackageName(Context context){
         String packageName = context.getPackageName();  
         return packageName;
    }12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152
```



#### 自定义ActivityManager管理Activity

  我们需要定义一个自己的ActivityManager，并且在BaseActivity中的OnCreate方法里将启动的Activity通过我们自定义的ActivityManager把任务放入栈中，在onDestroy方法中将Activity退栈。

```java
/**
 * 用于管理Activity,获取Activity
 * 在结束一个activity后应该判断当前栈是否为空,为空则将本类引用置为null,以便于虚拟机回收内存
 * 单例,调用 {@link #getActivityManager()} 获取实例
 * 成员变量 {@link #mActivityStack} 应该与系统的回退栈保持一致,所以在启动activity的时候必须在其onCreate中
 * 将该activity加入栈顶,在activity结束时,必须在onDestroy中将该activity出栈
 */

public class ActivityManager {

    private static ReStack<Activity> mActivityStack;    //Activity栈
    private static ActivityManager mInstance;

    private ActivityManager() {
        mActivityStack = new ReStack<>();
    }

    /**
     * 获取ActivityManager的单例.
     *
     * @return ActivityManager实例
     */
    public static ActivityManager getActivityManager() {
        if (mInstance == null) {
            mInstance = new ActivityManager();
        }
        return mInstance;
    }

    /**
     * 添加一个activity到栈顶.
     *
     * @param activity 添加的activity
     */
    public void pushActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new ReStack<>();
        }
        mActivityStack.push(activity);
    }

    /**
     * 获取栈顶的Activity.
     *
     * @return 如果栈存在, 返回栈顶的activity
     */
    public Activity peekActivity() {
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            return mActivityStack.peek();
        } else {
            return null;
        }
    }

    /**
     * 结束当前的activity,在activity的onDestroy中调用.
     */
    public void popActivity() {
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            mActivityStack.pop();
        }
        //如果移除一个activity之后栈为空,将本类的引用取消,以便于让虚拟机回收
        if (mActivityStack != null && mActivityStack.isEmpty()) {
            mInstance = null;
        }
    }

    /**
     * 结束最接近栈顶的匹配类名的activity.
     * 遍历到的不一定是被结束的,遍历是从栈底开始查找,为了确定栈中有这个activity,并获得一个引用
     * 删除是从栈顶查找,结束查找到的第一个
     * 在activity外结束activity时调用
     *
     * @param klass 类名
     */
    public void popActivity(Class<? extends BaseActivity> klass) {
        for (Activity activity : mActivityStack) {
            if (activity != null && activity.getClass().equals(klass)) {
                activity.finish();
                break;              //只结束一个
            }
        }
    }

    //移除所有的Activity
    public void removeAll(){
        for (Activity activity : mActivityStack) {
            if (activity != null) {
                activity.finish();
                break;              
            }
        }
    }
}
```

# 参考文章

1. [ActivityManager](https://www.jianshu.com/p/1cbecf6cc136)
2. [ActivityManager解析及使用](https://blog.csdn.net/qq_38520096/article/details/82109948)
