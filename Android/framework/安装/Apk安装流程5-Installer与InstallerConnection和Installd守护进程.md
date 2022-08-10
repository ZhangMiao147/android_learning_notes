# Apk 安装流程5-Installer、InstallerConnection和 Installd 守护进程 

本片文章的主要内容如下：

```
1、Installer简介
2、InstallerConnection简介
3、Installd守护进程
```

## 一、Installer 简介

[Installer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FInstaller.java)

### (一)、Installer类简介

```java
public final class Installer extends SystemService {
}
```

我们知道Installer继承自SystemService，在Android系统中有两个SystemServer，一个是[os/SystemService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fandroid%2Fos%2FSystemService.java)，另一个是[server/SystemService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2FSystemService.java)，这里Installer继承的是[server/SystemService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2FSystemService.java)，所以我们可以说Installer其实是一个系统服务。

### (二)、Installer类的构造函数

```java
    private final InstallerConnection mInstaller;
    public Installer(Context context) {
        super(context);
        mInstaller = new InstallerConnection();
    }
```

Installer就一个有参的构造函数，并且传入一个Context，而在构造函数里面什么都没做，就是初始化了mInstaller，这里mInstaller其实是一个InstallerConnection对象。

### (三)、Installer类的启动

#### 1、Installer的启动

代码在[SystemServer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fjava%2Fcom%2Fandroid%2Fserver%2FSystemServer.java) 326行

```java
private void startBootstrapServices() {
        // Wait for installd to finish starting up so that it has a chance to
        // create critical directories such as /data/user with the appropriate
        // permissions.  We need this to complete before we initialize other services.
        Installer installer = mSystemServiceManager.startService(Installer.class);
    ...
}
```

先来看下翻译

```
等待intalld完成启动，这样它就可以创建需要权限的关键目录，比如/data/user。在初始化其他服务之前，我们必须先做此操作
等待installd完成启动，以便它有机会创建具有适当权限的关键目录，如/ data / user。 在初始化其他服务之前，我们需要完成此操作。
```

#### 2、onStart()方法

因为Installer继承自SystemService，所以我们看下Installer的onStart方法
代码在[Installer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FInstaller.java) 396行

```java
    @Override
    public void onStart() {
        Slog.i(TAG, "Waiting for installd to be ready.");
        mInstaller.waitForConnection();
    }
```

我们发现这个方法里面什么都没做，就是调用了mInstaller.waitForConnection(String)方法。

#### 3、小结

先创建Installer对象，再调用onStart()方法，该方法中主要工作是等待socket通道建立完成。

### (四)、Installer类的其他方法

上面一篇文章我们在讲解PackageManagerService初始化的时候，涉及到了很多关于Installer的操作，我们就来看下：

* 1、[PackageManagerService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 构造函数里面
   1985行调用mInstaller.dexopt(lib, Process.SYSTEM_UID, true, dexCodeInstructionSet, dexoptNeeded, false);

* 2、[PackageManagerService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 构造函数里面 2034行调用
   mInstaller.dexopt(path, Process.SYSTEM_UID, true, dexCodeInstructionSet, dexoptNeeded, false);

* 3、 [PackageManagerService.java](https://link.jianshu.com?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FPackageManagerService.java) 构造函数里面 2103行调用
   mInstaller.moveFiles();

由于上面1和2调用都是 dexopt(String, int, boolean,String, int, boolean)方法，那我们就来看下这个方法：

#### 1、dexopt(String, int, boolean,String, int, boolean)方法

代码在[Installer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FInstaller.java) 83行

```java
    public int dexopt(String apkPath, int uid, boolean isPublic,
            String instructionSet, int dexoptNeeded, boolean bootComplete) {
        // 校验是否是非法的instructionSet
        if (!isValidInstructionSet(instructionSet)) {
            Slog.e(TAG, "Invalid instruction set: " + instructionSet);
            return -1;
        }
         // 最终调用了mInstaller的dexopt方法
        return mInstaller.dexopt(apkPath, uid, isPublic, instructionSet, dexoptNeeded,
                bootComplete);
    }
```

我们看到了这个方法本质其实是通过mInstaller的dexopt方法来进行的。

#### 2、mInstaller.moveFiles()方法

代码在[Installer.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fservices%2Fcore%2Fjava%2Fcom%2Fandroid%2Fserver%2Fpm%2FInstaller.java) 396行

```cpp
    public int moveFiles() {
        return mInstaller.execute("movefiles");
    }
```

我们发现这个方法里面什么都没做，就是调用了mInstaller.execute(String)方法。

#### 3、总结

大家发现什么概率没，是的，貌似Installer的很多方法的具体实现最后都是调用了mInstaller的方法，其中大部分的方法其最后，都是调用的mInstaller.execute(String)方法如下：

```go
63    public int install(String uuid, String name, int uid, int gid, String seinfo) {
64        StringBuilder builder = new StringBuilder("install");
65        builder.append(' ');
66        builder.append(escapeNull(uuid));
67        builder.append(' ');
68        builder.append(name);
69        builder.append(' ');
70        builder.append(uid);
71        builder.append(' ');
72        builder.append(gid);
73        builder.append(' ');
74        builder.append(seinfo != null ? seinfo : "!");
75        return mInstaller.execute(builder.toString());
76    }

101    public int dexopt(String apkPath, int uid, boolean isPublic, String pkgName,
102            String instructionSet, int dexoptNeeded, boolean vmSafeMode,
103            boolean debuggable, @Nullable String outputPath, boolean bootComplete) {
104        if (!isValidInstructionSet(instructionSet)) {
105            Slog.e(TAG, "Invalid instruction set: " + instructionSet);
106            return -1;
107        }
108        return mInstaller.dexopt(apkPath, uid, isPublic, pkgName,
109                instructionSet, dexoptNeeded, vmSafeMode,
110                debuggable, outputPath, bootComplete);
111    }
112
113    public int idmap(String targetApkPath, String overlayApkPath, int uid) {
114        StringBuilder builder = new StringBuilder("idmap");
115        builder.append(' ');
116        builder.append(targetApkPath);
117        builder.append(' ');
118        builder.append(overlayApkPath);
119        builder.append(' ');
120        builder.append(uid);
121        return mInstaller.execute(builder.toString());
122    }
123
124    public int movedex(String srcPath, String dstPath, String instructionSet) {
125        if (!isValidInstructionSet(instructionSet)) {
126            Slog.e(TAG, "Invalid instruction set: " + instructionSet);
127            return -1;
128        }
129
130        StringBuilder builder = new StringBuilder("movedex");
131        builder.append(' ');
132        builder.append(srcPath);
133        builder.append(' ');
134        builder.append(dstPath);
135        builder.append(' ');
136        builder.append(instructionSet);
137        return mInstaller.execute(builder.toString());
138    }
139
140    public int rmdex(String codePath, String instructionSet) {
141        if (!isValidInstructionSet(instructionSet)) {
142            Slog.e(TAG, "Invalid instruction set: " + instructionSet);
143            return -1;
144        }
145
146        StringBuilder builder = new StringBuilder("rmdex");
147        builder.append(' ');
148        builder.append(codePath);
149        builder.append(' ');
150        builder.append(instructionSet);
151        return mInstaller.execute(builder.toString());
152    }
153
154    /**
155     * Removes packageDir or its subdirectory
156     */
157    public int rmPackageDir(String packageDir) {
158        StringBuilder builder = new StringBuilder("rmpackagedir");
159        builder.append(' ');
160        builder.append(packageDir);
161        return mInstaller.execute(builder.toString());
162    }



169    public int remove(String uuid, String name, int userId) {
170        StringBuilder builder = new StringBuilder("remove");
171        builder.append(' ');
172        builder.append(escapeNull(uuid));
173        builder.append(' ');
174        builder.append(name);
175        builder.append(' ');
176        builder.append(userId);
177        return mInstaller.execute(builder.toString());
178    }

180    public int rename(String oldname, String newname) {
181        StringBuilder builder = new StringBuilder("rename");
182        builder.append(' ');
183        builder.append(oldname);
184        builder.append(' ');
185        builder.append(newname);
186        return mInstaller.execute(builder.toString());
187    }



194    public int fixUid(String uuid, String name, int uid, int gid) {
195        StringBuilder builder = new StringBuilder("fixuid");
196        builder.append(' ');
197        builder.append(escapeNull(uuid));
198        builder.append(' ');
199        builder.append(name);
200        builder.append(' ');
201        builder.append(uid);
202        builder.append(' ');
203        builder.append(gid);
204        return mInstaller.execute(builder.toString());
205    }



212    public int deleteCacheFiles(String uuid, String name, int userId) {
213        StringBuilder builder = new StringBuilder("rmcache");
214        builder.append(' ');
215        builder.append(escapeNull(uuid));
216        builder.append(' ');
217        builder.append(name);
218        builder.append(' ');
219        builder.append(userId);
220        return mInstaller.execute(builder.toString());
221    }

228    public int deleteCodeCacheFiles(String uuid, String name, int userId) {
229        StringBuilder builder = new StringBuilder("rmcodecache");
230        builder.append(' ');
231        builder.append(escapeNull(uuid));
232        builder.append(' ');
233        builder.append(name);
234        builder.append(' ');
235        builder.append(userId);
236        return mInstaller.execute(builder.toString());
237    }
238


244    public int createUserData(String uuid, String name, int uid, int userId, String seinfo) {
245        StringBuilder builder = new StringBuilder("mkuserdata");
246        builder.append(' ');
247        builder.append(escapeNull(uuid));
248        builder.append(' ');
249        builder.append(name);
250        builder.append(' ');
251        builder.append(uid);
252        builder.append(' ');
253        builder.append(userId);
254        builder.append(' ');
255        builder.append(seinfo != null ? seinfo : "!");
256        return mInstaller.execute(builder.toString());
257    }

259    public int createUserConfig(int userId) {
260        StringBuilder builder = new StringBuilder("mkuserconfig");
261        builder.append(' ');
262        builder.append(userId);
263        return mInstaller.execute(builder.toString());
264    }
265


271    public int removeUserDataDirs(String uuid, int userId) {
272        StringBuilder builder = new StringBuilder("rmuser");
273        builder.append(' ');
274        builder.append(escapeNull(uuid));
275        builder.append(' ');
276        builder.append(userId);
277        return mInstaller.execute(builder.toString());
278    }

280    public int copyCompleteApp(String fromUuid, String toUuid, String packageName,
281            String dataAppName, int appId, String seinfo) {
282        StringBuilder builder = new StringBuilder("cpcompleteapp");
283        builder.append(' ');
284        builder.append(escapeNull(fromUuid));
285        builder.append(' ');
286        builder.append(escapeNull(toUuid));
287        builder.append(' ');
288        builder.append(packageName);
289        builder.append(' ');
290        builder.append(dataAppName);
291        builder.append(' ');
292        builder.append(appId);
293        builder.append(' ');
294        builder.append(seinfo);
295        return mInstaller.execute(builder.toString());
296    }



303    public int clearUserData(String uuid, String name, int userId) {
304        StringBuilder builder = new StringBuilder("rmuserdata");
305        builder.append(' ');
306        builder.append(escapeNull(uuid));
307        builder.append(' ');
308        builder.append(name);
309        builder.append(' ');
310        builder.append(userId);
311        return mInstaller.execute(builder.toString());
312    }

314    public int markBootComplete(String instructionSet) {
315        if (!isValidInstructionSet(instructionSet)) {
316            Slog.e(TAG, "Invalid instruction set: " + instructionSet);
317            return -1;
318        }
320        StringBuilder builder = new StringBuilder("markbootcomplete");
321        builder.append(' ');
322        builder.append(instructionSet);
323        return mInstaller.execute(builder.toString());
324    }


331    public int freeCache(String uuid, long freeStorageSize) {
332        StringBuilder builder = new StringBuilder("freecache");
333        builder.append(' ');
334        builder.append(escapeNull(uuid));
335        builder.append(' ');
336        builder.append(String.valueOf(freeStorageSize));
337        return mInstaller.execute(builder.toString());
338    }



405    /**
406     * Links the 32 bit native library directory in an application's data directory to the
407     * real location for backward compatibility. Note that no such symlink is created for
408     * 64 bit shared libraries.
409     *
410     * @return -1 on error
411     */
412    public int linkNativeLibraryDirectory(String uuid, String dataPath, String nativeLibPath32,
413            int userId) {
414        if (dataPath == null) {
415            Slog.e(TAG, "linkNativeLibraryDirectory dataPath is null");
416            return -1;
417        } else if (nativeLibPath32 == null) {
418            Slog.e(TAG, "linkNativeLibraryDirectory nativeLibPath is null");
419            return -1;
420        }
421
422        StringBuilder builder = new StringBuilder("linklib");
423        builder.append(' ');
424        builder.append(escapeNull(uuid));
425        builder.append(' ');
426        builder.append(dataPath);
427        builder.append(' ');
428        builder.append(nativeLibPath32);
429        builder.append(' ');
430        builder.append(userId);
431
432        return mInstaller.execute(builder.toString());
433    }

440    public boolean restoreconData(String uuid, String pkgName, String seinfo, int uid) {
441        StringBuilder builder = new StringBuilder("restorecondata");
442        builder.append(' ');
443        builder.append(escapeNull(uuid));
444        builder.append(' ');
445        builder.append(pkgName);
446        builder.append(' ');
447        builder.append(seinfo != null ? seinfo : "!");
448        builder.append(' ');
449        builder.append(uid);
450        return (mInstaller.execute(builder.toString()) == 0);
451    }

453    public int createOatDir(String oatDir, String dexInstructionSet) {
454        StringBuilder builder = new StringBuilder("createoatdir");
455        builder.append(' ');
456        builder.append(oatDir);
457        builder.append(' ');
458        builder.append(dexInstructionSet);
459        return mInstaller.execute(builder.toString());
460    }


463    public int linkFile(String relativePath, String fromBase, String toBase) {
464        StringBuilder builder = new StringBuilder("linkfile");
465        builder.append(' ');
466        builder.append(relativePath);
467        builder.append(' ');
468        builder.append(fromBase);
469        builder.append(' ');
470        builder.append(toBase);
471        return mInstaller.execute(builder.toString());
472    }
```

所有Installer很多方法的具体实现都是mInstaller(即InstallerConnection对象)来实现的。那下面就让我们来看下这个类。

## 二、InstallerConnection简介

[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java)

### (一)、先来看下InstallerConnection类

```php
/**
 * Represents a connection to {@code installd}. Allows multiple connect and
 * disconnect cycles.
 *
 * @hide for internal use only
 */
public class InstallerConnection {
    ...
}
```

翻译一下注释：

```
代表与installd的连接，允许多个连接和断开连接
```

可见，这个类其实是一个"连接的"包装类

### (二)、先来看下InstallerConnection类的构造函数

```cpp
    public InstallerConnection() {
    }
```

InstallerConnection的就一个构造函数。里面什么都没有做。

那我们就来看下被Install调用的几个方法：

### (三)、先来看下InstallerConnection类的常用方法

#### 1、dexopt(String , int, boolean,String, int, boolean) 方法

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 94行

```java
    public int dexopt(String apkPath, int uid, boolean isPublic,
            String instructionSet, int dexoptNeeded, boolean bootComplete) {
        return dexopt(apkPath, uid, isPublic, "*", instructionSet, dexoptNeeded,
                false, false, null, bootComplete);
    }
```

这个方法什么都没做，直接调用了dexopt(String apkPath, int uid, boolean isPublic, String pkgName,String instructionSet, int dexoptNeeded, boolean vmSafeMode,boolean debuggable, String outputPath, boolean bootComplete) 方法 。

那我们来看下

```go
    public int dexopt(String apkPath, int uid, boolean isPublic, String pkgName,
            String instructionSet, int dexoptNeeded, boolean vmSafeMode,
            boolean debuggable, String outputPath, boolean bootComplete) {
        StringBuilder builder = new StringBuilder("dexopt");
        builder.append(' ');
        builder.append(apkPath);
        builder.append(' ');
        builder.append(uid);
        builder.append(isPublic ? " 1" : " 0");
        builder.append(' ');
        builder.append(pkgName);
        builder.append(' ');
        builder.append(instructionSet);
        builder.append(' ');
        builder.append(dexoptNeeded);
        builder.append(vmSafeMode ? " 1" : " 0");
        builder.append(debuggable ? " 1" : " 0");
        builder.append(' ');
        builder.append(outputPath != null ? outputPath : "!");
        builder.append(bootComplete ? " 1" : " 0");
        return execute(builder.toString());
    }
```

可见我们这个dexopt方法其实也是调用的execute(String)，再结合上面的解析，我们知道Install类调用InstallerConnection的方法基本上最后都是执行execute(String) 方法，那么我们就来看下

#### 2、execute(String cmd)方法

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 85行

```cpp
    public int execute(String cmd) {
        String res = transact(cmd);
        try {
            return Integer.parseInt(res);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
```

我们看到这方法 主要就是调用transact(String)方法，然后把String类型返回值转化为int型返回，那我们就来看下transact(String)方法

#### 3、transact(String cmd) 方法

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 49行

```dart
    public synchronized String transact(String cmd) {
         // 第一步
        if (!connect()) {
            Slog.e(TAG, "connection failed");
            return "-1";
        }
        // 第二步
        if (!writeCommand(cmd)) {
            /*
             * If installd died and restarted in the background (unlikely but
             * possible) we'll fail on the next write (this one). Try to
             * reconnect and write the command one more time before giving up.
             */
            Slog.e(TAG, "write command failed? reconnect!");
            if (!connect() || !writeCommand(cmd)) {
                return "-1";
            }
        }
        if (LOCAL_DEBUG) {
            Slog.i(TAG, "send: '" + cmd + "'");
        }

        // 第三步
        final int replyLength = readReply();
        if (replyLength > 0) {
            String s = new String(buf, 0, replyLength);
            if (LOCAL_DEBUG) {
                Slog.i(TAG, "recv: '" + s + "'");
            }
            return s;
        } else {
            if (LOCAL_DEBUG) {
                Slog.i(TAG, "fail");
            }
            return "-1";
        }
    }
```

就像一般的请求一样，我将上面的代码分为3部分

- 1、建立连接：connect()方法
- 2、发出请求：writeCommand(String)方法
- 3、收到回复：readReply()方法

下面我们就详细看下其对应的几个方法

##### 3.1、connect()方法简介

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 123行

```java
    private boolean connect() { 
        // 第一次才需要进行实际连接，之后就不需要了
        if (mSocket != null) {
            return true;
        }
        Slog.i(TAG, "connecting...");
        try {
            mSocket = new LocalSocket();
            // 得到"installd"目的端地址
            LocalSocketAddress address = new LocalSocketAddress("installd",
                    LocalSocketAddress.Namespace.RESERVED);
             // 进行连接
            mSocket.connect(address);

             // 以下得到输入流和输出流
            mIn = mSocket.getInputStream();
            mOut = mSocket.getOutputStream();
        } catch (IOException ex) {
            disconnect();
            return false;
        }
        return true;
    }
```

通过上面代码我们知道，在connect()方法内部通过LocalSocketAddress与installd建立连接，其中mIn和mOut分别对应输入流和输出流

##### 3.2、writeCommand(String)方法简介

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 192行

```java
    private boolean writeCommand(String cmdString) {
        final byte[] cmd = cmdString.getBytes();
        final int len = cmd.length;
        if ((len < 1) || (len > buf.length)) {
            return false;
        }

        buf[0] = (byte) (len & 0xff);
        buf[1] = (byte) ((len >> 8) & 0xff);
        try {
            // 写入的长度
            mOut.write(buf, 0, 2);
           // 写入的具体命令
            mOut.write(cmd, 0, len);
        } catch (IOException ex) {
            Slog.e(TAG, "write error");
            disconnect();
            return false;
        }
        return true;
    }
```

这个方法很简单，把cmdString转化byte[] ，这里面涉及到一个buf，buf是一个size为1024的byte数组。然后把cmdString对应的byte写入到输入流中。

##### 3.3、readReply()方法简介

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 173行

```go
    private int readReply() {
        if (!readFully(buf, 2)) {
            return -1;
        }
        final int len = (((int) buf[0]) & 0xff) | ((((int) buf[1]) & 0xff) << 8);
        if ((len < 1) || (len > buf.length)) {
            Slog.e(TAG, "invalid reply length (" + len + ")");
            disconnect();
            return -1;
        }
        if (!readFully(buf, len)) {
            return -1;
        }
        return len;
    }
```

这个方法内部很简单就是调用readFully(byte[] buffer, int len)读取输入流而已

```java
private boolean readFully(byte[] buffer, int len) {
     try {
         Streams.readFully(mIn, buffer, 0, len);
     } catch (IOException ioe) {
         disconnect();
         return false;
     }
     return true;
 }
```

##### 3.4、小结

可见，一次transct过程就是先connect()来判断是否建立socket连接，如果已经连接则调用writeCommand()将命令写入socket的mOut管道，等待从管道的mIn中readFully()读取应答消息。

#### 4、execute(String cmd)方法

代码在[InstallerConnection.java](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fbase%2Fcore%2Fjava%2Fcom%2Fandroid%2Finternal%2Fos%2FInstallerConnection.java) 85行

```cpp
public void waitForConnection() {
    for (;;) {
        if (execute("ping") >= 0) {
            return;
        }
        Slog.w(TAG, "installd not ready");
        SystemClock.sleep(1000);
    }
}
```

通过循环地方式，每次休眠1s

#### 5、总结

InstallerConnection就是一个连接类，负责连接installd。

## 三、Installd守护进程

### (一)、概述

我们知道PackageManagerServcie负责应用的安装，卸载等相关工作，但是大家注意，里面主要是"Manager"，那具体负责这一块的是什么？就是我们要讲解的installd，installd才是真正的干活的。是通过PackageManagerService来访问的installd服务来执行程序包的安装与卸载的。

如下图

![](https://upload-images.jianshu.io/upload_images/5713484-fad20ba9b9c6c825.png)

PackageManagerService是通过套接字方式访问installd服务进程的，

### (二)、为什么要用intalld

有人会问了，PackageManageService这么大的组件了，为什么还需要intalld这个守护进程?这是因为权限的问题，PackageManagerService只有system权限。installd却是具有root权限。

如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-b249408e0740d499.png)

### (三)、intalld支持的命令

```cpp
struct cmdinfo cmds[] = {
    { "ping",                 0, do_ping }, // 用于测试的空操作
    { "install",              5, do_install }, // 安装应用
    { "dexopt",               9, do_dexopt }, //将dex转换为oat或者patchoat oat文件
    { "markbootcomplete",     1, do_mark_boot_complete },
    { "movedex",              3, do_move_dex },  //把apk文件从一个目录移动到另一个目录
    { "rmdex",                2, do_rm_dex }, // 删除apk文件
    { "remove",               3, do_remove }, // 卸载应用
    { "rename",               2, do_rename }, // 更改应用数据目录的名称
    { "fixuid",               4, do_fixuid }, // 更改应用数据目录的uid
    { "freecache",            2, do_free_cache }, // 清除/cache目录下的文件
    { "rmcache",              3, do_rm_cache }, // 删除/cache下某个应用的目录
    { "rmcodecache",          3, do_rm_code_cache }, // 删除数据目录中code_cache文件夹
    { "getsize",              8, do_get_size }, // 计算一个应用占用的空间大小，包括apk大小，数据目录，cache目录等
    { "rmuserdata",           3, do_rm_user_data },// 删除一个用户中某个app的应用数据
    { "cpcompleteapp",        6, do_cp_complete_app },
    { "movefiles",            0, do_movefiles },//执行/system/etc/updatecmds/中的脚本
    { "linklib",              4, do_linklib }, // 建立 jib连接
    { "mkuserdata",           5, do_mk_user_data },// 为某个用户创建应用数据目录
    { "mkuserconfig",         1, do_mk_user_config },// 创建/data/misc/user/userid/
    { "rmuser",               2, do_rm_user },// 删除一个user的所有文件
    { "idmap",                3, do_idmap },
    { "restorecondata",       4, do_restorecon_data },// 恢复目录的SEAndroid安全上下文
    { "createoatdir",         2, do_create_oat_dir }, // 创建 /data/app/包名/oat/<inst>文件夹
    { "rmpackagedir",         1, do_rm_package_dir },// 删除/data/app/包名
    { "linkfile",             3, do_link_file } // 创建软连接
};
```

此命令表总共有25条命令，该表中第二列是指命令所需的参数个数，第三列是指命令所指向的函数。不同的Android版本该表格都会有所不同。

不同Android版本中installd命令列表如下图：

![](https://upload-images.jianshu.io/upload_images/5713484-6e515407bfe98681.png)

### (四)、intalld启动流程

#### 1、启动

installd 是由 Android 系统 init 进程(pid=1)，在解析 init.rc 文件的代码时，通过 fork 创建用户空间的守护进程 intalld。

代码在[init.rc](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fsystem%2Fcore%2Frootdir%2Finit.rc) 687行

```kotlin
service installd /system/bin/installd
    class main
    socket installd stream 600 system system
```

installd是随着系统启动过程中的main class而启动的，并且会创建一个socket套接字，用于跟上层的PackageManagerService进行交互。installd的启动入口是frameworks/base/cmds/installd/installd.c的main()方法，接下来从这里开始说。

#### 2、installd的main方法

代码在[installd.cpp](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fnative%2Fcmds%2Finstalld%2Finstalld.cpp) 660行

```cpp
int main(const int argc __unused, char *argv[]) {
    char buf[BUFFER_MAX];
    struct sockaddr addr;
    socklen_t alen;
    int lsocket, s;
    int selinux_enabled = (is_selinux_enabled() > 0);

    setenv("ANDROID_LOG_TAGS", "*:v", 1);
    android::base::InitLogging(argv);

    ALOGI("installd firing up\n");

    union selinux_callback cb;
    cb.func_log = log_callback;
    selinux_set_callback(SELINUX_CB_LOG, cb);

    // 初始化全局变量
    if (initialize_globals() < 0) {
        ALOGE("Could not initialize globals; exiting.\n");
        exit(1);
    }

    // 初始化安装目录
    if (initialize_directories() < 0) {
        ALOGE("Could not create directories; exiting.\n");
        exit(1);
    }

    if (selinux_enabled && selinux_status_open(true) < 0) {
        ALOGE("Could not open selinux status; exiting.\n");
        exit(1);
    }

     // 取得installd套接字，系统中所有的socket以ANDROID_SOCKET_[name]为key，socket为为value的方式保存在 环境变量中
    lsocket = android_get_control_socket(SOCKET_PATH);
    if (lsocket < 0) {
        ALOGE("Failed to get socket from environment: %s\n", strerror(errno));
        exit(1);
    }

    // 监听socket消息
    if (listen(lsocket, 5)) {
        ALOGE("Listen on socket failed: %s\n", strerror(errno));
        exit(1);
    }

    // 修改该socket的属性
    fcntl(lsocket, F_SETFD, FD_CLOEXEC);

    for (;;) {
        alen = sizeof(addr);

       //接受socket客户端请求
        s = accept(lsocket, &addr, &alen);
        if (s < 0) {
            ALOGE("Accept failed: %s\n", strerror(errno));
            continue;
        }

         // 接收到客户端的请求后，修改客户端请求socket客户端
        fcntl(s, F_SETFD, FD_CLOEXEC);

        ALOGI("new connection\n");
         // 循环读取客户端socket中内容，直到读取内容为空为止
         // 客户端 发送的数据格式：数据长度 | 数据内容
        for (;;) {
            unsigned short count;
  
             // 读取数据长度，读取成功返回0，反之返回-1
            if (readx(s, &count, sizeof(count))) {
                ALOGE("failed to read size\n");
                break;
            }
             //如果读取成功，但是读取的数据长度超出1024字节，同样停止读取
            if ((count < 1) || (count >= BUFFER_MAX)) {
                ALOGE("invalid size %d\n", count);
                break;
            }

            // 读取指令内容，读取成功返回0，反之返回-1
            if (readx(s, buf, count)) {
                ALOGE("failed to read command\n");
                break;
            }
            buf[count] = 0;
            if (selinux_enabled && selinux_status_updated() > 0) {
                selinux_android_seapp_context_reload();
            }

            // 执行指令
            if (execute(s, buf)) break;
        }
        ALOGI("closing connection\n");
        //执行完客户端的请求后，关闭socket连接，继续进入接手请求模式
        close(s);
    }
    return 0;
}
```

该方法首先初始化一些变量就安装目录，然后从环境变量中取得installd套接字的句柄值，然后进入监听此socket，当客户端发送过来请求时，接收客户端的请求，并读取客户端发送过来的命令数据，并根据读取客户端命令来执行命令操作。这里面涉及到3个关键方法：

* initialize_globals()方法：初始化全局信息

* initialize_directories()方法：初始化相关目录

* static int execute(int s, char cmd[BUFFER_MAX])方法：执行指令

##### 2.1、 initialize_globals()方法

代码在[installd.cpp](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fnative%2Fcmds%2Finstalld%2Finstalld.cpp) 349行

```cpp
int initialize_globals() {
    // Get the android data directory.
    // 从环境变量中读取数据存储目录，在Android启动脚本init.rc中配置了ANDROID_DATA
     // 环境变量，export ANDORID_DATA  /data ，因此变量android_data_dir=/data/
    if (get_path_from_env(&android_data_dir, "ANDROID_DATA") < 0) {
        return -1;
    }

    // Get the android app directory.
    // app目录/data/app/
    if (copy_and_append(&android_app_dir, &android_data_dir, APP_SUBDIR) < 0) {
        return -1;
    }

    // Get the android protected app directory.
    // 得到应用程序私有目录 android_app_private_dir=/data/app-private/
    if (copy_and_append(&android_app_private_dir, &android_data_dir, PRIVATE_APP_SUBDIR) < 0) {
        return -1;
    }

    // Get the android app native library directory.
    // app 本地库目录 /data/app-lib/
    if (copy_and_append(&android_app_lib_dir, &android_data_dir, APP_LIB_SUBDIR) < 0) {
        return -1;
    }

    // Get the sd-card ASEC mount point.
    // 从环境变量中取得sd-card ASEC 挂载点，在启动脚本init.rc中也有配置：
    //  export ASEC_MOUNTPOINT  /mnt/asec/  因此android_asec_dir=/mnt/asec/
    if (get_path_from_env(&android_asec_dir, "ASEC_MOUNTPOINT") < 0) {
        return -1;
    }

    // Get the android media directory.
     // 多媒体目录 /data/media
    if (copy_and_append(&android_media_dir, &android_data_dir, MEDIA_SUBDIR) < 0) {
        return -1;
    }

    // Get the android external app directory.
    // 外部app 目录/mnt/expand
    if (get_path_from_string(&android_mnt_expand_dir, "/mnt/expand/") < 0) {
        return -1;
    }

    // Take note of the system and vendor directories.
    // 系统和厂商目录
    android_system_dirs.count = 4;

    android_system_dirs.dirs = (dir_rec_t*) calloc(android_system_dirs.count, sizeof(dir_rec_t));
    if (android_system_dirs.dirs == NULL) {
        ALOGE("Couldn't allocate array for dirs; aborting\n");
        return -1;
    }

    dir_rec_t android_root_dir;
     // 目录 /system/app
    if (get_path_from_env(&android_root_dir, "ANDROID_ROOT") < 0) {
        ALOGE("Missing ANDROID_ROOT; aborting\n");
        return -1;
    }
    
     // 目录 /system/app
    android_system_dirs.dirs[0].path = build_string2(android_root_dir.path, APP_SUBDIR);
    android_system_dirs.dirs[0].len = strlen(android_system_dirs.dirs[0].path);

     // 目录 /system/app-lib
    android_system_dirs.dirs[1].path = build_string2(android_root_dir.path, PRIV_APP_SUBDIR);
    android_system_dirs.dirs[1].len = strlen(android_system_dirs.dirs[1].path);

     //  目录 /vendor/app/
    android_system_dirs.dirs[2].path = strdup("/vendor/app/");
    android_system_dirs.dirs[2].len = strlen(android_system_dirs.dirs[2].path);

     //  目录 /oem/app/
    android_system_dirs.dirs[3].path = strdup("/oem/app/");
    android_system_dirs.dirs[3].len = strlen(android_system_dirs.dirs[3].path);

    return 0;
}
```

##### 2.2、 initialize_directories()方法

代码在[installd.cpp](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fnative%2Fcmds%2Finstalld%2Finstalld.cpp) 406行

```cpp
int initialize_directories() {
    int res = -1;

    // Read current filesystem layout version to handle upgrade paths
    // 读取当前文件系统版本
    char version_path[PATH_MAX];
    snprintf(version_path, PATH_MAX, "%s.layout_version", android_data_dir.path);

    int oldVersion;
    if (fs_read_atomic_int(version_path, &oldVersion) == -1) {
        oldVersion = 0;
    }
    int version = oldVersion;

    // /data/user
     // 目录 /data/user
    char *user_data_dir = build_string2(android_data_dir.path, SECONDARY_USER_PREFIX);

    // /data/data
     // 目录 /data/data
    char *legacy_data_dir = build_string2(android_data_dir.path, PRIMARY_USER_PREFIX);

    // /data/user/0
     // 目录/data/user/0
    char *primary_data_dir = build_string3(android_data_dir.path, SECONDARY_USER_PREFIX, "0");
    if (!user_data_dir || !legacy_data_dir || !primary_data_dir) {
        goto fail;
    }

    // Make the /data/user directory if necessary
    // 如果 /data/user 目录不存在，则创建目录
    if (access(user_data_dir, R_OK) < 0) {
        if (mkdir(user_data_dir, 0711) < 0) {
            goto fail;
        }
         // 修改目录权限及所有属性
        if (chown(user_data_dir, AID_SYSTEM, AID_SYSTEM) < 0) {
            goto fail;
        }
        if (chmod(user_data_dir, 0711) < 0) {
            goto fail;
        }
    }
    // Make the /data/user/0 symlink to /data/data if necessary
    // 将/data/user/0 链接到 /data/data
    if (access(primary_data_dir, R_OK) < 0) {
        if (symlink(legacy_data_dir, primary_data_dir)) {
            goto fail;
        }
    }

    if (version == 0) {
        // Introducing multi-user, so migrate /data/media contents into /data/media/0
        ALOGD("Upgrading /data/media for multi-user");

        // Ensure /data/media
        if (fs_prepare_dir(android_media_dir.path, 0770, AID_MEDIA_RW, AID_MEDIA_RW) == -1) {
            goto fail;
        }

        // /data/media.tmp
        char media_tmp_dir[PATH_MAX];
        snprintf(media_tmp_dir, PATH_MAX, "%smedia.tmp", android_data_dir.path);

        // Only copy when upgrade not already in progress
        if (access(media_tmp_dir, F_OK) == -1) {
            if (rename(android_media_dir.path, media_tmp_dir) == -1) {
                ALOGE("Failed to move legacy media path: %s", strerror(errno));
                goto fail;
            }
        }

        // Create /data/media again
        if (fs_prepare_dir(android_media_dir.path, 0770, AID_MEDIA_RW, AID_MEDIA_RW) == -1) {
            goto fail;
        }

        if (selinux_android_restorecon(android_media_dir.path, 0)) {
            goto fail;
        }

        // /data/media/0
        char owner_media_dir[PATH_MAX];
        snprintf(owner_media_dir, PATH_MAX, "%s0", android_media_dir.path);

        // Move any owner data into place
        if (access(media_tmp_dir, F_OK) == 0) {
            if (rename(media_tmp_dir, owner_media_dir) == -1) {
                ALOGE("Failed to move owner media path: %s", strerror(errno));
                goto fail;
            }
        }

        // Ensure media directories for any existing users
        DIR *dir;
        struct dirent *dirent;
        char user_media_dir[PATH_MAX];

        dir = opendir(user_data_dir);
        if (dir != NULL) {
            while ((dirent = readdir(dir))) {
                if (dirent->d_type == DT_DIR) {
                    const char *name = dirent->d_name;

                    // skip "." and ".."
                    if (name[0] == '.') {
                        if (name[1] == 0) continue;
                        if ((name[1] == '.') && (name[2] == 0)) continue;
                    }

                    // /data/media/<user_id>
                    snprintf(user_media_dir, PATH_MAX, "%s%s", android_media_dir.path, name);
                    if (fs_prepare_dir(user_media_dir, 0770, AID_MEDIA_RW, AID_MEDIA_RW) == -1) {
                        goto fail;
                    }
                }
            }
            closedir(dir);
        }

        version = 1;
    }
```

##### 2.3、execute(int s, char cmd[BUFFER_MAX])方法方法

代码在[installd.cpp](https://link.jianshu.com/?t=http%3A%2F%2Fandroidxref.com%2F6.0.1_r10%2Fxref%2Fframeworks%2Fnative%2Fcmds%2Finstalld%2Finstalld.cpp) 265行

```cpp
/* Tokenize the command buffer, locate a matching command,
 * ensure that the required number of arguments are provided,
 * call the function(), return the result.
 */
static int execute(int s, char cmd[BUFFER_MAX])
{
    char reply[REPLY_MAX];
    char *arg[TOKEN_MAX+1];
    unsigned i;
    unsigned n = 0;
    unsigned short count;
    int ret = -1;

    // ALOGI("execute('%s')\n", cmd);

        /* default reply is "" */
    reply[0] = 0;

        /* n is number of args (not counting arg[0]) */
    // arg[0] 为命令名称，命令格式：[name arg1 arg2 arg3 arg4]
    arg[0] = cmd;
    // 计算命令参数个数
    while (*cmd) {
        if (isspace(*cmd)) {
            *cmd++ = 0;
            n++;
            arg[n] = cmd;
            if (n == TOKEN_MAX) {
                ALOGE("too many arguments\n");
                goto done;
            }
        }
        if (*cmd) {
          // 计算参数个数
          cmd++;
        }
    }
    // 根据命令名称匹配命令数组cmds中命令
    for (i = 0; i < sizeof(cmds) / sizeof(cmds[0]); i++) {
         // 命令名称比较
        if (!strcmp(cmds[i].name,arg[0])) {
             // 判断该命令的参数个数是否满足要求
            if (n != cmds[i].numargs) {
                // 参数不匹配，直接返回
                ALOGE("%s requires %d arguments (%d given)\n",
                     cmds[i].name, cmds[i].numargs, n);
            } else {
                // 执行相应的命令
                ret = cmds[i].func(arg + 1, reply);
            }
            goto done;
        }
    }
    ALOGE("unsupported command '%s'\n", arg[0]);

done:
    // 格式化返回结果
    if (reply[0]) {
        n = snprintf(cmd, BUFFER_MAX, "%d %s", ret, reply);
    } else {
        n = snprintf(cmd, BUFFER_MAX, "%d", ret);
    }
    if (n > BUFFER_MAX) n = BUFFER_MAX;
     // 返回结果数据长度
    count = n;

    // ALOGI("reply: '%s'\n", cmd);
    // 写结果数据长度
    if (writex(s, &count, sizeof(count))) return -1;
      // 写结果数据
    if (writex(s, cmd, count)) return -1;
    return 0;
}
```

### (五)、总结

PMS启动过程中使用了Installer的多个方法。Android APK的安装和卸载主要是由Installer和Installd完成的。Installer是Java层提供的Java API接口，Installd则是init进程启动的Daemon Service。Installer与Installd通过Socket通信，Installer是Socket的Client端，Installd则是Socket的Server端。通过Socket通信，将Installer的API调用转化为Installd中具体命令，这种转化关系通过cmds[]数组配置和映射。Installer和Installd的关系如图所示：

![](https://upload-images.jianshu.io/upload_images/5713484-eb7d9c110bb32fa9.png)

## 参考文章

1. [APK安装流程详解5——Installer、InstallerConnection和Installd守护进程](https://www.jianshu.com/p/df99b744ccc3)

