# getDimension()、getDimensionPixelOffset() 和 getDimensionPixelSize() 区别

在自定义控件中使用自定义属性时，经常需要使用 java 代码获取在 xml 中定义的尺寸，相关有以下三个函数：

* getDimension()
* getDimensionPixelOffset()
* getDimensionPixelSize()

在类 TypedArray 和类 Resources 中都有这三个函数，功能类似，TypeArray 中的函数是获取自定义属性的，Resources 中的函数是获取 android 预置属性的。

API reference 里的解释：

* getDimension() 是基于当前 DisplayMetrics 进行转换，获取指定资源 id 对应的尺寸。文档里并没说这里返回的就是像素，要注意这个函数的返回值是 float，像素肯定是 int。
* getDimensionPixelSize() 与 getDimension() 功能类似，不同的是将结果转换为 int，并且小数部分四舍五入。
* getDimensionPixelOffset() 与 getDimension() 功能类似，不同的是将结果转换为 int，并且偏移转换（offset conversion，函数命名中的 offset 是这个意思）是直接截断小数位，即取整（其实就是把 float 强制转换为 int，注意不是四舍五入哦）。

由此可见，这三个函数返回的都是绝对尺寸，而不是相对尺寸（dp/sp等）。如果 getDimension() 返回结果是 20.5f，那么 getDimensionPixelSize() 返回结果就是 21，getDimensionPixelOffset() 返回结果就是 20。

# 参考文章

1. [【android】getDimension()、getDimensionPixelOffset()和getDimensionPixelSize()区别详解](https://www.cnblogs.com/ldq2016/p/6834959.html)

