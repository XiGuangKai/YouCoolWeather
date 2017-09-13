package com.europecoolweather.gson;

/**
 * Created by GuangKai on 2017/8/2.
 *
 * 需求：
 *      Gson解析AQI的数据
 */

public class AQI {
    public AQICity city;

    public class AQICity {

        public String aqi;

        public String pm25;

        public String qlty;

    }
}
