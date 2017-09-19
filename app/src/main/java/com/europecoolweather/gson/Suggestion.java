package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GuangKai on 2017/8/2.
 * <p>
 * 需求：
 * Gson解析Suggestion信息
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("uv")
    public UVIntensity uvIntensity;

    public Sport sport;

    public class Comfort {

        @SerializedName("txt")
        public String info;

    }

    public class CarWash {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }

    public class UVIntensity {
        @SerializedName("brf")
        public String brf;
        @SerializedName("txt")
        public String info;
    }
}
