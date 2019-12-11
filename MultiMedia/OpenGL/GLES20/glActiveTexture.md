## glActiveTexture

### 功能

　　选择活动纹理单元。

### 方法

```
void glActiveTexture(int texture)
```

#### 参数

**texture：**指定要激活的纹理单元，纹理单元的数量依赖于实现，但必须至少为 8。必须是 GL_TEXTUTEi 之一，其中 i 的范围从 0 到 GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS - 1 。初始值为 GL_TEXTURE0。

### 描述

　　glActiveTexture 选择的纹理单元将会影响其后续的纹理调用状态。实现支持的纹理单元数依赖于实现，但必须至少为 8。

### 错误

　　如果纹理 ID (texture 的值)不是 GL_TEXTUREi （其中 i 的范围从 0 到 （GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS - 1）)之一的话，就会得到 **GL_INVALID_ENUM** 错误。

### 原文地址

[glActiveTexture](https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glActiveTexture.xml)






