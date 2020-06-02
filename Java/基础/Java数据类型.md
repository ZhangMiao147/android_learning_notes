# Java 数据类型

　　变量就是申请内存来存储值。也就是说，当创建变量的时候，需要在内存中申请空间。

　　内存管理系统更具变量的类型为变量分配存储空间，分配的空间只能用来存储该类型数据。

　　因此，通过定义不同类型的变量，可以在内存中存储整数、小数或者字符。

　　Java 的两大数据类型：

1. 内置数据类型
2. 引用数据类型

## 1. 基本数据类型

　　基本数据类型，也称内置类型，是可以在栈直接分配内存的，Java 

　　Java 语言提供了八种基本类型，基本类型可以分为三类，字符类型 char、布尔类型 boolean 以及数值类型 byte、short、int、long、float、double。数值类型又可以分为整数类型 byte、short、int、long 和浮点类型 float、double。JAVA 中的数值类型不存在无符号的，它们的取值范围是固定的，不会随着机器硬件环境或者操作系统的改变而改变。

　　实际上，JAVA 中还存在另外一种基本类型 void，它也有对应的包装类 java.lang.void，不过无法直接对它们进行操作。

#### 5.1.1. byte

1. byte 数据类型是 8 位、有符号的，以二进制补码表示的整数；256 个数字，占 1 个字节。
2. 最大存储数据量是 255，存放的数据范围是：-128(-2^7)-127(2^7-1) 之间；
3. 默认值是 0；
4. byte 类型用在大型数组中节约空间，主要代替整数，因为 byte 变量占用得空间只有 int 类型的四分之一。
5. 例子：byte a = 100，byte b = -50。

#### 5.1.2. short

1. short 数据类型是 16 位、有符号的以二进制补码表示的整数，占 2 字节。

2. 最大数据存储量是 65536，数据范围是：-32768(-2^15） - 32767(2^15-1) 之间。

　　short 数据类型也可以像 byte 那样节省空间。一个 short 变量是 int 型变量所占空间的二分之一。

　　默认值是 0。

　　例子：short s = 1000，short r = -20000。

#### 5.1.3. int

1. int 数据类型是 32 位、有符号的以二进制补码表示的整数，占 3 字节。
2. 最大数据存储容量是 2 的 32 次方减 1，数据范围为：-2,147,483,648（-2^31） -  2,147,485,647（2^31 - 1）。
3. 一般的整数变量默认为 int 类型。
4. 默认值为 0。
5. 例子：int a = 100000，int b = -200000。

#### 5.1.4. long

1. long 数组类型是 64 位、有符号的以二进制补码表示的整数，占 4 字节。
2. 最大数据存储容量是 2 的 64 次方减 1，数据范围为：-9,223,372,036,854,775,808（-2^63） - 9,223,372,036,854,775,807（2^63 -1）。
3. 这种类型主要使用在需要比较大整数的系统上。
4. 默认值是 0L。
5. 例子：long a = 100000L，b = -200000L。

　　long a = 111111111111111（错误，整数型变量默认是 int 型）。

　　long a = 111111111111111L（正确，强制转换）。

#### 5.1.5. float

1. float 数据类型是单精度、32 位、符合 IEEE 754 标准的浮点数，占 4 字节，浮点数是由舍入误差的。
2. 数据范围在 3.4E-45 - 1.4*E38，直接赋值时必须在数据后加上 f 或者 F。
3. float 在储存大型复现数组的时候可节省内存空间。
4. 默认值是 0.0f。
5. 浮点数不能用来表示精确的值，如货币。
6. 例子：float f1 = 234.5f。

　　float f = 6.25（错误，浮点数默认类型是 double 类型）。

　　float f = 6.26F（正确，强制）。

　　double d = 4.55（正确）。

#### 5.1.6. double

1. double 数据类型是双精度、64 位、符合 IEEE 754 标准的浮点数。
2. 数据范围在 4.9E-324 - 1.8E308，赋值时可以加上 d 或者 D，也可以不加。
3. 浮点数的默认类型是 double 类型。
4. double 类型同样不能表示精确的值，如货币。
5. 默认值是 0.0d；
6. 例子：double d1 = 123.4。
7. 如果是小数类型，并且小数比较小，比如四位小数，建议使用 BigDecimal。

#### 5.1.7. boolean

1. boolean 数据类型表示一位的信息。
2. 只有两个取值：true 和 false。
3. 这种类型只作为一种标志来记录 true/false 情况。
4. 默认值是 false。
5. 例子：boolean one = true。

#### 5.1.8. char

1. char 类型是一个单一的 16 位 Unicode 字符，用 ' ' 表示一个字符。java 内部使用 Unicode 字符集，也有一些转义字符，2 字节。存储 Unicode 码，用单引号赋值。
2. 范围为：‘\u0000’（即为0） - '\uffff'（即为65,535），可以当整数用，它的每一个字符都对应一个整数。
3. char 数据类型可以存储任何字符。
4. 例子：char letter = 'A'。

　　Java 决定了没中简单类型的大小。这些大小并不随着机器结构的变化而变化。这种大小的不可更改正是 Java 程序具有很强移植能力的原因之一。

　　下表列出了 Java 中定义的简单类型、占用二进制位数及对应的封装器类。

| 简单类型 | 二进制位数 | 封装器类  |
| -------- | ---------- | --------- |
| boolean  | 1          | Boolean   |
| byte     | 8          | Byte      |
| char     | 16         | Character |
| short    | 16         | Short     |
| int      | 32         | Integer   |
| long     | 64         | Long      |
| float    | 32         | Float     |
| double   | 64         | Double    |
| void     | --         | Void      |

　　对于数值类型的基本类型的取值范围，都已经以常量的形式定义在对应的包装类中了。例如，int 的最大值就是 Integer.MAX_VALUE。

　　注意：float、double 两种类型的最小值与 Float.MIN_VALUE 、Double.MIN_VALUE 的值并不相同，实际上 Float_MIN_VALUE 和 Double.MIN_VALUE 分别指的是 float 和 double 类型所能表示的最小正数。也就是说存在这样一种情况，0 到正负 Float.MIN_VALUE 之间的值 float 类型无法表示，0 到正负 Double.MIN_VALUE 之间的值 double 类型无法表示，0 到正负 Double.MIN_VALUE 之间的值 double 无法表示。这并没有什么好奇怪的，因为这些范围内的数值超出了它们的精度范围。

　　Float 和 Double 的最小值和最大值都是以科学记数法的形式输出的，结尾的 “E+数字” 表示 E 之前的数字要乘以 10 的多少倍。比如 3.14E3 就是 3.14x1000 = 3140，3.14E-3 = 3.14/1000 = 0.00314。

　　Java 基本类型存储在栈中，因此它们的存取速度要快于存储在堆中的对应包装类的实例对象。从 Java 5.0 (1.5) 开始，JAVA 虚拟机（Java Virtual Machine）可以完成基本类型和它们对应包装类之间的自动转换。因此在赋值、参数传递以及数学运算的时候像使用基本类型一样使用它们的包装类，但这并意味着可以通过基本类型调用它们的包装类才具有的方法。另外，所有基本类型（包括 void）的包装类都使用了 final 修饰，因此无法继承它们扩展新的类，也无法重写它们的任何方法。

　　基本类型的优势：数据存储相对简单，运算效率比较高。

　　包装类的优势：有的容易，比如集合的元素必须是对象类型，满足了 java 一切皆是对象的思想。

## 2. 引用数据类型

　　Java 有 5 中引用类型（对象类型）：类、接口、数组、枚举、标注。

　　引用类型：底层结构和基本类型差别较大。

　　JVM 的内存空间：

1. Heap 堆空间：分配对象 new Student;
2. Stack 栈空间：临时变量 Student stu；
3. Code 代码区：类的定义，静态资源 Student.class。

　　例子：

```java
Student stu = new Student(); // new 在内存的堆空间创建对象
stu.study(); //把对象的地址赋给 stu 引用对象
```

　　例子实现步骤：

1. JVM 加载 Student.class 到 Code 区。
2. new Student() 在堆空间分配空间并创建一个 Student 实例。
3. 将此实例的地址赋值给引用 stu，栈空间。



1. 引用类型变量由类的构造函数创建，可以使用它们访问所引用的对象。这些变量在声明时被指定为一个特定的类型，比如 Employee、Pubby 等。变量一旦声明，类型就不能被改变了。
2. 对象、数组都是引用数据类型。
3. 所有引用类型的默认值都是 null。
4. 一个引用变量可以用来引用与任何与之兼容的类型。
5. 例子：Animal animal = new Animal(”giraffe“)；

## 3. Java 中的常量

　　常量就是一个固定值。它们不需要计算，直接代表相应的值。

　　常量指不能改变的值。在 Java 中用 final 标志，声明方法和变量类似：

```java
final double PI = 3.1415927;
```

　　虽然常量名也可以用小写，但为了便于识别，通常使用大写字母表示常量。

　　字面量可以赋给任何内置类型的变量。例如：

```java
byte a = 68;
char a = 'A';
```

　　byte、int、long 和 short 都可以用十进制、十六进制以及八进程的方式来表示。

　　十六禁止整型常量：以十六进制表示时，需以 0x或者 0X 开头，如 Oxff、OX9A。

　　八进制整型常量：八进制必须以 0 开头，如 0123、034。

　　长整型：长整型必须以 L 作结尾，如 9L、342L。

　　浮点数常量：由于小数常量的默认类型是 double 型，所以 float 类型的后面一定要加 f 或者 F。同样带小数的变量默认为 double 类型。如 float f = 1.3f;

　　当使用常量的时候，前缀 0 表示 8 进制，而前缀 0x 表示 16 进制，例如：

```java
int decimal = 100;
int octal = 0144;
int hexa = 0x64;
```

　　和其他语言一样，Java 的字符串常量也是包含在两个引号之间的字符序列。下面是字符串型字面量的例子：

```java
"Hello World"
"two\nlines"
"\"This is in quotes\""
```

　　字符串常量和字符常量都可以包含任何 Unicode 字符。例如：

```java
char a = '\u0001';
String a = "\u0001";
```

　　字符常量：字符型常量需用两个单引号括起来（注意字符串常量是用两个双引号括起来）。Java 中的字符占两个字节。Java 语言支持一些特殊的转义字符：

| 符号    | 字符含义                     |
| ------- | ---------------------------- |
| \n      | 换行（0x0a）                 |
| \r      | 回车（0x0d）                 |
| \f      | 换页符（0x0c）               |
| \b      | 退格（0x08）                 |
| \s      | 空格（0x20）                 |
| \t      | 制表符                       |
| \ "     | 双引号                       |
| \ '     | 单引号                       |
| \ \     | 反斜杠                       |
| \ ddd   | 八进制字符（ddd）            |
| \ uxxxx | 16 进制 Unicode 字符（xxxx） |

## 4. 数据类型之间的转换

1. 简单类型数据间的转换，有两种方式：自动转换和强制转换，通常发生在表达式中或方法的参数传递时。

   自动转换：具体地讲，当一个较 “ 小 ” 数据与一个较 “大” 的数据一起运算时，系统将自动将 “ 小 ” 数据转换成 “ 大 ” 数据，再进行运算。而在方法调用时，实际参数较 “ 小 ”，而被调用的方法的形式参数数据又较 “ 大 ” 时（若有普配的，当然会直接调用匹配的方法），系统也将自动将 “ 小 ” 数据转换成 “ 大 ” 数据，再进行方法的调用，自然，对于多个同名的重载方法，会转换成最 “ 接近 ” 的 “ 大 ” 数据并进行调用。这些类型由 “ 小 ” 到 “ 大 ” 分别为 （byte、short、char）- int - long -  float - double。这里所说的 ” 大 “ 与 ” 小 “，并不是指占用字节的多少，而是指表示值得范围的大小。

   ```java
   byte b;int i = b;long l = b;float f = b; double d = b; //直接通过
   char c='c'; int i = c; // 如果时低级类型为 char 型，向高级类型（整型）转换时，会转换为对应 ASCII 码值。i = 99
   short i = 99; char c = (char)i //输出 c。
   ```

   强制转换：将 ” 大 “ 数据转换为 ” 小 “ 数据时，可以使用强制类型转换。但是必须采用下面语句格式：

   ```java
   int n = (int)3.14159/2;
   ```

   这种转换肯定可能会导致溢出或精度的下降。

2. 表达式的数据类型自动提升，关于类型的自动提升，注意下面的规则。

   * 所有的 byte、short、char 型的值将被提升为 int 型；
   * 如果有一个操作数是 long 型，计算结果是 long 型；
   * 如果有一个操作数是 float 型，计算结果是 float 型；
   * ru锅有一个操作数是 double 型，计算结果是 double 型；

   ```java
   byte b=3; b = (byte)(b*3);//必须声明 byte
   ```

3. 包装类过渡类型转换

   一般情况下，首先声明一个变量，然后生成一个对应的包装类，就可以利用包装类的各种方法进行类型转换了。

   ```java
   // 1. 希望把 float 型转换为 double 型时
   float f1 = 100.000f;
   Float F1 = new Float(f1);
   double d1 = F1.doubleValue);// F1.doubleValue() 为 Float 类的返回 double 值型的方法
   
   // 2. 当希望把 double 型转换为 int 型时
   double d1 = 100.00;
   Double D1 = new Double(d1);
   int i1 = D1.intValue();
   ```

   简单类型的变量转换为相应的包装类，可以利用包装类的构造函数。即：Boolean(boolean value)、Character(char value)、Integer(int value)、Long(long value)、Float(float value)、Double(double value)。

   而在各个包装类中，总有形为 xxValue() 的方法，来得到其对应的简单类型数据。利用这种方法，也可以实现不同数值型变量间的转换，例如，对于一个双精度实型类，intValue() 可以得到其对应的整型变量，而 doubleValue() 可以得到其对应的双精度实型变量。

4. 字符串与其他类型间的转换

   其他类型向字符串的转换：

   * 调用类的串转换方法：X.toString()；
   * 自动转化：x+"";
   * 使用 String 的方法：String.valueOf(x)；

   字符串作为值，向其他类型的转换：

   * 先转换成相应的封装器实例，再调用对应的方法转换成其他类型。

     ```java
     // 字符 " 32.1 " 转换 double 型的值的格式
     new Float("32.1").doubleValue();
     // 或者
     Double.valueOf("32.1").doubleValue();
     ```

   * 静态 parsexxx 方法

     ```java
     String s = "1";
     byte b = Byte.parseByte(s);
     short t = Short.parseShort(s);
     int i = Integer.parseInt(s);
     long l = Long.parseLong(s);
     Float f = Float.parseFloat(s);
     Double d = Double.parseDouble(s);
     ```

   * Character 的 getNumericValue(char ch) 方法

5. Data 类与其他数据类型的相互转换

   整型和 Data 类之间并不存在直接的对应关系，只是可以使用 int 型分别表示年、月、日、时、分、秒，这样就在两者之间建立了一个对应关系，在作这种转换时，可以使用 Date 类构造函数的三种形式：

   ```java
   Date(int year, int month, int date); //以 int 型表示年、月、日
   Date(int year, int month, int date, int hrs, int min);// 以 int 型表示年、月、日、时、分
   Date(int year, int month, int date, int hrs, int min, int sec);// 以 int 型表示年、月、日、时、分、秒
   ```

   在长整型和 Date 类之间有一个对应关系，就是将一个时间表示为举例格林尼标准时间 1970 年 1 月 1 日 0 时 0 分 0 秒的毫秒数。对于这种对应关系，Date 类也有其相对应的构造函数：Date(long date)。

   获取 Date 类中的年、月、日、时、分、秒以及星期可以使用 Date 类的 getYear()、getMonth()、getDate()、getHours()、getMinutes()、getSeconds()、getDay() 方法，也可以将其理解为将 Date 类转换成 int。

   而 Date 类的 getTime() 方法可以得到一个时间对应的长整型数，与包装类一样，Date 类也有一个 toString() 方法可以将其转换为 String 类。



## 参考文章

https://blog.csdn.net/boss2967/article/details/80108112

https://www.cnblogs.com/dubo-/p/5565677.html



