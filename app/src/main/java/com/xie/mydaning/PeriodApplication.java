package com.xie.mydaning;

import android.app.Application;

import com.xie.mydaning.utils.NotificationHelper;

public class PeriodApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化通知渠道，确保强提醒可用
        NotificationHelper.ensureChannels(this);
    }
}

