# Room 数据库操作

## 1. 介绍

关于 Room 数据的介绍可以看 Android 官方地址：https://developer.android.google.cn/training/data-storage/room?hl=zh_cn#java

Room 持久性库在 SQLite 上提供了一个抽象层，以便在充分利用 SQLite 的强大功能的同时，能够流畅地访问数据库。具体来说，Room 具有以下优势：

- 针对 SQL 查询的编译时验证。
- 可最大限度减少重复和容易出错的样板代码的方便注解。
- 简化了数据库迁移路径。

Room 是 Jetpack 组件中一个对象关系映射（ORM）库。可以很容易将 SQLite 表数据转换为 Java 对象。

Room 在 SQLite 上提供了一个抽象层，以便在充分利用 SQLite 的强大功能的同时，能够流畅地访问数据库。

支持与 LiveData、RxJava、Kotlin 协成组合使用。

Google 官方强烈推荐使用Room。

### 1.1. 框架特点

相对于 SQLiteOpenHelper 等传统方法，使用 Room 操作 SQLite 有以下优势：

* 编译期的 SQL 语法检查
* 开发高效，避免大量模板代码
* API 设计友好，容易理解
* 可以与 RxJava、 LiveData 、 Kotlin Coroutines 等进行桥接 

### 1.2. 主要组件

Room 包含三个主要组件：

- [数据库类](https://developer.android.google.cn/reference/kotlin/androidx/room/Database?hl=zh-cn)(DataBase)，用于保存数据库并作为应用持久性数据底层连接的主要访问点。
- [数据实体](https://developer.android.google.cn/training/data-storage/room/defining-data?hl=zh-cn)(Entity)，用于表示应用的数据库中的表。
- [数据访问对象 (DAO)](https://developer.android.google.cn/training/data-storage/room/accessing-data?hl=zh-cn)，提供您的应用可用于查询、更新、插入和删除数据库中的数据的方法。

数据库类为应用提供与该数据库关联的 DAO 的实例。反过来，应用可以使用 DAO 从数据库中检索数据，作为关联的数据实体对象的实例。此外，应用还可以使用定义的数据实体更新相应表中的行，或者创建新行供插入。图 1 说明了 Room 的不同组件之间的关系。

![](img/room_architecture.png)

Room 库架构的示意图。

## 2. 使用

### 2.1. 添加依赖

```groovy
def room_version ="2.1.0-alpha04"

implementation"androidx.room:room-runtime:$room_version"

annotationProcessor"androidx.room:room-compiler:$room_version"
```

使用前需要先准备数据实体，数据访问对象（DAO），数据库。

### 2.2. 数据实体

```java
@Entity(tableName = "db_testData") // 定义表名
public class testData {
    @Ignore
    private String content = "RoomDataBase数据类"; // 如果不是数据库字段 @Ignore 忽视
    
    @PrimaryKey(autoGenerate = true)
    public Integer userId; // 设置主键，主键自动增长
    
  	@NonNull
    public String name;
    public int age;
  	@ColumnInfo(name  = "kind1")
    public String kind;
    
    public testData(){
        
    }
    
    @Ignore
    public testData(String name,int age,String kind){
        this.name = name;
        this.age = age;
        this.kind = kind;
    }
}
```

必须定义构造方法，不然会报错。

- @Entity： 代表一个表中的实体，默认类名就是表名，如果不想使用类名作为表名，可以给注解添加表名字段 @Entity(tableName = "user")
- @PrimaryKey： 每个实体都需要自己的主键
- @NonNull 表示字段，方法，参数返回值不能为空
- @ColumnInfo(name = “kind1”) 如果希望表中字段名跟类中的成员变量名不同，添加此字段指明

### 2.3. 数据访问对象（DAO）

注意：返回主键 ID 类型与数据实体中的主键类型对象。或者实体中的类型是 Int，插入成功返回 Long/Int，不能相反。

```java
@Dao
public interface dbDao {
  	// 插入是，id不用传，会自动增长，建议不传
    @Insert
    void insertTestData(TestData testData);
    
    @Query("select * from db_testData where name=:name1 Order by userId desc limit 1")
    TestData selectTestData(String name1);
    
    @Query("select * from db_testData")
    List<TestData> selectTestDataList();
    
    @Query("update db_testData set name=:name1 where userId=:id1")
    void upDataTestData(String name1,Integer id1);
    
    @Query("delete from db_testData where userId=:id1")
    void deleteTestData(Integer id1);
    
    @Update()
    void updateAlarmInfoData(TestData testData);
    
}
```

- DAO 是数据访问对象，指定 SQL 查询，并让他与方法调用相关联。
- DAO 必须是一个接口或者抽象类。
- 默认情况下，所有的查询都必须在单独的线程中执行

### 2.4. 数据库

```java
/**
 * Database 数据库相关的类字段做了修改都需要升级版本，不然出错
 * entities 需要关联的数据类集合，支持多个
 * version  版本号
 * exportSchema 是否支持保留历史记录
 */
@Database(entities = {TestData.class}, version = 1, exportSchema = false)
public abstract class LocalRoomDataBase extends RoomDatabase {
    public abstract dbDao getTestDataDao(); // 数据库相关操作 Dao，可以定义多个	
}

```

- 创建一个抽象类继承自 RoomDatabase
- 给他添加一个注解 @Database 表明它是一个数据库，注解有两个参数第一个是数据库的实体，它是一个数组，可以传多个，当数据库创建的时候，会默认给创建好对应的表，第二个参数是数据库的版本号

### 2.5. 定义外部调用方法

```java
/**
 * 初始化 roomDataBase 并且创建 sql 语句方法
 */
public class LocalRoomUserDao {
    private static String db_name = "db_mvp";
    // 单例对象
    private static LocalRoomUserDao roomUserDao;
    private static LocalRoomDataBase roomDataBase;

    public static LocalRoomUserDao getInstance(Context context){
        if (roomUserDao == null){
            // 获取单例，双重验证线程安全
            synchronized (LocalRoomUserDao.class){
                if (roomUserDao == null){
                    roomUserDao = new LocalRoomUserDao();
                }
            }
        }
        if(roomDataBase == null){
            synchronized (LocalRoomUserDao.class){
                if(roomDataBase == null){
                    roomDataBase = Room.databaseBuilder(context.getApplicationContext(),LocalRoomDataBase.class,db_name)
                    .allowMainThreadQueries() // 允许主线程调用
                    .fallbackToDestructiveMigration() // 数据库版本迁移异常，重新创建方式崩溃
                    .build();
                }
            }
        }
        return roomUserDao;
    }

    public void insertTestData(TestData testData){
        roomDataBase.getTestDataDao().insertTestData(testData);
    }

    public TestData selectTestData(String name){
        return roomDataBase.getTestDataDao().selectTestData(name);
    }

}
```

* 定义跟数据库一起使用的相关的DAO类
* 创建一个LocalRoomUserDao 的单例，防止同时打开多个数据库的实例
* 使用Room提供的数据库构建器来创建该实例，第一个参数application，第二个参数当前数据库的实体类，第三个参数数据库的名字
* exportSchema = true 支持导出 Room 生成的配置文件

```groovy
      javaCompileOptions {
          annotationProcessorOptions {
              arguments = ["room.schemaLocation": "$projectDir/schemas".toString()]
          }
      }
```

Room 的操作都需要在子线程执行，如果需要在主线程执行需要设置 allowMainThreadQueries() 方法。

```java
  roomDataBase = Room.databaseBuilder(context.getApplicationContext(),LocalRoomDataBase.class,db_name)
                    .allowMainThreadQueries()
                    .addMigrations(LocalRoomDataBase.updateMigration) // 版本迁移调用
                    .fallbackToDestructiveMigration() // 数据库版本迁移异常，重新创建方式崩溃
                            .allowMainThreadQueries()
                    .build();
```

### 2.6. 使用

```java
        LocalRoomUserDao userDao = LocalRoomUserDao.getInstance(this);
        userDao.insertTestData(new TestData("1",1,"1"));
        userDao.selectTestData("1");
```

## 3. 更新数据库

当需要在原有的数据库里添加表或者在原有的表里添加新的字段时，需要更新数据库才能使新加的内容得以应用。

### 3.1. 自动迁移

注意：Room 在 2.4.0-alpha01 及更高版本中支持自动迁移。如果应用使用的是较低版本的 Room，则必须[手动定义迁移](https://developer.android.google.cn/training/data-storage/room/migrating-db-versions?hl=zh_cn#manual)。

不建议使用这种方式，因为高版本的 Room 需要更新 Kotlin 版本，Kotlin 插件版本，Gradle 版本等，而且还要升级 Android Studio。

旧版本代码：

```java
@Database(entities = [ RunRecord::class], version = 1)
abstract class YzDatabase : RoomDatabase() {...}
```

新版本代码：

```java

@Database(entities = [ RunRecord::class], version = 2,
    autoMigrations = [AutoMigration (from = 1, to = 2)])
abstract class YzDatabase : RoomDatabase() {}
```

### 3.2. 手动迁移

比如在 TestData 中增加个性别的字段：

```java
@Entity(tableName = "db_testData") // 定义表名
public class TestData {
		...
    private String sex;
		...
}
```

修改 LocalRoomDataBase 类，版本 +1

```java
@Database(entities = {TestData.class}, version = 2, exportSchema = false)
public abstract class LocalRoomDataBase extends RoomDatabase {
    public abstract dbDao getTestDataDao(); // 数据库相关操作 Dao，可以定义多个

    static final Migration updateMigration = new Migration(1, 2) { // 当前版本号跟目标版本号
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 需要执行的语句执行一次就可以了
            database.execSQL("ALTER TABLE db_testData ADD COLUMN sex TEXT");
        }
    };
}
```

然后将 Migration 添加到数据库构建中：

```java
 roomDataBase = Room.databaseBuilder(context.getApplicationContext(),LocalRoomDataBase.class,db_name)
                    .allowMainThreadQueries()
                    .addMigrations(LocalRoomDataBase.updateMigration) // 版本迁移调用
                    .fallbackToDestructiveMigration() // 数据库版本迁移异常，重新创建方式崩溃
                            .allowMainThreadQueries()
                    .build();
```

## 4. Room 关联表

### 4.1. 关联表配置

* foreignKeys 配置外键
* parentColumns：父表外键
* childColumns：子表外键
* NO_ACTION: parent表中某行被删掉(更新)后。child表中与parent这一行发生映射的行不发生任何改变
* RESTRICT: parent表中想要删除(更新)某行。如果child表中有与这一行发生映射的行。那么改操作拒绝。
* SET_NULL/SET_DEFAULT:parent表中某行被删掉(更新)后。child表中与parent这一行发生映射的行设置为NULL(DEFAULT)值。
* CASCADE:parent表中某行被删掉(更新)后。child表中与parent这一行发生映射的行被删掉(其属性更新到对应设置)

```java
    @Entity(tableName = FaceModel.FACE_TABLE_NAME,
        foreignKeys = {
                @ForeignKey(entity = UserModel.class,
                        parentColumns = "faceId",
                        childColumns = "faceId",
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index(value = {"faceId"})}
    )
```

### 4.2. 创建嵌套对象

```java
    public class UserAndFaceModel {
    
        @Relation(parentColumn = "faceId", entityColumn = "faceId", entity = FaceModel.class)
        public List<FaceModel> faceModels;
    
        @Embedded
        public UserModel userModel;
    
    }
```

### 4.3. 创建关联 Dao

```java
@Dao
public interface UserAndFaceDao {

    @Transaction // 保障事务
    @Query("SELECT * FROM "+ UserModel.USER_TABLE_NAME + " WHERE "+
            UserModel.NAME + " = :name")
    LiveData<UserAndFaceModel> queryByName2Lv(String name);

    @Transaction
    @Query("SELECT * FROM "+ UserModel.USER_TABLE_NAME + " WHERE "+
            UserModel.NAME + " = :name")
    UserAndFaceModel queryByName2Model(String name);

    @Transaction
    @Query("SELECT * FROM "+ UserModel.USER_TABLE_NAME + " WHERE "+
            UserModel.FACE_ID + " = :faceId")
    LiveData<UserAndFaceModel> queryByFaceId2Lv(String faceId);

    @Transaction
    @Query("SELECT * FROM "+ UserModel.USER_TABLE_NAME + " WHERE "+
            UserModel.FACE_ID + " = :faceId")
    UserAndFaceModel queryByFaceId2Model(String faceId);

    @Transaction
    @Query("SELECT * FROM "+ UserModel.USER_TABLE_NAME )
    List<UserAndFaceModel> queryAll();

}
```

### 4.4. 关联表数据插入注意

* 保障事务

```java
RoomDemoDatabase.getInstance(MainActivity.this.getApplicationContext()).runInTransaction(new Runnable() {
            @Override
            public void run() {
                UserModel userModel = new UserModel(1, "1", "2");
                RoomDemoDatabase.getInstance(MainActivity.this.getApplicationContext()).userDao().insertUser(userModel);
                FaceModel faceModel = new FaceModel("fa", 1, "fa");
                RoomDemoDatabase.getInstance(MainActivity.this.getApplicationContext()).faceDao().insertFace(faceModel);
            }
        });
```

## 5. 数据库数据加密

### 5.1. 文件加密

#### SQLCipher

```undefined
SQLCipher 是一个在 SQLite 基础之上进行扩展的开源数据库，它主要是在 SQLite 的基础之上增加了数据加密功能。
```

* 加密性能高、开销小，只要 5-15% 的开销用于加密。

* 完全做到数据库 100% 加密
* 采用良好的加密方式（CBC加密模式——密文分组链接模式）
* 使用方便，做到应用级别加密
* 采用 OpenSSL 加密库提供的算法
* 开源，支持多平台

#### Realm

##### Realm 介绍

* Realm 是一个 MVCC （多版本并发控制）数据库，

* 由 Y Combinator 公司在 2014 年 7 月发布一款支持运行在手机、平板和可穿戴设备上的嵌入式数据库，目标是取代 SQLite。
* Realm 本质上是一个嵌入式数据库，他并不是基于 SQLite 所构建的。
* 它拥有自己的数据库存储引擎，可以高效且快速地完成数据库的构建操作。
* 和 SQLite 不同，它允许你在持久层直接和数据对象工作。
* 在它之上是一个函数式风格的查询 api，众多的努力让它比传统的 SQLite 操作更快 。

##### Realm 加密

* 借助 Realm，我们可以轻松地进行加密，因为我们可以轻松地决定数据库内核所应该做的事情。
* 内部加密和通常在 Linux 当中做的加密哪样很类似。因为我们对整个文件建立了内存映射，因此我们可以对这部分内存进行保护。
* 如果任何人打算读取这个加密的模块，我们就会抛出一个文件系统警告“有人正视图访问加密数据。只有解密此模块才能够让用户读取。”通过非常安全的技术我们有一个很高效的方式来实现加密。加密并不是在产品表面进行的一层封装，而是在内部就构建好的一项功能。

### 5.2. 内容加密

- 在存储数据时加密内容，在查询时进行解密。但是这种方式不能彻底加密，数据库的表结构等信息还是能被查看到，另外检索数据也是一个问题。

| 加密算法  | 描述                                                         | 优点                                               | 缺点                                                         |
| :-------- | :----------------------------------------------------------- | :------------------------------------------------- | :----------------------------------------------------------- |
| DES，3DES | 对称加密算法                                                 | 算法公开、计算量小、加密速度快、加密效率高         | 双方都使用同样密钥，安全性得不到保证                         |
| AES       | 对称加密算法                                                 | 算法公开、计算量小、加密速度快、加密效率高         | 双方都使用同样密钥，安全性得不到保证                         |
| XOR       | 异或加密                                                     | 两个变量的互换（不借助第三个变量），简单的数据加密 | 加密方式简单                                                 |
| Base64    | 算不上什么加密算法，只是对数据进行编码传输                   |                                                    |                                                              |
| SHA       | 非对称加密算法。安全散列算法，数字签名工具。著名的图片加载框架 Glide 在缓存 key 时就采用的此加密 | 破解难度高，不可逆                                 | 可以通过穷举法进行破解                                       |
| RSA       | 非对称加密算法，最流行的公钥密码算法，使用长度可变的秘钥     | 不可逆，既能用于数据加密，也可以应用于数字签名     | RSA 非对称加密内容长度有限制，1024 位 key 的最多只能加密 127 位数据 |
| MD5       | 非对称加密算法。全程：Message-Digest Algorithm，翻译为消息摘要算法 | 不可逆，压缩性，不容易修改，容易计算               | 穷举法可以破解                                               |

### 5.3. Room 数据库数据库加密

- SQLCipher 并不直接支持 Room 的数据库进行加密，所以没法直接实现。
- 可以通过开源库([swac-saferoom](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fcommonsguy%2Fcwac-saferoom))进行数据加密（底层也是通过SQLCipher对数据库文件加密）

#### 集成 swag-saferoom

```groovy
添加 maven { url "https://s3.amazonaws.com/repo.commonsware.com" }
dependencies {
  implementation 'com.commonsware.cwac:saferoom:1.1.3'
}
```

#### 添加 openHelperFactory

```java
    private static AppDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
            .allowMainThreadQueries()
            .openHelperFactory(new SafeHelperFactory("123456".toCharArray()))
            .addCallback(new Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                
                }

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                }
              
            })
            .build();
    }
```

## 6. Room 与其他数据库对比

| 参数            | Room  | GreenDao | Realm |
| :-------------- | :---- | :------- | :---- |
| 集成包大小      | 0.05M | 0.05M    | 9.06M |
| 插入10000条速度 | 551ms | 806ms    | 195ms |
| 查询10000条速度 | 126ms | 71ms     | 4ms   |
| 删除10000条速度 | 3ms   | 6ms      | 5ms   |
| 更新10000条速度 | 622ms | 838ms    | 242ms |

## 7. 数据库调试工具

Android-Debug-Database:https://github.com/amitshekhariitbhu/Android-Debug-Database

使用debug-db 可以在浏览器查看表结构及数据

### 普通数据库

```bash
    - implementation 'com.amitshekhar.android:debug-db:1.0.6'
```

### 加密数据库

```bash
    debug {
        resValue("string", "PORT_NUMBER", "8081")
        resValue("string", "DB_PASSWORD_PERSON", "123456")
    }
    
    implementation 'com.amitshekhar.android:debug-db-encrypt:1.0.6'
```

## 参考文章

1. [安卓room 数据库操作](https://www.jianshu.com/p/32cce7d4d893)
2. [Room数据库使用与踩坑（最新）](https://blog.csdn.net/wumeixinjiazu/article/details/123382721)
3. [Room使用详解及常用数据库对比](https://www.jianshu.com/p/bc6cc48ffa67 )

