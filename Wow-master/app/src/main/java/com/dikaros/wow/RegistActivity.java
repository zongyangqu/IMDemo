package com.dikaros.wow;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dikaros.wow.net.asynet.AsyNet;
import com.dikaros.wow.net.asynet.NormalAsyNet;
import com.dikaros.wow.util.AlertUtil;
import com.dikaros.wow.util.NetUtil;
import com.dikaros.wow.util.SimpifyUtil;
import com.dikaros.wow.util.annotation.FindView;
import com.dikaros.wow.util.annotation.OnClick;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 注册活动，必须使用手机号注册
 * 原理:1.先检查输入项，有误则提醒
 * 2.之后生成一个6位验证码通过短信方式发送到用户手机
 * 3.用户收到后输入验证码，如果正确，则发送用户手机号和设置的密码到服务器
 * 4.解析服务器传回的结果
 * <p/>
 * 返回结果201注册成功，202手机号被使用，500服务器异常
 */
public class RegistActivity extends AppCompatActivity implements AsyNet.OnNetStateChangedListener<String> {

    //电话输入框
    @FindView(R.id.regist_phone)
    EditText etPhone;

    //密码输入框
    @FindView(R.id.regist_password)
    EditText etPasswd;

    //验证码输入框
    @FindView(R.id.regist_confirm)
    EditText etConfirm;

    //获取验证码按钮
    @FindView(R.id.btn_get_confirm)
    Button btnGetConfirm;

    //注册按钮
    @FindView(R.id.btn_regist_action)
    Button btnRegist;

    //进度条
    @FindView(R.id.regist_progress)
    ProgressBar progressBar;

    //注册表单用于提示view的挂载
    @FindView(R.id.email_regist_form)
    LinearLayout llRegist;

    //计时器，再次发送验证码的计时
    Timer timer;

    //网络工具
    NormalAsyNet asyNet;

    //计时标示
    //正在计时
    public final int ON_COUNT = 1;
    //开始计时
    public final int START_COUNT = 2;
    //结束计时
    public final int FINISH_COUNT = 3;

    //随机数
    Random random = new Random();
    //随机生成的验证码
    String confirmCode;
    //倒计时
    int time = 60;

    //计时器线程
    TimerTask timerTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        /**
         * @see SimpifyUtil
         */
        SimpifyUtil.findAll(this);
        //初始化计时器
        timer = new Timer();
    }


    /**
     * 注册按钮的点击事件
     *
     * @param v
     */
    @OnClick(R.id.btn_regist_action)
    public void attempRegist(View v) {
        //判断密码输入是否合法
        if (etPhone.getText().toString().length() < 6) {
            //设置错误信息
            etPasswd.setError("密码需要大于6位");
            return;
        }
        //判断手机号输入是否合法
        if (etPhone.getText().toString().length() != 11) {
            etPhone.setError("手机号码不合法");
            return;
        }
        //判断验证码输入是否为空
        if (etConfirm.getText().toString().equals("")) {
            etConfirm.setError("不能为空");
            return;
        }

        //比较验证码
        if (confirmCode == null || !etConfirm.getText().toString().equals(confirmCode)) {
            etConfirm.setError("验证码输入错误");
            return;
        }

        //构建发送表单
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            object.put("phone", etPhone.getText().toString());
            object.put("password", etPasswd.getText().toString());
            array.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //初始化网络参数
        asyNet = new NormalAsyNet(Config.REGIST_ADDRESS, "jsonFile", array.toString(), AsyNet.NetMethod.POST);
        //设置回调
        asyNet.setOnNetStateChangedListener(this);
        //执行
        asyNet.execute();

    }


    //hander，接收计时器线程的事件并进行相应的操作
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_COUNT:
                    //时间-1
                    time--;
                    //设置时间显示
                    btnGetConfirm.setText("获取验证码(" + time + ")");
                    break;
                case START_COUNT:
                    //创建一个计时器线程
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(time == 0 ? FINISH_COUNT : ON_COUNT);
                        }
                    };
                    //每秒执行一次
                    timer.schedule(timerTask, 0, 1000);
                    //设置获取验证码不可用
                    btnGetConfirm.setEnabled(false);
                    //设置处于计时状态
                    Config.inWait = true;

                    break;
                //计时完成
                case FINISH_COUNT:
                    //取消计时
                    timerTask.cancel();
                    //时间复位
                    time = 60;
                    //设置可以点击获取验证码
                    btnGetConfirm.setEnabled(true);
                    //设置处于非计时状态
                    Config.inWait = false;
                    btnGetConfirm.setText("获取验证码");

                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }



    /**
     * 获取验证码按钮点击事件
     * @param v
     */
    @OnClick(R.id.btn_get_confirm)
    public void getSmsCode(View v) {
        //判断手机号的合法性
        if (etPhone.getText().toString().length() != 11) {
            etPhone.setError("手机号码不合法");
            return;
        }
        //如果处于非计时状态,生成一个验证码通过百度api发送到用户手机上
        if (!Config.inWait) {
            //100000-999999保证验证码位数
            int i = random.nextInt(899999) + 100000;
            confirmCode = String.valueOf(i);
            //开线程请求调用短信验证码api
            new Thread() {
                @Override
                public void run() {
                    String t = NetUtil.sendSmsCode(etPhone.getText().toString(), confirmCode);
                    if (t != null) {
                        handler.sendEmptyMessage(START_COUNT);
                    }
                }
            }.start();

        }
    }


    /**
     * 网络访问前
     */
    @Override
    public void beforeAccessNet() {
        //设置注册按钮不可用
        btnRegist.setEnabled(false);
        //隐藏表单
        llRegist.setVisibility(View.GONE);
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterAccessNet(String result) {
        //设置注册可用
        btnRegist.setEnabled(true);
        //显示表单
        llRegist.setVisibility(View.VISIBLE);
        //隐藏进度条
        progressBar.setVisibility(View.GONE);

        //如果结果不为空
        if (result != null && !result.equals("{}")) {
            try {
                //解析json信息
                JSONObject object = new JSONObject(result);
                int code = object.getInt("code");
                String message = object.getString("message");
                if (code == 201) {
                    //注册成功
                    AlertUtil.toastMess(this, message + "");
                    finish();
                } else if (code == 202) {
                    //设置错误信息
                    etPhone.setError("手机号已被使用");
                } else if (code == 500) {
                    //服务器异常
                    AlertUtil.showSnack(btnGetConfirm, message + "");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void whenException(Throwable t) {
        //设置注册可用
        btnRegist.setEnabled(true);
        //显示表单
        llRegist.setVisibility(View.VISIBLE);
        //隐藏进度条
        progressBar.setVisibility(View.GONE);

        AlertUtil.showSnack(btnRegist, "网络异常");

    }

    @Override
    public void onProgress(Integer progress) {

    }

}
