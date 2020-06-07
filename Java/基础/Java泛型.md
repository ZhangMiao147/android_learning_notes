# Java 泛型

## 1. 概述

　　Java 泛型（generic）是 JDK 5 中引入的一个新特性，泛型提供了编译时类型安全检测机制，该机制允许程序员在编译时检测非法的类型。使用泛型机制编写的程序代码要比那些杂乱地使用 Object 变量，然后再进行强制类型转换地代码具有更好的安全性和可读性。泛型对于集合类尤其有用，例如，ArrayList 就是一个无处不在的集合类。

　　泛型的本质是参数化类型，也就是所操作的数据类型被指定为一个参数。

　　参数化类型就是将类型由原来的具体的类型参数化，类似于方法中的变量参数，此时类型也定义成参数形式（可以称之为类型形参）。

　　在使用 / 调用时传入具体的类型（类型实参）。

　　泛型的本质是为了参数化类型，在不创建新的类型情况下，通过泛型指定的不同类型来控制形参具体限制的类型。

　　操作的数据类型被指定为一个参数，这种参数类型可以用在类、接口和方法中，分别被称为泛型类、泛型接口、泛型方法。

## 2. 特性

　　泛型只在编译阶段有效。

```java
List<String> stringArrayList = new ArrayList<String>();
List<Integer> integerArrayList = new ArrayList<Integer>();

Class classStringArrayList = stringArrayList.getClass();
Class classIntegerArrayList = integerArrayList.getClass();

if(classStringArrayList.equals(classIntegerArrayList)){
    Log.d("泛型测试","类型相同"); // 输出 泛型测试：类型相同
}
```

　　通过上面的例子可以说明，在编译之后程序会采取去泛型化的措施。也就是说 Java 中的泛型，只在编译阶段有效。在编译过程中，正确检验泛型结果后，会将泛型的相关信息擦除，并且在对象进入和离开方法的边界处添加类型检查和类型转换的方法。也就是说，泛型信息不会进入到运行时阶段。

　　对此总结成一句话：泛型类型在逻辑上看成是多个不同的类型，实际上都是相同的数据结构。

## 3. 泛型的使用

　　泛型有三种常用的使用方式：泛型类、泛型接口和泛型方法。

### 3.1. 泛型类

　　泛型类型用于类的定义中，被称为泛型类。通过泛型可以完成对一组类的操作对外开放相同的接口。最典型的就是各种容器类，如：List、Set、Map。

　　一个泛型类（generic class）就是具有一个或多个类型变量的类。

　　泛型类的最基本写法：

```java
class 类名称 <泛型标识：可以随便写任意标识号，标识指定的泛型的类型>{
  private 泛型标识 /*（成员变量类型）*/ var; 
  .....

  }
}
```

　　一个普通的泛型类：

```java
/*
 * 泛型类
 * Java 库中 E 表示集合的元素类型，K 和 V 分别表示表的关键字与值的类型
 * T（需要时还可以用临近的字母 U 和 S）表示 “ 任意类型 ”
 */
public class Pair<T> {
    
    private T name;
    private T price;

    public Pair() {
    }

    public Pair(T name, T price) {
        this.name = name;
        this.price = price;
    }

    public T getName() {
        return name;
    }

    public void setName(T name) {
        this.name = name;
    }

    public T getPrice() {
        return price;
    }

    public void setPrice(T price) {
        this.price = price;
    }
}
```

　　Pair 类引入了一个类型变量 T ，用尖括号（<>）括起来，并放在类名的后面。泛型类可以有多个类型变量。例如，可以定义 Pair 类，其中第一个域和第二个域使用不同的类型：

```java
public class Pair<T,U> { ... }
```

　　类方法中的类型变量指定方法的返回类型以及域和局部变量的类型。例如：

```java
private T first; //uses the type variable
```

　　用具体的类型替换类型变量就可以实例化泛型类型，例如：

```java
Pair<String>
```

　　可以将结果想象成带有构造器的普通类：

```java
Pair<String>()
Pair<String>(String,String)
String getName()
String getPrice()
void setName(String)
void setPrice(String)
```

　　定义的泛型类就一定要传入泛型类型实参吗？并不是的，在使用泛型的时候如果传入泛型实参，则会根据传入的泛型实参做相应的限制，此时泛型才会起到本应起到的限制作用。如果不传入泛型类型实参的话，在泛型类中使用泛型的方法或成员变量定义的类可以为任何的类型。

```java
Pair pair1 = new Pair(0,1);
Pair pair1 = new Pair(3.14,5.18);
Pair pair1 = new Pair("name","price");
Pair pair1 = new Pair(true,false);
```

　　泛型的类型参数只能是类类型，不能是简单类型。

　　不能对确切的泛型类型使用 instanceof 操作。如下面的操作是非法的，编译时会出错。

```java
if(ex_num instanceof Generic<Number>){ }
```

### 3.2. 泛型接口

　　泛型接口与泛型类的定义及使用基本相同。泛型接口常被用在各种类的生产器中。

```java
// 定义一个泛型接口
public interface Generator<T> {

    public T next();

}
```

　　当实现泛型接口的类，传入泛型实参时：

```java
// 传入泛型实参时：
// 定义一个生产器实现这个接口，虽然只创建了一个泛型接口 Generator< T >
// 但是可以为 T 传入无数个实参，形成无数种类型的 Generator 接口。
// 在实现类实现泛型接口时，如已将泛型类型传入实参类型，则所有使用泛型的地方都要替换成传入的实参类型
public class FruitGenerator implements Generator<String> {

    @Override
    public String next() {
        return "Fruit";
    }

}
```

　　当实现泛型接口的类，未传入泛型实参时：

```java
// 未传入泛型实参时，与泛型类的定义相同，在声明类的时候，需将泛型的声明也一起加到类中
public class FruitGenerator<T> implements Generator<T> {

    private T next;

    public FruitGenerator(T next) {
        this.next = next;
    }

    @Override
    public T next() {
        return next;
    }

    public static void main(String[] args){
        FruitGenerator<String> fruit = new FruitGenerator<>("Fruit");
        System.out.println(fruit.next);
    }

}
```

### 3.3. 泛型方法

　　泛型类，是在实例化类的时候指明泛型的具体类型；泛型方法，是在调用方法的时候指明泛型的具体类型。

```java
public class ArrayAlg {

    public static <T> T getMiddle(T... a) {
        return a[a.length / 2];
    }
    
    public static void main(String[] args){
        System.out.println(ArrayAlg.getMiddle(1,2,3,4,5));
    }
}
```

　　这个方法是在普通类中定义的，而不是在泛型类中定义的。然而，这是一个泛型方法，可以从尖括号和类型变量看出这一点。注意，类型变量放在修饰符（public static）的后面，返回类型的前面。

#### 3.3.1. 静态方法与泛型

　　静态方法有一种情况需要注意一下，那就是在类中的静态方法使用泛型：静态方法无法访问类上定义的泛型；如果静态方法操作的引用数据类型不确定的时候，必须要将泛型定义在方法上。即：如果静态方法要使用泛型的话，必须将静态方法也定义成泛型方法。

```java
public class StaticGenerator<T> {
    ....
    ....
    /**
     * 如果在类中定义使用泛型的静态方法，需要添加额外的泛型声明（将这个方法定义成泛型方法）
     * 即使静态方法要使用泛型类中已经声明过的泛型也不可以。
     * 如：public static void show(T t){..},此时编译器会提示错误信息：
          "StaticGenerator cannot be refrenced from static context"
     */
    public static <T> void show(T t){

    }
}
```

#### 3.3.2. 泛型方法总结

　　泛型方法能使方法独立于类而产生变化，以下是一个基本的指导原则：

1. 无论何时，如果能做到，就该尽量使用泛型方法。也就是说，如果使用泛型方法将整个类泛型化，那么就应该使用泛型方法。
2. 另外对于一个 static 的方法而言，无法访问泛型类型的参数，所以如果 static 方法要使用泛型能力，就必须使其成为泛型方法。

### 3.4. 泛型通配符

　　Integer 是 Number 的一个子类，那么 Generic< Number > 和 Generic< Integer > 是否可以看成具有父子关系的泛型类型呢？

```java
Generic<Integer> gInteger = new Generic<Integer>(123);
Generic<Number> gNumber = new Generic<Number>(456);

showKeyValue(gNumber);

public void showKeyValue1(Generic<Number> obj){
    Log.d("泛型测试","key value is " + obj.getKey());
}

// showKeyValue这个方法编译器会为我们报错：Generic<java.lang.Integer> 
// cannot be applied to Generic<java.lang.Number>
// showKeyValue(gInteger);
```

　　通过提示信息可以看到 Generic< Integer > 不能被看作为 Generic< Number > 的子类。由此可以看出：同一种泛型可以对应多个版本（因为参数类型是不确定的），不同版本的泛型类实例是不兼容的。

　　解决上面的问题，需要一个在逻辑上可以标识同时是 Generic< Integer > 和 Generic< Number > 父类的引用类型，而这个引用类型就是使用类型通配符来作用。

```java
public void showKeyValue1(Generic<?> obj){
    Log.d("泛型测试","key value is " + obj.getKey());
}
```

　　类型通配符一般是使用 ? 代替具体的类型实参，注意了，此处 “？” 是类型实参，而不是类型形参。此处的 ？ 和 Number、String、Integer 一样都是一种实际的类型，可以把 ？ 看成所有类型的父类，是一种真实的类型。

　　可以解决当具体类型不确定的时候，使用通配符，也就是 ?，当操作类型时，不需要使用类型的具体功能时，只使用 Object 类中的功能，那么可以用 ？ 通配符来表示未知类型。

### 3.5. 泛型上下边界

　　在使用泛型的时候，还可以为传入的泛型类型实参进行上下边界的限制，如：类型实参只准传入某种类型的父类或某种类型的子类。

　　为泛型添加上边界，即传入的类型实参必须是指定类型的子类型。

```java
Generic<String> generic1 = new Generic<String>("11111");
Generic<Integer> generic2 = new Generic<Integer>(2222);
Generic<Float> generic3 = new Generic<Float>(2.4f);
Generic<Double> generic4 = new Generic<Double>(2.56);

//这一行代码编译器会提示错误，因为String类型并不是Number类型的子类
//showKeyValue1(generic1);

showKeyValue1(generic2);
showKeyValue1(generic3);
showKeyValue1(generic4);

public void showKeyValue1(Generic<? extends Number> obj){
    Log.d("泛型测试","key value is " + obj.getKey());
}
```

　　泛型方法的例子：

```java
// 在泛型方法中添加上下边界限制的时候，必须在权限声明与返回值之间的 < T > 上添加上下边界，即在泛型声明的时候添加
// public < T > T showKeyName(Generic<T extends Number> container)，编译器会报错："Unexpected bound"
public <T extends Number> T showKeyName(Generic<T> container){
    System.out.println("container key :" + container.getKey());
    T test = container.getKey();
    return test;
}
```

　　泛型的上下界添加，必须与泛型的声明在一起。

### 3.6. 泛型数组

　　在 java 中是 “ 不能创建一个确切的泛型类型的数组 ” 的。但是使用通配符创建数组是可以的。

```java
List<String>[] ls = new ArrayList<String>[10];  // 不可以
List<?>[] ls = new ArrayList<?>[10];  // 可以
List<String>[] ls = new ArrayList[10]; //可以
```

　　举个例子：

```java
List<String>[] lsa = new List<String>[10]; // Not really allowed.    
Object o = lsa;    
Object[] oa = (Object[]) o;    
List<Integer> li = new ArrayList<Integer>();    
li.add(new Integer(3));    
oa[1] = li; // Unsound, but passes run time store check    
String s = lsa[1].get(0); // Run-time error: ClassCastException.
```

　　这种情况下，由于 JVM 泛型的擦除机制，在运行时 jvm 是不知道泛型信息的，所以可以给 oa[1] 赋上一个 ArrayList 而不会出现异常。

　　但是在取出数据的时候却要做一次类型转换，所以就会出现 ClassCastException。

　　如果可以进行泛型数组的声明，上面说的这样情况在编译器将不会出现任何的警告和错误，只有在运行时才会出错。而对泛型数组的声明进行限制，对于这样的情况，可以在编译器提示代码有类型安全问题，比没有任何提示要强得多。

　　下面采用通配符的方式是被允许的：数组的类型不可以是类型变量，除非是采用通配符的方式，最后取出数据是要做显示的类型转换的。

```java
List<?>[] lsa = new List<?>[10]; // OK, array of unbounded wildcard type.    
Object o = lsa;    
Object[] oa = (Object[]) o;    
List<Integer> li = new ArrayList<Integer>();    
li.add(new Integer(3));    
oa[1] = li; // Correct.    
Integer i = (Integer) lsa[1].get(0); // OK
```


## 4. 参考文章

1. [Java 泛型](https://www.jianshu.com/p/41a7a975502d)

2. [Java 泛型详解-绝对是对泛型方法讲解最详细的，没有之一](https://www.cnblogs.com/coprince/p/8603492.html)