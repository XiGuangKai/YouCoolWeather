package com.europecoolweather.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.europecoolweather.R;
import com.europecoolweather.adapter.CityManagerAdapter;
import com.europecoolweather.database.CityManagerEntity;
import com.europecoolweather.database.SQLiteCityManager;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.UtilityClass;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;


public class CityManagerActivity extends AppCompatActivity {

    private static final String TAG = "CityManagerActivity";
    public static CityManagerEntity mCityManagerEntity = new CityManagerEntity();
    public static List<CityManagerEntity> mCityManagerEntityList = new ArrayList<>();

    private Button mBackButton;
    private Button mDeleteAllCityButton;
    private Context mContext;
    private GridView mGridView;
    private String cityName;
    private String imageUrl;
    private String weather;
    private String temp;
    public CityManagerEntity cityManagerEntity;
    public CityManagerAdapter cityManagerAdapter;
    public Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);
        mContext = CityManagerActivity.this;
        findView();
        initView();
        init();
        setInitImageLoader();
        DebugLog.d(TAG,"onCreate() complete");
    }

    private void findView() {
        mBackButton = (Button)findViewById(R.id.btn_back);
        mDeleteAllCityButton = (Button)findViewById(R.id.btn_delete_all_city);
        mGridView = (GridView) findViewById(R.id.gv_city_manager);
    }

    private void initView(){
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mDeleteAllCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext,R.style.ThemeAppCompatLightDialogAlertSelf);
                alertDialog.setIcon(R.drawable.cool_weather_icon);
                alertDialog.setTitle(R.string.dialog_delete_city_title);
                alertDialog.setMessage(R.string.dialog_delete_city_message);

                alertDialog.setPositiveButton(R.string.dialog_positive_button_exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //清除所有保存到数据库信息
                        UtilityClass.removeCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);
                        UtilityClass.removeCityName(mContext,UtilityClass.CHOOSE_CITY_TYPE);
                        UtilityClass.deleteDatabase(mContext,UtilityClass.YOU_COOL_WEATHER);
                        DebugLog.d(TAG,"delete all city success");
                        onResume();
                    }
                });

                alertDialog.setNegativeButton(R.string.dialog_negative_button_exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DebugLog.d(TAG,"cancel delete all city");
                    }
                });

                alertDialog.setCancelable(true);
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
    }

    private void setInitImageLoader(){
        Context context = getApplicationContext();
        initImageLoader(context);
    }

    private void setHomePageActivity(){
        for (int i = 0; i < mCityManagerEntityList.size(); i++) {
            if (mCityManagerEntityList.get(i).getCityName().equals(mCityManagerEntity.getCityName())) {
                mCityManagerEntityList.remove(mCityManagerEntity);
            }
        }
        // 为每次打开城市管理页都会加载一个item问题的解决方案
        mCityManagerEntityList.add(mCityManagerEntityList.size(), mCityManagerEntity);
        cityManagerAdapter.setCityManager(mCityManagerEntityList);
        for(int i = 0; i < mCityManagerEntityList.size(); i++ ){
            DebugLog.i(TAG, mCityManagerEntityList.get(i).getCityName());
        }
        DebugLog.i(TAG, "mCityManagerEntityList.size() = " + mCityManagerEntityList.size());
        cityManagerAdapter.notifyDataSetChanged();
    }

    private void init()
    {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                DebugLog.d(TAG,"position = " + position + ", mCityManagerEntityList.size() - 1 = " + (mCityManagerEntityList.size() - 1));
                if (UtilityClass.isNetWorkAvailable(mContext)) {
                    if (position == mCityManagerEntityList.size() - 1) {
                        intent = new Intent(mContext, ShowProvinceActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(mContext, WeatherShowActivity.class);
                        String cityName = mCityManagerEntityList.get(position).getCityName();
                        DebugLog.d(TAG, "city name = " + cityName);
                        intent.putExtra("select_city_name", cityName);

                        //将选择的城市名字保存
                        UtilityClass.setCityName(mContext,UtilityClass.CHOOSE_CITY_TYPE,cityName);

                        //将显示级别设置为选择类型
                        UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_CHOOSE_CITY;
                        startActivity(intent);
                    }
                } else {
                    DebugLog.e(TAG,"network is not useful");
                    UtilityClass.showToast(mContext,getString(R.string.toast_internet_no_useful_to_get_city));
                }
            }
        });
        cityManagerAdapter = new CityManagerAdapter(mContext,mCityManagerEntityList);
        mGridView.setAdapter(cityManagerAdapter);
    }

    public void queryDataBase() {
        SQLiteCityManager SQLite = new SQLiteCityManager(mContext,UtilityClass.YOU_COOL_DATABASE, null, 1);

        SQLiteDatabase db = SQLite.getWritableDatabase();

        Cursor cursor = db.query(UtilityClass.YOU_COOL_WEATHER, null, null, null, null, null, null);
        mCityManagerEntityList.clear();

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            cityName = cursor.getString(cursor.getColumnIndex("city_name"));
            imageUrl = cursor.getString(cursor.getColumnIndex("image_url"));
            weather = cursor.getString(cursor.getColumnIndex("weather"));
            temp = cursor.getString(cursor.getColumnIndex("temperature"));
            DebugLog.v(TAG,"_id = " + _id +", cityName = " + cityName);
            setCityManagerEntity();
        }
        cursor.close();
    }

    public void setCityManagerEntity() {
        cityManagerEntity = new CityManagerEntity();
        cityManagerEntity.setCityName(cityName);
        cityManagerEntity.setWeather(weather);
        cityManagerEntity.setTemperature(temp);
        cityManagerEntity.setWeatherImage(imageUrl);
        for (int i = 0; i < mCityManagerEntityList.size(); i++) {
            if (mCityManagerEntityList.get(i).getCityName().equals(cityManagerEntity.getCityName())) {
                mCityManagerEntityList.set(i, cityManagerEntity);
                return;
            }
        }
        mCityManagerEntityList.add(cityManagerEntity);
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onResume() {
        super.onResume();

        //获取数据库
        queryDataBase();

        //设置home 页面的显示
        setHomePageActivity();

        //初始化变量,修复数据需要两次点击城市管理才能更新信息的BUG
        init();
    }
}
