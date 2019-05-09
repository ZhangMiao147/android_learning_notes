# 关于 Service 的常见面试题，大家一起来看呀

> 前言：本文中提到的面试题都是从网络上查询出来的，至于面试题的解答只是本人的一些看法，如果有解答不对的地方希望大家提出会留言，也欢迎留言没有提到的面试问题。

**问题 1 ：bindService 和 startService 生命周期有啥不同？ **

　　bindService 第一次调用时会回调 onCreate() 和 onBind() 方法，使用 unbindService 解绑服务时会回调 onUnbind() 和 onDestory() 方法；startService 第一次启动服务时会回调 onCreate() 和 onStartCommand() 方法，之后再调用 startService() 方法，会回调 onStartCommand() 方法，调用 stopService() 方法时，会回调 onDestory() 方法；如果 bindService 和 startService 同时使用时，第一此启动调用 onCreate() 、 onStartCommand() 和 onBind() 方法，onStartCommand() 和 onBind() 方法的顺序取决于 start 和 bind 的顺序，只有调用 stopService 和 unbindService 两个方法之后才会回调 onDestory() 方法，如果将 unBind() 方法的返回值设置为 true，在还没有调用stopService 之前，绑定服务解绑服务之后再次绑定服务会回调 onRebind() 方法。