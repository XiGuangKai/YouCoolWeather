package com.europecoolweather.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        Button mButton = (Button)findViewById(R.id.btn_search_author_blog);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://blog.csdn.net/xiaoxiangyuhai?viewmode=list");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        DebugLog.d(TAG,"initView() complete");
    }
}
