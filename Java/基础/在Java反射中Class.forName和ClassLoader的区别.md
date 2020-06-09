# 在 Java 反射中 Class.forName 和 ClassLoader 的区别

　　在 java 中 Class.forName() 和 ClassLoader 都可以对类进行加载。ClassLoader 就是遵循双亲委派模型最终调用启动类加载器的类加载器，实现的功能是 “ 通过一个类的全限定名来获取此类的二进制字节流 ”，获取到二进制流后放到 JVM 中。

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

　　最后调用的方法是 forName0 这个方法，在这个 forName0 方法中的第二个参数被默认设置为了 true，这个参数代表是否会加载的类进行初始化，设置 true 时会对类进行初始化，代表会执行类中的静态代码块，以及对静态变量的赋值等操作。

　　也可以调用 Class.forName(String name, boolean initialize, ClassLoader loader) 方法来手动选择在加载类的时候是否要对类进行初始化。Class.forName(String name, boolean initialize, ClassLoader loader) 的源码如下：

```java
    /*
    * initialize ：如果额日 true，则加载的类会被初始化
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



 




## 6. 参考文章

1. [在Java的反射中，Class.forName和ClassLoader的区别](https://www.cnblogs.com/jimoer/p/9185662.html)

   


