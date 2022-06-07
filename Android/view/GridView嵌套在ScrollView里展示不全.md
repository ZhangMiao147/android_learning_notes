# GridView 嵌套在 ScrollView 里展示不全

## 问题描述

当 GridView 嵌套在 ScrollView 时，需要展示两行，结果只展示了一行。

## 解决问题

自定义 GridView 控件，在测量控件时，设置控件的高度样式为 AT_MOST（尽可能大）。

```java
package com.huihe.train.activity.app.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;


/**
 * 自定义gridview，解决ListView中嵌套gridview显示不正常的问题（1行半）
 */
public class TabGridView extends GridView {
public TabGridView(Context context, AttributeSet attrs) {
	super(context, attrs);
}


public TabGridView(Context context) {
	super(context);
}


public TabGridView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
}


@Override
public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
	MeasureSpec.AT_MOST);
	super.onMeasure(widthMeasureSpec, expandSpec);
	}
}

```

## 参考文档

1.[ 解决GridView展示不全问题](https://blog.csdn.net/ly646857201/article/details/108740501)