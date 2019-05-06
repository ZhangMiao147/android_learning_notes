# Service 知识

	本文内容：
	

## 1. Service 的基本概念
　　Service 是 Android 中实现程序后台运行的解决方案，非常适合用于去执行那些不需要和用户交互而且还要求长期运行的任务。不能运行在一个独立的进程当中，而是依赖于创建服务时所在的应用程序进程。只能在后台运行，并且可以和其他组件进行交互。

　　Service 可以在很多场合使用，比如播放多媒体的时候用户启动了其他 Activity，此时要在后台继续播放；比如检测 SD 卡上文件的变化；比如在后台记录你的地理位置的改变等等，总之服务是藏在后台的。

　　服务不会自动开启线程，需要在服务的内部手动创建子线程，并在子线程中执行具体的任务。

　　Service 是一个一种可以在后台执行长时间运行操作而没有用户界面的应用组件。服务可由其他应用组件启动（Activity），服务一旦被启动将在后台一直运行，即使启动服务的组件（Activity）已销毁也不受影响。此外组件可以绑定到服务，以与之进行交互，甚至时执行进程间通信（IPC），例如服务可以处理网络事务，播放音乐，执行文件（I/O）或与内容提供程序交互，而所有这一切均可在后台运行。

## 3. 两种启动方式

#### 3.1 startService
　　当应用组件（如 Activity）通过调用 startService() 启动服务时，服务即处于“启动”状态。一旦启动，服务即可在后台无限期运行，即使启动服务的组件已被销毁也不受影响，除非手动调用才能停止服务，已启动的服务通常是执行单一操作，而且不会将结果返回给调用方。


#### 3.2 bindService
　　当应用组件通过调用 bindService() 绑定到服务时，服务即处于“绑定”状态。绑定服务提供了一个客户端-服务端接口，允许组件与服务进行交互、发送请求、获取结果，甚至是利用进程间通信（IPC）扩进程执行这些操作。仅当与另一个应用组件绑定时，绑定服务才会运行。多个组件可以同时绑定到该服务，但全部取消绑定后，该服务即会被销毁。



## 使用 Bind Service 完成 Service 和 Activity 之间的通信
　　可以使用 Bind Service 让 Activity 与 Service 建立关联。

#### Bind Service 的介绍
　　应用程序组件（客户端）通过调用 bindService（）方法能够绑定服务，然后 Android 系统会调用服务的 onBind() 回调方法，这个方法会返回一个跟服务器端交互的 Binder 对象。

　　这个绑定是异步的，bindService() 方法立即返回，并且不给客户端返回 IBinder 对象。要接收 IBinder 对象，客户端必须创建一个 ServiceConnection 类的实例，并且把这个实例传递给 bindService() 方法。ServiceConnection 对象包含了一个系统调用的传递 IBinder 对象的回调方法。

　　注意：只有 Activity、Service、Content Provider 能够绑定服务；BroadcastReceiver 广播接收器不能绑定服务。

#### Bind Service 的使用

###### onBind()

###### 解除绑定

###### 销毁 Service

## Service 的生命周期


## 2. 定义一个 Service

#### 2.1 新建 MyService 类，继承 Service 类，实现抽象方法 onBind()，重写 onCreate()、onStartCommand()、onDestry() 方法。


#### 2.2 在 AndroidManifest.xml 文件中配置 MyService。
````
<service android:enabled="["true"" android:exported="["true"" android:icon="drawable resource" android:isolatedprocess="["true"" android:label="string resource" android:name="string" android:permission="string" android:process="string">
    . . .
</service>
```
android:exported 代表是否能被其他应用隐式调用，其默认值是由 service 中有无 intent-filter 决定的，如果有 intent-filter，默认值 true，否则是 false。为 false 的情况下，即使有 intent-filter 匹配，也无法打开，即无法被其他应用隐式调用。

android:name 对应 Service 类名

android:permission 权限声明

android:process 是否需要在单独线程中运行，当设置为 android:process=":remote" 时，代表 Service 在单独的进程中进行，注意“:”很重要，它的意思是指要在当前进程名称前面附加上当前的包名，所以“remote”和“:remote”不是一个意思，前者的进程名称为“remote”，而后者的进程名称为“App-packageName:remote”。

android:isolatedProcess：设置 true 意味着，服务会在一个特殊的进程下运行，这个进程与系统其他进程分开且没有自己的权限。与其通信的唯一途径是通过服务的 API(bind and start)。

android:enabled 是否可以被系统实例化，默认为 true，因为父标签也有 enable 属性，所以必须两个都为默认值 true 的情况下才会被激活，否则不会激活。







#### 2.3 在 MainActivity 中添加启动和停止 MyService。

　　onCreate() 方法只会在 Service 第一次被创建的时候调用，而 onStartCommand() 方法在每次启动服务的时候都会调用。

#### onStartCommand()方法的返回值
　　onStartCommand() 方法执行时，返回的是一个 int 型。这个整型可以有三个返回值：START_NOT_STICKY、START_STICKY、START_REDELIVER_INTENT。
* START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完 onStartComment 方法后，服务被异常 kill 掉，系统不会自动重启该服务。

* START_STICKY：如果 Service 进程被 kill 掉，保留 Service 的状态为开始状态，但不保留递送的 intent 对象。随后系统会尝试重新创建 Service，由于服务状态为开始状态，所以创建服务后一定会调用 onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到 Service，那么参数 Intent 将为 null 。

* START_REDELIVER_INTENT：重传 Intent。使用这个返回值时，系统会自动重启该服务，并将 Intent 的值传入。

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

#### onBind()
　　当另一个组件想通过调用 bindService() 与服务绑定（例如执行 RPC）时，系统将调用此方法。在此方法实现中，必须返回一个 IBinder 接口的实现类，供客户端用来与服务进行通信。无论是启动状态还是绑定状态，此方法必须重写，但在启动状态喜爱直接返回 null 。

#### onCreate()
　　首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前），如果服务已在运行，则不会调用此方法，该方法只调用一次。

#### onStartCommand()
　　当另一个组件（Activity）通过调用 startService() 请求启动服务时系统将调用此方法，一旦执行此方法，服务即会启动并可在后台无限期运行。如果自己实现此方法，则需要在服务工作完成后，通过调用 stopSelf() 或 stopService() 来停止服务（在绑定状态无需实现此方法）。

#### onDestory()
　　当服务不在使用且被销毁时，系统将调用此方法，服务应该实现此方法来清理所有的资源，如线程、注册的监听器、接收器等，这是服务接受的最后一个调用。


## 参考文章
1.[Android组件系列----Android Service组件深入解析](https://www.cnblogs.com/smyhvae/p/4070518.html)


2.[Android编程开发Android Service详解](https://www.2cto.com/kf/201802/721726.html)