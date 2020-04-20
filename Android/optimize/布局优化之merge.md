# 布局优化之 merge

## 1. 概述
　　merge 标签主要用于辅助 include 标签，在使用 include 后可能导致布局嵌套过多，多余的 layout 节点或导致解析变慢（可通过 hierarchy viewer 工具查看布局的嵌套情况）。

　　官方文档说明：merge 用于视图层级结构中的冗余视图，例如根布局是 LinearLayout，那么又 include 一个 LinearLayout 布局就没意义了，反而会减慢 UI 加载速度。

　　merge 就是为了减少在使用 include 布局文件时的层级。

## 2. merge 标签常用场景

1. 根布局是 FrameLayout 且不需要设置 background 或 padding 等属性，可以用 merge 代替，因为 Activity 的 ContentView 父元素就是 FrameLayout，所以可以用 merge 消除只剩一个。
2. 某布局作为子布局被其他布局 include 时，使用 merge 当做该布局的顶节点，这样在被引入时顶节点会自动被忽略，而将其子节点全部合并到主布局中。
3. 自定义 View 如果继承 LinearLayout(ViewGroup)，建议让自定义 View 的布局文件根布局设置成 merge，这样能少一层节点。

## 3. merge 使用注意

1. 因为 merge 标签并不是 View，所以在通过 LayoutInflate.inflate() 方法渲染的时候，第二个参数必须指定一个父容器，且第三个参数必须为 true，也就是必须为 merge 下的视图指定一个父亲节点。
2. 因为 merge 不是 View，所以对 merge 标签设置的所有属性都是无效的。
3. 注意如果 include 的 layout 用了 merge，调用 include 的根布局也使用了 merge 标签，那么就失去布局的属性了。
4. merge 标签必须使用在根布局。
5. ViewStub 标签中的 layout 布局不能使用 merge 标签。

## 4. merge 源码分析

　　相关的代码还是从 LayoutInflate 的 inflate() 函数中开始，从 include 的源码分析贴出的 inflate() 代码可以看到，如果标签是 merge 的话就会调用 rInflate() 方法解析子 view。

```java
    /**
     * Recursive method used to descend down the xml hierarchy and instantiate
     * views, instantiate their children, and then call onFinishInflate().
     * <p>
     * <strong>Note:</strong> Default visibility so the BridgeInflater can
     * override it.
     */
    void rInflate(XmlPullParser parser, View parent, Context context,
            AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
      
				// 省略部分代码，只贴出 merge 主要的代码
        ...
        final View view = createViewFromTag(parent, name, context, attrs);
      
				// 获取 merge 标签的 parent
        final ViewGroup viewGroup = (ViewGroup) parent;
      
				// 获取布局参数
        final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
      
				// 递归解析每个子元素
        rInflateChildren(parser, view, attrs, true);
      
				// 将子元素直接添加到 merge 标签的 parent view 中
        viewGroup.addView(view, params);
    }

```
　　如果是 merge 标签，那么直接将其中的子元素添加到 merge 标签的 parent 中，这样就保证了不会引入额外的层级。

## 5. 参考文章
1. [布局优化神器 include 、merge、ViewStub标签详解](https://blog.csdn.net/u012792686/article/details/72901531)
2. [Android布局优化之ViewStub、include、merge使用与源码分析](https://blog.csdn.net/bboyfeiyu/article/details/45869393)

