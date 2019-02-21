package com.momo5502.stauanalyse.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraParser {
    public List<Camera> parse(String data) {
        List<Camera> list = new ArrayList<>();

        String lines[] = data.split("[\\r\\n]+");
        if (lines.length <= 0) return list;

        String keys[] = lines[0].split("\\t");

        for (int i = 1; i < lines.length; ++i) {
            Map<String, String> cameraData = new HashMap<>();
            String fields[] = lines[i].split("\\t");

            for (int j = 0; j < Math.min(fields.length, keys.length); ++j) {
                cameraData.put(keys[j], fields[j]);
            }

            list.add(new Camera(cameraData));
        }

        return list;
    }
}
