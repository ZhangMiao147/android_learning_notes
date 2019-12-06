# OpenGL 相关知识

* OpenGL ES 官方文档翻译
* OpenGL ES 使用
* OpenGL ES 透视投影和正交投影

* Matrix 类

## 其他

* ByteByffer.allocateDiect

  ```java
      ByteBuffer a = ByteBuffer.allocateDirect(32);
      a.order(ByteOrder.nativeOrder());
      mVerBuffer = a.asFloatBuffer();
      mVerBuffer.put(pos);
      mVerBuffer.position(0);
  ```

  