package com.app.legend.lan_play_android.utils;

import android.app.Application;
import android.content.Context;

public class LanApp extends Application {

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
