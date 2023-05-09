# EditText 的 imeOptions 与多行输入的问题

在 xml 为 EditText 中设置 imeOptions 可以控制键盘确认键的具体功能，如下列举了一些：

```xml
android:imeOptions="flagNoExtractUi" //使软键盘不全屏显示，只占用一部分屏幕 同时,
这个属性还能控件软键盘右下角按键的显示内容,默认情况下为回车键
android:imeOptions="actionNone" //输入框右侧不带任何提示
android:imeOptions="actionGo"   //右下角按键内容为'开始'
android:imeOptions="actionSearch" //右下角按键为放大镜图片，搜索
android:imeOptions="actionSend"   //右下角按键内容为'发送'
android:imeOptions="actionNext"  //右下角按键内容为'下一步' 或者下一项
android:imeOptions="actionDone" //右下角按键内容为'完成'
```

**问题描述：**因为 EditText 一旦设置了多行显示，键盘总是显示 Enter 键。有时候不仅需要文本输入多行显示，而且 Enter 键需要支持 imeOptions 设置，比如显示完成键而不是回撤换行。

当 EditText 弹出输入法时，会调用方法 public InputConnection onCreateInputConnection(EditorInfo outAttrs) 来创建和输入法的连接，设置输入法的状态，包括显示什么样的键盘布局。需要注意的地方时这部分的代码：

```java
if (isMultilineInputType(outAttrs.inputType)) {
     // Multi-line text editors should always show an enter key.
     outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
 }


private static boolean isMultilineInputType(int type) {
        return (type & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE)) ==
            (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
    }
```

发现，当 EditText 的 inputType 包含 textMultiLine 标志位，会强迫 imeOptions 加上 IME_FLAG_NO_ENTER_ACTION 位，这导致了只显示 Enter 键。

在网上找的解决方案是：可以继承 EditText 类，覆写 onCreateInputConnection 方法，如下：

```java
@Override
public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    InputConnection connection = super.onCreateInputConnection(outAttrs);
    int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
    if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
        // clear the existing action
        outAttrs.imeOptions ^= imeActions;
        // set the DONE action
        outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
    }
    if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
    }
    return connection;
}
```

然后还有一个坑，在基础 EditText 后，要重写完所有的构造函数，要不在 inflate 时会出错，直接调用父类的相关的构造方法就好。



**网上的解决方案试了是没有效果的，所以多行输入与控制键的设置问题解决不了。**



## 参考文章

[EditText android:imeOptions与inputType="textMultiLine" 的坑](https://blog.csdn.net/a641324093/article/details/62238385)
