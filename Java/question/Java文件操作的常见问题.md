# Java 文件操作的常见问题

## 1. 读取一个文件，里面有数字，然后数字使用换行符隔开。要求：读取文件，去除其中的重复数组，然后将去重的数字写到文件中。

### 1.1. 解决思路

这个问题解决分为三个步骤：

1. 读取文件，得到数据。
2. 将数据中重复的数据进行剔除。
3. 将数字写入到文件中。

### 1.2. 代码实现

#### 1.2.1. 从文件中读取数据

```java
    public static List<Integer> readFileNum(String fileName) {
        List<Integer> numList = new ArrayList<>();
        File file = new File(fileName);
        StringBuffer sb = new StringBuffer();
        if (file.isFile() && file.exists()) {
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                while ((byteread = in.read(tempbytes)) != -1) {
                    for (int i = 0; i < byteread; i++) {
                        if (tempbytes[i] != '\n') {
                            if (tempbytes[i] >= '0' && tempbytes[i] <= '9') {
                                sb.append(tempbytes[i] - '0');
                            }
                        } else {
                            String numStr = sb.toString();
                            if (numStr != null && !"".equals(numStr)) {
                                int num = Integer.parseInt(numStr);
                                numList.add(num);
                            }
                            sb.delete(0, sb.length());
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return numList;
    }
```

#### 1.2.2. 剔除重复数字

这个问题有两种解法：

1. 两层嵌套循环，第一个指针遍历整个链表，第二个指针将第一个指针后面的与它相同的数字进行移除。
2. 先对链表进行排序，这样相同的数字就会聚集在一起，这样用一层循环就可以移除，一个指针指向的数字与前一个数字相同，则移除。

##### 1.2.2.1. 方法一实现

```java
    public static void removeSame(List<Integer> numList) {
        int i = 0;
        while (i < numList.size()) {
            int num = numList.get(i);
            if (i + 1 < numList.size()) {
                numList.subList(i + 1, numList.size()).remove((Object) num);
            }
            i++;
        }
    }
```

##### 1.2.2.2. 方法二实现

```java
    public static void removeSame2(List<Integer> numList) {
        // 先对链表进行排序
        numList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        System.out.println("removeSame2. numList:" + numList);
        int i = 0;
        while (i < numList.size()) {
            int num = numList.get(i);
            if (i - 1 >= 0) {
                // 如果与前一个数字相同，则移除
                if (num == numList.get(i - 1)) {
                    numList.remove(i);
                }
            }
            i++;
        }
    }
```

#### 1.2.3. 将数字写回文件

```java
    public static void writeFile(String fileName, List<Integer> numList) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numList.size(); i++) {
            sb.append(numList.get(i));
            if (i != numList.size() - 1) {
                sb.append('\n');
            }
        }
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            FileWriter wf = null;
            try {
                wf = new FileWriter(fileName);
                wf.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (wf != null) {
                    try {
                        wf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
```

