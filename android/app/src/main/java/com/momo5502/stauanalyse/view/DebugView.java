package com.momo5502.stauanalyse.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class DebugView {

    private boolean gpsInitialized = false;
    private boolean directionDetected = false;
    private boolean camerasFetched = false;
    private boolean camerasDetected = false;
    private boolean cameraAnalyzed = false;
    private boolean voiceTriggered = false;

    private TextView textView;

    private Context context;

    public DebugView(Context context) {
        this.context = context;
    }

    public void markGpsInitialized() {
        gpsInitialized = true;
        update();
    }

    public void maskDirectionDetected() {
        directionDetected = true;
        update();
    }

    public void markCamerasFetched() {
        camerasFetched = true;
        update();
    }

    public void markCamerasDetected() {
        camerasDetected = true;
        update();
    }

    public void markCameraAnalyzed() {
        cameraAnalyzed = true;
        update();
    }

    public void markVoiceTriggered() {
        voiceTriggered = true;
        update();
    }

    public void update() {
        new Handler(Looper.getMainLooper()).post(this::updateInternal);
    }

    private void updateInternal() {
        if (textView != null) {
            String text = "<br>" +
                    "GPS initialized: " + formatValue(gpsInitialized) + "<br>" +
                    "Direction detected: " + formatValue(directionDetected) + "<br>" +
                    "Cameras fetched: " + formatValue(camerasFetched) + "<br>" +
                    "Cameras detected: " + formatValue(camerasDetected) + "<br>" +
                    "Cameras analyzed: " + formatValue(cameraAnalyzed) + "<br>" +
                    "Voice triggered: " + formatValue(voiceTriggered) + "<br>";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            }
        }
    }

    private static String formatValue(boolean value) {
        if (value) {
            return "<font color='green'>true</font>";
        } else {
            return "<font color='red'>false</font>";
        }
    }

    public View getView() {
        if (textView == null) {
            textView = new TextView(context);
            textView.setTextSize(18.0f);
        }

        update();
        return textView;
    }
}
