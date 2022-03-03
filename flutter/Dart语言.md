# Dart语言

## 1. 变量声明

### 1.1. var

类似于 JavaScript 中的`var`，它可以接收任何类型的变量，但最大的不同是 Dart 中 var 变量一旦赋值，类型便会确定，则不能再改变其类型。

Dart 本身是一个强类型语言，任何变量都是有确定类型的，在 Dart 中，当用`var`声明一个变量后，Dart 在编译时会根据第一次赋值数据的类型来推断其类型，编译结束后其类型就已经被确定，而 JavaScript 是纯粹的弱类型脚本语言，var 只是变量的声明方式而已。

### 1.2. dynamic 和 Object

`Object` 是 Dart 所有对象的根基类，也就是说在 Dart 中所有类型都是`Object`的子类(包括Function和Null)，所以任何类型的数据都可以赋值给`Object`声明的对象。 `dynamic`与`Object`声明的变量都可以赋值任意对象，且后期可以改变赋值的类型，这和 `var` 是不同的，如：

```dart
dynamic t;
Object x;
t = "hi world";
x = 'Hello Object';
//下面代码没有问题
t = 1000;
x = 1000;
```

`dynamic`与`Object`不同的是`dynamic`声明的对象编译器会提供所有可能的组合，而`Object`声明的对象只能使用 `Object` 的属性与方法, 否则编译器会报错，如:

```dart
 dynamic a;
 Object b = "";
 main() {
   a = "";
   printLengths();
 }   

 printLengths() {
   // 正常
   print(a.length);
   // 报错 The getter 'length' is not defined for the class 'Object'
   print(b.length);
 }
```

`dynamic` 的这个特点使得我们在使用它时需要格外注意，这很容易引入一个运行时错误，比如下面代码在编译时不会报错，而在运行时会报错：

```dart
print(a.xx); // a是字符串，没有"xx"属性，编译时不会报错，运行时会报错
```

### 1.3. final 和 const

如果从未打算更改一个变量，那么使用 `final` 或 `const`，不是`var`，也不是一个类型。 一个 `final` 变量只能被设置一次，两者区别在于：`const` 变量是一个编译时常量（编译时直接替换为常量值），`final`变量在第一次使用时被初始化。被`final`或者`const`修饰的变量，变量类型可以省略，如：

```dart
//可以省略String这个类型声明
final str = "hi world";
//final String str = "hi world"; 
const str1 = "hi world";
//const String str1 = "hi world";
```

### 1.4. 空安全（null-safety）

有了空安全，则定义变量时可以指定变量是可空还是不可空。

```dart
int i = 8; //默认为不可空，必须在定义时初始化。
int? j; // 定义为可空类型，对于可空变量，我们在使用前必须判空。

// 如果我们预期变量不能为空，但在定义时不能确定其初始值，则可以加上late关键字，
// 表示会稍后初始化，但是在正式使用它之前必须得保证初始化过了，否则会报错
late int k;
k=9;
```

如果一个变量我们定义为可空类型，在某些情况下即使我们给它赋值过了，但是预处理器仍然有可能识别不出，这时我们就要显式（通过在变量后面加一个”!“符号）告诉预处理器它已经不是null了，比如：

```dart
class Test{
  int? i;
  Function? fun;
  say(){
    if(i!=null) {
      print(i! * 8); //因为已经判过空，所以能走到这 i 必不为null，如果没有显式申明，则 IDE 会报错
    }
    if(fun!=null){
      fun!(); // 同上
    }
  }
}
```

上面中如果函数变量可空时，调用的时候可以用语法糖：

```dart
fun?.call() // fun 不为空时则会被调用
```

## 2. 函数

Dart是一种真正的面向对象的语言，所以即使是函数也是对象，并且有一个类型**Function**。这意味着函数可以赋值给变量或作为参数传递给其他函数，这是函数式编程的典型特征。

### 2.1. 函数声明

