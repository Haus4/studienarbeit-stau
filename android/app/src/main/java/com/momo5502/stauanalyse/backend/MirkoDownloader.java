package com.momo5502.stauanalyse.backend;

import com.momo5502.stauanalyse.util.Callback;
import com.momo5502.stauanalyse.util.Downloader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MirkoDownloader {
    private static final String DL_URL = "http://192.168.0.215/fetcher.php";

    private Downloader downloader = new Downloader();

    public void getLatest(final Callback<List<byte[]>> callback) {
        downloader.download(new Callback<byte[]>() {
            @Override
            public void run(byte[] result, Exception error) {
                if (error != null || result == null) {
                    callback.run(null, error);
                } else {
                    List<byte[]> images = parseData(result);
                    callback.run(images, null);
                }
            }
        }, DL_URL, null);
    }

    List<byte[]> parseData(byte[] stream) {
        List<byte[]> images = new ArrayList<>();

        int index = 0;
        while (true) {
            byte[] img = parseImage(stream, index);
            if (img == null) break;
            index += 4 + 8 + img.length;

            images.add(img);
        }

        return images;
    }

    byte[] readBytesFromStream(byte[] stream, int index, int length) {
        if (stream.length < index + length) return null;

        byte[] result = new byte[length];
        System.arraycopy(stream, index, result, 0, result.length);
        return result;
    }

    byte[] parseImage(byte[] stream, int index) {
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
        Date ts = new Date(timestamp * 1000);
        System.out.println("Image: " + ts.toString());

        // Image
        return readBytesFromStream(stream, index, length);
    }
}
