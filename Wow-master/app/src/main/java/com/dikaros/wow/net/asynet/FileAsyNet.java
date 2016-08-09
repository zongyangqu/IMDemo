package com.dikaros.wow.net.asynet;

import com.squareup.okhttp.Response;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * Created by mac on 16/4/20.
 */
public class FileAsyNet extends AsyNet<RandomAccessFile> {


    //需要下载的文件
    RandomAccessFile file;


    /**
     * 无参方法
     *
     * @param url
     * @param method
     */
    public FileAsyNet(String url, AsyNet.NetMethod method,RandomAccessFile file) {
        super(url);
        this.file = file;
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
    public FileAsyNet(String url, String key, String jsonOrXmlFile,RandomAccessFile file,
                      AsyNet.NetMethod method) {
        super(url, key, jsonOrXmlFile);
        this.method = method;
        this.file = file;
    }


    public FileAsyNet(String url, HashMap keyValuePair,RandomAccessFile file, AsyNet.NetMethod method) {
        super(url, keyValuePair);
        this.method = method;
        this.file  = file;
    }

    @Override
    protected RandomAccessFile doInBackground(String... params) {


        try {
            Response response = accessNet();
            InputStream is = response.body().byteStream();
            long length = response.body().contentLength();
            int count = 0;
            int len = -1;
            byte[] bytes = new byte[1024];
            while ((len = is.read(bytes)) != -1) {
                file.write(bytes, 0, len);
                count += len;
                //发送进度单位%
                publishProgress(new Integer[]{(int) (count * 100 / length)});
            }
            return file;
        } catch (Exception e) {
            cancel(true);
        }


        return null;
    }
}
