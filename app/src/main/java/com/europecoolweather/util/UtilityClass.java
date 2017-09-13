package com.europecoolweather.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.europecoolweather.R;
import com.europecoolweather.database.City;
import com.europecoolweather.database.County;
import com.europecoolweather.database.Province;
import com.europecoolweather.database.SQLiteCityManager;
import com.europecoolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * 公共方法类
 * @author GuangKai
 * @version 版本1.0
 */

public class UtilityClass {

    private static final String TAG = "UtilityClass";

    //Progress Dialog相关的参数
    private static ProgressDialog progressDialog;
    public static boolean isShowProgressDialog = true;

    //handle消息
    public static final int MESSAGE_SUCCESS = 0;
    public static final int MESSAGE_FAILED = -1;
    public static final int MESSAGE_GET_INFO_SUCCESS = 1;
    public static final int MESSAGE_GET_INFO_FAILED = -2;

    //城市选择时的级别控制
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static int currentLevel = LEVEL_PROVINCE;
    public static int provinceCode = -1;
    public static int cityCode = -1;

    //数据库名字和数据库表名
    public static final String YOU_COOL_DATABASE = "YouCoolDatabase";
    public static final String YOU_COOL_WEATHER = "YouCoolWeather";
    private static final int DATABASE_VERSION = 1;

    //30分钟的循环
    public static int CYCLE_INTERVAL_TIME = 30 * 60 * 1000;

    //显示默认的城市信息
    public static final String SHOW_DEFAULT_CITY = "DEFAULT_CITY";
    //显示其他城市信息
    public static final String SHOW_CHOOSE_CITY = "CHOOSE_CITY";
    //显示定位城市信息
    public static final String SHOW_DATABASE_CITY = "DATABASE_CITY";
    //显示定位城市信息
    public static final String SHOW_LOCATION_CITY = "LOCAL_CITY";
    //控制显示信息
    public static String SHOW_CITY_WEATHER_INFO = SHOW_DEFAULT_CITY;

    //定时器相关参数
    private static AlarmManager mAlarmManager;
    private static PendingIntent mPendingIntent;
    public static final String BROADCAST_ALARM = "Broadcast";
    public static final String ACTIVITY_ALARM = "Activity";
    public static final String SERVICE_ALARM = "Service";

    //SharedPreferences相关参数
    public static final String DEFAULT_CITY_TYPE = "DefaultCity";
    public static final String CHOOSE_CITY_TYPE = "ChooseCity";

    //所有的天气状况
    private static final String SUNNY = "晴";
    private static final String PARTLY_CLOUDY = "多云";
    private static final String CLOUDY = "阴";
    private static final String THUNDERSTORMS = "雷阵雨";
    private static final String THUNDERSTORMS_ACCOMPANIED_BY_HAIL = "雷阵雨伴有冰雹";
    private static final String SLEET = "雨夹雪";
    private static final String RAIN = "雨";
    private static final String SHOWER = "阵雨";
    private static final String LIGHT_RAIN = "小雨";
    private static final String MIDDLE_RAIN = "中雨";
    private static final String HEAVY_RAIN = "大雨";
    private static final String RAINSTORM = "暴雨";
    private static final String HEAVY_RAINSTORM = "大暴雨";
    private static final String HEAVY_HEAVY_RAINSTORM = "特大暴雨";
    private static final String SNOW_SHOWERS = "阵雪";
    private static final String LIGHT_SNOW = "小雪";
    private static final String SNOW = "雪";
    private static final String MIDDLE_SNOW = "中雪";
    private static final String HEAVY_SNOW = "大雪";
    private static final String BLIZZARD = "暴雪";
    private static final String FOG = "雾";
    private static final String FROZEN_RAIN = "冻雨";
    private static final String SANDSTORMS = "沙尘暴";
    private static final String LIGHT_RAIN_TO_MIDDLE_RAIN = "小雨转中雨";
    private static final String MIDDLE_RAIN_TO_HEAVY_RAIN = "中雨转大雨";
    private static final String HEAVY_RAIN_TO_RAINSTORM = "大雨转暴雨";
    private static final String RAINSTORM_TO_HEAVY_RAINSTORM = "暴雨转大暴雨";
    private static final String HEAVY_RAINSTORM_TO_HEAVY_HEAVY_RAINSTORM = "大暴雨转特大暴雨";
    private static final String LIGHT_SNOW_TO_MIDDLE_SNOW = "小雪转中雪";
    private static final String MIDDLE_SNOW_TO_HEAVY_SNOW = "中雪转大雪";
    private static final String HEAVY_SNOW_TO_BLIZZARD = "大雪转暴雪";
    private static final String FLOATING_DUST = "浮尘";
    private static final String JANSHA = "扬沙";
    private static final String STRONG_DUST_STORMS = "强沙尘暴";
    private static final String SMOG = "雾霾";
    private static final String HAZE = "霾";

    /**
     * 保存运行时权限申请的状态
     * @param context 上下文信息
     * @param status 要保存的状态
     */
    public static void setRequestRunningPermissionStatus(Context context,boolean status) {
        //将权限状态标志保存
        SharedPreferences mStoredData = context.getSharedPreferences("stored_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorStoredData = mStoredData.edit();
        editorStoredData.putBoolean("isRequestPermissionOK", status);
        editorStoredData.apply();
    }

    /**
     * 获取运行时权限的状态
     * @param context 上下文信息
     * @return 返回运行时权限的状态，true or false
     */
    public static boolean getRequestRunningPermissionStatus(Context context){

        SharedPreferences mStoredData = context.getSharedPreferences("stored_data", Context.MODE_PRIVATE);

        boolean isRequestPermissionOK = mStoredData.getBoolean("isRequestPermissionOK", false);

        DebugLog.d(TAG,"running permission status is " + isRequestPermissionOK);
        return isRequestPermissionOK;
    }

    /**
     * 获取城市名字
     * @param context 上下文信息
     * @param type 获取选择的城市或者获取默认的城市的类型
     * @return 返回城市名字;如果没有找到或者参数类型错误，则返回null;
     */
    public static String getCityName(Context context,String type){
        if (type.isEmpty() || ((!DEFAULT_CITY_TYPE.equals(type)) && (!CHOOSE_CITY_TYPE.equals(type)))){
            DebugLog.e(TAG,"get city name parameter error");
            return null;
        }
        DebugLog.d(TAG,"get city name");
        SharedPreferences mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (DEFAULT_CITY_TYPE.equals(type)) {
            return mDefaultSharedPreferences.getString(DEFAULT_CITY_TYPE, null);
        }else if (CHOOSE_CITY_TYPE.equals(type)){
            return mDefaultSharedPreferences.getString(CHOOSE_CITY_TYPE, null);
        }
        return null;
    }

    /**
     * 保存城市的名字
     * @param context 上下文信息
     * @param type 获取选择的城市或者获取默认的城市的类型
     * @param cityName 要保存的城市名字
     */
    public static void setCityName(Context context,String type,final String cityName){
        if (type.isEmpty() || ((!DEFAULT_CITY_TYPE.equals(type)) && (!CHOOSE_CITY_TYPE.equals(type)))){
            DebugLog.e(TAG,"set city name parameter error");
            return;
        }

        DebugLog.d(TAG,"set default city name = " + cityName);
        SharedPreferences mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEditorDefaultSharedPreferences = mDefaultSharedPreferences.edit();
        if (DEFAULT_CITY_TYPE.equals(type)) {
            mEditorDefaultSharedPreferences.putString(DEFAULT_CITY_TYPE, cityName);
        }else if (CHOOSE_CITY_TYPE.equals(type)){
            mEditorDefaultSharedPreferences.putString(CHOOSE_CITY_TYPE, cityName);
        }
        mEditorDefaultSharedPreferences.apply();
    }

    /**
     * 删除城市的Key
     * @param context 上下文信息
     */
    public static void removeCityName(Context context,String type){
        if (type.isEmpty() || ((!DEFAULT_CITY_TYPE.equals(type)) && (!CHOOSE_CITY_TYPE.equals(type)))){
            DebugLog.e(TAG,"set city name parameter error");
            return;
        }
        DebugLog.d(TAG,"remove city name");
        SharedPreferences.Editor editorDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (DEFAULT_CITY_TYPE.equals(type)) {
            editorDefaultSharedPreferences.remove(DEFAULT_CITY_TYPE);
        }else if (CHOOSE_CITY_TYPE.equals(type)){
            editorDefaultSharedPreferences.remove(CHOOSE_CITY_TYPE);
        }
        editorDefaultSharedPreferences.apply();
    }

    /**
     * 更新数据库
     * @param context 上下文信息
     * @param weather 要保存的天气信息对象weather
     * @param weatherPicture 把图片被String化的字符串
     * @param responseWeatherInfo 获取到的所有的天气信息，其实也可以用weather替代
     * */
    public static void insertOrUpdateDatabase(Context context,Weather weather,String weatherPicture,String responseWeatherInfo){
        if (context != null && weather != null && weatherPicture != null) {
            // 创建SQLite对象并不会创建数据库
            SQLiteCityManager sqlite = new SQLiteCityManager(context, YOU_COOL_DATABASE, null, DATABASE_VERSION);
            // 读写数据库
            SQLiteDatabase db = sqlite.getWritableDatabase();

            // ContentValues键值对，类似HashMap
            ContentValues cv = new ContentValues();
            // key为字段名，value为所存数据
            cv.put("city_name", weather.basic.cityName);
            cv.put("image_url", weatherPicture);
            cv.put("weather", weather.now.more.info);
            cv.put("temperature", weather.now.temperature + "℃");
            cv.put("weather_description", responseWeatherInfo);
            Cursor cursor = db.query(YOU_COOL_WEATHER, null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                String cityName = cursor.getString(cursor.getColumnIndex("city_name"));
                cityName = cityName.substring(0, 2);
                String cityText = weather.basic.cityName;
                if (cityText.equals(cityName)) {
                    DebugLog.d(TAG, "update database cityName = " + cityName);
                    db.update(YOU_COOL_WEATHER, cv, "city_name = ?", new String[]{cityName});
                    db.close();
                    cursor.close();
                    return;
                }
            }
            // 插入，第二个参数:不能为null的字段
            DebugLog.d(TAG, "insert database cityName = " + weather.basic.cityName);
            db.insert(YOU_COOL_WEATHER, "city_name", cv);
            db.close();
            cursor.close();
        }
    }

    /**
     * 查询当前数据库中是否存在当前要查询的天气数据
     * @param context 上下文信息
     * @param cityName 要查询的城市名字
     * @return 存在返回true，否则返回false
     */
    public static String queryDatabaseWhetherCityExists(Context context,String cityName) {
        SQLiteCityManager SQLite = new SQLiteCityManager(context,UtilityClass.YOU_COOL_DATABASE, null, UtilityClass.DATABASE_VERSION);
        SQLiteDatabase db = SQLite.getWritableDatabase();
        Cursor cursor = db.query(UtilityClass.YOU_COOL_WEATHER, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("city_name")).equals(cityName)){
                DebugLog.d(TAG,"return city presence weather_description");
                return cursor.getString(cursor.getColumnIndex("weather_description"));
            }
        }
        cursor.close();
        DebugLog.e(TAG,"city not presence");
        return null;
    }

    /**
     * 查询数据库中第一条记录
     * @param context 上下文信息
     * @return CityManagerEntity，查询到的数据存储到CityManagerEntity对象中,如果没有则返回null
     */
    public static String queryDatabaseFirstId(Context context) {
        SQLiteCityManager SQLite = new SQLiteCityManager(context,UtilityClass.YOU_COOL_DATABASE, null, UtilityClass.DATABASE_VERSION);
        SQLiteDatabase db = SQLite.getWritableDatabase();
        Cursor cursor = db.query(UtilityClass.YOU_COOL_WEATHER, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            DebugLog.d(TAG,"first id = " + cursor.getString(cursor.getColumnIndex("_id")));
            return cursor.getString(cursor.getColumnIndex("weather_description"));
        }

        cursor.close();
        return null;
    }

    /**
     * 删除数据库中的表
     * @param context 上下文信息
     * @param tableName 要删除的数据库中表的名字
     */
    public static void deleteDatabase(Context context,String tableName){
        // 创建SQLite对象并不会创建数据库
        SQLiteCityManager sqlite = new SQLiteCityManager(context,YOU_COOL_DATABASE, null, DATABASE_VERSION);
        // 读写数据库
        SQLiteDatabase db = sqlite.getWritableDatabase();

        //删除所有数据
        db.delete(tableName, null, null);

        db.close();
    }

    /**
    * 解析服务器返回的省级数据
    * @param response 从网络获取的省级数据
    * @return 成功返回true，失败返回false
    * */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);
                DebugLog.d(TAG,"allProvinces.length() = " + allProvinces.length());

                for (int i = 0;i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);

                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                DebugLog.e(TAG,"handle Province Response JSONException");
                e.printStackTrace();
            }
        }
        DebugLog.e(TAG,"handle Province Response return false");
        return false;
    }

    /**
    * 解析服务器返回的市级数据
    * @param response 从网络获取的市级数据
    * @param provinceId 省的ID
    * @return 成功返回true，失败返回false
    * */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try{
                DebugLog.d(TAG,"handle Cities Response enter");
                JSONArray allCities = new JSONArray(response);

                for (int i = 0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceCode(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                DebugLog.e(TAG,"handle Cities Response JSONException");
                e.printStackTrace();
            }
        }
        DebugLog.e(TAG,"handle Cities Response return false");
        return false;
    }

    /**
    * 解析服务器返回的县级数据
    * @param response 从网络获取的县级数据
    * @param cityId 市的ID
    * @return 成功返回true，失败返回false
    * */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                DebugLog.d(TAG,"handle County Response enter");
                JSONArray allCounties = new JSONArray(response);

                for (int i = 0;i<allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                DebugLog.e(TAG,"handle County Response JSONException");
                e.printStackTrace();
            }
        }
        DebugLog.e(TAG,"handle County Response return false");
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     * @param response 从网络API所获取的数据
     * @return Weather 返回Weather类的实体
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            DebugLog.d(TAG,"handle weather response weather info");
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DebugLog.e(TAG,"handle weather response weather info is null");
        return null;
    }

    /**
    * 检测当前网络的状态确保该应用在使用时有网络可用
    * @param context 上下文信息
    * @return 网络可用返回true，网络不可用返回false
    * */
    public static boolean isNetWorkAvailable(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        DebugLog.d(TAG,"network available is " + (!(networkInfo==null||!networkInfo.isAvailable())));
        return (!(networkInfo==null||!networkInfo.isAvailable()));
    }

    /**
     * 显示Toast
     * @param context 参数Context
     * @param showInfo 希望显示的字符串
     */
    public static void showToast(Context context,String showInfo){
        Toast mToast = Toast.makeText(context,showInfo,Toast.LENGTH_SHORT);
        //mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();
    }

    /**
     * 图片转换成字符串
     * @param bitmap 需要转换成字符串的bitmap
     * @return 转换成功后的字符串
     */
    public static String bitmapToString(Bitmap bitmap){

        //将Bitmap转换成字符串
        ByteArrayOutputStream bStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bStream);
        byte[]bytes=bStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    /**
     * 字符串转换成图片
     * @param string 需要转换成图片的字符串
     * @return 转换成的图片Bitmap
     */
    public static Bitmap stringToBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 显示进度对话框
     * @param context 上下文信息
     * @param progressDialogContent 要显示在progressDialog上的内容
     */
    public static void showProgressDialog(Context context,String progressDialogContent) {
        DebugLog.d(TAG,"showProgressDialog isShowProgressDialog = " + isShowProgressDialog);
        if (isShowProgressDialog) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(progressDialogContent + "....");
                progressDialog.setCanceledOnTouchOutside(false);
            }
            DebugLog.d(TAG, "show progress dialog");
            progressDialog.show();
        }
    }

    /**
     * 关闭进度progressDialog
     */
    public static void closeProgressDialog() {
        if (progressDialog != null) {
            DebugLog.d(TAG,"close progress dialog");
            progressDialog.dismiss();
            progressDialog = null;
        }
        DebugLog.d(TAG,"closeProgressDialog isShowProgressDialog = " + isShowProgressDialog);
        isShowProgressDialog = true;
    }

    /**
     * 启动定时器
     * @param context 要设置定时器的对象如，this，new Xxx()
     * @param update_time_interval 间隔多长时间启动一次
     * @param alarmType 创建计时器的类型，必须是SERVICE_ALARM、ACTIVITY_ALARM、BROADCAST_ALARM中的一个
     */
    public static void startAlarm(Context context,int update_time_interval,String alarmType){

        DebugLog.d(TAG,"update time interval = " + update_time_interval + ", alarmType = " + alarmType);
        if (alarmType.isEmpty() || ((!SERVICE_ALARM.equals(alarmType)) && (!ACTIVITY_ALARM.equals(alarmType)) && (!BROADCAST_ALARM.equals(alarmType)))){
            DebugLog.e(TAG,"alarm type can not distinguish");
            return;
        }

        if (mAlarmManager == null){
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        long triggerAtTime = SystemClock.elapsedRealtime() + update_time_interval;

        DebugLog.d(TAG,"class = " + context.getClass());
        Intent intent = new Intent(context,context.getClass());

        if (SERVICE_ALARM.equals(alarmType)) {
            if (mPendingIntent == null) {
                mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
            }
        }else if (ACTIVITY_ALARM.equals(alarmType)){
            if (mPendingIntent == null) {
                mPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            }
        }else if (BROADCAST_ALARM.equals(alarmType)){
            if (mPendingIntent == null) {
                mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            }
        }

        if (mAlarmManager != null && mPendingIntent != null) {
            DebugLog.d(TAG,"start an alarm manager");
            mAlarmManager.cancel(mPendingIntent);
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, mPendingIntent);
        }
    }

    /**
     * 取消闹钟
     */
    public static void stopAlarm(){
        if (mAlarmManager != null && mPendingIntent != null) {
            DebugLog.d(TAG,"stop an alarm manager");
            mAlarmManager.cancel(mPendingIntent);
            mAlarmManager = null;
            mPendingIntent = null;
        }
    }

    /**
     * 根据获取到的天气状况配置对应的天气图片
     * @param context 上下文信息
     * @param weatherInfo 天气信息
     * @return 将对应的图片信息转变成String形式后返回
     */
    public static String setWeatherPictureToString(Context context, final String weatherInfo){
        String weatherPicture = "";
        Resources mResources = context.getResources();
        if (weatherInfo != null){

            DebugLog.d(TAG,"weather info is " + weatherInfo);

            switch (weatherInfo){
                case SUNNY:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.sunny));
                    break;
                case PARTLY_CLOUDY:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.partly_cloudy));
                    break;
                case CLOUDY:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.cloudy));
                    break;
                case THUNDERSTORMS:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.thunderstorms));
                    break;
                case THUNDERSTORMS_ACCOMPANIED_BY_HAIL:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.thunderstorms_accompanied_by_hail));
                    break;
                case SLEET:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.sleet));
                    break;
                case RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.rain));
                    break;
                case SHOWER:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.shower));
                    break;
                case LIGHT_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.light_rain));
                    break;
                case MIDDLE_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.middle_rain));
                    break;
                case HEAVY_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_rain));
                    break;
                case RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.rainstorm));
                    break;
                case HEAVY_RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_rainstorm));
                    break;
                case HEAVY_HEAVY_RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_heavy_rainstorm));
                    break;
                case SNOW_SHOWERS:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.snow_showers));
                    break;
                case LIGHT_SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.light_snow));
                    break;
                case SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.snow));
                    break;
                case MIDDLE_SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.middle_snow));
                    break;
                case HEAVY_SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_snow));
                    break;
                case BLIZZARD:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.blizzard));
                    break;
                case FOG:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.fog));
                    break;
                case FROZEN_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.frozen_rain));
                    break;
                case SANDSTORMS:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.sandstorms));
                    break;
                case LIGHT_RAIN_TO_MIDDLE_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.light_rain_to_middle_rain));
                    break;
                case MIDDLE_RAIN_TO_HEAVY_RAIN:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.middle_rain_to_heavy_rain));
                    break;
                case HEAVY_RAIN_TO_RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_rain_to_rainstorm));
                    break;
                case RAINSTORM_TO_HEAVY_RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.rainstorm_to_heavy_rainstorm));
                    break;
                case HEAVY_RAINSTORM_TO_HEAVY_HEAVY_RAINSTORM:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_rainstorm_to_heavy_heavy_rainstorm));
                    break;
                case LIGHT_SNOW_TO_MIDDLE_SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.light_snow_to_middle_snow));
                    break;
                case MIDDLE_SNOW_TO_HEAVY_SNOW:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.middle_snow_to_heavy_snow));
                    break;
                case HEAVY_SNOW_TO_BLIZZARD:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.heavy_snow_to_blizzard));
                    break;
                case FLOATING_DUST:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.floating_dust));
                    break;
                case JANSHA:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.jansha));
                    break;
                case STRONG_DUST_STORMS:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.strong_dust_storms));
                    break;
                case SMOG:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.smog));
                    break;
                case HAZE:
                    weatherPicture = UtilityClass.bitmapToString(BitmapFactory.decodeResource(mResources, R.drawable.haze));
                    break;
                default:
                    DebugLog.e(TAG,"can not known weather information");
                    break;
            }
        }
        return weatherPicture;
    }
}
