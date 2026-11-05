package com.github.shingyx.lockwidget;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class LockAdminReceiver extends DeviceAdminReceiver {
    public static final String ACTION_LOCK = "com.github.shingyx.lockwidget.LOCK";

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), LockAdminReceiver.class);
    }

    public static boolean isEnabled(Context context) {
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        return dpm != null && dpm.isAdminActive(getComponentName(context));
    }

    public static boolean lockScreen(Context context) {
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        if (dpm != null && dpm.isAdminActive(getComponentName(context))) {
            dpm.lockNow();
            return true;
        }
        return false;
    }

    public static void revokeAdmin(Context context) {
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        ComponentName componentName = getComponentName(context);
        if (dpm != null && dpm.isAdminActive(componentName)) {
            dpm.removeActiveAdmin(componentName);
        }
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}
