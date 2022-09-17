# Room 简介

## 简介

Room 框架的基本组件：

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6O7iahLp6Hb0pOqwucmNianKaPNXU590ic0iap4deJ8Hccr8MlibfM7yeNUDvCsF2uQqArp8Xem4TMlzg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

使用起来大体就是这几个步骤，很便捷。

![](https://mmbiz.qpic.cn/mmbiz_png/v1LbPPWiaSt6O7iahLp6Hb0pOqwucmNianKLjcCRib9rAnxIV3DkCDoEFbmuibYN8icZicGia7KhicQqmQmfvpwLQ0EKiaRA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

使用前需要构筑如下依赖：

```groovy
dependencies {
  def room_version = "2.2.6"

  implementation "androidx.room:room-runtime:$room_version"
  kapt "androidx.room:room-compiler:$room_version"

  implementation "androidx.room:room-ktx:$room_version"
  testImplementation "androidx.room:room-testing:$room_version"
}
```

## 实战

下面将通过一个展示电影列表的 demo 演示 Room 框架的使用。

### 组件构建

首先构建一个电影表 movie，有名称、演员、上映年份、评分这么几个字段。

```kotlin
@Entity
class Movie() : BaseObservable() {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "movie_name", defaultValue = "Harry Potter")
    lateinit var name: String

    @ColumnInfo(name = "actor_name", defaultValue = "Jack Daniel")
    lateinit var actor: String

    @ColumnInfo(name = "post_year", defaultValue = "1999")
    var year = 1999

    @ColumnInfo(name = "review_score", defaultValue = "8.0")
    var score = 8.0
}
```

@Entity 表示数据库中的表

@PrimaryKey 表示主键，autoGenerate 表示自增

@ColumnInfo 表示字段，name 表示字段名称

然后构建一个访问 Movie 表的 DAO 接口。

```kotlin
@Dao
interface MovieDao {
    @Insert
    fun insert(vararg movies: Movie?): LongArray?

    @Delete
    fun delete(movie: Movie?): Int

    @Update
    fun update(vararg movies: Movie?): Int

    @get:Query("SELECT * FROM movie")
    val allMovies: LiveData<List<Movie?>?>
}
```

@Dao 表示访问 DB 的方法，需要声明为接口或抽象类，编译阶段将生成 _impl 实现类，此处则将生成 MovieDao_Impl.java 文件。

@Insert、@Delete、@Update 和 @Query 分别表示数据库的增删改查方法。

最后需要构建 Room 使用的入口 RoomDatabase.

```kotlin
@Database(entities = [Movie::class], version = 1)
abstract class MovieDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var sInstance: MovieDataBase? = null
        private const val DATA_BASE_NAME = "jetpack_movie.db"

        @JvmStatic
        fun getInstance(context: Context): MovieDataBase? {
            if (sInstance == null) {
                synchronized(MovieDataBase::class.java) {
                    if (sInstance == null) {
                        sInstance = createInstance(context)
                    }
                }
            }
            return sInstance
        }

        private fun createInstance(context: Context): MovieDataBase {
            return Room.databaseBuilder(context.applicationContext, MovieDataBase::class.java, DATA_BASE_NAME)
                    ...
                    .build()
        }
    }
}
```

* @Database 表示继承自RoomDatabase的抽象类，entities指定表的实现类列表，version指定了DB版本
* 必须提供获取 DAO 接口的抽象方法，比如上面定义的 moveDao()，Room 将通过这个方法实例化 DAO 接口
* RoomDatabase 实例的内存开销较大，建议使用单例模式管理
* 编译时将生成 _Impl 实现类，此处将生成 MovieDataBase_Impl.java 文件

### 组件调用

本 demo 将结合 ViewModel 和 Room 进行数据交互，依赖 LiveData 进行异步查询，画面上则采用 Databinding 将数据和视图自动绑定。

```kotlin
class DemoActivity : AppCompatActivity() {
    private var movieViewModel: MovieViewModel? = null
    private var binding: ActivityRoomDbBinding? = null
    private var movieList: List<Movie?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDbBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.lifecycleOwner = this

        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
        movieViewModel?.getMovieList(this, { movieList: List<Movie?>? ->
            if (movieList == null) return@getMovieList
            this.movieList = movieList
            binding?.setMovieList(movieList)
        })
    }
}
```

ViewModel 通过 MediatorLiveData 担当列表查询的中介，当 DB 初始化结束后再更新 UI。

```kotlin
class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val mediatorLiveData = MediatorLiveData<List<Movie?>?>()
    private val db: MovieDataBase?
    private val mContext: Context

    init {
        mContext = application
        db = MovieDataBase.getInstance(mContext)
        if (db != null) {
            mediatorLiveData.addSource(db.movieDao().allMovies) { movieList ->
                if (db.databaseCreated.value != null) {
                    mediatorLiveData.postValue(movieList)
                }
            }
        };
    }

    fun getMovieList(owner: LifecycleOwner?, observer: Observer<List<Movie?>?>?) {
        if (owner != null && observer != null)
            mediatorLiveData.observe(owner, observer)
    }
}
```

RoomDatabase 创建后异步插入初始化数据，并通知 MediatorLiveData。

```
abstract class MovieDataBase : RoomDatabase() {
    val databaseCreated = MutableLiveData<Boolean?>()
    ...

    companion object {
        ...
        private fun createInstance(context: Context): MovieDataBase {
            return Room.databaseBuilder(context.applicationContext, ...)
                    ...
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newFixedThreadPool(5).execute {
                                val dataBase = getInstance(context)
                                val ids = dataBase!!.movieDao().insert(*Utils.initData)
                                dataBase.databaseCreated.postValue(true)
                            }
                        }
                        ...
                    })
                    .build()
        }
    }
}
```

运行下 demo 看下效果：

![](https://mmbiz.qpic.cn/mmbiz_png/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVooHRjSbTx23d6lJDiaIasLUYlNx7vfEIp1bibiaoaRFRJushHxDZwWNew/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

通过 Database inspector 工具可以看到 DB 数据创建成功了。

Database Inspector 支持实时刷新，查询和修改等 DB 操作，是 DB 开发的利器。

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVibv9okJCgcWTWz3m6beicOcicDNnV9zTl0zHbY5xGBdRNU8Nlm1paF4Dw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

## DAO 的具体使用

**@Insert**

@Insert支持设置冲突策略，默认为OnConflictStrategy.ABORT即中止并回滚。还可以指定为其他策略。

- OnConflictStrategy.REPLACE 冲突时替换为新记录
- OnConflictStrategy.IGNORE 忽略冲突(不推荐)
- OnConflictStrategy.ROLLBACK 已废弃，使用ABORT替代
- OnConflictStrategy.FAIL 同上

其声明的方法返回值可为空，也可为插入行的ID或列表。

- fun insertWithOutId(movie: Movie?)
- fun insert(movie: Movie?): Long?
- fun insert(vararg movies: Movie?): LongArray?

**@Delete**

和@Insert一样支持不返回删除结果或返回删除的函数，不再赘述。

**@Update**

和@Insert一样支持设置冲突策略和定制返回更新结果。此外需要注意的是@Update操作将匹配参数的主键id去更新字段。

fun update(vararg movies: Movie?): Int

**@Query**

查询操作主要依赖@Update的value，指定不同的SQL语句即可获得相应的查询结果。在编译阶段就将验证语句是否正确，避免错误的查询语句影响到运行阶段。

* 查询所有字段

  @get:Query(“SELECT * FROM movie”)

- 查询指定字段

  @get:Query(“SELECT id, movie_name, actor_name, post_year, review_score FROM movie”)

- 排序查询

  @get:Query(“SELECT * FROM movie ORDER BY post_year DESC”) 比如查询最近发行的电影列表

- 匹配查询

  @Query(“SELECT * FROM movie WHERE id = :id”)

- 多字段匹配查询

  @Query(“SELECT * FROM movie WHERE movie_name LIKE :keyWord " + " OR actor_name LIKE :keyWord”) 比如查询名称和演员中匹配关键字的电影

- 模糊查询

  @Query(“SELECT * FROM movie WHERE movie_name LIKE ‘%’ || :keyWord || ‘%’ " + " OR actor_name LIKE ‘%’ || :keyWord || ‘%’”) 比如查询名称和演员中包含关键字的电影

- 限制行数查询

  @Query(“SELECT * FROM movie WHERE movie_name LIKE :keyWord LIMIT 3”) 比如查询名称匹配关键字的前三部电影

- 参数引用查询

  @Query(“SELECT * FROM movie WHERE review_score >= :minScore”) 比如查询评分大于指定分数的电影

- 多参数查询

  @Query(“SELECT * FROM movie WHERE post_year BETWEEN :minYear AND :maxYear”) 比如查询介于发行年份区间的电影

- 不定参数查询

  @Query(“SELECT * FROM movie WHERE movie_name IN (:keyWords)”)

- Cursor查询

  @Query(“SELECT * FROM movie WHERE movie_name LIKE ‘%’ || :keyWord || ‘%’ LIMIT :limit”)

  fun searchMoveCursorByLimit(keyWord: String?, limit: Int): Cursor?

  注意：Cursor需要保证查询到的字段和取值一一对应，所以不推荐使用

- 响应式查询

  demo采用的LiveData进行的观察式查询，还可以配合RxJava2，Kotlin的Flow进行响应式查询。

## 进阶使用

### 数据库升级降级

在 movie 类里增加新字段后，重新运行已创建过 DB 的 demo 会发生崩溃。

```
Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number.
```

将 @Database 的 version 升级为 2 之后再次运行仍然发生崩溃。

```
A migration from 1 to 2 was required but not found. Please provide the necessary Migration path via RoomDatabase.Builder.addMigration(Migration ...) or allow fallback of the RoomDatabase.Builder.fallbackToDestructiveMigration* methods.
```

提醒我们调用 fallbackToDestructiveMigration() 允许升级失败时破坏性地删除 DB。

如果照做的话，将能避免发生崩溃，并且 onDestructiveMigration() 将被调用。在这个糊掉里可以试着重新初始化 DB。

```kotlin
private fun createInstance(context: Context): MovieDataBase {
    return Room.databaseBuilder(context.applicationContext, MovieDataBase::class.java, DATA_BASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(object : Callback() {
                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    // Init DB again after db removed.
                    Executors.newFixedThreadPool(5).execute {
                        val dataBase = getInstance(context)
                        val ids = dataBase!!.movieDao().insert(*Utils.initData)
                        dataBase.databaseCreated.postValue(true)
                    }
                }
            })
            .build()
}
```

但是 DB 升级后，无论原有数据被删除还是重新初始化都是用户难以接受的。

可以通过 addMigrations() 指定升级之后的迁移处理来达到保留旧数据和增加新字段的双赢。

比如如下展示的从版本 1 到版本 2，并增加一个默认值为 8.0 的评分列的迁移处理。

```kotlin
private fun createInstance(context: Context): MovieDataBase {
    return Room.databaseBuilder(context.applicationContext, MovieDataBase::class.java, DATA_BASE_NAME)
            // .fallbackToDestructiveMigration()
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE movie "
                            + " ADD COLUMN review_score INTEGER NOT NULL DEFAULT 8.0")
                }
            })
            ...
            })
            .build()
}
```

**注意**

降级则调用 fallbackToDestructiveMigrationOnDowngrade() 来指定在降级时删除 DB，也可以像上述那样指定 drop column 来进行数据迁移。

如果想要迁移数据，无论是升级还是降级，必须要给 @Database 的 version 指定正确的目标版本。Migration 迁移处理的起始版本以及实际的迁移处理 migrate() 都必不可少。

### 事务处理

当我们的 DB 操作需要保持一致性，或者查询关联性结构的时候需要保证事务处理。Room 提供了 @Transaction 注解帮助我们快速实现这个需求，它将确保注解哪的方法运行在同一个事务模式。

```kotlin
@Dao
public interface MovieDao {
    @Transaction
    default void insetNewAndDeleteOld(Movie newMovie, Movie oldMovie) {
        insert(newMovie);
        delete(oldMovie);
    }
}
```

需要注意的是，事务处理比较占用性能，避免在事务处理的方法内执行耗时逻辑。

另外，@Insert、@Delete 和 @Update 的处理自动在事务模式进行处理，无需增加 @Transaction 注解。

```kotlin
public long[] insert(final Movie... movies) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
        long[] _result = __insertionAdapterOfMovie.insertAndReturnIdsArray(movies);
        __db.setTransactionSuccessful();
        return _result;
    } finally {
        __db.endTransaction();
    }
}
```

上面的源码也启发我们可以手动执行事务处理，一般来说不需要，取决于具体情况。

RoomDatabase 的 beginTransaction() 和 endTransaction() 不推荐外部使用了，可以采用封装好的 runInTransaction() 实现。

```kotlin
db.runInTransaction(Runnable {
    val database = db.getOpenHelper().getWritableDatabase();

    val contentValues = ContentValues()
    contentValues.put("movie_name", newMovie.getName())
    contentValues.put("actor_name", newMovie.getActor())
    contentValues.put("post_year", newMovie.getYear())
    contentValues.put("review_score", newMovie.getScore())

    database.insert("movie", SQLiteDatabase.CONFLICT_ABORT, contentValues)
    database.delete("movie", "id = " + oldMovie.getId(), null)
})
```

## 原理浅谈

### RoomDatabase 的创建

RoomDatabase$Builder 的 build() 调用后便通过反射创建了 @Database 注解声明的 RoomDatabase 实例 XXX_Impl。

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXV8AcOY3SsogKoYia0gyQamp5BQswRiaAFKXkSNibovfQrpWIOealGcMBMw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

### SupportSQLiteDatabase 的创建

SupportSQLiteDatabase 是模仿 SQLiteDatabase 作成的接口，供 Room 框架内部对 DB 进行操作。由 FrameworkSQLiteDatabase 实现，其将通过内部持有的 SQLiteDatabase 实例，代理 DB 操作。

SupportSQLiteDatabase 的创建由增删改查等 DB 操作触发，需要经历 DB 的创建，表的创建，表的初始化，升降机以及打开等过程。

### 创建 DB 文件

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVCgiaanD30TIYIicP8YichSPdzUhoiagXYfjg3yULe2QWTL3nBGy0Q69skg/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

### 创建表

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVrT0COv4eYdGkEumoVe2hwcAbJh5C182NgrnC5wCKAwOiaZxumqWSUnQ/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

### 初始化表

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVGaZJAxdzFGGng5iaicxxqgRgibmTVKVvtHxayeZrCrCt9tiaJJNrGCL6Aw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

### 升级表

DB 文件已经存在并且版本和目标版本不一致的话，将执行数据迁移。但如果迁移处理未配置或者执行失败了便将删除 DB 并执行相应的回调。

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVYR6sMO6JQK4lVicTaTNkr92OG1yxicz9HrXhxqYvFCQW6cyomEY1pvUw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

### 打开表

DB 的创建或升级都正常完成后将回调 onOpen()。

![](https://mmbiz.qpic.cn/mmbiz_jpg/GIUVCiacpxgOft10jNzXuScoliaTsY8RXVaAEHyjkWVE8IdUUDbarM9mmUv7DuncPFjKE4T6NsL8ny5NkrfaicicUw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

## 注意

Room 框架的私用过程中遇到了些容易出错的地方，需要格外留意。

* RoomDatabase 的实例建议采用单例模式管理

* 不要在 UI 线程执行 DB 操作，否则发生异常：Cannot access database on the main thread since it may potentially lock the UI for a long period of time.

  通过调用：allowMainThreadqueries() 可以回避，但不推荐

* 不要在 Callback#onCreate() 里同步执行 insert 等 DB 处理，否则将阻塞 DB 实例的初始化并发生异常：getDatabase called recursively。

* @Entity 注解类不要提供多个构造函数，使用 @Ignore 可以回避

* Callback#onCreate 并非由 RoomDatabase$Builder#build() 触发，而是由具体的增删改查操作触发，切记。

## 总结

Room 的本质是在 SQLite 的基础上进行封装的抽象层，通过一系列注解让用户能够更简便的使用 SQLite。

优势：

* 声明注解便能完成接口的定义，易上手
* 编译阶段将验证注解里声明的 SQL 语句，提高了开发效率
* 支持使用 RxJava2、LiveData 以及 Flow 进行异步查询
* 相较其他数据库框架 SQL 执行效率更高。

## 参考文章

1. [啥也不说了，Room真香！](https://mp.weixin.qq.com/s/OADIm6fSlnd97vrs4a1GAw)

