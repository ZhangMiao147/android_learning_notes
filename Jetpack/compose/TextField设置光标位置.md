# TextField 设置光标位置

## 设置光标位置

在设置 TextFiled 的 text 的时候直接设置 String 类型的值，光标默认是在 0 的位置的

```kotlin
    var defaultValue = "test"
		BasicTextField(
        value = defaultValue,
        onValueChange = {
          defaultValue = it
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.SpaceAround) {
                repeat(otpCount) { index ->
                    val currentBordColor = when {
                        enabled.not() -> disEnabledBordColor
                        isError -> Error
                        else -> bordColor
                    }
                    val currentTextColor = if (isError) Error else textColor
                    Spacer(modifier = Modifier.width(2.dp))
                    CharView(
                        index = index,
                        text = otpText,
                        bordColor = currentBordColor,
                        textColor = currentTextColor,
                        charSize = charSize,
                        containerSize = containerSize,
                        type = type,
                        charBackground = if (enabled) charBackground else disEnabledCharBackground,
                        password = password,
                        passwordChar = passwordChar,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    )
```

如果点击输入框后，删除字符是没有效果的，输入字符是直接被插入到前面的，想要解决这个问题，就需要在设置默认字符的时候设置一下光标的位置。

设置光标的位置，需要用到 TextRange：

```kotlin
    var defaultValue = "test"
		BasicTextField(
        value = TextFieldValue(defaultValue, TextRange(defaultValue.length)), // 使用 TextRange 设置光标位置
        onValueChange = {
          defaultValue = it.text // 获取输入的字符就需要使用 it.text
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.SpaceAround) {
                repeat(otpCount) { index ->
                    val currentBordColor = when {
                        enabled.not() -> disEnabledBordColor
                        isError -> Error
                        else -> bordColor
                    }
                    val currentTextColor = if (isError) Error else textColor
                    Spacer(modifier = Modifier.width(2.dp))
                    CharView(
                        index = index,
                        text = otpText,
                        bordColor = currentBordColor,
                        textColor = currentTextColor,
                        charSize = charSize,
                        containerSize = containerSize,
                        type = type,
                        charBackground = if (enabled) charBackground else disEnabledCharBackground,
                        password = password,
                        passwordChar = passwordChar,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    )
```

设置 BasicTextField 的 value 属性时，使用 TextFieldValue(defaultValue, TextRange(defaultValue.length))，使用 TextRange 指定光标的位置。还有一个地方需要注意，那就是 onValueChange 回调的对象会从 String 表为 TextFieldValue，所以获取输入的内容就需要改为 it.text。

## 遇到的问题

加了光标后，如果在输入 n（特定数字）个字符后，调用 `focusManager.clearfocus()`方法隐藏键盘，那么 onValueChange 会被调用两次，可能会导致输入完内容的回调被调用两次，如果在回调中调用了接口，那么接口就会被调用了两次，从而导致了一些问题。

```kotlin
  var defaultValue = "test"
  val focusManager = LocalFocusManager.current
	BasicTextField(
    value = TextFieldValue(defaultValue, TextRange(defaultValue.length)), // 使用 TextRange 设置光标位置
    onValueChange = {
      Log.d("BasicTextField","onValueChange it:${it.text}")
      defaultValue = it.text // 获取输入的字符就需要使用 it.text
      if(it.text.length == 6){
        focusManager.clearFocus()
      }
    },
    enabled = enabled,
    keyboardOptions = keyboardOptions,
    decorationBox = {
        Row(horizontalArrangement = Arrangement.SpaceAround) {
            repeat(otpCount) { index ->
                val currentBordColor = when {
                    enabled.not() -> disEnabledBordColor
                    isError -> Error
                    else -> bordColor
                }
                val currentTextColor = if (isError) Error else textColor
                Spacer(modifier = Modifier.width(2.dp))
                CharView(
                    index = index,
                    text = otpText,
                    bordColor = currentBordColor,
                    textColor = currentTextColor,
                    charSize = charSize,
                    containerSize = containerSize,
                    type = type,
                    charBackground = if (enabled) charBackground else disEnabledCharBackground,
                    password = password,
                    passwordChar = passwordChar,
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
)
```

打印的日志就会是：

```
BasicTextField onValueChange it:1
BasicTextField onValueChange it:12
BasicTextField onValueChange it:123
BasicTextField onValueChange it:1234
BasicTextField onValueChange it:12345
BasicTextField onValueChange it:123456
BasicTextField onValueChange it:123456
```

可以简单的解决问题就是在 onValueChange 回调中判断，如果当前的内容和上一次的内容相同，则不要回调回去。

```kotlin
    var beforeText by remember {
        mutableStateOf("")
    }
    
     onValueChange = {
            val text = it.text
            if(text == beforeText){
                return@BasicTextField
            }
            beforeText = text
            //继续其他的操作
        },
```

也可以使用 `LocalSoftwareKeyboardController.current` 的 hide() 方法代替 `focusManager.clearfocus()` 隐藏键盘，但是 BasicTextField 的 onFocusChanged 回调不会收到消息，可能也会影响 onFocusChanged 回调的处理，为了不改动之前的逻辑，所以这个办法也不是一个好的解决方案。

## 参考文章

[Compose TextField设置光标位置](https://blog.csdn.net/mp624183768/article/details/128036630)
