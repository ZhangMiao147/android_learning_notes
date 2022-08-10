# Android 安装 apk

## 安装

7.0以上安装APK，需要自行配置FileProvider：

```xml
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths" />
        </provider>
```

xml/filepaths.xml 如下：

```xml
       <?xml version="1.0" encoding="utf-8"?>
       <paths xmlns:android="http://schemas.android.com/apk/res/android">
            <files-path
                  name="newApk"
                  path="newApk0/" /> #我是放在私有目录的files/newApk0下的，请根据自身要求修改
       </paths>
```

7.0以下安装APK，经常会遇到“安装包找不到”或“解析包失败等原因”，分析原因：因为我下载的APK是保存在私有目录data/data/{packageName}/files/{自定义文件夹}内的，Android的安装程序在加载安装包的时候，没有访问这个目录的权限，所以要给这个目录修改下访问权限。

安装的具体代码：

```java
    public static void install(Context context, File apkFile) {
        if (context == null || apkFile == null || !apkFile.exists()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            openInstallPage(context, FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile));
        } else {
            // 这一步很重要，android 6.0 及以下，需要获取文件全路径的文件权限
            chmodFullFile(context, apkFile);
        }
    }

    /**
     * 修改file全路径的读写权限
     *
     * @param file
     */
    private static void chmodFullFile(final Context context, final File file) {
        if (file == null) {
            return;
        }
        ArrayList<String> commands = new ArrayList<>();
        getAllCommand(commands, file);
        if (commands.size() > 0) {
            String[] c = new String[commands.size()];
            for (int i = 0; i < commands.size(); i++) {
                c[i] = commands.get(i);
                System.out.println(commands.get(i));
            }
            ProcessUtils.executeProcess(c, null, 5000, new ProcessUtils.ProcessListener() {
                @Override
                public void finish() {
                    openInstallPage(context, Uri.fromFile(file));
                }
            });
        }

    }

    private static void getAllCommand(ArrayList<String> command, File file) {
        if (file == null) {
            return;
        }
        command.add("chmod 777 " + file.getAbsolutePath());
        getAllCommand(command, file.getParentFile());
    }

    /**
     * 打开apk安装界面
     *
     * @param context
     * @param apkUri
     */
    private static void openInstallPage(Context context, Uri apkUri) {
        Intent intent = new Intent();
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
```

ProcessUtils工具类如下：

```java
public class ProcessUtils {

    interface ProcessListener {
        void finish();
    }

    /**
     * 执行Process命令 cmd
     *
     * @param command         指令
     * @param o
     * @param timeout         超时时间
     * @param processListener 回调
     * @desc 注意：回调没有切换线程
     */
    public static void executeProcess(final String[] command, Object o, final long timeout, final ProcessListener processListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String cmd : command) {
                    exec(cmd, timeout);
                }
                processListener.finish();
            }
        }).start();
    }

    private static void exec(String command, long timeout) {
        Process process = null;
        Worker worker = null;
        try {
            process = Runtime.getRuntime().exec(command);
            worker = new Worker(process);
            worker.start();
            worker.join(timeout);
        } catch (InterruptedException | IOException ex) {
            if (worker != null) {
                worker.interrupt();
            }
            Thread.currentThread().interrupt();
            ex.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static class Worker extends Thread {
        private final Process process;

        private Worker(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            InputStream errorStream = null;
            InputStream inputStream = null;
            try {
                errorStream = process.getErrorStream();
                inputStream = process.getInputStream();
                readStreamInfo(errorStream, inputStream);
                process.waitFor();
                process.destroy();
            } catch (InterruptedException ignore) {
            }
        }
    }

    /**
     * 读取RunTime.exec运行子进程的输入流 和 异常流
     *
     * @param inputStreams 输入流
     */
    private static void readStreamInfo(InputStream... inputStreams) {
        ExecutorService executorService = Executors.newFixedThreadPool(inputStreams.length);
        for (InputStream in : inputStreams) {
            executorService.execute(new CacheConsumer(in));
        }
        executorService.shutdown();
    }

    /**
     * Process缓存消费者
     */
    private static class CacheConsumer implements Runnable {
        private InputStream in;

        CacheConsumer(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "GBK"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line).append("\n");
                }
                if (result.length() > 0) {
                    Log.i("ProcessCacheConsumer", "==> " + result.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

## 安装系统广播

* 安装
  * Intent.ACTION_PACKAGE_ADDED
* 更新
  * Intent.ACTION_PACKAGE_REMOVED
  * intent:android.intent.action.PACKAGE_ADDED
  * Intent.ACTION_PACKAGE_REPLACED
* 卸载
  * Intent.ACTION_PACKAGE_REMOVED
  * Intent.ACTION_PACKAGE_REPLACED

## 参考文章

1. [Android安装APK](https://www.jianshu.com/p/a18834f43f57)

