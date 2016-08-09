package com.dikaros.wow.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dikaros on 2016/5/23.
 */
public class NetUtil {

    /**
     * 获取网络状态
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        //获取网络事件管理器
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取不到则返回false
        if (connectivity == null) {
            return false;
        } else {
            //获取到了查询当前的网络状态
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    //如果连接上了网络则返回true
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**
     * 判断当前是否使用了wifi
     * @param mContext
     * @return
     */
    public static boolean isWifi(Context mContext) {
        //获取网络事件管理器
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络信息
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        //检查状态
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 发送短信验证码（同步）
     *
     * @param phoneNumber 手机号
     * @param content 验证码
     */
    public static String sendSmsCode(String phoneNumber, String content) {
        if (phoneNumber == null || content == null) {
            return null;
        }
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        //地址
        String httpUrl = "http://apis.baidu.com/baidu_communication/sms_verification_code/smsverifycode";
        //参数
        String httpArg = "phone=" + phoneNumber + "&content=" + content;
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            //创建并打开连接
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            //http 请方法
            connection.setRequestMethod("GET");
            // api key
            connection.setRequestProperty("apikey",
                    "db642b2fac4fafe26849179ad8883592 ");
            //连接api
            connection.connect();
            //获取返回信息
            InputStream is = connection.getInputStream();
            //读取
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            //关闭读取器
            reader.close();
            //返回结果
            result = sbf.toString();
//            Log.i("result", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
