学习资料地址：[https://blog.csdn.net/laverfever/article/details/19076663](https://blog.csdn.net/laverfever/article/details/19076663)
官方 API：[https://docs.unity3d.com/ScriptReference/Texture2D.html](https://docs.unity3d.com/ScriptReference/Texture2D.html)
用途：在 Unity3D 中可以使用脚本创建 texture2D 贴图，并对像素进行操作。
## 1. 构造函数
```csharp
Texture2D(int width, int height);
Texture2D(int width, int height, TextureFormat format, bool mipmap);

Texture2D(int width, int height, TextureFormat format, bool mipmap, bool linear);
```
## 2. 方法
### 2.1. 对像素的操作
| 方法 | 作用 | 描述 |
| --- | --- | --- |
| Color GetPixel(int x,int y) | 获取像素颜色 | 
 |
| Color GetPixelBilinear(float u,float v) | 获取正交化坐标系下像素颜色 | 

多用于处理得知多边形UV坐标时对像素的处理。 |
| Color[] GetPixels(int miplevel=0) | 获取一个区块的像素颜色 | 获取以x,y为起始点，大小为width,height的一个区块，返回的是一个数组，数组内颜色的点顺序为从左至右，从下至上 |
| Color32[] GetPixels32(int miplevel=0) | 获取(指定mipmap level级别)的整张贴图的像素颜色(使用 Color32 格式) | 读取速度比反复使用 getPixel 读取速度快。 |
| void SetPixel(int x,int y,Color color) | 设置像素颜色 |  |
| void SetPixels(Color[] colors,int miplevel=0) | 设置(指定 mipmap level级别)的整张贴图的像素颜色 | 设置指定 mipmap level 下的整张贴图颜色 |
| void SetPixels32(Color32[] colors,int miplevel=0) | 设置(指定 mipmap level级别)的整张贴图的像素颜色(使用Color32格式) |  |

### 2.2. 对贴图的操作
| 方法 | 作用 | 描述 |
| --- | --- | --- |
| void Apply(bool updateMipmaps=true,bool makeNoLongerReadable=false) | 当对贴图的像素操作后必须调用的函数，使操作生效 |  |
| void Compress(bool highQuality) | 将贴图压缩成 DXT 格式 |  |
| byte[] EncodeToPNG() | 将贴图转码为 PNG 格式 |  |
| bool LoadImage(byte[] data) | 加载一张贴图 | 可以加载的格式为：JPG,PNG |
| Rect[] PackTextures(Texture2D[] textures,int padding,int maximumAtlasSize=2048,bool makeNoLongerReadable=false) | 将多张贴图打包到一张图集中 |  |
| void ReadPixels(Rect source,int destX,int destY,bool recalculateMipMaps=true) | 将屏幕色读入到贴图 | Rect source 可以用来创建需要截取的屏幕区域；
destX，destY 表明了渲染到贴图的起始点，(0,0)点为屏幕的左下角；
readPixels 函数主要可以配合 camera.OnPostRender 进行截图及 RenderToTexture 操作。 |
| bool Resize(int widht,int height,TextureFormat format,bool hasMipMap) | 重新定义贴图 |  |

