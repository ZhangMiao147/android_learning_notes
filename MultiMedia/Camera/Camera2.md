# Camera2
　　Camera2 的出现给相机应用程序带来了巨大的改革，因为它的目的是为了给应用层提供更多的相机控制权限，从而构建出更高质量的相机应用程序。

## Camera2 框架概述

#### Pipeline
　　Camera2 的 API 模型被设计成一个 Pipeline(管道)，它按顺序处理每一帧的请求结果给客户端。

　　Pipeline 的官方工作流程图：

![](./pipeline的工作流程.png)

　　当拍摄两张不同尺寸的图片时，在拍摄的过程中闪光灯必须亮起来，整个拍摄流程如下：
1. 创建一个用于从 Pipeline 获取图片的 CaptureRequest。
2. 修改 CaptureRequest 的闪光灯配置，让闪光灯在拍照过程中亮起来。
3. 创建两个不同尺寸的 Surface 用于接收图片数据，并且将它们添加到 CaptureRequest 中。
4. 发送配置好的 CaptureRequest 到 Pipeline 中等待它返回拍照结果。

　　一个新的 CaptureRequest 会被放入一个被称作 Pending Request Queue 的队列中等待被执行，当 In-Flight Capture Queue 队列空闲的时候就会从 Pending Request Queue 获取若干个待处理的 CaptureRequest，并且根据每一个 CaptureRequest 的配置进行 Capture 操作。最后我们从不同尺寸的 Surface 中获取图片数据并且还会得到一个包含了很多与本次拍照相关的信息的 CaptureResult，流程结束。

#### Supported Handware Level
　　相机功能的强大是否和硬件息息相关，不同厂商对 Camera2 的支持程度不同，所以 Camera2 定义了一个叫做 Supported Hardware Level 的重要概念，其作用是将不同设备上的 Camera2 根据功能的支持情况划分成多个不同级别以便开发者能够了解当前设备上 Camera2 的支持情况。截止到 Android P 为止，从低到高一共有 LEGACY、LIMITED、FULL 和 LEVEL_3 四个级别：

1. LEGACY：向后箭筒的级别，处于改级别的设备意味着它只支持 Camera1 的功能，不具备任何 Camera2 高级特性。
2. LIMITED：除了支持 Camera1 的基本功能之外，还支持部分 Camera2 高级特性的级别。
3. FULL：支持所有 Camera2 的高级特性。
4. LEVEL_3：新增更多 Camera2 高级特性，例如 YUV 数据的后处理等。

#### Capture
　　相机的所有操作和参数配置最终都是服务于图像捕获，例如对焦是为了让某一区域的图像更加清晰，调节曝光补偿是为了调节图像的亮度。因此，在 Camera2 里面所有的相机操作和参数配置都被抽象成 Capture(捕获)，所以不要简单的把 Capture 直接理解成拍照，因为 Capture 操作可能仅仅是为了让预览画面更清晰而进行对焦而已。Camera1 的 setFlashMode()、setFocusMode() 和 takePicture() 都是通过 Capture 来实现的。

　　Capture 从执行方式上又被细分为【单次模式】、【多次模式】和【重复模式】三种：
* **单次模式（One-shot）：**指的是只执行一次的 Capture 操作，例如设置闪光灯
模式、对焦模式和拍一张照片等，多个一次性模型的 Capture 会进入队列按顺序执行。

* **多次模式（Burst）：**指的是连续多次执行指定的 Capture 操作，该模式和多次执行单次模式的最大区别是连续多次 Capture 期间不允许插入其他任何 Capture 操作，例如连续拍摄 100 张照片，在拍摄这 100 张图片期间任何新的 Capture 请求都会排队等待，知道拍完 100 张图片。多组多次模式的 Capture 会进入队列按顺序执行。

* **重复模式（Repeating）：**指的是不断重复执行指定的 Capture 操作，当有其他模式的 Capture 提交时会暂停该模式，转而执行其他模式的 Capture，当其他模式的 Capture 执行完毕后又会自动恢复继续执行该模式的 Capture，例如显示预览画面就是不断 Capture 获取每一帧画面。该模式的 Capture 是全局唯一的，也就是新提交的重复模式 Capture 会覆盖旧的重复模式 Capture。

　　例如：假设相机应用程序开启了预览，所以会提交一个重复模式的 Capture 用于不断获取预览画面，然后再提交一个单词模式的 Capture，接着又提交了一组连续三次的单词模式的 Capture，这些不同模式的 Capture 会按照下图所示被执行：

![](./Capture工作原理.png)

　　下面是几个重要的注意事项：
1.无论 Capture 以何种模式被提交，它们都是按顺序串行执行的，不存在并行执行的情况。
2.重复模式是一个比较特殊的模式，因为它会保留提交的 CaptureRequest 对象用于不断重复执行 Capture 操作，所以大多数情况下重复模式的 CaptureRqequest 和其他模式的 CaptureRequest 是独立的，这就会导致重复模式的参数和其他模式的参数会有一定的差异，例如重复模式不会配置 CaptureRequest.AF_TRIGGER_START，因为这会导致相机不断触发对焦的操作。
3.如果某一次的 Capture 没有配置预览的 Surface，例如拍照的时候，就会导致本次 Capture 不会降画面输出到预览的 Surface 上，进而导致预览画面卡顿的情况，所以大部分情况下我们会将预览的 Surface 添加到所有的 CaptureRequest 里。




#### Surface
　　Surface 是一块用于填充图像数据的内存空间，例如可以使用 SurfaceView 的 Surface 接收每一帧预览数据用于显示预览画面，也可以使用 ImageReader 的 Surface 接收 JPEG 或 YUV 数据，每一个 Surface 都可以有自己的尺寸和数据格式，可以从 CameraCharacteristics 获取某一个数据格式支持的尺寸列表。



#### Camera2

　　Camera2 架构图：

![](./Camera2流程示意图.jpg)

　　Camera2 引用了管道的概念将安卓设备和摄像头之间联通起来，系统向摄像头发送 Capture 请求，而摄像头会返回 CanmeraMetadata。这一切建立在一个叫作 CameraCaptureSession 的会话中。

　　管道（Pipeline），它按顺序处理每一帧的请求并返回请求结果给客户端。

　　Camera2 拍照流程图：
![](./Camera2拍照流程图.png)

## camera2 中比较重要的类及方法

#### 1. CameraManager
　　摄像头管理器，用于打开和关闭系统摄像头。

　　CameraManager 是一个负责查询和简历相机连接的系统服务，它的服务不多，列出几个 CameraManager 的关键功能：
1. 将相机信息封装到 CameraCharacteristics 中，并获取 CameraCharacteristics 实例的方式。
2. 根据指定的相机 ID 连接相机设备。
3. 提供将闪光灯设置成手电筒模式的快捷方式。

* getCameraIdList()
	返回当前设备中可用的相机列表。

* getCameraCharacteristics(String cameraId)
	根据摄像头 id 返回该摄像头的相关信息。

* openCamera(String cameraId,final CameraDevice.StateCallback callback,Handler handler);
	打开指定 cameraId 的相机。参数 callback 为相机打开时的回调，参数 handler 为 callback 被调用时所在的线程。

#### 2.CameraDevice
　　描述系统摄像头，类似于早期的 Camera。

　　CameraDevice 代表当前连接的相机设备，它的职责有以下四个：
1.根据指定的参数创建 CameraCaptureSession。
2.根据指定的模板创建 CaptureRequest。
3.关闭相机设备。
4.监听相机设备的状态，例如断开连接、开启成功和开启失败等。


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
　　描述摄像头的各种特性，类似于 Camera1 中的 CameraInfo 或者 Camera1 的 Parameters。通过 CameraManager 的 getCameraCharacteristics(String CameraId)方法来获取。

　　CameraCharacteristics 是一个只读的相机信息提供者，其内部携带大量的相机信息，包括代表相机朝向的 LENS_FACING；判断闪光灯是否可用的 FLASH_INFO_AVAILABLE；获取所有可用 AE 模式的 CONTROL_AE_AVAILABLE_MODES 等等。

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
　　CaptureRequest 是向 CameraCaptureSession 提交 Capture 请求时的信息载体，其内部包括了本次 Capture 的参数配置和接收图像数据的 Surface。CaptureRequest 可以配置的信息非常多，包括图像格式、图像分辨率、传感器控制、闪光灯控制、3A 控制等等，可以说绝大部分的相机参数都是通过 CaptureRequest 配置的。值得注意的是每一个 CaptureRequest 表示一帧画面的操作，这意味着可以精确控制每一帧的 Capture 操作。

　　描述了一次操作请求，拍照、预览等操作都需要先传入 CaptureRequest 参数，具体的参数控制也是通过 CameraRequest 的成员变量来设置。

* addTarget(Surface outputTarget)：
	给此次请求添加一个 Surface 对象作为图像的输出目标。

* set(Ket< T > key,T value)
	设置执行的参数值。

　　可以通过 CameraDevice.createCaptureRequest() 方法创建一个 CaptureRequest.Builder 对象，该方法只有一个参数 templateType 用于指定使用何种模板创建 CaptureRequest.Builder 对象。因为 CaptureRequest 可以配置的参数很多。Camera2 根据使用场景的不同，事先配置好了一些常用的参数模板：
* TEMPLATE_PREVIEW：适用于配置预览的模板。
* TEMPLATE_RECORD：适用于视频录制的模板。
* TEMPLATE_STILL_CAPTURE：适用于拍照的模板。
* TEMPLATE_VIDEO_SNAPSHOT：适用于在录制视频过程中支持拍照的模版。
* TEMPLATE_MANUAL：适用于希望自己手动配置大部分参数的模版。





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
　　CameraCaptureSession 实际上就是配置了目标 Surface 的 Pipeline 的实例，在使用相机功能之前必须先创建 CameraCaptureSession 实例。一个 CameraDevice 一次只能开启一个 CameraCaptureSession，绝大部分的相机操作都是通过向 CameraCaptureSession 提交一个 Capture 请求实现的，例如拍照、连拍、设置闪光灯模式、触摸对焦、显示预览画面等等。

　　当需要拍照、预览等功能时，需要先创建该类的实例，然后通过该实例里的方法进行控制（例如：拍照 capture()）。

* setRepeatingRequest(CaptureRequest request,CaptureCallback listener,Handler handler)
	根据传入的 CaptureRequest 对象开始一个无限循环的捕捉图像的请求。第二个参数 listener 为捕捉图像的回调，在回调中可以拿到捕捉到的图像信息。

* capture(CaptureRequest request,CaptureCallback listener,Handler handler)
	拍照。第二个参数为拍照的结果回调。

#### 6. CaptureResult
　　描述拍照完成后的结果。

　　CaptureResult 是每一次 Capture 操作的结果，里面包括了很多状态信息，包括闪光灯状态、对焦状态、时间戳等等。例如可以在拍照完成的是偶，通过 CaptureResult 获取本次拍照时的对焦状态和时间戳。需要注意的是，CaptureResult 并不包含任何图像数据，图像数据都是从 Surface 获取的。
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

## 一些只有 Camera2 才支持的高级特性
1. 在开始相机之前检查相机信息
	处于某些原因，可能需要先检查相机信息再决定是否开始相机，例如检查闪光灯是否可用。在 Camera1 上，无法在开启相机之前检查详细的相机信息，因为这些信息都是通过一个已经开始的相机实例提供的。在 Camera2 上，有了和相机实例完全剥离的 CameraCharateristics 实例专门提供相机信息，所以可以在不开启相机的前提下检查几乎所有的相机信息。

2. 在不开去预览的情况下拍照
	在 Camera1 上，开启预览是一个很重要的环节，因为只有在开启预览之后才能进行拍照，因此即使显示预览画面与实际业务需要相违背的时候，也不得不开启预览。而 Camera2 则不强制要求必须先开始预览才能拍照。

3. 一次拍摄多张不同格式和尺寸的图片
	在 Camera1 上，一次只能拍摄一张图片，更不用谈不同格式和尺寸的图片了。而 Camera2 则支持一次拍摄多张图片，甚至是多张格式和尺寸都不同的图片。

4. 控制曝光时间
	在暗环境下拍照的时候，如果能够适当延长曝光时间，就可以让图像画面的亮度得到提高。在 Camera2 上，可以在规定的曝光市场范围内配置拍照的曝光时间，从而实现拍摄长曝光图片，甚至可以延长每一帧预览画面的曝光时间让整个预览画面在暗环境下也能保证一定的亮度。

5. 连拍
	连拍 30 张图片这样的功能在 Camera2 出现之前只有系统相机才能做到（通过 OpenGL 截取预览画面的做法除外），也可能是出于这个原因，市面上的第三方相机无一例外都不支持连拍。有了 Camera2，完全可以让你的相机应用程序支持连拍功能，甚至是连续拍 30 张使用不同曝光时间的图片。

6. 灵活的 3A 控制
	3A （AF(自动曝光)、AE（自动对焦）、AWB（自动白平衡））的控制在 Camera2 上得到了最大化的放权，应用层可以根据业务需求灵活配置 3A 流程并且实时获取 3A 状态，而 Camera1 在 3A 的控制和监控当面提供的接口则要少了很多。例如可以在拍照前进行 AE 操作，并且监听本次拍照是否点亮闪光灯。

## 一些从 Camera1 迁移到 Camera2 的建议
1.Camera1 严格区分了预览和拍照两个流程，而 Camera2 则把这两个流程都抽象成了 Capture 行为，只不过一个是不断重复的 Capture，一个是一次性的 Capture 而已，所以建议不要带着过多的 Camera1 思维使用 Camera2，避免因为思维上的束缚而无法充分利用 Camera2 灵活的 API。
2.如同 Camera1 一样，Camera2 的一些 API 调用也会耗时，所以建议使用独立的线程执行所有的相机操作，尽量避免直接在主线程调用 Camera2 的 API，HandlerThread 是一个不错的选择。
3.Camera2 所有的相机操作都可以注册相关的回调接口，然后在不同的回调方法里写业务逻辑，这可能会让代码因为不够线性而错综复杂，建议可以尝试使用子线程的阻塞方式来尽可能地保证代码的线性执行。例如在子线程阻塞等到 CaptureResult，然后继续执行后续的操作，不是将代码拆分到 CaptureCallback.onCaptureComplete() 方法里面。
4.可以认为 Camera1 是 Camera2 的一个子集，也就是说 Camera2 能做的事情 Camera2 一定能做，反过来咋不一定行得通。
5.如果应用程序需要同时兼容 Camera1 和 Camera2，建议分开维护，因为 Camera1 的 API 设计很可能让 Camera2 灵活的 API 无法得到充分的发挥，另外将两个设计上完全不兼容发的东西搅和在一起带来的痛苦可能远大于其带来的便利性。
6.官方说 Camera2 的性能会更好，但是在较早期的一些机器上运行 Camera2 的性能并没有比 Camera1 好。
7.当设备的 Supported Handware Level 低于 FULL 的时候，建议还是使用 Camera1，因为 FULL 级别以下的 Camera2 能提供的功能几乎和 Camera1 一样，所以倒不如选择更加稳定的 Camera1。

## 使用
1.预览尺寸并不是直接从 CameraCharacteristics 获取的，而是先通过 SCALER_STREAM_CONFIGURATION_MAP 获取 StreamConfigurationMap.getOutputSizes() 方法获取尺寸列表，该方法会要求传递一个 Class 类型，然后根据这个类型返回对应的尺寸列表，如果给定的类型不支持，则返回 null，可以通过 StreamConfigurationMap.isOutputSupportedFor() 方法判断某一个类型是否被支持，常见的类型有：
* ImageReader：常用来拍照或接收 YUV 数据。
* MediaRecorder：常用来录制视频。
* MediaCodec：常用来录制视频。
* SurfaceHolder：常用开显示预览画面。
* SurfaceTexture：常用来显示预览画面。

2. 在配置尺寸方面，Camera2 和 Camera1 有着很大的不同，Camera1 是将所有的尺寸信息都设置给相机，而 Camera2 则是把尺寸信息设置给 Surface，例如接收预览画面的 SurfaceTexture，或者是接收拍照图片的 ImageReader，相机在输出图像数据的时候会根据 Surface 配置的 Buffer 大小输出对应尺寸的画面。

3. 在 Camera2 里，预览本质是不断重复执行的 Capture 操作，每一次 Capture 都会把预览画面输出到对应的 Surface 上，涉及的方法是 CameraCaptureSession.setRepeatingRequest()，该方法有三个参数：
* request：在不断重复执行 Capture 时使用的 CaptureRequest 对象。
* callback：监听每一次 Capture 状态的 CameraCaptureSession.CaptureCallback 对象，例如 onCaptureStarted() 意味着一次 Capture 的开始，而 onCaptureCompleted() 意味着一次 Capture 的结束。
* hander：用于执行 CameraCaptureSession.CaptureCallback 的 Handler 对象，可以是异步线程的 Handler，也可以是主线程的 Handler。

4. 预览比例的适配方式：
（1）根据预览比例修改 TextureView 的宽高，比如用户选择了 4:3 的预览比例，这个时候会选取 4:3 的预览尺寸并且把 TextureView 修改成 4:3 的比例，从而让画面不会变形。
（2）使用固定的预览比例，然后根据比例去选取适合的预览尺寸，例如固定 4:3 的比例，选择 1440x1080 的尺寸，并且把 TextureView 的宽高也设置成 4:3。
（3）固定 TextureView 的宽高，然后根据预览比例使用 TextureView.setTransform() 方法修改预览画面绘制在 TextureView 上的方式，从而让预览画面不变形，这跟 ImageView.setImageMatrix() 如出一辙。

　　简单来说，解决预览画面变形的问题，本质上就是解决画面和画布比例不一致的问题。

5. Camera2 不需要竞购任何预览画面方向的矫正，就可以正确显示画面，而 Camera1 则需要根据摄像头传感器的方向进行预览画面方向的矫正。其实，Camera2 也需要进行预览画面的矫正，只不过系统帮做了而已，当使用 TextureView 或者 SurfaceView 进行画面预览的时候，系统会根据【设备自然方向】、【摄像传感器方向】和【显示方向】自然矫正预览画面的方向，并且该矫正规则只适用于显示方向和设备自然方向一致的情况下。当使用一个 GLSurfaceView 显示预览画面或者使用 ImageReader 接收推向数据的时候，系统都不会进行画面的自动矫正。

　　在矫正画面方向的时候要同时考虑两个因素，即摄像头传感器方向和显示方向。

6. 如何拍摄单张图片：拍摄单张图片是最简单的拍照模式，它使用的就是单次模式的 Capture，会使用 ImageReader 创建一个接收照片的 Surface，并且把它添加到 CaptureRequest 里提交给相机进行拍照，最后通过 ImageReader 的回调获取 Image 对象，进而获取 JPEG 图像数据进行保存。

7. 计算出图片的矫正角度后，要通过 CaptureRequest.JPEG_ORIENTATION 配置这个角度，相机在拍照输出 JPEG 图像的时候会参考这个角度值从以下两种方式选一种进行图像方向矫正：
（1）直接对图像进行旋转，并且将 Exif 的 ORIENTATION 标签赋值为 0 。
（2）不对图像进行旋转，而是将旋转信息写入 Exif 的 ORIENTATION 标签里。

　　客户端在显示图片的时候一定要去检查 Exif 的 ORIENTATION 标签的值，并且根据这个值对图片进行对应角度的旋转才能保证图片显示方向是正确的。

####缩略图
　　相机在输出 JPEG 图片的时候，同时会根据我们通过 CaptureRequest.JPEG_THUMBNAIL_SIZE 配置的缩略图尺寸生成一张缩略图写入图片的 Exif 信息里。在设置缩略图尺寸之前，我们首先要获取相机支持哪些缩略图尺寸，与获取预览尺寸或照片尺寸列表方式不一样的是，缩略图尺寸列表是直接通过 CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES 获取的。

　　在获取图片缩略图的时候，我们不能总是假设图片一定会在 Exif 写入缩略图，当 Exif 里面没有缩略图数据的时候，要转而直接 Decode 原图获取缩略图，另外无论是原图还是缩略图，都要根据 Exif 的 ORIENTATION 角度进行矫正才能正确显示。

#### 设置定位信息
　　拍照的时候，通常都会在图片的 Exit 写入定位信息，可以通过 CaptureRequest.JPEG_GPS_LOCATION 配置定位信息。

#### 播放快门音效
　　在进行拍照之前，还需要配置拍照时播放的快门音效，因为 Camera2 和 Camera1 不一样，拍照时不会有任何声音，需要在适当的时候通过 MediaSoundPlayer 播放快门音效，通常情况是在 CaptureStateCallback.onCaptureStarted() 回调的时候播放快门音效。


#### 前置摄像头拍照的镜像问题
　　如果使用前置摄像头进行拍照，虽然照片的方向已经被矫正了，但是画面却是相反的。出现这个问题的原因是默认情况下相机不会对 LPEG 图像进行镜像操作，导致输出的原始画面是非镜像的。解决这个问题的一个办法是拿到 JPEG 数据之后再次对图像进行镜像操作，然后才保存图片。

#### 如何连续拍摄多张图片
　　在用户双击快门按钮的时候连续拍摄 10 张图片，其实现原理就是采用了多次模式的 Capture，所有的配置流程和拍摄单张图片一样，唯一的区别是使用 CameraCaptureSession.captureBurst() 进行拍照，改方法要求传递三个参数：
* requests：按顺序连续执行的 CaptureRequest 对象列表，每一个 CaptureRequest 对象都可以有自己的配置。
* listener：监听 Capture 状态的回调接口，需要注意的是有多少和 CaptureRequest 对象就会回调该接口多少次。
* handler：回调 Capture 状态监听接口的 Handler 对象。

#### 如何连拍
　　使用重复模式的Capture 就可以轻松实现连拍功能。

　　停止连拍有以下两种方式：
1. 调用 CameraCaptureSession.stopRepeating() 方法停止重复模式的 Capture，但是这会导致预览也停止。
2. 调用 CameraCaptureSesion.setReadtingRequest() 方法并且使用预览的 CaptureRequest 对象，停止输出照片。

#### 如何切换前后置摄像头
　　按照以下顺序进行操作就可以轻松实现前后置摄像头的切换：1.关闭当前摄像头；2.开启新的摄像头；3.创建新的 Session；4.开启预览。

#### 拍照模式的建议
1. 重复模式和多次模式都可以实现连拍功能，其中重读模式适合没有连拍上限的情况，而多次模式适合有连拍上限的情况。
2. 一个 CaptureRequest 可以添加多个 Surface，这就意味着可以同时拍摄多张照片。
3. 拍照获取 CaptureResult 和 Image 对象走的是两个不同的回调接口，灵活运用子线程的阻塞操作可以简化代码逻辑。



## 其他
　　因为打开相机和创建会话等都是耗时操作，所以需要启动一个 HandlerThread 在子线程中处理。

　　Camera2 在一些低端机器上会出现预览画面拉伸问题。

　　在 android 5.0 ，硬件兼容级别为 legacy 时，Camera2 输出的宽高比和 Camera Sensor 保持一致。也就是说我们设置的预览宽高 720 * 1280 并不比作用，所以出现了画面拉伸。

　　对于这个问题，网上的答案是如果遇到这种情况放弃使用 Camera2，使用旧的 Camera1。

　　
## 查阅资料
1.[Android Camera2 教程 · 第一章 · 概览](https://www.jianshu.com/p/9a2e66916fcb) - 已阅读
2.[Android Camera2 教程 · 第二章 · 开关相机](https://www.jianshu.com/p/df3c8683bb90) - 已阅读
3.[Android Camera2 教程 · 第三章 · 预览](https://www.jianshu.com/p/067889611ae7) - 已阅读
4.[Android Camera2 教程 · 第四章 · 拍照](https://www.jianshu.com/p/2ae0a737c686)
5.[Android:Camera2开发详解(上)：实现预览、拍照、保存照片等功能](https://www.jianshu.com/p/0ea5e201260f) - 已阅读
6.[Android:Camera2开发详解(下)：实现人脸检测功能并实时显示人脸框](https://www.jianshu.com/p/331af6dc2772) - 已阅读