# SparseArray

　　SparseArray

## 一、SparseArray实现源码学习

SparseArray采用时间换取空间的方式来提高手机App的运行效率，这也是其与HashMap的区别；HashMap通过空间换取时间，查找迅速；HashMap中当table数组中内容达到总容量0.75时，则扩展为当前容量的两倍，关于HashMap可查看[HashMap实现原理学习](http://blog.csdn.net/xiaxl/article/details/72621758))

- SparseArray的key为int，value为Object。
- 在Android中，数据长度小于千时，用于替换HashMap
- 相比与HashMap，其采用 时间换空间 的方式，使用更少的内存来提高手机APP的运行效率(HashMap中当table数组中内容达到总容量0.75时，则扩展为当前容量的两倍，关于HashMap可查看[HashMap实现原理学习](http://blog.csdn.net/xiaxl/article/details/72621758))

![这里写图片描述](https://upload-images.jianshu.io/upload_images/1438561-4ebb4e14c1593323.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下边对其源码进行简单学习。

### 1、构造方法

```java
// 构造方法
public SparseArray() {
    this(10);
}

// 构造方法
public SparseArray(int initialCapacity) {
    if (initialCapacity == 0) {
        mKeys = EmptyArray.INT;
        mValues = EmptyArray.OBJECT;
    } else {
	    // key value各自为一个数组，默认长度为10
        mValues = ArrayUtils.newUnpaddedObjectArray(initialCapacity);
        mKeys = new int[mValues.length];
    }
    mSize = 0;
}
```

ps：
*SparseArray构造方法中，创建了两个数组mKeys、mValues分别存放int与Object，其默认长度为10*

### 2、 put(int key, E value)

```java
public void put(int key, E value) {
		// 二分查找,key在mKeys列表中对应的index
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        // 如果找到，则直接赋值
        if (i >= 0) {
            mValues[i] = value;
        } 
        // 找不到
        else {
	        // binarySearch方法中，找不到时，i取了其非，这里再次取非，则非非则正
            i = ~i;
            // 如果该位置的数据正好被删除,则赋值
            if (i < mSize && mValues[i] == DELETED) {
                mKeys[i] = key;
                mValues[i] = value;
                return;
            }
            // 如果有数据被删除了，则gc
            if (mGarbage && mSize >= mKeys.length) {
                gc();
                // Search again because indices may have changed.
                i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
            }
            // 插入数据，增长mKeys与mValues列表
            mKeys = GrowingArrayUtils.insert(mKeys, mSize, i, key);
            mValues = GrowingArrayUtils.insert(mValues, mSize, i, value);
            mSize++;
        }
}
```

ps：

- 因为key为int,不存在hash冲突
- mKeys为有序列表，通过二分查找，找到要插入的key对应的index (这里相对于查找hash表应该算是费时间吧，但节省了内存，所以是 时间换取了空间)
- 通过二分查找到的index，将Value插入到mValues数组的对应位置

### 3、get(int key)

```java
// 通过key查找对应的value
public E get(int key) {
        return get(key, null);
}
// 通过key查找对应的value
public E get(int key, E valueIfKeyNotFound) {
		// mKeys数组中采用二分查找，找到key对应的index
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
        // 没有找到，则返回空
        if (i < 0 || mValues[i] == DELETED) {
            return valueIfKeyNotFound;
        } else {
        // 找到则返回对应的value
            return (E) mValues[i];
        }
}
```

ps：
*每次调用get，则需经过一次mKeys数组的二分查找，因此mKeys数组越大则二分查找的时间就越长，因此SparseArray在大量数据，千以上时，会效率较低*

### 3、ContainerHelpers.binarySearch(mKeys, mSize, key)二分查找

```java
// array为有序数组
// size数组中内容长度
// value要查找的值
static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;
        // 循环查找
        while (lo <= hi) {
	        // 取中间位置元素
            final int mid = (lo + hi) >>> 1;
            final int midVal = array[mid];
            // 如果中间元素小于要查找元素，则midIndex赋值给 lo 
            if (midVal < value) {
                lo = mid + 1;
            }
            // 如果中间元素大于要查找元素，则midIndex赋值给 hi  
            else if (midVal > value) {
                hi = mid - 1;
            }
            // 找到则返回 
            else {
                return mid;  // value found
            }
        }
        // 找不到,则lo 取非
        return ~lo;  // value not present
}
```

## 二、android.support.v4.util.ArrayMap

ArrayMap和SparseArray有点类似；其中含有两个数组，一个是mHashes（key的hash值数组，为一个有序数组），另一个数组存储的是key和value，其中key和value是成对出现的，key存储在数组的偶数位上，value存储在数组的奇数位上。

![这里写图片描述](https://upload-images.jianshu.io/upload_images/1438561-b6396c7b8eebff0c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 1、构造方法

```java
public SimpleArrayMap() {
	 // key的hash值数组，为一个有序数组
	 mHashes = ContainerHelpers.EMPTY_INTS;
	 // key 与 value数组
	 mArray = ContainerHelpers.EMPTY_OBJECTS;
	 mSize = 0;
}
```

ps：
*构造方法中初始化了两个数组mHashes、mArray，其中mHashes为key的Hash值对应的数组*

### 2、put(K key, V value)

```java
public V put(K key, V value) {
		// key 对应的hash值
        final int hash;
        // hash对应的mHashes列表的index
        int index;
        // key为空，hash为0
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        }
        //  
        else {
	        // 计算key的hashcode
            hash = key.hashCode();
            // 查找key对应mHashes中的index,大于0则找到了，否则为未找到
            // 这里涉及到hash冲突,如果hash冲突，则在index的相邻位置插入数据
            index = indexOf(key, hash);
        }
        // 找到key对应mHashes中的index
        if (index >= 0) {
	        // 取出基数位置原有的Value
            index = (index<<1) + 1;
            final V old = (V)mArray[index];
            // 将新数据放到基数index位置
            mArray[index] = value;
            return old;
        }
        // indexOf中取了反，这里反反则正
        index = ~index;
        // 如果满了就扩容  
        if (mSize >= mHashes.length) {
            final int n = mSize >= (BASE_SIZE*2) ? (mSize+(mSize>>1))
                    : (mSize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            // 扩容
            allocArrays(n);
            // 把原来的数据拷贝到扩容后的数组中  
            if (mHashes.length > 0) {
                if (DEBUG) Log.d(TAG, "put: copy 0-" + mSize + " to 0");
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, mSize);
        }
        // 根据上面的二分法查找，如果index小于mSize，说明新的数据是插入到数组之间index位置，插入之前需要把后面的移位  
        if (index < mSize) {
            if (DEBUG) Log.d(TAG, "put: move " + index + "-" + (mSize-index)
                    + " to " + (index+1));
            System.arraycopy(mHashes, index, mHashes, index + 1, mSize - index);
            System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
        }
        // 保存数据
        mHashes[index] = hash;
        mArray[index<<1] = key;
        mArray[(index<<1)+1] = value;
        mSize++;
        return null;
}
// 根据key 与key的hash，查找key对应的index
int indexOf(Object key, int hash) {
        final int N = mSize;
        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }
		// 二分查找mHashes有序数组，查找hash对应的index
        int index = ContainerHelpers.binarySearch(mHashes, N, hash);
		// 没有找到
        if (index < 0) {
            return index;
        }
		// 偶数位为对应的key，则找到了
        if (key.equals(mArray[index<<1])) {
            return index;
        }
		// index之后查找
		// 这里涉及到hash冲突,如果hash冲突，则在index的相邻位置插入数据
        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end << 1])) return end;
        }
		// index之前查找
        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i << 1])) return i;
        }
		// 没有找到
        return ~end;
}
```









在写SparseArray某些情况下比HashMap性能更好，按照官方问答的解释，主要是因为SparseArray不需要对key和value进行auto-boxing（将原始类型封装为对象类型，比如把int类型封装成Integer类型），结构比HashMap简单（SparseArray内部主要使用两个一维数组来保存数据，一个用来存key，一个用来存value）不需要额外的额外的数据结构

##### key为int的时候才能使用，注意是int而不是Integer，这也是sparseArray效率提升的一个点，去掉了装箱的操作

#### 插入

100000条数据的存储使用DDMS查看，hashMap的存储空间14M左右，而SparseArray自由8M多几乎是少了40%接近
 插入时候，SparseArray 正序插入效率比起倒序插入快了几乎是10倍， hashMap差不多。
 我们是按照1,3,2的顺序排列的，但是在SparseArray内部还是按照正序排列的，这时因为SparseArray在检索数据的时候使用的是二分查找，所以每次插入新数据的时候SparseArray都需要重新排序，所以代码4中，逆序是最差情况。

#### SparseArray原理

单纯从字面上来理解，SparseArray指的是稀疏数组(Sparse array)，所谓稀疏数组就是数组中大部分的内容值都未被使用（或都为零），在数组中仅有少部分的空间使用。因此造成内存空间的浪费，为了节省内存空间，并且不影响数组中原有的内容值，我们可以采用一种压缩的方式来表示稀疏数组的内容。
 假设有一个9*7的数组，其内容如下
*

![img](https:////upload-images.jianshu.io/upload_images/5459476-7dc9f2f5bd6da41e.png?imageMogr2/auto-orient/strip|imageView2/2/w/289/format/webp)

*image.png*

*
 在此数组中，共有63个空间，但却只使用了5个元素，造成58个元素空间的浪费。以下我们就使用
  ![img](https:////upload-images.jianshu.io/upload_images/5459476-4cd72b2d004f1efe.png?imageMogr2/auto-orient/strip|imageView2/2/w/323/format/webp) image.png 
 其中在稀疏数组中第一部分所记录的是原数组的列数和行数以及元素使用的个数、第二部分所记录的是原数组中元素的位置和内容。经过压缩之后，原来需要声明大小为63的数组，而使用压缩后，只需要声明大小为6*3的数组，仅需18个存储空间



继续阅读SparseArray的源码，从构造方法我们可以看出，它和一般的List一样，可以预先设置容器大小，默认的大小是10：



```cpp
 public SparseArray() {
        this(10);
    }
```

#### 注意事项

SparseArray是android里为<Interger,Object>这样的Hashmap而专门写的类,目的是提高内存效率，其核心是折半查找函数（binarySearch）。注意内存二字很重要，因为它仅仅提高内存效率，而不是提高执行效率，
 它只适用于android系统（内存对android项目有多重要，地球人都知道）。SparseArray有两个优点：1.避免了自动装箱（auto-boxing），2.数据结构不会依赖于外部对象映射。我们知道HashMap 采用一种所谓的“Hash 算法”来决定每个元素的存储位置，存放的都是数组元素的引用，通过每个对象的hash值来映射对象。而SparseArray则是用数组数据结构来保存映射，然后通过折半查找来找到对象。但其实一般来说，SparseArray执行效率比HashMap要慢一点，因为查找需要折半查找，而添加删除则需要在数组中执行，而HashMap都是通过外部映射。但相对来说影响不大，最主要是SparseArray不需要开辟内存空间来额外存储外部映射，从而节省内存













# [Android学习笔记之性能优化SparseArray](https://www.cnblogs.com/RGogoing/p/5095168.html)

**PS：终于考完试了.来一发.微机原理充满了危机.不过好在数据库89分,还是非常欣慰的.**

 

**学习内容：**

**1.Android中SparseArray的使用..**

 

 **昨天研究完横向二级菜单，发现其中使用了SparseArray去替换HashMap的使用.于是乎自己查了一些相关资料,自己同时对性能进行了一些测试。首先先说一下SparseArray的原理.**

 **SparseArray(稀疏数组).他是Android内部特有的api,标准的jdk是没有这个类的.在Android内部用来替代HashMap<Integer,E>这种形式,使用SparseArray更加节省内存空间的使用,SparseArray也是以key和value对数据进行保存的.使用的时候只需要指定value的类型即可.并且key不需要封装成对象类型.
**

 **楼主根据亲测,SparseArray存储数据占用的内存空间确实比HashMap要小一些.一会放出测试的数据在进行分析。我们首先看一下二者的结构特性.**

 **HashMap是数组和链表的结合体,被称为链表散列.**

**![img](https://images2015.cnblogs.com/blog/734980/201601/734980-20160102193757573-803273823.png)**

 **SparseArray是单纯数组的结合.被称为稀疏数组,对数据保存的时候,不会有额外的开销.结构如下：
**

**![img](https://images2015.cnblogs.com/blog/734980/201601/734980-20160102194502120-1663772128.png)**

 **这就是二者的结构,我们需要看一下二者到底有什么差异...
**

 **首先是插入：**

 **HashMap的正序插入：**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 HashMap<Integer, String>map = new HashMap<Integer, String>();
 long start_map = System.currentTimeMillis();
 for(int i=0;i<MAX;i++){
     map.put(i, String.valueOf(i));
 }
 long map_memory = Runtime.getRuntime().totalMemory();
 long end_map = System.currentTimeMillis()-start_map;
 System.out.println("<---Map的插入时间--->"+end_map+"<---Map占用的内存--->"+map_memory); 执行后的结果: <---Map的插入时间--->914 <---Map占用的内存--->28598272
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

  **SparseArray的正序插入：
**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 SparseArray<String>sparse = new SparseArray<String>();
 long start_sparse = System.currentTimeMillis();
 for(int i=0;i<MAX;i++){
        sparse.put(i, String.valueOf(i));
 }
 long sparse_memory = Runtime.getRuntime().totalMemory();
 long end_sparse = System.currentTimeMillis()-start_sparse;
 System.out.println("<---Sparse的插入时间--->"+end_sparse+"<---Sparse占用的内存--->"+sparse_memory);

//执行后的结果：
<---Sparse的插入时间--->611
<---Sparse占用的内存--->23281664
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

  **我们可以看到100000条数据量正序插入时SparseArray的效率要比HashMap的效率要高.并且占用的内存也比HashMap要小一些..这里的正序插入表示的是i的值是从小到大进行的一个递增..序列取决于i的值，而不是for循环内部如何执行...**

 **通过运行后的结果我们可以发现，SparseArray在正序插入的时候，效率要比HashMap要快得多，并且还节省了一部分内存。网上有很多的说法关于二者的效率问题，很多人都会误认为SparseArray要比HashMap的插入和查找的效率要快，还有人则是认为Hash查找当然要比SparseArray中的二分查找要快得多.
**

 **其实我认为Android中在保存<Integer,Value>的时候推荐使用SparseArray的本质目的不是由于效率的原因，而是内存的原因.我们确实看到了插入的时候SparseArray要比HashMap要快.但是这仅仅是正序插入.我们来看看倒序插入的情况.**

 **HashMap倒序插入：**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
  System.out.println("<------------- 数据量100000 散列程度小 Map 倒序插入--------------->");
  HashMap<Integer, String>map_2 = new HashMap<Integer, String>();
  long start_map_2 = System.currentTimeMillis();
  for(int i=MAX-1;i>=0;i--){
      map_2.put(MAX-i-1, String.valueOf(MAX-i-1));
  }
  long map_memory_2 = Runtime.getRuntime().totalMemory();
  long end_map_2 = System.currentTimeMillis()-start_map_2;
  System.out.println("<---Map的插入时间--->"+end_map_2+"<---Map占用的内存--->"+map_memory_2);
  
  //执行后的结果：
  <------------- 数据量100000 Map 倒序插入--------------->
  <---Map的插入时间--->836<---Map占用的内存--->28598272
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 **SparseArray倒序插入：
**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
System.out.println("<------------- 数据量100000 散列程度小 SparseArray 倒序插入--------------->");
SparseArray<String>sparse_2 = new SparseArray<String>();
long start_sparse_2 = System.currentTimeMillis();
for(int i=MAX-1;i>=0;i--){
    sparse_2.put(i, String.valueOf(MAX-i-1));
}
long sparse_memory_2 = Runtime.getRuntime().totalMemory();
long end_sparse_2 = System.currentTimeMillis()-start_sparse_2;
System.out.println("<---Sparse的插入时间--->"+end_sparse_2+"<---Sparse占用的内存--->"+sparse_memory_2);
//执行后的结果
<------------- 数据量100000 SparseArray 倒序插入--------------->
<---Sparse的插入时间--->20222<---Sparse占用的内存--->23281664
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 **通过上面的运行结果,我们仍然可以看到,SparseArray与HashMap无论是怎样进行插入,数据量相同时,前者都要比后者要省下一部分内存,但是效率呢？我们可以看到,在倒序插入的时候,SparseArray的插入时间和HashMap的插入时间远远不是一个数量级.由于SparseArray每次在插入的时候都要使用二分查找判断是否有相同的值被插入.因此这种倒序的情况是SparseArray效率最差的时候.
**

 **SparseArray的插入源码我们简单的看一下..**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 public void put(int key, E value) {
        int i = ContainerHelpers.binarySearch(mKeys, mSize, key); //二分查找.

        if (i >= 0) {  //如果当前这个i在数组中存在,那么表示插入了相同的key值,只需要将value的值进行覆盖..
            mValues[i] = value;
        } else {  //如果数组内部不存在的话,那么返回的数值必然是负数.
            i = ~i;  //因此需要取i的相反数.
            //i值小于mSize表示在这之前. mKey和mValue数组已经被申请了空间.只是键值被删除了.那么当再次保存新的值的时候.不需要额外的开辟新的内存空间.直接对数组进行赋值即可.
            if (i < mSize && mValues[i] == DELETED) {
                mKeys[i] = key;
                mValues[i] = value;
                return;
            }
            //当需要的空间要超出,但是mKey中存在无用的数值,那么需要调用gc()函数.
            if (mGarbage && mSize >= mKeys.length) {
                gc();
                
                // Search again because indices may have changed.
                i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
            }
            //如果需要的空间大于了原来申请的控件,那么需要为key和value数组开辟新的空间.
            if (mSize >= mKeys.length) {
                int n = ArrayUtils.idealIntArraySize(mSize + 1);
                //定义了一个新的key和value数组.需要大于mSize
                int[] nkeys = new int[n];
                Object[] nvalues = new Object[n];

                // Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
                //对数组进行赋值也就是copy操作.将原来的mKey数组和mValue数组的值赋给新开辟的空间的数组.目的是为了添加新的键值对.
                System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
                System.arraycopy(mValues, 0, nvalues, 0, mValues.length);
                //将数组赋值..这里只是将数组的大小进行扩大..放入键值对的操作不在这里完成.
                mKeys = nkeys;
                mValues = nvalues;
            }
            //如果i的值没有超过mSize的值.只需要扩大mKey的长度即可.
            if (mSize - i != 0) {
                // Log.e("SparseArray", "move " + (mSize - i));
                System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
                System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
            }
            //这里是用来完成放入操作的过程.
            mKeys[i] = key;
            mValues[i] = value;
            mSize++;
        }
    } 
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 **这就是SparseArray插入函数的源码.每次的插入方式都需要调用二分查找.因此这样在倒序插入的时候会导致情况非常的糟糕,效率上绝对输给了HashMap学过数据结构的大家都知道.Map在插入的时候会对冲突因子做出相应的决策.有非常好的处理冲突的方式.不需要遍历每一个值.因此无论是倒序还是正序插入的效率取决于处理冲突的方式,因此插入时牺牲的时间基本是相同的.
**

 **通过插入.我们还是可以看出二者的差异的.**

 **我们再来看一下查找首先是HashMap的查找.
**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
  System.out.println("<------------- 数据量100000 Map查找--------------->");
  HashMap<Integer, String>map = new HashMap<Integer, String>();
       
  for(int i=0;i<MAX;i++){
        map.put(i, String.valueOf(i));
  }
  long start_time =System.currentTimeMillis();
  for(int i=0;i<MAX;i+=100){
           map.get(i);
  }
  long end_time =System.currentTimeMillis()-start_time;
  System.out.println(end_time);
  
  //执行后的结果
  <!---------查找的时间:175------------>
  
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

  **SparseArray的查找:**

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
  System.out.println("<------------- 数据量100000  SparseArray 查找--------------->");
  SparseArray<String>sparse = new SparseArray<String>();
  for(int i=0;i<10000;i++){
        sparse.put(i, String.valueOf(i));
  }
  long start_time =System.currentTimeMillis();
        
  for(int i=0;i<MAX;i+=10){
        sparse.get(i);
  }
  long end_time =System.currentTimeMillis()-start_time;
  System.out.println(end_time);
  //执行后的结果
  <!-----------查找的时间:239---------------->
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 **我这里也简单的对查找的效率进行了测试.\**对一个数据或者是几个数据的查询.二者的差异还是非常小的.\**当数据量是100000条.查100000条的效率还是Map要快一点.数据量为10000的时候.这就差异性就更小.但是Map的查找的效率确实还是赢了一筹.
**

 **其实在我看来.在保存<Integer,E>时使用SparseArray去替换HashMap的主要原因还是因为内存的关系.我们可以看到.保存的数据量无论是大还是小,Map所占用的内存始终是大于SparseArray的.数据量100000条时SparseArray要比HashMap要节约27%的内存.也就是以牺牲效率的代价去节约内存空间.我们知道Android对内存的使用是极为苛刻的.堆区允许使用的最大内存仅仅16M.很容易出现OOM现象的发生.因此在Android中内存的使用是非常的重要的.因此官方才推荐去使用SparseArray<E>去替换HashMap<Integer,E>.\**官方也确实声明这种差异性不会超过50%.\**所以牺牲了部分效率换来内存其实在Android中也算是一种很好的选择吧.**





## 参考文章

1. [SparseArray、ArrayMap 实现原理学习](https://www.cnblogs.com/xiaxveliang/p/12396049.html)

2. [SparseArray分析](https://www.jianshu.com/p/081b78dfe9f6)

3. [Android学习笔记之性能优化SparseArray](https://www.cnblogs.com/RGogoing/p/5095168.html)