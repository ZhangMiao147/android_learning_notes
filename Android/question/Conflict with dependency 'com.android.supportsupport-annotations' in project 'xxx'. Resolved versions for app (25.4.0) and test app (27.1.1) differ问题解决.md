# Conflict with dependency 'com.android.support:support-annotations' in project ':xxx'. Resolved versions for app (25.4.0) and test app (27.1.1) differ 问题解决

## 问题描述
```
Warning:Conflict with dependency 'com.android.support:support-annotations' in project ':xxx'. Resolved versions for app (25.4.0) and test app (27.1.1) differ. See http://g.co/androidstudio/app-test-app-conflict for details.
```
　　问题的意思就是说不能解决 25.4.0 和 测试 27.1.1 应用之间的差异，就是说 com.android.support:support-annotations 版本冲突了。

## 问题解决
　　在 build.gradle 添加
```
android {
	...
}
configurations.all {
	resolutionStrategy.force 'com.android.support:support-annotations:27.1.1'
}
dependencies {
	...
}
```
　　resolutionStrategy.force 是强制依赖的意思。

## 查看文章
　　https://www.jianshu.com/p/42fb70aa1602
