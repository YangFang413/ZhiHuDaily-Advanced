package activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.yf.myzhihu.R;


import database.ZhihuDB;

/**
 * Created by Administrator on 2016/8/18.
 * 新闻详情的活动类
 * 需要实现的功能：
 * 1. 显示网页形式的新闻
 * 2. 点击收藏按钮更新数据库
 *    点击单数次收藏，点击双数次取消收藏
 */
public class NewsDetailActivity extends Activity implements View.OnClickListener {

    private Button backButton;
    private Button isLikeButton;
    private WebView newsDetails;
    int flag = 0;
    private ZhihuDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.news_details);

        // 初始化
        backButton = (Button) findViewById(R.id.back_button);
        isLikeButton = (Button) findViewById(R.id.like_button);
        newsDetails = (WebView) findViewById(R.id.news_details);
        db = ZhihuDB.getInstance(this);

        // 为按钮绑定监听器
        backButton.setOnClickListener(this);
        isLikeButton.setOnClickListener(this);

        String text = getIntent().getStringExtra("response");
        newsDetails.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 设置图片以单列显示，不占用别的位置
        newsDetails.loadDataWithBaseURL(getApplicationContext().getFilesDir() + "/style.css",
                text, "text/html", "utf-8", null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_button:
                Intent i = new Intent(NewsDetailActivity.this, HomepageActivity.class);
                startActivity(i);
                break;
            case R.id.like_button:
                flag++;
                int id = getIntent().getIntExtra("id", 0);
                ContentValues values = new ContentValues();
                if (flag % 2 == 0){
                    values.clear();
                    values.put("islike", 0);
                    db.writeIsLike(id, values);
                    isLikeButton.setBackgroundResource(R.mipmap.like_white);
                } else {
                    values.clear();
                    values.put("islike", 1);
                    db.writeIsLike(id, values);
                    isLikeButton.setBackgroundResource(R.mipmap.like_red);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
