# IntentFilter 的匹配规则
## 1. 关于 IntentFilter

　　Activity 的启动方式分为隐式和显式两种。

　　显示需要明确的指定被启动对象的组卷信息，比如包名和类型等，而隐式调用就不需要明确指定组件的信息。

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

　　字符中的字符时严格区分大小写的。所以 action 的匹配是区分大小写的。

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

　　data 主要是由 URI 和 mimeType 组成的。

　　URL 的结构如下：
```xml
<scheme> :// < host> : <port> [<path>|<pathPrefix>|<pathPattern>]
```

　　这些值在 Manifest 文件中可以定义，语法如下：

```xml
<data android:scheme="string"
      android:host="string"
      android:port="string"
      android:path="string"
      android:pathPattern="string"
      android:pathPrefix="string"
      android:mimeType="string" />
```

　　设置 SecondActivity 的 data :

```xml
<activity android:name=".SecondActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data
            android:host="zede"
            android:port="1010"
            android:scheme="chan" />
    </intent-filter>
</activity>
```

　　跳转 SecondActiivty：

```java
Intent intent = new Intent();
intent.setData(Uri.parse("chan://zede:1010"));
startActivity(intent);
```

　　这样就可以跳转到 SecondActivity。

　　也可以创建一个 html 文件来实现跳转 SecondActivity：

```html
<!DOCTYPE html>
<html>
<head>
	<title></title>
</head>
<body>
<a href="chan://zede:1010/mypath?user=admin&psd=123456">跳转至SecondActivity</a>
</body>
</html>
```

　　使用收集浏览器打开这个 html 文件，点击这个超链接就可以跳转到 SecondActivity。而链接中的 `user=admin&psd=123456` 是传递给 SecondActivity 的数据。而在 SecondActivity 接收数据：

```java
Intent intent = getIntent();
Uri uri = intent.getData();

Log.e("chan", "==================getScheme= " + intent.getScheme()); // chan
Log.e("chan", "==================getHost= " + uri.getHost()); // zede
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

　　还需要注意另一个属性：android:mimeType，这个属性就是说要传递什么类型的数据，通常有 text/plain 或 image/jpeg。

　　可以通过以下代码来启动 Activity：

```java
intent.setType("text/plain");
```

　　如果同时设置了 URI 和 mimeType 的话就必须使用如下代码才可以跳转：

```java
intent.setDataAndType(Uri.parse("chan://zede:1010"), "text/plain");
```

　　因为如果使用 setData() 或者 setType() 的话，会分别将对应的 type 和 data 置为 null。





## 3. 参考文章

2. [史上最全intent-filter匹配规则，没有之一](https://www.jianshu.com/p/7ebc63399968)
2. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)

