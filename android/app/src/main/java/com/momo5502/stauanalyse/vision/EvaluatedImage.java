package com.momo5502.stauanalyse.vision;

import org.opencv.core.Rect;
import org.opencv.core.Mat;

import java.util.List;

public class EvaluatedImage {
    private Material mask;
    private Material image;
    private List<Rect> objects;

    public EvaluatedImage(Material mask, Material image, List<Rect> objects) {
        this.mask = mask;
        this.image = image;
        this.objects = objects;
    }

    public EvaluatedImage(Material mask, Material image) {
        this(mask, image, null);
    }

    public EvaluatedImage(Mat mask, Mat image, List<Rect> objects) {
        this(new Material(mask), new Material(image), objects);
    }

    public EvaluatedImage(Mat mask, Mat image) {
        this(mask, image, null);
    }

    public Material getMask() {
        return mask;
    }

    public Material getImage() {
        return image;
    }

    public List<Rect> getObjects() {
        return objects;
    }
}
