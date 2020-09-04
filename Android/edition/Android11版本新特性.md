# Android 11 版本新特性
前不久，谷歌发布了Android 11 的预览版，从开发者预览版我们可以了解到，Android 11增强了对用户隐私的保护，提供了很多吸引用户的新特性，并且可以更好地支持可折叠设备和 Vulkan 扩展程序等等。

谷歌官方的资料显示，Android 11 开发者预览版计划从 2020 年 2 月启动，到向 AOSP 和 OEM 提供最终的公开版本时结束，最终版本预计将于 2020 年第 3 季度发布，详细情况可以查看[Android 11时间轴、里程碑和更新](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.google.cn%2Fpreview%2Foverview)
 下图是官方发布的Android 11时间轴。

> ![img](https:////upload-images.jianshu.io/upload_images/22796403-165dd7edac53de95.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

目前，Android 11 提供的预览版本可以仅适用于 Pixel 系列机型，主要包含 Google Pixel 4/4XL，Pixel 3a/3a XL，Pixel 3/3 XL 和 Pixel 2/2 XL。开发者预览版是仅面向开发者的早期基准 build。它们不适合尝鲜者或消费者的日常使用，因此我们仅通过手动下载和刷机提供这些版本。当然，如果您没有可运行 Android 11 的硬件设备，那么可以使用 Android 模拟器进行开发和测试，同样可以抢先体验开发者预览版的新特性。

2020年2月19日，Google发布了Android 11的第一个开发者预览版，让我们提前了解了它的最新功能和新logo，从而拉开了今年新版本的序幕。3月18日，Google发布了第二个开发者预览版，增加了一些新功能，修复了前两个版本中的许多bug。与Android 10相比，Android 11的年增长率似乎相当平缓。开发者预览版2带来了一些小的UI更改，但也不算太大的变化。下面将深入探讨一些最新的功能，其中一些功能是针对Android处理5G连接方式的改进，包括对更多显示类型的支持，以及更强大的权限控制。

# 新特性

## 短信更新改进

在Google的第一个Android 11开发者预览版中，我们可以看到Google在改善Android消息体验方面的提升。

首先是聊天泡泡。与Facebook多年来在Android上提供的Messenger应用程序类似，Android 11优化了短信功能，提供更加友好的交互。同时，为了确保用户能尽快收到对方的消息，Android 11在通知阴影（Notification Shade）中引入了一个专门的对话部分，它将提供对用户正在进行的任何对话的即时访问。这一更新将有助于短信消息从其他通知中脱颖而出。

![img](https:////upload-images.jianshu.io/upload_images/22796403-cd76ffb012e47b3e.png?imageMogr2/auto-orient/strip|imageView2/2/w/800/format/webp)

## 隐私和权限

回顾Android 10，其中的一大亮点就是改进了对应用程序权限“仅这一次”的处理。Android 10给了用户更多的应用程序控制权以及他们可以访问的内容，Android 11则提出一个更加出色的新功能。

现在，Android 11 新增了关于位置、麦克风和摄像头的一次性权限许可。也就是说，获得一次性权限许可的 APP 在下次使用时，依然要询问用户获取授权。下次再使用该应用程序并希望使用该权限时，还会再重新授予它访问权限。

![img](https:////upload-images.jianshu.io/upload_images/22796403-1baaf22194bd2b77.png?imageMogr2/auto-orient/strip|imageView2/2/w/800/format/webp)

## 内置屏幕录制

此前，Google一直在努力向Android添加内置屏幕录制器，屏幕录像曾在 Android 10 的早期版本中出现，但随后被删除了，在Developer Preview 2添加了一个录屏工具，附带一个抛光的用户界面和一个用于录制音频和显示录制内容的开关。而Android 11几乎确认了该功能将在今年推出，您可以在第一个Android 11预览版中使用屏幕录像机。

![img](https:////upload-images.jianshu.io/upload_images/22796403-d6433434df4e18c2.png?imageMogr2/auto-orient/strip|imageView2/2/w/800/format/webp)

## 适配不同设备

Google在Android 8.0版本首次展现了折叠屏技术，近年来，折叠手机已经非常的流行，尤其是Galaxy Z Flip和Motorola RAZR等具有“翻盖手机”折叠式设计的设备，Android 11开发者预览版2增加了“铰链角度传感器API”，因此应用程序可以轻松检测到这些折叠手机的铰链。有了这些信息，开发人员就可以调整他们的应用程序，使其围绕铰链工作，并因此创造独特的体验。

同时，智能手机显示屏的另一个重大升级与更快的刷新率有关。对于手机来说，以90Hz或120Hz刷新的屏幕已经不少见了，Android 11允许开发者更好地利用这些强大的屏幕。在Android 11 DP2中引入，开发人员可以选择应用程序的刷新率。如果开发人员确定他们的应用程序在90Hz或60Hz下看起来最好，他们可以做出这个决定，并让手机的显示屏在使用该应用程序时相应地更改其刷新率。

![img](https:////upload-images.jianshu.io/upload_images/22796403-191ca9701bf12d1e.png?imageMogr2/auto-orient/strip|imageView2/2/w/800/format/webp)

## 网络优化

从2019年开始，5G开始普及，可以遇见的是，在2020年将会有越来越多的设备通过5G连接到无线网络。为了让这个过程尽可能顺利，Android 11增加了一个非常重要的“动态计量API”，这在字面上听上去可能不会太令人兴奋，但它本质上允许手机充分利用5G带来的所有进步。如果API检测到用户连接到无限5G信号，将可以访问最高质量的视频和图片，5G的潜力是非常巨大的，这个API可以确保用户充分利用可用的网速。

![img](https:////upload-images.jianshu.io/upload_images/22796403-b1998b8f008852c0.png?imageMogr2/auto-orient/strip|imageView2/2/w/800/format/webp)

目前，Android11还处于开发阶段，距离正是发布还有一段时间，如果想要体验Android11的心疼小，那么可以使用Google的Pixel 系列机型，或者模拟器来获取更新，每次更新均包括 SDK 工具、系统映像、模拟器、API 参考和 API 差异。



作者：程序媛饭冰冰
链接：https://www.jianshu.com/p/5cbbaa53d5a9
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。


## 参考文章
1. [Android 11 新特性，仔细看看哪些是你不知道的？](https://www.jianshu.com/p/5cbbaa53d5a9)

