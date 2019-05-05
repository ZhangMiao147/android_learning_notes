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


## 使用 Bind Service 完成 Service 和 Activity 之间的通信
　　可以使用 Bind Service 让 Activity 与 Service 建立关联。

#### Bind Service 的介绍
　　应用程序组件（客户端）通过调用 bindService（）方法能够绑定服务，然后 Android 系统会调用服务的 onBind() 回调方法，这个方法会返回一个跟服务器端交互的 Binder 对象。

　　这个绑定是异步的，bindService() 方法立即返回，并且不给客户端返回 IBinder 对象。要接收 IBinder 对象，客户端必须创建一个 ServiceConnection 类的实例，并且把这个实例传递给 bindService() 方法。ServiceConnection 对象包含了一个系统调用的传递 IBinder 对象的回调方法。

　　注意：只有 Activity、Service、Content Provider 能够绑定服务；BroadcastReceiver 广播接收器不能绑定服务。




## IntentService
　　服务不会自动开启线程，服务中的代码默认是运行在主线程中，如果直接在服务里执行一些耗时操作，容易造成 ANR(Application Not Responding)异常，为了可以简单的创建一个异步的、会自动停止的服务，Android 专门提供了一个 IntentService 类。可以启动 IntentService 多次，而每一个耗时操作会以工作队列的方式在 IntentService 的 onHandleIntent() 回调方法中执行，并且每次只会执行一个工作线程，执行完第一个，再执行第二个，以此类推。

#### IntentService 的使用


## start 服务与 bind 服务的区别

#### 区别一：生命周期
　　通过 started 方式的服务会一直运行在后台，需要由组件本身或外部组件来停止服务才会以结束结束。

　　bind 方式的服务，生命周期就会依赖绑定的组件。

#### 区别二：参数传递
　　started 服务可以给启动的服务对象传递参数，但无法获取服务中方法的返回值。

　　bind 服务可以给启动的服务对象传递参数，也可以用过绑定的业务对象获取返回结果。

#### 实际中使用
　　第一次先使用 started 方式来启动一个服务，之后可以使用 bind 的方式绑定服务，从而可以直接调用业务方法获取返回值。

## Service 的生命周期




## 参考文章
1.[Android组件系列----Android Service组件深入解析](https://www.cnblogs.com/smyhvae/p/4070518.html)


2.[Android编程开发Android Service详解](https://www.2cto.com/kf/201802/721726.html)