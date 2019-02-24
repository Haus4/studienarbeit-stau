package com.momo5502.stauanalyse.camera;

import com.momo5502.stauanalyse.backend.BackendConnector;
import com.momo5502.stauanalyse.backend.MultiImageLoader;
import com.momo5502.stauanalyse.speech.Speaker;
import com.momo5502.stauanalyse.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CameraImageFetcher {

    private Date lastPing;
    private Camera camera;
    private List<CameraImage> images = new ArrayList<>();
    private Callback<CameraImages> callback;
    private MultiImageLoader multiImageLoader;

    public CameraImageFetcher(BackendConnector backendConnector, Camera camera) {
        multiImageLoader = new MultiImageLoader(backendConnector);
        this.camera = camera;
    }

    public void setCallback(Callback<CameraImages> callback) {
        this.callback = callback;
    }

    public void work() {
        if (!multiImageLoader.isWorking() && (lastPing == null || (new Date().getTime() - lastPing.getTime()) > 29 * 1000)) {
            getNext();
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public void getNext() {
        lastPing = new Date();
        multiImageLoader.load((value, error) -> parseImages(value, error), camera.getId(), getLastImage());
    }

    public void parseImages(List<CameraImage> newImages, Exception error) {
        if (newImages != null) {
            newImages = addImages(newImages);
        }

        if (this.callback != null) {
            this.callback.run(new CameraImages(camera, newImages, images), error);
        }
    }

    public Date getLastImage() {
        return images.stream() //
                .reduce((f, s) -> f.getTime().getTime() > s.getTime().getTime() ? f : s) //
                .map(i -> i.getTime()) //
                .orElse(null);
    }

    private List<CameraImage> addImages(List<CameraImage> newImages) {
        Date lastImage = getLastImage();
        if (lastImage != null) {
            newImages = newImages.stream().filter(i -> i.getTime().getTime() > lastImage.getTime()).collect(Collectors.toList());
        }

        sortImages(newImages);
        images.addAll(newImages);
        sortImages(images);

        return newImages;
    }

    private void sortImages(List<CameraImage> images) {
        images.sort((o1, o2) -> (int) (o1.getTime().getTime() - o2.getTime().getTime()));
    }
}
