package com.europecoolweather.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.europecoolweather.R;
import com.europecoolweather.adapter.GridAddCityAdapter;
import com.europecoolweather.database.County;
import com.europecoolweather.database.SQLiteCityManager;
import com.europecoolweather.gson.Weather;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.europecoolweather.activity.ShowCitiesActivity.mShowCitiesActivity;
import static com.europecoolweather.activity.ShowProvinceActivity.mShowProvinceActivity;

public class ShowCountiesActivity extends AppCompatActivity {

    private final static String TAG = "ShowCountyActivity";

    private Context mContext;
    private TextView showCountyTitle;
    private TextView selectCounty;
    private boolean hasExisted;

    private SQLiteCityManager sqlite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_county);
        mContext = ShowCountiesActivity.this;
        findView();
        DebugLog.d(TAG, "onCreate() complete");
    }

    /**
     * 给各个组件赋值
     */
    private void findView() {
        showCountyTitle = (TextView) findViewById(R.id.show_county_title);
        DebugLog.d(TAG, "findView() complete");
    }

    /**
     * 给界面的显示填装数据
     */
    private void initView() {
        //初始化数据库
        sqlite = new SQLiteCityManager(mContext, UtilityClass.YOU_COOL_DATABASE, null, 1);

        //设置标题
        showCountyTitle.setText(R.string.tv_title_select_counties);
        GridView showCountyGrid = (GridView) findViewById(R.id.show_county_gridview);

        GridAddCityAdapter mGridAddCityAdapter = new GridAddCityAdapter(mContext);

        showCountyGrid.setAdapter(mGridAddCityAdapter);

        showCountyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectCounty = (TextView) view.findViewById(R.id.citytext);
                //获取选择的省份的名字
                String selectCountyName = selectCounty.getText().toString();

                DebugLog.d(TAG, "select county name is " + selectCountyName);

                //将level设置成LEVEL_CITY
                UtilityClass.currentLevel = UtilityClass.LEVEL_COUNTY;

                selectCounty.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.city_checkbox_selected, 0);
                queryData(selectCountyName);
                // 如果数据库中没有该城市，则添加到数据库。反之则提示。
                if (!hasExisted) {
                    DebugLog.d(TAG, "insert new city info");
                    //获取城市天气的信息，并且将数据插入数据库
                    getCityWeatherInfo(selectCountyName);
                } else {
                    DebugLog.v(TAG, "selected city has exist");
                    UtilityClass.showToast(mContext, getString(R.string.toast_context_not_repeat_add_county));
                }
            }
        });

        DebugLog.d(TAG, "initView() complete");
    }

    /**
     * 根据城市名称设置城市天气信息
     *
     * @param cityName 城市名字
     */
    private void getCityWeatherInfo(final String cityName) {

        UtilityClass.showProgressDialog(mContext, getString(R.string.progress_dialog_getting_weather_info));
        //拼凑访问和风天气的API地址
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + cityName + "&key=6616624b9a104d3aa3afe5dfef16783c";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = UtilityClass.handleWeatherResponse(responseText);

                if (weather != null && "ok".equals(weather.status)) {
                    DebugLog.d(TAG, "insert weather success");

                    //将获取的数据插入数据库
                    UtilityClass.insertOrUpdateDatabase(mContext, weather, UtilityClass.setWeatherPictureToString(mContext, weather.now.more.info), responseText);
                    Message msg = new Message();
                    msg.what = UtilityClass.MESSAGE_GET_INFO_SUCCESS;
                    mHandler.sendMessage(msg);
                } else {
                    DebugLog.e(TAG, "insert weather failed");
                    Message msg = new Message();
                    msg.what = UtilityClass.MESSAGE_GET_INFO_FAILED;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                DebugLog.e(TAG, "get city weather failed");
                Message msg = new Message();
                msg.what = UtilityClass.MESSAGE_GET_INFO_FAILED;
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     * 将为新选择的区(县)插入到数据库
     */
    private void insertData() {

        SQLiteDatabase db = sqlite.getReadableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("city_name", selectCounty.getText().toString());
        cv.put("image_url", "");
        cv.put("weather", R.string.db_click_update);
        cv.put("temperature", "0℃");
        db.insert(UtilityClass.YOU_COOL_WEATHER, "city_name", cv);
    }

    /**
     * 查询数据库中所选的区（县）是否存在
     *
     * @param mSelectCountyName 所选的区县的名字
     */
    public void queryData(String mSelectCountyName) {
        // 读写数据库
        SQLiteDatabase db = sqlite.getReadableDatabase();
        Cursor cursor = db.query(UtilityClass.YOU_COOL_WEATHER, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String cityName = cursor.getString(cursor.getColumnIndex("city_name"));
            cityName = cityName.substring(0, 2);
            mSelectCountyName = mSelectCountyName.substring(0, 2);
            // 与当前按下的城市名做比较
            if (hasExisted = cityName.equals(mSelectCountyName)) {
                DebugLog.d(TAG, "city has exist");
                cursor.close();
                db.close();
                return;
            }
        }
        cursor.close();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //解决当重新返回省份选择界面时，由于level的问题导致点击没有效果
        UtilityClass.currentLevel = UtilityClass.LEVEL_COUNTY;
        if (UtilityClass.isNetWorkAvailable(mContext)) {
            getCountiesList();
        } else {
            DebugLog.e(TAG, "network is not useful");
            UtilityClass.showToast(mContext, getString(R.string.toast_internet_no_useful_to_get_city));
        }
        DebugLog.d(TAG, "onResume() complete");
    }

    /**
     * handle处理界面数据的更新
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //关闭动态ProgressDialog
            UtilityClass.closeProgressDialog();

            switch (msg.what) {
                case UtilityClass.MESSAGE_SUCCESS:
                    DebugLog.d(TAG, "get counties successfully");
                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_SUCCESS);

                    //填装数据
                    initView();
                    break;
                case UtilityClass.MESSAGE_FAILED:
                    DebugLog.e(TAG, "get counties list failed");

                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_FAILED);

                    break;
                case UtilityClass.MESSAGE_GET_INFO_FAILED:

                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_GET_INFO_FAILED);

                    //在数据库中插入空数据
                    insertData();

                    //弹出Toast
                    UtilityClass.showToast(mContext, getString(R.string.toast_content_get_weather_info_failed));

                    //关闭所有选择城市界面
                    if (mShowProvinceActivity != null) {
                        mShowProvinceActivity.finish();
                        mShowProvinceActivity = null;
                    }

                    if (mShowCitiesActivity != null) {
                        mShowCitiesActivity.finish();
                        mShowCitiesActivity = null;
                    }

                    finish();

                    break;
                case UtilityClass.MESSAGE_GET_INFO_SUCCESS:

                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_GET_INFO_SUCCESS);

                    //关闭所有选择城市界面
                    if (mShowProvinceActivity != null) {
                        mShowProvinceActivity.finish();
                        mShowProvinceActivity = null;
                    }

                    if (mShowCitiesActivity != null) {
                        mShowCitiesActivity.finish();
                        mShowCitiesActivity = null;
                    }

                    finish();

                    break;
                default:
                    DebugLog.e(TAG, "an unknown error occurred");
                    break;
            }
        }
    };

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void getCountiesList() {
        //县列表
        List<County> countyList;
        int i = 0;

        countyList = DataSupport.where("cityId = ?", String.valueOf(UtilityClass.cityCode)).find(County.class);
        DebugLog.d(TAG, "counties list size = " + countyList.size());

        if (countyList.size() > 0 && countyList.size() <= 20) {

            GridAddCityAdapter.countiesName = new String[countyList.size()];
            for (County county : countyList) {
                DebugLog.d(TAG, " i = " + i);
                GridAddCityAdapter.countiesName[i] = county.getCountyName();
                i++;
            }

            countyList.clear();
            UtilityClass.currentLevel = UtilityClass.LEVEL_COUNTY;
            DebugLog.d(TAG, "add the data list finished from the database");

            Message msg = new Message();
            msg.what = UtilityClass.MESSAGE_SUCCESS;
            mHandler.sendMessage(msg);

        } else {
            String address = "http://guolin.tech/api/china/" + UtilityClass.provinceCode + "/" + UtilityClass.cityCode;
            DebugLog.d(TAG, "address = " + address);

            DataSupport.deleteAll(County.class);
            getCitiesByInternet(address);
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     *
     * @param address 获取城市数据的地址
     */
    private void getCitiesByInternet(String address) {
        UtilityClass.showProgressDialog(mContext, getString(R.string.progress_dialog_getting_counties));

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = UtilityClass.handleCountyResponse(responseText, UtilityClass.cityCode);

                DebugLog.d(TAG, "response result = " + result);
                if (result) {
                    DebugLog.d(TAG, "type is counties call getCountiesList()");
                    getCountiesList();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                DebugLog.e(TAG, "internet error");
                Message msg = new Message();
                msg.what = UtilityClass.MESSAGE_FAILED;
                mHandler.sendMessage(msg);
                UtilityClass.showToast(mContext, getString(R.string.toast_internet_error));
            }
        });
    }
}
