# Java 反射

## 1. 定义

　　Java 中创建对象大概有这几种方式：

1. 使用 new 关键字：这是最常见的也是最简单的创建对象的方式。
2. 使用 Clone 的方法：无论何时调用一个对象的 clone 方法，JVM 就会创建一个新的对象，将前面的对象的内容全部拷贝进去。
3. 使用反序列化：当序列化和反序列化一个对象，JVM 会创建一个单独的对象。

　　上边是 Java 中常见的创建对象的三种方式，其实除了上面的三种还有反射。

　　Java 反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法，对于任意一个对象，都能够调用它的任意方法和属性，这种动态获取信息以及动态调用对象方法的功能称为 Java 语言的反射机制。

　　反射就是把 Java 类中各个部分，映射成一个个的 Java 对象，拿到这些对象后可以做一些事情。

## 2. 用途

　　在日常的第三方应用开发过程中，经常会遇到某个类的某个成员变量、方法或是属性是私有的或是只对系统应用开放，这时候就可以利用 Java 的反射机制通过反射来获取所需的私有成员或方法。当然，也不是所有的都适合反射。有的类会在最终返回结果的地方对应用的权限进行校验，对于没有权限的应用返回值是没有意义的缺省值，否则返回实际值，这种就起到保护用户的隐私目的，这样通过反射得到的结果与预期不符。

　　一般来说反射是用来做框架的，或者说可以做一些抽象度比较高的底层代码，反射在日常的开发中用到的不多，但是搞懂了反射以后，可以帮助理解框架的一些原理，反射是框架设计的灵魂。

## 3. 反射原理

　　要想通过反射获取一个类的信息，首先要获取该类对应的 Class 类实例，Class 类的实例代表了正在运行中的 Java 应用的类和接口。Class 类没有公共的构造方法，Class 类对象是在二进制字节流（一般是 .class 文件，也可通过网络或 zip 包等路径获取）被 JVM 加载时，通过调用类加载器的 defineClass() 方法来构建的。

　　《 深入理解 Java 虚拟机 》一文中介绍，类从被加载到虚拟机内存中开始，到卸载出内存位置，它的整个生命周期包括：加载、连接、初始化、使用、卸载。而 JVM 在加载阶段要完成的 3 件事情中正好有 Class 对象的生成：

1. 通过一个类的全限定名来获取定义此类的二进制字节流。
2. 将这个字节流所代表的静态存储结构转换为方法区的运行时数据结构。
3. 在内存中生成一个代表这个类的 java.lang.Class 对象，作为方法区这个类的各种数据的访问入口。

### 3.1. 创建类实例的三种方式

1. Book.class
2. Object#getClass()
3. Class.forName()

#### 3.1.1. 代码

```java
    public static void main(String[] args) {
        // Book 的实例对象如何表示
        Book book1 = new Book();
        //任何类都是 Class 的实例对象，这个实例对象有三种表示方式
        // 第一种表示方式 - 》 实际在告诉任何一个类都有一个隐含的静态成员变量 class
        Class class1 = Book.class;

        // 第二种表示方式，已经知道该类的对象通过 getClass 方法
        Class class2 = book1.getClass();

        /**
         * 类也是对象，是 Class 类的实例对象，这个对象称为该类的类类型
         */
        // 不管 class1 还是 class2 都代表了 Book 类的类类型，一个类只可能是 Class 类的一个实例对象
        System.out.println("class1 == class2:" + (class1 == class2)); // true

        // 第三种表达方式
        Class class3 = null;
        try {
            class3 = Class.forName("Book");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("class2 == class3:" + (class2 == class3)); // true

        // 通过类的类类型创建该类的对象实例
        // 创建 Book 的实例对象
        try {
            // 需要有无参数的构造方法
            Book book = (Book) class1.newInstance(); // 需要强转
            System.out.println("book:" + book);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
```

　　不管哪种方式获取 Book 的 Class 实例，这些都是代表了 Book 类的类类型，一个类只可能是 Class 类的一个实例对象，所以不管哪种方式获取的类实例都是同一个。意思就是说 class1、class2、class3 是同一个，是相等的。

## 4. 反射机制的相关类

　　与 Java 反射相关的类如下：

| 类名           | 用途                                               |
| -------------- | -------------------------------------------------- |
| Class 类       | 代表类的实体，在运行的 Java 应用程序中表示类和接口 |
| Field 类       | 代表类的成员变量（成员变量也称为类的属性）         |
| Method 类      | 代表类的方法                                       |
| Constructor 类 | 代表类的构造方法                                   |

### 4.1. Class 类

　　一般写的代码是存储在后缀名是 .java 的文件里的，但是被编译后，最终真正去执行的是编译后的 .class 文件。Java 是面向对象的语言，一切皆对象，所以 java 认为这些编译后的 class 文件，这种事物也是一种对象，它也给抽象成了一种类，这个类就是 Class。

　　Class 代表类的实体，在运行的 Java 应用程序中表示类和接口。在这个类中提供了很多有用的方法。

#### 4.1.1. 获得类相关的方法

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

#### 4.1.2. 获得类中属性相关的方法

| 方法                          | 用途                   |
| ----------------------------- | ---------------------- |
| getField(String name)         | 获得某个公有的属性对象 |
| getFields()                   | 获得所有公有的属性对象 |
| getDeclaredField(String name) | 获得某个属性对象       |
| getDeclaredFields()           | 获得所有属性对象       |

#### 4.1.3. 获得类中注解相关的方法

| 方法                                              | 用途                                   |
| ------------------------------------------------- | -------------------------------------- |
| getAnnotation(Class < A > annotationClass)        | 返回该类中与参数类型匹配的公有注解对象 |
| getAnnotations()                                  | 返回该类所有的公有注解对象             |
| getDeclaredAnnotation(Class< A > annotationClass) | 返回该类中与参数类型匹配的所有注解对象 |
| getDeclaredAnnatations()                          | 返回该类所有的注解对象                 |

#### 4.1.4. 获得类中构造器相关的方法

| 方法                                                  | 用途                                   |
| ----------------------------------------------------- | -------------------------------------- |
| getConstructor(Class... < ? > parameterTypes)         | 获得该类中与参数类型匹配的公有构造方法 |
| getConstructors()                                     | 获得该类的所有公共构造方法             |
| getDeclaredConstructor(Class... < ? > parameterTypes) | 获得该类中与参数类型匹配的构造方法     |
| getDeclaredConstructors()                             | 获得该类所有的构造方法                 |

#### 4.1.5. 获得类中方法相关的方法

| 方法                                                        | 用途                   |
| ----------------------------------------------------------- | ---------------------- |
| getMethod(String name,Class...< ? > parameterTypes)         | 获得该类某个公有的方法 |
| getMethods()                                                | 获得该类所有公有的方法 |
| getDeclaredMethod(String name,Class...< ? > parameterTypes) | 获得该类某个方法       |
| getDeclaredMethods()                                        | 获得该类所有方法       |

#### 4.1.6. 类中其他重要的方法

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

### 4.2. Field 类

　　Field 代表类的成员变量（成员变量也称为类的属性）。

| 方法                         | 用途                       |
| ---------------------------- | -------------------------- |
| equals(Object object)        | 属性与 obj 相等则返回 true |
| get(Object obj)              | 获得 obj 中对应的属性值    |
| set(Object obj,Object value) | 设置 obj 中对应属性值      |

### 4.3. Method 类

　　Method 代表类的方法。

| 方法                              | 用途                                                    |
| --------------------------------- | ------------------------------------------------------- |
| invoke(Object obj,Object... args) | 传递 object 对象及参数调用 object 对象对应的方法 method |

### 4.4. Constructor 类

　　Constructor 代表类的构造方法。

| 方法                            | 用途                       |
| ------------------------------- | -------------------------- |
| newInstance(Object... initargs) | 根据传递的参数创建类的对象 |

　　在阅读 Class 类文档时发现一个特点，以通过反射获得 Method 为例，一般会提供四种方法：getMethod(parameterTypes)、getMethods()、getDeclaredMethod(parameterTypes) 和 getDeclared,ethods()。getMethod(parameterTypes) 用来获取该类某个公有的方法，getMethods() 获得该类所有公有的方法，getDeclaredMethod(parameterTypes) 获得该类某个方法，getDeclaredMethods() 获得该类所有方法。带有 Declared 修饰的方法可以反射到私有的方法，没有 Declared 修饰的只能用来反射公有的方法。其他的 Annotation、Field、Constructor 也是如此。

## 5. 使用

### 5.1. 案例

　　先写一个 Book 类，用于反射：

```java
public class Book {
    /**
     * 私有属性
     */
    private final static String TAG = "Book";

    private String name;
    private String author;

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                '}';
    }

    /**
     * 公共无参构造函数
     */
    public Book() {
    }

    /**
     * 私有有参构造函数
     * @param name
     * @param author
     */
    private Book(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 私有方法
     * @param index
     * @return
     */
    private String declaredMethod(int index){
        String string = null;
        switch (index){
            case 0:
                string = "I am declaredMethod 1 !";
                break;
            case 1:
                string = "I am declaredMethod 2 !";
                break;
            default:
                string = "I am declaredMethod 1 !";
        }
        return string;
    }
}

```

　　写一个 ReflectClass 类来反射获取 Book 的方法和属性：

```java
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectClass {
    private static final String TAG = "ReflectClass";

    public static final void main(String[] args) {
        reflectNewInstance();
        reflectProvateConstructor();
        reflectPrivateField();
        reflectPrivateMethod();
    }


    /**
     * 创建对象
     */
    public static void reflectNewInstance() {
        try {
            // 获取 Book 类
            Class<?> classBook = Class.forName("Book");
            // 创建 Book 对象
            Object objectBook = classBook.newInstance();
            Book book = (Book) objectBook;
            book.setName("Android进阶之光");
            book.setAuthor("刘望舒");
            System.out.println("reflectNewInstance book：" + book.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射私有的构造方法
     */
    public static void reflectProvateConstructor() {
        try {
            // 获取 Book 类
            Class<?> classBppk = Class.forName("Book");
            // 获取参数为 String 和 String 的所有构造方法
            Constructor<?> declaredConstructorBook = classBppk.getDeclaredConstructor(String.class, String.class);
            // 私有方法需要设置一下暴力反射
            declaredConstructorBook.setAccessible(true);
            // 调用得到的构造方法
            Object object = declaredConstructorBook.newInstance("Android进阶之光", "刘望舒");
            Book book = (Book) object;
            System.out.println("reflectProvateConstructor book：" + book.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射私有属性
     */
    public static void reflectPrivateField() {
        try {
            Class<?> classBook = Class.forName("Book");
            Object objectBook = classBook.newInstance();
            // 获取 book 对象的 TAG 属性对象
            Field fieldTag = classBook.getDeclaredField("TAG");
            fieldTag.setAccessible(true);
            // 获取 book 中对应的 tag 属性值
            String tag = (String) fieldTag.get(objectBook);
            System.out.println("reflectPrivateField tag：" + tag);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    /**
     * 反射私有方法
     */
    public static void reflectPrivateMethod() {
        try {
            // 获取 Book 类
            Class<?> classBook = Class.forName("Book");
            // 获取 Book 类的 declaredMethod 方法，参数是 int
            Method methodBook = classBook.getDeclaredMethod("declaredMethod", int.class);
            methodBook.setAccessible(true);
            // 创建 book 对象
            Object objectBook = classBook.newInstance();
            // 调用 book 对象的 declaredMethod 方法，传入参数为 0
            String string = (String) methodBook.invoke(objectBook, 0);
            System.out.println("reflectPrivateMethod string：" + string);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

```

### 5.2. jdk 1.4 和 jdk 1.5 处理 invoke 方法

　　jdk 1.4 和 jdk 1.5 处理 invoke 方法有区别：

```java
public Object invoke(Object obj,Object…args) // 1.5
public Object invoke(Object obj,Object[] args) // 1.4
```

　　由于 JDK 1.4 和 JDK 1.5 对 invoke 方法的处理有区别，所以在反射类似于 main（String[] args）这种参数是数组的方法时需要特殊处理。

　　启动 Java 程序的 main 方法的参数是一个字符串数组，即 public static void main(String[] args)，通过反射方法时来调用这个 main 方法时，如何为 invoke 方法传递参数呢？按 jdk 1.5 的语法，整个数组是一个参数，而按 jdk 1.4 的语法，数组中的每个元素对应一个参数，当把一个字符串数组作为参数传递给 invoke 方法时，javac 到底会按照哪种语法进行处理呢？jdk 1.5 肯定要兼容 jdk 1.4 的语法，会按 jdk 1.4 的语法进行处理，即把数组打散成为若干个单独的参数。所以，在给 main 方法传递参数时，不能使用代码 mainMethod.invoke(null,new String[]{})，javac 只把它当作 jdk 1.4 的语法进行理解，而不把它当作 jdk 1.5 的语法解释，因此会出现参数个数不对的问题。

　　上述问题的解决方法：

1. mainMethod.invoke(null,new Object[ ] ( new String[]{"xxx"}));

   这种方式，由于传的是一个数组的参数，所以为了向下兼容 1.4 的语法，javac 遇到数组会给拆开成多个参数，但是由于这个 Object[] 数组里只有一个元素值，所以就算它拆也没关系。

2. mainMethod.invoke(null,(Object)new String[]{"xxx"};

   这种方式相当于传的参数是一个对象，而不是数组，所以就算是按照 1.4 的语法它也不会拆，所以问题搞定。

　　编译器会作特殊处理，编译时不把参数当作数组看待，也就不会数组打散成若干个参数了。

**总结**：

　　在反射方法时，如果方法的参数是一个数组，考虑到向下兼容问题，会按照 JDK 1.4 的语法来对待（JVM 会把传递的数组参数拆开，拆开就会报参数的个数不匹配的错误）。

　　解决方法：防止 JVM 拆开数组。

1. 方法一：把数组看作是一个 Object 对象。

2. 方法二：重新构建一个 Object 数组，那个参数数组作为唯一的元素存在。

## 6. 总结

　　在 ReflactClass 类中还提供了两种反射 PowerManager.shutdown() 的方法，在调用的时候会输出如下 log，提示没有相关权限。

```java
 W/System.err: java.lang.reflect.InvocationTargetException
 W/System.err:     at java.lang.reflect.Method.invoke(Native Method)
 W/System.err:     at .ReflectClass.shutDown(ReflectClass.java:104)
 W/System.err:     at .MainActivity$1.onClick(MainActivity.java:25)
 W/System.err:     at android.view.View.performClick(View.java:6259)
 W/System.err:     at android.view.View$PerformClick.run(View.java:24732)
 W/System.err:     at android.os.Handler.handleCallback(Handler.java:789)
 W/System.err:     at android.os.Handler.dispatchMessage(Handler.java:98)
 W/System.err:     at android.os.Looper.loop(Looper.java:164)
 W/System.err:     at android.app.ActivityThread.main(ActivityThread.java:6592)
 W/System.err:     at java.lang.reflect.Method.invoke(Native Method)
 W/System.err:     at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
 W/System.err:     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:769)
 W/System.err: Caused by: java.lang.SecurityException: Neither user 10224 nor current process has android.permission.REBOOT.
 W/System.err:     at android.os.Parcel.readException(Parcel.java:1942)
 W/System.err:     at android.os.Parcel.readException(Parcel.java:1888)
 W/System.err:     at android.os.IPowerManager$Stub$Proxy.shutdown(IPowerManager.java:787)
 W/System.err:  ... 12 more
```

　　如果源码中明确进行了权限验证，而应用又无法获得这个权限的话，是无法成功反射的。

　　常用的框架里有很多地方都用到了反射，反射是框架的灵魂，具备反射知识和思想，是看懂框架的基础。

　　平时用到的框架，除了配置文件的形式，现在很多都使用了注解的形式，其实注解也和反射息息相关，使用反射也能轻而易举的拿到类、字段、方法上的注解，然后编写注解解析器对这些注解进行解析，做一些相关的处理，所以说不管是配置文件还是注解的形式，它们都和反射有关。


## 7. 参考文章

1. [Java 高级特性-反射](https://www.jianshu.com/p/9be58ee20dee)
2. [Java 中反射机制介绍](https://blog.csdn.net/ju_362204801/article/details/90578678)
3. [反射的原理，反射创建类实例的三种方式是什么？](https://blog.csdn.net/LianXu3344/article/details/82906201)
4. [Java反射的原理及反射创建类实例的三种方式](http://www.luyixian.cn/news_show_351132.aspx)

