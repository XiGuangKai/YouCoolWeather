package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * gson方式解析Now信息
 *
 * @author GuangKai
 * @version 版本1.0
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    @SerializedName("wind")
    public Wind wind;

    public class More {

        @SerializedName("txt")
        public String info;

    }

    public class Wind {
        @SerializedName("dir")
        public String WindDir;

        @SerializedName("sc")
        public String WindSc;
    }
}
