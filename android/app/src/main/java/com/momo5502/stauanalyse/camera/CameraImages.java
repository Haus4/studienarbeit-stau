package com.momo5502.stauanalyse.camera;

import java.util.List;

public class CameraImages {

    private Camera camera;
    private List<CameraImage> images;

    public CameraImages(Camera camera, List<CameraImage> images) {
        this.camera = camera;
        this.images = images;
    }

    public Camera getCamera() {
        return camera;
    }

    public List<CameraImage> getImages() {
        return images;
    }
}
