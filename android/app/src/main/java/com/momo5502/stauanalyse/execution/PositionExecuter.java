package com.momo5502.stauanalyse.execution;

import android.app.Activity;
import android.location.Location;

import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraFinder;
import com.momo5502.stauanalyse.camera.CameraLoader;
import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.position.DirectionCalculator;
import com.momo5502.stauanalyse.position.GlobalPositioningManager;
import com.momo5502.stauanalyse.position.Position;
import com.momo5502.stauanalyse.position.PositionHistory;

import java.util.List;
import java.util.Optional;

public class PositionExecuter implements Executer {

    private EventListener eventListener;
    private List<String> cameraFilter;

    public interface EventListener {
        void onCamerasLoaded(List<Camera> cameras);

        void onPositionChanged(Position position);

        void onRelevantCamerasChanged(List<Camera> cameras, Optional<Direction> direction);
    }

    private static final int POSITION_AVERAGE_COUNT = 5;
    private static final int RELEVANT_CAMERA_COUNT = 4;

    private GlobalPositioningManager globalPositioningManager;
    private PositionHistory positionHistory;
    private DirectionCalculator directionCalculator;

    private CameraLoader cameraLoader;
    private CameraFinder cameraFinder;

    public PositionExecuter(Activity context, EventListener eventListener) {
        this.eventListener = eventListener;

        cameraLoader = new CameraLoader();
        directionCalculator = new DirectionCalculator();
        positionHistory = new PositionHistory(POSITION_AVERAGE_COUNT);
        globalPositioningManager = new GlobalPositioningManager(context);

        cameraLoader.loadCameras((value, error) -> onCamerasLoaded(value, error));
        globalPositioningManager.setLocationCallback((location, error) -> onPositionChanged(location, error));
    }

    public void setCameraFilter(List<String> cameraFilter) {
        this.cameraFilter = cameraFilter;
        update();
    }

    @Override
    public void runFrame() {
        //update();
    }

    private void onPositionChanged(Location location, Exception error) {
        Position position = new Position(location);
        positionHistory.track(position);
        eventListener.onPositionChanged(position);

        update();
    }

    private void onCamerasLoaded(List<Camera> cameras, Exception error) {
        if (error != null) throw new RuntimeException(error);

        cameraFinder = new CameraFinder(cameras);
        eventListener.onCamerasLoaded(cameras);
    }

    private void update() {
        if (cameraFinder == null) return;

        Optional<Direction> direction = directionCalculator.getDirection(positionHistory);
        List<Camera> closestCameras = cameraFinder.findClosestCameras(RELEVANT_CAMERA_COUNT, positionHistory.getLast(), cameraFilter);
        eventListener.onRelevantCamerasChanged(closestCameras, direction);
    }
}
