#### GLES20.glUniform()
　　指定当前程序对象的统一变量的值。

```
void glUniform1f(    GLint location, GLfloat v0);
void glUniform2f(    GLint location,  GLfloat v0, GLfloat v1);
void glUniform3f(    GLint location, GLfloat v0, GLfloat v1, GLfloat v2);
void glUniform4f(    GLint location, GLfloat v0, GLfloat v1, GLfloat v2, GLfloat v3);
void glUniform1i(    GLint location, GLint v0);
void glUniform2i(    GLint location,GLint v0,GLint v1);
void glUniform3i(    GLint location,GLint v0,GLint v1,GLint v2);
void glUniform4i(    GLint location,GLint v0,GLint v1,GLint v2,GLint v3);

void glUniform1fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform2fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform3fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform4fv(    GLint location,GLsizei count,const GLfloat *value);
void glUniform1iv(    GLint location,GLsizei count,const GLint *value);
void glUniform2iv(    GLint location,GLsizei count,const GLint *value);
void glUniform3iv(    GLint location, GLsizei count, const GLint *value);
void glUniform4iv(    GLint location, GLsizei count, const GLint *value);

void glUniformMatrix2fv(    GLint location,GLsizei countM,GLboolean transpose,const GLfloat *valueM);
void glUniformMatrix3fv(    GLint location,GLsizei countM,GLboolean transpose,const GLfloat *valueM);
void glUniformMatrix4fv(    GLint location,GLsizei countM,GLboolean transpose, const GLfloat *valueM);

```
##### 参数
location：指定要修改的统一变量的位置。
