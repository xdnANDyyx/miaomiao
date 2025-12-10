package com.xie.mydaning.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.xie.mydaning.utils.ReminderScheduler;

/**
 * 设备重启后恢复提醒。
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "period_settings";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (prefs.getBoolean("water_reminder", true)) {
            // 喝水提醒改为分钟级
            int intervalMinutes = prefs.getInt("water_interval_minutes", 1);
            if (intervalMinutes < 1) intervalMinutes = 1;
            ReminderScheduler.scheduleWaterReminder(context, intervalMinutes);
        }

        if (prefs.getBoolean("period_reminder", true)) {
            ReminderScheduler.restorePeriodReminderIfNeeded(context);
        }
    }
}

