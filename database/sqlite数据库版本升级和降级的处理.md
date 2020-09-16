# SQL 数据库升级和降级的处理

一、SQLite升级和降级需要考虑的细节

​       ① SQLite升级：

​               v3.0数据库版本 [onUpgrade 情况：n-1,onCreate 情况：1]
​               \1. v1.0 -->v3.0 onUpgrade 
​                  alter table t_message add column isdel bit default 0;
​                    插入数据
​               \2. v2.0 -->v3.0 onUpgrade 
​                    alter table t_message add column isdel bit default 0;*
\*               3. 没有安装过 onCreate()

 

​       ② SQLite 降级：

​                降级由于情况太多，复杂所以我们只做**数据库V3.0降级到数据库V2.0：**

​                         降级的设计关键点
​               1、考虑云端要保存用户【自定义数据、行为习惯】。专业术语profile-->>提高用户黏度
​               2、考虑[当前]的最低版本要求-->>降低维护成本
​               3、尽可能本地的数据转移（所有新版本，都不删除字段）-->尽可能把未知变已知
​               try catch

​      ③onCreate（）和onUpgrade（）升级，onDowngrade（）降级方法执行的时机：

​          在上一张章onCreate和onUpgrade方法执行的时机博客中我们知道，先获取数据库的当前版本，当版本号为0的时候，就会执行onCreate方法(当数据库文件第一次创建的时候版本号就是0)如果版本号不为0，同时和最新版本号进行比较，如果大于的话，就执行升级操作onUpgrade方法，否则就执行降级onDowngrade方法。

二、相关代码：

  **1、升级：**

​       创建一个openHelper3.java

  openHelper3.java文件：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
public class openHelper3 extends SQLiteOpenHelper {

    private static final String DB_NAME = "mydata.db"; // 数据库名称
    private static final int version = 1; // 数据库版本
    public openHelper3(Context context) {
        super(context, DB_NAME, null, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        // 编写【从0开始到最新状态】建表语句
        Log.i("hi", "没有数据库，创建v4.0数据库");                                                     
        //0-->v3.0
        String sql_message = "create table t_message (id int primary key,userName varchar(50),lastMessage varchar(50),datetime  varchar(50),isdel bit default 0)";
        String inse="insert into t_message values(1,'TT','一起去旅游','10月1号',0)";
        db.execSQL(sql_message);
        
        db.execSQL(inse);
        Log.i("d","ok");
    }
      //升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
             //v1.0-->v3.0
                if(oldVersion==1)
                {
                    String inse2="insert into t_message values(2,'TT2','一起去旅游','10月1号',0)";
                    String inse3="insert into t_message values(3,'TT3','一起去旅游','10月1号',0)";
                    db.execSQL(inse2);
                    db.execSQL(inse3);
                    Log.i("hi", "升级v1.0-->v3.0数据库");
                }
                //v2.0--->3.0
                if(oldVersion==2)
                {
                    String sql="alter table t_message add column isdel bit default 0";
                    db.execSQL(sql);
                    Log.i("hi", "升级v2.0-->v3.0数据库");
                }
    }
}
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

  1、该类继承SQLiteOpenHelper对数据库进行相关的操作。

  2、在onCreate（）的方法创建表和添加数据，首先我们知道有3种情况 0-->v3.0，1-->v3.0，2-->v3.0。

  3、onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)升级方法,在onCreate（）和onUpgrade，onDowngrade（）方法执行的时机知识点我们知道了执行顺序。所以在onCreate（）方法中我们用来创建最新版本也就是0-->v3.0。

  4、在onUpgrade()方法中我们可以通过oldVersion（旧版本）参数来进行判断，进行相关的操作。

 

  **2、降级：**

​      降级操作我们只需在 openHelper3.java文件文件中添加一个onDowngrade（）降级方法。

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        
        try {
            //第一、先把t_message 未来的表，改名
            String alt_table="alert table t_message rename to t_message_bak";
            db.execSQL(alt_table);
            //第二、建立2.0的表结构
            String crt_table = "create table t_message (id int primary key,,userName varchar(50),lastMessage varchar(50),datetime  varchar(50))";
            db.execSQL(crt_table);
            //第三、把备份的数据，copy到 新建的2.0的表
            String cpy_table="insert into t_message select id,userName,lastMessage,datetime from t_message_bak";
            db.execSQL(cpy_table);
            //第四、把备份表drop掉
            String drp_table = "drop table if exists t_message_bak";
            db.execSQL(drp_table);
            
        } catch (Exception e) {
            
            //失败
            Log.i("hi", "降级失败，重新建立");
            String sql_drop_old_table = "drop table if exists t_message";
            String sql_message = "create table t_message (id int primary key,userName varchar(50),lastMessage varchar(50),datetime  varchar(50))";
            String sql_init_1 = "insert into t_message values (2,'TT2','一起去旅游','10月1号')";
            String sql_init_2 = "insert into t_message values (2,'TT2','一起去旅游','10月1号')";
            String sql_init_3 = "insert into t_message values (2,'TT2','一起去旅游','10月1号')";
            db.execSQL(sql_drop_old_table);
            db.execSQL(sql_message);
            db.execSQL(sql_init_1);
            db.execSQL(sql_init_2);
            db.execSQL(sql_init_3);
        }
    }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

   这种情况是在已知备份了V2.0的数据，将V3.0的改名并且将数据复制到V2.0新建的表中，因为表结构可能存在差异导致复制失败，所以添加异常处理，如果降级失败，就删除原来的表重新建回v2.0的数据表，这样依旧可以实现重v3.0降级到v2.0。

 

# 参考文章

[Android之sqlite数据库版本升级和降级的处理（onUpgrade和onDowngrade）](https://www.cnblogs.com/wdht/p/6125793.html)



