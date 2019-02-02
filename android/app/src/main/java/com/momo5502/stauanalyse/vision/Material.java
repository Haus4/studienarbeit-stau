package com.momo5502.stauanalyse.vision;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Material {

    private Mat mat;
    private Bitmap bitmap;

    public Material(Mat mat) {
        this.mat = mat;
    }

    public Mat getMat() {
        return mat;
    }

    public Bitmap getBitmap() {
        if (bitmap != null) {
            return bitmap;
        }

        if(this.mat.channels() == 3) {
            Mat copy = new Mat();
            Imgproc.cvtColor(mat, copy, Imgproc.COLOR_BGR2RGB, 3);
            mat = copy;
        }

        bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }
}
