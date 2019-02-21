package com.momo5502.stauanalyse.position;

import org.osmdroid.util.GeoPoint;

import java.util.Arrays;
import java.util.Optional;

public enum Direction {
    Frankfurt(0, 50.1109221, 8.6821267), Basel(1, 47.5595986, 7.5885761);

    private final int index;
    private final Position origin;

    Direction(int index, double latitude, double longitude) {
        this.index = index;
        this.origin = new Position(latitude, longitude);
    }

    public int getIndex() {
        return index;
    }

    public Position getOrigin() {
        return origin;
    }

    public static Optional<Direction> get(int index) {
        return Arrays.stream(Direction.values()).filter(d -> d.getIndex() == index).findFirst();
    }
}
