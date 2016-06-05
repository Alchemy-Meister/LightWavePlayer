package com.naman14.timber.permissions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.naman14.timber.R;

/**
 * Created by meister on 4/3/16.
 */
public class RuntimePermissionsHelper {

    public static void goToSettings(Context context) {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(myAppSettings);
    }

    public static void showMessageOKCancel(String message, final Context context) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RuntimePermissionsHelper.goToSettings(context);
                    }
                })
                .create()
                .show();
    }

    public static boolean hasPermissions(Context context, @NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                    context, permission))
                return false;
        return true;
    }
}
