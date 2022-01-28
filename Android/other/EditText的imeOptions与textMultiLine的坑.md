# EditText android:imeOptions 与 inputType="textMultiLine" 的坑

# 1. imeOptions

在 xml 为 EditText 中设置 imeOptions 可以控制键盘确认键的具体功能，如下列举了一些：

```java
android:imeOptions="flagNoExtractUi" //使软键盘不全屏显示，只占用一部分屏幕 同时,这个属性还能控件软键盘右下角按键的显示内容,默认情况下为回车键
android:imeOptions="actionNone" //输入框右侧不带任何提示
android:imeOptions="actionGo"   //右下角按键内容为'开始'
android:imeOptions="actionSearch" //右下角按键为放大镜图片，搜索
android:imeOptions="actionSend"   //右下角按键内容为'发送'
android:imeOptions="actionNext"  //右下角按键内容为'下一步' 或者下一项
android:imeOptions="actionDone" //右下角按键内容为'完成'
```

# 2. 坑 1

如果设置了 inputType="textMultiLine" 会使 android:imeOptions 无效。

