package com.xie.mydaning.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.xie.mydaning.MainActivity;
import com.xie.mydaning.R;

/**
 * 强提醒通知工具类：创建渠道并发送带声音、振动、状态栏通知。
 */
public class NotificationHelper {
    public static final String CHANNEL_PERIOD = "period_reminder_channel";
    public static final String CHANNEL_WATER = "water_reminder_channel";

    private static final int PERIOD_NOTIFICATION_ID = 1001;
    private static final int WATER_NOTIFICATION_ID = 1002;

    // 增强震动模式：更强烈的震动（重复震动模式）
    private static final long[] VIBRATION_PATTERN = new long[]{0, 1000, 500, 1000, 500, 1000, 500, 1000};

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel periodChannel = new NotificationChannel(
                CHANNEL_PERIOD,
                "经期提醒",
                NotificationManager.IMPORTANCE_MAX  // 最高重要性，确保声音和震动
        );
        periodChannel.setDescription("经期提醒通知，包含声音和震动");
        periodChannel.enableVibration(true);
        periodChannel.setVibrationPattern(VIBRATION_PATTERN);
        periodChannel.setSound(alarmSound, attrs);
        periodChannel.setShowBadge(true);
        periodChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        periodChannel.enableLights(true);
        periodChannel.setLightColor(android.graphics.Color.RED);
        periodChannel.setBypassDnd(true);  // 绕过勿扰模式
        manager.createNotificationChannel(periodChannel);

        NotificationChannel waterChannel = new NotificationChannel(
                CHANNEL_WATER,
                "喝水提醒",
                NotificationManager.IMPORTANCE_MAX  // 最高重要性，确保声音和震动
        );
        waterChannel.setDescription("喝水提醒通知，包含声音和震动");
        waterChannel.enableVibration(true);
        waterChannel.setVibrationPattern(VIBRATION_PATTERN);
        waterChannel.setSound(alarmSound, attrs);
        waterChannel.setShowBadge(true);
        waterChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        waterChannel.enableLights(true);
        waterChannel.setLightColor(android.graphics.Color.BLUE);
        waterChannel.setBypassDnd(true);  // 绕过勿扰模式
        manager.createNotificationChannel(waterChannel);
    }

    public static void notifyPeriodReminder(Context context, String title, String content) {
        sendStrongNotification(context, CHANNEL_PERIOD, PERIOD_NOTIFICATION_ID, title, content);
    }

    public static void notifyWaterReminder(Context context, String title, String content) {
        sendStrongNotification(context, CHANNEL_WATER, WATER_NOTIFICATION_ID, title, content);
    }

    private static void sendStrongNotification(Context context,
                                               String channelId,
                                               int notificationId,
                                               String title,
                                               String content) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 无通知权限时直接返回，避免崩溃
            return;
        }

        // 创建点击通知后打开的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 创建全屏Intent，用于唤醒屏幕（需要USE_FULL_SCREEN_INTENT权限）
        PendingIntent fullScreenIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                fullScreenIntent = PendingIntent.getActivity(
                        context,
                        notificationId + 10000,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } catch (Exception e) {
                // 如果权限不足，忽略全屏Intent
            }
        }

        // 确保通知渠道已创建（在发送通知前）
        ensureChannels(context);
        
        // 在 Android 8.0+ 中，声音和震动应该通过通知渠道设置，而不是在 Builder 中
        // 但为了兼容旧版本，我们仍然在 Builder 中设置
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        
        // 增强震动模式：更强烈的震动（重复震动模式）
        long[] strongVibration = new long[]{0, 1000, 500, 1000, 500, 1000, 500, 1000};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)  // 最高优先级
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(fullScreenIntent, true)  // 全屏Intent，唤醒屏幕
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)  // 使用所有默认设置（声音、震动、灯光）
                .setLights(android.graphics.Color.RED, 1000, 1000)  // LED灯闪烁
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))  // 大文本样式
                .setOngoing(false)
                .setOnlyAlertOnce(false);  // 每次都提醒，不静音
        
        // 对于 Android 8.0 以下版本，需要在 Builder 中设置声音和震动
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(alarmSound)
                   .setVibrate(strongVibration);
        }

        // 发送通知
        manager.notify(notificationId, builder.build());
        
        // 额外确保：直接触发震动和声音作为备用方案
        triggerVibrationAndSound(context);
    }
    
    /**
     * 直接触发震动和声音，作为通知的备用方案
     * 确保即使通知系统有问题，也能触发提醒
     */
    private static void triggerVibrationAndSound(Context context) {
        // 触发震动
        try {
            Vibrator vibrator = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    vibrator = vibratorManager.getDefaultVibrator();
                }
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
            
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect effect = VibrationEffect.createWaveform(
                            VIBRATION_PATTERN,
                            -1  // 不重复，只震动一次
                    );
                    vibrator.vibrate(effect);
                } else {
                    vibrator.vibrate(VIBRATION_PATTERN, -1);
                }
            }
        } catch (Exception e) {
            // 震动失败时忽略，依赖通知系统
        }
        
        // 播放闹钟声音
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);
                if (ringtone != null) {
                    ringtone.play();
                }
            }
        } catch (Exception e) {
            // 声音播放失败时忽略，依赖通知系统
        }
    }
}

