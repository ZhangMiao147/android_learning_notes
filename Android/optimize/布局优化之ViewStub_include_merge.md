# 布局优化之 ViewStub、include、merge

## include 标签
　　include 标签常用语将布局中的公共部分提取出来供其他 layout 共用，以实现布局模块化，也是平常设计布局时用的最多的。include 就是为了解决重复定义相同布局的问题。

#### include 使用注意
1. 一个 xml 布局文件有多个 include 标签需要设置 ID，才能找到相应子 View 的控件，否则只能找到第一个 include 的 layout 布局，以及该布局的控件。
2. include 标签如果使用 layout_xx 属性，会覆盖被 include 的 xml 文件根节点对应的 layout_xx 属性，建议在 include 标签调用的布局设置好宽高位置，防止不必要的 bug。
3. include 添加 id，会覆盖被 include 的 xml 文件根节点 ID，建议 include 和被 include 覆盖的 xml 文件根节点设置同名的 ID，不然有可能会报空指针异常。
4. 如果要在 include 标签下使用 RelativeLayout，如 layout_margin 等其他属性，记得要同时设置 layout_margin 等其他属性，记得要同时设置 layout_width 和 layout_height，不然其他属性会没反应。
5. 如果 include 中设置了 id，那么就通过 include 的 id 来查找被 include 布局根元素的 View；如果 include 中没有设置 id，而被 include 的布局的根元素设置了 id，那么通过该根元素的 id 来查找该 view 即可。拿到根元素后查找其子控件都是一样的。

#### 源码分析
　　为什么使用根元素的 id 查找 view 就会包空指针呢？分析一下源码来查看。对于布局文件的解析，最终都会调用 LayoutInflater 的 inflater 方法：
```
    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "inflate");

            final Context inflaterContext = mContext;
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) mConstructorArgs[0];
            mConstructorArgs[0] = inflaterContext;
            View result = root;

            try {
                // Look for the root node.
                int type;
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty
                }

                if (type != XmlPullParser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription()
                            + ": No start tag found!");
                }

                final String name = parser.getName();

                if (TAG_MERGE.equals(name)) {
                    ...
                } else {
                    // Temp is the root view that was found in the xml
                    final View temp = createViewFromTag(root, name, inflaterContext, attrs);

                    ViewGroup.LayoutParams params = null;

                    if (root != null) {
                        // Create layout params that match root, if supplied
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            // Set the layout params for temp if we are not
                            // attaching. (If we are, we use addView, below)
                            temp.setLayoutParams(params);
                        }
                    }
                    // Inflate all children under temp against its context.
					// 加载子 view
                    rInflateChildren(parser, temp, attrs, true);

                    // We are supposed to attach all the views we found (int temp)
                    // to root. Do that now.
                    if (root != null && attachToRoot) {
                        root.addView(temp, params);
                    }
					...
                }

            }
			...

            return result;
        }
    }
```
　　在 inflate() 方法中调用了 rInflateChildren() 方法去加载布局的子 View：
```
    /**
     * Recursive method used to inflate internal (non-root) children. This
     * method calls through to {@link #rInflate} using the parent context as
     * the inflation context.
     * <strong>Note:</strong> Default visibility so the BridgeInflater can
     * call it.
     */
    final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs,
            boolean finishInflate) throws XmlPullParserException, IOException {
        rInflate(parser, parent, parent.getContext(), attrs, finishInflate);
    }
```
　　inflate() 方法就是调用了 rInflate() 方法：
```
    /**
     * Recursive method used to descend down the xml hierarchy and instantiate
     * views, instantiate their children, and then call onFinishInflate().
     * <p>
     * <strong>Note:</strong> Default visibility so the BridgeInflater can
     * override it.
     */
    void rInflate(XmlPullParser parser, View parent, Context context,
            AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {

        final int depth = parser.getDepth();
        int type;

        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();

            if (TAG_REQUEST_FOCUS.equals(name)) {
                parseRequestFocus(parser, parent);
            } else if (TAG_TAG.equals(name)) {
                parseViewTag(parser, parent, attrs);
            } else if (TAG_INCLUDE.equals(name)) {
				// 如果是 include 节点
                if (parser.getDepth() == 0) {
                    throw new InflateException("<include /> cannot be the root element");
                }
                parseInclude(parser, context, parent, attrs);
            } else if (TAG_MERGE.equals(name)) {
                throw new InflateException("<merge /> must be the root element");
            } else {
                final View view = createViewFromTag(parent, name, context, attrs);
                final ViewGroup viewGroup = (ViewGroup) parent;
                final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                rInflateChildren(parser, view, attrs, true);
                viewGroup.addView(view, params);
            }
        }

        if (finishInflate) {
            parent.onFinishInflate();
        }
    }
```
　　rInflate() 方法中通过判断界面名字，如果是 include 界面，就调用了 parseInclude() 方法：
```
    private void parseInclude(XmlPullParser parser, Context context, View parent,
            AttributeSet attrs) throws XmlPullParserException, IOException {
        int type;

        if (parent instanceof ViewGroup) {
            // Apply a theme wrapper, if requested. This is sort of a weird
            // edge case, since developers think the <include> overwrites
            // values in the AttributeSet of the included View. So, if the
            // included View has a theme attribute, we'll need to ignore it.
            final TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            final int themeResId = ta.getResourceId(0, 0);
            final boolean hasThemeOverride = themeResId != 0;
            if (hasThemeOverride) {
                context = new ContextThemeWrapper(context, themeResId);
            }
            ta.recycle();

            // If the layout is pointing to a theme attribute, we have to
            // massage the value to get a resource identifier out of it.
            int layout = attrs.getAttributeResourceValue(null, ATTR_LAYOUT, 0);
			//include 标签中没有这事 layout 属性，则抛出异常
            if (layout == 0) {
                final String value = attrs.getAttributeValue(null, ATTR_LAYOUT);
                if (value == null || value.length() <= 0) {
                    throw new InflateException("You must specify a layout in the"
                            + " include tag: <include layout=\"@layout/layoutID\" />");
                }

                // Attempt to resolve the "?attr/name" string to an identifier.
                layout = context.getResources().getIdentifier(value.substring(1), null, null);
            }

            // The layout might be referencing a theme attribute.
            if (mTempValue == null) {
                mTempValue = new TypedValue();
            }
            if (layout != 0 && context.getTheme().resolveAttribute(layout, mTempValue, true)) {
                layout = mTempValue.resourceId;
            }

            if (layout == 0) {
                final String value = attrs.getAttributeValue(null, ATTR_LAYOUT);
                throw new InflateException("You must specify a valid layout "
                        + "reference. The layout ID " + value + " is not valid.");
            } else {
                final XmlResourceParser childParser = context.getResources().getLayout(layout);
                try {
					// 获取属性值，即在 include 标签中设置的属性
                    final AttributeSet childAttrs = Xml.asAttributeSet(childParser);

                    while ((type = childParser.next()) != XmlPullParser.START_TAG &&
                            type != XmlPullParser.END_DOCUMENT) {
                        // Empty.
                    }

                    if (type != XmlPullParser.START_TAG) {
                        throw new InflateException(childParser.getPositionDescription() +
                                ": No start tag found!");
                    }
					//1. 解析 include 中的第一个元素
                    final String childName = childParser.getName();
					//如果第一个元素是 merge 标签
                    if (TAG_MERGE.equals(childName)) {
                        // The <merge> tag doesn't support android:theme, so
                        // nothing special to do here.
                        rInflate(childParser, parent, context, childAttrs, false);
                    } else {
						// 2/ 根据 include 的属性集创建被 include 进来的 xml 布局的根 view
                        final View view = createViewFromTag(parent, childName,
                                context, childAttrs, hasThemeOverride);
                        final ViewGroup group = (ViewGroup) parent; // include 标签的 parent view

                        final TypedArray a = context.obtainStyledAttributes(
                                attrs, R.styleable.Include);
                        final int id = a.getResourceId(R.styleable.Include_id, View.NO_ID);
                        final int visibility = a.getInt(R.styleable.Include_visibility, -1);
                        a.recycle();

                        // We try to load the layout params set in the <include /> tag.
                        // If the parent can't generate layout params (ex. missing width
                        // or height for the framework ViewGroups, though this is not
                        // necessarily true of all ViewGroups) then we expect it to throw
                        // a runtime exception.
                        // We catch this exception and set localParams accordingly: true
                        // means we successfully loaded layout params from the <include>
                        // tag, false means we need to rely on the included layout params.
                        ViewGroup.LayoutParams params = null;
                        try {
							// 3. 获取布局实行
                            params = group.generateLayoutParams(attrs);
                        } catch (RuntimeException e) {
                            // Ignore, just fail over to child attrs.
                        }
                        if (params == null) {
                            params = group.generateLayoutParams(childAttrs);
                        }
                        view.setLayoutParams(params);

                        // Inflate all children.解析所有子控件
                        rInflateChildren(childParser, view, childAttrs, true);

						//5. 将 include 中设置的 id 设置给根 view
						// 因此实际上根 view 的 id 会变成 include 标签中的 id。
                        if (id != View.NO_ID) {
                            view.setId(id);
                        }

                        switch (visibility) {
                            case 0:
                                view.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                view.setVisibility(View.INVISIBLE);
                                break;
                            case 2:
                                view.setVisibility(View.GONE);
                                break;
                        }
						//6. 将根 view 添加到父控件
                        group.addView(view);
                    }
                } finally {
                    childParser.close();
                }
            }
        } else {
            throw new InflateException("<include /> can only be used inside of a ViewGroup");
        }

        LayoutInflater.consumeChildElements(parser);
    }
```
　　整个过程就是根据不同的标签解析不同的元素，首先会解析 include 元素，然后再解析被 include 进来的布局的 root view 元素。在代码中会判断 include 标签的 id 如果不是 View.NO_ID 的话会把 include 的 id 设置给被引入的布局根元素的 id。

## merge 标签
　　merge 标签主要用于辅助 include 标签，在使用 include 后可能导致布局嵌套过多，多余的 layout 节点或导致解析变慢（可通过 hierarchy viewer 工具查看布局的嵌套情况）。

　　官方文档说明：merge 用于视图层级结构中的冗余视图，例如根布局是 LinearLayout，那么又 include 一个 LinearLayout 布局就没意义了，反而会减慢 UI 加载速度。

　　减少在 include 布局文件时的层级。

#### merge 标签常用场景
1. 根布局是 FrameLayout 且不需要设置 background 或 padding 等属性，可以用 merge 代替，因为 Activity 的 ContentView 父元素就是 FrameLayout，所以可以用 merge 消除只剩一个。
2. 某布局作为子布局被其他布局 include 时，使用 merge 当做该布局的顶节点，这样在被引入时顶节点会自动被忽略，而将其子节点全部合并到主布局中。
3. 自定义 View 如果继承 LinearLayout(ViewGroup)，建议让自定义 View 的布局文件根布局设置成 merge，这样能少一层节点。

#### merge 使用注意
1. 因为 merge 标签并不是 View，所以在通过 LayoutInflate.inflate() 方法渲染的时候，第二个参数必须指定一个父容器，且第三个参数必须为 true，也就是必须为 merge 下的视图指定一个父亲节点。
2. 因为 merge 不是 View，所以对 merge 标签设置的所有属性都是无效的。
3. 注意如果 include 的 layout 用了 merge，调用 include 的根布局也使用了 merge 标签，那么就失去布局的属性了。
4. merge 标签必须使用在根布局。
5. ViewStub 标签中的 layout 布局不能使用 merge 标签。

#### merge 源码分析
　　相关的代码还是从 LayoutInflate 的 inflate() 函数中开始，从前面 include 的源码分析贴出的 inflate() 代码可以看到，如果标签是 merge 的话就会调用 rInflate() 方法解析子 view：
```
    /**
     * Recursive method used to descend down the xml hierarchy and instantiate
     * views, instantiate their children, and then call onFinishInflate().
     * <p>
     * <strong>Note:</strong> Default visibility so the BridgeInflater can
     * override it.
     */
    void rInflate(XmlPullParser parser, View parent, Context context,
            AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {
		// 解析 include 代码有 rInflate() 方法代码，所以省略部分代码，只贴出 merge 主要的代码
        ...
        final View view = createViewFromTag(parent, name, context, attrs);
		//获取 merge 标签的 parent
        final ViewGroup viewGroup = (ViewGroup) parent;
		//获取布局参数
        final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
		//递归解析每个子元素
        rInflateChildren(parser, view, attrs, true);
		//将子元素直接添加到 merge 标签的 parent view 中
        viewGroup.addView(view, params);
    }

```
　　如果是 merge 标签，那么直接将其中的子元素添加到 merge 标签的 parent 中，这样就保证了不会引入额外的层级。


## ViewStub 标签
　　ViewStub 标签最大的优点是当你需要时才会加载，使用它并不会影响 UI 初始化的性能。各种不常用的布局像加载条、显示错误信息等可以使用 ViewStub 标签，以减少内存使用量，加快渲染速度。ViewStub 是一个不可见的，实际上是把宽高设置为 0 的 View。效果有点类似普通的 view.setVisible()，但性能体验提高不少。

　　第一次初始化时，初始化的是 ViewStub View，当调用 inflate() 或 setVisibility() 后会被 remove 掉，然后再将其中的 layout 加到当前 view hierarchy 中。

　　ViewStub 就是一个宽高为 0 的一个 View，它默认是不可见的，只有通过调用 setVisibility 函数或者 Inflate 函数才会将其要装载的目标布局给加载出来，从而达到延迟加载的效果，这个要被加载的布局通过 android:layout 属性来设置。

#### 判断 ViewStub （做单例）是否已经加载过：
1. 如果通过 setVisibility 来加载，那么通过判断可见性即可。
2. 如果通过 inflate() 来加载，判断 ViewStub 的 ID 是否为 null 来判断。

#### ViewStub 标签使用注意
1. ViewStub 标签不支持 merge 标签
2. ViewStub 的 inflate 只能被调用一次，第二次调用会抛出异常，setVisibility 可以被调用多次，但不建议这么做（ViewStub 调用过后，可能给 GC 掉，再调用 setVisiblity() 会报异常）。
3. 为 ViewStub 赋值的 android:layout_xx 属性会替换待加载布局文件的根节点对应的属性。
4. 判断是否已经加载过，如果通过 setVisibility 来加载，那么通过判断可见性即可；如果通过 inflate() 来加载是不可以通过判断可见性来处理的，而需要判断 view 是否为 null 来进行判断。
5. findViewById 的问题，注意 ViewStub 中是否设置了 inflatedId，如果设置了则需要通过 inflateId 来查找目标布局的根元素。

#### ViewStub 源码分析
　　ViewStub 类部分代码：
```
@RemoteView
public final class ViewStub extends View {
	...

    public ViewStub(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ViewStub, defStyleAttr, defStyleRes);
		//获取 inflatedId 属性
        mInflatedId = a.getResourceId(R.styleable.ViewStub_inflatedId, NO_ID);
        mLayoutResource = a.getResourceId(R.styleable.ViewStub_layout, 0);
        mID = a.getResourceId(R.styleable.ViewStub_id, NO_ID);
        a.recycle();

        setVisibility(GONE);
        setWillNotDraw(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0); // 宽高都是 0
    }

    /**
     * When visibility is set to {@link #VISIBLE} or {@link #INVISIBLE},
     * {@link #inflate()} is invoked and this StubbedView is replaced in its parent
     * by the inflated layout resource. After that calls to this function are passed
     * through to the inflated view.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     *
     * @see #inflate()
     */
    @Override
    @android.view.RemotableViewMethod
    public void setVisibility(int visibility) {
        if (mInflatedViewRef != null) { //如果已经加载过则只设置 Visibility 属性
            View view = mInflatedViewRef.get();
            if (view != null) {
                view.setVisibility(visibility);
            } else {
                throw new IllegalStateException("setVisibility called on un-referenced view");
            }
        } else { //如果未加载，则加载目标布局
            super.setVisibility(visibility);
            if (visibility == VISIBLE || visibility == INVISIBLE) {
                inflate(); // 调用 inflate() 方法来加载目标布局
            }
        }
    }

    /**
     * Inflates the layout resource identified by {@link #getLayoutResource()}
     * and replaces this StubbedView in its parent by the inflated layout resource.
     *
     * @return The inflated layout resource.
     *
     */
    public View inflate() {
        final ViewParent viewParent = getParent();

        if (viewParent != null && viewParent instanceof ViewGroup) {
            if (mLayoutResource != 0) {
                final ViewGroup parent = (ViewGroup) viewParent;
                final LayoutInflater factory;
                if (mInflater != null) {
                    factory = mInflater;
                } else {
                    factory = LayoutInflater.from(mContext);
                }
				//1. 加载目标布局
                final View view = factory.inflate(mLayoutResource, parent,
                        false);
				//2. 如果 ViewStub 的 inflatedId 不是 NO_ID ，则把 inflatedId 设置为目标布局根元的 id
                if (mInflatedId != NO_ID) {
                    view.setId(mInflatedId);
                }

                final int index = parent.indexOfChild(this);
				// 3.将 ViewStub 自身从 parent 中移除
                parent.removeViewInLayout(this);

                final ViewGroup.LayoutParams layoutParams = getLayoutParams();
				// 4. 将目标布局的根元素添加到 parent 中
                if (layoutParams != null) {
                    parent.addView(view, index, layoutParams);
                } else {
                    parent.addView(view, index);
                }

                mInflatedViewRef = new WeakReference<View>(view);

                if (mInflateListener != null) {
                    mInflateListener.onInflate(this, view);
                }

                return view;
            } else {
                throw new IllegalArgumentException("ViewStub must have a valid layoutResource");
            }
        } else {
            throw new IllegalStateException("ViewStub must have a non-null ViewGroup viewParent");
        }
    }
}
```
　　其实最终加载目标布局的还是 inflate() 函数，在该函数中将加载目标布局，获取到根元素后，如果 mInflatedId 不为 NO_ID 则把 mInflatedId 设置为根元素的 id，这也是为什么在获取根元素时会使用 ViewStub 的 inflatedId。如果没有设置 inflatedId 的话可以通过根元素的 id 来获取。然后将 ViewStub 从 parent 中移除，将目标布局的根元素添加到 parent 中。最后会把目标布局的根元素返回。


## Space 组件
　　在 ConstaintLayout 出来前，写布局会使用大量的 margin 或 padding，但是这种方式可读性会很差，加一个布局嵌套又会损耗性能，鉴于这种情况，可以使用 space，使用方式和 View 一样，不过主要用来占位置，不会有任何显示效果。



























## 参考文章
[布局优化神器 include 、merge、ViewStub标签详解](https://blog.csdn.net/u012792686/article/details/72901531)
[Android布局优化之ViewStub、include、merge使用与源码分析](https://blog.csdn.net/bboyfeiyu/article/details/45869393)

