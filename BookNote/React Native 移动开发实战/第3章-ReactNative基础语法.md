# 第 3 章 React Native 基础语法

## 3.1. JSX 语法

　　React Native 使用 JSX 语法来构建页面。JSX 并不是一门新的开发语言，而是 Facebook 技术团队提出的一种语法方案，即一种可以在 JavaScript 代码中使用 HTML 标签来编写 JavaScript 对象的语法糖，所以 JSX 本质上来说还是 JavaScript。

　　在 React 和 React Native 应用开发中，不一定非要使用 JSX，也可以使用 JavaScript 进行开发。不过，因为 JSX 在定义上类似 HTML 这种树形结构，所以使用 JSX 可以极大地提高阅读和开发效率，减少代码维护的成本。

　　在 React 开发中，React 的核心机制之一就是可以在内存中创建虚拟 DOM 元素，进而减少对实际 DOM 的操作从而提升性能，而使用 JSX 语法可以很方便地创建虚拟 DOM。

　　组件的 render() 方法主要用于页面的渲染操作，它返回的是一个视图（View）对象，之所以没有看到创建对象和设置属性的代码，是因为 JSX 提供的 JSXTransformer 可以帮助我们把代码中的 XML-Like 语法编辑转换成 JavaScript 代码。借助 JSX 语法，开发者不仅可以用它来创建视图对象、样式和布局，还可以用它构建视图的树形结构。并且，JSX 语法的可读性也非常好，非常适合前端页面开发。

## 3.2. 语法基础

　　作为 React 前端框架在原生移动平台的衍生生产物，React Native 目前支持 ES5 及以上版本，不过实际开发中使用最多的还是 ES6。

### 3.2.1. let 和 const 命令

