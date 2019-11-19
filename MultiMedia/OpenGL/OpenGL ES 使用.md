# OpenGL ES 使用
　　

　　


## 判断是否支持 OpenGL ES 2
```

public static boolean isSupportEs2(Context context){
    //检查是够支持 2.0
    ActivityManager activityConfigurationInfo = actiivtyManager.getDeviceConfigurationInfo();
    if (activityManager != null){
        ConfigurationInfo  deviceConfigurationInfo = activityManager.getDeviceConfigurationInfo()；
        int reqGlEsVersion = deviceConfigurationInfo.reqGlEsVersion;
        return reqGlEsVersion >= GLES_VERSION_2 || 
        (Build.VERSION_SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
        && (Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknow")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK build for x86")
        ));
    } else {
        return false;
    }
}
```

## 创建 GLSurfaceView
```
//创建一个 GLSurfaceView
glSurfaceView = new GLSurfaceView(this);
glSurfaceView.setEGLContextClientVersion(2);
//设置自己的 Render，Render 内进行图形的绘制
glSurfaceView.setRenderer(new TriangleShapeRender(this));
isRenderSet = true;
setContextView(glSurfaceView);
```

　　在 Activity 对应的生命周期内，来调用 GLSurfaceView 的方法：

```
    @Override
    protected void onPause() {
        super.onPause();
        if (isRenderSet) {
            glSurfaceView.onPause();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRenderSet) {
            glSurfaceView.onResume();
        }

    }
```

　　因为 Android 中的 GLSurfaceView 的操作，都是在 GLThread 中进行，所以生命周期的毁掉也都在 GLThread 线程中，所有 OpenGL 的操作也都需要在该线程中。 

　　


## 查阅资料
1. [Android OpenGL ES(一)-开始描绘一个平面三角形](https://www.jianshu.com/p/4a014afde409)
2. [Android OpenGL ES(二)-正交投影](https://www.jianshu.com/p/7e0471100605)
3. [Android OpenGL ES(三)-平面图形](https://www.jianshu.com/p/320980800358)
4. [Android OpenGL ES(四)-为平面图添加滤镜](https://www.jianshu.com/p/7bcdb68823cb)
5. [Android OpenGL ES(五)-结合相机进行预览/录制及添加滤镜](https://www.jianshu.com/p/b36b6e17e818)
6. [Android OpenGL ES(六) - 将输入源换成视频](https://www.jianshu.com/p/9c616616f95a)
7. [Android OpenGL ES(七) - 生成抖音照片电影](https://www.jianshu.com/p/dac18f4ee3e9)
8. [Android OpenGL ES(八) - 简单实现绿幕抠图](https://www.jianshu.com/p/159744875386)
