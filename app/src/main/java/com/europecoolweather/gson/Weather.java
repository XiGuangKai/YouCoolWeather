package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guangkai on 2017/8/2.
 *
 * 需求：
 *      Gson解析Weather信息
 */

public class Weather {

    public Alarms alarms;

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
