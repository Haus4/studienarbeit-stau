package com.momo5502.stauanalyse.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

public class Downloader {

    private Thread thread;

    public void download(final Callback<byte[]> callback, final String url, final Map<String, String> headers) {
        if (isWorking()) {
            callback.run(null, new ConcurrentModificationException());
            return;
        }

        thread = new Thread(() -> {
            try {
                performDownload(callback, url, headers);
            } catch (Exception e) {
                callback.run(null, e);
            } finally {
                thread = null;
            }
        });

        thread.start();
    }

    public void await() {
        if (isWorking() && thread.isAlive()) {
            try {
                thread.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isWorking() {
        return thread != null;
    }

    private void performDownload(Callback<byte[]> callback, String urlString, Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        List<Byte> dataArray = new ArrayList<>();

        InputStream input = null;
        try {
            input = new BufferedInputStream(connection.getInputStream(), 8192);

            byte[] data = new byte[1024];

            int count;
            while ((count = input.read(data)) != -1) {
                for (int i = 0; i < count; ++i) {
                    dataArray.add(data[i]);
                }
            }

            input.close();
        } catch (Exception e) {
            if (input != null) input.close();
            throw e;
        }

        byte[] result = new byte[dataArray.size()];

        for (int i = 0; i < dataArray.size(); ++i) {
            result[i] = dataArray.get(i);
        }

        callback.run(result, null);
    }
}
