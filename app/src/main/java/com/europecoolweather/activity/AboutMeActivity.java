package com.europecoolweather.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.europecoolweather.R;
import com.europecoolweather.util.DebugLog;

public class AboutMeActivity extends AppCompatActivity {

    private static final String TAG = "AboutMeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        initView();
        DebugLog.d(TAG,"onCreate() complete");
    }

    private void initView()
    {
        DebugLog.d(TAG,"initView() complete");
    }
}
