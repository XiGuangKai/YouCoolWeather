package com.europecoolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.europecoolweather.R;
import com.europecoolweather.util.DebugLog;
import com.europecoolweather.util.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 实现分享APP信息的功能，目前只有微信的朋友圈可以实现文字和图片一起分享，但是其他的分享方式都只能是图片
 * @author GuangKai
 * @version 版本1.0
 */

public class ShareAppActivity{
    private static final String TAG = "ShareApp";

    private static final String DEFAULT_FILE_PATH =  "/storage/emulated/0/pictures/";

    public static void shareApp(Activity mActivity)
    {
        if (UtilityClass.isNetWorkAvailable(mActivity)) {
            String shareAppContent = mActivity.getString(R.string.share_app_content);

            DebugLog.d(TAG, "copy cool_weather_icon image to " + DEFAULT_FILE_PATH);
            Bitmap mBitmap = BitmapFactory.decodeResource(mActivity.getResources(), com.europecoolweather.R.drawable.cool_weather_icon);

            String imgName = mActivity.getString(R.string.app_name)+".png";
            if (saveImgToDisk(imgName, mBitmap)) {

                Intent localIntent = new Intent(Intent.ACTION_SEND);
                localIntent.putExtra(Intent.EXTRA_TEXT, shareAppContent);
                localIntent.putExtra("sms_body", shareAppContent);
                localIntent.putExtra("Kdescription", shareAppContent);
                Uri localUri1 = Uri.fromFile(new File(DEFAULT_FILE_PATH + imgName));
                localIntent.putExtra(Intent.EXTRA_STREAM, localUri1);
                localIntent.setType("image/*");
                mActivity.startActivity(Intent.createChooser(localIntent, ""));
                DebugLog.d(TAG, "share info success");
            } else {
                DebugLog.d(TAG, "share image not exists");
                UtilityClass.showToast(mActivity,mActivity.getString(R.string.share_app_failed));
            }
        }else{
            DebugLog.e(TAG,"network is not useful");
            UtilityClass.showToast(mActivity,mActivity.getString(R.string.toast_internet_no_useful_for_share_app));
        }
    }

    /**
     * 保存图片到本地 第一个参数是图片名称 第二个参数为需要保存的bitmap
     * @param imgName 要保存的Img的名字
     * @param bitmap 要保存的bitmap
     * @return 保存成功返回true，其他情况返回false
     * */
    private static boolean saveImgToDisk(String imgName, Bitmap bitmap) {
        File file = new File(DEFAULT_FILE_PATH, imgName);

        if(isFileExists(file.getPath())) {
            DebugLog.d(TAG,"share.png has exists");
            return true;
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            DebugLog.d(TAG,"copy share.png success");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        DebugLog.d(TAG,"copy share.png failed");
        return false;
    }

    /**
     * 判断文件路径是否已经存在
     * @param filePath 文件路径
     * @return 文件存在返回true,其他情况返回false
     * */
    private static boolean isFileExists(String filePath) {
        try {
            File file = new File( filePath );
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
