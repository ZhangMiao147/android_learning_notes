# 面试宝典 - kotlin

## kotlin

kotlin 语法简洁，具备现代高级语言特性，并且能与 Java 遗留代码无缝互操作。

java 与 kotlin 的差异就在编译的不同。

为什么要学 kotlin：

* Java 开发者喜欢的很多现代语言高级特性，它都没有，或者迟迟加入。
* kotlin 从这些经验教训中受益良多，而 Java 中的某些早起设计愈显陈旧，脱胎于旧语言，kotlin 解决了他们的很多痛点，进化成了一门优秀的语言。相比 java，kotlin 进步巨大，带来了更可靠的开发体验。

kotlin 的跨平台特性

* kotlin 不仅支持编写代码在虚拟机上运行，而且还是一门扩平台的通用型语言，可以用 kotlin 开发各种类型的原生应用，如 android、macOS、windows、javascript 应用。
* kotlin 能脱离虚拟机层，直接编译成可以在 windows、linux 和 macos 平台上运行的原生二进制代码。

## 只读变量

* 要声明可修改变量，使用 var 关键字
* 要声明只读变量，使用 val 关键字

## 类型

类型推断：对于已声明并赋值的变量，它允许你省略类型定义。

kotlin 的引用类型：

* java 有两种数据类型：引用类型与基本数据类型。
* kotlin 只提供引用类型这一种数据类型，出于更高性能的需要，kotlin 编译器会在 java 字节码中改用基本数据类型。

条件语句：

* if/else if 表达式

* Range 表达式

  in A..B，in 关键字用来检查某个值是否在指定范围之内。

```kotlin
fun main(){
    val age = 3
    if(age in 0..3){
        println("婴幼儿") // age=3 out:婴幼儿，age=0 out:婴幼儿
    } else if (age in 3..12){
        println("少儿")// age=4 out:少儿
    } else {
        println("未知")
    }
}
```

* when 表达式
  * 允许你编写条件式，在某个条件满足时，执行对应的代码
  * 只要代码包含 else if 分支，都建议改用 when 表达式。

```kotlin
    val school = "小学"
    var level:Any = when(school){
        "学前班" -> "幼儿"
        "小学" -> "少儿"
        "中学" -> "青少年"
        else -> {
            println("未知")
        }
    }
    println(level)
```

## 函数

函数头：

![](image/函数.png)

函数参数默认值参：

* 默认值参

  如果不打算传入值参，可以预先给参数指定默认值

* 具名函数参数

  如果使用命令值参，就可以不用管值参的顺序

```kotlin
fun main(){
    fix("Jack")
}

fun fix(name:String,age:Int=2){
    println(name+age) // out：Jack+2
}
```

### Unit 函数

不是所有函数都有返回值，kotlin 中没有返回值的函数叫 unit 函数，也就是说他们的返回类型是 unit。在 kotlin 之前，函数不返回任何东西用 void 描述，意思是“没有返回类型，不会带来什么，忽略它”，也就是说如果函数不返回任何东西，就忽略类型。但是，void 这种解决方案无法解释现代语言的一个重要特征，泛型。

```kotlin
fun main(){
    println(fix(age=10,name="Rose")) //kotlin.Unit
}

fun fix(name:String,age:Int=2){
    println(name+age)// Rose10
}
```

### Nothing 类型

* TODO 函数的任务就是抛出异常，就是永远别指望它运行成功，返回 Nothing 类型。

```kotlin
public inline fun TODO(reason: String): Nothing = throw NotImplementedError("An operation is not implemented: $reason")
```

### 什么是匿名函数

* 定义时不取名字的函数，称之为匿名函数，匿名函数通常整体传递给其他函数，或者从其他函数返回。
* 匿名函数对 kotlin 来说很重要，有了它，我们能够根据需要制定特殊规则，轻松定制标准库内的内置函数。

```kotlin
fun main() {
    val total: Int = "Mississippi".count()

    val totals:Int = "Mississippi".count({ letter ->
        letter == 's'
    })

    println(total) // 11
    println(totals) // 4
}
```

### 函数类型与隐式返回

* 匿名函数也有类型，匿名函数可以当作变量赋值给函数类型变量，就像其他变量一样，匿名函数就可以在代码里传递了。变量有类型，变量可以等于函数，函数也会有类型。函数的类型，由传入的参数和返回值类型决定。
* 和具名函数不一样，除了极少数情况外，匿名函数不需要 return 关键字来返回数据，匿名函数会隐式或自动返回函数体最后一行语句的结果。

```kotlin
    val blessingFunction:()-> String
    blessingFunction = {
        val holiday = "New Year."
        "Happy $holiday"
    }
```

### 函数参数

和具名函数一样，具名函数可以不带参数，也可以带一个或多个任何类型的参数，需要带参数时，参数的类型放在匿名函数的类型定义中，参数名则放在函数定义中。

```kotlin
    val blessingFunction:(String)-> String = {name->
        val holiday = "New Year."
        "$name, Happy $holiday"
    }
 println(blessingFunction("Jack"))//Jack, Happy New Year.
```

### it 关键字

* 定义只有一个参数的匿名函数时，可以使用 it 关键字来表示参数名。当你需要传入两个值参，it 关键字就不能用了。

```kotlin
    val blessingFunction:(String)-> String = {
        val holiday = "New Year."
        "$it, Happy $holiday"
    }

    println(blessingFunction("Jack"))
```

### 匿名函数的类型推断

* 定义一个变量时，如果已把匿名函数作为变量赋值给它，就不需要显示指明变量类型了。

```kotlin
    val blessingFunction:()-> String = {
        val holiday = "New Year."
        "Happy $holiday"
    }

    println(blessingFunction())
```

可以简化为：

```kotlin
    val blessingFunction = {
        val holiday = "New Year."
        "Happy $holiday"
    }

    println(blessingFunction())
```

* 类型推断也支持带参数的匿名函数，但为了帮助编译器更准确地推断变量类型，匿名函数的参数名和参数类型必须有。

```kotlin
    val blessingFunction:(String,Int) -> String= {name,year ->
        val holiday = "New Year."
        "$name,Happy $holiday $year"
    }

    println(blessingFunction("Jack",2027))
```

可以简化为：

```kotlin
    val blessingFunction= {name:String,year:Int ->
        val holiday = "New Year."
        "$name,Happy $holiday $year"
    }

    println(blessingFunction("Jack",2027))
```

### 什么是 lambda

我们将匿名函数称为 lambda，将它的定义称为 lambda 表达式，它返回的数据称为 lambda 结果。为什么叫 lambda？lambda 也可以用希腊字符标示，是 lambda 演算的简称，lambda 演算是一套数理演算逻辑，由数学家 Alonzo Church（阿隆佐.丘齐）于 20 世纪 30 年代发明，在定义匿名函数时，使用了 lambda 演算记法。

### 定义参数是函数的函数

函数的参数是另外一个函数

```kotlin
fun main() {
    // 函数
    val getDiscountWords = {goodsName:String,hour:Int ->
        val currentYear = 2027
        "${currentYear}年，双11${goodsName}促销倒计时：${hour}小时"
    }
  	// 调用 showOnBoard 函数，getDiscountWords 作为参数
    showOnBoard("卫生纸",getDiscountWords);
}

fun showOnBoard(goodsName:String,getDiscountWords:(String,Int)->String){
    val hour:Int = (1..24).shuffled().last()
    // 调用函数
  println(getDiscountWords(goodsName,hour))
}
```

### 简略写法

如果一个函数的 lambda 参数排在最后，或者是唯一的参数，那么括住 lambda 值参的一对圆括号就可以省略。

```kotlin
    val totals:Int = "Mississippi".count({ letter ->
        letter == 's'
    })
```

可以简化为：

```kotlin
    val totals:Int = "Mississippi".count{
        it == 's'
    }
```

### 函数内联

* lambda 可以让你更灵活地编写应用，但是，灵活也是要付出代价的。
* 在 JVM 上，你定义的 lambda 会以对象实例的形式存在，JVM 会为所有同 lambda 打交道的变量分配内存，这就产生了内存开销。更糟的是，lambda 的内存开销会带来严重的性能问题。幸运的是，kotlin 有一种优化机制叫内联，有了内联，JVM 就不需要使用 lambda 对象实例了，因而避免了变量内存分配。哪里需要使用 lambda，编译器就会将函数体复制粘贴到哪里。
* 使用 lambda 的递归函数无法内联，因为会导致复制粘贴无限循环，编译会发出警告。

### 函数引用

要把函数作为参数传给其他函数使用，除了传 lambda 表达式，kotlin 还提供了其他方法，传递函数引用，函数引用可以把一个具名函数转换成一个值参，使用 lambda 表达式的地方，都可以使用函数引用。

```kotlin
fun main() {
    showOnBoard("牙膏",::getDiscountWords)
}

private fun getDiscountWords(goodsName: String,hour:Int):String{
    val currentYear = 2027
    return "${currentYear}年，双11${goodsName}促销倒计时：${hour}小时"

}


private fun showOnBoard(goodsName:String,getDiscountWords:(String,Int)->String){
    val hour:Int = (1..24).shuffled().last()
    println(getDiscountWords(goodsName,hour))
}
```

### 函数类型作为返回类型

函数类型也是有效的返回类型，也就是说可以定义一个能返回函数的函数。

```kotlin
fun main() {
    val getDiscountWords = configDiscountWords()
    println(getDiscountWords("沐浴露"))

}

fun configDiscountWords():(String)->String{
    return {goodsName:String ->
        val currentYear = 2027
        val hour:Int = (1..24).shuffled().last()
        "${currentYear}年，双11${goodsName}促销倒计时：${hour}小时"
    }
}
```

### 闭包

* 在 kotlin 中，匿名函数能修改并引用定义在自己的作用域之外的变量，匿名函数引用着定义自身的函数里的变量，kotlin 中的 lambda 就是闭包。
* 能接收函数或者返回函数的函数又叫做高级函数，高级函数广泛应用于函数式编程当中。

### lambda 与匿名内部类

为什么要在代码中使用函数类型？函数类型能让开发者少写模式化代码，写出更灵活的代码。Java 8 支持面向对象编程和 lambda 表达式，但不支持将函数作为参数传给另一个函数或变量，不过 Java 的替代方案是匿名内部类。

同样的实现，java 的代码更多，kotlin 更简洁。

## kotlin 的可空性

Kotlin 更多地把运行时可能会出现的 null 问题，以编译时错误的方式，提前在变异期强迫我们重视起来，而不是等到运行时报错，防范于未然，提高了我们程序的健壮性。

对于 null 值问题，Kotlin 反其道而行之，除非另有规定，变量不可为 null 值，这样以来，运行时崩溃从根源上得到解决。

Kotlin 区分可空类型和非可空类型，所以，你要一个可空类型变量运行，而它又可能不存在，对于这种潜在危险，编译器时刻警惕着。为了应对这种风险，kotlin 不允许你在可空类型值上调用函数，除非你主动接手安全管理。

安装管理的选项：

1. 选项一：安全调用操作符
2. 选项二：使用非空断言操作符
3. 选项三：使用 if 判断 null 值情况

安全调用操作符：加了安全调用操作符，kotlin 就不会报错了，编译器看到有安全调用操作符，所以它知道如何检查 null 值。如果遇到 null 值，它就跳过函数调用，而不是返回 null。

```kotlin
fun main() {
    var str:String? = "butterfly"
    str = null
  	str.capitalize()// 会报错
    println(str?.capitalize()) // ? 就是安全调用操作符，输出：null
}
```

使用带 let 的安全调用：安全调用允许在可空类型上调用函数，但是如果还想做点额外的事，比如创建新值，或判断不为 null 就调用其他函数，怎么办？可以使用带 let 函数的安全调用操作符。你可以在任何类型上调用 let 函数，它的主要作用是让你在指定的作用域内定义一个或多个变量。

```kotlin
fun main() {
    var str:String? = "butterfly"
    str = ""
    str = str?.let {
        // 非空白的字符串
        if(it.isNotBlank()){
            it.capitalize()
        } else {
            "butterfly"
        }
    }
    println(str)
}
```

非空断言操作符：!!. 又称感叹号操作符，当变量值为 null 时，会抛出 KotlinNullPointerException。

对比使用 if 判断 null 值情况：也可以使用 if 判断，但是相比之下安全调用操作符用起来更灵活，代码也更简洁，我们可以用安全操作符进行多个函数的链式调用。

空合并操作符：?: 操作符的意思是，如果左边的求值结果为 null，就使用右边的结果值。空合并操作符也可以和 let 函数一起使用来代替 if/else 语句。

```kotlin
fun main() {
    var str:String? = "butterfly"
    str = null
    str = str?.let { it.capitalize() }?:"butterfly"
    println(str)
}
```

str = null，out：“butterfly”

Str = "jack"，out：“Jack”

## 异常处理雨自定义异常

* 抛出异常
* 自定义异常
* 异常处理

```java
import java.lang.Exception
import java.lang.IllegalArgumentException

fun main() {
    var number:Int?=null
    try {
        checkOperation(number)
        number!!.plus(1)
    }catch (e:Exception){
        println(e)
    }

}

fun checkOperation(number:Int?){
    number ?: throw UnskilledException()
}

// 自定义异常
class UnskilledException():IllegalArgumentException("操作不当")

```

## 先决条件函数

Kotlin 标准库提供了一些便利函数，使用这些内置函数，你可以抛出带自定义信息的异常，这些便利函数叫做先决条件函数，你可以用它定义先决条件，条件必须满足，目标代码才能执行。

| 函数           | 描述                                                         |
| -------------- | ------------------------------------------------------------ |
| checkNotNull   | 如果参数为null，则抛出IllegalStateException异常，否则返回非 null 值 |
| require        | 如果参数为 false，则抛出 IllegalArgumentException 异常       |
| requireNotNull | 如果参数为null，则抛出IllegalStateException异常，否则返回非null值 |
| error          | 如果参数为null，则抛出IllegalStateException异常并输出错误信息，否则返回非null值 |
| assert         | 如果参数为 false，则抛出 AssertError异常，并打上断言编译器标记 |

```java
fun main() {
    var number:Int?=null
    try {
        checkOperation(number)
        number!!.plus(1)
    }catch (e:Exception){
        println(e)
    }

}

fun checkOperation(number:Int?){
    checkNotNull(number,{"Something is not good."})
}
```

## substring

字符串截取，substring 函数支持 IntRange 类型（表示一个整数范围的类型）的参数，unit 创建的范围不包括上限值。

```java
const val NAME = "Jimmy's friend"
fun main() {
    val index = NAME.indexOf('\'')
    val str = NAME.substring(0,index)
    println(str)//out:Jimmy
    val str1 = NAME.substring(0 until index)
    println(str1)//out:Jimmy
}
```

## split

split 函数返回的是 List 集合数据，List 集合又支持解构语法特性，它允许你在一个表达式里给多个变量赋值，解构常用来简化变量的赋值。

```kotlin
const val NAME = "Jimmy's friend"
const val NAMES = "jack,jacky,jason"
fun main() {

    val data:List<String> = NAMES.split(",")
    println("$data")//out:[jack, jacky, jason]
    val(origin,dest,proxy) = NAMES.split(",")
    println("$origin $dest $proxy")//out:jack jacky jason
}
```

## replace

字符串替换

```kotlin
fun main() {
    val str1 = "The people's Republic of china."

    val str2 = str1.replace(Regex("[aeiou]")){
        when(it.value){
            "a" -> "8"
            "e" -> "6"
            "i" -> "9"
            "o" -> "1"
            "u" -> "3"
            else -> it.value
        }
    }

    println(str1)//out:The people's Republic of china.
    println(str2)//out:Th6 p61pl6's R6p3bl9c 1f ch9n8.

}
```

## == 与 === 比较

在 Kotlin 中，用 == 检查两个字符串中的字符是否匹配，用 === 检查两个变量是否指向内存堆上同一对象，而在 Java 中 == 做引用比较，做结构比较时用 equals 方法。

## 字符串遍历

foreach

```kotlin
fun main() {
    "The people's Republic of china.".forEach {
        print("$it *")//out:T *h *e *  *p *e *o *p *l *e *' *s *  *R *e *p *u *b *l *i *c *  *o *f *  *c *h *i *n *a *. *
    }
}
```

## 数字类型的安全转换函数

Kotlin 提供了 toDoubleOrNull 和 toIntOrNull 这样的安全转换函数，如果数值不能正确转换，与其触发异常不如干脆返回 null 值。

## double 类型格式化

* 格式化字符串是一串特殊字符，它决定改如何格式化数据

```kotlin
import kotlin.math.roundToInt

fun main() {

   println(8.956756.toInt())// 8
    println(8.956756.roundToInt())// 9，四舍五入

    val s = "%.2f".format(8.956756)// double 精度化二位，格式化字符
    println(s)//8.96
}
```

## apply 

apply 函数可看作一个配置函数，你可以传入一个接收者，然后调用一系列函数来配置它以便使用，如果提供 lambda 给 apply 函数执行，它会返回配置好的接受者。

可以看到，调用一个个函数类配置接收者时，变量名就省掉了，这是因为，在 lambda 表达式里，apply 能让每个配置函数都作用于接受者，这种行为有时又叫做相关作用域，因为 lambda 表达式里的所有函数调用都是针对接收者的，或者说，它们是针对接收者的隐式调用。

```kotlin
import java.io.File

fun main() {
    val file1 = File("E://i hava a dream_copy.txt")
    file1.setReadable(true)
    file1.setWritable(true)
    file1.setExecutable(false)

    // 使用 apply
    val file2 = File("E://i hava a dream_copy.txt").apply {
        setReadable(true)
        setWritable(true)
        setExecutable(false)
    }
}
```

## let

let 函数能使某个变量作用于其 lambda 表达式里，让 it 关键字能引用它。let 与 apply 比较，let 会把接收者传给 lambda，而 apply 什么都不传，匿名函数执行完，apply 会返回当前接收者，而 let 会返回 lambda 的最后一行。

```java
fun main() {
    val result = listOf(3,2,1).first().let {
        it*it
    }
    println(result) // 9

    // 不用 let
    val firstElement = listOf(3,2,1).first()
    val res = firstElement * firstElement
    println(res) // 9

    println(formatGreeting(null)) // What's your name?
    println(formatGreeting("Jack")) // Welcome,Jack.

    println(formatGreeting2(null)) // What's your name?
    println(formatGreeting2("Jack")) // Welcome,Jack.
}

fun formatGreeting(guestName:String?):String {
    return guestName?.let {
        "Welcome,$it."
    }?:"What's your name?"
}

// 不使用 let 写法
fun formatGreeting2(guestName:String?):String {
    return if(guestName != null){
        "Welcome,$guestName."
    } else {
        "What's your name?"
    }
}
```

## run

光看作用域行为，run 和 apply 差不多，但与 apply 不同，run 函数不返回接收者，run 返回的是 lambda 结果，也就是 true 或者 false。

```kotlin
import java.io.File

fun main() {
    val file = File("E://i hava a dream_copy.txt")
    val result = file.run {
        readText().contains("great")
    }
    println(result) // true
}
```

run 也能用来执行函数引用

```kotlin
fun main() {
    val result2 = "The People's Republic of china.".run(::isLong)
    println(result2) // true

    "The People's Republic of china."
        .run(::isLong)
        .run(::showMessage)
        .run(::println) // Name is too long.
}

fun isLong(name:String) = name.length >= 10

fun showMessage(isLong:Boolean):String{
    return if(isLong){
        "Name is too long."
    } else {
        "Please rename."
    }
}
```

## with

With 函数是 run 的变体，他们的功能行为是一样的，但 with 的调用方式不同，调用 with 时需要值参作为其第一个参数传入。

```kotlin
fun main() {
    // run 写法
    val result1 = "The People's Republic of china.".run {
        length >= 10
    }
    println(result1) // true
    // with 写法
    val result2 =with("The People's Republic of china."){
        length >= 10
    }
    println(result2) // true
}
```

## also

also 函数和 let 函数功能相似，和 let 一样，also 也是把接收者作为值参传给 lambda，但有一点不同：also 返回接收者对象，而 let 返回 lambda 结果。因为这个差异，alse 尤其适合针对同一原始对象，利用副作用做事，既然 also 返回的是接收者对象，你就可以基于原始接收者对象执行额外的链式调用。

```kotlin
import java.io.File

fun main() {
    var fileContents:List<String>
    val file:File = File("E://i hava a dream_copy.txt")
        .also {
            println(it.name) // i hava a dream_copy.txt
        }
        .also {
            fileContents = it.readLines()
        }

    println(fileContents) // 打印的字符串的集合
}
```

## takeIf

和其他标准函数有点不一样，takeIf 函数需要判断 lambda 中提供的条件表达式，给出 true 或 false 结果，如果判断结果是 true，从 takeIf 函数返回接收者对象，如果是 flase，则返回 null。如果需要判断某个条件是否满足，再决定是否可以赋值变量或执行某项任务，takeIf 就非常有用，概念上讲，takeIf 函数类似于 if 语句，但它的优势是可以直接在对象实例上调用，避免了临时变量赋值的麻烦。

```kotlin
import java.io.File

fun main() {
    val result = File("E://i hava a dream_copy.txt")
        .takeIf { it.exists() && it.canRead() }
        ?.readText()

    println(result) // 无文件，为null，有文件为文件内容
}
```

## takeUnless

takeIf 辅助函数 takeUnless，只有判断你给定的条件结果是 false 时，takeUnless 才会返回原始接收者对象。

```kotlin
import java.io.File

fun main() {

    val result = File("E://i hava a dream_copy.txt")
        .takeUnless { it.isHidden}// 文件不可见为 false，则返回文件对象
        ?.readText()

    println(result) // 文件可见，返回文件内容
}
```



















