package utility;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import database.ZhihuDB;
import fragment.HomepageFragment;
import model.News;

/**
 * Created by Administrator on 2016/8/14.
 * 用来解析和处理服务器返回的数据
 */
public class ParseUtility {

    public static JSONObject getJO(String response) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject;
        }
        return null;
    }

    public static int getJOInt(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.getInt(key);
    }

    public static String getJOString(JSONObject jsonObject, String key) throws JSONException{
        return jsonObject.getString(key);
    }

    public static JSONArray getJOArray(JSONObject jsonObject, String key) throws JSONException{
        return jsonObject.getJSONArray(key);
    }

    // 解析服务器返回的JSON类型的数据，更新开屏图片
    public synchronized static String parseJSONResponse(String response) throws JSONException {
            String address = getJOString(getJO(response), "img");
            return address;
    }

    // 解析服务器返回的Hot News的JSON数据，并存储到List<News>中，返回
    public static List<News> handleHotNews (String response){
        List<News> mList = new ArrayList<>();
        try {
            JSONArray array = getJOArray(getJO(response),"recent");
            for (int i = 0; i < HomepageFragment.VIEWPAGER_MAX ; i++){
                News news = new News();
                JSONObject jsonObject = array.getJSONObject(i);
                news.setId(jsonObject.getInt("news_id"));
                news.setTitle(jsonObject.getString("title"));
                news.setImageUrl(jsonObject.getString("thumbnail"));
                news.setNewsUrl(jsonObject.getString("url"));
                mList.add(news);
            }
            return mList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mList;
    }

    // 解析服务器返回的JSON类型的当日News数据
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public synchronized static void parseNewsJSONResponse (ZhihuDB db, String response) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            JSONObject jo = new JSONObject(response);
            String date = jo.getString("date");
            JSONArray jsonArray = jo.getJSONArray("stories");
            for (int i=0; i < jsonArray.length(); i++){
                News news = new News();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray array = jsonObject.getJSONArray("images");
                String[] url = array.get(0).toString().split("\"");
                news.setDate(date);
                news.setImageUrl(url[0]);
                news.setId(jsonObject.getInt("id"));
                news.setTitle(jsonObject.getString("title"));
                news.setIsFavorite(0);
                db.saveNews(news);
            }
        }
    }
}
