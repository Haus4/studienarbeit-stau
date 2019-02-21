package com.momo5502.stauanalyse.backend;

import com.momo5502.stauanalyse.util.Callback;
import com.momo5502.stauanalyse.util.Downloader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AvailableCamerasLoader {
    private BackendConnector backendConnector;

    public AvailableCamerasLoader(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    void load(final Callback<List<String>> callback) {
        backendConnector.cameras(new Downloader(), (value, error) -> {
            if (error != null) {
                callback.run(null, error);
                return;
            }

            List<String> cameras = parse(value);
            callback.run(cameras, null);
        });
    }

    private List<String> parse(byte[] data) {
        String text = new String(data);
        String[] cameras = text.split("\r?\n");

        return Arrays.stream(cameras) //
                .map(cam -> cam.trim()) //
                .filter(cam -> cam.length() > 0) //
                .collect(Collectors.toList());
    }
}
