# Android 常见问题 8

# 1. ListView 源码分析

## 1.1. ListView 的观察者模式

ListView 的父类 AbsListView 有一个变量 AdapterDataSetObserver ，作为观察者，用来通知数据变换。

在 onAttachedToWindow() 方法中注册观察者，在 onDetachedFromWindow() 方法中解除观察者。

在调用了 notifyDataSetChanged() 方法后，会采用观察者模式通知 ListView 重新绘制界面。调用 AdapterDataSetObserver 的 onChanged() 方法，而 onChanged() 方法中调用了 requestLayout() 方法。而 reqyestLayout() 就是 View 的 requestLayout() 方法，调用 scheduleTraversals() 方法，依次调用 onMeasure()、onLayout()、onDraw() 方法。

## 1.2. ListView 的缓存机制

ListView 用两个列表来缓存正在显示的和解除绑定的 View，在复用时是先根据 position 获取正在显示的 view，如果没有再根据 itemId 从正在显示的 view 中获取 view，如果还没有，则根据之前显示的 position 从解除绑定的列表中获取，没有则直接拿解除绑定的最后一个，如果还没有，则调用 getView() 得到 view。

## 1.3. ListView 的优化

ListView 的优化分为两步，1 是 getView() 方法中参数 view 的复用，如果 view 为空则去创建新对象，如果不为空，则直接修改显示内容即可，2 是 view 的 findViewById() 的优化，采用一个 holder 类，对 view 包含的控件进行 findViewById() 后存储，将 holder 设置为 view 的 tag，减少每次的 findViewById 的操作。

# 2. RecyclerView 源码分析



# 3. Android事件分发机制，如何处理冲突； 

https://www.jianshu.com/p/d82f426ba8f7

dispatchToucEvent() -> 分发事件

onInterceptTouchEvent() -> 拦截事件

onTouchEvent() -> 消费事件

如何处理滑动冲突：

滑动冲突分为两种：方向一致和方向不一致。

方向不一致，可以根据滑动的 x 和 y 的距离来区分，比如说父控件是横向滑动，子控件是竖向滑动，那么滑动的 x 的距离大于滑动的 y 的距离，则拦截事件，父控件消费事件，反之则不拦截事件，事件交给子控件处理。

方向一致，需要根据需要来决定，比如说 ScrollView 里面嵌套了 ListView，处理的方法就是当 ListView 滑动到了顶部，并且继续向上滑，则 ScrollView 拦截事件，当 ListView 滑动到了底部，并且继续向下滑，则 ScrollView 拦截事件，其他情况不拦截事件。

# 4. webview有哪些问题？ 

https://www.jianshu.com/p/28e9d5fc05a4 安全方面的坑

* webview 隐藏接口问题（任意命令执行漏洞）

  android webview 组件包含 3 个隐藏的系统接口：searchBoxJavaBridge_、accessibilityTraversal 以及 accessibility，恶意程序可以通过反射机制利用它们实现远程代码执行；该问题在 Android 4.4 以下版本出现。

  于是，在 Android 3.0 到 4.4 之间的版本，通过移除这些隐藏接口，来解决该问题：

  ```java
      // 19  4.4  Build.VERSION.KITKAT
      // 11  3.0  Build.VERSION_CODES.HONEYCOMB
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB 
          && Build.VERSION.SDK_INT < 19 && webView != null) { 
          webView.removeJavascriptInterface("searchBoxJavaBridge_");
          webView.removeJavascriptInterface("accessibility");
          webView.removeJavascriptInterface("accessibilityTraversal");
      }
  ```

* addJavascriptInterface 任何命令执行漏洞

  在 webview 中使用 js 与 html 进行交互是一个不错的方式，但是，在 Android 4.2(16)及以下版本中，如果使用 addJavascriptInterface，则会存在被注入 js 接口的漏洞；在 4.2 之后，由于 Google 增加了 @JavascriptInterface，该漏洞得以解决。

  解决该问题，最彻底的方式是在 4.2 以下放弃使用 addJavascriptInterface，采用 onJsPrompt 或其他方法替换。或者使用一些方案来降低该漏洞导致的风险：如使用 https 并进行证书校验，如果是 http 则进行页面完整性校验，如上面所述移除隐藏接口等。

  ```java
      public boolean onJsPrompt(WebView view, String url, String message,String defaultValue, JsPromptResult result) {
          result.confirm(CGJSBridge.callJava(view, message));
          Toast.makeText(view.getContext(),"message="+message,Toast.LENGTH_LONG).show();
          return true;
      }
  ```

* 绕过证书校验漏洞

  webviewClient 中有 onReceivedError 方法，当出现证书校验错误时，可以在该方法中使用 handler.process() 来忽略证书校验继续加载网页，或者使用默认的 handler.cancel() 来终端加载。

  因为使用了 handler.proceed()，由此产生了该 “绕过证书校验漏洞”。

  如果确定所有页面都能满足证书校验，则不必要使用 handler.proceed()。

  ```java
      @SuppressLint("NewApi")
      @Override
      public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
          //handler.proceed();// 接受证书
          super.onReceivedSslError(view, handler, error);
      }
  ```

* allowFileAccess 导致的 File 域同源策略绕过漏洞

  如果 web view.getSetting().setAllowFileAccess(boolean) 设置为 true，则会面临该问题；该漏洞是通过 WebView 对 Javascript 的延时执行和 html 文件替换产生的。

  解决方法是禁止 WebView 页面打开本地文件，即

  ```java
      webview.getSettings().setAllowFileAccess(false);
  ```

  或者更直接的禁止使用 Javascript

  ```java
      webview.getSettings().setJavaScriptEnabled(false);
  ```

  

https://www.zhihu.com/question/31316646

1. WebViewClient.onPageFinished()。永远无法确定当 WebView 调用这个方法的时候，页面内容是否真的加载完毕了。当前正在加载的页面产生跳转的时候这个方法可能会被多次调用。当 WebView 需要加载各种各样的网页并且需要在页面加载完成时采取一些操作的话，可能 WebChromeClient.onProgressChanged() 比 WebViewClient.onPageFinished() 都要靠谱。

2. WebView 后台耗电问题。当程序调用了 WebView 加载页面，WebView 会自己开启一些线程，如果没有正确的将 WebView 销毁的话，这些残余的线程会一直在后台运行，由此导致你的应用程序耗电量居高不下。对此采用的处理方式简单粗暴（不建议），在 Activity.onDestory() 中直接调用 system.exit(0)，使得应用程序完全被移除虚拟机，这样就不会有任何问题了。

# 5. Bitmap图片优化； 



# 6. 使用 JNI 时，如何在 C++ 代码中访问到一个 java 对象 

https://blog.csdn.net/lvwenbo0107/article/details/51087461?utm_source=blogxgwz0

使用 JNIEnv 来实现。JNIEnv 类型代表 Java 环境。通过这个 JNIEnv* 指针，就可以对 Java 端的代码进行操作。如，创建 Java 类的对象，调用 Java 对象的方法，获取 Java 对象的属性等。

```c++
#include <jni.h>  
#include <string.h>  
#include <stdio.h>  
   
// 环境变量PATH在windows下和linux下的分割符定义  
#ifdef _WIN32  
#define PATH_SEPARATOR ';' 
#else 
#define PATH_SEPARATOR ':' 
#endif  
   
   
int main(void)  
{  
    JavaVMOption options[1];  
    JNIEnv *env;  
    JavaVM *jvm;  
    JavaVMInitArgs vm_args;  
       
    long status;  
    jclass cls;  
    jmethodID mid;  
    jfieldID fid;  
    jobject obj;  
    // "-Djava.class.path=." 时传入当前路径，作为 JVM 寻找 class 的用户自定义路径
    options[0].optionString = "-Djava.class.path=.";  
    memset(&vm_args, 0, sizeof(vm_args));  
    // vm_args.version 是 Java 的版本 
    vm_args.version = JNI_VERSION_1_4;  
    // 传入的 options 有多长
    vm_args.nOptions = 1; 
    // 把 JavaVMOption 传给 JavaVMInitArgs 里面去
    vm_args.options = options;  
       
    // 启动虚拟机  
    status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);  
       
    if (status != JNI_ERR)  
    {  
        // 先获得 class 对象  
        cls = (*env)->FindClass(env, "Sample2");  
        if (cls != 0)  
        {  
            // 获取方法ID, 通过方法名和签名, 调用静态方法  
            mid = (*env)->GetStaticMethodID(env, cls, "sayHello", "(Ljava/lang/String;)Ljava/lang/String;");  
            if (mid != 0)  
            {  
                const char* name = "World";  
                // 从 C 转换为 java 的字符，使用 newStringUTF 方法
                jstring arg = (*env)->NewStringUTF(env, name);  
                jstring result = (jstring)(*env)->CallStaticObjectMethod(env, cls, mid, arg);  
                // 从 java 转换为 C 的字符，使用 GetStringUTFChars
                const char* str = (*env)->GetStringUTFChars(env, result, 0);  
                printf("Result of sayHello: %s\n", str);  
                (*env)->ReleaseStringUTFChars(env, result, 0);  
            }  
               
            /*** 新建一个对象 ***/ 
            // 调用默认构造函数  
            //obj = (*env)->AllocObjdect(env, cls);   
               
            // 调用指定的构造函数, 构造函数的名字叫做<init>  
            mid = (*env)->GetMethodID(env, cls, "<init>", "()V");  
            obj = (*env)->NewObject(env, cls, mid);  
            if (obj == 0)  
            {  
                printf("Create object failed!\n");  
            }  
            /*** 新建一个对象 ***/ 
               
            // 获取属性ID, 通过属性名和签名  
            fid = (*env)->GetFieldID(env, cls, "name", "Ljava/lang/String;");  
            if (fid != 0)  
            {  
                const char* name = "icejoywoo";  
                jstring arg = (*env)->NewStringUTF(env, name);  
                (*env)->SetObjectField(env, obj, fid, arg); // 修改属性  
            }  
               
            // 调用成员方法  
            mid = (*env)->GetMethodID(env, cls, "sayHello", "()Ljava/lang/String;");  
            if (mid != 0)  
            {  
                jstring result = (jstring)(*env)->CallObjectMethod(env, obj, mid);  
                const char* str = (*env)->GetStringUTFChars(env, result, 0);  
                printf("Result of sayHello: %s\n", str);  
                (*env)->ReleaseStringUTFChars(env, result, 0);  
            }  
        }  
           
        (*jvm)->DestroyJavaVM(jvm);  
        return 0;  
    }  
    else 
    {  
        printf("JVM Created failed!\n");  
        return -1;  
    }  
} 
```



# 7.  ServiceManager、ActivityManager、packageManager

## ServiceManager

管理系统的 service，有：inputMethodService、ActivityManagerService 等，在 serviceManager 中有两个重要的方法，add_service、check_service。

系统的 service 通过 add_service 把自己的信息注册到 serviceManager 中，而当需要使用系统的 service 的时候，通过 check_service 检查该 service 是否存在。

## ActivityManager

ActivityManager 的功能是与系统中所有运行的 activity 交互提供了接口，主要的接口围绕着运行中的进程信息、任务信息、服务信息等。

## PackageManager

PackageManager，主要职责管理应用程序包，通过它，可以获取应用程序信息。



# 8. Binder原理

https://blog.csdn.net/augfun/article/details/82343249

首先Binder是Android系统进程间通信(IPC)方式之一。

Binder使用Client－Server通信方式。Binder框架定义了四个角色：Server,Client,ServiceManager以及Binder驱动。其中Server,Client,ServiceManager运行于用户空间，驱动运行于内核空间。Binder驱动程序提供设备文件/dev/binder与用户空间交互，Client、Server和Service Manager通过open和ioctl文件操作函数与Binder驱动程序进行通信。

# 9. FrameWork 层的核心类

Android Framework框架包含了3个主要部分：服务端、客户端、Linux驱动。

首先介绍一下服务端都有哪些重要的东西：

ActivityManagerService(Ams):负责管理所有应用程序中的Activity,它掌握所有Activity的情况，具有所有调度Activity生命周期的能力，简单来说，ActivityManagerService是管理和掌控所有的Activity.

WindowManagerService(Wms):控制窗口的显示、隐藏以及窗口的层序，简单来说，它就是管理窗口的，大多数和View有关系的都要和它打交道。

KeyQ类:它是Wms的一个内部类，一旦创建就会启动一个新线程，这个线程会不断地接收和读取用户的UI操作消息，并把这些消息放到消息队列QueueEvent中。

InputDispatcherThread类：该类也是一旦创建就会启动一个新线程，这个线程会不断地从上面的QueueEvent中取出用户的消息进行一定的过滤，再将这些消息发送给当前活动的客户端程序中。

下面介绍一下客户端比较重要的东西：

ActivityThread类：主线程类，即UI线程类，我们的程序入口就是从ActivityThread的main()函数入口的。它根据Ams的要求（通过IApplicationThread接口，Ams为Client,ActivityThread.ApplicationThread为Server）负责调度和执行activities、broadcasts和其他操作。

ViewRoot类：很重要的一个类，负责客户端与Wms的交互：内部类有W类，W类继承与Binder，所以他与ApplicationThread的角色差不多，只不过它对应的是Wms，当Wms想与客户端进行通信的时候，Wms就调用这个类。内部又有ViewRootHandler类继承于Handler，所以他能在W类接收到Wms的消息后把这个消息传送到UI线程中。同时界面绘制的发起点也是在这里面：performTraversals();

W类：ViewRoot的帮手，继承与Binder，是ViewRoot内部类。主要帮助ViewRoot实现把Wms的IPC（进程间通信）调用转换为本地的一个异步调用。

Activity类：这个类我们比较熟悉，APK运行的最小单位。

PhoneWindow类：继承自Window类，它里面会放一个DecorView，它提供了一组统一窗口操作的API。

Window类：提供一些通用的窗口操作API.

DecorView类：这是我们所能看到的View的所有，它继承自FrameLayout，我们写的布局view就是放在它这个里面。

ApplicationThread类：继承鱼Binder,当Ams想与客户端通信时（即调用客户端的方法时），Ams调用的就是这个类。

Instrumentation类：负责直接与Ams对话，比如当客户端想与Ams进行通信时（即调用Ams服务里的方法），都是它去实现单项调用Ams，所有想调用Ams的操作都集中到它这里，它负责单向调用Ams。

WindowManager：客户端如果想创建一个窗口得先告诉WindowManager一声，然后它再和WindowManagerService交流一下看看能不能创建，客户端不能直接和WMS交互。

Linux驱动：

```
      Linux驱动和Framework相关的主要是两个部分：画家SurfaceFlingger和快递员Binder。每一个窗口都对应一个画Surface，SF主要是把各个Surface显示到同一屏幕上。Binder是提供跨进程的消息传递。
```

Manager机制：

```
     服务端有很多各种各样的系统服务，当客户端每次想要调用这些服务事（IPC）如果每次都是想要哪一个服务就直接去调用哪一个服务的话，会显得比较乱而且拓展性较差，所以Android采用了这种Manager机制，即设置一个类似经理的东西，也就是Manager，它自身也是一个服务，并且它管理着所有其他的服务，也就是说，我们需要哪个服务都要先经过它，它负责为我们去调用这个服务，所以这样就只给我们暴露的一个经理这个服务，其他的服务被他屏蔽了，这和java的封装很像。

     最后总结一下Android Framework的三大核心功能：1、View.java:View工作原理，实现包括绘制view、处理触摸、按键事件等。2、ActivityManagerService.java:Ams 管理所有应用程序的Activity等。3、WindowManagerService.java:Wms 为所有应用程序分配窗口，并管理这些窗口。
```

# 10. IPC 机制

从进程角度来看IPC机制

![img](https://img-blog.csdn.net/20180903054414366?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2F1Z2Z1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

每个Android的进程，只能运行在自己进程所拥有的虚拟地址空间。对应一个4GB的虚拟地址空间，其中3GB是用户空间，1GB是内核空间，当然内核空间的大小是可以通过参数配置调整的。对于用户空间，不同进程之间彼此是不能共享的，而内核空间却是可共享的。Client进程向Server进程通信，恰恰是利用进程间可共享的内核内存空间来完成底层通信工作的，Client端与Server端进程往往采用ioctl等方法跟内核空间的驱动进行交互。