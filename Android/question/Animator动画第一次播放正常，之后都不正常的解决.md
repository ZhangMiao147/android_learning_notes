# Animator 动画第一次播放正常，之后播放都不正常的问题解决

## 问题描述
　　第一次点击图片动画播放正常，在点击文字之后，图片没有显示出来，点击空白，播放动画，显示文字。
　　写了一个卡片翻转的动画，代码如下:

**activity_main.xml**
```java
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_gravity="center"
    android:background="#F2F2F2">

    <FrameLayout
        android:layout_width="252dp"
        android:layout_height="336dp"
        android:layout_gravity="center">

        <ImageView
            android:id="@+id/activity_main_image_iv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@mipmap/result" />

        <TextView
            android:id="@+id/activity_main_text_tv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#247BA0"
            android:gravity="center"
            android:text="确定"
            android:textColor="@android:color/white"
            android:textSize="36sp"
            android:visibility="gone" />

    </FrameLayout>


</FrameLayout>
```

**MainActivity.java**
```java
package com.zm.animatorerror;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_image;
    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        iv_image = (ImageView) findViewById(R.id.activity_main_image_iv);
        tv_text = (TextView) findViewById(R.id.activity_main_text_tv);
        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_text.setVisibility(View.VISIBLE);
                AnimatorSet inAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.rotate_in_anim);
                AnimatorSet outAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.rotate_out_anim);
                int distance = 16000;
                float scale = getResources().getDisplayMetrics().density * distance;
                iv_image.setCameraDistance(scale);
                tv_text.setCameraDistance(scale);
                outAnimator.setTarget(iv_image);
                inAnimator.setTarget(tv_text);
                outAnimator.start();
                inAnimator.start();
                outAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        iv_image.setVisibility(View.GONE);
                    }
                });
            }
        });
        tv_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_text.setVisibility(View.GONE);
                iv_image.setVisibility(View.VISIBLE);
            }
        });
    }
}

```



## 问题原因
　　Animator 动画会修改控件的属性值，第一次动画结束之后，控制的属性值就是动画播放完的属性，所以动画显示不正确。

## 解决方法
　　为动画设置播放监听，在动画结束之后，将控件的属性值设置为原始状态。
　　修改代码如下：
```java
    private void initView() {
        iv_image = (ImageView) findViewById(R.id.activity_main_image_iv);
        tv_text = (TextView) findViewById(R.id.activity_main_text_tv);
        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_text.setVisibility(View.VISIBLE);
                AnimatorSet inAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.rotate_in_anim);
                AnimatorSet outAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.rotate_out_anim);
                int distance = 16000;
                float scale = getResources().getDisplayMetrics().density * distance;
                iv_image.setCameraDistance(scale);
                tv_text.setCameraDistance(scale);
                outAnimator.setTarget(iv_image);
                inAnimator.setTarget(tv_text);
                outAnimator.start();
                inAnimator.start();
                outAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        iv_image.setVisibility(View.GONE);
      ***********************************修改部分**************************************
                        iv_image.setAlpha(1.0f);
                        iv_image.setRotationY(0.0f);
      ***********************************修改部分**************************************
                    }
                });
            }
        });
        tv_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_text.setVisibility(View.GONE);
                iv_image.setVisibility(View.VISIBLE);
            }
        });
    }
```
