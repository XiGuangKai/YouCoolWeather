package com.europecoolweather.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

//import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
//import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
//import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
//import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 * Created by GuangKai on 2017/8/15.
 * <p>
 * 需求：
 * Image加载
 */

public class CHImageView extends ImageView {
    /**
     * 用于记录默认下载中状态的图片
     */
    private int downLoadingImageId = 0;
    private int downLoadingImagefailureId = 0;
    // 图片是否加载成功
    private boolean loadSuccess = false;

    /**
     * 不设置将使用默认图片 设置下载中，与加载失败的图片,
     *
     * @param downlding 加载?? * @param failureId 加载失败
     */
    public void setDefultDownLoadAndFailureImage(int downlding, int failureId) {
        downLoadingImageId = downlding;
        downLoadingImagefailureId = failureId;
    }

    public CHImageView(Context context) {
        super(context);
    }

    public CHImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CHImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 对外接口，用于调用ImageView的异步下载图片功?? *
     *
     * @param url 图片的URL
     */
    public void loadImage(String url) {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(downLoadingImageId)
                .showImageForEmptyUri(downLoadingImagefailureId)
                .cacheInMemory().cacheOnDisc()
                .showImageOnFail(downLoadingImagefailureId)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(url, this, options);
//
//		ImageLoader.getInstance().loadImage(url, options,
//				new ImageLoadingListener() {
//
//					@Override
//					public void onLoadingStarted(String arg0, View arg1) {
//						loadSuccess = false;
//						setImageResource(downLoadingImageId);
//					}
//
//					@Override
//					public void onLoadingFailed(String arg0, View arg1,
//							FailReason arg2) {
//						loadSuccess = false;
//						setImageResource(downLoadingImagefailureId);
//					}
//
//					@Override
//					public void onLoadingComplete(String arg0, View arg1,
//							Bitmap arg2) {
//
//						if (getTag() == null || arg0.equals(getTag())) {
//							loadSuccess = true;
//							setImageBitmap(arg2);
//						}
//					}
//
//					@Override
//					public void onLoadingCancelled(String arg0, View arg1) {
//						loadSuccess = false;
//						setImageResource(downLoadingImagefailureId);
//					}
//				});
    }

    public boolean isLoadSuccess() {
        return loadSuccess;
    }
}
