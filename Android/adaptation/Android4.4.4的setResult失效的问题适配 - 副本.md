# Android 4.4.4 的 setResult 失效的问题适配

## 问题描述
　　当 AActivity 使用 startActivityForResult() 方法去启动 BActivity，当 BActivity 调用了 setResult() 方法返回数据给 AActivity，但是 AActivity 的 onActivityForResult() 没有被回调。并且发现在 AActivity 调用 BActivity 时候，就立刻回调了 AActivity 的 onActivityForResult() 方法。而 AAcivity 的启动模式是 standard，而 BActivity 的启动模式是 singleTask。

## 问题原因
　　在启动 BActivity 时，可以看到这样一句日志：
```
W/ActivityManager: Activity is launching as a new task, so cancelling activity result.
```
　　就是说 Activity 是作为新任务启动的，因此取消活动结果。

　　所以问题的原因就是 BActivity 的启动模式是 singleTask。

　　(疑问：singleTask 是栈内复用，不是开启新任务的，怎么也会被取消活动结果)

## 解决问题
　　由于问题是因为 launchMode 的问题引起的，所以解决方法也就从 launchMode 入手。

　　launchMode 有四个模式，standard（标准模式）、singletop(栈顶复用模式)、singleTask（栈内复用模式）、singleInstance(单例模式)。


　　不修改 AActivity 的 launchMode，将 BActivity 的启动模式修改为不创建新的任务的模式，即 standard 或者 singleTop，问题得到了解决。


