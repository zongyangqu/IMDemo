package com.example.administrator.imtest.core;

import android.util.Log;

import com.example.administrator.imtest.bean.Message;
import com.example.administrator.imtest.global.LocalConstant;
import com.example.administrator.imtest.listener.MessageReceiveListener;
import com.example.administrator.imtest.listener.RemoteServerStatusListenner;
import com.example.administrator.imtest.util.ConnectionRegisterException;
import com.example.administrator.imtest.util.ThreadManager;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 类名称：
 * 类描述：
 * 创建人：quzongyang
 * 创建时间：2016/8/9. 15:36
 * 版本：
 */
public class WebSocketConnection {

    private WebSocketClient mClient; //消息连接通道
    private List<MessageReceiveListener> mMessageReceiveListeners = new ArrayList<>();
    private List<RemoteServerStatusListenner> mRemoteServerStatusListeners = new ArrayList<>();
    private static volatile WebSocketConnection mInstance;

    private WebSocketConnection() {

    }


    /***
     * 获取实例对象
     *
     * @return
     */
    public static WebSocketConnection getInstance() {
        if (mInstance == null) {
            synchronized (WebSocketConnection.class) {
                if (mInstance == null) {
                    mInstance = new WebSocketConnection();
                }
            }
        }
        return mInstance;
    }

    /**
     * 判断当前连接是否正常
     *
     * @return
     */
    public boolean socketIsConnected() {
        if (mClient != null) {
            if (mClient.getReadyState() == WebSocket.READYSTATE.OPEN) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /***
     * 添加到消息监听
     *
     * @param _listener
     */
    public void addMessageReceiveListener(MessageReceiveListener _listener) {
        if (_listener != null && mMessageReceiveListeners != null && !mMessageReceiveListeners.contains(_listener))
            mMessageReceiveListeners.add(_listener);
    }

    /***
     * 移除消息监听
     *
     * @param _listener
     */
    public void removeMessageReceiveListenner(MessageReceiveListener _listener) {
        if (_listener != null && mMessageReceiveListeners != null) {
            mMessageReceiveListeners.remove(_listener);
        }
    }

    /***
     * 添加消息通道状态监听
     *
     * @param _listener
     */
    public void addRemoteServerStatusListener(RemoteServerStatusListenner _listener) {
        if (_listener != null && mRemoteServerStatusListeners != null && !mRemoteServerStatusListeners.contains(_listener))
            mRemoteServerStatusListeners.add(_listener);
    }

    /***
     * 移除消息通道状态监听
     *
     * @param _listener
     */
    public void removeRemoteServerStatusListener(RemoteServerStatusListenner _listener) {
        if (_listener != null && mRemoteServerStatusListeners != null) {
            mRemoteServerStatusListeners.remove(_listener);
        }
    }

    /**
     * 初始化连接通道
     **/
    private void init() {
        try {
            mClient = new WebSocketClient(new URI(LocalConstant.REMOTE_ADDRESS + ":" + LocalConstant.REMOTE_PORT), new Draft_17()) {
                @Override
                public void onOpen(final ServerHandshake handshakedata) {
                    ThreadManager.newInstance().executeShortTack(new Runnable() {
                        @Override
                        public void run() {
                            for (RemoteServerStatusListenner listener : mRemoteServerStatusListeners) {
                                if (listener != null) {
                                    listener.on0pen(handshakedata);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onMessage(final String message) {
                    ThreadManager.newInstance().executeShortTack(new Runnable() {
                        @Override
                        public void run() {
                            for (MessageReceiveListener listener : mMessageReceiveListeners) {
                                if (listener != null) {
                                    listener.onMessageReceive(message);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {
                    ThreadManager.newInstance().executeShortTack(new Runnable() {
                        @Override
                        public void run() {
                            for (RemoteServerStatusListenner listener : mRemoteServerStatusListeners) {
                                if (listener != null) {
                                    listener.onClose(code, reason, remote);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onError(final Exception ex) {
                    ThreadManager.newInstance().executeShortTack(new Runnable() {
                        @Override
                        public void run() {
                            for (RemoteServerStatusListenner listener : mRemoteServerStatusListeners) {
                                if (listener != null) {
                                    listener.onError(ex);
                                }
                            }
                        }
                    });
                }
            };
            WebSocketImpl.DEBUG = true;
            System.setProperty("java.net.preferIPv6Addresses", "false");
            System.setProperty("java.net.preferIPv4Stack", "true");
        } catch (URISyntaxException _e) {
            _e.printStackTrace();
        }
    }

    /***
     * 连接夫妇器
     */
    private void open() throws IllegalStateException {
        if (mClient == null) {
            init();
        }
        WebSocket tempConnection = mClient.getConnection();
        /**判断通道是否已经打开，如果没有打开，再去连接**/
        if (tempConnection != null) {
            Log.i(WebSocketConnection.class.getSimpleName(), tempConnection.getReadyState() + "");
        }
        if (tempConnection.getReadyState() != WebSocket.READYSTATE.OPEN) {
            mClient.connect();
        }
    }


    /***
     * 注册服务
     */
    public void registerService() throws ConnectionRegisterException {
        try {
            open();
        } catch (IllegalStateException exception) {
            throw new ConnectionRegisterException("websocket通道连接异常");
        }
    }

    /****
     * 断开连接
     */
    public void close() throws Exception {
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }

    /***
     * 发送消息
     *
     * @param _message
     */
    public void sendMessage(final Message _message) {
        if (mClient != null) {
            ThreadManager.newInstance().executeShortTack(new Runnable() {
                @Override
                public void run() {
                    mClient.send(_message.toJson());
                }
            });
        }
    }
}
