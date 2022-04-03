# 层叠布局 Stack、Position

层叠布局和 Web 中的绝对定位、Android 中的 Frame 布局是相似的，子组件可以根据距父容器四个角的位置来确定自身的位置。层叠布局允许子组件按照代码中声明的顺序堆叠起来。Flutter中使用 Stack 和 Positioned 这两个组件来配合实现绝对定位。Stack 允许子组件堆叠，而 Positioned 用于根据 Stack 的四个角来确定子组件的位置。

### Stack

```dart
Stack({
  this.alignment = AlignmentDirectional.topStart,
  this.textDirection,
  this.fit = StackFit.loose,
  this.clipBehavior = Clip.hardEdge,
  List<Widget> children = const <Widget>[],
})
```

- alignment：此参数决定如何去对齐没有定位（没有使用Positioned）或部分定位的子组件。所谓部分定位，在这里**特指没有在某一个轴上定位：**left、right 为横轴，top、bottom 为纵轴，只要包含某个轴上的一个定位属性就算在该轴上有定位。
- textDirection：和 Row、Wrap 的 textDirection 功能一样，都用于确定 alignment 对齐的参考系，即：textDirection 的值为TextDirection.ltr，则 alignment 的 start 代表左，end 代表右，即从左往右的顺序；textDirection 的值为 TextDirection.rtl，则alignment的 start 代表右，end 代表左，即从右往左的顺序。
- fit：此参数用于确定**没有定位**的子组件如何去适应 Stack 的大小。StackFit.loose 表示使用子组件的大小，StackFit.expand 表示扩伸到 Stack 的大小。
- clipBehavior：此属性决定对超出 Stack 显示空间的部分如何剪裁，Clip 枚举类中定义了剪裁的方式，Clip.hardEdge 表示直接剪裁，不应用抗锯齿。

### Positioned

```dart
const Positioned({
  Key? key,
  this.left, 
  this.top,
  this.right,
  this.bottom,
  this.width,
  this.height,
  required Widget child,
})
```

left、top 、right、 bottom 分别代表离 Stack 左、上、右、底四边的距离。width 和 height 用于指定需要定位元素的宽度和高度。注意，Positioned 的 width 、height 和其它地方的意义稍微有点区别，此处用于配合 left 、top 、right、 bottom来定位组件，举个例子，在水平方向时，只能指定left、right、width 三个属性中的两个，如指定 left 和 width 后，right 会自动算出(left+width)，如果同时指定三个属性则会报错，垂直方向同理。

## 参考资料

1.[层叠布局 Stack、Position](https://book.flutterchina.club/chapter4/stack.html)

