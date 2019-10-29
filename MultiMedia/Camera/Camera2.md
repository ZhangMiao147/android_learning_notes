# Camera2

## Camera2 框架概述
　　Camera2 架构图：

![](./Camera2流程示意图.jpg)

　　Camera2 引用了管道的概念将安卓设备和摄像头之间联通起来，系统向摄像头发送 Capture 请求，而摄像头会返回 CanmeraMetadata。这一切建立在一个叫作 CameraCaptureSession 的会话中。

　　Camera2 拍照流程图：
![](./Camera2拍照流程图.png)

## camera2 中比较重要的类及方法

#### 1. CameraManager
　　摄像头管理器，用于打开和关闭系统摄像头。

* getCameraIdList()
	返回当前设备中可用的相机列表。

* getCameraCharacteristics(String cameraId)
	根据摄像头 id 返回该摄像头的相关信息。

* openCamera(String cameraId,final CameraDevice.StateCallback callback,Handler handler);
	打开指定 cameraId 的相机。参数 callback 为相机打开时的回调，参数 handler 为 callback 被调用时所在的线程。

#### 2.CameraDevice
　　描述系统摄像头，类似于早期的 Camera。

* createCaptureRequest(int templateType)
	创建一个新的 Capture 请求。参数 templateType 代表了请求类型，请求类型一共分为六种，分别是：
1. TEMPLATE_PREVIEW：创建预览的请求。
2. TEMPLATE_STILL_CAPTURE：创建一个适合于静态图像捕获的请求，图像质量优先于帧速率。
3. TEMPLATE_RECORD：创建视频录制的请求。
4. TEMPLATE_VIDEO_SNAPSHOT：创建视频录制时截屏的请求。
5. TEMPLATE_ZERO_SHUTTER_LAG：创建一个适用于零快门延迟的请求。在不影响预览帧率的情况下最大化图像质量。
6. TEMPLATE_MANUAL：创建一个基本捕获请求，这种请求中所有的自动控制都是禁用的（自动曝光、自动白平衡、自动焦点）。

* createCaptureSession(List< Surface > outputs,CameraCaptureSession.StateCallback callback,Handler handler)
	创建 CaptureSession 会话。第一个参数 outputs 是一个 List 数组，相机会把捕捉到的图片数据传递给该参数中的 Surface。第二个参数 StateCallback 是创建会话的状态回调。第三个参数描述了 StateCallback 被调用时所在的线程。

#### 3.CameraCharacteristics
　　描述摄像头的各种特性，类似于 Camera1 中的 CameraInfo。通过 CameraManager 的 getCameraCharacteristics(String CameraId)方法来获取。

* get(Key< T > key)
	通过制定的 key 获取相应的相机参数。

　　常用的 key 值有：
1. CameraCharacteristics.LENS_FACING：
	获取摄像头方向。前置摄像头（LENS_FACING_FRONT）或 后置摄像头（LENS_FACING_BACK）。
2. CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL：
	获取当前设备支持的相机特性。
3. CameraCharacteristics.SENSOR_ORIENTATION：
	获取摄像头方向。
4. CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP：
	获取 StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸。
5. CameraCharacteristics.FLASH_INFO_AVAILABLE：
	是否支持闪光灯。
6. CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT：
	同时检测到人脸的数量。
7. CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES：
	相机支持的人脸检测模式。

#### 4. CaptureRequest
　　描述了一次操作请求，拍照、预览等操作都需要先传入 CaptureRequest 参数，具体的参数控制也是通过 CameraRequest 的成员变量来设置。

* addTarget(Surface outputTarget)：
	给此次请求添加一个 Surface 对象作为图像的输出目标。

* set(Ket< T > key,T value)
	设置执行的参数值。
```
//自动对焦
captureRequestBuilder.set(Cap)

```
//自动对焦
captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

//闪光灯
captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

//根据摄像头方向对保存的照片进行旋转，使其为“自然方向”
captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,mCameraSensorOrientation);

//人脸检测模式
captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,CameraCharacteris.STATISTICS_FACE_DETECT_MODE_SIMPLE);

```

#### 5.CameraCaptureSession
　　当需要拍照、预览等功能时，需要先创建该类的实例，然后通过该实例里的方法进行控制（例如：拍照 capture()）。

* setRepeatingRequest(CaptureRequest request,CaptureCallback listener,Handler handler)
	根据传入的 CaptureRequest 对象开始一个无限循环的捕捉图像的请求。第二个参数 listener 为捕捉图像的回调，在回调中可以拿到捕捉到的图像信息。

* capture(CaptureRequest request,CaptureCallback listener,Handler handler)
	拍照。第二个参数为拍照的结果回调。

#### 6. CaptureResult
　　描述拍照完成后的结果。

#### 7.ImageReader
　　用于接收拍照结果和访问拍摄照片的图像数据。

　　得到一个 ImagerEADER 对象的方法为 newInstance(int width,int height,int format,int maxImages)。前两个参数是保存图片的宽高，第三个参数为保存图片的格式，第四个参数代表用户可以同时访问到的最大图片数量。

　　注意：这个参数应该根据具体业务需求尽可能的小，因为它的数值越大意味着需要消耗的内存就越高。

* acquireNextImage()
	得到 ImageReader 图像队列中的下一张图片，返回值是一个 Image 对象。

#### 8.Image
　　一个完成的图片缓存。

* getPlanes()
	获取该图像的像素平面数组。这个数组的大小跟图片的格式有关，如 JPEG 格式数组大小为 1。

#### 9.Plane
　　图像数据的单色平面。

* getBuffer()
	获取包含帧数据的 ByteBuffer。通过这个 ByteBuffer 就可以吧图片保存下来。

## 其他
　　因为打开相机和创建会话等都是耗时操作，所以需要启动一个 HandlerThread 在子线程中处理。

　　Camera2 在一些低端机器上会出现预览画面拉伸问题。

　　在 android 5.0 ，硬件兼容级别为 legacy 时，Camera2 输出的宽高比和 Camera Sensor 保持一致。也就是说我们设置的预览宽高 720 * 1280 并不比作用，所以出现了画面拉伸。

　　对于这个问题，网上的答案是如果遇到这种情况放弃使用 Camera2，使用旧的 Camera1。

　　
## 查阅资料
1.[Android Camera2 教程 · 第一章 · 概览](https://www.jianshu.com/p/9a2e66916fcb)
2.[Android Camera2 教程 · 第二章 · 开关相机](https://www.jianshu.com/p/df3c8683bb90)
3.[Android Camera2 教程 · 第三章 · 预览](https://www.jianshu.com/p/067889611ae7)
4.[Android Camera2 教程 · 第四章 · 拍照](https://www.jianshu.com/p/2ae0a737c686)
5.[Android:Camera2开发详解(上)：实现预览、拍照、保存照片等功能](https://www.jianshu.com/p/0ea5e201260f)
6.[Android:Camera2开发详解(下)：实现人脸检测功能并实时显示人脸框](https://www.jianshu.com/p/331af6dc2772)