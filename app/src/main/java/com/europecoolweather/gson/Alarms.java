package com.europecoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by guangkai on 2017/8/8.
 *
 * 需求：
 *      Gson解析警告信息，将获取到的警告信息存储下来
 */

public class Alarms {

    @SerializedName("level")
    public String level;

    @SerializedName("stat")
    public String stat;

    @SerializedName("title")
    public String title;

    @SerializedName("txt")
    public String info;
}
