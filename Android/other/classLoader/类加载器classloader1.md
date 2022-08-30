# 类加载器 ClassLoader ( 启动类加载器 | 扩展类加载器 | 应用类加载器 | 类加载的双亲委托机制 )

## 类加载器

Java 虚拟机 ClassLoader 类加载器：

* Bootstrap classLoader：启动类加载器，该加载器由 c/c++ 实现，加载 java 的核心类库，如 java.lang 包下的系统类；java 中无法获取。
* Extention classloader:扩展类加载器，加载 /lib/ext 下的类，java 中可以获取，其实现类为 ExtClassLoader；
* Application classloader:应用类加载器，加载开发者开发的类，java 中可以通过 classloader.getsystemclassloader 方法获取，其实现类为 appclassloader；

类加载器加载流程：bootstrap classloader 先加载系统的核心类库，extention classloader 加载额外的 /lib/ext 类库，application classloader 加载开发者自己开发的类库。

加载完开发者开发的类库后，程序才能开始执行。

## 类加载的双亲委托机制

类加载器级别：权限 / 重要性 从高到低排列。

* 启动类加载器：bootstrap classloader
* 扩展类加载器：extension classloader
* 应用类加载器：application classloader
* 自定义类加载器：custom classloader

在双亲委托机制中，上层的类加载器是下层类加载的父类。

**类加载的双亲委托机制**：

类加载器 classloader 接收到类加载任务之后，自己不会先进行加载，反而将该类加载任务委托给父类类加载器执行。

父类类加载器接收到该类加载任务之后，也会委托父类的父类类加载执行。

委托操作，会一直传递到最顶层的启动类加载器 bootstrap classloader;

* 如果启动类加载器 bootstrap classloader 完成了类加载操作，返回加载的类
* 如果启动类加载器 bootstrap classloader 无法完成类加载操作，就会将类加载任务委托给子类完成。

同理，父类委托给子类的类加载任务，如果子类类加载器可以完成加载，成功返回，如果子类类加载器无法完成加载，就在此将类加载任务委托给子类的子类，继续向下传递。

向上委托：每个儿子都不想加载类，类加载任务到来后，优先将任务委托父类去做，父类由活交给父类的父类去做；

向下委托：加入父亲无法加载类，则再交给儿子去做，儿子尝试加载，如果也无法加载，儿子再交给孙子去做。

双亲委派模式优点：

* 避免了类重复加载：如果某 class 类已经加载过了，通过该机制，可以直接读取出已经加载的类
* 安全性强：系统类无法被替代，系统类只能由启动类加载器 bootstrap classloader 加载，应用类加载器加载被篡改的 java 核心类是无效的。

## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( 启动类加载器 | 扩展类加载器 | 应用类加载器 | 类加载的双亲委托机制 )](https://hanshuliang.blog.csdn.net/article/details/121758271)

