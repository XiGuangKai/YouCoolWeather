package com.europecoolweather.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.europecoolweather.R;
import com.europecoolweather.activity.AboutMeActivity;
import com.europecoolweather.activity.CityManagerActivity;
import com.europecoolweather.activity.WeatherShowActivity;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.UtilityClass;

/**
 * DrawerLayout的抽屉Fragment
 *
 * @author GuangKai
 * @version 版本1.0
 */

public class MainFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "MainFragment";

    //城市管理
    private Button btnCityManager;

    //关于作者
    private Button btnAboutAuthor;

    //显示默认城市
    private Button btnShowDefaultCity;

    //退出APP
    private Button btnExitApp;

    //构造方法
    public MainFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        btnCityManager = (Button) view.findViewById(R.id.btn_city_manager);
        btnAboutAuthor = (Button) view.findViewById(R.id.btn_about_author);
        btnShowDefaultCity = (Button) view.findViewById(R.id.btn_show_default_city);
        btnExitApp = (Button) view.findViewById(R.id.btn_exit_app);
        DebugLog.d(TAG, "onCreateView() finished");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnCityManager.setOnClickListener(this);
        btnAboutAuthor.setOnClickListener(this);
        btnShowDefaultCity.setOnClickListener(this);
        btnExitApp.setOnClickListener(this);
        DebugLog.d(TAG, "onActivityCreated() finished");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_city_manager:
                DebugLog.d(TAG, "click city manager button");
                //关闭drawer
                closeLeftDrawerLayout();

                //启动activity增加城市
                Intent addCityIntent = new Intent(getActivity(), CityManagerActivity.class);
                startActivity(addCityIntent);
                break;
            case R.id.btn_about_author:
                DebugLog.d(TAG, "click exit about me button");
                //关闭drawer
                closeLeftDrawerLayout();
                Intent aboutMeIntent = new Intent(getActivity(), AboutMeActivity.class);
                startActivity(aboutMeIntent);
                break;
            case R.id.btn_show_default_city:
                DebugLog.d(TAG, "click show default city");
                if (!(UtilityClass.getCityName(getActivity(), UtilityClass.DEFAULT_CITY_TYPE) == null)) {
                    DebugLog.d(TAG, "show default city");
                    //将显示级别设置为默认
                    UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_DEFAULT_CITY;

                    //启动主界面
                    Intent intent = new Intent(getActivity(), WeatherShowActivity.class);
                    startActivity(intent);

                    //关闭drawer
                    closeLeftDrawerLayout();
                } else {
                    DebugLog.e(TAG, "no default city");
                    UtilityClass.showToast(getActivity(), getString(R.string.toast_select_default_city));
                }
                break;
            case R.id.btn_exit_app:
                //关闭drawer
                closeLeftDrawerLayout();
                DebugLog.d(TAG, "click exit app button");
                exitAppDialog();
                break;
            default:
                break;
        }
    }

    /**
     * 弹出Dialog，请用户确认是否退出APP
     */
    private void exitAppDialog() {
        DebugLog.d(TAG, "Create the dialog to confirm that whether to exit APP");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.ThemeAppCompatLightDialogAlertSelf);
        alertDialog.setIcon(R.drawable.cool_weather_icon);
        alertDialog.setTitle(R.string.dialog_title_exit_app);
        alertDialog.setMessage(R.string.dialog_message_exit_app);

        alertDialog.setPositiveButton(R.string.dialog_positive_button_exit_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DebugLog.d(TAG, "confirm to exit APP");
                getActivity().finish();
            }
        });

        alertDialog.setNegativeButton(R.string.dialog_negative_button_exit_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DebugLog.d(TAG, "cancel to exit APP");
            }
        });

        alertDialog.setCancelable(true);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    /**
     * 当点击了某一个选项之后关闭抽屉
     */
    public void closeLeftDrawerLayout() {
        WeatherShowActivity mWeatherShowActivity = (WeatherShowActivity) getActivity();
        DrawerLayout drawerLayout = mWeatherShowActivity.drawerLayout;
        View leftDrawer = mWeatherShowActivity.leftDrawer;
        DebugLog.d(TAG, "is Drawer Open left = " + drawerLayout.isDrawerOpen(leftDrawer));
        if (drawerLayout.isDrawerOpen(leftDrawer)) {
            drawerLayout.closeDrawer(leftDrawer);
        } else {
            drawerLayout.openDrawer(leftDrawer);
        }
    }
}
