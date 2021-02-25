# 第 10 章 $AppClick 全埋点方案 7：Javassist

# 10.1. 关键技术

### 10.1.1. Javassist

​		Java 字节码以二进制的形式存储在 .class 文件中，每一个 .class 文件包含一个 Java 类或接口。Javaassist 框架就是一个用来处理 Java 字节码的类库。它可以在一个已经编译好的类中添加新的方法，或者是修改已有的方法，并且不需要对字节码方面有深入的了解。

​		Javassist 可以绕过编译，直接操作字节码，从而实现代码的注入。所以，使用 Javassist 框架的最佳时机就是在构建工具 Gradle 将源文件编译成 .class 文件之后，在将 .class 打包成 .dex 文件之前。

### 10.1.2. Javassist 基础

​		关于 Javassist 的相关基础知识。

* 读写字节码

  在 Javassist 框架中，.class 文件是用类 Javassist.CtClass 表示的。一个 CtClass 对象可以处理一个 .class 文件。

* 冻结类

  如果一个 CtClass 对象通过 writeFile()、toClass()、toBytecode() 等方法被转换成一个类文件，此 CtClass 对象就会被冻结起来，不再允许被修改，这是因为一个类只能被 JVM 加载一次。

  一个冻结的 CtClass 对象也可以被解冻。调用 defrost() 方法之后，这个 CtClass 对象就又可以被修改了。

* 类搜索路径

  通过 ClassPool.getDefault() 获取的 ClassPool 是使用 JVM 的类搜索路径。如果程序运行在 JBoss 或者 Tomcat 等 Web 服务器上，ClassPool 可能无法找到用户的类，因为 Web 服务器使用多个类加载器作为系统类加载器。在这种情况下， ClassPool 必须添加额外的类搜索路径。

* ClassPool

  ClassPool 是 CtClass 对象的容器。因为编译器在编译引用 CtClass 代表的 Java 类的源代码时，可能会引用 CtClass 对象，所以一旦一个 CtClass 被创建，它就会被保存在 ClassPool 中。

* 避免内存溢出

  如果 CtClass 对象的数量变得非常多，ClassPool 有可能会导致巨大的内存消耗。为了避免这个问题，我们可以从 ClassPool 中显式删除不必要的 CtClass 对象。如果对 CtClass 对象调用 detach() 方法，那么该 CtClass 对象将会从 ClassPool 中删除。

  在调用 detach() 方法之后，就不能再调用这个 CtClass 对象的任何有关方法了。如果调用 ClassPool 的 get()  方法，ClassPool 会再次读取这个类文件，并创建一个新的 CtClass 对象。

* 在方法体中插入代码

  CtMethod 和 CtConstructor 均提供了 insertBefore()、insertAfter() 及 addCatch() 等方法。它们可以把用 Java 编写的代码片段插入到现有的方法体中。Javassist 包括一个用于处理源代码的小型编译器，它接收用 Java 编写的源代码，然后将其编译成 Java 字节码，并内联到方法体中。

  也可以按行号来插入代码段（如果行号表包含在类文件中）。向 CtMethod 和 CtConstructor 中的 insertAt() 方法提供源代码和原始类定义中的源文件的行号，就可以将编译后的代码插入到指定行号位置。

  insertBefore()、insertAfter()、addCatch() 和 insertAt() 等方法都能接收一个表示语句或语句块的 String 对象。一个语句是一个单一的控制结构，比如 if 和 while，或者以分号结尾的表达式。语句块是一组用 {} 包围的语句。语句和语句块可以引用字段和方法，但不允许访问在方法中声明的局部变量，尽管在块中声明一个新的局部变量是允许的。

  传递给方法 insertBefore()、insertAfter()、addCatch() 和 insertAt() 的 String 对象是由 Javassist 的编译器编译的。由于编译器支持语言扩展，所以以 $ 开头的几个标识符都有特殊的含义：

  * $0，$1，$2，……

    传递给目标方法的参数使用 $1，$2，…… 来访问，而不是原始的参数名称。$1 表示第一个参数，$2 表示第二个参数，以此类推。这些变量的类型与参数类型相同。$0 等价于 this 指针。如果方法是静态的，则 $0 不可用。

* $args

  变量 $args 表示所有参数的数组。该变量的类型是 Object 类型的数组。如果参数类型是原始类型（如 int、boolean 等），则该参数值将被转换为包装器对象（如 java.lang.Integer）以存储在 $args 中。因此，如果第一个参数的类型不是原始类型，那么 $args[0] 等于 $1。注意 $args[0] 不等于 $0，因为 $0 表示 this。

* $$

  变量 $$ 是所有参数列表的缩写，用逗号分隔。

* $\_

  CtMethod 中的 insertAfter() 是在方法的末尾插入编译的代码。在传递给 insertAfter() 的语句中，不但可以使用特殊符号，如 $0、$1，也可以使用 $\_ 来表示方法的结果值。

  该变量的类型是方法的返回结果类型（返回类型）。如果返回结果类型为 void，那么 $\_ 的类型为 Object，$\_ 的值为 null。

  虽然由 insertAfter() 插入的编译代码通常在方法返回之前执行，但是当方法抛出异常时，它也可以执行。要在抛出异常时执行它，insertAfter() 的第二个参数 asFinally 必须为 true。

  如果抛出异常，由 insertAfter() 插入的编译代码将作为 finally 子句执行。$\_ 的值为 0 或 null。在编译代码的执行终止后，最初抛出的异常被重新抛出给调用者。注意，$\_ 的值不会被抛给调用者，而是被丢弃。

* addCatch

  addCatch() 插入方法体抛出异常时执行的代码，控制权会返回给调用者。在插入的源代码中，异常用 $e 表示。

  请注意，插入的代码片段必须以 throw 或 return 语句结束。

* 注解（Annotations）

  CtClass、CtMethod、CtField 和 CtConstructor 均提供了 getAnnotations() 方法，用于读取对应类型上添加的注解。它返回的是一个注解类型的对象数组。

## 10.2. 原理概述

​		在自定义的 plugin 里，我们可以注册一个自定义的 Transform，从而可以分别对当前应用程序的所有源码目录和 jar 包进行遍历。在遍历过程中，利用 Javassist 框架的 API 可以对满足特定条件的方法进行修改，比如插入相关埋点代码。整个原理与使用 ASM 框架类似，此时只是把操作 .class 文件的框架由 ASM 换成 Javassist 了。

## 10.3. 案例

​		以自动采集 Android 的 Button 控件点击事件为例。

​		完整的项目源码请参考：https://github.com/wangzhzh/AutoTrackAppClick7

## 10.4. 扩展采集能力

### 10.4.1. 扩展 1：支持采集通过 android：onClick 属性绑定的点击事件

​		原理和之前介绍的方案一致，也是通过添加自定义注解的方式来解决。

### 10.4.2. 扩展 2：支持采集 AlertDialog 的点击事件

​		对于 AlertDialog 点击事件的采集，我们如果判断当前类实现了 DialogInterface.OnClickListener 接口，并且又是相应的 onClick(DialogInterface dialog，int which) 方法，我们就插入相应的埋点代码即可。

​		再新增一个判断，即如果当前类实现了 DialogInterface.OnMultiChoiceClickListener 接口，并且又是相应的 onClick(DialogInterface dialogInterface，int which，boolean isChecked) 方法，我们就插入埋点代码。

### 10.4.3. 扩展 3：支持采集 MenuItem 的点击事件

​		和 MenuItem 相关的函数主要有两个：

* onContextItemSelected(MenuItem item)
* onOptionsItemSelected(MenuItem item)

​		所有对于 MenuItem 点击事件的采集，我们只需要增加判断当前方法是否是上面的两个方法，如果是，即可插入相应的埋点代码。

### 10.4.4. 扩展 4：支持采集 CheckBox、SwitchCompat、RadioButton、ToggleButton、RadioGroup 的点击事件

​		以上控件都属于同一种类型，它们都带有选择 “状态” 的按钮，同时又都是 CompoundButton 类型的子类。这些控件设置的 listener 均是 android.widget.CompoundButton.OnCheckedChangeListener 类型，实现的回调方法是 onCheckedChanged(CompoundButton compoundButton，boolean b)。

​		对于 CompoundButton 类型控件点击事件的采集，我们只需要判断当前类实现了相应的 CompoundButton.OnCheckedChangeListener 接口，并且又是对应的 onCheckedChanged(CompoundButton compoundButton，boolean b) 回调方法，然后插入埋点代码即可。

### 10.4.5. 扩展 5：支持采集 RatingBar 的点击事件

​		RatingBar 设置的 listener 是 android.widget.RatingBar.OnRatingBarChangeListener 类型，实现的回调方法是 onRatingChanged(RatingBar ratingBar，float rating，boolean fromUser)。

​		对于 RatingBar 控件点击事件的采集，我们只需要判断当前类实现了相应的 RatingBar.OnRatingBarChangeListener 接口，并且又是对应的 onRatingChanged(RatingBar ratingBar，float rating，boolean fromUser) 回调方法，然后插入埋点代码即可。

### 10.4.6. 扩展 6：支持采集 SeekBar 的点击事件

​		SeekBar 设置的 listener 是 android.widget.SeekBar.OnSeekBarChangeListener 类型，需要实现的回调方法一共有三个：

```java
private void initSeekBar() {
    SeekBar seekBar = findViewById(R.id.seekBar);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar,inti,boolean) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    });
}
```

​		根据实际的业务分析需求，一般情况下我们只需要关心 onStopTrackingTouch(SeekBar seekBar) 回调方法即可。对于 onProgressChanged 和 onStartTrackingTouch 回调方法可以直接忽略。

​		对于 SeekBar 控件点击事件的采集，我们只需要判断当前类实现了相应的 SeekBar.OnSeekBarChangeListener 接口，并且又是对应的 onStopTrackingTouch(SeekBar seekBar) 回调方法，然后插入埋点代码即可。

### 10.4.7. 扩展7：支持采集 Spinner 的点击事件

​		Spinner 是 AdapterView 的子类，它显示的是一个可选择的列表。Spinner 设置的 listener 是 android.widget.AdapterView.OnItemSelectedListener 类型，要实现的回调方法一共有两个：

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

​		根据实际的业务分析需求，一般情况下我们只需要关心 onItemSelected(AdapterView<？>parent，View view，int position，long id) 回调方法。对于 onNothingSelected(AdapterView<？>parent) 回调方法可以直接忽略。

​		对于 Spinner 控件点击事件的采集，我们只需要判断当前类实现了相应的 AdapterView.OnItemSelectedListener 接口，并且又是对应的 onItemSelected(AdapterView<？>parent，View view，int position，long id) 回调方法，然后插入埋点代码即可。

### 10.4.8. 扩展 8：支持采集 TabHost 的点击事件

​		TabHost 设置的 listener 是 android.widget.TabHost$OnTabChangeListener 类型，要实现的回调方法是 onTabChanged(String tabName)。

​		对于 TabHost 控件点击事件的采集，我们只需要判断当前类实现了相应的 TabHost$OnTabChangeListener 接口，并且又是对应的 onTabChanged(String tabName) 回调方法，然后插入埋点代码即可。

​		和之前面临的问题一样，TabHost 点击事件的采集，我们目前无法获取 TabHost 所属 Activity 的信息，仅能获取当前点击 Tab 对应的名称，即 onTabChanged(String tabName) 回调方法中的 tabName 参数。

### 10.4.9. 扩展 9：支持采集 ListView、GridView 的点击事件

​		ListView 和 GridView 的功能和使用方法非常相似，它们设置的 listener 均是 android.widget.AdapterView.OnItemClickListener 类型，需要实现的回调方法是 onItemClick(Adapter View<？>parent，View view，int position，long id)。

​		对于 ListView 和 GridView 控件点击事件的采集，我们只需要判断当前类实现了相应的 AdapterView.OnItemClickListener 接口，并且又是对应的 onItemClick(AdapterView<？>parent，View view，int position，long id) 回调方法，然后插入埋点代码即可。

### 10.4.10. 扩展 10：支持采集 ExpandableListView 的点击事件

​		ExpandableListView 是 ListView 的子类。ExpandableListView 的点击需要区分 childOnClick 和 groupOnClick 两种情况，它设置的 listener 分别是 android.widget.ExpandableListView.OnGroupClickListener 类型和 android.widget.ExpandableListView.OnChildClickListener 类型，需要实现的回调方法分别是 onChildClick(ExpandableListView expandableListView，View view，int groupPosition，int childPosition，long id) 和 onGroupClick(ExpandableListView expandableListView，View view，int groupPosition，long id)。

​		对于 ExpandableListView 控件点击事件的采集，我们只需要判断当前类实现了相应的 ExpandableListView.OnGroupClickListener 接口或 ExpandableListView.OnChildClickListener 接口，并且又是对应的 onGroupClick(ExpandableListView expandableListView，View view，int groupPosition，long id) 或 onChildClick(ExpandableListView expandableListView，View view，int groupPosition，int childPosition，long id) 回调方法，然后插入埋点代码即可。