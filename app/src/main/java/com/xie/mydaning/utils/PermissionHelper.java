package com.xie.mydaning.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * 权限和系统设置辅助类
 * 用于检查和请求闹钟功能所需的各种权限
 */
public class PermissionHelper {
    private static final String TAG = "PermissionHelper";

    /**
     * 检查所有闹钟功能所需的权限
     */
    public static boolean checkAllPermissions(Context context) {
        boolean allGranted = true;

        // 检查通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "通知权限未授予");
                allGranted = false;
            }
        }

        // 检查精确闹钟权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "精确闹钟权限未授予");
                allGranted = false;
            }
        }

        // 检查电池优化白名单
        if (!isIgnoringBatteryOptimizations(context)) {
            Log.w(TAG, "未在电池优化白名单中");
            allGranted = false;
        }

        return allGranted;
    }

    /**
     * 检查是否在电池优化白名单中
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true; // Android 6.0 以下不需要
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }

        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    /**
     * 请求忽略电池优化（打开设置页面）
     */
    public static void requestIgnoreBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (isIgnoringBatteryOptimizations(context)) {
            Log.d(TAG, "已在电池优化白名单中");
            return;
        }

        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // 如果系统不支持，尝试打开电池优化设置页面
            Log.e(TAG, "无法打开电池优化设置", e);
            try {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "无法打开电池优化设置页面", e2);
            }
        }
    }

    /**
     * 请求精确闹钟权限（打开设置页面）
     */
    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        if (alarmManager.canScheduleExactAlarms()) {
            Log.d(TAG, "精确闹钟权限已授予");
            return;
        }

        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "无法打开精确闹钟设置", e);
            // 尝试打开应用设置页面
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "无法打开应用设置", e2);
            }
        }
    }

    /**
     * 获取权限状态描述（用于调试）
     */
    public static String getPermissionStatus(Context context) {
        StringBuilder status = new StringBuilder();
        status.append("权限状态:\n");

        // 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            status.append("通知权限: ").append(granted ? "已授予" : "未授予").append("\n");
        }

        // 精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean granted = alarmManager != null && alarmManager.canScheduleExactAlarms();
            status.append("精确闹钟权限: ").append(granted ? "已授予" : "未授予").append("\n");
        }

        // 电池优化
        boolean ignoring = isIgnoringBatteryOptimizations(context);
        status.append("电池优化白名单: ").append(ignoring ? "已加入" : "未加入").append("\n");

        return status.toString();
    }
}

