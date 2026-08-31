# Jetpack Compose 手势之点击、拖动和滑动事件

Compose 在手势处理方面，为开发者提供了众多的 API，可以说比原生 View 的手势处理更为方便。

## 单击、双击和长按事件

```kotlin
@Composable
fun ClickGestureScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text(text = "点击事件", modifier = Modifier.clickable {
            Log.d(TAG, "ClickGestureScreen: click")
        })
    }
}

fun Modifier.clickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
)
```

单击事件可以由 Modifier.clickable() 方法提供，此方法有四个参数，其中只有 onClick 参数为必传，其余三个参数都是可以默认不传入，参数的介绍分别如下：

* enable：表示点击事件是否可用，默认为 true，可使用此参数控制点击事件是否响应；

* onClickLabel：此参数主要用于无障碍模式下播放的标签，一般情况下不会使用，如果应用需要适配无障碍模式，那么此参数就需要设置对应的语义；

* role：此参数也是用于无障碍模式，它充当了一种角色，Compose 默认提供了一系列角色比如：Button、CheckBox 等；

* onClick：此参数就是点击事件响应的回调，可在此 Lambda 内部进行点击事件的逻辑处理。

Compose 的点击事件中，默认会添加水波纹效果，如果不需要此效果，可以将 indication 设置为 null

```kotlin
@Composable
fun ClickGestureScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text(text = "去除点击事件的水波纹", modifier = Modifier.clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            Log.d(TAG, "ClickGestureScreen: click")
        })
    }
}
```

继续看一下长按和双击事件是如何处理的。

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoubleClickGestureScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text(text = "双击和长按事件", modifier = Modifier.combinedClickable(
            onLongClick = { Log.d(TAG, "DoubleClickGestureScreen: onLongClick") },
            onDoubleClick = { Log.d(TAG, "DoubleClickGestureScreen: onDoubleClick") },
            onClick = { Log.d(TAG, "DoubleClickGestureScreen: onClick") }
        ))
    }
}
```

长按和双击事件只是将 Modifier.clickable() 方法替换成 combinedClickable() 即可，combinedClickable 方法内部有三个 Lambda 参数，只需要在对应的参数中处理相应的逻辑即可，还是非常便捷的。

combinedClickable() 方法也是同样可以去除水波纹效果的，用法可点击事件一样。

### 拖动事件

在 Compose 中，拖动事件可以通过 Modifier.draggable() 方法提供，可以监听水平和垂直方向上拖动事件。

```kotlin
@Composable
fun DraggableGestureScreen() {

    var dragDistance by remember {
        mutableStateOf(0F)
    }
    val draggableState = rememberDraggableState(onDelta = {
        dragDistance += it
        Log.d(TAG, "DraggableGestureScreen onDelta: $it, dragDistance: $dragDistance")
    })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Text(
            text = "拖动", modifier = Modifier
                .draggable(
                    draggableState, Orientation.Vertical,
                    onDragStarted = { offset ->
                        Log.d(TAG, "DraggableGestureScreen start offset: $offset")
                    },
                    onDragStopped = { offset ->
                        Log.d(TAG, "DraggableGestureScreen end dragDistance: $offset")
                    }
                )
                .offset(y = dragDistance.dp)
        )
    }
}

fun Modifier.draggable(
    state: DraggableState,
    orientation: Orientation,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    startDragImmediately: Boolean = false,
    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
    onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
    reverseDirection: Boolean = false
)
```

draggable() 方法参数比较多，列举几个比较重要的参数做下解释：

* state：用于监听指定方向上手指拖动的偏移量；

* orientation：指定水平或者垂直方向；

* onDragStarted：拖动开始的回调，是一个挂起函数，内部的 startedPosition 表示手指落下的位置；

* onDragStopped：拖动停止的回调，也是一个挂起函数，内部的 velocity 表示停止时拖动的速度；

* reverseDirection：表示是否反转拖动反向，默认为 false。

通过 Modifier.draggable() 方法只能监听单一方向向上的拖动事件，除此之外还可以使用 Modifier.pointInput() 来同时监听水平和垂直方向上的拖动事件。

### 滑动事件

Compose 中滑动事件可以通过 Modifier.swipeable() 方法进行监听，但是 swipeable 目前之后 material 包下才有，material3 包下面还未提供。

滑动事件和拖动事件极为类似，都是在指定的方向上随着手指的移动而响应的事件，只是滑动事件比拖动事件多了个锚点的概念，它可以根据锚点的状态来设置阈值，事件会在对应的阈值范围内进行额外的滑动事件，比如说设置一个 Box 从上滑到下，并且阈值为 0.3，当手指从上滑到下总长的 70% 时停止，那么剩下的 30% 距离也会进行滑动事件。

```kotlin
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipGestureScreen() {

    val swipeableState = rememberSwipeableState(initialValue = 0)
    val boxSize = with(LocalDensity.current) { 50.dp.toPx() }
    val anchorMap = mapOf(0F to 0, boxSize to 1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Box(
            modifier = Modifier
                .size(50.dp, 100.dp)
                .background(color = Color.Blue)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, swipeableState.offset.value.toInt()) }
                    .swipeable(
                        state = swipeableState,
                        anchors = anchorMap,
                        orientation = Orientation.Vertical,
                        thresholds = { from, to ->
                            Log.d("SwipGestureScreen", "SwipGestureScreen from: $from, to: $to")
                            if (from == 1) {
                                FractionalThreshold(0.2F)
                            } else {
                                FractionalThreshold(0.4F)
                            }
                        }
                    )
                    .size(50.dp)
                    .background(color = Color.Red)
            )
        }
    }
}
```

swipeableState 是记录滑动事件的状态，可以从此 state 中获取滑动的偏移量；

anchorMap 是一个锚点集合，当偏移量为 0 时，锚点值设置为 0，当偏移量为 Box 大小时，此时锚点值设置为 1，后续可以根据锚点值进行对应的阈值设置；

Modifier.offset{ IntOffset(0, swipeableState.offset.value.toInt()) } 设置最小的 Box 随之滑动进行 Y 轴偏移；

Modifier.swipeable() 方法有四个比较重要的参数，分别作用如下：

* state：用于监听滑动事件的状态；

* anchors：配置滑动事件的锚点集合，也就是 anchorMap；

* orientation：配置滑动事件的方向，此外设置为垂直方向；

* thresholds：根据锚点值设置阈值，当从上到下滑动时，阈值设置为 0.4F，当从下到上滑动时，阈值设置为 0.2F。

实现效果，当手指从上到下滑动时，触发到 0.4F 阈值时，此时松开手指，红色的方块依旧会往下滑动；当从下往上滑动时只需要触发 0.2F 阈值时，手指松开方块也会继续往上滑动，这就是 swipe 比 drag 更美妙的地方。

可以通过 swipe 实现滑动关闭、折叠标题栏的各种炫酷的效果。

## 参考文章

[【Jetpack Compose】手势之点击、拖动和滑动事件 - 掘金](https://juejin.cn/post/7287409232186359823)
