# 第 5 章 React Native 组件详解

## 5.1. 基础组件

　　在传统的 Web 开发中，页面开发使用的是基础的 HTML 元素和标签，如 < html >、< div > 、< head > 和 < br > 等。与传统的 Web 页面开发不同，React Native 无法使用传统的 HTML 元素和标签。

### 5.1.1. Text

　　在 React Native 中，Text 是一个用于显示文本内容的组件，也是使用频率极高的组件，它支持文本和样式的嵌套以及触摸事件处理。

　　Text 组件支持如下属性：

* Selectable：用户是否可以长按选择文本实现复制和粘贴操作，默认为 false。

* adjustsFontSizeToFit：字体是否随着样式的限制而自动缩放，仅对 iOS 有效。

* allowFontScalling：控制字体是否要根据系统的字体大小来缩放。

* minimumFontScale：当 adjustsFontSizeToFit 为 true 时，可以使用此属性指定字体的最小缩放比，仅对 iOS 有效。

* onLayout：当挂载或者布局发生变化后执行此函数。

* onLongPress：当文本组件被长按以后触发此函数。

* numberOfLines：用于设置文本最大显示行数，文本过长时会裁剪文本。

* ellipsizeMode：当文本组件无法全部显示所需要显示的字符串时，此属性会指定省略号显示的位置。

* selectionColor：文本被选择时的高亮颜色，仅对 Android 有效。

* suppressHighlighting：文本被按下时是否显示视觉效果，仅对 iOS 有效。

  除此之外，Text 组件还支持以下常见样式。

* textShadowOffset：字体阴影效果。

* fontSize：字体大小。

* fontStyle：字体样式，常见的取值有 normal、italic。

* fontWeight：字体粗细，支持 normal、bold 和 100~900 的取值。

* lineHeight：文本行高度。

* textAlign：文本对齐方式，支持 auto、left、right、center 和 justify 取值。

* textDecorationLine：文本横线样式，支持的取值有 none、underline、line-through 和 underline line-through 样式。

* textDecorationColor：文本装饰线条的颜色。

* textDecorationStyle：文本装饰线条的自定义样式。

* writingDirection：文本显示的方向。

* textAlignVertical：垂直相仿上文本对齐的方式，支持 auto、top、bottom 和 center 方式。

* letterSpacing：每个字符之间的距离。

　　需要说明的是，Text 组件默认使用的都是特有的文本布局，如果想要文本内容居中显示，还需要在 Text 组件外面再套一层 View 组件。

### 5.1.2. TextInput

　　TextInput 是一个输入框组件，用于将文本内容输入到 TextInput 组件上。作为一个高频使用组件，TextInput 组件支持自动拼写修复、自动大小写切换、占位默认字符设置以及多种键盘设置等功能。

　　TextInput 组件提供的 onChangeText() 函数，实现对用户输入的监听。

　　作为一个使用频率极高的输入框组件，TextInput 组件支持如下一些常用的属性和函数。

* allowFontScaling：控制字体是否需要根据系统的字体大小进行缩放。
* autoCapitalize：控制是否需要将殊途的字符切换为大写。
* autoCorrect：是否关闭拼写自动修正。
* autoFocus：是否自动获得焦点。
* blurOnSubmit：是否在文本框内容提交的时候失去焦点，单行输入框墨人情况下为 true，多行则为 false。
* caretHidden：是否隐藏光标。
* clearButtonMode：是否要在文本框右侧显示清除按钮，仅在 iOS 的单行输入模式下有效。
* clearTextOnFocus：是否在每次开始输入的时候清除文本框的内容，仅对 iOS 有效。
* dataDetectorTypes：将输入的内容转换为指定的数据类型，可选值有 phoneNumber、link、address、calenderEvent、none 和 all。
* defaultValue：定义 TextInput 组件中的字符串默认值。
* disableFullscreenUI：是否开启全屏文本输入模式，默认为 false。
* editable：控制文本框是否可编辑。
* enablesReturnKeyAutomatically：控制输入文本时软键盘的返回键是否可用。
* inlineImageLeft：指定一个图片放置在输入框的左侧，仅对 Android 有效。图片必须放置在  /android/app/src/main/res/drawable 目录下。
* inlineImagePadding：给左侧的图片设置 padding 样式，仅对 Android 有效。
* keyboardAppearance：指定软键盘的颜色，仅对 iOS 有效。
* keyboardType：指定弹出软键盘的类型，支持 number-pad、decimal-pad、numeric、email-address 和 phone-pad 等键盘类型。
* maxLength：限制文本框中的字符个数。
* multiline：控制文本框是否可以输入多行文字。
* numberOfLines：输入框的行数，需要 multiline 属性为 true 时才有效。
* onBlur：文本框失去焦点时的回调函数。
* onChange：文本框内容发生变化时的回调函数，它的回调接收一个 event 参数，可以通过 event.nativeEvent.text 获取用于输入的内容。
* onChangeText：文本框内容发生变化时的回调函数，onChangeText 的回调函数返回的内容和 onChange 类似，不过 onChangeText 可以直接返回用户输入的内容。
* onContentSizeChange：文本框内容长度发生变化时调用此函数。
* onEndEditing：文本输入结束后的回调函数。
* onFocus：文本输入框获得焦点时的回调函数。
* onKeyPress：当指定的键被按下时的回调函数。
* onLayout：当组件加载或者布局发生变化时调用。
* onSelectionChange：长按选择文本内容，选择返回发生变化时调用此函数。
* onSubmitEditing：当软键盘的 【确定/提交】按钮被按下时调用此函数。
* placeholder：文本输入框的默认占位字符串。
* placeholderTextColor：文本输入框占位字符串显示的文字颜色。
* returnKeyLabel：是否显示软键盘的确认按钮，仅对 Android 有效。
* returnKeyType：决定确认按钮显示的内容，支持 done、go、next、search 和 send 等取值。
* secureTextEntry：是否显示文本框输入的文字，如果为 true，可以实现类似密码的显示效果。
* selection：设置选中文字的范围。
* selectionColor：设置输入框高亮时的颜色，包括光标的颜色。
* selectTextOnFocus：如果为 true，获得焦点时所有文字都会被选中。
* spellCheck：是否禁用拼写检查的样式，仅对 iOS 有效。

　　如果想获取 TextInput 组件中用户选择的内容，可以使用 this.state.value。

　　如果要调用 TextInput 组件的某个功能，可以使用组件的 ref 属性来获取组件的实例，然后再调用组件的 API 函数，例如调用 clear() 方法清除输入的文本内容。

### 5.1.3. Image

​		Image 是一个图片展示组件，其作用类似于 Android 的 ImageView 或者 iOS 的 UIImageView。Image 组件支持多种类型图片的展示，包括网络图片、静态资源、临时的本地图片以及本地磁盘上的图片等。使用 Image 组件加载图片时只需要设置 source 属性即可，如果加载的是网络图片还需要添加 uri 标识。

​		Image 组件默认的图片宽和高都为 0，使用 image 组件加载图片时需要为图片指定宽和高，否则图片无法显示。

​		目前，Image 组件支持的图片格式有 PNG、JPG、JPEG、BMP、GIF、WebP 和 PSD。不过，默认情况下 Android 是不支持 GIF 和 WebP 格式图片的，如果需要支持 GIF 和 WebP 图片格式，需要在 android/app/build.gradle 文件中添加以下依赖脚本。

```groovy
dependencies{
  // 支持 Android 4.0（API level 14）之前的版本
  compile 'com.facebook.fresco:animated-base-support:1.1.0.0'
  
  // 支持 GIF 动图
  compile 'com.facebook.fresco:animated-gif:1.10.0'
  
  // 需要支持 WebP 格式，包括 WebP 动图
  compile 'com.facebook.fresco:animated-webp:1.10.0'
  compile 'com.facebook.fresco:webpsupport:1.10.0'
  
  // 支持 WebP 格式不需要动图
  compile 'com.facebook.fresco:webpsupport:1.10.0'

}
```

​		使用 Image 组件时，有一个重要的属性，即 resizeMode，此属性用于控制当组件和图片尺寸不成比例的时候以何种方式调整图片的大小。resizeMode 的取值有 5 种，分别是 cover、contain、stretch、repeat 和 center。

* conver：在保持图片宽高比的前提下缩放图片，直到宽度和高度都大于等于容器视图的尺寸。
* contain：在保持图片宽高比的前提下缩放图片，直到宽度和高度都小于等于容器视图的尺寸。
* stretch：拉伸图片且不维持图片的宽高比，直到宽度和高度都刚好填满容器。
* repeat：在维持原始尺寸的前提下，重复平铺图片直到填满容器。
* center：居中且不拉伸的显示图片。

### 5.1.4. ActivityIndicator

​		ActivityIndicator 是一个加载指示器组件，俗称 “ 转菊花 ”，其作用类似于 Android 的 ProgressBar 或者 iOS 的 UIProgressView。

​		ActivityIndicator 组件在不同平台的表演也有所差异的。

​		在移动应用开发中，加载指示器通常被用在异步耗时操作过程中以提升用户体验。

### 5.1.5. Switch

​		Switch 是 React Native 提供的一个状态切换组件，俗称开关组件，主要用来对开和关两个状态进行切换。

​		Switch 组件的用法比较简单，只需要给组件绑定 value 属性即可。如果需要改变组件的状态，则必须使用 onValueChange() 来更新 value 属性的值，否则 Switch 组件的状态将不会改变。

## 5.2. 容器组件

### 5.2.1. View 组件

​		在 React Native 中，View 容器组件支持 Flexbox 布局、样式、触摸事件处理和一些无障碍功能，它可以被放到其他容器组件里，也可以包含任意多个子组件。无论是 iOS 还是 Android，View 组件都会直接对应平台的原生视图，其作用等同于 iOS 的 UIView 或者 Android 的 android.view。

​		作为一个容器组件，View 的设计初衷是和 StyleSheet 搭配使用，这样可以使代码变得更加清晰，也容易获得更好的性能。

### 5.2.2. ScrollView 组件

​		ScrollView 是一个通用的滚动容器组件，主要用来在有限的显示区域内显示更多的内容。ScrollView 支持垂直和水平两个方向上的滚动操作，并且支持嵌套任意多个不同类型的子组件。作为一个容器组件，ScrollView 必须有一个确定的高度才能正常工作。如果不知道容器的准确高度，可以将 ScrollView 组件的样式设置为 {flex:1}，让其自动填充父容器的空余空间。ScrollView 通常包裹在视图的外面，用来控制视图的滚动。除此之外，还可以用它实现一些复杂的滚动效果，例如使用它实现轮播广告效果。

​		horizontal 属性用于控制视图的滚动方向，pagingEnabled 属性用于控制水平分页，showsHorizontalScrollIndicator 属性用来控制水平滚动条的显示，onMomentumScrollEnd 则是滚动结束需要调用的函数。

### 5.2.3. WebView 组件

​		WebView 是一个浏览器组件，主要用于加载和显示网页元素，其作用等同于 iOS 的 UIWebView、WKWebView 组件，或者 Android 的 WebView 组件。

​		WebView 组件的使用方法非常简单，只需要提供 source 属性即可。

​		除了使用网络地址加载网页外，WebView 组件还支持直接加载本地的 HTML 代码。

### 5.2.4. TouchableOpacity 组件

​		在 React Native 应用开发中，点击和触摸都是比较常见的交互行为，不过并不是所有的组件都支持点击事件。为了给这些不具备点击响应的组件绑定点击事件，React Native 提供了 Touchable 系列组件。事实上，Toychable 系列组件并不单指某一个组件，而是由 TouchableWithoutFeedback、TouchableOpacity、TouchableHigh 和 TouchableNativeFeedback 组件组成。

​		其中，TouchableWithputFeedback 不带反馈效果，其他 3 个组件都是带有触摸反馈效果的，可以理解为其他 3 个组件是 TouchableWithoutFeedback 组件的扩展，它们的具体含义和作用如下。

* TouchableWithoutFeedback：无反馈性触摸，用户点击时页面无任何视觉效果。
* TouchableHighlight：高亮触摸，在用户点击组件时产生高亮效果。
* TouchableOpacity：透明触摸，在用户点击组件时产生透明效果。
* TouchableNativeFeedback：仅适用于 Android 平台的触摸响应组件，会在用户点击后产生水波纹的视觉效果。

​		在 React Native 应用开发中，使用得最多的就是 TouchableOpacity 组件。作为一个触摸响应容器组件，TouchableOpacity 组件支持嵌套一个或多个子组件，同时会在用户手指按下视图时降低视图的透明度产生一种透明效果。

​		TouchableOpacity 组件的使用方法比较简单，只需要将它包裹在其他组件的外面即可实现点击功能。

## 5.3. 列表组件

### 5.3.1. VirtualizedList 组件

​		在移动应用开发中，列表是一种常见的页面布局方式。在 React Native 早期的版本中，如果要实现列表布局，只能使用 ListView 组件，不过在数据量特别大的情况下，组件的性能特别差，容易出现卡顿和渲染延迟的问题。为了改善这一缺陷，React Native 在 0.43.0 版本引入了 VirtualizedList 系列组件，此类组件自带视图复用和回收特性，因此特性哟了质的提升。

​		事实上，VirtualizedList 组件通过维护一个有限的渲染窗口，将渲染窗口之外的元素全部用合适的定长空白空间代替，极大地降低了内存消耗以及提升了在大量数据下的使用性能。当一个元素距离可视区太远时，它的渲染优先级就会变低。通过此种方式，渲染窗口尽量减少出现空白区域的可能性，从而提高视图渲染的性能。

​		VirtualizedList 组件会默认初始化一个数量为 10 的列表，然后采用循环绘制的方式来绘制列表数据。当列表元素距离可视区太远时，元素将会被回收，否则将被绘制。

​		一般来说，除非有特殊的性能要求，否则不建议直接使用 VirtualizedList 组件，因为 VirtualizedList 是一个抽象组件，使用起来比较麻烦。在实际项目开发过程中，使用 FlatList 和 SectionList 组件即可满足开发需求，因为它们都是基于 VirtualizedList 组件扩展的，因而不存在任何性能上的问题。

### 5.3.2. FlatList 组件

​		在 FlatList 组件出现之前，React Native 使用 ListView 组件来实现列表功能，不过在列表数据比较多的情况下， ListView 组件的性能并不是很好，所以在 0.43.0 版本，React Native 引入了 FlatList 组件。相比 ListView 组件， FlatList 组件适用于加载长列表数据，而且性能也更佳。

​		相比 ListView 组件，FlatList 组件主要支持以下功能和特性。

* 完全跨平台。
* 支持水平布局模式。
* 支持行组件显示或隐藏时可配置事件回调。
* 支持单独的头部组件。
* 支持单独的尾部组件。
* 支持自定义行间分隔线。
* 支持下拉刷新。
* 支持上拉加载更多。
* 支持跳转到指定行，类似于ScrollToIndex功能。
* 如果需要支持分组/类/区的功能，请使用SectionList组件。

​		得益于 FlatList 组件对 VirtualizedList 组件的强大封装能力，使用 FlatList 组件实现列表效果时只需要提供 data 和 renderItem 属性即可，其他属性可以根据实际情况进行合理的配置。

​		FlatList 组件还可以实现网格列表效果，使用 FlatList 组件实现网格列表需要提供 FlatList 组件的 numColumns 属性。除了 data 和 renderItem 属性之外，FlatList 组件还有如下一些使用频率比较高的属性和方法。

#### 5.3.2.1. Item的key

​		使用 FlatList 组件实现列表效果时，系统要求给每一行子组件设置一个 key，key 是列表项的唯一标识，目的是当某个子视图的数据发生改变时可以快速地重绘改变的子组件。

​		当然，FlatList组件提供的keyExtractor属性也能达到此效果。

#### 5.3.2.2. 分割线 seperator

​		FlatList 组件本身的分割线并不是很明显，如果要实现分割线，主要有两种策略：设置 boderBottom 或 ItemSeperatorComponent 属性。

​		如果只是简单的一条分割线，在 Item 组件的中添加 boderBottom 相关的属性即可。

​		需要注意的是，使用 boderBottom 实现分割线时，列表顶部和底部的分割组件是不需要绘制的。当然，更简单的方式是使用 FlatList 的 ItemSeperatorComponent 属性。

#### 5.3.2.3. 下拉刷新和上拉加载更多

​		和 ListView 组件一样，FlatList 组件也可以实现下拉刷新和上拉加载更多的功能，使用时添加对应的属性即可。

​		其中，在使用 FlatList 组件实现下拉刷新和上拉加载更多功能时，包含以下几个状态。

* refreshing：列表是否处于正在刷新的状态。
* onRefresh：开始刷新事件，可以在此状态发起接口请求。
* onEndReached：上拉加载更多事件。可以在此方法中设置加载更多对应的组件状态，并在 setState() 方法的回调里请求后端数据。
* onEndReachedThreshold：表示距离底部多远时触发onEndReached。与 ListView 不同的是，FlatList 中的onEndReachedThreshold 的值是一个比值而非像素。

​		通常在执行下拉刷新操作过程中，字段的初始值都需要重新初始化，并且缓存的数据也需要清空。

#### 5.3.2.4. Header和Footer

​		在 React Native 应用开发中，下拉刷新是一个比较常见的功能，如果只需要执行下拉刷新操作可以直接使用 RefreshControl 组件。

​		除了 RefreshControl 组件外，还可以使用FlatList组件来实现下拉刷新操作，并且 FlatList 组件还支持上拉加载更多操作。之所以可以这么做，是因为 FlatList 组件自带了手势滑动监测功能。使用 FlatList 组件实现下拉刷新和上拉加载更多功能，需要使用 FlatList 组件的 onHeaderRefresh 和 onFooterRefresh 属性。

### 5.3.3. SectionList 组件

​		和 FlatList 组件一样，SectionList 组件也是由 VirtualizedList 组件扩展来的，不过相比于 VirtualizedList 组件，FlatList 和 SectionList 组件的应用更加广泛，尽管VirtualizedList组件更加灵活方便。

​		SectionList 也是一个列表组件，不同于 FlatList 组件， SectionList 组件主要用于开发列表分组、吸顶悬浮等功能。 SectionList 组件的使用方法也非常简单，只需要提供 renderItem、renderSectionHeader 和 sections 等必要的属性即可。

​		得益于 SectionList 组件提供的诸多强大的属性和方法，使用时只需要传入 renderSection Header、renderItem 和 sections 等必要的属性即可。

## 5.4. 平台组件

​		使用 React Native 进行跨平台应用开发时，由于 React Native 最终使用的是原生平台组件来完成渲染的，所以并不是所有的组件都是通用的。针对这一情况，React Native 提供了只能在某个特定平台才能使用的平台组件，并分别以 Android 和 iOS 后缀进行标识，如 ProgressBarAndroid、ViewPagerAndroid、ProgressViewIOS 和 DatePickerIOS 等。

### 5.4.1. ViewPagerAndroid 组件

​		ViewPagerAndroid 是一个只能运行在 Android 平台的页面切换容器组件，其作用类似于平台 Android 原生的 ViewPager 控件，主要作用是嵌套多个视图实现左右滑动切换效果。使用 ViewPagerAndroid 组件实现左右滑动切换时，每个子组件都被视为一个单独的页面，且每个子视图都必须是纯 View 视图，而不能是自定义的复合组件。

​		ViewPagerAndroid 组件的使用方法非常简单，只要将需要渲染的子视图添加到 ViewPager Android 中即可。

​		不过，由于 ViewPagerAndroid 仅对 Android 平台有效，所以在应用开发中使用的频率并不高。

### 5.4.2. SafeAreaView 组件

​		为了适配 iPhone X 及后面机型的刘海屏，React Native 官方在 0.50.1 版本引入了 SafeAreaView 组件。

​		目前，SafeAreaView 组件只支持 iPhone X 及以上机型使用，因此如果需要适配 Android 设备的刘海屏，则需要借助第三方库或者修改原生 API 来实现。

​		SafeAreaView 组件的使用方法非常简单，只需要将 SafeAreaView 组件嵌套在视图的最根级别中即可完成刘海屏适配。

​		在 React Native 应用开发中，为了完成 iPhone X 及以上机型刘海屏的适配，还需要对刘海屏和非刘海屏设备进行区分，因为只有满足刘海屏的机型才需要刘海屏适配，判断的代码如下。

```js
export let screenW = Dimension.get('window').width;
export let screenH = Dimension.get('window').height;
// iPhoneX 默认宽高
const X_WIDTH = 375;
const X_HEIGHT = 812;
/**
* 判断是否为 iphoneX 
* @returns {boolean}
*/
export function isIphoneX() {
  return (
  	Platform.OS === 'ios' && ((screenH === X_HEIGHT && screenW === X_WIDTH) || (screenH === X_WIDTH && screenW === X_HEIGHT))
  )
}

```

​		由于 SafeAreaView 是 0.50.1 版本才提供的新组件，所以如果要在老版本中适配 iPhoneX 及以上版本的刘海屏，需要借助一些开源的第三方库来实现，常见的有 react-native-safearea-view。

### 5.4.3. SegmentedControlIOS

​		SegmentedControlIOS 是一个分段选择组件，仅对 iOS 平台有效。如果要在 Android 平台实现分段选择，可以使用 Android 原生平台提供的 RadioButton 控件。

​		SegmentedControlIOS 是 React Native 对 iOS 原生系统 UISegmentedControl 控件的封装。

​		SegmentedControlIOS 组件的使用方法非常简单，使用时只需要提供 values 和 selectedIndex 属性即可。其中， values 属性表示分段组件的数据源，通常为数组格式， selectedIndex 属性用来表示被选中的选项的下标。

## 5.5. PureComponent 组件

​		PureComponent 又名纯组件，是 React 15.3 版本新增的根组件类。相比于传统的 Component 根组件， PureComponent 加入了很多优化的元素，因此可以认为是一个优化版的 Component。

​		PureComponent 之所以性能更强，是因为当组件的 props 或 state 发生改变时，PureComponent 将对 props 和 state 进行浅比较，然后调用 render() 绘制界面。而如果组件的 props 和 state 都没发生改变，render() 就不会触发，从而省去虚拟 DOM 的生成和比对过程，以此提升性能。

​		不过需要注意的是，PureComponent 的 shouldComponentUpdate() 只会对对象进行浅对比，如果遇到的是复杂的数据结构，也有可能会因深层的数据不一致而产生错误的判断结果。

​		因为 PureComponent 的 shallowEqual 操作，所以在使用 PureComponent 时，需要注意组件的 props 和 state 的引用是否发生改变。如果引用没有发生改变，直接调用 setState() 是不会触发重新渲染操作的。

​		平时开发中，为了改善性能、提升渲染速度，可以直接使用 PureComponent 根组件替换 Component，但是这种方式并不是最保险的，特别是对于已经运行了很多年的项目。因此，为了兼容一些老项目，最好还是区别对待。

```js
import React { PureComponent, Component } from 'react';
//兼容老版本的写法
class Demo extends (PureComponent || Component) {  //...}
```

## 5.6. 本章小结

​		在前端开发中，特别是 React Native 应用开发中，组件是构成前端页面的核心。通过 React Native 官方提供的组件，开发者可以高效地开发复杂的移动应用。

