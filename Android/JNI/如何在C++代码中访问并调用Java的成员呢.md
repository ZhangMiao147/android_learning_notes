# 如何在 C++ 代码中访问并调用 Java 的成员呢？

**下面是我写的一个简单的本地函数**



```java
JNIEXPORT jstring JNICALL Java_c_example_com_jni_jnidemo_SayHello
  (JNIEnv *env, jclass obj){
//     return (*env)->NewStringUTF(env,"This just a test for Android Studio NDK JNI developer!");
return env->NewStringUTF("HelloWorld from JNI !");
  }
```

   **JNIEnv**类型代表Java环境。通过这个JNIEnv*指针，就可以对Java端的代码进行操作。如，创建Java类的对象，调用Java对象的方法，获取Java对象的属性等。
   JNIEnv的指针会被JNI传送到本地方法的实现函数中来对Java端的代码进行操作。



***\*1.获取jclass\****



**为了能够在C/C++使用Java类，jni.h头文件中专门定义了jclass类型来表示Java中的Class类**

  jclass的取得：

  JNIEnv类中有如下几个简单的函数可以取得jclass

  jclass FindClass(const char* clsName)  根据类名来查找一个类，完整类名。

  jclass GetObjectClass(jobject obj)  根据一个对象，获取该对象的类

  jclass GetSuperClass(jclass obj)   获取一个类的父类  

  FindClass 会在classpath系统环境变量下寻找类，需要传入完整的类名，注意包与包之间是用"/"而不是"."来分割

如：jclass cls_string= env->FindClass("java/lang/String");

 

获取jclass又什么用，比如你要调用类的静态方法，静态属性就需要通过这个方法来获取一个类。

**
**





**2.本地代码访问Java类中的属性与方法** 

有了类和对象之后，如何才能访问java中的对象的属性和方法呢，这就需要用到以下这些方法了。

 JNI在jni.h头文件中定义了 jfieldID、jmethodID 类表示 Java 端的属性和方法

如何获取属性： 在访问或设置 Java 属性的时候，首先就要现在本地代码中取得代表Java属性的jfieldID，然后才能在本地代码中进行Java属性操作。

如何调用java的方法：调用Java端的方法时，需要取得代表方法的jmethodID才能进行Java方法调用

 

JNIEnv获取相应的fieldID和jmethodID的方法：

  GetFieldID/GetMethodID

  GetStaticFieldID/GetStaticMethodID

  GetMethodID也可以取得构造函数的jmethodID。创建Java对象时调用指定的构造函数。

  如：env->GetMethodID(data_Clazz,"method_name","()V")

  (*jniEnv)->GetMethodID(jniEnv, Clazz,"< init >", "()V"); 

  这个比较特殊，这个是默认构造函数的方法，一般用这个来初始化对象，但是再实际过程中，为了快速生成一个实例，一般通过工厂方法类创建jobject

  

  jni.h 对GetMethodID的定义：

  jmethodID (JNICALL *GetMethodID)

   (JNIEnv *env, jclass clazz, const char *name, const char *sig);

***\*3.sign签名\****





对于 jmethodID GetMethodID(jclass clazz, const char *name, const char *sign)

  clazz代表该属性所在的类，name表示方法名称，sign是签名

  那什么是签名，**签名是对函数参数和返回值的描述**，对同一个函数，在java中允许重载，这个时候就需要这个sign来进行区分了。

  以下是java类型签名的描述

  

**用来表示要取得的属性/方法的类型**  



![复制代码](http://common.cnblogs.com/images/copycode.gif)

类型      相应的签名 
boolean    Z 
byte      B 
char      C 
short     S 
int      I 
long      J 
float     F 
double     D 
void      V 
object     L用/分隔包的完整类名：  Ljava/lang/String; 
Array      [签名     [I   [Ljava/lang/Object; 
Method     (参数1类型签名 参数2类型签名···)返回值类型签名  

![复制代码](http://common.cnblogs.com/images/copycode.gif)

**特别注意：Object后面一定有分号（；）结束的,多个对象参数中间也用分号(;)来分隔** 

例子：

方法签名

void f1()             ()V
int f2(int, long)         (IJ)I
boolean f3(int[])         ([I)B
double f4(String, int)       (Ljava/lang/String;I)D
void f5(int, String [], char)  (I[Ljava/lang/String;C)V

 

 



 图解签名：

![img](http://images.cnblogs.com/cnblogs_com/likwo/jnisign.jpg) 

**使用javap命令来产生签名**

   javap -s -p [full class Name]

   -s 表示输出签名信息

   -p 同-private,输出包括private访问权限的成员信息

  

 例子：

![复制代码](http://common.cnblogs.com/images/copycode.gif)

 C:\E\java\workspaces\myeclipseblue\JNITest\bin>javap -s -private video1.TestNative 
Compiled from "TestNative.java" 
public class video1.TestNative extends java.lang.Object{ 
public java.lang.String name; 
 Signature: Ljava/lang/String; 
public video1.TestNative(); 
 Signature: ()V 
public int signTest(int, java.util.Date, int[]); 
 Signature: (ILjava/util/Date;[I)I 
public native void sayHello(); 
 Signature: ()V 
public static void main(java.lang.String[]); 
 Signature: ([Ljava/lang/String;)V 
}  

![复制代码](http://common.cnblogs.com/images/copycode.gif)

 **楼主在写案例的时候就遇到了一个当时认为很奇怪的问题。**

报错：04-07 15:27:20.436 27378-27378/c.example.com.jni W/dalvikvm: Pending exception is:
04-07 15:27:20.436 27378-27378/c.example.com.jni I/dalvikvm: java.lang.NoSuchMethodError: no method with name='<init>' signature='(Ljava/lang/String;)V' in class Lc/example/com/jni/Student;

现在找到出错原因了，楼主的Student的构造函数是



```
public Student(String name, int age)
```

**当时不理解****(Ljava/lang/String;)V的意思，现在已经改正为:(Ljava/lang/String;I)V**

**献上楼主代码：**

**cpp:**



```java
JNIEXPORT jobject JNICALL Java_c_example_com_jni_jnidemo_getStudentInfo



        (JNIEnv *env, jclass obj)



{



  //关于包描述符，这儿可以是 com/feixun/jni/Student 或者是 Lcom/feixun/jni/Student;



  //   这两种类型 都可以获得class引用



  jclass stucls = env->FindClass("c/example/com/jni/Student"); //或得Student类引用



 



  //获得得该类型的构造函数  函数名为 <init> 返回类型必须为 void 即 V



  jmethodID constrocMID = env->GetMethodID(stucls,"<init>","(Ljava/lang/String;I)V");



 



  jstring str = env->NewStringUTF("Student named Aly");



 



  jobject stu_ojb = env->NewObject(stucls,constrocMID,str,25);  //构造一个对象，调用该类的构造函数，并且传递参数



 



 



  return stu_ojb ;



}
```

java:



```java
    public static native Student getStudentInfo();
```

# 参考文章

[如何在C++代码中访问并调用Java的成员呢？](https://blog.csdn.net/lvwenbo0107/article/details/51087461?utm_source=blogxgwz0)

[【转】使用JNI进行混合编程：在C/C++中调用Java代码](https://www.cnblogs.com/black/p/5171798.html)