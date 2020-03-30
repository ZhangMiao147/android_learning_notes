# RxJava 2 源码分析

　　RxJava 2.x 版本相较于 1.x 版本的使用类似，但又有稍许变化，本篇只着重讲解不同的部分。



## Observer 接口

　　多了一个 `void onSubscribe(Disposable d);` 方法，用于观察者取消事件订阅。

　　查看 Disposable接口组成（注意：2.x 版本新增的 Disposable可以做到切断订阅事件的操作，让观察者 Observer 不再接收上游事件，避免内存泄漏）：

```java
/**
 * Represents a disposable resource.
 */
public interface Disposable {
    /**
     * Dispose the resource, the operation should be idempotent.
     */
    void dispose();

    /**
     * Returns true if this resource has been disposed.
     * @return true if this resource has been disposed
     */
    boolean isDisposed();
}
```

　　接口中两个方法，一个 dispose 方法，另一个是检测是否 dispose 方法，其结构与 Subscription 类似。








## 参考文章
[浅析RxJava 1.x&2.x版本使用区别及原理（一）：Observable、Flowable等基本元素源码解析](https://blog.csdn.net/itermeng/article/details/80139074)

[友好 RxJava2.x 源码解析（一）基本订阅流程](https://juejin.im/post/5a209c876fb9a0452577e830)

[友好 RxJava2.x 源码解析（二）线程切换](https://juejin.im/post/5a248206f265da432153ddbc)

[友好 RxJava2.x 源码解析（三）zip 源码分析](https://juejin.im/post/5ac16a2d6fb9a028b617a82a)