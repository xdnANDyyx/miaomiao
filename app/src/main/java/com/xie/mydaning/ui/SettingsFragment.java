package com.xie.mydaning.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.xie.mydaning.R;
import com.xie.mydaning.viewmodel.PeriodViewModel;
import com.xie.mydaning.utils.ReminderScheduler;
import com.xie.mydaning.utils.PermissionHelper;

public class SettingsFragment extends Fragment {
    private PeriodViewModel viewModel;
    private SwitchMaterial switchPeriodReminder;
    private SwitchMaterial switchWaterReminder;
    private SeekBar seekbarWaterInterval;
    private TextView tvWaterInterval;
    private TextView tvWaterReminderDesc;
    private TextView tvAvgCycleLength;
    private TextView tvAvgPeriodLength;
    private TextView tvPermissionStatus;
    private Button btnCheckPermissions;
    
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "period_settings";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        
        switchPeriodReminder = view.findViewById(R.id.switch_period_reminder);
        switchWaterReminder = view.findViewById(R.id.switch_water_reminder);
        seekbarWaterInterval = view.findViewById(R.id.seekbar_water_interval);
        tvWaterInterval = view.findViewById(R.id.tv_water_interval);
        tvWaterReminderDesc = view.findViewById(R.id.tv_water_reminder_desc);
        tvAvgCycleLength = view.findViewById(R.id.tv_avg_cycle_length);
        tvAvgPeriodLength = view.findViewById(R.id.tv_avg_period_length);
        tvPermissionStatus = view.findViewById(R.id.tv_permission_status);
        btnCheckPermissions = view.findViewById(R.id.btn_check_permissions);
        
        // 更新权限状态显示
        updatePermissionStatus();
        
        // 权限检查按钮
        btnCheckPermissions.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });
        
        // 加载设置
        switchPeriodReminder.setChecked(prefs.getBoolean("period_reminder", true));
        switchWaterReminder.setChecked(prefs.getBoolean("water_reminder", true));
        // 喝水提醒改为分钟级：1-120分钟（0-119对应1-120分钟）
        int intervalMinutes = prefs.getInt("water_interval_minutes", 1);
        if (intervalMinutes < 1) intervalMinutes = 1;
        if (intervalMinutes > 120) intervalMinutes = 120;
        // 将分钟数转换为进度值（1分钟=0, 2分钟=1, ...）
        int progress = intervalMinutes - 1;
        seekbarWaterInterval.setProgress(progress);
        updateWaterInterval(intervalMinutes);
        // 恢复时同步定时器
        if (switchWaterReminder.isChecked()) {
            ReminderScheduler.scheduleWaterReminder(requireContext(), intervalMinutes);
        }
        
        // 观察统计数据
        viewModel.getAverageCycle().observe(getViewLifecycleOwner(), cycle -> {
            if (cycle != null) {
                tvAvgCycleLength.setText(cycle + "天");
            }
        });
        
        viewModel.getAveragePeriod().observe(getViewLifecycleOwner(), period -> {
            if (period != null) {
                tvAvgPeriodLength.setText(period + "天");
            }
        });
        
        // 设置监听器
        switchPeriodReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("period_reminder", isChecked).apply();
        });
        
        switchWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("water_reminder", isChecked).apply();
            if (isChecked) {
                int currentProgress = seekbarWaterInterval.getProgress();
                int intervalMinutes1 = currentProgress + 1; // 1-120分钟
                ReminderScheduler.scheduleWaterReminder(requireContext(), intervalMinutes1);
            } else {
                ReminderScheduler.cancelWaterReminder(requireContext());
            }
        });
        
        seekbarWaterInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int intervalMinutes = progress + 1; // 1-120分钟
                updateWaterInterval(intervalMinutes);
                prefs.edit().putInt("water_interval_minutes", intervalMinutes).apply();
                if (switchWaterReminder.isChecked()) {
                    ReminderScheduler.scheduleWaterReminder(requireContext(), intervalMinutes);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void updateWaterInterval(int intervalMinutes) {
        if (intervalMinutes < 60) {
            tvWaterInterval.setText(intervalMinutes + "分钟");
        } else {
            int hours = intervalMinutes / 60;
            int minutes = intervalMinutes % 60;
            if (minutes == 0) {
                tvWaterInterval.setText(hours + "小时");
            } else {
                tvWaterInterval.setText(hours + "小时" + minutes + "分钟");
            }
        }
        // 更新描述文本
        if (intervalMinutes < 60) {
            tvWaterReminderDesc.setText("每" + intervalMinutes + "分钟提醒一次");
        } else {
            int hours = intervalMinutes / 60;
            int minutes = intervalMinutes % 60;
            if (minutes == 0) {
                tvWaterReminderDesc.setText("每" + hours + "小时提醒一次");
            } else {
                tvWaterReminderDesc.setText("每" + hours + "小时" + minutes + "分钟提醒一次");
            }
        }
    }
    
    private void updatePermissionStatus() {
        String status = PermissionHelper.getPermissionStatus(requireContext());
        tvPermissionStatus.setText(status);
    }
    
    private void checkAndRequestPermissions() {
        boolean allGranted = PermissionHelper.checkAllPermissions(requireContext());
        
        if (allGranted) {
            Toast.makeText(requireContext(), "所有权限已授予！", Toast.LENGTH_SHORT).show();
            updatePermissionStatus();
            return;
        }
        
        // 请求精确闹钟权限
        PermissionHelper.requestExactAlarmPermission(requireContext());
        
        // 请求电池优化白名单
        PermissionHelper.requestIgnoreBatteryOptimizations(requireContext());
        
        Toast.makeText(requireContext(), "请按照提示授予所有权限", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次返回时更新权限状态
        updatePermissionStatus();
    }
}

