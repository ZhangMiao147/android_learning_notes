# CustomPoint 与 Canvas

对于一些复杂或不规则的 UI，可能无法通过组合其它组件的方式来实现，比如需要一个正六边形、一个渐变的圆形进度条、一个棋盘等。当然，有时候可以使用图片来实现，但在一些需要动态交互的场景静态图片也是实现不了的，比如要实现一个手写输入面板，这时，就需要来自己绘制 UI 外观。

几乎所有的 UI 系统都会提供一个自绘UI的接口，这个接口通常会提供一块2D画布`Canvas`，`Canvas`内部封装了一些基本绘制的API，开发者可以通过`Canvas`绘制各种自定义图形。在Flutter中，提供了一个`CustomPaint` 组件，它可以结合画笔`CustomPainter`来实现自定义图形绘制。

### [#](https://book.flutterchina.club/chapter10/custom_paint.html#custompaint)

## 参考资料

1.[CustomPoint 与 Canvas](https://book.flutterchina.club/chapter10/custom_paint.html)

