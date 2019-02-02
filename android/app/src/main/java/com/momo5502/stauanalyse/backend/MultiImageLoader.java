package com.momo5502.stauanalyse.backend;

import com.momo5502.stauanalyse.camera.CameraImage;
import com.momo5502.stauanalyse.util.Callback;
import com.momo5502.stauanalyse.util.Downloader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiImageLoader {
    private static final String DL_URL = "http://192.168.0.215/fetcher.php";

    private Downloader downloader = new Downloader();

    public void loadLatest(final Callback<List<CameraImage>> callback) {
        downloader.download((result, error) -> {
            if (error != null || result == null) {
                callback.run(null, error);
            } else {
                List<CameraImage> images = parseData(result);
                callback.run(images, null);
            }
        }, DL_URL, null);
    }

    private List<CameraImage> parseData(byte[] stream) {
        List<CameraImage> images = new ArrayList<>();

        int index = 0;
        while (true) {
            CameraImage img = parseImage(stream, index);
            if (img == null) break;
            index += 4 + 8 + img.getData().length;

            images.add(img);
        }

        return images;
    }

    private byte[] readBytesFromStream(byte[] stream, int index, int length) {
        if (stream.length < index + length) return null;

        byte[] result = new byte[length];
        System.arraycopy(stream, index, result, 0, result.length);
        return result;
    }

    private CameraImage parseImage(byte[] stream, int index) {
        // Length
        byte[] lengthBytes = readBytesFromStream(stream, index, 4);
        index += 4;
        if (lengthBytes == null) return null;

        int length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        // Timestamp
        byte[] timeStampBytes = readBytesFromStream(stream, index, 8);
        index += 8;
        if (timeStampBytes == null) return null;

        long timestamp = ByteBuffer.wrap(timeStampBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        Date date = new Date(timestamp * 1000);

        // Image
        byte[] image = readBytesFromStream(stream, index, length);
        if(image == null) return null;

        return new CameraImage(date, image);
    }
}
