package com.xie.mydaning.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.xie.mydaning.utils.NotificationHelper;
import com.xie.mydaning.utils.ReminderScheduler;

/**
 * 接收喝水提醒闹铃并发送强提醒通知。
 * 收到提醒后自动安排下一次提醒，实现循环提醒。
 */
public class WaterReminderReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "period_settings";
    private static final String WAKE_LOCK_TAG = "WaterReminderReceiver::WakeLock";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WaterReminderReceiver", "收到喝水提醒广播！时间: " + System.currentTimeMillis());
        
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
                Log.d("WaterReminderReceiver", "已获取WakeLock");
            } catch (Exception e) {
                Log.e("WaterReminderReceiver", "获取WakeLock失败", e);
            }
        } else {
            Log.w("WaterReminderReceiver", "无法获取PowerManager");
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("water_reminder", true);
            if (!enabled) {
                Log.d("WaterReminderReceiver", "喝水提醒已关闭，取消下次提醒");
                // 如果已关闭，取消下次提醒
                ReminderScheduler.cancelWaterReminder(context);
                return;
            }

            Log.d("WaterReminderReceiver", "发送喝水提醒通知");
            // 发送强提醒通知（震动+声音+状态栏+唤醒屏幕）
            NotificationHelper.notifyWaterReminder(
                    context,
                    "💧 喝水提醒",
                    "记得补充水分，保持健康哦！"
            );

            // 自动安排下一次提醒（实现循环提醒）
            int intervalMinutes = prefs.getInt("water_interval_minutes", 1);
            Log.d("WaterReminderReceiver", "安排下一次提醒: " + intervalMinutes + "分钟后");
            ReminderScheduler.scheduleWaterReminder(context, intervalMinutes);
            Log.d("WaterReminderReceiver", "提醒处理完成");
        } catch (Exception e) {
            Log.e("WaterReminderReceiver", "处理提醒时出错", e);
        } finally {
            // 释放WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                    Log.d("WaterReminderReceiver", "已释放WakeLock");
                } catch (Exception e) {
                    Log.e("WaterReminderReceiver", "释放WakeLock失败", e);
                }
            }
        }
    }
}

