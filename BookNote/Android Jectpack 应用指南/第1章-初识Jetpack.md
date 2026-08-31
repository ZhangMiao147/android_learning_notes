# 第 1 章 初识 Jetpack

### 1.1. Android 应用程序架构设计标准的缺失概论

Android 的应用架构始终处于一个混乱的阶段，Android 工程师很困惑，他们不确定自己使用的架构是否真的是最佳方案。这不仅增加了工程师的学习成本，还可能最终导致他们开发出的应用程序质量参差不齐。

Android 工程师希望 Google 官方可以推出并维护一些关于架构的组件或指南，这样他们就可以将更多的精力放在自己的业务代码上了。Google 也意识到了这个问题，这便有了 Jetpack，Jetpack 正是为了解决这些问题而诞生的。

### 1.2. 什么是 Jetpack

 Jetpack 是一套库、工具和指南，可以帮助开发者更轻松地编写应用程序。Jetpack 中的组件可以帮助开发者遵循最佳做法、摆脱编写样板代码的工作并简化复杂的任务，以便他们能将精力集中放在业务所需的代码上。

Jetpack 主要包括 4 个方面，分别是架构（Architecture）、界面（UI）、行为（Behavior）和基础（Foundation）。其中，架构是关注的重点。

### 1.3. Jetpack 与 AndroidX

在 2018 年 Google 宣布用 AndroidX 代替 Android Support Library，Android Support Library 在版本 28 之后就不再更新了，未来的更新会在 AndroidX 中进行。不仅如此，AAC（Android Architecture Component）中的组件也被并入 AndroidX。

为什么 Jetpack 组件需要以兼容包的形式存在，而不是成为 Framework 的一部分呢？为了提供向后兼容，使 Jetpack 组件能够应对更加频繁的更新。除了 Android Support Library 和 AAC，其他一些需要频繁更新和迭代的特定也被并入了 AndroidX，例如 Emoji。

### 1.4. 迁移至 AndroidX

如果从未在项目中使用过 Jetpack 组件，现在希望将项目迁移至 AndroidX，那么可以在菜单栏中选择 “Refactor” -> "Migrate to AndroidX..." 选项，将项目迁移至 AndroidX。

迁移之后，打开项目中的 gradle.properties 文件，可以看到下面这两行代码：

```
android.useAndroidX = true
android.enableJetifier = true
```

上述代码的含义如下：

* android.useAndroidX 表示是否使用 AndroidX。
* android.enableJetifier 表示是否将第三方库迁移至 AndroidX。

### 1.5. 新建项目默认支持 AndroidX

如果 Android Studio 为最新版本，那么在新建一个项目时，应该能在创建过程中看到 “Use androidx.* artifacts” 这个选项。这表示，新创建的项目会默认配置对 AndroidX 的支持。

如果没有看到此选项，那么检查 SDK 配置。通过 “Tools” -> "SDK Manager" 打开配置界面，确保已经安装了 Android 9.0 及以上版本的 SDK。

### 1.6. 总结















