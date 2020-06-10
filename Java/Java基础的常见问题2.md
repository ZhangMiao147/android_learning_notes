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





#### 4.1.3. @Documented



#### 4.1.4. @Inherited



#### 4.1.5. @Repeatable









## 5. 文件 IO 操作



## 6. AtomInteger



## 7. 锁

### 7.1. 死锁？造成死锁的四个条件？



## 8. ArrayList 和 LinkedList 的区别

数组查改方便，增删不方便，链表查改不方便，增删方便

扩容机制不同：数组是扩大两倍，链表是根据数量增加减少。

## 9. List 和 Set 的区别

list：集合，可重读，可存null

set：几个，不可重复，set不行（编译不过）

list 如何去重 -》 用 Set。

Set 源码？



## 10. HashMap 和 HashTable 的区别

HashMap 的源码，不同版本（1.7 和 1.8 ）代码区别？线程不安全的体现？

为什么有了 hashmap 还有有 hashtable。

Concurrenthashmap 源码，线程安全，如何体现线程安全？

 











