# 第 6 章 React Native API

## 6.1. 基础 API 

​		所谓 API，就是一些预先定义好的函数，目的是让开发人员在无须访问源码或理解内部工作机制的前提下，通过调用 API 实现某种特定的能力。

### 6.1.1. AppRegistry

​		在 React Native 应用开发中，AppRegistry 是应用程序 JavaScript 代码的运行入口，是最基本的 API。

​		通常，运行一个 React Native 应用大致会经历如下过程：应用程序的根组件使用 AppRegistry.registerComponent() 注册自己，然后原生系统加载应用的代码及资源包，并在加载完成之后调用 AppRegistry.runApplication() 来真正运行应用。

​		AppRegistry 提供的 getAppKeys() 也可以用来获取应用运行时的信息。

​		需要说明的是，作为 React Native 开发中最基本的 API 之一，AppRegistry 应当先于其他模块导入，以确保其他模块正常运行。

### 6.1.2. AppState

​		在 React Native 开发中，经常会遇到前后台状态切换的场景。为了监控应用的运行状态，React Native 提供了 AppState。通过 AppState 开发者可以很容易地获取应用的当前状态。

​		在 AppState 中，应用的状态被分为 active、background 和 inactive。其中，active 表示应用处于前台运行状态，background 表示应用处于后台运行中，inactive 表示应用处于前后台切换过程中或是处在系统的多任务视图中。

​		AppState 的使用方法比较简单，通过 AppState.currentState 即可获取应用当前的状态。

​		除了获取应用的当前状态外，AppState 还支持事件监听，事件监听需要用到 addEventListener() 和 removeEventListener() 两个方法。

### 6.1.3. NetInfo

​		NetInfo 是 React Native 提供的一个用于获取手机联网状态的 API，开发者使用此 API 可以轻松获取手机的联网状态。

​		之所以能获取手机的联网状态，是因为 React Native 在初始化项目时会默认添加 node-fetch 包的依赖，该包是获取网络状态的重要工具。

​		作为 React Native 开发中一个比较常见的 API，NetInfo 的使用方法比较简单，使用时只需要调用 getConnectionInfo() 方法即可获取手机的联网状态。

​		由于 NetInfo 最终是通过原生系统来获取联网信息的，所以 NetInfo 在 iOS 和 Android 上返回的值也不是完全相同的。

​		不过，NetInfo返回的通用网络类型有 none、wifi、cellular 和 unknown。

* none：设备处于离线状态。
* wifi：通过 Wi-Fi 联网或者设备是 iOS 模拟器。、
* cellular：通过蜂窝数据流量联网。
* unknown：联网状态异常。

​		除了上面的通用状态外，Android 设备的联网状态还包括 wimax、bluetooth 和 ethernet。

* bluetooth：设备通过蓝牙协议联网。
* ethernet：设备通过以太网协议联网。
* wimax：设备通过 WiMAX 协议联网。

​		除此之外，NetInfo 的 effectiveType 还可以返回手机的联网类型：如 2G、3G 或 4G。

​		除了获取联网状态外，开发者还可以使用 NetInfo 提供的 addEventListener() 方法来监听网络状态。addEventListener() 方法的格式如下。 

```java
NetInfo.addEventListener(eventName, handler);
```

​		其中，eventName 表示事件名，handler 表示监听函数。

​		同时，为了不造成事件带来的额外资源消耗，还需要在合适的地方使用 removeEventListener() 方法移除事件监听。

​		对于 Android 设备来说，NetInfo 提供的 isConnectionExpensive() 方法可以用来判断当前连接是否收费。

### 6.1.4. AsyncStrage

​		AsyncStorage 是一个异步、持久化的数据存储 API，它以键值对的方式保存数据，其作用等价于 iOS 的 NSUserDefaluts 或 Android 的 SharedPreferences。

​		由于 AsyncStorage 的操作是全局的，所以官方建议开发者先对 AsyncStorage 进行封装后再使用，而不是直接使用。

​		需要说明的是，对于 iOS 平台来说，使用 AsyncStorage 保存数据时，系统会把数据保存到沙盒的 Documents 中并生成 manifest.json 文件，数据就存在这个 manifest.json 文件中。同时，执行删除操作时也仅仅是删除 manifest.json 文件中的某条数据，而不是删除 manifest.json 文件。

### 6.1.5. DeviceEventEmitter

​		在移动应用开发中，如果两个相互独立的组件或进程之间要进行通信，最简单的方式就是使用广播。DeviceEventEmitter 是 React Native 官方提供的用以实现事件发送和接收的 API，其作用类似于原生系统的广播。

​		和原生系统的广播机制类似，DeviceEventEmitter 使用的也是典型的发布订阅模式，即接收方在事件接收页面使用 DeviceEventEmitter.addListener() 方法注册需要监听的事件，而事件发送方则使用 DeviceEventEmitter.emit 函数发送事件。

​		需要说明的是，使用 DeviceEventEmitter 执行跨组件或跨进程通信时，除了可以发送基本的数据类型， DeviceEventEmitter 还支持传递 JSON 格式数据，开发时可以根据实际情况合理选择。

## 6.2.  屏幕相关 API

### 6.2.1. Dimensions

​		Dimensions 是官方提供的一个用于获取屏幕尺寸的  API，主要用于帮助开发者获取屏幕的宽和高的信息。Dimensions 的使用比较简单，只需要使用 get() 方法即可获取宽和高的信息。 

### 6.2.2. PixelRatio

​		在 React Native 开发中，PixelRatio 是一个用于获取设备像素密度的 API。所谓设备像素，指的是物理像素和设备独立像素之间的比值。

​		事实上，React Native 开发使用的尺寸单位是 pt，由于移动设备之间的像素密度不一样，所以 1pt 对应的像素数也是不一样的。因此，在实际开发中可以通过 PixelRatio 和 Dimensions 来计算屏幕的分辨率，公式如下。

```xml
屏幕分辨率=屏幕宽高×屏幕像素密度
```

​		在 React Native 中，使用 PixelRatio.get() 方法即可获取设备的像素密度。其中，常见设备的屏幕像素密度如下表所示。

![epub_31403502_62](https://res.weread.qq.com/wrepub/epub_31403502_62)

## 6.3. 动画 API

​		在移动应用开发中，流畅的动画是提升用户体验的重要手段。React Native 为开发者提供了简洁且强大的动画 API，这些 API 包括 requestAnimationFrame、 LayoutAnimation 和 Animated。

* requestAnimationFrame：帧动画，通过不断改变组件的状态来实现动画效果。
* LayoutAnimation：布局动画，当布局发生改变时的动画模块，允许在全局范围内创建和更新动画。
* Animated：最强大的动画 API，用于创建更精细的交互控制动画，例如实现组合动画。

### 6.3.1. requestAnimationFrame

​		requestAnimationFrame 是 React Native 提供的用于实现帧动画的 API，不过使用此 API 实现帧动画效果是非常简单粗暴的。

​		requestAnimationFrame 通过修改组件的 state 值来不断改变视图上的样式，从而实现动画效果。

​		requestAnimationFrame 通过不断修改组件的 state 值实现动画效果，但频繁地修改状态必然会导致组件频繁地销毁和重绘，导致内存开销大，引发性能问题，因此不建议使用。

### 6.3.2. LayoutAnimation

​		相比 requestAnimationFrame 动画 API，LayoutAnimation 的实现就要简单和智能得多。

​		LayoutAnimation 只有在组件的布局发生变化时，才会去执行视图的更新，因此 LayoutAnimation 又被称为布局动画。

​		使用 requestAnimationFrame 实现的动画改用 LayoutAnimation 实现的话，代码则会简洁许多，并且性能也更好。

​		使用 LayoutAnimation 动画 API 实现动画效果最简单的方法是调用 LayoutAnimation.configureNext()，然后再调用 setState() 来更新组件的属性值。

​		LayoutAnimation 被称为布局动画，通常被用在布局切换过程中。对于动画要求不是很高的场景，可以使用LayoutAnimation来实现。如果要实现精确的交互式组合动画，就需要使用另外一个动画 API，即 Animated。

### 6.3.3. Animated

​		相比 requestAnimationFrame 和 LayoutAnimation 两种动画来说，Animated 就要强大得多，Animated 被设计用来处理精确的交互式动画。借助 Animated，开发者可以很容易地实现各种复杂的动画和交互，并且具备极高的性能。

​		Animated 动画仅关注动画的输入、输出声明以及两者之间的可配置变换，然后使用 start()、stop() 方法来控制基于时间的动画执行，因此使用起来比较简单。

​		创建 Animated 动画最简单的方式就是创建一个 Animated.Value，并将它绑定到组件的一个或多个样式属性上，然后使用 Animated.timing() 方法驱动数据的变化，进而完成渐变动画效果。

​		除了 Animated.View 动画组件之外，目前官方支持 Animated 动画的组件还包括 Animated.ScrollView、 Animated.Image 和 Animated.Text。如果想要其他组件也支持 Animated 动画，可以使用 createAnimatedComponent() 进行封装。

​		除了 timing 动画，Animated 支持的动画类型还有 decay 和 spring。每种动画类型都提供了特定的函数曲线，用于控制动画值从初始值到最终值的变化过程。

* decay：衰减动画，以一个初始速度开始并且逐渐减慢停止。
* spring：弹跳动画，基于阻尼谐振动的弹性动画。
* timing：渐变动画，按照线性函数执行的动画。

​		在 Animated 动画 API 中，decay、spring 和 timing 是动画的核心，其他的复杂动画都可以使用这三种动画类型实现。

* static decay(value, config)
* static timing(value, config)
* static spring(value, config)

​		作为 Animated 动画的核心，decay、timing 和 spring 动画参数接收 value 和 config 两个参数。其中，value 表示动画的 x 轴或 y 轴的初始值，config 则表示动画的配置选项。

​		decay 用于定义一个衰减动画。

​		timing 用于定义线性渐变动画，Animated 动画的 Easing 模块定义了很多有用的渐变函数，开发者可以根据需要自行选择。

​		spring 则用于定义一个弹跳动画。

​		除了上面介绍的一些动画 API 之外，Animated 还支持复杂的组合动画，如常见的串行动画和并行动画。

​		Animated 可以通过以下方法将多个动画组合起来。

* parallel：并行执行。
* sequence：顺序执行。
* stagger：错峰执行，其实就是插入 delay 的 parrllel 动画。

​		delay，是组合动画之间的延迟方法，严格来讲不算是组合动画。

​		并行动画使用的是 parallel() 方法，该方法可以让多个动画并行执行。

​		除了上面介绍的一些常见的动画场景，Animated 还支持手势控制动画。手势控制动画使用的是 Animated.event，它支持将手势或其他事件直接绑定到动态值上。

## 6.4. 平台 API

### 6.4.1. BackHandler

​		BackHandler 是 React Native 在 0.44 版本发布的用于监听 Android 设备返回事件的 API，用以代替之前版本中的 BackAndroid。

​		BackHandler 的用法和 BackAndroid 类似，主要是使用 addEventListener() 方法添加事件监听和使用 removeEventListener() 方法移除事件监听，格式如下。

```react
BackHandler. addEventListener('hardwareBackPress', this.onBackPressed);

BackHandler. removeEventListener('hardwareBackPress', this.onBackPressed);
```

​		对于 Android 环境来说，如果要退出应用，还可以使用 BackHandler 提供的 exitApp() 方法。

### 6.4.2. PermissionsAndroid

​		PermissionsAndroid 是 React Native 为了适配 Android 6.0 及以上版本的动态权限问题而推出的 API，仅对 Android 平台有效。

​		众所周知，在 Android 6.0 版本以前，应用想要获取系统权限，只需要在应用的 Android Mainfest.xml 配置文件中声明即可。不过，在 Android 6.0 版本中，官方对权限系统做了重大升级，将所有权限分成了正常权限和危险权限，所有危险权限需要动态申请并得到用户的授权后才能使用。

​		为了方便开发者快速地适配 Android 6.0 及以上版本的动态权限，React Native 提供了如下方法。

* check()：检测用户是否授权过某个动态权限。
* request()：弹出提示框向用户请求某项动态权限。
* requestMultiple()：弹出提示框向用户请求多个动态权限。

​		由于适配 Android 6.0 动态权限需要原生 Android 平台的支持，因此使用 PermissionsAndroid 适配 Android 6.0 及以上版本的动态权限的第一步，就是在原生 Android 工程的 AndroidMainfest.xml 文件中添加需要申请的动态权限。

​		然后，使用 PermissionsAndroid 提供的 request() 方法申请权限。由于申请权限是一个异步的过程，所以申请权限的方法需要使用 async 关键字修饰。

​		动态权限的返回值会以常量的形式记录在 PermissionsAndroid.RESULTS 中，返回值的类型有 GRANTED、DENIED 和 NEVER_ASK_AGAIN 3 种类型。

* GRANTED：表示用户已授权。
* DENIED：表示用户已拒绝。
* NEVER_ASK_AGAIN：表示用户已拒绝，且不愿被再次询问。

​		如果想要检测用户是否授权了某个权限，可以使用 PermissionsAndroid 提供的 check() 方法。该方法会返回一个 Promise 对象，返回值是一个布尔变量，表示用户是否授权申请的权限。

### 6.4.3. AlertIOS

​		对于 iOS 开发者来说，React Native 官方提供的弹出对话框主要有两个，分别是 Alert 与 AlertIOS。前者可以在 Android 平台和 iOS 平台通用，后者只能适用于 iOS 平台。

​		除了可以实现提示对话框效果外，AlertIOS 还能显示一个带输入框的提示框。

​		使用 AlertIOS 开发提示弹框时，如果是提示对话框，可以使用 AlertIOS 的 alert() 方法，如果是输入框提示框，则可以使用 AlertIOS 的 prompt() 方法。

​		当然，除了使用官方提供的组件外，最常用的还是使用自定义组件，实现不同效果的弹对话框。

### 6.4.4. PushNotificationIOS

​		PushNotificationIOS 是 React Native 官方提供的本地推送通知 API，其作用类似于原生 iOS 系统的 NSNotification。借助此 API，开发者可以轻松地实现诸如权限控制以及修改应用图标角标数的任务。

​		默认情况下，React Native 项目的 iOS 工程是没有添加 PushNotificationIOS 库依赖的，所以在使用 PushNotificationIOS 开发通知推送功能之前，需要开发者手动导入并链接 PushNotificationIOS 的原生库，否则将无法进行后面的开发。导入并链接 PushNotificationIOS 原生库可以参考下面的步骤。

​		由于 PushNotificationIOS 只是针对 iOS 系统，如果要在 Android 系统中实现推送通知功能，那么就需要在 Android 原生端集成相应的推送 SDK。不过，在商业项目开发过程中，更多的时候是直接选择推送服务提供商，比如友盟、极光推送、Leancloud 和网易云信等。

## 6.5. 本章小结

​		在 React Native 开发中，API 是一个比较重要的内容。 API 赋予了开发人员在无须理解系统内部工作原理的前提下，就可以通过调用某个 API 实现某种特定功能的能力。

