# 布局优化之 include

## 1. 概述

　　include 标签常用于将布局中的公共部分提取出来供其他 layout 共用，以实现布局模块化，也是平常设计布局时用的最多的。

　　include 就是为了解决重复定义相同布局的问题。

## 2. include 使用注意

1. 一个 xml 布局文件有多个 include 标签需要设置 ID，才能找到相应子 View 的控件，否则只能找到第一个 include 的 layout 布局，以及该布局的控件。
2. include 标签如果使用 layout_xx 属性，会覆盖被 include 的 xml 文件根节点对应的 layout_xx 属性，建议在 include 标签调用的布局设置好宽高位置，防止不必要的 bug。
3. 如果要在 include 标签下使用 RelativeLayout，如 layout_margin 等其他属性，记得要同时设置 layout_margin 等其他属性，记得要同时设置 layout_width 和 layout_height，不然其它属性会没反应。
4. include 添加 id，会覆盖被 include 的 xml 文件根节点 id，建议 include 和被 include 覆盖的 xml 文件根节点设置同名的 id，不然有可能会报空指针异常。
5. 如果 include 中设置了 id，那么就通过 include 的 id 来查找被 include 布局根元素的 View；如果 include 中没有设置 id，而被 include 的布局的根元素设置了 id，那么通过该根元素的 id 来查找该 view 即可。拿到根元素后查找其子控件都是一样的。

## 3. 源码分析

　　为什么使用根元素的 id 查找 view 就会报空指针呢？分析一下源码来查看。对于布局文件的解析，最终都会调用 LayoutInflater 的 inflater 方法。

### 3.1. LayoutInflater#inflater

```java
    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "inflate");

            final Context inflaterContext = mContext;
          	// 获取布局参数
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

              	// 如果是 merge 标签
                if (TAG_MERGE.equals(name)) {
                    if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }

                  	// 调用 rInflate 方法
                    rInflate(parser, root, inflaterContext, attrs, false);
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
										// 调用 rInflateChildren 方法加载子 view
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

　　在 inflate() 方法中调用了 rInflateChildren() 方法去加载布局的子 View。

### 3.2. LayoutInflater#rInflateChildren

```java
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

　　inflate() 方法就是调用了 rInflate() 方法。

### 3.3. LayoutInflater#rInflate

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

        final int depth = parser.getDepth();
        int type;

      	// 迭代 xml 中的所有元素，挨个解析 
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
              	// 调用 parseInclude 方法
                parseInclude(parser, context, parent, attrs);
            } else if (TAG_MERGE.equals(name)) {
                throw new InflateException("<merge /> must be the root element");
            } else {
              	// merge 标签会进入到这里
                final View view = createViewFromTag(parent, name, context, attrs);
                final ViewGroup viewGroup = (ViewGroup) parent;
                final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
              	// 调用 rInflateChildren 方法
                rInflateChildren(parser, view, attrs, true);
                viewGroup.addView(view, params);
            }
        }

        if (finishInflate) {
            parent.onFinishInflate();
        }
    }
```

　　rInflate() 方法其实就是遍历 xml 中的所有元素，然后挨个进行解析。

　　例如解析到一个标签，那么就根据用户设置的一些 layout_width、layout_height、id 等属性来构造一个 TextView 对象，然后添加到父控件（ViewGroup 类型）中。

　　include 标签也是一样的，当遇到 include 标签时，就调用了 parseInclude() 方法，就会对 include 标签进行解析。

### 3.4. LayoutInflater#parseInclude

```java
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
          
						// include 标签中没有设置 layout 属性，则抛出异常
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
                  
										// 1. 解析 include 中的第一个元素
                    final String childName = childParser.getName();
                  
										// 如果第一个元素是 merge 标签
                    if (TAG_MERGE.equals(childName)) {
                        // The <merge> tag doesn't support android:theme, so
                        // nothing special to do here.
                     	  // 那么调用 rInflate 函数解析
                        rInflate(childParser, parent, context, childAttrs, false);
                    } else {
                      
												// 2. 根据 include 的属性集，创建被 include 进来的 xml 布局的根 view
                        final View view = createViewFromTag(parent, childName,
                                context, childAttrs, hasThemeOverride);
                        final ViewGroup group = (ViewGroup) parent; // include 标签的 parent view

                        final TypedArray a = context.obtainStyledAttributes(
                                attrs, R.styleable.Include);
                      	// include 的 id
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
                          
														// 3. 获取布局属性
                            params = group.generateLayoutParams(attrs);
                        } catch (RuntimeException e) {
                            // Ignore, just fail over to child attrs.
                        }
                        if (params == null) {
                          	// params 是被 include 进来的根 view 设置布局参数
                            params = group.generateLayoutParams(childAttrs);
                        }
                        view.setLayoutParams(params);

                        // 4. Inflate all children.解析所有子控件
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

　　整个过程就是根据不同的标签解析不同的元素，首先会解析 include 元素，然后再解析被 include 进来的布局的 root view 元素。然后再解析 root view 下面的所有元素，这个过程是从上面注释的 2-4 的过程，然后是设置布局参数。

　　注视 5 处解释了为什么 include 标签和被引入的布局的根元素都设置了 id 的情况下，通过被引入的根元素的 id 来查找子控件会找不到的情况。可以看到，注释 5 处会判断 include 标签的 id 如果不是 View.NO_ID 的话会把该 id 设置给被引入的布局根元素的 id，因此再通过引入的根元素的 id 来查找根元素就会找不到了。

　　所以结论就是：如果 include 中设置了 id，那么就通过 include 的 id 来查找被 include 布局根元素的 View；如果 include 中没有设置 id，而被 include 的布局的根元素设置了 id，那么通过该根元素的 id 来查找该 View 即可。拿到根元素后查找其子控件都是一样的。

## 4. 参考文章
1. [布局优化神器 include 、merge、ViewStub标签详解](https://blog.csdn.net/u012792686/article/details/72901531)
2. [Android布局优化之ViewStub、include、merge使用与源码分析](https://blog.csdn.net/bboyfeiyu/article/details/45869393)

