package com.xie.mydaning;

import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xie.mydaning.ui.HistoryFragment;
import com.xie.mydaning.ui.HomeFragment;
import com.xie.mydaning.ui.RecordFragment;
import com.xie.mydaning.ui.SettingsFragment;
import com.xie.mydaning.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private HomeFragment homeFragment;
    private RecordFragment recordFragment;
    private HistoryFragment historyFragment;
    private SettingsFragment settingsFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();
        requestBatteryOptimizationIfNeeded();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // 创建Fragment实例
        homeFragment = new HomeFragment();
        recordFragment = new RecordFragment();
        historyFragment = new HistoryFragment();
        settingsFragment = new SettingsFragment();
        
        // 默认显示首页
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }
        
        // 底部导航监听
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_record) {
                selectedFragment = recordFragment;
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = historyFragment;
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = settingsFragment;
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            
            return false;
        });
    }
    
    public void switchToHome() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
    
    public void switchToRecord() {
        bottomNavigation.setSelectedItemId(R.id.nav_record);
    }
    
    public void switchToHistory() {
        bottomNavigation.setSelectedItemId(R.id.nav_statistics);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1000
                );
            }
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        PermissionHelper.requestExactAlarmPermission(this);
    }
    
    private void requestBatteryOptimizationIfNeeded() {
        // 延迟请求，避免启动时弹出太多对话框
        getWindow().getDecorView().postDelayed(() -> {
            if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
                PermissionHelper.requestIgnoreBatteryOptimizations(this);
            }
        }, 2000);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            // 通知权限请求结果
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
            } else {
                // 权限被拒绝，可以显示提示
                android.widget.Toast.makeText(this, "需要通知权限才能接收提醒", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
}
