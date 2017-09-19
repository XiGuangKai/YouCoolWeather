package com.europecoolweather.activity;

import android.content.Context;
import android.content.Intent;
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
import com.europecoolweather.database.City;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ShowCitiesActivity extends AppCompatActivity {

    private final static String TAG = "ShowCityActivity";

    private Context mContext;
    private TextView selectCity;
    private TextView showCityTitle;
    /**
     * 市列表
     */
    private List<City> cityList;

    public static ShowCitiesActivity mShowCitiesActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_city);
        mContext = ShowCitiesActivity.this;
        mShowCitiesActivity = ShowCitiesActivity.this;
        findView();
        DebugLog.d(TAG, "onCreate() complete");
    }

    private void findView() {
        showCityTitle = (TextView) findViewById(R.id.show_city_title);
        DebugLog.d(TAG, "findView() complete");
    }

    private void initView() {
        showCityTitle.setText(R.string.tv_title_select_city);
        UtilityClass.currentLevel = UtilityClass.LEVEL_CITY;
        GridView addCityGrid = (GridView) findViewById(R.id.show_city_gridview);

        GridAddCityAdapter mGridAddCityAdapter = new GridAddCityAdapter(mContext);

        addCityGrid.setAdapter(mGridAddCityAdapter);

        addCityGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (UtilityClass.isNetWorkAvailable(mContext)) {
                    selectCity = (TextView) view.findViewById(R.id.citytext);
                    //获取选择的城市的名字
                    String selectCityName = selectCity.getText().toString();

                    //查找数据库中所有的城市
                    cityList = DataSupport.findAll(City.class);

                    //获取城市的code id
                    for (City mCity : cityList) {
                        if (selectCityName.equals(mCity.getCityName())) {
                            UtilityClass.cityCode = mCity.getCityCode();
                        }
                    }

                    //通过Intent启动ShowCityActivity
                    Intent startShowCountyActivityIntent = new Intent(mContext, ShowCountiesActivity.class);
                    DebugLog.d(TAG, "select city name is " + selectCityName + ", city code is " + UtilityClass.cityCode);
                    startShowCountyActivityIntent.putExtra("city_name", selectCityName);
                    startShowCountyActivityIntent.putExtra("cityCode", UtilityClass.cityCode);
                    startActivity(startShowCountyActivityIntent);

                    //将level设置成LEVEL_CITY
                    UtilityClass.currentLevel = UtilityClass.LEVEL_COUNTY;
                } else {
                    DebugLog.e(TAG, "network is not useful");
                    UtilityClass.showToast(mContext, getString(R.string.toast_internet_no_useful_to_get_city));
                }
            }
        });

        DebugLog.d(TAG, "initView() complete");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //解决当重新返回省份选择界面时，由于level的问题导致点击没有效果
        UtilityClass.currentLevel = UtilityClass.LEVEL_CITY;
        if (UtilityClass.isNetWorkAvailable(mContext)) {
            getCitiesList();
        } else {
            DebugLog.e(TAG, "network is not useful");
            UtilityClass.showToast(mContext, getString(R.string.toast_internet_no_useful_to_get_city));
        }
        DebugLog.d(TAG, "onResume() complete");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清除不必要的对象
        if (mShowCitiesActivity != null) {
            mShowCitiesActivity = null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UtilityClass.MESSAGE_SUCCESS:
                    DebugLog.d(TAG, "get cities successfully");
                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_SUCCESS);

                    //关闭动态ProgressDialog
                    UtilityClass.closeProgressDialog();

                    //填装数据
                    initView();
                    break;
                case UtilityClass.MESSAGE_FAILED:
                    DebugLog.e(TAG, "get cities list failed");

                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_FAILED);

                    //关闭动态ProgressDialog
                    UtilityClass.closeProgressDialog();

                    break;
                default:
                    DebugLog.e(TAG, "an unknown error occurred");

                    //关闭动态ProgressDialog
                    UtilityClass.closeProgressDialog();

                    break;
            }
        }
    };

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void getCitiesList() {

        int i = 0;
        //市列表
        List<City> cityList = DataSupport.where("provinceCode = ?", String.valueOf(UtilityClass.provinceCode)).find(City.class);
        DebugLog.d(TAG, "city list size = " + cityList.size() + ", provinceCode = " + UtilityClass.provinceCode);

        if (cityList.size() > 0 && cityList.size() < 30) {

            GridAddCityAdapter.citiesName = new String[cityList.size()];
            for (City city : cityList) {
                DebugLog.d(TAG, " i = " + i);
                GridAddCityAdapter.citiesName[i] = city.getCityName();
                i++;
            }

            UtilityClass.currentLevel = UtilityClass.LEVEL_CITY;

            cityList.clear();
            DebugLog.d(TAG, "add the dataList finished from the database");

            Message msg = new Message();
            msg.what = UtilityClass.MESSAGE_SUCCESS;
            mHandler.sendMessage(msg);
        } else {
            String address = "http://guolin.tech/api/china/" + UtilityClass.provinceCode;
            DebugLog.d(TAG, "address = " + address);

            DataSupport.deleteAll(City.class);
            getCitiesByInternet(address);
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void getCitiesByInternet(String address) {
        UtilityClass.showProgressDialog(mContext, getString(R.string.progress_dialog_content_get_cities));

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = UtilityClass.handleCityResponse(responseText, UtilityClass.provinceCode);

                DebugLog.d(TAG, "Response result = " + result);
                if (result) {
                    DebugLog.d(TAG, "type is cities call getCitiesList()");
                    getCitiesList();
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
