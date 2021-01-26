# 第 7 章 React Native 开发进阶

## 7.1. 组件生命周期详解

### 7.1.1. 组件生命周期基础知识

​		 组件，又名控件，是一段独立可复用的代码。在React Native 应用开发中，组件是页面最基本的组成部分。

​		和 React 的组件一样，React Native 的组件也有自己的生命周期。在 React Native 应用开发中，组件的生命周期指组件初始化并挂载到虚拟 DOM 为起始，到组件从虚拟 DOM 卸载为终结的整个过程，整个生命周期如下图所示。

![React Native组件生命周期示意图](https://res.weread.qq.com/wrepub/epub_31403502_73)

​		如图可知，React Native 组件的生命周期大体可以分为 3 个阶段，即挂载（mounting）、更新（updating）和卸载（unmounting）。其中，挂载和更新阶段都会调用 render() 方法绘制视图。组件的每个生命周期阶段都提供了一些方法供开发者调用，以实现相应的需求和功能。

​		挂载阶段指的是从组件的实例被创建到将其插入 DOM 的过程。

​		挂载阶段涉及的生命周期方法如下。

* defaultProps()：此阶段主要用于初始化一些默认属性，在 ES6 语法中，则统一使用 static 成员来定义。
* constructor()：此方法是组件的构造方法，可以在此阶段对组件的一些状态进行初始化。不同于 defaultProps()，此方法定义的变量可以通过 this.setState 进行修改。
* componentWillMount()：在挂载前被立即调用。它在 render() 方法之前被执行，因此在此方法中设置 state 不会导致重新渲染。
* render()：此方法主要用于渲染组件，返回 JSX 或其他组件构成 DOM。同时，此方法应尽量保持纯净，只渲染组件不修改状态。
* componentDidMount()：此方法在挂载结束之后立即调用，即在 render() 方法后被执行。开发者可以在此方法中获取元素或者子组件，也可以在此方法中执行网络请求操作。

​		当组件经过初始化阶段之后，应用程序就正常运行起来了，此时应用程序进入了运行阶段。运行阶段有个明显的特征，就是不论修改 props 还是 state，系统都会调用 shouldComponentUpdate() 方法来判断视图是否需要渲染。如果不需要，则不执行渲染，如果需要重新渲染，则调用 render() 方法执行视图的重绘。并且 props 的改变会比 state 的改变多一个步骤，props 会先调用 componentWillReceiveProps() 方法接收 props 后，再判断是否需要执行更新操作。运行阶段涉及的组件的生命周期函数如下。

* componentWillReceiveProps()：在挂载的组件接收到新的 props 时被调用。它接收一个 Object 类型的参数 nextProps，然后调用 this.setState() 来更新组件的状态。
* shouldComponentUpdate()：当组件接收到新的 props 或 state 时此方法就会被调用。此方法默认返回 true，用来保证数据变化时组件能够重新渲染。当然，开发者也可以重载此方法来决定组件是否需要执行重新渲染。
* componentWillUpdate()：如果 shouldComponentUpdate() 方法返回为 true，则此方法会在组件重新渲染前被调用。
* componentDidUpdate()：在组件重新渲染完成后被调用，可以在此函数中得到渲染完成之后的通知。

​		销毁阶段又名卸载阶段，主要指组件从挂载阶段到将其从 DOM 中删除的过程，是组件生命周期的终点。

​		除了正常移除组件外，组件的销毁还可能是由其他情况引起的，如系统遇到错误崩溃、系统内存空间不足，以及用户退出应用等。销毁阶段涉及的组件的生命周期函数如下。

* componentWillUnmount()：在组件卸载和销毁之前被立即调用。可以在此方法中执行必要的清理工作，如关掉计时器、取消网络请求和清除创建的 DOM 元素等。

​		在组件的整个生命周期中，每一个生命周期函数并不是只被调用一次，有的生命周期函数在整个生命周期阶段可能被调用多次，具体参考下表。

![epub_31403502_74](https://res.weread.qq.com/wrepub/epub_31403502_74)

### 7.1.2. 虚拟 DOM

​		众所周知，React 中的组件并不是真实的 DOM 节点，而是存在于内存之中的一种数据结构，叫作虚拟 DOM。只有当它被插入文档以后，才会变成真实的 DOM，其模型如下图所示。

![epub_31403502_75](https://res.weread.qq.com/wrepub/epub_31403502_75)

​		根据 React 的设计，所有的 DOM 变动都需要先反映在虚拟 DOM 上，再将实际发生变动的部分反映在真实 DOM 上，而这一过程的核心就是 DOM diff 算法。它可以减少不必要的 DOM 渲染，极大地提高组件的渲染性能。

​		在 React 开发中，直接操作 DOM 通常是很慢的，而 JavaScript 对象操作却很快。之所以比较快，是因为使用 JavaScript 对象可以很容易地表示 DOM 节点。DOM 节点通常由标签、属性和子节点组成。

​		虚拟 DOM 树转换为真实 DOM 采用的是深度优先遍历（DFS）技术。

### 7.1.3. 虚拟 DOM 与生命周期

​		在 React 中，组件的每个生命周期阶段都和虚拟 DOM 息息相关，因此可以根据生命周期函数来执行不同的 DOM 操作。组件生命周期和虚拟 DOM 操作的对应关系如下表所示。

![虚拟DOM与组件生命周期关系表](https://res.weread.qq.com/wrepub/epub_31403502_76)

## 7.2. 状态管理

​		众所周知，React 把组件视为一个简单的状态机，通过与用户的交互实现不同的状态渲染视图，最终驱动界面与数据保持一致。对于小型应用来说，使用系统提供的状态管理机制即可轻松完成组件的状态管理；但是对于复杂的中大型应用，特别是涉及跨模块通信时，借助一些流行的状态管理库来管理应用内组件的状态就显得很有必要。

### 7.2.1. Flux

​		Flux 是 Facebook 技术团队开发的用于构建客户端 Web 应用程序的系统架构。它利用单向数据流，为 React 的可复用视图组件提供了补充。准确地说，Flux 更像是一种模式，而不是一个框架。

​		一个使用 Flux 构建的应用主要由 3 大部分构成：Dispatcher、Store 和 View。如果细分也可以分为 4 部分，即Action、Dispatcher、Store 和 View，各部分相互独立、互不干扰。

* Action：视图层发出的消息或动作，比如 mouseClick。
* Dispatcher：应用派发器，接收 Action、执行回调函数。
* Store：应用数据层，用来存放应用的状态，一旦发生变动就提醒视图层执行页面更新。
* View：视图层。

​		作为一个致力于解决前端应用状态管理的框架，单向数据流是 Flux 框架的核心，如下图所示，是 Flux 状态管理框架的运作流程示意图。

![Flux运作流程示意图](https://res.weread.qq.com/wrepub/epub_31403502_78)

​		在 Flux 框架中，与用户直接打交道的是 View（视图）层，用户通过视图发出具体的 Action（动作），Dispatcher（派发）在收到动作事件后，会请求 Store（存储）执行存储和更新操作，当存储完成更新后会发出一个 change 事件触发视图的刷新，整个运作流程如下图所示。

![Flux完整运作流程示意图](https://res.weread.qq.com/wrepub/epub_31403502_79)

​		由于 Flux 的数据流是单向的，所以不存在 MVC 模式的双向绑定操作。同时，应用的不同部分保持解耦，只在存储时才会发生依赖，因此各部分能够保持严格的层次关系。

​		前面说过，作为一个 Web 应用状态管理框架，Flux 应用主要由 Action、Dispatcher、Store 和 View 4 个部分组成。

​		其中，View 作为 React 的视图组件，包含了所有组件的状态，并可以通过 props 将状态传递给子组件。

​		Action 用于处理视图层发出的消息或动作，每个 Action 都是一个单纯的对象，它由 actionType 和属性构成。另外，Action 还包含一个 Action Creator 和一些辅助函数。

​		除了用于创建动作外，Action 还会把动作传给 Dispatcher，并调用 Dispatcher 的 dispatch() 方法把动作派发给 Store。

​		在 Flux 框架中，最核心的就是 Dispatcher。Dispatcher 的作用就是将动作派发给 Store。每个 Store 都需要在 Dispatcher 中注册它自己，并提供一个回调函数，当有具体的动作发生时， Dispatcher 就会调用回调函数通知 Store 执行存储和更新。

​		需要注意的是，一个应用中只能有一个 Dispatcher 且是全局的。

​		当然，如果要将 Action 派发给 Store，还需要在 Dispatcher 中使用 register() 方法来登记各种 Action 回调函数，

​		在 Flux 中，Store 负责保存整个应用的 state 状态，其作用类似于 MVC 架构中的 Model 模块。作为 Flux 框架的基础组成部分，Store 提供了一些基础的方法和属性来帮助开发者对状态进行操作。

​		Store 负责保存整个应用的状态，其作用类似于 MVC 架构的Model。为了方便操作 Store，官方提供了工具类——  flux/utils。

### 7.2.2. Redux

​		随着 Web 应用单页面的需求越来越复杂，应用状态的管理也变得越来越混乱，如何保持多个组件之间状态的一致性成为前端开发人员迫切需要解决的问题，而 Redux 正是为解决这一复杂问题而存在的。

​		作为一个应用状态管理框架，Redux 和 Flux 有很多相似的地方。不同之处在于，Flux 可以有多个改变应用状态的 Store，并可以通过事件来触发状态的变化，组件可以通过订阅这些事件来和当前状态保持同步。另一方面，Redux 没有 Dispatcher（分发器）的概念，而在 Flux 框架中 Dispatcher 则被用来传递数据到注册的回调事件中。

​		和 Flux 框架管理应用状态的方式不同，Redux 使用一个单独的常量状态树来保存整个应用的状态，并且这个对象是不能直接被改变的。如果某些数据发生改变，那么就会创建出一个新的对象。由于 Redux 是在 Flux 的基础上扩展出的一种单向数据流实现，所以数据的流向、变化都能得到清晰的控制，并且能很好地划分业务逻辑和视图逻辑。

​		如下图所示，Redux 简化了 Flux 的状态管理流程，使得数据的流向和变化都得到精确的控制。

![Redux工作流程示意图](https://res.weread.qq.com/wrepub/epub_31403502_80)

​		Redux 框架主要由 Action、Store 和 Reducer 组成。其中，Action 表示用户触发的事件，Store 用于存放应用的状态，Reducer 则是表示获取应用当前状态和事件并产生新状态的过程。

​		在 Redux 框架中，Action 是一个普通的 JavaScript 对象，它的 type 属性是必需的，用来表示 Action 的名称。type 一般被定义为普通的字符串常量。

​		在 Redux 状态管理框架中，状态的变化通常会导致视图的变化，而状态的改变通常是通过接触视图来触发的，即根据视图产生的动作的不同，产生的状态结果也会不同。

​		当 Store 接收到动作以后，必须返回一个新的状态才能触发视图的变化，状态计算的过程即被称为 Reducer。

​		Reducer 本质上是一个函数，它接收动作和当前状态作为参数，并返回一个新的状态。

​		需要说明的是，Reducer 并不能直接改变状态，必须返回一个全新的状态对象。同时，为了保持 Reducer 函数的纯净，请不要在 Reducer 中执行如下操作。

* 修改传入参数。
* 执行有副作用的操作，如 API 请求和路由跳转。
* 调用非纯函数，如 Date.now() 或 Math.random()。

​		在使用 Redux 进行状态管理时，对于 Reducer 来说，整个应用的初始状态就可以直接作为应用状态的默认值。

​		在实际使用过程中，Reducer 函数并不需要像上面那样手动调用，因为 Store 的 store.dispatch() 方法会自动触发 Reducer 的执行。因此，只需要在生成 store 的时候将 reducer 传入 createStore() 方法即可。

​		和 Flux 框架中 Store 的作用一样，Redux 的 Store 主要用于保存应用程序的状态。可以把它看成一个容器，整个应用中只能有一个 Store，同时 Store 还具有将 Action 和 Reducer 联系在一起的作用。

​		在 Redux 框架中，创建 Store 是一件非常容易的事情，可以直接使用 Redux 提供的 createStore 函数来创建一个新的 Store。

​		通常，Store 对象包含了所有可能的数据，如果想要获取应用程序某个时刻的数据，可以使用 state 的 getState() 方法进行获取。

​		为了让应用的状态管理不再错综复杂，使用 Redux 时应遵循三大基本原则，否则将出现难以察觉的问题。

* 单一数据源：整个应用的状态被存储在一个状态树中，且只存在于唯一的 Store 中。
* state 是只读的：对于 Redux 来说，任何时候都不能直接修改状态，唯一改变状态的方法就是通过触发动作来间接修改。
* 应用状态的改变是通过纯函数来完成的：Redux 使用纯函数方式来执行状态的修改，Action 表明了修改状态值的意图，而真正执行状态修改的则是 Reducer。并且 Reducer 必须是一个纯函数，当 Reducer 接收到动作时，动作并不能直接修改状态的值，而是通过创建一个新的状态对象来返回修改的状态。

### 7.2.3. Mobx

​		MobX 是由 Mendix、Coinbase 和 Facebook 开源的状态管理框架，它通过响应式函数编程来实现状态的存储和管理。受到面向对象编程和响应式编程的影响，MobX 将状态包装成可观察的对象，通过观察和修改对象的状态进而实现视图的更新，其工作流程如下图所示。

![MobX工作流程示意图](https://res.weread.qq.com/wrepub/epub_31403502_81)

​		与 React 关注的是应用状态转换为可渲染组件树不同，MobX 更多的是关注应用的状态管理，因此，React 和 MobX 可以说是一对强有力的组合。

​		为了更好地理解 MobX 框架运作的原理和工作流程，需要重点理解几个与 MobX 相关的重要概念，即 State、Derivation 和 Action。

* State：状态，即驱动应用的数据，包括服务器端获取的数据以及本地组件状态的数据。
* Derivation：任何源自状态并且不会再有任何进一步的相互作用的东西就是衍生。衍生包括多种类型：用户界面、衍生数据和后端集成。MobX支持两种类型的衍生，即 Computed values 和 Reactions。其中，计算属性是使用纯函数从当前可观察状态中衍生出的值，Reactions 则是根据状态改变触发的结果。
* Action 是一段可以改变状态的代码，可以是用户事件、后端推送数据和预定事件等。MobX 框架支持显式地定义动作，以便使代码的组织结构更加清晰。

​		作为一个状态管理工具，MobX 支持单向数据流，也就是说动作改变状态，而状态的改变会触发视图的改变。为了方便管理状态，MobX 提供了很多实用的标签，常见的有 @observable、@observer、@action 和 @inject。

​		其中，@observable 标签用于标识要监控的数据，@observer 标签用于标识数据变化时要更新的组件类，@action 标签用于标识数据改变时的方法，@inject 标签则用于在组件类中注入 Store 对象，以便组件从 state 中获取 Store 对象数据。

​		事实上，MobX 框架的使用非常简单，使用时主要分为 3 个阶段：定义状态使其可观察，创建视图以响应状态的变化，以及更改状态并执行更新。

​		首先，定义一个数据结构来存储数据的状态，此数据结构可以是数字、字符串和对象等基本类型，然后使用 observable 标签标识以便让数据变得可观察。

​		在 MobX 框架中，被 observer 修饰的组件，会根据组件内被 observable 修饰的状态的变化而自动执行重绘。通常来说，任何函数都可以成为可以观察自身数据的变化以执行视图的重绘，而使用 MobX 框架也不例外。

​		通常，使用 observable 标签即可检测数据的变化，只有在严格模式下才建议使用 action 标签进行包装。使用 action 标签的好处在于，它可以帮助开发者更好地组织应用，并表达出函数修改状态的意图。

### 7.2.4. Mobx 与 Redux 的对比

​		Redux 和 MobX 都是时下比较火热的数据流管理框架，它们各有自己特定的适用场合，并且在某些场合下可以配合使用。要比较两个框架的异同，并且选择适合自己的状态管理框架，可以从以下几个方面进行选择。

* 单个与多个 store：Redux 将所有的状态数据存放在一个全局 Store 中，这个 Store 对象就是 Redux 框架单一的数据源；而 MobX 通常有多个 Store，多个 Store 之间互不影响。同时，Redux 框架中数据通常是标准化的，而 MobX 则可以保存非标准化的数据。
* 普通数据与可观察数据：Redux 使用普通的 JavaScript 对象来存储数据；而 MobX 使用 observable 来存储数据，因此 MobX 可以自动观察数据并跟踪数据发生的变化，而 Redux 则需要进行手动更新。·
* 可变与不可变：Redux 使用的是不可变状态，这意味着状态是只读的，开发者不能直接覆盖它们；MobX 的状态可以直接被覆盖，具体来说，只需要使用新的值更新状态即可。

​		同时，Redux 遵循函数式编程范例，而 MobX 则更适用于面向对象。开发者需要根据具体的业务合理地选择。总体来说，MobX 适用于数据流不太复杂且易于管理的场景，而 Redux 适用于数据流极度复杂，需要通过中间件来减缓业务复杂度的场景。

## 7.3. 第三方库

### 7.3.1. NativeBase

​		NativeBase 是由 GeekyAnts 公司开发的一款优秀的 React Native 组件库，它提供了丰富的第三方组件，大有一种要替代 React Native 原生组件的姿态。并且从 2.4.1 版本开始，NativeBase 已经支持前端 Web 开发。

​		NativeBase 自身提供了很多实用的组件，可以用来帮助开发者快速地开发页面。

​		NativeBase 所有的组件都放在 Container 组件中，Container 组件的作用类似于 React Native 的 View 组件，Header 是导航栏组件，Content 是文本组件。

​		同时，NativeBase 还提供了丰富的矢量图标，其作用类似于阿里巴巴的 iconfont。之所以支持矢量图标，是因为 NativeBase 默认集成了 react-native-vector-icons 矢量图库，使用 NativeBase 的资源文件时只需要声明图标名称即可，如 name='home'。

### 7.3.2. react-native-elements

​		除了 NativeBase 外，react-native-elements 也是一个常见的 React Native 组件库。react-native- elements 库提供了数十种常用的组件，可方便开发者快速构建应用界面。

### 7.3.3. react-navigation

​		一个完整的移动应用往往是由多个页面组成的，如果要在页面与页面之间执行跳转就需要借助路由或导航器。在 0.44 版本之前，开发者可以直接使用官方提供的 Navigator 组件来实现页面跳转。不过从 0.44 版本开始，Navigator 组件被官方从核心组件库中剥离出来，放到了 react-native-deprecated-custom-components 模块中。

​		如果开发者需要继续使用 Navigator 组件，可以使用 yarn addreact-native-deprecatedcustom-components 命令安装 Navigator 组件。不过，官方并不建议开发者这么做，而是建议直接使用导航库 react-navigation。react-navigation 是 ReactNative 社区非常著名的页面导航库，可以用来实现各种页面跳转操作。

​		目前，react-navigation 支持3种类型的导航器，分别是StackNavigator、TabNavigator 和 DrawerNavigator。

* StackNavigator：包含导航栏的页面导航组件，作用类似于官方的 Navigator 组件。
* TabNavigator：底部展示 tabBar 的页面导航组件。
* DrawerNavigator：用于实现侧边栏抽屉页面的导航组件。

​		需要说明的是，由于 react-navigation 在 3.x 版本进行了较大的升级，所以在使用方式上与 2.x 版本会有很多的不同。

​		在移动应用开发中，经常会遇到抽屉式菜单切换的需求，如果要实现抽屉导航，可以使用 react-navigation 提供的 createDrawerNavigator。

### 7.3.4. react-native-snap-carousel

​		react-native-snap-carousel 是一个被广泛使用的轮播组件库，可以实现各种复杂的轮播滚动及拖拽效果。

​		在 react-native-snap-carousel 库中，官方提供了 3 个组件，分别是 Carousel、Pagination 和 ParallaxImage。其中，Carousel 是一个轮播容器组件，Pagination 是一个指示器组件，ParallaxImage 是一个专门针对图片的轮播组件。

### 7.3.5. react-native-image-picker

​		react-native-image-picker 是 React Native 应用开发中一个被广泛使用的拍照和相册管理开源库，可以轻松实现拍照和图片选取功能。

​		react-native-image-picker 库对外提供了 3 个 API，分别是 showImagePicker、launchImageLibrary 和 launchCamera。其中，launchImageLibrary 用于打开图库，launchCamera 用于打开拍照，showImagePicker 则可以打开拍照和图库，并且需要用户自己选择。

​		由于 react-native-image-picker 库不支持裁剪和压缩等操作，如果需要对图片进行裁剪和压缩等操作，可以使用 react-native-image-crop-picker 开源库。

### 7.3.6. react-native-video

​		由于 React Native 官方并没有提供视频播放组件，所以要在移动应用中集成短视频功能，就需要开发者自定义组件或者使用第三方组件。由于播放器开发的复杂性，自定义播放器会遇到很多问题，因此建议大家直接使用第三方库，比如 react-native-video。

​		react-native-video 是 React Native 社区开源的一款视频播放组件库，可以实现各种视频播放效果，如播放/暂停、横竖屏切换、缓存播放以及缓存进度等，

​		由于 VideoPlayPage 在封装时只考虑了通用情况，如果需要对视频进行其他方面的操作，还需要结合实际情况解决。并且，如果要让视频实现横竖屏切换的功能，还需要引入 react-native-orientation 库。

## 7.4. 自定义组件

### 7.4.1. 组件导入与导出

​		众所周知，构成 React Native 应用页面最基本的元素就是组件，组件可以被导入，也可以被导出。

```react
// 组件导出
export default class App extends Component{
    ...
}

// 组件导入
import App from './ App '
```

​		除了组件外，变量和常量也支持导入和导出。

```react
// 变量和常量导出
var name = '张三'
const age = '28'
export {name, age}

// 变量和常量导入
import {name, age} from './App'
```

​		变量、常量的导出需要依赖于组件，导入时也需要导入组件后才能够获取变量和常量。方法的导入/导出，和变量、常量的导入/导出类似。

### 7.4.2. 自定义弹框组件

​		通常，React Native 中的自定义组件可以分为两种类型：自定义原生组件以及自定义 React 组件。其中，自定义 React 组件是一种比较简单的情况，自定义原生组件则比较复杂，需要开发者具备原生客户端开发经验。

​		自定义 React 组件的核心是 render() 方法。围绕 render() 方法，开发者可以将组件开发成通用组件和平台特有组件。通常，通用组件需要使用自定义属性的方式接收外界传入的值，如果是必须传入的属性，可以使用 isRequired 关键字。

```react
    static propTypes = {
          title: PropTypes.string. isRequired,     // 必须传入属性
          content: PropTypes.string,
    }
```

​		当自定义组件接收到自定义的属性后，接下来就可以执行 render() 方法来绘制界面了。

### 7.4.3. 自定义单选组件

​		在 React Native 中，官方并没有提供单选组件，如果应用开发中涉及单选功能，就需要开发者使用第三款开源库或者自定义单选组件。

### 7.4.4. 自定义评分组件

​		自定义案例。

## 7.5. 本章小结

​		作为一个跨平台框架，React Native 的核心任务就是解决页面的绘制问题，而页面通常是由各种组件构成的，所以开发者必须了解基本的组件以及组件的生命周期。其次，由于官方提供的组件有限，所以为了满足开发任务，就需要开发者合理地选取第三方开源库或者自定义相关的组件。