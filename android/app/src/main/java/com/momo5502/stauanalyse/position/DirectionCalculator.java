package com.momo5502.stauanalyse.position;

import java.util.Optional;

public class DirectionCalculator {

    public Optional<Direction> getDirection(PositionHistory positionHistory) {
        Position start = positionHistory.getStart();
        Position end = positionHistory.getEnd();

        if (start == null || end == null) return Optional.empty();

        // Rough calculation, doesn't respect being north of Frankfurt or south of Basel.
        // This also neglects earth's curvature, as (over time) it becomes less relevant.
        if (getDistanceSquared(start, Direction.Frankfurt) > getDistanceSquared(end, Direction.Frankfurt)) {
            return Optional.of(Direction.Frankfurt);
        } else if (getDistanceSquared(start, Direction.Basel) > getDistanceSquared(end, Direction.Basel)) {
            return Optional.of(Direction.Basel);
        }

        return Optional.empty();
    }

    private double getDistanceSquared(Position point, Direction direction) {
        return point.distanceSquared(direction.getOrigin());
    }
}

