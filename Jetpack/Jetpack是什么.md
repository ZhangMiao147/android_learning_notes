# Jetpack 是什么

在 Jetpack 的官方文档中是这样对它定义的：

Jetpack 是一套组件库，可帮助开发人员遵循最佳实践，减少样板代码并编写可在 Android 版本和设备上一致工作的代码，以便开发人员可以专注于他们关心的代码。

根据定义其实可以提炼出两个核心点：

1. 它是一套组件库。（说明它是由许多个不同的组件库构成，并不是一个单一的组件库）
2. 使用 Jetpack 可以帮助我们在不同的 Android 版本和不同的设备上，实现行为一致的动作代码。（说明 Jetpack 可以轻松的处理由 Android 版本不一致和设备不同产生的差异性和兼容性问题）

先来看看 Jetpack 包含哪些组件库

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zniaz404XZrnmT11hLjRPydy4RPOIOBbArrh1sLHRoVubjib6j5ic3FYnA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

根据官网上的介绍，目前 Jetpack 一共有 85 个组件库，有些看着很熟悉，比如：viewPager、fragment、recyclerView 等等，但有些好像根本就没有见过，也没有用过。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7z9DyreicWMibCgp7BpxPZMu1xHjSVPCEqy5yibtGagY8vhd6BTwQA6nibXg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7z4bbxOnWlwFKB7iaO3ic5jHvkNvXNibqjWVvDzC4rCLWch79luOgdyOb9g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

将 Jetpack 的 85 个组件库进行了分类和标签整理。

**第一个是核心类（8个）**，也可以把它理解为基础类，也就是说一个最基本的 Android 攻城都会默认依赖这些组件库。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zUkndeEbJaXajl4VZIPQJPzGTJX0nHS5zEpto6CvGAUibNdCZWQo0feA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第二个是架构组件（10个）**，Jetpack 推出之后很令人兴奋的一点，就是 Google 引入了现代 An droid 应用开发的架构指南，结合 MVVM 的架构设计，帮助我们轻松的处理 UI 与业务逻辑之间的关系。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zictatbXHAWps36OKeu1PuUkP2sWIDQZpicRwyYgU7vHe0ufpOvf3P75g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第三个是 UI 组件（22个）**，这里需要说明一下，大多数的 UI 组件其实都包含着核心组件中的 appcompat * 中，这里列出的是 Jetpack 中以独立组件库存在的 UI 组件。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zHvxZricKR2ErY2AmAKwj1x96JhxKFyeVE9rJZfN1NSYgTP2QJAyx6Hw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第四个是特殊业务组件（16个）**，根据不同的业务场景，选择性使用。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7z0h1PfsuPqyxljnnLsiaYxic0EQSxgumK7eED2r9KgJ1Gh5K01EznJNew/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第五个是用不着的组件（15个）**

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zwibmnZicx8PnRzWy3RUToU37iaqqSfATIaxL9fb1yPn80HFvPDArk7zfQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第六个是弃用的组件（11个）**，有一些事因为官方不再更新维护了，有一些是在 Jetpack 中有更好的替代解决方案，如果项目中还在使用这些组件库的话，建议尽快替换到最新的替代组件上。

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zgrhQBMg5IT2OlENZvhXicd6b6BzTUX93Po9rA4t2JQYmxXvZAMTzUzQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**第七个是用于测试的组件（2个）**

![](https://mmbiz.qpic.cn/mmbiz_png/f8uqictCHrGJBibua7eTFzswx7ECG1NO7zsWRNAP239LHkKCatSicol2pGIJIvxj5W5yNFXYWnJRGia4BlqlQgnA9w/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

Jetpack 是一套组件库，Jetpack 是由 85 个组件库构成的，每一个都可以根据自己的需求单独依赖使用，非常灵活和方便。

## 参考文章

1. [Jetpack 是什么？](https://mp.weixin.qq.com/s/BzwOF1d0lCvfrIhG2sDwZw)

