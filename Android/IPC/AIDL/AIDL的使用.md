# AIDL 的使用

## AIDL 用来作什么

AIDL 是 Android 中 **IPC（Inter-Process Communication）**方式中的一种，AIDL是**Android Interface definition language**的缩写，对于小白来说，AIDL 的作用是让你可以在自己的 APP 里绑定一个其他 APP 的 service，这样你的 APP 可以和其他 APP 交互。

 ## AIDL 的使用

在 android studio 2.0 里面使用 AIDL，因为是两个 APP 交互么，所以当然要两个 APP 啦，我们在第一个工程目录右键。

![](https://upload-images.jianshu.io/upload_images/2099385-d68440c5786c6047.jpg)

输入名称后，sutido 就帮我们创建了一个 AIDL 文件。

```java
// IMyAidlInterface.aidl
package cc.abto.demo;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
```

上面就是 studio 帮我生成的 aidl 文件。 basicTypes 这个方法可以无视，看注解知道这个方法只是告诉你在 AIDL 中你可以使用的基本类型（int, long, boolean, float, double, String），因为这里是要跨进程通讯的，所以不是随便你自己定义的一个类型就可以在 AIDL 使用的。我们在 AIDL 文件中定义一个我们要提供给第二个 APP 使用的接口。

```java
interface IMyAidlInterface {
   String getName();
}
```

定义好之后，就可以**sync project**一下，然后新建一个service。在service里面创建一个内部类，继承你刚才创建的AIDL的名称里的Stub类,并实现接口方法,在onBind返回内部类的实例。

```java
public class MyService extends Service
{

    public MyService()
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new MyBinder();
    }

    class MyBinder extends IMyAidlInterface.Stub
    {

        @Override
        public String getName() throws RemoteException
        {
            return "test";
        }
    }
}
```

接下来，将我们的AIDL文件拷贝到第二个项目，然后**sync project**一下工程。

![](https://upload-images.jianshu.io/upload_images/2099385-585fbc5fb15906e8.png)

这边的包名要跟第一个项目的一样哦，这之后在Activity中绑定服务。

```java
public class MainActivity extends AppCompatActivity
{


    private IMyAidlInterface iMyAidlInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService(new Intent("cc.abto.server"), new ServiceConnection()
        {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {

                iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {

            }
        }, BIND_AUTO_CREATE);
    }

    public void onClick(View view)
    {
        try
        {
            Toast.makeText(MainActivity.this, iMyAidlInterface.getName(), Toast.LENGTH_SHORT).show();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }
}
```

这边通过隐式意图来绑定service，在onServiceConnected方法中通过**IMyAidlInterface.Stub.asInterface(service)**获取iMyAidlInterface对象，然后在onClick中调用iMyAidlInterface.getName()。

![](https://upload-images.jianshu.io/upload_images/2099385-0bc46eaf6923f712.png)

## 自定义类型

如果我要在AIDL中使用自定义的类型，要怎么做呢。首先我们的自定义类型要实现**Parcelable**接口，下面的代码中创建了一个User类并实现Parcelable接口。

![](https://upload-images.jianshu.io/upload_images/2099385-575c252bdc2790f1.png)

接下新建一个 aidl 文件，名称为我们自定义类型的名称，这边是 User.aidl。在 User.aidl 申明我们的自定义类型和它的完整包名，注意这边 parcelable 是小写的，不是 Parcelable 接口，一个自定类型需要一个这样同名的 AIDL 文件。

```
package cc.abto.demo;
parcelable User;
```

然后再在我们的AIDL接口中导入我们的AIDL类型。

![](https://upload-images.jianshu.io/upload_images/2099385-2352fd543be79349.png)

然后定义接口方法，**sync project**后就可以在service中做具体实现了。

```java
public class MyService extends Service
{
    //...
    @Override
    public IBinder onBind(Intent intent)
    {
        return new MyBinder();
    }

    class MyBinder extends IMyAidlInterface.Stub
    {
        //...
        @Override
        public User getUserName() throws RemoteException
        {
            return new User("wswf");
        }
    }
}
```

最后将我们的AIDL文件和自定义类型的java一并拷贝到第二个项目，注意包名都要一样哦

![](https://upload-images.jianshu.io/upload_images/2099385-63e992963f1bd552.png)

然后就可以在Activity中使用该自定义类型的AIDL接口了

```java
public class MainActivity extends AppCompatActivity
{
    //...
    public void onClick(View view)
    {
        try
        {
            Toast.makeText(MainActivity.this, iMyAidlInterface.getUserName().getName(), Toast.LENGTH_SHORT).show();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }
}
```

## 参考文章

1. [Android中AIDL的使用详解](https://www.jianshu.com/p/d1fac6ccee98)

