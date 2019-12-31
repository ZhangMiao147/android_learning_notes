# ContentProvider 的常见问题

**问题 1 ** contentprovider 怎么升级维护 

​		在 ContentProvider 的 onCreate() 方法中处理升级。

**问题 2 **：contentprovider 已经是进程间通信，为什么还要引入 broadcasereceiver？ 

　　broadcastReceiver 主要功能是接收消息，而目标不是为了进行数据的传递，而 ContentProvider 主要功能是分享数据。