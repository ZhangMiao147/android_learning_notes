# IntentFilter 的使用
	本文内容
	1. 关于 IntentFilter
	2. IntentFilter 的使用
[TOC]

## 1. 关于 IntentFilter

## 1.1. IntentFilter 的使用
　　IntentFilter 有三个标签分别是：action、category、data。

　　关于 IntentFilter 的一些描述：
* 一个 Activity 中可以有多个 intent-filter
* 一个 intent-filter 同事可以有多个 action、category、data
* 一个 Intent 只要能匹配任何一组 intent-filter 即可启动对应 Activity

#### 1.1.1. action 的匹配规则
　　action 的匹配规则就是只要满足其中一个 action 就可以启动成功了。


#### 1.1.2. category 匹配规则
　　会默认匹配 “android.intent.category.DEFAULT”。

#### 1.1.3. data 的匹配规则
　　data 主要是由 URI 和 mimeType 组成的。

　　URL 的结构如下：
```
<scheme> :// < host> : <port> [<path>|<pathPrefix>|<pathPattern>]
```

　　这些值在 Manifest 文件中可以定义，语法如下：

```
<data android:scheme="string"
      android:host="string"
      android:port="string"
      android:path="string"
      android:pathPattern="string"
      android:pathPrefix="string"
      android:mimeType="string" />
```


## 2. 参考文章
1. [Activity 必知必会](https://juejin.im/post/5aef0d215188253dc612991b)
2. [史上最全intent-filter匹配规则，没有之一](https://www.jianshu.com/p/7ebc63399968)