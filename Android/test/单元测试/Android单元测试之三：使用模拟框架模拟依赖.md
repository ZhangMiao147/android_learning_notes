# Android单元测试之三：使用模拟框架模拟依赖

## 基本描述
　　如果是一些工具类方法的测试，如计算两数之和的方法，本地 JVM 虚拟机就能提供足够的运行环境，但如果要测试的单元依赖了 Android 框架，比如用到了 Android 中的 Context 类的一些方法，本地 JVM 将无法提供这样的环境，这时候模拟框架 Mockito 就派上用场了。
## 使用

#### 引入框架
```xml
testImplementation 'org.mockito:mockito-core:2.19.0'
```

#### 测试类
```java
package com.zm.androidUnitTest;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by zhangmiao on 2019/2/24.
 */

@RunWith(MockitoJUnitRunner.class)
public class MockUnitTest {
    private static final String FAKE_STRING = "AndroidUnitTest";

    @Mock
    Context mMockContext;

    @Test
    public void readStringFromContext_LocalizedString() {
        when(mMockContext.getString(R.string.app_name)).thenReturn(FAKE_STRING);
        assertThat(mMockContext.getString(R.string.app_name),is(FAKE_STRING));

        when(mMockContext.getPackageName()).thenReturn("com.jdqm.androidunittest");
        System.out.println(mMockContext.getPackageName());
    }
}
```

#### 测试结果
![](image/context_test.png)

## 参考文章
https://www.jianshu.com/p/aa51a3e007e2

