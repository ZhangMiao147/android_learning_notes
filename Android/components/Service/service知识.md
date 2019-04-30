# Service 知识

	本文内容：
	

## 1. Service 的基本概念
　　Service 是 Android 中实现程序后台运行的解决方案，非常适合用于去执行那些不需要和用户交互而且还要求长期运行的任务。不能运行在一个独立的进程当中，而是依赖于创建服务时所在的应用程序进程。只能在后台运行，并且可以和其他组件进行交互。

　　Service 可以在很多场合使用，比如播放多媒体的时候用户启动了其他 Activity，此时要在后台继续播放；比如检测 SD 卡上文件的变化；比如在后台记录你的地理位置的改变等等，总之服务是藏在后台的。

　　服务不会自动开启线程，需要在服务的内部手动创建子线程，并在子线程中执行具体的任务。

## 2. 定义一个 Service

#### 2.1 新建 MyService 类，继承 Service 类，实现抽象方法 onBind()，重写 onCreate()、onStartCommand()、onDestry() 方法。


#### 2.2 在 AndroidManifest.xml 文件中配置 MyService。


#### 2.3 在 MainActivity 中添加启动和停止 MyService。



## IntentService
　　服务不会自动开启线程，服务中的代码默认是运行在主线程中，如果直接在服务里执行一些耗时操作，容易造成 ANR(Application Not Responding)异常，为了可以简单的创建一个异步的、会自动停止的服务，Android 专门提供了一个 IntentService 类。可以启动 IntentService 多次，而每一个耗时操作会以工作队列的方式在 IntentService 的 onHandleIntent() 回调方法中执行，并且每次只会执行一个工作线程，执行完第一个，再执行第二个，以此类推。

#### IntentService 的使用


## start 服务与 bind 服务的区别

## Service 的生命周期




## 参考文章
1.[Android组件系列----Android Service组件深入解析](https://www.cnblogs.com/smyhvae/p/4070518.html)


2.[Android编程开发Android Service详解](https://www.2cto.com/kf/201802/721726.html)