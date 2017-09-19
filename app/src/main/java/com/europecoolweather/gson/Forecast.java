package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by guangkai on 2017/8/2.
 * <p>
 * 需求：
 * Gson解析Forecast信息
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }
}
