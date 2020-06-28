# java 读写文件

　　java 把这些不同来源和目标的数据都同一抽象位数据流。

　　Java 语言的输入输出功能是十分抢答而灵活的。

　　在 Java 类库中，IO 部分的内容是很庞大的，因为它涉及的领域很广泛：标准输入输出、文件的操作、网络上的数据流、字符串流、对象流、zip 文件流等等。

　　这里介绍几种读写文件的方式。

## 1. 字节流 - InputStream、OutputStream

## 2. 缓存字符流 - BufferedInputStream、BufferedOutputStream

　　使用方式和字符流差不多，但是效率更高，推荐使用。

## 3. 字节流 - InputStreamReader、OutputStreamWriter

　　种种方式不建议使用，不能直接字节长度读写。使用范围用做字符转换

## 4. 缓存流 - BufferedReader、BufferedWriter

　　提供 readLine 方法读取一行文本。

## 5. Reader、PrintWriter

　　PrintWriter 这个很好用，在写数据的同时可以格式化。



　　基本的几种用法就这么多，当然每一个读写的使用都是可以分开的。为了更好的来使用 IO。流里面的读写建议使用 BufferedInputStream、BufferedOutputStream。




## 参考文章

1. [java.io 的几种读写文件的方式](https://www.cnblogs.com/MyTiMo/p/10998525.html)

2. [java 读取文件完整版](https://www.cnblogs.com/JonaLin/p/11057398.html)