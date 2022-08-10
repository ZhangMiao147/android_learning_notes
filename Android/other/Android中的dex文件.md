# Android 中的 dex 文件

## 什么是 dex

简单说就是优化后的 android 版 .exe。每个 apk 安装包里都有。
相对于 PC 上的 java 虚拟机能运行 .class；android 上的 Davlik 虚拟机能运行 .dex。
为何要研究 dex 格式？因为 dex 里面包含了所有 app 代码，利用反编译工具可以获取 java 源码。理解并修改 dex 文件，就能更好的 apk 破解和防破解。

## dex 好处

dex 文件有个天大的好处：它可以直接用 DexClassLoader 类加载，这叫动态加载。于是只要在 dex 上加壳，在程序运行时脱壳，就可以规避静态反编译的风险。

## 一般的反编译流程是什么样子的？

1. 下载 apk 文件，然后解压 ，得到 class.dex 文件。
2.  用 dex2jar 把 class.dex 还原成 classes-dex2jar.jar  文件。
3. 用 jd-gui.exe 把 classes-dex2jar.jar 文件打开，就可以看到源码了。

## Android 为什么会出现 65535 限制问题

在 Android 系统中，一个 App 的所有代码都在一个 Dex 文件里面。Dex 是一个类似 Jar 的包，存储了很多 Java 编译字节码的归档文件。因为 Android 系统使用 Dalvik 虚拟机，所以需要把使用 Java Compiler 编译之后的 class 文件转换成 Dalvik 能够执行的 class 文件。这里需要强调的是，Dex 和 Jar 一样是一个归档文件，里面仍然是 Java 代码对应的字节码文件。

当 Android 系统启动一个应用的时候，有一步是对 Dex 进行优化，这个过程有一个专门的工具来处理，叫 DexOpt。DexOpt 的执行过程是在第一次加载 Dex 文件的时候执行的。这个过程会生成一个 ODEX 文件，即 Optimised Dex。执行 ODex 的效率会比直接执行 Dex 文件的效率要高很多。

但是在早期的 Android 系统中， DexOpt 有一个问题，也就是这篇文章想要说明并解决的问题。DexOpt 会把每一个类的方法 id 检索起来，存在一个链表结构里面。但是这个链表的长度是用一个 short 类型来保存的，导致了方法 id 的数目不能够超过 65536 个。当一个项目足够大的时候，显然这个方法数的上限是不够的。尽管在新版本的 Android 系统中，DexOpt 修复了这个问题，但是仍然需要对老系统做兼容。 

## dex 文件分析

逻辑上，可以把 dex 文件分成 3 个区，头文件、索引区和数据区。索引区的 ids 后缀为 identifiers 的缩写。

![](http://qn.javajgs.com/20220627/4926febd-c03e-4256-aac9-52a2a24025012022062718878c85-857a-459c-96f4-c5caae1dc2e81.jpg)

* header

  dex 文件里的 header。除了描述 .dex 文件的文件信息外，还有文件里其他各个区域的索引。

  ![](http://qn.javajgs.com/20220627/9c80dc88-60a5-4934-b578-0db136732b19202206274f936472-e62c-4cc9-a8f8-c21fd2705a441.jpg)

  （1）magic value，这8个字节一般是常量，为了使.dex文件能够被识别出来，它必须出现在.dex文件的最开头的位置。

  （2）checksum 和 signature：文件校验码，使用 alder32 算法校验文件出去 maigc、checksum 外余下的所有文件区域，用于校验文件错误。Signature，使用 SHA-1 算法 hash 除去 magic、checksum 和signature外余下的所有文件区域。

  （3）file_size：Dex文件的大小。

## odex 文件

odex 是 OptimizedDEX 的缩写，表示经过优化的 dex 文件。存放在 /data/dalvik-cache 目录下。由于 Android 程序的 apk 文件为 zip 压缩包格式，Dalvik 虚拟机每次加载它们时需要从 apk 中读取 classes.dex 文件，这样会耗费很多 cpu 时间，而采用 odex 方式优化的 dex 文件，已经包含了加载 dex 必须的依赖库文件列表，Dalvik 虚拟机只需检测并加载所需的依赖库即可执行相应的 dex 文件，这大大缩短了读取 dex 文件所需的时间。

不过，这个优化过程会根据不同设备上 Dalvik 虚拟机的版本、Framework 库的不同等因素而不同。在一台设备上被优化过的 ODEX 文件，拷贝到另一台设备上不一定能够运行。

### odex 文件结构

Odex 文件的结构可以理解为 dex 文件的一个超集。它的结构如下图所示，odex 文件在 dex 文件头部添加了一些数据，然后在 dex 文件尾部添加了 dex 文件的依赖库以及一些辅助数据。

![](http://qn.javajgs.com/20220627/7d5ebc78-8db4-43b7-b653-43ad887256802022062785f5653c-391e-4b9a-a88c-086d71e628121.jpg)

Dalvik 虚拟机将 dex 文件映射到内存中后是 Dalvik 格式，在 Android 系统源码的 dalvik/libdex/DexFile.h 文件中它的定义如下。

DexFile 结构中存入的多为其他结构的指针。DexFile 最前面的 DexOptHeader 就是 odex 的头，DexLink 以下的部分被成为 auxillary section，即辅助数据段，它记录了dex文件被优化后添加的一些信息。然而，DexFile 结构描述的是加载进内存的数据结构，还有一些数据是不会加载进内存的，经过分析，odex 文件结构定义整理如下：

![](http://qn.javajgs.com/20220627/56a54dc4-162c-49ed-99e6-609e85f96ec420220627120e480a-a441-4a1e-a79e-c33dfa2e31971.jpg)

```java
Struct ODEXFile{
           DexOptHeader  header;    /*odex文件头*/
           DEXFile  dexfile;  /*dex文件*/
           Dependences  deps; /*依赖库列表*/
           ChunkDexClassLookup lookup; /*类查询结构*/
           ChunkRegisterMapPool  mappool; /*映射池*/
           ChunkEnd  end;    /*结束标志*/
};
```

### odex 文件结构分析

ODEXFile 的文件头 DexOptHeader 在 DexFile.h 文件中定义如下：

```java
struct DexOptHeader{
     u1  magic[8];    /*odex版本标识 */
     u4  dexOffset;   /* dex文件头偏移*/
     u4  dexLength;   /* dex文件总长度*/
     u4  depsOffset;  /*odex依赖库列表偏移*/
     u4  depsLength;  /*依赖库列表总长度*/
     u4  optOffset;   /*辅助数据偏移*/
     u4  optLength;   /*辅助数据总长度*/
     u4  flags ;      /*标志*/
     u4  checksum;    /*依赖库与辅助数据的校验和*/
};
```

## dex 文件的验证与优化

### dex 文件加载流程

Android 提供了一个专门验证与优化 dex 文件的工具 dexopt。其源码位于 Android 系统源码的 dalvik/dexopt目录下，Dalvik 虚拟机在加载一个 dex 文件时，通过指定的验证与优化选项来调用 dexopt 进行相应的验证与优化操作。

dexopt 的主程序为 OptMain.cpp，其中处理 apk/jar/zip 文件中的 classes.dex 的函数为 extractAndProcessZip()，extractAndProcessZip() 首先通过 dexZipFindEntry() 函数检查目标文件中是否拥有 class.dex，如果没有就失败返回，成功的话就调用 dexZipGetEntryInfo() 函数来读取 classes.dex 的时间戳与 crc 校验值，如果这一步没有问题，接着调用 dexZipExtractEntryToFile() 函数释放 classes.dex 为缓存文件，然后开始解析传递过来的验证与优化选项，验证选项使用 “v=” 指出，优化选项使用 “o=” 指出。所有的预备工作都做完后，调用 dvmPrepForDexOpt() 函数启动一个虚拟机进程，在这个函数中，优化选项 dexOptMode 与验证选项 varifyMode 被传递到了全局 DvmGlobals 结构 gDvm 的 dexOptMode 与 classVerifyMode 字段中。这时候所有的初始化工作已经完成，dexopt 调用 dvmContinueOptimization() 函数开始真正的验证和优化工作。

dvmContinueOptimization() 函数的调用链比较长。首先从 OptMain.cpp 转移到 dalvik/vm/analysis/DexPrepare.cpp，因为这里有 dvmContinueOptimization() 函数的实现。函数首先对 dex 文件做简单的检查，确保传递进来的目标文件属于 dex 或 odex，接着调用 mmap() 函数将整个文件映射到内存中，然后根据 gDvm 的 dexOptMode 与 classVerifyMode 字段来设置 doVarify 与 doOpt 两个布尔值，接着调用 rewriteDex() 函数来重写 dex 文件，这里的重写内容包括字符调整、结构重新对齐、类验证信息以及辅助数据。rewriteDex() 函数调用 dexSwapAndVerify() 调整字节序，接着调用 dvmDexFileOpenPartial() 创建 DexFile 结构，dvmDexFileOpenPartial() 函数的实现在 Android 系统源码 dalvik/vm/DvmDex.cpp 文件中，该函数调用 dexFileParse() 函数解析 dex 文件，dexFileParse() 函数读取 dex 文件的头部，并根据需要调用验证 dexComputeChecksum() 函数或调用 dexComputeOptChecksum() 函数来验证 dex 或 odex 文件头的 checksum 与 signature 字段。

接着回到 DvmDex.cpp 文件继续看代码，当验证成功后， dvmDexFileOpenPartial() 函数调用 allocateAuxStructures() 函数设置 DexFile 结构辅助数据的相关字段，最后执行完后返回到 rewriteDex() 函数。rewriteDex() 接下来调用 loadAllClasses() 加载 dex 文件中所有的类，如果这一步失败了，程序等不到后面的优化与验证就退出了，如果没有错误发生，会调用 verifyAndOptimizeClasses() 函数进行真正的验证工作，这个函数会调用 verifyAndOptimizeClass() 函数来优化与验证具体的类，而 verifyAndOptimizeClass() 函数会细分这些工作，调用 dvmVerifyClass() 函数进行验证，再调用 dvmOptimizeClass() 函数进行优化。

dvmVerifyClass() 函数的实现代码位于 Android 系统源码的 dalvik/vm/analysis/DexVerify.cpp 文件中。这个函数调用 verifyMethod() 函数对类的所有直接方法与虚方法进行验证，verifyMethod() 函数具体的工作是先调用 verifyInstructions() 函数来验证方法中的指令及其数据的正确性，再调用 dvmVerifyCodeFlow() 函数来验证代码流的正确性。

dvmOptimizeClass() 函数的实现代码位于 Android 系统源码的 dalvik/vm/analysis/Optimize.cpp 文件中。这个函数调用 optimizeMethod() 函数对类的所有直接方法与虚方法进行优化，优化的主要工作是进行“指令替换”，替换原则的优先级为 “ volatile ” 替换 - 正确性替换 - 高性能替换。比如指令 iget-wide 会根据优先级替换为 “volatile” 形式的 iget-wide-volatile，而不是高性能的 iget-wide-quick。

 rewriteDex 函数返回后，会再次调用 dvmDexFileOpenPartial() 来验证 odex 文件，接着调用 dvmGenerateRegisterMaps() 函数来填充辅助数据区结构，填充结构完成后，接下来调用 updateChecksum() 函数重写 dex 文件的 checksum 值，再往下就是 writeDependencies() 与 writeOptData() 了。

### dex 文件优化加载流程图

![](http://qn.javajgs.com/20220627/c75537e7-1a67-4d5b-83af-0cc8d7fc93ee20220627965c2208-1e1b-4060-915c-93374000d2361.jpg)

![](http://qn.javajgs.com/20220627/44b9d1b8-4551-4cc5-97ca-2d797a02bb93202206276a69800e-3336-479b-b1d1-c51a42f1115e1.jpg)

![](http://qn.javajgs.com/20220627/9a19b97c-4e08-40f4-b506-221155c82f4820220627f1613452-eaf7-44fb-8aa1-20d3059b3bfa1.jpg)



## 参考文章

1. [Android中dex文件的加载与优化流程](https://javaforall.cn/153553.html)
2. [Android DEX 基础 ](https://www.cnblogs.com/zhaoyanjun/p/5736305.html)

