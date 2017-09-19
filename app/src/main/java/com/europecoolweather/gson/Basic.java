package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by guangkai on 2017/8/2.
 * <p>
 * 需求：
 * Gson解析basic信息
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }
}
