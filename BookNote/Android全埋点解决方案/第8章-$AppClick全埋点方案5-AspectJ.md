# 第 8 章 $AppClick 全埋点方案 5：AspectJ

## 8.1. 关键技术

### 8.1.1. AOP

​		AOP 是  Aspect Oriented Programming的缩写，即 “ 面向切面编程 ”。通过使用 AOP，可以在编译期间对代码进行动态管理，以达到统一维护的目的。AOP 其实是 OOP 编程思想的一种延续，同时也是 Spring 框架中的一个重要模块。

​		利用 AOP，我们可以对业务逻辑的各个模块进行隔离，从而使得业务逻辑各个部分之间的耦合度降低，提高程序的可重用性，同时也会提高开发的效率。利用 AOP，我们可以在无侵入的状态下在宿主中插入一些代码逻辑，从而实现一些特殊的功能，比如日志埋点、性能监控、动态权限控制、代码调试等。

​		和 AOP 相关的几个术语。

* Advice：增强

  也叫 “ 通知 ”。增强是织入到目标类连接点上的一段程序代码。在 Spring 框架中，增强除了被用于描述一段程序代码之外，还拥有另一个和连接点相关的信息，这便是执行点的方位。结合执行点方位信息和切点信息，我们就可以找到特定的连接点。

* JoinPoint：连接点

  即程序执行的某个特定位置，如类开始初始化前、类初始化后、类中某个方法调用前、调用后、方法抛出异常后等。一个类或一段程序代码拥有一些具有边界性质的特定点，这些点中的特定点就称为 “ 连接点 ”。Spring 框架仅支持方法的连接点，即仅能在方法调用前、方法调用后、方法抛出异常时以及方法调用前后这些程序执行点织入增强。连接点由两个信息确定：一是用方法表示的程序执行点；二是用相对点表示的方位。

* PointCut：切点

  也叫 “ 切入点 ”。每个程序类都拥有多个连接点，如一个拥有两个方法的类，这两个方法都是连接点，即连接点是程序类中客观存在的事物。AOP 通过 “ 切点 ” 来定位特定的连接点。连接点相当于数据库中的记录，而切点相当于查询条件。切点和连接点不是一对一的关系，一个切点可以匹配多个连接点。在 Spring 框架中，切点通过 Pointcut 接口进行描述，它使用类和方法作为连接点的查询条件，Spring AOP 的规则解析引擎负责切点所设定的查询条件，找到对应的连接点。确切地说，切点不能称之为查询连接点，因为连接点是方法执行前、执行后等包括方位信息的具体程序执行点，而切点只定位到某个方法上，所以如果希望定位到具体连接点上，还需要提供方位信息。

* Aspect：切面

  切面由切点和增强组成，它既包括了横切逻辑的定义，也包括了连接点的定义，Spring AOP 就是负责实施切面的框架，它将切面所定义的横切逻辑织入到切面所指定的连接点中。

* Weaving：织入

  织入是将增强添加到目标类具体连接点上的过程。AOP 像一台织布机，将目标类、增强通过 AoP 这台织布机天衣无缝地编织到一起。

  根据不同的实现技术，AOP 有三种不同的织入方式：

  1. 编译期织入，这要求使用特殊的 Java 编译器；

  2. 类装载期织入，这要求使用特殊的类装载器；

  3. 动态代理织入，在运行期为目标类添加增强生成子类的方式。

     Spring 采用动态代理织入，而我们本章要讲的 AspectJ 是采用编译期织入和类装载期织入。

* Target：目标对象增强逻辑的织入目标类。如果没有 AOP，目标业务类需要自己实现所有逻辑，而在 AOP 的帮助下，目标业务类只实现那些非横切逻辑的程序逻辑，而性能监控和事务管理等横切逻辑则可以使用 AOP 动态织入到特定的连接点上。

### 8.1.2. AspectJ

​		AOP 其实是一个概念，同时也是一个规范，它本身并没有规定具体实现的语言。而 AspectJ 实际上是对 AOP 编程思想的实现，它能够和 Java 配合起来使用。

​		AspectJ 最核心的模块就是它提供的 ajc 编译器，它其实就是将 AspectJ 的代码在编译期插入到目标程序当中，运行时跟在其他地方没有什么两样。因此要使用 AspectJ，最关键的就是使用它的 ajc 编译器去编译代码。ajc 会构建目标程序与 AspectJ 代码的联系，在编译期将 AspectJ 代码插入到被切出的 PointCut 中，从而达到 AOP 的目的。

​		关于 AspectJ 更详细的介绍，可以参考其官网：http://www.eclipse.org/aspectj/

### 8.1.3. AspectJ 注解

​		和 AspectJ 相关的一些注解类。

* @Aspect 

  该注解用来描述一个切面类。定义切面类的时候需要加上这个注解，标明当前类是切面类，以便能被 ajc 编译器识别。

* @PointCut（切点表达式）

  用来定义切点，标记方法。

  此切点可以用来匹配用 @SensorsDataTrackViewOnClick 注解标记的所有方法。

* @Before（切点表达式）

  前置增强，在某连接点之前执行的增强。

* @After（切点表达式）

  后置增强，在某连接点之后执行的增强。

* @Around（切点表达式）

  环绕增强，在切点前后执行。

* @AfterReturning（切点表达式）

  返回增强，切入点方法返回结果之后执行。

* @AfterThrowing（切点表达式）

  异常增强，切点抛出异常时执行。

### 8.1.4. 切点表达式

​		下面我们详细介绍一下切点表达式。

```ejs
execution(* android.view.View.OnClickListener.onClick(android.view.View))
```

​		上面就是一个切入点表达式的示例。一个完整的切入点表达式包含如下几个部分：

```
execution（<修饰符模式>？<返回类型模式><方法名模式>（<参数模式>）<异常模式>？）
```

其中：

* 带 ？的表示这部分是可选的；
* 修饰符模式指的是 public、private、protected 等；
* 异常模式指的是如 ClassNotFoundException 异常等。

### 8.1.5. JoinPoint

​		ProceedingJoinPoint 是 JoinPoint 的子类，如果使用 @Around 注解，参数为 ProceedingJoinPoint 类型。与 JoinPoint 相比，它多了一个 proceed() 方法，该方法用来执行切点方法（方法原来的业务逻辑）。

### 8.1.6. call与execution区别

​		那么 call 和 execution 又有什么区别呢？总的来说，当 call 捕获 joinPoint 时，捕获的是签名方法的调用点；而 excution 捕获 joinPoint 时，捕获的则是方法的执行点。两者的区别就在于一个是 “ 调用点 ”，一个是 “ 执行点 ”。

### 8.1.7. AspectJ 使用方法

​		总的来说，在 Android Studio 中使用 AspectJ 大概有两种方式。

1. 通过 Gradle 配置

   通过在 Gradle 的构建脚本中，定义任务来使得项目执行 ajc 编译，将 AOP 的 Module 编织进入到目标工程中，从而达到非侵入式 AOP 的目的。

2. 通过 Gradle Plugin 

   也可以通过插件来使用 AspectJ。

   目前有很多开源的类似项目（插件），比如：

   Ⅰ.https://github.com/uPhyca/gradle-android-aspectj-plugin

   Ⅱ.https://github.com/JakeWharton/hugo

   Ⅲ.https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx

### 8.1.8. 通过 Gradle 配置使用 AspectJ

​		以实现统计每个方法的耗时情况为例，介绍如何在 Android Studio 中通过 Gradle 配置来使用 AspectJ。

​		完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAspectJProject1

### 8.1.9. 自定义 Gradle Plugin

​		Gradle 插件是使用 Groovy 语言进行开发的，而 Groovy 是可以兼容 Java 语言的。Android Studio 除了可以开发 Android 应用程序外，还可以开发 Gradle 插件。

​		用一个示例来介绍如何自定义 Gradle 插件。该插件的功能非常简单，仅仅是定义了一个 Task 然后打印一条日志信息。完整的项目源码可以参考：https://github.com/wangzhzh/AutoTrackAspectJProject2

### 8.1.10. 发布 Gradle 插件

​		如果你想使用你的插件，或者把插件给别人使用，就需要把插件发布出去。一般情况下，按照 “ 把插件发布到哪里 ” 这个条件来区分的话，发布插件可以简单分为两种情况：

* 发布插件到本地仓库
* 发布插件到远程仓库

### 8.1.11. 使用 Gradle Plugin

​		使用本地仓库的插件的方法。

​		关于发布插件到远程仓库，可以参考其官网文档，链接如下：https://plugins.gradle.org/docs/submit

### 8.1.12. Plugin Project

​		自定义的插件类的 apply(Project project) 方法中，有一个 Project 类型的参数。Project 参数是插件与 Gradle 通信的管道。通过 Project 参数，插件可以通过代码使用 Gradle 的所有特性。其中，Project 与 build.gradle 是一对一的关系。

​		android{}、versionName、dataBinding{} 等这些设置又是如何被 Android 插件读取的呢？其实就是通过Extension（扩展）。

## 8.2. 原理概述

​		对于 Android 系统中的 View，它的点击处理逻辑，都是通过设置相应的 listener 对象并重写相应的回调方法实现的。比如，对于 Button、ImageView 等控件，它设置的 listener 对象均是 android.view.View.OnClickListener 类型，然后重写它的 onClick(android.view.View) 回调方法。我们只要利用一定的技术原理，在应用程序编译期间（比如生成.dex之前），在其 onClick(android.view.View) 方法中插入相应的埋点代码，即可做到自动埋点，也就是全埋点。

​		我们可以把 AspectJ 的处理脚本放到我们自定义的插件里，然后编写相应的切面类，再定义合适的 PointCut 用来匹配我们的织入目标方法（ listener 对象的相应回调方法），比如 android.view.View.OnClickListener 的 onClick(android.view.View) 方法，就可以在编译期间插入埋点代码，从而达到自动埋点即全埋点的效果。

## 8.3. 案例

​		完整的项目源码可以参考如下网址：https://github.com/wangzhzh/AutoTrackAppClick5

​		对于 View 的点击，全埋点时，我们都需要采集哪些属性呢？

​		要采集的属性，理论上可以分为两部分：

1. View 的标准属性，比如：
   * $element_type：View 的类型，比如当前 View 是Button、TextView 还是 ListView；
   *  $element_id：View 的 id，也就是 android：id 属性设置的字符串；
   * $activity：View 所属 Activity 或 Fragment（页面）；
   * $activity_title：View 所属 Activity 或 Fragment 的title（标题）；
   * $element_content：View 上显示的文本内容；
   * ……

2. View 的扩展属性

   是指针对当前 View 进行扩展的一些自定义属性。

   下面我们一一介绍这些属性。

   * $element_type是指控件的类型，也可以简单的理解成View的类名（或者包名+类名）。比如Button，我们可以通过如下两种方式获取：

     view.getClass().getCanonicalName()

     返回的是带完整的包名+类名的字符串，比如：android.widget.Button。

     view.getClass().getSimpleName()

     返回的只有类名，即：Button。

     具体使用哪个，可以根据实际的业务需求来确定。

     此外，还需要考虑一个细节：比如 Button，有 android.widget.Button、android.support.v7.widget.AppCompatButton、自定义 Button 之分，理论上这些都属于 Button 类型，不应该有所区分；对于自定义的 View，如果仅仅采集类名，那需要考虑不同包下面可能有相同的类名，但实际含义可能并不相同。

   * $element_id

     通过 view.getId() 方法可以拿到当前 View 的 id 属性，但此时拿到的是一个 int 类型的数值，这个是没有任何实际阅读意义的，我们需要把它转化成 android：id 属性里设置的那个字符串，方便阅读和识别，比如 android：id="@+id/lambdaButton" 中的 lambdaButton 字符串。我们可以通过下面的方法转化：

     ```java
     String idString = view.getContext().getResources().getResourceEntryName(view.getId());
     ```

     同时，我们也需要考虑两个问题：

     a）View 没有在 xml 中设置 android：id 或者没有通过 view.setId(int) 设置 id，即 view.getId()==View.NO_ID 的情况；

     b）View 的 id 是 android.R.id.xxx 格式的，比如： android.R.id.home。

   * $activity

     通过 view.getContext() 方法可以获取到当前 View 所属的 Context 对象，然后可以将 Context 对象转换成 Activity 对象。在这个转换的过程中，需要考虑 Context 是 ContaxtWeapper 类型的情况。

     ```java
     public static Activity getActivityFromView(View view) {
         Activity activity = null;
         if (view == null) {
             return null;
         }
     
         try {
             Context context = view.getContext();
             if (context != null) {
                 if (context instanceof Activity) {
                     activity = (Activity) context;
                 } else if (context instanceof ContextWrapper) {
                     while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                         context = ((ContextWrapper) context).getBaseContext();
                     }
                     if (context instanceof Activity) {
                         activity = (Activity) context;
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return activity;
     }
     ```

   * $activity_title

     即 Activity 的 title，也叫页面标题，可以理解成是 Activity 在 AndroidManifest.xml 中声明的 android：label 属性值

   * $element_content

     即控件上显示的文本内容信息。对于一些标准的控件，我们直接通过相应的方法即可获取显示的文本内容。

     Button 类型的控件，我们可以通过 button.getText().toString() 方法来获取；对于 ToggleButton 类型的控件，我们根据不同的状态，可以通过 toggleButton.getTextOn() 方法或者 toggleButton.getTextOff() 方法来获取相应的显示文本内容。

     对于 ImageView，我们可以简单的把 imageView.getContentDescription() 方法返回的内容当作文本内容。

     还有一种特殊情况需要考虑，那就是如何获取自定义 View 的显示文本？

     目前，比较好的处理方法是遍历自定义 View 的所有 SubView，然后再判断 SubView 是否是标准的控件。如果是标准的控件，我们再通过其相应的方法获取其显示文本；如果是 ViewGroup 类型，我们则继续递归遍历。最后将所有 SubView 的文本内容按照一定的格式拼接在一起（比如 a-b-c 格式）。

   * 扩展 View 的属性（添加自定义属性）

     全埋点采集控件的点击行为事件时，如果需要添加一些自定义的属性该怎么办？比如，在采集搜索按钮的点击行为事件时，我们还想把输入的关键词作为一个属性也一起采集了。针对这种需求，我们可以利用 View 提供的 setTag(int key，final Objecttag)方法来实现。

     可以通过 setTag(int，Object) 方法来给 View 设置一个 Object 对象（比如 JSONObject），然后在全埋点采集时，再通过 View 的 getTag(int) 方法去获取这个 Object 对象，最后转化为属性放到点击事件里面，即可达到给 View 扩展自定义属性的效果。

     setTag 方法的第一个参数 key 是一个 int 类型，这就引入了一个可能会相互冲突（覆盖）的问题。

     针对类似这种资源相互冲突的问题，我们可以利用 Android 的资源 id 来解决。当我们在 xml 里定义了一个 id，编译成功后，这个 id 就会出现在 R.java 里面，而且它被赋予的 int 类型的值，在整个应用程序范围内，肯定是唯一的，不会跟应用程序内其他模块有任何冲突。

## 8.4. 完善方案

​		通过测试发现，下面几种情况的点击事件，当前全埋点方案目前是无法采集的：

1.  通过 ButterKnife 的 @OnClick 注解绑定的事件；
2. 通过 android：OnClick 属性绑定的事件；
3. MenuItem 的点击事件；
4. 设置的 OnClickListener 使用了 Lambda 语法。

* 问题 1：无法采集通过 ButterKnife 的 @OnClick 注解绑定的事件

  由于 ButterKnife 是通过 @OnClick 注解绑定点击事件的，再加上 AspectJ 默认情况下无法织入第三方的库，所以我们定义的切入点无法匹配到，也就导致最终无法采集其点击行为事件。

  对于这个问题，我们可以新增一个切入点，专门用来匹配 ButterKnife@OnClick 注解。

  为了保证程序的稳定性以及正确性，我们可以修改切入点规则，即我们只匹配带有 @OnClick 注解，并且仅带有一个 View 参数的方法。

* 问题 2：无法采集通过 android：OnClick 属性绑定的事件

  通过 android：OnClick 属性绑定的事件，没有直接走 setOnClickListener 逻辑，所以切入点对它也是无效的。

  新增一个注解，然后在 android：OnClick 属性绑定的方法上用新增的注解标记，最后再新增一个切点匹配这个注解即可。

  在 android：onClick 属性绑定的方法上用我们上面新增的 @SensorsDataTrackView OnClick 注解标记。

* 问题 3：无法采集 MenuItem 的点击事件

  和MenuItem相关的方法有两个：

  1. Activity.onOptionsItemSelected(android.view.MenuItem)
  2. Activity.onContextItemSelected(android.view.MenuItem) 

  所以，我们只需要添加相应的切点去匹配上面两个方法就行了。

  通过 MenuItem，我们是无法获取当前 MenuItem 是所属 Activity 的。在这两个切点中，我们引入了 joinPoint.getTarget()。joinPoint.getTarget() 返回的就是该方法所属的类，由于和 MenuItem 相关的两个方法都是Activity 的，所以 joinPoint.getTarget() 返回的就是 MenuItem 所属的 Activity，这样，我们就能知道当前MenuItem 所属页面的信息了。

* 问题 4：setOnClickListener 使用了 Lambda 语法

  由于目前 AspectJ 还不支持 Lambda 语法，所以这个问题暂时无法解决。

## 8.5. 扩展采集能力

### 8.5.1. 扩展 1：支持采集 AlertDialog 的点击事件

​		要采集 AlertDialog 的点击事件，只需要针对 DialogInterface.OnClickListener 的回调方法 onClick(DialogInterface dialog，int which) 新增一个对应的切点进行匹配即可。

​		还有一种 AlertDialog 是可以显示带有选择状态的列表。

​		这种情况下，设置的 listener 是 DialogInterface.OnMultiChoiceClickListener 类型。如果还要支持这种场景的点击事件，我们就需要针对 DialogInterface.OnMultiChoiceClickListener 的回调方法 onClick(DialogInterface dialogInterface，int which，boolean isChecked) 再新增一个切点进行匹配。

### 8.5.2. 扩展 2：支持采集 CheckBox、SwitchCompat、RadioButton、ToggleButton、RadioGroup 等点击事件

​		以上控件设置的 listener 对象均是 CompoundButton.OnCheckedChangeListener 类型。如果我们要支持采集以上控件的点击事件，同时也需要新增一个对应的切入点用来匹配 CompoundButton.OnCheckedChangeListener 的回调方法onCheckedChanged(android.widget.CompoundButton，boolean) 。

### 8.5.3. 扩展 3：支持采集 RatingBar 的点击事件

​		RatingBar 设置的 listener 是 RatingBar.OnRatingBarChangeListener 类型。如果我们要支持采集 RatingBar 的点击事件，也需要新增一个对应的切入点用来匹配 RatingBar.OnRatingBarChangeListener 的 onRatingChanged(android.widget.RatingBar，float，boolean) 回调方法。

### 8.5.4. 扩展 4：支持采集 SeekBar 的点击事件

​		SeekBar 设置的 listener 是 SeekBar.OnSeekBarChangeListener 类型。

​		SeekBar.OnSeekBarChangeListener 接口有三个回调方法。一般情况下，我们只需要关注 onStopTrackingTouch(SeekBar seekBar) 回调方法即可。如果我们要支持采集 SeekBar 的点击事件，也可以新增一个对应的切点来匹配这个回调方法。

### 8.5.5. 扩展 5：支持采集 Spinner 的点击事件

​		Spinner 设置的 listener 是 AdapterView.OnItemSelectedListener 类型。如果我们要支持采集 Spinner 的点击事件，需要新增一个对应的切入点用来匹配 AdapterView.OnItemSelectedListener 的回调方法 onItemSelected(android.widget.AdapterView，android.view.View，int，long)。

### 8.5.6. 扩展 6：支持采集 TabHost 的点击事件

​		TabHost 设置的 listener 是 TabHost.OnTabChangeListener 类型。如果我们要支持采集 TabHost 的点击事件，可以新增一个对应的切点匹配 TabHost.OnTabChangeListener 的回调方法  onTabChanged(String)。

​		但采集 TabHost 的点击事件有一个问题，就是无法知道当前 TabHost 所属 Activity，这个问题一直没有办法解决。

### 8.5.7. 扩展 7：支持采集 ListView、GridView 的点击事件

​		ListView 和 GridView 设置的 listener 是 AdapterView.OnItemClickListener 类型。如果我们要支持采集 ListView 和 GridView 的点击事件，我们可以新增一个对应的切点用来匹配 AdapterView.OnItemClickListener 的回调方法 onItemClick(android.widget.AdapterView，android.view.View，int，long)。

### 8.5.8. 扩展 8：支持采集 ExpandableListView 的点击事件

​		ExpandableListView 的点击需要区分 groupOnClick 和 childOnClick 两种情况，它们设置的 listener 分别是  ExpandableListView.OnGroupClickListener 类型、 ExpandableListView.OnChildClickListener 类型。如果我们要支持采集 ExpandableListView 的点击事件，可以新增两个切点分别匹配 ExpandableListView.OnChildClickListener 的回调方法 onChildClick(android.widget.ExpandableListView，android.view.View，int，int，long) 和 ExpandableListView.OnGroupClickListener 的回调方法 onGroupClick(android.widget.ExpandableListView，android.view.View，int，long)。

## 8.6. 缺点

* 无法织入第三方的库；
* 由于定义的切点依赖编程语言，目前该方案无法兼容 Lambda 语法；
* 会有一些兼容性方面的问题，比如：D8、Gradle 4.x 等。