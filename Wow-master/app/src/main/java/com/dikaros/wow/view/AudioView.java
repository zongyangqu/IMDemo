package com.dikaros.wow.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.dikaros.wow.R;
import com.dikaros.wow.util.annotation.FindView;

/**
 * 聊天底部录音选项
 * Created by Dikaros on 2016/6/16.
 */
public class AudioView {
    View view;

    @FindView(R.id.btn_chat_pv_audio)
    Button btnHover;

    public AudioView(Context context) {
        //加载
        view = LayoutInflater.from(context).inflate(R.layout.chat_panel_audio, null);
        initView();
    }


    private void initView() {

        btnHover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    public View getView() {
        return view;
    }
}
