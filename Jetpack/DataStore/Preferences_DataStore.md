# Preferences DataStore

DataStore 是用来取代 SharedPreferences 的一种简易数据存储的解决方案。

DataStore 并不是具体的代码实现，目前其具体实现有两种方式：

* Preferences DataStore
* Proto DataStore

## 1. 创建 Preferences DataStore

首先得添加下依赖：

```groovy
implementation "androidx.datastore:datastore-preferences:1.0.0"
```

然后创建 DataStore：

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "number")
```

## 2. 存储数据

在以往使用 SharedPreferences 的时候，都是使用 \<Key,Value> 的形式进行填写的，但是 DataStore 与 SharedPreferences 不同的是，DataStore 并不是以 String 作为 key，而是以 Preferences.Key 作为 Key，不同的数据类型需要不同写法：

* Int -> intPreferencesKey(name)
* Double -> doublePreferencesKey(name)
* String -> stringPreferencesKey(name)
* Boolean -> booleanPreferencesKey(name)
* Float -> floatPreferencesKey(name)
* Long -> longPreferencesKey(name)
* Set -> stringPreferencesKey(name)

以存储 int 类型作为示例，把值读出来，再 +1 放回去：

```kotlin
	    val EXAMPLE_COUNTER = intPreferencesKey("example_counter")   // 创建 key

        dataStore.edit { settings ->
            val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
            settings[EXAMPLE_COUNTER] = currentCounterValue + 1
        }
```

## 3. 读取数据

```kotlin
val EXAMPLE_COUNTER = intPreferencesKey("example_counter")  // 创建 key

val exampleCounterFlow: Flow<Int> = dataStore.data
  .map { preferences ->
    preferences[EXAMPLE_COUNTER] ?: 0
}
```

要注意，exampleCounterFlow 是一个 Flow，是一个冷流，map 只是一个数据转换，所以，想要真正获取数据还需要调用 collect：

```kotlin
        dataStore.data
            .map { preferences ->
                preferences[EXAMPLE_COUNTER] ?: 0
            }.collect {
                println("EXAMPLE_COUNTER 的值：$it")
            }
```

## 4. 清除所有数据

```kotlin
        dataStore.edit {
            it.clear()
        }
```

## 5. SharedPreferences 兼容

Preferences DataStore 还提供一种方式，将 SharedPreferences 里面的数据迁移到 Preferences DataStore 中。

SharePreferences 写入数据：

```kotlin
        getSharedPreferences("old_data", MODE_PRIVATE).edit().apply {
            putString("name", "不近视的猫")
            apply()
        }
```

之后就会生成一个存储文件：

![](https://img-blog.csdnimg.cn/1b0a07c4a1a24af7bc4537a9865576a4.png#pic_center)

文件内容：

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="name">不近视的猫</string>
</map>
```

下面就是通过 SharedPreferencesMigration 进行 SharedPreferences 的迁移了：

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "new_data",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "old_data"))
    })
```

直接这样声明是不会进行运行的，需要稍微使用到 dataStore：

```kotlin
        GlobalScope.launch {
            dataStore.edit {  }
        }
```

然后就会看到 old_data.xml 文件被删除了，而被取代的是 `/data/data/<package_name>/files/datastore/new_data.preferences_pb`：

![](https://img-blog.csdnimg.cn/abf51205ea1d4ac7972ce784b1d9ae8b.png#pic_center)

## 参考文章

[Preferences DataStore全解析](https://blog.csdn.net/m0_46278918/article/details/125517664)
