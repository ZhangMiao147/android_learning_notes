
#### 启用或者禁用顶点属性数组
　　调用 GLES20.glEnableAttribArray 和 GLES20.glDisableVertexAttribArray 传入参数 index。

```
GLES20.glEnableVertexAttribArray(glHPosition);
GLES20.glDisableVertexAttribArray(glHCoordinate);
```
　　如果启用，那么当 GLES20.glDrawArrays 或者 GLES20.glDraeElements 被调用时，顶点属性数组会被使用。

GLES20.glEnableVertexAttribArray()
　　允许使用顶点坐标数组。
