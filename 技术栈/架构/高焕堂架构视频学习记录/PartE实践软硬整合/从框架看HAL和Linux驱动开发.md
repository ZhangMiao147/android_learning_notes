# 从框架看 HAL 和 Linux 驱动开发

# 1. 复习：函数表的定义与实例化

## C 的函数表（function）概念

* c 的 struct 不能包含有 C 函数的实现代码，只能放函数指针（function pointer）。
* 把函数部分独立出来，成为一个函数表(function table)。

![](image/function_table.png)

* 两者之间是什么关系呢？

  两者之间是一种 Whole-Part 组合 (Aggregation) 关系；而不是继承 (Inheritance) 关系。

![](image/whole.png)



# 2. Linux 驱动框架的函数表

* 在 Linux 中，所有的设备 (device) 都被视为一个特殊档案(file)， 称为装置文件 (device file)，每个档案都有自己特殊的编号和型态，定义于 Linux 的 /dev 目录区里。
* 所以，在 Linux 里，共有三个最主要的概念(concept)：
  * 设备，表现于 struct cdev 结构，相当于是 cdev 类。
  * 档案，表现于 struct file 结构，相当于是 file 类。
  * 模块，表现于 struct module 结构，相当于是 module 类。

## 函数表（接口）

* 将 struct file 里的一部分函数定义独立出来，成为函数表(function table)，也就是接口 (interface) 了。
* 例如，从 struct file 独立出来，成为 struct file_operations：

![](image/linux框架.png)

* 这 struct module、struct file 和 struct cdev 就成为 Linux 衔接驱动模块 (Stub) 的主角了；也就相当于基类的角色。
* 基于这些基础的 struct 结构 ( 即基类 )，我们就能加以扩充出子结构 ( 即子类 ) 了。

![](image/扩充.png)

## file_operations 函数表

![](image/file_operations.png)

* Linux 框架定义了 struct file：

```c
struct file {
mode_t f_mode;
loff_t f_pos;
unsigned int f_flags;
struct file_operations *f_op;
void *private_data;
struct dentry *f_dentry;
};
```

* Linux 框架也定义了 struct file_operations 函数表：

```c
struct file_operations {
struct module *owner; 
loff_t (*llseek) (struct file *, loff_t, int); 
ssize_t (*read) (struct file *, char *, size_t, loff_t *); 
ssize_t (*write) (struct file *, const char *, size_t, loff_t *); 
int (*readdir) (struct file *, void *, filldir_t);
  unsigned int (*poll) (struct file *, struct poll_table_struct *); 
int (*ioctl) (struct inode *, struct file *, unsigned int, unsigned long); 
int (*mmap) (struct file *, struct vm_area_struct *); 
int (*open) (struct inode *, struct file *); 
int (*flush) (struct file *); 
int (*release) (struct inode *, struct file *); 
int (*fsync) (struct file *, struct dentry *, int datasync); 
int (*fasync) (int, struct file *, int); 
int (*lock) (struct file *, int, struct file_lock *);
ssize_t (*readv) (struct file *, const struct iovec *, 
unsigned long, loff_t *); 
ssize_t (*writev) (struct file *, const struct iovec *, 
unsigned long, loff_t *); 
// ……..
};
```

## 扩充 struct file 的定义

![](image/adder_file.png)

![](image/驱动模块.png)

* 定义子类：adder_file

```c
struct adder_file {
Int data[2]
struct file* common;
struct semaphore sem;
};
```

* 诞生 adder_file 对象

```c
struct adder_file add_file;
```

* 诞生 file_operations 对象

```c
struct file_operations fop={
.owner = THIS_MODULE,
.open = add_open,
.read = add_read,
.write = add_write,
.release = add_release,
};
```

* 这 2 个对象是以静态 (static) 方式宣告的，会在驱动模块加载时刻 (loading time) 诞生出来。
* 这些函数指针 (Function Pointer) 是用来指向 C 函数的实现代码。
* 于是，在 Linux 驱动模块里，撰写 C 函数的实现代码，如下：

```c
int add_open(struct inode *inode, struct file *filp){
filp->private_data = &add_file;
add_file.common = filp;
return 0;
}
ssize_t add_access(int access_dir, struct file *filp, char __user *buf, 
size_t count){
int *data = filp->private_data->data;
ssize_t retval = 0;
if(down_interruptible(&device->sem)) 
return -ERESTARTSYS;
if(access_dir == 0) {
int sum = data[0] + data[1];
if(count != sizeof(int)) goto out;
retval = copy_to_user(buf, &sum, sizeof(int));
}
else {
if(count != sizeof(int) * 2) goto out;
retval = copy_from_user(data, buf, count);
}
if(retval) {
retval = -EFAULT;
goto out;
}
out:
up(&add_file.sem);
return retval;
}
int add_read(struct file *filp, char __user *buf, size_t count, loff_t 
*f_pos) {
return add_access(0, filp, buf, count);
}
int add_write(struct file *filp, const char __user *buf, size_t count, 
loff_t *f_pos){
return add_access(1, filp, (char __user *)buf, count);
}
int add_release(struct inode *inode, struct file *filp){
return 0;
}
```

* 这 2 个对象是以静态 (static) 方式宣告的。

```c
struct adder_file add_file;
```

```c
struct file_operations fop={
.owner = THIS_MODULE,
.open = add_open,
.read = add_read,
.write = add_write,
.release = add_release,
};
```

* 当驱动模块被载入 Linux 内核时，就诞生这 2 个对象，並让 file_operations 的函数指针指向 add_open()、add_read() 等函数的实现代码。

![](image/adder.png)

* 刚才的对象，是采静态 (static) 方式来诞生的。在驱动被加载时刻就诞生了。 
* 也可以采取动态 (dynamic) 方式来诞生。亦 即，由 module_init() 诞生它们 。

# 3. 活用工厂 EIT 造形

![](image/驱动模块stub.png)

![](image/file.png)

## 扩充及撰写 struct module 的代码

![](image/扩充eit.png)

![](image/工厂eit.png)

![](image/驱动模块ko.png)

* add_module 子类必须实作 module_init() 和module_exit() 函数。

```c
#define ADD_MAJOR 48
#define ADD_MINOR 0
struct cdev add_device;
int add_mod_init(void) {
int result;
dev_t devno = MKDEV(ADD_MAJOR, ADD_MINOR);
result = register_chrdev_region(devno, 1, "androidin");
if(result < 0) return result;
init_MUTEX(&adder.sem);
cdev_init(&add_device, &fop);
my_cdev.owner = THIS_MODULE;
result = cdev_add(&add_device, devno, 1);
if(result) goto fail;
return 0;
fail:
add_cleanup();
return result;
}
void add_mod_cleanup(void) {
dev_t devno = MKDEV(ADD_MAJOR, ADD_MINOR);
cdev_del(&add_device);
unregister_chrdev_region(devno, 1);
}
module_init(add_mod_init);
module_exit(add_mod_cleanup);
MODULE_LICENSE("GPL");
```

* module_init() 函数负责初期化 (initialization) 动作。首先呼叫 cdev 类别的 cdev_init() 函数来进行初始化动作，如下指令：

  ```c
  cdev_init(add_device, &fop);
  ```

* 这让 add_device 对象指向 fop 对象。此外，也让 Linux 所诞生的 struct file 对象 ( 内含 f_op 指针 ) 指向 fop 对象。

* 接着，执行如下指令：

  ```c
  cdev_add(&add_device, devno, 1);
  ```

* 这呼叫 cdev_add() 函数把 cdev 对象指针存入到 Linux 内核的 cdev_map 里。让 Linux 内核可以透过 file_operations 接口而呼叫到 cdev 类里的函数。

* 对系统而言，当设备驱动成功调用了 cdev_add() 之后，就意味着一个设备对象已经加入到了系统，让系统就可以找到它。对用户态的应用程序而言，调用 cdev_add() 之后，就已经可以通过 System Call 呼叫驱动程序了。

* 现在已经将 adder_file 和 adder_module 撰写完毕了。经过编译 & 连结成为 *.ko 模块之后，就能将此驱动模块挂载到 Linux 内核里了。

![](image/atruntime1.png)

![](image/runtime驱动.png)

* 这个对象是以静态 (static) 方式宣告的。

```c
struct cdev add_device;
```

![](image/cdev对象.png)

```c
cdev_init(&add_device, &fop);
```

* 这让 add_device 对象指向 fop 对象。此外，也让 Linux 所诞生的 strut file 对象 ( 内含 f_op 指针 ) 指向 fop 对象。

![](image/ops.png)

![](image/ops框架.png)

### Kernel-Driver 模块（代码层级）

![](image/kd.png)

# 4. 撰写用户态的应用程序

![](image/kdeit.png)

![](image/linux.png)

![](image/linux代码.png)

![](image/撰写app.png)

* 撰写 App 应用程序代码

```c
/* App应用程序 */
#include <stdio.h>
#include <fcntl.h>
#define DEVFILE "/dev/androidin"
#define BUFLEN 128
int main() {
int fd = 0;
int in[2] = {134, 2567};
int out = 0;
fd = open(DEVFILE, O_RDWR);
if(fd == 0)
printf("open '/dev/add' failed!\n");
printf("fd:%d\n", fd);
write(fd, in, sizeof(in));
read(fd, &out, sizeof(out));
close(fd);
printf("Input:%d %d\n", in[0], in[1]);
printf("Output:%d\n", out);
return 0;
}
```

* 执行到 App 的代码：

```c
fd = open(DEVFILE, O_RDWR);
```

* 此时，调用 add_open() 函数。

![](image/appopen.png)

* 执行到 add_open() 函数的实现代码

```c
int add_open(struct inode *inode, struct file *filp){
filp->private_data = &add_file;
add_file.common = filp;
return 0;
}
```

![](image/appdata.png)

* 接着，继续执行到 App 代码：

```c
write(fd, in, sizeof(in));
// ……..
```

* 就调用 add_write() 函数。

![](image/appwrite.png)

* 执行到 add_write() 函数的实现代码:

```c
retval = copy_from_user(data, buf, count);
```

* 将 App 里的数据 ( 存于 buf 内 ) 考贝到 add_file 对象内的 data 变量里。
* 接着，继续执行到 App 代码：

```c
read(fd, &out, sizeof(out));
```

* 就调用到 add_read() 函数。

![](image/appread.png)

* 执行到 add_read() 函数的的实现代码：

```c
int sum = data[0] + data[1];
//……….
retval = copy_to_user(buf, &sum, sizeof(int));
```

* 先进行加法运算，结果存于 sum 变量里。再将 sum 里的值考贝到 App 的 buf 里。
* 最后，继续执行到 App 代码：

```c
printf("Input:%d %d\n", in[0], in[1]);
printf("Output:%d\n", out);
```

* 就将加法计算的结果打印出来了。

## App 也可以是 HAL-Driver

* HAL-Drive 调用 Kernel-Driver

![](image/hal调用kernal.png)

* At run-time

![](image/hal调用.png)

![](image/调用框架.png)

![](image/hal代码.png)

![](image/halstub车.png)