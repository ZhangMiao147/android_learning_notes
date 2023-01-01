# Snackbars 常见问题

## 调整文本的高度

Snackbars 文本的行数默认只有 2 行，如果想显示超过 2 行的文本，则会展示为 ...，可以通过设置 snackbar 的自定义 view ，通过设置自定义 view 的 maxLines 来控制显示的行数。

```kotlin
   val text = message.messageText ?: resources.getText(message.messageId)
Snackbar.make(context, view, text.toString(), Snackbar.LENGTH_SHORT).apply {
  setBackgroundTint(
    ResourcesCompat.getColor(
      resources,
      R.color.auxiliary_peri_800,
      null
    )
  )
	val textView = this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

	textView?.let {
  	it.gravity = Gravity.CENTER_HORIZONTAL
  	it.textAlignment = View.TEXT_ALIGNMENT_CENTER
  	it.maxLines = 3 // 通过调整 maxlines 的数值控制文本显示的高度
	}
}.show()
```

