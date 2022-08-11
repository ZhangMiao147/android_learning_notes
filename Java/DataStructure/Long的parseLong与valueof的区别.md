# Long 的 parseLong 与 valueof 的区别

## 作用

​		将 String 转化成 long 类型。

## 区别

* Long.ValueOf("String") 返回 Long 包装类型
* Long.parseLong("String") 返回 long 基本数据类型。

```java
  	// 返回类型是 Long  
    public static Long valueOf(String s) throws NumberFormatException
    {
      	// 仍然是调用 Long.parseLong("String") 方法
        return Long.valueOf(parseLong(s, 10));
    }
    
		// 返回类型是 long
    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 10);
    }
```

* Long.valueof("String") 仍然是调用 Long.parseLong("String") 方法。所以一些代码检测工具如 SonarQube 或 FindBUGs，会提示推荐使用 Long.parseLong(“String”)。



