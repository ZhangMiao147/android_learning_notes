# Android 的常见问题 8

# 1. ListView 源码分析

## 1.1. ListView 的观察者模式

ListView 的父类 AbsListView 有一个变量 AdapterDataSetObserver ，作为观察者，用来通知数据变换。

在 onAttachedToWindow() 方法中注册观察者，在 onDetachedFromWindow() 方法中解除观察者。

在调用了 notifyDataSetChanged() 方法后，会采用观察者模式通知 ListView 重新绘制界面。调用 AdapterDataSetObserver 的 onChanged() 方法，而 onChanged() 方法中调用了 requestLayout() 方法。而 reqyestLayout() 就是 View 的 requestLayout() 方法，调用 scheduleTraversals() 方法，依次调用 onMeasure()、onLayout()、onDraw() 方法。

## 1.2. ListView 的缓存机制

ListView 用两个列表来缓存正在显示的和解除绑定的 View，在复用时是先根据 position 获取正在显示的 view，如果没有再根据 itemId 从正在显示的 view 中获取 view，如果还没有，则根据之前显示的 position 从解除绑定的列表中获取，没有则直接拿解除绑定的最后一个，如果还没有，则调用 getView() 得到 view。

## 1.3. ListView 的优化

ListView 的优化分为两步，1 是 getView() 方法中参数 view 的复用，如果 view 为空则去创建新对象，如果不为空，则直接修改显示内容即可，2 是 view 的 findViewById() 的优化，采用一个 holder 类，对 view 包含的控件进行 findViewById() 后存储，将 holder 设置为 view 的 tag，减少每次的 findViewById 的操作。

# 2. RecyclerView 源码分析



# 3. Android事件分发机制，如何处理冲突； 

https://www.jianshu.com/p/d82f426ba8f7

dispatchToucEvent() -> 分发事件

onInterceptTouchEvent() -> 拦截事件

onTouchEvent() -> 消费事件

如何处理滑动冲突：

滑动冲突分为两种：方向一致和方向不一致。

方向不一致，可以根据滑动的 x 和 y 的距离来区分，比如说父控件是横向滑动，子控件是竖向滑动，那么滑动的 x 的距离大于滑动的 y 的距离，则拦截事件，父控件消费事件，反之则不拦截事件，事件交给子控件处理。

方向一致，需要根据需要来决定，比如说 ScrollView 里面嵌套了 ListView，处理的方法就是当 ListView 滑动到了顶部，并且继续向上滑，则 ScrollView 拦截事件，当 ListView 滑动到了底部，并且继续向下滑，则 ScrollView 拦截事件，其他情况不拦截事件。

# 4. webview有哪些问题？ 

https://www.jianshu.com/p/28e9d5fc05a4 安全方面的坑

* webview 隐藏接口问题（任意命令执行漏洞）

  android webview 组件包含 3 个隐藏的系统接口：searchBoxJavaBridge_、accessibilityTraversal 以及 accessibility，恶意程序可以通过反射机制利用它们实现远程代码执行；该问题在 Android 4.4 以下版本出现。

  于是，在 Android 3.0 到 4.4 之间的版本，通过移除这些隐藏接口，来解决该问题：

  ```java
      // 19  4.4  Build.VERSION.KITKAT
      // 11  3.0  Build.VERSION_CODES.HONEYCOMB
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB 
          && Build.VERSION.SDK_INT < 19 && webView != null) { 
          webView.removeJavascriptInterface("searchBoxJavaBridge_");
          webView.removeJavascriptInterface("accessibility");
          webView.removeJavascriptInterface("accessibilityTraversal");
      }
  ```

* addJavascriptInterface 任何命令执行漏洞

  在 webview 中使用 js 与 html 进行交互是一个不错的方式，但是，在 Android 4.2(16)及以下版本中，如果使用 addJavascriptInterface，则会存在被注入 js 接口的漏洞；在 4.2 之后，由于 Google 增加了 @JavascriptInterface，该漏洞得以解决。

  解决该问题，最彻底的方式是在 4.2 以下放弃使用 addJavascriptInterface，采用 onJsPrompt 或其他方法替换。或者使用一些方案来降低该漏洞导致的风险：如使用 https 并进行证书校验，如果是 http 则进行页面完整性校验，如上面所述移除隐藏接口等。

  ```java
      public boolean onJsPrompt(WebView view, String url, String message,String defaultValue, JsPromptResult result) {
          result.confirm(CGJSBridge.callJava(view, message));
          Toast.makeText(view.getContext(),"message="+message,Toast.LENGTH_LONG).show();
          return true;
      }
  ```

* 绕过证书校验漏洞

  webviewClient 中有 onReceivedError 方法，当出现证书校验错误时，可以在该方法中使用 handler.process() 来忽略证书校验继续加载网页，或者使用默认的 handler.cancel() 来终端加载。

  因为使用了 handler.proceed()，由此产生了该 “绕过证书校验漏洞”。

  如果确定所有页面都能满足证书校验，则不必要使用 handler.proceed()。

  ```java
      @SuppressLint("NewApi")
      @Override
      public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
          //handler.proceed();// 接受证书
          super.onReceivedSslError(view, handler, error);
      }
  ```

* allowFileAccess 导致的 File 域同源策略绕过漏洞

  如果 web view.getSetting().setAllowFileAccess(boolean) 设置为 true，则会面临该问题；该漏洞是通过 WebView 对 Javascript 的延时执行和 html 文件替换产生的。

  解决方法是禁止 WebView 页面打开本地文件，即

  ```java
      webview.getSettings().setAllowFileAccess(false);
  ```

  或者更直接的禁止使用 Javascript

  ```java
      webview.getSettings().setJavaScriptEnabled(false);
  ```

  

https://www.zhihu.com/question/31316646

1. WebViewClient.onPageFinished()。永远无法确定当 WebView 调用这个方法的时候，页面内容是否真的加载完毕了。当前正在加载的页面产生跳转的时候这个方法可能会被多次调用。当 WebView 需要加载各种各样的网页并且需要在页面加载完成时采取一些操作的话，可能 WebChromeClient.onProgressChanged() 比 WebViewClient.onPageFinished() 都要靠谱。

2. WebView 后台耗电问题。当程序调用了 WebView 加载页面，WebView 会自己开启一些线程，如果没有正确的将 WebView 销毁的话，这些残余的线程会一直在后台运行，由此导致你的应用程序耗电量居高不下。对此采用的处理方式简单粗暴（不建议），在 Activity.onDestory() 中直接调用 system.exit(0)，使得应用程序完全被移除虚拟机，这样就不会有任何问题了。

# 5. Bitmap图片优化； 

