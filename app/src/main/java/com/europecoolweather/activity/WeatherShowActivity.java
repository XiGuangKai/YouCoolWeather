package com.europecoolweather.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.europecoolweather.R;
import com.europecoolweather.gson.Forecast;
import com.europecoolweather.gson.Weather;
import com.europecoolweather.service.AutoUpdateWeatherService;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherShowActivity extends AppCompatActivity {

    private final static String TAG = "WeatherShowActivity";

    private static boolean isBackgroundUpdate = false;

    private Button navButton;
    private Button titleLocationButton;

    private CountDownTimer mCountDownTimer = null;
    private Context mContext;

    private ImageView bingPicImg;
    private ImageView iv_weather_picture;
    private Intent autoUpdateWeatherServiceIntent;

    private LinearLayout forecastLayout;
    //天气预警信息
    private LinearLayout mAlarmLayout;
    private LinearLayout mAqiLayout;
    private LocationClient mLocationClient;

    private ScrollView weatherLayout;

    private TextView titleCity;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView quality;
    private TextView comfortText;
    private TextView comUvText;
    private TextView carWashText;
    private TextView sportText;

    //天气预警信息
    private TextView mAlarmLevelText;
    private TextView mAlarmStatusText;
    private TextView mAlarmTitleText;
    private TextView mAlarmInfoText;

    private UpdateWeatherInfoBroadcastReceiver updateWeatherInfoBroadcastReceiver;

    public DrawerLayout drawerLayout;
    public View leftDrawer;

    public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather_show);
        mContext = WeatherShowActivity.this;
        findView();
        initView();

        DebugLog.d(TAG,"onCreate() finished");
    }

    /**
     * 初始化各控件
     */
    private void findView(){
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        iv_weather_picture = (ImageView) findViewById(R.id.iv_weather_picture);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mAqiLayout = (LinearLayout) findViewById(R.id.layout_aqi);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        quality = (TextView) findViewById(R.id.tv_quality);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        comUvText = (TextView) findViewById(R.id.uv_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = findViewById(R.id.choose_area_fragment);
        navButton = (Button) findViewById(R.id.nav_button);
        titleLocationButton = (Button) findViewById(R.id.location_button);

        //天气预警
        mAlarmLevelText = (TextView)findViewById(R.id.alarm_level);
        mAlarmStatusText = (TextView)findViewById(R.id.alarm_status);
        mAlarmTitleText = (TextView)findViewById(R.id.alarm_title);
        mAlarmInfoText = (TextView)findViewById(R.id.alarm_info);
        mAlarmLayout = (LinearLayout) findViewById(R.id.alarms_layout);

        DebugLog.d(TAG,"findView() finished");
    }

    private void initView(){
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (UtilityClass.isNetWorkAvailable(mContext) && UtilityClass.getRequestRunningPermissionStatus(mContext)) {
                    UtilityClass.isShowProgressDialog = false;
                    initWeatherData();
                }else{
                    DebugLog.e(TAG,"network is not available");
                    UtilityClass.showToast(mContext, getString(R.string.toast_detect_internet_no_useful));


                    //关闭更新动画
                    if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //定位天气监听
        titleLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherLayout.setVisibility(View.INVISIBLE);
                DebugLog.d(TAG,"get weather by location");
                if (UtilityClass.isNetWorkAvailable(mContext)) {
                    //启动定位服务
                    startRequestLocation();

                    //设置选择的方式为定位方式
                    UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_LOCATION_CITY;
                }else{
                    UtilityClass.showToast(mContext,getString(R.string.toast_detect_internet_no_useful));
                }
            }
        });
    }

    private void initWeatherData(){

        //一定要在此处调用，不然后续swipeRefresh将会有出现空指针异常的风险
        updateShowWeatherInfo();
        DebugLog.d(TAG,"update show weather info");

        if (UtilityClass.isNetWorkAvailable(mContext)) {
            DebugLog.d(TAG,"network available start service");
            //启动更新Service
            startAutoUpdateWeatherService();

            //注册广播
            registerUpdateWeatherInfoReceiver();
        } else {
            DebugLog.e(TAG,"there is not available network and not start service");
        }
    }

    /**
     * 显示初始天气信息更新天气信息
     */
    private void updateShowWeatherInfo(){

        //获取存储的默认的城市名字和选择的城市名字
        String mDefaultCityName = UtilityClass.getCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);
        String mChooseCityName = UtilityClass.getCityName(mContext,UtilityClass.CHOOSE_CITY_TYPE);

        //当前代码块中的代码是显示哪一个城市的天气的选择机制.1、2、3、4共4条互相的配合使用，此选择机制四条缺少就会存在问题
        {
            //1. 如果要显示的城市是默认城市，但是却没有默认城市的信息，所以将要显示城市信息改成之前选择的城市信息
            if (mDefaultCityName == null && UtilityClass.SHOW_CITY_WEATHER_INFO.equals(UtilityClass.SHOW_DEFAULT_CITY)) {
                DebugLog.d(TAG, "set SHOW_CITY_WEATHER_INFO as SHOW_CHOOSE_CITY");
                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_CHOOSE_CITY;
            }

            //2. 如果要显示的城市是选择的城市，但是却没有选择城市的信息，所以将要显示的城市信息改成默认的城市信息
            if (mChooseCityName == null && UtilityClass.SHOW_CITY_WEATHER_INFO.equals(UtilityClass.SHOW_CHOOSE_CITY)) {
                DebugLog.d(TAG, "set SHOW_CITY_WEATHER_INFO as SHOW_DEFAULT_CITY");
                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_DEFAULT_CITY;
            }

            //3. 如果默认城市和选择城市信息全部没有，但是数据库中却存有其他城市信息，将会显示数据库中第一个城市的天气信息
            if (mDefaultCityName == null && mChooseCityName == null && UtilityClass.queryDatabaseFirstId(mContext) != null) {
                DebugLog.d(TAG, "set SHOW_CITY_WEATHER_INFO as SHOW_DATABASE_CITY");
                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_DATABASE_CITY;
            }

            //4. 当默认城市、选择的城市和其他城市信息存储的数据都是null的时候，使用定位服务进行城市定位并且获取天气信息
            if (mDefaultCityName == null && mChooseCityName == null && UtilityClass.queryDatabaseFirstId(mContext) == null) {
                DebugLog.d(TAG, "set SHOW_CITY_WEATHER_INFO as SHOW_LOCATION_CITY");
                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_LOCATION_CITY;
            }
        }

        //根据不同方式获取并且显示天气信息
        DebugLog.d(TAG,"SHOW_CITY_WEATHER_INFO = " + UtilityClass.SHOW_CITY_WEATHER_INFO);
        switch (UtilityClass.SHOW_CITY_WEATHER_INFO){
            case UtilityClass.SHOW_DEFAULT_CITY:
            case UtilityClass.SHOW_CHOOSE_CITY:
            case UtilityClass.SHOW_DATABASE_CITY:

                String mWeatherDescription = null;
                if (UtilityClass.SHOW_DEFAULT_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)){
                    DebugLog.d(TAG, "default city weather description");
                    mWeatherDescription = UtilityClass.queryDatabaseWhetherCityExists(mContext, mDefaultCityName);
                }

                if (UtilityClass.SHOW_CHOOSE_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)){
                    DebugLog.d(TAG,"choose city weather description");
                    mWeatherDescription = UtilityClass.queryDatabaseWhetherCityExists(mContext,mChooseCityName);
                }

                if (UtilityClass.SHOW_DATABASE_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)){
                    DebugLog.d(TAG,"database first city weather description");
                    mWeatherDescription = UtilityClass.queryDatabaseFirstId(mContext);
                }

                if (mWeatherDescription != null) {
                    // 有缓存时直接解析天气数据
                    Weather mWeatherInfo = UtilityClass.handleWeatherResponse(mWeatherDescription);
                    if (mWeatherInfo != null){
                        String mDefaultWeatherCityName = mWeatherInfo.basic.cityName;

                        DebugLog.d(TAG,"default weather city name = " + mDefaultWeatherCityName);
                        //先显示已有的Weather信息
                        showWeatherInfo(mWeatherInfo);

                        checkNetworkAndGetWeatherInfo(mDefaultWeatherCityName);
                    }
                }else{
                    DebugLog.e(TAG,"city weather description not exist");
                }
                break;
            case UtilityClass.SHOW_LOCATION_CITY:
                weatherLayout.setVisibility(View.INVISIBLE);
                if (UtilityClass.getRequestRunningPermissionStatus(mContext)){
                    DebugLog.d(TAG,"get weather info by location");
                    if (UtilityClass.isNetWorkAvailable(mContext)) {
                        startRequestLocation();
                    }else{
                        if (!isBackgroundUpdate) {
                            UtilityClass.showToast(mContext,getString(R.string.toast_detect_internet_no_useful));
                        }
                    }
                }else {
                    DebugLog.d(TAG,"get running permission before get weather info by location");
                    requestSelfPermission();
                }
                break;
            default:
                DebugLog.e(TAG,"default error");
                break;
        }

        //从stored_data读取背景图片的信息
        SharedPreferences mStoredData = mContext.getSharedPreferences("stored_data", Context.MODE_PRIVATE);
        String bingPic = mStoredData.getString("backgroundPicture", null);

        //获取背景图片
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            if (UtilityClass.isNetWorkAvailable(mContext)) {
                DebugLog.d(TAG,"update background picture by internet");
                loadBingPic();
            }
        }
    }

    /*
    * 检查当前网络状态更新获取城市信息
    * @param cityName 要获取天气的城市名字
    */
    private void checkNetworkAndGetWeatherInfo(String cityName){
        if (UtilityClass.isNetWorkAvailable(mContext)) {
            DebugLog.d(TAG,"network is available to get the weather info cityName = " + cityName);
            getWeatherInfo(cityName);
        } else {
            DebugLog.e(TAG,"network is not available");
            if (!isBackgroundUpdate) {
                UtilityClass.showToast(mContext, getString(R.string.toast_detect_internet_no_useful));
            }

            //关闭更新动画
            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
        }
    }

    /**
     * 根据城市名称请求城市天气信息
     * @param cityName 需要获取天气的城市名字
     */
    public void getWeatherInfo(final String cityName) {

        DebugLog.d(TAG,"get " + cityName + " weather info");

        //显示progressDialog
        UtilityClass.showProgressDialog(mContext,getString(R.string.progress_dialog_updating_weather_info));

        //拼凑访问和风天气的API地址
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city="+cityName+"&key=6616624b9a104d3aa3afe5dfef16783c";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = UtilityClass.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //显示天气信息
                            showWeatherInfo(weather);

                            //将数据更新到数据库
                            UtilityClass.insertOrUpdateDatabase(mContext,weather,UtilityClass.setWeatherPictureToString(mContext,weather.now.more.info),responseText);

                            //弹窗提示信息已经更新
                            UtilityClass.showToast(mContext,getString(R.string.toast_weather_info_finished));
                        } else {
                            DebugLog.e(TAG,"get weather info failed");
                            UtilityClass.showToast(mContext,getString(R.string.toast_get_weather_info_failed));
                        }

                        //关闭Progress Dialog
                        UtilityClass.closeProgressDialog();

                        //关闭更新动画
                        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DebugLog.e(TAG,"OkHttpRequest failed to get the weather info");
                        UtilityClass.showToast(mContext,getString(R.string.toast_get_weather_info_failed));

                        //关闭Progress Dialog
                        UtilityClass.closeProgressDialog();

                        //关闭更新动画
                        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                });
            }
        });
        loadBingPic();

        DebugLog.d(TAG,"getWeatherInfo() finished");
    }

    @Override
    protected void onResume(){
        super.onResume();

        //显示数据
        initWeatherData();

        //在没有网络的半个小时时间内自动检测网络并且检测到网络存在时自动更新数据
        if (!UtilityClass.isNetWorkAvailable(mContext)){
            if(mCountDownTimer == null){
                mCountDownTimer = new CountDownTimer(UtilityClass.CYCLE_INTERVAL_TIME, 5*1000){
                    @Override
                    public void onTick(long millisUntilFinished) {

                        if (UtilityClass.getRequestRunningPermissionStatus(mContext)) {
                            //用于统计网络连接正常之后，更新的次数
                            int isCountNetworkOK = 0;

                            //循环检测的时候，关闭Toast弹窗
                            isBackgroundUpdate = true;

                            //更新天气数据
                            initWeatherData();

                            //如果循环过程中网络好了，那么最多再循环一次，就取消循环
                            if (UtilityClass.isNetWorkAvailable(mContext)){
                                isCountNetworkOK++;
                                if (isCountNetworkOK >= 1){
                                    DebugLog.d(TAG,"cancel timer");
                                    mCountDownTimer.cancel();
                                    mCountDownTimer = null;
                                    isBackgroundUpdate = false;
                                }
                            }
                        }
                    }
                    @Override
                    public void onFinish() {
                        DebugLog.d(TAG,"Timer is finished");
                        mCountDownTimer.cancel();
                        mCountDownTimer = null;
                        isBackgroundUpdate = false;

                        UtilityClass.showToast(mContext,getString(R.string.toast_no_useful_thirty_minutes));
                        finish();
                    }
                }.start();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        String selectCityName = intent.getStringExtra("select_city_name");
        if (selectCityName != null && !selectCityName.isEmpty()){
            //非后台更新，显示Toast
            isBackgroundUpdate = false;

            //获取天气数据
            DebugLog.d(TAG,"select city name is " + selectCityName);
            checkNetworkAndGetWeatherInfo(selectCityName);
        }
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();

                //将背景数据保存到stored_data文件
                SharedPreferences mStoredData = mContext.getSharedPreferences("stored_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorStoredData = mStoredData.edit();
                editorStoredData.putString("backgroundPicture", bingPic);
                editorStoredData.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(mContext).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });

        DebugLog.d(TAG,"loadBingPic() finished");
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather Weather类的对象
     */
    private void showWeatherInfo(Weather weather) {

        if (weather != null) {
            String cityName = weather.basic.cityName;
            String degree = weather.now.temperature + "℃";
            String weatherInfo = weather.now.more.info;

            titleCity.setText(cityName);
            iv_weather_picture.setImageBitmap(UtilityClass.stringToBitmap(UtilityClass.setWeatherPictureToString(mContext,weather.now.more.info)));
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();

            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture);
                TextView temperature = (TextView) view.findViewById(R.id.temperature);

                //日期填充
                dateText.setText(forecast.date);

                //天气状况填充
                infoText.setText(forecast.more.info);

                //根据天气状况填充图片
                imageView.setImageBitmap(UtilityClass.stringToBitmap(UtilityClass.setWeatherPictureToString(mContext,forecast.more.info)));

                //温度填充
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(forecast.temperature.min)
                        .append(" ~ ")
                        .append(forecast.temperature.max)
                        .append("℃");
                temperature.setText(stringBuilder);
                forecastLayout.addView(view);
            }

            if (weather.aqi != null) {
                DebugLog.d(TAG, "air quality data is not null");
                mAqiLayout.setVisibility(View.VISIBLE);//使空气质量窗口可见
                quality.setText(weather.aqi.city.qlty);
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

            String comfort = getString(R.string.string_comfort) + weather.suggestion.comfort.info;
            String carWash = getString(R.string.string_car_wash_index) + weather.suggestion.carWash.info;
            String sport = getString(R.string.string_sports_advice) + weather.suggestion.sport.info;
            String Uv = getString(R.string.string_UV) + weather.suggestion.uvIntensity.brf + "\n" + weather.suggestion.uvIntensity.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            comUvText.setText(Uv);

            if (weather.alarms != null) {
                mAlarmLayout.setVisibility(View.VISIBLE);
                DebugLog.d(TAG, "alarms data is not null");
                String alarmLevel = getString(R.string.string_level) + weather.alarms.level;
                String alarmStatus = getString(R.string.string_status) + weather.alarms.stat;
                String alarmInfo = weather.alarms.info;
                String alarmTitle = weather.alarms.title;
                mAlarmLevelText.setText(alarmLevel);
                mAlarmStatusText.setText(alarmStatus);
                mAlarmTitleText.setText(alarmTitle);
                mAlarmInfoText.setText(alarmInfo);
            }
            weatherLayout.setVisibility(View.VISIBLE);
        }else {
            DebugLog.e(TAG,"weather is null");
            UtilityClass.showToast(mContext,getString(R.string.toast_show_weather_info_failed));
        }
    }

    /**
     * 启动Service更新天气信息
     * */
    private void startAutoUpdateWeatherService(){
        DebugLog.d(TAG,"start auto update service");
        autoUpdateWeatherServiceIntent = new Intent(this, AutoUpdateWeatherService.class);
        startService(autoUpdateWeatherServiceIntent);
    }

    /**
     * 停止Service更新天气信息
     * */
    private void stopAutoUpdateWeatherService(){
        if (autoUpdateWeatherServiceIntent != null) {
            DebugLog.d(TAG, "stop auto update service");
            stopService(autoUpdateWeatherServiceIntent);
        }
    }

    /**
     * 请求运行时权限
     * */
    private void requestSelfPermission(){

        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(mContext,Manifest.permission.READ_PHONE_STATE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()){
            DebugLog.d(TAG,"request running permission");
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherShowActivity.this,permissions,1);
        }else{

            UtilityClass.setRequestRunningPermissionStatus(mContext,true);

            DebugLog.d(TAG,"no need to request running permission");
            if (UtilityClass.isNetWorkAvailable(mContext)) {
                startRequestLocation();
            }else{
                if (!isBackgroundUpdate) {
                    UtilityClass.showToast(mContext,getString(R.string.toast_detect_internet_no_useful));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission,@NonNull int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result:grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            DebugLog.d(TAG,"need to allow all permission");
                            UtilityClass.showToast(mContext,getString(R.string.toast_please_allow_all_permission));

                            finish();
                            return;
                        }
                    }

                    DebugLog.d(TAG,"request permissions result is ok");

                    UtilityClass.setRequestRunningPermissionStatus(mContext,true);

                    if (UtilityClass.isNetWorkAvailable(mContext)) {
                        startRequestLocation();
                    }else{
                        if (!isBackgroundUpdate) {
                            UtilityClass.showToast(mContext,getString(R.string.toast_detect_internet_no_useful));
                        }
                    }
                }else {
                    DebugLog.e(TAG,"request permission error");
                    UtilityClass.showToast(mContext,getString(R.string.toast_allow_permission_error));
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void startRequestLocation(){
        //开启定位Progress Dialog
        UtilityClass.showProgressDialog(mContext,getString(R.string.progress_dialog_being_location));

        DebugLog.d(TAG,"start request location");
        initLocation();
        mLocationClient.start();
    }

    private void stopRequestLocation(){
        if (mLocationClient.isStarted()){
            DebugLog.d(TAG,"stop request location");
            mLocationClient.stop();
        }else{
            DebugLog.d(TAG,"request location is not running,no need to stop it");
        }
    }

    private void initLocation(){
        DebugLog.d(TAG,"initLocation() enter");
        int SCAN_SPAN_TIME = 5000;
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setScanSpan(SCAN_SPAN_TIME);
        locationClientOption.setIsNeedAddress(true);
        mLocationClient.setLocOption(locationClientOption);
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder().append("\n");

            //关闭定位Progress Dialog
            UtilityClass.closeProgressDialog();

            switch (location.getLocType()){
                case BDLocation.TypeGpsLocation://GPS定位结果
                case BDLocation.TypeNetWorkLocation://网络定位结果
                case BDLocation.TypeOffLineLocation:// 离线定位结果
                    currentPosition.append("latitude:").append(location.getLatitude()).append("\n")
                            .append("longitude:").append(location.getLongitude()).append("\n")
                            .append("country:").append(location.getCountry()).append("\n")
                            .append("province:").append(location.getProvince()).append("\n")
                            .append("city:").append(location.getCity()).append("\n")
                            .append("area:").append(location.getDistrict()).append("\n")
                            .append("street:").append(location.getStreet()).append("\n")
                            .append("targeting:");

                    if (BDLocation.TypeGpsLocation == location.getLocType()){
                        currentPosition.append("GPS");
                    }else if (BDLocation.TypeNetWorkLocation == location.getLocType()){
                        currentPosition.append("internet");
                    }else if (BDLocation.TypeOffLineLocation == location.getLocType()) {
                        currentPosition.append("offline location");
                    }
                    break;
                case BDLocation.TypeServerError:
                    currentPosition.append("server network location failed");
                    break;
                case BDLocation.TypeNetWorkException:
                    currentPosition.append("network fails to locate, please check whether the network is smooth");
                    break;
                case BDLocation.TypeCriteriaException:
                    currentPosition.append("can not get effective positioning based on lead to failure, usually due to mobile phone reasons, in flight mode will generally cause this result, you can try to restart the phone");
                    break;
                default:
                    currentPosition.append("unknown positioning exception");
                    break;
            }

            DebugLog.e(TAG, currentPosition.toString());

            if (location.getDistrict() != null) {

                //停止定位功能
                stopRequestLocation();

                DebugLog.d(TAG, "get weather info");
                checkNetworkAndGetWeatherInfo(location.getDistrict());
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        //停止定位
        stopRequestLocation();

        //停止service
        stopAutoUpdateWeatherService();

        //注销广播
        unregisterUpdateWeatherInfoReceiver();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        /*
        * 取消所有申请的资源
        * */
        if (mCountDownTimer != null){
            DebugLog.d(TAG,"cancel timer");
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        isBackgroundUpdate = false;
    }

    /**
     * 注册广播接收器
     */
    private void registerUpdateWeatherInfoReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.europecoolweather.updateWeatherInfo.BROADCAST");
        updateWeatherInfoBroadcastReceiver = new UpdateWeatherInfoBroadcastReceiver();
        registerReceiver(updateWeatherInfoBroadcastReceiver, filter);
        DebugLog.d(TAG,"register Broadcast Receiver");
    }

    /**
    * 取消广播注册
    * */
    private void unregisterUpdateWeatherInfoReceiver(){
        if (updateWeatherInfoBroadcastReceiver != null) {
            unregisterReceiver(updateWeatherInfoBroadcastReceiver);
            DebugLog.d(TAG, "unregister Broadcast Receiver");
        }
    }

    /**
    * Service 发送更新天气信息广播在此处理
    * */
    private class UpdateWeatherInfoBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            if ("com.europecoolweather.updateWeatherInfo.BROADCAST".equals(intent.getAction())){
                SharedPreferences mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String weatherString = mDefaultSharedPreferences.getString("DefaultCityWeatherInfo", null);

                if (weatherString != null) {
                    // 有缓存时直接解析天气数据
                    DebugLog.d(TAG,"receive broadcast to update the weather info");
                    Weather weather = UtilityClass.handleWeatherResponse(weatherString);

                    //先显示已有的Weather信息
                    showWeatherInfo(weather);
                }
            }
        }
    }
}
