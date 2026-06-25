package com.example.simplekeymapper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class MapperAccessibilityService extends AccessibilityService {

    public static MapperAccessibilityService instance = null;
    private boolean isActive = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    public void toggleService(boolean status) {
        this.isActive = status;
    }

    public void simulateMouseLook(float deltaX, float deltaY) {
        if (!isActive) return;

        float centerX = 500f;
        float centerY = 1000f;

        Path swipePath = new Path();
        swipePath.moveTo(centerX, centerY);
        swipePath.lineTo(centerX + deltaX, centerY + deltaY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 80));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    public void simulateTargetClick(float targetX, float targetY) {
        if (!isActive) return;

        Path clickPath = new Path();
        clickPath.moveTo(targetX, targetY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
