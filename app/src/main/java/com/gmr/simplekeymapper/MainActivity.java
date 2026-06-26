package com.gmr.simplekeymapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Switch toggleMapper;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 60);

        statusText = new TextView(this);
        statusText.setText("Checking permissions...");
        statusText.setTextSize(16);
        statusText.setPadding(0, 0, 0, 40);
        layout.addView(statusText);

        toggleMapper = new Switch(this);
        toggleMapper.setText("Start Keymapper Engine");
        toggleMapper.setTextSize(20);
        toggleMapper.setEnabled(false);
        layout.addView(toggleMapper);

        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndForcePermissions();
        syncSwitchState(); // অ্যাপে পুনরায় প্রবেশ করলে সুইচের অবস্থা ঠিক রাখার লজিক
    }

    // ব্যাকগ্রাউন্ড সার্ভিসের সাথে সুইচের স্টেট সিঙ্ক করার মেথড
    private void syncSwitchState() {
        if (toggleMapper != null) {
            toggleMapper.setOnCheckedChangeListener(null); // লুপ আটকানোর জন্য সাময়িক বন্ধ
            
            if (MapperAccessibilityService.instance != null && MapperAccessibilityService.instance.isUiRunning) {
                toggleMapper.setChecked(true);
            } else {
                toggleMapper.setChecked(false);
            }

            // পুনরায় লিসেনার যুক্ত করা
            toggleMapper.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (MapperAccessibilityService.instance != null) {
                        MapperAccessibilityService.instance.startKeymapperUI();
                        startKeepAliveService();
                        Toast.makeText(MainActivity.this, "Keymapper UI Active", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (MapperAccessibilityService.instance != null) {
                        MapperAccessibilityService.instance.stopKeymapperUI();
                    }
                    stopKeepAliveService();
                }
            });
        }
    }

    private void checkAndForcePermissions() {
        boolean overlayAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        boolean accessibilityAllowed = isAccessibilityServiceEnabled(this, MapperAccessibilityService.class);

        if (overlayAllowed && accessibilityAllowed) {
            statusText.setText("Status: All Permissions Granted! Ready to play.");
            statusText.setTextColor(0xFF00FF00);
            toggleMapper.setEnabled(true);
        } else {
            toggleMapper.setEnabled(false);
            toggleMapper.setChecked(false);
            statusText.setText("Status: Permissions Missing! Please allow them.");
            statusText.setTextColor(0xFFFF0000);
            showPermissionDialog(overlayAllowed, accessibilityAllowed);
        }
    }

    private void showPermissionDialog(boolean overlay, boolean accessibility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Required!");
        builder.setCancelable(false);

        if (!overlay) {
            builder.setMessage("Overlay Permission required. Click OK to enable.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(intent);
                }
            });
        } else if (!accessibility) {
            builder.setMessage("Accessibility Service required. Click OK, find 'Simple Keymapper' and turn it ON.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            });
        }
        builder.show();
    }

    private boolean isAccessibilityServiceEnabled(Context context, Class<?> service) {
        String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return settingValue != null && settingValue.contains(service.getCanonicalName());
    }

    private void startKeepAliveService() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, "gmr_channel")
                    .setContentTitle("Simple Keymapper Running")
                    .setContentText("Mapping Engine locked in background.")
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .setOngoing(true)
                    .build();
            manager.notify(99, notification);
        }
    }

    private void stopKeepAliveService() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(99);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gmr_channel", "Keymapper Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
