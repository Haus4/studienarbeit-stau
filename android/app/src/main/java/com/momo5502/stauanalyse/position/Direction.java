package com.momo5502.stauanalyse.position;

import org.osmdroid.util.GeoPoint;

import java.util.Arrays;
import java.util.Optional;

// Direction only for A5, for other roads, the entire concept would have to be extended
public enum Direction {
    // Official beginning is Niederaula (50° 49′ N, 9° 33′ O) and ending is Weil am Rhein (47° 35′ N, 7° 36′ O).
    // For simplicity, Frankfurt and Basel have been chosen though.
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
