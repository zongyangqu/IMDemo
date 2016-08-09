package com.dikaros.wow.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dikaros on 2016/6/20.
 * 数据管理类
 */
public class MessageDbHelper extends SQLiteOpenHelper {


    //表名
    public static final String TABLE_NAME="messages";

    //信息的字段名
    public static final String COLOMN_MESSAGE = "msg";

    //创建表
    public static final String SQL_CTEATE="create table messages(_id primary key ,msg text);";

    /**
     * 构造方法
     * @param context
     */
    public MessageDbHelper(Context context) {
        super(context, "message.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //执行创建表sql语句
        db.execSQL(SQL_CTEATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
