# Android 10.0 版本新特性
北京时间2019年3月14日Google正式对外发布Android Q Beta 1及预览版SDK，这意味着安卓开发者们又即将迎来一年一度的新版本适配工作了。Android Q 为开发者们带来了许多新功能，如折叠屏增强项、新网络连接 API、全新的媒体解码器、摄像头新功能、NNAPI 扩展、Vulkan 1.1 图形支持等等。2019/4  Beta2版本发布 ，5月份将会正式发布，本文将带大家对Android Q变更和新特性的详细解读。
 还有一点值得Android开发者注意的事件，**华为应用市场在3.26日发布了一则公告，要求华为市场上的App在2019年5月底前完成Android Q版本适配工作并自检通过，针对未适配或在Android Q版本体验欠佳的应用，华为应用市场将在Android Q版本机型上采取下架、不推荐更新或屏蔽策略，**这一点还需要所有开发者注意了。
 同时华为4.2号的公告也需要开发者注意了：
 自2019年5月1日起，华为应用市场新上架应用应基于Android 8.0 (API等级26，即targetSdkVersion大于等于26)及以上开发。自2019年8月1日起，现有应用的更新应基于Android 8.0 (API等级26，即targetSdkVersion大于等于26)及以上开发。
 2019年5月1日后，未达到要求的新应用，华为应用市场将拒绝上架。2019年8月1日后，未达到要求的现有应用，华为应用市场将拒绝更新。如您的应用API等级小于26，请尽快完成应用的升级改造。

本文将从三个角度介绍Android Q的重要部分的适配问题，也是大家开发适配过程中大概率会遇到的问题：

1. Android Q 行为变更：所有应用 （不管targetSdk是多少，对所有跑在Q设备上的应用均有影响）
2. Android Q 行为变更：以 Android Q 为目标平台的应用（targetSDK == Q 才有影响）
3. 项目升级遇到的问题

## 一、Android Q 行为变更：所有应用

官方文档将这一部分内容独立于Q 行为变更：所有应用来介绍，是因为这一部分内容庞大且重要，其中最大的更新就是用户隐私权限变更。具体变更的权限如下：

![img](https:////upload-images.jianshu.io/upload_images/6653666-40320d72e96e1355.png?imageMogr2/auto-orient/strip|imageView2/2/w/967/format/webp)

图片.png


 从后台启动Activity权限和无线扫描权限两种权限的变更影响较少 ，本文就不做介绍，详细可以查看请查阅官方文档：[https://developer.android.com/preview/privacy/background-activity-starts](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.com%2Fpreview%2Fprivacy%2Fbackground-activity-starts)



从后台启动Activity权限变更仅针对与用户毫无交互就启动一个Activity的情况，(比如微信登陆授权)。

本文将重点讲述存储权限,定位权限和设备标识符三种权限的变更与适配。

#### 1.用户存储权限的变更

Android Q 在外部存储设备中为每个应用提供了一个“隔离存储沙盒”（例如 /sdcard）。任何其他应用都无法直接访问您应用的沙盒文件。由于文件是您应用的私有文件，因此您不再需要任何权限即可在外部存储设备中访问和保存自己的文件。此变更可让您更轻松地保证用户文件的隐私性，并有助于减少应用所需的权限数量。

沙盒，简单而言就是应用专属文件夹，并且访问这个文件夹无需权限。谷歌官方推荐应用在沙盒内存储文件的地址为Context.getExternalFilesDir()下的文件夹。比如要存储一张图片,则应放在Context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)中。

以下将按访问的目标文件的地址介绍如何适配。

1. 访问自己文件：Q中用更精细的媒体特定权限替换并取消了 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE权限，并且无需特定权限，应用即可访问自己沙盒中的文件。
2. 访问系统媒体文件：Q中引入了一个新定义媒体文件的共享集合，如果要访问沙盒外的媒体共享文件，比如照片，音乐，视频等，需要申请新的媒体权限:READ_MEDIA_IMAGES,READ_MEDIA_VIDEO,READ_MEDIA_AUDIO,申请方法同原来的存储权限。
3. 访问系统下载文件：对于系统下载文件夹的访问，暂时没做限制，但是，要访问其中其他应用的文件，必须允许用户使用系统的文件选择器应用来选择文件。
4. 访问其他应用沙盒文件：如果你的应用需要使用其他应用在沙盒内创建的文件，请点击使用其他应用的文件,本文不做介绍。
    **所以请判断当应用运行在Q平台上时，取消对READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE两个权限的申请。并替换为新的媒体特定权限。**
    当满足以下每个条件时，将开启兼容模式，即不开启Q设备中的存储权限改动：
   1. 应用targetSDK<=P。
   2. 应用安装在从 Android P 升级到 Android Q 的设备上。
       **但是当应用重新安装(更新)时，不会重新开启兼容模式，存储权限改动将生效。**
       所以按官方文档所说，无论targetSDK是否为Q，必须对应用进行存储权限改动的适配。

在测试中，当targetSDK<=P,在Q Beat1版上申请两个旧权限时会自动改成申请三个新权限，不会影响应用正常使用，但当targetSDK==Q时，申请旧权限将失败并影响应用正常使用。

#### 2.用户的定位权限的变更

为了让用户更好地控制应用对位置信息的访问权限，Android Q 引入了新的位置权限 ACCESS_BACKGROUND_LOCATION。

与现有的 ACCESS_FINE_LOCATION 和 ACCESS_COARSE_LOCATION 权限不同，新权限仅会影响应用在后台运行时对位置信息的访问权。除非应用的某个 Activity 可见或应用正在运行前台服务，否则应用将被视为在后台运行。

与iOS系统一样，Q中也加入了后台位置权限ACCESS_BACKGROUND_LOCATION，如果应用需要在后台时也获得用户位置(比如滴滴)，就需要动态申请ACCESS_BACKGROUND_LOCATION权限。

当然如果不需要的话，应用就无需任何改动，且谷歌会按照应用的targetSDK作出不同处理：
 **targetSDK <= P 应用如果请求了ACCESS_FINE_LOCATION 或 ACCESS_COARSE_LOCATION权限，Q设备会自动帮你申请ACCESS_BACKGROUND_LOCATION权限。**

#### 3.设备唯一标识符的变更

从 Android Q 开始，应用必须具有 READ_PRIVILEGED_PHONE_STATE 签名权限才能访问设备的不可重置标识符（包含 IMEI 和序列号）。
 许多用例不需要不可重置的设备标识符。如果您的应用没有该权限，但您仍尝试查询标识符的相关信息。会返回空值或报错。
 设备唯一标识符需要特别注意，原来的READ_PHONE_STATE权限已经不能获得IMEI和序列号，如果想在Q设备上通过使用以下代码获取设备的ID



```css
((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()
```

则执行以上代码会返回空值(targetSDK<=P)或者报错(targetSDK==Q)。且官方所说的READ_PRIVILEGED_PHONE_STATE权限只提供给系统app，所以这个方法行不通了。

谷歌官方给予了设备唯一ID最佳做法，但是此方法给出的ID可变，可以按照具体需求具体解决。本文给出一个不变和基本不重复的UUID方法。



```tsx
public static String getUUID() {

  String serial = null;

  String m_szDevIDShort = "35" +
        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

        Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

        Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

        Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

        Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

        Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

        Build.USER.length() % 10; //13 位

  try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        serial = android.os.Build.getSerial();
      } else {
        serial = Build.SERIAL;
      }
      //API>=9 使用serial号
      return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    } catch (Exception exception) {
    //serial需要一个初始化
    serial = "serial"; // 随便一个初始化
  }
    //使用硬件信息拼凑出来的15位号码
    return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
}
```

虽然由于唯一标识符权限的更改会导致android.os.Build.getSerial()返回unknown,但是由于m_szDevIDShort是由硬件信息拼出来的，所以仍然保证了UUID的唯一性和持久性。

经测试上述方法完全相同的手机有可能重复，网上还有其他方案比如androidID,但是androidID可能由于机型原因返回null，所以个人任务两种方法半斤八两。设备ID的获取一个版本比一个版本艰难，如果有好的方法欢迎指出。

#### 4.关于minSDK警告

在 Android Q 中，当用户首次运行以 Android 6.0（API 级别 23）以下的版本为目标平台的任何应用时，Android平台会向用户发出警告。如果此应用要求用户授予权限，则系统会先向用户提供调整应用权限的机会，然后才会允许此应用首次运行。谷歌要求运行在Q设备上的应用targetSDK>=23,不然会向用户发出警告。

## 二、Android Q 行为变更：以 Android Q 为目标平台的应用

**非 SDK 接口限制**
 非SDK接口限制在Android P中就已提出，但是在Q中，**被限制的接口的分类有较大变化。**

**非SDK接口介绍**
 为了确保应用稳定性和兼容性，Android 平台开始限制您的应用可在 Android 9（API 级别 28）中使用哪些非 SDK 接口。Android Q 包含更新后的受限非 SDK 接口列表（基于与 Android 开发者之间的协作以及最新的内部测试）。

非SDK接口限制就是某些SDK中的私用方法，如private方法，你通过Java反射等方法获取并调用了。那么这些调用将在target>=P或target>=Q的设备上被限制使用，当你使用了这些方法后，会报错:

![img](https:////upload-images.jianshu.io/upload_images/6653666-07e18f41f8161cb7.png?imageMogr2/auto-orient/strip|imageView2/2/w/594/format/webp)

图片.png


**非SDK接口查找**
 如果您不确定自己的应用是否使用了非 SDK 接口，则可以测试该应用进行确认。当你调用了非SDK接口时，会有类似Accessing hidden XXX的日志:





```php
Accessing hidden field Landroid/os/Message;->flags:I (light greylist, JNI)
```

但是一个大项目到底哪里使用了这些方法，靠review代码和看日志肯定是不现实的，谷歌官方也提供了官方检查器veridex用来检测一个apk中哪里使用了非SDK接口。veridex下载。[https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat](https://links.jianshu.com/go?to=https%3A%2F%2Fandroid.googlesource.com%2Fplatform%2Fprebuilts%2Fruntime%2F%2B%2Fmaster%2Fappcompat)，其中有windows,linux和mac版本，对应下载即可。下载解压后命令行cd到veridex目录下使用./[appcompat.sh](https://links.jianshu.com/go?to=http%3A%2F%2Fappcompat.sh) --dex-file=Q.apk即可自动扫描。Q.apk为包的绝对路径，如果包与veridex在相同目录下直接输入包文件名即可。扫描结果分为两部分，一部分为被调用的非SDK接口的位置，另一部分为非SDK接口数量统计。
 **非SDK接口适配**
 如果您的应用依赖于非 SDK 接口，则应该开始计划迁移到 SDK 替代方案。如果您无法为应用中的某项功能找到使用非 SDK 接口的替代方案，则应该请求新的公共 API。

官方要求targetSDK>=P的应用不使用这些方法，并寻找其他的公共API去替代这些非SDK接口，如果找不到，则可以向谷歌申请，请求一个新的公共API [https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces#feature-request](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.com%2Fdistribute%2Fbest-practices%2Fdevelop%2Frestrictions-non-sdk-interfaces%23feature-request) (一般不需要)。

就我个人扫描并定位的结果来看，项目中使用非SDK接口大概率有以下两种情况：

1. 在自定义View的过程中为了方便，使用反射修改某个参数。
2. 三方SDK中使用了非SDK接口(这种情况比较多)。

第一种是好解决的，毕竟是我们自己写的代码。第二种就头疼了，只能更新到最新的三方SDK版本，或者提工单、换库(也是整个适配过程中工作量最庞大的部分)。

## 三、Android项目升级遇到的问题

**模拟器X86，项目中SO库为v7**

1. 找到so库源代码，编译成x86
2. 如果so库只是某个功能点使用，对APP整体没大影响，就可以屏蔽特定so库功能或略过测试
3. 如果so库是项目核心库必须加载，也可使用腾讯云测，上面有谷歌亲儿子Q版本。腾讯云测有adb远程连接调试功能(我没成功过)。adb连不上也没关系，直接安装就行，云测上也可以直接看日志。
4. 至于inter的houdini我尝试研究过，理论上能安装在x86模拟器上让它编译v7的so库，但是由于关于houdini的介绍比较少也比较旧，建议大家时间不充裕的话就别研究了。
    **Requires development platform Q but this is a release platform.**
    由于目前Q是preview版，所以targetSDK==Q 的应用只能在Q设备上跑。
    INSTALL_FAILED_INVALID_APK: Failed to extract native libraries, res=-2
    这个错误是由于打包压缩so库时造成的，具体原因可见：[https://issuetracker.google.com/issues/37045367](https://links.jianshu.com/go?to=https%3A%2F%2Fissuetracker.google.com%2Fissues%2F37045367)



```bash
在AndroidManifest.xml的application节点下加入android:extractNativeLibs="true"
```

可能有人加了上面代码还是不行，在app/build.gradle中的defaultConfig节点下加入



```bash
packagingOptions{ 
      doNotStrip "/armeabi/.so" doNotStrip "/armeabi-v7a/.so" doNotStrip "/x86/.so" }
```

**Didn't find class “org.apache.http.client.methods.HttpPost"**
 在AndroidManifest.xml的application节点下加入:



```dart
<uses-library android:name="org.apache.http.legacy" android:required="false"/>
```

**如果你的项目没有适配过android O或P，那么你需要注意：**

1. android O的读取已安装应用权限（对应用内自动更新有影响）
2. android P的默认禁止访问http的API

这两个版本的适配问题本文就不做详述，大家可以查看网上详细的介绍。

以上就是Android Q（10.0）版本新特性和兼容性适配，中间会遇到各种各样的坑，还是建议详细查看Google的官方文档。

本文的参考文档：

Android Q Beta开发者文档链接：：[https://developer.android.com/preview](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.com%2Fpreview)

Android Q Beta镜像下载链接：[https://developer.android.com/preview/download](https://links.jianshu.com/go?to=https%3A%2F%2Fdeveloper.android.com%2Fpreview%2Fdownload)

Android Q Beta 发布 blog：[https://android-developers.googleblog.com/2019/03/introducing-android-q-beta.html](https://links.jianshu.com/go?to=https%3A%2F%2Fandroid-developers.googleblog.com%2F2019%2F03%2Fintroducing-android-q-beta.html)

非SDK接口：[https://juejin.im/post/5afe50eef265da0b70262463](https://links.jianshu.com/go?to=https%3A%2F%2Fjuejin.im%2Fpost%2F5afe50eef265da0b70262463)



作者：安卓搬砖小曾
链接：https://www.jianshu.com/p/130918ed8b2f
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。


## 参考文章
1. [Android Q（10.0）版本新特性和兼容性适配](https://www.jianshu.com/p/130918ed8b2f)

