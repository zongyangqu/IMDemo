package com.dikaros.wow.net.asynet.block;

import com.google.gson.Gson;

/**
 * Created by Dikaros on 2016/6/8.
 */
public class JsonBlock implements Block {


    Object obj;
    public JsonBlock(Object obj){
        this.obj = obj;
    }

    public JsonBlock(Object obj,String jsonFile){
        this.obj = obj;
        this.jsonFile = jsonFile;
    }

    String jsonFile=null;

    @Override
    public Object doSth(Object... objs) {
        if (jsonFile==null) {
            if (objs.length > 0) {
                String json = (String) objs[0];
                Gson gson = new Gson();
                return gson.fromJson(json,obj.getClass());
            }
        }else {
            Gson gson = new Gson();
            return gson.fromJson(jsonFile,obj.getClass());
        }
        return null;
    }
}
