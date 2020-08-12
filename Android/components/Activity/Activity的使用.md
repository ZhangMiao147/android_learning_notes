# Activity 的使用

```
本文内容：
1. 使用 Activity 显式简单界面
2. Activity 之间的跳转
	2.1 startActivity
	2.2 startActivityForResult
3. Activity 之间的数据交互
	4. Activity 的启动		
	4.1 显示启动		
	4.2 隐式启动	
5. 启动系统中常见的 Activity		
	5.1 打开浏览器网页		
	5.2 打开相册	
	5.3 打开发送短信界面		
	5.4 打开拨号界面	
6. 结束 Activity
7. 参考文章
```

[TOC]

# 1. 使用 Activity 显式简单界面

* 创建一个 Activity 的子类 MainActivity ，继承 Activity

```java
package com.zhangmiao.activityproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

```

* 创建 MainActivity 的界面 activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/main_activity_message_tv"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:freezesText="true"
        android:text="这是 MainActivity" />
</LinearLayout>
```

* 在清单文件中声明 MainActivity

```xml
 <activity
	android:name=".MainActivity"
    android:configChanges="orientation|screenSize"
    >
	<intent-filter>
		<action android:name="android.intent.action.MAIN" />
		<category android:name="android.intent.category.LAUNCHER" />
	</intent-filter>
</activity>
```

# 2. Activity 之间的跳转

## 2.1. startActivity

　　通过调用 startActivity() 方法可以启动其他 Activity，在 Intent 中指定想要启动的 Activity ，Intent 对象还可能携带少量数据传递给想要启动的 Activity。

* 简单的 MainActivity 跳转 FirstActivity 界面
```java
Intent firstIntent = new Intent(MainActivity.this, FirstActivity.class);
//携带数据
firstIntent.putExtra("from", "MainActivity");
startActivity(firstIntent);
```

## 2.2 startActivityForResult

　　有时可能需要从启动的 Activity 获得结果。在这种情况下，就通过 startActivityForResult() 方法来启动 Activity ，被启动的 Activity 通过 setResult() 方法返回数据，启动的 Activity 通过 onActivityResult() 方法获取返回的数据。

* 简单的 MainActivity 跳转 FirstActivity 界面，FirstActivity 返回数据给 MainActivity
```java
Intent firstIntent = new Intent(MainActivity.this, FirstActivity.class);
firstIntent.putExtra("from", "MainActivity");
startActivityForResult(firstIntent,10001);
```

* FirstActivity 在合适的位置返回数据给 MainActivity
```java
        Intent intent = new Intent();
        intent.putExtra("result","OK");
        setResult(20001,intent);
```

* MainActivity 接收 FirstActivity 返回的数据
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001 && resultCode == 20001){
            if (data != null) {
                String result = data.getStringExtra("result");
            }
        }
    }
```

# 3. Activity 之间的数据交互

　　可以使用 Intent 对象进行数据的传递。Intent 重载了很多 putExtra() 方法，包括了 Java 八大基本类型以及其数组类型等。

*  MainActivity 跳转 FirstActivity 界面，并传递 String 类型的数据

```java
Intent firstIntent = new Intent(MainActivity.this, FirstActivity.class);
firstIntent.putExtra("from", "MainActivity");
startActivityForResult(firstIntent,10001);
```
　　Intent 对象还有一个 putExtras(Bundle bundle) 方法，就是把需要传递的数据组合在一起进行传递。

*  MainActivity 跳转 FirstActivity 界面，使用 putExtras(Bundle bundle) 传递数据

```java
Intent firstIntent = new Intent(MainActivity.this, FirstActivity.class);
Bundle bundle = new Bundle();
bundle.putString("from", "MainActivity");
bundle.putInt("count", 1);
firstIntent.putExtras(bundle);
startActivityForResult(firstIntent,10001);
```

# 4. Activity 的启动

## 4.1. 显示启动

　　显示启动的方式：

1. 直接在 Intent 构造方法启动：
```java
Intent firstIntent = new Intent(MainActivity.this, FirstActivity.class);
```

2. setComponent
```java
ComponentName componentName = new ComponentName(this,FirstActivity.class);
Intent firstIntent = new Intent();
firstIntent.setComponent(componentName);
startActivity(firstIntent);
```

3. setClass / setClassName
```java
Intent firstIntent = new Intent();
firstIntent.setClass(this,FirstActivity.class);
firstIntent.setClassName(this,"com.zhangmiao.activityproject.FirstActivity");
startActivity(firstIntent);
```

## 4.2. 隐式启动

　　隐式启动就是要在该 Activity 中设置 IntentFilter 属性，只要启动的 Intent 匹配 IntentFilter 的条件就可以启动相应的 Activity 。

　　对于想要作出响应的每一个 Intent 类型，必须加入相应的 < intent-filter >，其中包括一个 < action > 元素，还可选择性地包括一个 < category > 元素或一个 < data > 元素。这些元素指定 activity 可以响应的 Intent 类型。

* 例如调用系统的相册
```java
Intent intent = new Intent();
intent.setAction(Intent.ACTION_GET_CONTENT);
intent.setType("image/*");
startActivity(intent);
```

# 5. 启动系统中常见的 Activity

　　系统给我们提供了很多常用的 Activity，可以用来打开浏览器，打开发短信界面，打开相册界面，打开拨号界面等等。

## 5.1. 打开浏览器网页

```java
Intent intent = new Intent();
intent.setAction(Intent.ACTION_VIEW);
intent.setData(Uri.parse("http://www.baidu.com"));
startActivity(intent);
```

## 5.2. 打开相册

```java
Intent intent = new Intent();
intent.setAction(Intent.ACTION_GET_CONTENT);
intent.setType("image/*");
startActivity(intent);
```

## 5.3. 打开发送短信界面

```java
Intent intent = new Intent();
intent.setAction(Intent.ACTION_SNED);
intent.setType("text/plain");
intent.putExtra(Intent.EXTRA_TEXT,"Hello World!");
startActivity(intent);
```

## 5.4. 打开拨号界面

```java
Intent intent = new Intent();
intent.setAction(Intent.ACTION_VIEW);
intent.setData(Uri.parse("tel:110"));
startActivity(intent);
```

# 6. 结束 Activity

　　可以通过调用 Activity 的 finish() 方法来结束 Activity 。还可以通过调用 finishActivity() 方法结束之前启动的另一个 Activity 。
　　大多数情况下，不应该使用 finish() 或 finishActivity() 方法来结束 Activity 。Android 系统会管理 Activity 的生命周期，会在合适的时机结束 Activity 。调用结束 Activity 的方法可能对预期的用户体验产生不良影响，因此只应该在确实不想让用户返回此 Activity 实例时使用。

# 7. 参考文章

1. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)
2. [Android之Activity系列总结（一）--Activity概览](https://www.cnblogs.com/jycboy/p/6367282.html)
3. [Android四大组件之Activity详解](https://www.cnblogs.com/caobotao/p/4987015.html)

