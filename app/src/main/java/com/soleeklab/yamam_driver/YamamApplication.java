package com.soleeklab.yamam_driver;

import android.app.Application;


public class YamamApplication extends Application {

    private static YamamApplication instance;
    private Thread.UncaughtExceptionHandler defaultUEH;

    public static YamamApplication get() {
        if (instance == null)
            throw new IllegalStateException("Application instance has not been initialized!");
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }



}
