package com.momo5502.stauanalyse.position;

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
        if (points.size() <= 1) return null;

        synchronized (points) {
            double lat = 0.0;
            double lon = 0.0;

            int count = countForAverageComputation;
            if (points.size() <= countForAverageComputation) {
                count = points.size() / 2;
            }

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
        if (points.size() <= 1) return null;

        synchronized (points) {
            double lat = 0.0;
            double lon = 0.0;

            int count = countForAverageComputation;
            if (points.size() <= countForAverageComputation) {
                count = points.size() / 2;
            }

            if (count <= 0) return null;

            int start = points.size() - count;

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
