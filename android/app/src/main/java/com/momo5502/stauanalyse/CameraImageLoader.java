package com.momo5502.stauanalyse;

import java.util.HashMap;

public class CameraImageLoader {
    private Downloader downloader = new Downloader();

    public void get(String id, Downloader.DownloadCallback callback) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://www.svz-bw.de");

        downloader.download(callback, getUrl(id), headers);
    }

    private String getUrl(String camera) {
        return "https://www.svz-bw.de/kamera/ftpdata/" + camera + "/" + camera + "_gross.jpg";
    }
}
