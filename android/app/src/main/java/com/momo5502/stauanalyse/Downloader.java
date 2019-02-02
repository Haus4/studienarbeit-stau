package com.momo5502.stauanalyse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Downloader {

    public interface DownloadCallback {
        void onFinished(byte[] result, Exception error);
    }

    public void download(final DownloadCallback callback, final String url, final Map<String, String> headers) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    performDownload(callback, url, headers);
                } catch (Exception e) {
                    callback.onFinished(null, e);
                }
            }
        }).start();
    }

    private void performDownload(DownloadCallback callback, String urlString, Map<String, String> headers) throws IOException {
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

        callback.onFinished(result, null);
    }
}
