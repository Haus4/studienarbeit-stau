package com.momo5502.stauanalyse.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.vision.EvaluatedImage;

public class CameraRow extends TableRow {
    private Context context;
    private Camera camera;
    private EvaluatedImage evaluatedImage;
    private boolean jam;

    public CameraRow(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.camera = camera;

        TextView textView = new TextView(context);
        textView.setText(camera.getId());
        textView.setTextSize(17);
        textView.setPadding(10, 30, 10, 30);
        addView(textView);

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        this.setBackgroundResource(outValue.resourceId);

        this.setOnClickListener(v -> show());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        View line = new View(context);
        line.setBackgroundColor(Color.rgb(210, 210, 210));
        line.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        ((TableLayout)getParent()).addView(line);
    }

    public void update(EvaluatedImage evaluatedImage, boolean jam) {
        this.evaluatedImage = evaluatedImage;
        this.jam = jam;
    }

    private void show() {
        CameraStatus cameraStatus = new CameraStatus(context, camera);
        cameraStatus.setScene(evaluatedImage.getImage().getBitmap());
        cameraStatus.setMask(evaluatedImage.getMask().getBitmap());

        int cars = evaluatedImage.getObjects().size();

        if (jam) {
            cameraStatus.setDescription(cars + " Autos. Stau!.");
        } else {
            cameraStatus.setDescription(cars + " Autos. Kein Stau.");
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(cameraStatus);

        Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(scrollView);
        dialog.show();
    }
}
