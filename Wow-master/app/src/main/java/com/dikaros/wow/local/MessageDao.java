package com.dikaros.wow.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dikaros.wow.Config;
import com.dikaros.wow.bean.ImMessage;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dikaros on 2016/6/20.
 * 单例信息信息记录类
 */
public class MessageDao {
    //可操作的数据库
    SQLiteDatabase db;

    private static MessageDao instance;

    /**
     * 私有构造函数
     *
     * @param c 上下文
     */
    private MessageDao(Context c) {
        MessageDbHelper helper = new MessageDbHelper(c);
        //获取可写的数据库
        db = helper.getWritableDatabase();
    }

    /**
     * 懒加载
     *
     * @param context
     * @return
     */
    public static MessageDao getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDao(context);
        }
        return instance;
    }

    /**
     * 保存
     *
     * @param imMessage
     */
    public void save(String imMessage) {
        ContentValues cv = new ContentValues();
        cv.put("msg", imMessage);
        db.insert(MessageDbHelper.TABLE_NAME, null, cv);
    }


    /**
     * 获取信息
     * @param friendId
     * @param isSender
     * @return
     */
    public List<ImMessage> queryByFriendId(long friendId, boolean isSender) {
        ArrayList<ImMessage> messages = new ArrayList<>();
        Cursor cursor = db.query(MessageDbHelper.TABLE_NAME, null, null, null, null, null, null);
        Gson gson = new Gson();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String json = cursor.getString(cursor.getColumnIndex("msg"));
                ImMessage imMessage = gson.fromJson(json, ImMessage.class);

                if (!isSender && imMessage.getSenderId() == friendId) {
                    messages.add(imMessage);
                } else if (isSender&&imMessage.getReceiverId() == friendId) {
                    messages.add(imMessage);
                }

            }
        }

        return messages;

    }
}
