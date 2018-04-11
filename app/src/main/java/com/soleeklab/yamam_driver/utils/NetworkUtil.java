package com.soleeklab.yamam_driver.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.soleeklab.yamam_driver.YamamApplication;


public class NetworkUtil {


    private static ConnectivityManager connectivityManager() {
        return (ConnectivityManager) YamamApplication.get().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) YamamApplication.get().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

}
