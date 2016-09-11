package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import model.News;

/**
 * Created by Administrator on 2016/8/13.
 * 数据库类，封装一些跟数据库相关的操作
 * 单例模式，一个应用程序只有一个数据库
 */
public class ZhihuDB {

    public static final String DB_NAME = "zhihu"; // 数据库名
    public static final int version = 1; // 数据库版本
    private static ZhihuDB zhihuDB; // 申明数据库对象
    private SQLiteDatabase db;

    // 构造方法私有化
    private ZhihuDB(Context context){
        ZhihuDBOpenHelper dbHelper = new ZhihuDBOpenHelper(context, DB_NAME, null, version);
        db = dbHelper.getWritableDatabase();
    }

    // 获取ZhihuDB的实例
    public synchronized static ZhihuDB getInstance(Context context) {
        if (zhihuDB == null) {
            zhihuDB = new ZhihuDB(context);
        }
        return zhihuDB;
    }

    // 将News的实例存储到数据库中
    public void saveNews(News news){
        if (news != null){
            ContentValues values = new ContentValues();
            values.put("id", news.getId());
            values.put("title", news.getTitle());
            values.put("imageurl", news.getImageUrl());
            values.put("date", news.getDate());
            values.put("islike", 0);
            db.insert("news", null, values);
        }
    }

    // 从数据库中读取全部的News的信息
    public List<News> loadNews(){
        List<News> list;
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        String month = "";
        if (mMonth < 10){
            month = "0" + String.valueOf(mMonth);
        }
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        String date = String.valueOf(mYear) + month + String.valueOf(mDay);
        Cursor cursor = db.query("news", null, "date = ?", new String[]{date}, null, null, null);
        list = handleCursor(cursor);
        return list;
    }

    // 从数据库中设置是否收藏
    public void writeIsLike(int id, ContentValues values){
        db.update("news", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // 从数据库中查询是否收藏，并返回一个list
    public List<News> isFavorite(){
        List<News> list;
        Cursor cursor = db.query("news", null, "islike = ?", new String[]{"1"}, null, null, null);
        list = handleCursor(cursor);
        return list;
    }

    // 对数据库中查询到的数据进行处理
    public List<News> handleCursor (Cursor cursor){
        List<News> list = new ArrayList<News>();
        if (cursor.moveToFirst()){
            do {
                News news = new News();
                news.setId(cursor.getInt(cursor.getColumnIndex("id")));
                news.setImageUrl(cursor.getString(cursor.getColumnIndex("imageurl")));
                news.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                news.setDate(cursor.getString(cursor.getColumnIndex("date")));
                news.setIsFavorite(cursor.getInt(cursor.getColumnIndex("islike")));
                list.add(news);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
