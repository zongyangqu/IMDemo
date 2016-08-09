package com.dikaros.wow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dikaros.wow.bean.Friend;
import com.dikaros.wow.bean.ImMessage;
import com.dikaros.wow.local.MessageDao;
import com.dikaros.wow.service.AudioService;
import com.dikaros.wow.service.WebSocketService;
import com.dikaros.wow.util.AlertUtil;
import com.dikaros.wow.util.SimpifyUtil;
import com.dikaros.wow.util.Util;
import com.dikaros.wow.util.annotation.FindView;
import com.dikaros.wow.util.annotation.OnClick;
import com.dikaros.wow.view.AudioView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.java_websocket.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Friend friend;

    //接收器
    MyReceiver receiver;

    @FindView(R.id.et_conversation)
    EditText etConversation;

    @FindView(R.id.btn_conversation_send)
    Button btnSend;

    @FindView(R.id.srl_chat)
    SwipeRefreshLayout srlChat;

    @FindView(R.id.rcv_chat)
    RecyclerView rcvChat;

    @FindView(R.id.ll_sp_area)
    LinearLayout llSpArea;
    //音频播放器
    MediaPlayer player;

    //适配器
    ChatAdapter adaper;

    //音乐binder
    AudioService.AudioBinder binder;

    //服务连接器
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("wow","服务启动");
//            AlertUtil.toastMess(ChatActivity.this,"服务启动");
            binder = (AudioService.AudioBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //音频
    public static final int ACTION_AUDIO = 0;

    public static final int ACTION_VIDEO = 1;

    public static final int ACTION_IMAGE = 2;

    //摄像头
    public static final int ACTION_CARAMA = 3;

    //当前缓存的信息
    List<ImMessage> messages = new ArrayList<>();

    int startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        friend = (Friend) getIntent().getSerializableExtra("friend");
        startPosition = getIntent().getIntExtra("start_position", 0);
        SimpifyUtil.findAll(this);
        btnSend.setEnabled(false);
        receiver = new MyReceiver();
//        messages.addAll(MessageDao.getInstance(this).queryByFriendId(friend.getFriendId(),true));
//        messages.addAll(MessageDao.getInstance(this).queryByFriendId(friend.getFriendId(),false));


        //增加接收的历史信息
        if (Config.reveivedMap.containsKey(friend.getFriendId())) {
            //添加进来
            messages.addAll(Config.reveivedMap.get(friend.getFriendId()));
        }

        //增加发送的历史信息
        if (Config.sendedMap.containsKey(friend.getFriendId())) {
            messages.addAll(Config.sendedMap.get(friend.getFriendId()));
        }

        //排序
        Collections.sort(messages);

        //线性布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置从底部开始
        layoutManager.setStackFromEnd(true);
        //设置垂直布局
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //为RecyclerView设置布局管理器
        rcvChat.setLayoutManager(layoutManager);

        //增加适配器
        adaper = new ChatAdapter();
        rcvChat.setAdapter(adaper);
        srlChat.setEnabled(false);

        //设置文字输入框的文字改变事件监听器
        etConversation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*
                如果输入框没有文字
                则设置发送按钮不可用
                否则设置可用
                 */
                if (s.toString().length() > 0) {
                    btnSend.setEnabled(true);
                } else {
                    btnSend.setEnabled(false);
                }
            }
        });

        //移动到上次关闭的位置
        rcvChat.scrollToPosition(messages.size() - startPosition);

        Intent service = new Intent(this,AudioService.class);

        bindService(service,connection,BIND_AUTO_CREATE);

        setTitle(friend.getFriendMark()!=null?friend.getFriendMark():friend.getFriendName());
        setTitle("hehe");

    }

    /**
     * 点击特殊功能区相机按钮
     *
     * @param v
     */
    @OnClick(R.id.btn_chat_camera)
    public void sendCameraIntent(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, ACTION_CARAMA);
    }


    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //如果是照相机信息
                case ACTION_CARAMA:
                    //创建一个异步任务并执行
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {
                            //获取data
                            Bundle bundle = data.getExtras();
                            //获取摄像头捕捉的bitmap
                            Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                            //创建字节输出流
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            //将bitmap输出到字节流中
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, b);// 把数据写入文件
                            try {
                                //刷新字节流
                                b.flush();
                                b.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //图片转文字
                            String msg = Util.toBase64(b.toByteArray());
                            //发送信息
                            return msg;
                        }

                        @Override
                        protected void onPostExecute(String msg) {
                            //耗时操作执行完成后更新数据
                            if (msg != null) {
                                sendMessage(msg, 2);
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                //如果是录音信息
                case ACTION_AUDIO:
                    //创建一个异步任务并执行
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... params) {

                            try {

                                //获取data
                                Uri uri = data.getData();
                                //根据uri获取文件名
                                String fileName = Util.getAudioPathFromUri(ChatActivity.this, uri);

//                                AlertUtil.toastMess(ChatActivity.this,fileName);
                                Log.e("wow",fileName);
                                //创建字节输出流
                                ByteArrayOutputStream b = new ByteArrayOutputStream();
                                //文件输入流
                                FileInputStream fis = new FileInputStream(fileName);
                                int len;
                                byte[] buf = new byte[4096];
                                while ((len = fis.read(buf)) != -1) {
                                    //写入字节流
                                    b.write(buf, 0, len);
                                }
                                //刷新字节流
                                b.flush();

                                b.close();
                                //音频转文字
                                String msg = Util.toBase64(b.toByteArray());
                                //发送信息
                                return msg;
                            } catch (Exception e) {
                                Log.e("wow",e.toString()+"-");
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String msg) {
                            //耗时操作执行完成后更新数据
//                            AlertUtil.toastMess(ChatActivity.this,msg+"--");
                            if (msg != null) {
                                sendMessage(msg, 3);
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
            }
        }
    }

    /**
     * 通过按钮发送文字信息
     *
     * @param v
     */
    @OnClick(R.id.btn_conversation_send)
    public void sendMessage(View v) {
        Intent intent = new Intent(this, WebSocketService.class);
        ImMessage message = new ImMessage(Config.userId, friend.getFriendId(), System.currentTimeMillis(), Util.toBase64(etConversation.getText().toString()), 1);
        intent.putExtra(WebSocketService.SEND_MESSAGE, message.toJson());
        startService(intent);
        etConversation.setText("");
        messages.add(message);
        Config.addToSendMap(message);
        adaper.notifyItemInserted(messages.size() - 1);
        rcvChat.scrollToPosition(messages.size() - 1);
    }

    @OnClick(R.id.btn_chat_ptt)
    public void showAudioPanel(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/amr"); //String AUDIO_AMR = "audio/amr";
        intent.setClassName("com.android.soundrecorder",
                "com.android.soundrecorder.SoundRecorder");
        startActivityForResult(intent, ACTION_AUDIO);
    }

    /**
     * 发送其他类型信息
     *
     * @param msg
     * @param type
     */
    public void sendMessage(String msg, int type) {
        Intent intent = new Intent(this, WebSocketService.class);
        //构建信息，信息条目通过Base64编码
        ImMessage message = new ImMessage(Config.userId, friend.getFriendId(), System.currentTimeMillis(), msg, type);
        intent.putExtra(WebSocketService.SEND_MESSAGE, message.toJson());
        //启动服务发送信息
        startService(intent);
        etConversation.setText("");
        messages.add(message);
        Config.addToSendMap(message);
        adaper.notifyItemInserted(messages.size() - 1);
        rcvChat.scrollToPosition(messages.size() - 1);

    }


    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_ON_MESSAGE);
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_CLOSE);
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_OPEN);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        Intent s = new Intent(this, WebSocketService.class);
        s.putExtra(WebSocketService.START_WEBSOCKET, true);
        s.putExtra(WebSocketService.USER_ID, Config.userId);

        startService(s);
        super.onResume();

    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    /**
     * 广播接收器
     */
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Gson gson = new Gson();
            switch (intent.getAction()) {
                case WebSocketService.ACTION_WEBSOCKET_CLOSE:
                    AlertUtil.toastMess(ChatActivity.this, "断开服务器");
                    break;
                case WebSocketService.ACTION_WEBSOCKET_ON_MESSAGE:
//                    AlertUtil.toastMess(ChatActivity.this,"有数据");
                    ImMessage msg = (ImMessage) intent.getSerializableExtra(WebSocketService.WEBSOCKET_MESSAGE);
                    //解析传来的数据
                    if (msg.getSenderId() == friend.getFriendId()) {
//                        AlertUtil.toastMess(ChatActivity.this, "收到：" + msg.getMsg());
                    }
//                    Config.addToReveivedMap(msg);
                    messages.add(msg);
                    adaper.notifyItemInserted(messages.size() - 1);
                    rcvChat.scrollToPosition(messages.size() - 1);


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


    /**
     * Adapter
     */
    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

//        InnerItemListener innerItemListener;

//        public void setInnerItemListener(InnerItemListener innerItemListener) {
//            this.innerItemListener = innerItemListener;
//        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;

            switch (viewType) {
                case 0:
                    View v = LayoutInflater.from(ChatActivity.this).inflate(R.layout.list_cell_chat_left, parent, false);
                    holder = new LeftViewHolder(v);
                    break;
                case 1:
                    v = LayoutInflater.from(ChatActivity.this).inflate(R.layout.list_cell_chat_right, parent, false);
                    holder = new RightViewHolder(v);
                    break;
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            //取得信息
            final ImMessage message = messages.get(position);
            //如果是文字信息
            if (message.getType() == 1) {
                if (holder instanceof LeftViewHolder) {
                    LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
//                leftViewHolder.civ_friend
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + friend.getFriendId() + ".png").placeholder(R.color.colorPrimary).error(R.color.colorPrimary).into(leftViewHolder.civ_friend);
                    leftViewHolder.tvMsg.setText(Util.getFromBase64(message.getMsg()));
                    leftViewHolder.tvMsg.setVisibility(View.VISIBLE);
                    leftViewHolder.btnMsgImage.setVisibility(View.GONE);
                    leftViewHolder.btnPlayVoice.setVisibility(View.GONE);


                } else if (holder instanceof RightViewHolder) {
                    RightViewHolder rightViewHolder = (RightViewHolder) holder;
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + Config.userId + ".png").placeholder(R.color.colorAccent).error(R.color.colorAccent).into(rightViewHolder.civ_friend);
                    rightViewHolder.tvMsg.setText(Util.getFromBase64(message.getMsg()));
                    rightViewHolder.btnMsgImage.setVisibility(View.GONE);
                    rightViewHolder.tvMsg.setVisibility(View.VISIBLE);
                    rightViewHolder.btnPlayVoice.setVisibility(View.GONE);



                }
            }
            //图片信息
            else if (message.getType() == 2) {
                if (holder instanceof LeftViewHolder) {
                    LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + friend.getFriendId() + ".png").placeholder(R.color.colorPrimary).error(R.color.colorPrimary).into(leftViewHolder.civ_friend);
                    leftViewHolder.btnMsgImage.setImageBitmap(Util.getBitmapFromBase64(message.getMsg()));
                    leftViewHolder.btnMsgImage.setVisibility(View.VISIBLE);
                    leftViewHolder.tvMsg.setVisibility(View.GONE);
                    leftViewHolder.btnPlayVoice.setVisibility(View.GONE);
                } else if (holder instanceof RightViewHolder) {
                    RightViewHolder rightViewHolder = (RightViewHolder) holder;
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + Config.userId + ".png").placeholder(R.color.colorAccent).error(R.color.colorAccent).into(rightViewHolder.civ_friend);
                    rightViewHolder.btnMsgImage.setImageBitmap(Util.getBitmapFromBase64(message.getMsg()));
                    rightViewHolder.tvMsg.setVisibility(View.GONE);
                    rightViewHolder.btnMsgImage.setVisibility(View.VISIBLE);
                    rightViewHolder.btnPlayVoice.setVisibility(View.GONE);

                }
            }
            //语音信息
            else if (message.getType() == 3) {
                if (holder instanceof LeftViewHolder) {
                    LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + friend.getFriendId() + ".png").placeholder(R.color.colorPrimary).error(R.color.colorPrimary).into(leftViewHolder.civ_friend);
                    leftViewHolder.btnMsgImage.setVisibility(View.GONE);
                    leftViewHolder.tvMsg.setVisibility(View.GONE);
                    leftViewHolder.btnPlayVoice.setVisibility(View.VISIBLE);
                    final String filePath= Util.storeFileFromBase64(ChatActivity.this,message.getMsg());
                    message.setFilePath(filePath);

                    leftViewHolder.btnPlayVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (message.getFilePath()!=null) {
                                try {
                                    binder.play(message.getFilePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (holder instanceof RightViewHolder) {
                    RightViewHolder rightViewHolder = (RightViewHolder) holder;
                    Picasso.with(ChatActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + Config.userId + ".png").placeholder(R.color.colorAccent).error(R.color.colorAccent).into(rightViewHolder.civ_friend);
                    rightViewHolder.tvMsg.setVisibility(View.GONE);
                    rightViewHolder.btnMsgImage.setVisibility(View.GONE);
                    rightViewHolder.btnPlayVoice.setVisibility(View.VISIBLE);
                    final String filePath= Util.storeFileFromBase64(ChatActivity.this,message.getMsg());
                    message.setFilePath(filePath);
                    rightViewHolder.btnPlayVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            playVoice();
                            if (message.getFilePath()!=null) {
                                try {
                                    binder.play(message.getFilePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });


                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            ImMessage message = messages.get(position);
            //如果是朋友发来的返回1 否则返回0
            return message.getReceiverId() == friend.getFriendId() ? 1 : 0;
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }


        /**
         * 左边布局
         */
        class LeftViewHolder extends RecyclerView.ViewHolder {

            //头像
            @FindView(R.id.civ_friend_lite)
            CircleImageView civ_friend;

            //文字
            @FindView(R.id.tv_msg_left)
            TextView tvMsg;

            //图片按钮
            @FindView(R.id.tv_msg_left_image)
            ImageButton btnMsgImage;

            //语音
            @FindView(R.id.btn_msg_left_voice)
            Button btnPlayVoice;


            public LeftViewHolder(View itemView) {
                super(itemView);
                SimpifyUtil.findAll(this, itemView);
            }
        }

        /**
         * 右边布局
         */
        class RightViewHolder extends RecyclerView.ViewHolder {

            @FindView(R.id.civ_mine_lite)
            CircleImageView civ_friend;

            @FindView(R.id.tv_msg_right)
            TextView tvMsg;

            @FindView(R.id.tv_msg_right_image)
            ImageButton btnMsgImage;

            @FindView(R.id.btn_msg_right_voice)
            Button btnPlayVoice;


            public RightViewHolder(View itemView) {
                super(itemView);
                SimpifyUtil.findAll(this, itemView);
            }

        }
    }

//    /**
//     * 播放语音
//     *
//     * @param path
//     */
//    public void playVoice(String path) {
//        try {
//
//            if (player == null) {
//                player = new MediaPlayer();
//                //设置音频原
//                player.setDataSource(path);
//
//                //准备
//                player.prepare();
//                //播放
//                player.start();
//            } else {
//                player.stop();
//                //设置音频原
//                player.setDataSource(path);
//                //准备
//                player.prepare();
//                //播放
//                player.start();
//
//            }
//        } catch (IOException e) {
//            Log.e("wow",e.toString()+e.getCause());
//        }
//    }
}
