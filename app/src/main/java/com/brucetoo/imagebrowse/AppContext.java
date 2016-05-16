package com.brucetoo.imagebrowse;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by Bruce Too
 * On 9/28/15.
 * At 16:16
 */
public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //config imageloader
        ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory();
        configBuilder.tasksProcessingOrder(QueueProcessingType.LIFO);
//		.writeDebugLogs(); // Remove for release app

        ImageLoaderConfiguration config = configBuilder.build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}
