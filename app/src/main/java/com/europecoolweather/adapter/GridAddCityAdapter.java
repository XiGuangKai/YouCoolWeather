package com.europecoolweather.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.europecoolweather.R;
import com.europecoolweather.database.SQLiteCityManager;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.UtilityClass;

public class GridAddCityAdapter extends BaseAdapter {

    private final static String TAG = "GridAddCityAdapter";

    private LayoutInflater mInflater;
    public static String[] provinceName;
    public static String[] citiesName;
    public static String[] countiesName;

    private SparseBooleanArray sba = new SparseBooleanArray();

    public GridAddCityAdapter(Context context) {

        this.mInflater = LayoutInflater.from(context);

        if (UtilityClass.currentLevel == UtilityClass.LEVEL_COUNTY) {
            SQLiteCityManager sqlite = new SQLiteCityManager(context, UtilityClass.YOU_COOL_DATABASE, null, 1);
            SQLiteDatabase db = sqlite.getReadableDatabase();
            Cursor cursor = db.query(UtilityClass.YOU_COOL_WEATHER, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String nowCityName = cursor.getString(cursor.getColumnIndex("city_name"));
                DebugLog.i(TAG, "nowCityName is " + nowCityName);
                for (int i = 0; i < countiesName.length; i++) {
                    if (nowCityName.equals(countiesName[i])) {
                        sba.put(i, true);
                    }
                }
            }
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        if (UtilityClass.currentLevel == UtilityClass.LEVEL_PROVINCE) {
            if (provinceName != null) {
                DebugLog.d(TAG, "provinceName length = " + provinceName.length);
                return provinceName == null ? 0 : provinceName.length;
            }
        } else if (UtilityClass.currentLevel == UtilityClass.LEVEL_CITY) {
            if (citiesName != null) {
                DebugLog.d(TAG, "citiesName length = " + citiesName.length);
                return citiesName == null ? 0 : citiesName.length;
            }
        } else if (UtilityClass.currentLevel == UtilityClass.LEVEL_COUNTY) {
            if (countiesName != null) {
                DebugLog.d(TAG, "countiesName length = " + countiesName.length);
                return countiesName == null ? 0 : countiesName.length;
            }
        }

        return 0;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        DebugLog.d(TAG, "position = " + position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_addcity,
                    parent, false);// 此处需要加上第二个参数parent，否则item中的设置无效。如item高度设置。
        }
        TextView citytext = (TextView) convertView.findViewById(R.id.citytext);
        if (UtilityClass.currentLevel == UtilityClass.LEVEL_PROVINCE) {
            citytext.setText(provinceName[position]);
        } else if (UtilityClass.currentLevel == UtilityClass.LEVEL_CITY) {
            citytext.setText(citiesName[position]);
        } else if (UtilityClass.currentLevel == UtilityClass.LEVEL_COUNTY) {
            citytext.setText(countiesName[position]);
            // 查询数据库，数据库中有该城市则设置勾选
            if (sba.get(position)) {
                citytext.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.city_checkbox_selected, 0);
            }
        }

        return convertView;
    }
}
