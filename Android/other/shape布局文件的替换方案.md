# Shape 布局文件的替换方案

# 1. 前言

在日常开发中，需要为各种 UI 效果创建不同的 xml 文件进行描述。随着项目迭代，Drawable 的文件会越来越多，文件名很模棱两可，这样就会导致两个问题：1. 复用的难度很大，很难去查看全部的 xml 文件去确定有自己需要的文件；2. 项目体积就逐渐增大。

而导致 Drawable 文件越来越多的主要原因就是 Drawable 文件的复用很少，可能就是因为一个圆角值的不同或者填充颜色的不同，就需要创建一个新的 xml 文件。

Drawable 文件中使用最多的就是背景文件的创建，也就是：

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid android:color="@color/colorAccent" />
    <stroke
        android:width="4dp"
        android:color="@color/colorPrimary" />
    <corners android:radius="20dp" />

</shape>
```

所以本篇主要讲解如何替换 Drawable 中的 shape 文件，从而增加 Drawable 的复用。

## 1.1. 常用 shape 标签的样式

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid android:color="@color/colorAccent" />
    <stroke
        android:width="4dp"
        android:color="@color/colorPrimary" />
    <corners android:radius="20dp" />

</shape>
```

常用的 shape 标签涉及三个属性：

* solid：填充
  * color：填充颜色
* stroke：描边
  * Width：描边的宽度
  * Color：描边的颜色
* corners：圆角
  * Radius：圆角角度

## 1.2. 方案

目前替换方案有三种：

1. 通用 shape 样式控件

# 2. 第一种替换方案：通用 shape 样式控件

## 2.1. 方案思想

将 shape 标签的属性作为控件的属性，在控件中动态生成 GradientDrawable，然后设置背景。

## 2.2. 方案代码实现

### 2.2.1. style 属性

自定义 shape 的通用控件属性是根据 shape 标签的常用属性来设置的。

写在 attrs.xml 文件中（没有的话就在 values 文件夹中创建一个 attrs.xml 文件）

```xml
    <declare-styleable name="CommonShapeButton">
        <attr name="android:shape"/>
        <attr name="android:solidColor"/>
        <attr name="android:strokeColor"/>
        <attr name="strokeWidth" format="dimension"/>
        <attr name="android:radius"/>
    </declare-styleable>
```

strokeWidth 没有使用 android:strokeWidth 是因为 android:strokeWidth 是 float 的，想要使用 dimension 类型的，所以就没有使用 android:strokeWidth。

### 2.2.2. 自定义 View 实现

```java
package com.example.drawable.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.example.drawable.R;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

/**
 * 通用 shape 样式按钮
 */
public class CommonShapeButton extends AppCompatButton {

    public static final String TAG = CommonShapeButton.class.getSimpleName();

    private GradientDrawable mShapeDrawable; // 通用 shape 图片

    public CommonShapeButton(Context context) {
        this(context, null);
    }

    public CommonShapeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonShapeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initShapeAttribute(context, attrs);
    }

    /**
     * shape 模式
     * 矩形（rectangle）、椭圆形（oval）、线性（line）、环形（ring）
     */
    private int mShapeMode = RECTANGLE;
    /**
     * 填充颜色
     */
    private int mSolidColor = Color.TRANSPARENT;
    /**
     * 描边颜色
     */
    private int mStrokeColor = Color.TRANSPARENT;
    /**
     * 描边宽度
     */
    private int mStrokeWidth = 0;
    /**
     * 圆角
     */
    private float mCornersRadius = 0;

    /**
     * 获取自定义属性
     *
     * @param context 上下文
     * @param attrs   属性
     */
    private void initShapeAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonShapeButton);
        mShapeMode = typedArray.getInt(R.styleable.CommonShapeButton_android_shape, RECTANGLE);
        mSolidColor = typedArray.getInt(R.styleable.CommonShapeButton_android_solidColor, Color.TRANSPARENT);
        mStrokeColor = typedArray.getInt(R.styleable.CommonShapeButton_android_strokeColor, Color.TRANSPARENT);
        mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CommonShapeButton_strokeWidth, 0);
        mCornersRadius = typedArray.getDimension(R.styleable.CommonShapeButton_android_radius, 0);
        setShapeBackground();
        typedArray.recycle();
    }

    /**
     * 设置 shape
     */
    private void setShapeBackground() {
        mShapeDrawable = new GradientDrawable();
        mShapeDrawable.setShape(mShapeMode);
        mShapeDrawable.setStroke(mStrokeWidth, mStrokeColor);
        mShapeDrawable.setColor(mSolidColor);
        mShapeDrawable.setCornerRadius(mCornersRadius);
        setBackground(mShapeDrawable);
    }

    /**
     * 设置 shape 的 mode 属性
     *
     * @param shapeMode mode 属性
     */
    public void setShapeMode(int shapeMode) {
        this.mShapeMode = shapeMode;
        mShapeDrawable.setShape(mShapeMode);
        setBackground(mShapeDrawable);
    }

    /**
     * 设置描边的宽度
     *
     * @param strokeWidth 描边宽度
     */
    public void setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        mShapeDrawable.setStroke(mStrokeWidth, mStrokeColor);
    }

    /**
     * 设置描边颜色
     *
     * @param strokeColor 描边颜色
     */
    public void setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
        mShapeDrawable.setStroke(mStrokeWidth, mStrokeColor);
    }

    /**
     * 设置填充颜色
     *
     * @param solidColor 填充颜色
     */
    public void setSolidColor(int solidColor) {
        this.mSolidColor = solidColor;
        mShapeDrawable.setColor(mSolidColor);
    }

    /**
     * 设置圆角角度
     *
     * @param cornersRadius 圆角角度
     */
    public void setCornersRadius(float cornersRadius) {
        this.mCornersRadius = cornersRadius;
        mShapeDrawable.setCornerRadius(mCornersRadius);
    }
}

```

自定义 view 里面没有很多代码，主要就是从样式中取出设置的属性值，然后创建一个 GradientDrawable 对象，将属性值设置给 GradientDrawable 对象，然后调用 setBackground() 方法设置背景图。

### 2.2.3. 使用

可以在布局文件中使用，也可以在代码中动态设置：

* 在布局文件中使用

```xml
    <com.example.drawable.view.CommonShapeButton
        android:layout_width="160dp"
        android:layout_height="80dp"
        android:gravity="center"
        android:radius="10dp"
        android:shape="rectangle"
        android:solidColor="#ff0000"
        android:strokeColor="#00ff00"
        android:text="方案1-1"
        android:textSize="24sp"
        app:strokeWidth="4dp" />
```

* 在代码中设置

```java
    private void setFirstShape() {
        btn_first.setShapeMode(GradientDrawable.RECTANGLE);
        btn_first.setStrokeWidth(10);
        btn_first.setStrokeColor(Color.BLACK);
        btn_first.setSolidColor(Color.GREEN);
        btn_first.setCornersRadius(20);
    }
```

## 2.3. 方案评价

* 优点
  * 实现简单
  * 可以及时看到实现的样式
* 缺点
  * 不是所有的控件能使用，一般只会创建常用控件的自定义控件，如果其他控件使用，需要再次创建自定义控件。
* 适用场景
  * shape 布局文件只用于常用的几种控件

# 3. 第二种替换方案：动态生成 Drawable

## 3.1. 方案思路

对于方案一的缺陷，提出了方案二，那就是将 shape 的常用属性动态创建一个 GradientDrawable 对象，然后设置给任意的控件，这样就解决了需要针对不同控件创建不同自定义 view 的问题。

## 3.2. 方案代码实现

### 3.2.1. Shape 的常用属性 bean 类

```java
package com.example.drawable.bean;

import android.graphics.Color;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

/**
 * shape 属性 bean 类
 * ShapeUtils 类使用
 */
public class ShapeAttributeBean {

    /**
     * shape 模式
     * 矩形（rectangle）、椭圆形（oval）、线性（line）、环形（ring）
     */
    private int shapeMode = RECTANGLE;
    /**
     * 填充颜色
     */
    private int solidColor = Color.TRANSPARENT;
    /**
     * 描边颜色
     */
    private int strokeColor = Color.TRANSPARENT;
    /**
     * 描边宽度
     */
    private int strokeWidth = 0;
    /**
     * 圆角
     */
    private float cornersRadius = 0;

    public int getShapeMode() {
        return shapeMode;
    }

    public void setShapeMode(int shapeMode) {
        this.shapeMode = shapeMode;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public void setSolidColor(int solidColor) {
        this.solidColor = solidColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public float getCornersRadius() {
        return cornersRadius;
    }

    public void setCornersRadius(float cornersRadius) {
        this.cornersRadius = cornersRadius;
    }

    @Override
    public String toString() {
        return "ShapeAttributeBean{" +
                "shapeMode=" + shapeMode +
                ", solidColor=" + solidColor +
                ", strokeColor=" + strokeColor +
                ", strokeWidth=" + strokeWidth +
                ", cornersRadius=" + cornersRadius +
                '}';
    }
}
```

Shape 的 bean 类中也只是包含了 shape 标签的常用属性。

### 3.2.2. ShapeUtils 的实现

```java
package com.example.drawable.utils;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.example.drawable.bean.ShapeAttributeBean;

public class ShapeUtils {

    /**
     * 给 view 设置 shape 背景图
     *
     * @param view          目标 view
     * @param attributeBean shape 属性
     */
    public static void setViewShape(View view, ShapeAttributeBean attributeBean) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(attributeBean.getShapeMode());
        gradientDrawable.setStroke(attributeBean.getStrokeWidth(), attributeBean.getStrokeColor());
        gradientDrawable.setColor(attributeBean.getSolidColor());
        gradientDrawable.setCornerRadius(attributeBean.getCornersRadius());
        view.setBackground(gradientDrawable);
    }
}

```

ShapeUtils 的 setViewShape() 方法中也是创建了一个 GradientDrawable 对象，将 ShapeAttributeBean 的属性设置给 GradientDrawable 对象，然后调用 view 的 setBackground() 方法将 GradientDrawable 对象设置为背景图。

### 3.2.3. 使用

```java
    private void setSecondShape() {
        ShapeAttributeBean shapeAttributeBean = new ShapeAttributeBean();
        shapeAttributeBean.setShapeMode(GradientDrawable.RECTANGLE);
        shapeAttributeBean.setStrokeWidth(10);
        shapeAttributeBean.setStrokeColor(Color.BLUE);
        shapeAttributeBean.setSolidColor(Color.YELLOW);
        shapeAttributeBean.setCornersRadius(20);
        ShapeUtils.setViewShape(btn_second, shapeAttributeBean);
    }
```

使用也很简单，创建 ShapeAttributeBean 对象，设置想要的 Shape 属性，然后调用ShapeUtils 的  setViewShape() 方法就可以了。

## 3.3. 方案评价

* 优点
  * 实现简单
  * 所有的控件都可以使用
* 缺点
  * 在布局文件中不能知道布局的实现效果，需要在代码中查看
* 适用场景
  * 多种控件使用 shape 布局文件，而且代码控制方式多

# 4. 第三种替换方案：BindingAdapter

## 4.1. 方案思路

第一种方案和第二种方案各有优缺点，如果能有一个即能在布局文件中查看控件的展示效果，又能所有控件都能使用，也就是拥有第一种和第二种方案的优点，那应该就是最完美的方案了。

DataBinding 是 Android 官方推出的数据绑定库，数据绑定让数据变化直接反映到布局中。而绑定后运行时数据变化会设置给控件的实现原理关键就是 DataBinding 通过提供的 @BindAdapter 注解，该注解将任意指定的属性和任意指定的方法关联，DataBinding 就在编译的时候动态生成的调用关系，而对于常用的控件，DataBinding 已经预置了对应的注解方法。

任意指定的属性并非特指在布局中 Android 提供的标准属性，也就是说，可以提供任意字符串作为属性。所以可以使用 BindingAdapter 来实现。

## 4.2. 方案代码实现

### 4.2.1. 引入 DataBinding

在项目的 build.gradle 文件中添加以下代码：

```groovy
android {
    ...

    dataBinding{
        enabled = true
    }
}
```

### 4.2.2. 使用 BindingAdapter

```java
    @BindingAdapter(value = {
            "shape_mode",
            "stroke_width",
            "stroke_color",
            "solid_color",
            "corner_radius"
    }, requireAll = false)
    public static void setViewShapeStroke(View view, @ShapeMode int shapeMode, int strokeWidth, @ColorInt int strokeColor,
                                          @ColorInt int solidColor, int cornerRadius) {
        Log.d(TAG, "setViewShapeStroke");
        Drawable drawable = view.getBackground();
        Log.d(TAG, "setViewShapeStroke drawable:" + drawable);
        if (drawable == null || !(drawable instanceof GradientDrawable)) {
            drawable = new GradientDrawable();
            ((GradientDrawable) drawable).setShape(shapeMode);
            ((GradientDrawable) drawable).setStroke(strokeWidth, strokeColor);
            ((GradientDrawable) drawable).setColor(solidColor);
            ((GradientDrawable) drawable).setCornerRadius(cornerRadius);
            view.setBackground(drawable);
        } else {
            ((GradientDrawable) drawable).setShape(shapeMode);
            ((GradientDrawable) drawable).setStroke(strokeWidth, strokeColor);
            ((GradientDrawable) drawable).setColor(solidColor);
            ((GradientDrawable) drawable).setCornerRadius(cornerRadius);
        }
    }

    @IntDef({
            ShapeMode.RECTANGLE,
            ShapeMode.OVAL,
            ShapeMode.LINE,
            ShapeMode.RING
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShapeMode {
        int RECTANGLE = GradientDrawable.RECTANGLE;
        int OVAL = GradientDrawable.OVAL;
        int LINE = GradientDrawable.LINE;
        int RING = GradientDrawable.RING;

    }

```

使用 BindingAdapter 也很简单，这个方法即在布局引用的时候会调用，也可以在代码中调用方法使用。

### 4.2.3. 使用

```xml
<?xml version="1.0" encoding="utf-8"?>

<layout>

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        tools:context=".MainActivity">

				...
    
        <Button
            android:id="@+id/activity_main_three_btn"
            corner_radius="@{20}"
            shape_mode="@{@integer/shape_mode_rectangle}"
            solid_color="@{0xff00ffff}"
            stroke_color="@{0xffff0000}"
            stroke_width="@{10}"
            android:layout_width="160dp"
            android:layout_height="80dp"
            android:layout_marginTop="10dp"
            android:gravity="center"
            android:text="方案3"
            android:textSize="24sp" />

    </LinearLayout>
</layout>
```

在布局文件中使用时，需要使用 < layout > < /layout > 将自己的整体布局包裹起来，这样才能使用自己定义的布局属性。

除了在布局文件中使用外，还需要在布局使用的部分调用下面的代码：

```java
DataBindingUtil.setContentView(this, R.layout.activity_main);
```

在 Activity 中使用以上的代码替换掉：

```java
setContentView(R.layout.activity_main);
```

## 4.3. 方案评价

* 优点
  * 可以在布局文件中查看设置的布局样式
  * 所有的控件都可以使用
* 缺点
  * 需要引入 DataBinding
  * 相比较实现多一些
* 适用场景
  * 最好的实现方案，并且不介意引入 DataBinding

# 5. 总结

本篇主要讲解了 shape 布局文件的三种替换方案，三种方案的对比如下：

| 方案                | 实现思路                                                     | 优点                                               | 缺点                                                        | 适用场景                                        |
| ------------------- | ------------------------------------------------------------ | -------------------------------------------------- | ----------------------------------------------------------- | ----------------------------------------------- |
| 通用 shape 样式控件 | 将 shape 的常用属性作为常用控件的属性，然后根据属性生成 Drawable 对象，然后设置为背景图 | 实现简单；布局文件就可看到实现效果                 | 只能用于常用控件，其他控件想要复用就需要实现新的自定义 view | shape 布局文件只用于常用的几种控件              |
| 动态生成 Drawable   | 实现一个工具类方法，根据传递的 shape 属性创建 Drawable 对象，然后设置为 view 对象的背景图 | 实现简单；所有控件都可以使用                       | 只能在代码中看传递的参数，不能在布局文件中看到效果          | 多种控件使用 shape 布局文件，而且代码控制方式多 |
| BindingAdapter      | 使用 DataBinding 技术在布局中设置 shape 的属性，在代码中生成 Drawable 对象，设置为背景 | 既可以在布局中设置想要的效果，所有控件也都可以使用 | 需要引入 DataBinding                                        | 最好的实现方案，并且不介意引入 DataBinding      |

# 6. 参考文章

1. [减少drawable.xml并对其进行管理](https://blog.csdn.net/fuzhongbin/article/details/82656667?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.control)

2. [一种巧妙的drawable.xml替代方案](https://juejin.im/post/6844903673693208584)

3. [DataBinding使用指南(一)：布局和binding表达式](https://blog.csdn.net/guiying712/article/details/80206037)

