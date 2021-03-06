package com.momo5502.stauanalyse.camera;

import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.position.Position;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CameraFinder {
    private List<Camera> cameras;

    public CameraFinder(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public List<Camera> findClosestCameras(int count, Position position, List<String> filter, Optional<Direction> direction) {
        List<Camera> result = cameras.stream().filter(c -> filter == null || filter.contains(c.getId())).collect(Collectors.toList());

        sortCamerasByDistance(position, result);

        if (direction.isPresent()) {
            double distance = position.distanceSquared(direction.get().getOrigin());

            result = result.stream() //
                    .filter(c -> c.getLocation().distanceSquared(direction.get().getOrigin()) <= distance) //
                    .collect(Collectors.toList());
        }

        return result.stream().limit(count).collect(Collectors.toList());
    }

    private void sortCamerasByDistance(Position position, List<Camera> camerasToSort) {
        camerasToSort.sort((a, b) -> {
            double distance1 = position.distanceSquared(a.getLocation());
            double distance2 = position.distanceSquared(b.getLocation());
            if (distance1 < distance2) return -1;
            if (distance1 > distance2) return 1;
            return 0;
        });
    }
}
