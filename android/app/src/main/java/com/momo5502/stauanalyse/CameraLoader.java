package com.momo5502.stauanalyse;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CameraLoader {

    private final String CAMERA_LIST_1 = "https://www.svz-bw.de/kamera/kamera_A.txt";
    private final String CAMERA_LIST_2 = "https://www.svz-bw.de/kamera/kamera_B.txt";

    public void loadCameras(final CameraLoaderListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetch(listener);
            }
        }).start();
    }

    private void fetch(CameraLoaderListener listener) {
        try {
            String cameraData1 = fetchCameraData(CAMERA_LIST_1);
            String cameraData2 = fetchCameraData(CAMERA_LIST_2);

            CameraParser parser = new CameraParser();
            List<Camera> cameras1 = parser.parse(cameraData1);
            List<Camera> cameras2 = parser.parse(cameraData2);

            List<Camera> cameras = new ArrayList<>(cameras1);
            cameras.addAll(cameras2);

            listener.onLoad(cameras, null);
        } catch (Exception e) {
            listener.onLoad(null, e);
        }
    }

    private String fetchCameraData(String urlString) throws Exception {
        URL url = new URL(CAMERA_LIST_1);
        URLConnection connection = url.openConnection();
        connection.connect();

        StringBuilder builder = new StringBuilder();

        InputStream input = null;
        try {
            input = new BufferedInputStream(url.openStream(), 8192);

            byte data[] = new byte[1024];

            int count;
            while ((count = input.read(data)) != -1) {
                byte bytes[] = new byte[count];
                System.arraycopy(data, 0, bytes, 0, count);

                String str = new String(bytes);
                builder.append(str);
            }

            input.close();
        }
        catch (Exception e)
        {
            if(input != null) input.close();
            throw e;
        }

        return builder.toString();
    }
}
