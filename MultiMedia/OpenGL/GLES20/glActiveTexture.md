## glActiveTexture

### 功能

　　激活纹理单元。

### 方法

```
void glActiveTexture(int texture)
```

#### 参数

**texture：**指定要激活的纹理单元，纹理单元的数量依赖于实现，但必须至少为 8。必须是 GL_TEXTUTEi 之一，其中 0 <= i < GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS ，初始值为 GL_TEXTURE0。

### 描述

　　被 glActiveTexture 激活的纹理将会影响其后续的纹理调用状态。

### 使用错误

　　如果纹理 ID (texture 的值)不是 GL_TEXTUREi （其中 i 的范围从 0 到 （GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS - 1）)中的一个值的话，就会得到一个状态错误：GL_INVALID_ENUM。








