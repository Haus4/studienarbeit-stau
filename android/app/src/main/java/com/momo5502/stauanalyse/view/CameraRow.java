package com.momo5502.stauanalyse.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.ScrollView;

import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.vision.EvaluatedImage;

public class CameraRow extends TextRow {
    private Camera camera;
    private EvaluatedImage evaluatedImage;
    private boolean jam;

    public CameraRow(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        setText(camera.getId());

        this.setOnClickListener(v -> show());
    }

    public void update(EvaluatedImage evaluatedImage, boolean jam) {
        this.evaluatedImage = evaluatedImage;
        this.jam = jam;
    }

    private void show() {
        CameraStatus cameraStatus = new CameraStatus(getContext(), camera);
        cameraStatus.setScene(evaluatedImage.getImage().getBitmap());
        cameraStatus.setMask(evaluatedImage.getMask().getBitmap());

        int cars = evaluatedImage.getObjects().size();

        if (jam) {
            cameraStatus.setDescription(cars + " Autos. Stau!.");
        } else {
            cameraStatus.setDescription(cars + " Autos. Kein Stau.");
        }

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(cameraStatus);

        Dialog dialog = new Dialog(getContext());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(scrollView);
        dialog.show();
    }
}
