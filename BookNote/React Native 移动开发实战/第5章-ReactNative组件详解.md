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

