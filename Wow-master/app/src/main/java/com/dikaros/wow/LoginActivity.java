package com.dikaros.wow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dikaros.wow.net.asynet.AsyNet;
import com.dikaros.wow.net.asynet.NormalAsyNet;
import com.dikaros.wow.net.asynet.block.Block;
import com.dikaros.wow.service.WebSocketService;
import com.dikaros.wow.util.AlertUtil;
import com.dikaros.wow.util.SimpifyUtil;
import com.dikaros.wow.util.Util;
import com.dikaros.wow.util.annotation.FindView;
import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * 登录活动
 */
public class LoginActivity extends AppCompatActivity implements AsyNet.OnNetStateChangedListener<String> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "13838826842", "13783797081"
    };
    private NormalAsyNet asyNet = null;

    // UI references.
    @FindView(R.id.email)
    private AutoCompleteTextView mPhoneView;
    @FindView(R.id.password)
    private EditText mPasswordView;
    @FindView(R.id.login_progress)
    private View mProgressView;
    @FindView(R.id.login_form)
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SimpifyUtil.findAll(this);
//        populateAutoComplete();

        String userMsg = Util.getPreference(this,"user_msg");
        if (userMsg!=null){
            try {
                JSONObject root = new JSONObject(userMsg);
                String userName = root.getString("name");
                String userPassword = root.getString("password");
                String userPhone = root.getString("phone");
                String sessionId = root.getString("sessionId");
                Config.WEBSOCKET_SESSION = sessionId;
                Config.userId = root.getLong("id");
                mPhoneView.setText(userPhone);
                mPasswordView.setText(userPassword);
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_regist:
                Intent intent = new Intent(this,RegistActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
            }
        }
    }


    private void attemptLogin() {
        if (asyNet != null) {
            return;
        }

        mPhoneView.setError(null);
        mPasswordView.setError(null);

        String email = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mPhoneView.setError(getString(R.string.error_invalid_email));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            try {
                object.put("phone",mPhoneView.getText().toString());
                object.put("password",mPasswordView.getText().toString());
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            asyNet = new NormalAsyNet(Config.HTTP_LOGIN_ADDRESS,"jsonFile",array.toString(), AsyNet.NetMethod.POST);

            asyNet.setOnNetStateChangedListener(this);
            asyNet.execute();
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * 显示进度条隐藏登录表单
     * 这里需要判断Android的版本
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /**
         * 如果大于api13则显示动画
         * 否则仅仅是显示和隐藏
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            //动画显示时间
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            //设置登录表单的显示情况
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            //设置动画
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            //设置进度条显示情况
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //进度条动画
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * 网络访问前
     * 显示进度条
     */
    @Override
    public void beforeAccessNet() {
        showProgress(true);
    }

    /**
     * 网络访问后
     * @param result 访问结果
     */
    @Override
    public void afterAccessNet(String result) {
        /*
        {"id":2,"name":"123","phone":"123","password":"123,"sessionId":E104A6923565CD8F39A60DBEE3FF100B,code":1,"message":"登录成功"}
         */
        asyNet = null;
        setTitle("登录");
        if (result != null) {
            try {
                Log.e("result",result);
                JSONObject object = new JSONObject(result);
                int code = object.getInt("code");
                String message = object.getString("message");
                if (code == 101) {
                    //登录成功
                    AlertUtil.toastMess(this, message + "");
                    //保存用户信息
                    Util.setPreference(this,"user_msg",result);
                    String sessionId = object.getString("sessionId");
                    //设置sessionId
                    Config.WEBSOCKET_SESSION = sessionId;
                    //设置用户id
                    Config.userId = object.getLong("id");
                    //设置用户名
                    Config.userName = object.getString("name");
                    //设置用户信息
//                    Config.userMessage = object.getString("personalMessage");

                    //启动聊天服务
                    Intent service = new Intent(this,ShowActivity.class);
                    service.putExtra(WebSocketService.START_WEBSOCKET,true);
                    service.putExtra(WebSocketService.USER_ID,object.getLong("id"));
                    //保存用户二维码
                    JSONObject j = new JSONObject();
                    j.put("userId",Config.userId);
                    j.put("userName",Config.userName);
                    j.put("personalMessage",Config.userMessage);
                    j.put("avatarPath",Config.HTTP_AVATAR_ADDRESS+"/image/avator/" +Config.userId + ".png");
                    //调用二维码函数保存用户的部分信息
                    Config.USER_QR_CODE = Util.generateQrCode(Util.toBase64(j.toString()));
                    //启动聊天服务
                    startService(service);
                    //启动activity
                    Intent intent = new Intent(this,ShowActivity.class);
                    startActivity(intent);
                    finish();
                } else if (code == 102) {
                    mPasswordView.setError(message + "");
                    mPasswordView.requestFocus();
                } else if (code == 103) {
                    mPhoneView.setError(message + "");
                    mPhoneView.requestFocus();
                } else if (code == 500) {
                    AlertUtil.showSnack(mLoginFormView, message + "");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                e.printStackTrace();
            }

        }
        showProgress(false);

    }

    @Override
    public void whenException(Throwable t) {
        showProgress(false);
        asyNet = null;
        setTitle("登录");
        Log.e("tag",t.getMessage()+"--");
        AlertUtil.showSnack(mLoginFormView, "网络异常");
    }

    @Override
    public void onProgress(Integer progress) {

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }





}

