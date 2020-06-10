# 在 Java 反射中 Class.forName 和 ClassLoader 的区别

## 1. Java 类加载过程

![](image/类加载过程.jpg)

1. 装载：通过类的全限定名获取二进制字节流，将二进制字节流转换成方法区中的运行时数据结构，在内存中生成 Java.lang.class 对象。

2. 链接：执行下面的校验、准备和解析步骤，其中解析步骤是可以选择的。

* 校验：检查导入类或接口的二进制数据的正确性（文件格式验证、元数据验证、字节码验证、符号引用验证）。
* 准备：给类的静态变量分配并初始化存储空间；
* 解析：将常量池中的符号引用转成直接引用。

3. 初始化：激活类的静态变量的初始化 Java 代码和静态 Java 代码块，并初始化程序员设置的变量值。

## 2. 分析 Class.forName() 和 ClassLoader

　　在 java 中 Class.forName() 和 ClassLoader.loadClass() 都可以对类进行加载。ClassLoader 就是遵循双亲委派模型最终调用启动类加载器的类加载器，实现的功能是 “ 通过一个类的全限定名来获取此类的二进制字节流 ”，获取到二进制流后放到 JVM 中。

　　Class.forName() 方法实际上也是调用的 ClassLoader 来实现的。

　　Class.forName( String className ) 这个方法的源码是：

```java
    @CallerSensitive
    public static Class<?> forName(String className)
                throws ClassNotFoundException {
        Class<?> caller = Reflection.getCallerClass();
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }
```

　　最后调用的方法是 forName0 这个方法，在这个 forName0 方法中的第二个参数被默认设置为了 true，这个参数代表是否会加载的类进行初始化，设置 true 时会对类进行初始化，代表会执行类中的静态代码块（static 块代码），以及对静态变量（static）的赋值等操作。

　　也可以调用 Class.forName(String name, boolean initialize, ClassLoader loader) 方法来手动选择在加载类的时候是否要对类进行初始化。Class.forName(String name, boolean initialize, ClassLoader loader) 的源码如下：

```java
    /*
    * initialize ：如果设置 true，则加载的类会被初始化
    */
	@CallerSensitive
    public static Class<?> forName(String name, boolean initialize,
                                   ClassLoader loader)
        throws ClassNotFoundException
    {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Reflective call to get caller class is only needed if a security manager
            // is present.  Avoid the overhead of making this call otherwise.
            caller = Reflection.getCallerClass();
            if (loader == null) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (ccl != null) {
                    sm.checkPermission(
                        SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }
```

　　ClassLoader.loadClass(className) 方法，内部实际调用的方法是 ClassLoader.loadClass(ClassName,false)；

```java
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }
```

　　第二个 boolean 参数，表示目标对象是否进行链接，false 表示不进行链接，不进行链接意味着不进行包括初始化等一系列操作，那么静态块和静态对象就不会得到执行。

## 3. 代码实践

　　一个含有静态代码块、静态变量、赋值给静态变量的静态方法的类。

```java
public class ClassForName {

    static {
        System.out.println("执行了静态代码块");
    }

    /**
     * 静态变量
     */
    private static String staticField = staticMethod();

    /**
     * 赋值静态变量的静态方法
     *
     * @return
     */
    public static String staticMethod() {
        System.out.println("执行了静态方法");
        return "给静态字段赋值了";
    }
}
```

　　使用 Class.forName() 方法测试：

```java
        try {
            System.out.println("使用forName");
            Class.forName("ClassForName");
            System.out.println("forName结束");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
```

　　输出结果是：

```java
使用forName
执行了静态代码块
执行了静态方法
forName结束
```

　　使用 ClassLoader 方法测试：

```java
        try {            ClassLoader.getSystemClassLoader().loadClass("ClassForName");
            System.out.println("ClassLoader.loadClass结束");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
```

　　输出结果是：

```java
使用ClassLoader.loadClass
ClassLoader.loadClass结束
```

　　根据运行结果得出 Class.forName 加载类时将类进行了初始化，而 ClassLoader 的 loadClass 并没有对类进行初始化，只是把类加载到了虚拟机中。

 ## 4. 应用场景

　　在 Spring 框架中的 IOC 的实现就是使用的 ClassLoader。

　　而在使用 JDBC 时通常是使用 Class.forName() 方法来加载数据库连接驱动。这时因为在 JDBC 规范中明确要求 Driver（数据库驱动）类必须向 DriverManager 注册自己。

　　以 MySQL 的驱动为例解释：

```java
public class Driver extends NonRegisteringDriver implements java.sql.Driver {  
    // ~ Static fields/initializers  
    // ---------------------------------------------  
  
    //  
    // Register ourselves with the DriverManager  
    //  
    static {  
        try {  
            java.sql.DriverManager.registerDriver(new Driver());  
        } catch (SQLException E) {  
            throw new RuntimeException("Can't register driver!");  
        }  
    }  
  
    // ~ Constructors  
    // -----------------------------------------------------------  
  
    /** 
     * Construct a new driver and register it with DriverManager 
     *  
     * @throws SQLException 
     *             if a database error occurs. 
     */  
    public Driver() throws SQLException {  
        // Required for Class.forName().newInstance()  
    }  
}
```

　　看到 Driver 注册到 DriverManager 中的操作写在了静态代码块中，这就是为什么在写 JDBC 时使用 Class.forName() 的原因了，Class.forName(className) 才能在反射回去类的时候执行 static 块。

## 5. 总结 Class.forName 和  ClassLoader 的区别

1. Class.forName 加载类时将类进行了初始化。
2. ClassLoader 的 loadClass 并没有对类进行初始化，只是把类加载到了虚拟机中。


## 6. 参考文章

1. [在Java的反射中，Class.forName和ClassLoader的区别](https://www.cnblogs.com/jimoer/p/9185662.html)

2. [反射中Class.forName()和ClassLoader.loadClass()的区别](https://www.cnblogs.com/zabulon/p/5826610.html)

   


