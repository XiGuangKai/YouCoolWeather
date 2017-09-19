package com.europecoolweather.util;

import android.util.Log;

/**
 * Created by guangkai on 2017/8/2.
 * 需求：
 * 封装Android原生的log输出系统接口，方便以后app调试
 */

public class DebugLog {
    private static final String APPTAG = "EuropeCoolWeather";

    public static void d(String TAG, String str) {
        Log.d(APPTAG, TAG + " " + str);
    }

    public static void i(String TAG, String str) {
        Log.i(APPTAG, TAG + " " + str);
    }

    public static void e(String TAG, String str) {
        Log.e(APPTAG, TAG + " " + str);
    }

    public static void v(String TAG, String str) {
        Log.v(APPTAG, TAG + " " + str);
    }
}
