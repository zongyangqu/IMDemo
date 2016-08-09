package com.dikaros.wow.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.dikaros.wow.R;

/**
 * Created by Dikaros on 2016/5/10.
 * 提示消息工具
 */
public class AlertUtil {

    /**
     * 显示toast
     *
     * @param context 上下文
     * @param mess 消息内容
     */
    public static void toastMess(Context context, String mess) {
        Toast.makeText(context, mess, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示提示性对话框
     * @param context 上下文
     * @param title 标题
     * @param message 消息内容
     */
    public static void simpleAlertDialog(Context context, String title,
                                         String message) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton(R.string.action_ok, null).show();
    }

    /**
     * 显示判断对话框 含有两个按钮
     * @param context 上下文
     * @param title 标题
     * @param message 消息内容
     * @param okListener 确认事件
     * @param cancleListener 取消事件
     * @return
     */
    public static AlertDialog judgeAlertDialog(Context context, String title,
                                               String message, DialogInterface.OnClickListener okListener,
                                               DialogInterface.OnClickListener cancleListener) {
        AlertDialog aDialog = new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton(R.string.action_ok, okListener)
                .setPositiveButton(R.string.action_cancel, cancleListener).show();
        return aDialog;
    }


    /**
     * 显示底部弹出信息
     * @param anchor 挂载view
     * @param message 信息
     */
    public static void showSnack(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
