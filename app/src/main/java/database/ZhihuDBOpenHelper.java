package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/8/13.
 * 数据库帮助类，进行数据库的相关操作：建表、更新等。
 */
public class ZhihuDBOpenHelper extends SQLiteOpenHelper {

    // 创建news表，用来保存用户收藏的新闻
    final String CREATE_TABLE_NEWS = "create table news(id integer primary key, " +
            "title text, imageurl text, date text, islike integer)";

    public ZhihuDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 第一次使用数据库时自动建表
        sqLiteDatabase.execSQL(CREATE_TABLE_NEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
