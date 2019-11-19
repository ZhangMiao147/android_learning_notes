# SurfaceTexture
　　SurfaceTexture 是 Surface 和 OpenGL ES ( GLES ) 纹理的组合。SurfaceTexture 用于提供输出到 GLES 纹理的 Surface。

　　SurfaceTexture 包含一个应用是其使用方的 BufferQueue。当生产方将新的缓冲区排入队列时，onFrameAvailable() 回调会通知应用。然后，应用调用 updateTexImage()，这会释放之前占有的缓冲区，从队列中获取新缓冲区并执行 EGL 调用，从而使 GLES 可将此缓冲区作为外部纹理使用。

## 
　　

　　




## 查阅资料
1.[Android 视频展示控件之 SurfaceView、GLSurfaceView、SurfaceTexture、TextureView 对比总结](https://www.cnblogs.com/renhui/p/8258391.html)
2.[SurfaceTexture](https://source.android.google.cn/devices/graphics/arch-st)

