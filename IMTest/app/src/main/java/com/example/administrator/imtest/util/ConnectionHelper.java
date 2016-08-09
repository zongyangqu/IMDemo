package com.example.administrator.imtest.util;

import android.content.Context;
import android.content.Intent;

import com.example.administrator.imtest.bean.Message;
import com.example.administrator.imtest.core.WebSocketConnection;
import com.example.administrator.imtest.listener.MessageReceiveListener;
import com.example.administrator.imtest.service.WebSocketService;


/*
 *
 * 作 者 :quzongyang
 *
 * 版 本 :1.0
 *
 * 创建日期 :2016/8/8  13:40
 *
 * 描 述 :消息通道工具类
 *
 * 修订日期 :
 */
public class ConnectionHelper {

    /***
     * 开启消息服务通道
     *
     * @param _context
     * @throws Exception
     */
    public static void startService(Context _context) throws ConnectionRegisterException {
        if (!AppUtils.isServiceWork(_context, "com.example.administrator.imtest.service.WebSocketService")) {
            _context.startService(new Intent(_context, WebSocketService.class));
        }
    }

    /***
     * 添加消息监听
     *
     * @param _listener
     */
    public static void addMessageReceiveListener(MessageReceiveListener _listener) {
        if (_listener != null) {
            WebSocketConnection.getInstance().addMessageReceiveListener(_listener);
        }
    }

    /***
     * 移除消息监听
     *
     * @param _listener
     */
    public static void removeMessageReceiveListener(MessageReceiveListener _listener) {
        if (_listener != null) {
            WebSocketConnection.getInstance().removeMessageReceiveListenner(_listener);
        }
    }

    /**
     * 初始化服务通道
     **/
    public static void registerService() throws ConnectionRegisterException {
        WebSocketConnection.getInstance().registerService();
    }


    /***
     * 发送消息
     *
     * @param _message
     */
    public static void sendMessage(Message _message) {
        WebSocketConnection.getInstance().sendMessage(_message);
    }

    /**
     * 关闭连接
     */
    public static void closeConnection() throws Exception {
        WebSocketConnection.getInstance().close();
    }

    /***
     * 检查服务器通道是否处于连接状态
     *
     * @return
     */
    public static boolean checkIsConnected() {
        return WebSocketConnection.getInstance().socketIsConnected();
    }

    /***
     * 重连服务器
     *
     * @throws Exception
     */
    public static void reConnect() throws Exception {
        closeConnection();
        registerService();
    }
}
