# JVM 知识的常见问题 2

# 1. JVM 内存模型的相关知识了解多少，比如重排序、内存屏障、happen-before、主内存、工作内存等。

　　内存屏障：为了保障执行顺序和可见性的一条 cpu 指令。

　　重排序：为了提高性能，编译器和处理器会对执行进行重排序。

　　happen-before：操作间执行的顺序关系。有些操作先发生。

　　主内存：共享变量存储的区域即是主内存。

　　工作内存：每个线程 copy 的本地内存，存储了该线程以读/写共享变量的副本。

## 1.1. 并发编程模型的分类

　　在并发编程中，我们需要处理两个关键问题：线程之间如何通信及线程之间如何同步（这里的线程是指并发执行的活动实体）。通信是指线程之间以何种机制来交换信息。在命令式编程中，线程之间的通信机制有两种：共享内存和消息传递。

　　在共享内存的并发模型里，线程之间共享程序的公共状态，线程之间通过写-读内存中的公共状态来隐式进行通信。在消息传递的并发模型里，线程之间没有公共状态，线程之间必须通过明确的发送消息来显式进行通信。

　　同步是指程序用于控制不同线程之间操作发生相对顺序的机制。在共享内存并发模型里，同步是显式进行的。程序员必须显式指定某个方法或某段代码需要在线程之间互斥执行。在消息传递的并发模型里，由于消息的发送必须在消息的接收之前，因此同步是隐式进行的。

　　Java 的并发采用的是共享内存模型，Java 线程之间的通信总是隐式进行，整个通信过程对程序员完全透明。如果编写多线程程序的 Java 程序员不理解隐式进行的线程之间通信的工作机制，很可能会遇到各种奇怪的内存可见性问题。

## 1.2. Java 内存模型的抽象

　　在 java 中，所有实例域、静态域和数组元素存储在堆内存中，堆内存在线程之间共享（本文使用 “ 共享变量 ” 这个术语代指实例域，静态域和数组元素）。局部变量（Local variables），方法定义参数（ java 语言规范称之为 formal method parameters）和异常处理器参数（exception handler parameters）不会在线程之间共享，它们不会有内存可见性问题，也不受内存模型的影响。

　　Java 线程之间的通信由 Java 内存模型（本文简称为 JMM）控制，JMM 决定一个线程对共享变量的写入何时对另一个线程可见。从抽象的角度来看，JMM 定义了线程和主内存之间的抽象关系：线程之间的共享变量存储在主内存（main memory）中，每个线程都有一个私有的本地内存（local memory），本地内存中存储了该线程以读/写共享变量的副本。本地内存是 JMM 的一个抽象概念，并不真实存在。它涵盖了缓存，写缓冲区，寄存器以及其他的硬件和编译器优化。

　　Java 内存模型的抽象示意图如下：
[![img](http://ifeve.com/wp-content/uploads/2013/01/113.png)](http://ifeve.com/wp-content/uploads/2013/01/113.png)
　　从上图来看，线程 A 与线程 B 之间如要通信的话，必须要经历下面 2 个步骤：

1. 首先，线程 A 把本地内存 A 中更新过的共享变量刷新到主内存中去。

2. 然后，线程 B 到主内存中去读取线程 A 之前已更新过的共享变量。
   下面通过示意图来说明这两个步骤：
   [![img](http://ifeve.com/wp-content/uploads/2013/01/221.png)](http://ifeve.com/wp-content/uploads/2013/01/221.png)
   如上图所示，本地内存 A 和 B 有主内存中共享变量 x 的副本。假设初始时，这三个内存中的 x 值都为0。线程 A 在执行时，把更新后的 x 值（假设值为 1）临时存放在自己的本地内存 A 中。当线程 A 和线程 B 需要通信时，线程 A 首先会把自己本地内存中修改后的 x 值刷新到主内存中，此时主内存中的 x 值变为了 1。随后，线程 B 到主内存中去读取线程 A 更新后的 x 值，此时线程 B 的本地内存的 x 值也变为了 1。

   从整体来看，这两个步骤实质上是线程 A 在向线程 B 发送消息，而且这个通信过程必须要经过主内存。JMM 通过控制主内存与每个线程的本地内存之间的交互，来为 java 程序员提供内存可见性保证。

## 1.3. 重排序

　　在执行程序时为了提高性能，编译器和处理器常常会对指令做重排序。

　　重排序分三种类型：

1. 编译器优化的重排序。编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。

2. 指令级并行的重排序。现代处理器采用了指令级并行技术（Instruction-Level Parallelism， ILP）来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。

3. 内存系统的重排序。由于处理器使用缓存和读 / 写缓冲区，这使得加载和存储操作看上去可能是在乱序执行。

   从 java 源代码到最终实际执行的指令序列，会分别经历下面三种重排序：
   [![img](http://ifeve.com/wp-content/uploads/2013/01/331.png)](http://ifeve.com/wp-content/uploads/2013/01/331.png)
   上述的 1 属于编译器重排序，2 和 3 属于处理器重排序。这些重排序都可能会导致多线程程序出现内存可见性问题。对于编译器，JMM 的编译器重排序规则会禁止特定类型的编译器重排序（ 不是所有的编译器重排序都要禁止 ）。对于处理器重排序，JMM 的处理器重排序规则会要求 java 编译器在生成指令序列时，插入特定类型的内存屏障（memory barriers，intel称之为memory fence）指令，通过内存屏障指令来禁止特定类型的处理器重排序（ 不是所有的处理器重排序都要禁止 ）。

   JMM 属于语言级的内存模型，它确保在不同的编译器和不同的处理器平台之上，通过禁止特定类型的编译器重排序和处理器重排序，为程序员提供一致的内存可见性保证。

## 1.4. 处理器重排序与内存屏障指令

　　现代的处理器使用写缓冲区来临时保存向内存写入的数据。写缓冲区可以保证指令流水线持续运行，它可以避免由于处理器停顿下来等待向内存写入数据而产生的延迟。同时，通过以批处理的方式刷新写缓冲区，以及合并写缓冲区中对同一内存地址的多次写，可以减少对内存总线的占用。虽然写缓冲区有这么多好处，但每个处理器上的写缓冲区，仅仅对它所在的处理器可见。这个特性会对内存操作的执行顺序产生重要的影响：处理器对内存的读/写操作的执行顺序，不一定与内存实际发生的读/写操作顺序一致！为了具体说明，请看下面示例：

| Processor A                                             | Processor B             |
| ------------------------------------------------------- | ----------------------- |
| a = 1; //A1 x = b; //A2                                 | b = 2; //B1 y = a; //B2 |
| 初始状态：a = b = 0 处理器允许执行后得到结果：x = y = 0 |                         |

　　假设处理器 A 和处理器 B 按程序的顺序并行执行内存访问，最终却可能得到 x = y = 0 的结果。具体的原因如下图所示：
![img](http://ifeve.com/wp-content/uploads/2013/01/441.png)

　　这里处理器 A 和处理器 B 可以同时把共享变量写入自己的写缓冲区（A1，B1），然后从内存中读取另一个共享变量（A2，B2），最后才把自己写缓存区中保存的脏数据刷新到内存中（A3，B3）。当以这种时序执行时，程序就可以得到 x = y = 0 的结果。

　　从内存操作实际发生的顺序来看，直到处理器 A 执行 A3 来刷新自己的写缓存区，写操作 A1 才算真正执行了。虽然处理器 A 执行内存操作的顺序为：A1->A2，但内存操作实际发生的顺序却是：A2->A1。此时，处理器 A 的内存操作顺序被重排序了（处理器 B 的情况和处理器 A 一样，这里就不赘述了）。

　　这里的关键是，由于写缓冲区仅对自己的处理器可见，它会导致处理器执行内存操作的顺序可能会与内存实际的操作执行顺序不一致。由于现代的处理器都会使用写缓冲区，因此现代的处理器都会允许对写-读操作重排序。

　　为了保证内存可见性，java 编译器在生成指令序列的适当位置会插入内存屏障指令来禁止特定类型的处理器重排序。JMM 把内存屏障指令分为下列四类：

| 屏障类型            | 指令示例                   | 说明                                                         |
| ------------------- | -------------------------- | ------------------------------------------------------------ |
| LoadLoad Barriers   | Load1; LoadLoad; Load2     | 确保 Load1 数据的装载，之前于 Load2 及所有后续装载指令的装载。 |
| StoreStore Barriers | Store1; StoreStore; Store2 | 确保 Store1 数据对其他处理器可见（刷新到内存），之前于 Store2 及所有后续存储指令的存储。 |
| LoadStore Barriers  | Load1; LoadStore; Store2   | 确保 Load1 数据装载，之前于 Store2及所有后续的存储指令刷新到内存。 |
| StoreLoad Barriers  | Store1; StoreLoad; Load2   | 确保 Store1 数据对其他处理器变得可见（指刷新到内存），之前于 Load2及所有后续装载指令的装载。StoreLoad Barriers 会使该屏障之前的所有内存访问指令（存储和装载指令）完成之后，才执行该屏障之后的内存访问指令。 |

　　StoreLoad Barriers 是一个 “ 全能型 ” 的屏障，它同时具有其他三个屏障的效果。现代的多处理器大都支持该屏障（其他类型的屏障不一定被所有处理器支持）。执行该屏障开销会很昂贵，因为当前处理器通常要把写缓冲区中的数据全部刷新到内存中（buffer fully flush）。

## 1.5. happens-before

　　从 JDK5 开始，java 使用新的 JSR -133 内存模型（本文除非特别说明，针对的都是 JSR- 133 内存模型）。JSR-133 使用 happens-before 的概念来阐述操作之间的内存可见性。在 JMM 中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在 happens-before 关系。这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。

　　与程序员密切相关的 happens-before 规则如下：

- 程序顺序规则：一个线程中的每个操作，happens- before 于该线程中的任意后续操作。
- 监视器锁规则：对一个监视器锁的解锁，happens- before 于随后对这个监视器锁的加锁。
- volatile变量规则：对一个 volatile 域的写，happens- before 于任意后续对这个 volatile 域的读。
- 传递性：如果 A happens- before B，且 B happens- before C，那么 A happens- before C。

　　注意，两个操作之间具有 happens-before 关系，并不意味着前一个操作必须要在后一个操作之前执行！happens-before 仅仅要求前一个操作（执行的结果）对后一个操作可见，且前一个操作按顺序排在第二个操作之前（the first is visible to and ordered before the second）。

　　happens-before 与 JMM 的关系如下图所示：

 [![img](http://ifeve.com/wp-content/uploads/2013/01/552.png)](http://ifeve.com/wp-content/uploads/2013/01/552.png)
　　如上图所示，一个 happens-before 规则通常对应于多个编译器和处理器重排序规则。对于 java 程序员来说，happens-before 规则简单易懂，它避免 java 程序员为了理解 JMM 提供的内存可见性保证而去学习复杂的重排序规则以及这些规则的具体实现。

## 1.6. 什么是 Memory Barrier（内存屏障）？

> 内存屏障，又称内存栅栏，是一个 CPU 指令，基本上它是一条这样的指令：
>  1、保证特定操作的执行顺序。
>  2、影响某些数据（或则是某条指令的执行结果）的内存可见性。

　　编译器和 CPU 能够重排序指令，保证最终相同的结果，尝试优化性能。插入一条 Memory Barrier 会告诉编译器和 CPU：不管什么指令都不能和这条 Memory Barrier 指令重排序。

　　Memory Barrier 所做的另外一件事是强制刷出各种 CPU cache，如一个 Write-Barrier（写入屏障）将刷出所有在 Barrier 之前写入 cache 的数据，因此，任何 CPU 上的线程都能读取到这些数据的最新版本。



![img](https:////upload-images.jianshu.io/upload_images/2184951-ad0094fa98e6cda0.png?imageMogr2/auto-orient/strip|imageView2/2/w/220/format/webp)

Memory Barrier.png

　　这和java有什么关系？volatile 是基于 Memory Barrier 实现的。

　　如果一个变量是 volatile 修饰的，JMM 会在写入这个字段之后插进一个 Write-Barrier 指令，并在读这个字段之前插入一个 Read-Barrier 指令。



![img](https:////upload-images.jianshu.io/upload_images/2184951-6b466ec6493b0a4f.png?imageMogr2/auto-orient/strip|imageView2/2/w/222/format/webp)

volatile.png

　　这意味着，如果写入一个 volatile 变量 a，可以保证：
 1、一个线程写入变量 a 后，任何线程访问该变量都会拿到最新值。
 2、在写入变量 a 之前的写入操作，其更新的数据对于其他线程也是可见的。因为 Memory Barrier 会刷出 cache 中的所有先前的写入。

## 1.7. 抽象结构

> java 线程之间的通信由 java 内存模型（JMM）控制，JMM 决定一个线程对共享变量（实例域、静态域和数组）的写入何时对其它线程可见。

　　从抽象的角度来看，JMM 定义了线程和主内存 Main Memory（堆内存）之间的抽象关系：线程之间的共享变量存储在主内存中，每个线程都有自己的本地内存 Local Memory（只是一个抽象概念，物理上不存在），存储了该线程的共享变量副本。

　　所以，线程 A 和线程 B 之前需要通信的话，必须经过一下两个步骤：

1. 线程 A 把本地内存中更新过的共享变量刷新到主内存中。
2. 线程 B 到主内存中读取线程 A 之前更新过的共享变量。

# 2. 简单说说你了解的类加载器，可以打破双亲委派吗，怎么打破

深入理解 Java 虚拟机 第 7 章

类加载器的分类（bootstrap,ext,app,curstom），类加载的流程(load-link-init)

http://blog.csdn.net/gjanyanlig/article/details/6818655/

## 2.1. 类的加载过程

　　JVM将类加载过程分为三个步骤：装载（Load），链接（Link）和初始化(Initialize)，链接又分为三个步骤，如下图所示：

![img](http://hi.csdn.net/attachment/201109/25/0_131691377413Tr.gif)

1) 装载：查找并加载类的二进制数据；

2)链接：

> 验证：确保被加载类的正确性；
>
> 准备：为类的静态变量分配内存，并将其初始化为默认值；
>
> 解析：把类中的符号引用转换为直接引用；

3)初始化：为类的静态变量赋予正确的初始值；

　　那为什么我要有验证这一步骤呢？首先如果由编译器生成的class文件，它肯定是符合JVM字节码格式的，但是万一有高手自己写一个class文件，让JVM加载并运行，用于恶意用途，就不妙了，因此这个class文件要先过验证这一关，不符合的话不会让它继续执行的，也是为了安全考虑吧。

　　准备阶段和初始化阶段看似有点牟盾，其实是不牟盾的，如果类中有语句：private static int a = 10，它的执行过程是这样的，首先字节码文件被加载到内存后，先进行链接的验证这一步骤，验证通过后准备阶段，给a分配内存，因为变量a是static的，所以此时a等于int类型的默认初始值0，即a=0，然后到解析（后面在说），到初始化这一步骤时，才把a的真正的值10赋给a,此时a=10。

**2. 类的初始化**

　　类什么时候才被初始化：

> 1）创建类的实例，也就是new一个对象

> 2）访问某个类或接口的静态变量，或者对该静态变量赋值
>
> 3）调用类的静态方法
>
> 4）反射（Class.forName("com.lyj.load")）

> 5）初始化一个类的子类（会首先初始化子类的父类）
>
> 6）JVM 启动时标明的启动类，即文件名和类名相同的那个类

　　只有这 6 种情况才会导致类的类的初始化。

　　类的初始化步骤：

1. 如果这个类还没有被加载和链接，那先进行加载和链接

2. 假如这个类存在直接父类，并且这个类还没有被初始化（注意：在一个类加载器中，类只能初始化一次），那就初始化直接的父类（不适用于接口）

3. 加入类中存在初始化语句（如 static 变量和 static 块），那就依次执行这些初始化语句。

**3.类的加载**

　　类的加载指的是将类的 .class 文件中的二进制数据读入到内存中，将其放在运行时数据区的方法区内，然后在堆区创建一个这个类的 java.lang.Class 对象，用来封装类在方法区类的对象。看下面 2 图

![img](http://hi.csdn.net/attachment/201009/25/0_1285381395C6iW.gif)

![img](http://hi.csdn.net/attachment/201109/25/0_1316916841uQvx.gif)

　　类的加载的最终产品是位于堆区中的 Class 对象。

　　Class 对象封装了类在方法区内的数据结构，并且向 Java 程序员提供了访问方法区内的数据结构的接口。

　　加载类的方式有以下几种：

>  1）从本地系统直接加载
>
> 2）通过网络下载 .class 文件
>
> 3）从 zip，jar 等归档文件中加载 .class 文件
>
> 4）从专有数据库中提取 .class 文件
>
> 5）将 Java 源文件动态编译为 .class 文件（服务器）

**4.加载器**

**来自http://blog.csdn.net/cutesource/article/details/5904501**

　　JVM 的类加载是通过 ClassLoader 及其子类来完成的，类的层次关系和加载顺序可以由下图来描述：

![img](http://hi.csdn.net/attachment/201009/25/0_1285421756PHyZ.gif)

1）Bootstrap ClassLoader

　　负责加载$JAVA_HOME中jre/lib/rt.jar 里所有的 class，由 C++ 实现，不是 ClassLoader 子类。

2）Extension ClassLoader

　　负责加载 java 平台中扩展功能的一些 jar 包，包括$JAVA_HOME中jre/lib/*.jar或-Djava.ext.dirs指定目录下的jar包

3）App ClassLoader

　　负责加载 classpath 中指定的 jar 包及目录中 class。

4）Custom ClassLoader

　　属于应用程序根据自身需要自定义的 ClassLoader，如 tomcat、jboss都会根据 j2ee 规范自行实现 ClassLoader

　　加载过程中会先检查类是否被已加载，检查顺序是自底向上，从Custom ClassLoader到BootStrap ClassLoader逐层检查，只要某个 classloader 已加载就视为已加载此类，保证此类只所有 ClassLoader 加载一次。而加载的顺序是自顶向下，也就是由上层来逐层尝试加载此类。

# 3. 讲讲 JAVA 的反射机制。

　　Java 程序在运行状态可以动态的获取类的所有属性和方法，并实例化该类，调用方法的功能。

1. JAVA 反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法。

2. 对于任意一个对象，都能够调用它的任意方法和属性。

3. 这种动态获取信息以及动态调用对象方法的功能称为 java 语言的反射机制。

# 4. Java 类加载时机与加载过程

https://www.cnblogs.com/fnlingnzb-learner/p/11990943.html

https://blog.csdn.net/justloveyou_/article/details/72466105

# 5. Java 类加载的方式

## 5.1. 三种类加载方式

1. 由 new 关键字创建一个类的实例（静态加载）

   在由运行时刻用 new 方法载入

   如：Dog dog ＝ new Dog（）；

2. 调用 Class.forName() 方法
   通过反射加载类型，并创建对象实例
   如：Class clazz ＝ Class.forName（“Dog”）；
   Object dog ＝clazz.newInstance（）；

3. 调用某个 ClassLoader 实例的 loadClass() 方法
   通过该 ClassLoader 实例的 loadClass() 方法载入。应用程序可以通过继承 ClassLoader 实现自己的类装载器。
   如：Class clazz ＝ classLoader.loadClass（“Dog”）；
   Object dog ＝clazz.newInstance（）；

## 5.2. 三者的区别

　　1 和 2 使用的类加载器是相同的，都是当前类加载器。（即：this.getClass.getClassLoader）。
　　3 由用户指定类加载器。如果需要在当前类路径以外寻找类，则只能采用第 3 种方式。第 3 种方式加载的类与当前类分属不同的命名空间。
　　另外：1 是静态加载，2、3 是动态加载。

　　两个异常(exception)：

- 静态加载的时候如果在运行环境中找不到要初始化的类，抛出的是 NoClassDefFoundError，它在 JAVA 的异常体系中是一个Error。
- 动态态加载的时候如果在运行环境中找不到要初始化的类，抛出的是 ClassNotFoundException，它在 JAVA 的异常体系中是一个 checked 异常。

## 5.3. Class.forName与ClassLoader.loadClass 区别

　　Class 的装载包括 3 个步骤：加载（loading）,连接（link）,初始化（initialize）。

　　Class.forName(className) 实际上是调用 Class.forName(className, true, this.getClass().getClassLoader())。第二个参数，是指 Class 被 loading 后是不是必须被初始化。
ClassLoader.loadClass(className) 实际上调用的是 ClassLoader.loadClass(name, false)，第二个参数指 Class 是否被 link。

　　Class.forName(className) 装载的 class 已经被初始化，而 ClassLoader.loadClass(className) 装载的 class 还没有被 link。一般情况下，这两个方法效果一样，都能装载 Class。但如果程序依赖于 Class 是否被初始化，就必须用 Class.forName(name) 了。

　　对于相同的类，JVM 最多会载入一次。但如果同一个 class 文件被不同的 ClassLoader 载入，那么载入后的两个类是完全不同的。因为已被加载的类由该类的类加载器实例与该类的全路径名的组合标识。设有 packagename.A Class ，分别被类加载器 CL1 和 CL2 加载，所以系统中有两个不同的 java.lang.Class 实例： <CL1, packagename.A> 和 <CL2, packagename.A>。

#  6. Java 对象的创建过程

　　对象的创建过程：类加载检查、分配内存、初始化零值、设置对象信息、执行构造函数。

## 6.1. 类加载检查

　　当 Java 虚拟机遇到一条字节码 new 指令时，首先将去检查这个指令的参数是否能在常量池中定位到一个类的符号引用，并且检查这个符号引用代表的类是否已被加载、解析和初始化过。

　　如果没有，那必须先执行相应的类加载过程。

## 6.2. 分配内存

　　在类加载检查通过后，接下来虚拟机将为新生对象分配内存。

　　对象所需内存的大小在类加载完成后便可完全确定，为对象分配空间的任务实际上便等同于把一块确定大小的内存块从 Java 堆中划分出来。

　　假设 Java 堆中内存是绝对规整的，所有被使用过的内存都被放在一边，空闲的内存被放在另一边，中间放着一个指针作为分界点的指示器，那所分配内存就仅仅是把那个指针向空闲空间方向挪动一段与对象大小相等的距离，这种分配方式称为 “ **指针碰撞** ”（Bump The Pointer）。

　　但如果 Java 堆中的内存并不是规整的，已被使用的内存和空闲的内存相互交错在一起，那就没有办法简单地进行指针碰撞了，虚拟机就必须维护一个列表，记录上哪些内存块是可用的，在分配的时候从列表中找到一块足够大的空间划分给对象实例，并更新列表上的记录，这种分配方式称为 “ **空闲列表** ”（Free List）。

　　选择哪种分配方式由 Java 堆是否规整决定，而 Java 堆是否规整又由所采用的垃圾收集器是否带有空间压缩整理（Compact）的能力决定。

　　因此，当使用 Serial、ParNew 等带压缩整理过程的收集器时，系统采用的分配算法是指针碰撞，既简单又高效；而当使用 CMS 这种基于清除（Sweep）算法的收集器时，理论上就只能采用较为复杂的空闲列表来分配内存。

　　除如何划分可用空间外，还有另外一个问题：对象创建在虚拟机中是非常频繁的行为，即使仅仅修改一个指针所指向的位置，在并发情况下也并不是线程安全的。解决这个问题有两种可选方案：

1. 一种是对分配内存空间的动作进行同步处理

   实际上虚拟机是采用 CAS 配上失败重试的方式保证更新操作的原子性；

2. 另外一种是把内存分配的动作按照线程划分在不同的空间之中进行，即每个线程在 Java 堆中预先分配一小块内存，称为**本地线程分配缓冲**（Thread Local Allocation Buffer，TLAB）。

   哪个线程要分配内存，就在哪个线程的本地缓冲区中分配，只有本地缓冲区用完了，分配新的缓存区时才需要同步锁定。

   虚拟机是否使用 TLAB（本地线程分配缓冲），可以通过 -XX：+/-UseTLAB 参数来设定。

## 6.3. 初始化零值

　　内存分配完成之后，虚拟机必须将分配到的内存空间（但不包括对象头）都初始化为零值，如果使用了 TLAB（本地线程分配缓冲） 的话，这一项工作也可以提前至 TLAB（本地线程分配缓冲） 分配时顺便进行。这步操作保证了对象的实例字段在 Java 代码中可以不赋初始值就直接使用，使程序能访问到这些字段的数据类型所对应的零值。

## 6.4. 设置对象信息

　　接下来，Java 虚拟机还要对对象进行必要的设置，例如这个对象是哪个类的实例、如何才能找到类的元数据信息、对象的哈希码（实际上对象的哈希码会延后到真正调用 Object::hashCode() 方法时才计算）、对象的 GC 分代年龄等信息。

　　这些信息存放在对象的对象头（ObjectHeader）之中。

　　根据虚拟机当前运行状态的不同，如是否启用偏向锁等，对象头会有不同的设置方式。

## 6.5. 构造函数

　　从虚拟机的视角来看，一个新的对象已经产生了。但是从 Java 程序的视角看来，对象创建才刚刚开始——构造函数，即 Class 文件中的 < init >() 方法还没有执行，所有的字段都为默认的零值，对象需要的其他资源和状态信息也还没有按照预定的意图构造好。

　　一般来说（由字节码流中 new 指令后面是否跟随 invokespecial 指令所决定，Java 编译器会在遇到 new 关键字的地方同时生成这两条字节码指令，但如果直接通过其他方式产生的则不一定如此），new 指令之后会接着执行 < init >() 方法，按照程序员的意愿对对象进行初始化，这样一个真正可用的对象才算完全被构造出来。

#  7. 线上应用的 JVM 参数有哪些？

　　-Xms：设置堆的最小值。

　　-Xmx：设置堆的最大值。

　　-XX：+HeapDumpOnOutOfMemoryError 让虚拟机在出现内存溢出异常的时候 Dump 出当前的内存堆转储快照以便进行事后分析。

　　-Xss：设置栈容量。

 　　-XX：+/-UseTLAB：虚拟机是否使用 TLAB（本地线程分配缓冲）

 　　+XX：CompactFields：设置为 true，那子类之中较窄的变量也允许插入父类变量的空隙之中，以节省出一点点空间。

 　　-XX:PermSize：表示非堆区初始内存分配大小，其缩写为permanent size（持久化内存）。

 　　-XX:MaxPermSize：表示对非堆区分配的内存的最大上限。

 　　-XX:+HeapDumpOnOutOfMemoryError：当堆内存空间溢出时输出堆的内存快照。

 　　-Dfile.encoding：文件编码。

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
```

# 8. gl 和 cms 区别，吞吐量优先和响应优先的垃圾收集器选择。

　　Cms 是以获取最短回收停顿时间为目标的收集器。基于标记-清除算法实现。比较占用 cpu 资源，切易造成碎片。

　　G1 是面向服务端的垃圾收集器，是 jdk9 默认的收集器，基于标记-整理算法实现。可利用多核、多 cpu，保留分代，实现可预测停顿，可控。

http://blog.csdn.net/linhu007/article/details/48897597 

请解释如下 jvm 参数的含义：

-server -Xms512m -Xmx512m -Xss1024K

-XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxTenuringThreshold=20

XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly。

Server模式启动

最小堆内存512m

最大512m

每个线程栈空间1m

永久代256

最大永久代256

最大转为老年代检查次数20

Cms回收开启时机：内存占用80%

命令JVM不基于运行时收集的数据来启动CMS垃圾收集周期

# 9. 怎么打出线程栈信息

　　Linux下：

1. 第一步：在终端运行 Java 程序

2. 第二步：通过命令 pidof java 找到已经启动的 java 进程的ID，选择需要查看的 java 程序的进程ID

3. 第三步：使用命令 kill -3 <java进行的 pid> 打印出 java 程序的线程堆栈信息

4. 第四步：通常情况下运行的项目可能会比较大，那么这个时候打印的堆栈信息可能会有几千到几万行，为了方便查看，我们往往需要将输出内容进行重定向
   使用 linux 下的重定向命令方式即可：例如： demo.sh > run.log 2>&1 将输出信息重定向到 run.log中。

   注：在操作系统中，0 1 2 分别对应着不同的含义， 如下：
   0 ： 标准输入，即：C 中的stdin ， java 中的 System.in
   1 ： 标准输出， 即：C 中的stdout ，java 中的 System.out
   2 ： 错误输出， 即：C 中的stderr ， java 中的 System.err

　　使用命令：kill -3 {pid}，可以打印指定线程的堆栈信息到 tomcat 的 catalina.out 日志中。在性能测试过程中，可以观察响应时间的曲线，如果突然出现波峰则抓取当前时间点 tomcat 线程的堆栈信息供后续分析。

# 10. 如何进行内存调优

http://ifeve.com/useful-jvm-flags/

http://ifeve.com/useful-jvm-flags-part-4-heap-tuning/



