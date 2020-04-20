# 布局优化之 ViewStub


## 1. 概述
　　ViewStub 标签最大的优点是当需要时才会加载，所以使用它并不会影响 UI 初始化的性能。各种不常用的布局像加载条、显示错误信息等可以使用 ViewStub 标签，以减少内存使用量，加快渲染速度。

　　ViewStub 是一个不可见的，实际上是把宽高设置为 0 的 View。效果有点类似普通的 view.setVisible()，但性能体验提高不少。

　　ViewStub 默认是不可见的，只有通过调用 setVisibility 函数或者 Inflate 函数才会将其要装载的目标布局给加载出来，从而达到延迟加载的效果，这个要被加载的布局通过 android:layout 属性来设置。

　　第一次初始化时，初始化的是 ViewStub View，当调用 inflate() 或 setVisibility() 后会被 remove 掉，然后再将其中的 layout 加到当前 view hierarchy 中。

## 2. 判断 ViewStub 是否已经加载过

1. 如果通过 setVisibility 来加载，那么通过判断可见性即可。
2. 如果通过 inflate() 来加载，判断 ViewStub 是否为 null 来判断。

## 3. ViewStub 标签使用注意

1. ViewStub 标签不支持 merge 标签
2. ViewStub 的 inflate 只能被调用一次，第二次调用会抛出异常，setVisibility 可以被调用多次，但不建议这么做（ViewStub 调用过后，可能给 GC 掉，再调用 setVisiblity() 会报异常）。
3. 为 ViewStub 赋值的 android:layout_xx 属性会替换待加载布局文件的根节点对应的属性。
4. 判断是否已经加载过，如果通过 setVisibility 来加载，那么通过判断可见性即可；如果通过 inflate() 来加载是不可以通过判断可见性来处理的，而需要判断 view 是否为 null 来进行判断。
5. findViewById 的问题，注意 ViewStub 中是否设置了 inflatedId，如果设置了则需要通过 inflateId 来查找目标布局的根元素。

## 4. ViewStub 源码分析

### 4.1. ViewStub 类

　　ViewStub 类部分代码：

```java
@RemoteView
public final class ViewStub extends View {
  
		...

    public ViewStub(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ViewStub, defStyleAttr, defStyleRes);
    
				// 获取 inflatedId 属性
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
        } else { 
          	//如果未加载，则加载目标布局
            super.setVisibility(visibility);
            if (visibility == VISIBLE || visibility == INVISIBLE) {
              
              	// 调用 inflate() 方法来加载目标布局
                inflate(); 
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
              // 获取 ViewStub 的 parent View，也是目标布局根元素的 parent view
                final ViewGroup parent = (ViewGroup) viewParent;
                final LayoutInflater factory;
                if (mInflater != null) {
                    factory = mInflater;
                } else {
                    factory = LayoutInflater.from(mContext);
                }
              
								// 1. 加载目标布局
                final View view = factory.inflate(mLayoutResource, parent,
                        false);
              
								// 2. 如果 ViewStub 的 inflatedId 不是 NO_ID ，则把 inflatedId 设置为目标布局根元的 id
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
　　可以看出，其实最终加载目标布局的还是 inflate() 函数，在该函数中将加载目标布局，获取到根元素后，如果 mInflatedId 不为 NO_ID 则把 mInflatedId 设置为根元素的 id，这也是为什么在获取根元素时会使用 ViewStub 的 inflatedId。如果没有设置 inflatedId 的话可以通过根元素的 id 来获取。

　　然后将 ViewStub 从 parent 中移除，将目标布局的根元素添加到 parent 中。最后会把目标布局的根元素返回。因此在调用 inflate() 函数时可以直接获得根元素，省掉了 findViewById 的过程。


## 5. Space 组件
　　在 ConstaintLayout 出来前，写布局会使用大量的 margin 或 padding，但是这种方式可读性会很差，加一个布局嵌套又会损耗性能，鉴于这种情况，可以使用 space，使用方式和 View 一样，不过主要用来占位置，不会有任何显示效果。

## 6. 参考文章
1. [布局优化神器 include 、merge、ViewStub标签详解](https://blog.csdn.net/u012792686/article/details/72901531)
2. [Android布局优化之ViewStub、include、merge使用与源码分析]( )

