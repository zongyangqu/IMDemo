package com.dikaros.wow;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        if (savedInstanceState == null) {
            SettingFragment settingFragment = new SettingFragment();
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content,settingFragment)
                    .commit();
        }
    }

    public static class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // 加载xml资源文件
            addPreferencesFromResource(R.xml.settings);
        }
    }

}
