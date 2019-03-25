# You need to use a Theme.AppCompat theme (or descendant) with this activity 问题解决

## 问题代码
```
    void initCommitDialog(String message) {
        mCommitDialog = new AlertDialog.Builder(context).create();
        mCommitDialog.setCanceledOnTouchOutside(false);
        mCommitDialog.show();
        mCommitDialog.getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.papers_commit_main_width), getResources().getDimensionPixelSize(R.dimen.papers_commit_main_height));
        mCommitDialog.getWindow().setContentView(R.layout.dialog_zhenti_papers_commit);(问题日志指出这行代码有问题)
        mCommitDialog.getWindow().setBackgroundDrawableResource(R.drawable.radius_20_white_bg);
        TextView tv_message = (TextView) mCommitDialog.getWindow().findViewById(R.id.dialog_zhenti_papers_commit_message_tv);
        tv_message.setText(message);
        mCommitDialog.getWindow().findViewById(R.id.dialog_zhenti_papers_commit_cacel_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommitDialog.dismiss();
            }
        });
        mCommitDialog.getWindow().findViewById(R.id.dialog_zhenti_papers_commit_submit_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setCommitWhatEver();
                mPresenter.commit(null);
            }
        });
    }
```

## 问题日志
```
09-02 15:58:26.854 11821 11821 E AndroidRuntime: java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AppCompatDelegateImplV9.y(AppCompatDelegateImplV9.java:359)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AppCompatDelegateImplV9.x(AppCompatDelegateImplV9.java:328)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AppCompatDelegateImplV9.b(AppCompatDelegateImplV9.java:289)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AppCompatDialog.setContentView(AppCompatDialog.java:83)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AlertController.a(AlertController.java:226)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.support.v7.app.AlertDialog.onCreate(AlertDialog.java:259)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.app.Dialog.dispatchOnCreate(Dialog.java:373)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.app.Dialog.show(Dialog.java:274)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at cn.dream.ebag.classService.ui.exercise.paperView.PapersFragment.d(PapersFragment.java:592)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at cn.dream.ebag.classService.ui.exercise.paperView.PapersFragment.h(PapersFragment.java:579)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at cn.dream.ebag.classService.ui.exercise.paperView.e.a(PapersPresenter.java:102)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at cn.dream.ebag.classService.ui.exercise.paperView.PapersFragment.commit(PapersFragment.java:571)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at cn.dream.ebag.classService.ui.exercise.paperView.PapersFragment_ViewBinding$2.doClick(PapersFragment_ViewBinding.java:57)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at butterknife.internal.DebouncingOnClickListener.onClick(DebouncingOnClickListener.java:22)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.view.View.performClick(View.java:4781)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.view.View$PerformClick.run(View.java:19874)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.os.Handler.handleCallback(Handler.java:739)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.os.Handler.dispatchMessage(Handler.java:95)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.os.Looper.loop(Looper.java:135)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at android.app.ActivityThread.main(ActivityThread.java:5254)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Native Method)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Method.java:372)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:902)

09-02 15:58:26.854 11821 11821 E AndroidRuntime: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:697)

```
　　从日志可以看到问题的解决方法是给当前的 activity 使用 Theme.AppCompat 主题。

## 解决方法
　　给当前的 Activity 使用 ThemeAppCompat 主题。
　　**未修改之前的主题**
```
    <style name="AppTheme" parent="android:Theme.Holo.Light.NoActionBar.Fullscreen">
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowEnableSplitTouch">false</item>
        <item name="android:splitMotionEvents">false</item>
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowAnimationStyle">@style/ActivityAnimStyle</item>
    </style>
```
　　**修改之后的主题**
```
    <style name="ServiceAppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowBackground">@color/translate</item>
        <item name="android:windowAnimationStyle">@style/ActivityAnimStyle</item>
        <item name="android:windowActionBar">false</item>
        <item name="android:windowNoTitle">true</item>
    </style>
```
　　更换主题之后，问题得到解决。

## 查看资源

　　http://anforen.com/wp/2016/04/alertdialog-java-lang-illegalstateexception-you-need-to-use-a-theme-appcompat-theme-or-descendant-with-this-activity/
　　https://blog.csdn.net/jyw935478490/article/details/72868751


