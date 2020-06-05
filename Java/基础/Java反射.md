# Java 反射

## 1. 定义

　　Java 中创建对象大概有这几种方式：

1. 使用 new 关键字：这是最常见的也是嘴贱的创建对象的方式。
2. 使用 Clone 的方法：无论何时调用一个对象的 clone 方法，JVM 就会创建一个新的对象，将前面的对象的内容全部拷贝进去。
3. 使用反序列化：当序列化和反序列化一个对象，JVM 会创建一个单独的对象。

　　上边是 Java 中常见的创建对象的三种方式，其实除了上面的三种还有反射。

　　Java 反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；这种动态获取信息以及动态调用对象放啊的功能称为 java 语言的反射机制。

　　反射就是把 Java 类中各个部分，映射成一个个的 Java 对象，拿到这些对象后可以做一些事情。

## 2. 用途

　　在日常得第三方应用开发过程中，经常会遇到某个类得某个成员变量、方法或是属性是私有的或是只对系统应用开放，这时候就可以利用 Java 的反射机制通过反射来获取所需的私有成员或是方法。当然，也不是所有的都适合反射。有的类会在最终返回结果的地方对应用的权限进行校验，对于没有权限的应用返回值是没有意义的缺省值，否则返回实际值起到保护用户的隐私目的，这样通过反射得到的结果与预期不符。

　　一般来说反射是用来做框架的，或者说可以做一些抽象度比较高的底层代码，反射在日常的开发中用到的不多，但是搞懂了反射以后，可以帮助理解框架的一些原理。反射是框架设计的灵魂。

## 3. 反射机制的相关类

　　与 Java 反射相关的类如下：

| 类名           | 用途                                               |
| -------------- | -------------------------------------------------- |
| Class 类       | 代表类的实体，在运行的 Java 应用程序中表示类和接口 |
| Field 类       | 代表类的成员变量（成员变量也称为类的属性）         |
| Method 类      | 代表类的方法                                       |
| Constructor 类 | 代表类的构造方法                                   |

### 3.1. Class 类

　　一般写的代码是存储在后缀名是 .java 的文件里的，但是被编译后，最终真正去执行的是编译后的 .class 文件。Java 是面向对象的语言，一切皆对象，所以 java 认为这些编译后的 class 文件，这种事物也是一种对象，它也给抽象成了一种类，这个类就是 Class。

　　Class 代表类的实体，在运行的 Java 应用程序中表示类和接口。在这个类中提供了很多有用的方法，这里对他们简单的分类介绍。

#### 3.1.1. 获得类相关的方法

| 方法                         | 用途                                                   |
| ---------------------------- | ------------------------------------------------------ |
| asSubClass(Class< U > clazz) | 把传递的类的对象转换成代表其子类的对象                 |
| Cast                         | 把对象转换成代表类或是接口的对象                       |
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

#### 3.1.2. 获得类中属性相关的方法

| 方法                          | 用途                   |
| ----------------------------- | ---------------------- |
| getField(String name)         | 获得某个公有的属性对象 |
| getFields()                   | 获得所有公有的属性对象 |
| getDeclaredField(String name) | 获得某个属性对象       |
| getDeclaredFields()           | 获得所有属性对象       |

#### 3.1.3. 获得类中注解相关的方法

| 方法                                              | 用途                                   |
| ------------------------------------------------- | -------------------------------------- |
| getAnnotation(Class < A > annotationClass)        | 返回该类中与参数类型匹配的公有注解对象 |
| getAnnotation()                                   | 返回该类所有的公有注解对象             |
| getDeclaredAnnotation(Class< A > annotationClass) | 返回该类中与参数类型匹配的所有注解对象 |
| getDeclaredAnnatations()                          | 返回该类所有的注解对象                 |

#### 3.1.4. 获得类中构造器相关的方法

| 方法                                                  | 用途                                   |
| ----------------------------------------------------- | -------------------------------------- |
| getConstructor< Class... < ? > parameterTypes>        | 获得该类中与参数类型匹配的公有构造方法 |
| getConstructors()                                     | 获得该类的所有公共构造方法             |
| getDeclaredConstructor(Class... < ? > parameterTypes) | 获得该类中与参数类型匹配的构造方法     |
| getDeclaredConstructors()                             | 获得该类所有的构造方法                 |

#### 3.1.5. 获得类中方法相关的方法

| 方法                                                        | 用途                   |
| ----------------------------------------------------------- | ---------------------- |
| getMethod(String name,Class...< ? > parameterTypes)         | 获得该类某个公有的方法 |
| getMethods()                                                | 获得该类所有公有的方法 |
| getDeclaredMethod(String name,Class...< ? > parameterTypes) | 获得该类某个方法       |
| getDeclaredMethods()                                        | 获得该类所有方法       |

#### 3.1.6. 类中其他重要的方法

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

### 3.2. Field 类

　　Field 代表类的成员变量（成员变量也称为类的属性）。

| 方法                         | 用途                       |
| ---------------------------- | -------------------------- |
| equals(Object object)        | 属性与 obj 相等则返回 true |
| get(Object obj)              | 获得 obj 中对应的属性值    |
| set(Object obj,Object value) | 设置 obj 中对应属性值      |

### 3.3. Method 类

　　Method 代表类的方法。

| 方法                              | 用途                                       |
| --------------------------------- | ------------------------------------------ |
| invoke(Object obj,Object... args) | 传递 object 对象及参数调用该对象对应的方法 |

### 3.4. Constructor 类

　　Constructor 代表类的构造方法。

| 方法                            | 用途                       |
| ------------------------------- | -------------------------- |
| newInstance(Object... initargs) | 根据传递的参数创建类的对象 |

## 4. 使用

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

### 4.1. jdk 1.4 和 jdk 1.5 处理 invoke 方法

　　jdk 1.4 和 jdk 1.5 处理 invoke 方法有区别：

```java
public Object invoke(Object obj,Object…args) // 1.5
public Object invoke(Object obj,Object[] args) // 1.4
```



## 5. 总结


## 6. 参考文章

1. [Java 高级特性-反射](https://www.jianshu.com/p/9be58ee20dee)

2. [Java 中反射机制介绍](https://blog.csdn.net/ju_362204801/article/details/90578678)

