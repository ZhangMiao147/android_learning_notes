# Java 基础的常见问题3

[TOC]



# 1. 抽象与接口

## 1.1. 抽象类和接口的区别

1. 抽象类和接口都不能直接实例化，如果要实例化，抽象类变量必须指向实现所有抽象方法的子类对象，接口变量必须指向实现所有接口方法的类对象。

2. 抽象类要被子类继承，接口要被类实现。

3. 接口只能做方法声明（Java 8 可以方法实现，使用 default 标志），抽象类中可以做方法声明，也可以做方法实现。

4. 接口里定义的变量只能是公共的静态的常量，抽象类中的变量是普通变量。

5. 抽象类里的抽象方法必须全部被子类所实现，如果子类不能全部实现父类抽象方法，那么该子类只能是抽象类。同样，一个实现接口的时候，如不能全部实现接口方法，那么该类也只能为抽象类。

6. 抽象方法只能声明，不能实现。abstract void abc();不能写成abstract void abc(){}。

7. 抽象类里可以没有抽象方法。

8. 如果一个类里有抽象方法，那么这个类只能是抽象类。

9. 抽象方法要被实现，所以不能是静态的，也不能是私有的。

10. 接口可继承接口，并可多继承接口，但类只能单继承。

## 1.2. 类可以继承多个类吗

　　不可以，Java 是单继承。

## 1.3. 接口可以继承多个接口吗

　　可以。

## 1.4. 类可以实现多个接口吗

　　可以。

# 2. IO 模型有哪些，讲讲你理解的 nio，它和 bio、aio 的区别是啥？谈谈 reactor 模型。

[I/O模型之四：Java 浅析I/O模型（BIO、NIO、AIO、Reactor、Proactor）](https://www.cnblogs.com/duanxz/p/5150973.html)

**BIO：**同步并阻塞，服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程并处理，如果这个连接不做任何事情会造成不必要的开销，当然可以通过线程池机制改善。

**NIO **：同步非阻塞，服务器实现模式为一个请求一个线程，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有 IO 请求时才启动一个线程进行处理。

**NIO2(AIO)**：异步非阻塞，服务器实现模式为一个有效请求一个线程，客户端的 I/O 请求都是由 OS 先完成了再通知服务器应用去启动线程进行处理。

**reactor模型：**NIO 基于 Reactor，当 socket 有流可读或可写入 socket，操作系统会相应的通知引用程序进行处理，应用再将流读取到缓冲区或写入操作系统。也就是，不是一个链接就要对应一个处理线程，而是一个有效请求对应一个线程，当连接没有数据时，是没有工作线程来处理的。

![img](https:////upload-images.jianshu.io/upload_images/21439828-c9bde872c2518352.png?imageMogr2/auto-orient/strip|imageView2/2/w/934)

# 3. hashCode

## 3.1. 如何在父类中为子类自动完成所有的 hashcode 和 equals 实现？这么做有何优劣。

　　父类的 equals 不一定满足子类的 equals 需求。比如所有的对象都继承 Object，默认使用的是 Object 的 equals 方法，在比较两个对象的时候，是看他们是否指向同一个地址。

　　但是需求是对象的某个属性相同，就相等了，而默认的 equals 方法满足不了当前的需求，所以要重写 equals 方法。

　　如果重写了 equals 方法就必须重写 hashcode 法，否则就会降低 map 等集合的索引速度。

　　同时复写 hashcode 和 equals 方法，有时可以添加自定义逻辑，且不必调用超类的实现。

### 3.1.1. 覆盖 equals 时需要遵守的通用约定

 　　覆盖 equals 方法看起来似乎很简单，但是如果覆盖不当会导致错误，并且后果相当严重。《Effective Java》一书中提到 “ 最容易避免这类问题的办法就是不覆盖 equals 方法 ”，这句话貌似很搞笑，其实想想也不无道理，其实在这种情况下，类的每个实例都只与它自身相等。如果满足了以下任何一个条件，这就正是所期望的结果：

- **类的每个实例本质上都是唯一的**。对于代表活动实体而不是值的类来说却是如此，例如 Thread。Object 提供的 equals 实现对于这些类来说正是正确的行为。

- **不关心类是否提供了 “ 逻辑相等 ” 的测试功能**。假如 Random 覆盖了 equals，以检查两个 Random 实例是否产生相同的随机数序列，但是设计者并不认为客户需要或者期望这样的功能。在这样的情况下，从 Object 继承得到的 equals 实现已经足够了。

- **超类已经覆盖了 equals，从超类继承过来的行为对于子类也是合适的**。大多数的 Set 实现都从 AbstractSet 继承 equals 实现，List 实现从 AbstractList 继承 equals 实现，Map 实现从 AbstractMap 继承 equals 实现。

- **类是私有的或者是包级私有的，可以确定它的 equals 方法永远不会被调用**。在这种情况下，无疑是应该覆盖 equals 方法的，以防止它被意外调用：

  ```java
  @Override
  
  public boolean equals(Object o){
  
   throw new AssertionError(); //Method is never called
  
  }
  ```

　　在覆盖 equals 方法的时候，必须要遵守它的通用约定。下面是约定的内容，来自 Object 的规范[JavaSE6]。

- **自反性**。对于任何非 null 的引用值 x，x.equals(x) 必须返回 true。
- **对称性**。对于任何非 null 的引用值 x 和 y，当且仅当 y.equals(x) 返回 true 时，x.equals(y) 必须返回 true。
- **传递性**。对于任何非 null 的引用值 x、y 和 z，如果 x.equals(y) 返回 true，并且 y.equals(z) 也返回 true，那么 x.equals(z) 也必须返回 true。
- **一致性**。对于任何非 null 的引用值 x 和 y，只要 equals 的比较操作在对象中所用的信息没有被修改，多次调用该 x.equals(y) 就会一直地返回 true，或者一致地返回 false。
- 对于任何非 null 的引用值 x，x.equals(null) 必须返回 false。


　　结合以上要求，得出了以下实现高质量 equals 方法的诀窍：

1. **使用 == 符号检查 “ 参数是否为这个对象的引用 ” **。如果是，则返回 true。这只不过是一种性能优化，如果比较操作有可能很昂贵，就值得这么做。
2. **使用 instanceof 操作符检查 “ 参数是否为正确的类型 ”**。如果不是，则返回 false。一般来说，所谓 “ 正确的类型 ” 是指 equals 方法所在的那个类。
3. **把参数转换成正确的类型。**因为转换之前进行过 instanceof 测试，所以确保会成功。
4. **对于该类中的每个 “ 关键 ” 域，检查参数中的域是否与该对象中对应的域相匹配**。如果这些测试全部成功，则返回 true，否则返回 false。
5. **当编写完成了 equals 方法之后，检查 “ 对称性 ”、“ 传递性 ”、“ 一致性 ”。**
   **注意：**

- 覆盖 equals 时总要覆盖 hashCode()。
- 不要企图让 equals 方法过于智能。
- 不要将 equals 声明中的 Object 对象替换为其他的类型（因为这样我们并没有覆盖 Object 中的 equals 方法哦）。

### 3.1.2. 覆盖 equals 时总要覆盖 hashCode
 　　一个很常见的错误根源在于没有覆盖 hashCode 方法。在每个覆盖了 equals 方法的类中，也必须覆盖 hashCode 方法。如果不这样做的话，就会违反 Object.hashCode 的通用约定，从而导致该类无法结合所有基于散列的集合一起正常运作，这样的集合包括 HashMap、HashSet 和 Hashtable。

- 在应用程序的执行期间，只要对象的 equals 方法的比较操作所用到的信息没有被修改，那么对这同一个对象调用多次，hashCode 方法都必须始终如一地返回同一个整数。在同一个应用程序的多次执行过程中，每次执行所返回的整数可以不一致。
- 如果两个对象根据 equals() 方法比较是相等的，那么调用这两个对象中任意一个对象的 hashCode 方法都必须产生同样的整数结果。
- 如果两个对象根据 equals() 方法比较是不相等的，那么调用这两个对象中任意一个对象的 hashCode 方法，则不一定要产生相同的整数结果。但是程序员应该知道，给不相等的对象产生截然不同的整数结果，有可能提高散列表的性能。

## 3.2. 说一说你对 java.lang.Object 对象中 hashcode 和 equals 方法的理解。在什么场景下需要重新实现这两个方法。

https://blog.csdn.net/qq_21163061/article/details/73606523

https://blog.csdn.net/jingzi123456789/article/details/106224146

 　　Object 的对象 equals 方法是判断两个对象是否指向同一个地址，并不一定能满足我们的需求，比如 String 就重写了 Object 的 equals 方法，如果重写了 equals 方法就必须重写 hashcode 方法，否则就会降低 map 等集合的索引速度。

## 3.3. 这样的 a.hashcode() 有什么用，与 a.equals(b) 有什么关系？

```java
//hashCode方法部分源码
public native int hashCode();
 
//equals方法部分源码
public boolean equals(Object obj) {
        return (this == obj);
}
```

### 3.3.1. hashCode() 有什么用?

 　　hashCode() 方法提供了对象的 hashCode 值，是一个 native 方法，返回的默认值与 System.identityHashCode(obj) 一致。

 　　hashCode()  的作用是获取哈希码，也称为散列码；它实际上是返回一个 int 整数。这个哈希码的作用是确定该对象在哈希表中的索引位置。因为 hashCode()  在 Object 类中，所以任何类都可以重写 hashCode() ，但是如果哪个类没有重写 hashCode() ，当调用 hashCode()  默认行为是对堆上的对象产生独特值。

　　hashCode() 的返回值通常是对象头部的一部分二进制位组成的数字，具有一定的标识对象的意义存在，但绝不定于地址。

 　　作用是：用一个数字来标识对象。比如在 HashMap、HashSet 等类似的集合类中，如果用某个对象本身作为 Key，即要基于这个对象实现 hash 的写入和查找，那么对象本身如何实现这个呢？就是基于 hashcode 这样一个数字来完成的，只有数字才能完成计算和对比操作。

### 3.3.2. equals 与 hashCode 的关系?

1. 如果两个对象 equals，Java 运行时环境会认为他们的 hashcode 一定相等。 
2. 如果两个对象不 equals，他们的 hashcode 有可能相等。 
3. 如果两个对象 hashcode 相等，他们不一定 equals。 
4. 如果两个对象 hashcode 不相等，他们一定不 equals。 

### 3.3.3 hashcode 是否唯一

 　　hashcode 只能说是标识对象，在 hash 算法中可以将对象相对离散开，这样就可以在查找数据的时候根据这个 key 快速缩小数据的范围，但 hashcode 不一定是唯一的，所以 hash 算法中定位到具体的链表后，需要循环链表，然后通过 equals 方法来对比 Key 是否是一样的。

## 3.4. 有没有可能 2 个不相等的对象有相同的 hashcode

 　　有，因为 hashcode 是为了确定对象保存在散列表的位置，但是有可能会发生哈希冲突，就是因为对象的 hashcode 会相同，导致哈希冲突。

1、如果两个对象 equals，Java 运行时环境会认为他们的 hashcode 一定相等。
2、如果两个对象不 equals，他们的 hashcode 有可能相等。
3、如果两个对象 hashcode 相等，他们不一定 equals。
4、如果两个对象 hashcode 不相等，他们一定不 equals。

 　　HashSet 和 HashMap 一直都是 JDK 中最常用的两个类，HashSet 要求不能存储相同的对象，HashMap 要求不能存储相同的键。

  　　那么 Java 运行时环境是如何判断 HashSet 中相同对象、HashMap 中相同键的呢？当存储了 “ 相同的东西 ” 之后 Java 运行时环境又将如何来维护呢？ 

 　　在研究这个问题之前，首先说明一下 JDK 对 equals(Object obj) 和 hashcode() 两个方法的定义和规范：在 Java 中任何一个对象都具备 equals(Object obj) 和 hashcode() 这两个方法，因为他们是在 Object 类中定义的。 

 　　equals(Object obj) 方法用来判断两个对象是否 “ 相同 ”，如果 “ 相同 ” 则返回 true，否则返回 false。 

 　　hashcode() 方法返回一个 int 数，在 Object 类中的默认实现是 “ 将该对象的内部地址转换成一个整数返回 ”。 

 　　接下来有两个个关于这两个方法的重要规范： 

* 规范 1：若重写 equals(Object obj) 方法，有必要重写 hashcode() 方法，确保通过 equals(Object obj) 方法判断结果为 true 的两个对象具备相等的 hashcode() 返回值。说得简单点就是：“ 如果两个对象相同，那么他们的 hashcode 应该相等”。不过请注意：这个只是规范，如果你非要写一个类让 equals(Object obj) 返回 true 而 hashcode() 返回两个不相等的值，编译和运行都是不会报错的。不过这样违反了 Java 规范，程序也就埋下了 BUG。

* 规范 2：如果 equals(Object obj) 返回 false，即两个对象 “ 不相同 ”，并不要求对这两个对象调用 hashcode() 方法得到两个不相同的数。说的简单点就是：“ 如果两个对象不相同，他们的 hashcode 可能相同”。 

 　　根据这两个规范，可以得到如下推论： 

1. 如果两个对象 equals，Java 运行时环境会认为他们的 hashcode 一定相等。 
2. 如果两个对象不 equals，他们的 hashcode 有可能相等。 
3. 如果两个对象 hashcode 相等，他们不一定 equals。 
4. 如果两个对象 hashcode 不相等，他们一定不 equals。 

 　　这样就可以推断 Java 运行时环境是怎样判断 HashSet 和 HastMap 中的两个对象相同或不同了。先判断 hashcode 是否相等，再判断是否 equals。 

# 4. 请结合 OO 设计理念，谈谈访问修饰符 public 、private 、protected、default 在应用设计中的作用

https://blog.csdn.net/riemann_/article/details/87487472

 　　OO 就是面向对象，而面向对象的四大特性是：抽象、封装、继承、多态。

 　　封装也就是把客观事物封装成抽象的类，并且类可以把自己的数据和方法只让可信的类或者对象操作，对不可信的进行信息隐藏，所以可以通过public、private、protected、default 来进行访问控制。

 　　修饰符的存在可以在包与包之间、类与类之间产生一种权限的关系，并不能保证随心所欲，这样才能确保安全。

 　　访问修饰符，主要标示修饰块的作用域，方便隔离防护。

* public： Java 语言中访问限制最宽的修饰符，一般称之为 “ 公共的 ”。被其修饰的类、属性以及方法不仅可以跨类访问，而且允许跨包（package）访问。

* private: Java 语言中对访问权限限制的最窄的修饰符，一般称之为 “ 私有的 ”。被其修饰的类、属性以及方法只能被该类的对象访问，其子类不能访问，更不能允许跨包访问。

* protect: 介于 public 和 private 之间的一种访问修饰符，一般称之为 “ 保护形 ”。被其修饰的类、属性以及方法只能被类本身的方法及子类访问，即使子类在不同的包中也可以访问。

* default：即不加任何访问修饰符，通常称为 “ 默认访问模式 “。该模式下，只允许在同一个包中进行访问。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190217005808705.jpg)

 　　类的只有两种 public，default(不同包不可以访问)
 　　public – 都可访问(公有)
 　　private – 类内可访问（私有）
 　　protected – 包内和子类可访问（保护）
 　　不写(default) – 包内可访问 （默认）

 　　Java 方法默认访问级别 : 包访问

 　　Java 类默认访问级别 : 包访问对于一个 Class 的成员变量或成员函数，如果不用 public, protected, private 中的任何一个修饰，那么该成员获得 “ 默认访问控制 ” 级别，即 package access （包访问）。

 　　属于 package access 的成员可以被同一个包中的其他类访问，但不能被其他包的类访问。

 　　包访问的控制力弱于 private，但强于 protected。因为一方面，只要是子类，不管子类与父类是否位于同一个包中，那么子类都可以访问父 类中的 protected 方法。但是一旦位于原类的包外，不管是否是其子类，都无法访问其属于 package access 级别的成员。而另一方面，一个类可以访问同一个包中另一个类的 package access 成员，同时也能访问其 protected 成员。

 　　(注：package 是 Java 中的关键字，虽然包访问也是一种访问控制级别，但关键字 ” package ” 只能用来表示类属于哪个包，而不能像 ” private ”、” public ” 那样放到成员变量或函数前面，作为访问控制修饰符。)

 　　访问级别保护的强度：public < protected < 默认 < private。

# 5. String

## 5.1. 在自己的代码中，如果创建一个 java.lang.String 类，这个类是否可以被类加载器加载？为什么？

![](image/双亲委派.jpg)

### 5.1.1. 双亲委派模型

　　类加载器分为两种：一是启动类加载器（Bootstrap ClassLoader），是 C++ 实现的，是 JVM 的一部分；另一种是其他的类加载器，是 Java 实现的，独立于 JVM，全部都继承自抽象类 java.lang.ClassLoader。JDK 自带了三种类加载器，分别是启动类加载器（Boostrap ClassLoader）、扩展类加载器（Extension ClassLoader）、应用程序类加载器（Application ClassLoader）。后两种加载器是继承自抽象类 java.lang.ClassLoader。

　　一般是：自定义类加载器 -> 扩展类加载器 -> 启动类加载器

　　上面的层次关系被称为双亲委派模型（Parents Delegation Model）。除了最顶层的启动类加载器外，其余的类加载器都有对应的父亲加载器。

　　双亲委托机制：如果一个类加载器收到了类加载的请求，它首先不会自己尝试去加载这个类，而是把这个请求委派给父类加载器，每一个层次的类加载器都是如此，因此所有的加载请求最终到达顶层的启动类加载器，只有当父亲加载器反馈自己无法完成加载请求时（ 指它的搜索范围没有找到所需的类 ），子类加载器才会尝试自己去加载。

　　各个类加载器之间是组合关系，并非继承关系。

　　当一个类加载器收到类加载的请求，它将这个加载请求委派给父类加载器进行加载，每一层加载器都是如此，最终，所有的请求都会传动到启动类加载器。只有当父类加载器自己无法完成加载请求时，子类加载器才会尝试自己加载。

　　双亲委派模型可以确保安全性，可以保证所有的 Java 类库都是由启动类加载器加载。如用户编写的 java.lang.Object，加载请求传递到启动类加载器，启动类加载的是系统中的 Object 对象，而用户编写的 java.lang.Object 不会被加载。如用户编写的 java.lang.virus 类，加载请求传递到启动类加载器，启动类加载器发现 virus 类并不是核心 Java 类，无法进行加载，将会由具体的子类加载器进行加载，而经过不同加载器进行加载的类是无法访问彼此的。由不同加载器加载的类处于不同的运行时包。所有的访问权限都是基于同一个运行时包而言的。

### 5.1.2. 为什么要使用这种双亲委派模型呢？

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

### 5.1.4. 怎么打破双亲委派机制

[怎么打破双亲委派机制](https://blog.csdn.net/xiaobao5214/article/details/81674215)

1. 自定义类加载器，重写 loadClass 方法；
2. 使用线程上下文类加载器；

#### 5.1.4.1. 双亲委派模型破坏史

1. 第一次破坏

　　由于双亲委派模型是在 JDK1.2 之后才被引入的，而类加载器和抽象类 java.lang.ClassLoader 则在 JDK1.0 时代就已经存在，面对已经存在的用户自定义类加载器的实现代码，Java 设计者引入双亲委派模型时不得不做出一些妥协。在此之前，用户去继承 java.lang.ClassLoader 的唯一目的就是为了重写 loadClass() 方法，因为虚拟机在进行类加载的时候会调用加载器的私有方法 loadClassInternal()，而这个方法唯一逻辑就是去调用自己的 loadClass()。

2. 第二次破坏

　　双亲委派模型的第二次 “ 被破坏 ” 是由这个模型自身的缺陷所导致的，双亲委派很好地解决了各个类加载器的基础类的同一问题（ 越基础的类由越上层的加载器进行加载 ），基础类之所以称为 “ 基础 ”，是因为它们总是作为被用户代码调用的 API，但世事往往没有绝对的完美。

　　如果基础类又要调用回用户的代码，那该么办？

　　一个典型的例子就是 JNDI 服务，JNDI 现在已经是 Java 的标准服务，它的代码由启动类加载器去加载（ 在 JDK1.3 时放进去的 rt.jar ），但 JNDI 的目的就是对资源进行集中管理和查找，它需要调用由独立厂商实现并部署在应用程序的 ClassPath 下的 JNDI 接口提供者的代码，但启动类加载器不可能 “ 认识 ” 这些代码。

　　为了解决这个问题，Java 设计团队只好引入了一个不太优雅的设计：线程上下文类加载器 ( Thread Context ClassLoader )。这个类加载器可以通过 java.lang.Thread 类的 setContextClassLoader() 方法进行设置，如果创建线程时还未设置，他将会从父线程中继承一个，如果在应用程序的全局范围内都没有设置过的话，那这个类加载器默认就是应用程序类加载器。

　　有了线程上下文加载器，JNDI 服务就可以使用它去加载所需要的 SPI 代码，也就是父类加载器请求子类加载器去完成类加载的动作，这种行为实际上就是打通了双亲委派模型层次结构来逆向使用类加载器，实际上已经违背了双亲委派模型的一般性原则，但这也是无可奈何的事情。Java 中所有涉及 SPI 的加载动作基本上都采用这种方式，例如 JNDI、JDBC、JCE、JAXB 和 JBI 等。

3. 第三次破坏

　　双亲委派模型的第三次 “ 被破坏 ” 是由于用户对程序动态性的追求导致的，这里所说的 “ 动态性 ” 指的是当前一些非常 “ 热门 ” 的名词：代码热替换、模块热部署等，简答的说就是机器不用重启，只要部署上就能用。

　　OSGi 实现模块化热部署的关键则是它自定义的类加载器机制的实现。每一个程序模块 (Bundle) 都有一个自己的类加载器，当需要更换一个 Bundle 时，就把 Bundle 连同类加载器一起换掉以实现代码的热替换。在 OSGi 幻境下，类加载器不再是双亲委派模型中的树状结构，而是进一步发展为更加复杂的网状结构，当受到类加载请求时，OSGi 将按照下面的顺序进行类搜索：

1. 将 java.＊ 开头的类委派给父类加载器加载。
2. 否则，将委派列表名单内的类委派给父类加载器加载。
3. 否则，将 Import 列表中的类委派给 Export 这个类的 Bundle 的类加载器加载。
4. 否则，查找当前 Bundle 的 ClassPath，使用自己的类加载器加载。
5. 否则，查找类是否在自己的 Fragment Bundle 中，如果在，则委派给 Fragment Bundle 的类加载器加载。
6. 否则，查找 Dynamic Import 列表的 Bundle，委派给对应 Bundle 的类加载器加载。
7. 否则，类加载器失败。

# 6. 深拷贝和浅拷贝的区别

　　浅拷贝和深拷贝都是针对一个已有对象的操作。

　　在 Java 中，除了基本数据类型（元类型）之外，还存在类的实例对象这个引用数据类型。而一般使用 = 号做赋值操作的时候。对于基本数据类型，实际上是拷贝的它的值，但是对于对象而言，其实赋值的只是这个对象的引用，将原对象的引用传递过去，他们实际上还是指向同一个对象。

　　而浅拷贝和深拷贝就是这个基础之上做的区分，如果在拷贝这个对象的时候，只对基本数据类型进行了拷贝，而对引用数据类型只是进行了引用的传递，而没有真实的创建一个新的对象，则认为是浅拷贝。反之，在对引用数据类型进行拷贝的时候，创建了一个新的对象，并且复制其内的成员变量，则认为是深拷贝。

　　所谓的浅拷贝和深拷贝，只是在拷贝对象的时候，对类的实例对象这种引用数据类型的不同操作而已。

　　总的来说：

　　浅拷贝：对基本数据类型进行值传递，对引用数据类型进行引用传递般的拷贝，此为浅拷贝。

![](image/浅拷贝.jpg)

　　深拷贝：对基本数据类型进行值传递，对引用数据类型，创建一个新的对象，并复制其内容，此为深拷贝。

![](image/深拷贝.jpg)

　　如果一个对象内部只有基本数据类型，那用 clone() 方法获取到的就是这个对象的深拷贝，而如果其内部还有引用数据类型，那用 clone() 方法就是一次浅拷贝的操作。

# 7. HashSet 内部是如何工作的

　　HashSet 的底层是使用 HashMap 来实现的，通过 key 的唯一的特性，主要将 set 构建的对象放入 key 中，以这样的方式来使用集合的遍历一些特性，从而可以直接用 Set 来进行调用。

　　HashSet 的内部采用 HashMap 来实现，由于 Map 需要 key 和 value，所以 HashSet 中所有 key 的都有一个默认 value，类似于 HashMap，HashSet 不允许重复的 key，只允许有一个 null key，意思就是 HashSet 中只允许存储一个 null 对象。

　　HashSet的部分源代码：

```java
private transient HashMap<E,Object> map;

private static final Object PRESENT = new Object();

public HashSet() {

    map = new HashMap<>();

}

public boolean add(E e) {

    return map.put(e, PRESENT)==null;

}
```

# 8. 序列化

https://www.jianshu.com/p/208ac4a71c6f Android 序列化

## 8.1. 什么是序列化与反序列化

　　序列化：把对象转换为字节序列的过程称为对象的序列化。

　　反序列化：把字节序列恢复为对象的过程称为对象的反序列化。

　　 Java 序列化是指把 Java 对象转换为字节序列的过程；而 Java 反序列化是指把字节序列恢复为 Java 对象的过程。

　　 Java 对象序列化是将实现了 Serializable 接口的对象转换成一个字节序列，能够通过网络传输、文件存储等方式传输 ，传输过程中却不必担心数据在不同机器、不同环境下发生改变，也不必关心字节的顺序或其他任何细节，并能够在以后将这个字节序列完全恢复为原来的对象(恢复这一过程称之为反序列化)。

　　 对象的序列化是非常有趣的，因为利用它可以实现轻量级持久性，“ 持久性 ” 意味着一个对象的生存周期不单单取决于程序是否正在运行，它可以生存于程序的调用之间。通过将一个序列化对象写入磁盘，然后在重新调用程序时恢复该对象，从而达到实现对象的持久性的效果。

　　 本质上讲，序列化就是把实体对象状态按照一定的格式写入到有序字节流，反序列化就是从有序字节流重建对象，恢复对象状态。

- 序列化：对象序列化的最主要的用处就是在传递和保存对象的时候，保证对象的完整性和可传递性。序列化是把对象转换成有序字节流，以便在网络上传输或者保存在本地文件中。核心作用是对象状态的保存与重建。

  由于存在于内存中的对象都是暂时的，无法长期驻存，为了把对象的状态保持下来，这时需要把对象写入到磁盘或者其他介质中，这个过程就叫做序列化。

- 反序列化：客户端从文件中或网络上获得序列化后的对象字节流，根据字节流中所保存的对象状态及描述信息，通过反序列化重建对象。

  反序列化恰恰是序列化的反向操作，也就是说，把已存在在磁盘或者其他介质中的对象，反序列化（读取）到内存中，以便后续操作，而这个过程就叫做反序列化。

　　概括性来说序列化是指将对象实例的状态存储到存储媒体（磁盘或者其他介质）的过程。在此过程中，先将对象的公共字段和私有字段以及类的名称（包括类所在的程序集）转换为字节流，然后再把字节流写入数据流。在随后对对象进行反序列化时，将创建出与原对象完全相同的副本。

## 8.2. 序列化的应用场景

- 当你想把的内存中的对象状态保存到一个文件中或者数据库或者磁盘中的时候。
- 当你想用套接字在网络上传送对象的时候。
- 当你想通过RMI传输对象的时候。

　　当两个进程进行远程通信时，可以相互发送各种类型的数据，包括文本、图片、音频、视频等， 而这些数据都会以二进制序列的形式在网络上传送。那么当两个 Java 进程进行通信时，能否实现进程间的对象传送呢？答案是可以的。如何做到呢？这就需要 Java 序列化与反序列化了。换句话说，一方面，发送方需要把这个 Java 对象转换为字节序列，然后在网络上传送，另一方面，接收方需要从字节序列中恢复出 Java 对象。

　　Java 序列化的好处。其好处一是实现了数据的持久化，通过序列化可以把数据永久地保存到硬盘上（ 通常存放在文件里 ），二是，利用序列化实现远程通信，即在网络上传送对象的字节序列。

　　不同进程 / 程序间进行远程通信时，可以相互发送各种类型的数据，包括文本、图片、音频、视频等，而这些数据都会以二进制序列的形式在网络上传送。

　　那么当两个 Java 进程进行通信时，能否实现进程间的对象传送呢？当然是可以的！如何做到呢？这就需要使用  Java 序列化与反序列化了。发送方需要把这个Java对象转换为字节序列，然后在网络上传输，接收方则需要将字节序列中恢复出 Java 对象。

## 8.3. 序列化的好处

　　Java 序列化有哪些好处：

- 实现了数据的持久化，通过序列化可以把数据永久地保存到硬盘上(如：存储在文件里)，实现永久保存对象。
- 利用序列化实现远程通信，即：能够在网络上传输对象。

　　为什么要序列化，那就是说一下序列化的好处喽，序列化有什么什么优点，所以我们要序列化。

**一：对象序列化可以实现分布式对象。**

　　主要应用例如：RMI ( 即远程调用 Remote Method Invocation ) 要利用对象序列化运行远程主机上的服务，就像在本地机上运行对象时一样。

**二：java 对象序列化不仅保留一个对象的数据，而且递归保存对象引用的每个对象的数据。**

　　可以将整个对象层次写入字节流中，可以保存在文件中或在网络连接上传递。利用对象序列化可以进行对象的 " 深复制 "，即复制对象本身及引用的对象本身。序列化一个对象可能得到整个对象序列。

**三：序列化可以将内存中的类写入文件或数据库中。**

　　比如：将某个类序列化后存为文件，下次读取时只需将文件中的数据反序列化就可以将原先的类还原到内存中。也可以将类序列化为流数据进行传输。

　　总的来说就是将一个已经实例化的类转成文件存储，下次需要实例化的时候只要反序列化即可将类实例化到内存中并保留序列化时类中的所有变量和状态。

**四：对象、文件、数据，有许多不同的格式，很难统一传输和保存。**

　　序列化以后就都是字节流了，无论原来是什么东西，都能变成一样的东西，就可以进行通用的格式传输或保存，传输结束以后，要再次使用，就进行反序列化还原，这样对象还是对象，文件还是文件。

## 8.4. 如何实现序列化

　　一个对象要实现序列化操作，该类就必须实现了 Serializable 接口或者 Parcelable 接口，其中 Serializable 接口是在 java 中的序列化抽象类，而 Parcelable 接口则是 android 中特有的序列化接口，在某些情况下， Parcelable 接口实现的序列化更为高效。实现序列化操作时必须实现 Serializable 接口或者 Parcelable 接口之一。

### 8.4.1. Serizable

　　Serializable 是 java 提供的一个序列化接口，它是一个空接口，专门为对象提供标准的序列化和反序列化操作，使用 Serializable 实现类的序列化比较简单，只要在类声明中实现 Serializable 接口即可，同时强烈建议声明序列化标识。

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

　　如上述代码所示，User 类实现的 Serializable 接口并声明了序列化标识 serialVersionUID，该 ID 由编辑器生成，当然也可以自定义，如 1L，5L，不过还是建议使用编辑器生成唯一标识符。那么 serialVersionUID 有什么作用呢？实际上我们不声明 serialVersionUID 也是可以的，因为在序列化过程中会自动生成一个 serialVersionUID 来标识序列化对象。既然如此，那我们还需不需要要指定呢？原因是 serialVersionUID 是用来辅助序列化和反序列化过程的，原则上序列化后的对象中 serialVersionUID 只有和当前类的 serialVersionUID 相同才能够正常被反序列化，也就是说序列化与反序列化的 serialVersionUID 必须相同才能够使序列化操作成功。具体过程是这样的：序列化操作的时候系统会把当前类的 serialVersionUID 写入到序列化文件中，当反序列化时系统会去检测文件中的 serialVersionUID，判断它是否与当前类的 serialVersionUID 一致，如果一致就说明序列化类的版本与当前类版本是一样的，可以反序列化成功，否则失败。报出如下 UID 错误：

```java
Exception in thread "main" java.io.InvalidClassException: com.zejian.test.Client; 
local class incompatible: stream classdesc serialVersionUID = -2083503801443301445, 
local class serialVersionUID = -4083503801443301445
```

　　因此强烈建议指定 serialVersionUID，这样的话即使微小的变化也不会导致 crash 的出现，如果不指定的话只要这个文件多一个空格，系统自动生成的 UID 就会截然不同的，反序列化也就会失败。

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

　　从代码可以看出只需要 ObjectOutputStream 和 ObjectInputStream 就可以实现对象的序列化和反序列化操作，通过流对象把 user 对象写到文件中，并在需要时恢复 userBack 对象，但是两者并不是同一个对象了，反序列化后的对象是新创建的。这里有两点特别注意的是如果反序列类的成员变量的类型或者类名，发生了变化，那么即使 serialVersionUID 相同也无法正常反序列化成功。其次是静态成员变量属于类不属于对象，不会参与序列化过程，使用 transient 关键字标记的成员变量也不参与序列化过程。

　　另外，系统的默认序列化过程是可以改变的，通过实现如下 4 个方法，即可以控制系统的默认序列化和反序列过程：

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
     * 首先系统会先调用 writeReplace 方法,在这个阶段,
     * 可以进行自己操作,将需要进行序列化的对象换成我们指定的对象.
     * 一般很少重写该方法
     */
    private Object writeReplace() throws ObjectStreamException {
        System.out.println("writeReplace invoked");
        return this;
    }
    /**
     * 接着系统将调用 writeObject 方法,
     * 来将对象中的属性一个个进行序列化,
     * 我们可以在这个方法中控制住哪些属性需要序列化.
     * 这里只序列化 name 属性
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        System.out.println("writeObject invoked");
        out.writeObject(this.name == null ? "默认值" : this.name);
    }

    /**
     * 反序列化时,系统会调用 readObject 方法,将我们刚刚在 writeObject 方法序列化好的属性,
     * 反序列化回来.然后通过 readResolve 方法,我们也可以指定系统返回给我们特定的对象
     * 可以不是 writeReplace 序列化时的对象,可以指定其他对象.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        System.out.println("readObject invoked");
        this.name = (String) in.readObject();
        System.out.println("got name:" + name);
    }


    /**
     * 通过 readResolve 方法,我们也可以指定系统返回给我们特定的对象
     * 可以不是 writeReplace 序列化时的对象,可以指定其他对象.
     * 一般很少重写该方法
     */
    private Object readResolve() throws ObjectStreamException {
        System.out.println("readResolve invoked");
        return this;
    }
}
```

　　通过上面的 4 个方法，就可以随意控制序列化的过程了，由于在大部分情况下都没必要重写这4个方法，因此这里也不过介绍了，只要知道有这么一回事就行。

#### 8.4.1.1. 注意事项

1. 当一个父类实现序列化，子类就会自动实现序列化，不需要显式实现 Serializable 接口。

2. 当一个对象的实例变量引用其他对象，序列化该对象时也把引用对象进行序列化。

3. 并非所有的对象都可以进行序列化，比如：

- 安全方面的原因，比如一个对象拥有 private，public 等成员变量，对于一个要传输的对象，比如写到文件，或者进行 RMI 传输等等，在序列化进行传输的过程中，这个对象的 private 等域是不受保护的;
- 资源分配方面的原因，比如 socket，thread 类，如果可以序列化，进行传输或者保存，也无法对他们进行重新的资源分配，而且，也是没有必要这样实现。

4. 声明为 static 和 transient 类型的成员变量不能被序列化。因为 static 代表类的状态，transient 代表对象的临时数据。

5. 序列化运行时会使用一个称为 serialVersionUID 的版本号，并与每个可序列化的类相关联，该序列号在反序列化过程中用于验证序列化对象的发送者和接收者是否为该对象加载了与序列化兼容的类。如果接收者加载的该对象的类的 serialVersionUID 与对应的发送者的类的版本号不同，则反序列化将会导致 InvalidClassException。可序列化类可以通过声明名为 "serialVersionUID" 的字段 ( 该字段必须是静态 (static)、最终 (final) 的 long 型字段 ) 显式声明其自己的 serialVersionUID。

　　如果序列化的类未显式的声明 serialVersionUID，则序列化运行时将基于该类的各个方面计算该类的默认 serialVersionUID 值，如 “ Java(TM) 对象序列化规范 ” 中所述。不过，强烈建议所有可序列化类都显式声明 serialVersionUID 值，原因是计算默认的 serialVersionUID 对类的详细信息具有较高的敏感性，根据编译器实现的不同可能千差万别，这样在反序列化过程中可能会导致意外的 InvalidClassException。因此，为保证 serialVersionUID 值跨不同 java 编译器实现的一致性，序列化类必须声明一个明确的 serialVersionUID 值。还强烈建议使用 private 修饰符显示声明 serialVersionUID ( 如果可能 )，原因是这种声明仅应用于直接声明类 -- serialVersionUID 字段作为继承成员没有用处。数组类不能声明一个明确的 serialVersionUID，因此它们总是具有默认的计算值，但是数组类没有匹配 serialVersionUID 值的要求。

6. Java 有很多基础类已经实现了 serializable 接口，比如 String，Vector 等。但是也有一些没有实现 serializable 接口的。

7. 如果一个对象的成员变量是一个对象，那么这个对象的数据成员也会被保存!这是能用序列化解决深拷贝的重要原因。

### 8.4.2. Parcelable

　　鉴于 Serializable 在内存序列化上开销比较大，而内存资源属于 android 系统中的稀有资源（android 系统分配给每个应用的内存开销都是有限的），为此 android 中提供了 Parcelable 接口来实现序列化操作，Parcelable 的性能比 Serializable 好，在内存开销方面较小，所以在内存间数据传输时推荐使用 Parcelable，如通过 Intent 在 activity 间传输数据，而 Parcelable 的缺点就使用起来比较麻烦，下面给出一个 Parcelable 接口的实现案例，大家感受一下：

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

　　从代码可知，在序列化的过程中需要实现的功能有序列化和反序列以及内容描述。其中 writeToParcel 方法实现序列化功能，其内部是通过 Parcel 的一系列 write 方法来完成的，接着通过 CREATOR 内部对象来实现反序列化，其内部通过 createFromParcel 方法来创建序列化对象并通过 newArray 方法创建数组，最终利用 Parcel 的一系列 read 方法完成反序列化，最后由 describeContents 完成内容描述功能，该方法一般返回 0，仅当对象中存在文件描述符时返回 1。同时由于 User 是另一个序列化对象，因此在反序列化方法中需要传递当前线程的上下文类加载器，否则会报无法找到类的错误。

　　简单用一句话概括来说就是通过 writeToParcel 将我们的对象映射成 Parcel 对象，再通过 createFromParcel 将 Parcel 对象映射成我们的对象。也可以将 Parcel 看成是一个类似 Serliazable 的读写流，通过 writeToParcel 把对象写到流里面，在通过 createFromParcel 从流里读取对象，这个过程需要我们自己来实现并且写的顺序和读的顺序必须一致。ok~，到此 Parcelable 接口的序列化实现基本介绍完。
  
　　那么在哪里会使用到 Parcelable 对象呢？其实通过 Intent 传递复杂类型( 如自定义引用类型数据)的数据时就需要使用 Parcelable 对象，如下是日常应用中 Intent 关于 Parcelable 对象的一些操作方法，引用类型必须实现 Parcelable 接口才能通过 Intent 传递，而基本数据类型，String 类型则可直接通过 Intent 传递而且 Intent 本身也实现了 Parcelable 接口，所以可以轻松地在组件间进行传输。

|                         方法名称                          |                          含义                          |
| :-------------------------------------------------------: | :----------------------------------------------------: |
|          putExtra(String name, Parcelable value)          |         设置自定义类型并实现 Parcelable 的对象         |
|         putExtra(String name, Parcelable[] value)         |       设置自定义类型并实现 Parcelable 的对象数组       |
| putParcelableArrayListExtra(String name, ArrayList value) | 设置List数组，其元素必须是实现了 Parcelable 接口的数据 |

　　除了以上的 Intent 外系统还为我们提供了其他实现 Parcelable 接口的类，再如 Bundle、Bitmap，它们都是可以直接序列化的，因此我们可以方便地使用它们在组件间进行数据传递，当然 Bundle 本身也是一个类似键值对的容器，也可存储 Parcelable 实现类，其 API 方法跟 Intent 基本相似，由于这些属于 android 基础知识点，这里我们就不过多介绍了。

### 8.4.3. Parcelable 与 Serializable 区别

#### 8.4.3.1. 两者的实现差异

　　Serializable 的实现，只需要实现 Serializable 接口即可。这只是给对象打了一个标记（UID），系统会自动将其序列化。而 Parcelabel 的实现，不仅需要实现 Parcelabel 接口，还需要在类中添加一个静态成员变量 CREATOR，这个变量需要实现 Parcelable.Creator 接口，并实现读写的抽象方法。

#### 8.4.3.2. 两者的设计初衷

　　Serializable 的设计初衷是为了序列化对象到本地文件、数据库、网络流、RMI（远程方法调用） 以便数据传输，当然这种传输可以是程序内的也可以是两个程序间的。而 Android 的 Parcelable 的设计初衷是由于 Serializable 效率过低，消耗大，而 android 中数据传递主要是在内存环境中（内存属于 android 中的稀有资源），因此 Parcelable 的出现为了满足数据在内存中低开销而且高效地传递问题。

#### 8.4.3.3. 两者效率选择

　　Serializable 使用 IO 读写存储在硬盘上。序列化过程使用了反射技术，并且期间产生临时对象，优点代码少，在将对象序列化到存储设置中或将对象序列化后通过网络传输时建议选择 Serializable。

　　Parcelable 是直接在内存中读写，我们知道内存的读写速度肯定优于硬盘读写速度，所以 Parcelable 序列化方式性能上要优于 Serializable 方式很多。所以 Android 应用程序在内存间数据传输时推荐使用 Parcelable，如 activity 间传输数据和 AIDL 数据传递。大多数情况下使用 Serializable 也是没什么问题的，但是针对 Android 应用程序在内存间数据传输还是建议大家使用 Parcelable 方式实现序列化，毕竟性能好很多，其实也没多麻烦。

　　Parcelable 也不是不可以在网络中传输，只不过实现和操作过程过于麻烦并且为了防止 android 版本不同而导致 Parcelable 可能不同的情况，因此在序列化到存储设备或者网络传输方面还是尽量选择 Serializable 接口。

# 9. Java 8 的新特性

https://www.jianshu.com/p/0bf8fe0f153b

## 9.1. 接口的默认方法

　　Java 8 允许给接口添加一个非抽象的方法实现，只需要使用 default 关键字即可，这个特征又叫做扩展方法，示例如下：

```csharp
interface Formula {
    double calculate(int a);

    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}
```

　　Formula 接口在拥有 calculate 方法之外同时还定义了 sqrt 方法，实现了 Formula 接口的子类只需要实现一个 calculate 方法，默认方法 sqrt 在子类上可以直接使用。

## 9.2. Lambda 表达式

　　对于函数体可见简单表大。

```java
List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");

Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return b.compareTo(a);
    }
});
```

　　使用 Lambda 表达式：

```java
Collections.sort(names, (a, b) -> b.compareTo(a));
```

　　Java 编译器可以自动推导出参数类型，所以可以不用再写一次类型。

## 9.3. 函数式接口

　　每一个lambda 表达式都对应一个类型，通常是接口类型。而 “ 函数式接口 ” 是指仅仅只包含一个抽象方法的接口，每一个该类型的 lambda 表达式都会被匹配到这个抽象方法。因为默认方法不算抽象方法，所以也可以给你的函数式接口添加默认方法。

　　可以将 lambda 表达式当作任意只包含一个抽象方法的接口类型，确保接口一定达到这个要求，只需要给你的接口添加 @FunctionalInterface 注解，编译器如果发现标注了这个注解的接口有多于一个抽象方法的时候会报错的。

```tsx
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
Integer converted = converter.convert("123");
System.out.println(converted);    // 123
```

　　需要注意如果 @FunctionalInterface 如果没有指定，上面的代码也是对的。

## 9.4. 方法与构造函数引用

　　可以通过静态方法引用来表示方法的调用：

```rust
Converter<String, Integer> converter = Integer::valueOf;
Integer converted = converter.convert("123");
System.out.println(converted);   // 123
```

　　Java 8 允许你使用 :: 关键字来传递方法或者构造函数引用，上面的代码展示了如何引用一个静态方法，我们也可以引用一个对象的方法：

```ruby
 converter = something::startsWith;
String converted = converter.convert("Java");
System.out.println(converted);    // "J"
```

　　构造函数使用 :: new 来引用的，

```java
PersonFactory<Person> personFactory = Person::new;
```

　　只需要使用 Person::new 来获取 Person 类构造函数的引用，Java 编译器会自动根据方法的签名来选择合适的构造函数。

## 9.5. Lambda 作用域

　　在 lambda 表达式中访问外层作用域和老版本的匿名对象中的方式很相似。可以直接访问标记了 final 的外层局部变量，或者实例的字段以及静态变量。

## 9.6. 访问局部变量

　　可以直接在 lambda 表达式中访问外层的局部变量：

```dart
final int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

　　但是和匿名对象不同的是，这里的变量 num 可以不用声明为 final，该代码同样正确：

```dart
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

　　不过这里的 num 必须不可被后面的代码修改（即隐性的具有final的语义），例如下面的就无法编译：

```dart
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
num = 3;
```

　　在 lambda 表达式中试图修改 num 同样是不允许的。

## 9.7. 访问对象字段与静态变量

　　和本地变量不同的是，lambda 内部对于实例的字段以及静态变量是即可读又可写。该行为和匿名对象是一致的：

```java
class Lambda4 {
    static int outerStaticNum;
    int outerNum;

    void testScopes() {
        Converter<Integer, String> stringConverter1 = (from) -> {
            outerNum = 23;
            return String.valueOf(from);
        };

        Converter<Integer, String> stringConverter2 = (from) -> {
            outerStaticNum = 72;
            return String.valueOf(from);
        };
    }
}
```

## 9.8. 访问接口的默认方法

　　上面的接口 Formula 定义了一个默认方法 sqrt 可以直接被 formula 的实例包括匿名对象访问到，但是在 lambda 表达式中这个是不行的。
　　Lambda 表达式中是无法访问到默认方法的，以下代码将无法编译：

```cpp
Formula formula = (a) -> sqrt( a * 100);
Built-in Functional Interfaces
```

　　JDK 1.8 API 包含了很多内建的函数式接口，在老 Java 中常用到的比如 Comparator 或者 Runnable 接口，这些接口都增加了 @FunctionalInterface 注解以便能用在 lambda 上。

　　Java 8 API 同样还提供了很多全新的函数式接口来让工作更加方便，有一些接口是来自 Google Guava 库里的，这些都被扩展到 lambda 上。

## 9.9. Data API

　　Java 8 在包 java.time 下包含了一组全新的时间日期 API。新的日期 API 和开源的 Joda-Time 库差不多，但又不完全一样。

　　Clock 时钟：Clock 类提供了访问当前日期和时间的方法，Clock 是时区敏感的，可以用来取代 System.currentTimeMillis() 来获取当前的微秒数。某一个特定的时间点也可以使用 Instant 类来表示，Instant 类也可以用来创建老的 java.util.Date 对象。

　　Timezones 时区：在新 API 中时区使用 ZoneId 来表示。时区可以很方便的使用静态方法 of 来获取到。 时区定义了到 UTS 时间的时间差，在 Instant 时间点对象到本地日期对象之间转换的时候是极其重要的。

　　LocalTime 本地时间：LocalTime 定义了一个没有时区信息的时间，例如 晚上 10 点或者 17:30:15。

　　LocalDate 本地日期：LocalDate 表示了一个确切的日期，比如 2014-03-11。该对象值是不可变的，用起来和 LocalTime 基本一致。

　　LocalDateTime 本地日期时间：LocalDateTime 同时表示了时间和日期，相当于前两节内容合并到一个对象上了。LocalDateTime 和 LocalTime 还有 LocalDate 一样，都是不可变的。LocalDateTime 提供了一些能访问具体字段的方法。

## 9.10. Annotation 注解

　　在 Java 8 中支持多重注解了，先看个例子来理解一下是什么意思。
 首先定义一个包装类 Hints 注解用来放置一组具体的 Hint 注解：

```css
@interface Hints {
    Hint[] value();
}

@Repeatable(Hints.class)
@interface Hint {
    String value();
}
```

　　Java 8 允许把同一个类型的注解使用多次，只需要给该注解标注一下 @Repeatable 即可。

　　例 1: 使用包装类当容器来存多个注解（老方法）

```kotlin
@Hints({@Hint("hint1"), @Hint("hint2")})
class Person {}
```

　　例 2：使用多重注解（新方法）

```kotlin
@Hint("hint1")
@Hint("hint2")
class Person {}
```

　　第二个例子里 java 编译器会隐性的定义好 @Hints 解，即便没有在 Person 类上定义 @Hints 注解，还是可以通过  getAnnotation(Hints.class)  来获取 @Hints 注解，更加方便的方法是使用 getAnnotationsByType 可以直接获取到所有的 @Hint 注解。

# 10. 运算符

##  10.1. java 运算符与（&） 、非（～）、或（|）、异或（^）

　　位运算符主要针对二进制，它包括了：“与”、“非”、“或”、“异或”。从表面上看似乎有点像逻辑运算符，但逻辑运算符是针对两个关系运算符来进行逻辑运算，而位运算符主要针对两个二进制数的位进行逻辑运算。下面详细介绍每个位运算符。

### 10.1.1．与运算符

　　与运算符用符号 “&” 表示，其使用规律如下：
两个操作数中位都为 1，结果才为1，否则结果为 0，例如下面的程序段。

```java
public class data13 {
    public static void main(String[] args) {
        int a = 129;
        int b = 128;
        System.out.println("a 和b 与的结果是：" + (a & b));
    }
}
```

　　运行结果
　　a 和b 与的结果是：128
　　下面分析这个程序：
　　“a” 的值是129，转换成二进制就是 10000001，而 “b” 的值是128，转换成二进制就是 10000000。根据与运算符的运算规律，只有两个位都是1，结果才是1，可以知道结果就是 10000000，即 128。
### 10.1.2．或运算符
　　或运算符用符号 “|” 表示，其运算规律如下：
　　两个位只要有一个为 1，那么结果就是 1，否则就为 0，下面看一个简单的例子。

```java
public class data14 {
    public static void main(String[] args) {
        int a = 129;
        int b = 128;
        System.out.println("a 和b 或的结果是：" + (a | b));
    }
}
```

　　运行结果
　　a 和 b 或的结果是：129
　　下面分析这个程序段：
　　a 的值是129，转换成二进制就是 10000001，而 b 的值是128，转换成二进制就是 10000000，根据或运算符的运算规律，只有两个位有一个是 1，结果才是 1，可以知道结果就是 10000001，即 129。

### 10.1.3．非运算符
　　非运算符用符号 “~” 表示，其运算规律如下：

　　如果位为0，结果是1，如果位为1，结果是0，下面看一个简单例子。

```java
public class data15 {
    public static void main(String[] args) {
        int a = 2;
        System.out.println("a 非的结果是：" + (~a));
    }
}
```

　　运行结果
　　a 非得结果是：-3
　　下面分析这个程序段：
　　a 的值是 2 ，转换成二进制就是 00000010，非 a 就是 1111 1101，就是 -3 。

### 10.1.4．异或运算符

　　异或运算符是用符号 “^” 表示的，其运算规律是：
两个操作数的位中，相同则结果为 0，不同则结果为 1。下面看一个简单的例子。

```java
public class data16 {
    public static void main(String[] args) {
        int a = 15;
        int b = 2;
        System.out.println("a 与 b 异或的结果是：" + (a ^ b));
    }
}
```

　　运行结果
　　a 与 b 异或的结果是：13
　　分析上面的程序段：a 的值是 15，转换成二进制为 1111，而 b 的值是 2，转换成二进制为 0010，根据异或的运算规律，可以得出其结果为1101 即13。

