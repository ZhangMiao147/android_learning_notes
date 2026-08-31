# Compose 状态

## 1. 什么是 Compose 的状态？它和传统 View 体系的状态有什么区别？

Compose 中状态是驱动 UI 变化的数据，当状态改变时，Compose 会自动重组依赖该状态的 UI 部分。

传统 View 体系是命令式的，状态变化后需要手动调用 setxxx() 方法更新 UI；Compose 是声明式的，UI 是状态的函数（UI = f(state)），状态驱动 UI 自动更新。



## 2. Compose 中 State\<T> 和 MutableState\<T> 的区别是什么？如何创建一个可观察的状态？

State\<T> 是只读状态接口，仅暴露 value 获取方法；MutableState\<T> 继承 State\<T>，新增 value 设值方法，支持修改状态。

创建方式：

* 顶层函数 mutableStateOf(initialVlaue)，返回 MutableState\<T>。

* 搭配 remember 使用，避免重组时重复创建：val state = remember { mutableStateOf(initialVlaue) }。

## 3. remember 的作用是什么？它的生命周期是怎么样的？

* 作用：在 Composable 重组时保留对象的引用，避免每次重组都重新创建开销较大的对象（如 MutableState、List、Bitmap 等）。

* 生命周期：与所在的 Composable 重组作用域绑定，当 Composable 从 Composition 中移除（如被 if 条件剔除），remember 保存的对象会被销毁。

## 4. remember 和 remeberSaveable 的区别是什么？各自的适用场景？

* 核心区别：是否支持配置变更（如屏幕旋转、语言切换）时保存状态。

* remember：仅在重组时保留状态，配置变更时会丢失。适用于临时状态（如弹窗的显示隐藏、输入框的临时输入）。

* rememberSaveable：基于 remember 扩展，支持配置变更 + 进程被杀后恢复状态，底层通过 Bundle 序列化保存。适用于需要持久化的状态（如用户输入的表单内容、页面的选中状态）。

* 注意：rememberSaveable 支持的类型有限，自定义类型许通过 Parcelize 序列化或提供 Saver。

## 5. 什么是状态提升？它的核心原则和好处是什么？

* 定义：将和修改的状态从子 Composable 移到父 Composable 中，子组件通过参数接收状态 + 回调暴露事件，实现子组件无状态化。

* 核心原则：状态向下传递，事件向上传递（单向数据流）。

* 好处：
  
  * 子组件复用性更高（无状态组件可在多处复用）。
  
  * 状态集中管理，便于调试和维护。
  
  * 符合单一职责原则，子组件只负责 UI 渲染，父组件负责状态逻辑。

## 6. 什么是衍生状态？derivedStateOf 的作用是什么？

* 衍生状态：基于现有状态计算得到的新状态，例如从 List\<T> 中过滤出符合条件的子列表。

* derivedStateOf 作用：优化重组性能，只有当计算依赖的原始状态发生变化时，才能重新计算衍生状态，避免每次重组都执行计算逻辑。

* 适用场景：当计算逻辑较复杂，且依赖的状态不常变化时（如列表的筛选、排序结果）。

## 7. Compose 中状态变更为什么能触发 UI 重组？Snapshot State 的底层原理是什么？



## 8. remember 的 key 参数有什么作用？举例说明如何正确使用 key。



## 9. Compose 中如何处理异步状态？LaunchedEffect、produceState 分别适用于什么场景？



## 10. collectAsState 和 collectAsStateWithLifecycle 的区别是什么？在什么情况下需要使用后者？



## 11. 为什么不建议在 Composable 中直接创建 MutableState 而不使用 remeber？会有什么问题？



## 12. 如何实现一个可复用的带状态的按钮组件？要求支持点击计数、禁用状态，并遵循状态提升原则。



## 13. 在 Compose 中，如何使用 ViewModel 管理跨组件的状态？ViewModel 和 Compose 状态的关系是什么？



## 14. 如何优化 Compose 中因状态变更导致的不必要重组？列举至少 3 种优化手段。



## 15. Compose 中处理配置变更时，除了 rememberSaveable，还有哪些方案？各自的优缺点是什么？


