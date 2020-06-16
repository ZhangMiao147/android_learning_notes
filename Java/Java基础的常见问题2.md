# Java 基础的常见问题2

## 1. 泛型

　　Java 泛型是 JDK 1.5 中引入的一个新特性，泛型的本质是参数化类型，也就是所操作的数据类型被指定为一个参数。

　　特性：只在编译阶段有效。在编译过程中，正确检验泛型结果后，会将泛型的相关信息擦除，并且在对象进入和离开方法的边界处添加类型检查和类型转换的方法。也就是说，泛型信息不会进入到运行时阶段。

### 1.1. 在 jdk 1.5 中，引入了泛型，泛型的存在是用来解决什么问题的。

　　泛型的本质是为了参数化类型，在不创建新的类型情况下，通过泛型指定的不同类型来控制形参具体限制的类型。

## 2. 反射

　　Java 反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法，对于任意一个对象，都能够调用它的任意方法和属性，这种动态获取信息以及动态调用对象方法的功能称为 Java 语言的反射机制。

　　与 Java 反射相关的类如下：

| 类名           | 用途                                               |
| -------------- | -------------------------------------------------- |
| Class 类       | 代表类的实体，在运行的 Java 应用程序中表示类和接口 |
| Field 类       | 代表类的成员变量（成员变量也称为类的属性）         |
| Method  类     | 代表类的方法                                       |
| Constructor 类 | 代表类的构造方法                                   |

### 2.1. Class 类

#### 2.1.1 获得类相关的方法

| 方法                         | 用途                                                   |
| ---------------------------- | ------------------------------------------------------ |
| asSubClass(Class< U > clazz) | 把传递的类的对象转换成代表其子类的对象                 |
| cast(Object obj)             | 把对象转换成代表类或是接口的对象                       |
| getClassLoader()             | 获得类的加载器                                         |
| getClasses()                 | 返回一个数组，数组中包含该类中所有公共类和接口类的对象 |
| getDeclaredClasses()         | 返回一个数组，数组中包含该类中所有类和接口类的对象     |
| forName(String className)    | 根据类名返回类的对象                                   |
| getName()                    | 获得类的完整路径名字                                   |
| newInstance()                | 创建类的实例                                           |
| getPackage()                 | 获取类的包                                             |
| getSimpleName()              | 获得类的名字                                           |
| getSuperclass()              | 获得当前类继承的父类的名字                             |
| getInterfaces()              | 获得当前类实现的类或是接口                             |



#### 2.1.2. 获得类中属性相关的方法

| 方法                          | 用途                   |
| ----------------------------- | ---------------------- |
| getField(String name)         | 获得某个共有的属性对象 |
| getFields()                   | 获得所有共有属性对象   |
| getDeclaredField(String name) | 获得某个属性对象       |
| getDeclaredFields()           | 获得所有属性对象       |



#### 2.1.3. 获得类中注解相关的方法

| 方法                                              | 用途                                   |
| ------------------------------------------------- | -------------------------------------- |
| getAnnotation(Class< A > annotationClass)         | 返回该类中与参数类型匹配的公有注解对象 |
| getAnnotations()                                  | 返回该类所有的公有注解对象             |
| getDeclaredAnnotation(Class< A > annotationClass) | 返回该类中与参数类型匹配的所有主机对象 |
| getDeclaredAnnotations()                          | 返回该类所有的注解对象                 |



#### 2.1.4. 获得类中构造器相关的方法

| 方法                                                 | 用途                                   |
| ---------------------------------------------------- | -------------------------------------- |
| getConstructor(Class... < ? > parameterTypes)        | 获得该类中与参数类型匹配的公有构造方法 |
| getConstructors()                                    | 获得该类的所有公共构造方法             |
| getDeclaredConstructor(Class...< ? > parameterTypes) | 获得该类中与参数类型匹配的构造方法     |
| getDeclaredConstructors()                            | 获得该类所有的构造方法                 |



#### 2.1.5. 获得类中方法相关的方法

| 方法                                                         | 用途                   |
| ------------------------------------------------------------ | ---------------------- |
| getMethod(String name,Class... < ? > parameterTypes)         | 获得该类某个公有的方法 |
| getMethods()                                                 | 获得该类所有公有的方法 |
| getDeclaredMethod(String name,Class... < ? > parameterTypes) | 获得该类某个方法       |
| getDeclaredMethods()                                         | 获得该类所有方法       |



#### 2.1.6. 类中其他重要的方法

| 方法                                                         | 用途                             |
| ------------------------------------------------------------ | -------------------------------- |
| isAnnotation()                                               | 如果是注解类则返回 true          |
| isAnnotationPresent(Class< ? extends Annotation > annotationClass) | 如果是指定注解类型则返回 true    |
| isAnonymousClass()                                           | 如果是匿名类则返回 true          |
| isArray()                                                    | 如果是一个数组类则返回 true      |
| isEnum()                                                     | 如果是枚举类则返回 true          |
| isInstance(Object obj)                                       | 如果 obj 是该类的实例则返回 true |
| isInterface()                                                | 如果是接口类则返回 true          |
| isLocalClass()                                               | 如果是局部类则返回 true          |
| isMemberClass()                                              | 如果是内部类则返回 true          |

### 2.2. Field 类

　　Field 代表类的成员变量（成员变量也称为类的属性）。

| 方法                         | 用途                       |
| ---------------------------- | -------------------------- |
| equals(Object object)        | 属性与 obj 相等则返回 true |
| get(Object obj)              | 获得 obj 中对应的属性值    |
| set(Object obj,Object value) | 设置 obj 中对应属性值      |



### 2.3. Method 类

　　Method 代表类的方法。

| 方法                               | 用途                                                      |
| ---------------------------------- | --------------------------------------------------------- |
| invoke(Object obj, Object... args) | 传递 objecy 对象及参数，调用 object 对象对应的方法 method |



### 2.4. Constructor 类

　　Constructor 代表类的构造方法。

| 方法                            | 用途                       |
| ------------------------------- | -------------------------- |
| newInstance(Object... initargs) | 根据传递的参数创建类的对象 |



### 2.1. 反射的原理，反射创建类实例的三种方式是什么？

```java
        // Book 的实例对象如何表示
        Book book1 = new Book();
        //任何类都是 Class 的实例对象，这个实例对象有三种表示方式
        // 第一种表示方式 - 》 实际在告诉任何一个类都有一个隐含的静态成员变量 class
        Class class1 = Book.class;

/*********************************************/
        // 第二种表示方式，已经知道该类的对象通过 getClass 方法
        Class class2 = book1.getClass();

/*********************************************/
				// 方法三：Class.forName
				Class class3 = null;
        try {
            class3 = Class.forName("Book");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

```

　　不管哪种方式获取 Book 的 Class 实例，这些都是代表了 Book 类的类类型，一个类只可能是 Class 类的一个实例对象，所以不管哪种方式获取的类实例都是同一个。意思就是说 class1、class2、class3 是同一个，是相等的。

### 2.2. 反射中，Class.forName 和 ClassLoader 区别。

1. Class.forName 加载类时将类进行了初始化。
2. ClassLoader 的 loadClass 方法并没有对类进行初始化，只是把类加载到了虚拟机中。

## 3. 代理

　　代理（Proxy）是一种设计模式。提供了间接对目标对象进行访问的方式，即通过代理对象访问目标对象。这样做的好处是：可以在目标对象实现的基础上，增强额外的功能操作，即扩展目标对象的功能。

　　这就符合了设计模式的开闭原则，即在对既有代码不改动的情况下进行功能的扩展。

### 3.1. 三种代理模式

　　三种代理模式：静态代理、动态代理、Cglib 代理。

#### 3.1.1. 静态代理

　　静态代理在使用时，需要定义接口或者父类，被代理对象与代理对象一起实现相同的接口或者是继承相同的父类。

　　调用的时候通过代理对象的方法调用目标对象。

　　优点：可以做到不修改目标对象的功能前提下，对目标功能扩展。

　　缺点：因为代理对象需要与目标对象实现一样的接口，所以会有很多代理类，类太多。同时，一旦接口增加方法，目标对象与代理对象都要维护。

#### 3.1.2. 动态代理

　　动态代理就是在程序运行时 JVM 才为被代理对象生成代理对象。

　　动态代理的特点：

1. 代理对象，不需要实现接口。
2. 代理对象的生成，是利用 JDK 的 API，动态的在内存中构建代理对象，需要执行创建对象/目标对象实现的结构的类型。
3. 动态代理也叫做：JDK 代理、接口代理。

　　JDK 中生成代理对象的代理类就是 Proxy，所在包是 java.lang.reflect。JDK 实现代理只需要使用 Proxy 的 newProxyInstance 方法，该方法需要接收三个参数，完整的写法是：

```java
static Object newProxyInstance(
  ClassLoader loader,  // 目标对象的类加载器
  Class<?>[] interfaces, // 目标对象实现的接口类型
  InvocationHandler h ) // 事件处理，执行目标对象的方法时，会触发事件处理器的方法，会把当前执行目标对象的方法作为参数传入
```

　　代理对象不需要实现接口，但是目标对象一定要实现接口，否则不能用动态代理。

#### 3.1.3. Cglib 代理

　　静态代理和动态代理模式有个共同点就是都要求目标对象是实现一个接口的目标对象，如果目标对象只是一个单独的对象，并没有实现任何的接口，这个时候就可以使用继承以目标对象子类的方式来实现实现代理，这种方法就叫做：Cglib 代理。

　　Cglib 是一个强大的高性能的代码生成包，它可以在运行期扩展 java 类与实现 java 接口。它广泛的被许多 AOP 的框架使用，例如 Spring AOP 和 synaop，为他们提供方法的 interception（拦截）。

　　Cglib 包的底层是通过使用一个字节码处理框架 ASM 来转换字节码并生成新的类。

　　Cglib 子类代理实现方法：

1. 需要引入 cglib 的 jar 文件。
2. 引入功能包后，就可以在内存中动态创建子类。
3. 代理的类不能为 final ，否则报错。
4. 目标对象的方法如果为 final static ，那么就不会被拦截，即不会执行目标对象额外的业务方法。

　　在 Spring 的 AOP 编程中：如果加入容器的目标对象有实现接口，用 JDK 代理，如果目标对象没有实现接口，用 Cglib 代理。

## 4. 注解

　　注解是没有方法的，只有成员变量，变量名就是注解括号中对应的参数名，变量类型是注解括号中对应的参数类型。

　　注解的本质就是一个继承了 Annotation 接口的接口。接口的属性都是 static final 的，而定义接口的方法就相当于注解的属性。

　　解析一个类或者方法的注解往往有两种形式，一种是编译器直接的扫描，一种是运行期反射。编译器的扫描指的是编译器在对 jaba 代码编译字节码的过程中会检测到某个类或者方法被一些注解修饰，这时它就会对这些注解进行某些处理。

　　注解属性类型可以有以下列出的类型：

1. 基本数据类型。
2. String
3. 枚举类型
4. 注解类型
5. Class 类型
6. 以上类型的一维数组类型

### 4.1. 元注解

　　元注解作用在注解的定义上，一般用于指定某个注解生命周期以及作用目标等信息。

#### 4.1.1. @Retension

　　@Retension：表示注解存在阶段是保留在源码（编译器，@Retention(RetentionPolicy.SOURCE)）、字节码（类加载，@Retention(RetentionPolicy.CLASS)）或者是运行期（JVM 中运行，Rentention(RetentionPolicy.RUNTIME)），即这个注解的存活时间。

　　如果是自定义注解，自定义注解如果只存在源码中或者字节码文件中就无法发挥作用，而在运行期间能获取到注解才能实现目的，所以自定义注解中肯定是使用 @Retention(Retention.RUNNTIME)。

#### 4.1.2. @Target

　　@Target 元注解标识注解作用的范围，可以是类、方法、方法参数变量等。

　　@Target 的取值：

1. @Target(ElementType.TYPE) 作用一个类型，比如接口、类、枚举、注解。
2. @Target(ElementType.FIELD) 作用属性字段、枚举的常量。
3. @Target(ElementType.METHOD) 作用方法
4. @Target(ElementType.PARAMETER) 作用方法参数
5. @Target(ElementType.CONSTRUCTOR) 作用构造函数
6. @Target(ElementType.LOCAL_VARIABLE) 作用局部变量
7. @Target(ElementType.ANNOTATION_TYPE) 作用于注解（@Retention 注解中就使用该属性）
8. @Target(ElementType.PACKAGE) 作用于包
9. @Target(ElementType.TYPE_PARAMETER) 作用于类型泛型，即泛型方法、泛型类、泛型接口（jdk 1.8 加入）
10. @Target(ElementType.TYPE_USE) 类型使用，可以用于标注任意类型除了 class（jdk 1.8 加入）

#### 4.1.3. @Documented

　　@Documented 的作用是能够将注解中的元素包含到 Javadoc 中去。

#### 4.1.4. @Inherited

　　一个被 @Ingerited 注解了的注解修饰了一个父类，如果它的子类没有被其他注解修饰，则它的子类也继承了父类的注解。 

#### 4.1.5. @Repeatable

　　@Repeatable 是 Java 1.8 才加进来的，所以算是一个新的特性。被这个元注解修饰的注解可以同时作用一个对象多次，但是每次作用注解又可以代表不同的含义。

### 4.2. 获注解解属性

　　有几个基本方法：

1. isAnnotationPresent()： 是否存在对应的公有 Annotation 对象。
2. getAnnotation()：获取公有 Annotation 对象。
3. getAnnotations()：获得所有公有 Annotation 对象数组。
4. getDeclaredAnnotation()：返回本元素指定的所有注解。
5. getDeclaredAnnotations()：返回本元素的所有注解，不包含父类继承而来的。

### 4.3. 反射注解的工作原理

　　AnnotationInvovationHandler 是 JAVA 中专门用来处理注解的 Handler。

　　整个反射注解的工作原理：

1. 首先，通过键值对的形式可以为注解属性赋值，香这样 @Hello（value = "hello"）。
2. 接着，用注解修饰某个元素，编译器将扫描每个类或者方法上的注解，会做一个基本的检查，比如这个注解是否允许作用在当前位置，最后将注解信息写入元素的属性表。
3. 然后，当进行反射的时候，虚拟机将所有生命周期在 RUNTIME 的注解取出来放到一个 map 中，并创建一个 AnnotationInvocationHandler 实例，把这个 map 传递给它。
4. 最后，虚拟机将采用 JDK 动态代理机制生成一个目标注解的代理类，并初始化号处理器。

　　这样，一个注解的实例就创建出来了，它的本质上就是一个代理类。

### 4.4. JDK 提供的注解

1. @Override：用来描述当前方法是一个重写的方法，在编译阶段对方法进行检查。
2. @Deprecated：它是用来描述当前方法是一个过时的方法。
3. @SuppressWarning：对程序中的警告去除。
4. @SafeVarargs：参数安全类型注解。目的是提醒开发者不要用参数做一些不安全的操作，它存在会组织编译器产生 unchecked 这样的警告。
5. @FunctionalInterface：函数式接口注解。

### 4.5. 注解的作用

　　注解是一系列元数据，它提供数据用来解释程序代码，但是注解并非是所解释的代码本身的一部分。注解对于代码的运行效果没有直接影响。

1. 提供信息给编译器：编译器可以利用注解来检查出错误或者警告信息，打印出日志。
2. 编译阶段时的处理：软件工具可以用来利用注解信息来自动生成代码、文档或其他相应的自动处理。
3. 运行时处理：某些注解可以在程序运行的时候接受代码的提取，自动做相应的操作。
4. 注解能够提供原数据。

　　处理提取和处理 Annotation 的嗲吗统称为 ART（Annotation Processing Tool）。

## 5. 文件 IO 操作



## 6. AtomInteger



## 7. 锁

### 7.1. 死锁？造成死锁的四个条件？解决死锁的方法？



## 8. ArrayList 和 LinkedList 的区别

1. ArrayList 是基于动态数组的数据结构，LinkedList 是基于链表的数据结构。
2. 对于随机访问 get 和 set，ArrayList 要优于 LinkedList，因为 LinkedList 要移动指针；对于新增和删除操作 add 和 remove，LinkedList 比较占优势，因为 ArrayList 要移动数据。
3. ArrayList 的空间浪费主要体现在在 List 列表的结尾预留一定的容量空间，而 LinkedList 的空间浪费体现在它的每一个元素都需要消耗相当的空间（指向前后的指针）。

## 9. List 和 Set 的区别

### 9.1. 区别

1. List 和 Set 是存储单列数据的集合，两个接口都是继承自 Collection。
2. List 中存储的数据是有顺序的，并且值允许重复；Set 中存储的数据是无顺序的，并且不允许重复，子类 TreeSet 可以按照默认顺序或者自定义排序。
3. 

### 9.2. Set 源码

　　Set 的有三个实现类：TreeSet、HashSet、LinkedHashSet。

1. HashSet  的底层是用 HashMap 来构建的。
2. LinkedHashSet 的底层是用 LinkedHashMap 构建的。
3. TreeSet 的底是用 TreeSet 来构建的。

　　所以，Set 的实体类主要就是以 Map 为基础，将 set 构建的对象放入 map 的 key 中，相对应的使用环境也和对应的 map 相同。

疑问：list：可存null？**set不行（编译不过）



## 10 . HashMap

### 10.1. HashMap 的源码



### 10.2. HashMap 不同版本代码的区别



### 10.3. HashMap 线程不安全的体现



### 10.4. HashMap 和 HashTable 的区别



### 10.5. 为什么有了 hashmap 还有有 hashtable。



### 10.6. ConcurrentHashmap 源码

#### 10.6.1. ConcurrentHashmap如何体现线程安全？

 



## 11. 在多线程的情况下，在JAVA 中如何保证一个方法只被一个对象调用？

1. synchronized
2. volitle
3. Lock/unlock
4. 信号量
5. reentrantlock/synchronized 区别？如何实现上锁









