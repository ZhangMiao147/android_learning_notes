# Android 的常见问题 5

# 1. Android打包哪些类型文件不能混淆

　　屏幕适配

# 2. 模块化怎么做？怎么设计？接口发现暴漏怎么做？基于什么基本思想？ 

模块的接口暴露：https://www.cnblogs.com/cuizhf/archive/2011/08/11/2134868.html

https://www.jianshu.com/p/75b285f488eb

# 3. 设计个IM客户端以及数据库架构，类似微信，偏上层业务部分的会话、联系人、通知、 



# 4. 公众号如何存、分几张表，架构每一层都是什么，互相怎么交互工作？



# 5. RecyclerView/ListView中的观察者模式

# 6. SharedPareference 的 commit 和 apply 的区别

# 7. Android的多点触控如何传递 核心类 

https://blog.csdn.net/l707941510/article/details/81300333

https://blog.csdn.net/zhaoyazhi2129/article/details/40042693



# 8. bundle的数据结构，如何存储，既然有了Intent.putExtra，为啥还要用bundle。 

## 8.1. 解释

1. Bundle 只是一个信息的载体，内部其实就是维护了一个 Map< String, Object>。
2. Intent 负责 Activity 之间的交互，内部是持有一个 Bundle 的。

Intent.putExtra() 方法的源码：

```java
	public Intent putExtra(String name, boolean value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putBoolean(name, value);
        return this;
    }
```

putExtras(Bundle bundle)：会将 Intent 的内部 Bundle 替换成参数 bundle。

## 8.2. 应用场景

举个例子，如果我要从A界面跳转到B界面和C界面，那么我要写写两个Intent，如果传的数据相同，我两个Intent都要添加，但是如果我用Bundle，直接一次性包装就可以了。

再有，如果A界面要传值给B界面，再从B界面传值到C界面，你可以想象用Intent是多么的麻烦了，但是用Bundle就很简洁，而且还可以在B界面中添加新的信息。

## 8.3. bundle使用场景

- 在设备旋转时保存数据



```java
 // 自定义View旋转时保存数据
public class CustomView extends View {
    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.put...
        return bundle;
    }
```



```java
  // Activity旋转时保存数据
public class CustomActivity extends Activity {
  
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.put...
    }
```

- Fragment之间传递数据
   比如，某个 Fragment 中点击按钮弹出一个 DialogFragment。
   最便捷的方式就是通过 Fragment.setArguments(args) 传递参数。

所以，Bundle是不可替代的。

# 9. 那你能说一下 Intent 是怎么进程通信的 

　　两个应用之间进程间通信，主要通过 Intent 的隐式意图来实现。

　　隐式调用就需要 Intent 能够匹配目标组件的 IntentFilter 中所设置的过滤信息，如果匹配不成功就不能启动目标 Actiivty。

　　IntentFilter 中过滤的信息包括：action、category、data。

　　关于 IntentFilter 的一些描述：

* 匹配过滤列表时需要同时匹配过滤列表中的 action、category、data。

* 一个 Activity 中可以有多组 intent-filter。
* 一个 intent-filter 可以有多个 action、category、data，并各自构成不同类别，一个 Intent 必须同时匹配 action 类别、category 类别和 data 类别才算完全匹配。
* 一个 Intent 只要能匹配任何一组 intent-filter 就算匹配成功。

　　注册 MyActivity 的过滤信息:

```java
<activity android:name="com.intentfilter.MyActivity">
    <intent-filter>
        <action android:name="com.action.a"/>
        <action android:name="com.action.b"/>

        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="com.category.c"/>
        <category android:name="com.category.d"/>

        <data android:mimeType="text/plain"/>
    </intent-filter>
</activity>
```

## 9.1. action 的匹配规则

　　action 是一个字符串，系统默认定义了一些 action，同时也可以自定义。

### 9.1.1. action 的匹配规则

1. Intent 中必须存在 action，这一点和 category 不同。
2. action 的字符串严格区分大小写，intent 中的 action 必须和过滤规则中的 action 完全一致才能匹配成功。
3. 匹配规则中可以同时有多个 action，但是 Intent 中的 action 只需与其中一只相同即可匹配成功。

### 9.1.2.  action 的匹配

　　按照 action 的匹配规则，为了启动 MyActivity，Intent 的 action 可以为 “com.action.a” 或者 “com.action.b”。

## 9.2. category 匹配规则

　　和 action 一样，category 也是一个字符串，系统同样默认定义了一些，也可以自定义。

### 9.2.1. category 的匹配规则

1. 匹配规则中必须添加 “action.intent.category.DEFAULT” 这个过滤条件。
2. Intent 中可以不设置 category，系统会自动添加 “action.intent.category.DEFAULT” 这个默认的 category。
3. Intent 中可以同时设置多个 category，一旦设置多个 category，那么每个 category 都必须能够和过滤条件中的某个 category 匹配成功。

　　category 的第 3 个规则和 action 的匹配规则有所不同，action 有多个的时候，主要其中之一能够匹配成功即可，但是 category 必须是每一个都需要匹配成功。

### 9.2.2. category 匹配

　　按照 category 的匹配规则，为了启动 MyActivity，可以设置 Intent 的 category：setCategory("com.category.c") 或者是 setCategory("com.category.d") ，再或者是不设置 category。

## 9.3. data 的匹配规则

　　data 的结构：

```xml
   <data android:scheme="string"
      android:host="string"
      android:port="string"
      android:path="string"
      android:pathPattern="string"
      android:pathPrefix="string"
      android:mimeType="string" />
```

　　这些值是在 AndroidManifest 文件中可以定义的。

　　data 主要是由 URI 和 mimeType 组成的。

1. mimeType 表示 image/ipeg，video/* 等媒体结构。

   这个属性就是说要传递什么类型的数据，通常有 text/plain 或 image/jpeg。

2. URI 信息量相对大一些，其结构一般如下：

```xml
<scheme> :// < host> : <port> [<path>|<pathPrefix>|<pathPattern>]
```

　　URI 各个节点数据的含义：

1. scheme：整个 URI 的模式，如常见的 http、file 等，注意如果 URI 中没有指定的 scheme，那么整个 uri 无效。
2. host：URI 的域名，比如常见的 www.mi.com、www.baidu.com，与 scheme 一样，一旦没有 host ，那么整个 URI 也毫无意义。
3. port：端口号，比如 80，只有在 URI 中制定了 scheme 和 host 之后端口号才是有意义的。
4. path、PathPattern、pathPrefix 包含路径信息，path 表示完整的路径，pathPattern 在此基础上可以包含通配符，pathPrefix 表示路径的前缀信息。

### 9.3.1. data 的匹配规则

1. Intent 中必须有 data 数据。
2. Intent 中的 data 必须和过滤规则中的某一个 data 完全匹配。
3. 过滤规则中可以有多个 data 存在，但是 Intent 中的 data 只需匹配其中的任意一个 data 即可。
4. 过滤规则中可以没有指定 URI，但是系统会赋予其默认值：content 和 file，这一点在 Intent 中需要注意。
5. 为 Intent 设定 data 和 type 的时候必须要调用 setDataAndType() 方法，而不能先 setData 再 setType，因为这两个方法是互斥的，都会清除对方的值。
6. 在匹配规则中，data 的 scheme、host、post、path 等属性可以写在同一个 < / > 中，也可以分来单独写，其功效是一样的。

### 9.3.2. data 匹配

　　按照 data 的匹配规则，为了启动 MyActivity，可以设置 Intent 如下：

```java
Intent intent = new Intent("com.action.a");
intent.addCategory("com.category.c")
intent.setData(Uri.parse("file://abc"),"text/plain");
startActivity(intent);
```

　　这样就可以跳转到 MyActivity。



