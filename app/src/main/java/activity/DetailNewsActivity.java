package activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.yf.myzhihu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import utility.HttpUtil;
import utility.ParseUtility;

public class DetailNewsActivity extends AppCompatActivity {

    private WebView newsDetailsWebView;
    private final int DEFAULT_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_news);

        newsDetailsWebView = (WebView) findViewById(R.id.detail_news);
        initData();
    }

    private void initData() {
        final int newsId = getIntent().getIntExtra("newsId", DEFAULT_ID);
        if (newsId == DEFAULT_ID){
            Toast.makeText(getApplicationContext(), R.string.news_id_missing, Toast.LENGTH_SHORT).show();
        } else {
            if (!HttpUtil.networkCheck(this)){
                Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String address = "http://news-at.zhihu.com/api/4/news/" + newsId;
                            String response = HttpUtil.getUrlString(address);
                            JSONObject jsonObject = ParseUtility.getJO(response);
                            String body = ParseUtility.getJOString(jsonObject, "body");
                            String cssUrl = ParseUtility.getJOArray(jsonObject, "css").get(0).toString();
                            byte[] b = HttpUtil.getUrlByte(cssUrl);
                            File cssFile = new File(getApplicationContext().getFilesDir(), "style.css");
                            FileOutputStream outputStream = new FileOutputStream(cssFile);
                            outputStream.write(b);
                            outputStream.close();
                            int id = ParseUtility.getJOInt(jsonObject, "id");
                            String title = ParseUtility.getJOString(jsonObject, "title");
                            String imageUrl = ParseUtility.getJOString(jsonObject, "image");
                            String imageSource = ParseUtility.getJOString(jsonObject, "image_source");
                            File imageFile = new File(getApplicationContext().getFilesDir(), id + ".jpg");
                            byte[] bytes = HttpUtil.getUrlByte(imageUrl);
                            FileOutputStream os = new FileOutputStream(imageFile);
                            os.write(bytes);
                            os.close();
                            String imageSrc = getApplicationContext().getFilesDir() + "/" + id + ".jpg";
                            final String cssSrc = getApplicationContext().getFilesDir() + "/style.css";
                            StringBuilder sb = new StringBuilder();
                            sb.append("<div class=\"img-wrap\">")
                                    .append("<h1 class=\"headline-title\">")
                                    .append(title)
                                    .append("</h1>")
                                    .append("<span class=\"img-source\">")
                                    .append(imageSource)
                                    .append("</span>")
                                    .append("<img src=\"")
                                    .append(imageSrc)
                                    .append("\" alt=\"\">")
                                    .append("<div class=\"img-mask\"></div>");
                            final String bodyContent = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>"
                                    + "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>"
                                    + body.replace("<div class=\"img-place-holder\">", sb.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    newsDetailsWebView.getSettings().setSupportZoom(true); // 支持缩放
                                    newsDetailsWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);// 适应屏幕，内容将自动缩放
                                    newsDetailsWebView.getSettings().setJavaScriptEnabled(true);
                                    // newsDetailsWebView.getSettings().setDomStorageEnabled(true);
                                    // newsDetailsWebView.getSettings().setUseWideViewPort(true); // webview推荐使用的窗口
                                    // newsDetailsWebView.getSettings().setLoadWithOverviewMode(true); // 设置webview加载页面的模式
                                    newsDetailsWebView.loadDataWithBaseURL(cssSrc, bodyContent, "text/html", "utf-8", null);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomepageActivity.class);
        startActivity(intent);
        finish();
    }
}
