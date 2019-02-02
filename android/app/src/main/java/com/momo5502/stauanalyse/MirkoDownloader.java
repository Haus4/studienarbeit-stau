package com.momo5502.stauanalyse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MirkoDownloader {
    private static final String DL_URL = "http://172.16.72.180/fetcher.php";

    public interface Callback {
        void onFinished(List<byte[]> images, Exception error);
    }

    private Downloader downloader = new Downloader();

    public void getLatest(final Callback callback) {
        downloader.download(new Downloader.Callback() {
            @Override
            public void onFinished(byte[] result, Exception error) {
                if (error != null || result == null) {
                    callback.onFinished(null, error);
                } else {
                    List<byte[]> images = parseData(result);
                    callback.onFinished(images, null);
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
            index += 4 + img.length;

            images.add(img);
        }

        return images;
    }

    byte[] parseImage(byte[] stream, int index) {
        if (stream.length < index + 4) return null;

        byte[] lengthBytes = new byte[4];
        System.arraycopy(stream, index, lengthBytes, 0, lengthBytes.length);

        int length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (stream.length < index + 4 + length) return null;

        byte[] image = new byte[length];
        System.arraycopy(stream, index + 4, image, 0, image.length);

        return image;
    }
}
