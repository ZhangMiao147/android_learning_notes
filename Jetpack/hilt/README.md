# Hilt 知识目录

* [Hilt]

疑问：

* 保证嵌套依赖注入的源码分析

* activity/fragment 获取 viewmodel 对象的原理
  
  ```kotlin
  private val satelliteDiscoverViewModel: SatelliteDiscoverViewModel by viewModels()
  private val offersViewModel: GetOffersViewModel by activityViewModels()
  ```

* Hilt 与 ViewModel @HiltViewModel 

* @Binds 和 @Provides

* 字节码插桩？
  
  * ARouter
  
  * 埋点如何使用字节码插桩
  
  * 无痕权限框架？
  
  * AGP Transform + ASM 分别是什么？

* Android Hilt 之一：什么是依赖注入（什么是依赖注入，使用依赖注入和不使用依赖注入的区分）

* Android Hilt 之二：Hilt 如何使用（Hilt 是干嘛的，dagger 与 hilt，hilt 大概怎么用，hilt 的一些常见注解、hilt 的简单使用）

* Android Hilt 之三：Hilt 在实际开发中的使用（SharedPreferences 的注入、多模块项目、ViewModel 注入、数据库注入）

* Android Hilt 之四：Hilt 源码分析

* ## 资料
1. [Android Hilt：强大的依赖注入框架，高级传参解个耦？一、Hilt 干依赖注入的 Hilt是干嘛的 Hilt，一 - 掘金](https://juejin.cn/post/7244722310203260985)
