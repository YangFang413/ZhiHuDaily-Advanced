package activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.myzhihu.R;

import org.json.JSONException;

import java.io.IOException;

import utility.HttpUtil;
import utility.ParseUtility;

public class ScreenOpenActivity extends Activity {

    private ImageView openScreenImage;
    private TextView authorNameText;
    private static final int MESSAGE_START_ACTIVITY = 0;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_screen_open);

        openScreenImage = (ImageView) findViewById(R.id.screen_open_image);
        authorNameText = (TextView) findViewById(R.id.author_name_text);

        if (HttpUtil.networkCheck(this)){
            new DownloadTask().execute();
            new TimeCountDownTask().execute();

            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MESSAGE_START_ACTIVITY:
                            Intent i = HomepageActivity.newIntent(getApplicationContext());
                            startActivity(i);
                            finish();
                    }
                }
            };
        } else {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }

    }

    // 异步加载图片
    private class DownloadTask extends AsyncTask<Void, Void, Bitmap> {

        private String authorName;

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                String response = HttpUtil.getUrlString("http://news-at.zhihu.com/api/4/start-image/1080*1776");
                authorName = ParseUtility.getJO(response).getString("text");
                byte[] bytes = HttpUtil.getUrlByte(ParseUtility.parseJSONResponse(response));
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            openScreenImage.setImageBitmap(bitmap);
            authorNameText.setText(authorName);
        }
    }

    // 异步倒计时任务
    private class TimeCountDownTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.what = MESSAGE_START_ACTIVITY;
                    mHandler.sendMessage(message);
                }
            });
            return null;
        }
    }
}
