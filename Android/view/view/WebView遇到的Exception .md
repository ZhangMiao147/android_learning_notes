# Web 遇到的 Exception

## MissingWebViewPackageException

遇到的 exception 为 ：

```
android.webkit.WebViewFactory$MissingWebViewPackageException

Failed to load WebView provider: No WebView installed
```

详细信息如下：

```
Caused by android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed
       at android.webkit.WebViewFactory.getWebViewContextAndSetProvider(WebViewFactory.java:428)
       at android.webkit.WebViewFactory.getProviderClass(WebViewFactory.java:493)
       at android.webkit.WebViewFactory.getProvider(WebViewFactory.java:348)
       at android.webkit.WebView.getFactory(WebView.java:2596)
       at android.webkit.WebView.ensureProviderCreated(WebView.java:2590)
       at android.webkit.WebView.setOverScrollMode(WebView.java:2677)
       at android.view.View.<init>(View.java:5567)
       at android.view.View.<init>(View.java:5742)
       at android.view.ViewGroup.<init>(ViewGroup.java:720)
       at android.widget.AbsoluteLayout.<init>(AbsoluteLayout.java:58)
       at android.webkit.WebView.<init>(WebView.java:423)
       at android.webkit.WebView.<init>(WebView.java:365)
       at android.webkit.WebView.<init>(WebView.java:347)
       at android.webkit.WebView.<init>(WebView.java:334)
       at android.webkit.WebView.<init>(WebView.java:324)
       at packagename.EnterFragmentKt$EnterScreen$5$1$1$3$1.invoke(:7)
       at packagename.EnterFragmentKt$EnterScreen$5$1$1$3$1.invoke(:2)
       at androidx.compose.ui.viewinterop.ViewFactoryHolder.setFactory(ViewFactoryHolder.java:13)
       at androidx.compose.ui.viewinterop.AndroidView_androidKt$AndroidView$1.invoke(:13)
       at androidx.compose.ui.viewinterop.AndroidView_androidKt$AndroidView$1.invoke()
       at androidx.compose.ui.viewinterop.AndroidView_androidKt$AndroidView$$inlined$ComposeNode$1.invoke(:2)
       at androidx.compose.runtime.ComposerImpl$createNode$2.invoke(ComposerImpl.java:2)
       at androidx.compose.runtime.ComposerImpl$createNode$2.invoke(ComposerImpl.java:6)
       at androidx.compose.runtime.ComposerImpl$recordInsert$2.invoke(ComposerImpl.java:2)
       at androidx.compose.runtime.ComposerImpl$recordInsert$2.invoke(ComposerImpl.java:6)
       at androidx.compose.runtime.CompositionImpl.applyChangesInLocked(CompositionImpl.java:60)
       at androidx.compose.runtime.CompositionImpl.applyChanges(CompositionImpl.java:5)
       at androidx.compose.runtime.Recomposer.composeInitial$runtime_release(:86)
       at androidx.compose.runtime.CompositionImpl.setContent(CompositionImpl.java:15)
       at androidx.compose.ui.platform.WrappedComposition$setContent$1.invoke(:89)
       at androidx.compose.ui.platform.WrappedComposition$setContent$1.invoke(:2)
       at androidx.compose.ui.platform.AndroidComposeView.setOnViewTreeOwnersAvailable(AndroidComposeView.java:11)
       at androidx.compose.ui.platform.WrappedComposition.setContent(:12)
       at androidx.compose.ui.platform.WrappedComposition.onStateChanged(:29)
       at androidx.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(:14)
       at androidx.lifecycle.LifecycleRegistry.forwardPass(LifecycleRegistry.java:69)
       at androidx.lifecycle.LifecycleRegistry.sync(LifecycleRegistry.java:72)
       at androidx.lifecycle.LifecycleRegistry.moveToState(LifecycleRegistry.java:2)
       at androidx.lifecycle.LifecycleRegistry.handleLifecycleEvent(LifecycleRegistry.java:9)
       at androidx.fragment.app.FragmentViewLifecycleOwner.handleLifecycleEvent(FragmentViewLifecycleOwner.java:2)
       at androidx.fragment.app.Fragment.restoreViewState(Fragment.java:43)
       at androidx.fragment.app.Fragment.restoreViewState(Fragment.java:35)
       at androidx.fragment.app.Fragment.performActivityCreated(Fragment.java:18)
       at androidx.fragment.app.FragmentStateManager.activityCreated(FragmentStateManager.java:2)
       at androidx.fragment.app.FragmentStateManager.moveToExpectedState(FragmentStateManager.java:2)
       at androidx.fragment.app.FragmentManager.executeOpsTogether(FragmentManager.java:230)
       at androidx.fragment.app.FragmentManager.removeRedundantOperationsAndExecute(FragmentManager.java:91)
       at androidx.fragment.app.FragmentManager.execPendingActions(FragmentManager.java:21)
       at androidx.fragment.app.FragmentManager$4.run(:3)
       at android.os.Handler.handleCallback(Handler.java:938)
       at android.os.Handler.dispatchMessage(Handler.java:99)
       at android.os.Looper.loopOnce(Looper.java:226)
       at android.os.Looper.loop(Looper.java:313)
       at android.app.ActivityThread.main(ActivityThread.java:8751)
       at java.lang.reflect.Method.invoke(Method.java)
       at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:571)
       at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1135)
```

解决方法是：

```java
public class MyWebView extends WebView {

    @Override
    public void setOverScrollMode(int mode) {
        try {
            super.setOverScrollMode(mode);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Failed to load WebView provider: No WebView installed")) {
                e.printStackTrace();
            } else {
                throw e;
            }
        }
    }
}
```



