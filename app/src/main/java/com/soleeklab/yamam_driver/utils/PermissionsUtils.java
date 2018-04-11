package com.soleeklab.yamam_driver.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static android.content.Context.LOCATION_SERVICE;


/**
 * Created by Saif on 1/25/2017.
 */

public class PermissionsUtils {

    public static boolean requestPermission(String permission, Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        2);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    public static void turnGPSOn(final Activity activity) {
        LocationManager service = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    activity);
            alertDialogBuilder
                    .setMessage("GPS is disabled in your device. Enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Enable GPS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
/** Here it's leading to GPS setting options*/
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    activity.startActivity(callGPSSettingIntent);
                                }
                            });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }
    public static void showNoConnectionDialog(final Context ctx1) {

        if(NetworkUtil.isConnected()) {

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx1);
            builder.setCancelable(false);
            builder.setTitle("No Internet");
            builder.setMessage("Internet is required. Please Retry.");

            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    showNoConnectionDialog(ctx1);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}
