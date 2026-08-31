## Koin

Koin 时专为 Kotlin 打造的轻量级依赖注入（DI）框架，主要用于 Android、Kotlin 后端、Ktor 后端，替代 Hilt/Dagger，主打简单、少配置、上手快。



## Koin 问题

### 1. KoinViewModel 与 KoinApplicationViewModel 的区别？

`koinViewModel()` 和 `koinApplicationViewModel()` 这两个函数的核心区别在于它们的**作用域平台和生命周期**：

- `koinViewModel()`：是**跨平台通用**的，其作用域绑定在 Compose 组件树中的`KoinApplication`上，主要用于在共享代码（`commonMain`）中管理 ViewModel。

- `koinApplicationViewModel()`：是 **Android 专属**的，其作用域绑定在**整个应用程序**（Application）级别，生命周期与 App 进程一致。由于缺乏明确的生命周期边界，**极易引发内存泄漏**，因此在 Koin 4.0 版本中已被标记为 `@Deprecated` 并移除，强烈不推荐使用。

### 📊 深入对比：`koinViewModel()` vs `koinApplicationViewModel()`

| 特性         | `koinViewModel()`                                                                                                                                                      | `koinApplicationViewModel()`                                                                                                        |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **平台支持**   | **跨平台通用 (KMP)**<br>适用于 Android、iOS、Desktop 等所有 Koin 支持的平台[-27](https://insert-koin.io/docs/4.0/reference/koin-annotations/definitions/#automatic-or-specific-binding)。 | **Android 专属**<br>仅在 `koin-android` 等模块中存在，不适用于跨平台项目。                                                                               |
| **作用域绑定**  | **本地作用域**<br>默认绑定在组件树中的 `KoinApplication` 作用域上，遵循 Compose 的组件树生命周期[-12](https://blog.gitcode.com/c7473fdde16165eddbb0e11f45890eb3.html)。                               | **全局作用域**<br>绑定在整个 Koin 容器的全局根作用域上。                                                                                                 |
| **生命周期**   | **可预测**<br>随着其所在的 `KoinApplication` 作用域被销毁而自动清理，生命周期由框架管理。                                                                                                             | **与进程同寿**<br>一旦创建，在 App 的整个生命周期内都不会被销毁，由开发者自行管理。                                                                                    |
| **生命周期管理** | **自动**<br>Koin 框架会在作用域结束时自动清理，无需开发者干预。                                                                                                                                 | **手动**<br>需要开发者自行在合适的时机（如 Activity 销毁时）调用 `close()` 等方法进行清理，容易出错。                                                                   |
| **内存泄漏风险** | **低**<br>作用域明确，生命周期由框架管理，不易发生内存泄漏。                                                                                                                                     | **高**<br>极易发生内存泄漏，因为 ViewModel 会持有 Activity 或 Fragment 的引用，但它的生命周期比这些组件更长。                                                          |
| **推荐程度**   | **强烈推荐**<br>这是 Koin 官方推荐的在 Compose 中管理 ViewModel 的标准方式。                                                                                                                | **极度不推荐**<br>在 Koin 3.x 版本中已标记为 `@Deprecated`，并在 4.0 版本中被完全移除[-11](https://blog.gitcode.com/ba7dbd3efdde7baae8009aad11e8d81f.html)。 |
