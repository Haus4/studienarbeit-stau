package com.momo5502.stauanalyse.position;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

public class Position {

    private double latitude;
    private double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position(GeoPoint point) {
        latitude = point.getLatitude();
        longitude = point.getLongitude();
    }

    public Position(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

    public double distanceSquared(Position position) {
        double rawLat = position.getLatitude() - getLatitude();
        double rawLon = position.getLongitude() - getLongitude();

        return Math.pow(rawLat, 2) + Math.pow(rawLon, 2);
    }

    public double distance(Position position) {
        return Math.sqrt(distanceSquared(position));
    }
}
