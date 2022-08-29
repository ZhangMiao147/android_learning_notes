# AIDL 的工作原理

## IPC

IPC是**Inter-Process Communication**的缩写，是进程间通信或者跨进程通信的意思，既然说到进程，大家要区分一下进程和线程，**进程一般指的是一个执行单元，它拥有独立的地址空间，也就是一个应用或者一个程序。线程是CPU调度的最小单元，是进程中的一个执行部分或者说是执行体，两者之间是包含与被包含的关系**。因为进程间的资源不能共享的，所以每个系统都有自己的IPC机制，Android是基于Linux内核的移动操作系统，但它并没有继承Linux的IPC机制，而是有着自己的一套IPC机制。

## Binder

Binder就是Android中最具特色的IPC方式，AIDL其实就是通过Binder实现的，因为在我们定义好aidl文件后，studio就帮我们生成了相关的Binder类。事实上我们在使用AIDL时候继承的Stub类，就是studio帮我们生成的Binder类，所以我们可以通过查看studio生成的代码来了解Binder的工作原理。首先我们定义一个AIDL文件

```java
// UserManager.aidl
package cc.abto.demo;

interface UserManager {

    String getName();

    String getOtherName();
}
```

**sycn project**工程后，查看生成的UserManager.java文件：

```java
package cc.abto.demo;
// Declare any non-default types here with import statements

public interface UserManager extends android.os.IInterface
{

    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements cc.abto.demo.UserManager
    {
       //...
    }

    public java.lang.String getName() throws android.os.RemoteException;

    public java.lang.String getOtherName() throws android.os.RemoteException;
}                
```

生成的代码还是比较多的，我就不一次性全部贴上来了，先按类结构来看。

![](https://upload-images.jianshu.io/upload_images/2099385-77cef44f34461595.jpg)

sutido帮我们生成了一个继承android.os.IInterface接口的UserManager接口，所有在Binder中传输的接口都必须实现IInterface接口。接口定义了我们在AIDL文件中定义的方法，然后还有个内部静态类Stub，我们接着看这个Stub。

```java
public static abstract class Stub extends android.os.Binder implements cc.abto.demo.UserManager
    {

        private static final java.lang.String DESCRIPTOR = "cc.abto.demo.UserManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an cc.abto.demo.UserManager interface,
         * generating a proxy if needed.
         */
        public static cc.abto.demo.UserManager asInterface(android.os.IBinder obj)
        {

            if ((obj == null))
            {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof cc.abto.demo.UserManager)))
            {
                return ((cc.abto.demo.UserManager) iin);
            }
            return new cc.abto.demo.UserManager.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder()
        {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
        {

            switch (code)
            {
                case INTERFACE_TRANSACTION:
                {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getName:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _result = this.getName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_getOtherName:
                {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _result = this.getOtherName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements cc.abto.demo.UserManager
        {
           //...
        }

        static final int TRANSACTION_getName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);

        static final int TRANSACTION_getOtherName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    }
```

Stub继承了android.os.Binder并实现UserManager接口，下图是Stub的类结构。

![](https://upload-images.jianshu.io/upload_images/2099385-4b046a3aebaf748c.jpg)

我们可以看到Stub中的常量，其中两个int常量是用来标识我们在接口中定义的方法的，DESCRIPTOR常量是 Binder的唯一标识。
 **asInterface** 方法用于将服务端的Binder对象转换为客户端所需要的接口对象，该过程区分进程，如果进程一样，就返回服务端Stub对象本身，否则呢就返回封装后的Stub.Proxy对象。
 **onTransact** 方法是运行在服务端的Binder线程中的，当客户端发起远程请求后，在底层封装后会交由此方法来处理。通过code来区分客户端请求的方法，注意一点的是，如果该方法返回false的话，客户端的请求就会失败。一般可以用来做权限控制。
 最后我们来看一下Proxy代理类。

```java
private static class Proxy implements cc.abto.demo.UserManager
{

    private android.os.IBinder mRemote;

    Proxy(android.os.IBinder remote)
    {

        mRemote = remote;
    }

    @Override
    public android.os.IBinder asBinder()
    {

        return mRemote;
    }

    public java.lang.String getInterfaceDescriptor()
    {

        return DESCRIPTOR;
    }

    @Override
    public java.lang.String getName() throws android.os.RemoteException
    {

        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try
        {
            _data.writeInterfaceToken(DESCRIPTOR);
            mRemote.transact(Stub.TRANSACTION_getName, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        }
        finally
        {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    @Override
    public java.lang.String getOtherName() throws android.os.RemoteException
    {

        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try
        {
            _data.writeInterfaceToken(DESCRIPTOR);
            mRemote.transact(Stub.TRANSACTION_getOtherName, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readString();
        }
        finally
        {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }
}
```

代理类中我们主要看一下getName和getOtherName方法就可以了，这两个方法都是运行在客户端，当客户端发起远程请求时，\_data会写入参数，当然这边的例子并没有（啦啦啦...），然后调用transact方法发起RPC(远程过程调用)请求，同时挂起当前线程，然后服务端的onTransact方法就会被调起，直到RPC过程返回后，当前线程继续执行，并从_reply取出返回值（如果有的话），并返回结果。

## 最后

分析完 sutido 生成的 Binder 之后，就大概知道 AIDL 的工作原理，定义好 AIDL 文件只是方便 sutido 帮我生成所需的 Binder 类，AIDL 并不是必须的文件，因为这个 Binder 类我们也可以手写出来，所以这边最重要的还是 Binder 的知识点，其他一些 IPC 方式其实都是通过 Binder 来实现的，比如说 Messager，Bundle，ContentProvider，只是它们的封装方式不一样而已。总的来说，从应用层来说，Binder 是客户端和服务端之间通信的媒介。从 FrameWork 层来说，Binder 是 ServiceManager 连接各种 Manager 和 ManagerService 的桥梁。Android 系统中充斥着大量的 CS 模型，而 Binder 作为独有的 IPC 方式，如果能更好的理解它，对开发工作就会带来更多的帮助。

## 参考文章

1. [Android中AIDL的工作原理](https://www.jianshu.com/p/e0c583ea9289)

