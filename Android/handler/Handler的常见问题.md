# Handler 的常见问题

> 前言：本文中提到的面试题都是从网络上查询出来的，至于面试题的解答只是本人的一些看法，如果有解答不对的地方希望大家提出会留言，也欢迎留言没有提到的面试问题。

**问题 1 ：主线程使用 Handler 的过程**
　　在主线程创建一个 Handler 对象，并重写 handleMessage() 方法。子线程需要进行更新 UI 的操作时，就创建一个 Message 对象，通过 handle 发送消息出去。之后消息被加入到 MessageQueue 队列中等待被处理，通过 Looper 对象从 MessageQueue 取出待处理的消息，最后分发到 handler 的 handlerMessage() 方法中。

**问题 2 ： AsyncTask 和 Handler + Thread 机制的区别**




**问题 3 ： Handler、Message、Looper 的原理**

**问题 4 ： Handler、Looper、MessageQueue、Thread、Message，每个类功能、关系？**

**问题 5 ： 主线程 looper 如果没有消息，就会阻塞在那，为什么不会 ANR？**

**问题 6 ： 主线程 Looper 一直循环查消息为何没卡主线程？**

