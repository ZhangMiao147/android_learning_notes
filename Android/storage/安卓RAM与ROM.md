# 安卓 RAM 与 ROM

## 各种内存

* RAM(Random-Access Memory(随机存取存储器))

  运行时内存。相当于 PC 机的内存存储，用于存储应用运行时的各种对象和变量常量等，主要作用在于提高运行速度。是唯一一种断电后数据会清除的存储器。
  运行时内存。相当于我们台式电脑的内存条，可以存储我们的缓存文件，能够提高运行速度，但是断电的话数据就会被清，我们一般电脑太卡的话就要重新启动就是这个道理。（应用会跑出很多数据出来）

* 手机内存
  手机内存在逻辑上包括 ROM、内部内存和外部内存。(可以理解为电脑的硬盘)

* Rom(Read Only Memory只读存储器)
  翻译过来就是只读性内存。逻辑上这部分文件只能读取。可以理解为系统文件，如果损坏就会导致手机系统无法启动或者运行。

* 内部内存
  相当于内部沙盒，用来存储系统文件和应用的私有文件，Android 一般用来存储首选项，数据库等文件。路径为 /data/data/，可通过 Environment.getDataDirectory().getPath() 方法获取。

* 外部内存
  这部分就比较好理解了，一般用来存储照片啊，音乐啊，等等可以看到的文件。路径一般为：/storage/emulated，可通过Environment.getExternalStorageDirectory().getPath()方法获取。

* SD卡
  就是拓展性的储存卡。不过现在手机一般都不需要外置SD卡了。

## ROM 与 RAM

简单的说，一个完整的计算机系统是由软件和硬件组成的。其中，硬件部分由中央处理单元 CPU（包括运算器和控制器）、存储器和输入/输出设备构成。目前个人电脑上使用的主板一般只能支持到 1GB 的内存，即使是 INTEL 目前最高阶的 450NX 芯片组也只能支持到 4GB。

存储器包括主存储器（Main Memory）和辅助存储器（Auxiliary Memory）。主存储器又称内存储器（简称内存），辅助存储器又称外存储器（简称外存）。主存具有速度快、价格高、容量小的特点，负责直接与 CPU 交换指令和数据。辅存通常是磁性介质或光盘，能长期保存信息，并且不依赖于电来保存信息。辅存速度慢、价格低、容量大，可以用来保存程序和数据。常见的辅存如硬盘、软盘、CD-ROM 等，而现在的主存一般就是指半导体集成电路存储器了。那主存和内存有什么关系呢？可以这么认为：主存就是广义的内存。

广义的内存分为随机存储器（RAM，RANDOM ACCESS MEMORY）和只读存储器（ROM，READ ONLY MEMORY）。电脑上使用 RAM 来临时存储运行程序需要的数据，不过如果电脑断电后，这些存储在 RAM 中的数据将全部丢失。每种每台电脑中都结合有两种基本类型的内存，它们分别有不同的用途以完成不同的任务。 为了存储数据的持久性，ROM 常用于存储电脑重要的信息例如：电脑主板的 BIOS(基本输入/输出系统)。不像 RAM，存储在 ROM 中的数据理论上是永久的。即使电脑关机后，保存在 ROM 中的数据也不会丢失。 存储在 BIOS 中的信息控制着你电脑系统的运行。正因为其重要性，对 BIOS 未经授权的复制或删除是不允许的。

* RAM为运行内存，比如，360 手机助手的悬浮窗，经常提示的运行内存超过 80% 之类的，指的都是运行内存。一般大小为几个 G。

* ROM 为存储数据的内存，比如，爱奇艺 APP 在视频页面显示的 “ 总空间 31.6G，剩余 28.8G”，指的是 ROM。一般大小几十 G，几百 G 都有。ROM 越大，可存储的视频，文件，音乐等越多。

## RAM

　  RAM 是指通过指令可以随机的、个别的对各个存储单元进行访问的存储器，一般访问时间基本固定，而与存储单元地址无关。RAM 的速度比较快，但其保存的信息需要电力支持，一旦丢失供电即数据消失，所以又叫易失性存储器，还有一种很有趣的叫法是"挥发性存储器"，当然这里"挥发"掉的是数据而不是物理上的芯片。在 51 单片机中，RAM 主要用来保存数据、中间结果和运行程序等，所以也把 RAM 叫做数据存储器。

### 1. RAM 的分类

RAM 又分动态存储器（DRAM，DYNAMIC RAM）和静态存储器（SRAM，STATIC RAM）。经常说的 “ 系统内存 ” 就是指后者，DRAM。

静态 RAM 是靠双稳态触发器来记忆信息的，只要不断电，信息是不会丢失的，所以谓之静态；动态 RAM 是靠 MOS 电路中的栅极电容来记忆信息的。由于电容上的电荷会泄漏，需要定时给与补充，这个充电的过程叫再生或刷新（REFRESH），所以动态 RAM 需要设置刷新电路。

由于电容的充放电是需要相对较长的时间的，DRAM 的速度要慢于 SRAM。但 SRAM 免刷新的优点需要较复杂的电路支持，如一个典型的 SRAM 的存储单元需要六个晶体管（三极管）构成，而 DRAM 的一个存储单元最初需要三个晶体管和一个电容，后来经过改进，就只需要一个晶体管和一个电容了，所以动态 RAM 比静态 RAM 集成度高、功耗低，从而成本也低，适于作大容量存储器。所以主内存通常采用动态 RAM，而高速缓冲存储器（Cache）则使用静态 RAM。另外，内存还应用于显卡、声卡及 CMOS 等设备中，用于充当设备缓存或保存固定的程序及数据。

### 2. 目前和未来的的内存

目前在市场上还是 SDRAM 占统治地位，是目前的主流内存。但是随着内存技术蓬勃发展，几个大厂商都在加紧自己新型内存技术的发展，其中尤以 RDRAM 和 DDR 的较量最为激烈，可以预料未来将是 RDRAM 和 DDR 的天下。

　　下面将分别介绍这几种内存。

* SDRAM：SDRAM（Synchronous DRAM）的中文名字是 “ 同步动态随机存储器 ”，这就是目前主推的 PC100 和 PC133 规范所广泛使用的内存类型，它的带宽为 64bit，3.3 V 电压，目前产品的最高速度可达 5ns。它是与 CPU 使用相同的时钟频率进行数据交换，它的工作频率是与 CPU 的外频同步的，不存在延迟或等待时间。
* DDR SDRAM：又简称 DDR，是 “ 双倍速率 SDRAM ” 的意思，由于它可以在时钟触发沿的上、下沿都能进行数据传输，所以即使在 133MHz 的总线频率下的带宽也能达到 2.128GB/S。DDR 不支持 3.3V 电压的 LVTTL，而是支持 2.5V 的 SSTL2 标准。它仍然可以沿用现有 SDRAM 的生产体系，制造成本比 SDRAM 略高一些（约为 10% 左右）。
* RDRAM：Direct Rambus DRAM（DRDRAM）“ 接口动态随机存储器 ”，这是 Intel 所推崇的未来内存的发展方向，它将 RISC（精简指令集）引入其中，依靠高时钟频率来简化每个时钟周期的数据量。它具有相对 SDRAM 较高的工作频率（不低于 300MHz），但其数据通道接口带宽较低，只有 16bit，当工作时钟为 300MHz 时，Rambus 利用时钟的上沿和下沿分别传输数据，因此它的数据传输率能达到 300×16×2÷8=1.2GB/S，若是两个通道，就是 2.4GB/S。它与传统 DRAM 的区别在于引脚定义会随命令变化，同一组引脚线既可以被定义成地址线也可以被定义成控制线。其引脚数仅为普通 DRAM 的三分之一。当需要扩展芯片容量时，只需要改变命令，不需要增加芯片引脚。DRDRAM 要求 RIMM 中必须都插满，空余的插槽中必须插上传接板（也叫终结器）。

### 3. Android 获取 RAM 的方法

```java

		// 获取运行内存的信息
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
        MemoryInfo info = new MemoryInfo();  
        manager.getMemoryInfo(info);  
        StringBuilder sb = new StringBuilder();
        sb.append("可用RAM:");
        sb.append(info.availMem + "B");
        sb.append(",总RAM:");
        sb.append(info.totalMem + "B");
        sb.append("\r\n");
        sb.append(Formatter.formatFileSize(getBaseContext(), info.availMem));
        sb.append(",");
        LogUtil.print("totalMem:" + info.totalMem);
        sb.append(Formatter.formatFileSize(getBaseContext(), info.totalMem));
        tv1.setText(sb);
```

## ROM

ROM 英文概念是 Read Only Memory，只读式存储器，在计算机中，是一种类型的内存。此类型内存常被用于存储重要的或机密的数据。理想上认为，此种类型的内存是只能读取，而不允许擦写。在 51 单片机中，ROM 一般用来存放常数、数据表格、程序代码等，所以也叫做程序存储器。

不过也有一些不同一般的 ROM 类型，它可为某种特殊的要求而涮新其内容。

### 不同的 ROM 类型

#### 1. ROM

是标准的 ROM，用于永久性存储重要数据。当一项科技性产品需要其部分信息不会随着外界等因素的变化而变更时，它们通常都使用此标准的 ROM 模块。在 ROM 中，信息是被永久性的蚀刻在 ROM 单元中的，这使得 ROM 在完成蚀刻工作后是不可能再将其中的信息改变。

#### 2. PROM（Programmable ROM，可编程 ROM）

此类型的 ROM 的工作原理与 CD-R 相似，它允许你一次性地重写其中的数据，请记得：重写(涮新)其中数据的次数只有一次。一旦信息被写入 PROM 后，数据也将被永久性地蚀刻其中了，之后此块 PROM 与上面介绍的 ROM 就没什么两样了。 

#### 3. EPROM（Erasable Programmable ROM，可擦去可编程 ROM） 

当然存储在 ROM 中的数据需要抹去或进行重新写入时，EPROM 可以办到。使用紫外线照射此类型的 ROM 可以抹去其中的数据，它还允许将你需要的信息存储入此类 ROM 中。

#### 4. EEPROM（Electrically Erasable Programmable ROM，电可擦去可编程 ROM）

 此类 ROM 现在常用于电脑系统的 BIOS，它与 EPROM 非常相似， EEPROM 中的信息也同样可以被抹去，也可以向其中写入新数据。就如其名字所示，对于此 EEPROM 我们可以使用电来对其进行擦写，而不需要紫外线，这对于主板的 BIOS 是非常有用的。基于上面所介绍的原理，主板制造商可以发布他们最新的 BIOS，以供用户升级主板的 BIOS，而升级的方法就是利用 BIOS 升级程序来对产生电信号以涮新 BIOS 中的信息。 

 通过上面的分析介绍，非常明显并不是所有的 ROM 内存都是“Read Only，只读的“，那为什么都称他们为只读的呢？其实这只是延用历史名称罢了；至于其中的非只读部份却带给了我们许多好外；例如常用于主板 BIOS 的EEPROM，正因为它是不是只读的，而是可擦写的，因此主板产商可以通过发布最新主板 BIOS 的升级程序，用户只需下载并运行这些程序就可能升级主板的 BIOS，而不必拿着主板到产商那去升级 BIOS。

#### 5. Flash-ROM（闪存）

已经成为了目前最成功、流行的一种固态内存，广泛的用于主板和显卡声卡网卡等扩展卡的 BIOS 存储上。与 EEPROM 相比具有读写速度快，而与 SRAM 相比具有非易失、以及价廉等优势。闪存是非易失内存，可以对称为块的内存单元块进行擦写和再编程。任何闪存器件的写入操作只能在空或已擦除的单元内进行，所以大多数情况下，在进行写入操作之前必须先执行擦除。而基于 NOR 和 NAND 结构的闪存是现在市场上两种主要的非易失闪存技术。Intel 于 1988 年首先开发出 NOR flash 技术，彻底改变了原先由 EPROM 和 EEPROM 一统天下的局面。紧接着，1989 年东芝公司发表了 NAND flash 技术（后将该技术无偿转让给韩国　Samsung　公司），强调降低每比特的成本，更高的性能，并且象磁盘一样可以通过接口轻松升级。

### Android 获取 ROM 的方法

```java
// 获取 ROM 内存信息
        // 调用该类来获取磁盘信息（而 getDataDirectory 就是内部存储）(/data)
        final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long totalCounts = statFs.getBlockCountLong();// 总共的 block 数
        long availableCounts = statFs.getAvailableBlocksLong() ; //获取可用的 block 数
        long size = statFs.getBlockSizeLong(); // 每格所占的大小，一般是 4KB==
        long availROMSize = availableCounts * size; // 可用内部存储大小
        long totalROMSize = totalCounts *size; // 内部存储总大小
        sb.append("可用block数:" + availableCounts);
        sb.append("block总数:" + totalCounts);
        sb.append("\r\n");
        sb.append(" 每个block大小:" + size);
        sb.append("\r\n");
        sb.append(" 可用ROM:" + availROMSize + "B");
        sb.append(" 总ROM:" + totalROMSize + "B");
        tv2.setText(sb);

```

## 有关 BIOS 和 CMOS

**BIOS 系统开机启动 BIOS**，即微机的基本输入输出系统(Basic Input-Output System)，是集成在主板上的一个 ROM 芯片，其中保存有微机系统最重要的基本输入/输出程序、系统信息设置、开机上电自检程序和系统启动自举程序。在主板上可以看到 BIOS ROM 芯片，586 以后的 ROM BIOS 多采用 EEPROM (电可擦写只读 ROM)，通过跳线开关和系统配带的驱动程序盘，可以对 EEPROM 进行重写，方便地实现 BIOS 升级。一块主板性能优越与否，很大程度上取决于板上的 BIOS 管理功能是否先进。

**BIOS 中断例程** 即 BIOS 中断服务程序。它是微机系统软、硬件之间的一个可编程接口，用于程序软件功能与微机硬件实现的衍接。

**BIOS 系统设置程序** 微机部件配置情况是放在一块可读写的 CMOS RAM 芯片中的，它保存着系统 CPU、软硬盘驱动器、显示器、键盘等部件的信息。 关机后，系统通过主板上一块后备电池向 CMOS 供电以保持其中的信息。如果 CMOS 中关于微机的配置信息不正确，会导致系统性能降 低、零部件不能识别，并由此引发一系统的软硬件故障。在 BIOS ROM 芯片中装有一个程序称为 “ 系统设置程序 ”，就是用来设置 CMOS RAM 中的参数的。这个程序一般在开机时按下一个或一组键即可进入，它提供了良好的界面供用户使用。这个设置 CMOS 参数的过程，习惯上也称为 “ BIOS设置 ”。新购的微机或新增了部件的系统，都需进行 BIOS 设置。BIOS 系统启动自举程序 在完成 POST(Power On Self Test,上电自 检) 自检后，ROM BIOS 将按照系统 CMOS 设置中的启动顺序搜寻软硬盘驱动器及 CDROM、网络服务器等有效的启动驱动器 ，读入操作系统引导记录，然后将系统控制权交给引导记录，由引导记录完成系统的启动。

**CMOS**（本意是指互补金属氧化物半导体——一种大规模应用于集成电路芯片制造的原料）是微机主板上的一块可读写的 RAM 芯 片，用来保存当前系统的硬件配置和用户对某些参数的设定。CMOS 可由主板的电池供电，即使系统掉电，信息也不会丢失。CMOS RAM 本身只是一块存储器，只有数据保存功能，而对 CMOS 中各项参数的设定要通过专门的程序。

## 参考文章

1. [Android中的RAM、ROM、SD卡以及各种内存的区别](https://blog.csdn.net/weixin_44911775/article/details/121080338)
2. [ANDROID 获取内存信息（RAM,ROM）](https://www.freesion.com/article/202541185/)
3. [MTK Android ROM与RAM的区别](https://www.likecs.com/default/index/url?u=aHR0cHM6Ly93d3cuY25ibG9ncy5jb20vY3lxeC9wLzQ4NTY4OTkuaHRtbA%3D%3D)

