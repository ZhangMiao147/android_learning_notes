# ANR 解析

## 什么是 ANR
　　ANR 全称是 Application Not Responding，意思就是应用程序未响应。如果一个应用无法响应用户的输入，系统就会弹出一个 ANR 对话框，用户可以自行选择继续等待或者是停止当前程序。

## 出现场景
* 主线程被 IO 操作（从 4.0 之后网络 IO 不允许在主线程中）阻塞。
* 主线程中存在耗时的计算
* 主线程中错误的操作，比如 Thread.wait 或者 Thread.sleep 等。

　　Android 系统会监控程序的响应状况，一旦出现下面两种情况，则弹出 ANR 对话框：
* 应用在 5 秒内未响应用户的输入事件（如按键或者触摸）
* BroadcastReceiver 未在 10 秒内完成相关的处理

## 如何避免
　　基本的思路就是将 IO 操作在工作线程来处理，减少其他耗时操作和错误操作。
* 使用 AsyncTask 处理耗时 IO 操作。
* 使用 Thread 或者 HandlerThread 时，调用 Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)设置优先级，否则仍然会降低程序响应，因为默认 Thread 的优先级和主线程相同。
* 使用 Handler 处理工作线程结果，而不是使用 Thread.wait() 或者 Thread.sleep() 来阻塞主线程。
* Activity 的 onCreate 和 onResume 回调中尽量避免耗时的代码。
* BroadcastReceiver 中的 onReceive 代码也要尽量减少耗时，建议使用 Intentservice 处理。

## 画龙点睛
　　通常 100 到 200 毫秒就会让任察觉程序反应慢，为了更加提升响应，可以使用下面的几种方法：
* 如果程序正在后台处理用户的输入，建议使用让用户得知进度，比如使用 ProgressBar 控件。
* 程序启动时可以选择加上欢迎界面，避免让用户察觉卡顿。
* 使用 Systrace 和 TraceView 找到影响响应的问题。

## 如何定位
　　如果开发机器上出现问题，可以通过查看 /data/anr/traces.txt 即可，最新的 ANR 信息在最开始部分。

　　如果是线上版本引起的，Google Play 后台有相关的数据可以帮助查看分析并解决问题。

## 

## 参考文章
[说说 Android 中的 ANR](https://droidyue.com/blog/2015/07/18/anr-in-android/)
[Android ANR：原理分析及解决办法](https://www.jianshu.com/p/388166988cef)
[Android性能优化（七）之你真的理解ANR吗？](https://juejin.im/post/58e5bd6dda2f60005fea525c)

