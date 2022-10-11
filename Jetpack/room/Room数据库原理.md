# Room 数据库原理

构建数据库的代码是：

```java
 roomDataBase = Room.databaseBuilder(context.getApplicationContext(),LocalRoomDataBase.class,db_name)
                    .allowMainThreadQueries()
                    .addMigrations(LocalRoomDataBase.updateMigration) // 版本迁移调用
                    .fallbackToDestructiveMigration() // 数据库版本迁移异常，重新创建方式崩溃
                    .build();

```

Room 在编译期通过 kapt 处理 @Dao 和 @Database 注解，并生成 DAO 和 Database 的实现类，LocalRoomDataBase_Impl 和dbDao_Impl。kapt生成的代码在 build/generated/source/apt/。

Builder 的好处是便于对 Database 进行配置：

* createFromAsset()/createFromFile() ：从 SD 卡或者 Asset 的 db 文件创建 RoomDatabase 实例。
* addMigrations() ：添加一个数据库迁移（migration），当进行数据版本升级时需要。
* allowMainThreadQueries() ：允许在 UI 线程进行数据库查询，默认是不允许的。
* fallbackToDestructiveMigration() ：如果找不到 migration 则重建数据库表（会造成数据丢失）。

除上面以外，还有其他很多配置。调用 build() 后，创建 LocalRoomDatabase_Impl，并调用 init()，内部会调用 createOpenHelper()。

## 1. build() 方法

```java

        /**
         * Creates the databases and initializes it.
         * <p>
         * By default, all RoomDatabases use in memory storage for TEMP tables and enables recursive
         * triggers.
         *
         * @return A new database instance.
         */
        @SuppressLint("RestrictedApi")
        @NonNull
        public T build() {
            //noinspection ConstantConditions
            if (mContext == null) {
                throw new IllegalArgumentException("Cannot provide null context for the database.");
            }
            //noinspection ConstantConditions
            if (mDatabaseClass == null) {
                throw new IllegalArgumentException("Must provide an abstract class that"
                        + " extends RoomDatabase");
            }
            if (mQueryExecutor == null) {
                mQueryExecutor = ArchTaskExecutor.getIOThreadExecutor();
            }

            if (mMigrationStartAndEndVersions != null && mMigrationsNotRequiredFrom != null) {
                for (Integer version : mMigrationStartAndEndVersions) {
                    if (mMigrationsNotRequiredFrom.contains(version)) {
                        throw new IllegalArgumentException(
                                "Inconsistency detected. A Migration was supplied to "
                                        + "addMigration(Migration... migrations) that has a start "
                                        + "or end version equal to a start version supplied to "
                                        + "fallbackToDestructiveMigrationFrom(int... "
                                        + "startVersions). Start version: "
                                        + version);
                    }
                }
            }

            if (mFactory == null) {
                mFactory = new FrameworkSQLiteOpenHelperFactory();
            }
            DatabaseConfiguration configuration =
                    new DatabaseConfiguration(mContext, mName, mFactory, mMigrationContainer,
                            mCallbacks, mAllowMainThreadQueries, mJournalMode.resolve(mContext),
                            mQueryExecutor,
                            mMultiInstanceInvalidation,
                            mRequireMigration,
                            mAllowDestructiveMigrationOnDowngrade, mMigrationsNotRequiredFrom);
          	// 反射创建 LocalRoomDataBase 对象
            T db = Room.getGeneratedImplementation(mDatabaseClass, DB_IMPL_SUFFIX);
            db.init(configuration);
            return db;
        }
    }
```

在 build() 方法中会通过 Room 的 getGeneratedImplementation() 方法反射创建 LocalRoomDataBase 的对象：

```java
    @SuppressWarnings({"TypeParameterUnusedInFormals", "ClassNewInstance"})
    @NonNull
    static <T, C> T getGeneratedImplementation(Class<C> klass, String suffix) {
        final String fullPackage = klass.getPackage().getName();
        String name = klass.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty()
                ? name
                : (name.substring(fullPackage.length() + 1));
        final String implName = postPackageName.replace('.', '_') + suffix;
        //noinspection TryWithIdenticalCatches
        try {

            @SuppressWarnings("unchecked")
          // 通过反射的方式创建一个对象（以_Impl 为后缀）
            final Class<T> aClass = (Class<T>) Class.forName(
                    fullPackage.isEmpty() ? implName : fullPackage + "." + implName);
            return aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("cannot find implementation for "
                    + klass.getCanonicalName() + ". " + implName + " does not exist");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access the constructor"
                    + klass.getCanonicalName());
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to create an instance of "
                    + klass.getCanonicalName());
        }
    }
```

所以`roomDataBase = Room.databaseBuilder(context.getApplicationContext(),LocalRoomDataBase.class,db_name).build()` roomDataBase 对象其实是`AbstractAppDatabase_Impl`，所以`db.dbDao();`就是`AbstractAppDatabase_Impl.dbDao()`。





并调用其 init() 方法：

```java
    @CallSuper
    public void init(@NonNull DatabaseConfiguration configuration) {
        mOpenHelper = createOpenHelper(configuration);
        boolean wal = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wal = configuration.journalMode == JournalMode.WRITE_AHEAD_LOGGING;
            mOpenHelper.setWriteAheadLoggingEnabled(wal);
        }
        mCallbacks = configuration.callbacks;
        mQueryExecutor = configuration.queryExecutor;
        mAllowMainThreadQueries = configuration.allowMainThreadQueries;
        mWriteAheadLoggingEnabled = wal;
        if (configuration.multiInstanceInvalidation) {
            mInvalidationTracker.startMultiInstanceInvalidation(configuration.context,
                    configuration.name);
        }
    }
```

在 init() 方法中调用了 createOpenHelper() 方法。

## 2. LocalRoomDataBase_Impl

```java
package com.zhangmiao.myapplication;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class LocalRoomDataBase_Impl extends LocalRoomDataBase {
  private volatile dbDao _dbDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `db_testData` (`userId` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `age` INTEGER NOT NULL, `kind` TEXT, `sex` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"61412fd6826d31af14380f3b94a3c6d7\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `db_testData`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsDbTestData = new HashMap<String, TableInfo.Column>(5);
        _columnsDbTestData.put("userId", new TableInfo.Column("userId", "INTEGER", false, 1));
        _columnsDbTestData.put("name", new TableInfo.Column("name", "TEXT", false, 0));
        _columnsDbTestData.put("age", new TableInfo.Column("age", "INTEGER", true, 0));
        _columnsDbTestData.put("kind", new TableInfo.Column("kind", "TEXT", false, 0));
        _columnsDbTestData.put("sex", new TableInfo.Column("sex", "TEXT", false, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDbTestData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDbTestData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDbTestData = new TableInfo("db_testData", _columnsDbTestData, _foreignKeysDbTestData, _indicesDbTestData);
        final TableInfo _existingDbTestData = TableInfo.read(_db, "db_testData");
        if (! _infoDbTestData.equals(_existingDbTestData)) {
          throw new IllegalStateException("Migration didn't properly handle db_testData(com.zhangmiao.myapplication.TestData).\n"
                  + " Expected:\n" + _infoDbTestData + "\n"
                  + " Found:\n" + _existingDbTestData);
        }
      }
    }, "61412fd6826d31af14380f3b94a3c6d7", "7562f483cecb1e12ae27c05e9395c836");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "db_testData");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `db_testData`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  public dbDao getTestDataDao() {
    if (_dbDao != null) {
      return _dbDao;
    } else {
      synchronized(this) {
        if(_dbDao == null) {
          // 可以看到这里创建了一个 dbDao_Impl 类
          _dbDao = new dbDao_Impl(this);
        }
        return _dbDao;
      }
    }
  }
}

```

* createOpenHelper： Room.databaseBuilder().build() 创建 Database 时，会调用实现类的 createOpenHelper() 创建SupportSQLiteOpenHelper，此 Helper 用来创建 DB 以及管理版本。
* createInvalidationTracker ：创建跟踪器，确保 table 的记录修改时能通知到相关回调方。
* clearAllTables：清空 table 的实现

## 3. dbDao_Impl

```java
package com.zhangmiao.myapplication;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class dbDao_Impl implements dbDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfTestData;

  private final EntityDeletionOrUpdateAdapter __updateAdapterOfTestData;

  private final SharedSQLiteStatement __preparedStmtOfUpDataTestData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTestData;

  public dbDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTestData = new EntityInsertionAdapter<TestData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `db_testData`(`userId`,`name`,`age`,`kind`,`sex`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TestData value) {
        if (value.userId == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindLong(1, value.userId);
        }
        if (value.name == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.name);
        }
        stmt.bindLong(3, value.age);
        if (value.kind == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.kind);
        }
        if (value.sex == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.sex);
        }
      }
    };
    this.__updateAdapterOfTestData = new EntityDeletionOrUpdateAdapter<TestData>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `db_testData` SET `userId` = ?,`name` = ?,`age` = ?,`kind` = ?,`sex` = ? WHERE `userId` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TestData value) {
        if (value.userId == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindLong(1, value.userId);
        }
        if (value.name == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.name);
        }
        stmt.bindLong(3, value.age);
        if (value.kind == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.kind);
        }
        if (value.sex == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.sex);
        }
        if (value.userId == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindLong(6, value.userId);
        }
      }
    };
    this.__preparedStmtOfUpDataTestData = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "update db_testData set name=? where userId=?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteTestData = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "delete from db_testData where userId=?";
        return _query;
      }
    };
  }

  // 具体插入数据库数据的实现类
  @Override
  public void insertTestData(final TestData testData) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfTestData.insert(testData);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateAlarmInfoData(final TestData testData) {
    __db.beginTransaction();
    try {
      __updateAdapterOfTestData.handle(testData);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void upDataTestData(final String name1, final Integer id1) {
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpDataTestData.acquire();
    int _argIndex = 1;
    if (name1 == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, name1);
    }
    _argIndex = 2;
    if (id1 == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindLong(_argIndex, id1);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfUpDataTestData.release(_stmt);
    }
  }

  @Override
  public void deleteTestData(final Integer id1) {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTestData.acquire();
    int _argIndex = 1;
    if (id1 == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindLong(_argIndex, id1);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteTestData.release(_stmt);
    }
  }

  @Override
  public TestData selectTestData(final String name1) {
    final String _sql = "select * from db_testData where name=? Order by userId desc limit 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name1);
    }
    final Cursor _cursor = DBUtil.query(__db, _statement, false);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
      final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
      final int _cursorIndexOfSex = CursorUtil.getColumnIndexOrThrow(_cursor, "sex");
      final TestData _result;
      if(_cursor.moveToFirst()) {
        _result = new TestData();
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _result.userId = null;
        } else {
          _result.userId = _cursor.getInt(_cursorIndexOfUserId);
        }
        _result.name = _cursor.getString(_cursorIndexOfName);
        _result.age = _cursor.getInt(_cursorIndexOfAge);
        _result.kind = _cursor.getString(_cursorIndexOfKind);
        _result.sex = _cursor.getString(_cursorIndexOfSex);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TestData> selectTestDataList() {
    final String _sql = "select * from db_testData";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = DBUtil.query(__db, _statement, false);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
      final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
      final int _cursorIndexOfSex = CursorUtil.getColumnIndexOrThrow(_cursor, "sex");
      final List<TestData> _result = new ArrayList<TestData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TestData _item;
        _item = new TestData();
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _item.userId = null;
        } else {
          _item.userId = _cursor.getInt(_cursorIndexOfUserId);
        }
        _item.name = _cursor.getString(_cursorIndexOfName);
        _item.age = _cursor.getInt(_cursorIndexOfAge);
        _item.kind = _cursor.getString(_cursorIndexOfKind);
        _item.sex = _cursor.getString(_cursorIndexOfSex);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}

```

dbDao_Impl 主要有三个属性:

* \__db：RoomDatabase的实例
* __insertionAdapterOfTestData ：EntityInsertionAdapterd 实例，用于数据 insert。上例中，将在 insertTestData() 中调用。
* __updateAdapterOfTestData：EntityDeletionOrUpdateAdapter 实例，用于数据的 update/delete。 上例中，在 updateAlarmInfoData() 中调用。

所以`dbDao.insertTestData();`就是`dbDao_Impl.insertTestData();`



到这里就结束了，Room 就是采用注解的方式，通过 APT(编译时技术)生成了所需要的实现类(以_Impl结尾的)来实现具体的业务逻辑。

## 参考文章

1. [JetPack轻量级数据库Room使用和原理解析](https://blog.csdn.net/sunlifeall/article/details/114479483)
2. [【Android Jetpack】Room数据库的使用及原理详解](https://blog.51cto.com/u_15200109/2786116)



