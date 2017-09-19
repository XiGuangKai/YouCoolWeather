package com.europecoolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by guangkai on 2017/8/2.
 * 需求：
 * 使用OkHttp和服务器进行交互
 */

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        DebugLog.d(TAG, "OkHttp request to get the internet data");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
