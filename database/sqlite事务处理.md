# 数据库 SQLite 事务处理

 使用事务可以让一系列操作要不全部成功执行，要么全部不执行：

```java
db.beginTransaction();  //开启事务
try{
    db.execSQL("delete from book where id=?", new String[]{"3"});
    if(true){
        throw new NullPointerException(); 
    }
    db.execSQL("insert into book (name, author, price, pages) values (?, ?, ?, ?)", 
        new String[]{"name", "author", "9.99", "234"});
}catch(Exception ex){
    ex.printStackTrace();
}finally{
    db.endTransaction();    //结束事务
}
```

以上代码由于使用事务机制，所以删除语句也不会执行。

# 参考文章

[数据库SQLite事务处理](https://www.cnblogs.com/xuejianbest/p/10285031.html)



