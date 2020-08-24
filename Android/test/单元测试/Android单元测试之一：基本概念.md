# Android单元测试之一：基本概念

# 1. 简单介绍

　　单元测试是应用程序测试策略中的基本测试，通过对代码进行单元测试，一方面可以轻松地验证单个单元的逻辑是否正确，另一方面在每次构建之后运行单元测试，可以快速捕获和修复因代码更改（重构、优化等）带来的回归问题。

# 2. 为什么要进行单元测试？

* 提高稳定性，能够明确地了解是否正确的完成开发；
* 快速反馈 bug ，跑一遍单元测试用例，定位 bug ；
* 在开发周期中尽早通过单元测试检查 bug ，最小化技术债，越往后可能修复 bug 的代价会越大，严重的情况下会影响项目进度；
* 为代码重构提供安全保障，在优化代码时不用担心回归问题，在重构后跑一遍测试用例，没通过说明重构可能是有问题的，更加易于维护。

# 3. 单元测试要测什么

* 列出想要测试覆盖的正常、异常情况，进行测试验证；
* 性能测试，例如某个算法的耗时等等。

# 4. 单元测试的分类

1. 本地测试（ Local tests）：只在本地机器 JVM 上运行，以最小化执行时间，这种单元测试不依赖于 Android 框架，或者即使有依赖，也很方便使用模拟框架来模拟依赖，以达到隔离 Android 依赖的目的，模拟框架如 google 推荐的 Mockito；
2. 仪器化测试（Instrumented tests）：在真机或模拟器上运行的单元测试，由于需要跑到设备上，比较慢，这些测试可以访问仪器（Android 系统）信息，比如被测应用程序的上下文，一般地，依赖不太方便通过模拟框架模拟时采用这种方式。

# 5. JUnit 注解

　　了解一些 Jnit 注解，方便后面的测试说明。

| Annotation | 描述 |
| -------- | -------- |
| @Test publish void method | 定义所在方法为单元测试方法 |
| @Test(expected=Exception.class) public void method() | 测试方法若没有抛出 Annotation 中的 Exception 类型（子类型也可以）-> 失败 |
| @Test(timeout=100) public void method() | 性能测试，如果方法耗时超过 100 毫秒 -> 失败 |
| @Before public void method() | 这个方法在每个测试之前执行，用于准备测试环境（如：初始化类，读输入流等），在一个测试类中，每个 @Test 方法的执行都会触发一次调用。 |
| @After publish void method() | 这个方法在每个测试之后执行，用于清理测试环境数据，在一个测试类中，每个 @Test 方法的执行都会触发一次调用。 |
| @BeforeClass public static void method() | 这个方法在所有测试开始之前执行一次，用于做一些耗时的初始化工作（如，连接数据库），方法必须是 static 。 |
| @AfterClass public static void method() | 这个方法在所有测试结束之后执行一次，用于清理数据（如：断开数据连接），方法必须是 static 。 |
| @Ignore或者@Ignore(“太耗时”) public void method | 忽略当前测试方法，一般用于测试方法还没准备好，或者太耗时之类的。 |
| @FixMethodOrder(MethodAorters.NAME_ASCENDING) public class TestClass{} | 使得该测试类中的所有测试方法都按照方法名的字母顺序执行，分别指定 3 个值，DEFAULT、JVM、NAME_ASCENDING。 |

# 6. 参考文章

1. [Android单元测试只看这一篇就够了](https://www.jianshu.com/p/aa51a3e007e2)