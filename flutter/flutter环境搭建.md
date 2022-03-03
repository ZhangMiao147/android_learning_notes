# Flutter 环境搭建

## 1. 安装 Flutter

### 1.1. 获取 Flutter SDK

1. 去 flutter 官网下载其最新可用的安装包，官网地址：https://flutter.dev/sdk-archive/#macos

   如果官网拿不到，可以去 Flutter github 项目下去下载安装包，地址：https://github.com/flutter/flutter/releases。

2. 解压安装包到想安装的目录，如：

```
cd ~/development
unzip ~/Downloads/flutter_macos_2.10.0-stable.zip
```

3. 添加 flutter 相关工具到 path 中：

```
export PATH=`pwd`/flutter/bin:$PATH
```

此代码只能暂时针对当前命令行窗口设置 PATH 环境变量。

4. 运行 flutter doctor 命令

在Flutter命令行运行如下命令来查看是否还需要安装其它依赖，如果需要，安装它们：

```
flutter doctor
```

该命令检查你的环境并在命令行窗口中显示报告。Dart SDK 已经在打包在 Flutter SDK 里了，没有必要单独安装 Dart。 仔细检查命令行输出以获取可能需要安装的其他软件或进一步需要执行的任务。

### 1.2. 更新环境变量

将 Flutter 添加到 PATH 中，可以在任何终端会话中进行 flutter 命令。

对于所有终端会话永久修改此变量的步骤是和特定计算机系统相关的。通常，您会在打开新窗口时将设置环境变量的命令添加到执行的文件中。例如

1. 确定 Flutter SDK 的目录记为“FLUTTER_INSTALL_PATH”，将在步骤 3 中用到。

2. 打开(或创建) `$HOME/.bash_profile`。文件路径和文件名可能在你的电脑上不同.

```
sudo vi ~/.bash_profile
```

3. 添加以下路径:

```
export PATH=[FLUTTER_INSTALL_PATH]/flutter/bin:$PATH
```

例如：

```
export PATH=/User/ruming/Desktop/software/flutter/bin:$PATH
```

4. 运行 source $HOME/.bash_profile 刷新当前终端窗口。

```
source ~/.bash_profile
```

5. 验证 "flutter/bin" 是否已在 PATH 中：

```
echo $PATH
```

## 2. Android Studio 配置与使用

### 2.1. 安装 Flutter 和 Dart 插件

需要安装两个插件：

* Flutter 插件：支持 Flutter 开发工作流（运行、调试、热重载等）。
* Dart 插件：提供代码分析（输入代码时进行验证、代码补全等）。

安装步骤：

1. 启动 Android Studio。
2. 打开插件首选项（macOS：Preferences -> Plaugins）。
3. 查询 flutter 插件并点击 install。
4. 重启 Android Studio 后插件生效。

### 2.2. 创建 Flutter 应用

1. 选择 File -> New Flutter Project。
2. 选择 Flutter application 作为 project 类型，然后点击 Next。
3. 输入项目名称（如 flutter_app_test），然后点击 Next。
4. 点击 Finish。
5. 等待 Android Studio 安装 SDK 并创建项目。

在项目目录中，应用程序的代码位于 lib/main.dart。

如果提示设置 flutter SDK 地址，选择上面下载的 flutter SDK 的文件解压地址。

也可以在 Preferences -> Languages & Frameworks -> Flutter 里面修改 Flutter SDK 的地址。

### 2.3. 运行应用程序

1. 定位到 Android Studio 工具栏，如图所示：

![](https://book.flutterchina.club/assets/img/1-2.656e852b.png)

2. 在 **target selector** 中, 选择一个运行该应用的Android设备。如果没有列出可用，请选择 **Tools>Android>AVD Manager** 并在那里创建一个。
3. 在工具栏中点击 **Run图标**。
4. 如果一切正常, 您应该在您的设备或模拟器上会看到启动的应用程序，如图：

![](https://book.flutterchina.club/assets/img/1-3.801e91b2.png)



run 的时间会比较久，如果等了很久还不行，一直卡在 Running Gradle task 'assembleDebug'...，可以修改项目中 android/build.gradle 文件

![](https://img-blog.csdnimg.cn/20200408115541775.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ppbmdsaW5nZ2c=,size_16,color_FFFFFF,t_70)

改了之后再重新 run，再等等就可以了。

### 2.4. 体验热更新

Flutter 可以通过 热重载（hot reload） 实现快速的开发周期，热重载就是无需重启应用程序就能实时加载修改后的代码，并且不会丢失状态。简单的对代码进行更改，然后告诉 IDE 或命令行工具你需要重新加载（点击 reload 按钮），就会在设备或模拟器上看到更改。

1. 打开 lib/main.dart 文件
2. 将字符串 'You have pushed the button this many times:'  更改为  'You have clicked the button this many times:'。
3. 调用 Save (cmd-s / ctrl-s)，或者点击 热重载按钮 (带有闪电⚡️图标的按钮)。会立即在运行的应用程序中看到更新的字符串。

## 参考资料

1. [搭建Flutter开发环境](https://book.flutterchina.club/chapter1/install_flutter.html#_1-3-1-%E5%AE%89%E8%A3%85flutter)
2. [Flutter App Run卡在Running Gradle task 'assembleDebug'...](https://blog.csdn.net/jinglinggg/article/details/105383270)

