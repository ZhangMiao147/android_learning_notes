# Fragment 的使用

　　使用 Fragment 时，必须构造一个无参构造函数，系统会默认带，但一旦写有参构造函数，就必要构造无参构造函数。

## 1 Fragment 常用的 Api

　　Fragment 常用的三个类：

* android.support.v4.app.Fragment 主要用于定义 Fragment。
* android.support.v4.app.FragmentManager 主要用于在 Activity 中操作 Fragment。
* android.support.v4.app.FragmentTransation 处理 Fragment 操作的事务。

　　获取 FragmentManager 的方式：getFragmentManager()，v4 中是 getSupportManager()。

　　主要的操作都是 FragmentTransaction 的方法：

* 开启一个事务

  ```java
  FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
  ```

* 往 Activity 中添加一个 Fragment

  ```java
  transaction.add();
  ```

* 从 Activity 中移除一个 Fragment

  ```java
  transaction.remove();
  ```

  如果被移除的 Fragment 没有添加到回退栈，这个 Fragment 实例将会被销毁。

* 使用 Fragment 替换当前的

  ```java
  transaction.replace();
  ```

  实际上就是 remove() 然后 add() 的合体。

* 隐藏当前的 Fragment

  ```java
  transaction.hide();
  ```

  仅仅是设为不可见，并不会销毁，相当于 View 的 setVisibility() 方法。

* 显示之前隐藏的 Fragment

  ```java
  transaction.show();
  ```

* 将 view 从 UI 中移除

  ```java
  detach();
  ```

  和 remove() 不同，此时 fragment 的状态依然由 FragmentManager 维护。

* 重建 view 视图，附加到 UI 上并显示

  ```java
  attach();
  ```

* 提交一个事务

  ```java
  transaction.commit();
  ```

　　在一个事务开启到提交可以进行多个添加、移除、替换等操作。

## 2 在 Activity 中动态添加 Fragment 

　　动态添加 Fragment 主要分为 4 步：

1. 获取到 FragmentManager，在 Activity 中可以直接通过 getFragmentManager 得到。
2. 开启一个事务，通过调用 beginTransaction 方法开启。
3. 向容器内加入 Fragment，一般使用 replace 方法实现，需要传入容器的 id 和 Fragment 的实例。
4. 提交事务，调用 commit 方法提交。

## 3 Fragment 回退栈管理

　　Activity 是由任务栈管理的，遵循先进后出的原则，Fragment 也可以实现类似的栈管理，从而实现多个 Fragment 先后添加后可以返回上一个 Fragment，当 Activity 容器内没有 Fragment 时回退则退出 Activity。

```java
transaction.addToBackStack(null);
```

　　Activity 的第一个 Fragment（根 Fragment ）可以不添加回退栈，这样最后一个 Fragment 按返回时就不会空白而是直接退出 activity。

　　调用 addToBackStack(null) 将当前的事务添加到了回退栈，调用 replace 方法后 Fragment 实例不会被销毁，但是视图层次会被销毁，即会调用 onDestoryView 和 onCreateView 。若需保存当前 fragment 视图状态，则可以使用 hide 后 add 新的 Fragment。

## 4 Fragment 与 Activity 通信

1. 如果 Activity 中包含自己管理的 Fragment 的引用，可以通过引用直接访问所有的 Fragment 的 public 方法。
2. 如果 Activity 中未保存任何 Fragment 的引用，可以通过每个 Fragment 都有一个唯一的 TAG 或者 ID 使用 getFragmentManager.findFragmentByTag() 或者 findFragmentById() 获得任何 Fragment 实例，然后进行操作。

3. 在 Fragment 中可以通过 getActivity 得到当前绑定的 Activity 的实例，然后进行操作。

   如果在 Fragment 中需要 Context，可以通过调用 getActivity()，如果该 Context 需要在 Activity 被销毁后还存在，则使用 getActivity().getApplicationContext()。

4. Fragment 返回数据给 Activity：接口。

```java
//Fragment
public class TestFragment extends Fragment {

    private OnListener listener;

    public void setListener(OnListener listener) {
        this.listener = listener;
    }

    public interface OnListener {
        void onFinished(boolean result);

        void onStart();
    }


    @OnClick(R.id.btn)
    public void button() {
        ....
        listener.onFinished(true);
    }
}
```

```java
//Activity
TestFragment f = new TestFragment();
f.setListener(new TestFragment.OnListener() {

    @Override
    public void onFinished(boolean result) {
        ......
    }

    @Override
    public void onStart() {
        ......
    }
});

FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
fragmentTransaction.replace(R.id.fragment_container, f);
fragmentTransaction.commit();
```

5. 一般来说传参数给 Fragment，会通过 Fragment 的 setArguments（Bundle bundle）方法，而不是用构造方法传。

   setArguments 方法必须在 fragment 创建以后，添加给 Activity 前完成。千万不要先调用了 add，然后设置 arguments。

   ```java
      Bundle bundle = new Bundle();  
      bundle.putString(ARGUMENT, argument);  
      TestFragment f = new TestFragment();  
      f.setArguments(bundle); 
   ```

## 5 Fragment 之间通信

　　主要是通过 getAcvitity 方法，getActivity 方法可以让 Fragment 获取到关联的 Activity，然后再调用 Activity 的 findViewById 方法，就可以获取到和这个 Activity 关联的其他 Fragment 的视图了。

　　调用 Fragment.setTargetFragment，这个方法一般用于当前 Fragment 由其他 Fragment 启动时。

```java
EvaluateDialog dialog = new EvaluateDialog();  
//注意setTargetFragment  
dialog.setTargetFragment(ContentFragment.this, REQUEST_EVALUATE);  
dialog.show(getFragmentManager(), EVALUATE_DIALOG); 

//接收返回回来的数据  
@Override  
public void onActivityResult(int requestCode, int resultCode, Intent data)  
{  
    super.onActivityResult(requestCode, resultCode, data);  

    if (requestCode == REQUEST_EVALUATE)  
    {  
        String evaluate = data  
                    .getStringExtra(EvaluateDialog.RESPONSE_EVALUATE);  
        Toast.makeText(getActivity(), evaluate, Toast.LENGTH_SHORT).show();  
        Intent intent = new Intent();  
        intent.putExtra(RESPONSE, evaluate);  
        getActivity().setResult(Activity.REQUEST_OK, intent);  
        }  

    } 
}
```

```java
public class EvaluateDialog extends DialogFragment  
{ 
    ......

    // 设置返回数据  
    protected void setResult(int which)  
    {  
        // 判断是否设置了targetFragment  
        if (getTargetFragment() == null)  
            return;  

        Intent intent = new Intent();  
        intent.putExtra(RESPONSE_EVALUATE, mEvaluteVals[which]);  
        getTargetFragment().onActivityResult(ContentFragment.REQUEST_EVALUATE,  Activity.RESULT_OK, intent);            
    }  
}
```

## 6 Fragment 重叠问题

　　当屏幕旋转或者内存重启（Fragment 以及容器 Activity 被系统回收后再打开时重新初始化）会导致 Fragment 重叠问题，是因为 Activity 本身重启的时候会恢复 Fragment，然后创建 Fragment 的代码又会新建一个 Fragment 的原因。

　　解决方法：在 onCreate 方法中判断参数 Bundle savedInstanceState，为空时初始化 Fragment 实例，然后在 Fragment 中通过 onSaveInstanceState 的方法恢复数据。

```java
private TestFragment f;

protected void onCreate(Bundle savedInstanceState)  {  
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.activity_main);  

        Log.e(TAG, savedInstanceState+"");  

        if(savedInstanceState == null)  
        {  
            f = new TestFragment();  
            FragmentManager fm = getSupportFragmentManager();  
            FragmentTransaction tx = fm.beginTransaction();  
            tx.add(R.id.id_content, f, "ONE");  
            tx.commit();  
        }  
} 
```

## 7 Fragment 与 ActionBar 和 MenuItem

　　Fragment 可以添加自己的 MenuItem 到 Activity 的 ActionBar 或者可选菜单中。

1. 在 Fragment 的 onCreate 中调用 setHashOptionMenu(true)；

   ```java
   public class TestFragment extends Fragment  
   {  
       @Override  
       public void onCreate(Bundle savedInstanceState)  
       {  
           super.onCreate(savedInstanceState);  
           setHasOptionsMenu(true);  
       }   
   }
   ```

2. 然后在 Fragment 类中实现 onCreateOptionsMenu；

   ```java
   public class TestFragment extends Fragment  
   {  
       @Override  
       public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  
       {  
           inflater.inflate(R.menu.fragment_menu, menu);  
       }  
   }
   ```

3. 如果希望在 Fragment 中处理 MenuItem 的点击，也可以实现 onOptionsItemSelected；Activity 也可以直接处理该 MenuItem 的点击事件。

   ```java
   public class TestFragment extends Fragment  
   {  
       @Override  
       public boolean onOptionsItemSelected(MenuItem item)  
       {  
           switch (item.getItemId())  
           {  
           case R.id.id_menu_test:  
               ...... 
               break;  
           }  
           return true;  
       }  
   }
   ```

   ```java
   //MainActivity
   	@Override  
       public boolean onCreateOptionsMenu(Menu menu)  
       {  
           super.onCreateOptionsMenu(menu);  
           getMenuInflater().inflate(R.menu.main, menu);  
           return true;  
       }  
   
       @Override  
       public boolean onOptionsItemSelected(MenuItem item)  
       {  
           switch (item.getItemId())  
           {  
           case R.id.action_settings:  
               ......  
               return true;  
           default:  
               //如果希望 Fragment 自己处理 MenuItem 点击事件，一定不要忘了调用 super.xxx  
               return super.onOptionsItemSelected(item);  
           }  
       }  
   ```

## 8 没有布局的 Fragment -- 保存大量数据

　　主要用于处理异步请求带来的数据保存问题，尤其是异步请求未完成时屏幕旋转这种现象。步骤如下：

1. 继承 Fragment，声明引用指向有数据的对象。

   ```java
   //数据类
   public class MyAsyncTask extends AsyncTask<Void, Void, Void>  
   {  
       private FixProblemsActivity activity;  
       /** 
        * 是否完成 
        */  
       private boolean isCompleted;  
       /** 
        * 进度框 
        */  
       private LoadingDialog mLoadingDialog;  
       private List<String> items;  
   
       public MyAsyncTask(FixProblemsActivity activity)  
       {  
           this.activity = activity;  
       }  
   
       /** 
        * 开始时，显示加载框 
        */  
       @Override  
       protected void onPreExecute()  
       {  
           // 使用DialogFragment创建对话框
           mLoadingDialog = new LoadingDialog();  
           mLoadingDialog.show(activity.getFragmentManager(), "LOADING");  
       }  
   
       /** 
        * 加载数据 
        */  
       @Override  
       protected Void doInBackground(Void... params)  
       {  
           items = loadingData();  
           return null;  
       }  
   
       /** 
        * 加载完成回调当前的Activity 
        */  
       @Override  
       protected void onPostExecute(Void unused)  
       {  
           isCompleted = true;  
           notifyActivityTaskCompleted();  
           if (mLoadingDialog != null)  
               mLoadingDialog.dismiss();  
       }  
   
       public List<String> getItems()  
       {  
           return items;  
       }  
   
       private List<String> loadingData()  
       {  
           try  
           {  
               Thread.sleep(5000);  
           } catch (InterruptedException e)  
           {  
           }  
           return new ArrayList<String>(Arrays.asList("通过Fragment保存大量数据",  
                   "onSaveInstanceState保存数据",  
                   "getLastNonConfigurationInstance已经被弃用", "RabbitMQ", "Hadoop",  
                   "Spark"));  
       }  
   
       /** 
        * 设置Activity，因为Activity会一直变化，在onDestroy中set null
        *  
        * @param activity 
        */  
       public void setActivity(FixProblemsActivity activity)  
       {  
           // 如果上一个Activity销毁，将与上一个Activity绑定的DialogFragment销毁  
           if (activity == null)  
           {  
               mLoadingDialog.dismiss();  
           }  
           // 设置为当前的Activity  
           this.activity = activity;  
           // 开启一个与当前Activity绑定的等待框  
           if (activity != null && !isCompleted)  
           {  
               mLoadingDialog = new LoadingDialog();  
               mLoadingDialog.show(activity.getFragmentManager(), "LOADING");  
           }  
           // 如果完成，通知Activity  
           if (isCompleted)  
           {  
               notifyActivityTaskCompleted();  
           }  
       }  
   
       private void notifyActivityTaskCompleted()  
       {  
           if (null != activity)  
           {  
               activity.onTaskCompleted();  
           }  
       }  
   
   }  
   ```

   ```java
   public class TestFragment extends Fragment  
   {  
   
       // data object we want to retain  
       // 保存一个异步的任务，有数据的对象
       private MyAsyncTask data;  
   
       public void setData(MyAsyncTask data)  
       {  
           this.data = data;  
       }  
   
       public MyAsyncTask getData()  
       {  
           return data;  
       }  
   }
   ```

2. 当 Fragment 创建时调用 setRetainInstance(boolean)。

   ```java
   public class TestFragment extends Fragment  
   {  
       // this method is only called once for this fragment  
       @Override  
       public void onCreate(Bundle savedInstanceState)  
       {  
           super.onCreate(savedInstanceState);  
           // retain this fragment  
           setRetainInstance(true);  
       }  
   }
   ```

3. 把 Fragment 实例添加到 Activity 中。

   ```java
   public class FixProblemsActivity extends ListActivity  
   {  
       private static final String TAG = "MainActivity";  
       private ListAdapter mAdapter;  
       private List<String> mDatas;  
       private OtherRetainedFragment dataFragment;  
       private MyAsyncTask mMyTask;  
   
       @Override  
       protected void onRestoreInstanceState(Bundle state)  
       {  
           super.onRestoreInstanceState(state);  
           Log.e(TAG, "onRestoreInstanceState");  
       }  
   
       @Override  
       protected void onSaveInstanceState(Bundle outState)  
       {  
           mMyTask.setActivity(null);  
           super.onSaveInstanceState(outState);  
           Log.e(TAG, "onSaveInstanceState");  
       }  
   
       @Override  
       protected void onDestroy()  
       {  
           Log.e(TAG, "onDestroy");  
           super.onDestroy();  
   
       }  
       /** 
        * 回调 
        */  
       public void onTaskCompleted()  
       {  
           mDatas = mMyTask.getItems();  
           mAdapter = new ArrayAdapter<String>(FixProblemsActivity.this,  
                   android.R.layout.simple_list_item_1, mDatas);  
           setListAdapter(mAdapter);  
       }  
   
   } 
   ```
   
4. 当 Activity 重新启动后，使用 FragmentManager 对 Fragment 进行恢复。

   ```java
   public class FixProblemsActivity extends ListActivity  
   {  
       @Override  
       public void onCreate(Bundle savedInstanceState)  
       {  
           super.onCreate(savedInstanceState);  
           Log.e(TAG, "onCreate");  
   
           // find the retained fragment on activity restarts  
           FragmentManager fm = getFragmentManager();  
           dataFragment = (OtherRetainedFragment) fm.findFragmentByTag("data");  
   
           // create the fragment and data the first time  
           if (dataFragment == null)  
           {  
               // add the fragment  
               dataFragment = new OtherRetainedFragment();  
               fm.beginTransaction().add(dataFragment, "data").commit();  
           }  
           mMyTask = dataFragment.getData();  
           if (mMyTask != null)  
           {  
               mMyTask.setActivity(this);  
           } else  
           {  
               mMyTask = new MyAsyncTask(this);  
               dataFragment.setData(mMyTask);  
               mMyTask.execute();  
           }  
           // the data is available in dataFragment.getData()  
       }  
   } 
   ```

## 9 DialogFragment

　　和 Fragment 有着一致的生命周期，且 DialogFragment 也允许开发者把 Dialog 作为内嵌的组件进行重用，类似 Fragment（可以在大屏幕和小屏幕显示出不同得效果）。使用 DialogFragment 至少需要实现 onCreateView 或者 onCreateDialog 方法。

onCreateView 使用定义的 xml 布局文件展示 Dialog。

onCreateDialog 使用 AlertDialog 或者 Dialog 创建出 Dialog。

### 9.1 重写 onCreateView 创建 Dialog

1. 创建一个对话框布局文件。

2. 继承 DialogFragment，重写 onCreateView 方法。

   ```java
   public class TestFragment extends DialogFragment  
   {  
       @Override  
       public View onCreateView(LayoutInflater inflater, ViewGroup container,  
               Bundle savedInstanceState)  
       {  
          // 隐藏对话框标题栏
          getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE); 
           View view = inflater.inflate(R.layout.fragment_edit_name, container);  
           return view;  
       }  
   
   } 
   ```

3. 显示 DialogFragment

   ```java
   public void showDialog(View view)  {  
           TestDialogFragment dialog = new TestDialogFragment();  
           dialog.show(getFragmentManager(), "TestDialog");  
   }
   ```

### 9.2 重写 onCreateDialog 创建 Dialog

1. 新建对话框布局文件。

2. 继承 DialogFragment 重写 onCreateDialog 方法。

   ```java
   public class TestFragment extends DialogFragment  
   {  
   
       @Override  
       public Dialog onCreateDialog(Bundle savedInstanceState)  
       {  
           AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());  
           // Get the layout inflater  
           LayoutInflater inflater = getActivity().getLayoutInflater();  
           View view = inflater.inflate(R.layout.fragment_test_dialog, null);  
           // Inflate and set the layout for the dialog  
           // Pass null as the parent view because its going in the dialog layout  
           builder.setView(view)  
                   // Add action buttons  
                   .setPositiveButton("Test",  
                           new DialogInterface.OnClickListener()  
                           {  
                               @Override  
                               public void onClick(DialogInterface dialog, int id)  
                               {  
                               }  
                           }).setNegativeButton("Cancel", null);  
           return builder.create();  
       }  
   } 
   ```

3. 显示 DialogFragment

   ```java
   public void showDialog(View view)  {  
           TestFragment dialog = new TestFragment();  
           dialog.show(getFragmentManager(), "testDialog");  
   } 
   ```

## 10. Fragment 的 startActivityForResult

　　在 Fragment 中存在 startActivityForResult() 以及 onActivityResult() 方法，需要通过调用 getActivity().setResult(Fragment.REQUEST_CODE,intent)来设置返回值。

## 11. FragmentPagerAdapter 与 FragmentStatePagerAdapter 区别

　　使用 ViewPager 再结合 FragmentPagerAdapter 或者 FragmentStatePagerAdapter 可以制作一个 App 的主页。

　　而 FragmentPagerAdapter 和 FragmentStatePagerAdapter 的区别在于对于 Fragment 是否销毁：

* FragmentPagerAdapter：对于不再需要的 Fragment，选择调用 detach() 方法，仅销毁视图，并不会销毁 fragment 实例。
* FragmentStatePagerAdapter：会销毁不再需要的 Fragment，当当前事务提交以后，会彻底的将 fragment 从当前 Activity 的 FragmentManager 中移除，state 标明，销毁时，会将其 onSaveInstanceState(Bundle outState) 中的 bundle 信息保存下来，当用户切换回来，可以通过该 bundle 恢复生成新的 Fragment，也就是说，可以在 onSaveInstanceState(Bundle outState) 方法中保存一些数据，在 onCreate 中进行恢复创建。

　　使用 FragmentStatePagerAdapter 更省内存，但是销毁新建也是需要时间的。一般情况下，如果是制作主界面，就 3-4 个 Tab，那么可以选择使用 FragmentPagerAdapter，如果是用于 ViewPager 展示数量特别多的条目时，建议使用 FragmentStatePagerAdapter。


## 参考文章
[Android Fragment 完全解析，关于碎片你所需知道的一切](https://blog.csdn.net/guolin_blog/article/details/8881711)

[Android Fragment 学习与使用 -- 基础篇](https://blog.csdn.net/qq_24442769/category_6708901.html)

[Android Fragment 学习与使用 -- 高级篇](https://blog.csdn.net/qq_24442769/article/details/77679147)