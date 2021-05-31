# 观摩：Session 模式与 Proxy-Stub 模式的搭配

# 1. Session 设计模式：以 CameraService 服务为例

* 在 Android 的 C++ 层里，有个 CameraService 系统服务。 
* 在 MediaServer 进程初始化照相机服务。 
* 此进程的 main() 函数代码：

```c
int main(int argc, char** argv) {
sp<ProcessState> proc(ProcessState::self());
sp<IServiceManager> sm = defaultServiceManager();
// .........
CameraService::instantiate();
// .........
IPCThreadState::self()->joinThreadPool();
}
```

* 这个 CameraService::instantiate() 的代码是：

```c
void CameraService::instantiate() {
defaultServiceManager()->addService(
String16("media.camera"), new CameraService());
}
```

* 首先创建 CameraService 对象,然后委托 SM(ServiceManager) 登录到 BD(Binder Driver) 里。
* 当一个 Camera 应用程序启动时，会建立一个 CameraClient 来与 CameraService 衔接。

![](image/Cameraclient.png)

* 当很多个应用程序来衔接 CameraService 时，该如何接待呢?

  指派助理服务访客。

## 采用 Session 设计模式

* 步骤一、CameraClient 先调用 CameraService，这个动作通称为：取得连线 (Connection)。

![](image/connect.png)

* 此时，CameraService 创建一个 Client 对象，并将 Client 的 IBinder 接口回传给 CameraClient。

![](image/session.png)

* 步骤二、CameraClient 就调用 Client，要求 Client 与 CameraService 通信，间接调用到 CameraService 的服务。

![](image/步骤2.png)

* 每一个 Connection 都有一个私有的 Session 对象。

![](image/私有session.png)

* 当 CemeraClient 透过 SM 绑定了 CameraService 之后，Camera 就可调用 CameraService 的 connect() 函数来建立一个连线。
* 这时，CameraClient 把自己的 < ICameraClient > 接口传递给 CameraService。 
* 例如下述的 Android 程序源码：

```c
sp<ICamera> CameraService::connect(const sp<ICameraClient>& 
cameraClient)
{
Mutex::Autolock lock(mLock);
sp<Client> client;
if (mClient != 0) {
// 這個cameraClient已經調用過connect()了
// 先前已經給它一個Client對象(mClient)了
// 就先將mClient轉型態
sp<Client> currentClient = mClient.promote();
// 這currentClient就是mClient
if (currentClient != 0) {
sp<ICameraClient> currentCameraClient( 
currentClient->getCameraClient());
// 比對一下mClient所記錄的cameraClient與
// 本次來訪的cameraClient是否同一個
if (cameraClient->asBinder() 
== currentCameraClient->asBinder()) {
return currentClient;
// 如果同一個，回傳先前的Client對象
} else return client;
} else mClient.clear();
}
// 這個cameraClient是第一次來訪
// 建立一個新Client對象
client = new Client(this, cameraClient, 
IPCThreadState::self()->getCallingPid());
mClient = client;
return client;
}
```

* 这时，CameraService 就在自己进程里诞生了一个 Client 的对象。
* 并且，将此 Client 对象的 \<ICamera\> 接口回传给 CameraClient。

```c
sp<ICamera> CameraService::connect( const 
sp<ICameraClient>& cameraClient)
{
// ………
}
```

* 那么，\<ICamera\> 接口和 \<ICameraClient\> 接口，从那里来的呢? 定义在那里呢?

  答案是：来自 Proxy-Stub 设计模式。

  也就是 BnInterface\<T\> 和 BpInterface\<T\> 模版所建立的 Proxy 和 Stub 类。

![](image/ps.png)

# 2. Session 设计模式：以 VM 的 JNIEnv 对象为例

* 每一个 Connection 都有一个私有的 Session 对象；每一个线程进入 VM 都有一个私有 JNIEnv 对象。
* 每一个线程第一次进入 VM 调用本地函数时，VM 会替它诞生一个相对映的 JNIEnv 对象。
* Java 层的线程调用 C 层的本地函数时，该线程必然经过 VM，且 VM 一定替它诞生相对映的 JNIEnv 对象。
* 线程不共享 JNIEnv 对象，成为 “ 单线程 ” 开发，不必烦恼线程安全问题，让本地函数的撰写单纯化了。
* 简而言之，在预设情形下，在某个线程第一次进入 VM 去执行 JNI 层 C 函数时，VM 就会替它诞生专属的 JNIEnv 对象。只要该线程还存在着，就会一直保留它所专属的 JNIEnv 对象。

![](image/jnienv.png)

# 3. Session 设计模式：典型架构

* 在这 Client 与 Server 之间透过接口互相沟通；而且多个  Client 可同时与 Server 建立连结，取得 Server 的服务。所以，Client 与 Server 之间是 N:1 的关系。
* 基于这个架构，可以建立 Client 与 Server 之间的各种连结 (Connection) 和沟通 (Communication)。 
* 例如，Client 端的浏览器 (Browser) 会与 Server 建立连结，然后开起一段交谈 (Session)。
* 首先，Client 透过某项机制 ( 例如，Android 的 ServiceManager) 来绑定 (bind) 后台的 Server。
* 绑定 (bind) 之后，Client 就能呼叫 Server 的 getSession() 函数，准备开启一段对话。
* 此时，Server 就诞生一个 Session 对象，来作为这项连结的专属对象，可以记载对话过程所产生的信息。

![](image/getsession.png)

* 把 ISession 口回传给 Client。
* Client 掌握了 ISession 接口，就能透过 ISession 接口来呼叫 Session 的函数，然后由 Session 来与 Server 沟通。如下图：

![](image/isession.png)

* 在上图的呼叫时，Client 可以将自己的 IClient 接口传递给 Session 或 Server。让 Session 或 Server 就能透过 IClient 接口来呼叫 Client 的函数。于是建立了双向连结的架构，如下图：

![](image/双向.png)

* 由于 Client 拥有 ISession 接口，就能透过 Session 来与 Server 沟通。
* 同时，Server 拥有 IClient 接口，就能使用 IClient 接口来与 Client 沟通。

* 例如，Android 的 SurfaceFlinger 系统服务

![](image/sf.png)

# 4. 复习：Proxy-Stub 模式

* Proxy-Stub 模式的主要用途在于封装接口，以便提供更好的新接口。
* 也因而，它成为 < 挟天子以令诸侯 > 的主要架构设计模块。
* Android 提供了 BpInterface\<T\> 和 BnInterface\<T\> 两个模板，来协助创建 Proxy 和 Stub 两个类。
* 例如，BnInterface\<T\> 模板定义如下：

```c
template<typename INTERFACE>
class BnInterface :public INTERFACE, public BBinder {
public:
virtual sp<IInterface> queryLocalInterface(const 
String16& _descriptor);
virtual String16 getInterfaceDescriptor() const;
protected:
virtual IBinder* onAsBinder();
};
```

* 基于这个模板，并定义接口如下：

```c
class IMyService :public IInterface {
public:
DECLARE_META_INTERFACE(MyService);
virtual void sv1(…) = 0;
virtual void sv1(…) = 0;
virtual void sv1(…) = 0;
};
```

* 此时可使用 BnInterafce\<T> 模板来产生 BnInterafce\<IMyService\> 类别。如下：

```c
BnInterface<IMyService>
```

* 它一方面继承了 Binder 框架基类来得到 IBinder 接口。同时。也继承了 IMyService 接口所定义的 sv1(), sv2() 和 sv3() 函数。

* 基于这个模板产生的类别，就可衍生出 Stub 类别，如下：

```c
class BnMyService : public BnInterface<IMyService>
{
//………..
}
```

* 基于这个 Stub 类别 ( 即 BnMyService )，我们只要撰写 MyNativeService 类别，它来继承上述的 BnMyService 类别即可，如下定义：

```c
class MyNativeService : public BnMyService
{
//………..
}
```

* 如下图所示：

![](image/stub.png)

* 这两个类合起来，扮演 Stub 的角色

![](image/合stub.png)

* 进而，使用 BpInterface\<T\> 模板，来生成 Proxy 类。

![](image/proxy.png)

# 5. Proxy-Stub 设计模式：以 CameraService 为例

* 在 Android 里，CameraService 采用了 Session 模式，如下图：

![](image/cssession.png)

* 加上 proxy-stub 模式

![](image/csps.png)

* 如果使用 BpInterface\<T> 模板，这 Proxy 角色包含了两个类：

```
Proxy 模板类：BpInterface<ICamera>
Proxy 类：BpCamera
```

![](image/csp.png)

* 在上图的呼叫时，CameraClient 可以将自己的 IBinder 接口传递给 CameraService。 
* 这让 Client 能调用 CameraClient 的 IBinder 接口，如下图：

![](image/cs回调.png)

* 加上 Proxy-Stub 模式

![](image/cs加上.png)

![](image/cc.png)

* 如果使用模板，这 Proxy 和 Stub 角色各包含了两个类：

```
Proxy 模板类：BpInterface<ICameraClient>
Proxy 类：BpCameraClient
Stub 模板类：BnInterface<ICameraClient>
Stub 类：BnCameraClient
```

![](image/bn.png)

* 以上说明了 CameraService 服务幕后，使用 BpInterface\<T> 和 BnInterface\<T> 模板来生成 Proxy 和 Stub 两个类。
* 还有很多接口，例如，CameraService 也提供了 \<ICameraService> 接口。

![](image/camera.png)

# 6. SurfaceFlinger 服务的 Session 模式

## 简介 SurfaceFlinger 系统服务

* SurfaceFlinger 是一个掌管屏幕影像显示的 Native 服务。 
* 它与 AMS(ActivityManagerService) 和 WMS(WindowManagerService) 紧密合作，提供 App 关于屏幕显示的服务。

![](image/surface.png)

* SurfaceFlinger 系统服务的主要任务是：管理 GPU、管理 FrameBuffer、配置 Surface、以及合成 Surface 等。
* FrameBuffer 里的 Surface 合成影像，会透过硬件厂商提供的驱动模块 ( 如下图里的Gralloc.BROADPLATFORM.so ) 来对映到实际的硬件显示设备上。

![](image/gralloc.png)

## 6.2. 熟悉基本流程：只用 IBinder 接口

* SurfaceFlinger 與 SM(ServiceManager) 都是 Native 服務。

![](image/native.png)

* 当 WMS(WindowManagerService) 需要 Surface 时，就会诞生一个 SurfaceComposerClient 对象， 如下图：

![](image/scl.png)

* SurfaceComposerClient 对象调用 SM 的 getService() 函数，请求 SM 协助绑定 SurfaceFlinger 核心服务，如下图：

![](image/getservice.png)

* SM 绑定了服务，就将 SurfaceFlinger 的 IBinder 接口回传给 SurfaceComposerClient。
* SurfaceComposerClient 透过 IBinder 接口，向 SurfaceFlinger 请求建立一条连线。

![](image/连线.png)

* 此 SurfaceComposerClient 对象与 SurfaceFlinger 对象之间是 N:1 关系。于是，SurfaceFlinger 核心服务就诞生一个 BClient 对象。
* 创造出 SurfaceComposerClient 对象与 BClient 对象之间的 1:1 关系。并回传其 IBinder 接口 ( 建立了连线 ) 。
* SurfaceComposerClient 与 BClient 对象之间是 1:1 关系。
* 有了连线，SurfaceComposerClient 就透过 BClient 的 IBinder 接口，要求配置一个绘图 Surface。

![](image/bclient.png)

* 于是，SurfaceFlinger 就创建了一個 Suraface 對象，並將其 IBinder 接口回傳給 SurfaceComposerClient。
* 此 SurfaceComposerClient 对象与 Surface 对象之间是 1:N 关系。
* 于是，SurfaceComposerClient 就诞生一个 SurfaceControl 对象；创造出 SurfaceControl 对象与 Surface 对象之间的 1:1 关系。如下图：

![](image/关系.png)

# 7. SurfaceFlinger 服务的 Proxy-Stub 模式

### 使用模版：创造新接口

* 与上一节里的 CameraService 一样，我们也能使用BpInterface\<T\> 和 BnInterafce\<T\> 模板来提供 SurfaceFlinger 的新接口给 SurfaceComposerClient 使用。
* 把焦点放在 SurfaceFlinger 的 IBinder 接口上。

### SurfaceFlinger 新接口：ISurfaceComposer

![](image/sf新接口.png)

* 然后，针对 SurfaceFlinger 的 IBinder 接口，使用 BpInterface\<T\> 和 BnInterafce\<T\> 模板来包装 IBinder 接口，以便提供出 ISurfaceComposer 新接口。
* 如果将幕后的模板类也画出来的话，就如下图所示：

![](image/幕后.png)

### BClient 新接口：ISurfaceFlingerClient

* 还可以应用相同的技巧来实践并提供 BClient 的新接口：ISurfaceFlingerClient。

![](image/isfc.png)

* 然后，针对 BClient 的 IBinder 接口，使用BpInterface\<T> 和 BnInterafce\<T> 模板来包装 IBinder 接口，以便提供出 ISurfaceFlingerClient 新接口。

![](image/bpsfc.png)

### Surface 新接口：ISurface

* 还可以应用相同的技巧来实践并提供 Surface 的新接口：ISurface。

![](image/ibsurface.png)

* 然后，针对 Surface 的 IBinder 接口，使用 BpInterface\<T> 和 BnInterafce\<T> 模板来包装 IBinder 接口，以便提供出 ISurface 新接口。

![](image/bnsurface.png)

### Summary

* 理解了 BpInterface\<T> 和 BnInterafce\<T> 模板的用途，以及其包装 IBinder 接口的意义，就很容易看懂 Androoid 系统服务的代码了。
* 例如，现在你就很容易看懂下图 ( 韩超老师书里的图 ) 了。

![](image/韩超.png)

