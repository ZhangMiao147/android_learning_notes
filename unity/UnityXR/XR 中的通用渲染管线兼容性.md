# 5.XR 中的通用渲染管线兼容性

学习资料地址：[XR 中的通用渲染管线兼容性 - Unity 手册](https://docs.unity3d.com/cn/2021.2/Manual/xr-render-pipeline-compatibility.html)

通用渲染管线 (URP) 中对 XR 功能的支持因 URP 包版本而异。

Unity 2020.2 在通用渲染管线中支持以下 AR 和 VR 功能：

| **功能**                               | **XR 支持** |
| ------------------------------------ | --------- |
| 后期处理效果：泛光                            | 是         |
| 后期处理效果：运动模糊                          | 是         |
| 后期处理效果：镜头失真                          | 否         |
| 后期处理效果：景深                            | 是         |
| 后期处理效果：色调映射                          | 是         |
| 其他后期处理效果（颜色调整等）                      | 是         |
| GI（全局光照）                             | 是         |
| HDR                                  | 是         |
| MSAA                                 | 是         |
| 物理摄像机                                | 否         |
| CopyColor / ColorDepth               | 是         |
| 多显示                                  | 否         |
| 摄像机堆叠                                | 是         |
| 级联阴影                                 | 是         |
| sRGB                                 | 是         |
| Skybox                               | 是         |
| 雾效                                   | 是         |
| 公告牌                                  | 是         |
| Shader Graph                         | 是 (1)     |
| 粒子                                   | 是         |
| Terrain                              | 是         |
| 2D UI（Canvas Renderer，Text Mesh Pro） | 是         |
| URP 调试（Scene 视图模式，帧调试）               | 是 (2)     |
