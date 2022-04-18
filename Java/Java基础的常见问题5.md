# Java 基础的常见问题 5

[TOC]

# 1. Java 异常处理机制

　　在 Java 应用程序中，异常处理机制为：抛出异常，捕捉异常。     

**抛出异常**：当一个方法出现错误引发异常时，方法创建异常对象并交付运行时系统，异常对象中包含了异常类型和异常出现时的程序状态等异常信息。运行时系统负责寻找处置异常的代码并执行。     

**捕捉异常**：在方法抛出异常之后，运行时系统将转为寻找合适的异常处理器（exception handler）。潜在的异常处理器是异常发生时依次存留在调用栈中的方法的集合。当异常处理器所能处理的异常类型与方法抛出的异常类型相符时，即为合适的异常处理器。运行时系统从发生异常的方法开始，依次回查调用栈中的方法，直至找到含有合适异常处理器的方法并执行。当运行时系统遍历调用栈而未找到合适的异常处理器，则运行时系统终止。同时，意味着Java程序的终止。     

***对于运行时异常、错误或可查异常，Java技术所要求的异常处理方式有所不同。***     

由于运行时异常的不可查性，为了更合理、更容易地实现应用程序，Java规定，运行时异常将由Java运行时系统自动抛出，允许应用程序忽略运行时异常。     

对于方法运行中可能出现的Error，当运行方法不欲捕捉时，Java允许该方法不做任何抛出声明。因为，大多数Error异常属于永远不能被允许发生的状况，也属于合理的应用程序不该捕捉的异常。     

对于所有的可查异常，Java规定：一个方法必须捕捉，或者声明抛出方法之外。也就是说，当一个方法选择不捕捉可查异常时，它必须声明将抛出异常。     

能够捕捉异常的方法，需要提供相符类型的异常处理器。所捕捉的异常，可能是由于自身语句所引发并抛出的异常，也可能是由某个调用的方法或者Java运行时 系统等抛出的异常。也就是说，一个方法所能捕捉的异常，一定是Java代码在某处所抛出的异常。简单地说，异常总是先被抛出，后被捕捉的。      

任何Java代码都可以抛出异常，如：自己编写的代码、来自Java开发环境包中代码，或者Java运行时系统。无论是谁，都可以通过Java的throw语句抛出异常。从方法中抛出的任何异常都必须使用throws子句。捕捉异常通过try-catch语句或者try-catch-finally语句实现。      

总体来说，Java规定：对于可查异常必须捕捉、或者声明抛出。允许忽略不可查的RuntimeException和Error。 

# 2. 描述一下 JAVA 集合类的类图结构。

![img](https://img-blog.csdn.net/20131126154335359?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdmtpbmdfd2FuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

Set、List、Queue（接口） 都实现 Collection 接口。AbstractCollection 是 Collection 的实现抽象类。AbstractCollection 有三个抽象子类 AbstractSet、AbstractList、AbstractQueue。

TreeSet、HashSet 实现 Set 接口，继承 AbstractSet（抽象累） 类，LinkedHashSet 是 HashSet 的子类。

ArrayList、LinkedList、Vector 是实现 List 接口的，Vector 和 ArrayList 继承 AbstractList 抽象类。Stack 是 Vector 的子类。 LInkedList 继承 abstractSequentialList 抽象类，AbstractSequentialList 是 AbstractList 的抽象子类。

Deque 是 Queue 的子接口，LinkedList 实现了 Lits 和 Dequeue 两个接口。PrioritQueue 是 abstractQueue 的实现类。

![img](https://img-blog.csdn.net/20131126154350953?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdmtpbmdfd2FuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

Map 是一个接口，abstractMap 是 Map 的实现抽象类，HashMap 、 WeakHashMap 和 TreeMap 是 abstractMap 的实现类，LinkedHashMap 是 HashMap 的子类，SortedMap 是实现 Map 的接口，TreeMap 还实现了 NavigableMap 接口（SortedMap 的子接口）。HashTable 实现 Map 接口的类。WeakHashtable 是 Hashtable 的子类。

Properties 继承于 Hashtable.表示一个持久的属性集.属性列表中每个键及其对应值都是一个字符串。

Properties 类被许多Java类使用。例如，在获取环境变量时它就作为System.getProperties()方法的返回值。

Properties 定义如下实例变量.这个变量持有一个Properties对象相关的默认属性列表。

# 3. 为什么静态对象不可以访问非静态对象

静态方法是属于类的，即静态方法是随着类的加载而加载的，在加载类时，程序就会为静态方法分配内存。

非静态方法时属于对象的，对象是在类加载之后创建的。

静态方法先于对象存在，所以如果静态方法调用非静态方法的话，可能会报空指针异常。