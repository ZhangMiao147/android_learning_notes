
#### GLES20.glVertexAttribPointer(int index,int size,int type,boolean normalized,int stride,buffer ptr)
　　定义顶点属性数组。
参数含义：
index：指定要修改的顶点着色器中顶点变量 id。
size：指定每个顶点属性的组件数量。必须为1、2、3 或者 4.如 position 是由 3 个 （x,y,z）组成，而颜色是 4 个（r,g,b,a）。
type：指定数组中每个组件的数据类型。可用的符号常量有 GL_BYTE、GL_UNSIGNED_BYTE、GL_SHORT、GL_FIXED 和 GL_FLOAT,初始值为 GL_FLOAT。
normalized：指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）。
stride：指定连续顶点属性之间的偏移量。如果为 0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0，如果 normalized 被设置为 GL_TRUE，意味着整数型的值会被映射至区间 -1,1，或者区间[0,1]（无符号整数），反之，这些值会被直接转换为浮点值而不进行归一化处理。
ptr：顶点的缓冲数据。
