# Dart语言

在线调试语法地址：https://dartpad.dartlang.org/。

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

Dart 是一种真正的面向对象的语言，所以即使是函数也是对象，并且有一个类型 Function。这意味着函数可以赋值给变量或作为参数传递给其他函数，这是函数式编程的典型特征。

### 2.1. 函数声明

```dart
bool isNoble(int atomicNumber) {
  return _nobleGases[atomicNumber] != null;
}
```

Dart 函数声明如果没有显式声明返回值类型时会默认当做 dynamic 处理，注意，函数返回值没有类型推断：

```dart
typedef bool CALLBACK();

//不指定返回类型，此时默认为dynamic，不是bool
isNoble(int atomicNumber) {
  return _nobleGases[atomicNumber] != null;
}

void test(CALLBACK cb){
   print(cb()); 
}
//报错，isNoble不是bool类型
test(isNoble);
```

* 对于只包含一个表达式的函数，可以使用简写语法

```dart
bool isNoble (int atomicNumber)=> true ;   
```

* 函数作为变量

```dart
var say = (str){
  print(str);
};
say("hi world");
```

* 函数作为参数传递

```dart
void execute(var callback) {
    callback();
}
execute(() => print("xxx"))
```

* 可选的位置参数

包装一组函数参数，用[]标记为可选的位置参数，并放在参数列表的最后面：

```dart
String say(String from, String msg, [String device]) {
  var result = '$from says $msg';
  if (device != null) {
    result = '$result with a $device';
  }
  return result;
}
```

下面是一个不带可选参数调用这个函数的例子：

```dart
say('Bob', 'Howdy'); //结果是： Bob says Howdy
```

下面是用第三个参数调用这个函数的例子：

```dart
say('Bob', 'Howdy', 'smoke signal'); //结果是：Bob says Howdy with a smoke signal
```

* 可选的命名参数

定义函数时，使用{param1, param2, …}，放在参数列表的最后面，用于指定命名参数。例如：

```dart
//设置[bold]和[hidden]标志
void enableFlags({bool bold, bool hidden}) {
    // ... 
}
```

调用函数时，可以使用指定命名参数。例如：paramName: value

```dart
enableFlags(bold: true, hidden: false);
```

可选命名参数在Flutter中使用非常多。注意，不能同时使用可选的位置参数和可选的命名参数。

## 3. mixin

Dart 是不支持多继承的，但是它支持 mixin，简单来讲 mixin 可以 “组合” 多个类。

举例：定义一个 Person 类，实现吃饭、说话、走路和写代码功能，同时定义一个 Dog 类，实现吃饭、和走路功能。

```dart
class Person {
  say() {
    print('say');
  }
}

mixin Eat {
  eat() {
    print('eat');
  }
}

mixin Walk {
  walk() {
    print('walk');
  }
}

mixin Code {
  code() {
    print('key');
  }
}

class Dog with Eat, Walk{}
class Man extends Person with Eat, Walk, Code{}
```

注意：如果多个 mixin 中有同名方法，with 时，会默认使用最后面的 mixin 的，mixin 方法中可以通过 super 关键字调用之前 mixin 或类中的方法。

## 4. 异步支持

Dart 类库有非常多的返回 Future 或者 Stream 对象的函数。 这些函数被称为**异步函数**：它们只会在设置好一些耗时操作之后返回，比如像 IO 操作。而不是等到这个操作完成。

async 和 await 关键词支持了异步编程，允许您写出和同步代码很像的异步代码。

### 4.1. Future

 Future 与 JavaScript 中的 Promise 非常相似，表示一个异步操作的最终完成（或失败）及其结果值的表示。简单来说，它就是用于处理异步操作的，异步处理成功了就执行成功的操作，异步处理失败了就捕获错误或者停止后续操作。一个 Future 只会对应一个结果，要么成功，要么失败。

Future 的所有 API 的返回值仍然是一个 Future 对象，所以可以很方便的进行链式调用。

####  Futurn.then

举例：使用 Future.delayed 创建了一个延时任务（实际场景会是一个真正的耗时任务，比如一次网络请求），即 2 秒后返回结果字符串 "hi world!"，然后在 then 中接收异步结果并打印结果，代码如下：

```dart
Future.delayed(Duration(seconds: 2),(){
   return "hi world!";
}).then((data){
   print(data);
});
```

#### Futurn.catchError

如果异步任务发生错误，可以在 catchError 中捕获错误，将上面示例改为：

```dart
Future.delayed(Duration(seconds: 2),(){
   //return "hi world!";
   throw AssertionError("Error");  
}).then((data){
   //执行成功会走到这里  
   print("success");
}).catchError((e){
   //执行失败会走到这里  
   print(e);
});
```

在示例中，在异步任务中抛出了一个异常，then 的回调函数将不会被执行，取而代之的是 catchError 回调函数将被调用；但是，并不是只有 catchError 回调才能捕获错误，then 方法还有一个可选参数onError，也可以用它来捕获异常：

```dart
Future.delayed(Duration(seconds: 2), () {
	//return "hi world!";
	throw AssertionError("Error");
}).then((data) {
	print("success");
}, onError: (e) {
	print(e);
});
```

#### Futurn.whenComplete

有些时候，会遇到无论异步任务执行成功或失败都需要做一些事的场景，比如在网络请求前弹出加载对话框，在请求结束后关闭对话框。这种场景，有两种方法，第一种是分别在 then 或 catch 中关闭一下对话框，第二种就是使用 Future 的 whenComplete 回调，将上面示例改一下：

```dart
Future.delayed(Duration(seconds: 2),(){
   //return "hi world!";
   throw AssertionError("Error");
}).then((data){
   //执行成功会走到这里 
   print(data);
}).catchError((e){
   //执行失败会走到这里   
   print(e);
}).whenComplete((){
   //无论成功或失败都会走到这里
});
```

#### Future.wait

有些时候，需要等待多个异步任务都执行结束后才进行一些操作，比如有一个界面，需要先分别从两个网络接口获取数据，获取成功后，需要将两个接口数据进行特定的处理后再显示到 UI 界面上，应该怎么做？答案是 Future.wait，它接受一个 Future 数组参数，只有数组中所有 Future 都执行成功后，才会触发 then 的成功回调，只要有一个 Future 执行失败，就会触发错误回调。下面，通过模拟Future.delayed 来模拟两个数据获取的异步任务，等两个异步任务都执行成功时，将两个异步任务的结果拼接打印出来，代码如下：

```dart
Future.wait([
  // 2秒后返回结果  
  Future.delayed(Duration(seconds: 2), () {
    return "hello";
  }),
  // 4秒后返回结果  
  Future.delayed(Duration(seconds: 4), () {
    return " world";
  })
]).then((results){
  print(results[0]+results[1]);
}).catchError((e){
  print(e);
});
```

执行上面代码，4 秒后会在控制台中看到“hello world”。

### Async/await

Dart中的 async/await 和 JavaScript 中的 async/await 功能和用法是一模一样的。

### 回调地狱（Callback Hello）

如果代码中有大量异步逻辑，并且出现大量异步任务依赖其它异步任务的结果时，必然会出现 Future.then 回调中套回调情况。举个例子，比如现在有个需求场景是用户先登录，登录成功后会获得用户 ID，然后通过用户 ID，再去请求用户个人信息，获取到用户个人信息后，为了使用方便，需要将其缓存在本地文件系统，代码如下：

```dart
//先分别定义各个异步任务
Future<String> login(String userName, String pwd){
	...
    //用户登录
};
Future<String> getUserInfo(String id){
	...
    //获取用户信息 
};
Future saveUserInfo(String userInfo){
	...
	// 保存用户信息 
}; 
```

接下来，执行整个任务流：

```dart
login("alice","******").then((id){
 //登录成功后通过，id获取用户信息    
 getUserInfo(id).then((userInfo){
    //获取用户信息后保存 
    saveUserInfo(userInfo).then((){
       //保存用户信息，接下来执行其它操作
        ...
    });
  });
})
```

可以感受一下，如果业务逻辑中有大量异步依赖的情况，将会出现上面这种在回调里面套回调的情况，过多的嵌套会导致的代码可读性下降以及出错率提高，并且非常难维护，这个问题被形象的称为**回调地狱（Callback Hell）**。

#### 使用 Futurn 消除 Callback Hell

```dart
login("alice","******").then((id){
  	return getUserInfo(id);
}).then((userInfo){
    return saveUserInfo(userInfo);
}).then((e){
   //执行接下来的操作 
}).catchError((e){
  //错误处理  
  print(e);
});
```

正如上文所述， “Future 的所有 API 的返回值仍然是一个 Future 对象，所以可以很方便的进行链式调用” ，如果在 then 中返回的是一个 Future 的话，该 future 会执行，执行结束后会触发后面的 then 回调，这样依次向下，就避免了层层嵌套。

#### 使用 async/await 消除 callback hell

通过 Future 回调中再返回 Future 的方式虽然能避免层层嵌套，但是还是有一层回调，有没有一种方式能够可以像写同步代码那样来执行异步任务而不使用回调的方式？答案是肯定的，这就要使用 async/await 了，代码如下：

```dart
task() async {
   try{
    String id = await login("alice","******");
    String userInfo = await getUserInfo(id);
    await saveUserInfo(userInfo);
    //执行接下来的操作   
   } catch(e){
    //错误处理   
    print(e);   
   }  
}
```

* async 用来表示函数是异步的，定义的函数会返回一个 Future 对象，可以使用 then 方法添加回调函数。
* await 后面是一个 Future，表示等待该异步任务完成，异步完成后才会往下走；await 必须出现在 async 函数内部。

## 5. Stream

Stream 也是用于接收异步事件数据，和 Future 不同的是，它可以接收多个异步操作的结果（成功或失败）。 也就是说，在执行异步任务时，可以通过多次触发成功或失败事件来传递结果数据或错误异常。 Stream 常用于会多次读取数据的异步任务场景，如网络内容下载、文件读写等。举个例子：

```dart
Stream.fromFutures([
  // 1秒后返回结果
  Future.delayed(Duration(seconds: 1), () {
    return "hello 1";
  }),
  // 抛出一个异常
  Future.delayed(Duration(seconds: 2),(){
    throw AssertionError("Error");
  }),
  // 3秒后返回结果
  Future.delayed(Duration(seconds: 3), () {
    return "hello 3";
  })
]).listen((data){
   print(data);
}, onError: (e){
   print(e.message);
},onDone: (){

});
```

上面的代码依次会输出：

```
I/flutter (17666): hello 1
I/flutter (17666): Error
I/flutter (17666): hello 3
```

## 6. Dart 和 Java 及 JavaScript 对比

### 6.1. Dart vs Java

客观的来讲，Dart 在语法层面确实比 Java 更有表现力；在 VM 层面，Dart VM 在内存回收和吞吐量都进行了反复的优化，但具体的性能对比，笔者没有找到相关测试数据，但在笔者看来，只要 Dart 语言能流行，VM 的性能就不用担心，毕竟 Google 在 Go（没用 VM 但有 GC）、JavaScript（v8）、Dalvik（ Android 上的 Java VM ）上已经有了很多技术积淀。值得注意的是 Dart 在 Flutter 中已经可以将 GC 做到 10ms 以内，所以 Dart 和 Java 相比，决胜因素并不会是在性能方面。而在语法层面，Dart 要比 Java 更有表现力，最重要的是 Dart 对函数式编程支持要远强于 Java（目前只停留在 Lambda 表达式），而 Dart 目前真正的不足是生态，但相信，随着 Flutter 的逐渐火热，会回过头来反推 Dart 生态加速发展，对于 Dart 来说，现在需要的是时间。

### 6.2. Dart vs JavaScript

JavaScript 的弱类型一直被抓短，所以 TypeScript 甚至是 Facebook 的 Flow 才有市场。就使用过的脚本语言中，JavaScript 无疑是**动态化**支持最好的脚本语言，比如在 JavaScript 中，可以给任何对象在任何时候动态扩展属性，对于精通 JavaScript 的高手来说，这无疑是一把利剑。但是，任何事物都有两面性，JavaScript 强大的动态化特性也是把双刃剑，可经常听到另一个声音，认为 JavaScript 的这种动态性糟糕透了，太过灵活反而导致代码很难预期，无法限制不被期望的修改。毕竟有些人总是对自己或别人写的代码不放心，他们希望能够让代码变得可控，并期望有一套静态类型检查系统来帮助自己减少错误。正因如此，在 Flutter中，Dart 几乎放弃了脚本语言动态化的特性，如不支持反射、也不支持动态创建函数等。并且 Dart 从 2.0 开始强制开启了类型检查（Strong Mode），原先的检查模式（checked mode）和可选类型（optional type）将淡出，所以在类型安全这个层面来说，Dart 和 TypeScript、CoffeeScript 是差不多的，所以单从动态性来看，Dart 并不具备什么明显优势，但综合起来看，Dart 既能进行服务端脚本、APP 开发、Web 开发，这就有优势了！