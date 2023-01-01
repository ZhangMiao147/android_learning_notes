# 自定义 View 的实现方式

　　如果要按类型来划分的话，自定义 View 的实现方式大概可以分为三种，自绘控件、组合控件以及继承控件。

## 1. 自绘控件

　　自绘控件的意思就是，这个 View 上所展现的内容全部都是自己绘制出来的。

　　绘制的代码是写在 onDraw() 方法中的。

### 1.1. 自定义计数器 View

　　自定义一个计数器 View，这个 View 可以响应用户的点击事件，并自动记录一共点击了多少次。

　　新建一个 ContentView 继承自 View，代码如下：

```java
public class CounterView extends View implements OnClickListener {
 
	private Paint mPaint;
	
	private Rect mBounds;
 
	private int mCount;
	
	public CounterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBounds = new Rect();
		setOnClickListener(this);
	}
 
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setColor(Color.BLUE);
		canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
		mPaint.setColor(Color.YELLOW);
		mPaint.setTextSize(30);
		String text = String.valueOf(mCount);
		mPaint.getTextBounds(text, 0, text.length(), mBounds);
		float textWidth = mBounds.width();
		float textHeight = mBounds.height();
		canvas.drawText(text, getWidth() / 2 - textWidth / 2, getHeight() / 2
				+ textHeight / 2, mPaint);
	}
 
	@Override
	public void onClick(View v) {
		mCount++;
		invalidate();
	}
 
}
```

　　可以看到，首先在 CounterView 的构造函数中初始化了一些数据，并给这个 View 的本身注册了点击事件，这样当 CounterView 被点击的时候，onClick() 方法就会得到调用。而 onClick() 方法中的逻辑只是对 mCount 这个计数器加 1，然后调用 invalidate() 方法。调用 invalidate() 方法会导致视图进行重绘，因此 onDraw() 方法将会得到调用。

　　既然 CounterView 是一个自绘视图，那么最主要的逻辑就是在 onDraw() 方法里了。这里首先是将 Paint 画笔设置为蓝色，然后调用 Canvas 的 drawRect() 方法绘制一个矩形，这个矩形就是 ConterView 的背景图。接着将画笔设置为黄色，准备在背景上面绘制当前的计数，注意这里先是调用了 getTextBounds() 方法来获取到文字的宽度和高度，然后调用了 drawText() 方法去进行绘制就可以了。

　　这样，一个自定义的 View 就已经完成了，并且这个 CounterView 是具备自动计数功能的。使用 CounterView 和使用普通的控件一样。比如在布局文件中加入如下代码：

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
 
    <com.example.customview.CounterView
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_centerInParent="true" />
 
</RelativeLayout>
```

　　可以看到，这里将 CounterView 放入到了一个 RelativeLayout 中，然后可以像使用普通控件来给 CounterView 指定各种属性，比如通过 layout_width 和 layout_height 来指定 CounterView 的宽高，通过 android:layout_centerInParent 来指定它在布局里居中显示。

　　只不过需要注意，自定义的 View 在使用的时候一定要写出完整的包名，不然系统将无法找到这个 View。

## 2. 组合控件

　　组合控件的意思就是，并不需要自己去绘制视图上显示的内容，而只是用系统原生的控件就好了，但可以将几个系统原生的控件组合在一起，这样创建出的控件就被称为组合控件。

### 2.1. 标题栏

　　标题栏是一个很常见的控件，很多界面的头部都会放置一个标题栏，标题栏上会有一个返回按钮和标题，点击按钮后就可以返回到上一个界面。

　　新建一个 title.xml 布局文件，代码如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="50dp"
    android:background="#ffcb05" >
 
    <Button
        android:id="@+id/button_left"
        android:layout_width="60dp"
        android:layout_height="40dp"
        android:layout_centerVertical="true"
        android:layout_marginLeft="5dp"
        android:background="@drawable/back_button"
        android:text="Back"
        android:textColor="#fff" />
 
    <TextView
        android:id="@+id/title_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:text="This is Title"
        android:textColor="#fff"
        android:textSize="20sp" />
 
</RelativeLayout>
```

　　在这个布局文件中，首先定义了一个 RelativeLayout 作为背景布局，然后在这个布局里定义了一个 Button 和一个 TextView，Button 就是标题栏中的返回按钮，TextView 就是标题栏中的显示的文字。

　　接着创建一个 TitleView 继承自 FrameLayout，代码如下所示：

```java
public class TitleView extends FrameLayout {
 
	private Button leftButton;
 
	private TextView titleText;
 
	public TitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.title, this);
		titleText = (TextView) findViewById(R.id.title_text);
		leftButton = (Button) findViewById(R.id.button_left);
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).finish();
			}
		});
	}
 
	public void setTitleText(String text) {
		titleText.setText(text);
	}
 
	public void setLeftButtonText(String text) {
		leftButton.setText(text);
	}
 
	public void setLeftButtonListener(OnClickListener l) {
		leftButton.setOnClickListener(l);
	}
 
}
```

　　TitleView 中的代码非常简单，在 TitleView 的构建方法中，调用了 LayoutInflater 的 inflate() 方法来加载刚刚定义的 title.xml 布局。

　　接下来调用 findViewById() 方法获取到了返回按钮的实例，然后在它的 onClick 事件中调用 finish() 方法来关闭当前的 Activity，也就相当于实现返回功能了。

　　另外，为了让 TitleView 有更强的扩展性，还提供了 setTitleText()、setLeftButtonText()、setLeftButtonListener() 等方法，分别用于设置标题栏上的文字，返回按钮上的文字以及返回按钮的点击事件。

　　一个自定义的标题栏就完成了，引用这个自定义 View 的方法和基本方法是相同的，在布局文件中添加如下代码：

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
 
    <com.example.customview.TitleView
        android:id="@+id/title_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" >
    </com.example.customview.TitleView>
 
</RelativeLayout>
```

　　这样就成功将一个标题栏控件引入到布局文件中了。

　　点击一个 Back 按钮，就可以关闭当前的 Activity 了。如果想要修改标题栏上显示的内容，或者返回按钮的默认事件，只需要在 Activity 中通过 findViewById() 方法得到 TitleView 的实例，然后调用 setTitleText()、setLeftButtonText()、setLeftButtonListener() 等方法进行设置就 OK 了。

## 3. 继承控件

　　继承控件的意思就是并不需要自己从头去实现一个控件，只需要去继承一个现有的控件，然后在这个空间上增加一些新的功能，就可以形成一个自定义的控件了。

　　这种自定义控件的特点就是不仅能够按照需求加入相应的功能，还可以保留原生控件的所有功能。

### 3.1. 有删除按钮的 ListView

　　对 ListView 进行扩展，加入在 ListView 上滑动就可以显示出一个删除按钮，点击按钮就会删除相应数据的功能。

　　首先需要准备一个删除按钮的布局，新建 delete_button.xml 文件，代码如下所示：

```xml
<?xml version="1.0" encoding="utf-8"?>
<Button xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/delete_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/delete_button" >
 
</Button>
```

　　这个布局文件很简单，只是一个按钮而已，并且给这个按钮指定了一张删除背景图。

　　接着创建 MyListView 继承自 ListView，代码如下：

```java
public class MyListView extends ListView implements OnTouchListener,
		OnGestureListener {
 
	private GestureDetector gestureDetector;
 
	private OnDeleteListener listener;
 
	private View deleteButton;
 
	private ViewGroup itemLayout;
 
	private int selectedItem;
 
	private boolean isDeleteShown;
 
	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(getContext(), this);
		setOnTouchListener(this);
	}
 
	public void setOnDeleteListener(OnDeleteListener l) {
		listener = l;
	}
 
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isDeleteShown) {
			itemLayout.removeView(deleteButton);
			deleteButton = null;
			isDeleteShown = false;
			return false;
		} else {
			return gestureDetector.onTouchEvent(event);
		}
	}
 
	@Override
	public boolean onDown(MotionEvent e) {
		if (!isDeleteShown) {
			selectedItem = pointToPosition((int) e.getX(), (int) e.getY());
		}
		return false;
	}
 
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (!isDeleteShown && Math.abs(velocityX) > Math.abs(velocityY)) {
			deleteButton = LayoutInflater.from(getContext()).inflate(
					R.layout.delete_button, null);
			deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					itemLayout.removeView(deleteButton);
					deleteButton = null;
					isDeleteShown = false;
					listener.onDelete(selectedItem);
				}
			});
			itemLayout = (ViewGroup) getChildAt(selectedItem
					- getFirstVisiblePosition());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			itemLayout.addView(deleteButton, params);
			isDeleteShown = true;
		}
		return false;
	}
 
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
 
	@Override
	public void onShowPress(MotionEvent e) {
 
	}
 
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
 
	@Override
	public void onLongPress(MotionEvent e) {
	}
	
	public interface OnDeleteListener {
 
		void onDelete(int index);
 
	}
 
}
```

　　在 MyListView 的构造方法中创建了一个 GestureDetector 的实例用于监听手势，然后给 MyListView 注册了 touch 监听事件。然后在 onTouch() 方法中进行判断，如果删除按钮已经显示了，就将它移除掉，如果删除按钮没有显示，就使用 GestureDetector 来处理当前手势。

　　当手指按下时，会调用 OnGestureListener 的 onDown() 方法，在这里通过 pointToPosition() 方法来判断出当前选中的是 ListView 的哪一行。当手指快速滑动时，会调用 onFling() 方法，在这里会去加载 delete_button.xml 这个布局，然后将删除按钮添加到当前选中的那一行 item 上。注意，还给删除按钮添加了一个点击事件，当点击了删除按钮时就会回调 onDeleteListener 的 onDelete() 方法，在回调方法中应该去处理具体的删除操作。

　　自定义 View 的功能到此就完成了，接下来就是使用这个自定义 View。首先需要创建一个 ListView 子项的布局文件，新建 my_list_view_item.xml，代码如下所示：

```xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:descendantFocusability="blocksDescendants"
    android:orientation="vertical" >
 
    <TextView
        android:id="@+id/text_view"
        android:layout_width="wrap_content"
        android:layout_height="50dp"
        android:layout_centerVertical="true"
        android:gravity="left|center_vertical"
        android:textColor="#000" />
 
</RelativeLayout>
```

　　然后创建一个适配器 MyAdapter，在这个适配器中去加载 my_list_view_item 布局，代码如下所示：

```java
public class MyAdapter extends ArrayAdapter<String> {
 
	public MyAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.my_list_view_item, null);
		} else {
			view = convertView;
		}
		TextView textView = (TextView) view.findViewById(R.id.text_view);
		textView.setText(getItem(position));
		return view;
	}
 
}
```

　　下面再程序的主布局文件里面引入 MyListView 这个控件，如下所示：

```java
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
 
    <com.example.customview.MyListView
        android:id="@+id/my_list_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" >
    </com.example.customview.MyListView>
 
</RelativeLayout>
```

　　最后在 Activity 中初始化 MyListView 中的数据，并处理了 onDelete() 方法的删除逻辑，代码如下：

```java
public class MainActivity extends Activity {
 
	private MyListView myListView;
 
	private MyAdapter adapter;
 
	private List<String> contentList = new ArrayList<String>();
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initList();
		myListView = (MyListView) findViewById(R.id.my_list_view);
		myListView.setOnDeleteListener(new OnDeleteListener() {
			@Override
			public void onDelete(int index) {
				contentList.remove(index);
				adapter.notifyDataSetChanged();
			}
		});
		adapter = new MyAdapter(this, 0, contentList);
		myListView.setAdapter(adapter);
	}
 
	private void initList() {
		contentList.add("Content Item 1");
		contentList.add("Content Item 2");
		contentList.add("Content Item 3");
		contentList.add("Content Item 4");
		contentList.add("Content Item 5");
		contentList.add("Content Item 6");
		contentList.add("Content Item 7");
		contentList.add("Content Item 8");
		contentList.add("Content Item 9");
		contentList.add("Content Item 10");
	}
 
}
```

　　这样就把整个例子的代码就完成了。MyListView 可以像 ListView 一样，正常显示所有的数据，但是当用手机在 MyListView 的某一行上快速滑动时，就会有一个删除按钮显示出来。点击一下删除按钮就可以将这一行的数据删除。此时的 MyListView 不仅保留了 ListView 原生的所有功能，还增加了一个滑动进行删除的功能，确实是一个继承控件。


## 4. 参考文章
1. [Android 自定义 View 的实现方法，带你一步步深入理解 View（四）](https://blog.csdn.net/guolin_blog/article/details/17357967)