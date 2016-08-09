package com.dikaros.wow.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import com.dikaros.wow.Config;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Util {

    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    /**
     * 获取md5
     *
     * @param str
     * @return
     */
    public static String getMd5Code(String str) {
        String md5 = null;
        try {
            //使用系统的方法回去MD5码
            MessageDigest digest = MessageDigest.getInstance("MD5 ");
            byte[] buf = digest.digest(str.getBytes());
            BigInteger b = new BigInteger(buf);
            //转换成16进制
            md5 = b.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     * 设置preference
     *
     * @param context
     * @param key
     * @param token
     */
    public static void setPreference(Context context, String key, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.APP_ID,
                Context.MODE_PRIVATE);
        //打开编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //编辑
        editor.putString(key, token);
        //提交
        editor.commit();

    }

    public static void setPreferenceSet(Context context, String key, Set<String> set) {
        //设置内部存储字段
        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.APP_ID,
                Context.MODE_PRIVATE);
        //打开编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //编辑
        editor.putStringSet(key, set);
        //提交
        editor.commit();
    }

    public static Set<String> getPreferenceSet(Context context, String key) {
        //返回id为Config.APP_ID的内部存储字段
        return context
                .getSharedPreferences(Config.APP_ID, Context.MODE_PRIVATE).getStringSet(key, null);
    }


    /**
     * 获取Preference
     *
     * @param context
     * @param key
     * @return
     */
    public static String getPreference(Context context, String key) {
        //返回id为Config.APP_ID的内部存储字段
        return context
                .getSharedPreferences(Config.APP_ID, Context.MODE_PRIVATE)
                .getString(key, null);
    }

    public static boolean getBooleanPreference(Context context,String key){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key,true);
    }

    /**
     * 将文字转化为base64格式
     *
     * @param str
     * @return
     */
    public static String toBase64(String str) {
        //读取的str的byte数组
        byte[] b = null;
        //返回结果
        String s = null;
        try {
            //读取
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            try {
                //转换
                s = new String(Base64.encode(b, Base64.DEFAULT), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    /**
     * 将文字转化为base64格式
     *
     * @param str
     * @return
     */
    public static String toBase64(byte[] str) {
        String s = null;
        if (str != null) {
            try {
                s = new String(Base64.encode(str, Base64.DEFAULT), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    /**
     * 将base64转化为文字
     *
     * @param base64
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getFromBase64(String base64) {
        try {
            return new String(Base64.decode(base64.getBytes(), Base64.DEFAULT), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 将base64转存为文件
     *
     * @param base64
     * @return
     */
    public static String storeFileFromBase64(Context context, String base64) {
        try {
            String fileName = System.currentTimeMillis() + ".amr";
            byte[] bytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
//            File path = context.getDir("/voice", context.MODE_PRIVATE);
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wow/audio";
            File paths = new File(path);
            if (!paths.exists()) {
                paths.mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(new File(path + "/" + fileName), true);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            Log.e("wow", path + "/" + fileName);
            return path + "/" + fileName;
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("wow", e.getMessage());
        }
        return null;
    }

    /**
     * 获取Bitmap
     *
     * @param base64
     * @return
     */
    public static Bitmap getBitmapFromBase64(String base64) {
        //使用Base64解码获取图片
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /**
     * 根据uri获取系统音频
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getAudioPathFromUri(Context context, Uri uri) {
//        String[] proj = {MediaStore.Audio.Media.DATA};
//        //查询系统数据库
//
//        Cursor actualAudioCursor = activity.getContentResolver().query(uri,proj,null,null,null);
//        //移动到第一个元素
//        actualAudioCursor.moveToFirst();
//        //获得查询项的index
//        int actual_audio_column_index = actualAudioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//        //获取地址
//        String audio_path = actualAudioCursor.getString(actual_audio_column_index);
//
//        return audio_path;
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 将文字转化成图片
     *
     * @param str
     */
    public static Bitmap generateQrCode(String str) throws WriterException {
        int QR_WIDTH = 200; // 图像宽度
        int QR_HEIGHT = 200; // 图像高度

        Hashtable<EncodeHintType, String> hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //图像数据转换，使用了矩阵转换
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
        } catch (WriterException e) {
        }
        int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
        //下面这里按照二维码的算法，逐个生成二维码的图片，
        //两个for循环是图片横列扫描的结果
        for (int y = 0; y < QR_HEIGHT; y++) {
            for (int x = 0; x < QR_WIDTH; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * QR_WIDTH + x] = 0xff000000;
                } else {
                    pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
        }
        //生成二维码图片的格式，使用ARGB_8888
        Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
        return bitmap;
    }
}
