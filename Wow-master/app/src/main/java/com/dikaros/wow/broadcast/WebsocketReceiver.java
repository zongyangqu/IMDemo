package com.dikaros.wow.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.dikaros.wow.service.WebSocketService;

/**
 * Created by Dikaros on 2016/6/11.
 */
public class WebSocketReceiver extends BroadcastReceiver {
    Context context;

    WebSocketReceiver(Context context) {
        this.context = context;

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WebSocketService.ACTION_WEBSOCKET_CLOSE:

                break;
            case WebSocketService.ACTION_WEBSOCKET_ON_MESSAGE:



                break;
            case WebSocketService.ACTION_WEBSOCKET_OPEN:
                //连接成功时


                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
//                    AlertUtil.simpleAlertDialog(ShowActivity.this,"网络状态改变", NetUtil.isNetworkAvailable(ShowActivity.this)+"");
//                //如果可以连接网络
//                if (!connecting && NetUtil.isNetworkAvailable(MainShowActivity.this)) {
//                    if (!NetUtil.isWifi(MainShowActivity.this)) {
//                        AlertUtil.simpleAlertDialog(MainShowActivity.this, "提醒", "当前处于移动网络状态，可能产生大量流量");
//                    }
//                    Intent service = new Intent(MainShowActivity.this, WebSocketService.class);
//                    service.putExtra(WebSocketService.START_WEBSOCKET, true);
//                    startService(service);
//                }
                break;

            default:
                break;
        }


    }
}
