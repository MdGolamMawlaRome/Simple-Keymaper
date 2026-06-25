package com.gmr.simplekeymapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import         android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WindowManager windowManager;
    private View floatingButton;
    private boolean isFloatingButtonActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        Button btnAccessibility = new Button(this);
        btnAccessibility.setText("1. Enable Accessibility Permission");
        btnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Find 'Simple KeyMapper' and turn it ON", Toast.LENGTH_LONG).show();
            }
        });
        layout.addView(btnAccessibility);

        Button btnOverlayPermission = new Button(this);
        btnOverlayPermission.setText("2. Enable Overlay Permission");
        btnOverlayPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Overlay permission already granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        layout.addView(btnOverlayPermission);

        Switch toggleMapper = new Switch(this);
        toggleMapper.setText("Enable KeyMapper & Floating Button");
        toggleMapper.setTextSize(18);
        toggleMapper.setPadding(0, 50, 0, 0);

        toggleMapper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "Please grant overlay permission first!", Toast.LENGTH_LONG).show();
                        toggleMapper.setChecked(false);
                        return;
                    }
                    showFloatingButton();
                } else {
                    removeFloatingButton();
                }
            }
        });
        layout.addView(toggleMapper);

        setContentView(layout);
    }

    private void showFloatingButton() {
        if (isFloatingButtonActive) return;

        final Button btn = new Button(this);
        btn.setText("ON/OFF");
        btn.setBackgroundColor(0x88FF0000);
        floatingButton = btn;

        btn.setOnClickListener(new View.OnClickListener() {
            private boolean mapperModeActive = false;
            @Override
            public void onClick(View v) {
                mapperModeActive = !mapperModeActive;
                if (MapperAccessibilityService.instance != null) {
                    MapperAccessibilityService.instance.toggleService(mapperModeActive);
                    if (mapperModeActive) {
                        btn.setText("ACTIVE");
                        btn.setBackgroundColor(0xAA00FF00);
                        Toast.makeText(MainActivity.this, "KeyMapper Mode Activated", Toast.LENGTH_SHORT).show();
                    } else {
                        btn.setText("ON/OFF");
                        btn.setBackgroundColor(0x88FF0000);
                        Toast.makeText(MainActivity.this, "Normal Mouse Mode", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enable Accessibility Service first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 10;
        params.y = 300;

        windowManager.addView(floatingButton, params);
        isFloatingButtonActive = true;
    }

    private void removeFloatingButton() {
        if (isFloatingButtonActive && floatingButton != null) {
            windowManager.removeView(floatingButton);
            floatingButton = null;
            isFloatingButtonActive = false;

            if (MapperAccessibilityService.instance != null) {
                MapperAccessibilityService.instance.toggleService(false);
            }
            Toast.makeText(this, "KeyMapper completely stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeFloatingButton();
    }
        }
