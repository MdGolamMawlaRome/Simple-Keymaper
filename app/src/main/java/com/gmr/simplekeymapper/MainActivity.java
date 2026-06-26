package com.gmr.simplekeymapper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        Button btnAccessibility = new Button(this);
        btnAccessibility.setText("1. Enable Accessibility Permission");
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "Turn ON 'Simple Keymapper'", Toast.LENGTH_LONG).show();
        });
        layout.addView(btnAccessibility);

        Button btnOverlayPermission = new Button(this);
        btnOverlayPermission.setText("2. Enable Overlay Permission");
        btnOverlayPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Overlay permission allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        layout.addView(btnOverlayPermission);

        Switch toggleMapper = new Switch(this);
        toggleMapper.setText("Start Keymapper Engine");
        toggleMapper.setTextSize(18);
        toggleMapper.setPadding(0, 50, 0, 0);

        toggleMapper.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Grant overlay permission first!", Toast.LENGTH_LONG).show();
                    toggleMapper.setChecked(false);
                    return;
                }
                if (MapperAccessibilityService.instance != null) {
                    MapperAccessibilityService.instance.startKeymapperUI();
                    startKeepAliveService();
                    Toast.makeText(MainActivity.this, "Keymapper Floating Menu Active", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enable Accessibility Service first!", Toast.LENGTH_LONG).show();
                    toggleMapper.setChecked(false);
                }
            } else {
                if (MapperAccessibilityService.instance != null) {
                    MapperAccessibilityService.instance.stopKeymapperUI();
                }
                stopKeepAliveService();
            }
        });
        layout.addView(toggleMapper);

        setContentView(layout);
    }

    private void startKeepAliveService() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, "gmr_channel")
                    .setContentTitle("Simple Keymapper Running")
                    .setContentText("Mapping Service is active in background.")
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
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
