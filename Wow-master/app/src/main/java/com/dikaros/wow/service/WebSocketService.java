package com.dikaros.wow.service;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.dikaros.wow.ChatActivity;
import com.dikaros.wow.Config;
import com.dikaros.wow.R;
import com.dikaros.wow.bean.ImMessage;
import com.dikaros.wow.net.websocket.BaseWebSocketClient;
import com.dikaros.wow.util.Util;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.List;

/**
 * 聊天服务
 */
public class WebSocketService extends Service {


    //临时数据计数器，记录传来的数据条数
    public static int messageCount = 0;

    //websocket打开
    public static final String ACTION_WEBSOCKET_OPEN = "com.dikaros.websocket.open";
    //websocket收到数据
    public static final String ACTION_WEBSOCKET_ON_MESSAGE = "com.dikaros.websocket.onmessage";

    //websocket关闭
    public static final String ACTION_WEBSOCKET_CLOSE = "com.dikaros.websocket.close";

    public static final String DATA_MESSAGE_COUNT = "websocket_message_count";
    //websocket关闭原因
    public static final String WEBSOCKET_CLOSE_REASON = "websocket_close_by_remote";

    //websocket信息
    public static final String WEBSOCKET_MESSAGE = "websocket_message";

    //启动websocket
    public static final String START_WEBSOCKET = "startWebSocketClient";

    //关闭websocekt
    public static final String CLOSE_WEBSOCKET = "closeWebSokcetClient";

    public static final String USER_ID = "user_id";

    public static final String SEND_MESSAGE = "send_message";

    public WebSocketService() {

    }

    boolean startOnce = false;

    boolean connected = false;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    //WebSocket客户端
    BaseWebSocketClient client = null;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        通过startService调用Service
        如果意图不为空则判断其中是否附带有START_WEBSOCKET,CLOSE_WEBSOCKET,SEND_MESSAGE等参数
        如果有则分别执行启动服务，关闭服务和发送信息的行为
         */
        if (intent != null) {
            //启动服务
            boolean start = intent.getBooleanExtra(START_WEBSOCKET, false);
            long id = intent.getLongExtra(USER_ID, 0);
            if (start && client == null) {
                doConnectWebSocket(id);
            }

            //关闭服务
            boolean close = intent.getBooleanExtra(CLOSE_WEBSOCKET, false);
            if (close && client != null) {
                client.close();
                client = null;
            }

            //发送信息
            String message = intent.getStringExtra(SEND_MESSAGE);
            if (message != null && client != null) {
                Log.e("websocket", "send" + message);
                //发送信息
                client.send(message);
                //保存到数据库
//                MessageDao.getInstance(this).save(message);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        client = null;
        super.onDestroy();
    }

    Gson gson = new Gson();

    /**
     * 创建并连接websocket
     */
    private synchronized void doConnectWebSocket(long id) {
        try {
            client = new BaseWebSocketClient(Config.WEBSOCKET_ADDRESS + id);
            Log.e("websocket connect", "userId:" + id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client.setWebSocketListener(new BaseWebSocketClient.WebSocketClientListener() {
            @Override
            public void onOpen() {
                Log.e("websocket", "websocket open" + Config.userId);
                Intent openIntent = new Intent();
                openIntent.setAction(ACTION_WEBSOCKET_OPEN);
                sendBroadcast(openIntent);
                startOnce = true;
                connected = true;
            }

            @Override
            public void onMessage(String message) {
                messageCount++;
                Log.e("websocket", "websocket onMessage" + messageCount);
                //保存信息到数据库
//                MessageDao.getInstance(WebSocketService.this).save(message);
                ImMessage msg = gson.fromJson(message, ImMessage.class);
                if (msg != null) {
                    Config.addToReveivedMap(msg);
                    //保存本地信息
                }
                //构建intent准备发送广播
                Intent messageIntent = new Intent();
                //设置Action为Websocekt有信息
                messageIntent.setAction(ACTION_WEBSOCKET_ON_MESSAGE);
                //信息内容
                messageIntent.putExtra(WEBSOCKET_MESSAGE, msg);
                //信息计数器
                messageIntent.putExtra(DATA_MESSAGE_COUNT, messageCount);
                //发送广播
                sendBroadcast(messageIntent);
                //如果用户设置中通知是开启状态
                if (Util.getBooleanPreference(WebSocketService.this, "notification_stat")) {
                    //获得活动管理器
                    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    //获取当前互动栈顶的活动信息
                    List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
                    String taskTop = null;
                    if (runningTaskInfos != null) {
                        taskTop = runningTaskInfos.get(0).topActivity.getShortClassName();
                    }
                    Log.e("wow", taskTop + "");
                    //如果不是聊天活动则发送一条通知
                    if (!taskTop.equals(".ChatActivity")) {
                        Log.e("wow", "启动通知");
                        //通知内容
                        String sendMessage = null;
                        if (msg.getType() == 1) {
                            sendMessage = Util.getFromBase64(msg.getMsg());
                        } else if (msg.getType() == 2) {
                            sendMessage = "[图片]";
                        } else if (msg.getType() == 3) {
                            sendMessage = "[语音]";
                        }
                        //构建一条通知
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(WebSocketService.this)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("你有一条新信息")
                                        .setContentText(sendMessage + "")
                                        .setWhen(System.currentTimeMillis())
                                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                                        .setAutoCancel(true);
                        // 创建意图
                        Intent resultIntent = new Intent(WebSocketService.this, ChatActivity.class);
                        long notifiTime = System.currentTimeMillis();
                        resultIntent.putExtra("notifiContent", "你有一条新信息");
                        resultIntent.putExtra("notifiTime", notifiTime);
                        resultIntent.putExtra("friend", Config.friendList.get(msg.getSenderId()));
                        // 通过TaskStackBuilder创建PendingIntent对象
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(WebSocketService.this);
                        stackBuilder.addParentStack(ChatActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        1,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        //获取通知管理器
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        //发送通知
                        mNotificationManager.notify(1, mBuilder.build());
                    }
                }

            }

            @Override
            public void onClose(boolean remote, String message) {
                //websocket关闭的时候的回调
                messageCount = 0;
                //发送关闭时的广播
                Intent closeIntent = new Intent();
                closeIntent.setAction(ACTION_WEBSOCKET_CLOSE);
                closeIntent.putExtra(WEBSOCKET_CLOSE_REASON, remote);
                sendBroadcast(closeIntent);
                Log.e("websocket", "websocket close" + message);
                connected = false;
                // 试着重新连接
                client.close();
                client = null;

            }

        });
        //连接服务器
        client.connect();
    }



}
