# adb 命令

## 常用命令

* 杀死进程
  
  adb shell am kill xxx 后台杀死某个进程，可以用来模拟内存不足，activity 被回收。
  
  adb shell am kill com.test.test:name
  

* 清理应用缓存
  
  adb shell pm clear com.test.test
  
* 使用 scheme 打开应用

   adb shell am start -d "market://com.test.test/main"

* 使用类名启动应用

   adb shell am start -n 包名/Activity类名

* 查看已装应用列表

   adb shell pm list package

* 查看任务栈
  
   adb shell dumpsys activity activities 

* 查看 apk 版本号
  
   adb shell pm dump com.test.test | grep "version"


* 打开/关闭网络
  
   adb shell svc wifi disable
  
   adb shell svc wifi enable


* 卸载应用
  
   adb uninstall com.test.test

* 截图

  adb shell screencap -p /sdcard/screen.png 截图保存到 sdcard

  adb pull /sdcard/screen.png 将图片导出

  adb shell rm /sdcard/screen.png 删除 sdcard 中的图片


* 重启设备
  
   adb reboot 重启设备
   
* 拷贝文件到设备上

   adb push aaa/contacts_app.db /sdcard/     把文件 contacts_app.db 拷贝到手机 sdcard 上

* 拷贝设备上的文件到电脑上

   adb pull sdcard/``1222073679``.png 拷贝文件夹命令，如把log文件夹拷贝到电脑当前目录。

## adb push 与 adb install 的区别

**adb push和adb install是apk包安装的两种方法，用法如下：**

1. adb push XXX.apk /path你想推送的路径
2. adb install XXX.apk

显而易见，两者的区别是：

1. adb push可以自定义推送路径，adb install 只能安装在/data/app文件下。
2. 其次，adb push 是将 apk 包复制到路径下面，重启后 /system/app 和 /data/app 下的 apk 包都会自动安装。
   adb install 是将 apk 安装到手机里，流程是：
   复制 APK 安装包到 data/app 目录下，解压并扫描安装包，把 dex 文件( Dalvik 字节码)保存到 dalvik-cache 目录，并 data/data 目录下创建对应的应用数据目录（ so 文件也在./包名/lib下，或者/data/app-lib/包名）。
   另外，install 复制 APK 安装包到 data/app 目录下的过程中，会将 apk 名改为 包名-1.apk，数字为 android 接收到的版本号。
   其中对于 /system 文件夹比较容易碰到的问题是报 read only file 的错误，哪怕 /system 已经被你改成了 777 的权限，解决的方法是：1.adb shell 2.su 3.mount -o remount rw /system 重新挂载一下/system文件系统。