package com.momo5502.stauanalyse.execution;

import com.momo5502.stauanalyse.backend.BackendConnector;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImage;
import com.momo5502.stauanalyse.camera.CameraImages;
import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.util.Downloader;
import com.momo5502.stauanalyse.vision.EvaluatedImage;
import com.momo5502.stauanalyse.vision.ImageEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationExecutor implements Executer {

    private Direction direction;
    private Map<Camera, ImageEvaluator> imageEvaluators = new HashMap<>();
    private BackendConnector backendConnector;
    private EventListener eventListener;

    public interface EventListener {
        void onImageEvaluation(Camera camera, EvaluatedImage evaluatedImage);

        void onDirectionChanged(Direction direction);
    }

    public EvaluationExecutor(BackendConnector backendConnector, EventListener eventListener) {
        this.backendConnector = backendConnector;
        this.eventListener = eventListener;
    }

    @Override
    public void runFrame() {

    }

    @Override
    public void onTerminate() {

    }

    public void updateDirection(Optional<Direction> direction) {
        synchronized (this) {
            if (!direction.isPresent() || this.direction == direction.get()) return;
            this.direction = direction.get();
            imageEvaluators = new HashMap<>();
            eventListener.onDirectionChanged(this.direction);
        }
    }

    public void updateCameras(List<Camera> cameras) {
        synchronized (this) {
            List<Camera> evaluatorsToRemove = imageEvaluators.keySet() //
                    .stream() //
                    .filter(c -> !cameras.contains(c)) //
                    .collect(Collectors.toList());

            evaluatorsToRemove.forEach(e -> imageEvaluators.remove(e));
        }
    }

    public void updateImages(CameraImages images) {
        synchronized (this) {
            boolean containsEvaluator = imageEvaluators.containsKey(images.getCamera());
            if (images.getImages(containsEvaluator).isEmpty()) {
                return;
            }

            if (!containsEvaluator) {
                createEvaluator(images);
            }

            if (!imageEvaluators.containsKey(images.getCamera())) {
                System.out.println("No evaluator found after creation.");
                return;
            }

            trainEvaluator(imageEvaluators.get(images.getCamera()), images, !containsEvaluator);
        }
    }

    private void createEvaluator(CameraImages images) {
        if (direction == null) return;

        Downloader downloader = new Downloader();

        backendConnector.mask(downloader, (value, error) -> {
            if (error != null) return;

            ImageEvaluator imageEvaluator = new ImageEvaluator(value);
            imageEvaluators.put(images.getCamera(), imageEvaluator);
        }, images.getCamera().getId(), direction);

        downloader.await();
    }

    private void trainEvaluator(ImageEvaluator imageEvaluator, CameraImages images, boolean requiresInitialization) {
        List<CameraImage> imageList = images.getImages(!requiresInitialization);

        if (imageList == null || imageList.isEmpty()) return;

        for (int i = 0; i < imageList.size(); ++i) {
            CameraImage image = imageList.get(i);

            if (i + 1 == imageList.size()) {
                EvaluatedImage evaluatedImage = imageEvaluator.evaluate(image);
                eventListener.onImageEvaluation(images.getCamera(), evaluatedImage);
            } else {
                imageEvaluator.train(image);
            }
        }
    }
}
