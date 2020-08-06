# Java 8 的新特性

# 1. 接口的默认方法

　　Java 8 允许给接口添加一个非抽象的方法实现，只需要使用 default关键字即可，这个特征又叫做扩展方法，示例如下：

```csharp
interface Formula {
    double calculate(int a);

    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}
```

　　Formula 接口在拥有 calculate 方法之外同时还定义了 sqrt 方法，实现了 Formula 接口的子类只需要实现一个 calculate 方法，默认方法 sqrt 在子类上可以直接使用。

```cpp
Formula formula = new Formula() {
    @Override
    public double calculate(int a) {
        return sqrt(a * 100);
    }
};

formula.calculate(100);     // 100.0
formula.sqrt(16);           // 4.0
```

　　文中的 formula 被实现为一个匿名类的实例，该代码非常容易理解，6 行代码实现了计算 sqrt(a * 100)。

　　译者注： 在 Java 中只有单继承，如果要让一个类赋予新的特性，通常是使用接口来实现，在 C++ 中支持多继承，允许一个子类同时具有多个父类的接口与功能，在其他语言中，让一个类同时具有其他的可复用代码的方法叫做 mixin。新的 Java 8 的这个特新在编译器实现的角度上来说更加接近 Scala 的 trait。 在 C# 中也有名为扩展方法的概念，允许给已存在的类型扩展方法，和 Java 8 的这个在语义上有差别。

# 2. Lambda 表达式

　　首先看看在老版本的Java中是如何排列字符串的：

```java
List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");

Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return b.compareTo(a);
    }
});
```

　　只需要给静态方法 Collections.sort 传入一个 List 对象以及一个比较器来按指定顺序排列。通常做法都是创建一个匿名的比较器对象然后将其传递给 sort 方法。

　　在 Java 8 中你就没必要使用这种传统的匿名对象的方式了，Java 8提供了更简洁的语法，lambda 表达式：

```java
Collections.sort(names, (String a, String b) -> {
    return b.compareTo(a);
});
```

　　看到了吧，代码变得更段且更具有可读性，但是实际上还可以写得更短：

```java
Collections.sort(names, (String a, String b) -> b.compareTo(a));
```

　　对于函数体只有一行代码的，你可以去掉大括号 {} 以及 return 关键字，但是你还可以写得更短点：

```java
Collections.sort(names, (a, b) -> b.compareTo(a));
```

　　Java 编译器可以自动推导出参数类型，所以你可以不用再写一次类型。

# 3. 函数式接口

　　Lambda 表达式是如何在 java 的类型系统中表示的呢？每一个lambda 表达式都对应一个类型，通常是接口类型。而 “ 函数式接口 ” 是指仅仅只包含一个抽象方法的接口，每一个该类型的lambda表达式都会被匹配到这个抽象方法。因为 默认方法 不算抽象方法，所以你也可以给你的函数式接口添加默认方法。

　　我们可以将 lambda 表达式当作任意只包含一个抽象方法的接口类型，确保你的接口一定达到这个要求，你只需要给你的接口添加 @FunctionalInterface 注解，编译器如果发现你标注了这个注解的接口有多于一个抽象方法的时候会报错的。

```tsx
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
Integer converted = converter.convert("123");
System.out.println(converted);    // 123
```

　　需要注意如果@FunctionalInterface如果没有指定，上面的代码也是对的。

　　译者注：将 lambda 表达式映射到一个单方法的接口上，这种做法在 Java 8 之前就有别的语言实现，比如 Rhino JavaScript 解释器，如果一个函数参数接收一个单方法的接口而你传递的是一个 function，Rhino 解释器会自动做一个单接口的实例到 function 的适配器，典型的应用场景有 org.w3c.dom.events.EventTarget 的addEventListener 第二个参数 EventListener。

# 4. 方法与构造函数引用

　　前一节中的代码还可以通过静态方法引用来表示：

```rust
Converter<String, Integer> converter = Integer::valueOf;
Integer converted = converter.convert("123");
System.out.println(converted);   // 123
```

　　Java 8 允许你使用 :: 关键字来传递方法或者构造函数引用，上面的代码展示了如何引用一个静态方法，我们也可以引用一个对象的方法：

```ruby
 converter = something::startsWith;
String converted = converter.convert("Java");
System.out.println(converted);    // "J"
```

　　接下来看看构造函数是如何使用 :: 关键字来引用的，首先我们定义一个包含多个构造函数的简单类：

```dart
class Person {
    String firstName;
    String lastName;

    Person() {}

    Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
```

　　接下来我们指定一个用来创建Person对象的对象工厂接口：

```dart
interface PersonFactory<P extends Person> {
    P create(String firstName, String lastName);
}
```

　　这里我们使用构造函数引用来将他们关联起来，而不是实现一个完整的工厂：

```cpp
PersonFactory<Person> personFactory = Person::new;
Person person = personFactory.create("Peter", "Parker");
```

　　我们只需要使用 Person::new 来获取 Person 类构造函数的引用，Java 编译器会自动根据 PersonFactory.create 方法的签名来选择合适的构造函数。

# 5. Lambda 作用域

　　在 lambda 表达式中访问外层作用域和老版本的匿名对象中的方式很相似。你可以直接访问标记了final的外层局部变量，或者实例的字段以及静态变量。

# 6. 访问局部变量

　　我们可以直接在 lambda 表达式中访问外层的局部变量：

```dart
final int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

　　但是和匿名对象不同的是，这里的变量 num 可以不用声明为 final，该代码同样正确：

```dart
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

　　不过这里的 num 必须不可被后面的代码修改（即隐性的具有final的语义），例如下面的就无法编译：

```dart
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
num = 3;
```

　　在 lambda 表达式中试图修改 num 同样是不允许的。

# 7. 访问对象字段与静态变量

　　和本地变量不同的是，lambda 内部对于实例的字段以及静态变量是即可读又可写。该行为和匿名对象是一致的：

```java
class Lambda4 {
    static int outerStaticNum;
    int outerNum;

    void testScopes() {
        Converter<Integer, String> stringConverter1 = (from) -> {
            outerNum = 23;
            return String.valueOf(from);
        };

        Converter<Integer, String> stringConverter2 = (from) -> {
            outerStaticNum = 72;
            return String.valueOf(from);
        };
    }
}
```

# 8. 访问接口的默认方法

　　还记得第一节中的 formula 例子么，接口 Formula 定义了一个默认方法 sqrt 可以直接被 formula 的实例包括匿名对象访问到，但是在 lambda 表达式中这个是不行的。
　　Lambda 表达式中是无法访问到默认方法的，以下代码将无法编译：

```cpp
Formula formula = (a) -> sqrt( a * 100);
Built-in Functional Interfaces
```

　　JDK 1.8 API 包含了很多内建的函数式接口，在老 Java 中常用到的比如 Comparator 或者 Runnable 接口，这些接口都增加了 @FunctionalInterface 注解以便能用在 lambda 上。
 Java 8 API 同样还提供了很多全新的函数式接口来让工作更加方便，有一些接口是来自 Google Guava 库里的，即便你对这些很熟悉了，还是有必要看看这些是如何扩展到 lambda 上使用的。

## 8.1. Predicate接口

　　Predicate 接口只有一个参数，返回 **boolean** 类型。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）：

```java
Predicate<String> predicate = (s) -> s.length() > 0;

predicate.test("foo");              // true
predicate.negate().test("foo");     // false

Predicate<Boolean> nonNull = Objects::nonNull;
Predicate<Boolean> isNull = Objects::isNull;

Predicate<String> isEmpty = String::isEmpty;
Predicate<String> isNotEmpty = isEmpty.negate();
```

## 8.2. Function 接口

　　Function 接口有一个参数并且返回一个结果，并附带了一些可以和其他函数组合的默认方法（compose, andThen）：

```dart
Function<String, Integer> toInteger = Integer::valueOf;
Function<String, String> backToString = toInteger.andThen(String::valueOf);

backToString.apply("123");     // "123"
```

## 8.3. Supplier 接口

　　Supplier 接口返回一个任意范型的值，和 Function 接口不同的是该接口没有任何参数。

```dart
Supplier<Person> personSupplier = Person::new;
personSupplier.get();   // new Person
```

## 8.4. Consumer 接口

　　Consumer 接口表示执行在单个参数上的操作。

```csharp
Consumer<Person> greeter = (p) -> System.out.println("Hello, " + p.firstName);
greeter.accept(new Person("Luke", "Skywalker"));
```

## 8.5. Comparator 接口

　　Comparator 是老 Java 中的经典接口， Java 8 在此之上添加了多种默认方法：

```cpp
Comparator<Person> comparator = (p1, p2) -> p1.firstName.compareTo(p2.firstName);

Person p1 = new Person("John", "Doe");
Person p2 = new Person("Alice", "Wonderland");

comparator.compare(p1, p2);             // > 0
comparator.reversed().compare(p1, p2);  // < 0
```

## 8.6. Optional 接口

　　Optional 不是函数是接口，这是个用来防止 NullPointerException 异常的辅助类型，这是下一届中将要用到的重要概念，现在先简单的看看这个接口能干什么：

　　Optional 被定义为一个简单的容器，其值可能是null或者不是null。在 Java 8 之前一般某个函数应该返回非空对象但是偶尔却可能返回了null，而在 Java 8 中，不推荐你返回 null 而是返回 Optional。

```swift
Optional<String> optional = Optional.of("bam");

optional.isPresent();           // true
optional.get();                 // "bam"
optional.orElse("fallback");    // "bam"

optional.ifPresent((s) -> System.out.println(s.charAt(0)));     // "b"
```

## 8.7. Stream 接口

　　java.util.Stream 表示能应用在一组元素上一次执行的操作序列。Stream 操作分为中间操作或者最终操作两种，最终操作返回一特定类型的计算结果，而中间操作返回 Stream 本身，这样你就可以将多个操作依次串起来。Stream 的创建需要指定一个数据源，比如  java.util.Collection 的子类，List 或者 Set， Map 支持。Stream 的操作可以串行执行或者并行执行。

　　首先看看 Stream 是怎么用，首先创建实例代码的用到的数据 List：

```csharp
List<String> stringCollection = new ArrayList<>();
stringCollection.add("ddd2");
stringCollection.add("aaa2");
stringCollection.add("bbb1");
stringCollection.add("aaa1");
stringCollection.add("bbb3");
stringCollection.add("ccc");
stringCollection.add("bbb2");
stringCollection.add("ddd1");
```

　　Java 8扩展了集合类，可以通过 Collection.stream() 或者 Collection.parallelStream() 来创建一个Stream。下面几节将详细解释常用的 Stream 操作：

### 8.7.1. Filter 过滤

　　过滤通过一个 predicate 接口来过滤并只保留符合条件的元素，该操作属于中间操作，所以我们可以在过滤后的结果来应用其他 Stream 操作（比如 forEach）。forEach 需要一个函数来对过滤后的元素依次执行。forEach 是一个最终操作，所以我们不能在 forEach 之后来执行其他 Stream 操作。

```ruby
stringCollection
    .stream()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);

// "aaa2", "aaa1"
```

### 8.7.2. Sort 排序

　　排序是一个中间操作，返回的是排序好后的Stream。如果你不指定一个自定义的 Comparator 则会使用默认排序。

```ruby
stringCollection
    .stream()
    .sorted()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);

// "aaa1", "aaa2"
```

　　需要注意的是，排序只创建了一个排列好后的 Stream，而不会影响原有的数据源，排序之后原数据 stringCollection 是不会被修改的。

```csharp
System.out.println(stringCollection);
// ddd2, aaa2, bbb1, aaa1, bbb3, ccc, bbb2, ddd1
```

### 8.7.3. Map 映射

　　中间操作 map 会将元素根据指定的 Function 接口来依次将元素转成另外的对象，下面的示例展示了将字符串转换为大写字符串。你也可以通过 map 来将对象转换成其他类型，map 返回的 Stream 类型是根据你 map 传递进去的函数的返回值决定的。

```java
stringCollection
    .stream()
    .map(String::toUpperCase)
    .sorted((a, b) -> b.compareTo(a))
    .forEach(System.out::println);

// "DDD2", "DDD1", "CCC", "BBB3", "BBB2", "AAA2", "AAA1"
```

### 8.7.4. Match 匹配

　　Stream 提供了多种匹配操作，允许检测指定的 Predicate 是否匹配整个 Stream。所有的匹配操作都是最终操作，并返回一个 boolean 类型的值。

```java
boolean anyStartsWithA = 
    stringCollection
        .stream()
        .anyMatch((s) -> s.startsWith("a"));

System.out.println(anyStartsWithA);      // true

boolean allStartsWithA = 
    stringCollection
        .stream()
        .allMatch((s) -> s.startsWith("a"));

System.out.println(allStartsWithA);      // false

boolean noneStartsWithZ = 
    stringCollection
        .stream()
        .noneMatch((s) -> s.startsWith("z"));

System.out.println(noneStartsWithZ);      // true
```

### 8.7.5. Count 计数
　　计数是一个最终操作，返回 Stream 中元素的个数，返回值类型是long。

```java
long startsWithB = 
    stringCollection
        .stream()
        .filter((s) -> s.startsWith("b"))
        .count();

System.out.println(startsWithB);    // 3
```

### 8.7.6. Reduce 规约

　　这是一个最终操作，允许通过指定的函数来将 stream 中的多个元素规约为一个元素，规越后的结果是通过Optional接口表示的：

```rust
Optional<String> reduced =
    stringCollection
        .stream()
        .sorted()
        .reduce((s1, s2) -> s1 + "#" + s2);

reduced.ifPresent(System.out::println);
// "aaa1#aaa2#bbb1#bbb2#bbb3#ccc#ddd1#ddd2"
```

### 8.7.7. 并行 Streams

　　前面提到过 Stream 有串行和并行两种，串行 Stream 上的操作是在一个线程中依次完成，而并行 Stream 则是在多个线程上同时执行。

下面的例子展示了是如何通过并行 Stream 来提升性能：

首先我们创建一个没有重复元素的大表：

```java
int max = 1000000;
List<String> values = new ArrayList<>(max);
for (int i = 0; i < max; i++) {
    UUID uuid = UUID.randomUUID();
    values.add(uuid.toString());
}
```

　　然后我们计算一下排序这个 Stream 要耗时多久，

 #### 8.7.7.1. 串行排序

```csharp
long t0 = System.nanoTime();

long count = values.stream().sorted().count();
System.out.println(count);

long t1 = System.nanoTime();

long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
System.out.println(String.format("sequential sort took: %d ms", millis));
```

　　// 串行耗时: 899 ms

#### 8.7.7.2. 并行排序

```csharp
long t0 = System.nanoTime();

long count = values.parallelStream().sorted().count();
System.out.println(count);

long t1 = System.nanoTime();

long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
System.out.println(String.format("parallel sort took: %d ms", millis));
```

　　// 并行排序耗时: 472 ms
　　上面两个代码几乎是一样的，但是并行版的快了 50% 之多，唯一需要做的改动就是将 stream() 改为 parallelStream()。

## 8.8. Map

　　前面提到过，Map 类型不支持 stream，不过 Map 提供了一些新的有用的方法来处理一些日常任务。

```java
Map<Integer, String> map = new HashMap<>();

for (int i = 0; i < 10; i++) {
    map.putIfAbsent(i, "val" + i);
}

map.forEach((id, val) -> System.out.println(val));
```

　　以上代码很容易理解， putIfAbsent  不需要我们做额外的存在性检查，而 forEach 则接收一个 Consumer 接口来对 map 里的每一个键值对进行操作。

下面的例子展示了map上的其他有用的函数：

```java
map.computeIfPresent(3, (num, val) -> val + num);
map.get(3);             // val33

map.computeIfPresent(9, (num, val) -> null);
map.containsKey(9);     // false

map.computeIfAbsent(23, num -> "val" + num);
map.containsKey(23);    // true

map.computeIfAbsent(3, num -> "bam");
map.get(3);             // val33
```

　　接下来展示如何在 Map 里删除一个键值全都匹配的项:

```java
map.remove(3, "val3");
map.get(3);             // val33

map.remove(3, "val33");
map.get(3);             // null
```

　　另外一个有用的方法

```cpp
map.getOrDefault(42, "not found");  // not found
```

　　对 Map 的元素做合并也变得很容易了：

```csharp
map.merge(9, "val9", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9

map.merge(9, "concat", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9concat
```

　　Merge 做的事情是如果键名不存在则插入，否则则对原键对应的值做合并操作并重新插入到map中。

# 9. Date API

　　Java 8 在包 java.time 下包含了一组全新的时间日期API。新的日期API 和开源的 Joda-Time 库差不多，但又不完全一样，下面的例子展示了这组新 API 里最重要的一些部分：

## 9.1. Clock 时钟

　　Clock 类提供了访问当前日期和时间的方法，Clock 是时区敏感的，可以用来取代 System.currentTimeMillis() 来获取当前的微秒数。某一个特定的时间点也可以使用 Instant 类来表示，Instant 类也可以用来创建老的 java.util.Date 对象。

```csharp
Clock clock = Clock.systemDefaultZone();
long millis = clock.millis();

Instant instant = clock.instant();
Date legacyDate = Date.from(instant);   // legacy java.util.Date
```

## 9.2. Timezones 时区

　　在新 API 中时区使用 ZoneId 来表示。时区可以很方便的使用静态方法 of 来获取到。 时区定义了到 UTS 时间的时间差，在 Instant 时间点对象到本地日期对象之间转换的时候是极其重要的。

```csharp
System.out.println(ZoneId.getAvailableZoneIds());
// prints all available timezone ids

ZoneId zone1 = ZoneId.of("Europe/Berlin");
ZoneId zone2 = ZoneId.of("Brazil/East");
System.out.println(zone1.getRules());
System.out.println(zone2.getRules());

// ZoneRules[currentStandardOffset=+01:00]
// ZoneRules[currentStandardOffset=-03:00]
```

## 9.3. LocalTime 本地时间

　　LocalTime 定义了一个没有时区信息的时间，例如 晚上 10 点，或者 17:30:15。下面的例子使用前面代码创建的时区创建了两个本地时间。之后比较时间并以小时和分钟为单位计算两个时间的时间差：

```csharp
LocalTime now1 = LocalTime.now(zone1);
LocalTime now2 = LocalTime.now(zone2);

System.out.println(now1.isBefore(now2));  // false

long hoursBetween = ChronoUnit.HOURS.between(now1, now2);
long minutesBetween = ChronoUnit.MINUTES.between(now1, now2);

System.out.println(hoursBetween);       // -3
System.out.println(minutesBetween);     // -239
```

　　LocalTime 提供了多种工厂方法来简化对象的创建，包括解析时间字符串。

```csharp
LocalTime late = LocalTime.of(23, 59, 59);
System.out.println(late);       // 23:59:59

DateTimeFormatter germanFormatter =
    DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(Locale.GERMAN);

LocalTime leetTime = LocalTime.parse("13:37", germanFormatter);
System.out.println(leetTime);   // 13:37
```

## 9.4. LocalDate 本地日期

　　LocalDate 表示了一个确切的日期，比如 2014-03-11。该对象值是不可变的，用起来和 LocalTime 基本一致。下面的例子展示了如何给 Date 对象加减天/月/年。另外要注意的是这些对象是不可变的，操作返回的总是一个新实例。

```java
LocalDate today = LocalDate.now();
LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
LocalDate yesterday = tomorrow.minusDays(2);

LocalDate independenceDay = LocalDate.of(2014, Month.JULY, 4);
DayOfWeek dayOfWeek = independenceDay.getDayOfWeek();

System.out.println(dayOfWeek);    // FRIDAY
```

　　从字符串解析一个 LocalDate 类型和解析 LocalTime 一样简单：

```java
DateTimeFormatter germanFormatter =
    DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.GERMAN);

LocalDate xmas = LocalDate.parse("24.12.2014", germanFormatter);
System.out.println(xmas);   // 2014-12-24
```

## 9.5. LocalDateTime 本地日期时间

　　LocalDateTime 同时表示了时间和日期，相当于前两节内容合并到一个对象上了。LocalDateTime 和 LocalTime 还有 LocalDate 一样，都是不可变的。LocalDateTime 提供了一些能访问具体字段的方法。

```csharp
LocalDateTime sylvester = LocalDateTime.of(2014, Month.DECEMBER, 31, 23, 59, 59);

DayOfWeek dayOfWeek = sylvester.getDayOfWeek();
System.out.println(dayOfWeek);      // WEDNESDAY

Month month = sylvester.getMonth();
System.out.println(month);          // DECEMBER

long minuteOfDay = sylvester.getLong(ChronoField.MINUTE_OF_DAY);
System.out.println(minuteOfDay);    // 1439
```

　　只要附加上时区信息，就可以将其转换为一个时间点 Instant 对象，Instant 时间点对象可以很容易的转换为老式的 java.util.Date。

```java
Instant instant = sylvester
        .atZone(ZoneId.systemDefault())
        .toInstant();

Date legacyDate = Date.from(instant);
System.out.println(legacyDate);     // Wed Dec 31 23:59:59 CET 2014
```

　　格式化 LocalDateTime 和格式化时间和日期一样的，除了使用预定义好的格式外，我们也可以自己定义格式：

```java
DateTimeFormatter formatter =
    DateTimeFormatter
        .ofPattern("MMM dd, yyyy - HH:mm");

LocalDateTime parsed = LocalDateTime.parse("Nov 03, 2014 - 07:13", formatter);
String string = formatter.format(parsed);
System.out.println(string);     // Nov 03, 2014 - 07:13
```

　　和 java.text.NumberFormat 不一样的是新版的 DateTimeFormatter 是不可变的，所以它是线程安全的。

# 10. Annotation 注解

　　在 Java 8 中支持多重注解了，先看个例子来理解一下是什么意思。
 首先定义一个包装类 Hints 注解用来放置一组具体的 Hint 注解：

```css
@interface Hints {
    Hint[] value();
}

@Repeatable(Hints.class)
@interface Hint {
    String value();
}
```

　　Java 8 允许我们把同一个类型的注解使用多次，只需要给该注解标注一下 @Repeatable 即可。

　　例 1: 使用包装类当容器来存多个注解（老方法）

```kotlin
@Hints({@Hint("hint1"), @Hint("hint2")})
class Person {}
```

　　例 2：使用多重注解（新方法）

```kotlin
@Hint("hint1")
@Hint("hint2")
class Person {}
```

　　第二个例子里 java 编译器会隐性的帮你定义好 @Hints 解，了解这一点有助于你用反射来获取这些信息：

```kotlin
Hint hint = Person.class.getAnnotation(Hint.class);
System.out.println(hint);                   // null

Hints hints1 = Person.class.getAnnotation(Hints.class);
System.out.println(hints1.value().length);  // 2

Hint[] hints2 = Person.class.getAnnotationsByType(Hint.class);
System.out.println(hints2.length);          // 2
```

　　即便我们没有在 Person 类上定义 @Hints 注解，我们还是可以通过  getAnnotation(Hints.class)  来获取 @Hints 注解，更加方便的方法是使用 getAnnotationsByType 可以直接获取到所有的 @Hint 注解。
　　另外 Java 8 的注解还增加到两种新的 target 上了：

```css
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@interface MyAnnotation {}
```

　　关于Java 8的新特性就写到这了，肯定还有更多的特性等待发掘。 JDK 1.8 里还有很多很有用的东西，比如 Arrays.parallelSort,  StampedLock 和 CompletableFuture 等等。

# 11. 参考文章

[JAVA8十大新特性详解（精编）](https://www.jianshu.com/p/0bf8fe0f153b)