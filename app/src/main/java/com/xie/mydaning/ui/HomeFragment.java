package com.xie.mydaning.ui;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.xie.mydaning.MainActivity;
import com.xie.mydaning.R;
import com.xie.mydaning.utils.DateUtils;
import com.xie.mydaning.utils.NotificationHelper;
import com.xie.mydaning.utils.PeriodCalculator;
import com.xie.mydaning.utils.ReminderScheduler;
import com.xie.mydaning.view.CircularProgressView;
import com.xie.mydaning.viewmodel.PeriodViewModel;

public class HomeFragment extends Fragment {
    private static final String PREFS_NAME = "period_settings";
    private static final String KEY_LAST_PERIOD_NOTIFY = "last_period_notify_day";
    private final SimpleDateFormat dayKeyFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    private PeriodViewModel viewModel;
    private TextView cycleStatus;
    private TextView currentDay;
    private TextView nextPeriodDate;
    private CircularProgressView progressRing;
    private TextView tvAverageCycle;
    private TextView tvAveragePeriod;
    private TextView tvRegularity;
    private TextView tvWaterReminder;
    private TextView tvPeriodReminder;
    private Button btnRecordToday;
    private Button btnHistory;
    private Button btnDrinkWater;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消监听器，避免内存泄漏
        if (prefs != null && preferenceChangeListener != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        
        cycleStatus = view.findViewById(R.id.cycle_status);
        currentDay = view.findViewById(R.id.current_day);
        nextPeriodDate = view.findViewById(R.id.next_period_date);
        progressRing = view.findViewById(R.id.progress_ring);
        tvAverageCycle = view.findViewById(R.id.tv_average_cycle);
        tvAveragePeriod = view.findViewById(R.id.tv_average_period);
        tvRegularity = view.findViewById(R.id.tv_regularity);
        tvWaterReminder = view.findViewById(R.id.tv_water_reminder);
        tvPeriodReminder = view.findViewById(R.id.tv_period_reminder);
        btnRecordToday = view.findViewById(R.id.btn_record_today);
        btnHistory = view.findViewById(R.id.btn_history);
        btnDrinkWater = view.findViewById(R.id.btn_drink_water);
        
        // 观察数据
        viewModel.getCurrentPeriodStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                updateCycleStatus(status);
            }
        });
        
        viewModel.getAverageCycle().observe(getViewLifecycleOwner(), cycle -> {
            if (cycle != null) {
                tvAverageCycle.setText(String.valueOf(cycle));
            }
        });
        
        viewModel.getAveragePeriod().observe(getViewLifecycleOwner(), period -> {
            if (period != null) {
                tvAveragePeriod.setText(String.valueOf(period));
            }
        });
        
        viewModel.getRegularity().observe(getViewLifecycleOwner(), regularity -> {
            if (regularity != null) {
                tvRegularity.setText(regularity + "%");
            }
        });
        
        // 监听设置变化，实时更新喝水提醒显示
        preferenceChangeListener = (sharedPreferences, key) -> {
            if ("water_reminder".equals(key) || "water_interval_minutes".equals(key)) {
                updateWaterReminderDisplay();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        
        // 初始更新喝水提醒显示
        updateWaterReminderDisplay();
        
        // 按钮点击事件
        btnRecordToday.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToRecord();
            }
        });
        
        btnHistory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHistory();
            }
        });
        
        btnDrinkWater.setOnClickListener(v -> {
            Toast.makeText(getContext(), "已记录喝水！", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateWaterReminderDisplay() {
        boolean enabled = prefs.getBoolean("water_reminder", true);
        int intervalMinutes = prefs.getInt("water_interval_minutes", 1);
        
        if (!enabled) {
            tvWaterReminder.setText("喝水提醒：已关闭");
            return;
        }
        
        if (intervalMinutes < 60) {
            tvWaterReminder.setText("喝水提醒：每" + intervalMinutes + "分钟");
        } else {
            int hours = intervalMinutes / 60;
            int minutes = intervalMinutes % 60;
            if (minutes == 0) {
                tvWaterReminder.setText("喝水提醒：每" + hours + "小时");
            } else {
                tvWaterReminder.setText("喝水提醒：每" + hours + "小时" + minutes + "分钟");
            }
        }
    }
    
    private void updateCycleStatus(PeriodCalculator.CurrentPeriodStatus status) {
        if (status.isActive) {
            cycleStatus.setText(getString(R.string.cycle_status_active));
            currentDay.setText("第 " + status.currentDay + " 天");
            
            // 计算进度
            int avgPeriod = viewModel.getAveragePeriod().getValue() != null ? 
                viewModel.getAveragePeriod().getValue() : 5;
            float progress = (status.currentDay / (float) avgPeriod) * 100f;
            progressRing.setProgress(Math.min(progress, 100f));
        } else {
            cycleStatus.setText(getString(R.string.cycle_status_inactive));
            currentDay.setText("第 0 天");
            progressRing.setProgress(0f);
        }
        
        if (status.nextPeriodDate != null) {
            nextPeriodDate.setText(DateUtils.formatDate(status.nextPeriodDate));
            
            // 更新经期提醒
            long daysUntilNext = DateUtils.getDaysBetween(new Date(), status.nextPeriodDate);
            if (daysUntilNext > 7) {
                tvPeriodReminder.setText("经期提醒：还有 " + (daysUntilNext - 7) + " 天");
            } else if (daysUntilNext > 0) {
                tvPeriodReminder.setText("经期提醒：还有 " + daysUntilNext + " 天");
            } else {
                tvPeriodReminder.setText("经期提醒：即将到来");
            }

            // 距离经期近时，触发强提醒（震动+声音+通知）
            if (getContext() != null && prefs.getBoolean("period_reminder", true) && daysUntilNext <= 2) {
                String key = dayKeyFormat.format(status.nextPeriodDate);
                String last = prefs.getString(KEY_LAST_PERIOD_NOTIFY, "");
                if (!key.equals(last)) {
                    String title = "经期提醒";
                    String content = daysUntilNext > 0
                            ? "预计还有 " + daysUntilNext + " 天开始，请做好准备。"
                            : "经期即将或已经开始，注意休息。";
                    NotificationHelper.notifyPeriodReminder(getContext(), title, content);
                    prefs.edit().putString(KEY_LAST_PERIOD_NOTIFY, key).apply();
                }
            }

            // 安排提前一周的闹铃级提醒（即使应用退出/重启也能收到）
            if (getContext() != null && prefs.getBoolean("period_reminder", true)) {
                ReminderScheduler.schedulePeriodReminder(requireContext(), status.nextPeriodDate);
            }
        }
    }
}

