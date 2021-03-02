# 第 11 章 $AppClick 全埋点方案 8：AST

## 11.1. 关键技术

### 11.1.1. APT

​		APT 是 Annotation Processing Tool 的缩写，即注解处理器，是一种处理注解的工具。确切来说，它是 javac 的一个工具，用来在编译时扫描和处理注解。注解处理器以 Java 代码（或者编译过的字节码）作为输入，以生成 \.java 文件作为输出。简单来说，就是在编译期通过注解生成 \.java 文件。

### 11.1.2. Element

​		自定义注解处理器，需要继承 AbstractProcessor 类。对于 AbstractProcessor 来说，最重要的就是 process 方法，process 方法处理的核心是 Element 对象。

​		Element 有 5 个直接子类，它们分别代表一种特定类型的元素。5 个子类各有各的用处，并且有各自独有的方法，在使用的时候可以强制将 Element 对象转换成它们中的任意一种，但是必须满足转换的条件，不然会抛出异常。

* TypeElement

  一个类或接口程序元素。

* VariableElement

  一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数。

* ExecutableElement

  某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注解类型元素。

* PackageElement

  一个包程序元素。

* TypeParameterElement

  一般类、接口、方法或构造方法元素的泛型参数。

### 11.1.3. APT 实例

​		讲解一个关于 APT 实例。

​		通过 APT 来实现一个功能，该功能类似于 ButterKnife 中的 @BindView 注解。通过对 View 变量的注解，实现对 View 的绑定（无须调用 findViewById 方法）。

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAPTProject。

### 11.1.4. javapoet

​		也可以使用 javapoet 库来生成 Java 代码。

​		javapoet 是一个开源的项目，Github 地址：https://github.com/square/javapoet

#### 11.1.5 AST

​		AST，是 Abstract Syntax Tree 的缩写，即 “ 抽象语法树 ”，是编辑器对代码的第一步加工之后的结果，是一个树形式表示的源代码。源代码的每个元素映射到一个节点或子树。

​		Java 的编译过程可以分成三个阶段，参考下图。

![Java编译过程](https://res.weread.qq.com/wrepub/epub_25123578_61)

​		第一阶段：所有的源文件会被解析成语法树。

​		第二阶段：调用注解处理器，即 APT 模块。如果注解处理器产生了新的源文件，新的源文件也要参与编译。

​		第三阶段：语法树会被分析并转化成类文件。

## 11.2. 原理概述

​		编辑器对代码处理的流程大概是：

```
JavaTXT -> 词语法分析 -> 生成AST -> 语义分析 -> 编译字节码
```

​		通过操作 AST，可以达到修改源代码的功能。在自定义注解处理器的 process 方法里，通过 roundEnvironment.getRootElements() 方法可以拿到所有的 Element 对象，通过 trees.getTree(element) 方法可以拿到对应的抽象语法树（AST），然后我们自定义一个 TreeTranslator，在 visitMethodDef 里即可对方法进行判断。如果是目标处理方法，则通过 AST 框架的相关 API 即可插入埋点代码，从而实现全埋点的效果。

## 11.3. 案例

​		以自动采集 Android 的 Button 点击事件为例。

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAppClick8

## 11.4. 完善方案

​		通过测试发现，下面几种情况的点击事件，该方案目前无法采集：

1. 通过 ButterKnife 的 @OnClick 注解绑定的事件；
2. 通过 android：OnClick 属性绑定的事件；
3. 设置 OnClickListener 使用了 Lambda 语法。

### 11.4.1. 问题 1：无法采集通过 ButterKnife 的 @OnClick 注解绑定的事件

​		由于 ButterKnife 是通过 @OnClick 注解绑定事件的，所以我们上面的判断条件就无法满足，即实现了 View.OnClickListener 接口，并且方法是 onClick(View view)。

​		针对这个问题，我们可以判断一个方法上是否有 ButterKnife 的 @OnClick 注解标记，如果有，就插入相应的埋点代码。

### 11.4.2. 问题 2：无法采集通过 android：OnClick 属性绑定的事件

​		该问题的解决方案与之前的类似，通过添加注解的方式即可解决。

​		首先在 sdk module 里新增注解 @SensorsDataTrackViewOnClick。

​		然后，我们再判断一个方法上是否有 @SensorsDataTrackViewOnClick 注解标记，如果有，就插入相应的埋点代码。

​		最后，我们在 android：onClick 属性绑定的方法上使用上面新增的 @SensorsDataTrackViewOnClick 注解进行标记。

​		这样处理之后，就可以采集通过 android：onClick 属性绑定的点击事件了。

### 11.4.3. 问题 3：设置 OnClickListener 使用了 Lambda 语法

​		同 AspectJ 一样，该方面目前暂时无法支持。

## 11.5. 扩展采集能力

### 11.5.1. 扩展 1：支持采集 AlertDialog 的点击事件

​		对于采集 AlertDialog 的点击事件，只需要判断当前 JCMethodDecl 是否符合 “void onClick(DialogInterface dialog，int which)” 方法描述符规则，如果符合，则插入相应的埋点代码。

​		对 JCTree.JCMethodDecl 的判断，主要是从返回值、方法名称、方法参数个数及类型方面来判断的。所以，我们可以编写一个通用的判断规则，这样更方便维护和扩展。首先，定义 SensorsAnalyticsMethodCell 类，主要用来表示一个方法所包含的相关信息，主要包括返回值、方法名称、方法参数列表。

​		然后再定义配置类 SensorsAnalyticsConfig.java，主要用来保存各个要匹配的方法配置，以及判断方法。

### 11.5.2. 扩展 2：支持采集 MenuItem 的点击事件

​		在 Android 系统中，与MenuItem相关的方法主要有两个：

1. Activity.onOptionsItemSelected(android.view.MenuItem)
2. Activity.onContextItemSelected(android.view.MenuItem)

​		所以，对于采集 MenuItem 的点击事件，只需要判断当前 JCMethodDecl 是否符合上面的方法描述符规则即可，可以通过添加上面两个方法描述符对应的配置项来实现。

### 11.5.3. 扩展3：支持采集 CheckBox、SwitchCompat、RadioButton、ToggleButton、RadioGroup 的点击事件

​		以上控件都是 CompoundButton 的子类，它们设置的 listener 均是 CompoundButton.OnCheckedChangeListener 类型，要实现的回调方法是 “onCheckedChanged(CompoundButtoncompoundButton，boolean isChecked)”。

​		所以，对于采集 CompoundButton 类型控件的点击事件，我们只需要判断当前 JCMethod Decl 是否符合 “onCheckedChanged(CompoundButtoncompoundButton，boolean isChecked)” 对应的方法描述符规则即可。

### 11.5.4. 扩展 4：支持采集 RatingBar 的点击事件

​		RatingBar 设置的 listener 是RatingBar.OnRatingBarChangeListener 类型，要实现的回调方法是 “onRatingChanged(RatingBarratingBar，float rating，boolean fromUser)”。

​		所以，对于采集 RatingBar 控件的点击事件，只需要判断当前 JCMethodDecl 是否符合 “onRatingChanged(RatingBar ratingBar，floatrating，boolean fromUser)” 对应的方法描述符规则。

### 11.5.5. 扩展 5：支持采集 SeekBar 的点击事件

​		SeekBar 设置的 listener 是SeekBar.OnSeekBarChangeListener 类型，要实现的方法一共有三个：

```java
seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
});
```

​		根据实际的业务分析需求，我们只需要考虑 “onStopTrackingTouch(SeekBar seekBar)” 方法。

​		所以，对于采集 SeekBar 控件的点击事件，我们只需要判断当前 JCMethodDecl 是否符合 “onStopTrackingTouch(SeekBar seekBar)” 对应的方法描述符规则。

### 11.5.6. 扩展 6：支持采集 Spinner 的点击事件

​		Spinner 设置的 listener 是AdapterView.OnItemSelectedListener 类型，要实现的回调方法总共有两个：

```java
spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }
});
```

​		根据实际的业务分析需求，我们只需要考虑 “onItemSelected(AdapterView<？>parent，View view，int position，long id)” 方法。

​		所以，对于采集 Spinner 控件的点击事件，只需要判断当前 JCMethodDecl 是否符合 “onItemSelected(AdapterView<？>parent，View view，int position，long id)” 对应的方法描述符规则。

### 11.5.7. 扩展 7：支持采集 TabHost 的点击事件

​		TabHost 设置的 listener 是 TabHost.OnTabChangeListener 类型，要实现的回调方法是 “onTabChanged(String tabName)”，

​		所以，对于采集 TabHost 控件的点击事件，只需要判断当前 JCMethodDecl 是否符合 “onTabChanged(String tabName)” 对应的方法描述符规则。

### 11.5.8. 扩展 8：支持采集 ListView、GridView 的点击事件

​		ListView 和 GridView 设置的 listener 均是 AdapterView.OnItemClickListener 类型，要实现的回调方法是 “onItemClick(AdapterView<？>parent，View view，int position，long id)”。

​		所以，对于采集 ListView 和 GridView 控件的点击事件，只需要判断当前 JCMethodDecl 是否符合 “onItemClick(AdapterView<？>parent，Viewview，int position，long id)” 对应的方法描述符规则。

### 11.5.9. 扩展 9：支持采集 ExpandableListView 的点击事件

​		ExpandableListView 的点击分为 groupClick 和 childClick 两种情况。其中 groupClick 设置的 listener 是 ExpandableListView.OnGroupClickListener 类型，要实现的回调方法是 “onGroupClick(ExpandableListViewexpandableListView，View view，int position，long id)”。

​		所以，对于采集 ExpandableListView 控件的groupClick点击事件，我们只需要判断当前 JCMethodDecl 是否符合 “onGroupClick(ExpandableListViewexpandableListView，View view，int position，long id)” 对应的方法描述符规则。

​		其中，childClick 设置的 listener 是 ExpandableListView.OnChildClickListener 类型，要实现的回调方法是 “onChildClick(ExpandableListViewexpandableListView，View view，intgroupPosition，int childPosition，long id)”。

​		所以，对于采集 ExpandableListView 控件的 childClick 点击事件，我们只需要判断当前 JCMethodDecl 是否符合 “onChildClick(ExpandableListViewexpandableListView，View view，intgroupPosition，int childPosition，long id)” 对应的方法描述符规则。

## 11.6. 缺点

* com.sun.tools.javac.tree 相关 API 语法晦涩，理解难度大，要求有一定的编译原理基础；
* APT 无法扫描其他 module，导致 AST 无法处理其他 module；
* 不支持 Lambda 语法；
* 带有返回值的方法，很难把埋点代码插入到方法之后。

