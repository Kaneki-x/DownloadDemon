package me.kaneki.download;

import android.app.Application;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/12
 * @Email kaneki.cjb@alibaba-inc.com
 */
public class DownloadApplication extends Application {

    private static DownloadApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static DownloadApplication getInstance() {
        return instance;
    }
}
