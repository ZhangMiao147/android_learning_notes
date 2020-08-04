# Java 基础的常见问题3

# 1. 抽象与接口

抽象类和接口的区别，类可以集成多个类吗？接口可以继承多个接口吗？类可以实现多个接口吗？



# 2. IO 

IO 模型有哪些，讲讲你理解的 nio，它和 bio、aio 的区别是啥？谈谈 reactor 模型。

# 3. hashCode

如何在父类中为子类自动完成所有的 hashcode 和 equals 实现？这么做有何优劣。



说一说你对 java.lang.Object 对象中 hashcode 和 equals 方法的理解。在什么场景下需要重新实现这两个方法。



这样的 a.hashcode() 有什么用，与 a.equals(b) 有什么关系？



有没有可能 2 个不想等的对象有相同的 hashcode。

# 4. 访问修饰符

请结合 OO 设计理念，谈谈访问修饰符 public 、private 、protected、default 在应用设计中的作用。



# 5. String

## 5.1. 在自己的代码中，如果创建一个 java.lang.String 类，这个类是否可以被类加载器加载？为什么？

![](image/双亲委派.jpg)

### 5.1.1. 双亲委派模型

　　类加载器分为两种：一是启动类加载器（Bootstrap ClassLoader），是 C++ 实现的，是 JVM 的一部分；另一种是其他的类加载器，是 Java 实现的，独立于 JVM，全部都继承自抽象类 java.lang.ClassLoader。JDK 自带了三种类加载器，分别是启动类加载器（Boostrap ClassLoader）、扩展类加载器（Extension ClassLoader）、应用程序类加载器（Application ClassLoader）。后两种加载器是继承自抽象类 java.lang.ClassLoader。

　　一般是：自定义类加载器 -> 扩展类加载器 -> 启动类加载器

　　上面的层次关系被称为双亲委派模型（Parents Delegation Model）。除了最顶层的启动类加载器外，其余的类加载器都有对应的父亲加载器。

　　双亲委托机制：如果一个类加载器收到了类加载的请求，它首先不会自己尝试去加载这个类，而是把这个请求委派给父类加载器，每一个层次的类加载器都是如此，因此所有的加载请求最终到达顶层的启动类加载器，只有当父亲加载器反馈自己无法完成加载请求时（指它的搜索范围没有找到所需的类），子类加载器才会尝试自己去加载。

　　各个类加载器之间是组合关系，并非继承关系。

　　当一个类加载器收到类加载的请求，它将这个加载请求委派给父类加载器进行加载，每一层加载器都是如此，最终，所有的请求都会传动到启动类加载器。只有当父类加载器自己无法完成加载请求时，子类加载器才会尝试自己加载。

　　双亲委派模型可以确保安全性，可以保证所有的 Java 类库都是由启动类加载器加载。如用户编写的 java.lang.Object，加载请求传递到启动类加载器，启动类加载的是系统中的 Object 对象，而用户编写的 java.lang.Object 不会被加载。如用户编写的 java.lang.virus 类，加载请求传递到启动类加载器，启动类加载器发现 virus 类并不是核心 Java 类，无法进行加载，将会由具体的子类加载器进行加载，而经过不同加载器进行加载的类是无法访问彼此的。由不同加载器加载的类处于不同的运行时包。所有的访问权限都是基于同一个运行时包而言的。

### 5.1.2. 为什么要使用这种双亲委托模型呢？

　　因为这样可以避免重复加载，当父类已经加载了该类的时候，就没有必要子 ClassLoader 再加载一次。

　　考虑到安全因素，如果不使用这种委托模式，那就可以随时使用自定义的 String 来动态替代 java 核心 api 中定义类型，这样会存在非常大的安全隐患，而双亲委托的方式，就可以避免这种情况，因为 String 已经在启动时被加载，所以用户自定义类是无法加载一个自定义的 ClassLoader。

　　思考：假如自己写了一个 java.lang.String 的类，是否可以替换掉 JDK 本身的类？

　　答案是否定的。不能实现。双亲委托机制是可以打破的，完全可以自己写一个 classLoader 来加载自己写的 java.lang.String 类，但是会发现也不会加载成功，具体就是因为针对 java.* 开头的类，jvm 的实现中已经保证了必须由 bootstrp 来加载。

　　因加载某个类时，优先使用父类加载器加载需要使用的类。如果自定义了 java.lang.String 这个类，加载该自定义的 String 类，该自定义 String 类使用的加载器是 AppClassLoader，根据优先使用父类加载器原理，AppClassLoader 加载器的父类为 ExtClassLoader，所以这时加载 String 使用的类加载器是 ExtClassLoader，但是类加载器 ExtClassLoader 在 jre/lib/ext 目录下没有找到 String.Class 类。然后使用 ExtClassLoader 父类的加载器 BootStrap，父类加载器 BootStrap 在 JRE.jar 目录的 rt.jar 找到了 String.class，将其加载到内存中。这就是类加载器的委托机制。

### 5.1.3. 定义自己的 ClassLoader

　　既然 JVM 已经提供了默认的类加载器，为什么还要定义自己的类加载器呢？

　　因为 Java 中提供的默认 ClassLoader，只加载指定目录下的 jar 和 class，如果想加载其他位置的类或 jar 时，比如：要加载网络上的一个 class 文件，通过动态加载到内存之后，要调用这个类中的方法实现业务逻辑。在这样的情况下，默认的 ClassLoader 就不能满足需求了，所以需要定义自己的 ClassLoader。

　　定义自己的类加载器分为两步：

1. 继承 java.lang.ClassLoader
2. 重写父类的 findClass 方法。

　　父类有那么多方法，为什么偏偏只重写 findClass 方法？

　　因为 JDK 已经在 loadClass 方法中实现了 ClassLoader 搜索类的算法，当在 loadClass 方法中搜索不到类时，loadClass 方法就会调用 findClass 方法来搜索类，所以只需重写该方法即可。如没有特殊的要求，一般不建议重写 loadClass 搜索类的算法。

# 6. 拷贝

## 6.1. 深拷贝和浅拷贝的区别

　　浅拷贝和深拷贝都是针对一个已有对象的操作。

　　在 Java 中，除了基本数据类型（元类型）之外，还存在类的实例对象这个引用数据类型。而一般使用 = 号做赋值操作的时候。对于基本数据类型，实际上是拷贝的它的值，但是对于对象而言，其实赋值的只是这个对象的引用，将原对象的引用传递过去，他们实际上还是指向同一个对象。

　　而浅拷贝和深拷贝就是这个基础之上做的区分，如果在拷贝这个对象的时候，只对基本数据类型进行了拷贝，而对引用数据类型只是进行了引用的传递，而没有真实的创建一个新的对象，则认为是浅拷贝。反之，在对引用数据类型进行拷贝的时候，创建了一个新的对象，并且复制其内的成员变量，则认为是深拷贝。

　　所谓的浅拷贝和深拷贝，只是在拷贝对象的时候，对类的实例对象这种引用数据类型的不同操作而已。

　　总的来说：

　　浅拷贝：对基本数据类型进行值传递，对引用数据类型进行引用传递般的拷贝，此为浅拷贝。

![](image/浅拷贝.jpg)

　　深拷贝：对基本数据类型进行值传递，对引用数据类型，创建一个新的对象，并复制其内容，此为深拷贝。

![](image/深拷贝.jpg)

　　如果一个对象内部只有基本数据类型，那用 clone() 放啊获取到的就是这个对象的深拷贝，而如果其内部还有引用数据类型，那用 clone() 方法就是依次浅拷贝的操作。

# 6. HashSet

 Java 中的 HashSet 内部是如何工作的。



# 7. 序列化

https://www.jianshu.com/p/208ac4a71c6f Android 序列化

## 什么是序列化与反序列化

序列化：把对象转换为字节序列的过程称为对象的序列化。
反序列化：把字节序列恢复为对象的过程称为对象的反序列化。

 Java序列化是指把Java对象转换为字节序列的过程；而Java反序列化是指把字节序列恢复为Java对象的过程。

序列化是指将Java对象转换为字节序列的过程，而反序列化则是将字节序列转换为Java对象的过程。 Java对象序列化是将实现了Serializable接口的对象转换成一个字节序列，能够通过网络传输、文件存储等方式传输 ，传输过程中却不必担心数据在不同机器、不同环境下发生改变，也不必关心字节的顺序或其他任何细节，并能够在以后将这个字节序列完全恢复为原来的对象(恢复这一过程称之为反序列化)。

对象的序列化是非常有趣的，因为利用它可以实现轻量级持久性，“持久性”意味着一个对象的生存周期不单单取决于程序是否正在运行，它可以生存于程序的调用之间。通过将一个序列化对象写入磁盘，然后在重新调用程序时恢复该对象，从而达到实现对象的持久性的效果。

本质上讲，序列化就是把实体对象状态按照一定的格式写入到有序字节流，反序列化就是从有序字节流重建对象，恢复对象状态。



Java序列化是指把Java对象转换为字节序列的过程，而Java反序列化是指把字节序列恢复为Java对象的过程：

- 序列化：对象序列化的最主要的用处就是在传递和保存对象的时候，保证对象的完整性和可传递性。序列化是把对象转换成有序字节流，以便在网络上传输或者保存在本地文件中。核心作用是对象状态的保存与重建。
- 反序列化：客户端从文件中或网络上获得序列化后的对象字节流，根据字节流中所保存的对象状态及描述信息，通过反序列化重建对象。

## 为什么需要序列化与反序列化

当你想把的内存中的对象状态保存到一个文件中或者数据库中时候；
当你想用套接字在网络上传送对象的时候；
当你想通过RMI传输对象的时候；

 我们知道，当两个进程进行远程通信时，可以相互发送各种类型的数据，包括文本、图片、音频、视频等， 而这些数据都会以二进制序列的形式在网络上传送。那么当两个Java进程进行通信时，能否实现进程间的对象传送呢？答案是可以的。如何做到呢？这就需要Java序列化与反序列化了。换句话说，一方面，发送方需要把这个Java对象转换为字节序列，然后在网络上传送；另一方面，接收方需要从字节序列中恢复出Java对象。

Java序列化的好处。其好处一是实现了数据的持久化，通过序列化可以把数据永久地保存到硬盘上（通常存放在文件里），二是，利用序列化实现远程通信，即在网络上传送对象的字节序列。



我们知道，不同进程/程序间进行远程通信时，可以相互发送各种类型的数据，包括文本、图片、音频、视频等，而这些数据都会以二进制序列的形式在网络上传送。

那么当两个Java进程进行通信时，能否实现进程间的对象传送呢?当然是可以的!如何做到呢?这就需要使用Java序列化与反序列化了。发送方需要把这个Java对象转换为字节序列，然后在网络上传输，接收方则需要将字节序列中恢复出Java对象。

我们清楚了为什么需要使用Java序列化和反序列化后，我们很自然地会想到Java序列化有哪些好处：

- 实现了数据的持久化，通过序列化可以把数据永久地保存到硬盘上(如：存储在文件里)，实现永久保存对象。
- 利用序列化实现远程通信，即：能够在网络上传输对象。



为什么要序列化，那就是说一下序列化的好处喽，序列化有什么什么优点，所以我们要序列化。

**一：对象序列化可以实现分布式对象。**

主要应用例如：RMI(即远程调用Remote Method Invocation)要利用对象序列化运行远程主机上的服务，就像在本地机上运行对象时一样。

**二：java对象序列化不仅保留一个对象的数据，而且递归保存对象引用的每个对象的数据。**

可以将整个对象层次写入字节流中，可以保存在文件中或在网络连接上传递。利用对象序列化可以进行对象的"深复制"，即复制对象本身及引用的对象本身。序列化一个对象可能得到整个对象序列。

**三：序列化可以将内存中的类写入文件或数据库中。**

比如：将某个类序列化后存为文件，下次读取时只需将文件中的数据反序列化就可以将原先的类还原到内存中。也可以将类序列化为流数据进行传输。

总的来说就是将一个已经实例化的类转成文件存储，下次需要实例化的时候只要反序列化即可将类实例化到内存中并保留序列化时类中的所有变量和状态。

**四：对象、文件、数据，有许多不同的格式，很难统一传输和保存。**

序列化以后就都是字节流了，无论原来是什么东西，都能变成一样的东西，就可以进行通用的格式传输或保存，传输结束以后，要再次使用，就进行反序列化还原，这样对象还是对象，文件还是文件。

## 如何实现 Java 序列化与反序列化

实现Serializable接口即可。

实现这个Serializable 接口的时候，一定要给这个 serialVersionUID 赋值。

**关于 serialVersionUID 的描述**

序列化运行时使用一个称为 serialVersionUID 的版本号与每个可序列化类相关联，该序列号在反序列化过程中用于验证序列化对象的发送者和接收者是否为该对象加载了与序列化兼容的类。如果接收者加载的该对象的类的 serialVersionUID 与对应的发送者的类的版本号不同，则反序列化将会导致 InvalidClassException。可序列化类可以通过声明名为 “serialVersionUID” 的字段（该字段必须是静态 (static)、最终 (final) 的 long 型字段）显式声明其自己的 serialVersionUID。

如果可序列化类未显式声明 serialVersionUID，则序列化运行时将基于该类的各个方面计算该类的默认 serialVersionUID 值，如“Java™ 对象序列化规范”中所述。不过，强烈建议 所有可序列化类都显式声明 serialVersionUID 值，原因是计算默认的 serialVersionUID 对类的详细信息具有较高的敏感性，根据编译器实现的不同可能千差万别，这样在反序列化过程中可能会导致意外的 InvalidClassException。因此，为保证 serialVersionUID 值跨不同 java 编译器实现的一致性，序列化类必须声明一个明确的 serialVersionUID 值。还强烈建议使用 private 修饰符显示声明 serialVersionUID（如果可能），原因是这种声明仅应用于直接声明类 – serialVersionUID 字段作为继承成员没有用处。数组类不能声明一个明确的 serialVersionUID，因此它们总是具有默认的计算值，但是数组类没有匹配 serialVersionUID 值的要求。



1）JDK类库中序列化API

 java.io.ObjectOutputStream：表示对象输出流

它的writeObject(Object obj)方法可以对参数指定的obj对象进行序列化，把得到的字节序列写到一个目标输出流中。

java.io.ObjectInputStream：表示对象输入流

它的readObject()方法源输入流中读取字节序列，再把它们反序列化成为一个对象，并将其返回。

2）实现序列化的要求

只有实现了Serializable或Externalizable接口的类的对象才能被序列化，否则抛出异常。

3）实现Java对象序列化与反序列化的方法

假定一个Student类，它的对象需要序列化，可以有如下三种方法：

方法一：若Student类仅仅实现了Serializable接口，则可以按照以下方式进行序列化和反序列化

ObjectOutputStream采用默认的序列化方式，对Student对象的非transient的实例变量进行序列化。

ObjcetInputStream采用默认的反序列化方式，对对Student对象的非transient的实例变量进行反序列化。

方法二：若Student类仅仅实现了Serializable接口，并且还定义了readObject(ObjectInputStream in)和writeObject(ObjectOutputSteam out)，则采用以下方式进行序列化与反序列化。

ObjectOutputStream调用Student对象的writeObject(ObjectOutputStream out)的方法进行序列化。

ObjectInputStream会调用Student对象的readObject(ObjectInputStream in)的方法进行反序列化。

方法三：若Student类实现了Externalnalizable接口，且Student类必须实现readExternal(ObjectInput in)和writeExternal(ObjectOutput out)方法，则按照以下方式进行序列化与反序列化。

ObjectOutputStream调用Student对象的writeExternal(ObjectOutput out))的方法进行序列化。

ObjectInputStream会调用Student对象的readExternal(ObjectInput in)的方法进行反序列化。

4）JDK类库中序列化的步骤

步骤一：创建一个对象输出流，它可以包装一个其它类型的目标输出流，如文件输出流：

ObjectOutputStream out = new ObjectOutputStream(new fileOutputStream(“D:\\objectfile.obj”));

步骤二：通过对象输出流的writeObject()方法写对象：

out.writeObject(“Hello”);

out.writeObject(new Date());

5）JDK类库中反序列化的步骤

步骤一：创建一个对象输入流，它可以包装一个其它类型输入流，如文件输入流：

ObjectInputStream in = new ObjectInputStream(new fileInputStream(“D:\\objectfile.obj”));

步骤二：通过对象输出流的readObject()方法读取对象：

String obj1 = (String)in.readObject();

Date obj2 = (Date)in.readObject();

说明：为了正确读取数据，完成反序列化，必须保证向对象输出流写对象的顺序与从对象输入流中读对象的顺序一致。

为了更好地理解Java序列化与反序列化，选择方法一编码实现。

总结：

1）Java序列化就是把对象转换成字节序列，而Java反序列化就是把字节序列还原成Java对象。

2）采用Java序列化与反序列化技术，一是可以实现数据的持久化，在MVC模式中很是有用；二是可以对象数据的远程通信



只要对象实现了Serializable、Externalizable接口(该接口仅仅是一个标记接口，并不包含任何方法)，则该对象就实现了序列化。

#### 3、什么场景下需要序列化

- 当你想把的内存中的对象状态保存到一个文件中或者数据库中时候。
- 当你想用套接字在网络上传送对象的时候。
- 当你想通过RMI传输对象的时候。

#### 1、具体是如何实现的呢?

序列化，首先要创建某些OutputStream对象，然后将其封装在一个ObjectOutputStream对象内，这时调用writeObject()方法，即可将对象序列化，并将其发送给OutputStream(对象序列化是基于字节的，因此使用的InputStream和OutputStream继承的类)。

反序列化，即反向进行序列化的过程，需要将一个InputStream封装在ObjectInputStream对象内，然后调用readObject()方法，获得一个对象引用(它是指向一个向上转型的Object)，然后进行类型强制转换来得到该对象。

假定一个User类，它的对象需要序列化，可以有如下三种方法：

(1)若User类仅仅实现了Serializable接口，则可以按照以下方式进行序列化和反序列化。

- ObjectOutputStream采用默认的序列化方式，对User对象的非transient的实例变量进行序列化。
- ObjcetInputStream采用默认的反序列化方式，对对User对象的非transient的实例变量进行反序列化。

(2)若User类仅仅实现了Serializable接口，并且还定义了readObject(ObjectInputStream in)和writeObject(ObjectOutputSteam out)，则采用以下方式进行序列化与反序列化。

- ObjectOutputStream调用User对象的writeObject(ObjectOutputStream out)的方法进行序列化。
- ObjectInputStream会调用User对象的readObject(ObjectInputStream in)的方法进行反序列化。

(3)若User类实现了Externalnalizable接口，且User类必须实现readExternal(ObjectInput in)和writeExternal(ObjectOutput out)方法，则按照以下方式进行序列化与反序列化。

- ObjectOutputStream调用User对象的writeExternal(ObjectOutput out))的方法进行序列化。
- ObjectInputStream会调用User对象的readExternal(ObjectInput in)的方法进行反序列化。

java.io.ObjectOutputStream：对象输出流，它的writeObject(Object obj)方法可以对指定的obj对象进行序列化，把得到的字节序列写到一个目标输出流中。

java.io.ObjectInputStream：对象输入流，它的readObject()方法可以将从输入流中读取字节序列，再把它们反序列化成为一个对象，并将其返回。

### 注意事项

1、当一个父类实现序列化，子类就会自动实现序列化，不需要显式实现Serializable接口。

2、当一个对象的实例变量引用其他对象，序列化该对象时也把引用对象进行序列化。

3、并非所有的对象都可以进行序列化，比如：

- 安全方面的原因，比如一个对象拥有private，public等成员变量，对于一个要传输的对象，比如写到文件，或者进行RMI传输等等，在序列化进行传输的过程中，这个对象的private等域是不受保护的;
- 资源分配方面的原因，比如socket，thread类，如果可以序列化，进行传输或者保存，也无法对他们进行重新的资源分配，而且，也是没有必要这样实现。

4、声明为static和transient类型的成员变量不能被序列化。因为static代表类的状态，transient代表对象的临时数据。

5、序列化运行时会使用一个称为 serialVersionUID 的版本号，并与每个可序列化的类相关联，该序列号在反序列化过程中用于验证序列化对象的发送者和接收者是否为该对象加载了与序列化兼容的类。如果接收者加载的该对象的类的 serialVersionUID 与对应的发送者的类的版本号不同，则反序列化将会导致 InvalidClassException。可序列化类可以通过声明名为 "serialVersionUID" 的字段(该字段必须是静态 (static)、最终 (final) 的 long 型字段)显式声明其自己的 serialVersionUID。

如果序列化的类未显式的声明 serialVersionUID，则序列化运行时将基于该类的各个方面计算该类的默认 serialVersionUID 值，如“Java(TM) 对象序列化规范”中所述。不过，强烈建议 所有可序列化类都显式声明 serialVersionUID 值，原因是计算默认的 serialVersionUID 对类的详细信息具有较高的敏感性，根据编译器实现的不同可能千差万别，这样在反序列化过程中可能会导致意外的 InvalidClassException。因此，为保证 serialVersionUID 值跨不同 java 编译器实现的一致性，序列化类必须声明一个明确的 serialVersionUID 值。还强烈建议使用 private 修饰符显示声明 serialVersionUID(如果可能)，原因是这种声明仅应用于直接声明类 -- serialVersionUID 字段作为继承成员没有用处。数组类不能声明一个明确的 serialVersionUID，因此它们总是具有默认的计算值，但是数组类没有匹配 serialVersionUID 值的要求。

6、Java有很多基础类已经实现了serializable接口，比如String，Vector等。但是也有一些没有实现serializable接口的。

7、如果一个对象的成员变量是一个对象，那么这个对象的数据成员也会被保存!这是能用序列化解决深拷贝的重要原因。



## Android 序列化与 Java 序列化

在日常的应用开发中，我们可能需要让某些对象离开内存空间，存储到物理磁盘，以便长期保存，同时也能减少对内存的压力，而在需要时再将其从磁盘读取到内存，比如将某个特定的对象保存到文件中，隔一段时间后再把它读取到内存中使用，那么该对象就需要实现序列化操作，在java中可以使用Serializable接口实现对象的序列化，而在android中既可以使用Serializable接口实现对象序列化也可以使用Parcelable接口实现对象序列化，但是在内存操作时更倾向于实现Parcelable接口，这样会使用传输效率更高效。

# 8. 序列化与反序列

首先来了解一下序列化与反序列化。

### （1）序列化

由于存在于内存中的对象都是暂时的，无法长期驻存，为了把对象的状态保持下来，这时需要把对象写入到磁盘或者其他介质中，这个过程就叫做序列化。

### （2）反序列化

反序列化恰恰是序列化的反向操作，也就是说，把已存在在磁盘或者其他介质中的对象，反序列化（读取）到内存中，以便后续操作，而这个过程就叫做反序列化。

概括性来说序列化是指将对象实例的状态存储到存储媒体（磁盘或者其他介质）的过程。在此过程中，先将对象的公共字段和私有字段以及类的名称（包括类所在的程序集）转换为字节流，然后再把字节流写入数据流。在随后对对象进行反序列化时，将创建出与原对象完全相同的副本。

### （3）实现序列化的必要条件

一个对象要实现序列化操作，该类就必须实现了Serializable接口或者Parcelable接口，其中Serializable接口是在java中的序列化抽象类，而Parcelable接口则是android中特有的序列化接口，在某些情况下，Parcelable接口实现的序列化更为高效。实现序列化操作时必须实现Serializable接口或者Parcelable接口之一。

### （4）序列化的应用情景

主要有以下情况（但不限于以下情况）
 1）内存中的对象写入到硬盘；
 2）用套接字在网络上传送对象；

### Serizable

Serializable是java提供的一个序列化接口，它是一个空接口，专门为对象提供标准的序列化和反序列化操作，使用Serializable实现类的序列化比较简单，只要在类声明中实现Serializable接口即可，同时强烈建议声明序列化标识。

```java
public class User implements Serializable {

    private static final long serialVersionUID = -2083503801443301445L;

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

如上述代码所示，User类实现的Serializable接口并声明了序列化标识serialVersionUID，该ID由编辑器生成，当然也可以自定义，如1L，5L，不过还是建议使用编辑器生成唯一标识符。那么serialVersionUID有什么作用呢？实际上我们不声明serialVersionUID也是可以的，因为在序列化过程中会自动生成一个serialVersionUID来标识序列化对象。既然如此，那我们还需不需要要指定呢？原因是serialVersionUID是用来辅助序列化和反序列化过程的，原则上序列化后的对象中serialVersionUID只有和当前类的serialVersionUID相同才能够正常被反序列化，也就是说序列化与反序列化的serialVersionUID必须相同才能够使序列化操作成功。具体过程是这样的：序列化操作的时候系统会把当前类的serialVersionUID写入到序列化文件中，当反序列化时系统会去检测文件中的serialVersionUID，判断它是否与当前类的serialVersionUID一致，如果一致就说明序列化类的版本与当前类版本是一样的，可以反序列化成功，否则失败。报出如下UID错误：

```
Exception in thread "main" java.io.InvalidClassException: com.zejian.test.Client; 
local class incompatible: stream classdesc serialVersionUID = -2083503801443301445, 
local class serialVersionUID = -4083503801443301445
```

因此强烈建议指定serialVersionUID，这样的话即使微小的变化也不会导致crash的出现，如果不指定的话只要这个文件多一个空格，系统自动生成的UID就会截然不同的，反序列化也就会失败。

下面来看一个如何进行对象序列化和反序列化的列子：

```java
public class Demo {

    public static void main(String[] args) throws Exception {

        // 构造对象
        User user = new User();
        user.setId(1000);
        user.setName("韩梅梅");

        // 把对象序列化到文件
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/serializable/user.txt"));
        oos.writeObject(user);
        oos.close();

        // 反序列化到内存
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/serializable/user.txt"));
        User userBack = (User) ois.readObject();
        System.out.println("read serializable user:id=" + userBack.getId() + ", name=" + userBack.getName());
        ois.close();
    }
}
```

输出结果：

```java
read serializable user:id=1000, name=韩梅梅
```

从代码可以看出只需要ObjectOutputStream和ObjectInputStream就可以实现对象的序列化和反序列化操作，通过流对象把user对象写到文件中，并在需要时恢复userBack对象，但是两者并不是同一个对象了，反序列化后的对象是新创建的。这里有两点特别注意的是如果反序列类的成员变量的类型或者类名，发生了变化，那么即使serialVersionUID相同也无法正常反序列化成功。其次是静态成员变量属于类不属于对象，不会参与序列化过程，使用transient关键字标记的成员变量也不参与序列化过程。

另外，系统的默认序列化过程是可以改变的，通过实现如下4个方法，即可以控制系统的默认序列化和反序列过程：

```java
public class User implements Serializable {

    private static final long serialVersionUID = -4083503801443301445L;

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 序列化时,
     * 首先系统会先调用writeReplace方法,在这个阶段,
     * 可以进行自己操作,将需要进行序列化的对象换成我们指定的对象.
     * 一般很少重写该方法
     */
    private Object writeReplace() throws ObjectStreamException {
        System.out.println("writeReplace invoked");
        return this;
    }
    /**
     *接着系统将调用writeObject方法,
     * 来将对象中的属性一个个进行序列化,
     * 我们可以在这个方法中控制住哪些属性需要序列化.
     * 这里只序列化name属性
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        System.out.println("writeObject invoked");
        out.writeObject(this.name == null ? "默认值" : this.name);
    }

    /**
     * 反序列化时,系统会调用readObject方法,将我们刚刚在writeObject方法序列化好的属性,
     * 反序列化回来.然后通过readResolve方法,我们也可以指定系统返回给我们特定的对象
     * 可以不是writeReplace序列化时的对象,可以指定其他对象.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        System.out.println("readObject invoked");
        this.name = (String) in.readObject();
        System.out.println("got name:" + name);
    }


    /**
     * 通过readResolve方法,我们也可以指定系统返回给我们特定的对象
     * 可以不是writeReplace序列化时的对象,可以指定其他对象.
     * 一般很少重写该方法
     */
    private Object readResolve() throws ObjectStreamException {
        System.out.println("readResolve invoked");
        return this;
    }
}
```

通过上面的4个方法，我们就可以随意控制序列化的过程了，由于在大部分情况下我们都没必要重写这4个方法，因此这里我们也不过介绍了，只要知道有这么一回事就行。

### Parcelable

鉴于Serializable在内存序列化上开销比较大，而内存资源属于android系统中的稀有资源（android系统分配给每个应用的内存开销都是有限的），为此android中提供了Parcelable接口来实现序列化操作，Parcelable的性能比Serializable好，在内存开销方面较小，所以在内存间数据传输时推荐使用Parcelable，如通过Intent在activity间传输数据，而Parcelable的缺点就使用起来比较麻烦，下面给出一个Parcelable接口的实现案例，大家感受一下：

```java
public class User implements Parcelable {

    public int id;
    public String name;
    public User friend;

    /**
     * 当前对象的内容描述,一般返回0即可
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 将当前对象写入序列化结构中
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeParcelable(this.friend, 0);
    }

    public NewClient() {}

    /**
     * 从序列化后的对象中创建原始对象
     */
    protected NewClient(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
       //friend是另一个序列化对象，此方法序列需要传递当前线程的上下文类加载器，否则会报无法找到类的错误
       this.friend=in.readParcelable(Thread.currentThread().getContextClassLoader());
    }

    /**
     * public static final一个都不能少，内部对象CREATOR的名称也不能改变，必须全部大写。
     * 重写接口中的两个方法：
     * createFromParcel(Parcel in) 实现从Parcel容器中读取传递数据值,封装成Parcelable对象返回逻辑层，
     * newArray(int size) 创建一个类型为T，长度为size的数组，供外部类反序列化本类数组使用。
     */
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        /**
         * 从序列化后的对象中创建原始对象
         */
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        /**
         * 创建指定长度的原始对象数组
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
```

从代码可知，在序列化的过程中需要实现的功能有序列化和反序列以及内容描述。其中writeToParcel方法实现序列化功能，其内部是通过Parcel的一系列write方法来完成的，接着通过CREATOR内部对象来实现反序列化，其内部通过createFromParcel方法来创建序列化对象并通过newArray方法创建数组，最终利用Parcel的一系列read方法完成反序列化，最后由describeContents完成内容描述功能，该方法一般返回0，仅当对象中存在文件描述符时返回1。同时由于User是另一个序列化对象，因此在反序列化方法中需要传递当前线程的上下文类加载器，否则会报无法找到类的错误。

简单用一句话概括来说就是通过writeToParcel将我们的对象映射成Parcel对象，再通过createFromParcel将Parcel对象映射成我们的对象。也可以将Parcel看成是一个类似Serliazable的读写流，通过writeToParcel把对象写到流里面，在通过createFromParcel从流里读取对象，这个过程需要我们自己来实现并且写的顺序和读的顺序必须一致。ok~，到此Parcelable接口的序列化实现基本介绍完。
  
 那么在哪里会使用到Parcelable对象呢？其实通过Intent传递复杂类型(如自定义引用类型数据)的数据时就需要使用Parcelable对象，如下是日常应用中Intent关于Parcelable对象的一些操作方法，引用类型必须实现Parcelable接口才能通过Intent传递，而基本数据类型，String类型则可直接通过Intent传递而且Intent本身也实现了Parcelable接口，所以可以轻松地在组件间进行传输。

|                         方法名称                          |                         含义                         |
| :-------------------------------------------------------: | :--------------------------------------------------: |
|          putExtra(String name, Parcelable value)          |         设置自定义类型并实现Parcelable的对象         |
|         putExtra(String name, Parcelable[] value)         |       设置自定义类型并实现Parcelable的对象数组       |
| putParcelableArrayListExtra(String name, ArrayList value) | 设置List数组，其元素必须是实现了Parcelable接口的数据 |

除了以上的Intent外系统还为我们提供了其他实现Parcelable接口的类，再如Bundle、Bitmap，它们都是可以直接序列化的，因此我们可以方便地使用它们在组件间进行数据传递，当然Bundle本身也是一个类似键值对的容器，也可存储Parcelable实现类，其API方法跟Intent基本相似，由于这些属于android基础知识点，这里我们就不过多介绍了。

### Parcelable 与 Serializable 区别

### （1）两者的实现差异

Serializable的实现，只需要实现Serializable接口即可。这只是给对象打了一个标记（UID），系统会自动将其序列化。而Parcelabel的实现，不仅需要实现Parcelabel接口，还需要在类中添加一个静态成员变量CREATOR，这个变量需要实现 Parcelable.Creator 接口，并实现读写的抽象方法。

### （2）两者的设计初衷

Serializable的设计初衷是为了序列化对象到本地文件、数据库、网络流、RMI以便数据传输，当然这种传输可以是程序内的也可以是两个程序间的。而Android的Parcelable的设计初衷是由于Serializable效率过低，消耗大，而android中数据传递主要是在内存环境中（内存属于android中的稀有资源），因此Parcelable的出现为了满足数据在内存中低开销而且高效地传递问题。

### （3）两者效率选择

Serializable使用IO读写存储在硬盘上。序列化过程使用了反射技术，并且期间产生临时对象，优点代码少，在将对象序列化到存储设置中或将对象序列化后通过网络传输时建议选择Serializable。
 Parcelable是直接在内存中读写，我们知道内存的读写速度肯定优于硬盘读写速度，所以Parcelable序列化方式性能上要优于Serializable方式很多。所以Android应用程序在内存间数据传输时推荐使用Parcelable，如activity间传输数据和AIDL数据传递。大多数情况下使用Serializable也是没什么问题的，但是针对Android应用程序在内存间数据传输还是建议大家使用Parcelable方式实现序列化，毕竟性能好很多，其实也没多麻烦。
 Parcelable也不是不可以在网络中传输，只不过实现和操作过程过于麻烦并且为了防止android版本不同而导致Parcelable可能不同的情况，因此在序列化到存储设备或者网络传输方面还是尽量选择Serializable接口。



## 反序列化会遇到什么问题，如何解决？



# 9. Java 8

## Java 8 的新特性

https://www.jianshu.com/p/0bf8fe0f153b

# 10. 运算符

##  java 运算符与（&） 、非（～）、或（|）、异或（^）

https://www.cnblogs.com/liaojie970/p/5329690.html

