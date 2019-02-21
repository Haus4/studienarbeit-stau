package com.momo5502.stauanalyse.position;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class PositionHistory {

    private List<Position> points = new ArrayList<>();
    private int countForAverageComputation;

    public PositionHistory(int countForAverageComputation) {
        this.countForAverageComputation = countForAverageComputation;
    }

    public void track(Position point) {
        synchronized (points) {
            points.add(point);
        }
    }

    public Position getStart() {
        if (points.isEmpty()) return null;

        synchronized (points) {
            double lat = 0.0;
            double lon = 0.0;

            int count = Math.min(countForAverageComputation, points.size());
            if (count == 0) return null;

            for (int i = 0; i < count; ++i) {
                Position point = points.get(i);

                lat += point.getLatitude();
                lon += point.getLongitude();
            }

            lat /= count;
            lon /= count;

            return new Position(lat, lon);
        }
    }

    public Position getEnd() {
        if (points.isEmpty()) return null;

        synchronized (points) {
            double lat = 0.0;
            double lon = 0.0;

            int start = Math.max(0, points.size() - countForAverageComputation);
            int count = points.size() - start;
            if (count == 0) return null;

            for (int i = start; i < points.size(); ++i) {
                Position point = points.get(i);

                lat += point.getLatitude();
                lon += point.getLongitude();
            }

            lat /= count;
            lon /= count;

            return new Position(lat, lon);
        }
    }

    public Position getLast() {
        if (points.isEmpty()) return null;
        return points.get(points.size() - 1);
    }
}
