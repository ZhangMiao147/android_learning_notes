# Java 注解

　　Java 注解用于为 Java 代码提供元数据。作为元数据，注解不直接影响你的代码执行，但也有一些类型的注解实际上可以用于这一目的。

　　Java 注解是从 Java5 开始添加到 Java 的。

## 1. 注解的定义

　　日常开发中新建 Java 类，使用 class、interface 比较多，而注解和它们一样，也是一种类的类型，它是用的修饰符为 @interface。

## 2. 元注解

　　元注解顾名思义可以理解为注解的注解，它是作用在注解中，方便使用注解实现想要的功能。元注解分别有 @Retension、@Target、@Document、@Inherited 和 @Repeatable（JDK 1.8 加入）五种。

### 2.1. @Retention

　　Retention 英文意思有保留、保持的意思，它表示注解存在阶段是保留在源码（编译期）、字节码（类加载）或者运行期（JVM 中运行）。在 @Retention 注解中使用枚举 RententionPolicy 来表示注解保留时期。

　　@Retention(RetentionPolicy.SOURCE)，注解仅存在于源码中，在 class 字节码文件中不包含。

 　　@Retention(RetentionPolicy.CLASS)，默认的保留策略，注解会在 class 字节码文件中存在，但运行时无法获得 。

　　@Rentention(RetentionPolity.RUNTIME)，注解会在 class 字节码文件中存在，在运行时可以通过反射获得到。

　　如果是自定义注解，则通过前面分析，自定义注解如果只存着源码中或者字节码文件中就无法发挥作用，而在运行期间能获取到注解次啊能实现目的，所以自定义注解中肯定是使用 @Retention(RetentionPolicy.RUNNTIME)。

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTestAnnotation {

}
```

### 2.2. @Target

　　Target 的英文意思是目标，这也很容易理解，使用 @Target 元注解标识注解作用的范围就比较具体了，可以是类、方法、方法参数变量等，同样也是通过枚举类 ElementType 表达作用类型。

　　@Target(ElementType.TYPE) 作用接口、类、枚举、注解。

　　@Target(ElementType.FIELD) 作用属性字段、枚举的常量。

　　@Traget(ElementType.METHOD) 作用方法

　　@Traget(ElementType.PARAMETER) 作用方法参数

　　@Traget(ElementType.CONSTRUCTOR) 作用构造函数

　　@Target(ElementType.LOCAL_VARIABLE) 作用局部变量

　　@Target(ElementType.ANNOTATION_TYPE) 作用于注解（@Retention 注解中就使用该属性）

　　@Traget(ElementType.PACKAGE) 作用于包

　　@Target(ElementType.TYPE_PARAMETER) 作用于类型泛型，即泛型方法、泛型类、泛型接口（jdk 1.8 加入）

　　@Traget(ElementType.TYPE_USE) 类型使用，可以用于标注任意类型除了 class (jdk 1.8 加入)

　　一般比较常用的是 ElementType.TYPE 类型。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTestAnnotation {

}
```

### 2.3. @Documented

　　Documented 的英文意思是文档。它的作用是能够将注解中的元素包含到 Javadoc 中去。

### 2.4. @Inherited

　　Inherited 的英文意思是继承，但是这个继承和平时理解的继承大同小异，一个被 @Inherited 注解了的注解修饰了一个父类，如果他的子类没有被其他注解修饰，则它的子类也继承了父类的注解。

```java
/**自定义注解*/
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTestAnnotation {
}
/**父类标注自定义注解*/
@MyTestAnnotation
public class Father {
}
/**子类*/
public class Son extends Father {
}
/**测试子类获取父类自定义注解*/
public class test {
   public static void main(String[] args){

      //获取Son的class对象
       Class<Son> sonClass = Son.class;
      // 获取Son类上的注解MyTestAnnotation可以执行成功
      MyTestAnnotation annotation = sonClass.getAnnotation(MyTestAnnotation.class);
   }
}
```

### 2.5. @Repeatable

　　Repeatable 的英文意思是可重复的。顾名思义说明被这个元注解修饰的注解可以同时作用一个对象多次，但是每次作用注解又可以代表不同的含义。

```java
/**一个人喜欢玩游戏，他喜欢玩英雄联盟，绝地求生，极品飞车，尘埃4等，则我们需要定义一个人的注解，他属性代表喜欢玩游戏集合，一个游戏注解，游戏属性代表游戏名称*/
/**玩家注解*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface People {
    Game[] value() ;
}
/**游戏注解*/
@Repeatable(People.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Game {
    String value() default "";
}
/**玩游戏类*/
@Game(value = "LOL")
@Game(value = "PUBG")
@Game(value = "NFS")
@Game(value = "Dirt4")
public class PlayGame {
}
```

## 3. 注解的属性

　　注解的属性其实和类中定义的变量有异曲同工之处，只是注解中的变量都是成员变量（属性），并且注解中是没有方法的，只有成员变量，变量名就是使用注解括号中对应的参数名，变量返回值注解括号中对应参数类型。而 @Repeateable 注解中的变量的类型则是对应 Annotation（接口）的泛型 Class。

```java
/**注解Repeatable源码*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Repeatable {
    /**
     * Indicates the <em>containing annotation type</em> for the
     * repeatable annotation type.
     * @return the containing annotation type
     */
    Class<? extends Annotation> value();
}
```



### 3.1. 注解的本质

　　注解的本质就是一个 Annotation 接口。

```java
/**Annotation接口源码*/
public interface Annotation {

    boolean equals(Object obj);

    int hashCode();

    Class<? extends Annotation> annotationType();
}
```

　　通过以上源码，知道注解本身就是 Annotation 接口的子接口，也就是说注解中其实是可以有属性和方法，但是接口中的属性都是 static final 的，对于注解来说没什么意义，而定义接口的方法就相当于注解的属性，也就对应了前面说的为什么注解只有属性成员变量，其实他就是接口的方法，这就是为什么成员变量会有括号，不同于接口可以在注解的括号中给成员变量赋值。

### 3.2. 注解属性类型

　　注解属性类型可以有以下列出的类型：

1. 基本数据类型
2. String
3. 枚举类型
4. 注解类型
5. Class 类型
6. 以上类型的一维数组类型

### 3.3. 注解成员变量赋值

　　如果注解有多个属性，则可以在注解括号中用 “，” 号隔开分别给对应的属性赋值，如下例子，注解在父类中赋值属性。

```java
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTestAnnotation {
    String name() default "mao";
    int age() default 18;
}

@MyTestAnnotation(name = "father",age = 50)
public class Father {
}
```

### 3.4. 获取注解属性

　　如果获取注解属性，当然是反射了，主要有三个基本的方法。

```java
 /**是否存在对应 Annotation 对象*/
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

 /**获取 Annotation 对象*/
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().annotations.get(annotationClass);
    }
 /**获取所有 Annotation 对象数组*/   
 public Annotation[] getAnnotations() {
        return AnnotationParser.toArray(annotationData().annotations);
    }    
```

　　举例，来获取以下注解属性，在获取之前自定义的注解必须使用元注解 @Rerention(RetentionPolicy.RUNTIME)

```java
public class test {
   public static void main(String[] args) throws NoSuchMethodException {

        /**
         * 获取类注解属性
         */
        Class<Father> fatherClass = Father.class;
        boolean annotationPresent = fatherClass.isAnnotationPresent(MyTestAnnotation.class);
        if(annotationPresent){
            MyTestAnnotation annotation = fatherClass.getAnnotation(MyTestAnnotation.class);
            System.out.println(annotation.name());
            System.out.println(annotation.age());
        }

        /**
         * 获取方法注解属性
         */
        try {
            Field age = fatherClass.getDeclaredField("age");
            boolean annotationPresent1 = age.isAnnotationPresent(Age.class);
            if(annotationPresent1){
                Age annotation = age.getAnnotation(Age.class);
                System.out.println(annotation.value());
            }

            Method play = PlayGame.class.getDeclaredMethod("play");
            if (play!=null){
                People annotation2 = play.getAnnotation(People.class);
                Game[] value = annotation2.value();
                for (Game game : value) {
                    System.out.println(game.value());
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
```

　　运行结果：

```
father
50
35
LOL
PUBG
NFS
Dirt4
```

## 4. JDK 提供的注解

| 注解             | 作用                                                         | 注意事项                                                     |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| @Override        | 它是用来描述当前方法是一个重写的方法，在编译阶段对方法进行检查 | jdk 1.5 中它只能描述继承中的重写，jdk 1.6 中它可以描述接口实现的重写，也能描述类的继承的重写 |
| @Deprecated      | 它是用来描述当前方法是一个过时的方法                         | 无                                                           |
| @SuppressWarning | 对程序中的警告去除。                                         | 无                                                           |



## 5. 注解作用与应用

　　Java 注解用于为 Java 代码提供元数据。作为元数据，注解不直接影响代码执行，但也有一些类型的注解实际上可以用于这一目的。

　　它存活的时间、作用的区域都可以由用户方便设置。

### 5.1. 使用注解进行参数配置

　　以银行转装为例，假设银行有个转账业务，转账的限额可能会根据汇率的变化而变化，可以利用注解灵活配置转账的限额，而不用每次都去修改业务代码。

```java
/**定义限额注解*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BankTransferMoney {
    double maxMoney() default 10000;
}
/**转账处理业务类*/
public class BankService {
    /**
     * @param money 转账金额
     */
    @BankTransferMoney(maxMoney = 15000)
    public static void TransferMoney(double money){
        System.out.println(processAnnotationMoney(money));

    }
    private static String processAnnotationMoney(double money) {
        try {
            Method transferMoney = BankService.class.getDeclaredMethod("TransferMoney",double.class);
            boolean annotationPresent = transferMoney.isAnnotationPresent(BankTransferMoney.class);
            if(annotationPresent){
                BankTransferMoney annotation = transferMoney.getAnnotation(BankTransferMoney.class);
                double l = annotation.maxMoney();
                if(money>l){
                   return "转账金额大于限额，转账失败";
                }else {
                    return"转账金额为:"+money+"，转账成功";
                }
            }
        } catch ( NoSuchMethodException e) {
            e.printStackTrace();
        }
        return "转账处理失败";
    }
    public static void main(String[] args){
        TransferMoney(10000);
    }
}
```

　　运行结果：

```
转账金额为：10000.0，转账成功
```

### 5.2. 第三方框架的应用

　　作为一个 Android 开发者，平常所使用的第三方框架 ButterKnife、Retrofit2、Dagger2 等都有注解的应用，如果要了解这些框架的原理，则注解的基础只是则是必不可少的。

### 5.3. 注解的作用

1. 提供信息给编译器：编译器可以利用注解来检查出错误或者警告信息，打印出日志。
2. 编译阶段时的处理：软件工具可以用来利用注解信息来自动生成代码、文档或做其他相应的自动处理。
3. 运行时处理：某些注解可以在程序运行的时候接受代码的提取，自动做相应的操作。
4. 注解能够提供原数据，转账例子中处理获取注解值得过程是开发者直接写的注解提取逻辑，处理提取和处理 Annotation 的代码统称为 APT（Annotation Processing Tool）。上面转账例子中的 processAnnotationMoney 方法就可以理解为 APT 工具类。






## 参考文章

1. [Java 注解完全解析](https://www.jianshu.com/p/9471d6bcf4cf)
2. [Java 注解 - 最通俗易懂的讲解](https://blog.csdn.net/qq1404510094/article/details/80577555)
3. [Java 注解的基本原理](https://www.cnblogs.com/yangming1996/p/9295168.html)
4. [Java中的注解以及自定义注解](https://blog.csdn.net/ju_362204801/article/details/90697479)

