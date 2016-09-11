package utility;

import android.graphics.Bitmap;

import org.json.JSONException;

/**
 * Created by Administrator on 2016/8/14.
 * 方法回调的监听器接口
 */
public interface HttpCallBackListener {
    void onFinish(byte[] bytes);
    void onError(Exception e);
}
