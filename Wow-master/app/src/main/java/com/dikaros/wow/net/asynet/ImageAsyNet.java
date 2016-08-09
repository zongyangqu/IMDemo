package com.dikaros.wow.net.asynet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dikaros.wow.net.asynet.util.BitMapUtil;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by mac on 16/4/20.
 */
public class ImageAsyNet extends AsyNet<Bitmap> {




    public boolean isImageCompressed() {
        return imageCompressed;
    }

    public void setImageCompressed(boolean imageCompressed) {
        this.imageCompressed = imageCompressed;
    }

    //是否压缩图片
    boolean imageCompressed = false;


    /**
     * 无参方法
     *
     * @param url
     * @param method
     */
    public ImageAsyNet(String url, NetMethod method) {
        super(url);
        this.method = method;
    }

    /**
     * 基本构造方法传入json或xml文本
     *
     * @param url           请求地址
     * @param key           json或xml 名称
     * @param jsonOrXmlFile json或xml文本
     * @param method
     */
    public ImageAsyNet(String url, String key, String jsonOrXmlFile,
                       NetMethod method) {
        super(url, key, jsonOrXmlFile);
        this.method = method;
    }


    public ImageAsyNet(String url, HashMap keyValuePair, NetMethod method) {
        super(url, keyValuePair);
        this.method = method;
    }


    @Override
    protected Bitmap doInBackground(String... params) {


        try {
            Response response = accessNet();
            //这里直接用同步的方法获取响应
            InputStream is = response.body().byteStream();
            long length = response.body().contentLength();
            int count = 0;
            int len = -1;
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = is.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
                count += len;
                //发送进度单位%
                publishProgress(new Integer[]{(int) (count * 100 / length)});
            }
            byte[] data = bos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            //压缩图片保证图片大小在100kb以内
            if (imageCompressed){
                bitmap =  BitMapUtil.compressImage(bitmap);
            }
            return  bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            cancel(true);
        }
        return null;
    }
}
