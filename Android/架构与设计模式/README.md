# 架构与设计模式

* [设计模式选择](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E9%80%89%E6%8B%A9.md)
* 架构

## MVVM

* MVVM

* [MVVM 设计模式与 ViewModel、LiveData](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/MVVM%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E4%B8%8EViewModel%E4%B8%8ELiveData.md)

* [MutableLiveData 详解](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/MutableLiveData%E8%AF%A6%E8%A7%A3.md)

* [LiveDataBus 使用](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/LiveDataBus%E4%BD%BF%E7%94%A8.md)

* ViewModel 生命周期

  https://blog.csdn.net/weixin_55362248/article/details/123641982

  * ViewModel 是如何感知到生命周期的

    创建 ViewModel 的时候会传递当前所在的 Activity，Activity 实现了 ViewMo delStoreOwner 接口，并实现了 getViewModelStore() 方法来得到 ViewModelStore。

    在 ComponentActivity 的构造方法中会通过 Lifecycler 来感知 Activity 的生命周期，当 Activity 处于销毁状态时，检查一个是否发生配置变更，如果未发生配置变更，则调用 clear() 方法来清除 ViewModelStore 及其保存的 ViewModel。

    ViewModel 是存储在 ViewModelStore 中的，当进入 Activity 时，会自动创建一个 ViewModelStore，当在 oncreate() 方法中调用 ViewModelProvider.get() 方法时，就会将创建的 ViewModel 存到该 ViewModelStore 中。通过 Lifecycle 来得知 Activity 的生命周期，当 Activity 处于销毁时，检查一下是否发生配置变更，如果未发生配置变更，则调用 clear() 方法来消除 ViewModelStore 及其保存的 ViewModel。

  * 为什么屏幕旋转后 ViewModel 数据可以继续留存

    当 Activity 处于销毁时，会去检查一下是否发生配置变更，如果未发生配置变更，则调用 clear() 方法来清除 ViewModelStore 及其保存的 ViewModel。

    当 Activity 因为配置更改（如：旋转屏幕）而被销毁重建时，系统会立即调用 onRetainNonConfigurationInstance() 方法。在 onRetainNonConfigurationinstance() 方法中会获取先前创建的非配置实例数据 NonConfigurationInstances，将 viewModelStore 保存在 NonConfigurationInstances 对象中。

    非配置实例 NonConfigurationces 是在 ActivityThread 的 ActivityClientRecord 所创建的，当 Activity 发生配置变更而重建时 ActivityClientRecord 是不受影响的，所以这样当屏幕旋转 Activity 重建（配置变更）时，显示 onRetainerNonConfigurationInstance() 方法会被调用返回一个包含当前 ViewModelStore 的非配置实例对象，然后后续通过 getLastNonConfigurationInstance() 方法来获取到该非配置实例，所以保存在其 ViewModelStore 中的 ViewModel 是不会被清除的。

  * 为什么要设计成配置变更后 ViewModel 依然存在

    ViewModel 类负责处理的事 MVVM 架构模式中 ViewModel 层的工作，所以 ViewModel 保留的是 UI 状态数据。当你屏幕旋转时，Activity 会发生重建工作，将我们的 xml 布局从 portrait 切换到 landscape，但其实布局中展示的数据是一样的。所以，完全不需要再去 Model 层重新获取一个数据，直接复用 ViewModel 中保留的数据即可，从而节省系统开销。

    另外，在 ViewModel 出现之前，当我们想要在 Activity 发生销毁重建时保留数据需要通过复写 onSaveInstanceState() 方法来实现，该方法通过 Bundle 来将数据序列化保存在磁盘中，实现步骤相对复杂且又又大小限制，相对比，ViewModel 将数据保存在内存中，读写速度更快，而且又没有大小限制，所以是一种很好的替代方案。

  * LiveData 

## MVI

* [MVI](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/MVI.md)

## 组件化

* [组件化](https://github.com/ZhangMiao147/android_learning_notes/blob/master/Android/%E6%9E%B6%E6%9E%84%E4%B8%8E%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/%E7%BB%84%E4%BB%B6%E5%8C%96/%E7%BB%84%E4%BB%B6%E5%8C%96.md)

