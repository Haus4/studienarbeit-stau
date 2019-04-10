package com.momo5502.stauanalyse.jam;

import com.momo5502.stauanalyse.camera.Camera;

public class JamStatus {

    private Camera camera;
    private int carLimit;
    private int detectedCars;

    public JamStatus(Camera camera, int carLimit, int detectedCars) {
        this.camera = camera;
        this.carLimit = carLimit;
        this.detectedCars = detectedCars;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getDetectedCars() {
        return detectedCars;
    }

    public int getCarLimit() {
        return carLimit;
    }

    public boolean isJammed() {
        int halfLimit = (getCarLimit() / 2);
        return (getDetectedCars() > halfLimit);
    }

    public int getJamPercent() {
        if (!isJammed()) return 0;

        int halfLimit = (getCarLimit() / 2);
        return Math.min((100 * (getDetectedCars() - halfLimit)) / (getCarLimit() - halfLimit), 100);
    }
}
