# Classloader 类加载机制

不管是android 还是java项目我们知道我们的java文件都会通过 javac命令编译成二进制class文件，然后虚拟机再通过classloader类对class文件进行加载生成Class对象。其中java 和 android的classloader实现上还是有区别的，java主要加载的是 .class 而 android加载的是.dex 文件。本篇主要分析android classloader源码，对java 只做大致介绍。

### **1. Classloader 作用 ？**

   一个程序在运行时候，我们的逻辑代码java文件都被编译成class文件，在我们程序的入口处调用其他class文件时候，由于java程序是**动态加载的，**如果其他class文件不存在，程序就会崩溃，这时候就需要把class文件通过类加载器加载到内存中去， 然后其他class文件才能使用当前被加载到内存的class文件，这就是classloader的作用。  

### **2. Java 中的Classloader**

java中有Bootstrap Classloader , Extensions ClassLoader和 App ClassLoader 。其中Bootstrap Classloader 是加载C/C++的类加载对象，而 Extensions ClassLoader和 App ClassLoader 是继承自 Classloader抽象类的，也是我们java代码使用的加载机制。（不做过多介绍）

### **3. Android 中的Classloader**

首先在android中Classloader是个静态类，子类主要有 BaseDexClassloader，BootClassloader，PathClassloader和DexClassloader。继承关系如下图：

![img](https://img-blog.csdnimg.cn/20191212112622372.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhbmdSYWluMQ==,size_16,color_FFFFFF,t_70)

1. Classloader 一个抽象类，让子类重写findClass() 方法，并且实现loadClass()方法双亲委托机制。
2. BootClassloader 主要用于加载系统源码，例如：framework下的代码。
3. BaseDexClassloader 加载dex文件的主要功能实现，内部包括 DexPathLIst类Element数组对象。
4. DexClassloader 主要用于加载jar或者外部class文件。
5. PathClassloader 主要用于加载已经安装到手机上的apk内部的dex。

整个Classloader架构核心的代码都在BaseDexClassloader中的 DexPathlist 类，下面就大致分析一下代码流程：

### **4. Anddrod Classloader 源码流程** 

1. Classloader抽象类

   在Classloader内部首先会对bootClassloader初始化

   ```
    /**
   
   
   
        * Encapsulates the set of parallel capable loader types.
   
   
   
        */
   
   
   
       private static ClassLoader createSystemClassLoader() {
   
   
   
           //系统framework下文件路径
   
   
   
           String classPath = System.getProperty("java.class.path", ".");
   
   
   
           // native c/c++ 的library包
   
   
   
           String librarySearchPath = System.getProperty("java.library.path", "");
   
   
   
    
   
   
   
           // TODO Make this a java.net.URLClassLoader once we have those?
   
   
   
           return new PathClassLoader(classPath, librarySearchPath, BootClassLoader.getInstance());
   
   
   
       }
   ```

   除了系统类加载器的创建，还有一个非常重要的方法代码

   ```
     protected Class<?> loadClass(String name, boolean resolve)
   
   
   
           throws ClassNotFoundException
   
   
   
       {
   
   
   
               // First, check if the class has already been loaded
   
   
   
               // 首先到已经加载过的内存中去查找，找到了就返回，否则委托父节点去查询
   
   
   
               Class<?> c = findLoadedClass(name);
   
   
   
               if (c == null) {
   
   
   
                   try {
   
   
   
                       if (parent != null) {
   
   
   
                           c = parent.loadClass(name, false);
   
   
   
                       } else {
   
   
   
                           c = findBootstrapClassOrNull(name);
   
   
   
                       }
   
   
   
                   } catch (ClassNotFoundException e) {
   
   
   
                       // ClassNotFoundException thrown if class not found
   
   
   
                       // from the non-null parent class loader
   
   
   
                   }
   
   
   
    
   
   
   
                   if (c == null) {
   
   
   
                       // If still not found, then invoke findClass in order
   
   
   
                       // to find the class.
   
   
   
                       c = findClass(name);
   
   
   
                   }
   
   
   
               }
   
   
   
               return c;
   
   
   
       }
   ```

   这段代码就是Classloader双亲委托机制的代码实现，下面会详细介绍 什么是双亲委托机制。

2. DexClassloader

   ```
   public class DexClassLoader extends BaseDexClassLoader {
   
   
   
       public DexClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
   
   
   
           super((String)null, (File)null, (String)null, (ClassLoader)null);
   
   
   
           throw new RuntimeException("Stub!");
   
   
   
       }
   
   
   
   }
   ```

   dexPath : dex文件的路径，多个用 “：” 分开
   optimizedDirectory ： 解压的dex文件存储路径，这个路径必须是一个内部存储路径，一般情况下使用当前应用程序的私有路径：`/data/data/<Package Name>/...`
   librarySearchPath ：native 的library路径，可以为null
   ClassLoader parent ： 传入的父classloader，用于委托查询

3. PathClassloader

   ```
   public class PathClassLoader extends BaseDexClassLoader {
   
   
   
       public PathClassLoader(String dexPath, ClassLoader parent) {
   
   
   
           super((String)null, (File)null, (String)null, (ClassLoader)null);
   
   
   
           throw new RuntimeException("Stub!");
   
   
   
       }
   
   
   
    
   
   
   
       public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
   
   
   
           super((String)null, (File)null, (String)null, (ClassLoader)null);
   
   
   
           throw new RuntimeException("Stub!");
   
   
   
       }
   
   
   
   }
   ```

   可以看到 PathClassloader 比较容易理解，与DexClassloader参数意思相同。

4. BaseDexClassloader

   最重要的base加载器，可以看到PathClassloader和DexClassloader都是继承自他的，所以几乎大部分逻辑都被放到了BaseClassloader中。

   ```
    private final DexPathList pathList;
   
   
   
    
   
   
   
    public BaseDexClassLoader(String dexPath, File optimizedDirectory,
   
   
   
               String librarySearchPath, ClassLoader parent, boolean isTrusted) {
   
   
   
           super(parent);
   
   
   
           this.pathList = new DexPathList(this, dexPath, librarySearchPath, null, isTrusted);
   
   
   
    
   
   
   
           if (reporter != null) {
   
   
   
               reportClassLoaderChain();
   
   
   
           }
   
   
   
       }
   ```

   经过分析BaseClassloader中的方法可以看到，BaseClassloader的功能几乎都是调用了 DexPathList 类，而且是在构造方法中初始化的。接下里主要分析DexPathList的结果。

5. DexPathList 结构
   在DexPathList中保存了一个Element的数据结构，在构造方法中初始化Element数组

   ```
   private Element[] dexElements;
   
   
   
     
   
   
   
    DexPathList(ClassLoader definingContext, String dexPath,
   
   
   
               String librarySearchPath, File optimizedDirectory, boolean isTrusted) {
   
   
   
           this.definingContext = definingContext;
   
   
   
    
   
   
   
                 ..........
   
   
   
    
   
   
   
           // save dexPath for BaseDexClassLoader
   
   
   
           this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory,
   
   
   
                                              suppressedExceptions, definingContext, isTrusted);
   
   
   
    
   
   
   
           // Native libraries may exist in both the system and
   
   
   
           // application library paths, and we use this search order:
   
   
   
           //
   
   
   
           //   1. This class loader's library path for application libraries (librarySearchPath):
   
   
   
           //   1.1. Native library directories
   
   
   
           //   1.2. Path to libraries in apk-files
   
   
   
           //   2. The VM's library path from the system property for system libraries
   
   
   
           //      also known as java.library.path
   
   
   
           //
   
   
   
           // This order was reversed prior to Gingerbread; see http://b/2933456.
   
   
   
           .................
   
   
   
       }
   ```

   在这里根据传来的file路径，分割得到每个dex文件或者apk文件，然后放入到dexElements数组中去。看makeDexElement()方法

   ```
   //待封装的 dex / apk文件数组
   
   
   
   Element[] elements = new Element[files.size()];
   
   
   
         int elementsPos = 0;
   
   
   
         /*
   
   
   
          * Open all files and load the (direct or contained) dex files up front.
   
   
   
          */
   
   
   
         for (File file : files) {
   
   
   
             if (file.isDirectory()) {
   
   
   
                 // 是文件夹直接 加入到 数组中。
   
   
   
                 elements[elementsPos++] = new Element(file);
   
   
   
             } else if (file.isFile()) {
   
   
   
                 String name = file.getName();
   
   
   
    
   
   
   
                 DexFile dex = null;
   
   
   
                 // 是文件先判断 后缀名，然后把file 转换到 DexFile 数据结构
   
   
   
                 if (name.endsWith(DEX_SUFFIX)) {
   
   
   
                     // Raw dex file (not inside a zip/jar).
   
   
   
                     try {
   
   
   
                         // 转换到 DexFile 数据结构
   
   
   
                         dex = loadDexFile(file, optimizedDirectory, loader, elements);
   
   
   
                         if (dex != null) {
   
   
   
                             elements[elementsPos++] = new Element(dex, null);
   
   
   
                         }
   
   
   
                     } catch (IOException suppressed) {
   
   
   
                         
   
   
   
                     }
   
   
   
                 } else {
   
   
   
                    
   
   
   
                     dex = loadDexFile(file, optimizedDirectory, loader, elements);
   
   
   
                     // 为 null 说明不是 dex 文件，仅仅是个文件
   
   
   
                     if (dex == null) {
   
   
   
                         elements[elementsPos++] = new Element(file);
   
   
   
                     } else {
   
   
   
                         elements[elementsPos++] = new Element(dex, file);
   
   
   
                     }
   
   
   
                 }
   
   
   
                
   
   
   
         }
   
   
   
        
   
   
   
         return elements;
   ```

   在这里把所有dex/apk等文件放入 element 数组中，等待 findClass（）方法调用

### **5. 双亲委托机制**

通过对 Classloader 中的loadClass()方法的代码分析，知道大致流程图

![img](https://img-blog.csdnimg.cn/20191212143900869.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhbmdSYWluMQ==,size_16,color_FFFFFF,t_70)

当我们有一个class文件需要加载时，首先会到我们自定义的MyClassloader中去查询，查不到就会委托内部持有的parent classloader（PathClassloader）中查询，如果查不到，在继续往上委托到boot classloader中查找。直到整个委托过程结束后，再从boot classloader 向 指定的 path 路径下搜索 class 文件，一次类推，直到Myclassloader 到指定路径下去查 class 文件，查到就 load 到内存（一般用 io 流加载），否则返回 null。

### **6. PathClassloader 的初始化**

学过android系统启动流程，容易知道。当Zygote进程启动之后，就会创建SystemServer进程。在SystemServer进程被fork()之后，在通过反射调用 SystemServer 的 main() 方法 之前，传入了一个classloader，这个classloader就是 pathClassloader对象。

```
 ClassLoader cl = null;



            if (systemServerClasspath != null) {



                cl = createPathClassLoader(systemServerClasspath, parsedArgs.targetSdkVersion);



                Thread.currentThread().setContextClassLoader(cl);



            }



            /*



             * Pass the remaining arguments to SystemServer.



             */



            return ZygoteInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, cl);
```

其中createPathClassloader()方法 返回了cl 对象。

```
  public static ClassLoader createClassLoader(String dexPath,



            String librarySearchPath, ClassLoader parent, String classloaderName) {



        if (isPathClassLoaderName(classloaderName)) {



            return new PathClassLoader(dexPath, librarySearchPath, parent);



        } else if (isDelegateLastClassLoaderName(classloaderName)) {



            return new DelegateLastClassLoader(dexPath, librarySearchPath, parent);



        }



 



        throw new AssertionError("Invalid classLoaderName: " + classloaderName);



    }
```

加载流程

![img](https://img-blog.csdnimg.cn/20191212154116511.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhbmdSYWluMQ==,size_16,color_FFFFFF,t_70)

```
问题1.自己定义一个和系统包名一样的String类的时候，根本不会引用到我们自定义的String为什么？
因为string类在zygote进程启动的时候使用bootclassloader先把jdk等class先加载到内存了，等到我们app再用pathclassloader去加载string时候会委托到bootclassloader查询，然后就查询到了string对象。

问题2.为什么Tinker不使用DexClassLoader加载再反射获取它里面的Element？而是要反射makePathElement这样的方法把dex生成Element?
因为：就是A和B类，A调用了B，如果B被内联。那A和B要是同一个classloader加载的。如果A在补丁包，你用dexclassloader加载A，那不就不在同一个classloader了。
```

# ClassLoader

　　一个java程序来是由多个`class`类组成的，我们在运行程序的过程中需要通过`ClassLoader`将`class`类载入到`JVM`中才可以正常运行。
 而Android程序需要正常运行，也同样需要有`ClassLoader`机制将class类加载到Android 的 `Dalvik`（5.0之前版本）/`ART`（5.0增加的）中，只不过它和`java`中的`ClassLoader`不一样在于Android的`apk`打包，是将`class`文件打包成一个或者多个 `dex`文件（由于`Android 65k`问题，使用 `MultiDex` 就会生成多个 `dex` 文件），再由`BaseDexClassLoader`来进行处理。
 在安装`apk`的过程中，会有一个验证优化`dex`的机制，叫做`DexOpt`，这个过程会生成一个`odex`文件（ `odex 文件也属于dex文件`），即`Optimised Dex`。执行`odex`的效率会比直接执行`dex`文件的效率要高很多。运行Apk的时候，直接加载`odex`文件，从而避免重复验证和优化，加快了Apk的响应时间。
 `注意：Dalvik/ART 无法像 JVM 那样直接加载 class 文件和 jar 文件中的 class，需要通过工具来优化转换成 Dalvik byte code 才行，只能通过 dex 或者包含 dex 的jar、apk 文件来加载`

###### dex生成方法

你可以直接在编译工程后，在`app/build/intermediates/classes`中拿到你需要的`class`，然后再通过`dx`命令生成`dex`文件



```kotlin
dx --dex --output=/Users/test/test.dex multi/shengyuan/com/mytestdemo/test.class
```

### 双亲委派机制

类加载器双亲委派模型的工作过程是：如果一个类加载器收到一个类加载的请求，它首先将这个请求委派给父类加载器去完成，每一个层次类加载器都是如此，则所有的类加载请求都会传送到顶层的启动类加载器，只有父加载器无法完成这个加载请求(即它的搜索范围中没有找到所要的类)，子类才尝试加载。



![img](https:////upload-images.jianshu.io/upload_images/9268847-c6ad7bb51e4ff770.png?imageMogr2/auto-orient/strip|imageView2/2/w/700/format/webp)

图一.png

#### 双亲委派模式优势

采用双亲委派模式的是好处是Java类随着它的类加载器一起具备了一种带有优先级的层次关系，通过这种层级关可以避免类的重复加载，当父亲已经加载了该类时，就没有必要子ClassLoader再加载一次。其次是考虑到安全因素，java核心api中定义类型不会被随意替换，假设通过网络传递一个名为java.lang.Integer的类，通过双亲委托模式传递到启动类加载器，而启动类加载器在核心Java API发现这个名字的类，发现该类已被加载，并不会重新加载网络传递的过来的java.lang.Integer，而直接返回已加载过的Integer.class，这样便可以防止核心API库被随意篡改。

##### Android中的ClassLoader根据用途可分为一下几种：

- BootClassLoader：主要用于加载系统的类，包括`java`和`android`系统的类库，和`JVM`中不同，BootClassLoader是`ClassLoader`内部类，是由`Java`实现的，它也是所有系统`ClassLoader`的父ClassLoader
- PathClassLoader：用于加载Android系统类和开发编写应用的类，只能加载已经安装应用的 `dex` 或 `apk` 文件，也是`getSystemClassLoader`的返回对象
- DexClassLoader：可以用于加载任意路径的`zip`、`dex`、`jar`或者`apk`文件，也是进行安卓动态加载的基础

### DexClassLoader类



```dart
public class DexClassLoader extends BaseDexClassLoader {
    public DexClassLoader(String dexPath, String optimizedDirectory,
            String librarySearchPath, ClassLoader parent) {
        super(dexPath, new File(optimizedDirectory), librarySearchPath, parent);
    }
}
```

### PathClassLoader类



```java
public class PathClassLoader extends BaseDexClassLoader {

    public PathClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, null, null, parent);
    }

    public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, null, librarySearchPath, parent);
    }
}
```

### BaseDexClassLoader#BaseDexClassLoader方法

`PathClassLoader`、`DexClassLoader`均继承`BaseDexClassLoader`，所以其`super`方法均调用到了`BaseDexClassLoader`构造方法

##### 参数详解：

- dexPath
   待解析文件所在的全路径，`classloader`将在该路径中指定的`dex`文件寻找指定目标类
- optimzedDirectory
   优化路径，指的是虚拟机对于apk中的dex文件进行优化后生成文件存放的路径，如dalvik虚拟机生成的`ODEX`文件路径和`ART`虚拟机生成的`OAT`文件路径。
   这个路径必须是当前app的内部存储路径，Google认为如果放在公有的路径下，存在被恶意注入的危险
   `注意：PathClassLoader没有将optimizedDirectory置为Null,也就是没设置优化后的存放路径。其实optimizedDirectory为null时的默认路径就是/data/dalvik-cache 目录。 PathClassLoader是用来加载Android系统类和应用的类，并且不建议开发者使用。`
- libraryPath
   指定`native`（即`so`加载路径）层代码存放路径
- parent
   当前ClassLoader的`parent`，和`java`中`classloader`的`parent`含义一样



```kotlin
public BaseDexClassLoader(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(parent);
        this.originalPath = dexPath;
        this.pathList = new DexPathList(this, dexPath, libraryPath, optimizedDirectory);
    }
```

### ClassLoader#loadClass方法

通过该方法你就能发现双亲委派机制的妙处了



```java
protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
            // 1 通过调用c层findLoadedClass检查该类是否被加载过，若加载过则返回class对象（缓存机制）
            Class c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        //2 各种类型的类加载器在构造时都会传入一个parent类加载器
                        //2 若parent类不为空，则调用parent类的loadClass方法
                        c = parent.loadClass(name, false);
                    } else {
                        //3 查阅了PathClassLoader、DexClassLoader并没有重写该方法，默认是返回null
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                }
                if (c == null) {
                    //4  如果父ClassLoader不能加载该类才由自己去加载,这个方法从本ClassLoader的搜索路径中查找该类
                    long t1 = System.nanoTime();
                    c = findClass(name);
                }
            }
            return c;
    }
```

### BaseDexClassLoader#findClass方法

`DexClassLoader`、`PathClassLoader`通过继承`BaseDexClassLoader`从而使用其父类`findClass`方法，在`ClassLoader#loadClass`方法中第`3`步进入



```dart
@Override
protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
        Class c = pathList.findClass(name, suppressedExceptions);
        if (c == null) {
            ClassNotFoundException cnfe = new ClassNotFoundException("Didn't find class \"" + name + "\" on path: " + pathList);
            for (Throwable t : suppressedExceptions) {
                cnfe.addSuppressed(t);
            }
            throw cnfe;
        }
        return c;
}
```

### DexPathList#findClass方法

从`ClassLoader#loadClass`方法中我们可以知道，当走到第`2`步即会走到如下方法，通过对已构建好的`dexElements`进行遍历，通过`dex.loadClassBinaryName`方法`load`对应的`class`类，所以这里是一个热修复的点，你可以将需要热修复的`dex`文件插入到`dexElements`数组前面，这样遍历的时候查到你最新插入的则返回，从而实现动态替换有问题类



```dart
public Class findClass(String name, List<Throwable> suppressed) {
        for (Element element : dexElements) {
            DexFile dex = element.dexFile;

            if (dex != null) {
                 //调用到c层defineClassNative方法进行查找
                Class clazz = dex.loadClassBinaryName(name, definingContext, suppressed);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        if (dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
        return null;
    }
```

### DexPathList#makeElements

`BaseDexClassLoader`的构造方法中对`DexPathList`进行实例化，在`DexPathList`构造方法中调用`makeElements`生成`dexElements`数组，首先会根据传入的`dexPath`，生成一个file类型的list容器，然后传入后进行遍历加载，通过调用`DexFile`中的`loadDexFile`对`dexFile`文件进行加载



```dart
private static Element[] makeElements(List<File> files, File optimizedDirectory,
                                          List<IOException> suppressedExceptions,
                                          boolean ignoreDexFiles,
                                          ClassLoader loader) {
        Element[] elements = new Element[files.size()];
        int elementsPos = 0;
        //遍历所有文件，并提取 dex 文件。
        for (File file : files) {
            File zip = null;
            File dir = new File("");
            DexFile dex = null;
            String path = file.getPath();
            String name = file.getName();

            if (path.contains(zipSeparator)) {
                String split[] = path.split(zipSeparator, 2);
                zip = new File(split[0]);
                dir = new File(split[1]);
            } else if (file.isDirectory()) {
                //为文件夹时，直接存储
                elements[elementsPos++] = new Element(file, true, null, null);
            } else if (file.isFile()) {
                if (!ignoreDexFiles && name.endsWith(DEX_SUFFIX)) {
                 // loadDexFile 的作用是：根据 file 获取对应的 DexFile 对象。
                    try {
                        dex = loadDexFile(file, optimizedDirectory, loader, elements);
                    } catch (IOException suppressed) {
                        System.logE("Unable to load dex file: " + file, suppressed);
                        suppressedExceptions.add(suppressed);
                    }
                } else {
              // 非 dex 文件，那么 zip 表示包含 dex 文件的压缩文件，如 .apk，.jar 文件等
                    zip = file;
                    if (!ignoreDexFiles) {
                        try {
                            dex = loadDexFile(file, optimizedDirectory, loader, elements);
                        } catch (IOException suppressed) {
                            suppressedExceptions.add(suppressed);
                        }
                    }
                }
            } else {
                System.logW("ClassLoader referenced unknown path: " + file);
            }

            if ((zip != null) || (dex != null)) {
                elements[elementsPos++] = new Element(dir, false, zip, dex);
            }
        }
        if (elementsPos != elements.length) {
            elements = Arrays.copyOf(elements, elementsPos);
        }
        return elements;
    }
```

### 插件化

看到这里，你应该大概理解了`classloader`加载流程，其实`java`这层的`classloader`代码量并不多，主要集中在`c`层，但是我们在`java`层进行`hook`便可实现热修复。
 结合网上的资料及源码的阅读一共有三种方案

#### 方案1:向`dexElements`进行插入新的`dex`（目前最常见的方式）

从上面的`ClassLoader#loadClass`方法你就会知道，初始化的时候会进入`BaseDexClassLoader#findClass`方法中通过遍历`dexElements`进行查找`dex`文件，因为`dexElements`是一个数组，所以我们可以通过反射的形式，将需要热修复的`dex`文件插入到数组`首部`，这样遍历数组的时候就会优先读取你插入的`dex`，从而实现热修复。

![img](https:////upload-images.jianshu.io/upload_images/9268847-5684dae9b2844abf.jpeg?imageMogr2/auto-orient/strip|imageView2/2/w/772/format/webp)

图二.jpeg



###### DexClassLoader不是允许你加载外部dex吗？用DexClassLoader#loadClass不就行了

我们知道`DexClassLoader`是允许你加载外部`dex`文件的，所以网上有一些例子介绍通过`DexClassLoader#loadClass`可以加载到你的`dex`文件中的方法，那么有一些网友就会有疑问，我直接通过调用`DexClassLoader#loadClass`去获取我传入的外部`dex`文件中的`class`，不就行了，这样确实是可以的，但是它仅适用于新增的类，而不能去替换旧的类，因为通过上面的`dexElements`数组的生成以及`委派双亲机制`，你就会知道它的父类是先去把你应用类组装进来，当你调用`DexClassLoader`去`loadClass`时，是先委派父类去`loadClass`，如果查找不到才会到子类自行查找，也就是说应用中本来就已经存在`B.class`了，那么父类`loadClass`会直接返回，而你真正需要返回的其实是子类中的`B.class`，所以才说只适用于新增的类，你不通过一些手段修改源码层，是无法实现替换类的。

#### 方案2:在ActivityThread中替换LoadedApk的mClassLoader对象

小编在开发MPlugin的时候，使用了下面的方法，但发现当你插件apk中进行跳转的下一个页面的时候，若引了第三方的库，会抛出无法载入该第三方库控件异常。
 实现代码如下：



```dart
  public static void loadApkClassLoader(Context context,DexClassLoader dLoader){
        try{
            // 配置动态加载环境
            Object currentActivityThread = RefInvoke.invokeStaticMethod(
                    "android.app.ActivityThread", "currentActivityThread",
                    new Class[] {}, new Object[] {});//获取主线程对象 http://blog.csdn.net/myarrow/article/details/14223493
            String packageName = context.getPackageName();//当前apk的包名
            ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mPackages");
            WeakReference wr = (WeakReference) mPackages.get(packageName);
            RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader",
                    wr.get(), dLoader);
        }catch(Exception e){
             e.printStackTrace();
        }
    }
```

#### 方案3:通过自定义ClassLoader实现class拦截替换

我们知道`PathClassLoader`是加载已安装的`apk`的`dex`，那我们可以
 在 `PathClassLoader` 和 `BootClassLoader` 之间插入一个 自定义的`MyClassLoader`，而我们通过`ClassLoader#loadClass`方法中的第`2`步知道，若`parent`不为空，会调用`parent.loadClass`方法，固我们可以在`MyClassLoader`中重写`loadClass`方法，在这个里面做一个判断去拦截替换掉我们需要修复的`class`。

###### 如何拿到我们需要修复的class呢？

我当时首先想到的是通过`DexClassLoader`直接去`loadClass`来获得需要热修复的`Class`，但是通过`ClassLoader#loadClass`方法分析，可以知道加载查找`class`的第`1`步是调用`findLoadedClass`，这个方法主要作用是检查该类是否被加载过，如果加载过则直接返回，所以如果你想通过`DexClassLoader`直接去`loadClass`来获得你需要热修复的`Class`，是不可能完成替换的（热修复），因为你调用`DexClassLoader.loadClass`已经属于首次加载了，那么意味着下次加载就直接在`findLoadedClass`方法中返回`class`了，是不会再往下走，从而`MyClassLoader#loadClass`方法也不可能会被回调，也就无法实现修复。
 通过`BaseDexClassLoader#findClass`方法你就会知道，这个方法在父`ClassLoader`不能加载该类的时候才由自己去加载，我们可以通过这个方法来获得我们的`class`，因为你调用这个方法的话，是不会被缓存起来。也就不存在`ClassLoader#loadClass`中的第`1`步就查找到就被返回。

![img](https:////upload-images.jianshu.io/upload_images/9268847-014409f725a300b5.jpeg?imageMogr2/auto-orient/strip|imageView2/2/w/502/format/webp)

图三.jpeg



### 方案3代码：



```dart
public class HookUtil {
    /**
     * 在 PathClassLoader 和 BootClassLoader 之间插入一个 自定义的MyClassLoader
     * @param classLoader
     * @param newParent
     */
    public static void injectParent(ClassLoader classLoader, ClassLoader newParent) {
        try {
            Field parentField = ClassLoader.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            parentField.set(classLoader, newParent);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射调用findClass方法获取dex中的class类
     * @param context
     * @param dexPath
     * @param className
     */
    public static void hookFindClass(Context context,String dexPath,String className){
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, context.getDir("dex",context.MODE_PRIVATE).getAbsolutePath(),null, context.getClassLoader());
        try {
            Class<?> herosClass = dexClassLoader.getClass().getSuperclass();
            Method m1 = herosClass.getDeclaredMethod("findClass", String.class);
            m1.setAccessible(true);
            Class newClass = (Class) m1.invoke(dexClassLoader, className);
            ClassLoader pathClassLoader = MyApplication.getContext().getClassLoader();
            MyClassLoader myClassLoader = new MyClassLoader(pathClassLoader.getParent());
            myClassLoader.registerClass(className, newClass);
            injectParent(pathClassLoader, myClassLoader);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
```



```java
public class MyClassLoader extends ClassLoader {

    public Map<String,Class> myclassMap;

    public MyClassLoader(ClassLoader parent) {
        super(parent);
        myclassMap = new HashMap<>();
    }

    /**
     * 注册类名以及对应的类
     * @param className
     * @param myclass
     */
    public void registerClass(String className,Class myclass){
        myclassMap.put(className,myclass);
    }

    /**
     * 移除对应的类
     * @param className
     */
    public void removeClass(String className){
        myclassMap.remove(className);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class myclass = myclassMap.get(name);
        //重写父类loadClass方法，实现拦截
        if(myclass!=null){
            return myclass;
        }else{
            return super.loadClass(name, resolve);
        }
    }
}
```

### 关于CLASS_ISPREVERIFIED标记

因为在 `Dalvik`虚拟机下，执行 `dexopt` 时，会对类进行扫描，如果类里面所有直接依赖的类都在同一个 dex 文件中，那么这个类就会被打上 `CLASS_ISPREVERIFIED` 标记，如果一个类有 `CLASS_ISPREVERIFIED`标记，那么在热修复时，它加载了其他 dex 文件中的类，会报经典的`Class ref in pre-verified class resolved to unexpected implementation`异常
 通过源码搜索并没有找到`CLASS_ISPREVERIFIED`标记这个关键词，通过在`android7.0、8.0`上进行热修复，也没有遇到这个异常，猜测这个问题只属于android5.0以前（关于解决方法网上有很多，本文就不讲述了），因为android5.0后新增了`art`。

### 最后

看到这里相信java层的ClassLoader机制你已经熟悉得差不多了，相对于插件化而言你已经前进了一步，但仍有一些问题需要去思考解决的，比如解决资源加载、混淆、加壳等问题，为了更好的完善热修复机制，你也可以去阅读下c层的逻辑，尽管热修复带来了很多便利，但个人也并不是太认同热修复的使用，毕竟是通过hook去修改源码层，因为android的碎片化问题，很难确保你的hook能正常使用且不引发别的问题。
 `注意：本文源码阅读及案例测试是基于android7.0、8.0编写的，案例经过实测是可行的`



## 参考文章
1. [源码学习《6》Classloader 类加载机制 （热修复 1）原理篇](https://blog.csdn.net/WangRain1/article/details/103504590) 
2. [剖析ClassLoader深入热修复原理](https://www.jianshu.com/p/95387cc07e3c)
