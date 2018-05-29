package com.qwy.magic.magicimage.util;

import android.util.Log;
import com.qwy.magic.magicimage.constant.Constant;
import com.qwy.magic.magicimage.sign.YoutuSign;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.body.RequestBody;
import org.xutils.x;

import java.io.File;

/**
 * Created by qwy on 2016/10/19.
 * 网络请求工具类
 */
public class TecentHttpUtil {

    public static void ocrIdCard(String p, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("http://recognition.image.myqcloud.com/ocr/idcard");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("Host", "recognition.image.myqcloud.com");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "multipart/form-data");
        params.addBodyParameter("card_type", card_type);
        params.addBodyParameter("images", new File(p));
        params.addBodyParameter("appid", Constant.AppID);
        params.addBodyParameter("type", "gravatar");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {
                Log.d("onFinished", "=======onFinished");
            }
        });

    }

    public static void uploadIdCard(String bitmap, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("http://api.youtu.qq.com/youtu/ocrapi/idcardocr");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("Host", "api.youtu.qq.com");
        params.addHeader("user-agent", "youtu-java-sdk");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "text/json");
        params.addParameter("card_type", Integer.valueOf(card_type));
        params.addBodyParameter("image", bitmap);
        params.addBodyParameter("app_id", Constant.AppID);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {

            }
        });

    }

    public interface SimpleCallBack {
        void Succ(String result);

        void error();
    }


}
