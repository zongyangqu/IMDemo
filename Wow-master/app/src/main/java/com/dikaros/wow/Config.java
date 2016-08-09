package com.dikaros.wow;

import android.graphics.Bitmap;

import com.dikaros.wow.bean.Friend;
import com.dikaros.wow.bean.ImMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dikaros on 2016/6/11.
 */
public class Config {

    /**
     * app标识
     */
    public static String APP_ID = "com.dikaros.wow";

    /**
     * 用户二维码
     */
    public static Bitmap USER_QR_CODE = null;

    /**
     * 用户id
     */
    public static long userId = 0;

    /**
     * 用户名
     */
    public static String userName;
    /**
     * 用户个人说明
     */
    public static String userMessage;
    /**
     * 接收信息map
     */
    public static HashMap<Long, List<ImMessage>> reveivedMap = new HashMap<>();
    /**
     * 发送信息map
     */
    public static HashMap<Long, List<ImMessage>> sendedMap = new HashMap<>();

    public static HashMap<Long,Friend> friendList = new HashMap<>();

    /**
     * 增加信息到发送信息表
     */
    public static void addToSendMap(ImMessage message) {
        //如果含有字段则增加信息，如果没含有就创建新的
        if (sendedMap.containsKey(message.getSenderId())) {
            sendedMap.get(message.getReceiverId()).add(message);
        } else {
            List<ImMessage> messages = new ArrayList<>();
            messages.add(message);
            sendedMap.put(message.getReceiverId(), messages);
        }
    }

    /**
     * 增加信息到接收信息表
     */
    public static void addToReveivedMap(ImMessage message) {
        //如果含有字段则增加信息，如果没含有就创建新的
        if (reveivedMap.containsKey(message.getSenderId())) {
            reveivedMap.get(message.getSenderId()).add(message);
        } else {
            List<ImMessage> messages = new ArrayList<>();
            messages.add(message);
            reveivedMap.put(message.getSenderId(), messages);
        }
    }

    /**
     * Websocekt session
     */
    public static String WEBSOCKET_SESSION = null;
    /**
     * 计时器等待状态
     */
    public static boolean inWait = false;
    /**
     * websocket地址
     */
    public static final String WEBSOCKET_ADDRESS = "ws://123.206.75.202:8080/WowServer/websocket?userId=";
    /**
     * 登录地址
     */
    public static final String HTTP_LOGIN_ADDRESS = "http://123.206.75.202:8080/WowServer/login.do";
    /**
     * 注册地址
     */
    public static final String REGIST_ADDRESS = "http://123.206.75.202:8080/WowServer/regist.do";
    /**
     * 头像地址
     */
    public static final String HTTP_AVATAR_ADDRESS = "http://123.206.75.202:8080/WowServer";
    /**
     * 查询好友地址
     */
    public static final String HTTP_GET_FRIEND = "http://123.206.75.202:8080/WowServer/friend/query.do";

    /**
     * 增加好友地址
     */
    public static final String HTTP_ADD_FRIEND = "http://123.206.75.202:8080/WowServer/friend/add.do";

}
