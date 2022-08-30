# 【Android 逆向】类加载器 ClassLoader ( 类加载时机 | 隐式加载 | 显示加载 | 类加载步骤 | 装载 | 链接 | 初始化 )

## 类加载时机

### 隐式加载

类加载的隐式加载，没有明确的说明加载某个类，但是进行了如下操作：

* 使用 new 关键字直接创建类的实例对象；
* 使用 new 关键字直接创建类的子类的实例对象
* 通过反射方式创建 类/子类 实例对象
* 访问类的静态变量，对静态变量进行读或者写操作都会出大隐式加载；
* 访问类的静态函数；

### 显式加载

显式加载：明确的说明要加载某个类。

* 使用 Class.forName() 加载指定的类；
* 使用 ClassLoader.loadClass 加载指令的类。

## 类加载步骤

类加载步骤：

* 装载：加载某个类时，首先要查找并导入 class 字节码文件。
* 链接：该阶段，可以分为以下 3 个子阶段：
  * 验证：验证字节码文件是否正确
  * 准备：为静态变量划分内存空间，并进行默认值赋值
  * 解析：将“常量池”中的“符号引用”转为“直接引用”
* 初始化：调用 clinit 函数，初始化静态变量、静态代码块。

![](https://img-blog.csdnimg.cn/ea081467c8c345c6b170af7999f2b983.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Z-p5puZ5Lqu,size_20,color_FFFFFF,t_70,g_se,x_16)

## 参考文章

1. [【Android 逆向】类加载器 ClassLoader ( 类加载时机 | 隐式加载 | 显示加载 | 类加载步骤 | 装载 | 链接 | 初始化 )](https://hanshuliang.blog.csdn.net/article/details/121758853)

