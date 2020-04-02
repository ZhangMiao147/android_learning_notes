# 第 2 章 IPC 机制

## 2.1 Android IPC 简介

　　IPC 是 Inter-Process Communication 的缩写，含义为进程间通信或者跨进程通信，是指两个进程之间进行数据交换的过程。

\>> 按照操作系统中的描述，线程是CPU调度的最小单元，同时线程是一种有限的系统资源

\>> 而进程一般指一个执行单元，在PC和移动设备上指一个程序或者一个应用

\>> 一个进程可以包含多个线程，因此进程和线程是包含与被包含的关系

\>> 最简单的情况下，一个进程中可以只有一个线程，即主线程，在Android里面主线程也叫UI线程，在UI线程里才能操作界面元素

\>> 很多时候，一个进程中需要执行大量耗时的任务，如果这些任务放在主线程中去执行就会造成界面无法响应，严重影响用户体验，这种情况在PC系统和移动系统中都存在，在Android中有一个特殊的名字叫做ANR（Application Not Responding），即应用无响应。解决这个问题就需要用到线程，把一些耗时的任务放在线程中即可。

\>> IPC不是Android中所独有的，任何一个操作系统都需要有相应的IPC机制，比如Windows上可以通过剪贴板、管道和邮槽等来进行进程间通信；Linux上可以通过命名管道、共享内容、信号量等来进行进程间通信。可以看到不同的操作系统平台有着不同的进程间通信方式，对于Android来说，它是一种基于Linux内核的移动操作系统，它的进程间通信方式并不能完全继承自Linux，相反，它有自己的进程间通信方式

\>> 在Android中最有特色的进程间通信方式就是Binder了，通过Binder可以轻松地实现进程间通信。除了Binder, Android还支持Socket，通过Socket也可以实现任意两个终端之间的通信，当然同一个设备上的两个进程通过Socket通信自然也是可以的。

\>> 说到IPC的使用场景就必须提到多进程，只有面对多进程这种场景下，才需要考虑进程间通信。这个是很好理解的，如果只有一个进程在运行，又何谈多进程呢

\>> 多进程的情况分为两种。第一种情况是一个应用因为某些原因自身需要采用多进程模式来实现，至于原因，可能有很多，比如有些模块由于特殊原因需要运行在单独的进程中，又或者为了加大一个应用可使用的内存所以需要通过多进程来获取多份内存空间

\>> Android对单个应用所使用的最大内存做了限制，早期的一些版本可能是16MB，不同设备有不同的大小。另一种情况是当前应用需要向其他应用获取数据，由于是两个应用，所以必须采用跨进程的方式来获取所需的数据，甚至我们通过系统提供的ContentProvider去查询数据的时候，其实也是一种进程间通信，只不过通信细节被系统内部屏蔽了，我们无法感知而已

我的手机 2020/4/2 20:15:14

2.2 Android中的多进程模式

\>> 通过给四大组件指定android:process属性，我们可以轻易地开启多进程模式

\>> 有时候我们通过多进程得到的好处甚至都不足以弥补使用多进程所带来的代码层面的负面影响

\>> 2.2.1 开启多进程模式

\>> 首先，在Android中使用多进程只有一种方法，那就是给四大组件（Activity、Service、Receiver、ContentProvider）在AndroidMenifest中指定android:process属性，除此之外没有其他办法，也就是说我们无法给一个线程或者一个实体类指定其运行时所在的进程

\>> 其实还有另一种非常规的多进程方法，那就是通过JNI在native层去fork一个新的进程，但是这种方法属于特殊情况，也不是常用的创建多进程的方式

\>> 下面是一个示例，描述了如何在Android中创建多进程：

\>> 进程名为“com.ryg.chapter_2:remote”

\>> 进程名为“com.ryg.chapter_2.remote”

\>> 默认进程的进程名是包名

\>> 除了在Eclipse的DDMS视图中查看进程信息，还可以用shell来查看，命令为：adb shell ps或者adb shell ps | grep com.ryg.chapter_2。其中com.ryg.chapter_2是包名，如图2-2所示，通过ps命令也可以查看一个包名中当前所存在的进程信息。

\>> 分别为“:remote”和“com.ryg.chapter_2.remote”，那么这两种方式有区别吗？其实是有区别的，区别有两方面：首先，“:”的含义是指要在当前的进程名前面附加上当前的包名，这是一种简写的方法

\>> ，它是一种完整的命名方式，不会附加包名信息

\>> 其次，进程名以“:”开头的进程属于当前应用的私有进程，其他应用的组件不可以和它跑在同一个进程中，而进程名不以“:”开头的进程属于全局进程，其他应用通过ShareUID方式可以和它跑在同一个进程中。

\>> Android系统会为每个应用分配一个唯一的UID，具有相同UID的应用才能共享数据。这里要说明的是，两个应用通过ShareUID跑在同一个进程中是有要求的，需要这两个应用有相同的ShareUID并且签名相同才可以。在这种情况下，它们可以互相访问对方的私有数据，比如data目录、组件信息等，不管它们是否跑在同一个进程中。当然如果它们跑在同一个进程中，那么除了能共享data目录、组件信息，还可以共享内存数据，或者说它们看起来就像是一个应用的两个部分
20:16:55
我的手机 2020/4/2 20:16:55

2.2.2 多进程模式的运行机制

\>> Android为每一个应用分配了一个独立的虚拟机，或者说为每个进程都分配一个独立的虚拟机，不同的虚拟机在内存分配上有不同的地址空间，这就导致在不同的虚拟机中访问同一个类的对象会产生多份副本

\>> 所有运行在不同进程中的四大组件，只要它们之间需要通过内存来共享数据，都会共享失败，这也是多进程所带来的主要影响。正常情况下，四大组件中间不可能不通过一些中间层来共享数据，那么通过简单地指定进程名来开启多进程都会无法正确运行。当然，特殊情况下，某些组件之间不需要共享数据，这个时候可以直接指定android:process属性来开启多进程，但是这种场景是不常见的，几乎所有情况都需要共享数据。

\>> 一般来说，使用多进程会造成如下几方面的问题：

\>> （1）静态成员和单例模式完全失效。（2）线程同步机制完全失效。（3）SharedPreferences的可靠性下降。（4）Application会多次创建。

\>> 第3个问题是因为SharedPreferences不支持两个进程同时去执行写操作，否则会导致一定几率的数据丢失，这是因为SharedPreferences底层是通过读/写XML文件来实现的，并发写显然是可能出问题的，甚至并发读/写都有可能出问题

\>> 第4个问题也是显而易见的，当一个组件跑在一个新的进程中的时候，由于系统要在创建新的进程同时分配独立的虚拟机，所以这个过程其实就是启动一个应用的过程。因此，相当于系统又把这个应用重新启动了一遍，既然重新启动了，那么自然会创建新的Application。这个问题其实可以这么理解，运行在同一个进程中的组件是属于同一个虚拟机和同一个Application的，同理，运行在不同进程中的组件是属于两个不同的虚拟机和Application的

◆ 2.3 IPC基础概念介绍

\>> IPC中的一些基础概念，主要包含三方面内容：Serializable接口、Parcelable接口以及Binder

\>> Serializable和Parcelable接口可以完成对象的序列化过程，当我们需要通过Intent和Binder传输数据时就需要使用Parcelable或者Serializable。还有的时候我们需要把对象持久化到存储设备上或者通过网络传输给其他客户端，这个时候也需要使用Serializable来完成对象的持久化，

\>> 2.3.1 Serializable接口

\>> Serializable是Java所提供的一个序列化接口，它是一个空接口，为对象提供标准的序列化和反序列化操作。使用Serializable来实现序列化相当简单，只需要在类的声明中指定一个类似下面的标识即可自动实现默认的序列化过程。

\>> 在Android中也提供了新的序列化方式，那就是Parcelable接口，使用Parcelable来实现对象的序列号

\>> 想让一个对象实现序列化，只需要这个类实现Serializable接口并声明一个serialVersionUID即可，实际上，甚至这个serialVersionUID也不是必需的，我们不声明这个serialVersionUID同样也可以实现序列化，但是这将会对反序列化过程产生影响

\>> 通过Serializable方式来实现对象的序列化，实现起来非常简单，几乎所有工作都被系统自动完成了。如何进行对象的序列化和反序列化也非常简单，只需要采用ObjectOutputStream和ObjectInputStream即可轻松实现。

\>> 上述代码演示了采用Serializable方式序列化对象的典型过程，很简单，只需要把实现了Serializable接口的User对象写到文件中就可以快速恢复了，恢复后的对象newUser和user的内容完全一样，但是两者并不是同一个对象。

\>> 这个serialVersionUID是用来辅助序列化和反序列化过程的，原则上序列化后的数据中的serialVersionUID只有和当前类的serialVersionUID相同才能够正常地被反序列化

我的手机 2020/4/2 20:17:45

\> serialVersionUID的详细工作机制是这样的：序列化的时候系统会把当前类的serialVersionUID写入序列化的文件中（也可能是其他中介），当反序列化的时候系统会去检测文件中的serialVersionUID，看它是否和当前类的serialVersionUID一致，如果一致就说明序列化的类的版本和当前类的版本是相同的，这个时候可以成功反序列化；否则就说明当前类和序列化的类相比发生了某些变换，比如成员变量的数量、类型可能发生了改变，这个时候是无法正常反序列化的，因此会报如下错误：

\>> 一般来说，我们应该手动指定serialVersionUID的值，比如1L，也可以让Eclipse根据当前类的结构自动去生成它的hash值，这样序列化和反序列化时两者的serialVersionUID是相同的，因此可以正常进行反序列化

\>> 如果不手动指定serialVersionUID的值，反序列化时当前类有所改变，比如增加或者删除了某些成员变量，那么系统就会重新计算当前类的hash值并把它赋值给serialVersionUID，这个时候当前类的serialVersionUID就和序列化的数据中的serialVersionUID不一致，于是反序列化失败，程序就会出现crash

\>> serialVersionUID的作用，当我们手动指定了它以后，就可以在很大程度上避免反序列化过程的失败。比如当版本升级后，我们可能删除了某个成员变量也可能增加了一些新的成员变量，这个时候我们的反向序列化过程仍然能够成功，程序仍然能够最大限度地恢复数据，相反，如果不指定serialVersionUID的话，程序则会挂掉

\>> 如果类结构发生了非常规性改变，比如修改了类名，修改了成员变量的类型，这个时候尽管serialVersionUID验证通过了，但是反序列化过程还是会失败，因为类结构有了毁灭性的改变，根本无法从老版本的数据中还原出一个新的类结构的对象。

\>> 给serialVersionUID指定为1L或者采用Eclipse根据当前类结构去生成的hash值，这两者并没有本质区别，效果完全一样

\>> 静态成员变量属于类不属于对象，所以不会参与序列化过程；其次用transient关键字标记的成员变量不参与序列化过程。

\>> 另外，系统的默认序列化过程也是可以改变的，通过实现如下两个方法即可重写系统默认的序列化和反序列化过程
20:18:59
我的手机 2020/4/2 20:18:59

 2.3.2 Parcelable接口

\>> Parcelable也是一个接口，只要实现这个接口，一个类的对象就可以实现序列化并可以通过Intent和Binder传递。下面的示例是一个典型的用法。

\>> Parcel内部包装了可序列化的数据，可以在Binder中自由传输

\>> 在序列化过程中需要实现的功能有序列化、反序列化和内容描述

\>> 序列化功能由writeToParcel方法来完成，最终是通过Parcel中的一系列write方法来完成的；反序列化功能由CREATOR来完成，其内部标明了如何创建序列化对象和数组，并通过Parcel的一系列read方法来完成反序列化过程；内容描述功能由describeContents方法来完成，几乎在所有情况下这个方法都应该返回0，仅当当前对象中存在文件描述符时，此方法返回1。需要注意的是，在User(Parcel in)方法中，由于book是另一个可序列化对象，所以它的反序列化过程需要传递当前线程的上下文类加载器，否则会报无法找到类的错误

\>> 详细的方法说明请参看表2-1。

\>> 系统已经为我们提供了许多实现了Parcelable接口的类，它们都是可以直接序列化的，比如Intent、Bundle、Bitmap等，同时List和Map也可以序列化，前提是它们里面的每个元素都是可序列化的。

\>> 既然Parcelable和Serializable都能实现序列化并且都可用于Intent间的数据传递，那么二者该如何选取呢？Serializable是Java中的序列化接口，其使用起来简单但是开销很大，序列化和反序列化过程需要大量I/O操作。而Parcelable是Android中的序列化方式，因此更适合用在Android平台上，它的缺点就是使用起来稍微麻烦点，但是它的效率很高，这是Android推荐的序列化方式，因此我们要首选Parcelable

\>> Parcelable主要用在内存序列化上，通过Parcelable将对象序列化到存储设备中或者将对象序列化后通过网络传输也都是可以的，但是这个过程会稍显复杂，因此在这两种情况下建议大家使用Serializable。以上就是Parcelable和Serializable和区别。
 


我的电脑 2020/4/2 20:20:15


20:22:31
我的手机 2020/4/2 20:22:31

\> 2.3.3 Binder

\>> 直观来说，Binder是Android中的一个类，它继承了IBinder接口。从IPC角度来说，Binder是Android中的一种跨进程通信方式，Binder还可以理解为一种虚拟的物理设备，它的设备驱动是/dev/binder，该通信方式在Linux中没有；从Android Framework角度来说，Binder是ServiceManager连接各种Manager（ActivityManager、WindowManager，等等）和相应ManagerService的桥梁；从Android应用层来说，Binder是客户端和服务端进行通信的媒介，当bindService的时候，服务端会返回一个包含了服务端业务调用的Binder对象，通过这个Binder对象，客户端就可以获取服务端提供的服务或者数据，这里的服务包括普通服务和基于AIDL的服务。

\>> Android开发中，Binder主要用在Service中，包括AIDL和Messenger，其中普通Service中的Binder不涉及进程间通信，所以较为简单，无法触及Binder的核心，而Messenger的底层其实是AIDL，所以这里选择用AIDL来分析Binder的工作机制

\>> 所有可以在Binder中传输的接口都需要继承IInterface接口

\>> 当客户端发起远程请求时，由于当前线程会被挂起直至服务端进程返回数据，所以如果一个远程方法是很耗时的，那么不能在UI线程中发起此远程请求；其次，由于服务端的Binder方法运行在Binder的线程池中，所以Binder方法不管是否耗时都应该采用同步的方式去实现，因为它已经运行在一个线程中了

\>> Binder的工作机制图，如图2-5所示。

\>> AIDL文件并不是实现Binder的必需品。如果是我们手写的Binder，那么在服务端只需要创建一个BookManagerImpl的对象并在Service的onBind方法中返回即可。最后，是否手动实现Binder没有本质区别，二者的工作原理完全一样，AIDL文件的本质是系统为我们提供了一种快速实现Binder的工具，仅此而已。

\>> unlinkToDeath

\>> Binder中提供了两个配对的方法linkToDeath和unlinkToDeath，通过linkToDeath我们可以给Binder设置一个死亡代理，当Binder死亡时，我们就会收到通知，这个时候我们就可以重新发起连接请求从而恢复连接。

\>> 另外，通过Binder的方法isBinderAlive也可以判断Binder是否死亡。

我的手机 2020/4/2 20:24:27

2.4 Android中的IPC方式

\>> 2.4.1 使用Bundle

\>> 四大组件中的三大组件（Activity、Service、Receiver）都是支持在Intent中传递Bundle数据的，由于Bundle实现了Parcelable接口，所以它可以方便地在不同的进程间传输。基于这一点，当我们在一个进程中启动了另一个进程的Activity、Service和Receiver，我们就可以在Bundle中附加我们需要传输给远程进程的信息并通过Intent发送出去。当然，我们传输的数据必须能够被序列化，比如基本类型、实现了Parcellable接口的对象、实现了Serializable接口的对象以及一些Android支持的特殊对象，具体内容可以看Bundle这个类，就可以看到所有它支持的类型

\>> Bundle不支持的类型我们无法通过它在进程间传递数据

\>> 这是一种最简单的进程间通信方式，

\>> 2.4.2 使用文件共享

\>> 共享文件也是一种不错的进程间通信方式，两个进程通过读/写同一个文件来交换数据

\>> 在Windows上，一个文件如果被加了排斥锁将会导致其他线程无法对其进行访问，包括读和写，而由于Android系统基于Linux，使得其并发读/写文件可以没有限制地进行，甚至两个线程同时对同一个文件进行写操作都是允许的，尽管这可能出问题

\>> 通过文件交换数据很好使用，除了可以交换一些文本信息外，我们还可以序列化一个对象到文件系统中的同时从另一个进程中恢复这个对象

\>> 通过文件共享这种方式来共享数据对文件格式是没有具体要求的，比如可以是文本文件，也可以是XML文件，只要读/写双方约定数据格式即可

\>> 通过文件共享的方式也是有局限性的，比如并发读/写的问题，像上面的那个例子，如果并发读/写，那么我们读出的内容就有可能不是最新的，如果是并发写的话那就更严重了

\>> 因此我们要尽量避免并发写这种情况的发生或者考虑使用线程同步来限制多个线程的写操作

\>> 文件共享方式适合在对数据同步要求不高的进程之间进行通信，并且要妥善处理并发读/写的问题。

\>> 当然，SharedPreferences是个特例，众所周知，SharedPreferences是Android中提供的轻量级存储方案，它通过键值对的方式来存储数据，在底层实现上它采用XML文件来存储键值对，每个应用的SharedPreferences文件都可以在当前包所在的data目录下查看到。一般来说，它的目录位于/data/data/package name/shared_prefs目录下，其中package name表示的是当前应用的包名

\>> 从本质上来说，SharedPreferences也属于文件的一种，但是由于系统对它的读/写有一定的缓存策略，即在内存中会有一份SharedPreferences文件的缓存，因此在多进程模式下，系统对它的读/写就变得不可靠，当面对高并发的读/写访问，Sharedpreferences有很大几率会丢失数据，因此，不建议在进程间通信中使用SharedPreferences。

\>> 2.4.3 使用Messenger

\>> Messenger可以翻译为信使，顾名思义，通过它可以在不同进程中传递Message对象，在Message中放入我们需要传递的数据，就可以轻松地实现数据的进程间传递了

\>> Messenger是一种轻量级的IPC方案，它的底层实现是AIDL

\>> Messenger的使用方法很简单，它对AIDL做了封装，使得我们可以更简便地进行进程间通信。同时，由于它一次处理一个请求，因此在服务端我们不用考虑线程同步的问题，这是因为服务端中不存在并发执行的情形。

\>> Message中所支持的数据类型就是Messenger所支持的传输类型

\>> 实际上，通过Messenger来传输Message, Message中能使用的载体只有what、arg1、arg2、Bundle以及replyTo。Message中的另一个字段object在同一个进程中是很实用的，但是在进程间通信的时候，在Android 2.2以前object字段不支持跨进程传输，即便是2.2以后，也仅仅是系统提供的实现了Parcelable接口的对象才能通过它来传输。这就意味着我们自定义的Parcelable对象是无法通过object字段来传输的

\>> 非系统的Parcelable对象的确无法通过object字段来传输，这也导致了object字段的实用性大大降低，所幸我们还有Bundle, Bundle中可以支持大量的数据类型。

\>> 图2-6 Messenger的工作原理

\>> 2.4.4 使用AIDL

\>> Messenger是以串行的方式处理客户端发来的消息，如果大量的消息同时发送到服务端，服务端仍然只能一个个处理，如果有大量的并发请求，那么用Messenger就不太合适了

\>> Messenger的作用主要是为了传递消息，很多时候我们可能需要跨进程调用服务端的方法，这种情形用Messenger就无法做到了，但是我们可以使用AIDL来实现跨进程的方法调用

\>> AIDL也是Messenger的底层实现，因此Messenger本质上也是AIDL，只不过系统为我们做了封装从而方便上层的调用而已

\>> AIDL文件支持哪些数据类型呢？如下所示。· 基本数据类型（int、long、char、boolean、double等）；· String和CharSequence；· List：只支持ArrayList，里面每个元素都必须能够被AIDL支持；· Map：只支持HashMap，里面的每个元素都必须被AIDL支持，包括key和value；

\>> · Parcelable：所有实现了Parcelable接口的对象；· AIDL：所有的AIDL接口本身也可以在AIDL文件中使用。

\>> 以上6种数据类型就是AIDL所支持的所有类型，其中自定义的Parcelable对象和AIDL对象必须要显式import进来，不管它们是否和当前的AIDL文件位于同一个包内。

\>> 如果AIDL文件中用到了自定义的Parcelable对象，那么必须新建一个和它同名的AIDL文件，并在其中声明它为Parcelable类型。

\>> 除此之外，AIDL中除了基本数据类型，其他类型的参数必须标上方向：in、out或者inout, in表示输入型参数，out表示输出型参数，inout表示输入输出型参数，

\>> 要根据实际需要去指定参数类型，不能一概使用out或者inout，因为这在底层实现是有开销的。最后，AIDL接口中只支持方法，不支持声明静态常量，这一点区别于传统的接口。

\>> 为了方便AIDL的开发，建议把所有和AIDL相关的类和文件全部放入同一个包中，这样做的好处是，当客户端是另外一个应用时，我们可以直接把整个包复制到客户端工程中

\>> AIDL的包结构在服务端和客户端要保持一致，否则运行会出错，这是因为客户端需要反序列化服务端中和AIDL接口相关的所有类，如果类的完整路径不一样的话，就无法成功反序列化，程序也就无法正常运行

\>> Binder会把客户端传递过来的对象重新转化并生成一个新的对象

\>> RemoteCallbackList是系统专门提供的用于删除跨进程listener的接口。Remote-CallbackList是一个泛型，支持管理任意的AIDL接口，这点从它的声明就可以看出，因为所有的AIDL接口都继承自IInterface接口

\>> 它的工作原理很简单，在它的内部有一个Map结构专门用来保存所有的AIDL回调，这个Map的key是IBinder类型，value是Callback类型，如下所示。

\>> 其中Callback中封装了真正的远程listener。当客户端注册listener的时候，它会把这个listener的信息存入mCallbacks中，其中key和value分别通过下面的方式获得：

\>> 虽然说多次跨进程传输客户端的同一个对象会在服务端生成不同的对象，但是这些新生成的对象有一个共同点，那就是它们底层的Binder对象是同一个

\>> 同时RemoteCallbackList还有一个很有用的功能，那就是当客户端进程终止后，它能够自动移除客户端所注册的listener。另外，RemoteCallbackList内部自动实现了线程同步的功能，所以我们使用它来注册和解注册时，不需要做额外的线程同步工作。

\>> 使用RemoteCallbackList，有一点需要注意，我们无法像操作List一样去操作它，尽管它的名字中也带个List，但是它并不是一个List。遍历RemoteCallbackList，必须要按照下面的方式进行，其中beginBroadcast和beginBroadcast必须要配对使用，哪怕我们仅仅是想要获取RemoteCallbackList中的元素个数，这是必须要注意的地方。

\>> 客户端调用远程服务的方法，被调用的方法运行在服务端的Binder线程池中，同时客户端线程会被挂起，这个时候如果服务端方法执行比较耗时，就会导致客户端线程长时间地阻塞在这里，而如果这个客户端线程是UI线程的话，就会导致客户端ANR，这当然不是我们想要看到的。因此，如果我们明确知道某个远程方法是耗时的，那么就要避免在客户端的UI线程中去访问远程方法。由于客户端的onServiceConnected和onService Disconnected方法都运行在UI线程中，所以也不可以在它们里面直接调用服务端的耗时方法，这点要尤其注意

\>> 另外，由于服务端的方法本身就运行在服务端的Binder线程池中，所以服务端方法本身就可以执行大量耗时操作，这个时候切记不要在服务端方法中开线程去进行异步任务，除非你明确知道自己在干什么，否则不建议这么做

\>> Binder是可能意外死亡的，这往往是由于服务端进程意外停止了，这时我们需要重新连接服务

\>> 有两种方法，第一种方法是给Binder设置DeathRecipient监听，当Binder死亡时，我们会收到binderDied方法的回调，在binderDied方法中我们可以重连远程服务

\>> 另一种方法是在onServiceDisconnected中重连远程服务

\>> 这两种方法我们可以随便选择一种来使用，它们的区别在于：onServiceDisconnected在客户端的UI线程中被回调，而binderDied在客户端的Binder线程池中被回调。也就是说，在binderDied方法中我们不能访问UI，这就是它们的区别

\>> 在AIDL中进行权限验证，这里介绍两种常用的方法。

第一种方法，我们可以在onBind中进行验证，验证不通过就直接返回null，这样验证失败的客户端直接无法绑定服务，至于验证方式可以有多种，比如使用permission验证。使用这种验证方式，我们要先在AndroidMenifest中声明所需的权限，比如：

我的手机 2020/4/2 20:30:51

定义了权限以后，就可以在BookManagerService的onBind方法中做权限验证了，如下所示。

我的手机 2020/4/2 20:31:05

一个应用来绑定我们的服务时，会验证这个应用的权限，如果它没有使用这个权限，onBind方法就会直接返回null，最终结果是这个应用无法绑定到我们的服务，这样就达到了权限验证的效果，这种方法同样适用于Messenger中，读者可以自行扩展。
如果我们自己内部的应用想绑定到我们的服务中，只需要在它的AndroidMenifest文件中采用如下方式使用permission即可。

我的手机 2020/4/2 20:31:21

第二种方法，我们可以在服务端的onTransact方法中进行权限验证，如果验证失败就直接返回false，这样服务端就不会终止执行AIDL中的方法从而达到保护服务端的效果。至于具体的验证方式有很多，可以采用permission验证，具体实现方式和第一种方法一样。还可以采用Uid和Pid来做验证，通过getCallingUid和getCallingPid可以拿到客户端所属应用的Uid和Pid，通过这两个参数我们可以做一些验证工作，比如验证包名。

我的手机 2020/4/2 20:31:39

上面介绍了两种AIDL中常用的权限验证方法，但是肯定还有其他方法可以做权限验证，比如为Service指定android:permission属性等

2.4.5 使用ContentProvider

>> ContentProvider是Android中提供的专门用于不同应用间进行数据共享的方式，从这一点来看，它天生就适合进程间通信

>> 和Messenger一样，ContentProvider的底层实现同样也是Binder，由此可见，Binder在Android系统中是何等的重要

>> 虽然ContentProvider的底层实现是Binder，但是它的使用过程要比AIDL简单许多，这是因为系统已经为我们做了封装，使得我们无须关心底层细节即可轻松实现IPC

>> ContentProvider虽然使用起来很简单，包括自己创建一个ContentProvider也不是什么难事，尽管如此，它的细节还是相当多，比如CRUD操作、防止SQL注入和权限控制等

>> 系统预置了许多ContentProvider，比如通讯录信息、日程表信息等，要跨进程访问这些信息，只需要通过ContentResolver的query、update、insert和delete方法即可

>> 创建一个自定义的ContentProvider很简单，只需要继承ContentProvider类并实现六个抽象方法即可：onCreate、query、update、insert、delete和getType。这六个抽象方法都很好理解，onCreate代表ContentProvider的创建，一般来说我们需要做一些初始化工作；getType用来返回一个Uri请求所对应的MIME类型（媒体类型），比如图片、视频等，这个媒体类型还是有点复杂的，如果我们的应用不关注这个选项，可以直接在这个方法中返回null或者“*/*”；剩下的四个方法对应于CRUD操作，即实现对数据表的增删改查功能。根据Binder的工作原理，我们知道这六个方法均运行在ContentProvider的进程中，除了onCreate由系统回调并运行在主线程里，其他五个方法均由外界回调并运行在Binder线程池中

>> ContentProvider主要以表格的形式来组织数据，并且可以包含多个表，对于每个表格来说，它们都具有行和列的层次性，行往往对应一条记录，而列对应一条记录中的一个字段，这点和数据库很类似

>> 除了表格的形式，ContentProvider还支持文件数据，比如图片、视频等

>> 文件数据和表格数据的结构不同，因此处理这类数据时可以在ContentProvider中返回文件的句柄给外界从而让文件来访问ContentProvider中的文件信息

>> Android系统所提供的MediaStore功能就是文件类型的ContentProvider

>> 虽然ContentProvider的底层数据看起来很像一个SQLite数据库，但是ContentProvider对底层的数据存储方式没有任何要求，我们既可以使用SQLite数据库，也可以使用普通的文件，甚至可以采用内存中的一个对象来进行数据的存储

>> 注册这个BookProvider

>> 其中android:authorities是Content-Provider的唯一标识，通过这个属性外部应用就可以访问我们的BookProvider，因此，android:authorities必须是唯一的，这里建议读者在命名的时候加上包名前缀

>> ContentProvider的权限还可以细分为读权限和写权限，分别对应android:readPermission和android:writePermission属性，如果分别声明了读权限和写权限，那么外界应用也必须依次声明相应的权限才可以进行读/写操作，否则外界应用会异常终止

>> 通过ContentResolver对象的query方法去查询BookProvider中的数据，其中“content://com.ryg.chapter_2.book.provider”唯一标识了BookProvider，而这个标识正是我们前面为BookProvider的android:authorities属性所指定的值

>> 不能在onCreate中做耗时操作。

>> 可以使用UriMatcher的addURI方法将Uri和Uri_Code关联到一起

>> 通过ContentResolver的notifyChange方法来通知外界当前ContentProvider中的数据已经发生改变。要观察一个ContentProvider中的数据改变情况，可以通过ContentResolver的registerContentObserver方法来注册观察者，通过unregisterContentObserver方法来解除观察者

>> query、update、insert、delete四大方法是存在多线程并发访问的，因此方法内部要做好线程同步

>> SQLiteDatabase内部对数据库的操作是有同步处理的，但是如果通过多个SQLiteDatabase对象来操作数据库就无法保证线程同步，因为SQLiteDatabase对象之间无法进行线程同步

>> 如果ContentProvider的底层数据集是一块内存的话，比如是List，在这种情况下同List的遍历、插入、删除操作就需要进行线程同步，否则就会引起并发错误，这点是尤其需要注意的

>> ContentProvider除了支持对数据源的增删改查这四个操作，还支持自定义调用，这个过程是通过ContentResolver的Call方法和ContentProvider的Call方法来完成的

>> 2.4.6 使用Socket

>> Socket也称为“套接字”，是网络通信中的概念，它分为流式套接字和用户数据报套接字两种，分别对应于网络的传输控制层中的TCP和UDP协议

>> TCP协议是面向连接的协议，提供稳定的双向通信功能，TCP连接的建立需要经过“三次握手”才能完成，为了提供稳定的数据传输功能，其本身提供了超时重传机制，因此具有很高的稳定性

>> 而UDP是无连接的，提供不稳定的单向通信功能，当然UDP也可以实现双向通信功能

>> 在性能上，UDP具有更好的效率，其缺点是不保证数据一定能够正确传输，尤其是在网络拥塞的情况下

>> Socket本身可以支持传输任意字节流

>> 使用Socket来进行通信，有两点需要注意，首先需要声明权限：

>> 其次要注意不能在主线程中访问网络，因为这会导致我们的程序无法在Android 4.0及其以上的设备中运行，会抛出如下异常：android.os.NetworkOnMainThreadException

>> 而且进行网络操作很可能是耗时的，如果放在主线程中会影响程序的响应效率，从这方面来说，也不应该在主线程中访问网络

◆ 2.5 Binder连接池

>> 如何使用AIDL

>> 首先创建一个Service和一个AIDL接口，接着创建一个类继承自AIDL接口中的Stub类并实现Stub中的抽象方法，在Service的onBind方法中返回这个类的对象，然后客户端就可以绑定服务端Service，建立连接后就可以访问远程服务端的方法了。

>> 将所有的AIDL放在同一个Service中去管理。

>> 在这种模式下，整个工作机制是这样的：每个业务模块创建自己的AIDL接口并实现此接口，这个时候不同业务模块之间是不能有耦合的，所有实现细节我们要单独开来，然后向服务端提供自己的唯一标识和其对应的Binder对象；对于服务端来说，只需要一个Service就可以了，服务端提供一个queryBinder接口，这个接口能够根据业务模块的特征来返回相应的Binder对象给它们，不同的业务模块拿到所需的Binder对象后就可以进行远程方法调用了

>> 由此可见，Binder连接池的主要作用就是将每个业务模块的Binder请求统一转发到远程Service中去执行，从而避免了重复创建Service的过程，它的工作原理如图2-10所示。

## 2.6 选用合适的 IPC 方式

>> 通过表2-2，可以明确地看出不同IPC方式的优缺点和适用场景，那么在实际的开发中，只要我们选择合适的IPC方式就可以轻松完成多进程的开发场景。