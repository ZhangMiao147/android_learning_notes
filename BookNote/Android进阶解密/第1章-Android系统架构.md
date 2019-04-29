# 第 1 章 Android 系统架构

## 1.1 Android 系统架构
　　Android 系统架构分为五层，从上到下依次是应用层、应用框架层、系统运行库层、硬件抽象层和 Linux 内核层。

1. 应用层（System Apps）

　　系统内置的应用程序以及非系统级的应用程序都属于应用层，负责与用户进行直接交互，通常都是用 Java 进行开发的。

2. 应用框架层（Java API Framework）

　　应用框架层为开发人员提供了开发应用程序所需要的 API，我们平常开发应用程序都是调用这一层所提供的 API，当然也包括系统应用。这一层是由 Java 代码编写的，可以称为 Java Framework。

| 名称 | 功能描述 |
|--------|--------|
| Activity Manager(活动管理器) | 管理各个应用程序生命周期，以及常用的导航回退功能 |
| Location Manager(位置管理器) | 提供地理位置及定位功能服务 |
| Package Manager(包管理器) | 管理所有安装在 Android 系统中的应用程序 |
| Notification Manager(通知管理器) | 使得应用程序可以在状态栏中显示自定义的提示信息 |
| Resource Manager(资源管理器) | 提供应用程序使用的各种非代码资源，如本地化字符串、图片、布局文件、颜色文件等 |
| Telephony Manager(电话管理器) | 管理所有的移动设备功能 |
| Window Manager(窗口管理器) | 管理所有开启的窗口程序 |
| Content Provider(内容提供者) | 使得不同应用程序之间可以共享数据 |
| View System(视图系统) | 构建应用程序的基本组件 |

3. 系统运行库层（Native）

　　系统运行库层分为两部分，分别是 C/C+ + 程序库和 Android 运行时库。

1） C/C+ + 程序库

　　C/C+ + 程序库能被 Android 系统中的不同组件所使用，并通过应用程序框架为开发者提供服务。

主要的 C/C+ + 程序表

| 名称 | 功能描述 |
|--------|--------|
| OpenGL ES | 3D 绘图函数库 |
| Libc | 从 BSD 继承来的标准 C 系统函数库，专门为基于嵌入式 Linux 的设备定制 |
| Media Framework | 多媒体库，支持多种常用的音频、视频格式录制和回放 |
| SQLite | 轻型的关系型数据库引擎 |
| SGL | 底层的 2D 图形渲染引擎 |
| SSL | 安全套接层，是一种为网络通信提供安全及数据完整性的安全协议 |
| FreeType | 可移植的字体引擎，它提供统一的接口来访问多种字体格式文件 |

2）Android 运行时库

　　运行时库又分为核心库和 ART （Android 5.0 系统之后，Dalvik 虚拟机被 ART 取代）。核心库提供了 Java 语言核心库的大多数功能，这样开发者可以使用 Java 语言来编写 Android 应用。与 JVM 相比，Dalvik 虚拟机（DVM）是专门为移动设备定制的，允许在有限的内存中同时运行多个虚拟机的实例，并且每一个 Dalvik 应用作为一个独立的 Linux 进程执行。独立的进程可以防止在虚拟机崩溃的时候所有程序都被关闭。而替代 DVM 的 ART 的机制与 DVM 不同，DVM 中的应用每次运行时，自己吗都需要通过即时编译器（Just In Time,JIT）转换为机器码，这会使得应用的运行效率降低。而在 ART 中，系统在安装应用时会进行一次预编译（Android Of Time,AOT），将字节码预先编译成机器码并存储在本地，这样应用每次运行时就不需要执行编译了，运行效率也大大提高。

4. 硬件抽象层（HAL）

　　硬件抽象层是位于操作系统内核与硬件电路之间的接口层，其目的在于将硬件抽象化，为了保护硬件厂商的知识产权，它隐藏了特定平台的硬件接口细节，为操作系统提供虚拟硬件平台，使其具有硬件无惯性，可在多种平台上进行移植。从软硬件测试的角度来看，软硬件的测试工作都可分别基于硬件抽象层来完成，使得软硬件测试工作的并行进行称为可能。通俗来讲，就是将控制硬件的动作放在硬件抽象层中。

5. Linux 内核层（Linux Kernal）

　　Android 的核心系统服务基于 Linux 内核，在此基础上添加了部分 Android 专用的驱动。系统的安全性、内存管理、进程管理、网络协议栈和驱动模型等都依赖于该内核。

## 1.2 Android 系统源码目录
　　可以访问 http://androidxref.com 来阅读系统源码。推荐使用百度网盘地址 http://pan.baidu.com/s/lngsZa 进行下载。

#### 1.2.1 整体结构
　　如果是编译后的源码目录，会多一个 out 文件夹，用来存储编译产生的文件。

Android 8.0.0 的系统根目录结构说明

| Android 源码根目录 | 描述 |
|--------|--------|
| art | 全新的 ART 运行环境 |
| bionic | 系统 C 库 |
| bootable | 启动引导相关代码 |
| build | 存放系统编译规则及 generic 等基础开发包配置 |
| cts | Android 兼容性测试套件标准 |
| dalvik | Dalvik 虚拟机 |
| developers | 开发者目录 |
| development | 与应用程序开发相关 |
| device | 设备相关配置 |
| docs | 参考文档目录 |
| external | 开源模组相关文件 |
| frameworks | 应用程序框架，Android 系统核心部分，由 Java 和 C++ 编写 |
| hardware | 主要是硬件抽象层的代码 |
| libcore | 核心库相关文件 |
| libnativehelper | 动态库，实现 JNI 库的基础 |
| out | 编译完成后代码在此目录输出 |
| pdk | Plug Development Kit 的缩写，本地开发套件 |
| platform_testing | 平台测试 |
| prebuilts | X86 和 ARM 架构下预编译的一些资源 |
| sdk | SDK 和模拟器 |
| packages | 应用程序包 |
| system | 底层文件系统库、应用和组件 |
| toolchain | 工具链文件 |
| tools | 工具文件 |
| makefile | 全局 Makefile 文件，用来定义编译规则 |

#### 1.2.2 应用层部分
　　应用层位于整个 Android 系统的最上层，开发者开发的应用程序以及系统内置的应用程序都在应用层。源码根目录中的 packages 目录对应着系统应用层。

packages 目录结构

| packages 目录 | 描述 |
|--------|--------|
| apps | 核心应用程序 |
| experimental | 第三方应用程序 |
| inputmethods | 输入法目录 |
| providers | 内容提供者目录 |
| screensavers | 屏幕保护 |
| services | 通信服务 |
| wallpapers | 墙纸 |

#### 1.2.3 应用框架部分
　　应用框架层是系统的核心部分，一方面向上提供接口给应用层调用，另一方面向下与 C/C++ 程序库及硬件抽象层等进行衔接。应用框架层的主要实现代码在 framework/base 和 frameworks/av 目录下。

　　frameworks/base 目录

| frameworks/base 目录 | 描述 | frameworks/base 目录 | 描述 |
|--------|--------|--------|--------|
| api | 定义 API | cmds | 重要命令：am、app_proce 等 |
| core | 核心库 | data | 字体和声音等数据文件 |
| docs | 文档 | graphics | 与图形图像相关 |
| include | 头文件 | keystore | 与数据签名证书相关 |
| libs | 库 | location | 地理位置相关库 |
| media | 多媒体相关库 | native | 本地库 |
| nfc-extras | 与 NFC 相关 | obex | 蓝牙传输 |
| opengl | 2D/3D 图形 API | packages | 设置、TTS、VPN 程序 |
| sax | XML 解析器 | services | 系统服务 |
| telephony | 电话通信管理 | test-runner | 测试工具相关 |
| tests | 与测试相关 | tools | 工具 |
| vr | 与 VR 相关 | wifi | Wi-Fi 无限网络 |

#### 1.2.4 C/C++ 程序库部分
　　系统运行库层（Native）中的 C/C++ 程序库的类型繁多，功能强大，C/C++ 程序库并不完全在一个目录中。

　　C/C++ 程序库所在的目录位置

| 目录位置 | 描述 |
|--------|--------|
| bionic | Google 开发的系统 C 库，以 BSD 许可形式开源 |
| frameworks/av/media | 系统媒体库 |
| frameworks/native/opengl | 第三方图形渲染库 |
| frameworks/native/services/surfaceflinger | 图形显示库，主要负责图形的渲染、叠加和绘制等功能 |
| external/sqlite | 轻量级关系型数据库 SQLite 的 C++ 实现 |

　　Android 运行时库的代码在 art/目录中，硬件抽象层的代码在 hardware/目录中，这是手机厂商改动最大的部分，根据手机终端锁采用的硬件平台不同会有不同的实现。

## 1.3 源码阅读
　　系统源码的阅读有很多种方式，总的来说分为两种：一种是在线阅读；另一种是下载源码到本地用软件工具阅读。

#### 1.3.1 在线阅读
　　Android 在线阅读源码的网站有很多，比如 http://www.grepcode.com、http://androidxref.com、http://www.androidos.cn 等，推荐使用 http://androidxref.com 进行在线阅读，网站提供了 Android 1.6 到 Android 8.0.0 的源码。

#### 1.3.2 使用 Source Insight
　　本地阅读源码可以采用 Android Studio、Eclipse、Sublime 和 Source Insight 等软件，这里推荐使用 Source Insight。

## 1.4 本章小结












