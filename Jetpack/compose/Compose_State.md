# Compose State

## 知识

**State**

声明式 UI 的工作流程有点像是刷新网页一样。即我们去描述一个控件时要附带上它的状态。然后当有任何状态需要发生变化时，只需要像刷新网页一样，让界面上的元素刷新一遍，那么自然状态就能得到更新了。

Compose 更新 UI 页面的核心逻辑在于刷新界面，这个概念在 Compose 中被称为重组。

State 是一种基于观察者机制的组件，它的用法和 LiveData 类似，因此非常的简单易懂。

State 可以让 Compose 感知到页面上有状态发生了变化，从而对界面上相关联的 Com posable 函数进行重组。不仅如此，State 还可以让 Compose 能够精准只更新那些状态有变化的控件，而那些状态没有变化的控件在重组的时候则会跳过执行。

由此可见，为了让 Compose 的界面可以交互，State 可以说是至关重要的组件。

mutableStateOf() 函数就是用于创建一个可变的 State 对象，参数中传入的是初始值。

如果想要访问这个 State 对象中的值，那么就调用它的 getValue() 函数，想要更新 State 对象中的值，就调用它的 setValue() 函数。

使用了 State 来追踪某个数据之后，当这个数据的值发生变化，Compose 就会自动触发重组来更新所有读取这个值的地方，从而界面就会得到更新了。

remember 和 mutableStateOf 在 Composable 函数中几乎永远都是配套使用的。

remember 函数的作用是让其包裹住的变量在重组的过程中得到保留，从而就不会出现变量被重新初始化的情况了。

使用 by 关键字替代了之前等号，用委托的方式来为 count 变量赋值。别看只是改变了一下赋值方式，count 变量的类型都会因此而发生变化。之前用等号赋值的时候，count 的类型是 MutableState，而改用 by 关键字赋值之后，count 的类型就变成了 Int。

**恢复 State 数据**

用于计数的 count 变量是定义在 Composable 函数中的，虽然用 remember 函数将它包裹住了，但这只能保证它在 Composable 函数重组的时候数据不会丢失。而手机横竖屏旋转会导致 Activity 重新创建，这个时候数据肯定是会丢失的。

rememberSaveable 函数是 remember 函数的一个增强版，它唯一和 remember 不同的地方就是在于其包裹的数据在数据横竖屏旋转时会被保留下来。

**状态提升**

根据 Google 给出的最佳时间准则，有状态的 Composable 函数通常在复用性和可测试性方面都会表现得比较差。

当编写 Composable 函数时，最理想的情况就是尽可能地让它成为一个无状态的 Composable 函数。怎么才能做到这一点呢？Compose 提供了一种编程模式，叫 State hoisting。也就是说，我们要尽可能地把 State 提到更上一层的 Composable 函数当中，这样偏低层的 Composable 函数就可以成为无状态的函数，从而提高它们的复用性。

而实现状态提升最核心的步骤只有两个：

第一就是将原来声明 State 对象的写法改成用参数传递的写法。

第二就是将写入 State 数据的地方改成用回调的方式来通知到上一层。

那么经过状态提升后的 Counter 函数到底好在哪里呢？有状态的 Composable 函数通常在复用性方面会存在一些问题。而现在 Counter 函数已经无状态了，它的复用性会更加出色。

通常意义上来讲，像这种状态向下传递、事件向上传递的编程模式，称之为单项数据流模式。而状态提升就是这种单向数据流模式在 Composable 中的具体应用。

以下是应该考虑的状态提升最少应该达到哪个层级的关键因素：

1. 如果有多个 Composable 函数需要读取同一个 State 对象，那么至少要将 State 提升到这些 Composable 函数共有的夫级函数当中。

2. 如果有多个 Composable 函数需要对同一个 State 对象进行写入，那么至少要将 State 提升到所有执行写入的 Composable 函数里调用层级最高的那一层。

3. 如果某个事件的触发会导致两个或更多的 State 发生变更，那么这些 State 都应该提升到相同的层级。

**结合ViewModel一起使用**

传统 LiveData 的用法在 Compose 中并不好使，因为传统 LiveData 依赖于监听某个值的变化，并对相应的界面进行更新，而 Compose 的界面更新则依赖于重组。

因此，需要将 LiveData 转换成 State 才行，observeAsState() 函数就是用来做这个事情的，参数中传入的 0 表示它的初始值。

StateFlow 本来和 LiveData 的用法极其相似，最大的不同点可能就在于 StateFlow 是用 Kotlin 编写的，它可以在初始化的时候就传入一个初始值，从而确保它的值永远不会为空。

## AI 问答

1. **为什么在最初的计数器示例中，直接修改 `count` 变量无法更新界面？**  
   （提示：重组机制、State 的作用）
   
   原因：
   
   * Compose 的核心机制是重组
   
   Compose 通过重组（Recomposition）来更新UI，而直接修改普通的 count 变量不会触发重组，因为 Compose 无法感知这种变量的变化。
   
   * State 的作用是建立观察者机制
     
     使用 mutableStateOf() 创建的 State 对象会：
     
     * 标记状态依赖关系：当 Text 控件读取 count.value 时，Compose 会记录这种依赖。
     
     * 自动触发重组：当 count.value 被修改时，Compose 会自动调度重组，更新所有依赖此状态的控件。
   
   * 初始代码的缺失环节
     
     即使改用 mutableStateOf，若被配合 remember，重组时 count 会被重新初始化为 0。因此完整解决方案需要：var count by remember {mutableStateOf(0)}

2. **`mutableStateOf` 和 `remember` 的作用分别是什么？为什么它们通常一起使用？**  
   （提示：状态跟踪、重组保留）
   
   * mutableStateOf 的作用
     
     * 用于创建一个可观察的 State 对象
     
     * 当 State 的值变化时，Compose 就会自动触发重组来更新所有读取这个值的地方，从而界面就会得到更新了。
   
   * remember 的作用
     
     * 防止变量在重组时被重新初始化。
   
   * 为什么必须组合使用
     
     * 单独使用 State：重组时 count 会被重新初始化为 0.
     
     * 单独使用 remember：无法触发界面更新（无观察者机制）。
     
     * 组合后：既保持状态持久化，又能触发 UI 更新。

3. **`remember` 和 `rememberSaveable` 的区别是什么？分别在什么场景下使用？**  
   （提示：横竖屏旋转、临时状态）
   
   区别：
   
   * 数据保留范围：
     
     * remember：仅在当前 Composable 函数重组期间保留状态数据（如界面刷新）。当 Activity/Fragment 因配置变更（如横竖屏旋转）重建时，数据会丢失。
     
     * rememberSaveable：通过 Android 的 SavedInstanceState 机制持久化数据，在配置变更时也能恢复数据（如横竖屏旋转、系统杀死进程后恢复）
   
   * 实现原理：
     
     * remember：基于 Compose 的重组机制，内存级缓存。
     
     * rememberSaveable：在 remember 基础上增加了 Bundle 序列化/反序列化逻辑（自动处理可序列化类型，复杂对象需实现 Parcelable）。
   
   * 使用场景对比
     
     | 场景              | remember | rememberSaveable |
     | --------------- | -------- | ---------------- |
     | 临时 UI 状态（如动画进度） | ✅        | ❌（过度设计）          |
     | 用于输入的表单数据       | ❌        | ✅（防止旋转丢失）        |
     | 屏幕旋转需保留的状态      | ❌        | ✅                |
     | 高频更新的状态（如计时器）   | ✅（更高效）   | ❌（序列化开销）         |
   
   * 注意事项
     
     * 性能考量：rememberSaveable 有序列化开销，非必要场景优先用 remember。
     
     * 数据类型限制：rememberSaveable 默认支持基本类型，复杂数据需实现 Parceable。
     
     * 需要跨 Activity 生命周期时，ViewModel 是更好的选择。

4. **以下代码的问题是什么？如何修复？**
   
   ```kotlin
   fun Counter() {
      var count = 0
      Button(onClick = { count++ }) {
          Text("Count: $count")
      }
   }
   ```
   
   * 问题：count 是局部变量，Compose 重组时会被重新初始化为 0。直接修改 count 不会触发重组，因此界面不会更新。（Compose 依赖 State 触发重组，普通变量无效）
   
   * 修复：使用 State 和 remember 进行修复：var count by remember{mutableState(0)}

5. **状态提升（State Hoisting）的目的是什么？如何将一个有状态的 `Counter` 函数改造成无状态函数？**  
   （提示：参数化状态、回调函数）
   
   * 状态提升的目的：
     
     * 提高复用性
       
       无状态组件不依赖内部 State，因此可以通过参数配置不同行为，复用性更强。
     
     * 遵循单向数据流
       
       状态提升是单向数据流在 Compose 中的具体应用，即状态向下传递、事件向上回调，使数据流向更清晰，便于维护。
     
     * 改善可测试性
       
       无状态组件仅依赖输入参数和回调，测试时无需模拟 State 内部逻辑。
   
   * 如何改造有状态的 Counter 函数
     
     改造无状态版本的步骤
     
     * 参数化状态：将 State 对象改为通过函数参数传入，移除内部 mutableStateOf
     
     * 用回调处理事件：将写入操作改为高阶函数回调，通知父组件
     
     * 父组件管理状态：状态提升到父组件中，通过 State 和回调控制逻辑。
   
   * 关键点
     
     * 状态提升最少层级规则：
       
       * 多个组件读取同一个 State -> 提升到它们的共同父组件
       
       * 多个组件写入同一个 State -> 提升到最顶层写入组件
       
       * 事件触发多 State 变更 -> 集中管理这些 State.

6. **为什么状态提升后的 `Counter` 函数复用性更好？结合“双倍计数器”的示例说明。**
   
   状态提升（State Hoisting）的核心思想是将状态管理与 UI 渲染分离。通过将状态和状态修改逻辑提升到父组件中，子组件仅负责 UI 展示和事件传递，从而实现以下优势：
   
   * 逻辑与 UI 解耦
     
     * 状态提升前，函数内部直接管理状态，导致其逻辑与 UI 强耦合，若有不同逻辑，可能需要重新整个函数。
     
     * 状态提升后，函数仅接收外部传入的参数和回调，不关心具体逻辑。UI 部分可完全复用，逻辑由父组件控制。
   
   * 灵活适配不同业务需求
     
     ```kotlin
     fun CallCounter() {
         var count by rememberSaveable { mutableStateOf(0) }
         var doubleCount by rememberSaveable { mutableStateOf(0) }
     
         Column {
             // 单倍计数器
             Counter(
                 count = count,
                 onIncrement = { count++ } // 逻辑由父组件定义
             )
             // 双倍计数器
             Counter(
                 count = doubleCount,
                 onIncrement = { doubleCount += 2 } // 不同逻辑复用同一UI
             )
         }
     }
     ```
     
     复用性体现：同一个 Counter 组件通过外部传入不同的 onIncrement 逻辑，实现了单倍递增和双倍递增两种功能，无需修改 Counter 内部代码。
   
   * 符合单向数据流原则
     
     * 状态提升强制遵循“状态向下传递，事件向上通知”的模式，使数据流动更清晰：
       
       * 父组件：负责状态管理和业务逻辑。
       
       * 子组件：纯粹的无状态UI，仅通过参数和回调与父组件交互。
     
     * 这种模式降低了组件间的耦合度，便于测试和维护。
   
   * 总结
     
     状态提升通过将状态管理职责上移，使函数成为纯 UI 组件，其复用性体现在：
     
     * 逻辑可定制化：同一 UI 可绑定不同的业务逻辑。
     
     * 代码更简洁：子组件无需重复编写状态管理代码。
     
     * 易于扩展：新增功能时只需在父组件中调整逻辑，无需修改子组件。
     
     这种设计模式是 Compose 中实现高效复用的关键实践，也是声明式 UI 框架的通用最佳实践。

7. **在 Compose 中，如何将 ViewModel 的 `LiveData` 或 `StateFlow` 转换为 Compose 可用的状态？分别用什么函数？**  
   （提示：`observeAsState`、`collectAsState`）
   * LiveData 转换
     
     使用 observeAsState() 函数将 LiveData 转换为 Compose 可用的状态：
     
     ```kotlin
     val count by viewModel.count.observeAsState(initial = 0)
     ```
     
     底层原理：自动监听 LiveData 的变化并触发重组，无需手动观察。
   
   * StateFlow 转换
     
     使用 collectAsState() 函数将 StateFlow 转换为 Compose 可用的状态：
     
     ```kotlin
     发了val count by viewModel.count.collectAsState()
     ```
     
     自动订阅 Flow 并处理生命周期，避免内存泄漏。

8. **为什么在 TextField 示例中，直接写 `value = ""` 无法显示输入内容？如何通过 State 解决这个问题？**  
   （提示：状态驱动 UI、单向数据流）
   * 问题分析
     
     * TextField 的显示内容属于 UI 状态，Compose 遵循声明式 UI 原则，必须通过状态变化驱动界面重组才能更新显示。
     
     * 直接写 value="" 时，这是一个固定值，没有状态跟踪机制，用于输入时无法触发重组（recomposition），即使重组，value 始终为空字符串，导致输入无法显示。
   
   * 解决方案
     
     ```kotlin
     fun MyTextField() {
         // 1. 创建可记忆的状态对象
         var text by remember { mutableStateOf("") }
     
         // 2. 建立双向绑定
         TextField(
             value = text,  // 显示状态值
             onValueChange = { newText -> 
                 text = newText  // 状态更新触发重组
             }
         )
     }
     ```

9. **Compose 的“单向数据流”模式是什么？结合状态提升和回调函数解释。**
   
   在 Jetpack Compose 中，“单向数据流” 模式是一种核心的架构模式，它通过状态向下传递、事件向上传递的机制来实现数据流动。这种模式与状态提升密切相关，并通过回调函数来协调状态更新。
   * 单项数据流的定义
     
     单项数据流是指：
     
     * 数据（状态）的流动方向：从父组件（或高层组件）向下传递给子组件（通过参数传递）。
     
     * 事件的流向方向：子组件通过回调函数将用户交互事件向上通知父组件，由父组件决定如何更新状态。
     
     这种模式确保了状态的唯一来源，避免了子组件直接修改状态导致的混乱。
   
   * 状态提升（State Hoisting）
     
     状态提升是单向数据流在 Compose 中的具体实现方式，其核心思想是：
     
     * 将状态从子组件移动到父组件：子组件不再自己持有状态，而是通过参数接收状态。
     
     * 通过回调函数通知状态变更：子组件通过父组件传递的回调函数触发状态更新。
   
   * 回调函数的作用
     
     回调函数是单向数据流中事件向上传递的关键工具：
     
     * 子组件通过回调将事件传递给父组件。
     
     * 父组件根据事件决定如何更新状态。
   
   * 为什么需要单向数据流
     
     * 可维护性：状态集中在父组件，逻辑更清晰
     
     * 可测试性：无状态组件更容易独立测试
     
     * 复用性：子组件不依赖具体业务逻辑，可被多种场景复用。
   
   * 与 ViewModel 的结合
     
     单项数据流与 ViewModel 天然契合：
     
     * ViewModel 作为状态源：管理业务逻辑和状态
     
     * Compose 通过 observeAsState 或 collectAsState 监听状态。
   
   * 总结
     
     | 概念    | 在 Compose 中的实现                 | 作用              |
     | ----- | ------------------------------ | --------------- |
     | 单向数据流 | 状态向下传递+事件向上传递                  | 确保数据流动的可预测性和一致性 |
     | 状态提升  | 将状态移到父组件，子组件通过参数接收状态           | 提高组件的复用性和可测试性   |
     | 回调函数  | onClick、onValueChange 等 Lambda | 子组件通知父组件更新状态的桥  |
     
     通过这种模式，Compose 实现了高效、可维护的 UI 开发方式。

10. **Google 推荐尽量使用无状态 Composable 函数的原因是什么？**  
    （提示：复用性、可测试性、职责分离）
    * 复用性
      
      通过状态提升（State hoisting）模式，将状态管理移到上层函数后，底层 Composable 函数可以完全通过参数控制行为。
    
    * 可测试性
      
      无状态函数的所有依赖都通过参数显式传入，测试时无需模拟复杂的状态环境。
    
    * 职责分离
      
      状态管理与 UI 渲染分离，符合单向数据流（Unidirectional Data Flow）原则。这本质上是将状态管理的职责与 UI 展示的职责解耦。

11. **在状态提升时，如何决定状态应该提升到哪一层级？**  
    （提示：共享状态、写入操作、事件触发多状态变更）
    * 状态提升核心原则：
      
      * 共享状态的读取需求
        
        如果有多个 Composable 函数需要读取同一个 State 对象，那么至少要将 State 提升到这些 Composable 函数共有的父级函数当中。
      
      * 共享状态的写入需求
        
        如果有多个 Composable 函数需要对同一个 State 对象进行写入，那么至少要将 State 提升到所有执行写入的 Composable 函数里调用层最高的那一层。
      
      * 事件触发多状态变更
        
        如果某个事件的触发会导致两个或更多的 State 发生变更，那么这些 State 都应该提升到相同的层级。
    
    * 目标：通过状态提升实现单向数据流，确保状态可维护且Composable 函数无状态化。

## 参考文章

[写给初学者的Jetpack Compose教程，使用State让界面动起来](https://guolin.blog.csdn.net/article/details/133970363?spm=1001.2014.3001.5502)
