# api 与 implementation 的区别

## 1. 区别

　　Gradle Plugin 3.0 依赖方式增加了 implementation 和 api，用以取代 compile。

　　implementation 可以让 module 在编译时隐藏自己使用的依赖，但是在运行时这个依赖对所有模块时可见的，而 api 与 compile 一样，无法隐藏自己使用的依赖。

　　有工程 A、B、C。让 A 依赖 B 。

　　若 B implementation C，则 A 不能调用 C 的方法。

　　若 B api C，则 A 可以调用 C 的方法（同 compile）。

## 2. 目的

　　implemrntation 隐藏依赖的目的在于减少 build 的时间。

　　如果 B api C，那么当 C 发生变化时，编译需要重新编译 A、B、C。

　　如果 B implementation C，那么当 C 发生变化时，编制需要重新编译 B、C，不需要编译 A，这样就可以节省 build 的时间。

　　这就是说，如果 api 依赖，一个 module 发生变化，这条依赖链上所有的 mudole 都需要重新编译，而 implementation，只有直接依赖这个 module 需要重新编译。

## 3. 远程依赖

　　如果 A 远程 implementation B（implementation 'com.example:B:1.0.0'），B 远程 implementation C，A 是可以调用 C 的方法的。

　　如果 A 本地 implementation B，而 B 远程 implementation C，这时候 A 是不能调用 C 的方法的，依然起到了依赖隔离作用。

　　所以在全部远程依赖模式下，无论是 api 还是 implementation 都起不到依赖隔离的作用。

## 4. 总结

　　在多层次模块化（大于等于三层 module）开发时，如果都是本地依赖，implementation 相比 api，主要优势在于减少 build time。如果只有两层 module，api 与 implementation 在 build time 上并无太大的差别。


## 5. 参考文章
[implementation 和 api 的区别](https://www.jianshu.com/p/9345a02f20d4)

[api 与 implementation 的区别](https://www.jianshu.com/p/8962d6ba936e)

