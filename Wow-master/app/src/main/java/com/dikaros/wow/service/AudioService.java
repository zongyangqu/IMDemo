package com.dikaros.wow.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dikaros.wow.util.AlertUtil;

import java.io.IOException;

/**
 * 音频服务
 * 主要用于后台播放音频
 * 与ChatActivity绑定
 * @see com.dikaros.wow.ChatActivity
 *
 */
public class AudioService extends Service {

    //音频播放工具
    MediaPlayer mediaPlayer;
    public AudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //初始化
        mediaPlayer = new MediaPlayer();
//        AlertUtil.toastMess(this,"服务启动");
        return new AudioBinder();
    }

    /**
     * binder对象用于控制音频播放器的播放与停止
     */
    public class AudioBinder extends Binder{

        /**
         * 播放音频
         * 如果有音频正在播放则停止当前正在播放的播放新的
         * @param path
         * @throws IOException
         */
        public void play(String path) throws IOException {
            //如果正在播放则停止
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            Log.e("wow",path);
            //重置
            mediaPlayer.reset();
            //设置音频源
            mediaPlayer.setDataSource(path);
            //准备
            mediaPlayer.prepare();
            //播放
            mediaPlayer.start();

        }
    }
}
