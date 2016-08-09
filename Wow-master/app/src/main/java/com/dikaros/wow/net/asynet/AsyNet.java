package com.dikaros.wow.net.asynet;

import android.os.AsyncTask;

import com.dikaros.wow.net.asynet.block.Block;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 异步http工具
 * 由于Google在Android 6.0中取消了HttpClient,所以现在使用OkHttp替换HttpClient
 *
 * @param <T>
 * @author dikaros
 * @version 0.3
 */
public abstract class AsyNet<T> extends AsyncTask<String, Integer, T> {


    public enum NetMethod {
        GET, POST
    }

    //参数类型
    public enum ParamType {
        JSON_OR_XML_FILE, KEY_VALUE_PAIR, NO_PARAM
    }

    //http报文头
    HashMap<String, String> header;

    /**
     * 请求方法POST和get
     */
    NetMethod method;


    //设置监听器回调

    public OnNetStateChangedListener<T> getOnNetStateChangedListener() {
        return onNetStateChangedListener;
    }

    public void setOnNetStateChangedListener(OnNetStateChangedListener<T> onNetStateChangedListener) {
        this.onNetStateChangedListener = onNetStateChangedListener;
    }

    /**
     * 回调监听器，在需要使用网络的地方传入监听器，当网络发生变化的时候会调用其中的方法
     */
    OnNetStateChangedListener<T> onNetStateChangedListener;

    Throwable cancledThrowable;

    @Override
    protected void onCancelled() {
        //asynctask取消时
        if (onNetStateChangedListener != null) {
            onNetStateChangedListener.whenException(cancledThrowable);
        }
    }


    @Override
    protected void onPostExecute(T t) {
        if (onNetStateChangedListener != null) {
            onNetStateChangedListener.afterAccessNet(t);
        }
    }

    @Override
    protected void onPreExecute() {
        if (onNetStateChangedListener != null) {
            onNetStateChangedListener.beforeAccessNet();
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        if (onNetStateChangedListener != null) {
            if (values.length > 1) {
                onNetStateChangedListener.onProgress(values[0]);
            }
        }
    }


    protected Response accessNet() throws IOException {
        //响应
        Response response = null;
        //call
        Call call = null;
        //判断请求的类型是post还是get
        switch (method) {
            case POST:
                FormEncodingBuilder postParam = new FormEncodingBuilder();
                switch (type) {
                    //无参数的不操作
                    case NO_PARAM:
                        break;
                    //单一参数的增加一个键值对
                    case JSON_OR_XML_FILE:
                        postParam.add(key, jsonOrXmlFile);
                        break;
                    //增加一堆键值对
                    case KEY_VALUE_PAIR:
                        for (String pk : keyValuePair.keySet()
                                ) {
                            postParam.add(pk, keyValuePair.get(pk).toString());
                        }
                        break;
                }
                Request.Builder postBuilder = new Request.Builder();
                //循环添加http报文头
                for (String headerKey : header.keySet()
                        ) {
                    //添加报文头
                    postBuilder.addHeader(headerKey, header.get(headerKey));
                }
                //构建请求
                Request postRequest = postBuilder.url(url).post(postParam.build()).build();
                call = client.newCall(postRequest);
                break;
            case GET:
                String param = "?";
                switch (type) {
                    case NO_PARAM:
                        param = "";
                        break;
                    case JSON_OR_XML_FILE:
                        param += (key + "=" + jsonOrXmlFile);
                        break;
                    case KEY_VALUE_PAIR:
                        for (String k : keyValuePair.keySet()
                                ) {
                            //补充param
                            param += k + "=" + keyValuePair.get(k).toString() + "&";
                        }
                        //去掉最后的&
                        if (param.endsWith("&")) {
                            param = param.substring(0, param.length() - 1);

                        }
                        break;
                }

                //创建请求的构造器并添加http报文头
                Request.Builder getBuilder = new Request.Builder();
                //循环添加http报文头
                for (String headerKey : header.keySet()
                        ) {
                    //添加报文头
                    getBuilder.addHeader(headerKey, header.get(headerKey));
                }
                //请求
                Request request = getBuilder.url(url + param).build();
                call = client.newCall(request);
        }
        response = call.execute();
        return response;
    }

    /**
     * 网络状态改变时候的监听器
     *
     * @param <T>
     */
    public interface OnNetStateChangedListener<T> {
        /**
         * 访问网络之前
         */
        public void beforeAccessNet();

        /**
         * 访问网络成功获取数据之后
         *
         * @param result
         */
        public void afterAccessNet(T result);

        /**
         * 出现了错误的时候
         */
        public void whenException(Throwable t);

        /**
         * 执行进度
         *
         * @param progress
         */
        public void onProgress(Integer progress);
    }

    //OkHttp;
    OkHttpClient client;


    //请求地址
    String url;

    //数据
    String jsonOrXmlFile;
    String key;

    ParamType type;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getJsonOrXmlFile() {
        return jsonOrXmlFile;
    }

    public void setJsonOrXmlFile(String jsonOrXmlFile) {
        this.jsonOrXmlFile = jsonOrXmlFile;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<String, Object> getKeyValuePair() {
        return keyValuePair;
    }

    public void setKeyValuePair(HashMap<String, Object> keyValuePair) {
        this.keyValuePair = keyValuePair;
    }

    HashMap<String, Object> keyValuePair;

    /**
     * 无参构造方法,初始化OkHttpClient
     */
    protected AsyNet() {
        client = new OkHttpClient();
        header = new HashMap<>();
        blocks = new ArrayList<>();
        client.setConnectTimeout(connectTimeOut, TimeUnit.SECONDS);
    }

    long connectTimeOut = 5;

    public long getConnectTimeOut() {
        return connectTimeOut;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeOut
     */
    public void setConnectTimeOut(long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        client.setConnectTimeout(connectTimeOut, TimeUnit.SECONDS);
    }

    /**
     * 使用json或xml为参数的构造方法
     *
     * @param url
     * @param key
     * @param jsonOrXmlFile
     */
    protected AsyNet(String url, String key, String jsonOrXmlFile) {
        this();
        this.url = url;
        this.key = key;
        this.jsonOrXmlFile = jsonOrXmlFile;
        this.keyValuePair = null;
        type = ParamType.JSON_OR_XML_FILE;
    }

    /**
     * 没有参数的方法
     *
     * @param url
     */
    protected AsyNet(String url) {
        this();
        this.url = url;
        type = ParamType.NO_PARAM;
    }

    /**
     * 使用键值对的构造方法
     *
     * @param url
     * @param keyValuePair
     */
    protected AsyNet(String url, HashMap keyValuePair) {
        this();
        this.url = url;
        this.keyValuePair = keyValuePair;
        this.key = null;
        type = ParamType.KEY_VALUE_PAIR;
    }

    /**
     * 执行
     */
    public void execute() {
        //执行方法,设置同时执行的线程数量为5
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{});
    }

    public void addHeader(String key, String value) {
        header.put(key, value);

    }

    //块集合
    List<Block> blocks;



    /**
     * 增加块
     * @param block
     */
    public void addBlock(Block block){
            blocks.add(block);
    }

    /**
     * 增加块
     * @param index
     * @param block
     */
    public void addBlock(int index, Block block){
        blocks.add(index,block);
    }
}
