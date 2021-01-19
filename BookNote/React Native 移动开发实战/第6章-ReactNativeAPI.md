# 第 6 章 React Native API

## 6.1. 基础 API 

**所谓API，就是一些预先定义好的函数，目的是让开发人员在无须访问源码或理解内部工作机制的前提下，通过调用API实现某种特定的能力。**

**在React Native应用开发中，AppRegistry是应用程序JavaScript代码的运行入口，是最基本的API。**

**通常，运行一个React Native应用大致会经历如下过程：应用程序的根组件使用AppRegistry.registerComponent()注册自己，然后原生系统加载应用的代码及资源包，并在加载完成之后调用AppRegistry.runApplication()来真正运行应用。**

**AppRegistry提供的getAppKeys()也可以用来获取应用运行时的信息**

**除了registerComponent()方法外，AppRegistry还提供以下常用方法。**

**·registerConfig()：注册指定的配置。·registerRunnable()：注册进程。·registerSection()：注册一个切片。·getAppKeys()：获取所有注册的线程。·getRegistry()：获取所有注册的信息。·runApplication()：启动React Native应用。·unmountApplicationComponentAtRootTag()：销毁应用。**

**需要说明的是，作为React Native开发中最基本的API之一，AppRegistry应当先于其他模块导入，以确保其他模块正常运行。**

**在React Native开发中，经常会遇到前后台状态切换的场景。为了监控应用的运行状态，React Native提供了AppState。通过AppState开发者可以很容易地获取应用的当前状态。**

**在AppState中，应用的状态被分为active、background和inactive。其中，active表示应用处于前台运行状态，background表示应用处于后台运行中，inactive表示应用处于前后台切换过程中或是处在系统的多任务视图中。**

**AppState的使用方法比较简单，通过AppState.currentState即可获取应用当前的状态**

**除了获取应用的当前状态外，AppState还支持事件监听，事件监听需要用到addEventListener()和removeEventListener()两个方法。**

**NetInfo是React Native提供的一个用于获取手机联网状态的API，开发者使用此API可以轻松获取手机的联网状态**

**之所以能获取手机的联网状态，是因为React Native在初始化项目时会默认添加node-fetch包的依赖，该包是获取网络状态的重要工具。**

**作为React Native开发中一个比较常见的API，NetInfo的使用方法比较简单，使用时只需要调用getConnectionInfo()方法即可获取手机的联网状态。**

**由于NetInfo最终是通过原生系统来获取联网信息的，所以NetInfo在iOS和Android上返回的值也不是完全相同的。**

**不过，NetInfo返回的通用网络类型有none、wifi、cellular和unknown。·none：设备处于离线状态。·wifi：通过Wi-Fi联网或者设备是iOS模拟器。·cellular：通过蜂窝数据流量联网。·unknown：联网状态异常。**

**除了上面的通用状态外，Android设备的联网状态还包括wimax、bluetooth和ethernet。·bluetooth：设备通过蓝牙协议联网。·ethernet：设备通过以太网协议联网。·wimax：设备通过WiMAX协议联网。**

**除此之外，NetInfo的effectiveType还可以返回手机的联网类型：如2G、3G或4G。**

**除了获取联网状态外，开发者还可以使用NetInfo提供的addEventListener()方法来监听网络状态。**

**addEventListener()方法的格式如下。    NetInfo.addEventListener(eventName, handler);其中，eventName表示事件名，handler表示监听函数。**

**同时，为了不造成事件带来的额外资源消耗，还需要在合适的地方使用removeEventListener()方法移除事件监听。**

**对于Android设备来说，NetInfo提供的isConnectionExpensive()方法可以用来判断当前连接是否收费，**

**AsyncStorage是一个异步、持久化的数据存储API，它以键值对的方式保存数据，其作用等价于iOS的NSUserDefaluts或Android的SharedPreferences**

**由于AsyncStorage的操作是全局的，所以官方建议开发者先对AsyncStorage进行封装后再使用，而不是直接使用。**

**为了方便操作AsyncStorage，官方提供了如下方法。**

**getItem()：根据键值来获取结果，并将获取的结果返回给回调函数。·setItem()：根据键值来保存value的值，完成后调用回调函数。**

**·removeItem()：根据键值删除某个值，并将结果返回给回调函数。·mergeItem()：将已有的值和新的**

**值进行合并，合并的结果会返回给回调函数。·clear()：清除所有的AsyncStorage保存的数据。·getAllKeys()：获取所有本应用可以访问的数据，并将结果返回给回调函数。·flushGetRequests()：清除所有进行中的查询操作。·multiGet()：获取key所包含的所有字段的值，并将结果以回调函数的方式传递给一个键值数组。·multiRemove()：删除所有key字段名数组中的数据，并将结果返回给回调函数。·multiMerge()：将输入的值和已有的值合并，合并的对象要求是数组格式，合并的结果会返回给回调函数。**

**需要说明的是，对于iOS平台来说，使用AsyncStorage保存数据时，系统会把数据保存到沙盒的Documents中并生成manifest.json文件，数据就存在这个manifest.json文件中。同时，执行删除操作时也仅仅是删除manifest.json文件中的某条数据，而不是删除manifest.json文件。**

**在移动应用开发中，如果两个相互独立的组件或进程之间要进行通信，最简单的方式就是使用广播。DeviceEventEmitter是React Native官方提供的用以实现事件发送和接收的API，其作用类似于原生系统的广播。**

**和原生系统的广播机制类似，DeviceEventEmitter使用的也是典型的发布订阅模式，即接收方在事件接收页面使用DeviceEventEmitter.addListener()方法注册需要监听的事件，而事件发送方则使用DeviceEventEmitter.emit函数发送事件。**

**需要说明的是，使用DeviceEventEmitter执行跨组件或跨进程通信时，除了可以发送基本的数据类型，DeviceEventEmitter还支持传递JSON格式数据，开发时可以根据实际情况合理选择。**

## 6.2.  屏幕相关 API



## 6.3. 动画 API



## 6.4. 平台 API



## 6.5. 本章小结

