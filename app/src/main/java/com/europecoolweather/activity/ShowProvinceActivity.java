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
import com.europecoolweather.database.Province;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ShowProvinceActivity extends AppCompatActivity {

    private static final String TAG = "ShowProvinceActivity";
    private Context mContext;
    private TextView selectProvince;
    private TextView addCityTitle;

    //省列表
    private List<Province> provinceList;

    public static ShowProvinceActivity mShowProvinceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_province);
        mContext = ShowProvinceActivity.this;
        mShowProvinceActivity = ShowProvinceActivity.this;
        findView();
        DebugLog.d(TAG, "onCreate() complete");
    }

    /**
     * 给各个组件赋值
     */
    private void findView() {
        addCityTitle = (TextView) findViewById(R.id.addcity_title);
        DebugLog.d(TAG, "findView() complete");
    }

    /**
     * 给界面的显示填装数据
     */
    private void initView() {
        addCityTitle.setText(R.string.tv_title_select_province);
        GridView addCityGrid = (GridView) findViewById(R.id.addcity_gridview);

        GridAddCityAdapter mGridAddCityAdapter = new GridAddCityAdapter(mContext);

        addCityGrid.setAdapter(mGridAddCityAdapter);

        addCityGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (UtilityClass.isNetWorkAvailable(mContext)) {
                    selectProvince = (TextView) view.findViewById(R.id.citytext);
                    //获取选择的省份的名字
                    String selectProvinceName = selectProvince.getText().toString();

                    //查找数据库中所有的省份
                    provinceList = DataSupport.findAll(Province.class);

                    //获取省份的code id
                    for (Province mProvince : provinceList) {
                        if (selectProvinceName.equals(mProvince.getProvinceName())) {
                            UtilityClass.provinceCode = mProvince.getProvinceCode();
                        }
                    }

                    //通过Intent启动ShowCityActivity
                    Intent startShowCityActivityIntent = new Intent(mContext, ShowCitiesActivity.class);
                    DebugLog.d(TAG, "select the province name is " + selectProvinceName + ", province code is " + UtilityClass.provinceCode);
                    startShowCityActivityIntent.putExtra("provinceName", selectProvinceName);
                    startShowCityActivityIntent.putExtra("provinceCode", UtilityClass.provinceCode);
                    startActivity(startShowCityActivityIntent);

                    //将level设置成LEVEL_CITY
                    UtilityClass.currentLevel = UtilityClass.LEVEL_CITY;
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
        UtilityClass.currentLevel = UtilityClass.LEVEL_PROVINCE;
        if (UtilityClass.isNetWorkAvailable(mContext)) {
            getProvincesList();
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
        if (mShowProvinceActivity != null) {
            mShowProvinceActivity = null;
        }
    }

    /**
     * handle处理界面数据的更新
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UtilityClass.MESSAGE_SUCCESS:
                    DebugLog.d(TAG, "get provinces successfully");
                    //移除消息
                    removeMessages(UtilityClass.MESSAGE_SUCCESS);

                    //关闭动态ProgressDialog
                    UtilityClass.closeProgressDialog();

                    //填装数据
                    initView();
                    break;
                case UtilityClass.MESSAGE_FAILED:
                    DebugLog.e(TAG, "get provinces list failed");

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
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void getProvincesList() {
        //省列表
        List<Province> provinceList;

        provinceList = DataSupport.findAll(Province.class);
        int i = 0;
        DebugLog.d(TAG, "provinceList size = " + provinceList.size());

        if (provinceList.size() > 33 && provinceList.size() < 35) {

            GridAddCityAdapter.provinceName = new String[provinceList.size()];
            for (Province province : provinceList) {
                GridAddCityAdapter.provinceName[i] = province.getProvinceName();
                i++;
            }

            provinceList.clear();

            Message msg = new Message();
            msg.what = UtilityClass.MESSAGE_SUCCESS;
            mHandler.sendMessage(msg);
        } else {
            String address = "http://guolin.tech/api/china";
            DebugLog.d(TAG, "address = " + address);

            DataSupport.deleteAll(Province.class);
            getProvincesByInternet(address);
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     *
     * @param address 获取省数据的地址
     */
    private void getProvincesByInternet(String address) {
        UtilityClass.showProgressDialog(mContext, getString(R.string.progress_dialog_content_get_province));

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = UtilityClass.handleProvinceResponse(responseText);

                DebugLog.d(TAG, "response result = " + result);
                if (result) {
                    DebugLog.d(TAG, "type is province call getProvincesList()");
                    getProvincesList();
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
