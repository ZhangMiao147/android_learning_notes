# ConstraintLayout 的 helper

![](image/ConstraintLayout.png)

> 功能增强组件：
> 
> 1. MotionLayout
> 
> 2. ConstraintLayoutStates
> 
> 3. SharedValues

## 一、功能增强组件

# 

## 四、ConstraintProperties(流式API)

2.0 提供了 ConstraintProperties 可以使用流式 API 修改属性

```kotlin
val properties = ConstraintProperties(findViewById(R.id.image))
    properties.translationZ(32f)
          .margin(ConstraintSet.START, 43)
          .apply()
```

# 参考文章

1. [ConstraintLayout 用法全解析](jianshu.com/p/502127a493fb)
2. [史上最全ConstraintLayout使用详解！（建议收藏）](https://mp.weixin.qq.com/s/HDbPU-fej0L_YtMk41zeYg)
