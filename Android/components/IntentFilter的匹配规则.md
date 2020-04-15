# IntentFilter 的匹配规则
## 1. 关于 IntentFilter

　　Activity 的启动方式分为隐式和显式两种。

　　显示需要明确的指定被启动对象的组件信息，比如包名和类型等，而隐式调用就不需要明确指定组件的信息。

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

## 2. action 的匹配规则

　　action 是一个字符串，系统默认定义了一些 action，同时也可以自定义。

　　字符中的字符时严格区分大小写的，所以 action 的匹配是区分大小写的。

### 2.1. action 的匹配规则

1. Intent 中必须存在 action，这一点和 category 不同。
2. action 的字符串严格区分大小写，intent 中的 action 必须和过滤规则中的 action 完全一致才能匹配成功。
3. 匹配规则中可以同时有多个 action，但是 Intent 中的 action 只需与其中一只相同即可匹配成功。

### 2.2.  action 的匹配

　　按照 action 的匹配规则，为了启动 MyActivity，Intent 的 action 可以为 “com.action.a” 或者 “com.action.b”。

## 3. category 匹配规则

　　和 action 一样，category 也是一个字符串，系统同样默认定义了一些，也可以自定义。

### 3.1. category 的匹配规则

1. 匹配规则中必须添加 “action.intent.category.DEFAULT” 这个过滤条件。
2. Intent 中可以不设置 category，系统会自动添加 “action.intent.category.DEFAULT” 这个默认的 category。
3. Intent 中可以同时设置多个 category，一旦设置多个 category，那么每个 category 都必须能够和过滤条件中的某个 category 匹配成功。

　　category 的第 3 个规则和 action 的匹配规则有所不同，action 有多个的时候，主要其中之一能够匹配成功即可，但是 category 必须是每一个都需要匹配成功。

### 3.2. category 匹配

　　按照 category 的匹配规则，为了启动 MyActivity，可以设置 Intent 的 category：setCategory("com.category.c") 或者是 setCategory("com.category.d") ，再或者是不设置 category。

## 4. data 的匹配规则

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

### 4.1. data 的匹配规则

1. Intent 中必须有 data 数据。
2. Intent 中的 data 必须和过滤规则中的某一个 data 完全匹配。
3. 过滤规则中可以有多个 data 存在，但是 Intent 中的 data 只需匹配其中的任意一个 data 即可。
4. 过滤规则中可以没有指定 URI，但是系统会赋予其默认值：content 和 file，这一点在 Intent 中需要注意。
5. 为 Intent 设定 data 和 type 的时候必须要调用 setDataAndType() 方法，而不能先 setData 再 setType，因为这两个方法是互斥的，都会清除对方的值。
6. 在匹配规则中，data 的 scheme、host、post、path 等属性可以写在同一个 < / > 中，也可以分来单独写，其功效是一样的。

### 4.2. data 匹配

　　按照 data 的匹配规则，为了启动 MyActivity，可以设置 Intent 如下：

```java
Intent intent = new Intent("com.action.a");
intent.addCategory("com.category.c")
intent.setData(Uri.parse("file://abc"),"text/plain");
startActivity(intent);
```

　　这样就可以跳转到 MyActivity。

#### 4.2.1. 使用 html 来跳转 Activity

　　也可以创建一个 html 文件来实现跳转 SecondActivity：

```html
<!DOCTYPE html>
<html>
<head>
	<title></title>
</head>
<body>
<a href="content://intentfilter:1010/mypath?user=admin&psd=123456">跳转至 Activity</a>
</body>
</html>
```

　　使用收集浏览器打开这个 html 文件，点击这个超链接就可以跳转到匹配的 data 的 Activity。而链接中的 `user=admin&psd=123456` 是传递给 Activity的数据。而在 Activity接收数据：

```java
Intent intent = getIntent();
Uri uri = intent.getData();

Log.e("chan", "==================getScheme= " + intent.getScheme()); // content
Log.e("chan", "==================getHost= " + uri.getHost()); // intentfilter
Log.e("chan", "==================getPort= " + uri.getPort()); // 1010
Log.e("chan", "==================getPath= " + uri.getPath()); // /mypath

Log.e("chan", "==================getQuery= " + uri.getQuery()); // user=admin&psd=123456

Set < String > names = uri.getQueryParameterNames();

Iterator < String > iterator = names.iterator();
while (iterator.hasNext()) {
    String key = iterator.next();
    uri.getQueryParameter(key);
    Log.e("chan", "==================getQueryParameter= " + uri.getQueryParameter(key)); // admin 123456
}
```

## 5. 匹配失败

　　如果 action、category 和 data 所有的匹配失败就会报：

```xml
android.content.ActivityNotFoundException: No Activity found t0 handle Intent { act= com.action.a cat=[com.category.c] dat=file://abc type=text/plain }
...
```

　　为了避免发生上面的异常，可以使用 PackageManager 或者 Intent 的 resolveActivity() 方法，如果 intent 和过滤规则匹配失败，那么将返回 null，也就不会再继续调用 startActivity 方法了，从而去修改 intent 再次进行匹配直到成功匹配到预期 intent。

## 6. 其他

　　在 action 和 category 中有一些是系统自带的，其中有些比较重要，如：

```xml
<action android:name="android.intent.action.MAIN"/>

<category android:name="android.intent.category.LAUNCHER"/>
```

　　这两者一般是成双成对出现的，用来表明这是一个程序的入口，并且会出现在系统的应用列表中，之所以成双成对的出现是说，二者却已不可，少了彼此都没有实际意义。

　　intent-filter 匹配规则是适用于 Intent、Service 和 BroadcastReceiver 的，但是在 Android 5.0 以后需要显式调用来启动 Service，否则会报异常：`java.lang.IllegalArgumentException:Service Intent must be explicit`。

## 7. 参考文章

1. [史上最全intent-filter匹配规则，没有之一](https://www.jianshu.com/p/7ebc63399968)
2. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)

