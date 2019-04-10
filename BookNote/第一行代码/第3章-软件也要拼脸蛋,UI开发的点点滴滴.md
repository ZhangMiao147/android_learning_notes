# 第 3 章 软件也要拼脸蛋，UI开发的点点滴滴

1. ProgressDialog 使用 setCancelable() 中传入了 false ,表示 ProgressDialog 是不能通过 Back 键取消掉的。

2. TableLayout 布局可以使用 android:stretchColumns 属性允许将 TableLayout 中的某一列进行拉伸，以达到自动适应屏幕宽度的作用。

3. ArrayAdapter 可以通过泛型来指定要适配的数据类型，然后在构造函数中把要适配的数据传入即可。

4. android.R.layout.simple_list_item_1 是 Android 内置的布局文件，里面只有一个 TextView ，可用于简单地显示一段文本。

5. dp 是密度无关像素的意思，在不同密度的屏幕中显示比例将保持一致。 sp 是可伸缩像素的意思，解决文字大小的适配问题。

6. Android 中的密度就是屏幕每英寸所包含的像素数，通常以 dpi 为单位。

7. Nine-Patch 图片是一种被特殊处理过的 png 图片，能够指定哪些区域可以被拉伸而哪些区域不可以。