# 如何实现线程

## 1. 继承 Thread 类创建线程

　　使用继承 Thread 类创建线程时，受限需要创建一个类继承 Thread 类并覆写 Thread 类的 run() 方法，在 run() 方法中，要写线程要执行的任务。

　　具体具体说明，代码如下所示：

```java
class MyThread extends Thread{
	@Override
	public void run() {
		System.out.println("自己创建的线程");
	}
}
```

　　创建了继承于 Thread 类的子类 MyThread 类以及覆写了 Thread 类的 run() 方法后，就相当于有了线程的主体类，接下来需要产生线程类的实例化对象然后调用 run() 方法，但实际上只是嗲用了 run() 方法并不是启动一个线程，真正启动一个线程，需要调用的是 Thread 类的 start() 方法，而 start() 方法会自动调用 run() 方法，从而启动一个线程。

　　代码如下所示：

```java
class MyThread extends Thread{
	@Override
	public void run() {
		System.out.println("自己创建的线程");
	}
}
public class Genericity {
	public static void main(String[] args) {
		//实例化一个对象
		MyThread myThread=new MyThread();
		//调用Thread类的start()方法
		myThread.start();
		//在main方法中打印一条语句
		System.out.println("main方法");
	}
}
```

　　运行结果：

```java
main方法
自己创建的线程
```

　　首先说明一点：main 方法其实也是一个线程，是该进程的住线程。

　　在使用多线程技术时，代码的运行结果与代码调用的顺序无关，因为线程是一个子任务，CPU 以不确定的方式或者说以随机的时间来调用线程中的 run() 方法，所以会出现先执行创建的线程，但是先打印语句 “main 方法”。

　　注意：一个对象多次调用 start() 方法时，会出现 Excepction in thread "main" java.lang.IllegalThreadStateException 异常，错误代码师范如下：

```java
class MyThread extends Thread{
	@Override
	public void run() {
		System.out.println("自己创建的线程");
	}
}
public class Genericity {
	public static void main(String[] args) {
		MyThread myThread=new MyThread();
		//调用start()方法一次
		myThread.start();
		//调用start()方法两次
		myThread.start();
		System.out.println("main方法");
	}
}
```

　　此时就会出现异常：

```java
Exception in thread "main" 自己创建的线程
java.lang.IllegalThreadStateException
	at java.lang.Thread.start(Unknown Source)
	at Genericity.main(Genericity.java:13)
```

## 2. 实现 Runnbale 接口创建线程



## 参考文章

1. [Java之线程的创建](https://blog.csdn.net/tongxuexie/article/details/80142638)



