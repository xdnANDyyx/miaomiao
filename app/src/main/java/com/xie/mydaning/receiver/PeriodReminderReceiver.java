package com.xie.mydaning.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.xie.mydaning.utils.DateUtils;
import com.xie.mydaning.utils.NotificationHelper;

import java.util.Date;

/**
 * 提前一周的经期提醒闹铃接收器。
 */
public class PeriodReminderReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "period_settings";
    private static final String KEY_PERIOD_REMINDER_TIME = "period_reminder_time";
    private static final String WAKE_LOCK_TAG = "PeriodReminderReceiver::WakeLock";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PeriodReminderReceiver", "收到经期提醒广播！时间: " + System.currentTimeMillis());
        
        // 确保通知渠道已创建
        NotificationHelper.ensureChannels(context);
        
        // 获取WakeLock，确保设备被唤醒
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            try {
                wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        WAKE_LOCK_TAG
                );
                wakeLock.acquire(30 * 1000L); // 保持30秒，确保通知能发送
                Log.d("PeriodReminderReceiver", "已获取WakeLock");
            } catch (Exception e) {
                Log.e("PeriodReminderReceiver", "获取WakeLock失败", e);
            }
        } else {
            Log.w("PeriodReminderReceiver", "无法获取PowerManager");
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("period_reminder", true);
            long targetMillis = prefs.getLong(KEY_PERIOD_REMINDER_TIME, -1L);
            if (!enabled || targetMillis <= 0) {
                Log.d("PeriodReminderReceiver", "经期提醒已关闭或未设置");
                return;
            }

            Date targetDate = new Date(targetMillis + 7L * 24L * 60L * 60L * 1000L); // 目标经期日期（提前一周保存的触发点）
            String dateText = DateUtils.formatDate(targetDate);

            Log.d("PeriodReminderReceiver", "发送经期提醒通知: " + dateText);
            NotificationHelper.notifyPeriodReminder(
                    context,
                    "❤️ 经期提醒",
                    "预计一周后开始经期（" + dateText + "），请提前做好安排。"
            );
            Log.d("PeriodReminderReceiver", "提醒处理完成");
        } catch (Exception e) {
            Log.e("PeriodReminderReceiver", "处理提醒时出错", e);
        } finally {
            // 释放WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                    Log.d("PeriodReminderReceiver", "已释放WakeLock");
                } catch (Exception e) {
                    Log.e("PeriodReminderReceiver", "释放WakeLock失败", e);
                }
            }
        }
    }
}

