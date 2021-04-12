# 第 9 章 $AppClick 全埋点方案 6：ASM

​		Android 应用程序的打包流程，可以参考下图。

![Android 应用程序的打包流程](https://res.weread.qq.com/wrepub/epub_25123578_39)

​		通过上图可知，我们只要在图中红圈处拦截（即生成 .dex 文件之前），就可以拿到当前应用程序中所有的 .class 文件，然后借助一些库，就可以遍历这些 .class 文件中的所有方法，再根据一定的条件找到需要的目标方法，最后进行修改并保存，就可以插入埋点代码了。

​		Google 从 Android Gradle 1.5.0 开始，提供了 Transform API。通过 TransformAPI，允许第三方以插件（Plugin）的形式，在 Android 应用程序打包成 .dex 文件之前的编译过程中操作 .class 文件。我们只要实现一套 Transform，去遍历所有 .class 文件的所有方法，然后进行修改（在特定 listener 的回调方法中插入埋点代码），最后再对原文件进行替换，即可达到插入代码的目的。

## 9.1. 关键技术

### 9.1.1. Gradle Transform

​		Gradle Transform 是 Android 官方提供给开发者在项目构建阶段（即由 .class 到 .dex 转换期间）用来修改 .class 文件的一套标准 API。目前比较经典的应用是字节码插桩、代码注入等。

​		概括来说，Gradle Transform 的功能，就是把输入的 .class 文件转变成目标字节码文件。

​		Transform的 两个基础概念：

* TransformInput

  TransformInput 是指这些输入文件的一个抽象。它主要包括两个部分：

  * DirectoryInput

    集合是指以源码方式参与项目编译的所有目录结构及其目录下的源码文件。

  * JarInput集合

    是指以 jar 包方式参与项目编译的所有本地 jar 包和远程 jar 包。

    注：此处的 jar 亦包括 aar，后文也均以 jar 来统称。

* TransformOutputProvider

  是指 Transform 的输出，通过它可以获取输出路径等信息。

​		Transform.java 是一个抽象类，定义了几个抽象方法。

* getName

  代表该 Transform 对应 Task 的名称。它会出现在 app/build/intermediates/transforms 目录下。

* getInputTypes

  它是指定 Transform 要处理的数据类型。目前主要支持两种数据类型：

  * CLASSES 表示要处理编译后的字节码，可能是 jar 包也可能是目录。
  * RESOURCES 表示处理标准的 java 资源。

* getScopes 

  指定 Transform 的作用域。常见的作用域有下面 7 种：

  1. PROJECT 只处理当前项目。
  2. SUB_PROJECTS 只处理子项目。
  3. PROJECT_LOCAL_DEPS 只处理当前项目的本地依赖，例如 jar、aar。
  4. SUB_PROJECTS_LOCAL_DEPS 只处理子项目的本地依赖，例如 jar、aar。
  5. EXTERNAL_LIBRARIES 只处理外部的依赖库。
  6. PROVIDED_ONLY 只处理本地或远程以 provided 形式引入的依赖库。
  7. TESTED_CODE 测试代码。

* isIncremental

  是否是增量构建。

### 9.1.2. Gradle Transform 实例

​		通过 Transform 提供的 API，我们可以遍历当前应用程序中所有的 .class 文件，包括目录和 jar 包。如果要实现一个 Transform 并遍历 .class 文件，就需要通过 Gradle 插件来完成。

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackTransformProject

### 9.1.3. ASM

​		ASM 是一个功能比较齐全的 Java 字节码操作与分析框架。通过使用 ASM 框架，我们可以动态生成类或者增强既有类的功能。ASM 可以直接生成二进制 .class 文件，也可以在类被加载入 Java 虚拟机之前动态改变现有类的行为。Java 的二进制被存储在严格格式定义的 .class 文件里，这些字节码文件拥有足够的元数据信息用来表示类中的所有元素，包括类名称、方法、属性以及 Java 字节码指令。ASM 从字节码文件中读入这些信息后，能够改变类行为、分析类的信息，甚至能够根据具体的要求生成新的类。

​		介绍一个 ASM 框架中几个核心的相关类。

* ClassReader

  该类主要用来解析编译过的 .class 字节码文件。

* ClassWriter

  该类主要用来重新构建编译后的类，比如修改类名、属性以及方法，甚至可以生成新的类字节码文件。

* ClassVisitor

  主要负责 “ 拜访 ” 类成员信息。其中包括标记在类上的注解、类的构造方法、类的字段、类的方法、静态代码块等。

* AdviceAdapter

  实现了 MethodVisitor 接口，主要负责 “ 拜访 ” 方法的信息，用来进行具体的方法字节码操作。

​		在 ClassVisitor 类中，我们可以根据实际的需求进行条件判断，只要满足我们特定条件的类，我们才会去修改它的特定方法。比如，我们要自动采集 Button 控件的点击事件，那么只有实现了 View$OnClickListener 接口的类，我们才会去遍历这个类并找到重写的 onClick(view) 方法，然后进行修改操作并保存。

## 9.2. 原理概述

​		我们可以自定义一个 Gradle Plugin，然后注册一个 Transform 对象。在 transform 方法里，可以分别遍历目录和 jar 包，然后我们就可以遍历当前应用程序所有的 .class 文件。然后再利用 ASM 框架的相关 API，去加载相应的 .class 文件、解析 .class 文件，就可以找到满足特定条件的 .class 文件和相关方法，最后去修改相应的方法以动态插入埋点字节码，从而达到自动埋点的效果。

## 9.3. 案例

​		以自动采集 Android 的 Button 控件的点击事件为例，详细介绍该方案的实现步骤。

​		完整的项目源码请参考：https://github.com/wangzhzh/AutoTrackAppClick6

## 9.4. 完善

​		通过测试可以发现，该方案目前无法采集通过 android：onClick 属性绑定的点击事件。对于这个问题，原因和解决方案与使用 AspectJ 方案时一致。

​		在 sdk module 新增一个注解 @SensorsDataTrackViewOnClick。

​		需要判断一下当前扫描到的注解是否是我们自定义的 @SensorsDataTrackViewOnClick 类型。如果是则做个标记，然后在 visitMethod 里判断是否有这个标记，如果有，则插入埋点字节码。

​		在 android：onClick 属性绑定的方法上用我们上面自定义的注解标记一下，即 @SensorsDataTrackViewOnClick 。

​		这样处理之后，就可以采集通过 android：onClick 属性绑定的点击事件了。

## 9.5 扩展采集能力

### 9.5.1. 扩展 1：支持采集 AlertDialog 的点击事件

​		要在 MethodVisitor 的 onMethodExit 方法中，扫描到 onClick(DialogInterface dialog，int which) 方法时，如果当前类又实现了 DialogInterface.OnClickListener 接口，我们即可插入相应的埋点字节码，从而就可以支持采集 AlertDialog 的点击事件了。对于 AlertDialog 设置的 DialogInterface.OnMultiChoiceClickListener 类型也是同样的处理方式。

### 9.5.2. 扩展 2：支持采集 MenuItem 的点击事件

​		众所周知，Android 系统中的常用菜单有两种形式：

* 选项菜单

  是通过 onCreateOptionsMenu 方法创建的菜单，也就是你点击手机 menu 键弹出的菜单。

* 上下文菜单是通过 Activity 的 registerForContextMenu(View view) 方法给 View 注册的菜单，然后通过 onCreateContextMenu 方法创建。也就是你长按前面注册的 View 时弹出的菜单。

​		另外，在 Android 系统中，和 MenuItem 点击相关的方法主要有三个：

* Activity.onOptionsItemSelected(android.view.MenuItem)

  这个方法只在 onCreateOptionsMenu 创建的菜单被选中时才会被触发。

* Activity.onContextItemSelected(android.view.MenuItem)

  这个方法只在 onCreateContextMenu 创建的菜单被选中时才会被触发。

* Activity.onMenuItemSelected(int，android.view.MenuItem)

  当你选择上面两种菜单中的任意一种时，都会触发这个回调方法。在 AppCompatActivity 中，该方法已被标记为 final，无法重写，所以我们可以忽略。

​		如果我们要支持采集 MenuItem 的点击事件，只需要判断扫描到的方法是 onOptionsItemSelected 或 onContextItemSelected，然后插入相应的埋点字节码即可。

### 9.5.3. 扩展 3：支持采集 CheckBox、SwitchCompat、RadioButton、ToggleButton、RadioGroup 的点击事件

​		以上这些控件有一个共同的特点，就是都有选中/未选中的状态。它们的点击事件，设置的 listener 均是 CompoundButton.OnCheckedChangeListener 类型，实现的回调方法是 onCheckedChanged(CompoundButton compoundButton，boolean isChecked)。

​		对于以上控件点击事件的采集，我们只需要判断当前扫描到的方法是 onCheckedChanged(CompoundButton compoundButton，boolean isChecked)，并且当前类又实现了CompoundButton.OnCheckedChangeListene 接口，然后去插入相应的埋点字节码即可实现。

### 9.5.4. 扩展 4：支持采集 RatingBar 的点击事件

​		RatingBar 设置的 listener 是 RatingBar.OnRatingBarChangeListener 类型，实现的回调方法是 onRatingChanged(RatingBar ratingBar，float rating，boolean fromUser)。同理，我们只需要判断当前扫描到的方法是 onRatingChanged(RatingBar ratingBar，float rating，boolean fromUser)，并且当前类又实现了 RatingBar.OnRatingBarChangeListener 接口，然后插入相应的埋点字节码即可。

### 9.5.5. 扩展 5：支持采集 SeekBar 的点击事件

​		SeekBar 设置的 listener 是 SeekBar.OnSeekBarChangeListener 类型，它总共有三个回调方法：

```java
​```
public interface OnSeekBarChangeListener {

    /**
     * 拖动条进度改变的时候调用
     */
    void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

    /**
     * 拖动条开始拖动的时候调用
     */
    void onStartTrackingTouch(SeekBar seekBar);

    /**
     * 拖动条停止拖动的时候调用
     */
    void onStopTrackingTouch(SeekBar seekBar);
}
​```
```

​		根据实际的业务需求，我们一般只需要关注 onStopTrackingTouch(SeekBar seekBar) 即可，对于 onProgressChanged 和 onStartTrackingTouch 可以直接忽略。如果有更精细化的分析需求，也可以添加对这两个回调方法的处理。

​		对于 SeekBar 控件点击事件的采集，我们只需要判断当前扫描到的方法是 onStopTrackingTouch(SeekBar seekBar)，并且当前类又实现了 SeekBar.OnSeekBarChangeListener 接口，然后插入相应的埋点字节码即可。

### 9.5.6. 扩展 6：支持采集 Spinner 的点击事件

​		Spinner 设置的 listener 是 AdapterView.OnItemSelectedListener 类型，实现的回调方法是 onItemSelected(AdapterView<？>parent，View view，int position，long id)。对于 Spinner 控件点击事件的采集，我们只需要判断当前扫描到的方法是 onItemSelected(AdapterView<？>parent，View view，int position，long id)，并且当前类又实现了 AdapterView.OnItemSelectedListener 接口，然后插入相应的埋点字节码即可。

### 9.5.7. 扩展 7：采集 TabHost 的点击事件

​		TabHost 设置的 listener 是 TabHost.OnTabChangeListener 类型，实现的回调方法是 onTab Changed(String tabName)。对于 TabHost 控件点击事件的采集，我们只需要判断当前扫描到的方法是 onTab Changed(String tabName)，并且当前类又实现了 TabHost.OnTabChangeListener 接口，然后插入相应的埋点字节码即可。

​		TabHost 点击事件的采集，目前有一个问题，就是无法采集 TabHost 所在的页面信息（Activity）。因为从目前的情况，暂时无法拿到 Activity 对象，只能拿到当前被点击 Tab 的名称，即回调方法 onTabChanged(String tabName) 中的 tabName 参数。

### 9.5.8. 扩展 8：支持采集 ListView、GridView 的点击事件

​		ListView、GridView 的功能和使用方法都非常类似，它们设置的 listener 是 AdapterView.OnItemClickListener 类型，实现的回调方法是 onItemClick(AdapterView<？>parent，View view，int position，long id)。

​		对于 ListView 和 GridView 控件点击事件的采集，我们只需要判断当前扫描到的方法是 onItemClick(AdapterView<？>parent，View view，int position，long id)，并且当前类又实现了 AdapterView.OnItemClickListener 接口，然后插入相应的埋点字节码即可。

### 9.5.9. 扩展 9：支持采集 ExpandableListView 的点击事件

​		ExpandableListView 是 ListView 的子类，它的点击需要分为 groupClick 和 childClick 两种情况。所以它设置的 listener 也有两种类型，即 ExpandableListView.OnChildClickListener 类型和 ExpandableListView.OnGroupClickListener 类型，实现的回调方法分别是 onChild Click(ExpandableListView expandableListView，View view，int parentPos，int childPos，long l) 和 onGroupClick(ExpandableListView expandableListView，View view，int i，long l)。

​		对于 ExpandableListView 控件点击事件的采集，我们只需要判断当前扫描到的方法是 onChildClick(ExpandableListViewexpandableListView，View view，intparentPos，int childPos，long l) 或 onGroupClick(ExpandableListViewexpandableListView，View view，int i，longl)，并且当前类又实现了 ExpandableListView.OnChildClickListener 接口或 ExpandableListView.OnGroupClickListener，然后插入相应的埋点字节码即可。

## 9.6. 缺点

* 目前来看，实现全埋点，使用 ASM 框架是一个相对完美的选择，暂时没有发现有什么缺点。