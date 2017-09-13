package com.europecoolweather.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.europecoolweather.R;
import com.europecoolweather.activity.WeatherShowActivity;
import com.europecoolweather.database.CityManagerEntity;
import com.europecoolweather.database.SQLiteCityManager;
import com.europecoolweather.gson.Weather;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.HttpUtil;
import com.europecoolweather.util.UtilityClass;
import com.europecoolweather.view.CHImageView;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.europecoolweather.activity.CityManagerActivity.mCityManagerEntityList;

public class CityManagerAdapter extends BaseAdapter {

    private static final String TAG = "CityManagerAdapter";

    private Button grid_set_normal;
    private Button mClickViewButton;
    private LayoutInflater mInflater;
    private List<CityManagerEntity> cityManager;
    private Context mContext;
    private CHImageView grid_weatherimage;
    private TextView grid_city;
    private TextView grid_temp;
    private TextView grid_weather;
    private TextView city_item_layout;
    private TextView grid_item_delete;

    public CityManagerAdapter(Context context, List<CityManagerEntity> citymanager) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.cityManager = citymanager;
    }

    @Override
    public int getCount() {
        return cityManager == null ? 0 : cityManager.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_citymanager,parent, false);
        }
        grid_city = (TextView) convertView.findViewById(R.id.grid_city);
        grid_temp = (TextView) convertView.findViewById(R.id.grid_temp);
        grid_weatherimage = (CHImageView) convertView.findViewById(R.id.grid_weatherimage);
        grid_weather = (TextView) convertView.findViewById(R.id.grid_weather);
        grid_set_normal = (Button) convertView.findViewById(R.id.grid_set_normal);
        city_item_layout = (TextView) convertView.findViewById(R.id.city_item_layout);
        grid_item_delete = (TextView) convertView.findViewById(R.id.grid_item_delete);

        grid_item_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DebugLog.d(TAG,"Create the dialog to confirm delete city");
                final String cityName = mCityManagerEntityList.get(position).getCityName();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext,R.style.ThemeAppCompatLightDialogAlertSelf);
                alertDialog.setIcon(R.drawable.cool_weather_icon);
                alertDialog.setTitle(R.string.dialog_delete_city_title);
                alertDialog.setMessage(R.string.dialog_confirm_delete_city_content + cityName);

                alertDialog.setPositiveButton(R.string.dialog_positive_button_exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DebugLog.d(TAG,"confirm delete city " + cityName);
                        SQLiteCityManager sqlite = new SQLiteCityManager(mContext,UtilityClass.YOU_COOL_DATABASE, null, 1);
                        SQLiteDatabase db = sqlite.getWritableDatabase();

                        //删除数据库中所存在的数据记录
                        int index = db.delete(UtilityClass.YOU_COOL_WEATHER, "city_name = ?", new String []{cityName});
                        if(index == 0){
                            UtilityClass.showToast(mContext,mContext.getString(R.string.dialog_delete_city_failed));
                        }

                        String mDefaultCityName = UtilityClass.getCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);
                        String mChooseCityName = UtilityClass.getCityName(mContext,UtilityClass.CHOOSE_CITY_TYPE);

                        if (mDefaultCityName != null) {
                            //如果删除的数据是默认城市的数据，则将com.europecoolweather_preferences中默认的城市数据清除
                            DebugLog.d(TAG, "default weather city name = " + mDefaultCityName + ", delete city name = " + cityName);
                            if (mDefaultCityName.equals(cityName)) {
                                UtilityClass.removeCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);
                                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_CHOOSE_CITY;
                            }
                        }

                        if (mChooseCityName != null){
                            //如果删除的数据是之前选择的城市的数据，则将com.europecoolweather_preferences中选择的城市数据清除
                            DebugLog.d(TAG, "choose weather city name = " + mChooseCityName + ", delete city name = " + cityName);
                            if (mChooseCityName.equals(cityName)) {
                                UtilityClass.removeCityName(mContext,UtilityClass.CHOOSE_CITY_TYPE);
                                UtilityClass.SHOW_CITY_WEATHER_INFO = UtilityClass.SHOW_DEFAULT_CITY;
                            }
                        }

                        for(int i = 0; i < cityManager.size(); i++){
                            if(cityName.equals(cityManager.get(i).getCityName())){
                                cityManager.remove(i);
                            }
                        }

                        notifyDataSetChanged();
                    }
                });

                alertDialog.setNegativeButton(R.string.dialog_negative_button_exit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DebugLog.d(TAG,"cancel delete city = " + cityName);
                    }
                });

                alertDialog.setCancelable(true);
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });

        grid_set_normal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //获取点击的对应的View上Button，此处的获取很关键
                mClickViewButton = (Button) v.findViewById(R.id.grid_set_normal);
                String btnNormalStatus = mClickViewButton.getText().toString();
                String cityName = mCityManagerEntityList.get(position).getCityName();
                DebugLog.d(TAG,"status is " + btnNormalStatus + ", cityName = " + cityName);

                if (btnNormalStatus.equals(mContext.getString(R.string.btn_set_normal))){

                    DebugLog.d(TAG,"change button text is normal and save city name");
                    UtilityClass.setCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE,cityName);

                    mClickViewButton.setText(R.string.btn_normal);
                    UtilityClass.showToast(mContext,mContext.getString(R.string.toast_set_default_success));
                }else if (btnNormalStatus.equals(mContext.getString(R.string.btn_normal))){

                    DebugLog.d(TAG,"remove weather key and change button text is setNormal");
                    UtilityClass.removeCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);

                    mClickViewButton.setText(R.string.btn_set_normal);
                    UtilityClass.showToast(mContext,mContext.getString(R.string.toast_cancel_set_default_success));
                }else {
                    UtilityClass.showToast(mContext,mContext.getString(R.string.toast_unrecognized_state));
                }

                //更新界面
                notifyDataSetChanged();
            }
        });

        if (position == cityManager.size() - 1) {
            grid_city.setText("");
            grid_temp.setText("");
            grid_weather.setText("");
            grid_set_normal.setText("");
            grid_weatherimage.setImageDrawable(null);
            grid_set_normal.setBackgroundColor(Color.TRANSPARENT);
            city_item_layout.setBackgroundResource(R.drawable.cityadd_bg);
            grid_item_delete.setText("");
        } else {
            grid_item_delete.setText("×");
            grid_city.setText(cityManager.get(position).getCityName());
            grid_temp.setText(cityManager.get(position).getTemperature());

            grid_weatherimage.setImageBitmap(UtilityClass.stringToBitmap(cityManager.get(position).getWeatherImage()));
            grid_weather.setText(cityManager.get(position).getWeather());
            grid_set_normal.setBackgroundResource(R.drawable.citym_normal_bg);

            DebugLog.d(TAG, "all button text show setDefault");
            grid_set_normal.setText(R.string.btn_set_normal);


            String mDefaultCityName = UtilityClass.getCityName(mContext,UtilityClass.DEFAULT_CITY_TYPE);

            //将默认的城市的Button显示为“默认”
            if (mDefaultCityName != null){
                DebugLog.d(TAG,"default city name = " + mDefaultCityName +", get city name = " + cityManager.get(position).getCityName());
                if (mDefaultCityName.equals(cityManager.get(position).getCityName())) {
                    DebugLog.d(TAG, "button text show default");
                    grid_set_normal.setText(R.string.btn_normal);
                }
            }
            city_item_layout.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    public void setCityManager(List<CityManagerEntity> cityManager) {
        this.cityManager = cityManager;
    }
}
