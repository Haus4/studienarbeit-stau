package com.momo5502.stauanalyse.position;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.momo5502.stauanalyse.util.Callback;

import static android.content.Context.LOCATION_SERVICE;

public class GlobalPositioningManager {
    private Activity context;
    private Location location;
    private LocationManager locationManager;
    private Callback<Location> locationCallback;

    public GlobalPositioningManager(Activity context) {
        this.context = context;
        if (requestPermission()) {
            listen();
        }
    }

    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return false;
        }

        return true;
    }

    private void listen() {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location _location) {
                    location = _location;

                    if (locationCallback != null) {
                        locationCallback.run(location, null);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocationCallback(Callback<Location> locationCallback) {
        this.locationCallback = locationCallback;
    }
}
