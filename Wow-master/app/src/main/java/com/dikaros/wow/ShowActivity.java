package com.dikaros.wow;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dikaros.wow.bean.Friend;
import com.dikaros.wow.bean.ImMessage;
import com.dikaros.wow.net.asynet.AsyNet;
import com.dikaros.wow.net.asynet.NormalAsyNet;
import com.dikaros.wow.net.asynet.block.Block;
import com.dikaros.wow.service.WebSocketService;
import com.dikaros.wow.util.AlertUtil;
import com.dikaros.wow.util.SimpifyUtil;
import com.dikaros.wow.util.Util;
import com.dikaros.wow.util.annotation.FindView;
import com.dikaros.wow.util.annotation.OnClick;
import com.dikaros.wow.view.RecyclerViewDivider;
import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.picasso.Picasso;
import com.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * 朋友列表活动，主要展示好友列表
 * 左边抽屉布局进行基本操作
 *
 * @see NormalAsyNet 自定义的网络工具，基于okHttp
 * @see SimpifyUtil 快速view绑定工具
 */
public class ShowActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AsyNet.OnNetStateChangedListener<String> {


    //朋友列表
    @FindView(R.id.rcv_friend)
    RecyclerView rcvFriend;

    @FindView(R.id.srl_friend)
    SwipeRefreshLayout srlMain;

    //朋友集合
    public static List<Friend> friends;

    //蒙版，点击fab后显示
    @FindView(R.id.rl_blank)
    RelativeLayout blankBoard;

    //朋友适配器
    FriendAdapter friendAdapter;

    //自定义网络工具
    NormalAsyNet net;

    /**
     * 自定义广播接收器，用于接收服务发来的websocket广播从而进行相应处理
     */
    MyReceiver receiver;


    /**
     * 用户id
     */
    TextView tvUserId;

    /**
     * 用户名
     */
    TextView tvUserName;

    /**
     * 用户信息
     */
    TextView tvUserMessage;

    //标志值记录菜单打开与否
    boolean blankOpen = false;


    @FindView(R.id.iv_qr_code)
    ImageView ivQrCode;

    @FindView(R.id.tv_qr_title)
    TextView tvQrTitle;

    public static final String WOW_SCHEME = "wow://com.dikaros.wow/open?jsonFile=";

    public static final String HTTP_TITLE = "http://123.206.75.202:8080/WowServer/jump.html?jsonFile=";

    //从网页链接创建activity标示
    boolean isCreateFromWowScheme = false;


    public static final int ACTION_SCAN_QR_CODE = 1;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String temp;
        if ((temp = intent.getDataString()) != null) {
            //添加好友
            addFriendWithStr(temp.substring(WOW_SCHEME.length()));
        }
    }

    /**
     * OnCreate方法，在这里初始化信息
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        /**
         * @see SimpifyUtil具体信息详见快速查找工具
         */

        if (Util.getPreference(this, "user_msg") == null) {
            AlertUtil.toastMess(this, "请先登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            isCreateFromWowScheme = true;
        }

        String temp;
        if ((temp = getIntent().getDataString()) != null) {
            Log.e("wow", temp);
        }
        SimpifyUtil.findAll(this);
        //设置toolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化朋友集合
        friends = new ArrayList<>();
        //初始化fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);

        /**
         * 为fab配置监听器
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果菜单打开则关闭菜单，如果关闭则打开
                if (blankOpen) {
                    closeFabMenu(view);
                } else {
                    openFabMenu(view);
                }


            }
        });

        //初始化自定义广播接收器
        receiver = new MyReceiver();

        //初始化左边抽屉布局
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //初始化抽屉把手
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //设置抽屉把手
        drawer.setDrawerListener(toggle);
        //设置状态
        toggle.syncState();

        //初始化导航菜单
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //设置导航的监听器
        navigationView.setNavigationItemSelectedListener(this);

        //设置主list的布局管理器
        rcvFriend.setLayoutManager(new LinearLayoutManager(this));
        //初始化主list适配器
        friendAdapter = new FriendAdapter();
        //设置设配器
        rcvFriend.setAdapter(friendAdapter);
        //设置回调事件，当用户按下时调用onClick,当用户长按是调用onLongClick
        friendAdapter.setInnerItemListener(new InnerItemListener() {
            @Override
            public void onClick(View v, int index) {
                //如果fab菜单是关闭状态
                if (!blankOpen) {
                    //创建指向聊天活动的Activity
                    Intent intent = new Intent(ShowActivity.this, ChatActivity.class);
                    //增加朋友信息
                    intent.putExtra("friend", friends.get(index));
                    //增加新信息字段
                    intent.putExtra("start_position", friends.get(index).getNewMessage());
                    //设置点击后红点计数为0
                    friends.get(index).setNewMessage(0);
                    //通知适配器数据改变，更新ui
                    friendAdapter.notifyDataSetChanged();
                    //启动活动
                    startActivity(intent);

                }
            }

            @Override
            public void onLongClick(View v, int index) {
                //暂时不需要长按方法
            }
        });

        //设置下拉时的进度条切换为红->黄->青->绿
        srlMain.setColorSchemeColors(Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN);

        //设置刷新事件
        srlMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //访问网络获取数据
                accessNet();
            }
        });

        //访问网络获取数据
        accessNet();
        //设置标题
        setTitle(getString(R.string.friend_list));
        //设置主list分割线
        rcvFriend.addItemDecoration(new RecyclerViewDivider(this,
                RecyclerViewDivider.VERTICAL_LIST));

    }


    /**
     * 添加好友
     *
     * @param base64File
     */
    public void addFriendWithStr(String base64File) {
        try {
            //根元素
            final JSONObject root = new JSONObject(Util.getFromBase64(base64File));
            //判断框
            final long friendId = root.getLong("userId");
            //遍历查看是否好友关系已经存在
            boolean hasFriend = false;
            for (Friend friend : friends) {
                if (friend.getFriendId() == friendId) {
                    hasFriend = true;
                    break;
                }
            }
            if (!hasFriend) {
                AlertUtil.judgeAlertDialog(this, getString(R.string.add_friend) + " uid:" + friendId, getString(R.string.add_or_not) + root.getString("userName") + getString(R.string.as_your_friend), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject param = new JSONObject();
                        try {
                            param.put("hostId", Config.userId);
                            param.put("friendId", friendId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        NormalAsyNet normalAsyNet = new NormalAsyNet(Config.HTTP_ADD_FRIEND, "jsonFile", param.toString(), AsyNet.NetMethod.POST);
                        normalAsyNet.setOnNetStateChangedListener(new AsyNet.OnNetStateChangedListener<String>() {
                            @Override
                            public void beforeAccessNet() {

                            }

                            @Override
                            public void afterAccessNet(String result) {
                                Log.e("wow", "add_friend_result" + result);
                                if (result != null && !result.equals("{}")) {
                                    //解析结果
                                    JSONObject root = null;
                                    try {
                                        root = new JSONObject(result);
                                        if (root.getInt("code") == 400) {
                                            AlertUtil.toastMess(ShowActivity.this, getString(R.string.add_friend_success));
                                            accessNet();
                                        } else {
                                            AlertUtil.toastMess(ShowActivity.this, getString(R.string.add_friend_faild));

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }

                            @Override
                            public void whenException(Throwable t) {
                                Log.e("wow", t + "");
                            }

                            @Override
                            public void onProgress(Integer progress) {

                            }
                        });
                        normalAsyNet.execute();
                    }
                }, null);
            } else {
                //普通提醒
                AlertUtil.simpleAlertDialog(this, getString(R.string.already_friend), null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打开菜单
     * fab会显示一个带有惯性的旋转动画
     * 蒙版动态显示（由透明到半透明）
     * 增加好友按钮显示
     */
    public void openFabMenu(View view) {
        //设置对象动画,表现形式旋转，从0度到-155度再到0135度
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0, -155, -135);
        //设置持续时间为0.5s
        animator.setDuration(500);
        //启动动画
        animator.start();
        //设置蒙版显示
        blankBoard.setVisibility(View.VISIBLE);
        //设置透明度动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 0.7f);
        //设置持续时间0.5s
        alphaAnimation.setDuration(500);
        //设置在动画完成后保持状态
        alphaAnimation.setFillAfter(true);
        //启动动画
        blankBoard.startAnimation(alphaAnimation);
        rcvFriend.setVisibility(View.VISIBLE);

        //设置fab标志为开启
        blankOpen = true;
        ivQrCode.setVisibility(View.GONE);
        tvQrTitle.setVisibility(View.GONE);
    }

    /**
     * 关闭菜单
     * fab会显示一个带有惯性的旋转动画
     * 蒙版动态显示（半透明到小时）
     * 增加好友按钮隐藏
     */
    public void closeFabMenu(View view) {
        //设置对象动画,表现形式旋转，从-135度到20度再到0度
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", -135, 20, 0);
        //设置持续时间为0.5s
        animator.setDuration(500);
        //启动动画
        animator.start();
        //设置透明度动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.7f, 0);
        //设置持续时间0.5s
        alphaAnimation.setDuration(500);
        //设置在动画完成后保持状态
        alphaAnimation.setFillAfter(true);
        //启动动画
        blankBoard.startAnimation(alphaAnimation);
        //蒙版不显示
        blankBoard.setVisibility(View.GONE);
        //设置标志值
        rcvFriend.setVisibility(View.VISIBLE);
        blankOpen = false;
        ivQrCode.setVisibility(View.GONE);
        tvQrTitle.setVisibility(View.GONE);
    }


    /**
     * 活动到前台的回调
     * 在这里为这个活动注册广播接收器
     */
    @Override
    protected void onResume() {
        //设置过滤器
        IntentFilter filter = new IntentFilter();
        //为过滤器设置过滤websocket有信息事件
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_ON_MESSAGE);
        //为过滤器设置过滤websocket关闭事件
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_CLOSE);
        //为过滤器设置过滤websocket开启事件
        filter.addAction(WebSocketService.ACTION_WEBSOCKET_OPEN);
        //为过滤器设置过滤网络变化事件
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //注册广播接收器并设置过滤器
        registerReceiver(receiver, filter);
        //设置并启动后台服务，监听服务器传来的信息
        Intent s = new Intent(this, WebSocketService.class);
        s.putExtra(WebSocketService.START_WEBSOCKET, true);
        s.putExtra(WebSocketService.USER_ID, Config.userId);
        startService(s);
        super.onResume();

    }


    /**
     * 活动暂停的回调
     * 当程序从前台移动到后台时需要注销到广播接收器
     */
    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    /**
     * 异步访问网络
     */
    public void accessNet() {
        //如果网络工具不为空则强制停止并复位
        if (net != null) {
            net.cancel(true);
            net = null;
        }
        //初始化网络
        net = new NormalAsyNet(Config.HTTP_GET_FRIEND, "jsonFile", "{\"userId\":" + Config.userId + "}", AsyNet.NetMethod.POST);
        //设置回调
        net.setOnNetStateChangedListener(this);

        //执行
        net.execute();
    }

    /**
     * 返回键按下的回调
     */
    @Override
    public void onBackPressed() {
        //如果抽屉是打开的先关闭
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (blankOpen) {
            closeFabMenu(findViewById(R.id.fab_add));
        } else {
            super.onBackPressed();

        }
    }


    /**
     * 抽屉菜单的回调
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //获得ID
        int id = item.getItemId();

        //设置
        if (id == R.id.nav_setting) {
            Intent intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
        }
        //分享
        else if (id == R.id.nav_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "推荐一个聊天软件\nhttp://123.206.75.202:8080/WowServer/wow.apk");
            shareIntent.setType("text/plain");

            //设置分享列表的标题，并且每次都显示分享列表
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
        }
        //发送
        else if (id == R.id.nav_send) {

        }
        //退出登录
        else if (id == R.id.nav_log_out) {
            //清空用户信息
            Util.setPreference(this, "user_msg", null);
            //跳转到登录界面
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            //结束词活动
            finish();
        }
        //点击后关闭抽屉
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 在网络访问前
     */
    @Override
    public void beforeAccessNet() {
        //设置不可刷新
        srlMain.setEnabled(false);

    }

    /**
     * 网络访问后
     *
     * @param result 访问结果
     */
    @Override
    public void afterAccessNet(String result) {
        //设置停止刷新
        srlMain.setRefreshing(false);
        //设置可以刷新
        srlMain.setEnabled(true);
        Log.e("wow", result + "--");
        //如果返回结果不为空
        if (result != null || result.equals("[]")) {
            try {
                //解析json信息
                JSONArray array = new JSONArray(result);
                Gson gson = new Gson();
                //清空朋友列表，稍后重新加载
                friends.clear();
                //循环解析json并将朋友信息增加到朋友列表
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    Friend f = gson.fromJson(o.toString(), Friend.class);
                    Log.e("wow_friend", f.toString());
                    friends.add(f);
                    Config.friendList.put(f.getFriendId(),f);
                }
                Log.e("friend_count", friends.size() + "");
                //更新视图
                rcvFriend.getAdapter().notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //如果是从scheme启动的activity
        if (isCreateFromWowScheme) {
            String temp;
            if ((temp = getIntent().getDataString()) != null) {
                //添加好友
                addFriendWithStr(temp.substring(WOW_SCHEME.length()));
                isCreateFromWowScheme = false;
            }
        }
    }

    /**
     * 当网络访问出错时
     *
     * @param t 错误信息
     */
    @Override
    public void whenException(Throwable t) {
        srlMain.setRefreshing(false);
        srlMain.setEnabled(true);
        Log.e("wow", t.getMessage() + "--");


    }

    @Override
    public void onProgress(Integer progress) {

    }


    /**
     * 由于官方没有设置RecycleView item点击事件故设置
     * 朋友view的点击回调接口
     */
    interface InnerItemListener {
        public void onClick(View v, int index);

        public void onLongClick(View v, int index);
    }

    /**
     * 朋友Adapter
     */
    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

        InnerItemListener innerItemListener;

        public void setInnerItemListener(InnerItemListener innerItemListener) {
            this.innerItemListener = innerItemListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //加载视图
            View v = LayoutInflater.from(ShowActivity.this).inflate(R.layout.list_cell_friend, parent, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        //在这设置每一项的数据
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            //设置数据
            Friend friend = friends.get(position);
            //加载头像
            Picasso.with(ShowActivity.this).load(Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + friend.getFriendId() + ".png").error(R.drawable.icon).into(holder.civ_friend);
            //设置名字
            holder.tvFriendName.setText((friend.getFriendMark() == null || friend.getFriendMark().equals("")) ? friend.getFriendName() : friend.getFriendMark());
            //设置个性签名
            holder.tvLastMsg.setText(friend.getFriendMessage() == null ? getString(R.string.not_set_sign) : friend.getFriendMessage());
            //设置监听器
            if (innerItemListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    //单击事件
                    @Override
                    public void onClick(View v) {
                        innerItemListener.onClick(v, position);
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    //长按事件
                    @Override
                    public boolean onLongClick(View v) {
                        innerItemListener.onLongClick(v, position);
                        return true;
                    }
                });
            }

            //设置红点，如果新消息数大于0则显示
            if (friend.getNewMessage() > 0) {
                holder.tvMessageCount.setText(friend.getNewMessage() + "");
                //显示
                holder.tvMessageCount.setAlpha(1f);
            } else {
                //隐藏
                holder.tvMessageCount.setAlpha(0f);
            }
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }


        /**
         * 每一项的view类
         */
        class ViewHolder extends RecyclerView.ViewHolder {

            @FindView(R.id.civ_friend)
            CircleImageView civ_friend;

            @FindView(R.id.tv_friend_name)
            TextView tvFriendName;

            @FindView(R.id.tv_friend_last_message)
            TextView tvLastMsg;

            @FindView(R.id.tv_message_count)
            TextView tvMessageCount;


            public ViewHolder(View itemView) {
                super(itemView);
                SimpifyUtil.findAll(this, itemView);
            }
        }
    }

    /**
     * 邀请好友
     */
    @OnClick(R.id.btn_invite_friend)
    public void btnInviteFriendClicked(View v) {
        //设置二维码显示
        ivQrCode.setVisibility(View.VISIBLE);
        //设置标题显示
        tvQrTitle.setVisibility(View.VISIBLE);
        Log.e("wow", Config.userId + "--" + Config.userName + "--" + Config.userMessage);
        rcvFriend.setVisibility(View.GONE);
        JSONObject j = new JSONObject();

        try {
            j.put("userId", Config.userId);
            j.put("userName", Config.userName);
            j.put("personalMessage", Config.userMessage);
            j.put("avatarPath", Config.HTTP_AVATAR_ADDRESS + "/image/avator/" + Config.userId + ".png");
            ivQrCode.setImageBitmap(Util.generateQrCode("http://123.206.75.202:8080/WowServer/jump.html?jsonFile=" + Util.toBase64(j.toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (WriterException e) {
            e.printStackTrace();
        }


    }

    /**
     * 增加好友
     */
    @OnClick(R.id.btn_add_friend)
    public void btnAddFriendClicked(View v) {
        Intent i = new Intent(this, CaptureActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, ACTION_SCAN_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTION_SCAN_QR_CODE) {
//                data.get
//                Log.e("wow","scan_"+data.getExtras().getString("result"));
                String result = data.getExtras().getString("result");
                if (result != null && result.startsWith("http")) {
                    addFriendWithStr(result.substring(HTTP_TITLE.length()));
                }
            }

        }
    }

    /**
     * 广播接收器
     */
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                //websocket关闭
                case WebSocketService.ACTION_WEBSOCKET_CLOSE:
                    AlertUtil.toastMess(ShowActivity.this, "与服务器断开连接");

                    break;
                //websocket传来了信息
                case WebSocketService.ACTION_WEBSOCKET_ON_MESSAGE:
                    //获得传来的信息
                    ImMessage message = (ImMessage) intent.getSerializableExtra(WebSocketService.WEBSOCKET_MESSAGE);
                    //遍历，设置相关的红点提示
                    for (int i = 0; i < friends.size(); i++) {
                        Friend f = friends.get(i);
                        if (f.getFriendId() == message.getSenderId()) {
                            f.addMessage();
                            friendAdapter.notifyItemChanged(i);
                        }
                    }

                    break;
                //websocket打开
                case WebSocketService.ACTION_WEBSOCKET_OPEN:
                    //连接成功时
                    AlertUtil.toastMess(ShowActivity.this, "连接上服务器");
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    break;

                default:
                    break;
            }


        }
    }


}
