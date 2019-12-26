# 关于 ContentProvider 的知识

## ContentProvider 概念

　　需要操作其他应用程序的一些数据，例如需要操作系统里的媒体库、通讯录等，这是就可以通过 ContentProvider 来完成需求。

## 为什么要选择 ContentProvider

　　虽然也可以通过文件等其他方式来达到在不同程序之间共享数据，但是会很复杂，而 ContentProvider 也是可以实现应用程序之间共享数据的，除此之外，还有其他优点。以下就是 ContentProvider 的特点：

1. ContentProvider 为存储和获取数据提供了统一的接口。ContentProvider 对数据数据进行了封装，不用关心数据存储的细节。统一了数据的访问方式。
2. 使用 ContentProvider 可以在不同的应用程序之间共享数据。
3. Android 为常见的一些数据提供了默认的 ContentProvider（包括音频、视频、图片和通讯录等）。



## Uri 介绍

　　每一个 ContentProvider 都拥有一个公共的 URI，这个 URI 用于表示这个 ContentProvider 所提供的数据。

　　Android 所提供的 ContentProvider 都存放在 android.provider 包中。

　　A：标准前缀，用来说明一个 ContentProvider 控制这些数据，无法改变。

　　B：URI 的标识，用来唯一标识这个 ContentProvider ，外部调用者可以根据这个标识来找到它。它定义了是哪个 ContentProvider 提供这些数据。对于第三方应用程序，为了保证 URI 标识的唯一性，它必须是一个完整的、小写的类名。这个标识在元素的 authorities 属性中说明，一般是定义该 ContentProvider 的包类的名称。

　　C：路径（path），就是要操作的数据库中表的名字，或者也可以自己定义。

　　D：如果 URI 中包含表示需要获取的记录的 ID，则就返回该 id 对应的数据，如果没有 ID，就表示返回全部。

## 操作 Uri 的工具类

　　Uri 代表了要操作的数据，所以有时需要解析 Uri，并从 Uri 中获取数据。Android 系统提供了两个用于操作 Uri 的工具类，分别是 UriMatcher 和 ContentUris。

#### UriMatcher 类

UriMatcher 类用于匹配 Uri。

UriMatcher 的用法是：

1. 把需要匹配的 Uri 路径使用 UriMatcher 的 addURI 方法全部注册上。

```
addURI 方法
```



2. 使用 UriMatcher 的 match 方法进行匹配。如果匹配就返回匹配码。

```
match 方法
```



UriMatch.NO_MATCH（-1） 表示不匹配任何路径的返回码。

#### ContentUris

ContentUris 类用于操作 Uri 路径后面的 ID 部分。

withAppendedId(Uri,id) 方法用于为路径加上 ID 部分。

parseId(Uri) 方法用于从路径中获取 ID 部分。



## 参考文章

1. [Android 四大组件 -- ContentProvider 详解](https://www.cnblogs.com/0927wyj/p/5385565.html)
2. [深入理解 Android 四大组件之一 ContentProvider](https://blog.csdn.net/hehe26/article/details/51784355)
3. [android ContentResolver 详解](https://blog.csdn.net/cankingapp/article/details/7792999)
4. [Android四大组件之ContentProvider](https://www.jianshu.com/p/540a62ec37ea)

