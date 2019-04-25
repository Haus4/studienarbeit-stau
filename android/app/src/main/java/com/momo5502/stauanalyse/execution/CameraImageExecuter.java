package com.momo5502.stauanalyse.execution;

import com.momo5502.stauanalyse.backend.BackendConnector;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImageFetcher;
import com.momo5502.stauanalyse.camera.CameraImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CameraImageExecuter implements Executer {

    public interface EventListener {
        void onImagesReceived(CameraImages images);
    }

    private final BackendConnector backendConnector;
    private EventListener eventListener;

    private List<CameraImageFetcher> cameraImageFetchers = new ArrayList<>();

    public CameraImageExecuter(BackendConnector backendConnector, EventListener eventListener) {
        this.backendConnector = backendConnector;
        this.eventListener = eventListener;
    }

    public void updateCameras(List<Camera> cameras) {
        synchronized (this) {
            List<CameraImageFetcher> newFetchers = new ArrayList<>();
            cameras.forEach(camera -> {
                Optional<CameraImageFetcher> fetcher = cameraImageFetchers.stream() //
                        .filter(f -> f.getCamera().getId().equals(camera.getId())) //
                        .findFirst();

                if (!fetcher.isPresent()) {
                    fetcher = Optional.of(new CameraImageFetcher(backendConnector, camera));
                }

                fetcher.get().setCallback((value, error) -> onImagesReceived(value, error));

                newFetchers.add(fetcher.get());
            });

            cameraImageFetchers = newFetchers;
        }
    }

    @Override
    public void runFrame() {
        synchronized (this) {
            cameraImageFetchers.forEach(fetcher -> fetcher.work());
        }
    }

    @Override
    public void onTerminate() {

    }

    private void onImagesReceived(CameraImages images, Exception error) {
        eventListener.onImagesReceived(images);
    }
}
