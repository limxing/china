package com.limxing.china;

import android.app.Application;

import com.limxing.library.Crash.CrashHandler;

/**
 * Created by limxing on 16/4/13.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

    }
}
