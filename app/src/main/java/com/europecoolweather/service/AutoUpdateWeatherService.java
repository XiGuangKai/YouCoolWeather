package com.europecoolweather.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.europecoolweather.gson.Weather;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateWeatherService extends Service {

    private final static String TAG = "AutoUpdateWeatherService";

    private static final int AUTO_TIME_INTERVAL = 60 * 1000;

    public AutoUpdateWeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //更新天气信息
        updateWeather();

        //更新背景图片
        updateBingPic();

        //启动定时器，每隔一分钟刷新一次天气数据
        UtilityClass.startAlarm(this, AUTO_TIME_INTERVAL, UtilityClass.SERVICE_ALARM);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //停止计时器
        UtilityClass.stopAlarm();
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather() {

        //城市的天气描述信息
        String mWeatherDescription = null;

        //获取存储的默认的城市名字和选择的城市名字
        String mDefaultCityName = UtilityClass.getCityName(getBaseContext(), UtilityClass.DEFAULT_CITY_TYPE);
        String mChooseCityName = UtilityClass.getCityName(getBaseContext(), UtilityClass.CHOOSE_CITY_TYPE);

        {//根据不同状态获取不同的城市列表
            if (UtilityClass.SHOW_DEFAULT_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)) {
                DebugLog.d(TAG, "default city weather description");
                mWeatherDescription = UtilityClass.queryDatabaseWhetherCityExists(getBaseContext(), mDefaultCityName);
            }

            if (UtilityClass.SHOW_CHOOSE_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)) {
                DebugLog.d(TAG, "choose city weather description");
                mWeatherDescription = UtilityClass.queryDatabaseWhetherCityExists(getBaseContext(), mChooseCityName);
            }

            if (UtilityClass.SHOW_DATABASE_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)) {
                DebugLog.d(TAG, "database first city weather description");
                mWeatherDescription = UtilityClass.queryDatabaseFirstId(getBaseContext());
            }

            if (UtilityClass.SHOW_LOCATION_CITY.equals(UtilityClass.SHOW_CITY_WEATHER_INFO)) {
                DebugLog.e(TAG, "no city weather description");
                return;
            }
        }

        if (mWeatherDescription != null) {

            // 有缓存时直接解析天气数据
            Weather weather = UtilityClass.handleWeatherResponse(mWeatherDescription);
            if (weather != null) {
                String cityName = weather.basic.cityName;
                DebugLog.d(TAG, "auto update " + cityName + " weather info");
                String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + cityName + "&key=6616624b9a104d3aa3afe5dfef16783c";

                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseText = response.body().string();
                        Weather weather = UtilityClass.handleWeatherResponse(responseText);

                        if (weather != null && "ok".equals(weather.status)) {

                            //将数据更新到数据库
                            UtilityClass.insertOrUpdateDatabase(getBaseContext(), weather, UtilityClass.setWeatherPictureToString(getBaseContext(), weather.now.more.info), responseText);

                            //发送广播更新天气信息
                            Intent intent = new Intent("com.europecoolweather.updateWeatherInfo.BROADCAST");
                            sendBroadcast(intent);
                            DebugLog.d(TAG, "send broadcast to update weather info");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        DebugLog.d(TAG, "weather update error");
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * 更新必应每日一图
     */
    private void updateBingPic() {
        DebugLog.d(TAG, "updateBingPic() enter");

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences mStoredData = getBaseContext().getSharedPreferences("stored_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorStoredData = mStoredData.edit();
                editorStoredData.putString("backgroundPicture", bingPic);
                editorStoredData.apply();
                DebugLog.d(TAG, "update the background picture");
            }

            @Override
            public void onFailure(Call call, IOException e) {
                DebugLog.e(TAG, "get picture from internet failed");
                e.printStackTrace();
            }
        });
    }

}
