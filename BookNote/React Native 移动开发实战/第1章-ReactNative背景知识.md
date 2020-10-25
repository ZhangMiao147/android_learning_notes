# 第 1 章 React Native 背景知识

## 前言

　　目前，比较流行的移动跨平台技术主要有两种：一种是基于 Web 浏览器的 Hybrid 技术方案，采用此种方案时只需要使用 HTML 及 JavaScript 进行开发，然后使用浏览器加载即可完成应用的跨平台；另一种则是通过在不同平台上运行某种语言的虚拟机来实现应用跨平台，此种方案也是移动跨平台技术的主流方案，主要技术有 Flutter、React Native 和 Weex。

　　React Native 抛弃了传统的浏览器加载的思路，转而采用曲线调用原生 API 的思路来实现渲染界面，从而获得媲美原生应用的体验。

　　当然，React Native 也并不是没有缺点，比较明显的缺点有首次加载慢、调试不友好等，不过这些问题都可以通过社区得到很好的解决。并且，官方正在对 React Native 进行大规模的重构和优化，相信在不久的将来，React Native 会更加完善。

## 1.1. React Native 的诞生与发展

　　在 React 框架的基础上，React Native 框架前台的 JavaScript 代码通过调用封装的 Android 和 iOS 原生平台的代码来实现界面的渲染操作，因而调用原生代码的 App 的性能远远优于使用 HTML 5 开发的 App 性能。

 　　React Native 使用流行的 JSX 语法来替代常规的 JavaScript 语法，提高了代码的可阅读性。JSX 是一种 XML 和 JavaScript 结合的扩展语法，因此对于熟悉 Web 前端开发的技术人员来说，只需很少的学习就可以上手移动应用开发。

　　React Native 框架的优势在于，只需要使用一套代码就可以覆盖多个移动平台，真正做到 “ Learn Once，Write Anywhere ”。React Native 框架底层使用的是 JavaScriptCore 引擎，基本上只需要更新一下 JavaScript 文件，即可完成整个 App 的更新操作，非常适合用来开发 App 的热更新功能。

## 1.2. 移动跨平台技术横评

### 1.2.1. 阿里巴巴 Weex

　　Weex 是由阿里巴巴技术团队研发的一套移动跨平台技术框架，初衷是解决移动开发过程中频繁发版和多端研发难题。使用 Weex 提供的跨平台技术，开发者可以很方便地使用 Web 技术来构建高性能、可扩展的原生级别的性能体验，并支持在 Android、iOS、YunOS 和 Web 等多平台上部署。

　　作为一个前端跨平台技术框架，Weex 建立了一套源码转换以及原生端与 JavaScript 通信的机制。Weex 框架表面上是一个前端框架，但实际上它串联了从本地开发、云端部署到资源分发的整个链路。

　　具体来说，在开发阶段编写一个 .we 文件，然后使用 Weex 提供的 weex-toolKit 转换工具将 .we 文件转换为 JS bundle，并将生成的 JS bundle 上传部署到云端，最后通过网络请求或预下发的方式加载至用户的移动客户端应用中。同时，集成了 Weex SDK 的客户端接收到 JS bundle 文件后门，调用本地的 JavaScript 引擎执行环境执行相应的 JS bundle，并将执行过程中产生的各种命令发送到原生端进行洁面渲染，这个工作流程如下图所示。

![](image/Weex工作流程示意图.jpeg)