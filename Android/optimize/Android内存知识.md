# Android 内存知识

　　Android 应用层是由 java 开发的，Android 的 davlik 虚拟机与 jvm 也类似，只不过它是基于寄存器的。

　　在 java 中，通过 new 为对象分配内存，所有对象在 java 堆内分配空间，而内存的释放是由垃圾收集器（GC）来回收的。

　　Java 采用了有向图的原理来判断对象是否可以被回收。Java 将引用关系考虑为图的有向边，有向边从应用者指向引用对象。线程对象可以作为有向图的起始顶点，该图就是从起始顶点（GC roots）开始的一棵树，根顶点可以到达的对象都是有效对象，GC 不会回收这些对象。如果某个对象（连通子图）与这个根顶点不可达，那么认为这个对象不再被引用，可以被 GC 回收。

## 1. Android 的内存管理机制

　　Android 系统的 Dalvik 虚拟机扮演了常规的内存垃圾自动回收的角色，Android 系统没有为内存提供交换区，它使用 paging 与 memory-mapping(mmaping) 的机制来管理内存。

### 1.1. 共享内存

　　Android 系统通过下面几种方式来实现共享内存：
* Android 应用的进程都是从一个叫做 Zygote 的进程 fork 出来的。Zygote 进程在系统启动并且载入通用的 framework 的代码与资源之后开始启动。

  为了启动一个新的程序进程，系统会 fork Zygote 进程生成一个新的进程，然后在新的进程中加载并运行应用程序的代码。这使得大多数的 RAM pages 被用来分配给 framework 的代码，同时使得 RAM 资源能够在应用的所有进程之间进行共享。

* 大多数 static 的数据被 mmapped 到一个进程中。这不仅仅使得同样的数据能够在进程间进行共享，而且使得它能够在需要的时候被 paged out。

  常见的 static 数据包括 Dalvik Code、app resources、so 文件等。

* 大多数情况下，Android 通过显式的分配共享内存数据（例如 ashmem 或者 gralloc）来实现动态 RAM 区域能够在不同进程之间进行共享的机制。

  例如，Window Surface 在 App 与 Screen Compositor 之间使用共享的内存，Cursor Buffers 在 Content Provider 与 Clients 之间共享内存。

### 1.2. 分配与回收内存

* 每一个进程的 Dalvik heap 都反映了使用内存的占用范围。这就是通常逻辑意义上提到的 Davlik Heap Size，它可以随着需要进行增长，但是增长行为会有一个系统为它设置的上限。

* 逻辑上讲的 Heap Size 和实际物理意义上使用的内存大小是不对等的，Proportional Set Size(PSS) 记录了应用程序自身占用以及和其他进程进行共享的内存。

* Android 系统并不会对 Heap 中空闲内存区域做碎片整理。

  系统仅仅在新的内存分配之前判断 Heap 的尾端剩余空间是否足够，如果空间不够会触发 gc 操作，从而腾出更多空闲的内存空间。

  在 Android 的高级系统版本里面针对 Heap 空间有一个 Generational Heap Memory 的模型，最近分配的对象会存放在 Young Generation 区域，当这个对象在这个区域停留的时间达到一定程度，它会被移动到 Old Generation，最后累积一定时间再移动到 Permanent Generation 区域。

  系统会根据内存中不同的内存数据类型分别执行不同的 gc 操作。例如，刚分配到 Young Generation 区域的对象通常更容易被销毁回收，同时在 Young Generation 区域的 gc 操作速度会比 Old Generation 区域的 gc 操作速度更快。

　　每个 Generation 的内存区域都有固定的大小，随着新的对象陆续被分配到此区域，当这些对象总的大小快达到这一级别内存区域的阈值时，会触发 GC 的操作，以便腾出空间来存放其他新的对象。

　　通常情况下，GC 发生的时候，所有的线程都是会被暂停的。执行 GC 所占用的时间和它发生在哪一个 Generation 也有关系，Young Generation 中的每次 GC 操作时间是最短的，Old Generation 其次，Permanent Generation 最长。执行时间的长短也和当前 Generation 中的对象数量有关。

### 1.3. 限制应用的内存

* 为了整个 Android 系统的内存控制需要，Android 系统为每一个应用程序都设置了一个硬性的 Dalvik Heap Size 最大限制阈值，这个阈值在不同的设备上会因为 RAM 大小不同而各有差异。

  如果应用占用内存空间已经接近这个阈值，此时再尝试分配内存的话，很容易引起 OutOfMemoryError 的错误。

* ActivityManager.getMemoryClass() 可以用来查询当前应用的 Heap Size 阈值，这个方法会返回一个整数，表明应用的 Heap Size 阈值是多少 Mb(megabates)。

### 1.4. 应用切换操作

* Android 系统并不会在用户切换应用的时候做交换内存的操作。

  Android 会把那些不包含 Foreground 组件的应用进程放到 LRU Cache 中。

  例如，当用户开始开启了一个应用，系统会为它创建一个进程，但是当用户离开这个应用，此进程并不会立即被销毁，而是会被放到系统的 Cache 当中，如果用户后台再切换回到这个应用，此进程就能够被马上完整的恢复，从而实现应用的快速切换。

* 如果应用中有一个被缓存的进程，这个进程会占用一定的内存空间，它会对系统的整体性能有影响。

  因此当系统开始进入 Low Memory 的状态时，它会由系统根据 LRU 的规则、应用的优先级、内存占用情况以及其他因素的影响综合评估之后决定是否被杀死。

## 2. 什么是内存抖动
　　内存抖动是指在短时间内有大量的对象被创建或者被回收的现象。

　　内存抖动出现原因主要是频繁创建对象，导致大量对象在短时间内被创建，由于新对象要占用内存空间而且很频繁。

　　如果一次或者两次创建对象对内存影响不大，不会造成严重内存抖动，这样可以接受也不可避免，频繁的话就会内存抖动严重。

　　内存抖动的影响是：如果抖动很频繁，会导致垃圾回收机制频繁运行，短时间内产生大量对象，需要大量内存，而且还是频繁抖动，就可能会需要回收内存以用于产生对象，垃圾回收机制就自然会频繁运行了。

　　所以频繁内存抖动会导致垃圾回收频繁运行。


## 3. 参考文章
1. [Android 内存溢出和内存泄漏的区别](https://blog.csdn.net/u013435893/article/details/50608190)
2. [Android内存优化之OOM](http://hukai.me/android-performance-oom/)

