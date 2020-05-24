# Java 基础的常见问题

## 1. 面向对象的特性

　　Java 面向对象的四大特性为：封装、继承、抽象、多态。

### 1.1. 封装

　　将类的某些信息隐藏在类内部，不允许外部程序直接访问，而是通过该类提供的方法来实现对隐藏信息的操作和访问。

　　也有包级别的封装，将具有共同特性的类放在一起。

**好处**：

1. 增加复用。
2. 单一职责，减少变动带来的风险。
3. 只能通过规定的方法访问数据。
4. 隐藏类的实例细节，增加安全性、方便修改和实现。

```java
public class Person {
  private String name;
  private int age;

  public Person(String name, int age) {
    this.name = name;
    this age = age;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return this.age;
  }

  public void setAge(int age) {
    this.age = age;
  }
}
```

### 1.2. 继承

　　子类就是父类。

　　好处：子嘞拥有父类的所有属性和方法（除 private 修饰的属性不能拥有）从而实现了代码的复用。

**继承的方式**：

1. 普通类继承普通类、继承抽象类、实现接口
2. 抽象类继承普通类、继承抽象类、实现接口
3. 接口继承接口

**继承的缺点**：

1. 子类依赖于父类，父类发生改变时子类必须改变。
2. 子类会继承一些父类无用的方法，当继承链很长时，子类的方法很臃肿。

```java
public class Student extends Person {
  privete long studentId;
  
  public Student(String name, int age, long studentId) {
    super(name, age);
    this.studentId = studentId;
  }
  
  public long getStudentId() {
    return this.studentId;
  }
  
  public void setStudentId(long studentId) {
    this.studentId = studentId;
  }
}
```

### 1.3. 抽象

　　将具体的物体的特性抽象出来，是对真实世界的一种描述，用于模拟实际事物的特征和行为。

　　抽象最好的体现就是类。

```java
public class Person {
  public String name;
  public int age;

  public Person(String name, int age) {
    this.name = name;
    this age = age;
  }

  public void eat() {}
}
```

### 1.4. 多态

　　不同子类的相同属性的值不同、相同方法的实现不同（通过方法的重写）。

　　好处：避免子嘞的发展被多态限制。

```java
public interface Cry {
  public void cry();
}

public abstract class Animal implements Cry {
  public void cry();
}

public class Cat extends Animal {
  public void cry() {
    System.out.println("喵喵喵");
  }
}

public class Dog extends Animal {
  public void cry() {
    System.out.println("汪汪汪");
  }
}
```

## 2. switch-case

### 2.1. switch 语句支持的数据类型

　　对于表达式 switch 支持的数据类型：

1. 在 Java 5 之前，switch 只能是 byte、short、char、int 类型，byte、short、char 类型可以在不损失精度的情况下向上转型为 int 类型。

2. 从 Java 5 开始，java 中引入了枚举类型（enum 类型）和 byte、short、char、int 的包装类。

   四个包装类的支持是因为 java 编译器在底层进行了拆箱操作；

   枚举类型的支持是因为枚举类有一个 ordinal 方法，该方法实际上是返回一个 int 类型的数值。

3. 从 Java 7 开始，switch 还可以是 String 类型。

   String 类中因为有一个 hashCode 方法，结果也是返回 int 类型。

　　所以得出的结论是，switch 在底层实现目前只支持整数数据。

## 3. String、StringBuffer、StringBuilder 

　　String、StringBuilder 与 StringBuffer 都是 Java 用来处理字符串的类，并且都是 final 类，不允许被继承。

### 3.1. 三者的区别

1. 运行速度：StringBuilder > StringBuffer > String。

   String 运行速度慢是因为 String 是字符串常量，即 String 对象一旦创建之后该对象不可更改。对 String 进行操作就是一个不断创建新的对象并将旧的对象回收的过程，这导致效率低，并且会创建大量的内存，所以执行速度很慢。

   而 StringBuilder 和 StringBuffer 的对象是变量，对变量进行操作就是直接对该对象进行更改，不需要创建和回收操作，所以比 String 快很多。

   而 StringBuffer 是需要同步的，所以比 StringBuilder 要慢。

2. StringBuffer 是线程安全的，StringBuilder 是线程不安全的。

3. StringBuffer 与 StringBuilder 实现了 Serializable 和 CharSequare 两个接口，String 除了这两个接口，还实现了 Comparable< String > 接口，所以 String 的实例可以通过 compareTo 方法进行比较，而 StringBuffer 与 StringBuilder 不行。

4. String 可以空赋值，而 StringBuffer 和 StringBuilder 是不可以的。

### 3.2. “+” 与 append 的区别

```java
// 相当于 String S1 = “abc”; 运行最快
// 除了结果 S1，没有创建任何新 String
String S1 = “a” + “b” + “c”; 

// 运行最慢
String S1 = “a”; 
      String S2 =  “b”; 
      String S3 =  “c”; 
      String S1=S1+S2+S3；

// 运行中间
StringBuilder Sb = new StringBuilder(“a”).append(“b”).append(“c”);
```

　　String 使用 “+” 来拼接字符串，“+” 的原理是：每遇到一个 “+” ，就创建一个 StringBuilder 对象，然后用 append() 方法，最后调用 StringBuilder 的 toString() 方法返回 String 字符串，在使用之后还需要释放资源，效率低下。而 StringBuilder 的 append 不需要创建新的对象，节省资源。

### 3.3. StringBuffer 是如何实现线程安全的

　　StringBuffer 的方法都加了 synchronzied 关键字。

### 3.4. String 的 concat 方法与 append 的区别

```java
public String concat(String str) {
    int otherLen = str.length();
    if (otherLen == 0) {
        return this;
    }
    int len = value.length;
    /*copyOf数组复制,copyOf()的第二个自变量指定要建立的新e69da5e887aa7a686964616f31333365646261数组长度，
    如果新数组的长度超过原数组的长度，则保留为默认值null或0*/
    char buf[] = Arrays.copyOf(value, len + otherLen);
    //将字符从此字符串复制到目标字符数组,len为数组中的起始偏移量
    str.getChars(buf, len);
    return new String(buf, true);
}
```

　　String 的 concat 使用 copyOf 和 getChars 方法来拼接数组的，然后创建新的 String 对象。而 StringBuffer 只会创建一块内存空间，使用 append 添加或 delete 删除其内容时，也是在这一块内存空间中并不会生成多余的空间。

　　所以 StringBuffer 速度是比较快的，而 String 每次生成对象都会对系统性能产生影响，特别当内村中无引用对象多了以后，JVM 的 GC 就会开始工作，对速度的影响一定是相当大的。

## 4. 异常

### 4.1. try-catch-finally

#### 4.1.1. 在 try 中 return 还会不会调用 finally

　　肯定会执行。finally{} 块的代码只有在 try{} 块中包含遇到 System.exit(0); 之类的导致 Java 虚拟机直接退出的语句才会不执行。

　　当程序执行 try{} 遇到 return 时，程序会先执行 return 语句，但并不会立即返回 -- 也就是把 return 语句要做的一切事情都准备好，也就是在将要返回，但未返回的时候，程序把执行流程转去执行 finally 块，当 finally 块执行完成后就直接返回刚才 return 语句已经准备好的结果。

```java
public class Test{
       publicstatic void main(String[] args){
              System.out.println(new Test().test());;
       }

       staticint test(){
              int x = 1;
              try{
                     return x;
              }finally{
                     System.out.println("finally块执行:" + ++x);
              }
       }
}
```

　　输出结果为：

```java
finally块执行:2

1
```

　　因为 Java 会把 return 语句先执行完，把所有需要处理的东西都先处理完成，需要返回的值也都准备好之后，但是还未返回之前，程序流程会转去执行 finally 块，但此时 finally 块中的对 x 变量的修改意见不会影响 return 要返回的值了。

```java
public class Test{
       publicstatic void main(String[] args){
              System.out.println(new Test().test());;
       }
       staticint test(){
              int x = 1;
              try{
                     return x++;
              }finally{
                     System.out.println("finally块执行:" + ++x);
                     return x;
              }
       }
}
```

　　输出结果：

```java
finally 块执行：3
3
```

　　程序在执行 return x++;时，程序会把 return 语句执行完成，知识等待返回，此时 x 的值已经是 2 了，但程序此时准备返回值依然是 1。接下来程序流程转去执行 finally 块，此时程序会再次对 x 自加，于是 x 变成了 3，而且由于 finally 块中也有 return x；语句，因此程序将会直接由这条语句返回了。

#### 4.1.2. throw 和 throws





### 4.2. error 和 exception

#### 4.2.1. Error 和 Exception 的父类子类



#### 4.2.2. 常见的 Exception



#### 4.2.3. 常见的 Error

#### 4.2.4. CheckedException、RuntimeException 的区别



## 5. Java 的基本数据类型



## 6. 进程与线程



### 6.1. 线程的状态有哪些？



### 6.2. 如何实现线程



### 6.3. 线程与进程的区别？



### 6.4. 进程间通信



### 6.5. 线程间通信



### 6.6. 线程的常用方法



### 6.7. 线程池是什么？



### 6.8. 线程池分为几类？





## 7. 对象的引用类型有哪些？



###





### 2. ArrayList 和 LinkedList 有什么区别？





### 3. 什么是 HashMap



### 4. 用过哪些 Map 类，都有什么区别？



### 5. JAVA 8 的 ConcurrentHashMap 为什么放弃了分段所，有什么问题吗？如果你来设计，你如何设计？



### 6. HashMap、ConcurrentHashMap 原理



### 7. 有没有有顺序的 Map 实现类，如果有，他们是怎么保证有序的。



### 8. 抽象类和接口的区别，类可以集成多个类吗？接口可以继承多个接口吗？类可以实现多个接口吗？



### 9. IO 模型有哪些，讲讲你理解的 nio，它和 bio、aio 的区别是啥？谈谈 reactor 模型。



### 10. 反射的原理，反射创建类实例的三种方式是什么？



### 11. 反射中，Class.forName 和 ClassLoader 区别。



### 12. Java 动态代理实现与原理详细分析。



### 13.描述动态代理的几种实现方式，分别说出相应的优缺点。



### 14. 动态代理与 cglib 实现的区别。



### 15. 为什么 CGlib 方式可以对接口实现代理。



### 16. final 的用途



### 17. 写出三种单例模式实现



### 18. 如何在父类中为子类自动完成所有的 hashcode 和 equals 实现？这么做有何优劣。



### 19. 请结合 OO 设计理念，谈谈访问修饰符 public 、private 、protected、default 在应用设计中的作用。



### 20. 深拷贝和浅拷贝的区别。



### 21. 数组和链表数据结构描述，各自的时间复杂度。





### 23. 在自己的代码中，如果创建一个 java.lang.String 类，这个类是否可以被类加载器加载？为什么？



### 24. 说一说你对 java.lang.Object 对象中 hashcode 和 equals 方法的理解。在什么场景下需要重新实现这两个方法。



### 25. 在 jdk 1.5 中，引入了泛型，泛型的存在是用来解决什么问题的。



### 26. 这样的 a.hashcode() 有什么用，与 a.equals(b) 有什么关系？



### 27. 有没有可能 2 个不想等的对象有相同的 hashcode。



### 28. Java 中的 HashSet 内部是如何工作的。



### 29. 什么是序列化，怎么序列化，为什么序列化，反序列化会遇到什么问题，如何解决？



### 30. Java 8 的新特性



### 31. 强引用、软引用、弱引用、幻想引用有什么区别？



### 32. java 运算符与（&） 、非（～）、或（|）、异或（^）



