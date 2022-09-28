# 面试宝典 - kotlin 面试题

## 1.*

https://blog.csdn.net/gongjdde/article/details/124001671 Kotlin相关面试题

一.请简述下什么是kotlin？它有什么特性？

二.Kotlin 中注解 @JvmOverloads 的作用？

三.Kotlin中的MutableList与List有什么区别？

四.kotlin实现单例的几种方式？

五. kotlin中关键字data的理解？相对于普通的类有哪些特点？

六.什么是委托属性？简单说一下应用场景？

七.kotlin中with、run、apply、let函数的区别？一般用于什么场景？

八.kotlin中Unit的应用以及和Java中void的区别？

九.Kotlin 中 infix 关键字的原理和使用场景？

十. Kotlin中的可见性修饰符有哪些？相比于 Java 有什么区别？

十一.你觉得Kotlin与Java混合开发时需要注意哪些问题？

十二.在Kotlin中，何为解构？该如何使用？

十三.在Kotlin中，什么是内联函数？有什么作用？

十四.谈谈kotlin中的构造方法？有哪些注意事项？

十五.谈谈Kotlin中的Sequence，为什么它处理集合操作更加高效？

十六.请谈谈Kotlin中的Coroutines，它与线程有什么区别？有哪些优点？

十七.Kotlin中该如何安全地处理可空类型？

十八.Kotlin中的?.然后后面调用方法如果为空的情况下是什么？

十九.说说 Kotlin中 的 Any 与Java中的 Object 有何异同？

二十.Kotlin中的数据类型有隐式转换吗？为什么？

二十一.Kotlin 中集合遍历有哪几种方式？

二十二.为什么协程比线程要轻量？

二十三.协程Flow是什么，有哪些应用场景？

## 2.*

https://blog.csdn.net/weixin_39927378/article/details/117843597 android kotlin面试题,Kotlin面试25题



## 3.

https://wenku.baidu.com/view/45f2c39a950590c69ec3d5bbfd0a79563d1ed457.html kotlin面试题看这一篇就行了

1. 什么是 kotlin

   kotlin 是一个运行在 JVM 上的静态类型的程序语言。它可以使用 Java 源码或者 LLVM 编辑器编译。

2. Kotlin 的开发者是谁？

   JetBrains。

3. 为什么要从 java 转换到 kotlin

   相对于 java，kotlin 语言更简单。能减少冗余以及一些 java 不支持的特性。

4. 使用 kotlin 三个最重要的优势

   1. 易学，语法与 java 相似

   2. 基于 jvm 的函数式语言，移除了很多样板代码。
   3. 让代码更易读，更易理解。 

5. 解释扩展函数的使用

   扩展函数有助于扩展类而不需要继承类。

6. 空安全在 kotlin 意味着什么？

   空安全特性让 kotlin 移除了实时出现的空指针异常的风险。区分空引用和非空引用也是可能的。

7. 为什么 kotlin 和 java 是可交互的

   因为它也使用 jvm 字节码。把它直接编译成字节码有助于实现更快的编译事件且对于 jvm 而言和 java 无差异。

8. kotlin 中是否有像 java 那样的三元操作符

   没有

9. kotlin 中如何声明变量

   var my_var:Int

10. kotlin 中有多少有效构造函数

    两类构造函数：1.主要构造函数，2.次要构造函数

11. Kotlin 支持哪几种编程类型

    1.面向过程，2.面向对象

12. Kotlin 提供 java.io.file 的扩展函数的名字

    1. bufferedReader()：用于读取一个文件到 BufferedREader
    2. readBytest()：用于读取文件内容到字节数组
    3. readText()：用于读取文件内容到单个字符串
    4. forEachLine()：用于一行一行的读取一个文件
    5. readLines()：用于读取文件中的行到 List 中

13. Kotlin 如何处理空指针异常？

    Elvis 操作符用于处理空指针异常

    ```kotlin
    val i:Int = if(b != null) b.length else -1
    ```

    可替换为：

    ```kotlin
    val i = b?.length ?:-1
    ```

14. Kotlin 有而 java 没有特性

    1. 空指针安全
    2. 操作符重载
    3. 携程
    4. Range 表达式
    5. 智能转换
    6. 伴随体

15. kotlin 中数据类的使用

    数据类型持有基础数据类型，不包含任何功能

16. 我们能把代码从 java 迁移到 kotlin 么？

    可以，IDEA 提供一个内置的工具来进行迁移。

17. kotlin 支持宏指令么？

    不支持。

18. kotlin 类的默认行为？

    kotlin 中所有的类默认为 final。

## 4.*

https://www.jianshu.com/p/732dd96adeb5

## 5.*

https://zhuanlan.zhihu.com/p/421505065

## 6.*

https://www.sohu.com/a/486635057_121142027

## 7.*

http://blog.itpub.net/69902581/viewspace-2668916/

