package com.momo5502.stauanalyse.camera;

import java.util.List;

public class CameraImages {

    private Camera camera;
    private List<CameraImage> newImages;
    private List<CameraImage> allImages;

    public CameraImages(Camera camera, List<CameraImage> newImages, List<CameraImage> allImages) {
        this.camera = camera;
        this.newImages = newImages;
        this.allImages = allImages;
    }

    public Camera getCamera() {
        return camera;
    }

    public List<CameraImage> getImages(boolean onlyNewImages) {
        return onlyNewImages ? newImages : allImages;
    }
}
