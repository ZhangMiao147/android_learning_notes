# Proto DataStore

## 1. 什么是 DataStore

DataStore 是经过改进的新版数据存储解决方案，旨在取代 SharedPreferences。DataStore 基于 Kotlin 协程和 Flow 构建，提供两种不同的实现：Proto DataStore，用于存储类型化对象（由协议缓冲区支持）；Preferences DataStore，用于存储键值对。

DataStore 以异步、一致的事物方式存储数据，克服了 SharedPreferences 的一些缺点。

### 1.1. 存储方式对比

在需要存储较小或简单的数据集时，过去可能使用过 Sharedpreferences，但此 API 也存在一系列缺点。Jetpack DataStore 库旨在解决这些问题，从而创建一个简单、安全性更高的异步 API 来存储数据。它提供 2 种不同的实现：

* Preferences DataStore
* Proto DataStore

| 功能     | SharedPreferences                                  | PreferencesDataStore                                      | ProtoDataStore                                            |
| -------- | -------------------------------------------------- | --------------------------------------------------------- | --------------------------------------------------------- |
| 异步 API | :white_check_mark:(仅用于通过监听器读取已更改的值) | :white_check_mark:(通过 Flow 以及 RxJava 2 和 3 Flowable) | :white_check_mark:(通过 Flow 以及 RxJava 2 和 3 Flowable) |
|          | :white_check_mark:(但无法在界面线程上安全调用)     | :x:                                                       | :x:                                                       |
|          | :x:(1)                                             | :white_check_mark:(这项工作已在后台移至Dispatchers.IO)    | :white_check_mark:(这项工作已在后台移至Dispatchers.IO)    |
|          | :x:                                                | :white_check_mark:                                        | :white_check_mark:                                        |
|          | :x:(2)                                             | :white_check_mark:                                        | :white_check_mark:                                        |
|          | :x:                                                | :white_check_mark:                                        | :white_check_mark:                                        |
|          | :x:                                                | :white_check_mark:                                        | :white_check_mark:                                        |
|          | :x:                                                | :x:                                                       | :white_check_mark:(使用协议缓冲区)                        |

（1）SharedPreferences 有一个看上去可以在界面线程中安全调用的同步 API，但是该 API 实际上执行磁盘 I/O 操作。此外，`apply()`会阻断 `fsync()`上的界面线程。每次有服务器启动或停止以及每次 activity 在应用中的任何地方启动或停止时，系统都会触发待处理的 `fsync()`调用。界面线程在 `apply()`调度的待处理 `fsync()`调用上会被阻断，这通常会导致 ANR。

（2）SharedPreferences 会将解析错误作为运行时异常抛出。

**Preferences DataStore 与 Proto DataStore**

虽然 Preferences DataStore 和 Proto DataStore 都允许保存数据，但它们保存数据的方式不同：

* 与 SharedPreferences 一样，Preferences DataStore 根据键访问数据，而无需事先定义架构。
* Proto DataStore 使用协议缓冲区来定义架构。使用协议缓冲区可持久保留强类型数据。与 XML 和其他类似的数据格式相比，协议缓冲区速度更快、规格更小、使用更简单，并且更清楚明了。虽然使用 Proto DataStore 需要学习心得序列化机制，但认为 Proto DataStore 有着强大的类型优势，值得学习。

**Room 与 DataStore**

如果需要实现部分更新、引用完整性和大型/复杂数据集，应考虑使用 Room，而不是 DataStore。DataStore 非常适合小型和简单的数据集，但不支持部分更新或引用完整性。

## 2. Proto DataStore

SharedPreferences 和 Preferences DataStore 的一个缺点是无法定义架构，保证不了存取键时使用了正确的数据类型。Proto DataStore 可利用协议缓冲区定义架构来解决此问题。通过使用协议，DataStore 可以知道存储的类型，并且无需使用键便能提供类型。

**添加依赖项**

为了使用 Proto DataStore，让协议缓冲区为架构生成代码，需要对 build.gradle 文件进行一些更改：

* 添加协议缓冲区插件
* 添加协议缓冲区和 Proto DataStore 依赖项
* 配置协议缓冲区

```groovy
plugins {
    ...
    id "com.google.protobuf" version "0.8.17"
}

dependencies {
    implementation  "androidx.datastore:datastore-core:1.0.0"
    implementation  "com.google.protobuf:protobuf-javalite:3.18.0"
    ...
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option 'lite'
                }
            }
        }
    }
}
```

## 3. 定义和使用 protobuf 对象

协议缓冲区是一种对结构化数据进行序列化的机制。只需对数据结构化的方式进行一次定义，编译器便会生成源代码，轻松写入和读取结构化数据。

**创建 proto 文件**

可以在 proto 文件中定义架构。在此 Codelab 中，有两个用户偏好设置：`show_completed`和`sort_order`；目前两者以两种不同的对象来表示。因此，目标是将这两个标志统一到存储在 DataStore 中的一个 `UserPreferences`类下。将在协议缓冲区架构而非 Kotlin 中定义该类。

在 `app/src/main/proto`目录中创建一个名为 `user_prefs.proto`的新文件。如果未看到此文件夹结构，切换到项目视图试试。在协议缓冲区中，每个结构都使用一个 `message`关键字进行定义，并且结构中的每一个成员都会根据类型和名称在消息内进行定义，从而获得从 1 开始的排序。

定义一个 `UserPreferences`消息，目前该消息只有一个名为 `show_completed`的布尔值。

```protobuf
syntax = "proto3";

option java_package = "com.codelab.android.datastore";
option java_multiple_files = true;

message UserPreferences {
  // filter for showing / hiding completed tasks
  bool show_completed = 1;
}
```

UserPreferences 类在编译时会从 proto 文件中定义的 message 中生成。请务必重新构建该项目。

**创建序列化器**

如需告知 DataStore 如何读取和写入在 proto 文件中定义的数据类型，需要实现序列化器。如果磁盘上没有数据，序列化器还会定义默认返回值。在 `data`包中创建一个名为 `UserPreferencesSerializer`的新文件：

```kotlin
object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}
```

如果找不到 UserPreferences 对象或相关方法，清理并重建项目，以确保协议缓冲区生成对象。

## 4. 在 Proto DataStore 中保留数据

### 4.1. 创建 DataStore

`showCompleted` 标志保存在内存中的 `TasksViewModel` 里，但它应该存储在 `UserPreferencesRepository` 的 DataStore 实例中。

为了创建 DataStore 实例，使用 `dataStore` 委托，并将 `Context` 作为接收器。此委托有两个必需参数：

* DataStore 会处理的文件的名称。
* DataStore 使用的类型的序列化器。使用的序列化器如下：`UserPreferencesSerializer`。

```kotlin
private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
private const val SORT_ORDER_KEY = "sort_order"

private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = UserPreferencesSerializer
)
```

`dataStore` 委托可确保在应用中有一个具有该名称的 DataStore 实例。目前，`UserPreferencesRepository` 是作为单例实现的，因为它用于存储 `sortOrderFlow` 并避免将它与 `TaskActivity` 的生命周期相关联。由于 `UserPreferenceRepository` 只会处理来自 Datastore 的数据，而不会创建和存储任何新对象，因此已经可以移除单例实现：

* 移除 `companion object`
* 将 `constructor` 设为公开

`UserPreferencesRepository` 应该获取一个 `DataStore` 实例作为构造函数参数。现在可以将 `Context` 保留为参数，因为 SharedPreferences 需要用到它，稍后移除。

```kotlin
class UserPreferencesRepository(
    private val userPreferencesStore: DataStore<UserPreferences>,
    context: Context
) { ... }
```

在正式版应用中，应该使用 DataStoreFactory 将 DataStore 实例注入需要它的类中。

下面，在 `TasksActivity` 中更新 `UserPreferencesRepository` 的构造，并传入 `dataStore` ：

```kotlin
viewModel = ViewModelProvider(
    this,
    TasksViewModelFactory(
        TasksRepository,
        UserPreferencesRepository(dataStore, this)
    )
).get(TasksViewModel::class.java)
```

### 4.2. 从 Proto DataStore 中读取数据

Proto DataStore 会公开存储在 `Flow<UserPreferences>` 中的数据。创建一个被赋予 `dataStore.data` 的公共 `userPreferencesFlow: Flow<UserPreferences>` 值：

```kotlin
val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
```

**读取数据时处理异常**

由于 DataStore 从文件中读取数据，因此如果读取数据时出现错误，系统会抛出 `IOException`。可以使用 `catch` Flow 转换来处理这些异常，只需记录错误即可：

```kotlin
private val TAG: String = "UserPreferencesRepo"

val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            Log.e(TAG, "Error reading sort order preferences.", exception)
            emit(UserPreferences.getDefaultInstance())
        } else {
            throw exception
        }
    }
```

### 4.3. 将数据写入 Proto DataStore

为了写入数据，DataStore 提供了一个挂起 `DataStore.updateData()` 函数，将在此函数中以参数的形式获取 `UserPreferences` 的当前状态。若要更新状态，需要将偏好对象转换为构建器，设置新值，并构建新的偏好。

`updateData()` 在读取-写入-修改原子操作中用事物的方式更新数据。一旦数据持久存储在磁盘中，协程便会完成。

现在来创建一个挂起函数，以便能够更新 `UserPreferences` 的 `showCompleted` 属性，此函数称为 `updateShowCompleted()`，用于调用 `dataStore.updateData()` 并设置新值：

```kotlin
suspend fun updateShowCompleted(completed: Boolean) {
    dataStore.updateData { preferences ->
        preferences.toBuilder().setShowCompleted(completed).build()
    }
}
```

此时，应用可以成功编译，但是刚刚在 `UserPreferencesRepository` 中创建的功能不会被使用。

## 5. 从 SharedPreferences 迁移到 Proto DataStore

### 5.1. 定义将要在 proto 中保存的数据

排序顺序保存在 SharedPreferences 中。将其迁移到 DataStore 中。首先，更新 proto 文件中的 `UserPreferences`，以存储排序顺序。由于 `SortOrder` 是一个 `enum`，因此必须在 `UserPreference` 中进行定义。与 Kotlin 类似，`enums` 需要在协议缓冲区中进行定义。

枚举的默认值为枚举类型定义中列出的第一个值。但是从 SharedPreferences 向外迁移时，需要知道得到的值时默认值还是先前在 SharedPreferences 中设置的值。为此，为 `SortOrder` 枚举定义一个新值：`UNSPECIFIED`，并将其置于首位，这样它就可以作为默认值来使用。

`user_prefs.proto` 文件应如下所示：

```protobuf
syntax = "proto3";

option java_package = "com.codelab.android.datastore";
option java_multiple_files = true;

message UserPreferences {
  // filter for showing / hiding completed tasks
  bool show_completed = 1;

  // defines tasks sorting order: no order, by deadline, by priority, by deadline and priority
  enum SortOrder {
    UNSPECIFIED = 0;
    NONE = 1;
    BY_DEADLINE = 2;
    BY_PRIORITY = 3;
    BY_DEADLINE_AND_PRIORITY = 4;
  }

  // user selected tasks sorting order
  SortOrder sort_order = 2;
}
```

清理并重构项目，确保生成一个包含新字段的新 `UserPreferences` 对象。

现在，`SortOrder` 已在 proto 文件中得到定义，可以将声明从 `UserPreferencesRepository` 中移除。请删除以下内容：

```kotlin
enum class SortOrder {
    NONE,
    BY_DEADLINE,
    BY_PRIORITY,
    BY_DEADLINE_AND_PRIORITY
}
```

请确保在所有地方使用正确的 `SortOrder` 导入：

```kotlin
import com.codelab.android.datastore.UserPreferences.SortOrder
```

修改完架构之后，不需要执行任何其他操作，即可确保从 DataStore 中读取新字段。dataStore.data 发出的 UserPreferences 对象中将包括 proto 文件中定义的所有字段。

现在，正在 `TasksViewModel.filterSortTasks()` 中根据 `SortOrder` 类型执行不同的操作。由于还添加了 `UNSPECIFIED` 选项，接下来需要为 `when(sortOrder)` 语句添加另一个 case。由于不打算处理除当前选项之外的其他选项，只需要在其他 case 中抛出一个 `UnsupportedOperationException` 便可。

`filterSortTasks()` 函数现在如下所示：

```kotlin
private fun filterSortTasks(
    tasks: List<Task>,
    showCompleted: Boolean,
    sortOrder: SortOrder
): List<Task> {
    // filter the tasks
    val filteredTasks = if (showCompleted) {
        tasks
    } else {
        tasks.filter { !it.completed }
    }
    // sort the tasks
    return when (sortOrder) {
        SortOrder.UNSPECIFIED -> filteredTasks
        SortOrder.NONE -> filteredTasks
        SortOrder.BY_DEADLINE -> filteredTasks.sortedByDescending { it.deadline }
        SortOrder.BY_PRIORITY -> filteredTasks.sortedBy { it.priority }
        SortOrder.BY_DEADLINE_AND_PRIORITY -> filteredTasks.sortedWith(
            compareByDescending<Task> { it.deadline }.thenBy { it.priority }
        )
        // We shouldn't get any other values
        else -> throw UnsupportedOperationException("$sortOrder not supported")
    }
}
```

### 5.2. 从 SharedPreferences 向外迁移

为了便于迁移，DataStore 定义了 `SharedPreferencesMigration` 类。用于创建 Datastore（用在 `TasksActivity` 中）的 `by dataStore` 方法还会提供 `produceMigrations` 参数。在该代码块中，创建应为此 Datastore 实例运行的 `DataMigration` 列表。在本例中，只需迁移：`SharedPreferencesMigration`。

在实现 `SharedPreferencesMigration` 时，`migrate` 代码块提供了两个参数：

* `SharedPreferencesView` - 可以用于从 SharedPreferences 中检索数据
* `UserPreferences` - 当前数据

必须返回一个 `UserPreferences` 对象。

实现 `migrate` 代码块时，必须执行以下步骤：

1. 检查 `UserPreferences` 中的 `sortOrder` 值 。
2. 如果此值为 `SortOrder.UNSPECIFIED`，表示需要从 SharedPreferences 检索该值。如果缺少 `SortOrder`，则可以使用 SortOrder.NONE 作为默认值。
3. 获取排序顺序后，需要将 `UserPreferences` 对象转换为构建器，设置为排序顺序，然后通过调用 `build()` 再次构建对象。此更改不会影响其他任何字段。
4. 如果 `UserPreferences` 中的 `sortOrder` 值不是 `SortOrder.UNSPECIFIED`，说明迁移肯定已经成功运行，所以可以返回在 `migrate` 中获得的当前数据。

```kotlin
private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = UserPreferencesSerializer,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context,
                USER_PREFERENCES_NAME
            ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
                // Define the mapping from SharedPreferences to UserPreferences
                if (currentData.sortOrder == SortOrder.UNSPECIFIED) {
                    currentData.toBuilder().setSortOrder(
                        SortOrder.valueOf(
                            sharedPrefs.getString(SORT_ORDER_KEY, SortOrder.NONE.name)!!
                        )
                    ).build()
                } else {
                    currentData
                }
            }
        )
    }
)
```

由于已经定义迁移逻辑，现在需要告知  DataStore 应该使用该迁移逻辑了。

### 5.3. 将排序顺序保存在 DataStore

为了能在调用 `enableSortByDeadline()` 和 `enableSortByPriority()` 时更新排序顺序，必须执行以下操作：

* 在 `dataStore.updateData()` 的 lambda 中调用两者各自的功能。
* 由于 `updateData()` 是挂起函数，因此还应将 `enableSortByDeadline()` 和 `enableSortByPriority()` 也设置为挂起函数。
* 使用从 `updateData()` 收到的最新 `UserPreferences` 来构造新的排序顺序
* 通过将 `UserPreferences` 转换成构建器，设置新的排序顺序，然后再次构建 preference 来实现更新。

`enableSortByDealine()` 的实现形式如下。可自行更改 `enableSortByPriority()`。

```kotlin
suspend fun enableSortByDeadline(enable: Boolean) {
    // updateData handles data transactionally, ensuring that if the sort is updated at the same
    // time from another thread, we won't have conflicts
    dataStore.updateData { preferences ->
        val currentOrder = preferences.sortOrder
        val newSortOrder =
            if (enable) {
                if (currentOrder == SortOrder.BY_PRIORITY) {
                    SortOrder.BY_DEADLINE_AND_PRIORITY
                } else {
                    SortOrder.BY_DEADLINE
                }
            } else {
                if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                    SortOrder.BY_PRIORITY
                } else {
                    SortOrder.NONE
                }
            }
        preferences.toBuilder().setSortOrder(newSortOrder).build()
    }
}
```

现在，可以移除 `context` 构造函数参数和使用的所有 SharedPreferences。

## 6. 更新 TasksViewModel 以使用 UserPreferencesRepository

现在，`UserPreferencesRepository` 在 DataStore 中存储了 `show_completed` 和 `sort_order` 标志，并提供了 `Flow<UserPreferences>`。接下来，更新并使用 `TasksViewModel`。

移除 `showCompletedFlow` 和 `sortOrderFlow`，创建一个名为 `userPreferencesFlow` 的值并用 `UserPreferencesRepository.userPreferencesFlow` 对该值进行初始化：

```kotlin
private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
```

在 `tasksUiModelFlow` 创建中，将 `showCompletedFlow` 和 `sortOrderFlow` 替换为 `userPreferencesFlow`。请相应地替换参数。

调用 `filterSortTasks` 时，传入 `userPreferences` 的 `showCompleted` 和 `sortOrder`。代码应如下所示：

```kotlin
private val tasksUiModelFlow = combine(
        repository.tasks,
        userPreferencesFlow
    ) { tasks: List<Task>, userPreferences: UserPreferences ->
        return@combine TasksUiModel(
            tasks = filterSortTasks(
                tasks,
                userPreferences.showCompleted,
                userPreferences.sortOrder
            ),
            showCompleted = userPreferences.showCompleted,
            sortOrder = userPreferences.sortOrder
        )
    }
```

`showCompletedTasks()` 函数现在应已更新为调用 `userPreferencesRepository.updateShowCompleted()`。由于这是一个挂起函数，因此请在 `viewModelScope` 中创建一个新的协程：

```kotlin
fun showCompletedTasks(show: Boolean) {
    viewModelScope.launch {
        userPreferencesRepository.updateShowCompleted(show)
    }
}
```

`userPreferencesRepository` 函数，`enableSortByDealine()` 和 `enableSortByPriority()` 现在属于挂起函数，因此还应在 `viewModelScope` 中启动的新协程中调用它们：

```kotlin
fun enableSortByDeadline(enable: Boolean) {
    viewModelScope.launch {
       userPreferencesRepository.enableSortByDeadline(enable)
    }
}

fun enableSortByPriority(enable: Boolean) {
    viewModelScope.launch {
        userPreferencesRepository.enableSortByPriority(enable)
    }
}
```

**清理 UserPreferencesRepository**

现在来移除已经不需要的字段和方法。应能删除以下内容：

* _sortOrderFlow
* sortOrderFlow
* updateSortOrder()
* private val sortOrder: SortOrder
* private val sharedPreferences

应用现在应能成功进行编译。运行一下，看看 `show_completed` 和 `sort_order` 标志是否能成功保存。

查看 Codelab 代码库的 `proto_datastore` 分之，并与更改进行比较。

## 7. 小结

* SharedPreferences 存在一些缺点，包括看上去可以在界面线程中安全调用的同步 API，没有发出错误信号的机制，缺少事务性 API 等。
* DataStore 可替代 SharedPreferences，解决 API 的大部分问题。
* DataStore 有一个使用 Kotlin 协程和 Flow 的完全异步 API，可以处理数据迁移，保证数据一致性并处理数据损坏问题。

## 参考文章

[使用 Proto DataStore](https://developer.android.com/codelabs/android-proto-datastore?hl=zh-cn)
