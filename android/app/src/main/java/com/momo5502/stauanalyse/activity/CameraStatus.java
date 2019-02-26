package com.momo5502.stauanalyse.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.momo5502.stauanalyse.camera.Camera;

public class CameraStatus extends LinearLayout {

    private Camera camera;

    private ImageView scene;
    private ImageView mask;

    private TextView title;
    private TextView description;

    public CameraStatus(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        title = new TextView(context);
        title.setTextSize(17);
        title.setText("Camera: " + camera.getId());
        title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        title.setPadding(5, 0, 0, 0);
        addView(title);

        description = new TextView(context);
        description.setTextSize(17);
        description.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        description.setPadding(5, 5, 0, 0);
        addView(description);

        scene = new ImageView(context);
        scene.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scene.setPadding(0, 20, 0, 0);
        addView(scene);

        mask = new ImageView(context);
        mask.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mask.setPadding(0, 5, 0, 0);
        addView(mask);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setPadding(0, 100, 0, 0);
        setOrientation(VERTICAL);
    }

    public void setScene(Bitmap bitmap) {
        scene.setImageBitmap(bitmap);
    }

    public void setMask(Bitmap bitmap) {
        mask.setImageBitmap(bitmap);
    }

    public void setDescription(String text) {
        description.setText(text);
    }
}
