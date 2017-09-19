package com.europecoolweather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.europecoolweather.util.UtilityClass;

/**
 * 创建数据库YouCoolDatabase，并且在数据库YouCoolDatabase中增加表YouCoolWeather
 *
 * @author GuangKai
 * @version 版本1.0
 */

public class SQLiteCityManager extends SQLiteOpenHelper {
    public SQLiteCityManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + UtilityClass.YOU_COOL_WEATHER + "(_id integer primary key autoincrement, city_name varchar(20), "
                + "image_url varchar(20), weather varchar(20), temperature varchar(20), weather_description varchar(1000));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists person;");
    }
}
