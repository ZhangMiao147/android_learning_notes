# Java 注解

## 1. 注解的定义

　　Java 注解是从 Java5 开始添加到 Java 的。

　　Junit 会在运行时检查方法上是否存在此注解，如果存在，就通过反射来运行方法。

　　注解其实就是代码里的特殊标记，它可以用来替代配置文件，也就是说，传统方式通过配置文件告诉类如何运行，有了注解技术后，开发人员可以通过注解告诉类如何运行，可以减少项目的配置文件，使代码看起来更优雅。在 Java 技术里注解的典型应用是：可以通过反射技术去得到类里面的注解，以决定怎么去运行类。

　　注解可以提供便捷性、易于维护修改，但耦合度高。

　　日常开发中新建 Java 类，使用 class、interface 比较多，而注解和它们一样，也是一种类的类型，它是用的修饰符为 @interface。

## 2. 元注解

　　元注解顾名思义可以理解为注解的注解，或者说元注解是一种基本注解，它是作用在注解的定义上，方便使用注解实现想要的功能。

　　元注解一般用于指定某个注解生命周期以及作用目标等信息。

　　元注解分别有 @Retension、@Target、@Document、@Inherited 和 @Repeatable（JDK 1.8 加入）五种。

### 2.1. @Retention

　　Retention 英文意思有保留、保持的意思，它表示注解存在阶段是保留在源码（编译期）、字节码（类加载）或者运行期（JVM 中运行），即这个注解的存活时间。在 @Retention 注解中使用枚举 RententionPolicy 来表示注解保留时期。

　　它的取值如下：

1. @Retention(RetentionPolicy.SOURCE)，注解仅存在于源码中，在 class 字节码文件中不包含，也就是注解只在源码阶段保留，在编译器进行编码时它将被丢弃忽视。
2. @Retention(RetentionPolicy.CLASS)，默认的保留策略，注解会在 class 字节码文件中存在，但运行时无法获得，注解只被保留到编译进行的时候，它并不会被加载到 JVM 中 。
3. @Rentention(RetentionPolity.RUNTIME)，注解会在 class 字节码文件中存在，在运行时可以通过反射获得到。注解可以保留到程序运行的时候，它会被加载进入到 JVM 中，所以在程序运行时可以获取到它们。

　　如果是自定义注解，则通过前面分析，自定义注解如果只存着源码中或者字节码文件中就无法发挥作用，而在运行期间能获取到注解才能实现目的，所以自定义注解中肯定是使用 @Retention(RetentionPolicy.RUNNTIME)。

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTestAnnotation {

}
```

### 2.2. @Target

　　Target 的英文意思是目标，这也很容易理解，使用 @Target 元注解标识注解作用的范围，可以是类、方法、方法参数变量等，同样也是通过枚举类 ElementType 表达作用类型，指定了注解运用的地方。

　　Target 的取值：

1. @Target(ElementType.TYPE) 作用一个类型，比如接口、类、枚举、注解。
2. @Target(ElementType.FIELD) 作用属性字段、枚举的常量。
3. @Traget(ElementType.METHOD) 作用方法
4. @Traget(ElementType.PARAMETER) 作用方法参数
5. @Traget(ElementType.CONSTRUCTOR) 作用构造函数
6. @Target(ElementType.LOCAL_VARIABLE) 作用局部变量
7. @Target(ElementType.ANNOTATION_TYPE) 作用于注解（@Retention 注解中就使用该属性）
8. @Traget(ElementType.PACKAGE) 作用于包
9. @Target(ElementType.TYPE_PARAMETER) 作用于类型泛型，即泛型方法、泛型类、泛型接口（jdk 1.8 加入）
10. @Traget(ElementType.TYPE_USE) 类型使用，可以用于标注任意类型除了 class (jdk 1.8 加入)

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

　　Inherited 的英文意思是继承，但是这个继承和平时理解的继承大同小异，一个被 @Inherited 注解了的注解修饰了一个父类，如果它的子类没有被其他注解修饰，则它的子类也继承了父类的注解。

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

      // 获取 Son 的 class 对象
       Class<Son> sonClass = Son.class;
      // 获取 Son 类上的注解 MyTestAnnotation  可以执行成功
      MyTestAnnotation annotation = sonClass.getAnnotation(MyTestAnnotation.class);
   }
}
```

### 2.5. @Repeatable

　　Repeatable 的英文意思是可重复的。@Repeatable 是 Java 1.8 才加进来的，所以算是一个新的特性。顾名思义说明被这个元注解修饰的注解可以同时作用一个对象多次，但是每次作用注解又可以代表不同的含义。

```java
/**一个人喜欢玩游戏，他喜欢玩英雄联盟，绝地求生，极品飞车，尘埃 4 等，则需要定义一个人的注解，他属性代表喜欢玩游戏集合，一个游戏注解，游戏属性代表游戏名称*/
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

　　注解的属性其实和类中定义的变量有异曲同工之处，只是注解中的变量都是成员变量（属性），并且注解中是没有方法的，只有成员变量，变量名就是使用注解括号中对应的参数名，变量类型是注解括号中对应参数类型。而 @Repeateable 注解中的变量的类型则是对应 Annotation（接口）的泛型 Class。

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

　　注解的本质就是一个继承了 Annotation 接口的接口。

```java
/**Annotation接口源码*/
public interface Annotation {

    boolean equals(Object obj);

    int hashCode();

    Class<? extends Annotation> annotationType();
}
```

　　通过以上源码，知道注解本身就是 Annotation 接口的子接口，也就是说注解中其实是可以有属性和方法，但是接口中的属性都是 static final 的，对于注解来说没什么意义，而定义接口的方法就相当于注解的属性，也就对应了前面说的为什么注解只有属性成员变量，其实它就是接口的方法，这就是为什么成员变量会有括号，不同于接口可以在注解的括号中给成员变量赋值。

　　解析一个类或者方法的注解往往有两种形式，一种是编译器直接的扫描，一种是运行期反射。编译器的扫描指的是编译器在对 java 代码编译字节码的过程中会检测到某个类或者方法被一些注解修饰，这时它就会对于这些注解进行某些处理。

　　虚拟机规范定义了一系列和注解相关的属性表，也就是说，无论是字段、方法或是类本身，如果被注解修饰了，就可以被写进字节码文件。属性表有以下几种：

1. RuntimeVisibleAnnotations：运行时可见的注解
2. RuntimeInVisibleAnnotations：运行时不可见的注解
3. RuntimeVisibleParametrAnnotations：运行时可见的方法参数注解
4. RuntimeInVisibleParameterAnnotations：运行时不可见的方法参数注解
5. AnnotationDefault：注解类元素的默认值。

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
    String name() default "mao"; // 设置默认值
    int age() default 18; // 设置默认值
}

@MyTestAnnotation(name = "father",age = 50)
public class Father {
}
```

　　如果注解只有一个属性时：

```java
public @interface Check {
    String value();
}

// 与 @Check(value="hi") 是一样的
@Check("hi")
int a;

@Check(value="hi")
int a;
```

　　如果一个注解没有任何属性，括号可以省略：

```java
public @interface Perform {}

@Perform
public void testMethod(){}
```

### 3.4. 获取注解属性

　　如果获取注解属性，当然是反射了，主要有五个基本的方法。

```java
 /** 是否存在对应的公有 Annotation 对象 */
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
     return GenericDeclaration.super.isAnnotationPresent(annotationClass);
  }

 /** 获取公有 Annotation 对象 */
 public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
     Objects.requireNonNull(annotationClass);

     return (A) annotationData().annotations.get(annotationClass);
 }
 /** 获取所有公有 Annotation 对象数组 */   
 public Annotation[] getAnnotations() {
     return AnnotationParser.toArray(annotationData().annotations);
 }   
getDeclaredAnnotation：
 /** 返回本元素的指定的所有注解 */
 public native <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass);
 /** 返回本元素的所有注解，不包含父类继承而来的 */
 public native Annotation[] getDeclaredAnnotations();
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

　　需要注意的是，如果一个注解要在运行时被成功提取，那么 @Retention(RetentionPolicy.RUNTIME) 是必须的。

### 3.5. 反射注解的工作原理

　　AnnotationInvocationHandler 是 JAVA 中专门用来处理注解的 Handler。

```java
class AnnotationInvocationHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 6182022883658399397L;
    private final Class<? extends Annotation> type;
  	// 键是注解属性名称，值就是该属性当初被赋上的值
    private final Map<String, Object> memberValues;
    private transient volatile Method[] memberMethods = null;

    AnnotationInvocationHandler(Class<? extends Annotation> var1, Map<String, Object> var2) {
        Class[] var3 = var1.getInterfaces();
        if (var1.isAnnotation() && var3.length == 1 && var3[0] == Annotation.class) {
            this.type = var1;
            this.memberValues = var2;
        } else {
            throw new AnnotationFormatError("Attempt to create proxy for a non-annotation type.");
        }
    }
  	// 对于代理类中任何方法的调用都会被转到这里来
  	// var2 指向被调用的方法实例
    public Object invoke(Object var1, Method var2, Object[] var3) {
      	// var4 获取该方法的简明名称
        String var4 = var2.getName();
        Class[] var5 = var2.getParameterTypes();
        if (var4.equals("equals") && var5.length == 1 && var5[0] == Object.class) {
            return this.equalsImpl(var3[0]);
        } else if (var5.length != 0) {
            throw new AssertionError("Too many parameters for an annotation method");
        } else {
            byte var7 = -1;
          	// switch 判断当前的调用方法是谁
          	// 如果是 Annotation 中的四大方法，将 var7 赋上特定的值
            switch(var4.hashCode()) {
            case -1776922004:
                if (var4.equals("toString")) {
                    var7 = 0;
                }
                break;
            case 147696667:
                if (var4.equals("hashCode")) {
                    var7 = 1;
                }
                break;
            case 1444986633:
                if (var4.equals("annotationType")) {
                    var7 = 2;
                }
            }
						
          	// 如果当前调用的方法是 toString、equals、hashCode、annotationType 的话，AnnotationInvocationHandler 实例中已经与定义好了这些方法的实现，直接调用即可
            switch(var7) {
            case 0:
                return this.toStringImpl();
            case 1:
                return this.hashCodeImpl();
            case 2:
                return this.type;
            default:
               	// 说明当前的方法调用的是自定义注解字节声明的方法
                // 这种情况下，将从注解 map 中获取这个注解属性对应的值
                Object var6 = this.memberValues.get(var4);
                if (var6 == null) {
                    throw new IncompleteAnnotationException(this.type, var4);
                } else if (var6 instanceof ExceptionProxy) {
                    throw ((ExceptionProxy)var6).generateException();
                } else {
                    if (var6.getClass().isArray() && Array.getLength(var6) != 0) {
                        var6 = this.cloneArray(var6);
                    }

                    return var6;
                }
            }
        }
    }
```

　　总结整个反射注解的工作原理：

1. 首先，通过键值对的形式可以为注解属性赋值，像这样：@Hello（value = "hello"）
2. 接着，用注解修饰某个元素，编译器将扫描每个类或者方法上的注解，会做一个基本的检查，比如这个注解是否允许作用在当前位置，最后会将注解信息写入元素的属性表。
3. 然后，当进行反射的时候，虚拟机将所有生命周期在 RUNTIME 的注解取出来放到一个 map 中，并创建一个 AnnotationInvocationHandler 实例，把这个 map 传递给它。
4. 最后，虚拟机将采用 JDK 动态代理机制生成一个目标注解的代理类，并初始化好处理器。

　　这样，一个注解的实例就创建出来了，它本质上就是一个代理类。

## 4. JDK 提供的注解

| 注解                 | 作用                                                         | 注意事项                                                     |
| -------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| @Override            | 它是用来描述当前方法是一个重写的方法，在编译阶段对方法进行检查。 | jdk 1.5 中它只能描述继承中的重写，jdk 1.6 中它可以描述接口实现的重写，也能描述类的继承的重写 |
| @Deprecated          | 它是用来描述当前方法是一个过时的方法。                       | 无                                                           |
| @SuppressWarning     | 对程序中的警告去除。                                         | 无                                                           |
| @SafeVarargs         | 参数安全类型注解。目的是提醒开发者不要用参数做一些不安全的操作，它的存在会组织编译器产生 unchecked 这样的警告。它是在 Java 1.7 的版本中加入的。 |                                                              |
| @FunctionalInterface | 函数式接口注解，这个是 Java 1.8 版本引入的新特性。函数式编程很火，所以 Java 8 也即是添加了这个特性。函数式接口（Function Interface）就是一个具有一个方法的普通接口。 |                                                              |

## 5. 注解作用与应用

　　Java 注解用于为 Java 代码提供元数据。作为元数据，注解不直接影响代码执行，但也有一些类型的注解实际上可以用于这一目的。

　　它存活的时间、作用的区域都可以由用户方便设置。

### 5.1. 使用注解进行参数配置

　　以银行转账为例，假设银行有个转账业务，转账的限额可能会根据汇率的变化而变化，可以利用注解灵活配置转账的限额，而不用每次都去修改业务代码。

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

　　官方文档：注解是一系列元数据，它提供数据用来解释程序代码，但是注解并非是所解释的代码本身的一部分。注解对于代码的运行效果没有直接影响。

1. 提供信息给编译器：编译器可以利用注解来检查出错误或者警告信息，打印出日志。
2. 编译阶段时的处理：软件工具可以用来利用注解信息来自动生成代码、文档或做其他相应的自动处理。
3. 运行时处理：某些注解可以在程序运行的时候接受代码的提取，自动做相应的操作。
4. 注解能够提供原数据，转账例子中处理获取注解值的过程是开发者直接写的注解提取逻辑，处理提取和处理 Annotation 的代码统称为 APT（Annotation Processing Tool）。

　　当开发者使用了 Annotation 修饰了类、方法、Field 等成员之后，这些 Annotation 不会自己生效，必须由开发者提供相应的代码来提取并处理 Annotation 信息。这些处理提取和处理 Annotation 的代码统称为 ART（Annotation Processing Tool）。上面转账例子中的 processAnnotationMoney 方法就可以理解为 APT 工具类。

## 6. 自定义注解

　　下面是一个简单的自定义注解。

　　自定义注解类：

```java
@Retention(RetentionPolicy.RUNTIME) // 注解存在于运行时
@Target(ElementType.METHOD) // 说明 MyTest 注解只能用在方法上
public @interface MyTest {
}

```

　　使用自定义注解：

```java
public class DemoTest{

    @MyTest
    public void test1(){
        System.out.println("test1 执行了");
    }

    public void test2(){
        System.out.println("test2 执行了");
    }
}
```

　　测试：

```java
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        // 取得类的字节码
        Class clazz = DemoTest.class;
        // 反射其中的成员，此处就是方法成员
        Method methods[] = clazz.getMethods(); // 得到 DemoTest1 中的所有
        // 看哪个方法有 MyTest 注解
        for (Method m : methods) {
            boolean b = m.isAnnotationPresent(MyTest.class);
            System.out.println(b + "===" + m.getName());
            if (b){
                m.invoke(clazz.newInstance(),null);
            }
        }
    }
```

　　输出结果：

```java
true===test1
test1 执行了
false===test2
false===wait
false===wait
false===wait
false===equals
false===toString
false===hashCode
false===getClass
false===notify
false===notifyAll
```


## 7. 参考文章

1. [Java 注解完全解析](https://www.jianshu.com/p/9471d6bcf4cf)
3. [Java 注解的基本原理](https://www.cnblogs.com/yangming1996/p/9295168.html)
4. [Java中的注解以及自定义注解](https://blog.csdn.net/ju_362204801/article/details/90697479)

