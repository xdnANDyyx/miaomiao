package com.xie.mydaning.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.xie.mydaning.receiver.WaterReminderReceiver;
import com.xie.mydaning.receiver.PeriodReminderReceiver;

/**
 * 使用 AlarmManager 安排/取消强提醒。
 */
public class ReminderScheduler {
    private static final String PREFS_NAME = "period_settings";
    private static final String KEY_PERIOD_REMINDER_TIME = "period_reminder_time";
    private static final long ONE_WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L;

    /**
     * 安排喝水提醒（分钟级）- 使用精确闹钟，即使应用退出也能提醒
     * @param context 上下文
     * @param intervalMinutes 间隔分钟数（1-120分钟）
     */
    public static void scheduleWaterReminder(Context context, int intervalMinutes) {
        if (intervalMinutes <= 0) intervalMinutes = 1;
        if (intervalMinutes < 1) intervalMinutes = 1; // 最小1分钟

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                new Intent(context, WaterReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long intervalMillis = intervalMinutes * 60L * 1000L;
        long triggerAt = System.currentTimeMillis() + intervalMillis;

        alarmManager.cancel(pendingIntent);
        
        // 保存间隔设置，用于下次自动安排
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("water_interval_minutes", intervalMinutes).apply();
        
        // 使用系统闹钟 API (setAlarmClock)，这是最可靠的方法
        // 即使应用被完全关闭、杀死，系统也会在指定时间触发
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 创建 AlarmClockInfo，用于在系统状态栏和锁屏显示
                android.app.AlarmManager.AlarmClockInfo alarmClockInfo = 
                    new android.app.AlarmManager.AlarmClockInfo(triggerAt, pendingIntent);
                
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                Log.d("ReminderScheduler", "✅ 使用系统闹钟API设置喝水提醒: " + intervalMinutes + "分钟后，触发时间: " + new java.util.Date(triggerAt));
            } else {
                // Android 5.0 以下使用传统方法
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                Log.d("ReminderScheduler", "安排喝水提醒: " + intervalMinutes + "分钟后，使用普通闹钟(Android < 5.1)，触发时间: " + new java.util.Date(triggerAt));
            }
        } catch (Exception e) {
            Log.e("ReminderScheduler", "设置系统闹钟失败，尝试降级方案", e);
            // 降级方案：使用精确闹钟
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用精确闹钟，触发时间: " + new java.util.Date(triggerAt));
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用普通闹钟，触发时间: " + new java.util.Date(triggerAt));
                }
            } catch (Exception e2) {
                Log.e("ReminderScheduler", "所有闹钟设置方法都失败", e2);
            }
        }
    }

    public static void cancelWaterReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                new Intent(context, WaterReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * 提前一周提醒预计经期。
     */
    public static void schedulePeriodReminder(Context context, java.util.Date nextPeriodDate) {
        if (nextPeriodDate == null) return;

        long target = com.xie.mydaning.utils.DateUtils.getStartOfDay(nextPeriodDate).getTime() - ONE_WEEK_MILLIS;
        long now = System.currentTimeMillis();
        if (target < now + 5_000) {
            target = now + 5_000; // 太近则尽快提醒
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2002,
                new Intent(context, PeriodReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        
        // 使用系统闹钟 API (setAlarmClock)，这是最可靠的方法
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 创建 AlarmClockInfo，用于在系统状态栏和锁屏显示
                android.app.AlarmManager.AlarmClockInfo alarmClockInfo = 
                    new android.app.AlarmManager.AlarmClockInfo(target, pendingIntent);
                
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                Log.d("ReminderScheduler", "✅ 使用系统闹钟API设置经期提醒，触发时间: " + new java.util.Date(target));
            } else {
                // Android 5.0 以下使用传统方法
                alarmManager.set(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                Log.d("ReminderScheduler", "设置经期提醒(Android < 5.1)，触发时间: " + new java.util.Date(target));
            }
        } catch (Exception e) {
            Log.e("ReminderScheduler", "设置系统闹钟失败，尝试降级方案", e);
            // 降级方案：使用精确闹钟
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用精确闹钟，触发时间: " + new java.util.Date(target));
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用普通闹钟，触发时间: " + new java.util.Date(target));
                }
            } catch (Exception e2) {
                Log.e("ReminderScheduler", "所有闹钟设置方法都失败", e2);
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_PERIOD_REMINDER_TIME, target).apply();
    }

    public static void cancelPeriodReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2002,
                new Intent(context, PeriodReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PERIOD_REMINDER_TIME).apply();
    }

    /**
     * 设备重启后恢复经期提醒；依赖之前保存的 trigger 时间戳。
     */
    public static void restorePeriodReminderIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long target = prefs.getLong(KEY_PERIOD_REMINDER_TIME, -1L);
        if (target <= 0) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2002,
                new Intent(context, PeriodReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        
        // 使用系统闹钟 API (setAlarmClock)，这是最可靠的方法
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 创建 AlarmClockInfo，用于在系统状态栏和锁屏显示
                android.app.AlarmManager.AlarmClockInfo alarmClockInfo = 
                    new android.app.AlarmManager.AlarmClockInfo(target, pendingIntent);
                
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                Log.d("ReminderScheduler", "✅ 使用系统闹钟API恢复经期提醒，触发时间: " + new java.util.Date(target));
            } else {
                // Android 5.0 以下使用传统方法
                alarmManager.set(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                Log.d("ReminderScheduler", "恢复经期提醒(Android < 5.1)，触发时间: " + new java.util.Date(target));
            }
        } catch (Exception e) {
            Log.e("ReminderScheduler", "设置系统闹钟失败，尝试降级方案", e);
            // 降级方案：使用精确闹钟
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用精确闹钟，触发时间: " + new java.util.Date(target));
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, target, pendingIntent);
                    Log.d("ReminderScheduler", "降级使用普通闹钟，触发时间: " + new java.util.Date(target));
                }
            } catch (Exception e2) {
                Log.e("ReminderScheduler", "所有闹钟设置方法都失败", e2);
            }
        }
    }
}

