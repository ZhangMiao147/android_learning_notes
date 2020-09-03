# Java 基础的常见问题 4

# 1. 为什么 Java 枚举文字不能具有泛型类型参数

[enum泛型 - 为什么Java枚举文字不能具有泛型类型参数?](https://code-examples.net/zh-CN/q/41793e)

由于类型擦除。

这两种方法都不可能，因为参数类型被擦除。

```java
public <T> T getValue(MyEnum<T> param);
public T convert(Object);
```

可以用以下代码解决

```java
public enum MyEnum {
    LITERAL1(String.class),
    LITERAL2(Integer.class),
    LITERAL3(Object.class);

    private Class<?> clazz;

    private MyEnum(Class<?> clazz) {
      this.clazz = clazz;
    }

    ...

}
```

# 2. arraylist,linkedlist,vector 效率

ArrayList 底层是数组结构，查询快，增删慢，线程不安全，效率高。

LinkedList 底层是链表数据结构，查询慢，增删快，线程不安全，效率高。

Vector 底层是数组结构，查询快，增删慢，线程安全，效率低。

# 3. 请编程实现Java的生产者-消费者模型 

# 4. final finally finalize区别 

final 关键字可以用于类、方法、变量前，用来表示该关键字修饰的类、方法、变量具有不可变的特性。

final 关键字用于基本数据类型前：这是表明该关键字修饰的变量是一个常量，在定义后该变量的值就不能被修改。

final 关键字用于方法声明前：这是意味着该方法是最终方法，只能被调用，不能被覆盖，但是可以被重载。

final 关键字用于类名前：这是该类被称为最终类，该类不能被其他类继承。

finally：当代码抛出一个异常时，就会终止方法中剩余代码的处理，并退出这个方法的执行，可能会产生资源没有回收的问题。java 提供的解决方案就是 finally 子句，finally 子句中的语句是一定会被执行的，所以在 finally 子句中回收资源即可。

finalize：finalize() 方法来自于 java.lang.object，用于回收资源。可以为任何一个类添加 finalize 方法。finalize 方法将在垃圾回收器清除对象之前调用。在实际应用中，不要依赖该方法回收任何短缺的资源，因为很难知道这个方法什么时候被调用。

# 5. 输入一个数组，想一种方法让这个数组尽可能的乱序，保证功能能实现的情况下时间复杂度和空间复杂度尽可能的小，可使用随机数函数。

Fisher-Yates Shuffle ：算法思想就是从原始数组中随机抽取一个新的数字到新数组中。

Knuth-Durstendeld Shuffle：每次从未处理的数据中随机取出一个数组，然后把该数字放在数组的尾部，即数组尾部存放的是已经处理过的数组。

Inside-Out Shuffle：是一个 in-place 算法，原始数据被直接打乱，有些应用中可能需要保留原始数据，因此需要开辟一个新数组来存储打乱后的序列。Inside-Out Algorithm 算法的基本思想是设一游标 i 从前向后扫描原始数据的拷贝，在 [0,i] 之间随机一个小标 j，然后用位置 j 的元素替换掉位置 i 的数字，再用原始数据位置 i 的元素替换掉拷贝数据位置 j 的元素。

```java
    /**
     * Fisher-Yates Shuffle ：
     * 算法思想就是从原始数组中随机抽取一个新的数字到新数组中。
     *
     * @param nums
     * @return
     */
    public static int[] FYShuffle(int[] nums) {
        int[] result = new int[nums.length];
        boolean[] flags = new boolean[nums.length];
        Random random = new Random();
        int n = 0;
        while (n != nums.length) {
            int r = random.nextInt(nums.length);
            if (!flags[r]) {
                result[n++] = nums[r];
                flags[r] = true;
            }
        }
        return result;
    }

    /**
     * Knuth-Durstendeld Shuffle：
     * 每次从未处理的数据中随机取出一个数组，然后把该数字放在数组的尾部，即数组尾部存放的是已经处理过的数组。
     *
     * @param nums
     */
    public static void KDShuffle(int[] nums) {
        Random random = new Random();
        for (int i = nums.length - 1; i > 0; i--) {
            int r = random.nextInt(i);
            int temp = nums[r];
            nums[r] = nums[i];
            nums[i] = temp;
        }
    }

    /**
     * Inside-Out Shuffle：是一个 in-place 算法，原始数据被直接打乱，有些应用中可能需要保留原始数据，因此需要开辟一个新数组来存储打乱后的序列。
     * Inside-Out Algorithm 算法的基本思想是设一游标 i 从前向后扫描原始数据的拷贝，
     * 在 [0,i] 之间随机一个小标 j，然后用位置 j 的元素替换掉位置 i 的数字，再用原始数据位置 i 的元素替换掉拷贝数据位置 j 的元素。
     * @param nums
     */
    public static void IOShuffle(int[] nums){
        Random random = new Random();
        for (int i = 1;i<nums.length;i++){
            int r = random.nextInt(i);
            int temp = nums[i];
            nums[i] = nums[r];
            nums[r] = temp;
        }
    }
```



# 4. 文件 IO 操作

# 5. AtomInteger

AtomInteger